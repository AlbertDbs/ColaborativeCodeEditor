package com.collab.document.web;

import com.collab.document.domain.CommentThread;
import com.collab.document.service.AuthPrincipal;
import com.collab.document.service.CommentService;
import com.collab.document.web.dto.CommentDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/documents/{documentId}/comments/threads")
    public ResponseEntity<CommentDtos.CommentThreadResponse> createThread(Authentication auth,
                                                                          @PathVariable UUID documentId,
                                                                          @Valid @RequestBody CommentDtos.CreateCommentThreadRequest request) {
        AuthPrincipal principal = principal(auth);
        var resp = commentService.createThread(documentId, principal, request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/documents/{documentId}/comments/threads")
    public ResponseEntity<List<CommentDtos.CommentThreadResponse>> listThreads(Authentication auth,
                                                                               @PathVariable UUID documentId,
                                                                               @RequestParam(required = false) String status) {
        AuthPrincipal principal = principal(auth);
        CommentThread.Status st = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            st = CommentThread.Status.valueOf(status.toUpperCase());
        } else if (status == null || status.isBlank()) {
            st = CommentThread.Status.ACTIVE;
        }
        var resp = commentService.list(documentId, principal, st);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/comments/threads/{threadId}/messages")
    public ResponseEntity<CommentDtos.CommentMessageResponse> reply(Authentication auth,
                                                                    @PathVariable UUID threadId,
                                                                    @Valid @RequestBody CommentDtos.CreateCommentMessageRequest request) {
        AuthPrincipal principal = principal(auth);
        var resp = commentService.addMessage(threadId, principal, request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/comments/threads/{threadId}/resolve")
    public ResponseEntity<Void> resolve(Authentication auth, @PathVariable UUID threadId) {
        AuthPrincipal principal = principal(auth);
        commentService.resolve(threadId, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/threads/{threadId}/reopen")
    public ResponseEntity<Void> reopen(Authentication auth, @PathVariable UUID threadId) {
        AuthPrincipal principal = principal(auth);
        commentService.reopen(threadId, principal);
        return ResponseEntity.noContent().build();
    }

    private AuthPrincipal principal(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Missing token");
        }
        Object p = auth.getPrincipal();
        if (p instanceof AuthPrincipal ap) {
            return ap;
        }
        throw new org.springframework.security.access.AccessDeniedException("Invalid token");
    }
}
