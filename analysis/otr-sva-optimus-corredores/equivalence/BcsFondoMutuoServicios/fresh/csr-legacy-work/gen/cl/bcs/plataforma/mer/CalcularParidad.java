// GENERATED DOUBLE of the legacy cl.bcs.plataforma.mer.CalcularParidad by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularParidad implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CalcularParidad.class.getName());
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("CalcularParidad", "call", new Object[]{con, jin}, JsonObject.class); }
	
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
	
	public static class ParametroFinanciero{
		public String IdParametroFinanciero;
	    public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public Date Fecha;
		public BigDecimal Valor;
		public BigDecimal FactorInverso;
		public String IdUso;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Factor{
		public String IdParametroFinanciero;
	    public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public Date Fecha;
		public BigDecimal Factor;
		public String IdUso;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionParametro{
		public String IdParametroFinanciero;
		public String IdMonedaParametro;
		public String IdMonedaValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
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
	
	public static class Uso{
		public String IdUso;
		public String NombreUso;
		public Long Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public CalcularParidadSalida logica(Connection con, CalcularParidadEntrada entrada) throws Exception { return cl.bcs.oracle.Doubles.answer("CalcularParidad", "logica", new Object[]{con, entrada}, CalcularParidadSalida.class); }
	
	public Factor buscarFactor(List<Factor> factores, String IdMonedaOrigen, String IdMonedaDestino, Date Fecha) { throw new UnsupportedOperationException("oracle double CalcularParidad.buscarFactor: not part of the double"); }
	
	public Factor copiarFactor(Factor antiguo) { throw new UnsupportedOperationException("oracle double CalcularParidad.copiarFactor: not part of the double"); }
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double CalcularParidad.main: not part of the double"); }
	
	public static class ParametroFinanciero_Proceso {
		public Long IdInterno;
		public String IdParametroFinanciero;
		public Date FechaParametroFinanciero;
		public String Nombre;
		public BigDecimal Valor;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

}