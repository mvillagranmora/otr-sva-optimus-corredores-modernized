package cl.bcs.mesa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.mesa.SPT.GenerarInformeSaldosCuentaCambio;
import cl.bcs.mesa.SPT.GenerarInformeSaldosCuentaCambio.GenerarInformeSaldosCuentaCambio_Entrada;
import cl.bcs.mesa.SPT.GenerarInformeSaldosCuentaCambio.GenerarInformeSaldosCuentaCambio_Salida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformeSaldosCuentaCambioJob extends BcsJob implements JavaCall{

	private final static Logger logger = LogManager.getLogger(GenerarInformeSaldosCuentaCambioJob.class);
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaArchivo;
		public String 	RutaLog;
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
		logger.info("Inicio ejecucion Job GenerarInformeSaldosCuentaCambio");
		
		GenerarInformeSaldosCuentaCambio generarInformeSaldosCuentaCambio = new GenerarInformeSaldosCuentaCambio();
		GenerarInformeSaldosCuentaCambio_Entrada entradaReporte = new GenerarInformeSaldosCuentaCambio_Entrada();
		entradaReporte.p_IdModulo = "MSP";
		entradaReporte.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		entradaReporte.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		entradaReporte.p_Periodo = this.getParam("Periodo");
		entradaReporte.p_FormatoArchivo = this.getParam("FormatoArchivo");
		
		entradaReporte.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entradaReporte.p_IdEmpresa, "PLA.RutaBaseArchivos");
		
		
		try {
			GenerarInformeSaldosCuentaCambio_Salida salidaReporte = generarInformeSaldosCuentaCambio.logica(con, entradaReporte);
			SQL sql = new SQL();
			
			if(salidaReporte.P_OUT_NOMBRE_ARCHIVO != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(salidaReporte.P_OUT_RUTA_ARCHIVO,
													  "", 
													  this.getJobId(), 
													  salidaReporte.P_OUT_COMENTARIO));
				
				sql.batch(con, SQL_UPDATE_ID_PROGRAMA, actualizar, ActualizarPrograma.class);
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

