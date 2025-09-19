package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_registry")
public class PaymentRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Регистрация продукта не может быть пустой!")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_registry_id", nullable = false)
    private ProductRegistry productRegistry;

    @NotNull(message = "Дата оплаты не может быть пустой!")
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @NotNull(message = "Сумма не может быть пустой!")
    @Positive(message = "Сумма должна быть положительной!")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Значение процентной ставки не может быть пустым!")
    @Column(name = "interest_rate_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestRateAmount;

    @NotNull(message = "Сумма долга не может быть пустой!")
    @Column(name = "debt_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal debtAmount;

    @NotNull(message = "Флаг истечения срока действия не может быть пустым!")
    @Column(name = "expired", nullable = false)
    private Boolean expired = false;

    @NotNull(message = "Дата истечения срока действия не может быть пустой!")
    @Column(name = "payment_expiration_date", nullable = false)
    private LocalDate paymentExpirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    public PaymentRegistry() {}

    public PaymentRegistry(Long id, ProductRegistry productRegistry, LocalDate paymentDate, BigDecimal amount,
                           BigDecimal interestRateAmount, BigDecimal debtAmount, Boolean expired,
                           LocalDate paymentExpirationDate, PaymentStatus paymentStatus) {
        this.id = id;
        this.productRegistry = productRegistry;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.interestRateAmount = interestRateAmount;
        this.debtAmount = debtAmount;
        this.expired = expired;
        this.paymentExpirationDate = paymentExpirationDate;
        this.paymentStatus = paymentStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductRegistry getProductRegistry() {
        return productRegistry;
    }

    public void setProductRegistry(ProductRegistry productRegistry) {
        this.productRegistry = productRegistry;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getInterestRateAmount() {
        return interestRateAmount;
    }

    public void setInterestRateAmount(BigDecimal interestRateAmount) {
        this.interestRateAmount = interestRateAmount;
    }

    public BigDecimal getDebtAmount() {
        return debtAmount;
    }

    public void setDebtAmount(BigDecimal debtAmount) {
        this.debtAmount = debtAmount;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public LocalDate getPaymentExpirationDate() {
        return paymentExpirationDate;
    }

    public void setPaymentExpirationDate(LocalDate paymentExpirationDate) {
        this.paymentExpirationDate = paymentExpirationDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}