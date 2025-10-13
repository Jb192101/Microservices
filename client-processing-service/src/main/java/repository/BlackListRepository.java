package repository;

import entity.BlacklistRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListRepository extends JpaRepository<BlacklistRegistry, Long> {
    boolean existsByBlacklistRegistryDocumentId(String id);
}
