package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.CardController;
import entity.Card;
import entity.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.CardService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Card createTestCard() {
        Card card = new Card();
        card.setId(1L);
        card.setAccountId(100L);
        card.setCardId("1234567890123456");
        card.setStatus(CardStatus.ACTIVE);
        return card;
    }

    @Test
    void getAllCardsShouldReturnCards() throws Exception {
        List<Card> cards = Arrays.asList(createTestCard());
        when(cardService.getAllCards()).thenReturn(cards);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cardId").value("1234567890123456"));

        verify(cardService, times(1)).getAllCards();
    }

    @Test
    void getCardByIdShouldReturnCard() throws Exception {
        Card card = createTestCard();
        when(cardService.getCardById(1L)).thenReturn(card);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardId").value("1234567890123456"));

        verify(cardService, times(1)).getCardById(1L);
    }

    @Test
    void updateCardStatusShouldUpdateStatus() throws Exception {
        Card card = createTestCard();
        when(cardService.updateCardStatus(anyLong(), any(CardStatus.class))).thenReturn(card);

        mockMvc.perform(patch("/api/cards/1/status")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(cardService, times(1)).updateCardStatus(anyLong(), any(CardStatus.class));
    }
}