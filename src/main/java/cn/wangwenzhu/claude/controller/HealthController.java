package cn.wangwenzhu.claude.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setTimestamp(java.time.Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @Setter
    @Getter
    public static class HealthResponse {
        private String status;
        private String timestamp;

    }
}
