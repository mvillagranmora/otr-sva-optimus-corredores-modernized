package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlRetiroCustodia implements JavaCall {
	static final Logger logger = LogManager.getLogger(ControlRetiroCustodia.class);
	
	private final String SQL_SelectCarteraDisponible = loadResourceToString("cl/bcs/custodia/sql/ControlRetiroCustodia_SEL_CarteraDisponible.sql");

	public static String getHashBusqueda(Long IdInstrumento, Long IdPortafolio, Long IdCliente,String nemosvs) {
		return "" + IdInstrumento + "-" + IdPortafolio + "-" + IdCliente + "-" + nemosvs;
	}
	
	public static class ObjTablaCartera {
		public Long IdInstrumento;
		public Long IdPortafolio;
		public Long IdCliente;
		public Date FechaLiquidacion;
		public BigDecimal CantidadORescateDisponible;
		public String hashBusqueda;
		
		public String Nemotecnico;
		public String CodigoPortafolio;
		public String DocumentoIdentidad;
		
		public String NemoSVS;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class WrapperRetiro {
		public Long IdGenerarMovimientoCustodia;
		public ControlRetiroObjDatosEntrada retiro;
		public BigDecimal CantidadPositiva;	//se va a la cartera
		public BigDecimal MontoPositivo;	//se va a la cartera
		public BigDecimal RescatePositivo;	//se va a la cartera
		public BigDecimal CantidadNegativa;	//se va al negativo
		public BigDecimal MontoNegativo;	//se va a la cartera
		public BigDecimal RescateNegativo;	//se va a la cartera
	}
	
	public static class ControlRetiroCustodiaEntrada extends RetiroCustodia.RetiroCustodiaEntrada {
		//public List<ControlRetiroObjDatosEntrada>	p_DatosEntrada;
	};
	
	public static class ControlRetiroObjDatosEntrada extends RetiroCustodia.ObjDatosEntrada {
		public boolean PermiteSobregiro = false;
		public BigDecimal CantidadORescate = BigDecimal.ZERO; //SI ES IF, ACA VA EL RESCATE, SI ES RV O RF, ES CANTIDAD
	};
	
	
	public RetiroCustodiaSalida logica(Connection con, ControlRetiroCustodiaEntrada in) throws Exception {
		logger.info("INICIO");
		RetiroCustodia retiroCustodia = new RetiroCustodia();
		SQL _sql = new SQL();
		
		HashMap<String, ArrayList<WrapperRetiro>> entradasAgrupadas = new HashMap<>();
		List<IngresoCustodia.ObjDatosEntrada> ingresosNegativos = new ArrayList<>();

		List<String> list  = new ArrayList<>();
		
		String operacionesParam = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.OperacionesNegativas");
		List<String> operaciones = new ArrayList<>();
		
		try {
			String[] array = operacionesParam.split(",");
			operaciones = Arrays.asList(array);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		for (ObjDatosEntrada entrada : in.p_DatosEntrada) {
			ControlRetiroObjDatosEntrada entradaC = (ControlRetiroObjDatosEntrada) entrada;
			if (entradaC.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && entradaC.EsFungible.equalsIgnoreCase("S")) {
				entradaC.CantidadORescate = entrada.ValorRescate;
			}
			else {
				entradaC.CantidadORescate = entrada.Cantidad;
			}
		}
		
		//agrupo y genero mi lista de busqueda
		for (ObjDatosEntrada entrada : in.p_DatosEntrada) {
			
			String hash = getHashBusqueda(entrada.IdInstrumento, entrada.IdPortafolio, entrada.IdCliente,entrada.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")?entrada.NemoSVS:"");
			
			if (!entradasAgrupadas.containsKey(hash)) {
				entradasAgrupadas.put(hash, new ArrayList<>());
			}
			
			List<WrapperRetiro> grupo = entradasAgrupadas.get(hash);
		
			WrapperRetiro c = new WrapperRetiro();
			c.IdGenerarMovimientoCustodia = entrada.IdGenerarMovimientoCustodia;
			c.retiro = (ControlRetiroObjDatosEntrada) entrada;
			c.CantidadNegativa = BigDecimal.ZERO;
			c.CantidadPositiva = BigDecimal.ZERO;
			c.MontoNegativo = BigDecimal.ZERO;
			c.MontoPositivo = BigDecimal.ZERO;
			grupo.add(c);
			
			list.add(hash);
		}
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("lista", list);
		//TODO revisar filtro regimen, boveda, cuenta custodia
		List<ObjTablaCartera> carteras = _sql.query(con, SQL_SelectCarteraDisponible, params, ObjTablaCartera.class);
		params.clear();
		
		//main loop
		//por cada grupo de retiros segun el hash
		for (String key : entradasAgrupadas.keySet()){
			ArrayList<WrapperRetiro> grupo = entradasAgrupadas.get(key);
			for (WrapperRetiro wretiro : grupo) {
				if (!operaciones.contains(wretiro.retiro.IdTipoOperacion)) continue;
				if (!wretiro.retiro.PermiteSobregiro) continue;
				String hashBusqueda = getHashBusqueda(wretiro.retiro.IdInstrumento,wretiro.retiro.IdPortafolio,wretiro.retiro.IdCliente,wretiro.retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")?wretiro.retiro.NemoSVS:"");
			
				BigDecimal cantidadORescatedisponible = consumirCartera(carteras, hashBusqueda,wretiro.retiro);
				//devuelve la cantidad consumida (disponible para retiro), rebaja las carteras disponbles internamente 
				//debe considerar consumir por fecha de liquidacion
				//consumir la mas antigua primero -> FIFO
				
				//retiro 100, disp 30, negativo 70, actual 30  -> negativo + actual = retiro
				//retiro 100, disp 140, negativo 0, actual 100
				//retiro 100, disp 0, negativo 100, actual 0
				//retiro 100, disp 100, negativo 0, actual 100
				//retiro 100, disp 99, negativo 1, actual 99
							
				//tiene mas disponible (o justo) de lo que quiere retirar, no genera negativo
				if (cantidadORescatedisponible.compareTo(wretiro.retiro.CantidadORescate)>=0) {
					wretiro.CantidadNegativa = BigDecimal.ZERO;
					wretiro.CantidadPositiva = wretiro.retiro.CantidadORescate;
				}
				else {
					//se genera un registro negativo
					//calculamos nuevos valores para cantidad y monto
					wretiro.CantidadNegativa = wretiro.retiro.CantidadORescate.subtract(cantidadORescatedisponible);
					BigDecimal factorNegativo = wretiro.CantidadNegativa.divide(wretiro.retiro.CantidadORescate,RoundingMode.HALF_DOWN);
					wretiro.MontoNegativo = wretiro.retiro.MontoCompraMonedaInstrumento.multiply(factorNegativo, MathContext.DECIMAL64);
					wretiro.CantidadPositiva = cantidadORescatedisponible; 		//dejamos para retirar todo lo que me quedaba
					wretiro.MontoPositivo = wretiro.retiro.MontoCompraMonedaInstrumento.subtract(wretiro.MontoNegativo);
					if (wretiro.retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && wretiro.retiro.EsFungible.equalsIgnoreCase("S")) {
						wretiro.RescateNegativo = wretiro.CantidadNegativa;
						wretiro.RescatePositivo = wretiro.CantidadPositiva;
					}
					else if (wretiro.retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && wretiro.retiro.EsFungible.equalsIgnoreCase("N")) {
						wretiro.RescateNegativo = wretiro.retiro.ValorRescate.multiply(factorNegativo, MathContext.DECIMAL64);
						wretiro.RescatePositivo = wretiro.retiro.ValorRescate.subtract(wretiro.RescateNegativo);
					}
					else {
						wretiro.RescateNegativo = BigDecimal.ZERO;
						wretiro.RescatePositivo = BigDecimal.ZERO;
					}
					//si es if fungible, en cantidad esta el rescate, y la cantidad es 1, se proratea el monto
					//si es if no fungible, la cantidad son los cortes y hay que proratear el rescate y el monto
					//si es rf o rf, solo se proratea el monto
					
					//debemos actualizamos el retiro original, primero la variable dummy, despues cantidad monto y rescate
					{
						wretiro.retiro.CantidadORescate = wretiro.CantidadPositiva;
						//actualizamos rescate si es IF Fungible o cantidad si es lo demas
						if (wretiro.retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && wretiro.retiro.EsFungible.equalsIgnoreCase("S")) {
							wretiro.retiro.Cantidad = BigDecimal.ONE; //debe mantenerse en 1
							wretiro.retiro.ValorRescate = wretiro.RescatePositivo;
							
						}
						else if (wretiro.retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && wretiro.retiro.EsFungible.equalsIgnoreCase("N")) {
							wretiro.retiro.Cantidad = wretiro.CantidadPositiva;
							wretiro.retiro.ValorRescate = wretiro.RescatePositivo;
						}
						//RV RF
						else {
							wretiro.retiro.Cantidad = wretiro.CantidadPositiva;
						}
						wretiro.retiro.MontoCompraMonedaInstrumento = wretiro.MontoPositivo;
					}
					
					//generamos el registro negativo
					IngresoCustodia.ObjDatosEntrada c = new IngresoCustodia.ObjDatosEntrada();
					c.IdGenerarMovimientoCustodia 			= wretiro.IdGenerarMovimientoCustodia;	//debe ser el mismo para que la salida pueda interpretar
					c.TablaAfectada							= "NEGATIVA";
					c.AccionOperacion						= "INGRESO";//"RETIRO";
					c.IdMovimientoCarteraEnTransitoOrigen 	= wretiro.retiro.IdMovimientoCarteraEnTransitoOrigen;// mov.IdMovimientoCarteraEnTransito;
					c.IdCliente								= wretiro.retiro.IdCliente; //mov.IdCliente; 
					c.IdPortafolio							= wretiro.retiro.IdPortafolio; // mov.IdPortafolio;
					c.FechaOperacion						= wretiro.retiro.FechaOperacion; //mov.FechaOperacion;
					c.IdTipoOperacion						= wretiro.retiro.IdTipoOperacion; //mov.IdTipoOperacion;		
					c.IdConcepto							= wretiro.retiro.IdConcepto; //mov.IdConcepto;
					c.IdCondicionLiquidacion				= wretiro.retiro.IdCondicionLiquidacion; //mov.IdCondicionLiquidacion; 
					c.FechaLiquidacion						= wretiro.retiro.FechaLiquidacion;// Fecha.toDate(fecha);
					c.IdTipoCanalIngreso					= wretiro.retiro.IdTipoCanalIngreso; //mov.IdTipoCanalIngreso; 		
					c.IdTipoComprobante						= wretiro.retiro.IdTipoComprobante; //mov.IdTipoComprobante; 
					c.FolioComprobante						= wretiro.retiro.FolioComprobante; //mov.FolioComprobante; 
					c.FolioCustodia							= wretiro.retiro.FolioCustodia; //mov.FolioCustodia; 
					c.FolioExternoReferencia				= wretiro.retiro.FolioExternoReferencia; //mov.FolioExternoReferencia;
					c.IdUsuario								= wretiro.retiro.IdUsuario; //mov.IdUsuario;
					c.IdSucursal							= wretiro.retiro.IdSucursal; //mov.IdSucursal; 
					c.IdAgente								= wretiro.retiro.IdAgente; //mov.IdAgente;
					c.Glosa									= wretiro.retiro.Glosa; //mov.Glosa;
					c.IdInstrumento							= wretiro.retiro.IdInstrumento; //mov.IdInstrumento;
					c.Nemotecnico                      		= wretiro.retiro.Nemotecnico; //mov.Nemotecnico;
					c.NemoSVS                          		= wretiro.retiro.NemoSVS; //mov.NemoSVS;
					c.IdMonedaInstrumento					= wretiro.retiro.IdMonedaInstrumento; //mov.IdMonedaInstrumento;
					c.IdMonedaEmision						= wretiro.retiro.IdMonedaEmision; //mov.IdMonedaEmision;
					c.IdTipoReajusteInstrumento				= wretiro.retiro.IdTipoReajusteInstrumento; //mov.IdTipoReajuste;
					c.IdEmisor								= wretiro.retiro.IdEmisor; //mov.IdEmisor;
					c.EsFungible							= wretiro.retiro.EsFungible; //mov.EsFungible;
					c.IdBoveda								= wretiro.retiro.IdBoveda; //mov.IdBoveda;
					c.EsNominal								= wretiro.retiro.EsNominal; //mov.EsNominal;	
					c.Cantidad								= wretiro.retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && wretiro.retiro.EsFungible.equalsIgnoreCase("S")?BigDecimal.ONE:wretiro.CantidadNegativa;// mov.Cantidad; 
					c.CorteMinimo							= wretiro.retiro.CorteMinimo; //mov.CorteMinimo; 
					c.ValorRescate							= wretiro.RescateNegativo; //mov.ValorRescate; 
					c.PrecioCompra							= wretiro.retiro.PrecioCompra; //mov.PrecioCompra; 
					c.TasaCompra							= wretiro.retiro.TasaCompra; //mov.TasaCompra;
					c.MontoCompraMonedaInstrumento			= wretiro.MontoNegativo; //mov.MontoCompraMonedaInstrumento;
					c.EsVentaDescubierta					= wretiro.retiro.EsVentaDescubierta;
					c.IdTipoProducto						= wretiro.retiro.IdTipoProducto; //mov.IdTipoProducto;
					c.FechaVencimientoInstrumento  			= wretiro.retiro.FechaVencimientoInstrumento;
					if (wretiro.retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF"))
						c.FechaVencimientoOperacion				= wretiro.retiro.FechaVencimientoInstrumento;
					c.IdMovimientoComprobante				= 0L;//wretiro.retiro.IdMovimientoComprobante; TODO ESTE DATO NO ESTA EN EL RETIRO
					c.EsRegistroNuevo 						= "S";
					ingresosNegativos.add(c);
				}
			}
		}
		
		RetiroCustodiaSalida out = new RetiroCustodiaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		//eliminamos retiros con cantidad 0
		List<ObjDatosEntrada> retirosPositivos = new ArrayList<>();
		for(ObjDatosEntrada retiro : in.p_DatosEntrada) {
			if (retiro.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && retiro.EsFungible.equalsIgnoreCase("S")) {
				if (retiro.ValorRescate.compareTo(BigDecimal.ZERO) > 0)
					retirosPositivos.add(retiro);
			}
			else if (retiro.Cantidad.compareTo(BigDecimal.ZERO) > 0) {
				retirosPositivos.add(retiro);
			}
		}
		
		if (retirosPositivos.size() >0) {
			in.p_DatosEntrada = retirosPositivos;
			out = retiroCustodia.logica(con, in);
		}
		
		//ingresar negativos
		if (ingresosNegativos.size()>0) {
			IngresoCustodia ingresoCustodia = new IngresoCustodia();	
			IngresoCustodiaEntrada entradaIngresoCustodia = new IngresoCustodiaEntrada();
			entradaIngresoCustodia.p_DatosEntrada = ingresosNegativos;
			entradaIngresoCustodia.p_IdEmpresa = in.p_IdEmpresa;
			entradaIngresoCustodia.p_IdModulo = in.p_IdModulo;
			entradaIngresoCustodia.p_idPais = in.p_idPais;
			entradaIngresoCustodia.p_IdUsuario = in.p_IdUsuario;
			entradaIngresoCustodia.p_SimularOperacion = in.p_SimularOperacion;
			entradaIngresoCustodia.p_TicketAtencion = in.p_TicketAtencion;
			entradaIngresoCustodia.p_ValorizacionAutomatica = in.p_ValorizacionAutomatica;
			IngresoCustodiaSalida salidaIngresoCustodia = ingresoCustodia.logica(con, entradaIngresoCustodia );
			out.P_OUT_MENSAJES.addAll(salidaIngresoCustodia.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR+=salidaIngresoCustodia.P_OUT_ERRORESCONTADOR;
			List<ObjDatosSalida> datosSalidaIngreso = new ArrayList<>();
			for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida dato : salidaIngresoCustodia.P_OUT_DATOSSALIDA) {
				ObjDatosSalida o = new ObjDatosSalida();
				o.IdGenerarMovimientoCustodia = dato.IdGenerarMovimientoCustodia;
				o.IdCustodia = dato.IdCustodia;
				o.RegistroConProblemas = dato.RegistroConProblemas;
				o.TablaOrigen = dato.TablaOrigen;
				datosSalidaIngreso.add(o);
			}
			out.P_OUT_DATOSSALIDA.addAll(datosSalidaIngreso);
			
		}
		
		logger.info("TERMINO");
		return out;
	}

	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		//try (Connection con = PlataformaUtils.getQAPEConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				//String json = "{\"p_IdEmpresa\":1,\"p_SimularOperacion\":\"N\",\"p_TicketAtencion\":0,\"p_DatosEntrada\":[{\"IdGenerarMovimientoCustodia\":1641,\"TablaAfectada\":\"CARTERA\",\"EsRetiroNegocio\":null,\"EsRetiroPorFusion\":\"N\",\"IdCustodia\":1523269,\"FolioCustodia\":20684927,\"IdCarteraOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdTipoComprobanteBusqueda\":null,\"FolioComprobanteBusqueda\":null,\"AccionOperacion\":\"ANULACION\",\"FechaRetiroRegistro\":null,\"IdCliente\":123569,\"IdPortafolio\":27499,\"FechaOperacion\":\"2018-08-16 00:00:00.000\",\"IdTipoOperacion\":\"COMPAC\",\"IdTipoProducto\":\"MERF\",\"PlazoEnDiasOperacion\":null,\"IdOperacion\":null,\"IdConcepto\":\"REPCME\",\"IdCondicionLiquidacion\":null,\"FechaLiquidacion\":\"2018-08-16 00:00:00.000\",\"IdTipoCanalIngreso\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":null,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdSucursal\":null,\"IdAgente\":null,\"IdUsuario\":null,\"IdModulo\":null,\"Glosa\":null,\"IdOrigenGarantia\":null,\"IdInstrumento\":346378,\"IdMonedaInstrumento\":null,\"IdMonedaEmision\":null,\"IdTipoReajusteInstrumento\":null,\"IdEmisor\":\"\",\"EsFungible\":\"N\",\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"NemoSVS\":null,\"Cantidad\":1,\"CorteMinimo\":null,\"ValorRescate\":1508000.00000000,\"PrecioSucio\":null,\"PrecioCompra\":null,\"TasaCompra\":null,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":1.5,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":1500313,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null,\"TipoMonedaPrincipal\":null,\"IdCarga\":null,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":null}]}";
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":null,\"p_SimularOperacion\":\"N\",\"p_TicketAtencion\":1,\"p_DatosEntrada\":[{\"IdGenerarMovimientoCustodia\":1,\"TablaAfectada\":\"CARTERA\",\"EsRetiroNegocio\":null,\"EsRetiroPorFusion\":\"N\",\"IdCustodia\":1523278,\"FolioCustodia\":20684935,\"IdCarteraOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdTipoComprobanteBusqueda\":null,\"FolioComprobanteBusqueda\":null,\"AccionOperacion\":\"ANULACION\",\"FechaRetiroRegistro\":\"2018-08-16 20:55:45.000\",\"IdCliente\":123569,\"IdPortafolio\":27499,\"FechaOperacion\":\"2018-08-16 00:00:00.000\",\"IdTipoOperacion\":\"REVFWD\",\"IdTipoProducto\":null,\"PlazoEnDiasOperacion\":null,\"IdOperacion\":1642,\"IdConcepto\":\"REVFWD\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-08-17 00:00:00.000\",\"IdTipoCanalIngreso\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":1642,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdSucursal\":1,\"IdAgente\":10003,\"IdUsuario\":33,\"IdModulo\":\"MFW\",\"Glosa\":\"\",\"IdOrigenGarantia\":null,\"IdInstrumento\":346370,\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmision\":null,\"IdTipoReajusteInstrumento\":null,\"IdEmisor\":null,\"EsFungible\":null,\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"NemoSVS\":null,\"Cantidad\":1000.00000000,\"CorteMinimo\":null,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":0E-8,\"TasaCompra\":600.00000000,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"IdSpotAsociado\":0,\"IdModalidadCumplimiento\":6,\"MontoMonedaPrincipal\":1000.00000000,\"MontoMonedaSecundaria\":620000.0000000000000000,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":0,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":0E-8,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":0E-8,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":\"S\",\"TasaPuntosMonedaPrincipal\":0E-8,\"TasaPuntosMonedaSecundaria\":600.00000000,\"MontoMatriz\":0,\"TasaDescuentoAnticipo\":0E-8,\"TipoCambioAnticipo\":0E-8,\"MontoARecibirAnticipo\":0E-8,\"TipoCambioObservado\":0E-8,\"FechaEfectiva\":\"2018-08-16 00:00:00.000\",\"IdPlaza\":2931,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01 00:00:00.000\",\"HoraFixing\":\"12:00:00 AM\",\"IdReferenciaMercado\":0,\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"ValorTipoCambioCierre\":620.00000000,\"ValorParidadCierre\":0E-8,\"ValorTipoCambioCosto\":600.00000000,\"ValorParidadCosto\":0,\"TipoMonedaPrincipal\":null,\"IdCarga\":null,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":null}],\"p_ValorizacionAutomatica\":\"N\"}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":null,\"p_SimularOperacion\":\"N\",\"p_TicketAtencion\":1,\"p_DatosEntrada\":[{\"IdGenerarMovimientoCustodia\":1,\"TablaAfectada\":\"CARTERA\",\"EsRetiroNegocio\":null,\"EsRetiroPorFusion\":\"N\",\"IdCustodia\":1523434,\"FolioCustodia\":20685092,\"IdCarteraOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdTipoComprobanteBusqueda\":null,\"FolioComprobanteBusqueda\":null,\"AccionOperacion\":\"ANULACION\",\"FechaRetiroRegistro\":\"2018-08-17 18:37:31.000\",\"IdCliente\":123569,\"IdPortafolio\":27499,\"FechaOperacion\":\"2018-08-17 00:00:00.000\",\"IdTipoOperacion\":\"REVFWD\",\"IdTipoProducto\":null,\"PlazoEnDiasOperacion\":null,\"IdOperacion\":1655,\"IdConcepto\":\"REVFWD\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-08-22 00:00:00.000\",\"IdTipoCanalIngreso\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":1655,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdSucursal\":1,\"IdAgente\":10003,\"IdUsuario\":33,\"IdModulo\":\"MFW\",\"Glosa\":\"\",\"IdOrigenGarantia\":null,\"IdInstrumento\":346370,\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmision\":null,\"IdTipoReajusteInstrumento\":null,\"IdEmisor\":null,\"EsFungible\":null,\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"NemoSVS\":null,\"Cantidad\":125.00000000,\"CorteMinimo\":null,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":0E-8,\"TasaCompra\":600.00000000,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"IdSpotAsociado\":0,\"IdModalidadCumplimiento\":6,\"MontoMonedaPrincipal\":125.00000000,\"MontoMonedaSecundaria\":77500.0000000000000000,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":0,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":0E-8,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":0E-8,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":\"S\",\"TasaPuntosMonedaPrincipal\":0E-8,\"TasaPuntosMonedaSecundaria\":600.00000000,\"MontoMatriz\":0,\"TasaDescuentoAnticipo\":0E-8,\"TipoCambioAnticipo\":0E-8,\"MontoARecibirAnticipo\":0E-8,\"TipoCambioObservado\":0E-8,\"FechaEfectiva\":\"2018-08-17 00:00:00.000\",\"IdPlaza\":2931,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01 00:00:00.000\",\"HoraFixing\":\"12:00:00 AM\",\"IdReferenciaMercado\":0,\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"ValorTipoCambioCierre\":620.00000000,\"ValorParidadCierre\":0E-8,\"ValorTipoCambioCosto\":600.00000000,\"ValorParidadCosto\":0,\"TipoMonedaPrincipal\":null,\"IdCarga\":null,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":null}],\"p_ValorizacionAutomatica\":\"N\"}";
				ControlRetiroCustodiaEntrada Objin = gson.fromJson(json, ControlRetiroCustodiaEntrada.class);
							
				ControlRetiroCustodia  kk = new ControlRetiroCustodia();
				RetiroCustodiaSalida Objout = kk.logica(con, Objin);			
				System.out.println(Objout.P_OUT_DATOSSALIDA);
				con.rollback();
				
		    }catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}

	public static BigDecimal consumirCartera(List<ObjTablaCartera> carterasDisponibles, String hash, ControlRetiroObjDatosEntrada retiro) {
		
		BigDecimal cantidadRetirada = BigDecimal.ZERO;
		BigDecimal cantidadPorRetirar = retiro.CantidadORescate;
		
		for (ObjTablaCartera cartera : carterasDisponibles) {
			if (cantidadPorRetirar.compareTo(BigDecimal.ZERO) == 0)
					break;
			//si es el mismo hash y la fecha de liquidacion ta buena, puedo retirar de la cartera
			if (getHashBusqueda(cartera.IdInstrumento,cartera.IdPortafolio,cartera.IdCliente,cartera.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")?cartera.NemoSVS:"").equalsIgnoreCase(hash)
					&& cartera.FechaLiquidacion.compareTo(retiro.FechaLiquidacion) <= 0) {
				
				//alcanzo a retirar todo lo que falta y es mayor 0 igual a 0 el resto
				if (cartera.CantidadORescateDisponible.compareTo(cantidadPorRetirar) >= 0) {
					
					BigDecimal cantidadRetirarCartera = cantidadPorRetirar;
					
					cantidadRetirada = cantidadRetirada.add(cantidadRetirarCartera);
					cantidadPorRetirar = cantidadPorRetirar.subtract(cantidadRetirarCartera);
					
					cartera.CantidadORescateDisponible = cartera.CantidadORescateDisponible.subtract(cantidadRetirarCartera);
				}
				//solo retiro lo disponible en la cartera
				else {
					
					BigDecimal cantidadRetirarCartera = cartera.CantidadORescateDisponible;
					
					cantidadRetirada = cantidadRetirada.add(cantidadRetirarCartera);
					cantidadPorRetirar = cantidadPorRetirar.subtract(cantidadRetirarCartera);
					cartera.CantidadORescateDisponible = BigDecimal.ZERO;					
				}
				
				
				
			}
		}
		
		return cantidadRetirada;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	};	
}
