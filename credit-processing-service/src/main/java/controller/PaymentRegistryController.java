package controller;

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
    public ResponseEntity<List<PaymentRegistry>> getAllPayments() {
        return ResponseEntity.ok(paymentRegistryService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentRegistry> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentRegistryService.getPaymentById(id));
    }

    @GetMapping("/product/{productRegistryId}")
    public ResponseEntity<List<PaymentRegistry>> getPaymentsByProductRegistryId(
            @PathVariable Long productRegistryId) {
        return ResponseEntity.ok(paymentRegistryService.getPaymentsByProductRegistryId(productRegistryId));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<PaymentRegistry>> getPaymentsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(paymentRegistryService.getPaymentsByClientId(clientId));
    }

    @PostMapping
    public ResponseEntity<PaymentRegistry> createPayment(@Valid @RequestBody PaymentRegistry paymentRegistry) {
        return ResponseEntity.ok(paymentRegistryService.createPayment(paymentRegistry));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentRegistry> processPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentRegistryService.processPayment(id));
    }
}