package com.collab.document.web;

import com.collab.document.web.dto.CreateDocumentRequest;
import com.collab.document.web.dto.UpdateDocumentRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private UUID ownerId;
    private UUID workspaceId;
    private String bearer;

    @BeforeEach
    void setup() {
        ownerId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
        bearer = "Bearer " + generateToken(ownerId);
    }

    @Test
    void createAndGetDocument() throws Exception {
        CreateDocumentRequest req = new CreateDocumentRequest(workspaceId, "Title", "body");
        var create = mockMvc.perform(post("/documents")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Title")))
                .andReturn();

        String id = objectMapper.readTree(create.getResponse().getContentAsString()).get("id").asText();
        mockMvc.perform(get("/documents/" + id).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("body")));
    }

    @Test
    void updateBumpsVersion() throws Exception {
        CreateDocumentRequest req = new CreateDocumentRequest(workspaceId, "Doc", "v1");
        var create = mockMvc.perform(post("/documents")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        String id = objectMapper.readTree(create.getResponse().getContentAsString()).get("id").asText();

        UpdateDocumentRequest upd = new UpdateDocumentRequest("Doc2", "v2");
        mockMvc.perform(put("/documents/" + id)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"version\":2")));
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
