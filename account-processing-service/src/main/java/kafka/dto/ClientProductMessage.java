package kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClientProductMessage {
    private Long clientId;
    private Long productId;
    private String productType; // DC, CC, NS, PENS
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public ClientProductMessage() {}

    public ClientProductMessage(Long clientId, Long productId, String productType, BigDecimal amount, LocalDateTime createdAt) {
        this.clientId = clientId;
        this.productId = productId;
        this.productType = productType;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}