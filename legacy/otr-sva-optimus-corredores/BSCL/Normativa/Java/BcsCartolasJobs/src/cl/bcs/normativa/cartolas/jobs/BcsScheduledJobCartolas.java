package cl.bcs.normativa.cartolas.jobs;

import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.procesos.ProcesoProgramacionCartolas;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.PropertiesUtil;

public class BcsScheduledJobCartolas extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsScheduledJobCartolas.class.getName());
    private static final String MY_NAME = "Proceso programado de cartolas";

    @Override
    public void onError(
            final Throwable t) {
        final String msg = String.format("%s: Error, JobId=%d", MY_NAME, getJobId());
        LOGGER.log(Level.SEVERE, msg, t);
    }

    @Override
    public void pause() {
        // Nada a proposito.
    }

    @Override
    public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
        String msg;

        msg = String.format("%s: Inicio, JobId=%d", MY_NAME, getJobId());
        LOGGER.log(Level.INFO, msg);

        try (final Connection conn = getDataSource().getConnection()) {
            final Properties properties = PropertiesUtil.newProperties(parameters);
            new ProcesoProgramacionCartolas(conn, properties).procesar();
        }

        msg = String.format("%s: Termino OK, JobId=%d", MY_NAME, getJobId());
        LOGGER.log(Level.INFO, msg);

        // CUIDADO: El objetivo es que el proceso se chequee cada minuto.
        // Si se disminuye la pausa es probable que se active mas de una vez.
        // Si se aumenta la pausa es probable que no se ejecute.
        Thread.sleep(30000l);
        
        return true;
    }

}
