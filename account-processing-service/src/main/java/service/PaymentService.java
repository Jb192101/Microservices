package service;

import entity.Payment;
import repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TransactionService transactionService;

    public PaymentService(PaymentRepository paymentRepository, AccountService accountService,
                          TransactionService transactionService) {
        this.paymentRepository = paymentRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Платёж с ID не найден: " + id));
    }

    public List<Payment> getPaymentsByAccountId(Long accountId) {
        return paymentRepository.findByAccountId(accountId);
    }

    public List<Payment> getPaymentsByAccountIdAndDateRange(Long accountId, LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findByAccountIdAndDateRange(accountId, start, end);
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        validatePayment(payment);

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        if (payment.getIsCredit()) {
            accountService.updateAccountBalance(payment.getAccountId(), payment.getAmount(), true);
        } else {
            accountService.updateAccountBalance(payment.getAccountId(), payment.getAmount(), false);
        }

        transactionService.createTransactionFromPayment(payment);

        if (payment.getPayedAt() == null) {
            payment.setPayedAt(LocalDateTime.now());
        }

        return paymentRepository.save(payment);
    }

    private void validatePayment(Payment payment) {
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма оплаты должна быть положительной!");
        }

        accountService.getAccountById(payment.getAccountId());
    }
}
