package cl.bcs.socionegocio.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.cli.GenerarReporteClienteFallecido;
import cl.bcs.socionegocio.cli.GenerarReporteClienteFallecido.InConsultaClientes;
import cl.bcs.socionegocio.cli.GenerarReporteClienteFallecido.OutConsultaClientes;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class GenerarReporteClienteFallecidoJob extends BcsJob implements JavaCall{
	
	private final Log log = new Log("GenerarReporteClienteFallecidoJob"); 
	
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/socionegocio/jobs/sql/GenerarReporteClienteFallecido_Update.sql");
	
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		Long parIdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		Long parIdUsuario=Long.parseLong(this.getParam("IdUsuario"));
		String parIdModulo = this.getParam("IdModulo");
		
		GenerarReporteClienteFallecido generarReporteClienteFallecido = new GenerarReporteClienteFallecido();
		
		InConsultaClientes entrada = new InConsultaClientes();
		
		
		
		entrada.p_IdEmpresa = parIdEmpresa;
		entrada.p_IdUsuario = parIdUsuario;
		entrada.p_IdModulo = parIdModulo;
		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), parIdEmpresa, "PLA.RutaBaseArchivos");
		
		try{
			OutConsultaClientes out=generarReporteClienteFallecido.logica(con,entrada);
			SQL sql=new SQL();
			if(new Long(0).compareTo(out.P_OUT_ERRORESCONTADOR) == 0){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.P_OUT_NOMBRE_ARCHIVO,this.getJobId()));
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