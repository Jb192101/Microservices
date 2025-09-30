package aop;

import aop.annotations.LogDatasourceError;
import entity.ErrorLog;
import kafka.dto.aspect.ErrorLogMessage;
import kafka.dto.aspect.HttpLogMessage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Value("${spring.application.name:client-processing-service}")
    private String microserviceName;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @Pointcut("@annotation(aop.annotations.LogDatasourceError)")
    public void logDatasourceErrorPointcut() {}

    @Pointcut("@annotation(aop.annotations.HttpOutcomeRequestLog)")
    public void httpOutcomeRequestLogPointcut() {}

    @Pointcut("@annotation(aop.annotations.HttpIncomeRequestLog)")
    public void httpIncomeRequestLogPointcut() {}

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

    private LogDatasourceError getLogDatasourceErrorAnnotation(ProceedingJoinPoint joinPoint) {
        try {
            return (LogDatasourceError) joinPoint.getSignature()
                    .getClass()
                    .getMethod("getMethod")
                    .invoke(joinPoint.getSignature());
        } catch (Exception e) {
            return null;
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
}