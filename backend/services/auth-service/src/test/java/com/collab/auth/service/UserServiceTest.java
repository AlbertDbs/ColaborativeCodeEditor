package com.collab.auth.service;

import com.collab.auth.domain.UserRepository;
import com.collab.auth.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void registerPersistsUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest("alice@example.com", "password123");

        var response = userService.register(request);

        var persisted = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(persisted.getId()).isEqualTo(response.id());
        assertThat(persisted.getPasswordHash()).isNotEqualTo(request.password());
    }

    @Test
    void registerSameEmailThrowsConflict() {
        RegisterRequest request = new RegisterRequest("bob@example.com", "password123");
        userService.register(request);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }
}
