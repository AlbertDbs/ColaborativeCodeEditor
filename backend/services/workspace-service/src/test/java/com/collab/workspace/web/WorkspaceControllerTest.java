package com.collab.workspace.web;

import com.collab.workspace.web.dto.CreateWorkspaceRequest;
import com.collab.workspace.web.dto.UpdateWorkspaceRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private UUID ownerId;
    private String bearer;

    @BeforeEach
    void setup() {
        ownerId = UUID.randomUUID();
        bearer = "Bearer " + generateToken(ownerId);
    }

    @Test
    void createAndListWorkspaces() throws Exception {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest("Workspace A");
        mockMvc.perform(post("/workspaces")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Workspace A")));

        mockMvc.perform(get("/workspaces").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Workspace A")));
    }

    @Test
    void updateWorkspaceName() throws Exception {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest("Old");
        var createResult = mockMvc.perform(post("/workspaces")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        UpdateWorkspaceRequest update = new UpdateWorkspaceRequest("New Name");
        mockMvc.perform(patch("/workspaces/" + id)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New Name")));
    }

    private String generateToken(UUID uid) {
        byte[] key = "test-secret-should-be-at-least-32-bytes-long!".getBytes(StandardCharsets.UTF_8);
        return Jwts.builder()
                .setSubject("owner@example.com")
                .claim("uid", uid.toString())
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }
}
