package kafka.dto;

public class ClientCardMessage {
    private Long clientId;
    private Long accountId;
    private String paymentSystem;
    private String cardType;

    public ClientCardMessage() {}

    public ClientCardMessage(Long clientId, Long accountId, String paymentSystem, String cardType) {
        this.clientId = clientId;
        this.accountId = accountId;
        this.paymentSystem = paymentSystem;
        this.cardType = cardType;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getPaymentSystem() {
        return paymentSystem;
    }

    public void setPaymentSystem(String paymentSystem) {
        this.paymentSystem = paymentSystem;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
}
