package com.pflege.app;

import com.pflege.app.auth.AuthDtos;
import com.pflege.app.auth.AuthService;
import com.pflege.app.auth.JwtService;
import com.pflege.app.common.ApiException;
import com.pflege.app.domain.entity.AuthProvider;
import com.pflege.app.domain.entity.Role;
import com.pflege.app.domain.entity.User;
import com.pflege.app.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Test
    void registerCreatesLocalStudentAccount() {
        AuthService service = new AuthService(userRepository, new BCryptPasswordEncoder(), jwtService);
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.empty());
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");

        AuthDtos.TokenResponse response = service.register(new AuthDtos.RegisterRequest(" Student@Example.com ", "password123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("student@example.com");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(new BCryptPasswordEncoder().matches("password123", saved.getPasswordHash())).isTrue();
        assertThat(response.user().role()).isEqualTo("USER");
    }

    @Test
    void registerRejectsExistingEmail() {
        AuthService service = new AuthService(userRepository, new BCryptPasswordEncoder(), jwtService);
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.register(new AuthDtos.RegisterRequest("student@example.com", "password123")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Email already in use");
    }
}
