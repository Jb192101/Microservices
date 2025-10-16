package t1.com.microservices.test.unit;

import configuration.CreditLimitConfig;
import entity.ProductRegistry;
import entity.ProductStatus;
import kafka.dto.ClientInfoDTO;
import kafka.dto.CreditDecisionDTO;
import kafka.dto.CreditProductRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import service.CreditDecisionService;
import service.ProductRegistryService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditDecisionServiceTest {
    @Mock
    private ProductRegistryService productRegistryService;

    @Mock
    private CreditLimitConfig creditLimitConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CreditDecisionService creditDecisionService;

    private CreditProductRequestDTO request;
    private ClientInfoDTO clientInfo;

    @BeforeEach
    void setUp() {
        request = new CreditProductRequestDTO();
        request.setClientId(1L);
        request.setAmount(new BigDecimal("50000"));

        clientInfo = new ClientInfoDTO();
        clientInfo.setClientId(1L);
        clientInfo.setFullName("John Doe");
    }

    @Test
    void makeCreditDecisionApproved() {
        when(restTemplate.getForObject(anyString(), eq(ClientInfoDTO.class), anyLong()))
                .thenReturn(clientInfo);
        when(productRegistryService.getActiveProductsByClientId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(creditLimitConfig.getMaxTotalDebt()).thenReturn(new BigDecimal("1000000"));

        CreditDecisionDTO decision = creditDecisionService.makeCreditDecision(request);

        assertTrue(decision.getApproved());
        assertEquals(1L, decision.getClientId());
    }

    @Test
    void makeCreditDecisionExceedsDebtLimitReturnsNegativeDecision() {
        ProductRegistry existingProduct = new ProductRegistry();
        existingProduct.setStatus(ProductStatus.ACTIVE);

        when(restTemplate.getForObject(anyString(), eq(ClientInfoDTO.class), anyLong()))
                .thenReturn(clientInfo);
        when(productRegistryService.getActiveProductsByClientId(anyLong()))
                .thenReturn(Arrays.asList(existingProduct));
        when(creditLimitConfig.getMaxTotalDebt()).thenReturn(new BigDecimal("10000"));

        CreditDecisionDTO decision = creditDecisionService.makeCreditDecision(request);

        assertFalse(decision.getApproved());
    }

    @Test
    void makeCreditDecisionHasOverduePayments() {
        ProductRegistry overdueProduct = new ProductRegistry();
        overdueProduct.setStatus(ProductStatus.OVERDUE);

        when(restTemplate.getForObject(anyString(), eq(ClientInfoDTO.class), anyLong()))
                .thenReturn(clientInfo);
        when(productRegistryService.getActiveProductsByClientId(anyLong()))
                .thenReturn(Arrays.asList(overdueProduct));
        when(creditLimitConfig.getMaxTotalDebt()).thenReturn(new BigDecimal("1000000"));

        CreditDecisionDTO decision = creditDecisionService.makeCreditDecision(request);

        assertFalse(decision.getApproved());
    }
}
