package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.normativa.carga.CargaDJ1Pagos;
import cl.bcs.normativa.carga.CargaDJ1Pagos.CargaDJ1Pagos_Entrada;
import cl.bcs.scheduler.BcsJob;

public final class BcsJobCargaDJ1Pagos extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobCargaDJ1Pagos.class.getName());
    
    @Override
    public void onError(final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de carga", t);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	return logica(con);
    }
	
	// logica
	public boolean logica(Connection con) throws Exception {
    	LOGGER.info("INICIO::BcsJobCargaDJ1Pagos");
    	
    	CargaDJ1Pagos_Entrada in = new CargaDJ1Pagos_Entrada();
    	in.idEmpresa = Long.valueOf(getParam("idEmpresa"));
    	in.idUsuario = Long.valueOf(getParam("idUsuario"));
    	in.idDocumento = Long.valueOf(getParam("idDocumento"));
    	in.schema = getParam("schema");
    	in.grabar = getParam("grabar");
    	in.reemplazar = getParam("reemplazar");
    	in.aceptarFechasNull = getParam("aceptarFechasNull");
    	in.tipoCarga = getParam("tipoCarga");
    	in.fechaDesde = getParam("fechaDesde");
    	in.fechaHasta = getParam("fechaHasta");
    	
    	boolean result = true;
    	if (in.grabar.equals("true")) {
        	CargaDJ1Pagos cargaPagos = new CargaDJ1Pagos();
        	result = cargaPagos.logica(con,in);	
    	}
    	
    	LOGGER.info("FIN::BcsJobCargaDJ1Pagos");
    	
    	return result;
	}
}
