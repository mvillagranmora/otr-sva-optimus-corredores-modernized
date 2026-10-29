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

public class CentralizaCuentaCaja implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaCuentaCaja");
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCuentaCaja_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCuentaCaja_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCuentaCaja_SelectDatosMovimiento.sql");
	
	public static class TabDatosMovimientoPaso{
		public Long IdMovimiento;
		public String TipoDocumento;
		public Long   Folio;
		public String FechaDocumento;
		public Long   IdEmpresa;
		public String Fecha;
		public String DocumentoIdentidad;
		public String Portafolio;
		public String Concepto;
		public String NombreConcepto;
		public String NombreCliente;
		public String Moneda;
//		public String EsCuentaPropia;
		public String Agente;
		public String NombreAgente;
		public Long SucursalAgente;
		public String NombreSucursalAgente;
		public String CentroCostoAgente;
		public String Producto;
		public String Relacion;
		public String Bolsa;
		public String TipoPersona;
		public String Nemo;
		public String DocumentoIdentidadCAVALI;
		public Long Dias;
		public String TipoCmbte;
		public String ClienteBancario;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaCuentaCaja");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		
		String localizacionSistema = infoAdicional.p_LocalizacionSistema;
		
		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		
		out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		params.clear();
		
		//EVENTOS
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		params.clear();
		
		//MOVIMIENTO DATOS
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("LocSistema",localizacionSistema);
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
		params.clear();
		
		for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){

			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoDocumento"           , mov.TipoDocumento		  			, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Folio"                   , String.valueOf(mov.Folio)	        , BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaDocumento"          , mov.FechaDocumento				, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdEmpresa"               , String.valueOf(mov.IdEmpresa)		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Fecha"                   , mov.Fecha		            		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad"      , mov.DocumentoIdentidad			, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio"              , mov.Portafolio		    		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Concepto"                , mov.Concepto		        		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreConcepto"          , mov.NombreConcepto				, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente"           , mov.NombreCliente		    		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda"                  , mov.Moneda		        		, BigDecimal.ZERO));
//			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsCuentaPropia"          , mov.EsCuentaPropia				, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente"                  , mov.Agente		        		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente"			, mov.NombreAgente		    		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente"          , String.valueOf(mov.SucursalAgente), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente"    , mov.NombreSucursalAgente			, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente"       , mov.CentroCostoAgente				, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto"                , mov.Producto		        		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion"                , mov.Relacion		        		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa"                   , mov.Bolsa		            		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona"             , mov.TipoPersona		    		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo"                    , mov.Nemo		            		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidadCAVALI", mov.DocumentoIdentidadCAVALI		, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Dias"                    , String.valueOf(mov.Dias)			, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCmbte"			    , mov.TipoCmbte		    			, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClienteBancario"		 ,mov.ClienteBancario				, BigDecimal.ZERO));		
			
		}
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		log.write("FIN::CentralizaCuentaCaja");
		return out;
	}
	
}
