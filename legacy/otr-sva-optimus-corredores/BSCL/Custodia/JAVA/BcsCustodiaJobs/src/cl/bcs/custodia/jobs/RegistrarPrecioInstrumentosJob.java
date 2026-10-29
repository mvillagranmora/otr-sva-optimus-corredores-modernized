package cl.bcs.custodia.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import cl.bcs.custodia.jobs.RegistraPrecioInstrumento.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.custodia.jobs.RegistraPrecioInstrumento.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.custodia.jobs.RegistraPrecioInstrumento.Instrumento;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;

public class RegistrarPrecioInstrumentosJob extends BcsJob implements JavaCall{
	private static final Logger logger = LogManager.getLogger(RegistrarPrecioInstrumentosJob.class);
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("Inicia ejecucion del job");
		try {
			Long 	parIdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
			Long 	parIdUsuario=Long.parseLong(this.getParam("IdUsuario"));
			String 	parIdClasificacionTipoInstrumento = this.getParam("IdClasificacionTipoInstrumento");
			String 	parFecha = this.getParam("Fecha");
			RegistraPrecioInstrumento registraPrecioInstrumento = new RegistraPrecioInstrumento();	
			ConsultarInstrumentoPrecioBolsa_Entrada entrada = new ConsultarInstrumentoPrecioBolsa_Entrada();
			List<Instrumento> listIns = new ArrayList<Instrumento>();
			
			String[] clasif = parIdClasificacionTipoInstrumento.split(";");
			for (String c : clasif) {
				Instrumento ins = new Instrumento();
				ins.IdClasificacionTipoInstrumento = c;
				ins.IdEmpresa = parIdEmpresa;
				listIns.add(ins);
			}
			
			entrada.p_IdEmpresa = parIdEmpresa;	
			entrada.p_IdUsuario = parIdUsuario;
			entrada.p_Instrumentos = listIns;
			entrada.p_Fecha = parFecha;
			logger.debug("RegistrarPrecioInstrumentosJob-Param: " + entrada.toString());
			try{
				ConsultarInstrumentoPrecioBolsa_Salida res = registraPrecioInstrumento.logica(con,entrada);
				boolean resultado = false;
				if(res.P_OUT_ERRORESCONTADOR == 0 && res.P_OUT_INSTRUMENTO.size() > 0){
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

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}