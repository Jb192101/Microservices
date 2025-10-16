package com.t1.starter.pack.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic serviceLogsTopic() {
        return TopicBuilder
                .name("service_logs")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
