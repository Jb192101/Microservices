package kafka.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ClientPaymentMessage {
    private UUID id;
    private Long accountId;
    private BigDecimal amount;
    private String paymentType;

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
}