package com.collab.document.service;

import java.util.UUID;

public record AuthPrincipal(UUID userId, String email) {
}
