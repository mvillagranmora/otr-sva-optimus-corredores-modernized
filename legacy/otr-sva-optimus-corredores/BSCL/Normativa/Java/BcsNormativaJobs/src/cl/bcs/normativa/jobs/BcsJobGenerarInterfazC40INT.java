   
package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazC40INT;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INT.GenerarInterfazC40INTEntrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INT.GenerarInterfazC40INTSalida;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INTDET;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INTDET.EntradaGenerarInterfazC40INTDET;
import cl.bcs.normativa.interfaces.GenerarInterfazC40INTDET.SalidaGenerarInterfazC40INTDET;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

	public class BcsJobGenerarInterfazC40INT extends BcsJob implements JavaCall{

		private final Log log = new Log("BcsJobGenerarInterfazINT"); 
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
			GenerarInterfazC40INT GenerarInterfazC40INT = new GenerarInterfazC40INT();
			GenerarInterfazC40INTEntrada in = new GenerarInterfazC40INTEntrada();
			
			DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String dateformat = this.getParam("FechaUI");
			OffsetDateTime dateTime = OffsetDateTime.parse(dateformat);
			String fechaformateada = dateTime.format(formatoFecha);		
	
			
			in.p_Fecha     					= fechaformateada;	// Interfaz INT genera la fecha actual
			in.p_IdModulo  					= "NOR";
			in.p_IdEmpresa 					= Long.parseLong(this.getParam("IdEmpresa"));
			in.p_IdUsuario 					= Long.parseLong(this.getParam("IdUsuario"));
			in.p_Ruta      					= Utils.getConfiguracion(con,Utils.getSchema(),in.p_IdEmpresa,"PLA.RutaBaseArchivos");
			
			GenerarInterfazC40INTDET GenerarInterfazC40DET = new GenerarInterfazC40INTDET();
			EntradaGenerarInterfazC40INTDET in2 = new EntradaGenerarInterfazC40INTDET();
			in2.p_Fecha     					= fechaformateada;	// Interfaz INT genera la fecha actual
			in2.p_IdEmpresa 					= Long.parseLong(this.getParam("IdEmpresa"));
			in2.p_IdUsuario 					= Long.parseLong(this.getParam("IdUsuario"));
			in2.p_Ruta      					= Utils.getConfiguracion(con,Utils.getSchema(),in.p_IdEmpresa,"PLA.RutaBaseArchivos");
			
			
			try {
				GenerarInterfazC40INTSalida out =  GenerarInterfazC40INT.logica(con, in);
				SalidaGenerarInterfazC40INTDET out2 =  GenerarInterfazC40DET.logica(con, in2); 
				SQL sql = new SQL();
				if(out.P_OUT_NOMBRE_ARCHIVO != null){
					List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
					actualizar.add(new ActualizarPrograma(out.P_OUT_NOMBRE_ARCHIVO, 
							out2.P_OUT_NOMBRE_ARCHIVO, 
														  this.getJobId(), 
														  out.P_OUT_COMENTARIO));
					
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
