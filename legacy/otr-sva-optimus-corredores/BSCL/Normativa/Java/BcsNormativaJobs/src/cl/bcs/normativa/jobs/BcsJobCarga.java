package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.carga.ProcesoCarga;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.scheduler.BcsJob;

public final class BcsJobCarga extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobCarga.class.getName());

    @Override
    public void onError(final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de carga", t);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
        LOGGER.log(Level.INFO, "Proceso de carga con los parametros {0}", parameters);
        
        try (final Connection conn = getDataSource().getConnection()) {
        	final Proceso proceso = ProcesoCarga.newInstance(conn, parameters, getJobId(), getDataSource());
        	proceso.procesar();        
        }
        
        LOGGER.log(Level.INFO, "Proceso de carga finalizado OK");
        return true;
    }

}
