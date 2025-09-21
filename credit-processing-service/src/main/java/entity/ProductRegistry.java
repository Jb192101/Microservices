package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product_registry")
public class ProductRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "ID клиента не может быть пустым!")
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @NotNull(message = "ID аккаунта не может быть пустым!")
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @NotNull(message = "ID продукта не может быть пустым!")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull(message = "Процентная ставка не может быть пустой!")
    @Positive(message = "Процентная ставка должна быть положительной!")
    @Column(name = "interest_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal interestRate;

    @NotNull(message = "Дата открытия не может быть пустой!")
    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name="month_count", nullable = false)
    private Integer monthCount;

    public ProductRegistry(Long id, Long clientId, Long accountId, Long productId, BigDecimal interestRate,
                           LocalDate openDate, LocalDate closeDate, ProductStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.accountId = accountId;
        this.productId = productId;
        this.interestRate = interestRate;
        this.openDate = openDate;
        this.closeDate = closeDate;
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
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

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}