package service;

import aop.annotations.LogDatasourceError;
import entity.Card;
import entity.CardStatus;
import repository.CardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class CardService {
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private AccountService accountService;

    public CardService(CardRepository cardRepository, AccountService accountService) {
        this.cardRepository = cardRepository;
        this.accountService = accountService;
    }

    @LogDatasourceError
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    @LogDatasourceError
    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Карта с ID не существует: " + id));
    }

    @LogDatasourceError
    public Card getCardByCardId(String cardId) {
        return cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new RuntimeException("Карта с cardID не существует: " + cardId));
    }

    @LogDatasourceError
    public List<Card> getCardsByAccountId(Long accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    @LogDatasourceError
    public List<Card> getActiveCardsByAccountId(Long accountId) {
        return cardRepository.findActiveCardsByAccountId(accountId);
    }

    @Transactional
    @LogDatasourceError
    public Card createCard(Card card) {
        validateCard(card);

        if (card.getCardId() == null) {
            card.setCardId(generateCardId());
        }

        Card savedCard = cardRepository.save(card);
        accountService.getAccountById(card.getAccountId());

        return savedCard;
    }

    @Transactional
    @LogDatasourceError
    public Card updateCardStatus(Long id, CardStatus status) {
        Card card = getCardById(id);
        card.setStatus(status);
        return cardRepository.save(card);
    }

    @Transactional
    @LogDatasourceError
    public void blockCard(Long id) {
        updateCardStatus(id, CardStatus.BLOCKED);
    }

    @LogDatasourceError
    private void validateCard(Card card) {
        if (!accountService.isAccountActive(card.getAccountId())) {
            throw new IllegalArgumentException("Счет заблокирован или закрыт, нельзя создать карту!");
        }

        if (cardRepository.existsByAccountIdAndStatus(card.getAccountId(), CardStatus.ACTIVE)) {
            throw new IllegalArgumentException("Аккаунт уже обладает действующей картой!");
        }
    }

    private String generateCardId() {
        Random random = new Random();
        StringBuilder cardId = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardId.append(random.nextInt(10));
        }
        return cardId.toString();
    }
}