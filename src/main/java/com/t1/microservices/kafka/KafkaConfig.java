package com.t1.microservices.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    public NewTopic clientProductsTopic() {
        return TopicBuilder
                .name("client-products")
                .partitions(3)
                .build();
    }

    public NewTopic clientCreditTopic() {
        return TopicBuilder
                .name("client-credit")
                .partitions(3)
                .build();
    }

    public NewTopic clientCardsTopic() {
        return TopicBuilder
                .name("client-cards")
                .partitions(3)
                .build();
    }

    public NewTopic clientTransactionsTopic() {
        return TopicBuilder
                .name("client-transactions")
                .partitions(3)
                .build();
    }
}
