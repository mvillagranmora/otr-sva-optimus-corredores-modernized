package cl.bcs.filters.controller;

import cl.bcs.filters.service.LoginService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Handles the local login and provisional-password endpoints.
 * These URLs are whitelisted from both security filters so they are always reachable.
 */
@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /** POST /PLA/CrearSesion — local username + password login. */
    @PostMapping("/PLA/CrearSesion")
    public ResponseEntity<Map<String, String>> crearSesion(
            @Valid @RequestBody LoginRequest body) {
        try {
            String canal = body.canal() != null ? body.canal() : "WEB";
            String token = loginService.login(body.codigo(), body.clave(), canal);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (DisabledException | LockedException | AccountExpiredException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    /** POST /PLA/LoginCliente — alias used by client-facing applications. */
    @PostMapping("/PLA/LoginCliente")
    public ResponseEntity<Map<String, String>> loginCliente(
            @Valid @RequestBody LoginRequest body) {
        return crearSesion(body);
    }

    // --- request / response DTOs ---

    public record LoginRequest(
            @NotBlank String codigo,
            @NotBlank String clave,
            String canal) {
    }
}
