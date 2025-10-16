package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.ClientController;
import entity.Client;
import entity.DocumentType;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.ClientService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Client createTestClient() {
        User user = new User();
        user.setId(1L);

        Client client = new Client();
        client.setId(1L);
        client.setClientId("CLIENT123");
        client.setUser(user);
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setDateOfBirth(LocalDate.of(1990, 1, 1));
        client.setDocumentType(DocumentType.PASSPORT);
        client.setDocumentId("123456789");
        return client;
    }

    @Test
    void getClientByIdShouldReturnClient() throws Exception {
        Client client = createTestClient();
        when(clientService.getClientById(1L)).thenReturn(Optional.of(client));

        mockMvc.perform(get("/clients/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value("CLIENT123"));

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    void getClientByIdWhenNotExistsShouldReturnNotFound() throws Exception {
        when(clientService.getClientById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/clients/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    void getAllClientsShouldReturnClientsList() throws Exception {
        List<Client> clients = Arrays.asList(createTestClient());
        when(clientService.getAllClients()).thenReturn(clients);

        mockMvc.perform(get("/clients")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientId").value("CLIENT123"));

        verify(clientService, times(1)).getAllClients();
    }
}