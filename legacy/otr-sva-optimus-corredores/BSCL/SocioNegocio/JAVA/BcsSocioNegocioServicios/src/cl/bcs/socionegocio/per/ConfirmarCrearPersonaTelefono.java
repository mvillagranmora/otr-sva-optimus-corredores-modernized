package cl.bcs.socionegocio.per;

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

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.socionegocio.per.AutorizarCrearPersonaTelefonoMasivo.TelefonoPorAutorizar;
import cl.bcs.socionegocio.per.AutorizarCrearPersonaTelefonoMasivo.TelefonoUpdate;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarCrearPersonaTelefono implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPersonaTelefono.class);

	private final String SQL_UPDATE_TELEFONO_ESTADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaTelefonoMasivo_UpdateTelefonoEstado.sql");
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaTelefonoMasivo_InsertLogGenerarContratos.sql");
	private final String SQL_SELECT_TELEFONO_LOG = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaTelefono_SelectTelefonoLog.sql");

	private static final String ID_TIPO_ENTIDAD = "TELEFONO";
	private static final String NOMBRE_PROCESO = "Confirmar Ingreso Telefono";

	public static class EntradaConfirmarCrearPersonaTelefono{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdTelefono;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarCrearPersonaTelefono extends SalidaEstandar{
		public SalidaConfirmarCrearPersonaTelefono(){
			super();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public static class PersonaTelefonoLog{
		public Long IdPersona;
		public String Pais;
		public String NumeroTelefono;
		public String TipoTelefono;
		public String Anexo;
		public String Proposito;
		public String Comentario;
		public String Estado;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearPersonaTelefono in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPersonaTelefono.class);
		SalidaConfirmarCrearPersonaTelefono out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarCrearPersonaTelefono logica(Connection conn, EntradaConfirmarCrearPersonaTelefono in) throws Exception{
		logger.info("INICIO::ConfirmarCrearPersonaTelefono");
		SalidaConfirmarCrearPersonaTelefono salida = new SalidaConfirmarCrearPersonaTelefono();
		SQL sql = new SQL();

		TelefonoUpdate update = new TelefonoUpdate();
		update.p_IdTelefono = in.p_IdTelefono;
		update.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
		sql.batch(conn, SQL_UPDATE_TELEFONO_ESTADO, Arrays.asList(update), TelefonoUpdate.class);

		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

		TelefonoPorAutorizar logGenerarContratos = new TelefonoPorAutorizar(in.p_IdTelefono);
		logGenerarContratos.p_IdEmpresa = in.p_IdEmpresa;
		logGenerarContratos.p_IdUsuario = in.p_IdUsuario;
		logGenerarContratos.p_FechaSistema = fechaSistema;
		sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, Arrays.asList(logGenerarContratos), TelefonoPorAutorizar.class);

		registrarLog(conn, sql, in, fechaSistema);

		logger.info("FIN::ConfirmarCrearPersonaTelefono");
		return salida;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarCrearPersonaTelefono in, String fechaSistema) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaTelefono.registrarLog");

		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdTelefono", in.p_IdTelefono);
		List<PersonaTelefonoLog> resultado = sql.query(conn, SQL_SELECT_TELEFONO_LOG, params, PersonaTelefonoLog.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarCrearPersonaTelefono.registrarLog - sin datos de telefono/persona");
			return;
		}
		PersonaTelefonoLog datos = resultado.get(0);

		InAgregarLog entrada = new InAgregarLog();

		InAgregarLog.Log log = new InAgregarLog.Log();
		log.IdCabeceraLog = datos.IdPersona;
		log.IdTipoLog = "INGRESO";
		log.IdModulo = "SOC";
		log.IdTipoRegistro = ID_TIPO_ENTIDAD;
		log.IdUsuario = in.p_IdUsuario;
		log.IdEmpresa = in.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = in.p_IdUsuario;
		log.IdPersona = datos.IdPersona;
		log.IdRegistro = String.valueOf(in.p_IdTelefono);
		log.NombreProceso = NOMBRE_PROCESO;
		log.EstadoProceso = in.p_IdEstadoAutorizacion;
		entrada.p_Log.add(log);

		entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, "NumeroTelefono", "", datos.NumeroTelefono, ID_TIPO_ENTIDAD);
		entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, "Pais", "", datos.Pais, ID_TIPO_ENTIDAD);
		entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, "TipoTelefono", "", datos.TipoTelefono, ID_TIPO_ENTIDAD);
		entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, "Anexo", "", datos.Anexo, ID_TIPO_ENTIDAD);
		entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, "Proposito", "", datos.Proposito, ID_TIPO_ENTIDAD);
		entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, "Comentario", "", datos.Comentario, ID_TIPO_ENTIDAD);
		entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, "Estado", "", datos.Estado, ID_TIPO_ENTIDAD);

		new AgregarLog().logica(conn, entrada);

		logger.debug("FIN::ConfirmarCrearPersonaTelefono.registrarLog");
	}

}
