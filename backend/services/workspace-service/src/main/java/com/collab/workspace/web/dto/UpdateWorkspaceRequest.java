package com.collab.workspace.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWorkspaceRequest(
        @NotBlank @Size(max = 120) String name
) {
}
