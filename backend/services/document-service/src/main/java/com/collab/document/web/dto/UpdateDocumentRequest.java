package com.collab.document.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDocumentRequest(
        @NotNull @Size(max = 255) String title,
        @NotNull String content
) {
}
