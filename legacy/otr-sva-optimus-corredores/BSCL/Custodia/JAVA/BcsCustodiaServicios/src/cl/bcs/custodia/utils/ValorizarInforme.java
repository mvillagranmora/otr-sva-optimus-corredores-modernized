package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.plataforma.val.CalcularIntereses;
import cl.bcs.plataforma.val.Valorizar;
import cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ;
import cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoWS;
import cl.bcs.plataforma.val.ValorizarSubyacentePacto;
import cl.bcs.plataforma.val.ValorizarSubyacentePacto.EntradaValorizarSubyacentePacto;
import cl.bcs.plataforma.val.ValorizarSubyacentePacto.SalidaValorizarSubyacentePacto;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ValorizarInforme implements JavaCall{	
	
	static final Logger logger = LogManager.getLogger(ValorizarInforme.class.getName());
	
	private final String SQL_PortafolioCuentaPropia = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_PortafolioCuentaPropia.sql");
	private final String SQL_TipoValorizacionPorTipoInstrumento = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_TipoValorizacionPorTipoInstrumento.sql");
	private final String SQL_Instrumento = loadResourceToString("cl/bcs/custodia/sql/ValorizarInstrumento_SEL_Instrumento.sql");
	
	// GB
	private final String SQL_InsertCarteraValorizada = loadResourceToString("cl/bcs/custodia/sql/ValorizarInforme_INS_CarteraValorizada.sql");
	
	private final BigDecimal TASA_MAXIMA_VALORIZADOR_RF_DEFAULT = new BigDecimal("998");

	boolean ValorizaATasaPacto;
	boolean ValorizaATasaCompra;
	boolean ValorizaATasaMercado;
	boolean EsInformeCarteraPacto;
	boolean EsInformeCarteraPropia;
	boolean EsInformeMayorMenorValor;
	boolean EsInformeCoberturaPatrimonial;
	boolean EsInformeSimultaneas;
	boolean EsInformeMayorMenorValorRV;
	boolean valorizaMontoCompraAFechaCompra;
	
	public static class ObjEntradaInstrumentos {
		
		public String 			TablaAfecta;	// -> CARTERA / CARTERA_HIS / GARANTIA / GARANTIA_HIS / SUBYCENTE / SUBYACENTE_HIS / TRANSITO / TRANSITO_HIS
		public Long				IdValorizacion;
		public Long				IdCliente; 
		public Long				IdPortafolio;
		public Long				IdCartera 	= 0L;
		public Long 			FolioCartera = 0L;
		public Long				FolioOperacion = 0L;

		// Operacion ---------------------------------------------------------------
		public String			IdTipoOperacion;
		public String			FechaOperacion;							
		public String			FechaVencimientoOperacion;
		public Long				IdOperacion;
		public Long				PlazoEnDiasOperacion;
		public String			IdCondicionLiquidacion;
		public String			FechaLiquidacion;
		public String			Diferenciador;
		
		// Instrumento -------------------------------------------------------------
		public String			IdClasificacionTipoInstrumento;
		public String			IdTipoInstrumento; 
		public Long				IdInstrumento;
		public String			Emisor;								//valorizacionCL		
		public String			Nemotecnico = "";
		public String			NemoSVS;
		//public String			IdTipoReajuste;
		public String			IdMonedaInstrumento;
		public String			IdMonedaEmisionInstrumento;
		public String			IdTipoReajusteInstrumento;
		public String			FechaEmision;						//??? CUAL???
		public String			FechaVencimientoInstrumento;
		public String			FechaEmisionInstrumento;			//??? CUAL???
		public String			CodigoIFRS;
		public String			EsFungible;
	
		// Valor Actual ------------------------------------------------------------
		public BigDecimal		Cantidad; 
		public BigDecimal		ValorRescate; 
		public BigDecimal		PrecioCompra; 
		public BigDecimal		TasaCompra;
		public BigDecimal		TasaCompraValorizacionFechaOperacion;
		public BigDecimal		MontoCompra;								
		public BigDecimal		MontoCompraMonedaSistema;					
		public BigDecimal		MontoCompraMonedaInstrumento;				
		public BigDecimal		MontoCompraMonedaAdicional;
		public String			NombreCliente; 

		// Datos para los informes -------------------------------------------------		
		public String 			IdTipoReajusteCabecera;     	// C.Pactos
		public String			TablaOrigen;		
		public String			IdUso; 
		public String			TipoCartera; 
		public String			IdTipoCriterioTasa;

		public String			OrigenProcesamiento;
		public String          	IdConcepto;
		public String          	IdBoveda;
		public Long            	IdAgente;
		public BigDecimal      	MontoPactado;
		public Long            	IdCorredoraContraparte;
		public Long            	FolioComprobante;
		public BigDecimal		PrecioAPlazo;
		public BigDecimal		TasaAPlazo;
		public BigDecimal		MontoAPlazo;
		public BigDecimal		MontoAPlazoMonedaAdicional;
		public BigDecimal		MontoAPlazoMonedaInstrumento;
		public Long             IdMesa;
		public String           IdBolsa;
		public String          TipoCarteraCorredor;
		public String			FolioTransaccionNormal;
		public String			FolioTransaccionPlazo;
		public String			IdTipoProducto;
		
		
		public ObjEntradaInstrumentos () {
			this.IdTipoReajusteCabecera="";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	

	public static class ObjResultadoResumen {
		public String				Nemotecnico;
		public String				NemoSVS;
		public String				Emisor;
		public String				IdMonedaInstrumento;
		public String				IdTipoReajusteInstrumento;
		public BigDecimal			Cantidad;
		public BigDecimal			ValorRescate;
		public BigDecimal			MontoCompra;
		public BigDecimal			MontoCompraInicial;

		public BigDecimal			PrecioInicial;
		public BigDecimal			TasaInicial;
		public BigDecimal			MontoMonedaSistemaInicial;

		public BigDecimal			MontoInteresMonedaSistema;				
		public BigDecimal			MontoReajusteMonedaSistema;				
		
		public BigDecimal			PrecioFinal;
		public BigDecimal			TasaFinal;
		public BigDecimal			MontoMonedaSistemaFinal;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
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
		public String				ResultadoProceso;
		public String				MensajeProceso;
		public Long					TicketProcesamiento;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public java.sql.Date		FechaOperacion;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				IdClasificacionTipoInstrumento;		
		public Long					IdInstrumento; 
		public String				Nemotecnico;
		public String				NemoSVS;
		public String				Emisor;
		public BigDecimal			Cantidad;
		public BigDecimal			ValorRescate;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
		public String				MensajeErrorFuente;
		public BigDecimal			Tasa360;
		public BigDecimal			PrecioAPlazo;		
		public BigDecimal			TasaAPlazo;	 						//NO ESTAAAAAA
		public BigDecimal			MontoAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaSistema;
		public BigDecimal			MontoAPlazoMonedaInstrumento;			
		public BigDecimal			MontoInteresAPlazoMonedaInstrumento;		
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaInstrumento;
		public BigDecimal			MontoAPlazoMonedaAdicional;
		public BigDecimal			MontoInteresAPlazoMonedaAdicinal;
		public String				FechaValorizacionInicial;
		public String				MensajeErrorFuenteInicial;
		public java.sql.Date		FechaPrecioTasaInicial;			 
		public String				FuentePrecioTasaInicial;				
		public BigDecimal			PrecioInicial;
		public BigDecimal			TasaInicial;
		public Long					PlazoResidualInicial;
		public String				FechaValorizacionFinal;		
		public String				MensajeErrorFuenteFinal;		
		public java.sql.Date		FechaPrecioTasaFinal;			 
		public String				FuentePrecioTasaFinal;					
		public BigDecimal			PrecioFinal;
		public BigDecimal			TasaFinal;
		public Long					PlazoResidualFinal;
		
		public String				MensajeValorizacionInicial;
		public String				MensajeValorizacionFinal;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				IdMonedaSistema;
		public BigDecimal			MontoDevengadoAMonedaSistema;		

		public BigDecimal			MontoMonedaSistemaInicial;	
		public BigDecimal			MontoMonedaSistemaOriginalInicial;		
		public BigDecimal			MontoAPlazoMonedaSistemaInicial;
		public BigDecimal			MontoInteresMonedaSistema;				
		public BigDecimal			MontoReajusteMonedaSistema;				
		public BigDecimal			MontoReajusteAcumuladoMonedaSistema;
		public BigDecimal			MontoInteresAcumuladoMonedaSistema;
		public BigDecimal			MontoMonedaSistemaFinal;	
		public BigDecimal			MontoAPlazoMonedaSistemaFinal;		
		
		//++++++++++++++++++++++++++
		public String				IdMonedaInstrumento;
		public BigDecimal			MontoDevengadoAMonedaInstrumento;				//diferencia entre mañana y hoy		

		public BigDecimal			FactorMonedaInstrumentoInicial; 
		public java.sql.Date		FechaFactorMonedaInstrumentoInicial;
		public BigDecimal			MontoMonedaInstrumentoInicial;					
		public BigDecimal			MontoMonedaInstrumentoOriginalInicial;					
		public BigDecimal			MontoMonedaAPlazoInstrumentoInicial;		
		public BigDecimal			MontoInteresMonedaInstrumento;					
		public BigDecimal			MontoReajusteMonedaInstrumento;
		public BigDecimal			FactorMonedaInstrumentoFinal; 
		public BigDecimal			MontoMonedaAPlazoInstrumentoFinal;		
		public java.sql.Date		FechaFactorMonedaInstrumentoFinal;
		public BigDecimal			MontoMonedaInstrumentoFinal;					
		public BigDecimal			MontoMonedaInstrumentoOriginalFinal;					
		
		
		//++++++++++++++++++++++++++
		public String				IdMonedaAdicional;
		public BigDecimal			FactorMonedaAdicionalInicial; 
		public java.sql.Date		FechaFactorMonedaAdicionalInicial;
		public BigDecimal			MontoMonedaAdicionalInicial;			
		public BigDecimal			MontoMonedaAdicionalOriginalInicial;		
		public BigDecimal			MontoMonedaAPlazoAdicionalInicial;		
		public BigDecimal			MontoInteresMonedaAdicional;
		public BigDecimal			MontoReajusteMonedaAdicional;
		public BigDecimal			FactorMonedaAdicionalFinal; 
		public java.sql.Date		FechaFactorMonedaAdicionalFinal;
		public BigDecimal			MontoMonedaAdicionalFinal;			
		public BigDecimal			MontoMonedaAPlazoAdicionalFinal;		
		public BigDecimal			MontoMonedaAdicionalOriginalFinal;
		
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				TieneCorteDeCupon;
		public java.sql.Date		FechaCorteCupon;
		public java.sql.Date		FechaPagoCorteCupon;
		public BigDecimal			MontoInteresesPagoCorteCupon;
		public Long					NumeroCupon; 
		public String				IdMonedaPagoCupon;		 
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaSistema;				
		public BigDecimal			MontoCapitalCorteCuponMonedaSistema;		
		public BigDecimal			MontoInteresCorteCuponMonedaSistema; 		
		public BigDecimal			MontoReajusteCorteCuponMonedaSistema;		
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaInstrumento;			//Monto amortizacion + interes
		public BigDecimal			MontoCapitalCorteCuponMonedaInstrumento;	//Monto total sin interes
		public BigDecimal			MontoInteresCorteCuponMonedaInstrumento;	//Interes periodo corte
		public BigDecimal			MontoReajusteCorteCuponMonedaInstrumento;	//Bono reajustado según la inflacion
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaAdicional; 
		public BigDecimal			MontoCapitalCorteCuponMonedaAdicional; 
		public BigDecimal			MontoInteresCorteCuponMonedaAdicional; 
		public BigDecimal			MontoReajusteCorteCuponMonedaAdicional;
		//++++++++++++++++++++++++++
		public BigDecimal			MayorValorMonedaSistema;
		public BigDecimal			MayorValorMonedaInstrumento;
		public BigDecimal			MayorValorMonedaAdicional;
		public BigDecimal			MenorValorMonedaSistema;
		public BigDecimal			MenorValorMonedaInstrumento;
		public BigDecimal			MenorValorMonedaAdicional;		
		//++++++++++++++++++++++++++
		public BigDecimal			Duration;
		public BigDecimal			Convexidad;
		public BigDecimal			PlazoValorizador;
		public String				ValorTieneCall;
		public BigDecimal			TasaEmision;
		public String				ValorClasificacionRiesgo;

		public String				IdTipoReajuste;
		//public BigDecimal			ValorTipoReajuste;		
		public BigDecimal			ValorTipoReajusteInicial;
		public BigDecimal			ValorTipoReajusteFinal;		
		public String				NemoOriginal;
		
		
		public Long					PlazoEnDiasOperacion;
		public BigDecimal			MontoCompra;
		public BigDecimal			ValorPar;
		public BigDecimal			Precio;
		public BigDecimal			MontoMonedaSistema;
		public BigDecimal			MontoMonedaInstrumento;		
		public BigDecimal			MontoMonedaAdicional;
		public BigDecimal			FactorMonedaInstrumento; 
		public java.sql.Date		FechaFactorMonedaInstrumento;
		public BigDecimal			FactorMonedaAdicional;
		public java.sql.Date		FechaFactorMonedaAdicional;
		public BigDecimal			Tasa;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaAdicional;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public static class ObjPortafolioCuentaPropia {
		public Long					IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjInstrumento {		
		public Long					IdInstrumento;
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public String				Nemotecnico;
		public String 				ISIN;
		public String				IdEmisor;
		public String				IdTipoReajuste;
		public java.sql.Date		FechaEmisionInstrumento;
		public java.sql.Date		FechaVencimientoInstrumento;
		public String				IdMoneda;
		public BigDecimal			CorteMinimo;
		public Long					Tamano;
		public String				IndArt104;
		public BigDecimal			TasaFiscal;
		public BigDecimal			TasaEmision;
		public String				ValorizaPor;
		public String				TipoValorizacionInstrumento;
		public String				TieneCupon;
		public Integer				NumeroCupones;
		public String				CallConfirmado;
		public String				CuponeraVariable;
		public Integer				TipoCalculoInteresesCupon;
		public String				ValorClasificacionRiesgo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjTipoValorizacionPorTipoInstrumento {
		public Long					IdTipoValorizacionPorTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public String				UsaTasaCompra; 
		public String				UsaTasaMercado; 
		public String				UsaTasaCorredora; 
		public String				UsaTasaTIF; 
		public String				UsaPibMasMenos; 
		public String				ValorizaAnterior; 
		public String				ValorizaActual; 
		public String				ValorizaProximo; 
		public String				CalculaIntereses;
		public String				CalculaInteresesAPlazo;
		public String				TipoCalculoInteres;
		public Long					BaseCalculoDeInteres;
		public String				TipoValorizacion;
		public String				SubTipoValorizacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};	
	
	public static class ObjProcesamiento {
		public Long					IdProcesamiento; 
		public Long					IdValorizacion;		//Dato de entrada 
		public String				DimTasa; 
		public String				DimTiempo; 
		public String				FechaCalculada; 
		public String				TipoPrecioTasa; 
		public String				ValorizaPor; 
		public String				IdClasificacionTipoInstrumento; 
		public String				ValorClasificacionRiesgo;
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				Nemotecnico;
		public String				NemoSVS;
		public String				FechaEmision; 
		public BigDecimal			TasaEmision;
		public String				FechaVencimiento; 
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public String				Emisor;
		public String				IdCondicionDeLiquidacion;
		public Long					Tamano;
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate; 
		public BigDecimal			Precio;
		public java.sql.Date		FechaPrecioTasa; 
		public String				FuentePrecioTasa;
		public String				MensajeErrorFuente;
		public BigDecimal			Tasa;
		public BigDecimal			Monto;					// Monto en Moneda Sistema??
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia; 
		public BigDecimal			TasaTIF; 		
		public String				IdUso; 
		public String				TipoCartera; 
		public String				IdTipoCriterioTasa; 
		public String				EsCuentaPropia;
		public String				IndicadorUsoTasa; 
		public String				TipoValorizacion;
		public String				TipoValorizacionInstrumento; 
		public String				SubTipoValorizacion;
		public String				CalculaIntereses;
		public String				CalculaInteresesAPlazo; 
		public String				ValorizaCorteDeCupon = "N";
		public String 				EsBonoCero;
		public Integer				TipoCalculoInteresesCupon;
		public Long					PlazoResidual;
		public String				TablaOrigen;
		
		public String				NemoOriginal;		// Cuando hay cambio de nemo por corte cupon (& y *)
		public String				OrigenValorizacion;	// Para la valorizacion de instrumentos por la UI de la mesa
		public BigDecimal			MontoUI;			// Para la valorizacion de instrumentos por la UI de la mesa
		public BigDecimal			MontoMonedaSistemaOriginal;
		public String               IdReajusteCabecera;
		public String 				ValorizaATasaPacto;
		public String               EsPacto;		
		public String               FechaOperacion;
		
		public ObjProcesamiento () {
			this.ValorizaATasaPacto= "N";
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static class ObjValorizarInformesPQ {
		public Long					IdValorizacion; 
		public String				FechaValorizacion;
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				IdMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			Precio;
		public BigDecimal			Monto;
	
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	public static class ObjValorizarInformesWS {
		public Long					IdValorizacion; 
		public String				FechaValorizacion;
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				FechaEmision; 
		public String				FechaVencimiento; 
		public String				IdMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate; 
		public BigDecimal			Precio; 
		public BigDecimal			Tasa; 
		public BigDecimal			Monto;
		public String				TipoValorizacion;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	public static class TablaValorizacionRFIF {

		public Long				IdValorizacion;
		public String			IdInforme;
		public Long				IdCartera 	= 0L;
		public Long 			FolioCartera = 0L;
		public Long				FolioOperacion = 0L;
		public Long				IdEmpresa = 0L;

		// Operacion ---------------------------------------------------------------
		public String			IdTipoOperacion;
		public Long				IdCliente; 
		public Long				IdPortafolio;	
		public String			FechaOperacion;							
		public String			FechaVencimientoOperacion;
		public Long				IdOperacion;
		public Long				PlazoEnDiasOperacion;
		public String			IdCondicionLiquidacion;
		public String			FechaLiquidacion;
		public String			Diferenciador;
		// Instrumento -------------------------------------------------------------
		public String			IdClasificacionTipoInstrumento;
		public String			IdTipoInstrumento; 
		public Long				IdInstrumento;
		public String			Emisor;								//valorizacionCL		
		public String			Nemotecnico = "";
		public String			NemoSVS;
		public String			NemoOriginal;
		public String			IdTipoReajuste;
		public String			IdMonedaInstrumento;
		public String			IdMonedaEmisionInstrumento;
		public String			IdTipoReajusteInstrumento;
		public String			IdMonedaAdicional;
		public String			FechaEmision;						//??? CUAL???
		public String			FechaVencimientoInstrumento;
		public String			FechaEmisionInstrumento;			//??? CUAL???
		public String			TieneCorteCupon;
		public String			CodigoIFRS;
		public String			EsFungible;
	
		// Valor Actual ------------------------------------------------------------
		public BigDecimal		Cantidad; 
		public BigDecimal		ValorRescate; 
		public BigDecimal		PrecioCompra; 
		public BigDecimal		TasaCompra;
		public BigDecimal		MontoCompra;						//en moneda del instrumento		
		public BigDecimal		MontoCompraMonedaSistema;			//en moneda del instrumento		
		public BigDecimal		MontoCompraMonedaInstrumento;		//en moneda del instrumento
		public BigDecimal		MontoCompraMonedaAdicional;			//en moneda del instrumento	
		public BigDecimal		PrecioAPlazo;
		public BigDecimal		MontoAPlazo;
		public BigDecimal		MontoAPlazoMonedaInstrumento;
		public BigDecimal		MontoAPlazoMonedaAdicional;
		public BigDecimal		TasaAPlazo;
				
		// Valorizaci�n Fecha Inicio/Consulta --------------------------------------
		public String			FechaValorizacionActual;
		public BigDecimal		PlazoOperacionActual;
		public String			FechaFactorMonedaInstrumentoActual;
		public BigDecimal		ValorFactorMonedaInstrumentoActual;
		public String			FechaFactorMonedaAdicionalActual;
		public BigDecimal		ValorFactorMonedaAdicionalActual;
		public BigDecimal		TasaActual;
		public BigDecimal		PrecioActual;
		public BigDecimal		MontoActual;
		public BigDecimal		MontoActualMonedaInstrumento;
		public BigDecimal		MontoActualMonedaAdicional;
		public String			FechaPrecioTasaMercadoActual;
		public String			FuenteTasaPrecioMercadoActual;
		
		// Valorizaci�n Fecha Fin/Hasta ---------------------------------------------	
		public String		 	FechaValorizacionProximo;
		public BigDecimal		PlazoOperacionProximo;
		public String			FechaFactorMonedaInstrumentoProximo;
		public BigDecimal		ValorFactorMonedaInstrumentoProximo;
		public String			FechaFactorMonedaAdicionalProximo;
		public BigDecimal		ValorFactorMonedaAdicionalProximo;
		public BigDecimal		TasaProximo;
		public BigDecimal		PrecioProximo;
		public BigDecimal		MontoProximo;
		public BigDecimal		MontoProximoMonedaInstrumento;
		public BigDecimal		MontoProximoMonedaAdicional;
		public String			FechaPrecioTasaMercadoProximo;
		public String			FuenteTasaPrecioMercadoProximo;

		// Intereses y Reajustes entre Valorizaciones ------------------------------
		public BigDecimal		MontoInteres;
		public BigDecimal		MontoReajuste;
		public BigDecimal		MontoInteresMonedaInstrumento;
		public BigDecimal		MontoReajusteMonedaInstrumento;
		public BigDecimal		MontoInteresMonedaAdicional;
		public BigDecimal		MontoReajusteMonedaAdicional;

		// Intereses y Reajustes Acumulado (F.Operacion - F.Fin) ------------------
		public BigDecimal		MontoInteresCorrido;
		public BigDecimal		MontoReajusteCorrido;
		public BigDecimal		MontoInteresCorridoMonedaInstrumento;
		public BigDecimal		MontoReajusteCorridoMonedaInstrumento;
		public BigDecimal		MontoInteresCorridoMonedaAdicional;
		public BigDecimal		MontoReajusteCorridoMonedaAdicional;

		// Mayor y Menor Valor con Val.T.Mercado -------------- ------------------
		public BigDecimal		MayorValorMonedaSistema;				
		public BigDecimal		MayorValorMonedaInstrumento;	
		public BigDecimal		MayorValorMonedaAdicional;	
		public BigDecimal		MenorValorMonedaSistema;		
		public BigDecimal		MenorValorMonedaInstrumento;	
		public BigDecimal		MenorValorMonedaAdicional;		

		// Corte Cupon -----------------------------------------------------------
		public String			FechaCorteCupon;
		public String			FechaPagoCorteCupon;
		public BigDecimal		NumeroCupon;
		public String			IdMonedaPagoCupon;
		public BigDecimal		MontoCorteCupon;
		public BigDecimal		MontoInteresCorteCupon;
		public BigDecimal		MontoReajusteCorteCupon;
		public BigDecimal		MontoCorteCuponMonedaInstrumento;
		public BigDecimal		MontoInteresCorteCuponMonedaInstrumento;
		public BigDecimal		MontoReajusteCorteCuponMonedaInstrumento;
		public BigDecimal		MontoCorteCuponMonedaAdicional;
		public BigDecimal		MontoInteresCorteCuponMonedaAdicional;
		public BigDecimal		MontoReajusteCorteCuponMonedaAdicional;

		// Otros -------------------------------------------------------------------
		public BigDecimal		Duration;
		public BigDecimal 		Convexidad;
		public String			TablaOrigen;
		public String			NombreCliente;		
		
		public String			prmTipoInforme;		
		public String			prmTipoValorizacion; 
		public Long				prmUsuario; 
		public java.sql.Timestamp 	FechaEmisionReporte;
		public String          IdConcepto;
		public String          IdBoveda;
		public Long            IdAgente;
		public BigDecimal      MontoPactado;
		public Long            IdCorredoraContraparte;
		public Long            FolioComprobante;
		public Long            IdMesa;
		public String          IdBolsa;
		public String          TipoCarteraCorredor;
		public String			FolioTransaccionNormal;
		public String			FolioTransaccionPlazo;
		public String			IdTipoProducto;
		public Long            IdClienteSolicitante;
				

		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static class ValorizarInformeEntrada{
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;
		public String							p_IdModulo;
		public Long								p_IdPais = -1L;
		public String							p_IdMonedaAdicional = null;
		public Long								p_IdPortafolio = 0L;
		public List<Long>						p_IdsPortafolio;
		public List<ObjEntradaInstrumentos>		p_Instrumentos;
		public String							p_OrigenProcesamiento = "";
		public String							p_FechaInicial;
		public String							p_FechaFinal;
		public String							p_IdTipoTasaValorizacion;
		public String							p_IdFechaHoraSistema;
		public boolean                        p_EsCuentaPropia = true;
		public Long                            p_IdClienteSolicitante;
		public ValorizarInformeEntrada(){
			p_Instrumentos	= new ArrayList<ObjEntradaInstrumentos>();
			p_IdsPortafolio = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarInformeSalida{
		public List<ObjResultadoValorizacion>	P_OUT_INSTRUMENTOSVALORIZADOS;
		public List<MensajeSalida>				P_OUT_MENSAJES;		
		public List<ObjProcesamiento>           P_OUT_ERRORVALORIZACIONES;
		public Long								P_OUT_ERRORESCONTADOR;
		public String							LlaveReporte;
		
		public ValorizarInformeSalida(){
			P_OUT_MENSAJES 					= new ArrayList<MensajeSalida>();
			P_OUT_INSTRUMENTOSVALORIZADOS	= new ArrayList<ObjResultadoValorizacion>();
			P_OUT_ERRORVALORIZACIONES 		= new ArrayList<ObjProcesamiento>();
			P_OUT_ERRORESCONTADOR			= 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	@Trace
	public ValorizarInformeSalida logica(Connection con, ValorizarInformeEntrada in) throws Exception {
		logger.info("Inicio ValorizarInforme");
		ValorizarInformeSalida out = new ValorizarInformeSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();

		ValorizaATasaPacto = "PAC".equals(in.p_IdTipoTasaValorizacion);
		ValorizaATasaCompra = "COMP".equals(in.p_IdTipoTasaValorizacion);
		ValorizaATasaMercado = "MERC".equals(in.p_IdTipoTasaValorizacion);
		EsInformeCarteraPacto = "CARTERA.PACTO".equals(in.p_OrigenProcesamiento);
		EsInformeCarteraPropia = "CARTERA.PROPIA".equals(in.p_OrigenProcesamiento);
		EsInformeMayorMenorValor = "MAYOR.MENOR.VALOR.RF.".equals(in.p_OrigenProcesamiento);
		EsInformeMayorMenorValorRV = "MAYOR.MENOR.VALOR.RV.".equals(in.p_OrigenProcesamiento);
		EsInformeCoberturaPatrimonial = "COBERTURA.PATRIMONIAL".equals(in.p_OrigenProcesamiento);
		EsInformeSimultaneas = "CARTERA.SIMULTANEA".equals(in.p_OrigenProcesamiento);
		
		try {
			if(in.p_IdFechaHoraSistema == null || in.p_IdFechaHoraSistema.isEmpty()) {
				String codigoError = "FECHA_HORA_SISTEMA_NO_VALIDA";
				MensajeSalida errorParametros = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorParametros);
				out.P_OUT_ERRORESCONTADOR++;
			}			
			
			if ( ! ( EsInformeCarteraPacto || EsInformeCarteraPropia || EsInformeMayorMenorValor || EsInformeCoberturaPatrimonial || EsInformeSimultaneas || EsInformeMayorMenorValorRV) ) {
				// Valorizacion Informes IF/RF No reconoce tipo de informe				
				String codigoError = "VALORIZACION_INFORME_IFRF_TIPO_INFORME";
				MensajeSalida errorParametros = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorParametros);
				out.P_OUT_ERRORESCONTADOR++;
			}

			if ( ! ( ValorizaATasaCompra || ValorizaATasaMercado || ValorizaATasaPacto) ) {
				// Valorizacion Informes IF/RF No reconoce Tasa de valorizacion
				String codigoError = "VALORIZACION_INFORME_IFRF_TASA_VALORIZACION";
				MensajeSalida errorParametros = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorParametros);
				out.P_OUT_ERRORESCONTADOR++;
			}

			if ( ( EsInformeCarteraPropia && ValorizaATasaPacto ) || 
				 ( EsInformeMayorMenorValor && !ValorizaATasaMercado ) ||
				 ( EsInformeCoberturaPatrimonial && ValorizaATasaPacto ) ) {
				// Valorizacion Informes IF/RF Informe no admite este tipo de valorizacion
				String codigoError = "VALORIZACION_INFORME_IFRF_TASA_INFORME";
				MensajeSalida errorParametros = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorParametros);
				out.P_OUT_ERRORESCONTADOR++;
			}			
			
			if ( ( EsInformeCarteraPropia || EsInformeCarteraPacto ) &&
				 !( Fecha.toDate(in.p_FechaFinal).compareTo(Fecha.toDate(in.p_FechaInicial)) >= 0 ) ) {
				// Cart.Propia y Pacto > Fecha de valorizacion debe ser mayor o igual que fecha de consulta				
				String codigoError = "VALORIZACION_INFORME_IFRF_FECHA_FINAL";				
				MensajeSalida errorParametros = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorParametros);
				out.P_OUT_ERRORESCONTADOR++;
			}			
			
			if ( out.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0 ) {
				logger.info(out.P_OUT_MENSAJES);
				return out;
			}
						
		
			String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.LocalizacionSistema");
			String IdLlaveInforme = "";
			BigDecimal tasaMaximaValorizadorRF = obtenerTasaMaximaValorizadorRF(con, in.p_IdEmpresa);
			
			try {
				valorizaMontoCompraAFechaCompra = "S".equals(ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.ValorizaMontoCompraAFechaCompra"));
			}catch(Exception ex) {
				logger.info(String.format("Parametro: %s no encontrado", "PLA.ValorizaInformesAFechaCompra"));
			}
			
			// Parametros Generales de la Valorizacion del Informe
			String FechaValorizacionInicio = "MAYOR.MENOR.VALOR".equals(in.p_OrigenProcesamiento) || "COBERTURA.PATRIMONIAL".equals(in.p_OrigenProcesamiento) ? in.p_FechaFinal : in.p_FechaInicial;
			String FechaValorizacionFin = in.p_FechaFinal;
			String IdTipoTasaValorizacion = in.p_IdTipoTasaValorizacion;

			
			HashMap<String,Object> paramsPortafolioCuentaPropia = new HashMap<>();
			paramsPortafolioCuentaPropia.put("IdEmpresa",in.p_IdEmpresa);
			List<ObjPortafolioCuentaPropia> PCP = sql.query(con, SQL_PortafolioCuentaPropia, paramsPortafolioCuentaPropia, ObjPortafolioCuentaPropia.class);	
			if (in.p_IdsPortafolio != null && !in.p_IdsPortafolio.isEmpty()) {
				PCP = PCP.stream()
						.filter(port -> in.p_IdsPortafolio.contains(port.IdPortafolio))
					    .collect(Collectors.toList());
			}
			if (PCP.isEmpty() && in.p_EsCuentaPropia) {
				out.P_OUT_ERRORESCONTADOR++;
				String codigoError = "PORTAFOLIO_NO_VALIDO";
				MensajeSalida errorPortafolio = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorPortafolio);
				return out;
			}			
			HashMap<Long,Object> PortafoliosCuentaPropia = Utils.List2Hash(PCP, "IdPortafolio");
						
			HashMap<String,Object> paramsTipoValorizacionPorTipoInstrumento = new HashMap<>();
			paramsTipoValorizacionPorTipoInstrumento.put("IdEmpresa",in.p_IdEmpresa);
			List<ObjTipoValorizacionPorTipoInstrumento> TVTI = sql.query(con, SQL_TipoValorizacionPorTipoInstrumento, paramsTipoValorizacionPorTipoInstrumento, ObjTipoValorizacionPorTipoInstrumento.class);	
			HashMap<String,Object> TipoValorizacionPorTipoInstrumento = Utils.List2Hash(TVTI, "IdTipoInstrumento");
			
	
			HashMap<String,Object> paramsInstrumento = new HashMap<>();
			List<Long> IdInstrumento = in.p_Instrumentos.parallelStream().filter(t -> t.IdInstrumento != null).map(t -> t.IdInstrumento).distinct().collect(Collectors.toList());
			paramsInstrumento.put("IdInstrumento", IdInstrumento);
			List<ObjInstrumento> INS = sql.query(con, SQL_Instrumento, paramsInstrumento, ObjInstrumento.class);
			HashMap<Long,Object> InfoInstrumentos = Utils.List2Hash(INS, "IdInstrumento");
			
			//**************************************************************************************************************************************************
			//****************************** Obtencion de Secuencias *******************************************************************************************
			//**************************************************************************************************************************************************			
			logger.debug("Inicio Secuencias");
			
			//List<Long> SecProcesamientoValorizaCustodia = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodia", new Long( (35*in.p_Instrumentos.size()) ));
			List<Long> SecProcesamientoValorizaCustodia = new ArrayList<>();
			for (long i = 10000 ; i<(10000 + 36*in.p_Instrumentos.size()) ; i++) {
				SecProcesamientoValorizaCustodia.add(i);
			}
			logger.debug("Fin Secuencias");
			List<EntregarPreciosTasas_Entrada_DatosInstrumentos> ConsultarPreciosTasas = new ArrayList<EntregarPreciosTasas_Entrada_DatosInstrumentos>();			

			//**************************************************************************************************************************************************
			//*********************************** PROCESAR INSTRUMENTOS  ***************************************************************************************
			//**************************************************************************************************************************************************
			Integer i = 0;	
			List<ObjProcesamiento> ValorizacionDatosEntrada = new ArrayList<ObjProcesamiento>();
			
			logger.debug("Inicio Loop para Inicialiar Instrumentos");
			for (ObjEntradaInstrumentos Instrumento: in.p_Instrumentos ) {
				ObjTipoValorizacionPorTipoInstrumento TipoValorizacion = (ObjTipoValorizacionPorTipoInstrumento) TipoValorizacionPorTipoInstrumento.get(Instrumento.IdTipoInstrumento);
				ObjInstrumento InformacionAdicionalInstrumento = (ObjInstrumento) InfoInstrumentos.get(Instrumento.IdInstrumento);				
				if (TipoValorizacion == null || InformacionAdicionalInstrumento == null) continue;


				Long PlazoResidualActualInstrumento	= 0L;
				Long PlazoResidualProximoInstrumento= 0L;
				Long PlazoResidualActualOperacion	= 0L;
				Long PlazoResidualProximoOperacion	= 0L;
				
				// Plazo Residual respecto del INSTRUMENTO
				if (Instrumento.FechaVencimientoInstrumento != null) {
					PlazoResidualActualInstrumento = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoInstrumento).getTime()-Fecha.toDate(FechaValorizacionInicio).getTime())/86400000);
					PlazoResidualProximoInstrumento = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoInstrumento).getTime()-Fecha.toDate(FechaValorizacionFin).getTime())/86400000);
				}
				
				// Plazo Residual respecto de la OPERACION
				if (Instrumento.FechaVencimientoOperacion != null) {
					PlazoResidualActualOperacion = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoOperacion).getTime()-Fecha.toDate(FechaValorizacionInicio).getTime())/86400000);
					PlazoResidualProximoOperacion = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoOperacion).getTime()-Fecha.toDate(FechaValorizacionFin).getTime())/86400000);
				}				
				
				/*************************/
				if (Instrumento.Nemotecnico != null && !Instrumento.Nemotecnico.equals("") && !InformacionAdicionalInstrumento.Nemotecnico.equalsIgnoreCase(Instrumento.Nemotecnico)){
					InformacionAdicionalInstrumento.Nemotecnico = Instrumento.Nemotecnico;
				}					
				if (Instrumento.Nemotecnico != null && (Instrumento.Nemotecnico.equalsIgnoreCase("PRT") || Instrumento.Nemotecnico.equalsIgnoreCase("PRF"))){
					InformacionAdicionalInstrumento.Nemotecnico = "PAGARE R";
				}
				/***********************/
				ObjPortafolioCuentaPropia PortafolioCuentaPropia = (ObjPortafolioCuentaPropia) PortafoliosCuentaPropia.get(Instrumento.IdPortafolio);

				
				if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
					Instrumento.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
				}
				
				
				//++++++++++++++++++++++++++++++++++
				String ValorizarPor = "";
				ValorizarPor = InformacionAdicionalInstrumento.ValorizaPor != null && !InformacionAdicionalInstrumento.ValorizaPor.equals("") ? InformacionAdicionalInstrumento.ValorizaPor : "P";
				//++++++++++++++++++++++++++++++++++
				
				String IdUso = ""; 
				String TipoCartera = "";
				String IdTipoCriterioTasa = "";
				String EsCuentaPropia = "";
				
				
				if (PortafolioCuentaPropia == null ) EsCuentaPropia = "N"; 
				else EsCuentaPropia = "S";
				
				if (Instrumento.IdTipoCriterioTasa == null) IdTipoCriterioTasa = "NORMATIVO";
				else IdTipoCriterioTasa = Instrumento.IdTipoCriterioTasa;
				
				if (Instrumento.IdUso == null){
					if (Instrumento.TablaOrigen == null || Instrumento.TablaOrigen.equals("") ){
						IdUso = "CARTERA";
					} else {
						IdUso = Instrumento.TablaOrigen;
					}
				} else 
					IdUso = Instrumento.IdUso;
				
				if (Instrumento.TipoCartera == null){
					if (PortafolioCuentaPropia != null){
						TipoCartera = "PROPIA";
					} else 
						TipoCartera = "TERCEROS";
				} else 
					TipoCartera = Instrumento.TipoCartera;
				
				
				if(valorizaMontoCompraAFechaCompra && !ValorizaATasaPacto) {
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i++);
					PROC_WS.DimTasa					= "COMP"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.ValorizaCorteDeCupon	= "N";
					PROC_WS.FechaCalculada 		    = Instrumento.FechaLiquidacion;
					PROC_WS.FechaOperacion          = Instrumento.FechaOperacion;
					
					PROC_WS.PlazoResidual			= Instrumento.FechaVencimientoInstrumento != null ? PlazoResidualActualInstrumento : PlazoResidualActualOperacion;					
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= "N";					
					PROC_WS.OrigenValorizacion		= in.p_OrigenProcesamiento;
					PROC_WS.EsBonoCero				= "N";	
					PROC_WS.Precio				    = null;
					PROC_WS.Tasa				    = Instrumento.TasaCompraValorizacionFechaOperacion;
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ||
							TipoValorizacion.IdTipoInstrumento.equalsIgnoreCase("AUT")) {

							PROC_WS.SubTipoValorizacion	= "PAGARE";
							PROC_WS.Tasa				= Instrumento.TasaCompraValorizacionFechaOperacion;
							PROC_WS.Cantidad			= null;

						} else {
							PROC_WS.EsBonoCero					= Instrumento.Emisor.equals("CERO") ? "S" : "N";
							PROC_WS.SubTipoValorizacion			= PROC_WS.SubTipoValorizacion;
							PROC_WS.Tasa						= !EsInformeSimultaneas ?  ValorizarPor.equals("T") ? Instrumento.TasaCompraValorizacionFechaOperacion : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompraValorizacionFechaOperacion : null) : Instrumento.TasaCompraValorizacionFechaOperacion;
							PROC_WS.Precio					    = ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);
							PROC_WS.Cantidad					= Instrumento.Cantidad;						
							PROC_WS.ValorRescate				= null;
							PROC_WS.IdMonedaEmisionInstrumento 	= StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
						}

						if(EsInformeCarteraPacto && !ValorizaATasaMercado) {
							PROC_WS.Monto = (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? 
											Instrumento.MontoCompraMonedaSistema != null ?	
													Instrumento.MontoCompraMonedaSistema : Instrumento.MontoCompra : null;
						}
						
						if("IF".equals(Instrumento.IdClasificacionTipoInstrumento) && EsInformeCarteraPropia) {
							PROC_WS.Monto = Instrumento.MontoCompra;
						}
						
						ValorizacionDatosEntrada.add(PROC_WS);						
				}
				
				ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
				
				PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i++);
				PROC_WS.DimTasa					= IdTipoTasaValorizacion; //"COMP"; 
				PROC_WS.DimTiempo				= "AC";
				PROC_WS.IdUso					= IdUso;
				PROC_WS.TipoCartera				= TipoCartera;
				PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
				PROC_WS.EsCuentaPropia			= EsCuentaPropia;
				PROC_WS.FechaCalculada			= FechaValorizacionInicio;
				PROC_WS.FechaOperacion          = Instrumento.FechaOperacion;
				
				if ((EsInformeCarteraPacto || EsInformeSimultaneas) && (Fecha.toDate(Instrumento.FechaVencimientoOperacion).compareTo(Fecha.toDate(FechaValorizacionInicio)) < 0)) {
					PROC_WS.FechaCalculada = Instrumento.FechaVencimientoOperacion;
					PROC_WS.PlazoResidual= (Long) ((Fecha.toDate(Instrumento.FechaVencimientoOperacion).getTime()-Fecha.toDate(PROC_WS.FechaCalculada).getTime())/86400000);
				} else if ((EsInformeMayorMenorValor || EsInformeMayorMenorValorRV || EsInformeCoberturaPatrimonial) && Fecha.toDate(Instrumento.FechaLiquidacion).compareTo(Fecha.toDate(FechaValorizacionInicio)) > 0) {
					PROC_WS.FechaCalculada = Instrumento.FechaLiquidacion;
					PROC_WS.PlazoResidual  = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoInstrumento).getTime()-Fecha.toDate(PROC_WS.FechaCalculada).getTime())/86400000);
				} else {
					PROC_WS.FechaCalculada = Instrumento.FechaVencimientoInstrumento.compareTo(FechaValorizacionInicio) > 0 || "0001-01-01".equals(Instrumento.FechaVencimientoInstrumento) ? FechaValorizacionInicio : Instrumento.FechaVencimientoInstrumento;
					PROC_WS.PlazoResidual  = Instrumento.FechaVencimientoInstrumento != null ? PlazoResidualProximoInstrumento : PlazoResidualProximoOperacion;
				}
				// Valorizaci�n de Cupon solo afecta al PRXM 
				PROC_WS.ValorizaCorteDeCupon	= InformacionAdicionalInstrumento.NumeroCupones > 0 && in.p_FechaFinal.compareTo(in.p_FechaInicial) > 0 ? "S" : "N";
				
				// ????
				if ("ANTEMP".equalsIgnoreCase(Instrumento.IdTipoOperacion) || "ANTPRT".equalsIgnoreCase(Instrumento.IdTipoOperacion) ||  "EMPRES".equalsIgnoreCase(Instrumento.IdTipoOperacion) || "PRESTA".equalsIgnoreCase(Instrumento.IdTipoOperacion)) {
					PROC_WS.FechaCalculada 		= Instrumento.FechaOperacion.compareTo(in.p_FechaInicial) > 0 ? in.p_FechaInicial : Instrumento.FechaOperacion;
				}
				
				PROC_WS.PlazoResidual			= Instrumento.FechaVencimientoInstrumento != null ? PlazoResidualActualInstrumento : PlazoResidualActualOperacion;					
				PROC_WS.ValorizaPor				= ValorizarPor;
				PROC_WS.CalculaIntereses		= "N";					
				PROC_WS.OrigenValorizacion		= in.p_OrigenProcesamiento;
				PROC_WS.EsBonoCero				= "N";					

				// revisarrr ?????????
				//PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
				//PROC_WS.Cantidad				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;					
				
				PROC_WS.MontoUI					= null;

				// 2DA CONDICION ????						
				if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ||
					TipoValorizacion.IdTipoInstrumento.equalsIgnoreCase("AUT")) {

					PROC_WS.SubTipoValorizacion	= "PAGARE";
					PROC_WS.Tasa				= Instrumento.TasaCompra;
					PROC_WS.Precio				= null;
					PROC_WS.Cantidad			= null;

				} else {
					PROC_WS.EsBonoCero					= Instrumento.Emisor.equals("CERO") ? "S" : "N";
					PROC_WS.SubTipoValorizacion			= PROC_WS.SubTipoValorizacion;
					PROC_WS.Tasa						= !EsInformeSimultaneas ? ValorizarPor.equals("T") ? Instrumento.TasaCompra : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompra : null) : Instrumento.TasaCompra;
					PROC_WS.Precio						= ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);
					PROC_WS.Cantidad					= Instrumento.Cantidad;						
					PROC_WS.ValorRescate				= null;
					PROC_WS.IdMonedaEmisionInstrumento 	= StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
				}

				// Monto solo si Tasa y Precion en CERO
				if(EsInformeCarteraPacto && !ValorizaATasaMercado) {
					PROC_WS.Monto = (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? 
									Instrumento.MontoCompraMonedaSistema != null ?	
											Instrumento.MontoCompraMonedaSistema : Instrumento.MontoCompra : null;
				}
				
				
				if ( EsInformeCarteraPacto && ValorizaATasaPacto ) {
					PROC_WS.ValorizaATasaPacto = "S";
					PROC_WS.FechaEmision = Instrumento.FechaOperacion;
					PROC_WS.FechaVencimiento = Instrumento.FechaVencimientoOperacion;
					PROC_WS.IdMonedaInstrumento = Instrumento.IdMonedaInstrumento;
					PROC_WS.Monto = Instrumento.MontoCompra;						
					PROC_WS.ValorizaCorteDeCupon = "N";
				}
				
				// 1ra Valorizacion > F.Inicio
				// -------------------------------------------------------
				ValorizacionDatosEntrada.add(PROC_WS);
				// -------------------------------------------------------
				
				
				//Para busqueda de precio y tasa a mercado 
				//MAME 1ra Valorizacion a Compra
				
				if ( ValorizaATasaMercado && !(EsInformeMayorMenorValor || EsInformeMayorMenorValorRV) ) { 
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;
					PT_Mercado.FechaConsulta	= Fecha.toDate(FechaValorizacionInicio);
					PT_Mercado.PlazoResidual 	= EsInformeCarteraPacto && !ValorizaATasaMercado ? PlazoResidualActualOperacion : PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= PROC_WS.NemoSVS;
										
					// Verificar ??!!!
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {						
						PT_Mercado.IdMoneda			= StringUtils.isBlank(PROC_WS.IdMonedaEmisionInstrumento) ? PROC_WS.IdMonedaInstrumento:PROC_WS.IdMonedaEmisionInstrumento;
					}

					ConsultarPreciosTasas.add(PT_Mercado);
				}
				
			
				// -----------------------------------------------------------
				// 2da Valorizacion > F.Fin
				// -----------------------------------------------------------

				PROC_WS = null;
				PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
				
				PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i++);
				PROC_WS.DimTasa					= IdTipoTasaValorizacion; //"COMP"; 
				PROC_WS.DimTiempo				= "PR";
				PROC_WS.IdUso					= IdUso;
				PROC_WS.TipoCartera				= TipoCartera;
				PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
				PROC_WS.EsCuentaPropia			= EsCuentaPropia;
				PROC_WS.ValorizaCorteDeCupon	= InformacionAdicionalInstrumento.NumeroCupones > 0 && in.p_FechaFinal.compareTo(in.p_FechaInicial) > 0 ? "S" : "N";
				PROC_WS.FechaOperacion          = Instrumento.FechaOperacion;
				
				// Pacto vence antes de F.Valorizacion ?
				if ( ( EsInformeCarteraPacto || EsInformeSimultaneas )
					 && (Fecha.toDate(Instrumento.FechaVencimientoOperacion).compareTo( Fecha.toDate(FechaValorizacionFin)) < 0 ) ) {
					PROC_WS.FechaCalculada 		= Instrumento.FechaVencimientoOperacion;
					PROC_WS.PlazoResidual		= (Long) ((Fecha.toDate(Instrumento.FechaVencimientoOperacion).getTime()-Fecha.toDate(PROC_WS.FechaCalculada).getTime())/86400000);
				} else if ((EsInformeMayorMenorValor || EsInformeMayorMenorValorRV) && Fecha.toDate(Instrumento.FechaLiquidacion).compareTo(Fecha.toDate(FechaValorizacionFin)) > 0 ) {
					PROC_WS.FechaCalculada 		= Instrumento.FechaLiquidacion;
					PROC_WS.PlazoResidual		= (Long) ((Fecha.toDate(Instrumento.FechaVencimientoInstrumento).getTime()-Fecha.toDate(PROC_WS.FechaCalculada).getTime())/86400000);
				} else {
					PROC_WS.FechaCalculada		= Instrumento.FechaVencimientoInstrumento.compareTo(FechaValorizacionFin) > 0 || "0001-01-01".equals(Instrumento.FechaVencimientoInstrumento) ? FechaValorizacionFin : Instrumento.FechaVencimientoInstrumento;
					PROC_WS.PlazoResidual		= Instrumento.FechaVencimientoInstrumento != null ? PlazoResidualProximoInstrumento : PlazoResidualProximoOperacion;					
				}
				
				PROC_WS.ValorizaPor				= ValorizarPor;
				PROC_WS.Cantidad				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;
				
				// No se calculan intereses si C.PROP : la cartera en F.Fin no esta materializada
				// No se calculan intereses si C.PACT : La operacion vence <= de F.Fin	&& F.Ope = F.Ini
				
				PROC_WS.CalculaIntereses = "N";
				if ( ( EsInformeCarteraPropia || EsInformeSimultaneas || EsInformeMayorMenorValorRV ) && Instrumento.FechaLiquidacion != null 
					  && (Fecha.toDate(in.p_FechaFinal).compareTo( Fecha.toDate(Instrumento.FechaLiquidacion)) > 0 ) ) {
					PROC_WS.CalculaIntereses = "S";
				} else if (EsInformeMayorMenorValor) {
					PROC_WS.CalculaIntereses = "S";
				} else if ( EsInformeCarteraPacto
					  		&& !(Fecha.toDate(in.p_FechaInicial).compareTo( Fecha.toDate(Instrumento.FechaOperacion)) < 0 ) ) {
					PROC_WS.CalculaIntereses = "S";
				}
				
				PROC_WS.OrigenValorizacion		= in.p_OrigenProcesamiento;					

				if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ||
					TipoValorizacion.IdTipoInstrumento.equalsIgnoreCase("AUT")) {

					PROC_WS.EsBonoCero				= "N";
					PROC_WS.SubTipoValorizacion		= "PAGARE";
					PROC_WS.Tasa					= Instrumento.TasaCompra;
					PROC_WS.Precio					= null;
					//PROC_WS.Cantidad				= null;	
					PROC_WS.ValorRescate			= PROC_WS.ValorRescate;
					
				} else {
					PROC_WS.EsBonoCero				= Instrumento.Emisor.equals("CERO") ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= PROC_WS.SubTipoValorizacion;
					PROC_WS.Tasa					= !EsInformeSimultaneas ? ValorizarPor.equals("T") ? Instrumento.TasaCompra : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompra : null) : Instrumento.TasaCompra;
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);
					//PROC_WS.Cantidad				= Instrumento.Cantidad;
					PROC_WS.ValorRescate			= null;
					PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
				}
			
				// Monto solo si Tasa y Precion en CERO
				if(EsInformeCarteraPacto && !ValorizaATasaMercado) {
					PROC_WS.Monto = (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? 
							Instrumento.MontoCompraMonedaSistema != null ?	
									Instrumento.MontoCompraMonedaSistema : Instrumento.MontoCompra : null;
				}
				
				// ???
				// Si es 1er dia habil + Letra Hipotecaria => puede que cambie de Nemo.
				if ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") 
					 && InformacionAdicionalInstrumento.IdTipoInstrumento.equals("LTH") ) {

					Calendar cal = Calendar.getInstance();
			        cal.setTimeInMillis(Fecha.toDate(FechaValorizacionInicio).getTime());
			        int MesAnt = cal.get(Calendar.MONTH);
					
			        cal.setTimeInMillis(Fecha.toDate(FechaValorizacionFin).getTime());
			        int MesSgt = cal.get(Calendar.MONTH);

					if ( MesAnt != MesSgt ) {
						PROC_WS.NemoOriginal = PROC_WS.Nemotecnico;
						PROC_WS.Nemotecnico  = DeterminaNuevoNemo.NuevoNemo(PROC_WS.Nemotecnico, FechaValorizacionFin);
						PROC_WS.NemoSVS 	 = PROC_WS.Nemotecnico;
					}
				}

				
				if ( EsInformeCarteraPacto && ValorizaATasaPacto ) {
					
					PROC_WS.ValorizaATasaPacto = "S";
					PROC_WS.FechaEmision = Instrumento.FechaOperacion;
					PROC_WS.FechaVencimiento = Instrumento.FechaVencimientoOperacion;
					PROC_WS.IdMonedaInstrumento = Instrumento.IdMonedaInstrumento;
					PROC_WS.IdReajusteCabecera = Instrumento.IdTipoReajusteCabecera;
					PROC_WS.Monto = Instrumento.MontoCompra;						
					PROC_WS.ValorizaCorteDeCupon = "N";
				}

				if ( ValorizaATasaMercado ) { 
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;
					PT_Mercado.FechaConsulta	= Fecha.toDate(FechaValorizacionFin);
					PT_Mercado.PlazoResidual 	= EsInformeCarteraPacto && !ValorizaATasaMercado ? PlazoResidualProximoOperacion : PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= PROC_WS.NemoSVS;
					
					// Verificar ??!!!
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {						
						PT_Mercado.IdMoneda			= StringUtils.isBlank(PROC_WS.IdMonedaEmisionInstrumento) ? PROC_WS.IdMonedaInstrumento:PROC_WS.IdMonedaEmisionInstrumento;
					}

					ConsultarPreciosTasas.add(PT_Mercado);
				}

				// 2da Valorizacion > F.Fin
				// -------------------------------------------------------
				if ( !EsInformeCoberturaPatrimonial ) {
					ValorizacionDatosEntrada.add(PROC_WS);
				}
				// -------------------------------------------------------
				
			};					
				
			logger.debug("Fin Loop Inicializaci�n Instrumentos");
			
			//**************************************************************************************************************************************************
			//*********************************** OBTENER TASAS Y PRECIOS DE MERCADO ***************************************************************************
			//**************************************************************************************************************************************************
			if ( ValorizaATasaMercado ) {
				EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
				
				entregarPreciosTasasEntrada.p_IdEmpresa 				= in.p_IdEmpresa;
				entregarPreciosTasasEntrada.p_IdUsuario  				= in.p_IdUsuario;			
				entregarPreciosTasasEntrada.p_IdModulo 					= in.p_IdModulo;			
				entregarPreciosTasasEntrada.p_IdPais 					= in.p_IdPais;
				entregarPreciosTasasEntrada.p_DatosInstrumentos         = ConsultarPreciosTasas;
				
				if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
					EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
					logger.debug("Inicio EntregarPrecioTasa");
					EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
					logger.debug("Fin EntregarPrecioTasa");
					HashMap<Long, Object> precios = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS,"IdRegistro");
					
					for (ObjProcesamiento VDE : ValorizacionDatosEntrada){
						if("AN".equals(VDE.DimTiempo)) continue;
																	
						EntregarPreciosTasas_Salida_PreciosTasas PrecioTasaMercado = (EntregarPreciosTasas_Salida_PreciosTasas) precios.get(VDE.IdProcesamiento);
						if ( (EsInformeMayorMenorValor || EsInformeMayorMenorValorRV) && PrecioTasaMercado == null && "AC".equals(VDE.DimTiempo) ) {
							// MAME y 1ra Valorizacion?? => es a tasa compra, no es error
							continue;
							
						} else if ((PrecioTasaMercado == null || PrecioTasaMercado.MensajeError != null)) {
							VDE.MensajeErrorFuente 	= PrecioTasaMercado != null ? PrecioTasaMercado.MensajeError +"-"+ PrecioTasaMercado.CodigoFuente: "ERROR OBTENCION PRECIO TASA MERCADO";
							VDE.Tasa				= new BigDecimal(0);
							VDE.Precio				= new BigDecimal(0);
							continue;
						}
						
						VDE.FechaPrecioTasa = Fecha.toDate(Fecha.toStringDate(PrecioTasaMercado.FechaFuente));
						VDE.FuentePrecioTasa = PrecioTasaMercado.CodigoFuente;
						
						// Si no hay precio o es cero debe ir por tasa
						if ( VDE.IdClasificacionTipoInstrumento.equals("IF") || VDE.IdClasificacionTipoInstrumento.equals("RF") ) {
							VDE.Precio	= PrecioTasaMercado.Precio;
							VDE.Tasa	= PrecioTasaMercado.Tasa == null ? BigDecimal.ZERO : PrecioTasaMercado.Tasa;
							if ( VDE.ValorizaPor.equals("P") ) {
								if ( VDE.Precio == null || VDE.Precio.compareTo(BigDecimal.ZERO) == 0 ) {
									VDE.Precio	= null;
								} else { 
									VDE.Tasa	= null;
								}
							} else {
								if ( PrecioTasaMercado.Tasa != null ) {
									VDE.Precio	= null;
								} else { 
									VDE.Tasa	= VDE.Precio == null || VDE.Precio.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null ;
								}
								
							}
						} else {
							VDE.Tasa	= VDE.ValorizaPor.equals("T") ? PrecioTasaMercado.Tasa : (PrecioTasaMercado.Precio == null ? PrecioTasaMercado.Tasa : null);
							VDE.Precio	= VDE.ValorizaPor.equals("P") ? PrecioTasaMercado.Precio : (PrecioTasaMercado.Tasa == null ? PrecioTasaMercado.Precio : null);
						}
												
						if (VDE.SubTipoValorizacion.equals("PAGARE") && VDE.Tasa == null && VDE.TasaEmision != null ){
							VDE.Tasa 	= VDE.Tasa == null && VDE.Precio != null ? ( (VDE.Precio.multiply(VDE.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : VDE.Tasa;
							//VDE.Precio 	= VDE.Precio == null && VDE.Tasa != null ? ( (VDE.Tasa.multiply(new BigDecimal(100))).divide(VDE.TasaEmision,20,RoundingMode.HALF_UP) ) : VDE.Precio;
						};
					
						if("RF".equals(VDE.IdClasificacionTipoInstrumento) && VDE.Tasa.compareTo(tasaMaximaValorizadorRF) == 1) {
							VDE.Tasa = tasaMaximaValorizadorRF;
						}
					};
				};
				
				for (ObjProcesamiento VDE : ValorizacionDatosEntrada){				
					if(VDE.MensajeErrorFuente!= null && (VDE.IdClasificacionTipoInstrumento.equals("IF") || VDE.IdClasificacionTipoInstrumento.equals("RF"))) {
							out.P_OUT_ERRORVALORIZACIONES.add(VDE);				
					}					
				}
			}

			Map<Long, List<ObjProcesamiento>> hashValorizacionDatosEntrada = ValorizacionDatosEntrada.stream().collect(Collectors.groupingBy(t -> t.IdValorizacion));	
			
			//**************************************************************************************************************************************************
			//************************************ VALORIZAR CARTERAS ******************************************************************************************
			//**************************************************************************************************************************************************
			HashMap<Long, Object> ResultCalculoInteres = this.calcularInteres(con, in, hashValorizacionDatosEntrada, TipoValorizacionPorTipoInstrumento);
			Valorizar.InValorizar EntradaValorizador = new Valorizar.InValorizar();
			List<Valorizar.InValorizar.InstrumentoWS> ListaValorizacionSubyacentePacto = new ArrayList<Valorizar.InValorizar.InstrumentoWS>();
			
			EntradaValorizador.p_IdEmpresa 				= in.p_IdEmpresa;
			EntradaValorizador.p_IdUsuario 				= in.p_IdUsuario;
			EntradaValorizador.p_IdModulo 				= in.p_IdModulo;
			EntradaValorizador.p_IdPais 				= in.p_IdPais;			
			EntradaValorizador.p_IdMonedaAdicional		= in.p_IdMonedaAdicional;

			
			for (ObjProcesamiento VDE : ValorizacionDatosEntrada){
				if (VDE.Precio == null && VDE.Tasa == null && VDE.Monto == null) continue;
				ObjTipoValorizacionPorTipoInstrumento TipoValorizacion = (ObjTipoValorizacionPorTipoInstrumento) TipoValorizacionPorTipoInstrumento.get(VDE.IdTipoInstrumento);
				
				if ( VDE.TipoValorizacion.endsWith("PQ") ){
					Valorizar.InValorizar.InstrumentoPQ InsPQ = new Valorizar.InValorizar.InstrumentoPQ();
					
					InsPQ.IdValorizacion = VDE.IdValorizacion; 
					InsPQ.IdInstrumento = VDE.IdInstrumento; 
					InsPQ.Nemotecnico = VDE.Nemotecnico; 
					InsPQ.IdMonedaInstrumento = VDE.IdMonedaInstrumento; 
					InsPQ.Cantidad = VDE.Cantidad; 
					InsPQ.Precio = VDE.Precio; 
					InsPQ.Monto = VDE.Monto;
					InsPQ.Tasa = VDE.Tasa;
					
					InsPQ.IdValorizacion	= VDE.IdProcesamiento;
					InsPQ.FechaValorizacion	= VDE.FechaCalculada.substring(0,10);
					InsPQ.PlazoActual = new BigDecimal(0L);
					if( EsInformeSimultaneas && !"AN".equals(VDE.DimTiempo)) {
						 if( "AC".equals(VDE.DimTiempo) ) {
							 InsPQ.PlazoActual = BigDecimal.valueOf(((Fecha.toDate(FechaValorizacionInicio).getTime()-Fecha.toDate(VDE.FechaOperacion).getTime())/86400000));
						 } else if("PR".equals(VDE.DimTiempo)) {
							 InsPQ.PlazoActual = BigDecimal.valueOf(((Fecha.toDate(FechaValorizacionFin).getTime()-Fecha.toDate(VDE.FechaOperacion).getTime())/86400000));
						 }
					}
					EntradaValorizador.p_InstrumentosPQ.add(InsPQ);
				};
				
				
				if ( VDE.TipoValorizacion.endsWith("WS") ){
					Valorizar.InValorizar.InstrumentoWS InsWS = new Valorizar.InValorizar.InstrumentoWS();
					
					InsWS.IdValorizacion = VDE.IdValorizacion; 
					InsWS.IdInstrumento = VDE.IdInstrumento; 
					InsWS.Nemotecnico = VDE.Nemotecnico; 
					InsWS.FechaEmision = VDE.FechaEmision; 					
					InsWS.FechaVencimiento = VDE.FechaVencimiento; 
					InsWS.IdMonedaInstrumento = VDE.IdMonedaInstrumento;
					InsWS.IdMonedaEmisionInstrumento = VDE.IdMonedaEmisionInstrumento;
					InsWS.IdTipoReajusteInstrumento = VDE.IdTipoReajusteInstrumento;
					InsWS.Cantidad = VDE.Cantidad; 
					InsWS.ValorRescate = VDE.ValorRescate; 
					InsWS.Precio = VDE.Precio; 
					InsWS.Tasa = VDE.Tasa; 
					InsWS.Monto = VDE.Monto; 
					InsWS.IdClasificacionTipoInstrumento = VDE.IdClasificacionTipoInstrumento;
					InsWS.Emisor = VDE.Emisor;
					InsWS.IdCondicionDeLiquidacion = VDE.IdCondicionDeLiquidacion;
					InsWS.TipoValorizacionInstrumento = VDE.TipoValorizacionInstrumento;
					InsWS.SubTipoValorizacion = VDE.SubTipoValorizacion;
					InsWS.TipoCalculoInteresesCupon = VDE.TipoCalculoInteresesCupon;
					InsWS.ValorizaCorteDeCupon = VDE.ValorizaCorteDeCupon;
					InsWS.OrigenValorizacion = VDE.OrigenValorizacion;
					InsWS.MontoUI = VDE.MontoUI;
					
					InsWS.TipoValorizacionInstrumento	= localizacionSistema.equals("CL") && (VDE.TipoValorizacionInstrumento == null || VDE.TipoValorizacionInstrumento.equals("")) ? null : VDE.TipoValorizacionInstrumento; //Se usa en valorizacion Chile y se obtiene desde bursatil
					InsWS.IdValorizacion				= VDE.IdProcesamiento;
					InsWS.FechaValorizacion				= VDE.FechaCalculada.substring(0,10);
					InsWS.Base							= TipoValorizacion.BaseCalculoDeInteres.intValue();
					InsWS.IdReajusteCabecera 			= VDE.IdReajusteCabecera;
					InsWS.IdMonedaInstrumento           = VDE.IdMonedaInstrumento;
					InsWS.IdReajusteCabecera            = VDE.IdReajusteCabecera;
					
					//Filtra Valorizacion Subyacente Pacto
					if( EsInformeCarteraPacto && ValorizaATasaPacto ) {
						ListaValorizacionSubyacentePacto.add(InsWS);
					}else {
						EntradaValorizador.p_InstrumentosWS.add(InsWS);
					}
					
				};
			};					
			
			// En esta version de valorizaci�n los registros no vienen mezclados, solo 1 tipo de valorizaci�n por llamada
			SalidaValorizarSubyacentePacto SalidaValorizarSubyacentePacto = new SalidaValorizarSubyacentePacto();
			Valorizar.OutValorizar ResultValorizador = new Valorizar.OutValorizar();
			
			 if ( EsInformeCarteraPacto && ValorizaATasaPacto ) { 
				ValorizarSubyacentePacto ValorizacionDetallePacto = new ValorizarSubyacentePacto();
				EntradaValorizarSubyacentePacto EntradaValorizarSubyacentePacto = new EntradaValorizarSubyacentePacto();
				SalidaValorizarSubyacentePacto = new SalidaValorizarSubyacentePacto();
				
				if(!ListaValorizacionSubyacentePacto.isEmpty()) {
					EntradaValorizarSubyacentePacto.p_IdEmpresa 	= in.p_IdEmpresa;
					EntradaValorizarSubyacentePacto.p_IdUsuario 	= in.p_IdUsuario;
					EntradaValorizarSubyacentePacto.p_IdModulo 		= in.p_IdModulo;
					EntradaValorizarSubyacentePacto.p_CalculaFactor = "S";
					EntradaValorizarSubyacentePacto.p_InstrumentosWS = ListaValorizacionSubyacentePacto;	
					
					SalidaValorizarSubyacentePacto = ValorizacionDetallePacto.logica(con, EntradaValorizarSubyacentePacto);
					ResultValorizador.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.putAll(SalidaValorizarSubyacentePacto.P_RESULTADOS_VALORIZACION);
							
				}
			} 
			 
			if (!EntradaValorizador.p_InstrumentosWS.isEmpty() || !EntradaValorizador.p_InstrumentosPQ.isEmpty()) {
				Valorizar Valorizador = new Valorizar();
				ResultValorizador = Valorizador.logica(con, EntradaValorizador);
			}
			
			logger.debug("Fin Valorizador");
			
			//**************************************************************************************************************************************************
			//************************************* PREPARAR SALIDA ********************************************************************************************
			//**************************************************************************************************************************************************
			
			ValorizarInformeSalida salida = this.ordenarResultadoaValorizacion(con, in, ResultValorizador, hashValorizacionDatosEntrada, InfoInstrumentos, ResultCalculoInteres);			
			out.P_OUT_INSTRUMENTOSVALORIZADOS= salida.P_OUT_INSTRUMENTOSVALORIZADOS;


			//**************************************************************************************************************************************************
			//************************************ GRABAR VALORIZACION  ****************************************************************************************
			//**************************************************************************************************************************************************
			
			i = 0;
			// Solo para Pruebas (RESUMEN)
			List<ValorizarInforme.ObjResultadoResumen> ListaResumen = new ArrayList<ValorizarInforme.ObjResultadoResumen>();
			List<Long> secVal = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::IdValorizacionInformeRFIF", Long.valueOf(in.p_Instrumentos.size()));			
			List<ValorizarInforme.TablaValorizacionRFIF> RegistrosValorizados = new ArrayList<ValorizarInforme.TablaValorizacionRFIF>();
			Map<Long, ObjResultadoValorizacion> WSVal = out.P_OUT_INSTRUMENTOSVALORIZADOS.stream().collect(Collectors.toMap(obj -> obj.IdValorizacion, obj -> obj));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			
			//String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(con,in.p_IdEmpresa);
			IdLlaveInforme = in.p_OrigenProcesamiento + in.p_IdTipoTasaValorizacion + in.p_FechaInicial + in.p_FechaFinal + in.p_IdFechaHoraSistema;
			out.LlaveReporte = IdLlaveInforme;
			
			for (ObjEntradaInstrumentos Instrumento: in.p_Instrumentos){
							
				//Cruzo Entrada de Instrumento con Resultado de Valorizaciones
				ObjResultadoValorizacion rsVal = (ObjResultadoValorizacion) WSVal.get(Instrumento.IdValorizacion);
				ValorizarInforme.TablaValorizacionRFIF Rec = new ValorizarInforme.TablaValorizacionRFIF();
				
				Rec.IdValorizacion = secVal.get(i).longValue();
				Rec.IdInforme	   = IdLlaveInforme;
				Rec.IdCartera 	   = Instrumento.IdCartera;
				Rec.FolioCartera   = Instrumento.FolioCartera;
				Rec.FolioOperacion = Instrumento.FolioOperacion;
				Rec.IdEmpresa      = in.p_IdEmpresa;
				Rec.IdClienteSolicitante = in.p_IdClienteSolicitante;
				
				// Operacion ---------------------------------------------------------------
				Rec.IdTipoOperacion = Instrumento.IdTipoOperacion;
				Rec.IdCliente = Instrumento.IdCliente; 
				Rec.IdPortafolio = Instrumento.IdPortafolio;				
				Rec.FechaOperacion = rsVal.FechaOperacion == null ? null : Fecha.toStringDate(rsVal.FechaOperacion);							
				Rec.FechaVencimientoOperacion = Instrumento.FechaVencimientoOperacion;
				Rec.IdOperacion = Instrumento.IdOperacion;
				Rec.PlazoEnDiasOperacion = 0L;
				Rec.IdCondicionLiquidacion = Instrumento.IdCondicionLiquidacion;
				Rec.FechaLiquidacion = Instrumento.FechaLiquidacion;
				Rec.Diferenciador = Instrumento.Diferenciador;
				
				// Instrumento -------------------------------------------------------------
				Rec.IdClasificacionTipoInstrumento = rsVal.IdClasificacionTipoInstrumento;
				Rec.IdTipoInstrumento = Instrumento.IdTipoInstrumento; 
				Rec.IdInstrumento = rsVal.IdInstrumento;
				Rec.Emisor = rsVal.Emisor;								//valorizacionCL		
				Rec.Nemotecnico = rsVal.Nemotecnico;
				Rec.NemoSVS = rsVal.NemoSVS;
				Rec.NemoOriginal = rsVal.NemoOriginal;
				Rec.IdTipoReajuste = rsVal.IdTipoReajuste;
				Rec.IdMonedaInstrumento = rsVal.IdMonedaInstrumento;
				Rec.IdMonedaEmisionInstrumento = Instrumento.IdMonedaEmisionInstrumento;
				Rec.IdTipoReajusteInstrumento = Instrumento.IdTipoReajusteInstrumento;
				Rec.IdMonedaAdicional = in.p_IdMonedaAdicional;
				Rec.FechaEmision = Instrumento.FechaEmision;						//??? CUAL???
				Rec.FechaVencimientoInstrumento = Instrumento.FechaVencimientoInstrumento;
				Rec.FechaVencimientoOperacion = Instrumento.FechaVencimientoOperacion;
				Rec.FechaEmisionInstrumento= Instrumento.FechaEmisionInstrumento;						//??? CUAL???
				Rec.TieneCorteCupon = rsVal.TieneCorteDeCupon;
				Rec.CodigoIFRS = Instrumento.CodigoIFRS;
				Rec.EsFungible = Instrumento.EsFungible;
				Rec.FolioComprobante = Instrumento.FolioComprobante;

				// Valor Actual ------------------------------------------------------------
				Rec.Cantidad = Instrumento.Cantidad; 
				Rec.ValorRescate = Instrumento.ValorRescate; 
				Rec.PrecioCompra = Instrumento.PrecioCompra; 
				Rec.TasaCompra = Instrumento.TasaCompra; //Desde instrumento;
				Rec.MontoCompra = rsVal.MontoCompra;	
				Rec.MontoCompraMonedaSistema = Instrumento.MontoCompraMonedaSistema;
				Rec.MontoCompraMonedaInstrumento = Instrumento.MontoCompraMonedaInstrumento;
				Rec.MontoCompraMonedaAdicional = Instrumento.MontoCompraMonedaAdicional;
				Rec.PrecioAPlazo = Instrumento.PrecioAPlazo;
				Rec.MontoAPlazo	= Instrumento.MontoAPlazo;
				Rec.MontoAPlazoMonedaInstrumento = Instrumento.MontoAPlazoMonedaInstrumento;
				Rec.MontoAPlazoMonedaAdicional = Instrumento.MontoAPlazoMonedaAdicional;
				
				// Valorizaci�n Fecha Inicio/Consulta --------------------------------------
				Rec.FechaValorizacionActual = in.p_FechaInicial;
				Rec.FechaFactorMonedaInstrumentoActual = Fecha.toStringDate(rsVal.FechaFactorMonedaInstrumentoInicial);
				Rec.ValorFactorMonedaInstrumentoActual = rsVal.FactorMonedaInstrumentoInicial;
				Rec.FechaFactorMonedaAdicionalActual = Fecha.toStringDate(rsVal.FechaFactorMonedaAdicionalInicial);
				Rec.ValorFactorMonedaAdicionalActual = rsVal.FactorMonedaAdicionalInicial;
				Rec.TasaActual = rsVal.TasaInicial;
				Rec.PrecioActual = rsVal.PrecioInicial;
				Rec.MontoActual = rsVal.MontoMonedaSistemaInicial;
				Rec.MontoActualMonedaInstrumento = rsVal.MontoMonedaInstrumentoInicial;
				Rec.MontoActualMonedaAdicional = rsVal.MontoMonedaAdicionalInicial;
				Rec.FechaPrecioTasaMercadoActual = Fecha.toStringDate(rsVal.FechaPrecioTasaInicial);
				Rec.FuenteTasaPrecioMercadoActual = rsVal.FuentePrecioTasaInicial;
				Rec.PlazoOperacionActual = rsVal.PlazoResidualFinal == null ? BigDecimal.ZERO : BigDecimal.valueOf(rsVal.PlazoResidualInicial);
				if ( ValorizaATasaMercado && rsVal.FuentePrecioTasaInicial == null) {
					Rec.FuenteTasaPrecioMercadoActual = rsVal.MensajeErrorFuenteInicial;
				}
				
				// Valorizaci�n Fecha Fin/Hasta ---------------------------------------------	
				Rec.FechaValorizacionProximo = rsVal.FechaValorizacionFinal == null ? in.p_FechaFinal :  rsVal.FechaValorizacionFinal;
				Rec.PlazoOperacionProximo = BigDecimal.valueOf(-2);
				Rec.FechaFactorMonedaInstrumentoProximo = Fecha.toStringDate(rsVal.FechaFactorMonedaInstrumentoFinal); 
				Rec.ValorFactorMonedaInstrumentoProximo = rsVal.FactorMonedaInstrumentoFinal;
				Rec.FechaFactorMonedaAdicionalProximo = Fecha.toStringDate(rsVal.FechaFactorMonedaAdicionalFinal);
				Rec.ValorFactorMonedaAdicionalProximo = rsVal.FactorMonedaAdicionalFinal;
				Rec.TasaProximo = rsVal.TasaFinal;
				Rec.PrecioProximo = rsVal.PrecioFinal;
				Rec.MontoProximo = rsVal.MontoMonedaSistemaFinal;
				Rec.MontoProximoMonedaInstrumento = rsVal.MontoMonedaInstrumentoFinal;
				Rec.MontoProximoMonedaAdicional = rsVal.MontoMonedaAdicionalFinal;
				Rec.FechaPrecioTasaMercadoProximo = Fecha.toStringDate(rsVal.FechaPrecioTasaFinal);
				Rec.FuenteTasaPrecioMercadoProximo = rsVal.FuentePrecioTasaFinal;
				Rec.PlazoOperacionProximo = rsVal.PlazoResidualFinal == null ? BigDecimal.ZERO : BigDecimal.valueOf(rsVal.PlazoResidualFinal);
				if ( ValorizaATasaMercado && rsVal.FuentePrecioTasaFinal == null) {
					Rec.FuenteTasaPrecioMercadoProximo = rsVal.MensajeErrorFuenteFinal;
				}
				
				// Intereses y Reajustes entre Valorizaciones ------------------------------
				Rec.MontoInteres = rsVal.MontoInteresMonedaSistema ;
				Rec.MontoReajuste = rsVal.MontoReajusteMonedaSistema;
				Rec.MontoInteresMonedaInstrumento = rsVal.MontoInteresMonedaInstrumento;
				Rec.MontoReajusteMonedaInstrumento = rsVal.MontoReajusteMonedaInstrumento;
				Rec.MontoInteresMonedaAdicional = rsVal.MontoInteresMonedaAdicional;
				Rec.MontoReajusteMonedaAdicional = rsVal.MontoReajusteMonedaAdicional;
				
				// Intereses y Reajustes Acumulado (F.Operacion - F.Fin) ------------------
				Rec.MontoInteresCorrido = rsVal.MontoInteresAcumuladoMonedaSistema;
				Rec.MontoReajusteCorrido = rsVal.MontoReajusteAcumuladoMonedaSistema;
				Rec.MontoInteresCorridoMonedaInstrumento = rsVal.MontoInteresAcumuladoMonedaSistema;
				Rec.MontoReajusteCorridoMonedaInstrumento = rsVal.MontoReajusteAcumuladoMonedaSistema;
				Rec.MontoInteresCorridoMonedaAdicional = BigDecimal.ZERO;
				Rec.MontoReajusteCorridoMonedaAdicional = BigDecimal.ZERO;
				
				// Mayor y Menor Valor  ----------------------------------------------------
				Rec.MayorValorMonedaSistema		= rsVal.MayorValorMonedaSistema;		
				Rec.MayorValorMonedaInstrumento	= rsVal.MayorValorMonedaInstrumento;	
				Rec.MayorValorMonedaAdicional	= rsVal.MayorValorMonedaAdicional;	
				Rec.MenorValorMonedaSistema		= rsVal.MenorValorMonedaSistema;		
				Rec.MenorValorMonedaInstrumento	= rsVal.MenorValorMonedaInstrumento;	
				Rec.MenorValorMonedaAdicional	= rsVal.MenorValorMonedaAdicional;			

				// Corte Cupon -----------------------------------------------------------
				Rec.FechaCorteCupon = Fecha.toStringDate(rsVal.FechaCorteCupon);
				Rec.FechaPagoCorteCupon = Fecha.toStringDate(rsVal.FechaPagoCorteCupon);
				Rec.NumeroCupon = rsVal.NumeroCupon != null ? BigDecimal.valueOf(rsVal.NumeroCupon) : BigDecimal.ZERO ;
				Rec.IdMonedaPagoCupon = rsVal.IdMonedaPagoCupon;
				Rec.MontoCorteCupon = rsVal.MontoCorteCuponMonedaSistema;
				Rec.MontoInteresCorteCupon = rsVal.MontoInteresCorteCuponMonedaSistema;
				Rec.MontoReajusteCorteCupon = rsVal.MontoReajusteCorteCuponMonedaSistema;
				Rec.MontoCorteCuponMonedaInstrumento = rsVal.MontoCorteCuponMonedaInstrumento;
				Rec.MontoInteresCorteCuponMonedaInstrumento = rsVal.MontoInteresCorteCuponMonedaInstrumento;
				Rec.MontoReajusteCorteCuponMonedaInstrumento = rsVal.MontoReajusteCorteCuponMonedaInstrumento;
				Rec.MontoCorteCuponMonedaAdicional = rsVal.MontoCorteCuponMonedaAdicional;
				Rec.MontoInteresCorteCuponMonedaAdicional = rsVal.MontoInteresCorteCuponMonedaAdicional;
				Rec.MontoReajusteCorteCuponMonedaAdicional = rsVal.MontoReajusteCorteCuponMonedaAdicional;

				// Otros -------------------------------------------------------------------
				if("IF".equals(Instrumento.IdClasificacionTipoInstrumento) && Rec.TasaActual != null) {
					LocalDate fechaInicial = LocalDate.parse(in.p_FechaInicial, formatter);
					LocalDate fechaFinal = LocalDate.parse(Rec.FechaVencimientoInstrumento, formatter);	
					BigDecimal plazoActual = BigDecimal.valueOf(Math.abs(ChronoUnit.DAYS.between(fechaInicial,fechaFinal)));

					BigDecimal Duracion = plazoActual.divide(BigDecimal.valueOf(360), 6, RoundingMode.HALF_UP);
					BigDecimal tasaBruta = Rec.TasaActual;
					if ("PAGARE NR".equals(Rec.Nemotecnico)) {
					    tasaBruta = tasaBruta.multiply(BigDecimal.valueOf(12));
					}
					BigDecimal TASA = tasaBruta.divide(BigDecimal.valueOf(100));

			        Rec.Duration = Duracion.divide(BigDecimal.valueOf(1).add(TASA), 4, RoundingMode.DOWN);  
			    } else if ("RF".equals(Instrumento.IdClasificacionTipoInstrumento) && EsInformeCarteraPacto && ValorizaATasaPacto ) {
			    	LocalDate fechaInicial = LocalDate.parse(in.p_FechaFinal, formatter);
					LocalDate fechaFinal = LocalDate.parse(Rec.FechaVencimientoOperacion, formatter);	
					BigDecimal plazoActual = BigDecimal.valueOf(Math.abs(ChronoUnit.DAYS.between(fechaInicial,fechaFinal)));
					BigDecimal factor = Rec.TasaActual.divide(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(12)), 6 , RoundingMode.HALF_UP).add(BigDecimal.ONE);
					Rec.Duration = plazoActual.divide(BigDecimal.valueOf(360), 6, RoundingMode.HALF_UP).divide(factor, 6, RoundingMode.HALF_UP);			
			    }
				else {
					Rec.Duration = rsVal.Duration;
				}
				Rec.Convexidad = rsVal.Convexidad;
				Rec.TablaOrigen = Instrumento.TablaOrigen;
				Rec.NombreCliente = Instrumento.NombreCliente;
				Rec.IdBoveda = Instrumento.IdBoveda;
				Rec.IdAgente = Instrumento.IdAgente;
				Rec.IdConcepto = Instrumento.IdConcepto;
				Rec.MontoPactado = Instrumento.MontoPactado;
				Rec.IdCorredoraContraparte = Instrumento.IdCorredoraContraparte;
				Rec.TipoCarteraCorredor = Instrumento.TipoCarteraCorredor;
				Rec.IdTipoProducto = Instrumento.IdTipoProducto;
				
				// Parametros --------------------------------------------------------------
				Rec.prmTipoInforme = in.p_OrigenProcesamiento;		
				Rec.prmTipoValorizacion = in.p_IdTipoTasaValorizacion; 
				Rec.prmUsuario = in.p_IdUsuario; 
				
				//Rec.FechaEmisionReporte = Fecha.toDateTime(fechaHoraSistemaSecondate);
				Rec.FechaEmisionReporte = Fecha.toDateTime(in.p_IdFechaHoraSistema);
				Rec.IdBolsa = Instrumento.IdBolsa;
				Rec.IdMesa = Instrumento.IdMesa;
				Rec.PrecioAPlazo = Instrumento.PrecioAPlazo;
				
				//Simultaneas
				if( EsInformeSimultaneas ) {
					Rec.MontoInteres = Rec.MontoProximo != null && Rec.MontoActual != null && (Fecha.toDate(in.p_FechaFinal).compareTo( Fecha.toDate(Instrumento.FechaLiquidacion)) > 0 ) ? Rec.MontoProximo.subtract(Rec.MontoActual).setScale(0, RoundingMode.HALF_DOWN) : BigDecimal.ZERO;
					Rec.FolioTransaccionNormal = Instrumento.FolioTransaccionNormal;
					Rec.FolioTransaccionPlazo = Instrumento.FolioTransaccionPlazo;
				}
				
				RegistrosValorizados.add(Rec);
				i++;
				
				/*
				ValorizarInforme.ObjResultadoResumen rsm = new ValorizarInforme.ObjResultadoResumen();
				rsm.Emisor = rsVal.Emisor;
				rsm.Nemotecnico = rsVal.Nemotecnico; 
				rsm.NemoSVS = rsVal.NemoSVS;				
				rsm.IdMonedaInstrumento = rsVal.IdMonedaInstrumento;
				rsm.IdTipoReajusteInstrumento = rsVal.IdTipoReajuste;
				rsm.Cantidad = rsVal.Cantidad; 
				rsm.ValorRescate = rsVal.ValorRescate;
				rsm.MontoCompra = rsVal.MontoCompra;				
				rsm.MontoMonedaSistemaInicial = rsVal.MontoMonedaSistemaInicial;
				rsm.PrecioInicial = rsVal.PrecioInicial; 
				rsm.TasaInicial = rsVal.TasaInicial; 
				rsm.MontoInteresMonedaSistema = rsVal.MontoInteresMonedaSistema;
				rsm.MontoReajusteMonedaSistema = rsVal.MontoReajusteMonedaSistema;
				rsm.PrecioFinal = rsVal.PrecioFinal; 
				rsm.TasaFinal = rsVal.TasaFinal; 
				rsm.MontoMonedaSistemaFinal = rsVal.MontoMonedaSistemaFinal; 	
				ListaResumen.add(rsm);
				*/

			}
			
			
			if ( !RegistrosValorizados.isEmpty() ) {
				SQL _sql = new SQL();				
				_sql.batch(con, SQL_InsertCarteraValorizada, RegistrosValorizados, TablaValorizacionRFIF.class);
			}
			

			logger.debug("fin diccionario salida");
		}
		catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		logger.info(out.LlaveReporte);		
		return out;
	};
	
	
	private EntregarPreciosTasas_Entrada_DatosInstrumentos creaPojoEntregarPrecioTasa(ObjProcesamiento PROC_WS) {
		EntregarPreciosTasas_Entrada_DatosInstrumentos d = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
		
		//d.IdRegistro = PROC_WS.IdRegistro;
		d.TipoCartera = PROC_WS.TipoCartera;
		d.IdUso = PROC_WS.IdUso;
		d.IdTipoCriterioTasa = PROC_WS.IdTipoCriterioTasa;
		d.IdClasificacionTipoInstrumento = PROC_WS.IdClasificacionTipoInstrumento;
		d.IdTipoInstrumento = PROC_WS.IdTipoInstrumento;
		d.IdInstrumento = PROC_WS.IdInstrumento;
		d.Nemotecnico = PROC_WS.Nemotecnico;
		//d.IdMoneda = PROC_WS.IdMoneda;
		//d.FechaConsulta = PROC_WS.FechaConsulta;
		d.PlazoResidual = PROC_WS.PlazoResidual;
		//d.NemoSVS = PROC_WS.NemoSVS;
		
		return d;
	}

	private ObjProcesamiento creaPojoValorizacion(ObjEntradaInstrumentos instrumento,
			ObjTipoValorizacionPorTipoInstrumento tipoValorizacion, ObjInstrumento informacionAdicionalInstrumento) {
		
		ObjProcesamiento PROC_WS = new ObjProcesamiento();
		
		PROC_WS.IdValorizacion = instrumento.IdValorizacion;
		PROC_WS.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
		PROC_WS.IdTipoInstrumento = instrumento.IdTipoInstrumento;
		PROC_WS.IdInstrumento = instrumento.IdInstrumento;
		PROC_WS.Nemotecnico = instrumento.Nemotecnico;
		PROC_WS.NemoSVS = instrumento.NemoSVS;
		PROC_WS.FechaVencimiento = instrumento.FechaVencimientoInstrumento;
		PROC_WS.FechaEmision = instrumento.FechaEmision;
		PROC_WS.IdMonedaInstrumento = instrumento.IdMonedaInstrumento;
		PROC_WS.IdMonedaEmisionInstrumento = instrumento.IdMonedaEmisionInstrumento;
		PROC_WS.IdTipoReajusteInstrumento = instrumento.IdTipoReajusteInstrumento;
		PROC_WS.Emisor = instrumento.Emisor;
		PROC_WS.IdCondicionDeLiquidacion = null;
		PROC_WS.Cantidad = instrumento.Cantidad;
		PROC_WS.ValorRescate = instrumento.ValorRescate;
		PROC_WS.IdReajusteCabecera = instrumento.IdTipoReajusteCabecera;
		
		PROC_WS.TablaOrigen = instrumento.TablaOrigen;	
		PROC_WS.IdUso = instrumento.IdUso;
		PROC_WS.TipoCartera = instrumento.TipoCartera;
		PROC_WS.IdTipoCriterioTasa = instrumento.IdTipoCriterioTasa;

		PROC_WS.IdTipoInstrumento = tipoValorizacion.IdTipoInstrumento; 
		PROC_WS.CalculaIntereses = tipoValorizacion.CalculaIntereses;
		PROC_WS.CalculaInteresesAPlazo = tipoValorizacion.CalculaInteresesAPlazo;
		PROC_WS.TipoValorizacion = tipoValorizacion.TipoValorizacion;
		PROC_WS.SubTipoValorizacion = tipoValorizacion.SubTipoValorizacion;
		
		PROC_WS.IdInstrumento = informacionAdicionalInstrumento.IdInstrumento;
		PROC_WS.IdClasificacionTipoInstrumento = informacionAdicionalInstrumento.IdClasificacionTipoInstrumento;
		PROC_WS.IdTipoInstrumento = informacionAdicionalInstrumento.IdTipoInstrumento;
		PROC_WS.Nemotecnico = informacionAdicionalInstrumento.Nemotecnico;
		PROC_WS.Tamano = informacionAdicionalInstrumento.Tamano;
		PROC_WS.TasaEmision = informacionAdicionalInstrumento.TasaEmision;
		PROC_WS.ValorizaPor = informacionAdicionalInstrumento.ValorizaPor;
		PROC_WS.TipoValorizacionInstrumento = informacionAdicionalInstrumento.TipoValorizacionInstrumento;
		PROC_WS.TipoCalculoInteresesCupon = informacionAdicionalInstrumento.TipoCalculoInteresesCupon;
		PROC_WS.ValorClasificacionRiesgo = informacionAdicionalInstrumento.ValorClasificacionRiesgo;
		
		return PROC_WS;
	}
	
	private BigDecimal obtenerTasaMaximaValorizadorRF(Connection con, Long idEmpresa) {
		try {
			String tasa = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa,"PLATAFORMA", "PLA.TasaMaximaValorizadorRF");
			return new BigDecimal(tasa);
		} catch (Exception e) {
			logger.warn("obtenerTasaMaximaValorizadorRF:: No existe parametro PLA.TasaMaximaValorizadorRF, se toma valor por defecto");
			return TASA_MAXIMA_VALORIZADOR_RF_DEFAULT;
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ValorizarInformeEntrada jin=gson.fromJson(in.toString() ,ValorizarInformeEntrada.class);
		ValorizarInformeSalida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public ValorizarInformeSalida ordenarResultadoaValorizacion (Connection con,ValorizarInformeEntrada in,Valorizar.OutValorizar ResultValorizador
			, Map<Long, List<ObjProcesamiento>> hashValorizacionDatosEntrada,HashMap<Long,Object> InfoInstrumentos, HashMap<Long, Object> ResultCalculoInteres ) 
		throws Exception {		
		
		logger.debug("inicio actualizacion valorizacion");
		ValorizarInformeSalida out = new ValorizarInformeSalida();
		String MonedaSistema		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");	
		String MonedaAdicional		= in.p_IdMonedaAdicional == null?ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional"):in.p_IdMonedaAdicional;
		Date fechaCorteCupon = Fecha.toDate("0001-01-01");
		
		HashMap<Long, InstrumentoPQ> ResultValorizacionPQ = ResultValorizador.P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ;
		HashMap<Long, InstrumentoWS> ResultValorizacionWS = ResultValorizador.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS;
		
		for (ObjEntradaInstrumentos InInstrumento : in.p_Instrumentos ) {
			List<ObjProcesamiento> datosEntrada = hashValorizacionDatosEntrada.get(InInstrumento.IdValorizacion);
			
			if (datosEntrada == null) {
				continue;
			}
			
			ObjResultadoValorizacion Resultado = new ObjResultadoValorizacion();
			
			for (ObjProcesamiento WS: datosEntrada){					
				if ( !WS.IdValorizacion.equals( InInstrumento.IdValorizacion) ) continue;							
				
				//Informacion de valorizacion por WebService
				Valorizar.OutValorizar.InstrumentoWS ResultadoValorizacionWS = (Valorizar.OutValorizar.InstrumentoWS) ResultValorizacionWS.get(WS.IdProcesamiento);
				Valorizar.OutValorizar.InstrumentoPQ ResultadoValorizacionPQ = (Valorizar.OutValorizar.InstrumentoPQ) ResultValorizacionPQ.get(WS.IdProcesamiento);
				
				if ( "AC".equals(WS.DimTiempo) || "AN".equals(WS.DimTiempo) ) {
					// Se toma informacion BASE de la primera valorizacion
					Resultado.IdProcesamiento 	= WS.IdProcesamiento;
					Resultado.IdValorizacion 	= WS.IdValorizacion;
					Resultado.TipoValorizacion 	= WS.TipoValorizacion; 
					Resultado.SubTipoValorizacion = WS.SubTipoValorizacion;
					Resultado.TipoValorizacionInstrumento = WS.TipoValorizacionInstrumento;
					Resultado.IdUso 			= WS.IdUso; 
					Resultado.TipoCartera 		= WS.TipoCartera; 
					Resultado.IdTipoCriterioTasa = WS.IdTipoCriterioTasa;
					Resultado.IdClasificacionTipoInstrumento = WS.IdClasificacionTipoInstrumento;		
					Resultado.IdInstrumento 	= WS.IdInstrumento; 
					Resultado.Nemotecnico 		= WS.Nemotecnico;
					Resultado.NemoSVS			= WS.NemoSVS; 
					Resultado.Emisor 			= WS.Emisor;
					Resultado.Cantidad 			= WS.Cantidad;
					Resultado.ValorRescate 		= WS.ValorRescate;	
					Resultado.IdMonedaInstrumento = WS.IdMonedaInstrumento;
					Resultado.TasaEmision 		= WS.TasaEmision;
					Resultado.ValorClasificacionRiesgo = WS.ValorClasificacionRiesgo;
					Resultado.NemoOriginal 		= WS.NemoOriginal;				
					Resultado.ResultadoProceso	= "OK";
					Resultado.MensajeProceso	= "";
					Resultado.IdClasificacionTipoInstrumento = InInstrumento.IdClasificacionTipoInstrumento;
					Resultado.IdMonedaSistema	= MonedaSistema;
					Resultado.IdMonedaAdicional	= MonedaAdicional;
					Resultado.FechaOperacion	= Fecha.toDate(InInstrumento.FechaOperacion);
					Resultado.PlazoEnDiasOperacion 	= WS.PlazoResidual;

				}
				
				if (!valorizaMontoCompraAFechaCompra || ValorizaATasaPacto) {
					Resultado.MontoCompra = InInstrumento.MontoCompra;
				}
				
				if (ResultadoValorizacionPQ != null && WS.TipoValorizacion.equals("PQ")){
					
					Resultado.IdValorizacion = ResultadoValorizacionPQ.IdValorizacion;
					Resultado.FechaValorizacion = ResultadoValorizacionPQ.FechaValorizacion;
					Resultado.IdInstrumento = ResultadoValorizacionPQ.IdInstrumento;
					Resultado.Nemotecnico = ResultadoValorizacionPQ.Nemotecnico;
					Resultado.IdMonedaInstrumento  = ResultadoValorizacionPQ.IdMonedaInstrumento;
					Resultado.Cantidad = ResultadoValorizacionPQ.Cantidad;
					Resultado.Precio = ResultadoValorizacionPQ.Precio;
					Resultado.MontoMonedaSistema = ResultadoValorizacionPQ.MontoMonedaSistema;
					Resultado.MontoMonedaInstrumento = ResultadoValorizacionPQ.MontoMonedaInstrumento; 
					Resultado.MontoMonedaAdicional = ResultadoValorizacionPQ.MontoMonedaAdicional;
					Resultado.FactorMonedaInstrumento = ResultadoValorizacionPQ.FactorMonedaInstrumento; 
					Resultado.FechaFactorMonedaInstrumento = ResultadoValorizacionPQ.FechaFactorMonedaInstrumento;
					Resultado.FactorMonedaAdicional = ResultadoValorizacionPQ.FactorMonedaAdicional;
					Resultado.FechaFactorMonedaAdicional = ResultadoValorizacionPQ.FechaFactorMonedaAdicional; 
					Resultado.ResultadoProceso = ResultadoValorizacionPQ.ResultadoProceso;
					Resultado.Tasa = ResultadoValorizacionPQ.Tasa;
					Resultado.MensajeProceso = ResultadoValorizacionPQ.DescripcionProceso;
					Resultado.PlazoResidualInicial	= WS.PlazoResidual;
					Resultado.Cantidad = WS.Tamano > 0L && Resultado.Cantidad != null? Resultado.Cantidad.divide( new BigDecimal(WS.Tamano),20,RoundingMode.HALF_UP)  : Resultado.Cantidad;
					
					if("AC".equals(WS.DimTiempo) && (EsInformeMayorMenorValorRV || EsInformeSimultaneas)) {
						Resultado.PrecioInicial = EsInformeSimultaneas ? ResultadoValorizacionPQ.MontoMonedaSistema.divide(ResultadoValorizacionPQ.Cantidad ,20,RoundingMode.HALF_UP) : ResultadoValorizacionPQ.Precio;
						Resultado.TasaInicial = ResultadoValorizacionPQ.Tasa;
						Resultado.FechaValorizacionInicial = WS.FechaCalculada;
						
						Resultado.MontoMonedaSistemaInicial		= ResultadoValorizacionPQ.MontoMonedaSistema;		
						Resultado.MontoMonedaInstrumentoInicial	= ResultadoValorizacionPQ.MontoMonedaInstrumento;						
						Resultado.MontoMonedaAdicionalInicial	= ResultadoValorizacionPQ.MontoMonedaAdicional;
					} else if ("AN".equals(WS.DimTiempo)) {
						Resultado.MontoCompra = ResultadoValorizacionPQ.MontoMonedaSistema == null ? InInstrumento.MontoCompra : ResultadoValorizacionPQ.MontoMonedaSistema;
					} else if ("PR".equals(WS.DimTiempo)) {
						Resultado.PrecioFinal 			= EsInformeSimultaneas ? ResultadoValorizacionPQ.MontoMonedaSistema.divide(ResultadoValorizacionPQ.Cantidad ,20,RoundingMode.HALF_UP) : ResultadoValorizacionPQ.Precio;
						Resultado.TasaFinal				= ResultadoValorizacionPQ.Tasa;
						Resultado.FechaValorizacionFinal= WS.FechaCalculada;
						Resultado.PlazoResidualFinal	= WS.PlazoResidual;
						
						Resultado.MensajeErrorFuenteFinal = WS.MensajeErrorFuente;
						Resultado.MensajeValorizacionFinal = ResultadoValorizacionPQ.DescripcionProceso;
						
						Resultado.FactorMonedaInstrumentoFinal 	= ResultadoValorizacionPQ.FactorMonedaInstrumento; 
						Resultado.FechaFactorMonedaInstrumentoFinal = ResultadoValorizacionPQ.FechaFactorMonedaInstrumento; 
						Resultado.FactorMonedaAdicionalFinal 	= ResultadoValorizacionPQ.FactorMonedaAdicional; 
						Resultado.FechaFactorMonedaAdicionalFinal = ResultadoValorizacionPQ.FechaFactorMonedaAdicional;
						Resultado.MontoMonedaSistemaFinal		= ResultadoValorizacionPQ.MontoMonedaSistema;		
						Resultado.MontoMonedaInstrumentoFinal	= ResultadoValorizacionPQ.MontoMonedaInstrumento;						
						Resultado.MontoMonedaAdicionalFinal		= ResultadoValorizacionPQ.MontoMonedaAdicional;
						
						if (EsInformeMayorMenorValorRV) {
							if (Resultado.MontoMonedaAdicionalFinal != null && Resultado.MontoMonedaSistemaFinal.compareTo(Resultado.MontoMonedaSistemaInicial) > 0) {
								Resultado.MayorValorMonedaSistema = Resultado.MontoMonedaSistemaFinal.subtract(Resultado.MontoMonedaSistemaInicial).setScale(0, RoundingMode.HALF_UP);
								Resultado.MayorValorMonedaInstrumento = Resultado.MontoMonedaInstrumentoFinal.subtract(Resultado.MontoMonedaInstrumentoInicial).setScale(0, RoundingMode.HALF_UP);
								Resultado.MayorValorMonedaAdicional = Resultado.MontoMonedaAdicionalFinal.subtract(Resultado.MontoMonedaAdicionalInicial).setScale(4, RoundingMode.HALF_UP);
							} else if (Resultado.MontoMonedaAdicionalFinal != null && Resultado.MontoMonedaSistemaFinal.compareTo(Resultado.MontoMonedaSistemaInicial) < 0) {
								Resultado.MenorValorMonedaSistema = Resultado.MontoMonedaSistemaInicial.subtract(Resultado.MontoMonedaSistemaFinal).setScale(0, RoundingMode.HALF_UP);
								Resultado.MenorValorMonedaInstrumento = Resultado.MontoMonedaInstrumentoInicial.subtract(Resultado.MontoMonedaInstrumentoFinal).setScale(0, RoundingMode.HALF_UP);
								Resultado.MenorValorMonedaAdicional = Resultado.MontoMonedaAdicionalInicial.subtract(Resultado.MontoMonedaAdicionalFinal).setScale(4, RoundingMode.HALF_UP);
							}

						}
					}
					
					CalcularIntereses.ObjResultado ResultadoCalculoInteres = (CalcularIntereses.ObjResultado) ResultCalculoInteres.get(WS.IdProcesamiento);
					
					if(ResultadoCalculoInteres != null) {
						if("S".equals(WS.CalculaIntereses) && (EsInformeSimultaneas || EsInformeMayorMenorValorRV) && Fecha.toDate(in.p_FechaFinal).compareTo(Fecha.toDate(in.p_FechaInicial)) >= 0) {
								BigDecimal PrecioPlazo                                  = InInstrumento.PrecioCompra.subtract(new BigDecimal(1)).multiply(InInstrumento.TasaCompra).multiply(InInstrumento.PrecioCompra);
								Resultado.PrecioAPlazo									= PrecioPlazo.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
								
								Resultado.MontoAPlazoMonedaSistema						= ResultadoCalculoInteres.MontoMonedaConsulta;
								Resultado.MontoInteresAPlazoMonedaSistema				= Fecha.toDate(in.p_FechaFinal).compareTo(Fecha.toDate(in.p_FechaInicial)) > 0 ? ResultadoCalculoInteres.InteresDiarioMonedaSistema : BigDecimal.ZERO;
								Resultado.MontoInteresAcumuladoAPlazoMonedaSistema		= ResultadoCalculoInteres.InteresAcumuladoMonedaConsulta;
								
								Resultado.MontoAPlazoMonedaInstrumento					= ResultadoCalculoInteres.MontoMonedaConsulta;
								Resultado.MontoInteresAPlazoMonedaInstrumento			= ResultadoCalculoInteres.InteresDiarioMonedaConsulta;
								Resultado.MontoInteresAcumuladoAPlazoMonedaInstrumento	= ResultadoCalculoInteres.InteresAcumuladoMonedaSistema;
								
								Resultado.MontoAPlazoMonedaAdicional					= ResultadoCalculoInteres.MontoMonedaAdicional;
								Resultado.MontoInteresAPlazoMonedaAdicinal				= ResultadoCalculoInteres.InteresDiarioMonedaAdicional;
								Resultado.MontoInteresAcumuladoAPlazoMonedaAdicional	= ResultadoCalculoInteres.InteresAcumuladoMonedaSistema;
								Resultado.MontoMonedaSistema                            = ResultadoCalculoInteres.ValorPresenteMonedaConsulta;
								Resultado.MontoInteresMonedaSistema						= ResultadoCalculoInteres.InteresDiarioMonedaSistema;
								Resultado.MontoInteresMonedaInstrumento					= ResultadoCalculoInteres.InteresDiarioMonedaConsulta;
								Resultado.MontoInteresAcumuladoMonedaSistema			= ResultadoCalculoInteres.InteresAcumuladoMonedaSistema;
								Resultado.FechaValorizacionFinal                        = WS.FechaCalculada;
								Resultado.MontoMonedaSistemaFinal                       = ResultadoCalculoInteres.ValorPresenteMonedaConsulta;
						} 
						else if("N".equals(WS.CalculaIntereses) && (EsInformeSimultaneas) ) {
								Resultado.MontoMonedaSistemaFinal                       = ResultadoCalculoInteres.ValorPresenteMonedaConsulta;
						}
					}
				};
				
				if (ResultadoValorizacionWS != null && WS.TipoValorizacion.equals("WS")){
					
					Resultado.IdValorizacion 	= ResultadoValorizacionWS.IdValorizacion;
					Resultado.FechaValorizacion = ResultadoValorizacionWS.FechaValorizacion;
					Resultado.IdInstrumento 	= ResultadoValorizacionWS.IdInstrumento; 
					Resultado.IdMonedaInstrumento = Resultado.IdMonedaInstrumento = ValorizaATasaPacto && EsInformeCarteraPacto && ResultadoValorizacionWS.IdMonedaInstrumento == null ? WS.IdMonedaInstrumento : ResultadoValorizacionWS.IdMonedaInstrumento;
					Resultado.IdTipoReajuste 	= ResultadoValorizacionWS.IdTipoReajusteInstrumento;					
					Resultado.Cantidad 			= ResultadoValorizacionWS.Cantidad;
					Resultado.ValorRescate 		= ResultadoValorizacionWS.ValorRescate;
					Resultado.Tasa360 			= ResultadoValorizacionWS.Tasa360;
					Resultado.Duration 			= ResultadoValorizacionWS.Duration; 			
					Resultado.Convexidad 		= ResultadoValorizacionWS.Convexidad; 
					

					//+++++++++++++++++++++++++++++++++++++++++++++++++					
					Resultado.MontoInteresMonedaSistema		= BigDecimal.ZERO;
					Resultado.MontoReajusteMonedaSistema	= BigDecimal.ZERO;
					Resultado.MontoInteresMonedaInstrumento	= BigDecimal.ZERO;
					Resultado.MontoReajusteMonedaInstrumento= BigDecimal.ZERO;
					Resultado.MontoInteresMonedaAdicional	= BigDecimal.ZERO;
					Resultado.MontoReajusteMonedaAdicional	= BigDecimal.ZERO;
					//+++++++++++++++++++++++++++++++++++++++++++++++++
					Resultado.MontoCorteCuponMonedaInstrumento			= BigDecimal.ZERO;
					Resultado.MontoCapitalCorteCuponMonedaInstrumento	= BigDecimal.ZERO;
					Resultado.MontoInteresCorteCuponMonedaInstrumento	= BigDecimal.ZERO;
					//+++++++++++++++++++++++++++++++++++++++++++++++++
					Resultado.MayorValorMonedaSistema 		= BigDecimal.ZERO;
					Resultado.MayorValorMonedaInstrumento 	= BigDecimal.ZERO;									
					Resultado.MayorValorMonedaAdicional 	= BigDecimal.ZERO;									
					Resultado.MenorValorMonedaSistema 		= BigDecimal.ZERO;
					Resultado.MenorValorMonedaInstrumento 	= BigDecimal.ZERO;									
					Resultado.MenorValorMonedaAdicional 	= BigDecimal.ZERO;									
					//+++++++++++++++++++++++++++++++++++++++++++++++++					

					
					Resultado.PlazoValorizador 	= ResultadoValorizacionWS.PlazoValorizador; 
					Resultado.ResultadoProceso 	= ResultadoValorizacionWS.ResultadoProceso; 
					//Resultado.ValorTipoReajuste = ResultadoValorizacionWS.ValorTipoReajuste;
					Resultado.ValorPar 			= ResultadoValorizacionWS.valorPar;

					Resultado.MensajeProceso 	= ResultadoValorizacionWS.DescripcionProceso;
					Resultado.Cantidad 			= WS.Tamano > 0L && Resultado.Cantidad != null? Resultado.Cantidad.divide( new BigDecimal(WS.Tamano),20,RoundingMode.HALF_UP)  : Resultado.Cantidad;
					Resultado.ValorTieneCall	= Resultado.ValorTieneCall == null ? "" : Resultado.ValorTieneCall;
					

					if ( WS.DimTiempo.equals("AC") ) {

						Resultado.FechaPrecioTasaInicial 	= WS.FechaPrecioTasa;					 
						Resultado.FuentePrecioTasaInicial 	= WS.FuentePrecioTasa;							
						Resultado.PrecioInicial 			= ResultadoValorizacionWS.Precio;
						Resultado.TasaInicial 				= ResultadoValorizacionWS.Tasa;
						Resultado.PlazoResidualInicial		= WS.PlazoResidual;

						Resultado.MensajeErrorFuenteInicial = WS.MensajeErrorFuente;
						Resultado.MensajeValorizacionInicial = ResultadoValorizacionWS.DescripcionProceso;
						
						Resultado.FactorMonedaInstrumentoInicial = ResultadoValorizacionWS.FactorMonedaInstrumento; 
						Resultado.FechaFactorMonedaInstrumentoInicial = ResultadoValorizacionWS.FechaFactorMonedaInstrumento; 
						Resultado.FactorMonedaAdicionalInicial = ResultadoValorizacionWS.FactorMonedaAdicional; 
						Resultado.FechaFactorMonedaAdicionalInicial = ResultadoValorizacionWS.FechaFactorMonedaAdicional;
						
						// La cartera es de hoy y aun no esta materializada y es Inf.Propia ?? => No se valoriza, mantiene valores
						//if ( InInstrumento.FechaLiquidacion != null && "COMP".equalsIgnoreCase(in.p_IdTipoTasaValorizacion)						
						if ( ((( EsInformeCarteraPropia || EsInformeCoberturaPatrimonial ) 
							&& ValorizaATasaCompra) || EsInformeMayorMenorValor ) && InInstrumento.FechaLiquidacion != null  
							&& Fecha.toDate(in.p_FechaInicial).compareTo(Fecha.toDate(InInstrumento.FechaLiquidacion)) < 0) {
							Resultado.MontoMonedaSistemaInicial		= Resultado.MontoCompra;		
							Resultado.MontoMonedaInstrumentoInicial	= InInstrumento.MontoCompraMonedaInstrumento;						
							Resultado.MontoMonedaAdicionalInicial	= InInstrumento.MontoCompraMonedaAdicional;
							Resultado.ValorTipoReajusteInicial 		= InInstrumento.MontoCompraMonedaInstrumento;
							// Corrige tmb Precio si la valorizacion cambio precio
							Resultado.PrecioInicial = InInstrumento.PrecioCompra;
						} else if ( (EsInformeCarteraPacto && Fecha.toDate(InInstrumento.FechaOperacion).compareTo(Fecha.toDate(in.p_FechaInicial)) > 0) || 
								(EsInformeMayorMenorValor && Fecha.toDate(InInstrumento.FechaOperacion).compareTo(Fecha.toDate(in.p_FechaInicial)) > 0)){
							// Fecha operacion >= F.Inicial >> Pacto no ha comenzado => 0 Mto.Anterior
							Resultado.MontoMonedaSistemaInicial		= BigDecimal.ZERO;		
							Resultado.MontoMonedaInstrumentoInicial	= BigDecimal.ZERO;						
							Resultado.MontoMonedaAdicionalInicial	= BigDecimal.ZERO;
							Resultado.ValorTipoReajusteInicial 		= BigDecimal.ZERO;						
							
						} else {
							Resultado.MontoMonedaSistemaInicial		= ResultadoValorizacionWS.MontoTotalMonedaSistema == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaSistema;		
							Resultado.MontoMonedaInstrumentoInicial	= ResultadoValorizacionWS.MontoTotalMonedaInstrumento == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaInstrumento;						
							Resultado.MontoMonedaAdicionalInicial	= ResultadoValorizacionWS.MontoTotalMonedaAdicional == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaAdicional;
							Resultado.ValorTipoReajusteInicial 		= ResultadoValorizacionWS.ValorTipoReajuste;						
						}
					} else if ("AN".equals(WS.DimTiempo)) {
						Resultado.MontoCompra = ResultadoValorizacionWS.MontoMonedaSistema == null ? InInstrumento.MontoCompra : ResultadoValorizacionWS.MontoMonedaSistema;
					} else {

						if ( Resultado.NemoSVS.compareTo(WS.NemoSVS) != 0 ) {
							// Cambio de nemo en la valorizaci�n prx ??
							Resultado.NemoOriginal			= WS.Nemotecnico;							
						}

						Resultado.FechaPrecioTasaFinal 	= WS.FechaPrecioTasa;					 
						Resultado.FuentePrecioTasaFinal	= WS.FuentePrecioTasa;							
						Resultado.PrecioFinal 			= ResultadoValorizacionWS.Precio;
						Resultado.TasaFinal				= ResultadoValorizacionWS.Tasa;
						Resultado.FechaValorizacionFinal= WS.FechaCalculada;
						Resultado.PlazoResidualFinal	= WS.PlazoResidual;
						
						Resultado.MensajeErrorFuenteFinal = WS.MensajeErrorFuente;
						Resultado.MensajeValorizacionFinal = ResultadoValorizacionWS.DescripcionProceso;
						
						Resultado.FactorMonedaInstrumentoFinal 	= ResultadoValorizacionWS.FactorMonedaInstrumento; 
						Resultado.FechaFactorMonedaInstrumentoFinal = ResultadoValorizacionWS.FechaFactorMonedaInstrumento; 
						Resultado.FactorMonedaAdicionalFinal 	= ResultadoValorizacionWS.FactorMonedaAdicional; 
						Resultado.FechaFactorMonedaAdicionalFinal = ResultadoValorizacionWS.FechaFactorMonedaAdicional;
						Resultado.MontoMonedaSistemaFinal		= ResultadoValorizacionWS.MontoTotalMonedaSistema == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaSistema;		
						Resultado.MontoMonedaInstrumentoFinal	= ResultadoValorizacionWS.MontoTotalMonedaInstrumento == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaInstrumento;						
						Resultado.MontoMonedaAdicionalFinal		= ResultadoValorizacionWS.MontoTotalMonedaAdicional == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaAdicional;
						Resultado.ValorTipoReajusteFinal 		= ResultadoValorizacionWS.ValorTipoReajuste;	
						Resultado.PrecioFinal                   = ResultadoValorizacionWS.Precio;

					}
					
					// Tiene C.Cupon? Corte.Cupon + No es cambio Nemo x LH ???
					if (WS.ValorizaCorteDeCupon.equals("S")) 
						{

						Resultado.IdMonedaPagoCupon = (ResultadoValorizacionWS.IdMonedaInstrumento != null 
								&& ! (ResultadoValorizacionWS.IdMonedaInstrumento.trim().equals("") ) )
								? ResultadoValorizacionWS.IdMonedaInstrumento : MonedaSistema ;
						
						// Solo 1 cupon x Valorizacion
						for ( Valorizar.ObjCupon Cupon : ResultadoValorizacionWS.Cupones ){
							
							if ( Cupon.fecha != null 
								 && Cupon.fecha.compareTo(Fecha.toDate(in.p_FechaInicial)) > 0
								 && Cupon.fecha.compareTo(Fecha.toDate(in.p_FechaFinal)) <= 0 )
								//CuponAUsar = new Valorizar.ObjCupon();
													
							if (Cupon != null && Cupon.numero != null){
								
								// valor de reajuste se debe actualizar segun casuistica de fechacupon - fechaSistema <= 1 
								// en caso de cumplirse usaremos reajuste de paridades de cupon sino
								// usaremos el reajuste que viene desde valorizador
								 
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
								Date fechaSistema = ResultadoValorizacionWS.FechaValorizacion;
						        Date fechacupon=  sdf.parse(Cupon.fecha+"");
								fechaCorteCupon = Cupon.fecha;
						        
						        long startTime = fechaSistema.getTime();
						        long endTime = fechacupon.getTime();
						        long diasDesde = (long) Math.floor(startTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora 
						        long diasHasta = (long) Math.floor(endTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora
						        
						        long diferencia = diasHasta - diasDesde;
						        
								
								CalcularParidad par = new CalcularParidad();
								CalcularParidad.CalcularParidadEntrada parin = new CalcularParidad.CalcularParidadEntrada();
								parin.p_IdTrabajo = (long)Math.random()*10000;
								parin.p_IdEmpresa = in.p_IdEmpresa;
								//Reajustes para cupones a la fecha de vencimiento del instrumento
								 	
								
								CalcularParidad.CalculoEntrada auxmon = new CalcularParidad.CalculoEntrada();
										
								auxmon.IdInterno 		= ResultadoValorizacionWS.IdInstrumento;
								auxmon.IdTrabajo 		= parin.p_IdTrabajo;
								auxmon.IdUso 			= "VALORIZADOR";
								auxmon.Fecha 			= Fecha.toStringDate(Cupon.fecha);
								auxmon.IdMonedaOrigen 	= ResultadoValorizacionWS.IdTipoReajusteInstrumento!=null? ResultadoValorizacionWS.IdTipoReajusteInstrumento:MonedaSistema;
								auxmon.MontoOrigen 		= BigDecimal.ONE;
								auxmon.ValorCambioLocalModificado = BigDecimal.ZERO;
								auxmon.IdMonedaDestino 	= ResultadoValorizacionWS.IdMonedaInstrumento!=null?ResultadoValorizacionWS.IdMonedaInstrumento:MonedaAdicional;
								parin.p_CalculosEntrada.add(auxmon);			
											
							
								CalcularParidad.CalcularParidadSalida parout2 = par.logica(con, parin);
								
						        Resultado.ValorTipoReajusteFinal	= diferencia <= 1 ? parout2.p_out_CalculosSalida.get(0).ValorParametroFinanciero :ResultadoValorizacionWS.ValorTipoReajuste;  
								

								Resultado.NumeroCupon					= (long) Cupon.numero;
								Resultado.FechaCorteCupon				= Cupon.fechaAnterior;
								Resultado.FechaPagoCorteCupon			= Cupon.fecha;
								
								//Interes
								Resultado.MontoInteresesPagoCorteCupon 	= Resultado.ValorTipoReajusteFinal.multiply(Cupon.intereses.multiply( new BigDecimal(100)).multiply(Resultado.Cantidad.divide( new BigDecimal(100),20,RoundingMode.HALF_UP ))).setScale(0, RoundingMode.DOWN);
								//Total
								Resultado.MontoCorteCuponMonedaSistema			= Resultado.ValorTipoReajusteFinal.multiply(((Cupon.intereses.multiply( new BigDecimal(100)).multiply(Resultado.Cantidad.divide( new BigDecimal(100),20,RoundingMode.HALF_UP ))).add(Cupon.amortizacion.multiply(new BigDecimal(100)).multiply(ResultadoValorizacionWS.Cantidad.divide(new BigDecimal(100),20,RoundingMode.HALF_UP)))));
								Resultado.MontoCorteCuponMonedaSistema			= Resultado.MontoCorteCuponMonedaSistema.setScale(0, RoundingMode.DOWN);
								Resultado.MontoCapitalCorteCuponMonedaSistema	= Resultado.ValorTipoReajusteFinal.multiply(Cupon.amortizacion.multiply(new BigDecimal(100)).multiply(ResultadoValorizacionWS.Cantidad.divide(new BigDecimal(100),20,RoundingMode.HALF_UP))).setScale(0, RoundingMode.DOWN);
								Resultado.MontoInteresCorteCuponMonedaSistema	= Resultado.ValorTipoReajusteFinal.multiply(Cupon.intereses.multiply( new BigDecimal(100)).multiply(Resultado.Cantidad.divide( new BigDecimal(100),20,RoundingMode.HALF_UP ))).setScale(0, RoundingMode.DOWN);
								if(!"CLP".equals(ResultadoValorizacionWS.IdMonedaInstrumento)) {
									Resultado.MontoCorteCuponMonedaInstrumento			= Resultado.MontoCorteCuponMonedaSistema != null 	&& Resultado.ValorTipoReajusteFinal != null  ? Resultado.MontoCorteCuponMonedaSistema.divide(ResultadoValorizacionWS.FactorMonedaInstrumento,MathContext.DECIMAL128).setScale(4, RoundingMode.DOWN) : BigDecimal.ZERO;
									Resultado.MontoCapitalCorteCuponMonedaInstrumento	= Resultado.MontoCapitalCorteCuponMonedaSistema != null 	&& Resultado.ValorTipoReajusteFinal != null  ? Resultado.MontoCapitalCorteCuponMonedaSistema.divide(ResultadoValorizacionWS.FactorMonedaInstrumento,MathContext.DECIMAL128).setScale(4, RoundingMode.DOWN) : BigDecimal.ZERO;
									Resultado.MontoInteresCorteCuponMonedaInstrumento	= Resultado.MontoInteresCorteCuponMonedaSistema != null 	&& Resultado.ValorTipoReajusteFinal != null  ? Resultado.MontoInteresCorteCuponMonedaSistema.divide(ResultadoValorizacionWS.FactorMonedaInstrumento,MathContext.DECIMAL128).setScale(4, RoundingMode.DOWN) : BigDecimal.ZERO;
								}
								else {
									Resultado.MontoCorteCuponMonedaInstrumento			= Resultado.MontoCorteCuponMonedaSistema != null 	&& Resultado.ValorTipoReajusteFinal != null  ? Resultado.MontoCorteCuponMonedaSistema : BigDecimal.ZERO;
									Resultado.MontoCapitalCorteCuponMonedaInstrumento	= Resultado.MontoCapitalCorteCuponMonedaSistema != null 	&& Resultado.ValorTipoReajusteFinal != null  ? Resultado.MontoCapitalCorteCuponMonedaSistema : BigDecimal.ZERO;
									Resultado.MontoInteresCorteCuponMonedaInstrumento	= Resultado.MontoInteresCorteCuponMonedaSistema != null 	&& Resultado.ValorTipoReajusteFinal != null  ? Resultado.MontoInteresCorteCuponMonedaSistema : BigDecimal.ZERO;
								}
								
								Resultado.MontoReajusteCorteCuponMonedaInstrumento	= ResultadoValorizacionWS.MontoReajusteMonedaInstrumento;								
								Resultado.MontoReajusteCorteCuponMonedaSistema		= ResultadoValorizacionWS.MontoReajusteMonedaSistema; //Reajuste Periodo del corte
								//++++++++++++++++++++++++++
								Resultado.MontoCorteCuponMonedaAdicional			= Resultado.MontoCorteCuponMonedaSistema != null && ResultadoValorizacionWS.FactorMonedaAdicional != null ? Resultado.MontoCorteCuponMonedaSistema.multiply(ResultadoValorizacionWS.FactorMonedaAdicional).setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
								Resultado.MontoCapitalCorteCuponMonedaAdicional		= Resultado.MontoCapitalCorteCuponMonedaInstrumento != null && ResultadoValorizacionWS.FactorMonedaAdicional != null ? Resultado.MontoCapitalCorteCuponMonedaInstrumento.multiply(ResultadoValorizacionWS.FactorMonedaAdicional) : BigDecimal.ZERO;					
								Resultado.MontoInteresCorteCuponMonedaAdicional		= ResultadoValorizacionWS.MontoInteresMonedaAdicional;
								Resultado.MontoReajusteCorteCuponMonedaAdicional	= ResultadoValorizacionWS.MontoReajusteMonedaAdicional;

							}
						};
					}
					
					// Proximo ?
					if ( WS.DimTiempo.equals("PR") ) {
						
						// Falta definici�n para cuando instr.Vencido y TipoReajuste <> CLP
						// INSTRUMENTO VENCIDO (Cartera de Pacto tiene corregido esto)
						if ( !EsInformeCarteraPacto 
							 &&  Fecha.toDate(InInstrumento.FechaVencimientoInstrumento).compareTo(Fecha.toDate(WS.FechaCalculada)) <= 0 
							 &&  Resultado.MontoMonedaSistemaFinal == null  ) {
							if ( "IF".equalsIgnoreCase(Resultado.IdClasificacionTipoInstrumento ) ) {
								Resultado.MontoMonedaSistemaFinal = ResultadoValorizacionWS.ValorRescate;
								Resultado.MontoMonedaInstrumentoFinal = ResultadoValorizacionWS.ValorRescate;
								Resultado.MontoMonedaAdicionalFinal = ResultadoValorizacionWS.ValorRescate;
							} else {
								Resultado.MontoMonedaSistemaFinal = ResultadoValorizacionWS.Cantidad;
								Resultado.MontoMonedaInstrumentoFinal = ResultadoValorizacionWS.Cantidad;
								Resultado.MontoMonedaAdicionalFinal = ResultadoValorizacionWS.Cantidad;								
							}
						}
						
						if( ResultadoValorizacionWS.ResultadoProceso.equals("INST.VENCIDO") ) {

							// El instrumento vence mañana
							Resultado.MontoInteresMonedaSistema 		= BigDecimal.ZERO;
							Resultado.MontoInteresMonedaInstrumento 	= BigDecimal.ZERO;
							Resultado.MontoInteresMonedaAdicional 	= BigDecimal.ZERO;
							Resultado.MontoReajusteMonedaSistema		= BigDecimal.ZERO;
							Resultado.MontoReajusteMonedaAdicional	= BigDecimal.ZERO;
							Resultado.MontoReajusteMonedaInstrumento	= BigDecimal.ZERO;
							
						} else if ( "S".equals(WS.CalculaIntereses) ) {

							BigDecimal FactorDeltaReajuste;
							if (Resultado.MontoCorteCuponMonedaSistema != null) {
								// Se debe sumar el CC ??
								Resultado.MontoMonedaSistemaFinal = Resultado.MontoMonedaSistemaFinal.add(Resultado.MontoCorteCuponMonedaSistema);
								Resultado.MontoMonedaInstrumentoFinal = Resultado.MontoMonedaInstrumentoFinal.add(Resultado.MontoCorteCuponMonedaInstrumento);
								Resultado.MontoMonedaAdicionalFinal = Resultado.MontoMonedaAdicionalFinal.add(Resultado.MontoCorteCuponMonedaAdicional);
							}
							
							
							if ( EsInformeMayorMenorValor ) {
								
								if ( Resultado.MontoMonedaSistemaFinal.compareTo(Resultado.MontoMonedaSistemaInicial) > 0 ) {
									// Val.Mercado > Val.Compra => Mayor Valor
									Resultado.MayorValorMonedaSistema = Resultado.MontoMonedaSistemaFinal.subtract(Resultado.MontoMonedaSistemaInicial);
									Resultado.MayorValorMonedaInstrumento = Resultado.MontoMonedaInstrumentoFinal.subtract(Resultado.MontoMonedaInstrumentoInicial);									
									Resultado.MayorValorMonedaAdicional = Resultado.MontoMonedaAdicionalFinal.subtract(Resultado.MontoMonedaAdicionalInicial);									
								} else if ( Resultado.MontoMonedaSistemaFinal.compareTo(Resultado.MontoMonedaSistemaInicial) < 0 ) {
									// Val.Mercado < Val.Compra => Menor Valor
									Resultado.MenorValorMonedaSistema = Resultado.MontoMonedaSistemaInicial.subtract(Resultado.MontoMonedaSistemaFinal);
									Resultado.MenorValorMonedaInstrumento = Resultado.MontoMonedaInstrumentoInicial.subtract(Resultado.MontoMonedaInstrumentoFinal);									
									Resultado.MenorValorMonedaAdicional = Resultado.MontoMonedaAdicionalInicial.subtract(Resultado.MontoMonedaAdicionalFinal);									
								}
								

							} else {
								// Cartera Propia y Cartera de Pactos calculan intereses
								Resultado.MontoInteresMonedaSistema 	= Resultado.MontoMonedaSistemaInicial != null && Resultado.MontoMonedaSistemaFinal != null ? Resultado.MontoMonedaSistemaFinal.subtract(Resultado.MontoMonedaSistemaInicial) : BigDecimal.ZERO;
								Resultado.MontoInteresMonedaInstrumento = Resultado.MontoMonedaInstrumentoInicial != null && Resultado.MontoMonedaInstrumentoFinal != null ? Resultado.MontoMonedaInstrumentoFinal.subtract(Resultado.MontoMonedaInstrumentoInicial) : BigDecimal.ZERO;
								Resultado.MontoInteresMonedaAdicional 	= Resultado.MontoMonedaAdicionalInicial != null && Resultado.MontoMonedaAdicionalFinal != null ? Resultado.MontoMonedaAdicionalFinal.subtract(Resultado.MontoMonedaAdicionalInicial) : BigDecimal.ZERO;
								Resultado.MontoInteresAcumuladoMonedaSistema = Resultado.MontoMonedaSistemaFinal != null ? Resultado.MontoMonedaSistemaFinal.subtract(Resultado.MontoCompra) : BigDecimal.ZERO;
	
								// Moneda Sistema 
								if (Resultado.FactorMonedaInstrumentoInicial != null && Resultado.FactorMonedaInstrumentoFinal != null 
										&& Resultado.MontoMonedaSistemaInicial != null && Resultado.MontoMonedaSistemaFinal != null 
										&& Resultado.MontoMonedaSistemaInicial.compareTo(BigDecimal.ZERO) != 0 && Resultado.MontoMonedaSistemaFinal.compareTo(BigDecimal.ZERO) != 0
										&& ((Resultado.FactorMonedaInstrumentoFinal.subtract(Resultado.FactorMonedaInstrumentoInicial)).compareTo(BigDecimal.ZERO) != 0) ){
	
									FactorDeltaReajuste = (Resultado.FactorMonedaInstrumentoFinal.subtract(Resultado.FactorMonedaInstrumentoInicial)).divide(Resultado.FactorMonedaInstrumentoInicial, 20,RoundingMode.HALF_UP); 
									Resultado.MontoReajusteMonedaSistema	= Resultado.MontoMonedaSistemaInicial.multiply( FactorDeltaReajuste ).setScale(4, RoundingMode.HALF_UP);
									Resultado.MontoInteresMonedaSistema = Resultado.MontoInteresMonedaSistema.subtract(Resultado.MontoReajusteMonedaSistema);
								}
								
								// Reajuste Moneda Instrumento  
								if (Resultado.FactorMonedaInstrumentoInicial != null && Resultado.FactorMonedaInstrumentoFinal != null 
										&& Resultado.MontoMonedaInstrumentoInicial != null && Resultado.MontoMonedaInstrumentoFinal != null 
										&& Resultado.MontoMonedaInstrumentoInicial.compareTo(BigDecimal.ZERO) != 0 && Resultado.MontoMonedaInstrumentoFinal.compareTo(BigDecimal.ZERO) != 0
										&& ((Resultado.FactorMonedaInstrumentoFinal.subtract(Resultado.FactorMonedaInstrumentoInicial)).compareTo(BigDecimal.ZERO) != 0) ){
	
									FactorDeltaReajuste = (Resultado.FactorMonedaInstrumentoFinal.subtract(Resultado.FactorMonedaInstrumentoInicial)).divide(Resultado.FactorMonedaInstrumentoInicial, 20,RoundingMode.HALF_UP); 
									Resultado.MontoReajusteMonedaInstrumento = Resultado.MontoMonedaInstrumentoInicial.multiply( FactorDeltaReajuste ).setScale(4, RoundingMode.HALF_UP);
									Resultado.MontoInteresMonedaInstrumento  = Resultado.MontoInteresMonedaInstrumento.subtract(Resultado.MontoReajusteMonedaInstrumento);
								}
								
								// Reajuste Moneda Adicional							
								if (Resultado.FactorMonedaAdicionalInicial != null && Resultado.FactorMonedaAdicionalFinal != null 
										&& Resultado.MontoMonedaAdicionalInicial != null && Resultado.MontoMonedaAdicionalFinal != null 
										&& Resultado.FactorMonedaAdicionalFinal.compareTo(BigDecimal.ONE) == 1 
										&& ((Resultado.FactorMonedaAdicionalFinal.subtract(Resultado.FactorMonedaAdicionalInicial)).compareTo(BigDecimal.ZERO) != 0) ){
									Resultado.MontoReajusteMonedaAdicional	= Resultado.MontoMonedaAdicionalInicial.multiply( Resultado.FactorMonedaAdicionalFinal.subtract(Resultado.FactorMonedaAdicionalInicial).setScale(4, RoundingMode.HALF_UP) );
									Resultado.MontoInteresMonedaAdicional 	= Resultado.MontoInteresMonedaAdicional.subtract(Resultado.MontoReajusteMonedaAdicional);
								}
							}
						
						// Si no calcula interes, es pk cartera aun no esta materializada => debo conservar valores iniciales (C.PROPIA)
						} else if ( ( EsInformeCarteraPropia && InInstrumento.FechaLiquidacion != null 
									  && !(Fecha.toDate(in.p_FechaFinal).compareTo( Fecha.toDate(InInstrumento.FechaLiquidacion)) > 0 ) )
								 ||	( EsInformeCarteraPacto && !(InInstrumento.FechaOperacion.compareTo(in.p_FechaFinal) > 0) ) ) 
						{
							Resultado.MontoMonedaSistemaFinal		= Resultado.MontoCompra;		
							Resultado.MontoMonedaInstrumentoFinal	= InInstrumento.MontoCompraMonedaInstrumento;						
							Resultado.MontoMonedaAdicionalFinal		= InInstrumento.MontoCompraMonedaAdicional;
							Resultado.ValorTipoReajusteFinal 		= BigDecimal.ZERO;
								
						} else {
							Resultado.MontoMonedaSistemaFinal		= BigDecimal.ZERO;		
							Resultado.MontoMonedaInstrumentoFinal	= BigDecimal.ZERO;						
							Resultado.MontoMonedaAdicionalFinal		= BigDecimal.ZERO;
							Resultado.ValorTipoReajusteFinal 		= BigDecimal.ZERO;
						}
					} else if("IF".equalsIgnoreCase(Resultado.IdClasificacionTipoInstrumento)){
							
						long startTime = Resultado.FechaValorizacion.getTime();					
						long endTime = ResultadoValorizacionWS.FechaVencimiento.getTime();
						
					    long diasDesde = (long) Math.floor(startTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora 
					    long diasHasta = (long) Math.floor(endTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora
					    long TEnDias = diasHasta - diasDesde;
					    
					    BigDecimal TEnDiasSobre360 = new BigDecimal(TEnDias).divide(new BigDecimal(360L),20,RoundingMode.HALF_UP);
					    BigDecimal tasaAnualizada = ResultadoValorizacionWS.Tasa != null ? ResultadoValorizacionWS.Tasa.divide(new BigDecimal(12),20,RoundingMode.HALF_UP): BigDecimal.ZERO;
					    
					    //si es pagare nr o pdbc se anualiza la tasa nemotecnico = PAGARE NR
					    BigDecimal Tasa = Resultado.Nemotecnico.equalsIgnoreCase("PAGARE NR") ||  Resultado.Nemotecnico.equalsIgnoreCase("PDBC") ?  tasaAnualizada : Resultado.TasaInicial != null ? Resultado.TasaInicial  : BigDecimal.ZERO ;
					    BigDecimal TasaSobre100 = Tasa.divide(new BigDecimal(100), 20 , RoundingMode.HALF_UP) ;
					    
					    BigDecimal divisorDuration =  TEnDiasSobre360;
					    BigDecimal dividendoDuration =  TasaSobre100.multiply(TEnDiasSobre360).add(BigDecimal.ONE);
					    
					    BigDecimal DosporTasaAlCuadrado = new BigDecimal(2L).multiply((TEnDiasSobre360.pow(2))	); 
					    BigDecimal CuadradoDeUnoMasTasaSobreCienPorTasaSobretrecientossesenta = (BigDecimal.ONE.add(TasaSobre100.multiply(TEnDiasSobre360))).pow(2);
					    
					    Resultado.Duration = divisorDuration.divide(dividendoDuration,20,RoundingMode.HALF_UP);
					    Resultado.Convexidad = DosporTasaAlCuadrado.divide(CuadradoDeUnoMasTasaSobreCienPorTasaSobretrecientossesenta,20,RoundingMode.HALF_UP);
					}
				}
				
				Resultado.IdValorizacion = WS.IdValorizacion; 

			};
			
			out.P_OUT_INSTRUMENTOSVALORIZADOS.add(Resultado);
		};
		logger.debug("Fin actualizacion valorizacion");
		
		
		return out;
	}
	
	public HashMap<Long,Object> calcularInteres(Connection con,ValorizarInformeEntrada in,Map<Long, List<ObjProcesamiento>> hashValorizacionDatosEntrada,HashMap<String,Object> TipoValorizacionPorTipoInstrumento) throws Exception {
		
		CalcularIntereses.CalcularInteresesEntrada CalcularInteres = new CalcularIntereses.CalcularInteresesEntrada();
		for (ObjEntradaInstrumentos InInstrumento : in.p_Instrumentos ) {
			
			if (InInstrumento.OrigenProcesamiento.equalsIgnoreCase("FLUJOCUSTODIA")) {
				continue;
			}
			
			ObjTipoValorizacionPorTipoInstrumento TipoValorizacion = (ObjTipoValorizacionPorTipoInstrumento) TipoValorizacionPorTipoInstrumento.get(InInstrumento.IdTipoInstrumento);											
			
			List<ObjProcesamiento> datosEntrada = hashValorizacionDatosEntrada.get(InInstrumento.IdValorizacion);
			
			if (datosEntrada == null) {
				//en el caso que no este configurada la valorizacion para este tipo de instrumento
				continue;
			}
		
			for (ObjProcesamiento VDE: datosEntrada){
				if ( !VDE.IdValorizacion.equals( InInstrumento.IdValorizacion) ) continue;
				if (TipoValorizacion.CalculaInteresesAPlazo.equalsIgnoreCase("S") && InInstrumento.TasaCompra != null){						
					CalcularIntereses.ObjDatosEntrada RI = new CalcularIntereses.ObjDatosEntrada();
					RI.IdTrabajo			= VDE.IdProcesamiento;
					RI.FechaCalculo			= VDE.FechaCalculada;
					RI.Plazo				= InInstrumento.PlazoEnDiasOperacion;
					RI.PlazoResidual 		= VDE.PlazoResidual != null || VDE.PlazoResidual >0 ? VDE.PlazoResidual : 0L;
					RI.IdMonedaConsulta		= InInstrumento.IdMonedaInstrumento;
					RI.MontoMonedaConsulta	= InInstrumento.MontoCompra;
					RI.Tasa					= InInstrumento.TasaCompra;
					RI.Base					= TipoValorizacion.BaseCalculoDeInteres;
					RI.TipoInteres			= TipoValorizacion.TipoCalculoInteres;
					RI.Cantidad				= InInstrumento.Cantidad;
					RI.PrecioCompra			= InInstrumento.PrecioCompra;
					CalcularInteres.DatosEntrada.add(RI);
				};
			};
		};
		
		CalcularIntereses CI = new CalcularIntereses();		
		CalcularInteres.IdEmpresa = in.p_IdEmpresa;
		HashMap<Long, Object> ResultCalculoInteres = new HashMap<>();
		if (CalcularInteres.DatosEntrada.size() > 0) {
			CalcularIntereses.CalcularInteresesSalida RCI = CI.logica(con, CalcularInteres);
			ResultCalculoInteres = Utils.List2Hash(RCI.ResultadoCalculoInteres,"IdTrabajo");
		}
		
		return ResultCalculoInteres;
		
	}


	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getUAT2Connection()) {
			try {
				System.out.println("COMIENZA");

				// p_OrigenProcesamiento =>
				// "CARTERA.PACTO"
				// "CARTERA.PROPIA"
				// "MAYOR.MENOR.VALOR"
				// "COBERTURA.PATRIMONIAL

				// p_IdTipoTasaValorizacion => 				
				// "COMP"
				// "MERC"
				// "PAC"

				// Llamadas validas : 
				// INFORME CARTERA PROPIA + F.CONS. < F.VAL + <T.COMP|T.MERC> == VAL.PRX (F.VAL) - VAL.ACT (F.CONS) > INT+REAJ			
				// INFORME CARTERA PACTO  + F.CONS. < F.VAL + <T.COMP|T.MERC|T.PAC> == VAL.ACT (F.VAL) - VAL.ANT (F.CONS) > INT+REAJ
				// INFORME MAYOR MENOR VALOR + F.CONS = F.VAL + T.MERC = VAL.ACT T.COMPRA Y VAL.ACT T.MERCADO > MAY.MEN.VALOR 
				// INFORME COBERTURA PATRIMONIAL + F.CONS = F.VAL + <T.COMP|T.MERC> = VAL.ACT  > NADA			
				
				
				ValorizarInformeEntrada entrada = new ValorizarInformeEntrada();
				entrada.p_IdEmpresa = 1L;
				entrada.p_IdUsuario = 1L;
				entrada.p_IdPais = 44L;
				entrada.p_IdFechaHoraSistema = "2025-03-25 10:00";
				entrada.p_FechaInicial = "2025-02-17";
				entrada.p_FechaFinal = "2025-02-17";
				entrada.p_OrigenProcesamiento = "COBERTURA.PATRIMONIAL"; 	//				
				entrada.p_IdTipoTasaValorizacion = "MERC";		// COMP PAC MERC
				entrada.p_IdModulo = "CUS";
				entrada.p_IdMonedaAdicional = "USD";

				
				ObjEntradaInstrumentos Papel = new ObjEntradaInstrumentos();
				Papel.IdValorizacion = 1L;
				Papel.FechaOperacion = "2025-02-27";
				Papel.FechaVencimientoOperacion = "2025-02-27";
				Papel.IdCondicionLiquidacion = "T0";
				Papel.IdTipoOperacion = "COMTER";				
				Papel.FechaEmision = "0001-01-01";
				Papel.PrecioCompra = null;
				Papel.IdPortafolio = 2225L;
				Papel.IdCliente = 4846L;
				Papel.TablaOrigen = "CARTERA";
				Papel.IdUso = null;
				Papel.TipoCartera = null;
				Papel.IdTipoReajusteCabecera = null;				// Reajuste de la cabecera del Pacto				
				
				// Tipo papel BBC
				Papel.IdClasificacionTipoInstrumento = "RF";
				Papel.IdTipoInstrumento = "BBC";
				Papel.IdInstrumento = 262093L;
				Papel.FechaVencimientoInstrumento = "2025-08-10";				
				Papel.Emisor = "BBCONSORC";				
				Papel.Nemotecnico = "BCNOCJ0820";
				Papel.NemoSVS = "BCNOCJ0820";
				Papel.IdMonedaInstrumento = "CLP";
				Papel.IdTipoReajusteInstrumento = "UF";
				Papel.IdMonedaEmisionInstrumento = "CLP";				
				Papel.ValorRescate = null;				
				Papel.Cantidad = new BigDecimal("1000");
				Papel.TasaCompra = new BigDecimal("0.88");			// Tasa del Pacto
				Papel.MontoCompra = new BigDecimal("37830600");		// Monto Inicial del pacto (del papel)
				Papel.MontoCompraMonedaInstrumento = new BigDecimal("37830600");
				entrada.p_Instrumentos.add(Papel);
				

				
				ValorizarInforme  demo 	= new ValorizarInforme();
				ValorizarInformeSalida asdf = demo.logica(con, entrada);
				con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
		    	}		    	
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("TERMINA");
	};
}