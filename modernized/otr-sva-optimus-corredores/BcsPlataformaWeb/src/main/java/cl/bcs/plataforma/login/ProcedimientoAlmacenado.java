package cl.bcs.plataforma.login;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * Call of a stored procedure of the HANA catalog with IN, OUT and TABLE parameters (T-22), on the connection of the caller. The result carries every
 * OUT parameter by name ({@code P_OUT_ERRORESCONTADOR}, {@code P_OUT_MAIL}...). The production implementation is
 * {@link cl.bcs.plataforma.service.ProcedureDispatcherService}; the tests of the password recovery use a scripted double (an H2 database has no
 * HANA procedures, and calling the real ones would write: they generate the provisional password and send the notification).
 */
public interface ProcedimientoAlmacenado {

    JsonObject llamar(Connection con, String rutaServicio, JsonObject entrada) throws Exception;
}
