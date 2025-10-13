package configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "credit.limit")
public class CreditLimitConfig {
    private BigDecimal maxTotalDebt = new BigDecimal("1000000");
    private Integer maxOverdueDays = 30;

    public BigDecimal getMaxTotalDebt() { return maxTotalDebt; }
    public void setMaxTotalDebt(BigDecimal maxTotalDebt) { this.maxTotalDebt = maxTotalDebt; }

    public Integer getMaxOverdueDays() { return maxOverdueDays; }
    public void setMaxOverdueDays(Integer maxOverdueDays) { this.maxOverdueDays = maxOverdueDays; }
}