package cl.bcs.socionegocio.cli;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConsultarArchivosAnexos implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarArchivosAnexos.class);

	private final String SQL_SELECT_ARCHIVOS_ANEXOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultarArchivosAnexos_SelectArchivosAnexos.sql");

	public static class EntradaConsultarArchivosAnexos extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_DocumentoIdentidadCliente;
		public List<Long> p_IdsAnexos;

		public EntradaConsultarArchivosAnexos() {
			p_IdsAnexos = new ArrayList<>();
		}
	}

	public static class SalidaConsultarArchivosAnexos extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<ArchivoAnexoCodificado> p_out_Archivos;

		public SalidaConsultarArchivosAnexos() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_Archivos = new ArrayList<>();
		}
	}

	public static class ResultadoCodificarArchivos extends BcsPojo {
		public List<PlataformaUtils.MensajeSalida> mensajes;
		public List<ArchivoAnexoCodificado> archivosCodificados;

		public ResultadoCodificarArchivos() {
			mensajes = new ArrayList<>();
			archivosCodificados = new ArrayList<>();
		}
	}

	public static class ArchivoAnexo extends BcsPojo {
		public Long IdDocumento;
		public Long IdAnexo;
		public String NombreArchivo;
		public String ExtensionArchivo;
	}

	public static class ArchivoAnexoBinario extends ArchivoAnexo {
		public byte[] ContenidoArchivo;
	}

	public static class ArchivoAnexoCodificado extends ArchivoAnexo {
		public String ContenidoArchivo;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarArchivosAnexos entrada = gson.fromJson(in, EntradaConsultarArchivosAnexos.class);
		SalidaConsultarArchivosAnexos salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();

		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	private SalidaConsultarArchivosAnexos logica(Connection conn, EntradaConsultarArchivosAnexos entrada) throws Exception {
		logger.info("INICIO::ConsultarArchivosAnexos");
		long inicio = System.currentTimeMillis();

		SalidaConsultarArchivosAnexos salida = new SalidaConsultarArchivosAnexos();

		List<ArchivoAnexoBinario> archivos = obtenerArchivos(conn, entrada.p_DocumentoIdentidadCliente, entrada.p_IdsAnexos);
		ResultadoCodificarArchivos resultadoCodificarArchivos = codificarArchivos(conn, entrada.p_IdEmpresa, entrada.p_IdsAnexos, archivos);
		salida.p_out_Archivos.addAll(resultadoCodificarArchivos.archivosCodificados);
		salida.P_OUT_MENSAJES.addAll(resultadoCodificarArchivos.mensajes);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarArchivosAnexos", fin - inicio);

		return salida;
	}

	public List<ArchivoAnexoBinario> obtenerArchivos(Connection conn, String documentoIdentidadCliente, List<Long> idsAnexos) throws Exception {
		logger.debug("INICIO::ConsultarArchivosAnexos.obtenerArchivos");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(4);
		params.put("IdsAnexos", idsAnexos);
		params.put("DocumentoIdentidadCliente", documentoIdentidadCliente);

		SQL sql = new SQL();
		List<ArchivoAnexoBinario> archivos =  sql.query(conn, SQL_SELECT_ARCHIVOS_ANEXOS, params, ArchivoAnexoBinario.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarArchivosAnexos.obtenerArchivos", fin - inicio);

		return archivos;
	}

	private ResultadoCodificarArchivos codificarArchivos(Connection conn, Long idEmpresa, List<Long> idsAnexos, List<ArchivoAnexoBinario> archivos) throws Exception {
		logger.debug("INICIO::ConsultarArchivosAnexos.codificarArchivos");
		long inicio = System.currentTimeMillis();

		ResultadoCodificarArchivos resultado = new ResultadoCodificarArchivos();

		Set<Long> idsAnexosSinArchivo = new HashSet<>(idsAnexos);
		List<ArchivoAnexoCodificado> archivosCodificados = archivos
				.stream()
				.map(archivo -> {
					idsAnexosSinArchivo.remove(archivo.IdAnexo);

					ArchivoAnexoCodificado codificado = new ArchivoAnexoCodificado();
					codificado.IdDocumento = archivo.IdDocumento;
					codificado.IdAnexo = archivo.IdAnexo;
					codificado.ExtensionArchivo = archivo.ExtensionArchivo;
					codificado.NombreArchivo = archivo.NombreArchivo;
					codificado.ContenidoArchivo = Base64.encodeBase64String(archivo.ContenidoArchivo);

					return codificado;
				})
				.collect(Collectors.toList());
		resultado.archivosCodificados.addAll(archivosCodificados);

		HashMap<String, String> params = new HashMap<>(2);
		for (Long idAnexo : idsAnexosSinArchivo) {
			params.put("IdAnexo", String.valueOf(idAnexo));
			resultado.mensajes.add(new PlataformaUtils.MensajeSalida(idAnexo, PlataformaUtils.MensajeSalida.MENSAJE_INFORMACION, "UI_ARCHIVO_NO_ENCONTRADO_PARA_ANEXO", params, conn, idEmpresa));
			params.clear();
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarArchivosAnexos.codificarArchivos", fin - inicio);

		return resultado;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_DocumentoIdentidadCliente\":\"89090100-K\",\"p_IdsAnexos\":[293,410,139,2,3,4]}";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarArchivosAnexos entrada = gson.fromJson(json, EntradaConsultarArchivosAnexos.class);
			System.out.println(entrada);

			ConsultarArchivosAnexos consultarArchivosAnexos = new ConsultarArchivosAnexos();
			try {
				SalidaConsultarArchivosAnexos salida = consultarArchivosAnexos.logica(conn, entrada);
				salida.p_out_Archivos.forEach(archivo -> archivo.ContenidoArchivo = "BASE64_FILE_CONTENT");
				System.out.println(salida);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
