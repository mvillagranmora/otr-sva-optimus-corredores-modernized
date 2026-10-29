package cl.bcs.tesoreria.familia_b.support.colaboradores;

import java.sql.SQLException;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle SQL.call shim: a HANA stored procedure is not reproducible on H2, it is recorded as {procedimiento, entrada} and answers no outputs;
 * {@code colaboradores.procedimientoLanza} (text) makes it fail with a {@link SQLException}.
 */
public class StoredProcedureGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "procedimientoLanza";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) throws SQLException {
        if (guiones.has(clave()) && !guiones.get(clave()).isJsonNull()) {
            throw new SQLException(guiones.get(clave()).getAsString());
        }
        return new JsonObject();
    }
}
