package t1.com.microservices.test.unit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import entity.*;
import kafka.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repository.ClientProductRepository;
import service.ClientProductService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientProductServiceTest {
    @Mock
    private ClientProductRepository clientProductRepository;

    @Mock
    private ClientProductService clientProductService;

    private ClientProduct testClientProduct;
    private Client testClient;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setClientId("CLIENT123");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setKey(ProductKey.DC);

        testClientProduct = ClientProduct.builder()
                .id(1L)
                .client(testClient)
                .product(testProduct)
                .openDate(null)
                .status(null)
                .build();
    }

    @Test
    void assignProductToClientWhenProductAlreadyAssignedShouldThrowException() {
        when(clientProductRepository.existsByClientIdAndProductId(anyLong(), anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                clientProductService.assignProductToClient(testClientProduct, 100L, "VISA", "GOLD"));
    }

    @Test
    void getClientProductByIdShouldReturnProduct() {
        when(clientProductRepository.findById(1L)).thenReturn(Optional.of(testClientProduct));

        Optional<ClientProduct> result = clientProductService.getClientProductById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getClientProductsByClientIdShouldReturnList() {
        List<ClientProduct> expectedList = Arrays.asList(testClientProduct);
        when(clientProductRepository.findByClientId(1L)).thenReturn(expectedList);

        List<ClientProduct> result = clientProductService.getClientProductsByClientId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void updateClientProductStatusWhenProductNotExistsShouldThrowException() {
        when(clientProductRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                clientProductService.updateClientProductStatus(1L, ClientProductStatus.CLOSED));
    }
}
