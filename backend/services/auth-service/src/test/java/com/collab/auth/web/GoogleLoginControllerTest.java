package com.collab.auth.web;

import com.collab.auth.service.GoogleClientProperties;
import com.collab.auth.service.GoogleTokenVerifier;
import com.collab.auth.web.dto.GoogleLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // nothing to seed; the service will create user on first login
    }

    @Test
    void googleLoginReturnsToken() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest("valid-google-token");

        mockMvc.perform(post("/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")));
    }

    @Test
    void googleLoginWithInvalidTokenReturns401() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest("invalid-token");

        mockMvc.perform(post("/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
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
                        return Optional.of(new GoogleTokenPayload("sub-xyz", "controller.user@example.com", true));
                    }
                    return Optional.empty();
                }
            };
        }
    }
}
