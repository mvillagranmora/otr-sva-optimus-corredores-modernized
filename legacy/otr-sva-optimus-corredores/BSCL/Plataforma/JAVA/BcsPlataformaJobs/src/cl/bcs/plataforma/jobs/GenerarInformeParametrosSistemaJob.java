package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;

import cl.bcs.plataforma.pla.GenerarInformeParametrosSistema;
import cl.bcs.plataforma.pla.GenerarInformeParametrosSistema.InGeneraInformeParametros;
import cl.bcs.plataforma.pla.GenerarInformeParametrosSistema.OutGeneraInformeParametros;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/***************************************
 * @version 	1.0
 * @author 		Sebastian Hechenleitner
 * 06-06-2017	SAP-6614 Mantenedor Consulta de Parametros
**************************************/

public class GenerarInformeParametrosSistemaJob extends BcsJob implements JavaCall{
	
	private final Log log = new Log("GenerarInformeParametrosSistemaJob"); 
	
	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma";
	
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
		
		GenerarInformeParametrosSistema generarInformeParametrosSistema = new GenerarInformeParametrosSistema();
		
		InGeneraInformeParametros entrada = new InGeneraInformeParametros();

		entrada.p_IdEmpresa = parIdEmpresa;
		entrada.p_IdUsuario = parIdUsuario;
		entrada.p_IdModulo = parIdModulo;
		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), parIdEmpresa, "PLA.RutaBaseArchivos");
		
		try{
			OutGeneraInformeParametros out = generarInformeParametrosSistema.logica(con,entrada);
			SQL sql=new SQL();
			if(new Long(0).compareTo(out.P_OUT_ERRORESCONTADOR) == 0){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.P_OUT_NOMBRE_ARCHIVO,this.getJobId()));
				sql.batch(con, SQL_UPDATE_ID_PROGRAMA , actualizar, ActualizarPrograma.class);
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