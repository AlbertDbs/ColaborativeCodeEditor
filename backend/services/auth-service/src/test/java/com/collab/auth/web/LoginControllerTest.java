package com.collab.auth.web;

import com.collab.auth.domain.User;
import com.collab.auth.domain.UserRepository;
import com.collab.auth.web.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void seedUser() throws Exception {
        userRepository.deleteAll();
        userRepository.save(new User(
                UUID.randomUUID(),
                "apiuser@example.com",
                passwordEncoder.encode("Secret123"),
                OffsetDateTime.now()
        ));
        var request = new LoginRequest("apiuser@example.com", "Secret123");
        var mvcResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andReturn();
        this.token = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void loginReturnsBearerToken() throws Exception {
        // token was already verified in setup; nothing additional needed here
    }

    @Test
    void loginWithBadPasswordReturns401() throws Exception {
        LoginRequest bad = new LoginRequest("apiuser@example.com", "wrong");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpointRequiresValidToken() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("userId")));
    }
}
