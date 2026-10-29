package cl.bcs.normativa.cartolas.procesos;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.caller.CallParams;
import cl.bcs.normativa.cartolas.caller.CallTable;
import cl.bcs.normativa.cartolas.caller.ProcedureCaller;
import cl.bcs.normativa.cartolas.dal.TrazaService;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.Recurso;
import cl.bcs.normativa.cartolas.entidades.TipoRecurso;
import cl.bcs.normativa.cartolas.entidades.Traza;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;

public final class ProcesoCargaDatos implements Proceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoCargaDatos.class.getName());

    public static Proceso newInstance(
            final Connection connection,
            final Properties properties,
            final long idProceso)
                    throws SQLException {
        return new ProcesoCargaDatos(connection, properties, idProceso);
    }

    public static ProcesoCargaDatos newInstance(
            final ContextoCartolas contexto) {
        return new ProcesoCargaDatos(contexto);
    }

    private final ContextoCartolas contexto;

    public ProcesoCargaDatos(
            final Connection connection,
            final Properties properties,
            final long idProceso) throws SQLException {
        this(new ContextoCartolas(connection, properties, idProceso));
    }

    public ProcesoCargaDatos(
            final ContextoCartolas contexto) {
        this.contexto = contexto;
    }

    private void call(
            final Recurso recurso,
            final CallParams params,
            final CargadorDatos cargador,
            final String tempName)
                    throws SQLException,
                    IOException,
                    ParseException {
        final String dataSourceName = recurso.getNombre();
        final String procedureName = recurso.getValor();
        ProcedureCaller.call(contexto, procedureName, params, cargador, dataSourceName, tempName);
    }

    private void cargarDatos(
            final long start)
                    throws SQLException {
        final GrabadorDatosBulk grabador = new GrabadorDatosBulk(contexto);
        final CargadorDatos cargador = new CargadorDatosPersistentes(contexto, grabador);

        final Nomina nomina = contexto.getNomina();
        final int size = contexto.getCartolas().size();
        final int tope = size - 1;
        final int lote = contexto.getConfigs().getCallerBatchSize();

        for (int desde = 0; desde < size; desde += lote) {
            int hasta = desde + lote - 1;
            if (hasta > tope) {
                hasta = tope;
            }

            final CallParams params = new CallParams(contexto.getNomina(), contexto.getCartolas(),
                    desde, hasta);
            final CallTable table = CallTable.newInstance(contexto.getConnection());
            table.load(params);

            try {
                for (final Recurso recurso : nomina.getPlantilla().getRecursos()) {
                    if (recurso.getTipo().compareTo(TipoRecurso.PROCEDIMIENTO) == 0) {
                        logProgress(grabador, start);

                        if (LOGGER.isLoggable(Level.INFO)) {
                            final String msg = String.format("Procesando %d-%d: %s", desde, hasta,
                                    recurso.getNombre());
                            LOGGER.info(msg);
                        }

                        try {
                            call(recurso, params, cargador, table.getName());
                        } catch (IOException | ParseException | SQLException ex) {
                            trace(ex);
                        }
                    }
                }
            } finally {
                table.drop();
            }
        }

        grabador.endBatch();
        logProgress(grabador, start);
    }

    private void eliminarDatos()
            throws SQLException {
        final EliminadorDatosBulk eliminador = new EliminadorDatosBulk(contexto);
        for (final Cartola cartola : contexto.getCartolas()) {
            eliminador.eliminar(cartola);
        }

        eliminador.endBatch();
    }

    private void logProgress(
            final GrabadorDatosBulk grabador,
            final long start) {
        if (LOGGER.isLoggable(Level.INFO)) {
            String msg = String.format("Commited: %d", grabador.getCommited());
            LOGGER.info(msg);

            final long elapsed = (System.currentTimeMillis() - start) / 1000;
            msg = String.format("Elapsed: %s", elapsed);
            LOGGER.info(msg);
        }
    }

    @Override
    public void procesar()
            throws ProcesoException {
        final long start = System.currentTimeMillis();

        try {
            contexto.getConnection().setAutoCommit(false);
            eliminarDatos();
            cargarDatos(start);
        } catch (SQLException ex) {
            final String msg = "Error al procesar carga de datos de cartolas";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProcesoException(msg, ex);
        }
    }

    private void trace(
            final Exception ex)
                    throws SQLException {
        final String msg = ex.getMessage();
        LOGGER.severe(msg);
        final Traza traza = new Traza();
        traza.setIdNomina(contexto.getNomina().getId());
        traza.setMensaje(msg);
        TrazaService.save(contexto, traza);
    }

}
