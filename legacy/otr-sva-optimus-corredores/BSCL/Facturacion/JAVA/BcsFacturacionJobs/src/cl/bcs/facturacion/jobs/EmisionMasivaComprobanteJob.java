package cl.bcs.facturacion.jobs;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.EmisionMasivaComprobante;
import cl.bcs.facturacion.EmisionMasivaComprobante.InEmisionComprobante;
import cl.bcs.facturacion.EmisionMasivaComprobante.OutEmisionComprobante;
import cl.bcs.scheduler.BcsJob;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class EmisionMasivaComprobanteJob extends BcsJob implements JavaCall {
	
	private final Log log = new Log("EmisionMasivaComprobanteJob"); 
	
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/facturacion/jobs/sql/ComprobanteEmisionMasiva_update.sql");
	
	public static class ActualizarPrograma{
		public String RutaLog;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaLog,Long IdPrograma){
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma;
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		EmisionMasivaComprobante EmisionComprobante = new  EmisionMasivaComprobante();
		InEmisionComprobante entrada= new InEmisionComprobante();
		
		Long parIdEmpresa=Long.parseLong(this.getParam("p_IdEmpresa"));
		Long parIdUsuario=Long.parseLong(this.getParam("p_IdUsuario"));
		String parIdModulo = this.getParam("p_IdModulo");
		String parIdClasificacionTipoInstrumento = this.getParam("p_IdClasificacionTipoInstrumento");
		
		String parIdTipoInstrumento = this.getParam("p_IdTipoInstrumento");
		String parIdTipoComprobante = this.getParam("p_IdTipoComprobante");
		String parIdCondicionLiquidacion = this.getParam("p_IdCondicionLiquidacion");
		String parIdBoveda = this.getParam("p_IdBoveda");
		String parFechaMovimientoComprobante = this.getParam("p_FechaMovimientoComprobante");
		
		String parGlosa = this.getParam("p_Glosa");
		String parSelCliente = this.getParam("p_SelCliente");
		String parsimularOperacion= this.getParam("p_simularOperacion");
		String parUsaReglasNegocio = this.getParam("p_UsaReglasNegocio");
		String parEsIngresoAutomatico = this.getParam("p_EsIngresoAutomatico");
		String parMovimientoFacturarjson = this.getParam("p_MovimientoFacturarjson");
		String parRuta =  Utils.getConfiguracion(con, Utils.getSchema(), parIdEmpresa, "PLA.RutaBaseArchivos");
		
		entrada.p_IdEmpresa=parIdEmpresa;
		entrada.p_IdUsuario=parIdUsuario;
		entrada.p_IdModulo=parIdModulo;
		entrada.p_IdClasificacionTipoInstrumento = parIdClasificacionTipoInstrumento;
		entrada.p_IdTipoInstrumento=parIdTipoInstrumento;
		entrada.p_IdTipoComprobante=parIdTipoComprobante;
		entrada.p_IdCondicionLiquidacion=parIdCondicionLiquidacion;
		entrada.p_IdBoveda=parIdBoveda;
		entrada.p_FechaMovimientoComprobante=parFechaMovimientoComprobante;
		entrada.p_Glosa=parGlosa;
		entrada.p_SelCliente=parSelCliente;
		entrada.p_simularOperacion=parsimularOperacion;
		entrada.p_UsaReglasNegocio=parUsaReglasNegocio;
		entrada.p_EsIngresoAutomatico=parEsIngresoAutomatico;
		
		JsonParser parser     = new JsonParser();
        JsonObject dataJsonIn  = parser.parse(parMovimientoFacturarjson).getAsJsonObject();
		
        entrada.p_MovimientoFacturarjson=dataJsonIn;
		entrada.p_Ruta=parRuta;
		
	
		try{
			OutEmisionComprobante out=EmisionComprobante.logica(con,entrada);
			SQL sql=new SQL();
			if(new Long(0).compareTo(out.P_OUT_ERRORESCONTADOR) == 0){
				con.commit();
			}else{
				con.rollback();
			}
			if(out.P_OUT_NOMBREARCHIVO != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.P_OUT_NOMBREARCHIVO,this.getJobId()));
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
