// GENERATED DOUBLE of the legacy cl.bcs.fondomutuo.CalculaCustodiaRescate by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class CalculaCustodiaRescate implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CalculaCustodiaRescate.class);
	
	public static class EntradaCalculaCustodiaRescate{
		
		public Long p_IdEmpresa;		
		public List<InSolicitud> p_Solicitudes;
		
		public EntradaCalculaCustodiaRescate(){
			this.p_Solicitudes = new ArrayList<InSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitud{
		
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public String IdMoneda;
	 	public String IdMonedaFondo;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal ValorParidad;
	 	public String TipoSolicitud;
	 	public Long IdInstrumento;
	 	public Long IdCliente;
	 	public Long IdPortafolio;
	 	public String MontoMonedaSistema;
	 	public Date FechaValorCuota;
		public String IdRegimenTributario;
		public Long IdOmnibus;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudProceso{
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public String IdMoneda;
	 	public String IdMonedaFondo;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal CostoMonedaLocal;
	 	public BigDecimal ParidadFechaCertificacion;
	 	public String TipoSolicitud;
	 	public Long IdInstrumento;
	 	public Long IdCliente;
	 	public Long IdPortafolio;
	 	public BigDecimal MontoMonedaSistema;
	 	public Date FechaValorCuota;
	 	public BigDecimal CantidadDisponible;
	 	public String IdRegimenTributario;
	 	public Long IdOmnibus;
	 	public String EstadoProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdRegimenFondoMutuo;
		public BigDecimal CantidadDisponiblePortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalculaCustodiaRescate{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoCalculaCustodiaRescate> P_OUT_RESULTADO;
		
		public SalidaCalculaCustodiaRescate(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCalculaCustodiaRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoCalculaCustodiaRescate{
		
		public Long IdSolicitudAporteRescate;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal ValorParidad;
	 	public BigDecimal CantidadDisponible;
	 	public BigDecimal MontoMonedaSistema;
	 	public BigDecimal CostoEnFactorAjuste;
	 	public BigDecimal ValorFactorAjuste; 
	 	public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LlaveCarterasAgrupadas{
		public Long IdPortafolio;
		public Long IdInstrumento;
		
		@Override
		public int hashCode() {
			return 31 * this.IdPortafolio.hashCode() + this.IdInstrumento.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			LlaveCarterasAgrupadas llave = (LlaveCarterasAgrupadas) obj;
			return this.IdPortafolio.equals(llave.IdPortafolio) && this.IdInstrumento.equals(llave.IdInstrumento);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarteraAgrupada{
		public LlaveCarterasAgrupadas Id;
		public BigDecimal CantidadTotal;
		public Map<String, Cartera> CarterasPorRegimen;
		
		public CarteraAgrupada() {
			this.CarterasPorRegimen = new HashMap<String, Cartera>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroConsultarCartera {
		public List<Long> IdPortafolios;
		public List<Long> IdInstrumentos;
		public Long IdEmpresa;
		
		public ParametroConsultarCartera() {
			this.IdPortafolios = new ArrayList<Long>();
			this.IdInstrumentos = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("CalculaCustodiaRescate", "call", new Object[]{conn, in}, JsonObject.class); }
	
	public SalidaCalculaCustodiaRescate logica(Connection con, EntradaCalculaCustodiaRescate entrada) throws Exception{ return cl.bcs.oracle.Doubles.answer("CalculaCustodiaRescate", "logica", new Object[]{con, entrada}, SalidaCalculaCustodiaRescate.class); }
}
