package cl.bcs.apv.jobs.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.regex.Pattern;

/**
 * SAP HANA -> H2 dialect bridge used by BOTH sides of the characterization tests (the legacy oracle and the modern code run the same SQL text on the
 * same H2 engine). Trimmed copy of the BcsAPVServicios bridge: only the constructs the SQL of the four APV jobs uses.
 * Rewrites: trailing {@code ;} removed; {@code #Name} (local temporary table) becomes {@code TT_Name}; {@code BSCL."X"} becomes {@code "X"};
 * {@code CURRENT_UTCTIMESTAMP}/{@code CURRENT_TIMESTAMP} become the fixed-clock alias {@code HANA_UTCNOW()}; column-store attributes are dropped.
 * TO_NVARCHAR / TO_VARCHAR / TO_BIGINT are static methods registered as H2 aliases by schema-apv-jobs.sql (String arguments).
 */
public final class H2Hana {

    /** Fixed UTC clock returned by CURRENT_UTCTIMESTAMP. */
    public static final Timestamp UTC_NOW = Timestamp.valueOf("2025-04-01 10:15:30");

    private static final Pattern TEMP_TABLE = Pattern.compile("#(\\w+)");
    private static final Pattern DEFAULT_SCHEMA = Pattern.compile("\\bBSCL\\.\"");
    private static final Pattern UTC_NOW_RE = Pattern.compile("\\bCURRENT_(?:UTC)?TIMESTAMP\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern CS_ATTR_RE = Pattern.compile("\\s+CS_[A-Z0-9_]+\\b");

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
        s = TEMP_TABLE.matcher(s).replaceAll("TT_$1");
        s = DEFAULT_SCHEMA.matcher(s).replaceAll("\"");
        s = UTC_NOW_RE.matcher(s).replaceAll("HANA_UTCNOW()");
        return CS_ATTR_RE.matcher(s).replaceAll("");
    }

    /** Wraps {@code real} so every SQL text is translated before reaching H2. */
    public static Connection wrap(Connection real) {
        InvocationHandler h = (proxy, m, a) -> {
            try {
                String n = m.getName();
                if (a != null && a.length > 0 && a[0] instanceof String s && (n.equals("prepareStatement") || n.equals("prepareCall") || n.equals("nativeSQL"))) {
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
                        && (n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery") || n.equals("addBatch") || n.equals("executeLargeUpdate"))) {
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

    // ------------------------------------------------------------------------------------------------ SQL functions (H2 aliases)

    private static String formatTimestamp(Timestamp ts, String fmt) {
        java.time.LocalDateTime t = ts.toLocalDateTime();
        return fmt.replace("YYYY", String.format("%04d", t.getYear())).replace("HH24", String.format("%02d", t.getHour()))
                .replace("MI", String.format("%02d", t.getMinute())).replace("SS", String.format("%02d", t.getSecond()))
                .replace("MM", String.format("%02d", t.getMonthValue())).replace("DD", String.format("%02d", t.getDayOfMonth()));
    }

    public static String toNvarchar(String v) {
        return v;
    }

    /** HANA TO_NVARCHAR(date_or_timestamp, format). A text that does not look like a date is returned as is. */
    public static String toNvarchar(String v, String fmt) {
        if (v == null) {
            return null;
        }
        String s = v.strip();
        if (s.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return formatTimestamp(toTimestamp(s), fmt);
        }
        return s;
    }

    public static String toVarchar(String v) {
        return toNvarchar(v);
    }

    public static String toVarchar(String v, String fmt) {
        return toNvarchar(v, fmt);
    }

    public static Long toBigint(String v) {
        return v == null ? null : new BigDecimal(v.strip()).longValue();
    }

    private static Timestamp toTimestamp(String v) {
        String s = v.strip();
        return Timestamp.valueOf(s.length() == 10 ? s + " 00:00:00" : s);
    }

    public static Timestamp utcNow() {
        return UTC_NOW;
    }
}
