package service;

import com.t1.starter.pack.aop.annotations.LogDatasourceError;
import entity.User;
import entity.UserRole;
import repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return createUser(user, UserRole.CURRENT_CLIENT);
    }

    @LogDatasourceError
    public User createUser(User user, UserRole role) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("Логин уже существует: " + user.getLogin());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-mail уже существует: " + user.getEmail());
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    @LogDatasourceError
    public void updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id не найден: " + userId));

        user.setRole(newRole);
        userRepository.save(user);
    }

    public void blockClient(Long userId) {
        updateUserRole(userId, UserRole.BLOCKED_CLIENT);
    }

    public void unblockClient(Long userId) {
        updateUserRole(userId, UserRole.CURRENT_CLIENT);
    }

    @LogDatasourceError
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @LogDatasourceError
    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @LogDatasourceError
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @LogDatasourceError
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id не найден: " + id));

        if (!user.getLogin().equals(userDetails.getLogin()) &&
                userRepository.existsByLogin(userDetails.getLogin())) {
            throw new IllegalArgumentException("Логин уже существует: " + userDetails.getLogin());
        }

        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("E-mail уже существует: " + userDetails.getEmail());
        }

        user.setLogin(userDetails.getLogin());
        user.setPassword(userDetails.getPassword());
        user.setEmail(userDetails.getEmail());

        return userRepository.save(user);
    }

    @LogDatasourceError
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Пользователь с id не найден: " + id);
        }
        userRepository.deleteById(id);
    }
}
