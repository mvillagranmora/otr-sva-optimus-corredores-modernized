package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaCarterasRVTerminoYMayorMenorValor implements BcsCentralizacion {

	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectDatosMovimiento.sql");
	
	private final String SQL_SELECT_MOVIMIENTOS_HISTORICO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectMovimientosHistorico.sql");
	private final String SQL_SELECT_EVENTOS_HISTORICO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectEventosHistorico.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS_HISTORICO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectDatosMovimientoHistorico.sql");
	
	private final Log log = new Log("CON.CentralizaCarterasRVTerminoYMayorMenorValor");
	
	public static class MovimientoCarterasRVTerminoYMayorMenorValor{		
		public Long IdMovimiento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String Portafolio;
		public String Moneda;
		public String Producto;
		public String ClaseInstrumento;
		public String TipoInstrumento;
		public String Nemo;
		public String Boveda;
		public String IdCartera;
		public String Emisor;
		public String TipoReajuste;
		public String CodigoIFRS;
		public String TipoCartera;
		public String ClaseEmisor;
		public String Agente;
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String CentroCostoAgente;
		public BigDecimal PrecioMercadoAnterior;
		public BigDecimal PrecioMercadoActual;
		public BigDecimal TipoCambio;
		public Date FechaAnterior;
		public Date FechaActual;
		public Date FechaLiquidacion;
		public String TipoPersona;
		public Date FechaOperacion;
		public String IdConcepto;
		public String DocumentoIdentidadCAVALI;
		public String Relacion;
		public String Bolsa;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaCarterasRVTerminoYMayorMenorValor");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		
		String fechaSistema = infoAdicional.p_FechaSistema;
		InformacionEmpresa empresa = infoAdicional.p_Empresa;
		
		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaContable", entrada.p_FechaContable);
		params.put("p_IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("p_DocumentoIdentidadCorredora", empresa.DocumentoIdentidad);
		if(fechaSistema.equals(entrada.p_FechaContable)) {
			out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
		}else {
			out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS_HISTORICO, params, TabMovimientos.class);
		}
		
		params.clear();
		
		//EVENTOS
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaContable", entrada.p_FechaContable);
		params.put("p_IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		params.put("p_DocumentoIdentidadCorredora", empresa.DocumentoIdentidad);
		if(fechaSistema.equals(entrada.p_FechaContable)) {
			out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
		}else {
			out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS_HISTORICO, params, TabEventos.class);
		}
		
		params.clear();
		
		//MOVIMIENTO DATOS OK
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaContable", entrada.p_FechaContable);
		params.put("p_DocumentoIdentidadCorredora", empresa.DocumentoIdentidad);
		
		List<MovimientoCarterasRVTerminoYMayorMenorValor> tabDatosMovimientoPaso = new ArrayList<MovimientoCarterasRVTerminoYMayorMenorValor>();
		if(fechaSistema.equals(entrada.p_FechaContable)) {
			tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, MovimientoCarterasRVTerminoYMayorMenorValor.class);
		}else {
			tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS_HISTORICO, params, MovimientoCarterasRVTerminoYMayorMenorValor.class);
		}
		params.clear();
		tabDatosMovimientoPaso.forEach(mov ->{
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdMovimiento", String.valueOf(mov.IdMovimiento), BigDecimal.ZERO)); 
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", mov.Boveda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdCartera", mov.IdCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoReajuste", mov.TipoReajuste, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCartera", mov.TipoCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseEmisor", mov.ClaseEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PrecioMercadoAnterior", "", mov.PrecioMercadoAnterior));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PrecioMercadoActual", "", mov.PrecioMercadoActual));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCambio", "", mov.TipoCambio));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaAnterior", Fecha.stringToSqlDate(mov.FechaAnterior), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaActual", Fecha.stringToSqlDate(mov.FechaActual), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa", mov.Bolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", Fecha.stringToSqlDate(mov.FechaLiquidacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona", mov.TipoPersona, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", Fecha.stringToSqlDate(mov.FechaOperacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Concepto", mov.IdConcepto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidadCAVALI", mov.DocumentoIdentidadCAVALI, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode", mov.CentroCostoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode2", mov.Producto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode3", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode4", mov.Bolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CostingCode5", "", BigDecimal.ZERO));
			
		});
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		return out;
	}

}
