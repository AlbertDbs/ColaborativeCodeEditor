package com.collab.document.service;

import com.collab.document.domain.Document;
import com.collab.document.domain.DocumentRepository;
import com.collab.document.web.dto.CreateDocumentRequest;
import com.collab.document.web.dto.UpdateDocumentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Document create(UUID ownerId, CreateDocumentRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        Document doc = new Document(
                UUID.randomUUID(),
                request.workspaceId(),
                ownerId,
                request.title(),
                request.content(),
                1,
                now,
                now
        );
        return repository.save(doc);
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
    public Document update(UUID id, UUID ownerId, UpdateDocumentRequest request) {
        Document doc = get(id);
        if (!doc.getOwnerId().equals(ownerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Only owner can update");
        }
        doc.update(request.title(), request.content());
        return repository.save(doc);
    }
}
