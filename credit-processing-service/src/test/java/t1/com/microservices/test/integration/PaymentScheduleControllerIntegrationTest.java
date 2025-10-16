package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.PaymentScheduleController;
import kafka.dto.PaymentScheduleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.PaymentScheduleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentScheduleControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private PaymentScheduleService paymentScheduleService;

    @InjectMocks
    private PaymentScheduleController paymentScheduleController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentScheduleController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private PaymentScheduleDTO createTestPaymentSchedule() {
        return new PaymentScheduleDTO(
                1L,
                1,
                LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(1500.50),
                BigDecimal.valueOf(200.50),
                BigDecimal.valueOf(1300.00),
                BigDecimal.valueOf(48500.00)
        );
    }

    @Test
    void getPaymentScheduleByProductShouldReturnSchedule() throws Exception {
        List<PaymentScheduleDTO> schedule = Arrays.asList(createTestPaymentSchedule());
        when(paymentScheduleService.getPaymentScheduleByProduct(1L)).thenReturn(schedule);

        mockMvc.perform(get("/api/payment-schedule/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productRegistryId").value(1))
                .andExpect(jsonPath("$[0].paymentNumber").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(1500.50));

        verify(paymentScheduleService, times(1)).getPaymentScheduleByProduct(1L);
    }

    @Test
    void getPaymentScheduleByProductNotFoundShouldReturnNotFound() throws Exception {
        when(paymentScheduleService.getPaymentScheduleByProduct(999L))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/payment-schedule/product/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(paymentScheduleService, times(1)).getPaymentScheduleByProduct(999L);
    }

    @Test
    void getUpcomingPaymentsShouldReturnUpcomingPayments() throws Exception {
        List<PaymentScheduleDTO> upcomingPayments = Arrays.asList(createTestPaymentSchedule());
        when(paymentScheduleService.getPaymentScheduleByProduct(1L)).thenReturn(upcomingPayments);

        mockMvc.perform(get("/api/payment-schedule/product/1/upcoming")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentNumber").value(1))
                .andExpect(jsonPath("$[0].interestAmount").value(200.50));

        verify(paymentScheduleService, times(1)).getPaymentScheduleByProduct(1L);
    }

    @Test
    void getOverduePaymentsShouldReturnOverduePayments() throws Exception {
        PaymentScheduleDTO overduePayment = new PaymentScheduleDTO(
                1L,
                1,
                LocalDate.now().minusDays(10),
                BigDecimal.valueOf(1500.50),
                BigDecimal.valueOf(200.50),
                BigDecimal.valueOf(1300.00),
                BigDecimal.valueOf(48500.00)
        );
        List<PaymentScheduleDTO> overduePayments = Arrays.asList(overduePayment);
        when(paymentScheduleService.getPaymentScheduleByProduct(1L)).thenReturn(overduePayments);

        mockMvc.perform(get("/api/payment-schedule/product/1/overdue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentNumber").value(1));

        verify(paymentScheduleService, times(1)).getPaymentScheduleByProduct(1L);
    }

    @Test
    void getPaymentScheduleByClientNotFoundShouldReturnNotFound() throws Exception {
        when(paymentScheduleService.getPaymentScheduleByClientId(999L))
                .thenThrow(new RuntimeException("Client not found"));

        mockMvc.perform(get("/api/payment-schedule/client/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(paymentScheduleService, times(1)).getPaymentScheduleByClientId(999L);
    }
}