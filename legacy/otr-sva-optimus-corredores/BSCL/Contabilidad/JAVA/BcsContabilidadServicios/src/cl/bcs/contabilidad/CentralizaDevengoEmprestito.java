package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CentralizaDevengoEmprestito implements BcsCentralizacion {
	private final Log log = new Log("CON.CentralizaDevengoEmprestito");
	
	private final String SQL_SELECT_EMPRESTITO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaDevengoEmprestito_SelectMovimiento.sql");
	private final String SQL_SELECT_EMPRESTITO_HISTORICO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaDevengoEmprestito_SelectMovimientoHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTO_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaDevengoEmprestito_SelectMovimientoTipoCentralizacionTipo.sql");
	
	public static final String DEVENGO_EMPRESTITO_RV	= "DEVEMPRV"; 
	public static final String DEVENGO_EMPRESTITO_RF	= "DEVEMPRF"; 
	
	public static class Movimientos{		
		public String 	DocumentoIdentidad;
		public Long 	IdPortafolio;
		public String 	CodigoPortafolio;
		public String 	EsCuentaPropia;
		public String 	EsRelacionado;
		public Date 	FechaOperacion;
		public Date 	FechaVencimientoOperacion;
		public Date 	FechaLiquidacion;
		public Long 	IdOperacion;
		public Long 	FolioOperacion;
		public String 	IdClasificacionTipoInstrumento;
		public String 	IdTipoInstrumento;
		public Long 	IdInstrumento;
		public String 	Nemotecnico;
		public String 	IdEmisor;
		public String 	IdTipoReajuste;
		public Date 	FechaVencimientoInstrumento;
		public String 	IdBoveda;
		public String 	CodigoIFRS;
		public String 	IdMonedaSistema;
		public String 	IdMonedaInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal TasaTir;
		public BigDecimal TasaCompra;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoAPlazoAlInicio;
		public BigDecimal ValorAPlazoAlInicio;
		public BigDecimal ValorMercadoAnterior;
		public BigDecimal MontoMercadoAnterior;
		public BigDecimal ValorMercadoActual;
		public BigDecimal MontoMercadoActual;
		public BigDecimal ValorMercadoProximo;
		public BigDecimal MontoMercadoProximo;
		public BigDecimal MontoCompraAnterior;
		public BigDecimal MontoCompraActual;
		public BigDecimal MontoCompraProximo;
		public BigDecimal ValorCompraAnterior;
		public BigDecimal ValorCompraActual;
		public BigDecimal ValorCompraProximo;
		public BigDecimal InteresMontoCompraAnterior;
		public BigDecimal InteresMontoCompraProximo;
		public Long IdEmpresa;
		public String IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoCentralizacion{
		public String IdTipoMovimiento;
	}
	
	public static class Configuracion{
		public String 			tipoCentralizacion;
		public String 			usaFechaLiquidacion;
		public java.util.Date 	fechaSistema;
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaDevengoEmprestito");
		SQL sql = new SQL();
		
		SalidaCentralizacion out = new SalidaCentralizacion();
		java.util.Date fechaContable = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_FechaContable);
		String fechaSist = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		java.util.Date fechaSistema = new SimpleDateFormat("yyyy-MM-dd").parse(fechaSist);
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
		List<TipoCentralizacion> TipoCentralizacion = sql.query(con, SQL_SELECT_MOVIMIENTO_CENTRALIZACION, params, TipoCentralizacion.class);
		params.clear();
		
		List<Movimientos> emprestitos = new ArrayList<Movimientos>();
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdTipoInstrumento", DEVENGO_EMPRESTITO_RV.equals(TipoCentralizacion.get(0).IdTipoMovimiento) ? "EMP" : "ERF");
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		
		if(fechaSistema.equals(fechaContable)) {
			emprestitos = sql.query(con, SQL_SELECT_EMPRESTITO, params, Movimientos.class);
		}else {
			emprestitos = sql.query(con, SQL_SELECT_EMPRESTITO_HISTORICO, params, Movimientos.class);
		}
		
		Configuracion conf = new Configuracion();
		conf.fechaSistema = fechaSistema;
		conf.tipoCentralizacion = TipoCentralizacion.get(0).IdTipoMovimiento;
		conf.usaFechaLiquidacion = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		for (Movimientos data : emprestitos) {
			dataCentralizacion(con, out, data, entrada, conf);
		}

		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		log.write("FIN::CentralizaDevengoEmprestito");
		return out;
	}
	
	
	public SalidaCentralizacion dataCentralizacion(Connection con, SalidaCentralizacion out, Movimientos data, EntradaCentralizacion entrada, Configuracion conf) throws Exception {
		if ("NO".equals(data.EsCuentaPropia)) {
			//MOVIMIENTOS
			TabMovimientos mov 	= new TabMovimientos();
			mov.IdTipoCentralizacion 	= entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento 			= data.FolioOperacion;
			mov.IdTipoMovimiento 		= DEVENGO_EMPRESTITO_RV.equals(conf.tipoCentralizacion) ? DEVENGO_EMPRESTITO_RV : DEVENGO_EMPRESTITO_RF;
			mov.IdEmpresa 				= data.IdEmpresa;
			mov.Numero 					= data.FolioOperacion;
			mov.Linea 					= data.FolioOperacion;
			mov.DocumentoIdentidad 		= data.DocumentoIdentidad;
			mov.IdPortafolio 			= data.IdPortafolio;
			mov.Fecha 					= entrada.p_FechaContable;
			mov.Periodo 				= entrada.p_FechaContable.substring(0,7);
			mov.Ejercicio 				= entrada.p_Ejercicio;
			mov.Monto 					= data.MontoCompraActual;
			mov.IdMoneda 				= data.IdMonedaSistema;
			mov.Estado 					= data.IdEstado;
			out.P_TABMOVIMIENTOS.add(mov);
			
			
			//DATOSMOVIMIENTO
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", data.DocumentoIdentidad, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", data.CodigoPortafolio, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsCuentaPropia", String.valueOf(data.EsCuentaPropia), BigDecimal.ZERO)); 
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsRelacionado", String.valueOf(data.EsRelacionado), BigDecimal.ZERO)); 
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", data.FechaOperacion.toString(), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", data.FechaLiquidacion.toString(), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FolioOperacion", data.FolioOperacion.toString(), BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", data.IdClasificacionTipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", data.IdTipoInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemotecnico", data.Nemotecnico, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", data.IdBoveda, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", data.IdEmisor, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoReajuste", data.IdTipoReajuste, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", data.CodigoIFRS, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", data.IdMonedaInstrumento, BigDecimal.ZERO));
			out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Premio","", data.TasaCompra));
			
			
			//EVENTOS
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MONTO_MERCADO_ANTERIOR", "Monto a Tasa Mercado Anterior",data.MontoMercadoAnterior, data.IdMonedaSistema)); 
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MONTO_MERCADO_ACTUAL", "Monto a Tasa Mercado Actual",data.MontoMercadoActual, data.IdMonedaSistema)); 
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MONTO_COMPRA_ANTERIOR", "Monto a Tasa Compra Anterior",data.MontoCompraAnterior, data.IdMonedaSistema)); 
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MONTO_COMPRA_ACTUAL", "Monto a Tasa Compra Actual",data.MontoCompraActual, data.IdMonedaSistema)); 
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MONTO_COMPRA_PROXIMO", "Monto a Tasa Compra Proximo",data.MontoCompraProximo, data.IdMonedaSistema)); 
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "INTERES_COMPRA_ANTERIOR", "Interes Compra Anterior",data.InteresMontoCompraAnterior, data.IdMonedaSistema)); 
			out.P_TABEVENTOS.add(new TabEventos(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "INTERES_COMPRA_PROXIMO", "Interes Compra Proximo",data.InteresMontoCompraProximo, data.IdMonedaSistema)); 
		}
		
		return out;
	}

	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			
			String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CON\",\"p_IdTipoCentralizacion\":406,\"p_FechaContable\":\"2024-04-10\",\"p_Ejercicio\":2024,\"p_SimulaOperacion\":\"N\"}";
			
			EntradaCentralizacion Objin = gson.fromJson(Json, EntradaCentralizacion.class);
			CentralizaDevengoEmprestito  demo=new CentralizaDevengoEmprestito();				
			try {
				SalidaCentralizacion ObjOut=demo.centralizar(con, Objin, null);
				con.commit();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
	}
}
