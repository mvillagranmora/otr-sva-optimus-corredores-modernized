package cl.bcs.facturacion.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.GenerarInformeAFP;
import cl.bcs.facturacion.GenerarInformeAFP.EntradaGenerarInformeAFP;
import cl.bcs.facturacion.GenerarInformeAFP.SalidaGenerarInformeAFP;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobGenerarInformesAFP extends BcsJob implements JavaCall{ 
	        
	private final Log log = new Log("BcsJobGenerarInformesAFP"); 
	
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	
	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE PROCESAMIENTO
	// -------------------------------------------------------------------------------------------
	
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
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		GenerarInformeAFP generarInforme = new  GenerarInformeAFP();
		EntradaGenerarInformeAFP entrada= new EntradaGenerarInformeAFP();
		
		entrada.p_IdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_Fecha = ConfiguracionSistema.getParametroConfiguracionString(con,Long.parseLong(this.getParam("IdEmpresa")), "PLATAFORMA", "PLA.FechaSistema");
		entrada.p_Ruta =  Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");
		entrada.p_IdModulo = "NOR";
		entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		entrada.IdAFP = Long.parseLong(this.getParam("ID_AFP"));
		entrada.FechaAFP = this.getParam("Fecha_AFP").substring(0,10);
		entrada.NombreAFP = this.getParam("NOMBRE_AFP");
		entrada.ParametroJPM = this.getParam("PARAMETRO_JPM");

		try{
			SalidaGenerarInformeAFP out=generarInforme.logica(con, entrada);
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
        log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
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