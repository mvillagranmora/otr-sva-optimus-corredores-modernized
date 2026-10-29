package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularValores;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class IngresoCustodia implements JavaCall{
	static final Logger logger = LogManager.getLogger(IngresoCustodia.class);
	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_SEL_Cartera.sql");
	private final String SQL_SelectGarantia = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_SEL_Garantia.sql");
	private final String SQL_SelectSubyacente = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_SEL_Subyacente.sql");
	
	private final String SQL_InsertTransito = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_INS_MovimientoCarteraEnTransito.sql");
	private final String SQL_InsertCartera = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_INS_Cartera.sql");
	private final String SQL_InsertSubyacente = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_INS_Subyacente.sql");
	private final String SQL_InsertGarantia = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_INS_Garantia.sql");	
	private final String SQL_InsertNegativo = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_INS_CarteraNegativa.sql");

	public static class ObjTablaCarteraNegativa {
		public Long					IdCarteraNegativa;
		public java.sql.Timestamp 	FechaIngresoRegistro; 
		public String 				FechaNeteo = "0001-01-01 00:00:00"; 
		public String 				IdEstado = "VIG"; 
		public Long 				IdUsuarioIngreso; 
		public Long 				IdUsuarioNeteo = 0L; 
		public java.sql.Date 		FechaOperacion; 
		public String 				IdTipoOperacion; 
		public Long 				IdEmpresa; 
		public Long 				IdCliente; 
		public Long 				IdPortafolio; 
		public Long 				IdInstrumento; 
		public BigDecimal 			Cantidad; 
		public BigDecimal 			ValorRescate;
		public String 				IdConcepto; 
		public Long 				IdCuentaCustodioPortafolio; 
		public String 				IdCondicionLiquidacion; 
		public java.sql.Date 		FechaLiquidacion; 
		public String 				CodigoIFRS; 
		public Long 				IdMesa; 
		public String 				Nemotecnico; 
		public String 				IdEmisor; 
		public java.sql.Date 		FechaVencimientoOperacion; 
		public String 				EsFungible; 
		public String 				IdTipoReajuste; 
		public Long 				IdOperacionMesa; 
		public String 				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public Long 				IdMovimientoComprobante; 
		public BigDecimal 			PrecioOperacion; 
		public BigDecimal 			MontoOperacionMonedaInstrumento;
		public BigDecimal 			TasaOperacion; 
		public String 				IdBoveda; 
		public String 				IdTipoProducto; 
		public Long 				IdCorredoraContraparte; 
		public String 				FolioTransaccion; 
		public java.sql.Date		FechaTransaccion; 
		public String 				IdBolsa;
		public Long 				IdGenerarMovimientoCustodia;
		public String 				IdTipoCanalIngreso;
		public Long 				IdOperacion;
		public java.sql.Date 		FechaComprobante;
		public Long 				IdAgente;
		public String 				IdMonedaInstrumento;
		public BigDecimal 			CorteMinimo;
		public Long 				IdSucursal;
		public String 				Glosa;
		public String 				NemoSVS;
		public String 				IdMonedaEmision;
		public Date 				FechaVencimientoInstrumento;
		public String 				FolioExternoReferencia;
		public Long 				FolioCustodia;
	}
	
	public static class ObjTablaCartera {
		public Long					IdCartera;
		public Long					FolioCartera;
		public Long					FolioCarteraRelacionada;
		public Long					FolioGarantiaOrigen;
		public String               FolioSistemaExterno;
		public String               ExigirGarantia;
		public String               TipoCustodia;
		public String               EstadoAnticipo;
		public java.sql.Timestamp	FechaIngresoRegistro;
		public java.sql.Timestamp	FechaModificacionRegistro;
		public Long					IdEmpresa;
		public java.sql.Date		FechaOperacion;
		public java.sql.Date		FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;
		public Long					IdCliente;
		public Long					IdPortafolio;
		public Long					IdCuentaCustodioPortafolio;	
		public Long					IdClienteRelacionado;			//tiene relacion con movimientos de mesa
		public Long					IdPortafolioRelacionado;		//tiene relacion con movimientos de mesa
		public String				IdEstado;
		public String				IdTipoProducto;
		public String				IdTipoCanalIngreso;
		public String				IdMonedaSistema;
		public String				IdMonedaAdicional;
		public java.sql.Date		FechaFactorMonedaAdicional;
		public BigDecimal			ValorFactorMonedaAdicional;
		public Long					IdMovimientoCarteraEnTransito;
		public String				IdConcepto;
		public String				IdTipoOperacion;
		public Long					IdOperacion;
		public String				IdTipoComprobante;
		public Long					FolioComprobante;
		public java.sql.Date		FechaComprobante;
		public String				FolioExternoReferencia;
		public Long					IdMesa;
		public String				IdBolsa;
		public Long					IdSucursal;
		public Long					IdAgente;
		public String				IdCondicionLiquidacion;
		public java.sql.Date		FechaLiquidacion;
		public String				EsIntraday;
		public String				UsaGarantiaEspecifica;
		public String				CobraPrimaAlInicio;
		public String				IdTipoPrestamo;
		public String				Glosa;
		public Long					IdUsuario;
		public String				IdModulo;
		public String				TipoCarteraCorredor;
		public String				TipoCarteraNormativa;
		public java.sql.Date		FechaCurseInversion;
		public String				TipoFactorAjuste;
		public BigDecimal			ValorFactorAjusteaFechaCurse;
		public BigDecimal			CostoInversionEnFactorAjuste;
		public BigDecimal			ValorFactorAjuste;
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public Long					IdInstrumento;
		public Long					IdInstrumentoAnterior;
		public String				Nemotecnico;
		public String				IdEmisor;
		public String				NemoSVS;
		public String				IdTipoReajuste;
		public String				IdMonedaEmision;
		public java.sql.Date		FechaEmisionInstrumento;
		public java.sql.Date		FechaVencimientoInstrumento;
		public String				EsFungible;
		public String				ValorizaPor;
		public String				TipoValorizacion;
		public String				TieneCorteDeCupon;
		public String				CodigoIFRS;
		public String				IdBoveda;
		public String				CuentaBoveda;
		public String				IdMonedaInstrumento;
		public Long					Tamano;
		public String				TIFAfectoBeneficio;
		public BigDecimal			TIFTasa;
		public String				ISIN;
		public java.sql.Date		FechaFactorMonedaInstrumento;
		public BigDecimal			ValorFactorMonedaInstrumento;
		public BigDecimal			Cantidad;
		public BigDecimal			CorteMinimo;
		public BigDecimal			ValorRescate;
		public BigDecimal			PrecioSucio;
		public BigDecimal			PrecioCompra;
		public BigDecimal			TasaCompra;
		public BigDecimal			MontoCompra;
		public BigDecimal			MontoCompraMonedaInstrumento;
		public BigDecimal			MontoCompraMonedaAdicional;
		public BigDecimal			TasaAgente;
		public BigDecimal			PrecioAPlazo;
		public BigDecimal			MontoAPlazo;
		public BigDecimal			MontoAPlazoMonedaInstrumento;
		public BigDecimal			MontoAPlazoMonedaAdicional;
		public java.sql.Date		FechaEfectiva;
		public Long					IdPlaza;
		public Long					IdFixing;
		public java.sql.Date		FechaFixing;
		public java.sql.Time		HoraFixing;
		public Long					IdModalidadCumplimiento;
		public Long					IdReferenciaMercado;
		public String				IdMonedaPrincipal;
		public String				IdMonedaSecundaria;
		public BigDecimal			ValorTipoCambioCierre;
		public BigDecimal			ValorParidadCierre;
		public BigDecimal			ValorTipoCambioCosto;
		public BigDecimal			ValorParidadCosto;
		public BigDecimal			MontoMonedaPrincipal;
		public BigDecimal			MontoMonedaSecundaria;
		
		public String 				EsCustodiaExterna;
		//solicitados por normativa, Mario Santander
		public String				TipoFactor;
		public java.sql.Date		FechaFactor;
		public BigDecimal			ValorFactor;
		public BigDecimal			MontoCompraFactor;		
		//solicitados por normativa, Mario Santander
		
		public String               IdRegimenFondoMutuo = "";
		public java.sql.Date		FechaOrigenCompra;
		public Long					IdCarga;
		public Long					IdSecuenciaCarteraMigracion;
		public String				CodigoPeriodo;
		public String				INGIdConcepto;
		public String				INGIdTipoOperacion;
		public String				INGGlosa;
		public BigDecimal			INGTasaAgente;
		public java.sql.Date		INGFechaOperacion;
		public String				INGIdCondicionLiquidacion;
		public java.sql.Date		INGFechaLiquidacion;
		public BigDecimal			INGCantidad;
		public BigDecimal			INGValorRescate;
		public BigDecimal			INGPrecioPromedioCompra;
		public BigDecimal			INGMontoPrecioPromedioCompra;
		public BigDecimal			INGMontoPrecioPromedioMonedaInstrumento;
		public BigDecimal			INGMontoPrecioPromedioMonedaAdicional;
		public BigDecimal			INGPrecioAPlazo;
		public BigDecimal			INGMontoAPlazo;
		public BigDecimal			INGMontoAPlazoMonedaInstrumento;
		public BigDecimal			INGMontoAPlazoMonedaAdicional;
		public String				INGEsPuntos;
		public BigDecimal			INGValorTipoCambioSpot;
		public BigDecimal			INGValorTipoCambioCotizador;
		public BigDecimal			INGTasaPuntosMonedaPrincipal;
		public BigDecimal			INGTasaPuntosMonedaSecundaria;
		public Long					INGTicketValorizacion;
		public String				INGValorizacionConProblemas;
		public java.sql.Timestamp	INGFechaValorizacionCartera;
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
		public BigDecimal			INGPrecioSucio;
		public BigDecimal			INGPrecioCompra;
		public BigDecimal			INGTasaCompra;
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
		public BigDecimal			INGMontoMonedaPrincipal;
		public BigDecimal			INGMontoMonedaSecundaria;
		public BigDecimal			INGMontoDiferido;
		public BigDecimal			INGMontoUtilidadDiferida;
		public BigDecimal			INGMontoPerdidaDiferida;
		public BigDecimal			INGMontoMargen;
		public BigDecimal			INGMontoValorRazonableActivo;
		public BigDecimal			INGMontoValorRazonablePasivo;
		public BigDecimal			INGMontoValorRazonableNeto;
		public BigDecimal			INGTasaValorRazonableActivo;
		public BigDecimal			INGTasaValorRazonablePasivo;
		public BigDecimal			INGMontoDevengoTipoCambio;
		public BigDecimal			INGMontoDevengoTipoCambioAnterior;
		public BigDecimal			INGMontoDiferidoDiario;
		public BigDecimal			INGMontoDiferidoAcumulado;
		public BigDecimal			INGMontoMatriz;
		//costoUF
		public BigDecimal			INGValorFactorAjusteaFechaCurse;
		public BigDecimal			INGCostoInversionEnFactorAjuste;
		//-------------------------------------------------------
		public Long					EGRTicketValorizacion;
		public String				EGRValorizacionConProblemas;
		public java.sql.Date		EGRFechaFactorMonedaInstrumento;
		public BigDecimal			EGRValorFactorMonedaInstrumento;
		public java.sql.Date		EGRFechaFactorMonedaAdicional;
		public BigDecimal			EGRValorFactorMonedaAdicional;
		public String				EGRGlosa;
		public java.sql.Date		EGRFechaOperacion;
		public String				EGRIdTipoOperacion;
		public Long					EGRIdOperacion;
		public Long					EGRIdMovimientoCarteraEnTransito;
		public String				EGRIdTipoCanalIngreso;
		public String				EGRIdCondicionLiquidacion;
		public java.sql.Date		EGRFechaLiquidacion;
		public String				EGRIdTipoComprobante;
		public Long					EGRFolioComprobante;
		public java.sql.Date		EGRFechaComprobante;
		public String				EGRFolioExternoReferencia;
		public String				EGRIdModulo;
		public String				EGRIdConcepto;
		public Long					EGRIdUsuario;
		public Long					EGRIdSucursal;
		public Long					EGRIdAgente;
		public java.sql.Date		EGRFechaCurseRetiro;
		public BigDecimal			EGRCostoRetiroenFactorAjuste;
		public BigDecimal			EGRValorFactorAjusteaFechaRetiro;
		public BigDecimal			EGRCantidad;
		public BigDecimal			EGRValorRescate;
		public BigDecimal			EGRPrecioSucio;
		public BigDecimal			EGRPrecio;
		public BigDecimal			EGRTasa;
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
		public Long					EGRIdSpotAsociado;
		public BigDecimal			EGRMontoMonedaPrincipal;
		public BigDecimal			EGRMontoMonedaSecundaria;
		public String				EGRIdMonedaLiquidacion;
		public BigDecimal			EGRMontoLiquidacion;
		public BigDecimal			EGRMontoLiquidacionMonedaPrincipal;
		public BigDecimal			EGRValorTipoCambioSpot;
		public BigDecimal			EGRValorParidad;
		public BigDecimal			EGRValorTipoCambioCotizador;
		public String				EGREsPuntos;
		public BigDecimal			EGRTasaPuntosMonedaPrincipal;
		public BigDecimal			EGRTasaPuntosMonedaSecundaria;
		public BigDecimal			EGRTasaDescuentoAnticipo;
		public BigDecimal			EGRTipoCambioAnticipo;
		public BigDecimal			EGRMontoARecibirAnticipo;
		public BigDecimal			EGRTipoCambioObservado;
		//costoUF
		public BigDecimal			EGRValorFactorAjusteaFechaCurse;
		public BigDecimal			EGRCostoInversionEnFactorAjuste;
		//MarcaCartera
		public String				EsPrimeraCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjTablaTransito {
		public Long					IdGenerarMovimientoCustodia;				//Requerido para relacionar movimientos en transito
		public Long					IdMovimientoCarteraEnTransito;
		public Long					IdMovimientoCarteraEnTransitoRelacionado;
		public java.sql.Timestamp	FechaIngresoRegistro;
		public java.sql.Timestamp	FechaCertificacionRegistro;
		public java.sql.Timestamp	FechaAnulacionRegistro;
		public Long					IdUsuarioIngreso;
		public Long					IdUsuarioCertificacion;
		public Long					IdUsuarioAnulacion;
		public java.sql.Date		FechaOperacion;
		public java.sql.Date		FechaOperacionOriginal;
		public java.sql.Date		FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;
		public Long					FolioCustodia;
		public String				IdTipoComprobante;
		public Long					FolioComprobante;
		public Long					IdMovimientoComprobante;
		public String				EsRegistroGeneradoPorSistema;
		public String				MueveCustodia;
		public String				PermiteCertificacionPorUsuario;
		public String				OrigenCustodia;
		public Long					IdCustodiaOrigen;
		public String				IdTipoOperacionOrigen;
		public Long					FolioCarteraGarantizada;
		public Long					IdInstrumentoCarteraGarantizada;
		public String				EsEspecifica;
		public Long					IdContraparteGarantia;
		public String				RazonSocialContraparteGarantia;
		public String				CuentaContraparteGarantia;
		public String				IdOrigenGarantia;
		public Long					IdEspecieGarantia;
		public Long					IdEmpresa;
		public Long					IdPais;
		public String				EsNominal;
		public String				FolioExternoReferencia;
		public Long					IdCliente;
		public Long					IdPortafolio;
		public Long					IdCuentaCustodioPortafolio;
		public Long					IdClienteRelacionado;						//----------------------------------------------------
		public Long					IdPortafolioRelacionado;					//----------------------------------------------------
		public Long					IdCuentaCustodioPortafolioRelacionado;		//----------------------------------------------------			
		public String				IdEstado;
		public String				IdTipoCanalIngreso;
		public String				IdMonedaSistema;
		public String				IdMonedaAdicional;
		public java.sql.Date		FechaFactorMonedaAdicional;
		public BigDecimal			ValorFactorMonedaAdicional;
		public String				CodigoIFRS;
		public String				IdConcepto;
		public String				IdTipoOperacion;
		public Long					IdOperacion;
		public Long					IdMesa;
		public String				IdBolsa;
		public Long					IdSucursal;
		public Long					IdAgente;
		public String				IdCondicionLiquidacion;
		public java.sql.Date		FechaLiquidacion;
		public String				EsIntraday;
		public String				UsaGarantiaEspecifica;
		public String				Glosa;
		public Long					IdUsuario;
		public String				IdModulo;
		public String				TipoCarteraCorredor;
		public String				TipoCarteraNormativa;
		public java.sql.Date		FechaCurse;
		public String				TipoFactorAjuste;
		public BigDecimal 			ValorFactorAjusteaFechaCurse;
		public BigDecimal 			CostoEnFactorAjuste;
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public Long					IdInstrumento;
		public Long					IdInstrumentoAnterior;
		public String				Nemotecnico;
		public String				IdEmisor;
		public String				NemoSVS;
		public String				IdTipoReajuste;
		public String				IdMonedaEmision;
		public java.sql.Date		FechaEmisionInstrumento;
		public java.sql.Date		FechaVencimientoInstrumento;
		public String				EsFungible;
		public String				ValorizaPor;
		public String				TipoValorizacion;
		public String				TieneCorteDeCupon;
		public String				IdMonedaInstrumento;
		public Long					Tamano;
		public String				IdBoveda;
		public String				CuentaBoveda;
		public String				ISIN;
		public java.sql.Date		FechaFactorMonedaInstrumento;
		public BigDecimal 			ValorFactorMonedaInstrumento;
		public BigDecimal	 		Cantidad;
		public BigDecimal 			CorteMinimo;
		public BigDecimal 			ValorRescate;
		public BigDecimal			PrecioAPlazo;
		public BigDecimal			PrecioSucio;
		public BigDecimal 			PrecioCompra;
		public BigDecimal 			TasaCompra;
		public BigDecimal 			MontoCompra;
		public BigDecimal 			MontoCompraMonedaInstrumento;
		public BigDecimal 			MontoCompraMonedaAdicional;
		public BigDecimal 			MontoAPlazo;
		public BigDecimal 			MontoAPlazoMonedaInstrumento;
		public BigDecimal 			MontoAPlazoMonedaAdicional;
		public Long					IdCarga;
		public Long					IdSecuenciaCarteraMigracion;
		public String 				CodigoPeriodo;
		public String 				EsVentaCorta;
		public String				IdTipoProducto;
		public String               IdRegimenFondoMutuo = "";
		public String               EsVentaDescubierta = "N";
		public java.sql.Date		FechaOrigenCompra;
		public Long                 IdCorredora;
		public BigDecimal			TasaTIR;// RF
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjTablaGarantia {
		public Long				IdGarantia;
		public Long				FolioGarantia;
		public String           ExigirGarantia;
		public Long				FolioCarteraOrigen;
		public Long				IdCarteraOrigen;
		public Long				FolioCarteraGarantizada;
		public Long				IdInstrumentoCarteraGarantizada;
		public String			EsEspecifica;
		public Long				IdContraparteGarantia;
		public String			RazonSocialContraparteGarantia;
		public String			CuentaContraparteGarantia;
		public String			IdOrigenGarantia;
		public Long				IdEspecieGarantia;
		public java.sql.Timestamp	FechaIngresoRegistro;
		public java.sql.Timestamp	FechaModificacionRegistro;
		public BigDecimal	 	PrecioCompraOperacionOrigen;
		public BigDecimal 		TasaCompraOperacionOrigen;
		public BigDecimal 		PrecioPromedioCompraOperacionOrigen;
		public BigDecimal 		MontoPrecioPromedioCompraOperacionOrigen;
		public BigDecimal 		MontoPrecioPromedioMonedaInstrumentoOperacionOrigen;
		public BigDecimal 		MontoPrecioPromedioMonedaAdicionalOperacionOrigen;
		public Long				IdEmpresa;
		public java.sql.Date	FechaOperacion;
		public java.sql.Date	FechaVencimientoOperacion;
		public Long				PlazoEnDiasOperacion;
		public Long				IdCliente;
		public Long				IdPortafolio;
		public Long				IdCuentaCustodioPortafolio;
		public String			IdEstado;
		public String			IdTipoCanalIngreso;
		public String			IdMonedaSistema;
		public String			IdMonedaAdicional;
		public java.sql.Date	FechaFactorMonedaAdicional;
		public BigDecimal 		ValorFactorMonedaAdicional;
		public String			IdConcepto;
		public String			IdTipoComprobante;
		public Long				FolioComprobante;
		public java.sql.Date	FechaComprobante;
		public String			FolioExternoReferencia;
		public Long				IdSucursal;
		public Long				IdAgente;
		public String			IdCondicionLiquidacion;
		public java.sql.Date	FechaLiquidacion;
		public Long				IdUsuario;
		public String			IdModulo;
		public Long				IdMovimientoCarteraEnTransito;
		public String			IdClasificacionTipoInstrumento;
		public String			IdTipoInstrumento;
		public Long				IdInstrumento;
		public Long				IdInstrumentoAnterior;
		public String			Nemotecnico;
		public String			IdEmisor;
		public String			NemoSVS;
		public String			IdTipoReajuste;
		public String			IdMonedaEmision;
		public java.sql.Date	FechaEmisionInstrumento;
		public java.sql.Date	FechaVencimientoInstrumento;
		public String			EsFungible;
		public String			ValorizaPor;
		public String			TipoValorizacion;
		public String			TieneCorteDeCupon;
		public String			CodigoIFRS;
		public String			IdMonedaInstrumento;
		public Long				Tamano;
		public String			TIFAfectoBeneficio;
		public BigDecimal 		TIFTasa;
		public String			ISIN;
		public java.sql.Date	FechaFactorMonedaInstrumento;
		public BigDecimal 		ValorFactorMonedaInstrumento;
		public BigDecimal 		Cantidad;
		public BigDecimal 		CorteMinimo;
		public BigDecimal 		ValorRescate;
		public BigDecimal 		PrecioGarantia;
		public BigDecimal 		TasaGarantia;
		public BigDecimal 		MontoGarantia;
		public BigDecimal 		MontoGarantiaMonedaInstrumento;
		public BigDecimal 		MontoGarantiaMonedaAdicional;
		public Long				IdCarga;
		public Long				IdSecuenciaCarteraMigracion;
		public String			CodigoPeriodo;
		public java.sql.Date	INGFechaOperacion;
		public String			INGIdCondicionLiquidacion;
		public java.sql.Date	INGFechaLiquidacion;
		public BigDecimal 		INGCantidad;
		public BigDecimal 		INGValorRescate;
		public Long				INGTicketValorizacion;
		public String			INGValorizacionConProblemas;
		public java.sql.Timestamp	INGFechaValorizacionGarantia;
		public java.sql.Date	INGFechaValorizacionAnterior;
		public java.sql.Date	INGFechaValorizacionActual;
		public java.sql.Date	INGFechaValorizacionProximo;
		public java.sql.Date	INGFechaFactorMonedaInstrumentoAnterior;
		public BigDecimal 		INGValorFactorMonedaInstrumentoAnterior;
		public java.sql.Date	INGFechaFactorMonedaInstrumentoActual;
		public BigDecimal 		INGValorFactorMonedaInstrumentoActual;
		public java.sql.Date	INGFechaFactorMonedaInstrumentoProximo;
		public BigDecimal 		INGValorFactorMonedaInstrumentoProximo;
		public java.sql.Date	INGFechaFactorMonedaAdicionalAnterior;
		public BigDecimal 		INGValorFactorMonedaAdicionalAnterior;
		public java.sql.Date	INGFechaFactorMonedaAdicionalActual;
		public BigDecimal 		INGValorFactorMonedaAdicionalActual;
		public java.sql.Date	INGFechaFactorMonedaAdicionalProximo;
		public BigDecimal 		INGValorFactorMonedaAdicionalProximo;
		public BigDecimal 		INGPrecioGarantia;
		public BigDecimal 		INGTasaGarantia;
		public BigDecimal 		INGMontoATasaGarantiaAnterior;
		public BigDecimal 		INGMontoInteresGarantiaAnterior;
		public BigDecimal 		INGMontoReajusteGarantiaAnterior;
		public BigDecimal 		INGMontoGarantiaAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoInteresGarantiaAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteGarantiaAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoGarantiaAnteriorMonedaAdicional;
		public BigDecimal 		INGMontoInteresGarantiaAnteriorMonedaAdicional;
		public BigDecimal 		INGMontoReajusteGarantiaAnteriorMonedaAdicional;
		public BigDecimal 		INGDurationGarantiaAnterior;
		public BigDecimal 		INGConvexidadGarantiaAnterior;
		public BigDecimal 		INGMontoGarantiaAnteriorPibMas;
		public BigDecimal 		INGMontoGarantiaAnteriorPibMasMonedaInstrumento;
		public BigDecimal 		INGMontoGarantiaAnteriorPibMasMonedaAdicional;
		public BigDecimal 		INGMontoGarantiaAnteriorPibMenos;
		public BigDecimal 		INGMontoGarantiaAnteriorPibMenosMonedaInstrumento;
		public BigDecimal 		INGMontoGarantiaAnteriorPibMenosMonedaAdicional;
		public BigDecimal 		INGMontoATasaGarantiaActual;
		public BigDecimal 		INGMontoGarantiaActualMonedaInstrumento;
		public BigDecimal 		INGMontoGarantiaActualMonedaAdicional;
		public BigDecimal 		INGDurationGarantiaActual;
		public BigDecimal 		INGConvexidadGarantiaActual;
		public BigDecimal 		INGMontoInteresCorridoGarantiaActual;
		public BigDecimal 		INGMontoReajusteCorridoGarantiaActual;
		public BigDecimal 		INGMontoInteresCorridoGarantiaActualMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteCorridoGarantiaActualMonedaInstrumento;
		public BigDecimal 		INGMontoInteresCorridoGarantiaActualMonedaAdicional;
		public BigDecimal 		INGMontoReajusteCorridoGarantiaActualMonedaAdicional;
		public BigDecimal 		INGMontoGarantiaActualPibMas;
		public BigDecimal 		INGMontoGarantiaActualPibMasMonedaInstrumento;
		public BigDecimal 		INGMontoGarantiaActualPibMasMonedaAdicional;
		public BigDecimal 		INGMontoGarantiaActualPibMenos;
		public BigDecimal 		INGMontoGarantiaActualPibMenosMonedaInstrumento;
		public BigDecimal 		INGMontoGarantiaActualPibMenosMonedaAdicional;
		public BigDecimal 		INGMontoATasaGarantiaProximo;
		public BigDecimal 		INGMontoInteresGarantiaProximo;
		public BigDecimal 		INGMontoReajusteGarantiaProximo;
		public BigDecimal 		INGMontoGarantiaProximoMonedaInstrumento;
		public BigDecimal 		INGMontoInteresGarantiaProximoMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteGarantiaProximoMonedaInstrumento;
		public BigDecimal 		INGMontoGarantiaProximoMonedaAdicional;
		public BigDecimal 		INGMontoInteresGarantiaProximoMonedaAdicional;
		public BigDecimal 		INGMontoReajusteGarantiaProximoMonedaAdicional;
		public BigDecimal 		INGDurationGarantiaProximo;
		public BigDecimal 		INGConvexidadGarantiaProximo;
		public java.sql.Date	INGFechaPrecioTasaMercadoAnterior;
		public String			INGFuenteTasaPrecioMercadoAnterior;
		public BigDecimal 		INGPrecioMercadoAnterior;
		public BigDecimal 		INGTasaMercadoAnterior;
		public BigDecimal 		INGMontoATasaMercadoAnterior;
		public BigDecimal 		INGMontoInteresMercadoAnterior;
		public BigDecimal 		INGMontoReajusteMercadoAnterior;
		public BigDecimal 		INGMontoMercadoAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoInteresMercadoAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteMercadoAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoMercadoAnteriorMonedaAdicional;
		public BigDecimal 		INGMontoInteresMercadoAnteriorMonedaAdicional;
		public BigDecimal 		INGMontoReajusteMercadoAnteriorMonedaAdicional;
		public BigDecimal 		INGDurationMercadoAnterior;
		public BigDecimal 		INGConvexidadMercadoAnterior;
		public BigDecimal 		INGMontoMercadoAnteriorPibMas;
		public BigDecimal 		INGMontoMercadoAnteriorPibMasMonedaInstrumento;
		public BigDecimal 		INGMontoMercadoAnteriorPibMasMonedaAdicional;
		public BigDecimal 		INGMontoMercadoAnteriorPibMenos;
		public BigDecimal 		INGMontoMercadoAnteriorPibMenosMonedaInstrumento;
		public BigDecimal 		INGMontoMercadoAnteriorPibMenosMonedaAdicional;
		public java.sql.Date	INGFechaPrecioTasaMercadoActual;
		public String			INGFuenteTasaPrecioMercadoActual;
		public BigDecimal 		INGPrecioMercadoActual;
		public BigDecimal 		INGTasaMercadoActual;
		public BigDecimal 		INGMontoATasaMercadoActual;
		public BigDecimal 		INGMontoMercadoActualMonedaInstrumento;
		public BigDecimal 		INGMontoMercadoActualMonedaAdicional;
		public BigDecimal 		INGDurationMercadoActual;
		public BigDecimal 		INGConvexidadMercadoActual;
		public BigDecimal 		INGMontoInteresCorridoMercadoActual;
		public BigDecimal 		INGMontoReajusteCorridoMercadoActual;
		public BigDecimal 		INGMontoInteresCorridoMercadoActualMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteCorridoMercadoActualMonedaInstrumento;
		public BigDecimal 		INGMontoInteresCorridoMercadoActualMonedaAdicional;
		public BigDecimal 		INGMontoReajusteCorridoMercadoActualMonedaAdicional;
		public BigDecimal 		INGMontoMercadoActualPibMas;
		public BigDecimal 		INGMontoMercadoActualPibMasMonedaInstrumento;
		public BigDecimal 		INGMontoMercadoActualPibMasMonedaAdicional;
		public BigDecimal 		INGMontoMercadoActualPibMenos;
		public BigDecimal 		INGMontoMercadoActualPibMenosMonedaInstrumento;
		public BigDecimal 		INGMontoMercadoActualPibMenosMonedaAdicional;
		public java.sql.Date	INGFechaPrecioTasaMercadoProximo;
		public String			INGFuenteTasaPrecioMercadoProximo;
		public BigDecimal 		INGPrecioMercadoProximo;
		public BigDecimal 		INGTasaMercadoProximo;
		public BigDecimal 		INGMontoATasaMercadoProximo;
		public BigDecimal 		INGMontoInteresMercadoProximo;
		public BigDecimal 		INGMontoReajusteMercadoProximo;
		public BigDecimal 		INGMontoMercadoProximoMonedaInstrumento;
		public BigDecimal 		INGMontoInteresMercadoProximoMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteMercadoProximoMonedaInstrumento;
		public BigDecimal 		INGMontoMercadoProximoMonedaAdicional;
		public BigDecimal 		INGMontoInteresMercadoProximoMonedaAdicional;
		public BigDecimal 		INGMontoReajusteMercadoProximoMonedaAdicional;
		public BigDecimal 		INGDurationMercadoProximo;
		public BigDecimal 		INGConvexidadMercadoProximo;
		public BigDecimal 		INGMontoATasaCompraAnterior;
		public BigDecimal 		INGMontoInteresCompraAnterior;
		public BigDecimal 		INGMontoReajusteCompraAnterior;
		public BigDecimal 		INGMontoCompraAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoInteresCompraAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteCompraAnteriorMonedaInstrumento;
		public BigDecimal 		INGMontoCompraAnteriorMonedaAdicional;
		public BigDecimal 		INGMontoInteresCompraAnteriorMonedaAdicional;
		public BigDecimal 		INGMontoReajusteCompraAnteriorMonedaAdicional;
		public BigDecimal 		INGDurationCompraAnterior;
		public BigDecimal 		INGConvexidadCompraAnterior;
		public BigDecimal 		INGMontoCompraAnteriorPibMas;
		public BigDecimal 		INGMontoCompraAnteriorPibMasMonedaInstrumento;
		public BigDecimal 		INGMontoCompraAnteriorPibMasMonedaAdicional;
		public BigDecimal 		INGMontoCompraAnteriorPibMenos;
		public BigDecimal 		INGMontoCompraAnteriorPibMenosMonedaInstrumento;
		public BigDecimal 		INGMontoCompraAnteriorPibMenosMonedaAdicional;
		public BigDecimal 		INGMontoATasaCompraActual;
		public BigDecimal 		INGMontoCompraActualMonedaInstrumento;
		public BigDecimal 		INGMontoCompraActualMonedaAdicional;
		public BigDecimal 		INGDurationCompraActual;
		public BigDecimal 		INGConvexidadCompraActual;
		public BigDecimal 		INGMontoInteresCorridoCompraActual;
		public BigDecimal 		INGMontoReajusteCorridoCompraActual;
		public BigDecimal 		INGMontoInteresCorridoCompraActualMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteCorridoCompraActualMonedaInstrumento;
		public BigDecimal 		INGMontoInteresCorridoCompraActualMonedaAdicional;
		public BigDecimal 		INGMontoReajusteCorridoCompraActualMonedaAdicional;
		public BigDecimal 		INGMontoCompraActualPibMas;
		public BigDecimal 		INGMontoCompraActualPibMasMonedaInstrumento;
		public BigDecimal 		INGMontoCompraActualPibMasMonedaAdicional;
		public BigDecimal 		INGMontoCompraActualPibMenos;
		public BigDecimal 		INGMontoCompraActualPibMenosMonedaInstrumento;
		public BigDecimal 		INGMontoCompraActualPibMenosMonedaAdicional;
		public BigDecimal 		INGMontoATasaCompraProximo;
		public BigDecimal 		INGMontoInteresCompraProximo;
		public BigDecimal 		INGMontoReajusteCompraProximo;
		public BigDecimal 		INGMontoCompraProximoMonedaInstrumento;
		public BigDecimal 		INGMontoInteresCompraProximoMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteCompraProximoMonedaInstrumento;
		public BigDecimal 		INGMontoCompraProximoMonedaAdicional;
		public BigDecimal 		INGMontoInteresCompraProximoMonedaAdicional;
		public BigDecimal 		INGMontoReajusteCompraProximoMonedaAdicional;
		public BigDecimal 		INGDurationCompraProximo;
		public BigDecimal 		INGConvexidadCompraProximo;
		public BigDecimal 		INGTasaCorredora;
		public java.sql.Date	INGFechaTasaCorredora;
		public BigDecimal 		INGMontoATasaCorredora;
		public BigDecimal 		INGMontoATasaCorredoraMonedaInstrumento;
		public BigDecimal 		INGMontoATasaCorredoraMonedaAdicional;
		public java.sql.Date	INGFechaCorteCupon;
		public Long 			INGNumeroCupon;
		public String 			INGIdMonedaPagoCupon;
		public BigDecimal 		INGMontoCorteCupon;
		public java.sql.Date	INGFechaPagoCorteCupon;
		public BigDecimal 		INGMontoCapitalCorteCupon;
		public BigDecimal 		INGMontoInteresCorteCupon;
		public BigDecimal 		INGMontoReajusteCorteCupon;
		public BigDecimal 		INGMontoCorteCuponMonedaInstrumento;
		public BigDecimal 		INGMontoCapitalCorteCuponMonedaInstrumento;
		public BigDecimal 		INGMontoInteresCorteCuponMonedaInstrumento;
		public BigDecimal 		INGMontoReajusteCorteCuponMonedaInstrumento;
		public BigDecimal 		INGMontoCorteCuponMonedaAdicional;
		public BigDecimal 		INGMontoCapitalCorteCuponMonedaAdicional;
		public BigDecimal 		INGMontoInteresCorteCuponMonedaAdicional;
		public BigDecimal 		INGMontoReajusteCorteCuponMonedaAdicional;
		public Long				EGRTicketValorizacion;
		public String 			EGRValorizacionConProblemas;
		public java.sql.Date	EGRFechaFactorMonedaInstrumento;
		public BigDecimal 		EGRValorFactorMonedaInstrumento;
		public java.sql.Date	EGRFechaFactorMonedaAdicional;
		public BigDecimal 		EGRValorFactorMonedaAdicional;
		public java.sql.Date	EGRFechaOperacion;
		public String			EGRIdTipoOperacion;
		public Long				EGRIdOperacion;
		public Long				EGRIdMovimientoCarteraEnTransito;
		public String			EGRIdTipoCanalIngreso;
		public String			EGRIdCondicionLiquidacion;
		public java.sql.Date	EGRFechaLiquidacion;
		public String			EGRIdTipoComprobante;
		public Long				EGRFolioComprobante;
		public java.sql.Date	EGRFechaComprobante;
		public String			EGRFolioExternoReferencia;
		public String			EGRIdModulo;
		public String			EGRIdConcepto;
		public Long				EGRIdUsuario;
		public Long				EGRIdSucursal;
		public Long				EGRIdAgente;
		public BigDecimal 		EGRCantidad;
		public BigDecimal 		EGRValorRescate;
		public BigDecimal 		EGRPrecio;
		public BigDecimal 		EGRTasa;
		public BigDecimal 		EGRMonto;
		public BigDecimal 		EGRMontoMonedaInstrumento;
		public BigDecimal 		EGRMontoMonedaAdicional;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjTablaSubyacente {
		public Long				IdSubyacente;
		public Long				FolioSubyacente;
		public Long				FolioCarteraRelacionada;
		public java.sql.Timestamp	FechaIngresoRegistro;
		public java.sql.Timestamp	FechaModificacionRegistro;
		public Long				IdCarteraRetirada;
		public Long				IdMovimientoCarteraEnTransito;
		public BigDecimal		PrecioCompraOperacionRelacionada;
		public BigDecimal		TasaCompraOperacionRelacionada;
		public BigDecimal		PrecioPromedioCompraOperacionRelacionada;
		public BigDecimal		MontoPrecioPromedioCompraOperacionRelacionada;
		public BigDecimal		MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada;
		public BigDecimal		MontoPrecioPromedioMonedaAdicionalOperacionRelacionada;
		public String			IdEstado;
		public String			IdTipoCanalIngreso;
		public String			IdClasificacionTipoInstrumento;
		public String			IdTipoInstrumento;
		public Long				IdInstrumento;
		public Long				IdInstrumentoAnterior;
		public String			Nemotecnico;
		public String			IdEmisor;
		public String			NemoSVS;
		public String			IdTipoReajuste;
		public String			IdMonedaEmision;
		public java.sql.Date	FechaEmisionInstrumento;
		public java.sql.Date	FechaVencimientoInstrumento;
		public String			EsFungible;
		public String			ValorizaPor;
		public String			TipoValorizacion;
		public String			TieneCorteDeCupon;
		public String			IdMonedaInstrumento;
		public Long				Tamano;
		public String			TIFAfectoBeneficio;
		public BigDecimal		TIFTasa;
		public String			ISIN;
		public java.sql.Date	FechaFactorMonedaInstrumento;
		public BigDecimal		ValorFactorMonedaInstrumento;
		public BigDecimal		Cantidad;
		public BigDecimal		CorteMinimo;
		public BigDecimal		ValorRescate;
		public BigDecimal		PrecioSubyacente;
		public BigDecimal		TasaSubyacente;
		public BigDecimal		MontoSubyacente;
		public BigDecimal		MontoSubyacenteMonedaInstrumento;
		public BigDecimal		MontoSubyacenteMonedaAdicional;
		public Long				IdCarga;
		public java.sql.Date	INGFechaOperacion;
		public BigDecimal		INGCantidad;
		public BigDecimal		INGValorRescate;
		public Long				INGTicketValorizacion;
		public String			INGValorizacionConProblemas;
		public java.sql.Timestamp	INGFechaValorizacionSubyacente;
		public java.sql.Date	INGFechaValorizacionAnterior;
		public java.sql.Date	INGFechaValorizacionActual;
		public java.sql.Date	INGFechaValorizacionProximo;
		public java.sql.Date	INGFechaFactorMonedaInstrumentoAnterior;
		public BigDecimal		INGValorFactorMonedaInstrumentoAnterior;
		public java.sql.Date	INGFechaFactorMonedaInstrumentoActual;
		public BigDecimal		INGValorFactorMonedaInstrumentoActual;
		public java.sql.Date	INGFechaFactorMonedaInstrumentoProximo;
		public BigDecimal		INGValorFactorMonedaInstrumentoProximo;
		public java.sql.Date	INGFechaFactorMonedaAdicionalAnterior;
		public BigDecimal		INGValorFactorMonedaAdicionalAnterior;
		public java.sql.Date	INGFechaFactorMonedaAdicionalActual;
		public BigDecimal		INGValorFactorMonedaAdicionalActual;
		public java.sql.Date	INGFechaFactorMonedaAdicionalProximo;
		public BigDecimal		INGValorFactorMonedaAdicionalProximo;
		public BigDecimal		INGPrecioSubyacente;
		public BigDecimal		INGTasaSubyacente;
		public BigDecimal		INGMontoATasaSubyacenteAnterior;
		public BigDecimal		INGMontoInteresSubyacenteAnterior;
		public BigDecimal		INGMontoReajusteSubyacenteAnterior;
		public BigDecimal		INGMontoSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoInteresSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoReajusteSubyacenteAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoSubyacenteAnteriorMonedaAdicional;
		public BigDecimal		INGMontoInteresSubyacenteAnteriorMonedaAdicional;
		public BigDecimal		INGMontoReajusteSubyacenteAnteriorMonedaAdicional;
		public BigDecimal		INGDurationSubyacenteAnterior;
		public BigDecimal		INGConvexidadSubyacenteAnterior;
		public BigDecimal		INGMontoSubyacenteAnteriorPibMas;
		public BigDecimal		INGMontoSubyacenteAnteriorPibMasMonedaInstrumento;
		public BigDecimal		INGMontoSubyacenteAnteriorPibMasMonedaAdicional;
		public BigDecimal		INGMontoSubyacenteAnteriorPibMenos;
		public BigDecimal		INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento;
		public BigDecimal		INGMontoSubyacenteAnteriorPibMenosMonedaAdicional;
		public BigDecimal		INGMontoATasaSubyacenteActual;
		public BigDecimal		INGMontoSubyacenteActualMonedaInstrumento;
		public BigDecimal		INGMontoSubyacenteActualMonedaAdicional;
		public BigDecimal		INGDurationSubyacenteActual;
		public BigDecimal		INGConvexidadSubyacenteActual;
		public BigDecimal		INGMontoInteresCorridoSubyacenteActual;
		public BigDecimal		INGMontoReajusteCorridoSubyacenteActual;
		public BigDecimal		INGMontoInteresCorridoSubyacenteActualMonedaInstrumento;
		public BigDecimal		INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento;
		public BigDecimal		INGMontoInteresCorridoSubyacenteActualMonedaAdicional;
		public BigDecimal		INGMontoReajusteCorridoSubyacenteActualMonedaAdicional;
		public BigDecimal		INGMontoSubyacenteActualPibMas;
		public BigDecimal		INGMontoSubyacenteActualPibMasMonedaInstrumento;
		public BigDecimal		INGMontoSubyacenteActualPibMasMonedaAdicional;
		public BigDecimal		INGMontoSubyacenteActualPibMenos;
		public BigDecimal		INGMontoSubyacenteActualPibMenosMonedaInstrumento;
		public BigDecimal		INGMontoSubyacenteActualPibMenosMonedaAdicional;
		public BigDecimal		INGMontoATasaSubyacenteProximo;
		public BigDecimal		INGMontoInteresSubyacenteProximo;
		public BigDecimal		INGMontoReajusteSubyacenteProximo;
		public BigDecimal		INGMontoSubyacenteProximoMonedaInstrumento;
		public BigDecimal		INGMontoInteresSubyacenteProximoMonedaInstrumento;
		public BigDecimal		INGMontoReajusteSubyacenteProximoMonedaInstrumento;
		public BigDecimal		INGMontoSubyacenteProximoMonedaAdicional;
		public BigDecimal		INGMontoInteresSubyacenteProximoMonedaAdicional;
		public BigDecimal		INGMontoReajusteSubyacenteProximoMonedaAdicional;
		public BigDecimal		INGDurationSubyacenteProximo;
		public BigDecimal		INGConvexidadSubyacenteProximo;
		public java.sql.Date	INGFechaPrecioTasaMercadoAnterior;
		public String			INGFuenteTasaPrecioMercadoAnterior;
		public BigDecimal		INGPrecioMercadoAnterior;
		public BigDecimal		INGTasaMercadoAnterior;
		public BigDecimal		INGMontoATasaMercadoAnterior;
		public BigDecimal		INGMontoInteresMercadoAnterior;
		public BigDecimal		INGMontoReajusteMercadoAnterior;
		public BigDecimal		INGMontoMercadoAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoInteresMercadoAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoReajusteMercadoAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoMercadoAnteriorMonedaAdicional;
		public BigDecimal		INGMontoInteresMercadoAnteriorMonedaAdicional;
		public BigDecimal		INGMontoReajusteMercadoAnteriorMonedaAdicional;
		public BigDecimal		INGDurationMercadoAnterior;
		public BigDecimal		INGConvexidadMercadoAnterior;
		public BigDecimal		INGMontoMercadoAnteriorPibMas;
		public BigDecimal		INGMontoMercadoAnteriorPibMasMonedaInstrumento;
		public BigDecimal		INGMontoMercadoAnteriorPibMasMonedaAdicional;
		public BigDecimal		INGMontoMercadoAnteriorPibMenos;
		public BigDecimal		INGMontoMercadoAnteriorPibMenosMonedaInstrumento;
		public BigDecimal		INGMontoMercadoAnteriorPibMenosMonedaAdicional;
		public java.sql.Date	INGFechaPrecioTasaMercadoActual;
		public String			INGFuenteTasaPrecioMercadoActual;
		public BigDecimal		INGPrecioMercadoActual;
		public BigDecimal		INGTasaMercadoActual;
		public BigDecimal		INGMontoATasaMercadoActual;
		public BigDecimal		INGMontoMercadoActualMonedaInstrumento;
		public BigDecimal		INGMontoMercadoActualMonedaAdicional;
		public BigDecimal		INGDurationMercadoActual;
		public BigDecimal		INGConvexidadMercadoActual;
		public BigDecimal		INGMontoInteresCorridoMercadoActual;
		public BigDecimal		INGMontoReajusteCorridoMercadoActual;
		public BigDecimal		INGMontoInteresCorridoMercadoActualMonedaInstrumento;
		public BigDecimal		INGMontoReajusteCorridoMercadoActualMonedaInstrumento;
		public BigDecimal		INGMontoInteresCorridoMercadoActualMonedaAdicional;
		public BigDecimal		INGMontoReajusteCorridoMercadoActualMonedaAdicional;
		public BigDecimal		INGMontoMercadoActualPibMas;
		public BigDecimal		INGMontoMercadoActualPibMasMonedaInstrumento;
		public BigDecimal		INGMontoMercadoActualPibMasMonedaAdicional;
		public BigDecimal		INGMontoMercadoActualPibMenos;
		public BigDecimal		INGMontoMercadoActualPibMenosMonedaInstrumento;
		public BigDecimal		INGMontoMercadoActualPibMenosMonedaAdicional;
		public java.sql.Date	INGFechaPrecioTasaMercadoProximo;
		public String			INGFuenteTasaPrecioMercadoProximo;
		public BigDecimal		INGPrecioMercadoProximo;
		public BigDecimal		INGTasaMercadoProximo;
		public BigDecimal		INGMontoATasaMercadoProximo;
		public BigDecimal		INGMontoInteresMercadoProximo;
		public BigDecimal		INGMontoReajusteMercadoProximo;
		public BigDecimal		INGMontoMercadoProximoMonedaInstrumento;
		public BigDecimal		INGMontoInteresMercadoProximoMonedaInstrumento;
		public BigDecimal		INGMontoReajusteMercadoProximoMonedaInstrumento;
		public BigDecimal		INGMontoMercadoProximoMonedaAdicional;
		public BigDecimal		INGMontoInteresMercadoProximoMonedaAdicional;
		public BigDecimal		INGMontoReajusteMercadoProximoMonedaAdicional;
		public BigDecimal		INGDurationMercadoProximo;
		public BigDecimal		INGConvexidadMercadoProximo;
		public BigDecimal		INGMontoATasaCompraAnterior;
		public BigDecimal		INGMontoInteresCompraAnterior;
		public BigDecimal		INGMontoReajusteCompraAnterior;
		public BigDecimal		INGMontoCompraAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoInteresCompraAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoReajusteCompraAnteriorMonedaInstrumento;
		public BigDecimal		INGMontoCompraAnteriorMonedaAdicional;
		public BigDecimal		INGMontoInteresCompraAnteriorMonedaAdicional;
		public BigDecimal		INGMontoReajusteCompraAnteriorMonedaAdicional;
		public BigDecimal		INGDurationCompraAnterior;
		public BigDecimal		INGConvexidadCompraAnterior;
		public BigDecimal		INGMontoCompraAnteriorPibMas;
		public BigDecimal		INGMontoCompraAnteriorPibMasMonedaInstrumento;
		public BigDecimal		INGMontoCompraAnteriorPibMasMonedaAdicional;
		public BigDecimal		INGMontoCompraAnteriorPibMenos;
		public BigDecimal		INGMontoCompraAnteriorPibMenosMonedaInstrumento;
		public BigDecimal		INGMontoCompraAnteriorPibMenosMonedaAdicional;
		public BigDecimal		INGMontoATasaCompraActual;
		public BigDecimal		INGMontoCompraActualMonedaInstrumento;
		public BigDecimal		INGMontoCompraActualMonedaAdicional;
		public BigDecimal		INGDurationCompraActual;
		public BigDecimal		INGConvexidadCompraActual;
		public BigDecimal		INGMontoInteresCorridoCompraActual;
		public BigDecimal		INGMontoReajusteCorridoCompraActual;
		public BigDecimal		INGMontoInteresCorridoCompraActualMonedaInstrumento;
		public BigDecimal		INGMontoReajusteCorridoCompraActualMonedaInstrumento;
		public BigDecimal		INGMontoInteresCorridoCompraActualMonedaAdicional;
		public BigDecimal		INGMontoReajusteCorridoCompraActualMonedaAdicional;
		public BigDecimal		INGMontoCompraActualPibMas;
		public BigDecimal		INGMontoCompraActualPibMasMonedaInstrumento;
		public BigDecimal		INGMontoCompraActualPibMasMonedaAdicional;
		public BigDecimal		INGMontoCompraActualPibMenos;
		public BigDecimal		INGMontoCompraActualPibMenosMonedaInstrumento;
		public BigDecimal		INGMontoCompraActualPibMenosMonedaAdicional;
		public BigDecimal		INGMontoATasaCompraProximo;
		public BigDecimal		INGMontoInteresCompraProximo;
		public BigDecimal		INGMontoReajusteCompraProximo;
		public BigDecimal		INGMontoCompraProximoMonedaInstrumento;
		public BigDecimal		INGMontoInteresCompraProximoMonedaInstrumento;
		public BigDecimal		INGMontoReajusteCompraProximoMonedaInstrumento;
		public BigDecimal		INGMontoCompraProximoMonedaAdicional;
		public BigDecimal		INGMontoInteresCompraProximoMonedaAdicional;
		public BigDecimal		INGMontoReajusteCompraProximoMonedaAdicional;
		public BigDecimal		INGDurationCompraProximo;
		public BigDecimal		INGConvexidadCompraProximo;
		public BigDecimal		INGTasaCorredora;
		public java.sql.Date	INGFechaTasaCorredora;
		public BigDecimal		INGMontoATasaCorredora;
		public BigDecimal		INGMontoATasaCorredoraMonedaInstrumento;
		public BigDecimal		INGMontoATasaCorredoraMonedaAdicional;
		public java.sql.Date	INGFechaCorteCupon;
		public Long				INGNumeroCupon;
		public String			INGIdMonedaPagoCupon;
		public BigDecimal		INGMontoCorteCupon;
		public java.sql.Date	INGFechaPagoCorteCupon;
		public BigDecimal		INGMontoCapitalCorteCupon;
		public BigDecimal		INGMontoInteresCorteCupon;
		public BigDecimal		INGMontoReajusteCorteCupon;
		public BigDecimal		INGMontoCorteCuponMonedaInstrumento;
		public BigDecimal		INGMontoCapitalCorteCuponMonedaInstrumento;
		public BigDecimal		INGMontoInteresCorteCuponMonedaInstrumento;
		public BigDecimal		INGMontoReajusteCorteCuponMonedaInstrumento;
		public BigDecimal		INGMontoCorteCuponMonedaAdicional;
		public BigDecimal		INGMontoCapitalCorteCuponMonedaAdicional;
		public BigDecimal		INGMontoInteresCorteCuponMonedaAdicional;
		public BigDecimal		INGMontoReajusteCorteCuponMonedaAdicional;
		public BigDecimal		INGMontoPresenteTIF;
		public BigDecimal		INGMontoInteresTIF;
		public BigDecimal		INGMontoReajusteTIF;
		public BigDecimal		INGMontoInteresAcumTIF;
		public BigDecimal		INGMontoReajusteAcumTIF;
		public BigDecimal		INGMontoPresenteTIFMonedaInstrumento;
		public BigDecimal		INGMontoInteresTIFMonedaInstrumento;
		public BigDecimal		INGMontoReajusteTIFMonedaInstrumento;
		public BigDecimal		INGMontoInteresAcumTIFMonedaInstrumento;
		public BigDecimal		INGMontoReajusteAcumTIFMonedaInstrumento;
		public BigDecimal		INGMontoPresenteTIFMonedaAdicional;
		public BigDecimal		INGMontoInteresTIFMonedaAdicional;
		public BigDecimal		INGMontoReajusteTIFMonedaAdicional;
		public BigDecimal		INGMontoInteresAcumTIFMonedaAdicional;
		public BigDecimal		INGMontoReajusteAcumTIFMonedaAdicional;
		public Long				EGRTicketValorizacion;
		public String			EGRValorizacionConProblemas;
		public java.sql.Date	EGRFechaFactorMonedaInstrumento;
		public BigDecimal		EGRValorFactorMonedaInstrumento;
		public java.sql.Date	EGRFechaFactorMonedaAdicional;
		public BigDecimal		EGRValorFactorMonedaAdicional;
		public java.sql.Date	EGRFechaOperacion;
		public String			EGRIdTipoCanalIngreso;
		public String			EGRIdCondicionLiquidacion;
		public java.sql.Date	EGRFechaLiquidacion;
		public String			EGRIdTipoComprobante;      
		public Long				EGRFolioComprobante;
		public java.sql.Date	EGRFechaComprobante;
		public String			EGRFolioExternoReferencia;
		public String			EGRIdModulo;              
		public String			EGRIdTipoOperacion;       
		public Long				EGRIdOperacion;
		public String			EGRIdConcepto;
		public Long				EGRIdUsuario;
		public Long				EGRIdSucursal;
		public Long				EGRIdAgente;
		public BigDecimal		EGRCantidad;
		public BigDecimal		EGRValorRescate;
		public BigDecimal		EGRPrecio;
		public BigDecimal		EGRTasa;
		public BigDecimal		EGRMonto;
		public BigDecimal		EGRMontoMonedaInstrumento;
		public BigDecimal		EGRMontoMonedaAdicional;
		public BigDecimal		EGRSpread;
		public java.sql.Date	EGRFechaPrecioTasaMercado;
		public String			EGRFuenteTasaPrecioMercado;
		public BigDecimal		EGRPrecioMercado;
		public BigDecimal		EGRTasaMercado;
		public BigDecimal		EGRMontoATasaMercado;
		public BigDecimal		EGRMontoMercadoMonedaInstrumento;
		public BigDecimal		EGRMontoMercadoMonedaAdicional;
		public BigDecimal		EGRSpreadMercado;
		public BigDecimal		EGRMontoPresenteTIF;
		public BigDecimal		EGRMontoPresenteTIFMonedaInstrumento;
		public BigDecimal		EGRMontoPresenteTIFMonedaAdicional;
		
		public String			IdTipoOperacion;
		public java.sql.Date	FechaVencimientoOperacion;
		public String 			EsPrimeraCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjDatosEntrada {
		public Long					IdGenerarMovimientoCustodia;	//ID Interno del proceso
		public String				TablaAfectada; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
		public String 				EsRegistroNuevo;				//S o N
		public String 			    FolioSistemaExterno;
		public String               TipoCustodia;
		public String 			    EstadoAnticipo;
		public String               ExigirGarantia;
		public String 				EsCustodiaExterna;
		public String				IdEstado;

		
		//////////////////////////	SALDOS CARTERAS
		public Long					IdCustodia;						//Cuando es registro de saldo  - en caso de registro en tabla transito indica la informacion de la custodia a retirar
		public Long					FolioCustodia;					//Cuando es resgitro de saldo
		public String				GenerarNuevoFolioCustodia = "N";//S o N ,Cuando se le quiere asignar forsosamente un nuevo folio
		public String				MantieneFolioCustodia = "N";	//S o N ,Cuando es registro nuevo pero debe mantener el folio
		
		//////////////////////////	INFORMACION DE DEPENDENCIAS ENTRE TABLAS
		public Long					IdCarteraOrigen;
		public Long					FolioCarteraRelacionada;
		public Long					FolioCarteraOrigen;		
		public Long					IdGarantiaOrigen;
		public Long					FolioGarantiaOrigen;
		public Long					IdMovimientoCarteraEnTransitoOrigen;
		public Long					IdMovimientoCarteraEnTransitoRelacionado;
		public String				MueveCarteraEnTransito;	          //Indicador de si se genera la cartera al certificar el movimiento en transito

		
		/////////////////////////// OPERACION GESTORA		
		public String				IdTipoOperacionGestora;			//Operacion con subyacente
		public Long					IdCustodiaGestor;				//Id de operacion con subyacente
		
		/////////////////////////// OPERACION
		public String				AccionOperacion;				//INGRESO - CERTIFICACION		
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public Long					IdCuentaCustodioPortafolio;
		public Long					IdClienteRelacionado; 					//----------------------------------------------------
		public Long					IdPortafolioRelacionado; 				//----------------------------------------------------
		public Long					IdCuentaCustodioPortafolioRelacionado;	//----------------------------------------------------	
		public java.sql.Date		FechaOperacion;
		public java.sql.Date		FechaOperacionOriginal;
		public java.sql.Date		FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;
		public String				IdTipoOperacion;		
		public Long					IdGrupoOperacion; 
		public Long					IdOperacion;
		public String				IdConcepto;
		public Long					FolioOperacionRelacionada;
		public String				IdCondicionLiquidacion; 
		public java.sql.Date		FechaLiquidacion;		
		public String				IdTipoProducto; 
		public String				IdTipoCanalIngreso; 		
		public String				CodigoIFRS;		 		 
		public String				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public java.sql.Date		FechaComprobante; 
		public Long 				IdMovimientoComprobante; //migita de pan para los transitos
		public String				FolioExternoReferencia; 
		
		public Long					IdMesa; 
		public String				IdBolsa;
		public String				EsIntraday; 
		public String				TipoCarteraCorredor;
		public String				UsaGarantiaEspecifica;
		public String				CobraPrimaAlInicio; 
		public String				IdTipoPrestamo;		
		public Long					IdUsuario;
		public Long					IdSucursal; 
		public Long					IdAgente; 
		public String				Glosa;
		public String				PermiteCertificacionPorUsuario;		//Mov en transito
		public String				IdMonedaAdicional = null;
		/////////////////////////// GARANTIAS
		public Long					FolioCarteraGarantizada; 
		public Long					IdInstrumentoCarteraGarantizada; 		
		public Long					IdContraparteGarantia; 
		public String				RazonSocialContraparteGarantia; 
		public String				CuentaContraparteGarantia; 
		public String				IdOrigenGarantia;
		public Long					IdEspecieGarantia;
		public String				EsEspecifica;
		
		/////////////////////////// INSTRUMENTO
		public Long					IdInstrumento;
		public String				Nemotecnico;
		public Long					IdInstrumentoAnterior;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmision;
		public String				IdTipoReajusteInstrumento;
		public String				IdEmisor;
		public String				EsFungible;
		public String				IdBoveda;		
		public String				CuentaBoveda;			//????
		public String				EsNominal;
		public String				EsVentaDescubierta;   
		public java.sql.Date		FechaVencimientoInstrumento;		//SOLO PARA IF
		public java.sql.Date		FechaEmisionInstrumento;			//SOLO PARA IF
		public String 				NemoSVS; 							//SOLO PARA IF
		/////////////////////////// PRESTAMO
		public Long                 IdInstrumentoSubyacente;
		public String 				NemotecnicoSubyacente;
		public String				IdTipoInstrumentoSubyacente;
		public BigDecimal			TasaCompraInstrumento;
		public BigDecimal			TasaCompraOperacionRelacionada;
		
		/////////////////////////// CANTIDADES Y MONTOS		 
		public BigDecimal			Cantidad; 
		public BigDecimal			CorteMinimo; 
		public BigDecimal			ValorRescate;
		public BigDecimal			PrecioSucio;
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra;
		public BigDecimal			MontoCompra; 
		public BigDecimal			MontoCompraMonedaInstrumento; 
		public BigDecimal			TasaAgente; 
		public BigDecimal			PrecioAPlazo;
		public BigDecimal			MontoAPlazo; 
		public BigDecimal			MontoAPlazoMonedaInstrumento; 
		
		/////////////////////////// FONDOS MUTUOS
		public String				TipoCarteraNormativa; 
		public java.sql.Date		FechaCurse; 
		public String				TipoFactorAjuste; 
		public BigDecimal			ValorFactorAjusteaFechaCurse; 
		public BigDecimal			CostoEnFactorAjuste;
		public String				IdRegimenFondoMutuo;
		public java.sql.Date		FechaOrigenCompra;
		//rentabilidad
		public BigDecimal			ValorFactor;
		public BigDecimal			MontoCompraFactor;
		/////////////////////////// FORWARD
		public Long					IdSpotAsociado; 
		public Long					IdModalidadCumplimiento; 
		public BigDecimal			MontoMonedaPrincipal; 
		public BigDecimal			MontoMonedaSecundaria; 
		public String				IdMonedaLiquidacion; 
		public BigDecimal			MontoLiquidacion; 
		public BigDecimal			MontoLiquidacionMonedaPrincipal; 
		public BigDecimal			MontoDiferido; 
		public BigDecimal			MontoUtilidadDiferida; 
		public BigDecimal			MontoPerdidaDiferida; 
		public BigDecimal			MontoMargen; 
		public BigDecimal			MontoValorRazonableActivo; 
		public BigDecimal			MontoValorRazonablePasivo; 
		public BigDecimal			MontoValorRazonableNeto; 
		public BigDecimal			TasaValorRazonableActivo; 
		public BigDecimal			TasaValorRazonablePasivo; 
		public BigDecimal			MontoDevengoTipoCambio; 
		public BigDecimal			MontoDevengoTipoCambioAnterior; 
		public BigDecimal			MontoDiferidoDiario; 
		public BigDecimal			MontoDiferidoAcumulado; 
		public BigDecimal			ValorTipoCambioSpot; 
		public BigDecimal			ValorTipoCambioCotizador; 
		public String				EsPuntos; 
		public BigDecimal			TasaPuntosMonedaPrincipal; 
		public BigDecimal			TasaPuntosMonedaSecundaria; 
		public BigDecimal			MontoMatriz; 
		public BigDecimal			TasaDescuentoAnticipo; 
		public BigDecimal			TipoCambioAnticipo; 
		public BigDecimal			MontoARecibirAnticipo; 
		public BigDecimal			TipoCambioObservado;		
		public java.sql.Date		FechaEfectiva; // RF
		public Long					IdPlaza; // RF
		public Long					IdFixing; // RF
		public java.sql.Date		FechaFixing; // RF
		public java.sql.Time		HoraFixing; // RF
		public Long					IdReferenciaMercado; // RF
		public String				IdMonedaPrincipal; // RF
		public String				IdMonedaSecundaria; // RF
		public BigDecimal			ValorTipoCambioCierre; // RF
		public BigDecimal			ValorParidadCierre; // RF
		public BigDecimal			ValorTipoCambioCosto; // RF
		public BigDecimal			ValorParidadCosto;// RF
		public BigDecimal			TasaTIR;// RF
		
		/////////////////////////// CARGA DE DATOS (EJEM: CARGA ARCHIVO)
		public Long					IdCarga; 
		public Long					IdSecuenciaCarteraMigracion; 
		public String				CodigoPeriodo;
		public String 				EsVentaCorta;
		
		/////////////////////////// AUTORIZACION
		public String               EsPendienteAutorizacion = "N";
		public Long                 IdCorredora;
		
		//COMPLETANDO DATOS
		public String				FolioTransaccion;
		public Date					FechaTransaccion;
		
		//Circular 514             
		public String               UsaTasaRecompraPacto;
		public Long					IdMovimientoEnTransito = 0L;
		
		public ObjDatosEntrada() {
			this.IdRegimenFondoMutuo = "";
			this.MueveCarteraEnTransito="S" ;
			this.UsaTasaRecompraPacto="N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresoCustodiaEntrada {
	    public Long						p_IdEmpresa;
	    public String					p_IdModulo;
	    public Long						p_IdUsuario = 0L;
	    public String					p_SimularOperacion="N";
	    public Long						p_TicketAtencion;
	    public List<ObjDatosEntrada>	p_DatosEntrada;
		public String 					p_ValorizacionAutomatica = "S";
		
		public Long p_idPais =null;
	    	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
		public IngresoCustodiaEntrada(){
	    	p_DatosEntrada = new ArrayList<ObjDatosEntrada>();
	    };
	};
	
	public static class IngresoCustodiaSalida {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<ObjDatosSalida>		P_OUT_DATOSSALIDA;
		
		public static class ObjDatosSalida {
			public Long		IdGenerarMovimientoCustodia;
			public String	TablaOrigen;			
			public Long		IdCustodia;
			public Long		FolioCustodia;			
			public Long		IdOperacion;			//Si viene del negocio
			public Long		FolioComprobante;		//Si viene de facturacion
			public Long		IdInstrumento;
			public String	RegistroConProblemas = "N";
			public String	Mensaje;
			public String	DesplegarMensaje = "S";
			public BigDecimal MontoCompraFactorOriginal;
			
			public String toString() {
				return Utils.printJson(this, false);
			};
		};
		public String toString() {
			return Utils.printJson(this, false);
		};
		public IngresoCustodiaSalida (){
			P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			P_OUT_DATOSSALIDA	= new ArrayList<ObjDatosSalida>();
		};
	};
	
	////////********************************************************************************************************************************************////////
	
	public IngresoCustodiaSalida logica(Connection con, IngresoCustodiaEntrada in) throws Exception {
		IngresoCustodiaSalida out = new IngresoCustodiaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;		
		SQL sql = new SQL();
		
		try {
			//00 - INFORMACION DE SISTEMA
			ConsultarEmpresa empresaService = new ConsultarEmpresa();
			
			InformacionEmpresa empresa = null;
			if (in.p_idPais != null && in.p_idPais != 0 ) {
				empresa = new InformacionEmpresa();
				empresa.IdPais = in.p_idPais;
			}else{
				empresa = empresaService.logica(con, in.p_IdEmpresa);	
			}

			SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
			
			//01 - ESTANDARIZAR LOS REGISTROS DE ENTRADA
			EstandarizacionRegistroCustodia.EstandarizacionRegistroCustodiaEntrada  paramInEstandarizacionRegistros = new EstandarizacionRegistroCustodia.EstandarizacionRegistroCustodiaEntrada();
			paramInEstandarizacionRegistros.p_IdEmpresa = in.p_IdEmpresa;
			
			for (ObjDatosEntrada DE: in.p_DatosEntrada){
				EstandarizacionRegistroCustodia.ObjRegistroAProcesar RegistroEstandarizar = new EstandarizacionRegistroCustodia.ObjRegistroAProcesar(); 
				Utils.copyPojo(DE, RegistroEstandarizar);
				paramInEstandarizacionRegistros.p_RegistrosAProcesar.add(RegistroEstandarizar);
			};			
			EstandarizacionRegistroCustodia EstandarizacionRegistro = new EstandarizacionRegistroCustodia();
			EstandarizacionRegistroCustodia.EstandarizacionRegistroCustodiaSalida resultEstandarizacion = EstandarizacionRegistro.logica(con, paramInEstandarizacionRegistros);
			Set<Long> movimientosCustodia = new HashSet<>();
			HashMap<Long,Object> hmDatosEstandarizados = Utils.List2Hash( resultEstandarizacion.p_RegistrosProcesados.stream().filter(reg -> movimientosCustodia.add(reg.IdGenerarMovimientoCustodia)).collect(Collectors.toList()) , "IdGenerarMovimientoCustodia");
			if (!resultEstandarizacion.P_OUT_ERRORESCONTADOR.equals(0L)){
				out.P_OUT_MENSAJES = resultEstandarizacion.P_OUT_MENSAJES;
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			};
			
			//02 - RECUPERAR INFORMACION DE REGISTROS EXISTENTES ( Para ingreso de saldos )
			List<Long> IdCartera = new ArrayList<Long>();
			List<Long> IdGarantia = new ArrayList<Long>();
			List<Long> IdSubyacente = new ArrayList<Long>();
			for (ObjDatosEntrada DE: in.p_DatosEntrada){
				if (DE.EsRegistroNuevo.equalsIgnoreCase("N") && DE.TablaAfectada.equals("CARTERA") )
					IdCartera.add( DE.IdCustodia );				
				if (DE.EsRegistroNuevo.equalsIgnoreCase("N") && DE.TablaAfectada.equals("GARANTIA") )
					IdGarantia.add( DE.IdCustodia );
				if (DE.EsRegistroNuevo.equalsIgnoreCase("N") && DE.TablaAfectada.equals("SUBYACENTE") )
					IdSubyacente.add( DE.IdCustodia);
				if (DE.EsRegistroNuevo.equalsIgnoreCase("S") && DE.IdCarteraOrigen != null)
					IdCartera.add( DE.IdCarteraOrigen );
			};
			HashMap<String,Object> paramsCartera = new HashMap<>();
			paramsCartera.put("IdCartera", IdCartera);
			HashMap<Long,Object> hmCarteras = new HashMap<Long,Object>();
			if (IdCartera.size() > 0)
			{
				List<ObjTablaCartera> CAR = sql.query(con, SQL_SelectCartera, paramsCartera, ObjTablaCartera.class);
				//HashMap<Long,Object> hmCarteras = Utils.List2Hash(CAR, "IdCartera");
				hmCarteras = Utils.List2Hash(CAR, "IdCartera");
			}
						
			HashMap<String,Object> paramsGarantia = new HashMap<>();
			paramsGarantia.put("IdGarantia", IdGarantia);
			HashMap<Long,Object> hmGarantias = new HashMap<Long,Object>(); 
			
			if (IdGarantia.size() > 0)
			{
				List<ObjTablaGarantia> GAR = sql.query(con, SQL_SelectGarantia, paramsGarantia, ObjTablaGarantia.class);
				hmGarantias = Utils.List2Hash(GAR, "IdGarantia");
			}
			
			HashMap<String,Object> paramsSubyacente = new HashMap<>();
			paramsSubyacente.put("IdSubyacente", IdSubyacente);
			HashMap<Long,Object> hmSubyacentes = new HashMap<Long,Object>();
			if (IdSubyacente.size() > 0)
			{
				List<ObjTablaSubyacente> SUB = sql.query(con, SQL_SelectSubyacente, paramsSubyacente, ObjTablaSubyacente.class);
				hmSubyacentes = Utils.List2Hash(SUB, "IdSubyacente");
			}
			
			

			//03- OBTENER SECUENCIAS DE TABLAS
			
			Long cantTransito = 0L;
			Long cantCartera = 0L;
			Long cantGarantia = 0L;
			Long cantSubyacente = 0L;
			Long cantCNegativas = 0L;
			for (ObjDatosEntrada RP: in.p_DatosEntrada){
				if ( RP.TablaAfectada.equalsIgnoreCase("TRANSITO"))		cantTransito++;
				if ( RP.TablaAfectada.equalsIgnoreCase("CARTERA"))		cantCartera++;
				if ( RP.TablaAfectada.equalsIgnoreCase("GARANTIA"))		cantGarantia++;
				if ( RP.TablaAfectada.equalsIgnoreCase("SUBYACENTE"))	cantSubyacente++;
				if ( RP.TablaAfectada.equalsIgnoreCase("NEGATIVA"))		cantCNegativas++;
			};
			
			List<Long> SecIdCartera = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::IdCartera", new Long(cantCartera));
			List<Long> SecFolioCartera = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::FolioCartera", new Long(cantCartera));
			List<Long> SecFolioComprobanteCustodia = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::FolioComprobanteCustodia", new Long(cantCartera+cantTransito+cantGarantia+cantSubyacente) );
			//---------
			List<Long> SecIdMovimientoCarteraEnTransito = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::FolioCartera", new Long(cantTransito));
			//---------
			List<Long> SecIdGarantia = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::IdGarantia", new Long(cantGarantia));
			List<Long> SecFolioGarantia = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::FolioCartera", new Long(cantGarantia));
			//---------
			List<Long> SecIdSubyacente = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::IdSubyacente", new Long(cantSubyacente));
			List<Long> SecFolioSubyacente = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::FolioCartera", new Long(cantSubyacente));
			//---------
			List<Long> SecIdCNegativa = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::FolioCartera", new Long(cantCNegativas));

			
			//04 - PREPARAR REGISTROS A INGRESAR
			int indxTransito = 0;
			int indxCartera = 0;
			int indxGarantia = 0;
			int indxSubyacente = 0;			
			int indxCNegativa = 0;			
			List<ObjTablaTransito> paramsActualizacionTransito = new ArrayList<ObjTablaTransito>();
			List<ObjTablaCartera> paramsActualizacionCartera = new ArrayList<ObjTablaCartera>();
			List<ObjTablaGarantia> paramsActualizacionGarantia = new ArrayList<ObjTablaGarantia>();
			List<ObjTablaSubyacente> paramsActualizacionSubyacente = new ArrayList<ObjTablaSubyacente>();
			List<ObjTablaCarteraNegativa> paramsActualizacionCNegativa = new ArrayList<ObjTablaCarteraNegativa>();
			List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> paramsValorizarCustodia = new ArrayList<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada>();
			//Se utiliza para comprobar los ingresos correspondientes a Vencimiento de Pactos
			List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> paramsValorizarCustodiaVCDPAC = new ArrayList<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada>();
			
			HashMap<Long,Object> paridadesUFUTM = new HashMap<Long,Object>();
			
			//CALCULA PARIDAD PARA COSTO UF Y RENTABILIDAD NETA
			{
				HashMap<String,Boolean> fechas = new HashMap<String,Boolean>();
				CalcularParidad paridad = new CalcularParidad();
				CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
				calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
				calcularParidadEntrada.p_IdTrabajo = 2001L;
				
				for(ObjDatosEntrada DatoEntrada: in.p_DatosEntrada) {
					EstandarizacionRegistroCustodia.ObjRegistroProcesado InfoIngreso = (EstandarizacionRegistroCustodia.ObjRegistroProcesado) hmDatosEstandarizados.get(DatoEntrada.IdGenerarMovimientoCustodia);
					if ( DatoEntrada.TablaAfectada.equalsIgnoreCase("CARTERA") && InfoIngreso != null){
						ObjTablaCartera InfoCartera = (ObjTablaCartera) hmCarteras.get(DatoEntrada.IdCustodia);
						java.sql.Date fecha = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaOperacion : InfoCartera.FechaOperacion;
						String strFecha = "0001-01-01";
						if(fecha != null) {
							strFecha = Fecha.toStringDate(fecha);
						}
						if(fechas.get(strFecha) == null) {
							CalculoEntrada uf = new CalculoEntrada();
							uf.Fecha = strFecha;
							uf.IdInterno = Long.valueOf(strFecha.replaceAll("\\-", ""));
							uf.IdMonedaOrigen = "UF";
							uf.IdMonedaDestino = "CLP";
							uf.IdTrabajo = 2001L;
							uf.MontoOrigen = BigDecimal.ONE;
							uf.IdUso = "CARTERA";
							calcularParidadEntrada.p_CalculosEntrada.add(uf);
							CalculoEntrada utm = new CalculoEntrada();
							utm.Fecha = strFecha;
							utm.IdInterno = Long.valueOf(strFecha.replaceAll("\\-", "")) * -1;
							utm.IdMonedaOrigen = "UTM";
							utm.IdMonedaDestino = "CLP";
							utm.IdTrabajo = 2001L;
							utm.MontoOrigen = BigDecimal.ONE;
							utm.IdUso = "CARTERA";
							calcularParidadEntrada.p_CalculosEntrada.add(utm);
							fechas.put(strFecha, true);
						}
					}
				}
				
				if(calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
					CalcularParidadSalida calcularParidadSalida = paridad.logica(con, calcularParidadEntrada);
					paridadesUFUTM = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
				}
			}
			
			for (ObjDatosEntrada DatoEntrada: in.p_DatosEntrada) {
				EstandarizacionRegistroCustodia.ObjRegistroProcesado InfoIngreso = (EstandarizacionRegistroCustodia.ObjRegistroProcesado) hmDatosEstandarizados.get(DatoEntrada.IdGenerarMovimientoCustodia);				
				if (InfoIngreso == null){
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;
				};

				if ( DatoEntrada.TablaAfectada.equalsIgnoreCase("CARTERA")){
									
					ObjTablaCartera InfoCartera = (ObjTablaCartera) hmCarteras.get(DatoEntrada.IdCustodia);					
					ObjTablaCartera registroAIngresar = new ObjTablaCartera();
					
					registroAIngresar.IdCartera 				= SecIdCartera.get( indxCartera );
					registroAIngresar.IdCliente 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCliente : InfoCartera.IdCliente;
					registroAIngresar.IdPortafolio 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdPortafolio : InfoCartera.IdPortafolio;
					registroAIngresar.IdClienteRelacionado 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdClienteRelacionado : InfoCartera.IdClienteRelacionado;
					registroAIngresar.IdPortafolioRelacionado 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdPortafolioRelacionado : InfoCartera.IdPortafolioRelacionado;
					registroAIngresar.FolioCartera 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") && DatoEntrada.MantieneFolioCustodia.equalsIgnoreCase("N") ? SecFolioCartera.get( indxCartera ) : (DatoEntrada.GenerarNuevoFolioCustodia.equals("S") ? SecFolioCartera.get( indxCartera ) : InfoCartera.FolioCartera);
					registroAIngresar.FolioCarteraRelacionada 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("N") ? DatoEntrada.FolioCarteraRelacionada : (DatoEntrada.IdTipoOperacion.equalsIgnoreCase("EMPRES") || DatoEntrada.IdConcepto.equalsIgnoreCase("NETDER") ? DatoEntrada.FolioCarteraRelacionada : null );
					registroAIngresar.IdConcepto 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdConcepto : InfoCartera.IdConcepto;
					registroAIngresar.IdTipoOperacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoOperacion : InfoCartera.IdTipoOperacion;
					registroAIngresar.FolioGarantiaOrigen 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("N") ? DatoEntrada.FolioGarantiaOrigen : null;
					registroAIngresar.FolioSistemaExterno 		= DatoEntrada.FolioSistemaExterno;
					registroAIngresar.TipoCustodia 		        = DatoEntrada.TipoCustodia;
					registroAIngresar.ExigirGarantia 		    = DatoEntrada.ExigirGarantia;
					registroAIngresar.EsCustodiaExterna 		= DatoEntrada.EsCustodiaExterna;
					registroAIngresar.IdEmpresa 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? in.p_IdEmpresa : InfoCartera.IdEmpresa;
					registroAIngresar.FechaOperacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaOperacion : InfoCartera.FechaOperacion;
					registroAIngresar.FechaVencimientoOperacion	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaVencimientoOperacion : InfoCartera.FechaVencimientoOperacion;
					registroAIngresar.PlazoEnDiasOperacion 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.PlazoEnDiasOperacion : InfoCartera.PlazoEnDiasOperacion;
					registroAIngresar.IdCuentaCustodioPortafolio = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCuentaCustodioPortafolio : InfoCartera.IdCuentaCustodioPortafolio;
					if(DatoEntrada.EsPendienteAutorizacion.equalsIgnoreCase("S")) {
						registroAIngresar.IdEstado				=  "PEN";
					}else {
						registroAIngresar.IdEstado 				= "VIG";
					}
					registroAIngresar.IdTipoProducto 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoProducto : InfoCartera.IdTipoProducto;
					registroAIngresar.IdTipoCanalIngreso 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoCanalIngreso : InfoCartera.IdTipoCanalIngreso;
					registroAIngresar.IdMonedaSistema			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaSistema : InfoCartera.IdMonedaSistema;
					registroAIngresar.IdMonedaAdicional 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaAdicional : InfoCartera.IdMonedaAdicional;
					registroAIngresar.FechaFactorMonedaAdicional = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.FechaFactorMonedaAdicional;
					registroAIngresar.ValorFactorMonedaAdicional = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.ValorFactorMonedaAdicional;
					registroAIngresar.IdMovimientoCarteraEnTransito = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdMovimientoCarteraEnTransitoOrigen : InfoCartera.IdMovimientoCarteraEnTransito;
					registroAIngresar.IdConcepto 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdConcepto : InfoCartera.IdConcepto;
					registroAIngresar.IdTipoOperacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoOperacion : InfoCartera.IdTipoOperacion;
					registroAIngresar.IdOperacion 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") || (this.esRetornoMesa(DatoEntrada.IdConcepto,InfoIngreso.IdOperacion))? InfoIngreso.IdOperacion : InfoCartera.IdOperacion;
					registroAIngresar.IdTipoComprobante 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoComprobante : InfoCartera.IdTipoComprobante;
					registroAIngresar.FolioComprobante 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? (InfoIngreso.IdTipoComprobante.equals("CUS") ? SecFolioComprobanteCustodia.get(indxCartera) : InfoIngreso.FolioComprobante) : InfoCartera.FolioComprobante;
					registroAIngresar.FechaComprobante 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaComprobante : InfoCartera.FechaComprobante;
					registroAIngresar.FolioExternoReferencia 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FolioExternoReferencia : InfoCartera.FolioExternoReferencia;
					registroAIngresar.IdMesa 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMesa : InfoCartera.IdMesa;
					registroAIngresar.IdBolsa 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdBolsa : InfoCartera.IdBolsa;
					registroAIngresar.IdSucursal 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdSucursal : InfoCartera.IdSucursal;
					registroAIngresar.IdAgente 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdAgente : InfoCartera.IdAgente;
					registroAIngresar.IdCondicionLiquidacion 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCondicionLiquidacion : InfoCartera.IdCondicionLiquidacion;
					registroAIngresar.FechaLiquidacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaLiquidacion : InfoCartera.FechaLiquidacion;
					registroAIngresar.EsIntraday 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.EsIntraday : InfoCartera.EsIntraday;
					registroAIngresar.UsaGarantiaEspecifica 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.UsaGarantiaEspecifica : InfoCartera.UsaGarantiaEspecifica;
					registroAIngresar.CobraPrimaAlInicio 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CobraPrimaAlInicio : InfoCartera.CobraPrimaAlInicio;
					registroAIngresar.IdTipoPrestamo 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoPrestamo : InfoCartera.IdTipoPrestamo;
					registroAIngresar.Glosa 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.Glosa : InfoCartera.Glosa;
					registroAIngresar.IdUsuario 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdUsuario : InfoCartera.IdUsuario;
					
					if(("VNSIMU").equalsIgnoreCase(DatoEntrada.IdConcepto) || ("CMSIMU").equalsIgnoreCase(DatoEntrada.IdConcepto)) {
						registroAIngresar.IdModulo 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? "FAC" : InfoCartera.IdModulo;
					} else {
						registroAIngresar.IdModulo 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? in.p_IdModulo : InfoCartera.IdModulo;						
					}
					
					registroAIngresar.TipoCarteraCorredor 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TipoCarteraCorredor : InfoCartera.TipoCarteraCorredor;
					registroAIngresar.TipoCarteraNormativa 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TipoCarteraNormativa : InfoCartera.TipoCarteraNormativa;
					registroAIngresar.FechaCurseInversion 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaCurse : InfoCartera.FechaCurseInversion;
					registroAIngresar.TipoFactorAjuste 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TipoFactorAjuste : InfoCartera.TipoFactorAjuste;
					registroAIngresar.ValorFactorAjusteaFechaCurse 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorFactorAjusteaFechaCurse : InfoCartera.ValorFactorAjusteaFechaCurse;
					registroAIngresar.CostoInversionEnFactorAjuste 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CostoEnFactorAjuste : InfoCartera.CostoInversionEnFactorAjuste;
					registroAIngresar.IdClasificacionTipoInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdClasificacionTipoInstrumento : InfoCartera.IdClasificacionTipoInstrumento;
					registroAIngresar.IdTipoInstrumento 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoInstrumento : InfoCartera.IdTipoInstrumento;
					registroAIngresar.IdInstrumento 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdInstrumento : InfoCartera.IdInstrumento;
					registroAIngresar.IdInstrumentoAnterior 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdInstrumento : InfoCartera.IdInstrumentoAnterior;;
					registroAIngresar.Nemotecnico 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Nemotecnico : InfoCartera.Nemotecnico;
					registroAIngresar.IdEmisor 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdEmisor : InfoCartera.IdEmisor;
					registroAIngresar.NemoSVS 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.NemoSVS : InfoCartera.NemoSVS;
					registroAIngresar.IdTipoReajuste 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoReajuste : InfoCartera.IdTipoReajuste;
					registroAIngresar.IdMonedaEmision 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaEmision : InfoCartera.IdMonedaEmision;
					registroAIngresar.FechaEmisionInstrumento 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaEmisionInstrumento : InfoCartera.FechaEmisionInstrumento;
					registroAIngresar.FechaVencimientoInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaVencimientoInstrumento : InfoCartera.FechaVencimientoInstrumento;
					registroAIngresar.EsFungible 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.EsFungible : InfoCartera.EsFungible;
					registroAIngresar.ValorizaPor 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorizaPor : InfoCartera.ValorizaPor;
					registroAIngresar.TipoValorizacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TipoValorizacion : InfoCartera.TipoValorizacion;
					registroAIngresar.TieneCorteDeCupon 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TieneCorteDeCupon : InfoCartera.TieneCorteDeCupon;
					registroAIngresar.CodigoIFRS 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CodigoIFRS : InfoCartera.CodigoIFRS;
					registroAIngresar.IdBoveda 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdBoveda : InfoCartera.IdBoveda;
					registroAIngresar.CuentaBoveda 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CuentaBoveda : InfoCartera.CuentaBoveda;
					registroAIngresar.IdMonedaInstrumento 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaInstrumento : InfoCartera.IdMonedaInstrumento;
					registroAIngresar.Tamano 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Tamano : InfoCartera.Tamano;
					registroAIngresar.TIFAfectoBeneficio 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.TIFAfectoBeneficio;
					registroAIngresar.TIFTasa 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.TIFTasa;
					registroAIngresar.ISIN 						= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ISIN : InfoCartera.ISIN;
					registroAIngresar.FechaFactorMonedaInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.FechaFactorMonedaInstrumento;
					registroAIngresar.ValorFactorMonedaInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.ValorFactorMonedaInstrumento;
					registroAIngresar.CorteMinimo 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CorteMinimo : InfoCartera.CorteMinimo;
					registroAIngresar.Cantidad 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Cantidad : (DatoEntrada.GenerarNuevoFolioCustodia.equals("S") ? InfoIngreso.Cantidad : (!"-2".equalsIgnoreCase(DatoEntrada.FolioExternoReferencia) ? InfoCartera.Cantidad : DatoEntrada.Cantidad));					
					registroAIngresar.ValorRescate 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorRescate : (DatoEntrada.GenerarNuevoFolioCustodia.equals("S") ? InfoIngreso.ValorRescate : (!"-2".equalsIgnoreCase(DatoEntrada.FolioExternoReferencia) ? InfoCartera.ValorRescate : DatoEntrada.ValorRescate));
					registroAIngresar.PrecioSucio 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.PrecioSucio : InfoCartera.PrecioSucio;
					registroAIngresar.PrecioCompra 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.PrecioCompra : InfoCartera.PrecioCompra;
					registroAIngresar.TasaCompra 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") || "S".equals(DatoEntrada.UsaTasaRecompraPacto) ? InfoIngreso.TasaCompra : InfoCartera.TasaCompra;
					registroAIngresar.MontoCompra 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompra : (!"-2".equalsIgnoreCase(DatoEntrada.FolioExternoReferencia) ? InfoCartera.MontoCompra : DatoEntrada.MontoCompra);
					registroAIngresar.MontoCompraMonedaInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompraMonedaInstrumento : (!"-2".equalsIgnoreCase(DatoEntrada.FolioExternoReferencia) ? InfoCartera.MontoCompraMonedaInstrumento : DatoEntrada.MontoCompraMonedaInstrumento);
					registroAIngresar.MontoCompraMonedaAdicional 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.MontoCompraMonedaAdicional;
					registroAIngresar.TasaAgente 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TasaAgente : InfoCartera.TasaAgente;
					registroAIngresar.PrecioAPlazo 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.PrecioAPlazo : InfoCartera.PrecioAPlazo;
					registroAIngresar.MontoAPlazo 				= this.guardaMontoAPlazo(DatoEntrada)? InfoIngreso.MontoAPlazo : InfoCartera.MontoAPlazo;
					registroAIngresar.MontoAPlazoMonedaInstrumento 	= this.guardaMontoAPlazo(DatoEntrada)?InfoIngreso.MontoAPlazoMonedaInstrumento : InfoCartera.MontoAPlazoMonedaInstrumento;
					registroAIngresar.MontoAPlazoMonedaAdicional 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoCartera.MontoAPlazoMonedaAdicional;
					registroAIngresar.FechaEfectiva 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaEfectiva : InfoCartera.FechaEfectiva;
					registroAIngresar.IdPlaza 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdPlaza : InfoCartera.IdPlaza;
					registroAIngresar.IdFixing 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdFixing : InfoCartera.IdFixing;
					registroAIngresar.FechaFixing 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaFixing : InfoCartera.FechaFixing;
					registroAIngresar.HoraFixing 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.HoraFixing : InfoCartera.HoraFixing;
					registroAIngresar.IdModalidadCumplimiento 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdModalidadCumplimiento : InfoCartera.IdModalidadCumplimiento;
					registroAIngresar.IdReferenciaMercado 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdReferenciaMercado : InfoCartera.IdReferenciaMercado;
					registroAIngresar.IdMonedaPrincipal 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaPrincipal : InfoCartera.IdMonedaPrincipal;
					registroAIngresar.IdMonedaSecundaria 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaSecundaria : InfoCartera.IdMonedaSecundaria;
					registroAIngresar.ValorTipoCambioCierre 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorTipoCambioCierre : InfoCartera.ValorTipoCambioCierre;
					registroAIngresar.ValorParidadCierre 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorParidadCierre : InfoCartera.ValorParidadCierre;
					registroAIngresar.ValorTipoCambioCosto 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorTipoCambioCosto : InfoCartera.ValorTipoCambioCosto;
					registroAIngresar.ValorParidadCosto 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorParidadCosto : InfoCartera.ValorParidadCosto;
					registroAIngresar.MontoMonedaPrincipal 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoMonedaPrincipal : (!"-2".equalsIgnoreCase(DatoEntrada.FolioExternoReferencia) ? InfoCartera.MontoMonedaPrincipal : DatoEntrada.MontoMonedaPrincipal);
					registroAIngresar.MontoMonedaSecundaria 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoMonedaSecundaria : InfoCartera.MontoMonedaSecundaria;
					registroAIngresar.IdRegimenFondoMutuo   	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdRegimenFondoMutuo : InfoCartera.IdRegimenFondoMutuo;
					registroAIngresar.FechaOrigenCompra			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaOrigenCompra : InfoCartera.FechaOrigenCompra;
					
					registroAIngresar.IdCarga 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdCarga : InfoCartera.IdCarga;
					registroAIngresar.IdSecuenciaCarteraMigracion = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdSecuenciaCarteraMigracion : InfoCartera.IdSecuenciaCarteraMigracion;
					registroAIngresar.CodigoPeriodo 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.CodigoPeriodo : InfoCartera.CodigoPeriodo;
					//------------------------------------------------------------------------------------------------------------
					registroAIngresar.INGGlosa 						= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.Glosa : InfoCartera.Glosa;
					registroAIngresar.INGIdConcepto					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") || InfoIngreso.IdConcepto != null ? InfoIngreso.IdConcepto : InfoCartera.IdConcepto;
					registroAIngresar.INGFechaOperacion 			= InfoIngreso.FechaOperacion;
					registroAIngresar.INGIdCondicionLiquidacion 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCondicionLiquidacion : InfoCartera.IdCondicionLiquidacion;
					registroAIngresar.INGFechaLiquidacion 			= InfoIngreso.FechaLiquidacion == null ? DatoEntrada.FechaLiquidacion : InfoIngreso.FechaLiquidacion;
					registroAIngresar.INGIdTipoOperacion			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") || InfoIngreso.IdTipoOperacion != null ? InfoIngreso.IdTipoOperacion : InfoCartera.IdTipoOperacion;
					registroAIngresar.INGCantidad 					= InfoIngreso.Cantidad;
					registroAIngresar.INGValorRescate 				= InfoIngreso.ValorRescate;
					registroAIngresar.INGPrecioSucio 				= InfoIngreso.PrecioSucio;
					registroAIngresar.INGPrecioCompra 				= InfoIngreso.PrecioCompra;
					registroAIngresar.INGTasaCompra 				= InfoIngreso.TasaCompra == null ? DatoEntrada.TasaCompra : InfoIngreso.TasaCompra;
					registroAIngresar.INGMontoATasaCompraActual 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompra : null;
					registroAIngresar.INGMontoCompraActualMonedaInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompraMonedaInstrumento : (!"VCDPAC".equalsIgnoreCase(DatoEntrada.IdConcepto) ? null : InfoIngreso.MontoCompraMonedaInstrumento);
					if(DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("N") && (registroAIngresar.IdTipoInstrumento.equals("PRF")||registroAIngresar.IdTipoInstrumento.equals("ERF"))) {
						registroAIngresar.INGMontoATasaCompraActual = DatoEntrada.MontoCompra;
						registroAIngresar.INGMontoCompraActualMonedaInstrumento =  DatoEntrada.MontoCompraMonedaInstrumento;
					}
					
					if(DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("N") && (registroAIngresar.IdTipoInstrumento.equals("EMP")||registroAIngresar.IdTipoInstrumento.equals("PRT"))) {
						registroAIngresar.INGMontoCompraActualMonedaInstrumento =  DatoEntrada.MontoCompraMonedaInstrumento;
						registroAIngresar.INGMontoATasaCompraActual =  DatoEntrada.MontoCompra;
					}
					if("N".equalsIgnoreCase(DatoEntrada.EsRegistroNuevo) && "FFM".equalsIgnoreCase(registroAIngresar.IdTipoInstrumento) && 
							"RESCFM".equalsIgnoreCase(DatoEntrada.IdTipoOperacion) && "FM".equalsIgnoreCase(InfoIngreso.IdClasificacionTipoInstrumento)) {
						registroAIngresar.INGMontoATasaCompraActual = InfoIngreso.MontoCompra;
						registroAIngresar.INGMontoCompraActualMonedaInstrumento =  InfoIngreso.MontoCompraMonedaInstrumento;
					}
					registroAIngresar.INGTasaAgente 				= InfoIngreso.TasaAgente;
					registroAIngresar.INGPrecioAPlazo 				= InfoIngreso.PrecioAPlazo;
					registroAIngresar.INGMontoAPlazo 				= InfoIngreso.MontoAPlazo;
					registroAIngresar.INGMontoAPlazoMonedaInstrumento = InfoIngreso.MontoAPlazoMonedaInstrumento;
					registroAIngresar.INGEsPuntos 					= InfoIngreso.EsPuntos;					
					registroAIngresar.INGTasaPuntosMonedaPrincipal 	= InfoIngreso.TasaPuntosMonedaPrincipal;
					registroAIngresar.INGTasaPuntosMonedaSecundaria = InfoIngreso.TasaPuntosMonedaSecundaria;					
					registroAIngresar.INGMontoMonedaPrincipal 		= InfoIngreso.MontoMonedaPrincipal;
					registroAIngresar.INGMontoMonedaSecundaria 		= InfoIngreso.MontoMonedaSecundaria;
					registroAIngresar.INGValorTipoCambioSpot 		= InfoIngreso.ValorTipoCambioSpot;
					registroAIngresar.INGValorTipoCambioCotizador 	= InfoIngreso.ValorTipoCambioCotizador;
					registroAIngresar.INGMontoUtilidadDiferida 		= InfoIngreso.MontoUtilidadDiferida;
					registroAIngresar.INGMontoPerdidaDiferida 		= InfoIngreso.MontoPerdidaDiferida;
					registroAIngresar.INGMontoMargen 				= InfoIngreso.MontoMargen;
					registroAIngresar.INGMontoValorRazonableActivo 	= InfoIngreso.MontoValorRazonableActivo;
					registroAIngresar.INGMontoValorRazonablePasivo 	= InfoIngreso.MontoValorRazonablePasivo;
					registroAIngresar.INGMontoValorRazonableNeto 	= InfoIngreso.MontoValorRazonableNeto;
					registroAIngresar.INGTasaValorRazonableActivo 	= InfoIngreso.TasaValorRazonableActivo;
					registroAIngresar.INGTasaValorRazonablePasivo 	= InfoIngreso.TasaValorRazonablePasivo;
					registroAIngresar.INGMontoDevengoTipoCambio 	= InfoIngreso.MontoDevengoTipoCambio;
					registroAIngresar.INGMontoDevengoTipoCambioAnterior = InfoIngreso.MontoDevengoTipoCambioAnterior;
					registroAIngresar.INGMontoDiferido 				= InfoIngreso.MontoDiferido;
					registroAIngresar.INGMontoDiferidoDiario 		= InfoIngreso.MontoDiferidoDiario;
					registroAIngresar.INGMontoDiferidoAcumulado 	= InfoIngreso.MontoDiferidoAcumulado;
					registroAIngresar.INGMontoMatriz 				= InfoIngreso.MontoMatriz;
					registroAIngresar.INGValorFactorAjusteaFechaCurse 	= InfoIngreso.ValorFactorAjusteaFechaCurse;
					registroAIngresar.INGCostoInversionEnFactorAjuste 	= InfoIngreso.CostoEnFactorAjuste;
					
					//marca inicial carteras
					registroAIngresar.EsPrimeraCartera				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? "S" : null;
					
					if(InfoIngreso.IdConcepto != null && InfoIngreso.IdConcepto.contentEquals("REVFWD")) {
						registroAIngresar.IdConcepto				= InfoIngreso.IdConcepto;
					}
					
					if(DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S")){
						if(registroAIngresar.IdClasificacionTipoInstrumento.equals("RV") || registroAIngresar.IdClasificacionTipoInstrumento.equals("FM")){
							//calculo rentabilidad
							CalcularValores main = new CalcularValores();
							CalcularValores.EntradaCalcularValores entrada = new CalcularValores.EntradaCalcularValores();
							List<ObjTablaCartera> list = new ArrayList<ObjTablaCartera>();
							list.add(registroAIngresar);
							entrada.p_IdEmpresa = 1L;
							entrada.p_IdUsuario = 1L;
							entrada.p_IdModulo = "CUS";
							entrada.p_IngresoCustodia = list;
							entrada.p_Paridades = paridadesUFUTM;
							CalcularValores.SalidaCalcularValores salida = main.logica(con, entrada);
							HashMap<Long,Object> salidaValores = Utils.List2Hash(salida.p_IngresoCustodia, "IdCartera");
							ObjTablaCartera carteraConValores = (ObjTablaCartera) salidaValores.get(registroAIngresar.IdCartera);
							registroAIngresar.CostoInversionEnFactorAjuste = carteraConValores.CostoInversionEnFactorAjuste;
							registroAIngresar.INGCostoInversionEnFactorAjuste = carteraConValores.INGCostoInversionEnFactorAjuste;
							//solicitados por normativa, Mario Santander
							registroAIngresar.TipoFactor 				= carteraConValores.TipoFactor;
							registroAIngresar.FechaFactor 				= carteraConValores.FechaFactor;
							registroAIngresar.ValorFactor 				= carteraConValores.ValorFactor;
							registroAIngresar.MontoCompraFactor			= carteraConValores.MontoCompraFactor;
							//solicitados por normativa, Mario Santander
						}
					}else{
						//solicitados por normativa, Mario Santander
						registroAIngresar.TipoFactor 				= InfoCartera.TipoFactor;
						registroAIngresar.FechaFactor 				= InfoCartera.FechaFactor;
						registroAIngresar.ValorFactor 				= InfoCartera.ValorFactor;
						registroAIngresar.MontoCompraFactor			= InfoCartera.MontoCompraFactor;
						//solicitados por normativa, Mario Santander
					}	    
					
					indxCartera ++;
					paramsActualizacionCartera.add(registroAIngresar);

					
					//VALORIZACION
					ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
					Utils.copyPojo(registroAIngresar, ValorizarCustodia);					
					ValorizarCustodia.IdCustodia					= registroAIngresar.IdCartera;
					ValorizarCustodia.EsRegistroNuevo				= DatoEntrada.EsRegistroNuevo;
					ValorizarCustodia.GenerarNuevoFolioCustodia		= DatoEntrada.GenerarNuevoFolioCustodia;
					ValorizarCustodia.TablaOrigen					= DatoEntrada.TablaAfectada;					
					ValorizarCustodia.FechaEmisionInstrumento 		= registroAIngresar.FechaEmisionInstrumento;
					ValorizarCustodia.FechaVencimientoInstrumento 	= registroAIngresar.FechaVencimientoInstrumento;
					ValorizarCustodia.IdMonedaEmisionInstrumento 	= registroAIngresar.IdMonedaEmision;
					ValorizarCustodia.IdTipoReajusteInstrumento 	= registroAIngresar.IdTipoReajuste;
					ValorizarCustodia.Cantidad						= registroAIngresar.INGCantidad;
					ValorizarCustodia.ValorRescate					= registroAIngresar.INGValorRescate;
					ValorizarCustodia.TasaCompra 					= registroAIngresar.INGTasaCompra; 
					ValorizarCustodia.PrecioCompra 					= registroAIngresar.INGPrecioCompra;
					ValorizarCustodia.MontoCompra 					= registroAIngresar.INGMontoCompraActualMonedaInstrumento;
					ValorizarCustodia.IdTipoInstrumento 			= registroAIngresar.IdTipoInstrumento;
								
					if ("PRES".equalsIgnoreCase(DatoEntrada.IdTipoProducto)) {
						ValorizarCustodia.IdInstrumento = DatoEntrada.IdInstrumentoSubyacente;
						ValorizarCustodia.Nemotecnico = DatoEntrada.NemotecnicoSubyacente;
						ValorizarCustodia.IdMonedaInstrumento = DatoEntrada.IdMonedaInstrumento;
						ValorizarCustodia.FechaOperacion = DatoEntrada.FechaOperacion;
						if ("RF".equalsIgnoreCase(registroAIngresar.IdClasificacionTipoInstrumento)) {
							ValorizarCustodia.TasaCompra = DatoEntrada.TasaCompraInstrumento;
							ValorizarCustodia.IdTipoInstrumento = DatoEntrada.IdTipoInstrumentoSubyacente;
						}
					}
					
					paramsValorizarCustodia.add(ValorizarCustodia);
					if("VCDPAC".equalsIgnoreCase(DatoEntrada.IdConcepto)) paramsValorizarCustodiaVCDPAC.add(ValorizarCustodia);
					
					//Informacion salida
					IngresoCustodiaSalida.ObjDatosSalida InfoSalida = new IngresoCustodiaSalida.ObjDatosSalida();					
					InfoSalida.IdGenerarMovimientoCustodia = DatoEntrada.IdGenerarMovimientoCustodia;
					InfoSalida.TablaOrigen 		= DatoEntrada.TablaAfectada;
					InfoSalida.IdCustodia		= registroAIngresar.IdCartera;
					InfoSalida.FolioCustodia	= registroAIngresar.FolioCartera;
					InfoSalida.MontoCompraFactorOriginal = registroAIngresar.MontoCompraFactor;
					Utils.copyPojo(registroAIngresar, InfoSalida);
					out.P_OUT_DATOSSALIDA.add(InfoSalida);
				};
				
				if (DatoEntrada.TablaAfectada.equalsIgnoreCase("TRANSITO")){
					
					ObjTablaTransito registroAIngresar = new ObjTablaTransito();
					
					registroAIngresar.IdGenerarMovimientoCustodia			= DatoEntrada.IdGenerarMovimientoCustodia;
					registroAIngresar.IdMovimientoCarteraEnTransito 		= SecIdMovimientoCarteraEnTransito.get( indxTransito );
					registroAIngresar.IdMovimientoCarteraEnTransitoRelacionado = DatoEntrada.IdMovimientoCarteraEnTransitoRelacionado;
					registroAIngresar.IdUsuarioIngreso 						= DatoEntrada.IdUsuario;
					registroAIngresar.FechaOperacion 						= InfoIngreso.FechaOperacion;
					registroAIngresar.FechaOperacionOriginal				= DatoEntrada.FechaOperacionOriginal;
					registroAIngresar.FechaVencimientoOperacion				= InfoIngreso.FechaVencimientoOperacion;
					registroAIngresar.IdMovimientoComprobante				= DatoEntrada.IdMovimientoComprobante;
					registroAIngresar.PlazoEnDiasOperacion 					= InfoIngreso.PlazoEnDiasOperacion;					
					registroAIngresar.IdTipoComprobante 					= InfoIngreso.IdTipoComprobante;
					registroAIngresar.FolioComprobante 						= InfoIngreso.IdTipoComprobante.equals("CUS") ? SecFolioComprobanteCustodia.get(indxTransito) : InfoIngreso.FolioComprobante;
					registroAIngresar.EsRegistroGeneradoPorSistema 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? "N" : "S";
					registroAIngresar.PermiteCertificacionPorUsuario 		= DatoEntrada.PermiteCertificacionPorUsuario;					
					registroAIngresar.OrigenCustodia						= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? "DAT" : null;
					registroAIngresar.IdCustodiaOrigen 						= DatoEntrada.IdCustodia;
					registroAIngresar.FolioCustodia 						= DatoEntrada.FolioCustodia;  //FolioCarteraOrigen;
					registroAIngresar.IdTipoOperacionOrigen					= DatoEntrada.IdTipoOperacionGestora;
					registroAIngresar.FolioCarteraGarantizada 				= InfoIngreso.FolioCarteraGarantizada;
					registroAIngresar.IdInstrumentoCarteraGarantizada 		= InfoIngreso.IdInstrumentoCarteraGarantizada;
					registroAIngresar.EsEspecifica 							= InfoIngreso.EsEspecifica;
					registroAIngresar.IdContraparteGarantia 				= InfoIngreso.IdContraparteGarantia;
					registroAIngresar.RazonSocialContraparteGarantia 		= InfoIngreso.RazonSocialContraparteGarantia;
					registroAIngresar.CuentaContraparteGarantia 			= InfoIngreso.CuentaContraparteGarantia;
					registroAIngresar.IdOrigenGarantia 						= InfoIngreso.IdOrigenGarantia;
					registroAIngresar.IdEspecieGarantia 					= InfoIngreso.IdEspecieGarantia;
					registroAIngresar.IdEmpresa 							= in.p_IdEmpresa;
					registroAIngresar.IdPais 								= empresa.IdPais;
					registroAIngresar.EsNominal 							= InfoIngreso.EsNominal;
					registroAIngresar.FolioExternoReferencia 				= InfoIngreso.FolioExternoReferencia;
					registroAIngresar.IdCliente 							= InfoIngreso.IdCliente;
					registroAIngresar.IdPortafolio 							= InfoIngreso.IdPortafolio;
					registroAIngresar.IdCuentaCustodioPortafolio 			= InfoIngreso.IdCuentaCustodioPortafolio;					
					registroAIngresar.IdClienteRelacionado 					= InfoIngreso.IdClienteRelacionado;
					registroAIngresar.IdPortafolioRelacionado 				= InfoIngreso.IdPortafolioRelacionado;
					registroAIngresar.IdCuentaCustodioPortafolioRelacionado	= InfoIngreso.IdCuentaCustodioPortafolioRelacionado;
					if(DatoEntrada.EsPendienteAutorizacion.equalsIgnoreCase("S")) {
						registroAIngresar.IdEstado = "PDA";
					} else {
						registroAIngresar.IdEstado = "PEN";
					}
					registroAIngresar.IdTipoCanalIngreso 					= InfoIngreso.IdTipoCanalIngreso;
					registroAIngresar.IdMonedaSistema 						= InfoIngreso.IdMonedaSistema;
					registroAIngresar.IdMonedaAdicional 					= InfoIngreso.IdMonedaAdicional;
					registroAIngresar.CodigoIFRS 							= InfoIngreso.CodigoIFRS;
					registroAIngresar.IdConcepto 							= InfoIngreso.IdConcepto;
					registroAIngresar.IdTipoOperacion 						= InfoIngreso.IdTipoOperacion;
					registroAIngresar.IdTipoProducto 						= InfoIngreso.IdTipoProducto;
					registroAIngresar.IdOperacion 							= InfoIngreso.IdOperacion;
					registroAIngresar.IdMesa 								= InfoIngreso.IdMesa;
					registroAIngresar.IdBolsa 								= InfoIngreso.IdBolsa;
					registroAIngresar.IdSucursal 							= DatoEntrada.IdSucursal;
					registroAIngresar.IdAgente 								= DatoEntrada.IdAgente;
					registroAIngresar.IdCondicionLiquidacion 				= InfoIngreso.IdCondicionLiquidacion;
					registroAIngresar.FechaLiquidacion 						= InfoIngreso.FechaLiquidacion;
					registroAIngresar.EsIntraday 							= InfoIngreso.EsIntraday;
					registroAIngresar.UsaGarantiaEspecifica 				= InfoIngreso.UsaGarantiaEspecifica;
					registroAIngresar.Glosa 								= DatoEntrada.Glosa;
					registroAIngresar.IdUsuario 							= DatoEntrada.IdUsuario;
					registroAIngresar.IdModulo 								= in.p_IdModulo;
					registroAIngresar.TipoCarteraCorredor 					= InfoIngreso.TipoCarteraCorredor;
					registroAIngresar.TipoCarteraNormativa 					= InfoIngreso.TipoCarteraNormativa;
					registroAIngresar.FechaCurse 							= InfoIngreso.FechaCurse;
					registroAIngresar.TipoFactorAjuste 						= InfoIngreso.TipoFactorAjuste;
					registroAIngresar.ValorFactorAjusteaFechaCurse 			= InfoIngreso.ValorFactorAjusteaFechaCurse;
					registroAIngresar.CostoEnFactorAjuste 					= InfoIngreso.CostoEnFactorAjuste;
					registroAIngresar.IdClasificacionTipoInstrumento 		= InfoIngreso.IdClasificacionTipoInstrumento;
					registroAIngresar.IdTipoInstrumento 					= InfoIngreso.IdTipoInstrumento;
					registroAIngresar.IdInstrumento 						= InfoIngreso.IdInstrumento;
					registroAIngresar.IdInstrumentoAnterior 				= InfoIngreso.IdInstrumentoAnterior;
					registroAIngresar.Nemotecnico 							= InfoIngreso.Nemotecnico;
					registroAIngresar.IdEmisor 								= InfoIngreso.IdEmisor;
					registroAIngresar.NemoSVS 								= InfoIngreso.NemoSVS;
					registroAIngresar.IdTipoReajuste 						= InfoIngreso.IdTipoReajuste;
					registroAIngresar.IdMonedaEmision 						= InfoIngreso.IdMonedaEmision;
					registroAIngresar.FechaEmisionInstrumento 				= InfoIngreso.FechaEmisionInstrumento;
					registroAIngresar.FechaVencimientoInstrumento 			= InfoIngreso.FechaVencimientoInstrumento;
					registroAIngresar.EsFungible 							= InfoIngreso.EsFungible;
					registroAIngresar.ValorizaPor 							= InfoIngreso.ValorizaPor;
					registroAIngresar.TipoValorizacion 						= InfoIngreso.TipoValorizacion;
					registroAIngresar.TieneCorteDeCupon 					= InfoIngreso.TieneCorteDeCupon;
					registroAIngresar.IdMonedaInstrumento 					= InfoIngreso.IdMonedaInstrumento;
					registroAIngresar.Tamano 								= InfoIngreso.Tamano;
					registroAIngresar.IdBoveda 								= InfoIngreso.IdBoveda;
					registroAIngresar.CuentaBoveda 							= InfoIngreso.CuentaBoveda;
					registroAIngresar.ISIN 									= InfoIngreso.ISIN;					
					registroAIngresar.Cantidad 								= InfoIngreso.Cantidad;
					registroAIngresar.CorteMinimo 							= InfoIngreso.CorteMinimo;
					registroAIngresar.ValorRescate 							= InfoIngreso.ValorRescate;
					registroAIngresar.PrecioAPlazo 							= InfoIngreso.PrecioAPlazo;
					registroAIngresar.PrecioSucio 							= InfoIngreso.PrecioSucio;
					registroAIngresar.PrecioCompra 							= InfoIngreso.PrecioCompra;
					registroAIngresar.TasaCompra 							= InfoIngreso.TasaCompra;
					registroAIngresar.MontoCompra 							= InfoIngreso.MontoCompra;					
					registroAIngresar.MontoCompraMonedaInstrumento 			= InfoIngreso.MontoCompraMonedaInstrumento;
					registroAIngresar.MontoAPlazo 							= InfoIngreso.MontoAPlazo;
					registroAIngresar.MontoAPlazoMonedaInstrumento 			= InfoIngreso.MontoAPlazoMonedaInstrumento;
					registroAIngresar.IdCarga 								= DatoEntrada.IdCarga;
					registroAIngresar.IdSecuenciaCarteraMigracion 			= DatoEntrada.IdSecuenciaCarteraMigracion;
					registroAIngresar.CodigoPeriodo 						= DatoEntrada.CodigoPeriodo;
					registroAIngresar.EsVentaCorta							= DatoEntrada.EsVentaCorta;
					registroAIngresar.IdRegimenFondoMutuo					= DatoEntrada.IdRegimenFondoMutuo;
					registroAIngresar.EsVentaDescubierta                    = DatoEntrada.EsVentaDescubierta;
					registroAIngresar.FechaOrigenCompra						= DatoEntrada.FechaOrigenCompra;
					registroAIngresar.IdCorredora							= DatoEntrada.IdCorredora;
					registroAIngresar.TasaTIR								= DatoEntrada.TasaTIR;
					
					if(InfoIngreso.EsNominal != null && ("S").equalsIgnoreCase(InfoIngreso.EsNominal) || "N".equals(DatoEntrada.MueveCarteraEnTransito)){
						
						if(("ANTPRT").equalsIgnoreCase(InfoIngreso.IdConcepto) || ("VENPRT").equalsIgnoreCase(InfoIngreso.IdConcepto)) {
							registroAIngresar.MueveCustodia 					= "S";
						} else {
							registroAIngresar.MueveCustodia 					= "N";
						}						
						
					}else{
						registroAIngresar.MueveCustodia 					= "S";
					}
					indxTransito ++;
					paramsActualizacionTransito.add(registroAIngresar);
					
					
					//VALORIZACION
					ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
					Utils.copyPojo(registroAIngresar, ValorizarCustodia);
					ValorizarCustodia.IdCustodia					= registroAIngresar.IdMovimientoCarteraEnTransito;
					ValorizarCustodia.EsRegistroNuevo				= DatoEntrada.EsRegistroNuevo;
					ValorizarCustodia.GenerarNuevoFolioCustodia		= DatoEntrada.GenerarNuevoFolioCustodia;
					ValorizarCustodia.TablaOrigen					= DatoEntrada.TablaAfectada;
					ValorizarCustodia.FechaEmisionInstrumento 		= registroAIngresar.FechaEmisionInstrumento;
					ValorizarCustodia.FechaVencimientoInstrumento 	= registroAIngresar.FechaVencimientoInstrumento;
					ValorizarCustodia.IdMonedaEmisionInstrumento 	= registroAIngresar.IdMonedaEmision;
					ValorizarCustodia.IdTipoReajusteInstrumento 	= registroAIngresar.IdTipoReajuste;
					ValorizarCustodia.Cantidad						= registroAIngresar.Cantidad;
					ValorizarCustodia.ValorRescate					= registroAIngresar.ValorRescate;
					ValorizarCustodia.TasaCompra 					= registroAIngresar.TasaCompra; 
					ValorizarCustodia.PrecioCompra 					= registroAIngresar.PrecioCompra;
					ValorizarCustodia.MontoCompra 					= registroAIngresar.MontoCompraMonedaInstrumento;
					ValorizarCustodia.MontoCompraGarantia 			= DatoEntrada.IdOrigenGarantia != null && !DatoEntrada.IdOrigenGarantia.equalsIgnoreCase("CUS") ? registroAIngresar.MontoCompraMonedaInstrumento : null;
					
					if ("PRES".equalsIgnoreCase(DatoEntrada.IdTipoProducto)) {
						ValorizarCustodia.IdInstrumento = DatoEntrada.IdInstrumentoSubyacente;
						ValorizarCustodia.Nemotecnico = DatoEntrada.NemotecnicoSubyacente;
						ValorizarCustodia.IdMonedaInstrumento = DatoEntrada.IdMonedaInstrumento;
						ValorizarCustodia.FechaOperacion = registroAIngresar.FechaOperacion;
						if ("RF".equalsIgnoreCase(registroAIngresar.IdClasificacionTipoInstrumento))
							ValorizarCustodia.TasaCompra = DatoEntrada.TasaCompraInstrumento;
					}
					
					paramsValorizarCustodia.add(ValorizarCustodia);
										
					//Informacion salida
					IngresoCustodiaSalida.ObjDatosSalida InfoSalida = new IngresoCustodiaSalida.ObjDatosSalida();
					InfoSalida.IdGenerarMovimientoCustodia = DatoEntrada.IdGenerarMovimientoCustodia;
					InfoSalida.TablaOrigen 			= DatoEntrada.TablaAfectada;
					InfoSalida.IdCustodia			= registroAIngresar.IdMovimientoCarteraEnTransito;
					InfoSalida.DesplegarMensaje 	= DatoEntrada.PermiteCertificacionPorUsuario != null && DatoEntrada.PermiteCertificacionPorUsuario.equals("N") ? "N" : "S";
					Utils.copyPojo(registroAIngresar, InfoSalida);
					out.P_OUT_DATOSSALIDA.add(InfoSalida);
					
				};
				
				if (DatoEntrada.TablaAfectada.equalsIgnoreCase("NEGATIVA")){
					
					ObjTablaCarteraNegativa registroAIngresar = new ObjTablaCarteraNegativa();
					
					registroAIngresar.IdGenerarMovimientoCustodia			= DatoEntrada.IdGenerarMovimientoCustodia;
					registroAIngresar.IdCarteraNegativa				 		= SecIdCNegativa.get( indxCNegativa );
					//registroAIngresar.IdMovimientoCarteraEnTransitoRelacionado = DatoEntrada.IdMovimientoCarteraEnTransitoRelacionado;
					registroAIngresar.IdUsuarioIngreso 						= DatoEntrada.IdUsuario;
					registroAIngresar.FechaOperacion 						= DatoEntrada.FechaOperacion;
					//registroAIngresar.FechaOperacionOriginal				= DatoEntrada.FechaOperacionOriginal;
					registroAIngresar.FechaVencimientoOperacion				= DatoEntrada.FechaVencimientoOperacion == null?Fecha.toDate("0001-01-01"):DatoEntrada.FechaVencimientoOperacion;
					registroAIngresar.IdMovimientoComprobante				= DatoEntrada.IdMovimientoComprobante;
					//registroAIngresar.PlazoEnDiasOperacion 					= InfoIngreso.PlazoEnDiasOperacion;					
					registroAIngresar.IdTipoComprobante 					= DatoEntrada.IdTipoComprobante;
					registroAIngresar.FolioComprobante 						= DatoEntrada.FolioComprobante;
					registroAIngresar.FolioTransaccion						= DatoEntrada.FolioTransaccion == null? "":DatoEntrada.FolioTransaccion;
					registroAIngresar.FechaTransaccion						= DatoEntrada.FechaTransaccion == null? Fecha.toDate("0001-01-01"):DatoEntrada.FechaTransaccion;
					//registroAIngresar.EsRegistroGeneradoPorSistema 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? "N" : "S";
					//registroAIngresar.PermiteCertificacionPorUsuario 		= DatoEntrada.PermiteCertificacionPorUsuario;					
					//registroAIngresar.OrigenCustodia						= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? "DAT" : null;
					//registroAIngresar.IdCustodiaOrigen 						= DatoEntrada.IdCustodia;
					registroAIngresar.FolioCustodia 						= DatoEntrada.FolioCustodia;
					registroAIngresar.IdTipoOperacion						= DatoEntrada.IdTipoOperacion;
					//registroAIngresar.FolioCarteraGarantizada 				= InfoIngreso.FolioCarteraGarantizada;
					//registroAIngresar.IdInstrumentoCarteraGarantizada 		= InfoIngreso.IdInstrumentoCarteraGarantizada;
					//registroAIngresar.EsEspecifica 							= InfoIngreso.EsEspecifica;
					//registroAIngresar.IdContraparteGarantia 				= InfoIngreso.IdContraparteGarantia;
					//registroAIngresar.RazonSocialContraparteGarantia 		= InfoIngreso.RazonSocialContraparteGarantia;
					//registroAIngresar.CuentaContraparteGarantia 			= InfoIngreso.CuentaContraparteGarantia;
					//registroAIngresar.IdOrigenGarantia 						= InfoIngreso.IdOrigenGarantia;
					//registroAIngresar.IdEspecieGarantia 					= InfoIngreso.IdEspecieGarantia;
					registroAIngresar.IdEmpresa 							= in.p_IdEmpresa;
					//registroAIngresar.IdPais 								= empresa.IdPais;
					//registroAIngresar.EsNominal 							= InfoIngreso.EsNominal;
					registroAIngresar.FolioExternoReferencia 				= DatoEntrada.FolioExternoReferencia;
					registroAIngresar.IdCliente 							= DatoEntrada.IdCliente;
					registroAIngresar.IdPortafolio 							= DatoEntrada.IdPortafolio;
					registroAIngresar.IdCuentaCustodioPortafolio 			= DatoEntrada.IdCuentaCustodioPortafolio==null?0L:DatoEntrada.IdCuentaCustodioPortafolio;					
					//registroAIngresar.IdClienteRelacionado 					= InfoIngreso.IdClienteRelacionado;
					//registroAIngresar.IdPortafolioRelacionado 				= InfoIngreso.IdPortafolioRelacionado;
					//registroAIngresar.IdCuentaCustodioPortafolioRelacionado	= InfoIngreso.IdCuentaCustodioPortafolioRelacionado;
					registroAIngresar.IdEstado = "VIG";
					registroAIngresar.IdTipoCanalIngreso 					= DatoEntrada.IdTipoCanalIngreso;
					//registroAIngresar.IdMonedaSistema 						= InfoIngreso.IdMonedaSistema;
					//registroAIngresar.IdMonedaAdicional 					= InfoIngreso.IdMonedaAdicional;
					registroAIngresar.CodigoIFRS 							= DatoEntrada.CodigoIFRS == null?"":DatoEntrada.CodigoIFRS;
					registroAIngresar.IdConcepto 							= DatoEntrada.IdConcepto;
					registroAIngresar.IdTipoOperacion 						= DatoEntrada.IdTipoOperacion;
					registroAIngresar.IdTipoProducto 						= DatoEntrada.IdTipoProducto;
					registroAIngresar.IdOperacion 							= DatoEntrada.IdOperacion == null? 0L:DatoEntrada.IdOperacion;
					registroAIngresar.IdOperacionMesa						= DatoEntrada.IdOperacion == null? 0L:DatoEntrada.IdOperacion;
					registroAIngresar.IdMesa 								= DatoEntrada.IdMesa == null?0L:DatoEntrada.IdMesa;
					registroAIngresar.IdBolsa 								= DatoEntrada.IdBolsa == null? "":DatoEntrada.IdBolsa;
					registroAIngresar.IdSucursal 							= DatoEntrada.IdSucursal;
					registroAIngresar.IdAgente 								= DatoEntrada.IdAgente;
					registroAIngresar.IdCondicionLiquidacion 				= DatoEntrada.IdCondicionLiquidacion;
					registroAIngresar.FechaLiquidacion 						= DatoEntrada.FechaLiquidacion;
					//registroAIngresar.EsIntraday 							= InfoIngreso.EsIntraday;
					//registroAIngresar.UsaGarantiaEspecifica 				= InfoIngreso.UsaGarantiaEspecifica;
					registroAIngresar.Glosa 								= DatoEntrada.Glosa;
					registroAIngresar.IdUsuarioIngreso						= DatoEntrada.IdUsuario;
					//registroAIngresar.IdModulo 								= in.p_IdModulo;
					//registroAIngresar.TipoCarteraCorredor 					= InfoIngreso.TipoCarteraCorredor;
					//registroAIngresar.TipoCarteraNormativa 					= InfoIngreso.TipoCarteraNormativa;
					//registroAIngresar.FechaCurse 							= InfoIngreso.FechaCurse;
					//registroAIngresar.TipoFactorAjuste 						= InfoIngreso.TipoFactorAjuste;
					//registroAIngresar.ValorFactorAjusteaFechaCurse 			= InfoIngreso.ValorFactorAjusteaFechaCurse;
					//registroAIngresar.CostoEnFactorAjuste 					= InfoIngreso.CostoEnFactorAjuste;
					//registroAIngresar.IdClasificacionTipoInstrumento 		= InfoIngreso.IdClasificacionTipoInstrumento;
					//registroAIngresar.IdTipoInstrumento 					= InfoIngreso.IdTipoInstrumento;
					registroAIngresar.IdInstrumento 						= DatoEntrada.IdInstrumento;
					//registroAIngresar.IdInstrumentoAnterior 				= InfoIngreso.IdInstrumentoAnterior;
					registroAIngresar.Nemotecnico 							= DatoEntrada.Nemotecnico;
					registroAIngresar.IdEmisor 								= DatoEntrada.IdEmisor;
					registroAIngresar.NemoSVS 								= DatoEntrada.NemoSVS;
					registroAIngresar.IdTipoReajuste 						= DatoEntrada.IdTipoReajusteInstrumento;
					registroAIngresar.IdMonedaEmision 						= DatoEntrada.IdMonedaEmision;
					//registroAIngresar.FechaEmisionInstrumento 				= InfoIngreso.FechaEmisionInstrumento;
					registroAIngresar.FechaVencimientoInstrumento 			= DatoEntrada.FechaVencimientoInstrumento;
					registroAIngresar.EsFungible 							= DatoEntrada.EsFungible;
					//registroAIngresar.ValorizaPor 							= InfoIngreso.ValorizaPor;
					//registroAIngresar.TipoValorizacion 						= InfoIngreso.TipoValorizacion;
					//registroAIngresar.TieneCorteDeCupon 					= InfoIngreso.TieneCorteDeCupon;
					registroAIngresar.IdMonedaInstrumento 					= DatoEntrada.IdMonedaInstrumento;
					//registroAIngresar.Tamano 								= InfoIngreso.Tamano;
					registroAIngresar.IdBoveda 								= DatoEntrada.IdBoveda;
					//registroAIngresar.CuentaBoveda 							= InfoIngreso.CuentaBoveda;
					//registroAIngresar.ISIN 									= InfoIngreso.ISIN;					
					registroAIngresar.Cantidad 								= DatoEntrada.Cantidad;
					registroAIngresar.CorteMinimo 							= DatoEntrada.CorteMinimo;
					registroAIngresar.ValorRescate 							= DatoEntrada.ValorRescate;
					//registroAIngresar.PrecioAPlazo 							= InfoIngreso.PrecioAPlazo;
					//registroAIngresar.PrecioSucio 							= InfoIngreso.PrecioSucio;
					registroAIngresar.PrecioOperacion 							= DatoEntrada.PrecioCompra;
					registroAIngresar.TasaOperacion 							= DatoEntrada.TasaCompra;
					//registroAIngresar.MontoCompra 							= InfoIngreso.MontoCompra;					
					registroAIngresar.MontoOperacionMonedaInstrumento 			= DatoEntrada.MontoCompraMonedaInstrumento;
					//registroAIngresar.MontoAPlazo 							= InfoIngreso.MontoAPlazo;
					//registroAIngresar.MontoAPlazoMonedaInstrumento 			= InfoIngreso.MontoAPlazoMonedaInstrumento;
					//registroAIngresar.IdCarga 								= DatoEntrada.IdCarga;
					//registroAIngresar.IdSecuenciaCarteraMigracion 			= DatoEntrada.IdSecuenciaCarteraMigracion;
					//registroAIngresar.CodigoPeriodo 						= DatoEntrada.CodigoPeriodo;
					//registroAIngresar.EsVentaCorta							= DatoEntrada.EsVentaCorta;
					//registroAIngresar.IdRegimenFondoMutuo					= DatoEntrada.IdRegimenFondoMutuo;
					//registroAIngresar.EsVentaDescubierta                    = DatoEntrada.EsVentaDescubierta;
					//registroAIngresar.FechaOrigenCompra						= DatoEntrada.FechaOrigenCompra;
					registroAIngresar.IdCorredoraContraparte					= DatoEntrada.IdCorredora == null?0L:DatoEntrada.IdCorredora;
					
					indxCNegativa ++;
					paramsActualizacionCNegativa.add(registroAIngresar);
					
					
					//VALORIZACION
					ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
					
					//TODO NO USAR COPY POJO
					Utils.copyPojo(registroAIngresar, ValorizarCustodia);
					ValorizarCustodia.IdCustodia					= registroAIngresar.IdCarteraNegativa;
					ValorizarCustodia.EsRegistroNuevo				= DatoEntrada.EsRegistroNuevo;
					ValorizarCustodia.GenerarNuevoFolioCustodia		= DatoEntrada.GenerarNuevoFolioCustodia;
					ValorizarCustodia.TablaOrigen					= DatoEntrada.TablaAfectada;
					ValorizarCustodia.IdMonedaEmisionInstrumento 	= DatoEntrada.IdMonedaEmision;
					ValorizarCustodia.IdTipoReajusteInstrumento 	= DatoEntrada.IdTipoReajusteInstrumento;
					ValorizarCustodia.TasaCompra 					= DatoEntrada.TasaCompra; 
					ValorizarCustodia.PrecioCompra 					= DatoEntrada.PrecioCompra;
					ValorizarCustodia.MontoCompra 					= DatoEntrada.MontoCompraMonedaInstrumento;
					ValorizarCustodia.IdTipoInstrumento				= InfoIngreso.IdTipoInstrumento;
					ValorizarCustodia.IdClasificacionTipoInstrumento= InfoIngreso.IdClasificacionTipoInstrumento;
					ValorizarCustodia.FechaVencimientoInstrumento	= InfoIngreso.FechaVencimientoInstrumento;
					
					paramsValorizarCustodia.add(ValorizarCustodia);
										
					//Informacion salida
					IngresoCustodiaSalida.ObjDatosSalida InfoSalida = new IngresoCustodiaSalida.ObjDatosSalida();
					Utils.copyPojo(registroAIngresar, InfoSalida);
					InfoSalida.IdGenerarMovimientoCustodia = DatoEntrada.IdGenerarMovimientoCustodia;
					InfoSalida.TablaOrigen 			= DatoEntrada.TablaAfectada;
					InfoSalida.IdCustodia			= registroAIngresar.IdCarteraNegativa;
					InfoSalida.DesplegarMensaje 	= DatoEntrada.PermiteCertificacionPorUsuario != null && DatoEntrada.PermiteCertificacionPorUsuario.equals("N") ? "N" : "S";
					out.P_OUT_DATOSSALIDA.add(InfoSalida);
					
				};
				
				if ( DatoEntrada.TablaAfectada.equalsIgnoreCase("GARANTIA")){					
					ObjTablaCartera InfoCartera = (ObjTablaCartera) hmCarteras.get(DatoEntrada.IdCarteraOrigen);	
					ObjTablaGarantia InfoGarantia = (ObjTablaGarantia) hmGarantias.get(DatoEntrada.IdCustodia);	
					ObjTablaGarantia registroAIngresar = new ObjTablaGarantia(); 
					
					registroAIngresar.IdGarantia 				= SecIdGarantia.get( indxGarantia );
					registroAIngresar.FolioGarantia 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? SecFolioGarantia.get( indxGarantia ) : (DatoEntrada.GenerarNuevoFolioCustodia.equals("S") ? SecFolioGarantia.get( indxGarantia ) :InfoGarantia.FolioGarantia);
					registroAIngresar.ExigirGarantia 		    = DatoEntrada.ExigirGarantia;
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.FolioCarteraOrigen 		= InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.FolioCartera : InfoGarantia.FolioCarteraOrigen) : null;
					
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.IdCarteraOrigen 			= InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.IdCartera : InfoGarantia.IdCarteraOrigen) : null;
					registroAIngresar.FolioCarteraGarantizada 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FolioCarteraGarantizada : InfoGarantia.FolioCarteraGarantizada;
					registroAIngresar.IdInstrumentoCarteraGarantizada = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdInstrumentoCarteraGarantizada : InfoGarantia.IdInstrumentoCarteraGarantizada;
					registroAIngresar.EsEspecifica 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.EsEspecifica : InfoGarantia.EsEspecifica;
					registroAIngresar.IdContraparteGarantia 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdContraparteGarantia : InfoGarantia.IdContraparteGarantia;
					registroAIngresar.RazonSocialContraparteGarantia = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.RazonSocialContraparteGarantia : InfoGarantia.RazonSocialContraparteGarantia;
					registroAIngresar.CuentaContraparteGarantia = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CuentaContraparteGarantia : InfoGarantia.CuentaContraparteGarantia;
					registroAIngresar.IdOrigenGarantia 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdOrigenGarantia : InfoGarantia.IdOrigenGarantia;
					registroAIngresar.IdEspecieGarantia			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdEspecieGarantia : InfoGarantia.IdEspecieGarantia;
					//registroAIngresar.FechaModificacionRegistro = ;
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.PrecioCompraOperacionOrigen 	= InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.PrecioCompra : InfoGarantia.PrecioCompraOperacionOrigen) : null;
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.TasaCompraOperacionOrigen		= InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.TasaCompra : InfoGarantia.TasaCompraOperacionOrigen) : null;
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.PrecioPromedioCompraOperacionOrigen = InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.INGPrecioPromedioCompra : InfoGarantia.PrecioPromedioCompraOperacionOrigen) : null;
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.MontoPrecioPromedioCompraOperacionOrigen = InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.INGMontoPrecioPromedioCompra : InfoGarantia.MontoPrecioPromedioCompraOperacionOrigen) : null;
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.MontoPrecioPromedioMonedaInstrumentoOperacionOrigen = InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ?  InfoCartera.INGMontoPrecioPromedioMonedaInstrumento : InfoGarantia.MontoPrecioPromedioMonedaInstrumentoOperacionOrigen) : null;
					if ( InfoCartera != null || InfoGarantia != null) registroAIngresar.MontoPrecioPromedioMonedaAdicionalOperacionOrigen = InfoIngreso.IdOrigenGarantia.equals("CUS") ? (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.INGMontoPrecioPromedioMonedaAdicional : InfoGarantia.MontoPrecioPromedioMonedaAdicionalOperacionOrigen) : null;
					registroAIngresar.IdEmpresa 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? in.p_IdEmpresa : InfoGarantia.IdEmpresa;
					registroAIngresar.FechaOperacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaOperacion : InfoGarantia.FechaOperacion;
					registroAIngresar.FechaVencimientoOperacion = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaVencimientoOperacion : InfoGarantia.FechaVencimientoOperacion;
					registroAIngresar.PlazoEnDiasOperacion 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.PlazoEnDiasOperacion : InfoGarantia.PlazoEnDiasOperacion;
					registroAIngresar.IdCliente 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCliente : InfoGarantia.IdCliente;
					registroAIngresar.IdPortafolio 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdPortafolio : InfoGarantia.IdPortafolio;
					registroAIngresar.IdCuentaCustodioPortafolio = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCuentaCustodioPortafolio : InfoGarantia.IdCuentaCustodioPortafolio;
					registroAIngresar.IdEstado 					= "VIG";
					registroAIngresar.IdTipoCanalIngreso 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoCanalIngreso : InfoGarantia.IdTipoCanalIngreso;
					registroAIngresar.IdMonedaSistema 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaSistema : InfoGarantia.IdMonedaSistema;
					registroAIngresar.IdMonedaAdicional 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaAdicional : InfoGarantia.IdMonedaAdicional;
					registroAIngresar.FechaFactorMonedaAdicional = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoGarantia.FechaFactorMonedaAdicional;
					registroAIngresar.ValorFactorMonedaAdicional = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoGarantia.ValorFactorMonedaAdicional;					
					registroAIngresar.IdConcepto 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdConcepto : InfoGarantia.IdConcepto;
					registroAIngresar.IdTipoComprobante 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoComprobante : InfoGarantia.IdTipoComprobante;					
					registroAIngresar.FolioComprobante 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FolioComprobante: InfoGarantia.FolioComprobante;//(InfoIngreso.IdTipoComprobante.equals("CUS") ? SecFolioComprobanteCustodia.get(indxGarantia) : InfoIngreso.FolioComprobante) : InfoGarantia.FolioComprobante;
					registroAIngresar.FechaComprobante 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaComprobante : InfoGarantia.FechaComprobante;					
					//registroAIngresar.FolioExternoReferencia = ;
					registroAIngresar.IdSucursal 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdSucursal : InfoGarantia.IdSucursal;
					registroAIngresar.IdAgente 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdAgente : InfoGarantia.IdAgente;
					registroAIngresar.IdCondicionLiquidacion 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCondicionLiquidacion : InfoGarantia.IdCondicionLiquidacion;
					registroAIngresar.FechaLiquidacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaLiquidacion : InfoGarantia.FechaLiquidacion;
					registroAIngresar.IdUsuario 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdUsuario : InfoGarantia.IdUsuario;
					registroAIngresar.IdModulo 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? in.p_IdModulo : InfoGarantia.IdModulo;
					registroAIngresar.IdMovimientoCarteraEnTransito 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdMovimientoCarteraEnTransitoOrigen : InfoGarantia.IdMovimientoCarteraEnTransito;
					registroAIngresar.IdClasificacionTipoInstrumento 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdClasificacionTipoInstrumento : InfoGarantia.IdClasificacionTipoInstrumento;
					registroAIngresar.IdTipoInstrumento 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoInstrumento : InfoGarantia.IdTipoInstrumento;
					registroAIngresar.IdInstrumento 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdInstrumento : InfoGarantia.IdInstrumento;
					//registroAIngresar.IdInstrumentoAnterior = ;
					registroAIngresar.Nemotecnico 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Nemotecnico : InfoGarantia.Nemotecnico;
					registroAIngresar.IdEmisor 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdEmisor : InfoGarantia.IdEmisor;
					registroAIngresar.NemoSVS 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.NemoSVS : InfoGarantia.NemoSVS;
					registroAIngresar.IdTipoReajuste 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoReajuste : InfoGarantia.IdTipoReajuste;
					registroAIngresar.IdMonedaEmision 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaEmision : InfoGarantia.IdMonedaEmision;
					registroAIngresar.FechaEmisionInstrumento 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaEmisionInstrumento : InfoGarantia.FechaEmisionInstrumento;
					registroAIngresar.FechaVencimientoInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaVencimientoInstrumento : InfoGarantia.FechaVencimientoInstrumento;
					registroAIngresar.EsFungible 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.EsFungible : InfoGarantia.EsFungible;
					registroAIngresar.ValorizaPor 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorizaPor : InfoGarantia.ValorizaPor;
					registroAIngresar.TipoValorizacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TipoValorizacion : InfoGarantia.TipoValorizacion;
					registroAIngresar.TieneCorteDeCupon 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TieneCorteDeCupon : InfoGarantia.TieneCorteDeCupon;
					registroAIngresar.CodigoIFRS 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CodigoIFRS : InfoGarantia.CodigoIFRS;
					registroAIngresar.IdMonedaInstrumento 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaInstrumento : InfoGarantia.IdMonedaInstrumento;
					registroAIngresar.Tamano 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Tamano : InfoGarantia.Tamano;
					registroAIngresar.ISIN 						= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ISIN : InfoGarantia.ISIN;
					registroAIngresar.Cantidad 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Cantidad : InfoGarantia.Cantidad;
					registroAIngresar.CorteMinimo 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CorteMinimo : InfoGarantia.CorteMinimo;
					registroAIngresar.ValorRescate 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorRescate : InfoGarantia.ValorRescate;
					registroAIngresar.PrecioGarantia 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.PrecioCompra : InfoGarantia.PrecioGarantia;
					registroAIngresar.TasaGarantia 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TasaCompra : InfoGarantia.TasaGarantia;
					registroAIngresar.MontoGarantia 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompra : InfoGarantia.MontoGarantia;
					registroAIngresar.MontoGarantiaMonedaInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompraMonedaInstrumento : InfoGarantia.MontoGarantiaMonedaInstrumento;
					registroAIngresar.MontoGarantiaMonedaAdicional = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoGarantia.MontoGarantiaMonedaInstrumento;
					registroAIngresar.IdCarga 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdCarga : InfoGarantia.IdCarga;
					registroAIngresar.IdSecuenciaCarteraMigracion = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdSecuenciaCarteraMigracion : InfoGarantia.IdSecuenciaCarteraMigracion;
					registroAIngresar.CodigoPeriodo 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.CodigoPeriodo : InfoGarantia.CodigoPeriodo;
					//-----------------------------------------------------------------------------------------------------------------------------------------
					registroAIngresar.INGFechaOperacion 		= (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") || (DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("N") && "RETGTA".equalsIgnoreCase(InfoIngreso.IdTipoOperacion))) ? InfoIngreso.FechaOperacion : InfoGarantia.FechaOperacion;
					registroAIngresar.INGIdCondicionLiquidacion = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdCondicionLiquidacion : InfoGarantia.IdCondicionLiquidacion;
					registroAIngresar.INGFechaLiquidacion 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaLiquidacion : InfoGarantia.FechaLiquidacion;
					registroAIngresar.INGCantidad 				= InfoIngreso.Cantidad;
					registroAIngresar.INGValorRescate 			= InfoIngreso.ValorRescate;
					registroAIngresar.INGPrecioGarantia 		= InfoIngreso.PrecioCompra;
					if(InfoCartera != null) {
						registroAIngresar.INGTasaGarantia	 		=  DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoCartera.TasaCompra :  InfoIngreso.TasaCompra;
					}
					else {
						registroAIngresar.INGTasaGarantia	 		= InfoIngreso.TasaCompra;
					}
					registroAIngresar.INGMontoATasaGarantiaActual				= InfoIngreso.MontoCompra;
					registroAIngresar.INGMontoGarantiaActualMonedaInstrumento	= InfoIngreso.MontoCompraMonedaInstrumento;
					registroAIngresar.EGRIdOperacion							= InfoIngreso.IdOperacion;

					indxGarantia ++;
					paramsActualizacionGarantia.add(registroAIngresar);
					
					//VALORIZACION
					//if (registroAIngresar.IdOrigenGarantia.equals("CUS")){
						ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
						Utils.copyPojo(registroAIngresar, ValorizarCustodia);
						ValorizarCustodia.IdCustodia				= registroAIngresar.IdGarantia;
						ValorizarCustodia.EsRegistroNuevo			= DatoEntrada.EsRegistroNuevo;
						ValorizarCustodia.GenerarNuevoFolioCustodia	= DatoEntrada.GenerarNuevoFolioCustodia;
						ValorizarCustodia.TablaOrigen				= DatoEntrada.TablaAfectada;
						ValorizarCustodia.FechaEmisionInstrumento 	= registroAIngresar.FechaEmisionInstrumento;
						ValorizarCustodia.FechaVencimientoInstrumento 	= registroAIngresar.FechaVencimientoInstrumento;
						ValorizarCustodia.IdMonedaEmisionInstrumento 	= registroAIngresar.IdMonedaEmision;
						ValorizarCustodia.IdTipoReajusteInstrumento = registroAIngresar.IdTipoReajuste;
						ValorizarCustodia.Cantidad					= registroAIngresar.INGCantidad;
						ValorizarCustodia.ValorRescate				= registroAIngresar.INGValorRescate;
						ValorizarCustodia.TasaCompra 				= registroAIngresar.TasaCompraOperacionOrigen; 
						ValorizarCustodia.PrecioCompra 				= registroAIngresar.PrecioCompraOperacionOrigen;
						ValorizarCustodia.TasaGarantia 				= registroAIngresar.INGTasaGarantia; 
						ValorizarCustodia.PrecioGarantia 			= registroAIngresar.INGPrecioGarantia;
						ValorizarCustodia.MontoCompra 				= registroAIngresar.INGMontoCompraActualMonedaInstrumento;
						ValorizarCustodia.MontoCompraGarantia		= registroAIngresar.INGMontoGarantiaActualMonedaInstrumento;
						paramsValorizarCustodia.add(ValorizarCustodia);
					//};
					
					//Informacion salida
					IngresoCustodiaSalida.ObjDatosSalida InfoSalida = new IngresoCustodiaSalida.ObjDatosSalida();
					InfoSalida.IdGenerarMovimientoCustodia = DatoEntrada.IdGenerarMovimientoCustodia;
					InfoSalida.TablaOrigen 		= DatoEntrada.TablaAfectada;
					InfoSalida.IdCustodia		= registroAIngresar.IdGarantia;
					InfoSalida.FolioCustodia	= registroAIngresar.FolioGarantia;
					Utils.copyPojo(registroAIngresar, InfoSalida);
					out.P_OUT_DATOSSALIDA.add(InfoSalida);
				};
								
				if ( DatoEntrada.TablaAfectada.equalsIgnoreCase("SUBYACENTE")){
					ObjTablaCartera InfoCartera = (ObjTablaCartera) hmCarteras.get(DatoEntrada.IdCarteraOrigen);	
					ObjTablaSubyacente InfoSubyacente = (ObjTablaSubyacente) hmSubyacentes.get(DatoEntrada.IdCustodia);
					ObjTablaSubyacente registroAIngresar = new ObjTablaSubyacente();
					
					registroAIngresar.IdSubyacente 				= SecIdSubyacente.get( indxSubyacente );
					registroAIngresar.FolioSubyacente 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? SecFolioSubyacente.get( indxSubyacente ) : (DatoEntrada.GenerarNuevoFolioCustodia.equals("S") ? SecFolioSubyacente.get( indxSubyacente ) : InfoSubyacente.FolioSubyacente);
					registroAIngresar.FolioCarteraRelacionada 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.FolioCarteraRelacionada : InfoSubyacente.FolioCarteraRelacionada;
					//registroAIngresar.FechaModificacionRegistro = ;
					
					
					
					if(DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S")) {
						
						if (("VENPAC").equals(DatoEntrada.IdTipoOperacion) && DatoEntrada.EsNominal.equals("S")) {
							registroAIngresar.IdCarteraRetirada = null != InfoCartera ? InfoCartera.IdCartera : 0L;
						}else if(DatoEntrada.EsNominal.equals("S") || 
								DatoEntrada.IdTipoOperacion.equals("COMRRV") || 
								DatoEntrada.IdTipoOperacion.equals("COMRRF") || 
								DatoEntrada.IdTipoOperacion.equals("PRESTA")) {							
							
							registroAIngresar.IdCarteraRetirada = 0L;
							
						}else {
							if(InfoCartera != null) {
								registroAIngresar.IdCarteraRetirada = InfoCartera.IdCartera;
							} else {
								registroAIngresar.IdCarteraRetirada = null;
							}
						}
					} else {
						registroAIngresar.IdCarteraRetirada = InfoSubyacente.IdCarteraRetirada;
					}
					
					registroAIngresar.IdMovimientoCarteraEnTransito 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? DatoEntrada.IdMovimientoCarteraEnTransitoOrigen : InfoSubyacente.IdMovimientoCarteraEnTransito;
					registroAIngresar.PrecioCompraOperacionRelacionada 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? (DatoEntrada.EsNominal.equals("S") || DatoEntrada.IdTipoOperacion.equals("COMRRV")|| DatoEntrada.IdTipoOperacion.equals("COMRRF") || DatoEntrada.IdTipoOperacion.equals("COMSIM") || DatoEntrada.IdTipoOperacion.equals("PRESTA") || DatoEntrada.IdTipoOperacion.equals("EMPRES") ? DatoEntrada.PrecioCompra : (InfoCartera != null)?InfoCartera.INGPrecioCompra : null) : InfoSubyacente.PrecioCompraOperacionRelacionada;
					registroAIngresar.TasaCompraOperacionRelacionada 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? (DatoEntrada.EsNominal.equals("S") || DatoEntrada.IdTipoOperacion.equals("COMRRV")|| DatoEntrada.IdTipoOperacion.equals("COMRRF") || DatoEntrada.IdTipoOperacion.equals("COMSIM") || DatoEntrada.IdTipoOperacion.equals("PRESTA") || DatoEntrada.IdTipoOperacion.equals("EMPRES") ? (DatoEntrada.IdTipoOperacion.equals("PRESTA") || DatoEntrada.IdTipoOperacion.equals("EMPRES")) ? DatoEntrada.TasaCompraOperacionRelacionada : DatoEntrada.PrecioCompra : (InfoCartera != null)?InfoCartera.INGTasaCompra : null) : InfoSubyacente.TasaCompraOperacionRelacionada;
					registroAIngresar.PrecioPromedioCompraOperacionRelacionada = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? (DatoEntrada.EsNominal.equals("S") || DatoEntrada.IdTipoOperacion.equals("COMRRV")|| DatoEntrada.IdTipoOperacion.equals("COMRRF") || DatoEntrada.IdTipoOperacion.equals("COMSIM") || DatoEntrada.IdTipoOperacion.equals("PRESTA") ? BigDecimal.ZERO : (InfoCartera != null)?InfoCartera.INGPrecioPromedioCompra : null) : InfoSubyacente.PrecioPromedioCompraOperacionRelacionada;
					registroAIngresar.MontoPrecioPromedioCompraOperacionRelacionada = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? (DatoEntrada.EsNominal.equals("S") || DatoEntrada.IdTipoOperacion.equals("COMRRV")|| DatoEntrada.IdTipoOperacion.equals("COMRRF") || DatoEntrada.IdTipoOperacion.equals("COMSIM") || DatoEntrada.IdTipoOperacion.equals("PRESTA") ? BigDecimal.ZERO : (InfoCartera != null)?InfoCartera.INGMontoPrecioPromedioCompra: null) : InfoSubyacente.MontoPrecioPromedioCompraOperacionRelacionada;
					registroAIngresar.MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? (DatoEntrada.EsNominal.equals("S") || DatoEntrada.IdTipoOperacion.equals("COMRRV")|| DatoEntrada.IdTipoOperacion.equals("COMRRF") || DatoEntrada.IdTipoOperacion.equals("COMSIM") || DatoEntrada.IdTipoOperacion.equals("PRESTA") ? BigDecimal.ZERO : (InfoCartera != null)?InfoCartera.INGMontoPrecioPromedioMonedaInstrumento: null) : InfoSubyacente.MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada;
					registroAIngresar.MontoPrecioPromedioMonedaAdicionalOperacionRelacionada = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? (DatoEntrada.EsNominal.equals("S") || DatoEntrada.IdTipoOperacion.equals("COMRRV")|| DatoEntrada.IdTipoOperacion.equals("COMRRF") || DatoEntrada.IdTipoOperacion.equals("COMSIM") || DatoEntrada.IdTipoOperacion.equals("PRESTA") ? BigDecimal.ZERO : (InfoCartera != null)?InfoCartera.INGMontoPrecioPromedioMonedaAdicional : null) : InfoSubyacente.MontoPrecioPromedioMonedaAdicionalOperacionRelacionada;
					registroAIngresar.IdEstado 					= "VIG";
					registroAIngresar.IdTipoCanalIngreso 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoCanalIngreso : InfoSubyacente.IdTipoCanalIngreso;
					registroAIngresar.IdClasificacionTipoInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdClasificacionTipoInstrumento : InfoSubyacente.IdClasificacionTipoInstrumento;
					registroAIngresar.IdTipoInstrumento 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoInstrumento : InfoSubyacente.IdTipoInstrumento;
					registroAIngresar.IdInstrumento 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdInstrumento : InfoSubyacente.IdInstrumento;
					//registroAIngresar.IdInstrumentoAnterior = ;
					registroAIngresar.Nemotecnico 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Nemotecnico : InfoSubyacente.Nemotecnico;
					registroAIngresar.IdEmisor 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdEmisor : InfoSubyacente.IdEmisor;
					registroAIngresar.NemoSVS 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.NemoSVS : InfoSubyacente.NemoSVS;
					registroAIngresar.IdTipoReajuste 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdTipoReajuste : InfoSubyacente.IdTipoReajuste;
					registroAIngresar.IdMonedaEmision 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaEmision : InfoSubyacente.IdMonedaEmision;
					registroAIngresar.FechaEmisionInstrumento 	= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaEmisionInstrumento : InfoSubyacente.FechaEmisionInstrumento;
					registroAIngresar.FechaVencimientoInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaVencimientoInstrumento : InfoSubyacente.FechaVencimientoInstrumento;
					registroAIngresar.EsFungible 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.EsFungible : InfoSubyacente.EsFungible;
					registroAIngresar.ValorizaPor 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorizaPor : InfoSubyacente.ValorizaPor;
					registroAIngresar.TipoValorizacion 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TipoValorizacion : InfoSubyacente.TipoValorizacion;
					registroAIngresar.TieneCorteDeCupon 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TieneCorteDeCupon : InfoSubyacente.TieneCorteDeCupon;
					registroAIngresar.IdMonedaInstrumento 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.IdMonedaInstrumento : InfoSubyacente.IdMonedaInstrumento;
					registroAIngresar.Tamano 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Tamano : InfoSubyacente.Tamano;
					registroAIngresar.ISIN 						= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ISIN : InfoSubyacente.ISIN;
					registroAIngresar.Cantidad 					= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.Cantidad : InfoSubyacente.Cantidad;
					registroAIngresar.CorteMinimo 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.CorteMinimo : InfoSubyacente.CorteMinimo;
					registroAIngresar.ValorRescate 				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.ValorRescate : InfoSubyacente.ValorRescate;
					registroAIngresar.PrecioSubyacente 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.PrecioCompra : InfoSubyacente.PrecioSubyacente;
					registroAIngresar.TasaSubyacente 			= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.TasaCompra : InfoSubyacente.TasaSubyacente;
					
					//marca inicial carteras
					registroAIngresar.EsPrimeraCartera				= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? "S" : null;
					
					//Si el IdTipoOperacion es COMPAC y VENPAC se almacena el monto a tasa pacto
					if("COMPAC".equalsIgnoreCase(InfoIngreso.IdTipoOperacion) || "VENPAC".equalsIgnoreCase(InfoIngreso.IdTipoOperacion)) {
						registroAIngresar.MontoSubyacente		= InfoIngreso.ValorRescate;
					}else {
						registroAIngresar.MontoSubyacente 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompra : InfoSubyacente.MontoSubyacente;
					}
					registroAIngresar.MontoSubyacenteMonedaInstrumento = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.MontoCompraMonedaInstrumento : InfoSubyacente.MontoSubyacenteMonedaInstrumento;
					registroAIngresar.MontoSubyacenteMonedaAdicional = DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? null : InfoSubyacente.MontoSubyacenteMonedaInstrumento;
					registroAIngresar.IdCarga 					= DatoEntrada.IdCarga;
					//------------------------------------------------------------------------------------------------
					registroAIngresar.INGFechaOperacion 		= DatoEntrada.EsRegistroNuevo.equalsIgnoreCase("S") ? InfoIngreso.FechaOperacion : InfoSubyacente.INGFechaOperacion;
					registroAIngresar.INGCantidad 				= InfoIngreso.Cantidad;
					registroAIngresar.INGValorRescate	 		= InfoIngreso.ValorRescate;
					registroAIngresar.INGPrecioSubyacente 		= InfoIngreso.PrecioCompra;
					registroAIngresar.INGTasaSubyacente 		= InfoIngreso.TasaCompra;
					registroAIngresar.INGMontoATasaSubyacenteActual = InfoIngreso.MontoCompra;
					registroAIngresar.INGMontoSubyacenteActualMonedaInstrumento	= InfoIngreso.MontoCompraMonedaInstrumento;
					if ("PRES".equalsIgnoreCase(DatoEntrada.IdTipoProducto)) {
						registroAIngresar.INGMontoCompraActualMonedaInstrumento	= InfoIngreso.MontoCompraMonedaInstrumento;
						registroAIngresar.INGMontoATasaCompraActual = InfoIngreso.MontoCompra;
					}

					registroAIngresar.IdTipoOperacion 			= InfoIngreso.IdTipoOperacion;
					registroAIngresar.FechaVencimientoOperacion	= InfoIngreso.FechaVencimientoOperacion;
					
					indxSubyacente ++;
					paramsActualizacionSubyacente.add(registroAIngresar);
					
					//VALORIZACION
					ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
					Utils.copyPojo(registroAIngresar, ValorizarCustodia);
					ValorizarCustodia.IdCustodia					= registroAIngresar.IdSubyacente;
					ValorizarCustodia.EsRegistroNuevo				= DatoEntrada.EsRegistroNuevo;
					ValorizarCustodia.GenerarNuevoFolioCustodia		= DatoEntrada.GenerarNuevoFolioCustodia;
					ValorizarCustodia.TablaOrigen					= DatoEntrada.TablaAfectada;
					ValorizarCustodia.FechaOperacion				= registroAIngresar.INGFechaOperacion;
					ValorizarCustodia.FechaEmisionInstrumento 		= registroAIngresar.FechaEmisionInstrumento;
					ValorizarCustodia.FechaVencimientoInstrumento 	= registroAIngresar.FechaVencimientoInstrumento;
					ValorizarCustodia.IdMonedaEmisionInstrumento 	= registroAIngresar.IdMonedaEmision;
					ValorizarCustodia.IdTipoReajusteInstrumento 	= registroAIngresar.IdTipoReajuste;
					ValorizarCustodia.Cantidad						= registroAIngresar.INGCantidad;
					ValorizarCustodia.ValorRescate					= registroAIngresar.INGValorRescate;
					ValorizarCustodia.TasaCompra 					= registroAIngresar.TasaCompraOperacionRelacionada; 
					ValorizarCustodia.PrecioCompra 					= registroAIngresar.PrecioCompraOperacionRelacionada;
					ValorizarCustodia.TasaSubyacente 				= registroAIngresar.INGTasaSubyacente; 
					ValorizarCustodia.PrecioSubyacente 				= registroAIngresar.INGPrecioSubyacente;
					ValorizarCustodia.MontoCompra 					= registroAIngresar.INGMontoCompraActualMonedaInstrumento;
					ValorizarCustodia.MontoCompraSubyacente			= InfoIngreso.MontoCompraMonedaInstrumento;					
					ValorizarCustodia.FechaVencimientoOperacion		= DatoEntrada.FechaVencimientoOperacion;
					
					if ("PRES".equalsIgnoreCase(DatoEntrada.IdTipoProducto)) {
						if ("RF".equalsIgnoreCase(InfoIngreso.IdClasificacionTipoInstrumento)) 
							ValorizarCustodia.TasaCompra = registroAIngresar.INGTasaSubyacente;
					}
					
					paramsValorizarCustodia.add(ValorizarCustodia);
					
					//Informacion salida
					IngresoCustodiaSalida.ObjDatosSalida InfoSalida = new IngresoCustodiaSalida.ObjDatosSalida();
					InfoSalida.IdGenerarMovimientoCustodia = DatoEntrada.IdGenerarMovimientoCustodia;
					InfoSalida.TablaOrigen 		= DatoEntrada.TablaAfectada;
					InfoSalida.IdCustodia		= registroAIngresar.IdSubyacente;
					InfoSalida.FolioCustodia	= registroAIngresar.FolioSubyacente;
					Utils.copyPojo(registroAIngresar, InfoSalida);
					out.P_OUT_DATOSSALIDA.add(InfoSalida);
				};
			};
			
			//05 - EJECUTAR CAMBIOS
			if ( in.p_SimularOperacion.equalsIgnoreCase("N") ){
				if (paramsActualizacionTransito.size() > 0){
					
					//SETEAR DEPENDENCIAS ENTRE MOVIMIENTOS EN TRANSITO
					for (ObjTablaTransito mov: paramsActualizacionTransito){
						if (mov.IdMovimientoCarteraEnTransitoRelacionado == null) continue;
						if (mov.IdMovimientoCarteraEnTransitoRelacionado != null && mov.IdMovimientoCarteraEnTransitoRelacionado.equals(0L)) continue;						
						
						for (ObjTablaTransito movOrigen: paramsActualizacionTransito){
							if (!movOrigen.IdGenerarMovimientoCustodia.equals(mov.IdMovimientoCarteraEnTransitoRelacionado)) continue;
							
							mov.IdMovimientoCarteraEnTransitoRelacionado = movOrigen.IdMovimientoCarteraEnTransito;
						};
					};
					
					
					sql.batch(con, SQL_InsertTransito, paramsActualizacionTransito, ObjTablaTransito.class);
				};				
				if (paramsActualizacionCartera.size() > 0){
					sql.batch(con, SQL_InsertCartera, paramsActualizacionCartera, ObjTablaCartera.class);
				};
				if (paramsActualizacionCNegativa.size() > 0){
					sql.batch(con, SQL_InsertNegativo, paramsActualizacionCNegativa, ObjTablaCarteraNegativa.class);
				};
				if (paramsActualizacionGarantia.size() > 0){ 
					sql.batch(con, SQL_InsertGarantia, paramsActualizacionGarantia, ObjTablaGarantia.class);
				};
				if (paramsActualizacionSubyacente.size() > 0){ 
					sql.batch(con, SQL_InsertSubyacente, paramsActualizacionSubyacente, ObjTablaSubyacente.class);
				};
			};
			
			//06 - VALORIZAR REGISTROS INGRESADOS
			paramsValorizarCustodia.stream().forEach(t -> t.OrigenProcesamiento = "INGRESO_CUSTODIA");
			ValorizarCustodia.ValorizarCustodiaEntrada VIE = new ValorizarCustodia.ValorizarCustodiaEntrada();				
			VIE.p_IdEmpresa					= in.p_IdEmpresa;	
			VIE.p_IdUsuario					= in.p_IdUsuario;
			VIE.p_IdModulo					= "CUS";
			VIE.p_IdPais 					= empresa.IdPais;
			VIE.p_OrigenProcesamiento 		= "INGRESO_CUSTODIA";
			VIE.p_InstrumentosAValorizar 	= paramsValorizarCustodia;	
			
			if (in.p_SimularOperacion.equalsIgnoreCase("N") && paramsValorizarCustodia.size() > 0 && in.p_ValorizacionAutomatica.equalsIgnoreCase("S")){
						
				ValorizarCustodia VI = new ValorizarCustodia();
				ValorizarCustodia.ValorizarCustodiaSalida VIS = VI.logica(con, VIE);
				
				//Si la valorizacion tiene como origen los Vencimientos de Pactos se cambia el tipo de mensaje a ADVERTENCIA
				if(paramsValorizarCustodiaVCDPAC.size() > 0) {
					VIS.P_OUT_ERRORESCONTADOR = 0L;
					VIS.P_OUT_MENSAJES.stream().forEach(m -> m.tipo = "Registro no valorizado".equalsIgnoreCase(m.codigo) ? "ADVERTENCIA" : m.tipo);
					out.P_OUT_MENSAJES.addAll(VIS.P_OUT_MENSAJES);
				}
				
				if (!VIS.P_OUT_ERRORESCONTADOR.equals(0L)){
					out.P_OUT_MENSAJES.addAll(VIS.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;
				};
			};
			
			if (in.p_SimularOperacion.equalsIgnoreCase("N") && paramsValorizarCustodia.size() > 0 && in.p_ValorizacionAutomatica.equalsIgnoreCase("N")){
				CacheValorizacion.agregar(VIE);
			}
			
			
			//07 - PREPARAR SALIDA
			for (IngresoCustodiaSalida.ObjDatosSalida DS : out.P_OUT_DATOSSALIDA){
				if (DS.RegistroConProblemas.equalsIgnoreCase("S") )
					out.P_OUT_ERRORESCONTADOR = 1L;
			};
		}
		catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		return out;
	}
	
	public Boolean guardaMontoAPlazo (ObjDatosEntrada DatoEntrada) {
		Boolean guardaMonto = false;
		
		if("COMPAC".equals(DatoEntrada.IdTipoOperacion) || "VENPAC".equals(DatoEntrada.IdTipoOperacion)) {
			guardaMonto = true;
		}else {
			if(("S").equalsIgnoreCase(DatoEntrada.EsRegistroNuevo)) {
				guardaMonto = true;
			}
		}	
		
		return guardaMonto;
	}
	
	public boolean esRetornoMesa (String IdConcepto,Long IdOperacion) {
		boolean esOperacionMesa= false;
		
		if(("VCDPAC".equals(IdConcepto) || "ANUCPA".equals(IdConcepto)|| "ANUVPA".equals(IdConcepto)|| "ANTPAC".equals(IdConcepto)) && IdOperacion != null) {
			esOperacionMesa= true;
		}		
		return esOperacionMesa;
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		IngresoCustodiaEntrada jin=gson.fromJson(in.toString() ,IngresoCustodiaEntrada.class);
		IngresoCustodiaSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				/*String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"CUS\",\"p_FechaMovimiento\":\"2017-10-01\",\"p_TicketAtencion\":0,"
						+ "\"p_DatosEntrada\":["
							+ "{"
							+ "  \"IdGenerarMovimientoCustodia\": 1,"
							+ "  \"TablaAfectada\": \"CARTERA\"," 		
							+ "  \"EsRegistroNuevo\":\"S\","
							+ "  \"IdCliente\": 174994," 
							+ "  \"IdPortafolio\": 40739," 
							+ "  \"FechaOperacion\": \"2017-07-21 00:00:00.000\","
							+ "  \"FechaVencimientoOperacion\": \"0001-01-01 00:00:00.000\","
							+ "  \"IdTipoOperacion\": \"INGTIT\","
							+ "  \"IdAccionOperacion\": \"INGRESO\","
							+ "  \"IdConcepto\": \"INTIRV\","
							+ "  \"IdCondicionLiquidacion\": \"T0\"," 
							+ "  \"FechaLiquidacion\": \"2017-07-21 00:00:00.000\","
							+ "  \"IdTipoCanalIngreso\": \"AGE\"," 
							+ "  \"IdMonedaSistema\": \"PEN\"," 
							+ "  \"IdBolsa\": \"BVL\","
							+ "  \"IdUsuario\": 69," 
							+ "  \"IdAgente\": 69," 
							+ "  \"IdInstrumento\": 10000,"
							+ "  \"IdMonedaInstrumento\": \"PEN\","
							+ "  \"IdTipoReajuste\": \"PEN\"," 
							+ "  \"FechaEmisionInstrumento\": \"0001-01-01 00:00:00.000\"," 
							+ "  \"FechaVencimientoInstrumento\": \"0001-01-01 00:00:00.000\"," 
							+ "  \"EsFungible\": \"N\"," 	
							+ "  \"IdBoveda\": \"CAV\"," 
							+ "  \"Cantidad\": 5000,"
							+ "  \"CorteMinimo\": null," 
							+ "  \"ValorRescate\": null," 
							+ "  \"PrecioCompra\": 9," 
							+ "  \"TasaCompra\": 0," 
							+ "  \"MontoCompra\": 45000," 
							+ "  \"MontoCompraMonedaInstrumento\": 45000"
							+ "},"
							+ "{"
							+ "  \"IdGenerarMovimientoCustodia\": 2,"
							+ "  \"TablaAfectada\": \"CARTERA\","
							+ "  \"EsRegistroNuevo\":\"S\","
							+ "  \"IdCliente\": 174994," 
							+ "  \"IdPortafolio\": 40739," 
							+ "  \"FechaOperacion\": \"2017-07-21 00:00:00.000\","
							+ "  \"FechaVencimientoOperacion\": \"2026-12-19 00:00:00.000\","
							+ "  \"IdTipoOperacion\": \"INGTIT\","
							+ "  \"IdAccionOperacion\": \"INGRESO\","
							+ "  \"IdConcepto\": \"INTIRF\","
							+ "  \"IdCondicionLiquidacion\": \"T0\"," 
							+ "  \"FechaLiquidacion\": \"2017-07-21 00:00:00.000\","
							+ "  \"IdTipoCanalIngreso\": \"AGE\"," 
							+ "  \"IdMonedaSistema\": \"PEN\"," 
							+ "  \"IdBolsa\": \"BVL\","
							+ "  \"IdUsuario\": 69,"
							+ "  \"IdAgente\": 69," 
							+ "  \"IdInstrumento\": 177619," 
							+ "  \"IdMonedaInstrumento\": \"PEN\","
							+ "  \"IdTipoReajuste\": \"USD\"," 
							+ "  \"FechaEmisionInstrumento\": \"0001-01-01 00:00:00.000\"," 
							+ "  \"FechaVencimientoInstrumento\": \"0001-01-01 00:00:00.000\"," 
							+ "  \"EsFungible\": \"N\"," 		
							+ "  \"IdBoveda\": \"CAV\","
							+ "  \"Cantidad\": 50000," 
							+ "  \"CorteMinimo\": 1," 
							+ "  \"ValorRescate\": null," 
							+ "  \"PrecioCompra\": 150," 
							+ "  \"TasaCompra\": 1.296668," 
							+ "  \"MontoCompra\": 245242.5," 
							+ "  \"MontoCompraMonedaInstrumento\": 75000" 
							+ "}"
						+ "] }";*/
				
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"CUS\",\"p_IdUsuario\":0,\"p_SimularOperacion\":\"N\",\"p_TicketAtencion\":0,\"p_DatosEntrada\":[{\"IdGenerarMovimientoCustodia\":0,\"TablaAfectada\":\"CARTERA\",\"EsRegistroNuevo\":\"S\",\"IdCustodia\":null,\"FolioCustodia\":null,\"GenerarNuevoFolioCustodia\":\"N\",\"MantieneFolioCustodia\":\"N\",\"IdCarteraOrigen\":null,\"FolioCarteraRelacionada\":null,\"FolioCarteraOrigen\":null,\"IdGarantiaOrigen\":null,\"FolioGarantiaOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdMovimientoCarteraEnTransitoRelacionado\":null,\"IdTipoOperacionGestora\":null,\"IdCustodiaGestor\":null,\"AccionOperacion\":\"INGRESO\",\"IdCliente\":100477,\"IdPortafolio\":100866,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":\"2018-02-08 00:00:00.000\",\"FechaOperacionOriginal\":\"2018-02-08 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":\"INGTIT\",\"IdGrupoOperacion\":null,\"IdOperacion\":901809,\"IdConcepto\":\"CARSIN\",\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-02-08 00:00:00.000\",\"IdTipoProducto\":\"INFM\",\"IdTipoCanalIngreso\":\"AGE\",\"CodigoIFRS\":null,\"IdTipoComprobante\":\"CUS\",\"FolioComprobante\":901809,\"FechaComprobante\":\"2018-02-08 00:00:00.000\",\"FolioExternoReferencia\":\"901809\",\"IdMesa\":null,\"IdBolsa\":\"BCS\",\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdUsuario\":0,\"IdSucursal\":0,\"IdAgente\":20180,\"Glosa\":\"\",\"PermiteCertificacionPorUsuario\":null,\"IdMonedaAdicional\":\"CLP\",\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":\"CUS\",\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":133264,\"Nemotecnico\":\"LIQUIDEZB\",\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmision\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"IdEmisor\":\"CREDICORPAGF\",\"EsFungible\":\"N\",\"IdBoveda\":\"COR\",\"CuentaBoveda\":null,\"EsNominal\":\"N\",\"EsVentaDescubierta\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":\"LIQUIDEZB\",\"Cantidad\":3.70999999999999996447286321199499070644378662109375,\"CorteMinimo\":1,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":34.210000,\"TasaCompra\":0.0000,\"MontoCompra\":127,\"MontoCompraMonedaInstrumento\":127,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdRegimenFondoMutuo\":\"\",\"FechaOrigenCompra\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":null,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":null,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null,\"IdCarga\":678,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":\"2018-02\",\"EsVentaCorta\":null},{\"IdGenerarMovimientoCustodia\":1,\"TablaAfectada\":\"CARTERA\",\"EsRegistroNuevo\":\"S\",\"IdCustodia\":null,\"FolioCustodia\":null,\"GenerarNuevoFolioCustodia\":\"N\",\"MantieneFolioCustodia\":\"N\",\"IdCarteraOrigen\":null,\"FolioCarteraRelacionada\":null,\"FolioCarteraOrigen\":null,\"IdGarantiaOrigen\":null,\"FolioGarantiaOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdMovimientoCarteraEnTransitoRelacionado\":null,\"IdTipoOperacionGestora\":null,\"IdCustodiaGestor\":null,\"AccionOperacion\":\"INGRESO\",\"IdCliente\":100477,\"IdPortafolio\":100866,\"IdCuentaCustodioPortafolio\":null,\"IdClienteRelacionado\":null,\"IdPortafolioRelacionado\":null,\"IdCuentaCustodioPortafolioRelacionado\":null,\"FechaOperacion\":\"2018-02-08 00:00:00.000\",\"FechaOperacionOriginal\":\"2018-02-08 00:00:00.000\",\"FechaVencimientoOperacion\":null,\"PlazoEnDiasOperacion\":null,\"IdTipoOperacion\":\"INGTIT\",\"IdGrupoOperacion\":null,\"IdOperacion\":906139,\"IdConcepto\":\"CARSIN\",\"FolioOperacionRelacionada\":null,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-02-08 00:00:00.000\",\"IdTipoProducto\":\"INFM\",\"IdTipoCanalIngreso\":\"AGE\",\"CodigoIFRS\":null,\"IdTipoComprobante\":\"CUS\",\"FolioComprobante\":906139,\"FechaComprobante\":\"2018-02-08 00:00:00.000\",\"FolioExternoReferencia\":\"906139\",\"IdMesa\":null,\"IdBolsa\":\"BCS\",\"EsIntraday\":null,\"TipoCarteraCorredor\":null,\"UsaGarantiaEspecifica\":null,\"CobraPrimaAlInicio\":null,\"IdTipoPrestamo\":null,\"IdUsuario\":0,\"IdSucursal\":0,\"IdAgente\":20180,\"Glosa\":\"\",\"PermiteCertificacionPorUsuario\":null,\"IdMonedaAdicional\":\"CLP\",\"FolioCarteraGarantizada\":null,\"IdInstrumentoCarteraGarantizada\":null,\"IdContraparteGarantia\":null,\"RazonSocialContraparteGarantia\":null,\"CuentaContraparteGarantia\":null,\"IdOrigenGarantia\":\"CUS\",\"IdEspecieGarantia\":null,\"EsEspecifica\":null,\"IdInstrumento\":133264,\"Nemotecnico\":\"LIQUIDEZB\",\"IdInstrumentoAnterior\":null,\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmision\":\"CLP\",\"IdTipoReajusteInstrumento\":\"CLP\",\"IdEmisor\":\"CREDICORPAGF\",\"EsFungible\":\"N\",\"IdBoveda\":\"COR\",\"CuentaBoveda\":null,\"EsNominal\":\"N\",\"EsVentaDescubierta\":null,\"FechaVencimientoInstrumento\":null,\"FechaEmisionInstrumento\":null,\"NemoSVS\":\"LIQUIDEZB\",\"Cantidad\":0.450000000000000011102230246251565404236316680908203125,\"CorteMinimo\":1,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":33.330000,\"TasaCompra\":0.0000,\"MontoCompra\":15,\"MontoCompraMonedaInstrumento\":15,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"TipoCarteraNormativa\":null,\"FechaCurse\":null,\"TipoFactorAjuste\":null,\"ValorFactorAjusteaFechaCurse\":null,\"CostoEnFactorAjuste\":null,\"IdRegimenFondoMutuo\":\"\",\"FechaOrigenCompra\":null,\"IdSpotAsociado\":null,\"IdModalidadCumplimiento\":null,\"MontoMonedaPrincipal\":null,\"MontoMonedaSecundaria\":null,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":null,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":null,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":null,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":null,\"TasaPuntosMonedaPrincipal\":null,\"TasaPuntosMonedaSecundaria\":null,\"MontoMatriz\":null,\"TasaDescuentoAnticipo\":null,\"TipoCambioAnticipo\":null,\"MontoARecibirAnticipo\":null,\"TipoCambioObservado\":null,\"FechaEfectiva\":null,\"IdPlaza\":null,\"IdFixing\":null,\"FechaFixing\":null,\"HoraFixing\":null,\"IdReferenciaMercado\":null,\"IdMonedaPrincipal\":null,\"IdMonedaSecundaria\":null,\"ValorTipoCambioCierre\":null,\"ValorParidadCierre\":null,\"ValorTipoCambioCosto\":null,\"ValorParidadCosto\":null,\"IdCarga\":678,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":\"2018-02\",\"EsVentaCorta\":null}],\"p_ValorizacionAutomatica\":\"N\",\"p_idPais\":null,\"FolioSistemaExterno\":\"61-0000000018122\"}";
				IngresoCustodiaEntrada Objin = gson.fromJson(json, IngresoCustodiaEntrada.class);
							
				IngresoCustodia  kk = new IngresoCustodia();
				IngresoCustodiaSalida Objout = kk.logica(con, Objin);
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
}
