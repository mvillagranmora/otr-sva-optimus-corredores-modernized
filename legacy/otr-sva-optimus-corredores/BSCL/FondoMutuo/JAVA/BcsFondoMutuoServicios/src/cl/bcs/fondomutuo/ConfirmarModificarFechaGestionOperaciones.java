package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ActualizarFechaGestionOperaciones.SolicitudesAporteRescate;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarFechaGestionOperaciones implements JavaCall, ConfirmarEjecutarFechaGestionOperaciones{


	static final Logger logger = LogManager.getLogger(ConfirmarModificarFechaGestionOperaciones.class);
	
	private final String SQL_UPDATE_FECHAS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarFechaGestionOperaciones_UpdateFechas.sql");
	private final String SQL_INSERT_HISTORIAL = loadResourceToString("cl/bcs/fondomutuo/sql/HistorialCambiosModificarFechas_InsertCambios.sql");
	private final String SQL_SELECT_HISTORIAL = loadResourceToString("cl/bcs/fondomutuo/sql/HistorialCambiosModificarFechas_SelectCambios.sql");
	private final String SQL_SELECT_SOLICITUD_APORTERESCATES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarFechaGestionOperaciones_SelectFechas.sql");
	
	
	
	private final String ID_TIPO_ENTIDAD = "SOLICITUD-APORTE-RESCATE";
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarEjecutarFechaGestionOperaciones jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarFechaGestionOperaciones.class);
		SalidaConfirmarEjecutarFechaGestionOperaciones out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarEjecutarFechaGestionOperaciones logica(Connection con, EntradaConfirmarEjecutarFechaGestionOperaciones entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarFechaGestionOperaciones");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConfirmarEjecutarFechaGestionOperaciones out = new SalidaConfirmarEjecutarFechaGestionOperaciones();
		
		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_SOLICITUD_APORTE_RESCATE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<SolicitudAporteRescate> solicitudAporteRescate = obtenerEntidadesSolicitudAporteRescate(solicitudesCambios,con);
		List<SolicitudesAporteRescate> solicitudesAporteRescate = obtenerEntidadesSolicitudesAporteRescate(solicitudesCambios,con);
	
		
		modificarSolicitudAporteRescate(con, solicitudAporteRescate, entrada, solicitudesAporteRescate);
	
		out = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarFechaGestionOperaciones", finServicio - inicioServicio);
		return out;
	}
	
	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarEjecutarFechaGestionOperaciones entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarFechaGestionOperaciones.obtenerSolicitudesDeCambios");
		long inicio = System.currentTimeMillis();

		ObtenerSolicitudesDeCambiosFechaGestionOperacionesMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosFechaGestionOperacionesMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarFechaGestionOperaciones.obtenerSolicitudesDeCambios", fin - inicio);
		
		return solicitudesCambios;
	}
	
	private List<SolicitudAporteRescate> obtenerEntidadesSolicitudAporteRescate(List<SolicitudCambio> solicitudesCambios, Connection con) throws Exception {
		logger.info("INICIO::ConfirmarModificarFechaGestionOperaciones.obtenerEntidadesSolicitudAporteRescate");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<SolicitudAporteRescate> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> solicitudAporteRescate : solicitudesPorIdEntidad.entrySet()) {
			SolicitudAporteRescate solicitudAporteRescateModificar = new SolicitudAporteRescate();
			List<SolicitudCambio> cambiosSolicitudAporteRescate = solicitudAporteRescate.getValue();
			solicitudAporteRescateModificar.IdSolicitudAporteRescate = solicitudAporteRescate.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosSolicitudAporteRescate) {
				solicitudAporteRescateModificar = ValorAntiguo.asignarValorNuevo(solicitudAporteRescateModificar, solicitudCambio.CampoModificado, solicitudCambio.ValorNuevo);
			}
			datosAActualizar.add(solicitudAporteRescateModificar);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarFechaGestionOperaciones.obtenerEntidadesSolicitudAporteRescate", fin - inicio);
		return datosAActualizar;
	}
	
	private List<SolicitudesAporteRescate> obtenerEntidadesSolicitudesAporteRescate(List<SolicitudCambio> solicitudesCambios, Connection con) throws Exception {
		logger.info("INICIO::ConfirmarModificarFechaGestionOperaciones.obtenerEntidadesSolicitudesAporteRescate");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<SolicitudesAporteRescate> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> solicitudAporteRescate : solicitudesPorIdEntidad.entrySet()) {
			SolicitudesAporteRescate solicitudAporteRescateModificar = new SolicitudesAporteRescate();
			List<SolicitudCambio> cambiosSolicitudAporteRescate = solicitudAporteRescate.getValue();
			solicitudAporteRescateModificar.IdSolicitudAporteRescate = solicitudAporteRescate.getKey();
		
			
			for(SolicitudCambio solicitudCambio : cambiosSolicitudAporteRescate) {
				if(solicitudCambio.CampoModificado.equals("FechaAceptacion")) {
					solicitudAporteRescateModificar.FechaAceptacionAnterior = solicitudCambio.ValorAntiguo;
				}	
				
				if(solicitudCambio.CampoModificado.equals("FechaCertificacion")) {
					solicitudAporteRescateModificar.FechaCertificacionAnterior = solicitudCambio.ValorAntiguo;
				}
				if(solicitudCambio.CampoModificado.equals("FechaLiquidacion")) {
					solicitudAporteRescateModificar.FechaLiquidacionAnterior = solicitudCambio.ValorAntiguo;
				}
				if(solicitudCambio.CampoModificado.equals("FechaValorCuota")) {
					solicitudAporteRescateModificar.FechaValorCuotaAnterior = solicitudCambio.ValorAntiguo;
				}
				solicitudAporteRescateModificar = ValorAntiguo.asignarValorNuevo(solicitudAporteRescateModificar, solicitudCambio.CampoModificado, solicitudCambio.ValorNuevo);
			}
			datosAActualizar.add(solicitudAporteRescateModificar);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarFechaGestionOperaciones.obtenerEntidadesSolicitudesAporteRescate", fin - inicio);
		return datosAActualizar;
	}
	
	
	private void modificarSolicitudAporteRescate(Connection con, List<SolicitudAporteRescate> solicitudesAporteRescate, EntradaConfirmarEjecutarFechaGestionOperaciones entrada, List<SolicitudesAporteRescate> solicitudesAporteRescateAnteriores) throws Exception {
		logger.debug("INICIO::ConfirmarModificarFechaGestionOperaciones.modificarSolicitudAporteRescate");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();	
		solicitudesAporteRescate = setearFechasNull(solicitudesAporteRescate);

		if( solicitudesAporteRescate.size() > 0 ) {
			solicitudesAporteRescateAnteriores = obtenerSolicitudesAporteRescateAnteriores(con, solicitudesAporteRescateAnteriores, entrada);
			sql.batch(con, SQL_UPDATE_FECHAS, solicitudesAporteRescate, SolicitudAporteRescate.class);
			sql.batch(con, SQL_INSERT_HISTORIAL, solicitudesAporteRescateAnteriores, SolicitudesAporteRescate.class);
		}
		
		
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarFechaGestionOperaciones.modificarSolicitudAporteRescate", fin - inicio);
	}
	
	private List<SolicitudesAporteRescate> obtenerSolicitudesAporteRescateAnteriores(Connection con, List<SolicitudesAporteRescate> solicitudesAporteRescateAnteriores, EntradaConfirmarEjecutarFechaGestionOperaciones entrada) throws Exception {
		logger.debug("INICIO::ConfirmarModificarFechaGestionOperaciones.obtenerSolicitudesAporteRescateAnteriores");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> params = new HashMap<String,Object>();
		HashMap<String,Object> paramsFechasAnteriores = new HashMap<String,Object>();
		List<Long> idSolicitud = entrada.p_Entidad.parallelStream().map(sol -> {
			return sol.IdEntidad;
		}).collect(Collectors.toList());
		params.put("IdEvento", entrada.p_IdEvento);
		paramsFechasAnteriores.put("IdSolicitudAporteRescate", idSolicitud);

			SQL sql = new SQL();	
			List<SolicitudesAporteRescate> listUsuarioModificadorUsuarioAutorizador = sql.query(con, SQL_SELECT_HISTORIAL, params, SolicitudesAporteRescate.class);
			List<SolicitudesAporteRescate> listFechasAnteriores = sql.query(con, SQL_SELECT_SOLICITUD_APORTERESCATES, paramsFechasAnteriores, SolicitudesAporteRescate.class);
		
			
			for(SolicitudesAporteRescate listFechas : listFechasAnteriores) {
				for(SolicitudesAporteRescate listUsuarios : listUsuarioModificadorUsuarioAutorizador ) {
					listFechas.UsuarioAutorizador = entrada.p_IdUsuarioCambioEstado;
					listFechas.UsuarioModificador = listUsuarios.UsuarioModificador;
					
				}
			}
		
			for(SolicitudesAporteRescate solicitudesAnterioresFinal : solicitudesAporteRescateAnteriores) {
			
				for(SolicitudesAporteRescate solicitudesAnteriores : listFechasAnteriores) {
					if(solicitudesAnterioresFinal.FechaAceptacionAnterior == null || solicitudesAnterioresFinal.FechaAceptacionAnterior.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaAceptacionAnterior = solicitudesAnteriores.FechaAceptacion;
					}
					if(solicitudesAnterioresFinal.FechaCertificacionAnterior == null || solicitudesAnterioresFinal.FechaCertificacionAnterior.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaCertificacionAnterior = solicitudesAnteriores.FechaCertificacion;
					}
					if(solicitudesAnterioresFinal.FechaLiquidacionAnterior == null || solicitudesAnterioresFinal.FechaLiquidacionAnterior.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaLiquidacionAnterior = solicitudesAnteriores.FechaLiquidacion;
					}
					if(solicitudesAnterioresFinal.FechaValorCuotaAnterior == null || solicitudesAnterioresFinal.FechaValorCuotaAnterior.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaValorCuotaAnterior = solicitudesAnteriores.FechaValorCuota;
					}
					if(solicitudesAnterioresFinal.FechaAceptacion == null || solicitudesAnterioresFinal.FechaAceptacion.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaAceptacion= solicitudesAnteriores.FechaAceptacion;
					}
					if(solicitudesAnterioresFinal.FechaCertificacion == null || solicitudesAnterioresFinal.FechaCertificacion.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaCertificacion = solicitudesAnteriores.FechaCertificacion;
					}
					if(solicitudesAnterioresFinal.FechaLiquidacion == null || solicitudesAnterioresFinal.FechaLiquidacion.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaLiquidacion = solicitudesAnteriores.FechaLiquidacion;
					}
					if(solicitudesAnterioresFinal.FechaValorCuota == null || solicitudesAnterioresFinal.FechaValorCuota.equals("0001-01-01")) {
						solicitudesAnterioresFinal.FechaValorCuota = solicitudesAnteriores.FechaValorCuota;
					}
					
					solicitudesAnterioresFinal.UsuarioAutorizador = solicitudesAnteriores.UsuarioAutorizador;
					solicitudesAnterioresFinal.UsuarioModificador = solicitudesAnteriores.UsuarioModificador;
				}
			}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarFechaGestionOperaciones.obtenerSolicitudesAporteRescateAnteriores", fin - inicio);
		return solicitudesAporteRescateAnteriores;
	}

	public List<SolicitudAporteRescate> setearFechasNull(List<SolicitudAporteRescate> solicitudes) {
		logger.info("INICIO::ConfirmarModificarFechaGestionOperaciones.setearFechasNull");
		long inicio = System.currentTimeMillis();

		for(SolicitudAporteRescate solicitud : solicitudes) {
			solicitud.FechaAceptacion = String.valueOf(solicitud.FechaAceptacion).equals("0001-01-01") ? null : solicitud.FechaAceptacion;

			solicitud.FechaLiquidacion = String.valueOf(solicitud.FechaLiquidacion).equals("0001-01-01") ? null : solicitud.FechaLiquidacion;

			solicitud.FechaCertificacion = String.valueOf(solicitud.FechaCertificacion).equals("0001-01-01") ? null : solicitud.FechaCertificacion;

			solicitud.FechaValorCuota = String.valueOf(solicitud.FechaValorCuota).equals("0001-01-01") ? null : solicitud.FechaValorCuota;

		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarFechaGestionOperaciones.setearFechasNull", fin - inicio);
		return solicitudes;
		
	}
	
	private SalidaConfirmarEjecutarFechaGestionOperaciones modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarEjecutarFechaGestionOperaciones entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarFechaGestionOperaciones.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		SalidaConfirmarEjecutarFechaGestionOperaciones out = new SalidaConfirmarEjecutarFechaGestionOperaciones();
		ModificarEstadoRegistroSolicitudCambioMasivo modificarEstadoRegistroSolicitudCambioMasivo = new ModificarEstadoRegistroSolicitudCambioMasivo();
		ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado entradaModificarEstadoRegistroSolicitudCambioMasivo = new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		
		for (SolicitudCambio sc : solicitudesCambios) {
			ModificarEstadoRegistroSolicitudCambioMasivo.SolicitudCambio solicitud = new ModificarEstadoRegistroSolicitudCambioMasivo.SolicitudCambio();
			solicitud.IdSolicitudCambio = sc.IdSolicitudCambio;
			entradaModificarEstadoRegistroSolicitudCambioMasivo.p_SolicitudCambio.add(solicitud);
		}
		
		ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado salidaModificarEstadoRegistroSolicitudCambioMasivo = modificarEstadoRegistroSolicitudCambioMasivo.logica(con, entradaModificarEstadoRegistroSolicitudCambioMasivo);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambioMasivo.p_out_ErroresContador;
		
		for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaModificarEstadoRegistroSolicitudCambioMasivo.p_out_ErroresDetalle) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarFechaGestionOperaciones.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return out;
    }

}


