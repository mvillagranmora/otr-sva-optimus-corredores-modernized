package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;

/**
 * ORACLE FAKE of cl.bcs.plataforma.mer.CalcularParidad.
 */
public class CalcularParidad implements JavaCall {

	// legacy cl/bcs/plataforma/mer/CalcularParidad.java:57-69 (verbatim)
	public static class CalcularParidadEntrada {
		public Long p_IdEmpresa;
		public Long p_IdTrabajo;
		public List<CalculoEntrada> p_CalculosEntrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalcularParidadEntrada() {
			this.p_CalculosEntrada = new ArrayList<CalculoEntrada>();
		}
	}

	// legacy cl/bcs/plataforma/mer/CalcularParidad.java:71-89 (verbatim)
	public static class CalculoEntrada {
		public Long IdTrabajo;
		public Long IdInterno;
		public String Fecha;
		public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
		public BigDecimal MontoOrigen;
		public String IdUso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalculoEntrada(){
			this.ValorCambioLocalModificado = BigDecimal.ZERO;
			this.IdUso = "";
		}
	}

	// legacy cl/bcs/plataforma/mer/CalcularParidad.java:126-180 (verbatim)
	public static class CalculoSalida {
		public Long IdTrabajo;
		public Long IdInterno;
		public java.sql.Date Fecha;
		public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public BigDecimal ValorCambioLocalModificado;
		public BigDecimal MontoOrigen;
		public String IdUso;
		
		public BigDecimal FactorOrigenDestino;
		public BigDecimal MontoDestino;
		public BigDecimal FactorOrigenLocal;
		public BigDecimal MontoLocal;
		
		public BigDecimal FactorOrigenCambio;
		public java.sql.Date FechaOrigenCambio;
		public String IdTipoRegistroOrigenCambio;
		
		public BigDecimal FactorCambioLocal;
		public java.sql.Date FechaCambioLocal;
		public String IdTipoRegistroCambioLocal;
		
		public BigDecimal FactorLocalCambio;
		public java.sql.Date FechaLocalCambio;
		public String IdTipoRegistroLocalCambio;
		
		public BigDecimal FactorCambioDestino;
		public java.sql.Date FechaCambioDestino;
		public String IdTipoRegistroCambioDestino;
		
		public String Resultado;
		public String CodigoMensaje;
		public BigDecimal ValorParametroFinanciero;
		public Date FechaParametroFinanciero;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalculoSalida(){
			this.FactorOrigenDestino = BigDecimal.ZERO;
			this.FactorOrigenLocal = BigDecimal.ZERO;
			this.FactorOrigenCambio = BigDecimal.ZERO;
			this.IdTipoRegistroOrigenCambio = "";
			this.FactorCambioLocal = BigDecimal.ZERO;
			this.IdTipoRegistroCambioLocal = "";
			this.FactorLocalCambio = BigDecimal.ZERO;
			this.IdTipoRegistroLocalCambio = "";
			this.FactorCambioDestino = BigDecimal.ZERO;
			this.IdTipoRegistroCambioDestino = "";
			this.ValorParametroFinanciero =BigDecimal.ZERO;
		}
	
	}

	// legacy cl/bcs/plataforma/mer/CalcularParidad.java:182-193 (verbatim)
	public static class CalcularParidadSalida {
		public List<CalculoSalida> p_out_CalculosSalida;
		public Long	 P_OUT_ERRORESCONTADOR = 0L;
		
		public CalcularParidadSalida(){
			this.p_out_CalculosSalida = new ArrayList<CalculoSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

	/** FAKE of CalcularParidad.logica (legacy converts amounts with the exchange rates): records the input; MontoDestino = MontoOrigen * "colaboradores.factorParidad" (default 1). */
	public CalcularParidadSalida logica(Connection con, CalcularParidadEntrada entrada) throws Exception {
		Hooks.registrar("CalcularParidad", entrada);
		CalcularParidadSalida out = new CalcularParidadSalida();
		BigDecimal f = Hooks.colaboradores.has("factorParidad") ? Hooks.colaboradores.get("factorParidad").getAsBigDecimal() : BigDecimal.ONE;
		for (CalculoEntrada c : entrada.p_CalculosEntrada) {
			CalculoSalida s = new CalculoSalida();
			s.IdInterno = c.IdInterno;
			s.MontoDestino = c.MontoOrigen.multiply(f);
			out.p_out_CalculosSalida.add(s);
		}
		return out;
	}

}
