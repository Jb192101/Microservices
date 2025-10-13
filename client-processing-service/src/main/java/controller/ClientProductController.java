package controller;

import com.t1.starter.pack.aop.annotations.HttpIncomeRequestLog;
import com.t1.starter.pack.aop.annotations.HttpOutcomeRequestLog;
import entity.ClientProduct;
import entity.ClientProductStatus;
import service.ClientProductService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client-products")
@RequiredArgsConstructor
public class ClientProductController {
    @Autowired
    private ClientProductService clientProductService;

    @PostMapping
    @HttpIncomeRequestLog
    public ResponseEntity<ClientProduct> assignProductToClient(@Valid @RequestBody ClientProduct clientProduct, Long accountId,
                                                               String paymentSystem, String cardType) {
        ClientProduct assignedProduct = clientProductService.assignProductToClient(
                clientProduct,
                accountId,
                paymentSystem,
                cardType
                );
        return ResponseEntity.ok(assignedProduct);
    }

    @GetMapping("/{id}")
    @HttpOutcomeRequestLog
    public ResponseEntity<ClientProduct> getClientProductById(@PathVariable Long id) {
        return clientProductService.getClientProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<ClientProduct>> getClientProductsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientProductService.getClientProductsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/active")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<ClientProduct>> getActiveClientProducts(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientProductService.getActiveClientProducts(clientId));
    }

    @PatchMapping("/{id}/status")
    @HttpIncomeRequestLog
    public ResponseEntity<ClientProduct> updateClientProductStatus(
            @PathVariable Long id,
            @RequestParam ClientProductStatus status) {
        try {
            ClientProduct updatedProduct = clientProductService.updateClientProductStatus(id, status);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @HttpIncomeRequestLog
    public ResponseEntity<Void> removeProductFromClient(@PathVariable Long id) {
        try {
            clientProductService.removeProductFromClient(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
