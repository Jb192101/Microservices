package kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentScheduleDTO {
    @NotNull
    private Long productRegistryId;

    @NotNull
    private Integer paymentNumber;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @NotNull
    @Positive
    private BigDecimal totalAmount;

    @NotNull
    @Positive
    private BigDecimal interestAmount;

    @NotNull
    @Positive
    private BigDecimal principalAmount;

    @NotNull
    @Positive
    private BigDecimal remainingBalance;

    private LocalDate calculatedDate;
    private String scheduleType = "ANNUITY";

    public PaymentScheduleDTO() {
        this.calculatedDate = LocalDate.now();
    }

    public PaymentScheduleDTO(Long productRegistryId, Integer paymentNumber, LocalDate paymentDate,
                              BigDecimal totalAmount, BigDecimal interestAmount,
                              BigDecimal principalAmount, BigDecimal remainingBalance) {
        this();
        this.productRegistryId = productRegistryId;
        this.paymentNumber = paymentNumber;
        this.paymentDate = paymentDate;
        this.totalAmount = totalAmount;
        this.interestAmount = interestAmount;
        this.principalAmount = principalAmount;
        this.remainingBalance = remainingBalance;
    }

    // Getters and Setters
    public Long getProductRegistryId() { return productRegistryId; }
    public void setProductRegistryId(Long productRegistryId) { this.productRegistryId = productRegistryId; }

    public Integer getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(Integer paymentNumber) { this.paymentNumber = paymentNumber; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public LocalDate getCalculatedDate() { return calculatedDate; }
    public void setCalculatedDate(LocalDate calculatedDate) { this.calculatedDate = calculatedDate; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }
}