package kafka.dto.aspect;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorLogMessage {
    private LocalDate timestamp;
    private String methodSignature;
    private String stackTrace;
    private String errorMessage;
    private Object[] methodParameters;

    public ErrorLogMessage() {}

    public ErrorLogMessage(LocalDate timestamp, String methodSignature, String stackTrace,
                           String errorMessage, Object[] methodParameters) {
        this.timestamp = timestamp;
        this.methodSignature = methodSignature;
        this.stackTrace = stackTrace;
        this.errorMessage = errorMessage;
        this.methodParameters = methodParameters != null ? Arrays.copyOf(methodParameters, methodParameters.length) : null;
    }

    public LocalDate getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDate timestamp) { this.timestamp = timestamp; }

    public String getMethodSignature() { return methodSignature; }
    public void setMethodSignature(String methodSignature) { this.methodSignature = methodSignature; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Object[] getMethodParameters() { return methodParameters; }
    public void setMethodParameters(Object[] methodParameters) { this.methodParameters = methodParameters; }
}
