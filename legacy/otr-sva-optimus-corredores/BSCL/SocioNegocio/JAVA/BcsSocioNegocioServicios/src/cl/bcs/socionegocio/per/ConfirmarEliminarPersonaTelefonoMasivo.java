package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConfirmarEliminarPersonaTelefonoMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona {
	
static final Logger logger = LogManager.getLogger(ConfirmarEliminarPersonaTelefonoMasivo.class);
	
	private String SQL_DELETE_DIRECCION_ELECTRONICA	= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaTelefonoMasivo_DeleteTelefono.sql");
	
	private final String TIPO_LOG = "ELIMINACION";
	private final String ID_TIPO_ENTIDAD = "TELEFONO-BORRAR";
	private final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_ELIMINAR_TELEFONO";

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarCambiosPersona.class);
		SalidaConfirmarEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Override
	public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarEliminarPersonaTelefonoMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();

		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_DIRECCION_ELECTRONICA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}

		ObtenerEntidadesPersonaEliminar obtenerEntidadesPersonaEliminar = new ObtenerEntidadesPersonaEliminar();
		List<EntidadPersonaEliminar> datosEntidadPersonaEliminar = obtenerEntidadesPersonaEliminar.logica(solicitudesCambios);
		
		EliminarEntidadPersona eliminarEntidadPersona = new EliminarEntidadPersona();
		eliminarEntidadPersona.logica(con, datosEntidadPersonaEliminar, SQL_DELETE_DIRECCION_ELECTRONICA);
		
		ModificarEstadoRegistroSolicitudCambioPersona modificarEstadoRegistroSolicitudCambioPersona = new ModificarEstadoRegistroSolicitudCambioPersona();
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambioPersona.logica(con, entrada, solicitudesCambios);
		
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
		agregarLogPersona.agregarLog(con, entrada, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarEliminarPersonaTelefonoMasivo", finServicio - inicioServicio);
		return out;
	}
	
}
