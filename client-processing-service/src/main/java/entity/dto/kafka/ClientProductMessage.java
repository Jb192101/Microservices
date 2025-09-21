package entity.dto.kafka;

import entity.ClientProductStatus;
import entity.dto.kafka.enums.ActionEnum;

import java.time.LocalDate;

public class ClientProductMessage {
    private String clientId;
    private String productKey;
    private Long productId;
    private LocalDate openDate;
    private LocalDate closeDate;
    private ClientProductStatus status;
    private ActionEnum action;
    private LocalDate timestamp;

    public ClientProductMessage() {}

    public ClientProductMessage(String clientId, String productKey, Long productId, LocalDate openDate,
                                LocalDate closeDate, ClientProductStatus status, ActionEnum action,
                                LocalDate timestamp) {
        this.clientId = clientId;
        this.productKey = productKey;
        this.productId = productId;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.status = status;
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public ClientProductStatus getStatus() {
        return status;
    }

    public void setStatus(ClientProductStatus status) {
        this.status = status;
    }

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }
}
