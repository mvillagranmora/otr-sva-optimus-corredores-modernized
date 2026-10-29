package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
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
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.plataforma.pla.SaldoFlujoCaja} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class SaldoFlujoCaja {

	private SaldoFlujoCaja() {
	}


	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:89-108 (verbatim)
	public static class ServiceEntrada {
		public Long								P_IDEMPRESA;
		public String							P_FECHADESDE;
		public String							P_FECHAHASTA;
		public String							P_USO;
		public String							P_DESPLEGARENUI;
		public String							P_AGRUPARPORORIGEN;
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

	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:110-126 (verbatim)
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

	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:133-154 (verbatim)
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

	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:156-170 (verbatim)
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

	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:172-191 (verbatim)
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

	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:193-196 (verbatim)
	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}
}
