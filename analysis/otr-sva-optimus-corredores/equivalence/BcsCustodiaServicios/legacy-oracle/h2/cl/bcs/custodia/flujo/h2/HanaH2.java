package cl.bcs.custodia.flujo.h2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * H2 dialect bridge of the ConsultarFlujoCustodia oracle (H2 only, never HANA). SINGLE SOURCE: this file lives in
 * analysis/.../equivalence/BcsCustodiaServicios/legacy-oracle/h2/ and is copied byte for byte by legacy-oracle/sync.py into the module tests
 * (src/test/java/cl/bcs/custodia/flujo/h2/): the legacy oracle and the JUnit harness see EXACTLY the same SQL rewriting. It belongs to the harness
 * (the JDBC boundary), not to the subject: the legacy SQL text and the new implementation's SQL text go through it unchanged in meaning.
 *
 * <p>Rewrites, in this order (every one listed in README "H2 dialect equivalences"):
 * <ol>
 * <li>{@code "seq".NEXTVAL} -> {@code NEXT VALUE FOR "seq"} (HANA sequence pseudo column; H2 2.x LEGACY mode does not take the quoted form)</li>
 * <li>{@code TO_DATE(} -> {@code HANA_TO_DATE(} (HANA TO_DATE(x) with the default format YYYY-MM-DD returns DATE; H2's built-in TO_DATE returns
 *     TIMESTAMP WITH TIME ZONE, which the legacy reflection mapper cannot put in a java.sql.Date field)</li>
 * <li>{@code TO_DECIMAL(} -> {@code HANA_TO_DECIMAL(} (no TO_DECIMAL in H2)</li>
 * <li>{@code (1==1)} -> {@code (1=1)}: the always-true join the legacy writes when the FIRST winner of the usage matrix has no dimension
 *     (ConsultarFlujoCustodia.java:2198). H2 rejects {@code ==}; whether HANA accepts it is NOT verified here (QA unreachable): OPEN question.</li>
 * </ol>
 * TO_BIGINT is not rewritten: it is an H2 alias (schema-session.sql) because H2 has no function of that name. The text of the SQL is recorded
 * (option {@link #registro}) only for debugging; it is not part of the golden.
 */
public final class HanaH2 {

    private HanaH2() {
    }

    /** last SQL texts seen (after translation), for diagnostics of a failing case; bounded */
    private static final List<String> REGISTRO = Collections.synchronizedList(new ArrayList<>());

    public static List<String> registro() {
        return REGISTRO;
    }

    public static String translate(String sql) {
        if (sql == null) return null;
        String s = sql.replaceAll("(\"[^\"]+\")\\.NEXTVAL", "NEXT VALUE FOR $1");
        s = s.replaceAll("(?i)(?<![A-Za-z0-9_\"])TO_DATE\\s*\\(", "HANA_TO_DATE(");
        s = s.replaceAll("(?i)(?<![A-Za-z0-9_\"])TO_DECIMAL\\s*\\(", "HANA_TO_DECIMAL(");
        s = s.replace("(1==1)", "(1=1)");
        if (REGISTRO.size() > 200) REGISTRO.clear();
        REGISTRO.add(s);
        return s;
    }

    /** wraps an H2 connection: every SQL text given to prepareStatement / prepareCall / Statement.execute* / addBatch goes through translate */
    public static Connection wrap(Connection raw) {
        return (Connection) Proxy.newProxyInstance(HanaH2.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("prepareStatement") || n.equals("prepareCall") || n.equals("nativeSQL")) && a != null && a.length > 0 && a[0] instanceof String) {
                a = a.clone();
                a[0] = translate((String) a[0]);
                return invoke(raw, m, a);
            }
            if (n.equals("createStatement")) return statement((Statement) invoke(raw, m, a));
            if (n.equals("unwrap") && a != null && a.length == 1 && a[0] == Connection.class) return raw;
            return invoke(raw, m, a);
        });
    }

    private static Statement statement(Statement st) {
        return (Statement) Proxy.newProxyInstance(HanaH2.class.getClassLoader(), new Class<?>[] { Statement.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery") || n.equals("executeLargeUpdate") || n.equals("addBatch"))
                    && a != null && a.length > 0 && a[0] instanceof String) {
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
