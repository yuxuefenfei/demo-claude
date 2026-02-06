package cn.wangwenzhu.claude.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        var response = new HealthResponse("UP", Instant.now());
        return ResponseEntity.ok(response);
    }

    public record HealthResponse(String status, Instant timestamp) {
    }
}
