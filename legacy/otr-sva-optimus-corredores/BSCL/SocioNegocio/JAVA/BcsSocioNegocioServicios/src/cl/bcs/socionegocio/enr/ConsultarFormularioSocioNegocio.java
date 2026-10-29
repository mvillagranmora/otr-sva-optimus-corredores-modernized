package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsultarFormularioSocioNegocio implements JavaCall {

	static final Logger logger = LogManager.getLogger(ConsultarFormularioSocioNegocio.class);

	static final String PERSONA_NATURAL = "NATURAL";
	static final String PERSONA_JURIDICA = "JURIDICA";

	static final String[] FORMULARIOS = {
			"DATOS_GENERALES_PERSONA"
			, "DOCUMENTO_IDENTIDAD"
			, "CUENTA_BANCARIA"
			, "TARJETA_BANCARIA"
			, "DIRECCION_FISICA"
			, "TELEFONO"
			, "DIRECCION_ELECTRONICA"
			, "GRUPO_DIRECCION_ELECTRONICA"
			, "GRUPO_DIRECCION_ELECTRONICA_DETALLE"
			, "INDICADORES_PERSONA"
			, "INDICADORES_CLIENTE"
			, "REQUISITOS_PERSONA"
			, "REQUISITOS_CLIENTE"
			, "RELACIONES"
			, "FATCA"
			, "FATCA_PAIS_RESIDENCIA"
			, "DATOS_GENERALES_CLIENTE"
			, "PREFERENCIAS_PAGO"
			, "CUENTA_CUSTODIO"
			, "PORTAFOLIO_CLIENTE_INDICADORES"
			, "PORTAFOLIO_CLIENTE_ANOTACIONES"
			, "DATOS_GENERALES_PORTAFOLIO_CLIENTE"
			, "DOCUMENTOS_CLIENTE_FIRMA_ANEXO"
			, "DOCUMENTOS_CLIENTE_SUBIR_ANEXO"
			, "ANOTACIONES_CLIENTE"
			, "PREFERENCIAS_NEGOCIOS_CLIENTE"
			, "DECLARACION_ORIGEN_FONDOS"
			, "PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_AUTORIZACIONES"
			, "PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_PERFIL_PRODUCTOS"
			, "PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_PERFIL_LIMITES"
			, "PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_PERFIL_COBROS"
			, "PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_MATRIZ_OPERACIONAL"
			, "PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO"
			, "HISTORIAL_CAMBIOS_CLIENTE"
			, "NOMBRES_FANTASIA_PERSONA"
			, "DATOS_GENERALES_PERSONA_DATOS_EMPLEADOR"
			, "INDICADORES_PORTAFOLIO"
	};

	public static class EntradaConsultarFormularioSocioNegocio extends BcsPojo {
		public String p_TipoEnrolamiento;
		public String p_TipoPersona;
	}

	public static class SalidaConsultarFormularioSocioNegocio extends BcsPojo {
		public List<FormularioSocioNegocio> p_FormulariosSocioNegocio;
	}

	public static class FormularioSocioNegocio extends BcsPojo {
		public String Formulario;
		public String CodigoFormulario;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarFormularioSocioNegocio jin = gson.fromJson(in, EntradaConsultarFormularioSocioNegocio.class);
		SalidaConsultarFormularioSocioNegocio out = logica(conn, jin);

		JsonParser parser = new JsonParser();

		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConsultarFormularioSocioNegocio logica(Connection conn, EntradaConsultarFormularioSocioNegocio entrada) {
		logger.info("INICIO::ConsultarFormularioSocioNegocio");
		long inicio = System.currentTimeMillis();

		SalidaConsultarFormularioSocioNegocio salida = new SalidaConsultarFormularioSocioNegocio();
		salida.p_FormulariosSocioNegocio = ObtenerFormulariosSocioNegocio(entrada.p_TipoPersona, entrada.p_TipoEnrolamiento);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarFormularioSocioNegocio", fin - inicio);

		return salida;
	}

	public static List<FormularioSocioNegocio> ObtenerFormulariosSocioNegocio(String tipoPersona, String tipoEnrolamiento) {
		logger.trace("INICIO::ValidarBorradorDocumentoIdentidad.validarDocumentosIdentidadExistentes");
		long inicio = System.currentTimeMillis();
		String tipoPersonaFormulario = "N".equals(tipoPersona) ? PERSONA_NATURAL : PERSONA_JURIDICA;
		String tipoEnrolamientoFormulario = tipoEnrolamiento.toUpperCase();
		List<FormularioSocioNegocio> formulariosSocioNegocio = new ArrayList<FormularioSocioNegocio>();
		for (String formulario : FORMULARIOS) {
			FormularioSocioNegocio formularioSocioNegocio = new FormularioSocioNegocio();
			formularioSocioNegocio.Formulario = formulario;
			formularioSocioNegocio.CodigoFormulario = formulario + "_" + tipoEnrolamientoFormulario + "_" + tipoPersonaFormulario;
			formulariosSocioNegocio.add(formularioSocioNegocio);
		}

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::ValidarBorradorDocumentoIdentidad.validarDocumentosIdentidadExistentes", fin - inicio);
		return formulariosSocioNegocio;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA ==");

		final String entradaJson = "{ \"p_TipoEnrolamiento\": \"FULL\", \"p_TipoPersona\":\"N\" }";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarFormularioSocioNegocio entrada = gson.fromJson(entradaJson, EntradaConsultarFormularioSocioNegocio.class);

			ConsultarFormularioSocioNegocio consultarFormularioSocioNegocio = new ConsultarFormularioSocioNegocio();
			SalidaConsultarFormularioSocioNegocio salida = consultarFormularioSocioNegocio.logica(conn, entrada);

			System.out.println(salida);
			System.out.println("== fin PRUEBA ==");

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
