package t1.com.microservices.test.unit;

import entity.Payment;
import entity.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.PaymentRepository;
import service.AccountService;
import service.PaymentService;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setAccountId(100L);
        testPayment.setAmount(BigDecimal.valueOf(500));
        testPayment.setIsCredit(true);
        testPayment.setType(PaymentType.LOAN_PAYMENT);
    }

    @Test
    void createPaymentWithValidDataShouldCreatePayment() {
        when(accountService.getAccountById(100L)).thenReturn(null);
        when(accountService.updateAccountBalance(anyLong(), any(BigDecimal.class), anyBoolean())).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        doNothing().when(transactionService).createTransactionFromPayment(any(Payment.class));

        Payment result = paymentService.createPayment(testPayment);

        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
        verify(accountService).updateAccountBalance(anyLong(), any(BigDecimal.class), anyBoolean());
        verify(transactionService).createTransactionFromPayment(any(Payment.class));
    }

    @Test
    void createPaymentWithInvalidAmountShouldThrowException() {
        testPayment.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(testPayment));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentsByAccountIdShouldReturnPayments() {
        List<Payment> expectedPayments = Arrays.asList(testPayment);
        when(paymentRepository.findByAccountId(100L)).thenReturn(expectedPayments);

        List<Payment> result = paymentService.getPaymentsByAccountId(100L);

        assertEquals(expectedPayments, result);
        verify(paymentRepository).findByAccountId(100L);
    }

    @Test
    void calculateCreditDebtShouldReturnTotalUnpaidAmount() {
        Payment unpaidPayment1 = new Payment();
        unpaidPayment1.setAmount(BigDecimal.valueOf(100));

        Payment unpaidPayment2 = new Payment();
        unpaidPayment2.setAmount(BigDecimal.valueOf(200));

        List<Payment> unpaidPayments = Arrays.asList(unpaidPayment1, unpaidPayment2);

        when(paymentRepository.findUnpaidPaymentsByAccountId(100L)).thenReturn(unpaidPayments);

        BigDecimal result = paymentService.calculateCreditDebt(100L);

        assertEquals(BigDecimal.valueOf(300), result);
        verify(paymentRepository).findUnpaidPaymentsByAccountId(100L);
    }

    @Test
    void isPaymentDueWithDuePaymentsShouldReturnTrue() {
        Payment duePayment = new Payment();
        duePayment.setPayedAt(null);
        duePayment.setExpired(false);

        List<Payment> duePayments = Arrays.asList(duePayment);

        when(paymentRepository.findByAccountIdAndPaymentDateBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(duePayments);

        boolean result = paymentService.isPaymentDue(100L);

        assertTrue(result);
    }

    @Test
    void isPaymentDueWithNoDuePaymentsShouldReturnFalse() {
        when(paymentRepository.findByAccountIdAndPaymentDateBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        boolean result = paymentService.isPaymentDue(100L);

        assertFalse(result);
    }

    @Test
    void updateExistingPaymentsShouldUpdateAllPayments() {
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();
        List<Payment> payments = Arrays.asList(payment1, payment2);

        when(paymentRepository.findByAccountId(100L)).thenReturn(payments);
        when(paymentRepository.saveAll(anyList())).thenReturn(payments);

        paymentService.updateExistingPayments(100L);

        verify(paymentRepository).findByAccountId(100L);
        verify(paymentRepository).saveAll(payments);
        assertNotNull(payment1.getPayedAt());
        assertNotNull(payment2.getPayedAt());
    }
}