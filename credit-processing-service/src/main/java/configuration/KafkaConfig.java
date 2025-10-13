package configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic clientProductsTopic() {
        return TopicBuilder
                .name("client-products")
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic clientCreditTopic() {
        return TopicBuilder
                .name("client-credit")
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic clientCardsTopic() {
        return TopicBuilder
                .name("client-cards")
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic clientTransactionsTopic() {
        return TopicBuilder
                .name("client-transactions")
                .partitions(3)
                .build();
    }
}
