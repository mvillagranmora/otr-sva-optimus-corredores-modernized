package cl.bcs.normativa.cartolas.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Cliente;
import cl.bcs.normativa.dal.RecordNotFoundException;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class ComunService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ComunService.class.getName());

    public static Cliente getCliente(
            final Connection connection,
            final String schema,
            final long idCliente)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.VW_CLIENTE);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdCliente", idCliente);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Cliente.newInstance(rs);
                }
            }
        }

        return null;
    }

    public static String getCodigoPortafolio(
            final Connection connection,
            final String schema,
            final long idPortafolio)
                    throws SQLException {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("SELECT ").append(SqlUtil.toQuotedString("CodigoPortafolio")).append(" FROM ")
                .append(SqlUtil.toQuotedString(schema)).append('.')
                .append(SqlUtil.toQuotedString(Nombres.TB_PORTAFOLIO)).append(" WHERE ")
                .append(SqlUtil.toQuotedString("IdPortafolio")).append("=?");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, idPortafolio);
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        final String msg = String.format("No existe portafolio IdPortafolio=%d", idPortafolio);
        throw new RecordNotFoundException(msg);
    }
    
    
    private ComunService() {
    }
}
