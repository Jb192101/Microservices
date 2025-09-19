package repository;

import entity.ClientProduct;
import entity.ClientProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientProductRepository extends JpaRepository<ClientProduct, Long> {
    List<ClientProduct> findByClientId(Long clientId);
    List<ClientProduct> findByProductId(Long productId);
    List<ClientProduct> findByClientIdAndStatus(Long clientId, ClientProductStatus status);
    Optional<ClientProduct> findByClientIdAndProductId(Long clientId, Long productId);
    boolean existsByClientIdAndProductId(Long clientId, Long productId);
}