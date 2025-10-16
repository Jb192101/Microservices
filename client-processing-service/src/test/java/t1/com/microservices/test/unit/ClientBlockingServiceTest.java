package t1.com.microservices.test.unit;

import entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ClientBlockingService;
import service.ClientService;
import service.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientBlockingServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientBlockingService clientBlockingService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        entity.User user = new entity.User();
        user.setId(1L);

        testClient = new entity.Client();
        testClient.setUser(user);
    }

    @Test
    void blockClientByClientIdWhenClientExistsShouldBlockUser() {
        String clientId = "CLIENT123";
        when(clientService.getClientByClientId(clientId)).thenReturn(Optional.of(testClient));

        clientBlockingService.blockClientByClientId(clientId);

        verify(userService).blockClient(1L);
    }

    @Test
    void blockClientByClientIdWhenClientNotExistsShouldDoNothing() {
        String clientId = "NON_EXISTENT";
        when(clientService.getClientByClientId(clientId)).thenReturn(Optional.empty());

        clientBlockingService.blockClientByClientId(clientId);

        verify(userService, never()).blockClient(anyLong());
    }

    @Test
    void blockClientByUserIdShouldCallUserService() {
        clientBlockingService.blockClientByUserId(1L);

        verify(userService).blockClient(1L);
    }

    @Test
    void unblockClientByClientIdWhenClientExistsShouldUnblockUser() {
        String clientId = "CLIENT123";
        when(clientService.getClientByClientId(clientId)).thenReturn(Optional.of(testClient));

        clientBlockingService.unblockClientByClientId(clientId);

        verify(userService).unblockClient(1L);
    }

    @Test
    void unblockClientByUserIdShouldCallUserService() {
        clientBlockingService.unblockClientByUserId(1L);

        verify(userService).unblockClient(1L);
    }
}
