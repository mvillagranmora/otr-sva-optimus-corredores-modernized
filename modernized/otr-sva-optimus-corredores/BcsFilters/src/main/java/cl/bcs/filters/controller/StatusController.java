package cl.bcs.filters.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Minimal health / probe endpoint.
 *
 * This endpoint IS protected by both security filters (it is not in noAuthURLs).
 * It is used in integration tests to exercise filter behaviour without needing a
 * domain-specific controller — a valid X-SESSION-TOKEN is required to get 200.
 */
@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    /** GET /PLA/Ping — whitelisted, no auth required. */
    @GetMapping("/PLA/Ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
