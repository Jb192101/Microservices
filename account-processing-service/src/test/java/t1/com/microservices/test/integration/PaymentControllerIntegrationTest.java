package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.PaymentController;
import entity.Payment;
import entity.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Payment createTestPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAccountId(100L);
        payment.setAmount(BigDecimal.valueOf(250.75));
        payment.setIsCredit(true);
        payment.setType(PaymentType.LOAN_PAYMENT);
        payment.setPaymentDate(LocalDateTime.of(2024, 1, 15, 14, 30));
        payment.setPayedAt(LocalDateTime.of(2024, 1, 15, 14, 35));
        payment.setExpired(false);
        return payment;
    }

    @Test
    void getAllPaymentsShouldReturnPaymentsList() throws Exception {
        List<Payment> payments = Arrays.asList(createTestPayment());
        when(paymentService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountId").value(100))
                .andExpect(jsonPath("$[0].amount").value(250.75))
                .andExpect(jsonPath("$[0].isCredit").value(true))
                .andExpect(jsonPath("$[0].type").value("LOAN_PAYMENT"));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    void getPaymentByIdShouldReturnPayment() throws Exception {
        Payment payment = createTestPayment();
        when(paymentService.getPaymentById(1L)).thenReturn(payment);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(100))
                .andExpect(jsonPath("$.amount").value(250.75))
                .andExpect(jsonPath("$.isCredit").value(true));

        verify(paymentService, times(1)).getPaymentById(1L);
    }

    @Test
    void getPaymentsByAccountIdShouldReturnAccountPayments() throws Exception {
        List<Payment> payments = Arrays.asList(createTestPayment());
        when(paymentService.getPaymentsByAccountId(100L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/account/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountId").value(100))
                .andExpect(jsonPath("$[0].amount").value(250.75));

        verify(paymentService, times(1)).getPaymentsByAccountId(100L);
    }

    @Test
    void createPaymentWithValidDataShouldCreatePayment() throws Exception {
        Payment payment = createTestPayment();
        when(paymentService.createPayment(any(Payment.class))).thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(100))
                .andExpect(jsonPath("$.amount").value(250.75))
                .andExpect(jsonPath("$.isCredit").value(true));

        verify(paymentService, times(1)).createPayment(any(Payment.class));
    }

    @Test
    void createPaymentWithInvalidDataShouldReturnBadRequest() throws Exception {
        Payment invalidPayment = new Payment();
        invalidPayment.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayment)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).createPayment(any(Payment.class));
    }

    @Test
    void createPaymentWithMissingRequiredFieldsShouldReturnBadRequest() throws Exception {
        Payment invalidPayment = new Payment();

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayment)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).createPayment(any(Payment.class));
    }

    @Test
    void getPaymentsByAccountIdAndDateRangeWithInvalidDateParametersShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/payments/account/100/range")
                        .param("start", "invalid-date")
                        .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).getPaymentsByAccountIdAndDateRange(anyLong(), any(), any());
    }
}