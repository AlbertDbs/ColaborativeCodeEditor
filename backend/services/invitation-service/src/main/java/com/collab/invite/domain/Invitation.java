package com.collab.invite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "workspace_owner_id", nullable = false)
    private UUID workspaceOwnerId;

    @Column(name = "inviter_id", nullable = false)
    private UUID inviterId;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "accepted_by")
    private UUID acceptedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Invitation() {
    }

    public Invitation(UUID id,
                      UUID workspaceId,
                      UUID workspaceOwnerId,
                      UUID inviterId,
                      String inviteeEmail,
                      InvitationStatus status,
                      UUID acceptedBy,
                      OffsetDateTime createdAt,
                      OffsetDateTime updatedAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.workspaceOwnerId = workspaceOwnerId;
        this.inviterId = inviterId;
        this.inviteeEmail = inviteeEmail;
        this.status = status;
        this.acceptedBy = acceptedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void touchUpdatedAt() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public UUID getWorkspaceOwnerId() { return workspaceOwnerId; }
    public UUID getInviterId() { return inviterId; }
    public String getInviteeEmail() { return inviteeEmail; }
    public InvitationStatus getStatus() { return status; }
    public UUID getAcceptedBy() { return acceptedBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void accept(UUID userId) {
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedBy = userId;
    }

    public void refuse(UUID userId) {
        this.status = InvitationStatus.REFUSED;
        this.acceptedBy = userId;
    }

    public boolean isPending() {
        return this.status == InvitationStatus.PENDING;
    }
}
