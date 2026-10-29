package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.JsonObject;

/** Runs the unmodified legacy class exactly as CallServlet does: new instance per call, JavaCall.call(conn, json). */
public class LegacySubject implements Subject {
    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        return new cl.bcs.plataforma.cre.ConsultarEstadoModulo().call(con, jin);
    }
}
