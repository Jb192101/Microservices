package repository;

import entity.ProductRegistry;
import entity.ProductStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRegistryRepository extends JpaRepository<ProductRegistry, Long> {
    List<ProductRegistry> findByClientId(Long clientId);
    List<ProductRegistry> findByProductId(Long productId);
    List<ProductRegistry> findByStatus(ProductStatus status);
    Optional<ProductRegistry> findByAccountId(Long accountId);

    @Query("SELECT pr FROM ProductRegistry pr WHERE pr.clientId = :clientId AND pr.status = 'ACTIVE'")
    List<ProductRegistry> findActiveProductsByClientId(@Param("clientId") Long clientId);

    boolean existsByAccountIdAndStatus(Long accountId, ProductStatus status);
}
