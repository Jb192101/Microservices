package service;

import entity.PaymentRegistry;
import entity.PaymentStatus;
import entity.ProductRegistry;
import kafka.dto.PaymentScheduleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Для расчёта графика платежей

@Slf4j
@Service
public class PaymentScheduleService {
    @Autowired
    private final PaymentRegistryService paymentRegistryService;
    @Autowired
    private final ProductRegistryService productRegistryService;
    @Autowired
    private final AnnuityPaymentCalculator annuityPaymentCalculator;

    public PaymentScheduleService(PaymentRegistryService paymentRegistryService,
                                  ProductRegistryService productRegistryService,
                                  AnnuityPaymentCalculator annuityPaymentCalculator) {
        this.paymentRegistryService = paymentRegistryService;
        this.productRegistryService = productRegistryService;
        this.annuityPaymentCalculator = annuityPaymentCalculator;
    }

    @Transactional
    public List<PaymentRegistry> createAndSavePaymentSchedule(ProductRegistry product,
                                                              BigDecimal loanAmount,
                                                              BigDecimal annualRate,
                                                              Integer months) {
        try {
            log.info("Создание графика платежей для продукта {}: сумма={}, ставка={}%, срок={} мес.",
                    product.getId(), loanAmount, annualRate, months);

            List<PaymentScheduleDTO> schedule = annuityPaymentCalculator.generatePaymentSchedule(
                    product.getId(),
                    loanAmount,
                    annualRate,
                    months,
                    product.getOpenDate()
            );

            log.info("Сгенерировано {} платежей для продукта {}", schedule.size(), product.getId());

            return schedule.stream()
                    .map(paymentSchedule -> savePaymentRecord(paymentSchedule, product))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Ошибка при создании графика платежей для продукта {}: {}",
                    product.getId(), e.getMessage());
            throw new RuntimeException("Не удалось создать график платежей", e);
        }
    }

    @Transactional
    public PaymentRegistry savePaymentRecord(PaymentScheduleDTO paymentSchedule, ProductRegistry product) {
        try {
            PaymentRegistry payment = convertToPaymentRegistry(paymentSchedule, product);

            validatePaymentSchedule(paymentSchedule);

            PaymentRegistry savedPayment = paymentRegistryService.createPayment(payment);

            log.debug("Сохранен платеж #{} для продукта {}: сумма={}, дата={}",
                    paymentSchedule.getPaymentNumber(), product.getId(),
                    paymentSchedule.getTotalAmount(), paymentSchedule.getPaymentDate());

            return savedPayment;

        } catch (Exception e) {
            log.error("Ошибка при сохранении платежа #{} для продукта {}: {}",
                    paymentSchedule.getPaymentNumber(), product.getId(), e.getMessage());
            throw new RuntimeException("Не удалось сохранить платеж в графике", e);
        }
    }

    private PaymentRegistry convertToPaymentRegistry(PaymentScheduleDTO schedule, ProductRegistry product) {
        PaymentRegistry payment = new PaymentRegistry();

        payment.setProductRegistry(product);
        payment.setPaymentDate(schedule.getPaymentDate());
        payment.setAmount(schedule.getTotalAmount());
        payment.setInterestRateAmount(schedule.getInterestAmount());
        payment.setDebtAmount(schedule.getPrincipalAmount());
        payment.setExpired(false);
        payment.setPaymentExpirationDate(schedule.getPaymentDate().plusDays(7)); // +7 дней на оплату
        payment.setPaymentStatus(PaymentStatus.PENDING);

        return payment;
    }

    private void validatePaymentSchedule(PaymentScheduleDTO schedule) {
        if (schedule.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма платежа должна быть положительной");
        }

        if (schedule.getPaymentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата платежа не может быть в прошлом");
        }

        if (!schedule.getTotalAmount().equals(
                schedule.getInterestAmount().add(schedule.getPrincipalAmount()))) {
            throw new IllegalArgumentException("Общая сумма должна равняться сумме процентов и основного долга");
        }
    }

    public List<PaymentScheduleDTO> getPaymentScheduleByClientId(Long id) {
        List<PaymentRegistry> payments = paymentRegistryService.getPaymentsByClientId(id);

        return payments.stream()
                .map(this::convertToPaymentScheduleDTO)
                .sorted((p1, p2) -> p1.getPaymentNumber().compareTo(p2.getPaymentNumber()))
                .collect(Collectors.toList());
    }

    public List<PaymentScheduleDTO> getPaymentScheduleByProduct(Long productRegistryId) {
        List<PaymentRegistry> payments = paymentRegistryService.getPaymentsByProductRegistryId(productRegistryId);

        return payments.stream()
                .map(this::convertToPaymentScheduleDTO)
                .sorted((p1, p2) -> p1.getPaymentNumber().compareTo(p2.getPaymentNumber()))
                .collect(Collectors.toList());
    }

    private PaymentScheduleDTO convertToPaymentScheduleDTO(PaymentRegistry payment) {
        Integer paymentNumber = determinePaymentNumber(payment);

        return new PaymentScheduleDTO(
                payment.getProductRegistry().getId(),
                paymentNumber,
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getInterestRateAmount(),
                payment.getDebtAmount(),
                calculateRemainingBalance(payment) // Нужно будет реализовать этот метод
        );
    }

    private Integer determinePaymentNumber(PaymentRegistry payment) {
        List<PaymentRegistry> allPayments = paymentRegistryService
                .getPaymentsByProductRegistryId(payment.getProductRegistry().getId());

        return allPayments.indexOf(payment) + 1;
    }

    private BigDecimal calculateRemainingBalance(PaymentRegistry currentPayment) {
        try {
            ProductRegistry product = currentPayment.getProductRegistry();

            List<PaymentRegistry> futurePayments = paymentRegistryService
                    .getPaymentsByProductRegistryId(product.getId()).stream()
                    .filter(p -> p.getPaymentDate().isAfter(currentPayment.getPaymentDate()))
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                    .collect(Collectors.toList());

            BigDecimal futurePrincipal = futurePayments.stream()
                    .map(PaymentRegistry::getDebtAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (futurePayments.isEmpty()) {
                return BigDecimal.ZERO;
            }

            return futurePrincipal;

        } catch (Exception e) {
            log.warn("Ошибка при расчете остатка через будущие платежи: {}", e.getMessage());

            return calculateRemainingBalanceAlternative(currentPayment);
        }
    }

    private BigDecimal calculateRemainingBalanceAlternative(PaymentRegistry currentPayment) {
        try {
            ProductRegistry product = currentPayment.getProductRegistry();

            List<PaymentRegistry> allPayments = paymentRegistryService
                    .getPaymentsByProductRegistryId(product.getId());

            BigDecimal totalPrincipal = allPayments.stream()
                    .map(PaymentRegistry::getDebtAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal paidPrincipal = allPayments.stream()
                    .filter(p -> p.getPaymentDate().isBefore(currentPayment.getPaymentDate()))
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                    .map(PaymentRegistry::getDebtAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remaining = totalPrincipal.subtract(paidPrincipal);

            return remaining.compareTo(BigDecimal.ZERO) >= 0 ? remaining : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Ошибка в резервном методе расчета остатка: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Transactional
    public List<PaymentRegistry> recalculateScheduleForEarlyRepayment(Long productRegistryId,
                                                                      BigDecimal earlyRepaymentAmount) {
        ProductRegistry product = productRegistryService.getProductById(productRegistryId);

        // Получаем текущие будущие платежи
        List<PaymentRegistry> futurePayments = paymentRegistryService
                .getPaymentsByProductRegistryId(productRegistryId).stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .filter(p -> p.getPaymentDate().isAfter(LocalDate.now()))
                .sorted((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()))
                .collect(Collectors.toList());

        if (futurePayments.isEmpty()) {
            throw new IllegalStateException("Нет будущих платежей для пересчета");
        }

        if (earlyRepaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма досрочного погашения должна быть положительной");
        }

        log.info("Пересчет графика платежей для продукта {} при досрочном погашении {}",
                productRegistryId, earlyRepaymentAmount);

        BigDecimal totalRemainingPrincipal = futurePayments.stream()
                .map(PaymentRegistry::getDebtAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Общий оставшийся основной долг: {}", totalRemainingPrincipal);

        if (earlyRepaymentAmount.compareTo(totalRemainingPrincipal) > 0) {
            earlyRepaymentAmount = totalRemainingPrincipal;
            log.info("Сумма досрочного погашения скорректирована до: {}", earlyRepaymentAmount);
        }

        boolean isFullRepayment = earlyRepaymentAmount.compareTo(totalRemainingPrincipal) == 0;

        if (isFullRepayment) {
            return handleFullEarlyRepayment(futurePayments, product);
        } else {
            return handlePartialEarlyRepayment(futurePayments, product, earlyRepaymentAmount, totalRemainingPrincipal);
        }
    }

    private List<PaymentRegistry> handleFullEarlyRepayment(List<PaymentRegistry> futurePayments, ProductRegistry product) {
        log.info("Полное досрочное погашение продукта {}", product.getId());

        // Помечаем все будущие платежи как оплаченные и закрываем продукт
        futurePayments.forEach(payment -> {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaymentDate(LocalDate.now()); // Дата фактического погашения
            payment.setExpired(false);
            paymentRegistryService.createPayment(payment); // Сохраняем изменения
        });

        // Закрываем продукт
        productRegistryService.closeProduct(product.getId());

        log.info("Продукт {} полностью погашен досрочно", product.getId());
        return futurePayments;
    }

    private List<PaymentRegistry> handlePartialEarlyRepayment(List<PaymentRegistry> futurePayments,
                                                              ProductRegistry product,
                                                              BigDecimal earlyRepaymentAmount,
                                                              BigDecimal totalRemainingPrincipal) {
        log.info("Частичное досрочное погашение: {} из {}", earlyRepaymentAmount, totalRemainingPrincipal);

        BigDecimal remainingEarlyAmount = earlyRepaymentAmount;
        List<PaymentRegistry> updatedPayments = new ArrayList<>();

        // 1. Проходим по платежам в порядке дат и уменьшаем основной долг
        for (PaymentRegistry payment : futurePayments) {
            if (remainingEarlyAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal currentPrincipal = payment.getDebtAmount();

            if (remainingEarlyAmount.compareTo(currentPrincipal) >= 0) {
                // Полное погашение этого платежа
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setPaymentDate(LocalDate.now());
                payment.setExpired(false);
                remainingEarlyAmount = remainingEarlyAmount.subtract(currentPrincipal);
            } else {
                // Частичное погашение - уменьшаем основной долг
                BigDecimal newPrincipal = currentPrincipal.subtract(remainingEarlyAmount);
                payment.setDebtAmount(newPrincipal);

                // Пересчитываем общую сумму платежа (основной долг + проценты)
                BigDecimal newTotalAmount = newPrincipal.add(payment.getInterestRateAmount());
                payment.setAmount(newTotalAmount);

                remainingEarlyAmount = BigDecimal.ZERO;
            }

            updatedPayments.add(paymentRegistryService.createPayment(payment));
        }

        // 2. Пересчитываем оставшиеся платежи с новыми параметрами
        if (remainingEarlyAmount.compareTo(BigDecimal.ZERO) < 0) {
            recalculateRemainingPayments(futurePayments, product, earlyRepaymentAmount);
        }

        log.info("Частичное досрочное погашение завершено. Обработано платежей: {}", updatedPayments.size());
        return updatedPayments;
    }

    private void recalculateRemainingPayments(List<PaymentRegistry> futurePayments,
                                              ProductRegistry product,
                                              BigDecimal earlyRepaymentAmount) {
        List<PaymentRegistry> remainingPayments = futurePayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .filter(p -> p.getPaymentDate().isAfter(LocalDate.now()))
                .sorted((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()))
                .collect(Collectors.toList());

        if (remainingPayments.isEmpty()) {
            return;
        }

        BigDecimal newTotalPrincipal = remainingPayments.stream()
                .map(PaymentRegistry::getDebtAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal reductionFactor = BigDecimal.ONE.subtract(
                earlyRepaymentAmount.divide(newTotalPrincipal.add(earlyRepaymentAmount), 10, BigDecimal.ROUND_HALF_UP)
        );

        for (PaymentRegistry payment : remainingPayments) {
            BigDecimal newPrincipal = payment.getDebtAmount().multiply(reductionFactor)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            payment.setDebtAmount(newPrincipal);

            BigDecimal newInterest = payment.getInterestRateAmount().multiply(reductionFactor)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            payment.setInterestRateAmount(newInterest);

            payment.setAmount(newPrincipal.add(newInterest));

            paymentRegistryService.createPayment(payment);
        }

        log.debug("Пересчитано оставшихся платежей: {}", remainingPayments.size());
    }
}