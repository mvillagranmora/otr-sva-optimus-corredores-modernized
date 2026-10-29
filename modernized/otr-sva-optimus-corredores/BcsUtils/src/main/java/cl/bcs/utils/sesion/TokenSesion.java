package cl.bcs.utils.sesion;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.UUID;

/**
 * RULE-021: session token format of {@code Sesion.insertarSesion} (legacy {@code Sesion.java:184-185}):
 * {@code Base64(idEmpresa + ":" + usuario + ":" + Timestamp.toString() + UUID)}, standard alphabet with padding.
 * The legacy encoded with the platform default charset, pinned as UTF-8. Storing the session stays with the caller.
 */
public final class TokenSesion {

    private TokenSesion() {
    }

    /** The text that is encoded; {@code null} values are written as {@code "null"}, as in the legacy. */
    public static String raw(Long idEmpresa, String nombreUsuario, Timestamp currentTimestamp, UUID uuid) {
        return idEmpresa + ":" + nombreUsuario + ":" + currentTimestamp + uuid;
    }

    /** The token. Pass {@code UUID.randomUUID()} for a new session, as the legacy did. */
    public static String generar(Long idEmpresa, String nombreUsuario, Timestamp currentTimestamp, UUID uuid) {
        byte[] texto = raw(idEmpresa, nombreUsuario, currentTimestamp, uuid).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(texto);
    }
}
