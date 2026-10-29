package cl.bcs.plataforma.ccl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

public class ConciliacionFFMMMovimientoSpot implements BcsConciliacion {
	
	private static FileLoader loader = new FileLoader();
	
	private static String SQL_CANTIDAD_MOVIMIENTOS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMMovimientoSpot_CantidadMovimientos.sql");
	private static String SQL_CANTIDAD_MOVIMIENTOS_DETALLE = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMMovimientoSpot_CantidadMovimientosDetalle.sql");
	private static String SQL_MONTOS_MOVIMIENTOS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMMovimientoSpot_MontosMovimientos.sql");
	
	
	public static class CantidadMovimientos extends BcsPojo {
		public Long IdSolicitudAporteRescate;
		public Long CantidadOperacionSpot;
	}
	
	public static class CantidadMovimientosDetalle extends BcsPojo {
		public Long IdSolicitudAporteRescate;
		public Long IdOperacionSpot;
	}
	
	public static class Monto extends BcsPojo{
		public Long IdSolicitudAporteRescate;
		public String IdTipoOperacionSpot;
		public BigDecimal MontoSolicitud;
		public BigDecimal MontoMonedaPrincipalSpot;
		public BigDecimal MontoMonedaSecundariaSpot;
		public String IdMonedaSolicitud;
		public String IdMonedaPago;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
	}

	@Override
	public SalidaConciliacion conciliar(Connection con) throws SQLException {
		SalidaConciliacion salidaConciliacion = new SalidaConciliacion();
		salidaConciliacion.nombreConciliacion = "CONCILIACION SOLICITUDES FONDOS MUTUOS-MOVIMIENTO SPOT";
		
		DetalleConciliacion detalleConciliacionCantidadSolicitudesMovimientoSpot = obtenerConciliacionCantidadSolicitudesMovimientoSpot(con);
		DetalleConciliacion detalleConciliacionMontoSolicitudMovimientoSpot = obtenerConciliacionMontoSolicitudMovimientoSpot(con);
		
		salidaConciliacion.detalleConciliacion.add(detalleConciliacionCantidadSolicitudesMovimientoSpot);
		salidaConciliacion.detalleConciliacion.add(detalleConciliacionMontoSolicitudMovimientoSpot);
		
		salidaConciliacion.estadoConciliacion = obtenerEstadoConciliacion(salidaConciliacion);
		
		return salidaConciliacion;
	}
	
	private DetalleConciliacion obtenerConciliacionCantidadSolicitudesMovimientoSpot(Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		DetalleConciliacion detalleConciliacionCantidadSolicitudesMovimientoSpot = new DetalleConciliacion();
		detalleConciliacionCantidadSolicitudesMovimientoSpot.nombreDetalle = "Conciliacion de cantidad de movimientos spot a partir de solicitudes con moneda pago distinta a moneda solicitud (1 movimiento por solicitud)";
		List<CantidadMovimientos> cabeceraConciliacionCantidadSolicitudesMovimientoSpot = sql.query(con, SQL_CANTIDAD_MOVIMIENTOS, params, CantidadMovimientos.class);
		
		if (cabeceraConciliacionCantidadSolicitudesMovimientoSpot.isEmpty()) {
			detalleConciliacionCantidadSolicitudesMovimientoSpot.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
			detalleConciliacionCantidadSolicitudesMovimientoSpot.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
		}
		else {
			detalleConciliacionCantidadSolicitudesMovimientoSpot.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
			detalleConciliacionCantidadSolicitudesMovimientoSpot.mensajeDetalle = cabeceraConciliacionCantidadSolicitudesMovimientoSpot.size() + " solicitudes descuadrados";
			List<CantidadMovimientosDetalle> detalleMovimientos = sql.query(con, SQL_CANTIDAD_MOVIMIENTOS_DETALLE, params, CantidadMovimientosDetalle.class);
			detalleConciliacionCantidadSolicitudesMovimientoSpot.detalleMovimientos.addAll(detalleMovimientos);
		}
		
		return detalleConciliacionCantidadSolicitudesMovimientoSpot;
	}
	
	private DetalleConciliacion obtenerConciliacionMontoSolicitudMovimientoSpot(Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		DetalleConciliacion detalleConciliacionMontoSolicitudesMovimientoSpot = new DetalleConciliacion();
		detalleConciliacionMontoSolicitudesMovimientoSpot.nombreDetalle = "Conciliacion de Montos de Movimientos Spot vs Solicitudes con moneda pago distinta a moneda solicitud";
		List<Monto> cabeceraConciliacionMontoSolicitudesMovimientoSpot = sql.query(con, SQL_MONTOS_MOVIMIENTOS, params, Monto.class);
		
		if (cabeceraConciliacionMontoSolicitudesMovimientoSpot.isEmpty()) {
			detalleConciliacionMontoSolicitudesMovimientoSpot.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
			detalleConciliacionMontoSolicitudesMovimientoSpot.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
		}
		else {
			detalleConciliacionMontoSolicitudesMovimientoSpot.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
			detalleConciliacionMontoSolicitudesMovimientoSpot.mensajeDetalle = cabeceraConciliacionMontoSolicitudesMovimientoSpot.size() + " solicitudes descuadrados";
			detalleConciliacionMontoSolicitudesMovimientoSpot.detalleMovimientos.addAll(cabeceraConciliacionMontoSolicitudesMovimientoSpot);
		}
		
		return detalleConciliacionMontoSolicitudesMovimientoSpot;
	}
	
	private String obtenerEstadoConciliacion(SalidaConciliacion conciliaciones) {
		Boolean tieneErrores = conciliaciones.detalleConciliacion.parallelStream().filter(conciliacion->ConstantesConciliacion.ESTADO_NOOK.equals(conciliacion.estadoDetalle)).collect(Collectors.toList()).size() > 0;
		if(tieneErrores) {
			return ConstantesConciliacion.ESTADO_NOOK;
		}else {
			return ConstantesConciliacion.ESTADO_OK;
		}
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getBolsaConnection()) {
			ConciliacionFFMMMovimientoSpot conciliacion = new ConciliacionFFMMMovimientoSpot();
			System.out.println(conciliacion.conciliar(conn));
	    }
	}

}
