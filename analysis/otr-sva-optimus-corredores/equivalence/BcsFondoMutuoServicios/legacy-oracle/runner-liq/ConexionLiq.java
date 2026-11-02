import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.regex.Pattern;

/**
 * F3 W3 T6-B (LiquidacionSolicitudAporteRescate): OWN wrapper of the connection the legacy class receives (the shared SessionConnection is not touched). ONE job, a SQL dialect bridge that exists
 * ONLY because the stand-in database is H2: the UPDATE of the class (LiquidacionSolicitudAporteRescate_UpdateSolicitudes.sql) is HANA's {@code UPDATE alias SET alias."c" = .. FROM "table" AS alias WHERE ..}
 * and H2 reads {@code alias} as a table name. The bridge rewrites it to {@code UPDATE "table" AS alias SET alias."c" = .. WHERE ..} (same rows, same assignments). The same regex lives in the test harness of
 * the module (ConexionLiq in the test package of the step). Nothing else is altered; every other statement is delegated untouched.
 */
final class ConexionLiq {

    private static final Pattern UPDATE_FROM = Pattern.compile("(?is)^\\s*UPDATE\\s+(\\w+)\\s+SET\\s+(.*?)\\s+FROM\\s+(\"[^\"]+\")\\s+AS\\s+\\1\\s+WHERE\\s+(.*?)\\s*;?\\s*$");

    private ConexionLiq() {
    }

    static String puente(String sql) {
        java.util.regex.Matcher m = UPDATE_FROM.matcher(sql);
        return m.matches() ? "UPDATE " + m.group(3) + " AS " + m.group(1) + " SET " + m.group(2) + " WHERE " + m.group(4) : sql;
    }

    static Connection envolver(Connection interna) {
        return (Connection) Proxy.newProxyInstance(ConexionLiq.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if (m.getName().equals("prepareStatement") && a != null && a.length > 0 && a[0] instanceof String) {
                a = a.clone();
                a[0] = puente((String) a[0]);
            }
            try {
                return m.invoke(interna, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
