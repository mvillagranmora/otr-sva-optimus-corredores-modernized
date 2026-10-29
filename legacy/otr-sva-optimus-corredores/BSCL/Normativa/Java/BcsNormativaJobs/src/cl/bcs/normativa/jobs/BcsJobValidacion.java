package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.normativa.carga.ProcesoCarga;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.validacion.ProcesoValidacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Utils;

public final class BcsJobValidacion extends BcsJob {

    private static final Logger LOGGER = Logger.getLogger(BcsJobValidacion.class.getName());

    @Override
    public void onError(
            final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de validacion", t);
    }

    @Override
    public void pause() {
    }

    @Override
    public boolean transaction(
            final Connection conn)
                    throws InterruptedException,
                    Throwable {
/*
    	parameters.put("p_CreaParam", "S");  //new
    	parameters.put("p_IdAgenda", "0");  //new
    	parameters.put("p_IdEmpresa", "1");  //new
    	parameters.put("p_IdModulo", "TRI");  //new
    	parameters.put("p_IdTipoTarea", "ValidacionDJ"); //new
    	parameters.put("p_IdUsuario", "51");  //new
    	parameters.put("p_Segundos", "0");  //new
    	parameters.put("idDocumento", "114");
    	parameters.put("idUsuario", "51");
    	parameters.put("schema", "BSCL");
*/
    	LOGGER.log(Level.INFO, "Proceso de validacion activado con los parametros {0}", parameters);

        try {
            final Proceso proceso = ProcesoValidacion.newInstance(conn, parameters, getJobId(), getDataSource());
            proceso.procesar();
        } catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        LOGGER.log(Level.INFO, "Proceso de validacion finalizado OK");
        return true;
    }
	/********************
	 * Método Principal *
	 ********************/
/*
	public static void main(String[] args) throws Exception {
		Connection conn = PlataformaUtils.getQAConnection(); // .getDESAConnection();
    	
		BcsJobValidacion demo = new BcsJobValidacion();		 			

		boolean Errores;

		try {
			Errores = demo.transaction(conn);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
*/
}
