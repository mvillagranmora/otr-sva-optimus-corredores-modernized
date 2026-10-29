package cl.bcs.apv.jobs.job.contrato;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cl.bcs.apv.jobs.job.JsonLegacy;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.pla.SaldoFlujoCaja} (the Colaboradores port carries its input/output as the legacy JSON). The nested data
 * classes are VERBATIM copies of the legacy source (field names ARE the contract, constructor defaults included); only {@code toString} uses
 * {@link JsonLegacy}. Generated from the oracle shims by the Step 2 scratch script; nothing is retyped.
 */
@SuppressWarnings({"unused", "rawtypes"})
public final class SaldoFlujoCaja {

    private SaldoFlujoCaja() {
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
			return JsonLegacy.texto(this);
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
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/plataforma/pla/SaldoFlujoCaja.java:193-196 (verbatim)
	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}
}
