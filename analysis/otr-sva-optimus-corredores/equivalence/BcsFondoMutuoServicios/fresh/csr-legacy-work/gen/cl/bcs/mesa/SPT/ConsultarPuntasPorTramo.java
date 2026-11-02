// GENERATED DOUBLE of the legacy cl.bcs.mesa.SPT.ConsultarPuntasPorTramo by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarPuntasPorTramo implements JavaCall{
	
	
	public static class ConsultarPuntasPorTramoEntrada {
		public Long p_IdEmpresa;
		public String p_IdMoneda;
		public Long p_MontoPorTramo = 0L;
		public String p_Canal = "Punta";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarPuntasPorTramoSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<PuntaPorTramoSalida> P_OUT_PUNTAS;
		
		public ConsultarPuntasPorTramoSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_PUNTAS = new ArrayList<PuntaPorTramoSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Punta {
		public Long IdPuntas;
		public Long IdEmpresa;
		public java.sql.Timestamp Fecha;
		public String IdUsuario;
		public BigDecimal AperturaPrecio;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String IdMonedaPunta;
		public String CheckPrecioPorTramo;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntaPorTramo {
		public Long IdPuntasPorTramo;
		public Long IdEmpresa;
		public Long IdPuntas;
		public BigDecimal MontoPorTramo;
		public BigDecimal PorcentajeTramo;
		public BigDecimal MontoCentavos;
		public BigDecimal ValorCompra;
		public BigDecimal ValorVenta;
		public String Estado;
		public String IdUsuario;
		public java.sql.Timestamp FechaModificacion;
		public String Canal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntaPorTramoSalida {
		public BigDecimal MontoPorTramo;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal PuntaCompraDefault;
		public BigDecimal PuntaVentaDefault;
		public String CheckPrecioPorTramo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarPuntasPorTramo", "call", new Object[]{con, jin}, JsonObject.class); }
	
	public ConsultarPuntasPorTramoSalida logica(Connection conn, ConsultarPuntasPorTramoEntrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarPuntasPorTramo", "logica", new Object[]{conn, in}, ConsultarPuntasPorTramoSalida.class); }
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double ConsultarPuntasPorTramo.main: not part of the double"); }
}