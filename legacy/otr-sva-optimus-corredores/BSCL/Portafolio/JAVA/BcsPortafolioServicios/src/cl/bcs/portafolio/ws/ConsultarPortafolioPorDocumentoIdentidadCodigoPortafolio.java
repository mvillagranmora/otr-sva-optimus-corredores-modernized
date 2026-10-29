package cl.bcs.portafolio.ws;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.class);
	
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/ws/sql/ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio_SelectPortafolio.sql");
	
	public static class EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<EntradaDocumentoIdentidadCodigoPortafolio> p_DocumentosIdentidadCodigosPortafolio;
		
		public EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio() {
			this.p_DocumentosIdentidadCodigosPortafolio = new ArrayList<EntradaDocumentoIdentidadCodigoPortafolio>();
		}
	}
	
	public static class EntradaDocumentoIdentidadCodigoPortafolio extends BcsPojo {
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
	}
	
	public static class SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Portafolio> P_OUT_PORTAFOLIOS;
		
		public SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_PORTAFOLIOS = new ArrayList<Portafolio>();
		}
	}
	
	public static class Portafolio extends BcsPojo{
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdEstadoPortafolio;
		public String IdEstadoCliente;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio jin = gson.fromJson(in.toString(), EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.class);
		SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio logica(Connection con, EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio entrada) throws Exception {
		logger.info("INICIO::ConsultarClientePorDocumentoIdentidad");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio out = new SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio();
		out.P_OUT_PORTAFOLIOS = obtenerPortafolios(con, entrada.p_DocumentosIdentidadCodigosPortafolio);
		out.P_OUT_MENSAJES = validarPortafolio(con, entrada, out.P_OUT_PORTAFOLIOS);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarClientePorDocumentoIdentidad", finServicio - inicioServicio);
		return out;
	}
	
	private List<Portafolio> obtenerPortafolios(Connection con, List<EntradaDocumentoIdentidadCodigoPortafolio> entradaDocumentosIdentidadCodigosPortafolio) throws Exception {
		logger.debug("INICIO::ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.obtenerPortafolios");
		long inicio = System.currentTimeMillis();
		
		List<String> documentosIdentidad = entradaDocumentosIdentidadCodigosPortafolio
				.stream()
				.map(entradaDocumentoIdentidadCodigoPortafolio->entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad)
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("DocumentoIdentidad", documentosIdentidad);
		
		SQL sql = new SQL();
		List<Portafolio> portafoliosPorDocumentoIdentidad = sql.query(con, SQL_SELECT_PORTAFOLIO, parametros, Portafolio.class);
		
		Map<String, Portafolio> portafoliosPorDocumentoIdentidadCodigoPortafolio = portafoliosPorDocumentoIdentidad
				.stream()
				.collect(Collectors.toMap(portafolio->portafolio.DocumentoIdentidad + "_" + portafolio.CodigoPortafolio, portafolio->portafolio));
		
		List<Portafolio> portafoliosEncontrados = new ArrayList<Portafolio>();
		HashMap<String,Boolean> portafoliosIngresados = new HashMap<String,Boolean>(portafoliosPorDocumentoIdentidad.size());
		for(EntradaDocumentoIdentidadCodigoPortafolio entradaDocumentoIdentidadCodigoPortafolio : entradaDocumentosIdentidadCodigosPortafolio) {
			Portafolio portafolio = portafoliosPorDocumentoIdentidadCodigoPortafolio.get(entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad + "_" + entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio);
			if(portafolio != null && portafoliosIngresados.get(entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad + "_" + entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio) == null) {
				portafoliosEncontrados.add(portafolio);
				portafoliosIngresados.put(entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad + "_" + entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio, true);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.obtenerPortafolios", fin - inicio);
		return portafoliosEncontrados;
		
	}
	
	private List<MensajeSalida> validarPortafolio(Connection con, EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio entrada, List<Portafolio> portafolios) throws Exception{
		logger.trace("INICIO::ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.validarPortafolio");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajesSalida = new ArrayList<MensajeSalida>();
		
		Map<String, Portafolio> portafoliosPorDocumentoIdentidadCodigoPortafolio = portafolios
				.stream()
				.collect(Collectors.toMap(portafolio->portafolio.DocumentoIdentidad + "_" + portafolio.CodigoPortafolio, portafolio->portafolio));
		
		for(EntradaDocumentoIdentidadCodigoPortafolio entradaDocumentoIdentidadCodigoPortafolio : entrada.p_DocumentosIdentidadCodigosPortafolio) {
			Portafolio portafolio = portafoliosPorDocumentoIdentidadCodigoPortafolio.get(entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad + "_" + entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio);
			if(portafolio == null) {
				HashMap<String, String> parametros = new HashMap<String, String>(3);
				parametros.put("DocumentoIdentidad", entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad);
				parametros.put("CodigoPortafolio", entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio);
				MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CONSULTARPORTAFOLIOPORDOCUMENTOIDENTIDADCODIGOPORTAFOLIO_PORTAFOLIO_NO_EXISTE", parametros, con, entrada.p_IdEmpresa);
				mensajesSalida.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.validarPortafolio", fin - inicio);
		return mensajesSalida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio = new EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio();
			entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdEmpresa = 1L;
			entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdUsuario = 21L;
			entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdModulo = "SOC";
			entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_UsaReglasNegocio = "S";
			
			EntradaDocumentoIdentidadCodigoPortafolio entradaDocumentoIdentidadCodigoPortafolio = new EntradaDocumentoIdentidadCodigoPortafolio();
			entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad = "16338637-2";
			entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio = "0";

			entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_DocumentosIdentidadCodigosPortafolio = Arrays.asList(entradaDocumentoIdentidadCodigoPortafolio);
			ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio consultarPortafolioPorDocumentoIdentidadCodigoPortafolio = new ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio();
			try {
				SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio = consultarPortafolioPorDocumentoIdentidadCodigoPortafolio.logica(con, entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio);
				System.out.println(salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} finally {
				con.rollback();
			}

    	}
		System.out.println("== END ==");
	}

}
