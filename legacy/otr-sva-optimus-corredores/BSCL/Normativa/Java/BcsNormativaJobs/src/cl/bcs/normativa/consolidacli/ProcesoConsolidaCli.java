package cl.bcs.normativa.consolidacli;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.exportacion.ProcesoExportacion;
import cl.bcs.normativa.generacion.ProcesoGeneracion;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;

/**
 * Implementaci&oacute;n de la exportaci&oacute;n de documentos.
 *
 *
 *
 */
public class ProcesoConsolidaCli implements Proceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoGeneracion.class.getName());

    private static String getQuotedString(final String name) {
        return "\"" + name + "\"";
    }

    /**
     * Obtiene instancia de {@link ProcesoExportacion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @return Instancia de {@link ProcesoExportacion}.
     * @throws SQLException
     *             En caso de error SQL.
     */

    public static Proceso newInstance(final Connection conn, final long idProceso)
            throws SQLException {

        return new ProcesoConsolidaCli(conn, idProceso);
        // return newInstance(conn, props, idProceso);
    }

    /**
     * Contexto de exportaci&oacute;n de datos.
     */
    private ContextoConsolidaCli contexto;

    /**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @return
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ProcesoConsolidaCli(final Connection conn, final long idProceso) throws SQLException {
        this.contexto = new ContextoConsolidaCli(conn, idProceso);
    }

    // PP
    private void consolidaClientes() throws SQLException {

        String ADD_PROGRAM_PROCEDURE_NAME = "BSCL.Normativa.BD.Servicios.NCG380::ConsolidaClientes";
        // ResumenGeneracion resumen;
        // resumen = ResumenGeneracionBuilder.crearResumen(contexto);

        String sqlCall = "";

        sqlCall = String.format("{CALL %s.%s()}", getQuotedString("BSCL"),
                getQuotedString(ADD_PROGRAM_PROCEDURE_NAME));
        System.out.println(sqlCall);

        LOGGER.log(Level.SEVERE, sqlCall);

        Connection conn = contexto.getConnection();

        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
            conn.setAutoCommit(false);

            if (cstmt.execute()) {
                conn.commit();

                try (final ResultSet rs = cstmt.getResultSet()) {

                    // resumen.setGlosa("Finalizada");

                } catch (Exception ex) {
                    // resumen.setGlosa("Interrumpida");
                    LOGGER.log(Level.SEVERE, "Error durante proceso de consolidacion de clientes.",
                            ex);
                } finally {
                    // ResumenGeneracionBuilder.actualizarResumen(contexto,
                    // resumen);
                }
            }

        } catch (Exception ex) {
            // resumen.setGlosa("Interrumpida");
            LOGGER.log(Level.SEVERE, "Error durante proceso de consolidacion de clientes.", ex);
        }

    }

    @Override
    public void procesar() throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso de consolidacion de clientes."));

        // try (final ControladorProceso controlador = new
        // ControladorProceso(contexto)) {
        try {
            consolidaClientes(); // PP
        } catch (SQLException ex) {
            final String msg = "Error durante proceso de consolidacion";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProcesoException(msg, ex);
        }
        // }
    }

}
