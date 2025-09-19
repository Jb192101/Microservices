package configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Profile("account")
@EnableJpaRepositories(basePackages = "repository")
@EntityScan("entity")
@EnableTransactionManagement
public class AccountProcessingConfig {
    @Bean
    @Profile("account")
    public CommandLineRunner accountRunner() {
        return args -> {
            System.out.println("Микросервис обработки аккаунтов запустился!");
        };
    }
}