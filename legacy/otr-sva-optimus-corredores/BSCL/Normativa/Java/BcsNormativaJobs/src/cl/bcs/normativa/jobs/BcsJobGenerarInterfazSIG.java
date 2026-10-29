package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazSIG;
import cl.bcs.normativa.interfaces.GenerarInterfazSIG.EntradaGenerarInterfazSIG;
import cl.bcs.normativa.interfaces.GenerarInterfazSIG.SalidaGenerarInterfazSIG;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class BcsJobGenerarInterfazSIG extends BcsJob implements JavaCall{

	private final Log log = new Log("BcsJobGenerarInterfazSIG"); 
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
		GenerarInterfazSIG generarInterfazSIG = new GenerarInterfazSIG();
		EntradaGenerarInterfazSIG in = new EntradaGenerarInterfazSIG();
		in.p_IdModulo  					= "NOR";
		in.p_IdEmpresa 					= Long.parseLong(this.getParam("IdEmpresa"));
		in.p_IdUsuario 					= Long.parseLong(this.getParam("IdUsuario"));
		in.p_Ruta      					= Utils.getConfiguracion(con,Utils.getSchema(),in.p_IdEmpresa,"PLA.RutaBaseArchivos");
		
		// IMPBESTOPT-901
		DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String dateformat = this.getParam("FechaUI");
		OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
		in.p_Fecha  = dateTime.format(formatoFecha);
		
		try {
			SalidaGenerarInterfazSIG out = generarInterfazSIG.Logica(con, in);
			SQL sql = new SQL();
			
			if(!out.P_OUT_ARCHIVOS.isEmpty()){
				String sufijo = Utils.getConfiguracion(con, Utils.getSchema(), in.p_IdEmpresa, "PLA.SufijoArchivos");
				String zip = "SIG"+sufijo+".zip";
				ZipUtil.generateMultipleZip(out.P_OUT_ARCHIVOS, in.p_Ruta + "/" + zip);   
				
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				
				actualizar.add(new ActualizarPrograma(zip,out.P_OUT_NOMBRE_ARCHIVO_LOG,this.getJobId(), out.P_OUT_COMENTARIO)); 
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
	public JsonObject call(Connection conn, JsonObject in) throws Exception {

		return null;
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

}
