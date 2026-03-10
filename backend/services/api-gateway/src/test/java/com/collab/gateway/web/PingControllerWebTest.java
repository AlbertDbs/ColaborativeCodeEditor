package com.collab.gateway.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class PingControllerWebTest {

    @Autowired
    private WebTestClient client;

    @Test
    void pingReturnsPong() {
        client.get()
                .uri("/api/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("pong");
    }

    @Test
    void pingRequiresApiPrefix() {
        client.get()
                .uri("/ping")
                .exchange()
                .expectStatus().isNotFound();
    }
}
