package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.PaymentRegistryController;
import entity.PaymentRegistry;
import entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.PaymentRegistryService;

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
class PaymentRegistryControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private PaymentRegistryService paymentRegistryService;

    @InjectMocks
    private PaymentRegistryController paymentRegistryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentRegistryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private PaymentRegistry createTestPayment() {
        PaymentRegistry payment = new PaymentRegistry();
        payment.setId(1L);
        payment.setAmount(BigDecimal.valueOf(1500.50));
        payment.setPaymentDate(LocalDate.now().plusDays(10));
        payment.setPaymentExpirationDate(LocalDate.now().plusDays(17));
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setExpired(false);
        payment.setInterestRateAmount(BigDecimal.valueOf(200.50));
        payment.setDebtAmount(BigDecimal.valueOf(1300.00));
        return payment;
    }

    @Test
    void getAllPaymentsShouldReturnPayments() throws Exception {
        List<PaymentRegistry> payments = Arrays.asList(createTestPayment());
        when(paymentRegistryService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(1500.50))
                .andExpect(jsonPath("$[0].paymentStatus").value("PENDING"));

        verify(paymentRegistryService, times(1)).getAllPayments();
    }

    @Test
    void getPaymentByIdShouldReturnPayment() throws Exception {
        PaymentRegistry payment = createTestPayment();
        when(paymentRegistryService.getPaymentById(1L)).thenReturn(payment);

        mockMvc.perform(get("/api/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(1500.50));

        verify(paymentRegistryService, times(1)).getPaymentById(1L);
    }

    @Test
    void getPaymentsByProductRegistryIdShouldReturnPayments() throws Exception {
        List<PaymentRegistry> payments = Arrays.asList(createTestPayment());
        when(paymentRegistryService.getPaymentsByProductRegistryId(100L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/product/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].debtAmount").value(1300.00));

        verify(paymentRegistryService, times(1)).getPaymentsByProductRegistryId(100L);
    }

    @Test
    void getPaymentsByClientIdShouldReturnPayments() throws Exception {
        List<PaymentRegistry> payments = Arrays.asList(createTestPayment());
        when(paymentRegistryService.getPaymentsByClientId(200L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/client/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].interestRateAmount").value(200.50));

        verify(paymentRegistryService, times(1)).getPaymentsByClientId(200L);
    }

    @Test
    void processPaymentShouldProcessPayment() throws Exception {
        PaymentRegistry processedPayment = createTestPayment();
        processedPayment.setPaymentStatus(PaymentStatus.PAID);
        when(paymentRegistryService.processPayment(1L)).thenReturn(processedPayment);

        mockMvc.perform(post("/api/payments/1/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        verify(paymentRegistryService, times(1)).processPayment(1L);
    }
}