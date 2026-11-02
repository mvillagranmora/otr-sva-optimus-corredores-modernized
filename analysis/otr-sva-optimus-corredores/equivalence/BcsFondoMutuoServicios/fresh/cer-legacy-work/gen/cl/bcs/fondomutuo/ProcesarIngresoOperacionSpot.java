// GENERATED DOUBLE of the legacy cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.InSolicitudesAporte;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.FormaPagoSpot;

import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ProcesarIngresoOperacionSpot implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ProcesarIngresoOperacionSpot.class);
			
			
	public static class EntradaProcesarIngresoOperacionSpot{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CalculaMargen;
		public String p_UsaReglasNegocio;
		public List<SolicitudAporteRescate> p_DatosOperacion;
		public List<InSolicitudesAporte> p_DatosOrigenInversion;
		public List<FormaPagoSpot> p_DatosFormaPago;
		public String p_PermiteOperarProspectos;
			
		public EntradaProcesarIngresoOperacionSpot(){
			this.p_DatosOperacion = new ArrayList<SolicitudAporteRescate>();
			this.p_DatosOrigenInversion = new ArrayList<InSolicitudesAporte>();
			this.p_DatosFormaPago = new ArrayList<FormaPagoSpot>();
			this.p_PermiteOperarProspectos = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class SalidaProcesarIngresoOperacionSpot {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionObjOut>	P_OUT_IDOPERACIONSPOT;
		
		public SalidaProcesarIngresoOperacionSpot() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Mesa{
		public Long     IdMesa;
		public String toString(){
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class PuntaOperacion{
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal TipoCambioCosto;
		
		public PuntaOperacion() {
			this.PuntaCompra = BigDecimal.ZERO;
			this.PuntaVenta = BigDecimal.ZERO;
			this.TipoCambioCosto = BigDecimal.ZERO;
		}
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("ProcesarIngresoOperacionSpot", "call", new Object[]{conn, in}, JsonObject.class); }
	@Trace
	public SalidaProcesarIngresoOperacionSpot logica(Connection con, EntradaProcesarIngresoOperacionSpot entrada) throws Exception{ return cl.bcs.oracle.Doubles.answer("ProcesarIngresoOperacionSpot", "logica", new Object[]{con, entrada}, SalidaProcesarIngresoOperacionSpot.class); }
	
}