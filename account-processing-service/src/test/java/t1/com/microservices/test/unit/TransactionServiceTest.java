package t1.com.microservices.test.unit;

import entity.Transaction;
import entity.TransactionStatus;
import entity.TransactionType;
import entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.TransactionRepository;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setAccountId(100L);
        testTransaction.setCardId(200L);
        testTransaction.setAmount(BigDecimal.valueOf(500));
        testTransaction.setType(TransactionType.DEPOSIT);
        testTransaction.setStatus(TransactionStatus.COMPLETE);
        testTransaction.setTimestamp(LocalDateTime.now());

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setAccountId(100L);
        testPayment.setAmount(BigDecimal.valueOf(300));
        testPayment.setIsCredit(true);
    }

    @Test
    void getAllTransactionsShouldReturnAllTransactions() {
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll()).thenReturn(expectedTransactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertEquals(expectedTransactions, result);
        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactionByIdWhenTransactionExistsShouldReturnTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Transaction result = transactionService.getTransactionById(1L);

        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAccountId(), result.getAccountId());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void getTransactionByIdWhenTransactionNotExistsShouldThrowException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.getTransactionById(1L));
        assertEquals("Transaction not found with id: 1", exception.getMessage());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void getTransactionsByAccountIdShouldReturnTransactions() {
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByAccountId(100L)).thenReturn(expectedTransactions);

        List<Transaction> result = transactionService.getTransactionsByAccountId(100L);

        assertEquals(expectedTransactions, result);
        verify(transactionRepository).findByAccountId(100L);
    }

    @Test
    void getTransactionsByAccountIdAndDateRangeShouldReturnFilteredTransactions() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByAccountIdAndDateRange(100L, start, end))
                .thenReturn(expectedTransactions);

        List<Transaction> result = transactionService
                .getTransactionsByAccountIdAndDateRange(100L, start, end);

        assertEquals(expectedTransactions, result);
        verify(transactionRepository).findByAccountIdAndDateRange(100L, start, end);
    }

    @Test
    void getAccountBalanceWithDepositsAndWithdrawalsShouldCalculateCorrectBalance() {
        when(transactionRepository.getTotalDepositsByAccountId(100L)).thenReturn(1500.0);
        when(transactionRepository.getTotalWithdrawalsByAccountId(100L)).thenReturn(500.0);

        BigDecimal result = transactionService.getAccountBalance(100L);

        assertEquals(BigDecimal.valueOf(1000.0), result);
        verify(transactionRepository).getTotalDepositsByAccountId(100L);
        verify(transactionRepository).getTotalWithdrawalsByAccountId(100L);
    }

    @Test
    void getAccountBalanceWithNullDepositsShouldHandleNullValues() {
        when(transactionRepository.getTotalDepositsByAccountId(100L)).thenReturn(null);
        when(transactionRepository.getTotalWithdrawalsByAccountId(100L)).thenReturn(500.0);

        BigDecimal result = transactionService.getAccountBalance(100L);

        assertEquals(BigDecimal.valueOf(-500.0), result);
    }

    @Test
    void createTransactionWithValidDataShouldCreateTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(testTransaction);

        assertNotNull(result);
        assertNotNull(result.getTimestamp());
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void createTransactionWithNullTimestampShouldSetCurrentTimestamp() {
        testTransaction.setTimestamp(null);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(testTransaction);

        assertNotNull(result.getTimestamp());
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void createTransactionWithInvalidAmountShouldThrowException() {
        testTransaction.setAmount(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(testTransaction));
        assertEquals("Transaction amount must be positive", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransactionWithNegativeAmountShouldThrowException() {
        testTransaction.setAmount(BigDecimal.valueOf(-100));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(testTransaction));
        assertEquals("Transaction amount must be positive", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransactionFromPaymentWithCreditPaymentShouldCreateDepositTransaction() {
        testPayment.setIsCredit(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.createTransactionFromPayment(testPayment);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransactionFromPaymentWithDebitPaymentShouldCreateWithdrawalTransaction() {
        testPayment.setIsCredit(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.createTransactionFromPayment(testPayment);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransactionStatusShouldUpdateStatus() {
        TransactionStatus newStatus = TransactionStatus.CANCELLED;
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.updateTransactionStatus(1L, newStatus);

        assertEquals(newStatus, result.getStatus());
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void isSuspiciousActivityWithHighTransactionCountShouldReturnTrue() {
        when(transactionRepository.countByCardIdAndTimestampAfter(anyLong(), any(LocalDateTime.class)))
                .thenReturn(15);

        boolean result = transactionService.isSuspiciousActivity(200L);

        assertTrue(result);
        verify(transactionRepository).countByCardIdAndTimestampAfter(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void isSuspiciousActivityWithLowTransactionCountShouldReturnFalse() {
        when(transactionRepository.countByCardIdAndTimestampAfter(anyLong(), any(LocalDateTime.class)))
                .thenReturn(5);

        boolean result = transactionService.isSuspiciousActivity(200L);

        assertFalse(result);
        verify(transactionRepository).countByCardIdAndTimestampAfter(anyLong(), any(LocalDateTime.class));
    }
}