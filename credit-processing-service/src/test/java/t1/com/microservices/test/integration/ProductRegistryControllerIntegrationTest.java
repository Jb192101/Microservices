package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.ProductRegistryController;
import entity.ProductRegistry;
import entity.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.ProductRegistryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductRegistryControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private ProductRegistryService productRegistryService;

    @InjectMocks
    private ProductRegistryController productRegistryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productRegistryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private ProductRegistry createTestProduct() {
        ProductRegistry product = new ProductRegistry();
        product.setId(1L);
        product.setClientId(100L);
        product.setProductId(200L);
        product.setAccountId(300L);
        product.setInterestRate(BigDecimal.valueOf(15.5));
        product.setStatus(ProductStatus.ACTIVE);
        product.setOpenDate(LocalDate.now());
        product.setMonthCount(12);
        return product;
    }

    @Test
    void getAllProductsShouldReturnProducts() throws Exception {
        List<ProductRegistry> products = Arrays.asList(createTestProduct());
        when(productRegistryService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientId").value(100))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(productRegistryService, times(1)).getAllProducts();
    }

    @Test
    void getProductByIdShouldReturnProduct() throws Exception {
        ProductRegistry product = createTestProduct();
        when(productRegistryService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.interestRate").value(15.5));

        verify(productRegistryService, times(1)).getProductById(1L);
    }

    @Test
    void createProductWithValidDataShouldCreateProduct() throws Exception {
        ProductRegistry product = createTestProduct();
        when(productRegistryService.createProduct(any(ProductRegistry.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(productRegistryService, times(1)).createProduct(any(ProductRegistry.class));
    }

    @Test
    void updateProductStatusShouldUpdateStatus() throws Exception {
        ProductRegistry updatedProduct = createTestProduct();
        updatedProduct.setStatus(ProductStatus.CLOSED);
        when(productRegistryService.updateProductStatus(anyLong(), any(ProductStatus.class))).thenReturn(updatedProduct);

        mockMvc.perform(patch("/api/products/1/status")
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(productRegistryService, times(1)).updateProductStatus(1L, ProductStatus.CLOSED);
    }

    @Test
    void closeProductShouldCloseProduct() throws Exception {
        doNothing().when(productRegistryService).closeProduct(1L);

        mockMvc.perform(post("/api/products/1/close"))
                .andExpect(status().isOk());

        verify(productRegistryService, times(1)).closeProduct(1L);
    }
}