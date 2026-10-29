package cl.bcs.ordenes.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.GenerarInformeEstadistica;
import cl.bcs.ordenes.GenerarInformeEstadistica.GenerarInformeEstadistica_Entrada;
import cl.bcs.ordenes.GenerarInformeEstadistica.GenerarInformeEstadistica_Salida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformeEstadisticasJob extends BcsJob implements JavaCall{

	private final static Logger logger = LogManager.getLogger(GenerarInformeEstadisticasJob.class);
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
		logger.info("Inicio ejecucion Job GenerarInformeEstadistica");
		GenerarInformeEstadistica generarInformeEstadistica = new GenerarInformeEstadistica();
		GenerarInformeEstadistica_Entrada entradaInforme = new GenerarInformeEstadistica_Entrada();
		entradaInforme.p_IdModulo = "ORD";
		entradaInforme.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		entradaInforme.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		entradaInforme.p_FechaOperacionOrdenF = this.getParam("FechaOperacionOrdenF");
		entradaInforme.p_FechaOperacionOrdenI = this.getParam("FechaOperacionOrdenI");
		
		entradaInforme.p_Ruta = Utils.getConfiguracion(con,Utils.getSchema(),entradaInforme.p_IdEmpresa,"PLA.RutaBaseArchivos");
		
		try {
			GenerarInformeEstadistica_Salida salidaInformeEstadistica = generarInformeEstadistica.logica(con, entradaInforme);
			SQL sql = new SQL();
			
			if(salidaInformeEstadistica.P_OUT_NOMBRE_ARCHIVO != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(salidaInformeEstadistica.P_OUT_NOMBRE_ARCHIVO, 
													  salidaInformeEstadistica.P_OUT_NOMBRE_ARCHIVO_LOG, 
													  this.getJobId(), 
													  salidaInformeEstadistica.P_OUT_COMENTARIO));
				
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
