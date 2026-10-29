package cl.bcs.fondomutuo.normal;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 stand-in for what the module gets from HANA at the JDBC boundary, the SAME emulation the legacy oracle applies to the legacy (its
 * {@code SessionConnection}, copied: it is the harness, not the subject; H2 ONLY, never HANA): the two AUTONOMOUS TRANSACTION lock procedures run on a
 * SECOND connection in autocommit (they commit at once, outside the transaction of the request), {@code CREATE LOCAL TEMPORARY TABLE #x} / {@code DROP TABLE #x}
 * are session state (the table is pre-created by schema-session.sql), and the HANA dialect pieces of the SQL are rewritten.
 */
public final class SesionHana {

    static final String LOCK_TABLE = "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate";
    private static final Pattern CALL_LOCK = Pattern.compile("(?is)^\\s*CALL\\s+\"[^\"]*::(Crear|Eliminar)BloqueoSolicitudAporteRescate\"");
    private static final Pattern CREATE_TMP = Pattern.compile("(?is)^\\s*CREATE\\s+LOCAL\\s+TEMPORARY\\s+TABLE\\s+(#\\w+)");
    private static final Pattern DROP_TMP = Pattern.compile("(?is)^\\s*DROP\\s+TABLE\\s+(#\\w+)");

    private final Connection raw;
    private final String url;
    private final String reloj;
    private final Set<String> localCreated = new TreeSet<>();
    private final List<String> autonomous = new ArrayList<>();
    /** Off by default (the goldens were recorded without it). On = emulate the implicit COMMIT of the lock DDL that HANA does. */
    boolean commitEnDdl;
    /** F2 W1B: the layer-2 cases (Tesoreria library real) set it to the case script; null = off (every earlier case). See {@link TesoreriaSesion}. */
    cl.bcs.fondomutuo.cierre3b.Guion tesoreria;

    public SesionHana(Connection raw, String url, String reloj) {
        this.raw = raw;
        this.url = url;
        this.reloj = reloj;
    }

    public Set<String> tablasLocalesSinDrop() {
        return localCreated;
    }

    public List<String> commitsAutonomos() {
        return autonomous;
    }

    public Connection conexion() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("prepareStatement") || n.equals("prepareCall")) && a != null && a.length > 0 && a[0] instanceof String) {
                String sql = (String) a[0];
                if (n.equals("prepareCall") && CALL_LOCK.matcher(sql).find()) {
                    return lockCall(sql);
                }
                if (n.equals("prepareCall") && tesoreria != null && TesoreriaSesion.esProcedimiento(sql)) {
                    return TesoreriaSesion.procedimiento(tesoreria);
                }
                a = a.clone();
                a[0] = traducir(sql);
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
        return tesoreria != null ? TesoreriaSesion.traducir(s) : s;   // F2 W1B: Tesoreria's dialect bridge, only for SQL issued by cl.bcs.tesoreria.*
    }

    private Statement sentencia(Statement st) {
        return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Statement.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery") || n.equals("addBatch")) && a != null && a.length > 0
                    && a[0] instanceof String) {
                String sql = (String) a[0];
                Matcher c = CREATE_TMP.matcher(sql);
                Matcher d = DROP_TMP.matcher(sql);
                boolean crear = c.find();
                if (crear || d.find()) {
                    String t = crear ? c.group(1) : d.group(1);
                    if (crear ? !localCreated.add(t) : !localCreated.remove(t)) {
                        throw new SQLException("session: " + (crear ? "cannot use duplicate table name: " : "invalid table name: ") + t);
                    }
                    raw.createStatement().executeUpdate("DELETE FROM \"" + t + "\"");
                    if (commitEnDdl) {
                        raw.commit();   // HANA (measured, HANA_DDL_COMMIT_HALLAZGO): CREATE/DROP of a local temporary table commits the open transaction; H2 does not
                    }
                    return n.equals("execute") ? Boolean.FALSE : n.equals("executeUpdate") ? (Object) Integer.valueOf(0) : null;
                }
                a = a.clone();
                a[0] = traducir(sql);
            }
            return invocar(st, m, a);
        });
    }

    private CallableStatement lockCall(String sql) {
        final boolean crear = sql.contains("::CrearBloqueo");
        return (CallableStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { CallableStatement.class }, (p, m, a) -> {
            switch (m.getName()) {
            case "execute":
            case "executeUpdate":
                autonomo(crear);
                return m.getName().equals("execute") ? (Object) Boolean.FALSE : (Object) Integer.valueOf(0);
            case "getUpdateCount":
                return -1;
            case "getMoreResults":
            case "isClosed":
                return false;
            default:
                return m.getReturnType().isPrimitive() ? (m.getReturnType() == boolean.class ? (Object) false : (Object) 0) : null;
            }
        });
    }

    private void autonomo(boolean crear) throws SQLException {
        if (!localCreated.contains("#p_BloqueoSolicitudAporteRescate")) {
            throw new SQLException("session: invalid table name: #p_BloqueoSolicitudAporteRescate (not created in this session)");
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

    private static Object invocar(Object target, Method m, Object[] a) throws Throwable {
        try {
            return m.invoke(target, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
