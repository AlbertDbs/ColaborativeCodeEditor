package com.collab.gateway.gateway;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureWebTestClient
class GatewayRoutesTest {

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void overrideRoutes(DynamicPropertyRegistry registry) {
        registry.add("collab.routes.auth", () -> "forward:/__stub/auth");
        registry.add("collab.routes.user", () -> "forward:/__stub/user");
    }

    @Test
    void authRouteStripsPrefixAndForwards() {
        client.get()
                .uri("/auth/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").isEqualTo("auth")
                .jsonPath("$.path").value(path -> path.toString().endsWith("/ping"));
    }

    @Test
    void userRouteStripsPrefixAndForwards() {
        client.get()
                .uri("/users/profile")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").isEqualTo("user")
                .jsonPath("$.path").value(path -> path.toString().endsWith("/profile"));
    }

    /**
     * Stub controller lives only in test context; it simulates downstream services.
     */
    @RestController
    static class StubController {

        @GetMapping("/__stub/{service}/**")
        public Map<String, String> echo(@PathVariable String service, org.springframework.http.server.reactive.ServerHttpRequest request) {
            return Map.of(
                    "service", service,
                    "path", request.getPath().pathWithinApplication().value()
            );
        }
    }
}
