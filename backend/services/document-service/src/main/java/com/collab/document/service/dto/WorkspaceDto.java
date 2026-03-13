package com.collab.document.service.dto;

import java.util.UUID;

public record WorkspaceDto(UUID id, UUID ownerId, String name) {
}
