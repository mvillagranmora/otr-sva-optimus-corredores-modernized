package cl.bcs.ordenes;

import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfirmarRechazarAutorizacionAsignacion implements JavaCall, BcsAutorizaciones {
	static final Logger logger = LogManager.getLogger(ConfirmarRechazarAutorizacionAsignacion.class);
	
	private final String SQL_SELECT_DETALLE_EVENTO = loadResourceToString("cl/bcs/ordenes/sql/ConfirmarRechazarAutorizacionAsignacion_SelectDetalleEvento.sql");
	private final String SQL_SELECT_ESTADO_ASIGNACION = loadResourceToString("cl/bcs/ordenes/sql/ConfirmarRechazarAutorizacionAsignacion_SelectEstadoAsignacion.sql");
	private final String SQL_UPDATE_ESTADO_ASIGNACION = loadResourceToString("cl/bcs/ordenes/sql/ConfirmarRechazarAutorizacionAsignacion_UpdateEstadoAsignacion.sql");
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	public static class DetalleEvento extends BcsPojo {
		public Long IdEventoDetalle;
		public Long IdEvento;
		public String NombreCampo;
		public String Valor;
	}
	
	public static class Asignacion extends BcsPojo {
		public Long IdAsignacion;
		public String IdEstadoAsignacion;
		public String IdEstadoAsignacionPreAutorizacion;
		public String IdEstadoAsignacionPostAutorizacion;
	}
	
	private enum Accion {
		AUTORIZAR("NEF"),
		RECHAZAR("ANU");
		
		public final String IdEstado;
		
		Accion(String idEstado) {
			IdEstado = idEstado;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		return logica(conn, entrada, Accion.AUTORIZAR);
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		return logica(conn, entrada, Accion.RECHAZAR);
	}
	
	private SalidaAutorizacion logica(Connection conn, EntradaAutorizacion entrada, Accion accion) {
		logger.info("INICIO::ConfirmarRechazarAutorizacionAsignacion");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();
		
		try {
			List<Long> eventoIds = entrada.p_Lista.stream().map(u -> u.IdEvento).filter(Objects::nonNull).distinct().collect(Collectors.toList());
			List<DetalleEvento> detalles = obtenerDetallesEventos(conn, eventoIds);
			List<Asignacion> asignaciones = obtenerAsignaciones(conn, accion, detalles);
			validarAsignaciones(conn, entrada.p_IdEmpresa, asignaciones, salidaAutorizacion);
			if (salidaAutorizacion.P_OUT_ERRORESCONTADOR > 0) {
				return salidaAutorizacion;
			}
			
			actualizarAsignaciones(conn, asignaciones);
			if (Accion.RECHAZAR.equals(accion)) {
				anularAsignaciones(conn, entrada, asignaciones, salidaAutorizacion);
				if (salidaAutorizacion.P_OUT_ERRORESCONTADOR > 0) {
					return salidaAutorizacion;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			salidaAutorizacion.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarRechazarAutorizacionAsignacion {}", fin - inicio);
		return salidaAutorizacion;
	}
	
	private void validarAsignaciones(Connection conn, Long idEmpresa, List<Asignacion> asignaciones, SalidaAutorizacion salida) throws Exception {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionAsignacion.validarAsignaciones");
		long inicio = System.currentTimeMillis();
		
		for (Asignacion asignacion : asignaciones) {
			if (_c.ESTADO_ASIGNACION_PENDIENTE_AUTORIZACION.equals(asignacion.IdEstadoAsignacionPreAutorizacion) == false) {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(asignacion.IdAsignacion, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_ASIGNACION_NO_PDA", new HashMap<>(), conn, idEmpresa));
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionAsignacion.validarAsignaciones {}", fin - inicio);
	}
	
	private List<DetalleEvento> obtenerDetallesEventos(Connection conn, List<Long> eventoIds) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionAsignacion.obtenerDetallesEventos");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("EventoIds", eventoIds);
		
		List<DetalleEvento> detalles = new SQL().query(conn, SQL_SELECT_DETALLE_EVENTO, params, DetalleEvento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionAsignacion.obtenerDetallesEventos {}", fin - inicio);
		
		return detalles;
	}
	
	private List<Asignacion> obtenerAsignaciones(Connection conn, Accion accion, List<DetalleEvento> detalles) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionAsignacion.obtenerAsignaciones");
		long inicio = System.currentTimeMillis();
		
		List<Long> asignacionIds = detalles.stream().filter(u -> Objects.nonNull(u.Valor)).map(u -> Long.valueOf(u.Valor)).distinct().collect(Collectors.toList());
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("AsignacionIds", asignacionIds);
		
		List<Asignacion> asignaciones = new SQL().query(conn, SQL_SELECT_ESTADO_ASIGNACION, params, Asignacion.class);
		for (Asignacion asignacion : asignaciones) {
			asignacion.IdEstadoAsignacionPostAutorizacion = accion.IdEstado;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionAsignacion.obtenerAsignaciones {}", fin - inicio);
		
		return asignaciones;
	}
	
	private void actualizarAsignaciones(Connection conn, List<Asignacion> asignaciones) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionAsignacion.actualizarAsignaciones");
		long inicio = System.currentTimeMillis();
		
		new SQL().batch(conn, SQL_UPDATE_ESTADO_ASIGNACION, asignaciones, Asignacion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionAsignacion.actualizarAsignaciones {}", fin - inicio);
	}
	
	private void anularAsignaciones(Connection conn, EntradaAutorizacion entrada, List<Asignacion> asignaciones, SalidaAutorizacion salida) throws Exception {
		logger.trace("INICIO::ConfirmarRechazarAutorizacionAsignacion.anularAsignaciones");
		long inicio = System.currentTimeMillis();
		
		AnularAsignaciones.AnularAsignaciones_Entrada entradaAnularAsignaciones = new AnularAsignaciones.AnularAsignaciones_Entrada();
		entradaAnularAsignaciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAnularAsignaciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaAnularAsignaciones.p_IdModulo = entrada.p_IdModulo;
		entradaAnularAsignaciones.asignacionesParaAnular = asignaciones.stream().map(asignacion -> {
			AnularAsignaciones.Asignacion_Entrada asignacionEntrada = new AnularAsignaciones.Asignacion_Entrada();
			asignacionEntrada.IdAsignacion = asignacion.IdAsignacion;
			return asignacionEntrada;
		}).collect(Collectors.toList());
		
		AnularAsignaciones.AnularAsignaciones_Salida salidaAnularAsignaciones = new AnularAsignaciones().logica(conn, entradaAnularAsignaciones);
		if (salidaAnularAsignaciones.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_ERRORESCONTADOR += salidaAnularAsignaciones.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll(salidaAnularAsignaciones.p_out_Mensajes);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarRechazarAutorizacionAsignacion.anularAsignaciones {}", fin - inicio);
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Lista\":[{\"IdEvento\":261735}]}";
			EntradaAutorizacion entradaAutorizacion = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
			System.out.println("ENTRADA");
			System.out.println(entradaAutorizacion);
			
			try {
				SalidaAutorizacion salidaAutorizacion = new ConfirmarRechazarAutorizacionAsignacion().rechazarEvento(conn, entradaAutorizacion);
				System.out.println("SALIDA");
				System.out.println(salidaAutorizacion);
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}
