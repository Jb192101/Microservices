package t1.com.microservices.test.unit;

import entity.User;
import entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.UserRepository;
import service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.CURRENT_CLIENT);
    }

    @Test
    void createUserWithValidDataShouldSaveUser() {
        when(userRepository.existsByLogin("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals(UserRole.CURRENT_CLIENT, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithExistingLoginShouldThrowException() {
        when(userRepository.existsByLogin("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(testUser));

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WithRoleShouldSaveUserWithRole() {
        when(userRepository.existsByLogin("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser, UserRole.MASTER);

        assertEquals(UserRole.MASTER, result.getRole());
    }

    @Test
    void updateUserRoleWithValidUserIdShouldUpdateRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUserRole(1L, UserRole.BLOCKED_CLIENT);

        assertEquals(UserRole.BLOCKED_CLIENT, testUser.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    void blockClientShouldUpdateRoleToBlocked() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.blockClient(1L);

        assertEquals(UserRole.BLOCKED_CLIENT, testUser.getRole());
    }

    @Test
    void unblockClientShouldUpdateRoleToCurrentClient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.unblockClient(1L);

        assertEquals(UserRole.CURRENT_CLIENT, testUser.getRole());
    }

    @Test
    void getUserByIdShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getLogin());
    }

    @Test
    void updateUserWithValidDataShouldUpdateUser() {
        User updatedUser = new User();
        updatedUser.setLogin("newlogin");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByLogin("newlogin")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(1L, updatedUser);

        assertEquals("newlogin", result.getLogin());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUserWithExistingIdShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}