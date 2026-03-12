package com.collab.invite.service;

import com.collab.invite.domain.InvitationRepository;
import com.collab.invite.domain.InvitationStatus;
import com.collab.invite.web.dto.CreateInvitationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class InvitationServiceTest {

    @Autowired
    private InvitationService service;
    @Autowired
    private InvitationRepository repository;

    private UUID inviterId;
    private UUID workspaceId;

    @BeforeEach
    void clean() {
        repository.deleteAll();
        inviterId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
    }

    @Test
    @Transactional
    void createSavesPendingInvitation() {
        var inv = service.create(inviterId, inviterId, new CreateInvitationRequest(workspaceId, "bob@example.com"));
        assertThat(inv.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void acceptSetsStatusAndAcceptedBy() {
        var inv = service.create(inviterId, inviterId, new CreateInvitationRequest(workspaceId, "bob@example.com"));
        var accepted = service.accept(inv.getId(), UUID.randomUUID(), "bob@example.com");
        assertThat(accepted.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(accepted.getAcceptedBy()).isNotNull();
    }

    @Test
    void refuseSetsStatusAndAcceptedBy() {
        var inv = service.create(inviterId, inviterId, new CreateInvitationRequest(workspaceId, "bob@example.com"));
        var refused = service.refuse(inv.getId(), UUID.randomUUID(), "bob@example.com");
        assertThat(refused.getStatus()).isEqualTo(InvitationStatus.REFUSED);
    }

    @Test
    void acceptFailsWhenCallerIsNotInvitee() {
        var inv = service.create(inviterId, inviterId, new CreateInvitationRequest(workspaceId, "bob@example.com"));
        assertThatThrownBy(() -> service.accept(inv.getId(), UUID.randomUUID(), "alice@example.com"))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
