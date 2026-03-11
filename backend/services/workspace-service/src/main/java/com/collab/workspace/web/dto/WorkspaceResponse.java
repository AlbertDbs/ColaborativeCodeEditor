package com.collab.workspace.web.dto;

import com.collab.workspace.domain.Workspace;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        UUID ownerId,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getOwnerId(),
                workspace.getName(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }
}
