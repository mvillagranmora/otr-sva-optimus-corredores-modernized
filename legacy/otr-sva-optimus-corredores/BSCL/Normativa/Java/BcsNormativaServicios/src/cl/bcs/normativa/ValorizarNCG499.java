package cl.bcs.normativa;

import java.awt.print.Printable;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.utils.ValorizarInstrumento;

import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarNCG499 implements JavaCall { 
	static final Logger logger = LogManager.getLogger(ValorizarNCG499.class.getName());
	
	public static class ObjInstrumentoAValorizar {
		public Long					IdValorizacion;
		public String				FechaValorizacion;
		public String				TablaOrigen;
		public Long					IdCustodia;
		public Long					IdCliente; 
		public Long					IdPortafolio;
		public String				EsRegistroNuevo;
		public String				GenerarNuevoFolioCustodia;
		public String				OrigenProcesamiento;
		
		public String				FechaOperacion;
		public String				FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion; // Solo para operaciones a plazo O iif
		public String				IdCondicionDeLiquidacion;
		public String				FechaDeLiquidacion;	
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				FechaVencimiento;
		public String				FechaEmision;
		public String				TieneCorteDeCupon;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public BigDecimal			Cantidad; 
		//public BigDecimal			ValorRescate;		
		public BigDecimal			MontoCompra; // En moneda del instrumento ( se usa para el calculo de interes)
		public BigDecimal			MontoCompraMonedaSistema;
		public BigDecimal			MontoMercadoMonedaSistema;
		public BigDecimal			MontoCompraSubyacente;
		public BigDecimal			MontoCompraGarantia;
		
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra;
		public BigDecimal			PrecioMercado; 
		public BigDecimal			TasaMercado;		
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia;
		public BigDecimal			TasaTIF;
		public BigDecimal			TasaCorredora;

		// MSH
		public String				Emisor;
		public String				NemoSVS;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	public static class ObjResultadoValorizacion {
		public Long					IdProcesamiento;
		public Long					IdValorizacion;
		public String				TipoValorizacion; 
		public String				SubTipoValorizacion;
		public String				TipoValorizacionInstrumento;
		public String				IdUso; 
		public String				TipoCartera; 
		public String				IdTipoCriterioTasa;
		public java.sql.Date		FechaValorizacion;
		public String				DimensionTiempo; // AN - AC - PR 
		public String				DimensionTasa; // COMP - MERC - GTIA - SUBY - CORR - TIFF - P+CM - P+MC - P+GT - P+SY - P-CM - P-MC - P-GT - P-SY   		
		public java.sql.Date		FechaDimemsion;
		public String				ResultadoProceso;
		public String				MensajeProceso;
		public Long					TicketProcesamiento;

		public java.sql.Date		FechaOperacion;

		public String				IdClasificacionTipoInstrumento;		
		public Long					IdInstrumento; 
		public String				Nemotecnico;
		public String				Emisor;
		public BigDecimal			Cantidad;

	
		public String				MensajeErrorFuente;
		public java.sql.Date		FechaPrecioTasa; // Fecha de la tasa mercado obtenida 
		public String				FuentePrecioTasa; // Fuente de la tasa mercado obtenida		
		public BigDecimal			Precio;
		public BigDecimal			Tasa;
		public BigDecimal			Tasa360;
		public BigDecimal			PrecioAPlazo;		
		public BigDecimal			TasaAPlazo;	 						
		public String				CalculaIntereses;
		public String				CalculaInteresesAPlazo;

		public String				IdMonedaSistema;
		public BigDecimal			MontoMonedaSistema;								
		public BigDecimal			MontoInteresMonedaSistema;				
		public BigDecimal			MontoInteresAcumuladoMonedaSistema;
		public BigDecimal			MontoReajusteMonedaSistema;				
		public BigDecimal			MontoReajusteAcumuladoMonedaSistema;
		public BigDecimal			MontoDevengadoAMonedaSistema;		
		public BigDecimal			MontoAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaSistema;

		public String				IdMonedaInstrumento;
		public BigDecimal			FactorMonedaInstrumento; 
		public java.sql.Date		FechaFactorMonedaInstrumento;
		public BigDecimal			MontoMonedaInstrumento;							// Total (mon + interes y reajuste)		
		public BigDecimal			MontoInteresMonedaInstrumento;					// Interes diario
		public BigDecimal			MontoInteresAcumuladoMonedaInstrumento;			// Interes desde el ingreso
		public BigDecimal			MontoReajusteMonedaInstrumento;
		public BigDecimal			MontoReajusteAcumuladoMonedaInstrumento;
		public BigDecimal			MontoDevengadoAMonedaInstrumento;				// Diferencia entre ma y hoy		
		public BigDecimal			MontoAPlazoMonedaInstrumento;					// Desde CalculoInteres
		public BigDecimal			MontoInteresAPlazoMonedaInstrumento;		
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaInstrumento;

		public String				IdMonedaAdicional;
		public BigDecimal			FactorMonedaAdicional; 
		public java.sql.Date		FechaFactorMonedaAdicional;
		public BigDecimal			MontoMonedaAdicional;				
		public BigDecimal			MontoInteresMonedaAdicional;
		public BigDecimal			MontoInteresAcumuladoMonedaAdicional;
		public BigDecimal			MontoReajusteMonedaAdicional;
		public BigDecimal			MontoReajusteAcumuladoMonedaAdicional;
		public BigDecimal			MontoDevengadoAMonedaAdicional;
		public BigDecimal			MontoAPlazoMonedaAdicional;
		public BigDecimal			MontoInteresAPlazoMonedaAdicinal;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaAdicional;

		public String				TieneCorteDeCupon;
		public java.sql.Date		FechaCorteCupon;
		public java.sql.Date		FechaPagoCorteCupon;
		public BigDecimal			MontoInteresesPagoCorteCupon;
		public Long					NumeroCupon; 
		public String				IdMonedaPagoCupon;		 

		public BigDecimal			MontoCorteCuponMonedaSistema;				
		public BigDecimal			MontoCapitalCorteCuponMonedaSistema;		
		public BigDecimal			MontoInteresCorteCuponMonedaSistema; 		
		public BigDecimal			MontoReajusteCorteCuponMonedaSistema;		

		public BigDecimal			MontoCorteCuponMonedaInstrumento;			// Monto amortizacion + interes
		public BigDecimal			MontoCapitalCorteCuponMonedaInstrumento;	// Monto total sin interes
		public BigDecimal			MontoInteresCorteCuponMonedaInstrumento;	// Interes periodo corte
		public BigDecimal			MontoReajusteCorteCuponMonedaInstrumento;	// Bono reajustado segun la inflacion

		public BigDecimal			MontoCorteCuponMonedaAdicional; 
		public BigDecimal			MontoCapitalCorteCuponMonedaAdicional; 
		public BigDecimal			MontoInteresCorteCuponMonedaAdicional; 
		public BigDecimal			MontoReajusteCorteCuponMonedaAdicional;

		public BigDecimal			Duration;
		public BigDecimal			Convexidad;
		public BigDecimal			PlazoValorizador;
		public String				ValorTieneCall;
		public BigDecimal			TasaEmision;
		public String				ValorClasificacionRiesgo;

		public String				IdTipoReajuste;
		public BigDecimal			ValorTipoReajuste;		
		public String				NemoOriginal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class DatosNCG{
		//String FechaInforme;
		public Long 		IdValorizacion;
		public String 		LinDet;
		public String 		Producto;
		public String 		Mercado;
		public String 		FechaInicio;
		public String 		FechaVencimiento;
		public Long 		Plazo;
		public String 		RutCliente;
		public String 		RutEmisor;
		public String 		MonedaEmisor;
		public String 		MonedaPacto;
		public BigDecimal 	Tasatransferencia;
		public BigDecimal 	TasaTransaccion;
		public BigDecimal 	TasaOperacion;
		public String 		BASTAS;
		public String 		Sucursal;
		public BigDecimal 	MontoCompraVenta;
		public BigDecimal 	Reajuste;
		public BigDecimal 	MontoTransaccion;
		public BigDecimal 	Comision;
		public BigDecimal 	UtilidaPerdidaDiferenciaPrecio;
		public BigDecimal 	PrecioCompraVenta;
		public BigDecimal 	UtilidadPerdidaOperacionCambio;
		public BigDecimal 	UtilidadPerdidaSimultanea;
		
		public BigDecimal	ValMontoMonedaInstrumento;
		public BigDecimal 	ValMontoMonedaSistema;
		
		public BigDecimal 	ValMontoMonedaInstrumentoFechaVenta;
		public BigDecimal 	ValMontoMonedaSistemaFechaVenta;
		
		public BigDecimal 	ValMontoDevengadoAMonedaInstrumento;
		public BigDecimal 	ValMontoDevengadoAMonedaSistema;
		
		public String MensajeErrorFuente;
		
		public Long					IdInstrumento; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public String				Nemotecnico; 
		public String				InsFechaVencimiento;
		public String				InsFechaEmision;
		public String				TieneCorteDeCupon;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public String				Emisor;
		public String				IdCondicionDeLiquidacion;
		public String				FechaDeLiquidacion;	
		public BigDecimal			Cantidad; 
		//public BigDecimal			ValorRescate;		
		public BigDecimal			MontoCompra; // En moneda del instrumento ( se usa para el calculo de interes)
		public BigDecimal			MontoCompraMonedaSistema;
		public BigDecimal			MontoMercadoMonedaSistema;
		public BigDecimal			MontoCompraSubyacente;
		public BigDecimal			MontoCompraGarantia;
		public String				IdTipoOperacion;
		
		//public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra;
		public BigDecimal			PrecioMercado; 
		public BigDecimal			TasaMercado;		
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia;
		public BigDecimal			TasaTIF;
		public BigDecimal			TasaCorredora;
		public String				FechaOperacion;
		public String				FechaValorizacion;
		public String			    FechaVencimientoOperacion;
		public String				Situacion;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosNCG() {};
	}

	public static class ValorizarNCGEntrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public Long	p_IdPais;
		public String p_IdModulo;
		public String p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ValorizarNCGSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	public static class ResultadoValorizacion {
		public Long	IdValorizacion;
		public String IndicadorValorizado;
		public String ValResultado;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	private static FileLoader loader = new FileLoader();
	private static String SQL_SELECT_NCG = loader.loadResourceToString("cl/bcs/normativa/sql/ValorizarNCG499_SelectCircular_RFIF.sql");
	private static String SQL_UPDATE_ESTADO_NCG = loader.loadResourceToString("cl/bcs/normativa/sql/ValorizarNCG499_UpdateEstado.sql");
	private static String SQL_UPDATE_INDICADOR_NCG = loader.loadResourceToString("cl/bcs/normativa/sql/ValorizarNCG499_UpdateIndicador.sql");
	private static String SQL_INSERT_RFIF_MANT_NCG = loader.loadResourceToString("cl/bcs/normativa/sql/ValorizarNCG499_InsertRFIF_MANT.sql");
	private static String SQL_UPDATE_NCG = loader.loadResourceToString("cl/bcs/normativa/sql/ValorizarNCG499_UpdateCircular.sql");
	private static String SQL_UPDATE_RESULTADO_VALORIZACION = "UPDATE \"BSCL.Custodia.BD::CUS.CircularCarteraCMF\" SET \"IndicadorValorizado\"=:IndicadorValorizado, \"ValResultado\"=:ValResultado WHERE \"IdCircularCartera\"=:IdValorizacion";
	private static String SQL_ERROR_NCG = loader.loadResourceToString("cl/bcs/normativa/sql/ValorizarNCG499_UpdateCircularError.sql");
	
	private final String SQL_UPDATE_VALORIZACION_CUSTODIA_MONTO = "UPDATE \"BSCL.Custodia.BD::CUS.CircularCarteraCMF\" SET \"MontoMercadoCustodia\"=:ValMontoMonedaInstrumento WHERE \"IdCircularCartera\"=:IdValorizacion";
	private String SQL_UPDATE_VALORIZACION = "";
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ValorizarNCGEntrada in = gson.fromJson(jin.toString(), ValorizarNCGEntrada.class);
		ValorizarNCGSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
	    JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ValorizarNCGSalida logica(Connection con, ValorizarNCGEntrada entrada) throws Exception {
		ValorizarNCGSalida out = new ValorizarNCGSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
        try {
	        SQL sql = new SQL();
	   
	        ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			
			ValorizarNCGEntrada param = new ValorizarNCGEntrada();
			List<ValorizarNCGEntrada> lstParametros = new ArrayList<>();
			param.p_IdEmpresa = entrada.p_IdEmpresa;
			param.p_Fecha = entrada.p_Fecha;

			lstParametros.add(param);

			sql.batch(con, SQL_UPDATE_ESTADO_NCG, lstParametros, ValorizarNCGEntrada.class);
			logger.info("UpdateEstado RFIF INA");
			
			sql.batch(con, SQL_INSERT_RFIF_MANT_NCG, lstParametros, ValorizarNCGEntrada.class);
			logger.info("Insert RFIF MANT");
			
			sql.batch(con, SQL_UPDATE_INDICADOR_NCG, lstParametros, ValorizarNCGEntrada.class);
			logger.info("UpdateIndicador RFIF S");
			
	    	HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_Fecha", entrada.p_Fecha);
			
			List<DatosNCG> data = sql.query(con, SQL_SELECT_NCG, params, DatosNCG.class);
			int count = 0;
			
			if(data.size() == 0) {
				out.P_OUT_MENSAJE = "Sin carteras para valorizar";
				return out;
			}
			
			for (DatosNCG item : data) {
				try {
					List<DatosNCG> singleItemList = new ArrayList<>();
					singleItemList.add(item);
					
					// Datos valorizados
					List<DatosNCG> dataR = ValorizarNCG(con, singleItemList, entrada,idPaisEmpresa);
					
					String columnaEditar = null;
					
					for(DatosNCG itemVal : dataR) {
						
						List<ResultadoValorizacion> listRes = new ArrayList<>();
						ResultadoValorizacion res = new ResultadoValorizacion();

						res.IdValorizacion = item.IdValorizacion;
						
				        logger.info("ValMontoMonedaInstrumento: " + itemVal.ValMontoMonedaInstrumento);
				        
						logger.info("Grabando valorizacion");
						if (itemVal.ValMontoMonedaInstrumento.compareTo(BigDecimal.ZERO) != 0) {
							
							String situacion = itemVal.Situacion;

							logger.info("Situacion de instrumento: " + situacion);
							switch(situacion) {
								case "CUSTODIA":
									columnaEditar = "ValorMercadoCustodia";
									sql.batch(con, SQL_UPDATE_VALORIZACION_CUSTODIA_MONTO, dataR, DatosNCG.class);
									break;
								case "GARANTIA":
									columnaEditar = "ValorizacionGarantia";
									break;
								case "PRESTAMO":
									columnaEditar = "ValorizacionPrestamo";
									break;
								case "PLAZOVEN":
									columnaEditar = "ValorizacionPlazoVenta";
									break;
								case "PLAZOCOM":
									columnaEditar = "ValorizacionPlazoCompra";
									break;
								default:
									res.IndicadorValorizado = "E";
									res.ValResultado = "Situacion no corresponde";
									break;
							}
							
						} else {
							// Webservice retorna valorizacion 0
							res.IndicadorValorizado = "S";
							res.ValResultado = "WARNING: WS RETURN 0";
						}
						
						if(itemVal.MensajeErrorFuente != null && itemVal.MensajeErrorFuente.length() > 0) { // Webservice retorna un error
							res.IndicadorValorizado = "E";
					        res.ValResultado = "ERROR: " + itemVal.MensajeErrorFuente.trim();
						} 
						
						if(columnaEditar != null) {
							SQL_UPDATE_VALORIZACION = "UPDATE \"BSCL.Custodia.BD::CUS.CircularCarteraCMF\" SET \"" + columnaEditar + "\"=:ValMontoMonedaInstrumento WHERE \"IdCircularCartera\"=:IdValorizacion";
							logger.info("Executing SQL: " + SQL_UPDATE_VALORIZACION);
							res.IndicadorValorizado = "S";
							res.ValResultado = "OK";
							
							sql.batch(con, SQL_UPDATE_VALORIZACION, dataR, DatosNCG.class);
							
							logger.info("Valorizacion grabada");
						} else {
							logger.info("No se pudo grabar valorizacion");
						}
					
						listRes.add(res);
						sql.batch(con, SQL_UPDATE_RESULTADO_VALORIZACION, listRes, ResultadoValorizacion.class);
	
					}
					
					count++;
					out.P_OUT_MENSAJE = "Carteras valorizadas: " + count;
					
				} catch (Exception e) {
					logger.error("Error para valorizar instrumento " + item.IdValorizacion + " - " + item.Nemotecnico);
					sql.batch(con, SQL_ERROR_NCG, data, DatosNCG.class);
					e.printStackTrace();
				}
			}
        } catch (SQLException e) {
        	out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "ERROR";
			e.printStackTrace();
			return out;
		}
        con.commit();
		return out;
	}
	
	public static List<DatosNCG> ValorizarNCG(Connection con, List<DatosNCG> data, ValorizarNCGEntrada in, Long idPaisEmpresa) throws IOException, Exception {
			
		ValorizarInstrumento.ValorizarInstrumentoSalida resultValorizacion = new ValorizarInstrumento.ValorizarInstrumentoSalida();
		ValorizarInstrumento.ValorizarInstrumentoSalida resultValorizacionVenta = new ValorizarInstrumento.ValorizarInstrumentoSalida();
		
		List <DatosNCG> dat= new ArrayList<DatosNCG>();
		
		try {
			ValorizarInstrumento.ValorizarInstrumentoEntrada VIE = new ValorizarInstrumento.ValorizarInstrumentoEntrada();
			ValorizarInstrumento.ValorizarInstrumentoEntrada VIEVTA = new ValorizarInstrumento.ValorizarInstrumentoEntrada();
			
			for(DatosNCG r: data) {
				if (r.IdInstrumento != null && !r.IdInstrumento.equals(0L)) {
					
					ValorizarInstrumento.ObjEntradaInstrumentos EI = new ValorizarInstrumento.ObjEntradaInstrumentos();
					
					EI.IdValorizacion = r.IdValorizacion;
					EI.OrigenProcesamiento = "MESA_UI";
					EI.FechaValorizacion = r.FechaValorizacion; // Dia de la compra
					EI.FechaOperacion = r.FechaOperacion; // Solo para operaciones a plazo
					EI.FechaVencimientoOperacion = r.FechaVencimientoOperacion; // Solo para operaciones a plazo
					EI.PlazoEnDiasOperacion = r.Plazo; // Solo para operaciones a plazo		
					EI.IdClasificacionTipoInstrumento = r.IdClasificacionTipoInstrumento; 
					EI.IdTipoInstrumento = r.IdTipoInstrumento; 
					EI.IdInstrumento = r.IdInstrumento;
					EI.Nemotecnico = r.Nemotecnico;
					EI.FechaVencimiento = r.InsFechaVencimiento; // "1900-01-01";
					EI.FechaEmision = r.InsFechaEmision;
					EI.IdMonedaInstrumento = r.IdMonedaInstrumento;
					EI.IdMonedaEmisionInstrumento = r.IdMonedaEmisionInstrumento;
					EI.IdTipoReajusteInstrumento = r.IdClasificacionTipoInstrumento == "IF" ? r.IdMonedaEmisionInstrumento : r.IdTipoReajusteInstrumento;
					EI.Emisor = r.Emisor; // valorizacionCL
					EI.IdCondicionDeLiquidacion = r.IdCondicionDeLiquidacion; // valorizacionCL
					EI.FechaDeLiquidacion = r.FechaDeLiquidacion;
					EI.Cantidad = r.Cantidad; 
					EI.ValorRescate = r.Cantidad;
					EI.TasaCompra = r.TasaCompra;
					EI.PrecioSubyacente = r.PrecioSubyacente; 
					EI.TasaSubyacente = r.TasaSubyacente; 
					EI.PrecioGarantia = r.PrecioGarantia; 
					EI.TasaGarantia = r.TasaGarantia; 
					EI.TasaTIF = r.TasaTIF;
					EI.IdPortafolio = 0L;
					EI.TablaOrigen = "CARTERA";		
					EI.IdTipoOperacion = r.IdTipoOperacion;
									
					VIE.p_Instrumentos.add(EI);
					ValorizarInstrumento.ObjEntradaInstrumentos EI2 = new ValorizarInstrumento.ObjEntradaInstrumentos();
					EI2 = Utils.copyPojo(EI, EI2);
									
					EI2.FechaValorizacion =  EI.FechaOperacion;
					VIEVTA.p_Instrumentos.add(EI2); // Dia de la venta 
				}		
			}
		
			if (VIE.p_Instrumentos.size() > 0) {
				ValorizarInstrumento VI = new ValorizarInstrumento();
				VIE.p_IdEmpresa	= in.p_IdEmpresa;		
				VIE.p_IdUsuario	= in.p_IdUsuario;
				VIE.p_IdModulo	= "MES"; // in.p_IdModulo;
				VIE.p_IdPais	= idPaisEmpresa;
				VIE.p_IdMonedaAdicional="CLP"; // MonedaAdicional;
				resultValorizacion = VI.logica(con, VIE);
				logger.debug("Salida ValorizarInstrumento dia compra");
	
	
				// Dia venta
				ValorizarInstrumento VIA = new ValorizarInstrumento();
				VIEVTA.p_IdEmpresa	= in.p_IdEmpresa;		
				VIEVTA.p_IdUsuario	= in.p_IdUsuario;
				VIEVTA.p_IdModulo	= "MES"; // in.p_IdModulo;
				VIEVTA.p_IdPais		= idPaisEmpresa;
				VIEVTA.p_IdMonedaAdicional="CLP"; // MonedaAdicional;
				resultValorizacionVenta = VIA.logica(con, VIEVTA);
				logger.debug("Salida ValorizarInstrumento dia ventar");
	
				//String FechaValorizaActual=Fechaaaaammdd.substring(0, 4) + "-"+ Fechaaaaammdd.substring(4, 6) +"-"+ Fechaaaaammdd.substring(6, 8);
				
				for(DatosNCG r: data) {
					for (ValorizarInstrumento.ObjResultadoValorizacion sal : resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS) {
						// Fecha Valorizacion
						if ( sal.IdValorizacion.equals(r.IdValorizacion) && sal.DimensionTasa.equals("COMP")) {
							
							DatosNCG EI = new DatosNCG();
							EI.IdValorizacion = r.IdValorizacion;
							EI.LinDet = r.LinDet;
							EI.Producto = r.Producto;
							EI.Mercado = r.Mercado;
							EI.FechaInicio = r.FechaInicio;
							EI.FechaVencimiento = r.FechaVencimiento;
							EI.Plazo = r.Plazo;
							EI.RutCliente = r.RutCliente;
							EI.RutEmisor = r.RutEmisor;
							EI.MonedaEmisor = r.MonedaEmisor;
							EI.MonedaPacto = r.MonedaPacto;
							EI.Tasatransferencia = r.Tasatransferencia;
							EI.TasaTransaccion = r.TasaTransaccion;
							EI.TasaOperacion = r.TasaOperacion;
							EI.BASTAS = r.BASTAS;
							EI.Sucursal = r.Sucursal;
							EI.MontoCompraVenta = r.MontoCompraVenta;
							EI.ValMontoMonedaInstrumento = sal.MontoMonedaInstrumento;
							EI.ValMontoDevengadoAMonedaInstrumento = sal.MontoDevengadoAMonedaInstrumento;
							EI.ValMontoMonedaSistema = sal.MontoMonedaSistema;
							EI.ValMontoDevengadoAMonedaSistema = sal.MontoDevengadoAMonedaSistema;
							EI.Reajuste = r.Reajuste;
							EI.MontoTransaccion = r.MontoTransaccion;
							EI.Comision = r.Comision;
							EI.UtilidaPerdidaDiferenciaPrecio = r.UtilidaPerdidaDiferenciaPrecio;
							EI.PrecioCompraVenta = r.PrecioCompraVenta;
							EI.UtilidadPerdidaOperacionCambio = r.UtilidadPerdidaOperacionCambio;
							EI.UtilidadPerdidaSimultanea = r.UtilidadPerdidaSimultanea;
							EI.IdTipoOperacion = r.IdTipoOperacion;
							EI.Situacion = r.Situacion;
														 	
							// Resultado de la venta
							for (ValorizarInstrumento.ObjResultadoValorizacion salVta : resultValorizacionVenta.P_OUT_INSTRUMENTOSVALORIZADOS) {
								// Fecha Valorizacion anterior
									if (salVta.IdValorizacion.equals(sal.IdValorizacion) && salVta.DimensionTasa.equals("COMP")) {
										EI.ValMontoMonedaInstrumentoFechaVenta = salVta.MontoMonedaInstrumento;
										EI.ValMontoMonedaSistemaFechaVenta = salVta.MontoMonedaSistema;
										
										if (EI.ValMontoMonedaInstrumentoFechaVenta.signum()> 0 && sal.MontoMonedaInstrumento.signum()>0) {
											if ((EI.ValMontoMonedaInstrumentoFechaVenta.subtract(sal.MontoMonedaInstrumento)).intValue() != 0) {
												EI.ValMontoDevengadoAMonedaInstrumento = EI.ValMontoMonedaInstrumentoFechaVenta.subtract(sal.MontoMonedaInstrumento);	
											}
										}
										if (EI.ValMontoMonedaSistemaFechaVenta.signum()> 0 && sal.MontoMonedaSistema.signum()>0) {
											if ((EI.ValMontoMonedaSistemaFechaVenta.subtract(sal.MontoMonedaSistema)).intValue() != 0)
											{
												EI.ValMontoDevengadoAMonedaSistema = EI.ValMontoMonedaSistemaFechaVenta.subtract(sal.MontoMonedaSistema);	
											}
										}
										
										break;
									}
								}
							
							// ERRORES
							if (resultValorizacion.P_OUT_ERRORVALORIZACIONES.size() >0) {
								for (ValorizarInstrumento.ObjProcesamiento err : resultValorizacion.P_OUT_ERRORVALORIZACIONES) {
									if (err.MensajeErrorFuente != null){
										EI.MensajeErrorFuente = err.MensajeErrorFuente;
									};
									break;
								};	
							};
							// Solo si la primera valorizacion no tiene errores, muestra posibles errores de la segunda
							if (resultValorizacionVenta.P_OUT_ERRORVALORIZACIONES.size() > 0 && resultValorizacion.P_OUT_ERRORVALORIZACIONES.size() == 0 ) {
								for (ValorizarInstrumento.ObjProcesamiento errVta : resultValorizacionVenta.P_OUT_ERRORVALORIZACIONES) {
									if (errVta.MensajeErrorFuente != null) {
										EI.MensajeErrorFuente = errVta.MensajeErrorFuente;
									};
									break;
								};	
							};
							dat.add(EI);
							
						} else {
							continue;	
						}
					}
			    }
				return dat;
			};
		
        } catch (Exception e) {
            resultValorizacion.P_OUT_ERRORESCONTADOR = 1L;
            resultValorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "EXCEPTION: ", e.getMessage()));
            throw e;
        }	
        	
	    return dat;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				ValorizarNCGEntrada entrada = new ValorizarNCGEntrada();
				entrada.p_IdEmpresa	= 1L;
				entrada.p_IdUsuario	= 6L;
				entrada.p_IdPais	= 44L;
				entrada.p_IdModulo	= "CUS";
				entrada.p_Fecha	= "2024-03-31";
				
				ValorizarNCG499 demo = new ValorizarNCG499();			
				ValorizarNCGSalida Salida = new ValorizarNCGSalida();
				Salida = demo.logica(con, entrada);
				
				System.out.println(Salida.P_OUT_MENSAJE);
				
				//con.rollback();;
				con.commit();
				
			} catch(Exception e) {
				con.rollback();
				e.printStackTrace();
				
			} finally {
				con.close();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		System.out.println("== END ==");
		    
	}

}
