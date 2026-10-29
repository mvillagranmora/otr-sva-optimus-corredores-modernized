package cl.bcs.fondomutuo.rescatereinversion;

import java.lang.reflect.InvocationTargetException;
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
import java.util.regex.Pattern;

import cl.bcs.fondomutuo.normal.SesionHana;

/**
 * The H2 session of the FMM-RRE cases: Normal's {@link SesionHana} (the oracle's {@code SessionConnection}: local tables, dialect, Normal's lock) plus the
 * SAME stand-in of the two AUTONOMOUS reinversion lock procedures that the oracle applies to the legacy ({@code runner/cl/bcs/oracle/ReinversionLock.java},
 * copied: harness, not subject; H2 ONLY): the rows of {@code #p_BloqueoProcesarReinversion} are applied on a SECOND connection and committed at once
 * (all or none), and logged as "REINVERSION INSERT n / DELETE n".
 */
public final class SesionRre {

    static final String LOCAL = "#p_BloqueoProcesarReinversion";
    static final String LOCK_TABLE = "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion";
    private static final Pattern CALL = Pattern.compile("(?is)^\\s*CALL\\s+\"[^\"]*::(CrearBloqueoProcesarReinversion|EliminarBloqueoReinversion)\"");
    private static final String[] COLS = { "IdBloqueaReinversion", "IdPortafolio", "IdRegimenTributario", "IdSerieFondo", "Monto", "NumeroCuotas", "FechaSistema",
            "IdCliente", "IdGrupoRegimen" };

    private final SesionHana base;
    private final Connection raw;
    private final String url;
    /** Test hook (canaries / failure tests): make the CREATE procedure fail. */
    volatile boolean fallarCrear;
    volatile boolean fallarEliminar;

    public SesionRre(Connection raw, String url, String reloj) {
        this.base = new SesionHana(raw, url, reloj);
        this.raw = raw;
        this.url = url;
    }

    public SesionHana base() {
        return base;
    }

    public Connection conexion() {
        Connection inner = base.conexion();
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if (m.getName().equals("prepareCall") && a != null && a.length > 0 && a[0] instanceof String && CALL.matcher((String) a[0]).find()) {
                return llamada(((String) a[0]).contains("::CrearBloqueo"));
            }
            try {
                return m.invoke(inner, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    private CallableStatement llamada(boolean crear) {
        return (CallableStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { CallableStatement.class }, (p, m, a) -> {
            switch (m.getName()) {
            case "execute":
            case "executeUpdate":
                aplicar(crear);
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

    private void aplicar(boolean crear) throws SQLException {
        if (!base.tablasLocalesSinDrop().contains(LOCAL)) {
            throw new SQLException("oracle session: invalid table name: " + LOCAL + " (not created in this session)");
        }
        if (crear ? fallarCrear : fallarEliminar) throw new SQLException("session: procedure failure (test hook)");
        List<Object[]> filas = new ArrayList<>();
        try (Statement src = raw.createStatement(); ResultSet rs = src.executeQuery("SELECT * FROM \"" + LOCAL + "\"")) {
            while (rs.next()) {
                Object[] f = new Object[COLS.length];
                for (int i = 0; i < COLS.length; i++) f[i] = rs.getObject(COLS[i]);
                filas.add(f);
            }
        }
        int n = 0;
        try (Connection au = DriverManager.getConnection(url, "sa", "")) {
            au.setAutoCommit(false);
            try {
                String q = crear ? "INSERT INTO \"" + LOCK_TABLE + "\" (\"" + String.join("\",\"", COLS) + "\") VALUES (?,?,?,?,?,?,?,?,?)"
                        : "DELETE FROM \"" + LOCK_TABLE + "\" WHERE \"IdBloqueaReinversion\" = ?";
                try (PreparedStatement ps = au.prepareStatement(q)) {
                    for (Object[] f : filas) {
                        if (crear) {
                            for (int i = 0; i < COLS.length; i++) ps.setObject(i + 1, f[i]);
                        } else {
                            ps.setObject(1, f[0]);
                        }
                        n += ps.executeUpdate();
                    }
                }
                au.commit();
            } catch (SQLException e) {
                au.rollback();
                throw e;
            }
        }
        base.commitsAutonomos().add("REINVERSION " + (crear ? "INSERT " : "DELETE ") + n + " (committed on its own connection)");
    }
}
