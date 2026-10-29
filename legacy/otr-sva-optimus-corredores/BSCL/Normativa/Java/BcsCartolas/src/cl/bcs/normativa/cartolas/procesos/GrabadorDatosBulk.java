package cl.bcs.normativa.cartolas.procesos;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.dal.Nombres;
import cl.bcs.normativa.cartolas.entidades.Datos;
import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.sql.utils.SqlUtil;

/**
 * Grabador de los datos.
 *
 * @author fpino@email.com
 *
 */
final class GrabadorDatosBulk extends SqlBulk {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GrabadorDatosBulk.class.getName());

    /**
     * Arma el comando INSERT de grabado.
     *
     * @param contexto
     *            El contexto.
     * @return El comando INSERT de grabado.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String armarInsert(
            final ContextoCartolas contexto)
                    throws SQLException {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("INSERT INTO ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.TB_DATOS)).append('(')
                .append(SqlUtil.toQuotedString("IdCartola")).append(',')
                .append(SqlUtil.toQuotedString("Nombre")).append(',')
                .append(SqlUtil.toQuotedString("Formato")).append(',')
                .append(SqlUtil.toQuotedString("Valor")).append(") VALUES(?,?,?,?)");
        final String sql = sb.toString();
        LOGGER.info(sql);
        return sql;
    }

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public GrabadorDatosBulk(
            final ContextoCartolas contexto) throws SQLException {
        super(contexto.getConnection(), armarInsert(contexto),
                contexto.getConfigs().getInsertBatchSize());
    }

    /**
     * Recibe un registro de datos para ser grabado.
     * <p>
     * Potencialmente, puede grabar un lote de registros, pues el grabado
     * est&aacute; implementado en modo bulk.
     *
     * @param datos
     *            El registro de datos a grabar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void grabar(
            final Datos datos)
                    throws SQLException {
        final PreparedStatement bulkStatement = getBulkStatement();
        bulkStatement.clearParameters();
        bulkStatement.setLong(1, datos.getIdCartola());
        bulkStatement.setString(2, datos.getNombre());
        bulkStatement.setString(3, datos.getFormato());
        bulkStatement.setString(4, datos.getValor());
        addBatch();
    }

}
