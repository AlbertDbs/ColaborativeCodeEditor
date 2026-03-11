package com.collab.invite.web.dto;

import com.collab.invite.domain.Invitation;
import com.collab.invite.domain.InvitationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID workspaceId,
        UUID workspaceOwnerId,
        UUID inviterId,
        String inviteeEmail,
        InvitationStatus status,
        UUID acceptedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static InvitationResponse from(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getWorkspaceId(),
                invitation.getWorkspaceOwnerId(),
                invitation.getInviterId(),
                invitation.getInviteeEmail(),
                invitation.getStatus(),
                invitation.getAcceptedBy(),
                invitation.getCreatedAt(),
                invitation.getUpdatedAt()
        );
    }
}
