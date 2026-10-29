package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaTomaDepositosAutodesembolso implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaTomaDepositosAutodesembolso");
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTomaDepositosAutodesembolso_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTomaDepositosAutodesembolso_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTomaDepositosAutodesembolso_SelectDatosMovimiento.sql");
	
	public static class TabDatosMovimientoPaso{
		public Long IdMovimiento;
		public String DocumentoIdentidad;
		public String Portafolio;
		public String CentroCosto;
		public String CodigoAgente;
		public String CodigoBolsa;
		public String CodigoIFRS;
		public String TipoInstrumento;
		public String CodigoSucursal;
		public String CodigoTipoOperacion;
		public Long CodSecuencia;
		public String DiasLiquidacion;
		public String DiasVencido;
		public Integer DiasVencimiento;
		public String EmisorInstrumento;
		public String EstadoMovimiento;
		public String FamiliaInstrumento;
		public Date FechaEmisionDocumento;
		public Date FechaLiquidacionDocumento;
		public String IndicadorLiquidacion;
		public String IndicadorSimultanea;
		public String Nemo;
		public String ProductoMovimiento;
		public String RelacionadoEmpresa;
		public String RelacionCorredora;
		public String TipoEmisor;
		public String FamiliaTipoInstrumento;
		public String Relacion;
		public String ClaseEmisor;
		public String TipoCartera;
		public String TipoFamilia;
		public String TipoReajuste;
		public Long   IdCartera;
		public String TipoDocumento;
		//public String VentaCorta;
		//public String TipoCmbteCont;
		//public String FolioCmbteCont;
		public String Concepto;
		public String Glosa;
		public Date FechaDeVencimiento;
		public Date FechaAnterior;
		public Timestamp FechaActual;
		public BigDecimal TasaMercadoActual;
		public BigDecimal PrecioMercadoActual;
		public BigDecimal TasaMercadoAnterior;
		public BigDecimal PrecioMercadoAnterior;
		public String TasaEmision;
		public BigDecimal TasaCompra;
		public BigDecimal PrecioCompra;
		public String Moneda;
		public BigDecimal TasaCambio;
		public Date FechaLiquidacion;
		public String TipoPersona;
		public Date FechaOperacion;
		public String NombreCliente;
		public String DocumentoIdentidadCAVALI;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaTomaDepositosAutodesembolso");
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
//		params.put("FechaSistema", fechaSistema);
		List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
		params.clear();
		
		for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCosto", mov.CentroCosto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoAgente", mov.CodigoAgente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoBolsa", mov.CodigoBolsa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoSucursal", mov.CodigoSucursal, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoTipoOperacion", mov.CodigoTipoOperacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodSecuencia", "", BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DiasLiquidacion", mov.DiasLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DiasVencido", mov.DiasVencido, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DiasVencimiento", "", BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EmisorInstrumento", mov.EmisorInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EstadoMovimiento", mov.EstadoMovimiento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FamiliaInstrumento", mov.FamiliaInstrumento, BigDecimal.ZERO));
			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaEmisionDocumento", Fecha.stringToSqlDate(mov.FechaEmisionDocumento), BigDecimal.ZERO));
			

			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacionDocumento", Fecha.stringToSqlDate(mov.FechaLiquidacionDocumento), BigDecimal.ZERO));			
			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IndicadorLiquidacion", mov.IndicadorLiquidacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IndicadorSimultanea", mov.IndicadorSimultanea, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ProductoMovimiento", mov.ProductoMovimiento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "RelacionadoEmpresa", mov.RelacionadoEmpresa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "RelacionCorredora", mov.RelacionCorredora, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoEmisor", mov.TipoEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FamiliaTipoInstrumento", mov.FamiliaTipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseEmisor", mov.ClaseEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCartera", mov.TipoCartera, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoFamilia", mov.TipoFamilia, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoReajuste", mov.TipoReajuste, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdCartera","", BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoDocumento", mov.TipoDocumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Concepto", mov.Concepto, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Glosa", mov.Glosa, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaDeVencimiento", Fecha.stringToSqlDate(mov.FechaDeVencimiento), BigDecimal.ZERO));			
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaAnterior", Fecha.stringToSqlDate(mov.FechaAnterior), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaActual", Fecha.toStringDatetime(mov.FechaActual), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", Fecha.stringToSqlDate(mov.FechaLiquidacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona", mov.TipoPersona, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", Fecha.stringToSqlDate(mov.FechaOperacion), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidadCAVALI", mov.DocumentoIdentidadCAVALI, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TasaMercadoActual", "", mov.TasaMercadoActual));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PrecioMercadoActual", "", mov.PrecioMercadoActual));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TasaMercadoAnterior", "", mov.TasaMercadoAnterior));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PrecioMercadoAnterior", "", mov.PrecioMercadoAnterior));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TasaEmision", mov.TasaEmision, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TasaCompra", "", mov.TasaCompra));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PrecioCompra", "", mov.PrecioCompra));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TasaCambio", "", mov.TasaCambio));

		}
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		log.write("FIN::CentralizaTomaDepositosAutodesembolso");
		return out;
	}
	
}
