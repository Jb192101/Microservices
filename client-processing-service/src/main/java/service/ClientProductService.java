package service;

import entity.ClientProduct;
import entity.ClientProductStatus;
import entity.ProductKey;
import kafka.KafkaProducer;
import kafka.dto.ClientCardMessage;
import kafka.dto.ClientProductMessage;
import kafka.dto.CreditDTO;
import repository.ClientProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientProductService {
    @Autowired
    private ClientProductRepository clientProductRepository;

    @Autowired
    private KafkaProducer kafkaProducer;

    public ClientProductService(ClientProductRepository clientProductRepository) {
        this.clientProductRepository = clientProductRepository;
    }

    public ClientProduct assignProductToClient(ClientProduct clientProduct, Long accountId,
                                               String paymentSystem, String cardType) {
        if (clientProduct.getOpenDate() == null) {
            clientProduct.setOpenDate(LocalDate.now());
        }

        if (clientProduct.getStatus() == null) {
            clientProduct.setStatus(ClientProductStatus.ACTIVE);
        }

        if (clientProductRepository.existsByClientIdAndProductId(
                clientProduct.getClient().getId(),
                clientProduct.getProduct().getId())) {
            throw new IllegalArgumentException("Продукт уже назначен клиенту");
        }

        kafkaProducer.sendClientCardEvent(new ClientCardMessage(
                        clientProduct.getClient().getId(),
                        accountId,
                        paymentSystem,
                        cardType
                 ),
                "CREATED");

        return clientProductRepository.save(clientProduct);
    }

    public Optional<ClientProduct> getClientProductById(Long id) {
        return clientProductRepository.findById(id);
    }

    public List<ClientProduct> getClientProductsByClientId(Long clientId) {
        return clientProductRepository.findByClientId(clientId);
    }

    public List<ClientProduct> getActiveClientProducts(Long clientId) {
        return clientProductRepository.findByClientIdAndStatus(clientId, ClientProductStatus.ACTIVE);
    }

    public ClientProduct updateClientProductStatus(Long id, ClientProductStatus status) {
        ClientProduct clientProduct = clientProductRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиентский продукт не найден с ID: " + id));

        if (status == ClientProductStatus.CLOSED && clientProduct.getCloseDate() == null) {
            clientProduct.setCloseDate(LocalDate.now());
        }

        clientProduct.setStatus(status);

        kafkaDecision(clientProduct, BigDecimal.ZERO);

        return clientProductRepository.save(clientProduct);
    }

    public void removeProductFromClient(Long id) {
        if (!clientProductRepository.existsById(id)) {
            throw new IllegalArgumentException("Клиентский продукт не найден с ID: " + id);
        }

        kafkaDecision(clientProductRepository.findById(id).get(), BigDecimal.ZERO);

        clientProductRepository.deleteById(id);
    }

    private void kafkaDecision(ClientProduct clientProduct, BigDecimal amount) {
        if(isTypeFirst(clientProduct.getProduct().getKey())) {
            ClientProductMessage msg = new ClientProductMessage(
                Long.valueOf(clientProduct.getProduct().getProductId()),
                    Long.valueOf(clientProduct.getClient().getClientId())
            );
            kafkaProducer.sendClientProductEvent(msg, "CREATED");
        } else if(isTypeSecond(clientProduct.getProduct().getKey())) {
            kafkaProducer.sendClientCreditProductEvent(new CreditDTO(
                        clientProduct.getClient().getClientId(),
                        amount
                    ),
                    "CREATED");
        }
    }

    private boolean isTypeFirst(ProductKey key) {
        return key == ProductKey.DC || key == ProductKey.CC || key == ProductKey.NS || key == ProductKey.PENS;
    }

    private boolean isTypeSecond(ProductKey key) {
        return key == ProductKey.IPO || key == ProductKey.PC || key == ProductKey.AC;
    }
}
