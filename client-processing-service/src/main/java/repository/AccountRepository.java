package repository;

import entity.Account;
import entity.AccountStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientId(Long clientId);
    List<Account> findByProductId(Long productId);
    List<Account> findByStatus(AccountStatus status);
    Optional<Account> findByClientIdAndProductId(Long clientId, Long productId);

    @Query("SELECT a FROM Account a WHERE a.clientId = :clientId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByClientId(@Param("clientId") Long clientId);

    boolean existsByClientIdAndProductIdAndStatus(Long clientId, Long productId, AccountStatus status);
}