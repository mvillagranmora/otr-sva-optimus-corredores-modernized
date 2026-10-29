package cl.bcs.oracle;

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

/**
 * F2 W1 front 2, H2 ONLY: stand-in of the two AUTONOMOUS TRANSACTION procedures of the reinversion lock of CrearSolicitudRescateReinversion, written from
 * their source (legacy BSCL/FondoMutuo/BD/Servicios/CrearBloqueoProcesarReinversion.sql and EliminarBloqueoReinversion.sql, read only):
 * <ul>
 * <li>CrearBloqueoProcesarReinversion(p_BloqueoReinversion => #p_BloqueoProcesarReinversion): ONE {@code INSERT INTO FMM.ProcesarReinversionRescate_BloqueoReinversion
 *     (9 columns) SELECT ... FROM :p_BloqueoReinversion} inside BEGIN AUTONOMOUS TRANSACTION: applied on a SECOND connection and committed at once (all rows
 *     or none: one statement);</li>
 * <li>EliminarBloqueoReinversion: {@code DELETE ... WHERE IdBloqueaReinversion IN (SELECT IdBloqueaReinversion FROM :p_BloqueoReinversion)}, same way.</li>
 * </ul>
 * The session-local table must have been created in this session (else the HANA "invalid table name"). The rows are read on the request's connection (they
 * are uncommitted there, like a HANA local table that only its session sees). The persistent table types are the catalog ones (schema-rre.sql).
 */
final class ReinversionLock {
    static final String LOCAL = "#p_BloqueoProcesarReinversion";
    private static final String[] COLS = { "IdBloqueaReinversion", "IdPortafolio", "IdRegimenTributario", "IdSerieFondo", "Monto", "NumeroCuotas", "FechaSistema",
            "IdCliente", "IdGrupoRegimen" };

    private ReinversionLock() { }

    static CallableStatement call(SessionConnection session, Connection raw, String url, boolean crear) {
        return (CallableStatement) Proxy.newProxyInstance(ReinversionLock.class.getClassLoader(), new Class<?>[] { CallableStatement.class }, (p, m, a) -> {
            switch (m.getName()) {
                case "execute": case "executeUpdate": apply(session, raw, url, crear); return m.getName().equals("execute") ? (Object) Boolean.FALSE : (Object) Integer.valueOf(0);
                case "getUpdateCount": return -1;
                case "getMoreResults": case "isClosed": return false;
                case "getResultSet": return null;
                default: return m.getReturnType().isPrimitive() ? (m.getReturnType() == boolean.class ? (Object) false : (Object) 0) : null;
            }
        });
    }

    private static void apply(SessionConnection session, Connection raw, String url, boolean crear) throws SQLException {
        if (!session.localTablesCreated().contains(LOCAL)) {
            throw new SQLException("oracle session: invalid table name: " + LOCAL + " (not created in this session)");
        }
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
                if (crear) {
                    StringBuilder q = new StringBuilder("INSERT INTO \"" + SessionConnection.LOCK_TABLE_REINV + "\" (");
                    for (int i = 0; i < COLS.length; i++) q.append(i > 0 ? "," : "").append('"').append(COLS[i]).append('"');
                    q.append(") VALUES (?,?,?,?,?,?,?,?,?)");
                    try (PreparedStatement ps = au.prepareStatement(q.toString())) {
                        for (Object[] f : filas) {
                            for (int i = 0; i < COLS.length; i++) ps.setObject(i + 1, f[i]);
                            n += ps.executeUpdate();
                        }
                    }
                } else {
                    try (PreparedStatement ps = au.prepareStatement("DELETE FROM \"" + SessionConnection.LOCK_TABLE_REINV + "\" WHERE \"IdBloqueaReinversion\" = ?")) {
                        for (Object[] f : filas) {
                            ps.setObject(1, f[0]);
                            n += ps.executeUpdate();
                        }
                    }
                }
                au.commit();
            } catch (SQLException e) {
                au.rollback();   // one statement in the procedure: all rows or none
                throw e;
            }
        }
        session.autonomousLog().add("REINVERSION " + (crear ? "INSERT " : "DELETE ") + n + " (committed on its own connection)");
    }
}
