package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class ObtenerPortafoliosEjecutivo {

    private ObtenerPortafoliosEjecutivo() {
    }

    public static final String CLASE = "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo";

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
		
}
}
