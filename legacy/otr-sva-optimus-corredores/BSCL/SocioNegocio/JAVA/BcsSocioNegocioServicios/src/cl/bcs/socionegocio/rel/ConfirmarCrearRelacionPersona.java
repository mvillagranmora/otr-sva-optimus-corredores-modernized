package cl.bcs.socionegocio.rel;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.SalidaModificarIndicador;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarCrearRelacionPersona implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConfirmarCrearRelacionPersona.class);
	
	private final String SQL_UPDATE_ESTADO_AUTORIZACION_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/rel/sql/ConfirmarCrearRelacionPersona_UpdateEstadoAutorizacionRelacionPersona.sql");
	private final String SQL_SELECT_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/rel/sql/ConfirmarCrearRelacionPersona_SelectRelacionPersona.sql");
	private final String SQL_SELECT_INDICADOR_PERSONA_EXPUESTA_POLITICAMENTE = loadResourceToString("cl/bcs/socionegocio/rel/sql/ConfirmarCrearRelacionPersona_SelectExpuestaPoliticamente.sql");
	
	public static class EntradaConfirmarCrearRelacionPersona extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdRelacionPersona;
		public String p_IdEstadoAutorizacion;
	}
	
	public static class SalidaConfirmarCrearRelacionPersona extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConfirmarCrearRelacionPersona() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class RelacionPersona extends BcsPojo{
		public Long IdRelacionPersona;
		public String IdEstadoAutorizacion;
		public Long IdPersonaOrigen;
		public Long IdPersonaDestino;
	}
	
	public static class Indicador extends BcsPojo{
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public String ValorIndicador;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarCrearRelacionPersona jin = gson.fromJson(in.toString(), EntradaConfirmarCrearRelacionPersona.class);
		SalidaConfirmarCrearRelacionPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarCrearRelacionPersona logica(Connection con, EntradaConfirmarCrearRelacionPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarCrearRelacionPersona");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarCrearRelacionPersona out = new SalidaConfirmarCrearRelacionPersona();
		
		actualizarEstadoAutorizacionRelacionPersona(con, entrada.p_IdRelacionPersona, entrada.p_IdEstadoAutorizacion);
		
		SalidaModificarIndicador salidaModificarIndicador = modificarIndicadorPersonaExpuestaPoliticamente(con, entrada);
		out.P_OUT_ERRORESCONTADOR += salidaModificarIndicador.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarIndicador.P_OUT_MENSAJES);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarCrearRelacionPersona", finServicio - inicioServicio);
		return out;
	}
	
	private void actualizarEstadoAutorizacionRelacionPersona(Connection con, Long idRelacionPersona, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ConfirmarCrearRelacionPersona.actualizarEstadoAutorizacionRelacionPersona");
		long inicio = System.currentTimeMillis();
		RelacionPersona relacionPersona = new RelacionPersona();
		relacionPersona.IdRelacionPersona = idRelacionPersona;
		relacionPersona.IdEstadoAutorizacion = idEstadoAutorizacion;
		List<RelacionPersona> relacionesPersona = Arrays.asList(relacionPersona);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_ESTADO_AUTORIZACION_RELACION_PERSONA, relacionesPersona, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearRelacionPerson.actualizarEstadoAutorizacionRelacionPersona", fin - inicio);
	}
	
	private SalidaModificarIndicador modificarIndicadorPersonaExpuestaPoliticamente(Connection con, EntradaConfirmarCrearRelacionPersona entrada) throws Exception {
		logger.debug("INICIO::ConfirmarCrearRelacionPersona.modificarIndicadorPersonaExpuestaPoliticamente");
		long inicio = System.currentTimeMillis();
		SalidaModificarIndicador salidaModificarIndicador = new SalidaModificarIndicador();
		RelacionPersona relacionPersona = obtenerInformacionRelacionPersona(con, entrada.p_IdRelacionPersona);
		
		if(relacionPersona != null) {
			Indicador indicadorPersonaDestino = obtenerInformacionIndicadorPersonaExpuestaPoliticamnente(con, relacionPersona.IdPersonaDestino);
			
			if(indicadorPersonaDestino != null && "S".equals(indicadorPersonaDestino.ValorIndicador)) {
				Indicador indicadorPersonaOrigen = obtenerInformacionIndicadorPersonaExpuestaPoliticamnente(con, relacionPersona.IdPersonaOrigen);
				
				ModificarIndicador modificarIndicador = new ModificarIndicador();
				EntradaModificarIndicador entradaModificarIndicador = new EntradaModificarIndicador();
				entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
				entradaModificarIndicador.p_IdModulo = entrada.p_IdModulo;
				entradaModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				entradaModificarIndicador.p_IdTipoEntidad = "PERSONA";
				entradaModificarIndicador.p_IdEntidad = relacionPersona.IdPersonaOrigen;
				ModificarIndicador.RegistroAtributo registroAtributo = new ModificarIndicador.RegistroAtributo();
				registroAtributo.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
				registroAtributo.IdIndicador = indicadorPersonaOrigen.IdIndicador;
				registroAtributo.IdTipoIndicador = indicadorPersonaOrigen.IdTipoIndicador;
				registroAtributo.ValorIndicador = "S";
				entradaModificarIndicador.p_RegistroAtributo.add(registroAtributo);
				
				salidaModificarIndicador = modificarIndicador.logica(con, entradaModificarIndicador);
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearRelacionPerson.modificarIndicadorPersonaExpuestaPoliticamente", fin - inicio);
		return salidaModificarIndicador;
	}
	
	private RelacionPersona obtenerInformacionRelacionPersona(Connection con, Long idRelacionPersona) throws Exception {
		logger.debug("INICIO::ConfirmarCrearRelacionPersona.obtenerInformacionRelacionPersona");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdRelacionPersona", idRelacionPersona);
		
		SQL sql = new SQL();
		List<RelacionPersona> relacionesPersona = sql.query(con, SQL_SELECT_RELACION_PERSONA, parametros, RelacionPersona.class);
		RelacionPersona relacionPersona = relacionesPersona.parallelStream().findFirst().orElse(null);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearRelacionPerson.obtenerInformacionRelacionPersona", fin - inicio);
		return relacionPersona;
	}
	
	private Indicador obtenerInformacionIndicadorPersonaExpuestaPoliticamnente(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::ConfirmarCrearRelacionPersona.obtenerInformacionIndicadorPersonaExpuestaPoliticamnente");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersona", idPersona);
		
		SQL sql = new SQL();
		List<Indicador> indicadores = sql.query(con, SQL_SELECT_INDICADOR_PERSONA_EXPUESTA_POLITICAMENTE, parametros, Indicador.class);
		Indicador indicador = indicadores.parallelStream().findFirst().orElse(null);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearRelacionPerson.obtenerInformacionIndicadorPersonaExpuestaPoliticamnente", fin - inicio);
		return indicador;
	}

}
