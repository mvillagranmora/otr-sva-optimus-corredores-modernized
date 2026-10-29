package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaOrdenDirecta implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaOrdenDirecta");
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaOrdenDirecta_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaOrdenDirecta_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaOrdenDirecta_SelectDatosMovimiento.sql");
	
	public static class TabDatosMovimientoPaso{
		public Long 	IdMovimiento;
		public String   TipoDocumento;
		public String 	FolioTransaccion;
		public String 	CompraVenta;
		public String 	CodigoBolsa;
		public String 	TipoInstrumento;
		public String 	EsCuentaPropia;
		public String 	FamiliaInstrumento;
		public Date   	FechaLiquidacion;
		public String 	Nemo;
		public String 	Moneda;
		public String 	Producto;
		public String 	Estado;

		
		public static class DatoEmpleado {
			public Long IdEmpleado;
		}
		
		public static class RestringeCartera {
			public String RestringeCarteraCliente;
		}
		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaOrdenDirecta");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();

		String idMonedaSistema = infoAdicional.p_IdMonedaSistema;
		
		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);		
		out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		params.clear();
		
		//EVENTOS
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("MonedaSistema", idMonedaSistema);
		out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		params.clear();
		
		//MOVIMIENTO DATOS
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
		params.clear();
		
		for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){

			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoDocumento"           , mov.TipoDocumento		  				, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FolioTransaccion"        , mov.FolioTransaccion	, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CompraVenta"				, mov.CompraVenta						, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoBolsa"				, mov.CodigoBolsa						, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento"			, mov.TipoInstrumento					, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsCuentaPropia"          , mov.EsCuentaPropia					, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FamiliaInstrumento"      , mov.FamiliaInstrumento				, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion"		, Fecha.stringToSqlDate(mov.FechaLiquidacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo"                    , mov.Nemo		            			, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda"                  , mov.Moneda		        		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto"                , mov.Producto		        		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Estado"                	, mov.Estado		        		, BigDecimal.ZERO));
			
		}
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		log.write("FIN::CentralizaOrdenDirecta");
		return out;
	}
	
}
