package com.collab.auth.service;

import com.collab.auth.domain.User;
import com.collab.auth.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class GoogleLoginServiceTest {

    @Autowired
    private GoogleLoginService googleLoginService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void createsUserAndReturnsTokenWhenTokenValid() {
        String token = googleLoginService.loginWithIdToken("valid-google-token");

        assertThat(token).isNotBlank();
        assertThat(userRepository.findByEmail("google.user@example.com")).isPresent();
    }

    @Test
    void returnsExistingUserDoesNotDuplicate() {
        userRepository.save(new User(UUID.randomUUID(), "google.user@example.com",
                passwordEncoder.encode("GOOGLE_LOGIN_PLACEHOLDER"), OffsetDateTime.now()));

        googleLoginService.loginWithIdToken("valid-google-token");

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void invalidTokenThrowsUnauthorized() {
        assertThatThrownBy(() -> googleLoginService.loginWithIdToken("invalid"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @TestConfiguration
    static class StubVerifierConfig {
        @Bean
        @Primary
        GoogleTokenVerifier googleTokenVerifier() {
            GoogleClientProperties props = new GoogleClientProperties();
            props.setClientId("test-google-client-id");
            return new GoogleTokenVerifier(props) {
                @Override
                public Optional<GoogleTokenPayload> verify(String idToken) {
                    if ("valid-google-token".equals(idToken)) {
                        return Optional.of(new GoogleTokenPayload("sub-123", "google.user@example.com", true));
                    }
                    return Optional.empty();
                }
            };
        }
    }
}
