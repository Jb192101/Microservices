package kafka;

import entity.*;
import kafka.dto.ClientCardMessage;
import kafka.dto.ClientPaymentMessage;
import kafka.dto.ClientProductMessage;
import kafka.dto.ClientTransactionMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import service.AccountService;
import service.CardService;
import service.PaymentService;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class KafkaConsumer {
    private final AccountService accountService;
    private final CardService cardService;
    private final TransactionService transactionService;
    private final PaymentService paymentService;

    public KafkaConsumer(AccountService accountService,
                                CardService cardService,
                                TransactionService transactionService,
                                PaymentService paymentService) {
        this.accountService = accountService;
        this.cardService = cardService;
        this.transactionService = transactionService;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "client_products", groupId = "ms-group")
    public void handleClientProduct(ClientProductMessage message) {
        try {
            Account account = new Account();
            account.setClientId(message.getClientId());
            account.setProductId(message.getProductId());
            account.setBalance(BigDecimal.ZERO);
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
            Account account = accountService.getAccountById(message.getAccountId());
            if (account == null) return;

            if (account.getStatus() == AccountStatus.BLOCKED ||
                    account.getStatus() == AccountStatus.FROZEN) {
                return;
            }

            if (transactionService.isSuspiciousActivity(message.getCardId())) {
                accountService.updateAccountStatus(account.getId(), AccountStatus.BLOCKED);
                cardService.blockCard(message.getCardId());
                return;
            }

            Transaction transaction = new Transaction();
            transaction.setAccountId(message.getAccountId());
            transaction.setCardId(message.getCardId());
            transaction.setAmount(message.getAmount());
            transaction.setType(TransactionType.valueOf(message.getTransactionType()));
            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction.setTimestamp(LocalDateTime.now());

            boolean isCredit = message.getTransactionType().equals("DEPOSIT") ||
                    message.getTransactionType().equals("TRANSFER_IN");

            accountService.updateAccountBalance(account.getId(), message.getAmount(), isCredit);

            if (account.getRecalc() && isCredit) {
                paymentService.createPaymentSchedule(account.getId(), account.getInterestRate());
            }

            if (account.getRecalc() && isCredit &&
                    paymentService.isPaymentDue(account.getId()) &&
                    account.getBalance().compareTo(message.getAmount()) >= 0) {

                accountService.updateAccountBalance(account.getId(), message.getAmount(), false);
            }

            transactionService.createTransaction(transaction);

        } catch (Exception e) {
        }
    }

    @KafkaListener(topics = "client_payments", groupId = "ms-group")
    public void handleClientPayment(ClientPaymentMessage message) {
        try {
            Account account = accountService.getAccountById(message.getAccountId());
            if (account == null) return;

            BigDecimal creditDebt = paymentService.calculateCreditDebt(account.getId());
            if (message.getAmount().compareTo(creditDebt) == 0) {
                accountService.updateAccountBalance(account.getId(), message.getAmount(), false);

                Payment payment = new Payment();
                payment.setAccountId(account.getId());
                payment.setAmount(message.getAmount());
                payment.setPaymentDate(LocalDateTime.now());
                payment.setIsCredit(true);
                payment.setType(PaymentType.LOAN_PAYMENT);
                payment.setPayedAt(LocalDateTime.now());

                paymentService.createPayment(payment);

                paymentService.updateExistingPayments(account.getId());
            }
        } catch (Exception e) {
        }
    }
}