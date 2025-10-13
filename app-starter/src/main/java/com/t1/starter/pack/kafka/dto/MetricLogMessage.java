package com.t1.starter.pack.kafka.dto;

import java.time.LocalDate;

public class MetricLogMessage {
    private LocalDate timestamp;
    private String methodSignature;
    private long executionTime;
    private long executionLimit;
    private Object[] methodArguments;
    private String microserviceName;

    public MetricLogMessage() {}

    public MetricLogMessage(LocalDate timestamp, String methodSignature, long executionTime,
                            long executionLimit, Object[] methodArguments, String microserviceName) {
        this.timestamp = timestamp;
        this.methodSignature = methodSignature;
        this.executionTime = executionTime;
        this.executionLimit = executionLimit;
        this.methodArguments = methodArguments;
        this.microserviceName = microserviceName;
    }

    public LocalDate getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDate timestamp) { this.timestamp = timestamp; }

    public String getMethodSignature() { return methodSignature; }
    public void setMethodSignature(String methodSignature) { this.methodSignature = methodSignature; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }

    public long getExecutionLimit() { return executionLimit; }
    public void setExecutionLimit(long executionLimit) { this.executionLimit = executionLimit; }

    public Object[] getMethodArguments() { return methodArguments; }
    public void setMethodArguments(Object[] methodArguments) { this.methodArguments = methodArguments; }

    public String getMicroserviceName() { return microserviceName; }
    public void setMicroserviceName(String microserviceName) { this.microserviceName = microserviceName; }
}
