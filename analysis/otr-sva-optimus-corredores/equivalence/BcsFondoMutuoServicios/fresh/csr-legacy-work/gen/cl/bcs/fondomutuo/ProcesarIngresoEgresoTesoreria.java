// GENERATED DOUBLE of the legacy cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ProcesarIngresoEgresoTesoreria implements JavaCall{
	
	public static class EntradaProcesarIngresoEgresoTesoreria{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_SimularOperacion;
		public String						p_UsaReglasNegocio;		
		public String                       p_EsIngresoAutomatico;
		List<SolicitudAporteRescate> p_Solicitudes;
		
		public EntradaProcesarIngresoEgresoTesoreria(){
			this.p_Solicitudes = new ArrayList<SolicitudAporteRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaProcesarIngresoEgresoTesoreria {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<IngresoEgresoGenerado_Salida>	P_OUT_INGRESOEGRESOGENERADO;
		public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;
		
		public SalidaProcesarIngresoEgresoTesoreria()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado_Salida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<ResultadoProceso_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class Empleado{
		public Long		IdEmpleado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	


	public static class PreferenciaPagoCorredor{		
		public String		IdTipoMovimiento;
		public String		IdConcepto;
		public String		IdMedioDePago;
		public Long			IdCuentaBanco;
		public String		IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	public static class PreferenciaPagoPortafolio{
		
		public Long    IdPreferenciaPagoPortafolio;
		public Long    IdCuentaBancaria;
		public Long    IdBancoCuentaBancaria;
		public String  NumeroCuentaBancaria;
		public String  IdTipoCuentaBancaria;
		public String  NombreTipoCuentaBancaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("ProcesarIngresoEgresoTesoreria", "call", new Object[]{conn, in}, JsonObject.class); }
	@Trace
	public SalidaProcesarIngresoEgresoTesoreria logica(Connection con, EntradaProcesarIngresoEgresoTesoreria entrada) throws Exception{ return cl.bcs.oracle.Doubles.answer("ProcesarIngresoEgresoTesoreria", "logica", new Object[]{con, entrada}, SalidaProcesarIngresoEgresoTesoreria.class); }
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES

	
	public static void main(String[] args) throws Exception 
	{ throw new UnsupportedOperationException("oracle double ProcesarIngresoEgresoTesoreria.main: not part of the double"); }
	
	
}
