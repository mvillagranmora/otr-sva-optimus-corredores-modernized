package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

public class CentralizaEmprestitoInicio implements BcsCentralizacion {
	
	private final String SQL_SELECT_CLASIFICACION_TIPO_INSTRUMENTO= loadResourceToString("cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectClasificacionTipoInstrumento.sql");
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectMovimientos.sql");
	private final String SQL_SELECT_MOVIMIENTOS_HISTORICOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectMovimientosHistoricos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectEventos.sql");
	private final String SQL_SELECT_EVENTOS_HISTORICOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectEventosHistoricos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectDatosMovimiento.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS_HISTORICOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectDatosMovimientoHistorico.sql");

	private final Logger log = LogManager.getLogger(CentralizaEmprestitoInicio.class);
	
	public static class TabDatosMovimientoPaso extends BcsPojo{
		public Long IdMovimiento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String Portafolio;
		public String Moneda;
		public String Producto;
		public String EsRelacionado;
		public String EsCuentaPropia;
		public String Nemo;
		public String Boveda;
		public String IdCartera;
		public String Emisor;
		public String TipoReajuste;		
		public String CodigoIFRS;		
		public String Agente;	
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String CentroCostoAgente;
		public String CondicionLiquidacion;
		public String TipoPrestamo;
		public String TipoPrestatario;
		public String FechaLiquidacion;
		public String FechaOperacion;	
	}
	
	public static class ClasificacionTipoInstrumento extends BcsPojo {
		public String ClasificacionTipoInstrumento;
	}

	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.info("INICIO::CentralizaEmprestitoInicio");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		
		String localizacionSistema = infoAdicional.p_LocalizacionSistema;
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		List<ClasificacionTipoInstrumento> clasificacion = sql.query(con, SQL_SELECT_CLASIFICACION_TIPO_INSTRUMENTO, params, ClasificacionTipoInstrumento.class);
		params.clear();
		
		//MOVIMIENTOS
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("IdClasificacionTipoInstrumento",clasificacion.get(0).ClasificacionTipoInstrumento);
		if(entrada.p_FechaContable.equals(fechaSistema)) {
			out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		}else {
			out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS_HISTORICOS, params, TabMovimientos.class);
		}
		params.clear();
		
		//EVENTOS
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("IdClasificacionTipoInstrumento",clasificacion.get(0).ClasificacionTipoInstrumento);
		if(entrada.p_FechaContable.equals(fechaSistema)) {
			out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		}else {
			out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS_HISTORICOS, params, TabEventos.class);
		}
		params.clear();
		
		//MOVIMIENTO DATOS
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("LocSistema",localizacionSistema);
		params.put("IdClasificacionTipoInstrumento",clasificacion.get(0).ClasificacionTipoInstrumento);
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = new ArrayList<TabDatosMovimientoPaso>();
		if(entrada.p_FechaContable.equals(fechaSistema)) {
			tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
		}else {
			tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS_HISTORICOS, params, TabDatosMovimientoPaso.class);
		}
		params.clear();
		
		for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){

			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsRelacionado", mov.EsRelacionado, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsCuentaPropia", mov.EsCuentaPropia, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", mov.Boveda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdCartera", mov.IdCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoReajuste", mov.TipoReajuste, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPrestamo", mov.TipoPrestamo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPrestatario", mov.TipoPrestatario, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CondicionLiquidacion", mov.CondicionLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", mov.FechaLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", mov.FechaOperacion, BigDecimal.ZERO));
		}
	
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		log.info("FIN::CentralizaEmprestitoInicio");
	
		return out;
	}
}