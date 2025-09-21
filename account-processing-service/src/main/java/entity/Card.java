package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "cards", uniqueConstraints = {
        @UniqueConstraint(columnNames = "cardId")
})
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Card() {}

    public Card(Long id, Long accountId, String cardId, PaymentSystem paymentSystem, CardStatus status) {
        this.id = id;
        this.accountId = accountId;
        this.cardId = cardId;
        this.paymentSystem = paymentSystem;
        this.status = status;
    }

    @NotNull(message = "ID аккаунта не может быть пустым!")
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @NotBlank(message = "ID аккаунта не может быть пустым!")
    @Pattern(regexp = "\\d{16}", message = "Длина ID карты должна быть 16 символов")
    @Column(unique = true, nullable = false, length = 16)
    private String cardId;

    @NotNull(message = "Платёжная система не может быть пустой!")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_system", nullable = false, length = 20)
    private PaymentSystem paymentSystem;

    @NotNull(message = "Статус карты не может быть пустым!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

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

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public PaymentSystem getPaymentSystem() {
        return paymentSystem;
    }

    public void setPaymentSystem(PaymentSystem paymentSystem) {
        this.paymentSystem = paymentSystem;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }
}
