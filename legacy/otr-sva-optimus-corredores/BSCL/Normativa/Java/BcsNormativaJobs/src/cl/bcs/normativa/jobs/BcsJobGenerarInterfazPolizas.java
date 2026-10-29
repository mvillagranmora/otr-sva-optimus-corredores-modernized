package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.normativa.interfaces.GenerarInterfazPolizas;
import cl.bcs.normativa.interfaces.GenerarInterfazPolizas.InGenerarInterfazPolizas;
import cl.bcs.normativa.interfaces.GenerarInterfazPolizas.OutGenerarInterfazPolizas;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsJobGenerarInterfazPolizas extends BcsJob implements JavaCall{
	
	private final Log log = new Log("BcsJobGenerarInterfazOrdenes"); 
	
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
		
		GenerarInterfazPolizas generarInterfazClientesEnLinea = new  GenerarInterfazPolizas();
		InGenerarInterfazPolizas entrada= new InGenerarInterfazPolizas();
		
		entrada.p_IdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_IdUsuario=Long.parseLong(this.getParam("IdUsuario"));
		entrada.p_IdModulo = this.getParam("IdModulo");
		entrada.p_FechaDesde = this.getParam("FechaDesde");
		entrada.p_FechaHasta = this.getParam("FechaHasta");
		entrada.p_IdCliente = Long.parseLong(this.getParam("IdCliente"));
		entrada.p_EsTodosCliente = this.getParam("TodosClientes");
		entrada.p_IdInstrumento = Long.parseLong(this.getParam("IdInstrumento"));
		entrada.p_EsTodosInstrumentos = this.getParam("TodosInstrumentos");
		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");

		try{
			OutGenerarInterfazPolizas out=generarInterfazClientesEnLinea.logica(con,entrada);
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
