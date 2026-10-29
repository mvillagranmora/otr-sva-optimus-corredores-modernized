package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazC08InformacionContable;
import cl.bcs.normativa.interfaces.GenerarInterfazC08InformacionContable.GenerarInterfazC08_Entrada;
import cl.bcs.normativa.interfaces.GenerarInterfazC08InformacionContable.GenerarInterfazC08_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobGenerarInterfazC08CBC extends BcsJob implements JavaCall{ 
	        
	
	private final Log log = new Log("BcsJobGenerarInterfazCliente"); 
	
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
		
		GenerarInterfazC08InformacionContable generarInterfazC08 = new  GenerarInterfazC08InformacionContable();
		GenerarInterfazC08_Entrada entrada = new GenerarInterfazC08_Entrada();
		entrada.p_IdEmpresa =Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_IdUsuario =Long.parseLong(this.getParam("IdUsuario"));
		entrada.p_Fecha =  ConfiguracionSistema.getParametroConfiguracionString(con,Long.parseLong(this.getParam("IdEmpresa")), "PLATAFORMA", "PLA.FechaSistema");
		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");
		

		try{
			GenerarInterfazC08_Salida out = generarInterfazC08.logica(con, entrada);
			SQL sql = new SQL();
			if(out.P_OUT_NOMBRE_ARCHIVO != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.P_OUT_NOMBRE_ARCHIVO, 
													  out.P_OUT_NOMBRE_ARCHIVO_LOG, 
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
	public void pause() {

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
