package cl.bcs.oracle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * Subject of the NEW implementation (modernized BcsPlataformaServicios, plan step 3): calls the in-process JavaCall entry
 * {@code cl.bcs.plataforma.adapter.in.javacall.ConsultarEstadoModuloHandler.call(Connection, JsonObject)} on the same seeded H2 connection
 * the legacy gets. Reflection on purpose: the oracle compiles with --release 11 and the new classes are Java 25, so nothing here links to
 * them at compile time; they only need to be on the RUN classpath (record.sh, ORACLE_EXTRA_CP = modernized/.../BcsPlataformaServicios/target/classes).
 * The handler is plain Java (JDBC + Gson, no Spring). Exceptions are rethrown unwrapped, so the dump records the real exception type/text
 * (the generic HTTP 500 of DEV-A3 lives in the controller, not here).
 */
public class NewSubject implements Subject {

    private static final String HANDLER = "cl.bcs.plataforma.adapter.in.javacall.ConsultarEstadoModuloHandler";

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Object handler = Class.forName(HANDLER).getDeclaredConstructor().newInstance();   // one instance per call, as the legacy PluginLoader
        Method m = handler.getClass().getMethod("call", Connection.class, JsonObject.class);
        try {
            return (JsonObject) m.invoke(handler, con, jin);
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            if (c instanceof Exception) throw (Exception) c;
            throw e;
        }
    }
}
