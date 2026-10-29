package cl.bcs.oracle;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * F2 W1B (layer 2 of PLAN_FONDOMUTUO_F2 7) at the JDBC boundary of the oracle session, active only with -Doracle.tesoreria=true (record_w1b.sh):
 * <ul>
 * <li>the SQL issued by a {@code cl.bcs.tesoreria.*} class (legacy or the modern library: the same SQL text) goes through Tesoreria's own H2 dialect
 *     bridge ({@code cl.bcs.tesoreria.familia_a.support.H2Hana.translate}, of BcsTesoreriaServicios' family-A tests), AFTER the FondoMutuo rules (so
 *     CURRENT_UTCTIMESTAMP keeps the FondoMutuo fixed clock); FondoMutuo's own SQL is untouched;</li>
 * <li>the HANA procedure {@code BSCL.Tesoreria.BD.Servicios::CrearComprobante} (not reproducible on H2) is a REGISTERED DOUBLE at the JDBC call: the
 *     call is recorded as collaborator {@code CrearComprobante.procedimiento} with its bound IN values (as sorted text: the legacy binds them by
 *     NamedCallableStatement, the modern library by position) and answered from the case script; its OUT scalars come from the answer (default 0) and
 *     it returns no table (the voucher rows are not created: whatever the procedure would write is outside this oracle).</li>
 * </ul>
 * The JUnit copy in the module ({@code normal.TesoreriaSesion}) implements the same rules.
 */
public final class TesoreriaH2 {

    private static final Pattern PROCEDIMIENTO = Pattern.compile("(?is)\\bcall\\s+\"BSCL\\.Tesoreria\\.BD\\.Servicios::CrearComprobante\"");

    private TesoreriaH2() {
    }

    public static boolean activo() {
        return Boolean.getBoolean("oracle.tesoreria");
    }

    static boolean desdeTesoreria() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().startsWith("cl.bcs.tesoreria.") && !e.getClassName().startsWith("cl.bcs.tesoreria.familia_a.")) return true;
        }
        return false;
    }

    public static String traducir(String sql) {
        if (!desdeTesoreria()) return sql;
        try {
            Method m = Class.forName("cl.bcs.tesoreria.familia_a.support.H2Hana").getMethod("translate", String.class);
            return (String) m.invoke(null, sql);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Tesoreria dialect bridge not on the oracle classpath", e);
        }
    }

    public static boolean esProcedimiento(String sql) {
        return PROCEDIMIENTO.matcher(sql).find();
    }

    public static CallableStatement procedimiento(String sql) {
        List<String> valores = new ArrayList<>();
        JsonObject[] respuesta = new JsonObject[1];
        return (CallableStatement) Proxy.newProxyInstance(TesoreriaH2.class.getClassLoader(), new Class<?>[] { CallableStatement.class }, (p, m, a) -> {
            String n = m.getName();
            if (n.startsWith("set") && a != null && a.length >= 2 && !n.equals("setQueryTimeout") && !n.equals("setFetchSize")) {
                valores.add(n.equals("setNull") ? "null" : String.valueOf(a[1]));
                return null;
            }
            switch (n) {
                case "execute": case "executeUpdate":
                    Collections.sort(valores);
                    JsonArray arg = new JsonArray();
                    valores.forEach(arg::add);
                    respuesta[0] = Doubles.answer("CrearComprobante.procedimiento", "call", new Object[] { arg }, JsonObject.class);
                    return n.equals("execute") ? (Object) Boolean.FALSE : (Object) Integer.valueOf(0);
                case "getLong": case "getInt": case "getString": case "getObject": case "getBigDecimal":
                    return salida(respuesta[0], a[0], m.getReturnType());
                case "getUpdateCount": return -1;
                case "getMoreResults": case "isClosed": case "wasNull": return false;
                case "getResultSet": return null;
                default: return m.getReturnType().isPrimitive() ? (m.getReturnType() == boolean.class ? (Object) false : (Object) 0) : null;
            }
        });
    }

    private static Object salida(JsonObject r, Object clave, Class<?> tipo) {
        JsonElement v = r != null && clave instanceof String && r.has((String) clave) ? r.get((String) clave) : null;
        long n = v == null || v.isJsonNull() ? 0L : v.getAsLong();
        if (tipo == long.class || tipo == Long.class) return n;
        if (tipo == int.class) return (int) n;
        if (tipo == String.class) return String.valueOf(n);
        if (tipo == java.math.BigDecimal.class) return java.math.BigDecimal.valueOf(n);
        return n;
    }
}
