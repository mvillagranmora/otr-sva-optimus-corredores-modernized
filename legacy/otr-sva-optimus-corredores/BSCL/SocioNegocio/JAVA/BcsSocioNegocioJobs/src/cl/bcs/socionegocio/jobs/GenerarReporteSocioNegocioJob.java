package cl.bcs.socionegocio.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.soc.GenerarReporteSocioNegocio;
import cl.bcs.socionegocio.soc.GenerarReporteSocioNegocio.InGeneraReporteSocioNegocio;
import cl.bcs.socionegocio.soc.GenerarReporteSocioNegocio.OutGeneraReporteSocioNegocio;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarReporteSocioNegocioJob extends BcsJob implements JavaCall{
	private final Log log = new Log("SOC.GenerarReporteSocioNegocioJob");
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/socionegocio/jobs/sql/GenerarReporteSocioNegocio_Update.sql");
	
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
		
		GenerarReporteSocioNegocio socioNegocio = new GenerarReporteSocioNegocio();
		InGeneraReporteSocioNegocio entrada = new InGeneraReporteSocioNegocio();
		
		
		entrada.p_IdEmpresa =Long.parseLong(this.getParam("p_IdEmpresa"));
		entrada.p_IdUsuario = Long.parseLong(this.getParam("p_IdUsuario"));
		entrada.p_IdModulo = this.getParam("p_IdModulo");
		entrada.p_EsCliente =  this.getParam("p_EsCliente");
		entrada.p_EsEmpleado =  this.getParam("p_EsEmpleado");
		entrada.p_EsProveedor =  this.getParam("p_EsProveedor");
		entrada.p_EsContraparte =  this.getParam("p_EsContraparte");
		entrada.p_FechaHasta =  this.getParam("p_FechaHasta");
		entrada.p_FechaDesde =  this.getParam("p_FechaDesde");
		entrada.p_Alias =  this.getParam("p_Alias");
		entrada.p_IdTipoDocumentoIdentidadDefecto =  this.getParam("p_IdTipoDocumentoIdentidadDefecto");
		entrada.p_ListaDocumentosIdentidad =  this.getParam("p_ListaDocumentosIdentidad");
		entrada.p_NombreCompleto =  this.getParam("p_NombreCompleto");
		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");
		
		try{
			OutGeneraReporteSocioNegocio out=socioNegocio.logica(con,entrada);
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
		
		return null;
	}
}
