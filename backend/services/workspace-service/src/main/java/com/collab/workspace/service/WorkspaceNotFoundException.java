package com.collab.workspace.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WorkspaceNotFoundException extends RuntimeException {
    public WorkspaceNotFoundException(UUID id) {
        super("Workspace not found: " + id);
    }
}
