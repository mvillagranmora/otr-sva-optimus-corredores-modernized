package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.pojo.NotificacionActualizacionDatos;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.AgregarNotificacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarCaducidadDatos implements JavaCall {

	private final String SELECT_CLIENTES = loadResourceToString("cl/bcs/socionegocio/cli/sql/NotificarCaducidadDatos_Select.sql");
	private final String SELECT_CORREDORA = loadResourceToString("cl/bcs/socionegocio/cli/sql/NotificarCaducidadDatos_SelectCorredora.sql");

	public static class SalidaNotificarCaducidad extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;

		public SalidaNotificarCaducidad() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}

	public static class Notificacion extends BcsPojo {
		public Long IdUsuario;
		public String RazonCorredora;
		public String TelefonoCorredora;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();// new Gson();
		SalidaNotificarCaducidad out = logica(conn);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaNotificarCaducidad logica(Connection con) throws Exception {

		SalidaNotificarCaducidad out = new SalidaNotificarCaducidad();

		HashMap<String, Object> params = new HashMap<String, Object>();

		SQL sql = new SQL();

		Long IdEmpresa = 1L;

		String fechaSistema = null;
		String diasCaducidad = null;
		try {
			fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA",
					"PLA.FechaSistema");
			diasCaducidad = ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA",
					"PLA.DiasCaducidad");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String fechaConsulta = fechaAviso(fechaSistema, Long.parseLong(diasCaducidad));
		//String fechaConsulta = fechaAviso("2020-06-18", Long.parseLong(diasCaducidad));
		params.put("fechaConsulta", fechaConsulta);
		List<NotificacionActualizacionDatos> clientes = sql.query(con, SELECT_CLIENTES, params, NotificacionActualizacionDatos.class);

		AgregarNotificacion.EntradaAgregarNotificacion entradaAgregarNotificacion = new AgregarNotificacion.EntradaAgregarNotificacion();

		params.clear();

		params.put("IdEmpresa", 1L);
		params.put("CodigoUsuario", "PROJO");

		List<Notificacion> notificaciones = sql.query(con, SELECT_CORREDORA, params, Notificacion.class);
		Notificacion notificacion = notificaciones.parallelStream().findFirst().orElse(new Notificacion());
		long idEvento = 0L;

		entradaAgregarNotificacion.P_IDEMPRESA = 1L;
		entradaAgregarNotificacion.P_IDUSUARIO = 296L;
		entradaAgregarNotificacion.P_IDMODULO = "SOC";
		entradaAgregarNotificacion.P_EVENTOS = new ArrayList<>();
		entradaAgregarNotificacion.P_VALORES = new ArrayList<>();
		entradaAgregarNotificacion.P_ARCHIVOSADJUNTOS = new ArrayList<>();
		entradaAgregarNotificacion.P_IDDIRECCIONES = new ArrayList<>();

		for (NotificacionActualizacionDatos cliente : clientes) {

			AgregarNotificacion.Evento eventoNotificacion = new AgregarNotificacion.Evento();
			eventoNotificacion.CodigoTipoEvento = "NOTIF_VENCIMIENTO_ACT_DATOS";
			eventoNotificacion.IdSecuencia = idEvento;
			eventoNotificacion.FechaCreacionSistema = fechaSistema;
			eventoNotificacion.IdPersonaDestinatario = cliente.IdPersona;
			eventoNotificacion.IdPersonaRemitente = 0L;
			eventoNotificacion.IdPortafolio = 0L;
			entradaAgregarNotificacion.P_EVENTOS.add(eventoNotificacion);

			AgregarNotificacion.DireccionEvento direccionEventoNotificacion = new AgregarNotificacion.DireccionEvento();

			direccionEventoNotificacion.IdSecuencia = idEvento;
			direccionEventoNotificacion.TipoDestinatario = "TO";
			direccionEventoNotificacion.IdDireccion = cliente.IdDireccion;

			entradaAgregarNotificacion.P_IDDIRECCIONES.add(direccionEventoNotificacion);

			entradaAgregarNotificacion.addValor(idEvento, "CLIENTE", cliente.Nombre)
					.addValor(idEvento, "DOCUMENTOIDENTIDAD", cliente.DocumentoIdentidad)
					.addValor(idEvento, "CORREDORA", notificacion.RazonCorredora)
					.addValor(idEvento, "ATENCIONCLIENTE", notificacion.TelefonoCorredora);

			AgregarNotificacion not = new AgregarNotificacion();

			not.logica(con, entradaAgregarNotificacion);

		}

		out.P_OUT_ERRORESCONTADOR = 0L;

		return out;
	}

	public String fechaAviso(String fecha, Long diasCaducidad) {

		LocalDate fechaRestada = LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE);

		return fechaRestada.plusDays(diasCaducidad).format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	public static void main(String[] args) {

		try (Connection con = PlataformaUtils.getDESAConnection()) {

			NotificarCaducidadDatos demo = new NotificarCaducidadDatos();

			SalidaNotificarCaducidad ObjOut;
			try {
				ObjOut = demo.logica(con);
				System.out.println(ObjOut.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//con.rollback();
			con.commit();
			System.out.println("SALIDA");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
