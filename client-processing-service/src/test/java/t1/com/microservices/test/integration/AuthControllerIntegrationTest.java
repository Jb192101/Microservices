package t1.com.microservices.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.AuthController;
import entity.User;
import entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.UserService;
import utils.JwtUtil;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginWithValidCredentialsShouldReturnToken() throws Exception {
        User user = new User();
        user.setLogin("testuser");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.CURRENT_CLIENT);

        when(userService.getUserByLogin("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-token");

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setLogin("testuser");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.role").value("CURRENT_CLIENT"));

        verify(userService, times(1)).getUserByLogin("testuser");
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
        verify(jwtUtil, times(1)).generateToken(anyString(), anyString());
    }

    @Test
    void loginWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {
        when(userService.getUserByLogin("testuser")).thenReturn(Optional.empty());

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setLogin("testuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).getUserByLogin("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void loginWithBlockedClientShouldReturnForbidden() throws Exception {
        User user = new User();
        user.setLogin("blockeduser");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.BLOCKED_CLIENT);

        when(userService.getUserByLogin("blockeduser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setLogin("blockeduser");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());

        verify(userService, times(1)).getUserByLogin("blockeduser");
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
}