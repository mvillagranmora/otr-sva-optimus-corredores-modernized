package cl.bcs.normativa.cartolas.batch;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public class Batch {

    public static final String ADD_PROGRAM_PROCEDURE_NAME = "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma";

    public static void createParamsTable(
            final Connection conn,
            final String tableName)
                    throws SQLException {
        final String sql = String.format(
                "CREATE LOCAL TEMPORARY TABLE %s(%s NVARCHAR(100), %s NVARCHAR(100))",
                getQuotedString(tableName), getQuotedString("NombreParametro"),
                getQuotedString("Valor"));

        try (final Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void dropTable(
            final Connection conn,
            final String tableName)
                    throws SQLException {
        final String sql = String.format("DROP TABLE %s", getQuotedString(tableName));

        try (final Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static BatchParams getBatchParams(
            final long idEmpresa,
            final long idUsuario) {
        final BatchParams batchParams = new BatchParams();
        batchParams.setCreaParam("S");
        batchParams.setIdAgenda(0);
        batchParams.setIdEmpresa(idEmpresa);
        batchParams.setIdModulo(ContextoCartolas.MODULO_CARTOLAS);
        batchParams.setIdTipoTarea("ProcesoCartolas");
        batchParams.setIdUsuario(idUsuario);
        batchParams.setSegundos(0);
        return batchParams;
    }

    private static String getQuotedString(
            final String name) {
        return "\"" + name + "\"";
    }

    public static String getTemporaryName() {
        final Date now = new Date();
        final long time = now.getTime();
        return "#temp" + time;
    }

    public static void registerParams(
            final Connection conn,
            final String tableName,
            final Map<String, String> map)
                    throws SQLException {
        final String sql = String.format("INSERT INTO %s VALUES(?, ?)", getQuotedString(tableName));

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            for (final String key : map.keySet()) {
                ps.setString(1, key);
                ps.setString(2, map.get(key));
                ps.executeUpdate();
            }
        }
    }

    public static long scheduleProcess(
            final Connection conn,
            final long idEmpresa,
            final long idUsuario,
            final String schema,
            final Map<String, String> map)
                    throws SQLException {

        final String tempName = getTemporaryName();
        createParamsTable(conn, tempName);
        registerParams(conn, tempName, map);

        final BatchParams batchParams = getBatchParams(idEmpresa, idUsuario);
        final String sql = String.format("{CALL %s.%s(?,?,?,?,?,?,%s,?,?,?,?,?,?)}",
                getQuotedString(schema), getQuotedString(ADD_PROGRAM_PROCEDURE_NAME),
                getQuotedString(tempName));

        final boolean autoCommit = conn.getAutoCommit();

        long ret = -1;
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            conn.setAutoCommit(false);

            cstmt.setLong(1, batchParams.getIdEmpresa());
            cstmt.setLong(2, batchParams.getIdUsuario());
            cstmt.setString(3, batchParams.getIdModulo());
            cstmt.setLong(4, batchParams.getIdAgenda());
            cstmt.setString(5, batchParams.getIdTipoTarea());
            cstmt.setLong(6, batchParams.getSegundos());
            cstmt.setString(7, batchParams.getCreaParam());
            cstmt.registerOutParameter(8, Types.INTEGER);
            cstmt.registerOutParameter(9, Types.INTEGER);

            if (cstmt.execute()) {
                conn.commit();
                try (final ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        ret = rs.getLong(1);
                    }
                }
            }
        } finally {
            conn.setAutoCommit(autoCommit);
        }

        if (ret < 0) {
            throw new BatchException("No se logro programar ejecucion batch");
        }

        return ret;
    }

}
