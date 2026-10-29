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

/**
 * ORACLE FAKE of legacy cl.bcs.fondomutuo.CalcularMontoNumeroCuota (cl/bcs/fondomutuo/CalcularMontoNumeroCuota.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by ProcesarDistribucion.
 */
public class CalcularMontoNumeroCuota implements JavaCall {

	// legacy cl/bcs/fondomutuo/CalcularMontoNumeroCuota.java:48-59 (verbatim)
	public static class EntradaCalcularMontoNumeroCuota {
		public Long p_IdEmpresa;
		public List<Serie> p_Series;
		
		public EntradaCalcularMontoNumeroCuota() {
			this.p_Series = new ArrayList<Serie>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/fondomutuo/CalcularMontoNumeroCuota.java:61-81 (verbatim)
	public static class Serie extends BcsPojo{
		public Long IdInterno;
		public String Fecha;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public String IdMonedaFondo;
	 	public String IdMonedaInversion;
	 	public String TipoSolicitud;
	 	public String TipoFactorAjuste;
	 	public Long IdEmpresa;
	 	public BigDecimal Porcentaje;
	 	public String IdTipoMovimientoFondoMutuo;
	 	
	 	public Serie() {
	 		this.IdTipoMovimientoFondoMutuo = "";
	 	}
	 	
	 	
	}

	// legacy cl/bcs/fondomutuo/CalcularMontoNumeroCuota.java:147-169 (verbatim)
	public static class Resultado{
		public Long IdInterno;
		public String TipoSolicitud;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal ValorParidad;
	 	public BigDecimal ValorFactorAjuste;
	 	public BigDecimal CostoEnFactorAjuste;
	 	public String IdMonedaSistema;
	 	public BigDecimal MontoMonedaSistema;
	 	public String Resultado;
	 	public String CodigoMensaje;
	 	public BigDecimal Porcentaje;
	 	public Long DecimalesValorCuota;
	 	public Long DecimalesNumeroCuotas;
	 	public String FechaValorCuota;
	 	
	 	public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/fondomutuo/CalcularMontoNumeroCuota.java:83-93 (verbatim)
	public static class SalidaCalcularMontoNumeroCuota{
		public List<Resultado> P_OUT_RESULTADO;
		
		public SalidaCalcularMontoNumeroCuota(){
			this.P_OUT_RESULTADO = new ArrayList<Resultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/**
	 * FAKE of CalcularMontoNumeroCuota.logica (legacy converts amounts of USD funds): records its input and answers, for every requested series,
	 * {IdInterno, Monto = Monto x "colaboradores.calcularMontoNumeroCuota.factor" (default 1), NumeroCuotas = "cuotas" (default 10)};
	 * "omitir": [IdInterno...] leaves series without result; "lanza": "text".
	 */
	public SalidaCalcularMontoNumeroCuota logica(Connection con, EntradaCalcularMontoNumeroCuota in) throws Exception {
		Hooks.registrar("CalcularMontoNumeroCuota", in);
		JsonObject script = Hooks.objeto("calcularMontoNumeroCuota");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		SalidaCalcularMontoNumeroCuota out = new SalidaCalcularMontoNumeroCuota();
		java.util.Set<Long> omitir = new java.util.HashSet<>();
		if (script.has("omitir")) for (com.google.gson.JsonElement e : script.getAsJsonArray("omitir")) omitir.add(e.getAsLong());
		BigDecimal factor = script.has("factor") ? script.get("factor").getAsBigDecimal() : BigDecimal.ONE;
		BigDecimal cuotas = script.has("cuotas") ? script.get("cuotas").getAsBigDecimal() : new BigDecimal("10");
		if (in.p_Series != null) {
			for (Serie s : in.p_Series) {
				if (omitir.contains(s.IdInterno)) continue;
				Resultado r = new Resultado();
				r.IdInterno = s.IdInterno;
				r.TipoSolicitud = s.TipoSolicitud;
				r.Monto = s.Monto.multiply(factor);
				r.NumeroCuotas = cuotas;
				out.P_OUT_RESULTADO.add(r);
			}
		}
		return out;
	}

}
