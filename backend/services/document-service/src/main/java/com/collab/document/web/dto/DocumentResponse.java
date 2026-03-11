package com.collab.document.web.dto;

import com.collab.document.domain.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID workspaceId,
        UUID ownerId,
        String title,
        String content,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static DocumentResponse from(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getWorkspaceId(),
                doc.getOwnerId(),
                doc.getTitle(),
                doc.getContent(),
                doc.getVersion(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
