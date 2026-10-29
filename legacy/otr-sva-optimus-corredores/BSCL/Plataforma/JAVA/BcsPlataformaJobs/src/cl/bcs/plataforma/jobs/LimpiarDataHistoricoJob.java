package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.LimpiarDataHistorico;
import cl.bcs.plataforma.pla.LimpiarDataHistorico.LimpiarDataHistoricoEntrada;
import cl.bcs.plataforma.pla.LimpiarDataHistorico.LimpiarDataHistoricoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;

public class LimpiarDataHistoricoJob  extends BcsJob implements JavaCall {
static final Logger logger 	= LogManager.getLogger(LimpiarDataHistoricoJob.class.getName());
	
   

    


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		// TODO Auto-generated method stub
		
		logger.debug("***** Inicio LimpiarDataHistoricoJob *****");
		boolean debug = false;
		
		LimpiarDataHistoricoEntrada limpiarDataHistoricoEntrada = new LimpiarDataHistoricoEntrada();
		if (debug == true) {
			
    		limpiarDataHistoricoEntrada.p_IdEmpresa = 1L;
    		limpiarDataHistoricoEntrada.p_IdUsuario = 1L;
    		limpiarDataHistoricoEntrada.p_Tabla  = "\"BSCL.Custodia.BD::CUS.PatrimonioCustodia\"";
    		limpiarDataHistoricoEntrada.p_Filtro  = "\"FechaPatrimonio\"";
    		limpiarDataHistoricoEntrada.p_Desde  = "2024-09-27";
    		limpiarDataHistoricoEntrada.p_Hasta  = "2024-09-29";
    		limpiarDataHistoricoEntrada.p_SoloLimpia  = "S";
			
		}
		else {
		long IdEmpresa = Long.parseLong(parameters.get("IdEmpresa"));
		long IdUsuario = Long.parseLong(parameters.get("IdUsuario"));
		limpiarDataHistoricoEntrada.p_IdEmpresa = IdEmpresa;
		limpiarDataHistoricoEntrada.p_IdUsuario = IdUsuario;
		limpiarDataHistoricoEntrada.p_Tabla  = (String)parameters.get("Tabla");
		limpiarDataHistoricoEntrada.p_Filtro  = (String)parameters.get("Filtro");
		limpiarDataHistoricoEntrada.p_Desde  = (String)parameters.get("Desde");
		limpiarDataHistoricoEntrada.p_Hasta  = (String)parameters.get("Hasta");
		limpiarDataHistoricoEntrada.p_SoloLimpia  = (String)parameters.get("SoloLimpia");
		}
		
		LimpiarDataHistorico limpiarDataHistorico = new LimpiarDataHistorico();
		
		LimpiarDataHistoricoSalida limpiarDataHistoricoSalida = limpiarDataHistorico.logica(con, limpiarDataHistoricoEntrada);
		
		limpiarDataHistoricoSalida.P_OUT_MENSAJES.stream().forEach(mensaje->{
			logger.debug(mensaje.descripcion);
		});

	logger.debug("***** Fin LimpiarDataHistoricoJob *****");
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
			LimpiarDataHistoricoJob clase = new LimpiarDataHistoricoJob();
			clase.transaction(con);
			con.commit();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
