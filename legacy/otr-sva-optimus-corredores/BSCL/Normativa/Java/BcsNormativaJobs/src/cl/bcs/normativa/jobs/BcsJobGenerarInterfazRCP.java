package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject; 

import cl.bcs.normativa.ils.GenerarInterfazRCP;
import cl.bcs.normativa.ils.GenerarInterfazRCP.InGenerarInterfazRCP;
import cl.bcs.normativa.ils.GenerarInterfazRCP.OutGenerarInterfazRCP;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobGenerarInterfazRCP extends BcsJob implements JavaCall{
	
	private final Log log = new Log("BcsJobGenerarInterfazRCP"); 
	
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/normativa/jobs/sql/InterfacesJob_update.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma;
		}
	}
	

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		GenerarInterfazRCP generarInterfazRCP = new  GenerarInterfazRCP();
		InGenerarInterfazRCP entrada= new InGenerarInterfazRCP();
		
		entrada.p_IdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_IdUsuario=Long.parseLong(this.getParam("IdUsuario"));
		entrada.p_IdModulo = this.getParam("IdModulo");
		entrada.p_FechaDesde = this.getParam("FechaDesde");
		entrada.p_Ruta =  Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");

		try{
			OutGenerarInterfazRCP out=generarInterfazRCP.logica(con,entrada);
			SQL sql=new SQL();
			if(out.p_out_NombreArchivo != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.p_out_NombreArchivo,out.p_out_NombreArchivoLog,this.getJobId()));
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
