// GENERATED DOUBLE of the legacy cl.bcs.fondomutuo.CrearSolicitudAporteReinversion by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class CrearSolicitudAporteReinversion implements JavaCall{
	static 	final Logger logger = LogManager.getLogger(CrearSolicitudAporteReinversion.class);
	
	public static class EntradaCrearSolicitudAporteReinversion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<InSolicitudesAporte> p_SolicitudesAporte;
		
		public EntradaCrearSolicitudAporteReinversion() {
			p_SolicitudesAporte = new ArrayList<InSolicitudesAporte>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesAporte{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondoOrigen;
    	public String IdEmisorOrigen;
    	public BigDecimal CostoUF;
    	public String FechaOrigenInversion;
    	public BigDecimal MontoOrigen;
    	public String IndicadorMLT;
    	public String IdSerieFondo;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public String Visado;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String IdRegimenTributario;
    	public String Glosa;
    	public Long IdAutorizadoOrigen;
    	public Long FolioContrato;
    	public Long IdSolicitudAporteRescateRelacionado;
    	public String FechaEmision;
    	public BigDecimal Porcentaje;
		public Long IdAgrupador;
		public String FechaCertificacionRescateOrigen;
		public String FechaLiquidacionRescateOrigen;
		public String FechaValorCuotaRescateOrigen;
		public String FechaCertificaSpotCompra;
		public String FechaLiquidaSpotCompra;
		public String Switch;
		public String GeneraSpot;
		public BigDecimal MontoPatrimonio;
		public String Canal;
		public String IngresoAutomatico;
		public BigDecimal MontoTotalInversion;
		public Date FechaValorCuota;

		public String TipoReinversion = "";        // SWITCH   -  INTERNACIONAL -  NACIONAL  
		
		public InSolicitudesAporte() {
			this.Switch= "N";
			this.IdAgrupador = 0L;
			this.Porcentaje = new BigDecimal("100");
			this.GeneraSpot = "N";
			this.MontoPatrimonio = BigDecimal.ZERO;
			this.IngresoAutomatico = "N";
			this.MontoTotalInversion = BigDecimal.ZERO;
			this.FechaCertificacionRescateOrigen = "0001-01-01";
			this.FechaLiquidacionRescateOrigen = "0001-01-01";
			this.FechaValorCuotaRescateOrigen = "0001-01-01";
			this.CostoUF = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaRegimenReinversion{
		public Long IdSolicitudAporteRescate;
		public String IdRegimenTributario;
	}
	
	public static class SalidaCrearSolicitudAporteReinversion{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudAporteReinversion(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearSolicitudAporteReinversion", "call", new Object[]{conn, in}, JsonObject.class); }
	
	public SalidaCrearSolicitudAporteReinversion logica(Connection con, EntradaCrearSolicitudAporteReinversion entrada) throws Exception{ if (cl.bcs.oracle.T4Spy.arReal()) return cl.bcs.oracle.T4Real.ar(con, entrada); return cl.bcs.oracle.Doubles.answer("CrearSolicitudAporteReinversion", "logica", new Object[]{con, entrada}, SalidaCrearSolicitudAporteReinversion.class); }
	
	public SolicitudAporteRescate copiarSolicitudAporteRescate(SolicitudAporteRescate Solicitud) { throw new UnsupportedOperationException("oracle double CrearSolicitudAporteReinversion.copiarSolicitudAporteRescate: not part of the double"); }
	
	public boolean calcularHeredarCostoUF(Connection con,InSolicitudesAporte sol,String codigoPortafolio,List<String> regimenesDestino,List<ConsultaRegimenReinversion>regimenSolicitudReinversion) { throw new UnsupportedOperationException("oracle double CrearSolicitudAporteReinversion.calcularHeredarCostoUF: not part of the double"); }
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double CrearSolicitudAporteReinversion.main: not part of the double"); }
}
