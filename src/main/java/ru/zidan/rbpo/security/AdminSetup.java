package ru.zidan.rbpo.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.zidan.rbpo.entity.User;
import ru.zidan.rbpo.repository.UserRepository;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminSetup implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${MUSEUM_ADMIN_USER:}")
    private String username;
    @Value("${MUSEUM_ADMIN_PASSWORD:}")
    private String password;

    @Override
    @NullMarked
    public void run(String... args) {
        if (username.isBlank() || password.isBlank()) {
            return;
        }
        // Существующий аккаунт автоматически администратором не становится.
        if (userRepository.findByUsername(username) != null) {
            return;
        }
        if (!PasswordRules.valid(password)) {
            throw new IllegalArgumentException("Invalid admin password configuration");
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setFullName("Администратор музея");
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
    }
}
