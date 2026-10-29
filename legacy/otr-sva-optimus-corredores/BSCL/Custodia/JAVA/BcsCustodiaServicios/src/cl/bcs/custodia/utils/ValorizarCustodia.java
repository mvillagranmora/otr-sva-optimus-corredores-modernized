package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ValorizarCustodia implements JavaCall{	
	

	static final Logger logger = LogManager.getLogger(ValorizarCustodia.class.getName());
	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_Cartera.sql");
	private final String SQL_SelectCartera_Pauta = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_Cartera_Pauta.sql");
	private final String SQL_InsertCartera = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_INS_Cartera.sql");
	private final String SQL_DeleteCartera = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_DEL_Cartera.sql");
	private final String SQL_UpdateTablaCartera = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_UPD_Cartera.sql");
	private final String SQL_UpdateTablaGarantia = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_UPD_Garantia.sql");
	private final String SQL_UpdateTablaSubyacente = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_UPD_Subyacente.sql");
	private final String SQL_UpdateTablaNegativos = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_UPD_Negativos.sql");
	private final String SQL_UpdateTablaTransito = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_UPD_MovimientoCarteraEnTransito.sql");
	private final String SQL_SelectInfoCarteraLogPauta = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_InfoCarteraLogPauta.sql");
	private final String SQL_SelectInfoGarantiaLogPauta = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_InfoGarantiaLogPauta.sql");
	private final String SQL_InsertDetalleLogValorizadorPauta = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_INS_DetalleLogValorizadorPauta.sql");
	
	public static class ObjCartera {
		public Long					IdCartera;
		public String				IdEmisor;
		public String				EGRIdConcepto;
		public String				EGRIdTipoOperacion;
		public BigDecimal			INGCantidad;
		public BigDecimal			INGValorRescate;
        public BigDecimal			INGMontoATasaCompraActual;
        public BigDecimal			INGMontoATasaMercadoActual;
        public java.sql.Date		FechaLiquidacion;
        
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ObjCarteraPauta {
		public Long					IdCartera;
		public String				IdEmisor;
		public String				EGRIdConcepto;
		public String				EGRIdTipoOperacion;
		public BigDecimal			INGCantidad;
		public BigDecimal			INGValorRescate;
        public BigDecimal			INGMontoATasaCompraActual;
        public BigDecimal			INGMontoATasaMercadoActual;
        public java.sql.Date		FechaLiquidacion;
        public Long					FolioCartera;
        public String				Nemotecnico;
        
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ObjInfoCarteraLogPauta {
		public String TablaOrigen;
		public Long IdOrigen;
		public Long FolioOrigen;
		public String Nemotecnico;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
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
		
		//-----------------------------------------------------------
		public String				FechaOperacion;
		public String				FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;				//Solo para operaciones a plazo	O iif
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
		public BigDecimal			ValorRescate;		
		public BigDecimal			MontoCompra;						//en moneda del instrumento ( se usa para el calculo de interes)
		public BigDecimal			MontoCompraMonedaSistema;
		public BigDecimal			MontoMercadoMonedaSistema;
		public BigDecimal			MontoCompraSubyacente;
		public BigDecimal			MontoCompraGarantia;
		
		//-----------------------------------------------------------
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
		
		public String				IdTipoOperacion;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	public static class ObjUpdateTablas {
		public Long					IdValorizacion;
		public Long					TicketProcesamiento;
		public String				TablaOrigen;
		public String				EsRegistroNuevo;
		public String				GenerarNuevoFolioCustodia;
		public String				OrigenProcesamiento; 
		public Long					IdCartera 		= 0L;
		public Long 				IdSubyacente 	= 0L;
		public Long					IdGarantia 		= 0L;
		public Long					IdMovimientoCarteraEnTransito = 0L;
		public BigDecimal			TasaCompra;
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
		public java.sql.Date		FechaFactorMonedaInstrumento;
		public BigDecimal			ValorFactorMonedaInstrumento;
		public java.sql.Date		FechaFactorMonedaAdicional;
		public BigDecimal			ValorFactorMonedaAdicional;
		public String				TIFAfectoBeneficio;
		public BigDecimal			TIFTasa;
		public BigDecimal			MontoCompra;					//solo en el caso de saldo cartera
		public BigDecimal			MontoCompraMonedaInstrumento;	//solo en el caso de saldo cartera
		public BigDecimal			MontoCompraMonedaAdicional;		
		public BigDecimal			MontoAPlazo;
		public BigDecimal			MontoAPlazoMonedaInstrumento;
		public BigDecimal			MontoAPlazoMonedaAdicional;
		public BigDecimal 			MontoGarantiaMonedaAdicional;
		public BigDecimal			MontoSubyacenteMonedaAdicional;		
		////-------------------------------------------------------
		public Long					INGTicketValorizacion;
		public String				INGValorizacionConProblemas;		
		public BigDecimal			INGPrecioPromedioCompra;
		public BigDecimal			INGMontoPrecioPromedioCompra;
		public BigDecimal			INGMontoPrecioPromedioMonedaInstrumento;
		public BigDecimal			INGMontoPrecioPromedioMonedaAdicional;
		public BigDecimal			INGMontoAPlazoMonedaAdicional;
		public java.sql.Date		INGFechaValorizacionAnterior;
		public java.sql.Date		INGFechaValorizacionActual;
		public java.sql.Date		INGFechaValorizacionProximo;
		public java.sql.Date		INGFechaFactorMonedaInstrumentoAnterior;
		public BigDecimal			INGValorFactorMonedaInstrumentoAnterior;
		public java.sql.Date		INGFechaFactorMonedaInstrumentoActual;
		public BigDecimal			INGValorFactorMonedaInstrumentoActual;
		public java.sql.Date		INGFechaFactorMonedaInstrumentoProximo;
		public BigDecimal			INGValorFactorMonedaInstrumentoProximo;
		public java.sql.Date		INGFechaFactorMonedaAdicionalAnterior;
		public BigDecimal			INGValorFactorMonedaAdicionalAnterior;
		public java.sql.Date		INGFechaFactorMonedaAdicionalActual;
		public BigDecimal			INGValorFactorMonedaAdicionalActual;
		public java.sql.Date		INGFechaFactorMonedaAdicionalProximo;
		public BigDecimal			INGValorFactorMonedaAdicionalProximo;
		public BigDecimal			INGMontoATasaCompraAnterior;
		public BigDecimal			INGMontoInteresCompraAnterior;
		public BigDecimal			INGMontoReajusteCompraAnterior;
		public BigDecimal			INGMontoCompraAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoInteresCompraAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoReajusteCompraAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoCompraAnteriorMonedaAdicional;
		public BigDecimal			INGMontoInteresCompraAnteriorMonedaAdicional;
		public BigDecimal			INGMontoReajusteCompraAnteriorMonedaAdicional;
		public BigDecimal			INGDurationCompraAnterior;
		public BigDecimal			INGConvexidadCompraAnterior;
		public BigDecimal			INGMontoCompraAnteriorPibMas;
		public BigDecimal			INGMontoCompraAnteriorPibMasMonedaInstrumento;
		public BigDecimal			INGMontoCompraAnteriorPibMasMonedaAdicional;
		public BigDecimal			INGMontoCompraAnteriorPibMenos;
		public BigDecimal			INGMontoCompraAnteriorPibMenosMonedaInstrumento;
		public BigDecimal			INGMontoCompraAnteriorPibMenosMonedaAdicional;		
		public BigDecimal			INGMontoATasaCompraActual;
		public BigDecimal			INGMontoCompraActualMonedaInstrumento;
		public BigDecimal			INGMontoCompraActualMonedaAdicional;
		public BigDecimal			INGDurationCompraActual;
		public BigDecimal			INGConvexidadCompraActual;		
		public BigDecimal			INGMontoInteresCompraActual;
		public BigDecimal			INGMontoReajusteCompraActual;
		public BigDecimal			INGMontoInteresCompraActualMonedaInstrumento;
		public BigDecimal			INGMontoReajusteCompraActualMonedaInstrumento;
		public BigDecimal			INGMontoInteresCompraActualMonedaAdicional;
		public BigDecimal			INGMontoReajusteCompraActualMonedaAdicional;
		public BigDecimal			INGMontoInteresCorridoCompraActual;
		public BigDecimal			INGMontoReajusteCorridoCompraActual;
		public BigDecimal			INGMontoInteresCorridoCompraActualMonedaInstrumento;
		public BigDecimal			INGMontoReajusteCorridoCompraActualMonedaInstrumento;
		public BigDecimal			INGMontoInteresCorridoCompraActualMonedaAdicional;
		public BigDecimal			INGMontoReajusteCorridoCompraActualMonedaAdicional;
		public BigDecimal			INGMontoCompraActualPibMas;
		public BigDecimal			INGMontoCompraActualPibMasMonedaInstrumento;
		public BigDecimal			INGMontoCompraActualPibMasMonedaAdicional;
		public BigDecimal			INGMontoCompraActualPibMenos;
		public BigDecimal			INGMontoCompraActualPibMenosMonedaInstrumento;
		public BigDecimal			INGMontoCompraActualPibMenosMonedaAdicional;		
		public BigDecimal			INGMontoATasaCompraProximo;
		public BigDecimal			INGMontoInteresCompraProximo;
		public BigDecimal			INGMontoReajusteCompraProximo;
		public BigDecimal			INGMontoCompraProximoMonedaInstrumento;
		public BigDecimal			INGMontoInteresCompraProximoMonedaInstrumento;
		public BigDecimal			INGMontoReajusteCompraProximoMonedaInstrumento;
		public BigDecimal			INGMontoCompraProximoMonedaAdicional;
		public BigDecimal			INGMontoInteresCompraProximoMonedaAdicional;
		public BigDecimal			INGMontoReajusteCompraProximoMonedaAdicional;
		public BigDecimal			INGDurationCompraProximo;
		public BigDecimal			INGConvexidadCompraProximo;		
		public java.sql.Date		INGFechaPrecioTasaMercadoAnterior;
		public String				INGFuenteTasaPrecioMercadoAnterior;
		public BigDecimal			INGPrecioMercadoAnterior;
		public BigDecimal			INGTasaMercadoAnterior;
		public BigDecimal			INGMontoATasaMercadoAnterior;
		public BigDecimal			INGMontoInteresMercadoAnterior;
		public BigDecimal			INGMontoReajusteMercadoAnterior;
		public BigDecimal			INGMontoMercadoAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoInteresMercadoAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoReajusteMercadoAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoMercadoAnteriorMonedaAdicional;
		public BigDecimal			INGMontoInteresMercadoAnteriorMonedaAdicional;
		public BigDecimal			INGMontoReajusteMercadoAnteriorMonedaAdicional;
		public BigDecimal			INGDurationMercadoAnterior;
		public BigDecimal			INGConvexidadMercadoAnterior;
		public BigDecimal			INGMontoMercadoAnteriorPibMas;
		public BigDecimal			INGMontoMercadoAnteriorPibMasMonedaInstrumento;
		public BigDecimal			INGMontoMercadoAnteriorPibMasMonedaAdicional;
		public BigDecimal			INGMontoMercadoAnteriorPibMenos;
		public BigDecimal			INGMontoMercadoAnteriorPibMenosMonedaInstrumento;
		public BigDecimal			INGMontoMercadoAnteriorPibMenosMonedaAdicional;		
		public java.sql.Date		INGFechaPrecioTasaMercadoActual;
		public String				INGFuenteTasaPrecioMercadoActual;
		public BigDecimal			INGPrecioMercadoActual;
		public BigDecimal			INGTasaMercadoActual;
		public BigDecimal			INGMontoATasaMercadoActual;
		public BigDecimal			INGMontoMercadoActualMonedaInstrumento;
		public BigDecimal			INGMontoMercadoActualMonedaAdicional;
		public BigDecimal			INGDurationMercadoActual;
		public BigDecimal			INGConvexidadMercadoActual;
		public BigDecimal			INGMontoInteresCorridoMercadoActual;
		public BigDecimal			INGMontoReajusteCorridoMercadoActual;
		public BigDecimal			INGMontoInteresCorridoMercadoActualMonedaInstrumento;
		public BigDecimal			INGMontoReajusteCorridoMercadoActualMonedaInstrumento;
		public BigDecimal			INGMontoInteresCorridoMercadoActualMonedaAdicional;
		public BigDecimal			INGMontoReajusteCorridoMercadoActualMonedaAdicional;
		public BigDecimal			INGMontoMercadoActualPibMas;
		public BigDecimal			INGMontoMercadoActualPibMasMonedaInstrumento;
		public BigDecimal			INGMontoMercadoActualPibMasMonedaAdicional;
		public BigDecimal			INGMontoMercadoActualPibMenos;
		public BigDecimal			INGMontoMercadoActualPibMenosMonedaInstrumento;
		public BigDecimal			INGMontoMercadoActualPibMenosMonedaAdicional;		
		public java.sql.Date		INGFechaPrecioTasaMercadoProximo;
		public String				INGFuenteTasaPrecioMercadoProximo;
		public BigDecimal			INGPrecioMercadoProximo;
		public BigDecimal			INGTasaMercadoProximo;
		public BigDecimal			INGMontoATasaMercadoProximo;
		public BigDecimal			INGMontoInteresMercadoProximo;
		public BigDecimal			INGMontoReajusteMercadoProximo;
		public BigDecimal			INGMontoMercadoProximoMonedaInstrumento;
		public BigDecimal			INGMontoInteresMercadoProximoMonedaInstrumento;
		public BigDecimal			INGMontoReajusteMercadoProximoMonedaInstrumento;
		public BigDecimal			INGMontoMercadoProximoMonedaAdicional;
		public BigDecimal			INGMontoInteresMercadoProximoMonedaAdicional;
		public BigDecimal			INGMontoReajusteMercadoProximoMonedaAdicional;
		public BigDecimal			INGDurationMercadoProximo;
		public BigDecimal			INGConvexidadMercadoProximo;		
		public BigDecimal			INGTasaCorredora;
		public java.sql.Date		INGFechaTasaCorredora;
		public BigDecimal			INGMontoATasaCorredora;
		public BigDecimal			INGMontoATasaCorredoraMonedaInstrumento;
		public BigDecimal			INGMontoATasaCorredoraMonedaAdicional;		
		public java.sql.Date		INGFechaCorteCupon;
		public Long					INGNumeroCupon;
		public String				INGIdMonedaPagoCupon;
		public BigDecimal			INGMontoCorteCupon;
		public java.sql.Date		INGFechaPagoCorteCupon;
		public BigDecimal			INGInteresesPagoCorteCupon;
		public BigDecimal			INGMontoCapitalCorteCupon;
		public BigDecimal			INGMontoInteresCorteCupon;
		public BigDecimal			INGMontoReajusteCorteCupon;
		public BigDecimal			INGMontoCorteCuponMonedaInstrumento;
		public BigDecimal			INGMontoCapitalCorteCuponMonedaInstrumento;
		public BigDecimal			INGMontoInteresCorteCuponMonedaInstrumento;
		public BigDecimal			INGMontoReajusteCorteCuponMonedaInstrumento;
		public BigDecimal			INGMontoCorteCuponMonedaAdicional;
		public BigDecimal			INGMontoCapitalCorteCuponMonedaAdicional;
		public BigDecimal			INGMontoInteresCorteCuponMonedaAdicional;
		public BigDecimal			INGMontoReajusteCorteCuponMonedaAdicional;		
		public BigDecimal			INGMontoPresenteTIF;
		public BigDecimal			INGMontoInteresTIF;
		public BigDecimal			INGMontoReajusteTIF;
		public BigDecimal			INGMontoInteresAcumTIF;
		public BigDecimal			INGMontoReajusteAcumTIF;
		public BigDecimal			INGMontoPresenteTIFMonedaInstrumento;
		public BigDecimal			INGMontoInteresTIFMonedaInstrumento;
		public BigDecimal			INGMontoReajusteTIFMonedaInstrumento;
		public BigDecimal			INGMontoInteresAcumTIFMonedaInstrumento;
		public BigDecimal			INGMontoReajusteAcumTIFMonedaInstrumento;
		public BigDecimal			INGMontoPresenteTIFMonedaAdicional;
		public BigDecimal			INGMontoInteresTIFMonedaAdicional;
		public BigDecimal			INGMontoReajusteTIFMonedaAdicional;
		public BigDecimal			INGMontoInteresAcumTIFMonedaAdicional;
		public BigDecimal			INGMontoReajusteAcumTIFMonedaAdicional;
		////GARANTIA
		public BigDecimal 			INGMontoATasaGarantiaAnterior;
		public BigDecimal 			INGMontoInteresGarantiaAnterior;
		public BigDecimal 			INGMontoReajusteGarantiaAnterior;
		public BigDecimal 			INGMontoGarantiaAnteriorMonedaInstrumento;
		public BigDecimal 			INGMontoInteresGarantiaAnteriorMonedaInstrumento;
		public BigDecimal 			INGMontoReajusteGarantiaAnteriorMonedaInstrumento;
		public BigDecimal 			INGMontoGarantiaAnteriorMonedaAdicional;
		public BigDecimal 			INGMontoInteresGarantiaAnteriorMonedaAdicional;
		public BigDecimal 			INGMontoReajusteGarantiaAnteriorMonedaAdicional;		
		public BigDecimal 			INGMontoGarantiaAnteriorPibMas;
		public BigDecimal 			INGMontoGarantiaAnteriorPibMasMonedaInstrumento;
		public BigDecimal 			INGMontoGarantiaAnteriorPibMasMonedaAdicional;
		public BigDecimal 			INGMontoGarantiaAnteriorPibMenos;
		public BigDecimal 			INGMontoGarantiaAnteriorPibMenosMonedaInstrumento;
		public BigDecimal 			INGMontoGarantiaAnteriorPibMenosMonedaAdicional;			
		public BigDecimal 			INGDurationGarantiaAnterior;
		public BigDecimal 			INGConvexidadGarantiaAnterior;
		public BigDecimal 			INGMontoATasaGarantiaActual;
		public BigDecimal 			INGMontoGarantiaActualMonedaInstrumento;
		public BigDecimal 			INGMontoGarantiaActualMonedaAdicional;
		public BigDecimal 			INGMontoGarantiaActualPibMas;
		public BigDecimal 			INGMontoGarantiaActualPibMasMonedaInstrumento;
		public BigDecimal 			INGMontoGarantiaActualPibMasMonedaAdicional;
		public BigDecimal 			INGMontoGarantiaActualPibMenos;
		public BigDecimal 			INGMontoGarantiaActualPibMenosMonedaInstrumento;
		public BigDecimal 			INGMontoGarantiaActualPibMenosMonedaAdicional;
		public BigDecimal 			INGDurationGarantiaActual;
		public BigDecimal 			INGConvexidadGarantiaActual;
		public BigDecimal 			INGMontoInteresCorridoGarantiaActual;
		public BigDecimal 			INGMontoReajusteCorridoGarantiaActual;
		public BigDecimal 			INGMontoInteresCorridoGarantiaActualMonedaInstrumento;
		public BigDecimal 			INGMontoReajusteCorridoGarantiaActualMonedaInstrumento;
		public BigDecimal 			INGMontoInteresCorridoGarantiaActualMonedaAdicional;
		public BigDecimal 			INGMontoReajusteCorridoGarantiaActualMonedaAdicional;
		public BigDecimal 			INGMontoATasaGarantiaProximo;
		public BigDecimal 			INGMontoInteresGarantiaProximo;
		public BigDecimal 			INGMontoReajusteGarantiaProximo;
		public BigDecimal 			INGMontoGarantiaProximoMonedaInstrumento;
		public BigDecimal 			INGMontoInteresGarantiaProximoMonedaInstrumento;
		public BigDecimal 			INGMontoReajusteGarantiaProximoMonedaInstrumento;
		public BigDecimal 			INGMontoGarantiaProximoMonedaAdicional;
		public BigDecimal 			INGMontoInteresGarantiaProximoMonedaAdicional;
		public BigDecimal 			INGMontoReajusteGarantiaProximoMonedaAdicional;
		public BigDecimal 			INGDurationGarantiaProximo;
		public BigDecimal 			INGConvexidadGarantiaProximo;
		////SUBYACENTE
		public BigDecimal			INGMontoATasaSubyacenteAnterior;
		public BigDecimal			INGMontoInteresSubyacenteAnterior;
		public BigDecimal			INGMontoReajusteSubyacenteAnterior;
		public BigDecimal			INGMontoSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoInteresSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoReajusteSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal			INGMontoSubyacenteAnteriorMonedaAdicional;
		public BigDecimal			INGMontoInteresSubyacenteAnteriorMonedaAdicional;
		public BigDecimal			INGMontoReajusteSubyacenteAnteriorMonedaAdicional;
		public BigDecimal			INGDurationSubyacenteAnterior;
		public BigDecimal			INGConvexidadSubyacenteAnterior;
		public BigDecimal			INGMontoSubyacenteAnteriorPibMas;
		public BigDecimal			INGMontoSubyacenteAnteriorPibMasMonedaInstrumento;
		public BigDecimal			INGMontoSubyacenteAnteriorPibMasMonedaAdicional;
		public BigDecimal			INGMontoSubyacenteAnteriorPibMenos;
		public BigDecimal			INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento;
		public BigDecimal			INGMontoSubyacenteAnteriorPibMenosMonedaAdicional;
		public BigDecimal			INGMontoATasaSubyacenteActual;
		public BigDecimal			INGMontoSubyacenteActualMonedaInstrumento;
		public BigDecimal			INGMontoSubyacenteActualMonedaAdicional;
		public BigDecimal			INGDurationSubyacenteActual;
		public BigDecimal			INGConvexidadSubyacenteActual;
		public BigDecimal			INGMontoInteresCorridoSubyacenteActual;
		public BigDecimal			INGMontoReajusteCorridoSubyacenteActual;
		public BigDecimal			INGMontoInteresCorridoSubyacenteActualMonedaInstrumento;
		public BigDecimal			INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento;
		public BigDecimal			INGMontoInteresCorridoSubyacenteActualMonedaAdicional;
		public BigDecimal			INGMontoReajusteCorridoSubyacenteActualMonedaAdicional;
		public BigDecimal			INGMontoSubyacenteActualPibMas;
		public BigDecimal			INGMontoSubyacenteActualPibMasMonedaInstrumento;
		public BigDecimal			INGMontoSubyacenteActualPibMasMonedaAdicional;
		public BigDecimal			INGMontoSubyacenteActualPibMenos;
		public BigDecimal			INGMontoSubyacenteActualPibMenosMonedaInstrumento;
		public BigDecimal			INGMontoSubyacenteActualPibMenosMonedaAdicional;
		public BigDecimal			INGMontoATasaSubyacenteProximo;
		public BigDecimal			INGMontoInteresSubyacenteProximo;
		public BigDecimal			INGMontoReajusteSubyacenteProximo;
		public BigDecimal			INGMontoSubyacenteProximoMonedaInstrumento;
		public BigDecimal			INGMontoInteresSubyacenteProximoMonedaInstrumento;
		public BigDecimal			INGMontoReajusteSubyacenteProximoMonedaInstrumento;
		public BigDecimal			INGMontoSubyacenteProximoMonedaAdicional;
		public BigDecimal			INGMontoInteresSubyacenteProximoMonedaAdicional;
		public BigDecimal			INGMontoReajusteSubyacenteProximoMonedaAdicional;
		public BigDecimal			INGDurationSubyacenteProximo;
		public BigDecimal			INGConvexidadSubyacenteProximo;
		////-------------------------------------------------------
		public Long					EGRTicketValorizacion;
		public String				EGRValorizacionConProblemas;
		public java.sql.Date		EGRFechaFactorMonedaInstrumento;
		public BigDecimal			EGRValorFactorMonedaInstrumento;
		public java.sql.Date		EGRFechaFactorMonedaAdicional;
		public BigDecimal			EGRValorFactorMonedaAdicional;
		public BigDecimal			EGRMonto;
		public BigDecimal			EGRMontoMonedaInstrumento;
		public BigDecimal			EGRMontoMonedaAdicional;
		public BigDecimal			EGRSpread;
		public java.sql.Date		EGRFechaPrecioTasaMercado;
		public String				EGRFuenteTasaPrecioMercado;
		public BigDecimal			EGRPrecioMercado;
		public BigDecimal			EGRTasaMercado;
		public BigDecimal			EGRMontoATasaMercado;
		public BigDecimal			EGRMontoMercadoMonedaInstrumento;
		public BigDecimal			EGRMontoMercadoMonedaAdicional;
		public BigDecimal			EGRSpreadMercado;
		public BigDecimal			EGRPrecioAPlazo;
		public BigDecimal			EGRMontoAPlazo;
		public BigDecimal			EGRMontoAPlazoMonedaInstrumento;
		public BigDecimal			EGRMontoAPlazoMonedaAdicional;
		public BigDecimal			EGRMontoPresenteTIF;
		public BigDecimal			EGRMontoPresenteTIFMonedaInstrumento;
		public BigDecimal			EGRMontoPresenteTIFMonedaAdicional;
		////RENTABILIDAD
		public String				EGRTipoFactorRentabilidadNeta;
		public java.sql.Date		EGRFechaFactorRentabilidadNeta;
		public BigDecimal			EGRValorFactorRentabilidadNeta;
		public BigDecimal			EGRMontoRentabilidadNeta;
		public Object ValorPar;
		
		public Long 				IdCarteraNegativa;
				
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	//--------------------------------------------------------------------------------------------------------------------------
	public static class ValorizarCustodiaEntrada {
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;
		public String							p_IdModulo;		
		public Long								p_IdPais = -1L;
		public String                			p_FechaValorizacion = "0001-01-01";
		//----------------------------------------
		public String							p_OrigenProcesamiento = "";		//INGRESO_CUSTODIA - RETIRO_CUSTODIA - VALORIZACION_PAUTA
		public List<ObjDatoEntrada>				p_InstrumentosAValorizar;
		public String							p_EsPauta = "N";
		public Long 							p_IdLogValorizadorPauta = 0L;
	
	    public static class ObjDatoEntrada {
			public String				TablaOrigen;
			public Long					IdCustodia;
			public Long					IdCliente; 
			public Long					IdPortafolio;
			public String				EsRegistroNuevo;
			public String				GenerarNuevoFolioCustodia;
			public String				OrigenProcesamiento;
			//-----------------------------------------------------------
			public java.sql.Date		FechaOperacion;
			public java.sql.Date		FechaVencimientoOperacion;
			public Long					PlazoEnDiasOperacion;				//Solo para operaciones a plazo
			public String				IdCondicionLiquidacion;
			public java.sql.Date		FechaLiquidacion;		
			public String				IdClasificacionTipoInstrumento; 
			public String				IdTipoInstrumento; 
			public Long					IdInstrumento; 
			public String				Nemotecnico; 
			public java.sql.Date		FechaVencimientoInstrumento;
			public java.sql.Date		FechaEmisionInstrumento;
			public String				TieneCorteDeCupon;
			public String				IdMonedaInstrumento;
			public String				IdMonedaEmisionInstrumento;
			public String				IdTipoReajusteInstrumento;
			public BigDecimal			Cantidad; 
			public BigDecimal			ValorRescate; 
			public BigDecimal			MontoCompra;						//en moneda del instrumento ( se usa para el calculo de interes)
			public BigDecimal			MontoCompraMonedaSistema;			//Solo para valorizar egresos, para el calculo de spread (MontoING)
			public BigDecimal			MontoMercadoMonedaSistema;			//Solo para valorizar egresos, para el calculo de spread (MontoING)
			public BigDecimal			MontoCompraSubyacente;				//en moneda del instrumento  (MontoING)
			public BigDecimal			MontoCompraGarantia;				//en moneda del instrumento  (MontoING)
			public BigDecimal           MontoFinalSubyacente;				//Monto final subyacente pacto
			
			//-----------------------------------------------------------
			public BigDecimal			PrecioCompra; 
			public BigDecimal			TasaCompra;			
			public BigDecimal			PrecioSubyacente; 
			public BigDecimal			TasaSubyacente;			
			public BigDecimal			PrecioGarantia; 
			public BigDecimal			TasaGarantia;
			
			public String				IdTipoOperacion;
			
			// MSH
			// -----------------------------------------------------------
			public String				Emisor;
			public String 				IdTipoReajusteCabecera;     // se utiliza para valorizaciones de subyacentes de pactos
					
			public ObjDatoEntrada() {
				this.IdTipoReajusteCabecera = "";
			}

			public String toString() {
				return Utils.printJson(this, true);
			}
		}
	    public ValorizarCustodiaEntrada (){
	    	p_InstrumentosAValorizar 	= new ArrayList<ObjDatoEntrada>();
	    	
		}	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCustodiaSalida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ValorizarCustodiaSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};
	
	public static class ObjDetalleLogValorizadorPauta {
		public Long IdDetalleLogValorizadorPauta;
		public Long IdLogValorizadorPauta;
		public Long FolioCartera;
		public String Nemotecnico;
		public String Estado;
		public String Observacion;
		public String Origen;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjEstadoLogValorizacion {
		public String Estado;
		public String Observacion;
	}
	
	@Trace
	public ValorizarCustodiaSalida logica(Connection con, ValorizarCustodiaEntrada in) throws Exception {
		logger.info("Inicio " + this.getClass().getName());
		ValorizarCustodiaSalida out = new ValorizarCustodiaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		
		try {			
			//00 - VARIABLES DE SISTEMA
			String tFechaHoraSistema	="0001-01-01".equals(in.p_FechaValorizacion)? ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema"):in.p_FechaValorizacion;
			String MonedaSistema		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");		
			String MonedaAdicional		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional");
			
			//01 - SECUENCIAS
			List<Long> SecValorizacionCustodia = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::ValorizacionCustodia", new Long(in.p_InstrumentosAValorizar.size()));
			logger.debug("Fin ObtenerSecuencias");
						
			//02 - PREPARAR INSTRUMENTOS A VALORIZAR
			int indxValorizacion = 0;
			ValorizarInstrumento.ValorizarInstrumentoEntrada VIE 			= new ValorizarInstrumento.ValorizarInstrumentoEntrada();
			List<ObjInstrumentoAValorizar> InstrumentosAValorizar 			= new ArrayList<ObjInstrumentoAValorizar>();
			CalcularParidad.CalcularParidadEntrada CalcParidad 				= new CalcularParidad.CalcularParidadEntrada();
			List<CalcularPrecioPromedio.ObjEntradaEntrada> PPDatosEntrada 	= new ArrayList<CalcularPrecioPromedio.ObjEntradaEntrada>();
			
			logger.debug("Inicio Loop Entradas ValorizarInstrumento");
			for (ValorizarCustodiaEntrada.ObjDatoEntrada  DatoEntrada : in.p_InstrumentosAValorizar){
				if (DatoEntrada.IdInstrumento != null && !DatoEntrada.IdInstrumento.equals(0L)){
					ObjInstrumentoAValorizar instrumento = new ObjInstrumentoAValorizar();
					
					instrumento.TablaOrigen = DatoEntrada.TablaOrigen;
					instrumento.IdCustodia = DatoEntrada.IdCustodia;
					instrumento.IdCliente = DatoEntrada.IdCliente;
					instrumento.IdPortafolio = DatoEntrada.IdPortafolio;
					instrumento.EsRegistroNuevo = DatoEntrada.EsRegistroNuevo;
					instrumento.GenerarNuevoFolioCustodia = DatoEntrada.GenerarNuevoFolioCustodia;
					instrumento.OrigenProcesamiento = DatoEntrada.OrigenProcesamiento;
					instrumento.FechaOperacion = Fecha.toStringDate(DatoEntrada.FechaOperacion);
					instrumento.PlazoEnDiasOperacion = DatoEntrada.	PlazoEnDiasOperacion;
					instrumento.IdClasificacionTipoInstrumento = DatoEntrada.IdClasificacionTipoInstrumento;
					instrumento.IdTipoInstrumento = DatoEntrada.IdTipoInstrumento;
					instrumento.IdInstrumento = DatoEntrada.IdInstrumento;
					instrumento.Nemotecnico = DatoEntrada.Nemotecnico;
					instrumento.TieneCorteDeCupon = DatoEntrada.TieneCorteDeCupon;
					instrumento.IdMonedaInstrumento = DatoEntrada.IdMonedaInstrumento;
					instrumento.IdMonedaEmisionInstrumento = DatoEntrada.IdMonedaEmisionInstrumento;
					instrumento.IdTipoReajusteInstrumento = DatoEntrada.IdTipoReajusteInstrumento;
					instrumento.Cantidad = DatoEntrada. Cantidad;
					instrumento.ValorRescate = DatoEntrada.	ValorRescate;
					if(DatoEntrada.MontoCompraSubyacente != null) {
						instrumento.MontoCompra = DatoEntrada.ValorRescate;
					}else {
						instrumento.MontoCompra = DatoEntrada.MontoCompra;
					}
					
					instrumento.MontoCompraMonedaSistema = DatoEntrada.MontoCompraMonedaSistema;
					instrumento.MontoMercadoMonedaSistema = DatoEntrada.MontoMercadoMonedaSistema;
					instrumento.MontoCompraSubyacente = DatoEntrada.MontoCompraSubyacente;
					instrumento.MontoCompraGarantia = DatoEntrada.MontoCompraGarantia;
					instrumento.PrecioCompra = DatoEntrada. PrecioCompra;
					instrumento.TasaCompra = DatoEntrada.TasaCompra;
					instrumento.PrecioSubyacente = DatoEntrada.PrecioSubyacente;
					instrumento.TasaSubyacente = DatoEntrada.TasaSubyacente;
					instrumento.PrecioGarantia = DatoEntrada.PrecioGarantia;
					instrumento.TasaGarantia = DatoEntrada.TasaGarantia;
					instrumento.Emisor = DatoEntrada.Emisor;
					
					
					
					
					instrumento.IdValorizacion 				= SecValorizacionCustodia.get( indxValorizacion );
					instrumento.FechaValorizacion 			= tFechaHoraSistema;
					instrumento.FechaOperacion				= DatoEntrada.FechaOperacion.toString();
					instrumento.FechaVencimientoOperacion	= DatoEntrada.FechaVencimientoOperacion != null ? DatoEntrada.FechaVencimientoOperacion.toString() : null;
					instrumento.FechaVencimiento			= DatoEntrada.FechaVencimientoInstrumento != null ? DatoEntrada.FechaVencimientoInstrumento.toString() : null;
					instrumento.FechaEmision				= DatoEntrada.FechaEmisionInstrumento != null ? DatoEntrada.FechaEmisionInstrumento.toString() : "0001-01-01";
					instrumento.IdCondicionDeLiquidacion	= DatoEntrada.IdCondicionLiquidacion;
					instrumento.FechaDeLiquidacion			= DatoEntrada.FechaLiquidacion != null ? DatoEntrada.FechaLiquidacion.toString() : "0001-01-01";
					instrumento.OrigenProcesamiento 		= DatoEntrada.OrigenProcesamiento == null ? in.p_OrigenProcesamiento : DatoEntrada.OrigenProcesamiento;
					DatoEntrada.OrigenProcesamiento 		= instrumento.OrigenProcesamiento;
					instrumento.IdTipoOperacion				= DatoEntrada.IdTipoOperacion;
					InstrumentosAValorizar.add(instrumento);
					indxValorizacion ++;
					
					ValorizarInstrumento.ObjEntradaInstrumentos EI = new ValorizarInstrumento.ObjEntradaInstrumentos();
					
					EI.IdValorizacion = instrumento.IdValorizacion;
					EI.OrigenProcesamiento = instrumento.OrigenProcesamiento;
					EI.FechaValorizacion = instrumento.FechaValorizacion;
					EI.FechaOperacion = instrumento.FechaOperacion;						//Solo para operaciones a plazo
					EI.FechaVencimientoOperacion = instrumento.FechaVencimientoOperacion;			//Solo para operaciones a plazo
					EI.PlazoEnDiasOperacion = instrumento.PlazoEnDiasOperacion;				//Solo para operaciones a plazo		
					EI.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento; 
					EI.IdTipoInstrumento = instrumento.IdTipoInstrumento; 
					EI.IdInstrumento = instrumento.IdInstrumento;
					EI.Nemotecnico = instrumento.Nemotecnico;
					EI.FechaVencimiento = instrumento.FechaVencimiento;
					EI.FechaEmision = instrumento.FechaEmision;
					EI.IdMonedaInstrumento = instrumento.IdMonedaInstrumento;
					EI.IdMonedaEmisionInstrumento = instrumento.IdMonedaEmisionInstrumento;
					EI.IdTipoReajusteInstrumento = instrumento.IdTipoReajusteInstrumento;
					EI.Emisor = instrumento.Emisor;								//valorizacionCL
					EI.IdCondicionDeLiquidacion = instrumento.IdCondicionDeLiquidacion;			//valorizacionCL
					EI.FechaDeLiquidacion = instrumento.FechaDeLiquidacion;
					EI.Cantidad = instrumento.Cantidad; 
					EI.ValorRescate = instrumento.ValorRescate; 
					EI.PrecioCompra = instrumento.PrecioCompra; 
					EI.TasaCompra = instrumento.TasaCompra;
					EI.MontoCompra =instrumento.MontoCompra;						//en moneda del instrumento		
					EI.MontoCompraMonedaSistema = instrumento.MontoCompraMonedaSistema;			//en moneda del instrumento
					EI.PrecioSubyacente = instrumento.PrecioSubyacente; 
					EI.TasaSubyacente = instrumento.TasaSubyacente; 
					EI.PrecioGarantia = instrumento.PrecioGarantia; 
					EI.TasaGarantia = instrumento.TasaGarantia; 
					EI.TasaTIF = instrumento.TasaTIF;
					EI.IdPortafolio = instrumento.IdPortafolio;
					EI.TablaOrigen = instrumento.TablaOrigen;							
					EI.IdTipoOperacion = DatoEntrada.IdTipoOperacion;
					EI.IdTipoReajusteCabecera = DatoEntrada.IdTipoReajusteCabecera;
					EI.MontoFinalSubyacente = DatoEntrada.MontoFinalSubyacente;
					
					VIE.p_Instrumentos.add(EI);
					
					//CalculoPrecioPromedio
					if ( !DatoEntrada.IdTipoInstrumento.equals("SIM") ) {
						CalcularPrecioPromedio.ObjEntradaEntrada PrecioPromedio = new CalcularPrecioPromedio.ObjEntradaEntrada();
						
						PrecioPromedio.IdCliente = DatoEntrada.IdCliente;
						PrecioPromedio.IdPortafolio = DatoEntrada.IdPortafolio;
						PrecioPromedio.IdInstrumento = DatoEntrada.IdInstrumento;
						
						PPDatosEntrada.add(PrecioPromedio);
					}
				} else {
					//Garantias en dinero y especies
					if (!DatoEntrada.TablaOrigen.equals("GARANTIA")) continue;
					
					CalcularParidad.CalculoEntrada montoGarantia = new CalcularParidad.CalculoEntrada();
					montoGarantia.IdTrabajo 		= 7899854L;
					montoGarantia.IdInterno 		= DatoEntrada.IdCustodia;
					montoGarantia.Fecha				= DatoEntrada.FechaOperacion.toString();
					montoGarantia.IdMonedaOrigen 	= DatoEntrada.IdMonedaInstrumento;
					montoGarantia.MontoOrigen		= DatoEntrada.MontoCompraGarantia;
					montoGarantia.IdMonedaDestino 	= (MonedaAdicional != null) ? MonedaAdicional : MonedaSistema;
					montoGarantia.IdUso				= "CARTERA";
					CalcParidad.p_CalculosEntrada.add(montoGarantia);
				};
				
			};
			logger.debug("Fin Loop Entradas ValorizarInstrumento");
			ValorizarInstrumento.ValorizarInstrumentoSalida resultValorizacion = new ValorizarInstrumento.ValorizarInstrumentoSalida();
			if (VIE.p_Instrumentos.size() > 0){
				ValorizarInstrumento VI = new ValorizarInstrumento();
				VIE.p_IdEmpresa	= in.p_IdEmpresa;		
				VIE.p_IdUsuario	= in.p_IdUsuario;
				VIE.p_IdModulo	= in.p_IdModulo;
				VIE.p_IdPais	= in.p_IdPais;
				resultValorizacion = VI.logica(con, VIE);
				logger.debug("Salida ValorizarInstrumento");
				
				boolean continuarFlujoConErrorValorizacion = "S".equalsIgnoreCase(in.p_EsPauta) && in.p_IdLogValorizadorPauta != null && !in.p_IdLogValorizadorPauta.equals(0L);

				if (VIE != null && resultValorizacion.P_OUT_ERRORESCONTADOR != null && !resultValorizacion.P_OUT_ERRORESCONTADOR.equals(0L)) {
					out.P_OUT_ERRORESCONTADOR = 1L;

					if (resultValorizacion.P_OUT_MENSAJES != null) {
						out.P_OUT_MENSAJES.addAll(resultValorizacion.P_OUT_MENSAJES);
					}
					if (!continuarFlujoConErrorValorizacion) {
						return out;
					}
				}
				boolean errorValorizacionInstrumento = false;
				if (resultValorizacion.P_OUT_MENSAJES != null && ("S").equals(in.p_EsPauta)) {
					for (PlataformaUtils.MensajeSalida m : resultValorizacion.P_OUT_MENSAJES) {
						if (m != null && m.descripcion != null && m.descripcion.toLowerCase().contains("error")) {
							errorValorizacionInstrumento = true;
							break;
						}
					}
				}
				if (errorValorizacionInstrumento) {

					if (resultValorizacion.P_OUT_MENSAJES != null) {
						out.P_OUT_MENSAJES.addAll(resultValorizacion.P_OUT_MENSAJES);
					}
					if (!continuarFlujoConErrorValorizacion) {
						return out;
					}
				}

			};			
			
			List<ValorizarInstrumento.ObjProcesamiento> ListaValorizacionesIFRF = new ArrayList<ValorizarInstrumento.ObjProcesamiento>();
			
			/*
		    REVISAR A LA VUELTA DE E.MARCHANT LOGICA Y EJECUCION
			ListaValorizacionesIFRF = resultValorizacion.P_OUT_ERRORVALORIZACIONES.stream().filter(objValorizacion -> 
				("IF").equals(objValorizacion.IdClasificacionTipoInstrumento) || 
				("RF").equals(objValorizacion.IdClasificacionTipoInstrumento) 
			).collect(Collectors.toList());
			*/
			
			if(!ListaValorizacionesIFRF.isEmpty()) {
				ValorizaTasaCompra valTasaCompra = new ValorizaTasaCompra();
				ValorizaTasaCompra.EntradaValorizaTasaCompra  entradaValTasaCompra = new ValorizaTasaCompra.EntradaValorizaTasaCompra(); 
				
				entradaValTasaCompra.p_IdEmpresa= in.p_IdEmpresa;
				entradaValTasaCompra.p_IdModulo = in.p_IdModulo;
				entradaValTasaCompra.p_IdUsuario= in.p_IdUsuario;
				entradaValTasaCompra.p_Instrumentos =VIE.p_Instrumentos;
				entradaValTasaCompra.p_listaObjValorizador=ListaValorizacionesIFRF;
				entradaValTasaCompra.p_InstrumentosAValorizar= in.p_InstrumentosAValorizar;
				
				ValorizaTasaCompra.SalidadaValorizaTasaCompra salidaValTasaCompra = valTasaCompra.logica(con, entradaValTasaCompra);
				
				resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS.addAll(salidaValTasaCompra.P_OUT_RESULTADOS);
				
			}
			 
			// obtengo informacion de emisores para poner fecha de cupones a los instrumentos CERO y ZERO
			List<ObjUpdateTablas> idsCartera = new ArrayList<ObjUpdateTablas>();
			HashMap<Long,ValorizarCustodiaEntrada.ObjDatoEntrada> hashDatosEntrada = new HashMap<>();
			for (ValorizarCustodiaEntrada.ObjDatoEntrada  DatoEntrada : in.p_InstrumentosAValorizar){
				if (DatoEntrada.TablaOrigen.equals("CARTERA") && !hashDatosEntrada.containsKey(DatoEntrada.IdCustodia)){
					hashDatosEntrada.put(DatoEntrada.IdCustodia, DatoEntrada);
					ObjUpdateTablas t = new ObjUpdateTablas();
					t.IdCartera = DatoEntrada.IdCustodia;
					idsCartera.add(t);
				}
			}
			
			sql.batch(con, SQL_InsertCartera, idsCartera, ObjUpdateTablas.class);
			List<ObjCartera> CAR = sql.query(con, SQL_SelectCartera, new HashMap<>(), ObjCartera.class);			
			HashMap<Long,Object> hmCartera = Utils.List2Hash(CAR, "IdCartera");
			SQL.exec(con, SQL_DeleteCartera);					

			//03 - ACTUALIZAR CUSTODIA
			List<ObjUpdateTablas> paramsUpdCartera = new ArrayList<ObjUpdateTablas>();
			List<ObjUpdateTablas> paramsUpdGarantia = new ArrayList<ObjUpdateTablas>();
			List<ObjUpdateTablas> paramsUpdSubyacente = new ArrayList<ObjUpdateTablas>();
			List<ObjUpdateTablas> paramsUpdTransito = new ArrayList<ObjUpdateTablas>();
			List<ObjUpdateTablas> paramsUpdNegativas = new ArrayList<ObjUpdateTablas>();
			
			
			HashMap<Long,ArrayList<cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion>> hashResultadoValorizacion = new HashMap<Long,ArrayList<cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion>>();
			for (ObjResultadoValorizacion res : resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS) {
				ArrayList<cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion> ll = hashResultadoValorizacion.get(res.IdValorizacion);
				if (ll == null) {
					ll = new ArrayList<ValorizarInstrumento.ObjResultadoValorizacion>();
					hashResultadoValorizacion.put(res.IdValorizacion,ll);
				}
				
				ll.add(res);
			}
			
			for (ObjInstrumentoAValorizar InstrumentoAValorizar: InstrumentosAValorizar){
				ObjUpdateTablas Tabla = new ObjUpdateTablas();
				ObjCartera InfoCartera = new ObjCartera();
				if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA")){
					InfoCartera = (ObjCartera) hmCartera.get(InstrumentoAValorizar.IdCustodia);
				}
				
				//ACTUALIZACION TITULOS
				//con el hash restringimos la lista solo a las valorizaciones que correspondan a la entrada
				ArrayList<ObjResultadoValorizacion> listaValorizaciones = hashResultadoValorizacion.get(InstrumentoAValorizar.IdValorizacion);
				for (ValorizarInstrumento.ObjResultadoValorizacion ResultadoValorizacion : listaValorizaciones){
					
					if ( !InstrumentoAValorizar.IdValorizacion.equals( ResultadoValorizacion.IdValorizacion) ) {
						continue;
					}
					
					Tabla.IdValorizacion				= ResultadoValorizacion.IdValorizacion;
					Tabla.TicketProcesamiento			= ResultadoValorizacion.TicketProcesamiento;
					Tabla.TablaOrigen					= InstrumentoAValorizar.TablaOrigen;
					Tabla.EsRegistroNuevo				= InstrumentoAValorizar.EsRegistroNuevo;
					Tabla.GenerarNuevoFolioCustodia		= InstrumentoAValorizar.GenerarNuevoFolioCustodia;
					Tabla.OrigenProcesamiento			= InstrumentoAValorizar.OrigenProcesamiento;
					Tabla.IdCartera 					= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("CARTERA") ? InstrumentoAValorizar.IdCustodia : null;
					Tabla.IdCarteraNegativa				= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("NEGATIVA") ? InstrumentoAValorizar.IdCustodia : null;
					Tabla.IdSubyacente 					= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("SUBYACENTE") ? InstrumentoAValorizar.IdCustodia : null;
					Tabla.IdGarantia 					= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("GARANTIA") ? InstrumentoAValorizar.IdCustodia : null;
					Tabla.IdMovimientoCarteraEnTransito	= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("TRANSITO") ? InstrumentoAValorizar.IdCustodia : null;	
					
					//*****************************************************************************************************

					if (Tabla.OrigenProcesamiento.length() < 12) {
						logger.debug("algo raro: origen procesamiento" + in.p_OrigenProcesamiento);
						logger.debug("tabla " + Tabla.toString());
					}
					
					if (Tabla.OrigenProcesamiento.equalsIgnoreCase("INGRESO_CUSTODIA") || (Tabla.OrigenProcesamiento.length() >= 12 && Tabla.OrigenProcesamiento.substring(0,12).equalsIgnoreCase("VALORIZACION")) ){
						if (ResultadoValorizacion.ResultadoProceso == null) continue;							
						Tabla.INGTicketValorizacion 			= ResultadoValorizacion.TicketProcesamiento;
						Tabla.INGValorizacionConProblemas 		= ResultadoValorizacion.ResultadoProceso.equals("OK") ? "N" : "S";
						
						
						Tabla.TasaCompra = InstrumentoAValorizar.TasaCompra;

						
						if ( ResultadoValorizacion.DimensionTiempo.equals("AN") ){
							Tabla.INGFechaValorizacionAnterior 				= ResultadoValorizacion.FechaValorizacion;
							Tabla.INGFechaFactorMonedaInstrumentoAnterior	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
							Tabla.INGValorFactorMonedaInstrumentoAnterior	= ResultadoValorizacion.FactorMonedaInstrumento;
							Tabla.INGFechaFactorMonedaAdicionalAnterior 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
							Tabla.INGValorFactorMonedaAdicionalAnterior 	= ResultadoValorizacion.FactorMonedaAdicional;
							
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								
								Tabla.INGMontoATasaCompraAnterior 						= ResultadoValorizacion.MontoMonedaSistema ;
								Tabla.INGMontoInteresCompraAnterior 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteCompraAnterior 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoCompraAnteriorMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresCompraAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteCompraAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoCompraAnteriorMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresCompraAnteriorMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteCompraAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationCompraAnterior 						= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadCompraAnterior 						= ResultadoValorizacion.Convexidad;								
								Tabla.INGMontoAPlazoMonedaAdicional 					= ResultadoValorizacion.MontoAPlazoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								
								Tabla.INGFechaPrecioTasaMercadoAnterior 		= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGFuenteTasaPrecioMercadoAnterior 		= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.INGPrecioMercadoAnterior 					= ResultadoValorizacion.Precio;
								Tabla.INGTasaMercadoAnterior 					= ResultadoValorizacion.Tasa;
								Tabla.INGMontoATasaMercadoAnterior 				= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresMercadoAnterior 			= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteMercadoAnterior 			= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoMercadoAnteriorMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresMercadoAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteMercadoAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoMercadoAnteriorMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresMercadoAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteMercadoAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationMercadoAnterior 	= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadMercadoAnterior 	= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								Tabla.INGMontoATasaGarantiaAnterior 				= ResultadoValorizacion.MontoMonedaSistema;
								//Tabla.INGMontoInteresGarantiaAnterior 				= ResultadoValorizacion.MontoInteresMonedaSistema;
								//Tabla.INGMontoReajusteGarantiaAnterior 				= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								//Tabla.INGMontoInteresGarantiaAnteriorMonedaInstrumento = ResultadoValorizacion.MontoInteresMonedaInstrumento;
								//Tabla.INGMontoReajusteGarantiaAnteriorMonedaInstrumento = ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoGarantiaAnteriorMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								//Tabla.INGMontoInteresGarantiaAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								//Tabla.INGMontoReajusteGarantiaAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								//Tabla.INGDurationGarantiaAnterior 					= ResultadoValorizacion.Duration;
								//Tabla.INGConvexidadGarantiaAnterior 				= ResultadoValorizacion.Convexidad;									
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){
								Tabla.INGMontoATasaSubyacenteAnterior	 			= ResultadoValorizacion.MontoMonedaSistema;
								//Tabla.INGMontoInteresSubyacenteAnterior 			= ResultadoValorizacion.MontoInteresMonedaSistema;
								//Tabla.INGMontoReajusteSubyacenteAnterior 			= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								//Tabla.INGMontoInteresSubyacenteAnteriorMonedaInstrumento	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								//Tabla.INGMontoReajusteSubyacenteAnteriorMonedaInstrumento	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoSubyacenteAnteriorMonedaAdicional		= ResultadoValorizacion.MontoMonedaAdicional;
								//Tabla.INGMontoInteresSubyacenteAnteriorMonedaAdicional	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								//Tabla.INGMontoReajusteSubyacenteAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								//Tabla.INGDurationSubyacenteAnterior 						= ResultadoValorizacion.Duration;
								//Tabla.INGConvexidadSubyacenteAnterior 					= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+CM")){
								
								Tabla.INGMontoCompraAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraAnteriorPibMasMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraAnteriorPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-CM")){
								
								Tabla.INGMontoCompraAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraAnteriorPibMenosMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraAnteriorPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+MC")){
								
								Tabla.INGMontoMercadoAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoAnteriorPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoAnteriorPibMasMonedaAdicional		= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-MC")){
								
								Tabla.INGMontoMercadoAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoAnteriorPibMenosMonedaInstrumento	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoAnteriorPibMenosMonedaAdicional	= ResultadoValorizacion.MontoMonedaSistema;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+SY")){
								
								Tabla.INGMontoSubyacenteAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorPibMasMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoSubyacenteAnteriorPibMasMonedaAdicional	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-SY")){
								
								Tabla.INGMontoSubyacenteAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorPibMenosMonedaAdicional		= ResultadoValorizacion.MontoMonedaSistema;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+GT")){
								
								Tabla.INGMontoGarantiaAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoGarantiaAnteriorPibMasMonedaAdicional		= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-GT")){
								
								Tabla.INGMontoGarantiaAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorPibMenosMonedaInstrumento	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorPibMenosMonedaAdicional	= ResultadoValorizacion.MontoMonedaSistema;
							};
							
						}
						if ( ResultadoValorizacion.DimensionTiempo.equals("AC") ){
							Tabla.INGFechaValorizacionActual 			= ResultadoValorizacion.FechaValorizacion;
							Tabla.INGFechaFactorMonedaInstrumentoActual = ResultadoValorizacion.FechaFactorMonedaInstrumento;
							Tabla.INGValorFactorMonedaInstrumentoActual = ResultadoValorizacion.FactorMonedaInstrumento;
							Tabla.INGFechaFactorMonedaAdicionalActual 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
							Tabla.INGValorFactorMonedaAdicionalActual 	= ResultadoValorizacion.FactorMonedaAdicional;
							
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S") || (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S"))){
									Tabla.FechaFactorMonedaInstrumento 	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
									Tabla.ValorFactorMonedaInstrumento	= ResultadoValorizacion.FactorMonedaInstrumento;										
									Tabla.FechaFactorMonedaAdicional 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
									Tabla.ValorFactorMonedaAdicional 	= ResultadoValorizacion.FactorMonedaAdicional;
									Tabla.MontoCompraMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
									Tabla.MontoAPlazoMonedaAdicional 	= ResultadoValorizacion.MontoAPlazoMonedaAdicional;
								};
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("N")){
									Tabla.INGMontoATasaCompraActual		= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.INGMontoCompraActualMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								};
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S")){ //Para el caso de titulos que retornan a la cartera con nuevo folio 
									Tabla.MontoCompra					= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.MontoCompraMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;										
									Tabla.MontoAPlazo					= ResultadoValorizacion.MontoAPlazoMonedaSistema;
									Tabla.MontoAPlazoMonedaInstrumento	= ResultadoValorizacion.MontoAPlazoMonedaInstrumento;
								};
								
								Tabla.INGMontoCompraActualMonedaAdicional					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationCompraActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadCompraActual 							= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoCompraActual 					= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoCompraActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoCompraActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoCompraActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaAdicional 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
								
								// MSH
								// ------------------------------------------------------------------------------------------------
								// Tabla.INGMontoInteresCompraProximo 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								// Tabla.INGMontoReajusteCompraProximo 				= ResultadoValorizacion.MontoReajusteMonedaSistema;
								
								// Tabla.INGMontoInteresCompraProximoMonedaInstrumento = ResultadoValorizacion.MontoInteresMonedaInstrumento;
								// Tabla.INGMontoReajusteCompraProximoMonedaInstrumento= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								
								// Tabla.INGMontoInteresCompraProximoMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								// Tabla.INGMontoReajusteCompraProximoMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								// ------------------------------------------------------------------------------------------------								
								
								//MANEJO ESPECIAL PARA LOS CERO Y ZERO YA QUE EL VALORIZADOR ENTREGA LA FECHA DEL CUPON CORRIDO EN UN MES.
								//SE SOLICITA A ROBERTO CERDA LA CORRECCION, PERO SEGURAMENTE DEMORARAN MUCHO TIEMPO, ESO SI ES QUE LO ARREGLAN.
								//JAIME DUARTE SOLICITA QUE HICIERAMOS ESTE PARCHE. 2018-08
								//InfoEmisor.IdEmisor	= InfoEmisor.IdEmisor == null ? "" : InfoEmisor.IdEmisor;
								if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA") || InstrumentoAValorizar.TablaOrigen.equals("GARANTIA") || InstrumentoAValorizar.TablaOrigen.equals("NEGATIVA")){
									if(InfoCartera.IdEmisor != null && (InfoCartera.IdEmisor.equals("CERO") || InfoCartera.IdEmisor.equals("ZERO"))) {
										Tabla.INGFechaCorteCupon		= Fecha.toDate("20"+ ResultadoValorizacion.Nemotecnico.substring(8,10) + "-" + ResultadoValorizacion.Nemotecnico.substring(6,8) +  "-" + ResultadoValorizacion.Nemotecnico.substring(4,6));
										Tabla.INGFechaPagoCorteCupon	= Fecha.toDate("20"+ ResultadoValorizacion.Nemotecnico.substring(8,10) + "-" + ResultadoValorizacion.Nemotecnico.substring(6,8) +  "-" + ResultadoValorizacion.Nemotecnico.substring(4,6));
									}else{
										Tabla.INGFechaCorteCupon 							= ResultadoValorizacion.FechaCorteCupon;
										Tabla.INGFechaPagoCorteCupon 						= ResultadoValorizacion.FechaPagoCorteCupon;
									}
								};

																
								//Tabla.INGFechaPagoCorteCupon 						= ResultadoValorizacion.FechaPagoCorteCupon;
								Tabla.INGInteresesPagoCorteCupon					= ResultadoValorizacion.MontoInteresesPagoCorteCupon;
								Tabla.INGNumeroCupon 								= ResultadoValorizacion.NumeroCupon;
								Tabla.INGIdMonedaPagoCupon 							= ResultadoValorizacion.IdMonedaPagoCupon;
								Tabla.INGMontoCorteCupon							= ResultadoValorizacion.MontoCorteCuponMonedaSistema;									
								Tabla.INGMontoCapitalCorteCupon 					= ResultadoValorizacion.MontoCapitalCorteCuponMonedaSistema;
								Tabla.INGMontoInteresCorteCupon 					= ResultadoValorizacion.MontoInteresCorteCuponMonedaSistema;
								Tabla.INGMontoReajusteCorteCupon 					= ResultadoValorizacion.MontoReajusteCorteCuponMonedaSistema;
								Tabla.INGMontoCorteCuponMonedaInstrumento 			= ResultadoValorizacion.MontoCorteCuponMonedaInstrumento;
								Tabla.INGMontoCapitalCorteCuponMonedaInstrumento 	= ResultadoValorizacion.MontoCapitalCorteCuponMonedaInstrumento;
								Tabla.INGMontoInteresCorteCuponMonedaInstrumento 	= ResultadoValorizacion.MontoInteresCorteCuponMonedaInstrumento;
								Tabla.INGMontoReajusteCorteCuponMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteCorteCuponMonedaInstrumento;
								Tabla.INGMontoCorteCuponMonedaAdicional 			= ResultadoValorizacion.MontoCorteCuponMonedaAdicional;
								Tabla.INGMontoCapitalCorteCuponMonedaAdicional 		= ResultadoValorizacion.MontoCapitalCorteCuponMonedaAdicional;
								Tabla.INGMontoInteresCorteCuponMonedaAdicional 		= ResultadoValorizacion.MontoInteresCorteCuponMonedaAdicional;
								Tabla.INGMontoReajusteCorteCuponMonedaAdicional 	= ResultadoValorizacion.MontoReajusteCorteCuponMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								Tabla.INGFechaPrecioTasaMercadoActual 						= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGFuenteTasaPrecioMercadoActual 						= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.INGPrecioMercadoActual 								= ResultadoValorizacion.Precio;
								Tabla.INGTasaMercadoActual 									= ResultadoValorizacion.Tasa;
								Tabla.INGMontoATasaMercadoActual 							= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoActualMonedaInstrumento 				= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoActualMonedaAdicional 					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationMercadoActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadMercadoActual 							= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoMercadoActual 					= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoMercadoActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaInstrumento = ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
								
								//System.out.println(Tabla.INGMontoATasaMercadoActual);
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S") || (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S"))){
									Tabla.MontoGarantiaMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;										
								};
								
								Tabla.INGMontoATasaGarantiaActual 				= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaActualMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								
																									
								Tabla.INGMontoGarantiaActualMonedaAdicional 					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationGarantiaActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadGarantiaActual 								= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoGarantiaActual 						= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoGarantiaActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoGarantiaActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoGarantiaActualMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoGarantiaActualMonedaAdicional 		= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoGarantiaActualMonedaAdicional 		= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S") || (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S"))){
									Tabla.MontoSubyacenteMonedaAdicional	= ResultadoValorizacion.MontoMonedaAdicional;										
								};
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("N")){
									Tabla.INGMontoATasaSubyacenteActual				= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.INGMontoSubyacenteActualMonedaInstrumento	= ResultadoValorizacion.MontoMonedaInstrumento;
								};
								
								Tabla.INGMontoSubyacenteActualMonedaAdicional					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationSubyacenteActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadSubyacenteActual 							= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoSubyacenteActual 					= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoSubyacenteActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoSubyacenteActualMonedaInstrumento	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoSubyacenteActualMonedaAdicional		= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoSubyacenteActualMonedaAdicional	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
								
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+CM")){
								
								Tabla.INGMontoCompraActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraActualPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-CM")){
								
								Tabla.INGMontoCompraActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraActualPibMenosMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraActualPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+MC")){
								
								Tabla.INGMontoMercadoActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoActualPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-MC")){
								
								Tabla.INGMontoMercadoActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoActualPibMenosMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoActualPibMenosMonedaAdicional 		= ResultadoValorizacion.MontoMonedaAdicional;
							};								
							if (ResultadoValorizacion.DimensionTasa.equals("P+SY")){
								
								Tabla.INGMontoSubyacenteActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteActualPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoSubyacenteActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-SY")){
								
								Tabla.INGMontoSubyacenteActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteActualPibMenosMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoSubyacenteActualPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+GT")){
								
								Tabla.INGMontoGarantiaActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaActualPibMasMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoGarantiaActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-GT")){
								
								Tabla.INGMontoGarantiaActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaActualPibMenosMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoGarantiaActualPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};								
							if (ResultadoValorizacion.DimensionTasa.equals("TIFF")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S")){
									Tabla.TIFAfectoBeneficio 	= "";
									Tabla.TIFTasa 				=  ResultadoValorizacion.Tasa;
								};
								
								Tabla.INGMontoPresenteTIF 				= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresTIF 				= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteTIF 				= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoInteresAcumTIF 			= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteAcumTIF 			= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoPresenteTIFMonedaInstrumento 		= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresTIFMonedaInstrumento 		= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteTIFMonedaInstrumento 		= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoInteresAcumTIFMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteAcumTIFMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoPresenteTIFMonedaAdicional 		= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresTIFMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteTIFMonedaAdicional 		= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGMontoInteresAcumTIFMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteAcumTIFMonedaAdicional 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("CORR")){
								Tabla.INGFechaTasaCorredora 	= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGTasaCorredora 			= ResultadoValorizacion.Tasa;									
								Tabla.INGMontoATasaCorredora 	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoATasaCorredoraMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoATasaCorredoraMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};

						}
						if ( ResultadoValorizacion.DimensionTiempo.equals("PR") ){
							Tabla.INGFechaValorizacionProximo 				= ResultadoValorizacion.FechaValorizacion;
							Tabla.INGFechaFactorMonedaInstrumentoProximo 	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
							Tabla.INGValorFactorMonedaInstrumentoProximo 	= ResultadoValorizacion.FactorMonedaInstrumento;
							Tabla.INGFechaFactorMonedaAdicionalProximo 		= ResultadoValorizacion.FechaFactorMonedaAdicional;
							Tabla.INGValorFactorMonedaAdicionalProximo 		= ResultadoValorizacion.FactorMonedaAdicional;

							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){								
								
								if ( ( ResultadoValorizacion.IdClasificacionTipoInstrumento.equals("IF") || ResultadoValorizacion.IdClasificacionTipoInstrumento.equals("RF") )
										&& ( InfoCartera.FechaLiquidacion != null && InfoCartera.FechaLiquidacion.compareTo(Fecha.toDateTime(tFechaHoraSistema)) > 0 )) {									
									
									Tabla.INGMontoATasaCompraProximo 						= BigDecimal.ZERO;
									Tabla.INGMontoInteresCompraProximo 						= BigDecimal.ZERO;
									Tabla.INGMontoReajusteCompraProximo 					= BigDecimal.ZERO;
									
									Tabla.INGMontoCompraProximoMonedaInstrumento 			= BigDecimal.ZERO;
									Tabla.INGMontoInteresCompraProximoMonedaInstrumento 	= BigDecimal.ZERO;
									Tabla.INGMontoReajusteCompraProximoMonedaInstrumento 	= BigDecimal.ZERO;
									
									Tabla.INGMontoCompraProximoMonedaAdicional 				= BigDecimal.ZERO;
									Tabla.INGMontoInteresCompraProximoMonedaAdicional 		= BigDecimal.ZERO;
									Tabla.INGMontoReajusteCompraProximoMonedaAdicional 		= BigDecimal.ZERO;
								
								}
								else{
									
									Tabla.INGMontoATasaCompraProximo 						= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.INGMontoInteresCompraProximo 						= ResultadoValorizacion.MontoInteresMonedaSistema;
									Tabla.INGMontoReajusteCompraProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
									
									Tabla.INGMontoCompraProximoMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
									Tabla.INGMontoInteresCompraProximoMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
									Tabla.INGMontoReajusteCompraProximoMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
									
									Tabla.INGMontoCompraProximoMonedaAdicional 				= ResultadoValorizacion.MontoMonedaAdicional;
									Tabla.INGMontoInteresCompraProximoMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
									Tabla.INGMontoReajusteCompraProximoMonedaAdicional 		= ResultadoValorizacion.MontoReajusteMonedaAdicional;									
								}
								
								Tabla.INGDurationCompraProximo 							= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadCompraProximo	 					= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								Tabla.INGFechaPrecioTasaMercadoProximo 					= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGFuenteTasaPrecioMercadoProximo 				= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.INGPrecioMercadoProximo 							= ResultadoValorizacion.Precio;
								Tabla.INGTasaMercadoProximo 							= ResultadoValorizacion.Tasa;
								Tabla.INGMontoATasaMercadoProximo 						= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresMercadoProximo 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteMercadoProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoMercadoProximoMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresMercadoProximoMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteMercadoProximoMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoMercadoProximoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresMercadoProximoMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteMercadoProximoMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationMercadoProximo 						= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadMercadoProximo 						= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								Tabla.INGMontoATasaGarantiaProximo 						= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresGarantiaProximo 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteGarantiaProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoGarantiaProximoMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresGarantiaProximoMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteGarantiaProximoMonedaInstrumento	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoGarantiaProximoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresGarantiaProximoMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteGarantiaProximoMonedaAdicional	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationGarantiaProximo 						= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadGarantiaProximo						= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){
								Tabla.INGMontoATasaSubyacenteProximo 						= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresSubyacenteProximo 						= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteSubyacenteProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoSubyacenteProximoMonedaInstrumento			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresSubyacenteProximoMonedaInstrumento		= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteSubyacenteProximoMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoSubyacenteProximoMonedaAdicional 				= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresSubyacenteProximoMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteSubyacenteProximoMonedaAdicional		= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationSubyacenteProximo 							= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadSubyacenteProximo 						= ResultadoValorizacion.Convexidad;
							};
						}

						if ( ResultadoValorizacion.DimensionTiempo.equals("ACUM") ){
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								Tabla.INGMontoInteresCorridoCompraActual					= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaSistema;
								Tabla.INGMontoReajusteCorridoCompraActual					= ResultadoValorizacion.MontoReajusteMonedaSistema;  
								Tabla.INGMontoInteresCorridoCompraActualMonedaInstrumento	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaInstrumento	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoInteresCorridoCompraActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaAdicional	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){								
								Tabla.INGMontoInteresCorridoMercadoActual 					= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaSistema;
								Tabla.INGMontoReajusteCorridoMercadoActual 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaInstrumento = ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
							};
						}
						
					};//////////if (Tabla.OrigenProcesamiento.equalsIgnoreCase("INGRESO_CUSTODIA")){
					
					if (Tabla.OrigenProcesamiento.equalsIgnoreCase("RETIRO_CUSTODIA")){
						if (ResultadoValorizacion.ResultadoProceso == null) continue;
						Tabla.EGRTicketValorizacion 		= ResultadoValorizacion.TicketProcesamiento;
						Tabla.EGRValorizacionConProblemas 	= ResultadoValorizacion.ResultadoProceso.equals("OK") ? "N" : "S";
						
						if ( ResultadoValorizacion.DimensionTiempo.equals("AC") ){
							
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								
								Tabla.EGRFechaFactorMonedaInstrumento 	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
								Tabla.EGRValorFactorMonedaInstrumento 	= ResultadoValorizacion.FactorMonedaInstrumento;
								Tabla.EGRFechaFactorMonedaAdicional 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
								Tabla.EGRValorFactorMonedaAdicional 	= ResultadoValorizacion.FactorMonedaAdicional;
								//Ventas con pacto y a termino el valor del campo EGRMonto es el enviado por la UI 
								if("VENPAC".equalsIgnoreCase(InstrumentoAValorizar.IdTipoOperacion) || "VENTER".equalsIgnoreCase(InstrumentoAValorizar.IdTipoOperacion)) {
									Tabla.EGRMonto = InstrumentoAValorizar.MontoCompra;
								}
								//AL SER GARANTIA, EL MONTO VALORIZADO ES CALCULADO RESPECTO A LOS PRECIOS Y TASAS DE LA GARANTIA CREADA
								//SE REQUIERE MANTENER LOS MONTOS RESPECTO AL PRECIO Y TASA DE COMPRA ORIGINAL DE LA GARANTIA
								else if (CAR.size()>0) {
									if("GAPRFG".equalsIgnoreCase(CAR.get(0).EGRIdConcepto) && "INGGTA".equalsIgnoreCase(CAR.get(0).EGRIdTipoOperacion)) {
										Tabla.EGRMonto					= (CAR.get(0).INGMontoATasaCompraActual.multiply(ResultadoValorizacion.Cantidad)).divide(CAR.get(0).INGCantidad);
									}else {
										Tabla.EGRMonto 					= ResultadoValorizacion.MontoMonedaSistema;
									}
								}else {
									Tabla.EGRMonto 						= ResultadoValorizacion.MontoMonedaSistema;
								}
								//Ventas con pacto y a termino no actualizan el campo EGRMontoMonedaInstrumento
								if("VENPAC".equalsIgnoreCase(InstrumentoAValorizar.IdTipoOperacion) || "VENTER".equalsIgnoreCase(InstrumentoAValorizar.IdTipoOperacion)) {
									Tabla.EGRMontoMonedaInstrumento		= null;
								}else {
									Tabla.EGRMontoMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								}
								
								Tabla.EGRMontoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.EGRPrecioAPlazo					= ResultadoValorizacion.PrecioAPlazo;
								Tabla.EGRMontoAPlazo					= ResultadoValorizacion.MontoAPlazoMonedaSistema;
								Tabla.EGRMontoAPlazoMonedaInstrumento	= ResultadoValorizacion.MontoAPlazoMonedaInstrumento; 									
								Tabla.EGRMontoAPlazoMonedaAdicional 	= ResultadoValorizacion.MontoAPlazoMonedaAdicional;
								/*
				                if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
				                	Tabla.EGRSpread 					= InstrumentoAValorizar.MontoCompraMonedaSistema.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
				                }
				                else {
				                	Tabla.EGRSpread 					= InstrumentoAValorizar.MontoCompraMonedaSistema.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP));
				                }*/
								Tabla.EGRSpread 						= BigDecimal.ZERO;
								if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA")){
									if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
										if (Tabla.EGRMonto != null){
											Tabla.EGRSpread 				= Tabla.EGRMonto.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
										}
					                }
					                else {
										if (Tabla.EGRMonto != null){
											Tabla.EGRSpread 				= Tabla.EGRMonto.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP));
										}
					                }
									// MSH - Para pactos este dato viene en blanco									
									if ( ResultadoValorizacion.IdMonedaInstrumento == null || ResultadoValorizacion.IdMonedaInstrumento.equals("") ) {
										ResultadoValorizacion.IdMonedaInstrumento = ResultadoValorizacion.IdMonedaSistema;
									}
									Tabla.EGRSpread = MonedaFormato.formatear(con, Tabla.EGRSpread, MonedaFormato.MONTO, ResultadoValorizacion.IdMonedaInstrumento).valor;
								}
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								
								Tabla.EGRFechaPrecioTasaMercado 		= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.EGRFuenteTasaPrecioMercado 		= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.EGRPrecioMercado 					= ResultadoValorizacion.Precio;
								Tabla.EGRTasaMercado 					= ResultadoValorizacion.Tasa;
								Tabla.EGRMontoATasaMercado 				= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.EGRMontoMercadoMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.EGRMontoMercadoMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
								//Tabla.EGRSpreadMercado 					= ResultadoValorizacion.MontoMonedaSistema != null ? InstrumentoAValorizar.MontoMercadoMonedaSistema.subtract(ResultadoValorizacion.MontoMonedaSistema) : null;
								/*
								if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
				                	Tabla.EGRSpreadMercado				= InstrumentoAValorizar.MontoMercadoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
				                }
				                else {
				                	Tabla.EGRSpreadMercado				= InstrumentoAValorizar.MontoMercadoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP));
				                }*/
								if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA")){
									if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
										// MSH - Pactos vienen con estos datos en null (cabecera)
										Tabla.EGRSpreadMercado 				= BigDecimal.ZERO;
										if (ResultadoValorizacion.MontoMonedaSistema != null ) {
					                	Tabla.EGRSpreadMercado				= ResultadoValorizacion.MontoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
					                }
					                }
					                else {
					                	Tabla.EGRSpreadMercado				= ResultadoValorizacion.MontoMonedaSistema != null ? ResultadoValorizacion.MontoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP)): InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP);
					                }
									
									// MSH - Para pactos este dato viene en blanco
									if ( ResultadoValorizacion.IdMonedaInstrumento == null || ResultadoValorizacion.IdMonedaInstrumento.equals("") ) {
										ResultadoValorizacion.IdMonedaInstrumento = ResultadoValorizacion.IdMonedaSistema;
									}
									Tabla.EGRSpreadMercado = MonedaFormato.formatear(con, Tabla.EGRSpreadMercado, MonedaFormato.MONTO, ResultadoValorizacion.IdMonedaInstrumento).valor;
								}
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								Tabla.EGRMonto 							= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.EGRMontoMonedaInstrumento 		= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.EGRMontoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){									
							};
							if (ResultadoValorizacion.DimensionTasa.equals("TIFF")){

								Tabla.EGRMontoPresenteTIF 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.EGRMontoPresenteTIFMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.EGRMontoPresenteTIFMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;									
							};
						};					
						
					};//////////if (Tabla.OrigenProcesamiento.equalsIgnoreCase("RETIRO_CUSTODIA")){					
				}; //for (ValorizarInstrumento
				
				
				//TABLAS A ACTUALIZAR
				if (InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("CARTERA") ){
					paramsUpdCartera.add(Tabla);
				};
				if (InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("GARANTIA") ){
					paramsUpdGarantia.add(Tabla);
				};
				if (InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("SUBYACENTE") ){
					paramsUpdSubyacente.add(Tabla);
				};
				if (InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("TRANSITO") ){
					paramsUpdTransito.add(Tabla);
				};
				if (InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("NEGATIVA") ){
					paramsUpdNegativas.add(Tabla);
				};
				
			}; //for (ObjInstrumentoAValorizar
			
			
			//04 - ACTUALIZAR PRECIO PROMEDIO
			if ( PPDatosEntrada.size() > 0 ){
				CalcularPrecioPromedio.CalcularPrecioPromedioEntrada PPEntrada = new CalcularPrecioPromedio.CalcularPrecioPromedioEntrada();
				PPEntrada.IdEmpresa 	= in.p_IdEmpresa;
				PPEntrada.FechaParidad 	= Fecha.toDate(tFechaHoraSistema);
				PPEntrada.p_Entrada 	= PPDatosEntrada;			
				CalcularPrecioPromedio CPP = new CalcularPrecioPromedio();
				CalcularPrecioPromedio.CalcularPrecioPromedioSalida RPP = CPP.logica(con, PPEntrada);
				logger.debug("Fin CalcularPrecioPromedio");
				//Set<Long> carteras = new HashSet<>();
				Map<Long,List<ObjUpdateTablas>> HashCartera = paramsUpdCartera.parallelStream().collect(Collectors.groupingBy(car->car.IdCartera));//Utils.List2Hash( paramsUpdCartera.parallelStream().filter(car -> carteras.add(car.IdCartera)).collect(Collectors.toList()) , "IdCartera");
				
				HashMap<Long,Object> HashGarantia = Utils.List2Hash(paramsUpdGarantia, "IdGarantia");
				HashMap<Long,Object> HashSubyacente = Utils.List2Hash(paramsUpdSubyacente, "IdSubyacente");
				
				
				for (CalcularPrecioPromedio.ObjSalidaSalida resultPP : RPP.p_Salida){
					
					List<ObjUpdateTablas> car = HashCartera.get(resultPP.IdCustodia);
					ObjUpdateTablas gar = (ObjUpdateTablas) HashGarantia.get(resultPP.IdCustodia);
					ObjUpdateTablas sub = (ObjUpdateTablas) HashSubyacente.get(resultPP.IdCustodia);
					
					if (car != null || gar != null || sub != null){
						if (car != null){
							car.parallelStream().forEach(cartera->{
								cartera.INGPrecioPromedioCompra 					= resultPP.PrecioPromedioMonedaInstrumento;
								cartera.INGMontoPrecioPromedioCompra 				= resultPP.MontoPromedioMonedaSistema;
								cartera.INGMontoPrecioPromedioMonedaInstrumento 	= resultPP.MontoPromedioMonedaInstrumento;
								cartera.INGMontoPrecioPromedioMonedaAdicional 		= resultPP.MontoPromedioMonedaAdicional;
							});
							//log.write(car.IdCartera);							
							
						}
						if (gar != null){
							gar.INGPrecioPromedioCompra 					= resultPP.PrecioPromedioMonedaInstrumento;
							gar.INGMontoPrecioPromedioCompra 				= resultPP.MontoPromedioMonedaSistema;
							gar.INGMontoPrecioPromedioMonedaInstrumento 	= resultPP.MontoPromedioMonedaInstrumento;
							gar.INGMontoPrecioPromedioMonedaAdicional 		= resultPP.MontoPromedioMonedaAdicional;
						}
						if (sub != null){
							sub.INGPrecioPromedioCompra 					= resultPP.PrecioPromedioMonedaInstrumento;
							sub.INGMontoPrecioPromedioCompra 				= resultPP.MontoPromedioMonedaSistema;
							sub.INGMontoPrecioPromedioMonedaInstrumento 	= resultPP.MontoPromedioMonedaInstrumento;
							sub.INGMontoPrecioPromedioMonedaAdicional 		= resultPP.MontoPromedioMonedaAdicional;
						}
					} else {
						ObjUpdateTablas Tabla = new ObjUpdateTablas();
						Tabla.IdCartera 								= resultPP.TablaAfectada.equalsIgnoreCase("CARTERA") ? resultPP.IdCustodia : null;						
						Tabla.IdGarantia 								= resultPP.TablaAfectada.equalsIgnoreCase("GARANTIA") ? resultPP.IdCustodia : null;
						Tabla.IdSubyacente 								= resultPP.TablaAfectada.equalsIgnoreCase("SUBYACENTE") ? resultPP.IdCustodia : null;
						Tabla.INGPrecioPromedioCompra 					= resultPP.PrecioPromedioMonedaInstrumento;
						Tabla.INGMontoPrecioPromedioCompra 				= resultPP.MontoPromedioMonedaSistema;
						Tabla.INGMontoPrecioPromedioMonedaInstrumento 	= resultPP.MontoPromedioMonedaInstrumento;
						Tabla.INGMontoPrecioPromedioMonedaAdicional 	= resultPP.MontoPromedioMonedaAdicional;
						
						if (resultPP.TablaAfectada.equalsIgnoreCase("CARTERA") ){
							paramsUpdCartera.add(Tabla);
							List<ObjUpdateTablas> carterasNuevas = Arrays.asList(Tabla);
							HashCartera.put(Tabla.IdCartera, carterasNuevas);
						};
						if (resultPP.TablaAfectada.equalsIgnoreCase("GARANTIA") ){
							paramsUpdGarantia.add(Tabla);
							HashGarantia.put(Tabla.IdCartera, Tabla);
						};
						if (resultPP.TablaAfectada.equalsIgnoreCase("SUBYACENTE") ){
							paramsUpdSubyacente.add(Tabla);
							HashSubyacente.put(Tabla.IdCartera, Tabla);
						};
					};					
				};
				
			};
			
			
			//05 - ACTUALIZAR GARANTIAS EN DINERO Y ESPECIE
			if (CalcParidad.p_CalculosEntrada.size() > 0){
				CalcParidad.p_IdEmpresa 		= in.p_IdEmpresa; 
				CalcParidad.p_IdTrabajo 		= 7899854L;
				
				CalcularParidad CP = new CalcularParidad();
				CalcularParidad.CalcularParidadSalida ResultParidad = CP.logica(con, CalcParidad);
				logger.debug("Fin CalcularParidad Garantias En Dinero");
				HashMap<Long,Object> RCP = Utils.List2Hash(ResultParidad.p_out_CalculosSalida, "IdInterno");
				
				
				for (ValorizarCustodiaEntrada.ObjDatoEntrada  InstrumentoAValorizar : in.p_InstrumentosAValorizar){
					CalcularParidad.CalculoSalida resultadoParidad = (CalcularParidad.CalculoSalida) RCP.get(InstrumentoAValorizar.IdCustodia);					
					if (resultadoParidad == null) {
						out.P_OUT_ERRORESCONTADOR = 1L;						
						continue;
					};
					
					ObjUpdateTablas Tabla = new ObjUpdateTablas();
					
					if (InstrumentoAValorizar.OrigenProcesamiento.equalsIgnoreCase("INGRESO_CUSTODIA") || InstrumentoAValorizar.OrigenProcesamiento.substring(0,12).equalsIgnoreCase("VALORIZACION") ){
						Tabla.INGMontoATasaGarantiaActual			= resultadoParidad.MontoLocal;				
						Tabla.INGMontoGarantiaActualMonedaAdicional = resultadoParidad.MontoDestino;
					};
					if (InstrumentoAValorizar.OrigenProcesamiento.equalsIgnoreCase("RETIRO_CUSTODIA")){
						Tabla.EGRMonto 							= resultadoParidad.MontoLocal;
						Tabla.EGRMontoMonedaInstrumento 		= resultadoParidad.MontoOrigen;
						Tabla.EGRMontoMonedaAdicional 			= resultadoParidad.MontoDestino;
					};
					
					if (InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("GARANTIA") ){
						Tabla.IdGarantia = InstrumentoAValorizar.IdCustodia;
						paramsUpdGarantia.add(Tabla);
					};
				};
			};
			
			
			//06 - ACTUALIZAR RENTABILIDAD
			CalcularRentabilidadNeta CRN = new CalcularRentabilidadNeta();
			CalcularRentabilidadNeta.CalcularRentabilidadNetaEntrada CalcRentabilidad = new CalcularRentabilidadNeta.CalcularRentabilidadNetaEntrada();
			for (ObjUpdateTablas carteraValorizada : paramsUpdCartera){
				
				ObjDatoEntrada DatoEntrada = hashDatosEntrada.get(carteraValorizada.IdCartera);
				if (DatoEntrada == null) continue;
				
				if (!DatoEntrada.OrigenProcesamiento.equalsIgnoreCase("RETIRO_CUSTODIA")) continue;
				
				
				//Para OSAS no aplica calculo de rentabilidad en egresos ya que son opciones en transito de suscripcion y no se renta por ellas
				if (DatoEntrada.IdTipoInstrumento.equalsIgnoreCase("OSA")) continue;
				
				CalcularRentabilidadNeta.ObjEntrada RentabilidadNeta = new CalcularRentabilidadNeta.ObjEntrada(); 
				RentabilidadNeta.IdInterno 		= carteraValorizada.IdCartera;
				RentabilidadNeta.IdCliente		= DatoEntrada.IdCliente;
				RentabilidadNeta.IdPortafolio	= DatoEntrada.IdPortafolio;
				RentabilidadNeta.FechaRetiro	= Fecha.toDate(tFechaHoraSistema);
				//RentabilidadNeta.MontoSaldo		= DatoEntrada.MontoCompra;
				RentabilidadNeta.MontoRetiro	= carteraValorizada.EGRMonto; //Moneda Sistema.
				RentabilidadNeta.Cantidad		= DatoEntrada.Cantidad;
				CalcRentabilidad.p_Entrada.add(RentabilidadNeta);
				
			};
			if (CalcRentabilidad.p_Entrada.size() > 0 ){
				CalcRentabilidad.p_IdEmpresa 	= in.p_IdEmpresa;
//				CalcRentabilidad.p_FechaRetiro	= Fecha.toDate(tFechaHoraSistema);
				CalcularRentabilidadNeta.CalcularRentabilidadNetaSalida resultRentabilidadNeta = CRN.logica(con, CalcRentabilidad);
				logger.debug("Fin CalculoRentabilidad");
				
				if ( resultRentabilidadNeta.p_Salida.size() > 0  && resultRentabilidadNeta.P_OUT_ERRORESCONTADOR.equals(0L)){				
					for (CalcularRentabilidadNeta.ObjSalida resultadoRentabilidadNeta : resultRentabilidadNeta.p_Salida){
						for (ObjUpdateTablas updCartera : paramsUpdCartera){
							if (!updCartera.IdCartera.equals(resultadoRentabilidadNeta.IdInterno)) continue;
							
							updCartera.EGRTipoFactorRentabilidadNeta	= resultadoRentabilidadNeta.TipoFactorRentabilidadNeta;
							updCartera.EGRFechaFactorRentabilidadNeta	= resultadoRentabilidadNeta.FechaFactorRentabilidadNeta;
							updCartera.EGRValorFactorRentabilidadNeta	= resultadoRentabilidadNeta.ValorFactorRentabilidadNeta;
							updCartera.EGRMontoRentabilidadNeta			= resultadoRentabilidadNeta.MontoRentabilidadNetaActualizada;
						};
					};
				};			
			};
			
			List<ObjUpdateTablas> paramsUpdLogPauta = new ArrayList<ObjUpdateTablas>();
			paramsUpdLogPauta.addAll(paramsUpdCartera.stream().filter(p -> p != null && p.IdValorizacion != null).collect(Collectors.toList()));
			paramsUpdLogPauta.addAll(paramsUpdGarantia.stream().filter(p -> p != null && p.IdValorizacion != null).collect(Collectors.toList()));
			
			if ("S".equalsIgnoreCase(in.p_EsPauta) && in.p_IdLogValorizadorPauta != null && !in.p_IdLogValorizadorPauta.equals(0L) && resultValorizacion != null && resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS != null && paramsUpdLogPauta  != null && paramsUpdLogPauta .size() > 0) {
				List<ObjDetalleLogValorizadorPauta> detallesLogPauta = construirDetallesLogValorizadorPauta(con, sql, in.p_IdLogValorizadorPauta, resultValorizacion, paramsUpdLogPauta , InstrumentosAValorizar);
				
				boolean isDebug = false;
				if (isDebug) {
					imprimirDebugDetalleLogPauta(detallesLogPauta);
				}

				insertarDetalleLogValorizadorPauta(con, sql, detallesLogPauta);
				boolean hayErrorDetalle = detallesLogPauta.stream().anyMatch(detalle -> detalle != null	&& "ERROR".equalsIgnoreCase(detalle.Estado));
				if (hayErrorDetalle) {
					out.P_OUT_ERRORESCONTADOR = 1L;
				}
			}
			
			System.out.println("DEBUG paramsUpdCartera: " + paramsUpdCartera.size());
			//07 - ACTUALIZACION DATOS EN CARTERA
			if (paramsUpdCartera.size() > 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,paramsUpdCartera.size() + " carteras valorizadas", paramsUpdCartera.size() + " carteras valorizadas"));
				HashMap<String, String> sqlActualizacion = new HashMap<>();
				HashMap<String, List<ObjUpdateTablas>> carterasAgrupadasPorSql = new HashMap<>();
				//generar updates dependiendo de los parametros que no son nulos.
				paramsUpdCartera.forEach(param -> {
					String query = generateUpdateCarteraSql(param);
					sqlActualizacion.put(query, query);
					List<ObjUpdateTablas> listaCarterasAgrupadas = carterasAgrupadasPorSql.getOrDefault(query, new ArrayList<>());
					listaCarterasAgrupadas.add(param);
					carterasAgrupadasPorSql.put(query, listaCarterasAgrupadas);
				});
				
				for(String key : sqlActualizacion.keySet()) {
					sql.batch(con, SQL_UpdateTablaCartera.replaceAll("<<updates>>", sqlActualizacion.get(key)), carterasAgrupadasPorSql.get(key), ObjUpdateTablas.class);
				}
				
			};
			if (paramsUpdTransito.size() > 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,paramsUpdTransito.size() + " transitos valorizados", paramsUpdTransito.size() + " transitos valorizados"));
				sql.batch(con, SQL_UpdateTablaTransito, paramsUpdTransito, ObjUpdateTablas.class);
			};
			if (paramsUpdGarantia.size() > 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,paramsUpdGarantia.size() + " garantias valorizadas", paramsUpdGarantia.size() + " garantias valorizadas"));
				sql.batch(con, SQL_UpdateTablaGarantia, paramsUpdGarantia, ObjUpdateTablas.class);
			};
			if (paramsUpdSubyacente.size() > 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,paramsUpdSubyacente.size() + " subyacentes valorizadas", paramsUpdSubyacente.size() + " subyacentes valorizadas"));
				sql.batch(con, SQL_UpdateTablaSubyacente, paramsUpdSubyacente, ObjUpdateTablas.class);
			};
			if (paramsUpdNegativas.size() > 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,paramsUpdSubyacente.size() + " carteras negativas valorizadas", paramsUpdSubyacente.size() + " carteras negativas valorizadas"));
				sql.batch(con, SQL_UpdateTablaNegativos, paramsUpdNegativas, ObjUpdateTablas.class);
			};
			Map<Long, Long> cantidadPorCartera = paramsUpdCartera.stream()
				    .filter(p -> p.IdCartera != null)
				    .collect(Collectors.groupingBy(p -> p.IdCartera, Collectors.counting()));

			logger.debug("Fin Actualizacion");
		}
		catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "EXCEPTION: ", e.getMessage()));
			throw e;
		}		
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ValorizarCustodiaEntrada jin=gson.fromJson(in.toString() ,ValorizarCustodiaEntrada.class);
		ValorizarCustodiaSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	private void imprimirDebugDetalleLogPauta(List<ObjDetalleLogValorizadorPauta> detalles) {

		if (detalles == null || detalles.isEmpty()) {
			System.out.println("DEBUG LogValorizadorPauta sin detalles.");
			return;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("\n");
		sb.append("----------------------------------------------------------------------------------------\n");
		sb.append(String.format("%-7s %-10s %-12s %-16s %s%n", "Estado", "Origen", "Folio", "Nemo", "Observacion"));
		sb.append("----------------------------------------------------------------------------------------\n");

		for (ObjDetalleLogValorizadorPauta detalle : detalles) {
			sb.append(String.format(
				"%-7s %-10s %-12s %-16s %s%n",
				detalle.Estado != null ? detalle.Estado : "",
				detalle.Origen != null ? detalle.Origen : "",
				detalle.FolioCartera != null ? detalle.FolioCartera.toString() : "",
				detalle.Nemotecnico != null ? detalle.Nemotecnico : "",
				detalle.Observacion != null ? detalle.Observacion : ""
			));
		}

		System.out.println(sb.toString());
		logger.info("\n" + sb.toString());
		logger.info("DEBUG DetalleLogValorizadorPauta: " + Utils.printJson(detalles, false));
	}

	private String obtenerTablaOrigenDesdeIds(ObjUpdateTablas registro) {

		int idsValidos = 0;
		String tablaInferida = "";

		if (registro.IdCartera != null && registro.IdCartera.longValue() > 0L) {
			idsValidos++;
			tablaInferida = "CARTERA";
		}

		if (registro.IdGarantia != null && registro.IdGarantia.longValue() > 0L) {
			idsValidos++;
			tablaInferida = "GARANTIA";
		}

		return idsValidos == 1 ? tablaInferida : "";
	}

	private String obtenerTablaOrigenLog(ObjUpdateTablas registro, ObjInstrumentoAValorizar instrumento) {

		String tablaRegistro = "";

		if (registro.TablaOrigen != null && !registro.TablaOrigen.trim().isEmpty()) {
			String tabla = registro.TablaOrigen.trim().toUpperCase();

			if ("CARTERA".equalsIgnoreCase(tabla) || "GARANTIA".equalsIgnoreCase(tabla)) {
				tablaRegistro = tabla;
			}
		}

		if (!"".equalsIgnoreCase(tablaRegistro)) {
			return tablaRegistro;
		}

		String tablaDesdeIds = obtenerTablaOrigenDesdeIds(registro);

		if (!"".equals(tablaDesdeIds)) {
			return tablaDesdeIds;
		}

		String tablaInstrumento = "";

		if (instrumento != null && instrumento.TablaOrigen != null && !instrumento.TablaOrigen.trim().isEmpty()) {
			String tabla = instrumento.TablaOrigen.trim().toUpperCase();

			if ("CARTERA".equalsIgnoreCase(tabla) || "GARANTIA".equalsIgnoreCase(tabla)) {
				tablaInstrumento = tabla;
			}
		}

		if (!"".equalsIgnoreCase(tablaInstrumento)) {
			Long idOrigenSegunInstrumento = obtenerIdOrigenLog(registro, tablaInstrumento);
			if (idOrigenSegunInstrumento != null && idOrigenSegunInstrumento.longValue() > 0L && instrumento.IdCustodia != null && instrumento.IdCustodia.equals(idOrigenSegunInstrumento)) {
				return tablaInstrumento;
			}
		}
		
		return "";
	}

	private Long obtenerIdOrigenLog(ObjUpdateTablas registro, String tablaOrigenLog) {

		if ("".equalsIgnoreCase(tablaOrigenLog)) {
			return null;
		}

		if ("CARTERA".equalsIgnoreCase(tablaOrigenLog)) {
			return registro.IdCartera != null && registro.IdCartera.longValue() > 0L ? registro.IdCartera : null;
		}

		if ("GARANTIA".equalsIgnoreCase(tablaOrigenLog)) {
			return registro.IdGarantia != null && registro.IdGarantia.longValue() > 0L ? registro.IdGarantia : null;
		}

		return null;
	}
	
	private List<ObjDetalleLogValorizadorPauta> construirDetallesLogValorizadorPauta(Connection con, SQL sql, Long idLogValorizadorPauta, ValorizarInstrumento.ValorizarInstrumentoSalida resultValorizacion, List<ObjUpdateTablas> paramsUpdLogPauta, List<ObjInstrumentoAValorizar> InstrumentosAValorizar) throws Exception { 
		
		List<ObjDetalleLogValorizadorPauta> detallesLogPauta = new ArrayList<>();	
		
		Map<Long, ObjEstadoLogValorizacion> estadoPorValorizacion = new HashMap<Long, ObjEstadoLogValorizacion>();

		if (resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS != null) {
			for (ObjResultadoValorizacion valorizacion : resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS) {

				if (valorizacion == null || valorizacion.IdValorizacion == null) {
					continue;
				}

				ObjEstadoLogValorizacion estadoLog = estadoPorValorizacion.get(valorizacion.IdValorizacion);

				if (estadoLog == null) {
					estadoLog = new ObjEstadoLogValorizacion();
					estadoLog.Estado = "OK";
					estadoLog.Observacion = "";
					estadoPorValorizacion.put(valorizacion.IdValorizacion, estadoLog);
				}

				if (!"ERROR".equalsIgnoreCase(estadoLog.Estado)) {

					boolean tieneError = (valorizacion.ResultadoProceso != null && !"OK".equalsIgnoreCase(valorizacion.ResultadoProceso)) || valorizacion.MensajeErrorFuente != null;

					if (tieneError) {
						estadoLog.Estado = "ERROR";

						String observacion = valorizacion.MensajeErrorFuente != null  ? valorizacion.MensajeErrorFuente	: valorizacion.MensajeProceso;
						observacion = observacion != null ? observacion.trim() : "";

						if (observacion.length() > 5000) {
							observacion = observacion.substring(0, 5000);
						}

					estadoLog.Observacion = observacion;
					}
				}
			}
		}
		Map<Long, ObjInstrumentoAValorizar> instrumentoPorValorizacion = InstrumentosAValorizar == null	? new HashMap<>() : InstrumentosAValorizar.stream()
																																					.filter(i -> i != null && i.IdValorizacion != null)
																																					.collect(Collectors.toMap(i -> i.IdValorizacion, i -> i, (a, b) -> a));
		List<Long> idsCarteraLog = new ArrayList<>();
		List<Long> idsGarantiaLog = new ArrayList<>();

		for (ObjUpdateTablas registroLog : paramsUpdLogPauta) {

			ObjInstrumentoAValorizar instrumento = instrumentoPorValorizacion.get(registroLog.IdValorizacion);

			String tablaOrigenLog = obtenerTablaOrigenLog(registroLog, instrumento);
			Long idOrigenLog = obtenerIdOrigenLog(registroLog, tablaOrigenLog);

			if ("CARTERA".equalsIgnoreCase(tablaOrigenLog) && idOrigenLog != null) {
				idsCarteraLog.add(idOrigenLog);
			}

			if ("GARANTIA".equalsIgnoreCase(tablaOrigenLog) && idOrigenLog != null) {
				idsGarantiaLog.add(idOrigenLog);
			}
		}

		idsCarteraLog = idsCarteraLog.stream().distinct().collect(Collectors.toList());
		idsGarantiaLog = idsGarantiaLog.stream().distinct().collect(Collectors.toList());

		HashMap<String, Object> hmInfoOrigenLog = new HashMap<>();

		if (!idsCarteraLog.isEmpty()) {

			HashMap<String, Object> paramsCarteraLog = new HashMap<String, Object>();
			paramsCarteraLog.put("IdsCartera", idsCarteraLog);

			List<ObjInfoCarteraLogPauta> infoCarterasLog = sql.query(con, SQL_SelectInfoCarteraLogPauta, paramsCarteraLog, ObjInfoCarteraLogPauta.class);

			for (ObjInfoCarteraLogPauta infoOrigenLog : infoCarterasLog) {

				String tablaOrigenInfo = infoOrigenLog.TablaOrigen != null ? infoOrigenLog.TablaOrigen.trim().toUpperCase() : "";
				String claveOrigenLog = "";

				if (!"".equalsIgnoreCase(tablaOrigenInfo) && infoOrigenLog.IdOrigen != null && infoOrigenLog.IdOrigen.longValue() > 0L) {
					claveOrigenLog = tablaOrigenInfo + "_" + infoOrigenLog.IdOrigen;
				}

				if (!"".equalsIgnoreCase(claveOrigenLog)) {
					hmInfoOrigenLog.put(claveOrigenLog, infoOrigenLog);
				}
			}
		}

		if (!idsGarantiaLog.isEmpty()) {

			HashMap<String, Object> paramsGarantiaLog = new HashMap<String, Object>();
			paramsGarantiaLog.put("IdsGarantia", idsGarantiaLog);

			List<ObjInfoCarteraLogPauta> infoGarantiasLog = sql.query(con, SQL_SelectInfoGarantiaLogPauta, paramsGarantiaLog, ObjInfoCarteraLogPauta.class);

			for (ObjInfoCarteraLogPauta infoOrigenLog : infoGarantiasLog) {

				String tablaOrigenInfo = infoOrigenLog.TablaOrigen != null ? infoOrigenLog.TablaOrigen.trim().toUpperCase() : "";
				String claveOrigenLog = "";

				if (!"".equalsIgnoreCase(tablaOrigenInfo) && infoOrigenLog.IdOrigen != null && infoOrigenLog.IdOrigen.longValue() > 0L) {
					claveOrigenLog = tablaOrigenInfo + "_" + infoOrigenLog.IdOrigen;
				}

				if (!"".equalsIgnoreCase(claveOrigenLog)) {
					hmInfoOrigenLog.put(claveOrigenLog, infoOrigenLog);
				}
			}
		}
		
		for (ObjUpdateTablas registroLog : paramsUpdLogPauta) {

			ObjInstrumentoAValorizar instrumento = instrumentoPorValorizacion.get(registroLog.IdValorizacion);
			String tablaOrigenLog = obtenerTablaOrigenLog(registroLog, instrumento);
			Long idOrigenLog = obtenerIdOrigenLog(registroLog, tablaOrigenLog);
			String tablaOrigenClave = tablaOrigenLog != null ? tablaOrigenLog.trim().toUpperCase() : "";
			String claveOrigenLog = "";
			if (!"".equalsIgnoreCase(tablaOrigenClave) && idOrigenLog != null && idOrigenLog.longValue() > 0L) {
				claveOrigenLog = tablaOrigenClave + "_" + idOrigenLog;
			}
			ObjInfoCarteraLogPauta infoLog = (ObjInfoCarteraLogPauta) hmInfoOrigenLog.get(claveOrigenLog);
			ObjEstadoLogValorizacion estadoLog = estadoPorValorizacion.get(registroLog.IdValorizacion);
			ObjDetalleLogValorizadorPauta detalle = construirDetalleLogValorizadorPauta(idLogValorizadorPauta, registroLog, tablaOrigenLog, idOrigenLog, infoLog, instrumento, estadoLog);
			detallesLogPauta.add(detalle);
		}

		return detallesLogPauta;
	}
	
	private ObjDetalleLogValorizadorPauta construirDetalleLogValorizadorPauta(Long idLogValorizadorPauta, ObjUpdateTablas registroLog, String tablaOrigenLog, Long idOrigenLog, ObjInfoCarteraLogPauta infoLog, ObjInstrumentoAValorizar instrumento, ObjEstadoLogValorizacion estadoLog) {

		ObjDetalleLogValorizadorPauta detalle = new ObjDetalleLogValorizadorPauta();

		detalle.IdDetalleLogValorizadorPauta = null;
		detalle.IdLogValorizadorPauta = idLogValorizadorPauta;
		detalle.Origen = tablaOrigenLog;
		detalle.FolioCartera = infoLog != null && infoLog.FolioOrigen != null ? infoLog.FolioOrigen	: idOrigenLog;
		detalle.Nemotecnico = infoLog != null && infoLog.Nemotecnico != null ? infoLog.Nemotecnico : instrumento != null && instrumento.Nemotecnico != null	? instrumento.Nemotecnico : "";
		detalle.Estado = "OK";
		detalle.Observacion = "";

		if (infoLog == null) {
			detalle.Estado = "ERROR";

			String observacion = "No se encontro informacion de " + tablaOrigenLog + " para IdOrigen " + idOrigenLog;

			if (observacion.length() > 5000) {
				observacion = observacion.substring(0, 5000);
			}

			detalle.Observacion = observacion;
			return detalle;
		}

		if (estadoLog == null) {
			detalle.Estado = "ERROR";
			detalle.Observacion = "Sin resultado de valorizacion asociado";
			return detalle;
		}

		detalle.Estado = estadoLog.Estado;
		detalle.Observacion = estadoLog.Observacion;

		return detalle;
	}
	
	private void insertarDetalleLogValorizadorPauta(Connection con, SQL sql, List<ObjDetalleLogValorizadorPauta> detallesLogPauta) throws Exception {

		if (detallesLogPauta == null || detallesLogPauta.isEmpty()) {
			return;
		}

		List<Long> secDetalleLog = Utils.getSecuencia(con,"BSCL.Plataforma.BD.Secuencias::IdDetalleLogValorizadorPauta", Long.valueOf(detallesLogPauta.size()));

		for (int i = 0; i < detallesLogPauta.size(); i++) {
			detallesLogPauta.get(i).IdDetalleLogValorizadorPauta = secDetalleLog.get(i);
		}

		sql.batch( con, SQL_InsertDetalleLogValorizadorPauta, detallesLogPauta,	ObjDetalleLogValorizadorPauta.class);
	}
	
	public static void main(String[] args) throws Exception {		
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa02","connections.properties")){
			try {
				Gson gson = Utils.getGsonBuilder();
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":260,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_OrigenProcesamiento\":\"\",\"p_InstrumentosAValorizar\":[{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75600,\"IdCliente\":30772,\"IdPortafolio\":43888,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"2022-01-18 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-07-31 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BR\",\"IdInstrumento\":69825,\"Nemotecnico\":\"AE\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"0001-01-01 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"Cantidad\":228948.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":22301364.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":0E-8,\"TasaCompra\":5.00000000,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75601,\"IdCliente\":30772,\"IdPortafolio\":43888,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"2022-01-18 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-07-31 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BR\",\"IdInstrumento\":69825,\"Nemotecnico\":\"AE\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"0001-01-01 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"Cantidad\":228948.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":22301364.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":0E-8,\"TasaCompra\":5.00000000,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75602,\"IdCliente\":22205,\"IdPortafolio\":42609,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T3\",\"FechaLiquidacion\":\"2018-08-03 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACX\",\"IdInstrumento\":1337,\"Nemotecnico\":\"JPM\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"2012-05-22 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":1441.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":151305.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":105.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983260,\"IdCliente\":22205,\"IdPortafolio\":42609,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T3\",\"FechaLiquidacion\":\"2018-08-03 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACX\",\"IdInstrumento\":1337,\"Nemotecnico\":\"JPM\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"2012-05-22 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":1441.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":151305.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":105.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75603,\"IdCliente\":30772,\"IdPortafolio\":43888,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T3\",\"FechaLiquidacion\":\"2018-08-03 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACX\",\"IdInstrumento\":2,\"Nemotecnico\":\"AAPL\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"2012-05-22 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":5.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":955.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":191.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75604,\"IdCliente\":22205,\"IdPortafolio\":42609,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T3\",\"FechaLiquidacion\":\"2018-08-03 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACX\",\"IdInstrumento\":1337,\"Nemotecnico\":\"JPM\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"2012-05-22 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":1441.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":151305.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":105.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983261,\"IdCliente\":22205,\"IdPortafolio\":42609,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T3\",\"FechaLiquidacion\":\"2018-08-03 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACX\",\"IdInstrumento\":1337,\"Nemotecnico\":\"JPM\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"2012-05-22 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":1441.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":151305.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":105.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75605,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":440,\"Nemotecnico\":\"COLBUN\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1986-10-14 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":43193.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":6047020.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":140.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75606,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":440,\"Nemotecnico\":\"COLBUN\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1986-10-14 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":449000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":62815100.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":139.90000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75607,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":39379.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":23895965.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75608,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75609,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75610,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75611,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75612,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75613,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":440,\"Nemotecnico\":\"COLBUN\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1986-10-14 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":1.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":140.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":139.90000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75614,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983262,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":440,\"Nemotecnico\":\"COLBUN\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1986-10-14 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":43193.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":6047020.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":140.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983263,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":440,\"Nemotecnico\":\"COLBUN\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1986-10-14 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":449000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":62815100.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":139.90000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983264,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":39379.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":23895965.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983265,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983266,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983267,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983268,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983269,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983270,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":440,\"Nemotecnico\":\"COLBUN\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1986-10-14 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":1.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":140.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":139.90000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"CARTERA\",\"IdCustodia\":1983271,\"IdCliente\":35699,\"IdPortafolio\":47422,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T1\",\"FechaLiquidacion\":\"2018-08-01 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":174,\"Nemotecnico\":\"BESALCO\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"1995-02-13 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":48000.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":29127360.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":606.82000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75625,\"IdCliente\":30772,\"IdPortafolio\":43888,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T3\",\"FechaLiquidacion\":\"2018-08-03 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACX\",\"IdInstrumento\":2,\"Nemotecnico\":\"AAPL\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"2012-05-22 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":5.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":955.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":191.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null},{\"TablaOrigen\":\"TRANSITO\",\"IdCustodia\":75628,\"IdCliente\":30772,\"IdPortafolio\":43888,\"EsRegistroNuevo\":\"S\",\"GenerarNuevoFolioCustodia\":\"N\",\"OrigenProcesamiento\":\"INGRESO_CUSTODIA\",\"FechaOperacion\":\"2018-07-31 00:00:00.000\",\"FechaVencimientoOperacion\":\"0001-01-01 00:00:00.000\",\"PlazoEnDiasOperacion\":0,\"IdCondicionLiquidacion\":\"T3\",\"FechaLiquidacion\":\"2018-08-03 00:00:00.000\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACX\",\"IdInstrumento\":2,\"Nemotecnico\":\"AAPL\",\"FechaVencimientoInstrumento\":\"0001-01-01 00:00:00.000\",\"FechaEmisionInstrumento\":\"2012-05-22 00:00:00.000\",\"TieneCorteDeCupon\":\"N\",\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmisionInstrumento\":\"\",\"IdTipoReajusteInstrumento\":null,\"Cantidad\":5.00000000,\"ValorRescate\":0E-8,\"MontoCompra\":955.00000000,\"MontoCompraMonedaSistema\":null,\"MontoMercadoMonedaSistema\":null,\"MontoCompraSubyacente\":null,\"MontoCompraGarantia\":null,\"PrecioCompra\":191.00000000,\"TasaCompra\":0E-8,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null}]}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_FechaAnterior\":null,\"p_FechaActual\":\"2025-02-28\",\"p_FechaProximo\":null,\"p_ValorizarCarteras\":\"S\",\"p_ValorizarGarantias\":\"S\",\"p_ValorizaATasaMercado\":\"S\",\"p_ValorizaTIF\":\"S\",\"p_SimularOperacion\":\"N\",\"p_TicketProcesamiento\":0,\"p_OrigenProcesamiento\":\"CONSULTA_CARTERAS\",\"p_OmiteValorizarAnteriorWS\":\"N\",\"p_Instrumentos\":[{\"IdClasificacionTipoInstrumento\":null,\"IdTipoInstrumento\":null,\"IdInstrumento\":null,\"IdEmisor\":null,\"FechaVencimiento\":null,\"IdTipoReajuste\":null,\"EsFungible\":null,\"IdBoveda\":null}],\"p_Carteras\":[{\"FolioCartera\":\"\",\"FolioGarantia\":23744,\"FolioSubyacente\":null,\"IdCliente\":7113,\"IdPortafolio\":7903}]}";
				ValorizarCustodiaEntrada Objin = gson.fromJson(json, ValorizarCustodiaEntrada.class);
								
				ValorizarCustodia  kk = new ValorizarCustodia();
				kk.logica(con, Objin);
				con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
	private String generateUpdateCarteraSql(ObjUpdateTablas cartera) {
		StringBuilder updateSql = new StringBuilder();
		updateSql.append((cartera.FechaFactorMonedaInstrumento!=null) ? "\"FechaFactorMonedaInstrumento\" = :FechaFactorMonedaInstrumento , " : "");
		updateSql.append((cartera.ValorFactorMonedaInstrumento!=null) ? "\"ValorFactorMonedaInstrumento\" = :ValorFactorMonedaInstrumento , " : "");
		updateSql.append((cartera.FechaFactorMonedaAdicional!=null) ? "\"FechaFactorMonedaAdicional\" = :FechaFactorMonedaAdicional , " : "");
		updateSql.append((cartera.ValorFactorMonedaAdicional!=null) ? "\"ValorFactorMonedaAdicional\" = :ValorFactorMonedaAdicional , " : "");
		updateSql.append((cartera.TIFAfectoBeneficio!=null) ? "\"TIFAfectoBeneficio\" = :TIFAfectoBeneficio , " : "");
		updateSql.append((cartera.TIFTasa!=null) ? "\"TIFTasa\" = :TIFTasa , " : "");
		updateSql.append((cartera.MontoCompra!=null) ? "\"MontoCompra\" = :MontoCompra , " : ""); 
		updateSql.append((cartera.MontoCompraMonedaInstrumento!=null) ? "\"MontoCompraMonedaInstrumento\" = :MontoCompraMonedaInstrumento , " : ""); 
		updateSql.append((cartera.MontoCompraMonedaAdicional!=null) ? "\"MontoCompraMonedaAdicional\" = :MontoCompraMonedaAdicional , " : ""); 
		updateSql.append((cartera.MontoAPlazo!=null) ? "\"MontoAPlazo\" = :MontoAPlazo , " : "");
		updateSql.append((cartera.MontoAPlazoMonedaInstrumento!=null) ? "\"MontoAPlazoMonedaInstrumento\" = :MontoAPlazoMonedaInstrumento , " : "");
		updateSql.append((cartera.MontoAPlazoMonedaAdicional!=null) ? "\"MontoAPlazoMonedaAdicional\" = :MontoAPlazoMonedaAdicional , " : "");
		updateSql.append((cartera.MontoGarantiaMonedaAdicional!=null) ? "\"MontoGarantiaMonedaAdicional\" = :MontoGarantiaMonedaAdicional , " : "");
		updateSql.append((cartera.MontoSubyacenteMonedaAdicional!=null) ? "\"MontoSubyacenteMonedaAdicional\" = :MontoSubyacenteMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGTicketValorizacion!=null) ? "\"INGTicketValorizacion\" = :INGTicketValorizacion , " : "");
		updateSql.append((cartera.INGValorizacionConProblemas!=null) ? "\"INGValorizacionConProblemas\" = :INGValorizacionConProblemas , " : ""); 
		updateSql.append((cartera.INGPrecioPromedioCompra!=null) ? "\"INGPrecioPromedioCompra\" = :INGPrecioPromedioCompra , " : "");
		updateSql.append((cartera.INGMontoPrecioPromedioCompra!=null) ? "\"INGMontoPrecioPromedioCompra\" = :INGMontoPrecioPromedioCompra , " : "");
		updateSql.append((cartera.INGMontoPrecioPromedioMonedaInstrumento!=null) ? "\"INGMontoPrecioPromedioMonedaInstrumento\" = :INGMontoPrecioPromedioMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoPrecioPromedioMonedaAdicional!=null) ? "\"INGMontoPrecioPromedioMonedaAdicional\" = :INGMontoPrecioPromedioMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoAPlazoMonedaAdicional!=null) ? "\"INGMontoAPlazoMonedaAdicional\" = :INGMontoAPlazoMonedaAdicional , " : "");
		updateSql.append((cartera.INGFechaValorizacionAnterior!=null) ? "\"INGFechaValorizacionAnterior\" = :INGFechaValorizacionAnterior , " : "");
		updateSql.append((cartera.INGFechaValorizacionActual!=null) ? "\"INGFechaValorizacionActual\" = :INGFechaValorizacionActual , " : "");
		updateSql.append((cartera.INGFechaValorizacionProximo!=null) ? "\"INGFechaValorizacionProximo\" = :INGFechaValorizacionProximo , " : "");
		updateSql.append((cartera.INGFechaFactorMonedaInstrumentoAnterior!=null) ? "\"INGFechaFactorMonedaInstrumentoAnterior\" = :INGFechaFactorMonedaInstrumentoAnterior , " : "");
		updateSql.append((cartera.INGValorFactorMonedaInstrumentoAnterior!=null) ? "\"INGValorFactorMonedaInstrumentoAnterior\" = :INGValorFactorMonedaInstrumentoAnterior , " : "");
		updateSql.append((cartera.INGFechaFactorMonedaInstrumentoActual!=null) ? "\"INGFechaFactorMonedaInstrumentoActual\" = :INGFechaFactorMonedaInstrumentoActual , " : "");
		updateSql.append((cartera.INGValorFactorMonedaInstrumentoActual!=null) ? "\"INGValorFactorMonedaInstrumentoActual\" = :INGValorFactorMonedaInstrumentoActual , " : "");
		updateSql.append((cartera.INGFechaFactorMonedaInstrumentoProximo!=null) ? "\"INGFechaFactorMonedaInstrumentoProximo\" = :INGFechaFactorMonedaInstrumentoProximo , " : "");
		updateSql.append((cartera.INGValorFactorMonedaInstrumentoProximo!=null) ? "\"INGValorFactorMonedaInstrumentoProximo\" = :INGValorFactorMonedaInstrumentoProximo , " : "");
		updateSql.append((cartera.INGFechaFactorMonedaAdicionalAnterior!=null) ? "\"INGFechaFactorMonedaAdicionalAnterior\" = :INGFechaFactorMonedaAdicionalAnterior , " : "");
		updateSql.append((cartera.INGValorFactorMonedaAdicionalAnterior!=null) ? "\"INGValorFactorMonedaAdicionalAnterior\" = :INGValorFactorMonedaAdicionalAnterior , " : "");
		updateSql.append((cartera.INGFechaFactorMonedaAdicionalActual!=null) ? "\"INGFechaFactorMonedaAdicionalActual\" = :INGFechaFactorMonedaAdicionalActual , " : "");
		updateSql.append((cartera.INGValorFactorMonedaAdicionalActual!=null) ? "\"INGValorFactorMonedaAdicionalActual\" = :INGValorFactorMonedaAdicionalActual , " : "");
		updateSql.append((cartera.INGFechaFactorMonedaAdicionalProximo!=null) ? "\"INGFechaFactorMonedaAdicionalProximo\" = :INGFechaFactorMonedaAdicionalProximo , " : "");
		updateSql.append((cartera.INGValorFactorMonedaAdicionalProximo!=null) ? "\"INGValorFactorMonedaAdicionalProximo\" = :INGValorFactorMonedaAdicionalProximo , " : "");
		updateSql.append((cartera.INGMontoATasaCompraAnterior!=null) ? "\"INGMontoATasaCompraAnterior\" = :INGMontoATasaCompraAnterior , " : "");
		updateSql.append((cartera.INGMontoInteresCompraAnterior!=null) ? "\"INGMontoInteresCompraAnterior\" = :INGMontoInteresCompraAnterior , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraAnterior!=null) ? "\"INGMontoReajusteCompraAnterior\" = :INGMontoReajusteCompraAnterior , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorMonedaInstrumento!=null) ? "\"INGMontoCompraAnteriorMonedaInstrumento\" = :INGMontoCompraAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCompraAnteriorMonedaInstrumento!=null) ? "\"INGMontoInteresCompraAnteriorMonedaInstrumento\" = :INGMontoInteresCompraAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraAnteriorMonedaInstrumento!=null) ? "\"INGMontoReajusteCompraAnteriorMonedaInstrumento\" = :INGMontoReajusteCompraAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorMonedaAdicional!=null) ? "\"INGMontoCompraAnteriorMonedaAdicional\" = :INGMontoCompraAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresCompraAnteriorMonedaAdicional!=null) ? "\"INGMontoInteresCompraAnteriorMonedaAdicional\" = :INGMontoInteresCompraAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraAnteriorMonedaAdicional!=null) ? "\"INGMontoReajusteCompraAnteriorMonedaAdicional\" = :INGMontoReajusteCompraAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationCompraAnterior!=null) ? "\"INGDurationCompraAnterior\" = :INGDurationCompraAnterior , " : "");
		updateSql.append((cartera.INGConvexidadCompraAnterior!=null) ? "\"INGConvexidadCompraAnterior\" = :INGConvexidadCompraAnterior , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorPibMas!=null) ? "\"INGMontoCompraAnteriorPibMas\" = :INGMontoCompraAnteriorPibMas , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorPibMasMonedaInstrumento!=null) ? "\"INGMontoCompraAnteriorPibMasMonedaInstrumento\" = :INGMontoCompraAnteriorPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorPibMasMonedaAdicional!=null) ? "\"INGMontoCompraAnteriorPibMasMonedaAdicional\" = :INGMontoCompraAnteriorPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorPibMenos!=null) ? "\"INGMontoCompraAnteriorPibMenos\" = :INGMontoCompraAnteriorPibMenos , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorPibMenosMonedaInstrumento!=null) ? "\"INGMontoCompraAnteriorPibMenosMonedaInstrumento\" = :INGMontoCompraAnteriorPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCompraAnteriorPibMenosMonedaAdicional!=null) ? "\"INGMontoCompraAnteriorPibMenosMonedaAdicional\" = :INGMontoCompraAnteriorPibMenosMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGMontoATasaCompraActual!=null) ? "\"INGMontoATasaCompraActual\" = :INGMontoATasaCompraActual , " : "");
		updateSql.append((cartera.INGMontoCompraActualMonedaInstrumento!=null) ? "\"INGMontoCompraActualMonedaInstrumento\" = :INGMontoCompraActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCompraActualMonedaAdicional!=null) ? "\"INGMontoCompraActualMonedaAdicional\" = :INGMontoCompraActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationCompraActual!=null) ? "\"INGDurationCompraActual\" = :INGDurationCompraActual , " : "");
		updateSql.append((cartera.INGConvexidadCompraActual!=null) ? "\"INGConvexidadCompraActual\" = :INGConvexidadCompraActual , " : ""); 
		updateSql.append((cartera.INGMontoInteresCompraActual!=null) ? "\"INGMontoInteresCompraActual\" = :INGMontoInteresCompraActual , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraActual!=null) ? "\"INGMontoReajusteCompraActual\" = :INGMontoReajusteCompraActual , " : "");
		updateSql.append((cartera.INGMontoInteresCompraActualMonedaInstrumento!=null) ? "\"INGMontoInteresCompraActualMonedaInstrumento\" = :INGMontoInteresCompraActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraActualMonedaInstrumento!=null) ? "\"INGMontoReajusteCompraActualMonedaInstrumento\" = :INGMontoReajusteCompraActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCompraActualMonedaAdicional!=null) ? "\"INGMontoInteresCompraActualMonedaAdicional\" = :INGMontoInteresCompraActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraActualMonedaAdicional!=null) ? "\"INGMontoReajusteCompraActualMonedaAdicional\" = :INGMontoReajusteCompraActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoCompraActual!=null) ? "\"INGMontoInteresCorridoCompraActual\" = :INGMontoInteresCorridoCompraActual , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoCompraActual!=null) ? "\"INGMontoReajusteCorridoCompraActual\" = :INGMontoReajusteCorridoCompraActual , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoCompraActualMonedaInstrumento!=null) ? "\"INGMontoInteresCorridoCompraActualMonedaInstrumento\" = :INGMontoInteresCorridoCompraActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoCompraActualMonedaInstrumento!=null) ? "\"INGMontoReajusteCorridoCompraActualMonedaInstrumento\" = :INGMontoReajusteCorridoCompraActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoCompraActualMonedaAdicional!=null) ? "\"INGMontoInteresCorridoCompraActualMonedaAdicional\" = :INGMontoInteresCorridoCompraActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoCompraActualMonedaAdicional!=null) ? "\"INGMontoReajusteCorridoCompraActualMonedaAdicional\" = :INGMontoReajusteCorridoCompraActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoCompraActualPibMas!=null) ? "\"INGMontoCompraActualPibMas\" = :INGMontoCompraActualPibMas , " : "");
		updateSql.append((cartera.INGMontoCompraActualPibMasMonedaInstrumento!=null) ? "\"INGMontoCompraActualPibMasMonedaInstrumento\" = :INGMontoCompraActualPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCompraActualPibMasMonedaAdicional!=null) ? "\"INGMontoCompraActualPibMasMonedaAdicional\" = :INGMontoCompraActualPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoCompraActualPibMenos!=null) ? "\"INGMontoCompraActualPibMenos\" = :INGMontoCompraActualPibMenos , " : "");
		updateSql.append((cartera.INGMontoCompraActualPibMenosMonedaInstrumento!=null) ? "\"INGMontoCompraActualPibMenosMonedaInstrumento\" = :INGMontoCompraActualPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCompraActualPibMenosMonedaAdicional!=null) ? "\"INGMontoCompraActualPibMenosMonedaAdicional\" = :INGMontoCompraActualPibMenosMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGMontoATasaCompraProximo!=null) ? "\"INGMontoATasaCompraProximo\" = :INGMontoATasaCompraProximo , " : "");
		updateSql.append((cartera.INGMontoInteresCompraProximo!=null) ? "\"INGMontoInteresCompraProximo\" = :INGMontoInteresCompraProximo , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraProximo!=null) ? "\"INGMontoReajusteCompraProximo\" = :INGMontoReajusteCompraProximo , " : "");
		updateSql.append((cartera.INGMontoCompraProximoMonedaInstrumento!=null) ? "\"INGMontoCompraProximoMonedaInstrumento\" = :INGMontoCompraProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCompraProximoMonedaInstrumento!=null) ? "\"INGMontoInteresCompraProximoMonedaInstrumento\" = :INGMontoInteresCompraProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraProximoMonedaInstrumento!=null) ? "\"INGMontoReajusteCompraProximoMonedaInstrumento\" = :INGMontoReajusteCompraProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCompraProximoMonedaAdicional!=null) ? "\"INGMontoCompraProximoMonedaAdicional\" = :INGMontoCompraProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresCompraProximoMonedaAdicional!=null) ? "\"INGMontoInteresCompraProximoMonedaAdicional\" = :INGMontoInteresCompraProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCompraProximoMonedaAdicional!=null) ? "\"INGMontoReajusteCompraProximoMonedaAdicional\" = :INGMontoReajusteCompraProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationCompraProximo!=null) ? "\"INGDurationCompraProximo\" = :INGDurationCompraProximo , " : "");
		updateSql.append((cartera.INGConvexidadCompraProximo!=null) ? "\"INGConvexidadCompraProximo\" = :INGConvexidadCompraProximo , " : ""); 
		updateSql.append((cartera.INGFechaPrecioTasaMercadoAnterior!=null) ? "\"INGFechaPrecioTasaMercadoAnterior\" = :INGFechaPrecioTasaMercadoAnterior , " : "");
		updateSql.append((cartera.INGFuenteTasaPrecioMercadoAnterior!=null) ? "\"INGFuenteTasaPrecioMercadoAnterior\" = :INGFuenteTasaPrecioMercadoAnterior , " : "");
		updateSql.append((cartera.INGPrecioMercadoAnterior!=null) ? "\"INGPrecioMercadoAnterior\" = :INGPrecioMercadoAnterior , " : "");
		updateSql.append((cartera.INGTasaMercadoAnterior!=null) ? "\"INGTasaMercadoAnterior\" = :INGTasaMercadoAnterior , " : "");
		updateSql.append((cartera.INGMontoATasaMercadoAnterior!=null) ? "\"INGMontoATasaMercadoAnterior\" = :INGMontoATasaMercadoAnterior , " : "");
		updateSql.append((cartera.INGMontoInteresMercadoAnterior!=null) ? "\"INGMontoInteresMercadoAnterior\" = :INGMontoInteresMercadoAnterior , " : "");
		updateSql.append((cartera.INGMontoReajusteMercadoAnterior!=null) ? "\"INGMontoReajusteMercadoAnterior\" = :INGMontoReajusteMercadoAnterior , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorMonedaInstrumento!=null) ? "\"INGMontoMercadoAnteriorMonedaInstrumento\" = :INGMontoMercadoAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresMercadoAnteriorMonedaInstrumento!=null) ? "\"INGMontoInteresMercadoAnteriorMonedaInstrumento\" = :INGMontoInteresMercadoAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteMercadoAnteriorMonedaInstrumento!=null) ? "\"INGMontoReajusteMercadoAnteriorMonedaInstrumento\" = :INGMontoReajusteMercadoAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorMonedaAdicional!=null) ? "\"INGMontoMercadoAnteriorMonedaAdicional\" = :INGMontoMercadoAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresMercadoAnteriorMonedaAdicional!=null) ? "\"INGMontoInteresMercadoAnteriorMonedaAdicional\" = :INGMontoInteresMercadoAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteMercadoAnteriorMonedaAdicional!=null) ? "\"INGMontoReajusteMercadoAnteriorMonedaAdicional\" = :INGMontoReajusteMercadoAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationMercadoAnterior!=null) ? "\"INGDurationMercadoAnterior\" = :INGDurationMercadoAnterior , " : "");
		updateSql.append((cartera.INGConvexidadMercadoAnterior!=null) ? "\"INGConvexidadMercadoAnterior\" = :INGConvexidadMercadoAnterior , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorPibMas!=null) ? "\"INGMontoMercadoAnteriorPibMas\" = :INGMontoMercadoAnteriorPibMas , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorPibMasMonedaInstrumento!=null) ? "\"INGMontoMercadoAnteriorPibMasMonedaInstrumento\" = :INGMontoMercadoAnteriorPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorPibMasMonedaAdicional!=null) ? "\"INGMontoMercadoAnteriorPibMasMonedaAdicional\" = :INGMontoMercadoAnteriorPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorPibMenos!=null) ? "\"INGMontoMercadoAnteriorPibMenos\" = :INGMontoMercadoAnteriorPibMenos , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorPibMenosMonedaInstrumento!=null) ? "\"INGMontoMercadoAnteriorPibMenosMonedaInstrumento\" = :INGMontoMercadoAnteriorPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoMercadoAnteriorPibMenosMonedaAdicional!=null) ? "\"INGMontoMercadoAnteriorPibMenosMonedaAdicional\" = :INGMontoMercadoAnteriorPibMenosMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGFechaPrecioTasaMercadoActual!=null) ? "\"INGFechaPrecioTasaMercadoActual\" = :INGFechaPrecioTasaMercadoActual , " : "");
		updateSql.append((cartera.INGFuenteTasaPrecioMercadoActual!=null) ? "\"INGFuenteTasaPrecioMercadoActual\" = :INGFuenteTasaPrecioMercadoActual , " : "");
		updateSql.append((cartera.INGPrecioMercadoActual!=null) ? "\"INGPrecioMercadoActual\" = :INGPrecioMercadoActual , " : "");
		updateSql.append((cartera.INGTasaMercadoActual!=null) ? "\"INGTasaMercadoActual\" = :INGTasaMercadoActual , " : "");
		updateSql.append((cartera.INGMontoATasaMercadoActual!=null) ? "\"INGMontoATasaMercadoActual\" = :INGMontoATasaMercadoActual , " : "");
		updateSql.append((cartera.INGMontoMercadoActualMonedaInstrumento!=null) ? "\"INGMontoMercadoActualMonedaInstrumento\" = :INGMontoMercadoActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoMercadoActualMonedaAdicional!=null) ? "\"INGMontoMercadoActualMonedaAdicional\" = :INGMontoMercadoActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationMercadoActual!=null) ? "\"INGDurationMercadoActual\" = :INGDurationMercadoActual , " : "");
		updateSql.append((cartera.INGConvexidadMercadoActual!=null) ? "\"INGConvexidadMercadoActual\" = :INGConvexidadMercadoActual , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoMercadoActual!=null) ? "\"INGMontoInteresCorridoMercadoActual\" = :INGMontoInteresCorridoMercadoActual , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoMercadoActual!=null) ? "\"INGMontoReajusteCorridoMercadoActual\" = :INGMontoReajusteCorridoMercadoActual , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoMercadoActualMonedaInstrumento!=null) ? "\"INGMontoInteresCorridoMercadoActualMonedaInstrumento\" = :INGMontoInteresCorridoMercadoActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoMercadoActualMonedaInstrumento!=null) ? "\"INGMontoReajusteCorridoMercadoActualMonedaInstrumento\" = :INGMontoReajusteCorridoMercadoActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoMercadoActualMonedaAdicional!=null) ? "\"INGMontoInteresCorridoMercadoActualMonedaAdicional\" = :INGMontoInteresCorridoMercadoActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoMercadoActualMonedaAdicional!=null) ? "\"INGMontoReajusteCorridoMercadoActualMonedaAdicional\" = :INGMontoReajusteCorridoMercadoActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoMercadoActualPibMas!=null) ? "\"INGMontoMercadoActualPibMas\" = :INGMontoMercadoActualPibMas , " : "");
		updateSql.append((cartera.INGMontoMercadoActualPibMasMonedaInstrumento!=null) ? "\"INGMontoMercadoActualPibMasMonedaInstrumento\" = :INGMontoMercadoActualPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoMercadoActualPibMasMonedaAdicional!=null) ? "\"INGMontoMercadoActualPibMasMonedaAdicional\" = :INGMontoMercadoActualPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoMercadoActualPibMenos!=null) ? "\"INGMontoMercadoActualPibMenos\" = :INGMontoMercadoActualPibMenos , " : "");
		updateSql.append((cartera.INGMontoMercadoActualPibMenosMonedaInstrumento!=null) ? "\"INGMontoMercadoActualPibMenosMonedaInstrumento\" = :INGMontoMercadoActualPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoMercadoActualPibMenosMonedaAdicional!=null) ? "\"INGMontoMercadoActualPibMenosMonedaAdicional\" = :INGMontoMercadoActualPibMenosMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGFechaPrecioTasaMercadoProximo!=null) ? "\"INGFechaPrecioTasaMercadoProximo\" = :INGFechaPrecioTasaMercadoProximo , " : "");
		updateSql.append((cartera.INGFuenteTasaPrecioMercadoProximo!=null) ? "\"INGFuenteTasaPrecioMercadoProximo\" = :INGFuenteTasaPrecioMercadoProximo , " : "");
		updateSql.append((cartera.INGPrecioMercadoProximo!=null) ? "\"INGPrecioMercadoProximo\" = :INGPrecioMercadoProximo , " : "");
		updateSql.append((cartera.INGTasaMercadoProximo!=null) ? "\"INGTasaMercadoProximo\" = :INGTasaMercadoProximo , " : "");
		updateSql.append((cartera.INGMontoATasaMercadoProximo!=null) ? "\"INGMontoATasaMercadoProximo\" = :INGMontoATasaMercadoProximo , " : "");
		updateSql.append((cartera.INGMontoInteresMercadoProximo!=null) ? "\"INGMontoInteresMercadoProximo\" = :INGMontoInteresMercadoProximo , " : "");
		updateSql.append((cartera.INGMontoReajusteMercadoProximo!=null) ? "\"INGMontoReajusteMercadoProximo\" = :INGMontoReajusteMercadoProximo , " : "");
		updateSql.append((cartera.INGMontoMercadoProximoMonedaInstrumento!=null) ? "\"INGMontoMercadoProximoMonedaInstrumento\" = :INGMontoMercadoProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresMercadoProximoMonedaInstrumento!=null) ? "\"INGMontoInteresMercadoProximoMonedaInstrumento\" = :INGMontoInteresMercadoProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteMercadoProximoMonedaInstrumento!=null) ? "\"INGMontoReajusteMercadoProximoMonedaInstrumento\" = :INGMontoReajusteMercadoProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoMercadoProximoMonedaAdicional!=null) ? "\"INGMontoMercadoProximoMonedaAdicional\" = :INGMontoMercadoProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresMercadoProximoMonedaAdicional!=null) ? "\"INGMontoInteresMercadoProximoMonedaAdicional\" = :INGMontoInteresMercadoProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteMercadoProximoMonedaAdicional!=null) ? "\"INGMontoReajusteMercadoProximoMonedaAdicional\" = :INGMontoReajusteMercadoProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationMercadoProximo!=null) ? "\"INGDurationMercadoProximo\" = :INGDurationMercadoProximo , " : "");
		updateSql.append((cartera.INGConvexidadMercadoProximo!=null) ? "\"INGConvexidadMercadoProximo\" = :INGConvexidadMercadoProximo , " : ""); 
		updateSql.append((cartera.INGTasaCorredora!=null) ? "\"INGTasaCorredora\" = :INGTasaCorredora , " : "");
		updateSql.append((cartera.INGFechaTasaCorredora!=null) ? "\"INGFechaTasaCorredora\" = :INGFechaTasaCorredora , " : "");
		updateSql.append((cartera.INGMontoATasaCorredora!=null) ? "\"INGMontoATasaCorredora\" = :INGMontoATasaCorredora , " : "");
		updateSql.append((cartera.INGMontoATasaCorredoraMonedaInstrumento!=null) ? "\"INGMontoATasaCorredoraMonedaInstrumento\" = :INGMontoATasaCorredoraMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoATasaCorredoraMonedaAdicional!=null) ? "\"INGMontoATasaCorredoraMonedaAdicional\" = :INGMontoATasaCorredoraMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGFechaCorteCupon!=null) ? "\"INGFechaCorteCupon\" = :INGFechaCorteCupon , " : "");
		updateSql.append((cartera.INGNumeroCupon!=null) ? "\"INGNumeroCupon\" = :INGNumeroCupon , " : "");
		updateSql.append((cartera.INGIdMonedaPagoCupon!=null) ? "\"INGIdMonedaPagoCupon\" = :INGIdMonedaPagoCupon , " : "");
		updateSql.append((cartera.INGMontoCorteCupon!=null) ? "\"INGMontoCorteCupon\" = :INGMontoCorteCupon , " : "");
		updateSql.append((cartera.INGFechaPagoCorteCupon!=null) ? "\"INGFechaPagoCorteCupon\" = :INGFechaPagoCorteCupon , " : "");
		updateSql.append((cartera.INGInteresesPagoCorteCupon!=null) ? "\"INGInteresesPagoCorteCupon\" = :INGInteresesPagoCorteCupon , " : "");
		updateSql.append((cartera.INGMontoCapitalCorteCupon!=null) ? "\"INGMontoCapitalCorteCupon\" = :INGMontoCapitalCorteCupon , " : "");
		updateSql.append((cartera.INGMontoInteresCorteCupon!=null) ? "\"INGMontoInteresCorteCupon\" = :INGMontoInteresCorteCupon , " : "");
		updateSql.append((cartera.INGMontoReajusteCorteCupon!=null) ? "\"INGMontoReajusteCorteCupon\" = :INGMontoReajusteCorteCupon , " : "");
		updateSql.append((cartera.INGMontoCorteCuponMonedaInstrumento!=null) ? "\"INGMontoCorteCuponMonedaInstrumento\" = :INGMontoCorteCuponMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCapitalCorteCuponMonedaInstrumento!=null) ? "\"INGMontoCapitalCorteCuponMonedaInstrumento\" = :INGMontoCapitalCorteCuponMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCorteCuponMonedaInstrumento!=null) ? "\"INGMontoInteresCorteCuponMonedaInstrumento\" = :INGMontoInteresCorteCuponMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCorteCuponMonedaInstrumento!=null) ? "\"INGMontoReajusteCorteCuponMonedaInstrumento\" = :INGMontoReajusteCorteCuponMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoCorteCuponMonedaAdicional!=null) ? "\"INGMontoCorteCuponMonedaAdicional\" = :INGMontoCorteCuponMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoCapitalCorteCuponMonedaAdicional!=null) ? "\"INGMontoCapitalCorteCuponMonedaAdicional\" = :INGMontoCapitalCorteCuponMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresCorteCuponMonedaAdicional!=null) ? "\"INGMontoInteresCorteCuponMonedaAdicional\" = :INGMontoInteresCorteCuponMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCorteCuponMonedaAdicional!=null) ? "\"INGMontoReajusteCorteCuponMonedaAdicional\" = :INGMontoReajusteCorteCuponMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGMontoPresenteTIF!=null) ? "\"INGMontoPresenteTIF\" = :INGMontoPresenteTIF , " : "");
		updateSql.append((cartera.INGMontoInteresTIF!=null) ? "\"INGMontoInteresTIF\" = :INGMontoInteresTIF , " : "");
		updateSql.append((cartera.INGMontoReajusteTIF!=null) ? "\"INGMontoReajusteTIF\" = :INGMontoReajusteTIF , " : "");
		updateSql.append((cartera.INGMontoInteresAcumTIF!=null) ? "\"INGMontoInteresAcumTIF\" = :INGMontoInteresAcumTIF , " : "");
		updateSql.append((cartera.INGMontoReajusteAcumTIF!=null) ? "\"INGMontoReajusteAcumTIF\" = :INGMontoReajusteAcumTIF , " : "");
		updateSql.append((cartera.INGMontoPresenteTIFMonedaInstrumento!=null) ? "\"INGMontoPresenteTIFMonedaInstrumento\" = :INGMontoPresenteTIFMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresTIFMonedaInstrumento!=null) ? "\"INGMontoInteresTIFMonedaInstrumento\" = :INGMontoInteresTIFMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteTIFMonedaInstrumento!=null) ? "\"INGMontoReajusteTIFMonedaInstrumento\" = :INGMontoReajusteTIFMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresAcumTIFMonedaInstrumento!=null) ? "\"INGMontoInteresAcumTIFMonedaInstrumento\" = :INGMontoInteresAcumTIFMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteAcumTIFMonedaInstrumento!=null) ? "\"INGMontoReajusteAcumTIFMonedaInstrumento\" = :INGMontoReajusteAcumTIFMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoPresenteTIFMonedaAdicional!=null) ? "\"INGMontoPresenteTIFMonedaAdicional\" = :INGMontoPresenteTIFMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresTIFMonedaAdicional!=null) ? "\"INGMontoInteresTIFMonedaAdicional\" = :INGMontoInteresTIFMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteTIFMonedaAdicional!=null) ? "\"INGMontoReajusteTIFMonedaAdicional\" = :INGMontoReajusteTIFMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresAcumTIFMonedaAdicional!=null) ? "\"INGMontoInteresAcumTIFMonedaAdicional\" = :INGMontoInteresAcumTIFMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteAcumTIFMonedaAdicional!=null) ? "\"INGMontoReajusteAcumTIFMonedaAdicional\" = :INGMontoReajusteAcumTIFMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoATasaGarantiaAnterior!=null) ? "\"INGMontoATasaGarantiaAnterior\" = :INGMontoATasaGarantiaAnterior , " : "");
		updateSql.append((cartera.INGMontoInteresGarantiaAnterior!=null) ? "\"INGMontoInteresGarantiaAnterior\" = :INGMontoInteresGarantiaAnterior , " : "");
		updateSql.append((cartera.INGMontoReajusteGarantiaAnterior!=null) ? "\"INGMontoReajusteGarantiaAnterior\" = :INGMontoReajusteGarantiaAnterior , " : "");
		updateSql.append((cartera.INGMontoGarantiaAnteriorMonedaInstrumento!=null) ? "\"INGMontoGarantiaAnteriorMonedaInstrumento\" = :INGMontoGarantiaAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresGarantiaAnteriorMonedaInstrumento!=null) ? "\"INGMontoInteresGarantiaAnteriorMonedaInstrumento\" = :INGMontoInteresGarantiaAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteGarantiaAnteriorMonedaInstrumento!=null) ? "\"INGMontoReajusteGarantiaAnteriorMonedaInstrumento\" = :INGMontoReajusteGarantiaAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoGarantiaAnteriorMonedaAdicional!=null) ? "\"INGMontoGarantiaAnteriorMonedaAdicional\" = :INGMontoGarantiaAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresGarantiaAnteriorMonedaAdicional!=null) ? "\"INGMontoInteresGarantiaAnteriorMonedaAdicional\" = :INGMontoInteresGarantiaAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteGarantiaAnteriorMonedaAdicional!=null) ? "\"INGMontoReajusteGarantiaAnteriorMonedaAdicional\" = :INGMontoReajusteGarantiaAnteriorMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGMontoGarantiaAnteriorPibMas!=null) ? "\"INGMontoGarantiaAnteriorPibMas\" = :INGMontoGarantiaAnteriorPibMas , " : "");
		updateSql.append((cartera.INGMontoGarantiaAnteriorPibMasMonedaInstrumento!=null) ? "\"INGMontoGarantiaAnteriorPibMasMonedaInstrumento\" = :INGMontoGarantiaAnteriorPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoGarantiaAnteriorPibMasMonedaAdicional!=null) ? "\"INGMontoGarantiaAnteriorPibMasMonedaAdicional\" = :INGMontoGarantiaAnteriorPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoGarantiaAnteriorPibMenos!=null) ? "\"INGMontoGarantiaAnteriorPibMenos\" = :INGMontoGarantiaAnteriorPibMenos , " : "");
		updateSql.append((cartera.INGMontoGarantiaAnteriorPibMenosMonedaInstrumento!=null) ? "\"INGMontoGarantiaAnteriorPibMenosMonedaInstrumento\" = :INGMontoGarantiaAnteriorPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoGarantiaAnteriorPibMenosMonedaAdicional!=null) ? "\"INGMontoGarantiaAnteriorPibMenosMonedaAdicional\" = :INGMontoGarantiaAnteriorPibMenosMonedaAdicional , " : ""); 
		updateSql.append((cartera.INGDurationGarantiaAnterior!=null) ? "\"INGDurationGarantiaAnterior\" = :INGDurationGarantiaAnterior , " : "");
		updateSql.append((cartera.INGConvexidadGarantiaAnterior!=null) ? "\"INGConvexidadGarantiaAnterior\" = :INGConvexidadGarantiaAnterior , " : "");
		updateSql.append((cartera.INGMontoATasaGarantiaActual!=null) ? "\"INGMontoATasaGarantiaActual\" = :INGMontoATasaGarantiaActual , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualMonedaInstrumento!=null) ? "\"INGMontoGarantiaActualMonedaInstrumento\" = :INGMontoGarantiaActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualMonedaAdicional!=null) ? "\"INGMontoGarantiaActualMonedaAdicional\" = :INGMontoGarantiaActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualPibMas!=null) ? "\"INGMontoGarantiaActualPibMas\" = :INGMontoGarantiaActualPibMas , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualPibMasMonedaInstrumento!=null) ? "\"INGMontoGarantiaActualPibMasMonedaInstrumento\" = :INGMontoGarantiaActualPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualPibMasMonedaAdicional!=null) ? "\"INGMontoGarantiaActualPibMasMonedaAdicional\" = :INGMontoGarantiaActualPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualPibMenos!=null) ? "\"INGMontoGarantiaActualPibMenos\" = :INGMontoGarantiaActualPibMenos , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualPibMenosMonedaInstrumento!=null) ? "\"INGMontoGarantiaActualPibMenosMonedaInstrumento\" = :INGMontoGarantiaActualPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoGarantiaActualPibMenosMonedaAdicional!=null) ? "\"INGMontoGarantiaActualPibMenosMonedaAdicional\" = :INGMontoGarantiaActualPibMenosMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationGarantiaActual!=null) ? "\"INGDurationGarantiaActual\" = :INGDurationGarantiaActual , " : "");
		updateSql.append((cartera.INGConvexidadGarantiaActual!=null) ? "\"INGConvexidadGarantiaActual\" = :INGConvexidadGarantiaActual , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoGarantiaActual!=null) ? "\"INGMontoInteresCorridoGarantiaActual\" = :INGMontoInteresCorridoGarantiaActual , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoGarantiaActual!=null) ? "\"INGMontoReajusteCorridoGarantiaActual\" = :INGMontoReajusteCorridoGarantiaActual , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoGarantiaActualMonedaInstrumento!=null) ? "\"INGMontoInteresCorridoGarantiaActualMonedaInstrumento\" = :INGMontoInteresCorridoGarantiaActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoGarantiaActualMonedaInstrumento!=null) ? "\"INGMontoReajusteCorridoGarantiaActualMonedaInstrumento\" = :INGMontoReajusteCorridoGarantiaActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoGarantiaActualMonedaAdicional!=null) ? "\"INGMontoInteresCorridoGarantiaActualMonedaAdicional\" = :INGMontoInteresCorridoGarantiaActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoGarantiaActualMonedaAdicional!=null) ? "\"INGMontoReajusteCorridoGarantiaActualMonedaAdicional\" = :INGMontoReajusteCorridoGarantiaActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoATasaGarantiaProximo!=null) ? "\"INGMontoATasaGarantiaProximo\" = :INGMontoATasaGarantiaProximo , " : "");
		updateSql.append((cartera.INGMontoInteresGarantiaProximo!=null) ? "\"INGMontoInteresGarantiaProximo\" = :INGMontoInteresGarantiaProximo , " : "");
		updateSql.append((cartera.INGMontoReajusteGarantiaProximo!=null) ? "\"INGMontoReajusteGarantiaProximo\" = :INGMontoReajusteGarantiaProximo , " : "");
		updateSql.append((cartera.INGMontoGarantiaProximoMonedaInstrumento!=null) ? "\"INGMontoGarantiaProximoMonedaInstrumento\" = :INGMontoGarantiaProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresGarantiaProximoMonedaInstrumento!=null) ? "\"INGMontoInteresGarantiaProximoMonedaInstrumento\" = :INGMontoInteresGarantiaProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteGarantiaProximoMonedaInstrumento!=null) ? "\"INGMontoReajusteGarantiaProximoMonedaInstrumento\" = :INGMontoReajusteGarantiaProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoGarantiaProximoMonedaAdicional!=null) ? "\"INGMontoGarantiaProximoMonedaAdicional\" = :INGMontoGarantiaProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresGarantiaProximoMonedaAdicional!=null) ? "\"INGMontoInteresGarantiaProximoMonedaAdicional\" = :INGMontoInteresGarantiaProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteGarantiaProximoMonedaAdicional!=null) ? "\"INGMontoReajusteGarantiaProximoMonedaAdicional\" = :INGMontoReajusteGarantiaProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationGarantiaProximo!=null) ? "\"INGDurationGarantiaProximo\" = :INGDurationGarantiaProximo , " : "");
		updateSql.append((cartera.INGConvexidadGarantiaProximo!=null) ? "\"INGConvexidadGarantiaProximo\" = :INGConvexidadGarantiaProximo , " : "");
		updateSql.append((cartera.INGMontoATasaSubyacenteAnterior!=null) ? "\"INGMontoATasaSubyacenteAnterior\" = :INGMontoATasaSubyacenteAnterior , " : "");
		updateSql.append((cartera.INGMontoInteresSubyacenteAnterior!=null) ? "\"INGMontoInteresSubyacenteAnterior\" = :INGMontoInteresSubyacenteAnterior , " : "");
		updateSql.append((cartera.INGMontoReajusteSubyacenteAnterior!=null) ? "\"INGMontoReajusteSubyacenteAnterior\" = :INGMontoReajusteSubyacenteAnterior , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorMonedaInstrumento!=null) ? "\"INGMontoSubyacenteAnteriorMonedaInstrumento\" = :INGMontoSubyacenteAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresSubyacenteAnteriorMonedaInstrumento!=null) ? "\"INGMontoInteresSubyacenteAnteriorMonedaInstrumento\" = :INGMontoInteresSubyacenteAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteSubyacenteAnteriorMonedaInstrumento!=null) ? "\"INGMontoReajusteSubyacenteAnteriorMonedaInstrumento\" = :INGMontoReajusteSubyacenteAnteriorMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorMonedaAdicional!=null) ? "\"INGMontoSubyacenteAnteriorMonedaAdicional\" = :INGMontoSubyacenteAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresSubyacenteAnteriorMonedaAdicional!=null) ? "\"INGMontoInteresSubyacenteAnteriorMonedaAdicional\" = :INGMontoInteresSubyacenteAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteSubyacenteAnteriorMonedaAdicional!=null) ? "\"INGMontoReajusteSubyacenteAnteriorMonedaAdicional\" = :INGMontoReajusteSubyacenteAnteriorMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationSubyacenteAnterior!=null) ? "\"INGDurationSubyacenteAnterior\" = :INGDurationSubyacenteAnterior , " : "");
		updateSql.append((cartera.INGConvexidadSubyacenteAnterior!=null) ? "\"INGConvexidadSubyacenteAnterior\" = :INGConvexidadSubyacenteAnterior , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorPibMas!=null) ? "\"INGMontoSubyacenteAnteriorPibMas\" = :INGMontoSubyacenteAnteriorPibMas , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorPibMasMonedaInstrumento!=null) ? "\"INGMontoSubyacenteAnteriorPibMasMonedaInstrumento\" = :INGMontoSubyacenteAnteriorPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorPibMasMonedaAdicional!=null) ? "\"INGMontoSubyacenteAnteriorPibMasMonedaAdicional\" = :INGMontoSubyacenteAnteriorPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorPibMenos!=null) ? "\"INGMontoSubyacenteAnteriorPibMenos\" = :INGMontoSubyacenteAnteriorPibMenos , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento!=null) ? "\"INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento\" = :INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoSubyacenteAnteriorPibMenosMonedaAdicional!=null) ? "\"INGMontoSubyacenteAnteriorPibMenosMonedaAdicional\" = :INGMontoSubyacenteAnteriorPibMenosMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoATasaSubyacenteActual!=null) ? "\"INGMontoATasaSubyacenteActual\" = :INGMontoATasaSubyacenteActual , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualMonedaInstrumento!=null) ? "\"INGMontoSubyacenteActualMonedaInstrumento\" = :INGMontoSubyacenteActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualMonedaAdicional!=null) ? "\"INGMontoSubyacenteActualMonedaAdicional\" = :INGMontoSubyacenteActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationSubyacenteActual!=null) ? "\"INGDurationSubyacenteActual\" = :INGDurationSubyacenteActual , " : "");
		updateSql.append((cartera.INGConvexidadSubyacenteActual!=null) ? "\"INGConvexidadSubyacenteActual\" = :INGConvexidadSubyacenteActual , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoSubyacenteActual!=null) ? "\"INGMontoInteresCorridoSubyacenteActual\" = :INGMontoInteresCorridoSubyacenteActual , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoSubyacenteActual!=null) ? "\"INGMontoReajusteCorridoSubyacenteActual\" = :INGMontoReajusteCorridoSubyacenteActual , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoSubyacenteActualMonedaInstrumento!=null) ? "\"INGMontoInteresCorridoSubyacenteActualMonedaInstrumento\" = :INGMontoInteresCorridoSubyacenteActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento!=null) ? "\"INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento\" = :INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresCorridoSubyacenteActualMonedaAdicional!=null) ? "\"INGMontoInteresCorridoSubyacenteActualMonedaAdicional\" = :INGMontoInteresCorridoSubyacenteActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteCorridoSubyacenteActualMonedaAdicional!=null) ? "\"INGMontoReajusteCorridoSubyacenteActualMonedaAdicional\" = :INGMontoReajusteCorridoSubyacenteActualMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualPibMas!=null) ? "\"INGMontoSubyacenteActualPibMas\" = :INGMontoSubyacenteActualPibMas , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualPibMasMonedaInstrumento!=null) ? "\"INGMontoSubyacenteActualPibMasMonedaInstrumento\" = :INGMontoSubyacenteActualPibMasMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualPibMasMonedaAdicional!=null) ? "\"INGMontoSubyacenteActualPibMasMonedaAdicional\" = :INGMontoSubyacenteActualPibMasMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualPibMenos!=null) ? "\"INGMontoSubyacenteActualPibMenos\" = :INGMontoSubyacenteActualPibMenos , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualPibMenosMonedaInstrumento!=null) ? "\"INGMontoSubyacenteActualPibMenosMonedaInstrumento\" = :INGMontoSubyacenteActualPibMenosMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoSubyacenteActualPibMenosMonedaAdicional!=null) ? "\"INGMontoSubyacenteActualPibMenosMonedaAdicional\" = :INGMontoSubyacenteActualPibMenosMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoATasaSubyacenteProximo!=null) ? "\"INGMontoATasaSubyacenteProximo\" = :INGMontoATasaSubyacenteProximo , " : "");
		updateSql.append((cartera.INGMontoInteresSubyacenteProximo!=null) ? "\"INGMontoInteresSubyacenteProximo\" = :INGMontoInteresSubyacenteProximo , " : "");
		updateSql.append((cartera.INGMontoReajusteSubyacenteProximo!=null) ? "\"INGMontoReajusteSubyacenteProximo\" = :INGMontoReajusteSubyacenteProximo , " : "");
		updateSql.append((cartera.INGMontoSubyacenteProximoMonedaInstrumento!=null) ? "\"INGMontoSubyacenteProximoMonedaInstrumento\" = :INGMontoSubyacenteProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoInteresSubyacenteProximoMonedaInstrumento!=null) ? "\"INGMontoInteresSubyacenteProximoMonedaInstrumento\" = :INGMontoInteresSubyacenteProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoReajusteSubyacenteProximoMonedaInstrumento!=null) ? "\"INGMontoReajusteSubyacenteProximoMonedaInstrumento\" = :INGMontoReajusteSubyacenteProximoMonedaInstrumento , " : "");
		updateSql.append((cartera.INGMontoSubyacenteProximoMonedaAdicional!=null) ? "\"INGMontoSubyacenteProximoMonedaAdicional\" = :INGMontoSubyacenteProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoInteresSubyacenteProximoMonedaAdicional!=null) ? "\"INGMontoInteresSubyacenteProximoMonedaAdicional\" = :INGMontoInteresSubyacenteProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGMontoReajusteSubyacenteProximoMonedaAdicional!=null) ? "\"INGMontoReajusteSubyacenteProximoMonedaAdicional\" = :INGMontoReajusteSubyacenteProximoMonedaAdicional , " : "");
		updateSql.append((cartera.INGDurationSubyacenteProximo!=null) ? "\"INGDurationSubyacenteProximo\" = :INGDurationSubyacenteProximo , " : "");
		updateSql.append((cartera.INGConvexidadSubyacenteProximo!=null) ? "\"INGConvexidadSubyacenteProximo\" = :INGConvexidadSubyacenteProximo , " : "");
		updateSql.append((cartera.EGRTicketValorizacion!=null) ? "\"EGRTicketValorizacion\" = :EGRTicketValorizacion , " : "");
		updateSql.append((cartera.EGRValorizacionConProblemas!=null) ? "\"EGRValorizacionConProblemas\" = :EGRValorizacionConProblemas , " : "");
		updateSql.append((cartera.EGRFechaFactorMonedaInstrumento!=null) ? "\"EGRFechaFactorMonedaInstrumento\" = :EGRFechaFactorMonedaInstrumento , " : "");
		updateSql.append((cartera.EGRValorFactorMonedaInstrumento!=null) ? "\"EGRValorFactorMonedaInstrumento\" = :EGRValorFactorMonedaInstrumento , " : "");
		updateSql.append((cartera.EGRFechaFactorMonedaAdicional!=null) ? "\"EGRFechaFactorMonedaAdicional\" = :EGRFechaFactorMonedaAdicional , " : "");
		updateSql.append((cartera.EGRValorFactorMonedaAdicional!=null) ? "\"EGRValorFactorMonedaAdicional\" = :EGRValorFactorMonedaAdicional , " : "");
		updateSql.append((cartera.EGRMonto!=null) ? "\"EGRMonto\" = :EGRMonto , " : "");
		updateSql.append((cartera.EGRMontoMonedaInstrumento!=null) ? "\"EGRMontoMonedaInstrumento\" = :EGRMontoMonedaInstrumento , " : "");
		updateSql.append((cartera.EGRMontoMonedaAdicional!=null) ? "\"EGRMontoMonedaAdicional\" = :EGRMontoMonedaAdicional , " : "");
		updateSql.append((cartera.EGRSpread!=null) ? "\"EGRSpread\" = :EGRSpread , " : "");
		updateSql.append((cartera.EGRFechaPrecioTasaMercado!=null) ? "\"EGRFechaPrecioTasaMercado\" = :EGRFechaPrecioTasaMercado , " : "");
		updateSql.append((cartera.EGRFuenteTasaPrecioMercado!=null) ? "\"EGRFuenteTasaPrecioMercado\" = :EGRFuenteTasaPrecioMercado , " : "");
		updateSql.append((cartera.EGRPrecioMercado!=null) ? "\"EGRPrecioMercado\" = :EGRPrecioMercado , " : "");
		updateSql.append((cartera.EGRTasaMercado!=null) ? "\"EGRTasaMercado\" = :EGRTasaMercado , " : "");
		updateSql.append((cartera.EGRMontoATasaMercado!=null) ? "\"EGRMontoATasaMercado\" = :EGRMontoATasaMercado , " : "");
		updateSql.append((cartera.EGRMontoMercadoMonedaInstrumento!=null) ? "\"EGRMontoMercadoMonedaInstrumento\" = :EGRMontoMercadoMonedaInstrumento , " : "");
		updateSql.append((cartera.EGRMontoMercadoMonedaAdicional!=null) ? "\"EGRMontoMercadoMonedaAdicional\" = :EGRMontoMercadoMonedaAdicional , " : "");
		updateSql.append((cartera.EGRSpreadMercado!=null) ? "\"EGRSpreadMercado\" = :EGRSpreadMercado , " : "");
		updateSql.append((cartera.EGRPrecioAPlazo!=null) ? "\"EGRPrecioAPlazo\" = :EGRPrecioAPlazo , " : "");
		updateSql.append((cartera.EGRMontoAPlazo!=null) ? "\"EGRMontoAPlazo\" = :EGRMontoAPlazo , " : "");
		updateSql.append((cartera.EGRMontoAPlazoMonedaInstrumento!=null) ? "\"EGRMontoAPlazoMonedaInstrumento\" = :EGRMontoAPlazoMonedaInstrumento , " : "");
		updateSql.append((cartera.EGRMontoAPlazoMonedaAdicional!=null) ? "\"EGRMontoAPlazoMonedaAdicional\" = :EGRMontoAPlazoMonedaAdicional , " : "");
		updateSql.append((cartera.EGRMontoPresenteTIF!=null) ? "\"EGRMontoPresenteTIF\" = :EGRMontoPresenteTIF , " : "");
		updateSql.append((cartera.EGRMontoPresenteTIFMonedaInstrumento!=null) ? "\"EGRMontoPresenteTIFMonedaInstrumento\" = :EGRMontoPresenteTIFMonedaInstrumento , " : "");
		updateSql.append((cartera.EGRMontoPresenteTIFMonedaAdicional!=null) ? "\"EGRMontoPresenteTIFMonedaAdicional\" = :EGRMontoPresenteTIFMonedaAdicional , " : "");
		updateSql.append((cartera.EGRTipoFactorRentabilidadNeta!=null) ? "\"EGRTipoFactorRentabilidadNeta\" = :EGRTipoFactorRentabilidadNeta , " : "");
		updateSql.append((cartera.EGRFechaFactorRentabilidadNeta!=null) ? "\"EGRFechaFactorRentabilidadNeta\" = :EGRFechaFactorRentabilidadNeta , " : "");
		updateSql.append((cartera.EGRValorFactorRentabilidadNeta!=null) ? "\"EGRValorFactorRentabilidadNeta\" = :EGRValorFactorRentabilidadNeta , " : "");
		updateSql.append((cartera.EGRMontoRentabilidadNeta!=null) ? "\"EGRMontoRentabilidadNeta\" = :EGRMontoRentabilidadNeta , " : "");
		return updateSql.toString();
	}

};
