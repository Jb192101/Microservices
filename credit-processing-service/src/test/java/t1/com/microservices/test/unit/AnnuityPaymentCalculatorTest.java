package t1.com.microservices.test.unit;

import kafka.dto.PaymentScheduleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import service.AnnuityPaymentCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AnnuityPaymentCalculatorTest {
    private AnnuityPaymentCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AnnuityPaymentCalculator();
    }

    @Test
    void calculateAnnuityPaymentValidInputReturnsCorrectPayment() {
        BigDecimal loanAmount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("12");
        Integer months = 12;

        BigDecimal result = calculator.calculateAnnuityPayment(loanAmount, annualRate, months);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(2, result.scale());
    }

    @Test
    void calculateAnnuityPaymentZeroLoanAmountThrowsException() {
        BigDecimal loanAmount = BigDecimal.ZERO;
        BigDecimal annualRate = new BigDecimal("12");
        Integer months = 12;

        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateAnnuityPayment(loanAmount, annualRate, months));
    }

    @Test
    void generatePaymentScheduleValidInputReturnsCorrectSchedule() {
        Long productRegistryId = 1L;
        BigDecimal loanAmount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("12");
        Integer months = 3;
        LocalDate startDate = LocalDate.of(2024, 1, 1);

        List<PaymentScheduleDTO> schedule = calculator.generatePaymentSchedule(
                productRegistryId, loanAmount, annualRate, months, startDate);

        assertNotNull(schedule);
        assertEquals(months, schedule.size());

        PaymentScheduleDTO firstPayment = schedule.get(0);
        assertEquals(productRegistryId, firstPayment.getProductRegistryId());
        assertEquals(LocalDate.of(2024, 2, 1), firstPayment.getPaymentDate());
        assertTrue(firstPayment.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}