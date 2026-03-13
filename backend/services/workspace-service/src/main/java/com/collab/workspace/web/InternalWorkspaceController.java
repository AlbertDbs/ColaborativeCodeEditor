package com.collab.workspace.web;

import com.collab.workspace.config.InternalApiProperties;
import com.collab.workspace.domain.Workspace;
import com.collab.workspace.service.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/workspaces")
public class InternalWorkspaceController {

    private final WorkspaceService service;
    private final InternalApiProperties props;

    public InternalWorkspaceController(WorkspaceService service, InternalApiProperties props) {
        this.service = service;
        this.props = props;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInternal(@RequestHeader("X-Internal-Token") String token,
                                                           @PathVariable UUID id) {
        if (!props.getApiKey().equals(token)) {
            return ResponseEntity.status(403).build();
        }
        Workspace ws = service.get(id);
        return ResponseEntity.ok(Map.of(
                "id", ws.getId(),
                "ownerId", ws.getOwnerId(),
                "name", ws.getName()
        ));
    }
}
