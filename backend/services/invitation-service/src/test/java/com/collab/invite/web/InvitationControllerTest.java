package com.collab.invite.web;

import com.collab.invite.web.dto.CreateInvitationRequest;
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
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private UUID inviterId;
    private String bearer;

    @BeforeEach
    void setup() {
        inviterId = UUID.randomUUID();
        bearer = "Bearer " + generateToken(inviterId);
    }

    @Test
    void createAndAcceptInvitation() throws Exception {
        CreateInvitationRequest req = new CreateInvitationRequest(UUID.randomUUID(), "guest@example.com");
        var create = mockMvc.perform(post("/invitations")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("guest@example.com")))
                .andReturn();
        String id = objectMapper.readTree(create.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/invitations/" + id + "/accept")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ACCEPTED")));
    }

    @Test
    void listReturnsCreatedInvitation() throws Exception {
        CreateInvitationRequest req = new CreateInvitationRequest(UUID.randomUUID(), "list@example.com");
        mockMvc.perform(post("/invitations")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/invitations").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("list@example.com")));
    }

    private String generateToken(UUID uid) {
        byte[] key = "test-secret-should-be-at-least-32-bytes-long!".getBytes(StandardCharsets.UTF_8);
        return Jwts.builder()
                .setSubject("inviter@example.com")
                .claim("uid", uid.toString())
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }
}
