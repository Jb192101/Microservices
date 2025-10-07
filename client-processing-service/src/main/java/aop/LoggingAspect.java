package aop;

import aop.annotations.Cached;
import aop.annotations.LogDatasourceError;
import entity.ErrorLog;
import kafka.dto.aspect.ErrorLogMessage;
import kafka.dto.aspect.HttpLogMessage;
import kafka.dto.aspect.MetricLogMessage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import repository.ErrorLogRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Value("${spring.application.name:client-processing-service}")
    private String microserviceName;

    @Value("${metric.execution.limit:5000}")
    private long metricExecutionLimit;

    @Value("${cache.default.ttl:300}")
    private long defaultCacheTtl;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    private final Map<String, Map<Object, CacheEntry>> cacheStore = new ConcurrentHashMap<>();

    @Pointcut("@annotation(aop.annotations.LogDatasourceError)")
    public void logDatasourceErrorPointcut() {}

    @Pointcut("@annotation(aop.annotations.HttpOutcomeRequestLog)")
    public void httpOutcomeRequestLogPointcut() {}

    @Pointcut("@annotation(aop.annotations.HttpIncomeRequestLog)")
    public void httpIncomeRequestLogPointcut() {}

    @Pointcut("@annotation(aop.annotations.Metric)")
    public void metricPointcut() {}

    @Pointcut("@annotation(aop.annotations.Cached)")
    public void cachedPointcut() {}

    @Around("logDatasourceErrorPointcut()")
    public Object logDatasourceError(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            LogDatasourceError annotation = getLogDatasourceErrorAnnotation(joinPoint);
            String errorType = annotation != null ? annotation.type() : "ERROR";

            handleDatasourceError(joinPoint, ex, errorType);
            throw ex;
        }
    }

    @AfterReturning(
            pointcut = "httpOutcomeRequestLogPointcut()",
            returning = "result")
    public void logHttpOutcomeRequest(JoinPoint joinPoint, Object result) {
        try {
            HttpLogMessage logMessage = createHttpOutcomeLogMessage(joinPoint);
            sendToServiceLogs(logMessage, "INFO", "HTTP_OUTCOME");
        } catch (Exception ex) {
            log.error("Ошибка при отправке HTTP запроса: {}", ex.getMessage());
        }
    }

    @Before("httpIncomeRequestLogPointcut()")
    public void logHttpIncomeRequest(JoinPoint joinPoint) {
        try {
            HttpLogMessage logMessage = createHttpIncomeLogMessage(joinPoint);
            sendToServiceLogs(logMessage, "INFO", "HTTP_INCOME");
        } catch (Exception ex) {
            log.error("Ошибка при получении HTTP запроса: {}", ex.getMessage());
        }
    }

    @Around("metricPointcut()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Метод {} выполнен за {} мс", joinPoint.getSignature().toShortString(), executionTime);

            // Проверяем превышение лимита
            if (executionTime > metricExecutionLimit) {
                handleSlowMethod(joinPoint, executionTime);
            }
        }
    }

    @Around("cachedPointcut()")
    public Object cacheMethodResult(ProceedingJoinPoint joinPoint) throws Throwable {
        Cached cachedAnnotation = getCachedAnnotation(joinPoint);
        if (cachedAnnotation == null) {
            return joinPoint.proceed();
        }

        String cacheName = cachedAnnotation.cacheName();
        long ttl = cachedAnnotation.ttl() > 0 ? cachedAnnotation.ttl() : defaultCacheTtl;

        Object cacheKey = generateCacheKey(joinPoint);

        CacheEntry cachedResult = getFromCache(cacheName, cacheKey);
        if (cachedResult != null && !cachedResult.isExpired()) {
            log.debug("Кэш попадание для метода {} с ключом {}",
                    joinPoint.getSignature().toShortString(), cacheKey);
            return cachedResult.getValue();
        }

        log.debug("Кэш промах для метода {} с ключом {}",
                joinPoint.getSignature().toShortString(), cacheKey);
        Object result = joinPoint.proceed();

        if (result != null) {
            putToCache(cacheName, cacheKey, result, ttl);
        }

        return result;
    }

    // Обработка медленного метода
    private void handleSlowMethod(ProceedingJoinPoint joinPoint, long executionTime) {
        try {
            MetricLogMessage metricMessage = createMetricLogMessage(joinPoint, executionTime);
            sendToServiceLogs(metricMessage, "WARNING", "METRIC_SLOW_METHOD");
            log.warn("Медленный метод: {} выполнен за {} мс (лимит: {} мс)",
                    joinPoint.getSignature().toShortString(), executionTime, metricExecutionLimit);
        } catch (Exception ex) {
            log.error("Ошибка при обработке метрики медленного метода: {}", ex.getMessage());
        }
    }

    private MetricLogMessage createMetricLogMessage(ProceedingJoinPoint joinPoint, long executionTime) {
        return new MetricLogMessage(
                LocalDate.now(),
                joinPoint.getSignature().toShortString(),
                executionTime,
                metricExecutionLimit,
                joinPoint.getArgs(),
                microserviceName
        );
    }

    private Object generateCacheKey(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        if (args.length == 1 && args[0] instanceof Long) {
            return args[0];
        }

        return java.util.Objects.hash(args);
    }

    private CacheEntry getFromCache(String cacheName, Object key) {
        Map<Object, CacheEntry> cache = cacheStore.get(cacheName);
        if (cache == null) {
            return null;
        }

        CacheEntry entry = cache.get(key);
        if (entry != null && entry.isExpired()) {
            cache.remove(key);
            return null;
        }

        return entry;
    }

    private void putToCache(String cacheName, Object key, Object value, long ttl) {
        cacheStore.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>())
                .put(key, new CacheEntry(value, ttl));
    }

    // Метод для очистки просроченных записей
    public void cleanupExpiredCacheEntries() {
        for (Map<Object, CacheEntry> cache : cacheStore.values()) {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    private Cached getCachedAnnotation(ProceedingJoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            return method.getAnnotation(Cached.class);
        } catch (Exception e) {
            log.warn("Не удалось получить аннотацию Cached для метода: {}",
                    joinPoint.getSignature().toShortString());
            return null;
        }
    }

    private LogDatasourceError getLogDatasourceErrorAnnotation(ProceedingJoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            return method.getAnnotation(LogDatasourceError.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Существующие вспомогательные методы остаются без изменений...
    private void handleDatasourceError(ProceedingJoinPoint joinPoint, Exception ex, String errorType) {
        ErrorLogMessage errorMessage = createErrorMessage(joinPoint, ex);

        try {
            sendToServiceLogs(errorMessage, errorType, "DATASOURCE_ERROR");
        } catch (Exception kafkaEx) {
            saveToErrorLog(errorType, errorMessage);
        }

        logErrorToConsole(joinPoint, ex, errorType);
    }

    private ErrorLogMessage createErrorMessage(ProceedingJoinPoint joinPoint, Exception ex) {
        String methodSignature = joinPoint.getSignature().toShortString();

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        return new ErrorLogMessage(
                LocalDate.now(),
                methodSignature,
                stackTrace,
                ex.getMessage(),
                joinPoint.getArgs()
        );
    }

    private HttpLogMessage createHttpOutcomeLogMessage(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        String uri = extractUriFromArgs(args);
        Object body = extractBodyFromArgs(args);
        Object parameters = extractParametersFromArgs(args);

        return new HttpLogMessage(
                LocalDate.now(),
                joinPoint.getSignature().toShortString(),
                uri,
                parameters,
                body
        );
    }

    private HttpLogMessage createHttpIncomeLogMessage(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return new HttpLogMessage(
                    LocalDate.now(),
                    joinPoint.getSignature().toShortString(),
                    "unknown",
                    null,
                    null
            );
        }

        HttpServletRequest request = (HttpServletRequest) attributes.getRequest();
        Map<String, String> parameters = extractRequestParameters(request);
        Object body = extractRequestBody(joinPoint.getArgs());

        return new HttpLogMessage(
                LocalDate.now(),
                joinPoint.getSignature().toShortString(),
                request.getRequestURI(),
                parameters,
                body
        );
    }

    private void sendToServiceLogs(Object message, String type, String messageType) {
        try {
            if (kafkaTemplate == null) {
                log.warn("KafkaTemplate недоступен!");
                saveToErrorLog(type, message);
                return;
            }

            String jsonMessage = convertToJson(message);

            Message<String> kafkaMessage = MessageBuilder
                    .withPayload(jsonMessage)
                    .setHeader(KafkaHeaders.TOPIC, "service_logs")
                    .setHeader(KafkaHeaders.KEY, microserviceName)
                    .setHeader("type", type)
                    .setHeader("messageType", messageType)
                    .build();

            kafkaTemplate.send(kafkaMessage);
            log.debug("Сообщение отправлено в Kafka: {}", messageType);
        } catch (Exception ex) {
            log.error("Ошибка при отправке в Kafka: {}", ex.getMessage());
            saveToErrorLog(type, message);
        }
    }

    private void saveToErrorLog(String type, Object message) {
        try {
            String jsonMessage = convertToJson(message);
            ErrorLog errorLog = new ErrorLog(
                    microserviceName,
                    type,
                    jsonMessage,
                    LocalDate.now()
            );
            errorLogRepository.save(errorLog);
            log.debug("Ошибка при сохранении в БД: {}", type);
        } catch (Exception dbEx) {
            log.error("Ошибка при сохранении ошибки в БД: {}", dbEx.getMessage());
        }
    }

    private void logErrorToConsole(ProceedingJoinPoint joinPoint, Exception ex, String type) {
        log.error("=== ОШИБКА БД ===");
        log.error("Микросервис: {}", microserviceName);
        log.error("Tип: {}", type);
        log.error("Meтод: {}", joinPoint.getSignature().toShortString());
        log.error("Ошибка: {}", ex.getMessage());
        log.error("Время: {}", LocalDateTime.now());
        log.error("=========================");
    }

    private String extractUriFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String && ((String) arg).startsWith("http")) {
                return (String) arg;
            }
            if (arg instanceof java.net.URI) {
                return arg.toString();
            }
        }
        return "unknown";
    }

    private Object extractBodyFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof org.springframework.http.HttpEntity) {
                return ((org.springframework.http.HttpEntity<?>) arg).getBody();
            }
        }
        return null;
    }

    private Object extractParametersFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Map) {
                return arg;
            }
        }
        return null;
    }

    private Map<String, String> extractRequestParameters(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            parameters.put(paramName, request.getParameter(paramName));
        }
        return parameters;
    }

    private Object extractRequestBody(Object[] args) {
        for (Object arg : args) {
            if (arg != null &&
                    !(arg instanceof HttpServletRequest) &&
                    !(arg instanceof javax.servlet.http.HttpServletResponse) &&
                    !arg.getClass().getName().startsWith("java.")) {
                return arg;
            }
        }
        return null;
    }

    private String convertToJson(Object object) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Ошибка при сериализации сообщения в JSON: {}", e.getMessage());
            return "{\"error\": \"Ошибка при сериализации сообщения\"}";
        }
    }

    // Внутренний класс для хранения записей кэша
    private static class CacheEntry {
        private final Object value;
        private final long creationTime;
        private final long ttl;

        public CacheEntry(Object value, long ttl) {
            this.value = value;
            this.creationTime = System.currentTimeMillis();
            this.ttl = TimeUnit.SECONDS.toMillis(ttl);
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > creationTime + ttl;
        }
    }
}