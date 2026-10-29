package cl.bcs.filters.controller;

import cl.bcs.filters.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SSO (OAuth2 / OIDC) endpoints.
 *
 * These URLs are whitelisted from both security filters.
 *
 * Security fixes applied (implemented in {@link SsoService}):
 *   RULE-046 — state is a random UUID; callback validates the stored state.
 *   RULE-045 — callback checks FechaVigencia (account expiry).
 */
@RestController
public class SsoController {

    private static final Logger log = LoggerFactory.getLogger(SsoController.class);

    private final SsoService ssoService;

    public SsoController(SsoService ssoService) {
        this.ssoService = ssoService;
    }

    /**
     * GET /PLA/SSOInit — initiates the SSO flow.
     *
     * Returns the CSRF state token (UUID) that the client must embed in the IdP redirect URL.
     * RULE-046 FIX: state is generated here via UUID.randomUUID() in SsoService.
     */
    @GetMapping("/PLA/SSOInit")
    public ResponseEntity<Map<String, String>> ssoInit() {
        String state = ssoService.initSso();
        // In production the caller would build the full IdP redirect URL using this state.
        return ResponseEntity.ok(Map.of("state", state));
    }

    /**
     * GET /PLA/SSOLogon — IdP callback with authorisation code and CSRF state.
     *
     * RULE-046: validates the received state against the stored one.
     * RULE-045: checks FechaVigencia before creating a session.
     */
    @GetMapping("/PLA/SSOLogon")
    public ResponseEntity<Map<String, String>> ssoLogon(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam String codigoUsuario) {
        try {
            String token = ssoService.handleCallback(code, state, codigoUsuario);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalStateException e) {
            log.warn("SSO callback rejected: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid SSO state"));
        } catch (Exception e) {
            log.warn("SSO login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
