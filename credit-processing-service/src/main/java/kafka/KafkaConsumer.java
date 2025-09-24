package kafka;

import entity.ProductRegistry;
import entity.ProductStatus;
import jakarta.transaction.Transactional;
import kafka.dto.CreditDecisionDTO;
import kafka.dto.CreditProductRequestDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import service.*;

import java.time.LocalDate;
import java.util.List;

@Component
public class KafkaConsumer {
    private final CreditDecisionService creditDecisionService;
    private final ProductRegistryService productRegistryService;
    private final PaymentScheduleService paymentScheduleService;

    public KafkaConsumer(CreditDecisionService creditDecisionService, ProductRegistryService productRegistryService,
                         PaymentScheduleService paymentScheduleService) {
        this.creditDecisionService = creditDecisionService;
        this.productRegistryService = productRegistryService;
        this.paymentScheduleService = paymentScheduleService;
    }

    @KafkaListener(topics = "client_credit_products", groupId = "ms-group")
    @Transactional
    public void processCreditProduct(@Payload CreditProductRequestDTO request) {
        try {
            CreditDecisionDTO decision = creditDecisionService.makeCreditDecision(request);

            if (!decision.getApproved()) {
                // Логируем отказ и выходим
                System.out.println("Кредит отклонен для клиента " + request.getClientId() +
                        ": " + decision.getReason());
                return;
            }

            // Создание продукта
            ProductRegistry product = createCreditProduct(request);
            decision.setProductRegistryId(product.getId());

            // Создание графика платежей
            createPaymentSchedule(product, request);

            System.out.println("Кредитный продукт успешно создан для клиента " + request.getClientId());
        } catch (Exception e) {
            System.err.println("Ошибка при обработке кредитного продукта: " + e.getMessage());
            throw new RuntimeException("Ошибка обработки кредитного продукта", e);
        }
    }

    private ProductRegistry createCreditProduct(CreditProductRequestDTO request) {
        ProductRegistry product = new ProductRegistry();
        product.setClientId(request.getClientId());
        product.setAccountId(request.getAccountId());
        product.setProductId(request.getProductId());
        product.setInterestRate(request.getInterestRate());
        product.setOpenDate(LocalDate.now());
        product.setStatus(ProductStatus.ACTIVE);
        product.setMonthCount(request.getMonthCount());

        return productRegistryService.createProduct(product);
    }

    private void createPaymentSchedule(ProductRegistry product, CreditProductRequestDTO request) {
        try {
            List<?> savedPayments = paymentScheduleService.createAndSavePaymentSchedule(
                    product,
                    request.getAmount(),
                    request.getInterestRate(),
                    request.getMonthCount()
            );
        } catch (Exception e) {
            // Откатываем создание продукта если не удалось создать график
            productRegistryService.updateProductStatus(product.getId(), ProductStatus.BLOCKED);
            throw e;
        }
    }
}