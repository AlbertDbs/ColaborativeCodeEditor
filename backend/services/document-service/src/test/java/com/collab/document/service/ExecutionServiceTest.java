package com.collab.document.service;

import com.collab.document.web.dto.ExecutionDtos;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionServiceTest {

    private final ExecutionService executionService = new ExecutionService(
            new RestTemplate(),
            "local",
            "http://localhost:2358",
            "",
            "",
            10,
            250
    );

    @Test
    void capturesPythonStdout() {
        ExecutionDtos.ExecutionResponse response = executionService.run(
                new ExecutionDtos.ExecutionRequest("python", "print(\"hi\")", "")
        );

        assertEquals("SUCCESS", response.status());
        assertTrue(response.stdout().contains("hi"));
    }

    @Test
    void passesStdinToPythonProgram() {
        ExecutionDtos.ExecutionResponse response = executionService.run(
                new ExecutionDtos.ExecutionRequest("python", "print(input())", "hello from stdin\n")
        );

        assertEquals("SUCCESS", response.status());
        assertTrue(response.stdout().contains("hello from stdin"));
    }
}
