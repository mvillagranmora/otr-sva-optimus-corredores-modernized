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

public class ConciliacionFFMMCuentaInversion implements BcsConciliacion {
	private static FileLoader loader = new FileLoader();
	
	private static String SQL_CANTIDAD_MOVIMIENTOS_NO_DIFERIDOS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCuentaInversion_CantidadMovimientosNoDiferidos.sql");
	private static String SQL_CANTIDAD_MOVIMIENTOS_DETALLE_NO_DIFERERIDOS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCuentaInversion_CantidadMovimientosDetalleNoDiferidos.sql");
	
	private static String SQL_CANTIDAD_MOVIMIENTOS_DIFERIDOS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCuentaInversion_CantidadMovimientosDiferidos.sql");
	private static String SQL_CANTIDAD_MOVIMIENTOS_DIFERIDOS_DETALLE = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCuentaInversion_CantidadMovimientosDiferidosDetalle.sql");
	
	private static String SQL_MONTOS_MOVIMIENTOS_NO_DIFERIDOS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCuentaInversion_MontosMovimientosNoDiferidos.sql");
	private static String SQL_MONTOS_MOVIMIENTOS_DIFERIDOS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCuentaInversion_MontosMovimientosDiferidos.sql");
	
	public static class CantidadMovimientos extends BcsPojo {
		public Long IdSolicitudAporteRescate;
		public Long CantidadCargoAbono;
	}
	
	public static class CantidadMovimientosDetalle extends BcsPojo {
		public Long IdSolicitudAporteRescate;
		public Long IdCargoAbonoMovimiento;
		
	}
	
	public static class Monto extends BcsPojo{
		public Long IdSolicitudAporteRescate;
		public BigDecimal MontoMonedaFondo;
		public BigDecimal MontoMonedaSolicitud;
		public BigDecimal MontoCuentaInversion;
		public String IdMoneda;
		public String IdMonedaFondo;
	}
	
	@Override
	public SalidaConciliacion conciliar(Connection con) throws SQLException {
		
		SalidaConciliacion salidaConciliacion = new SalidaConciliacion();
		
		salidaConciliacion.nombreConciliacion = "CONCILIACION SOLICITUDES FONDOS MUTUOS-CUENTA INVERSION";
		DetalleConciliacion detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos = obtenerConciliacionCantidadSolicitudesCargoAbonoNoDiferidos(con);
		DetalleConciliacion detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos = obtenerConciliacionCantidadSolicitudesCargoAbonoDiferidos(con);
		DetalleConciliacion detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos = obtenerConcilacionMontoSolicitudesCargoAbonoNoDiferidos(con);
		DetalleConciliacion detalleConciliacionMontoSolicitudesCargoAbonoDiferidos = obtenerConcilacionMontoSolicitudesCargoAbonoDiferidos(con);
		
		salidaConciliacion.detalleConciliacion.add(detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos);
		salidaConciliacion.detalleConciliacion.add(detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos);
		salidaConciliacion.detalleConciliacion.add(detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos);
		salidaConciliacion.detalleConciliacion.add(detalleConciliacionMontoSolicitudesCargoAbonoDiferidos);
		
		salidaConciliacion.estadoConciliacion = obtenerEstadoConciliacion(salidaConciliacion);
		
		return salidaConciliacion;
	}
	
	private DetalleConciliacion obtenerConciliacionCantidadSolicitudesCargoAbonoNoDiferidos(Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		DetalleConciliacion detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos = new DetalleConciliacion();
		detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.nombreDetalle = "Conciliacion de cantidad de cargos abonos a partir de solicitudes no diferidas (1 movimiento por solicitud)";
		List<CantidadMovimientos> cabeceraConciliacionCantidadSolicitudesCargoAbonoNoDiferidos = sql.query(con, SQL_CANTIDAD_MOVIMIENTOS_NO_DIFERIDOS, params, CantidadMovimientos.class);
		
		if (cabeceraConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.isEmpty()) {
			detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
			detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
		}
		else {
			detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
			detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.mensajeDetalle = cabeceraConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.size() + " solicitudes descuadrados";
			List<CantidadMovimientosDetalle> detalleMovimientos = sql.query(con, SQL_CANTIDAD_MOVIMIENTOS_DETALLE_NO_DIFERERIDOS, params, CantidadMovimientosDetalle.class);
			detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos.detalleMovimientos.addAll(detalleMovimientos);
		}
		
		return detalleConciliacionCantidadSolicitudesCargoAbonoNoDiferidos;
	}
	
	private DetalleConciliacion obtenerConciliacionCantidadSolicitudesCargoAbonoDiferidos(Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		DetalleConciliacion detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos = new DetalleConciliacion();
		detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos.nombreDetalle = "Conciliacion de cantidad de cargos abonos a partir de solicitudes diferidas (2 movimiento por solicitud)";
		List<CantidadMovimientos> cabeceraConciliacionCantidadSolicitudesCargoAbonoDiferidos = sql.query(con, SQL_CANTIDAD_MOVIMIENTOS_DIFERIDOS, params, CantidadMovimientos.class);
		
		if (cabeceraConciliacionCantidadSolicitudesCargoAbonoDiferidos.isEmpty()) {
			detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
			detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
		}
		else {
			detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
			detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos.mensajeDetalle = cabeceraConciliacionCantidadSolicitudesCargoAbonoDiferidos.size() + " solicitudes descuadrados";
			List<CantidadMovimientosDetalle> detalleMovimientos = sql.query(con, SQL_CANTIDAD_MOVIMIENTOS_DIFERIDOS_DETALLE, params, CantidadMovimientosDetalle.class);
			detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos.detalleMovimientos.addAll(detalleMovimientos);
		}
		
		return detalleConciliacionCantidadSolicitudesCargoAbonoDiferidos;
	}
	
	private DetalleConciliacion obtenerConcilacionMontoSolicitudesCargoAbonoNoDiferidos(Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		DetalleConciliacion detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos = new DetalleConciliacion();
		detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos.nombreDetalle = "Conciliacion de Montos de Cargos/Abonos vs Solicitudes No Diferidos";
		List<Monto> cabeceraConciliacionMontoSolicitudesCargoAbonoNoDiferidos = sql.query(con, SQL_MONTOS_MOVIMIENTOS_NO_DIFERIDOS, params, Monto.class);
		
		if (cabeceraConciliacionMontoSolicitudesCargoAbonoNoDiferidos.isEmpty()) {
			detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
			detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
		}
		else {
			detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
			detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos.mensajeDetalle = cabeceraConciliacionMontoSolicitudesCargoAbonoNoDiferidos.size() + " solicitudes descuadrados";
			detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos.detalleMovimientos.addAll(cabeceraConciliacionMontoSolicitudesCargoAbonoNoDiferidos);
		}
		
		return detalleConciliacionMontoSolicitudesCargoAbonoNoDiferidos;
	}
	
	private DetalleConciliacion obtenerConcilacionMontoSolicitudesCargoAbonoDiferidos(Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		DetalleConciliacion detalleConciliacionMontoSolicitudesCargoAbonoDiferidos = new DetalleConciliacion();
		detalleConciliacionMontoSolicitudesCargoAbonoDiferidos.nombreDetalle = "Conciliacion de Montos de Cargos/Abonos vs Solicitudes No Diferidos";
		List<Monto> cabeceraConciliacionMontoSolicitudesCargoAbonoDiferidos = sql.query(con, SQL_MONTOS_MOVIMIENTOS_DIFERIDOS, params, Monto.class);
		
		if (cabeceraConciliacionMontoSolicitudesCargoAbonoDiferidos.isEmpty()) {
			detalleConciliacionMontoSolicitudesCargoAbonoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
			detalleConciliacionMontoSolicitudesCargoAbonoDiferidos.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
		}
		else {
			detalleConciliacionMontoSolicitudesCargoAbonoDiferidos.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
			detalleConciliacionMontoSolicitudesCargoAbonoDiferidos.mensajeDetalle = cabeceraConciliacionMontoSolicitudesCargoAbonoDiferidos.size() + " solicitudes descuadrados";
			detalleConciliacionMontoSolicitudesCargoAbonoDiferidos.detalleMovimientos.addAll(cabeceraConciliacionMontoSolicitudesCargoAbonoDiferidos);
		}
		
		return detalleConciliacionMontoSolicitudesCargoAbonoDiferidos;
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
			ConciliacionFFMMCuentaInversion conciliacion = new ConciliacionFFMMCuentaInversion();
			System.out.println(conciliacion.conciliar(conn));
	    }
		 	
	}
	

}
