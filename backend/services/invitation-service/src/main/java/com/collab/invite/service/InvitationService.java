package com.collab.invite.service;

import com.collab.invite.domain.Invitation;
import com.collab.invite.domain.InvitationRepository;
import com.collab.invite.domain.InvitationStatus;
import com.collab.invite.web.dto.CreateInvitationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository repository;

    public InvitationService(InvitationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Invitation create(UUID inviterId, UUID workspaceOwnerId, CreateInvitationRequest request) {
        Invitation invitation = new Invitation(
                UUID.randomUUID(),
                request.workspaceId(),
                workspaceOwnerId,
                inviterId,
                request.inviteeEmail().toLowerCase(),
                InvitationStatus.PENDING,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        return repository.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<Invitation> listSentBy(UUID inviterId) {
        return repository.findByInviterId(inviterId);
    }

    @Transactional(readOnly = true)
    public List<Invitation> listReceivedBy(String inviteeEmail) {
        return repository.findByInviteeEmailIgnoreCase(inviteeEmail.toLowerCase());
    }

    @Transactional
    public Invitation accept(UUID invitationId, UUID userId, String userEmail) {
        Invitation invitation = repository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException(invitationId));
        if (!invitation.getInviteeEmail().equalsIgnoreCase(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Doar invitatul poate accepta");
        }
        if (!invitation.isPending()) {
            return invitation;
        }
        invitation.accept(userId);
        return repository.save(invitation);
    }

    @Transactional
    public Invitation refuse(UUID invitationId, UUID userId, String userEmail) {
        Invitation invitation = repository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException(invitationId));
        if (!invitation.getInviteeEmail().equalsIgnoreCase(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Doar invitatul poate refuza");
        }
        if (!invitation.isPending()) {
            return invitation;
        }
        invitation.refuse(userId);
        return repository.save(invitation);
    }
}
