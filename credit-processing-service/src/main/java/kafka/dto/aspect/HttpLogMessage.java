package kafka.dto.aspect;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpLogMessage {
    private LocalDate timestamp;
    private String methodSignature;
    private String requestUri;
    private Object parameters;
    private Object body;

    public HttpLogMessage() {}

    public HttpLogMessage(LocalDate timestamp, String methodSignature, String requestUri,
                          Object parameters, Object body) {
        this.timestamp = timestamp;
        this.methodSignature = methodSignature;
        this.requestUri = requestUri;
        this.parameters = parameters;
        this.body = body;
    }

    public LocalDate getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDate timestamp) { this.timestamp = timestamp; }

    public String getMethodSignature() { return methodSignature; }
    public void setMethodSignature(String methodSignature) { this.methodSignature = methodSignature; }

    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }

    public Object getParameters() { return parameters; }
    public void setParameters(Object parameters) { this.parameters = parameters; }

    public Object getBody() { return body; }
    public void setBody(Object body) { this.body = body; }
}