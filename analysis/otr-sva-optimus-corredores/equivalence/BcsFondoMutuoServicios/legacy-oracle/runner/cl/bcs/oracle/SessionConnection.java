package cl.bcs.oracle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * H2 stand-in for what the legacy gets from HANA at the JDBC boundary. The session wraps the real H2 connection (same object type for the
 * legacy and for the new implementation: it is the harness, not the subject) and emulates, H2 ONLY, never HANA:
 * <ul>
 * <li>AUTONOMOUS TRANSACTION procedures CrearBloqueoSolicitudAporteRescate / EliminarBloqueoSolicitudAporteRescate: the CALL reads the
 *     session local table #p_BloqueoSolicitudAporteRescate and applies INSERT / DELETE on the lock table through a SECOND connection in
 *     autocommit: it commits at once, outside the transaction of the caller (so a rollback of the request does not undo the lock).</li>
 * <li>CREATE LOCAL TEMPORARY TABLE #x / DROP TABLE #x: session state tracked here; the table itself is pre-created by schema-session.sql
 *     (a real H2 DDL would implicitly COMMIT the pending writes of the request; whether HANA does the same is an OPEN QUESTION).</li>
 * <li>HANA SQL dialect used by the 36 in-scope SQL: CURRENT_UTCTIMESTAMP (fixed oracle clock), schema.NEXTVAL, #name identifiers.</li>
 * </ul>
 */
public final class SessionConnection {
    public static final String LOCK_TABLE = "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate";
    private static final Pattern CALL_LOCK = Pattern.compile("(?is)^\\s*CALL\\s+\"[^\"]*::(Crear|Eliminar)BloqueoSolicitudAporteRescate\"");
    // F2 W1 front 2 (RescateReinversion): the two AUTONOMOUS TRANSACTION procedures of the reinversion lock (source BSCL/FondoMutuo/BD/Servicios/*.sql).
    // Inert for every earlier case: no earlier SQL names them.
    public static final String LOCK_TABLE_REINV = "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion";
    private static final Pattern CALL_LOCK_REINV = Pattern.compile("(?is)^\\s*CALL\\s+\"[^\"]*::(CrearBloqueoProcesarReinversion|EliminarBloqueoReinversion)\"");
    private static final Pattern CREATE_TMP = Pattern.compile("(?is)^\\s*CREATE\\s+LOCAL\\s+TEMPORARY\\s+TABLE\\s+(#\\w+)");
    private static final Pattern DROP_TMP = Pattern.compile("(?is)^\\s*DROP\\s+TABLE\\s+(#\\w+)");

    private final Connection raw;
    private final String url;
    private final String clock;
    private final Set<String> localCreated = new TreeSet<>();
    private final List<String> autonomous = new ArrayList<>();

    private SessionConnection(Connection raw, String url, String clock) {
        this.raw = raw; this.url = url; this.clock = clock;
    }

    public static SessionConnection of(Connection raw, String url, String clock) { return new SessionConnection(raw, url, clock); }

    public Set<String> localTablesCreated() { return localCreated; }

    /** autonomous commits performed (for the dump): "INSERT n" / "DELETE n" lines */
    public List<String> autonomousLog() { return autonomous; }

    public Connection connection() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("prepareStatement") || n.equals("prepareCall")) && a != null && a.length > 0 && a[0] instanceof String) {
                String sql = (String) a[0];
                if (n.equals("prepareCall") && CALL_LOCK.matcher(sql).find()) return lockCall(sql);
                if (n.equals("prepareCall") && CALL_LOCK_REINV.matcher(sql).find()) return ReinversionLock.call(this, raw, url, sql.contains("::CrearBloqueo"));
                if (n.equals("prepareCall") && TesoreriaH2.activo() && TesoreriaH2.esProcedimiento(sql)) return TesoreriaH2.procedimiento(sql);
                a = a.clone(); a[0] = translate(sql);
                return invoke(raw, m, a);
            }
            if (n.equals("createStatement")) return statement((Statement) invoke(raw, m, a));
            return invoke(raw, m, a);
        });
    }

    String translate(String sql) {
        String s = sql.replaceAll("\\bCURRENT_UTCTIMESTAMP\\b(?!\\s*\\()", "TIMESTAMP '" + clock + "'");
        s = s.replaceAll("(\"[^\"]+\")\\.NEXTVAL", "NEXT VALUE FOR $1");
        s = s.replaceAll("(?<=[\\s(,=>])#(?=[A-Za-z_])", "\"#").replaceAll("(\"#\\w+)", "$1\"");
        return TesoreriaH2.activo() ? TesoreriaH2.traducir(s) : s;   // F2 W1B: Tesoreria's dialect bridge, only for SQL issued by cl.bcs.tesoreria.*
    }

    private Statement statement(Statement st) {
        return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Statement.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery") || n.equals("addBatch")) && a != null && a.length > 0 && a[0] instanceof String) {
                String sql = (String) a[0];
                java.util.regex.Matcher c = CREATE_TMP.matcher(sql), d = DROP_TMP.matcher(sql);
                if (c.find() || d.find()) {
                    boolean create = c.reset().find();
                    String t = create ? c.group(1) : d.group(1);
                    if (create ? !localCreated.add(t) : !localCreated.remove(t)) {
                        throw new SQLException("oracle session: " + (create ? "cannot use duplicate table name: " : "invalid table name: ") + t);
                    }
                    raw.createStatement().executeUpdate("DELETE FROM \"" + t + "\"");
                    return n.equals("execute") ? Boolean.FALSE : n.equals("executeUpdate") ? (Object) Integer.valueOf(0) : null;
                }
                a = a.clone(); a[0] = translate(sql);
            }
            return invoke(st, m, a);
        });
    }

    private CallableStatement lockCall(String sql) {
        final boolean crear = CALL_LOCK.matcher(sql).find() && sql.contains("::CrearBloqueo");
        return (CallableStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { CallableStatement.class }, (p, m, a) -> {
            switch (m.getName()) {
                case "execute": case "executeUpdate": autonomousApply(crear); return m.getName().equals("execute") ? (Object) Boolean.FALSE : (Object) Integer.valueOf(0);
                case "getUpdateCount": return -1;
                case "getMoreResults": case "isClosed": return false;
                case "getResultSet": return null;
                default: return m.getReturnType().isPrimitive() ? (m.getReturnType() == boolean.class ? (Object) false : (Object) 0) : null;
            }
        });
    }

    /** the autonomous transaction: second connection, autocommit, commits immediately */
    private void autonomousApply(boolean crear) throws SQLException {
        if (!localCreated.contains("#p_BloqueoSolicitudAporteRescate")) {
            throw new SQLException("oracle session: invalid table name: #p_BloqueoSolicitudAporteRescate (not created in this session)");
        }
        try (Connection au = DriverManager.getConnection(url, "sa", ""); Statement src = raw.createStatement();
             ResultSet rs = src.executeQuery("SELECT * FROM \"#p_BloqueoSolicitudAporteRescate\"")) {
            au.setAutoCommit(true);
            int n = 0;
            while (rs.next()) {
                if (crear) {
                    try (PreparedStatement ps = au.prepareStatement("INSERT INTO \"" + LOCK_TABLE + "\" (\"IdBloqueoSolicitudAporteRescate\",\"IdPortafolio\","
                            + "\"IdSerieFondo\",\"IdRegimenTributario\",\"TipoSolicitud\",\"Monto\",\"NumeroCuotas\",\"FechaSistema\") VALUES (?,?,?,?,?,?,?,?)")) {
                        for (int i = 1; i <= 8; i++) ps.setObject(i, rs.getObject(i));
                        n += ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = au.prepareStatement("DELETE FROM \"" + LOCK_TABLE + "\" WHERE \"IdBloqueoSolicitudAporteRescate\" = ?")) {
                        ps.setObject(1, rs.getObject(1));
                        n += ps.executeUpdate();
                    }
                }
            }
            autonomous.add((crear ? "INSERT " : "DELETE ") + n + " (committed on its own connection)");
        }
    }

    private static Object invoke(Object target, Method m, Object[] a) throws Throwable {
        try {
            return m.invoke(target, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
