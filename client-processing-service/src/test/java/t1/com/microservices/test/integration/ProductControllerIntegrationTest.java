package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.ProductController;
import entity.Product;
import entity.ProductKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.ProductService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Debit Card");
        product.setKey(ProductKey.DC);
        product.setCreateDate(LocalDate.now());
        return product;
    }

    @Test
    void createProductWithValidDataShouldCreateProduct() throws Exception {
        Product product = createTestProduct();
        when(productService.createProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Debit Card"));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void getProductByIdShouldReturnProduct() throws Exception {
        Product product = createTestProduct();
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Debit Card"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getAllProductsShouldReturnProductsList() throws Exception {
        List<Product> products = Arrays.asList(createTestProduct());
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Debit Card"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void updateProductWithValidRoleShouldUpdateProduct() throws Exception {
        Product product = createTestProduct();
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(product);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }
}