package cl.bcs.normativa.cartolas.caller;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.procesos.CargadorDatos;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.utils.SqlUtil;

public final class ProcedureCaller {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcedureCaller.class.getName());

    public static void call(
            final Connection conn,
            final String schema,
            final String modulo,
            final String procedureName,
            final CallParams params,
            final CargadorDatos cargador,
            final String dataSourceName,
            final String tempName)
                    throws SQLException,
                    IOException,
                    ParseException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(schema)).append('.')
                .append(SqlUtil.toQuotedString(procedureName)).append("(?,?,?,?,?,")
                .append(SqlUtil.toQuotedString(tempName)).append(")}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setFetchSize(10000);
            cstmt.setString(1, modulo);
            cstmt.setLong(2, params.getNomina().getIdUsuario());
            cstmt.setLong(3, params.getNomina().getIdEmpresa());
            cstmt.setDate(4, params.getNomina().getFechaDesde());
            cstmt.setDate(5, params.getNomina().getFechaHasta());
            cstmt.execute();

            cargador.load(dataSourceName, cstmt.getResultSet());
        }
    }

    public static void call(
            final ContextoCartolas contexto,
            final String procedureName,
            final CallParams params,
            final CargadorDatos cargador,
            final String dataSourceName,
            final String tempName)
                    throws SQLException,
                    IOException,
                    ParseException {
        call(contexto.getConnection(), contexto.getSchema(), contexto.getModulo(), procedureName,
                params, cargador, dataSourceName, tempName);

    }

    private ProcedureCaller() {
    }

}
