package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * Runs the unmodified legacy class exactly as CallServlet does: new instance per call, JavaCall.call(conn, json). The class is reached BY NAME
 * (cl.bcs.fondomutuo.&lt;servicio&gt;) so that each script (record.sh, record_rre.sh, record_fsa.sh, ...) compiles only the closure it needs; before F3 this
 * was a switch over the classes of F1/F2 with the same behaviour.
 */
public class LegacySubject implements Subject {
    @Override
    public JsonObject call(String servicio, Connection con, JsonObject jin) throws Exception {
        Class<?> c;
        try {
            c = Class.forName("cl.bcs.fondomutuo." + servicio);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("unknown servicio " + servicio);
        }
        return ((cl.bcs.utils.JavaCall) c.getDeclaredConstructor().newInstance()).call(con, jin);
    }
}
