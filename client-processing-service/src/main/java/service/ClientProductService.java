package service;

import entity.ClientProduct;
import entity.ClientProductStatus;
import repository.ClientProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientProductService {
    @Autowired
    private ClientProductRepository clientProductRepository;

    public ClientProductService(ClientProductRepository clientProductRepository) {
        this.clientProductRepository = clientProductRepository;
    }

    public ClientProduct assignProductToClient(ClientProduct clientProduct) {
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

        return clientProductRepository.save(clientProduct);
    }

    public Optional<ClientProduct> getClientProductById(Long id) {
        return clientProductRepository.findById(id);
    }

    public List<ClientProduct> getClientProductsByClientId(Long clientId) {
        return clientProductRepository.findByClientId(clientId);
    }

    public List<ClientProduct> getClientProductsByProductId(Long productId) {
        return clientProductRepository.findByProductId(productId);
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
        return clientProductRepository.save(clientProduct);
    }

    public void removeProductFromClient(Long id) {
        if (!clientProductRepository.existsById(id)) {
            throw new IllegalArgumentException("Клиентский продукт не найден с ID: " + id);
        }
        clientProductRepository.deleteById(id);
    }
}
