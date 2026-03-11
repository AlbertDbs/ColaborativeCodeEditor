package com.collab.document.service;

import com.collab.document.domain.DocumentRepository;
import com.collab.document.web.dto.CreateDocumentRequest;
import com.collab.document.web.dto.UpdateDocumentRequest;
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
class DocumentServiceTest {

    @Autowired
    private DocumentService service;
    @Autowired
    private DocumentRepository repository;

    private UUID ownerId;
    private UUID workspaceId;

    @BeforeEach
    void clean() {
        repository.deleteAll();
        ownerId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
    }

    @Test
    @Transactional
    void createPersistsDocumentWithVersion1() {
        var doc = service.create(ownerId, new CreateDocumentRequest(workspaceId, "Doc", "content"));
        assertThat(doc.getVersion()).isEqualTo(1);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void updateIncrementsVersion() {
        var doc = service.create(ownerId, new CreateDocumentRequest(workspaceId, "Doc", "content"));
        var updated = service.update(doc.getId(), ownerId, new UpdateDocumentRequest("Doc2", "new content"));
        assertThat(updated.getVersion()).isEqualTo(2);
    }

    @Test
    void updateByNonOwnerDenied() {
        var doc = service.create(ownerId, new CreateDocumentRequest(workspaceId, "Doc", "content"));
        assertThatThrownBy(() -> service.update(doc.getId(), UUID.randomUUID(),
                new UpdateDocumentRequest("Doc2", "new content")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
