package com.collab.document.service;

import com.collab.document.web.dto.DocumentResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class DocumentEventBroadcaster {

    private final Map<UUID, CopyOnWriteArraySet<SseEmitter>> emittersByDocument = new ConcurrentHashMap<>();

    public SseEmitter register(UUID documentId) {
        SseEmitter emitter = new SseEmitter(0L);
        emittersByDocument.computeIfAbsent(documentId, id -> new CopyOnWriteArraySet<>()).add(emitter);

        emitter.onCompletion(() -> remove(documentId, emitter));
        emitter.onTimeout(() -> remove(documentId, emitter));
        emitter.onError((ex) -> remove(documentId, emitter));

        return emitter;
    }

    public void documentChanged(DocumentResponse payload) {
        broadcast(payload.id(), "changed", payload);
    }

    public void documentDeleted(UUID documentId) {
        broadcast(documentId, "deleted", documentId);
    }

    private void broadcast(UUID documentId, String eventName, Object payload) {
        var emitters = emittersByDocument.get(documentId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        var dead = new ArrayList<SseEmitter>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        dead.forEach(em -> remove(documentId, em));
    }

    private void remove(UUID documentId, SseEmitter emitter) {
        var emitters = emittersByDocument.get(documentId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByDocument.remove(documentId);
        }
    }
}
