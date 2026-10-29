package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.exportacion.ProcesoExportacion;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.scheduler.BcsJob;

public final class BcsJobExportacion extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobExportacion.class.getName());

    @Override
    public void onError(
            final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de exportacion", t);
    }

    @Override
    public void pause() {
    }

    @Override
    public boolean transaction(
            final Connection conn)
                    throws InterruptedException,
                    Throwable {
        LOGGER.log(Level.INFO, "Proceso de exportacion activado con los parametros {0}",
                parameters);

        final Proceso proceso = ProcesoExportacion.newInstance(conn, parameters, getJobId(), getDataSource());
        proceso.procesar();

        LOGGER.log(Level.INFO, "Proceso de exportacion finalizado OK");
        return true;
    }

}
