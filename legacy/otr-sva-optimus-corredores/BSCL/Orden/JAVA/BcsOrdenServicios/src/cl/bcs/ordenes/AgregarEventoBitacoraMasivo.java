package cl.bcs.ordenes;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AgregarEventoBitacoraMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(AgregarEventoBitacoraMasivo.class);

	private final String SQL_INSERT_EVENTO_BITACORA = loadResourceToString("cl/bcs/ordenes/sql/AgregarEventoBitacoraMasivo_InsertEventoBitacora.sql");

	public static class EntradaAgregarEventoBitacoraMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdTipoCanalIngreso;
		public String p_UsaReglasNegocio;
		public List<EventoBitacora> p_eventos;

		public EntradaAgregarEventoBitacoraMasivo() {
			p_eventos = new ArrayList<>();
		}
	}

	public static class SalidaAgregarEventoBitacoraMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<Identificador> p_out_Identificadores;

		public SalidaAgregarEventoBitacoraMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			p_out_Identificadores = new ArrayList<>();
		}
	}

	public static class EventoBitacora extends BcsPojo {
		public Long IdInterno;
		public Long IdEntidad;
		public Long IdEventoBitacora;
		public String NombreEntidad;
		public Long IdOrden;
		public Long IdAsignacion;
		public Long IdTransaccion;
		public String MensajeEventoBitacora;
		public java.sql.Timestamp FechaEventoBitacora;
		public String IdTipoEventoBitacora;
	}

	public static class Identificador extends BcsPojo {
		public Long IdInterno;
		public Long IdEventoBitacora;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAgregarEventoBitacoraMasivo entrada = gson.fromJson(in, EntradaAgregarEventoBitacoraMasivo.class);
		SalidaAgregarEventoBitacoraMasivo salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	public SalidaAgregarEventoBitacoraMasivo logica(Connection conn, EntradaAgregarEventoBitacoraMasivo entrada) throws Exception {
		logger.info("INICIO::AgregarEventoBitacoraMasivo");
		long inicio = System.currentTimeMillis();

		SalidaAgregarEventoBitacoraMasivo salida = new SalidaAgregarEventoBitacoraMasivo();

		List<EventoBitacora> eventos = obtenerEventosBitacorasInsertables(conn, entrada.p_IdEmpresa, entrada.p_eventos);
		insertarEventosBitacoras(conn, eventos);
		salida.p_out_Identificadores = obtenerIdentificadoresEventos(eventos);

		long fin = System.currentTimeMillis();
		logger.info("FIN::AgregarEventoBitacoraMasivo", fin - inicio);

		return salida;
	}

	private List<EventoBitacora> obtenerEventosBitacorasInsertables(Connection conn, Long idEmpresa, List<EventoBitacora> eventos) throws Exception {
		logger.debug("INICIO::AgregarEventoBitacoraMasivo.obtenerEventosBitacorasInsertables");
		long inicio = System.currentTimeMillis();

		java.sql.Timestamp fechaSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa);
		Iterator<Long> idsEventos = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::EventoBitacora", (long) eventos.size()).iterator();

		for (EventoBitacora evento : eventos) {
			evento.IdEventoBitacora = idsEventos.next();
			evento.FechaEventoBitacora = fechaSistema;
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AgregarEventoBitacoraMasivo.obtenerEventosBitacorasInsertables", fin - inicio);

		return eventos;
	}

	private void insertarEventosBitacoras(Connection conn, List<EventoBitacora> eventos) throws SQLException {
		logger.debug("INICIO::AgregarEventoBitacoraMasivo.insertarEventosBitacoras");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		sql.batch(conn, SQL_INSERT_EVENTO_BITACORA, eventos, EventoBitacora.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AgregarEventoBitacoraMasivo.insertarEventosBitacoras", fin - inicio);
	}

	private List<Identificador> obtenerIdentificadoresEventos(List<EventoBitacora> eventos) {
		logger.trace("INICIO::AgregarEventoBitacoraMasivo.obtenerIdentificadoresEventos");
		long inicio = System.currentTimeMillis();

		List<Identificador> identificadores = eventos.stream().map(evento -> {
			Identificador identificador = new Identificador();
			identificador.IdEventoBitacora = evento.IdEventoBitacora;
			identificador.IdInterno = evento.IdInterno;

			return identificador;
		}).collect(Collectors.toList());

		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarEventoBitacoraMasivo.obtenerIdentificadoresEventos", fin - inicio);

		return identificadores;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_UsaReglasNegocio\":\"N\",\"p_eventos\":[{\"IdInterno\":1,\"NombreEntidad\":\"INSTRUCCION\",\"IdEntidad\":1,\"IdOrden\":1,\"IdAsignacion\":1,\"IdTransaccion\":1,\"MensajeEventoBitacora\":\"JASDJ\",\"IdTipoEventoBitacora\":\"CONFIRMACION_INSTRUCCION\"},{\"IdInterno\":2,\"NombreEntidad\":\"INSTRUCCION\",\"IdEntidad\":2,\"IdOrden\":2,\"IdAsignacion\":2,\"IdTransaccion\":2,\"MensajeEventoBitacora\":\"JASDJ\",\"IdTipoEventoBitacora\":\"CONFIRMACION_INSTRUCCION\"},{\"IdInterno\":3,\"NombreEntidad\":\"INSTRUCCION\",\"IdEntidad\":3,\"IdOrden\":3,\"IdAsignacion\":3,\"IdTransaccion\":3,\"MensajeEventoBitacora\":\"JASDJ\",\"IdTipoEventoBitacora\":\"CONFIRMACION_INSTRUCCION\"}]}";

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaAgregarEventoBitacoraMasivo entrada = gson.fromJson(jsonRequest, EntradaAgregarEventoBitacoraMasivo.class);
			System.out.println("Entrada");
			System.out.println(entrada);

			try {
				AgregarEventoBitacoraMasivo agregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo();
				SalidaAgregarEventoBitacoraMasivo salidaAgregarEventoBitacoraMasivo = agregarEventoBitacoraMasivo.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaAgregarEventoBitacoraMasivo);

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
