package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.cuadratura.ProcesoCuadratura;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.scheduler.BcsJob;

public final class BcsJobCuadratura extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobCuadratura.class.getName());

    @Override
    public void onError(
            final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de cuadratura", t);
    }

    @Override
    public void pause() {
    }

    @Override
    public boolean transaction(
            final Connection conn)
                    throws InterruptedException,
                    Throwable {
        LOGGER.log(Level.INFO, "Proceso de cuadratura activado con los parametros {0}", parameters);

        final Proceso proceso = ProcesoCuadratura.newInstance(conn, parameters, getJobId(), getDataSource());
        proceso.procesar();

        LOGGER.log(Level.INFO, "Proceso de cuadratura finalizado OK");
        return true;
    }

}
