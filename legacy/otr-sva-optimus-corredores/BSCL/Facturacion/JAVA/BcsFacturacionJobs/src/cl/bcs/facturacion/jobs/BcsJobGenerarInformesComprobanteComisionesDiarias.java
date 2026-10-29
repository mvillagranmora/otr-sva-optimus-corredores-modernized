package cl.bcs.facturacion.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.GenerarInformesComprobanteComisionesDiarias;
import cl.bcs.facturacion.GenerarInformesComprobanteComisionesDiarias.EntradaGenerarInformesComprobanteComisionesDiarias;
import cl.bcs.facturacion.GenerarInformesComprobanteComisionesDiarias.SalidaGenerarInformesComprobanteComisionesDiarias;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobGenerarInformesComprobanteComisionesDiarias extends BcsJob implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(BcsJobGenerarInformesComprobanteComisionesMensuales.class);
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		public String 	Comentario;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma, String Comentario){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma; 
			this.Comentario = Comentario; 
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		GenerarInformesComprobanteComisionesDiarias generarInforme = new GenerarInformesComprobanteComisionesDiarias();
		EntradaGenerarInformesComprobanteComisionesDiarias entrada= new EntradaGenerarInformesComprobanteComisionesDiarias();
		
		entrada.p_IdEmpresa	= Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");
		entrada.p_IdModulo = "FAC";
		entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		entrada.p_IdCliente	= Long.parseLong(this.getParam("IdCliente"));
		entrada.p_IdPortafolio	= Long.parseLong(this.getParam("IdPortafolio"));
		entrada.p_Fecha = this.getParam("Fecha");

		
		try{
			SalidaGenerarInformesComprobanteComisionesDiarias out = generarInforme.logica(con, entrada);
			SQL sql=new SQL();
			if(out.p_out_NombreArchivo != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.p_out_NombreArchivo,out.p_out_NombreArchivoLog,this.getJobId(), out.p_out_Comentario));
				sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
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
		logger.info("****** ERROR  *******");
	    logger.info(t);
	    t.printStackTrace();
		
	}

	@Override
	public void pause() {}


}
