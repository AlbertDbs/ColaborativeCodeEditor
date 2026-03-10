package com.collab.user.service;

import com.collab.user.domain.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class UserProfileServiceTest {

    @Autowired
    private UserProfileService service;
    @Autowired
    private UserProfileRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void upsertCreatesProfile() {
        UUID id = UUID.randomUUID();
        var profile = service.upsert(id, "user@example.com");

        assertThat(profile.getId()).isEqualTo(id);
        assertThat(repository.findByEmail("user@example.com")).isPresent();
    }

    @Test
    void findByEmailThrowsWhenMissing() {
        assertThatThrownBy(() -> service.findByEmail("missing@example.com"))
                .isInstanceOf(UserProfileNotFoundException.class);
    }
}
