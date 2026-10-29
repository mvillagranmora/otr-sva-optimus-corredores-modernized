package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

/**
 * ORACLE FAKE of legacy cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC (Plataforma/.../pla/ValidaEjecucionProcesoRecPAC.java:25-130).
 * POJOs as legacy; logica() records its input and answers an empty output (the job ignores it);
 * "colaboradores.ValidaEjecucionProcesoRecPAC.lanza" scripts a failure.
 */
public class ValidaEjecucionProcesoRecPAC {

	public static class ValidaEjecucionProcesoRecPACEntrada {
		public Long p_IdEmpresa;
		public String p_ValorNuevo;
	}

	public static class ValidaEjecucionProcesoRecPACSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public ValidaEjecucionProcesoRecPACSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}

	public ValidaEjecucionProcesoRecPACSalida logica(Connection conn, ValidaEjecucionProcesoRecPACEntrada in) throws Exception {
		Hooks.registrar("ValidaEjecucionProcesoRecPAC", in);
		Hooks.lanzaSiCorresponde("ValidaEjecucionProcesoRecPAC.lanza");
		return new ValidaEjecucionProcesoRecPACSalida();
	}
}
