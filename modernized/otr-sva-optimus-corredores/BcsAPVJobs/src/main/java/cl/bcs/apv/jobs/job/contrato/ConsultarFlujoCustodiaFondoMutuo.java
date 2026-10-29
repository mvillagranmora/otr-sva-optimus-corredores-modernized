package cl.bcs.apv.jobs.job.contrato;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cl.bcs.apv.jobs.job.JsonLegacy;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo} (the Colaboradores port carries its input/output as the legacy JSON). The nested data
 * classes are VERBATIM copies of the legacy source (field names ARE the contract, constructor defaults included); only {@code toString} uses
 * {@link JsonLegacy}. Generated from the oracle shims by the Step 2 scratch script; nothing is retyped.
 */
@SuppressWarnings({"unused", "rawtypes"})
public final class ConsultarFlujoCustodiaFondoMutuo {

    private ConsultarFlujoCustodiaFondoMutuo() {
    }

	// legacy cl/bcs/fondomutuo/ConsultarFlujoCustodiaFondoMutuo.java:45-64 (verbatim)
	public static class EntradaConsultarFlujoCustodiaFondoMutuo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdPais;
		public List<InInstrumento> p_Instrumentos;
		public List<InCarteras> p_Carteras;
		public String p_FechaSaldo;
		public Integer p_DiasProyeccion;
		
		
		public EntradaConsultarFlujoCustodiaFondoMutuo() {
			this.p_Instrumentos = new ArrayList<InInstrumento>();
			this.p_Carteras = new ArrayList<InCarteras>();
		}
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/fondomutuo/ConsultarFlujoCustodiaFondoMutuo.java:66-72 (verbatim)
	public static class InInstrumento{
		public Long IdInstrumento;
		public String IdRegimenFondoMutuo;
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/fondomutuo/ConsultarFlujoCustodiaFondoMutuo.java:74-81 (verbatim)
	public static class InCarteras{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/fondomutuo/ConsultarFlujoCustodiaFondoMutuo.java:111-121 (verbatim)
	public static class SalidaConsultarFlujoCustodiaFondoMutuo{
		public List<FlujoFondoMutuoTotalizado> P_OUT_FLUJOFONDOMUTUOTOTALIZADO;
		
		public SalidaConsultarFlujoCustodiaFondoMutuo(){
			this.P_OUT_FLUJOFONDOMUTUOTOTALIZADO = new ArrayList<FlujoFondoMutuoTotalizado>();
		}
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/fondomutuo/ConsultarFlujoCustodiaFondoMutuo.java:123-147 (verbatim)
	public static class FlujoFondoMutuoTotalizado{
		public Long IdInterno;
		public Long IdInstrumento;
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal CantidadDisponible;
		public BigDecimal MontoDisponible;
		public BigDecimal MontoDisponibleCustodia;
		public String RescateTotal;
		public String Nemotecnico;
		public String IdRegimenTributario;
		public String IdMoneda;
		
		public FlujoFondoMutuoTotalizado(){
			this.CantidadDisponible = BigDecimal.ZERO;
			this.MontoDisponible = BigDecimal.ZERO;
			this.MontoDisponibleCustodia = BigDecimal.ZERO;
			this.Nemotecnico = "";
			this.IdMoneda = "";
		}
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}
}
