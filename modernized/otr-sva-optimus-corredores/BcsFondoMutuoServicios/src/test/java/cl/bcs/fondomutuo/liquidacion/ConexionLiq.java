package cl.bcs.fondomutuo.liquidacion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * F3 W3 T6-B: the SQL dialect bridge that the H2 stand-in needs and HANA does not, the SAME one the oracle applies to the legacy ({@code runner-liq/ConexionLiq.java}). The UPDATE of Liquidacion
 * ({@code LiquidacionSolicitudAporteRescate_UpdateSolicitudes.sql}, verbatim in {@code src/main/resources}) is HANA's {@code UPDATE alias SET alias."c" = .. FROM "table" AS alias WHERE ..} and H2 reads
 * {@code alias} as a table name: the bridge rewrites it to {@code UPDATE "table" AS alias SET alias."c" = .. WHERE ..} (same rows, same assignments). Every other statement is delegated untouched. It is the harness,
 * not the subject: production code keeps the verbatim HANA text.
 */
final class ConexionLiq {

    private static final Pattern UPDATE_FROM = Pattern.compile("(?is)^\\s*UPDATE\\s+(\\w+)\\s+SET\\s+(.*?)\\s+FROM\\s+(\"[^\"]+\")\\s+AS\\s+\\1\\s+WHERE\\s+(.*?)\\s*;?\\s*$");

    private ConexionLiq() {
    }

    static String puente(String sql) {
        Matcher m = UPDATE_FROM.matcher(sql);
        return m.matches() ? "UPDATE " + m.group(3) + " AS " + m.group(1) + " SET " + m.group(2) + " WHERE " + m.group(4) : sql;
    }

    static Connection envolver(Connection interna) {
        return (Connection) Proxy.newProxyInstance(ConexionLiq.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            Object[] args = a;
            if (m.getName().equals("prepareStatement") && a != null && a.length > 0 && a[0] instanceof String) {
                args = a.clone();
                args[0] = puente((String) a[0]);
            }
            try {
                return m.invoke(interna, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
