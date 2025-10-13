package com.t1.starter.pack.config;

import com.t1.starter.pack.aop.LoggingAspect;
import com.t1.starter.pack.repository.ErrorLogRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnClass(LoggingAspect.class)
@EnableConfigurationProperties(AopLoggingProperties.class)
@ConditionalOnProperty(prefix = "aop.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EntityScan("com.t1.starter.pack.entity")
@EnableJpaRepositories("com.t1.starter.pack.repository")
public class AspectConfig {
    @Bean
    @ConditionalOnMissingBean
    public LoggingAspect loggingAspect(AopLoggingProperties properties,
                                       KafkaTemplate<String, Object> kafkaTemplate,
                                       ErrorLogRepository errorLogRepository) {
        return new LoggingAspect(properties, kafkaTemplate, errorLogRepository);
    }
}
