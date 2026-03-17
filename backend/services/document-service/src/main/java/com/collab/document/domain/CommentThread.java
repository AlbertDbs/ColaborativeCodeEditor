package com.collab.document.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comment_threads")
public class CommentThread {

    public enum Status {
        ACTIVE,
        RESOLVED
    }

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "line_start", nullable = false)
    private int lineStart;

    @Column(name = "line_end", nullable = false)
    private int lineEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column(name = "created_by_email", nullable = false)
    private String createdByEmail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CommentThread() {
    }

    public CommentThread(UUID id, UUID workspaceId, UUID documentId, int lineStart, int lineEnd,
                         Status status, UUID createdById, String createdByEmail,
                         OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.documentId = documentId;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.status = status;
        this.createdById = createdById;
        this.createdByEmail = createdByEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public UUID getDocumentId() { return documentId; }
    public int getLineStart() { return lineStart; }
    public int getLineEnd() { return lineEnd; }
    public Status getStatus() { return status; }
    public UUID getCreatedById() { return createdById; }
    public String getCreatedByEmail() { return createdByEmail; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void resolve() {
        this.status = Status.RESOLVED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void reopen() {
        this.status = Status.ACTIVE;
        this.updatedAt = OffsetDateTime.now();
    }
}
