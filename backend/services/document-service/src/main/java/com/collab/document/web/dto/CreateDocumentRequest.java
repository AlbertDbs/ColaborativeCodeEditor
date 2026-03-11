package com.collab.document.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDocumentRequest(
        @NotNull UUID workspaceId,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String content
) {
}
