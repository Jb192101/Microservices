package controller;

import aop.annotations.HttpIncomeRequestLog;
import aop.annotations.HttpOutcomeRequestLog;
import entity.Card;
import entity.CardStatus;
import service.CardService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    @Autowired
    private CardService cardService;

    @GetMapping
    @HttpOutcomeRequestLog
    public ResponseEntity<List<Card>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("/{id}")
    @HttpOutcomeRequestLog
    public ResponseEntity<Card> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/card/{cardId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<Card> getCardByCardId(@PathVariable String cardId) {
        return ResponseEntity.ok(cardService.getCardByCardId(cardId));
    }

    @GetMapping("/account/{accountId}")
    @HttpOutcomeRequestLog
    public ResponseEntity<List<Card>> getCardsByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(cardService.getCardsByAccountId(accountId));
    }

    @PostMapping
    @HttpIncomeRequestLog
    public ResponseEntity<Card> createCard(@Valid @RequestBody Card card) {
        return ResponseEntity.ok(cardService.createCard(card));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Card> updateCardStatus(
            @PathVariable Long id,
            @RequestParam CardStatus status) {
        return ResponseEntity.ok(cardService.updateCardStatus(id, status));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }
}
