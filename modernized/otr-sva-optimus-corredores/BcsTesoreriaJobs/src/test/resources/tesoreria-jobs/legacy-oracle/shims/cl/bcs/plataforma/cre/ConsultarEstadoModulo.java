package cl.bcs.plataforma.cre;

import java.sql.Connection;
import java.util.List;

import com.google.gson.JsonElement;

import cl.bcs.oracle.Hooks;
import cl.bcs.utils.Utils;

/**
 * ORACLE FAKE of legacy cl.bcs.plataforma.cre.ConsultarEstadoModulo (Plataforma/.../cre/ConsultarEstadoModulo.java:19-60, POJOs verbatim).
 * logica() records its input and answers "colaboradores.ConsultarEstadoModulo": {"salida": {p_out_ListaModulos:[{NombreModulo, IdEstadoModulo}]}}
 * (absent: an empty list; "salida": {"p_out_ListaModulos": null} gives a null list), "lanza" scripts a failure.
 */
public class ConsultarEstadoModulo {

	public static class Modulo {
		public String IdEstadoModulo;
		public String NombreModulo;
	}

	public static class OutErroresDetalle {
		public String Codigo;
		public String Descripcion;
	}

	public static class OutAdvertenciasDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class InConsultarEstadoModulo {
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
	}

	public static class OutConsultarEstadoModulo {
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_ERRORESCONTADOR;
		public List<OutAdvertenciasDetalle> p_out_AdvertenciasDetalle;
		public List<OutErroresDetalle> p_out_ErroresDetalle;
		public List<Modulo> p_out_ListaModulos;
	}

	public OutConsultarEstadoModulo logica(Connection con, InConsultarEstadoModulo entrada) throws Exception {
		Hooks.registrar("ConsultarEstadoModulo", entrada);
		Hooks.lanzaSiCorresponde("ConsultarEstadoModulo.lanza");
		JsonElement s = Hooks.objeto("ConsultarEstadoModulo").get("salida");
		if (s == null) {
			OutConsultarEstadoModulo o = new OutConsultarEstadoModulo();
			o.p_out_ListaModulos = new java.util.ArrayList<>();
			return o;
		}
		return Utils.getGsonBuilder().fromJson(s, OutConsultarEstadoModulo.class);
	}
}
