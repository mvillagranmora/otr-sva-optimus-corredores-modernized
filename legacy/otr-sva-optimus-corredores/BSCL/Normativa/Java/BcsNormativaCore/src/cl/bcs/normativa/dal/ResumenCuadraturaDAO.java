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
 * M&eacute;todos para acceder a la tabla ResumenCuadratura.
 *
 * @author fpino@email.com
 *
 */
public final class ResumenCuadraturaDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ResumenCuadraturaDAO.class.getName());

    /**
     * Elimina todos los resumenes de cuadratura asociadas a un documento por
     * tipo de cuadratura y clasificador.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param idDocumento
     *            El id del documento.
     * @param tipoCuadratura
     *            El tipo de cuadratura.
     * @param tipoClasificador
     *            El tipo de clasificador (puede ser <code>null</code>).
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void eliminarPorIdDocumento(final Connection conn, final String schema,
            final long idDocumento, final String tipoCuadratura, final String tipoClasificador)
                    throws SQLException {

        final SqlDelete delete = new SqlDelete(
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_CUADRATURA));
        delete.getWhere().put("IdDocumento", idDocumento);
        delete.getWhere().put("TipoCuadratura", tipoCuadratura);
        delete.getWhere().put("TipoClasificador", tipoClasificador);

        final String sql = GENERATOR.getSqlText(delete);
        LOGGER.info(sql);
        SqlUtil.executeUpdate(conn, sql);
    }

    private ResumenCuadraturaDAO() {
    }

}
