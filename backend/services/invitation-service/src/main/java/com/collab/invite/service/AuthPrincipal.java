package com.collab.invite.service;

import java.util.UUID;

public record AuthPrincipal(UUID userId, String email) {
}
