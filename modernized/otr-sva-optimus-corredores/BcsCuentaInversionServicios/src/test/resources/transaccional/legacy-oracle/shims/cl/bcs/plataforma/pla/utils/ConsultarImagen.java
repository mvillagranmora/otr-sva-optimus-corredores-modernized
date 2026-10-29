package cl.bcs.plataforma.pla.utils;

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
import cl.bcs.socionegocio.pojo.SalidaEstandar;

/**
 * ORACLE FAKE of cl.bcs.plataforma.pla.utils.ConsultarImagen.
 */
public class ConsultarImagen implements JavaCall {

	// legacy cl/bcs/plataforma/pla/utils/ConsultarImagen.java:31-34 (verbatim)
	public static class ConsultarImagenEntrada {
		public long p_IdEmpresa;
		public List<String> p_imagenNombres = new ArrayList<>();
	}

	// legacy cl/bcs/plataforma/pla/utils/ConsultarImagen.java:36-41 (verbatim)
	public static class ConsultarImagenSalida extends SalidaEstandar {
		public List<Imagen> OUT_IMAGENES;
		public ConsultarImagenSalida() {
			this.OUT_IMAGENES = new ArrayList<>();
		}
	}

	// legacy cl/bcs/plataforma/pla/utils/ConsultarImagen.java:42-57 (verbatim)
	public static class Imagen {
		public Long IdEmpresa;
		public String IdImagen;
		public String NombreImagen;
		public String Tipo;
		public String Extension;
		public String Version;
		public String RutaPublica;
		public Imagen() {
			this.RutaPublica = "";
			this.NombreImagen = "";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

	/** FAKE of ConsultarImagen.logica: one image whose RutaPublica comes from "colaboradores.logoRutaPublica" (default none). */
	public ConsultarImagenSalida logica(Connection con, ConsultarImagenEntrada entrada) throws Exception {
		Hooks.registrar("ConsultarImagen", entrada);
		ConsultarImagenSalida out = new ConsultarImagenSalida();
		String ruta = Hooks.texto("logoRutaPublica", null);
		if (ruta != null) {
			Imagen i = new Imagen();
			i.RutaPublica = ruta;
			out.OUT_IMAGENES.add(i);
		}
		return out;
	}

}
