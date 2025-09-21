package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "ID аккаунта не может быть пустым!")
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @NotNull(message = "Дата оплаты не может быть пустой!")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @NotNull(message = "Сумма не может быть пустой!")
    @DecimalMin(value = "0.0", inclusive = false, message = "Сумма должна быть положительной!")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Флаг кредита не может быть пустым!")
    @Column(name = "is_credit", nullable = false)
    private Boolean isCredit;

    @Column(name = "payed_at")
    private LocalDateTime payedAt;

    @NotNull(message = "Тип оплаты не может быть пустым!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentType type;

    public Payment() {}

    public Payment(Long id, Long accountId, LocalDateTime paymentDate, BigDecimal amount, Boolean isCredit,
                   LocalDateTime payedAt, PaymentType type) {
        this.id = id;
        this.accountId = accountId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.isCredit = isCredit;
        this.payedAt = payedAt;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getIsCredit() {
        return isCredit;
    }

    public void setIsCredit(Boolean credit) {
        isCredit = credit;
    }

    public LocalDateTime getPayedAt() {
        return payedAt;
    }

    public void setPayedAt(LocalDateTime payedAt) {
        this.payedAt = payedAt;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }
}