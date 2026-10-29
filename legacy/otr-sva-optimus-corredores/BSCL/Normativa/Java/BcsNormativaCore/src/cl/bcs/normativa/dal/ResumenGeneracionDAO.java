package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.ResumenGeneracion;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.statements.SqlInsert;
import cl.bcs.sql.statements.SqlUpdate;
import cl.bcs.sql.utils.SqlUtil;

/**
 * M&eacute;todos para acceder a la tabla ResumenGeneracion.
 *
 * @author fpino@email.com
 *
 */
public  class ResumenGeneracionDAO {

    /**
     * Instancia del generador SQL.
     */
    private static SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ResumenGeneracionDAO.class.getName());

    /**
     * Actualiza un registro de resumen de generaci&oacute;n.
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
            final ResumenGeneracion resumen) throws SQLException {

        final SqlUpdate update = new SqlUpdate(
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_GENERACION));
        update.getValues().put("Glosa", resumen.getGlosa());
        update.getValues().put("TotalRegistrosGenerados", resumen.getTotalRegistrosGenerados());
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
             ResumenGeneracion resumen) throws SQLException {

        SecuenciadorRegistro secuenciador = new SecuenciadorRegistro(conn,
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_GENERACION));
        resumen.setId(secuenciador.getSiguiente());

        SqlInsert insert = new SqlInsert(
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_GENERACION));
        insert.getValues().put("ID", resumen.getId());
        insert.getValues().put("IdProceso", resumen.getIdProceso());
        insert.getValues().put("IdDocumento", resumen.getIdDocumento());
        insert.getValues().put("IdUsuario", resumen.getIdUsuario());
        insert.getValues().put("Fecha", resumen.getFecha());
        insert.getValues().put("Glosa", resumen.getGlosa());
        insert.getValues().put("NombreArchivo", resumen.getNombreArchivo());
        insert.getValues().put("TotalRegistrosGenerados", resumen.getTotalRegistrosGenerados());
        insert.getValues().put("UltimoCorrelativo", resumen.getUltimoCorrelativo());
        ///podria existir una duplicidad de insercion al ejecutarse N veces una solicitud por job
        try {
        String sql = GENERATOR.getSqlText(insert);
        LOGGER.info(sql);
        SqlUtil.executeUpdate(conn, sql);
        } catch (Exception e) {};
        
        
        
       
    }

    private ResumenGeneracionDAO() {
    }

}
