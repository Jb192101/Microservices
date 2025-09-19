package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "ID клиента не может быть пустым!")
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @NotNull(message = "ID продукта не может быть пустым!")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull(message = "Баланс не может быть пустым!")
    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull(message = "Процентная ставка не может быть пустой!")
    @DecimalMin(value = "0.0", message = "Процентная ставка не может быть отрицательной!")
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @NotNull(message = "Флаг пересчёта не может быть пустым!")
    @Column(name = "is_recalc", nullable = false)
    private Boolean isRecalc = false;

    @NotNull(message = "Флаг существования карты не может быть пустым!")
    @Column(name = "card_exist", nullable = false)
    private Boolean cardExist = false;

    @NotNull(message = "Статус аккаунта не может быть пустым!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    public Account() {}

    public Account(Long id, Long clientId, Long productId, BigDecimal balance, BigDecimal interestRate,
                   Boolean isRecalc, Boolean cardExist, AccountStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.productId = productId;
        this.balance = balance;
        this.interestRate = interestRate;
        this.isRecalc = isRecalc;
        this.cardExist = cardExist;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Boolean getRecalc() {
        return isRecalc;
    }

    public void setRecalc(Boolean recalc) {
        isRecalc = recalc;
    }

    public Boolean getCardExist() {
        return cardExist;
    }

    public void setCardExist(Boolean cardExist) {
        this.cardExist = cardExist;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}