package t1.com.microservices.test.unit;

import entity.Client;
import entity.DocumentType;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ClientRepository;
import service.ClientService;
import utils.ClientIdGenerator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientIdGenerator clientIdGenerator;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        testClient = new Client();
        testClient.setId(1L);
        testClient.setClientId("CLIENT123");
        testClient.setUser(user);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testClient.setDocumentType(DocumentType.PASSPORT);
        testClient.setDocumentId("1234567890");
    }

    @Test
    void createClientWithGeneratedClientIdShouldSaveClient() {
        testClient.setClientId(null);
        when(clientIdGenerator.generateClientId()).thenReturn("GEN123");
        when(clientRepository.existsByClientId("GEN123")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        Client result = clientService.createClient(testClient);

        assertNotNull(result);
        verify(clientIdGenerator).generateClientId();
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createClientWithExistingClientIdShouldThrowException() {
        when(clientRepository.existsByClientId("CLIENT123")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                clientService.createClient(testClient));

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getClientByIdShouldReturnClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        Optional<Client> result = clientService.getClientById(1L);

        assertTrue(result.isPresent());
        assertEquals("CLIENT123", result.get().getClientId());
    }

    @Test
    void getClientByClientIdShouldReturnClient() {
        when(clientRepository.findByClientId("CLIENT123")).thenReturn(Optional.of(testClient));

        Optional<Client> result = clientService.getClientByClientId("CLIENT123");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getAllClientsShouldReturnClientsList() {
        List<Client> clients = Arrays.asList(testClient);
        when(clientRepository.findAll()).thenReturn(clients);

        List<Client> result = clientService.getAllClients();

        assertEquals(1, result.size());
        assertEquals("CLIENT123", result.get(0).getClientId());
    }

    @Test
    void updateClientWithNonExistingIdShouldThrowException() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                clientService.updateClient(1L, testClient));

        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClientWithExistingIdShouldDeleteClient() {
        when(clientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clientRepository).deleteById(1L);

        clientService.deleteClient(1L);

        verify(clientRepository).deleteById(1L);
    }

    @Test
    void deleteClientWithNonExistingIdShouldThrowException() {
        when(clientRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                clientService.deleteClient(1L));

        verify(clientRepository, never()).deleteById(any());
    }
}
