package cl.bcs.filters.port.out;

import cl.bcs.filters.domain.Sesion;

import java.util.Optional;

/** Port — session persistence. */
public interface SesionRepository {

    /** Returns the session for the given token, or empty if not found. */
    Optional<Sesion> findByToken(String token);

    /** Persists a new session and returns the token. */
    String create(Long idUsuario, String canal);
}
