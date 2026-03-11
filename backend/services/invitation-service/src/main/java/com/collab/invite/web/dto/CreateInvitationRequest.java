package com.collab.invite.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInvitationRequest(
        @NotNull UUID workspaceId,
        @Email @NotBlank String inviteeEmail
) {
}
