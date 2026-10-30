package cl.bcs.replica.port.out;

import java.sql.Connection;

import com.google.gson.JsonObject;

public interface ObtenerDiaSiguientePort {
    JsonObject logica(Connection conn, JsonObject entrada) throws Exception;
}
