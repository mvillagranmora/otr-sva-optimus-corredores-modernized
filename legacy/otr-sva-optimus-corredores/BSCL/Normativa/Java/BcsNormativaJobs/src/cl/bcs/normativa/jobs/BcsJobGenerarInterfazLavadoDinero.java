package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazLavadodeDinero;
import cl.bcs.normativa.interfaces.GenerarInterfazLavadodeDinero.EntradaGenerarInterfazLavadodeDinero;
import cl.bcs.normativa.interfaces.GenerarInterfazLavadodeDinero.SalidaGenerarInterfazLavadodeDinero;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
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

public class BcsJobGenerarInterfazLavadoDinero extends BcsJob implements JavaCall{ 
	        
	
	private final Log log = new Log("BcsJobGenerarInterfazCliente"); 
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaArchivo;
		public String 	RutaLog; 
		public Long 	IdPrograma; 
		public String 	Comentario;
		
		public ActualizarPrograma(String p_out_Archivos,String p_out_Archivos_log, Long IdPrograma, String Comentario){
			this.RutaArchivo = p_out_Archivos;
			this.RutaLog     = p_out_Archivos_log;
			this.IdPrograma = IdPrograma; 
			this.Comentario = Comentario; 
		}
		
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		GenerarInterfazLavadodeDinero generarInterfazLavadoDinero = new  GenerarInterfazLavadodeDinero();
		EntradaGenerarInterfazLavadodeDinero entrada = new EntradaGenerarInterfazLavadodeDinero();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,Long.parseLong(this.getParam("IdEmpresa")), "PLATAFORMA", "PLA.FechaSistema");
		entrada.p_IdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		
		//IMPBESTOPT-901
		DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String dateformat = this.getParam("FechaUI");
		OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
		String fechaformateada = dateTime.format(formatoFecha);		
		entrada.p_Fecha = fechaformateada;	
		
		
		entrada.p_Ruta =  Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");

		try{
			SalidaGenerarInterfazLavadodeDinero out = generarInterfazLavadoDinero.logica(con, entrada);
			SQL sql=new SQL();
			if(!out.p_out_Archivos.isEmpty()){
				
				
				String sufijo = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.SufijoArchivos");
				String zip = "FINDNA"+sufijo+".zip";
				
				ZipUtil.generateMultipleZip(out.p_out_Archivos, entrada.p_Ruta + "/" + zip);   
				
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				
				actualizar.add(new ActualizarPrograma(zip,out.p_out_nombre_archivo_log,this.getJobId(), out.p_out_Comentario)); 
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
