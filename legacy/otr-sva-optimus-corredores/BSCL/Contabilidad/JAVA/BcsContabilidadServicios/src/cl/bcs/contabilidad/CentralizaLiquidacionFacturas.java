package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaLiquidacionFacturas implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaLiquidacionFacturas");
	
	private final String SQL_SELECT_MOVIMIENTOS 		= loadResourceToString("cl/bcs/contabilidad/sql/CentralizaLiquidacionFacturas_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS 			= loadResourceToString("cl/bcs/contabilidad/sql/CentralizaLiquidacionFacturas_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS 	= loadResourceToString("cl/bcs/contabilidad/sql/CentralizaLiquidacionFacturas_SelectDatosMovimiento.sql");
	
	public static class TabDatosMovimientoPaso{
		public Long IdMovimiento;	
        public String TipoDocumento;
        public Long Folio;
        public String FechaDocumento;
        public String CompraVenta;
        public String Operacion;
        public String Producto;
        public String ClaseInstrumento;
        public String TipoInstrumento;
        public String CondicionLiquidacion;  
        public String MonedaComprobante;
        public String Bolsa;
        public String DocumentoIdentidad;
        public String NombreCliente;
        public String Portafolio;
        public String Concepto;
        public String NombreConcepto;
        public String Agente;        
        public String NombreAgente;
        public String SucursalAgente;
        public String NombreSucursalAgente;
        public String CentroCostoAgente;
        public String EstadoSUNAT;
        public String DocumentoIdentidadCAVALI;
        public String EsExento;
        public String Moneda;
        public String MonedaLiquidacion;
        public String EsContraparte;
        public String Relacion;
        public String ClaseEmisor;
        public String Emisor;  
        public String Nemotecnico;
        public String CodigoIFRS;
        
      
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaLiquidacionFacturas");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = new ArrayList<TabDatosMovimientoPaso>();
		
		String localizacionSistema = infoAdicional.p_LocalizacionSistema;
		
		
//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<String,Object>();

		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		params.clear();

//EVENTOS

		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("Sistema", localizacionSistema);	
		out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		params.clear();
			
// MOVIMIENTOS			
		params.put("FechaContable", entrada.p_FechaContable);
		tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);


		for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoDocumento", mov.TipoDocumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Folio", String.valueOf(mov.Folio), BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaDocumento", mov.FechaDocumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CompraVenta", mov.CompraVenta, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Operacion", mov.Operacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MonedaComprobante", mov.MonedaComprobante, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa", mov.Bolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Concepto", mov.Concepto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreConcepto", mov.NombreConcepto, BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EstadoSUNAT", mov.EstadoSUNAT, BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidadCAVALI", mov.DocumentoIdentidadCAVALI, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsExento", mov.EsExento, BigDecimal.ZERO));	
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MonedaLiquidacion", mov.MonedaLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsContraparte", mov.EsContraparte, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseEmisor", mov.ClaseEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemotecnico", mov.Nemotecnico, BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CondicionLiquidacion", mov.CondicionLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));

		}
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);

		log.write("FIN::CentralizaLiquidacionFacturas");
		
		return out;
	}
}
