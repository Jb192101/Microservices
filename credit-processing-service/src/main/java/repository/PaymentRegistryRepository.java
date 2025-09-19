package repository;

import entity.PaymentRegistry;
import entity.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRegistryRepository extends JpaRepository<PaymentRegistry, Long> {
    List<PaymentRegistry> findByProductRegistryId(Long productRegistryId);
    List<PaymentRegistry> findByPaymentDate(LocalDate paymentDate);
    List<PaymentRegistry> findByExpired(Boolean expired);
    List<PaymentRegistry> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT p FROM PaymentRegistry p WHERE p.paymentExpirationDate < :currentDate AND p.paymentStatus = 'PENDING'")
    List<PaymentRegistry> findOverduePayments(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT p FROM PaymentRegistry p WHERE p.productRegistry.clientId = :clientId")
    List<PaymentRegistry> findByClientId(@Param("clientId") Long clientId);
}