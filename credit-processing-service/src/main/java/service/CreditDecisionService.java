package service;

import configuration.CreditLimitConfig;
import entity.ProductRegistry;
import entity.ProductStatus;
import kafka.dto.ClientInfoDTO;
import kafka.dto.CreditDecisionDTO;
import kafka.dto.CreditProductRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditDecisionService {
    @Autowired
    private final ProductRegistryService productRegistryService;
    @Autowired
    private final CreditLimitConfig creditLimitConfig;
    @Autowired
    private final RestTemplate restTemplate;

    // Обращение к МС-1
    private static final String MS1_CLIENT_INFO_URL = "http://localhost:8081/client-processing-service/clients/{clientId}";

    public CreditDecisionDTO makeCreditDecision(CreditProductRequestDTO request) {
        // Получаем информацию о клиенте из МС-1
        ClientInfoDTO clientInfo = getClientInfoFromClients(request.getClientId());

        // Проверяем существующие кредитные продукты клиента
        List<ProductRegistry> existingCreditProducts = productRegistryService
                .getActiveProductsByClientId(request.getClientId());

        // Проверка 1: Суммарная задолженность не превышает лимит
        BigDecimal totalDebt = calculateTotalDebt(existingCreditProducts);
        BigDecimal requestedAmount = request.getAmount();

        if (totalDebt.add(requestedAmount).compareTo(creditLimitConfig.getMaxTotalDebt()) > 0) {
            return new CreditDecisionDTO(false,
                    "Суммарная задолженность превышает установленный лимит",
                    request.getClientId());
        }

        // Проверка 2: Наличие просрочек по текущим продуктам
        if (hasOverduePayments(existingCreditProducts)) {
            return new CreditDecisionDTO(false,
                    "Имеются просрочки по существующим кредитным продуктам",
                    request.getClientId());
        }

        // Проверка 3: Если просрочек нет и сумма в пределах лимита - одобряем
        return new CreditDecisionDTO(true, "Кредит одобрен", request.getClientId());
    }

    private ClientInfoDTO getClientInfoFromClients(Long clientId) {
        try {
            return restTemplate.getForObject(MS1_CLIENT_INFO_URL, ClientInfoDTO.class, clientId);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении информации о клиенте из МС-1: " + e.getMessage());
        }
    }

    private BigDecimal calculateTotalDebt(List<ProductRegistry> products) {
        return products.stream()
                .map(product -> {
                    // Здесь должна быть логика расчета текущей задолженности по продукту
                    // Для упрощения возвращаем фиксированное значение или получаем из другого сервиса
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasOverduePayments(List<ProductRegistry> products) {
        return products.stream()
                .anyMatch(product -> product.getStatus() == ProductStatus.OVERDUE);
    }
}