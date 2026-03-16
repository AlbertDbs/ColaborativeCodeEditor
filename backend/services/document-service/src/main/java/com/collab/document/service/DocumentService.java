package com.collab.document.service;

import com.collab.document.domain.Document;
import com.collab.document.domain.DocumentRepository;
import com.collab.document.web.dto.CreateDocumentRequest;
import com.collab.document.web.dto.DocumentResponse;
import com.collab.document.web.dto.UpdateDocumentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final MembershipService membershipService;
    private final DocumentEventBroadcaster broadcaster;

    public DocumentService(DocumentRepository repository,
                           MembershipService membershipService,
                           DocumentEventBroadcaster broadcaster) {
        this.repository = repository;
        this.membershipService = membershipService;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public Document create(AuthPrincipal principal, CreateDocumentRequest request) {
        if (!membershipService.canWrite(request.workspaceId(), principal)) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed in workspace");
        }
        OffsetDateTime now = OffsetDateTime.now();
        Document doc = new Document(
                UUID.randomUUID(),
                request.workspaceId(),
                principal.userId(),
                request.title(),
                request.content() == null ? "" : request.content(),
                1,
                principal.userId(),
                principal.email(),
                now,
                now
        );
        Document saved = repository.save(doc);
        broadcaster.documentChanged(DocumentResponse.from(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public Document get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Document> listByWorkspace(UUID workspaceId) {
        return repository.findByWorkspaceId(workspaceId);
    }

    @Transactional
    public Document update(UUID id, AuthPrincipal principal, UpdateDocumentRequest request) {
        Document doc = get(id);
        if (doc.getOwnerId().equals(principal.userId()) || membershipService.canWrite(doc.getWorkspaceId(), principal)) {
            doc.update(request.title(), request.content(), principal.userId(), principal.email());
            Document saved = repository.save(doc);
            broadcaster.documentChanged(DocumentResponse.from(saved));
            return saved;
        }
        throw new org.springframework.security.access.AccessDeniedException("Not allowed to update");
    }

    @Transactional
    public void delete(UUID id, AuthPrincipal principal) {
        Document doc = get(id);
        if (doc.getOwnerId().equals(principal.userId()) || membershipService.canWrite(doc.getWorkspaceId(), principal)) {
            repository.delete(doc);
            broadcaster.documentDeleted(id);
            return;
        }
        throw new org.springframework.security.access.AccessDeniedException("Not allowed to delete");
    }

    public boolean canAccess(Document doc, AuthPrincipal principal) {
        return doc.getOwnerId().equals(principal.userId()) || membershipService.canWrite(doc.getWorkspaceId(), principal);
    }
}
