package controller;

import com.t1.starter.pack.aop.annotations.HttpIncomeRequestLog;
import com.t1.starter.pack.aop.annotations.HttpOutcomeRequestLog;
import entity.Product;
import service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping
    @HttpIncomeRequestLog
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok(createdProduct);
    }

    @GetMapping("/{id}")
    @HttpOutcomeRequestLog
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE', 'CURRENT_CLIENT')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @HttpOutcomeRequestLog
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE', 'CURRENT_CLIENT')")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping("/{id}")
    @HttpIncomeRequestLog
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @HttpIncomeRequestLog
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
