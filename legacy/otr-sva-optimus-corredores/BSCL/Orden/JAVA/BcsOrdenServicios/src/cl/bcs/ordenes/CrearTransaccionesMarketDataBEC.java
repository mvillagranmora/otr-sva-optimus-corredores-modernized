package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Entrada;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Salida;

import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.TransaccionBECMarketData;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearTransaccionesMarketDataBEC implements JavaCall {
	
	private final Logger logger = LogManager.getLogger(CrearTransaccionesMarketDataBEC.class);
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private final String SQL_SELECT_CORREDORAS = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_SelectCorredoras.sql");
	private final String SQL_INSERT_TRANSACCIONES_WRAPPER = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketDataBEC_InsertTransaccionesWrapper.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketDataBEC_SelectTransacciones.sql");
	private final String SQL_SELECT_SIMULTANEA = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketDataBEC_SelectSimultanea.sql");
	private final String SQL_SELECT_LIBROS = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesMarketData_SelectLibros.sql");
	
	private final String SQL_SELECT_LIBRO_ORDENES_BY_OPERADOR = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByOperador.sql");
	private final String SQL_SELECT_LIBRO_ORDENES_BY_DEFAULT = loadResourceToString("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByDefault.sql");
	
	List<LibrosOrdenes> libroOrdenesDefault = null;
	List<LibrosOrdenes> libroOrdenesPorOperador = null;
	
	private Long obtenerLibroOrdenes(Connection conn, String codigoOperadorCorredor, Long idEmpresa) throws SQLException {
		SQL sql = new SQL();
		
		if (libroOrdenesDefault == null) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", idEmpresa);
			libroOrdenesDefault = sql.query(conn, SQL_SELECT_LIBRO_ORDENES_BY_DEFAULT, params, LibrosOrdenes.class);
		}
		
		if (libroOrdenesPorOperador == null) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", idEmpresa);
			libroOrdenesPorOperador = sql.query(conn, SQL_SELECT_LIBRO_ORDENES_BY_OPERADOR, params, LibrosOrdenes.class);	
		}
		
		if(codigoOperadorCorredor == null || codigoOperadorCorredor.equals("")) {
			return libroOrdenesDefault.get(0).IdLibroOrdenes;			
		}
		else {
			for (LibrosOrdenes conf : libroOrdenesPorOperador) {
				if (conf.CodigoOperador != null && conf.CodigoOperador.equalsIgnoreCase(codigoOperadorCorredor)) {
					return conf.IdLibroOrdenes;
				}
			}
		}		
		return libroOrdenesDefault.get(0).IdLibroOrdenes;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		return null;
	}
	
	public static class Corredora {
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
		public String IdBolsa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LibroOrdenes_Proceso {
		public String CodigoOperador;
		public Long IdLibroOrdenes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Proceso extends cl.bcs.ordenes.CrearTransacciones.Transaccion_Entrada {
		public String Nemotecnico;
		public String AccionOrden;
		public String IdTipoInstrumento;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoTransaccion {
		public Long IdInterno;
		public String Nemotecnico;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public String IdTipoReajuste;
		public BigDecimal CorteMinimo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public CrearTransaccionesMarketDataBEC_Salida logica(Connection conn,CrearTransaccionesMarketDataBEC_Entrada entrada) throws Exception {
			
		CrearTransaccionesMarketDataBEC_Salida salida = new CrearTransaccionesMarketDataBEC_Salida();
		SQL sql = new SQL();
		ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = serviceConsultarEmpresa.logica(conn, entrada.p_IdEmpresa);
		
		String operadorRuteoAgente = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "OperadorRuteoAgente");
		String operadorRuteoRetail = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "OperadorRuteoRetail");
        
		// Distincion entre palos SIM y otros
		List<TransaccionBECMarketData_Entrada> entradaTransacciones = new ArrayList<TransaccionBECMarketData_Entrada>();
		HashMap<String,TransaccionBECMarketData_Entrada> transaccionSIM = new HashMap<String,TransaccionBECMarketData_Entrada>();
		
		for (TransaccionBECMarketData_Entrada tran : entrada.transacciones) {
			if (tran.Mercado == 5) {
				transaccionSIM.put(tran.Folio+tran.Fecha, tran);
			}else {
				entradaTransacciones.add(tran);
			}
		}
        
		HashMap<String,Corredora> corredoras = new HashMap<String,Corredora>();
		{
			HashMap<String,Object> params1 = new HashMap<>();
			List<Corredora> _corredoras = sql.query(conn, SQL_SELECT_CORREDORAS, params1, Corredora.class);
			corredoras = new HashMap<>();
			for (Corredora corr : _corredoras) {
				corredoras.put(corr.CodigoCorredora, corr);
			}
		}
			
		List<Transaccion_Proceso> transacciones = new ArrayList<Transaccion_Proceso>();

		for (TransaccionBECMarketData_Entrada tran : entradaTransacciones) {
			
			//PALOS RV
			if(tran.Mercado == 0) {

				Transaccion_Proceso tx = new Transaccion_Proceso();
				tx.IdInterno = tran.IdInterno;
				Corredora corredoraCompra = corredoras.get(tran.BCSCodeComprador);
				Corredora corredoraVenta = corredoras.get(tran.BCSCodeVendedor);
				
				
				if (corredoraCompra == null) {
					String codigo = "Corredor " + tran.BCSCodeComprador + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				if (corredoraVenta == null) {
					String codigo ="Corredor " + tran.BCSCodeVendedor + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}

				if (corredoraCompra != null && empresa.IdCorredora.equals(corredoraCompra.IdCorredora)) {
					
					if (!tran.BCSCodeComprador.equalsIgnoreCase(tran.CompradorIdOpdir) && tran.CompradorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.CompradorTraderID;
					}
					
					if (!tran.BCSCodeVendedor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorContraparte = tran.VendedorIdOpdir;
					}
					else {
						tx.CodigoOperadorContraparte = tran.VendedorTraderID;
					}
					
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeVendedor).IdCorredora;
					tx.AccionOrden = _c.ACCION_COMPRA;
					tx.IdBolsa = "BEC";
				}
				else if (corredoraVenta != null && empresa.IdCorredora.equals(corredoraVenta.IdCorredora)) {
					
					if (!tran.VendedorIdCorredor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.VendedorIdOpdir;
						tx.CodigoOperadorContraparte = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.VendedorTraderID;
						tx.CodigoOperadorContraparte = tran.CompradorTraderID;
					}
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeComprador).IdCorredora;
					tx.AccionOrden = _c.ACCION_VENTA;
					tx.IdBolsa = "BEC";
				}
				else {
					// En caso de no corresponder a un palo de la corredora actual, salir
					String codigo = "Transaccion otros corredores Folio:" + tran.Folio + " Fecha:" + tran.FechaRegistro+ " C:" + tran.BCSCodeComprador + " V:" + tran.BCSCodeVendedor;
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				tx.Cantidad = BigDecimal.valueOf(tran.Cantidad);
				tx.EsBilateral  = "N";
				tx.EsCuentaPropia = "N";
				tx.EsFueraRueda = "N";
				tx.EsFungible ="N";
				
				
				if(tran.CompradorIdCorredor.equalsIgnoreCase(tran.VendedorIdCorredor)){
					if (!tran.CompradorIdOpdir.equalsIgnoreCase("") || !tran.VendedorIdOpdir.equalsIgnoreCase("")) {
						tx.IndicadorOrdenDirecta = "OE";	
					}
					else {
						tx.IndicadorOrdenDirecta = "OD";
					}
				}else {
					tx.IndicadorOrdenDirecta = "";
				}
				
				if (tran.ventaCorta) {
					tx.EsVentaCorta = "S";	
				}
				else {
					tx.EsVentaCorta = "N";
				}
				
				SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
				
				tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				
				tx.FechaEmision = "0001-01-01";
				
				tx.FechaLiquidacionContado = "0001-01-01";
				
				tx.FechaVencimiento = "0001-01-01";	
				tx.FolioTransaccion = String.valueOf(tran.Folio);
				tx.FolioTransaccionPlazo = "";
				
				tx.HoraCreacion = tran.Hora;
				
				
				// Condicion de liquidacion
				ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
				entradaObtenerPlazo.p_IdPais = empresa.IdPais;
				
				ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
				f.FechaDesde = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				f.FechaHasta = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.FechaLiquidacion)));
				f.IdInterno = tran.IdInterno;
				f.SoloDiasHabiles = "S";
				entradaObtenerPlazo.p_Fechas.add(f);
				
				Map<Integer, String> plazosMap = new HashMap<>();
				plazosMap.put(0, "T0");
				plazosMap.put(1, "T1");
				plazosMap.put(2, "T2");
				plazosMap.put(3, "T3");
				plazosMap.put(4, "T4");
				plazosMap.put(5, "T5");
	
				String CondicionLiquidacion = plazosMap.getOrDefault(tran.Plazo, null);
				tx.IdCondicionDeLiquidacionContado = CondicionLiquidacion;

				// Fin Condicion de Liquidacion
				
				tx.IdCorredoraCorresponsal = 0L;
				tx.IdEmpleado = 0L;
				tx.IdEmisor = "";
				//txe.IdInstrumento = infoTransaccion.IdInstrumento;
				tx.IdInstrumentoSubyacente = 0L;
				tx.IdInterno = tx.IdInterno;
				tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
				tx.IdMonedaLiquidacionCorresponsal = "";
				
				// Asumo que este es BEC
				tx.IdSistemaBursatil = "BEC";
				
				//txe.IdTipoOperacion;
				tx.IdTipoReajuste = "";
				tx.IdMoneda = "CLP";
				tx.IndicadorDCV =  "S"; //REVISAR DESDE TABLA DE INSTRUMENTOS
				if (tx.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
					tx.MarcaNegociacion = tran.ClientIDCompra;
					tx.MarcaNegociacionContraparte = tran.ClientIDVenta; //cuando es OD siempre procesamos como compra
				}
				else {
					tx.MarcaNegociacion = tran.ClientIDVenta;
				}
				
				
				tx.MejorPrecioCompra = BigDecimal.valueOf(tran.puntaCompraPrecio);
				tx.MejorPrecioVenta = BigDecimal.valueOf(tran.puntaVentaPrecio);
				
				tx.Monto = BigDecimal.valueOf(tran.Monto);
				tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
				tx.MontoPlazo = BigDecimal.ZERO;
				tx.Nemotecnico = tran.Instrumento;
				
				tx.NemotecnicoSVS = "";
				tx.NemotecnicoEspecificoBolson = "";
				tx.Plazo = 0L; 
				tx.Precio = BigDecimal.valueOf(tran.Precio);
				tx.PrecioPlazo = BigDecimal.valueOf(tran.PrecioPlazo);
				tx.Tasa = BigDecimal.ZERO;
				tx.TipoCambioCorresponsal = BigDecimal.ZERO;
				tx.ValorRescate = BigDecimal.ZERO;
				//consultar si la transaccion es por ruteo.
				//SpadditionaltradeinformationResponse response = serviceAddTrxInfo.spadditionaltradeinformationExecute(BigDecimal.valueOf(Long.parseLong(tx.FolioTransaccion)), tx.FechaCreacion);
				tx.EsRuteo  =/* (response.getAdditionaltradesResult()!=null && !response.getAdditionaltradesResult().isEmpty() && response.getAdditionaltradesResult().get(0).getCONDCOD012().equalsIgnoreCase("C")) ? "S" :*/ "N";
				if (tx.CodigoOperadorCorredor.equalsIgnoreCase(operadorRuteoAgente) || tx.CodigoOperadorCorredor.equalsIgnoreCase(operadorRuteoRetail)) {
					tx.EsRuteo = "S";
				}
				tx.FlujoOrigen = "INGRESOORDEN";
				transacciones.add(tx);
			}
			
			// PALOS RF
			if(tran.Mercado == 1) {
				Transaccion_Proceso tx = new Transaccion_Proceso();
				tx.IdInterno = tran.IdInterno;
				Corredora corredoraCompra = corredoras.get(tran.BCSCodeComprador);
				Corredora corredoraVenta = corredoras.get(tran.BCSCodeVendedor);
				
				if (corredoraCompra == null) {
					String codigo = "Corredor " + tran.BCSCodeComprador + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				if (corredoraVenta == null) {
					String codigo ="Corredor " + tran.BCSCodeVendedor + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
					
					if (!tran.BCSCodeComprador.equalsIgnoreCase(tran.CompradorIdOpdir) && tran.CompradorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.CompradorTraderID;
					}
					
					if (!tran.BCSCodeVendedor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorContraparte = tran.VendedorIdOpdir;
					}
					else {
						tx.CodigoOperadorContraparte = tran.VendedorTraderID;
					}
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeVendedor).IdCorredora;
					tx.AccionOrden = _c.ACCION_COMPRA;
					tx.IdBolsa = "BEC";
				}
				else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {

					if (!tran.BCSCodeVendedor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.VendedorIdOpdir;
						tx.CodigoOperadorContraparte = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.VendedorTraderID;
						tx.CodigoOperadorContraparte = tran.CompradorTraderID;
					}
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeComprador).IdCorredora;
					tx.AccionOrden = _c.ACCION_VENTA;
					tx.IdBolsa = "BEC";
				}
				else {
					String codigo = "Transaccion otros corredores Folio:" + tran.Folio + " Fecha:" + tran.FechaRegistro+ " C:" + tran.BCSCodeComprador + " V:" + tran.BCSCodeVendedor;
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				tx.Cantidad = BigDecimal.valueOf(tran.Cantidad);
				tx.EsBilateral  = "N";
				tx.EsCuentaPropia = "N";
				tx.EsFueraRueda = "N";
				tx.EsFungible ="N";
				
				if(tran.CompradorIdCorredor.equalsIgnoreCase(tran.VendedorIdCorredor)){

					if (!tran.CompradorIdOpdir.equalsIgnoreCase("") || !tran.VendedorIdOpdir.equalsIgnoreCase("")) {
						tx.IndicadorOrdenDirecta = "OE";	
					}
					else {
						tx.IndicadorOrdenDirecta = "OD";
					}
				}else {
					tx.IndicadorOrdenDirecta = "";
				}
				
				tx.EsVentaCorta = "N";

				SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
				
				tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				
				tx.FechaEmision = "0001-01-01";
				
				SimpleDateFormat sdfSettleDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfSettleDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tx.FechaLiquidacionContado = sdfSettleDateOUT.format(sdfSettleDateIN.parse(tran.FechaLiquidacion));
				
				tx.FechaVencimiento = "0001-01-01";	
				tx.FolioTransaccion = String.valueOf(tran.Folio);
				tx.FolioTransaccionPlazo = "";
				
				tx.HoraCreacion = tran.Hora; 
		
				// Condicion de Liquidacion
				ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
				entradaObtenerPlazo.p_IdPais = empresa.IdPais;
				
				ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
				f.FechaDesde = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				f.FechaHasta = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.FechaLiquidacion)));
				f.IdInterno = tran.IdInterno;
				f.SoloDiasHabiles = "S";
				entradaObtenerPlazo.p_Fechas.add(f);
		
	
				ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
				OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(conn, entradaObtenerPlazo);
				for (cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut fecha : salidaObtenerPlazo.p_out_Fechas) {
					tran.Plazo = fecha.Plazo.intValue();
				}
				
				
				Map<Integer, String> plazosMap = new HashMap<>();
				plazosMap.put(0, "T0");
				plazosMap.put(1, "T1");
				plazosMap.put(2, "T2");
				plazosMap.put(3, "T3");
				plazosMap.put(4, "T4");
				plazosMap.put(5, "T5");
	
				String CondicionLiquidacion = plazosMap.getOrDefault(tran.Plazo, null);
				tx.IdCondicionDeLiquidacionContado = CondicionLiquidacion;
				// Fin Condicion de Liquidacion

				tx.IdCorredoraCorresponsal = 0L;
				tx.IdEmpleado = 0L;
				tx.IdEmisor = "";
				//txe.IdInstrumento = infoTransaccion.IdInstrumento;
				tx.IdInstrumentoSubyacente = 0L;
				tx.IdInterno = tx.IdInterno;
				tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
				tx.IdMonedaLiquidacionCorresponsal = "";
							
				// Asumo que este es BEC
				tx.IdSistemaBursatil = "BEC";
				
				//txe.IdTipoOperacion;
				tx.IdTipoReajuste = "";
				tx.IdMoneda = "CLP";
				tx.IndicadorDCV =  "S"; // No se usa
				tx.MarcaNegociacion = "";
				tx.MejorPrecioCompra = BigDecimal.ZERO;
				tx.MejorPrecioVenta = BigDecimal.ZERO;
				
				tx.Monto = BigDecimal.valueOf(tran.Monto);
				tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
				tx.MontoPlazo = BigDecimal.ZERO;
				if (tran.Instrumento.startsWith("CERO") ) {
					tx.Nemotecnico = "CERO";
				}
				else if (tran.Instrumento.startsWith("XERO")) {
					tx.Nemotecnico = "XERO";
				}
				else if (tran.Instrumento.startsWith("ZERO")) {
					tx.Nemotecnico = "ZERO";
				}
				else if (tran.Instrumento.startsWith("BR")) {
					tx.Nemotecnico = "BR";
				}
				else {
					tx.Nemotecnico = tran.Instrumento;	
				}
				
				tx.NemotecnicoSVS = "";
				tx.NemotecnicoEspecificoBolson = tran.Instrumento;
				tx.Plazo = 0L;
				tx.Precio = BigDecimal.valueOf(tran.ValorPar); // ValorPar es el Precio para RF
				tx.PrecioPlazo = BigDecimal.ZERO;
				tx.Tasa = BigDecimal.valueOf(tran.Precio); // Precio es la tasa para RF
				tx.TipoCambioCorresponsal = BigDecimal.ZERO;
				tx.ValorRescate = BigDecimal.ZERO;
				tx.FlujoOrigen = "INGRESOORDEN";
				
				transacciones.add(tx);
			}
			
			// PALOS IF
			if(tran.Mercado == 2 || tran.Mercado == 4) {
				Transaccion_Proceso tx = new Transaccion_Proceso();
				tx.IdInterno = tran.IdInterno;
				Corredora corredoraCompra = corredoras.get(tran.BCSCodeComprador);
				Corredora corredoraVenta = corredoras.get(tran.BCSCodeVendedor);
				
				if (corredoraCompra == null) {
					String codigo = "Corredor " + tran.BCSCodeComprador + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				if (corredoraVenta == null) {
					String codigo ="Corredor " + tran.BCSCodeVendedor + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
					
					if (!tran.BCSCodeComprador.equalsIgnoreCase(tran.CompradorIdOpdir) && tran.CompradorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.CompradorTraderID;
					}
					
					if (!tran.BCSCodeVendedor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorContraparte = tran.VendedorIdOpdir;
					}
					else {
						tx.CodigoOperadorContraparte = tran.VendedorTraderID;
					}
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeVendedor).IdCorredora;
					tx.AccionOrden = _c.ACCION_COMPRA;
				}
				else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {

					if (!tran.BCSCodeVendedor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.VendedorIdOpdir;
						tx.CodigoOperadorContraparte = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.VendedorTraderID;
						tx.CodigoOperadorContraparte = tran.CompradorTraderID;
					}
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeComprador).IdCorredora;
					tx.AccionOrden = _c.ACCION_VENTA;
				}
				else {
					String codigo = "Transaccion otros corredores Folio:" + tran.Folio + " Fecha:" + tran.FechaRegistro+ " C:" + tran.BCSCodeComprador + " V:" + tran.BCSCodeVendedor;
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				if (tran.Instrumento.equalsIgnoreCase("PRBC") || tran.Instrumento.equalsIgnoreCase("PDBC")) {
					tx.Cantidad = BigDecimal.ZERO;  //mas abajo se calcula
				}
				else {
					tx.Cantidad = BigDecimal.ONE;
				}
				
				tx.EsBilateral  = "N";
				tx.EsCuentaPropia = "N";
				tx.EsFueraRueda = "N";
				if (tran.Instrumento.equalsIgnoreCase("PRBC") || tran.Instrumento.equalsIgnoreCase("PDBC")) {
					tx.EsFungible ="N";
				}
				else {
					tx.EsFungible = "S";	
				}
				
				if(tran.CompradorIdCorredor.equalsIgnoreCase(tran.VendedorIdCorredor)){
					
					// SI UNO ES OPERADOR EXTERNO => OE / SINO OD
					if (!tran.CompradorIdOpdir.equalsIgnoreCase("") || !tran.VendedorIdOpdir.equalsIgnoreCase("")) {
						tx.IndicadorOrdenDirecta = "OE";	
					}
					else {
						tx.IndicadorOrdenDirecta = "OD";
					}
				}else {
					tx.IndicadorOrdenDirecta = "";
				}
				
				
				tx.EsVentaCorta = "N";
				SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
				
				tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				
				tx.FechaEmision = "0001-01-01";
				
				SimpleDateFormat sdfSettleDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfSettleDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tx.FechaLiquidacionContado = sdfSettleDateOUT.format(sdfSettleDateIN.parse(tran.FechaLiquidacion));

				SimpleDateFormat sdfExpireTimeIN = new SimpleDateFormat("yyyyMMdd");
		        Calendar calendar = Calendar.getInstance();
		        calendar.setTime(sdfExpireTimeIN.parse(tran.Fecha));
		        calendar.add(Calendar.DAY_OF_YEAR, Integer.valueOf(tran.PlazoDias));
		        
				tx.FechaVencimiento = sdfSettleDateOUT.format(sdfSettleDateIN.parse(tran.FechaLiquidacion));

				tx.FolioTransaccion = String.valueOf(tran.Folio);
				tx.FolioTransaccionPlazo = "";

				tx.HoraCreacion = tran.Hora;
				tx.IdBolsa = "BEC";
				
				// Condicion de Liquidacion
				ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
				entradaObtenerPlazo.p_IdPais = empresa.IdPais;
				
				ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
				f.FechaDesde = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				f.FechaHasta = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.FechaLiquidacion)));
				f.IdInterno = tran.IdInterno;
				f.SoloDiasHabiles = "S";
				entradaObtenerPlazo.p_Fechas.add(f);
				
				Map<Integer, String> plazosMap = new HashMap<>();
				plazosMap.put(0, "T0");
				plazosMap.put(1, "T1");
				plazosMap.put(2, "T2");
				plazosMap.put(3, "T3");
				plazosMap.put(4, "T4");
				plazosMap.put(5, "T5");
	
				String CondicionLiquidacion = plazosMap.getOrDefault(tran.Plazo, null);
				tx.IdCondicionDeLiquidacionContado = CondicionLiquidacion;
				// Fin Condicion de Liquidacion
				
				tx.IdCorredoraCorresponsal = 0L;
				tx.IdEmpleado = 0L;
				tx.IdEmisor = tran.EmisorNombre;
				//txe.IdInstrumento = infoTransaccion.IdInstrumento;
				tx.IdInstrumentoSubyacente = 0L;
				tx.IdInterno = tx.IdInterno;
				tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
				tx.IdMonedaLiquidacionCorresponsal = "";
				
				tx.IdSistemaBursatil = "BEC";

				tx.Monto = BigDecimal.valueOf(tran.Monto); //DEPENDIENDO DEL TIPO DE REAJUSTE REPASAMOS EL MONTO
				if (tran.Reajustabilidad.equalsIgnoreCase("NR")) {
					tx.IdTipoReajuste = "CLP";
					tx.IdMoneda = "CLP";
					tx.IdMonedaEmision = "CLP";
				}else if (tran.Reajustabilidad.equalsIgnoreCase("UF")) {
					tx.IdTipoReajuste = "UF";
					tx.IdMoneda = "CLP";
					tx.IdMonedaEmision = "UF";
				}else if (tran.Reajustabilidad.equalsIgnoreCase("US$")) {
					//PARA DOLAR VIENE US$ AGREGAR
					tx.IdTipoReajuste = "USD";
					tx.IdMoneda = "CLP";
					tx.IdMonedaEmision = "USD";
				}
				else {
					tx.IdTipoReajuste = null;
					tx.IdMonedaEmision = null;
					tx.IdMoneda = null;
				}
				
				tx.IndicadorDCV =  "S";
				tx.MarcaNegociacion = "";
				tx.MejorPrecioCompra = BigDecimal.ZERO;
				tx.MejorPrecioVenta = BigDecimal.ZERO;
				
				tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
				tx.MontoPlazo = BigDecimal.ZERO;

				// PARA NEMOTECNICO
				// SI CONTIENE PDBC -> ES PDBC
				// si reajuste es NR -> EL NEMO ES PAGARE NR
				// EL RESTO ES PAGARE R
				
				if (tran.Instrumento.toUpperCase().contains("PDBC")) {
				    tx.Nemotecnico = "PDBC";
				}else if (tran.Reajustabilidad.equalsIgnoreCase("NR")) {
					tx.Nemotecnico = "PAGARE NR";
				}else {
					tx.Nemotecnico = "PAGARE R";
				}
				
				tx.NemotecnicoSVS = tran.Instrumento;
				tx.Plazo = new Long(tran.PlazoDias);
				tx.Precio = BigDecimal.ZERO;
				tx.PrecioPlazo = BigDecimal.ZERO;
				tx.Tasa = BigDecimal.valueOf(tran.Precio);
				
				tx.TipoCambioCorresponsal = BigDecimal.ZERO;
				tx.ValorRescate = BigDecimal.valueOf(tran.Cantidad);
				tx.FlujoOrigen = "INGRESOORDEN";
				
				transacciones.add(tx);
				
			}
			
		}
		
		if(!transaccionSIM.isEmpty()) {
			
			List<TransaccionBECMarketData_Entrada> entradaTransaccionesSIM = new ArrayList<TransaccionBECMarketData_Entrada>();;
			Iterator<Map.Entry<String, TransaccionBECMarketData_Entrada>> iterator = transaccionSIM.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, TransaccionBECMarketData_Entrada> entry = iterator.next();
				TransaccionBECMarketData_Entrada aux = entry.getValue();
				if(aux.Plazo<=2) {
					transaccionSIM.get(aux.Folio+aux.Fecha).MontoPlazo = transaccionSIM.get(aux.FolioRelacionado+aux.Fecha).Monto;
					transaccionSIM.get(aux.Folio+aux.Fecha).PrecioPlazo = transaccionSIM.get(aux.FolioRelacionado+aux.Fecha).Precio;        
					transaccionSIM.get(aux.Folio+aux.Fecha).PlazoDias = String.valueOf(transaccionSIM.get(aux.FolioRelacionado+aux.Fecha).Plazo);
					transaccionSIM.get(aux.Folio+aux.Fecha).FechaLiquidacionPlazo = transaccionSIM.get(aux.FolioRelacionado+aux.Fecha).FechaLiquidacion;        
				}else {
					continue;
				}
				
				entradaTransaccionesSIM.add(transaccionSIM.get(aux.Folio+aux.Fecha));
			}
			
			for (TransaccionBECMarketData_Entrada tran : entradaTransaccionesSIM) {
				Transaccion_Proceso tx = new Transaccion_Proceso();
				tx.IdInterno = tran.IdInterno;
				Corredora corredoraCompra = corredoras.get(tran.BCSCodeComprador);
				Corredora corredoraVenta = corredoras.get(tran.BCSCodeVendedor);
				
				if (corredoraCompra == null) {
					String codigo = "Corredor " + tran.BCSCodeComprador + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				if (corredoraVenta == null) {
					String codigo ="Corredor " + tran.BCSCodeVendedor + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
					
					if (!tran.BCSCodeComprador.equalsIgnoreCase(tran.CompradorIdOpdir) && tran.CompradorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.CompradorTraderID;
					}
					
					if (!tran.BCSCodeVendedor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorContraparte = tran.VendedorIdOpdir;
					}
					else {
						tx.CodigoOperadorContraparte = tran.VendedorTraderID;
					}
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeVendedor).IdCorredora;
					tx.AccionOrden = _c.ACCION_COMPRA;
					tx.IdBolsa = "BEC";
				}
				else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {
				
					if (!tran.BCSCodeVendedor.equalsIgnoreCase(tran.VendedorIdOpdir) && tran.VendedorIdOpdir.trim().length() > 0) {
						tx.CodigoOperadorCorredor = tran.VendedorIdOpdir;
						tx.CodigoOperadorContraparte = tran.CompradorIdOpdir;
					}
					else {
						tx.CodigoOperadorCorredor = tran.VendedorTraderID;
						tx.CodigoOperadorContraparte = tran.CompradorTraderID;
					}
					
					tx.IdCorredoraContraparte = corredoras.get(tran.BCSCodeComprador).IdCorredora;
					tx.AccionOrden = _c.ACCION_VENTA;
					tx.IdBolsa = "BEC";
				}
				else {
					String codigo = "Transaccion otros corredores Folio:" + tran.Folio + " Fecha:" + tran.FechaRegistro + " C:" + tran.BCSCodeComprador + " V:" + tran.BCSCodeVendedor;
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo);
					salida.P_OUT_MENSAJES.add(msj);
					logger.debug(msj);
					continue;
				}
				
				tx.Cantidad = BigDecimal.valueOf(tran.Cantidad);
				tx.EsBilateral  = "N";
				tx.EsCuentaPropia = "N";
				tx.EsFueraRueda = "N";
				tx.EsFungible ="N";
				
				
				if(tran.CompradorIdCorredor.equalsIgnoreCase(tran.VendedorIdCorredor)){
					
					// SI UNO ES OPERADOR EXTERNO => OE / SINO OD
					if (!tran.CompradorIdOpdir.equalsIgnoreCase("") || !tran.VendedorIdOpdir.equalsIgnoreCase("")) {
						tx.IndicadorOrdenDirecta = "OE";	
					}
					else {
						tx.IndicadorOrdenDirecta = "OD";
					}
				}else {
					tx.IndicadorOrdenDirecta = "";
				}
				
				tx.EsVentaCorta = "N";
				
				SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
				
				tx.FechaCreacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				
				tx.FechaEmision = "0001-01-01";
				
				tx.FechaLiquidacionContado = "0001-01-01";
				
				SimpleDateFormat sdfExpirationDateIN = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdfExpirationDateOUT = new SimpleDateFormat("yyyy-MM-dd");
				tx.FechaVencimiento = sdfExpirationDateOUT.format(sdfExpirationDateIN.parse(tran.FechaLiquidacionPlazo));
				
				tx.FolioTransaccion = String.valueOf(tran.Folio);
				tx.FolioTransaccionPlazo = String.valueOf(tran.FolioRelacionado);
				
				tx.HoraCreacion = tran.Hora;
				
				// Condicion de Liquidacion
				ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
				entradaObtenerPlazo.p_IdPais = empresa.IdPais;
				
				ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
				f.FechaDesde = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.Fecha)));
				f.FechaHasta = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tran.FechaLiquidacion)));
				f.IdInterno = tran.IdInterno;
				f.SoloDiasHabiles = "S";
				entradaObtenerPlazo.p_Fechas.add(f);
		

				ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
				OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(conn, entradaObtenerPlazo);
				for (cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut fecha : salidaObtenerPlazo.p_out_Fechas) {
					tran.Plazo = tran.Plazo==0L?fecha.Plazo.intValue():tran.Plazo;
				}
				
				Map<Integer, String> plazosMap = new HashMap<>();
				plazosMap.put(0, "T0");
				plazosMap.put(1, "T1");
				plazosMap.put(2, "T2");
				plazosMap.put(3, "T3");
				plazosMap.put(4, "T4");
				plazosMap.put(5, "T5");

				String CondicionLiquidacion = plazosMap.getOrDefault(tran.Plazo, null);
				tx.IdCondicionDeLiquidacionContado = CondicionLiquidacion;
				// Fin Condicion de Liquidacion

				tx.IdCorredoraCorresponsal = 0L;
				tx.IdEmpleado = 0L;
				tx.IdEmisor = "";
				//txe.IdInstrumento = infoTransaccion.IdInstrumento;
				tx.IdInstrumentoSubyacente = 0L;
				tx.IdInterno = tx.IdInterno;
				tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, tx.CodigoOperadorCorredor, entrada.p_IdEmpresa);
				tx.IdMonedaLiquidacionCorresponsal = "";
				tx.IdSistemaBursatil = "BEC";
				
				//txe.IdTipoOperacion;
				tx.IdTipoInstrumento = "SIM";			
				
				tx.IdTipoReajuste = "";
				tx.IdMoneda = "CLP";
				tx.IndicadorDCV =  "S"; //REVISAR DESDE TABLA DE INSTRUMENTOS
				
				if (tx.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
					tx.MarcaNegociacion = tran.ClientIDCompra;
					tx.MarcaNegociacionContraparte = tran.ClientIDVenta; //cuando es OD siempre procesamos como compra
				}
				else {
					tx.MarcaNegociacion = tran.ClientIDVenta;
				}
							
				tx.MejorPrecioCompra = BigDecimal.ZERO;
				tx.MejorPrecioVenta = BigDecimal.ZERO;
				
				tx.Monto = BigDecimal.valueOf(tran.Monto);
				tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
				
				tx.MontoPlazo = BigDecimal.valueOf(tran.MontoPlazo);
				tx.Nemotecnico = tran.Instrumento;	
				
				tx.NemotecnicoSVS = "";
				tx.NemotecnicoEspecificoBolson = "";
				tx.Plazo = -1L; //-1 PARA QUE EL SERVICIO LO CALCULE
				tx.Precio = BigDecimal.valueOf(tran.Precio);
				tx.PrecioPlazo = BigDecimal.valueOf(tran.PrecioPlazo); // PRECIO * (1 + VARIACION)/100
//				tx.PrecioPlazo = BigDecimal.valueOf(tran.Precio * ((1 + tran.Variacion)/100)); // PRECIO * (1 + VARIACION)/100
				tx.Tasa = BigDecimal.valueOf(tran.Variacion);
				tx.TipoCambioCorresponsal = BigDecimal.ZERO;
				tx.ValorRescate = BigDecimal.ZERO;
				tx.FlujoOrigen = "INGRESOORDEN";
				
				transacciones.add(tx);
			}
		}
				
		if (transacciones.size() == 0) {
			return salida;
		}

		sql.batch(conn, SQL_INSERT_TRANSACCIONES_WRAPPER, transacciones, Transaccion_Proceso.class);
		
		//QUERY TIPO OPERACION, DIAS LIQUIDACION
		List<InfoTransaccion> _infoTransacciones = sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<>(), InfoTransaccion.class);
		HashMap<Long,Object> infoTransacciones = Utils.List2Hash(_infoTransacciones, "IdInterno");
		
		List<InfoTransaccion> _infoTransaccionesSimultanea = sql.query(conn, SQL_SELECT_SIMULTANEA, new HashMap<>(), InfoTransaccion.class);
		HashMap<Long,Object> infoTransaccionesSimultanea = Utils.List2Hash(_infoTransaccionesSimultanea, "IdInterno");
		
		HashMap<String,Object> params1 = new HashMap<>();
		params1.put("idEmpresa", entrada.p_IdEmpresa);
		List<LibroOrdenes_Proceso> libros = sql.query(conn, SQL_SELECT_LIBROS, params1, LibroOrdenes_Proceso.class);
		
		for (Transaccion_Proceso tx : transacciones) {
			
			InfoTransaccion info = (InfoTransaccion) infoTransacciones.get(tx.IdInterno);
			
			if (info == null) {
				String codigo = "Error al obtener instrumento " + tx.Nemotecnico;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo));
				continue;
			}
			
			//SIMULTANEAS LAS MARCAMOS MAS ARRIBA
			if (tx.IdTipoInstrumento != null && tx.IdTipoInstrumento.equalsIgnoreCase("SIM")) {
				
				InfoTransaccion infoSim = (InfoTransaccion) infoTransaccionesSimultanea.get(tx.IdInterno);
				if (infoSim == null) {
					String codigo = "Error al obtener instrumento simultanea";
					salida.P_OUT_MENSAJES.add(new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,codigo));
					logger.debug(salida.P_OUT_MENSAJES.toString());
					continue;
				}
				
				tx.IdTipoOperacion = infoSim.IdTipoOperacion;
				tx.IdInstrumento = infoSim.IdInstrumento;
				tx.IdInstrumentoSubyacente = info.IdInstrumento;
				tx.MontoPlazo = MonedaFormato.formatear(conn, tx.MontoPlazo, MonedaFormato.MONTO, tx.IdMoneda).valor;
			}
			else {
				tx.IdTipoOperacion = info.IdTipoOperacion;
				tx.IdTipoInstrumento = info.IdTipoInstrumento;
				tx.IdInstrumento = info.IdInstrumento;
			}
			
			if (tx.Nemotecnico.equalsIgnoreCase("PDBC") || tx.Nemotecnico.equalsIgnoreCase("PRBC")) {
				tx.Cantidad = tx.ValorRescate.divide(info.CorteMinimo);
			}
			
			for (LibroOrdenes_Proceso lib : libros) {
				if (lib.CodigoOperador.equalsIgnoreCase(tx.CodigoOperadorCorredor)) {					
					//tx.IdLibroOrdenes = lib.IdLibroOrdenes;
					tx.IdLibroOrdenes = obtenerLibroOrdenes(conn, lib.CodigoOperador, entrada.p_IdEmpresa);
					break;
				}
			}
			tx.Intereses = BigDecimal.ZERO;
			tx.Reajustes = BigDecimal.ZERO;
			tx.GastosCorresponsal = BigDecimal.ZERO;
			tx.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			tx.TipoCambioCorresponsal = BigDecimal.ZERO;
		}
		
		CrearTransacciones serviceCrearTransacciones = new CrearTransacciones();
		
		CrearTransacciones_Entrada entradaCrearTransacciones = new CrearTransacciones_Entrada();
		entradaCrearTransacciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTransacciones.p_IdModulo = _c.ID_MODULO_ORDENES;
		entradaCrearTransacciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTransacciones.p_simularOperacion = _c.NO;
		entradaCrearTransacciones.p_UsaReglasNegocio = _c.SI;
		for (Transaccion_Proceso tx : transacciones) {
			if(tx.IdTipoOperacion == null) {
				System.out.println("Entre");
			}
			entradaCrearTransacciones.p_Transacciones.add(tx);
		}
		CrearTransacciones_Salida salidaCrearTransacciones = serviceCrearTransacciones.logica(conn, entradaCrearTransacciones );
		
		for (cl.bcs.ordenes.CrearTransacciones.Resultado_Salida resp : salidaCrearTransacciones.P_OUT_RESULTADO) {
			Resultado_Salida resp2 = new Resultado_Salida();
			Utils.copyPojo(resp, resp2);
			salida.P_OUT_RESULTADO.add(resp2);
		}
		
		for (MensajeSalida resp : salidaCrearTransacciones.P_OUT_MENSAJES) {
			MensajeSalida resp2 = new MensajeSalida();
			Utils.copyPojo(resp, resp2);
			salida.P_OUT_MENSAJES.add(resp2);
		}
		
		return salida;
	}
	
	public static class TransaccionBECMarketData_Entrada extends TransaccionBECMarketData {
		public Long IdInterno;

	}
	
	public static class TransaccionBECMarketDataSIM_Entrada extends TransaccionBECMarketData {
		public Long IdInterno;
	}
	

	public static class CrearTransaccionesMarketDataBEC_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<TransaccionBECMarketData_Entrada> transacciones;
		
		public CrearTransaccionesMarketDataBEC_Entrada() {
			this.transacciones = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdTransaccion;
		public String IdTipoOperacion;
		public String EsAPlazo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearTransaccionesMarketDataBEC_Salida {
		
		public List<Resultado_Salida> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public CrearTransaccionesMarketDataBEC_Salida() {
			this.P_OUT_RESULTADO = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LibrosOrdenes {
		//public Long IdLibroOrdenesOperador;
		public Long IdEmpresa;
		public Long IdLibroOrdenes;
		public String CodigoOperador;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
			

		}	
	}
}
	
