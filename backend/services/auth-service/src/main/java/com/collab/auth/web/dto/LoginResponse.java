package com.collab.auth.web.dto;

public record LoginResponse(
        String accessToken,
        long expiresInSeconds,
        String tokenType
) {
    public static LoginResponse bearer(String token, long expiresInSeconds) {
        return new LoginResponse(token, expiresInSeconds, "Bearer");
    }
}
