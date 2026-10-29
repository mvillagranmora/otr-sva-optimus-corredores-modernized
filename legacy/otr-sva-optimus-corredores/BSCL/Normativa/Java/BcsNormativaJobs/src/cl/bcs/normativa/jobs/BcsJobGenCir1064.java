package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.generacircular1064.ProcesoGenCir1064;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.scheduler.BcsJob;

public final class BcsJobGenCir1064 extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobGenCir1064.class.getName());

    @Override
    public void onError(
            final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de generacion Circular 1064", t);
    }

    @Override
    public void pause() {
    }

    @Override
    public boolean transaction(
            final Connection conn)
                    throws InterruptedException,
                    Throwable {
        LOGGER.log(Level.INFO,
                "Proceso de Generacion Circular 1064 activado con los parametros {0}", parameters);

        final Proceso proceso = ProcesoGenCir1064.newInstance(conn, parameters, getJobId(), getDataSource());
        proceso.procesar();

        LOGGER.log(Level.INFO, "Proceso de Generacion Circular 1064 OK");
        return true;
    }

}
