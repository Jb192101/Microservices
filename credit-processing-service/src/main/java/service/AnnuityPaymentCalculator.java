package service;

import kafka.dto.PaymentScheduleDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnuityPaymentCalculator {
    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    // A = S × [i × (1 + i)^n] / [(1 + i)^n - 1]
    public BigDecimal calculateAnnuityPayment(BigDecimal loanAmount, BigDecimal annualRate, Integer months) {
        if (loanAmount.compareTo(BigDecimal.ZERO) <= 0 ||
                annualRate.compareTo(BigDecimal.ZERO) <= 0 ||
                months <= 0) {
            throw new IllegalArgumentException("Недопустимые входные параметры для аннуитетного расчёта");
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), SCALE, ROUNDING_MODE)
                .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);

        BigDecimal numerator = monthlyRate.multiply(
                BigDecimal.ONE.add(monthlyRate).pow(months)
        );

        BigDecimal denominator = BigDecimal.ONE.add(monthlyRate).pow(months).subtract(BigDecimal.ONE);

        return loanAmount.multiply(numerator.divide(denominator, SCALE, ROUNDING_MODE))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public List<PaymentScheduleDTO> generatePaymentSchedule(Long productRegistryId,
                                                            BigDecimal loanAmount,
                                                            BigDecimal annualRate,
                                                            Integer months,
                                                            LocalDate startDate) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), SCALE, ROUNDING_MODE)
                .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);

        BigDecimal annuityPayment = calculateAnnuityPayment(loanAmount, annualRate, months);
        BigDecimal remainingBalance = loanAmount;

        List<PaymentScheduleDTO> schedule = new ArrayList<>();
        LocalDate paymentDate = startDate.plusMonths(1);

        for (int i = 1; i <= months; i++) {
            BigDecimal interestAmount = remainingBalance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalAmount = annuityPayment.subtract(interestAmount);

            if (i == months) {
                principalAmount = remainingBalance;
                annuityPayment = principalAmount.add(interestAmount);
            }

            remainingBalance = remainingBalance.subtract(principalAmount);
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }

            PaymentScheduleDTO payment = new PaymentScheduleDTO(
                    productRegistryId, i, paymentDate,
                    annuityPayment, interestAmount, principalAmount, remainingBalance
            );

            schedule.add(payment);
            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }
}