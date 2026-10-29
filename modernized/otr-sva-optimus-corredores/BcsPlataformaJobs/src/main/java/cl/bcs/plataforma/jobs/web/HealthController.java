package cl.bcs.plataforma.jobs.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Minimal health-check controller.
 * Used as the canary endpoint for BUILD SUCCESS verification.
 */
@RestController
@RequestMapping("/plataforma-jobs")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "plataforma-jobs",
                "port", "8110"
        ));
    }
}
