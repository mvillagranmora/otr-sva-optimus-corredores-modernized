package cl.bcs.socionegocio.per;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.NombreFantasia;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaNombreFantasiaMasivo implements JavaCall,ConfirmarEjecutarCambiosPersona {
	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaNombreFantasiaMasivo.class);
	
	private String SQL_UPDATE_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaNombreFantasiaMasivo_UpdateNombreFantasia.sql");

	private final String TIPO_LOG = "MODIFICACION";
	private final String ID_TIPO_ENTIDAD = "NOMBREFANTASIA";
	private final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_MODIFICAR_NOMBRE_FANTASIA";

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarCambiosPersona.class);
		SalidaConfirmarEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaNombreFantasiaoMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();

		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_TARJETA_BANCARIA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<NombreFantasia> datosEntidad = obtenerEntidades(solicitudesCambios);

		modificarCampoNombreFantasia(con, datosEntidad);
		
		ModificarEstadoRegistroSolicitudCambioPersona modificarEstadoRegistroSolicitudCambioPersona = new ModificarEstadoRegistroSolicitudCambioPersona();
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambioPersona.logica(con, entrada, solicitudesCambios);
		
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
		agregarLogPersona.agregarLog(con, entrada, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaNombreFantasiaoMasivo", finServicio - inicioServicio);
		return out;
	}

	private void modificarCampoNombreFantasia(Connection con, List<NombreFantasia> datosEntidad) throws SQLException {
		logger.debug("INICIO::ConfirmarModificarPersonaNombreFantasiaoMasivo.modificarCampoNombreFantasia");
		long inicio = System.currentTimeMillis();
		
		if( datosEntidad.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_NOMBRE_FANTASIA, datosEntidad, NombreFantasia.class);			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaNombreFantasiaoMasivo.modificarCampoNombreFantasia", fin - inicio);
	}

	private List<NombreFantasia> obtenerEntidades(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaNombreFantasiaoMasivo.obtenerEntidadesNombreFantasia");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<NombreFantasia> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
			NombreFantasia datosEntidad = new NombreFantasia();
			
			List<SolicitudCambio> cambiosEntidad = persona.getValue();
			datosEntidad.IdNombreFantasia = persona.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosEntidad) {
				Field campo = datosEntidad.getClass().getField(solicitudCambio.CampoModificado);
				if(campo.getType().isAssignableFrom(String.class)) {
					campo.set(datosEntidad, solicitudCambio.ValorNuevo);
				}
				if(campo.getType().isAssignableFrom(Long.class)) {
					campo.set(datosEntidad, Long.parseLong(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
					campo.set(datosEntidad, Fecha.toDateTime(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Date.class)) {
					campo.set(datosEntidad, Fecha.toDate(solicitudCambio.ValorNuevo));
				}
			}
			datosAActualizar.add(datosEntidad);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaNombreFantasiaoMasivo.obtenerEntidadesNombreFantasia", fin - inicio);
		return datosAActualizar;
	}
}
