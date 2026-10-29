package cl.bcs.plataforma.ccl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConcilicacionFFMMCustodia implements BcsConciliacion {

	private static FileLoader loader = new FileLoader();
	private static String SQL_CUADRATURA_CUOTAS_APORTES_CARTERAS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCustodia_CuadraturaCuotasAportesCarteras.sql");
	private static String SQL_CUADRATURA_CUOTAS_RESCATES_CARTERAS = loader.loadResourceToString("cl/bcs/plataforma/ccl/sql/ConciliacionFFMMCustodia_CuadraturaCuotasRescatesCarteras.sql");
	
	
	
	public static class ObjSolicitudCartera extends BcsPojo {
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public String IdSerieFondo;
		public BigDecimal NumeroCuotasFFMM;
		public BigDecimal NumeroCuotasCustodia;
	}
	
	
	
	
	@Override
	public SalidaConciliacion conciliar(Connection conn) throws SQLException {
		
		SalidaConciliacion salida = new SalidaConciliacion();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		String estadoConciliacion = ConstantesConciliacion.ESTADO_OK;
		
		//conciliacion de cantidad de cuotas en aportes y en carteras
		{
			DetalleConciliacion r1 = new DetalleConciliacion();
			salida.detalleConciliacion.add(r1);
			r1.nombreDetalle = "Conciliacion de cantidad de cuotas en aportes vs cuotas ingresadas en cartera";
			List<ObjSolicitudCartera> cab1 = sql.query(conn, SQL_CUADRATURA_CUOTAS_APORTES_CARTERAS, params, ObjSolicitudCartera.class);
			if (cab1.isEmpty()) {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
				r1.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
			}
			else {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
				estadoConciliacion = ConstantesConciliacion.ESTADO_NOOK;
				r1.mensajeDetalle = cab1.size() + " solicitudes descuadradas";
				
				
			}
		}
		
		//conciliacion de cantidad de cuotas en rescates y en salidas de carteras
		{
			DetalleConciliacion r1 = new DetalleConciliacion();
			salida.detalleConciliacion.add(r1);
			r1.nombreDetalle = "Conciliacion de cantidad de cuotas en rescates vs cuotas rescatadas en cartera";
			List<ObjSolicitudCartera> cab1 = sql.query(conn, SQL_CUADRATURA_CUOTAS_RESCATES_CARTERAS, params, ObjSolicitudCartera.class);
			if (cab1.isEmpty()) {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_OK;
				r1.mensajeDetalle = ConstantesConciliacion.ESTADO_OK;
			}
			else {
				r1.estadoDetalle = ConstantesConciliacion.ESTADO_NOOK;
				estadoConciliacion = ConstantesConciliacion.ESTADO_NOOK;
				r1.mensajeDetalle = cab1.size() + " solicitudes descuadradas";
				
				
			}
		}
		
		
		salida.estadoConciliacion = estadoConciliacion;
		salida.nombreConciliacion = "CONCILIACION FFMM-CUSTODIA";
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getSURAConnection()) {
			ConcilicacionFFMMCustodia c = new ConcilicacionFFMMCustodia();
			c.conciliar(conn);
	    }
		 	
	}

}
