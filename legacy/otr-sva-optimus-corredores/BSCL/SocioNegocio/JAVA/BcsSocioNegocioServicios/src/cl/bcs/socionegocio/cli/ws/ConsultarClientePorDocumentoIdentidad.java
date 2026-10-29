package cl.bcs.socionegocio.cli.ws;

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

public class ConsultarClientePorDocumentoIdentidad implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarClientePorDocumentoIdentidad.class);
	
	private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/ws/sql/ConsultarClientePorDocumentoIdentidad_SelectCliente.sql");
	
	public static class EntradaConsultarClientePorDocumentoIdentidad extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<EntradaDocumentoIdentidad> p_DocumentosIdentidad;
		
		public EntradaConsultarClientePorDocumentoIdentidad() {
			this.p_DocumentosIdentidad = new ArrayList<EntradaDocumentoIdentidad>();
		}
	}
	
	public static class EntradaDocumentoIdentidad extends BcsPojo {
		public String DocumentoIdentidad;
	}
	
	public static class SalidaConsultarClientePorDocumentoIdentidad extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Cliente> P_OUT_CLIENTES;
		
		public SalidaConsultarClientePorDocumentoIdentidad() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CLIENTES = new ArrayList<Cliente>();
		}
	}
	
	public static class Cliente extends BcsPojo {
		public Long IdCliente;
		public String DocumentoIdentidad;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarClientePorDocumentoIdentidad jin = gson.fromJson(in.toString(), EntradaConsultarClientePorDocumentoIdentidad.class);
		SalidaConsultarClientePorDocumentoIdentidad out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarClientePorDocumentoIdentidad logica(Connection con, EntradaConsultarClientePorDocumentoIdentidad entrada) throws Exception {
		logger.info("INICIO::ConsultarClientePorDocumentoIdentidad");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarClientePorDocumentoIdentidad out = new SalidaConsultarClientePorDocumentoIdentidad();
		
		out.P_OUT_CLIENTES = obtenerClientes(con, entrada.p_DocumentosIdentidad);
		out.P_OUT_MENSAJES = validarClientes(con, entrada, out.P_OUT_CLIENTES);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarClientePorDocumentoIdentidad", finServicio - inicioServicio);
		return out;
	}
	
	private List<Cliente> obtenerClientes(Connection con, List<EntradaDocumentoIdentidad> entradaDocumentosIdentidad) throws Exception {
		logger.debug("INICIO::ConsultarClientePorDocumentoIdentidad.obtenerClientes");
		long inicio = System.currentTimeMillis();
		
		List<String> documentosIdentidad = entradaDocumentosIdentidad
				.stream()
				.map(entradaDocumentoIdentidad->entradaDocumentoIdentidad.DocumentoIdentidad)
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("DocumentoIdentidad", documentosIdentidad);
		
		SQL sql = new SQL();
		List<Cliente> clientes = sql.query(con, SQL_SELECT_CLIENTE, parametros, Cliente.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarClientePorDocumentoIdentidad.obtenerClientes", fin - inicio);
		return clientes;
	}
	
	private List<MensajeSalida> validarClientes(Connection con, EntradaConsultarClientePorDocumentoIdentidad entrada, List<Cliente> clientes) throws Exception{
		logger.debug("INICIO::ConsultarClientePorDocumentoIdentidad.obtenerClientes");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajesSalida = new ArrayList<MensajeSalida>();
		
		Map<String, Cliente> clientesPorIdCliente = clientes
				.stream()
				.collect(Collectors.toMap(cliente->cliente.DocumentoIdentidad, cliente->cliente));
		
		for(EntradaDocumentoIdentidad entradaDocumentoIdentidad : entrada.p_DocumentosIdentidad) {
			Cliente cliente = clientesPorIdCliente.get(entradaDocumentoIdentidad.DocumentoIdentidad);
			if(cliente == null) {
				HashMap<String, String> parametros = new HashMap<String, String>(2);
				parametros.put("DocumentoIdentidad", entradaDocumentoIdentidad.DocumentoIdentidad);
				MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CONSULTARCLIENTEPORDOCUMENTOIDENTIDAD_CLIENTE_NO_EXISTE", parametros, con, entrada.p_IdEmpresa);
				mensajesSalida.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarClientePorDocumentoIdentidad.obtenerClientes", fin - inicio);
		return mensajesSalida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaConsultarClientePorDocumentoIdentidad entradaConsultarClientePorDocumentoIdentidad = new EntradaConsultarClientePorDocumentoIdentidad();
			entradaConsultarClientePorDocumentoIdentidad.p_IdEmpresa = 1L;
			entradaConsultarClientePorDocumentoIdentidad.p_IdUsuario = 21L;
			entradaConsultarClientePorDocumentoIdentidad.p_IdModulo = "SOC";
			entradaConsultarClientePorDocumentoIdentidad.p_UsaReglasNegocio = "S";
			
			EntradaDocumentoIdentidad entradaDocumentoIdentidad = new EntradaDocumentoIdentidad();
			entradaDocumentoIdentidad.DocumentoIdentidad = "16338637-2";

			entradaConsultarClientePorDocumentoIdentidad.p_DocumentosIdentidad = Arrays.asList(entradaDocumentoIdentidad);
			ConsultarClientePorDocumentoIdentidad consultarClientePorDocumentoIdentidad = new ConsultarClientePorDocumentoIdentidad();
			try {
				SalidaConsultarClientePorDocumentoIdentidad salidaConsultarClientePorDocumentoIdentidad = consultarClientePorDocumentoIdentidad.logica(con, entradaConsultarClientePorDocumentoIdentidad);
				System.out.println(salidaConsultarClientePorDocumentoIdentidad.toString());
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
