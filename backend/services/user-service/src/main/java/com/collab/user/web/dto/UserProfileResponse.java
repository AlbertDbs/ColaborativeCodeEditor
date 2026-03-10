package com.collab.user.web.dto;

import com.collab.user.domain.UserProfile;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String displayName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getEmail(),
                profile.getDisplayName(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
