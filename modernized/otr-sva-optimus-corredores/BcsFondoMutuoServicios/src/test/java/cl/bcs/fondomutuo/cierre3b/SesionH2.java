package cl.bcs.fondomutuo.cierre3b;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

/**
 * H2 stand-in for the HANA dialect at the JDBC boundary (the same three rewrites as the oracle's SessionConnection.translate, H2 ONLY):
 * {@code CURRENT_UTCTIMESTAMP} = the fixed clock of the case, {@code "seq".NEXTVAL} = {@code NEXT VALUE FOR "seq"}, {@code #name} identifiers quoted.
 * The module code is not changed for H2: only the connection it receives is wrapped, exactly as the oracle wraps it for the legacy.
 */
public final class SesionH2 {

    private final Connection raw;
    private final String reloj;

    public SesionH2(Connection raw, String reloj) {
        this.raw = raw;
        this.reloj = reloj;
    }

    public Connection conexion() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("prepareStatement") || n.equals("prepareCall")) && a != null && a.length > 0 && a[0] instanceof String) {
                a = a.clone();
                a[0] = traducir((String) a[0]);
                return invocar(raw, m, a);
            }
            if (n.equals("createStatement")) {
                return sentencia((Statement) invocar(raw, m, a));
            }
            return invocar(raw, m, a);
        });
    }

    String traducir(String sql) {
        String s = sql.replaceAll("\\bCURRENT_UTCTIMESTAMP\\b(?!\\s*\\()", "TIMESTAMP '" + reloj + "'");
        s = s.replaceAll("(\"[^\"]+\")\\.NEXTVAL", "NEXT VALUE FOR $1");
        s = s.replaceAll("(?<=[\\s(,=>])#(?=[A-Za-z_])", "\"#").replaceAll("(\"#\\w+)", "$1\"");
        return s;
    }

    private Statement sentencia(Statement st) {
        return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Statement.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery") || n.equals("addBatch")) && a != null && a.length > 0
                    && a[0] instanceof String) {
                a = a.clone();
                a[0] = traducir((String) a[0]);
            }
            return invocar(st, m, a);
        });
    }

    private static Object invocar(Object target, Method m, Object[] a) throws Throwable {
        try {
            return m.invoke(target, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
