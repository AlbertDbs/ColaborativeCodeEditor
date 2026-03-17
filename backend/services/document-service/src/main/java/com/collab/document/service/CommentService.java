package com.collab.document.service;

import com.collab.document.domain.CommentMessage;
import com.collab.document.domain.CommentMessageRepository;
import com.collab.document.domain.CommentThread;
import com.collab.document.domain.CommentThreadRepository;
import com.collab.document.web.dto.CommentDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentThreadRepository threadRepository;
    private final CommentMessageRepository messageRepository;
    private final DocumentService documentService;
    private final DocumentEventBroadcaster broadcaster;

    public CommentService(CommentThreadRepository threadRepository,
                          CommentMessageRepository messageRepository,
                          DocumentService documentService,
                          DocumentEventBroadcaster broadcaster) {
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.documentService = documentService;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public CommentDtos.CommentThreadResponse createThread(UUID documentId, AuthPrincipal principal,
                                                          CommentDtos.CreateCommentThreadRequest request) {
        var doc = documentService.get(documentId);
        ensureAccess(doc.getWorkspaceId(), principal, doc.getOwnerId());

        OffsetDateTime now = OffsetDateTime.now();
        CommentThread thread = new CommentThread(
                UUID.randomUUID(),
                doc.getWorkspaceId(),
                doc.getId(),
                request.lineStart(),
                request.lineEnd(),
                CommentThread.Status.ACTIVE,
                principal.userId(),
                principal.email(),
                now,
                now
        );
        threadRepository.save(thread);

        CommentMessage msg = new CommentMessage(
                UUID.randomUUID(),
                thread.getId(),
                principal.userId(),
                principal.email(),
                request.body(),
                now
        );
        messageRepository.save(msg);

        broadcaster.commentsUpdated(doc.getId());
        return CommentDtos.CommentThreadResponse.from(thread, List.of(msg));
    }

    @Transactional(readOnly = true)
    public List<CommentDtos.CommentThreadResponse> list(UUID documentId, AuthPrincipal principal, CommentThread.Status status) {
        var doc = documentService.get(documentId);
        ensureAccess(doc.getWorkspaceId(), principal, doc.getOwnerId());

        List<CommentThread> threads = status == null
                ? threadRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                : threadRepository.findByDocumentIdAndStatusOrderByCreatedAtAsc(documentId, status);

        return threads.stream()
                .map(t -> CommentDtos.CommentThreadResponse.from(t, messageRepository.findByThreadIdOrderByCreatedAtAsc(t.getId())))
                .toList();
    }

    @Transactional
    public CommentDtos.CommentMessageResponse addMessage(UUID threadId, AuthPrincipal principal, CommentDtos.CreateCommentMessageRequest request) {
        CommentThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        var doc = documentService.get(thread.getDocumentId());
        ensureAccess(doc.getWorkspaceId(), principal, doc.getOwnerId());

        CommentMessage msg = new CommentMessage(
                UUID.randomUUID(),
                threadId,
                principal.userId(),
                principal.email(),
                request.body(),
                OffsetDateTime.now()
        );
        CommentMessage saved = messageRepository.save(msg);
        broadcaster.commentsUpdated(doc.getId());
        return CommentDtos.CommentMessageResponse.from(saved);
    }

    @Transactional
    public void resolve(UUID threadId, AuthPrincipal principal) {
        changeStatus(threadId, principal, CommentThread.Status.RESOLVED);
    }

    @Transactional
    public void reopen(UUID threadId, AuthPrincipal principal) {
        changeStatus(threadId, principal, CommentThread.Status.ACTIVE);
    }

    private void changeStatus(UUID threadId, AuthPrincipal principal, CommentThread.Status status) {
        CommentThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        var doc = documentService.get(thread.getDocumentId());
        ensureAccess(doc.getWorkspaceId(), principal, doc.getOwnerId());

        if (status == CommentThread.Status.RESOLVED) {
            thread.resolve();
        } else {
            thread.reopen();
        }
        threadRepository.save(thread);
        broadcaster.commentsUpdated(doc.getId());
    }

    private void ensureAccess(UUID workspaceId, AuthPrincipal principal, UUID ownerId) {
        if (ownerId.equals(principal.userId())) return;
        if (!documentService.canAccessWorkspace(workspaceId, principal)) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed in workspace");
        }
    }
}
