package service;

import entity.Payment;
import entity.Transaction;
import entity.TransactionStatus;
import entity.TransactionType;
import repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    public List<Transaction> getTransactionsByAccountIdAndDateRange(Long accountId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountIdAndDateRange(accountId, start, end);
    }

    public BigDecimal getAccountBalance(Long accountId) {
        Double deposits = transactionRepository.getTotalDepositsByAccountId(accountId);
        Double withdrawals = transactionRepository.getTotalWithdrawalsByAccountId(accountId);

        deposits = deposits != null ? deposits : 0.0;
        withdrawals = withdrawals != null ? withdrawals : 0.0;

        return BigDecimal.valueOf(deposits - withdrawals);
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        validateTransaction(transaction);

        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(LocalDateTime.now());
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void createTransactionFromPayment(Payment payment) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(payment.getAccountId());
        transaction.setAmount(payment.getAmount());
        transaction.setTimestamp(LocalDateTime.now());

        if (payment.getIsCredit()) {
            transaction.setType(TransactionType.DEPOSIT);
        } else {
            transaction.setType(TransactionType.WITHDRAWAL);
        }

        transaction.setStatus(TransactionStatus.COMPLETE);

        createTransaction(transaction);
    }

    @Transactional
    public Transaction updateTransactionStatus(Long id, TransactionStatus status) {
        Transaction transaction = getTransactionById(id);
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }
}
