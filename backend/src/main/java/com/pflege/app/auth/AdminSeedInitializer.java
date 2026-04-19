package com.pflege.app.auth;

import com.pflege.app.config.AppProperties;
import com.pflege.app.domain.entity.AuthProvider;
import com.pflege.app.domain.entity.Role;
import com.pflege.app.domain.entity.User;
import com.pflege.app.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;

    public AdminSeedInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, AppProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        User user = new User();
        user.setEmail(properties.getAdminSeed().getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(properties.getAdminSeed().getPassword()));
        user.setRole(Role.ADMIN);
        user.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(user);
    }
}
