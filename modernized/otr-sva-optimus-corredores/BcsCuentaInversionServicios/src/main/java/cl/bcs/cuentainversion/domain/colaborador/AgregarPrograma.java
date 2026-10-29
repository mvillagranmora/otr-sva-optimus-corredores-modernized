package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.bat.AgregarPrograma}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class AgregarPrograma {

    private AgregarPrograma() {
    }

    public static final String CLASE = "cl.bcs.plataforma.bat.AgregarPrograma";

	// legacy cl/bcs/plataforma/bat/AgregarPrograma.java:99-117 (verbatim)
	public static class InAgregarPrograma {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		public String			p_IdTipoPrograma;
		public Long				p_Segundos;
		public String			p_FechaSistema;
		public List<Parametro>	p_Parametros;
		public String			p_Comentario;
		public static class Parametro{
			public String	NombreParametro;
			public String	Valor;
			public String	ValorBlob;
		}
		public InAgregarPrograma(){
			this.p_Parametros = new ArrayList<Parametro>();
			this.p_Comentario = "";
		}
	}

	public static class OutAgregarPrograma {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDPROGRAMA;
		public Long P_OUT_IDGRUPOPARAMETRO;
		public String P_OUT_MENSAJE;
	}
}
