package cl.bcs.filters.port.out;

import cl.bcs.filters.domain.Usuario;

import java.util.Optional;

/** Port — user persistence. */
public interface UsuarioRepository {

    Optional<Usuario> findByCodigo(String codigoUsuario);

    /** Resolves the user linked to an active session token. */
    Optional<Usuario> findByToken(String token);

    void incrementarIntentos(String codigoUsuario);

    void resetearIntentos(String codigoUsuario);
}
