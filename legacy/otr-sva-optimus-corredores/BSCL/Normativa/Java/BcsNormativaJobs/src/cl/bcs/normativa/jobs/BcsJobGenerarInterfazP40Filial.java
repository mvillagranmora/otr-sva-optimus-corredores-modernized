package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.EntradaGenerarInterfazP40Filial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.SalidaArchivoFilial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.SalidaGenerarInterfazP40Filial;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;

public class BcsJobGenerarInterfazP40Filial extends BcsJob implements JavaCall {
	/*
	 * Job para generar servicio de Interfaz P40 Filial
	 */
	private final Log log = new Log("BcsJobGenerarInterfazP40Filial");
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
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		GenerarInterfazP40Filial generarInterfazP40 = new GenerarInterfazP40Filial();
		EntradaGenerarInterfazP40Filial entradap40 = new EntradaGenerarInterfazP40Filial();
		
		// Parametros de entrada (el resto se asigna por defecto)
		entradap40.p_IdEmpresa	= Long.parseLong(this.getParam("IdEmpresa"));	
		entradap40.p_IdUsuario 	= Long.parseLong(this.getParam("IdUsuario"));	
		entradap40.p_Ruta      	= Utils.getConfiguracion(con,Utils.getSchema(),entradap40.p_IdEmpresa,"PLA.RutaBaseArchivos");
		
		DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String dateformat = this.getParam("FechaUI");
		OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
		entradap40.p_Fecha  = dateTime.format(formatoFecha);		
		
		try{
			SalidaGenerarInterfazP40Filial salida = generarInterfazP40.logica(con, entradap40);
			SQL sql = new SQL();
			
			if(!salida.P_OUT_ARCHIVOS_POR_FILIAL.isEmpty()) {
				List<SalidaArchivoFilial> salidaArchivoFilial = salida.P_OUT_ARCHIVOS_POR_FILIAL;
				
				for(SalidaArchivoFilial salidaFilial : salidaArchivoFilial) {
					List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
					String zip  = salidaFilial.P_OUT_NOMBRE_ZIP + ".zip";
					ZipUtil.generateMultipleZip(salidaFilial.P_OUT_ARCHIVOS, entradap40.p_Ruta + "/" + zip);  
					actualizar.add(new ActualizarPrograma(zip,salidaFilial.P_OUT_NOMBRE_ARCHIVO_LOG,this.getJobId(), salidaFilial.P_OUT_COMENTARIO));
					sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
					con.commit();
				}
				
				return true;
				
			} else{
				return false;
			}
			
		} catch(Exception e){
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

}
