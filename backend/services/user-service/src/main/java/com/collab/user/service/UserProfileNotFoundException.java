package com.collab.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(UUID id) {
        super("User profile not found for id: " + id);
    }

    public UserProfileNotFoundException(String email) {
        super("User profile not found for email: " + email);
    }
}
