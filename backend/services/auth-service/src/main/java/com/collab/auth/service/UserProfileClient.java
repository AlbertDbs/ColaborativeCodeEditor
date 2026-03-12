package com.collab.auth.service;

import com.collab.auth.config.UserServiceProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class UserProfileClient {

    private final RestClient restClient;
    private final UserServiceProperties properties;

    public UserProfileClient(RestClient restClient, UserServiceProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public void createProfile(UUID id, String email) {
        restClient.post()
                .uri(properties.getBaseUrl() + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserProfilePayload(id, email))
                .retrieve()
                .toBodilessEntity();
    }

    private record UserProfilePayload(UUID id, String email) {}
}
