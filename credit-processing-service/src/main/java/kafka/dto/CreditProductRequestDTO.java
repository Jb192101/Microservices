package kafka.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreditProductRequestDTO {
    @NotNull
    private Long clientId;

    @NotNull
    private Long accountId;

    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @Positive
    private BigDecimal interestRate;

    @NotNull
    @Positive
    private Integer monthCount;

    public CreditProductRequestDTO() {}

    public CreditProductRequestDTO(Long clientId, Long accountId, Long productId,
                                   BigDecimal amount, BigDecimal interestRate, Integer monthCount) {
        this.clientId = clientId;
        this.accountId = accountId;
        this.productId = productId;
        this.amount = amount;
        this.interestRate = interestRate;
        this.monthCount = monthCount;
    }

    // Getters and Setters
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public Integer getMonthCount() { return monthCount; }
    public void setMonthCount(Integer monthCount) { this.monthCount = monthCount; }
}
