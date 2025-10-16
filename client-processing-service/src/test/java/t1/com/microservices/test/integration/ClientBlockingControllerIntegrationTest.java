package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.ClientBlockingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.ClientBlockingService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClientBlockingControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private ClientBlockingService clientBlockingService;

    @InjectMocks
    private ClientBlockingController clientBlockingController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientBlockingController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void blockClientByClientIdWithValidRoleShouldBlockClient() throws Exception {
        doNothing().when(clientBlockingService).blockClientByClientId(anyString());

        ClientBlockingController.BlockRequest request = new ClientBlockingController.BlockRequest();
        request.setClientId("CLIENT123");

        mockMvc.perform(post("/internal/client-blocking/block-by-client-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(clientBlockingService, times(1)).blockClientByClientId("CLIENT123");
    }

    @Test
    void blockClientByUserIdWithValidRoleShouldBlockClient() throws Exception {
        doNothing().when(clientBlockingService).blockClientByUserId(anyLong());

        ClientBlockingController.BlockRequest request = new ClientBlockingController.BlockRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/internal/client-blocking/block-by-user-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(clientBlockingService, times(1)).blockClientByUserId(1L);
    }

    @Test
    void unblockClientByClientIdWithValidRoleShouldUnblockClient() throws Exception {
        doNothing().when(clientBlockingService).unblockClientByClientId(anyString());

        ClientBlockingController.BlockRequest request = new ClientBlockingController.BlockRequest();
        request.setClientId("CLIENT123");

        mockMvc.perform(post("/internal/client-blocking/unblock-by-client-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(clientBlockingService, times(1)).unblockClientByClientId("CLIENT123");
    }
}