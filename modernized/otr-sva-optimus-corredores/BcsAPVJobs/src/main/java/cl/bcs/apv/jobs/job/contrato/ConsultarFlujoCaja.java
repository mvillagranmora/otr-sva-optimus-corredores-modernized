package cl.bcs.apv.jobs.job.contrato;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cl.bcs.apv.jobs.job.JsonLegacy;
import cl.bcs.apv.jobs.job.contrato.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.apv.jobs.job.contrato.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO;
import cl.bcs.apv.jobs.job.contrato.SaldoFlujoCaja.ObjERRORESDETALLE;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.pla.ConsultarFlujoCaja} (the Colaboradores port carries its input/output as the legacy JSON). The nested data
 * classes are VERBATIM copies of the legacy source (field names ARE the contract, constructor defaults included); only {@code toString} uses
 * {@link JsonLegacy}. Generated from the oracle shims by the Step 2 scratch script; nothing is retyped.
 */
@SuppressWarnings({"unused", "rawtypes"})
public final class ConsultarFlujoCaja {

    private ConsultarFlujoCaja() {
    }

	// legacy cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:44-65 (verbatim)
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
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:67-74 (verbatim)
	public static class PortafolioAConsultar{
		public Long IdPortafolio;
		public String IdMoneda;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:76-83 (verbatim)
	public static class ClienteAConsultar{
		public Long IdCliente;
		public String IdMoneda;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/plataforma/pla/ConsultarFlujoCaja.java:102-118 (verbatim)
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
			return JsonLegacy.texto(this);
		}
	}
}
