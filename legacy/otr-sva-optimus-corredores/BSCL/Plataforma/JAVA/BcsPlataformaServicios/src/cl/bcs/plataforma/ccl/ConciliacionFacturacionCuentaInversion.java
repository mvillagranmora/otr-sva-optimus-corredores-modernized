package cl.bcs.plataforma.ccl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

public class ConciliacionFacturacionCuentaInversion implements BcsConciliacion {

	private static FileLoader loader = new FileLoader();
	private static String SQL_CANTIDAD_MOVIMIENTOS_NO_SPOT_CABECERA = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFacturacionCuentaInversion_CantidadMovimientosNoSpot.sql");
	private static String SQL_CANTIDAD_MOVIMIENTOS_NO_SPOT_DETALLE = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFacturacionCuentaInversion_CantidadMovimientosNoSpotDetalle.sql");
	
	private static String SQL_CANTIDAD_MOVIMIENTOS_SPOT_CABECERA = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFacturacionCuentaInversion_CantidadMovimientosSpot.sql");
	private static String SQL_CANTIDAD_MOVIMIENTOS_SPOT_DETALLE = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFacturacionCuentaInversion_CantidadMovimientosSpotDetalle.sql");
	
	private static String SQL_MONTO_MOVIMIENTOS_COMPRA_SPOT = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFacturacionCuentaInversion_MontoMovimientosCompraSpot.sql");
	private static String SQL_MONTO_MOVIMIENTOS_VENTA_SPOT = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFacturacionCuentaInversion_MontoMovimientosVentaSpot.sql");
	private static String SQL_MONTO_MOVIMIENTOS_NO_SPOT= loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFacturacionCuentaInversion_MontoMovimientosNoSpot.sql");
	
	
	public static class ObjNoSpotCabecera extends BcsPojo {
		public Long IdComprobante;
		public Long CantidadCargoAbono;
	}
	
	public static class ObjNoSpotDetalle extends BcsPojo {
		public Long IdComprobante;
		public Long IdCargoAbonoMovimiento;
		
	}
	
	public static class ObjMonto extends BcsPojo {
		public Long IdComprobante;
		public BigDecimal Monto;
		public BigDecimal MontoCobro;
		public BigDecimal MontoComprobante;
		public String IdMonedaComprobante;
		public Long IdCargoAbonoMovimiento;
		public String IdTipoMovimiento;
		public BigDecimal MontoCargoAbono;
		public String IdMonedaCuentaInversion;
	}
	
	
	@Override
	public SalidaConciliacion conciliar(Connection conn) throws SQLException {
		
		SalidaConciliacion salida = new SalidaConciliacion();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		String estadoConciliacion = ConstantesConciliacion.ESTADO_OK;
		
		//conciliacion de cantidad de cargos abonos a partir de facturas no spot
		{
			DetalleConciliacion r1 = new DetalleConciliacion();
			salida.detalleConciliacion.add(r1);
			r1.nombreDetalle = "Conciliacion de cantidad de cargos abonos a partir de comprobantes de facturacion no spot (1 movimiento por comprobante)";
			List<ObjNoSpotCabecera> cab1 = sql.query(conn, SQL_CANTIDAD_MOVIMIENTOS_NO_SPOT_CABECERA, params, ObjNoSpotCabecera.class);
			if (cab1.isEmpty()) {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
				r1.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
			}
			else {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
				estadoConciliacion = ConstantesConciliacion.ESTADO_NOOK;
				r1.mensajeDetalle = cab1.size() + " comprobantes descuadrados";
				List<ObjNoSpotDetalle> det1 = sql.query(conn, SQL_CANTIDAD_MOVIMIENTOS_NO_SPOT_DETALLE, params, ObjNoSpotDetalle.class);
				for (ObjNoSpotDetalle obj : det1) {
					r1.detalleMovimientos.add(obj);
				}
			}
		}
		
		
		//conciliacion de cantidad de cargos abonos a partir de facturas spot
		{
			DetalleConciliacion r1 = new DetalleConciliacion();
			salida.detalleConciliacion.add(r1);
			r1.nombreDetalle = "Conciliacion de cantidad de cargos abonos a partir de comprobantes de facturacion spot (2 movimientos por comprobante)";
			List<ObjNoSpotCabecera> cab1 = sql.query(conn, SQL_CANTIDAD_MOVIMIENTOS_SPOT_CABECERA, params, ObjNoSpotCabecera.class);
			if (cab1.isEmpty()) {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
				r1.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
			}
			else {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
				estadoConciliacion = ConstantesConciliacion.ESTADO_NOOK;
				r1.mensajeDetalle = cab1.size() + " comprobantes descuadrados";
				List<ObjNoSpotDetalle> det1 = sql.query(conn, SQL_CANTIDAD_MOVIMIENTOS_SPOT_DETALLE, params, ObjNoSpotDetalle.class);
				for (ObjNoSpotDetalle obj : det1) {
					r1.detalleMovimientos.add(obj);
				}
			}
		}
		
		
		//conciliacion de monto cargoabono vs comprobantes no spot
		{
			DetalleConciliacion r1 = new DetalleConciliacion();
			salida.detalleConciliacion.add(r1);
			r1.nombreDetalle = "Conciliacion de Montos de Cargos/Abonos vs Comprobantes de Facturacion No Spot";
			List<ObjMonto> cab1 = sql.query(conn, SQL_MONTO_MOVIMIENTOS_NO_SPOT, params, ObjMonto.class);
			if (cab1.isEmpty()) {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
				r1.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
			}
			else {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
				estadoConciliacion = ConstantesConciliacion.ESTADO_NOOK;
				r1.mensajeDetalle = cab1.size() + " comprobantes descuadrados";
				for (ObjMonto obj : cab1) {
					r1.detalleMovimientos.add(obj);
				}
			}
		}
		
		//conciliacion de monto cargoabono vs comprobantes compra spot
		{
			DetalleConciliacion r1 = new DetalleConciliacion();
			salida.detalleConciliacion.add(r1);
			r1.nombreDetalle = "Conciliacion de Montos de Cargos/Abonos vs Comprobantes de Facturacion Compra Spot";
			List<ObjMonto> cab1 = sql.query(conn, SQL_MONTO_MOVIMIENTOS_COMPRA_SPOT, params, ObjMonto.class);
			if (cab1.isEmpty()) {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
				r1.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
			}
			else {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
				estadoConciliacion = ConstantesConciliacion.ESTADO_NOOK;
				r1.mensajeDetalle = cab1.size() + " comprobantes descuadrados";
				for (ObjMonto obj : cab1) {
					r1.detalleMovimientos.add(obj);
				}
			}
		}
		
		//conciliacion de monto cargoabono vs comprobantes venta spot
		{
			DetalleConciliacion r1 = new DetalleConciliacion();
			salida.detalleConciliacion.add(r1);
			r1.nombreDetalle = "Conciliacion de Montos de Cargos/Abonos vs Comprobantes de Facturacion Venta Spot";
			List<ObjMonto> cab1 = sql.query(conn, SQL_MONTO_MOVIMIENTOS_VENTA_SPOT, params, ObjMonto.class);
			if (cab1.isEmpty()) {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
				r1.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
			}
			else {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
				estadoConciliacion = ConstantesConciliacion.ESTADO_NOOK;
				r1.mensajeDetalle = cab1.size() + " comprobantes descuadrados";
				for (ObjMonto obj : cab1) {
					r1.detalleMovimientos.add(obj);
				}
			}
		}
		
		salida.estadoConciliacion = estadoConciliacion;
		salida.nombreConciliacion = "CONCILIACION FACTURACION-CUENTA INVERSION";
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getSURAConnection()) {
			ConciliacionFacturacionCuentaInversion c = new ConciliacionFacturacionCuentaInversion();
			c.conciliar(conn);
	    }
		 	
	}

}
