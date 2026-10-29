package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;

import cl.bcs.oracle.Hooks;
import cl.bcs.utils.Utils;

/**
 * ORACLE FAKE of legacy cl.bcs.tesoreria.ConsultarFormatoArchivo (Tesoreria/.../ConsultarFormatoArchivo.java:20-96, POJOs verbatim).
 * logica() records its input and answers "colaboradores.ConsultarFormatoArchivo.formatos" (array of SalidaFormatoArchivo, each with its
 * "Detalles"; absent or empty: an empty list). Like the real logica it copies IdEmpresa and IdUsuario of the request into element 0
 * (ConsultarFormatoArchivo.java:93-94). "colaboradores.ConsultarFormatoArchivo.lanza" scripts a failure.
 */
public class ConsultarFormatoArchivo {

	public static class EntradaFormatoArchivo {
		public Long IdFormatoArchivo;
		public Long IdEmpresa;
		public Long IdUsuario;
	}

	public static class SalidaFormatoArchivo {
		public Long IdEmpresa;
		public Long IdUsuario;
		public Long IdNominaTransferencia;
		public Long IdFormatoArchivo;
		public String CodigoFormatoArchivo;
		public Long IdBanco;
		public String NombreBanco;
		public String IdMoneda;
		public String IdMedioDePago;
		public String FormatoSalida;
		public String Descripcion;
		public String Servicio;
		public String RutaTesoreria;
		public String RutaCompleta;
		public Long CantidadDeTiposDeRegistro;
		public String NombreArchivoSalida;
		public String SeparadorCampos;
		public String SeparadorDecimales;
		public String RellenarCampo;
		public List<FormatoArchivoDetalle> Detalles;
	}

	public static class FormatoArchivoDetalle {
		public Long IdFormatoArchivoDetalle;
		public String CodigoFormatoArchivo;
		public Integer TipoRegistro;
		public String Campo;
		public String TipoDato;
		public Integer Largo;
		public Integer Decimales;
		public String ValorDefecto;
		public Integer Orden;
		public String DescripcionFormatoDetalle;
		public String Formato;
	}

	public List<SalidaFormatoArchivo> logica(Connection conn, EntradaFormatoArchivo in) throws Exception {
		Hooks.registrar("ConsultarFormatoArchivo", in);
		Hooks.lanzaSiCorresponde("ConsultarFormatoArchivo.lanza");
		JsonElement f = Hooks.objeto("ConsultarFormatoArchivo").get("formatos");
		List<SalidaFormatoArchivo> out = new ArrayList<>();
		if (f == null || f.isJsonNull()) return out;
		for (JsonElement e : f.getAsJsonArray()) out.add(Utils.getGsonBuilder().fromJson(e, SalidaFormatoArchivo.class));
		if (out.isEmpty()) return out;
		out.get(0).IdEmpresa = in.IdEmpresa;
		out.get(0).IdUsuario = in.IdUsuario;
		return out;
	}
}
