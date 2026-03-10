package com.collab.auth.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google")
public class GoogleClientProperties {
    /**
     * OAuth client ID (audience) expected in Google ID tokens.
     */
    private String clientId = "dummy-client-id";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
