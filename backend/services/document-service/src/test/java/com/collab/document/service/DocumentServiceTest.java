package com.collab.document.service;

import com.collab.document.domain.DocumentRepository;
import com.collab.document.web.dto.CreateDocumentRequest;
import com.collab.document.web.dto.UpdateDocumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class DocumentServiceTest {

    @Autowired
    private DocumentService service;
    @Autowired
    private DocumentRepository repository;

    @MockBean
    private MembershipService membershipService;

    private AuthPrincipal owner;
    private UUID workspaceId;

    @BeforeEach
    void clean() {
        repository.deleteAll();
        owner = new AuthPrincipal(UUID.randomUUID(), "owner@example.com");
        workspaceId = UUID.randomUUID();
        when(membershipService.canWrite(workspaceId, owner)).thenReturn(true);
    }

    @Test
    @Transactional
    void createPersistsDocumentWithVersion1() {
        var doc = service.create(owner, new CreateDocumentRequest(workspaceId, "Doc", "content"));
        assertThat(doc.getVersion()).isEqualTo(1);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void updateIncrementsVersion() {
        var doc = service.create(owner, new CreateDocumentRequest(workspaceId, "Doc", "content"));
        var updated = service.update(doc.getId(), owner, new UpdateDocumentRequest("Doc2", "new content"));
        assertThat(updated.getVersion()).isEqualTo(2);
    }

    @Test
    void updateByNonOwnerDenied() {
        var doc = service.create(owner, new CreateDocumentRequest(workspaceId, "Doc", "content"));
        var other = new AuthPrincipal(UUID.randomUUID(), "other@example.com");
        when(membershipService.canWrite(workspaceId, other)).thenReturn(false);
        assertThatThrownBy(() -> service.update(doc.getId(), other,
                new UpdateDocumentRequest("Doc2", "new content")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
