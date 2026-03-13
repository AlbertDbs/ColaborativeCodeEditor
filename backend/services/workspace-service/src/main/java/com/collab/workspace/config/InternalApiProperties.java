package com.collab.workspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "internal")
public class InternalApiProperties {
    /** token partajat pentru apeluri interne */
    private String apiKey = "internal-secret";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
