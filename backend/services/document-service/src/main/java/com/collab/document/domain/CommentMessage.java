package com.collab.document.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comment_messages")
public class CommentMessage {

    @Id
    private UUID id;

    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected CommentMessage() {
    }

    public CommentMessage(UUID id, UUID threadId, UUID authorId, String authorEmail, String body, OffsetDateTime createdAt) {
        this.id = id;
        this.threadId = threadId;
        this.authorId = authorId;
        this.authorEmail = authorEmail;
        this.body = body;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getThreadId() { return threadId; }
    public UUID getAuthorId() { return authorId; }
    public String getAuthorEmail() { return authorEmail; }
    public String getBody() { return body; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
