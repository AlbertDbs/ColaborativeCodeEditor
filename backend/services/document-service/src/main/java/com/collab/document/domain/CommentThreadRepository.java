package com.collab.document.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentThreadRepository extends JpaRepository<CommentThread, UUID> {
    List<CommentThread> findByDocumentIdAndStatusOrderByCreatedAtAsc(UUID documentId, CommentThread.Status status);
    List<CommentThread> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
