package com.collab.user.web;

import com.collab.user.domain.UserProfile;
import com.collab.user.domain.UserProfileRepository;
import com.collab.user.web.dto.UpdateDisplayNameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserProfileRepository repository;

    private UUID userId;
    private String bearer;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        userId = UUID.randomUUID();
        repository.save(new UserProfile(userId, "profile@example.com", null, OffsetDateTime.now(), OffsetDateTime.now()));
        bearer = "Bearer " + generateToken(userId);
    }

    @Test
    void meReturnsProfileWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("profile@example.com")));
    }

    @Test
    void updateDisplayNameWorks() throws Exception {
        UpdateDisplayNameRequest req = new UpdateDisplayNameRequest("New Name");

        mockMvc.perform(patch("/users/me/display-name")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New Name")));
    }

    @Test
    void createOrGetCreatesProfile() throws Exception {
        UUID newId = UUID.randomUUID();
        String body = """
                {"id":"%s","email":"new@example.com"}
                """.formatted(newId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("new@example.com")));
    }

    private String generateToken(UUID uid) {
        byte[] key = "test-secret-should-be-at-least-32-bytes-long!".getBytes(StandardCharsets.UTF_8);
        return Jwts.builder()
                .setSubject("profile@example.com")
                .claim("uid", uid.toString())
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }
}
