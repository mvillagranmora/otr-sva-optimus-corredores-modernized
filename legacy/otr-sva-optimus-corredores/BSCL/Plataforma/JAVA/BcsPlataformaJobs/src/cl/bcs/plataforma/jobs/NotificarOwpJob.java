package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.AgregarLog;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.NotificarEstadoOrdenes_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.utils.SQL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class NotificarOwpJob extends BcsJob implements JavaCall{
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	private final Log log = new Log("PLA.NotificarOwpJob");	
		
	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		public String	Comentario;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma, String Comentario){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma;
			this.Comentario = Comentario;
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		SQL sql = new SQL();
		Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		String operaciones = this.getParam("Operaciones");
		
		//llenar datos para llamar a webService
		String webService = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.WebServiceOWP");
		Boolean auth = true;
		String user = "";
		String pass = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.WebServiceOWPHeadAuth");
		String url = "";
				
		//LOG EN ARCHIVO
		String fecha = Utils.getFechaHoraSistema(con, Utils.getSchema(), idEmpresa);
		String ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
		String nombreArchivoLog= "logNotificacionOWP/log_Notificacion_Owp_"+fecha+".log";
		String rutaArchivoLog = ruta + "/"+ nombreArchivoLog;
		File archivoLog = new File(rutaArchivoLog);
		File folder = new File(ruta + "/logNotificacionOWP");
		
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
			
		try {
			 //Llamar web service
			CallRestService call = new CallRestService(webService, user, pass,url, auth);
			String output = call.callRestPostHeaderAuth(operaciones.toString());	
			
			Gson g = Utils.getGsonBuilder();
			NotificarEstadoOrdenes_Salida result = new NotificarEstadoOrdenes_Salida();
			result = g.fromJson(output, NotificarEstadoOrdenes_Salida.class);	
				
			bwArchivoLog.write("Entrada: " + operaciones.toString());
			bwArchivoLog.write(System.lineSeparator());
			bwArchivoLog.write("Respuesta de Servicio OWP - RequestCode: " + result.RequestCode + ", Message: " + result.Message);
			bwArchivoLog.close();
			
			List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
			actualizar.add(new ActualizarPrograma(rutaArchivoLog, nombreArchivoLog, this.getJobId(), ""));
			sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar, ActualizarPrograma.class);
			
			//Log interno
			log.write("Respuesta de Servicio OWP - RequestCode: " + result.RequestCode + ", Message: " + result.Message);
			
			return true;
			
		}catch(Exception e){
			log.write("Ha ocurrido un problema con la notificación OWP: "+ e);			
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
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws InterruptedException, Throwable{
		Connection con = PlataformaUtils.getDESAConnection();	
		NotificarOwpJob notifica = new NotificarOwpJob();
		notifica.transaction(con);
		//con.commit();
		con.rollback();
	}
}