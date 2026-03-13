package com.collab.invite.web;

import com.collab.invite.config.InternalApiProperties;
import com.collab.invite.domain.InvitationStatus;
import com.collab.invite.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/invitations")
public class InternalInvitationController {

    private final InvitationService service;
    private final InternalApiProperties props;

    public InternalInvitationController(InvitationService service, InternalApiProperties props) {
        this.service = service;
        this.props = props;
    }

    @GetMapping("/accepted")
    public ResponseEntity<Map<String, Boolean>> accepted(@RequestHeader("X-Internal-Token") String token,
                                                         @RequestParam UUID workspaceId,
                                                         @RequestParam String email) {
        if (!props.getApiKey().equals(token)) {
            return ResponseEntity.status(403).build();
        }
        boolean accepted = service.isAccepted(workspaceId, email, InvitationStatus.ACCEPTED);
        return ResponseEntity.ok(Map.of("accepted", accepted));
    }
}
