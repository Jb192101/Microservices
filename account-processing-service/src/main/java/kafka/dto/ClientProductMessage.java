package kafka.dto;

public class ClientProductMessage {
    private Long clientId;
    private Long productId;

    public ClientProductMessage() {}

    public ClientProductMessage(Long clientId, Long productId) {
        this.clientId = clientId;
        this.productId = productId;
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
}