package com.t1.starter.pack.aop;

import com.t1.starter.pack.aop.annotations.Cached;
import com.t1.starter.pack.aop.annotations.LogDatasourceError;
import com.t1.starter.pack.config.AopLoggingProperties;
import com.t1.starter.pack.entity.ErrorLog;
import com.t1.starter.pack.kafka.dto.ErrorLogMessage;
import com.t1.starter.pack.kafka.dto.HttpLogMessage;
import com.t1.starter.pack.kafka.dto.MetricLogMessage;
import com.t1.starter.pack.repository.ErrorLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final AopLoggingProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ErrorLogRepository errorLogRepository;
    private final Map<String, Map<Object, CacheEntry>> cacheStore = new ConcurrentHashMap<>();

    public LoggingAspect(AopLoggingProperties properties,
                         KafkaTemplate<String, Object> kafkaTemplate,
                         ErrorLogRepository errorLogRepository) {
        this.properties = properties;
        this.kafkaTemplate = kafkaTemplate;
        this.errorLogRepository = errorLogRepository;
    }

    @Pointcut("@annotation(com.example.aoplogging.annotations.LogDatasourceError)")
    public void logDatasourceErrorPointcut() {}

    @Pointcut("@annotation(com.example.aoplogging.annotations.HttpOutcomeRequestLog)")
    public void httpOutcomeRequestLogPointcut() {}

    @Pointcut("@annotation(com.example.aoplogging.annotations.HttpIncomeRequestLog)")
    public void httpIncomeRequestLogPointcut() {}

    @Pointcut("@annotation(com.example.aoplogging.annotations.Metric)")
    public void metricPointcut() {}

    @Pointcut("@annotation(com.example.aoplogging.annotations.Cached)")
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

            if (executionTime > properties.getMetricExecutionLimit()) {
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
        long ttl = cachedAnnotation.ttl() > 0 ? cachedAnnotation.ttl() : properties.getDefaultCacheTtl();

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

    private void handleSlowMethod(ProceedingJoinPoint joinPoint, long executionTime) {
        try {
            MetricLogMessage metricMessage = createMetricLogMessage(joinPoint, executionTime);
            sendToServiceLogs(metricMessage, "WARNING", "METRIC_SLOW_METHOD");
            log.warn("Медленный метод: {} выполнен за {} мс (лимит: {} мс)",
                    joinPoint.getSignature().toShortString(), executionTime, properties.getMetricExecutionLimit());
        } catch (Exception ex) {
            log.error("Ошибка при обработке метрики медленного метода: {}", ex.getMessage());
        }
    }

    private MetricLogMessage createMetricLogMessage(ProceedingJoinPoint joinPoint, long executionTime) {
        return new MetricLogMessage(
                LocalDate.now(),
                joinPoint.getSignature().toShortString(),
                executionTime,
                properties.getMetricExecutionLimit(),
                joinPoint.getArgs(),
                properties.getMicroserviceName()
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

    public void cleanupExpiredCacheEntries() {
        for (Map<Object, CacheEntry> cache : cacheStore.values()) {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    // Аннотации и вспомогательные методы...
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

    private void handleDatasourceError(ProceedingJoinPoint joinPoint, Exception ex, String errorType) {
        ErrorLogMessage errorMessage = createErrorMessage(joinPoint, ex);

        try {
            sendToServiceLogs(errorMessage, errorType, "DATASOURCE_ERROR");
        } catch (Exception kafkaEx) {
            if (properties.getDatabase().isEnabled()) {
                saveToErrorLog(errorType, errorMessage);
            }
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
        if (!properties.getKafka().isEnabled()) {
            log.debug("Kafka отправка отключена для: {}", messageType);
            return;
        }

        try {
            if (kafkaTemplate == null) {
                log.warn("KafkaTemplate недоступен!");
                if (properties.getDatabase().isEnabled()) {
                    saveToErrorLog(type, message);
                }
                return;
            }

            String jsonMessage = convertToJson(message);

            Message<String> kafkaMessage = MessageBuilder
                    .withPayload(jsonMessage)
                    .setHeader(KafkaHeaders.TOPIC, properties.getKafka().getTopic())
                    .setHeader(KafkaHeaders.KEY, properties.getMicroserviceName())
                    .setHeader("type", type)
                    .setHeader("messageType", messageType)
                    .build();

            kafkaTemplate.send(kafkaMessage);
            log.debug("Сообщение отправлено в Kafka: {}", messageType);
        } catch (Exception ex) {
            log.error("Ошибка при отправке в Kafka: {}", ex.getMessage());
            if (properties.getDatabase().isEnabled()) {
                saveToErrorLog(type, message);
            }
        }
    }

    private void saveToErrorLog(String type, Object message) {
        try {
            String jsonMessage = convertToJson(message);
            ErrorLog errorLog = new ErrorLog(
                    properties.getMicroserviceName(),
                    type,
                    jsonMessage,
                    LocalDate.now()
            );
            errorLogRepository.save(errorLog);
            log.debug("Ошибка сохранена в БД: {}", type);
        } catch (Exception dbEx) {
            log.error("Ошибка при сохранении ошибки в БД: {}", dbEx.getMessage());
        }
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
            if (arg instanceof java.util.Map) {
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
                    !(arg instanceof javax.servlet.http.HttpServletRequest) &&
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

    private void logErrorToConsole(ProceedingJoinPoint joinPoint, Exception ex, String type) {
        log.error("=== ОШИБКА БД ===");
        log.error("Микросервис: {}", properties.getMicroserviceName());
        log.error("Tип: {}", type);
        log.error("Meтод: {}", joinPoint.getSignature().toShortString());
        log.error("Ошибка: {}", ex.getMessage());
        log.error("Время: {}", LocalDateTime.now());
        log.error("=========================");
    }

    private static class CacheEntry {
        private final Object value;
        private final long creationTime;
        private final long ttl;

        public CacheEntry(Object value, long ttl) {
            this.value = value;
            this.creationTime = System.currentTimeMillis();
            this.ttl = TimeUnit.SECONDS.toMillis(ttl);
        }

        public Object getValue() { return value; }
        public boolean isExpired() { return System.currentTimeMillis() > creationTime + ttl; }
    }
}