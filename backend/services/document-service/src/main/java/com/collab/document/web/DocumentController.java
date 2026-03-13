package com.collab.document.web;

import com.collab.document.service.DocumentService;
import com.collab.document.service.AuthPrincipal;
import com.collab.document.web.dto.CreateDocumentRequest;
import com.collab.document.web.dto.DocumentResponse;
import com.collab.document.web.dto.UpdateDocumentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> create(Authentication auth,
                                                   @Valid @RequestBody CreateDocumentRequest request) {
        AuthPrincipal principal = principal(auth);
        var doc = service.create(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.from(doc));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> get(@PathVariable UUID id) {
        var doc = service.get(id);
        return ResponseEntity.ok(DocumentResponse.from(doc));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> listByWorkspace(@RequestParam UUID workspaceId) {
        var docs = service.listByWorkspace(workspaceId).stream().map(DocumentResponse::from).toList();
        return ResponseEntity.ok(docs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> update(@PathVariable UUID id,
                                                   Authentication auth,
                                                   @Valid @RequestBody UpdateDocumentRequest request) {
        AuthPrincipal principal = principal(auth);
        var doc = service.update(id, principal, request);
        return ResponseEntity.ok(DocumentResponse.from(doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        AuthPrincipal principal = principal(auth);
        service.delete(id, principal);
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
