package kafka.dto;

public class ClientProductMessage {
    private Long productId;
    private Long clientId;

    public ClientProductMessage(Long productId, Long clientId) {
        this.productId = productId;
        this.clientId = clientId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
