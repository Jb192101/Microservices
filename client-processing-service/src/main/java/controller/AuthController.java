package controller;

import entity.User;
import service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import utils.JwtUtil;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userService.getUserByLogin(loginRequest.getLogin());

        if (userOpt.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body("Неверные учетные данные");
        }

        User user = userOpt.get();

        if (user.getRole() == entity.UserRole.BLOCKED_CLIENT) {
            return ResponseEntity.status(403).body("Клиент заблокирован");
        }

        String token = jwtUtil.generateToken(user.getLogin(), user.getRole().name());

        return ResponseEntity.ok(new JwtResponse(token, user.getRole().name()));
    }

    @Data
    public static class LoginRequest {
        private String login;
        private String password;
    }

    @Data
    public static class JwtResponse {
        private String token;
        private String role;

        public JwtResponse(String token, String role) {
            this.token = token;
            this.role = role;
        }
    }
}
