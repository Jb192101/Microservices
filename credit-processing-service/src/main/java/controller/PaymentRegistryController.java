package controller;

import aop.annotations.HttpIncomeRequestLog;
import aop.annotations.HttpOutcomeRequestLog;
import entity.PaymentRegistry;
import service.PaymentRegistryService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentRegistryController {
    @Autowired
    private PaymentRegistryService paymentRegistryService;

    public PaymentRegistryController(PaymentRegistryService paymentRegistryService) {
        this.paymentRegistryService = paymentRegistryService;
    }

    @GetMapping
    @HttpOutcomeRequestLog
    public ResponseEntity<List<PaymentRegistry>> getAllPayments() {
        return ResponseEntity.ok(paymentRegistryService.getAllPayments());
    }

    @GetMapping("/{id}")
    @HttpOutcomeRequestLog
    public ResponseEntity<PaymentRegistry> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentRegistryService.getPaymentById(id));
    }

    @GetMapping("/product/{productRegistryId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<PaymentRegistry>> getPaymentsByProductRegistryId(
            @PathVariable Long productRegistryId) {
        return ResponseEntity.ok(paymentRegistryService.getPaymentsByProductRegistryId(productRegistryId));
    }

    @GetMapping("/client/{clientId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<PaymentRegistry>> getPaymentsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(paymentRegistryService.getPaymentsByClientId(clientId));
    }

    @PostMapping
    @HttpIncomeRequestLog
    public ResponseEntity<PaymentRegistry> createPayment(@Valid @RequestBody PaymentRegistry paymentRegistry) {
        return ResponseEntity.ok(paymentRegistryService.createPayment(paymentRegistry));
    }

    @PostMapping("/{id}/process")
    @HttpIncomeRequestLog
    public ResponseEntity<PaymentRegistry> processPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentRegistryService.processPayment(id));
    }
}