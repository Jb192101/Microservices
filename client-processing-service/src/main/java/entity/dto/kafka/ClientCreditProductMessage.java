package entity.dto.kafka;

import entity.ClientProductStatus;
import entity.ProductKey;
import entity.dto.kafka.enums.ActionEnum;

import java.time.LocalDate;

public class ClientCreditProductMessage {
    private String clientId;
    private String productName;
    private ProductKey productKey;
    private LocalDate openDate;
    private LocalDate closeDate;
    private ClientProductStatus status;
    private ActionEnum action;
    private LocalDate timestamp;

    public ClientCreditProductMessage() {}

    public ClientCreditProductMessage(String clientId, String productName, ProductKey productKey, LocalDate openDate,
                                      LocalDate closeDate, ClientProductStatus status, ActionEnum action,
                                      LocalDate timestamp) {
        this.clientId = clientId;
        this.productName = productName;
        this.productKey = productKey;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ProductKey getProductKey() {
        return productKey;
    }

    public void setProductKey(ProductKey productKey) {
        this.productKey = productKey;
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
