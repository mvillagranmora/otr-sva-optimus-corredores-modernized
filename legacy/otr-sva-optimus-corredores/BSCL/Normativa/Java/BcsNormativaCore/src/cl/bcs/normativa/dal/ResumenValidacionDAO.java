package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.statements.SqlDelete;
import cl.bcs.sql.utils.SqlUtil;

/**
 * M&eacute;todos para acceder a la tabla ResumenValidacion.
 *
 * @author fpino@email.com
 *
 */
public final class ResumenValidacionDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ResumenValidacionDAO.class.getName());

    /**
     * Elimina todos los resumenes de validaci&oacute;n asociadas a un
     * documento.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param idDocumento
     *            El id del documento.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void eliminarPorIdDocumento(final Connection conn, final String schema,
            final long idDocumento) throws SQLException {

        final SqlDelete delete = new SqlDelete(
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_VALIDACION));
        delete.getWhere().put("IdDocumento", idDocumento);

        final String sql = GENERATOR.getSqlText(delete);
        LOGGER.info(sql);
        SqlUtil.executeUpdate(conn, sql);
    }

    private ResumenValidacionDAO() {
    }

}
