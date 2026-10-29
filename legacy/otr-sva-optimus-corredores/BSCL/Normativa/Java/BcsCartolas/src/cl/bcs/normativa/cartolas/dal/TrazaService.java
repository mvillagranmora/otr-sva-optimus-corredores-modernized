package cl.bcs.normativa.cartolas.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Traza;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.utils.SqlUtil;

public final class TrazaService {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TrazaService.class.getName());

    public static void save(
            final Connection connection,
            final String schema,
            final Traza traza)
                    throws SQLException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("INSERT INTO ").append(SqlUtil.toQuotedString(schema)).append('.')
                .append(SqlUtil.toQuotedString(Nombres.TB_TRAZA)).append('(')
                .append(SqlUtil.toQuotedString("IdNomina")).append(',')
                .append(SqlUtil.toQuotedString("Mensaje")).append(") VALUES(?, ?)");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, traza.getIdNomina());
            pstmt.setString(2, traza.getMensaje());
            pstmt.executeUpdate();
        }
    }

    public static void save(
            final ContextoCartolas contexto,
            final Traza traza)
                    throws SQLException {
        save(contexto.getConnection(), contexto.getSchema(), traza);
    }

    private TrazaService() {
    }

}
