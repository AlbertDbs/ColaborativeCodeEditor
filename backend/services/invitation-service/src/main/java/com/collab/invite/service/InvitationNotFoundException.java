package com.collab.invite.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException(UUID id) {
        super("Invitation not found: " + id);
    }
}
