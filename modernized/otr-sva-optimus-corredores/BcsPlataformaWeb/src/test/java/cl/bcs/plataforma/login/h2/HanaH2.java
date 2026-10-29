package cl.bcs.plataforma.login.h2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

/**
 * H2 dialect bridge of the login tests (H2 only, never HANA): the HANA statements of the login path run unchanged through it. Rewrites:
 * {@code "seq".NEXTVAL} to {@code NEXT VALUE FOR "seq"}; {@code CURRENT_UTCTIMESTAMP} to {@code LOCALTIMESTAMP} (the tests run with the JVM in UTC,
 * so the local clock of H2 is the UTC clock). {@code ADD_SECONDS} and {@code TO_INT} are aliases created by the schema script.
 */
public final class HanaH2 {

    private HanaH2() {
    }

    public static String translate(String sql) {
        if (sql == null) return null;
        String s = sql.replaceAll("(\"[^\"]+\")\\.NEXTVAL", "NEXT VALUE FOR $1");
        return s.replace("CURRENT_UTCTIMESTAMP", "LOCALTIMESTAMP");
    }

    /** wraps an H2 connection: every SQL text given to prepareStatement / prepareCall / Statement.execute* goes through translate */
    public static Connection wrap(Connection raw) {
        return (Connection) Proxy.newProxyInstance(HanaH2.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("prepareStatement") || n.equals("prepareCall") || n.equals("nativeSQL")) && a != null && a.length > 0 && a[0] instanceof String) {
                a = a.clone();
                a[0] = translate((String) a[0]);
                return invoke(raw, m, a);
            }
            if (n.equals("createStatement")) return statement((Statement) invoke(raw, m, a));
            return invoke(raw, m, a);
        });
    }

    private static Statement statement(Statement st) {
        return (Statement) Proxy.newProxyInstance(HanaH2.class.getClassLoader(), new Class<?>[] { Statement.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery") || n.equals("addBatch")) && a != null && a.length > 0
                    && a[0] instanceof String) {
                a = a.clone();
                a[0] = translate((String) a[0]);
            }
            return invoke(st, m, a);
        });
    }

    private static Object invoke(Object target, Method m, Object[] a) throws Throwable {
        try {
            return m.invoke(target, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
