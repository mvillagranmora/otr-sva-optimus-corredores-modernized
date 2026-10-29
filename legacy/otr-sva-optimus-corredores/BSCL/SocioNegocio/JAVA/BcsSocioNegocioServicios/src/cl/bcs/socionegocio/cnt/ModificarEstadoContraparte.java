package cl.bcs.socionegocio.cnt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: CNT.ModificarEstadoContraparte
 * Evalua el estado de la contraparte a modificar (bloqueo/desbloqueo) y lo deja como
 * solicitud de cambio (CONTRAPARTE-ESTADO). Si no requiere autorizacion, confirma de
 * inmediato invocando ConfirmarModificarEstadoContraparte; si requiere, genera el evento
 * de autorizacion para que lo confirme el supervisor.
 */
public class ModificarEstadoContraparte implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ModificarEstadoContraparte.class);

	private final String SQL_SELECT_COUNT_CONTRAPARTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/cnt/sql/ModificarEstadoContraparte_Select_CountContraparte.sql");
	private final String SQL_SELECT_VALORES_ACTUALES = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/cnt/sql/ModificarEstadoContraparte_Select_ValoresActuales.sql");
	private final String SQL_SELECT_GLOSA = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/cnt/sql/ModificarEstadoContraparte_Select_Glosa.sql");

	public static class EntradaModificarEstadoContraparte {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdContraparte;
		public String p_IdEstado;
		public String p_IdTipo;
		public String p_Fecha;
		public String p_Comentario;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaModificarEstadoContraparte {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaModificarEstadoContraparte() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Contador {
		public Long Contador;
	}

	public static class ValoresActualesContraparte {
		public String IdEstado;
		public String IdTipoBloqueo;
		public String FechaBloqueo;
		public String ComentarioBloqueo;
		public String IdTipoDesbloqueo;
		public String FechaDesbloqueo;
		public String ComentarioDesbloqueo;
	}

	public static class GlosaResult {
		public String Glosa;
	}

	public SalidaModificarEstadoContraparte logica(Connection conn, EntradaModificarEstadoContraparte in) throws Exception {
		logger.info("INIT ModificarEstadoContraparte");
		SalidaModificarEstadoContraparte salida = new SalidaModificarEstadoContraparte();
		SQL sql = new SQL();

		try {
			String v_TipoEntidad = "CONTRAPARTE-ESTADO";
			String v_IdConcepto = "CAECNT";
			String v_EstadoAutorizacion = "PEN";
			String v_IdEntidadOrigen = String.valueOf(in.p_IdContraparte);

			// Validar existencia de la contraparte
			logger.debug("Validar existencia de contraparte");
			HashMap<String, Object> paramsContraparte = new HashMap<>();
			paramsContraparte.put("IdContraparte", in.p_IdContraparte);

			List<Contador> countList = sql.query(conn, SQL_SELECT_COUNT_CONTRAPARTE, paramsContraparte, Contador.class);
			Long v_countContraparte = (countList != null && !countList.isEmpty()) ? countList.get(0).Contador : 0L;

			if (v_countContraparte == 0) {
				String mensajeNoExiste = Diccionario.getMensaje(conn, in.p_IdEmpresa, "CONTRAPARTE_NO_EXISTE", new HashMap<String, String>());
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("CONTRAPARTE_NO_EXISTE", mensajeNoExiste));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Validar que el estado solicitado sea uno de los soportados (bloqueo/desbloqueo)
			if (!"BLO".equals(in.p_IdEstado) && !"ACT".equals(in.p_IdEstado)) {
				String mensajeEstadoNoDefinido = Diccionario.getMensaje(conn, in.p_IdEmpresa, "ESTADO_NO_DEFINIDO", new HashMap<String, String>());
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ESTADO_NO_DEFINIDO", mensajeEstadoNoDefinido));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Obtener valores actuales para dejarlos como ValorAntiguo en la solicitud de cambio
			logger.debug("Obtener valores actuales de la contraparte");
			List<ValoresActualesContraparte> valoresActualesList = sql.query(conn, SQL_SELECT_VALORES_ACTUALES, paramsContraparte, ValoresActualesContraparte.class);
			ValoresActualesContraparte valoresActuales = (valoresActualesList != null && !valoresActualesList.isEmpty())
				? valoresActualesList.get(0) : new ValoresActualesContraparte();

			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
			Long v_IdSolicitudCambio = secuencia.get(0);

			// Armar los registros de solicitud de cambio segun si es bloqueo (BLO) o desbloqueo (ACT)
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> registros = new ArrayList<>();
			if ("BLO".equals(in.p_IdEstado)) {
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "IdEstado", "NVARCHAR", valoresActuales.IdEstado, in.p_IdEstado, v_EstadoAutorizacion));
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "IdTipoBloqueo", "NVARCHAR", valoresActuales.IdTipoBloqueo, in.p_IdTipo, v_EstadoAutorizacion));
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "FechaBloqueo", "SECONDDATE", valoresActuales.FechaBloqueo, in.p_Fecha, v_EstadoAutorizacion));
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "ComentarioBloqueo", "NVARCHAR", valoresActuales.ComentarioBloqueo, in.p_Comentario, v_EstadoAutorizacion));
			} else {
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "IdEstado", "NVARCHAR", valoresActuales.IdEstado, in.p_IdEstado, v_EstadoAutorizacion));
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "IdTipoDesbloqueo", "NVARCHAR", valoresActuales.IdTipoDesbloqueo, in.p_IdTipo, v_EstadoAutorizacion));
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "FechaDesbloqueo", "SECONDDATE", valoresActuales.FechaDesbloqueo, in.p_Fecha, v_EstadoAutorizacion));
				registros.add(construirRegistro(v_IdSolicitudCambio, v_TipoEntidad, v_IdEntidadOrigen, "ComentarioDesbloqueo", "NVARCHAR", valoresActuales.ComentarioDesbloqueo, in.p_Comentario, v_EstadoAutorizacion));
			}

			// Crear los registros de solicitud de cambio (quedan en PEN)
			logger.debug("Crear registros de solicitud de cambio");
			CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio entradaCrearSolicitud =
				new CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio();
			entradaCrearSolicitud.p_RegistroSolicitudCambio = registros;

			CrearRegistroSolicitudCambio crearSolicitudService = new CrearRegistroSolicitudCambio();
			crearSolicitudService.logica(conn, entradaCrearSolicitud);

			// Consultar si requiere autorizacion
			logger.debug("Consultando si requiere autorización");
			String v_RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, v_IdConcepto);

			if ("S".equals(v_RequiereAutorizacion)) {
				// Obtener glosa para el evento
				logger.debug("Obtener glosa de la contraparte");
				List<GlosaResult> glosaList = sql.query(conn, SQL_SELECT_GLOSA, paramsContraparte, GlosaResult.class);
				String v_Glosa = (glosaList != null && !glosaList.isEmpty()) ? glosaList.get(0).Glosa : "";

				String v_FechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

				// Generar evento de autorización
				logger.debug("Crear evento de autorización");
				CrearEventoAutorizacion.ServiceEntrada crearEvtIn = new CrearEventoAutorizacion.ServiceEntrada();
				crearEvtIn.p_IdEmpresa = in.p_IdEmpresa;
				crearEvtIn.p_IdUsuario = in.p_IdUsuario;
				crearEvtIn.p_IdModulo = in.p_IdModulo;
				crearEvtIn.p_UsaReglasNegocio = "N";
				crearEvtIn.p_IdTipoEvento = v_IdConcepto;
				crearEvtIn.p_IdModuloSolicita = in.p_IdModulo;
				crearEvtIn.p_IdUsuarioSolicita = in.p_IdUsuario;
				crearEvtIn.p_Fecha = v_FechaSistema;
				crearEvtIn.p_IdConcepto = v_IdConcepto;
				crearEvtIn.p_NumeroOperacionConcepto = v_IdEntidadOrigen;
				crearEvtIn.p_FechaOperacionConcepto = v_FechaSistema;
				crearEvtIn.p_IdCliente = in.p_IdContraparte;
				crearEvtIn.p_Glosa = v_Glosa;
				crearEvtIn.p_IdCanal = "OPT";

				CrearEventoAutorizacion crearEvtService = new CrearEventoAutorizacion();
				CrearEventoAutorizacion.ServiceSalida crearEvtOut = crearEvtService.logica(conn, crearEvtIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += crearEvtOut.p_out_AdvertenciasContador.intValue();
				for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : crearEvtOut.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += crearEvtOut.p_out_ErroresContador.intValue();
				for (CrearEventoAutorizacion.ObjERRORESDETALLE err : crearEvtOut.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}

				if (salida.P_OUT_ERRORESCONTADOR == 0) {
					String mensajeAutorizacion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<String, String>());
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", mensajeAutorizacion));
					salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
				}

			} else {
				// No requiere autorizacion: confirmar de inmediato usando el nuevo servicio Java
				logger.debug("Confirmar modificación de estado de contraparte");
				ConfirmarModificarEstadoContraparte.EntradaConfirmarModificarEstadoContraparte confirmarIn =
					new ConfirmarModificarEstadoContraparte.EntradaConfirmarModificarEstadoContraparte();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdContraparte = in.p_IdContraparte;
				confirmarIn.p_IdEstadoAutorizacion = "ING";

				ConfirmarModificarEstadoContraparte confirmarService = new ConfirmarModificarEstadoContraparte();
				ConfirmarModificarEstadoContraparte.SalidaConfirmarModificarEstadoContraparte confirmarOut = confirmarService.logica(conn, confirmarIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.P_OUT_ADVERTENCIASCONTADOR;
				for (ConfirmarModificarEstadoContraparte.AdvertenciaDetalle adv : confirmarOut.P_OUT_ADVERTENCIASDETALLE) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.P_OUT_ERRORESCONTADOR;
				for (ConfirmarModificarEstadoContraparte.ErrorDetalle err : confirmarOut.P_OUT_ERRORESDETALLE) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}

				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}

				salida.P_OUT_REFID = v_IdSolicitudCambio;
			}

		} catch (Exception ex) {
			logger.error("Error en ModificarEstadoContraparte: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_MODIFICAR_ESTADO_CONTRAPARTE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ModificarEstadoContraparte");
		return salida;
	}

	private CrearRegistroSolicitudCambio.RegistroSolicitudCambio construirRegistro(Long idSolicitudCambio, String idTipoEntidad, String idEntidadOrigen,
			String campoModificado, String tipoDato, String valorAntiguo, String valorNuevo, String idEstadoAutorizacion) {
		CrearRegistroSolicitudCambio.RegistroSolicitudCambio registro = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
		registro.IdSolicitudCambio = idSolicitudCambio;
		registro.IdTipoEntidad = idTipoEntidad;
		registro.IdEntidadOrigen = idEntidadOrigen;
		registro.CampoModificado = campoModificado;
		registro.TipoDato = tipoDato;
		registro.ValorAntiguo = valorAntiguo != null ? valorAntiguo : "";
		registro.ValorNuevo = valorNuevo != null ? valorNuevo : "";
		registro.IdEstadoAutorizacion = idEstadoAutorizacion;
		return registro;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarEstadoContraparte in = gson.fromJson(jin.toString(), EntradaModificarEstadoContraparte.class);
		SalidaModificarEstadoContraparte out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdContraparte\":123456,\"p_IdEstado\":\"BLO\",\"p_IdTipo\":\"MOROSO\",\"p_Fecha\":\"2026-07-21\",\"p_Comentario\":\"Prueba\"}";
			EntradaModificarEstadoContraparte entrada = gson.fromJson(json, EntradaModificarEstadoContraparte.class);
			ModificarEstadoContraparte demo = new ModificarEstadoContraparte();
			SalidaModificarEstadoContraparte salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
