package kafka.dto;

import java.math.BigDecimal;

public class ClientTransactionMessage {
    private Long accountId;
    private Long cardId;
    private BigDecimal amount;
    private String transactionType;
    private String description;

    public ClientTransactionMessage() {}

    public ClientTransactionMessage(Long accountId, Long cardId, BigDecimal amount, String transactionType, String description) {
        this.accountId = accountId;
        this.cardId = cardId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
