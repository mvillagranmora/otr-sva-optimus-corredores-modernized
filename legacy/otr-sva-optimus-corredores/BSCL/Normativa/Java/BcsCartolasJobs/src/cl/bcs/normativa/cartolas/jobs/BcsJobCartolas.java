package cl.bcs.normativa.cartolas.jobs;

import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.tools.ProcesoCartolasApp;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.PropertiesUtil;

public final class BcsJobCartolas extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobCartolas.class.getName());
    private static final String MY_NAME = "Proceso de cartolas";

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

        final Properties properties = PropertiesUtil.newProperties(parameters);
        ProcesoCartolasApp.procesar(getDataSource(), properties, getJobId());

        msg = String.format("%s: Termino OK, JobId=%d", MY_NAME, getJobId());
        LOGGER.log(Level.INFO, msg);
        return true;
    }

}
