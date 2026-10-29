package cl.bcs.fondomutuo.normal;

import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.tesoreria.H2Hana;

/**
 * F2 W1B, the module side of {@code cl.bcs.oracle.TesoreriaH2} of the oracle (legacy-oracle/runner), SAME rules: the SQL issued from a
 * {@code cl.bcs.tesoreria.*} class (the tesoreria-service library) goes through Tesoreria's dialect bridge AFTER the FondoMutuo rules; the HANA procedure
 * {@code BSCL.Tesoreria.BD.Servicios::CrearComprobante} is the scripted collaborator {@code CrearComprobante.procedimiento} (bound IN values as sorted
 * text; OUT scalars from the answer, default 0; no table). Active only for the layer-2 cases ({@code "tesoreriaReal": true}).
 */
final class TesoreriaSesion {

    private static final Pattern PROCEDIMIENTO = Pattern.compile("(?is)\\bcall\\s+\"BSCL\\.Tesoreria\\.BD\\.Servicios::CrearComprobante\"");

    private TesoreriaSesion() {
    }

    static boolean desdeTesoreria() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().startsWith("cl.bcs.tesoreria.")) return true;
        }
        return false;
    }

    static String traducir(String sql) {
        return desdeTesoreria() ? H2Hana.translate(sql) : sql;
    }

    static boolean esProcedimiento(String sql) {
        return PROCEDIMIENTO.matcher(sql).find();
    }

    static CallableStatement procedimiento(Guion guion) {
        List<String> valores = new ArrayList<>();
        JsonObject[] respuesta = new JsonObject[1];
        return (CallableStatement) Proxy.newProxyInstance(TesoreriaSesion.class.getClassLoader(), new Class<?>[] { CallableStatement.class }, (p, m, a) -> {
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
                    respuesta[0] = guion.responder("CrearComprobante.procedimiento", arg);
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
