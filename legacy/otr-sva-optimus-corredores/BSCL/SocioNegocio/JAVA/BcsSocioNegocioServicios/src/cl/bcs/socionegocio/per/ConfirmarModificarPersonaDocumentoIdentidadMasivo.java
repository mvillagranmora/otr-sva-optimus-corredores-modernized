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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ModificarPersonaDocumentoIdentidadMasivo.DocumentoIdentidad;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaDocumentoIdentidadMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona  {

	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaDocumentoIdentidadMasivo.class);
	
	private String SQL_UPDATE_DATOS_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentoIdentidadMasivo_UpdateDatosDocumentoIdentidad.sql");
	
	private final String TIPO_LOG = "MODIFICACION";
	private final String ID_TIPO_ENTIDAD = "DOCUMENTOIDENTIDAD";
	private final String NOMBRE_PROCESO_LOG  = "LOG_NOMBRE_PROCESO_MODIFICAR_DOCUMENTO_IDENTIDAD";
	
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
		logger.info("INICIO::ConfirmarModificarPersonaDocumentoIdentidadMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();

		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_DIRECCION_FISICA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}

		List<DocumentoIdentidad> datosEntidad = obtenerEntidades(solicitudesCambios);
		
		modificarCampoDocumentoIdentidad(con, datosEntidad);
		
		ModificarEstadoRegistroSolicitudCambioPersona modificarEstadoRegistroSolicitudCambioPersona = new ModificarEstadoRegistroSolicitudCambioPersona();
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambioPersona.logica(con, entrada, solicitudesCambios);
		
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
		agregarLogPersona.agregarLog(con, entrada, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaDocumentoIdentidadMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<DocumentoIdentidad> obtenerEntidades(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDocumentoIdentidadMasivo.obtenerEntidadesDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<DocumentoIdentidad> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
			DocumentoIdentidad datosEntidades = new DocumentoIdentidad();
			
			List<SolicitudCambio> cambiosEntidad = persona.getValue();
			datosEntidades.IdDocumentoIdentidad = persona.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosEntidad) {
				Field campo = datosEntidades.getClass().getField(solicitudCambio.CampoModificado);
				if(campo.getType().isAssignableFrom(String.class)) {
					campo.set(datosEntidades, solicitudCambio.ValorNuevo);
				}
				if(campo.getType().isAssignableFrom(Long.class)) {
					campo.set(datosEntidades, Long.parseLong(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
					campo.set(datosEntidades, Fecha.toDateTime(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Date.class)) {
					campo.set(datosEntidades, Fecha.toDate(solicitudCambio.ValorNuevo));
				}
			}
			datosAActualizar.add(datosEntidades);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDocumentoIdentidadMasivo.obtenerEntidadesDatosGenerales", fin - inicio);
		return datosAActualizar;
	}
	
	private void modificarCampoDocumentoIdentidad(Connection con, List<DocumentoIdentidad> lstEntidad) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDocumentoIdentidadMasivo.modificarCampoDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		if( lstEntidad.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_DATOS_DOCUMENTO_IDENTIDAD, lstEntidad, DocumentoIdentidad.class);			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDocumentoIdentidadMasivo.modificarCampoDocumentoIdentidad", fin - inicio);
	}
	
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{p_IdEmpresa:1,p_IdUsuario:21,p_IdModulo:\"SOC\",p_UsaReglasNegocio: \"N\",p_PersonaDatosGenerales: [{IdPersona:1790725}],p_IdEstadoAutorizacion: \"AUT\"}";
			EntradaConfirmarEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaConfirmarEjecutarCambiosPersona.class);
			
			ConfirmarModificarPersonaDocumentoIdentidadMasivo demo = new ConfirmarModificarPersonaDocumentoIdentidadMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaConfirmarEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");

	}
}