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

	// legacy cl/bcs/fondomutuo/CalcularMontoNumeroCuota.java:95-106 (verbatim)
	public static class ValorCuotaTipoParidad{
		public String IdSerieFondo;
		public BigDecimal ValorCuota;
		public BigDecimal ValorDolarAporte;
		public BigDecimal ValorDolarRescate;
		public Long DecimalesCuota;
		public Long DecimalesValorCuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

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


	/** FAKE of the legacy logica: records its input (Hooks.registrar) and answers the script colaboradores.<Servicio> of the case (see oracle/Hooks.java); nothing of the legacy behaviour is reproduced. */
	public SalidaCalcularMontoNumeroCuota logica(Connection con, EntradaCalcularMontoNumeroCuota in) throws Exception {
		Hooks.registrar("CalcularMontoNumeroCuota", in);
		return Hooks.salida("CalcularMontoNumeroCuota", SalidaCalcularMontoNumeroCuota.class);
	}

}
