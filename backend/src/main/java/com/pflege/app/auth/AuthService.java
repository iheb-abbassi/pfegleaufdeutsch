package com.pflege.app.auth;

import com.pflege.app.common.ApiException;
import com.pflege.app.domain.entity.AuthProvider;
import com.pflege.app.domain.entity.Role;
import com.pflege.app.domain.entity.User;
import com.pflege.app.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request) {
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        });
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(user);
        return issueTokens(user);
    }

    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return issueTokens(user);
    }

    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest request) {
        Claims claims = jwtService.parse(request.refreshToken());
        User user = userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        return issueTokens(user);
    }

    public AuthDtos.UserResponse me(AuthenticatedUser authenticatedUser) {
        return new AuthDtos.UserResponse(authenticatedUser.userId(), authenticatedUser.email(), authenticatedUser.role().name());
    }

    public AuthDtos.TokenResponse issueTokens(User user) {
        return new AuthDtos.TokenResponse(
                jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole()),
                jwtService.generateRefreshToken(user.getId(), user.getEmail(), user.getRole()),
                new AuthDtos.UserResponse(user.getId(), user.getEmail(), user.getRole().name())
        );
    }
}
