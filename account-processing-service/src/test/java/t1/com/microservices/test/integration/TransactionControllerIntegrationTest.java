package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.TransactionController;
import entity.Transaction;
import entity.TransactionStatus;
import entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Transaction createTestTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountId(100L);
        transaction.setCardId(200L);
        transaction.setAmount(BigDecimal.valueOf(500.0));
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETE);
        transaction.setTimestamp(LocalDateTime.of(2024, 1, 1, 12, 0));
        return transaction;
    }

    @Test
    void getAllTransactionsShouldReturnTransactionsList() throws Exception {
        List<Transaction> transactions = Arrays.asList(createTestTransaction());
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountId").value(100))
                .andExpect(jsonPath("$[0].amount").value(500.0))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].status").value("COMPLETE"));

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    void getTransactionByIdShouldReturnTransaction() throws Exception {
        Transaction transaction = createTestTransaction();
        when(transactionService.getTransactionById(1L)).thenReturn(transaction);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(100))
                .andExpect(jsonPath("$.amount").value(500.0));

        verify(transactionService, times(1)).getTransactionById(1L);
    }

    @Test
    void getTransactionsByAccountIdShouldReturnAccountTransactions() throws Exception {
        List<Transaction> transactions = Arrays.asList(createTestTransaction());
        when(transactionService.getTransactionsByAccountId(100L)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/account/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountId").value(100));

        verify(transactionService, times(1)).getTransactionsByAccountId(100L);
    }

    @Test
    void getAccountBalanceShouldReturnBalance() throws Exception {
        BigDecimal balance = BigDecimal.valueOf(1500.50);
        when(transactionService.getAccountBalance(100L)).thenReturn(balance);

        mockMvc.perform(get("/api/transactions/account/100/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1500.50));

        verify(transactionService, times(1)).getAccountBalance(100L);
    }

    @Test
    void createTransactionWithValidDataShouldCreateTransaction() throws Exception {
        Transaction transaction = createTestTransaction();
        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(transaction);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(100))
                .andExpect(jsonPath("$.amount").value(500.0));

        verify(transactionService, times(1)).createTransaction(any(Transaction.class));
    }

    @Test
    void updateTransactionStatusShouldUpdateStatus() throws Exception {
        Transaction updatedTransaction = createTestTransaction();
        updatedTransaction.setStatus(TransactionStatus.BLOCKED);

        when(transactionService.updateTransactionStatus(anyLong(), any(TransactionStatus.class)))
                .thenReturn(updatedTransaction);

        mockMvc.perform(patch("/api/transactions/1/status")
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(transactionService, times(1)).updateTransactionStatus(anyLong(), any(TransactionStatus.class));
    }

    @Test
    void updateTransactionStatusWithInvalidStatusShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/api/transactions/1/status")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).updateTransactionStatus(anyLong(), any(TransactionStatus.class));
    }
}