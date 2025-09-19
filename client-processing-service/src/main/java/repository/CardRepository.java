package repository;

import entity.Card;
import entity.CardStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByAccountId(Long accountId);
    List<Card> findByStatus(CardStatus status);
    Optional<Card> findByCardId(String cardId);

    @Query("SELECT c FROM Card c WHERE c.accountId = :accountId AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByAccountId(@Param("accountId") Long accountId);

    boolean existsByAccountIdAndStatus(Long accountId, CardStatus status);
}