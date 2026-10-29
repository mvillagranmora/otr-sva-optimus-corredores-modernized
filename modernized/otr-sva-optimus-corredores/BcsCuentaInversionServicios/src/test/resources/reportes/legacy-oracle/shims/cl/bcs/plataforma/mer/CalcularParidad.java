package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.oracle.Colaboradores;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.plataforma.mer.CalcularParidad (BcsPlataformaServicios/.../mer/CalcularParidad.java, 959 lines
 * over the HANA financial-parameter tables). The request/response classes below are copied VERBATIM from CalcularParidad.java
 * (CalcularParidadEntrada .. Uso, lines 57-204). The calculation is replaced by a rate table taken from case.json
 * colaboradores.paridad ({"USD>CLP": "950.50"}, key = IdMonedaOrigen + ">" + IdMonedaDestino):
 *   - same currency: factor 1, as CalcularParidad.java:217-251 does for origen = destino = moneda sistema
 *   - rate present:  MontoDestino = MontoOrigen x rate (no rounding here; callers round), Resultado/CodigoMensaje "OK"
 *   - rate absent:   MontoDestino null, Resultado "ERROR" (real: parameter not found, MontoDestino stays null)
 * MontoLocal (amount in the company's local currency) is set equal to MontoDestino: every caller in this suite converts to the system
 * (= local) currency, where the real service gives the same figure. (Used by GenerarArchivoCuentasPorCobrarAnexo16.)
 * The real service's own numeric behaviour is a platform concern and is not pinned by the reporting suite.
 * Every request is logged under llamadasColaboradores.
 */
public class CalcularParidad implements JavaCall {

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        CalcularParidadEntrada in = gson.fromJson(jin.toString(), CalcularParidadEntrada.class);
        CalcularParidadSalida out = logica(con, in);
        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

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
	

    public CalcularParidadSalida logica(Connection con, CalcularParidadEntrada entrada) throws Exception {
        if (Colaboradores.CONFIG.has("canonicalizarParidad")) {
            // GuardarPatrimonioCaja numbers rows from a parallel stream (IdInterno order is not deterministic): record the
            // request sorted by (currency, amount, date) with IdInterno replaced by the position
            CalcularParidadEntrada copia = new CalcularParidadEntrada();
            copia.p_IdEmpresa = entrada.p_IdEmpresa;
            copia.p_IdTrabajo = entrada.p_IdTrabajo;
            List<CalculoEntrada> ord = new ArrayList<>(entrada.p_CalculosEntrada);
            ord.sort(java.util.Comparator.comparing((CalculoEntrada c) -> c.IdMonedaOrigen).thenComparing(c -> c.MontoOrigen).thenComparing(c -> c.Fecha));
            long pos = 1;
            for (CalculoEntrada c : ord) {
                CalculoEntrada n = Utils.getGsonBuilder().fromJson(Utils.getGsonBuilder().toJson(c), CalculoEntrada.class);
                n.IdInterno = pos++;
                copia.p_CalculosEntrada.add(n);
            }
            Colaboradores.registrar("CalcularParidad", copia);
        } else {
            Colaboradores.registrar("CalcularParidad", entrada);
        }
        CalcularParidadSalida salida = new CalcularParidadSalida();
        JsonObject tabla = Colaboradores.CONFIG.has("paridad") ? Colaboradores.CONFIG.getAsJsonObject("paridad") : new JsonObject();
        for (CalculoEntrada calc : entrada.p_CalculosEntrada) {
            CalculoSalida cs = new CalculoSalida();
            cs.IdTrabajo = calc.IdTrabajo;
            cs.IdInterno = calc.IdInterno;
            cs.IdMonedaOrigen = calc.IdMonedaOrigen;
            cs.IdMonedaDestino = calc.IdMonedaDestino;
            cs.ValorCambioLocalModificado = calc.ValorCambioLocalModificado;
            cs.MontoOrigen = calc.MontoOrigen;
            cs.IdUso = calc.IdUso;
            cs.Fecha = Fecha.toDate(calc.Fecha.length() > 10 ? calc.Fecha.substring(0, 10) : calc.Fecha);
            String clave = calc.IdMonedaOrigen + ">" + calc.IdMonedaDestino;
            if (calc.IdMonedaOrigen.equals(calc.IdMonedaDestino)) {
                cs.FactorOrigenDestino = BigDecimal.ONE;
                cs.MontoDestino = calc.MontoOrigen;
                cs.MontoLocal = calc.MontoOrigen;
                cs.Resultado = "OK";
                cs.CodigoMensaje = "OK";
            } else if (tabla.has(clave)) {
                BigDecimal rate = new BigDecimal(tabla.get(clave).getAsString());
                cs.FactorOrigenDestino = rate;
                cs.MontoDestino = calc.MontoOrigen == null ? null : calc.MontoOrigen.multiply(rate);
                cs.MontoLocal = cs.MontoDestino;
                cs.Resultado = "OK";
                cs.CodigoMensaje = "OK";
            } else {
                cs.MontoDestino = null;
                cs.MontoLocal = null;
                cs.Resultado = "ERROR";
                cs.CodigoMensaje = "PARIDAD_NO_ENCONTRADA";
            }
            salida.p_out_CalculosSalida.add(cs);
        }
        return salida;
    }
}
