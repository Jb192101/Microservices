package com.t1.starter.pack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aop.logging")
public class AopLoggingProperties {
    private String microserviceName = "default-service";
    private long metricExecutionLimit = 5000;
    private long defaultCacheTtl = 300;
    private boolean enabled = true;
    private Kafka kafka = new Kafka();
    private Database database = new Database();

    public static class Kafka {
        private boolean enabled = true;
        private String topic = "service_logs";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
    }

    public static class Database {
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public String getMicroserviceName() { return microserviceName; }
    public void setMicroserviceName(String microserviceName) { this.microserviceName = microserviceName; }
    public long getMetricExecutionLimit() { return metricExecutionLimit; }
    public void setMetricExecutionLimit(long metricExecutionLimit) { this.metricExecutionLimit = metricExecutionLimit; }
    public long getDefaultCacheTtl() { return defaultCacheTtl; }
    public void setDefaultCacheTtl(long defaultCacheTtl) { this.defaultCacheTtl = defaultCacheTtl; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }
}
