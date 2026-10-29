package cl.bcs.facturacion;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.EnviarFacturacionCobroMensual.ComprobanteCobrosDetalle_Salida;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Entrada;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Salida;
import cl.bcs.facturacion.ProcesarGananciaCapital.EntradaProcesarGananciaCapital;

import cl.bcs.facturacion.pojo.Comprobante;
import cl.bcs.facturacion.pojo.ComprobanteCobro;
import cl.bcs.facturacion.pojo.ComprobanteDetalle;
import cl.bcs.facturacion.pojo.ComprobanteFormaDePago;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.facturacion.pojo.MovimientoComprobanteCobro;
import cl.bcs.facturacion.pojo.MovimientoComprobanteDetalle;
import cl.bcs.facturacion.pojo.MovimientoComprobanteFormaDePago;
import cl.bcs.facturacion.pojo.ReferenciaTipoDocumento;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.Persona;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ModalidadPago;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.NotificarEstadoOrdenes_Entrada;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.OrdenModificada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.ConsultarComprobantes.IdComprobantes;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearComprobante implements JavaCall,JavaCallMetadata {
	
	private static final Logger logger = LogManager.getLogger(CrearComprobante.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_PORCENTAJE_RETENCION = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectRetencionAPV-B.sql");
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectConfiguracion.sql");
	private final String SQL_SELECT_PREFERENCIAS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectPreferenciasFacturacion.sql");
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientos.sql");
	private final String SQL_SELECT_MOVIMIENTOS_COBROS_FIJOS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosCobrosFijos.sql");
	private final String SQL_SELECT_MOVIMIENTOS_TESORERIA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosTesoreria.sql");
	private final String SQL_SELECT_COMPROBANTES_LIQUIDADOS_TESORERIA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosLiquidadosTesoreria.sql");
	private final String SQL_SELECT_MOVIMIENTOS_NO_LIQUIDADOS_TESORERIA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosNoLiquidadosTesoreria.sql");
	private final String SQL_SELECT_COMPROBANTES_NO_LIQUIDADOS_TESORERIA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectComprobantesNoLiquidadosTesoreria.sql");
	private final String SQL_UPDATE_ANULACION_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateAnulacionMovimientos.sql");
	private final String SQL_SELECT_MOVIMIENTOS_SPOT = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosSpot.sql");
	private final String SQL_SELECT_ESTADO_CAJA_SPOT = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectEstadoCajaSpot.sql");
	private final String SQL_SELECT_MOVIMIENTOS_SPOT_INTEGRACION = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosSpotIntegracion.sql");	
	private final String SQL_SELECT_PERSONA_TGR = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectPersonaPreferenciaPagoTGR.sql");
	private final String SQL_SELECT_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectGlosaTipoInstrumento.sql");
	
	private final String SQL_SELECT_MOVIMIENTOS_COBROS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosCobros.sql");
	private final String SQL_SELECT_MOVIMIENTOS_FORMAS_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosFormasDePago.sql");
	private final String SQL_BLOQUEA_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_BloqueaMovimientos.sql");
	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectComprobantesExistentes.sql");
	private final String SQL_SELECT_COBRO_IVA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectCobroIva.sql");
	
	private final String SQL_UPDATE_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateMovimientos.sql");
	private final String SQL_INSERT_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_InsertComprobante.sql");
	private final String SQL_INSERT_COMPROBANTE_COBRO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_InsertCobro.sql");
	private final String SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_InsertFormaDePago.sql");
	private final String SQL_INSERT_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_InsertComprobanteDetalle.sql");
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_InsertMovimientoComprobanteDetalle.sql");
	private final String SQL_UPDATE_COMPROBANTE_REFERENCIA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateComprobanteReferencia.sql");
	private final String SQL_SELECT_ORDENES_MODIFICADAS = loadResourceToString("cl/bcs/ordenes/sql/OrdenesModificadas_Select.sql");
	
	private final String SQL_UPDATE_REFERENCIA_TIPO_DOCUMENTO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateMovimientoComprobanteReferencia.sql");
	
	private final String SQL_UPDATE_OPERACIONES_FMM_CONFIRMACION 	= loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateOperacionesFMMConfirmacion.sql");
	private final String SQL_SELECT_MODALIDAD_PAGO 					= loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectModalidadPago.sql");
	
	private final String SQL_SELECT_COBROS_COMISIONES_MENSUALES_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_SelectCobrosComisionesMensualesDetalle.sql");
	private final String SQL_UPDATE_COBROS_COMISIONES_MENSUALES = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateCobrosComisionesMensuales.sql");
	private final String SQL_UPDATE_COBROS_COMISIONES_MENSUALES_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_UpdateCobrosComisionesMensualesDetalle.sql");
	private final String SQL_ANULA_MOVIMIENTO_COMPROBANTE_COBROS_FIJO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_AnularMovimientoComprobanteCobro.sql");

	
	public final List<String> tiposFactura = Arrays.asList("FAC", "FAE", "BOE", "FOE","FOC", "FEE", "FOX","POL","BEE");
	public final List<String> tiposNotaCredito = Arrays.asList("NCE", "NCR", "NCO", "NEE", "NEO");
	public final List<String> tiposNotaCreditoEsp = Arrays.asList("NEO");
	public final List<String> tiposCobroFijo = Arrays.asList("COFI", "DEFI", "GAFI","IVA","GFFA","GFOD");
	public final List<String> tiposNotaDebito = Arrays.asList("NDE");
	public List<String> camposComprobanteDetalle = new ArrayList<>();
	public List<String> camposComprobanteDetalleVentaCorta = new ArrayList<>();
	public List<String> camposComprobanteDetallePrecioPromedioRV = new ArrayList<>();
	
	private static final String NO_APLICA_FUSION= "FAC_NOAPLICAFUSION";
	private static final String FUSION_PRECIO_PROMEDIO= "FAC_FUSIONYAGRUPACIONPORPRECIOPROMEDIO";
	private static final String FUSION_SEPARA_PRECIO= "FAC_FUSIONYSEPARAPORPRECIO";
	private String paramLiquidacionComisionFact = "N";
	private static final String TIPO_PRESTAMO_VENTA_CORTA = "PRTVTC";
	private static final String TIPO_PRESTAMO_VENTA_CORTA_RF = "PRVCRF";
	private static String PERMITE_SALTO_CUSTODIA_VENTA_CORTA_RENTA_FIJA = "S";
	private static final String SI = "S";
	private static final String NO = "N";
	private static final String RENTA_VARIABLE = "RV";
	private static final String INTERMEDIACION_FINANCIERA = "IF";
	private static final String RENTA_FIJA = "RF";
	private static final String SIMULTANEA = "SIM";
	
	public List<String> camposComprobante = new ArrayList<>(); 
	
	List<MovimientoComprobante_Proceso> movimientos = new ArrayList<MovimientoComprobante_Proceso>();
	List<MovimientoComprobante_Proceso> movimientosError = new ArrayList<MovimientoComprobante_Proceso>();
	
	public void llenarLlaves(ConfiguracionFacturacion conf) {
		
		camposComprobante.add("IdPortafolio");
		camposComprobante.add("IdCliente");
		camposComprobante.add("IdBolsa");
		camposComprobante.add("IdTipoComprobante");
		camposComprobante.add("ModPrefFact");
		camposComprobante.add("IdComprobanteReferencia");
		camposComprobante.add("FechaLiquidacion");
		camposComprobante.add("FechaComprobante");
		camposComprobante.add("IdMonedaLiquidacion");
		camposComprobante.add("IdTipoProducto");
		camposComprobante.add("IdTipoOperacion");
		camposComprobante.add("EsVentaCorta");
		camposComprobante.add("CodigoIFRS");
		camposComprobante.add("ActualizarCustodia");
		camposComprobante.add("ActualizarTesoreria");
		camposComprobante.add("ActualizarCuentaInversion");
		camposComprobante.add("EsAPlazo");
		camposComprobante.add("IdConceptoMovimiento");
		camposComprobante.add("CodigoPortafolio");
		
		if(SI.equalsIgnoreCase(paramLiquidacionComisionFact)) {
			camposComprobante.add("IdTipoLiquidacionComisionFactura");
		}
		
		if(SI.equalsIgnoreCase(conf.SeparaPorBoveda)) {
			camposComprobante.add("IdBoveda");
		}
		
		if(SI.equalsIgnoreCase(conf.SeparaPorEjecutivo)) {
			camposComprobante.add("IdEmpleadoEjecutivoPortafolio");
		}
		
		if(SI.equalsIgnoreCase(conf.SeparaPorSucursal)) {
			camposComprobante.add("IdSucursalEmpleadoEjecutivoPortafolio");
		}
		
		camposComprobanteDetalle.add("IdComprobante");
		camposComprobanteDetalle.add("IdInstrumento");
		camposComprobanteDetalle.add("IdBolsa");
		camposComprobanteDetalle.add("Tasa");
		camposComprobanteDetalle.add("Precio");
		camposComprobanteDetalle.add("IdEmpleadoEjecutivoPortafolio");
		camposComprobanteDetalle.add("IdSucursalEmpleadoEjecutivoPortafolio");
		
		camposComprobanteDetalleVentaCorta.add("IdComprobante");
		camposComprobanteDetalleVentaCorta.add("IdCliente");
		camposComprobanteDetalleVentaCorta.add("IdPortafolio");
		camposComprobanteDetalleVentaCorta.add("IdBolsa");
		camposComprobanteDetalleVentaCorta.add("IdInstrumento");
		camposComprobanteDetalleVentaCorta.add("IdCondicionDeLiquidacion");
		camposComprobanteDetalleVentaCorta.add("FechaVencimiento");
		camposComprobanteDetalleVentaCorta.add("TasaOperacion");
		
		camposComprobanteDetallePrecioPromedioRV.add("IdComprobante");
		camposComprobanteDetallePrecioPromedioRV.add("IdCliente");
		camposComprobanteDetallePrecioPromedioRV.add("IdPortafolio");
		camposComprobanteDetallePrecioPromedioRV.add("IdBolsa");
		camposComprobanteDetallePrecioPromedioRV.add("IdInstrumento");
		camposComprobanteDetallePrecioPromedioRV.add("IdCondicionDeLiquidacion");
		camposComprobanteDetallePrecioPromedioRV.add("IdTipoOperacion");
	}
	
	//ENTRADAS
	public static class CrearComprobante_Entrada extends BcsPojo {		
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;   
		public String							p_IdModulo;   
		public String							p_simularOperacion;				// S/N se usa para simular operacion (algunos casos)
		public String							p_UsaReglasNegocio;				// S/N se usa en caso de servicios anidados.
		public String							p_EsIngresoAutomatico = "N";	// S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente
	    

		public String         					p_GeneraComprobanteTesoreria = null;
		
		
		public String							p_Glosa = "";
	    public List<MovimientoComprobante_Entrada>	P_MOVIMIENTOFACTURAR;
		public String							p_ServicioAgrupador = "";
		
	    public CrearComprobante_Entrada (){
	    	P_MOVIMIENTOFACTURAR	= new ArrayList<MovimientoComprobante_Entrada>();
		}
	}
	
	public static class MovimientoComprobante_Entrada extends BcsPojo {
		public Long					IdMovimientoComprobante;
		public String				FechaComprobante;
		public String				IdConcepto;
		public Long 				IdCliente;
		public Long 				IdPortafolio;
	}
	
	//SALIDAS
	public static class ResultadoProceso_Salida extends BcsPojo {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
	}
	
	public static class CrearComprobante_Salida extends BcsPojo {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<Comprobante_Salida>				P_OUT_COMPROBANTE;
		public List<MovimientoCtaInversion_Salida>	P_OUT_CARGOSABONOSCTAINVERSION;
		public List<MovimientoTesoreria_Salida>		P_OUT_INGRESOSEGRESOSTESORERIA;
		public List<MovimientoCustodia_Salida>		P_OUT_INGRESOSRETIROSCUSTODIA;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		
		
		public CrearComprobante_Salida (){
			P_OUT_MENSAJES	= new ArrayList<>();
			P_OUT_COMPROBANTE = new ArrayList<>();
			P_OUT_CARGOSABONOSCTAINVERSION = new ArrayList<>();
			P_OUT_INGRESOSEGRESOSTESORERIA = new ArrayList<>();
			P_OUT_INGRESOSRETIROSCUSTODIA = new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class MovimientoCtaInversion_Salida extends BcsPojo {
		public Long 	IdCargoAbonoMovimiento;
		public String 	IdTipoMovimiento;
		public Long		IdMovimientoComprobante;
		public Long		IdComprobante;
	}
	
	public static class MovimientoTesoreria_Salida extends BcsPojo {
		public Long 	IdIngresoEgreso;
		public String 	IdTipoMovimiento;
		public Long		IdMovimientoComprobante;
		public Long		IdComprobante;
	}
	
	public static class Comprobante_Salida extends BcsPojo {
		public Long	IdComprobante;
		public Long	IdMovimientoComprobante;
	}
	
	public static class MovimientoCustodia_Salida extends BcsPojo {
		public Long 	FolioCustodia;
		public Long		IdMovimientoComprobante;
		public Long		IdComprobante;
	}
	
	//PROCESOS
	public static class InfoMovimientoComprobante_Proceso extends BcsPojo{
		public String Accion;
		public String ActualizarCustodia;
		public String ActualizarTesoreria;
		public String ActualizarCuentaInversion;
		
		public String CodigoIFRS;
		public BigDecimal Cantidad;
		public String CobraPrimaAlInicio;
		public String CodigoBroker;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public BigDecimal CorteMinimo;
		
		public BigDecimal DiasPlazo;
		public String DireccionFacturacion;
		public String DocumentoIdentidad;
		
		public String EsAPlazo;
		public String EsComprobIndividual;
		public String EsVentaCorta;
		public String EsEnRueda;
		public String EsFungible;
		public String EsClienteFinal;
		
		public String FolioTransaccion;
		public java.sql.Timestamp FechaTransaccion;
		public Date FechaLiquidacion;
		public Long FolioComprobanteReferencia;
		public Date FechaComprobanteReferencia;
		public java.sql.Timestamp FechaEmision;
		public Date FechaVencimientoInstrumentoRelacionado;
		public Date FechaEmisionInstrumentoRelacionado;
		public java.sql.Timestamp FechaMovimientoComprobante;
		public Long FolioCartera;
		public java.sql.Timestamp FechaOrden;
		public java.sql.Timestamp FechaVencimiento;
		
		public Long IdComprobante;
		public Long IdCuentaCustodioPortafolio;
		public Long IdMovimientoComprobante;
		public Long IdMovimientoRelacionado;
		public String IdEstadoFacturacion;
		public String IndicadorOrdenDirecta;
		public Long IdTransaccion;
		public String IdTipoReajuste;
		public String IdTipoInstrumentoSubyacente;
		public String IdTipoInstrumentoRelacionado;
		public String IdTipoInstrumento;
		public Long IdSucursalEmpleadoEjecutivoPortafolio;
		public String IdMonedaSubyacente;
		public String IdMonedaMovimientoComprobante;
		public String IdMonedaLiquidacion;
		public String IdMoneda;
		public String IdMonedaSpot;
		public Long IdOperacion;
		public String IdMonedaEmision;
		public Long IdMesa;
		public String IdMonedaNegociacion;
		public Long IdInstrumentoSubyacente;
		public Long IdInstrumentoRelacionado;
		public Long IdInstrumentoPrestamo;
		public Long IdInstrumento;
		public String IdFormaCustodia;
		public Long IdEmpleadoEjecutivoPortafolio;
		public String IdConceptoMovimiento;
		public String IdClasificacionTipoInstrumentoRelacionado;
		public String IdClasificacionTipoInstrumento;
		public Long IdCliente;
		public String IdBolsa;
		public Long IdEmpresa;
		public String IdCondicionDeLiquidacion;
		public Long IdPortafolio;
		public String IdTipoComprobante;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdBoveda;
		public String IdTipoCanalIngreso;
		public Long IdComprobanteReferencia;
		public String IdTipoComprobanteReferencia;
		public String IdMonedaInstrumentoRelacionado;
		public String IdTipoReajusteRelacionado;
		public String IdEmisorRelacionado;
		public String IdEmisorSubyacente;
		public String IdTipoPrestamo;
		public String IdEmisor;
		public BigDecimal Intereses;
		public Long IdOrden;
		public Long IdPersonaBancoLiquidador;
		public Long IdPersonaBancoCustodio;
		public Long IdPersona;
		public Long IdPersonaBroker;
		public String IdTipoCliente;
		public String IdTipoLiquidacionComisionFactura;
		
		public String LiquidaBancoCustodio;
		
		public BigDecimal MontoMovimientoComprobante;
		public BigDecimal MontoLiquidacion;
		public BigDecimal Monto;
		public BigDecimal MontoRaiz;
		public String ModPrefFact;
		public BigDecimal MontoAPlazo;
		public String ModalidadSMV;
		public String MercadoSMV;
		
		public String NombreTipoInstrumentoSubyacente;
		public String NombreTipoInstrumentoRelacionado;
		public String NombreTipoInstrumento;
		public String NombreClasificacionTipoInstrumentoRelacionado;
		public String NombreClasificacionTipoInstrumento;
		public String NemotecnicoSubyacente;
		public String NemotecnicoRelacionado;
		public String NemotecnicoInstrumento;
		public String NemoSVS;
		public String NombreEmpleadoEjecutivoPortafolio;
		public String NombreSucursalEmpleadoEjecutivoPortafolio;
		public String NombreLargoCliente;
		public String NombreTipoCliente;
		
		public BigDecimal Precio;
		public BigDecimal PrecioRaiz;
		public BigDecimal PrecioAPlazo;
		public BigDecimal TasaEmision;
		
		public BigDecimal ReajustesVAC;
		
		public Long TamanoRelacionado;
		public String TipoDocumentoIdentidad;
		public String TipoComprobanteFacturacion;
		
		public BigDecimal Tasa;				//TASA RF IF SIM
		public BigDecimal TasaOperacion;	//PARA VENTA CORTA
		public BigDecimal ValorTipoCambio;
		public BigDecimal ValorRescate;
		public BigDecimal ValorParidad;
		public BigDecimal ValorCaptacionMonedaOrigen;
		
		public String     Glosa;
		public String     CodigoPortafolio;
		
		public String Referencia;
		public String TipoReferencia;
		
		public String Contrapartida;
		public Date FechaVencimientoInstrumento;
		public String EsExento;
		
		public String     DeatalleOperacion;
		
		public InfoMovimientoComprobante_Proceso() {
			this.IdTipoLiquidacionComisionFactura = "";
			this.ModPrefFact = "";
			this.Contrapartida = "N";
			this.EsExento = "N";
		}
	}
	
	public static class InfoMovimientoSpot_Proceso extends BcsPojo {
		public Long IdOperacionSpot;
		public Long IdSolicitudAporteRescate;
		public Long IdSolicitudAporteRescateRelacionado;
		public String IdTipoCanalIngreso;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String IdRegimenTributario;
		public Long IdPersona;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdPersonaEmisor;
		public Long IdCuentaBancariaCorredora;
	}
	
	public static class EstadoCajaSpot extends BcsPojo {
		public Long	 IdMovimientoComprobante;
		public String EstadoCaja;
		
		public EstadoCajaSpot() {
			this.EstadoCaja = "NRE";
		}
	}
	
	public static class InfoMovimientoSpotIntegracion_Proceso extends BcsPojo {
		public Long IdMovimientoComprobante;
		public Long  IdOperacionSpot;
		public String Estado;
	}
	
	public static class MovimientoComprobanteFormaDePago_Proceso extends MovimientoComprobanteFormaDePago{		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobante_Proceso extends BcsPojo {
		public Long IdMovimientoComprobante;
		public String FechaComprobante;
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazo;
		public BigDecimal Monto;

		/* VALIDACIONES */
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
	}
	
	public static class MovimientoComprobanteCobroFijos extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long Cantidad;
	}
	
	public static class PorcentajeRetencionRegimenAPVB extends BcsPojo {
		public String RegimenTributario;
		public BigDecimal PorcentajeRetencion;
	}
	
	public static class MovimientoComprobanteExistente_Proceso extends BcsPojo {
		public String LlavePortafolioClasificacion;
	}
	
	public static class Cobro  extends BcsPojo {
        public String      IdCobro;
        public BigDecimal  ValorCobro;
    }

	public static class ComprobanteDetalle_Proceso extends ComprobanteDetalle {
		public Long IdCliente;
		public Long IdPortafolio;
		public String llaveComprobanteDetalle;
		public String Glosa;
		
		public List<MovimientoComprobanteDetalle_Proceso> movimientoComprobanteDetalles;
		
		public ComprobanteDetalle_Proceso() {
			movimientoComprobanteDetalles = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ComprobanteFormaDePago_Proceso extends ComprobanteFormaDePago {
		public String Accion;
		public String IdTipoMovimientoSpot;
		public String AfectarTesoreria;
		public String AfectarCuentaInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteDetalle_Proceso extends MovimientoComprobanteDetalle {
		public MovimientoComprobante_Proceso movimientoComprobanteProceso;
		
		public String Accion;
		public String ActualizarTesoreria;
		public String ActualizarCustodia;
		public String ActualizarCuentaInversion;
		public String CodigoBroker;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public BigDecimal CorteMinimo;
		public BigDecimal DiasPlazo;
		public String DireccionFacturacion;
		public String DocumentoIdentidad;
		public String EsClienteFinal;
		public String EsComprobIndividual;
		public String EsAPlazo;
		public String EsFungible;
		public java.sql.Timestamp FechaComprobante;
		public Date FechaComprobanteReferencia;
		public java.sql.Date FechaLiquidacion;
		public Timestamp FechaVencimiento;
		public Long FolioCartera; //para vender
		public Long FolioComprobanteReferencia;
		public String Glosa;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCliente;
		public Long IdComprobante;
		public Long IdComprobanteReferencia;
		public String IdCondicionDeLiquidacion;
		public Long IdCuentaCustodioPortafolio;
		public String IdEmisor;
		public String IdEmisorSubyacente;
		public Long IdEmpresa;
		public Long IdOrden;
		public Long IdPersona;
		public Long IdPersonaBancoCustodio;
		public Long IdPersonaBancoLiquidador;
		public Long IdPersonaBroker;
		public Long IdPortafolio;
		public String IdTipoCanalIngreso;
		public String IdTipoCliente;
		public String IdTipoComprobante;
		public String IdTipoComprobanteReferencia;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public BigDecimal Intereses;
		public String LiquidaBancoCustodio;
		public String MercadoSMV;
		public String ModalidadSMV;
		public String NombreEmpleadoEjecutivoPortafolio;
		public String NombreLargoCliente;
		public String NombreSucursalEmpleadoEjecutivoPortafolio;
		public String NombreTipoCliente;
		public BigDecimal ReajustesVAC;
		public String TipoDocumentoIdentidad;
		public String TipoComprobanteFacturacion;
		
		public Long IdOperacion;

		public BigDecimal MontoAPlazo;
		public BigDecimal PrecioAPlazo;
		public String CodigoPortafolio;
		
		public String Contrapartida;
		public Date FechaVencimientoInstrumento;
		public String EsExento;
		
		
		public MovimientoComprobanteDetalle_Proceso() {
			this.EsExento = "N";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteCobro_Proceso extends MovimientoComprobanteCobro {
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CodigoPortafolio extends BcsPojo {
		public String CodigoPortafolio;
	}
	
	public static class MovimientoFusionVC extends BcsPojo {
		public Long IdMovimientoComprobante;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal Precio;
	}

	
	public static class ConfiguracionFacturacion extends BcsPojo {
		public String SeparaPorEjecutivo;
		public String SeparaPorSucursal;
		public String SeparaPorBoveda;
	}
	
	public static class PreferenciaFacturacion extends BcsPojo {
		public Long IdPortafolio;
		public String FusionaComprobanteDetallePorPrecio;
		public String SeparaComprobantePorInstrumento;
		public String SeparaComprobanteDetallePorTransaccion;
		public String ModoFacturacion;
		public String FusionFacturaVentaCorta;
		public String FusionFacturaPrecioPromedioRV;
	}
	
	public static class SelectCondicionDeLiquidacion extends BcsPojo {
		public String	IdCondicionDeLiquidacion;
		public String	Codigo;
		public String	Nombre;
		public Long		PlazoEnDias;
	}
	
	public static class SelectTipoPrestamo extends BcsPojo {
		public Long		IdInstrumentoPrestamo;
		public String	IdTipoPrestamo;
		public String	TipoPrestamoSistemaExterno;
	}
	
	public static class PersonaTGR extends BcsPojo{
		public Long IdPersona;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		
		public PersonaTGR(){
			this.IdPersona = 0L;
			this.IdCuentaBancaria = 0L;
			this.IdBanco = 0L;
			this.IdTipoCuentaBancaria = "";
			this.IdMoneda = "";
			this.NumeroCuentaBancaria = "";
			this.NombreTipoCuentaBancaria = "";
		}
	}
	
	public static class MovimientoTesoreria_Proceso extends BcsPojo{
		public Long IdComprobanteOrigen;
		public Long Id;
		public String NombreTipoMovimiento;
		public String IdEstadoComprobante;
		public BigDecimal Monto;
		public String IdTipoMovimiento;
		public String IdConcepto;
		public String IdMoneda;
		public String IdTipoOperacion;
		public String IdTipoProducto;
	}
	
	public static class ComprobanteLiquidadoTesoreria extends BcsPojo{
		public String IdEstado;
		public BigDecimal Monto;
		public String IdTipoOperacion;
		public String IdMoneda;
		public Long IdComprobanteOrigen;
		public Long IdComprobante;
		public Long IdIngresoEgreso;
	}
	
	public static class MovimientoNoLiquidadoTesoreria extends BcsPojo{
		public Long IdComprobanteOrigen;
		public String IdEstadoComprobante;
		public BigDecimal Monto;
		public Long IdCliente;
		public String NombrePersona;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public String IdMoneda;
		public String Nemotecnico;
		public String IdEstado;
		
	}
	
	public static class ComprobanteNoLiquidadoTesoreria extends BcsPojo{
		public Long IdComprobante;
		public String FechaComprobanteOrigen;
	}
	
	public static class ComprobanteCobro_Proceso extends ComprobanteCobro {
		
	}
	
	public static class Comprobante_Proceso extends Comprobante {
		
		public String llaveComprobante;
		public int advertencias;
		public int autorizaciones;
		public int errores;

		public String Accion;
		public String ActualizarTesoreria;
		public String ActualizarCustodia;
		public String ActualizarCuentaInversion;
		public String EsComprobIndividual;
		public Long IdPersona;
		public String TipoComprobanteFacturacion;
		
		public List<ComprobanteDetalle_Proceso> comprobanteDetalles;
		public List<ComprobanteFormaDePago_Proceso> comprobanteFormasDePago;
		public HashMap<String,ComprobanteFormaDePago_Proceso> hashComprobanteFormasDePago = new HashMap<>(); //para simular el group by
		public List<ComprobanteCobro_Proceso> comprobanteCobros;
		public HashMap<String,ComprobanteCobro_Proceso> hashComprobanteCobros; //para simular el group by
		public List<MovimientoComprobanteDetalle_Proceso> movimientosComprobanteDetalle;
		public String EstadoCaja;
		public Comprobante_Proceso() {
			comprobanteDetalles = new ArrayList<>();
			comprobanteFormasDePago = new ArrayList<>();
			movimientosComprobanteDetalle = new ArrayList<>();
			comprobanteCobros = new ArrayList<>();
			hashComprobanteCobros = new HashMap<>();
			hashComprobanteFormasDePago = new HashMap<>();
			
			advertencias = 0;
			autorizaciones = 0;
			errores = 0;
			this.EstadoCaja = "NRE";
		}
		
		public void agregarComprobanteCobro(ComprobanteCobro_Proceso cobro) {
			
			String llaveCobro = cobro.IdCobro + cobro.IdComprobante + cobro.IdMonedaCobro + cobro.IdMonedaComprobanteCobro + cobro.IdMonedaLiquidacionCobro;
			
			ComprobanteCobro_Proceso cobroExistente = hashComprobanteCobros.get(llaveCobro);
			if (cobroExistente != null) {
				//SUMA MONTOS
				cobroExistente.MontoCobro = cobroExistente.MontoCobro.add(cobro.MontoCobro);				
				cobroExistente.MontoComprobanteCobro = cobroExistente.MontoComprobanteCobro.add(cobro.MontoComprobanteCobro);
				cobroExistente.MontoLiquidacionCobro = cobroExistente.MontoLiquidacionCobro.add(cobro.MontoLiquidacionCobro);
				hashComprobanteCobros.put(llaveCobro, cobroExistente);
			}
			else {
				comprobanteCobros.add(cobro);
				hashComprobanteCobros.put(llaveCobro, cobro);
			}
		}
		
		public void agregarComprobanteFormaDePago(ComprobanteFormaDePago_Proceso fpag) {
			String llaveFormaDePago = fpag.IdComprobante + this.IdTipoComprobante + this.Accion + fpag.IdMedioDePago + fpag.NumeroSerie + fpag.NumeroDocumento + fpag.IdCuentaBancaria
					+ fpag.IdTipoCuentaBancaria + fpag.NombreTipoCuentaBancaria + fpag.NumeroCuentaBancaria + fpag.IdTarjetaBancaria + fpag.IdTipoTarjetaBancaria + fpag.NombreTipoTarjetaBancaria 
					+ fpag.NumeroTarjetaBancaria + fpag.FechaVctoTarjetaBancaria.toString() + fpag.IdModalidadDePago + fpag.IdBanco + fpag.IdMoneda + fpag.FechaDocumentoPago.toString() 
					+ fpag.IdTipoMovimientoSpot + fpag.AfectarTesoreria + fpag.AfectarCuentaInversion;
			
			//TODO Para chile falta agregar IdTipoMovimientoSpot,AfectarTesoreria a la llave
			ComprobanteFormaDePago_Proceso fpagExistente = hashComprobanteFormasDePago.get(llaveFormaDePago);
			if (fpagExistente != null) {
				//SUMA MONTOS
				fpagExistente.MontoLiquidacionFormaDePago = fpagExistente.MontoLiquidacionFormaDePago.add(fpag.MontoLiquidacionFormaDePago);
				hashComprobanteFormasDePago.put(llaveFormaDePago, fpagExistente);
			}
			else {
				comprobanteFormasDePago.add(fpag);
				hashComprobanteFormasDePago.put(llaveFormaDePago, fpag);
			}
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GlosaTipoInstrumento extends BcsPojo {
		public String IdGlosa;
		public String IdTipoInstrumento;
		public String Nombre;
		public Long Prioridad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CrearComprobante_Entrada jin=gson.fromJson(in.toString() ,CrearComprobante_Entrada.class);
		CrearComprobante_Salida out=logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		List<MensajeSalida> l = new ArrayList<>();
		
		//PARCIALIADES AUN NO
		if (out.P_OUT_ERRORESCONTADOR != null && out.P_OUT_ERRORESCONTADOR > 0) {
			for (MensajeSalida msj : out.P_OUT_MENSAJES) {
				if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
					l.add(msj);	
				}
			}
			out.P_OUT_MENSAJES = l;
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
        setMetadata(jin,out);
		return dataout;
	}
	
	@Override
	public void setMetadata(Object in,Object out) {
		CrearComprobante_Salida salida = (CrearComprobante_Salida)out;
		ArrayList<Long> movsProcesados = new ArrayList<>();
		for (Comprobante_Salida mov : salida.P_OUT_COMPROBANTE) {
			movsProcesados.add(mov.IdMovimientoComprobante);
		}
		metadata[0] = movsProcesados.size()+"";
	}
	
	
	public String construirGlosaLarga(MovimientoComprobanteDetalle_Proceso movdet ,ComprobanteDetalle_Proceso compros, List<GlosaTipoInstrumento> listaGlosas ) {
		
		String glosaLarga = "";
		List <String> tiposNotasDeCredito = new ArrayList<String> (Arrays.asList("NCR", "NCE","NEE", "NEO"));
		
		if(movdet.IdTipoOperacion.equals("COMOTC") || movdet.IdTipoOperacion.equals("VENOTC")|| movdet.IdTipoOperacion.equals("ANCOTC") ||  movdet.IdTipoOperacion.equals("ANVOTC")) {
			glosaLarga = movdet.Glosa;
			return glosaLarga; 
		};
		
		List<String> clavesFiltradas = listaGlosas.stream()
			    .filter(g -> movdet.IdTipoInstrumento.equals(g.IdTipoInstrumento))
			    .sorted(Comparator.comparingLong(g -> g.Prioridad))
			    .map(g -> g.Nombre)
			    .collect(Collectors.toList());
		
		if(clavesFiltradas.size() == 0) {
			glosaLarga = comparaValor(movdet.Glosa,compros.Glosa);
			return glosaLarga; 
		}
		
		for(String clave : clavesFiltradas) {
			if(clave.equals("TEXTO LIBRE")) {
				glosaLarga += comparaValor(movdet.Glosa,compros.Glosa) ;
			
			}
			
			if(clave.equals("FECHA VENCIMIENTO")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				glosaLarga += sdf.format(movdet.FechaVencimiento);
			}
			
			if(clave.equals("MERCADO")) {
				if(movdet.IdClasificacionTipoInstrumento.equals("RF") || movdet.IdClasificacionTipoInstrumento.equals("IF") ) {
					glosaLarga += movdet.NombreClasificacionTipoInstrumento;					
				}else {
					glosaLarga += movdet.NombreTipoInstrumento;					
				}
			}
			
			if(clave.equals("NEMO CORREDORA")) {
				glosaLarga += movdet.IdBolsa;
			}
			
			if(clave.equals("NEMOTECNICO")) {
				if(movdet.IdTipoInstrumento.equals("SPT")) {
					glosaLarga +=movdet.IdMonedaSpot;
				}else {
					glosaLarga += movdet.NemotecnicoInstrumento;					
				}
			}
			if(clave.equals("TASA")) {
				glosaLarga += movdet.Tasa != null ? movdet.Tasa.setScale(4, RoundingMode.HALF_UP) + "%" : "";

			}
			if(clave.equals("COMPRA/VENTA")) {
				if(tiposNotasDeCredito.contains(movdet.TipoComprobanteFacturacion)) {
					glosaLarga += "ANULACION ";
					if("VENTA".equalsIgnoreCase(movdet.Accion)) {
						glosaLarga += "COMPRA";
					}else {
						glosaLarga += "VENTA";
					}
				}else {
					glosaLarga += movdet.Accion;					
				}
			}
			
			if(clave.equals("APB")) {
				if(movdet.IdTipoInstrumento.equals("ACN") && movdet.IdInstrumento > 0) {
					glosaLarga += "APB";
					
				}
			}
			
			if(clavesFiltradas.size() > 1 && clavesFiltradas.indexOf(clave) < clavesFiltradas.size()-1 ) {
				glosaLarga += " ";
			}
			
			
		}
		
		return glosaLarga;
	}
	
	@Trace
	public CrearComprobante_Salida logica(Connection conn, CrearComprobante_Entrada in) throws Exception {
		logger.info("Inicio CrearComprobante");
		long inicio = System.currentTimeMillis();
		CrearComprobante_Salida out = new CrearComprobante_Salida();
		SQL _sql = new SQL();
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String genCbmteTeso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.GeneraComprobanteTesoreria");
		String paisSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		String genCbmteTesoSPT = "N";
		try { genCbmteTesoSPT = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "MESA", "GEN_CMBTE_TES_EMITIDO"); }catch (Exception er){};
		Long cantidadMaximaDetalles = ConfiguracionSistema.getParametroConfiguracionLong(conn, in.p_IdEmpresa, "FACTURACION", "CantidadMaximaDetalleComprobante");
		paramLiquidacionComisionFact = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PRODUCTO", "PRO.CobroComisionesMensuales");
		paramLiquidacionComisionFact = "ACTIVO".equals(paramLiquidacionComisionFact) ? "S" : "N";
		PERMITE_SALTO_CUSTODIA_VENTA_CORTA_RENTA_FIJA = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"FACTURACION","PermiteSaltoCustodiaVentaCortaRentaFija" );
		String cobroMXsobrePagoMX = obtieneParametroCobroMXsobrePagoMX(conn, in.p_IdEmpresa);
		String productosSinCobroMXsobrePagoMX = obtieneParametroProductosSinCobroMXsobrePagoMX(conn, in.p_IdEmpresa);
		
		List<String> productosSinCobroMX = new ArrayList<>();
		try {
			String[] listado = productosSinCobroMXsobrePagoMX.split(",");
			productosSinCobroMX = Arrays.asList(listado);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String idMonedaLocal = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		if (in.P_MOVIMIENTOFACTURAR.size() == 0) {
			String codigo = "NO_VIENEN_MOVIMIENTOS";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		HashMap<String,Object> paramsRegimen = new HashMap<String,Object>();
		List<String> ListaRegimenesRetencion = new ArrayList<>();
		ListaRegimenesRetencion.add("APV-B");
		ListaRegimenesRetencion.add("APV-AN");
		paramsRegimen.put("RegimenTributario", ListaRegimenesRetencion);
		
		List<PorcentajeRetencionRegimenAPVB> porcentajeRegimen = _sql.query(conn, SQL_SELECT_PORCENTAJE_RETENCION, paramsRegimen, PorcentajeRetencionRegimenAPVB.class);
		HashMap<String,Object> hashprocentajeRetencionAPV = new HashMap<String,Object>();
		
		List<ModalidadPago> listaModalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_PAGO, new HashMap<String,Object>(), ModalidadPago.class);
 		Map<String,ModalidadPago> hashModalidadPago = listaModalidadPago.parallelStream().collect(Collectors.toMap(modalidad->modalidad.IdMedioDePago, modalidad->modalidad));
 		
 		List<GlosaTipoInstrumento> listaGlosasTipoInstrumento = _sql.query(conn, SQL_SELECT_GLOSA_TIPO_INSTRUMENTO,  new HashMap<String,Object>(), GlosaTipoInstrumento.class);
				
		if (porcentajeRegimen.size() > 0) {
			hashprocentajeRetencionAPV=Utils.List2Hash(porcentajeRegimen,"RegimenTributario");
		}
		
		//LECTURA DE INFORMACION
		List<MovimientoComprobanteCobro_Proceso> listMovimientosCobros = null;
		List<MovimientoComprobanteCobroFijos> listMovimientosCobrosFijos = null;
		List<MovimientoComprobanteFormaDePago_Proceso> listMovimientosFormasDePago = null;
		HashMap<Object, Object> hashInfoMovimientos = null;
		List<InfoMovimientoComprobante_Proceso> movimientosWebSocket = new ArrayList<CrearComprobante.InfoMovimientoComprobante_Proceso>();			

		Map<Long,InfoMovimientoSpot_Proceso> hashInfoMovimientosSpot = new HashMap<Long,InfoMovimientoSpot_Proceso>();
		String documentoIdentidadTGR = "";
		PersonaTGR personaTGR = new PersonaTGR();
		Persona personaSII = obtenerPersonaSII(conn, in.p_IdEmpresa);
		List<Long> secuenciasComprobanteFormaDePagoAPVB = new ArrayList<Long>();
		HashMap<Long, Object> hashEstadoCajaSpot = new HashMap<>();
		
		//HashMap<Long,MovimientoComprobante_Proceso> hashMovimientos_IdMovimiento = null;
		{
			List<InfoMovimientoComprobante_Proceso> _movimientos = null;			
						
			ArrayList<Long> idsMovimientoComprobante = new ArrayList<Long>();
			for (MovimientoComprobante_Entrada mov : in.P_MOVIMIENTOFACTURAR) {
				if (!idsMovimientoComprobante.contains(mov.IdMovimientoComprobante)) {
					idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
				}
			}
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("idsMovimientoComprobante", idsMovimientoComprobante);
			
			try {
				_sql.query(conn, SQL_BLOQUEA_MOVIMIENTOS, params, MovimientoComprobante.class);
			} catch (SQLException sqle) {
				if (sqle.getErrorCode() == 131) {
					// error especifico porque el movimiento que se esta tratando de obtener ya esta
					// siendo procesado.
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR,
							"UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "UI_TRANSACCION_BLOQUEADA_EN_PROCESO"));
					return out;
				} else {
					throw sqle;
				}
			}
			
			_movimientos = _sql.query(conn, SQL_SELECT_MOVIMIENTOS, params, InfoMovimientoComprobante_Proceso.class);
			
			for(InfoMovimientoComprobante_Proceso _movimiento : _movimientos) {
				_movimiento.Monto =  MonedaFormato.formatear(conn, _movimiento.Monto, MonedaFormato.MONTO, _movimiento.IdMoneda).valor;
				_movimiento.MontoAPlazo =  MonedaFormato.formatear(conn, _movimiento.MontoAPlazo, MonedaFormato.MONTO, _movimiento.IdMoneda).valor;
				_movimiento.MontoLiquidacion =  MonedaFormato.formatear(conn, _movimiento.MontoLiquidacion, MonedaFormato.MONTO, _movimiento.IdMonedaLiquidacion).valor;
				_movimiento.MontoMovimientoComprobante =  MonedaFormato.formatear(conn, _movimiento.MontoMovimientoComprobante, MonedaFormato.MONTO, _movimiento.IdMonedaMovimientoComprobante).valor;
			}
			
			movimientosWebSocket =_movimientos;
			hashInfoMovimientos = Utils.List2Hash(_movimientos, "IdMovimientoComprobante");
					
			//SPOT
			{
				List<Long> _idsSpot = _movimientos.parallelStream().filter(movimiento-> "MEME".equals(movimiento.IdTipoProducto)).map(movimiento->movimiento.IdTransaccion).collect(Collectors.toList());
				
				params.clear();
				params.put("IdOperacionSpot", _idsSpot);
				List<InfoMovimientoSpot_Proceso> _movimientosSpot = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_SPOT, params, InfoMovimientoSpot_Proceso.class);
				hashEstadoCajaSpot = Utils.List2Hash(_sql.query(conn, SQL_SELECT_ESTADO_CAJA_SPOT, params, EstadoCajaSpot.class), "IdMovimientoComprobante");
				
				hashInfoMovimientosSpot = _movimientosSpot.parallelStream().collect(Collectors.toMap(movimiento->movimiento.IdOperacionSpot, movimiento->movimiento));
				
				if (_movimientosSpot.size() > 0) {
					_sql.batch(conn, SQL_UPDATE_OPERACIONES_FMM_CONFIRMACION, _movimientosSpot, InfoMovimientoSpot_Proceso.class);
				}
				
				Long cantidadBonificacion = _movimientosSpot.parallelStream().filter(movimiento-> "BF".equals(movimiento.IdRegimenTributario)).count();
				
				if(cantidadBonificacion > 0) {
					try {
						documentoIdentidadTGR = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "APV.DocumentoIdentidadPersonaTGR");
					}catch(Exception e) {
						logger.debug("ADVERTENCIA::Parametro APV.DocumentoIdentidadPersonaTGR no existe.");
					}
					params.clear();
					params.put("DocumentoIdentidad", documentoIdentidadTGR);
					List<PersonaTGR> personasTGR = _sql.query(conn, SQL_SELECT_PERSONA_TGR, params, PersonaTGR.class);
					if(personasTGR.size() > 0) {
						personaTGR = personasTGR.get(0);
					}
				}
				
				Long cantidadAPVB = _movimientosSpot.parallelStream().filter(movimiento->"APV-B".equals(movimiento.IdRegimenTributario) || ("APV-AN".equals(movimiento.IdRegimenTributario))).count();
				if(cantidadAPVB > 0)
					secuenciasComprobanteFormaDePagoAPVB = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::ComprobanteFormaDePago", cantidadAPVB);
			}
			
			
			//SPOT INTEGRACION
			params.clear();			
			params.put("idsMovimientoComprobante", idsMovimientoComprobante);
			List<InfoMovimientoSpotIntegracion_Proceso> _movimientosSpotIntegracion = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_SPOT_INTEGRACION, params, InfoMovimientoSpotIntegracion_Proceso.class);			

			if (_movimientosSpotIntegracion.size() >0) {				
				
				for (InfoMovimientoSpotIntegracion_Proceso mov : _movimientosSpotIntegracion) {					
					MovimientoComprobante_Proceso movto = new MovimientoComprobante_Proceso();					
					movto.IdMovimientoComprobante = mov.IdOperacionSpot;
					
					if (!mov.Estado.equalsIgnoreCase("FAC")){
						ProcesaRequisito(conn,in.p_IdEmpresa,mov.Estado.equalsIgnoreCase("ANU"),"OPERACION_SPOT_ANULADA",MensajeSalida.MENSAJE_ERROR,movto,null,out.P_OUT_MENSAJES);
						ProcesaRequisito(conn,in.p_IdEmpresa,!mov.Estado.equalsIgnoreCase("FAC") && !mov.Estado.equalsIgnoreCase("ANU"),"DEBE_FACTURAR_OPERACION_SPOT",MensajeSalida.MENSAJE_ERROR,movto,null,out.P_OUT_MENSAJES);										
						out.P_OUT_ERRORESCONTADOR++;	
						return out;						
					}
				}			
			}			
			
			for (MovimientoComprobante_Entrada mov : in.P_MOVIMIENTOFACTURAR) {
				
				MovimientoComprobante_Proceso movto = new MovimientoComprobante_Proceso();
				
				InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
				
				if (info == null) {
					ProcesaRequisito(conn,in.p_IdEmpresa,info == null,"MOVIMIENTO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,movto,null,out.P_OUT_MENSAJES);
					continue;
				}
				
				movto.IdMovimientoComprobante = mov.IdMovimientoComprobante;
				movto.FechaComprobante = mov.FechaComprobante;
				movto.Monto = info.Monto != null ? info.Monto: BigDecimal.ZERO;
				
				ProcesaRequisito(conn,in.p_IdEmpresa,info.IdEstadoFacturacion.equalsIgnoreCase("BLO"),"MOVIMIENTO_COMPROBANTE_BLQ",MensajeSalida.MENSAJE_ERROR,movto,null,out.P_OUT_MENSAJES);
				ProcesaRequisito(conn,in.p_IdEmpresa,info.IdEstadoFacturacion.equalsIgnoreCase("FAC"),"MOVIMIENTO_COMPROBANTE_FAC",MensajeSalida.MENSAJE_ERROR,movto,null,out.P_OUT_MENSAJES);
				ProcesaRequisito(conn,in.p_IdEmpresa,info.IdEstadoFacturacion.equalsIgnoreCase("ANU"),"MOVIMIENTO_COMPROBANTE_ANU",MensajeSalida.MENSAJE_ERROR,movto,null,out.P_OUT_MENSAJES);
				
				//TODO VALIDACION PRESTAMOS
				//TODO VALIDACION SECUENCIAS
				
				movimientos.add(movto);		
			}
		}
		
		//LECTURA DE CONFIGURACION
		{
			ConfiguracionFacturacion configuracionFacturacion = null;
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdEmpresa",in.p_IdEmpresa);
			List<ConfiguracionFacturacion> conf = _sql.query(conn, SQL_SELECT_CONFIGURACION, params, ConfiguracionFacturacion.class);
			if (conf.size() == 0) {
				throw new Exception("Error al obtener configuracion de facturacion");
			}
			configuracionFacturacion = conf.get(0);
			
			if ("PE".equalsIgnoreCase(localizacionSistema)) {
				configuracionFacturacion.SeparaPorBoveda = "S";
			}
			else {
				configuracionFacturacion.SeparaPorBoveda = "N";
			}
			llenarLlaves(configuracionFacturacion );		
		}
		
		limpiaMovimientos(movimientos,movimientosError,out.P_OUT_RESULTADOPROCESO);
		if (movimientos.size() == 0) {
			return out;
		}
		
		{
			ArrayList<Long> idsMovimientoComprobante = new ArrayList<Long>();
			for (MovimientoComprobante_Proceso mov : movimientos) {
				
				InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
				
				if (info.IdMovimientoRelacionado != null && !idsMovimientoComprobante.contains(info.IdMovimientoRelacionado) && !info.IdMovimientoRelacionado.equals(0L) && ("RE".equalsIgnoreCase(info.IdClasificacionTipoInstrumento))) {
					idsMovimientoComprobante.add(info.IdMovimientoRelacionado);
				}
			}
			
			if (idsMovimientoComprobante.size() > 0) {
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("idsMovimientoComprobante", idsMovimientoComprobante);
				List<InfoMovimientoComprobante_Proceso> _movimientos = _sql.query(conn, SQL_SELECT_MOVIMIENTOS, params, InfoMovimientoComprobante_Proceso.class);
				HashMap<Long, Object> hashInfoMovimientosRel = Utils.List2Hash(_movimientos, "IdMovimientoComprobante");
				List<MovimientoComprobante_Proceso> nuevosMovimientos = new ArrayList<>();
				
				for (MovimientoComprobante_Proceso mov : movimientos) {
					
					InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
					InfoMovimientoComprobante_Proceso infoRel = (InfoMovimientoComprobante_Proceso) hashInfoMovimientosRel.get(info.IdMovimientoRelacionado);
					
					if (infoRel != null) {
					
						MovimientoComprobante_Proceso movto = new MovimientoComprobante_Proceso();
						movto.IdMovimientoComprobante = info.IdMovimientoRelacionado;
						movto.FechaComprobante = mov.FechaComprobante;
						if (!hashInfoMovimientos.containsKey(infoRel.IdMovimientoComprobante)) {
							hashInfoMovimientos.put(infoRel.IdMovimientoComprobante,infoRel);
							nuevosMovimientos.add(movto);
						}
					}
				}
				
				movimientos.addAll(nuevosMovimientos);
			}
			
		}
		
        movimientos.sort((movA, movB) -> movA.IdMovimientoComprobante.compareTo(movB.IdMovimientoComprobante));
        
		if (movimientos.size() == 0) {
			return out;
		}
		
		//---------------------------------------------------------------------------------------------------------
		//PRIMA VENTA CORTA
		CrearMovimientoComprobante_Entrada inCrearMovimientoComprobante = new CrearMovimientoComprobante_Entrada();
		for (MovimientoComprobante_Proceso mov : movimientos) {
			
			InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
			if (SI.equalsIgnoreCase(info.EsVentaCorta) && info.CobraPrimaAlInicio!= null && SI.equalsIgnoreCase(info.CobraPrimaAlInicio)) {
				cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada arg0 = new cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada();
				arg0.NumeroMvtoCmbte 				= info.IdMovimientoComprobante;
				arg0.IdEmpresa 						= in.p_IdEmpresa;
				arg0.IdConcepto 					= "COBPRI";
				arg0.SecuenciaAgrupacion 			= "";
				arg0.IdCliente 						= info.IdCliente;
				arg0.IdPortafolio 					= info.IdPortafolio;
				arg0.ModPrefFact 					= info.ModPrefFact;
				arg0.IdTipoLiquidacionComisionFactura = info.IdTipoLiquidacionComisionFactura;
				arg0.IdInstrumento 					= info.IdInstrumentoPrestamo;
				arg0.IdTipoProducto 				= "CUST";
				arg0.IdTipoConvenio 				= "SCV";
				arg0.EsComprobIndividual 			= "N";
				arg0.EsCuentaPropia 				= "N";
				arg0.EsEnRueda 						= "N";
				arg0.EsRuteo 						= "N";
				arg0.IdTipoReajuste 				= "CLP";
				arg0.EsAPlazo 						= "N";
				arg0.EsFungible 					= "N";
				arg0.ActualizarCustodiaRelacionado 	= "N";
				arg0.IdMoneda 						= info.IdMoneda;
				arg0.IdTipoComprobante 				= "FEE";
				arg0.EsVentaCorta 					= "N";
				arg0.IdTipoOperacion 				= "PRESTA";
				arg0.IdMovimientoRelacionado 		= mov.IdMovimientoComprobante;
				arg0.IdBoveda 						= info.IdBoveda;
				arg0.IdCorredoraContraparte 		= 0L;
				arg0.IdCondicionLiquidacion 		= info.IdCondicionDeLiquidacion;
				arg0.FechaLiquidacion 				= Fecha.toStringDate(info.FechaLiquidacion);
				arg0.IdBolsa 						= info.IdBolsa;
				arg0.FechaOrden 					= Fecha.toStringDatetime(info.FechaOrden);
				arg0.IdFormaCustodia 				= info.IdFormaCustodia;
				arg0.FechaEmision 					= Fecha.toStringDatetime(info.FechaEmision);
				arg0.IdTipoCanalIngreso 			= info.IdTipoCanalIngreso;
				arg0.IdOrden 						= info.IdOrden;
				arg0.IdTransaccion 					= info.IdTransaccion;
				arg0.FolioTransaccion 				= info.FolioTransaccion;
				arg0.FechaVencimiento 				= Fecha.toStringDate(info.FechaVencimiento);
				arg0.Cantidad 						= BigDecimal.ZERO;
				arg0.Precio 						= BigDecimal.ZERO;
				arg0.FolioCartera 					= 0L;
				
				//Calcula monto de la prima para Venta Corta (debe usar Precio ingresado al crear la transaccion)				
				BigDecimal montoPrima = calcularPrimaVC(conn,info.TasaOperacion,info.DiasPlazo,info.Cantidad,info.Precio,info.IdMoneda,MonedaFormato.MONTO);				
				arg0.PrecioAPlazo 				= montoPrima;
				arg0.Tasa 						= info.TasaOperacion;
				arg0.TasaOperacion 				= info.TasaOperacion;
				arg0.FechaMovimientoComprobante = fechaSistema;
				arg0.Monto 						= montoPrima;
				arg0.MontoMovimientoComprobante = montoPrima;
				arg0.MontoLiquidacion 			= montoPrima;
				arg0.DiasPlazo 					= info.DiasPlazo;
				arg0.IdTipoCanalIngreso 		= info.IdTipoCanalIngreso;
				arg0.ActualizarCuentaInversion 	= "S";
				arg0.ActualizarCustodia 		= "N";
				arg0.ActualizarTesoreria 		= "S";
				arg0.MontoAPlazo 				= info.MontoAPlazo;
				arg0.IdUsuario 					= in.p_IdUsuario;
				arg0.EsPrecioPrima 				= "S";
				arg0.IdMonedaLiquidacion 		= info.IdMonedaLiquidacion;
				arg0.ValorRescate 				= info.ValorRescate;
				mov.PrecioAPlazo 				= arg0.PrecioAPlazo;
				mov.MontoAPlazo 				= arg0.MontoAPlazo;
				arg0.Glosa 						= "Creacion automatica por mvto. venta corta " + info.IdClasificacionTipoInstrumento + 
													", Instrumento: " + info.NemotecnicoInstrumento +
													", Cantidad: " + info.Cantidad +
													", Tasa Prima: " + info.TasaOperacion +
													", Precio: " + info.Precio +
													", Plazo (dias): " + info.DiasPlazo;
				
				inCrearMovimientoComprobante.p_Movimientos.add(arg0 );
			}
		}
		
		if (inCrearMovimientoComprobante.p_Movimientos.size() > 0) {
			inCrearMovimientoComprobante.p_EsIngresoAutomatico = "S";
			inCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
			inCrearMovimientoComprobante.p_IdModulo = "FAC";
			inCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
			inCrearMovimientoComprobante.p_simularOperacion = in.p_simularOperacion;
			inCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
			CrearMovimientoComprobante c = new CrearMovimientoComprobante();
			CrearMovimientoComprobante_Salida outCrearMovimientoComprobante = c.logica(conn, inCrearMovimientoComprobante);
			logger.debug("Salida Mov Prima VC " + outCrearMovimientoComprobante.toString());
			out.P_OUT_MENSAJES.addAll(outCrearMovimientoComprobante.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(outCrearMovimientoComprobante.P_OUT_MENSAJES) > 0) {
				return out;
			}
		}
		
		{
			ArrayList<Long> idsMovimientoComprobante = new ArrayList<Long>();
			for (MovimientoComprobante_Proceso mov : movimientos) {
				if (!idsMovimientoComprobante.contains(mov.IdMovimientoComprobante)) {
					idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
				}
			}
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("idsMovimientoComprobante", idsMovimientoComprobante);
			
			listMovimientosCobros = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_COBROS, params, MovimientoComprobanteCobro_Proceso.class);
			listMovimientosFormasDePago = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_FORMAS_DE_PAGO, params, MovimientoComprobanteFormaDePago_Proceso.class);
		
			ArrayList<Long> idsCliente = new ArrayList<Long>();
			ArrayList<Long> idsPortafolio = new ArrayList<Long>();
			ArrayList<String> idsFechaComprobante = new ArrayList<String>();
			HashMap<String,Object> paramsCobrosFijos = new HashMap<String,Object>();
			
			for (MovimientoComprobante_Entrada mov : in.P_MOVIMIENTOFACTURAR) {
				if (!idsCliente.contains(mov.IdCliente)) {	
					if (!idsPortafolio.contains(mov.IdPortafolio)) {
						idsCliente.add(mov.IdCliente );
						idsPortafolio.add(mov.IdPortafolio);
						idsFechaComprobante.add(mov.FechaComprobante);						
					}					
				}
			}
			
			paramsCobrosFijos.put("idsCliente", idsCliente);
			paramsCobrosFijos.put("idsPortafolio", idsPortafolio);
			paramsCobrosFijos.put("idsFechaComprobante", idsFechaComprobante);
			listMovimientosCobrosFijos = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_COBROS_FIJOS, paramsCobrosFijos, MovimientoComprobanteCobroFijos.class);
		}
		
		
		for(MovimientoComprobanteCobro_Proceso cobro : listMovimientosCobros) {
			cobro.Monto = MonedaFormato.formatear(conn, cobro.Monto, MonedaFormato.MONTO, cobro.IdMonedaCobro).valor;
			cobro.MontoCobro = MonedaFormato.formatear(conn, cobro.MontoCobro, MonedaFormato.MONTO, cobro.IdMonedaCobro).valor;
			cobro.MontoLiquidacion = MonedaFormato.formatear(conn, cobro.MontoLiquidacion, MonedaFormato.MONTO, cobro.IdMonedaLiquidacionCobro).valor;
			cobro.MontoLiquidacionCobro =  MonedaFormato.formatear(conn, cobro.MontoLiquidacionCobro, MonedaFormato.MONTO, cobro.IdMonedaLiquidacionCobro).valor;
			cobro.MontoMovimientoComprobante =  MonedaFormato.formatear(conn, cobro.MontoMovimientoComprobante, MonedaFormato.MONTO, cobro.IdMonedaMovimientoComprobanteCobro).valor;
			cobro.MontoMovimientoComprobanteCobro =  MonedaFormato.formatear(conn, cobro.MontoMovimientoComprobanteCobro, MonedaFormato.MONTO, cobro.IdMonedaMovimientoComprobanteCobro).valor;
		}
		
		for(MovimientoComprobanteFormaDePago_Proceso formaDePago : listMovimientosFormasDePago) {
			formaDePago.MontoLiquidacionFormaDePago = MonedaFormato.formatear(conn, formaDePago.MontoLiquidacionFormaDePago, MonedaFormato.MONTO, formaDePago.IdMoneda).valor;
		}
		
		//TOTALIZADORES DE COBROS
		//LECTURA DE CONFIGURACION
		HashMap<Long, Object> hashPreferenciasFacturacion = null;
		HashMap<Long,String> hashPortafolioDocumentoEntidad = new HashMap<Long,String>();
		{
			String documentoEntidad;
			Long tipoPrestamoVentaCortaCantidad = 0L;
			HashMap<String,Object> params = new HashMap<>();
			List<Long> idsPortafolio = new ArrayList<>();
			params.put("idsPortafolio",idsPortafolio);
			for (MovimientoComprobante_Proceso mov : movimientos){
				
				InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
				
				if (!idsPortafolio.contains(info.IdPortafolio)) {
					idsPortafolio.add(info.IdPortafolio);
				}
				if (info.IdTipoPrestamo != null && (TIPO_PRESTAMO_VENTA_CORTA.equals(info.IdTipoPrestamo) || TIPO_PRESTAMO_VENTA_CORTA_RF.equalsIgnoreCase(info.IdTipoPrestamo) && SI.equalsIgnoreCase(info.EsVentaCorta))) {
					tipoPrestamoVentaCortaCantidad++;
				}
				documentoEntidad = hashPortafolioDocumentoEntidad.get(info.IdPortafolio);				
				if (documentoEntidad == null) {					
					hashPortafolioDocumentoEntidad.put(info.IdPortafolio,info.DocumentoIdentidad);
				}
			}
			
			if (idsPortafolio.size() > 0) {
				List<PreferenciaFacturacion> preferencias =_sql.query(conn, SQL_SELECT_PREFERENCIAS, params, PreferenciaFacturacion.class);
				hashPreferenciasFacturacion = Utils.List2Hash(preferencias, "IdPortafolio");
			}

			for (MovimientoComprobante_Entrada mov : in.P_MOVIMIENTOFACTURAR) {
				PreferenciaFacturacion pref = (PreferenciaFacturacion) hashPreferenciasFacturacion.get(mov.IdPortafolio);
				InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
				if (pref != null && (tipoPrestamoVentaCortaCantidad > 0 && ("".equals(pref.FusionFacturaVentaCorta) || pref.FusionFacturaVentaCorta.equals(null)) && SI.equalsIgnoreCase(info.EsVentaCorta))) {
					
					documentoEntidad = hashPortafolioDocumentoEntidad.get(mov.IdPortafolio);
					String codigo = "UI_VENTA_CORTA_PREFERENCIAS_NEGOCIO_FFPVTC";
					String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("DocumentoEntidad", documentoEntidad).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante, MensajeSalida.MENSAJE_ERROR, codigo, desc));
					out.P_OUT_ERRORESCONTADOR++;
				}
			}
			
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0 && out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}
		}
		
		List<Long> secuenciasComprobante = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::Comprobante", (long) movimientos.size());
		List<Long> secuenciasComprobanteDetalle = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::ComprobanteDetalle", (long) movimientos.size());
		List<Long> secuenciasMovimientoComprobanteDetalle = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle", (long) movimientos.size());
		List<Long> secuenciasComprobanteCobro = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro", (long) listMovimientosCobros.size());
		List<Long> secuenciasComprobanteFormaDePago = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::ComprobanteFormaDePago", (long) listMovimientosFormasDePago.size());
		
		HashMap<String,Comprobante_Proceso> hashComprobanteLlave = new HashMap<>();
		HashMap<String,Comprobante_Proceso> hashComprobanteLlaveErrores = new HashMap<>();
		HashMap<String,ComprobanteDetalle_Proceso> hashComprobanteDetalleLlave = new HashMap<>();
		
		//---------------------------------------------------------------------------------------------------------
		//MOVIMIENTO COMPROBANTE DETALLE
		//CALCULO DE COMPROBANTES
		Integer indexSecuenciaComprobante = 0;
		for (MovimientoComprobante_Proceso mov : movimientos){
			
			MovimientoComprobanteDetalle_Proceso movdet = new MovimientoComprobanteDetalle_Proceso();
			
			InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
			
			movdet.ActualizarCuentaInversion = info.ActualizarCuentaInversion;
			movdet.ActualizarCustodia = info.ActualizarCustodia;
			movdet.ActualizarTesoreria = info.ActualizarTesoreria;
			movdet.Accion = info.Accion;
			movdet.Cantidad = info.Cantidad;
			movdet.EsFungible = info.EsFungible;
			movdet.CodigoBancoLiquidador = info.CodigoBancoLiquidador;
			movdet.CodigoBancoCustodio = info.CodigoBancoCustodio;
			movdet.CodigoBroker = info.CodigoBroker;movdet.DireccionFacturacion = info.DireccionFacturacion;
			movdet.CodigoIFRS = info.CodigoIFRS;
			movdet.CorteMinimo = info.CorteMinimo;
			movdet.DiasPlazo = info.DiasPlazo;
			movdet.DocumentoIdentidad = info.DocumentoIdentidad;
			movdet.EsAPlazo = info.EsAPlazo;
			movdet.EsEnRueda = info.EsEnRueda;
			movdet.EsClienteFinal = info.EsClienteFinal;
			movdet.EsVentaCorta = info.EsVentaCorta;
			movdet.EsComprobIndividual = info.EsComprobIndividual;
			movdet.FechaComprobanteReferencia = info.FechaComprobanteReferencia;
			movdet.FechaLiquidacion = info.FechaLiquidacion;
			if ("PE".equalsIgnoreCase(localizacionSistema)){
				movdet.FechaComprobante = Fecha.toDateTime(mov.FechaComprobante.length() == 10 ? mov.FechaComprobante + " 00:00:00" : mov.FechaComprobante);
			}
			else {
				movdet.FechaComprobante = Fecha.toDateTime(fechaSistema + " 00:00:00");
			}
			movdet.FechaVencimiento = info.FechaVencimiento;
			movdet.FolioCartera = info.FolioCartera;
			movdet.FolioComprobanteReferencia = info.FolioComprobanteReferencia;
			movdet.FolioTransaccion = info.FolioTransaccion;
			movdet.FechaTransaccion = info.FechaTransaccion;
			movdet.IdBolsa = info.IdBolsa;
			movdet.IdBoveda = info.IdBoveda;
			movdet.IdCarga = 0L;
			movdet.IdClasificacionTipoInstrumento = info.IdClasificacionTipoInstrumento;
			movdet.IdClasificacionTipoInstrumentoRelacionado = info.IdClasificacionTipoInstrumentoRelacionado;
			movdet.IdCliente = info.IdCliente;
			movdet.IdComprobanteReferencia = info.IdComprobanteReferencia;
			movdet.IdConceptoMovimiento = info.IdConceptoMovimiento;
			movdet.IdCondicionDeLiquidacion = info.IdCondicionDeLiquidacion;
			movdet.IdEmpleadoEjecutivoPortafolio = info.IdEmpleadoEjecutivoPortafolio;
			movdet.IdEmpresa = info.IdEmpresa;
			movdet.IdEstadoMovimientoComprobanteDetalle = "FAC";
			movdet.IdFormaCustodia = info.IdFormaCustodia;
			movdet.IdInstrumento = info.IdInstrumento;
			movdet.IdInstrumentoAnterior = 0L;
			movdet.IdInstrumentoRelacionado = info.IdInstrumentoRelacionado;
			movdet.IdInstrumentoSubyacente = info.IdInstrumentoSubyacente;
			movdet.IdMesa = info.IdMesa;
			movdet.IdMoneda = info.IdMoneda;
			movdet.IdMonedaEmision = info.IdMonedaEmision;
			movdet.IdMonedaLiquidacion = info.IdMonedaLiquidacion;
			movdet.IdMonedaMovimientoComprobanteDetalle = info.IdMonedaMovimientoComprobante;
			movdet.IdMonedaSubyacente = info.IdMonedaSubyacente;
			movdet.IdMonedaSpot = info.IdMonedaSpot;
			movdet.IdMonedaNegociacion = info.IdMonedaNegociacion;
			movdet.IdMovimientoComprobante = info.IdMovimientoComprobante;
			movdet.IdMovimientoComprobanteDetalle = secuenciasMovimientoComprobanteDetalle.get(movimientos.indexOf(mov));
			movdet.IdCuentaCustodioPortafolio = info.IdCuentaCustodioPortafolio;
			movdet.IdEmisor = info.IdEmisor;
			movdet.IdEmisorSubyacente = info.IdEmisorSubyacente;
			movdet.IdOrden = info.IdOrden;
			movdet.IdPersona = info.IdPersona;
			movdet.IdPersonaBancoCustodio = info.IdPersonaBancoCustodio;
			movdet.IdPersonaBancoLiquidador = info.IdPersonaBancoLiquidador;
			movdet.IdPersonaBroker = info.IdPersonaBroker;
			movdet.IdPortafolio = info.IdPortafolio;
			movdet.IdSucursalEmpleadoEjecutivoPortafolio = info.IdSucursalEmpleadoEjecutivoPortafolio;
			movdet.IdTipoCanalIngreso = info.IdTipoCanalIngreso;
			movdet.IdTipoCliente = info.IdTipoCliente;
			movdet.IdTipoComprobante = info.IdTipoComprobante;
			movdet.IdTipoComprobanteReferencia = info.IdTipoComprobanteReferencia;
			movdet.IdTipoInstrumento = info.IdTipoInstrumento;
			movdet.IdTipoInstrumentoRelacionado = info.IdTipoInstrumentoRelacionado;
			movdet.IdTipoInstrumentoSubyacente = info.IdTipoInstrumentoSubyacente;
			movdet.IdTipoOperacion = info.IdTipoOperacion;
			movdet.IdTipoProducto = info.IdTipoProducto;
			movdet.IdTipoReajuste = info.IdTipoReajuste;
			movdet.IdTipoLiquidacionComisionFactura = info.IdTipoLiquidacionComisionFactura;
			movdet.IdTransaccion = info.IdTransaccion;
			movdet.IndicadorOrdenDirecta = info.IndicadorOrdenDirecta;
			movdet.Intereses = info.Intereses;
			movdet.LiquidaBancoCustodio = info.LiquidaBancoCustodio;
			movdet.MercadoSMV = info.MercadoSMV;
			movdet.ModalidadSMV = info.ModalidadSMV;
			movdet.ModPrefFact = info.ModPrefFact;
			movdet.Monto = info.Monto;
			movdet.MontoRaiz = info.MontoRaiz;
			movdet.MontoLiquidacion = info.MontoLiquidacion;
			movdet.MontoMovimientoComprobanteDetalle = info.MontoMovimientoComprobante;
			
			movdet.NemoSVS = info.NemoSVS;
			movdet.NemotecnicoInstrumento = info.NemotecnicoInstrumento;
			movdet.NemotecnicoRelacionado = info.NemotecnicoRelacionado;			
			movdet.NemotecnicoSubyacente = info.NemotecnicoSubyacente;
			movdet.NombreClasificacionTipoInstrumento = info.NombreClasificacionTipoInstrumento;
			movdet.NombreClasificacionTipoInstrumentoRelacionado = info.NombreClasificacionTipoInstrumentoRelacionado;
			movdet.NombreEmpleadoEjecutivoPortafolio = info.NombreEmpleadoEjecutivoPortafolio;
			movdet.NombreLargoCliente = info.NombreLargoCliente;
			movdet.NombreSucursalEmpleadoEjecutivoPortafolio = info.NombreSucursalEmpleadoEjecutivoPortafolio;
			movdet.NombreTipoCliente = info.NombreTipoCliente;
			movdet.NombreTipoInstrumento = info.NombreTipoInstrumento;
			movdet.NombreTipoInstrumentoRelacionado = info.NombreTipoInstrumentoRelacionado;
			movdet.NombreTipoInstrumentoSubyacente = info.NombreTipoInstrumentoSubyacente;
			movdet.Precio = info.Precio;
			movdet.PrecioRaiz = info.PrecioRaiz;
			
			movdet.PrecioPromedio = BigDecimal.ZERO;
			movdet.ReajustesVAC = info.ReajustesVAC;
			movdet.SecMovimientoFacturarMigracion = 0L;
			movdet.SecuenciaAgrupacion = "0";
			movdet.Spread = BigDecimal.ZERO;
			
			movdet.TasaOperacion = info.TasaOperacion;
			if (SI.equalsIgnoreCase(info.EsVentaCorta))
			{
				movdet.TasaOperacion = info.TasaOperacion;
				movdet.MontoAPlazo = calcularMontoAPlazoVC(conn,info.TasaOperacion,info.DiasPlazo,info.Monto,info.IdMoneda,MonedaFormato.MONTO);
				
				//Calcula monto de la prima para Venta Corta (debe usar Precio ingresado al crear la transaccion)
				movdet.PrecioAPlazo = calcularPrimaVC(conn,info.TasaOperacion,info.DiasPlazo,info.Cantidad, info.Precio ,info.IdMoneda,MonedaFormato.MONTO);
				movdet.Tasa = info.Tasa;
				movdet.FechaVencimientoInstrumento = info.FechaVencimientoInstrumento;
			}
			else
			{
				movdet.Tasa = info.Tasa;
				movdet.PrecioAPlazo = info.PrecioAPlazo;
				movdet.MontoAPlazo = info.MontoAPlazo;
			}
			movdet.TasaEmision = info.TasaEmision;
			movdet.TipoDocumentoIdentidad = info.TipoDocumentoIdentidad;
			movdet.TipoComprobanteFacturacion = info.TipoComprobanteFacturacion;
			movdet.ValorCaptacionMonedaOrigen = info.ValorCaptacionMonedaOrigen;
			movdet.ValorParidad = info.ValorParidad;
			movdet.ValorRescate = info.ValorRescate;
			movdet.ValorTipoCambio = info.ValorTipoCambio;
			movdet.Glosa = info.Glosa;
			movdet.EsExento = info.EsExento;
		
			movdet.IdOperacion = info.IdOperacion;
			movdet.CodigoPortafolio = info.CodigoPortafolio;
			//OBTENER COMPROBANTE DETALLE A LA CUAL CORRESPONDE EL MOVIMIENTO
			PreferenciaFacturacion pref = (PreferenciaFacturacion) hashPreferenciasFacturacion.get(movdet.IdPortafolio);
			String llaveComprobante = calculaLLaveComprobante(pref,movdet);
			
			Comprobante_Proceso cte = hashComprobanteLlave.get(llaveComprobante);
			if (cte == null) {
				cte = new Comprobante_Proceso();
				cte.IdComprobante = secuenciasComprobante.get(indexSecuenciaComprobante++);
				cte.llaveComprobante = llaveComprobante;
				EstadoCajaSpot estadoCaja = (EstadoCajaSpot) hashEstadoCajaSpot.get(movdet.IdMovimientoComprobante);
				cte.EstadoCaja = estadoCaja == null ? "NRE" : estadoCaja.EstadoCaja;
				hashComprobanteLlave.put(llaveComprobante,cte);
			}
			movdet.movimientoComprobanteProceso = mov;
			movdet.IdComprobante = cte.IdComprobante;			
			movdet.Contrapartida = info.Contrapartida;
			info.IdComprobante = cte.IdComprobante;
			cte.movimientosComprobanteDetalle.add(movdet);
			           
		}
		
		//GENERAR COMPROBANTES DETALLE
		Integer indexSecuenciaComprobanteDetalle = 0;
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
				PreferenciaFacturacion pref = (PreferenciaFacturacion) hashPreferenciasFacturacion.get(movdet.IdPortafolio);
				String llaveComprobanteDetalle = calculaLLaveComprobanteDetalle(pref,movdet);
				ComprobanteDetalle_Proceso ctedet = hashComprobanteDetalleLlave.get(llaveComprobanteDetalle);
				
				if (ctedet == null) {
					ctedet = new ComprobanteDetalle_Proceso();
					ctedet.llaveComprobanteDetalle = llaveComprobanteDetalle;
					ctedet.IdComprobanteDetalle = secuenciasComprobanteDetalle.get(indexSecuenciaComprobanteDetalle++);
					hashComprobanteDetalleLlave.put(llaveComprobanteDetalle, ctedet);
					cte.comprobanteDetalles.add(ctedet);
					ctedet.IdComprobante = cte.IdComprobante;
				}
				ctedet.movimientoComprobanteDetalles.add(movdet);
				movdet.IdComprobanteDetalle = ctedet.IdComprobanteDetalle;
			}
		}				
		
		//redistribuir Detalles, no toque si no sabe
		{
			int maxDetalle = cantidadMaximaDetalles.intValue();
			int llaveTemp = 1;
			List<Comprobante_Proceso> nuevosComprobantes = new ArrayList<>();
			for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
				
				//comprobante que tiene mas detalles de lo permitido, se deben crear nuevos comprobantes y reasignar
				if (cte.comprobanteDetalles.size() > cantidadMaximaDetalles) {
					
					List<ComprobanteDetalle_Proceso> ctedeteliminar = new ArrayList<>();
					List<MovimientoComprobanteDetalle_Proceso> movdeteliminar = new ArrayList<>();
					Comprobante_Proceso ctenuevo = null;
					for (int ctedetindex = maxDetalle;ctedetindex<cte.comprobanteDetalles.size();ctedetindex++) {
						
						if (ctedetindex%maxDetalle == 0) {
							//nuevo comprobante con detalles que no entran en el comprobante que estamos revisando
							ctenuevo = new Comprobante_Proceso();
							ctenuevo.IdComprobante = secuenciasComprobante.get(indexSecuenciaComprobante++);
							ctenuevo.llaveComprobante = cte.llaveComprobante+(llaveTemp++);
							nuevosComprobantes.add(ctenuevo);
						}
						
						//reasignamos detalles y movimientos al nuevo comprobante
						ComprobanteDetalle_Proceso ctedet = cte.comprobanteDetalles.get(ctedetindex);
						ctedet.IdComprobante = ctenuevo.IdComprobante;
						ctenuevo.comprobanteDetalles.add(ctedet);
						ctedeteliminar.add(ctedet);
						for (MovimientoComprobanteDetalle_Proceso movdet : ctedet.movimientoComprobanteDetalles) {
							movdet.IdComprobante = ctenuevo.IdComprobante;
							ctenuevo.movimientosComprobanteDetalle.add(movdet);
							movdeteliminar.add(movdet);
						}
					}
					
					//eliminamos las asignaciones antiguas
					cte.comprobanteDetalles.removeAll(ctedeteliminar);
					cte.movimientosComprobanteDetalle.removeAll(movdeteliminar);
					
				}
				
			}
			
			for (Comprobante_Proceso cte : nuevosComprobantes) {
				hashComprobanteLlave.put(cte.llaveComprobante, cte);
			}				
		}
		
		List<MovimientoComprobanteExistente_Proceso> listComprobantesExistentes = null;
		HashMap<String,Object> hashComprobantesExistentes = new HashMap<String,Object>();
		//SE OBTIENEN COMPROBANTES EXISTENTES PARA PORTAFOLIOS Y MERCADOS, PARA OMITIR EL COBRO FIJO POR FACTURA DIARIA
		{
			ArrayList<Long> idsPortafolio = new ArrayList<Long>();
			ArrayList<String> idsClasificacion = new ArrayList<String>();
			ArrayList<String> fechasComprobante = new ArrayList<String>();
			ArrayList<String> keyComprobantes = new ArrayList<String>();
			for (MovimientoComprobante_Proceso mov : movimientos) {
				InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
				if (!keyComprobantes.contains(info.IdPortafolio + "_" + info.IdClasificacionTipoInstrumento + "_" + Fecha.toStringDate(info.FechaMovimientoComprobante))) {
					keyComprobantes.add(info.IdPortafolio + "_" + info.IdClasificacionTipoInstrumento + "_" + Fecha.toStringDate(info.FechaMovimientoComprobante));
					idsPortafolio.add(info.IdPortafolio);
					idsClasificacion.add(info.IdClasificacionTipoInstrumento);
					fechasComprobante.add(Fecha.toStringDate(info.FechaMovimientoComprobante));
				}
			}
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("idsPortafolio", idsPortafolio);
			params.put("idsClasificacion", idsClasificacion);
			params.put("fechasComprobante", fechasComprobante);
			listComprobantesExistentes = _sql.query(conn, SQL_SELECT_COMPROBANTES, params, MovimientoComprobanteExistente_Proceso.class);
			hashComprobantesExistentes = Utils.List2Hash(listComprobantesExistentes,"LlavePortafolioClasificacion");
		}
		
		//TOTALIZADORES DE COBROS
		HashMap<Long,BigDecimal> hashMontoCobroPorMovimiento = new HashMap<Long,BigDecimal>();
		HashMap<Long,BigDecimal> hashMontoLiquidacionCobroPorMovimiento = new HashMap<Long,BigDecimal>();
		HashMap<Long,BigDecimal> hashMontoLocalCobroPorMovimiento = new HashMap<Long,BigDecimal>();
		HashMap<Long,BigDecimal> hashMontoFormaPagoPorMovimiento = new HashMap<Long,BigDecimal>(); ///Se crea para actualizar monto de liquidacion de formas de pago para cobros fijos
	
		HashMap<String,MovimientoComprobanteCobro> hashCobrosGasFij = new HashMap<String,MovimientoComprobanteCobro>();
		HashMap<String,MovimientoComprobanteCobro> hashCobrosCLGF = new HashMap<String,MovimientoComprobanteCobro>();
		HashMap<String,MovimientoComprobanteCobro> hashCobrosGasFijIVA = new HashMap<String,MovimientoComprobanteCobro>();
		HashMap<String,String> hashCobrosCLGFCobr = new HashMap<String,String>();
		HashMap<String,String> hashCobrosCLGFCobr2 = new HashMap<String,String>();
				
		String keyGF, keyIvaGF, keyCLGF,keyCLGFCbr;
		Long keyValorGF;			
		BigDecimal montoCobro = BigDecimal.ZERO, montoCobroLiquidacion = BigDecimal.ZERO, montoCobroLocal = BigDecimal.ZERO, valorModificar = BigDecimal.ZERO, valorModificarLoc = BigDecimal.ZERO, valorModificarLiq = BigDecimal.ZERO,
				   montoCobroGFParaIVA = BigDecimal.ZERO, montoCmbteCobroGFParaIVA = BigDecimal.ZERO, montoLiqCobroGFParaIVA = BigDecimal.ZERO;
		
		{				
			List<MovimientoComprobanteCobro> listAnularMovimientosComprobantesCobros = new ArrayList<>();
			
			for (MovimientoComprobante_Proceso mov : movimientos) {
				montoCobro = hashMontoCobroPorMovimiento.get(mov.IdMovimientoComprobante);				
				if (montoCobro == null) {					
					hashMontoCobroPorMovimiento.put(mov.IdMovimientoComprobante,montoCobro);
				}
						
				montoCobroLiquidacion = hashMontoLiquidacionCobroPorMovimiento.get(mov.IdMovimientoComprobante);
				if (montoCobroLiquidacion == null) {	
				
					hashMontoLiquidacionCobroPorMovimiento.put(mov.IdMovimientoComprobante,BigDecimal.ZERO);
				}
						
				montoCobroLocal = hashMontoLocalCobroPorMovimiento.get(mov.IdMovimientoComprobante);
				if (montoCobroLocal == null) {

					hashMontoLocalCobroPorMovimiento.put(mov.IdMovimientoComprobante,BigDecimal.ZERO);
				}
			}
			
		
			for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {	
				montoCobroGFParaIVA = BigDecimal.ZERO;
				montoCmbteCobroGFParaIVA = BigDecimal.ZERO;
				montoLiqCobroGFParaIVA = BigDecimal.ZERO;
				
				for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
					montoCobro = BigDecimal.ZERO;
					montoCobroLiquidacion = BigDecimal.ZERO;
					montoCobroLocal = BigDecimal.ZERO;

					for (MovimientoComprobanteCobro cobro : listMovimientosCobros) {
						if (cobro.IdMovimientoComprobante.equals(movdet.IdMovimientoComprobante)) {
							keyGF = cobro.IdCobro + "-" + cobro.MontoCobro + "-" + cte.IdComprobante;

							if (tiposCobroFijo.contains(cobro.IdCobro)) {
								if (!hashCobrosGasFij.containsKey(keyGF)) {
									hashCobrosGasFij.put(keyGF, cobro);

									if (!"IVA".equalsIgnoreCase(cobro.IdCobro)) {
										keyCLGF = cobro.IdCliente + "-" + cobro.IdPortafolio;

										if (listMovimientosCobrosFijos.size() > 0) {

											// El cobro "GFOD Gasto Fijo por orden Diaria" solo se cobra una vez por dia
											// a un cliente portafolio (en la primera factura)
											Boolean primerCobroFijo = true;
											keyCLGFCbr = cobro.IdCliente + "-" + cobro.IdPortafolio + "-" + cobro.IdCobro;

											for (MovimientoComprobanteCobroFijos cobroFijo : listMovimientosCobrosFijos) {
												if (cobroFijo.IdCliente.equals(cobro.IdCliente) && cobroFijo.IdPortafolio.equals(cobro.IdPortafolio) && cobroFijo.Cantidad > 0) {
													primerCobroFijo = false;
													listAnularMovimientosComprobantesCobros.add(cobro);
												} else {// Se agrega para validar que no calcule mas de una vez cobros
														// fijos cuando es primera factura y se envia mas de una a la
														// vez, la segunda no deberia llevar cobro
													if (hashCobrosCLGFCobr.get(keyCLGFCbr) != null) {
														primerCobroFijo = false;
														listAnularMovimientosComprobantesCobros.add(cobro);
													}
												}
											}

											hashCobrosCLGFCobr.put(keyCLGFCbr, "Existe mismo cobro fijo para el dia en este conjunto de facturas enviadas, pero no aun en BD");

											if (primerCobroFijo == true) {
												montoCobro = montoCobro.add(cobro.MontoCobro);
												hashMontoCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobro);
												montoCobroLiquidacion = montoCobroLiquidacion.add(cobro.MontoLiquidacionCobro);
												hashMontoLiquidacionCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobroLiquidacion);
												montoCobroLocal = montoCobroLocal.add(cobro.MontoMovimientoComprobanteCobro);
												hashMontoLocalCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobroLocal);
												montoCobroGFParaIVA = montoCobroGFParaIVA.add(cobro.MontoCobro);
												montoCmbteCobroGFParaIVA = montoCmbteCobroGFParaIVA.add(cobro.MontoCobro);
												montoLiqCobroGFParaIVA = montoLiqCobroGFParaIVA.add(cobro.MontoLiquidacionCobro);

												hashCobrosCLGF.put(keyCLGF, cobro);
											}

										} else {
											if (!hashCobrosCLGF.containsKey(keyCLGF)) {
												montoCobro = montoCobro.add(cobro.MontoCobro);
												hashMontoCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobro);
												montoCobroLiquidacion = montoCobroLiquidacion.add(cobro.MontoLiquidacionCobro);
												hashMontoLiquidacionCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobroLiquidacion);
												montoCobroLocal = montoCobroLocal.add(cobro.MontoMovimientoComprobanteCobro);
												hashMontoLocalCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobroLocal);
												montoCobroGFParaIVA = montoCobroGFParaIVA.add(cobro.MontoCobro);
												montoCmbteCobroGFParaIVA = montoCmbteCobroGFParaIVA.add(cobro.MontoCobro);
												montoLiqCobroGFParaIVA = montoLiqCobroGFParaIVA.add(cobro.MontoLiquidacionCobro);

												hashCobrosCLGF.put(keyCLGF, cobro);
											}
										}
									}
								} else {
									listAnularMovimientosComprobantesCobros.add(cobro);
								}
							} else {
								montoCobro = montoCobro.add(cobro.MontoCobro);
								hashMontoCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobro);
								montoCobroLiquidacion = montoCobroLiquidacion.add(cobro.MontoLiquidacionCobro);
								hashMontoLiquidacionCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobroLiquidacion);
								montoCobroLocal = montoCobroLocal.add(cobro.MontoMovimientoComprobanteCobro);
								hashMontoLocalCobroPorMovimiento.put(cobro.IdMovimientoComprobante, montoCobroLocal);

								montoCobroGFParaIVA = montoCobroGFParaIVA.add(cobro.MontoCobro);
								montoCmbteCobroGFParaIVA = montoCmbteCobroGFParaIVA.add(cobro.MontoCobro);
								montoLiqCobroGFParaIVA = montoLiqCobroGFParaIVA.add(cobro.MontoLiquidacionCobro);
							}
						}
					}
				}
				
				for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
					for (MovimientoComprobanteCobro cobroIVA : listMovimientosCobros) {
						if ("IVA".equalsIgnoreCase(cobroIVA.IdCobro)) {

							keyIvaGF = "IVA" + "-" + cte.IdComprobante;
							keyValorGF = cobroIVA.IdMovimientoComprobante;

							if (tiposCobroFijo.contains(cobroIVA.IdCobro)) {
								if (cobroIVA.IdMovimientoComprobante.equals(movdet.IdMovimientoComprobante)) {
									if (!hashCobrosGasFijIVA.containsKey(keyIvaGF) || ("COSAFM".equalsIgnoreCase(movdet.IdConceptoMovimiento) && hashCobrosGasFijIVA.containsKey(keyIvaGF))) {
										hashCobrosGasFijIVA.put(keyIvaGF, cobroIVA);

										if (montoCobroGFParaIVA.compareTo(BigDecimal.ZERO) != 0) {
											valorModificar = hashMontoCobroPorMovimiento.get(keyValorGF).add(montoCobroGFParaIVA.multiply(cobroIVA.Porcentaje).divide(BigDecimal.valueOf(100)));

											if (RENTA_VARIABLE.equals(movdet.IdClasificacionTipoInstrumento)&& !cobroIVA.IdMonedaCobro.equals(idMonedaLocal)) {
												valorModificarLoc = hashMontoLocalCobroPorMovimiento.get(keyValorGF).add(montoCobroLocal.multiply(cobroIVA.Porcentaje).divide(BigDecimal.valueOf(100)));
											} else {
												valorModificarLoc = hashMontoLocalCobroPorMovimiento.get(keyValorGF).add(montoCmbteCobroGFParaIVA.multiply(cobroIVA.Porcentaje).divide(BigDecimal.valueOf(100)));
											}

											valorModificarLiq = hashMontoLiquidacionCobroPorMovimiento.get(keyValorGF).add(montoLiqCobroGFParaIVA.multiply(cobroIVA.Porcentaje).divide(BigDecimal.valueOf(100)));

											hashMontoCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, valorModificar);
											hashMontoLiquidacionCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, valorModificarLiq);
											hashMontoLocalCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, valorModificarLoc);

										} else {
											if (montoCobroGFParaIVA.compareTo(BigDecimal.ZERO) == 0 && (!"FOE".equalsIgnoreCase(movdet.IdTipoComprobante)) && NO.equalsIgnoreCase(movdet.Contrapartida)) {
												if ("NEO".equalsIgnoreCase(movdet.IdTipoComprobante)) {
													hashMontoCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, cobroIVA.MontoCobro);
													hashMontoLiquidacionCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, cobroIVA.MontoLiquidacionCobro);
													hashMontoLocalCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, cobroIVA.MontoMovimientoComprobanteCobro);
												} else {
													hashMontoCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, BigDecimal.ZERO);
													hashMontoLiquidacionCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, BigDecimal.ZERO);
													hashMontoLocalCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, BigDecimal.ZERO);
												}
											} else {
												hashMontoCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante,cobroIVA.MontoCobro);
												hashMontoLiquidacionCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, cobroIVA.MontoLiquidacionCobro);
												hashMontoLocalCobroPorMovimiento.put(cobroIVA.IdMovimientoComprobante, cobroIVA.MontoMovimientoComprobanteCobro);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(listAnularMovimientosComprobantesCobros.size() > 0) {
				anulaMovimientosComprobantesCrobrosGFOD(listAnularMovimientosComprobantesCobros,conn);
			}
		}
		//COMPLETAR COMPROBANTES Y COMPROBANTEDETALLE
		List<Comprobante_Proceso> comprobantesFacturacion = new ArrayList<>();
		{
			for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
				
				for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
					
					PreferenciaFacturacion pref = (PreferenciaFacturacion) hashPreferenciasFacturacion.get(movdet.IdPortafolio);
					
					cte.Accion = comparaValor(movdet.Accion, cte.Accion);
					if (cte.Cantidad == null) {
						cte.Cantidad = BigDecimal.ZERO;
					}
					cte.ActualizarTesoreria = comparaValor(movdet.ActualizarTesoreria, cte.ActualizarTesoreria);
					cte.ActualizarCustodia = comparaValor(movdet.ActualizarCustodia, cte.ActualizarCustodia);
					cte.ActualizarCuentaInversion = comparaValor(movdet.ActualizarCuentaInversion, cte.ActualizarCuentaInversion);
					cte.Cantidad = INTERMEDIACION_FINANCIERA.equals(movdet.IdClasificacionTipoInstrumento) && SI.equalsIgnoreCase(movdet.EsFungible) ? new BigDecimal("1") : cte.Cantidad.add(movdet.Cantidad);
					cte.CodigoBroker = comparaValor(movdet.CodigoBroker, cte.CodigoBroker);
					cte.CodigoBancoCustodio = comparaValor(movdet.CodigoBancoCustodio, cte.CodigoBancoCustodio);
					cte.CodigoBancoLiquidador = comparaValor(movdet.CodigoBancoLiquidador, cte.CodigoBancoLiquidador);
					cte.CodigoPortafolio = comparaValor(movdet.CodigoPortafolio, cte.CodigoPortafolio);
					cte.CodigoIFRS = comparaValor(movdet.CodigoIFRS, cte.CodigoIFRS);
					cte.DiasPlazo =  comparaValor(movdet.DiasPlazo, cte.DiasPlazo);
					cte.DireccionFacturacion = comparaValor(movdet.DireccionFacturacion, cte.DireccionFacturacion);
					cte.DocumentoIdentidad = comparaValor(movdet.DocumentoIdentidad, cte.DocumentoIdentidad);
					cte.EsAPlazo = comparaValor(movdet.EsAPlazo, cte.EsAPlazo);
					cte.EsClienteFinal = comparaValor(movdet.EsClienteFinal, cte.EsClienteFinal);
					cte.EsExento = comparaValor(movdet.EsExento, cte.EsExento);
					cte.EsVentaCorta = comparaValor(movdet.EsVentaCorta, cte.EsVentaCorta);
					cte.EsVentaDescubierta = "N";
					cte.FechaComprobanteReferencia = comparaValor(movdet.FechaComprobanteReferencia, cte.FechaComprobanteReferencia);
					cte.FechaComprobante = comparaValor(movdet.FechaComprobante, cte.FechaComprobante);
					cte.FechaEmision = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);
					cte.FechaEnvio = Fecha.toDateTime("0001-01-01 00:00:00");
					cte.FechaLiquidacion = comparaValor(movdet.FechaLiquidacion, cte.FechaLiquidacion);
					cte.FechaRespuestaEnvio = Fecha.toDateTime("0001-01-01 00:00:00");
					cte.FechaSistema = Fecha.toDateTime(fechaSistema + " 00:00:00");
					cte.FechaVencimiento = comparaValor(movdet.FechaVencimiento, cte.FechaVencimiento);
					cte.FolioComprobante = 0L;
					cte.FolioComprobanteReferencia = comparaValor(movdet.FolioComprobanteReferencia, cte.FolioComprobanteReferencia);
					cte.FolioComprobanteFacturacionMigracion = 0L;
					
					if (in.p_Glosa.length() > 0 ) {
						cte.Glosa = in.p_Glosa;
					}else{
						cte.Glosa = comparaValor(movdet.Glosa, cte.Glosa);
					}
					
					cte.IdBolsa = comparaValor(movdet.IdBolsa, cte.IdBolsa);
					cte.IdBoveda = comparaValor(movdet.IdBoveda, cte.IdBoveda);
					cte.IdCarga = 0L;
					
					cte.IdClasificacionTipoInstrumento = comparaValor(movdet.IdClasificacionTipoInstrumento, cte.IdClasificacionTipoInstrumento);
					cte.IdCliente = comparaValor(movdet.IdCliente, cte.IdCliente);
					cte.IdComprobanteReferencia = comparaValor(movdet.IdComprobanteReferencia, cte.IdComprobanteReferencia);
					cte.IdConcepto = comparaValor(movdet.IdConceptoMovimiento, cte.IdConcepto);
					cte.IdCondicionLiquidacion = comparaValor(movdet.IdCondicionDeLiquidacion, cte.IdCondicionLiquidacion);
					cte.IdCuentaCustodioPortafolio = comparaValor(movdet.IdCuentaCustodioPortafolio, cte.IdCuentaCustodioPortafolio);
					cte.IdEstadoDTE = "PEN";
					cte.IdEstadoSII = "PEN";
					cte.IdEstadoComprobante = "FAC";
					cte.IdEmpresa = comparaValor(movdet.IdEmpresa, cte.IdEmpresa);
					cte.IdEmpleadoEjecutivoPortafolio = comparaValor(movdet.IdEmpleadoEjecutivoPortafolio, cte.IdEmpleadoEjecutivoPortafolio);
					cte.IdInstrumento = comparaValor(movdet.IdInstrumento, cte.IdInstrumento);
					cte.IdInstrumentoSubyacente = comparaValor(movdet.IdInstrumentoSubyacente, cte.IdInstrumentoSubyacente);
					if (cte.Intereses == null) {
						cte.Intereses = BigDecimal.ZERO;
					}
					cte.Intereses = cte.Intereses.add(movdet.Intereses);
					cte.IdMoneda = comparaValor(movdet.IdMoneda, cte.IdMoneda);
					cte.IdMonedaLiquidacion = comparaValor(movdet.IdMonedaLiquidacion, cte.IdMonedaLiquidacion);
					cte.IdMonedaSpot = comparaValor(movdet.IdMonedaSpot, cte.IdMonedaSpot);
					cte.IdMonedaComprobante = comparaValor(movdet.IdMonedaMovimientoComprobanteDetalle, cte.IdMonedaComprobante);
					cte.IdMonedaNegociacion = comparaValor(movdet.IdMonedaNegociacion, cte.IdMonedaNegociacion);
					cte.IdPersona = comparaValor(movdet.IdPersona, cte.IdPersona);
					cte.IdPersonaBroker = comparaValor(movdet.IdPersonaBroker, cte.IdPersonaBroker);
					cte.IdPersonaBancoCustodio = comparaValor(movdet.IdPersonaBancoCustodio, cte.IdPersonaBancoCustodio);
					cte.IdPersonaBancoLiquidador = comparaValor(movdet.IdPersonaBancoLiquidador, cte.IdPersonaBancoLiquidador);
					cte.IdPortafolio = comparaValor(movdet.IdPortafolio, cte.IdPortafolio);
					cte.IdRespuestaDTE = 0L;
					cte.IdFormaCustodia = comparaValor(movdet.IdFormaCustodia, cte.IdFormaCustodia);
					cte.IdSucursalEmpleadoEjecutivoPortafolio = comparaValor(movdet.IdSucursalEmpleadoEjecutivoPortafolio, cte.IdSucursalEmpleadoEjecutivoPortafolio);
					cte.IdTipoCliente = comparaValor(movdet.IdTipoCliente, cte.IdTipoCliente);
					cte.IdTipoComprobante = comparaValor(movdet.IdTipoComprobante, cte.IdTipoComprobante);
					cte.IdTipoOperacion = comparaValor(movdet.IdTipoOperacion, cte.IdTipoOperacion);
					cte.IdTipoProducto = comparaValor(movdet.IdTipoProducto, cte.IdTipoProducto);
					cte.IdTipoInstrumento = comparaValor(movdet.IdTipoInstrumento, cte.IdTipoInstrumento);
					cte.IdTipoComprobanteReferencia = comparaValor(movdet.IdTipoComprobanteReferencia, cte.IdTipoComprobanteReferencia);
					cte.IdUsuario = in.p_IdUsuario;
					cte.LiquidaBancoCustodio = comparaValor(movdet.LiquidaBancoCustodio, cte.LiquidaBancoCustodio);
					cte.MercadoSMV = comparaValor(movdet.MercadoSMV, cte.MercadoSMV);
					cte.ModalidadSMV = comparaValor(movdet.ModalidadSMV, cte.ModalidadSMV);
					cte.ModPrefFact = comparaValor(movdet.ModPrefFact, cte.ModPrefFact);
					cte.IdEstadoModPrefFact = ("MODPREFMENS".equalsIgnoreCase(cte.ModPrefFact)? "VIG": "");  
					
					if (cte.Monto == null) {
						cte.Monto = BigDecimal.ZERO;						
					}
					cte.Monto = cte.Monto.add(movdet.Monto);
					
					if (cte.MontoLiquidacion == null) {
						cte.MontoLiquidacion = BigDecimal.ZERO;						
					}					
					cte.MontoLiquidacion = cte.MontoLiquidacion.add(movdet.MontoLiquidacion);
										
					if (cte.MontoCobro == null) {
						cte.MontoCobro = BigDecimal.ZERO;
					}
					
					cte.MontoCobro = cte.MontoCobro.add(hashMontoCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!= null ? hashMontoCobroPorMovimiento.get(movdet.IdMovimientoComprobante): BigDecimal.ZERO);	
															
					cte.MontoFinal = BigDecimal.ZERO;
					
					if (cte.MontoComprobanteFinal == null) {
						cte.MontoComprobanteFinal = BigDecimal.ZERO;
					}
					
					if (cte.MontoLiquidacionFinal == null) {
						cte.MontoLiquidacionFinal = BigDecimal.ZERO;	
					}
					
					boolean AplicarCobroMX;
					
					if ("COMMOC".equalsIgnoreCase(cte.IdTipoOperacion) || "VENMOC".equalsIgnoreCase(cte.IdTipoOperacion)) {						
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(new BigDecimal(0).equals(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante)) ? cte.Monto: hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(new BigDecimal(0).equals(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante))? cte.MontoLiquidacion:hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante));	
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
					}
					else if("VENOTC".equalsIgnoreCase(cte.IdTipoOperacion) || "ANVOTC".equalsIgnoreCase(cte.IdTipoOperacion)) {
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null?MonedaFormato.formatear(conn, hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaComprobante).valor: BigDecimal.ZERO);
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null? MonedaFormato.formatear(conn, hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor: BigDecimal.ZERO);						                                                                                                                                                                             
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null? MonedaFormato.formatear(conn,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor: BigDecimal.ZERO));
					}
					else if (("VENTA".equalsIgnoreCase(cte.Accion) || "COMPRA".equalsIgnoreCase(cte.Accion)) && SI.equalsIgnoreCase(movdet.Contrapartida) && "FOE".equals(cte.IdTipoComprobante) ){
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null?MonedaFormato.formatear(conn, hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaComprobante).valor: BigDecimal.ZERO);
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null? MonedaFormato.formatear(conn, hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor: BigDecimal.ZERO);						                                                                                                                                                                             
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null? MonedaFormato.formatear(conn,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor: BigDecimal.ZERO));
					}
					else if ("COMPRA".equalsIgnoreCase(cte.Accion) && tiposNotaCredito.contains(cte.IdTipoComprobante) && SI.equalsIgnoreCase(movdet.Contrapartida)) {
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!= null?MonedaFormato.formatear(conn,hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaComprobante).valor:BigDecimal.ZERO);
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null?MonedaFormato.formatear(conn,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor:BigDecimal.ZERO);
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null?MonedaFormato.formatear(conn,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor:BigDecimal.ZERO));
					} 
					else if(("COMPRA".equalsIgnoreCase(cte.Accion) && tiposFactura.contains(cte.IdTipoComprobante))
						||("COMPRA".equalsIgnoreCase(cte.Accion) && tiposNotaDebito.contains(cte.IdTipoComprobante))){
						
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!= null?MonedaFormato.formatear(conn,hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaComprobante).valor:BigDecimal.ZERO);
						
						if ("CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion)) {
							AplicarCobroMX = true;
						} else {							
							AplicarCobroMX = SI.equalsIgnoreCase(cobroMXsobrePagoMX) && productosSinCobroMX.stream().anyMatch(p -> p.equals(cte.IdTipoProducto));
						}
						
						if (AplicarCobroMX) {
							cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante) != null ? MonedaFormato.formatear(conn,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante),MonedaFormato.MONTO,cte.IdMonedaLiquidacion).valor: BigDecimal.ZERO);						
							hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null?MonedaFormato.formatear(conn,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor:BigDecimal.ZERO));
						} else {
							cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion);			
							hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion);
						}			
					}
					else if ("COMPRA".equalsIgnoreCase(cte.Accion) && tiposNotaCredito.contains(cte.IdTipoComprobante)) {
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).subtract(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).subtract(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.subtract(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)));
					} 
					else if (("VENTA".equalsIgnoreCase(cte.Accion) && tiposFactura.contains(cte.IdTipoComprobante)) 
						|| ("VENTA".equalsIgnoreCase(cte.Accion) && tiposNotaDebito.contains(cte.IdTipoComprobante)) ){ 
							
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).subtract(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null?MonedaFormato.formatear(conn, hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaComprobante).valor: BigDecimal.ZERO);
						
						if ("CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion)) {
							AplicarCobroMX = true;
						} else {							
							AplicarCobroMX = SI.equalsIgnoreCase(cobroMXsobrePagoMX) && productosSinCobroMX.stream().anyMatch(p -> p.equals(cte.IdTipoProducto));
						}
						
						if (AplicarCobroMX) {
							cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).subtract(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null? MonedaFormato.formatear(conn, hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor: BigDecimal.ZERO);
							hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.subtract(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null? MonedaFormato.formatear(conn,hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor: BigDecimal.ZERO));						
						} else {
							cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion);
							hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion);
						}														                                                                                                                                                                             
					}
					else if ("VENTA".equalsIgnoreCase(cte.Accion) && tiposNotaCredito.contains(cte.IdTipoComprobante)) {
						//cte.MontoFinal = cte.MontoFinal.add(movdet.Monto).add(hashMontoCobroPorMovimiento.get(movdet.IdMovimientoComprobante));	
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)));
					}
					else if (tiposNotaCreditoEsp.contains(cte.IdTipoComprobante)){
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante));	
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)));
					}
					else if ("RESCFM".equalsIgnoreCase(cte.IdTipoOperacion) && tiposFactura.contains(cte.IdTipoComprobante)){
						cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(movdet.MontoMovimientoComprobanteDetalle).add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(movdet.MontoLiquidacion).add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante));
						hashMontoFormaPagoPorMovimiento.put(movdet.IdMovimientoComprobante,movdet.MontoLiquidacion.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)));
					}					
					else {
						throw new Exception("caso no controlado");
					}
					
					cte.MontoPagado = BigDecimal.ZERO;
					
					if (cte.MontoComprobante == null) {
						cte.MontoComprobante = BigDecimal.ZERO;
					}
					cte.MontoComprobante = cte.MontoComprobante.add(movdet.MontoMovimientoComprobanteDetalle);
					if (cte.MontoComprobanteCobro == null) {
						cte.MontoComprobanteCobro = BigDecimal.ZERO;
					}
					cte.MontoComprobanteCobro = cte.MontoComprobanteCobro.add(hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null? hashMontoLocalCobroPorMovimiento.get(movdet.IdMovimientoComprobante): BigDecimal.ZERO);
					if (cte.MontoLiquidacionCobro == null) {
						cte.MontoLiquidacionCobro = BigDecimal.ZERO;
					}
					cte.MontoLiquidacionCobro = cte.MontoLiquidacionCobro.add(hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante)!=null ? hashMontoLiquidacionCobroPorMovimiento.get(movdet.IdMovimientoComprobante) :BigDecimal.ZERO);
					cte.NemotecnicoInstrumento = comparaValor(movdet.NemotecnicoInstrumento, cte.NemotecnicoInstrumento);
					cte.IdEmisorSubyacente = comparaValor(movdet.IdEmisorSubyacente, cte.IdEmisorSubyacente);
					cte.NemotecnicoSubyacente = comparaValor(movdet.NemotecnicoSubyacente, cte.NemotecnicoSubyacente);
					cte.NombreEmpleadoEjecutivoPortafolio = comparaValor(movdet.NombreEmpleadoEjecutivoPortafolio, cte.NombreEmpleadoEjecutivoPortafolio);
					cte.NombreLargoCliente = comparaValor(movdet.NombreLargoCliente, cte.NombreLargoCliente);
					cte.NombreSucursalEmpleadoEjecutivoPortafolio = comparaValor(movdet.NombreSucursalEmpleadoEjecutivoPortafolio, cte.NombreSucursalEmpleadoEjecutivoPortafolio);
					cte.NombreTipoCliente = comparaValor(movdet.NombreTipoCliente, cte.NombreTipoCliente);
					cte.IdPortafolio = movdet.IdPortafolio;
					
					if (SI.equalsIgnoreCase(movdet.EsComprobIndividual)) {
						cte.Precio = movdet.Precio;
					} else {
						if (cte.Cantidad.compareTo(BigDecimal.ZERO) == 0) {
							cte.Precio = comparaValor(movdet.Precio, cte.Precio);
						} else {
							cte.Precio = MonedaFormato.formatear(conn, movdet.Precio, MonedaFormato.PRECIO, cte.IdMoneda, cte.IdTipoInstrumento, "").valor;
						}
					}
					
					if (cte.ReajustesVAC == null) {
						cte.ReajustesVAC = BigDecimal.ZERO;
					}
					cte.ReajustesVAC = cte.ReajustesVAC.add(movdet.ReajustesVAC);
					cte.RespuestaDTEEnvioSII = "";
					cte.RespuestaLocationDTE = "";
					cte.RespuestaPdfDTE = "";
					cte.RespuestaXmlDTE = "";
					cte.Tasa = comparaValor(movdet.Tasa, cte.Tasa);
					cte.TipoDocumentoIdentidad = comparaValor(movdet.TipoDocumentoIdentidad, cte.TipoDocumentoIdentidad);
					cte.TipoComprobanteFacturacion = comparaValor(movdet.TipoComprobanteFacturacion, cte.TipoComprobanteFacturacion);
					cte.ValorTipoCambio = comparaValor(movdet.ValorTipoCambio, cte.ValorTipoCambio);
					cte.ValorParidad = comparaValor(movdet.ValorParidad, cte.ValorParidad);
					cte.IdTipoCanalIngreso = comparaValor(movdet.IdTipoCanalIngreso, cte.IdTipoCanalIngreso);
					cte.IdTipoLiquidacionComisionFactura = comparaValor(movdet.IdTipoLiquidacionComisionFactura, cte.IdTipoLiquidacionComisionFactura);
				}
				
				cte.Monto = MonedaFormato.formatear(conn, cte.Monto, MonedaFormato.MONTO, cte.IdMoneda).valor;
				cte.MontoCobro = MonedaFormato.formatear(conn, cte.MontoCobro, MonedaFormato.MONTO, cte.IdMoneda).valor;
				cte.MontoFinal = MonedaFormato.formatear(conn, cte.MontoFinal, MonedaFormato.MONTO, cte.IdMoneda).valor;
				cte.MontoPagado = MonedaFormato.formatear(conn, cte.MontoPagado, MonedaFormato.MONTO, cte.IdMoneda).valor;
				cte.MontoLiquidacion = MonedaFormato.formatear(conn, cte.MontoLiquidacion, MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor;
				cte.MontoLiquidacionCobro = MonedaFormato.formatear(conn, cte.MontoLiquidacionCobro, MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor;
				cte.MontoLiquidacionFinal = MonedaFormato.formatear(conn, cte.MontoLiquidacionFinal, MonedaFormato.MONTO, cte.IdMonedaLiquidacion).valor;
				cte.MontoComprobante = MonedaFormato.formatear(conn, cte.MontoComprobante, MonedaFormato.MONTO, cte.IdMonedaComprobante).valor;
				cte.MontoComprobanteCobro = MonedaFormato.formatear(conn, cte.MontoComprobanteCobro, MonedaFormato.MONTO, cte.IdMonedaComprobante).valor;
				cte.MontoComprobanteFinal = MonedaFormato.formatear(conn, cte.MontoComprobanteFinal, MonedaFormato.MONTO, cte.IdMonedaComprobante).valor;
				
				PreferenciaFacturacion prefPor = (PreferenciaFacturacion) hashPreferenciasFacturacion.get(cte.IdPortafolio);
				
				if((FUSION_PRECIO_PROMEDIO.equals(prefPor.FusionFacturaVentaCorta) && SI.equalsIgnoreCase(cte.EsVentaCorta)) ||
				   (RENTA_VARIABLE.equals(cte.IdClasificacionTipoInstrumento) && !SIMULTANEA.equalsIgnoreCase(cte.IdTipoInstrumento) 
						   && SI.equalsIgnoreCase(prefPor.FusionFacturaPrecioPromedioRV))) {
						cte.Precio = cte.Monto.divide(cte.Cantidad, MathContext.DECIMAL128);
				}
				
				comprobantesFacturacion.add(cte);
			}
		}
		
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			System.out.println("IdComprobante:" + cte.IdComprobante);
			for (ComprobanteDetalle_Proceso ctedet : cte.comprobanteDetalles) {
				System.out.println("\tIdDetalle:" +ctedet.IdComprobanteDetalle + " - IdComprobante:" + ctedet.IdComprobante);
				for (MovimientoComprobanteDetalle_Proceso mov : ctedet.movimientoComprobanteDetalles) {
					System.out.println("\t\t IdMovimiento:" +mov.IdMovimientoComprobante  + " - IdDetalle:" + mov.IdComprobanteDetalle + " - IdComprobante:" + mov.IdComprobante);
				}
			}
		}
		
		//CORRECCION MONTOS FINALES
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			if (("COMPRA".equalsIgnoreCase(cte.Accion) && tiposFactura.contains(cte.IdTipoComprobante)) 
					|| ("VENTA".equalsIgnoreCase(cte.Accion) && tiposNotaCredito.contains(cte.IdTipoComprobante)) 
					|| ("COMPRA".equalsIgnoreCase(cte.Accion) && tiposNotaDebito.contains(cte.IdTipoComprobante))
					|| ("VENTA".equalsIgnoreCase(cte.Accion) && tiposNotaDebito.contains(cte.IdTipoComprobante))
					|| ("RESCATE".equalsIgnoreCase(cte.Accion) && tiposFactura.contains(cte.IdTipoComprobante))) {
				cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.multiply(new BigDecimal(-1));
				cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.multiply(new BigDecimal(-1));
			}
		}
		
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			Integer nroLinea = 1;
			for (ComprobanteDetalle_Proceso ctedet: cte.comprobanteDetalles) {
				
				ctedet.NroLinea = nroLinea++;
							
				for (MovimientoComprobanteDetalle_Proceso movdet : ctedet.movimientoComprobanteDetalles) {
					PreferenciaFacturacion pref = (PreferenciaFacturacion) hashPreferenciasFacturacion.get(movdet.IdPortafolio);
					
					ctedet.CodigoIFRS = comparaValor(movdet.CodigoIFRS, ctedet.CodigoIFRS);
					ctedet.EsVentaCorta = comparaValor(movdet.EsVentaCorta, ctedet.EsVentaCorta);
					ctedet.FechaComprobanteDetalle = Fecha.toDate(Fecha.toFormat(cte.FechaComprobante,"yyyy-MM-dd"));
					ctedet.FechaLiquidacion = comparaValor(movdet.FechaLiquidacion, ctedet.FechaLiquidacion);
					ctedet.FolioComprobanteFacturacionMigracion = 0L;
					ctedet.IdBolsa = comparaValor(movdet.IdBolsa, ctedet.IdBolsa);
					ctedet.IdCarga = 0L;
					ctedet.IdClasificacionTipoInstrumento = comparaValor(movdet.IdClasificacionTipoInstrumento, ctedet.IdClasificacionTipoInstrumento);
					ctedet.IdClasificacionTipoInstrumentoRelacionado = comparaValor(movdet.IdClasificacionTipoInstrumentoRelacionado, ctedet.IdClasificacionTipoInstrumentoRelacionado);
					ctedet.IdComprobante = cte.IdComprobante;
					ctedet.IdConcepto = comparaValor(movdet.IdConceptoMovimiento, ctedet.IdConcepto);
					ctedet.IdEmpleadoEjecutivoPortafolio = comparaValor(movdet.IdEmpleadoEjecutivoPortafolio, ctedet.IdEmpleadoEjecutivoPortafolio);
					ctedet.IdEstadoComprobanteDetalle = "FAC";
					ctedet.IdFormaCustodia = comparaValor(movdet.IdFormaCustodia, ctedet.IdFormaCustodia);
					ctedet.IdInstrumento = comparaValor(movdet.IdInstrumento, ctedet.IdInstrumento);
					ctedet.IdInstrumentoRelacionado = comparaValor(movdet.IdInstrumentoRelacionado, ctedet.IdInstrumentoRelacionado);
					ctedet.IdInstrumentoSubyacente = comparaValor(movdet.IdInstrumentoSubyacente, ctedet.IdInstrumentoSubyacente);
					ctedet.IdMesa = comparaValor(movdet.IdMesa, ctedet.IdMesa);
					ctedet.IdMoneda = comparaValor(movdet.IdMoneda, ctedet.IdMoneda);
					ctedet.IdMonedaLiquidacion = comparaValor(movdet.IdMonedaLiquidacion, ctedet.IdMonedaLiquidacion);
					ctedet.IdMonedaComprobanteDetalle = comparaValor(movdet.IdMonedaMovimientoComprobanteDetalle, ctedet.IdMonedaComprobanteDetalle);
					ctedet.IdMonedaSubyacente = comparaValor(movdet.IdMonedaSubyacente, ctedet.IdMonedaSubyacente);
					ctedet.IdMonedaSpot = comparaValor(movdet.IdMonedaSpot, ctedet.IdMonedaSpot);
					ctedet.IdMonedaNegociacion = comparaValor(movdet.IdMonedaNegociacion, ctedet.IdMonedaNegociacion);
					ctedet.IndicadorOrdenDirecta = comparaValor(movdet.IndicadorOrdenDirecta, ctedet.IndicadorOrdenDirecta);
					ctedet.IdSucursalEmpleadoEjecutivoPortafolio = comparaValor(movdet.IdSucursalEmpleadoEjecutivoPortafolio, ctedet.IdSucursalEmpleadoEjecutivoPortafolio);
					ctedet.IdTipoInstrumento = comparaValor(movdet.IdTipoInstrumento, ctedet.IdTipoInstrumento);
					ctedet.IdTipoInstrumentoSubyacente = comparaValor(movdet.IdTipoInstrumentoSubyacente, ctedet.IdTipoInstrumentoSubyacente);
					ctedet.IdTipoInstrumentoRelacionado = comparaValor(movdet.IdTipoInstrumentoRelacionado, ctedet.IdTipoInstrumentoRelacionado);
					ctedet.IdTipoLiquidacionComisionFactura = comparaValor(movdet.IdTipoLiquidacionComisionFactura, ctedet.IdTipoLiquidacionComisionFactura);
					ctedet.IdTipoOperacion = comparaValor(movdet.IdTipoOperacion, ctedet.IdTipoOperacion);
					ctedet.IdTipoProducto = comparaValor(movdet.IdTipoProducto, ctedet.IdTipoProducto);
					ctedet.IdTransaccion = comparaValor(movdet.IdTransaccion, ctedet.IdTransaccion);
					ctedet.FolioTransaccion = comparaValor(movdet.FolioTransaccion, ctedet.FolioTransaccion);
					ctedet.FechaTransaccion = comparaValor(movdet.FechaTransaccion, ctedet.FechaTransaccion);
					ctedet.ModPrefFact = comparaValor(movdet.ModPrefFact, ctedet.ModPrefFact);
					ctedet.NemoSVS = comparaValor(movdet.NemoSVS, ctedet.NemoSVS);
					ctedet.NemotecnicoInstrumento = comparaValor(movdet.NemotecnicoInstrumento, ctedet.NemotecnicoInstrumento);
					ctedet.NemotecnicoRelacionado = comparaValor(movdet.NemotecnicoRelacionado, ctedet.NemotecnicoRelacionado);
					ctedet.NemotecnicoSubyacente = comparaValor(movdet.NemotecnicoSubyacente, ctedet.NemotecnicoSubyacente);
					ctedet.NombreTipoInstrumento = comparaValor(movdet.NombreTipoInstrumento, ctedet.NombreTipoInstrumento);
					ctedet.NombreClasificacionTipoInstrumento = comparaValor(movdet.NombreClasificacionTipoInstrumento, ctedet.NombreClasificacionTipoInstrumento);
					ctedet.NombreTipoInstrumentoSubyacente = comparaValor(movdet.NombreTipoInstrumentoSubyacente, ctedet.NombreTipoInstrumentoSubyacente);
					ctedet.NombreTipoInstrumentoRelacionado = comparaValor(movdet.NombreTipoInstrumentoRelacionado, ctedet.NombreTipoInstrumentoRelacionado);
					ctedet.NombreClasificacionTipoInstrumentoRelacionado = comparaValor(movdet.NombreClasificacionTipoInstrumentoRelacionado, ctedet.NombreClasificacionTipoInstrumentoRelacionado);
					
					if (ctedet.Cantidad == null) {
						ctedet.Cantidad = BigDecimal.ZERO;
					}
					ctedet.Cantidad = ctedet.Cantidad.add(movdet.Cantidad);
					
					if (ctedet.ValorRescate == null) {
						ctedet.ValorRescate = BigDecimal.ZERO;
					}
					ctedet.ValorRescate = ctedet.ValorRescate.add(movdet.ValorRescate);
					
					if (ctedet.Tasa == null) {
						ctedet.Tasa = BigDecimal.ZERO;
					}
					ctedet.Tasa = ctedet.Tasa.add(movdet.Tasa);
					
					if (ctedet.Monto == null) {
						ctedet.Monto = BigDecimal.ZERO;
					}
					ctedet.Monto = ctedet.Monto.add(movdet.Monto);
					
					if (ctedet.MontoLiquidacion == null) {
						ctedet.MontoLiquidacion = BigDecimal.ZERO;
					}
					ctedet.MontoLiquidacion = ctedet.MontoLiquidacion.add(movdet.MontoLiquidacion);
					
					if (ctedet.MontoComprobanteDetalle == null) {
						ctedet.MontoComprobanteDetalle = BigDecimal.ZERO;
					}
					ctedet.MontoComprobanteDetalle = ctedet.MontoComprobanteDetalle.add(movdet.MontoMovimientoComprobanteDetalle);
					
					if (SI.equalsIgnoreCase(movdet.EsComprobIndividual)) {
						ctedet.Precio = movdet.Precio;
					}
					else {
						if (ctedet.Cantidad.compareTo(BigDecimal.ZERO) == 0) {	
							ctedet.Precio = comparaValor(movdet.Precio, ctedet.Precio);
						}
						else {
							ctedet.Precio = MonedaFormato.formatear(conn, movdet.Precio, MonedaFormato.PRECIO, ctedet.IdMoneda, ctedet.IdTipoInstrumento, "").valor;
						}
					}
					
					ctedet.ValorTipoCambio = comparaValor(movdet.ValorTipoCambio, ctedet.ValorTipoCambio);					
					ctedet.ValorParidad = comparaValor(movdet.ValorParidad, ctedet.ValorParidad);
					ctedet.Glosa = comparaValor(movdet.Glosa,ctedet.Glosa);
					ctedet.GlosaSimplificada = construirGlosaLarga(movdet,ctedet, listaGlosasTipoInstrumento);  
				
				}
				PreferenciaFacturacion prefPor = (PreferenciaFacturacion) hashPreferenciasFacturacion.get(cte.IdPortafolio);
				
				if((FUSION_PRECIO_PROMEDIO.equals(prefPor.FusionFacturaVentaCorta) && SI.equalsIgnoreCase(ctedet.EsVentaCorta)) ||
				   (RENTA_VARIABLE.equals(ctedet.IdClasificacionTipoInstrumento) && !SIMULTANEA.equalsIgnoreCase(ctedet.IdTipoInstrumento) 
						   && SI.equalsIgnoreCase(prefPor.FusionFacturaPrecioPromedioRV))) {
					ctedet.Precio = ctedet.Monto.divide(ctedet.Cantidad, MathContext.DECIMAL128);					
				}
				
				if(FUSION_PRECIO_PROMEDIO.equals(prefPor.FusionFacturaVentaCorta) && SI.equalsIgnoreCase(ctedet.EsVentaCorta) && RENTA_FIJA.equals(ctedet.IdClasificacionTipoInstrumento)) {
					ctedet.Tasa = valorizarInstrumentoComprobanteDetalleVentaCortaTasaTIR( conn, ctedet, in.p_IdEmpresa, in.p_IdModulo, in.p_IdUsuario );
				}
			}
		}
				
		//COMPROBANTE FORMA DE PAGO
		//Obtiene valor Iva
		List<Cobro> cobros = _sql.query(conn, SQL_SELECT_COBRO_IVA, new HashMap<>(), Cobro.class);
		BigDecimal IVA = new BigDecimal(cobros.get(0).ValorCobro.toString()).divide(BigDecimal.valueOf(100));
		BigDecimal CalculaImporteIva = IVA.add(new BigDecimal(1));	
		
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
				for (MovimientoComprobanteFormaDePago_Proceso pag: listMovimientosFormasDePago) {
					
					if (pag.IdMovimientoComprobante.equals(movdet.IdMovimientoComprobante)) {
						
						ComprobanteFormaDePago_Proceso ctepag = new ComprobanteFormaDePago_Proceso();
						ctepag.CodigoBanco = pag.CodigoBanco;
						ctepag.FechaDocumentoPago = pag.FechaDocumentoPago;
						ctepag.FechaVctoTarjetaBancaria = pag.FechaVctoTarjetaBancaria != null ? pag.FechaVctoTarjetaBancaria : Fecha.toDate("0001-01-01");
						ctepag.IdBanco = pag.IdBanco;
						ctepag.IdComprobante = cte.IdComprobante;
						ctepag.IdComprobanteFormaDePago = secuenciasComprobanteFormaDePago.get(listMovimientosFormasDePago.indexOf(pag));
						ctepag.IdCuentaBancaria = pag.IdCuentaBancaria;
						ctepag.IdEstado = "VIG";
						ctepag.IdMedioDePago = pag.IdMedioDePago;
						ctepag.IdModalidadDePago = pag.IdModalidadDePago;
						ctepag.IdMoneda = pag.IdMoneda;
						ctepag.IdTarjetaBancaria = pag.IdTarjetaBancaria;
						ctepag.IdTipoCuentaBancaria = pag.IdTipoCuentaBancaria;
						ctepag.IdTipoTarjetaBancaria = pag.IdTipoTarjetaBancaria;						
						if (pag.MontoComision.compareTo(BigDecimal.ZERO) == 1 && "CLP".equalsIgnoreCase(pag.IdMoneda)) {							
							ctepag.MontoLiquidacionFormaDePago = (new BigDecimal(0).equals(pag.MontoComision)? pag.MontoComision: (pag.MontoCobro.add(pag.MontoLiquidacionFormaDePago)));
							if ("FMM".equalsIgnoreCase(cte.IdTipoCanalIngreso) && "COMSPT".equalsIgnoreCase(cte.IdTipoOperacion)) {
								ctepag.MontoLiquidacionFormaDePagoTesoreria = pag.MontoLiquidacionFormaDePago.subtract((pag.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128).multiply(pag.TipoCambioCierre).setScale(0, RoundingMode.FLOOR)).multiply(CalculaImporteIva).setScale(0, RoundingMode.FLOOR));
							}else {
								ctepag.MontoLiquidacionFormaDePagoTesoreria = pag.MontoLiquidacionFormaDePago.subtract(new BigDecimal(0).equals(pag.MontoComision)? pag.MontoComision: (pag.MontoComision.multiply(pag.TipoCambioCierre)));
							}
						}else {
							if (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1) {
								ctepag.MontoLiquidacionFormaDePago = (cte.IdMonedaLiquidacion.equalsIgnoreCase(pag.IdMoneda)) ? (cte.IdMoneda.equals(cte.IdMonedaLiquidacion)) ? MonedaFormato.formatear(conn, cte.Monto.abs(), MonedaFormato.MONTO, cte.IdMoneda).valor : MonedaFormato.formatear(conn, cte.MontoLiquidacion.abs(), MonedaFormato.MONTO, cte.IdMoneda).valor : cte.MontoComprobanteCobro.abs();
							} else {
								ctepag.MontoLiquidacionFormaDePago = BigDecimal.ZERO.equals(hashMontoFormaPagoPorMovimiento.get(movdet.IdMovimientoComprobante))? pag.MontoLiquidacionFormaDePago: "SPT".equalsIgnoreCase(movdet.IdTipoInstrumento)? pag.MontoLiquidacionFormaDePago: MonedaFormato.formatear(conn, hashMontoFormaPagoPorMovimiento.get(movdet.IdMovimientoComprobante), MonedaFormato.MONTO, cte.IdMoneda).valor;
							}
							
						}						
						ctepag.NombreBanco = pag.NombreBanco;
						ctepag.NombreCuentaBancaria = pag.NombreCuentaBancaria;
						ctepag.NombreMedioDePago = pag.NombreMedioDePago;
						ctepag.NombreModalidadDePago = pag.NombreModalidadDePago;
						ctepag.NombreTipoCuentaBancaria = pag.NombreTipoCuentaBancaria;
						ctepag.NombreTipoTarjetaBancaria = pag.NombreTipoTarjetaBancaria;
						ctepag.NumeroCuentaBancaria = pag.NumeroCuentaBancaria;
						ctepag.NumeroDocumento = pag.NumeroDocumento;
						ctepag.NumeroSerie = pag.NumeroSerie;
						ctepag.NumeroTarjetaBancaria = pag.NumeroTarjetaBancaria;
						ctepag.AfectarTesoreria = pag.AfectarTesoreria;
						ctepag.AfectarCuentaInversion = pag.AfectarCuentaInversion;
						ctepag.IdTipoMovimientoSpot = pag.IdTipoMovimientoSpot;
						
						cte.agregarComprobanteFormaDePago(ctepag);
					}
				}
			}
		}
		
		//COMPROBANTE COBRO
		HashMap<String,ComprobanteCobro_Proceso> hashCobros = new HashMap<>();		
		HashMap<String,MovimientoComprobanteCobro> hashCobrosCLGF2 = new HashMap<String,MovimientoComprobanteCobro>();
		List<MovimientoComprobanteCobro_Proceso> primerCobroGFOD = new ArrayList<>();
		MovimientoComprobanteCobro_Proceso restarIVA = null;
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			String key, keyIva,keyCLGFCbr2;
						
			BigDecimal montoCobroParaIVA = BigDecimal.ZERO, montoCmbteCobroParaIVA = BigDecimal.ZERO, montoLiqCobroParaIVA = BigDecimal.ZERO;
			montoCobroGFParaIVA = BigDecimal.ZERO;
			montoCmbteCobroGFParaIVA = BigDecimal.ZERO;
			montoLiqCobroGFParaIVA = BigDecimal.ZERO;
			
			
			for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
				for (MovimientoComprobanteCobro_Proceso cobro: listMovimientosCobros) {
					key = movdet.IdPortafolio + "-" + cobro.IdCobro + "-" + cobro.MontoCobro + "-" + cte.IdComprobante;		
					
					if (cobro.IdMovimientoComprobante.equals(movdet.IdMovimientoComprobante)) {
							ComprobanteCobro_Proceso ctecob = new ComprobanteCobro_Proceso();
							ctecob.FechaSistema = Fecha.toDateTime(fechaSistema + " 00:00:00");
							ctecob.IdCarga = 0L;
							ctecob.IdCobro = cobro.IdCobro;
							ctecob.IdComprobante = cte.IdComprobante;
							ctecob.IdComprobanteCobro = secuenciasComprobanteCobro.get(listMovimientosCobros.indexOf(cobro));
							ctecob.IdEstado = "VIG";
							ctecob.IdMonedaCobro = cobro.IdMonedaCobro;
							ctecob.IdMonedaComprobanteCobro = cobro.IdMonedaMovimientoComprobanteCobro;
							ctecob.IdMonedaLiquidacionCobro = cobro.IdMonedaLiquidacionCobro;
							ctecob.MontoCobro = cobro.MontoCobro;
							ctecob.MontoComprobanteCobro = cobro.MontoMovimientoComprobanteCobro;
							ctecob.MontoLiquidacionCobro = cobro.MontoLiquidacionCobro;
							ctecob.Porcentaje = cobro.Porcentaje;
													
							if (tiposCobroFijo.contains(cobro.IdCobro)) {
								if (!hashCobros.containsKey(key)) {
									hashCobros.put(key, ctecob);
									
									if (!"IVA".equalsIgnoreCase(ctecob.IdCobro)) {
										keyCLGF = cobro.IdCliente + "-" + cobro.IdPortafolio;
										
										Boolean primerCobroFijo = true;
										keyCLGFCbr2 = cobro.IdCliente + "-" + cobro.IdPortafolio + "-" + cobro.IdCobro;
										
										if(listMovimientosCobrosFijos.size() > 0) {
											
											// El cobro "GFOD  Gasto Fijo por orden Diaria" solo se cobra una vez por dia a un cliente portafolio (en la primera factura)
											
											for (MovimientoComprobanteCobroFijos cobroFijo : listMovimientosCobrosFijos) {
												if (cobroFijo.IdCliente.equals(cobro.IdCliente) && cobroFijo.IdPortafolio.equals(cobro.IdPortafolio) && cobroFijo.Cantidad > 0)
												{
													primerCobroFijo = false;
												}else {// Se agrega para validar que no calcule mas de una vez cobros fijos cuando es primera factura y se envia mas de una a la vez, la segunda no deberia llevar cobro
											  		if (hashCobrosCLGFCobr2.get(keyCLGFCbr2) != null) {
										  				primerCobroFijo = false;										  				
										  		    }
									            }	
											}
											hashCobrosCLGFCobr2.put(keyCLGFCbr2, "Existe Cobro en este conjunto de facturas enviadas, pero no aun en BD");
											
											if (primerCobroFijo)
											{
												primerCobroGFOD.add(cobro);
												
												cte.agregarComprobanteCobro(ctecob);
												montoCobroParaIVA = montoCobroParaIVA.add(ctecob.MontoCobro);
												
												if (RENTA_VARIABLE.equals(movdet.IdClasificacionTipoInstrumento) && !idMonedaLocal.equals(ctecob.IdMonedaCobro)) {
													montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoComprobanteCobro);
												}else
												{
													montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoCobro);
												}
												
												montoLiqCobroParaIVA = montoLiqCobroParaIVA.add(ctecob.MontoLiquidacionCobro);	
												
												hashCobrosCLGF2.put(keyCLGF, cobro);
											}
										}
										else if("GFOD".equals(cobro.IdCobro)) {
											for(MovimientoComprobanteCobro_Proceso cobroFijo : primerCobroGFOD) {
												if("GFOD".equals(cobroFijo.IdCobro) && cobroFijo.IdCliente.equals(cobro.IdCliente) && cobroFijo.IdPortafolio.equals(cobro.IdPortafolio)) {
													primerCobroFijo = false;
												}
											}
											
											if(primerCobroFijo) {
												primerCobroGFOD.add(cobro);
												
												cte.agregarComprobanteCobro(ctecob);
												montoCobroParaIVA = montoCobroParaIVA.add(ctecob.MontoCobro);
												
												if (RENTA_VARIABLE.equals(movdet.IdClasificacionTipoInstrumento) && !idMonedaLocal.equals(ctecob.IdMonedaCobro)) {
													montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoComprobanteCobro);
												}
												else
												{
													montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoCobro);
												}
												
												montoLiqCobroParaIVA = montoLiqCobroParaIVA.add(ctecob.MontoLiquidacionCobro);	
												
												hashCobrosCLGF2.put(keyCLGF, cobro);
											}
											else {//se quitan los costos asociados al gasto fijo por orden diaria
												cte.MontoCobro = cte.MontoCobro.subtract(cobro.MontoCobro);
												cte.MontoComprobanteCobro = cte.MontoComprobanteCobro.subtract(cobro.MontoCobro);
												cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(cobro.MontoCobro);
												if(!cobro.IdMonedaCobro.equals(cte.IdMoneda)) {
													cte.MontoLiquidacionCobro = cte.MontoLiquidacionCobro.subtract(cobro.MontoCobro.multiply(cte.ValorTipoCambio));
													cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(cobro.MontoCobro.multiply(cte.ValorTipoCambio));
												}
												else {
													cte.MontoLiquidacionCobro = cte.MontoLiquidacionCobro.subtract(cobro.MontoCobro);
													cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(cobro.MontoCobro);
												}
												restarIVA = cobro;
											}
										}
										else if(!hashCobrosCLGF2.containsKey(keyCLGF)) {
									    	cte.agregarComprobanteCobro(ctecob);
									    	montoCobroParaIVA = montoCobroParaIVA.add(ctecob.MontoCobro);
											
											if (RENTA_VARIABLE.equals(movdet.IdClasificacionTipoInstrumento) && !idMonedaLocal.equals(ctecob.IdMonedaCobro)) {
												montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoComprobanteCobro);
											}
											else
											{
												montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoCobro);
											}
											
											montoLiqCobroParaIVA = montoLiqCobroParaIVA.add(ctecob.MontoLiquidacionCobro);	
											
											hashCobrosCLGF2.put(keyCLGF, cobro);													
									    }    
									}
									else if(restarIVA != null && restarIVA.IdCliente.equals(restarIVA.IdCliente) && restarIVA.IdPortafolio.equals(restarIVA.IdPortafolio)) {
										cte.MontoCobro = cte.MontoCobro.subtract(restarIVA.MontoCobro.multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
										cte.MontoComprobanteCobro = cte.MontoComprobanteCobro.subtract(restarIVA.MontoCobro.multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
										cte.MontoComprobanteFinal = cte.MontoComprobanteFinal.add(restarIVA.MontoCobro.multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
										if(!restarIVA.IdMonedaCobro.equals(cte.IdMoneda)) {
											cte.MontoLiquidacionCobro = cte.MontoLiquidacionCobro.subtract(restarIVA.MontoCobro.multiply(cte.ValorTipoCambio).multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
											cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(restarIVA.MontoCobro.multiply(cte.ValorTipoCambio).multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
										}
										else {
											cte.MontoLiquidacionCobro = cte.MontoLiquidacionCobro.subtract(restarIVA.MontoCobro.multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
											cte.MontoLiquidacionFinal = cte.MontoLiquidacionFinal.add(restarIVA.MontoCobro.multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
										}
										restarIVA = null;
									}
								}
							} else {
								cte.agregarComprobanteCobro(ctecob);
								montoCobroParaIVA = montoCobroParaIVA.add(ctecob.MontoCobro);
								
								if (RENTA_VARIABLE.equals(movdet.IdClasificacionTipoInstrumento) && !idMonedaLocal.equals(ctecob.IdMonedaCobro)) {
									montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoComprobanteCobro);
								}
								else
								{
									montoCmbteCobroParaIVA = montoCmbteCobroParaIVA.add(ctecob.MontoCobro);
								}
								
								montoLiqCobroParaIVA = montoLiqCobroParaIVA.add(ctecob.MontoLiquidacionCobro);
							}
							montoCobroParaIVA = MonedaFormato.formatear(conn, montoCobroParaIVA, MonedaFormato.MONTO, ctecob.IdMonedaCobro).valor;
							montoCmbteCobroParaIVA = MonedaFormato.formatear(conn, montoCmbteCobroParaIVA, MonedaFormato.MONTO, ctecob.IdMonedaComprobanteCobro).valor;
							montoLiqCobroParaIVA = MonedaFormato.formatear(conn, montoLiqCobroParaIVA, MonedaFormato.MONTO, ctecob.IdMonedaLiquidacionCobro).valor;
					}
				}
			}
			
	
			for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {	
			  for (MovimientoComprobanteCobro_Proceso cobroIVA: listMovimientosCobros) {
				if ("IVA".equalsIgnoreCase(cobroIVA.IdCobro)) {
					keyIva = cte.IdPortafolio + "-" + "IVA" + "-" + cte.IdComprobante;		
	
					ComprobanteCobro_Proceso ctecobIVA = new ComprobanteCobro_Proceso();
					
					if (tiposCobroFijo.contains(cobroIVA.IdCobro)) {
					  if (cobroIVA.IdMovimientoComprobante.equals(movdet.IdMovimientoComprobante)) {	
						  if (!hashCobros.containsKey(keyIva) || ("COSAFM".equalsIgnoreCase(movdet.IdConceptoMovimiento) && hashCobros.containsKey(keyIva))) {
							hashCobros.put(keyIva, ctecobIVA);
								
								ctecobIVA.FechaSistema = Fecha.toDateTime(fechaSistema + " 00:00:00");
								ctecobIVA.IdCarga = 0L;
								ctecobIVA.IdCobro = cobroIVA.IdCobro;
								ctecobIVA.IdComprobante = cte.IdComprobante;
								ctecobIVA.IdComprobanteCobro = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro",1L).get(0);
								ctecobIVA.IdEstado = "VIG";
								ctecobIVA.IdMonedaCobro = cobroIVA.IdMonedaCobro;
								ctecobIVA.IdMonedaComprobanteCobro = cobroIVA.IdMonedaMovimientoComprobanteCobro;
								ctecobIVA.IdMonedaLiquidacionCobro = cobroIVA.IdMonedaLiquidacionCobro;
								ctecobIVA.Porcentaje = cobroIVA.Porcentaje;															
								ctecobIVA.MontoCobro = (montoCobroParaIVA.compareTo(BigDecimal.ZERO) != 0 ? montoCobroParaIVA.multiply(cobroIVA.Porcentaje).divide(BigDecimal.valueOf(100)): montoCobroParaIVA.compareTo(BigDecimal.ZERO) == 0 && (!"FOE".equalsIgnoreCase(movdet.IdTipoComprobante))? (("NEO".equalsIgnoreCase(movdet.IdTipoComprobante)) ? cobroIVA.MontoCobro : BigDecimal.valueOf(0)) : cobroIVA.MontoCobro);								
								ctecobIVA.MontoComprobanteCobro = (montoCobroParaIVA.compareTo(BigDecimal.ZERO) != 0 ? montoCmbteCobroParaIVA.multiply(cobroIVA.Porcentaje).divide(BigDecimal.valueOf(100)): montoCobroParaIVA.compareTo(BigDecimal.ZERO) == 0 && (!"FOE".equalsIgnoreCase(movdet.IdTipoComprobante))? (("NEO".equalsIgnoreCase(movdet.IdTipoComprobante)) ? cobroIVA.MontoMovimientoComprobanteCobro : BigDecimal.valueOf(0)) : cobroIVA.MontoMovimientoComprobanteCobro);
								ctecobIVA.MontoLiquidacionCobro = (montoCobroParaIVA.compareTo(BigDecimal.ZERO) != 0 ? montoLiqCobroParaIVA.multiply(cobroIVA.Porcentaje).divide(BigDecimal.valueOf(100)): montoCobroParaIVA.compareTo(BigDecimal.ZERO) == 0 && (!"FOE".equalsIgnoreCase(movdet.IdTipoComprobante))? (("NEO".equalsIgnoreCase(movdet.IdTipoComprobante)) ? cobroIVA.MontoLiquidacionCobro : BigDecimal.valueOf(0)) : cobroIVA.MontoLiquidacionCobro);
								
								ctecobIVA.MontoCobro =  MonedaFormato.formatear(conn, ctecobIVA.MontoCobro, MonedaFormato.MONTO, ctecobIVA.IdMonedaCobro).valor;
								ctecobIVA.MontoComprobanteCobro =  MonedaFormato.formatear(conn, ctecobIVA.MontoComprobanteCobro, MonedaFormato.MONTO, ctecobIVA.IdMonedaComprobanteCobro).valor;
								ctecobIVA.MontoLiquidacionCobro =  MonedaFormato.formatear(conn, ctecobIVA.MontoLiquidacionCobro, MonedaFormato.MONTO, ctecobIVA.IdMonedaLiquidacionCobro).valor;
								cte.agregarComprobanteCobro(ctecobIVA);									
						}							
					  }
					}
				} 
			  }		
			}				
			
		}
				
		//-----------------------------------------------------------------------------------------------------------------------
		//LLAMADA SERVICIOS-----------------------------------------------------------------------------------------------------------------------

		List<Comprobante_Proceso> comprobantesAValidar = new ArrayList<Comprobante_Proceso>(hashComprobanteLlave.values());
		
		boolean hayMontosNegativos = comprobantesAValidar.stream()
			.anyMatch(item -> tiposFactura.contains(item.TipoComprobanteFacturacion)
					&& "VENTA".equalsIgnoreCase(item.Accion) 
					&& item.MontoLiquidacionFinal != null && item.MontoLiquidacionFinal.compareTo(BigDecimal.ZERO) < 0);
		
		if(hayMontosNegativos) {
			String codigo = "UI_MENSAJE_MONTO_NEGATIVO";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			out.P_OUT_ERRORESCONTADOR++;
			return out;
		}
		
		List<MovimientoComprobanteDetalle_Proceso> datosGestionCustodia = new ArrayList<MovimientoComprobanteDetalle_Proceso>();
		List<Comprobante_Proceso> datosNotasCredito = new ArrayList<>();
		HashMap<Long,MovimientoFusionVC> movimientosVc = new HashMap<>();
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			
			//notas de credito
			if ("NCR".equalsIgnoreCase(cte.TipoComprobanteFacturacion) && (SI.equalsIgnoreCase(cte.ActualizarCustodia)) && (!"ANSIMV".equalsIgnoreCase(cte.IdTipoOperacion) && !"ANSIMC".equalsIgnoreCase(cte.IdTipoOperacion)))
			{
				datosNotasCredito.add(cte);
			}
			else {
				for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
					
					if (!SI.equalsIgnoreCase(movdet.ActualizarCustodia)) {
						continue;
					}
					// si es Prestamo RF y venta corta , no enviar a CUSTODIA siempre que parametro este encendido 
					if(SI.equalsIgnoreCase(movdet.EsVentaCorta) && SI.equalsIgnoreCase(PERMITE_SALTO_CUSTODIA_VENTA_CORTA_RENTA_FIJA) && "VCRF".equalsIgnoreCase(movdet.IdTipoOperacion)) {
						continue;
					}
					datosGestionCustodia.add(movdet);
				}
			}
		}

		{
			
			if (datosGestionCustodia.size() > 0 || datosNotasCredito.size() > 0) {
				ProcesarFacturacionCustodia enviarMovimientosCustodia = new ProcesarFacturacionCustodia();
				ProcesarFacturacionCustodia_Entrada entrada = new ProcesarFacturacionCustodia_Entrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdModulo = "FAC";
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_simularOperacion = in.p_simularOperacion;
				entrada.p_UsaReglasNegocio = "S";
				entrada.p_Movimientos = datosGestionCustodia;
				entrada.p_NotasCredito = datosNotasCredito;
				entrada.p_Comprobantes = comprobantesFacturacion;
				
				ProcesarFacturacionCustodia_Salida salida = enviarMovimientosCustodia.logica(conn, entrada );
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				
				if (salida.retirosFin != null) {
					for (RetiroCustodia.ObjDatosEntrada registro : salida.retirosFin) {
						for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
							if (registro.FolioComprobante.equals(cte.IdComprobante)) {
								cte.EsVentaDescubierta = registro.EsVentaDescubierta;
							}
						}
					}
				}
				
				for (MensajeSalida msj : salida.P_OUT_MENSAJES) {
					
					for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
						for (MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
							
							if (msj.id.equals(movdet.IdMovimientoComprobanteDetalle)) {
								if (msj.tipo == MensajeSalida.MENSAJE_ADVERTENCIA) {
									cte.advertencias++;
								}
								if (msj.tipo == MensajeSalida.MENSAJE_AUTORIZACION) {
									cte.autorizaciones++;
								}
								if (msj.tipo == MensajeSalida.MENSAJE_ERROR) {
									cte.errores++;
								}
							}
						}
					}
				}
				
				limpiaComprobantes(hashComprobanteLlave,hashComprobanteLlaveErrores);
				
				if (hashComprobanteLlave.size() == 0) {
					return out;
				}
			}
			
		}	
		
		/***** Cuenta Inversion ****/
		//LLAMAR CUENTA INVERSION MOVIMIENTOS SPOT
		List<ObjCargoAbonoAGenerar> listCargoAbono = new ArrayList<>();
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			
			if (!SI.equalsIgnoreCase(cte.ActualizarCuentaInversion)) {
				continue;
			}
			if (!"MEME".equalsIgnoreCase(cte.IdTipoProducto)) {
				continue;
			}

			
			String idTipoCanalIngreso = "";
			
			
			if(cte.comprobanteDetalles.get(0) != null) {
				
				idTipoCanalIngreso = cte.comprobanteDetalles.get(0).movimientoComprobanteDetalles.get(0).IdTipoCanalIngreso;
				
			}
			
			for (ComprobanteFormaDePago_Proceso pag : cte.comprobanteFormasDePago) {
				
				if (NO.equalsIgnoreCase(pag.AfectarCuentaInversion)) {
					continue;
				}
				
				ObjCargoAbonoAGenerar cab = new ObjCargoAbonoAGenerar();
				
				cab.NumeroCargoAbono = pag.IdComprobanteFormaDePago;
				cab.IdPortafolio = cte.IdPortafolio;
				cab.IdMoneda = pag.IdMoneda;
				
				if ("MOVING".equalsIgnoreCase(pag.IdTipoMovimientoSpot)) {
					cab.IdTipoMovimiento = "MOVCAR";
				} else if ("MOVEGR".equalsIgnoreCase(pag.IdTipoMovimientoSpot)) {
					cab.IdTipoMovimiento = "MOVABN";
				} else {
					cab.IdTipoMovimiento = "";
				}
				
				cab.IdConcepto = cte.IdConcepto;
				cab.Descripcion = "";
				cab.IdCondicionDeLiquidacion = cte.IdCondicionLiquidacion;
				cab.FechaCondicionDeLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
				cab.Monto = pag.MontoLiquidacionFormaDePago.abs();					
				cab.IdEjecutivo = cte.IdEmpleadoEjecutivoPortafolio;
				cab.IdSucursal = cte.IdSucursalEmpleadoEjecutivoPortafolio;
				cab.IdTipoProducto = cte.IdTipoProducto;
				cab.IdTipoOperacion = cte.IdTipoOperacion;
				cab.IdOperacion = cte.IdComprobante;
				
				if ("PE".equalsIgnoreCase(localizacionSistema)) {
					cab.FechaOperacion = Fecha.toStringDate(cte.FechaComprobante);				
				}else{
					cab.FechaOperacion = "0001-01-01";	
				}
				
				cab.FechaLiquidacionOperacion = "0001-01-01";
				cab.FechaVencimientoOperacion = Fecha.toStringDatetime(cte.FechaVencimiento);
				cab.IdMonedaOperacion = cte.IdMoneda;
				cab.MontoOperacion = cte.MontoLiquidacionFinal.abs();
				cab.TipoCambioOperacion = BigDecimal.ZERO;
				cab.IdContraparte = 0L;
				cab.IdTipoComprobante = cte.IdTipoComprobante;
				cab.IdComprobante = cte.IdComprobante;
				cab.FolioComprobante = cte.FolioComprobante+"";
				cab.IdComprobanteDetalle = 0L;
				cab.IdComprobanteFormaDePago = 0L;
				cab.IdTipoMovimientoAsociado = cte.IdTipoComprobante;
				cab.IdMovimientoAsociado = cte.IdComprobante;
				cab.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);
				
				if (cab.IdTipoMovimientoAsociado == null) {
					cab.IdTipoMovimientoAsociado = "";
				}
				
				if (cab.IdMovimientoAsociado == null ) {
					cab.IdMovimientoAsociado = 0L;
				}
				
				cab.IdPersonaPagoCobro = 0L;
				cab.IdInstrumento = cte.IdInstrumento;
				cab.IdTipoInstrumento = cte.IdTipoInstrumento;
				cab.Nemo = cte.NemotecnicoInstrumento;
				cab.EsIndicadorBilateral = "N";
				cab.EsCargoAbonoPorTraspaso = "N";
				cab.IdPortafolioTraspaso = 0L;
				//no fue a tesoreria
				if ("".equalsIgnoreCase(cab.IdTipoMovimientoAsociado)) {
					cab.GeneroContraparteEnTesoreria = "N";
				}
				else {
					cab.GeneroContraparteEnTesoreria = "S";
				}
				cab.CertificadoAutomatico = "S";
				cab.Cantidad = cte.Cantidad;
				
				listCargoAbono.add(cab);
			}
			
		}
		
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			
			if (!SI.equalsIgnoreCase(cte.ActualizarCuentaInversion)) {
				continue;
			}
			
			if ("MEME".equalsIgnoreCase(cte.IdTipoProducto)) {
				continue;
			}
			
			ObjCargoAbonoAGenerar cab = new ObjCargoAbonoAGenerar();
			
			cab.NumeroCargoAbono = cte.IdComprobante;
			cab.IdPortafolio = cte.IdPortafolio;
			cab.IdMoneda = cte.IdMonedaLiquidacion;
			
			if ("VENPRT".equalsIgnoreCase(cte.IdTipoOperacion) || "ANTPRT".equalsIgnoreCase(cte.IdTipoOperacion)) {
				cab.IdTipoMovimiento = "MOVCAR";
			}
			else if ("VENEMP".equalsIgnoreCase(cte.IdTipoOperacion) || "ANTEMP".equalsIgnoreCase(cte.IdTipoOperacion)) {
				cab.IdTipoMovimiento = "MOVABN";
			}
			else if ((tiposFactura.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion))) {
				cab.IdTipoMovimiento = "MOVCAR";
			}
			else if ( (tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == 1 ) 
			    || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == 1 ) ){
				cab.IdTipoMovimiento = "MOVABN";
			}
			else if ((tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == -1 ) 
			   || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == -1 ) ){
				cab.IdTipoMovimiento = "MOVCAR";
			}
			else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) {
				cab.IdTipoMovimiento = "MOVCAR";
			}
			else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)) {
				cab.IdTipoMovimiento = "MOVABN";
			}
			else if (tiposNotaCreditoEsp.contains(cte.IdTipoComprobante)) {
				cab.IdTipoMovimiento = "MOVABN";
			}else if ((tiposFactura.contains(cte.IdTipoComprobante) && "RESCATE".equalsIgnoreCase(cte.Accion))) {
				cab.IdTipoMovimiento = "MOVCAR";
			}else{
				   cab.IdTipoMovimiento = "";
			}
			
			cab.IdConcepto = cte.IdConcepto;
			cab.Descripcion = "";
			cab.IdCondicionDeLiquidacion = cte.IdCondicionLiquidacion;
			cab.FechaCondicionDeLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
			//Condicion que solo afecta a las facturas que se hacen con CFI en moneda de liquidacion distinta de CLP
			if ((!"CLP".equalsIgnoreCase(cte.IdMoneda) && "CFI".equalsIgnoreCase(cte.IdTipoInstrumento))
					|| (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1)
			)
			{
				if(cte.IdMoneda.equals(cte.IdMonedaLiquidacion)) {
					cab.MontoOperacion = cte.Monto.abs();
					cab.Monto = cte.Monto.abs();
				}
				else {
					cab.MontoOperacion = cte.MontoLiquidacion.abs();
					cab.Monto = cte.MontoLiquidacion.abs();
				}
			}
			else {
				cab.MontoOperacion = cte.MontoLiquidacionFinal.abs();
				cab.Monto = cte.MontoLiquidacionFinal.abs();
			}
			cab.IdEjecutivo = cte.IdEmpleadoEjecutivoPortafolio;
			cab.IdSucursal = cte.IdSucursalEmpleadoEjecutivoPortafolio;
			cab.IdTipoProducto = cte.IdTipoProducto;
			cab.IdTipoOperacion = cte.IdTipoOperacion;
			cab.IdOperacion = cte.IdComprobante;
			
			
			if ("PE".equalsIgnoreCase(localizacionSistema)) {
				cab.FechaOperacion = Fecha.toStringDate(cte.FechaComprobante);				
			}else{
				cab.FechaOperacion = "0001-01-01";	
			}
			
			cab.FechaLiquidacionOperacion = cte.FechaLiquidacion.toString();
			cab.FechaVencimientoOperacion = Fecha.toStringDatetime(cte.FechaVencimiento);
			cab.IdMonedaOperacion = cte.IdMoneda;
			
			cab.TipoCambioOperacion = BigDecimal.ZERO;
			cab.IdContraparte = 0L;
			cab.IdTipoComprobante = cte.IdTipoComprobante;
			cab.IdComprobante = cte.IdComprobante;
			cab.FolioComprobante = cte.FolioComprobante+"";
			cab.IdComprobanteDetalle = 0L;
			cab.IdComprobanteFormaDePago = 0L;
			cab.IdTipoMovimientoAsociado = cte.IdTipoComprobante;
			cab.IdMovimientoAsociado = cte.IdComprobante;
			cab.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);
			
			if (cab.IdTipoMovimientoAsociado == null) {
				cab.IdTipoMovimientoAsociado = "";
			}
			
			if (cab.IdMovimientoAsociado == null ) {
				cab.IdMovimientoAsociado = 0L;
			}
			
			cab.IdPersonaPagoCobro = 0L;
			cab.IdInstrumento = "SIM".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "PRT".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "EMP".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "RRV".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "RRF".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: cte.IdInstrumento;
			cab.IdTipoInstrumento = cte.IdTipoInstrumento;
			
			cab.Nemo = "SIM".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "PRT".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "EMP".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "RRV".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "RRF".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: cte.NemotecnicoInstrumento; 
			cab.EsIndicadorBilateral = "N";
			cab.EsCargoAbonoPorTraspaso = "N";
			cab.IdPortafolioTraspaso = 0L;
			//no fue a tesoreria
			if ("".equalsIgnoreCase(cab.IdTipoMovimientoAsociado)) {
				cab.GeneroContraparteEnTesoreria = "N";
			}
			else {
				cab.GeneroContraparteEnTesoreria = "S";
			}
			cab.CertificadoAutomatico = "S";
			
			listCargoAbono.add(cab);
			
			if((!"CLP".equalsIgnoreCase(cte.IdMoneda) && "CFI".equalsIgnoreCase(cte.IdTipoInstrumento) && cte.MontoComprobanteCobro.compareTo(BigDecimal.ZERO) > 0)
					|| (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1)
					)
			{
				ObjCargoAbonoAGenerar cab2 = new ObjCargoAbonoAGenerar();
				
				cab2.NumeroCargoAbono = cte.IdComprobante*1000;//cobro Comision+Iva, X1000 es solo para diferenciarlo del movimiento original
				cab2.IdPortafolio = cte.IdPortafolio;
				cab2.IdMoneda = "CLP";
				
				cab2.IdTipoMovimiento = (!tiposNotaCredito.contains(cte.IdTipoComprobante)) ? "MOVCAR" : "MOVABN";
				cab2.IdConcepto = productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) ? cte.IdConcepto : "FDMOOT";//concepto para el cobro solamente
				cab2.Descripcion = "";
				cab2.IdCondicionDeLiquidacion = cte.IdCondicionLiquidacion;
				cab2.FechaCondicionDeLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
				
				//En caso de cambiar cab2.IdMoneda por uno distinto de CLP, se debe cambiar el campo desde donde se obtiene el monto de los cobros
				//cte.MontoCobro -> Monto en la moneda del cobro, cte.MontoComprobanteCobro -> Monto en la moneda del comprobante (moneda local CLP)
				cab2.Monto =  MonedaFormato.formatear(conn, cte.MontoComprobanteCobro, MonedaFormato.MONTO, cab2.IdMoneda).valor;
				cab2.MontoOperacion = MonedaFormato.formatear(conn, cte.MontoComprobanteCobro, MonedaFormato.MONTO, cab2.IdMoneda).valor;
				
				cab2.IdEjecutivo = cte.IdEmpleadoEjecutivoPortafolio;
				cab2.IdSucursal = cte.IdSucursalEmpleadoEjecutivoPortafolio;
				cab2.IdTipoProducto = cte.IdTipoProducto;
				cab2.IdTipoOperacion = cte.IdTipoOperacion;
				cab2.IdOperacion = cte.IdComprobante;
				
				if ("PE".equalsIgnoreCase(localizacionSistema)) {
					cab2.FechaOperacion = Fecha.toStringDate(cte.FechaComprobante);				
				}else{
					cab2.FechaOperacion = "0001-01-01";	
				}
				
				cab2.FechaLiquidacionOperacion = cte.FechaLiquidacion.toString();
				cab2.FechaVencimientoOperacion = Fecha.toStringDatetime(cte.FechaVencimiento);
				cab2.IdMonedaOperacion = "CLP";
				cab2.TipoCambioOperacion = BigDecimal.ZERO;
				cab2.IdContraparte = 0L;
				cab2.IdTipoComprobante = cte.IdTipoComprobante;
				cab2.IdComprobante = cte.IdComprobante;
				cab2.FolioComprobante = cte.FolioComprobante+"";
				cab2.IdComprobanteDetalle = 0L;
				cab2.IdComprobanteFormaDePago = 0L;
				cab2.IdTipoMovimientoAsociado = cte.IdTipoComprobante;
				cab2.IdMovimientoAsociado = cte.IdComprobante;
				cab2.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);
				
				if (cab2.IdTipoMovimientoAsociado == null) {
					cab2.IdTipoMovimientoAsociado = "";
				}
				
				if (cab2.IdMovimientoAsociado == null ) {
					cab2.IdMovimientoAsociado = 0L;
				}
				
				cab2.IdPersonaPagoCobro = 0L;
				cab2.IdInstrumento = "SIM".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "PRT".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "EMP".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "RRV".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "RRF".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: cte.IdInstrumento;
				cab2.IdTipoInstrumento = cte.IdTipoInstrumento;
				
				cab2.Nemo = "SIM".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "PRT".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "EMP".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "RRV".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "RRF".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: cte.NemotecnicoInstrumento; 
				cab2.EsIndicadorBilateral = "N";
				cab2.EsCargoAbonoPorTraspaso = "N";
				cab2.IdPortafolioTraspaso = 0L;
				//no fue a tesoreria
				if ("".equalsIgnoreCase(cab2.IdTipoMovimientoAsociado)) {
					cab2.GeneroContraparteEnTesoreria = "N";
				}
				else {
					cab2.GeneroContraparteEnTesoreria = "S";
				}
				cab2.CertificadoAutomatico = "S";
				listCargoAbono.add(cab2);
			}
		}
		
		//APV-B RETENCION - CARGO A CUENTA INVERSION
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			InfoMovimientoSpot_Proceso infoMovimientoSpotProceso = null;
			if (!SI.equalsIgnoreCase(cte.ActualizarCuentaInversion)) {
				continue;
			}
			if("MEME".equals(cte.IdTipoProducto)) {
				if(cte.comprobanteDetalles.get(0) != null) {
					Long idSpot = cte.comprobanteDetalles.get(0).IdTransaccion;
					infoMovimientoSpotProceso = hashInfoMovimientosSpot.get(idSpot);
				}
			}
			for (ComprobanteFormaDePago_Proceso pag : cte.comprobanteFormasDePago) {
				if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) && "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) 
						&& tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && !"CIN".equalsIgnoreCase(pag.IdMedioDePago)){
					ObjCargoAbonoAGenerar cab = new ObjCargoAbonoAGenerar();
					
					PorcentajeRetencionRegimenAPVB porcentaje= (PorcentajeRetencionRegimenAPVB) hashprocentajeRetencionAPV.get(cte.CodigoPortafolio);
					cab.NumeroCargoAbono = (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1 && !cte.IdMonedaLiquidacion.equalsIgnoreCase(pag.IdMoneda) && "CLP".equalsIgnoreCase(pag.IdMoneda)) ? cte.IdComprobante * 100 : cte.IdComprobante * 10000;
					cab.IdPortafolio = cte.IdPortafolio;
					cab.IdMoneda = (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1 && !cte.IdMonedaLiquidacion.equalsIgnoreCase(pag.IdMoneda) && "CLP".equalsIgnoreCase(pag.IdMoneda)) ? "CLP" : cte.IdMonedaLiquidacion;
					cab.IdTipoMovimiento = "MOVCAR";
					cab.IdConcepto = "CAREAB";
					cab.Descripcion = "";
					cab.IdCondicionDeLiquidacion = cte.IdCondicionLiquidacion;
					cab.FechaCondicionDeLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
					cab.Monto = pag.MontoLiquidacionFormaDePago.multiply(porcentaje.PorcentajeRetencion.divide(new BigDecimal(100), MathContext.DECIMAL128), MathContext.DECIMAL128);
					cab.Monto = (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1 && !cte.IdMonedaLiquidacion.equalsIgnoreCase(pag.IdMoneda) && "CLP".equalsIgnoreCase(pag.IdMoneda)) ?  MonedaFormato.formatear(conn, cab.Monto, MonedaFormato.MONTO, "CLP").valor : MonedaFormato.formatear(conn, cab.Monto, MonedaFormato.MONTO, cab.IdMoneda).valor;
					cab.IdEjecutivo = cte.IdEmpleadoEjecutivoPortafolio;
					cab.IdSucursal = cte.IdSucursalEmpleadoEjecutivoPortafolio;
					cab.IdTipoProducto = cte.IdTipoProducto;
					cab.IdTipoOperacion = cte.IdTipoOperacion;
					cab.IdOperacion = cte.IdComprobante;
					
					if ("PE".equalsIgnoreCase(localizacionSistema)) {
						cab.FechaOperacion = Fecha.toStringDate(cte.FechaComprobante);				
					}else{
						cab.FechaOperacion = "0001-01-01";	
					}
					
					cab.FechaLiquidacionOperacion = "0001-01-01";
					cab.FechaVencimientoOperacion = Fecha.toStringDatetime(cte.FechaVencimiento);
					cab.IdMonedaOperacion = cte.IdMoneda;
					cab.MontoOperacion = cab.Monto;
					cab.TipoCambioOperacion = BigDecimal.ZERO;
					cab.IdContraparte = 0L;
					cab.IdTipoComprobante = cte.IdTipoComprobante;
					cab.IdComprobante = cte.IdComprobante;
					cab.FolioComprobante = cte.FolioComprobante+"";
					cab.IdComprobanteDetalle = 0L;
					cab.IdComprobanteFormaDePago = 0L;
					
					cab.IdTipoMovimientoAsociado = cte.IdTipoComprobante;
					cab.IdMovimientoAsociado = cte.IdComprobante;
					cab.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);
					
					if (cab.IdTipoMovimientoAsociado == null) {
						cab.IdTipoMovimientoAsociado = "";
					}
					
					if (cab.IdMovimientoAsociado == null ) {
						cab.IdMovimientoAsociado = 0L;
					}
					
					cab.IdPersonaPagoCobro = 0L;
					cab.IdInstrumento = "SIM".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "PRT".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "EMP".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "RRV".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: "RRF".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.IdInstrumentoSubyacente: cte.IdInstrumento;
					cab.IdTipoInstrumento = cte.IdTipoInstrumento;
					
					cab.Nemo = "SIM".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "PRT".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "EMP".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "RRV".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: "RRF".equalsIgnoreCase(cte.IdTipoInstrumento)? cte.NemotecnicoSubyacente: cte.NemotecnicoInstrumento; 
					cab.EsIndicadorBilateral = "N";
					cab.EsCargoAbonoPorTraspaso = "N";
					cab.IdPortafolioTraspaso = 0L;
					//no fue a tesoreria
					if ("".equalsIgnoreCase(cab.IdTipoMovimientoAsociado)) {
						cab.GeneroContraparteEnTesoreria = "N";
					}
					else {
						cab.GeneroContraparteEnTesoreria = "S";
					}
					cab.CertificadoAutomatico = "S";
					listCargoAbono.add(cab);
					
				}
			}
		}
		
		if (listCargoAbono.size() > 0) {
			CrearCargoAbono serviceCrearCargoAbono = new CrearCargoAbono();
			CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
			entradaCrearCargoAbono.p_CargoAbonoAGenerar = listCargoAbono;
			entradaCrearCargoAbono.p_EsIngresoManual = "N";
			

			if ("PE".equalsIgnoreCase(localizacionSistema)) {
				entradaCrearCargoAbono.p_FechaSistema =  ((ObjCargoAbonoAGenerar) listCargoAbono.get(0)).FechaOperacion;
			}else {
				entradaCrearCargoAbono.p_FechaSistema = fechaSistema;	
			}			
			
			entradaCrearCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdModulo = "FAC";
			entradaCrearCargoAbono.p_IdUsuario = in.p_IdUsuario;
			entradaCrearCargoAbono.p_simularOperacion = in.p_simularOperacion;
			entradaCrearCargoAbono.p_UsaReglasNegocio = "S";
			CrearCargoAbonoSalida salidaCrearCargoAbono = serviceCrearCargoAbono.logica(conn, entradaCrearCargoAbono);
			out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
						
			for (MensajeSalida msj : salidaCrearCargoAbono.P_OUT_MENSAJES) {
				for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
					if (msj.tipo == MensajeSalida.MENSAJE_ADVERTENCIA) {
						cte.advertencias++;
					}
					if (msj.tipo == MensajeSalida.MENSAJE_AUTORIZACION) {
						cte.autorizaciones++;
					}
					if (msj.tipo == MensajeSalida.MENSAJE_ERROR) {
						cte.errores++;
					}
				}
			}
			
			for (ObjCargoAbonoGenerado f : salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO) {
				MovimientoCtaInversion_Salida e = new MovimientoCtaInversion_Salida();
				e.IdCargoAbonoMovimiento = f.IdCargoAbonoMovimiento;
				e.IdTipoMovimiento = f.IdTipoMovimiento;
				e.IdComprobante = f.NumeroCargoAbono;
				out.P_OUT_CARGOSABONOSCTAINVERSION.add(e);
			}
			
			limpiaComprobantes(hashComprobanteLlave,hashComprobanteLlaveErrores);
			
			if (hashComprobanteLlave.size() == 0) {
				return out;
			}
			
		}
		
		/***** Fin Cuenta Inversion ****/
		
		
	
		/***** Tesoreria ****/		
		List<IngresoEgreso_Entrada> listIngresosEgresos = new ArrayList<>();
		List<FormaDePago_Entrada> listFormasDePago = new ArrayList<>();
		Long indexFormaDePago = 0L;
		Integer indexSecuenciaAPVB = 0;
		//Facturas
		//COMPROBANTES SPOT AFECTA TESORERIA
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			if (!"MEME".equalsIgnoreCase(cte.IdTipoProducto)) {
				continue;
			}
			
			///Para NCredito generara la anulacion del movimiento en tesoreria no movimiento de contrapartida
			if (tiposNotaCredito.contains(cte.IdTipoComprobante) || tiposNotaCreditoEsp.contains(cte.IdTipoComprobante) ) {
				continue;
			}
			
			InfoMovimientoSpot_Proceso infoMovimientoSpotProceso = null;
			
			if(cte.comprobanteDetalles.get(0) != null) {
				Long idSpot = cte.comprobanteDetalles.get(0).IdTransaccion;
				infoMovimientoSpotProceso = hashInfoMovimientosSpot.get(idSpot);
			}
			
			//Si el spot esta asociado a un Traspaso, el egreso debe quedar para la entidad recaudadora
			if(infoMovimientoSpotProceso != null && infoMovimientoSpotProceso.IdPersona != null && ("RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo) || "RESTRASP".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo))) {
				IngresoEgreso_Entrada ing = new IngresoEgreso_Entrada();
				ing.NumeroReferencia = cte.IdComprobante * -1;
				ing.IdTipoMovimiento = "MOVEGR";
				ing.IdTipoPersona = "CNT";
				ing.IdPersona = infoMovimientoSpotProceso.IdPersona;
				ing.IdPortafolio = 0L;
				ing.IdMoneda = cte.IdMoneda;
				ing.Monto = cte.MontoComprobanteFinal.abs();
				ing.FechaLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
				ing.IdConcepto = cte.IdConcepto;
				ing.IdTipoPersonaTitularPago = "CLI";
				ing.IdPersonaTitularPago = cte.IdPersona;
				ing.IdPortafolioTitularPago = cte.IdPortafolio;
				ing.IdCaja = 0L;
				ing.SecuenciaAgrupacion = "";
				ing.SecuenciaRelacionada = cte.IdComprobante;
				ing.Glosa = "";
				ing.IdEmpleadoDigitador = cte.IdEmpleadoEjecutivoPortafolio;
				ing.IdSucursalEmpleadoDigitador = cte.IdSucursalEmpleadoEjecutivoPortafolio;
				ing.IdTipoProducto = cte.IdTipoProducto;
				ing.IdTipoOperacion = cte.IdTipoOperacion;
				ing.IdTipoComprobante = cte.IdTipoComprobante;
				ing.IdComprobante = cte.IdComprobante;
				ing.FechaComprobante = fechaSistema;
				ing.FechaLiquidacionComprobante = Fecha.toStringDate(cte.FechaLiquidacion);
				ing.IdOperacion = cte.IdComprobante;
				ing.FechaOperacion = "0001-01-01";
				ing.IdCondicionLiquidacion = "";
				ing.IdBolsa = "";
				ing.IdMonedaOperacion = "";
				ing.MontoOperacion = cte.MontoComprobanteFinal.abs();
				ing.IdTipoReajuste = "";
				ing.FechaParidad = "0001-01-01";
				ing.ValorParidad = BigDecimal.ZERO;
				ing.FolioCartera = 0L;
				ing.IdInstrumento = (cte.IdInstrumento == null ? 0 : cte.IdInstrumento);
				ing.IdTipoInstrumento = (cte.IdTipoInstrumento == null ? "" : cte.IdTipoInstrumento);
				ing.IdClasificacionTipoInstrumento = (cte.IdClasificacionTipoInstrumento == null ? "" : cte.IdClasificacionTipoInstrumento);
				ing.Bilateral = "N";
				ing.GeneraComprobante = "N";
				ing.AgruparEnComprobante = "S";
				ing.MueveCuentaInversion = "S";
				ing.TitularPago = "N";
				
				ing.IdTipoCanalIngreso = infoMovimientoSpotProceso != null ? infoMovimientoSpotProceso.IdTipoCanalIngreso : "MES" ; 
				
				listIngresosEgresos.add(ing);
				
				FormaDePago_Entrada fpag = new FormaDePago_Entrada();
				
				fpag.NumeroReferencia = ing.NumeroReferencia;
				fpag.NumeroReferenciaFormaDePago = indexFormaDePago++;
				fpag.IdMedioDePago = "TRA";
				fpag.IdModalidadDePago = hashModalidadPago.get(fpag.IdMedioDePago) != null ? hashModalidadPago.get(fpag.IdMedioDePago).IdModalidadDePago : "MODCAJ";
				fpag.IdMoneda = ing.IdMoneda;
				fpag.Monto = ing.Monto;
				fpag.IdCuentaBancariaCorredora = ("RESTRASP".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo) && infoMovimientoSpotProceso != null && infoMovimientoSpotProceso.IdCuentaBancariaCorredora != null) ? infoMovimientoSpotProceso.IdCuentaBancariaCorredora : 0L;
				fpag.IdTarjetaBancariaCorredora = 0L;
				fpag.IdChequera = 0L;
				fpag.NumeroDocumentoCorredora = 0L;
				fpag.FechaDocumentoPagoCorredora = "0001-01-01";
				fpag.IdCuentaBancariaSocio = infoMovimientoSpotProceso.IdCuentaBancaria;
				fpag.IdBancoSocio = infoMovimientoSpotProceso.IdBanco;
				fpag.NombreCuentaBancariaSocio = "";
				fpag.NumeroCuentaBancariaSocio = infoMovimientoSpotProceso.NumeroCuentaBancaria;
				fpag.IdTipoCuentaBancariaSocio = infoMovimientoSpotProceso.IdTipoCuentaBancaria;
				fpag.NombreTipoCuentaBancariaSocio = infoMovimientoSpotProceso.NombreTipoCuentaBancaria;
				fpag.IdTarjetaBancariaSocio = 0L;
				fpag.IdTipoTarjetaBancariaSocio = "";
				fpag.NombreTipoTarjetaBancariaSocio = "";
				fpag.NumeroTarjetaBancariaSocio = "";
				fpag.NumeroSerieSocio = "";
				fpag.NumeroDocumentoSocio = 0L;
				fpag.FechaDocumentoPagoSocio = ing.FechaLiquidacion;
				listFormasDePago.add(fpag);
				
			}
			
			BigDecimal MontoOperacion = null;
			
			for (ComprobanteFormaDePago_Proceso pag : cte.comprobanteFormasDePago) {
				Long idComprobanteAux= pag.IdComprobante;
				PorcentajeRetencionRegimenAPVB porcentaje= (PorcentajeRetencionRegimenAPVB) hashprocentajeRetencionAPV.get(cte.CodigoPortafolio);
				
				if( "MOVING".equalsIgnoreCase(pag.IdTipoMovimientoSpot) && (infoMovimientoSpotProceso != null && "RESCNN".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo)) ) {
					continue;
				}
				
				if (!SI.equalsIgnoreCase(pag.AfectarTesoreria)) {
					continue;
				}
				
				if (pag.IdMedioDePago == null) {
					continue;
				}
				
				if(infoMovimientoSpotProceso != null && infoMovimientoSpotProceso.IdPersona != null && ("RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo) 
						|| "RESTRASP".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo)) && "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimientoSpot)) {
					continue;
				}
				
				IngresoEgreso_Entrada ing = new IngresoEgreso_Entrada();
				ing.NumeroReferencia = pag.IdComprobanteFormaDePago;
				ing.IdTipoMovimiento = pag.IdTipoMovimientoSpot;
				ing.IdTipoPersona = "CLI";
				ing.IdPersona = cte.IdPersona;
				ing.IdPortafolio = cte.IdPortafolio;
				ing.IdMoneda = pag.IdMoneda;
				if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) && "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) 
						&& tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) || (infoMovimientoSpotProceso != null && ("APV-B".equals(infoMovimientoSpotProceso.IdRegimenTributario) 
						|| "APV-AN".equals(infoMovimientoSpotProceso.IdRegimenTributario) ) && "RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo)
						&& "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimientoSpot) && infoMovimientoSpotProceso.IdPersona == null && !"CIN".equals(pag.IdMedioDePago)))  {
					
					ing.Monto = pag.MontoLiquidacionFormaDePago.abs().multiply((new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128)), MathContext.DECIMAL128);
					ing.Monto = MonedaFormato.formatear(conn, ing.Monto, MonedaFormato.MONTO, ing.IdMoneda).valor;
					MontoOperacion = pag.MontoLiquidacionFormaDePago;
				} else {
					ing.Monto = pag.MontoLiquidacionFormaDePago.abs();
					ing.Monto = pag.MontoLiquidacionFormaDePagoTesoreria != null ? pag.MontoLiquidacionFormaDePagoTesoreria.compareTo(BigDecimal.ZERO) == 1? pag.MontoLiquidacionFormaDePagoTesoreria.abs(): pag.MontoLiquidacionFormaDePago.abs(): pag.MontoLiquidacionFormaDePago.abs();
				}
				ing.FechaLiquidacion = Fecha.toStringDate(pag.FechaDocumentoPago);
				ing.IdConcepto = cte.IdConcepto;
				boolean personaPagoAGF = esPersonaPagoCobroAGF(conn, infoMovimientoSpotProceso, pag);
				//if(infoMovimientoSpotProceso != null && "BF".equals(infoMovimientoSpotProceso.IdRegimenTributario) && ("RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo) || "RESTRASP".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo)) && "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimientoSpot)) {
				if(infoMovimientoSpotProceso != null && personaPagoAGF) {	
					ing.IdTipoPersonaTitularPago = "PER";
					ing.IdPersonaTitularPago = personaTGR.IdPersona;
					ing.IdPortafolioTitularPago = 0L;
					if("RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo)) {
						ing.IdPersonaTitularPago = cte.IdPersona;
						ing.IdPersona = personaTGR.IdPersona;
					}
				}else {
					ing.IdTipoPersonaTitularPago = "CLI";
					ing.IdPersonaTitularPago = cte.IdPersona;
					ing.IdPortafolioTitularPago = cte.IdPortafolio;
				}
				
				boolean personaPagoAGFNN = esPersonaPagoCobroAGFNN(conn, infoMovimientoSpotProceso, pag);
				if(infoMovimientoSpotProceso != null && personaPagoAGFNN) {
					ing.IdPersonaTitularPago = infoMovimientoSpotProceso.IdPersonaEmisor;
					ing.IdPortafolioTitularPago = 0L;
					ing.IdTipoPersonaTitularPago = "PER";
				}
							
				ing.IdCaja = 0L;
				ing.SecuenciaAgrupacion = "";
				ing.SecuenciaRelacionada = cte.IdComprobante;
				ing.Glosa = cte.Glosa;
				ing.IdEmpleadoDigitador = cte.IdEmpleadoEjecutivoPortafolio;
				ing.IdSucursalEmpleadoDigitador = cte.IdSucursalEmpleadoEjecutivoPortafolio;
				ing.IdTipoProducto = cte.IdTipoProducto;
				ing.IdTipoOperacion = cte.IdTipoOperacion;
				ing.IdTipoComprobante = cte.IdTipoComprobante;
				ing.IdComprobante = cte.IdComprobante;
						
				if ("PE".equalsIgnoreCase(localizacionSistema)){
					ing.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);				
				}else {
					ing.FechaComprobante = fechaSistema;
				}
				
				ing.FechaLiquidacionComprobante = Fecha.toStringDate(cte.FechaLiquidacion);
				ing.IdOperacion = cte.IdComprobante;
				ing.FechaOperacion = "0001-01-01";
				ing.IdCondicionLiquidacion = "";
				ing.IdBolsa = "";
				ing.IdMonedaOperacion = "";
				ing.MontoOperacion = ing.Monto;
				ing.IdTipoReajuste = "";
				ing.FechaParidad = "0001-01-01";
				ing.ValorParidad = BigDecimal.ZERO;
				ing.FolioCartera = 0L;
				ing.IdInstrumento = (cte.IdInstrumento == null ? 0 : cte.IdInstrumento);
				ing.IdTipoInstrumento = (cte.IdTipoInstrumento == null ? "" : cte.IdTipoInstrumento);
				ing.IdClasificacionTipoInstrumento = (cte.IdClasificacionTipoInstrumento == null ? "" : cte.IdClasificacionTipoInstrumento);
				ing.Bilateral = "N";
				ing.GeneraComprobante = "N";
				ing.AgruparEnComprobante = "S";
				ing.MueveCuentaInversion = "S";
				ing.TitularPago = "N";
				
				ing.IdTipoCanalIngreso = infoMovimientoSpotProceso != null ? infoMovimientoSpotProceso.IdTipoCanalIngreso : (cte.IdTipoCanalIngreso!="DAT"?  cte.IdTipoCanalIngreso:"MES"); 
				
				listIngresosEgresos.add(ing);
				
				FormaDePago_Entrada fpag = new FormaDePago_Entrada();
				
				fpag.NumeroReferencia = ing.NumeroReferencia;
				fpag.NumeroReferenciaFormaDePago = indexFormaDePago++;
				fpag.IdMedioDePago = pag.IdMedioDePago;
				fpag.IdModalidadDePago = pag.IdModalidadDePago;
				fpag.IdMoneda = pag.IdMoneda;
				if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) && "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) 
						&& tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) || (infoMovimientoSpotProceso != null 
						&& ("APV-B".equals(infoMovimientoSpotProceso.IdRegimenTributario) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) && "RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo) 
						&& "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimientoSpot) && infoMovimientoSpotProceso.IdPersona == null)){
					fpag.Monto = pag.MontoLiquidacionFormaDePago.multiply((new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128)), MathContext.DECIMAL128);
					fpag.Monto = MonedaFormato.formatear(conn, fpag.Monto, MonedaFormato.MONTO, fpag.IdMoneda).valor;
				} else {
					fpag.Monto = pag.MontoLiquidacionFormaDePago;
					fpag.Monto = pag.MontoLiquidacionFormaDePagoTesoreria != null ? pag.MontoLiquidacionFormaDePagoTesoreria.compareTo(BigDecimal.ZERO) == 1? pag.MontoLiquidacionFormaDePagoTesoreria.abs(): pag.MontoLiquidacionFormaDePago.abs(): pag.MontoLiquidacionFormaDePago.abs();
				}
				fpag.FechaDocumentoPagoCorredora = "0001-01-01";
				
				if(esFormaPagoSpotCorredora(cte.IdConcepto,pag.IdTipoMovimientoSpot)) {
					fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
					fpag.IdTarjetaBancariaCorredora = pag.IdTarjetaBancaria;
					fpag.IdChequera = 0L;
					fpag.NumeroDocumentoCorredora = pag.NumeroDocumento;
					fpag.FechaDocumentoPagoCorredora = Fecha.toStringDate(pag.FechaDocumentoPago);
				}
				else
				{
				fpag.IdCuentaBancariaCorredora = 0L;
				fpag.IdTarjetaBancariaCorredora = 0L;
				fpag.IdChequera = 0L;
				fpag.NumeroDocumentoCorredora = 0L;
				fpag.FechaDocumentoPagoCorredora = "0001-01-01";
				}
				if(infoMovimientoSpotProceso != null && "BF".equals(infoMovimientoSpotProceso.IdRegimenTributario) && ("RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo) 
						|| "RESTRASP".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo)) && "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimientoSpot)) {
					fpag.IdCuentaBancariaSocio = personaTGR.IdCuentaBancaria;
					fpag.IdBancoSocio = personaTGR.IdBanco;
					fpag.NombreCuentaBancariaSocio = "";
					fpag.NumeroCuentaBancariaSocio = personaTGR.NumeroCuentaBancaria;
					fpag.IdTipoCuentaBancariaSocio = personaTGR.IdTipoCuentaBancaria;
					fpag.NombreTipoCuentaBancariaSocio = personaTGR.NombreTipoCuentaBancaria;
					fpag.IdTarjetaBancariaSocio = 0L;
					fpag.IdTipoTarjetaBancariaSocio = "";
					fpag.NombreTipoTarjetaBancariaSocio = "";
					fpag.NumeroTarjetaBancariaSocio = "";
					fpag.NumeroSerieSocio = "";
					fpag.NumeroDocumentoSocio = 0L;
					fpag.FechaDocumentoPagoSocio = Fecha.toStringDate(pag.FechaDocumentoPago);
				}
				else {
					if(esFormaPagoSpotCorredora(cte.IdConcepto,pag.IdTipoMovimientoSpot)) {
						fpag.IdCuentaBancariaSocio = 0L;
						fpag.IdBancoSocio = 0L;
						fpag.NombreCuentaBancariaSocio = "";
						fpag.NumeroCuentaBancariaSocio = "";
						fpag.IdTipoCuentaBancariaSocio = "";
						fpag.NombreTipoCuentaBancariaSocio = "";
						fpag.IdTarjetaBancariaSocio = 0L;
						fpag.IdTipoTarjetaBancariaSocio = "";
						fpag.NombreTipoTarjetaBancariaSocio = "";
						fpag.NumeroTarjetaBancariaSocio = "";
						fpag.NumeroSerieSocio = "";
						fpag.NumeroDocumentoSocio = 0L;
						fpag.FechaDocumentoPagoSocio = "0001-01-01";
					}
					else {
						fpag.IdCuentaBancariaSocio = pag.IdCuentaBancaria;
						fpag.IdBancoSocio = pag.IdBanco;
						fpag.NombreCuentaBancariaSocio = "";
						fpag.NumeroCuentaBancariaSocio = pag.NumeroCuentaBancaria;
						fpag.IdTipoCuentaBancariaSocio = pag.IdTipoCuentaBancaria;
						fpag.NombreTipoCuentaBancariaSocio = pag.NombreTipoCuentaBancaria;
						fpag.IdTarjetaBancariaSocio = pag.IdTarjetaBancaria;
						fpag.IdTipoTarjetaBancariaSocio = pag.IdTipoTarjetaBancaria;
						fpag.NombreTipoTarjetaBancariaSocio = pag.NombreTipoTarjetaBancaria;
						fpag.NumeroTarjetaBancariaSocio = pag.NumeroTarjetaBancaria;
						fpag.NumeroSerieSocio = pag.NumeroSerie;
						fpag.NumeroDocumentoSocio = pag.NumeroDocumento;
						fpag.FechaDocumentoPagoSocio = Fecha.toStringDate(pag.FechaDocumentoPago);
					}
				}
				
				listFormasDePago.add(fpag);
			
				//SI ES UN RESCATE CON REGIMEN APV-B SE ENVIA A TESORERIA LA RETENCION COMO PAGO AL SII
				if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) 
						&& "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) 
						&& tiposFactura.contains(cte.IdTipoComprobante) && cte.Accion.equalsIgnoreCase("VENTA") || (infoMovimientoSpotProceso != null 
						&& ("APV-B".equals(infoMovimientoSpotProceso.IdRegimenTributario) ||"APV-AN".equals(infoMovimientoSpotProceso.IdRegimenTributario)) 
						&& "RESNORM".equals(infoMovimientoSpotProceso.IdTipoMovimientoFondoMutuo)
						&& "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimientoSpot) && infoMovimientoSpotProceso.IdPersona == null &&  !"CIN".equals(pag.IdMedioDePago))){
				    
					IngresoEgreso_Entrada ing2 = new IngresoEgreso_Entrada();
					
					ing2.NumeroReferencia = secuenciasComprobanteFormaDePagoAPVB.get(indexSecuenciaAPVB++);
					
					ing2.IdTipoMovimiento = pag.IdTipoMovimientoSpot;
					ing2.IdTipoPersona =   _c.TipoPersonaContraparte; 
					ing2.IdPersona =        personaSII.IdPersona;
					ing2.IdPortafolio = 0L;
					ing2.IdMoneda = pag.IdMoneda;
					ing2.Monto = pag.MontoLiquidacionFormaDePago.abs().multiply(porcentaje.PorcentajeRetencion.divide(new BigDecimal(100), MathContext.DECIMAL128), MathContext.DECIMAL128);
					ing2.Monto = MonedaFormato.formatear(conn, ing2.Monto, MonedaFormato.MONTO, ing2.IdMoneda).valor;
					
					ing2.FechaLiquidacion = Fecha.toStringDate(pag.FechaDocumentoPago);
					//ing2.IdConcepto = cte.IdConcepto;
					ing2.IdConcepto = "CAREAB";
					
					ing2.IdTipoPersonaTitularPago = _c.TipoPersonaCliente;
					ing2.IdPersonaTitularPago = cte.IdPersona;
					ing2.IdPortafolioTitularPago = cte.IdPortafolio;
					
					ing2.IdCaja = 0L;
					ing2.SecuenciaAgrupacion = "";
					ing2.SecuenciaRelacionada = cte.IdComprobante;
					ing2.Glosa = "";
					ing2.IdEmpleadoDigitador = cte.IdEmpleadoEjecutivoPortafolio;
					ing2.IdSucursalEmpleadoDigitador = cte.IdSucursalEmpleadoEjecutivoPortafolio;
					ing2.IdTipoProducto = cte.IdTipoProducto;
					ing2.IdTipoOperacion = cte.IdTipoOperacion;
					ing2.IdTipoComprobante = cte.IdTipoComprobante;
					ing2.IdComprobante = cte.IdComprobante;
							
					if ("PE".equalsIgnoreCase(localizacionSistema))
						ing2.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);
					else
						ing2.FechaComprobante = fechaSistema;
					
					ing2.FechaLiquidacionComprobante = Fecha.toStringDate(cte.FechaLiquidacion);
					ing2.IdOperacion = cte.IdComprobante;
					ing2.FechaOperacion = "0001-01-01";
					ing2.IdCondicionLiquidacion = "";
					ing2.IdBolsa = "";
					ing2.IdMonedaOperacion = "";
					ing2.MontoOperacion = ing.Monto;
					ing2.IdTipoReajuste = "";
					ing2.FechaParidad = "0001-01-01";
					ing2.ValorParidad = BigDecimal.ZERO;
					ing2.FolioCartera = 0L;
					ing2.IdInstrumento = (cte.IdInstrumento == null ? 0 : cte.IdInstrumento);
					ing2.IdTipoInstrumento = (cte.IdTipoInstrumento == null ? "" : cte.IdTipoInstrumento);
					ing2.IdClasificacionTipoInstrumento = (cte.IdClasificacionTipoInstrumento == null ? "" : cte.IdClasificacionTipoInstrumento);
					ing2.Bilateral = "N";
					ing2.GeneraComprobante = "N";
					ing2.AgruparEnComprobante = "S";
					ing2.MueveCuentaInversion = "N";
					ing2.TitularPago = "N";
					
					ing2.IdTipoCanalIngreso = infoMovimientoSpotProceso != null ? infoMovimientoSpotProceso.IdTipoCanalIngreso : "MES" ; 
					
					listIngresosEgresos.add(ing2);
					
					List<IngresoEgreso_Entrada> filtrados = new ArrayList<IngresoEgreso_Entrada>();
					BigDecimal Monto85, Monto15, SumaPorcentajes = null;
					Long NumeroDeReferencia = null;
                    //buscamos los objetos
                    for(IngresoEgreso_Entrada objIngresoEgreso: listIngresosEgresos ) {
                        if(objIngresoEgreso.IdComprobante.equals(idComprobanteAux)) {
                            filtrados.add(objIngresoEgreso);
                        }
                    }
                    //verificamos monto mayor para identificar cual es el 85%.
                    if(filtrados.get(0).Monto.intValue() > filtrados.get(1).Monto.intValue()) {
                        Monto85=filtrados.get(0).Monto;
                        Monto15=filtrados.get(1).Monto;
                        NumeroDeReferencia = filtrados.get(0).NumeroReferencia;
					}else {
                        Monto85=filtrados.get(1).Monto;
                        Monto15=filtrados.get(0).Monto;
                        NumeroDeReferencia = filtrados.get(1).NumeroReferencia;
                    }
                    
                    SumaPorcentajes = Monto85.add(Monto15);
                    
                      //aki
                    //Verificamos que el valor de SumaPorcentajes coincida con el valor de MontoOperacion, de no ser asi corregimos el Monto85.
                    if(!SumaPorcentajes.equals(MontoOperacion)) {
                    	//Si SumaPorcentajes es mayor a MontoOperacion,se le resta 1 a Monto85
                        if(SumaPorcentajes.intValue() > MontoOperacion.intValue() )
                        {
                        	Monto85 = Monto85.subtract(new BigDecimal(1));
                        }
                        //Si SumaPorcentajes es menor a MontoOperacion,se le suma 1 a Monto85
                        else{
                        	Monto85 = Monto85.add(new BigDecimal(1));
                        }
                        
                        for(IngresoEgreso_Entrada objList : listIngresosEgresos) {
                            if(objList.NumeroReferencia.equals(NumeroDeReferencia)) {
                                objList.Monto = Monto85;
							}
                        }
                    }
					filtrados= null;
					
					FormaDePago_Entrada fpag2 = new FormaDePago_Entrada();
					
					fpag2.NumeroReferencia = ing2.NumeroReferencia;
					fpag2.NumeroReferenciaFormaDePago = indexFormaDePago++;
					fpag2.IdMedioDePago = pag.IdMedioDePago;
					fpag2.IdModalidadDePago = pag.IdModalidadDePago;
					fpag2.IdMoneda = pag.IdMoneda;
					fpag2.Monto = pag.MontoLiquidacionFormaDePago.multiply(porcentaje.PorcentajeRetencion.divide(new BigDecimal(100), MathContext.DECIMAL128), MathContext.DECIMAL128);
					fpag2.Monto = MonedaFormato.formatear(conn, fpag2.Monto, MonedaFormato.MONTO, fpag2.IdMoneda).valor;
					fpag2.FechaDocumentoPagoCorredora = "0001-01-01";
					fpag2.IdCuentaBancariaCorredora = 0L;
					fpag2.IdTarjetaBancariaCorredora = 0L;
					fpag2.IdChequera = 0L;
					fpag2.NumeroDocumentoCorredora = 0L;
					fpag2.FechaDocumentoPagoCorredora = "0001-01-01";
					fpag2.IdCuentaBancariaSocio = personaSII.IdCuentaBancaria;
					fpag2.IdBancoSocio = personaSII.IdBanco;
					fpag2.NombreCuentaBancariaSocio = "";
					fpag2.NumeroCuentaBancariaSocio = personaSII.NumeroCuentaBancaria;
					fpag2.IdTipoCuentaBancariaSocio = personaSII.IdTipoCuentaBancaria;
					fpag2.NombreTipoCuentaBancariaSocio = personaSII.NombreTipoCuentaBancaria;
					fpag2.IdTarjetaBancariaSocio = 0L;
					fpag2.IdTipoTarjetaBancariaSocio = "";
					fpag2.NombreTipoTarjetaBancariaSocio = "";
					fpag2.NumeroTarjetaBancariaSocio = "";
					fpag2.NumeroSerieSocio = "";
					fpag2.NumeroDocumentoSocio = 0L;
					fpag2.FechaDocumentoPagoSocio = Fecha.toStringDate(pag.FechaDocumentoPago);
					
					listFormasDePago.add(fpag2);
					
					//buscamos los objetos y seteamos montos segun NumeroReferencia
					for(FormaDePago_Entrada objFormasPago: listFormasDePago ) {
						for(IngresoEgreso_Entrada objIngresoEgreso: listIngresosEgresos ) {
							if(objFormasPago.NumeroReferencia.equals(objIngresoEgreso.NumeroReferencia)) {
								objFormasPago.Monto = objIngresoEgreso.Monto;
							}
						}
					}
					
				}
			}
		}
		
		//Movimiento Tesoreria para actualizar monto de las Notas de credito		
		HashMap<String,Object> paramsNC = new HashMap<>();
		List<Long> idsComprobanteRef = hashComprobanteLlave.values().stream().map(s -> s.IdComprobanteReferencia).collect(Collectors.toList()); 
		paramsNC.put("idsComprobante", idsComprobanteRef);
		
		
		
		
		List<MovimientoTesoreria_Proceso> movimientosTesoreriaNC = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_TESORERIA, paramsNC, MovimientoTesoreria_Proceso.class);
		HashMap<Long,MovimientoTesoreria_Proceso> hasMovimientosTesoreriaNC = new HashMap<>();
		for(MovimientoTesoreria_Proceso mov : movimientosTesoreriaNC) {
			hasMovimientosTesoreriaNC.put(mov.IdComprobanteOrigen, mov);
		}
		
		
		
		for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
			
			if (!SI.equalsIgnoreCase(cte.ActualizarTesoreria)) {
				continue;
			}
			// Agrupar movimientos por IdComprobanteOrigen para acceso eficiente
			Map<Long, List<MovimientoTesoreria_Proceso>> movTesoreriaNCPorOrigen = movimientosTesoreriaNC.stream().collect(Collectors.groupingBy(m -> m.IdComprobanteOrigen));				    
				        
			//EN NOTAS DE CREDITO PARA OPERACIONES SPOT SE UTILIZA CICLO PARA LOS DISTINTOS COMPROBANTES EXISTENTES EN TESORERIA
			if(tiposNotaCredito.contains(cte.IdTipoComprobante) && "MEME".equalsIgnoreCase(cte.IdTipoProducto)) {
				List<MovimientoTesoreria_Proceso> movimientosTesoreriaSpotNC = movTesoreriaNCPorOrigen.getOrDefault(cte.IdComprobanteReferencia, Collections.emptyList());
				        
				for (MovimientoTesoreria_Proceso movTesNC : movimientosTesoreriaSpotNC) {
					IngresoEgreso_Entrada ing = new IngresoEgreso_Entrada();
					ing.NumeroReferencia = movTesNC.Id;
					
					if (   (tiposFactura.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) 
						|| (tiposNotaDebito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion))) {
						ing.IdTipoMovimiento = "MOVING";
					}
					else if ((tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == 1) 
						 || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == 1) ){
						ing.IdTipoMovimiento = "MOVEGR";
					}
					else if ((tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == -1) 
					     || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == -1) ) {
						ing.IdTipoMovimiento = "MOVING";
					}
					else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) {
						ing.IdTipoMovimiento = ("VENSPT".equalsIgnoreCase(movTesNC.IdTipoOperacion) && "MOVING".equalsIgnoreCase(movTesNC.IdTipoMovimiento)) ? "MOVEGR" : "MOVING";
					}
					else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)) {
						ing.IdTipoMovimiento = ("COMSPT".equalsIgnoreCase(movTesNC.IdTipoOperacion) && "MOVEGR".equalsIgnoreCase(movTesNC.IdTipoMovimiento)) ? "MOVING" : "MOVEGR";
					}
					else if (tiposNotaCreditoEsp.contains(cte.IdTipoComprobante)) {
						ing.IdTipoMovimiento = "MOVEGR";
					}
					
					PorcentajeRetencionRegimenAPVB porcentaje= (PorcentajeRetencionRegimenAPVB) hashprocentajeRetencionAPV.get(cte.CodigoPortafolio);
						
					ing.IdTipoPersona = "CLI";
					ing.IdPersona = cte.IdPersona;
					ing.IdPortafolio = cte.IdPortafolio;
					ing.IdMoneda = movTesNC.IdMoneda;
					if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) 
							&& "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) && tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)){
						ing.Monto = cte.MontoLiquidacionFinal.abs().multiply(new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100) ,MathContext.DECIMAL128).abs(), MathContext.DECIMAL128);
						ing.Monto = MonedaFormato.formatear(conn, ing.Monto, MonedaFormato.MONTO, ing.IdMoneda).valor;
					} else if (tiposNotaCredito.contains(cte.IdTipoComprobante)) {
						ing.Monto = movTesNC.Monto;
					} else {
						ing.Monto = cte.MontoLiquidacionFinal.abs();
					}
					ing.FechaLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
					ing.IdConcepto = cte.IdConcepto;
					ing.IdTipoPersonaTitularPago = "CLI";
					ing.IdPersonaTitularPago = cte.IdPersona;
					ing.IdPortafolioTitularPago = cte.IdPortafolio;
					ing.IdCaja = 0L;
					ing.SecuenciaAgrupacion = "";
					ing.SecuenciaRelacionada = cte.IdComprobante;
					ing.Glosa = "";
					ing.IdEmpleadoDigitador = cte.IdEmpleadoEjecutivoPortafolio;
					ing.IdSucursalEmpleadoDigitador = cte.IdSucursalEmpleadoEjecutivoPortafolio;
					ing.IdTipoProducto = cte.IdTipoProducto;
					ing.IdTipoOperacion = cte.IdTipoOperacion;
					ing.IdTipoComprobante = cte.IdTipoComprobante;
					ing.IdComprobante = cte.IdComprobante;
					ing.IdTipoCanalIngreso = cte.IdTipoCanalIngreso; 
					
					if ("PE".equalsIgnoreCase(localizacionSistema)){
						ing.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);				
					}else {
						ing.FechaComprobante = fechaSistema;
					}
					
					ing.FechaLiquidacionComprobante = Fecha.toStringDate(cte.FechaLiquidacion);
					ing.IdOperacion = cte.IdComprobante;
					ing.FechaOperacion = "0001-01-01";
					ing.IdCondicionLiquidacion = cte.IdCondicionLiquidacion;
					ing.IdBolsa = "";
					ing.IdMonedaOperacion = "";
					ing.MontoOperacion = ing.Monto;
					ing.IdTipoReajuste = "";
					ing.FechaParidad = "0001-01-01";
					ing.ValorParidad = BigDecimal.ZERO;
					ing.FolioCartera = 0L;
					ing.IdInstrumento = (cte.IdInstrumento == null ? 0 : cte.IdInstrumento);
					ing.IdTipoInstrumento = (cte.IdTipoInstrumento == null ? "" : cte.IdTipoInstrumento);
					ing.IdClasificacionTipoInstrumento = (cte.IdClasificacionTipoInstrumento == null ? "" : cte.IdClasificacionTipoInstrumento);
					ing.Bilateral = "N";
					
					
					ing.GeneraComprobante = in.p_GeneraComprobanteTesoreria;
					if(in.p_GeneraComprobanteTesoreria == null) {
						//mas abajo se revalida la asignacion de este campo de acuerdo a los montos enviados a tesoreria
						ing.GeneraComprobante = (cte.comprobanteFormasDePago.size() > 0) ? (genCbmteTeso == null || "".equalsIgnoreCase(genCbmteTeso)) ? "N" : genCbmteTeso : "N";
					}
					
					
					
					ing.AgruparEnComprobante = "S";
					ing.MueveCuentaInversion = "S";
					ing.TitularPago = "N";
		
					
					listIngresosEgresos.add(ing);
							
					for (ComprobanteFormaDePago_Proceso pag : cte.comprobanteFormasDePago) {
						
						if(!ing.IdMoneda.equalsIgnoreCase(pag.IdMoneda)) {
							continue;
						}
						
						FormaDePago_Entrada fpag = new FormaDePago_Entrada();
						
						fpag.NumeroReferencia = ing.NumeroReferencia;
						fpag.NumeroReferenciaFormaDePago = indexFormaDePago++;
						fpag.IdMedioDePago = pag.IdMedioDePago;
						fpag.IdModalidadDePago = pag.IdModalidadDePago;
						fpag.IdMoneda = pag.IdMoneda;
						if ("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) && "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) && tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)){
							fpag.Monto = pag.MontoLiquidacionFormaDePago.multiply((new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128)), MathContext.DECIMAL128);
							fpag.Monto = MonedaFormato.formatear(conn, fpag.Monto, MonedaFormato.MONTO, fpag.IdMoneda).valor;
						} else if (tiposNotaCredito.contains(cte.IdTipoComprobante)) {
							fpag.Monto = movTesNC.Monto;
						}else {
							fpag.Monto = pag.MontoLiquidacionFormaDePago;
						}
		
						if ((tiposFactura.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) ) {
							fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
							fpag.IdTarjetaBancariaCorredora = pag.IdTarjetaBancaria;
							fpag.NumeroDocumentoCorredora = pag.NumeroDocumento;
							fpag.FechaDocumentoPagoCorredora = Fecha.toStringDate(pag.FechaDocumentoPago);
						}
						else if (tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && (pag.MontoLiquidacionFormaDePago.compareTo(cte.MontoLiquidacionCobro) == -1)) {
							fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
							fpag.IdTarjetaBancariaCorredora = pag.IdTarjetaBancaria;
							fpag.NumeroDocumentoCorredora = pag.NumeroDocumento;
							fpag.FechaDocumentoPagoCorredora = Fecha.toStringDate(pag.FechaDocumentoPago);
						}
						else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && !"MEME".equalsIgnoreCase(cte.IdTipoProducto) && !"INSI".equalsIgnoreCase(cte.IdTipoProducto)) {
							fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
							fpag.IdTarjetaBancariaCorredora = pag.IdTarjetaBancaria;
							fpag.NumeroDocumentoCorredora = pag.NumeroDocumento;
							fpag.FechaDocumentoPagoCorredora = Fecha.toStringDate(pag.FechaDocumentoPago);
						}
						else {
							fpag.IdCuentaBancariaCorredora = 0L;
							fpag.IdTarjetaBancariaCorredora = 0L;
							fpag.NumeroDocumentoCorredora = 0L;
							fpag.FechaDocumentoPagoCorredora = "0001-01-01";
						}
						
						if (tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && (pag.MontoLiquidacionFormaDePago.compareTo(cte.MontoLiquidacionCobro) == 1) ) {
							fpag.IdCuentaBancariaSocio = pag.IdCuentaBancaria;
							fpag.IdBancoSocio = pag.IdBanco;
							fpag.NumeroCuentaBancariaSocio = pag.NumeroCuentaBancaria;
							fpag.IdTipoCuentaBancariaSocio = pag.IdTipoCuentaBancaria;
							fpag.NombreTipoCuentaBancariaSocio = pag.NombreTipoCuentaBancaria;
							fpag.IdTarjetaBancariaSocio = pag.IdTarjetaBancaria;
							fpag.IdTipoTarjetaBancariaSocio = pag.IdTipoTarjetaBancaria;
							fpag.NombreTipoTarjetaBancariaSocio = pag.NombreTipoTarjetaBancaria;
							fpag.NumeroTarjetaBancariaSocio = pag.NumeroTarjetaBancaria;
							fpag.NumeroSerieSocio = pag.NumeroSerie;
							fpag.NumeroDocumentoSocio = pag.NumeroDocumento;
							fpag.FechaDocumentoPagoSocio = Fecha.toStringDate(pag.FechaDocumentoPago);
						}
						else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && !"MEME".equalsIgnoreCase(cte.IdTipoProducto)) {
							fpag.IdCuentaBancariaSocio = pag.IdCuentaBancaria;
							fpag.IdBancoSocio = pag.IdBanco;
							fpag.NumeroCuentaBancariaSocio = pag.NumeroCuentaBancaria;
							fpag.IdTipoCuentaBancariaSocio = pag.IdTipoCuentaBancaria;
							fpag.NombreTipoCuentaBancariaSocio = pag.NombreTipoCuentaBancaria;
							fpag.IdTarjetaBancariaSocio = pag.IdTarjetaBancaria;
							fpag.IdTipoTarjetaBancariaSocio = pag.IdTipoTarjetaBancaria;
							fpag.NombreTipoTarjetaBancariaSocio = pag.NombreTipoTarjetaBancaria;
							fpag.NumeroTarjetaBancariaSocio = pag.NumeroTarjetaBancaria;
							fpag.NumeroSerieSocio = pag.NumeroSerie;
							fpag.NumeroDocumentoSocio = pag.NumeroDocumento;
							fpag.FechaDocumentoPagoSocio = Fecha.toStringDate(pag.FechaDocumentoPago);
						}
						else {
							fpag.IdCuentaBancariaSocio = 0L;
							fpag.IdBancoSocio = 0L;
							fpag.NumeroCuentaBancariaSocio = "";
							fpag.IdTipoCuentaBancariaSocio = "";
							fpag.NombreTipoCuentaBancariaSocio = "";
							fpag.IdTarjetaBancariaSocio = 0L;
							fpag.IdTipoTarjetaBancariaSocio = "";
							fpag.NombreTipoTarjetaBancariaSocio = "";
							fpag.NumeroTarjetaBancariaSocio = "";
							fpag.NumeroSerieSocio = "";
							fpag.NumeroDocumentoSocio = 0L;
							fpag.FechaDocumentoPagoSocio = "0001-01-01";
						}
						
						fpag.NombreCuentaBancariaSocio = "";
						fpag.IdChequera = 0L;
						
						listFormasDePago.add(fpag);
					}
				}
			} else {
				
				if ("MEME".equalsIgnoreCase(cte.IdTipoProducto)) {
					continue;
				}
			
				IngresoEgreso_Entrada ing = new IngresoEgreso_Entrada();
				ing.NumeroReferencia = cte.IdComprobante;
				
				if (   (tiposFactura.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) 
					|| (tiposNotaDebito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) ) {
					ing.IdTipoMovimiento = "MOVING";
				}
				else if ((tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == 1) 
					 || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == 1) ){
					ing.IdTipoMovimiento = "MOVEGR";
				}
				else if ((tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == -1) 
				     || (tiposNotaDebito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && cte.MontoLiquidacion.compareTo(cte.MontoLiquidacionCobro) == -1) ) {
					ing.IdTipoMovimiento = "MOVING";
				}
				else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion) ) {
					ing.IdTipoMovimiento = "MOVING";
				}
				else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) ) {
					ing.IdTipoMovimiento = "MOVEGR";
				}
				else if (tiposNotaCreditoEsp.contains(cte.IdTipoComprobante)) {
					ing.IdTipoMovimiento = "MOVEGR";
				}
				
				PorcentajeRetencionRegimenAPVB porcentaje= (PorcentajeRetencionRegimenAPVB) hashprocentajeRetencionAPV.get(cte.CodigoPortafolio);
					
				ing.IdTipoPersona = "CLI";
				ing.IdPersona = cte.IdPersona;
				ing.IdPortafolio = cte.IdPortafolio;
				ing.IdMoneda = cte.IdMonedaLiquidacion;
				if (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1) {
					if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) 
							&& "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) && tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)){
						ing.Monto = (cte.IdMoneda.equals(cte.IdMonedaLiquidacion)) ? 
								cte.Monto.abs().multiply(new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100) ,MathContext.DECIMAL128).abs(), MathContext.DECIMAL128) : 
									cte.MontoLiquidacion.abs().multiply(new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100) ,MathContext.DECIMAL128).abs(), MathContext.DECIMAL128);
						ing.Monto = MonedaFormato.formatear(conn, ing.Monto, MonedaFormato.MONTO, ing.IdMoneda).valor;
					} else {
						ing.Monto = (cte.IdMoneda.equals(cte.IdMonedaLiquidacion)) ? cte.Monto.abs() : cte.MontoLiquidacion.abs();
					}
				} else {
					if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio) ) 
							&& "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) && tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)){
						ing.Monto = cte.MontoLiquidacionFinal.abs().multiply(new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100) ,MathContext.DECIMAL128).abs(), MathContext.DECIMAL128);
						ing.Monto = MonedaFormato.formatear(conn, ing.Monto, MonedaFormato.MONTO, ing.IdMoneda).valor;
					} else {
						ing.Monto = cte.MontoLiquidacionFinal.abs();
					}
				}
				ing.FechaLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
				ing.IdConcepto = cte.IdConcepto;
				ing.IdTipoPersonaTitularPago = "CLI";
				ing.IdPersonaTitularPago = cte.IdPersona;
				ing.IdPortafolioTitularPago = cte.IdPortafolio;
				ing.IdCaja = 0L;
				ing.SecuenciaAgrupacion = "";
				ing.SecuenciaRelacionada = cte.IdComprobante;
				ing.Glosa = "";
				ing.IdEmpleadoDigitador = cte.IdEmpleadoEjecutivoPortafolio;
				ing.IdSucursalEmpleadoDigitador = cte.IdSucursalEmpleadoEjecutivoPortafolio;
				ing.IdTipoProducto = cte.IdTipoProducto;
				ing.IdTipoOperacion = cte.IdTipoOperacion;
				ing.IdTipoComprobante = cte.IdTipoComprobante;
				ing.IdComprobante = cte.IdComprobante;
				ing.IdTipoCanalIngreso = cte.IdTipoCanalIngreso; 
				
				if ("PE".equalsIgnoreCase(localizacionSistema)){
					ing.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);				
				}else {
					ing.FechaComprobante = fechaSistema;
				}
				
				ing.FechaLiquidacionComprobante = Fecha.toStringDate(cte.FechaLiquidacion);
				ing.IdOperacion = cte.IdComprobante;
				ing.FechaOperacion = "0001-01-01";
				ing.IdCondicionLiquidacion = cte.IdCondicionLiquidacion;
				ing.IdBolsa = "";
				ing.IdMonedaOperacion = "";
				ing.MontoOperacion = ing.Monto;
				ing.IdTipoReajuste = "";
				ing.FechaParidad = "0001-01-01";
				ing.ValorParidad = BigDecimal.ZERO;
				ing.FolioCartera = 0L;
				ing.IdInstrumento = (cte.IdInstrumento == null ? 0 : cte.IdInstrumento);
				ing.IdTipoInstrumento = (cte.IdTipoInstrumento == null ? "" : cte.IdTipoInstrumento);
				ing.IdClasificacionTipoInstrumento = (cte.IdClasificacionTipoInstrumento == null ? "" : cte.IdClasificacionTipoInstrumento);
				ing.Bilateral = "N";
				
				
				ing.GeneraComprobante = in.p_GeneraComprobanteTesoreria;
				if(in.p_GeneraComprobanteTesoreria == null) {
					//mas abajo se revalida la asignacion de este campo de acuerdo a los montos enviados a tesoreria
					ing.GeneraComprobante = (cte.comprobanteFormasDePago.size() > 0) ? (genCbmteTeso == null || "".equalsIgnoreCase(genCbmteTeso)) ? "N" : genCbmteTeso : "N";
				}
				
				
				
				ing.AgruparEnComprobante = "S";
				ing.MueveCuentaInversion = "S";
				ing.TitularPago = "N";
	
				
				listIngresosEgresos.add(ing);
				
				if (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1) {
					IngresoEgreso_Entrada ingMX = new IngresoEgreso_Entrada();
					ingMX.NumeroReferencia = cte.IdComprobante * 1000;
					ingMX.IdTipoMovimiento = (!tiposNotaCredito.contains(cte.IdTipoComprobante)) ? "MOVING" : "MOVEGR";
					ingMX.IdTipoPersona = "CLI";
					ingMX.IdPersona = cte.IdPersona;
					ingMX.IdPortafolio = cte.IdPortafolio;
					ingMX.IdMoneda = "CLP";
					if (("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) || "APV-AN".equalsIgnoreCase(cte.CodigoPortafolio)) 
							&& "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) && tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)){
						ingMX.Monto = cte.MontoComprobanteCobro.abs().multiply(new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100) ,MathContext.DECIMAL128).abs(), MathContext.DECIMAL128);
						ingMX.Monto = MonedaFormato.formatear(conn, ingMX.Monto, MonedaFormato.MONTO, ingMX.IdMoneda).valor;
					} else {
						ingMX.Monto = cte.MontoComprobanteCobro.abs();
					}
					ingMX.FechaLiquidacion = Fecha.toStringDate(cte.FechaLiquidacion);
					ingMX.IdConcepto = cte.IdConcepto;
					ingMX.IdTipoPersonaTitularPago = "CLI";
					ingMX.IdPersonaTitularPago = cte.IdPersona;
					ingMX.IdPortafolioTitularPago = cte.IdPortafolio;
					ingMX.IdCaja = 0L;
					ingMX.SecuenciaAgrupacion = "";
					ingMX.SecuenciaRelacionada = cte.IdComprobante;
					ingMX.Glosa = "";
					ingMX.IdEmpleadoDigitador = cte.IdEmpleadoEjecutivoPortafolio;
					ingMX.IdSucursalEmpleadoDigitador = cte.IdSucursalEmpleadoEjecutivoPortafolio;
					ingMX.IdTipoProducto = cte.IdTipoProducto;
					ingMX.IdTipoOperacion = cte.IdTipoOperacion;
					ingMX.IdTipoComprobante = cte.IdTipoComprobante;
					ingMX.IdComprobante = cte.IdComprobante;
					ingMX.IdTipoCanalIngreso = cte.IdTipoCanalIngreso;
					if ("PE".equalsIgnoreCase(localizacionSistema)){
						ingMX.FechaComprobante = Fecha.toStringDate(cte.FechaComprobante);				
					}else {
						ingMX.FechaComprobante = fechaSistema;
					}
					ingMX.FechaLiquidacionComprobante = Fecha.toStringDate(cte.FechaLiquidacion);
					ingMX.IdOperacion = cte.IdComprobante;
					ingMX.FechaOperacion = "0001-01-01";
					ingMX.IdCondicionLiquidacion = cte.IdCondicionLiquidacion;
					ingMX.IdBolsa = "";
					ingMX.IdMonedaOperacion = "";
					ingMX.MontoOperacion = ingMX.Monto;
					ingMX.IdTipoReajuste = "";
					ingMX.FechaParidad = "0001-01-01";
					ingMX.ValorParidad = BigDecimal.ZERO;
					ingMX.FolioCartera = 0L;
					ingMX.IdInstrumento = (cte.IdInstrumento == null ? 0 : cte.IdInstrumento);
					ingMX.IdTipoInstrumento = (cte.IdTipoInstrumento == null ? "" : cte.IdTipoInstrumento);
					ingMX.IdClasificacionTipoInstrumento = (cte.IdClasificacionTipoInstrumento == null ? "" : cte.IdClasificacionTipoInstrumento);
					ingMX.Bilateral = "N";
					ingMX.GeneraComprobante = in.p_GeneraComprobanteTesoreria;
					if(in.p_GeneraComprobanteTesoreria == null) {
						//mas abajo se revalida la asignacion de este campo de acuerdo a los montos enviados a tesoreria
						ingMX.GeneraComprobante = (cte.comprobanteFormasDePago.size() > 0) ? (genCbmteTeso == null || "".equalsIgnoreCase(genCbmteTeso)) ? "N" : genCbmteTeso : "N";
					}
					ingMX.AgruparEnComprobante = "S";
					ingMX.MueveCuentaInversion = "S";
					ingMX.TitularPago = "N";
					listIngresosEgresos.add(ingMX);
				}
				
				for (ComprobanteFormaDePago_Proceso pag : cte.comprobanteFormasDePago) {
					if (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1) {
						if (!ing.IdMoneda.equalsIgnoreCase(pag.IdMoneda) && !"CLP".equalsIgnoreCase(pag.IdMoneda)) continue;
					} else  {
						if(!ing.IdMoneda.equalsIgnoreCase(pag.IdMoneda)) continue;
					}
					
					if (!SI.equalsIgnoreCase(pag.AfectarTesoreria)) {
						continue;
					}
					
					
					FormaDePago_Entrada fpag = new FormaDePago_Entrada();
					
					fpag.NumeroReferencia = (NO.equalsIgnoreCase(cobroMXsobrePagoMX) && !"CLP".equalsIgnoreCase(cte.IdMonedaLiquidacion) && productosSinCobroMX.stream().anyMatch(o -> o.equals(cte.IdTipoProducto)) && new BigDecimal("0").compareTo(cte.MontoComprobanteCobro) == -1 && !ing.IdMoneda.equalsIgnoreCase(pag.IdMoneda) && "CLP".equalsIgnoreCase(pag.IdMoneda)) ? ing.NumeroReferencia * 1000 : ing.NumeroReferencia;
					fpag.NumeroReferenciaFormaDePago = indexFormaDePago++;
					fpag.IdMedioDePago = pag.IdMedioDePago;
					fpag.IdModalidadDePago = pag.IdModalidadDePago;
					fpag.IdMoneda = pag.IdMoneda;
					if ("APV-B".equalsIgnoreCase(cte.CodigoPortafolio) && "RV".equalsIgnoreCase(cte.IdClasificacionTipoInstrumento) && tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion)){
						fpag.Monto = pag.MontoLiquidacionFormaDePago.multiply((new BigDecimal(100).subtract(porcentaje.PorcentajeRetencion, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128)), MathContext.DECIMAL128);
						fpag.Monto = MonedaFormato.formatear(conn, fpag.Monto, MonedaFormato.MONTO, fpag.IdMoneda).valor;
					} else {
						fpag.Monto = pag.MontoLiquidacionFormaDePago;
					}
	
					if ((tiposFactura.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) ||  (tiposNotaDebito.contains(cte.IdTipoComprobante) && "COMPRA".equalsIgnoreCase(cte.Accion)) ) {
						fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
						fpag.IdTarjetaBancariaCorredora = pag.IdTarjetaBancaria;
						fpag.NumeroDocumentoCorredora = pag.NumeroDocumento;
						fpag.FechaDocumentoPagoCorredora = Fecha.toStringDate(pag.FechaDocumentoPago);
					}
					else if (tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && (pag.MontoLiquidacionFormaDePago.compareTo(cte.MontoLiquidacionCobro) == -1)) {
						fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
						fpag.IdTarjetaBancariaCorredora = pag.IdTarjetaBancaria;
						fpag.NumeroDocumentoCorredora = pag.NumeroDocumento;
						fpag.FechaDocumentoPagoCorredora = Fecha.toStringDate(pag.FechaDocumentoPago);
					}
					else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && !"MEME".equalsIgnoreCase(cte.IdTipoProducto) && !"INSI".equalsIgnoreCase(cte.IdTipoProducto)) {
						fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
						fpag.IdTarjetaBancariaCorredora = pag.IdTarjetaBancaria;
						fpag.NumeroDocumentoCorredora = pag.NumeroDocumento;
						fpag.FechaDocumentoPagoCorredora = Fecha.toStringDate(pag.FechaDocumentoPago);
					}
					else {
						fpag.IdCuentaBancariaCorredora = 0L;
						fpag.IdTarjetaBancariaCorredora = 0L;
						fpag.NumeroDocumentoCorredora = 0L;
						fpag.FechaDocumentoPagoCorredora = "0001-01-01";
					}
					
					if (tiposFactura.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && (pag.MontoLiquidacionFormaDePago.compareTo(cte.MontoLiquidacionCobro) == 1) ) {
						fpag.IdCuentaBancariaSocio = pag.IdCuentaBancaria;
						fpag.IdBancoSocio = pag.IdBanco;
						fpag.NumeroCuentaBancariaSocio = pag.NumeroCuentaBancaria;
						fpag.IdTipoCuentaBancariaSocio = pag.IdTipoCuentaBancaria;
						fpag.NombreTipoCuentaBancariaSocio = pag.NombreTipoCuentaBancaria;
						fpag.IdTarjetaBancariaSocio = pag.IdTarjetaBancaria;
						fpag.IdTipoTarjetaBancariaSocio = pag.IdTipoTarjetaBancaria;
						fpag.NombreTipoTarjetaBancariaSocio = pag.NombreTipoTarjetaBancaria;
						fpag.NumeroTarjetaBancariaSocio = pag.NumeroTarjetaBancaria;
						fpag.NumeroSerieSocio = pag.NumeroSerie;
						fpag.NumeroDocumentoSocio = pag.NumeroDocumento;
						fpag.FechaDocumentoPagoSocio = Fecha.toStringDate(pag.FechaDocumentoPago);
					}
					else if (tiposNotaCredito.contains(cte.IdTipoComprobante) && "VENTA".equalsIgnoreCase(cte.Accion) && !"MEME".equalsIgnoreCase(cte.IdTipoProducto)) {
						fpag.IdCuentaBancariaSocio = pag.IdCuentaBancaria;
						fpag.IdBancoSocio = pag.IdBanco;
						fpag.NumeroCuentaBancariaSocio = pag.NumeroCuentaBancaria;
						fpag.IdTipoCuentaBancariaSocio = pag.IdTipoCuentaBancaria;
						fpag.NombreTipoCuentaBancariaSocio = pag.NombreTipoCuentaBancaria;
						fpag.IdTarjetaBancariaSocio = pag.IdTarjetaBancaria;
						fpag.IdTipoTarjetaBancariaSocio = pag.IdTipoTarjetaBancaria;
						fpag.NombreTipoTarjetaBancariaSocio = pag.NombreTipoTarjetaBancaria;
						fpag.NumeroTarjetaBancariaSocio = pag.NumeroTarjetaBancaria;
						fpag.NumeroSerieSocio = pag.NumeroSerie;
						fpag.NumeroDocumentoSocio = pag.NumeroDocumento;
						fpag.FechaDocumentoPagoSocio = Fecha.toStringDate(pag.FechaDocumentoPago);
					}
					else {
						fpag.IdCuentaBancariaSocio = 0L;
						fpag.IdBancoSocio = 0L;
						fpag.NumeroCuentaBancariaSocio = "";
						fpag.IdTipoCuentaBancariaSocio = "";
						fpag.NombreTipoCuentaBancariaSocio = "";
						fpag.IdTarjetaBancariaSocio = 0L;
						fpag.IdTipoTarjetaBancariaSocio = "";
						fpag.NombreTipoTarjetaBancariaSocio = "";
						fpag.NumeroTarjetaBancariaSocio = "";
						fpag.NumeroSerieSocio = "";
						fpag.NumeroDocumentoSocio = 0L;
						fpag.FechaDocumentoPagoSocio = "0001-01-01";
					}
					
					fpag.NombreCuentaBancariaSocio = "";
					fpag.IdChequera = 0L;
					
					listFormasDePago.add(fpag);
					
				}
			}		
		}
		
		
		
		// Si el monto de las fomas de pago no coincide con el monto del movimiento que se creara en tesoreria, el parametro GeneraComprobante debe ir en N es decir generar solo el mov en tesorri no el comprobante (aunque genCbmteTeso este encendido)
		for (IngresoEgreso_Entrada movTes : listIngresosEgresos)
		{
			List<FormaDePago_Entrada> FormasDePago = listFormasDePago.stream().filter(c -> c.NumeroReferencia.compareTo(movTes.NumeroReferencia) == 0).collect(Collectors.toList());
			BigDecimal totalMontoFormasPago = BigDecimal.ZERO;
					
			if (!FormasDePago.isEmpty())
			{
				totalMontoFormasPago = FormasDePago.stream().map(o -> o.Monto).reduce(BigDecimal.ZERO, BigDecimal::add);
			}				
			
			if ("SPT".equalsIgnoreCase(movTes.IdTipoInstrumento) && NO.equalsIgnoreCase(movTes.GeneraComprobante))
			{
				movTes.GeneraComprobante = genCbmteTesoSPT;
			}
			
			if (movTes.Monto.compareTo(totalMontoFormasPago) != 0 )
			{
				movTes.GeneraComprobante = "N";
			}
		}
		
		// Obtencion de IdComprobanteReferencia
		HashMap<String, Object> paramsNotaCredito = new HashMap<>();
		List<Long> idsComprobanteReferencia = hashComprobanteLlave.values().stream().filter(s -> s.IdComprobanteReferencia!= 0).map(s -> s.IdComprobanteReferencia).filter(Objects::nonNull).distinct().collect(Collectors.toList());
		if (!idsComprobanteReferencia.isEmpty()) 
			paramsNotaCredito.put("idsComprobanteOrigen", idsComprobanteReferencia);
		
		// Consulta Tesoreria Liquidados
		List<ComprobanteLiquidadoTesoreria> comprobanteTesoreriaLiquidados;
		if (paramsNotaCredito.size()>0) {
			comprobanteTesoreriaLiquidados = _sql.query(conn, SQL_SELECT_COMPROBANTES_LIQUIDADOS_TESORERIA, paramsNotaCredito, ComprobanteLiquidadoTesoreria.class);
		}
		else {
			comprobanteTesoreriaLiquidados = new ArrayList<>();
		}
		HashMap<Long,ComprobanteLiquidadoTesoreria> hasComprobanteTesoreriaLiquidados = new HashMap<>();
		Set<Long> referenciasLiquidadas = comprobanteTesoreriaLiquidados.stream().map(c -> c.IdIngresoEgreso).filter(Objects::nonNull).collect(Collectors.toSet());

		for(ComprobanteLiquidadoTesoreria mov : comprobanteTesoreriaLiquidados) {
			hasComprobanteTesoreriaLiquidados.put(mov.IdComprobanteOrigen, mov);	
		}
		
		Set<Long> idsComprobantesNCE = hashComprobanteLlave.values().stream().filter(cte -> "NCE".equals(cte.IdTipoComprobante)||"NEE".equals(cte.IdTipoComprobante) ||"NEO".equals(cte.IdTipoComprobante)).map(cte -> cte.IdComprobante).collect(Collectors.toSet());
			       	    	
		// Determinar por comprobante si realiza contrapartida o no
		HashMap<Long, Boolean> mapEjecutaTesoreria = new HashMap<>();
		
		for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {

			 boolean ejecutaTesoreria =
				        cte.IdComprobanteReferencia != null && hasComprobanteTesoreriaLiquidados.containsKey(cte.IdComprobanteReferencia);			  
				    	mapEjecutaTesoreria.put(cte.IdComprobante, ejecutaTesoreria);
		}
		
		// Separar en dos listas
		List<IngresoEgreso_Entrada> listIngresosEgresosLiquidados = new ArrayList<>();
		List<IngresoEgreso_Entrada> listIngresosEgresosNoLiquidados = new ArrayList<>();
		 Map<Long, Comprobante_Proceso> mapComprobantePorId = new HashMap<>();

			for (Comprobante_Proceso c : hashComprobanteLlave.values()) {
			    if (c.IdComprobante != null) {
			        mapComprobantePorId.put(c.IdComprobante, c);
			    }
			}
		for (IngresoEgreso_Entrada ie : listIngresosEgresos) {

		    boolean esNCE = idsComprobantesNCE.contains(ie.IdComprobante);
		    Boolean ejecutaTesoreria = mapEjecutaTesoreria.get(ie.IdComprobante);

		    if (!esNCE) {
		        // NO es Nota de Credito
		        listIngresosEgresosLiquidados.add(ie);
		        continue;
		    }

		    // Es Nota de Credito
		    if (Boolean.TRUE.equals(ejecutaTesoreria)) {
		    	   boolean esLiquidado = "MEME".equals(ie.IdTipoProducto)? referenciasLiquidadas.contains(ie.NumeroReferencia): true;
		    	                    
		    	   if (esLiquidado) {
		    	       // NCE + Liquidado
		    	       listIngresosEgresosLiquidados.add(ie);
		    	   } else {
		    	       // NCE + NO liquidado
		    	       listIngresosEgresosNoLiquidados.add(ie);
		    	   }
		    	}	    
		}
		
		List<IngresoEgreso_Entrada> listNoNCE = listIngresosEgresosLiquidados.stream().filter(ie -> !idsComprobantesNCE.contains(ie.IdComprobante)).collect(Collectors.toList());		
		Map<String, IngresoEgreso_Entrada> mapMovimientos = new LinkedHashMap<>();
		   
		for (IngresoEgreso_Entrada ie : listIngresosEgresosLiquidados) {
			
			if (!idsComprobantesNCE.contains(ie.IdComprobante)) {
		        continue;
		    }

		    if (!Boolean.TRUE.equals(mapEjecutaTesoreria.get(ie.IdComprobante))) {
		        continue;
		    }

		    String key = ie.IdComprobante + "_" + ie.NumeroReferencia + "_" + ie.IdMoneda + "_" + ie.Monto;
		           	         
		    mapMovimientos.putIfAbsent(key, ie);
		}

		List<IngresoEgreso_Entrada> movimientosContrapartida = new ArrayList<>(mapMovimientos.values());
	        
		AgregarLog agregarLogTesoreriaContrapartida = new AgregarLog();
	    AgregarLog.InAgregarLog entradaLogTesoreriaContrapartida = new AgregarLog.InAgregarLog();
	    
	    for (IngresoEgreso_Entrada ie : movimientosContrapartida) {
			
				Comprobante_Proceso cte = mapComprobantePorId.get(ie.IdComprobante);

				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.NombreProceso = "Generación Movimiento Contrapartida Tesorería";
				log.IdCabeceraLog = ie.IdComprobante != null ? ie.IdComprobante : 0L;
				log.IdTipoLog = "INFORMATIVO";
				log.IdModulo = "TES";
				log.IdTipoRegistro = "COMPROBANTE_CONTRAPARTIDA";
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
				log.Folio = ie.IdComprobante != null ? ie.IdComprobante : 0L;
				log.Monto = ie.Monto!= null ? ie.Monto : BigDecimal.ZERO;
				log.EstadoRegistro = "ING";
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = (cte != null && cte.IdCliente != null) ? cte.IdCliente : 0L;              
				log.NombreCliente = (cte != null && cte.NombreLargoCliente != null) ? cte.NombreLargoCliente : "";      
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoOperacion = ie.IdTipoOperacion != null ? ie.IdTipoOperacion : "";
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.IdMonedaOrigen = ie.IdMoneda;
				log.IdMonedaLiquidacion = "";
				log.Nemo = (cte != null && cte.NemotecnicoInstrumento != null) ? cte.NemotecnicoInstrumento : "";         
				log.IdProceso = 0L;
				log.EstadoProceso = "";

				entradaLogTesoreriaContrapartida.p_Log.add(log);
			}

		if (!entradaLogTesoreriaContrapartida.p_Log.isEmpty()) {
		    agregarLogTesoreriaContrapartida.logica(conn, entradaLogTesoreriaContrapartida);
		}
		long contadorMovContrapartidaTes = movimientosContrapartida.size();
		if (contadorMovContrapartidaTes > 0) {

		    String codigoMensajeResumen = "UI_CREACION_CONTRAPARTIDAS_TESORERIA";

		    String mensajeResumen =   Diccionario.getMensaje(conn, in.p_IdEmpresa, codigoMensajeResumen).add("Cantidad", contadorMovContrapartidaTes).msg(); 	
		    out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_OK,codigoMensajeResumen,mensajeResumen));

		}
		
		// Determinar Formas de Pago Liquidadas.
		Set<Long> numerosReferenciaLiquidados = listIngresosEgresosLiquidados.stream().map(ie -> ie.NumeroReferencia).filter(Objects::nonNull).collect(Collectors.toSet());		      		             
		List<FormaDePago_Entrada> listFormasDePagoLiquidadas = new ArrayList<>();
			
		for (FormaDePago_Entrada fdp : listFormasDePago) {

		    if (numerosReferenciaLiquidados.contains(fdp.NumeroReferencia)) {
		        listFormasDePagoLiquidadas.add(fdp);
		    }

		}
		
		Map<Long, FormaDePago_Entrada> mapFormasPago = new LinkedHashMap<>();

		for (FormaDePago_Entrada fdp : listFormasDePagoLiquidadas) {
		    mapFormasPago.putIfAbsent(fdp.NumeroReferencia, fdp);
		}
		listFormasDePagoLiquidadas = new ArrayList<>(mapFormasPago.values());
		
		// Comprobantes NO LIQUIDADOS.
		List<ComprobanteNoLiquidadoTesoreria> comprobanteTesoreriaNoLiquidados;
		if (paramsNotaCredito.size() > 0) {
			comprobanteTesoreriaNoLiquidados = _sql.query(conn, SQL_SELECT_COMPROBANTES_NO_LIQUIDADOS_TESORERIA, paramsNotaCredito, ComprobanteNoLiquidadoTesoreria.class);
		}
		else {
			comprobanteTesoreriaNoLiquidados = new ArrayList<>();
		}
		List<AnularRegistros.Registro_Entrada> registrosAnular = new ArrayList<>();

		for (ComprobanteNoLiquidadoTesoreria mov : comprobanteTesoreriaNoLiquidados) {
			
		        AnularRegistros.Registro_Entrada regComp = new AnularRegistros.Registro_Entrada();
		        regComp.Id = mov.IdComprobante;
		        regComp.Entidad = "TES.Comprobante";
		        registrosAnular.add(regComp);
		}
			    
		if (!registrosAnular.isEmpty()) {

		    AnularRegistros serviceAnular = new AnularRegistros();
		    AnularRegistros.AnularRegistros_Entrada entradaAnular = new AnularRegistros.AnularRegistros_Entrada();
		            
		    entradaAnular.p_IdEmpresa = in.p_IdEmpresa;
		    entradaAnular.p_IdUsuario = in.p_IdUsuario;
		    entradaAnular.p_IdModulo = "FAC";
		    entradaAnular.p_UsaReglasNegocio = "S";
		    entradaAnular.p_simularOperacion = in.p_simularOperacion;
		    entradaAnular.p_FechaMovimiento = comprobanteTesoreriaNoLiquidados.get(0).FechaComprobanteOrigen;
		            
		    entradaAnular.p_RegistrosAProcesar = registrosAnular;

		    AnularRegistros.AnularRegistros_Salida salidaAnular = serviceAnular.logica(conn, entradaAnular);

		            
		    out.P_OUT_MENSAJES.addAll(salidaAnular.P_OUT_MENSAJES);
		    
		    long erroresServicio = MensajeSalida.getNumeroErrores(salidaAnular.P_OUT_MENSAJES);
		            
		    if (erroresServicio > 0) {
		        out.P_OUT_ERRORESCONTADOR += erroresServicio;
		    }	  
		}	    
		// Movimientos NO LIQUIDADOS.
		if (paramsNotaCredito.size() > 0) {
			List<MovimientoNoLiquidadoTesoreria> movimientosTesoreriaNoLiquidados = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_NO_LIQUIDADOS_TESORERIA, paramsNotaCredito, MovimientoNoLiquidadoTesoreria.class);					
			AgregarLog agregarLogTesoreriaAnular = new AgregarLog();
		    AgregarLog.InAgregarLog entradaLogTesoreriaAnular = new AgregarLog.InAgregarLog();
		    long contadorMovAnuladosTes = 0;
		    contadorMovAnuladosTes = movimientosTesoreriaNoLiquidados.size();
		    
			if (!movimientosTesoreriaNoLiquidados.isEmpty()) {
			    _sql.batch(conn,SQL_UPDATE_ANULACION_MOVIMIENTOS,movimientosTesoreriaNoLiquidados,MovimientoNoLiquidadoTesoreria.class);
			    	   
			    for (MovimientoNoLiquidadoTesoreria mov : movimientosTesoreriaNoLiquidados) {
	
			        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			        log.NombreProceso = "Anulación Movimiento Tesorería";
			        log.IdCabeceraLog = mov.IdComprobanteOrigen != null ? mov.IdComprobanteOrigen : 0L;
			        log.IdTipoLog = "INFORMATIVO";
			        log.IdModulo = "TES";
			        log.IdTipoRegistro = "COMPROBANTE_ANULADO";
			        log.IdUsuario = in.p_IdUsuario;
			        log.IdEmpresa = in.p_IdEmpresa;
			        log.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
			        log.Folio = mov.IdComprobanteOrigen != null ? mov.IdComprobanteOrigen : 0L;
			        log.Monto = mov.Monto != null ? mov.Monto : BigDecimal.ZERO;
			        log.EstadoRegistro = "ANU";
			        log.IdUsuarioAtendido = in.p_IdUsuario;
			        log.IdCliente = mov.IdCliente != null ? mov.IdCliente : 0L;
			        log.NombreCliente = mov.NombrePersona;
			        log.IdPortafolio = mov.IdPortafolio;
					log.EstadoPortafolio = "";
					log.IdTipoOperacion = mov.IdTipoOperacion != null ? mov.IdTipoOperacion : "";
					log.Cantidad = BigDecimal.ZERO;
					log.PrecioTasa = BigDecimal.ZERO;
					log.IdMonedaOrigen = mov.IdMoneda;
					log.IdMonedaLiquidacion = "";
					log.Nemo = mov.Nemotecnico != null ? mov.Nemotecnico : "";
					log.IdProceso = 0L;
					log.EstadoProceso = "";
	
			        entradaLogTesoreriaAnular.p_Log.add(log);
			    }
	
			    agregarLogTesoreriaAnular.logica(conn, entradaLogTesoreriaAnular);
			}
			
			
			if (contadorMovAnuladosTes > 0) {
	
			    String codigoMensajeResumen = "UI_ANULACION_MOVIMIENTOS_TESORERIA";
	
			    String mensajeResumen = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigoMensajeResumen).add("Cantidad", contadorMovAnuladosTes).msg();
			    out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_OK,codigoMensajeResumen,mensajeResumen));
			}
		}

		// ── 3. Llamada exclusiva para no-NCE → CrearIngresoEgreso ────────────
		if (!listNoNCE.isEmpty()) {

			Set<Long> movNumsReferencia = listNoNCE.stream().map(ie -> ie.NumeroReferencia).filter(Objects::nonNull).collect(Collectors.toSet());
		        
	        List<FormaDePago_Entrada> formasDePago = listFormasDePagoLiquidadas.stream().filter(fdp -> movNumsReferencia.contains(fdp.NumeroReferencia)).collect(Collectors.toList());
	        
	        CrearIngresoEgreso serviceCrearIngresoEgreso = new CrearIngresoEgreso();
	        CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();

	        entradaCrearIngresoEgreso.p_EsIngresoAutomatico  = "S";
	        entradaCrearIngresoEgreso.p_FormaDePago           = formasDePago;
	        entradaCrearIngresoEgreso.p_IdEmpresa             = in.p_IdEmpresa;
	        entradaCrearIngresoEgreso.p_IdModulo              = "FAC";
	        entradaCrearIngresoEgreso.p_IdUsuario             = in.p_IdUsuario;
	        entradaCrearIngresoEgreso.p_IngresoEgresoGenerar  = listNoNCE;
	        entradaCrearIngresoEgreso.p_SimularOperacion      = in.p_simularOperacion;
	        entradaCrearIngresoEgreso.p_FechaMovimiento       = ((IngresoEgreso_Entrada)listNoNCE.get(0)).FechaComprobante;
	        entradaCrearIngresoEgreso.p_UsaReglasNegocio      = "S";

	        CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = serviceCrearIngresoEgreso.logica(conn, entradaCrearIngresoEgreso);

	        // propagar errores/mensajes si es necesario
	        out.P_OUT_MENSAJES.addAll(salidaCrearIngresoEgreso.P_OUT_MENSAJES);		   
		}
		     
		// Comprobantes tipo facturas
		if (!movimientosContrapartida.isEmpty()) {
			
			// Obtener todos los NumeroReferencia
		    Set<Long> movNumsReferencia = movimientosContrapartida.stream().map(ie -> ie.NumeroReferencia).filter(Objects::nonNull).collect(Collectors.toSet());

		    // Filtrar formas de pago
		    List<FormaDePago_Entrada> formasDePagoLiquidadas = listFormasDePagoLiquidadas.stream().filter(fdp -> movNumsReferencia.contains(fdp.NumeroReferencia)).collect(Collectors.toList());
			CrearIngresoEgreso serviceCrearIngresoEgreso = new CrearIngresoEgreso();
			CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
			entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "S";
					
			entradaCrearIngresoEgreso.p_FormaDePago = formasDePagoLiquidadas;
			
			entradaCrearIngresoEgreso.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearIngresoEgreso.p_IdModulo = "FAC";
			entradaCrearIngresoEgreso.p_IdUsuario = in.p_IdUsuario;
			
			entradaCrearIngresoEgreso.p_IngresoEgresoGenerar = movimientosContrapartida; 
			entradaCrearIngresoEgreso.p_SimularOperacion = in.p_simularOperacion;
			
			entradaCrearIngresoEgreso.p_FechaMovimiento = movimientosContrapartida.get(0).FechaComprobante; 
			 
			entradaCrearIngresoEgreso.p_UsaReglasNegocio = "S";
			CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = serviceCrearIngresoEgreso.logica(conn, entradaCrearIngresoEgreso);
			
			//listIngresosEgresosGenerados = salidaCrearIngresoEgreso.P_OUT_INGRESOEGRESOGENERADO;
			out.P_OUT_MENSAJES.addAll(salidaCrearIngresoEgreso.P_OUT_MENSAJES);
			
			for (MensajeSalida msj : salidaCrearIngresoEgreso.P_OUT_MENSAJES) {
				for (Comprobante_Proceso cte: hashComprobanteLlave.values()) {
					if (msj.tipo == MensajeSalida.MENSAJE_ADVERTENCIA) {
						cte.advertencias++;
					}
					if (msj.tipo == MensajeSalida.MENSAJE_AUTORIZACION) {
						cte.autorizaciones++;
					}
					if (msj.tipo == MensajeSalida.MENSAJE_ERROR) {
						cte.errores++;
					}
				}
			}
			
			limpiaComprobantes(hashComprobanteLlave,hashComprobanteLlaveErrores);
			
			if (hashComprobanteLlave.size() == 0) {
				return out;
			}
		}		

		/***** Fin Tesoreria ****/
		

		List<MovimientoComprobanteDetalle_Proceso> movsUpdate = new ArrayList<MovimientoComprobanteDetalle_Proceso>();
		
		if (NO.equalsIgnoreCase(in.p_simularOperacion)) {
			List<Comprobante_Proceso> comprobantes = new ArrayList<Comprobante_Proceso>(hashComprobanteLlave.values());
			_sql.batch(conn, SQL_INSERT_COMPROBANTE, comprobantes, Comprobante_Proceso.class);
			
			// Agrega referencia a Comprobante de otros conceptos
			String[] tiposComprobanteRef = {"FOX", "FOE", "NEO"};
			List<String> listaTiposComprobanteRef = Arrays.asList(tiposComprobanteRef);
			List<ReferenciaTipoDocumento> listaReferenciaTipoDocumento = new ArrayList<>();
			
			for (MovimientoComprobante_Entrada mov : in.P_MOVIMIENTOFACTURAR) {
				InfoMovimientoComprobante_Proceso info = (InfoMovimientoComprobante_Proceso) hashInfoMovimientos.get(mov.IdMovimientoComprobante);
				if(listaTiposComprobanteRef.contains(info.IdTipoComprobante) &&
					(info.Referencia != null && info.Referencia.length() > 0) &&
					(info.TipoReferencia != null && info.TipoReferencia.length() > 0)
				) {
					ReferenciaTipoDocumento referenciaTipoDocumento = new ReferenciaTipoDocumento();
					referenciaTipoDocumento.IdMovimientoComprobante = info.IdMovimientoComprobante.toString();
					referenciaTipoDocumento.Referencia = info.Referencia;
					referenciaTipoDocumento.TipoReferencia = info.TipoReferencia;
					referenciaTipoDocumento.IdComprobante = info.IdComprobante.toString();
					listaReferenciaTipoDocumento.add(referenciaTipoDocumento);	
				}
			}
			_sql.batch(conn, SQL_UPDATE_REFERENCIA_TIPO_DOCUMENTO, listaReferenciaTipoDocumento, ReferenciaTipoDocumento.class);
			// FIN: Agrega referencia a Comprobante de otros conceptos
			
			List<ComprobanteCobro> comprobantesCobro = new ArrayList<>();
			for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
				comprobantesCobro.addAll(cte.comprobanteCobros);
			}
			_sql.batch(conn, SQL_INSERT_COMPROBANTE_COBRO, comprobantesCobro, ComprobanteCobro.class);
			
			List<ComprobanteFormaDePago> comprobantesFormasDePago = new ArrayList<>();
			for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
				comprobantesFormasDePago.addAll(cte.comprobanteFormasDePago);
			}
			_sql.batch(conn, SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO, comprobantesFormasDePago, ComprobanteFormaDePago.class);
			
			List<ComprobanteDetalle_Proceso> comprobantesDetalle = new ArrayList<>();
			for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
				comprobantesDetalle.addAll(cte.comprobanteDetalles);
			}
			_sql.batch(conn, SQL_INSERT_COMPROBANTE_DETALLE, comprobantesDetalle, ComprobanteDetalle_Proceso.class);
			
			List<MovimientoComprobanteDetalle> movimientosDetalle = new ArrayList<>();	
			
			for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
				movimientosDetalle.addAll(cte.movimientosComprobanteDetalle);
				movsUpdate.addAll(cte.movimientosComprobanteDetalle);
			}
			_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_DETALLE, movimientosDetalle, MovimientoComprobanteDetalle.class);
			_sql.batch(conn, SQL_UPDATE_COMPROBANTE_REFERENCIA, comprobantes, Comprobante_Proceso.class);
			
			_sql.batch(conn, SQL_UPDATE_MOVIMIENTOS, movsUpdate, MovimientoComprobanteDetalle_Proceso.class);
			
			if(SI.equalsIgnoreCase(paramLiquidacionComisionFact)) {
				HashMap<Long,Long> movimientosComisionMensual = new HashMap<>();		
				HashMap<String,Object> parametro = new HashMap<>();
				ArrayList<Long> idsMovimientos = new ArrayList<>();	

				movsUpdate.stream().filter(mov -> mov.Glosa != null && mov.Glosa.toUpperCase().startsWith("COMISION MENSUAL")).forEach(mov -> {
					idsMovimientos.add(mov.IdMovimientoComprobante);
					movimientosComisionMensual.put(mov.IdMovimientoComprobante, mov.IdComprobante);
				});
				
				parametro.put("p_IdMovimientos", idsMovimientos);
				
				List<ComprobanteCobrosDetalle_Salida> cobrosComisionesMensualesDetalle  = _sql.query(conn, SQL_SELECT_COBROS_COMISIONES_MENSUALES_DETALLE, parametro, ComprobanteCobrosDetalle_Salida.class);

				if(cobrosComisionesMensualesDetalle.size() > 0)
				{
					cobrosComisionesMensualesDetalle.stream().filter(ccm -> movimientosComisionMensual.containsKey(ccm.IdMovimientoComprobanteComisionMensual)).forEach(ccm -> {
						ccm.IdComprobanteComisionMensual = movimientosComisionMensual.get(ccm.IdMovimientoComprobanteComisionMensual);
					});
					
					_sql.batch(conn, SQL_UPDATE_COBROS_COMISIONES_MENSUALES, cobrosComisionesMensualesDetalle, ComprobanteCobrosDetalle_Salida.class);
					_sql.batch(conn, SQL_UPDATE_COBROS_COMISIONES_MENSUALES_DETALLE, cobrosComisionesMensualesDetalle, ComprobanteCobrosDetalle_Salida.class);
					
				}			
				
			}
			
			//procesa ganancia capital
			String retencionGananciaCapital ="N";
			try {retencionGananciaCapital = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "FACTURACION", "IGK_GENERAR_RETENCION_CMBTE");}catch (Exception er) {};
			if (SI.equalsIgnoreCase(retencionGananciaCapital)) {
				ProcesarGananciaCapital ProcesarGanancia = new ProcesarGananciaCapital();
				EntradaProcesarGananciaCapital EntradaGananciaCapital = new EntradaProcesarGananciaCapital();
				EntradaGananciaCapital.p_IdEmpresa = in.p_IdEmpresa;
				EntradaGananciaCapital.p_ComprobanteDetalle=movimientosDetalle;
				ProcesarGananciaCapital.SalidaProcesarGananciaCapital salidaGananciaCapital  = ProcesarGanancia.logica(conn, EntradaGananciaCapital,in);
			}

			//Agregar Log Comprobante Facturacion	
			
			AgregarLog agregarLog = new AgregarLog();
			InAgregarLog entradaLog = new InAgregarLog();
					
			if (comprobantes != null && !comprobantes.isEmpty()) {
				for(Comprobante_Proceso datosLog : comprobantes) {
				
					//Construir entradas log
					InAgregarLog.Log headLog = new InAgregarLog.Log();
					headLog.NombreProceso = "Generar Comprobante";
					headLog.IdCabeceraLog = datosLog.IdComprobante != null ? datosLog.IdComprobante : 0L;
					headLog.IdTipoLog = "INGRESO";
					headLog.IdModulo = in.p_IdModulo;
					headLog.IdTipoRegistro = "GENERA_COMPROBANTE";
					headLog.IdUsuario = in.p_IdUsuario;
					headLog.IdEmpresa = in.p_IdEmpresa;
					headLog.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
					headLog.IdUsuarioAtendido = in.p_IdUsuario;
					headLog.IdCliente = datosLog.IdCliente != null ? datosLog.IdCliente : 0L;
					headLog.IdPortafolio = datosLog.IdPortafolio != null ? datosLog.IdPortafolio : 0L;
					headLog.IdTipoOperacion = datosLog.IdTipoOperacion != null ? datosLog.IdTipoOperacion : "";
					headLog.Folio = datosLog.IdComprobante != null ? datosLog.IdComprobante : 0L;
					headLog.Cantidad = datosLog.Cantidad != null ? datosLog.Cantidad : BigDecimal.ZERO;
					headLog.PrecioTasa = datosLog.Precio != null ? datosLog.Precio : BigDecimal.ZERO;
					headLog.Monto = datosLog.Monto != null ? datosLog.Monto : BigDecimal.ZERO;
					headLog.IdMonedaOrigen = datosLog.IdMoneda != null ? datosLog.IdMoneda : "";
					headLog.IdMonedaLiquidacion = datosLog.IdMonedaLiquidacion != null ? datosLog.IdMonedaLiquidacion : "";
					headLog.Nemo = datosLog.NemotecnicoInstrumento != null ? datosLog.NemotecnicoInstrumento : "";
					
					//caso nemo simultaneas
					if((datosLog.IdTipoOperacion != null) && ("COMSIM".equals(datosLog.IdTipoOperacion) || "VENSIM".equals(datosLog.IdTipoOperacion))){
						headLog.Nemo = (datosLog.NemotecnicoSubyacente != null) ? datosLog.NemotecnicoSubyacente : "";
					}
					
					entradaLog.p_Log.add(headLog);
				}	
				//llamar al log
				agregarLog.logica(conn, entradaLog);
			}						
			
		}

		//PREPARAR SALIDA
		for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
			String codigo = "UI_COMPROBANTE_CREADO";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobante", cte.IdComprobante).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(cte.IdComprobante,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
			
			ResultadoProceso_Salida res = new ResultadoProceso_Salida();
			res.Campo = "IdComprobante";
			res.Id = cte.IdComprobante;
			res.Servicio = "CrearComprobante.java";
			res.Resultado = "ING";
			out.P_OUT_RESULTADOPROCESO.add(res);
			
		}
		
		for (MovimientoComprobante_Proceso mov : movimientos) {
			Comprobante cteaux = null;
			for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
				for ( MovimientoComprobanteDetalle_Proceso movdet : cte.movimientosComprobanteDetalle) {
					if (mov.IdMovimientoComprobante.equals(movdet.IdMovimientoComprobante)) {
						cteaux = cte;
						break;
					}
				}
				if (cteaux != null) {
					break;
				}
			}
			if (cteaux != null) {
				Comprobante_Salida cteSalida = new Comprobante_Salida();
				cteSalida.IdComprobante = cteaux.IdComprobante;
				cteSalida.IdMovimientoComprobante = mov.IdMovimientoComprobante;
				out.P_OUT_COMPROBANTE.add(cteSalida);	
			}
			
		}
		
		//Notificacion OpenWorkPlace
		String notificaOWP = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");
		if(SI.equalsIgnoreCase(notificaOWP)){
			HashMap<String,Object> parametro = new HashMap<>();
			ArrayList<Long> idsOrden = new ArrayList<>();	
			for (MovimientoComprobanteDetalle_Proceso mov : movsUpdate) {
				if("INAC".equalsIgnoreCase(mov.IdTipoProducto) && ("COMACN".equalsIgnoreCase(mov.IdTipoOperacion) || "VENACN".equalsIgnoreCase(mov.IdTipoOperacion))){
					idsOrden.add(mov.IdOrden);
				}
			}
			parametro.put("p_IdOrdenes", idsOrden);
			List<OrdenModificada> listaOrdenesModificadas  = _sql.query(conn, SQL_SELECT_ORDENES_MODIFICADAS, parametro, OrdenModificada.class);
					
			NotificarEstadoOrdenesOWP servicioNotOwp = new NotificarEstadoOrdenesOWP();
			List<NotificarEstadoOrdenes_Entrada> listOrdenNotiOWP = new ArrayList<NotificarEstadoOrdenes_Entrada>();
			
			for (OrdenModificada ord : listaOrdenesModificadas) {
					NotificarEstadoOrdenes_Entrada orden = new NotificarEstadoOrdenes_Entrada();
					orden.IdEmpresa = in.p_IdEmpresa;
					orden.IdUsuario = in.p_IdUsuario;
					orden.Solsec = ord.IdOrden;
					orden.NTitulos = ord.Cantidad;
					orden.Precio = ord.PrecioOrden;
					orden.Fecha = ord.FechaOperacion;
					orden.Estado = "FAC".equalsIgnoreCase(ord.IdEstadoFacturacion) ? "FAC" : ord.IdEstadoOrden;
					orden.Comision = ord.Comision;
					

					orden.Gastos = ord.Gasto.add(ord.Impuesto);
					
					orden.Mercado = "A";
					orden.Divisa = ord.IdMonedaOrden;
					orden.TipoCambio = ord.TipoCambio;
					orden.SolsecAsoc = "";
					orden.Nemo = ord.Nemotecnico;
					listOrdenNotiOWP.add(orden);
				}		
				
			if(listOrdenNotiOWP.size() > 0){
				servicioNotOwp.go(conn, listOrdenNotiOWP);
			}
		}
		
		if(out.P_OUT_ERRORESCONTADOR ==0L) {
			AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa,"GENFACT","Refresco Generacion Facturacion" );
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearComprobante", fin-inicio);
		return out;
	};

	public static String generaLlave(List<Field> fields,List<String> campos,Object obj) {
		logger.trace("INICIO::CrearComprobante.generaLlave");
		long inicio = System.currentTimeMillis();
		String llave = "";
		
		for (String campo : campos) {
			
			Field f = null;
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(campo)) {
					f= field;
					break;
				}
			}
			
			if (f != null) {
				try{
					llave+=f.get(obj);
				}
				catch(Exception e) {
					logger.error(e.getStackTrace());
				}
			}
			else {
				logger.error("FALTA LLAVE " + campo);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.generaLlave", fin-inicio);
		return llave;
	}
	
	public void anulaMovimientosComprobantesCrobrosGFOD(List<MovimientoComprobanteCobro> listIdMovimientoComprobanteCobroGFOD,Connection conn) throws Exception {
		logger.trace("INICIO::anulaMovimientosComprobantesCrobrosGFOD");
		long inicio = System.currentTimeMillis();
		
		if(listIdMovimientoComprobanteCobroGFOD.size()>0) {
			SQL _sql = new SQL();
			_sql.batch(conn, SQL_ANULA_MOVIMIENTO_COMPROBANTE_COBROS_FIJO, listIdMovimientoComprobanteCobroGFOD,MovimientoComprobanteCobro.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::anulaMovimientosComprobantesCrobrosGFOD", fin-inicio);
	}
	
	public String calculaLLaveComprobante(PreferenciaFacturacion preferencia, MovimientoComprobanteDetalle_Proceso movdet) {
		logger.trace("INICIO::CrearComprobante.calculaLLaveComprobante");
		long inicio = System.currentTimeMillis();

		String llaveCalculada = "";
		
		ArrayList<Field> fieldsSuperclass = new ArrayList<>(Arrays.asList(MovimientoComprobanteDetalle_Proceso.class.getSuperclass().getDeclaredFields()));
		ArrayList<Field> fields = new ArrayList<>(Arrays.asList(MovimientoComprobanteDetalle_Proceso.class.getDeclaredFields()));
		fields.addAll(fieldsSuperclass);
		
		List<String> llaves = new ArrayList<>(camposComprobante);
		
		boolean separaComprobantePorInstrumento = SI.equalsIgnoreCase(preferencia.SeparaComprobantePorInstrumento);
		boolean esVentaCorta = SI.equalsIgnoreCase(movdet.EsVentaCorta);
		boolean esComprobanteIndividual = SI.equalsIgnoreCase(movdet.EsComprobIndividual);
		boolean fusionVentaCortaSeparaPrecio = FUSION_SEPARA_PRECIO.equals(preferencia.FusionFacturaVentaCorta);
		boolean fusionVentaCortaPrecioPromedio = FUSION_PRECIO_PROMEDIO.equals(preferencia.FusionFacturaVentaCorta);
		boolean fusionVentaCortaNoAplica = NO_APLICA_FUSION.equals(preferencia.FusionFacturaVentaCorta);
		
		if (separaComprobantePorInstrumento) {
			llaves.add("IdInstrumento");
			if("IF".equals(movdet.IdClasificacionTipoInstrumento)) {
				llaves.add("NemoSVS");
				llaves.add("Tasa");
			}
		}
		if (!esComprobanteIndividual && esVentaCorta && fusionVentaCortaSeparaPrecio) {
			llaves.add("Precio");
		}
		if (esVentaCorta && (fusionVentaCortaPrecioPromedio || fusionVentaCortaSeparaPrecio)) {
			llaves.add("TasaOperacion");
			llaves.add("FechaVencimiento");
		}
		
		llaveCalculada = generaLlave(fields,llaves,movdet);
		
		//PARA QUE NO AGRUPE
		if (esComprobanteIndividual || fusionVentaCortaNoAplica && esVentaCorta) {
			llaveCalculada += movdet.hashCode();
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.calculaLLaveComprobante", fin-inicio);
		return llaveCalculada+"_01";
	}
	
	public String calculaLLaveComprobanteDetalle(PreferenciaFacturacion preferencia,MovimientoComprobanteDetalle_Proceso movdet) {
		logger.trace("INICIO::CrearComprobante.calculaLLaveComprobanteDetalle");
		long inicio = System.currentTimeMillis();
		
		String llaveCalculada = "";
		
		ArrayList<Field> fieldsSuperclass = new ArrayList<>(Arrays.asList(MovimientoComprobanteDetalle_Proceso.class.getSuperclass().getDeclaredFields()));
		ArrayList<Field> fields = new ArrayList<>(Arrays.asList(MovimientoComprobanteDetalle_Proceso.class.getDeclaredFields()));
		fields.addAll(fieldsSuperclass);
		
		List<String> llaves= new ArrayList<>();
		
		/* Agrupa por Preferencia de Facturacion configurada en el IdPortafolio del Cliente 
		 * 		Separa linea de Factura por Transaccion o Fusiona Factura por Precio
		 *      o Fusiona Factura a Precio Promedio RV
		 * Para los casos de Venta Corta, si esta habilitada la opcion valida si:
		 * 		Fusiona por Precio o Precio Promedio
		 */
		
		boolean fusionaDetallePorPrecio = SI.equalsIgnoreCase(preferencia.FusionaComprobanteDetallePorPrecio);
		boolean fusionaDetallePrecioPromedioRV = SI.equalsIgnoreCase(preferencia.FusionFacturaPrecioPromedioRV);
		boolean separaDetallePorTransaccion = SI.equalsIgnoreCase(preferencia.SeparaComprobanteDetallePorTransaccion);
		
		boolean fusionVentaCortaNoAplica = NO_APLICA_FUSION.equals(preferencia.FusionFacturaVentaCorta);
		boolean fusionVentaCortaPrecioPromedio = FUSION_PRECIO_PROMEDIO.equals(preferencia.FusionFacturaVentaCorta);
		boolean fusionVentaCortaSeparaPrecio = FUSION_SEPARA_PRECIO.equals(preferencia.FusionFacturaVentaCorta);

		boolean esVentaCorta = SI.equalsIgnoreCase(movdet.EsVentaCorta);
		boolean esInstrumentoRV = RENTA_VARIABLE.equalsIgnoreCase(movdet.IdClasificacionTipoInstrumento);
		boolean esInstrumentoIF = INTERMEDIACION_FINANCIERA.equals(movdet.IdClasificacionTipoInstrumento);
		boolean esSimultanea = SIMULTANEA.equalsIgnoreCase(movdet.IdTipoInstrumento);

		if (!esVentaCorta && esInstrumentoRV && !esSimultanea && fusionaDetallePrecioPromedioRV) {
			llaves = new ArrayList<>(camposComprobanteDetallePrecioPromedioRV);
		} else {
			if (!esVentaCorta || (esVentaCorta && fusionVentaCortaNoAplica)) {
				llaves = new ArrayList<>(camposComprobanteDetalle);

				if (separaDetallePorTransaccion) {
					llaves.add("IdTransaccion");
				}
				if (!fusionaDetallePorPrecio) {
					llaves.add("Precio");
				}
			} else if (esVentaCorta && fusionVentaCortaPrecioPromedio) {
				llaves = new ArrayList<>(camposComprobanteDetalleVentaCorta);
			} else if (esVentaCorta && fusionVentaCortaSeparaPrecio) {
				llaves = new ArrayList<>(camposComprobanteDetalleVentaCorta);
				llaves.add("Precio");
			}

			if (movdet.IdClasificacionTipoInstrumento != null && esInstrumentoIF) {
				llaves.add("FechaVencimiento");
				llaves.add("IdEmisor");
			}
		}
		
		llaveCalculada = generaLlave(fields,llaves,movdet);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.calculaLLaveComprobanteDetalle", fin-inicio);
		return llaveCalculada;
	}
	
	private void ProcesaRequisito(Connection conn,Long idEmpresa,boolean esError,String codMsg,String codRequisito,MovimientoComprobante_Proceso mov,HashMap<String, Object> requisitos,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		logger.trace("INICIO::CrearComprobante.ProcesaRequisito");
		long inicio = System.currentTimeMillis();
		if (esError) {
			String msg = Diccionario.getMensaje(conn, idEmpresa,codMsg)
					.add("IdMovimientoComprobante", mov.IdMovimientoComprobante)
					.msg();
			
			if (codRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				mov.errores++;
			}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				mov.errores++;						
			}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				mov.autorizaciones++;						
			}
			else if (codRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				mov.advertencias++;
			}
			else {
				mov.ok++;
			}
		}
		else {
			mov.ok++;
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.ProcesaRequisito", fin-inicio);
	}
	
	public static java.sql.Date comparaValor(java.sql.Date origen,java.sql.Date destino) {
		if (destino == null) {
			return origen;
		}
		else if (!destino.equals(origen)){
			return Fecha.toDate("0001-01-01");
		}
		return destino;
	}
	
	public static java.sql.Timestamp comparaValor(java.sql.Timestamp origen,java.sql.Timestamp destino) {
		if (destino == null) {
			return origen;
		}
		else if (!destino.equals(origen)){
			return Fecha.toDateTime("0001-01-01 00:00:00");
		}
		return destino;
	}
	
	public static String comparaValor(String origen,String destino) {
		if (destino == null) {
			return origen;
		}
		else if (!destino.equals(origen)){
			return "";
		}
		return destino;
	}
	
	public static BigDecimal comparaValor(BigDecimal origen,BigDecimal destino) {
		if (destino == null) {
			return origen;
		}
		else if (!destino.equals(origen)){
			return BigDecimal.ZERO;
		}
		return destino;
	}
	
	public static Long comparaValor(Long origen,Long destino) {
		if (destino == null) {
			return origen;
		}
		else if (!destino.equals(origen)){
			return 0L;
		}
		return destino;
	}
	
	public void limpiaMovimientos(List<MovimientoComprobante_Proceso> movs,List<MovimientoComprobante_Proceso> movsError,List<ResultadoProceso_Salida> ress) {
		logger.trace("INICIO::CrearComprobante.limpiaMovimientos");
		long inicio = System.currentTimeMillis();
		
		for (MovimientoComprobante_Proceso mov : movs) {
			if (mov.errores > 0) {
				movsError.add(mov);
				
				ResultadoProceso_Salida res = new ResultadoProceso_Salida();
				res.Campo = "IdMovimientoComprobante";
				res.Id = mov.IdMovimientoComprobante;
				res.Resultado = MensajeSalida.MENSAJE_ERROR;
				res.Servicio = "CrearComprobante.java";
				ress.add(res);
				
			}
		}
		
		for (MovimientoComprobante_Proceso mov : movsError) {
			if (movs.contains(mov)) {
				movs.remove(mov);
			}
		}		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.limpiaMovimientos", fin-inicio);
	}
	
	private BigDecimal calcularMontoAPlazoVC(Connection conn,BigDecimal tasaEntrada, BigDecimal diasPlazo, BigDecimal monto,String IdMoneda,String monedaFormato) throws Exception {
		logger.trace("INICIO::CrearComprobante.calcularMontoAPlazoVC");
		long inicio = System.currentTimeMillis();

		BigDecimal tasa 							= tasaEntrada.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
		BigDecimal periodos 						= diasPlazo.divide(new BigDecimal(30),20,RoundingMode.HALF_UP);
		BigDecimal factor 							= BigDecimal.ONE.add(tasa.multiply(periodos));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.calcularMontoAPlazoVC", fin-inicio);
		
		return MonedaFormato.formatear(conn, monto.multiply(factor), monedaFormato, IdMoneda).valor;
		
	}
	
	//Calcula monto de la prima para Venta Corta (debe usar Precio ingresado al crear la transaccion)	
	//Prima = (Precio x Cantidad) x (plazo/30) * (tasa/100)	
	private BigDecimal calcularPrimaVC(Connection conn, BigDecimal tasaEntrada, BigDecimal diasPlazo, BigDecimal cantidad, BigDecimal precio ,String IdMoneda,String monedaFormato) throws Exception {
		logger.trace("INICIO::CrearComprobante.calcularPrimaVC");
		long inicio = System.currentTimeMillis();
		
		BigDecimal tasa 							= tasaEntrada.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
		BigDecimal periodos 						= diasPlazo.divide(new BigDecimal(30),20,RoundingMode.HALF_UP);
		BigDecimal factor 							= tasa.multiply(periodos);		
		BigDecimal monto                            = cantidad.multiply(precio); // calcula monto, pero con precio medio
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.calcularPrimaVC", fin-inicio);
		
		return MonedaFormato.formatear(conn, monto.multiply(factor), monedaFormato, IdMoneda).valor;		
	}

	private void limpiaComprobantes(HashMap<String, Comprobante_Proceso> hashComprobanteLlave, HashMap<String, Comprobante_Proceso> hashComprobanteLlaveErrores) {
		logger.trace("INICIO::CrearComprobante.limpiaComprobantes");
		long inicio = System.currentTimeMillis();
		
		for (Comprobante_Proceso cte : hashComprobanteLlave.values()) {
			if (cte.errores > 0) {
				hashComprobanteLlaveErrores.put(cte.llaveComprobante, cte);
			}
		}
		
		for (Comprobante_Proceso cteErr : hashComprobanteLlaveErrores.values()) {
			if (hashComprobanteLlave.containsKey(cteErr.llaveComprobante)) {
				hashComprobanteLlave.remove(cteErr.llaveComprobante);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.limpiaComprobantes", fin-inicio);
	}
	
	private Persona obtenerPersonaSII(Connection con, Long idEmpresa) throws SQLException {
		logger.trace("INICIO::CrearComprobante.obtenerPersonaSII");
		long inicio = System.currentTimeMillis();
		Persona personaSII = null;
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		String documentoIdentidadSII = obtenerDocumentoIdentidadSII(con, idEmpresa);
		params.put("DocumentoIdentidad", documentoIdentidadSII);
		List<Persona> personasSII = sql.query(con, SQL_SELECT_PERSONA_TGR, params, Persona.class);
		if(personasSII.size() > 0) 
			personaSII = personasSII.get(0);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.obtenerPersonaSII", fin-inicio);

		return personaSII;
	}
	
	private boolean esPersonaPagoCobroAGF(Connection con, InfoMovimientoSpot_Proceso entrada, ComprobanteFormaDePago_Proceso entradaPago) {
		boolean EsPagoCobroAGF = false;
		if( entrada !=null && "BF".equals(entrada.IdRegimenTributario) 
				&& ("RESNORM".equals(entrada.IdTipoMovimientoFondoMutuo) || "RESTRASP".equals(entrada.IdTipoMovimientoFondoMutuo)) 
				&& "MOVEGR".equalsIgnoreCase(entradaPago.IdTipoMovimientoSpot) ) {
			EsPagoCobroAGF = true;
		}
		return EsPagoCobroAGF;
	}
	
	private boolean esPersonaPagoCobroAGFNN(Connection con, InfoMovimientoSpot_Proceso entrada, ComprobanteFormaDePago_Proceso entradaPago) {
		boolean pagoCobroAGFNN = false;
		if(entrada != null && "RESCNN".equals(entrada.IdTipoMovimientoFondoMutuo) && "MOVEGR".equalsIgnoreCase(entradaPago.IdTipoMovimientoSpot)) {
			pagoCobroAGFNN = true;
		}
		return pagoCobroAGFNN;
	}
	
	private String obtenerDocumentoIdentidadSII(Connection con, Long idEmpresa) {
		logger.trace("INICIO::CrearComprobante.obtenerDocumentoIdentidadSII");
		long inicio = System.currentTimeMillis();
		
		String documentoIdentidadSII = "";
		try {
			documentoIdentidadSII = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "APV.DocumentoIdentidadPersonaSII");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::Parametro APV.DocumentoIdentidadPersonaSII no existe.");
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.obtenerDocumentoIdentidadSII", fin-inicio);

		return documentoIdentidadSII;
	}
	
	public Boolean esFormaPagoSpotCorredora(String IdConcepto ,String IdTipoMovimientoSpot) {
		if(("CMMESP").equalsIgnoreCase(IdConcepto) && ("MOVING").equalsIgnoreCase(IdTipoMovimientoSpot) ||
			("VNMESP").equalsIgnoreCase(IdConcepto) && ("MOVING").equalsIgnoreCase(IdTipoMovimientoSpot)) {
			return true;
		}else {
			return false;
		}
	}
	
	private BigDecimal valorizarInstrumentoComprobanteDetalleVentaCortaTasaTIR( Connection con, ComprobanteDetalle_Proceso comprobanteDetalle, Long idEmpresa, String idModulo, Long idUsuario ) throws Exception {
		
		if ( comprobanteDetalle.movimientoComprobanteDetalles.size() < 2 ) {
			return comprobanteDetalle.Tasa;
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString( con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema" );
		String paisSistema 	= ConfiguracionSistema.getParametroConfiguracionString( con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema" );
		
		ValorizarInstrumento VI 								= new ValorizarInstrumento();
		ValorizarInstrumento.ValorizarInstrumentoEntrada VIE 	= new ValorizarInstrumento.ValorizarInstrumentoEntrada();
		ValorizarInstrumento.ValorizarInstrumentoSalida VIS 	= new ValorizarInstrumento.ValorizarInstrumentoSalida();
		
		ValorizarInstrumento.ObjEntradaInstrumentos EI = new ValorizarInstrumento.ObjEntradaInstrumentos();
		
		EI.IdValorizacion 					= 1L;
		EI.OrigenProcesamiento 				= "FACTURACION";
		EI.FechaValorizacion 				= new SimpleDateFormat("yyyy-MM-dd").format(comprobanteDetalle.FechaLiquidacion);
		EI.FechaOperacion 					= new SimpleDateFormat("yyyy-MM-dd").format(comprobanteDetalle.FechaComprobanteDetalle);
		EI.IdClasificacionTipoInstrumento 	= comprobanteDetalle.IdClasificacionTipoInstrumento; 
		EI.IdTipoInstrumento 				= comprobanteDetalle.IdTipoInstrumento; 
		EI.IdInstrumento 					= comprobanteDetalle.IdInstrumento;
		EI.Nemotecnico 						= comprobanteDetalle.NemotecnicoInstrumento;
		EI.FechaVencimiento 				= new SimpleDateFormat("yyyy-MM-dd").format( comprobanteDetalle.movimientoComprobanteDetalles.get( 0 ).FechaVencimientoInstrumento );
		EI.IdMonedaInstrumento 				= comprobanteDetalle.movimientoComprobanteDetalles.get( 0 ).IdMoneda;
		EI.IdMonedaEmisionInstrumento 		= comprobanteDetalle.movimientoComprobanteDetalles.get( 0 ).IdMonedaEmision;
		EI.IdTipoReajusteInstrumento 		= comprobanteDetalle.movimientoComprobanteDetalles.get( 0 ).IdTipoReajuste;
		EI.Emisor 							= comprobanteDetalle.movimientoComprobanteDetalles.get( 0 ).IdEmisor;
		EI.IdCondicionDeLiquidacion 		= comprobanteDetalle.movimientoComprobanteDetalles.get( 0 ).IdCondicionDeLiquidacion;
		EI.Cantidad 						= comprobanteDetalle.Cantidad; 
		EI.ValorRescate 					= null; 
		EI.PrecioCompra 					= comprobanteDetalle.Precio; 
		EI.TasaCompra 						= null;
		EI.MontoCompra 						= null;
		EI.TablaOrigen 						= "CARTERA";
		
		VIE.p_IdEmpresa	= idEmpresa;		
		VIE.p_IdUsuario	= idUsuario;
		VIE.p_IdModulo	= idModulo;
		VIE.p_IdPais	= Long.valueOf( paisSistema );
		VIE.p_OrigenProcesamiento = "FACTURACION";
		VIE.p_Instrumentos.add( EI );
		VIS = VI.logica(con, VIE);
		
		if ( VIE != null && VIS.P_OUT_ERRORESCONTADOR.equals( 0L ) ) {
			if ( VIS.P_OUT_INSTRUMENTOSVALORIZADOS.size( ) > 0 ) {
				comprobanteDetalle.Tasa = VIS.P_OUT_INSTRUMENTOSVALORIZADOS.get( 0 ).Tasa == null ? BigDecimal.ZERO : VIS.P_OUT_INSTRUMENTOSVALORIZADOS.get( 0 ).Tasa;
			}
		}
		return comprobanteDetalle.Tasa;
	}
	
	private String obtieneParametroCobroMXsobrePagoMX(Connection conn, Long idEmpresa) {
		logger.trace("INICIO::CrearComprobante.obtieneParametroCobroMXsobrePagoMX");
		long inicio = System.currentTimeMillis();
		String cobroMXsobrePagoMX = "S";
		try {
			cobroMXsobrePagoMX = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "FACTURACION", "CobroMXsobrePagoMX");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CrearComprobante.obtieneParametroCobroMXsobrePagoMX::Parametro CobroMXsobrePagoMX no existe.");
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.obtieneParametroCobroMXsobrePagoMX", fin-inicio);

		return cobroMXsobrePagoMX;
	}
	
	private String obtieneParametroProductosSinCobroMXsobrePagoMX(Connection conn, Long idEmpresa) {
		logger.trace("INICIO::CrearComprobante.obtieneParametroProductosSinCobroMXsobrePagoMX");
		long inicio = System.currentTimeMillis();
		String productosSinCobroMXsobrePagoMX = "";
		try {
			productosSinCobroMXsobrePagoMX = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "FACTURACION", "ProductosSinCobroMXsobrePagoMX");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CrearComprobante.obtieneParametroProductosSinCobroMXsobrePagoMX::Parametro ProductosSinCobroMXsobrePagoMX no existe.");
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearComprobante.obtieneParametroProductosSinCobroMXsobrePagoMX", fin-inicio);

		return productosSinCobroMXsobrePagoMX;
	}
	
	public static void main(String[] args) throws Exception {		
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa2","connections.properties")){
			Gson gson = Utils.getGsonBuilder();					

			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"FAC\",\r\n"
					+ "    \"p_simularOperacion\": \"N\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_EsIngresoAutomatico\": \"N\",\r\n"
					+ "    \"p_Glosa\": \"\",\r\n"
					+ "    \"P_MOVIMIENTOFACTURAR\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 12497,\r\n"
					+ "            \"IdCliente\": 11539,\r\n"
					+ "            \"IdMovimientoComprobante\": 12358282,\r\n"
					+ "            \"IdTipoComprobante\": \"FAE\",\r\n"
					+ "            \"FechaComprobante\": \"2025-07-02\"\r\n"
					+ "        },\r\n"
					+ "    ]\r\n"
					+ "}";
			
				CrearComprobante_Entrada Objin = gson.fromJson(json, CrearComprobante_Entrada.class);			
				CrearComprobante crearComp = new CrearComprobante();

				JsonParser parser 		= new JsonParser();
		        JsonObject dataJsonIn	= parser.parse(json).getAsJsonObject();
				JsonObject salida = crearComp.call(con, dataJsonIn);
				System.out.println(salida);
				con.rollback();
		} catch (Exception e) {
			e.printStackTrace();			
		}
		System.out.println("== END ==");
	};

};