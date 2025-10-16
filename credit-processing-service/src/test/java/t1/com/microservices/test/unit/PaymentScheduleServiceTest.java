package t1.com.microservices.test.unit;

import entity.PaymentRegistry;
import entity.PaymentStatus;
import entity.ProductRegistry;
import entity.ProductStatus;
import kafka.dto.PaymentScheduleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import service.AnnuityPaymentCalculator;
import service.PaymentRegistryService;
import service.PaymentScheduleService;
import service.ProductRegistryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentScheduleServiceTest {
    @Mock
    private PaymentRegistryService paymentRegistryService;

    @Mock
    private ProductRegistryService productRegistryService;

    @Mock
    private AnnuityPaymentCalculator annuityPaymentCalculator;

    @InjectMocks
    private PaymentScheduleService paymentScheduleService;

    private ProductRegistry product;
    private PaymentRegistry payment;
    private PaymentScheduleDTO paymentScheduleDTO;

    @BeforeEach
    void setUp() {
        product = new ProductRegistry();
        product.setId(1L);
        product.setClientId(100L);
        product.setOpenDate(LocalDate.now());
        product.setStatus(ProductStatus.ACTIVE);

        payment = new PaymentRegistry();
        payment.setId(1L);
        payment.setProductRegistry(product);
        payment.setAmount(BigDecimal.valueOf(1500.50));
        payment.setInterestRateAmount(BigDecimal.valueOf(200.50));
        payment.setDebtAmount(BigDecimal.valueOf(1300.00));
        payment.setPaymentDate(LocalDate.now().plusMonths(1));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        paymentScheduleDTO = new PaymentScheduleDTO(
                1L, 1, LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(1500.50), BigDecimal.valueOf(200.50),
                BigDecimal.valueOf(1300.00), BigDecimal.valueOf(48500.00)
        );
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE"})
    void createAndSavePaymentScheduleWithValidDataShouldCreateSchedule() {
        List<PaymentScheduleDTO> scheduleDTOs = Arrays.asList(paymentScheduleDTO);
        when(annuityPaymentCalculator.generatePaymentSchedule(
                anyLong(), any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(LocalDate.class))).thenReturn(scheduleDTOs);
        when(paymentRegistryService.createPayment(any(PaymentRegistry.class))).thenReturn(payment);

        List<PaymentRegistry> result = paymentScheduleService.createAndSavePaymentSchedule(
                product, BigDecimal.valueOf(50000), BigDecimal.valueOf(12.5), 24);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(annuityPaymentCalculator, times(1)).generatePaymentSchedule(
                anyLong(), any(BigDecimal.class), any(BigDecimal.class),
                anyInt(), any(LocalDate.class));
        verify(paymentRegistryService, times(1)).createPayment(any(PaymentRegistry.class));
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE"})
    void savePaymentRecordWithValidDataShouldSavePayment() {
        when(paymentRegistryService.createPayment(any(PaymentRegistry.class))).thenReturn(payment);

        PaymentRegistry result = paymentScheduleService.savePaymentRecord(paymentScheduleDTO, product);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(paymentRegistryService, times(1)).createPayment(any(PaymentRegistry.class));
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void getPaymentScheduleByClientIdShouldReturnSchedules() {
        List<PaymentRegistry> payments = Arrays.asList(payment);
        when(paymentRegistryService.getPaymentsByClientId(100L)).thenReturn(payments);

        List<PaymentScheduleDTO> result = paymentScheduleService.getPaymentScheduleByClientId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRegistryService, times(1)).getPaymentsByClientId(100L);
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void recalculateScheduleForEarlyRepaymentWithFullRepaymentShouldCloseProduct() {
        List<PaymentRegistry> futurePayments = Arrays.asList(payment);
        when(productRegistryService.getProductById(1L)).thenReturn(product);
        when(paymentRegistryService.getPaymentsByProductRegistryId(1L)).thenReturn(futurePayments);
        when(paymentRegistryService.createPayment(any(PaymentRegistry.class))).thenReturn(payment);
        doNothing().when(productRegistryService).closeProduct(1L);

        BigDecimal totalPrincipal = futurePayments.stream()
                .map(PaymentRegistry::getDebtAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PaymentRegistry> result = paymentScheduleService.recalculateScheduleForEarlyRepayment(
                1L, totalPrincipal);

        assertNotNull(result);
        verify(productRegistryService, times(1)).closeProduct(1L);
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void recalculateScheduleForEarlyRepaymentWithPartialRepaymentShouldUpdatePayments() {
        List<PaymentRegistry> futurePayments = Arrays.asList(payment);
        when(productRegistryService.getProductById(1L)).thenReturn(product);
        when(paymentRegistryService.getPaymentsByProductRegistryId(1L)).thenReturn(futurePayments);
        when(paymentRegistryService.createPayment(any(PaymentRegistry.class))).thenReturn(payment);

        List<PaymentRegistry> result = paymentScheduleService.recalculateScheduleForEarlyRepayment(
                1L, BigDecimal.valueOf(500));

        assertNotNull(result);
        verify(paymentRegistryService, atLeastOnce()).createPayment(any(PaymentRegistry.class));
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void recalculateScheduleForEarlyRepaymentWithNoFuturePaymentsShouldThrowException() {
        when(productRegistryService.getProductById(1L)).thenReturn(product);
        when(paymentRegistryService.getPaymentsByProductRegistryId(1L)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () ->
                paymentScheduleService.recalculateScheduleForEarlyRepayment(1L, BigDecimal.valueOf(500)));
    }

    @Test
    @WithMockUser(roles = {"MASTER", "GRAND_EMPLOYEE", "CURRENT_CLIENT"})
    void recalculateScheduleForEarlyRepaymentWithInvalidAmountShouldThrowException() {
        when(productRegistryService.getProductById(1L)).thenReturn(product);
        List<PaymentRegistry> futurePayments = Arrays.asList(payment);
        when(paymentRegistryService.getPaymentsByProductRegistryId(1L)).thenReturn(futurePayments);

        assertThrows(IllegalArgumentException.class, () ->
                paymentScheduleService.recalculateScheduleForEarlyRepayment(1L, BigDecimal.ZERO));
    }

    @Test
    void calculateRemainingBalanceWithFuturePaymentsShouldReturnBalance() {
        List<PaymentRegistry> allPayments = Arrays.asList(payment);
        when(paymentRegistryService.getPaymentsByProductRegistryId(1L)).thenReturn(allPayments);

        BigDecimal result = paymentScheduleService.calculateRemainingBalance(payment);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
    }
}
