package cl.bcs.plataforma.com;
 
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
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
 
public class ConfirmarModificarPerfilCobrosDetalle implements JavaCall {
 
	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPerfilCobrosDetalle.class); 
	private static final String TIPO_ENTIDAD = "PERFILCOBRODETALLE"; 
	private final String SQL_SELECT_SOLICITUDCAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Select_SolicitudCambio.sql"); 
	private final String SQL_UPDATE_IDVALORCOBRO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdValorCobro.sql");
	private final String SQL_UPDATE_IDBOLSA = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdBolsa.sql");
	private final String SQL_UPDATE_IDTIPOPRODUCTO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdTipoProducto.sql");
	private final String SQL_UPDATE_IDTIPOOPERACION = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdTipoOperacion.sql");
	private final String SQL_UPDATE_IDCANALINGRESO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdCanalIngreso.sql");
	private final String SQL_UPDATE_IDMONEDA = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdMoneda.sql");
	private final String SQL_UPDATE_DIASPLAZO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_DiasPlazo.sql");
	private final String SQL_UPDATE_PRIORIDAD = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_Prioridad.sql");
	private final String SQL_UPDATE_IDESTADO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_IdEstado.sql"); 
	private final String SQL_UPDATE_VALORCOBRO_IDCOBRO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_ValorCobro_IdCobro.sql");
	private final String SQL_UPDATE_VALORCOBRO_VALORCOBRO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_ValorCobro_ValorCobro.sql");
	private final String SQL_UPDATE_VALORCOBRO_VALORTOPEMINIMOORDEN = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_ValorCobro_ValorTopeMinimoOrden.sql");
	private final String SQL_UPDATE_VALORCOBRO_IDMONEDA = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilCobrosDetalle_Update_ValorCobro_IdMoneda.sql");
 
	public static class EntradaConfirmarModificarPerfilCobrosDetalle {

		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDetallePerfilCobros;
		public String p_IdEstadoAutorizacion; 

		public String toString() {

			return Utils.printJson(this, false);
		}
	}
 
	public static class AdvertenciaDetalle {

		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
 
	public static class ErrorDetalle {

		public String Codigo;
		public String Descripcion;

		public ErrorDetalle() {}
 
		public ErrorDetalle(String codigo, String descripcion) {

			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}
 
	public static class SalidaConfirmarModificarPerfilCobrosDetalle {

		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;
 
		public SalidaConfirmarModificarPerfilCobrosDetalle() {

			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}
 
		public String toString() {

			return Utils.printJson(this, false);
		}
	}
 
	public static class SolicitudCambioData {

		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
		public String IdEstadoAutorizacion;
	}
 
	public static class UpdateCampo {

		public String ValorNuevo;
		public Long IdDetallePerfilCobros;
	}
 
	@Override

	public JsonObject call(Connection conn, JsonObject jin) throws Exception {

		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarPerfilCobrosDetalle in = gson.fromJson(jin.toString(), EntradaConfirmarModificarPerfilCobrosDetalle.class);
		SalidaConfirmarModificarPerfilCobrosDetalle out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}
 
	public SalidaConfirmarModificarPerfilCobrosDetalle logica(Connection conn, EntradaConfirmarModificarPerfilCobrosDetalle in) throws Exception {

		logger.info("INICIO ConfirmarModificarPerfilCobrosDetalle"); 
		SQL sql = new SQL();

		SalidaConfirmarModificarPerfilCobrosDetalle out = new SalidaConfirmarModificarPerfilCobrosDetalle();
 
		try {

			String idEntidadOrigen = String.valueOf(in.p_IdDetallePerfilCobros);
 
			HashMap<String, Object> paramsValidar = new HashMap<>();

			paramsValidar.put("p_IdEntidadOrigen", idEntidadOrigen);

			paramsValidar.put("p_IdTipoEntidad", TIPO_ENTIDAD);
 
			logger.debug("Consultar solicitudes de cambio pendientes - IdDetallePerfilCobros: " + idEntidadOrigen);

			List<SolicitudCambioData> solicitudes = sql.query(conn, SQL_SELECT_SOLICITUDCAMBIO, paramsValidar, SolicitudCambioData.class);
 
			if (solicitudes == null || solicitudes.isEmpty()) {

				out.p_out_ErroresDetalle.add(new ErrorDetalle("SOLICITUD_NO_EXISTE", "La solicitud no existe"));

				out.p_out_ErroresContador = (long) out.p_out_ErroresDetalle.size();
				return out;
			}
 
			boolean esPersonalizado = false;
 
			for (SolicitudCambioData solicitud : solicitudes) {

				if ("EsPersonalizado".equals(solicitud.CampoModificado)) {

					esPersonalizado = true;
					continue;
				}
 
				UpdateCampo update = new UpdateCampo();

				update.ValorNuevo = solicitud.ValorNuevo;

				update.IdDetallePerfilCobros = in.p_IdDetallePerfilCobros;

				List<UpdateCampo> updateList = Arrays.asList(update);
 
				switch (solicitud.CampoModificado) {

					case "IdValorCobro":

						sql.batch(conn, SQL_UPDATE_IDVALORCOBRO, updateList, UpdateCampo.class);
						break;

					case "IdBolsa":

						sql.batch(conn, SQL_UPDATE_IDBOLSA, updateList, UpdateCampo.class);
						break;

					case "IdTipoProducto":

						sql.batch(conn, SQL_UPDATE_IDTIPOPRODUCTO, updateList, UpdateCampo.class);
						break;

					case "IdTipoOperacion":

						sql.batch(conn, SQL_UPDATE_IDTIPOOPERACION, updateList, UpdateCampo.class);
						break;

					case "IdCanalIngreso":

						sql.batch(conn, SQL_UPDATE_IDCANALINGRESO, updateList, UpdateCampo.class);
						break;

					case "IdMoneda":

						sql.batch(conn, SQL_UPDATE_IDMONEDA, updateList, UpdateCampo.class);
						break;

					case "DiasPlazo":

						sql.batch(conn, SQL_UPDATE_DIASPLAZO, updateList, UpdateCampo.class);
						break;

					case "Prioridad":

						sql.batch(conn, SQL_UPDATE_PRIORIDAD, updateList, UpdateCampo.class);
						break;

					case "IdEstado":

						sql.batch(conn, SQL_UPDATE_IDESTADO, updateList, UpdateCampo.class);
						break;

					default:
						break;
				}
			}
 
			if (esPersonalizado) {

				for (SolicitudCambioData solicitud : solicitudes) {

					UpdateCampo update = new UpdateCampo();
					update.ValorNuevo = solicitud.ValorNuevo;
					update.IdDetallePerfilCobros = in.p_IdDetallePerfilCobros;

					List<UpdateCampo> updateList = Arrays.asList(update);
 
					switch (solicitud.CampoModificado) {

						case "IdCobro":

							sql.batch(conn, SQL_UPDATE_VALORCOBRO_IDCOBRO, updateList, UpdateCampo.class);
							break;

						case "ValorCobro":

							sql.batch(conn, SQL_UPDATE_VALORCOBRO_VALORCOBRO, updateList, UpdateCampo.class);
							break;

						case "ValorTopeMinimoOrden":

							sql.batch(conn, SQL_UPDATE_VALORCOBRO_VALORTOPEMINIMOORDEN, updateList, UpdateCampo.class);
							break;

						case "IdMoneda":

							sql.batch(conn, SQL_UPDATE_VALORCOBRO_IDMONEDA, updateList, UpdateCampo.class);
							break;

						default:
							break;
					}
				}
			}
 
			AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();
 
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "ACTUALIZACION";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "DETALLE PERFIL COBROS";
			entradaLog.p_Log.add(log);
 
			for (SolicitudCambioData solicitud : solicitudes) {

				AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();

				detLog.IdCabeceraLog = 1L;
				detLog.IdTrabajo = 0L;
				detLog.SubRegistro = idEntidadOrigen;
				detLog.CampoRegistro = solicitud.CampoModificado;
				detLog.ValorAntiguo = solicitud.ValorAntiguo;
				detLog.ValorNuevo = solicitud.ValorNuevo;
				entradaLog.p_LogDetalle.add(detLog);

			}
 
			AgregarLog agregarLogService = new AgregarLog();

			agregarLogService.logica(conn, entradaLog);
 
			ModificarEstadoRegistroSolicitudCambio modificarEstado = new ModificarEstadoRegistroSolicitudCambio();

			EntradaModificarEstadoRegistroSolicitudCambio entradaModificar = new EntradaModificarEstadoRegistroSolicitudCambio();

			entradaModificar.p_IdEmpresa = in.p_IdEmpresa;

			entradaModificar.p_IdUsuario = in.p_IdUsuario;

			entradaModificar.p_IdModulo = in.p_IdModulo;

			entradaModificar.p_IdEntidadOrigen = idEntidadOrigen;

			entradaModificar.p_IdTipoEntidad = TIPO_ENTIDAD;

			entradaModificar.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
 
			SalidaModificarEstadoRegistroSolicitudCambio salidaModificar = modificarEstado.logica(conn, entradaModificar);
 
			if (salidaModificar.P_OUT_ERRORESCONTADOR > 0) {

				out.p_out_ErroresContador = salidaModificar.P_OUT_ERRORESCONTADOR;
			}
 
		} catch (Exception ex) {

			logger.error("Error en ConfirmarModificarPerfilCobrosDetalle: " + ex.getMessage(), ex);

			throw ex;
		}
 
		logger.info("FIN ConfirmarModificarPerfilCobrosDetalle");
		return out;
	} 
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
 
			ConfirmarModificarPerfilCobrosDetalle demo = new ConfirmarModificarPerfilCobrosDetalle();
 
			EntradaConfirmarModificarPerfilCobrosDetalle entrada = new EntradaConfirmarModificarPerfilCobrosDetalle();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 76770L;
			entrada.p_IdModulo = "PLA";
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdDetallePerfilCobros = 131946L;
			entrada.p_IdEstadoAutorizacion = "AUT";
 
			SalidaConfirmarModificarPerfilCobrosDetalle salida = demo.logica(conn, entrada);
 
			System.out.println(salida.toString());
 
			conn.rollback();
 
		} catch (Exception e) {
			e.printStackTrace();
		}
 
		System.out.println("== END ==");
	}
	
	
	
	
	
	
} 