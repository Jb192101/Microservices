package kafka;

import entity.*;
import kafka.dto.ClientCardMessage;
import kafka.dto.ClientProductMessage;
import kafka.dto.ClientTransactionMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import service.AccountService;
import service.CardService;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class KafkaConsumer {
    private final AccountService accountService;
    private final CardService cardService;
    private final TransactionService transactionService;

    public KafkaConsumer(AccountService accountService,
                                CardService cardService,
                                TransactionService transactionService) {
        this.accountService = accountService;
        this.cardService = cardService;
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "client_products", groupId = "ms-group")
    public void handleClientProduct(ClientProductMessage message) {
        try {
            Account account = new Account();
            account.setClientId(message.getClientId());
            account.setProductId(message.getProductId());
            account.setBalance(message.getAmount());
            account.setInterestRate(BigDecimal.ZERO);
            account.setStatus(AccountStatus.ACTIVE);

            accountService.createAccount(account);
        } catch (Exception e) {
        }
    }

    @KafkaListener(topics = "client_cards", groupId = "ms-group")
    public void handleClientCard(ClientCardMessage message) {
        try {
            Account account = accountService.getAccountById(message.getAccountId());

            if(account == null)
                return;

            if (account.getStatus() != AccountStatus.BLOCKED &&
                    account.getStatus() != AccountStatus.CLOSED) {

                Card card = new Card();
                card.setAccountId(message.getAccountId());
                card.setPaymentSystem(PaymentSystem.valueOf(message.getPaymentSystem()));
                card.setStatus(CardStatus.ACTIVE);

                cardService.createCard(card);

                account.setCardExist(true);
                accountService.updateAccountStatus(account.getId(), account.getStatus());
            }
        } catch (Exception e) {
        }
    }

    @KafkaListener(topics = "client_transactions", groupId = "ms-group")
    public void handleClientTransaction(ClientTransactionMessage message) {
        try {
            Transaction transaction = new Transaction();
            transaction.setAccountId(message.getAccountId());
            transaction.setCardId(message.getCardId());
            transaction.setAmount(message.getAmount());
            transaction.setType(TransactionType.valueOf(message.getTransactionType()));
            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction.setTimestamp(LocalDateTime.now());

            transactionService.createTransaction(transaction);

        } catch (Exception e) {
        }
    }
}