package repository;

import entity.Transaction;
import entity.TransactionStatus;
import entity.TransactionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByCardId(Long cardId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByType(TransactionType type);
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp BETWEEN :startDate AND :endDate")
    List<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.type = 'DEPOSIT' AND t.status = 'COMPLETE'")
    Double getTotalDepositsByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.type = 'WITHDRAWAL' AND t.status = 'COMPLETE'")
    Double getTotalWithdrawalsByAccountId(@Param("accountId") Long accountId);

    int countByCardIdAndTimestampAfter(Long cardId, LocalDateTime timestamp);
}
