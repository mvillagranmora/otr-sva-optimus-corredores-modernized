package cl.bcs.filters.domain;

import java.time.LocalDateTime;

/**
 * Domain model for a platform session.
 *
 * @param idTokenSesion session token (opaque string)
 * @param tokenValido   'S' means valid; any other value means invalid / revoked
 * @param idUsuario     FK to ACC_USUARIO
 * @param canal         WEB, COR, WS, CLI, AGE …
 * @param fechaCreacion when the session was created (used for COR timeout — RULE-025)
 */
public record Sesion(
        String idTokenSesion,
        boolean tokenValido,
        Long idUsuario,
        String canal,
        LocalDateTime fechaCreacion) {
}
