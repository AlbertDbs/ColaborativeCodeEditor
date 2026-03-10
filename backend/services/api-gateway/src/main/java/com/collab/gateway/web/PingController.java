package com.collab.gateway.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("pong");
    }
}
