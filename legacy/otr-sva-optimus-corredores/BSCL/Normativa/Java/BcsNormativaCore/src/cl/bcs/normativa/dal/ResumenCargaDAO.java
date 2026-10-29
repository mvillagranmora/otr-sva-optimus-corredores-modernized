package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.ResumenCarga;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.statements.SqlInsert;
import cl.bcs.sql.statements.SqlUpdate;
import cl.bcs.sql.utils.SqlUtil;

/**
 * M&eacute;todos para acceder a la tabla ResumenCarga.
 *
 * @author fpino@email.com
 *
 */
public final class ResumenCargaDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ResumenCargaDAO.class.getName());

    /**
     * Actualiza un registro de resumen de carga.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param resumen
     *            El resumen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void actualizar(final Connection conn, final String schema,
            final ResumenCarga resumen) throws SQLException {

        final SqlUpdate update = new SqlUpdate(
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_CARGA));
        update.getValues().put("Glosa", resumen.getGlosa());
        update.getValues().put("TotalRegistrosLeidos", resumen.getTotalRegistrosLeidos());
        update.getValues().put("TotalRegistrosCargados", resumen.getTotalRegistrosCargados());
        update.getValues().put("TotalRegistrosErroneos", resumen.getTotalRegistrosErroneos());
        update.getWhere().put("ID", resumen.getId());

        final String sql = GENERATOR.getSqlText(update);
        LOGGER.info(sql);
        SqlUtil.executeUpdate(conn, sql);
    }

    /**
     * Inserta nuevo registro de resumen, previa asignaci&oacute;n del id.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param resumen
     *            El resumen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void insertar(final Connection conn, final String schema,
            final ResumenCarga resumen) throws SQLException {

        final SecuenciadorRegistro secuenciador = new SecuenciadorRegistro(conn,
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_CARGA));
        resumen.setId(secuenciador.getSiguiente());

        final SqlInsert insert = new SqlInsert(
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_CARGA));
        insert.getValues().put("ID", resumen.getId());
        insert.getValues().put("IdProceso", resumen.getIdProceso());
        insert.getValues().put("IdDocumento", resumen.getIdDocumento());
        insert.getValues().put("IdUsuario", resumen.getIdUsuario());
        insert.getValues().put("Fecha", resumen.getFecha());
        insert.getValues().put("Glosa", resumen.getGlosa());
        insert.getValues().put("NombreArchivo", resumen.getNombreArchivo());
        insert.getValues().put("TotalRegistrosLeidos", resumen.getTotalRegistrosLeidos());
        insert.getValues().put("TotalRegistrosCargados", resumen.getTotalRegistrosCargados());
        insert.getValues().put("TotalRegistrosErroneos", resumen.getTotalRegistrosErroneos());

        final String sql = GENERATOR.getSqlText(insert);
        LOGGER.info(sql);
        SqlUtil.executeUpdate(conn, sql);
    }

    private ResumenCargaDAO() {
    }

}
