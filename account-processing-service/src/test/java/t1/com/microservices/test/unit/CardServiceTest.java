package t1.com.microservices.test.unit;

import entity.Card;
import entity.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.CardRepository;
import service.AccountService;
import service.CardService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CardService cardService;

    private Card testCard;

    @BeforeEach
    void setUp() {
        testCard = new Card();
        testCard.setId(1L);
        testCard.setAccountId(100L);
        testCard.setCardId("1234567890123456");
        testCard.setStatus(CardStatus.ACTIVE);
    }

    @Test
    void getCardByIdWhenCardExistsShouldReturnCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        Card result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
        verify(cardRepository).findById(1L);
    }

    @Test
    void createCardWithValidDataShouldCreateCard() {
        when(accountService.isAccountActive(100L)).thenReturn(true);
        when(cardRepository.existsByAccountIdAndStatus(100L, CardStatus.ACTIVE)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(accountService.getAccountById(100L)).thenReturn(null);

        Card result = cardService.createCard(testCard);

        assertNotNull(result);
        verify(cardRepository).save(any(Card.class));
        verify(accountService).isAccountActive(100L);
    }

    @Test
    void createCardWithInactiveAccountShouldThrowException() {
        when(accountService.isAccountActive(100L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(testCard));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void updateCardStatusShouldUpdateStatus() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        Card result = cardService.updateCardStatus(1L, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void blockCardShouldBlockCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }
}
