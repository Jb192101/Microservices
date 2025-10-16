package t1.com.microservices.test.unit;

import entity.PaymentRegistry;
import entity.PaymentStatus;
import entity.ProductRegistry;
import entity.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.test.context.support.WithMockUser;
import repository.PaymentRegistryRepository;
import repository.ProductRegistryRepository;
import service.PaymentRegistryService;
import service.ProductRegistryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRegistryServiceTest {
    @Mock
    private PaymentRegistryRepository paymentRegistryRepository;

    @InjectMocks
    private PaymentRegistryService paymentRegistryService;

    private PaymentRegistry payment;
    private ProductRegistry product;

    @BeforeEach
    void setUp() {
        product = new ProductRegistry();
        product.setId(1L);
        product.setClientId(100L);
        product.setStatus(ProductStatus.ACTIVE);

        payment = new PaymentRegistry();
        payment.setId(1L);
        payment.setProductRegistry(product);
        payment.setAmount(BigDecimal.valueOf(1500.50));
        payment.setInterestRateAmount(BigDecimal.valueOf(200.50));
        payment.setDebtAmount(BigDecimal.valueOf(1300.00));
        payment.setPaymentDate(LocalDate.now().plusDays(10));
        payment.setPaymentExpirationDate(LocalDate.now().plusDays(17));
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setExpired(false);
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void getAllPaymentsShouldReturnPayments() {
        List<PaymentRegistry> payments = Arrays.asList(payment);
        when(paymentRegistryRepository.findAll()).thenReturn(payments);

        List<PaymentRegistry> result = paymentRegistryService.getAllPayments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(paymentRegistryRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void getPaymentByIdWithExistingIdShouldReturnPayment() {
        when(paymentRegistryRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentRegistry result = paymentRegistryService.getPaymentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(1500.50), result.getAmount());
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void getPaymentByIdWithNonExistingIdShouldThrowException() {
        when(paymentRegistryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentRegistryService.getPaymentById(999L));
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void getPaymentsByProductRegistryIdShouldReturnPayments() {
        List<PaymentRegistry> payments = Arrays.asList(payment);
        when(paymentRegistryRepository.findByProductRegistryId(1L)).thenReturn(payments);

        List<PaymentRegistry> result = paymentRegistryService.getPaymentsByProductRegistryId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProductRegistry().getId());
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void getPaymentsByClientIdShouldReturnPayments() {
        List<PaymentRegistry> payments = Arrays.asList(payment);
        when(paymentRegistryRepository.findByClientId(100L)).thenReturn(payments);

        List<PaymentRegistry> result = paymentRegistryService.getPaymentsByClientId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getProductRegistry().getClientId());
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE"})
    void createPaymentWithValidDataShouldSavePayment() {
        when(paymentRegistryRepository.save(any(PaymentRegistry.class))).thenReturn(payment);

        PaymentRegistry result = paymentRegistryService.createPayment(payment);

        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        verify(paymentRegistryRepository, times(1)).save(payment);
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE"})
    void createPaymentWithInactiveProductShouldThrowException() {
        product.setStatus(ProductStatus.CLOSED);
        payment.setProductRegistry(product);

        assertThrows(IllegalArgumentException.class, () -> paymentRegistryService.createPayment(payment));
    }
}
