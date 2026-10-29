package cl.bcs.plataforma.bat;

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
 * ORACLE FAKE of cl.bcs.plataforma.bat.AgregarPrograma.
 */
public class AgregarPrograma implements JavaCall {

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

	// legacy cl/bcs/plataforma/bat/AgregarPrograma.java:119-124 (verbatim)
	public static class OutAgregarPrograma {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDPROGRAMA;
		public Long P_OUT_IDGRUPOPARAMETRO;
		public String P_OUT_MENSAJE;
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

	/** FAKE of AgregarPrograma.logica (legacy schedules a batch job): records the input; counter from "colaboradores.agregarProgramaErrores" (default 0). */
	public OutAgregarPrograma logica(Connection con, InAgregarPrograma entrada) throws Exception {
		Hooks.registrar("AgregarPrograma", entrada);
		OutAgregarPrograma out = new OutAgregarPrograma();
		out.P_OUT_ERRORESCONTADOR = Hooks.numero("agregarProgramaErrores", 0L);
		return out;
	}

}
