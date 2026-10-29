package cl.bcs.socionegocio.cli.ws;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.SalidaModificarIndicador;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarIndicadoresCliente implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarIndicadoresCliente.class);
	
	private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/ws/sql/ModificarIndicadoresCliente_SelectCliente.sql");
	private final String SQL_SELECT_INDICADOR_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/ws/sql/ModificarIndicadoresCliente_SelectIndicadorCliente.sql");
	
	public static class EntradaModificarIndicadoresCliente extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdInterno;
		public String p_IdTipoDocumentoIdentidad;
		public String p_DocumentoIdentidad;
		public String p_CodigoIndicador;
		public String p_ValorIndicador;
	}
	
	public static class SalidaModificarIndicadoresCliente extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		Resultado P_OUT_RESULTADO;
		public SalidaModificarIndicadoresCliente() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Resultado extends BcsPojo{
		public Long IdInterno;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String CodigoIndicador;
		public String ValorIndicador;
	}
	
	public static class IndicadorCliente extends BcsPojo{
		public Long IdCliente;
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public String ValorIndicador;
	}
	
	public static class Cliente extends BcsPojo{
		public Long IdCliente;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarIndicadoresCliente jin = gson.fromJson(in.toString(), EntradaModificarIndicadoresCliente.class);
		SalidaModificarIndicadoresCliente out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarIndicadoresCliente logica(Connection con, EntradaModificarIndicadoresCliente entrada) throws Exception {
		logger.info("INICIO::ModificarIndicadoresCliente");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarIndicadoresCliente out = new SalidaModificarIndicadoresCliente();
		Cliente cliente = obtenerCliente(con, entrada.p_IdTipoDocumentoIdentidad, entrada.p_DocumentoIdentidad);
		if(cliente == null) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			//Se usa metodo sin ir a buscar la traduccion a base de datos, dado que es una migracion de WS (no se pueden cambiar el codigo del mensaje) y la traduccion de ese codigo no coincide con lo de la BD y no se puede cambiar la traduccion.
			out.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_EXISTE","Cliente no encontrado"));
			return out;
		}
		IndicadorCliente indicadorCliente = obtenerIndicadorCliente(con, cliente.IdCliente, entrada.p_CodigoIndicador);
		if(indicadorCliente == null) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			//Se usa metodo sin ir a buscar la traduccion a base de datos, dado que es una migracion de WS (no se pueden cambiar el codigo del mensaje) y la traduccion de ese codigo no coincide con lo de la BD y no se puede cambiar la traduccion.
			out.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTES_ERROR_INTERNO","Ha ocurrido un error interno.No hay coincidencias para CodigoIndicador ingresado"));
			return out;
		}
		
		SalidaModificarIndicador salidaModificarIndicador = modificarIndicador(con, entrada, indicadorCliente);
		out.P_OUT_ERRORESCONTADOR = salidaModificarIndicador.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarIndicador.P_OUT_MENSAJES);
		out.P_OUT_RESULTADO = obtenerResultado(con, entrada, cliente.IdCliente);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarSolicitudAporteRescateUI", finServicio - inicioServicio);
		return out;
	}
	
	public Cliente obtenerCliente(Connection con, String idTipoDocumentoIdentidad, String documentoIdentidad) throws Exception {
		logger.debug("INICIO::ModificarIndicadoresCliente.obtenerCliente");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<String, Object>(2);
		parametrosConsulta.put("IdTipoDocumentoIdentidad", idTipoDocumentoIdentidad);
		parametrosConsulta.put("DocumentoIdentidad", documentoIdentidad);
		List<Cliente> respuestaQuery = sql.query(con, SQL_SELECT_CLIENTE, parametrosConsulta, Cliente.class);
		Cliente cliente = null;
		if(respuestaQuery.size() > 0) {
			cliente = respuestaQuery.get(0);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicadoresCliente.obtenerCliente", fin - inicio);
		return cliente;
	}
	
	public IndicadorCliente obtenerIndicadorCliente(Connection con, Long idCliente, String codigoIndicador) throws Exception {
		logger.debug("INICIO::ModificarIndicadoresCliente.obtenerIndicadorCliente");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<String, Object>(2);
		parametrosConsulta.put("IdCliente", idCliente);
		parametrosConsulta.put("CodigoTipoIndicador", codigoIndicador);
		List<IndicadorCliente> respuestaQuery = sql.query(con, SQL_SELECT_INDICADOR_CLIENTE, parametrosConsulta, IndicadorCliente.class);
		IndicadorCliente indicadorCliente = null;
		if(respuestaQuery.size() > 0) {
			indicadorCliente = respuestaQuery.get(0);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicadoresCliente.obtenerIndicadorCliente", fin - inicio);
		return indicadorCliente;
	}
	
	public SalidaModificarIndicador modificarIndicador(Connection con,  EntradaModificarIndicadoresCliente entrada, IndicadorCliente indicadorCliente) throws Exception {
		logger.debug("INICIO::ModificarIndicadoresCliente.modificarIndicador");
		long inicio = System.currentTimeMillis();
		ModificarIndicador modificarIndicador = new ModificarIndicador();
		EntradaModificarIndicador entradaModificarIndicador = new EntradaModificarIndicador();
		entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarIndicador.p_IdModulo = entrada.p_IdModulo;
		entradaModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaModificarIndicador.p_IdTipoEntidad = "CLIENTE";
		entradaModificarIndicador.p_IdEntidad = indicadorCliente.IdCliente;
		ModificarIndicador.RegistroAtributo registroAtributo = new ModificarIndicador.RegistroAtributo();
		registroAtributo.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
		registroAtributo.IdIndicador = indicadorCliente.IdIndicador;
		registroAtributo.IdTipoIndicador = indicadorCliente.IdTipoIndicador;
		registroAtributo.ValorIndicador = entrada.p_ValorIndicador;
		entradaModificarIndicador.p_RegistroAtributo.add(registroAtributo);
		
		SalidaModificarIndicador salidaModificarIndicador = modificarIndicador.logica(con, entradaModificarIndicador);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicadoresCliente.modificarIndicador", fin - inicio);
		return salidaModificarIndicador;
	}
	
	public Resultado obtenerResultado(Connection con, EntradaModificarIndicadoresCliente entrada, Long idCliente) throws Exception {
		logger.debug("INICIO::ModificarIndicadoresCliente.obtenerResultado");
		long inicio = System.currentTimeMillis();
		Resultado resultado = new Resultado();
		IndicadorCliente indicadorCliente = obtenerIndicadorCliente(con, idCliente, entrada.p_CodigoIndicador);
		resultado.IdInterno = entrada.p_IdInterno;
		resultado.DocumentoIdentidad = entrada.p_DocumentoIdentidad;
		resultado.TipoDocumentoIdentidad = entrada.p_IdTipoDocumentoIdentidad;
		resultado.CodigoIndicador = entrada.p_CodigoIndicador;
		resultado.ValorIndicador = indicadorCliente == null ? "" : indicadorCliente.ValorIndicador;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicadoresCliente.obtenerResultado", fin - inicio);
		return resultado;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String desaJson = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdInterno\":0,\"p_IdTipoDocumentoIdentidad\":\"R\",\"p_DocumentoIdentidad\":\"1689504-4\",\"p_CodigoIndicador\":\"BCCUM\",\"p_ValorIndicador\":\"S\"}";

		
		EntradaModificarIndicadoresCliente Objin = gson.fromJson(desaJson, EntradaModificarIndicadoresCliente.class);
		ModificarIndicadoresCliente  demo=new ModificarIndicadoresCliente();				
		try {
			SalidaModificarIndicadoresCliente ObjOut=demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.commit();
			}else {
				con.rollback();
			}
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
