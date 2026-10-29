package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/**
 * Port H (WRITE, append-only; gate G5): legacy {@code cl.bcs.plataforma.pla.AgregarLog}. By HTTP the owner commits on its own connection, so a later
 * rollback of the request does not undo the log line (5.4). If the router answers 503 or is unreachable the call raises
 * {@code ServicioNoServidoException} (rollback + 503, DEV-F5); an indeterminate failure (timeout, 500) rolls back and answers the generic 500.
 */
public interface LogPort {

    JsonObject agregar(JsonObject entrada);
}
