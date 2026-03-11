package com.collab.workspace.service;

import com.collab.workspace.domain.WorkspaceRepository;
import com.collab.workspace.web.dto.CreateWorkspaceRequest;
import com.collab.workspace.web.dto.UpdateWorkspaceRequest;
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
class WorkspaceServiceTest {

    @Autowired
    private WorkspaceService service;
    @Autowired
    private WorkspaceRepository repository;

    private UUID ownerId;

    @BeforeEach
    void clean() {
        repository.deleteAll();
        ownerId = UUID.randomUUID();
    }

    @Test
    @Transactional
    void createAddsWorkspaceForOwner() {
        var ws = service.create(ownerId, new CreateWorkspaceRequest("My WS"));
        assertThat(ws.getOwnerId()).isEqualTo(ownerId);
        assertThat(repository.findByOwnerId(ownerId)).hasSize(1);
    }

    @Test
    void updateNameChecksOwner() {
        var ws = service.create(ownerId, new CreateWorkspaceRequest("Old"));
        UUID other = UUID.randomUUID();
        assertThatThrownBy(() -> service.updateName(ws.getId(), other, new UpdateWorkspaceRequest("New")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
