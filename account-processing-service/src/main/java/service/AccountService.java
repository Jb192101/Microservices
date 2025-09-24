package service;

import entity.Account;
import entity.AccountStatus;
import repository.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionService transactionService;

    public AccountService(AccountRepository accountRepository, TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аккаунта с ID не существует: " + id));
    }

    public List<Account> getAccountsByClientId(Long clientId) {
        return accountRepository.findByClientId(clientId);
    }

    public List<Account> getActiveAccountsByClientId(Long clientId) {
        return accountRepository.findActiveAccountsByClientId(clientId);
    }

    @Transactional
    public Account createAccount(Account account) {
        validateAccount(account);
        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccountBalance(Long accountId, BigDecimal amount, boolean isCredit) {
        Account account = getAccountById(accountId);

        BigDecimal newBalance;
        if (isCredit) {
            newBalance = account.getBalance().add(amount);
        } else {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Недостаточно средств!");
            }
            newBalance = account.getBalance().subtract(amount);
        }

        account.setBalance(newBalance);
        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccountStatus(Long id, AccountStatus status) {
        Account account = getAccountById(id);
        account.setStatus(status);
        return accountRepository.save(account);
    }

    @Transactional
    public void closeAccount(Long id) {
        Account account = getAccountById(id);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Невозможно закрыть счет с ненулевым балансом!");
        }
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    private void validateAccount(Account account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным!");
        }

        if (accountRepository.existsByClientIdAndProductIdAndStatus(
                account.getClientId(), account.getProductId(), AccountStatus.ACTIVE)) {
            throw new IllegalArgumentException("У клиента уже есть активная учетная запись для этого продукта!");
        }
    }

    public boolean isAccountActive(Long accountId) {
        Account account = getAccountById(accountId);
        return account.getStatus() == AccountStatus.ACTIVE;
    }
}
