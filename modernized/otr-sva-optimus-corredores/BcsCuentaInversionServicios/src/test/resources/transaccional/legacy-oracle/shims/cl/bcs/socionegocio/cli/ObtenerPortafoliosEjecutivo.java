package cl.bcs.socionegocio.cli;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;

/**
 * ORACLE FAKE of cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo (scripted).
 */
public class ObtenerPortafoliosEjecutivo implements JavaCall {

	// legacy cl/bcs/socionegocio/cli/ObtenerPortafoliosEjecutivo.java:90-106 (verbatim)
	public static class ObtenerPortafoliosEjecutivo_entrada{
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		public String	p_filtrarListaPortafolios;
		public List<Long> p_ListaIdPortafolios;
		
		public ObtenerPortafoliosEjecutivo_entrada() {
			this.p_ListaIdPortafolios = new ArrayList<Long>();
			this.p_filtrarListaPortafolios = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

	/**
	 * FAKE of ObtenerPortafoliosEjecutivo.getPortafoliosEjecutivo (legacy resolves the portfolios an employee may see). Records the input.
	 * p_filtrarListaPortafolios = S: returns the given list filtered to those in "colaboradores.portafoliosPermitidos" (default: all of them);
	 * p_filtrarListaPortafolios = N: returns "colaboradores.portafoliosEjecutivo" (default none).
	 */
	public List<Long> getPortafoliosEjecutivo(Connection con, ObtenerPortafoliosEjecutivo_entrada entrada) throws Exception {
		Hooks.registrar("ObtenerPortafoliosEjecutivo", entrada);
		List<Long> out = new ArrayList<>();
		if ("S".equals(entrada.p_filtrarListaPortafolios)) {
			boolean hayFiltro = Hooks.colaboradores.has("portafoliosPermitidos");
			for (Long p : entrada.p_ListaIdPortafolios) {
				boolean ok = !hayFiltro;
				for (com.google.gson.JsonElement e : Hooks.lista("portafoliosPermitidos")) if (e.getAsLong() == p) ok = true;
				if (ok) out.add(p);
			}
		} else {
			for (com.google.gson.JsonElement e : Hooks.lista("portafoliosEjecutivo")) out.add(e.getAsLong());
		}
		return out;
	}

}
