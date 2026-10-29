package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.consolidacli.ProcesoConsolidaCli;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.scheduler.BcsJob;

public final class BcsJobConsolidaCli extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobConsolidaCli.class.getName());

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
        LOGGER.log(Level.INFO, "Proceso de consolidacion activado con los parametros {0}",
                parameters);

        final Proceso proceso = ProcesoConsolidaCli.newInstance(conn, getJobId());
        proceso.procesar();

        LOGGER.log(Level.INFO, "Proceso de consolidacion finalizado OK");
        return true;
    }

}
