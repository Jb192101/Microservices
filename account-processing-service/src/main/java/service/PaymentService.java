package service;

import aop.annotations.LogDatasourceError;
import entity.Account;
import entity.Payment;
import entity.PaymentType;
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

    @LogDatasourceError
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @LogDatasourceError
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Платёж с ID не найден: " + id));
    }

    @LogDatasourceError
    public List<Payment> getPaymentsByAccountId(Long accountId) {
        return paymentRepository.findByAccountId(accountId);
    }

    @LogDatasourceError
    public List<Payment> getPaymentsByAccountIdAndDateRange(Long accountId, LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findByAccountIdAndDateRange(accountId, start, end);
    }

    @Transactional
    @LogDatasourceError
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

    @LogDatasourceError
    private void validatePayment(Payment payment) {
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма оплаты должна быть положительной!");
        }

        accountService.getAccountById(payment.getAccountId());
    }

    @LogDatasourceError
    public BigDecimal calculateCreditDebt(Long accountId) {
        List<Payment> unpaidPayments = paymentRepository.findUnpaidPaymentsByAccountId(accountId);
        return unpaidPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @LogDatasourceError
    public void updateExistingPayments(Long accountId) {
        List<Payment> payments = paymentRepository.findByAccountId(accountId);
        payments.forEach(payment -> payment.setPayedAt(LocalDateTime.now()));
        paymentRepository.saveAll(payments);
    }

    @LogDatasourceError
    public void createPaymentSchedule(Long accountId, BigDecimal interestRate) {
        try {
            Account account = accountService.getAccountById(accountId);
            if (account == null || !account.getRecalc()) {
                return;
            }

            BigDecimal loanAmount = account.getBalance();
            int loanTermMonths = 12;
            BigDecimal monthlyInterestRate = interestRate.divide(BigDecimal.valueOf(100), 6, BigDecimal.ROUND_HALF_UP)
                    .divide(BigDecimal.valueOf(12), 6, BigDecimal.ROUND_HALF_UP);

            // Расчет аннуитетного платежа
            BigDecimal temp = BigDecimal.ONE.add(monthlyInterestRate);
            BigDecimal powered = temp.pow(loanTermMonths);
            BigDecimal coefficient = monthlyInterestRate.multiply(powered)
                    .divide(powered.subtract(BigDecimal.ONE), 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal monthlyPayment = coefficient.multiply(loanAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

            LocalDateTime now = LocalDateTime.now();

            // Создание графика платежей на каждый месяц
            for (int i = 1; i <= loanTermMonths; i++) {
                Payment payment = new Payment();
                payment.setAccountId(accountId);
                payment.setPaymentDate(now.plusMonths(i));
                payment.setAmount(monthlyPayment);
                payment.setIsCredit(true);
                payment.setType(PaymentType.LOAN_PAYMENT);
                payment.setExpired(false);

                paymentRepository.save(payment);
            }

        } catch (Exception e) {
        }
    }

    @LogDatasourceError
    public boolean isPaymentDue(Long accountId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

            List<Payment> duePayments = paymentRepository.findByAccountIdAndPaymentDateBetween(accountId, startOfDay, endOfDay);

            if (duePayments.isEmpty()) {
                return false;
            }

            for (Payment payment : duePayments) {
                if (payment.getPayedAt() == null && !Boolean.TRUE.equals(payment.getExpired())) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }
}
