package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjProcesamiento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;
import cl.bcs.facturacion.ReversarFacturacionCustodia.FoliosPres;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.val.Valorizar;
import cl.bcs.plataforma.val.Valorizar.OutValorizar;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class RetiroCustodia implements JavaCall{
	static final Logger logger = LogManager.getLogger(RetiroCustodia.class);
	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_SEL_Cartera.sql");
	private final String SQL_BloqueoCarteras = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_SEL_BloqueoCartera.sql");
	private final String SQL_SelectCarteraFechaOrigenCompra = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_SEL_CarteraFechaOrigenCompra.sql");
	private final String SQL_SelectGarantia = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_SEL_Garantia.sql");
	private final String SQL_SelectSubyacente = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_SEL_Subyacente.sql");
	private final String SQL_Select_Prestamos_Vigentes = loadResourceToString("cl/bcs/facturacion/sql/ReversarFacturacionCustodia_SelectPrestamosVigentes.sql");
	
	//private final String SQL_UpdateTransito = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_UPD_MovimientoCarteraEnTransito.sql");
	private final String SQL_UpdateCartera = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_UPD_Cartera.sql");
	private final String SQL_UpdateSubyacente = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_UPD_Subyacente.sql");
	private final String SQL_UpdateGarantia = loadResourceToString("cl/bcs/custodia/sql/RetiroCustodia_UPD_Garantia.sql");	
	
	public final String SI = "S";
	public final String NO = "N";
	
	public static class ObjTablaCartera {
		public String				TablaAfectada;
		public Long					IdCartera;
		public Long					FolioCartera;
		public Long					FolioCarteraRelacionada;
		public Long					FolioGarantiaOrigen;
		public String				AccionOperacion;
		public java.sql.Timestamp	FechaIngresoRegistro;
		public java.sql.Timestamp	FechaModificacionRegistro;
		public Long					IdEmpresa;
		public Long					IdUsuarioMod;
		public java.sql.Date		FechaOperacion;
		public java.sql.Date		FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;
		public Long					IdCliente;
		public Long					IdPortafolio;
		public Long					IdCuentaCustodioPortafolio;
		public Long					IdClienteRelacionado;
		public Long					IdPortafolioRelacionado;
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
		public java.sql.Date		FechaOrigenCompra;
		public Long					IdCarga;
		public Long					IdSecuenciaCarteraMigracion;
		public String				CodigoPeriodo;
		public String				IdRegimenFondoMutuo;
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
		public String				EGRRetiroPorFusion;
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
		public String 				FolioSistemaExterno;
		//costoUF
		public BigDecimal			EGRValorFactorAjusteaFechaCurse;
		public BigDecimal			EGRCostoInversionEnFactorAjuste;
		//rentabilidad
		public BigDecimal			ValorFactor;
		public BigDecimal			MontoCompraFactor;
		public BigDecimal			EGRValorFactor;
		public BigDecimal			EGRMontoCompraFactor;
		//-------------------------------------------------------
		
		//para la carga
		public Long					IdGenerarMovimientoCustodia;
		public Long					IdGenerarMovimientoCustodiaPadre;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjTablaTransito {
		public Long					IdMovimientoCarteraEnTransito;
		public Long					IdMovimientoCarteraEnTransitoRelacionado;
		public java.sql.Timestamp	FechaIngresoRegistro;
		public java.sql.Timestamp	FechaCertificacionRegistro;
		public java.sql.Timestamp	FechaAnulacionRegistro;
		public Long					IdUsuarioIngreso;
		public Long					IdUsuarioCertificacion;
		public Long					IdUsuarioAnulacion;
		public java.sql.Date		FechaOperacion;
		public java.sql.Date		FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;
		public Long					FolioCustodia;
		public String				IdTipoComprobante;
		public Long				FolioComprobante;
		public String			EsRegistroGeneradoPorSistema;
		public String			MueveCustodia;
		public String			PermiteCertificacionPorUsuario;
		public String			OrigenCustodia;
		public Long				IdCustodiaOrigen;
		public String			IdTipoOperacionOrigen;
		public Long				FolioCarteraGarantizada;
		public Long				IdInstrumentoCarteraGarantizada;
		public String			EsEspecifica;
		public Long				IdContraparteGarantia;
		public String			RazonSocialContraparteGarantia;
		public String			CuentaContraparteGarantia;
		public String			IdOrigenGarantia;
		public Long				IdEspecieGarantia;
		public Long				IdEmpresa;
		public Long				IdPais;
		public String			EsNominal;
		public String			FolioExternoReferencia;
		public Long				IdCliente;
		public Long				IdPortafolio;
		public Long				IdCuentaCustodioPortafolio;
		public String			IdEstado;
		public String			IdTipoCanalIngreso;
		public String			IdMonedaSistema;
		public String			IdMonedaAdicional;
		public java.sql.Date	FechaFactorMonedaAdicional;
		public BigDecimal		ValorFactorMonedaAdicional;
		public String			CodigoIFRS;
		public String			IdConcepto;
		public String			IdTipoOperacion;
		public Long				IdMesa;
		public String			IdBolsa;
		public Long				IdSucursal;
		public Long				IdAgente;
		public String			IdCondicionLiquidacion;
		public java.sql.Date	FechaLiquidacion;
		public String			EsIntraday;
		public String			UsaGarantiaEspecifica;
		public String			Glosa;
		public Long				IdUsuario;
		public String			IdModulo;
		public String			TipoCarteraCorredor;
		public String			TipoCarteraNormativa;
		public java.sql.Date	FechaCurse;
		public String			TipoFactorAjuste;
		public BigDecimal 		ValorFactorAjusteaFechaCurse;
		public BigDecimal 		CostoEnFactorAjuste;
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
		public String			IdBoveda;
		public String			CuentaBoveda;
		public String			ISIN;
		public java.sql.Date	FechaFactorMonedaInstrumento;
		public BigDecimal 		ValorFactorMonedaInstrumento;
		public BigDecimal	 	Cantidad;
		public BigDecimal 		CorteMinimo;
		public BigDecimal 		ValorRescate;
		public BigDecimal 		PrecioCompra;
		public BigDecimal 		TasaCompra;
		public BigDecimal 		MontoCompra;
		public BigDecimal 		MontoCompraMonedaInstrumento;
		public BigDecimal 		MontoCompraMonedaAdicional;
		public Long				IdCarga;
		public Long				IdSecuenciaCarteraMigracion;
		public String 			CodigoPeriodo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjTablaGarantia {
		public String			TablaAfectada;
		public Long				IdGarantia;
		public Long				FolioGarantia;
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
		public String			IdBoveda;
		public String			TipoCarteraCorredor;
		public String			IdTipoOperacionOrigen;
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
		public String 			TablaAfectada;
		public Long				IdSubyacente;
		public Long				IdCliente;
		public Long				IdPortafolio;
		public String			AccionOperacion;
		public Long				PlazoEnDiasOperacion;
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
		public Long	 			IdCuentaCustodioPortafolio;
		public String			TipoCarteraCorredor;
		public String			IdTipoComprobante;
		public Long				FolioComprobante;
		public java.sql.Date	FechaLiquidacion;
		public String			IdBoveda;		
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
		
		public String			IdTipoOperacionOrigen;
		public Long				FolioCarteraRetirada;
		public Long 			IdOperacionOrigen;
		
		//TASA INSTRUMENTO VENTA CON PACTO
		public BigDecimal		PrecioReservaPacto;
		public BigDecimal		TasaReservaPacto;
		public BigDecimal		EGRValorPresente;
		public String			IdCondicionLiquidacion;
		public String  			GeneraCarteraNuevaTasaInstrumentoPacto = "N";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public class SalidaValorizacionTipoInstrumento {
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public BigDecimal TasaReservaPacto;
	}
	
	public class ObjValorPresente{
		public long EGRValorPresente;
	}
	
	
	public static class ObjSaldoCustodia {
		public Long				IdGenerarMovimientoCustodia;
		public Long				IdGenerarMovimientoCustodiaPadre;
		public String			TablaAfectada;
		public Long				IdCustodia;
		public Long				FolioCustodia;
		public BigDecimal		CantidadActual;				//Cantidad actual que posee el registro custodia
		public BigDecimal		CantidadARetirar;			//Cantidad a retirar del registro custodia
		public BigDecimal		SaldoCantidad;				//Saldo con que quedara el registro custodia	
		public BigDecimal		ValorRescateActual;			
		public BigDecimal		ValorRescateARetirar;
		public BigDecimal		SaldoValorRescate;
		public java.sql.Date	FechaOperacion;
		public java.sql.Date	FechaLiquidacion;
		public java.sql.Date	FechaOrigenCompra;
		public BigDecimal		CostoInversionEnFactorAjusteARetirar;
		public BigDecimal		ValorFactorAjusteaFechaCurseARetirar;
		public BigDecimal		CostoInversionEnFactorAjusteOriginal;
		public BigDecimal 		CostoInversionEnFactorAjusteActual;
		public BigDecimal		ValorFactorOriginal;
		public BigDecimal		ValorFactorARetirar;
		public BigDecimal		MontoCompraFactorOriginal;
		public BigDecimal		MontoCompraFactorARetirar;
		public String			RegistroConProblemas = "N";
		public String			Mensaje;
		public String			CodigoMensaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjCustodiaDescartada {
		public Long				IdGenerarMovimientoCustodia;
		public String			TablaAfectada;
		public Long				IdCustodia;
		public Long				FolioCustodia;
		public String			MotivoDescarte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	//--------------------------------------------------------------------------------------------------------------------------------------
	public static class ObjDatosEntrada {
		public Long					IdGenerarMovimientoCustodia;	//ID Interno del proceso
		public String				TablaAfectada; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
		public String 				EsRetiroNegocio;				//S o N
		public String				EsRetiroPorFusion;				//Cuando es retorno de cartera
		
		//////////////////////////	RETIRO REGISTRO ESPECIFICO
		public Long					IdCustodia;
		public Long					FolioCustodia;
		
		//////////////////////////	INFORMACION DE DEPENDENCIAS ENTRE TABLAS
		public Long					IdCarteraOrigen;
		public Long					IdMovimientoCarteraEnTransitoOrigen;
		
		//////////////////////////	INFORMACION PARA FILTRO DE CARTERA
		public String				IdTipoComprobanteBusqueda;
		public Long					FolioComprobanteBusqueda;
		
		/////////////////////////// OPERACION
		public String				AccionOperacion;				//RETIRO - VENCIMIENTO - ANTICIPO(REVERSA ENTRA POR ANULACION) - ANULACION - MODIFICACION
		public java.sql.Timestamp	FechaRetiroRegistro;
		public Long					IdCliente; 
		public Long					IdPortafolio; 
		public java.sql.Date		FechaOperacion;
		public String				IdTipoOperacion;
		public String				IdTipoProducto;
		public Long					PlazoEnDiasOperacion;
		public Long					IdOperacion;
		public String				IdConcepto;
		public String				IdCondicionLiquidacion; 
		public java.sql.Date		FechaLiquidacion;		
		public String				IdTipoCanalIngreso; 	 
		public String				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public java.sql.Date		FechaComprobante;
		public String				FolioExternoReferencia;
		public Long					IdSucursal;
		public Long					IdAgente;
		public Long					IdUsuario;
		public String				IdModulo;
		public String				Glosa;
		public String				IdMonedaAdicional;
		public String				IdEstado;
		public String 				FolioSistemaExterno;
		
		/////////////////////////// DATOS VALORIZACION PRESTAMOS
		public Long                 IdInstrumentoPrestamo;
		public String 				NemotecnicoPrestamo;
		public String				IdTipoInstrumentoPrestamo;
		public BigDecimal			TasaCompraInstrumentoPrestamo;
		public BigDecimal			TasaPrimaPremio;
		
		/////////////////////////// MONTO RESTANTE PRESTAMO
		public BigDecimal			MontoRestanteLocal;
		public BigDecimal			MontoRestanteMonedaInstrumento;
		
		/////////////////////////// GARANTIAS
		public String				IdOrigenGarantia;
		
		/////////////////////////// INSTRUMENTO
		public Long					IdInstrumento;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmision;
		public String				IdTipoReajusteInstrumento;
		public String				IdEmisor;
		public String				EsFungible;
		public String				IdBoveda;
		public String				CuentaBoveda;
		public String				EsNominal;
		public java.sql.Date		FechaVencimientoInstrumento;
		public String 				NemoSVS;
		public String				IdClasificacionTipoInstrumento;
		
		/////////////////////////// CANTIDADES Y MONTOS		
		public BigDecimal			Cantidad; 
		public BigDecimal			CorteMinimo; 
		public BigDecimal			ValorRescate;
		public BigDecimal			PrecioSucio;
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra; 
		public BigDecimal			MontoCompraMonedaInstrumento; 
		public BigDecimal			TasaAgente; 
		public String				Nemotecnico;
		public BigDecimal			PrecioAPlazo;
		public BigDecimal			MontoAPlazo; 
		public BigDecimal			MontoAPlazoMonedaInstrumento; 
		

		public BigDecimal			MontoCompra; 
		
		
		/////////////////////////// FONDOS MUTUOS
		/*public String				TipoCarteraNormativa; 
		public java.sql.Date		FechaCurse; 
		public String				TipoFactorAjuste;*/ 
		public BigDecimal			ValorFactorAjusteaFechaCurse;
		public String				IdRegimenFondoMutuo;
		//public BigDecimal			CostoEnFactorAjuste;
		public String				OrdenaFechaOrigenCompra;
		//rentabilidad
		public BigDecimal			MontoCompraFactor;
		public BigDecimal			ValorFactor;
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
		public String 				TipoMonedaPrincipal;
		
		/////////////////////////// CARGA DE DATOS (EJEM: CARGA ARCHIVO)
		public Long					IdCarga; 
		public Long					IdSecuenciaCarteraMigracion; 
		public String				CodigoPeriodo;
		public String               EsVentaDescubierta;
		public String               NombreCliente;
		public String               CodigoPortafolio;
		
		//Parametro simultanea para la cartera prestada al financista
		public String				SIMCarteraPrestadaAlFinancista;
		public String 				SubyacenteRetornaCartera;
		
		//Solo para garantias
		public Long					FolioGarantiaOrigen;
		
		public ObjDatosEntrada() {
			this.EsRetiroPorFusion = "N";
			this.IdMonedaAdicional = null;
			this.IdClasificacionTipoInstrumento = "";
			this.NombreCliente = "";
			this.CodigoPortafolio = "";
			this.SIMCarteraPrestadaAlFinancista = "N";
			this.SubyacenteRetornaCartera = "S";
			this.FolioGarantiaOrigen = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		};
	};	
	
	public static class RetiroCustodiaEntrada {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario = 0L;
		public String					p_IdModulo;
	    public String					p_SimularOperacion="N";
	    public Long						p_TicketAtencion;
	    public List<ObjDatosEntrada>	p_DatosEntrada;
		public String 					p_ValorizacionAutomatica = "S";
		public String					p_IdMonedaAdicional = null;
		public Long						p_idPais=null;
		
	    	        
		public String toString() {
			return Utils.printJson(this, false);
		};
		public RetiroCustodiaEntrada(){
	    	p_DatosEntrada = new ArrayList<ObjDatosEntrada>();
	    };	
	};
	
	public static class RetiroCustodiaSalida {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<ObjDatosSalida>		P_OUT_DATOSSALIDA;
		public List<ObjTablaCartera>	P_OUT_CARTERAS_RETIRADAS;
		
		public static class ObjDatosSalida {
			public Long				IdGenerarMovimientoCustodia;
			public String			TablaOrigen;
			public Long				IdCustodia;
			public Long				FolioCustodia;
			public Long				IdOperacion;			//Si viene del negocio
			public Long				FolioComprobante;		//Si viene de facturacion
			public Long				IdInstrumento;
			public BigDecimal		Cantidad; 
			public BigDecimal		ValorRescate;
			public java.sql.Date 	FechaOperacion;
			public java.sql.Date	FechaLiquidacion;
			public java.sql.Date	FechaOrigenCompra;
			public BigDecimal		CostoInversionEnFactorAjusteARetirar;
			public BigDecimal		ValorFactorAjusteaFechaCurseARetirar;
			public BigDecimal		CostoInversionEnFactorAjusteOriginal;
			public BigDecimal 		CostoInversionEnFactorAjusteActual;
			public BigDecimal		ValorFactorOriginal;
			public BigDecimal		ValorFactorARetirar;
			public BigDecimal		MontoCompraFactorOriginal;
			public BigDecimal		MontoCompraFactorARetirar;
			public String			RegistroConProblemas;
			public String			Mensaje;
			public String			CodigoMensaje;
			public Long				IdGarantiaGenerada = 0L;
			public String toString() {
				return Utils.printJson(this, false);
			};
		};
		public String toString() {
			return Utils.printJson(this, false);
		};
		public RetiroCustodiaSalida (){
			P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			P_OUT_DATOSSALIDA	= new ArrayList<ObjDatosSalida>();
			P_OUT_CARTERAS_RETIRADAS = new ArrayList<ObjTablaCartera>();
		};
	};
	
	////////********************************************************************************************************************************************////////
	@Trace
	public RetiroCustodiaSalida logica(Connection con, RetiroCustodiaEntrada in) throws Exception {
		logger.info("INCIO");
		RetiroCustodiaSalida out = new RetiroCustodiaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;	
		SQL sql = new SQL();
		
		try {
			//00 - INFORMACION DE SISTEMA
			ConsultarEmpresa empresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = empresaService.logica(con, in.p_IdEmpresa);
			Long idPais = empresa.IdPais;
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			String usaFechaLiquidacionEnVencimiento = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
			String pasaAGarantiaReporteSimultanea = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "IngresaAGarantiaSimRepDeCompra");
			String parametroTasaRecompraVentaConPacto = obtenerParametroUsoTasaRecompraVentaConPacto(con, in.p_IdEmpresa);

			
			//01 - ESTANDARIZAR LOS REGISTROS
			EstandarizacionRegistroCustodia.EstandarizacionRegistroCustodiaEntrada  paramInEstandarizacionRegistros = new EstandarizacionRegistroCustodia.EstandarizacionRegistroCustodiaEntrada();
			paramInEstandarizacionRegistros.p_IdEmpresa = in.p_IdEmpresa;
			
			//Valida que no existan prestamistas asociados
			List<Long> idsFolioPrestamo = new ArrayList<>();
			HashMap<Long, ObjDatosEntrada> hashObjDatosEntrada = new HashMap<>();
			for (ObjDatosEntrada reg : in.p_DatosEntrada) {
				if (reg.FolioCustodia != null && !"ANTPRT".equals(reg.IdConcepto)){
					idsFolioPrestamo.add(reg.FolioCustodia);
					hashObjDatosEntrada.put(reg.FolioCustodia, reg);
				}
			}
			
			HashMap<String,Object> paramsFolioPrestamo = new HashMap<>();
			paramsFolioPrestamo.put("FolioPrestamo", idsFolioPrestamo);
			List<FoliosPres> foliosPrestamos = sql.query(con, SQL_Select_Prestamos_Vigentes, paramsFolioPrestamo, FoliosPres.class);

			for (FoliosPres folio : foliosPrestamos) {
				if (folio.CantidadAsignada.compareTo(BigDecimal.ZERO) > 0) {
					in.p_DatosEntrada.remove(hashObjDatosEntrada.get(folio.FolioPrestamo));
					
					String codigo = "NOTA_DE_CREDITO_VC_PRESTAMISTAS";
					String descripcion = Diccionario.getMensaje(con, in.p_IdEmpresa, codigo)
							.add("FolioPrestamo",folio.FolioPrestamo).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(folio.FolioPrestamo, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
					return out;
				}
			}
						
			for (ObjDatosEntrada DE: in.p_DatosEntrada){
				EstandarizacionRegistroCustodia.ObjRegistroAProcesar RegistroEstandarizar = new EstandarizacionRegistroCustodia.ObjRegistroAProcesar(); 
				Utils.copyPojo(DE, RegistroEstandarizar);
				paramInEstandarizacionRegistros.p_RegistrosAProcesar.add(RegistroEstandarizar);
			};			
			EstandarizacionRegistroCustodia EstandarizacionRegistro = new EstandarizacionRegistroCustodia();
			EstandarizacionRegistroCustodia.EstandarizacionRegistroCustodiaSalida resultEstandarizacion = EstandarizacionRegistro.logica(con, paramInEstandarizacionRegistros);			
			HashMap<Long,Object> hmDatosEstandarizados = Utils.List2Hash(resultEstandarizacion.p_RegistrosProcesados, "IdGenerarMovimientoCustodia");
			
			
			if (!resultEstandarizacion.P_OUT_ERRORESCONTADOR.equals(0L)){
				out.P_OUT_MENSAJES = resultEstandarizacion.P_OUT_MENSAJES;
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			};
			
			//02 - PARAMETROS DE BUSQUEDA DE CUSTODIAS
			Long cantCartera = 0L;
			Long cantGarantia = 0L;
			Long cantSubyacente = 0L;
			Long cantCarteraFiltroOrigenCompra = 0L;
			List<Long> IdPortafoliosCartera = new ArrayList<Long>();
			List<Long> IdInstrumentosCartera = new ArrayList<Long>();
			List<Long> IdCustodiasCartera = new ArrayList<Long>();
			List<String> IdRegimenesCartera = new ArrayList<String>(); //FILTRO POR REGIMEN
			
			List<Long> IdPortafoliosCarteraOrdenaFechaOrigenCompra = new ArrayList<Long>();
			List<Long> IdInstrumentosCarteraOrdenaFechaOrigenCompra = new ArrayList<Long>();
			List<Long> IdCustodiasCarteraOrdenaFechaOrigenCompra = new ArrayList<Long>();
			List<String> IdRegimenesCarteraOrdenaFechaOrigenCompra = new ArrayList<String>(); //FILTRO POR REGIMEN
			
			List<Long> IdPortafoliosGarantia = new ArrayList<Long>();
			List<Long> IdInstrumentosGarantia = new ArrayList<Long>();
			List<Long> IdCustodiasGarantia = new ArrayList<Long>();
			
			List<Long> IdPortafoliosSubyacente = new ArrayList<Long>();
			List<Long> IdInstrumentosSubyacente = new ArrayList<Long>();
			List<Long> IdCustodiasSubyacente = new ArrayList<Long>();
			
			
			List<String> ltNemosSVS = new ArrayList<String>();

			List<Long> FoliosCartera = new ArrayList<Long>();
			
			HashMap<Long,BigDecimal> disponibleCartera = new HashMap<>();
			HashMap<Long,BigDecimal> disponibleGarantia = new HashMap<>();
			HashMap<Long,BigDecimal> disponibleSubyacente = new HashMap<>();
			
			for (EstandarizacionRegistroCustodia.ObjRegistroProcesado RP: resultEstandarizacion.p_RegistrosProcesados){				
				if ( RP.TablaAfectada.equalsIgnoreCase("CARTERA") && !"S".equalsIgnoreCase(RP.OrdenaFechaOrigenCompra))		{
					cantCartera++;
				
					if (RP.IdCustodia != null && !RP.IdCustodia.equals(0L))	{
						IdCustodiasCartera.add( RP.IdCustodia );		//Retiro Cartera especifica
					}
					if (RP.IdPortafolio != null && !RP.IdPortafolio.equals(0L))	{
						IdPortafoliosCartera.add( RP.IdPortafolio );	//Retiro Cartera FIFO
					}
					if (RP.IdInstrumento != null && !RP.IdInstrumento.equals(0L)) {
						IdInstrumentosCartera.add( RP.IdInstrumento );	//Retiro Cartera FIFO
					}
					if (RP.IdRegimenFondoMutuo != null && !RP.IdRegimenFondoMutuo.isEmpty()) {
						IdRegimenesCartera.add(RP.IdRegimenFondoMutuo); //Retiro Cartera FIFO
					}
					if (RP.FolioCustodia != null && !RP.FolioCustodia.equals(0L)){
						FoliosCartera.add(RP.FolioCustodia); //Retiro Cartera especifica 
					}
					
				}
				if ( RP.TablaAfectada.equalsIgnoreCase("CARTERA") && "S".equalsIgnoreCase(RP.OrdenaFechaOrigenCompra))		{
					cantCarteraFiltroOrigenCompra++;
				
					if (RP.IdCustodia != null && !RP.IdCustodia.equals(0L))	{
						IdCustodiasCarteraOrdenaFechaOrigenCompra.add( RP.IdCustodia );		//Retiro Cartera especifica
					}
					if (RP.IdPortafolio != null && !RP.IdPortafolio.equals(0L))	{
						IdPortafoliosCarteraOrdenaFechaOrigenCompra.add( RP.IdPortafolio );	//Retiro Cartera FIFO
					}
					if (RP.IdInstrumento != null && !RP.IdInstrumento.equals(0L)) {
						IdInstrumentosCarteraOrdenaFechaOrigenCompra.add( RP.IdInstrumento );	//Retiro Cartera FIFO
					}
					if (RP.IdRegimenFondoMutuo != null && !RP.IdRegimenFondoMutuo.isEmpty()) {
						IdRegimenesCarteraOrdenaFechaOrigenCompra.add(RP.IdRegimenFondoMutuo); //Retiro Cartera FIFO
					}
					if (RP.FolioCustodia != null && !RP.FolioCustodia.equals(0L)){
						FoliosCartera.add(RP.FolioCustodia); //Retiro Cartera especifica 
					}
					
				}
				if ( RP.TablaAfectada.equalsIgnoreCase("GARANTIA"))		{
					cantGarantia++;
					
					if (RP.IdCustodia != null && !RP.IdCustodia.equals(0L))	{
						IdCustodiasGarantia.add( RP.IdCustodia );		//Retiro Cartera especifica
					}
					if (RP.IdPortafolio != null && !RP.IdPortafolio.equals(0L))	{
						IdPortafoliosGarantia.add( RP.IdPortafolio );	//Retiro Cartera FIFO
					}
					if (RP.IdInstrumento != null && !RP.IdInstrumento.equals(0L)) {
						IdInstrumentosGarantia.add( RP.IdInstrumento );	//Retiro Cartera FIFO
					}
				}
				if ( RP.TablaAfectada.equalsIgnoreCase("SUBYACENTE"))	{
					cantSubyacente++;
					
					if (RP.IdCustodia != null && !RP.IdCustodia.equals(0L))	{
						IdCustodiasSubyacente.add( RP.IdCustodia );		//Retiro Cartera especifica
					}
					if (RP.IdPortafolio != null && !RP.IdPortafolio.equals(0L))	{
						IdPortafoliosSubyacente.add( RP.IdPortafolio );	//Retiro Cartera FIFO
					}
					if (RP.IdInstrumento != null && !RP.IdInstrumento.equals(0L)) {
						IdInstrumentosSubyacente.add( RP.IdInstrumento );	//Retiro Cartera FIFO
					}
				}
				
				/*if ("IF".equalsIgnoreCase(RP.IdClasificacionTipoInstrumento) && RP.NemoSVS !=null){
					ltNemosSVS.add(RP.NemoSVS);
				}*/
				
				
			};

			//03 - SELECCION DE REGISTROS A RETIRAR
			List<ObjTablaCartera> Carteras = new ArrayList<ObjTablaCartera>();
			List<ObjTablaCartera> CarterasVigentesAnticipo = new ArrayList<ObjTablaCartera>();
			List<ObjTablaCartera> CarterasReordenar = new ArrayList<ObjTablaCartera>();
			if (!cantCartera.equals(0L)){
				HashMap<String,Object> params = new HashMap<>();
				HashMap<String,Object> paramsAnt = new HashMap<>();
				params.put("FiltroCustodiaValido", 0);
				params.put("FiltroPortafolioValido", 0);
				params.put("FiltroInstrumentoValido", 0);
				params.put("FiltroFolioCarteraValido", 1);
				params.put("FiltroEstadoValido", 0);
				//params.put("FiltroNemoSvsValido", 0);
				params.put("FiltroRegimenValido", 0);
				
				if (IdCustodiasCartera.size() == 0) {
					IdCustodiasCartera.add(-1L); 
					params.put("FiltroCustodiaValido", 1);
				}
				if (IdPortafoliosCartera.size() == 0) {
					IdPortafoliosCartera.add(-1L);
					params.put("FiltroPortafolioValido", 1);
				}
				if (IdInstrumentosCartera.size() == 0) {
					IdInstrumentosCartera.add(-1L);
					params.put("FiltroInstrumentoValido", 1);
				}
				
				if (IdRegimenesCartera.size() == 0) {
					IdRegimenesCartera.add("");
					params.put("FiltroRegimenValido", 1);
				}
				
				/*if (ltNemosSVS.size() == 0) {
					ltNemosSVS.add("");
					params.put("FiltroNemoSvsValido", 1);
				}*/
				
				
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCustodia", IdCustodiasCartera);
				params.put("IdPortafolio", IdPortafoliosCartera);
				params.put("IdInstrumento", IdInstrumentosCartera);	
				params.put("IdRegimenFondoMutuo", IdRegimenesCartera);
				params.put("FolioCartera", null);
				
				params.put("IdEstado",(in.p_DatosEntrada.get(0).IdConcepto != null && in.p_DatosEntrada.get(0).IdConcepto.equals("REVFWD")) ? ((in.p_DatosEntrada.get(0).IdEstado != null && in.p_DatosEntrada.get(0).IdEstado.equals("LIQ")) ? "VIG" : "ANT") : "VIG");
				
				//params.put("NemoSVS", ltNemosSVS);
				
				
				//System.out.println(params+"ini"+ (new Date()));
				sql.query(con, SQL_BloqueoCarteras, params, ObjTablaCartera.class);//bloqueo carteras.
				Carteras = sql.query(con, SQL_SelectCartera, params, ObjTablaCartera.class);
				
				
				//Para la persona que revise cuando se retira por cartera mas de los solicitado, el problema es que el select de arriba no filtra por cartera
				//y los arreglos de abajo vuelven a agregar todas las carteras a la lista, entonces nunca filtra aqui, pero si mas abajo, cuando descarta la cartera por folio
				CarterasReordenar = Carteras.stream().filter(x -> FoliosCartera.contains(x.FolioCartera)).collect(Collectors.toList());
				CarterasReordenar.addAll(Carteras.stream().filter(x -> !FoliosCartera.contains(x.FolioCartera)).collect(Collectors.toList()));
				Carteras = CarterasReordenar;
				
				for (ObjTablaCartera c : Carteras) {
					if (!c.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
						disponibleCartera.put(c.IdCartera, c.IdEstado.equalsIgnoreCase("ANT")?c.EGRCantidad:c.INGCantidad);	
					}
					else {
						disponibleCartera.put(c.IdCartera, c.IdEstado.equalsIgnoreCase("ANT")?c.EGRValorRescate:c.INGValorRescate);
					}
					if (c.IdEstado.equalsIgnoreCase("ANT") || (c.IdEstado.equalsIgnoreCase("LIQ") && in.p_DatosEntrada.get(0).IdConcepto.contentEquals("REVFWD"))) {
						paramsAnt.put("IdEmpresa", in.p_IdEmpresa);
						paramsAnt.put("IdCustodia", null);
						paramsAnt.put("IdPortafolio", IdPortafoliosCartera);
						paramsAnt.put("IdInstrumento", null);
						paramsAnt.put("IdRegimenFondoMutuo", null);
						paramsAnt.put("FolioCartera", c.FolioCartera);
						paramsAnt.put("IdEstado", "VIG");
						//paramsAnt.put("NemoSVS", ltNemosSVS);
						
						paramsAnt.put("FiltroCustodiaValido", 1);
						paramsAnt.put("FiltroPortafolioValido", 0);
						paramsAnt.put("FiltroInstrumentoValido", 1);
						paramsAnt.put("FiltroRegimenValido", 1);
						paramsAnt.put("FiltroFolioCarteraValido", 0);
						paramsAnt.put("FiltroEstadoValido", 0);
					//	paramsAnt.put("FiltroNemoSvsValido", 1);
					}
				}
				if (paramsAnt.size() > 0){
					CarterasVigentesAnticipo = sql.query(con, SQL_SelectCartera, paramsAnt, ObjTablaCartera.class);
				}
			};
			
			if (!cantCarteraFiltroOrigenCompra.equals(0L)){
				HashMap<String,Object> params = new HashMap<>();
				params.put("FiltroCustodiaValido", 0);
				params.put("FiltroPortafolioValido", 0);
				params.put("FiltroInstrumentoValido", 0);
				params.put("FiltroFolioCarteraValido", 1);
				params.put("FiltroEstadoValido", 0);
				params.put("FiltroRegimenValido", 0);
				
				if (IdCustodiasCarteraOrdenaFechaOrigenCompra.size() == 0) {
					IdCustodiasCarteraOrdenaFechaOrigenCompra.add(-1L); 
					params.put("FiltroCustodiaValido", 1);
				}
				if (IdPortafoliosCarteraOrdenaFechaOrigenCompra.size() == 0) {
					IdPortafoliosCarteraOrdenaFechaOrigenCompra.add(-1L);
					params.put("FiltroPortafolioValido", 1);
				}
				if (IdInstrumentosCarteraOrdenaFechaOrigenCompra.size() == 0) {
					IdInstrumentosCarteraOrdenaFechaOrigenCompra.add(-1L);
					params.put("FiltroInstrumentoValido", 1);
				}
				
				if (IdRegimenesCarteraOrdenaFechaOrigenCompra.size() == 0) {
					IdRegimenesCarteraOrdenaFechaOrigenCompra.add("");
					params.put("FiltroRegimenValido", 1);
				}
				
				if(!IdCustodiasCarteraOrdenaFechaOrigenCompra.isEmpty()) {
					IdCustodiasCarteraOrdenaFechaOrigenCompra = IdCustodiasCarteraOrdenaFechaOrigenCompra.stream().distinct().collect(Collectors.toList());
				}
				
				if(!IdPortafoliosCarteraOrdenaFechaOrigenCompra.isEmpty()) {
					IdPortafoliosCarteraOrdenaFechaOrigenCompra = IdPortafoliosCarteraOrdenaFechaOrigenCompra.stream().distinct().collect(Collectors.toList());
				}
				
				if(!IdInstrumentosCarteraOrdenaFechaOrigenCompra.isEmpty()) {
					IdInstrumentosCarteraOrdenaFechaOrigenCompra = IdInstrumentosCarteraOrdenaFechaOrigenCompra.stream().distinct().collect(Collectors.toList());
				}
				
				if(!IdRegimenesCarteraOrdenaFechaOrigenCompra.isEmpty()) {
					IdRegimenesCarteraOrdenaFechaOrigenCompra = IdRegimenesCarteraOrdenaFechaOrigenCompra.stream().distinct().collect(Collectors.toList());
				}
				
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCustodia", IdCustodiasCarteraOrdenaFechaOrigenCompra);
				params.put("IdPortafolio", IdPortafoliosCarteraOrdenaFechaOrigenCompra);
				params.put("IdInstrumento", IdInstrumentosCarteraOrdenaFechaOrigenCompra);	
				params.put("IdRegimenFondoMutuo", IdRegimenesCarteraOrdenaFechaOrigenCompra);
				params.put("FolioCartera", null);
				params.put("IdEstado", "VIG");


				Carteras = sql.query(con, SQL_SelectCarteraFechaOrigenCompra, params, ObjTablaCartera.class);
				for (ObjTablaCartera c : Carteras) {
					if (!c.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
						disponibleCartera.put(c.IdCartera, c.IdEstado.equalsIgnoreCase("ANT")?c.EGRCantidad:c.INGCantidad);	
					}
					else {
						disponibleCartera.put(c.IdCartera, c.IdEstado.equalsIgnoreCase("ANT")?c.EGRValorRescate:c.INGValorRescate);
					}
				}
			};
			
			List<ObjTablaGarantia> Garantias = new ArrayList<ObjTablaGarantia>();
			if (!cantGarantia.equals(0L)){
				HashMap<String,Object> params = new HashMap<>();
				params.put("FiltroCustodiaValido", 0);
				params.put("FiltroPortafolioValido", 0);
				params.put("FiltroInstrumentoValido", 0);
				
				
				if (IdCustodiasGarantia.size() == 0) {
					IdCustodiasGarantia.add(-1L); 
					params.put("FiltroCustodiaValido", 1);
				}
				if (IdPortafoliosGarantia.size() == 0) {
					IdPortafoliosGarantia.add(-1L);
					params.put("FiltroPortafolioValido", 1);
				}
				if (IdInstrumentosGarantia.size() == 0) {
					IdInstrumentosGarantia.add(-1L);
					params.put("FiltroInstrumentoValido", 1);
				}
				
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCustodia", IdCustodiasGarantia);
				params.put("IdPortafolio", IdPortafoliosGarantia);
				params.put("IdInstrumento", IdInstrumentosGarantia);				
				Garantias = sql.query(con, SQL_SelectGarantia, params, ObjTablaGarantia.class);
				for (ObjTablaGarantia g : Garantias) {
					if (!g.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
						disponibleGarantia.put(g.IdGarantia, g.INGCantidad);	
					}
					else {
						disponibleGarantia.put(g.IdGarantia, g.INGValorRescate);
					}
				}
			};
			
			List<ObjTablaSubyacente> Subyacentes = new ArrayList<ObjTablaSubyacente>();
			if (!cantSubyacente.equals(0L)){
				HashMap<String,Object> params = new HashMap<>();
				params.put("FiltroCustodiaValido", 0);
				params.put("FiltroPortafolioValido", 0);
				params.put("FiltroInstrumentoValido", 0);
				
				
				if (IdCustodiasSubyacente.size() == 0) {
					IdCustodiasSubyacente.add(-1L); 
					params.put("FiltroCustodiaValido", 1);
				}
				if (IdPortafoliosSubyacente.size() == 0) {
					IdPortafoliosSubyacente.add(-1L);
					params.put("FiltroPortafolioValido", 1);
				}
				if (IdInstrumentosSubyacente.size() == 0) {
					IdInstrumentosSubyacente.add(-1L);
					params.put("FiltroInstrumentoValido", 1);
				}
				
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCustodia", IdCustodiasSubyacente);
				params.put("IdPortafolio", IdPortafoliosSubyacente);
				params.put("IdInstrumento", IdInstrumentosSubyacente);				
				Subyacentes = sql.query(con, SQL_SelectSubyacente, params, ObjTablaSubyacente.class);
				for (ObjTablaSubyacente s : Subyacentes) {
					if (!s.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
						disponibleSubyacente.put(s.IdSubyacente, s.INGCantidad);	
					}
					else {
						disponibleSubyacente.put(s.IdSubyacente, s.INGValorRescate);
					}
				}
			};
			

			
			//04 - OBTENCION SALDO DE CUSTODIAS
			List<ObjSaldoCustodia> Saldos = new ArrayList<ObjSaldoCustodia>();			
			List<ObjCustodiaDescartada> CustodiasDescartadas = new ArrayList<ObjCustodiaDescartada>();
			
			HashMap<Long,Object> paridadesUFUTM = new HashMap<Long,Object>();
			
			//CALCULA PARIDAD PARA COSTO UF Y RENTABILIDAD NETA
			{
				HashMap<String,Boolean> fechas = new HashMap<String,Boolean>();
				CalcularParidad paridad = new CalcularParidad();
				CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
				calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
				calcularParidadEntrada.p_IdTrabajo = 2001L;
				
				for(ObjDatosEntrada DatoEntrada: in.p_DatosEntrada) {
					EstandarizacionRegistroCustodia.ObjRegistroProcesado RegProcesados = (EstandarizacionRegistroCustodia.ObjRegistroProcesado) hmDatosEstandarizados.get(DatoEntrada.IdGenerarMovimientoCustodia);
					if ( DatoEntrada.TablaAfectada.equalsIgnoreCase("CARTERA") && RegProcesados != null){
						java.sql.Date fecha = RegProcesados.FechaOperacion;
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
			
			for (ObjDatosEntrada DatoEntrada: in.p_DatosEntrada){
				EstandarizacionRegistroCustodia.ObjRegistroProcesado RegProcesados = (EstandarizacionRegistroCustodia.ObjRegistroProcesado) hmDatosEstandarizados.get(DatoEntrada.IdGenerarMovimientoCustodia);
				BigDecimal CantidadRetirada = RegProcesados.Cantidad;
				BigDecimal ValorRescateRetirado = RegProcesados.ValorRescate;				
				BigDecimal SaldoCantidadCustodia = BigDecimal.ZERO;
				BigDecimal SaldoValorRescateCustodia = BigDecimal.ZERO;
				BigDecimal SaldoCantidadARetirar = BigDecimal.ZERO;
				BigDecimal SaldoValorRescateARetirar = BigDecimal.ZERO;
								
				if (DatoEntrada.TablaAfectada.equals("CARTERA")){
					for (ObjTablaCartera car : Carteras){
						ObjCustodiaDescartada CustodiaDescartada = new ObjCustodiaDescartada(); 
						Utils.copyPojo(DatoEntrada, CustodiaDescartada);
						CustodiaDescartada.IdCustodia = car.IdCartera;
						CustodiaDescartada.FolioCustodia = car.FolioCartera;
												
						//Restringir a cartera valida
						if ((DatoEntrada.AccionOperacion.equals("RETIRO") || DatoEntrada.AccionOperacion.equals("VENCIMIENTO")) && !car.IdEstado.equals("VIG")) continue;
						if (DatoEntrada.IdCliente != null && !DatoEntrada.IdCliente.equals(car.IdCliente )) continue;
						if (DatoEntrada.IdPortafolio != null && !DatoEntrada.IdPortafolio.equals(car.IdPortafolio )) continue;						
						
						if (RegProcesados.IdInstrumento != null && !car.IdInstrumento.equals(RegProcesados.IdInstrumento)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_INSTRUMENTO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdEmisor != null && !RegProcesados.IdEmisor.equals("OPTIMUS") && !car.IdEmisor.trim().equals(RegProcesados.IdEmisor.trim())) {
							//Se omite validacion : Emisor OPTIMUS ya que es de tipo sistema (simultaneas y Reportes)
							logger.info("OMITE ERROR IDEMISOR_CORECUS X VCTO.PACTO");
							if (!"VCDPAC".equalsIgnoreCase(RegProcesados.IdConcepto)) {
									// MSH : Por el momento, hasta encontrar el pato
									CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_EMISOR_NO_COINCIDE").msg();
									CustodiasDescartadas.add(CustodiaDescartada);							
							}
							continue;
						};
						if (RegProcesados.FechaVencimientoInstrumento != null && car.FechaVencimientoInstrumento.compareTo(RegProcesados.FechaVencimientoInstrumento) != 0) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FECHAVENCIMIENTO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdTipoReajuste != null && !car.IdTipoReajuste.equals(RegProcesados.IdTipoReajuste)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_TIPOREAJUSTE_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.EsFungible != null && !car.EsFungible.equals(RegProcesados.EsFungible)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FUNGIBILIDAD_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdCuentaCustodioPortafolio != null && !car.IdCuentaCustodioPortafolio.equals(RegProcesados.IdCuentaCustodioPortafolio)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_CUENTACUSTODIO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.TipoCarteraCorredor != null && !car.TipoCarteraCorredor.equals(RegProcesados.TipoCarteraCorredor)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_TIPOCARTERA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};						
						if (RegProcesados.FechaLiquidacion != null && RegProcesados.FechaLiquidacion.compareTo(Fecha.toDate("0001-01-01"))!=0 && car.FechaLiquidacion.compareTo(RegProcesados.FechaLiquidacion) == 1 && !"ANULACION".equals(DatoEntrada.AccionOperacion)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FECHALIQUIDACION_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdBoveda != null && !RegProcesados.IdBoveda.equals("") && !car.IdBoveda.equals( RegProcesados.IdBoveda )) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_BOVEDA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdCustodia != null && !RegProcesados.IdCustodia.equals(0L) && !car.IdCartera.equals(RegProcesados.IdCustodia)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_SECUENCIACARTERA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.FolioCustodia != null && !RegProcesados.FolioCustodia.equals(0L) && !car.FolioCartera.equals(RegProcesados.FolioCustodia)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FOLIOCARTERA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						
						if (RegProcesados.IdRegimenFondoMutuo != null && !RegProcesados.IdRegimenFondoMutuo.equals("") && "FM".equals(car.IdClasificacionTipoInstrumento) && !RegProcesados.IdRegimenFondoMutuo.equals(car.IdRegimenFondoMutuo)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_REGIMEN_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						//if (DatoEntrada.IdTipoComprobanteBusqueda != null && !car.IdTipoComprobante.equals(DatoEntrada.IdTipoComprobanteBusqueda)) continue;
						//if (DatoEntrada.FolioComprobanteBusqueda != null && !car.FolioComprobante.equals(DatoEntrada.FolioComprobanteBusqueda)) continue;						
						
						
						//Manejo de Saldo 
						//Entrada (falta algo por retirar)
						if (!car.IdClasificacionTipoInstrumento.equals("IF") && CantidadRetirada.compareTo( BigDecimal.ZERO ) == 0) {
							break;
						}
						if (car.IdClasificacionTipoInstrumento.equals("IF") && ValorRescateRetirado.compareTo( BigDecimal.ZERO ) == 0) {
							break;
						}
						//Cartera (cuanto le queda a la cartera disponible)
						if (disponibleCartera.get(car.IdCartera).compareTo(BigDecimal.ZERO) == 0 ) {
							continue;
						}
						
						if (!(car.IdEstado.equalsIgnoreCase("ANT") && (car.IdTipoOperacion.equalsIgnoreCase("COMSIM") || car.IdTipoOperacion.equalsIgnoreCase("VENSIM"))))
						{
						ObjSaldoCustodia Saldo = new ObjSaldoCustodia();
						Utils.copyPojo(DatoEntrada, Saldo);
						Saldo.IdCustodia				= car.IdCartera;
						Saldo.FolioCustodia				= car.FolioCartera;
						Saldo.FechaOperacion			= car.FechaOperacion;
						Saldo.FechaLiquidacion			= car.FechaLiquidacion;
						Saldo.FechaOrigenCompra			= car.FechaOrigenCompra;
						if (car.IdEstado.equalsIgnoreCase("ANT")){
							Saldo.CantidadActual 			= car.EGRCantidad;
							Saldo.ValorRescateActual 		= car.EGRValorRescate;
						}
						else {
							Saldo.CantidadActual 			= car.INGCantidad;
							Saldo.ValorRescateActual 		= car.INGValorRescate;
						}
						for (ObjSaldoCustodia sal: Saldos){
							if (sal.IdCustodia.equals(car.IdCartera)){ 
								Saldo.CantidadActual 			= sal.SaldoCantidad;//.subtract(CantidadRetirada);
								Saldo.ValorRescateActual 		= sal.SaldoValorRescate;//.subtract(ValorRescateRetirado);
								Saldo.IdGenerarMovimientoCustodiaPadre = sal.IdGenerarMovimientoCustodia; 
							}
						}
						
						HashMap<String,Object> resultadoSaldo = CalculoSaldo(Saldo, DatoEntrada.AccionOperacion, pasaAGarantiaReporteSimultanea, CantidadRetirada, ValorRescateRetirado, RegProcesados);
						CantidadRetirada = (BigDecimal) resultadoSaldo.get("CantidadRetirada");
						ValorRescateRetirado = (BigDecimal) resultadoSaldo.get("ValorRescateRetirado");
						
						String strFecha = Fecha.toStringDate(car.FechaOperacion);
						Long longFecha = Long.parseLong(strFecha.replaceAll("\\-", ""));
						CalculoSalida calculoValorUF = (CalculoSalida) paridadesUFUTM.get(longFecha);
						CalculoSalida calculoValorUTM = (CalculoSalida) paridadesUFUTM.get(longFecha * -1);
						
						BigDecimal valorUF = calculoValorUF != null && calculoValorUF.MontoDestino != null ? calculoValorUF.MontoDestino : BigDecimal.ZERO;
						BigDecimal valorUTM = calculoValorUTM != null && calculoValorUTM.MontoDestino != null ? calculoValorUTM.MontoDestino : BigDecimal.ONE;
						if("FM".equals(car.IdClasificacionTipoInstrumento)) {
							Saldo.CostoInversionEnFactorAjusteARetirar = Saldo.CantidadARetirar.divide(car.Cantidad, MathContext.DECIMAL128).multiply(car.CostoInversionEnFactorAjuste, MathContext.DECIMAL128);
							Saldo.ValorFactorAjusteaFechaCurseARetirar = valorUF;
							Saldo.CostoInversionEnFactorAjusteOriginal = car.CostoInversionEnFactorAjuste;
							Saldo.CostoInversionEnFactorAjusteActual = car.INGCostoInversionEnFactorAjuste.subtract(Saldo.CostoInversionEnFactorAjusteARetirar, MathContext.DECIMAL128);
						}
						if(car.IdClasificacionTipoInstrumento.equals("FM") || car.IdClasificacionTipoInstrumento.equals("RV")) {
							Saldo.ValorFactorOriginal = car.ValorFactor;
							Saldo.ValorFactorARetirar = valorUTM;
							Saldo.MontoCompraFactorOriginal = car.MontoCompraFactor;
							Saldo.MontoCompraFactorARetirar = Saldo.CantidadARetirar.divide(car.Cantidad, MathContext.DECIMAL128).multiply(car.MontoCompraFactor, MathContext.DECIMAL128);;
						}
						
						Saldos.add(Saldo);
						
						if (!car.IdClasificacionTipoInstrumento.equals("IF")) {disponibleCartera.put(car.IdCartera,Saldo.SaldoCantidad);}
						if (car.IdClasificacionTipoInstrumento.equals("IF"))  {disponibleCartera.put(car.IdCartera,Saldo.SaldoValorRescate);}
						
						SaldoCantidadCustodia = SaldoCantidadCustodia.add(Saldo.CantidadActual);
						SaldoValorRescateCustodia = SaldoValorRescateCustodia.add( Saldo.ValorRescateActual);
						SaldoCantidadARetirar = SaldoCantidadARetirar.add( Saldo.CantidadARetirar);
						SaldoValorRescateARetirar = SaldoValorRescateARetirar.add( Saldo.ValorRescateARetirar);
						}
					};									
				};//CARTERA
				
				if (DatoEntrada.TablaAfectada.equals("GARANTIA")){
					for (ObjTablaGarantia gar : Garantias){
						ObjCustodiaDescartada CustodiaDescartada = new ObjCustodiaDescartada(); 
						Utils.copyPojo(DatoEntrada, CustodiaDescartada);
						CustodiaDescartada.IdCustodia = gar.IdGarantia;
						CustodiaDescartada.FolioCustodia = gar.FolioGarantia;
						
						//Restringir cartera valida	
						if (DatoEntrada.AccionOperacion.equals("RETIRO") && !gar.IdEstado.equals("VIG")) continue;
						if (DatoEntrada.IdCliente != null && !DatoEntrada.IdCliente.equals(gar.IdCliente )) continue;
						if (DatoEntrada.IdPortafolio != null && !DatoEntrada.IdPortafolio.equals(gar.IdPortafolio )) continue;
						
						if (!gar.IdGarantia.equals(RegProcesados.IdCustodia)) continue;
						
						
						if (RegProcesados.IdCustodia != null && !RegProcesados.IdCustodia.equals(0L) && !gar.IdGarantia.equals(RegProcesados.IdCustodia)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_SECUENCIAGARANTIA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.FolioCustodia != null && !RegProcesados.FolioCustodia.equals(0L) && !gar.FolioGarantia.equals(RegProcesados.FolioCustodia)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FOLIOGARANTIA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdInstrumento != null && !gar.IdInstrumento.equals(RegProcesados.IdInstrumento)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_INSTRUMENTO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdEmisor != null && !gar.IdEmisor.equals(RegProcesados.IdEmisor)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_EMISOR_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.FechaVencimientoInstrumento != null && gar.FechaVencimientoInstrumento.compareTo(RegProcesados.FechaVencimientoInstrumento) != 0) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FECHAVENCIMIENTO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdTipoReajuste != null && !gar.IdTipoReajuste.equals(RegProcesados.IdTipoReajuste)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_TIPOREAJUSTE_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (gar.IdClasificacionTipoInstrumento.equals("IF") && RegProcesados.EsFungible != null && !gar.EsFungible.equals(RegProcesados.EsFungible)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FUNGIBILIDAD_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdCuentaCustodioPortafolio != null && !gar.IdCuentaCustodioPortafolio.equals(RegProcesados.IdCuentaCustodioPortafolio)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_CUENTACUSTODIO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.TipoCarteraCorredor != null && !gar.TipoCarteraCorredor.equals(RegProcesados.TipoCarteraCorredor)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_TIPOCARTERA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.FechaLiquidacion != null && RegProcesados.FechaLiquidacion.compareTo(Fecha.toDate("0001-01-01"))!=0 && gar.FechaLiquidacion.compareTo(RegProcesados.FechaLiquidacion) == 1) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FECHALIQUIDACION_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						
						if (RegProcesados.IdBoveda != null && !RegProcesados.IdBoveda.equals("") && !gar.IdBoveda.equals( RegProcesados.IdBoveda )) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_BOVEDA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};											
						//if (DatoEntrada.IdTipoComprobanteBusqueda != null && !gar.IdTipoComprobante.equals(DatoEntrada.IdTipoComprobanteBusqueda)) continue;
						//if (DatoEntrada.FolioComprobanteBusqueda != null && !gar.FolioComprobante.equals(DatoEntrada.FolioComprobanteBusqueda)) continue;												
						if (RegProcesados.IdConcepto != null && !gar.IdConcepto.equals(RegProcesados.IdConcepto) && "N".equalsIgnoreCase(pasaAGarantiaReporteSimultanea) && !"REGGTA".equals(RegProcesados.IdConcepto)) {	
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_CONCEPTO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};						
						
						//Manejo de Saldo
						if (!gar.IdClasificacionTipoInstrumento.equals("IF") && CantidadRetirada.compareTo( BigDecimal.ZERO ) == 0) {
							continue;
						}
						if (gar.IdClasificacionTipoInstrumento.equals("IF") && ValorRescateRetirado.compareTo( BigDecimal.ZERO ) == 0) {
							continue;
						}
						
						//Garantia (cuanto le queda a la garantia disponible)
						if (disponibleGarantia.get(gar.IdGarantia).compareTo(BigDecimal.ZERO) == 0 ) {
							break;
						}
						
						ObjSaldoCustodia Saldo = new ObjSaldoCustodia();
						Utils.copyPojo(DatoEntrada, Saldo);
						Saldo.IdCustodia		= gar.IdGarantia;
						Saldo.FolioCustodia		= gar.FolioCarteraGarantizada;
						Saldo.CantidadActual 	= gar.INGCantidad;
						Saldo.ValorRescateActual = gar.INGValorRescate;
						
						HashMap<String,Object> resultadoSaldo = CalculoSaldo(Saldo, DatoEntrada.AccionOperacion, pasaAGarantiaReporteSimultanea, CantidadRetirada, ValorRescateRetirado, RegProcesados);
						CantidadRetirada = (BigDecimal) resultadoSaldo.get("CantidadRetirada");
						ValorRescateRetirado = (BigDecimal) resultadoSaldo.get("ValorRescateRetirado");
						Saldos.add(Saldo);
						
						if (!gar.IdClasificacionTipoInstrumento.equals("IF")) {disponibleGarantia.put(gar.IdGarantia,Saldo.SaldoCantidad);}
						if (gar.IdClasificacionTipoInstrumento.equals("IF"))  {disponibleGarantia.put(gar.IdGarantia,Saldo.SaldoValorRescate);}
						
						SaldoCantidadCustodia = SaldoCantidadCustodia.add(Saldo.CantidadActual);
						SaldoValorRescateCustodia = SaldoValorRescateCustodia.add( Saldo.ValorRescateActual);
						SaldoCantidadARetirar = SaldoCantidadARetirar.add( Saldo.CantidadARetirar);
						SaldoValorRescateARetirar = SaldoValorRescateARetirar.add( Saldo.ValorRescateARetirar);
					};
				};//GARANTIA
				
				if (DatoEntrada.TablaAfectada.equals("SUBYACENTE")){
					for (ObjTablaSubyacente sub : Subyacentes){
						ObjCustodiaDescartada CustodiaDescartada = new ObjCustodiaDescartada(); 
						Utils.copyPojo(DatoEntrada, CustodiaDescartada);
						CustodiaDescartada.IdCustodia = sub.IdSubyacente;
						CustodiaDescartada.FolioCustodia = sub.FolioSubyacente;
						
						//Restringir cartera valida
						if (DatoEntrada.AccionOperacion.equals("RETIRO") && !sub.IdEstado.equals("VIG")) continue;
						
						if (RegProcesados.IdInstrumento != null && !sub.IdInstrumento.equals(RegProcesados.IdInstrumento)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_INSTRUMENTO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdEmisor != null && !sub.IdEmisor.equals("OPTIMUS") && !sub.IdEmisor.equals(RegProcesados.IdEmisor)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_EMISOR_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.FechaVencimientoInstrumento != null && sub.FechaVencimientoInstrumento.compareTo(RegProcesados.FechaVencimientoInstrumento) != 0) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FECHAVENCIMIENTO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdTipoReajuste != null && !sub.IdTipoReajuste.equals(RegProcesados.IdTipoReajuste)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_TIPOREAJUSTE_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.EsFungible != null && !sub.EsFungible.equals(RegProcesados.EsFungible)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FUNGIBILIDAD_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdCuentaCustodioPortafolio != null && !sub.IdCuentaCustodioPortafolio.equals(RegProcesados.IdCuentaCustodioPortafolio)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_CUENTACUSTODIO_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.TipoCarteraCorredor != null && !sub.TipoCarteraCorredor.equals(RegProcesados.TipoCarteraCorredor)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_TIPOCARTERA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};						
						if (RegProcesados.FechaLiquidacion != null && RegProcesados.FechaLiquidacion.compareTo(Fecha.toDate("0001-01-01"))!=0 && sub.FechaLiquidacion.compareTo(RegProcesados.FechaLiquidacion) == 1 && !"ANULACION".equals(DatoEntrada.AccionOperacion)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FECHALIQUIDACION_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdBoveda != null && !RegProcesados.IdBoveda.equals("") && !sub.IdBoveda.equals( RegProcesados.IdBoveda )) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_BOVEDA_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.IdCustodia != null && !RegProcesados.IdCustodia.equals(0L) && !sub.IdSubyacente.equals(RegProcesados.IdCustodia)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_SECUENCIASUBYACENTE_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						if (RegProcesados.FolioCustodia != null && !sub.FolioSubyacente.equals(RegProcesados.FolioCustodia)) {
							CustodiaDescartada.MotivoDescarte = Diccionario.getMensaje(con,in.p_IdEmpresa,"CORECUS_FOLIOSUBYACENTE_NO_COINCIDE").msg();
							CustodiasDescartadas.add(CustodiaDescartada);							
							continue;
						};
						//if (DatoEntrada.IdTipoComprobanteBusqueda != null && !sub.IdTipoComprobante.equals(DatoEntrada.IdTipoComprobanteBusqueda)) continue;
						//if (DatoEntrada.FolioComprobanteBusqueda != null && !sub.FolioComprobante.equals(DatoEntrada.FolioComprobanteBusqueda)) continue;						
						
						//Manejo de Saldo
						if (!sub.IdClasificacionTipoInstrumento.equals("IF") && CantidadRetirada.compareTo( BigDecimal.ZERO ) == 0) {
							continue;
						}
						if (sub.IdClasificacionTipoInstrumento.equals("IF") && ValorRescateRetirado.compareTo( BigDecimal.ZERO ) == 0) {
							continue;
						}
						
						//Subyacente (cuanto le queda a la garantia disponible)
						if (disponibleSubyacente.get(sub.IdSubyacente).compareTo(BigDecimal.ZERO) == 0 ) {
							break;
						}
						
						ObjSaldoCustodia Saldo = new ObjSaldoCustodia();
						Utils.copyPojo(DatoEntrada, Saldo);
						Saldo.IdCustodia		= sub.IdSubyacente;
						Saldo.FolioCustodia		= sub.FolioSubyacente;
						Saldo.CantidadActual 	= sub.INGCantidad;
						Saldo.ValorRescateActual = sub.INGValorRescate;
						
						HashMap<String,Object> resultadoSaldo = CalculoSaldo(Saldo, DatoEntrada.AccionOperacion, pasaAGarantiaReporteSimultanea, CantidadRetirada, ValorRescateRetirado, RegProcesados);
						CantidadRetirada = (BigDecimal) resultadoSaldo.get("CantidadRetirada");
						ValorRescateRetirado = (BigDecimal) resultadoSaldo.get("ValorRescateRetirado");
						Saldos.add(Saldo);
						
						if (!sub.IdClasificacionTipoInstrumento.equals("IF")) {disponibleSubyacente.put(sub.IdSubyacente,Saldo.SaldoCantidad);}
						if (sub.IdClasificacionTipoInstrumento.equals("IF"))  {disponibleSubyacente.put(sub.IdSubyacente,Saldo.SaldoValorRescate);}
						
						SaldoCantidadCustodia = SaldoCantidadCustodia.add(Saldo.CantidadActual);
						SaldoValorRescateCustodia = SaldoValorRescateCustodia.add( Saldo.ValorRescateActual);
						SaldoCantidadARetirar = SaldoCantidadARetirar.add( Saldo.CantidadARetirar);
						SaldoValorRescateARetirar = SaldoValorRescateARetirar.add( Saldo.ValorRescateARetirar);
					};
				};//SUBYACENTE
				
				//Validacion Saldo				
				if (RegProcesados.TablaAfectada.equalsIgnoreCase("GARANTIA") && null != RegProcesados.IdOrigenGarantia && RegProcesados.IdOrigenGarantia.equalsIgnoreCase("CTA")){
					if ((RegProcesados.Cantidad.compareTo(SaldoCantidadCustodia) == -1 || RegProcesados.ValorRescate.compareTo(SaldoValorRescateCustodia) == -1) && (DatoEntrada.AccionOperacion.equals("VCD") || DatoEntrada.AccionOperacion.equals("ANU"))) {					
						for (ObjSaldoCustodia Saldo: Saldos){
							if (!Saldo.IdGenerarMovimientoCustodia.equals( DatoEntrada.IdGenerarMovimientoCustodia)) continue;
							Saldo.RegistroConProblemas  = "S";
							
							if (RegProcesados.Cantidad.compareTo(BigDecimal.ZERO) == 1 ) {
								Saldo.CodigoMensaje = "CORECUS_TOTAL_RETIRAR_INFERIOR_DISPONIBLE";
								Saldo.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,Saldo.CodigoMensaje).add("Solicitado", RegProcesados.Cantidad.toString()).add("Disponible",SaldoCantidadARetirar.toString()).msg();
							}
							if (RegProcesados.ValorRescate.compareTo(BigDecimal.ZERO) == 1 ) {
								Saldo.CodigoMensaje = "CORECUS_TOTAL_RETIRAR_INFERIOR_DISPONIBLE";
								Saldo.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,Saldo.CodigoMensaje).add("Solicitado", RegProcesados.ValorRescate.toString()).add("Disponible",SaldoValorRescateARetirar.toString()).msg();
							}
						};
					};
					
				} else {
					
					if (("IF".equals(RegProcesados.IdClasificacionTipoInstrumento) && RegProcesados.Cantidad.compareTo(SaldoCantidadARetirar) == 1) || ("IF".equals(RegProcesados.IdClasificacionTipoInstrumento) && RegProcesados.ValorRescate.compareTo(SaldoValorRescateARetirar)== 1)){					
						for (ObjSaldoCustodia Saldo: Saldos){
							if (!Saldo.IdGenerarMovimientoCustodia.equals( DatoEntrada.IdGenerarMovimientoCustodia)) continue;
														
							if (RegProcesados.Cantidad.compareTo(BigDecimal.ZERO) == 0 ) {
								Saldo.RegistroConProblemas  = "S";
								Saldo.CodigoMensaje = "CORECUS_SALDO_CUSTODIA_INSUFICIENTE";
								Saldo.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,Saldo.CodigoMensaje).add("Solicitado", RegProcesados.Cantidad.toString()).add("Disponible",SaldoCantidadARetirar.toString()).msg();
							}
							if (RegProcesados.ValorRescate.compareTo(BigDecimal.ZERO) == 0 ) {
								Saldo.RegistroConProblemas  = "S";
								Saldo.CodigoMensaje = "CORECUS_SALDO_CUSTODIA_INSUFICIENTE";
								Saldo.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,Saldo.CodigoMensaje).add("Solicitado", RegProcesados.ValorRescate.toString()).add("Disponible",SaldoValorRescateARetirar.toString()).msg();
							}
						};
					};
					
					if ((RegProcesados.Cantidad.compareTo(SaldoCantidadCustodia) == -1 || RegProcesados.ValorRescate.compareTo(SaldoValorRescateCustodia) == -1) && (DatoEntrada.AccionOperacion.equals("VCD") || DatoEntrada.AccionOperacion.equals("ANU"))) {					
						for (ObjSaldoCustodia Saldo: Saldos){
							if (!Saldo.IdGenerarMovimientoCustodia.equals( DatoEntrada.IdGenerarMovimientoCustodia)) continue;
							Saldo.RegistroConProblemas  = "S";
							
							if (RegProcesados.Cantidad.compareTo(BigDecimal.ZERO) == 1 ) {
								Saldo.CodigoMensaje = "CORECUS_TOTAL_RETIRAR_INFERIOR_DISPONIBLE";
								Saldo.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,Saldo.CodigoMensaje).add("Solicitado", RegProcesados.Cantidad.toString()).add("Disponible",SaldoCantidadARetirar.toString()).msg();
							}
							if (RegProcesados.ValorRescate.compareTo(BigDecimal.ZERO) == 1 ) {
								Saldo.CodigoMensaje = "CORECUS_TOTAL_RETIRAR_INFERIOR_DISPONIBLE";
								Saldo.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,Saldo.CodigoMensaje).add("Solicitado", RegProcesados.ValorRescate.toString()).add("Disponible",SaldoValorRescateARetirar.toString()).msg();
							}
						};
					};
					
				}
		
			};//04
			
			
			//05 - PREPARAR REGISTROS A RETIRAR
			HashMap<Long,Object> hmDatosEntrada = Utils.List2Hash(in.p_DatosEntrada, "IdGenerarMovimientoCustodia");
			HashMap<Long,Object> hmCarteras = Utils.List2Hash(Carteras, "IdCartera");
			HashMap<Long,Object> hmGarantias = Utils.List2Hash(Garantias, "IdGarantia");
			HashMap<Long,Object> hmSubyacentes = Utils.List2Hash(Subyacentes, "IdSubyacente");
			HashMap<Long,Object> hmCarterasVigentesAnticipo = Utils.List2Hash(CarterasVigentesAnticipo, "FolioCartera");
			List<ObjTablaCartera> paramsActualizacionCartera = new ArrayList<ObjTablaCartera>();
			List<ObjTablaCartera> paramsNoActualizacionCarteras = new ArrayList<ObjTablaCartera>();
			List<ObjTablaGarantia> paramsActualizacionGarantia = new ArrayList<ObjTablaGarantia>();
			List<ObjTablaSubyacente> paramsActualizacionSubyacente = new ArrayList<ObjTablaSubyacente>();
			
			
			IngresoCustodia.IngresoCustodiaEntrada InIngresoCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
			List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> paramsValorizarCustodia = new ArrayList<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada>();
			List<RetornoCartera.ObjDatosEntrada> RetornosTablaCartera= new ArrayList<RetornoCartera.ObjDatosEntrada>();
			
			Long indexRegistroRetornoCartera = 1L;
			HashMap<Long,Long> hashIdGenerarMovCustodiaRegistroRetorno = new HashMap<Long,Long>();
			
			HashMap<Long,List<ObjDatosEntrada>> hmFolioCustodia = new HashMap<Long,List<ObjDatosEntrada>>();
			HashMap<Long,List<ObjDatosEntrada>> hmFolioExtCustodia = new HashMap<Long,List<ObjDatosEntrada>>();
			HashMap<Long,Long> hmFolioCustodiaContador = new HashMap<Long,Long>();
			HashMap<Long,Long> hmFolioExtCustodiaContador = new HashMap<Long,Long>();
			HashMap<Long,BigDecimal> hmFolioCustodiaSumaCantidad = new HashMap<Long,BigDecimal>();
			HashMap<Long,BigDecimal> hmFolioExtCustodiaSumaCantidad = new HashMap<Long,BigDecimal>();
			HashMap<Long,BigDecimal> hmFolioCustodiaSumaValorRescate = new HashMap<Long,BigDecimal>();
			HashMap<Long,BigDecimal> hmFolioExtCustodiaSumaValorRescate = new HashMap<Long,BigDecimal>();
			HashMap<Long,BigDecimal> hmFolioCustodiaSumaMCMI = new HashMap<Long,BigDecimal>();
			HashMap<Long,BigDecimal> hmFolioExtCustodiaSumaMCMI = new HashMap<Long,BigDecimal>();
			
			for (ObjDatosEntrada dato : in.p_DatosEntrada) {
				if(!"VCDPAC".equalsIgnoreCase(dato.IdTipoOperacion) && !"VENCIMIENTO".equalsIgnoreCase(dato.AccionOperacion)) continue;
				
				//Folio Custodia
				List<ObjDatosEntrada> listFolioCustodia = hmFolioCustodia.get(dato.FolioCustodia);
				if(listFolioCustodia == null) {
					listFolioCustodia = new ArrayList<ObjDatosEntrada>();
				}
				listFolioCustodia.add(dato);
				hmFolioCustodia.put(dato.FolioCustodia, listFolioCustodia);
				hmFolioCustodiaContador.put(dato.FolioCustodia, 0L);
				hmFolioCustodiaSumaCantidad.put(dato.FolioCustodia, BigDecimal.ZERO);
				hmFolioCustodiaSumaValorRescate.put(dato.FolioCustodia, BigDecimal.ZERO);
				hmFolioCustodiaSumaMCMI.put(dato.FolioCustodia, BigDecimal.ZERO);
			
				//Folio Ext Custodia
				if(dato.FolioExternoReferencia != null) {
					List<ObjDatosEntrada> listFolioExtCustodia = hmFolioExtCustodia.get(Long.parseLong(dato.FolioExternoReferencia));
					if(listFolioExtCustodia == null) {
						listFolioExtCustodia = new ArrayList<ObjDatosEntrada>();
					}
					
					BigDecimal sumaCantidad = dato.Cantidad;
					if(hmFolioExtCustodiaSumaCantidad.containsKey(Long.parseLong(dato.FolioExternoReferencia))) {
						sumaCantidad = hmFolioExtCustodiaSumaCantidad.get(Long.parseLong(dato.FolioExternoReferencia)).add(dato.Cantidad);
					}
						
					listFolioExtCustodia.add(dato);
					hmFolioExtCustodia.put(Long.parseLong(dato.FolioExternoReferencia), listFolioExtCustodia);
					hmFolioExtCustodiaContador.put(Long.parseLong(dato.FolioExternoReferencia), 0L);
					hmFolioExtCustodiaSumaCantidad.put(Long.parseLong(dato.FolioExternoReferencia), sumaCantidad);
					hmFolioExtCustodiaSumaValorRescate.put(Long.parseLong(dato.FolioExternoReferencia), BigDecimal.ZERO);
					hmFolioExtCustodiaSumaMCMI.put(Long.parseLong(dato.FolioExternoReferencia), BigDecimal.ZERO);
				}
			}
			
			for (ObjSaldoCustodia Saldo: Saldos){
				ObjDatosEntrada DatoEntrada = (ObjDatosEntrada) hmDatosEntrada.get(Saldo.IdGenerarMovimientoCustodia);
								
				if (Saldo.RegistroConProblemas.equalsIgnoreCase("S")) continue;
				
				EstandarizacionRegistroCustodia.ObjRegistroProcesado InfoRetiro = (EstandarizacionRegistroCustodia.ObjRegistroProcesado) hmDatosEstandarizados.get(Saldo.IdGenerarMovimientoCustodia);
				String IdEstado = "";
				
				switch (DatoEntrada.AccionOperacion) {				 
			        case "VENCIMIENTO": IdEstado = "VCD"; break;
			        case "ANTICIPO": IdEstado = "ANT"; break;
			        case "ANULACION": IdEstado = "ANU"; break;
			        case "MODIFICACION": IdEstado = "MOD"; break;
			        default: IdEstado = "LIQ"; break; 
				};
								
				if (Saldo.TablaAfectada.equals("CARTERA")){
					ObjTablaCartera InfoCartera = (ObjTablaCartera) hmCarteras.get(Saldo.IdCustodia);
					ObjTablaCartera InfoCarteraVigenteAnticipo = (ObjTablaCartera) hmCarterasVigentesAnticipo.get(Saldo.FolioCustodia);
					ObjTablaCartera registrosARetirar = new ObjTablaCartera();					
					
					registrosARetirar.IdGenerarMovimientoCustodia 		= InfoRetiro.IdGenerarMovimientoCustodia;
					registrosARetirar.IdGenerarMovimientoCustodiaPadre	= Saldo.IdGenerarMovimientoCustodiaPadre;
					registrosARetirar.AccionOperacion					= DatoEntrada.AccionOperacion;
					if (InfoCartera.IdEstado.equalsIgnoreCase("ANT") || (InfoCartera.IdEstado.equalsIgnoreCase("LIQ") && in.p_DatosEntrada.get(0).IdConcepto.contentEquals("REVFWD"))){
						Utils.copyPojo(InfoCartera, registrosARetirar);
						registrosARetirar.IdCartera 						= Saldo.IdCustodia;
						registrosARetirar.FolioCartera						= Saldo.FolioCustodia;
						registrosARetirar.IdEstado 							= IdEstado;
						registrosARetirar.FechaModificacionRegistro			= Fecha.toDateTime(fechaSistema);
						if(DatoEntrada.IdConcepto.contentEquals("REVFWD")) {
							registrosARetirar.EGRIdConcepto					= "ANTFWD";
						}else {
							registrosARetirar.EGRIdConcepto 				= DatoEntrada.IdConcepto;
						}
						registrosARetirar.EGRFolioComprobante				= registrosARetirar.FolioComprobante;
						if(InfoCarteraVigenteAnticipo != null){
							InfoCarteraVigenteAnticipo.IdEstado 				= IdEstado;
							InfoCarteraVigenteAnticipo.FechaModificacionRegistro= Fecha.toDateTime(fechaSistema);
							InfoCarteraVigenteAnticipo.EGRIdConcepto 			= DatoEntrada.IdConcepto;
							paramsActualizacionCartera.add(InfoCarteraVigenteAnticipo);
						}
						//crear el ingreso de esa cartera.
						IngresoCustodia.ObjDatosEntrada registrosAIngresarAnticipo = new IngresoCustodia.ObjDatosEntrada();
						Utils.copyPojo(InfoCartera, registrosAIngresarAnticipo);
						registrosAIngresarAnticipo.IdGenerarMovimientoCustodia = InfoRetiro.IdGenerarMovimientoCustodia;
						registrosAIngresarAnticipo.AccionOperacion 			= "INGRESO";						
						registrosAIngresarAnticipo.TablaAfectada 				= InfoRetiro.TablaAfectada;
						registrosAIngresarAnticipo.IdCustodia 					= InfoCartera.IdCartera;
						registrosAIngresarAnticipo.EsRegistroNuevo 				= "N";
						registrosAIngresarAnticipo.GenerarNuevoFolioCustodia	= "N";
						registrosAIngresarAnticipo.IdInstrumento 				= InfoCartera.IdInstrumento;
						registrosAIngresarAnticipo.Cantidad 					= InfoCarteraVigenteAnticipo != null?Saldo.CantidadARetirar.add(InfoCarteraVigenteAnticipo.INGCantidad):Saldo.CantidadARetirar;//Saldo.SaldoValorRescate.compareTo( BigDecimal.ZERO ) > 0? Saldo.CantidadARetirar : Saldo.SaldoCantidad;
						registrosAIngresarAnticipo.ValorRescate 				= InfoCarteraVigenteAnticipo != null?Saldo.SaldoValorRescate.add(InfoCarteraVigenteAnticipo.INGValorRescate):Saldo.SaldoValorRescate;//Saldo.SaldoValorRescate;
						registrosAIngresarAnticipo.PrecioCompra					= InfoCartera.INGPrecioCompra;
						registrosAIngresarAnticipo.TasaCompra					= InfoCartera.INGTasaCompra;
						registrosAIngresarAnticipo.NemoSVS 						= InfoCartera.NemoSVS;
						registrosAIngresarAnticipo.FechaOperacion				= InfoRetiro.FechaOperacion;
						registrosAIngresarAnticipo.PlazoEnDiasOperacion			= InfoRetiro.PlazoEnDiasOperacion;
						registrosAIngresarAnticipo.IdEmisor						= InfoRetiro.IdEmisor;
						registrosAIngresarAnticipo.IdTipoReajusteInstrumento	= InfoRetiro.IdTipoReajuste;
						registrosAIngresarAnticipo.FolioCarteraRelacionada 		= InfoCartera.FolioCarteraRelacionada;
						registrosAIngresarAnticipo.IdConcepto = DatoEntrada.IdConcepto;

						
						
						//registrosAIngresarAnticipo.MontoCompra = registrosAIngresarAnticipo.Cantidad.multiply(InfoRetiro.TipoCambioAnticipo);
						registrosAIngresarAnticipo.MontoCompra = InfoRetiro.TipoCambioAnticipo != null ? registrosAIngresarAnticipo.Cantidad.multiply(InfoRetiro.TipoCambioAnticipo) : registrosAIngresarAnticipo.Cantidad;
						registrosAIngresarAnticipo.MontoMonedaPrincipal = registrosAIngresarAnticipo.Cantidad;
						
						if(InfoCartera.Nemotecnico.equals("ARBITR")){
							registrosAIngresarAnticipo.MontoMonedaSecundaria = BigDecimal.ZERO;
							if(DatoEntrada.TipoMonedaPrincipal.equals("D") && !(InfoCartera.ValorParidadCierre.compareTo(BigDecimal.ZERO) == 0)){
								registrosAIngresarAnticipo.MontoMonedaSecundaria = registrosAIngresarAnticipo.Cantidad.divide(InfoCartera.ValorParidadCierre,8,RoundingMode.HALF_UP);
							}
							else if(DatoEntrada.TipoMonedaPrincipal.equals("M")){
								registrosAIngresarAnticipo.MontoMonedaSecundaria = registrosAIngresarAnticipo.Cantidad.multiply(InfoCartera.ValorParidadCierre);
							}
						}
						else{
							registrosAIngresarAnticipo.MontoMonedaSecundaria = registrosAIngresarAnticipo.Cantidad.multiply(InfoCartera.ValorTipoCambioCierre);
						}
						registrosAIngresarAnticipo.EsPuntos = InfoRetiro.EsPuntos;
						registrosAIngresarAnticipo.TasaPuntosMonedaPrincipal = InfoRetiro.TasaPuntosMonedaPrincipal;
						registrosAIngresarAnticipo.TasaPuntosMonedaSecundaria = InfoRetiro.TasaPuntosMonedaSecundaria;
						
						InIngresoCustodia.p_DatosEntrada.add(registrosAIngresarAnticipo);
					}
					else {
						registrosARetirar.IdCartera 						= Saldo.IdCustodia;
						registrosARetirar.FolioCartera						= Saldo.FolioCustodia;
						registrosARetirar.IdEstado 							= IdEstado;
						registrosARetirar.EGRGlosa 							= DatoEntrada.Glosa;
						registrosARetirar.EGRFechaOperacion 				= !"VCDPAC".equalsIgnoreCase(DatoEntrada.IdConcepto) ? InfoRetiro.FechaOperacion : Fecha.toDate(fechaSistema);
						registrosARetirar.EGRIdTipoOperacion 				= InfoRetiro.IdTipoOperacion;
						registrosARetirar.EGRIdOperacion 					= !"VCDPAC".equalsIgnoreCase(DatoEntrada.IdConcepto) ? InfoRetiro.IdOperacion : (!"OPTIMUS".equalsIgnoreCase(DatoEntrada.IdEmisor)? DatoEntrada.IdOperacion : DatoEntrada.IdGenerarMovimientoCustodia);
						registrosARetirar.EGRIdMovimientoCarteraEnTransito 	= DatoEntrada.IdMovimientoCarteraEnTransitoOrigen;
						registrosARetirar.EGRIdTipoCanalIngreso 			= InfoRetiro.IdTipoCanalIngreso;
						registrosARetirar.EGRIdCondicionLiquidacion 		= InfoRetiro.IdCondicionLiquidacion;
						registrosARetirar.EGRFechaLiquidacion	 			= !"VCDPAC".equalsIgnoreCase(DatoEntrada.IdConcepto) ? InfoRetiro.FechaLiquidacion : Fecha.toDate(fechaSistema);
						registrosARetirar.EGRIdTipoComprobante 				= InfoRetiro.IdTipoComprobante;
						registrosARetirar.EGRFolioComprobante 				= "VCDPAC".equals(DatoEntrada.IdTipoOperacion)? DatoEntrada.IdOperacion:InfoRetiro.FolioComprobante;
						registrosARetirar.EGRFechaComprobante 				= InfoRetiro.FechaComprobante;
						registrosARetirar.EGRFolioExternoReferencia 		= InfoRetiro.FolioExternoReferencia;					
						registrosARetirar.EGRIdConcepto 					= InfoRetiro.IdConcepto;
						registrosARetirar.EGRRetiroPorFusion				= DatoEntrada.EsRetiroPorFusion;
						
						registrosARetirar.EGRIdUsuario 						= DatoEntrada.IdUsuario;
						registrosARetirar.EGRIdModulo 						= DatoEntrada.IdModulo;
						registrosARetirar.EGRIdSucursal 					= DatoEntrada.IdSucursal;
						registrosARetirar.EGRIdAgente 						= DatoEntrada.IdAgente;
						registrosARetirar.EGRCantidad 						= Saldo.CantidadARetirar;
						registrosARetirar.EGRValorRescate 					= Saldo.ValorRescateARetirar;
						registrosARetirar.EGRPrecioSucio 					= InfoRetiro.PrecioSucio;
						registrosARetirar.EGRPrecio 						= InfoRetiro.PrecioCompra;
						registrosARetirar.EGRTasa 							= InfoRetiro.TasaCompra;
						//AL SER GARANTIA, EL MONTO VALORIZADO ES CALCULADO RESPECTO A LOS PRECIOS Y TASAS DE LA GARANTIA CREADA
						//SE REQUIERE MANTENER LOS MONTOS RESPECTO AL PRECIO Y TASA DE COMPRA ORIGINAL DE LA GARANTIA
						if(("GAPRFG").equalsIgnoreCase(InfoRetiro.IdConcepto) && ("INGGTA").equalsIgnoreCase(InfoRetiro.IdConcepto)) {
							registrosARetirar.EGRMonto 						= (InfoCartera.INGMontoATasaCompraActual.multiply(DatoEntrada.Cantidad)).divide(InfoCartera.INGCantidad);
						} else if (("VCDTIT").equalsIgnoreCase(InfoRetiro.IdConcepto)) {
							registrosARetirar.EGRMonto 						= InfoCartera.INGMontoATasaMercadoActual;
						}
						else {
							registrosARetirar.EGRMonto 						= InfoRetiro.MontoCompra;
						}
						registrosARetirar.EGRCostoInversionEnFactorAjuste	= Saldo.CostoInversionEnFactorAjusteARetirar;
						registrosARetirar.EGRValorFactorAjusteaFechaCurse	= Saldo.ValorFactorAjusteaFechaCurseARetirar;
						registrosARetirar.EGRValorFactor					= Saldo.ValorFactorARetirar;
						registrosARetirar.EGRMontoCompraFactor				= Saldo.MontoCompraFactorARetirar;
						
						//Info FORWARD
						registrosARetirar.EGRIdSpotAsociado 				= InfoRetiro.IdSpotAsociado;
						registrosARetirar.EGRMontoMonedaPrincipal 			= InfoRetiro.MontoMonedaPrincipal;
						registrosARetirar.EGRMontoMonedaSecundaria 			= InfoRetiro.MontoMonedaSecundaria;
						registrosARetirar.EGRIdMonedaLiquidacion 			= InfoRetiro.IdMonedaLiquidacion;
						registrosARetirar.EGRMontoLiquidacion 				= InfoRetiro.MontoLiquidacion;
						registrosARetirar.EGRMontoLiquidacionMonedaPrincipal = InfoRetiro.MontoLiquidacionMonedaPrincipal;
						registrosARetirar.EGRValorTipoCambioSpot 			= InfoRetiro.ValorTipoCambioSpot;
						registrosARetirar.EGRValorParidad 					= InfoRetiro.ValorParidadCierre;			// Debe venir del negocio o valorizacion???
						registrosARetirar.EGRValorTipoCambioCotizador 		= InfoRetiro.ValorTipoCambioCotizador;
						registrosARetirar.EGREsPuntos 						= InfoRetiro.EsPuntos;
						registrosARetirar.EGRTasaPuntosMonedaPrincipal		= InfoRetiro.TasaPuntosMonedaPrincipal;
						registrosARetirar.EGRTasaPuntosMonedaSecundaria 	= InfoRetiro.TasaPuntosMonedaSecundaria;
						registrosARetirar.EGRTasaDescuentoAnticipo 			= InfoRetiro.TasaDescuentoAnticipo;
						registrosARetirar.EGRTipoCambioAnticipo 			= InfoRetiro.TipoCambioAnticipo;
						registrosARetirar.EGRMontoARecibirAnticipo 			= InfoRetiro.MontoARecibirAnticipo;
						registrosARetirar.EGRTipoCambioObservado 			= InfoRetiro.TipoCambioObservado;
						registrosARetirar.EGRFechaCurseRetiro 				= InfoRetiro.FechaCurse;					// Debe venir del negocio o valorizacion???
						registrosARetirar.EGRCostoRetiroenFactorAjuste 		= InfoRetiro.CostoEnFactorAjuste;			// Debe venir del negocio o valorizacion???
						registrosARetirar.EGRValorFactorAjusteaFechaRetiro 	= InfoRetiro.ValorFactorAjusteaFechaCurse;	// Debe venir del negocio o valorizacion???
						registrosARetirar.FechaModificacionRegistro			= Fecha.toDateTime(fechaSistema);
						registrosARetirar.IdTipoReajuste                    = DatoEntrada.IdTipoReajusteInstrumento != null ? DatoEntrada.IdTipoReajusteInstrumento : InfoRetiro.IdTipoReajuste != null ?InfoRetiro.IdTipoReajuste : "0";						
						registrosARetirar.IdMonedaEmision                   = DatoEntrada.IdMonedaEmision;
						
						//if (DatoEntrada.AccionOperacion.equalsIgnoreCase("VENCIMIENTO") && InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("PRT")){
						if ((DatoEntrada.AccionOperacion.equalsIgnoreCase("VENCIMIENTO") || DatoEntrada.AccionOperacion.equalsIgnoreCase("ANTICIPO")) && InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("SIM")){
							// Para Vencimiento de Prestamos, usa la info que ya viene para poblar estos campos
							registrosARetirar.EGRMontoLiquidacion = InfoRetiro.MontoLiquidacion;
							registrosARetirar.EGRIdMonedaLiquidacion = InfoRetiro.IdMonedaLiquidacion;
							registrosARetirar.EGRMontoAPlazoMonedaInstrumento = InfoRetiro.MontoAPlazoMonedaInstrumento;
							registrosARetirar.EGRMontoAPlazo = InfoRetiro.MontoAPlazo;
							registrosARetirar.EGRPrecioAPlazo = InfoRetiro.PrecioAPlazo;
							registrosARetirar.EGRMontoMercadoMonedaInstrumento = InfoRetiro.MontoCompraMonedaInstrumento;
							registrosARetirar.EGRMontoATasaMercado = InfoRetiro.MontoCompraMonedaInstrumento;
							registrosARetirar.EGRMonto = DatoEntrada.AccionOperacion.equalsIgnoreCase("VENCIMIENTO") ? InfoRetiro.MontoAPlazo : InfoRetiro.MontoCompraMonedaInstrumento;
							
							registrosARetirar.EGRTasaMercado = InfoRetiro.TasaCompra;
							registrosARetirar.EGRPrecioMercado = InfoRetiro.PrecioAPlazo;
							registrosARetirar.EGRMontoMonedaInstrumento = InfoRetiro.MontoCompraMonedaInstrumento;
							
							registrosARetirar.EGRTasa = InfoRetiro.TasaCompra;
							registrosARetirar.EGRPrecio = InfoRetiro.PrecioAPlazo;
						}
						if ((DatoEntrada.AccionOperacion.equalsIgnoreCase("VENCIMIENTO") || DatoEntrada.AccionOperacion.equalsIgnoreCase("ANTICIPO")) && (InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("PRT") || InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("EMP")|| InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("PRF") || InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("ERF"))){
							// Para Vencimiento de Prestamos, usa la info que ya viene para poblar estos campos
							registrosARetirar.EGRIdMonedaLiquidacion = InfoRetiro.IdMonedaLiquidacion;
							registrosARetirar.EGRMonto = InfoRetiro.MontoCompra;
							registrosARetirar.EGRMontoMonedaInstrumento = InfoRetiro.MontoCompraMonedaInstrumento;
							registrosARetirar.EGRMontoAPlazoMonedaInstrumento = InfoRetiro.MontoAPlazoMonedaInstrumento;
							registrosARetirar.EGRMontoAPlazo = InfoRetiro.MontoAPlazo;
							registrosARetirar.EGRPrecioAPlazo = InfoRetiro.PrecioAPlazo;
							registrosARetirar.EGRTasa = InfoRetiro.TasaCompra;
							registrosARetirar.EGRPrecio = InfoRetiro.PrecioCompra;
						}
						if("S".equalsIgnoreCase(DatoEntrada.SIMCarteraPrestadaAlFinancista)) {
							registrosARetirar.EGRPrecio	= InfoRetiro.PrecioAPlazo;
							//Revisar que no hayan nulos
							if(registrosARetirar.EGRCantidad != null && DatoEntrada.Cantidad != null && InfoRetiro.PrecioAPlazo!= null && InfoRetiro.MontoAPlazo != null) {
								//Si la cantidad de egreso es menor a la cantidad de entrada no se realiza un retiro total por lo que se debe recalcular el monto de egreso
								registrosARetirar.EGRMonto	= registrosARetirar.EGRCantidad.compareTo(DatoEntrada.Cantidad) == -1 ? registrosARetirar.EGRCantidad.multiply(InfoRetiro.PrecioAPlazo):InfoRetiro.MontoAPlazo;
							}
						}
					
									
						if (((!"IF".equals(InfoRetiro.IdClasificacionTipoInstrumento) && Saldo.SaldoCantidad.compareTo( BigDecimal.ZERO ) > 0)  || ("IF".equals(InfoRetiro.IdClasificacionTipoInstrumento) && Saldo.SaldoValorRescate.compareTo( BigDecimal.ZERO ) > 0)) && !"VCDPAC".equalsIgnoreCase(InfoRetiro.IdTipoOperacion)){						
							List<String> idsTipoOperacion = new ArrayList<>();
					        idsTipoOperacion.add("RETTIT");
					        idsTipoOperacion.add("INGGTA");
					        idsTipoOperacion.add("VENRF");
					        idsTipoOperacion.add("COMTER");
					        idsTipoOperacion.add("COMPAC");
					        idsTipoOperacion.add("VENPAC");
					        idsTipoOperacion.add("VENTER");
					        idsTipoOperacion.add("RETGTA");
					        
							IngresoCustodia.ObjDatosEntrada IngresoSaldoCartera = new IngresoCustodia.ObjDatosEntrada();						
							IngresoSaldoCartera.IdGenerarMovimientoCustodia = InfoRetiro.IdGenerarMovimientoCustodia;
							IngresoSaldoCartera.AccionOperacion 			= "INGRESO";						
							IngresoSaldoCartera.TablaAfectada 				= InfoRetiro.TablaAfectada;
							IngresoSaldoCartera.IdCustodia 					= Saldo.IdCustodia;//InfoCartera.IdCartera;
							IngresoSaldoCartera.FolioCarteraRelacionada 	= InfoCartera.FolioCarteraRelacionada;
							IngresoSaldoCartera.EsRegistroNuevo 			= "N";
							IngresoSaldoCartera.GenerarNuevoFolioCustodia	= "N";
							IngresoSaldoCartera.IdInstrumento 				= InfoCartera.IdInstrumento;
							IngresoSaldoCartera.Cantidad 					= Saldo.SaldoCantidad;
							IngresoSaldoCartera.ValorRescate 				= Saldo.SaldoValorRescate;
							IngresoSaldoCartera.PrecioCompra				= InfoCartera.INGPrecioCompra;
							IngresoSaldoCartera.TasaCompra					= InfoCartera.INGTasaCompra;
							IngresoSaldoCartera.NemoSVS 					= InfoCartera.NemoSVS;
							IngresoSaldoCartera.FechaOperacion				= idsTipoOperacion.contains(InfoRetiro.IdTipoOperacion) ? InfoRetiro.FechaOperacion : InfoCartera.FechaOperacion;
							IngresoSaldoCartera.FechaLiquidacion			= InfoCartera.FechaLiquidacion;
							IngresoSaldoCartera.PlazoEnDiasOperacion		= InfoRetiro.PlazoEnDiasOperacion;
							IngresoSaldoCartera.IdEmisor					= InfoRetiro.IdEmisor;
							IngresoSaldoCartera.IdTipoReajusteInstrumento	= InfoRetiro.IdTipoReajuste;
							IngresoSaldoCartera.FechaVencimientoOperacion 	= InfoCartera.FechaVencimientoOperacion;
							IngresoSaldoCartera.IdTipoOperacion				= InfoRetiro.IdTipoOperacion;
							IngresoSaldoCartera.CostoEnFactorAjuste			= Saldo.CostoInversionEnFactorAjusteActual;
							IngresoSaldoCartera.ValorFactorAjusteaFechaCurse = Saldo.ValorFactorAjusteaFechaCurseARetirar;
							IngresoSaldoCartera.ValorFactor					= Saldo.ValorFactorARetirar;
							IngresoSaldoCartera.MontoCompraFactor			= Saldo.MontoCompraFactorARetirar;
							IngresoSaldoCartera.MontoAPlazo 				= InfoRetiro.MontoAPlazo;
							IngresoSaldoCartera.MontoAPlazoMonedaInstrumento= InfoRetiro.MontoAPlazoMonedaInstrumento;
							IngresoSaldoCartera.FolioSistemaExterno			= InfoRetiro.FolioSistemaExterno;
							IngresoSaldoCartera.IdTipoProducto				= InfoRetiro.IdTipoProducto;
							IngresoSaldoCartera.FolioGarantiaOrigen			= DatoEntrada.FolioGarantiaOrigen;
							 
							if ("RETIRO".equalsIgnoreCase(DatoEntrada.AccionOperacion) && "FFM".equalsIgnoreCase(InfoRetiro.IdTipoInstrumento) && 
									"RESCFM".equalsIgnoreCase(DatoEntrada.IdTipoOperacion)) {
								IngresoSaldoCartera.MontoCompra					= InfoRetiro.MontoCompra;
								IngresoSaldoCartera.MontoCompraMonedaInstrumento=InfoRetiro.MontoCompraMonedaInstrumento;
							}
							
							registrosARetirar.IdEstado = IdEstado.equals("ANT")?IdEstado:"RPA";
							
							//Para Anticipos Parciales de Forward
							if(IdEstado.equals("ANT") && DatoEntrada.IdTipoOperacion.equals("ANTFWD")){
								registrosARetirar.IdEstado = IdEstado;
								
								IngresoSaldoCartera.MontoCompra = Saldo.SaldoCantidad.multiply(InfoRetiro.TipoCambioAnticipo);
								IngresoSaldoCartera.MontoMonedaPrincipal = Saldo.SaldoCantidad;
								if(InfoCartera.Nemotecnico.equals("ARBITR")){
									IngresoSaldoCartera.MontoMonedaSecundaria = BigDecimal.ZERO;
									if(DatoEntrada.TipoMonedaPrincipal.equals("D") && !(InfoCartera.ValorParidadCierre.compareTo(BigDecimal.ZERO) == 0)){
										IngresoSaldoCartera.MontoMonedaSecundaria = Saldo.SaldoCantidad.divide(InfoCartera.ValorParidadCierre,8,RoundingMode.HALF_UP);
									}
									else if(DatoEntrada.TipoMonedaPrincipal.equals("M")){
										IngresoSaldoCartera.MontoMonedaSecundaria = Saldo.SaldoCantidad.multiply(InfoCartera.ValorParidadCierre);
									}
								}
								else{
									IngresoSaldoCartera.MontoMonedaSecundaria = Saldo.SaldoCantidad.multiply(InfoCartera.ValorTipoCambioCierre);
								}
								IngresoSaldoCartera.EsPuntos = InfoRetiro.EsPuntos;
								IngresoSaldoCartera.TasaPuntosMonedaPrincipal = InfoRetiro.TasaPuntosMonedaPrincipal;
								IngresoSaldoCartera.TasaPuntosMonedaSecundaria = InfoRetiro.TasaPuntosMonedaSecundaria;
							}
							
							if (DatoEntrada.AccionOperacion.equalsIgnoreCase("ANTICIPO") && InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("SIM")  ){
								IngresoSaldoCartera.MontoCompra = IngresoSaldoCartera.Cantidad.multiply(IngresoSaldoCartera.PrecioCompra);
								IngresoSaldoCartera.MontoCompraMonedaInstrumento = IngresoSaldoCartera.MontoCompra;
								IngresoSaldoCartera.PlazoEnDiasOperacion = CuentaDias(IngresoSaldoCartera.FechaOperacion.toString(), IngresoSaldoCartera.FechaVencimientoOperacion.toString(), "C");
								
								IngresoSaldoCartera.PrecioAPlazo = CalculoInteres(IngresoSaldoCartera.MontoCompra, new BigDecimal(IngresoSaldoCartera.PlazoEnDiasOperacion), IngresoSaldoCartera.TasaCompra, new BigDecimal(30) );
								IngresoSaldoCartera.MontoAPlazo = IngresoSaldoCartera.MontoCompra.add(IngresoSaldoCartera.PrecioAPlazo);
								IngresoSaldoCartera.MontoAPlazoMonedaInstrumento = IngresoSaldoCartera.MontoAPlazo; 
							}
							if (DatoEntrada.AccionOperacion.equalsIgnoreCase("ANTICIPO") && (InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("PRT") || InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("EMP") || InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("PRF") || InfoRetiro.IdTipoInstrumento.equalsIgnoreCase("ERF"))  ){
								IngresoSaldoCartera.MontoCompraMonedaInstrumento = DatoEntrada.MontoRestanteMonedaInstrumento;
								IngresoSaldoCartera.MontoCompra = DatoEntrada.MontoRestanteLocal;
								IngresoSaldoCartera.PlazoEnDiasOperacion = CuentaDias(usaFechaLiquidacionEnVencimiento.equalsIgnoreCase(SI) && IngresoSaldoCartera.FechaLiquidacion.after(IngresoSaldoCartera.FechaOperacion) ? IngresoSaldoCartera.FechaLiquidacion.toString() : IngresoSaldoCartera.FechaOperacion.toString(), IngresoSaldoCartera.FechaVencimientoOperacion.toString(), "C");
								
								BigDecimal base = new BigDecimal(DatoEntrada.IdMonedaInstrumento.equalsIgnoreCase("CLP") ? 30 : 360);
								
								IngresoSaldoCartera.PrecioAPlazo = CalculoInteres(IngresoSaldoCartera.MontoCompraMonedaInstrumento, new BigDecimal(IngresoSaldoCartera.PlazoEnDiasOperacion), IngresoSaldoCartera.TasaCompra, base);
 								BigDecimal PrecioPlazoLocal = CalculoInteres(IngresoSaldoCartera.MontoCompra, new BigDecimal(IngresoSaldoCartera.PlazoEnDiasOperacion), IngresoSaldoCartera.TasaCompra, base );
								
								IngresoSaldoCartera.MontoAPlazo = IngresoSaldoCartera.MontoCompra.add(PrecioPlazoLocal);
								IngresoSaldoCartera.MontoAPlazoMonedaInstrumento = IngresoSaldoCartera.MontoCompraMonedaInstrumento.add(IngresoSaldoCartera.PrecioAPlazo); 
							}
							
							if ("PRES".equalsIgnoreCase(DatoEntrada.IdTipoProducto)) {
								IngresoSaldoCartera.IdInstrumentoSubyacente         = DatoEntrada.IdInstrumentoPrestamo; 
								IngresoSaldoCartera.NemotecnicoSubyacente 			= DatoEntrada.NemotecnicoPrestamo;
								IngresoSaldoCartera.IdTipoInstrumentoSubyacente 	= DatoEntrada.IdTipoInstrumentoPrestamo;
								IngresoSaldoCartera.TasaCompraInstrumento 	     	= DatoEntrada.TasaCompraInstrumentoPrestamo;
								IngresoSaldoCartera.IdMonedaInstrumento 	     	= DatoEntrada.IdMonedaInstrumento;
								IngresoSaldoCartera.IdMonedaEmision			 		= DatoEntrada.IdMonedaEmision;
								IngresoSaldoCartera.FechaVencimientoInstrumento 	= DatoEntrada.FechaVencimientoInstrumento;
							}
//							
							InIngresoCustodia.p_DatosEntrada.add(IngresoSaldoCartera);
							indexRegistroRetornoCartera ++;
							
						}
					}
					
					registrosARetirar.TablaAfectada 				= DatoEntrada.TablaAfectada;
					registrosARetirar.IdCliente						= DatoEntrada.IdCliente; 
					registrosARetirar.IdPortafolio					= DatoEntrada.IdPortafolio;
					registrosARetirar.IdClasificacionTipoInstrumento= InfoCartera.IdClasificacionTipoInstrumento; 
					registrosARetirar.IdTipoInstrumento				= InfoCartera.IdTipoInstrumento; 
					registrosARetirar.IdInstrumento					= InfoCartera.IdInstrumento; 
					registrosARetirar.Nemotecnico					= InfoCartera.Nemotecnico; 
					registrosARetirar.FechaVencimientoInstrumento	= InfoCartera.FechaVencimientoInstrumento;
					registrosARetirar.FechaVencimientoOperacion		= InfoCartera.FechaVencimientoOperacion;
					registrosARetirar.FechaEmisionInstrumento		= InfoCartera.FechaEmisionInstrumento;
					registrosARetirar.TieneCorteDeCupon				= InfoCartera.TieneCorteDeCupon;
					registrosARetirar.IdMonedaInstrumento			= InfoCartera.IdMonedaInstrumento;
					registrosARetirar.IdMonedaEmision 				= InfoCartera.IdMonedaEmision;
					registrosARetirar.MontoCompraMonedaInstrumento	= DatoEntrada.MontoCompraMonedaInstrumento;
					registrosARetirar.PlazoEnDiasOperacion 			= DatoEntrada.PlazoEnDiasOperacion;
					registrosARetirar.IdUsuarioMod					= in.p_IdUsuario;
					registrosARetirar.IdEstado = "ANU".equals(IdEstado) && "ANINGA".equals(DatoEntrada.IdTipoOperacion) ? "LIQ" : registrosARetirar.IdEstado;
					if("VENPAC".equalsIgnoreCase(DatoEntrada.IdTipoOperacion) || "VENTER".equalsIgnoreCase(DatoEntrada.IdTipoOperacion)) {
						registrosARetirar.EGRMontoMonedaInstrumento = InfoRetiro.MontoCompraMonedaInstrumento;
					}
					
					boolean existe = false;
					Long countIngresosCartera = InIngresoCustodia.p_DatosEntrada.stream().filter(custodia->custodia.IdCustodia.equals(Saldo.IdCustodia)).count();
					if(countIngresosCartera > 0) {
						for (ObjTablaCartera obj : paramsActualizacionCartera){
							if (obj.FolioCartera.equals(Saldo.FolioCustodia)){
								existe = true;
							}
						}
					}
					if (!existe){
						paramsActualizacionCartera.add(registrosARetirar);
					}
					else {
						paramsNoActualizacionCarteras.add(registrosARetirar);  
					}					
				};//(Saldo.TablaOrigen.equals("CARTERA"))
				
				
				if (Saldo.TablaAfectada.equals("GARANTIA")){
					ObjTablaGarantia InfoGarantia = (ObjTablaGarantia) hmGarantias.get(Saldo.IdCustodia);
					ObjTablaGarantia registrosARetirar = new ObjTablaGarantia();					
					
					registrosARetirar.IdGarantia 				= Saldo.IdCustodia;
					registrosARetirar.IdEstado 					= IdEstado;														
					registrosARetirar.EGRFechaOperacion 		= InfoRetiro.FechaOperacion; 
					registrosARetirar.EGRIdTipoOperacion		= InfoRetiro.IdTipoOperacion;
					registrosARetirar.EGRIdOperacion 			= InfoRetiro.IdOperacion; 					
					registrosARetirar.EGRIdTipoCanalIngreso		= InfoRetiro.IdTipoCanalIngreso; 
					registrosARetirar.EGRIdCondicionLiquidacion = InfoRetiro.IdCondicionLiquidacion;
					registrosARetirar.EGRFechaLiquidacion 		= InfoRetiro.FechaLiquidacion;
					registrosARetirar.EGRIdTipoComprobante		= InfoRetiro.IdTipoComprobante;
					registrosARetirar.EGRFolioComprobante 		= InfoRetiro.FolioComprobante;
					registrosARetirar.EGRFechaComprobante 		= InfoRetiro.FechaComprobante;
					registrosARetirar.EGRFolioExternoReferencia = InfoRetiro.FolioExternoReferencia;
					registrosARetirar.EGRIdMovimientoCarteraEnTransito = DatoEntrada.IdMovimientoCarteraEnTransitoOrigen;
					
					registrosARetirar.EGRIdModulo 				= DatoEntrada.IdModulo; 
					registrosARetirar.EGRIdConcepto 			= DatoEntrada.IdConcepto != null ? DatoEntrada.IdConcepto : InfoGarantia.IdConcepto; 
					registrosARetirar.EGRIdUsuario 				= DatoEntrada.IdUsuario;
					registrosARetirar.EGRIdSucursal 			= DatoEntrada.IdSucursal != null ? DatoEntrada.IdSucursal : InfoGarantia.IdSucursal;
					registrosARetirar.EGRIdAgente 				= DatoEntrada.IdAgente != null ? DatoEntrada.IdAgente : InfoGarantia.IdAgente; 
					registrosARetirar.EGRCantidad 				= Saldo.CantidadARetirar; 
					registrosARetirar.EGRValorRescate 			= Saldo.ValorRescateARetirar;					
					registrosARetirar.EGRPrecio 				= InfoRetiro.PrecioCompra; 
					registrosARetirar.EGRTasa 					= InfoRetiro.TasaCompra; 
					registrosARetirar.EGRMontoMonedaInstrumento	= !InfoGarantia.IdOrigenGarantia.equals("CUS") ? InfoRetiro.MontoCompraMonedaInstrumento : Saldo.CantidadARetirar;
					registrosARetirar.EGRMonto 					= !InfoGarantia.IdOrigenGarantia.equals("CUS") ? InfoRetiro.MontoCompra : Saldo.CantidadARetirar ;
					registrosARetirar.FechaModificacionRegistro	= Fecha.toDateTime(fechaSistema);
					registrosARetirar.IdTipoReajuste			= InfoRetiro.IdTipoReajuste;
					

					if ((!"IF".equals(InfoRetiro.IdClasificacionTipoInstrumento) && Saldo.SaldoCantidad.compareTo( BigDecimal.ZERO ) > 0)  || ("IF".equals(InfoRetiro.IdClasificacionTipoInstrumento) && Saldo.SaldoValorRescate.compareTo( BigDecimal.ZERO ) > 0)){
						
						IngresoCustodia.ObjDatosEntrada IngresoSaldoCartera = new IngresoCustodia.ObjDatosEntrada();						
						IngresoSaldoCartera.IdGenerarMovimientoCustodia = InfoRetiro.IdGenerarMovimientoCustodia;
						IngresoSaldoCartera.AccionOperacion 			= "INGRESO";
						IngresoSaldoCartera.TablaAfectada 				= InfoRetiro.TablaAfectada;
						IngresoSaldoCartera.IdCustodia 					= InfoGarantia.IdGarantia;
						IngresoSaldoCartera.IdOrigenGarantia			= InfoGarantia.IdOrigenGarantia;
						IngresoSaldoCartera.EsRegistroNuevo 			= "N";
						IngresoSaldoCartera.GenerarNuevoFolioCustodia	= "N";
						IngresoSaldoCartera.IdInstrumento 				= InfoGarantia.IdInstrumento;
						IngresoSaldoCartera.Cantidad 					= Saldo.SaldoCantidad;
						IngresoSaldoCartera.ValorRescate 				= Saldo.SaldoValorRescate;
						IngresoSaldoCartera.PrecioCompra				= InfoGarantia.PrecioCompraOperacionOrigen;
						IngresoSaldoCartera.TasaCompra					= InfoGarantia.TasaCompraOperacionOrigen;
						IngresoSaldoCartera.FechaOperacion				= InfoRetiro.FechaOperacion;
						IngresoSaldoCartera.PlazoEnDiasOperacion		= InfoRetiro.PlazoEnDiasOperacion;
						IngresoSaldoCartera.IdEmisor					= InfoGarantia.IdEmisor;
						IngresoSaldoCartera.IdTipoReajusteInstrumento	= InfoGarantia.IdTipoReajuste;
						if (!InfoGarantia.IdOrigenGarantia.equals("CUS")){
							IngresoSaldoCartera.MontoCompra = Saldo.SaldoCantidad;
							IngresoSaldoCartera.MontoCompraMonedaInstrumento = Saldo.SaldoCantidad;
						}
						if("RETGTA".equals(DatoEntrada.IdTipoOperacion) && "GARANTIA".equals(DatoEntrada.TablaAfectada)) {
							IngresoSaldoCartera.IdTipoOperacion = DatoEntrada.IdTipoOperacion;
						}
						InIngresoCustodia.p_DatosEntrada.add(IngresoSaldoCartera);
						
						//registrosARetirar.IdEstado = "RPA";
						registrosARetirar.IdEstado = IdEstado.equals("ANT")?IdEstado:"RPA";
					};
					
					//RETORNO A TABLA CARTERA (no aplica para compras de simultaneas)
					if (!InfoGarantia.IdCarteraOrigen.equals(0L) && ((!"ANT".equals(registrosARetirar.IdEstado) && !"VCD".equals(registrosARetirar.IdEstado)) || (InfoGarantia.IdTipoOperacionOrigen != null && !InfoGarantia.IdTipoOperacionOrigen.equalsIgnoreCase("COMSIM")&& !InfoGarantia.IdTipoOperacionOrigen.equalsIgnoreCase("COMRRV")))){
						if(Saldo.CantidadARetirar.compareTo(BigDecimal.ZERO) > 0) {
							RetornoCartera.ObjDatosEntrada RetornoCartera = new RetornoCartera.ObjDatosEntrada();
							RetornoCartera.IdGenerarMovimientoCustodia	= indexRegistroRetornoCartera;
							RetornoCartera.FechaOperacion				= InfoRetiro.FechaOperacion;
							RetornoCartera.TablaDestino					= "CARTERA";						
							RetornoCartera.IdConcepto 					= InfoRetiro.IdConcepto;
							RetornoCartera.IdCustodiaDestino			= InfoGarantia.IdCarteraOrigen;
							RetornoCartera.FolioCustodiaDestino			= InfoGarantia.FolioCarteraOrigen;
							RetornoCartera.TablaOrigen					= Saldo.TablaAfectada;
							RetornoCartera.IdTipoOperacionOrigen		= InfoRetiro.IdTipoOperacion;
							RetornoCartera.IdOperacionOrigen			= InfoGarantia.IdGarantia;		
							RetornoCartera.EsFungible					= InfoGarantia.EsFungible;
							RetornoCartera.FolioCustodiaOrigen			= InfoGarantia.FolioGarantia;
							RetornoCartera.IdCustodiaOrigen				= InfoGarantia.IdGarantia;
							RetornoCartera.PrecioARetornar				= InfoRetiro.PrecioCompra;
							RetornoCartera.TasaARetornar				= InfoRetiro.TasaCompra;
							RetornoCartera.CantidadARetornar			= Saldo.CantidadARetirar;
							RetornoCartera.ValorRescateARetornar 		= Saldo.ValorRescateARetirar;
							RetornoCartera.FechaLiquidacionARetornar	= registrosARetirar.EGRFechaLiquidacion;
							RetornoCartera.IdModulo						= registrosARetirar.EGRIdModulo;
							RetornoCartera.IdUsuario					= registrosARetirar.EGRIdUsuario;
							RetornoCartera.IdTipoReajuste				= InfoGarantia.IdTipoReajuste;
							RetornoCartera.IdMonedaInstrumento			= InfoGarantia.IdMonedaInstrumento;
							RetornoCartera.IdMovimientoCarteraEnTransitoOrigen = "RETGTA".equals(InfoRetiro.IdTipoOperacion) ? InfoRetiro.IdGenerarMovimientoCustodia : 0L;
							RetornosTablaCartera.add(RetornoCartera);
							hashIdGenerarMovCustodiaRegistroRetorno.put(indexRegistroRetornoCartera, InfoRetiro.IdGenerarMovimientoCustodia);
							indexRegistroRetornoCartera++;							
						}
					};
					registrosARetirar.TablaAfectada 				= DatoEntrada.TablaAfectada;
					registrosARetirar.IdCliente						= DatoEntrada.IdCliente; 
					registrosARetirar.IdPortafolio					= DatoEntrada.IdPortafolio;
					registrosARetirar.IdClasificacionTipoInstrumento= InfoGarantia.IdClasificacionTipoInstrumento; 
					registrosARetirar.IdTipoInstrumento				= InfoGarantia.IdTipoInstrumento; 
					registrosARetirar.IdInstrumento					= InfoGarantia.IdInstrumento; 
					registrosARetirar.Nemotecnico					= InfoGarantia.Nemotecnico; 
					registrosARetirar.FechaVencimientoInstrumento	= InfoGarantia.FechaVencimientoInstrumento;
					registrosARetirar.FechaVencimientoOperacion		= InfoGarantia.FechaVencimientoOperacion;
					registrosARetirar.FechaEmisionInstrumento		= InfoGarantia.FechaEmisionInstrumento;
					registrosARetirar.TieneCorteDeCupon				= InfoGarantia.TieneCorteDeCupon;
					registrosARetirar.IdMonedaInstrumento			= InfoGarantia.IdMonedaInstrumento;	
					registrosARetirar.IdMonedaEmision 				= InfoGarantia.IdMonedaEmision;
					registrosARetirar.INGMontoATasaGarantiaActual	= InfoGarantia.INGMontoATasaGarantiaActual;
					registrosARetirar.INGMontoATasaGarantiaActual	= InfoGarantia.INGMontoATasaGarantiaActual;
					paramsActualizacionGarantia.add(registrosARetirar);
				};//(Saldo.TablaOrigen.equals("GARANTIA"))
				
				
				if (Saldo.TablaAfectada.equals("SUBYACENTE")){
					ObjTablaSubyacente InfoSubyacente = (ObjTablaSubyacente) hmSubyacentes.get(Saldo.IdCustodia);
					ObjTablaSubyacente registrosARetirar = new ObjTablaSubyacente();				
					
					registrosARetirar.IdSubyacente 				= Saldo.IdCustodia;
					registrosARetirar.IdEstado 					= IdEstado;					
					registrosARetirar.EGRFechaOperacion 		= InfoRetiro.FechaOperacion; 
					registrosARetirar.EGRIdTipoCanalIngreso 	= InfoRetiro.IdTipoCanalIngreso; 
					registrosARetirar.EGRIdCondicionLiquidacion = InfoRetiro.IdCondicionLiquidacion; 
					registrosARetirar.EGRFechaLiquidacion 		= InfoRetiro.FechaLiquidacion; 
					registrosARetirar.EGRIdTipoComprobante 		= InfoRetiro.IdTipoComprobante; 
					registrosARetirar.EGRFolioComprobante 		= InfoRetiro.FolioComprobante; 
					registrosARetirar.EGRFechaComprobante 		= InfoRetiro.FechaComprobante; 
					registrosARetirar.EGRFolioExternoReferencia = InfoRetiro.FolioExternoReferencia; 					 
					registrosARetirar.EGRIdTipoOperacion 		= InfoRetiro.IdTipoOperacion; 
					registrosARetirar.EGRIdOperacion 			= InfoRetiro.IdOperacion; 
					registrosARetirar.EGRIdConcepto 			= InfoRetiro.IdConcepto;
					
					registrosARetirar.EGRIdUsuario 				= DatoEntrada.IdUsuario; 
					registrosARetirar.EGRIdModulo 				= DatoEntrada.IdModulo;
					registrosARetirar.EGRIdSucursal 			= DatoEntrada.IdSucursal; 
					registrosARetirar.EGRIdAgente 				= DatoEntrada.IdAgente; 
					registrosARetirar.EGRCantidad 				= Saldo.CantidadARetirar; 
					registrosARetirar.EGRValorRescate 			= Saldo.ValorRescateARetirar;
					registrosARetirar.EGRPrecio 				= InfoRetiro.PrecioCompra;
					registrosARetirar.EGRMonto					= InfoRetiro.MontoCompra;
					registrosARetirar.EGRMontoMonedaInstrumento	= InfoRetiro.MontoCompraMonedaInstrumento;
					registrosARetirar.IdCliente					= DatoEntrada.IdCliente;
					registrosARetirar.IdPortafolio				= DatoEntrada.IdPortafolio;
					registrosARetirar.PlazoEnDiasOperacion		= DatoEntrada.PlazoEnDiasOperacion;
					registrosARetirar.AccionOperacion			= DatoEntrada.AccionOperacion;
					
					if ((DatoEntrada.AccionOperacion.equalsIgnoreCase("VENCIMIENTO") || DatoEntrada.AccionOperacion.equalsIgnoreCase("ANTICIPO") )&& !"PRES".equalsIgnoreCase(DatoEntrada.IdTipoProducto)){
						registrosARetirar.EGRMonto 					= DatoEntrada.MontoCompraMonedaInstrumento;	
					}
					
					registrosARetirar.EGRTasa 					= InfoRetiro.TasaCompra; 
					registrosARetirar.FechaModificacionRegistro	= Fecha.toDateTime(fechaSistema);
					registrosARetirar.IdTipoReajuste			= InfoRetiro.IdTipoReajuste;
					
					if (this.IngresarSaldosSubyacente(Saldo.SaldoCantidad, Saldo.SaldoValorRescate, InfoRetiro.IdTipoOperacion, DatoEntrada.IdTipoOperacion)) {
												
						IngresoCustodia.ObjDatosEntrada IngresoSaldoCartera = new IngresoCustodia.ObjDatosEntrada();						
						IngresoSaldoCartera.IdGenerarMovimientoCustodia = InfoRetiro.IdGenerarMovimientoCustodia;
						IngresoSaldoCartera.AccionOperacion 			= "INGRESO";
						IngresoSaldoCartera.TablaAfectada 				= InfoRetiro.TablaAfectada;
						IngresoSaldoCartera.IdCustodia 					= InfoSubyacente.IdSubyacente;
						IngresoSaldoCartera.EsRegistroNuevo 			= "N";
						IngresoSaldoCartera.GenerarNuevoFolioCustodia	= "N";
						IngresoSaldoCartera.IdInstrumento 				= InfoSubyacente.IdInstrumento;
						IngresoSaldoCartera.Cantidad 					= Saldo.SaldoCantidad;
						IngresoSaldoCartera.ValorRescate 				= Saldo.SaldoValorRescate;
						IngresoSaldoCartera.PrecioCompra				= InfoSubyacente.PrecioCompraOperacionRelacionada;
						IngresoSaldoCartera.TasaCompra					= InfoSubyacente.TasaCompraOperacionRelacionada;
						IngresoSaldoCartera.FechaOperacion				= InfoRetiro.FechaOperacion;
						IngresoSaldoCartera.PlazoEnDiasOperacion		= InfoRetiro.PlazoEnDiasOperacion;
						IngresoSaldoCartera.IdEmisor					= InfoSubyacente.IdEmisor;
						IngresoSaldoCartera.IdTipoReajusteInstrumento	= InfoSubyacente.IdTipoReajuste;
						IngresoSaldoCartera.IdConcepto					= InfoRetiro.IdConcepto;
						IngresoSaldoCartera.IdTipoOperacion				= InfoRetiro.IdTipoOperacion;
						IngresoSaldoCartera.IdTipoProducto				= InfoRetiro.IdTipoProducto;
						if ("PRES".equalsIgnoreCase(DatoEntrada.IdTipoProducto)) {
							IngresoSaldoCartera.PrecioCompra				= InfoRetiro.PrecioCompra;
							IngresoSaldoCartera.TasaCompra					= InfoRetiro.TasaCompra;
						}
						InIngresoCustodia.p_DatosEntrada.add(IngresoSaldoCartera);
						
						//registrosARetirar.IdEstado = "RPA";
						registrosARetirar.IdEstado = IdEstado.equals("ANT")?IdEstado:"RPA";
					};
										
					//Circular 514
					boolean usaTasaInstrumentoRecompraVentaConPacto = "S".equals(parametroTasaRecompraVentaConPacto);
					boolean esOperacionVentaConPacto = "VENPAC".equals(InfoSubyacente.IdTipoOperacionOrigen);
					boolean ventaNoProvieneDeCompraConPacto = !"CC".equals(InfoSubyacente.TipoCarteraCorredor);
					
					if(usaTasaInstrumentoRecompraVentaConPacto && esOperacionVentaConPacto && ventaNoProvieneDeCompraConPacto){
						InfoSubyacente.GeneraCarteraNuevaTasaInstrumentoPacto = "S";
					}
					
					//RETORNO A TABLA CARTERA
					if(("S").equals(DatoEntrada.SubyacenteRetornaCartera)) {
						if (!InfoSubyacente.IdCarteraRetirada.equals(0L) && !InfoSubyacente.IdTipoOperacionOrigen.equalsIgnoreCase("COMSIM") ){
							RetornoCartera.ObjDatosEntrada RetornoCartera = new RetornoCartera.ObjDatosEntrada();
							RetornoCartera.IdGenerarMovimientoCustodia	= InfoRetiro.IdGenerarMovimientoCustodia;
							RetornoCartera.FechaOperacion				= InfoRetiro.FechaOperacion;
							RetornoCartera.TablaDestino					= "CARTERA";
							RetornoCartera.IdConcepto 					= InfoRetiro.IdConcepto;
							RetornoCartera.IdCustodiaDestino			= InfoSubyacente.IdCarteraRetirada;
							RetornoCartera.FolioCustodiaDestino			= InfoSubyacente.FolioCarteraRetirada;
							RetornoCartera.TablaOrigen					= Saldo.TablaAfectada;
							RetornoCartera.IdTipoOperacionOrigen		= (InfoRetiro.IdTipoOperacion!=null && !InfoRetiro.IdTipoOperacion.equals("")) ? InfoRetiro.IdTipoOperacion : InfoSubyacente.IdTipoOperacionOrigen;
							RetornoCartera.FolioCustodiaOrigen			= InfoSubyacente.FolioSubyacente;
							RetornoCartera.IdCustodiaOrigen				= InfoSubyacente.IdSubyacente;
							RetornoCartera.PrecioARetornar				= InfoRetiro.PrecioCompra;
							RetornoCartera.TasaARetornar				= InfoRetiro.TasaCompra;
							RetornoCartera.CantidadARetornar			= Saldo.CantidadARetirar;
							RetornoCartera.ValorRescateARetornar 		= Saldo.ValorRescateARetirar;
							RetornoCartera.FechaLiquidacionARetornar	= registrosARetirar.EGRFechaLiquidacion;
							RetornoCartera.IdMonedaInstrumento          = InfoRetiro.IdMonedaInstrumento;
							RetornoCartera.IdmonedaEmision	            = InfoRetiro.IdMonedaEmision;
							RetornoCartera.IdTipoReajuste	            = InfoRetiro.IdTipoReajuste;
							RetornoCartera.EsFungible					= InfoRetiro.EsFungible;
							RetornoCartera.IdOperacionOrigen			= DatoEntrada.IdOperacion;
							RetornoCartera.PrecioReservaPacto           = InfoSubyacente.PrecioReservaPacto;
							RetornoCartera.TasaReservaPacto				= InfoSubyacente.TasaReservaPacto;
							RetornoCartera.GeneraCarteraNuevaTasaInstrumentoPacto  = InfoSubyacente.GeneraCarteraNuevaTasaInstrumentoPacto;
							RetornosTablaCartera.add(RetornoCartera);
						};
					}
					registrosARetirar.TablaAfectada 				= DatoEntrada.TablaAfectada;
					registrosARetirar.IdCliente						= DatoEntrada.IdCliente; 
					registrosARetirar.IdPortafolio					= DatoEntrada.IdPortafolio;
					registrosARetirar.IdClasificacionTipoInstrumento= InfoSubyacente.IdClasificacionTipoInstrumento; 
					registrosARetirar.IdTipoInstrumento				= InfoSubyacente.IdTipoInstrumento; 
					registrosARetirar.IdInstrumento					= InfoSubyacente.IdInstrumento; 
					registrosARetirar.Nemotecnico					= InfoSubyacente.Nemotecnico; 
					registrosARetirar.FechaVencimientoInstrumento	= InfoSubyacente.FechaVencimientoInstrumento;
					registrosARetirar.FechaLiquidacion				= DatoEntrada.FechaLiquidacion;
					registrosARetirar.FechaEmisionInstrumento		= InfoSubyacente.FechaEmisionInstrumento;
					registrosARetirar.TieneCorteDeCupon				= InfoSubyacente.TieneCorteDeCupon;
					registrosARetirar.IdMonedaInstrumento			= InfoSubyacente.IdMonedaInstrumento;
					registrosARetirar.IdMonedaEmision 				= InfoSubyacente.IdMonedaEmision;
					registrosARetirar.INGMontoATasaSubyacenteActual	= InfoSubyacente.INGMontoATasaSubyacenteActual;
					registrosARetirar.INGMontoATasaMercadoActual	= InfoSubyacente.INGMontoATasaMercadoActual;
					registrosARetirar.PlazoEnDiasOperacion			= DatoEntrada.PlazoEnDiasOperacion;
					paramsActualizacionSubyacente.add(registrosARetirar);	
				};//(Saldo.TablaOrigen.equals("SUBYACENTE"))

			};//for (ObjSaldoCustodia Saldo: Saldos)			
			
			//06 - INGRESAR SALDOS
			HashMap<Long, Object> ingSalMap = new HashMap<Long, Object>();
			if (in.p_SimularOperacion.equalsIgnoreCase("N") && InIngresoCustodia.p_DatosEntrada.size() > 0){			
				InIngresoCustodia.p_IdEmpresa 			= in.p_IdEmpresa;
				InIngresoCustodia.p_SimularOperacion 	= in.p_SimularOperacion;
				InIngresoCustodia.p_TicketAtencion		= in.p_TicketAtencion;
				InIngresoCustodia.p_ValorizacionAutomatica = in.p_ValorizacionAutomatica;
				IngresoCustodia CPP = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaSalida RPP = CPP.logica(con, InIngresoCustodia);
				HashMap<Long, Object> ingEntMap = Utils.List2Hash(InIngresoCustodia.p_DatosEntrada, "IdGenerarMovimientoCustodia");
				ingSalMap = Utils.List2Hash(RPP.P_OUT_DATOSSALIDA, "IdGenerarMovimientoCustodia");
				if (!RPP.P_OUT_ERRORESCONTADOR.equals(0L)){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES = RPP.P_OUT_MENSAJES;
					return out;
				}
				
				for(MensajeSalida mensaje : RPP.P_OUT_MENSAJES) {
					if("ADVERTENCIA".equalsIgnoreCase(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(mensaje);
					}
				}
				
				//actulizo los registro ingresados para el caso que tenga que tenga mas
				List<ObjTablaCartera> paramsActualizacionCarteraNueva = new ArrayList<ObjTablaCartera>();
				
				for (ObjTablaCartera act: paramsActualizacionCartera){
					IngresoCustodia.ObjDatosEntrada ingEnt = (IngresoCustodia.ObjDatosEntrada) ingEntMap.get(act.IdGenerarMovimientoCustodia);
					cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida ingSal = (cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida) ingSalMap.get(act.IdGenerarMovimientoCustodia);
					List<ObjTablaCartera> padre = paramsNoActualizacionCarteras.parallelStream().filter(p-> p != null && act.IdGenerarMovimientoCustodia.equals(p.IdGenerarMovimientoCustodiaPadre)).collect(Collectors.toList());
					if (ingSal != null && ingEnt != null && act.IdCartera.equals(ingEnt.IdCustodia) && padre.size() == 0){// && ingEnt != null*/){
						Optional<ObjSaldoCustodia> saldo1 = Saldos.stream().filter(saldo->saldo.IdCustodia.equals(act.IdCartera)).findFirst();
						BigDecimal saldo2 = saldo1.get().ValorRescateARetirar.compareTo( BigDecimal.ZERO ) > 0 ? saldo1.get().ValorRescateActual : saldo1.get().CantidadActual;
						BigDecimal monto = Saldos.parallelStream().filter(saldo->saldo.IdCustodia.equals(act.IdCartera)).map(saldo->saldo.ValorRescateARetirar.compareTo( BigDecimal.ZERO ) > 0 ? saldo.ValorRescateARetirar : saldo.CantidadARetirar).reduce(BigDecimal.ZERO, (acum,saldo)->acum.add(saldo));
						ObjTablaCartera objUpd = new ObjTablaCartera();
						
						if(saldo2.compareTo(monto) != 1) {
							Utils.copyPojo(act, objUpd);
							objUpd.FolioComprobante = ingSal.FolioComprobante;
							objUpd.FolioCartera = ingSal.FolioCustodia;
							objUpd.IdCartera =  ingSal.IdCustodia;
							if("VCDPAC".equalsIgnoreCase(ingEnt.IdConcepto)) {
								objUpd.IdClienteRelacionado = act.IdCliente;
								objUpd.IdPortafolioRelacionado = act.IdPortafolio;
								objUpd.IdCliente = ingEnt.IdCliente;
								objUpd.IdPortafolio = ingEnt.IdPortafolio;
								objUpd.IdEstado = "VIG";
								objUpd.EGRCantidad = BigDecimal.ZERO;
								objUpd.EGRValorRescate = BigDecimal.ZERO;
								objUpd.EGRFechaComprobante = Date.valueOf("0001-01-01");
								objUpd.EGRFechaLiquidacion = Date.valueOf("0001-01-01");
								objUpd.EGRFechaOperacion = Date.valueOf("0001-01-01");
								objUpd.EGRFolioExternoReferencia = "";
								objUpd.EGRIdConcepto = "";
								objUpd.EGRIdTipoComprobante = "";
								objUpd.EGRIdTipoOperacion = "";
								objUpd.EGRIdOperacion = 0L;
							}
							paramsActualizacionCarteraNueva.add(objUpd);
						}
						
					}
				}
				for (ObjTablaCartera act: paramsNoActualizacionCarteras){
					IngresoCustodia.ObjDatosEntrada ingEnt = (IngresoCustodia.ObjDatosEntrada) ingEntMap.get(act.IdGenerarMovimientoCustodia);
					cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida ingSal = (cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida) ingSalMap.get(act.IdGenerarMovimientoCustodiaPadre);
					if (ingSal != null && ingEnt != null && act.IdCartera.equals(ingEnt.IdCustodia)){// && ingEnt != null){
						ObjTablaCartera objUpd = new ObjTablaCartera();
						Utils.copyPojo(act, objUpd);
						objUpd.FolioComprobante = ingSal.FolioComprobante;
						objUpd.FolioCartera = ingSal.FolioCustodia;
						objUpd.IdCartera =  ingSal.IdCustodia;
						paramsActualizacionCarteraNueva.add(objUpd);
						
					} else if(ingSal != null){
						ObjTablaCartera objUpd = new ObjTablaCartera();
						Utils.copyPojo(act, objUpd);
						objUpd.FolioComprobante = ingSal.FolioComprobante;
						objUpd.FolioCartera = ingSal.FolioCustodia;
						objUpd.IdCartera =  ingSal.IdCustodia;
						paramsActualizacionCarteraNueva.add(objUpd);
					}
				}
				paramsActualizacionCartera.addAll(paramsActualizacionCarteraNueva);
			};
			
			
			//Se agrega valorizacion a tasa instrumento solo para anticipos y vencimientos de ventas con pacto, para mercados RF/IF. 
			List<ObjResultadoValorizacion> valorizaValorPresente = new ArrayList<ObjResultadoValorizacion>();
			HashMap<Long,Object> hashValorPresente = new HashMap<Long,Object>();
			
			List<ObjTablaSubyacente> subyacentesVentasConPacto = Subyacentes.stream().filter(x->("RF".equals(x.IdClasificacionTipoInstrumento)
						|| "IF".equals(x.IdClasificacionTipoInstrumento))
					&& ("VENPAC".equals(x.IdTipoOperacionOrigen)
						|| "COMPAC".equals(x.IdTipoOperacionOrigen))
					&& "VIG".equals(x.IdEstado)).collect(Collectors.toList());
					
			
			if (!subyacentesVentasConPacto.isEmpty()) {
				valorizaValorPresente = valorizarATasaInstrumento(con, in, subyacentesVentasConPacto, fechaSistema, idPais);
				hashValorPresente = Utils.List2Hash(valorizaValorPresente,"IdValorizacion"); 
			}
			
			for (ObjTablaSubyacente subyacente : paramsActualizacionSubyacente) {
				
				ObjResultadoValorizacion valorPresente = (ObjResultadoValorizacion) hashValorPresente.getOrDefault(subyacente.IdSubyacente, new ObjResultadoValorizacion());
				subyacente.EGRValorPresente = valorPresente.MontoMonedaInstrumento == null ? BigDecimal.ZERO : valorPresente.MontoMonedaInstrumento;				
			}
			
			//07 - EJECUTAR CAMBIOS
			if ( in.p_SimularOperacion.equalsIgnoreCase("N") ){
				if (paramsActualizacionCartera.size() > 0){ 
					sql.batch(con, SQL_UpdateCartera, paramsActualizacionCartera, ObjTablaCartera.class);
				};
				if (paramsActualizacionGarantia.size() > 0){ 
					sql.batch(con, SQL_UpdateGarantia, paramsActualizacionGarantia, ObjTablaGarantia.class);
				};
				if (paramsActualizacionSubyacente.size() > 0){ 
					sql.batch(con, SQL_UpdateSubyacente, paramsActualizacionSubyacente, ObjTablaSubyacente.class);
				};
			};
			
			//08 - VALORIZAR REGISTROS RETIRADOS
			for(ObjTablaCartera cartera : paramsActualizacionCartera) {
				if(!"VENCIMIENTO".equalsIgnoreCase(cartera.AccionOperacion) && !"ANTICIPO".equalsIgnoreCase(cartera.AccionOperacion)) {
					ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
					Utils.copyPojo(cartera, ValorizarCustodia);
					ValorizarCustodia.TablaOrigen					= cartera.TablaAfectada;
					ValorizarCustodia.IdCustodia					= cartera.IdCartera;
					ValorizarCustodia.IdCliente						= cartera.IdCliente; 
					ValorizarCustodia.IdPortafolio					= cartera.IdPortafolio;
					ValorizarCustodia.EsRegistroNuevo				= "N";
					ValorizarCustodia.GenerarNuevoFolioCustodia		= "N";
					ValorizarCustodia.IdTipoOperacion				= cartera.EGRIdTipoOperacion;
					ValorizarCustodia.FechaOperacion				= cartera.EGRFechaOperacion;
					ValorizarCustodia.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento; 
					ValorizarCustodia.IdTipoInstrumento				= cartera.IdTipoInstrumento; 
					ValorizarCustodia.IdInstrumento					= cartera.IdInstrumento; 
					ValorizarCustodia.Nemotecnico					= cartera.Nemotecnico; 
					ValorizarCustodia.FechaVencimientoInstrumento	= cartera.FechaVencimientoInstrumento;
					ValorizarCustodia.FechaVencimientoOperacion		= cartera.FechaVencimientoOperacion;
					ValorizarCustodia.FechaEmisionInstrumento		= cartera.FechaEmisionInstrumento;
					ValorizarCustodia.TieneCorteDeCupon				= cartera.TieneCorteDeCupon;
					ValorizarCustodia.IdMonedaInstrumento			= cartera.IdMonedaInstrumento;  //viene
					ValorizarCustodia.IdMonedaEmisionInstrumento 	= cartera.IdMonedaEmision;       //null retiro por fusion
					ValorizarCustodia.IdTipoReajusteInstrumento		= cartera.IdTipoReajuste;  //null retiro por fusion
					ValorizarCustodia.Cantidad						= cartera.EGRCantidad; 
					ValorizarCustodia.ValorRescate					= cartera.EGRValorRescate;
					ValorizarCustodia.PrecioCompra					= cartera.EGRPrecio; 
					ValorizarCustodia.TasaCompra					= cartera.EGRTasa;
					if("VENPAC".equalsIgnoreCase(cartera.EGRIdTipoOperacion) || "VENTER".equalsIgnoreCase(cartera.EGRIdTipoOperacion)) {
						ValorizarCustodia.MontoCompra				= cartera.EGRMonto;
						ValorizarCustodia.MontoCompraMonedaSistema	= cartera.EGRMonto;
					}else {
						ValorizarCustodia.MontoCompra				= cartera.MontoCompraMonedaInstrumento;//InfoCartera.MontoCompra;
						ValorizarCustodia.MontoCompraMonedaSistema	= cartera.MontoCompraMonedaInstrumento;//InfoCartera.INGMontoATasaCompraActual;
					}
					ValorizarCustodia.MontoMercadoMonedaSistema		= cartera.MontoCompraMonedaInstrumento;//InfoCartera.INGMontoATasaMercadoActual;	
					ValorizarCustodia.PlazoEnDiasOperacion 			= cartera.PlazoEnDiasOperacion;
					ValorizarCustodia.OrigenProcesamiento			= "RETIRO_CUSTODIA";
					
					if ("PRT".equalsIgnoreCase(cartera.IdTipoInstrumento) || "PRF".equalsIgnoreCase(cartera.IdTipoInstrumento) || "EMP".equalsIgnoreCase(cartera.IdTipoInstrumento) || "ERF".equalsIgnoreCase(cartera.IdTipoInstrumento) ) {
						ObjDatosEntrada DatoEntrada = (ObjDatosEntrada) hmDatosEntrada.get(cartera.IdGenerarMovimientoCustodia);
						ValorizarCustodia.IdInstrumento = DatoEntrada.IdInstrumentoPrestamo;
						ValorizarCustodia.Nemotecnico = DatoEntrada.NemotecnicoPrestamo;
						ValorizarCustodia.IdMonedaInstrumento = DatoEntrada.IdMonedaInstrumento;
						ValorizarCustodia.FechaOperacion = DatoEntrada.FechaOperacion;
						if ("RF".equalsIgnoreCase(DatoEntrada.IdClasificacionTipoInstrumento)) {
							ValorizarCustodia.TasaCompra = DatoEntrada.TasaCompraInstrumentoPrestamo;
						}
					}
					paramsValorizarCustodia.add(ValorizarCustodia);
				}
			}
			
			for(ObjTablaGarantia garantia : paramsActualizacionGarantia) {
				if ("CUS".equals(garantia.IdOrigenGarantia)) {
					ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
					Utils.copyPojo(garantia, ValorizarCustodia);
					ValorizarCustodia.TablaOrigen					= garantia.TablaAfectada;
					ValorizarCustodia.IdCustodia					= garantia.IdGarantia;
					ValorizarCustodia.IdCliente						= garantia.IdCliente; 
					ValorizarCustodia.IdPortafolio					= garantia.IdPortafolio;
					ValorizarCustodia.EsRegistroNuevo				= "N";
					ValorizarCustodia.GenerarNuevoFolioCustodia		= "N";
					ValorizarCustodia.FechaOperacion				= garantia.EGRFechaOperacion;
					ValorizarCustodia.IdClasificacionTipoInstrumento = garantia.IdClasificacionTipoInstrumento; 
					ValorizarCustodia.IdTipoInstrumento				= garantia.IdTipoInstrumento; 
					ValorizarCustodia.IdInstrumento					= garantia.IdInstrumento; 
					ValorizarCustodia.Nemotecnico					= garantia.Nemotecnico; 
					ValorizarCustodia.FechaVencimientoInstrumento	= garantia.FechaVencimientoInstrumento;
					ValorizarCustodia.FechaVencimientoOperacion		= garantia.FechaVencimientoOperacion;
					ValorizarCustodia.FechaEmisionInstrumento		= garantia.FechaEmisionInstrumento;
					ValorizarCustodia.TieneCorteDeCupon				= garantia.TieneCorteDeCupon;
					ValorizarCustodia.IdMonedaInstrumento			= garantia.IdMonedaInstrumento;	
					ValorizarCustodia.IdMonedaEmisionInstrumento 	= garantia.IdMonedaEmision;
					ValorizarCustodia.IdTipoReajusteInstrumento		= garantia.IdTipoReajuste;
					ValorizarCustodia.Cantidad						= garantia.EGRCantidad; 
					ValorizarCustodia.ValorRescate					= garantia.EGRValorRescate;
					ValorizarCustodia.PrecioCompra					= garantia.EGRPrecio; 
					ValorizarCustodia.TasaCompra					= garantia.EGRTasa;
					ValorizarCustodia.MontoCompraGarantia			= !garantia.IdOrigenGarantia.equals("CUS") ? garantia.EGRMontoMonedaInstrumento : null;
					ValorizarCustodia.MontoCompraMonedaSistema		= garantia.INGMontoATasaGarantiaActual;
					ValorizarCustodia.MontoMercadoMonedaSistema		= garantia.INGMontoATasaMercadoActual;
					ValorizarCustodia.OrigenProcesamiento			= "RETIRO_CUSTODIA";
					paramsValorizarCustodia.add(ValorizarCustodia);
				}
			}
			
			for(ObjTablaSubyacente subyacente : paramsActualizacionSubyacente) {
				if(!"VENCIMIENTO".equalsIgnoreCase(subyacente.AccionOperacion) && !"ANTICIPO".equalsIgnoreCase(subyacente.AccionOperacion)) {
					//VALORIZACION
					ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
					Utils.copyPojo(subyacente, ValorizarCustodia);
					ValorizarCustodia.TablaOrigen					= subyacente.TablaAfectada;
					ValorizarCustodia.IdCustodia					= subyacente.IdSubyacente;
					ValorizarCustodia.IdCliente						= subyacente.IdCliente; 
					ValorizarCustodia.IdPortafolio					= subyacente.IdPortafolio;
					ValorizarCustodia.EsRegistroNuevo				= "N";
					ValorizarCustodia.FechaOperacion				= subyacente.EGRFechaOperacion;
					ValorizarCustodia.IdClasificacionTipoInstrumento = subyacente.IdClasificacionTipoInstrumento; 
					ValorizarCustodia.IdTipoInstrumento				= subyacente.IdTipoInstrumento; 
					ValorizarCustodia.IdInstrumento					= subyacente.IdInstrumento; 
					ValorizarCustodia.Nemotecnico					= subyacente.Nemotecnico; 
					ValorizarCustodia.FechaVencimientoInstrumento	= subyacente.FechaVencimientoInstrumento;
					ValorizarCustodia.FechaVencimientoOperacion		= subyacente.FechaLiquidacion;
					ValorizarCustodia.FechaEmisionInstrumento		= subyacente.FechaEmisionInstrumento;
					ValorizarCustodia.TieneCorteDeCupon				= subyacente.TieneCorteDeCupon;
					ValorizarCustodia.IdMonedaInstrumento			= subyacente.IdMonedaInstrumento;
					ValorizarCustodia.IdMonedaEmisionInstrumento 	= subyacente.IdMonedaEmision;
					ValorizarCustodia.IdTipoReajusteInstrumento		= subyacente.IdTipoReajuste; // moneda agregada
					ValorizarCustodia.Cantidad						= subyacente.EGRCantidad; 
					ValorizarCustodia.ValorRescate					= subyacente.EGRValorRescate;
					ValorizarCustodia.PrecioCompra					= subyacente.EGRPrecio; 
					ValorizarCustodia.TasaCompra					= subyacente.EGRTasa;
					ValorizarCustodia.MontoCompraMonedaSistema		= subyacente.INGMontoATasaSubyacenteActual;
					ValorizarCustodia.MontoMercadoMonedaSistema		= subyacente.INGMontoATasaMercadoActual;
					ValorizarCustodia.PlazoEnDiasOperacion			= subyacente.PlazoEnDiasOperacion;
					ValorizarCustodia.OrigenProcesamiento			= "RETIRO_CUSTODIA";
					paramsValorizarCustodia.add(ValorizarCustodia);
				}
			}
			
			ValorizarCustodia.ValorizarCustodiaEntrada VIE = new ValorizarCustodia.ValorizarCustodiaEntrada();				
			VIE.p_IdEmpresa				= in.p_IdEmpresa;	
			VIE.p_IdUsuario				= in.p_IdUsuario;
			VIE.p_IdModulo				= "CUS";
			VIE.p_IdPais 				= empresa.IdPais;
			VIE.p_OrigenProcesamiento 	= "RETIRO_CUSTODIA";
			VIE.p_InstrumentosAValorizar = paramsValorizarCustodia;		
			
			if (in.p_SimularOperacion.equalsIgnoreCase("N") && paramsValorizarCustodia.size() > 0 && in.p_ValorizacionAutomatica.equalsIgnoreCase("S")){
					
				ValorizarCustodia VI = new ValorizarCustodia();
				ValorizarCustodia.ValorizarCustodiaSalida VIS = VI.logica(con, VIE);
				if (!VIS.P_OUT_ERRORESCONTADOR.equals(0L)){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES = VIS.P_OUT_MENSAJES;
					return out;
				};
			};
			
			if (in.p_SimularOperacion.equalsIgnoreCase("N") && paramsValorizarCustodia.size() > 0 && in.p_ValorizacionAutomatica.equalsIgnoreCase("N")){
				CacheValorizacion.agregar(VIE);
			}
			
			//09 - RETORNOS TABLA CARTERA
			if (in.p_SimularOperacion.equalsIgnoreCase("N") && RetornosTablaCartera.size() > 0){
				RetornoCartera.RetornoCarteraEntrada RCE = new RetornoCartera.RetornoCarteraEntrada();
				RCE.p_IdEmpresa			= in.p_IdEmpresa;
				RCE.p_TicketAtencion	= in.p_TicketAtencion;
				RCE.p_ValorizacionAutomatica = in.p_ValorizacionAutomatica;
				RCE.p_DatosEntrada		= RetornosTablaCartera;
				RetornoCartera RC = new RetornoCartera();
				RetornoCartera.RetornoCarteraSalida RCS = RC.logica(con, RCE);
				if (!RCS.P_OUT_ERRORESCONTADOR.equals(0L)){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES = RCS.P_OUT_MENSAJES;
					return out;
				};			
			};
					
			
			//10 - PREPARAR SALIDA
			out.P_OUT_CARTERAS_RETIRADAS.addAll(paramsActualizacionCartera);
			for (ObjDatosEntrada entrada: in.p_DatosEntrada){
				List<ObjSaldoCustodia> saldosEntrada = Saldos.parallelStream().filter(saldo->saldo.IdGenerarMovimientoCustodia.equals(entrada.IdGenerarMovimientoCustodia) || (saldo.IdGenerarMovimientoCustodia.equals(entrada.IdGenerarMovimientoCustodia*-1) && "VCDPAC".equalsIgnoreCase(entrada.IdConcepto))).collect(Collectors.toList());
				if (saldosEntrada.size() > 0){					
					ArrayList<String> procesado = new ArrayList<>();
					for (ObjSaldoCustodia Saldo: Saldos){
						if (!Saldo.IdGenerarMovimientoCustodia.equals(entrada.IdGenerarMovimientoCustodia)) continue;
						RetiroCustodiaSalida.ObjDatosSalida InfoSalida = new RetiroCustodiaSalida.ObjDatosSalida();
						Utils.copyPojo(Saldo, InfoSalida);
						InfoSalida.TablaOrigen 	= Saldo.TablaAfectada;
						InfoSalida.Cantidad		= Saldo.CantidadARetirar;
						InfoSalida.ValorRescate	= Saldo.ValorRescateARetirar;
						InfoSalida.CodigoMensaje =  InfoSalida.CodigoMensaje == null || InfoSalida.CodigoMensaje.equals("")? "CARTERA_RETIRADA": InfoSalida.CodigoMensaje;
						InfoSalida.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,InfoSalida.CodigoMensaje).add("FolioCartera", Saldo.FolioCustodia).msg();
						if("GARANTIA".equals(InfoSalida.TablaOrigen)) {
							IngresoCustodiaSalida.ObjDatosSalida garantiaGenerada = (IngresoCustodiaSalida.ObjDatosSalida) ingSalMap.get(Saldo.IdGenerarMovimientoCustodia);
							InfoSalida.FolioCustodia = garantiaGenerada != null ? garantiaGenerada.FolioCustodia: 0L;
							InfoSalida.IdGarantiaGenerada = garantiaGenerada != null ? garantiaGenerada.IdCustodia : 0L;
							
						}
						out.P_OUT_DATOSSALIDA.add(InfoSalida);
						
						String key = Saldo.IdGenerarMovimientoCustodia +"-"+ Saldo.Mensaje;		
						if (!procesado.contains(key) && Saldo.Mensaje != null) {
							if (Saldo.RegistroConProblemas.equals("S")) out.P_OUT_ERRORESCONTADOR = 1L;
							InfoSalida.Mensaje = InfoSalida.Mensaje.concat(Saldo.Mensaje+"; ");
							procesado.add(key);
						};
					};					
				} else {
					RetiroCustodiaSalida.ObjDatosSalida InfoSalida = new RetiroCustodiaSalida.ObjDatosSalida();	
					InfoSalida.IdGenerarMovimientoCustodia	= entrada.IdGenerarMovimientoCustodia;
					InfoSalida.TablaOrigen					= entrada.TablaAfectada;
					InfoSalida.IdCustodia					= entrada.IdCustodia;
					InfoSalida.FolioCustodia				= entrada.FolioCustodia;
					InfoSalida.RegistroConProblemas			= "S";
					
					if (CustodiasDescartadas.size() > 0){
						InfoSalida.CodigoMensaje = "CORECUS_MENSAJE_ERROR_POR_CRITERIOS";
						InfoSalida.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,InfoSalida.CodigoMensaje).msg();
						
						ArrayList<String> procesado = new ArrayList<>();
						for (ObjCustodiaDescartada CD: CustodiasDescartadas){
							if ( !InfoSalida.IdGenerarMovimientoCustodia.equals(CD.IdGenerarMovimientoCustodia) ) continue;
							String key = CD.IdGenerarMovimientoCustodia +"-"+ CD.MotivoDescarte;
							
							if (!procesado.contains(key)) {
								InfoSalida.Mensaje = InfoSalida.Mensaje.concat(CD.MotivoDescarte+"; ");
								procesado.add(key);
					        };
						};
					} else {
						InfoSalida.CodigoMensaje = "NO_SE_ENCONTRO_CARTERA_PARA_RETIRAR";
						InfoSalida.Mensaje = Diccionario.getMensaje(con,in.p_IdEmpresa,InfoSalida.CodigoMensaje).msg();
					};
					
					out.P_OUT_DATOSSALIDA.add(InfoSalida);
					out.P_OUT_ERRORESCONTADOR = 1L;	
				};
				
				
				MensajeSalida msj = new MensajeSalida();
				List<Long> listMensajes = new ArrayList<>();
				for (ObjDatosSalida dat : out.P_OUT_DATOSSALIDA) {
					
					msj.descripcion = dat.Mensaje;
					msj.id = dat.IdGenerarMovimientoCustodia;
					msj.codigo = dat.CodigoMensaje;
					
					if (dat.RegistroConProblemas.equalsIgnoreCase("S")) {
						msj.tipo = MensajeSalida.MENSAJE_ERROR;
					}
					else {
						msj.tipo = MensajeSalida.MENSAJE_INFORMACION;
					}
					if (!listMensajes.contains(entrada.IdGenerarMovimientoCustodia)) {
						listMensajes.add(entrada.IdGenerarMovimientoCustodia);
						out.P_OUT_MENSAJES.add(msj);	
					}
				}
			};
		}
		catch (Exception e) {
			throw e;
		}
		
		logger.info("TERMINO");
		return out;
	};

	public HashMap<String,Object> CalculoSaldo(ObjSaldoCustodia Saldo, String AccionOperacion, String pasaAGarantiaReporteSimultanea, BigDecimal CantidadRetirada, BigDecimal ValorRescateRetirado, EstandarizacionRegistroCustodia.ObjRegistroProcesado RegProcesados) {
		HashMap<String,Object> result = new HashMap<>();
		
		BigDecimal SD = Saldo.CantidadActual.subtract(CantidadRetirada);
		
		if(RegProcesados.IdClasificacionTipoInstrumento != null && RegProcesados.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && RegProcesados.EsFungible.equalsIgnoreCase("S") && Saldo.CantidadActual.compareTo(new BigDecimal(1)) == 0) {
			Saldo.CantidadARetirar 	= Saldo.CantidadActual;
			Saldo.SaldoCantidad 	= Saldo.CantidadActual;
			CantidadRetirada 		= BigDecimal.ZERO;
		} else if (SD.compareTo( BigDecimal.ZERO ) < 0 ){
			Saldo.CantidadARetirar 	= Saldo.CantidadActual;
			Saldo.SaldoCantidad 	= BigDecimal.ZERO;
			CantidadRetirada 		= CantidadRetirada.subtract(Saldo.CantidadARetirar);
		} else {
			Saldo.CantidadARetirar 	= CantidadRetirada;
			Saldo.SaldoCantidad 	= SD;
			CantidadRetirada 		= BigDecimal.ZERO;
		};
							
		BigDecimal SVR = Saldo.ValorRescateActual.subtract(ValorRescateRetirado);
		if (SVR.compareTo( BigDecimal.ZERO ) < 0 ){
			Saldo.ValorRescateARetirar 	= Saldo.ValorRescateActual;
			Saldo.SaldoValorRescate 	= BigDecimal.ZERO;
			ValorRescateRetirado 		= ValorRescateRetirado.subtract(Saldo.ValorRescateARetirar);
		} else {
			Saldo.ValorRescateARetirar 	= ValorRescateRetirado;
			Saldo.SaldoValorRescate 	= SVR;
			ValorRescateRetirado 		= BigDecimal.ZERO;
		};
		
		
		result.put("Saldo", Saldo);
		result.put("CantidadRetirada", CantidadRetirada);
		result.put("ValorRescateRetirado", ValorRescateRetirado);
		
        return result;
    };
    
    private String obtenerParametroUsoTasaRecompraVentaConPacto(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::RetiroCustodia.obtenerParametroUsoTasaRecompraVentaConPacto");
		String usaTasaReccompraVentaConPacto = NO;
		try {
			usaTasaReccompraVentaConPacto = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "MESA", "TASARECOMPRAVENTACONPACTO");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::RetiroCustodia.obtenerParametroUsoTasaRecompraVentaConPacto::no existe parametro TASARECOMPRAVENTACONPACTO, se deja valor por defecto.");
		}
		
		logger.trace("FIN::RetiroCustodia.obtenerParametroUsoTasaRecompraVentaConPacto");
		return usaTasaReccompraVentaConPacto;
	}
    
    public List<ObjResultadoValorizacion> valorizarATasaInstrumento (Connection con, RetiroCustodiaEntrada in, List<ObjTablaSubyacente> listaSubyacentes, String fechaSistema, long idPais) throws Exception {
		
		logger.trace("INICIO::ValorizaValorPresente.valorizar");
	    long inicioMetodo = System.currentTimeMillis();    
	    List<ObjEntradaInstrumentos> listaIngresoValorizadorValorPresente = new ArrayList<ObjEntradaInstrumentos>();
	    ValorizarInstrumentoEntrada ValIn = new ValorizarInstrumentoEntrada();
	    
		for (ObjTablaSubyacente objValSubya : listaSubyacentes) {
			ValorizarInstrumento.ObjEntradaInstrumentos objetoValorizador = new ValorizarInstrumento.ObjEntradaInstrumentos();
			objetoValorizador.IdValorizacion = objValSubya.IdSubyacente;
			objetoValorizador.OrigenProcesamiento = "MESA_UI";			
			objetoValorizador.Nemotecnico = objValSubya.Nemotecnico;
			objetoValorizador.Cantidad = objValSubya.Cantidad;
			objetoValorizador.FechaValorizacion = fechaSistema;
			objetoValorizador.FechaEmision = fechaSistema;
			objetoValorizador.FechaOperacion = fechaSistema;
			objetoValorizador.ValorRescate = objValSubya.ValorRescate;
			objetoValorizador.TasaCompra = objValSubya.TasaReservaPacto;
			objetoValorizador.TasaSubyacente = objValSubya.TasaReservaPacto;
			objetoValorizador.IdInstrumento = objValSubya.IdInstrumento;
			objetoValorizador.Emisor       = objValSubya.IdEmisor;
			objetoValorizador.IdMonedaEmisionInstrumento = objValSubya.IdMonedaEmision;
			objetoValorizador.IdTipoInstrumento = objValSubya.IdTipoInstrumento;
			objetoValorizador.FechaVencimiento = (objValSubya.FechaVencimientoInstrumento != null) ? new SimpleDateFormat("yyyy-MM-dd").format(objValSubya.FechaVencimientoInstrumento) : null;
			objetoValorizador.FechaVencimientoOperacion = (objValSubya.FechaVencimientoInstrumento != null) ? new SimpleDateFormat("yyyy-MM-dd").format(objValSubya.FechaVencimientoInstrumento) : null;
			objetoValorizador.IdClasificacionTipoInstrumento = objValSubya.IdClasificacionTipoInstrumento;
			objetoValorizador.FechaDeLiquidacion = (objValSubya.FechaLiquidacion != null) ? new SimpleDateFormat("yyyy-MM-dd").format(objValSubya.FechaLiquidacion) : null;
			objetoValorizador.IdCondicionDeLiquidacion = objValSubya.IdCondicionLiquidacion;
			objetoValorizador.IdMonedaInstrumento = objValSubya.IdMonedaInstrumento;
			objetoValorizador.IdTipoReajusteInstrumento = objValSubya.IdTipoReajuste;
			listaIngresoValorizadorValorPresente.add(objetoValorizador);

		}
	    
	    ValorizarInstrumentoSalida ResultadoValorizador = new ValorizarInstrumentoSalida();
	    ValorizarInstrumento valorizador = new ValorizarInstrumento();
	  
	 // Datos generales
	    ValIn.p_IdEmpresa         = in.p_IdEmpresa;
	    ValIn.p_IdUsuario         = in.p_IdUsuario;
	    ValIn.p_IdModulo          = in.p_IdModulo;
	    ValIn.p_IdPais            = idPais;
	    ValIn.p_IdMonedaAdicional = in.p_IdMonedaAdicional;
	   

	    // Llamar valorizador real
		ValIn.p_Instrumentos = listaIngresoValorizadorValorPresente;
	    ResultadoValorizador = valorizador.logica(con, ValIn);
	    List<ObjResultadoValorizacion> InstrumentosValorizados = ResultadoValorizador.P_OUT_INSTRUMENTOSVALORIZADOS;
	    
	   
	    
	    //filtrar datos importantes: COMP - IdSubyacente - ValorPresente
	    long finMetodo = System.currentTimeMillis();
	    logger.trace("FIN::ValorizaValorPresente.valorizar : " + (finMetodo - inicioMetodo) + " ms");
	    
	    return InstrumentosValorizados.stream().filter(x -> "COMP".equals(x.DimensionTasa)).collect(Collectors.toList());	    
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		RetiroCustodiaEntrada jin=gson.fromJson(in.toString() ,RetiroCustodiaEntrada.class);
		RetiroCustodiaSalida out=logica(conn,jin);		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":null,\"p_SimularOperacion\":\"N\",\"p_TicketAtencion\":1,\"p_DatosEntrada\":[{\"IdGenerarMovimientoCustodia\":1,\"TablaAfectada\":\"CARTERA\",\"EsRetiroNegocio\":null,\"EsRetiroPorFusion\":\"N\",\"IdCustodia\":1523434,\"FolioCustodia\":20685092,\"IdCarteraOrigen\":null,\"IdMovimientoCarteraEnTransitoOrigen\":null,\"IdTipoComprobanteBusqueda\":null,\"FolioComprobanteBusqueda\":null,\"AccionOperacion\":\"ANULACION\",\"FechaRetiroRegistro\":\"2018-08-17 18:37:31.000\",\"IdCliente\":123569,\"IdPortafolio\":27499,\"FechaOperacion\":\"2018-08-17 00:00:00.000\",\"IdTipoOperacion\":\"REVFWD\",\"IdTipoProducto\":null,\"PlazoEnDiasOperacion\":null,\"IdOperacion\":1655,\"IdConcepto\":\"REVFWD\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-08-22 00:00:00.000\",\"IdTipoCanalIngreso\":null,\"IdTipoComprobante\":null,\"FolioComprobante\":1655,\"FechaComprobante\":null,\"FolioExternoReferencia\":null,\"IdSucursal\":1,\"IdAgente\":10003,\"IdUsuario\":33,\"IdModulo\":\"MFW\",\"Glosa\":\"\",\"IdOrigenGarantia\":null,\"IdInstrumento\":346370,\"IdMonedaInstrumento\":\"USD\",\"IdMonedaEmision\":null,\"IdTipoReajusteInstrumento\":null,\"IdEmisor\":null,\"EsFungible\":null,\"IdBoveda\":null,\"CuentaBoveda\":null,\"EsNominal\":null,\"FechaVencimientoInstrumento\":null,\"NemoSVS\":null,\"Cantidad\":125.00000000,\"CorteMinimo\":null,\"ValorRescate\":0,\"PrecioSucio\":null,\"PrecioCompra\":0E-8,\"TasaCompra\":600.00000000,\"MontoCompraMonedaInstrumento\":null,\"TasaAgente\":null,\"PrecioAPlazo\":null,\"MontoAPlazo\":null,\"MontoAPlazoMonedaInstrumento\":null,\"IdSpotAsociado\":0,\"IdModalidadCumplimiento\":6,\"MontoMonedaPrincipal\":125.00000000,\"MontoMonedaSecundaria\":77500.0000000000000000,\"IdMonedaLiquidacion\":null,\"MontoLiquidacion\":0,\"MontoLiquidacionMonedaPrincipal\":null,\"MontoDiferido\":0E-8,\"MontoUtilidadDiferida\":null,\"MontoPerdidaDiferida\":null,\"MontoMargen\":0E-8,\"MontoValorRazonableActivo\":null,\"MontoValorRazonablePasivo\":null,\"MontoValorRazonableNeto\":null,\"TasaValorRazonableActivo\":null,\"TasaValorRazonablePasivo\":null,\"MontoDevengoTipoCambio\":null,\"MontoDevengoTipoCambioAnterior\":null,\"MontoDiferidoDiario\":null,\"MontoDiferidoAcumulado\":null,\"ValorTipoCambioSpot\":null,\"ValorTipoCambioCotizador\":null,\"EsPuntos\":\"S\",\"TasaPuntosMonedaPrincipal\":0E-8,\"TasaPuntosMonedaSecundaria\":600.00000000,\"MontoMatriz\":0,\"TasaDescuentoAnticipo\":0E-8,\"TipoCambioAnticipo\":0E-8,\"MontoARecibirAnticipo\":0E-8,\"TipoCambioObservado\":0E-8,\"FechaEfectiva\":\"2018-08-17 00:00:00.000\",\"IdPlaza\":2931,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01 00:00:00.000\",\"HoraFixing\":\"12:00:00 AM\",\"IdReferenciaMercado\":0,\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"ValorTipoCambioCierre\":620.00000000,\"ValorParidadCierre\":0E-8,\"ValorTipoCambioCosto\":600.00000000,\"ValorParidadCosto\":0,\"TipoMonedaPrincipal\":null,\"IdCarga\":null,\"IdSecuenciaCarteraMigracion\":null,\"CodigoPeriodo\":null}],\"p_ValorizacionAutomatica\":\"N\"}";
				RetiroCustodiaEntrada Objin = gson.fromJson(json, RetiroCustodiaEntrada.class);
							
				RetiroCustodia  kk = new RetiroCustodia();
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
	};
	
	public static BigDecimal CalculoInteres (BigDecimal Monto, BigDecimal Plazo, BigDecimal Tasa, BigDecimal Base){
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal tasaDivCien = BigDecimal.ZERO;
		BigDecimal plazoDivBase = BigDecimal.ZERO;
		
		//return Number(monto * ( 1 + (tasa/100) * (plazo /base) ));
		
		tasaDivCien = Tasa.divide(new BigDecimal(100), MathContext.DECIMAL128);
		plazoDivBase = Plazo.divide(Base, MathContext.DECIMAL128);
		
		total = Monto.multiply( BigDecimal.ONE.add(tasaDivCien.multiply(plazoDivBase, MathContext.DECIMAL128), MathContext.DECIMAL128), MathContext.DECIMAL128 );
		total = total.subtract(Monto, MathContext.DECIMAL128);
		
		return total;
	}
	
	public Boolean IngresarSaldosSubyacente(BigDecimal cantidad, BigDecimal valorRescate , String tipoOperacionCartera , String tipoOperacionEntrada) {
		logger.trace("INICIO::RetiroCustodia.IngresaSaldosSubyacente");
		Boolean IngresoSaldo= false;
				
		if ((cantidad.compareTo( BigDecimal.ZERO ) > 0  || valorRescate.compareTo( BigDecimal.ZERO ) > 0)) {
			IngresoSaldo= true;
			if("VCDPAC".equalsIgnoreCase(tipoOperacionCartera) && "VCDPAC".equalsIgnoreCase(tipoOperacionEntrada)) {
				IngresoSaldo = false;
			}else if (("ANUCPA".equalsIgnoreCase(tipoOperacionCartera) && "ANUCPA".equalsIgnoreCase(tipoOperacionEntrada)) || ("ANUVPA".equalsIgnoreCase(tipoOperacionCartera) && "ANUVPA".equalsIgnoreCase(tipoOperacionEntrada))) {
				IngresoSaldo = false;
			}else if ("ANTPAC".equalsIgnoreCase(tipoOperacionCartera) && "ANTPAC".equalsIgnoreCase(tipoOperacionEntrada)) {
				IngresoSaldo = false;
			}		
		}
		
		logger.trace("FIN::RetiroCustodia.IngresaSaldosSubyacente");
		return IngresoSaldo;
	}
	
	public static Long CuentaDias (String d1, String d2, String tipo){
		Long CantDias = 0L;
		LocalDate ld = LocalDate.parse(d1, Fecha.YYYYMMDD);
        LocalDate ld2 = LocalDate.parse(d2, Fecha.YYYYMMDD);
       
        int diasDiferenciaCompleto = ld2.getDayOfYear() - ld.getDayOfYear();
        int diasDiferenciaLaboral = 0;
       
        for(int asd = 0; asd< diasDiferenciaCompleto; asd++) {
            ld = ld.plusDays(asd);
            if(ld.getDayOfWeek() != DayOfWeek.SATURDAY && ld.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasDiferenciaLaboral++;
            }
        }
		
        if (tipo.equalsIgnoreCase("L"))
        	CantDias = new Long(diasDiferenciaLaboral);
        else
        	CantDias = new Long(diasDiferenciaCompleto);
		return CantDias;
		
	}
	
}
