package controller;

import aop.annotations.HttpIncomeRequestLog;
import aop.annotations.HttpOutcomeRequestLog;
import entity.ProductRegistry;
import entity.ProductStatus;
import service.ProductRegistryService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductRegistryController {
    @Autowired
    private ProductRegistryService productRegistryService;

    public ProductRegistryController(ProductRegistryService productRegistryService) {
        this.productRegistryService = productRegistryService;
    }

    @GetMapping
    @HttpOutcomeRequestLog
    public ResponseEntity<List<ProductRegistry>> getAllProducts() {
        return ResponseEntity.ok(productRegistryService.getAllProducts());
    }

    @GetMapping("/{id}")
    @HttpOutcomeRequestLog
    public ResponseEntity<ProductRegistry> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productRegistryService.getProductById(id));
    }

    @GetMapping("/client/{clientId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<ProductRegistry>> getProductsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(productRegistryService.getProductsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/active")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<ProductRegistry>> getActiveProductsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(productRegistryService.getActiveProductsByClientId(clientId));
    }

    @PostMapping
    @HttpIncomeRequestLog
    public ResponseEntity<ProductRegistry> createProduct(@Valid @RequestBody ProductRegistry productRegistry) {
        return ResponseEntity.ok(productRegistryService.createProduct(productRegistry));
    }

    @PatchMapping("/{id}/status")
    @HttpIncomeRequestLog
    public ResponseEntity<ProductRegistry> updateProductStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status) {
        return ResponseEntity.ok(productRegistryService.updateProductStatus(id, status));
    }

    @PostMapping("/{id}/close")
    @HttpIncomeRequestLog
    public ResponseEntity<Void> closeProduct(@PathVariable Long id) {
        productRegistryService.closeProduct(id);
        return ResponseEntity.ok().build();
    }
}