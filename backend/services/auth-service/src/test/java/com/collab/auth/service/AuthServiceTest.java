package com.collab.auth.service;

import com.collab.auth.domain.User;
import com.collab.auth.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupUser() {
        userRepository.deleteAll();
        userRepository.save(new User(
                UUID.randomUUID(),
                "login@example.com",
                passwordEncoder.encode("Secret123"),
                OffsetDateTime.now()
        ));
    }

    @Test
    @Transactional
    void loginWithCorrectCredentialsReturnsToken() {
        var result = authService.login("login@example.com", "Secret123");
        assertThat(result.accessToken()).isNotBlank();
    }

    @Test
    void loginWithWrongPasswordThrows() {
        assertThatThrownBy(() -> authService.login("login@example.com", "bad"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginWithUnknownEmailThrows() {
        assertThatThrownBy(() -> authService.login("unknown@example.com", "Secret123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
