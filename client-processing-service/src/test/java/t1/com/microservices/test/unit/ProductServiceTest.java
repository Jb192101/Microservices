package t1.com.microservices.test.unit;

import entity.Product;
import entity.ProductKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductRepository;
import service.ProductService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Debit Card");
        testProduct.setKey(ProductKey.DC);
        testProduct.setCreateDate(LocalDate.now());
    }

    @Test
    void createProductWithGeneratedDateShouldSaveProduct() {
        testProduct.setCreateDate(null);
        when(productRepository.existsByKey(ProductKey.DC)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertNotNull(result);
        assertNotNull(result.getCreateDate());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProductWithExistingKeyShouldThrowException() {
        when(productRepository.existsByKey(ProductKey.DC)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(testProduct));

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductByIdShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Debit Card", result.get().getName());
    }

    @Test
    void getProductByKeyShouldReturnProduct() {
        when(productRepository.findByKey(ProductKey.DC)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductByKey(ProductKey.DC);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getAllProductsShouldReturnProductsList() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(ProductKey.DC, result.get(0).getKey());
    }

    @Test
    void updateProductWithValidDataShouldUpdateProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Credit Card");
        updatedProduct.setKey(ProductKey.CC);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByKey(ProductKey.CC)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertEquals("Credit Card", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProductWithExistingIdShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }
}