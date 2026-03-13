package com.collab.document.service;

import com.collab.document.service.dto.InvitationAcceptedResponse;
import com.collab.document.service.dto.WorkspaceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipService.class);

    private final RestTemplate restTemplate;
    private final String workspaceServiceUrl;
    private final String invitationServiceUrl;
    private final String internalApiKey;

    public MembershipService(RestTemplate restTemplate,
                             @Value("${services.workspace.url:http://localhost:8083}") String workspaceServiceUrl,
                             @Value("${services.invitation.url:http://localhost:8084}") String invitationServiceUrl,
                             @Value("${internal.api-key:internal-secret}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.workspaceServiceUrl = workspaceServiceUrl;
        this.invitationServiceUrl = invitationServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    public boolean canWrite(UUID workspaceId, AuthPrincipal principal) {
        boolean owner = workspaceOwnerMatches(workspaceId, principal.userId());
        boolean invited = hasAcceptedInvitation(workspaceId, principal.email());
        if (!owner && !invited) {
            log.warn("Access denied for user {} email {} on workspace {} (owner={}, invited={})",
                    principal.userId(), principal.email(), workspaceId, owner, invited);
        }
        return owner || invited;
    }

    private boolean workspaceOwnerMatches(UUID workspaceId, UUID userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Internal-Token", internalApiKey);
            ResponseEntity<WorkspaceDto> resp = restTemplate.exchange(
                    workspaceServiceUrl + "/internal/workspaces/" + workspaceId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    WorkspaceDto.class
            );
            WorkspaceDto ws = resp.getBody();
            return ws != null && ws.ownerId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasAcceptedInvitation(UUID workspaceId, String email) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Internal-Token", internalApiKey);
            ResponseEntity<InvitationAcceptedResponse> resp = restTemplate.exchange(
                    invitationServiceUrl + "/internal/invitations/accepted?workspaceId=" + workspaceId + "&email=" + email,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    InvitationAcceptedResponse.class
            );
            return resp.getBody() != null && resp.getBody().accepted();
        } catch (Exception e) {
            return false;
        }
    }
}
