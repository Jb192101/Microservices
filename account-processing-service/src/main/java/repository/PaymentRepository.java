package repository;

import entity.Payment;
import entity.PaymentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAccountId(Long accountId);
    List<Payment> findByIsCredit(Boolean isCredit);
    List<Payment> findByType(PaymentType type);
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM Payment p WHERE p.accountId = :accountId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Payment> findUnpaidPaymentsByAccountId(Long accountId);
    List<Payment> findByAccountIdAndPaymentDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);
}
