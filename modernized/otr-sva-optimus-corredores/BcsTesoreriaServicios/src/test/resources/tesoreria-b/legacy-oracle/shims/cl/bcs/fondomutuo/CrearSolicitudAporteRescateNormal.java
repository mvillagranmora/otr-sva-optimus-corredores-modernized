package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;
import java.util.Date;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;

/**
 * ORACLE FAKE of legacy cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal (cl/bcs/fondomutuo/CrearSolicitudAporteRescateNormal.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by ProcesarDistribucion.
 */
public class CrearSolicitudAporteRescateNormal implements JavaCall {

	// legacy cl/bcs/fondomutuo/CrearSolicitudAporteRescateNormal.java:117-142 (verbatim)
	public static class EntradaCrearSolicitudAporteRescateNormal{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public Long p_NumeroDividendo;
		public List<InSolicitudesAporteRescate> p_SolicitudesAporteRescate;
		public List<InFormaPagoSpot> p_FormaPagoSpot;
		public String p_IdOrigenProceso;  // REC: Recaudacion    PAC: Pago automatico  BONIF: Proceso Gestion Bonificacion  CAR: Carga Masiva Fondos Mutuos
		public String p_PermiteOperarProspectos;
		
		public EntradaCrearSolicitudAporteRescateNormal() {
			p_IdTipoCanalIngreso = "OPT";
			p_SolicitudesAporteRescate = new ArrayList<InSolicitudesAporteRescate>();
			p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
			p_NumeroDividendo = 0L;
			p_IdOrigenProceso = "OPT";
			p_PermiteOperarProspectos = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/fondomutuo/CrearSolicitudAporteRescateNormal.java:144-203 (verbatim)
	public static class InSolicitudesAporteRescate{
    	public Long IdInterno;
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String TipoSolicitud;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
    	public String EsPorMontoLiquido;
    	public BigDecimal Monto;
    	public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public Long IdPreferenciaPagoPortafolio;
    	public String Visado;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String FechaOperacion;
    	public String IdRegimenTributario;
    	public String Glosa;
    	public String IndicadorMLT;
    	public String IdSerieFondoDestino;
    	public String IdEmisorDestino;
    	public Long IdAutorizadoOrigen;
    	public BigDecimal Porcentaje;
    	public Long FolioContrato;
    	public String IdMonedaPago;
    	public BigDecimal MontoPatrimonio;
    	
    	public String IngresoAutomatico;
    	public String ForzarSpot; //Parametro para forzar spot independiente si tiene saldo en dolar, para distribucion y CSC
    	public String TipoContratoOperacion;
    	public String FolioContratoOperacion;
    	public String EsTraspaso;
    	public String Canal;
    	public Long IdSolicitudAporteRescateRelacionado;//Para solicitudes BF
    	public Date FechaValorCuota;
    	public String IgnorarMontoMinimo;	// S / N
    	public String SimulaCambioPortafolioEjecutadoOk;
    	public String IngresoDesde;
    	public InSolicitudesAporteRescate(){
    		this.IdMonedaPago = "";
    		this.IdEmisorDestino = "";
    		this.MontoPatrimonio = BigDecimal.ZERO;
    		this.IngresoAutomatico = "N";
    		this.ForzarSpot = "N";
    		this.TipoContratoOperacion = "";
    		this.FolioContratoOperacion = "";
    		this.EsTraspaso = "N";
    		this.Canal = "";
    		this.IdSolicitudAporteRescateRelacionado = 0L;
    		this.IgnorarMontoMinimo = "N";
    		this.SimulaCambioPortafolioEjecutadoOk = "N";
    		this.IngresoDesde = "";
    	}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public Long IdOrigenRecaCliente = 0L;
	}

	// legacy cl/bcs/fondomutuo/CrearSolicitudAporteRescateNormal.java:205-222 (verbatim)
	public static class InFormaPagoSpot{
		
    	public Long IdInternoPagoOperacion;
    	public Long IdComprobantePagoOperacion;
    	public String IdTipoMovimiento;
    	public String IdFormaDePago;
    	public String IdMoneda;
    	public String IdMonedaLiquidacion;
    	public Long IdOperacion;
    	public String RecibePagaPortafolio;
    	public BigDecimal MontoFormaDePago;
    	public String AfectarTesoreria;
    
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/fondomutuo/CrearSolicitudAporteRescateNormal.java:244-258 (verbatim)
	public static class SalidaCrearSolicitudAporteRescateNormal{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudAporteRescateNormal(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/**
	 * FAKE of CrearSolicitudAporteRescateNormal.logica (legacy creates fund contributions/redemptions): records its input and answers
	 * "colaboradores.crearSolicitudAporteRescate": {"mensajes":[{id,tipo,codigo,descripcion}], "idSolicitudBase": n (one result per requested solicitud with
	 * IdSolicitudAporteRescate = base + index and IdTipoMovimientoFondoMutuo = "tipoMovimiento" or APONORM), "sinResultados": true,
	 * "errores": n (default: number of ERROR messages), "lanza": "text"}.
	 */
	public SalidaCrearSolicitudAporteRescateNormal logica(Connection con, EntradaCrearSolicitudAporteRescateNormal in) throws Exception {
		Hooks.registrar("CrearSolicitudAporteRescateNormal", in);
		JsonObject script = Hooks.objeto("crearSolicitudAporteRescate");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		SalidaCrearSolicitudAporteRescateNormal out = new SalidaCrearSolicitudAporteRescateNormal();
		if (script.has("mensajes")) {
			for (com.google.gson.JsonElement e : script.getAsJsonArray("mensajes")) out.P_OUT_MENSAJES.add(Hooks.mensaje(e.getAsJsonObject()));
		}
		long base = script.has("idSolicitudBase") ? script.get("idSolicitudBase").getAsLong() : 7000L;
		String tipo = script.has("tipoMovimiento") ? script.get("tipoMovimiento").getAsString() : "APONORM";
		if (!script.has("sinResultados")) {
			long i = 0;
			for (InSolicitudesAporteRescate s : in.p_SolicitudesAporteRescate) {
				cl.bcs.fondomutuo.pojo.ResultadoSolicitud r = new cl.bcs.fondomutuo.pojo.ResultadoSolicitud();
				r.IdInterno = s.IdInterno;
				r.IdSolicitudAporteRescate = base + (i++);
				r.IdTipoMovimientoFondoMutuo = tipo;
				out.P_OUT_RESULTADO.add(r);
			}
		}
		out.P_OUT_ERRORESCONTADOR = script.has("errores") ? script.get("errores").getAsLong() : MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;
	}

}
