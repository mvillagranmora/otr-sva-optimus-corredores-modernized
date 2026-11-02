// GENERATED DOUBLE of the legacy cl.bcs.normativa.ResumenPatrimonial by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.normativa;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ResumenPatrimonial implements JavaCall{

	public static class	ResumenPatrimonial_Entrada implements Serializable{
		public Long p_IdEmpresa;		
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public Long p_IdUsuario;
		public String p_IdModulo;
	    public String p_FechaDesde;
	    public String p_FechaHasta;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResumenPatrimonial_Salida{
		
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public ResumenPeriodo				P_OUT_RESUMENPERIODO;
		
		public ResumenPatrimonial_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 	   =  new ArrayList<MensajeSalida>();
			this.P_OUT_RESUMENPERIODO  = new ResumenPeriodo();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResumenPeriodo{
		public BigDecimal PatrimonioInicial;
		public BigDecimal PatrimonioFinal;
		public BigDecimal Aportes;
		public BigDecimal Rescates;
		public BigDecimal ComisionDerechoGasto;
		public BigDecimal ResultadoPeriodo;
		public BigDecimal RentabilidadPeriodo;
		
		public ResumenPeriodo() {
			this.PatrimonioInicial=BigDecimal.ZERO;
			this.PatrimonioFinal  =BigDecimal.ZERO;
			this.Aportes		  =BigDecimal.ZERO;
			this.Rescates		  =BigDecimal.ZERO;
			this.ComisionDerechoGasto=BigDecimal.ZERO;
			this.ResultadoPeriodo	 =BigDecimal.ZERO;
			this.RentabilidadPeriodo =BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ComisionesDerechosGastos{
		public BigDecimal TotalComisiones;
		public BigDecimal TotalImpuestos;
		public BigDecimal TotalDerechos;
		public BigDecimal TotalGastos;
		
		public ComisionesDerechosGastos(){
			this.TotalComisiones=BigDecimal.ZERO;
			this.TotalImpuestos=BigDecimal.ZERO;
			this.TotalDerechos=BigDecimal.ZERO;
			this.TotalGastos=BigDecimal.ZERO;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AporteRescate{
		
		public String TipoMovimiento;
		public String IdMoneda;
		public BigDecimal MontoSumado;
		
		public AporteRescate() {
			this.MontoSumado=BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("ResumenPatrimonial", "call", new Object[]{con, jin}, JsonObject.class); }

	
	
	public BigDecimal consultarPatrimonio(Connection con, Date fechaDesde, Date fechaHasta ,ResumenPatrimonial_Entrada in) throws Exception { throw new UnsupportedOperationException("oracle double ResumenPatrimonial.consultarPatrimonio: not part of the double"); }

	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double ResumenPatrimonial.main: not part of the double"); }

}
