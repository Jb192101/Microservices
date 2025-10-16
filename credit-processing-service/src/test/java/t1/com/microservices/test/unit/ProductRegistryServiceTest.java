package t1.com.microservices.test.unit;

import entity.ProductRegistry;
import entity.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductRegistryRepository;
import service.ProductRegistryService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRegistryServiceTest {
    @Mock
    private ProductRegistryRepository productRegistryRepository;

    @InjectMocks
    private ProductRegistryService productRegistryService;

    private ProductRegistry product;

    @BeforeEach
    void setUp() {
        product = new ProductRegistry();
        product.setId(1L);
        product.setClientId(1L);
        product.setInterestRate(new BigDecimal("10.5"));
        product.setAccountId(1001L);
        product.setStatus(ProductStatus.ACTIVE);
    }

    @Test
    void getProductByIdExistingIdReturnsProduct() {
        when(productRegistryRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductRegistry result = productRegistryService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getClientId());
    }

    @Test
    void getProductByIdNonExistingIdThrowsException() {
        when(productRegistryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productRegistryService.getProductById(999L));
    }

    @Test
    void getProductsByClientIdValidClientIdReturnsProducts() {
        List<ProductRegistry> products = Arrays.asList(product);
        when(productRegistryRepository.findByClientId(1L)).thenReturn(products);

        List<ProductRegistry> result = productRegistryService.getProductsByClientId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getClientId());
    }

    @Test
    void createProduct_ValidProductReturnsSavedProduct() {
        when(productRegistryRepository.existsByAccountIdAndStatus(anyLong(), any(ProductStatus.class)))
                .thenReturn(false);
        when(productRegistryRepository.save(any(ProductRegistry.class))).thenReturn(product);

        ProductRegistry result = productRegistryService.createProduct(product);

        assertNotNull(result);
        assertEquals(ProductStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getOpenDate());
        verify(productRegistryRepository, times(1)).save(product);
    }

    @Test
    void updateProductStatus_ValidInputUpdatesStatus() {
        when(productRegistryRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRegistryRepository.save(any(ProductRegistry.class))).thenReturn(product);

        ProductRegistry result = productRegistryService.updateProductStatus(1L, ProductStatus.CLOSED);

        assertNotNull(result);
        assertEquals(ProductStatus.CLOSED, result.getStatus());
        assertNotNull(result.getCloseDate());
    }
}
