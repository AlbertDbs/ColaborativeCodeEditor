package com.collab.document.service;

import com.collab.document.web.dto.ExecutionDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);
    private static final Pattern JAVA_PUBLIC_CLASS = Pattern.compile("\\bpublic\\s+class\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern JAVA_CLASS = Pattern.compile("\\bclass\\s+([A-Za-z_][A-Za-z0-9_]*)");

    private final RestTemplate restTemplate;
    private final String provider;
    private final String judge0Url;
    private final String judge0Host;
    private final String judge0Key;
    private final Duration timeout;
    private final Duration judge0PollInterval;

    private static final Map<String, Integer> LANGUAGE_IDS = Map.of(
            "c", 50,
            "cpp", 54,
            "c++", 54,
            "java", 62,
            "python", 71,
            "py", 71,
            "python3", 71
    );

    public ExecutionService(RestTemplate restTemplate,
                            @Value("${execution.provider:local}") String provider,
                            @Value("${judge0.url:http://localhost:2358}") String judge0Url,
                            @Value("${judge0.host:}") String judge0Host,
                            @Value("${judge0.key:}") String judge0Key,
                            @Value("${execution.timeout-seconds:10}") long timeoutSeconds,
                            @Value("${judge0.poll-interval-ms:250}") long judge0PollIntervalMs) {
        this.restTemplate = restTemplate;
        this.provider = provider;
        this.judge0Url = judge0Url;
        this.judge0Host = judge0Host;
        this.judge0Key = judge0Key;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.judge0PollInterval = Duration.ofMillis(judge0PollIntervalMs);
    }

    public ExecutionDtos.ExecutionResponse run(ExecutionDtos.ExecutionRequest request) {
        String language = normalizeLanguage(request.language());
        Integer langId = LANGUAGE_IDS.get(language);
        if (langId == null) {
            throw new IllegalArgumentException("Unsupported language: " + request.language());
        }

        String normalizedProvider = provider.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedProvider) {
            case "judge0" -> runWithJudge0(request, language, langId);
            case "local" -> runLocally(request, language);
            default -> throw new IllegalArgumentException("Unsupported execution provider: " + provider);
        };
    }

    private ExecutionDtos.ExecutionResponse runWithJudge0(ExecutionDtos.ExecutionRequest request, String language, Integer langId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!judge0Host.isBlank()) headers.set("X-RapidAPI-Host", judge0Host);
        if (!judge0Key.isBlank()) headers.set("X-RapidAPI-Key", judge0Key);

        var payload = Map.of(
                "language_id", langId,
                "source_code", request.sourceCode(),
                "stdin", request.stdin() == null ? "" : request.stdin(),
                "redirect_stderr_to_stdout", false,
                "enable_per_process_and_thread_time_limit", true,
                "enable_per_process_and_thread_memory_limit", true
        );

        try {
            var entity = new HttpEntity<>(payload, headers);
            String submitUrl = judge0Url + "/submissions?base64_encoded=false";
            ResponseEntity<Map> submitResponse = restTemplate.exchange(submitUrl, HttpMethod.POST, entity, Map.class);
            Map submitBody = submitResponse.getBody();
            if (submitBody == null) {
                throw new RuntimeException("Empty submission response from Judge0");
            }

            String token = asString(submitBody.get("token"));
            if (token.isBlank()) {
                throw new RuntimeException("Judge0 did not return a submission token");
            }

            String resultUrl = judge0Url + "/submissions/" + token + "?base64_encoded=false";
            Instant deadline = Instant.now().plus(timeout);
            while (Instant.now().isBefore(deadline)) {
                ResponseEntity<Map> resultResponse = restTemplate.exchange(
                        resultUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );
                Map body = resultResponse.getBody();
                if (body == null) {
                    throw new RuntimeException("Empty result response from Judge0");
                }
                if (!isPending(body)) {
                    return toResponse(body, language);
                }
                sleep(judge0PollInterval);
            }
            return new ExecutionDtos.ExecutionResponse(
                    "TIMEOUT",
                    "",
                    "",
                    "Judge0 polling timed out.",
                    String.valueOf(timeout.toSeconds()),
                    "",
                    language
            );
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.warn("Judge0 request failed with status {}: {}", e.getStatusCode(), responseBody);
            return new ExecutionDtos.ExecutionResponse(
                    "INTERNAL_ERROR",
                    "",
                    responseBody,
                    "",
                    "",
                    "",
                    language
            );
        }
    }

    private ExecutionDtos.ExecutionResponse runLocally(ExecutionDtos.ExecutionRequest request, String language) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("collab-exec-");
            return switch (language) {
                case "python", "py", "python3" -> runPython(request, language, workDir);
                case "c" -> runCompiled(request, language, workDir, "main.c", List.of("cc", "-std=c11", "-O2", "main.c", "-o", "main"), List.of("./main"));
                case "cpp", "c++" -> runCompiled(request, language, workDir, "main.cpp", List.of("c++", "-std=c++17", "-O2", "main.cpp", "-o", "main"), List.of("./main"));
                case "java" -> runJava(request, language, workDir);
                default -> throw new IllegalArgumentException("Unsupported language: " + request.language());
            };
        } catch (Exception e) {
            log.warn("Local execution failed", e);
            return new ExecutionDtos.ExecutionResponse(
                    "INTERNAL_ERROR",
                    "",
                    e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(),
                    "",
                    "",
                    "",
                    language
            );
        } finally {
            deleteQuietly(workDir);
        }
    }

    private ExecutionDtos.ExecutionResponse runPython(ExecutionDtos.ExecutionRequest request, String language, Path workDir) throws IOException, InterruptedException {
        Files.writeString(workDir.resolve("script.py"), request.sourceCode(), StandardCharsets.UTF_8);
        ProcessResult result = runCommand(workDir, List.of("python3", "script.py"), request.stdin());
        return runtimeResponse(language, result, "");
    }

    private ExecutionDtos.ExecutionResponse runJava(ExecutionDtos.ExecutionRequest request, String language, Path workDir) throws IOException, InterruptedException {
        String className = detectJavaClassName(request.sourceCode());
        if (className == null) {
            return new ExecutionDtos.ExecutionResponse(
                    "COMPILE_ERROR",
                    "",
                    "",
                    "Java source must declare a class, for example `public class Main`.",
                    "",
                    "",
                    language
            );
        }

        Files.writeString(workDir.resolve(className + ".java"), request.sourceCode(), StandardCharsets.UTF_8);
        ProcessResult compileResult = runCommand(workDir, List.of("javac", className + ".java"), null);
        if (compileResult.timedOut()) {
            return new ExecutionDtos.ExecutionResponse("TIMEOUT", "", "", compileResult.stderr(), seconds(compileResult.elapsedMillis()), "", language);
        }
        if (compileResult.exitCode() != 0) {
            return new ExecutionDtos.ExecutionResponse(
                    "COMPILE_ERROR",
                    "",
                    "",
                    mergeOutputs(compileResult),
                    seconds(compileResult.elapsedMillis()),
                    "",
                    language
            );
        }

        ProcessResult runResult = runCommand(workDir, List.of("java", "-cp", ".", className), request.stdin());
        return runtimeResponse(language, runResult, "");
    }

    private ExecutionDtos.ExecutionResponse runCompiled(ExecutionDtos.ExecutionRequest request,
                                                        String language,
                                                        Path workDir,
                                                        String sourceFile,
                                                        List<String> compileCommand,
                                                        List<String> runCommand) throws IOException, InterruptedException {
        Files.writeString(workDir.resolve(sourceFile), request.sourceCode(), StandardCharsets.UTF_8);

        ProcessResult compileResult = runCommand(workDir, compileCommand, null);
        if (compileResult.timedOut()) {
            return new ExecutionDtos.ExecutionResponse("TIMEOUT", "", "", compileResult.stderr(), seconds(compileResult.elapsedMillis()), "", language);
        }
        if (compileResult.exitCode() != 0) {
            return new ExecutionDtos.ExecutionResponse(
                    "COMPILE_ERROR",
                    "",
                    "",
                    mergeOutputs(compileResult),
                    seconds(compileResult.elapsedMillis()),
                    "",
                    language
            );
        }

        ProcessResult runResult = runCommand(workDir, runCommand, request.stdin());
        return runtimeResponse(language, runResult, "");
    }

    private ExecutionDtos.ExecutionResponse runtimeResponse(String language, ProcessResult result, String compileOutput) {
        if (result.timedOut()) {
            return new ExecutionDtos.ExecutionResponse(
                    "TIMEOUT",
                    result.stdout(),
                    result.stderr(),
                    compileOutput,
                    seconds(result.elapsedMillis()),
                    "",
                    language
            );
        }
        if (result.exitCode() != 0) {
            return new ExecutionDtos.ExecutionResponse(
                    "RUNTIME_ERROR",
                    result.stdout(),
                    result.stderr(),
                    compileOutput,
                    seconds(result.elapsedMillis()),
                    "",
                    language
            );
        }
        return new ExecutionDtos.ExecutionResponse(
                "SUCCESS",
                result.stdout(),
                result.stderr(),
                compileOutput,
                seconds(result.elapsedMillis()),
                "",
                language
        );
    }

    private ProcessResult runCommand(Path workDir, List<String> command, String stdin) throws IOException, InterruptedException {
        Instant startedAt = Instant.now();
        Process process = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .start();

        if (stdin != null) {
            process.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
        }
        process.getOutputStream().close();

        CompletableFuture<String> stdoutFuture = readStream(process.getInputStream());
        CompletableFuture<String> stderrFuture = readStream(process.getErrorStream());

        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        long elapsedMillis = Duration.between(startedAt, Instant.now()).toMillis();
        if (!finished) {
            process.destroyForcibly();
            process.waitFor(2, TimeUnit.SECONDS);
            return new ProcessResult(-1, stdoutFuture.join(), stderrFuture.join(), true, elapsedMillis);
        }
        return new ProcessResult(
                process.exitValue(),
                stdoutFuture.join(),
                stderrFuture.join(),
                false,
                elapsedMillis
        );
    }

    private CompletableFuture<String> readStream(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        });
    }

    private ExecutionDtos.ExecutionResponse toResponse(Map body, String language) {
        String status = statusFrom(body);
        return new ExecutionDtos.ExecutionResponse(
                status,
                asString(body.get("stdout")),
                asString(body.get("stderr")),
                asString(body.get("compile_output")),
                String.valueOf(body.getOrDefault("time", "")),
                String.valueOf(body.getOrDefault("memory", "")),
                language
        );
    }

    private boolean isPending(Map body) {
        Object status = body.get("status");
        if (!(status instanceof Map<?, ?> statusMap)) {
            return false;
        }
        Object id = statusMap.get("id");
        return id instanceof Number number && (number.intValue() == 1 || number.intValue() == 2);
    }

    private String statusFrom(Map body) {
        Object status = body.get("status");
        if (!(status instanceof Map<?, ?> statusMap)) {
            return "INTERNAL_ERROR";
        }
        Object statusObj = statusMap.get("description");
        String description = statusObj == null ? "" : statusObj.toString().toUpperCase(Locale.ROOT);
        if (description.contains("WRONG"))
            return "RUNTIME_ERROR";
        if (description.contains("COMPILATION ERROR"))
            return "COMPILE_ERROR";
        if (description.contains("TIME"))
            return "TIMEOUT";
        if (description.contains("RUNTIME"))
            return "RUNTIME_ERROR";
        if (description.contains("ACCEPTED")) return "SUCCESS";
        return description.isEmpty() ? "INTERNAL_ERROR" : description.replace(' ', '_');
    }

    private String detectJavaClassName(String sourceCode) {
        Matcher publicClassMatcher = JAVA_PUBLIC_CLASS.matcher(sourceCode);
        if (publicClassMatcher.find()) {
            return publicClassMatcher.group(1);
        }
        Matcher classMatcher = JAVA_CLASS.matcher(sourceCode);
        if (classMatcher.find()) {
            return classMatcher.group(1);
        }
        return null;
    }

    private String normalizeLanguage(String language) {
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private String mergeOutputs(ProcessResult result) {
        String stderr = result.stderr();
        String stdout = result.stdout();
        if (!stderr.isBlank() && !stdout.isBlank()) {
            return stderr + System.lineSeparator() + stdout;
        }
        return stderr.isBlank() ? stdout : stderr;
    }

    private String seconds(long elapsedMillis) {
        return String.format(Locale.ROOT, "%.3f", elapsedMillis / 1000.0);
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Judge0", e);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(current -> {
                try {
                    Files.deleteIfExists(current);
                } catch (IOException ignored) {
                    log.debug("Failed to delete temporary execution path {}", current);
                }
            });
        } catch (IOException ignored) {
            log.debug("Failed to clean up temporary execution directory {}", path);
        }
    }

    private String asString(Object o) {
        return o == null ? "" : o.toString();
    }

    private record ProcessResult(int exitCode, String stdout, String stderr, boolean timedOut, long elapsedMillis) {
    }
}
