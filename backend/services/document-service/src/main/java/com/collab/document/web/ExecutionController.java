package com.collab.document.web;

import com.collab.document.service.ExecutionService;
import com.collab.document.web.dto.ExecutionDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/executions")
    public ResponseEntity<ExecutionDtos.ExecutionResponse> run(@Valid @RequestBody ExecutionDtos.ExecutionRequest request) {
        var resp = executionService.run(request);
        return ResponseEntity.ok(resp);
    }
}
