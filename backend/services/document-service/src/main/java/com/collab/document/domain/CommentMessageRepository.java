package com.collab.document.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentMessageRepository extends JpaRepository<CommentMessage, UUID> {
    List<CommentMessage> findByThreadIdOrderByCreatedAtAsc(UUID threadId);
}
