package cl.bcs.filters.domain;

import java.time.LocalDate;

/**
 * Domain model for a platform user.
 *
 * @param idUsuario      internal PK
 * @param codigoUsuario  business key (login name)
 * @param estado         ACT = active; BLO = blocked; any other value = inactive
 * @param fechaVigencia  account-expiry date; null means no expiry
 * @param clave          stored password hash (BCrypt or SHA-256 legacy — HybridPasswordEncoder detects)
 * @param intentos       failed-login attempt counter
 */
public record Usuario(
        Long idUsuario,
        String codigoUsuario,
        String estado,
        LocalDate fechaVigencia,
        String clave,
        int intentos) {
}
