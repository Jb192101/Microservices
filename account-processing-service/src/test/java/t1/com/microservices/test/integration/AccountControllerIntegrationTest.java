package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.AccountController;
import entity.Account;
import entity.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.AccountService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Account createTestAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setClientId(100L);
        account.setProductId(200L);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }

    @Test
    void getAllAccountsShouldReturnAccounts() throws Exception {
        List<Account> accounts = Arrays.asList(createTestAccount());
        when(accountService.getAllAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientId").value(100));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void getAccountByIdShouldReturnAccount() throws Exception {
        Account account = createTestAccount();
        when(accountService.getAccountById(1L)).thenReturn(account);

        mockMvc.perform(get("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value(100));

        verify(accountService, times(1)).getAccountById(1L);
    }

    @Test
    void getAccountsByClientIdShouldReturnAccounts() throws Exception {
        List<Account> accounts = Arrays.asList(createTestAccount());
        when(accountService.getAccountsByClientId(100L)).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts/client/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientId").value(100));

        verify(accountService, times(1)).getAccountsByClientId(100L);
    }

    @Test
    void createAccountWithValidDataShouldCreateAccount() throws Exception {
        Account account = createTestAccount();
        when(accountService.createAccount(any(Account.class))).thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(accountService, times(1)).createAccount(any(Account.class));
    }

    @Test
    void updateAccountStatusWithValidRoleShouldUpdateStatus() throws Exception {
        Account account = createTestAccount();
        when(accountService.updateAccountStatus(anyLong(), any(AccountStatus.class))).thenReturn(account);

        mockMvc.perform(patch("/api/accounts/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(accountService, times(1)).updateAccountStatus(anyLong(), any(AccountStatus.class));
    }
}