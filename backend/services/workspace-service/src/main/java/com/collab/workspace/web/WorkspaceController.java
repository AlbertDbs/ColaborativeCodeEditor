package com.collab.workspace.web;

import com.collab.workspace.service.WorkspaceService;
import com.collab.workspace.web.dto.CreateWorkspaceRequest;
import com.collab.workspace.web.dto.UpdateWorkspaceRequest;
import com.collab.workspace.web.dto.WorkspaceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService service;

    public WorkspaceController(WorkspaceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(Authentication auth, @Valid @RequestBody CreateWorkspaceRequest request) {
        UUID ownerId = userId(auth);
        var ws = service.create(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(WorkspaceResponse.from(ws));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> list(Authentication auth) {
        UUID ownerId = userId(auth);
        var list = service.listForOwner(ownerId).stream().map(WorkspaceResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> get(@PathVariable UUID id) {
        var ws = service.get(id);
        return ResponseEntity.ok(WorkspaceResponse.from(ws));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> update(@PathVariable UUID id,
                                                    Authentication auth,
                                                    @Valid @RequestBody UpdateWorkspaceRequest request) {
        UUID ownerId = userId(auth);
        var ws = service.updateName(id, ownerId, request);
        return ResponseEntity.ok(WorkspaceResponse.from(ws));
    }

    private UUID userId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Missing token");
        }
        return UUID.fromString(auth.getPrincipal().toString());
    }
}
