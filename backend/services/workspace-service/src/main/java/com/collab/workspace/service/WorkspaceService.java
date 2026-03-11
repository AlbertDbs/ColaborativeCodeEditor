package com.collab.workspace.service;

import com.collab.workspace.domain.Workspace;
import com.collab.workspace.domain.WorkspaceRepository;
import com.collab.workspace.web.dto.CreateWorkspaceRequest;
import com.collab.workspace.web.dto.UpdateWorkspaceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceService {

    private final WorkspaceRepository repository;

    public WorkspaceService(WorkspaceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Workspace create(UUID ownerId, CreateWorkspaceRequest request) {
        Workspace workspace = new Workspace(
                UUID.randomUUID(),
                ownerId,
                request.name(),
                OffsetDateTime.now(),
                OffsetDateTime.now());
        return repository.save(workspace);
    }

    @Transactional(readOnly = true)
    public Workspace get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new WorkspaceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Workspace> listForOwner(UUID ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    @Transactional
    public Workspace updateName(UUID id, UUID ownerId, UpdateWorkspaceRequest request) {
        Workspace workspace = get(id);
        if (!workspace.getOwnerId().equals(ownerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Only owner can update workspace");
        }
        workspace.setName(request.name());
        return repository.save(workspace);
    }
}
