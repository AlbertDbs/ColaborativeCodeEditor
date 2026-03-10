package com.collab.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDisplayNameRequest(
        @NotBlank @Size(max = 100) String displayName
) {
}
