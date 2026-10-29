package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazP40Aseguradora;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Aseguradora.EntradaGenerarInterfazP40Aseguradora;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Aseguradora.SalidaGenerarInterfazP40Aseguradora;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BcsJobGenerarInterfazP40Aseguradora extends BcsJob implements JavaCall {

	private final Log log = new Log("BcsJobGenerarInterfazP40Aseguradora"); 
	
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
		GenerarInterfazP40Aseguradora generarInterfazP40 = new  GenerarInterfazP40Aseguradora();
		EntradaGenerarInterfazP40Aseguradora entradap40 			= new  EntradaGenerarInterfazP40Aseguradora();  
		
		entradap40.p_Fecha     					= "";	// Interfaz P40 genera la fecha actual
		entradap40.p_IdModulo  					= "NOR";
		entradap40.p_IdEmpresa 					= Long.parseLong(this.getParam("IdEmpresa"));
		entradap40.p_IdUsuario 					= Long.parseLong(this.getParam("IdUsuario"));
		entradap40.p_Ruta      					= Utils.getConfiguracion(con,Utils.getSchema(),entradap40.p_IdEmpresa,"PLA.RutaBaseArchivos");
		entradap40.p_Banco						="223";
		entradap40.p_SBIF						="862";
		entradap40.p_TipoMercado				="";	// Todos los mercados (RF/IF/ACN/FM)
		entradap40.p_TipoCartera				="2";
		entradap40.p_UsaNemoSVS					="S";
		entradap40.p_ExcluyeGarantia			="N";
		entradap40.p_ExcluyeSimultanea			="S";  
		entradap40.p_UsaCustodia				="N";
		entradap40.p_FamiliaInstrumento			="49";
		
		DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String dateformat = this.getParam("FechaUI");
		OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
		entradap40.p_Fecha  = dateTime.format(formatoFecha);	
		
		try{
			SalidaGenerarInterfazP40Aseguradora out = generarInterfazP40.logica(con, entradap40);
			SQL sql = new SQL();
			if(!out.P_OUT_ARCHIVOS.isEmpty()){
				
				String sufijo = Utils.getConfiguracion(con, Utils.getSchema(), entradap40.p_IdEmpresa, "PLA.SufijoArchivos");
				String zip = "SP40"+sufijo+".zip";
				
				ZipUtil.generateMultipleZip(out.P_OUT_ARCHIVOS, entradap40.p_Ruta + "/" + zip);   
				
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
	public void pause() {

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
