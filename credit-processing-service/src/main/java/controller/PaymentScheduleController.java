package controller;

import aop.annotations.HttpOutcomeRequestLog;
import kafka.dto.PaymentScheduleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.PaymentScheduleService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payment-schedule")
@RequiredArgsConstructor
public class PaymentScheduleController {
    @Autowired
    private final PaymentScheduleService paymentScheduleService;

    // Получение графика платежей по ID продукта
    @GetMapping("/product/{productRegistryId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<PaymentScheduleDTO>> getPaymentScheduleByProduct(
            @PathVariable Long productRegistryId) {
        try {
            log.info("Запрос графика платежей для продукта: {}", productRegistryId);

            List<PaymentScheduleDTO> schedule = paymentScheduleService
                    .getPaymentScheduleByProduct(productRegistryId);

            log.debug("Найдено {} платежей для продукта {}", schedule.size(), productRegistryId);

            return ResponseEntity.ok(schedule);
        } catch (RuntimeException e) {
            log.error("Ошибка при получении графика платежей для продукта {}: {}",
                    productRegistryId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении графика платежей для продукта {}: {}",
                    productRegistryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Получение графика платежей по ID клиента
    @GetMapping("/client/{clientId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<PaymentScheduleDTO>> getPaymentScheduleByClient(
            @PathVariable Long clientId) {
        try {
            log.info("Запрос графиков платежей для клиента: {}", clientId);

            List<PaymentScheduleDTO> schedules = paymentScheduleService
                    .getPaymentScheduleByClientId(clientId);

            log.debug("Найдено графиков платежей для клиента {}: {}", clientId, schedules.size());

            return ResponseEntity.ok(schedules);

        } catch (RuntimeException e) {
            log.error("Ошибка при получении графиков платежей для клиента {}: {}",
                    clientId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении графиков платежей для клиента {}: {}",
                    clientId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение ближайших платежей по ID продукта
     */
    @GetMapping("/product/{productRegistryId}/upcoming")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<PaymentScheduleDTO>> getUpcomingPayments(
            @PathVariable Long productRegistryId,
            @RequestParam(defaultValue = "3") Integer count) {
        try {
            log.info("Запрос ближайших {} платежей для продукта: {}", count, productRegistryId);

            List<PaymentScheduleDTO> schedule = paymentScheduleService
                    .getPaymentScheduleByProduct(productRegistryId);

            // Фильтруем только будущие платежи и берем указанное количество
            List<PaymentScheduleDTO> upcomingPayments = schedule.stream()
                    .filter(payment -> payment.getPaymentDate().isAfter(java.time.LocalDate.now()))
                    .sorted((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()))
                    .limit(count)
                    .collect(java.util.stream.Collectors.toList());

            log.debug("Найдено ближайших платежей для продукта {}: {}", productRegistryId, upcomingPayments.size());

            return ResponseEntity.ok(upcomingPayments);

        } catch (RuntimeException e) {
            log.error("Ошибка при получении ближайших платежей для продукта {}: {}",
                    productRegistryId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении ближайших платежей для продукта {}: {}",
                    productRegistryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

     // Получение просроченных платежей по ID продукта
    @GetMapping("/product/{productRegistryId}/overdue")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<PaymentScheduleDTO>> getOverduePayments(
            @PathVariable Long productRegistryId) {
        try {
            log.info("Запрос просроченных платежей для продукта: {}", productRegistryId);

            List<PaymentScheduleDTO> schedule = paymentScheduleService
                    .getPaymentScheduleByProduct(productRegistryId);

            List<PaymentScheduleDTO> overduePayments = schedule.stream()
                    .filter(payment -> payment.getPaymentDate().isBefore(java.time.LocalDate.now()))
                    .sorted((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()))
                    .collect(java.util.stream.Collectors.toList());

            log.debug("Найдено просроченных платежей для продукта {}: {}", productRegistryId, overduePayments.size());

            return ResponseEntity.ok(overduePayments);

        } catch (RuntimeException e) {
            log.error("Ошибка при получении просроченных платежей для продукта {}: {}",
                    productRegistryId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении просроченных платежей для продукта {}: {}",
                    productRegistryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
