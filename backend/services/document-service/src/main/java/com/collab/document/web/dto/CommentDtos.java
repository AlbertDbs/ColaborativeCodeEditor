package com.collab.document.web.dto;

import com.collab.document.domain.CommentMessage;
import com.collab.document.domain.CommentThread;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class CommentDtos {

    public record CreateCommentThreadRequest(int lineStart, int lineEnd, String body) { }
    public record CreateCommentMessageRequest(String body) { }

    public record CommentMessageResponse(UUID id, UUID authorId, String authorEmail, String body, OffsetDateTime createdAt) {
        public static CommentMessageResponse from(CommentMessage msg) {
            return new CommentMessageResponse(msg.getId(), msg.getAuthorId(), msg.getAuthorEmail(), msg.getBody(), msg.getCreatedAt());
        }
    }

    public record CommentThreadResponse(UUID id,
                                        UUID documentId,
                                        UUID workspaceId,
                                        int lineStart,
                                        int lineEnd,
                                        String status,
                                        UUID createdById,
                                        String createdByEmail,
                                        OffsetDateTime createdAt,
                                        OffsetDateTime updatedAt,
                                        List<CommentMessageResponse> messages) {
        public static CommentThreadResponse from(CommentThread t, List<CommentMessage> msgs) {
            return new CommentThreadResponse(
                    t.getId(),
                    t.getDocumentId(),
                    t.getWorkspaceId(),
                    t.getLineStart(),
                    t.getLineEnd(),
                    t.getStatus().name(),
                    t.getCreatedById(),
                    t.getCreatedByEmail(),
                    t.getCreatedAt(),
                    t.getUpdatedAt(),
                    msgs.stream().map(CommentMessageResponse::from).toList()
            );
        }
    }
}
