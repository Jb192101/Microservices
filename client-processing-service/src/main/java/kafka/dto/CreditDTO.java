package kafka.dto;

import java.math.BigDecimal;

public class CreditDTO {
    private String clientId;
    private BigDecimal requestedAmount;

    public CreditDTO(String clientId, BigDecimal requestedAmount) {
        this.clientId = clientId;
        this.requestedAmount = requestedAmount;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
}
