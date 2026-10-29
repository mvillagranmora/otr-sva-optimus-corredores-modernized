package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.GenerarNCG380;
import cl.bcs.normativa.GenerarNCG380.GenerarNCG380_Entrada;
import cl.bcs.normativa.GenerarNCG380.GenerarNCG380_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class PautaGenerarNCG380 extends BcsJob implements BcsProcesoPauta {

	private final Log log = new Log("PLA.PautaGenerarNCG380");
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaGenerarNCG380 *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			salida.P_OUT_ERRORESCONTADOR = 0L;
			GenerarNCG380_Entrada entrada = new GenerarNCG380_Entrada();
			entrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			GenerarNCG380 generarNCG380 = new GenerarNCG380();
			GenerarNCG380_Salida resp = generarNCG380.logica(con, entrada);
			salida.P_OUT_ERRORESCONTADOR += resp.P_OUT_ERRORESCONTADOR;

		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR += 1;
			log.write(e.getMessage());
    		log.write("***** Error PautaGenerarNCG380 *****");
    		throw e;
		}finally {
			if(salida.P_OUT_ERRORESCONTADOR == 0) {				
				salida.P_OUT_MENSAJE = "El proceso PautaGenerarNCG380 se ha ejecutado correctamente";
			}else {
				salida.P_OUT_MENSAJE = "El proceso PautaGenerarNCG380 ha finalizado con errores";
			}
			log.write(salida.P_OUT_MENSAJE);
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("***** Fin PautaGenerarNCG380 *****\n");
		}
		return salida;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con =  PlataformaUtils.getPropertiesConnection("scotia","connections.properties")) {
			Map<String,Object> params = new HashMap<>();
			
			PautaGenerarNCG380 pgn = new PautaGenerarNCG380();
			try {
				pgn.transaction(con, params);
//				con.commit();
				con.rollback();
			} catch (Throwable e) {
				e.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}	

	}
	
	private static final Logger logger = LogManager.getLogger(PautaGenerarNCG380.class);
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("Inicia ejecucion del job");
		try {			
			GenerarNCG380_Entrada entrada = new GenerarNCG380_Entrada();
			entrada.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entrada.p_FechaEjecucion = this.getParam("FechaEjecucion") == null ? "" : this.getParam("FechaEjecucion");
			logger.debug("PautaGenerarNCG380-Param: " + entrada.toString());
			GenerarNCG380 generarNCG380 = new GenerarNCG380();
			
			try{
				GenerarNCG380_Salida res = generarNCG380.logica(con, entrada);
				boolean resultado = false;
				if(res.P_OUT_ERRORESCONTADOR == 0){
					resultado=true;
				}
				if(resultado){
					return true;
				}
				return false;
			}catch(Exception e){
				logger.error(e);
				return false;
			}
		}catch(Exception e){
			logger.error(e);
			return false;
		}
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(t);
    }

	@Override
	public void pause() {

	}
}
