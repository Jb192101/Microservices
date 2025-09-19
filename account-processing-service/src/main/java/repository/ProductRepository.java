package repository;

import entity.Product;
import entity.ProductKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByKey(ProductKey key);
    List<Product> findByKeyIn(List<ProductKey> keys);
    boolean existsByKey(ProductKey key);
}
