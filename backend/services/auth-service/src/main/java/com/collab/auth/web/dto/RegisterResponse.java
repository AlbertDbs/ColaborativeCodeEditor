package com.collab.auth.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String email,
        OffsetDateTime createdAt
) {
}
