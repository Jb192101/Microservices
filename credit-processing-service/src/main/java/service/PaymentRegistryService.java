package service;

import aop.annotations.LogDatasourceError;
import entity.PaymentRegistry;
import entity.PaymentStatus;
import entity.ProductRegistry;
import entity.ProductStatus;
import repository.PaymentRegistryRepository;
import repository.ProductRegistryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentRegistryService {
    @Autowired
    private PaymentRegistryRepository paymentRegistryRepository;
    @Autowired
    private ProductRegistryRepository productRegistryRepository;
    @Autowired
    private ProductRegistryService productRegistryService;

    public PaymentRegistryService(PaymentRegistryRepository paymentRegistryRepository,
                                  ProductRegistryRepository productRegistryRepository,
                                  ProductRegistryService productRegistryService) {
        this.paymentRegistryRepository = paymentRegistryRepository;
        this.productRegistryRepository = productRegistryRepository;
        this.productRegistryService = productRegistryService;
    }

    @LogDatasourceError
    public List<PaymentRegistry> getAllPayments() {
        return paymentRegistryRepository.findAll();
    }

    @LogDatasourceError
    public PaymentRegistry getPaymentById(Long id) {
        return paymentRegistryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Платёжный реестр с ID не найден: " + id));
    }

    @LogDatasourceError
    public List<PaymentRegistry> getPaymentsByProductRegistryId(Long productRegistryId) {
        return paymentRegistryRepository.findByProductRegistryId(productRegistryId);
    }

    @LogDatasourceError
    public List<PaymentRegistry> getPaymentsByClientId(Long clientId) {
        return paymentRegistryRepository.findByClientId(clientId);
    }

    @Transactional
    @LogDatasourceError
    public PaymentRegistry createPayment(PaymentRegistry paymentRegistry) {
        validatePayment(paymentRegistry);

        ProductRegistry product = paymentRegistry.getProductRegistry();
        BigDecimal totalPayment = paymentRegistry.getAmount();
        BigDecimal interestAmount = paymentRegistry.getInterestRateAmount();
        BigDecimal debtAmount = paymentRegistry.getDebtAmount();

        if (totalPayment.compareTo(interestAmount.add(debtAmount)) != 0) {
            throw new IllegalArgumentException("Общая сумма должна быть равна сумме процентов плюс сумма долга!");
        }

        paymentRegistry.setPaymentStatus(PaymentStatus.PENDING);
        paymentRegistry.setExpired(LocalDate.now().isAfter(paymentRegistry.getPaymentExpirationDate()));

        return paymentRegistryRepository.save(paymentRegistry);
    }

    @Transactional
    @LogDatasourceError
    public PaymentRegistry processPayment(Long paymentId) {
        PaymentRegistry payment = getPaymentById(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Платёж не находится в состоянии ожидания!");
        }

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setExpired(false);

        return paymentRegistryRepository.save(payment);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    @LogDatasourceError
    public void checkOverduePayments() {
        LocalDate today = LocalDate.now();
        List<PaymentRegistry> overduePayments = paymentRegistryRepository.findOverduePayments(today);

        for (PaymentRegistry payment : overduePayments) {
            payment.setExpired(true);
            payment.setPaymentStatus(PaymentStatus.OVERDUE);
            paymentRegistryRepository.save(payment);

            ProductRegistry product = payment.getProductRegistry();
            product.setStatus(ProductStatus.OVERDUE);
            productRegistryRepository.save(product);
        }
    }

    private void validatePayment(PaymentRegistry paymentRegistry) {
        if (paymentRegistry.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной!");
        }

        if (paymentRegistry.getPaymentExpirationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата платежа не может быть в прошлом!");
        }

        ProductRegistry product = paymentRegistry.getProductRegistry();
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Продукт должен быть активным для создания платежа!");
        }
    }
}