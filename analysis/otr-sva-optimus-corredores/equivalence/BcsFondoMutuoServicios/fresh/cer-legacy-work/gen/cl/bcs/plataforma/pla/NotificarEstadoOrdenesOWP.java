// GENERATED DOUBLE of the legacy cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class NotificarEstadoOrdenesOWP implements JavaCall{

	//ENTRADA Y SALIDA DE LA CLASE
	
	public static class NotificarEstadoOrdenes_Entrada {
		public Long IdEmpresa;
		public Long IdUsuario;
		public Long Solsec;
		public String Fecha;
		public String Estado;
		public String Mercado;	
		public BigDecimal NTitulos;
		public BigDecimal Precio;
		public BigDecimal Comision;
		public BigDecimal Gastos;
		public String Divisa;
		public BigDecimal TipoCambio;
		public String SolsecAsoc;
		public String Nemo;
		public String IdClasificacionTipoInstrumento;	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OrdenModificada{
		public Long IdOrden;
		public BigDecimal Cantidad;
		public BigDecimal PrecioOrden;
		public String FechaOperacion;
		public String IdEstadoOrden;
		public String IdMonedaOrden;
		public BigDecimal TipoCambio;
		public String IdClasificacionTipoInstrumento;	
		public String IdEstadoFacturacion;
		public String Nemotecnico;
		public BigDecimal Comision;
		public BigDecimal Gasto;
		

		public BigDecimal Impuesto; // Ej. IVA
		
		public String SolsecAsoc;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class NotificarEstadoOrdenes_Salida {
		public String Message;
		public String RequestCode;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Estado {
		public String IdEstadoOWP;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class AgregarLog{
		public List<AgregarLog.Log> Logs;
		
		public AgregarLog(){
			Logs = new ArrayList<AgregarLog.Log>();
		}
		
	    public static class Log {
			public Long			IdLog; 
			public Long			IdEmpresa; 
			public String		FechaLog; 
			public String		IdCorrelativoOWP; 
			public String		Glosa; 
			public String		Entrada;
			public String		Salida;
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("NotificarEstadoOrdenesOWP", "call", new Object[]{con, jin}, JsonObject.class); }
	
	
	public void go(Connection con, List<NotificarEstadoOrdenes_Entrada> entrada) throws Exception { cl.bcs.oracle.Doubles.answer("NotificarEstadoOrdenesOWP", "go", new Object[]{con, entrada}, Void.class); }
	
	
	//PRUEBAS
	public static void main(String[] args) throws Exception{ throw new UnsupportedOperationException("oracle double NotificarEstadoOrdenesOWP.main: not part of the double"); }
}



