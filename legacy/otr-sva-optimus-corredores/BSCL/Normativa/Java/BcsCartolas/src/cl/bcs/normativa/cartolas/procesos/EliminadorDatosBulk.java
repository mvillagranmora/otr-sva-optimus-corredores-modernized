package cl.bcs.normativa.cartolas.procesos;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.dal.Nombres;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.sql.utils.SqlUtil;

final class EliminadorDatosBulk extends SqlBulk {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(EliminadorDatosBulk.class.getName());

    private static String armarDelete(
            final ContextoCartolas contexto) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("DELETE FROM ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.TB_DATOS)).append(" WHERE ")
                .append(SqlUtil.toQuotedString("IdCartola")).append("=?");
        final String sql = sb.toString();
        LOGGER.info(sql);
        return sql;
    }

    public EliminadorDatosBulk(
            final ContextoCartolas contexto) throws SQLException {
        super(contexto.getConnection(), armarDelete(contexto),
                contexto.getConfigs().getDeleteBatchSize());
    }

    public void eliminar(
            final Cartola cartola)
                    throws SQLException {
        LOGGER.info("Eliminando datos de la cartola: id=" + cartola.getId());
        final PreparedStatement bulkStatement = getBulkStatement();
        bulkStatement.clearParameters();
        bulkStatement.setLong(1, cartola.getId());
        addBatch();
    }

}
