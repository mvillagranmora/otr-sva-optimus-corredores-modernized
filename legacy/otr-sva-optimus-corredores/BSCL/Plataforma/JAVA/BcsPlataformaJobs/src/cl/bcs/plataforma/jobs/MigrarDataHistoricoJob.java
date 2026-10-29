package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.MigrarDataHistorico;
import cl.bcs.plataforma.pla.MigrarDataHistorico.MigrarDataHistoricoEntrada;
import cl.bcs.plataforma.pla.MigrarDataHistorico.MigrarDataHistoricoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;

public class MigrarDataHistoricoJob  extends BcsJob implements JavaCall {
static final Logger logger 	= LogManager.getLogger(MigrarDataHistoricoJob.class.getName());
	
   

    public boolean procesar(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	
    	logger.debug("***** Inicio MigrarDataHistoricoJob *****");
		
    	
    		
    		MigrarDataHistoricoEntrada migrarDataHistoricoEntrada = new MigrarDataHistoricoEntrada();
    		migrarDataHistoricoEntrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		migrarDataHistoricoEntrada.p_IdUsuario = (Long)parameters.get("IdUsuario");
    		migrarDataHistoricoEntrada.p_Tabla  = (String)parameters.get("Tabla");
    		migrarDataHistoricoEntrada.p_Filtro  = (String)parameters.get("Filtro");
    		migrarDataHistoricoEntrada.p_Desde  = (String)parameters.get("Desde");
    		migrarDataHistoricoEntrada.p_Hasta  = (String)parameters.get("Hasta");
    		migrarDataHistoricoEntrada.p_SoloLimpia  = (String)parameters.get("SoloLimpia");
    		
    		MigrarDataHistorico migrarDataHistorico = new MigrarDataHistorico();
			
    		MigrarDataHistoricoSalida migrarDataHistoricoSalida = migrarDataHistorico.logica(con, migrarDataHistoricoEntrada);
			
    		migrarDataHistoricoSalida.P_OUT_MENSAJES.stream().forEach(mensaje->{
    			logger.debug(mensaje.descripcion);
			});
		
		logger.debug("***** Fin MigrarDataHistoricoJob *****");
		return true;
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		// TODO Auto-generated method stub
		logger.debug("***** Inicio MigrarDataHistoricoJob *****");
		boolean debug = false;
    	
		
		MigrarDataHistoricoEntrada migrarDataHistoricoEntrada = new MigrarDataHistoricoEntrada();
		
		if (debug == true) {
			
			migrarDataHistoricoEntrada.p_IdEmpresa = 1L;
			migrarDataHistoricoEntrada.p_IdUsuario = 1L;
			migrarDataHistoricoEntrada.p_Tabla  = "\"BSCL.Custodia.BD::CUS.PatrimonioCustodia\"";
    		migrarDataHistoricoEntrada.p_Filtro  = "\"FechaPatrimonio\"";
    		migrarDataHistoricoEntrada.p_Desde  = "2024-09-27";
    		migrarDataHistoricoEntrada.p_Hasta  = "2024-09-28";
    		migrarDataHistoricoEntrada.p_SoloLimpia  = "N";
			
		}
		else {
		long IdEmpresa = Long.parseLong(parameters.get("IdEmpresa"));
		long IdUsuario = Long.parseLong(parameters.get("IdUsuario"));
		migrarDataHistoricoEntrada.p_IdEmpresa = IdEmpresa;
		migrarDataHistoricoEntrada.p_IdUsuario = IdUsuario;
		migrarDataHistoricoEntrada.p_Tabla  = (String)parameters.get("Tabla");
		migrarDataHistoricoEntrada.p_Filtro  = (String)parameters.get("Filtro");
		migrarDataHistoricoEntrada.p_Desde  = (String)parameters.get("Desde");
		migrarDataHistoricoEntrada.p_Hasta  = (String)parameters.get("Hasta");
		migrarDataHistoricoEntrada.p_SoloLimpia  = (String)parameters.get("SoloLimpia");
		}
		
		MigrarDataHistorico migrarDataHistorico = new MigrarDataHistorico();
		
		MigrarDataHistoricoSalida migrarDataHistoricoSalida = migrarDataHistorico.logica(con, migrarDataHistoricoEntrada);
		
		migrarDataHistoricoSalida.P_OUT_MENSAJES.stream().forEach(mensaje->{
			logger.debug(mensaje.descripcion);
		});
	
	logger.debug("***** Fin MigrarDataHistoricoJob *****");
	return true;
	}


	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws Exception {
		try(Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			MigrarDataHistoricoJob clase = new MigrarDataHistoricoJob();
			clase.transaction(con);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

