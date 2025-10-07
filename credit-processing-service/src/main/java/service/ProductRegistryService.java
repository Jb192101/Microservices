package service;

import aop.annotations.LogDatasourceError;
import entity.ProductRegistry;
import entity.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.ProductRegistryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRegistryService {
    @Autowired
    private ProductRegistryRepository productRegistryRepository;

    @LogDatasourceError
    public List<ProductRegistry> getAllProducts() {
        return productRegistryRepository.findAll();
    }

    @LogDatasourceError
    public ProductRegistry getProductById(Long id) {
        return productRegistryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product registry not found with id: " + id));
    }

    @LogDatasourceError
    public List<ProductRegistry> getProductsByClientId(Long clientId) {
        return productRegistryRepository.findByClientId(clientId);
    }

    @LogDatasourceError
    public List<ProductRegistry> getActiveProductsByClientId(Long clientId) {
        return productRegistryRepository.findActiveProductsByClientId(clientId);
    }

    @Transactional
    @LogDatasourceError
    public ProductRegistry createProduct(ProductRegistry productRegistry) {
        validateProductRegistry(productRegistry);
        productRegistry.setOpenDate(LocalDate.now());
        productRegistry.setStatus(ProductStatus.ACTIVE);
        return productRegistryRepository.save(productRegistry);
    }

    @Transactional
    @LogDatasourceError
    public ProductRegistry updateProductStatus(Long id, ProductStatus status) {
        ProductRegistry product = getProductById(id);
        product.setStatus(status);
        if (status == ProductStatus.CLOSED) {
            product.setCloseDate(LocalDate.now());
        }
        return productRegistryRepository.save(product);
    }

    @Transactional
    public void closeProduct(Long id) {
        updateProductStatus(id, ProductStatus.CLOSED);
    }

    @LogDatasourceError
    private void validateProductRegistry(ProductRegistry productRegistry) {
        if (productRegistry.getInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interest rate must be positive");
        }

        if (productRegistryRepository.existsByAccountIdAndStatus(
                productRegistry.getAccountId(), ProductStatus.ACTIVE)) {
            throw new IllegalArgumentException("Account already has an active product");
        }
    }
}
