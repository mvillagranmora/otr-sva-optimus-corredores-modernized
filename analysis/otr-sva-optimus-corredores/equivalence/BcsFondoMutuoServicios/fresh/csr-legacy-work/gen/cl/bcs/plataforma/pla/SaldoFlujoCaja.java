// GENERATED DOUBLE of the legacy cl.bcs.plataforma.pla.SaldoFlujoCaja by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class SaldoFlujoCaja implements JavaCall {
	static final Logger logger = LogManager.getLogger(SaldoFlujoCaja.class);
	/*
	 * 
	 * Clases utilitarias que representan objetos IN/OUT
	 */
	public static class ServiceEntrada {
		public Long								P_IDEMPRESA;
		public String							P_FECHADESDE;
		public String							P_FECHAHASTA;
		public String							P_USO;
		public String							P_DESPLEGARENUI;
		public String							P_AGRUPARPORORIGEN;
		public BarraProgreso					p_BarraProgreso;
		public List<ObjPORTAFOLIOACONSULTAR>	P_PORTAFOLIOACONSULTAR;

		public ServiceEntrada() {
			this.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>();
			this.P_DESPLEGARENUI = "N";
			this.P_AGRUPARPORORIGEN = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServiceSalida {
		public List<ObjFLUJOCAJA>			P_OUT_FLUJOCAJA;
		public List<ObjFLUJOCAJADETALLADO>	P_OUT_FLUJOCAJADETALLADO;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<ObjERRORESDETALLE>		P_OUT_ERRORESDETALLE;

		public ServiceSalida() {
			this.P_OUT_FLUJOCAJA = new ArrayList<>();
			this.P_OUT_FLUJOCAJADETALLADO = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*
	 * 
	 * Clases utilitarias que representan objetos de procesamiento
	 * 
	 */
	public static class ObjPORTAFOLIOACONSULTAR {
		public Long		IdPortafolio;
		public String	IdMoneda;
		
		public ObjPORTAFOLIOACONSULTAR(Long idPortafolio, String idMoneda) {
			this.IdPortafolio = idPortafolio;
			this.IdMoneda = idMoneda;
		}
		
		public ObjPORTAFOLIOACONSULTAR() {
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}

		@Override
		public boolean equals(Object o2) {
			ObjPORTAFOLIOACONSULTAR oo = (ObjPORTAFOLIOACONSULTAR)o2;
			return o2 == null? this == null : this.IdPortafolio == oo.IdPortafolio && this.IdMoneda == oo.IdMoneda;
		}
	}

	public static class ObjFLUJOCAJA {
		public String		NombreUso;
		public String		FechaSaldo;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public BigDecimal	Saldo;
		
		public ObjFLUJOCAJA(){
			this.Saldo = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjFLUJOCAJADETALLADO {
		public String		FechaSaldo;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public Long			IdUsoFlujoCaja;
		public String		NombreUso;
		public Long			IdConfiguracionFlujoCaja;
		public String		ItemCalculo;
		public String		FuncionOperador;
		public BigDecimal	Monto;
		
		public ObjFLUJOCAJADETALLADO(){
			this.IdConfiguracionFlujoCaja = 0L;
			this.Monto = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}
	
	public static class ConfiguracionFlujoCaja {
		public Long IdUsoFlujoCaja;
		public String NombreUso;
		public String ItemCalculo;
		public String ItemOrigen;
		public String FuncionOperador;
		public String DesplegarEnUI;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafolioProceso {
		public String Id;
		public Long IdPersona;
		public Long IdPortafolio;
		public String IdMoneda;
		
		public PortafolioProceso() {
			
		}
		
		public PortafolioProceso(long idPersona, long idPortafolio, String idMoneda) {
			this.IdPersona = idPersona;
			this.IdPortafolio = idPortafolio;
			this.IdMoneda = idMoneda;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RangoFecha{
		public String Id;
		public Long IdPersona;
		public Long IdPortafolio;
		public String IdMoneda;
		public Long IdUsoFlujoCaja;
		public String NombreUso;
		public String Item;
		public String ItemCalculo;
		public String ItemOrigen;
		public Long FuncionOperador;
		public String FuncionOperadorString;
		public String DesplegarEnUI;
		public String UsoSolicitado;
		public Date Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosFlujoCaja{
		public String Id;
		public Date FechaLiquidacion;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		public BigDecimal MontoCompra;
		public BigDecimal MontoVenta;
		public String ItemCalculo;
		
		public String TipoCalculoFlujoCaja;  //Tipo de Calculo de flujo Caja por Fecha liquidacion o fecha de adquisicion (solo para compras)
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("SaldoFlujoCaja", "call", new Object[]{con, jin}, JsonObject.class); }
	
	

	/*
	 * Logica Servicio
	 * 
	 */
	@Trace
	public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("SaldoFlujoCaja", "logica", new Object[]{con, in}, ServiceSalida.class); }

	
	
	public RangoFecha getPojoSaldoFecha(PortafolioProceso pp, ConfiguracionFlujoCaja conf, RangoFecha rf){ throw new UnsupportedOperationException("oracle double SaldoFlujoCaja.getPojoSaldoFecha: not part of the double"); }
	
	public ObjFLUJOCAJA getPojoFlujoCaja(RangoFecha rf, ObjFLUJOCAJA fc){ throw new UnsupportedOperationException("oracle double SaldoFlujoCaja.getPojoFlujoCaja: not part of the double"); }
	
	public ObjFLUJOCAJADETALLADO getPojoFlujoDetallado(RangoFecha rf, ObjFLUJOCAJADETALLADO fd){ throw new UnsupportedOperationException("oracle double SaldoFlujoCaja.getPojoFlujoDetallado: not part of the double"); }
	
	public String obtenerTipoMonto(String item){ throw new UnsupportedOperationException("oracle double SaldoFlujoCaja.obtenerTipoMonto: not part of the double"); }
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double SaldoFlujoCaja.main: not part of the double"); }
}