package t1.com.microservices.test.unit;

import entity.Account;
import entity.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AccountRepository;
import service.AccountService;
import service.TransactionService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setClientId(100L);
        testAccount.setProductId(200L);
        testAccount.setBalance(BigDecimal.valueOf(1000));
        testAccount.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void getAllAccountsShouldReturnAllAccounts() {
        List<Account> expectedAccounts = Arrays.asList(testAccount);
        when(accountRepository.findAll()).thenReturn(expectedAccounts);

        List<Account> result = accountService.getAllAccounts();

        assertEquals(expectedAccounts, result);
        verify(accountRepository).findAll();
    }

    @Test
    void getAccountByIdWhenAccountExistsShouldReturnAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        Account result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountByIdWhenAccountNotExistsShouldThrowException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountService.getAccountById(1L));
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountsByClientIdShouldReturnClientAccounts() {
        List<Account> expectedAccounts = Arrays.asList(testAccount);
        when(accountRepository.findByClientId(100L)).thenReturn(expectedAccounts);

        List<Account> result = accountService.getAccountsByClientId(100L);

        assertEquals(expectedAccounts, result);
        verify(accountRepository).findByClientId(100L);
    }

    @Test
    void updateAccountBalanceWhenCreditShouldIncreaseBalance() {
        BigDecimal amount = BigDecimal.valueOf(500);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccountBalance(1L, amount, true);

        assertEquals(BigDecimal.valueOf(1500), result.getBalance());
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccountBalanceWhenDebitWithSufficientBalanceShouldDecreaseBalance() {
        BigDecimal amount = BigDecimal.valueOf(500);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccountBalance(1L, amount, false);

        assertEquals(BigDecimal.valueOf(500), result.getBalance());
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccountBalanceWhenDebitWithInsufficientBalanceShouldThrowException() {
        BigDecimal amount = BigDecimal.valueOf(1500);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateAccountBalance(1L, amount, false));
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void closeAccountWithZeroBalanceShouldCloseAccount() {
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.closeAccount(1L);

        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void closeAccountWithNonZeroBalanceShouldThrowException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalStateException.class, () -> accountService.closeAccount(1L));
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }
}
