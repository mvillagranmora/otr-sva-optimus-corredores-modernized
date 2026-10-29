package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaSpotIngreso implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaSpotIngreso");
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotIngreso_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotIngreso_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotIngreso_SelectDatosMovimiento.sql");
	
	public static class TabDatosMovimientoPaso{
		public Long   IdMovimiento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String Portafolio;
		public String Moneda;
		public String Producto;
		public String ClaseInstrumento;
		public String TipoInstrumento;
		public String Nemo;
		public String Boveda;
		public Long IdCartera;
		public String Emisor;
		public String TipoReajuste;
		public String CodigoIFRS;
		public String Agente;
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String Canal;
		public String Relacion;
		public String Bolsa;
		public String CentroCostoAgente;
		public String TipoComprobante;
		public String CompraVenta;
		public String TipoPersona;
		public String FechaOperacion;
		public String ClienteBancario;
		public String OperacionNoFacturable;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaSpotIngreso");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		
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
		out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		params.clear();
		
		//MOVIMIENTO DATOS
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
		params.clear();
		
		for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){
	
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", mov.Boveda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdCartera", "", BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoReajuste", mov.TipoReajuste, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Canal", mov.Canal, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa", mov.Bolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoComprobante", mov.TipoComprobante, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CompraVenta", mov.CompraVenta, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona", mov.TipoPersona, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", mov.FechaOperacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClienteBancario", mov.ClienteBancario, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "OperacionNoFacturable", mov.OperacionNoFacturable, BigDecimal.ZERO));
		}
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		log.write("FIN::CentralizaSpotIngreso");
		return out;
	}
	
}
