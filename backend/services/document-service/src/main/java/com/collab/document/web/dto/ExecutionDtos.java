package com.collab.document.web.dto;

import jakarta.validation.constraints.NotBlank;

public class ExecutionDtos {

    public record ExecutionRequest(
            @NotBlank String language,
            @NotBlank String sourceCode,
            String stdin
    ) {
    }

    public record ExecutionResponse(
            String status,
            String stdout,
            String stderr,
            String compileOutput,
            String executionTime,
            String memory,
            String language
    ) {
    }
}
