package com.collab.invite.web;

import com.collab.invite.domain.Invitation;
import com.collab.invite.service.InvitationService;
import com.collab.invite.web.dto.CreateInvitationRequest;
import com.collab.invite.web.dto.InvitationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService service;

    public InvitationController(InvitationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InvitationResponse> create(Authentication auth,
                                                     @Valid @RequestBody CreateInvitationRequest request) {
        UUID inviterId = userId(auth);
        Invitation invitation = service.create(inviterId, inviterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(InvitationResponse.from(invitation));
    }

    @GetMapping
    public ResponseEntity<List<InvitationResponse>> list(Authentication auth) {
        UUID inviterId = userId(auth);
        var list = service.listSentBy(inviterId).stream().map(InvitationResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<InvitationResponse> accept(Authentication auth, @PathVariable UUID id) {
        UUID userId = userId(auth);
        Invitation invitation = service.accept(id, userId);
        return ResponseEntity.ok(InvitationResponse.from(invitation));
    }

    @PostMapping("/{id}/refuse")
    public ResponseEntity<InvitationResponse> refuse(Authentication auth, @PathVariable UUID id) {
        UUID userId = userId(auth);
        Invitation invitation = service.refuse(id, userId);
        return ResponseEntity.ok(InvitationResponse.from(invitation));
    }

    private UUID userId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Missing token");
        }
        return UUID.fromString(auth.getPrincipal().toString());
    }
}
