package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.contrato.SaldoFlujoCaja.ObjERRORESDETALLE;
import cl.bcs.tesoreria.javacall.contrato.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.tesoreria.javacall.contrato.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO;
import cl.bcs.tesoreria.javacall.contrato.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.plataforma.pla.ConsultarFlujoCaja} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class ConsultarFlujoCaja {

	private ConsultarFlujoCaja() {
	}


	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:44-65 (verbatim)
	public static class EntradaConsultarFlujoCaja{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	   	public String p_UsaReglasNegocio;
		public String p_FechaDesde;
	    public String p_FechaHasta;
		public String p_Uso;
		public String p_DesplegarEnUI;
		public String p_AgruparPorOrigen;
	    public List<PortafolioAConsultar> p_PortafolioAConsultar;
	    public List<ClienteAConsultar> p_ClienteAConsultar;
	    
	    public EntradaConsultarFlujoCaja() {
	    	this.p_PortafolioAConsultar = new ArrayList<PortafolioAConsultar>();
	    	this.p_ClienteAConsultar = new ArrayList<ClienteAConsultar>();
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:67-74 (verbatim)
	public static class PortafolioAConsultar{
		public Long IdPortafolio;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:76-83 (verbatim)
	public static class ClienteAConsultar{
		public Long IdCliente;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:85-92 (verbatim)
	public static class ClientePortafolio{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:94-100 (verbatim)
	public static class Uso{
		public String NombreUso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:102-118 (verbatim)
	public static class SalidaConsultarFlujoCaja{
		public Long P_OUT_ERRORESCONTADOR;
		public List<ObjFLUJOCAJA> P_OUT_FLUJOCAJA;
		public List<ObjFLUJOCAJADETALLADO> P_OUT_FLUJOCAJADETALLADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConsultarFlujoCaja() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_FLUJOCAJA = new ArrayList<ObjFLUJOCAJA>();
			this.P_OUT_FLUJOCAJADETALLADO = new ArrayList<ObjFLUJOCAJADETALLADO>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:120-130 (verbatim)
	public static class OutFlujoCaja{
		public String NombreUso;
		public String FechaSaldo;
		public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Saldo;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:132-145 (verbatim)
	public static class OutFlujoCajaDetallado{
		public String FechaSaldo;
		public Long IdPortafolio;
        public String IdMoneda;
        public Long IdUsoFlujoCaja;
        public String NombreUso;
        public Long IdConfiguracionFlujoCaja;
        public String ItemCalculo;
        public BigDecimal Monto;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
