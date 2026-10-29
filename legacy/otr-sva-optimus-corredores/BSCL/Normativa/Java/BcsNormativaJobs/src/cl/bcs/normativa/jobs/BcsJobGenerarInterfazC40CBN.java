package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraNormal;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraNormal.EntradaGenerarInterfazC40CarteraNormal;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraNormal.SalidaGenerarInterfazC40CarteraNormal;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobGenerarInterfazC40CBN extends BcsJob implements JavaCall{ 
	        
	private final Log log = new Log("BcsJobGenerarInterfazC40CBN"); 
	
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
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		GenerarInterfazC40CarteraNormal generarInterfazC40 = new  GenerarInterfazC40CarteraNormal();
		EntradaGenerarInterfazC40CarteraNormal entrada= new EntradaGenerarInterfazC40CarteraNormal();
		
		entrada.p_IdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_Ruta =  Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");
		entrada.p_IdModulo = "NOR";
		entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));

		DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String dateformat = this.getParam("FechaUI");
		OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
		String fechaformateada = dateTime.format(formatoFecha);		
		entrada.p_Fecha = fechaformateada;			
		
		try{
			SalidaGenerarInterfazC40CarteraNormal out=generarInterfazC40.logica(con, entrada);
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