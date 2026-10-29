package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.pla.SaldoFlujoCaja}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class SaldoFlujoCaja {

    private SaldoFlujoCaja() {
    }

    public static final String CLASE = "cl.bcs.plataforma.pla.SaldoFlujoCaja";

	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:89-108 (verbatim)
	public static class ServiceEntrada {
		public Long								P_IDEMPRESA;
		public String							P_FECHADESDE;
		public String							P_FECHAHASTA;
		public String							P_USO;
		public String							P_DESPLEGARENUI;
		public String							P_AGRUPARPORORIGEN;
		public Object p_BarraProgreso;
		public List<ObjPORTAFOLIOACONSULTAR>	P_PORTAFOLIOACONSULTAR;

		public ServiceEntrada() {
			this.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>();
			this.P_DESPLEGARENUI = "N";
			this.P_AGRUPARPORORIGEN = "N";
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
		
}

	public static class ObjPORTAFOLIOACONSULTAR {
		public Long		IdPortafolio;
		public String	IdMoneda;
		
		public ObjPORTAFOLIOACONSULTAR(Long idPortafolio, String idMoneda) {
			this.IdPortafolio = idPortafolio;
			this.IdMoneda = idMoneda;
		}
		
		public ObjPORTAFOLIOACONSULTAR() {
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
		
}

	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}
}
