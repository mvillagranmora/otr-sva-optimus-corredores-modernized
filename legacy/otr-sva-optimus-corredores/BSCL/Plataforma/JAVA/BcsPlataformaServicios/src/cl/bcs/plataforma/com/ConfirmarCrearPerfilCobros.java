package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.Arrays;
import java.util.HashMap;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;

/**
* Confirma la creación del perfil de cobro actualizando su estado de autorización.
* Migrado de SP COM.ConfirmarCrearPerfilCobros a Java.
*/
public class ConfirmarCrearPerfilCobros implements JavaCall, BcsAutorizaciones{
	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPerfilCobros.class);
	private final String SQL_UPDATE_ESTADO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilCobros_UpdateEstado.sql");
	private final String SQL_SELECT_SOLICITUDES_CAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilCobros_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_RECHAZO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilCobros_UpdateRechazo.sql");
	private final String SQL_UPDATE_RECHAZO_INACTIVAR = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilCobros_UpdateRechazoInactivar.sql");
	private final String SQL_UPDATE_DESCRIPCION = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilCobros_UpdateDescripcion.sql");

	public static class EntradaConfirmarCrearPerfilCobros {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilCobros;
		public String p_IdEstadoAutorizacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EntradaRechazaAutorizacionPerfilCobro {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPerfilCobros;
		public String p_IdConcepto;
 
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
	}
	
	public static class PerfilCobrosUpdateEstado {
		public String p_IdEstadoAutorizacion;
		public String p_IdPerfilCobros;
	}
	
	public static class PerfilCobrosUpdateDescripcion {
	    public String ValorNuevo;
	    public String IdPerfilCobros;
	}
	
	
	public static class SolicitudCambioData {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String IdEstadoAutorizacion;
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
	}
 
	public static class PerfilCobrosUpdateRechazo {
		public String p_IdEstadoAutorizacion;
		public String p_IdEstado;
		public Long p_IdPerfilCobros;
	}
		

	public static class SalidaConfirmarCrearPerfilCobros {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;

		public SalidaConfirmarCrearPerfilCobros() {
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearPerfilCobros in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPerfilCobros.class);
		SalidaConfirmarCrearPerfilCobros out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	} 
	 
	public SalidaConfirmarCrearPerfilCobros logica(Connection conn, EntradaConfirmarCrearPerfilCobros in) throws Exception {

	    logger.info("INICIO ConfirmarCrearPerfilCobros (Wrapper)");

	    SQL sql = new SQL();

	    SalidaConfirmarCrearPerfilCobros out = new SalidaConfirmarCrearPerfilCobros();
	 
	    try {

	        // Migrado de SP a Java: UPDATE directo del estado de autorización del perfil de cobro

	        PerfilCobrosUpdateEstado update = new PerfilCobrosUpdateEstado();

	        update.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;

	        update.p_IdPerfilCobros = in.p_IdPerfilCobros;

	        List<PerfilCobrosUpdateEstado> perfilesUpdate = Arrays.asList(update);

	        logger.debug("UPDATE COM.PerfilCobros - IdPerfilCobros: " + in.p_IdPerfilCobros);

	        sql.batch(conn, SQL_UPDATE_ESTADO, perfilesUpdate, PerfilCobrosUpdateEstado.class);
	 
	        // Buscar solicitud pendiente y aplicar el ValorNuevo al campo real

	        HashMap<String, Object> paramsValidar = new HashMap<>();

	        paramsValidar.put("p_IdEntidadOrigen", in.p_IdPerfilCobros);

	        paramsValidar.put("p_IdTipoEntidad", "PERFILCOBRO");

	        List<SolicitudCambioData> solicitudes = sql.query(conn, SQL_SELECT_SOLICITUDES_CAMBIO, paramsValidar, SolicitudCambioData.class);
	 
	        if (solicitudes != null && !solicitudes.isEmpty()) {

	            SolicitudCambioData solicitud = solicitudes.get(0);
	 
	            if ("DescripcionPerfilCobros".equals(solicitud.CampoModificado)) {

	                PerfilCobrosUpdateDescripcion updateDesc = new PerfilCobrosUpdateDescripcion();

	                updateDesc.ValorNuevo = solicitud.ValorNuevo;

	                updateDesc.IdPerfilCobros = in.p_IdPerfilCobros;

	                logger.debug("UPDATE COM.PerfilCobros descripcion - IdPerfilCobros: " + in.p_IdPerfilCobros);

	                sql.batch(conn, SQL_UPDATE_DESCRIPCION, Arrays.asList(updateDesc), PerfilCobrosUpdateDescripcion.class);

	            }
	 
	            // Marcar la solicitud como resuelta (AUT)

	            ModificarEstadoRegistroSolicitudCambio modificarEstado = new ModificarEstadoRegistroSolicitudCambio();

	            EntradaModificarEstadoRegistroSolicitudCambio entradaModificar = new EntradaModificarEstadoRegistroSolicitudCambio();

	            entradaModificar.p_IdEmpresa = in.p_IdEmpresa;

	            entradaModificar.p_IdUsuario = in.p_IdUsuario;

	            entradaModificar.p_IdModulo = in.p_IdModulo;

	            entradaModificar.p_IdSolicitudCambio = solicitud.IdSolicitudCambio;

	            entradaModificar.p_IdEstadoAutorizacion = "AUT";

	            modificarEstado.logica(conn, entradaModificar);

	        }
	 
	    } catch (Exception ex) {

	        logger.error("Error en wrapper: " + ex.getMessage(), ex);

	        throw ex;

	    }
	 
	    logger.info("FIN ConfirmarCrearPerfilCobros (Wrapper)");

	    return out;

	}
	 
	
	public SalidaConfirmarCrearPerfilCobros rechazarLogica(Connection conn, EntradaRechazaAutorizacionPerfilCobro in) throws Exception {

		logger.info("INICIO RechazaAutorizacionPerfilCobro");

		SQL sql = new SQL();

		SalidaConfirmarCrearPerfilCobros out = new SalidaConfirmarCrearPerfilCobros();
 
		try {

			final String v_IdEstadoAutorizacion = "RZD";

			final String v_TipoEntidad = "PERFILCOBRO";

			String v_IdEntidadOrigen = String.valueOf(in.p_IdPerfilCobros);
 
			// Validacion y modificacion de solicitud: solo para concepto MCPCOB

			if ("MCPCOB".equals(in.p_IdConcepto)) {

				logger.debug("Validar existencia de solicitud - IdPerfilCobros: " + v_IdEntidadOrigen);
 
				HashMap<String, Object> paramsValidar = new HashMap<>();

				paramsValidar.put("p_IdEntidadOrigen", v_IdEntidadOrigen);

				paramsValidar.put("p_IdTipoEntidad", v_TipoEntidad);
 
				List<SolicitudCambioData> solicitudes = sql.query(conn, SQL_SELECT_SOLICITUDES_CAMBIO, paramsValidar, SolicitudCambioData.class);
 
				if (solicitudes == null || solicitudes.isEmpty()) {

					ErrorDetalle error = new ErrorDetalle();

					error.Codigo = "SOLICITUD_NO_EXISTE";

					error.Descripcion = "La solicitud no existe";

					out.p_out_ErroresDetalle.add(error);

					out.p_out_ErroresContador = (long) out.p_out_ErroresDetalle.size();

					return out;

				}
 
				// Modificar estado del registro de solicitud a RZD (via clase Java migrada)

				logger.debug("Modificar estado registro solicitud cambio");

				ModificarEstadoRegistroSolicitudCambio modificarEstado = new ModificarEstadoRegistroSolicitudCambio();

				EntradaModificarEstadoRegistroSolicitudCambio entradaModificar = new EntradaModificarEstadoRegistroSolicitudCambio();

				entradaModificar.p_IdEmpresa = in.p_IdEmpresa;

				entradaModificar.p_IdUsuario = in.p_IdUsuario;

				entradaModificar.p_IdModulo = in.p_IdModulo;

				entradaModificar.p_IdSolicitudCambio = solicitudes.get(0).IdSolicitudCambio;

				entradaModificar.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;
 
				SalidaModificarEstadoRegistroSolicitudCambio salidaModificar = modificarEstado.logica(conn, entradaModificar);
 
				if (salidaModificar.P_OUT_ERRORESCONTADOR > 0) {

					out.p_out_ErroresContador = salidaModificar.P_OUT_ERRORESCONTADOR;

					return out;

				}

			}
 
			// UPDATE del perfil de cobro a estado RZD (siempre)

			logger.debug("UPDATE COM.PerfilCobros rechazo - IdPerfilCobros: " + v_IdEntidadOrigen);

			if ("NCPPRO".equals(in.p_IdConcepto)) {

				// Concepto NCPPRO: ademas inactiva el perfil (IdEstado = INA)

				PerfilCobrosUpdateRechazo update = new PerfilCobrosUpdateRechazo();

				update.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				update.p_IdEstado = "INA";

				update.p_IdPerfilCobros = in.p_IdPerfilCobros;

				List<PerfilCobrosUpdateRechazo> perfilesUpdate = Arrays.asList(update);

				sql.batch(conn, SQL_UPDATE_RECHAZO_INACTIVAR, perfilesUpdate, PerfilCobrosUpdateRechazo.class);

			} else {

				// Otros conceptos: solo cambia el estado de autorizacion, no toca IdEstado

				PerfilCobrosUpdateRechazo update = new PerfilCobrosUpdateRechazo();

				update.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				update.p_IdPerfilCobros = in.p_IdPerfilCobros;

				List<PerfilCobrosUpdateRechazo> perfilesUpdate = Arrays.asList(update);

				sql.batch(conn, SQL_UPDATE_RECHAZO, perfilesUpdate, PerfilCobrosUpdateRechazo.class);

			}
 
		} catch (Exception ex) {

			logger.error("Error en RechazaAutorizacionPerfilCobro: " + ex.getMessage(), ex);

			throw ex;

		}
 
		logger.info("FIN RechazaAutorizacionPerfilCobro");

		return out;

	}
	
	@Override

	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {

	    logger.info("INICIO::ConfirmarCrearPerfilCobros.autorizarEvento");

	    SalidaAutorizacion salida = new SalidaAutorizacion();

	    try {

	        for (Evento evento : entrada.p_Lista) {

	            EntradaConfirmarCrearPerfilCobros in = new EntradaConfirmarCrearPerfilCobros();

	            in.p_IdEmpresa = entrada.p_IdEmpresa;

	            in.p_IdUsuario = entrada.p_IdUsuario;

	            in.p_IdModulo = entrada.p_IdModulo;

	            in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;

	            in.p_IdPerfilCobros = evento.NumeroOperacionConcepto;

	            in.p_IdEstadoAutorizacion = "AUT";
	 
	            SalidaConfirmarCrearPerfilCobros out = logica(conn, in);
	 
	            if (out.p_out_ErroresContador != null && out.p_out_ErroresContador > 0) {

	                salida.P_OUT_ERRORESCONTADOR += out.p_out_ErroresContador;

	            }

	        }

	    } catch (Exception e) {

	        logger.error("Error en autorizarEvento: " + e.getMessage(), e);

	        salida.P_OUT_ERRORESCONTADOR = 1L;

	    }

	    logger.info("FIN::ConfirmarCrearPerfilCobros.autorizarEvento");

	    return salida;

	}
	 
	@Override

	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {

	    logger.info("INICIO::ConfirmarCrearPerfilCobros.rechazarEvento");

	    SalidaAutorizacion salida = new SalidaAutorizacion();

	    try {

	        for (Evento evento : entrada.p_Lista) {

	            EntradaRechazaAutorizacionPerfilCobro in = new EntradaRechazaAutorizacionPerfilCobro();

	            in.p_IdEmpresa = entrada.p_IdEmpresa;

	            in.p_IdUsuario = entrada.p_IdUsuario;

	            in.p_IdModulo = entrada.p_IdModulo;

	            in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;

	            in.p_IdPerfilCobros = Long.valueOf(evento.NumeroOperacionConcepto);

	            in.p_IdConcepto = evento.IdConcepto;
	 
	            SalidaConfirmarCrearPerfilCobros out = rechazarLogica(conn, in);
	 
	            if (out.p_out_ErroresContador != null && out.p_out_ErroresContador > 0) {

	                salida.P_OUT_ERRORESCONTADOR += out.p_out_ErroresContador;

	            }

	        }

	    } catch (Exception e) {

	        logger.error("Error en rechazarEvento: " + e.getMessage(), e);

	        salida.P_OUT_ERRORESCONTADOR = 1L;

	    }

	    logger.info("FIN::ConfirmarCrearPerfilCobros.rechazarEvento");

	    return salida;

	}
	
	
	
	
}