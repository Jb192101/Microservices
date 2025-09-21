package entity.dto.kafka;

import entity.dto.kafka.enums.ActionEnum;

import java.time.LocalDate;

public class ClientCardMessage {
    private String clientId;
    private String accountNumber;
    private String cardType;
    private String cardProduct;
    private LocalDate issueDate;
    private ActionEnum action;

    public ClientCardMessage(String clientId, String accountNumber, String cardType, String cardProduct,
                             LocalDate issueDate) {
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.cardType = cardType;
        this.cardProduct = cardProduct;
        this.issueDate = issueDate;
        this.action = action;
    }
}
