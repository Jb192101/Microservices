package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.CreditProcessingController;
import kafka.dto.CreditDecisionDTO;
import kafka.dto.CreditProductRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.CreditDecisionService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CreditProcessingControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private CreditDecisionService creditDecisionService;

    @InjectMocks
    private CreditProcessingController creditProcessingController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(creditProcessingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private CreditProductRequestDTO createTestCreditRequest() {
        CreditProductRequestDTO request = new CreditProductRequestDTO();
        request.setClientId(1L);
        request.setAmount(BigDecimal.valueOf(50000));
        return request;
    }

    private CreditDecisionDTO createTestCreditDecision() {
        return new CreditDecisionDTO(true, "Кредит одобрен", 1L);
    }

    @Test
    void makeCreditDecisionWithValidRequestShouldReturnDecision() throws Exception {
        CreditProductRequestDTO request = createTestCreditRequest();
        CreditDecisionDTO decision = createTestCreditDecision();

        when(creditDecisionService.makeCreditDecision(any(CreditProductRequestDTO.class))).thenReturn(decision);

        mockMvc.perform(post("/api/credit/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.clientId").value(1L));

        verify(creditDecisionService, times(1)).makeCreditDecision(any(CreditProductRequestDTO.class));
    }

    @Test
    void healthCheckShouldReturnServiceStatus() throws Exception {
        mockMvc.perform(get("/api/credit/health")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("MС-3: сервис обработки кредитов запущен"));
    }

    @Test
    void makeCreditDecisionWithRejectedRequestShouldReturnRejection() throws Exception {
        CreditProductRequestDTO request = createTestCreditRequest();
        CreditDecisionDTO rejectedDecision = new CreditDecisionDTO(false, "Превышен лимит задолженности", 1L);

        when(creditDecisionService.makeCreditDecision(any(CreditProductRequestDTO.class))).thenReturn(rejectedDecision);

        mockMvc.perform(post("/api/credit/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(false));

        verify(creditDecisionService, times(1)).makeCreditDecision(any(CreditProductRequestDTO.class));
    }
}