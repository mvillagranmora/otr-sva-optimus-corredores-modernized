package cl.bcs.mesa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.mesa.MRF.GenerarInformeUtilidadPerdida;
import cl.bcs.mesa.MRF.GenerarInformeUtilidadPerdida.GenerarInformeUtilidaPerdida_Salida;
import cl.bcs.mesa.MRF.GenerarInformeUtilidadPerdida.GenerarInformeUtilidadPerdida_Entrada;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformeUtilidadPerdidaJob extends BcsJob implements JavaCall{

	private final static Logger logger = LogManager.getLogger(GenerarInformeUtilidadPerdidaJob.class);
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		public String 	Comentario;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma, String Comentario){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog     = RutaLog;
			this.IdPrograma  = IdPrograma; 
			this.Comentario  = Comentario; 
		}
	}
	

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("Inicio ejecucion Job GenerarInformeUtilidadPerdida");
		
		GenerarInformeUtilidadPerdida generarInformeUtilidadPerdida = new GenerarInformeUtilidadPerdida();
		GenerarInformeUtilidadPerdida_Entrada entradaInforme =new GenerarInformeUtilidadPerdida_Entrada();
		entradaInforme.p_IdModulo = "MESA";
		entradaInforme.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		entradaInforme.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		entradaInforme.p_FechaHasta = this.getParam("FechaHasta");
		entradaInforme.p_FechaDesde = this.getParam("FechaDesde");
		
		entradaInforme.p_Ruta = Utils.getConfiguracion(con,Utils.getSchema(),entradaInforme.p_IdEmpresa,"PLA.RutaBaseArchivos");
		
		try {
			GenerarInformeUtilidaPerdida_Salida salidaInformeUtilidadPerdida = generarInformeUtilidadPerdida.logica(con, entradaInforme);
			SQL sql = new SQL();
			
			if(salidaInformeUtilidadPerdida.P_OUT_NOMBRE_ARCHIVO != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(salidaInformeUtilidadPerdida.P_OUT_NOMBRE_ARCHIVO, 
						salidaInformeUtilidadPerdida.P_OUT_NOMBRE_ARCHIVO_LOG, 
													  this.getJobId(), 
													  salidaInformeUtilidadPerdida.P_OUT_COMENTARIO));
				
				sql.batch(con, SQL_UPDATE_ID_PROGRAMA, actualizar,ActualizarPrograma.class);
				con.commit();
				
				return true;
				
			}else{
				return false;
			}
			
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
		
	}

	@Override
	public void onError(Throwable t) {
		logger.error("****** ERROR  *******");
		logger.error(t);
        t.printStackTrace();
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {

		return null;
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

}

