// GENERATED DOUBLE of the legacy cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;
import cl.bcs.fondomutuo.pojo.MovimientoCustodiaFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ProcesarCustodiaFondoMutuo implements JavaCall {

	public static class EntradaProcesarCustodiaFondoMutuo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public List<MovimientoCustodiaFondoMutuo> p_Movimientos;
		
		public EntradaProcesarCustodiaFondoMutuo() {
			this.p_Movimientos = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaMovimientoCustodiaFondoMutuo {
		public Long IdGenerarMovimientoCustodia;
		public String TablaOrigen;
		public Long FolioCustodia;
		public Long IdCustodia;
		public BigDecimal Cantidad;
		public Date FechaOperacion;
		public Date FechaLiquidacion;
		public Date FechaOrigenCompra;
		public BigDecimal CostoInversionEnFactorAjusteARetirar;
		public BigDecimal ValorFactorAjusteaFechaCurseARetirar;
		public BigDecimal CostoInversionEnFactorAjusteOriginal;
		public BigDecimal CostoInversionEnFactorAjusteActual;
		public BigDecimal ValorFactorOriginal;
		public BigDecimal ValorFactorARetirar;
		public BigDecimal MontoCompraFactorARetirar;
		public BigDecimal MontoCompraFactorOriginal;
		
		public SalidaMovimientoCustodiaFondoMutuo(Long IdGenerarMovimientoCustodia, String TablaOrigen, Long FolioCustodia, Long IdCustodia, BigDecimal Cantidad, Date FechaOperacion, Date FechaLiquidacion, Date FechaOrigenCompra
				, BigDecimal CostoInversionEnFactorAjusteARetirar, BigDecimal ValorFactorAjusteaFechaCurseARetirar, BigDecimal CostoInversionEnFactorAjusteOriginal, BigDecimal CostoInversionEnFactorAjusteActual
				, BigDecimal ValorFactorARetirar, BigDecimal ValorFactorOriginal, BigDecimal MontoCompraFactorARetirar, BigDecimal MontoCompraFactorOriginal){
			this.IdGenerarMovimientoCustodia = IdGenerarMovimientoCustodia;
			this.TablaOrigen = TablaOrigen;
			this.FolioCustodia = FolioCustodia;
			this.IdCustodia = IdCustodia;
			this.Cantidad = Cantidad;
			this.FechaOperacion = FechaOperacion;
			this.FechaLiquidacion = FechaLiquidacion;
			this.FechaOrigenCompra = FechaOrigenCompra;
			this.CostoInversionEnFactorAjusteARetirar = CostoInversionEnFactorAjusteARetirar;
			this.ValorFactorAjusteaFechaCurseARetirar = ValorFactorAjusteaFechaCurseARetirar;
			this.CostoInversionEnFactorAjusteOriginal = CostoInversionEnFactorAjusteOriginal;
			this.CostoInversionEnFactorAjusteActual = CostoInversionEnFactorAjusteActual;
			this.ValorFactorOriginal = ValorFactorOriginal;
			this.ValorFactorARetirar = ValorFactorARetirar;
			this.MontoCompraFactorARetirar = MontoCompraFactorARetirar;
			this.MontoCompraFactorOriginal = MontoCompraFactorOriginal;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaProcesarCustodiaFondoMutuo {
		public Long					P_OUT_ERRORESCONTADOR;
		public List<SalidaMovimientoCustodiaFondoMutuo> P_OUT_MOVIMIENTOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public SalidaProcesarCustodiaFondoMutuo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();	
			this.P_OUT_MOVIMIENTOS = new ArrayList<SalidaMovimientoCustodiaFondoMutuo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("ProcesarCustodiaFondoMutuo", "call", new Object[]{conn, in}, JsonObject.class); }
	
	public SalidaProcesarCustodiaFondoMutuo logica(Connection con,EntradaProcesarCustodiaFondoMutuo entrada) throws Exception { return cl.bcs.oracle.Doubles.answer("ProcesarCustodiaFondoMutuo", "logica", new Object[]{con, entrada}, SalidaProcesarCustodiaFondoMutuo.class); }
	
	
}
