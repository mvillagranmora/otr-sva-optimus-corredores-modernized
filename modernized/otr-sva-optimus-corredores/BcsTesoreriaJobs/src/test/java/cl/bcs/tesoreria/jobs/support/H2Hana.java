package cl.bcs.tesoreria.jobs.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.regex.Pattern;

/**
 * SAP HANA -> H2 dialect bridge used by BOTH sides of the characterization tests (the legacy oracle and the modern tests), so
 * that both execute exactly the same SQL text (the production SQL, unmodified) against the same H2 engine. Reduced copy of
 * the Tesoreria Servicios {@code familia_b.support.H2Hana}: only what BcsTesoreriaJobs SQL needs. Pure JDK (it is also
 * compiled by the oracle, see legacy-oracle/record.sh).
 *
 * <p>It wraps a JDBC {@link Connection} (dynamic proxy) and rewrites every statement prepared or executed:
 * <ol>
 *   <li>a trailing {@code ;} is removed;</li>
 *   <li>{@code BSCL."X"} (HANA default schema prefix) becomes {@code "X"};</li>
 *   <li>{@code CURRENT_UTCTIMESTAMP} becomes {@code HANA_UTCNOW()} and {@code CURRENT_DATE} {@code HANA_TODAY()}: the database
 *       clock is the fixed {@link #reloj} (the case "reloj"), so the goldens are deterministic;</li>
 *   <li>{@code SET SCHEMA ...} and {@code SET TRANSACTION AUTOCOMMIT DDL OFF} (the BcsJob session statements, BcsJob.java:130-132)
 *       become {@code SELECT 1}.</li>
 * </ol>
 * HANA functions (TO_NVARCHAR, TO_DECIMAL ...) are static methods of this class registered as H2 aliases by schema-jobs.sql.
 */
public final class H2Hana {

    /** The database UTC clock; the oracle and the modern tests set it from the case. */
    public static volatile Timestamp reloj = Timestamp.valueOf("2025-04-01 10:15:30");

    private static final Pattern DEFAULT_SCHEMA = Pattern.compile("\\bBSCL\\.\"");
    private static final Pattern UTC_NOW_RE = Pattern.compile("\\bCURRENT_UTCTIMESTAMP\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TODAY_RE = Pattern.compile("\\bCURRENT_DATE\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SESSION_RE = Pattern.compile("(?is)^\\s*SET\\s+(SCHEMA\\b.*|TRANSACTION\\s+AUTOCOMMIT\\b.*)$");

    private H2Hana() {
    }

    public static String translate(String sql) {
        if (sql == null) {
            return null;
        }
        String s = sql.strip();
        while (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1).stripTrailing();
        }
        if (SESSION_RE.matcher(s).matches()) {
            return "SELECT 1";
        }
        s = DEFAULT_SCHEMA.matcher(s).replaceAll("\"");
        s = UTC_NOW_RE.matcher(s).replaceAll("HANA_UTCNOW()");
        s = TODAY_RE.matcher(s).replaceAll("HANA_TODAY()");
        return s;
    }

    /** Wraps {@code real} so every SQL text is translated before reaching H2. */
    public static Connection wrap(Connection real) {
        InvocationHandler h = (proxy, m, a) -> {
            try {
                String n = m.getName();
                if (a != null && a.length > 0 && a[0] instanceof String s
                        && (n.equals("prepareStatement") || n.equals("prepareCall") || n.equals("nativeSQL"))) {
                    Object[] b = a.clone();
                    b[0] = translate(s);
                    return m.invoke(real, b);
                }
                if (n.equals("createStatement")) {
                    return wrapStatement((Statement) m.invoke(real, a));
                }
                return m.invoke(real, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(H2Hana.class.getClassLoader(), new Class<?>[]{Connection.class}, h);
    }

    private static Statement wrapStatement(Statement real) {
        InvocationHandler h = (proxy, m, a) -> {
            try {
                String n = m.getName();
                if (a != null && a.length > 0 && a[0] instanceof String s
                        && (n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery")
                        || n.equals("addBatch") || n.equals("executeLargeUpdate"))) {
                    Object[] b = a.clone();
                    b[0] = translate(s);
                    return m.invoke(real, b);
                }
                return m.invoke(real, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Statement) Proxy.newProxyInstance(H2Hana.class.getClassLoader(), new Class<?>[]{Statement.class}, h);
    }

    // ------------------------------------------------------------------ SQL functions (H2 aliases; String arguments)

    /** HANA TO_NVARCHAR(x): the canonical text of the value. */
    public static String toNvarchar(String v) {
        return v;
    }

    /** HANA TO_NVARCHAR(date, format): a date text is reformatted ('yyyy-mm-dd' style formats), anything else is returned as is. */
    public static String toNvarchar(String v, String fmt) {
        if (v == null) {
            return null;
        }
        String s = v.strip();
        if (s.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            String f = fmt.replace("YYYY", "yyyy").replace("DD", "dd").replace("HH24", "HH").replace("MI", "mm").replace("SS", "ss")
                    .replace("mm-dd", "MM-dd").replace("yyyy-mm", "yyyy-MM");
            return new java.text.SimpleDateFormat(f).format(java.sql.Date.valueOf(s.substring(0, 10)));
        }
        return s;
    }

    public static BigDecimal toDecimal(String v) {
        return v == null ? null : new BigDecimal(v.strip());
    }

    public static Timestamp utcNow() {
        return reloj;
    }

    public static java.sql.Date today() {
        return java.sql.Date.valueOf(reloj.toLocalDateTime().toLocalDate());
    }
}
