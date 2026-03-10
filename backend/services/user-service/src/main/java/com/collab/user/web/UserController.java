package com.collab.user.web;

import com.collab.user.service.UserProfileService;
import com.collab.user.web.dto.CreateUserRequest;
import com.collab.user.web.dto.UpdateDisplayNameRequest;
import com.collab.user.web.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserProfileService service;

    public UserController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> createOrGet(@Valid @RequestBody CreateUserRequest request) {
        var profile = service.upsert(request.id(), request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserProfileResponse.from(profile));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        UUID userId = requireUserId(authentication);
        var profile = service.findById(userId);
        return ResponseEntity.ok(UserProfileResponse.from(profile));
    }

    @PatchMapping("/me/display-name")
    public ResponseEntity<UserProfileResponse> updateDisplayName(Authentication authentication,
                                                                  @Valid @RequestBody UpdateDisplayNameRequest request) {
        UUID userId = requireUserId(authentication);
        var profile = service.updateDisplayName(userId, request);
        return ResponseEntity.ok(UserProfileResponse.from(profile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getById(@PathVariable UUID id) {
        var profile = service.findById(id);
        return ResponseEntity.ok(UserProfileResponse.from(profile));
    }

    private UUID requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Missing authentication");
        }
        return UUID.fromString(authentication.getPrincipal().toString());
    }
}
