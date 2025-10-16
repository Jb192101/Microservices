package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.ClientProductController;
import entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.ClientProductService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientProductControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private ClientProductService clientProductService;

    @InjectMocks
    private ClientProductController clientProductController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientProductController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private ClientProduct createTestClientProduct() {
        Client client = new Client();
        client.setId(1L);
        client.setClientId("CLIENT123");

        Product product = new Product();
        product.setId(1L);
        product.setKey(ProductKey.DC);

        ClientProduct clientProduct = new ClientProduct();
        clientProduct.setId(1L);
        clientProduct.setClient(client);
        clientProduct.setProduct(product);
        clientProduct.setOpenDate(LocalDate.now());
        clientProduct.setStatus(ClientProductStatus.ACTIVE);
        return clientProduct;
    }

    @Test
    void assignProductToClientWithValidDataShouldAssignProduct() throws Exception {
        ClientProduct clientProduct = createTestClientProduct();
        when(clientProductService.assignProductToClient(any(ClientProduct.class), anyLong(), anyString(), anyString()))
                .thenReturn(clientProduct);

        mockMvc.perform(post("/client-products")
                        .param("accountId", "100")
                        .param("paymentSystem", "VISA")
                        .param("cardType", "GOLD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(clientProductService, times(1)).assignProductToClient(any(ClientProduct.class), eq(100L), eq("VISA"), eq("GOLD"));
    }

    @Test
    void getClientProductByIdShouldReturnClientProduct() throws Exception {
        ClientProduct clientProduct = createTestClientProduct();
        when(clientProductService.getClientProductById(1L)).thenReturn(Optional.of(clientProduct));

        mockMvc.perform(get("/client-products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(clientProductService, times(1)).getClientProductById(1L);
    }

    @Test
    void getClientProductsByClientIdShouldReturnList() throws Exception {
        List<ClientProduct> clientProducts = Arrays.asList(createTestClientProduct());
        when(clientProductService.getClientProductsByClientId(1L)).thenReturn(clientProducts);

        mockMvc.perform(get("/client-products/client/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(clientProductService, times(1)).getClientProductsByClientId(1L);
    }

    @Test
    void getActiveClientProductsShouldReturnActiveProducts() throws Exception {
        List<ClientProduct> clientProducts = Arrays.asList(createTestClientProduct());
        when(clientProductService.getActiveClientProducts(1L)).thenReturn(clientProducts);

        mockMvc.perform(get("/client-products/client/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(clientProductService, times(1)).getActiveClientProducts(1L);
    }

    @Test
    void updateClientProductStatusShouldUpdateStatus() throws Exception {
        ClientProduct clientProduct = createTestClientProduct();
        when(clientProductService.updateClientProductStatus(eq(1L), any(ClientProductStatus.class)))
                .thenReturn(clientProduct);

        mockMvc.perform(patch("/client-products/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(clientProductService, times(1)).updateClientProductStatus(eq(1L), any(ClientProductStatus.class));
    }
}