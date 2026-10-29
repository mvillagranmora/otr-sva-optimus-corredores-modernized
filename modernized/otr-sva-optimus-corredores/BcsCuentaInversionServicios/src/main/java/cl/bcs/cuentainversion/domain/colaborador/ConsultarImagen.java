package cl.bcs.cuentainversion.domain.colaborador;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.pla.utils.ConsultarImagen}: field names and defaults are the legacy Entrada/Salida POJOs,
 * so the modern services hand {@code Colaboradores.invocar} the same JSON the legacy call log recorded.
 */
public final class ConsultarImagen {

    private ConsultarImagen() {
    }

    public static final String CLASE = "cl.bcs.plataforma.pla.utils.ConsultarImagen";

	// legacy cl/bcs/plataforma/pla/utils/ConsultarImagen.java:31-34 (verbatim)
	public static class ConsultarImagenEntrada {
		public long p_IdEmpresa;
		public List<String> p_imagenNombres = new ArrayList<>();
	}

	public static class ConsultarImagenSalida {
		public List<Imagen> OUT_IMAGENES;
		public ConsultarImagenSalida() {
			this.OUT_IMAGENES = new ArrayList<>();
		}
	}

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
}
}
