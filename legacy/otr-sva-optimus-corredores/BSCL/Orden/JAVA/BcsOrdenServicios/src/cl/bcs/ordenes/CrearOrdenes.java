package cl.bcs.ordenes;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import cl.bcs.ordenes.CalcularCantidadVisible.EntradaCalcularCantidadVisible;
import cl.bcs.ordenes.CalcularCantidadVisible.SalidaCalcularCantidadVisible;
import cl.bcs.ordenes.CalcularCoberturaOrdenes.EntradaCoberturasOrdenes;
import cl.bcs.ordenes.CalcularCoberturaOrdenes.InCoberturaSimulacion;
import cl.bcs.ordenes.CalcularCoberturaOrdenes.OutCoberturaPortafolio;
import cl.bcs.ordenes.CalcularCoberturaOrdenes.OutCoberturaSimulacion;
import cl.bcs.ordenes.CalcularCoberturaOrdenes.SalidaCoberturasOrdenes;
import cl.bcs.ordenes.CalcularExigibilidadOrdenes.EntradaExigibilidadOrdenes;
import cl.bcs.ordenes.CalcularExigibilidadOrdenes.SalidaExigibilidadOrdenes;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjERRORESDETALLE;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjORDENES;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ServiceSalida;
import cl.bcs.ordenes.CrearGarantiasOrdenes.CrearGarantiasOrdenes_Entrada;
import cl.bcs.ordenes.CrearGarantiasOrdenes.CrearGarantiasOrdenes_Salida;
import cl.bcs.ordenes.CrearGarantiasOrdenes.GarantiaCtaInversion_Entrada;
import cl.bcs.ordenes.CrearGarantiasOrdenes.GarantiaCustodia_Entrada;
import cl.bcs.ordenes.NotificarEstadoOrden.NotificarEstadoOrden_Entrada;
import cl.bcs.ordenes.RutearOrdenes.RutearOrdenes_Entrada;
import cl.bcs.ordenes.pojo.CobroOrden;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.ordenes.pojo.FormaDePagoOrden;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.ordenes.pojo.LibroOrdenes;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLADETALLE; 
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro;
import cl.bcs.plataforma.gta.CalcularExigibilidad.ObjExigibilidadEntrada;
import cl.bcs.plataforma.gta.CalcularExigibilidad.ObjExigibilidadPortafolio;
import cl.bcs.plataforma.gta.CalcularExigibilidad.ObjExigibilidadSimulacion;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion.ConsultarInstrumentoNegociacion_Entrada;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion.DatosInstrumento;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion.DatosInstrumentoNegociacion;
import cl.bcs.plataforma.ins.ConsultarNemoNegociacion.DatosInstrumentoNegociacion_Salida;
import cl.bcs.plataforma.lim.EvaluacionLimite;
import cl.bcs.plataforma.lim.EvaluacionLimite.ObjTABLAOPERACION;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.BarraProgresoServicio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.DiasAConsultar;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.DiasAConsultarSalida;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosEntradaServicio;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosSalidaServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ConsultaEstadoCntBC;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
 
public class CrearOrdenes implements JavaCall,JavaCallMetadata {
	private static final Logger logger = LogManager.getLogger(CrearOrdenes.class);
	//Identifica cada corrida de CrearOrdenes dentro de la tabla de paso CrearOrdenes_Proceso (GLOBAL TEMPORARY),
	//para blindar el caso donde un servicio superior invoca CrearOrdenes mas de una vez en la misma sesion/transaccion
	//(sin commit de por medio) y la tabla todavia no fue limpiada por ON COMMIT DELETE ROWS.
	public static final AtomicLong CONTADOR_ID_TRABAJO = new AtomicLong(0);

	private final String SQL_ACTUALIZA_PRECIO_OPCION = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_ActualizaPrecioOpcion.sql");
	private final String SQL_INSERT_ORDENES_WRAPPER = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_InsertOrdenesWrapper.sql");
	private final String SQL_INSERT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_InsertOrdenes.sql");
	private final String SQL_INSERT_COBROS = loadResourceToString("cl/bcs/ordenes/sql/Globales_InsertCobros.sql");
	private final String SQL_INSERT_COBROS_PERU = loadResourceToString("cl/bcs/ordenes/sql/Globales_InsertCobrosPeru.sql");	
	private final String SQL_INSERT_COBROS_RANGOS_PERU = loadResourceToString("cl/bcs/ordenes/sql/Globales_InsertCobrosRangosPeru.sql");
	private final String SQL_INSERT_FORMA_DE_PAGO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_InsertFormaDePago.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectOrdenes.sql");
	private final String SQL_SELECT_PARAMETROS_VENTA_CORTA = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectParametrosCustodiaVentaCorta.sql");
	private final String SQL_SELECT_REQUISITOS_OPERATIVOS = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectRequisitosOperativos.sql");
	private final String SQL_SELECT_INDICADORES_CLIENTE = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectIndicadoresCliente.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_LIBROS_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectLibroOrdenes.sql");
	private final String SQL_SELECT_LIBRO_POR_DEFECTO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectLibroPorDefecto.sql");
	private final String SQL_SELECT_ESTADO_MODULO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectPreferenciaPago.sql");
	private final String SQL_SELECT_ORDEN_EXISTENTE = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectOrdenExistente.sql");
	private final String SQL_SELECT_LINEA_SOBREGIRO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectLineaSobregiro.sql");
	private final String SQL_SELECT_PORCENTAJE_CASTIGO_INSTRUMENTO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_PorcentajeCastigoInstrumento.sql");
	private final String SQL_SELECT_TIPO_LIQUIDACION_DEFECTO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectTipoLiquidacion.sql");
	private final String SQL_SELECT_PAIS_RESIDENCIA_PRINCIPAL_CLIENTE = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectPaisResidenciaPrincipalCliente.sql");
	private final String SQL_SELECT_ULTIMO_CAMBIO_FECHA = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectCambioFechaPauta.sql");
	private final String SQL_CALL_BLOQUEO_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_CallBloqueoOrden.sql");
	private final String SQL_SELECT_BLOQUEO_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectBloqueoOrden.sql");
	private final String SQL_CALL_DELETE_BLOQUEO_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_CallDeleteBloqueoOrden.sql");
	private final String SQL_DELETE_BLOQUEO_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_EliminarListaOrdenes.sql");
	private final String SQL_SELECT_TABLA_VELA = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectVela.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_ = loadResourceToString("cl/bcs/ordenes/sql/ConsultarFormaDePagoOrden_SelectFormaDePago.sql");
	private final String SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectParametroOperaConSaldoNegativo.sql");
	private final String SQL_SELECT_PREFERENCIAS_DIGITADOR = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectPreferenciasEmpleadoDigitador.sql");
	
	private final String IdTipoPrestamoVentaCorta = "PRTVTC";
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	public static class CrearOrdenesEntrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public BarraProgreso				p_BarraProgreso;
		public List<ObjGarantiasIn>			p_Garantias;
		public List<ObjCobrosModificados>	p_CobrosModificados;
		public List<Cobro_Fijo>				p_CobrosFijos;
		public List<ObjOrdenesEntrada>		p_Ordenes;
		
		public CrearOrdenesEntrada() {
			p_Garantias = new ArrayList<ObjGarantiasIn>();
			p_CobrosModificados = new ArrayList<ObjCobrosModificados>();
			p_Ordenes = new ArrayList<ObjOrdenesEntrada>();
			p_CobrosFijos = new ArrayList<Cobro_Fijo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Fijo {
		public Long IdInterno; 
		public Long IdInternoCobro;
		public Long IdPortafolio; 
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro; 
		public String FormaCobro; 
		public BigDecimal ValorTopeMinimoOrden; 
		public String IdMoneda; 
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearOrdenesSalida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<ObjCOBROS>		P_OUT_COBROS;
		public List<ObjGARANTIAS>	P_OUT_GARANTIAS;
		public List<ObjRESULTADO>	P_OUT_RESULTADO;
		
		public CrearOrdenesSalida() {
			this.P_OUT_COBROS = new ArrayList<ObjCOBROS>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_GARANTIAS = new ArrayList<ObjGARANTIAS>();
			this.P_OUT_RESULTADO = new ArrayList<ObjRESULTADO>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjGarantiasIn {
		public Long			IdInstrumento;
		public Long			IdInstrumentoGarantizado;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public BigDecimal	Cantidad;
		public BigDecimal	Monto;
		public BigDecimal	Precio;
		public String		IdBolsa;
		public String		IdMoneda;
		public String		IdTipoGarantia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjCobrosModificados {
		public Long			IdInterno;
		public String		IdCobro;
		public BigDecimal	ValorCobroAplicado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjOrdenesEntrada {
		public Long			IdTrabajo;
		public Long			IdInterno;
		public String		FechaRecepcion;
		public String		FechaVigencia;
		public String		FechaEmision;
		public String		FechaOperacion;
		public String		IdTipoVigencia;
		public String		Glosa;
		public BigDecimal	Cantidad;
		public BigDecimal	MontoNeto;
		public BigDecimal	ValorRescate;
		public String		IdBoveda;
		public String		IdFormaCustodia;
		public Long			IdPorcentajeVisibilidad;
		public BigDecimal	PrecioLimite = BigDecimal.ZERO;;
		public String		EsFacturaInmediata;
		public String		IdTipoPrecio;
		public Long			IdPortafolio;
		public String		IdTipoLibroOrden;
		public Long			IdLibroOrdenes;
		public String		IdTipoCanalIngreso;
		public String		IdTipoReajuste;
		public String		IdEmisor;
		public String		IdBolsa;
		public String		EspecificaBolsa;
		public String		IdMonedaLiquidacionOrden;
		public String 		IdMonedaOrden; // solo para IF
		public BigDecimal	TipoCambio;
		public Long			IdInstrumento;
		public Long			IdInstrumentoSubyacente;
		public String		IdCondicionDeLiquidacionPlazo;
		
		public BigDecimal	TasaVentaCorta = BigDecimal.ZERO;
		public BigDecimal	TasaSimultanea = BigDecimal.ZERO;
		public BigDecimal	TasaRFIF = BigDecimal.ZERO;
		
		public Long			PlazoIF = 0L;
		public Long			PlazoSimultanea = 0L;
		public Long			PlazoVentaCorta = 0L;
		
		public String		EsVentaCorta = "N";            /////////////// en duro ?
		public String		EsOD = "N";
		public String		EsFungible = "N";
		public Long			IdCorredorCorresponsal;
		public Long			IdPersonaAutorizadoDarOrdenes;
		public String		IdTipoCanalDistribucion;
		public BigDecimal	TasaReporte = BigDecimal.ZERO;
		public Long			PlazoReporte = 0L;
		public Long			BaseTasa = 0L; 
		public String		TipoInteres; //C o S (Compuesto Simple) 
		public String 		IdCondicionDeLiquidacion;
		public String 		IdTipoOperacion;
		public Long 		IdCuentaCustodioPortafolio;
		public String		EsPorMonto = "N";
		public String		EsOrdenEspecial = "N";
		public String		PagoCobroCuentaInversion;
		public Long			IdPreferenciaPagoPortafolio;
		public String		IdMecanismoCaptacion = "";
		public BigDecimal	TasaPromedioReporte = BigDecimal.ZERO;
		public BigDecimal	TasaReportanteReferencia = BigDecimal.ZERO;
		public String 		NemotecnicoSVS = "";
		
		public String 		IdSistemaBursatil;
		public String 		InstrumentoAcreditado = "N";
		
		public String		FormadorMercado = "N";
		public String		EsClienteFinal;
		public String		GeneraSpot = "N";
		
		public BigDecimal	MontoInteresContado = BigDecimal.ZERO;
		public BigDecimal	MontoInteresPlazo = BigDecimal.ZERO;
		public BigDecimal 	PorcentajeAllIn;
		public String 		FechaConfirmacionPlazo;
		public Long			IdOrdenReferencia;
		public String 		TipoLiquidacionFactura;
		public String		CodigoModoFacturacionPortafolio;
		public Date         FechaVencimiento;
		public String 		CodigoEjecutivoDigitador = "";
					
		public String		IdBloqueaOrden; //id para eliminar de la tabla temporal de bloqueo de ordenes
		
		public String		IngresoDesde = "";

		public String GlosaRefactura = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public  ObjOrdenesEntrada(){
			 this.FechaConfirmacionPlazo = "0001-01-01";
		}
	}

	public static class ObjCOBROS {
		public Long			IdInterno;
		public Long			IdOrden;
		public String		CodigoCobroClasificacion;
		public String		IdCobro;
		public String		IdMonedaCobro;
		public BigDecimal	ValorCobroPerfil;
		public String		FormaCobro;
		public BigDecimal	MontoMinimo;
		public BigDecimal	ValorCobroAplicado;
		public BigDecimal	MontoCobroCalculado;
		public BigDecimal	MontoCobroCalculadoLocal;
		public BigDecimal	MontoCobroCalculadoLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjGARANTIAS {
		public Long			FolioGarantia;
		public String		IdMonedaInstrumento;
		public String		Instrumento;
		public BigDecimal	Cantidad;
		public BigDecimal	Monto;
		public String		IdMoneda;
		public String		IdTipoGarantia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjRESULTADO {
		public Long			IdInterno;
		public Long			IdOrden;
		public String		DocumentoIdentidad;
		public String		CodigoPortafolio;
		public Timestamp	FechaOperacionOrden;
		public BigDecimal	Cantidad;
		public BigDecimal	MontoNeto;
		public BigDecimal	MontoLiquidacionOperacion;
		public BigDecimal	MontoLiquidacionOperacionPlazo;
		public BigDecimal	TotalCobros;
		public BigDecimal	MontoTotal;
		public BigDecimal	PrecioOrden;
		public String		Instrumento;
		public String		InstrumentoNegociacion;
		public String		InstrumentoSubyacente;
		public String		NombreTipoOperacion;
		public String		IdTipoOperacion;
		public String		IdMonedaOrden;
		public String		IdMonedaLiquidacionOrden;
		public String		EsVentaCorta;
		public String		AccionOrden;
		public String		IdCondicionDeLiquidacion;
		public String		NombreCondicionDeLiquidacion;
		public String		IdTipoPrecio;
		public String		NombreTipoPrecio;
		public String		IdTipoVigenciaOrden;
		public String		NombreTipoVigenciaOrden;
		public Date			FechaVigencia;
		public Long			Plazo;
		public String		NombreTipoInstrumento;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public String		EsOD;
		public String		NombreCompleto;
		public BigDecimal	TasaVentaCorta;
		public BigDecimal	PlazoVentaCorta;
		public Date			FechaVencimientoVentaCorta;
		public String		NombreEjecutivoPortafolio;
		public BigDecimal	TasaReporte;
		public BigDecimal	TasaPromedioReporte;
		public Integer		PlazoReporte;
		public Long			BaseTasa;
		public BigDecimal	TasaSimultanea;
		public Long			PlazoSimultanea;
		public Long			IdPreferenciaPagoPortafolio;
		public BigDecimal	PrecioPlazo;
		public BigDecimal	MontoNetoPlazo;
		public BigDecimal	MontoTotalPlazo;
		public BigDecimal	MontoLiquidacionPlazo;
		public BigDecimal	MontoInteresContado;
		public BigDecimal	MontoInteresPlazo;
		public String 		TipoCanalDistribucion;
		
		public String GlosaRefactura = "";
		public String FechaOperacionOriginal = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcFoliacionOrden{
		public Long 		IdOrden;
		public String		Folio;
		public String		TipoFolio;
		public ProcFoliacionOrden(){}
		public ProcFoliacionOrden(Long IdOrden){
			this.IdOrden = IdOrden;
		}
	}
	
	public static class PorcentajeCastigo {
		public BigDecimal	PorcentajeCastigo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OrdenEliminar{
		public String IdOrden;
	}

	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearOrdenesEntrada in = gson.fromJson(jin.toString(), CrearOrdenesEntrada.class);
		CrearOrdenesSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		this.setMetadata(in, out);
		return dataout;
	}

	@Override
	public void setMetadata(Object in,Object out) {
		CrearOrdenesSalida salida = (CrearOrdenesSalida)out;
		metadata[0] = salida.P_OUT_RESULTADO.toString();
	}
	
	public static class Vela {
		public Long			IdVela;
		public BigDecimal	PrecioMin;
		public BigDecimal	PrecioMax;
		public BigDecimal	FactorDecimal;	
		public String	    Multiplo;
	    public String		DecimasNoEntero;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*
	 * Logica Servicio
	 * 
	 */
	public static class OrdenProceso extends BcsPojo{
		public Long 	IdInterno;
		public Long 	IdOrden;
		public Long 	IdCliente;
		public Long 	IdPortafolioCliente;
		public String	CodigoPortafolio;
		public Long 	IdInstrumento;
		public Long 	IdInstrumentoNegociacion;
		public String 	InstrumentoNegociacion;
		public java.sql.Timestamp FechaOperacion;
		public String 	IdBolsa;
		public Long 	IdLibroOrdenes;
		public String 	IdTipoOperacion;
		public String 	IdTipoCanalIngreso;
		public String 	IdCondicionDeLiquidacion;
		public Long 	IdPorcentajeVisibilidad;
		public String 	IdTipoVigenciaOrden;
		public String 	IdBoveda;
		public String 	IdFormaCustodia;
		public Long 	IdEmpleadoPortafolio;
		public String 	IdTipoPrecio;
		public String 	IdTipoInstrumento;
		public String 	IdTipoCanalDistribucion;
		public Long 	IdEmpleadoDigitador;
		public Long 	IdInstrumentoCustodia;
		public String 	IdEstadoInstrumento;
		public Long 	IdInstrumentoReal;
		public String 	NombreTipoInstrumento;
		public BigDecimal TamanoCorte;
		public String 	IdMonedaInstrumento;
		public String 	NombreClasificacionTipoInstrumento;
		public Long 	TamanoContrato;
		public String 	IdTipoOperacionReal;
		public String 	EspecificaBolsa;
		public String 	EsVentaCorta;
		public String 	NombreTipoOperacion;
		public String 	AccionOrden;
		public String 	IdBolsaReal;
		public java.sql.Date FechaEmision;
		public String 	RuteoGeneraComprobanteFacturacionAutomatico;
		public String 	IndicadorFacturacionInmediata;
		public Long 	IdInstrumentoSubyacente;
		public Long 	IdPersonaAutorizadoDarOrdenes;
		public String 	RuteoGeneraMovimientoFacturacionAutomatico;
		public String 	IdCondicionDeLiquidacionContadoReal;
		public String 	NombreCondicionDeLiquidacion;
		public String 	IdTipoVigenciaReal;
		public String 	NombreTipoVigenciaOrden;
		public String 	IdBovedaReal;
		public String 	IdFormaCustodiaReal;
		public Long 	IdPorcentajeVisibilidadReal;
		public String 	IdTipoPrecioReal;
		public String 	NombreTipoPrecio;
		public String 	IdTipoCanalIngresoReal;
		public Long 	DiasLiquidacionContado;
		public String 	IdTipoReajusteReal;
		public String 	InstrumentoSubyacente;
		public String 	InstrumentoPermiteVentaCorta;
		public String 	InstrumentoPermiteSimultanea;
		public String 	IdTipoCanalDistribucionReal;
		public BigDecimal MontoMaximoDistribucionOrden;
		public String 	ValidaHorario;
		public String 	AplazaOrdenPorHorarioNegociacion;
		public String 	HorarioPreMarketInicio;
		public String 	HorarioPreMarketFin;
		public String 	HorarioMarketInicio;
		public String 	HorarioMarketFin;
		public String 	IdEstadoModulo;
		public String 	NombreModulo;
		public String 	AplazaOrdenPorModuloCerrado;
		public java.sql.Date FechaLiquidacion;
		public String 		IdClasificacionTipoInstrumento;
		public Long 		IdCorredorCorresponsal;
		public String 		IdProducto;
		public Long 		IdGrupoCobro;
		public BigDecimal 	ValorRescate;
		public BigDecimal 	Cantidad;
		public BigDecimal	MontoOrden;
		public BigDecimal	PrecioOrden;
		public String 		EsFungible;
		public String 		Nemotecnico;
		public BigDecimal 	PrecioMercado = BigDecimal.ZERO;
		public String 		IdMonedaOrden;
		public BigDecimal 	TipoCambio;
		public java.sql.Timestamp FechaRecepcion;
		public String 		IdMonedaLiquidacionOrden;
		public BigDecimal	MontoLiquidacion = BigDecimal.ZERO;
		public BigDecimal 	MontoLocal = BigDecimal.ZERO;
		public String 		IdEmisor;
		public String 		IdTipoReajuste;
		public String 		IdEstadoOrden;
		public java.sql.Timestamp FechaAceptacion;
		public java.sql.Date FechaAutorizacion;
		public Long 		IdEmpresa;
		public String 		CodigoClaseOrden = "";
		public Long 		IdPortafolioReal;
		public String 		IdEstadoPortafolio;
		public String 		IdEstadoAutorizacionPortafolio;
		public Long 		IdPersonaCliente;
		public java.sql.Date FechaVigencia;
		public String 		IdEstadoPersona;
		public Long 		IdClienteReal;
		public String 		IdEstadoCliente;
		public String 		DocumentoIdentidad;
		public String 		NombreCompleto;
		public String 		IdPerfilCobros;
		public String 		TipoDocumentoIdentidad;
		public String 		NombreEjecutivoPortafolio;
		public String 		IdEstadoEmpleadoEjecutivoPortafolio;
		public String 		IndicadorInstrumentoAcreditado;
		public String 		InstrumentoAcreditado;
		public java.sql.Date FechaConfirmacionPlazo; //marca
		public java.sql.Timestamp FechaConfirmacion; 
		public String		IdEstadoConfirmacion;
		public Long	IdOrdenReferencia;
		
		public String 		ValidaSaldoCustodia;
		public String 		CumpleHorario;
		public BigDecimal 	MontoPrimaVentaCorta = BigDecimal.ZERO;
		
		/* VALIDACIONES */
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
		
		public BigDecimal 	TasaReporte;
		public Long 		PlazoReporte;
		public Long 		BaseTasa = 0L;
		
		List<CobroOrdenProceso> cobros = new ArrayList<CobroOrdenProceso>();
		public Long			cantidadCobrosPortafolio = 0L;
		public String 		IdEstadoEmpleadoDigitador;
		public String 		PlantillaEmpleadoPermiteRuteo;
		public String 		PlantillaEmpleadoProductoHabilitado;
		public BigDecimal 	PlantillaEmpleadoMaximoDescuentoComision;
		public BigDecimal 	PlantillaEmpleadoMontoMaximoOperacion;
		
		public BigDecimal 	TotalCobrosLiquidacion = BigDecimal.ZERO;
		public BigDecimal 	TotalCobrosLiquidacionPlazo = BigDecimal.ZERO;
		public BigDecimal	TotalCobrosLocal = BigDecimal.ZERO;
		public BigDecimal 	MontoTotalLiquidacion = BigDecimal.ZERO;
		public BigDecimal 	MontoTotalLiquidacionPlazo = BigDecimal.ZERO;
		
		//para saldo caja
		public BigDecimal	TotalMontoPortafolio = BigDecimal.ZERO;
		public BigDecimal 	SaldoCajaPortafolio = BigDecimal.ZERO;
		public BigDecimal 	LineaSobregiroPortafolio = BigDecimal.ZERO;
		//para saldo custodia
		public BigDecimal 	TotalCantidadCustodiaPortafolio = BigDecimal.ZERO;
		public BigDecimal	TotalValorRescateCustodiaPortafolio = BigDecimal.ZERO;
		public BigDecimal 	SaldoCantidadCustodiaPortafolio = BigDecimal.ZERO;
		public BigDecimal 	SaldoValorRescateCustodiaPortafolio = BigDecimal.ZERO;
		public BigDecimal 	SaldoCantidadVentaDescubiertaCustodiaPortafolio = BigDecimal.ZERO; 
		
		public String 		Glosa;
		public String 		GlosaRefactura = "";
		public Long 		PlazoVentaCorta;
		public Long 		PlazoSimultanea;
		public BigDecimal 	TasaVentaCorta;
		public BigDecimal 	TasaSimultanea;
		public BigDecimal 	TasaRFIF;
		public Long 		PlazoIF;
		public Long 		IdSucursalEmpleadoDigitador;
		public Long 		IdSucursalEmpleadoPortafolio;
		public Date 		FechaVencimientoVentaCorta;
		public Date			FechaVencimientoSimultanea;
		public Date 		FechaVencimientoIF;
		public String 		PortafolioProductoHabilitado = "N";
		public String 		PortafolioProductoPermiteRuteo = "N";
		public BigDecimal 	PrecioPlazo = BigDecimal.ZERO;
		public BigDecimal 	MontoOrdenPlazo = BigDecimal.ZERO;
		public Date 		FechaVencimientoReporte;
		public BigDecimal 	MontoLiquidacionPlazo = BigDecimal.ZERO;
		public BigDecimal 	MontoLocalPlazo = BigDecimal.ZERO;
		public String 		IdTipoLibroOrden;
		public Long 		IdCuentaCustodioPortafolio;
		public String		EsPorMonto = "N";
		public String		EsOrdenEspecial = "N";
		public BigDecimal	MontoTotalLocal;
		public String 		ValidaSaldoCaja = "N";
		
		public String		PagoCobroCuentaInversion;
		public Long			IdPreferenciaPagoPortafolio;
		public String 		TipoInteres;
		public String 		CodigoProcedenciaOrden = "";
		public String		Folio = "";
		public Date 		FechaSistemaCreacion;
		public BigDecimal	TasaPromedioReporte = BigDecimal.ZERO;
		public BigDecimal	TasaReportanteReferencia = BigDecimal.ZERO;
		public String 		NemotecnicoSVS;
		
		public String 		IdSistemaBursatil;
		public String 		ExisteOrdenMismaOperacionInstrumento = "N";
		public String 		IdTipoCliente;
		public Long 		IdPersonaBroker;
		public String 		EsBroker;
		public String 		ModalidadSMV;
		public String		MercadoSMV;
		
		public String		FormadorMercado;
		
		public String 		IdEstadoInterno;
		public String 		IdEstadoLimite;
		public String		GeneraSpot;
		
		public BigDecimal	MontoAllIn;
		public BigDecimal	PorcentajeAllIn;
		public Date 		FechaVencimientoInstrumento;
		public BigDecimal	MontoInteresContado;
		public BigDecimal	MontoInteresPlazo;
		public Date 		FechaVencimientoInstrumentoSubyacente;
		public String		EsClienteFinal;
		public BigDecimal	MontoCoberturaExigibilidadEntidad = BigDecimal.ZERO;
		public BigDecimal	MontoCoberturaExigibilidadCorredora = BigDecimal.ZERO;
		public String 		CumpleParaVentaDescubierta = "N"; 
		public List<MensajeSalida>	MensajesProceso = new ArrayList<>(); //agregados para manejar mensajes de errores, advertencias y autorizaciones 
		
		public String 		IdCategoriaInversionista;
		public String 		AplazaSoloHora = "N";
		public java.sql.Date       FechaSaldoCaja;
		public String 		TipoLiquidacionFactura;
		public String		CodigoModoFacturacionPortafolio;
		public String		IdBloqueaOrden; //id para eliminar de la tabla temporal de bloqueo de ordenes
		
		public String		IngresoDesde;
		public String 		EsInstrumentoCalificado;
		
		public String 		EsRaiz;
		public String		CodigoEjecutivoDigitador = "";
		
		public String		OperaConSaldoNegativo = "N";
		
		public OrdenProceso() {
			this.TipoLiquidacionFactura = "";
			this.CodigoModoFacturacionPortafolio = "";
		}
	}
	
	public static class PreferenciaPagoPortafolio {
		public Long			IdPreferenciaPagoPortafolio;
		public String		IdMedioDePago;
		public Long			IdCuentaBancaria;
		public String		IdTipoCuentaBancaria;
		public String		NumeroCuentaBancaria;
		public Long			IdTarjetaBancaria;
		public String		IdTipoTarjetaBancaria;
		public String		NumeroTarjetaBancaria;
		public String		IdModalidadDePago;
		public Long			IdBanco;
		
	}
	
	public static class CobroOrdenProceso {
		public String 		CodigoCobroClasificacion;
		public String 		CodigoCobroClasificacionBase;
		public String 		FormaCobro;
		public String 		IdCobro;
		public Long 		IdInterno;
		public String 		IdMoneda;
		public OrdenProceso Orden;
		public String 		IdOrigenCobro;
		public BigDecimal 	MontoMinimo;
		public String 		NombreCobro;
		public String 		PermiteCambiarValor;
		public BigDecimal 	ValorCobroPerfil;
		public BigDecimal 	ValorCobroAplicado;
		public BigDecimal 	ValorCobroFinal;
		public BigDecimal 	MontoCobroCalculado;
		public BigDecimal	MontoCobroCalculadoLocal;
		public BigDecimal	MontoCobroCalculadoLiquidacion;
		public Long 		IdInternoCobro;
		
		//CAMPOS PERU
		public String			CheckCobroMonedaOperacion;
		public String			IdMonedaCobroOtraMoneda;
		public BigDecimal		MontoFijo_MontoCobro;
		public String			MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal		MontoFijo_ValorMontoOperacionMenor;
		public String			MontoFijo_CheckSiNo;
		public BigDecimal		MontoFijo_ValorMontoSiNo;
		public BigDecimal		MontoFijoPorUnidad_MontoCobro;
		public String			MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal		PorcentualFijo_ValorCobro;
		public String			PorcentualFijo_CheckSobreMonto;
		public String			PorcentualFijo_CheckSobreCantidad;
		public String			PorcentualFijo_CheckMayorEntreDos;
		public String			PorcentualFijo_CheckMenorEntreDos;
		public String			PorcentualFijo_CheckMontoMinimo;
		public BigDecimal		PorcentualFijo_ValorMontoMinimo;
		public String			PorcentualFijo_CheckMontoMaximo;
		public BigDecimal		PorcentualFijo_ValorMontoMaximo;
		public String			PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal		PorcentualTasaPlazo_ValorCobro;
		public String			Otros_CheckAfectoImpuesto;
		public String			Otros_CheckPermiteAumentarValor;
		public String			Otros_CheckPermiteDisminuirValor;
		public String			Otros_CheckOperacionContado;
		public String			Otros_CheckOperacionPlazo;
		public String			Otros_CheckAllIn;
		public String			Otros_CheckInteresCompuesto;
		public String			Otros_CheckInteresSimple;
		public List<DefinicionRangoCobro> RangoCobros = new ArrayList<DefinicionRangoCobro>();
		public String PorcentualRango_IdMonedaRangoMonto;
		public String PorcentualRango_IdTipoRangoCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RequisitoOperativoCrearOrden {
		public String IdTipoCanalIngreso;
		public String CO_ContratoSimultanea;
		public String CO_ContratoCustodia;
		public String CO_PortafolioBloqueado;
		public String CO_ContratoFuturos;
		public String CO_ContratoVentaCortaRV;
		public String CO_ContratoMercantil;
		public String CO_CompraNominalRV;
		public String CO_CompraNominalRFIF;
		public String CO_VentaNominalRV;
		public String CO_VentaNominalRFIF;
		public String CO_ConfirmacionOrdenVerbalEscrita;
		public String CO_ContratoCondicionesGenerales;
		public String CO_ClienteBloqueado;
		public String CO_PersonaBloqueada;
		public String CO_EjecutivoSinProductoPlantilla;
		public String CO_EjecutivoSinRuteoPlantilla;
		public String CO_EjecutivoPortafolioBloqueado;
		public String CO_OrdenRequiereAutorizacion;
		public String CO_FotocopiaCarnet;
		public String CO_AntecedentesLegales;
		public String CO_OperacionSinFlujoCustodia;
		public String CO_MontoOperacionSuperaPermitidoPlantilla;
		public String CO_ComisionSuperaMaximoPermitido;
		public String CO_ComisionBajoMinimoPermitido;
		public String CO_MontoSuperaMaximoPermitidoCanalDistribucion;
		public String CO_DescuentoComisionSuperaPermitidoPlantilla;
		public String CO_OrdenPermanentePrecioMercado;
		public String CO_OperacionSinFlujoCaja;
		public String CO_DiferenciaPrecioOrden;
		public String CO_MecanismoCaptacion;
		public String CO_MonedaInstrumentoDiferenteMonedaLiquidacion;
		public String CO_OrdenMismaOperacionInstrumento;
		public String CO_RuteoPrecioMercado;
		public String CO_NoUsaPreferenciaNegocioOperaCuentaInversion;
		public String CO_ExigibilidadCorredora;
		public String CO_ExigibilidadEntidad;
		public String CO_PorcentajeCastigo;
		public String CO_OperacionSinFlujoVentaDescubierta;	
		public String CO_ContratoEmprestitos;
		public String CO_ContratoCanalInternet;
		public String CO_ContratoOpciones;
		public String CO_FlujoCantidadCustodiaInsuficiente;   	//Flujo custodia Insufuciente
		public String CO_MontoMinimoCrearOrden;   		
		public String CO_MontoMinimoCrearOrdenAutorizar;
		public String CO_CobroMaxPermitido; 
		public String CO_PorcentajeMaxOrden;
		public String CO_PorcentajeVisibilidad;
		public String CO_ContratoDeclaracionCRSFatca;
		public String CO_InstrumentoCalificado;
		public String CO_MontoOrdenVentaMenorCostos;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadoresClienteCrearOrden {
		public Long IdCliente;
		public String ContratoSimultanea;
		public String ContratoFuturos;
		public String ContratoVentaCortaRV;
		public String ContratoCustodia;
		public String ContratoMercantil;
		public String ClaseOrden;
		public String ContratoCondicionesGenerales;
		public String FotocopiaCarnet;
		public String AntecedentesLegales;
		public String CodigoProcedenciaOrden;
		public String InstrumentoAcreditado;
		public String ContratoEmprestitos;
		public String ContratoDeclaracionCRSFatca;
		public String InversionistaCalificado;
		// OPCIONES
		public String ContratoOpciones;
		public String PortafolioDerivados;
		public String ContratoInternet;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LineaSobregiro {
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal MontoTotal;
	}
	
	public static class PaisResidenciaPrincipalCliente {
		public Long IdPersona;		
		public String EsPaisUs;
		public String EsOcde;
	}
	
	public static class ParametroVentaCorta {
		public String IdTipoPrestamo;
		public Long VentaCortaPlazoMinimo;
		public Long VentaCortaPlazoMaximo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroOperaCSN {
		public String OperaConSaldoNegativo;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CobroRango {
		public Long IdRangoCobro;
		public Long IdCobroOrden;
		public Long IdOrden;
		public Long IdGrupoCobro;
		public Long IdEmpresa;
		public String CodigoDefinicionCobro;
		public BigDecimal Hasta;
		public BigDecimal Valor;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class UltimoCambioFechaPauta {
		public Timestamp UltimoProcesoCambioFecha;
	}
	
	public static class EntradaPreferenciasEmpDigitador {
		public List<String> CodigosDig;
	}
	
	public static class PreferenciasEmpDigitador {
		public Long IdEmpleado;
		public String CodigoEmpleado;
		public Long IdSucursalEmpleadoDigitador;
		public String IdPlantillaEmpleado;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdEstado;
		public String PlantillaEmpleadoProductoHabilitado;
		public String PermiteRuteo;
		public BigDecimal PorcentajeDescuentoComision;
		public BigDecimal MontoMaximoOperacion;
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,OrdenProceso ord,HashMap<String, RequisitoOperativoCrearOrden> requisitos,List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		MensajeSalida objMensaje = new MensajeSalida(); //JALA para manejar mejora proceso de Autorizaciones
		if (esError) {
			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("DocumentoIdentidad", ord.DocumentoIdentidad);
			valores.put("CodigoPortafolio", ord.CodigoPortafolio);
			valores.put("NombreCompleto", ord.NombreCompleto);
			valores.put("DocumentoIdentidad", ord.DocumentoIdentidad);
			valores.put("Nemotecnico", ord.Nemotecnico);
			
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdInterno", ord.IdInterno)
				  .add("IdOrden", ord.IdOrden)
			      .add("IdCliente", ord.IdCliente)
				  .add("IdPortafolio", ord.IdPortafolioCliente)
				  .add("IdInstrumento", ord.IdInstrumento)
				  .add("IdBolsa", ord.IdBolsa)
				  .add("IdTipoOperacion", ord.IdTipoOperacion)
				  .add("IdCondicionDeLiquidacionContado", ord.IdCondicionDeLiquidacion)
				  .add("IdPorcentajeVisibilidad", ord.IdPorcentajeVisibilidad)
				  .add("IdTipoVigencia", ord.IdTipoVigenciaOrden)
				  .add("IdBoveda", ord.IdBoveda)
				  .add("IdFormaCustodia", ord.IdFormaCustodia)
				  .add("IdTipoPrecio", ord.IdTipoPrecio)				  
				  .add("PlazoVentaCorta", ord.PlazoVentaCorta)
				  .add("PlazoSimultanea", ord.PlazoSimultanea)
				  .add("TasaVentaCorta", ord.TasaVentaCorta)
				  .add("TasaSimultanea", ord.TasaSimultanea)
				  .add("DocumentoIdentidad", ord.DocumentoIdentidad)
				  .add("CombinacionCanalDistribucion", ord.IdTipoCanalDistribucion + "-" + ord.IdBolsa+ "-" + ord.IdTipoInstrumento)
				  .add("IdEmpleadoDigitador", ord.IdEmpleadoDigitador)
				  .add("FlujoCustodiaPortafolio", ord.SaldoCantidadCustodiaPortafolio)
				  .add("CodigoPortafolio", ord.CodigoPortafolio)
				  .add("NombreCompleto", ord.NombreCompleto)
				  .add("DocumentoIdentidad", ord.DocumentoIdentidad)
				  .add("Nemotecnico", ord.Nemotecnico)
				  .add("FlujoCustodiaVentaDescubierta", ord.SaldoCantidadVentaDescubiertaCustodiaPortafolio)
				  .add("IdMonedaLiquidacionOrden", ord.IdMonedaLiquidacionOrden)
				  .add("FlujoCajaPortafolio", MonedaFormato.formatear(con, ord.SaldoCajaPortafolio, MonedaFormato.MONTO, ord.IdMonedaLiquidacionOrden).valor)
				  .add("CodigoEjecutivoDigitador", ord.CodigoEjecutivoDigitador)
				  .msg();
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				objMensaje = new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				ord.MensajesProceso.add(objMensaje);
				ord.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION) && ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) {
				objMensaje = new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				ord.MensajesProceso.add(objMensaje);
				ord.errores++;						
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				objMensaje = new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				ord.MensajesProceso.add(objMensaje);
				ord.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				objMensaje = new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				ord.MensajesProceso.add(objMensaje);
				ord.advertencias++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_INFORMACION)) {
				objMensaje = new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_INFORMACION,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				ord.MensajesProceso.add(objMensaje);
				ord.advertencias++;
			}
			else {
				//si el comportamiento viene de requisitos operativos
				String requisito=obtenerValorRequisitoOperativo(requisitos, ord.IdTipoCanalIngreso, CodRequisito);
				if (requisito.equals(MensajeSalida.MENSAJE_ERROR) || requisito.equals(MensajeSalida.MENSAJE_RECHAZO)) { //TODO ELIMINAR MENSAJE RECHAZO
					objMensaje = new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,codMsg,valores,con,idEmpresa);
					P_OUT_MENSAJES.add(objMensaje);
					ord.MensajesProceso.add(objMensaje);
					ord.errores++;
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_AUTORIZACION) && ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) {
					objMensaje = new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,codMsg,msg);
					P_OUT_MENSAJES.add(objMensaje);
					ord.MensajesProceso.add(objMensaje);
					ord.errores++;
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
					objMensaje = new MensajeSalida(ord.IdInterno,requisito,codMsg,msg);
					P_OUT_MENSAJES.add(objMensaje);
					ord.MensajesProceso.add(objMensaje);
					ord.autorizaciones++;
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
					objMensaje = new MensajeSalida(ord.IdInterno,requisito,codMsg,valores,con,idEmpresa);
					P_OUT_MENSAJES.add(objMensaje);
					ord.MensajesProceso.add(objMensaje);
					ord.advertencias++;
				}
				else {
					ord.ok++;
				}
				}
			if(ord.errores>0) {
				eliminarOrdenBloqueada(con,idEmpresa,valores,ord,P_OUT_MENSAJES);
			}
		}else {
			ord.ok++;
		}		
	}
	
	public void limpiarOrdenes(List<OrdenProceso> ordenes, List<OrdenProceso> ordenesConErrores) {
		for (OrdenProceso ord : ordenes) {
			if (ord.errores > 0) {
				ordenesConErrores.add(ord);
			}
		}
		
		for (OrdenProceso ord : ordenesConErrores) {
			if (ordenes.contains(ord))
				ordenes.remove(ord);
		}
	}
	
	public static class InformacionOrden {
		public String IdMonedaInstrumento;
		public Long IdInterno;
		public Long IdOrden;
		public java.sql.Date FechaEmision;
		public String IdTipoReajusteInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdEmisorInstrumentoSubyacente;
		public String IdEmisor;
		public String IdTipoProducto;
		public Long IdEmpleadoDigitador;
		public String IdEstadoInstrumento;
		public Long IdInstrumentoReal;
		public String NombreTipoInstrumento;
		public BigDecimal TamanoCorte;
		public String NombreClasificacionTipoInstrumento;
		public Long TamanoContrato;
		public String IdTipoOperacionReal;
		public String NombreTipoOperacion;
		public String AccionOrden;
		public String EsClienteFinal;
		public String IdBolsaReal;
		public String IdCondicionDeLiquidacionContadoReal;
		public String NombreCondicionDeLiquidacion;
		public String IdTipoVigenciaReal;
		public String NombreTipoVigenciaOrden;
		public String IdBovedaReal;
		public String IdFormaCustodiaReal;
		public Long IdPorcentajeVisibilidadReal;
		public String IdTipoPrecioReal;
		public String NombreTipoPrecio;
		public String IdTipoCanalIngresoReal;
		public Long DiasLiquidacionContado;
		public String IdTipoReajusteReal;
		public String InstrumentoSubyacente;
		public String InstrumentoPermiteVentaCorta;
		public String InstrumentoPermiteSimultanea;
		public Long IdGrupoCobro;
		public String IdTipoCanalDistribucionReal;
		public String RuteoGeneraMovimientoFacturacionAutomatico;
		public String RuteoGeneraComprobanteFacturacionAutomatico;
		public BigDecimal MontoMaximoDistribucionOrden;
		public String ValidaHorario;
		public String AplazaOrdenPorHorarioNegociacion;
		public String HorarioPreMarketInicio;
		public String HorarioPreMarketFin;
		public String HorarioMarketInicio;
		public String HorarioMarketFin;
		public String IdEstadoModulo;
		public String NombreModulo;
		public String AplazaOrdenPorModuloCerrado;
		public Long IdPortafolioReal;
		public String IdEstadoPortafolio;
		public String IdEstadoAutorizacionPortafolio;
		public Long IdCliente;
		public Long IdPersonaCliente;
		public Long IdPersonaBroker;	//si el portafolio es un broker
		public Long IdPortafolioBroker;	//si el portafolio es de un broker
		public String IdEstadoPersona;
		public Long IdClienteReal;
		public String IdEstadoCliente;
		public Long IdSucursalEmpleadoDigitador;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String IdPerfilCobros;
		public String TipoDocumentoIdentidad;
		public String NombreEjecutivoPortafolio;
		public String IdEstadoEmpleadoEjecutivoPortafolio;
		public Long IdSucursalEmpleadoPortafolio;
		public Long IdEmpleadoPortafolio;
		public String IdEstadoEmpleadoDigitador;
		public String PlantillaEmpleadoProductoHabilitado;
		public String PlantillaEmpleadoPermiteRuteo;
		public BigDecimal PlantillaEmpleadoMaximoDescuentoComision;
		public BigDecimal PlantillaEmpleadoMontoMaximoOperacion;
		public String Nemotecnico;
		public String CodigoPortafolio;
		public String IdSistemaBursatil;
		public String IdTipoCliente;
		public String InstrumentoAcreditado;
		public Date FechaVencimientoInstrumento;
		public Date FechaVencimientoInstrumentoSubyacente;
		public String IdCategoriaInversionista;
		public String EsInstrumentoCalificado;
		public String EsRaiz;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OrdenTipoLiquidacion extends BcsPojo{
		
		public String IdTipoLiquidacionComisionFactura;
		public String TipoLiquidacion;
		public String TipoPorDefecto;
		public String CodigoDetalleIndicadorDefecto;
	}
	
	
	@Trace
	@SuppressWarnings("unlikely-arg-type")
	public CrearOrdenesSalida logica(Connection con, CrearOrdenesEntrada in) throws Exception {
		
		
		Gson gson = Utils.getGsonBuilder();
		JsonParser parser = new JsonParser();
		Long init = System.currentTimeMillis();
		SQL _sql = new SQL();
		if (in.p_BarraProgreso == null) {
			// No habilitado para servicio principal
        } else {
            in.p_BarraProgreso.actualizar(0L, "PROCESANDO", "Inicio creacion de ordenes");
        }
		CrearOrdenesSalida out = new CrearOrdenesSalida();
		Date fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate("0001-01-01");
		java.sql.Timestamp fechaUltimaEjecucionCambioFecha = Fecha.toTimeStamp("0001-01-01 00:00:00");
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
		String horaSistema = FechaDB.ObtenerHoraSistemaLocal(con,in.p_IdEmpresa);
		Timestamp fechaHoraReal= Fecha.toLocalTimeStamp(FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa));
		java.sql.Date fechaReal = Fecha.toDate(fechaHoraReal.toString().split(" ")[0]);
		
		{
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(2L,"PROCESANDO","Obteniendo fecha sistema");
			}
		}
		
		fechaUltimaEjecucionCambioFecha = Fecha.toLocalTimeStamp(obtieneUltimoProcesoCambioFecha(con));
		fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate(fechaUltimaEjecucionCambioFecha.toString().split(" ")[0]);
		Date fechaSistemaLocal = Fecha.toDate("0001-01-01");
		fechaSistemaLocal= Fecha.toDate(FechaDB.ObtenerFechaHoraSistemaLocal(con,in.p_IdEmpresa).toString().split(" ")[0]);
		
		String operacionesParam = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.OperacionesNegativas");
		List<String> operaciones = new ArrayList<>();
		
		List<String> codigosEmpConsulta = new ArrayList<>();	
		HashSet<String> codigosEmpleados = new HashSet<>();
		HashMap<String,Object> PreferenciasEmpsDigitadores = new HashMap<>();
		
		try {
			String[] array = operacionesParam.split(",");
			operaciones = Arrays.asList(array);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("Inicio servicio CrearOrdenes");
		try {
			
			HashMap<String,String> valores = new HashMap<String,String>();
			
			if (in.p_Ordenes.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ORDENES",valores,con,in.p_IdEmpresa)); 
				{
					if(in.p_BarraProgreso != null) {
						JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
						in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
					}
				}
				return out;
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(5L,"PROCESANDO","Verificando datos de entrada");
				}
			}
			ArrayList<Long> correlativos = new ArrayList<Long>();
			for (ObjOrdenesEntrada orden: in.p_Ordenes) {
				
				BigDecimal precioLimite = orden.PrecioLimite.stripTrailingZeros();
				String idBloqueaOrden = orden.IdPortafolio.toString() + '_' + orden.IdInstrumento.toString() + '_' + orden.IdTipoOperacion + '_' +  orden.Cantidad.toString() + '_' + precioLimite.toString() + '_' + fechaSistemaLocal.toString() ;
				orden.IdBloqueaOrden =	idBloqueaOrden;
				HashMap<String,Object> inParametros = new HashMap<String,Object>();
				inParametros.put("p_IdBloqueaOrden", idBloqueaOrden);
				inParametros.put("p_IdPortafolioCliente", orden.IdPortafolio);
				inParametros.put("p_IdInstrumento", orden.IdInstrumento);
				inParametros.put("p_IdTipoOperacion", orden.IdTipoOperacion);
				inParametros.put("p_Cantidad", orden.Cantidad);
				inParametros.put("p_CantidadOrdenes", 1);
				inParametros.put("p_PrecioLimite", precioLimite);
				inParametros.put("p_FechaSistema", fechaSistemaLocal);
				
				if("WBS".equals(orden.IngresoDesde) && orden.CodigoEjecutivoDigitador != null && !"".equals(orden.CodigoEjecutivoDigitador)) {
					if(codigosEmpleados.add(orden.CodigoEjecutivoDigitador)) {
						codigosEmpConsulta.add(orden.CodigoEjecutivoDigitador);
					}
				}
				
				HashMap<String,Class> outParametros = new HashMap<String,Class>();
				
				if(!_c.GLOSA_CARGA_MASIVA.equalsIgnoreCase(orden.Glosa) && !_c.GLOSA_ORDEN_POSTRERA.equalsIgnoreCase(orden.Glosa)) {
					
					if("N".equalsIgnoreCase(in.p_simularOperacion)) {	
						try {
							logger.debug("INICIO::SQL_CALL_BLOQUEO_ORDEN");
							HashMap<String,Object> salidaBloqueoOrden = _sql.call(con,SQL_CALL_BLOQUEO_ORDEN,inParametros,outParametros);
							logger.debug("FIN::SQL_CALL_BLOQUEO_ORDEN");
						}catch(SQLException sqle) {
							logger.error(sqle);
							out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BLOQUEO_ORDEN",valores,con,in.p_IdEmpresa));
							{
								if(in.p_BarraProgreso != null) {
									JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
									in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
								}
							}
								
							return out;
						}
						
						try {
							logger.debug("INICIO::SQL_SELECT_BLOQUEO_ORDEN");
							_sql.query(con, SQL_SELECT_BLOQUEO_ORDEN, inParametros, ObjOrdenesEntrada.class);
							logger.debug("FIN::SQL_SELECT_BLOQUEO_ORDEN");
						}catch(SQLException sqle) {
							{
								if(in.p_BarraProgreso != null) {
									JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
									in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
								}
							}
							return out;
						}
					}
				}
				if (correlativos.contains(orden.IdInterno)) {
					eliminarOrdenBloqueada(con,in.p_IdEmpresa,valores,inParametros,outParametros,out.P_OUT_MENSAJES,orden.Glosa);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",valores,con,in.p_IdEmpresa));
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				} else {
					correlativos.add(orden.IdInterno);
				}
				
				//Si es Venta Corta y el Canal de Distribucion es Manual no se debe ingresar la Orden
				if (!"MAN".equalsIgnoreCase(orden.IdTipoCanalDistribucion) && "S".equalsIgnoreCase(orden.EsVentaCorta)) {
					eliminarOrdenBloqueada(con,in.p_IdEmpresa,valores,inParametros,outParametros,out.P_OUT_MENSAJES,orden.Glosa);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ORDEN_VC_RUTEO",valores,con,in.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR += 1L;
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				}
			}
			
			if(!codigosEmpConsulta.isEmpty()) {
				System.out.println(codigosEmpConsulta);
				
				HashMap<String,Object> paramsC = new HashMap<>();
				paramsC.put("CodigosDig", codigosEmpConsulta);
						
				List<PreferenciasEmpDigitador> res = _sql.query(con, SQL_SELECT_PREFERENCIAS_DIGITADOR, paramsC, PreferenciasEmpDigitador.class);				
				paramsC.clear();
				
				System.out.println(res);
				
				
				for (PreferenciasEmpDigitador preferencia : res) {				
					if("0".equalsIgnoreCase(preferencia.IdTipoOperacion)) {
						PreferenciasEmpsDigitadores.put(preferencia.CodigoEmpleado + preferencia.IdTipoProducto + "GEN", preferencia);
					}
					else {
						PreferenciasEmpsDigitadores.put(preferencia.CodigoEmpleado + preferencia.IdTipoProducto + preferencia.IdTipoOperacion, preferencia);
					}
					
				}
				
			}
			
			
			
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(10L,"PROCESANDO","Validando requisitos minimos");
				}
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				eliminarOrdenBloqueada(con,in.p_IdEmpresa, valores, in.p_Ordenes, out.P_OUT_MENSAJES);
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				{
					if(in.p_BarraProgreso != null) {
						JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
						in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
					}
				}
				return out;
			}
			
			{
				HashMap<String,Object> params = new HashMap<>();
				params.put("IdModulo", _c.ID_MODULO_ORDENES);
				params.put("IdEmpresa", in.p_IdEmpresa);
				List<EstadoModulo> modulo = _sql.query(con, SQL_SELECT_ESTADO_MODULO, params, EstadoModulo.class);
				if (modulo.size() == 0) {
					eliminarOrdenBloqueada(con,in.p_IdEmpresa, valores, in.p_Ordenes, out.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_NO_EXISTE",valores,con,in.p_IdEmpresa));
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				}
				if (modulo.get(0).IdEstadoModulo.equalsIgnoreCase(_c.ESTADO_MODULO_CERRADO)) {
					eliminarOrdenBloqueada(con,in.p_IdEmpresa, valores, in.p_Ordenes, out.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",valores,con,in.p_IdEmpresa));
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					return out;
				}
				
				
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(12L,"PROCESANDO","Consultando parametros de sistema");
				}
			}
			//variables
			String plazoConf = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "ORDENES", "PlazoConfirmacionOrden");
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			String paramLiquidacionComisionFact = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PRODUCTO", "PRO.CobroComisionesMensuales");
			paramLiquidacionComisionFact = "ACTIVO".equals(paramLiquidacionComisionFact) ? "S" : "N";
			String[] plazoCnf = plazoConf.split(";");
			HashMap< String, Long> plazoConfirmacion = new HashMap<>();
			for (String p: plazoCnf){
				String [] val = p.split(":");
				plazoConfirmacion.put(val[0], new Long(val[1]));
			}
			String productosConConfirmacion = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "ORDENES", "ProductosConfirmacionOrden");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Timestamp fechaHoraSistemaTS = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,in.p_IdEmpresa));
			String fechaHoraSistema_ = Fecha.toStringDatetime(fechaHoraSistemaTS);
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			BigDecimal porcentajeDivergenciaPrecio = ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa, "ORDENES", "PorcentajeDivergenciaPrecio");
			BigDecimal porcentajeMaximoComision = ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa, "ORDENES", "PorcentajeMaximoComision");
			BigDecimal porcentajeMinimoComision = ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa, "ORDENES", "PorcentajeMinimoComision");
			BigDecimal montoMaximoOrden = ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa, "ORDENES", "MontoMaximoOrden");
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String usaFechaLiquidacionEnVencimiento = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
			
			String diaSiguiente_FechaSistema = obtieneFechaDiaSiguiente(fechaSistema,idPaisEmpresa,con,out);
			Boolean esDiaHabil_FechaSistema = esDiaHabil(fechaSistema,in.p_IdEmpresa, con);
			Boolean esDiaHabil_FechaReal= esDiaHabil(fechaReal.toString(),in.p_IdEmpresa, con);

			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(15L,"PROCESANDO","Consultando precio puntas");
				}
			}
			
			String calculoPrecioPunta;
			try {
				calculoPrecioPunta = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "ORDENES", "CalculoPrecioPunta");
			} catch (Exception e) {
				calculoPrecioPunta = "N";
			}
			String parametroMontoMinimo;
			String parametroMontoMaximoRuteo;
			String ParametroPrecioMercado = null;
			BigDecimal parametroPorcentajeCobro;
			try {
				// Parametro Banco Estado para considerar monto minimo para crear una orden
				parametroMontoMinimo = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa,"ORDENES","MontoMinimo");
				
				if (parametroMontoMinimo==null || parametroMontoMinimo.equals("")) {
					parametroMontoMinimo = "0";
				}
				
			} catch (Exception e) {
				parametroMontoMinimo = "0";
			}
			try {
				parametroMontoMaximoRuteo= ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa,"ORDENES","MontoMaximoCrearOrdenes");
				if(parametroMontoMaximoRuteo.equals("")) {
					parametroMontoMaximoRuteo = null;
				}
			}catch(Exception e) {
				parametroMontoMaximoRuteo = null;
			}
			try {
				ParametroPrecioMercado = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "ORDENES", "ParametroPrecioMercado");	
				if(ParametroPrecioMercado == null || ParametroPrecioMercado.equals("")) {
					ParametroPrecioMercado = _c.NO;	
				}
			}catch(Exception e) {
				ParametroPrecioMercado = _c.NO;
			}
			try {
				parametroPorcentajeCobro = ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa, "ORDENES", "PorcentajeCobro");
			}catch(Exception e) {
				parametroPorcentajeCobro = null;
			}
			
			HashMap<String,Object> paramsTipoLiquidacion = new HashMap<>(1);
			List<OrdenTipoLiquidacion> tipoLiquidacionPorDefecto = _sql.query(con, SQL_SELECT_TIPO_LIQUIDACION_DEFECTO, paramsTipoLiquidacion, OrdenTipoLiquidacion.class);
			
			HashMap<String,Object> paramsInVentaCorta = new HashMap<>();
			paramsInVentaCorta.put("IdTipoPrestamoVentaCorta", IdTipoPrestamoVentaCorta);
			List<ParametroVentaCorta> parametrosVentaCorta = _sql.query(con, SQL_SELECT_PARAMETROS_VENTA_CORTA, paramsInVentaCorta, ParametroVentaCorta.class);
			
			ArrayList<OrdenProceso> ordenesConErrores = new ArrayList<>();
			ArrayList<OrdenProceso> ordenes = new ArrayList<>();
			ArrayList<OrdenProceso> ordenesConfirmacion = new ArrayList<>();
			
			
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(18L,"PROCESANDO","Consultando datos de las ordenes");
				}
			}
			
			
			
			HashMap<String, BigDecimal> divergenciaCondicionLiquidacion = new HashMap<>();
			HashMap<Long,OrdenProceso> ordenesHash = new HashMap<Long,OrdenProceso>();
			//LECTURA DE DATOS DE LA ORDEN
			{
				//CrearOrdenes_Proceso es GLOBAL TEMPORARY (ON COMMIT DELETE ROWS): no soporta DELETE y no hace falta,
				//salvo que un servicio superior llame a CrearOrdenes mas de una vez en la misma sesion/transaccion
				//sin commit de por medio, en cuyo caso la tabla podria seguir teniendo filas de la llamada anterior.
				//IdTrabajo aisla las filas de esta llamada especifica para que el select de mas abajo no las mezcle.
				Long idTrabajo = CONTADOR_ID_TRABAJO.incrementAndGet();
				for (ObjOrdenesEntrada ordenEntradaTrabajo : in.p_Ordenes) {
					ordenEntradaTrabajo.IdTrabajo = idTrabajo;
				}
				//Se insertan datos de ordenes en la tabla de paso
				_sql.batch(con,SQL_INSERT_ORDENES_WRAPPER,in.p_Ordenes, ObjOrdenesEntrada.class);

				HashMap<String,Object> paramsIn1 = new HashMap<>();
				paramsIn1.put("IdEmpresa", in.p_IdEmpresa);
				paramsIn1.put("IdUsuario", in.p_IdUsuario);
				paramsIn1.put("IdTrabajo", idTrabajo);

				List<InformacionOrden> _infoordenes = _sql.query(con, SQL_SELECT_ORDENES, paramsIn1, InformacionOrden.class);
				HashMap<Long,Object> infoOrdenes = Utils.List2Hash(_infoordenes, "IdInterno");
				
				for (ObjOrdenesEntrada ordenEntrada : in.p_Ordenes) {
					InformacionOrden info = (InformacionOrden) infoOrdenes.get(ordenEntrada.IdInterno);
					OrdenProceso ordenProceso = copyPojoOrdenProceso(ordenEntrada);
					
					ordenProceso.IdBloqueaOrden = ordenEntrada.IdBloqueaOrden;
					
					ordenProceso.IdInterno = ordenEntrada.IdInterno;
					ordenProceso.Cantidad = ordenEntrada.Cantidad;
					ordenProceso.IdOrdenReferencia = ordenEntrada.IdOrdenReferencia;
					ordenProceso.CumpleHorario = _c.SI;
					
					if (ordenEntrada.GeneraSpot.equalsIgnoreCase("S")) {
						if ("COMPRA".equalsIgnoreCase(info.AccionOrden)) {
							if ("USD".equalsIgnoreCase(ordenEntrada.IdMonedaOrden)) {
								ordenProceso.GeneraSpot = "S";
							}else{
								if ("CLP".equalsIgnoreCase(ordenEntrada.IdMonedaOrden) && ("USD".equalsIgnoreCase(ordenEntrada.IdMonedaLiquidacionOrden))) {
									ordenProceso.GeneraSpot = "S";	
								}else{
									ordenProceso.GeneraSpot = "N";
								}								
							}							
						}					
					}else{
						ordenProceso.GeneraSpot = "N";
					};
					
					ordenProceso.TipoLiquidacionFactura = "S".equals(paramLiquidacionComisionFact) ? ordenEntrada.TipoLiquidacionFactura : "";
					ordenProceso.EsInstrumentoCalificado = info.EsInstrumentoCalificado;
					ordenProceso.EsRaiz = info.EsRaiz;
					String horaRecepcion = ordenEntrada.FechaRecepcion.length() == 19 ? ordenEntrada.FechaRecepcion.substring(11) : ordenEntrada.FechaRecepcion; // "0001-01-01 00:00:00"
					String horaUtcChile = horaRecepcion.length() == 8 ? convertirHoraUtcAHoraChile(horaRecepcion) : horaSistema;// "00:00:00"
					String horaUtcSistema = FechaDB.ObtenerHoraUTC(con);

					
					if (ordenEntrada.FechaRecepcion == null || ("0001-01-01 00:00:00").equalsIgnoreCase(ordenEntrada.FechaRecepcion) || ("00:00:00").equalsIgnoreCase(horaUtcChile)) {
					    ordenProceso.FechaRecepcion = Fecha.toDateTime(fechaHoraSistema_);
					}
					else {
					    ordenEntrada.FechaRecepcion = ordenEntrada.FechaRecepcion.length() == 19 ? ordenEntrada.FechaRecepcion : ordenEntrada.FechaRecepcion+" "+horaUtcSistema; 
					    ordenProceso.FechaRecepcion = Fecha.toDateTime(ordenEntrada.FechaRecepcion);
					}
					
					
					if (ordenEntrada.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_AGENTE) && !ordenEntrada.FechaOperacion.equalsIgnoreCase("0001-01-01") && !ordenEntrada.FechaOperacion.equalsIgnoreCase(""))
					{	// Aca entran las ordenes por carma masiva, donde se envia la FechaOperacion con una hora definida
						ordenProceso.FechaOperacion = Fecha.toDateTime(ordenEntrada.FechaOperacion);
						ordenProceso.FechaSistemaCreacion = Fecha.toDate(ordenEntrada.FechaOperacion);
					}
					else if (ordenEntrada.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) { //// TRA
						ordenProceso.FechaOperacion = Fecha.toDateTime(ordenEntrada.FechaOperacion);
						ordenProceso.FechaSistemaCreacion = Fecha.toDate(ordenEntrada.FechaOperacion);
					} else {
						
						ordenProceso.FechaOperacion = Fecha.toDateTime(fechaHoraSistema_);
						ordenProceso.FechaSistemaCreacion = Fecha.toDate(fechaSistema);
						
						String utcStringTime = Fecha.toStringTime(ordenProceso.FechaOperacion);
							LocalTime lt = LocalTime.parse(utcStringTime).plusSeconds(segundosZonaHoraria);
							 
							LocalTime inicioDia = LocalTime.parse("00:00:00");
							LocalTime finDia = LocalTime.parse("23:59:59");
							LocalTime premkInicio = LocalTime.parse(info.HorarioPreMarketInicio);
							//LocalTime premkFin = LocalTime.parse(info.HorarioPreMarketFin);  no estamos usando estos valores
							//LocalTime mkInicio = LocalTime.parse(info.HorarioMarketInicio);  no estamos usando estos valores
							LocalTime mkFin = LocalTime.parse(info.HorarioMarketFin);

						if (info.ValidaHorario.equalsIgnoreCase("S") && ordenProceso.IdTipoCanalDistribucion.equalsIgnoreCase("MAN")) {
								if (!esDiaHabil_FechaReal) {
									ordenProceso.CumpleHorario = _c.NO;
									ordenProceso.AplazaSoloHora = _c.SI;
									ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
									if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
										//Valida si la fecha de sistema es dia habil, por pauta de fin de mes que asigna dia inhabil al sistema
										if(esDiaHabil_FechaSistema) {
											//la fecha de sistema es habil, se mantiene la fecha sistema creacion
											ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
										}
										else {
											ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
											ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema, info.HorarioPreMarketInicio);	
										}
										
									}
								}
								//LOS 3 CASOS SIGUIENTES SON EN DIAS HABILES
								//CASO POST PAUTA, BUSCA LA ULTIMA FECHA DE EJECUCION DE PAUTA Y SI ES IGUAL A LA FECHA REAL QUIERE DECIR QUE SE EJECUTO LA PAUTA Y DEBE QUEDAR PARA MISMA FECHA SISTEMA + HORA 9:01
								else if (fechaReal.compareTo(fechaUltimaEjecucionCambioFechaLocal) ==  0) {
									ordenProceso.CumpleHorario = _c.NO;
									ordenProceso.AplazaSoloHora = _c.SI;
									if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
										//se mantiene la fecha sistema creacion
										ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
										ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
									}
								}
								//ORDEN ENTRE 00 Y 09 (LOCAL) MISMA FECHA SISTEMA + HORA 9:01 (HORARIO NO VALIDO) PREVIA A APERTURA DE MERCADO
								else if (lt.compareTo(inicioDia) >= 1 && lt.compareTo(premkInicio) <= -1) {
									ordenProceso.CumpleHorario = _c.NO;
									ordenProceso.AplazaSoloHora = _c.SI;
									if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
										ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
										//se mantiene la fecha sistema creacion
									}
								}
								//ORDEN ENTRE 16 Y 23 59 (LOCAL) MISMA FECHA SISTEMA, DEBE QUEDAR FECHA SISTEMA + 1 DIA HABIL + HORA 9:01  (HORARIO NO VALIDO), ESTO ES POST CIERRE MERCADO Y PREVIO A PAUTA
								else if (lt.compareTo(mkFin) >= 1 && lt.compareTo(finDia) <= -1) {
									ordenProceso.CumpleHorario = _c.NO;
									if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
										
										//se le suma 1 dia habil a la fecha sistema
										ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
										ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema, info.HorarioPreMarketInicio);	
									}
								}
						}	
						//RECALCULO DE FECHA DE OPERACION PARA ORDENES DE RUTEO FUERA DEL HORARIO DE MERCADO
						//SI NO SABE PREGUNTE
						if (info.ValidaHorario.equalsIgnoreCase("S") && (ordenProceso.IdTipoCanalDistribucion.equalsIgnoreCase("RUR") || ordenProceso.IdTipoCanalDistribucion.equalsIgnoreCase("RUA"))) {
							//FECHA REAL INHABIL?, ESTO ES PARA EL CASO QUE EL CLIENTE INGRESE UNA ORDEN SABADO O DOMINGO O FERIADO, DEBE QUEDAR PARA LA MISMA FECHA DEL SISTEMA A LAS 9AM
							if (!esDiaHabil_FechaReal) {
								ordenProceso.CumpleHorario = _c.NO;
								ordenProceso.AplazaSoloHora = _c.SI;
								ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									//Valida si la fecha de sistema es dia habil, por pauta de fin de mes que asigna dia inhabil al sistema
									if(esDiaHabil_FechaSistema) {
									//la fecha de sistema es habil, se mantiene la fecha sistema creacion
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
									}
									else {
										//la fecha de sistema no es habil, se le suma 1 dia habil a la fecha sistema
										ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
										ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema , info.HorarioPreMarketInicio);	
									}
									
								}
							}
							//LOS 3 CASOS SIGUIENTES SON EN DIAS HABILES
							//CASO POST PAUTA, BUSCA LA ULTIMA FECHA DE EJECUCION DE PAUTA Y SI ES IGUAL A LA FECHA REAL QUIERE DECIR QUE SE EJECUTO LA PAUTA Y DEBE QUEDAR PARA MISMA FECHA SISTEMA + HORA 9:01
							else if (fechaReal.compareTo(fechaUltimaEjecucionCambioFechaLocal) ==  0) {
								ordenProceso.CumpleHorario = _c.NO;
								ordenProceso.AplazaSoloHora = _c.SI;
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									//se mantiene la fecha sistema creacion
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
									ordenProceso.FechaRecepcion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaReal.toString() , horaSistema);
								}
							}
							//ORDEN ENTRE 00 Y 09 (LOCAL) MISMA FECHA SISTEMA + HORA 9:01 (HORARIO NO VALIDO) PREVIA A APERTURA DE MERCADO
							else if (lt.compareTo(inicioDia) >= 1 && lt.compareTo(premkInicio) <= -1) {
								ordenProceso.CumpleHorario = _c.NO;
								ordenProceso.AplazaSoloHora = _c.SI;
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema , info.HorarioPreMarketInicio);
									//se mantiene la fecha sistema creacion
								}
							}
							//ORDEN ENTRE 16 Y 23 59 (LOCAL) MISMA FECHA SISTEMA, DEBE QUEDAR FECHA SISTEMA + 1 DIA HABIL + HORA 9:01  (HORARIO NO VALIDO), ESTO ES POST CIERRE MERCADO Y PREVIO A PAUTA
							else if (lt.compareTo(mkFin) >= 1 && lt.compareTo(finDia) <= -1) {
								ordenProceso.CumpleHorario = _c.NO;
								if (info.AplazaOrdenPorHorarioNegociacion.equalsIgnoreCase("S")) {
									ordenProceso.FechaSistemaCreacion = Fecha.toDate(diaSiguiente_FechaSistema);
									ordenProceso.FechaOperacion = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(diaSiguiente_FechaSistema , info.HorarioPreMarketInicio);	
								}
							}
							
						}
												
					}
					//agregar Fecha Confirmacion Plazo
					if (ordenProceso.IdTipoCanalIngreso.equalsIgnoreCase("CLI")){
						ordenProceso.IdEstadoConfirmacion = _c.ESTADO_CONFIRMACION_ORDEN_EN_PLAZO;
						ordenProceso.FechaConfirmacionPlazo = Fecha.toDate(Fecha.toStringDate(ordenProceso.FechaOperacion));
						ordenProceso.FechaConfirmacion = ordenProceso.FechaOperacion;
					}
					ordenProceso.IdTipoVigenciaOrden = ordenEntrada.IdTipoVigencia;
					if (ordenEntrada.IdTipoVigencia.equalsIgnoreCase("") || ordenEntrada.IdTipoVigencia.equalsIgnoreCase(_c.TIPO_VIGENCIA_SOLO_HOY) ) {
						ordenProceso.FechaVigencia = ordenProceso.FechaSistemaCreacion;
					}
					else if (ordenEntrada.IdTipoVigencia.equalsIgnoreCase(_c.TIPO_VIGENCIA_SEMANAL) || ordenEntrada.IdTipoVigencia.equalsIgnoreCase(_c.TIPO_VIGENCIA_FECHA_FIJA)) {
						ordenProceso.FechaVigencia = Fecha.toDate(ordenEntrada.FechaVigencia);
					}

					else if (ordenEntrada.IdTipoVigencia.equalsIgnoreCase(_c.TIPO_VIGENCIA_PERMANENTE)) {
						ordenProceso.FechaVigencia = Fecha.toDate(ordenEntrada.FechaVigencia);
					}
					else {
						ordenProceso.FechaVigencia = null;
					}
					
					ordenProceso.IdEstadoOrden = _c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR;
					ordenProceso.IdEstadoInterno = _c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR;
					ordenProceso.IdEstadoLimite = _c.ESTADO_ORDEN_AUTORIZADA;
					ordenProceso.EsClienteFinal = ordenEntrada.EsClienteFinal;
					
					if (ordenEntrada.IdBoveda == null || ordenEntrada.IdBoveda.equalsIgnoreCase("0")) {
						ordenProceso.IdBoveda = null;
					} else {
						ordenProceso.IdBoveda = ordenEntrada.IdBoveda;
					}
					
					ordenProceso.PrecioOrden = ordenEntrada.PrecioLimite;
					if (ordenEntrada.EsFacturaInmediata == null || ordenEntrada.EsFacturaInmediata.equalsIgnoreCase("")) {
						ordenProceso.IndicadorFacturacionInmediata = "N";
					} else {
						ordenProceso.IndicadorFacturacionInmediata = ordenEntrada.EsFacturaInmediata;
					}
					
					ordenProceso.IdPortafolioCliente = ordenEntrada.IdPortafolio;
					if (ordenEntrada.EsVentaCorta == null) {
						ordenProceso.EsVentaCorta = _c.NO;
						ordenProceso.MontoPrimaVentaCorta = BigDecimal.ZERO;
					} else {
						ordenProceso.EsVentaCorta = ordenEntrada.EsVentaCorta;
					}
					
					if (ordenEntrada.PorcentajeAllIn == null || ordenEntrada.PorcentajeAllIn.equals(BigDecimal.ZERO)) {
						ordenProceso.PorcentajeAllIn = ordenEntrada.PorcentajeAllIn;	
					}
					
					if (ordenEntrada.TipoCambio == null) {
						
						if((ordenProceso.IdMonedaOrden == null || ordenProceso.IdMonedaLiquidacionOrden == null) || (ordenProceso.IdMonedaOrden.equals(ordenProceso.IdMonedaLiquidacionOrden) && monedaSistema.equals(ordenProceso.IdMonedaOrden))) {
							ordenProceso.TipoCambio = BigDecimal.ONE;
						}else {
						ordenProceso.TipoCambio = BigDecimal.ZERO;
						}
					}
					else {
						ordenProceso.TipoCambio = ordenEntrada.TipoCambio;
					}
					
					ordenProceso.MontoOrden = ordenEntrada.MontoNeto;
					if (ordenEntrada.IdCorredorCorresponsal == null) {
						ordenProceso.IdCorredorCorresponsal = 0L;
					}
					else {
						ordenProceso.IdCorredorCorresponsal = ordenEntrada.IdCorredorCorresponsal;
					}
					
					if (ordenEntrada.IdPersonaAutorizadoDarOrdenes == null) {
						ordenProceso.IdPersonaAutorizadoDarOrdenes = 0L;
					}
					else {
						ordenProceso.IdPersonaAutorizadoDarOrdenes = ordenEntrada.IdPersonaAutorizadoDarOrdenes;
					}
					
					ordenProceso.IdOrden = info.IdOrden;
					ordenProceso.IdClasificacionTipoInstrumento = info.IdClasificacionTipoInstrumento;
					ordenProceso.IdEmpresa = in.p_IdEmpresa;
					ordenProceso.IdTipoInstrumento = info.IdTipoInstrumento;
					
					if (ordenEntrada.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) {
						ordenProceso.FechaEmision = Fecha.toDate(ordenEntrada.FechaEmision);
					} else {
						ordenProceso.FechaEmision = info.FechaEmision;
					}
					
					if (ordenProceso.EsClienteFinal == null) {
						ordenProceso.EsClienteFinal = info.EsClienteFinal;
					}
					
					if (localizacionSistema.equalsIgnoreCase("CL") && info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
						ordenProceso.IdTipoReajuste = info.IdTipoReajusteInstrumento;
					}
					else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						ordenProceso.IdTipoReajuste = ordenEntrada.IdTipoReajuste;
					}
					else {
						ordenProceso.IdTipoReajuste = "";
					}
					
					if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						ordenProceso.IdEmisor = ordenEntrada.IdEmisor;
					}
					else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						ordenProceso.IdEmisor = info.IdEmisor;
					}
					else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)
							&& info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)
							&& ordenEntrada.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)) {
						ordenProceso.IdEmisor = info.IdEmisorInstrumentoSubyacente;
					}
					else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)
							&& info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)
							&& ordenEntrada.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM)) {
						ordenProceso.IdEmisor = "";
					}
					else {
						ordenProceso.IdEmisor = info.IdEmisor;
					}
					
					if (info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
						ordenProceso.IdInstrumentoCustodia = ordenEntrada.IdInstrumentoSubyacente;
					}
					else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						ordenProceso.IdInstrumentoCustodia = ordenEntrada.IdInstrumentoSubyacente;
					}
					else {
						ordenProceso.IdInstrumentoCustodia = ordenEntrada.IdInstrumento;
					}
					
					//datos leidos desde tabla
					ordenProceso.AccionOrden = info.AccionOrden;
					ordenProceso.CodigoPortafolio = info.CodigoPortafolio;
					ordenProceso.IdBolsaReal = info.IdBolsaReal;
					
					ordenProceso.IdEstadoInstrumento = info.IdEstadoInstrumento;
					ordenProceso.IdInstrumentoReal = info.IdInstrumentoReal;
					ordenProceso.IdMonedaInstrumento = info.IdMonedaInstrumento;
					ordenProceso.IdProducto = info.IdTipoProducto;
					ordenProceso.IdTipoOperacionReal = info.IdTipoOperacionReal;
										
					ordenProceso.Nemotecnico = info.Nemotecnico;
					ordenProceso.NombreClasificacionTipoInstrumento = info.NombreClasificacionTipoInstrumento;
					ordenProceso.NombreTipoInstrumento = info.NombreTipoInstrumento;
					ordenProceso.NombreTipoOperacion = info.NombreTipoOperacion;
					ordenProceso.TamanoCorte = info.TamanoCorte;
					ordenProceso.TamanoContrato = info.TamanoContrato;
					
					ordenProceso.IdCondicionDeLiquidacionContadoReal = info.IdCondicionDeLiquidacionContadoReal;
					ordenProceso.NombreCondicionDeLiquidacion = info.NombreCondicionDeLiquidacion;
					ordenProceso.IdTipoVigenciaReal = info.IdTipoVigenciaReal;
					ordenProceso.NombreTipoVigenciaOrden = info.NombreTipoVigenciaOrden;
					
					ordenProceso.IdBovedaReal = info.IdBovedaReal;
					ordenProceso.IdFormaCustodiaReal = info.IdFormaCustodiaReal;
					ordenProceso.IdPorcentajeVisibilidadReal = info.IdPorcentajeVisibilidadReal;
					ordenProceso.IdTipoPrecioReal = info.IdTipoPrecioReal;
					ordenProceso.NombreTipoPrecio = info.NombreTipoPrecio;
					
					ordenProceso.IdTipoCanalIngresoReal = info.IdTipoCanalIngresoReal;
					ordenProceso.DiasLiquidacionContado = info.DiasLiquidacionContado;
					ordenProceso.IdTipoReajusteReal = info.IdTipoReajusteReal;
					ordenProceso.InstrumentoSubyacente = info.InstrumentoSubyacente;
					
					ordenProceso.InstrumentoPermiteVentaCorta = info.InstrumentoPermiteVentaCorta;
					ordenProceso.InstrumentoPermiteSimultanea = info.InstrumentoPermiteSimultanea;
					
					ordenProceso.IdGrupoCobro = info.IdGrupoCobro;
					ordenProceso.IdTipoCanalDistribucionReal ="BEC".equalsIgnoreCase(info.IdBolsaReal)?"MAN":info.IdTipoCanalDistribucionReal;
					ordenProceso.RuteoGeneraMovimientoFacturacionAutomatico = info.RuteoGeneraMovimientoFacturacionAutomatico;
					ordenProceso.RuteoGeneraComprobanteFacturacionAutomatico = info.RuteoGeneraComprobanteFacturacionAutomatico;
					ordenProceso.MontoMaximoDistribucionOrden = info.MontoMaximoDistribucionOrden;
					ordenProceso.ValidaHorario = info.ValidaHorario;
					ordenProceso.AplazaOrdenPorHorarioNegociacion = info.AplazaOrdenPorHorarioNegociacion;
					ordenProceso.HorarioPreMarketInicio = info.HorarioPreMarketInicio;
					ordenProceso.HorarioPreMarketFin = info.HorarioPreMarketFin;
					ordenProceso.HorarioMarketInicio = info.HorarioMarketInicio;
					ordenProceso.HorarioMarketFin = info.HorarioMarketFin;
					ordenProceso.IdEstadoModulo = info.IdEstadoModulo;
					ordenProceso.NombreModulo = info.NombreModulo;
					ordenProceso.AplazaOrdenPorModuloCerrado = info.AplazaOrdenPorModuloCerrado;
					ordenProceso.IdPortafolioReal = info.IdPortafolioReal;
					ordenProceso.IdEstadoPortafolio = info.IdEstadoPortafolio;
					ordenProceso.IdEstadoAutorizacionPortafolio = info.IdEstadoAutorizacionPortafolio;
					ordenProceso.IdCliente = info.IdCliente;
					ordenProceso.IdPersonaCliente = info.IdPersonaCliente;
					ordenProceso.IdEstadoPersona = info.IdEstadoPersona;
					ordenProceso.IdClienteReal = info.IdClienteReal;
					ordenProceso.IdEstadoCliente = info.IdEstadoCliente;
					
					ordenProceso.DocumentoIdentidad = info.DocumentoIdentidad;
					ordenProceso.NombreCompleto = info.NombreCompleto;
					ordenProceso.IdPerfilCobros = info.IdPerfilCobros;
					ordenProceso.TipoDocumentoIdentidad = info.TipoDocumentoIdentidad;
					ordenProceso.NombreEjecutivoPortafolio = info.NombreEjecutivoPortafolio;
					
					ordenProceso.IdEstadoEmpleadoEjecutivoPortafolio = info.IdEstadoEmpleadoEjecutivoPortafolio;
					ordenProceso.IdSucursalEmpleadoPortafolio = info.IdSucursalEmpleadoPortafolio;
					ordenProceso.IdEmpleadoPortafolio = info.IdEmpleadoPortafolio;
					
					//// PLANTILLA EMPLEADO DIGITADOR EXTERNA
					
					if("WBS".equalsIgnoreCase(ordenEntrada.IngresoDesde) && ordenEntrada.CodigoEjecutivoDigitador != null && !"".equalsIgnoreCase(ordenEntrada.CodigoEjecutivoDigitador)) {
						PreferenciasEmpDigitador plantillaDigitadorEXT = (PreferenciasEmpDigitador) PreferenciasEmpsDigitadores.get(ordenEntrada.CodigoEjecutivoDigitador + info.IdTipoProducto + info.IdTipoOperacionReal);
						if(plantillaDigitadorEXT == null) {
							plantillaDigitadorEXT = (PreferenciasEmpDigitador) PreferenciasEmpsDigitadores.get(ordenEntrada.CodigoEjecutivoDigitador + info.IdTipoProducto + "GEN");
						}
						
						if(plantillaDigitadorEXT != null) {
							ordenProceso.IdEstadoEmpleadoDigitador = plantillaDigitadorEXT.IdEstado;
							ordenProceso.IdSucursalEmpleadoDigitador = plantillaDigitadorEXT.IdSucursalEmpleadoDigitador;
							ordenProceso.PlantillaEmpleadoProductoHabilitado = plantillaDigitadorEXT.PlantillaEmpleadoProductoHabilitado;
							ordenProceso.PlantillaEmpleadoPermiteRuteo = plantillaDigitadorEXT.PermiteRuteo;
							ordenProceso.PlantillaEmpleadoMaximoDescuentoComision = plantillaDigitadorEXT.PorcentajeDescuentoComision;
							ordenProceso.PlantillaEmpleadoMontoMaximoOperacion = plantillaDigitadorEXT.MontoMaximoOperacion;
							ordenProceso.IdEmpleadoDigitador = plantillaDigitadorEXT.IdEmpleado;
						}
					}else {
						ordenProceso.IdEstadoEmpleadoDigitador = info.IdEstadoEmpleadoDigitador;
						ordenProceso.IdSucursalEmpleadoDigitador = info.IdSucursalEmpleadoDigitador;		
						ordenProceso.PlantillaEmpleadoProductoHabilitado = info.PlantillaEmpleadoProductoHabilitado;
						ordenProceso.PlantillaEmpleadoPermiteRuteo = info.PlantillaEmpleadoPermiteRuteo;
						ordenProceso.PlantillaEmpleadoMaximoDescuentoComision = info.PlantillaEmpleadoMaximoDescuentoComision;
						ordenProceso.PlantillaEmpleadoMontoMaximoOperacion = info.PlantillaEmpleadoMontoMaximoOperacion;
						ordenProceso.IdEmpleadoDigitador = info.IdEmpleadoDigitador;
					}
							
					ordenProceso.PrecioMercado = BigDecimal.ZERO;
					ordenProceso.IdTipoCliente = info.IdTipoCliente;
					
					ordenProceso.IdCategoriaInversionista = info.IdCategoriaInversionista;
					if (info.IdPersonaBroker != null) {
						ordenProceso.EsBroker = "S";
						ordenProceso.IdPersonaBroker = info.IdPersonaBroker;	
					}
					else {
						ordenProceso.EsBroker = "N";
						if (info.IdPortafolioBroker != null) {
							ordenProceso.IdPersonaBroker = info.IdPortafolioBroker;
						}
						
					}
					
					ordenProceso.FechaVencimientoIF = ordenEntrada.FechaVencimiento;
					ordenProceso.FechaVencimientoInstrumento = info.FechaVencimientoInstrumento;
					ordenProceso.FechaVencimientoInstrumentoSubyacente = info.FechaVencimientoInstrumentoSubyacente;
					
					//MECANISMO DE CAPTACION
					ordenProceso.CodigoClaseOrden = ordenEntrada.IdMecanismoCaptacion;
					
					if (localizacionSistema.equalsIgnoreCase("PE")) {
						String[] modalidadMercadoSMV = _c.getModalidadMercadoSMV(ordenProceso.IdBolsa, ordenProceso.IdClasificacionTipoInstrumento, ordenProceso.IdTipoInstrumento, ordenProceso.IdSistemaBursatil, ordenProceso.IdTipoLibroOrden);
						ordenProceso.ModalidadSMV = modalidadMercadoSMV[0];
						ordenProceso.MercadoSMV = modalidadMercadoSMV[1];
					}
					else {
						ordenProceso.ModalidadSMV = "";
						ordenProceso.MercadoSMV = "";
					}
					ordenProceso.CodigoEjecutivoDigitador = ordenEntrada.CodigoEjecutivoDigitador;
					ordenProceso.IngresoDesde = ordenEntrada.IngresoDesde;
					
					//FIN
					ordenesHash.put(ordenProceso.IdInterno, ordenProceso);
					ordenes.add(ordenProceso);
				}
			}
				// FECHAS Vigencia			
			{
				ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
				inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
				inObtenerDiaSiguiente.p_diaHabil = "N";
				inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
				inObtenerDiaSiguiente.p_consideraHabilAnterior = "S";
				inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
				Long numeroDias = 0l;
				LocalDate fechaVigencia = LocalDate.now();
				for (OrdenProceso ord : ordenes) {
					FechasIn fechasIn = new FechasIn();
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)
							&& !ord.IdTipoVigenciaReal.equalsIgnoreCase(_c.TIPO_VIGENCIA_SOLO_HOY)) {
					fechasIn.fechaInicial = Fecha.toStringDate(ord.FechaOperacion);
					if (ord.IdTipoVigenciaReal.equalsIgnoreCase(_c.TIPO_VIGENCIA_PERMANENTE)){
						numeroDias = 29L;
					}else if (ord.IdTipoVigenciaReal.equalsIgnoreCase(_c.TIPO_VIGENCIA_SEMANAL)){
						numeroDias = 6L;
					}else{ 
						numeroDias = 0l;
						fechaVigencia = LocalDate.parse(ord.FechaVigencia.toString());
						if(ord.CumpleHorario.equals("N") && ord.AplazaOrdenPorHorarioNegociacion.equals("S") && ord.AplazaSoloHora.equals("N"))//AplazaOrden
						{							
							fechaVigencia = fechaVigencia.plusDays(1l);
						}
						fechasIn.fechaInicial = fechaVigencia.toString();
					}
					fechasIn.numeroDias = numeroDias;
					fechasIn.IdInterno = ord.IdOrden;
					inObtenerDiaSiguiente.p_fechas.add(fechasIn);
					}
				}

				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(con,inObtenerDiaSiguiente);
				HashMap<Long, Object> hashFechas = Utils.List2Hash(outObtenerDiaSiguiente.p_out_fechas,"IdInterno");
				for (OrdenProceso ord : ordenes) {
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)
							&& !ord.IdTipoVigenciaReal.equalsIgnoreCase(_c.TIPO_VIGENCIA_SOLO_HOY)) {
						FechasOut fechaLiquidacionOut = (FechasOut) hashFechas.get(ord.IdOrden);
						ord.FechaVigencia =  Fecha.toDate(fechaLiquidacionOut.fechaCalculada);
					}
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(21L,"PROCESANDO","Validando fechas de liquidacion");
				}
			}
			
			//FECHAS LIQUIDACION
			{
				ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
				inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
				inObtenerDiaSiguiente.p_diaHabil = "S";
				inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
				
				for (OrdenProceso ord : ordenes) {
					FechasIn f = new FechasIn();
					f.fechaInicial = Fecha.toStringDate(ord.FechaSistemaCreacion);
					if (ord.DiasLiquidacionContado == null)
						ord.DiasLiquidacionContado = 0L;
					f.numeroDias = new Long(ord.DiasLiquidacionContado);
					f.IdInterno = ord.IdOrden;
					inObtenerDiaSiguiente.p_fechas.add(f);
					
					if (!ord.IdTipoCanalIngreso.equalsIgnoreCase("CLI")){
						if(productosConConfirmacion.contains(ord.IdTipoInstrumento) ){
							FechasIn fechaConfirmar = new FechasIn();
							fechaConfirmar.fechaInicial = Fecha.toStringDate(ord.FechaSistemaCreacion);
							fechaConfirmar.numeroDias = plazoConfirmacion.get(ord.IdTipoInstrumento);
							fechaConfirmar.pais = idPaisEmpresa;
							fechaConfirmar.IdInterno = ord.IdOrden*-1;
							inObtenerDiaSiguiente.p_fechas.add(fechaConfirmar);
						}
					}
				}
				
				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(con, inObtenerDiaSiguiente);
				HashMap<Long,Object> hashFechas = Utils.List2Hash(outObtenerDiaSiguiente.p_out_fechas,"IdInterno");
				for (OrdenProceso ord : ordenes) {
						FechasOut fechaLiquidacionOut = (FechasOut) hashFechas.get(ord.IdOrden);
						if (Fecha.toStringDate(ord.FechaSistemaCreacion).equalsIgnoreCase(fechaLiquidacionOut.fechaInicial) && ord.DiasLiquidacionContado.equals(fechaLiquidacionOut.numeroDiasInicial)){
							ord.FechaLiquidacion = Fecha.toDate(fechaLiquidacionOut.fechaCalculada);
						}
						if(!ord.IdTipoCanalIngreso.equalsIgnoreCase("CLI")) {
							FechasOut fechaConfirmacionOut = (FechasOut) hashFechas.get(ord.IdOrden*-1);
							if(productosConConfirmacion.contains(ord.IdTipoInstrumento)){
								ord.FechaConfirmacionPlazo = Fecha.toDate(fechaConfirmacionOut.fechaCalculada);
								ord.IdEstadoConfirmacion = _c.ESTADO_CONFIRMACION_ORDEN_NO;
								ord.FechaConfirmacion = Fecha.toDateTime("0001-01-01");
								ordenesConfirmacion.add(ord);
							}else{
								ord.FechaConfirmacionPlazo = Fecha.toDate("0001-01-01");
								ord.FechaConfirmacion = Fecha.toDateTime("0001-01-01");
								ord.IdEstadoConfirmacion = "";
							}
						}
				}
				//ordenProceso.FechaLiquidacion = Fecha.toDate(outObtenerDiaSiguiente.fechas.get(0).fechaCalculada);
				
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(23L,"PROCESANDO","Validando fechas de vencimiento");
				}
			}
			//FECHAS VENCIMIENTO
			{
				ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
				inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
				inObtenerDiaSiguiente.p_diaHabil = "N";
				
				for (OrdenProceso ord : ordenes) {
					FechasIn f = new FechasIn();
					f.IdInterno = ord.IdOrden;
					if (ord.EsVentaCorta.equalsIgnoreCase(_c.SI)) {
						f.numeroDias = ord.PlazoVentaCorta;
						f.fechaInicial = usaFechaLiquidacionEnVencimiento.equals(_c.SI) ? Fecha.toStringDate(ord.FechaLiquidacion) : Fecha.toStringDate(ord.FechaSistemaCreacion);
					}
					else if (ord.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
						f.numeroDias = ord.PlazoSimultanea;
						f.fechaInicial = Fecha.toStringDate(ord.FechaLiquidacion);
					}
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						f.numeroDias = ord.PlazoIF;
						f.fechaInicial = Fecha.toStringDate(ord.FechaLiquidacion);
					}
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						f.numeroDias = ord.PlazoReporte;
						f.fechaInicial = Fecha.toStringDate(ord.FechaLiquidacion);
					}
					if (f.numeroDias != null) {
						inObtenerDiaSiguiente.p_fechas.add(f);
					}
				}
				
				if (inObtenerDiaSiguiente.p_fechas.size() > 0) {
					OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(con, inObtenerDiaSiguiente);
					HashMap<Long,Object> hashFechas = Utils.List2Hash(outObtenerDiaSiguiente.p_out_fechas,"IdInterno");
					for (OrdenProceso ord : ordenes) {
							FechasOut fechaVencimientoOut = (FechasOut) hashFechas.get(ord.IdOrden);
							if(fechaVencimientoOut != null) {
								if (!((usaFechaLiquidacionEnVencimiento.equals(_c.SI) ? Fecha.toStringDate(ord.FechaLiquidacion) : Fecha.toStringDate(ord.FechaSistemaCreacion)).equals(fechaVencimientoOut.fechaInicial) && ord.PlazoVentaCorta.equals(fechaVencimientoOut.numeroDias) && ord.EsVentaCorta.equalsIgnoreCase(_c.SI))
										&& !(Fecha.toStringDate(ord.FechaLiquidacion).equals(fechaVencimientoOut.fechaInicial) && ord.PlazoSimultanea.equals(fechaVencimientoOut.numeroDias) && ord.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM))
										&& !(Fecha.toStringDate(ord.FechaLiquidacion).equals(fechaVencimientoOut.fechaInicial) && ord.PlazoReporte.equals(fechaVencimientoOut.numeroDias) && ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)))
									continue;
								if (ord.EsVentaCorta.equalsIgnoreCase(_c.SI)) {
									ord.FechaVencimientoVentaCorta = Fecha.toDate(fechaVencimientoOut.fechaCalculada);
								}
								else if (ord.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
									ord.FechaVencimientoSimultanea = Fecha.toDate(fechaVencimientoOut.fechaCalculada);
								}							
								else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
									ord.FechaVencimientoReporte = Fecha.toDate(fechaVencimientoOut.fechaCalculada);
								}
							}
							
					}
					
				}
			}
			
			//PREFERENCIAS DE PAGO
			HashMap<Long,PreferenciaPagoPortafolio> preferenciasPago = new HashMap<>();
			{
				List<Long> idsPreferenciasPagoPortafolio = new ArrayList<Long>();
				HashMap<String,Object> paramsInPreferenciasPago = new HashMap<>();
				for (OrdenProceso ord : ordenes) {
					if (ord.IdPreferenciaPagoPortafolio != null)
						idsPreferenciasPagoPortafolio.add(ord.IdPreferenciaPagoPortafolio);
				}
				
				if (idsPreferenciasPagoPortafolio.size() >0 ) {
					paramsInPreferenciasPago.put("idsPreferenciasPagoPortafolio", idsPreferenciasPagoPortafolio);
					List<PreferenciaPagoPortafolio> _preferencias = _sql.query(con, SQL_SELECT_PREFERENCIA_PAGO, paramsInPreferenciasPago, PreferenciaPagoPortafolio.class);
					for (PreferenciaPagoPortafolio pago : _preferencias) {
						preferenciasPago.put(pago.IdPreferenciaPagoPortafolio, pago);
					}
				}if(preferenciasPago.size()== 0){
					//REVISA LAS PREFERENCIAS DE PAGO CUANDO _preferencias NO RESCATE NADA (SIN DEFINIR)
					try {
						HashMap<String,Object> params = new HashMap<String,Object>();
						List<Long> idsPortafolio = new ArrayList<Long>();
						List<String> idTipoOperacion = new ArrayList<String>();
						List<String> idMonedaLiquidacionOrden = new ArrayList<String>();
						List<Long>idOrden = new ArrayList<Long>();
						for (OrdenProceso ord : ordenes) {
							if(ord.PagoCobroCuentaInversion.equals("N") && (ord.IdPreferenciaPagoPortafolio==null || ord.IdPreferenciaPagoPortafolio==0)) {
								idsPortafolio.add(ord.IdPortafolioCliente);
								idTipoOperacion.add(ord.IdTipoOperacion);
								idMonedaLiquidacionOrden.add(ord.IdMonedaLiquidacionOrden);
								idOrden.add(ord.IdOrden);
							}
						}
						params.put("IdPortafolio", idsPortafolio);
						params.put("IdTipoOperacion", idTipoOperacion);
						params.put("IdMonedaLiquidacionOrden", idMonedaLiquidacionOrden);
	
						List<PreferenciaPagoPortafolio> _preferencias = _sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_, params, PreferenciaPagoPortafolio.class);
						for (PreferenciaPagoPortafolio pago : _preferencias) {
							if(pago.IdMedioDePago.equals("CIN")) {
								//si encuentro algo debo asignarlo a ord.IdPreferenciaPagoPortafolio = pago.IdPreferenciaPagoPortafolio
								preferenciasPago.put(pago.IdPreferenciaPagoPortafolio, pago);
								OrdenProceso orden = ordenes.stream()
					                    .filter(p -> p.IdOrden.equals(idOrden.get(0)))
					                    .findFirst()
					                    .orElse(null);
								orden.IdPreferenciaPagoPortafolio = pago.IdPreferenciaPagoPortafolio;
							}
						}
					}catch(Exception er) {
						er.printStackTrace();
					}
				}				
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(29L,"PROCESANDO","Consultando requisitos operativos");
				}
			}
			
			//requisitos
			HashMap<String, RequisitoOperativoCrearOrden> requisitos = new HashMap<>();
			{
				HashMap<String,Object> paramsInRequisitosOperativos = new HashMap<>();
				paramsInRequisitosOperativos.put("IdEmpresa", in.p_IdEmpresa);
				List<RequisitoOperativoCrearOrden> _requisitos = _sql.query(con, SQL_SELECT_REQUISITOS_OPERATIVOS,paramsInRequisitosOperativos,RequisitoOperativoCrearOrden.class);
				HashMap<Object,Object> __requisitos = Utils.List2Hash(_requisitos, "IdTipoCanalIngreso");
				for(Object key: __requisitos.keySet()) {
					requisitos.put((String)key,(RequisitoOperativoCrearOrden) __requisitos.get(key));
				}
			}
			
			//Paises clientes
			HashMap<Long, PaisResidenciaPrincipalCliente> paisesResidenciaPrincipalCliente = new HashMap<>();
			{
				Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
				HashMap<String,Object> paramsPaisResidenciaPricipalCliente = new HashMap<String, Object>(3);
				List<Long> idPersonas = ordenes.parallelStream().map(orden -> orden.IdPersonaCliente).collect(Collectors.toList());
				paramsPaisResidenciaPricipalCliente.put("IdPersonas", idPersonas);
				paramsPaisResidenciaPricipalCliente.put("IdPaisSistema", idPaisSistema);
				List<PaisResidenciaPrincipalCliente> paisesResidenciaPrincipal = _sql.query(con, SQL_SELECT_PAIS_RESIDENCIA_PRINCIPAL_CLIENTE, paramsPaisResidenciaPricipalCliente, PaisResidenciaPrincipalCliente.class);
	
				HashMap<Object,Object> __paisesResidenciaPrincipalCliente = Utils.List2Hash(paisesResidenciaPrincipal, "IdPersona");
				for(Object key: __paisesResidenciaPrincipalCliente.keySet()) {
					paisesResidenciaPrincipalCliente.put((Long)key,(PaisResidenciaPrincipalCliente) __paisesResidenciaPrincipalCliente.get(key));
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(32L,"PROCESANDO","Validando instrumentos de negociacion");
				}
			}
			
			//VALIDACION INSTRUMENTO NEGOCIACION
			{
				ConsultarNemoNegociacion consultarNemoNegociacion = new ConsultarNemoNegociacion();
				ConsultarInstrumentoNegociacion_Entrada entradaNemoNegociacion = new ConsultarInstrumentoNegociacion_Entrada();
				List<DatosInstrumento> listaEntradaNemoNegociacion =  new ArrayList< DatosInstrumento>();
				List<Long> listInstrumentos = new ArrayList<Long>();
				
				for(OrdenProceso ord: ordenes) {
					if(ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {
						
						DatosInstrumento datosNemoNegociacion =  new DatosInstrumento();
						datosNemoNegociacion.IdConsulta = ord.IdOrden;
						datosNemoNegociacion.IdMoneda = ord.IdMonedaOrden == null || "".equals(ord.IdMonedaOrden.trim()) ? ord.IdMonedaInstrumento:ord.IdMonedaOrden;
						datosNemoNegociacion.IdInstrumentoRaiz = ord.IdInstrumento;
						datosNemoNegociacion.IdCondLiquidacion = ord.IdCondicionDeLiquidacion;
						listaEntradaNemoNegociacion.add(datosNemoNegociacion);
					}else{
						listInstrumentos.add(ord.IdOrden);
					}
				}
				entradaNemoNegociacion.p_DatosInstrumentoConsulta = listaEntradaNemoNegociacion;
				DatosInstrumentoNegociacion_Salida salidaNemoNegociacion = consultarNemoNegociacion.logica(con,entradaNemoNegociacion);
			
				for(OrdenProceso ord: ordenes) {
					if(ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {
						for(DatosInstrumentoNegociacion sal: salidaNemoNegociacion.P_InstrumentoSalida){
							if(ord.IdOrden.equals(sal.IdConsulta)){
								ord.IdInstrumentoNegociacion = sal.IdInstrumento;
								ord.InstrumentoNegociacion = sal.Nemotecnico;
								break;
							}
						}
					}else{
						for(Long list: listInstrumentos){
							if(ord.IdOrden.equals(list)){
							ord.IdInstrumentoNegociacion = ord.IdInstrumento;
							ord.InstrumentoNegociacion = ord.Nemotecnico;
							break;
							}
						}
					}
				}
			}
			
			//VALIDACIONES BASICAS
			{
				List<DiasAConsultar> fechas = new ArrayList<DiasAConsultar>();
				for (OrdenProceso orden: ordenes){
					DiasAConsultar fecha = new DiasAConsultar();
					fecha.IdInterno= orden.IdOrden;
					fecha.fechaConsulta= Fecha.toStringDate(orden.FechaVigencia);
					fechas.add(fecha);
				}
				List<DiasAConsultarSalida>  fechasProcesadas =  esDiaHabilMasivo(fechas,in.p_IdEmpresa,con);
				HashMap<Long,Object> hashfechasProcesadas = Utils.List2Hash(fechasProcesadas, "IdInterno");
				for (OrdenProceso ord: ordenes) {
					LocalDate fechaVigencia =  LocalDate.parse(ord.FechaVigencia.toString());
					LocalDate fechaOperacion = ord.FechaOperacion.toLocalDateTime().toLocalDate();
					DiasAConsultarSalida fechaProcesada = (DiasAConsultarSalida) hashfechasProcesadas.get(ord.IdOrden);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdClienteReal == null),"CLIENTE_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdCategoriaInversionista == "" || ord.IdCategoriaInversionista.equalsIgnoreCase("SIN CATEGORIA")),"CLIENTE_SIN_CATEGORIA_INVERSIONISTA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.PagoCobroCuentaInversion == null),"PAGO_COBRO_CUENTA_INVERSION_NO_ESPECIFICADA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.CodigoClaseOrden == null),"MECANISMO_CAPTACION_NO_ESPECIFICADO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdPortafolioReal == null),"PORTAFOLIO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdInstrumentoReal == null),"INSTRUMENTO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.EsRaiz.equalsIgnoreCase("N")),"ERROR_NEMO_NO_ES_RAIZ",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoOperacionReal==null),"TIPO_OPERACION_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdBoveda != null && ord.IdBovedaReal==null),"BOVEDA_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoVigenciaReal == null),"TIPO_VIGENCIA_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdFormaCustodiaReal == null),"FORMA_CUSTODIA_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoPrecioReal == null),"TIPO_PRECIO_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdTipoLibroOrden.equalsIgnoreCase("NORMAL") && ord.IdLibroOrdenes == null),"LIBRO_ORDENES_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoCanalDistribucionReal == null ),"CANAL_DISTRIBUCION_NO_CONFIGURADO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdEmpleadoDigitador == null || (ord.IdEmpleadoDigitador == 0 && !ord.IdTipoCanalIngreso.equals("CLI"))),"CODIGO_EJECUTIVO_DIGITADOR_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdEmpleadoPortafolio == null || ord.IdEmpleadoPortafolio == 0),"EJECUTIVO_PORTAFOLIO_NO_CONFIGURADO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.EspecificaBolsa.equals("N") && ord.IdBolsaReal==null),"BOLSA_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdCondicionDeLiquidacionContadoReal == null),"CONDICION_LIQUIDACION_CONTADO_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdPorcentajeVisibilidadReal == null || ord.IdPorcentajeVisibilidadReal < 0),"PORCENTAJE_VISIBILIDAD_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(localizacionSistema.equalsIgnoreCase("PE") && (ord.IdCuentaCustodioPortafolio == null || ord.IdCuentaCustodioPortafolio == 0)) ,"UI_CUENTA_CUSTODIO_OBLIGATORIA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoInstrumento.equalsIgnoreCase("RRF") && ord.AccionOrden.equalsIgnoreCase("VENTA") && ord.FechaVencimientoReporte.compareTo(ord.FechaVencimientoInstrumentoSubyacente) == 1 ),"PLAZO_INVALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,("S".equalsIgnoreCase(ord.EsVentaCorta) && !"0001-01-01".equals(ord.FechaVencimientoInstrumento.toString()) && ord.FechaVencimientoVentaCorta.compareTo(ord.FechaVencimientoInstrumento) == 1 ),"PLAZO_VENTA_CORTA_SUPERA_FECHA_VENCIMIENTO_INSTRUMENTO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(!fechaProcesada.habil),"FECHA_VIGENCIA_INHABIL",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,( ChronoUnit.DAYS.between(fechaOperacion,fechaVigencia)>29l && _c.CLASIFICACION_RV.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento)),"FECHA_VIGENCIA_ORDENES",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,( ChronoUnit.DAYS.between(fechaOperacion,fechaVigencia)<0l),"FECHA_VIGENCIA_INHABIL",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(0 == ord.IdInstrumentoNegociacion|| null == ord.IdInstrumentoNegociacion),"INSTRUMENTO_NEGOCIACION_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
				}
				
				limpiarOrdenes(ordenes, ordenesConErrores);
				if (ordenes.size() == 0) {
					for(MensajeSalida mje : out.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
							out.P_OUT_ERRORESCONTADOR++;
						}
					}
					if(out.P_OUT_ERRORESCONTADOR.compareTo(0L)>=1L) {
						{
							if(in.p_BarraProgreso != null) {
								JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
								in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
							}
						}
					}
					return out;
				}
								
			}
			
			{  //validaciones tabla Vela MBI

				HashMap<String,Object> params = new HashMap<>();
				List<Vela> TablaVela = new ArrayList<>();
				TablaVela = _sql.query(con, SQL_SELECT_TABLA_VELA, params, Vela.class);	
				for (OrdenProceso ord: ordenes) {	
					if ("ACN".equalsIgnoreCase(ord.IdTipoInstrumento)||"ETN".equalsIgnoreCase(ord.IdTipoInstrumento) ) {				    	
						TablaVela.parallelStream().filter(vela -> vela.IdVela != null).forEach(vela -> {		
							if (ord.PrecioOrden.compareTo(vela.PrecioMin)>=0   && ord.PrecioOrden.compareTo(vela.PrecioMax)<=0){
					    		if( validacionTablaVela(ord.PrecioOrden ,vela.FactorDecimal,vela.DecimasNoEntero)) { 
					    			valores.put("Multiplo",vela.Multiplo);
					    			try {
										out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_PRECIO_MULTIPLO",valores,con,in.p_IdEmpresa));
										ord.errores++;	
					    			} catch (Exception e) {
										e.printStackTrace();
										}
					    			}
					    		}
							});
						}
					}
				
				limpiarOrdenes(ordenes, ordenesConErrores);
				if (ordenes.size() == 0) {
					for(MensajeSalida mje : out.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
							out.P_OUT_ERRORESCONTADOR++;
						}
					}
				}
			}		
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(39L,"PROCESANDO","Validando ingreso de garantias");
				}
			}
			
			//INGRESO DE GARANTIAS
			if (in.p_Garantias.size() > 0) {
				CrearGarantiasOrdenes_Entrada entradaGarantias = new CrearGarantiasOrdenes_Entrada();
				entradaGarantias.p_IdEmpresa = in.p_IdEmpresa;
				entradaGarantias.p_IdUsuario = in.p_IdUsuario;
				entradaGarantias.p_IdModulo = in.p_IdModulo;
				entradaGarantias.p_GarantiasCustodia = new ArrayList<GarantiaCustodia_Entrada>();
				entradaGarantias.p_GarantiasCuentaInversion = new ArrayList<GarantiaCtaInversion_Entrada>();
				
				for (ObjGarantiasIn gar: in.p_Garantias) {
					if (gar.IdTipoGarantia.equalsIgnoreCase("CUS")) {
						GarantiaCustodia_Entrada ent = new GarantiaCustodia_Entrada();
						//Utils.copyPojo(gar, ent);
						ent.IdInstrumento = gar.IdInstrumento;
						ent.IdInstrumentoGarantizado = gar.IdInstrumentoGarantizado;
						ent.IdCliente = gar.IdCliente;
						ent.IdPortafolio = gar.IdPortafolio;
						ent.Cantidad = gar.Cantidad;
						ent.IdBolsa = gar.IdBolsa;
						ent.Precio = gar.Precio;
						ent.Monto = gar.Monto;
						//ent.IdTipoOperacion = gar.IdTipoOperacion;
						//ent.PlazoEnDias = gar.PlazoEnDias;
						//ent.IdOperacion = gar.IdOperacion;
						//ent.Tasa = gar.Tasa;
						//ent.FolioCarteraGarantizada = gar.FolioCarteraGarantizada;
						
						for (ObjOrdenesEntrada ordenEntrada : in.p_Ordenes) {
							ent.IdTipoOperacion = ordenEntrada.IdTipoOperacion;
							ent.PlazoEnDias 			= ordenesHash.get(ordenEntrada.IdInterno).IdClasificacionTipoInstrumento.equals("RE")?ordenEntrada.PlazoReporte:ordenEntrada.PlazoSimultanea;
							ent.IdOperacion 			= in.p_Ordenes.size()>1? 0: ordenesHash.get(ordenEntrada.IdInterno).IdOrden;
							ent.Tasa 					= ordenesHash.get(ordenEntrada.IdInterno).IdClasificacionTipoInstrumento.equals("RE")?ordenesHash.get(ordenEntrada.IdInterno).TasaReporte:ordenesHash.get(ordenEntrada.IdInterno).TasaSimultanea;
							ent.FolioCarteraGarantizada = 0L;
						}
						entradaGarantias.p_GarantiasCustodia.add(ent);
					} else {
						GarantiaCtaInversion_Entrada ent = new GarantiaCtaInversion_Entrada();
						//Utils.copyPojo(gar, ent);
						ent.IdMoneda = gar.IdMoneda;
						ent.IdCliente = gar.IdCliente;
						ent.IdPortafolio = gar.IdPortafolio;
						ent.Monto = gar.Monto;
						ent.IdInstrumentoGarantizado = gar.IdInstrumentoGarantizado;
						//ent.IdTipoOperacion = gar.IdTipoOperacion;
						//ent.IdOperacion = gar.IdOperacion;
						
						for (ObjOrdenesEntrada ordenEntrada : in.p_Ordenes) {
							ent.IdTipoOperacion = ordenEntrada.IdTipoOperacion;
						}
						entradaGarantias.p_GarantiasCuentaInversion.add(ent);
					}
				}
				
				CrearGarantiasOrdenes crearGarantiasService = new CrearGarantiasOrdenes();
				CrearGarantiasOrdenes_Salida salidaGarantias = crearGarantiasService.logica(con, entradaGarantias);
				if (MensajeSalida.getNumeroErrores(salidaGarantias.p_out_Mensajes) > 0) {
					eliminarOrdenBloqueada(con,in.p_IdEmpresa, valores, in.p_Ordenes, out.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(salidaGarantias.p_out_Mensajes);
					{
						if(in.p_BarraProgreso != null) {
							JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
							in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
						}
					}
					
					return out;
				}
				
				for (CrearGarantiasOrdenes.GarantiaCustodia_Salida gar : salidaGarantias.p_out_GarantiasCustodia) {
					ObjGARANTIAS garantiaSalida = new ObjGARANTIAS();
					garantiaSalida.FolioGarantia = gar.FolioGarantia;
					garantiaSalida.IdMonedaInstrumento = gar.IdMonedaInstrumento;
					garantiaSalida.Instrumento = gar.Instrumento;
					garantiaSalida.Cantidad = gar.Cantidad;
					garantiaSalida.Monto = gar.Monto;
					garantiaSalida.IdMoneda = "";
					garantiaSalida.IdTipoGarantia = "CUS";
					out.P_OUT_GARANTIAS.add(garantiaSalida);
				}
				
				for (CrearGarantiasOrdenes.GarantiaCtaInversion_Salida gar : salidaGarantias.p_out_GarantiasCtaInversion) {
					ObjGARANTIAS garantiaSalida = new ObjGARANTIAS();
					garantiaSalida.FolioGarantia = gar.FolioGarantia;
					garantiaSalida.IdMonedaInstrumento = "";
					garantiaSalida.Instrumento = "";
					garantiaSalida.Cantidad = BigDecimal.ZERO;
					garantiaSalida.Monto = gar.Monto;
					garantiaSalida.IdMoneda = "";
					garantiaSalida.IdTipoGarantia = "CTA";
					out.P_OUT_GARANTIAS.add(garantiaSalida);
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(45L,"PROCESANDO","Consultando indicadores de clientes");
				}
			}
			
			//INDICADORES CLIENTEs
			HashMap<Long, IndicadoresClienteCrearOrden> indicadoresCliente = new HashMap<>();
			{
				HashMap<String,Object> paramsInIndicadorCliente = new HashMap<>();
				ArrayList<Long> idsCliente = new ArrayList<>();
				
				for (OrdenProceso ord : ordenes) {
					idsCliente.add(ord.IdCliente);
				}
				
				paramsInIndicadorCliente.put("IdsCliente", idsCliente);
				List<IndicadoresClienteCrearOrden> _indicadoresCliente = _sql.query(con, SQL_SELECT_INDICADORES_CLIENTE, paramsInIndicadorCliente, IndicadoresClienteCrearOrden.class);
				HashMap<Object, Object> __indicadoresCliente = Utils.List2Hash(_indicadoresCliente, "IdCliente");
				for(Object key: __indicadoresCliente.keySet()) {
					indicadoresCliente.put((Long)key,(IndicadoresClienteCrearOrden) __indicadoresCliente.get(key));
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(47L,"PROCESANDO","Consultando indicadores de portafolios");
				}
			}
			
			//INDICADORES PORTAFOLIO
			HashMap<Long, IndicadoresPortafolio> indicadoresPortafolio = new HashMap<>();
			{
				HashMap<String,Object> paramsInIndicadorPortafolio = new HashMap<>();
				ArrayList<Long> idsPortafolio = new ArrayList<>();
				
				for (OrdenProceso ord : ordenes) {
					idsPortafolio.add(ord.IdPortafolioCliente);
				}
				
				paramsInIndicadorPortafolio.put("idsPortafolio", idsPortafolio);
				List<IndicadoresPortafolio> _indicadoresPortafolio = _sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsInIndicadorPortafolio, IndicadoresPortafolio.class);
				HashMap<Object, Object>__indicadoresPortafolio = Utils.List2Hash(_indicadoresPortafolio, "IdPortafolio");
				for(Object key: __indicadoresPortafolio.keySet()) {
					indicadoresPortafolio.put((Long)key,(IndicadoresPortafolio) __indicadoresPortafolio.get(key));
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(51L,"PROCESANDO","Actualizando indicadores");
				}
			}
			
			//SVALIN-491: Opera con saldo Negativo // CORRECCION DE FUNCIONAMIENTO EN PARAMETRO QUE PERMITE OPERAR ORDENES SIN SALDO EN LA CTA INVERSION.
			HashMap<String, Object> paramsInOPCSN = new HashMap<>();

			paramsInOPCSN.put("IdPortafolio", ordenes.stream().map(ord -> ord.IdPortafolioCliente).collect(Collectors.toList()));
			
			List<ParametroOperaCSN> parametroOPCSN = _sql.query(con, SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO, paramsInOPCSN, ParametroOperaCSN.class);
			
			Map<Long, String> mapaOperaCSN = parametroOPCSN.stream()
				    .collect(Collectors.toMap(
				        p -> p.IdPortafolio,
				        p -> p.OperaConSaldoNegativo
				    ));
			
			//actualiza orden con indicadores
			{
				for (OrdenProceso ord: ordenes) {
					
					ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente) == null),"INDICADORES_CLIENTE_NO_EXISTEN",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					
					if (indicadoresCliente.get(ord.IdCliente) == null)
					{
						continue;
					}
					
					ord.OperaConSaldoNegativo = mapaOperaCSN.getOrDefault(ord.IdPortafolioCliente, "N");
					
					if (ord.PagoCobroCuentaInversion.equalsIgnoreCase(_c.SI) && ord.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA) && (ord.OperaConSaldoNegativo.equalsIgnoreCase("N") || !ord.IngresoDesde.equalsIgnoreCase("COR"))) {
						ord.ValidaSaldoCaja = _c.SI;
					}
					
					if (localizacionSistema.equalsIgnoreCase("CL") || localizacionSistema.equalsIgnoreCase("PE")) {
						ord.CodigoProcedenciaOrden = indicadoresCliente.get(ord.IdCliente).CodigoProcedenciaOrden;
					}

					//Se valida si el parametro de liquidacion comision de la factura se encuentra encendido
					if("S".equals(paramLiquidacionComisionFact)) {
						String indicadorTipoLiquidacionPortafolio = indicadoresPortafolio.get(ord.IdPortafolioCliente).ModificaTipoLiquidacion;
						//Se valida en caso que el campo venga vacio
						indicadorTipoLiquidacionPortafolio = indicadorTipoLiquidacionPortafolio == null ? "" : indicadorTipoLiquidacionPortafolio;
						
						if (ord.TipoLiquidacionFactura == null || "".equals(ord.TipoLiquidacionFactura)) {
							ord.TipoLiquidacionFactura  = determinaTipoLiquidacion(indicadorTipoLiquidacionPortafolio,tipoLiquidacionPorDefecto);
						}
						
						ord.CodigoModoFacturacionPortafolio  = determinaCodigoModoFactura(indicadorTipoLiquidacionPortafolio,tipoLiquidacionPorDefecto);
					}
					else {
						ord.TipoLiquidacionFactura = "DIR";
						ord.CodigoModoFacturacionPortafolio = "MODPREFNORM";
					}

					ProcesaRequisito(con,in.p_IdEmpresa,(ord.CodigoProcedenciaOrden == null || ord.CodigoProcedenciaOrden.length() >2 || ord.CodigoProcedenciaOrden.length() <=0),"INDICADOR_PROCEDENCIA_CLIENTE_INVALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					
					if (localizacionSistema.equalsIgnoreCase("PE")) {
						ord.IndicadorInstrumentoAcreditado = indicadoresCliente.get(ord.IdCliente).InstrumentoAcreditado;
					}else{
						ord.IndicadorInstrumentoAcreditado = _c.NO;
					}
					
					if (ord.IdFormaCustodia.equalsIgnoreCase("CUS") && ord.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA) && ord.EsVentaCorta.equals(_c.NO) && !ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_DV)) {
						ord.ValidaSaldoCustodia = _c.SI;
					} else {
						ord.ValidaSaldoCustodia = _c.NO;
					}
					
					//SI ES ORDEN PARA LIBRO NORMAL, SE OBTIENE DESDE EL PORTAFOLIO
					if (ord.IdTipoLibroOrden.equalsIgnoreCase("NORMAL")){
						if (ord.IdLibroOrdenes == null) ord.IdLibroOrdenes = libroSegunCanalDistribucion(ord.IdTipoCanalDistribucion, indicadoresPortafolio.get(ord.IdPortafolioCliente));
						if (ord.IdLibroOrdenes == 0) ord.IdLibroOrdenes = libroPorDefecto(con, ord.IdEmpresa);
					}
					
					// Determina si la orden cumple requisitos para una posible venta Descubierta (SOLO CHILE, NO POSTRERA, NO iCliente , RV, VENTA, CN, INDICADOR "PermiteVentaDescubierta" = S)		
					if (localizacionSistema.equalsIgnoreCase("CL") && ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && ord.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA) && ord.IdCondicionDeLiquidacion.equalsIgnoreCase("T2") && indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteVentaDescubierta.equalsIgnoreCase("S"))
					{
						ord.CumpleParaVentaDescubierta = _c.SI;
					}
					
					//VALIDA QUE SOLO CLIENTES QUE TENGAN EL INDICADOR INVERSIONISTA CALIFICADO OPEREN CON INSTRUMENTOS CALIFICADOS
					ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).InversionistaCalificado.equalsIgnoreCase(_c.NO) && ord.EsInstrumentoCalificado.equals(_c.SI)),"CLIENTE_NO_OPERA_INSTRUMENTO_CALIFICADO","CO_InstrumentoCalificado",ord,requisitos,out.P_OUT_MENSAJES);
					
				}
				
				limpiarOrdenes(ordenes, ordenesConErrores);
				if (ordenes.size() == 0) {
					for(MensajeSalida mje : out.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
							out.P_OUT_ERRORESCONTADOR++;
						}
					}
					if(out.P_OUT_ERRORESCONTADOR.compareTo(0L)>=1L) {
						{
							if(in.p_BarraProgreso != null) {
								JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
								in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
							}
						}
					}
					return out;
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(53L,"PROCESANDO","Validando libro de ordenes");
				}
			}
			
			//LIBROS
			HashMap<Long, LibroOrdenes> librosOrdenes = new HashMap<>();
			{
				HashMap<String,Object> paramsInLibroOrdenes = new HashMap<>();
				ArrayList<Long> idsLibroOrdenes = new ArrayList<>();
				
				for (OrdenProceso ord : ordenes) {
					if (ord.IdLibroOrdenes != null)
						idsLibroOrdenes.add(ord.IdLibroOrdenes);
				}
				paramsInLibroOrdenes.put("idsLibroOrdenes", idsLibroOrdenes);
				List<LibroOrdenes> _libros = _sql.query(con, SQL_SELECT_LIBROS_ORDENES, paramsInLibroOrdenes, LibroOrdenes.class);
				
				for (LibroOrdenes libro : _libros) {
					librosOrdenes.put(libro.IdLibroOrdenes, libro);
				}
			}
			
			//ORDENES REPETIDAS, PERU
			{
				if (localizacionSistema.equalsIgnoreCase("PE")) {
					List<String> llaves = new ArrayList<String>();
					for (OrdenProceso ord : ordenes) {
						llaves.add(ord.IdPortafolioCliente+ord.IdTipoOperacion+ord.IdInstrumento);
					}
					if (llaves.size() > 0) {
						HashMap<String,Object> params1 = new HashMap<>();
						params1.put("llaves", llaves);
						params1.put("fechaSistema", fechaSistema);
						List<Orden> ordenesRepetidas = _sql.query(con, SQL_SELECT_ORDEN_EXISTENTE, params1, Orden.class);
						for (OrdenProceso ord : ordenes) {
							for (Orden ord2 : ordenesRepetidas) {
								if (ord.IdPortafolioCliente.equals(ord2.IdPortafolioCliente) 
										&& ord.IdInstrumento.equals(ord2.IdInstrumento)
										&& ord.IdTipoOperacion.equalsIgnoreCase(ord2.IdTipoOperacion)) {
									ord.ExisteOrdenMismaOperacionInstrumento = _c.SI;
								}
							}
						}
					}
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(56L,"PROCESANDO","Consultando precios de mercado");
				}
			}
			
			//PRECIO MERCADO
			{	
				EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
				entregarPreciosTasasEntrada.p_IdEmpresa = in.p_IdEmpresa;
				entregarPreciosTasasEntrada.p_IdModulo = in.p_IdModulo;
				entregarPreciosTasasEntrada.p_IdPais = idPaisEmpresa;
				entregarPreciosTasasEntrada.p_IdUsuario = in.p_IdUsuario;
				
				for (OrdenProceso ord : ordenes) {
					if ((ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && !ord.IdTipoInstrumento.equalsIgnoreCase("SIM"))) {
						EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
						entradaDatosInstrumentos.IdRegistro = ord.IdOrden;
						entradaDatosInstrumentos.TipoCartera = "0";
						entradaDatosInstrumentos.IdUso = "ORDEN";
						entradaDatosInstrumentos.IdTipoCriterioTasa = "0";
						entradaDatosInstrumentos.IdClasificacionTipoInstrumento = "RV";
						entradaDatosInstrumentos.IdTipoInstrumento = "0";
						entradaDatosInstrumentos.IdInstrumento = ord.IdInstrumentoNegociacion!=0L ? ord.IdInstrumentoNegociacion: ord.IdInstrumentoCustodia;
						entradaDatosInstrumentos.Nemotecnico = ord.Nemotecnico;
						entradaDatosInstrumentos.IdMoneda =  ord.IdMonedaOrden == null || "".equals(ord.IdMonedaOrden.trim()) ? ord.IdMonedaInstrumento:ord.IdMonedaOrden;
						entradaDatosInstrumentos.FechaConsulta = ord.FechaSistemaCreacion;  
						entradaDatosInstrumentos.PlazoResidual = 0L;
						entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
					}
				}
				
				
				if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
					EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
					EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
					HashMap<String, Object> precios = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS,"IdRegistro");
					for (OrdenProceso ord : ordenes) {
						
						if (!ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)){
							EntregarPreciosTasas_Salida_PreciosTasas registroPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) precios.get(ord.IdOrden);
							if (registroPrecio != null && registroPrecio.Precio != null && registroPrecio.Precio.doubleValue() > 0) {
								ord.PrecioMercado = registroPrecio.Precio;
								if (ord.IdTipoPrecio.equalsIgnoreCase(_c.TIPO_PRECIO_MERCADO)) {
									ord.PrecioOrden = ord.PrecioMercado;
								}
							}
						}
					
							
					}
				}
				
				
//Precios RV Banco Estado https://jira.bcs.cl/browse/GCESTENROL-6 CGO
					if (calculoPrecioPunta.equalsIgnoreCase("S")) {
					
						ConsultarInstrumentoPrecioBolsa_Entrada objConsultaPrecio_Entrada = new ConsultarInstrumentoPrecioBolsa_Entrada();
						ConsultarInstrumentoPrecioBolsa objConsultaPrecio = new ConsultarInstrumentoPrecioBolsa();							
						ConsultarInstrumentoPrecioBolsa_Salida objConsultaPrecio_Salida;
						
						objConsultaPrecio_Entrada.p_IdEmpresa = in.p_IdEmpresa;
						objConsultaPrecio_Entrada.p_IdUsuario = in.p_IdUsuario;
					
						for (ObjOrdenesEntrada ord: in.p_Ordenes) {
							ConsultarInstrumentoPrecioBolsa.Instrumento objInstrumento = new ConsultarInstrumentoPrecioBolsa.Instrumento();
							objInstrumento.Fecha =  fechaSistema;				
							objInstrumento.IdEmpresa = in.p_IdEmpresa;
							objInstrumento.IdInstrumento = "COMSIM".equalsIgnoreCase(ord.IdTipoOperacion)|| "VENSIM".equalsIgnoreCase(ord.IdTipoOperacion)?ord.IdInstrumentoSubyacente : ord.IdInstrumento;						
							objConsultaPrecio_Entrada.p_Instrumentos.add(objInstrumento);
						}
						
						objConsultaPrecio_Salida = objConsultaPrecio.logica(con, objConsultaPrecio_Entrada);
						
						for (OrdenProceso ord : ordenes) {
							BigDecimal puntaCompra =BigDecimal.ZERO;
							BigDecimal puntaVenta =BigDecimal.ZERO;
	
							if (((ord.IdTipoCanalIngreso.equalsIgnoreCase("CLI")  && ParametroPrecioMercado.equalsIgnoreCase("S"))  ||
								 (!ord.IdTipoCanalIngreso.equalsIgnoreCase("CLI"))) 
								&& ord.IdClasificacionTipoInstrumento.contentEquals("RV")){			
									Long instrumento = "SIM".equalsIgnoreCase(ord.IdTipoInstrumento)?ord.IdInstrumentoSubyacente : ord.IdInstrumento;
									BigDecimal precioRef = ord.PrecioMercado;						
									puntaCompra = objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.stream().filter(a -> a.IdInstrumento.equals(instrumento)).findFirst().get().PuntaCompra;
									puntaVenta =  objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.stream().filter(a -> a.IdInstrumento.equals(instrumento)).findFirst().get().PuntaVenta;
									
									if(puntaVenta != null && puntaVenta.compareTo(BigDecimal.ZERO) == 1 && ord.AccionOrden.equals("COMPRA")){//	ord.IdTipoOperacion.equals("COMACN")) {
										if(puntaVenta.doubleValue() > precioRef.doubleValue()) ord.PrecioOrden = puntaVenta;
									} else {
										if(puntaCompra != null && puntaCompra.compareTo(BigDecimal.ZERO) == 1) {
											if(puntaCompra.doubleValue() < precioRef.doubleValue()) ord.PrecioOrden = puntaCompra;
										}
									}
							}						
													
					}					
				}
				
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(59L,"PROCESANDO","Consultando perfil de productos");
				}
			}
			
			//obtiene los productos
			{
				cl.bcs.plataforma.com.ConsultarProductosPortafolio serviceConsultarProductosPortafolio = new cl.bcs.plataforma.com.ConsultarProductosPortafolio();
				cl.bcs.plataforma.com.ConsultarProductosPortafolio.ConsultarProductosPortafolio_Entrada entradaConsultarProductosPortafolio = new cl.bcs.plataforma.com.ConsultarProductosPortafolio.ConsultarProductosPortafolio_Entrada();
				
				entradaConsultarProductosPortafolio.p_IdEmpresa = in.p_IdEmpresa;
				entradaConsultarProductosPortafolio.p_IdModulo = in.p_IdModulo;
				entradaConsultarProductosPortafolio.p_IdUsuario = in.p_IdUsuario;
				
				for (OrdenProceso ord : ordenes) {
					cl.bcs.plataforma.com.ConsultarProductosPortafolio.PortafolioEntrada p = new cl.bcs.plataforma.com.ConsultarProductosPortafolio.PortafolioEntrada();
					p.IdPortafolio = ord.IdPortafolioCliente;
					entradaConsultarProductosPortafolio.p_portafolios.add(p);
				}
				
				cl.bcs.plataforma.com.ConsultarProductosPortafolio.ConsultarProductosPortafolio_Salida salidaConsultarProductosPortafolio = serviceConsultarProductosPortafolio.logica(con, entradaConsultarProductosPortafolio);
				
				for (OrdenProceso ord : ordenes) {
					for (cl.bcs.plataforma.com.ConsultarProductosPortafolio.DetallePerfilProducto detalle : salidaConsultarProductosPortafolio.p_out_PortafolioProductos) {
						if (detalle.IdPortafolio.equals(ord.IdPortafolioCliente)
								&& detalle.IdTipoProducto.equalsIgnoreCase(ord.IdProducto)
								&& (detalle.IdTipoOperacion.equalsIgnoreCase("0") || detalle.IdTipoOperacion.equalsIgnoreCase(ord.IdTipoOperacion))
								&& (detalle.IdTipoCanalIngreso.equalsIgnoreCase("0") || detalle.IdTipoCanalIngreso.equalsIgnoreCase(ord.IdTipoCanalIngreso))
								&& (detalle.IdEstadoDetallePerfilProducto.equalsIgnoreCase("ACT"))
								) {
							ord.PortafolioProductoHabilitado = "S";
							if(detalle.PermiteRuteo.equalsIgnoreCase("S")){
								ord.PortafolioProductoPermiteRuteo = "S";								
							}
						}
					}
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(63L,"PROCESANDO","Calculando cobros");
				}
			}
			
			//INTEGRACION CON CALCULAR MONTO Y COBROS ORDEN
			{
				CalcularMontoYCobrosOrden_Entrada entradaCalcularMontoYCobros = new CalcularMontoYCobrosOrden_Entrada();
				
				HashMap<Long, Object> hashOrdenes = new HashMap<Long, Object>();
				hashOrdenes = Utils.List2Hash(ordenes, "IdInterno");
				
				
				entradaCalcularMontoYCobros.p_IdEmpresa = in.p_IdEmpresa;
				entradaCalcularMontoYCobros.p_IdModulo = in.p_IdModulo;
				entradaCalcularMontoYCobros.p_IdUsuario = in.p_IdUsuario;
				
				if(in.p_CobrosFijos.size()>0){
					for (Cobro_Fijo CobrosFijo : in.p_CobrosFijos) {
						

						OrdenProceso orden = (OrdenProceso) hashOrdenes.get(CobrosFijo.IdInterno);
						
						if (orden != null)
						{	// El calculo solo se hace para las ordenes validas
							CalcularMontoYCobrosOrden.Cobro_Fijo cobroEntrada = copyPojoCalcularMontoYCobrosOrdenCobroFijo(CobrosFijo);
							entradaCalcularMontoYCobros.p_CobrosFijos.add(cobroEntrada);	
						}

					}
				}else{
					for (ObjCobrosModificados cobroModificado : in.p_CobrosModificados) {
						CalcularMontoYCobrosOrden.Cobro_Entrada cobroEntrada = new CalcularMontoYCobrosOrden.Cobro_Entrada();
						//Utils.copyPojo(cobroModificado, cobroEntrada);
						cobroEntrada.IdInterno = cobroModificado.IdInterno;
						cobroEntrada.IdCobro = cobroModificado.IdCobro;
						cobroEntrada.ValorCobroAplicado = cobroModificado.ValorCobroAplicado;
						entradaCalcularMontoYCobros.p_CobrosModificados.add(cobroEntrada);
					}
				}
				
				
				for (OrdenProceso orden : ordenes) {
					CalcularMontoYCobrosOrden.Orden_Entrada ordenEntrada = new CalcularMontoYCobrosOrden.Orden_Entrada(); 
					
					ordenEntrada.CantidadOrden = orden.Cantidad;
					ordenEntrada.EsAPlazo  = "N";
					ordenEntrada.TipoInteres = orden.TipoInteres;
					ordenEntrada.BaseTasa = orden.BaseTasa;
					ordenEntrada.EsFungible = orden.EsFungible;
					ordenEntrada.EsPorMonto  = orden.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_WS) || orden.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_OWP) ? orden.EsPorMonto : "N";
					ordenEntrada.FechaOperacion  = Fecha.toStringDate(orden.FechaSistemaCreacion);
					ordenEntrada.FechaOperacionOriginal = _c.ORIGEN_REFACTURACION.equalsIgnoreCase(orden.GlosaRefactura) && _c.CLASIFICACION_IF.equalsIgnoreCase(orden.IdClasificacionTipoInstrumento) ? Fecha.toStringDate(orden.FechaEmision) : "";
					ordenEntrada.GlosaRefactura = orden.GlosaRefactura;
					ordenEntrada.IdBolsa  = orden.IdBolsa;
					ordenEntrada.IdCorredorCorresponsal = orden.IdCorredorCorresponsal;
					ordenEntrada.IdEmisor = orden.IdEmisor;
					ordenEntrada.IdInstrumento = orden.IdInstrumento;
					ordenEntrada.IdInstrumentoSubyacente = orden.IdInstrumentoSubyacente;
					ordenEntrada.IdInterno = orden.IdInterno;
					ordenEntrada.IdMonedaLiquidacionOrden = orden.IdMonedaLiquidacionOrden;
					ordenEntrada.IdMonedaOrden = orden.IdMonedaOrden; // Solo para IF
					ordenEntrada.IdClasificacionTipoInstrumento= orden.IdClasificacionTipoInstrumento;
					ordenEntrada.IdPortafolio = orden.IdPortafolioCliente;
					ordenEntrada.IdTipoCanalIngreso = orden.IdTipoCanalIngreso;
					ordenEntrada.IdTipoOperacion = orden.IdTipoOperacion;
					ordenEntrada.IdTipoProducto = orden.IdProducto;
					ordenEntrada.MontoOrden = orden.MontoOrden;
					ordenEntrada.MontoTotal = orden.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_WS) || orden.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_OWP) ? orden.MontoOrden : BigDecimal.ZERO;
					ordenEntrada.PlazoReporte = orden.PlazoReporte;
					ordenEntrada.PlazoIF = orden.PlazoIF;
					ordenEntrada.PlazoSimultanea = orden.PlazoSimultanea;
					ordenEntrada.PlazoVentaCorta = orden.PlazoVentaCorta;
					ordenEntrada.TasaRFIF = orden.TasaRFIF;
					ordenEntrada.TasaSimultanea = orden.TasaSimultanea;
					ordenEntrada.TasaVentaCorta = orden.TasaVentaCorta;
					ordenEntrada.TasaReporte = orden.TasaPromedioReporte;
					ordenEntrada.TasaReportado = orden.TasaReporte;
					ordenEntrada.PrecioOrden = orden.PrecioOrden;
					ordenEntrada.TipoCambio = orden.TipoCambio;
					ordenEntrada.ValorRescateOrden = orden.ValorRescate;
					ordenEntrada.MontoAllIn = orden.MontoAllIn;
					ordenEntrada.PorcentajeAllIn = orden.PorcentajeAllIn;
					entradaCalcularMontoYCobros.p_Ordenes.add(ordenEntrada);
				}
				
				CalcularMontoYCobrosOrden serviceCalcularMontoYCobrosOrden = new CalcularMontoYCobrosOrden();
				CalcularMontoYCobrosOrden_Salida salidaCalcularMontoYCobrosOrden = serviceCalcularMontoYCobrosOrden.logica(con, entradaCalcularMontoYCobros);
				
				//TODO ERRORES
				for (CalcularMontoYCobrosOrden.Orden_Salida ordenSalida : salidaCalcularMontoYCobrosOrden.p_out_Ordenes) {
					OrdenProceso ord = ordenesHash.get(ordenSalida.IdInterno);
					ord.PrecioPlazo = ordenSalida.PrecioPlazo;
					ord.Cantidad = ordenSalida.CantidadOperacion;
					ord.IdMonedaOrden = ordenSalida.IdMonedaOrden;
					ord.MontoOrden = ordenSalida.MontoOperacion;
					ord.MontoOrdenPlazo = ordenSalida.MontoOperacionPlazo;
					ord.MontoLiquidacion = ordenSalida.MontoOperacionLiquidacion;
					ord.MontoLiquidacionPlazo = ordenSalida.MontoOperacionLiquidacionPlazo;
					ord.MontoLocal = ordenSalida.MontoOperacionLocal;
					ord.MontoLocalPlazo = ordenSalida.MontoOperacionLocalPlazo;
					ord.MontoTotalLocal = ordenSalida.MontoTotalLocal;
					ord.MontoTotalLiquidacion = ordenSalida.MontoTotalLiquidacion;
					ord.MontoTotalLiquidacionPlazo = ordenSalida.MontoTotalLiquidacionPlazo;
					//ordenSalida.PorcentajeCobrosUltimaOrden; NO LO USAMOS
					ord.TotalCobrosLiquidacion = ordenSalida.TotalCobrosLiquidacion;
					ord.TotalCobrosLiquidacionPlazo = ordenSalida.TotalCobrosLiquidacionPlazo;
					ord.TotalCobrosLocal = ordenSalida.TotalCobrosLocal;
					ord.ValorRescate = ordenSalida.ValorRescateOrden;
					ord.PorcentajeAllIn = ordenSalida.PorcentajeAllIn;
					ord.MontoInteresContado = ordenSalida.MontoInteresContado;
					ord.MontoInteresPlazo = ordenSalida.MontoInteresPlazo;
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")){
						ord.MontoOrden = ordenSalida.MontoOperacionLocal;
						ord.MontoOrdenPlazo = ordenSalida.MontoOperacionLocalPlazo;
						ord.MontoTotalLiquidacion = ordenSalida.MontoOperacionLiquidacion;
						ord.MontoTotalLiquidacionPlazo = ordenSalida.MontoOperacionLiquidacionPlazo;
					}
					
					//Paso a ruteo manual  MONTO MAXIMO en RV o precio mercado
					if(parametroMontoMaximoRuteo != null) {
						BigDecimal montoMaximoRuteo = new BigDecimal(parametroMontoMaximoRuteo);
						if(ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_RV)&& ord.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR) && ((ord.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL) && montoMaximoRuteo.compareTo(ord.MontoOrden) < 0 ) || (ord.IdTipoPrecio.equals(_c.TIPO_PRECIO_MERCADO) && ParametroPrecioMercado.equals(_c.SI) &&(ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_CLI)||ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_AGENTE))))){
							ord.IdTipoCanalDistribucion = _c.TIPO_CANAL_DISTRIBUCION_MANUAL;
//							ord.IdTipoCanalDistribucionReal = _c.TIPO_CANAL_DISTRIBUCION_MANUAL;
						}
					}
					if (ord.IdTipoPrecio.equals("M")){
						ord.IdTipoCanalDistribucion = _c.TIPO_CANAL_DISTRIBUCION_MANUAL;
					};
					if((ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE) && ord.IdTipoPrecio.equals("L"))
							&& (indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteRutear.equals("N") || ord.PlantillaEmpleadoPermiteRuteo.equals("N") || ord.PortafolioProductoPermiteRuteo.equals("N"))) {
							ord.IdTipoCanalDistribucion = _c.TIPO_CANAL_DISTRIBUCION_MANUAL;
					};
					if((ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL) && ord.IdTipoPrecio.equals("L"))
							&& (indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteRutear.equals("N") || ord.PortafolioProductoPermiteRuteo.equals("N"))) {
							ord.IdTipoCanalDistribucion = _c.TIPO_CANAL_DISTRIBUCION_MANUAL;
					};
				}
				
				for (CalcularMontoYCobrosOrden.Cobro_Salida cobroSalida : salidaCalcularMontoYCobrosOrden.p_out_Cobros) {
					OrdenProceso orden = ordenesHash.get(cobroSalida.IdInterno);
					//Utils.copyPojo(cobroSalida, cob);
					CobroOrdenProceso cob = copyPojoCobroOrdenProceso(cobroSalida);
					
					cob.Orden = orden;
					cob.IdMoneda = cobroSalida.IdMonedaCobro;
					orden.cobros.add(cob);
					if (cob.IdOrigenCobro.equalsIgnoreCase("PORTAFOLIO"))
						orden.cantidadCobrosPortafolio++;
				}
			}
//			TODO RESCATAR ERRORES
			
			
			//validaciones
			{
				for (OrdenProceso ord: ordenes) {
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.cantidadCobrosPortafolio == 0),"PORTAFOLIO_SIN_COBROS_CONFIGURADOS",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	

					ProcesaRequisito(con,in.p_IdEmpresa,ord.PagoCobroCuentaInversion.equalsIgnoreCase(_c.SI) && !indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCuentaInversion.equalsIgnoreCase(_c.SI),"PORTAFOLIO_NO_OPERA_CON_CUENTA_INVERSION",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdEstadoInstrumento.equals("ACT")),"INSTRUMENTO_NO_VIGENTE",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdEstadoCliente.equals("ACT")),"CLIENTE_BLOQUEADO","CO_ClienteBloqueado",ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdEstadoPortafolio.equals("ACT")),"PORTAFOLIO_BLOQUEADO","CO_PortafolioBloqueado",ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdEstadoAutorizacionPortafolio.equals("PEN")),"PORTAFOLIO_PENDIENTE_AUTORIZACION",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.PortafolioProductoHabilitado.equals("N")),"PRODUCTO_NO_HABILITADO_PORTAFOLIO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteRutear.equals("N") && (ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL) || ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE))),"NO_RUTEA_PORTAFOLIO",MensajeSalida.MENSAJE_INFORMACION,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.PortafolioProductoPermiteRuteo.equals("N") && (ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL) || ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE))),"PRODUCTO_NO_RUTEA_PORTAFOLIO",MensajeSalida.MENSAJE_INFORMACION,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlantillaEmpleadoProductoHabilitado.equals("N"))&& !ord.IdTipoCanalIngreso.equals("CLI"),"PRODUCTO_NO_HABILITADO_EMPLEADO","CO_EjecutivoSinProductoPlantilla",ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlantillaEmpleadoPermiteRuteo.equals("N") && ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE)  && (ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_AGENTE)  )),"RUTEO_NO_HABILITADO_EMPLEADO","CO_EjecutivoSinRuteoPlantilla",ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdEstadoEmpleadoDigitador.equals("ACT")),"EMPLEADO_NO_HABILITADO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdEstadoPersona.equals("ACT")),"PERSONA_BLOQUEADA","CO_PersonaBloqueada",ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdEstadoEmpleadoEjecutivoPortafolio.equals("ACT")),"EJECUTIVO_PORTAFOLIO_BLOQUEADO","CO_EjecutivoPortafolioBloqueado",ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(true),"ORDEN_REQUIERE_AUTORIZACION","CO_OrdenRequiereAutorizacion",ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoPrecio.equalsIgnoreCase(_c.TIPO_PRECIO_MERCADO) && (ord.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL) || ord.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE))) ,"RUTEO_PRECIO_MERCADO","CO_RuteoPrecioMercado",ord,requisitos,out.P_OUT_MENSAJES); // ORDENES DE RUTEO
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.CumpleHorario.equals("N") && ord.AplazaOrdenPorHorarioNegociacion.equals("N")),"ORDEN_FUERA_HORARIO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.CumpleHorario.equals("N") && ord.AplazaOrdenPorHorarioNegociacion.equals("S") && ord.AplazaSoloHora.equals("N")),"ORDEN_APLAZADA_DIA_SIGUIENTE","ADVERTENCIA",ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoLibroOrden.equalsIgnoreCase("NORMAL") && ord.IdLibroOrdenes  == null),"CLIENTE_SIN_LIBRO_ORDENES_CONFIGURADO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdTipoLibroOrden.equalsIgnoreCase("NORMAL") && ord.IdLibroOrdenes  == null),"LIBRO_NO_ESPECIFICADO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoCanalDistribucion.equals(_c.TIPO_CANAL_DISTRIBUCION_MANUAL) && (ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL) || ord.IdTipoCanalDistribucionReal.equals(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE))),"PRECIO_MERCADO_CLIENTE",MensajeSalida.MENSAJE_INFORMACION,ord,requisitos,out.P_OUT_MENSAJES);	
					if (librosOrdenes.get(ord.IdLibroOrdenes) != null) {
						ProcesaRequisito(con,in.p_IdEmpresa,(!librosOrdenes.get(ord.IdLibroOrdenes).IdEstado.equals("ABT")),"LIBRO_CERRADO_PARA_INGRESO_ORDENES",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdTipoLibroOrden.equalsIgnoreCase("NORMAL") && !ord.IdInstrumento.equals(librosOrdenes.get(ord.IdLibroOrdenes).IdInstrumento)),"LIBRO_NO_CORRESPONDE_A_INSTRUMENTO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						
					}

//Precios RV Banco Estado https://jira.bcs.cl/browse/GCESTENROL-6 CGO					
					BigDecimal precioRef = ord.PrecioMercado;					
					if (calculoPrecioPunta.equalsIgnoreCase("S")) {
						precioRef = ord.PrecioOrden;										
					}					
					
					BigDecimal limiteDivergencia = porcentajeDivergenciaPrecio;
					String nemoCondicion = ord.IdCondicionDeLiquidacion;

					if (_c.CLASIFICACION_RV.equals(ord.IdClasificacionTipoInstrumento) && nemoCondicion != null
							&& java.util.Arrays.asList("T2", "T1", "T0").contains(nemoCondicion)) {

						if (divergenciaCondicionLiquidacion.isEmpty()) {
							try {
								divergenciaCondicionLiquidacion.put("T2",ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa,"ORDENES", "PorcentajeDivergenciaPrecioCondicionT2"));
							} catch (Exception e) {
								logger.debug("Parametro divergencia condicion T2 no encontrado");
							}
							try {
								divergenciaCondicionLiquidacion.put("T1",ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa,"ORDENES", "PorcentajeDivergenciaPrecioCondicionT1"));
							} catch (Exception e) {
								logger.debug("Parametro divergencia condicion T1 no encontrado");
							}
							try {
								divergenciaCondicionLiquidacion.put("T0",ConfiguracionSistema.getParametroConfiguracionDecimal(con, in.p_IdEmpresa,"ORDENES", "PorcentajeDivergenciaPrecioCondicionT0"));
							} catch (Exception e) {
								logger.debug("Parametro divergencia condicion T0 no encontrado");
							}
						}

						BigDecimal limiteCondicion = divergenciaCondicionLiquidacion.get(nemoCondicion);

						if (limiteCondicion != null) {
							limiteDivergencia = limiteCondicion;
						}
					}
				
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoPrecio.equals("L") && precioRef != null && precioRef.compareTo(BigDecimal.ZERO) == 1 &&  100.0 * Math.abs((precioRef.doubleValue()-ord.PrecioOrden.doubleValue())/precioRef.doubleValue()) > limiteDivergencia.doubleValue()) ,"ORDEN_PORCENTAJE_DIVERGENCIA","CO_DiferenciaPrecioOrden",ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdFormaCustodia.equals("CUS") && indicadoresCliente.get(ord.IdCliente).ContratoCustodia.equals("N")),"CLIENTE_SIN_CONTRATO_CUSTODIA","CO_ContratoCustodia",ord,requisitos,out.P_OUT_MENSAJES);	
					ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdCondicionDeLiquidacion.equals("T2") && ord.IdBolsa.equalsIgnoreCase(_c.ID_CUALQUIER_BOLSA) && (ord.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE) || ord.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL))),"RUTEO_REQUIERE_ESPECIFICAR_BOLSA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,!(ord.PagoCobroCuentaInversion.equalsIgnoreCase(indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCuentaInversion)),"NO_USA_PREFERENCIA_NEGOCIO_OPERA_CUENTA_INVERSION","CO_NoUsaPreferenciaNegocioOperaCuentaInversion",ord,requisitos,out.P_OUT_MENSAJES);
				
					
					if (localizacionSistema.equalsIgnoreCase("CL")) {
						//Validacion Portafolio tiene activado mecanismos captacion verbal o Escrita
						boolean lanzarAdvertenciaVerbalEscrita = false; 
						String macanismoCaptacionPortafolio = indicadoresPortafolio.get(ord.IdPortafolioCliente).MecanismoCaptacion;
						Type tipoToken = new TypeToken<Map<String, String>>(){}.getType();
						Map<String, String> mecanismoCaptacionMapa = Utils.getGsonBuilder().fromJson(macanismoCaptacionPortafolio, tipoToken);
						lanzarAdvertenciaVerbalEscrita = mecanismoCaptacionMapa==null || mecanismoCaptacionMapa.get(ord.CodigoClaseOrden) == null || !mecanismoCaptacionMapa.get(ord.CodigoClaseOrden).equalsIgnoreCase("true");
						ProcesaRequisito(con,in.p_IdEmpresa, lanzarAdvertenciaVerbalEscrita,"ORDEN_ES_VERBAL_O_ESCRITA","CO_ConfirmacionOrdenVerbalEscrita",ord,requisitos,out.P_OUT_MENSAJES);
						
						ProcesaRequisito(con,in.p_IdEmpresa,(!indicadoresCliente.get(ord.IdCliente).FotocopiaCarnet.equals("S")),"CLIENTE_SIN_FOTOCOPIA_DOCUMENTO_IDENTIDAD","CO_FotocopiaCarnet",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con,in.p_IdEmpresa,(!indicadoresCliente.get(ord.IdCliente).AntecedentesLegales.equals("S")),"CLIENTE_SIN_ANTECEDENTES_LEGALES","CO_AntecedentesLegales",ord,requisitos,out.P_OUT_MENSAJES);					
						ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoMercantil.equals("N") && indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCuentaInversion.equals("S")),"CLIENTE_SIN_CONTRATO_MERCANTIL_PARA_OPERAR_CUENTA_INVERSION","CO_ContratoMercantil",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoCondicionesGenerales.equals("N")),"CLIENTE_SIN_CONTRATO_CONDICIONES_GENERALES","CO_ContratoCondicionesGenerales",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoInternet.equals("N")),"CLIENTE_SIN_CONTRATO_INTERNET","CO_ContratoCanalInternet",ord,requisitos,out.P_OUT_MENSAJES);
						
					}
					
					if (localizacionSistema.equalsIgnoreCase("PE")) {
						ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdMonedaOrden !=null && !ord.IdMonedaOrden.equalsIgnoreCase(ord.IdMonedaLiquidacionOrden)),"MONEDA_INSTRUMENTO_DIFERENTE_MONEDA_LIQUIDACION","CO_MonedaInstrumentoDiferenteMonedaLiquidacion",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con,in.p_IdEmpresa,(ord.ExisteOrdenMismaOperacionInstrumento.equalsIgnoreCase(_c.SI)),"EXISTE_ORDEN_MISMA_OPERACION_MISMO_INSTRUMENTO","CO_OrdenMismaOperacionInstrumento",ord,requisitos,out.P_OUT_MENSAJES);
						
						ProcesaRequisito(con,in.p_IdEmpresa,(ord.InstrumentoAcreditado.equalsIgnoreCase(_c.SI)&&!ord.IndicadorInstrumentoAcreditado.equalsIgnoreCase(_c.SI)),"CLIENTE_NO_ACREDITADO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	
					}
					
					
					//Identificar mecanismo de captacion (CL Y PE) si no es postrera
					if(!ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_TRADER)){
						String _mec = indicadoresPortafolio.get(ord.IdPortafolioCliente).MecanismoCaptacion;
						Type type = new TypeToken<Map<String, String>>(){}.getType();
						Map<String, String> myMap = Utils.getGsonBuilder().fromJson(_mec, type);
						ProcesaRequisito(con,in.p_IdEmpresa, myMap==null || myMap.get(ord.CodigoClaseOrden) == null || !myMap.get(ord.CodigoClaseOrden).equalsIgnoreCase("true") ,"MECANISMO_CAPTACION_NO_PERMITIDO_PORTAFOLIO","CO_MecanismoCaptacion",ord,requisitos,out.P_OUT_MENSAJES);
					}
					//REPORTES
					if (ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_RE)) {
						if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF)) {
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.TasaReporte==null || ord.TasaReporte.doubleValue() < 0 ),"TASA_REPORTE_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoReporte==null || ord.PlazoReporte <= 0),"PLAZO_REPORTE_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,(!ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_TRADER) && (ord.MontoOrden==null || ord.MontoOrden.doubleValue() <= 0)),"MONTO_MENOR_IGUAL_CERO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	// --SOLO CUANDO NO ES POSTRERA VIENE POR MONTO										
						}
						if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV)) {
							ProcesaRequisito(con,in.p_IdEmpresa, ord.PrecioOrden==null || ord.PrecioOrden.doubleValue() <= 0,"PRECIO_MENOR_IGUAL_CERO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						}
						
						if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF)) {
							BigDecimal total = new BigDecimal(100);
							HashMap<String,Object> paramsPC = new HashMap<>();
							paramsPC.put("IdInstrumento", ord.IdInstrumentoCustodia);
							List<PorcentajeCastigo> porcentajeCastigo = _sql.query(con, SQL_SELECT_PORCENTAJE_CASTIGO_INSTRUMENTO, paramsPC, PorcentajeCastigo.class);
							BigDecimal PorcentajeTotal = ord.PrecioOrden.add(porcentajeCastigo.get(0).PorcentajeCastigo);
							
							ProcesaRequisito(con,in.p_IdEmpresa, PorcentajeTotal.compareTo(total) == 1 ,"PORCENTAJE_CASTIGO_MENOR_A_LO_DEFINIDO","CO_PorcentajeCastigo",ord,requisitos,out.P_OUT_MENSAJES);
						}	
					}
					
					//VENTA CORTA
					if (ord.EsVentaCorta.equals(_c.SI)) {
						/*** INFORMACION CORREDORA ***/
						ConsultaDataCorredoraBlockChain consultaDataCorredora = new ConsultaDataCorredoraBlockChain();
						ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora = new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
						entradaConsultaDataCorredora.p_IdEmpresa = in.p_IdEmpresa;
						ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(con, entradaConsultaDataCorredora);
						
						ContratoEsBlockChainFirmado ContratoEsBlockChain = new ContratoEsBlockChainFirmado();
						ContratoEsBlockChainFirmado_Entrada ContratoEsBlockChainEntrada = new  ContratoEsBlockChainFirmado_Entrada();
						ContratoEsBlockChainEntrada.p_IdEmpresa 			= in.p_IdEmpresa;
						ContratoEsBlockChainEntrada.p_IdUsuario 			= in.p_IdUsuario;
						ContratoEsBlockChainEntrada.p_ClaveUsuario 			= "";
						ConsultaEstadoCntBC ConsultaEstadoCntBC 			= new ConsultaEstadoCntBC();
						ConsultaEstadoCntBC.p_IdTipoDocumento 				= ConstantesBCK.IDCONTRATOPRESTAMOANEXO42;
						ConsultaEstadoCntBC.p_IdCliente 					= ord.IdCliente.toString();
						ConsultaEstadoCntBC.p_BrokerageRUT 					= salidaConsultaDataCorredora.p_Out_BrokerageRUT;
						ConsultaEstadoCntBC.p_IdContrato 					= ConstantesBCK.CONTRATOPRESTAMOANEXO42;
						ContratoEsBlockChainEntrada.p_ConsultaEstadoCntBC 	= ConsultaEstadoCntBC;
						ContratoEsBlockChainFirmado_Salida ContratoEsBlockChainFirmado_Salida = ContratoEsBlockChain.logica(con, ContratoEsBlockChainEntrada);
						String p_EsBlockChain = null;
						String p_ContratoFirmado = null;
						for(ContratoEsBlockChainFirmado.Resultado ResultadoBC: ContratoEsBlockChainFirmado_Salida.P_OUT_RESULTADOPROCESO){
							 p_EsBlockChain = ResultadoBC.EsBlockChain; 
						     p_ContratoFirmado = ResultadoBC.ContratoFirmado;
						     break;
						}
						ord.errores = ContratoEsBlockChainFirmado_Salida.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES.addAll(ContratoEsBlockChainFirmado_Salida.P_OUT_MENSAJES);
						if (p_EsBlockChain.equals(_c.NO)){
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoVentaCorta==null || ord.PlazoVentaCorta < 0 ),"PLAZO_VENTA_CORTA_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,!ord.InstrumentoPermiteVentaCorta.equals("S"),"INSTRUMENTO_NO_PERMITE_VENTA_CORTA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.TasaVentaCorta==null || ord.TasaVentaCorta.doubleValue() < 0 ),"TASA_VENTA_CORTA_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoVentaCorta > parametrosVentaCorta.get(0).VentaCortaPlazoMaximo ),"PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoVentaCorta < parametrosVentaCorta.get(0).VentaCortaPlazoMinimo ),"PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							//REQUISITO VENTA CORTA
							if (localizacionSistema.equalsIgnoreCase("CL")) {
								ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoEmprestitos.equals("N")),"CLIENTE_SIN_CONTRATO_EMPRESTITOS","CO_ContratoEmprestitos",ord,requisitos,out.P_OUT_MENSAJES);
							}
							ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoVentaCortaRV.equals("N")),"CLIENTE_SIN_CONTRATO_VENTA_CORTA","CO_ContratoVentaCortaRV",ord,requisitos,out.P_OUT_MENSAJES);	
						}
						if (p_EsBlockChain.equals(_c.SI) && p_ContratoFirmado.equals(_c.NO)){ 
								ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoVentaCorta==null || ord.PlazoVentaCorta < 0 ),"PLAZO_VENTA_CORTA_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,!ord.InstrumentoPermiteVentaCorta.equals("S"),"INSTRUMENTO_NO_PERMITE_VENTA_CORTA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,(ord.TasaVentaCorta==null || ord.TasaVentaCorta.doubleValue() < 0 ),"TASA_VENTA_CORTA_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoVentaCorta > parametrosVentaCorta.get(0).VentaCortaPlazoMaximo ),"PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoVentaCorta < parametrosVentaCorta.get(0).VentaCortaPlazoMinimo ),"PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoVentaCortaRV.equals("N")),"CLIENTE_SIN_CONTRATO_VENTA_CORTA","CO_ContratoVentaCortaRV",ord,requisitos,out.P_OUT_MENSAJES);	
						}
					}
					//DERIVADOS
					if (ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_DV)) {
						//valida que el portafolio corresponda a DERIVADOS (Indicador a nivel depreferencia de negocio del CLiente)
						ProcesaRequisito(con,in.p_IdEmpresa,(!indicadoresCliente.get(ord.IdCliente).PortafolioDerivados.equals(ord.IdPortafolioCliente.toString())),"CLIENTE_SIN_PORTAFOLIO_PARA_DERIVADOS",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						
						if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMFUT) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENFUT)) {
							ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoFuturos.equals("N")),"CLIENTE_SIN_CONTRATO_FUTUROS","CO_ContratoFuturos",ord,requisitos,out.P_OUT_MENSAJES);	
						}
						else if(_c.tipoInstrumentoOpciones.contains(ord.IdTipoInstrumento)){
				              ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoOpciones.equals("N")),"CLIENTE_SIN_CONTRATO_OPCIONES","CO_ContratoOpciones",ord,requisitos,out.P_OUT_MENSAJES);
				        }						
					}
					
					// RENTA VARIABLE
					if (ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_RV)) {
						BigDecimal montoMinimo = new BigDecimal(parametroMontoMinimo);
						// Inicio Valicion WS para Vigencia de Ordenes OP, SE, FF
						boolean esPermanente = _c.TIPO_VIGENCIA_PERMANENTE.equalsIgnoreCase(ord.IdTipoVigenciaOrden);
						boolean esSemanal = _c.TIPO_VIGENCIA_SEMANAL.equalsIgnoreCase(ord.IdTipoVigenciaOrden);
						boolean esFechaFija = _c.TIPO_VIGENCIA_FECHA_FIJA.equalsIgnoreCase(ord.IdTipoVigenciaOrden);

						// Variables de fechas base
						java.time.ZoneId zonaLocal = null;
						java.time.LocalDate fechaCreacion = null;
						FechasIn f = null;

						// Variables para el servicio de dia habiles
						ObtenerDiaSiguiente obtenerDiaSgteService = null;
						InObtenerDiaSiguiente inObtenerDiaSgte = null;
						OutObtenerDiaSiguiente outObtenerDiaSgte = null;
						String fechaCalculadaStr = null;
						java.util.Date fechaUtil = null;

						// Variables para validacion fecha fija
						boolean superaFechaMaxima = false;
						boolean esInhabil = false;
						String fechaOriginalJSON = null;
						boolean tieneFechaValida = false;
						java.util.Date fechaDigitada = null;
						java.time.LocalDate fechaVigencia = null;
						java.time.LocalDate fechaMaxima = null;
						List<DiasAConsultar> fechasConsultaFF = null;
						DiasAConsultar fechaConsultaFF = null;
						List<DiasAConsultarSalida> fechasProcesadasFF = null;

						if (ord.FechaSistemaCreacion != null) {
							zonaLocal = java.time.ZoneId.systemDefault();
							fechaCreacion = java.time.Instant.ofEpochMilli(ord.FechaSistemaCreacion.getTime())
									.atZone(zonaLocal).toLocalDate();

							if (esPermanente || esSemanal) {
								try {
									obtenerDiaSgteService = new ObtenerDiaSiguiente();
									inObtenerDiaSgte = new InObtenerDiaSiguiente();
									f = new FechasIn();

									inObtenerDiaSgte.p_idPais = 44L;
									inObtenerDiaSgte.p_diaHabil = "N";
									inObtenerDiaSgte.p_soloCaeDiaHabil = "S";
									inObtenerDiaSgte.p_consideraHabilAnterior = "S";
									inObtenerDiaSgte.p_fechas = new ArrayList<FechasIn>();

									f.fechaInicial = Fecha.toStringDate(ord.FechaSistemaCreacion);
									f.numeroDias = esPermanente ? 29L : 6L;
									inObtenerDiaSgte.p_fechas.add(f);
									outObtenerDiaSgte = obtenerDiaSgteService.logica(con, inObtenerDiaSgte);

									if (outObtenerDiaSgte.p_out_fechas != null
											&& !outObtenerDiaSgte.p_out_fechas.isEmpty()) {
										fechaCalculadaStr = outObtenerDiaSgte.p_out_fechas.get(0).fechaCalculada;
										fechaUtil = Fecha.toDate(fechaCalculadaStr);

										if (fechaUtil != null) {
											ord.FechaVigencia = new java.sql.Date(fechaUtil.getTime());
										}
									}
								} catch (Exception e) {
									logger.debug(
											"Error al calcular vigencia Permanente/Semanal para WS: " + e.getMessage());
								}
							}

							else if (esFechaFija) {
								if (in.p_Ordenes != null) {
									for (ObjOrdenesEntrada entrada : in.p_Ordenes) {
										if (entrada.IdInterno != null && entrada.IdInterno.equals(ord.IdInterno)) {
											fechaOriginalJSON = entrada.FechaVigencia;
											break;
										}
									}
								}

								tieneFechaValida = fechaOriginalJSON != null && !fechaOriginalJSON.trim().isEmpty()
										&& !fechaOriginalJSON.contains("0001-01-01");

								if (tieneFechaValida) {
									fechaDigitada = Fecha.toDate(fechaOriginalJSON);

									if (fechaDigitada != null) {
										ord.FechaVigencia = new java.sql.Date(fechaDigitada.getTime());
										fechaVigencia = java.time.Instant.ofEpochMilli(fechaDigitada.getTime())
												.atZone(zonaLocal).toLocalDate();
										fechaMaxima = fechaCreacion.plusDays(29);
										superaFechaMaxima = fechaVigencia.isAfter(fechaMaxima);

										try {
											fechasConsultaFF = new ArrayList<DiasAConsultar>();
											fechaConsultaFF = new DiasAConsultar();
											fechaConsultaFF.IdInterno = ord.IdInterno;
											fechaConsultaFF.fechaConsulta = fechaVigencia.toString();
											fechasConsultaFF.add(fechaConsultaFF);

											fechasProcesadasFF = esDiaHabilMasivo(fechasConsultaFF, in.p_IdEmpresa,
													con);

											if (fechasProcesadasFF != null && !fechasProcesadasFF.isEmpty()) {
												esInhabil = !fechasProcesadasFF.get(0).habil;
											}
										} catch (Exception e) {
											logger.debug("Error al calcular vigencia Permanente/Semanal para WS: "
													+ e.getMessage());
											esInhabil = (fechaVigencia.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
													|| fechaVigencia.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
										}

										ProcesaRequisito(con, in.p_IdEmpresa, esInhabil, "FECHA_VIGENCIA_INHABIL",
												MensajeSalida.MENSAJE_ERROR, ord, requisitos, out.P_OUT_MENSAJES);
									}
								}
								
								ProcesaRequisito(con, in.p_IdEmpresa, superaFechaMaxima, "FECHA_VIGENCIA_ORDENES",
										MensajeSalida.MENSAJE_ERROR, ord, requisitos, out.P_OUT_MENSAJES);
							}
						}
						// Fin de la validacion de WS
						
						ProcesaRequisito(con, in.p_IdEmpresa,!ord.IdTipoOperacion.equals(_c.TIPO_OPERACION_COMSIM)  && (ord.Cantidad==null || ord.Cantidad.doubleValue() <= 0)  && ord.EsPorMonto.equals('N') ,"CANTIDAD_MENOR_IGUAL_CERO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,!ord.IdTipoInstrumento.equals(_c.TIPO_INSTRUMENTO_SIM) && !ord.IdTipoPrecio.equals("M") && (ord.PrecioOrden==null || ord.PrecioOrden.doubleValue() <= 0),"PRECIO_MENOR_IGUAL_CERO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRV.equals("S")  &&  ord.AccionOrden.equals("COMPRA") &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_NOMINAL","CO_CompraNominalRV",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,ord.EsVentaCorta.equals("N") && indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRV.equals(_c.SI) && indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteVenderNominalRV.equals("N")  &&  ord.AccionOrden.equals("VENTA") &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,ord.EsVentaCorta.equals("N") && indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRV.equals(_c.SI) && indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteVenderNominalRV.equals("S")  &&  ord.AccionOrden.equals("VENTA") &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL","CO_VentaNominalRV",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,ord.IdTipoVigenciaOrden.equalsIgnoreCase(_c.TIPO_VIGENCIA_PERMANENTE) && ord.IdTipoPrecio.equalsIgnoreCase(_c.TIPO_PRECIO_MERCADO) ,"ORDEN_PERMANENTE_A_PRECIO_MERCADO","CO_OrdenPermanentePrecioMercado",ord,requisitos,out.P_OUT_MENSAJES);
					

						// Valida monto minimo de la orden ingresada 
						ProcesaRequisito(con,in.p_IdEmpresa,(montoMinimo.compareTo(ord.MontoOrden) > 0 && (ord.IdTipoCanalIngreso.equals("CLI") || ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_WS))),"MENOR_MONTO_MIN_ORDEN","CO_MontoMinimoCrearOrden", ord,requisitos,out.P_OUT_MENSAJES);	//
						ProcesaRequisito(con,in.p_IdEmpresa,(montoMinimo.compareTo(ord.MontoOrden) > 0 && ord.IdTipoCanalIngreso.equals("AGE")),"MENOR_MONTO_MIN_ORDEN_REQUIERE_AUTORIZACION","CO_MontoMinimoCrearOrdenAutorizar", ord,requisitos,out.P_OUT_MENSAJES);	//						
						
						// Valida Monto maximo permitido por ruteo
						if(parametroMontoMaximoRuteo != null) {
							BigDecimal montoMaximoRuteo = new BigDecimal(parametroMontoMaximoRuteo);
							ProcesaRequisito(con,in.p_IdEmpresa,(montoMaximoRuteo.compareTo(ord.MontoOrden) < 0 &&  (ord.IdTipoCanalIngreso.equals("CLI") || ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_WS)) && ord.IdTipoPrecio.contentEquals("L") ),"SUPERA_MONTO_MAX_RUTEO",MensajeSalida.MENSAJE_INFORMACION,ord,requisitos,out.P_OUT_MENSAJES);	//
						}
						
						
						// Valida parametro precio Mercado cliente
						// ProcesaRequisito(con,in.p_IdEmpresa,(ParametroPrecioMercado.equals(_c.SI) && ord.IdTipoPrecio.equals(_c.TIPO_PRECIO_MERCADO)),"PRECIO_MERCADO_CLIENTE",MensajeSalida.MENSAJE_INFORMACION,ord,requisitos,out.P_OUT_MENSAJES);	
						
						// Valida que en operaciones de venta el saldo líquido no sea negativo
						if(ord.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA)) {
						    boolean saldoVentaNegativo = ord.TotalCobrosLiquidacion.compareTo(ord.MontoLiquidacion) > 0;
						    ProcesaRequisito(con, in.p_IdEmpresa, saldoVentaNegativo,"MONTO_ORDEN_VENTA_MENOR_COSTOS","CO_MontoOrdenVentaMenorCostos", ord, requisitos, out.P_OUT_MENSAJES);
						}
						
						// Valida porcentaje de cobros permitido por parametro
						if(parametroPorcentajeCobro != null && ord.MontoLiquidacion.compareTo(BigDecimal.ZERO)!= 0) {
							BigDecimal porcentajeCobros = (new BigDecimal(100)).multiply(ord.TotalCobrosLiquidacion).divide(ord.MontoLiquidacion,20,RoundingMode.HALF_UP);
							ProcesaRequisito(con,in.p_IdEmpresa, parametroPorcentajeCobro.compareTo(porcentajeCobros) < 0 && (ord.IdTipoCanalIngreso.equals("CLI") || ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_WS) || ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_OWP)),"COBRO_MAX_PERMITIDO","CO_CobroMaxPermitido",ord,requisitos,out.P_OUT_MENSAJES);
						}
						
						// Valida declaracion CRS FATCA segun residencia
						PaisResidenciaPrincipalCliente paisResidenciaPrincipalCliente = paisesResidenciaPrincipalCliente.get(ord.IdPersonaCliente);
						if(validaEsPaisFatcaCrs(paisResidenciaPrincipalCliente)) {
							ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoDeclaracionCRSFatca.equals("N")),"CLIENTE_SIN_DECLARACION_CRS_FATCA","CO_ContratoDeclaracionCRSFatca",ord,requisitos,out.P_OUT_MENSAJES);
						}
						
						// Valida porcentaje de visibilidad para cliente Web services
						if(ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_WS)) {
							CalcularCantidadVisible demo =new CalcularCantidadVisible();
							EntradaCalcularCantidadVisible calculaCantidadVisible = new EntradaCalcularCantidadVisible();
							calculaCantidadVisible.p_IdEmpresa = in.p_IdEmpresa;
							calculaCantidadVisible.p_IdUsuario = in.p_IdUsuario;
							calculaCantidadVisible.p_IdModulo = in.p_IdModulo;
							calculaCantidadVisible.p_MontoBruto = ord.MontoOrden.toString(); // consultar que monto representa el bruto
							calculaCantidadVisible.p_MonedaInstrumento = ord.IdMonedaInstrumento;
							calculaCantidadVisible.p_Fecha = Fecha.toStringDate(ord.FechaSistemaCreacion);
							calculaCantidadVisible.p_VisibilidadParam = ord.IdPorcentajeVisibilidadReal;
			 				SalidaCalcularCantidadVisible outCalcularCantidadVisible= demo.logica(con, calculaCantidadVisible);
							ProcesaRequisito(con,in.p_IdEmpresa,  Long.parseLong(outCalcularCantidadVisible.p_out_Porcentaje) != ord.IdPorcentajeVisibilidad &&  ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_WS),"PORCENTAJE_VISIBILIDAD","CO_PorcentajeVisibilidad",ord,requisitos,out.P_OUT_MENSAJES);

						}
						
		 				
						//SIMULTANEAS
						if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)) {
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.TasaSimultanea==null || ord.TasaSimultanea.doubleValue() < 0 ),"TASA_SIMULTANEA_NO_VALIDA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlazoSimultanea==null || ord.PlazoSimultanea <= 0),"PLAZO_SIMULTANEA_NO_VALIDO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.IdTipoOperacion.equals(_c.TIPO_OPERACION_COMSIM) && !ord.IdTipoCanalIngreso.equals(_c.TIPO_CANAL_INGRESO_TRADER) && (ord.MontoOrden==null || ord.MontoOrden.doubleValue() <= 0)),"MONTO_MENOR_IGUAL_CERO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);	// --SOLO CUANDO NO ES POSTRERA VIENE POR MONTO					
							ProcesaRequisito(con,in.p_IdEmpresa,(indicadoresCliente.get(ord.IdCliente).ContratoSimultanea.equals("N")),"CLIENTE_SIN_CONTRATO_SIMULTANEA","CO_ContratoSimultanea",ord,requisitos,out.P_OUT_MENSAJES);						
						}
					}
					
					//INTERMEDIACION FINANCIERA
					if (ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_IF)) {
						ProcesaRequisito(con, in.p_IdEmpresa,ord.ValorRescate.doubleValue() <= 0,"VALOR_RESCATE_MENOR_IGUAL_CERO",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRFIF.equals(_c.SI)  &&  ord.AccionOrden.equals(_c.ACCION_COMPRA) &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_NOMINAL","CO_CompraNominalRFIF",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRFIF.equals(_c.SI) && indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteVenderNominalRFIF.equals("N")  &&  ord.AccionOrden.equals("VENTA") &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRFIF.equals(_c.SI) && indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteVenderNominalRFIF.equals("S")  &&  ord.AccionOrden.equals("VENTA") &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL","CO_VentaNominalRFIF",ord,requisitos,out.P_OUT_MENSAJES);
					}
					
					//RENTA FIJA
					if (ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_RF)) {
						ProcesaRequisito(con, in.p_IdEmpresa,indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRFIF.equals(_c.SI)  &&  ord.AccionOrden.equals(_c.ACCION_COMPRA) &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_NOMINAL","CO_CompraNominalRFIF",ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,ord.EsVentaCorta.equals("N") && indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRFIF.equals(_c.SI) && indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteVenderNominalRFIF.equals("N") && ord.AccionOrden.equals("VENTA") &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
						ProcesaRequisito(con, in.p_IdEmpresa,ord.EsVentaCorta.equals("N") && indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaCustodiaRFIF.equals(_c.SI) && indicadoresPortafolio.get(ord.IdPortafolioCliente).PermiteVenderNominalRFIF.equals("S") && ord.AccionOrden.equals("VENTA") &&  ord.IdFormaCustodia.equals("NOM") ,"CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL","CO_VentaNominalRFIF",ord,requisitos,out.P_OUT_MENSAJES);		
					}
				}
				
				limpiarOrdenes(ordenes, ordenesConErrores);
				if (ordenes.size() == 0) {
					for(MensajeSalida mje : out.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
							out.P_OUT_ERRORESCONTADOR++;
						}
					}
					if(out.P_OUT_ERRORESCONTADOR.compareTo(0L)>=1L) {
						{
							if(in.p_BarraProgreso != null) {
								JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
								in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
							}
						}
					}
					return out;
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(65L,"PROCESANDO","Validando flujo de caja");
				}
			}
			
			
			//FLUJO CAJA
			{
				class FlujoCajaElem {
					String IdMoneda;
					String Fecha;
					Long IdPortafolio;
					
					public FlujoCajaElem(String moneda,String fecha,Long idPortafolio) {
						this.IdMoneda = moneda;
						this.Fecha = fecha;
						this.IdPortafolio = idPortafolio;
					}
					
					@Override
					public boolean equals(Object o) {
						FlujoCajaElem e = (FlujoCajaElem)o;
						return this.IdMoneda.equalsIgnoreCase(e.IdMoneda) && this.Fecha.equalsIgnoreCase(e.Fecha) && this.IdPortafolio.equals(e.IdPortafolio);
					}
					
					@Override
					public int hashCode() {
						return (this.IdMoneda + this.Fecha + this.IdPortafolio).hashCode();
					}
				}
				
				SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
				java.sql.Date fechaLiquidacionMinima = Fecha.toDate(fechaSistema); //ordenes.get(0).FechaLiquidacion;
				java.sql.Date fechaLiquidacionMaxima = Fecha.toDate(fechaSistema); //ordenes.get(0).FechaLiquidacion;
				HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
				HashMap<FlujoCajaElem,BigDecimal> totalOrdenesPortafolio = new HashMap<>();
				List<String> keysPortafolioMoneda = new ArrayList<String>();
				for (OrdenProceso ord: ordenes) {
					if (ord.ValidaSaldoCaja.equalsIgnoreCase(_c.NO)) {
						continue;
					}
					
					//AHORA VALIDA LA LINEA
					/*if (ord.ValidaSaldoCaja.equalsIgnoreCase(_c.SI) && indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaLineaSobregiro.equalsIgnoreCase(_c.SI)) {
						continue;
					}*/

					ord.FechaSaldoCaja = ord.FechaLiquidacion;
					if (ord.FechaLiquidacion.before(Fecha.toDate(fechaSistema))) {
						ord.FechaSaldoCaja = Fecha.toDate(fechaSistema);
					}
					
					if (ord.FechaSaldoCaja.before(fechaLiquidacionMinima)) {
						fechaLiquidacionMinima = ord.FechaSaldoCaja;
					}
					
					if (ord.FechaSaldoCaja.after(fechaLiquidacionMaxima)) {
						fechaLiquidacionMaxima = ord.FechaSaldoCaja;
					}
					FlujoCajaElem llave  = new FlujoCajaElem(ord.IdMonedaLiquidacionOrden, Fecha.toStringDate(ord.FechaLiquidacion),ord.IdPortafolioCliente);
					if("S".equalsIgnoreCase(ord.GeneraSpot)) {
						 llave = new FlujoCajaElem("CLP", Fecha.toStringDate(ord.FechaLiquidacion),ord.IdPortafolioCliente);
					}
					if (!totalOrdenesPortafolio.containsKey(llave)) {
						totalOrdenesPortafolio.put(llave, BigDecimal.ZERO);
					}
					BigDecimal totalOrdenes = totalOrdenesPortafolio.get(llave);
					totalOrdenes = totalOrdenes.add(ord.MontoTotalLiquidacion);
					if("S".equalsIgnoreCase(ord.GeneraSpot)) {
						totalOrdenes = totalOrdenes.add(ord.MontoTotalLocal);
					}
					totalOrdenesPortafolio.put(llave, totalOrdenes);
					
					SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
					port.IdMoneda = ord.IdMonedaLiquidacionOrden;
					if("S".equalsIgnoreCase(ord.GeneraSpot)) {
						port.IdMoneda = "CLP";
					}
					port.IdPortafolio = ord.IdPortafolioCliente;
					portafoliosFlujoCaja.add(port);
					
					//portafolio Moneda
					if (indicadoresPortafolio.get(ord.IdPortafolioCliente).OperaLineaSobregiro.equalsIgnoreCase(_c.SI)) {
						keysPortafolioMoneda.add(ord.IdPortafolioCliente+ord.IdMonedaLiquidacionOrden);
					}
				}
				
				//actualizamos ordenes con total por grupo (portafolio,moneda de liquidacion,fecha liquidacion)
				Iterator<Entry<FlujoCajaElem, BigDecimal>> it = totalOrdenesPortafolio.entrySet().iterator();
			    while (it.hasNext()) {
			        Entry<FlujoCajaElem, BigDecimal> pair = it.next();
			        for (OrdenProceso orden : ordenes) {
			        	if("S".equalsIgnoreCase(orden.GeneraSpot) &&
			        			"CLP".equalsIgnoreCase(pair.getKey().IdMoneda) && 
			        			Fecha.toStringDate(orden.FechaLiquidacion).equalsIgnoreCase(pair.getKey().Fecha) && 
			        			orden.IdPortafolioCliente.equals(pair.getKey().IdPortafolio)) {
			        		orden.TotalMontoPortafolio = pair.getValue();
						
							
						}else if (orden.IdMonedaLiquidacionOrden.equalsIgnoreCase(pair.getKey().IdMoneda) && 
			        			Fecha.toStringDate(orden.FechaLiquidacion).equalsIgnoreCase(pair.getKey().Fecha) && 
			        			orden.IdPortafolioCliente.equals(pair.getKey().IdPortafolio)) {
			        		orden.TotalMontoPortafolio = pair.getValue();
			        	}
			        }
			    }
				
			    if (portafoliosFlujoCaja.size() > 0) {
			    	entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
			    	entradaFlujoCaja.P_FECHADESDE = Fecha.toStringDate(fechaLiquidacionMinima);
					entradaFlujoCaja.P_FECHAHASTA = Fecha.toStringDate(fechaLiquidacionMaxima);
					entradaFlujoCaja.P_USO = "ORDENES_DE_COMPRA";
					entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
					{
						if(in.p_BarraProgreso != null) {
							BarraProgreso subBarraSaldoFlujoCaja = in.p_BarraProgreso.subrango(65, 80);
							entradaFlujoCaja.p_BarraProgreso = subBarraSaldoFlujoCaja;
						}
					}
					SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
					SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(con,entradaFlujoCaja);
					
					//TODO MANEJO DE ERRORES
					List<ObjFLUJOCAJA> asdf = salidaFlujoCaja.P_OUT_FLUJOCAJA;
					for (ObjFLUJOCAJA flj : asdf) {
						for (OrdenProceso orden : ordenes) {
							if("S".equalsIgnoreCase(orden.GeneraSpot) &&
									"USD".equalsIgnoreCase(orden.IdMonedaLiquidacionOrden) && 
									"CLP".equalsIgnoreCase(flj.IdMoneda) && 
				        			Fecha.toStringDate(orden.FechaSaldoCaja).equalsIgnoreCase(flj.FechaSaldo) && 
				        			orden.IdPortafolioCliente.equals(flj.IdPortafolio)) {
								orden.SaldoCajaPortafolio = flj.Saldo;
								
							}else if (orden.IdMonedaLiquidacionOrden.equalsIgnoreCase(flj.IdMoneda) && 
				        			Fecha.toStringDate(orden.FechaSaldoCaja).equalsIgnoreCase(flj.FechaSaldo) && 
				        			orden.IdPortafolioCliente.equals(flj.IdPortafolio)) {
				        		orden.SaldoCajaPortafolio = flj.Saldo;
				        	}
				        }
					}
			    	
			    }
			    
				if (keysPortafolioMoneda.size() > 0) {
			    	HashMap<String,Object> params = new HashMap<>();
			    	params.put("keysPortafolioMoneda",keysPortafolioMoneda);
			    	List<LineaSobregiro> lineas = _sql.query(con, SQL_SELECT_LINEA_SOBREGIRO, params, LineaSobregiro.class);
			    	
			    	for (LineaSobregiro linea : lineas) {
						for (OrdenProceso orden : ordenes) {
				        	if (orden.IdMonedaLiquidacionOrden.equalsIgnoreCase(linea.IdMoneda) && 
				        			orden.IdPortafolioCliente.equals(linea.IdPortafolio)) {
				        		orden.LineaSobregiroPortafolio = linea.MontoTotal;
				        	}
				        }
					}
			    }
			}
			
			//FLUJO EVALUACION LIMITE
			{
				EvaluacionLimite.ServiceEntrada entradaEvaluacionLimite = new EvaluacionLimite.ServiceEntrada();
				HashSet<EvaluacionLimite.ObjTABLAOPERACION> tablaOperacion = new HashSet<>();
				for (OrdenProceso ord: ordenes) {
					
					//CONDICIONES PARA QUE NO EVALUE LIMITE, ORDEN QUEDA AUTORIZADA
					if (!ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_AGENTE) || !ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_OWP) ||!ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_WS) )  {
						ord.IdEstadoLimite = _c.ESTADO_ORDEN_AUTORIZADA;
					}
					EvaluacionLimite.ObjTABLAOPERACION ope = new EvaluacionLimite.ObjTABLAOPERACION();
					ope.IdPortafolio = ord.IdPortafolioCliente;
					ope.IdCliente = ord.IdCliente;
					ope.MontoUtilizado = ord.MontoOrden;
					ope.IdMoneda = ord.IdMonedaOrden;
					ope.TipoCambio = ord.TipoCambio;
					ope.IdOperacion = ord.IdOrden;
					ope.IdTipoOperacion = ord.IdTipoOperacion;
					ope.IdTipoInstrumento = ord.IdTipoInstrumento;
					ope.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
					ope.IdTipoReajuste = ord.IdTipoReajuste;
					ope.IdInstrumento = ord.IdInstrumento;
					ope.IdEmisor = ord.IdEmisor;
					if(ord.EsVentaCorta.equals(_c.SI)){
						ope.Tasa = ord.TasaVentaCorta;
						ope.DiasPlazo = ord.PlazoVentaCorta.intValue();
					}
					else if(ord.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)){
						ope.Tasa = ord.TasaSimultanea;
						ope.DiasPlazo = ord.PlazoSimultanea.intValue();
					}
					else if(ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) || ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)){
						ope.Tasa = ord.TasaRFIF;
						ope.DiasPlazo = ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) ? ord.PlazoIF.intValue() : 0;
					}
					else if(ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						ope.Tasa = ord.TasaReporte;
						ope.DiasPlazo = ord.PlazoReporte.intValue();
					}
					else{
						ope.Tasa = BigDecimal.ZERO;
					}
					ope.FechaOperacion = Fecha.toStringDate(ord.FechaSistemaCreacion);
					ope.CodigoOperador = "";
					ope.IdAgente = 0L;
					ope.IdCanalIngreso = ord.IdTipoCanalIngreso;
					tablaOperacion.add(ope);
				}
				
			    if (tablaOperacion.size() > 0) {
			    	entradaEvaluacionLimite.P_IDEMPRESA = in.p_IdEmpresa;
			    	entradaEvaluacionLimite.P_IDUSUARIO = in.p_IdUsuario;
			    	entradaEvaluacionLimite.P_IDMODULO = in.p_IdModulo;
			    	entradaEvaluacionLimite.P_USAREGLASNEGOCIO = "N";
			    	entradaEvaluacionLimite.P_SIMULAROPERACION = "N";
			    	if(in.p_simularOperacion != null) {
			    		entradaEvaluacionLimite.P_SIMULAROPERACION = in.p_simularOperacion;
					}
			    	entradaEvaluacionLimite.P_TABLAOPERACION = new ArrayList<ObjTABLAOPERACION>(tablaOperacion);
			    	entradaEvaluacionLimite.P_IDTIPOENTIDAD = "ORDEN";
			    	entradaEvaluacionLimite.P_GENERAAUTORIZACION = "S";//default N
			    	
			    	/*EvaluacionLimite evaluacionLimiteService = new EvaluacionLimite();
			    	EvaluacionLimite.ServiceSalida salidaEvaluacionLimite = evaluacionLimiteService.logica(con,entradaEvaluacionLimite);
					
					List<ObjESTADOOPERACION> estadoOperacion = salidaEvaluacionLimite.P_OUT_ESTADOOPERACION;
					for (ObjESTADOOPERACION estOpe : estadoOperacion) {
						for (OrdenProceso orden : ordenes) {
				        	if (orden.IdOrden.equals(estOpe.IdOperacion)){
				        		if(estOpe.IdEstado.equalsIgnoreCase("PEN")){
				        			if(orden.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) {
				        				//ERROR, no puede ser postrera y solicitar autorizacion.
					        			orden.errores++;
					        			out.P_OUT_MENSAJES.add(new MensajeSalida(orden.IdInterno,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_AUTORIZACION_ORDEN_POSTRERA_LIMITE", "UI_ERROR_AUTORIZACION_ORDEN_POSTRERA_LIMITE"));
				        			}else {
				        				orden.IdEstadoLimite = _c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR;
				        				out.P_OUT_MENSAJES.add(new MensajeSalida((long)0,MensajeSalida.MENSAJE_AUTORIZACION, "UI_LIMITE_REQUIERE_AUTORIZACION", "UI_LIMITE_REQUIERE_AUTORIZACION"));
				        			}
				        			
				        		}
				        		else {
				        			orden.IdEstadoLimite = _c.ESTADO_ORDEN_AUTORIZADA;
				        		}
				        	}
				        }
					}*/
			    }
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(80L,"PROCESANDO","Validando flujo de custodia");
				}
			}
			
			//FLUJO CUSTODIA 
			{
				class FlujoCustodiaElem {
					public String IdBoveda;
					public String IdTipoOperacion;
					public String Fecha;
					public Long IdInstrumento;
					public Long IdPortafolio;
					
					public FlujoCustodiaElem(String idBoveda,String idTipoOperacion,Long idPortafolio,String fecha,Long idInstrumento) {
						this.IdTipoOperacion = idTipoOperacion;
						this.IdBoveda = idBoveda;
						this.IdInstrumento = idInstrumento;
						this.Fecha = fecha;
						this.IdPortafolio = idPortafolio;
					}
					
					@Override
					public boolean equals(Object o) {
						FlujoCustodiaElem e = (FlujoCustodiaElem)o;
						boolean b = this.IdTipoOperacion.equalsIgnoreCase(e.IdTipoOperacion) && this.Fecha.equalsIgnoreCase(e.Fecha) 
								&& this.IdPortafolio.equals(e.IdPortafolio) && (this.IdBoveda == e.IdBoveda || this.IdBoveda.equalsIgnoreCase(e.IdBoveda) ) && this.IdInstrumento.equals(e.IdInstrumento);
						return b;
					}

					@Override
					public int hashCode() {
					    return (this.IdBoveda + this.IdTipoOperacion + this.Fecha + this.IdInstrumento + this.IdPortafolio).hashCode();  
					}
					
				}
				
				class FlujoCustodiaIFElem {
					String IdBoveda;
					String IdTipoOperacion;
					String Fecha;
					Long IdInstrumento;
					Long IdPortafolio;
					
					public String IdEmisor;
					public String IdTipoReajuste;
					public String EsFungible;
					public String FechaVencimiento;
					
					public FlujoCustodiaIFElem(String idBoveda,String idTipoOperacion,Long idPortafolio,String fecha,Long idInstrumento, String idEmisor, String idTipoReajuste, String esFungible, String fechaVencimiento) {
						this.IdTipoOperacion = idTipoOperacion;
						this.IdBoveda = idBoveda;
						this.IdInstrumento = idInstrumento;
						this.Fecha = fecha;
						this.IdPortafolio = idPortafolio;
						this.IdEmisor = idEmisor;
						this.IdTipoReajuste = idTipoReajuste;
						this.EsFungible = esFungible;
						this.FechaVencimiento = fechaVencimiento;
					}
					
					@Override
					public int hashCode() {
					    return (this.IdTipoOperacion + this.IdBoveda + this.IdInstrumento + this.Fecha + this.IdPortafolio + this.IdEmisor + this.IdTipoReajuste + this.EsFungible + this.FechaVencimiento).hashCode();  
					}
					
					@Override
					public boolean equals(Object o) {
						FlujoCustodiaIFElem e = (FlujoCustodiaIFElem)o;
						return this.IdTipoOperacion.equalsIgnoreCase(e.IdTipoOperacion) && this.Fecha.equalsIgnoreCase(e.Fecha) && this.EsFungible.equalsIgnoreCase(e.EsFungible)
								&& this.IdPortafolio.equals(e.IdPortafolio) && (this.IdBoveda == e.IdBoveda || this.IdBoveda.equalsIgnoreCase(e.IdBoveda)) && this.IdInstrumento.equals(e.IdInstrumento)
								&& this.IdTipoReajuste.equalsIgnoreCase(e.IdTipoReajuste) && this.IdEmisor.equalsIgnoreCase(e.IdEmisor)
								&& this.FechaVencimiento.equalsIgnoreCase(e.FechaVencimiento);
					}
					
				}
				
				ConsultarCustodiaIngresoOrden consultarCustodiaService = new ConsultarCustodiaIngresoOrden();
				Long DiasLiquidacionContadoMaximo = ordenes.get(0).DiasLiquidacionContado;
				java.sql.Date fechaOperacionMinima = ordenes.get(0).FechaSistemaCreacion;
				HashMap<FlujoCustodiaElem,BigDecimal> totalCustodiaOrdenes = new HashMap<>();
				HashMap<FlujoCustodiaIFElem,BigDecimal> totalCantidadOrdenesIF = new HashMap<>();
				HashMap<FlujoCustodiaIFElem,BigDecimal> totalValorRescateOrdenesIF = new HashMap<>();
				
				HashSet<ObjORDENES> ordenesConsultarCustodia = new HashSet<ObjORDENES>();
				for (OrdenProceso ord : ordenes) {
					if (ord.ValidaSaldoCustodia.equalsIgnoreCase(_c.NO))
						continue;
					
					if (ord.DiasLiquidacionContado.compareTo(DiasLiquidacionContadoMaximo) == 1) {
						DiasLiquidacionContadoMaximo = ord.DiasLiquidacionContado;
					}
					
					java.sql.Date fechaLiquidacion = ord.FechaLiquidacion;
					
					if (fechaLiquidacion.before(fechaOperacionMinima)) {
						fechaOperacionMinima = fechaLiquidacion;
					}
					
					
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) {
						FlujoCustodiaIFElem llaveIF = new FlujoCustodiaIFElem(ord.IdBoveda, ord.IdTipoOperacion, ord.IdPortafolioCliente, Fecha.toStringDate(ord.FechaLiquidacion), ord.IdInstrumento,ord.IdEmisor, ord.IdTipoReajuste, ord.EsFungible, Fecha.toStringDate(ord.FechaVencimientoIF));
						if (!totalCantidadOrdenesIF.containsKey(llaveIF)) {
							totalCantidadOrdenesIF.put(llaveIF, BigDecimal.ZERO);
							totalValorRescateOrdenesIF.put(llaveIF, BigDecimal.ZERO);
						}
						BigDecimal totalCantidad = totalCantidadOrdenesIF.get(llaveIF);
						totalCantidad = totalCantidad.add(ord.Cantidad);
						totalCantidadOrdenesIF.put(llaveIF,totalCantidad);
						
						BigDecimal totalValorRescate = totalValorRescateOrdenesIF.get(llaveIF);
						totalValorRescate = totalValorRescate.add(ord.ValorRescate);
						totalValorRescateOrdenesIF.put(llaveIF,totalValorRescate);
						
					}else {
						FlujoCustodiaElem llave = new FlujoCustodiaElem(ord.IdBoveda, ord.IdTipoOperacion, ord.IdPortafolioCliente, Fecha.toStringDate(ord.FechaLiquidacion), ord.IdInstrumentoCustodia);
						
						if (!totalCustodiaOrdenes.containsKey(llave)) {
							totalCustodiaOrdenes.put(llave, BigDecimal.ZERO);
						}
						BigDecimal totalCantidad = totalCustodiaOrdenes.get(llave);
						totalCantidad = totalCantidad.add(ord.Cantidad);
						totalCustodiaOrdenes.put(llave,totalCantidad);
					}
					
					ObjORDENES port = new ObjORDENES();
					port.IdBoveda = ord.IdBoveda;
					port.IdPortafolio = ord.IdPortafolioCliente;
					port.IdTipoOperacion = ord.IdTipoOperacion;
					port.IdInstrumento = ord.IdInstrumento;
					port.CumpleParaVentaDescubierta = ord.CumpleParaVentaDescubierta;  //Si orden cumple requisitos para una posible venta descubierta
					port.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
					ordenesConsultarCustodia.add(port);
				}
				
				//actualizamos ordenes con total por grupo
				Iterator<Entry<FlujoCustodiaIFElem, BigDecimal>> it1 = totalCantidadOrdenesIF.entrySet().iterator();
				Iterator<Entry<FlujoCustodiaIFElem, BigDecimal>> it2 = totalValorRescateOrdenesIF.entrySet().iterator();
			    while (it1.hasNext()) {
			    	Entry<FlujoCustodiaIFElem, BigDecimal> pairCantidad = it1.next();
			    	Entry<FlujoCustodiaIFElem, BigDecimal> pairValorRescate = it2.next();
			        for (OrdenProceso orden : ordenes) {
			        	if (orden.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && pairCantidad.getKey().Fecha.equalsIgnoreCase(Fecha.toStringDate(orden.FechaLiquidacion))
			        			&& (pairCantidad.getKey().IdBoveda == orden.IdBoveda || pairCantidad.getKey().IdBoveda.equalsIgnoreCase(orden.IdBoveda)) && pairCantidad.getKey().IdInstrumento.equals(orden.IdInstrumentoCustodia)
			        			&& pairCantidad.getKey().IdPortafolio.equals(orden.IdPortafolioCliente) && pairCantidad.getKey().IdTipoOperacion.equalsIgnoreCase(orden.IdTipoOperacion)
			        			&& pairCantidad.getKey().EsFungible.equalsIgnoreCase(orden.EsFungible) && pairCantidad.getKey().IdEmisor.equalsIgnoreCase(orden.IdEmisor)
			        			&& pairCantidad.getKey().IdTipoReajuste.equalsIgnoreCase(orden.IdTipoReajuste) && pairCantidad.getKey().FechaVencimiento.equalsIgnoreCase(Fecha.toStringDate(orden.FechaVencimientoIF))
			        			) {
			        		orden.TotalCantidadCustodiaPortafolio = pairCantidad.getValue();
			        		orden.TotalValorRescateCustodiaPortafolio = pairValorRescate.getValue();
			        	}
			        	
			        }
			    }
			    
			  //actualizamos ordenes con total por grupo
				Iterator<Entry<FlujoCustodiaElem, BigDecimal>> it3 = totalCustodiaOrdenes.entrySet().iterator();
			    while (it3.hasNext()) {
			    	Entry<FlujoCustodiaElem, BigDecimal> pair = it3.next();
			        for (OrdenProceso orden : ordenes) {
			        	if (!orden.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && pair.getKey().Fecha.equalsIgnoreCase(Fecha.toStringDate(orden.FechaLiquidacion))
			        			&& (pair.getKey().IdBoveda == orden.IdBoveda || pair.getKey().IdBoveda.equalsIgnoreCase(orden.IdBoveda)) && pair.getKey().IdInstrumento.equals(orden.IdInstrumentoCustodia)
			        			&& pair.getKey().IdPortafolio.equals(orden.IdPortafolioCliente) && pair.getKey().IdTipoOperacion.equalsIgnoreCase(orden.IdTipoOperacion)
			        			) {
			        		orden.TotalCantidadCustodiaPortafolio = pair.getValue();
			        	}
			        }
			    }
				
				if (ordenesConsultarCustodia.size() > 0) {
					ConsultarCustodiaIngresoOrden.ServiceEntrada entradaConsultarCustodia = new ConsultarCustodiaIngresoOrden.ServiceEntrada();
					entradaConsultarCustodia.P_DIASPROYECCION = DiasLiquidacionContadoMaximo;
					entradaConsultarCustodia.P_FECHASALDO = Fecha.toStringDate(fechaOperacionMinima);
					entradaConsultarCustodia.P_IDEMPRESA = in.p_IdEmpresa;
					entradaConsultarCustodia.P_IDMODULO = in.p_IdModulo;
					entradaConsultarCustodia.P_IDUSUARIO = in.p_IdUsuario;
					entradaConsultarCustodia.P_VALORIZARFLUJO = "N";
					{
						if(in.p_BarraProgreso != null) {
							BarraProgreso subBarraSaldoFlujoCustodia = in.p_BarraProgreso.subrango(80, 90);
							entradaConsultarCustodia.p_BarraProgreso = subBarraSaldoFlujoCustodia;
						}
					}
					entradaConsultarCustodia.P_ORDENES = new ArrayList<ObjORDENES>(ordenesConsultarCustodia) ;
					ServiceSalida salidaConsultarCustodia = consultarCustodiaService.logica(con,entradaConsultarCustodia);
					{
						if(in.p_BarraProgreso != null) {
							in.p_BarraProgreso.actualizar(91L,"PROCESANDO","Validando datos de custodia");
						}
					}
					List<ObjERRORESDETALLE> erroresSaldoCustodia = salidaConsultarCustodia.P_OUT_ERRORESDETALLE;
					
					for (OrdenProceso ord : ordenes){
						for (ObjERRORESDETALLE err : erroresSaldoCustodia) {
							if (ord.IdTipoOperacion.equalsIgnoreCase(err.IdTipoOperacion)
									&& ord.IdPortafolioCliente.equals(err.IdPortafolio)) {
								eliminarOrdenBloqueada(con,in.p_IdEmpresa,valores,ord,out.P_OUT_MENSAJES);
								out.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,err.Codigo,err.Descripcion));
								ord.errores++;
							}
						}
					}
					
					limpiarOrdenes(ordenes, ordenesConErrores);
					if (ordenes.size() == 0) {
						for(MensajeSalida mje : out.P_OUT_MENSAJES){
							if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
								out.P_OUT_ERRORESCONTADOR++;
							}
						}
						if(out.P_OUT_ERRORESCONTADOR.compareTo(0L)>=1L) {
							{
								if(in.p_BarraProgreso != null) {
									JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
									in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
								}
							}
						}
						return out;
					}
					
					BigDecimal totalCantidad = BigDecimal.ZERO;
					String ultimaFechaSaldo = "";
					Long idInstrumentoActual = 0L;
					String ultimoUsoTipoOperacion = "";
					salidaConsultarCustodia.P_OUT_SALIDA.sort(Comparator
							.comparing((cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA s) -> s.FechaSaldo,
									Comparator.nullsLast(Comparator.naturalOrder()))
							.thenComparing((cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA s) -> s.IdInstrumento,
									Comparator.nullsLast(Comparator.naturalOrder()))
							.thenComparing((cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA s) -> s.UsoTipoOperacion,
									Comparator.nullsLast(Comparator.naturalOrder()))
							); // nullsLast pone los null al final
					
					for (ConsultarCustodiaIngresoOrden.ObjSALIDA sld : salidaConsultarCustodia.P_OUT_SALIDA) {
						if (sld.IdInstrumento != null) {
							if (!ultimaFechaSaldo.equalsIgnoreCase(sld.FechaSaldo) || idInstrumentoActual.compareTo(sld.IdInstrumento) != 0 || !ultimoUsoTipoOperacion.equalsIgnoreCase(sld.UsoTipoOperacion)) {
								totalCantidad = BigDecimal.ZERO; // Reiniciar la suma
							}

							idInstrumentoActual = sld.IdInstrumento;
							ultimaFechaSaldo = sld.FechaSaldo;
							totalCantidad = totalCantidad.add(sld.Cantidad);
							ultimoUsoTipoOperacion = sld.UsoTipoOperacion;
						}
						for (OrdenProceso ord : ordenes) {
							if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && 
									sld.FechaSaldo.equals(Fecha.toStringDate(ord.FechaLiquidacion)) && sld.EsFungible.equals(ord.EsFungible) &&
									sld.FechaVencimiento.equals(ord.FechaVencimientoIF) && (sld.IdBoveda == ord.IdBoveda || sld.IdBoveda.equals(ord.IdBoveda)) && 
									sld.IdEmisor.equalsIgnoreCase(ord.IdEmisor) && sld.IdInstrumento.equals(ord.IdInstrumentoCustodia) &&
									sld.IdPortafolio.equals(ord.IdPortafolioCliente) && sld.IdTipoOperacion.equalsIgnoreCase(ord.IdTipoOperacion) &&
									sld.IdTipoReajuste.equalsIgnoreCase(ord.IdTipoReajuste)
									) {
								ord.SaldoCantidadCustodiaPortafolio = sld.Cantidad;
								ord.SaldoValorRescateCustodiaPortafolio = sld.ValorRescate;
							}
							if(!ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && sld.IdInstrumento != null &&
									sld.FechaSaldo.equals(Fecha.toStringDate(ord.FechaLiquidacion)) && (sld.IdBoveda != null && sld.IdBoveda.equals(ord.IdBoveda)) && 
									sld.IdInstrumento.equals(ord.IdInstrumentoCustodia) && sld.IdPortafolio.equals(ord.IdPortafolioCliente) && sld.IdTipoOperacion.equalsIgnoreCase(ord.IdTipoOperacion)) {
								if (sld.UsoTipoOperacion.equalsIgnoreCase("VENDES")) 
								{	// Flujo Venta Descubierta (Solo para RV)
									ord.SaldoCantidadVentaDescubiertaCustodiaPortafolio = sld.Cantidad;
								}
								else
								{	// Otros flujos
									ord.SaldoCantidadCustodiaPortafolio = totalCantidad;
								}
							}			
						}
					}
				}
			}
			
			
			//CALCULO EXEGIBILIDAD y COBERTURA
			{
				for (OrdenProceso ord : ordenes){
					if(_c.SI.equalsIgnoreCase(ord.EsVentaCorta) || _c.TIPO_INSTRUMENTO_SIM.equalsIgnoreCase(ord.IdTipoInstrumento) || 
							(_c.TIPO_OPERACION_VENRRV.equalsIgnoreCase(ord.IdTipoOperacion)) && in.p_simularOperacion.equalsIgnoreCase("N")){
						//Cobertura
						CalcularCoberturaOrdenes calcularCoberturaOrdenes = new CalcularCoberturaOrdenes();
						EntradaCoberturasOrdenes entradaCoberturasOrdenes = new EntradaCoberturasOrdenes();
						entradaCoberturasOrdenes.p_IdEmpresa = in.p_IdEmpresa;
						entradaCoberturasOrdenes.p_IdUsuario = in.p_IdUsuario;
						entradaCoberturasOrdenes.p_IdModulo = in.p_IdModulo;
						entradaCoberturasOrdenes.p_EsVentaCorta = ord.EsVentaCorta;
						entradaCoberturasOrdenes.p_IdBolsa = ord.IdBolsa;
						entradaCoberturasOrdenes.p_IdPortafolio = ord.IdPortafolioCliente;
						entradaCoberturasOrdenes.p_IdInstrumento = ord.IdInstrumento;
						entradaCoberturasOrdenes.p_IdTipoInstrumento = ord.IdTipoInstrumento;
						entradaCoberturasOrdenes.p_Simulacion = new ArrayList<InCoberturaSimulacion>();
						entradaCoberturasOrdenes.p_UsaReglasNegocio = "S";
						entradaCoberturasOrdenes.p_IdMoneda = ord.IdMonedaLiquidacionOrden;
						if(_c.TIPO_INSTRUMENTO_SIM.equalsIgnoreCase(ord.IdTipoInstrumento) || _c.TIPO_OPERACION_VENRRV.equalsIgnoreCase(ord.IdTipoOperacion)){
							for(ObjGarantiasIn garantia : in.p_Garantias){
								if(ord.IdPortafolioCliente.longValue() == garantia.IdPortafolio.longValue()){
									InCoberturaSimulacion inCoberturaSimulacion = new InCoberturaSimulacion();
									inCoberturaSimulacion.IdReferencial = ord.IdInterno;
									inCoberturaSimulacion.IdInstrumento = garantia.IdInstrumento;
									inCoberturaSimulacion.Cantidad = BigDecimal.ZERO;
									inCoberturaSimulacion.PrecioMercado = BigDecimal.ZERO;
									inCoberturaSimulacion.Monto = garantia.Monto;
									inCoberturaSimulacion.MonedaDestino = garantia.IdMoneda;
									entradaCoberturasOrdenes.p_Simulacion.add(inCoberturaSimulacion);
								}
							}
						}
						SalidaCoberturasOrdenes salidaCoberturasOrdenes = calcularCoberturaOrdenes.logica(con, entradaCoberturasOrdenes);
						BigDecimal montoCoberturaCorredora = BigDecimal.ZERO;
						BigDecimal montoCoberturaEntidad = BigDecimal.ZERO;
						for(OutCoberturaPortafolio salida : salidaCoberturasOrdenes.p_out_TableCoberturaPortafolio){
							montoCoberturaCorredora = montoCoberturaCorredora.add(salida.MontoCoberturaCorredora);
							montoCoberturaEntidad = montoCoberturaEntidad.add(salida.MontoCoberturaEntidad);
						}
						
						/*CELCONDOR-13 Para los reportes en Peru el calculo de cobertura no se considera lo que esta en Portafolio*/
						if(localizacionSistema.equalsIgnoreCase("PE") && (ord.IdTipoInstrumento.equalsIgnoreCase("RRV") || ord.IdTipoInstrumento.equalsIgnoreCase("RRF")))
						{
							montoCoberturaCorredora = BigDecimal.ZERO;
							montoCoberturaEntidad = BigDecimal.ZERO;
						}
						
						for(OutCoberturaSimulacion salida : salidaCoberturasOrdenes.p_out_TableCoberturaSimulacion){
							montoCoberturaCorredora = montoCoberturaCorredora.add(salida.MontoCoberturaCorredora);
							montoCoberturaEntidad = montoCoberturaEntidad.add(salida.MontoCoberturaEntidad);
						}
						
						//Exigibilidad
						CalcularExigibilidadOrdenes calcularExigibilidadOrdenes = new CalcularExigibilidadOrdenes();
						EntradaExigibilidadOrdenes entradaExigibilidadOrdenes = new EntradaExigibilidadOrdenes();
						ObjExigibilidadEntrada inTableExigibilidadSimulacion = new ObjExigibilidadEntrada();
						entradaExigibilidadOrdenes.p_IdEmpresa = in.p_IdEmpresa;
						entradaExigibilidadOrdenes.p_IdUsuario = in.p_IdUsuario;
						entradaExigibilidadOrdenes.p_IdModulo = in.p_IdModulo;
						entradaExigibilidadOrdenes.p_simularOperacion = "N";
						if(in.p_simularOperacion != null) {
							entradaExigibilidadOrdenes.p_simularOperacion = in.p_simularOperacion;
						}
						entradaExigibilidadOrdenes.p_EsVentaCorta = ord.EsVentaCorta;
						entradaExigibilidadOrdenes.p_IdBolsa = ord.IdBolsa;
						//entradaExigibilidadOrdenes.p_IdInstrumento = ord.IdInstrumento;
						entradaExigibilidadOrdenes.p_IdInstrumento = ord.IdInstrumentoSubyacente;
						entradaExigibilidadOrdenes.p_IdPortafolio = ord.IdPortafolioCliente;
						entradaExigibilidadOrdenes.p_IdTipoInstrumento = ord.IdTipoInstrumento;
						entradaExigibilidadOrdenes.p_UsaReglasNegocio = "S";
						entradaExigibilidadOrdenes.p_IdMoneda = ord.IdMonedaLiquidacionOrden;
						entradaExigibilidadOrdenes.p_TableExigibilidadSimulacion = new ArrayList<ObjExigibilidadEntrada>();
						inTableExigibilidadSimulacion.IdInterno = ord.IdInterno;
						inTableExigibilidadSimulacion.IdBolsa = ord.IdBolsa;
						inTableExigibilidadSimulacion.IdInstrumento = ord.IdInstrumento;
						inTableExigibilidadSimulacion.Cantidad = ord.Cantidad;
						inTableExigibilidadSimulacion.Precio = ord.PrecioOrden;
						inTableExigibilidadSimulacion.Monto = ord.MontoOrden;
						entradaExigibilidadOrdenes.p_TableExigibilidadSimulacion.add(inTableExigibilidadSimulacion);
						SalidaExigibilidadOrdenes salidaExigibilidadOrdenes = calcularExigibilidadOrdenes.logica(con, entradaExigibilidadOrdenes);
						BigDecimal montoExigibilidadCorredora = BigDecimal.ZERO;
						BigDecimal montoExigibilidadEntidad = BigDecimal.ZERO;
						for(ObjExigibilidadPortafolio salida : salidaExigibilidadOrdenes.p_out_TableExigibilidadPortafolio){
							montoExigibilidadCorredora = montoExigibilidadCorredora.add(salida.MontoExigibilidadCorredora);
							montoExigibilidadEntidad = montoExigibilidadEntidad.add(salida.MontoExigibilidadEntidad);
						}
						
						/*CELCONDOR-13 Para los reportes en Peru el calculo de exigibilidad no se considera lo que esta en Portafolio*/
						if(localizacionSistema.equalsIgnoreCase("PE") && (ord.IdTipoInstrumento.equalsIgnoreCase("RRV") || ord.IdTipoInstrumento.equalsIgnoreCase("RRF")))
						{
							montoExigibilidadCorredora = BigDecimal.ZERO;
							montoExigibilidadEntidad = BigDecimal.ZERO;
						}
						
						for(ObjExigibilidadSimulacion salida : salidaExigibilidadOrdenes.p_out_TableExigibilidadSimulacion){
							montoExigibilidadCorredora = montoExigibilidadCorredora.add(salida.MontoExigibilidadCorredora);
							montoExigibilidadEntidad = montoExigibilidadEntidad.add(salida.MontoExigibilidadEntidad);
						}
						ord.MontoCoberturaExigibilidadCorredora = montoExigibilidadCorredora.subtract(montoCoberturaCorredora, MathContext.DECIMAL128);
						ord.MontoCoberturaExigibilidadEntidad = montoExigibilidadEntidad.subtract(montoCoberturaEntidad, MathContext.DECIMAL128);
					}
				}
			}
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(94L,"PROCESANDO","Validando datos de la orden");
				}
			}
			//VALIDACIONES
			{
				for (OrdenProceso ord : ordenes) {
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.MontoCoberturaExigibilidadCorredora.doubleValue() > 0),"MONTO_COBERTURA_MENOR_EXIGIBILIDAD_CORREDORA","CO_ExigibilidadCorredora",ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.MontoCoberturaExigibilidadEntidad.doubleValue() > 0),"MONTO_COBERTURA_MENOR_EXIGIBILIDAD_ENTIDAD","CO_ExigibilidadEntidad",ord,requisitos,out.P_OUT_MENSAJES);
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.MontoMaximoDistribucionOrden.doubleValue() > 0 && ord.MontoLocal.doubleValue() > ord.MontoMaximoDistribucionOrden.doubleValue()),"MONTO_DISTRIBUCION_SUPERA_PERMITIDO","CO_MontoSuperaMaximoPermitidoCanalDistribucion",ord,requisitos,out.P_OUT_MENSAJES);
					
					//CELLINCECT-592: Opera con saldo Negativo // SE IMPLEMENTA PARAMETRO QUE PERMITE OPERAR ORDENES SIN SALDO EN LA CTA INVERSION.
					if((ord.OperaConSaldoNegativo.equalsIgnoreCase("N") || !ord.IngresoDesde.equalsIgnoreCase("COR")) && ord.PagoCobroCuentaInversion.equalsIgnoreCase("S")) {
						/*CELCONDOR-58 En la validacion de la linea de sobre giro se quita que sume la linea de sobre giro del portafolio por que en la orden ya la considera*/
						ProcesaRequisito(con,in.p_IdEmpresa,(ord.ValidaSaldoCaja.equalsIgnoreCase(_c.SI) && ord.TotalMontoPortafolio.doubleValue() > ord.SaldoCajaPortafolio.doubleValue()),"FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO","CO_OperacionSinFlujoCaja",ord,requisitos,out.P_OUT_MENSAJES);	
					}// FIN CELLINCECT-592
					
					boolean indicador_sobregiro = false;
					if (operaciones.stream().anyMatch(o -> o.equals(ord.IdTipoOperacion))) {
						if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && indicadoresPortafolio.get(ord.IdPortafolioCliente).SobregiroRF.equalsIgnoreCase("S") ) {
							indicador_sobregiro = true;
						} else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && indicadoresPortafolio.get(ord.IdPortafolioCliente).SobregiroRV.equalsIgnoreCase("S") ) {
							indicador_sobregiro = true;
						} else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && indicadoresPortafolio.get(ord.IdPortafolioCliente).SobregiroIF.equalsIgnoreCase("S") ) {
							indicador_sobregiro = true;
						}
					}
					
					//flujo para if fungible
					if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && ord.EsFungible.equalsIgnoreCase(_c.SI)) {
						if (!indicador_sobregiro) {
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.ValidaSaldoCustodia.equals(_c.SI) && ord.TotalValorRescateCustodiaPortafolio.doubleValue() > ord.SaldoValorRescateCustodiaPortafolio.doubleValue()),"FLUJO_CUSTODIA_PORTAFOLIO_INSUFICIENTE","CO_OperacionSinFlujoCustodia",ord,requisitos,out.P_OUT_MENSAJES);
						} else {
							if (ord.TotalValorRescateCustodiaPortafolio.doubleValue() > ord.SaldoValorRescateCustodiaPortafolio.doubleValue()) {
								
								HashMap<String, String> datos = new HashMap<String, String>();
								DecimalFormat formatea = new DecimalFormat("###,###.##");
								datos.put("Cantidad", ""+formatea.format(ord.TotalValorRescateCustodiaPortafolio.doubleValue() - ord.SaldoValorRescateCustodiaPortafolio.doubleValue()));
								datos.put("Nemotecnico", ord.NemotecnicoSVS);
								datos.put("Nombre", ord.NombreCompleto);
								datos.put("Portafolio", ord.CodigoPortafolio);
								out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ASIGNACION_SOBREGIRO",datos ,con,in.p_IdEmpresa));
							}
						}
					}
					//flujo para if no fungible
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && ord.EsFungible.equalsIgnoreCase(_c.NO)) {
						if (!indicador_sobregiro) {
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.ValidaSaldoCustodia.equals(_c.SI) && ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadCustodiaPortafolio.doubleValue()),"FLUJO_CUSTODIA_PORTAFOLIO_INSUFICIENTE","CO_OperacionSinFlujoCustodia",ord,requisitos,out.P_OUT_MENSAJES);
						} else {
							if (ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadCustodiaPortafolio.doubleValue()) {
								
								HashMap<String, String> datos = new HashMap<String, String>();
								DecimalFormat formatea = new DecimalFormat("###,###.##");
								datos.put("Cantidad", ""+formatea.format(ord.TotalCantidadCustodiaPortafolio.doubleValue() - ord.SaldoCantidadCustodiaPortafolio.doubleValue()));
								datos.put("Nemotecnico", ord.NemotecnicoSVS);
								datos.put("Nombre", ord.NombreCompleto);
								datos.put("Portafolio", ord.CodigoPortafolio);
								out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ASIGNACION_SOBREGIRO",datos ,con,in.p_IdEmpresa));
							}
						}

					}
					
					else if(ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_CLI) && ord.IdTipoOperacion.equals("VENACN")){
						ProcesaRequisito(con,in.p_IdEmpresa,(ord.TotalCantidadCustodiaPortafolio.compareTo(ord.SaldoCantidadCustodiaPortafolio) > 0),"FLUJO_CUSTODIA_PORTAFOLIO_INSUFICIENTE_CLI","CO_FlujoCantidadCustodiaInsuficiente",ord,requisitos,out.P_OUT_MENSAJES);
						if (ord.TotalCantidadCustodiaPortafolio.compareTo(ord.SaldoCantidadCustodiaPortafolio) > 0) {
							ord.Cantidad = ord.CumpleParaVentaDescubierta=="S"?ord.Cantidad:ord.SaldoCantidadCustodiaPortafolio;
						}
					}
					//flujo para los demas
					else {
						//Si orden es una posible venta descubierta y "Cantidad Orden" > "Saldo Custodia para Ordenes" --> Se valida el flujo Venta Descubierta
						if (ord.CumpleParaVentaDescubierta.equalsIgnoreCase("S") && (ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadCustodiaPortafolio.doubleValue()))
						{	//Evalua Flujo VENTA DESCUBIERTA
							ProcesaRequisito(con,in.p_IdEmpresa,(ord.ValidaSaldoCustodia.equals(_c.SI) && ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadVentaDescubiertaCustodiaPortafolio.doubleValue()),"FLUJO_CUSTODIA_VENTA_DESCUBIERTA_INSUFICIENTE","CO_OperacionSinFlujoVentaDescubierta",ord,requisitos,out.P_OUT_MENSAJES);	
						}
						else
						{	//Evalua el flujo normal
							
							if (indicador_sobregiro) {
								if (ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadCustodiaPortafolio.doubleValue()) {
									
									HashMap<String, String> datos = new HashMap<String, String>();
									DecimalFormat formatea = new DecimalFormat("###,###.##");
									datos.put("Cantidad", ""+formatea.format(ord.TotalCantidadCustodiaPortafolio.doubleValue() - ord.SaldoCantidadCustodiaPortafolio.doubleValue()));
									datos.put("Nemotecnico", ord.Nemotecnico);
									datos.put("Nombre", ord.NombreCompleto);
									datos.put("Portafolio", ord.CodigoPortafolio);
									out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ASIGNACION_SOBREGIRO",datos ,con,in.p_IdEmpresa));
								}
							}
							else {
								ProcesaRequisito(con,in.p_IdEmpresa,(ord.ValidaSaldoCustodia.equals(_c.SI) && ord.TotalCantidadCustodiaPortafolio.doubleValue() > ord.SaldoCantidadCustodiaPortafolio.doubleValue()),"FLUJO_CUSTODIA_PORTAFOLIO_INSUFICIENTE","CO_OperacionSinFlujoCustodia",ord,requisitos,out.P_OUT_MENSAJES);	
							}
						}
					}
					
					ProcesaRequisito(con,in.p_IdEmpresa,(ord.PlantillaEmpleadoMontoMaximoOperacion.doubleValue() > 0 && ord.MontoLocal.doubleValue() > ord.PlantillaEmpleadoMontoMaximoOperacion.doubleValue() ),"MONTO_OPERACION_SUPERA_MAXIMO_PERMITIDO_PLANTILLA_EJECUTIVO","CO_MontoOperacionSuperaPermitidoPlantilla",ord,requisitos,out.P_OUT_MENSAJES);
					
					ProcesaRequisito(con,in.p_IdEmpresa,(montoMaximoOrden.longValue() > 0 && ord.MontoTotalLocal.doubleValue() > montoMaximoOrden.doubleValue() ),"MONTO_ORDEN_SUPERA_MAXIMO_PERMITIDO_CORREDORA",MensajeSalida.MENSAJE_ERROR,ord,requisitos,out.P_OUT_MENSAJES);
					
					for (CobroOrdenProceso cob: ord.cobros){
						if (localizacionSistema.equalsIgnoreCase("CL")) {
							if (cob.CodigoCobroClasificacion.equals("COMISION") && cob.FormaCobro.equals("V")) {
								BigDecimal MaximoDescuentoComision = BigDecimal.valueOf(1.0).add(ord.PlantillaEmpleadoMaximoDescuentoComision.divide(BigDecimal.valueOf(100.0)));
								BigDecimal MinimoDescuentoComision = BigDecimal.valueOf(1.0).subtract(ord.PlantillaEmpleadoMaximoDescuentoComision.divide(BigDecimal.valueOf(100.0)));
								ProcesaRequisito(con,in.p_IdEmpresa,((cob.ValorCobroPerfil.multiply(MaximoDescuentoComision).doubleValue()) < (cob.ValorCobroFinal.doubleValue())),"DESCUENTO_COMISION_EXCEDE_PERMITIDO","CO_DescuentoComisionSuperaPermitidoPlantilla",ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,((cob.ValorCobroPerfil.multiply(MinimoDescuentoComision).doubleValue()) > (cob.ValorCobroFinal.doubleValue())),"DESCUENTO_COMISION_BAJO_MINIMO_PERMITIDO","CO_DescuentoComisionSuperaPermitidoPlantilla",ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,(cob.ValorCobroFinal.doubleValue() > porcentajeMaximoComision.doubleValue()),"PORCENTAJE_COMISION_SOBRE_MAXIMO_PERMITIDO_CORREDORA","CO_ComisionSuperaMaximoPermitido",ord,requisitos,out.P_OUT_MENSAJES);
								ProcesaRequisito(con,in.p_IdEmpresa,(cob.ValorCobroFinal.doubleValue() < porcentajeMinimoComision.doubleValue()),"PORCENTAJE_COMISION_BAJO_MINIMO_PERMITIDO_CORREDORA","CO_ComisionBajoMinimoPermitido",ord,requisitos,out.P_OUT_MENSAJES);
								
							}
						}
						if (localizacionSistema.equalsIgnoreCase("PE")) {
							if (cob.CodigoCobroClasificacionBase.equals("COMISION") && cob.FormaCobro.equalsIgnoreCase("PORCENTAJEFIJO") && cob.Otros_CheckAllIn.equals("S")) {
								BigDecimal variacion = cob.ValorCobroPerfil.subtract(ord.PorcentajeAllIn).abs();
								boolean res = variacion.subtract(ord.PlantillaEmpleadoMaximoDescuentoComision).abs().compareTo(new BigDecimal("0.0001")) == 1;
								ProcesaRequisito(con,in.p_IdEmpresa,( res && !ord.PlantillaEmpleadoMaximoDescuentoComision.equals(new BigDecimal(100)) ),"VARIACION_COMISION_EXCEDE_PERMITIDO","CO_DescuentoComisionSuperaPermitidoPlantilla",ord,requisitos,out.P_OUT_MENSAJES);
							}
							else if (cob.CodigoCobroClasificacionBase.equals("COMISION") && cob.FormaCobro.equalsIgnoreCase("PORCENTAJEFIJO") && cob.Otros_CheckAllIn.equals("N")) {
								BigDecimal variacion = cob.ValorCobroPerfil.subtract(cob.ValorCobroFinal).abs();
								boolean res = variacion.subtract(ord.PlantillaEmpleadoMaximoDescuentoComision).abs().compareTo(new BigDecimal("0.00001")) == 1;
								ProcesaRequisito(con,in.p_IdEmpresa,( res && !ord.PlantillaEmpleadoMaximoDescuentoComision.equals(new BigDecimal(100)) ),"VARIACION_COMISION_EXCEDE_PERMITIDO","CO_DescuentoComisionSuperaPermitidoPlantilla",ord,requisitos,out.P_OUT_MENSAJES);
							}
						}
					}
				}
				
				limpiarOrdenes(ordenes, ordenesConErrores);
				if (ordenes.size() == 0) {
					for(MensajeSalida mje : out.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
							out.P_OUT_ERRORESCONTADOR++;
						}
					}
					if(out.P_OUT_ERRORESCONTADOR.compareTo(0L) >= 1L) {
						{
							if(in.p_BarraProgreso != null) {
								JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
								in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
							}
						}
					}
					return out;
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(94L,"PROCESANDO","Validando estado de la orden");
				}
			}
			
			//CALCULO DE ESTADO DE LA ORDEN
			{
				
				for (OrdenProceso ord : ordenes) {
					//evaluar si la orden esta pendiente de autorizacion
					if (ord.autorizaciones == 0 && 
							(ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_AGENTE) 
							|| ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_OWP) 
							|| ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_CLI) 
							|| ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_WS)
							|| ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_APP))) {
						ord.IdEstadoOrden = _c.ESTADO_ORDEN_INGRESADA;
						ord.IdEstadoInterno = _c.ESTADO_ORDEN_AUTORIZADA;
						ord.FechaAceptacion = ord.FechaOperacion;
						ord.FechaAutorizacion = Fecha.toDate(Fecha.toStringDate(ord.FechaOperacion));
					}
					else if (ord.autorizaciones == 0 && ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) {
						ord.IdEstadoOrden = _c.ESTADO_ORDEN_OFERTADA;
						ord.IdEstadoInterno = _c.ESTADO_ORDEN_AUTORIZADA;
						//IMPCREDOPT-2500 : FechaAceptacion de la orden postrera tendra 2 minutos menos que la FechaOperacion
						ord.FechaAceptacion = ord.FechaOperacion;
						ord.FechaAutorizacion = ord.FechaSistemaCreacion;
					}
					else {
						ord.FechaAceptacion = Fecha.toTimeStamp("0001-01-01 00:00:00");
						ord.FechaAutorizacion = Fecha.toDate("0001-01-01");
					}
					
					//evaluar si es necesario dejar la orden en estado pendientes
					if(ord.IdEstadoLimite.equalsIgnoreCase(_c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR)) {
						ord.FechaAceptacion = Fecha.toTimeStamp("0001-01-01 00:00:00");
						ord.FechaAutorizacion = Fecha.toDate("0001-01-01");
						ord.IdEstadoOrden = _c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR;
					}
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(92L,"PROCESANDO","Ingresando ordenes");
				}
			}
			
			//INSERTA ORDENES
			ArrayList<Orden> ordenesFinales = new ArrayList<>();
			ArrayList<CobroOrden> cobrosFinales = new ArrayList<>();
			ArrayList<FormaDePagoOrden> formasDePagoFinales = new ArrayList<>();
			{
				for (OrdenProceso ord: ordenes) {
					//Utils.copyPojo(ord, o1);
					Orden o1 = copyPojoOrden(ord);
					
					if (ord.EsVentaCorta.equalsIgnoreCase(_c.SI)) {
						o1.FechaVencimiento = ord.FechaVencimientoVentaCorta;
						BigDecimal montoPlazo = o1.MontoLiquidacion.multiply(BigDecimal.ONE.add(o1.TasaVentaCorta.multiply((new BigDecimal(o1.PlazoVentaCorta))).divide(new BigDecimal(100),20,RoundingMode.HALF_UP).divide(new BigDecimal(30),20,RoundingMode.HALF_UP)));
						o1.MontoPrimaVentaCorta = montoPlazo.subtract(o1.MontoLiquidacion);
					}
					else if (ord.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
						o1.FechaVencimiento = ord.FechaVencimientoSimultanea;
					}
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						o1.FechaVencimiento = ord.FechaVencimientoIF;
					}
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
						o1.FechaVencimiento = ord.FechaVencimientoReporte;
						o1.MontoOrden = ord.MontoOrden;
						o1.MontoLiquidacion = ord.MontoTotalLiquidacion;
						o1.MontoOrdenPlazo = ord.MontoOrdenPlazo;
						o1.MontoLiquidacionPlazo = ord.MontoTotalLiquidacionPlazo;
						/*
						if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")){
							ord.MontoOrden = ordenSalida.MontoOperacionLocal;
							ord.MontoOrdenPlazo = ordenSalida.MontoOperacionLocalPlazo;
							ord.MontoTotalLiquidacion = ordenSalida.MontoOperacionLiquidacion;
							ord.MontoTotalLiquidacionPlazo = ordenSalida.MontoOperacionLiquidacionPlazo;
						}*/
					}
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)) {
						o1.FechaVencimiento = ord.FechaVencimientoInstrumento;
					}
					else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
						o1.FechaVencimiento = ord.FechaVencimientoInstrumento;
					}
					else {
						o1.FechaVencimiento = Fecha.toDate("0001-01-01");
					}
					
					if (ord.MontoLiquidacion.compareTo(BigDecimal.ZERO) == 1)
						o1.PorcentajeCobros = (new BigDecimal(100)).multiply(ord.TotalCobrosLiquidacion).divide(ord.MontoLiquidacion,20,RoundingMode.HALF_UP);
					else
						o1.PorcentajeCobros = BigDecimal.ZERO;
					
					ordenesFinales.add(o1);
					
					for (CobroOrdenProceso cob : ord.cobros) {
						//Utils.copyPojo(cob, cobroFinal);
						CobroOrden cobroFinal = copyPojoCobroOrden(cob);
						
						cobroFinal.EsComprobante = "S";
						cobroFinal.IdGrupoCobro = cob.Orden.IdGrupoCobro;
						cobroFinal.IdOrden = cob.Orden.IdOrden;
						cobrosFinales.add(cobroFinal);
					}
					
					if (ord.PagoCobroCuentaInversion.equalsIgnoreCase(_c.SI) && aplicaFormaPagoCuentaInversionCanalIngresoClientes(ord.IdTipoCanalIngreso, ord.IdPreferenciaPagoPortafolio, preferenciasPago)) {
						FormaDePagoOrden forma = new FormaDePagoOrden();
						forma.IdOrden = ord.IdOrden;
						forma.IdMedioDePago = _c.MEDIO_PAGO_CUENTA_INVERSION;
						formasDePagoFinales.add(forma);
					}
					else if (ord.IdPreferenciaPagoPortafolio != null && preferenciasPago.get(ord.IdPreferenciaPagoPortafolio) != null) {
						PreferenciaPagoPortafolio pref =  preferenciasPago.get(ord.IdPreferenciaPagoPortafolio);
						FormaDePagoOrden forma = new FormaDePagoOrden();
						//Utils.copyPojo(pref, forma);
						//forma.IdOrden = pref.IdOrden;
						forma.IdPreferenciaPagoPortafolio = pref.IdPreferenciaPagoPortafolio;
						forma.IdMedioDePago = pref.IdMedioDePago;
						forma.IdCuentaBancaria = pref.IdCuentaBancaria;
						forma.IdTipoCuentaBancaria = pref.IdTipoCuentaBancaria;
						forma.NumeroCuentaBancaria = pref.NumeroCuentaBancaria;
						forma.IdTarjetaBancaria = pref.IdTarjetaBancaria;
						forma.IdTipoTarjetaBancaria = pref.IdTipoTarjetaBancaria;
						forma.NumeroTarjetaBancaria = pref.NumeroTarjetaBancaria;
						forma.IdModalidadDePago = pref.IdModalidadDePago;
						forma.IdBanco = pref.IdBanco;
						
						forma.IdOrden = ord.IdOrden;
						formasDePagoFinales.add(forma);
					}
				}
			}
			
			
			
			
			if (!in.p_simularOperacion.equalsIgnoreCase(_c.SI)) {
				//ACTUALIZA INSTRUMENTO OPCIONES PERU CON PRECIO MERCADO
				{
					List<cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.Instrumento> params = new ArrayList<>();
					for (Orden ord : ordenesFinales) {
						if (localizacionSistema.equalsIgnoreCase("PE") && ord.IdTipoInstrumento.equalsIgnoreCase("OPC")) { 
							cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.Instrumento i = new cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.Instrumento(); 
							i.IdInstrumento = ord.IdInstrumento;
							i.Precio = ord.PrecioOrden;
							params.add(i);
						}
					}
					if (params.size() > 0) {
						_sql.batch(con, SQL_ACTUALIZA_PRECIO_OPCION, params, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.Instrumento.class);
					}
				}
				
				//ASIGNA FOLIO ORDEN (PE)
				if (localizacionSistema.equalsIgnoreCase("PE")) {
					for(Orden ordfoliar : ordenesFinales){
						ObtenerFolioOrden ob = new ObtenerFolioOrden();
						String folio = ob.Obtener(con, in.p_IdEmpresa, ordfoliar);
						ordfoliar.Folio = folio;
					}
				}
				_sql.batch(con, SQL_INSERT_ORDENES, ordenesFinales , Orden.class);
				
				if (localizacionSistema.equalsIgnoreCase("CL")) {
					_sql.batch(con, SQL_INSERT_COBROS, cobrosFinales , CobroOrden.class);
				}
				else if (localizacionSistema.equalsIgnoreCase("PE")) {
					List<Long> ids = Utils.getSecuencia(con, "BSCL.Orden.BD.Secuencias::CobrosOrden", (long) cobrosFinales.size());
					for (int i=0; i < cobrosFinales.size(); i++){
						cobrosFinales.get(i).IdCobroOrden =  ids.get(i).longValue();
						//logger.debug(cobrosFinales.toString());
					}
					_sql.batch(con, SQL_INSERT_COBROS_PERU, cobrosFinales , CobroOrden.class);
					
					List<CobroRango> rangoCobroFinal = new ArrayList<>();
					for (CobroOrden cobRanFin: cobrosFinales){
						for (DefinicionRangoCobro ran : cobRanFin.RangoCobros){
							CobroRango rangoFin = new CobroRango();
							rangoFin.IdCobroOrden = cobRanFin.IdCobroOrden;
							rangoFin.IdOrden = cobRanFin.IdOrden;
							rangoFin.IdGrupoCobro = cobRanFin.IdGrupoCobro;
							rangoFin.IdEmpresa = ran.IdEmpresa;
							rangoFin.CodigoDefinicionCobro = ran.CodigoDefinicionCobro;
							rangoFin.Hasta = ran.Hasta;
							rangoFin.Valor = ran.Valor;
							//logger.debug(rangoFin.toString());
							rangoCobroFinal.add(rangoFin);
						}
					}
					if (rangoCobroFinal.size() > 0)
						_sql.batch(con, SQL_INSERT_COBROS_RANGOS_PERU, rangoCobroFinal, CobroRango.class);
					
				}
				if (formasDePagoFinales.size() > 0) {
					_sql.batch(con, SQL_INSERT_FORMA_DE_PAGO, formasDePagoFinales , FormaDePagoOrden.class);
				}
				
				//AGREGAR LOG
				{
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					for (OrdenProceso ord : ordenes) {
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = ord.IdOrden;
						log.IdTipoLog = "INGRESO";
						log.IdModulo = _c.ID_MODULO_ORDENES;
						log.IdUsuario = in.p_IdUsuario;
						log.IdEmpresa = in.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = in.p_IdUsuario;
						log.IdCliente = ord.IdCliente;
						log.EstadoCliente = ord.IdEstadoCliente;
						log.NombreCliente = ord.NombreCompleto;
						log.IdTipoDocumentoIdentidad = ord.TipoDocumentoIdentidad;
						log.IdDocumentoIdentidad = ord.DocumentoIdentidad;
						log.IdPortafolio = ord.IdPortafolioCliente;
						log.EstadoPortafolio = ord.IdEstadoPortafolio;
						if("Orden Postrera".equals(ord.Glosa)) {
						log.IdTipoRegistro = "ORDEN POSTRERA";
						} else { 
							log.IdTipoRegistro = "ORDEN";
						}
						log.EstadoRegistro = ord.IdEstadoOrden;
						log.IdTipoOperacion = ord.IdTipoOperacion;
						log.Folio = ord.IdOrden;
						log.Cantidad = ord.Cantidad;
						log.PrecioTasa = ord.PrecioOrden;
						log.Monto = ord.MontoTotalLiquidacion;
						log.IdMonedaOrigen = ord.IdMonedaOrden;
						log.IdMonedaLiquidacion = ord.IdMonedaLiquidacionOrden;
						log.TipoCambio = ord.TipoCambio.doubleValue()+"";
						log.Nemo = ord.Nemotecnico;
						log.IdProceso = 0L;
						log.EstadoProceso = "";
						log.NombreProceso = "Ingreso Orden";						
						entradaAgregarLog.p_Log.add(log);
					}
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(con, entradaAgregarLog);
				}
				
				//NOTIFICACION ORDEN
				{
					List<NotificarEstadoOrden.Orden> ordenesNotificacion = new ArrayList<NotificarEstadoOrden.Orden>();
					for (OrdenProceso ord : ordenes) {
						NotificarEstadoOrden.Orden ordenNotificar = new NotificarEstadoOrden.Orden();
						ordenNotificar.IdOrden = ord.IdOrden;					
						ordenesNotificacion.add(ordenNotificar);
					}
					
					NotificarEstadoOrden_Entrada entradaNotificarEstadoOrden = new NotificarEstadoOrden_Entrada();
					entradaNotificarEstadoOrden.p_IdEmpresa = in.p_IdEmpresa;
					entradaNotificarEstadoOrden.p_IdModulo = in.p_IdModulo;
					entradaNotificarEstadoOrden.p_IdUsuario = in.p_IdUsuario;
					entradaNotificarEstadoOrden.p_Ordenes = ordenesNotificacion;
					NotificarEstadoOrden notificarEstadoOrdenService = new NotificarEstadoOrden();
					notificarEstadoOrdenService.logicaAsync(con, entradaNotificarEstadoOrden);
				}
				//Notificar confirmacion ordenes
				{
					List<NotificarConfirmacionOrden.Orden> ordenesNotificacionConfirmacion = new ArrayList<NotificarConfirmacionOrden.Orden>();
					for (OrdenProceso ord : ordenesConfirmacion) {
						NotificarConfirmacionOrden.Orden ordenNotificar = new NotificarConfirmacionOrden.Orden();
						ordenNotificar.IdOrden = ord.IdOrden;					
						ordenesNotificacionConfirmacion.add(ordenNotificar);
					}
					
					NotificarConfirmacionOrden.NotificarConfirmacionOrden_Entrada entradaNotificarConfirmacionOrden = new NotificarConfirmacionOrden.NotificarConfirmacionOrden_Entrada();
					entradaNotificarConfirmacionOrden.p_IdEmpresa = in.p_IdEmpresa;
					entradaNotificarConfirmacionOrden.p_IdModulo = in.p_IdModulo;
					entradaNotificarConfirmacionOrden.p_IdUsuario = in.p_IdUsuario;
					entradaNotificarConfirmacionOrden.p_Ordenes = ordenesNotificacionConfirmacion;
					NotificarConfirmacionOrden notificarConfirmacionOrdenService = new NotificarConfirmacionOrden();
					notificarConfirmacionOrdenService.logicaAsync(con, entradaNotificarConfirmacionOrden);
				}
				
				//EVENTOS DE AUTORIZACION
				{
					CrearEventoAutorizacionMasivo.ServiceEntrada inCrearAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
					List<ObjTABLAEVENTOS> tablaEventos = new ArrayList<ObjTABLAEVENTOS>();
					List<ObjTABLADETALLE> tablaEventoDetalle = new ArrayList<ObjTABLADETALLE>();
					Long lngEvento = 0L;
					for (OrdenProceso ord : ordenes) {
						if (ord.autorizaciones > 0 && !ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)) {
							ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();
							++lngEvento;
							evento.IdUnion=lngEvento;
							evento.IdTipoEvento = "INGORD";
							evento.IdModuloSolicita = _c.ID_MODULO_ORDENES;
							evento.IdUsuarioSolicita = in.p_IdUsuario;
							evento.Fecha = fechaSistema;
							evento.IdTipoOperacion = ord.IdTipoOperacion;
							evento.IdConcepto = "INGORD";
							evento.NumeroOperacionConcepto = ord.IdOrden.longValue()+"";
							evento.FechaOperacionConcepto = Fecha.toStringDate(ord.FechaSistemaCreacion);
							evento.IdCliente = ord.IdCliente;
							evento.IdFuncionario = 0L;
							evento.IdProveedor = 0L;
							evento.IdTipoProducto = ord.IdProducto;
							evento.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
							evento.IdTipoInstrumento = ord.IdTipoInstrumento;
							evento.IdInstrumento = ord.IdInstrumento;
							evento.Nemo = ord.Nemotecnico;
							evento.Cantidad = ord.Cantidad.longValue();
							evento.Monto = ord.MontoTotalLiquidacion;
							evento.Precio = ord.PrecioOrden;
							
							if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)
									|| ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
								evento.Tasa = ord.TasaRFIF;
							}
							else if (ord.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
								evento.Tasa = ord.TasaReporte;
							}
							else if (ord.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
								evento.Tasa = ord.TasaSimultanea;
							}
							else if (ord.EsVentaCorta.equalsIgnoreCase(_c.SI)) {
								evento.Tasa = ord.TasaVentaCorta;
							}
							else {
								evento.Tasa = BigDecimal.ZERO;
							}
							evento.IdMoneda = ord.IdMonedaOrden;
							evento.Glosa = "Documento identidad: " + ord.DocumentoIdentidad 
							+ " / Nombre Persona: " + ord.NombreCompleto 
							+ " / CodPort: "	+ ord.CodigoPortafolio 
							+ " / Orden: " + ord.IdOrden.longValue();
							tablaEventos.add(evento);
							
							//JALA al 11-04-2019 llena el objDetalleEvento
							//agrega elementos de objeto que iran a nivel de cabecera del nuevo panel de detalle de las autorizaciones 
							ObjTABLADETALLE ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Nro de Orden";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.IdOrden.toString();
							tablaEventoDetalle.add(ItemEventoDetalle);

							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Instrumento";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.Nemotecnico;
							tablaEventoDetalle.add(ItemEventoDetalle);

							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Operacion";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.NombreTipoOperacion;
							tablaEventoDetalle.add(ItemEventoDetalle);

							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Operacion";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.AccionOrden;
							tablaEventoDetalle.add(ItemEventoDetalle);
							
							
							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Nombre Tipo Operacion";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.NombreTipoOperacion;
							tablaEventoDetalle.add(ItemEventoDetalle);
							
							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Cantidad";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.Cantidad.toString();
							tablaEventoDetalle.add(ItemEventoDetalle);

							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Monto Final";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.MontoOrden.toString();
							tablaEventoDetalle.add(ItemEventoDetalle);
							
							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Venta Corta";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.EsVentaCorta.equals("S")? "SI" : "NO";
							tablaEventoDetalle.add(ItemEventoDetalle);

							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Ejecutivo";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.NombreEjecutivoPortafolio;
							tablaEventoDetalle.add(ItemEventoDetalle);
							
							ItemEventoDetalle = new ObjTABLADETALLE();
							ItemEventoDetalle.IdUnion	= evento.IdUnion; 
							ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							ItemEventoDetalle.NombreCampo="Condicion de Liq";
							ItemEventoDetalle.TipoDato = "STRING";
							ItemEventoDetalle.Valor=ord.NombreCondicionDeLiquidacion;
							tablaEventoDetalle.add(ItemEventoDetalle);
							
							//agrega detalle de evento pero esta vez para mostrar en la grilla
							for (MensajeSalida msg : ord.MensajesProceso) {
								
								ItemEventoDetalle = new ObjTABLADETALLE();
								ItemEventoDetalle.IdUnion	= evento.IdUnion; 
								ItemEventoDetalle.Clasificacion = "2"; //indica que este valor ira a nivel de detalle (grilla)
/*
  								ItemEventoDetalle.NombreCampo="Grilla";
								ItemEventoDetalle.TipoDato = "STRING";
								ItemEventoDetalle.Valor = msg.id.toString() + "|" + msg.tipo + "|" + msg.descripcion;
*/
								
  								ItemEventoDetalle.NombreCampo= msg.id.toString();
								ItemEventoDetalle.TipoDato = msg.tipo;
								ItemEventoDetalle.Valor= msg.descripcion;

								tablaEventoDetalle.add(ItemEventoDetalle);
								
							}
						}
					}
					
					if (tablaEventos.size() > 0) {
						CrearEventoAutorizacionMasivo crearAutorizacionService = new CrearEventoAutorizacionMasivo();
						inCrearAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
						inCrearAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
						inCrearAutorizacion.P_IDMODULO = in.p_IdModulo;
						inCrearAutorizacion.P_SIMULAROPERACION = "N";
						if(in.p_simularOperacion != null) {
							inCrearAutorizacion.P_SIMULAROPERACION = in.p_simularOperacion;
						}
						inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";
						inCrearAutorizacion.P_TABLAEVENTOS = tablaEventos; 
						inCrearAutorizacion.P_DETALLE = tablaEventoDetalle;
						
						//enviar arreglo de campos nuevos
					
						crearAutorizacionService.logica(con, inCrearAutorizacion);
						
					}
				}
				
				//RUTEAR ORDENES
				{
					List<RutearOrdenes.Orden> ordenesRuteo = new ArrayList<RutearOrdenes.Orden>();
					for (OrdenProceso ord: ordenes) {
						if (ord.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_INGRESADA) && 
								(ord.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE)  || ord.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL))) {
							
							RutearOrdenes.Orden ordenRuteo = new RutearOrdenes.Orden();
							ordenRuteo.IdBolsaDistribucion = ord.IdBolsa;
							ordenRuteo.IdOrden = ord.IdOrden;
							ordenesRuteo.add(ordenRuteo);					
							
						}
						
						//INGRESO DE NOTIFICACIONES CANAL DISTRIBUCION MANUAL 
						if(ord.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_INGRESADA) && "MAN".equalsIgnoreCase(ord.IdTipoCanalDistribucion)){
							AgregarMensajeWebsocket.agregar(con, in.p_IdEmpresa, "AVISO", "Ingreso de Orden \nIdOrden        : "+ord.IdOrden+"\nNemotecnico: "+ord.Nemotecnico);
							AgregarMensajeWebsocket.agregar(con, in.p_IdEmpresa, "CREAR_ORDEN", "");
						}
						
					}
					if (ordenesRuteo.size() > 0) {
						RutearOrdenes_Entrada rutearOrdenesEntrada = new RutearOrdenes_Entrada();
						rutearOrdenesEntrada.p_IdEmpresa = in.p_IdEmpresa;
						rutearOrdenesEntrada.p_Ordenes = ordenesRuteo;
						RutearOrdenes rutearOrdenesService = new RutearOrdenes();
						rutearOrdenesService.logica(con, rutearOrdenesEntrada);
					}
				}
			}
			
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(95L,"PROCESANDO","Finalizando creacion de ordenes");
				}
			}
			
			//PREPARAR SALIDA ORDENES Y COBROS
			//SI ORDEN ES APV-A SE PREPARA LA CREACION DE ORDEN BONIFICACION FISCAL
			List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
			for (OrdenProceso ord : ordenes) {
				//Utils.copyPojo(ord, ordenResultado);
				ObjRESULTADO ordenResultado = copyPojoObjResultado(ord);
				
				ordenResultado.FechaOperacionOrden = ord.FechaOperacion;
				ordenResultado.FechaOperacionOriginal = _c.ORIGEN_REFACTURACION.equalsIgnoreCase(ord.GlosaRefactura) && _c.CLASIFICACION_IF.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento) ? Fecha.toStringDate(ord.FechaEmision) : "";
				ordenResultado.MontoNeto = ord.MontoOrden;
				ordenResultado.MontoLiquidacionOperacion = ord.MontoLiquidacion;
				ordenResultado.MontoLiquidacionOperacionPlazo = ord.MontoLiquidacion;
				ordenResultado.MontoTotal = ord.MontoTotalLiquidacion;
				ordenResultado.Instrumento = ord.Nemotecnico;
				ordenResultado.InstrumentoNegociacion = ord.InstrumentoNegociacion;
				ordenResultado.TotalCobros = ord.TotalCobrosLiquidacion;
				
				ordenResultado.PrecioPlazo = ord.PrecioPlazo;
				ordenResultado.MontoNetoPlazo = ord.MontoOrdenPlazo;
				ordenResultado.MontoLiquidacionPlazo = ord.MontoLiquidacionPlazo;
				ordenResultado.MontoTotalPlazo = ord.MontoTotalLiquidacionPlazo;
				ordenResultado.MontoInteresContado = ord.MontoInteresContado;
				ordenResultado.MontoInteresPlazo = ord.MontoInteresPlazo;
				
				if( ord.IdClasificacionTipoInstrumento.equals(_c.CLASIFICACION_RE)){
					ordenResultado.MontoNeto = ord.MontoOrden;
					ordenResultado.MontoNetoPlazo = ord.MontoOrdenPlazo;
					ordenResultado.MontoTotal = ord.MontoLiquidacion;
					ordenResultado.MontoTotalPlazo = ord.MontoLiquidacionPlazo;
					ordenResultado.MontoInteresContado = ord.MontoInteresContado;
					ordenResultado.MontoInteresPlazo = ord.MontoInteresPlazo;
					ordenResultado.PrecioPlazo = ord.PrecioPlazo;
				}
				//Orden Bonificacion Fiscal APV
				if (ordenResultado.CodigoPortafolio.equalsIgnoreCase("APV-A")){
					AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
					AgregarPrograma.InAgregarPrograma.Parametro	paramIdOrden = new AgregarPrograma.InAgregarPrograma.Parametro();
					AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
					paramIdEmpresa.NombreParametro 	= "IdEmpresa";
					paramIdEmpresa.Valor			= in.p_IdEmpresa.toString();
					paramsParametros.add(paramIdEmpresa);
					paramIdOrden.NombreParametro 	= "IdOrden";
					paramIdOrden.Valor			= ordenResultado.IdOrden.toString();
					paramsParametros.add(paramIdOrden);
					paramIdUsuario.NombreParametro 	= "IdUsuario";
					paramIdUsuario.Valor			= in.p_IdUsuario.toString();
					paramsParametros.add(paramIdUsuario);
				}
				ordenResultado.TipoCanalDistribucion = ord.IdTipoCanalDistribucion;
				/*
				$scope.formResumen.principalContado= respuesta.P_OUT_RESULTADO[0].MontoNeto;
				$scope.formResumen.montoLiquidacionContado=respuesta.P_OUT_RESULTADO[0].MontoTotal;
				$scope.formResumen.principalPlazo=respuesta.P_OUT_RESULTADO[0].MontoNetoPlazo;
				$scope.formResumen.montoLiquidacionPlazo=respuesta.P_OUT_RESULTADO[0].MontoTotalPlazo;*/
				
				out.P_OUT_RESULTADO.add(ordenResultado);
				
				for (CobroOrdenProceso cobro : ord.cobros) {
					ObjCOBROS cobroResultado = new ObjCOBROS();
					//Utils.copyPojo(cobro, cobroResultado);
					cobroResultado.IdInterno = cobro.IdInterno;
					cobroResultado.IdOrden = cobro.Orden.IdOrden;
					cobroResultado.CodigoCobroClasificacion = cobro.CodigoCobroClasificacion;
					cobroResultado.IdCobro = cobro.IdCobro;
					cobroResultado.IdMonedaCobro = cobro.IdMoneda;
					cobroResultado.ValorCobroPerfil = cobro.ValorCobroPerfil;
					cobroResultado.FormaCobro = cobro.FormaCobro;
					cobroResultado.MontoMinimo = cobro.MontoMinimo;
					cobroResultado.ValorCobroAplicado = cobro.ValorCobroAplicado;
					cobroResultado.MontoCobroCalculado = cobro.MontoCobroCalculado;
					cobroResultado.MontoCobroCalculadoLocal = cobro.MontoCobroCalculadoLocal;
					cobroResultado.MontoCobroCalculadoLiquidacion = cobro.MontoCobroCalculadoLiquidacion;
					out.P_OUT_COBROS.add(cobroResultado);
				}
				BigDecimal precioOrden = ord.PrecioOrden.stripTrailingZeros();

				String idBloqueaOrden = ord.IdBloqueaOrden;
				HashMap<String,Object> inParametros = new HashMap<String,Object>();
				inParametros.put("p_IdBloqueaOrden", idBloqueaOrden);
				inParametros.put("p_IdPortafolioCliente", ord.IdPortafolioCliente);
				inParametros.put("p_IdInstrumento", ord.IdInstrumento);
				inParametros.put("p_IdTipoOperacion", ord.IdTipoOperacion);
				inParametros.put("p_Cantidad", ord.Cantidad);
				inParametros.put("p_PrecioLimite", precioOrden);
				inParametros.put("p_FechaSistema", fechaSistemaLocal);
				
				HashMap<String,Class> outParametros = new HashMap<String,Class>();
				
				if(!_c.GLOSA_CARGA_MASIVA.equalsIgnoreCase(ord.Glosa) && !_c.GLOSA_ORDEN_POSTRERA.equalsIgnoreCase(ord.Glosa)) {
					try {
						logger.debug("INICIO::SQL_DELETE_BLOQUEO_ORDEN");
						HashMap<String,Object> salidaDeleteBloqueoOrden = _sql.call(con,SQL_CALL_DELETE_BLOQUEO_ORDEN,inParametros,outParametros);
						logger.debug("FIN::SQL_DELETE_BLOQUEO_ORDEN");
					}catch(SQLException sqle) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DELETE_BLOQUEO_ORDEN",valores,con,in.p_IdEmpresa));
						{
							if(in.p_BarraProgreso != null) {
								JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
								in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
							}
						}
						
						return out;
					}
				}
			}
			
			//envio bonificacion fiscal
			if(paramsParametros.size()!=0) {
				//String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
				AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
				paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
				paramsIn.p_IdModulo			= in.p_IdModulo;
				paramsIn.p_IdUsuario		= in.p_IdUsuario;
				paramsIn.p_IdTipoPrograma	= "INGRESAR_ORDEN_RV_BONIFICACION_FISCAL";
				paramsIn.p_Segundos			= 0L;
				//paramsIn.p_FechaSistema		= FechaSistema;
				paramsIn.p_FechaSistema		= fechaSistema;
				paramsIn.p_Parametros		= paramsParametros;
				AgregarPrograma AP = new AgregarPrograma();
				AP.logica(con, paramsIn);
			}
			
		} 
		catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		//El cleanup de CrearOrdenes_Proceso (GLOBAL TEMPORARY, ON COMMIT DELETE ROWS) ya no requiere DELETE explicito:
		//no esta soportado sobre esta tabla y el commit/rollback de CallServlet la limpia solo al cerrar la transaccion.
		logger.info("Termino del servicio CrearOrdenes, tiempo:" + (System.currentTimeMillis() - init));
		for(MensajeSalida mje : out.P_OUT_MENSAJES){
			if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
				out.P_OUT_ERRORESCONTADOR++;
			}
		}
		
		{
			if(out.P_OUT_ERRORESCONTADOR.compareTo(0L) >= 1L) {
				if(in.p_BarraProgreso != null) {
					JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
					in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
				}
			}
			else {
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Creacion de ordenes finalizada");
				}
			}
		}
		
		
		return out;
	}

	private boolean validaEsPaisFatcaCrs(PaisResidenciaPrincipalCliente paisResidenciaPrincipalCliente) {
		boolean esFatcaCrs = false;		
		if(paisResidenciaPrincipalCliente != null && ("S".equals(paisResidenciaPrincipalCliente.EsOcde) || "S".equals(paisResidenciaPrincipalCliente.EsPaisUs))) {
			esFatcaCrs = true;
		}
		return esFatcaCrs;
	}

	public String obtenerValorRequisitoOperativo(HashMap<String, RequisitoOperativoCrearOrden> requisitos,String IdTipoCanalIngreso,String requisitoOperativo) throws IllegalArgumentException, IllegalAccessException {
		if (requisitos.get(IdTipoCanalIngreso) == null &&  requisitos.get("0") == null) return "OK";
		RequisitoOperativoCrearOrden reqCanal = requisitos.get(IdTipoCanalIngreso);
		RequisitoOperativoCrearOrden reqTodos = requisitos.get("0");
		Field[] fields = reqCanal.getClass().getDeclaredFields();
		for (Field f: fields) {
			if (f.getName().equalsIgnoreCase(requisitoOperativo)) {
				String requisitoCanal = (String) f.get(reqCanal);
				String requisitoTodos = null;
				if (f.get(reqTodos)!= null) {
					requisitoTodos = (String) f.get(reqTodos);
				} else {
					requisitoTodos = null;
				}
				if (requisitoCanal != null){
					return requisitoCanal;
				}
				if (requisitoTodos != null){
					return requisitoTodos;
				}
				else {
					return "OK";
				}
			}
		}
		return "";
	}
	
	
	public OrdenProceso copyPojoOrdenProceso(ObjOrdenesEntrada ordenEntrada) {
		OrdenProceso ordenProceso = new OrdenProceso();
		ordenProceso.IdInterno = ordenEntrada.IdInterno;
		//ordenProceso.IdOrden = ordenEntrada.IdOrden;
		//ordenProceso.IdCliente = ordenEntrada.IdCliente;
		//ordenProceso.IdPortafolioCliente = ordenEntrada.IdPortafolioCliente;
		//ordenProceso.CodigoPortafolio = ordenEntrada.CodigoPortafolio;
		ordenProceso.IdInstrumento = ordenEntrada.IdInstrumento;
		ordenProceso.FechaOperacion = Fecha.toDateTime(ordenEntrada.FechaOperacion);
		ordenProceso.IdBolsa = ordenEntrada.IdBolsa;
		ordenProceso.IdLibroOrdenes = ordenEntrada.IdLibroOrdenes;
		ordenProceso.IdTipoOperacion = ordenEntrada.IdTipoOperacion;
		ordenProceso.IdTipoCanalIngreso = ordenEntrada.IdTipoCanalIngreso;
		ordenProceso.IdCondicionDeLiquidacion = ordenEntrada.IdCondicionDeLiquidacion;
		ordenProceso.IdPorcentajeVisibilidad = ordenEntrada.IdPorcentajeVisibilidad;
		//ordenProceso.IdTipoVigenciaOrden = ordenEntrada.IdTipoVigenciaOrden;
		ordenProceso.IdBoveda = ordenEntrada.IdBoveda;
		ordenProceso.IdFormaCustodia = ordenEntrada.IdFormaCustodia;
		//ordenProceso.IdEmpleadoPortafolio = ordenEntrada.IdEmpleadoPortafolio;
		ordenProceso.IdTipoPrecio = ordenEntrada.IdTipoPrecio;
		//ordenProceso.IdTipoInstrumento = ordenEntrada.IdTipoInstrumento;
		ordenProceso.IdTipoCanalDistribucion = ordenEntrada.IdTipoCanalDistribucion;
		//ordenProceso.IdEmpleadoDigitador = ordenEntrada.IdEmpleadoDigitador;
		//ordenProceso.IdInstrumentoCustodia = ordenEntrada.IdInstrumentoCustodia;
		//ordenProceso.IdEstadoInstrumento = ordenEntrada.IdEstadoInstrumento;
		//ordenProceso.IdInstrumentoReal = ordenEntrada.IdInstrumentoReal;
		//ordenProceso.NombreTipoInstrumento = ordenEntrada.NombreTipoInstrumento;
		//ordenProceso.TamanoCorte = ordenEntrada.TamanoCorte;
		//ordenProceso.IdMonedaInstrumento = ordenEntrada.IdMonedaInstrumento;
		//ordenProceso.NombreClasificacionTipoInstrumento = ordenEntrada.NombreClasificacionTipoInstrumento;
		//ordenProceso.TamanoContrato = ordenEntrada.TamanoContrato;
		//ordenProceso.IdTipoOperacionReal = ordenEntrada.IdTipoOperacionReal;
		ordenProceso.EspecificaBolsa = ordenEntrada.EspecificaBolsa;
		ordenProceso.EsVentaCorta = ordenEntrada.EsVentaCorta;
		//ordenProceso.NombreTipoOperacion = ordenEntrada.NombreTipoOperacion;
		//ordenProceso.AccionOrden = ordenEntrada.AccionOrden;
		//ordenProceso.IdBolsaReal = ordenEntrada.IdBolsaReal;
		ordenProceso.FechaEmision = Fecha.toDate(ordenEntrada.FechaEmision);
		//ordenProceso.RuteoGeneraComprobanteFacturacionAutomatico = ordenEntrada.RuteoGeneraComprobanteFacturacionAutomatico;
		//ordenProceso.IndicadorFacturacionInmediata = ordenEntrada.IndicadorFacturacionInmediata;
		ordenProceso.IdInstrumentoSubyacente = ordenEntrada.IdInstrumentoSubyacente;
		ordenProceso.IdPersonaAutorizadoDarOrdenes = ordenEntrada.IdPersonaAutorizadoDarOrdenes;
		//ordenProceso.RuteoGeneraMovimientoFacturacionAutomatico = ordenEntrada.RuteoGeneraMovimientoFacturacionAutomatico;
		//ordenProceso.IdCondicionDeLiquidacionContadoReal = ordenEntrada.IdCondicionDeLiquidacionContadoReal;
		//ordenProceso.NombreCondicionDeLiquidacion = ordenEntrada.NombreCondicionDeLiquidacion;
		//ordenProceso.IdTipoVigenciaReal = ordenEntrada.IdTipoVigenciaReal;
		//ordenProceso.NombreTipoVigenciaOrden = ordenEntrada.NombreTipoVigenciaOrden;
		//ordenProceso.IdBovedaReal = ordenEntrada.IdBovedaReal;
		//ordenProceso.IdFormaCustodiaReal = ordenEntrada.IdFormaCustodiaReal;
		//ordenProceso.IdPorcentajeVisibilidadReal = ordenEntrada.IdPorcentajeVisibilidadReal;
		//ordenProceso.IdTipoPrecioReal = ordenEntrada.IdTipoPrecioReal;
		//ordenProceso.NombreTipoPrecio = ordenEntrada.NombreTipoPrecio;
		//ordenProceso.IdTipoCanalIngresoReal = ordenEntrada.IdTipoCanalIngresoReal;
		//ordenProceso.DiasLiquidacionContado = ordenEntrada.DiasLiquidacionContado;
		//ordenProceso.IdTipoReajusteReal = ordenEntrada.IdTipoReajusteReal;
		//ordenProceso.InstrumentoSubyacente = ordenEntrada.InstrumentoSubyacente;
		//ordenProceso.InstrumentoPermiteVentaCorta = ordenEntrada.InstrumentoPermiteVentaCorta;
		//ordenProceso.InstrumentoPermiteSimultanea = ordenEntrada.InstrumentoPermiteSimultanea;
		//ordenProceso.IdTipoCanalDistribucionReal = ordenEntrada.IdTipoCanalDistribucionReal;
		//ordenProceso.MontoMaximoDistribucionOrden = ordenEntrada.MontoMaximoDistribucionOrden;
		//ordenProceso.ValidaHorario = ordenEntrada.ValidaHorario;
		//ordenProceso.AplazaOrdenPorHorarioNegociacion = ordenEntrada.AplazaOrdenPorHorarioNegociacion;
		//ordenProceso.HorarioPreMarketInicio = ordenEntrada.HorarioPreMarketInicio;
		//ordenProceso.HorarioPreMarketFin = ordenEntrada.HorarioPreMarketFin;
		//ordenProceso.HorarioMarketInicio = ordenEntrada.HorarioMarketInicio;
		//ordenProceso.HorarioMarketFin = ordenEntrada.HorarioMarketFin;
		//ordenProceso.IdEstadoModulo = ordenEntrada.IdEstadoModulo;
		//ordenProceso.NombreModulo = ordenEntrada.NombreModulo;
		//ordenProceso.AplazaOrdenPorModuloCerrado = ordenEntrada.AplazaOrdenPorModuloCerrado;
		//ordenProceso.FechaLiquidacion = ordenEntrada.FechaLiquidacion;
		//ordenProceso.IdClasificacionTipoInstrumento = ordenEntrada.IdClasificacionTipoInstrumento;
		ordenProceso.IdCorredorCorresponsal = ordenEntrada.IdCorredorCorresponsal;
		//ordenProceso.IdProducto = ordenEntrada.IdProducto;
		//ordenProceso.IdGrupoCobro = ordenEntrada.IdGrupoCobro;
		ordenProceso.ValorRescate = ordenEntrada.ValorRescate;
		ordenProceso.Cantidad = ordenEntrada.Cantidad;
		//ordenProceso.MontoOrden = ordenEntrada.MontoOrden;
		//ordenProceso.PrecioOrden = ordenEntrada.PrecioOrden;
		ordenProceso.EsFungible = ordenEntrada.EsFungible;
		//ordenProceso.Nemotecnico = ordenEntrada.Nemotecnico;
		//ordenProceso.PrecioMercado = ordenEntrada.PrecioMercado;
		ordenProceso.IdMonedaOrden = ordenEntrada.IdMonedaOrden;
		ordenProceso.TipoCambio = ordenEntrada.TipoCambio;
		ordenProceso.FechaRecepcion = Fecha.toDateTime(ordenEntrada.FechaRecepcion);
		ordenProceso.IdMonedaLiquidacionOrden = ordenEntrada.IdMonedaLiquidacionOrden;
		//ordenProceso.MontoLiquidacion = ordenEntrada.MontoLiquidacion;
		//ordenProceso.MontoLocal = ordenEntrada.MontoLocal;
		ordenProceso.IdEmisor = ordenEntrada.IdEmisor;
		ordenProceso.IdTipoReajuste = ordenEntrada.IdTipoReajuste;
		//ordenProceso.IdEstadoOrden = ordenEntrada.IdEstadoOrden;
		//ordenProceso.FechaAceptacion = ordenEntrada.FechaAceptacion;
		//ordenProceso.FechaAutorizacion = ordenEntrada.FechaAutorizacion;
		//ordenProceso.IdEmpresa = ordenEntrada.IdEmpresa;
		//ordenProceso.CodigoClaseOrden = ordenEntrada.CodigoClaseOrden;
		//ordenProceso.IdPortafolioReal = ordenEntrada.IdPortafolioReal;
		//ordenProceso.IdEstadoPortafolio = ordenEntrada.IdEstadoPortafolio;
		//ordenProceso.IdPersonaCliente = ordenEntrada.IdPersonaCliente;
		ordenProceso.FechaVigencia = (ordenEntrada.FechaVigencia != null ? Fecha.toDate(ordenEntrada.FechaVigencia) : null);
		//ordenProceso.IdEstadoPersona = ordenEntrada.IdEstadoPersona;
		//ordenProceso.IdClienteReal = ordenEntrada.IdClienteReal;
		//ordenProceso.IdEstadoCliente = ordenEntrada.IdEstadoCliente;
		//ordenProceso.DocumentoIdentidad = ordenEntrada.DocumentoIdentidad;
		//ordenProceso.NombreCompleto = ordenEntrada.NombreCompleto;
		//ordenProceso.IdPerfilCobros = ordenEntrada.IdPerfilCobros;
		//ordenProceso.TipoDocumentoIdentidad = ordenEntrada.TipoDocumentoIdentidad;
		//ordenProceso.NombreEjecutivoPortafolio = ordenEntrada.NombreEjecutivoPortafolio;
		//ordenProceso.IdEstadoEmpleadoEjecutivoPortafolio = ordenEntrada.IdEstadoEmpleadoEjecutivoPortafolio;
		//ordenProceso.IndicadorInstrumentoAcreditado = ordenEntrada.IndicadorInstrumentoAcreditado;
		ordenProceso.InstrumentoAcreditado = ordenEntrada.InstrumentoAcreditado;
		ordenProceso.IdOrdenReferencia = ordenEntrada.IdOrdenReferencia;
		//ordenProceso.ValidaSaldoCustodia = ordenEntrada.ValidaSaldoCustodia;
		//ordenProceso.CumpleHorario = ordenEntrada.CumpleHorario;
		//ordenProceso.MontoPrimaVentaCorta = ordenEntrada.MontoPrimaVentaCorta;
		//ordenProceso.errores = ordenEntrada.errores;
		//ordenProceso.advertencias = ordenEntrada.advertencias;
		//ordenProceso.autorizaciones = ordenEntrada.autorizaciones;
		//ordenProceso.ok = ordenEntrada.ok;
		ordenProceso.TasaReporte = ordenEntrada.TasaReporte;
		ordenProceso.PlazoReporte = ordenEntrada.PlazoReporte;
		ordenProceso.BaseTasa = ordenEntrada.BaseTasa;
		//ordenProceso.cobros = new ArrayList<CobroOrdenProceso>(ordenEntrada.cobros);
		//ordenProceso.cantidadCobrosPortafolio = ordenEntrada.cantidadCobrosPortafolio;
		//ordenProceso.IdEstadoEmpleadoDigitador = ordenEntrada.IdEstadoEmpleadoDigitador;
		//ordenProceso.PlantillaEmpleadoPermiteRuteo = ordenEntrada.PlantillaEmpleadoPermiteRuteo;
		//ordenProceso.PlantillaEmpleadoProductoHabilitado = ordenEntrada.PlantillaEmpleadoProductoHabilitado;
		//ordenProceso.PlantillaEmpleadoMaximoDescuentoComision = ordenEntrada.PlantillaEmpleadoMaximoDescuentoComision;
		//ordenProceso.PlantillaEmpleadoMontoMaximoOperacion = ordenEntrada.PlantillaEmpleadoMontoMaximoOperacion;
		//ordenProceso.TotalCobrosLiquidacion = ordenEntrada.TotalCobrosLiquidacion;
		//ordenProceso.TotalCobrosLiquidacionPlazo = ordenEntrada.TotalCobrosLiquidacionPlazo;
		//ordenProceso.TotalCobrosLocal = ordenEntrada.TotalCobrosLocal;
		//ordenProceso.MontoTotalLiquidacion = ordenEntrada.MontoTotalLiquidacion;
		//ordenProceso.MontoTotalLiquidacionPlazo = ordenEntrada.MontoTotalLiquidacionPlazo;
		//ordenProceso.TotalMontoPortafolio = ordenEntrada.TotalMontoPortafolio;
		//ordenProceso.SaldoCajaPortafolio = ordenEntrada.SaldoCajaPortafolio;
		//ordenProceso.LineaSobregiroPortafolio = ordenEntrada.LineaSobregiroPortafolio;
		//ordenProceso.TotalCantidadCustodiaPortafolio = ordenEntrada.TotalCantidadCustodiaPortafolio;
		//ordenProceso.TotalValorRescateCustodiaPortafolio = ordenEntrada.TotalValorRescateCustodiaPortafolio;
		//ordenProceso.SaldoCantidadCustodiaPortafolio = ordenEntrada.SaldoCantidadCustodiaPortafolio;
		//ordenProceso.SaldoValorRescateCustodiaPortafolio = ordenEntrada.SaldoValorRescateCustodiaPortafolio;
		//ordenProceso.SaldoCantidadVentaDescubiertaCustodiaPortafolio = ordenEntrada.SaldoCantidadVentaDescubiertaCustodiaPortafolio;
		ordenProceso.Glosa = ordenEntrada.Glosa;
		ordenProceso.GlosaRefactura = ordenEntrada.GlosaRefactura;
		ordenProceso.PlazoVentaCorta = ordenEntrada.PlazoVentaCorta;
		ordenProceso.PlazoSimultanea = ordenEntrada.PlazoSimultanea;
		ordenProceso.TasaVentaCorta = ordenEntrada.TasaVentaCorta;
		ordenProceso.TasaSimultanea = ordenEntrada.TasaSimultanea;
		ordenProceso.TasaRFIF = ordenEntrada.TasaRFIF;
		ordenProceso.PlazoIF = ordenEntrada.PlazoIF;
		//ordenProceso.IdSucursalEmpleadoDigitador = ordenEntrada.IdSucursalEmpleadoDigitador;
		//ordenProceso.IdSucursalEmpleadoPortafolio = ordenEntrada.IdSucursalEmpleadoPortafolio;
		//ordenProceso.FechaVencimientoVentaCorta = ordenEntrada.FechaVencimientoVentaCorta;
		//ordenProceso.FechaVencimientoSimultanea = ordenEntrada.FechaVencimientoSimultanea;
		//ordenProceso.FechaVencimientoIF = ordenEntrada.FechaVencimientoIF;
		//ordenProceso.PortafolioProductoHabilitado = ordenEntrada.PortafolioProductoHabilitado;
		//ordenProceso.PrecioPlazo = ordenEntrada.PrecioPlazo;
		//ordenProceso.MontoOrdenPlazo = ordenEntrada.MontoOrdenPlazo;
		//ordenProceso.FechaVencimientoReporte = ordenEntrada.FechaVencimientoReporte;
		//ordenProceso.MontoLiquidacionPlazo = ordenEntrada.MontoLiquidacionPlazo;
		//ordenProceso.MontoLocalPlazo = ordenEntrada.MontoLocalPlazo;
		ordenProceso.IdTipoLibroOrden = ordenEntrada.IdTipoLibroOrden;
		ordenProceso.IdCuentaCustodioPortafolio = ordenEntrada.IdCuentaCustodioPortafolio;
		ordenProceso.EsPorMonto = ordenEntrada.EsPorMonto;
		ordenProceso.EsOrdenEspecial = ordenEntrada.EsOrdenEspecial;
		//ordenProceso.MontoTotalLocal = ordenEntrada.MontoTotalLocal;
		//ordenProceso.ValidaSaldoCaja = ordenEntrada.ValidaSaldoCaja;
		ordenProceso.PagoCobroCuentaInversion = ordenEntrada.PagoCobroCuentaInversion;
		ordenProceso.IdPreferenciaPagoPortafolio = ordenEntrada.IdPreferenciaPagoPortafolio;
		ordenProceso.TipoInteres = ordenEntrada.TipoInteres;
		//ordenProceso.CodigoProcedenciaOrden = ordenEntrada.CodigoProcedenciaOrden;
		//ordenProceso.Folio = ordenEntrada.Folio;
		//ordenProceso.FechaSistemaCreacion = ordenEntrada.FechaSistemaCreacion;
		ordenProceso.TasaPromedioReporte = ordenEntrada.TasaPromedioReporte;
		ordenProceso.TasaReportanteReferencia = ordenEntrada.TasaReportanteReferencia;
		ordenProceso.NemotecnicoSVS = ordenEntrada.NemotecnicoSVS;
		ordenProceso.IdSistemaBursatil = ordenEntrada.IdSistemaBursatil;
		//ordenProceso.ExisteOrdenMismaOperacionInstrumento = ordenEntrada.ExisteOrdenMismaOperacionInstrumento;
		//ordenProceso.IdTipoCliente = ordenEntrada.IdTipoCliente;
		//ordenProceso.IdPersonaBroker = ordenEntrada.IdPersonaBroker;
		//ordenProceso.EsBroker = ordenEntrada.EsBroker;
		//ordenProceso.ModalidadSMV = ordenEntrada.ModalidadSMV;
		//ordenProceso.MercadoSMV = ordenEntrada.MercadoSMV;
		ordenProceso.FormadorMercado = ordenEntrada.FormadorMercado;
		//ordenProceso.IdEstadoInterno = ordenEntrada.IdEstadoInterno;
		//ordenProceso.IdEstadoLimite = ordenEntrada.IdEstadoLimite;
		//ordenProceso.MontoAllIn = ordenEntrada.MontoAllIn;
		ordenProceso.PorcentajeAllIn = ordenEntrada.PorcentajeAllIn;
		//ordenProceso.FechaVencimientoInstrumento = ordenEntrada.FechaVencimientoInstrumento;
		ordenProceso.MontoInteresContado = ordenEntrada.MontoInteresContado;
		ordenProceso.MontoInteresPlazo = ordenEntrada.MontoInteresPlazo;
		//ordenProceso.FechaVencimientoInstrumentoSubyacente = ordenEntrada.FechaVencimientoInstrumentoSubyacente;
		ordenProceso.EsClienteFinal = ordenEntrada.EsClienteFinal;
		//ordenProceso.MontoCoberturaExigibilidadEntidad = ordenEntrada.MontoCoberturaExigibilidadEntidad;
		//ordenProceso.MontoCoberturaExigibilidadCorredora = ordenEntrada.MontoCoberturaExigibilidadCorredora;
		//ordenProceso.CumpleParaVentaDescubierta = ordenEntrada.CumpleParaVentaDescubierta;
		ordenProceso.FechaConfirmacionPlazo = Fecha.toDate(ordenEntrada.FechaConfirmacionPlazo);
		ordenProceso.TipoLiquidacionFactura = (ordenEntrada.TipoLiquidacionFactura != null ? ordenEntrada.TipoLiquidacionFactura : "" ); 
		return ordenProceso;
	}
	
	private CalcularMontoYCobrosOrden.Cobro_Fijo copyPojoCalcularMontoYCobrosOrdenCobroFijo(Cobro_Fijo CobrosFijo) {
		CalcularMontoYCobrosOrden.Cobro_Fijo cobroEntrada = new CalcularMontoYCobrosOrden.Cobro_Fijo();
		cobroEntrada.IdInterno = CobrosFijo.IdInterno;
		cobroEntrada.IdInternoCobro = CobrosFijo.IdInternoCobro;
		cobroEntrada.IdPortafolio = CobrosFijo.IdPortafolio;
		cobroEntrada.IdBolsa = CobrosFijo.IdBolsa;
		cobroEntrada.IdTipoProducto = CobrosFijo.IdTipoProducto;
		cobroEntrada.IdTipoOperacion = CobrosFijo.IdTipoOperacion;
		cobroEntrada.IdTipoCanalIngreso = CobrosFijo.IdTipoCanalIngreso;
		cobroEntrada.IdCobro = CobrosFijo.IdCobro;
		cobroEntrada.ValorCobro = CobrosFijo.ValorCobro;
		cobroEntrada.FormaCobro = CobrosFijo.FormaCobro;
		cobroEntrada.ValorTopeMinimoOrden = CobrosFijo.ValorTopeMinimoOrden;
		cobroEntrada.IdMoneda = CobrosFijo.IdMoneda;
		cobroEntrada.CodigoCobroClasificacion = CobrosFijo.CodigoCobroClasificacion;
		cobroEntrada.PermiteCambiarValor = CobrosFijo.PermiteCambiarValor;
		return cobroEntrada;
	}
	
	private CobroOrdenProceso copyPojoCobroOrdenProceso(CalcularMontoYCobrosOrden.Cobro_Salida cobroSalida) {
		CobroOrdenProceso cob = new CobroOrdenProceso();
		cob.CodigoCobroClasificacion = cobroSalida.CodigoCobroClasificacion;
		cob.CodigoCobroClasificacionBase = cobroSalida.CodigoCobroClasificacionBase;
		cob.FormaCobro = cobroSalida.FormaCobro;
		cob.IdCobro = cobroSalida.IdCobro;
		cob.IdInterno = cobroSalida.IdInterno;
		//cob.IdMoneda = cobroSalida.IdMoneda;
		//cob.Orden = cobroSalida.Orden;
		cob.IdOrigenCobro = cobroSalida.IdOrigenCobro;
		cob.MontoMinimo = cobroSalida.MontoMinimo;
		cob.NombreCobro = cobroSalida.NombreCobro;
		cob.PermiteCambiarValor = cobroSalida.PermiteCambiarValor;
		cob.ValorCobroPerfil = cobroSalida.ValorCobroPerfil;
		cob.ValorCobroAplicado = cobroSalida.ValorCobroAplicado;
		cob.ValorCobroFinal = cobroSalida.ValorCobroFinal;
		cob.MontoCobroCalculado = cobroSalida.MontoCobroCalculado;
		cob.MontoCobroCalculadoLocal = cobroSalida.MontoCobroCalculadoLocal;
		cob.MontoCobroCalculadoLiquidacion = cobroSalida.MontoCobroCalculadoLiquidacion;
		cob.IdInternoCobro = cobroSalida.IdInternoCobro;
		cob.CheckCobroMonedaOperacion = cobroSalida.CheckCobroMonedaOperacion;
		cob.IdMonedaCobroOtraMoneda = cobroSalida.IdMonedaCobroOtraMoneda;
		cob.MontoFijo_MontoCobro = cobroSalida.MontoFijo_MontoCobro;
		cob.MontoFijo_CheckMontoOperacionMenor = cobroSalida.MontoFijo_CheckMontoOperacionMenor;
		cob.MontoFijo_ValorMontoOperacionMenor = cobroSalida.MontoFijo_ValorMontoOperacionMenor;
		cob.MontoFijo_CheckSiNo = cobroSalida.MontoFijo_CheckSiNo;
		cob.MontoFijo_ValorMontoSiNo = cobroSalida.MontoFijo_ValorMontoSiNo;
		cob.MontoFijoPorUnidad_MontoCobro = cobroSalida.MontoFijoPorUnidad_MontoCobro;
		cob.MontoFijoPorUnidad_AllInCheckValor = cobroSalida.MontoFijoPorUnidad_AllInCheckValor;
		cob.PorcentualFijo_ValorCobro = cobroSalida.PorcentualFijo_ValorCobro;
		cob.PorcentualFijo_CheckSobreMonto = cobroSalida.PorcentualFijo_CheckSobreMonto;
		cob.PorcentualFijo_CheckSobreCantidad = cobroSalida.PorcentualFijo_CheckSobreCantidad;
		cob.PorcentualFijo_CheckMayorEntreDos = cobroSalida.PorcentualFijo_CheckMayorEntreDos;
		cob.PorcentualFijo_CheckMenorEntreDos = cobroSalida.PorcentualFijo_CheckMenorEntreDos;
		cob.PorcentualFijo_CheckMontoMinimo = cobroSalida.PorcentualFijo_CheckMontoMinimo;
		cob.PorcentualFijo_ValorMontoMinimo = cobroSalida.PorcentualFijo_ValorMontoMinimo;
		cob.PorcentualFijo_CheckMontoMaximo = cobroSalida.PorcentualFijo_CheckMontoMaximo;
		cob.PorcentualFijo_ValorMontoMaximo = cobroSalida.PorcentualFijo_ValorMontoMaximo;
		cob.PorcentualFijo_IdMonedaMinimoMaximo = cobroSalida.PorcentualFijo_IdMonedaMinimoMaximo;
		cob.PorcentualTasaPlazo_ValorCobro = cobroSalida.PorcentualTasaPlazo_ValorCobro;
		cob.Otros_CheckAfectoImpuesto = cobroSalida.Otros_CheckAfectoImpuesto;
		cob.Otros_CheckPermiteAumentarValor = cobroSalida.Otros_CheckPermiteAumentarValor;
		cob.Otros_CheckPermiteDisminuirValor = cobroSalida.Otros_CheckPermiteDisminuirValor;
		cob.Otros_CheckOperacionContado = cobroSalida.Otros_CheckOperacionContado;
		cob.Otros_CheckOperacionPlazo = cobroSalida.Otros_CheckOperacionPlazo;
		cob.Otros_CheckAllIn = cobroSalida.Otros_CheckAllIn;
		cob.Otros_CheckInteresCompuesto = cobroSalida.Otros_CheckInteresCompuesto;
		cob.Otros_CheckInteresSimple = cobroSalida.Otros_CheckInteresSimple;
		cob.RangoCobros = new ArrayList<DefinicionRangoCobro>(cobroSalida.RangoCobros);
		cob.PorcentualRango_IdMonedaRangoMonto = cobroSalida.PorcentualRango_IdMonedaRangoMonto;
		cob.PorcentualRango_IdTipoRangoCobro = cobroSalida.PorcentualRango_IdTipoRangoCobro;
		return cob;
	}
	
	private Orden copyPojoOrden(OrdenProceso ord) {
		Orden o1 = new Orden();
		o1.BaseTasa = ord.BaseTasa;
		//o1.Bloqueado = ord.Bloqueado;
		o1.Cantidad = ord.Cantidad;
		o1.CodigoClaseOrden = ord.CodigoClaseOrden;
		o1.CodigoModoFacturacionPortafolio = ord.CodigoModoFacturacionPortafolio;
		o1.CodigoProcedenciaOrden = ord.CodigoProcedenciaOrden;
		o1.DocumentoIdentidad = ord.DocumentoIdentidad;
		o1.EsVentaCorta = ord.EsVentaCorta;
		o1.EspecificaBolsa = ord.EspecificaBolsa;
		o1.EsClienteFinal = ord.EsClienteFinal;
		//o1.EsConfirmada = ord.EsConfirmada;
		o1.EsFungible = ord.EsFungible;
		o1.EsPorMonto = ord.EsPorMonto;
		o1.EsOrdenEspecial = ord.EsOrdenEspecial;
		o1.EsBroker = ord.EsBroker;
		o1.FechaRecepcion = ord.FechaRecepcion;
		//o1.FechaIngresoRegistro = ord.FechaIngresoRegistro;
		//o1.FechaIngresoRegistroCorta = ord.FechaIngresoRegistroCorta;
		//o1.FechaSolicitudAnulacion = ord.FechaSolicitudAnulacion;
		//o1.FechaAnulacion = ord.FechaAnulacion;
		o1.FechaOperacion = ord.FechaOperacion;
		o1.FechaVigencia = ord.FechaVigencia;
		//o1.FechaVencimiento = ord.FechaVencimiento;
		o1.FechaEmision = ord.FechaEmision;
		o1.FechaLiquidacion = ord.FechaLiquidacion;
		o1.FechaAceptacion = ord.FechaAceptacion;
		o1.FechaAutorizacion = ord.FechaAutorizacion;
		//o1.FechaOferta = ord.FechaOferta;
		//o1.FechaModificacion = ord.FechaModificacion;
		o1.FechaSistemaCreacion = ord.FechaSistemaCreacion;
		o1.Folio = ord.Folio;
		o1.FormadorMercado = ord.FormadorMercado;
		o1.Glosa = ord.Glosa;
		o1.IdBoveda = ord.IdBoveda;
		o1.IdBolsa = ord.IdBolsa;
		//o1.IdBolsaDistribucion = ord.IdBolsaDistribucion;
		//o1.IdCarga = ord.IdCarga;
		o1.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
		o1.IdCliente = ord.IdCliente;
		o1.IdCondicionDeLiquidacion = ord.IdCondicionDeLiquidacion;
		o1.IdEmisor = ord.IdEmisor;
		o1.IdEmpresa = ord.IdEmpresa;
		o1.IdEstadoOrden = ord.IdEstadoOrden;
		o1.IdEstadoInterno = ord.IdEstadoInterno;
		o1.IdEstadoLimite = ord.IdEstadoLimite;
		o1.IdGrupoCobro = ord.IdGrupoCobro;
		o1.IdInstrumento = ord.IdInstrumento;
		o1.IdLibroOrdenes = ord.IdLibroOrdenes;
		//o1.IdMedioDePago = ord.IdMedioDePago;
		o1.IdMonedaOrden = ord.IdMonedaOrden;
		o1.IdOrden = ord.IdOrden;
		o1.IdPerfilCobros = ord.IdPerfilCobros;
		o1.IdPortafolioCliente = ord.IdPortafolioCliente;
		o1.IdProducto = ord.IdProducto;
		o1.IdTipoCanalDistribucion = ord.IdTipoCanalDistribucion;
		o1.IdTipoCanalIngreso = ord.IdTipoCanalIngreso;
		o1.IdTipoCliente = ord.IdTipoCliente;
		o1.IdTipoInstrumento = ord.IdTipoInstrumento;
		o1.IdTipoOperacion = ord.IdTipoOperacion;
		o1.IdTipoReajuste = ord.IdTipoReajuste;
		o1.IdInstrumentoSubyacente = ord.IdInstrumentoSubyacente;
		o1.IdTipoPrecio = ord.IdTipoPrecio;
		//o1.IdTipoTasa = ord.IdTipoTasa;
		o1.IdPorcentajeVisibilidad = ord.IdPorcentajeVisibilidad;
		o1.IdTipoVigenciaOrden = ord.IdTipoVigenciaOrden;
		//o1.IdTipoCustodia = ord.IdTipoCustodia;
		o1.IdFormaCustodia = ord.IdFormaCustodia;
		o1.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden;
		o1.IdEmpleadoPortafolio = ord.IdEmpleadoPortafolio;
		o1.IdEmpleadoDigitador = ord.IdEmpleadoDigitador;
		o1.IdCorredorCorresponsal = ord.IdCorredorCorresponsal;
		o1.IdPersonaAutorizadoDarOrdenes = ord.IdPersonaAutorizadoDarOrdenes;
		o1.IdPersonaBroker = ord.IdPersonaBroker;
		o1.IdSucursalEmpleadoPortafolio = ord.IdSucursalEmpleadoPortafolio;
		o1.IdSucursalEmpleadoDigitador = ord.IdSucursalEmpleadoDigitador;
		o1.IdCuentaCustodioPortafolio = ord.IdCuentaCustodioPortafolio;
		//o1.IdUsuarioReserva = ord.IdUsuarioReserva;
		o1.IdSistemaBursatil = ord.IdSistemaBursatil;
		//o1.IdUsuarioBloqueo = ord.IdUsuarioBloqueo;
		o1.IndicadorFacturacionInmediata = ord.IndicadorFacturacionInmediata;
		o1.MercadoSMV = ord.MercadoSMV;
		o1.ModalidadSMV = ord.ModalidadSMV;
		o1.IdTipoLiquidacionComisionFactura = ord.TipoLiquidacionFactura;
		o1.MontoLiquidacion = ord.MontoLiquidacion;
		o1.MontoLiquidacionPlazo = ord.MontoLiquidacionPlazo;
		o1.MontoLocal = ord.MontoLocal;
		o1.MontoLocalPlazo = ord.MontoLocalPlazo;
		o1.MontoInteresContado = ord.MontoInteresContado;
		o1.MontoInteresPlazo = ord.MontoInteresPlazo;
		o1.MontoOrden = ord.MontoOrden;
		o1.MontoOrdenPlazo = ord.MontoOrdenPlazo;
		o1.MontoPrimaVentaCorta = ord.MontoPrimaVentaCorta;
		//o1.MotivoBloqueo = ord.MotivoBloqueo;
		//o1.MotivoReserva = ord.MotivoReserva;
		o1.Nemotecnico = ord.Nemotecnico;
		o1.NemotecnicoSVS = ord.NemotecnicoSVS;
		o1.NombreCompleto = ord.NombreCompleto;
		//o1.OrdenVigente = ord.OrdenVigente;
		o1.PrecioMercado = ord.PrecioMercado;
		o1.PrecioOrden = ord.PrecioOrden;
		o1.PlazoIF = ord.PlazoIF;
		o1.PlazoSimultanea = ord.PlazoSimultanea;
		o1.PlazoVentaCorta = ord.PlazoVentaCorta;
		o1.PlazoReporte = ord.PlazoReporte;
		o1.PrecioPlazo = ord.PrecioPlazo;
		//o1.PorcentajeCobros = ord.PorcentajeCobros;
		//o1.Reservado = ord.Reservado;
		o1.RuteoGeneraMovimientoFacturacionAutomatico = ord.RuteoGeneraMovimientoFacturacionAutomatico;
		o1.RuteoGeneraComprobanteFacturacionAutomatico = ord.RuteoGeneraComprobanteFacturacionAutomatico;
		//o1.SecOrdenMigracion = ord.SecOrdenMigracion;
		o1.ValorRescate = ord.ValorRescate;
		//o1.ValorCaptacion = ord.ValorCaptacion;
		o1.TipoCambio = ord.TipoCambio;
		o1.TasaVentaCorta = ord.TasaVentaCorta;
		o1.TasaSimultanea = ord.TasaSimultanea;
		o1.TasaRFIF = ord.TasaRFIF;
		o1.TasaReporte = ord.TasaReporte;
		o1.TasaPromedioReporte = ord.TasaPromedioReporte;
		o1.TipoInteres = ord.TipoInteres;
		o1.TasaReportanteReferencia = ord.TasaReportanteReferencia;
		o1.IdOrdenReferencia = ord.IdOrdenReferencia;
		o1.FechaConfirmacionPlazo = ord.FechaConfirmacionPlazo;
		o1.FechaConfirmacion = ord.FechaConfirmacion;
		o1.IdEstadoConfirmacion = ord.IdEstadoConfirmacion;
		o1.GeneraSpot = ord.GeneraSpot;
		o1.IdInstrumentoNegociacion = ord.IdInstrumentoNegociacion;
		return o1;
	}
	
	private CobroOrden copyPojoCobroOrden(CobroOrdenProceso cob) {
		CobroOrden cobroFinal = new CobroOrden();
		//cobroFinal.IdCobroOrden = cob.IdCobroOrden;
		//cobroFinal.IdGrupoCobro = cob.IdGrupoCobro;
		//cobroFinal.IdOrden = cob.IdOrden;
		cobroFinal.IdCobro = cob.IdCobro;
		cobroFinal.ValorCobroPerfil = cob.ValorCobroPerfil;
		cobroFinal.ValorCobroFinal = cob.ValorCobroFinal;
		cobroFinal.ValorCobroAplicado = cob.ValorCobroAplicado;
		cobroFinal.MontoCobroCalculado = cob.MontoCobroCalculado;
		cobroFinal.FormaCobro = cob.FormaCobro;
		//cobroFinal.EsComprobante = cob.EsComprobante;
		//cobroFinal.FechaIngresoCobro = cob.FechaIngresoCobro;
		cobroFinal.MontoCobroCalculadoLocal = cob.MontoCobroCalculadoLocal;
		cobroFinal.MontoCobroCalculadoLiquidacion = cob.MontoCobroCalculadoLiquidacion;
		cobroFinal.MontoMinimo = cob.MontoMinimo;
		cobroFinal.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
		cobroFinal.CodigoCobroClasificacionBase = cob.CodigoCobroClasificacionBase;
		cobroFinal.IdMoneda = cob.IdMoneda;
		cobroFinal.IdOrigenCobro = cob.IdOrigenCobro;
		//cobroFinal.IdAsociacion = cob.IdAsociacion;
		cobroFinal.PermiteCambiarValor = cob.PermiteCambiarValor;
		cobroFinal.CheckCobroMonedaOperacion = cob.CheckCobroMonedaOperacion;
		cobroFinal.IdMonedaCobroOtraMoneda = cob.IdMonedaCobroOtraMoneda;
		cobroFinal.MontoFijo_MontoCobro = cob.MontoFijo_MontoCobro;
		cobroFinal.MontoFijo_CheckMontoOperacionMenor = cob.MontoFijo_CheckMontoOperacionMenor;
		cobroFinal.MontoFijo_ValorMontoOperacionMenor = cob.MontoFijo_ValorMontoOperacionMenor;
		cobroFinal.MontoFijo_CheckSiNo = cob.MontoFijo_CheckSiNo;
		cobroFinal.MontoFijo_ValorMontoSiNo = cob.MontoFijo_ValorMontoSiNo;
		cobroFinal.MontoFijoPorUnidad_MontoCobro = cob.MontoFijoPorUnidad_MontoCobro;
		cobroFinal.MontoFijoPorUnidad_AllInCheckValor = cob.MontoFijoPorUnidad_AllInCheckValor;
		cobroFinal.PorcentualFijo_ValorCobro = cob.PorcentualFijo_ValorCobro;
		cobroFinal.PorcentualFijo_CheckSobreMonto = cob.PorcentualFijo_CheckSobreMonto;
		cobroFinal.PorcentualFijo_CheckSobreCantidad = cob.PorcentualFijo_CheckSobreCantidad;
		cobroFinal.PorcentualFijo_CheckMayorEntreDos = cob.PorcentualFijo_CheckMayorEntreDos;
		cobroFinal.PorcentualFijo_CheckMenorEntreDos = cob.PorcentualFijo_CheckMenorEntreDos;
		cobroFinal.PorcentualFijo_CheckMontoMinimo = cob.PorcentualFijo_CheckMontoMinimo;
		cobroFinal.PorcentualFijo_ValorMontoMinimo = cob.PorcentualFijo_ValorMontoMinimo;
		cobroFinal.PorcentualFijo_CheckMontoMaximo = cob.PorcentualFijo_CheckMontoMaximo;
		cobroFinal.PorcentualFijo_ValorMontoMaximo = cob.PorcentualFijo_ValorMontoMaximo;
		cobroFinal.PorcentualFijo_IdMonedaMinimoMaximo = cob.PorcentualFijo_IdMonedaMinimoMaximo;
		cobroFinal.PorcentualTasaPlazo_ValorCobro = cob.PorcentualTasaPlazo_ValorCobro;
		cobroFinal.Otros_CheckAfectoImpuesto = cob.Otros_CheckAfectoImpuesto;
		cobroFinal.Otros_CheckPermiteAumentarValor = cob.Otros_CheckPermiteAumentarValor;
		cobroFinal.Otros_CheckPermiteDisminuirValor = cob.Otros_CheckPermiteDisminuirValor;
		cobroFinal.Otros_CheckOperacionContado = cob.Otros_CheckOperacionContado;
		cobroFinal.Otros_CheckOperacionPlazo = cob.Otros_CheckOperacionPlazo;
		cobroFinal.Otros_CheckAllIn = cob.Otros_CheckAllIn;
		cobroFinal.Otros_CheckInteresCompuesto = cob.Otros_CheckInteresCompuesto;
		cobroFinal.Otros_CheckInteresSimple = cob.Otros_CheckInteresSimple;
		cobroFinal.RangoCobros = new ArrayList<DefinicionRangoCobro>(cob.RangoCobros);
		cobroFinal.PorcentualRango_IdMonedaRangoMonto = cob.PorcentualRango_IdMonedaRangoMonto;
		cobroFinal.PorcentualRango_IdTipoRangoCobro = cob.PorcentualRango_IdTipoRangoCobro;
		return cobroFinal;
	}
	
	private ObjRESULTADO copyPojoObjResultado(OrdenProceso ord) {
		ObjRESULTADO ordenResultado = new ObjRESULTADO();
		ordenResultado.IdInterno = ord.IdInterno;
		ordenResultado.IdOrden = ord.IdOrden;
		ordenResultado.DocumentoIdentidad = ord.DocumentoIdentidad;
		ordenResultado.CodigoPortafolio = ord.CodigoPortafolio;
		//ordenResultado.FechaOperacionOrden = ord.FechaOperacionOrden;
		ordenResultado.Cantidad = ord.Cantidad;
		//ordenResultado.MontoNeto = ord.MontoNeto;
		//ordenResultado.MontoLiquidacionOperacion = ord.MontoLiquidacionOperacion;
		//ordenResultado.MontoLiquidacionOperacionPlazo = ord.MontoLiquidacionOperacionPlazo;
		//ordenResultado.TotalCobros = ord.TotalCobros;
		//ordenResultado.MontoTotal = ord.MontoTotal;
		ordenResultado.PrecioOrden = ord.PrecioOrden;
		//ordenResultado.Instrumento = ord.Instrumento;
		ordenResultado.InstrumentoSubyacente = ord.InstrumentoSubyacente;
		ordenResultado.NombreTipoOperacion = ord.NombreTipoOperacion;
		ordenResultado.IdTipoOperacion = ord.IdTipoOperacion;
		ordenResultado.IdMonedaOrden = ord.IdMonedaOrden;
		ordenResultado.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden;
		ordenResultado.EsVentaCorta = ord.EsVentaCorta;
		ordenResultado.AccionOrden = ord.AccionOrden;
		ordenResultado.IdCondicionDeLiquidacion = ord.IdCondicionDeLiquidacion;
		ordenResultado.NombreCondicionDeLiquidacion = ord.NombreCondicionDeLiquidacion;
		ordenResultado.IdTipoPrecio = ord.IdTipoPrecio;
		ordenResultado.NombreTipoPrecio = ord.NombreTipoPrecio;
		ordenResultado.IdTipoVigenciaOrden = ord.IdTipoVigenciaOrden;
		ordenResultado.NombreTipoVigenciaOrden = ord.NombreTipoVigenciaOrden;
		ordenResultado.FechaVigencia = ord.FechaVigencia;
		//ordenResultado.Plazo = ord.Plazo;
		ordenResultado.NombreTipoInstrumento = ord.NombreTipoInstrumento;
		ordenResultado.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
		ordenResultado.IdTipoInstrumento = ord.IdTipoInstrumento;
		//ordenResultado.EsOD = ord.EsOD;
		ordenResultado.NombreCompleto = ord.NombreCompleto;
		ordenResultado.TasaVentaCorta = ord.TasaVentaCorta;
		ordenResultado.PlazoVentaCorta = new BigDecimal(ord.PlazoVentaCorta);
		ordenResultado.FechaVencimientoVentaCorta = ord.FechaVencimientoVentaCorta;
		ordenResultado.NombreEjecutivoPortafolio = ord.NombreEjecutivoPortafolio;
		ordenResultado.TasaReporte = ord.TasaReporte;
		ordenResultado.TasaPromedioReporte = ord.TasaPromedioReporte;
		ordenResultado.PlazoReporte = ord.PlazoReporte.intValue();
		ordenResultado.BaseTasa = ord.BaseTasa;
		ordenResultado.TasaSimultanea = ord.TasaSimultanea;
		ordenResultado.PlazoSimultanea = ord.PlazoSimultanea;
		ordenResultado.IdPreferenciaPagoPortafolio = ord.IdPreferenciaPagoPortafolio;
		ordenResultado.PrecioPlazo = ord.PrecioPlazo;
		//ordenResultado.MontoNetoPlazo = ord.MontoNetoPlazo;
		//ordenResultado.MontoTotalPlazo = ord.MontoTotalPlazo;
		ordenResultado.MontoLiquidacionPlazo = ord.MontoLiquidacionPlazo;
		ordenResultado.MontoInteresContado = ord.MontoInteresContado;
		ordenResultado.MontoInteresPlazo = ord.MontoInteresPlazo;
		ordenResultado.GlosaRefactura = ord.GlosaRefactura;
		return ordenResultado;
	}
	
	private Long libroSegunCanalDistribucion(String canalDistribucion, IndicadoresPortafolio indicadoresPortafolio){
		Long idLibroOrden = null;
		try {
			switch (canalDistribucion) {
			case "RUR":
				idLibroOrden = new Long(indicadoresPortafolio.IdLibroOrdenesRetail);
				break;
			case "MAN":
				idLibroOrden = new Long(indicadoresPortafolio.IdLibroOrdenes);
				break;
			case "RUA":
				idLibroOrden = new Long(indicadoresPortafolio.IdLibroOrdenesAgente);
				break;
			default:
				idLibroOrden = 0L;
			}
		}catch (Exception e){
			idLibroOrden = 0L;
		}
		return idLibroOrden;
	}
	
	private Long libroPorDefecto(Connection conn, Long idEmpresa) throws SQLException{
		SQL _sql = new SQL();
		HashMap<String,Object> paramsInLibroPorDefecto = new HashMap<>();
				
		paramsInLibroPorDefecto.put("idEmpresa", idEmpresa);
		List<LibroOrdenes> libroPorDefecto = _sql.query(conn, SQL_SELECT_LIBRO_POR_DEFECTO, paramsInLibroPorDefecto, LibroOrdenes.class);
		
		return new Long(libroPorDefecto.get(0).IdLibroOrdenes);
	}
	public Timestamp obtieneUltimoProcesoCambioFecha(Connection conn) throws SQLException{
		logger.debug("INICIO::ObtieneUltimoProcesoCambioFecha");
		Long inicioMetodo = System.currentTimeMillis();
		SQL _sql = new SQL();
		List<UltimoCambioFechaPauta> ultimoCambioFechaPauta = _sql.query(conn, SQL_SELECT_ULTIMO_CAMBIO_FECHA, new HashMap<>(), UltimoCambioFechaPauta.class);		
		
		Long finMetodo = System.currentTimeMillis();
		logger.debug("INICIO::ObtieneUltimoProcesoCambioFecha", finMetodo - inicioMetodo);
		return ultimoCambioFechaPauta.get(0).UltimoProcesoCambioFecha;
	}

	private String determinaTipoLiquidacion(String indicadorTipoLiquidacionCliente, List<OrdenTipoLiquidacion> tipoLiquidacionOrden) {
		
		String tipoLiquidacionResultado;
		if("MODPREFCONT".equals(indicadorTipoLiquidacionCliente) || "MODPREFCONV".equals(indicadorTipoLiquidacionCliente)) {
			tipoLiquidacionResultado = tipoLiquidacionOrden.parallelStream().filter(tipoLiq->"MODPREFCONT".equals(tipoLiq.CodigoDetalleIndicadorDefecto) || "MODPREFCONV".equals(tipoLiq.CodigoDetalleIndicadorDefecto)).findFirst().get().IdTipoLiquidacionComisionFactura;
		}
		else if("MODPREFNORM".equals(indicadorTipoLiquidacionCliente)) {
			tipoLiquidacionResultado = tipoLiquidacionOrden.parallelStream().filter(tipoLiq->"MODPREFNORM".equals(tipoLiq.CodigoDetalleIndicadorDefecto)).findFirst().get().IdTipoLiquidacionComisionFactura;
		}
		else {
			tipoLiquidacionResultado = tipoLiquidacionOrden.size() > 0 ? tipoLiquidacionOrden.parallelStream().filter(tipoLiq->"S".equals(tipoLiq.TipoPorDefecto)).findFirst().get().IdTipoLiquidacionComisionFactura : "DIR";
		}
		return tipoLiquidacionResultado;
	}
	
	private String determinaCodigoModoFactura(String indicadorTipoLiquidacionCliente, List<OrdenTipoLiquidacion> tipoLiquidacionPorDefecto) {
		
		String tipoLiquidacionResultado = indicadorTipoLiquidacionCliente;
		if("".equals(tipoLiquidacionResultado))
			tipoLiquidacionResultado = tipoLiquidacionPorDefecto.size() > 0 ? tipoLiquidacionPorDefecto.parallelStream().filter(tipoLiq->"S".equals(tipoLiq.TipoPorDefecto)).findFirst().get().CodigoDetalleIndicadorDefecto : "MODPREFNORM";
		
		return tipoLiquidacionResultado;
	}

	public String obtieneFechaDiaSiguiente(String fecha,Long idPais,Connection con,CrearOrdenesSalida salida) throws Exception {
		logger.debug("INICIO::obtieneFechaDiaSiguiente");
			Long inicioMetodo = System.currentTimeMillis();
			ObtenerDiaSiguiente sgte = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente entradaDiaSgte = new InObtenerDiaSiguiente();
			entradaDiaSgte.p_diaHabil = "S";
			entradaDiaSgte.p_idPais = idPais;
			entradaDiaSgte.p_soloCaeDiaHabil = "S";
			FechasIn e = new FechasIn();
			e.fechaInicial = fecha;
			e.numeroDias = 1L;
			e.pais = idPais;
			entradaDiaSgte.p_fechas.add(e );
			OutObtenerDiaSiguiente salidaDiaSgte = sgte.logica(con, entradaDiaSgte );
			if (salidaDiaSgte.p_out_fechas.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_EN_OBTENER_DIA_SIGUEINTE","No encontro dia siguiente"));
			}
			
			Long finMetodo = System.currentTimeMillis();
			logger.debug("FIN::ObtieneUltimoProcesoCambioFecha", finMetodo - inicioMetodo);
			
			return salidaDiaSgte.p_out_fechas.get(0).fechaCalculada;
		
		
		
	}
	
	public Boolean esDiaHabil(String fechaConsulta,Long idEmpresa, Connection con) throws Exception {
			logger.debug("INICIO::esDiaHabil");
			Long inicioMetodo = System.currentTimeMillis();
			
			ObtenerFeriado obtenerFeriadoService = new ObtenerFeriado();
			ParametrosEntradaServicio inObtenerFeriado = new ParametrosEntradaServicio();
			inObtenerFeriado.fechaConsulta = fechaConsulta;
			inObtenerFeriado.IdEmpresa = idEmpresa;
			ParametrosSalidaServicio outObtenerFeriado = obtenerFeriadoService.logica(con, inObtenerFeriado);
			
			
			Long finMetodo = System.currentTimeMillis();
			logger.debug("FIN::esDiaHabil", finMetodo - inicioMetodo);
			
			return outObtenerFeriado.habil;
		
	}
	private List<DiasAConsultarSalida> esDiaHabilMasivo(List<DiasAConsultar> fechaConsulta,Long idEmpresa, Connection con) throws Exception {
		logger.debug("INICIO::esDiaHabil");
		Long inicioMetodo = System.currentTimeMillis();
		
		ObtenerFeriado obtenerFeriadoService = new ObtenerFeriado();
		ParametrosEntradaServicio inObtenerFeriado = new ParametrosEntradaServicio();
		inObtenerFeriado.IdEmpresa = idEmpresa;
		inObtenerFeriado.fechasConsulta = fechaConsulta;
		ParametrosSalidaServicio outObtenerFeriado = obtenerFeriadoService.logica(con, inObtenerFeriado);
		
		
		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::esDiaHabil", finMetodo - inicioMetodo);
		
		return outObtenerFeriado.fechasConsultaOut;
	
}
	
	private void eliminarOrdenBloqueada(Connection con,Long idEmpresa,HashMap<String,String> valores,OrdenProceso ord,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		Date fechaSistemaLocal = Fecha.toDate("0001-01-01");
		fechaSistemaLocal= Fecha.toDate(FechaDB.ObtenerFechaHoraSistemaLocal(con,idEmpresa).toString().split(" ")[0]);
		BigDecimal precioOrden = ord.PrecioOrden.stripTrailingZeros();
		SQL _sql = new SQL();
		MensajeSalida objMensaje = new MensajeSalida();
		
		String idBloqueaOrden = ord.IdBloqueaOrden;
		HashMap<String,Object> inParametros = new HashMap<String,Object>();
		inParametros.put("p_IdBloqueaOrden", idBloqueaOrden);
		inParametros.put("p_IdPortafolioCliente", ord.IdPortafolioCliente);
		inParametros.put("p_IdInstrumento", ord.IdInstrumento);
		inParametros.put("p_IdTipoOperacion", ord.IdTipoOperacion);
		inParametros.put("p_Cantidad", ord.Cantidad);
		inParametros.put("p_PrecioLimite", precioOrden);
		inParametros.put("p_FechaSistema", fechaSistemaLocal);
		
		HashMap<String,Class> outParametros = new HashMap<String,Class>();
		
		if(!_c.GLOSA_CARGA_MASIVA.equalsIgnoreCase(ord.Glosa) && !_c.GLOSA_ORDEN_POSTRERA.equalsIgnoreCase(ord.Glosa)) {
			try {
				logger.debug("INICIO::SQL_DELETE_BLOQUEO_ORDEN");
				HashMap<String,Object> salidaDeleteBloqueoOrden = _sql.call(con,SQL_CALL_DELETE_BLOQUEO_ORDEN,inParametros,outParametros);
				logger.debug("FIN::SQL_DELETE_BLOQUEO_ORDEN");
			}catch(SQLException sqle) {
				objMensaje = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DELETE_BLOQUEO_ORDEN",valores,con,idEmpresa);
				P_OUT_MENSAJES.add(objMensaje);
				ord.MensajesProceso.add(objMensaje);
				ord.errores++;
			}
		}
		
	}
	
	private void eliminarOrdenBloqueada(Connection con,Long idEmpresa,HashMap<String,String> valores,HashMap<String,Object> inParametros,HashMap<String,Class> outParametros,List<MensajeSalida> P_OUT_MENSAJES, String Glosa) throws Exception {
		SQL _sql = new SQL();
		MensajeSalida objMensaje = new MensajeSalida();
		
		if(!_c.GLOSA_CARGA_MASIVA.equalsIgnoreCase(Glosa) && !_c.GLOSA_ORDEN_POSTRERA.equalsIgnoreCase(Glosa)) {
			try {
				logger.debug("INICIO::SQL_DELETE_BLOQUEO_ORDEN");
				HashMap<String,Object> salidaDeleteBloqueoOrden = _sql.call(con,SQL_CALL_DELETE_BLOQUEO_ORDEN,inParametros,outParametros);
				logger.debug("FIN::SQL_DELETE_BLOQUEO_ORDEN");
			}catch(SQLException sqle) {
				objMensaje = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DELETE_BLOQUEO_ORDEN",valores,con,idEmpresa);
				P_OUT_MENSAJES.add(objMensaje);
			}
		}
		
	}
	
	
	private void eliminarOrdenBloqueada(Connection con,Long idEmpresa,HashMap<String,String> valores,List<ObjOrdenesEntrada> p_Ordenes,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		if (p_Ordenes.size() > 0) {
			ArrayList <OrdenEliminar> Ordenes = new ArrayList <OrdenEliminar>();
			SQL _sql = new SQL();
			
			for (ObjOrdenesEntrada orden: p_Ordenes) {
				
				
				if(!_c.GLOSA_CARGA_MASIVA.equalsIgnoreCase(orden.Glosa) && !_c.GLOSA_ORDEN_POSTRERA.equalsIgnoreCase(orden.Glosa)) {
					String idBloqueaOrden = orden.IdBloqueaOrden;
					OrdenEliminar ordelim = new OrdenEliminar();
					ordelim.IdOrden = idBloqueaOrden;
					Ordenes.add(ordelim);
				}
			}
			
			if(!Ordenes.isEmpty()) {
				try {
					logger.debug("INICIO::SQL_DELETE_BLOQUEO_ORDEN");
					int[] res = _sql.batch(con,SQL_DELETE_BLOQUEO_ORDENES,Ordenes,OrdenEliminar.class);
					logger.debug("FIN::SQL_DELETE_BLOQUEO_ORDEN");
				}catch(SQLException sqle) {
					P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DELETE_BLOQUEO_ORDENES",valores,con,idEmpresa));
				}
			}
			
		}
	}
	
	public boolean validacionTablaVela(BigDecimal ValorPrecio , BigDecimal ValorFactor,String DecimasNoEntero) {
		boolean retorno = false;
		Integer CantidadDecimas =0;
		if ( "S".equalsIgnoreCase(DecimasNoEntero)) {
			BigDecimal parteDecimal = BigDecimal.valueOf(ValorPrecio.remainder(new BigDecimal(1)).doubleValue());  
				if(parteDecimal.compareTo(BigDecimal.ZERO)== 0 ) {
					CantidadDecimas =0;
				}else {
					CantidadDecimas = parteDecimal.toString().length()-2;
					}
				if( CantidadDecimas >  ValorFactor.intValue()) {
					retorno =true;
					}else {
						retorno =false;
					}
			}else {
					BigDecimal parteDecimal = BigDecimal.valueOf(ValorPrecio.remainder(new BigDecimal(1)).doubleValue());
					if(parteDecimal.compareTo(BigDecimal.ZERO)== 0 ) {
						CantidadDecimas =0;
				}else {
					CantidadDecimas = parteDecimal.toString().length()-2;}
					if (CantidadDecimas ==0 ) {
						BigDecimal parteEntera= BigDecimal.valueOf( ValorPrecio.longValue()).divide(ValorFactor).remainder(new BigDecimal(1));
				if (parteEntera.compareTo( BigDecimal.ZERO)==0) {
					retorno =false;
					}else {
						retorno =true;
						}
				}else {
					retorno =true;
					}
				}
		return retorno;
		} 
	
	public String convertirHoraUtcAHoraChile(String horaUtc) {
	    LocalDate fechaActual = LocalDate.now();
	    LocalTime localTimeUtc = LocalTime.parse(horaUtc, DateTimeFormatter.ofPattern("HH:mm:ss"));
	    ZonedDateTime utcDateTime = ZonedDateTime.of(fechaActual, localTimeUtc, ZoneId.of("UTC"));
	    ZonedDateTime chileDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("America/Santiago"));

	    return chileDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}
	
	public boolean aplicaFormaPagoCuentaInversionCanalIngresoClientes(String idCanalIngreso, Long idPreferenciaPagoPortafolio, HashMap<Long,PreferenciaPagoPortafolio> preferenciasPago) {
		return (_c.TIPO_CANAL_INGRESO_CLI.equalsIgnoreCase(idCanalIngreso) 
				&& (idPreferenciaPagoPortafolio == null || (idPreferenciaPagoPortafolio != null && preferenciasPago.get(idPreferenciaPagoPortafolio) == null)))
				|| !_c.TIPO_CANAL_INGRESO_CLI.equalsIgnoreCase(idCanalIngreso);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
			String RV ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":14500,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_CobrosModificados\":[],\"p_CobrosFijos\":[],\"p_Ordenes\":[{\"BaseTasa\":0,\"Cantidad\":10,\"EsFacturaInmediata\":\"N\",\"EsFungible\":\"N\",\"EsOD\":\"N\",\"EsOrdenEspecial\":\"N\",\"EspecificaBolsa\":\"N\",\"EsPorMonto\":\"N\",\"EsVentaCorta\":\"N\",\"FechaEmision\":\"0001-01-01\",\"FechaOperacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01 00:00:00\",\"FechaVigencia\":\"2026-08-31\",\"Glosa\":\"\",\"IdBolsa\":0,\"IdBoveda\":\"DCV\",\"IdCondicionDeLiquidacion\":\"T2\",\"IdCondicionDeLiquidacionPlazo\":\"\",\"IdCorredorCorresponsal\":null,\"IdCuentaCustodioPortafolio\":null,\"IdEmisor\":\"COPEC\",\"IdFormaCustodia\":\"CUS\",\"IdInstrumento\":502,\"IdInstrumentoSubyacente\":0,\"IdInstrumentoNegociacion\":502,\"IdInterno\":0,\"IdLibroOrdenes\":null,\"IdMecanismoCaptacion\":\"OM\",\"IdMonedaLiquidacionCobros\":\"CLP\",\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdMonedaOrden\":\"CLP\",\"IdPersonaAutorizadoDarOrdenes\":null,\"IdPorcentajeVisibilidad\":1,\"IdPortafolio\":332636,\"IdTipoCanalDistribucion\":\"RUR\",\"IdTipoCanalDistribucionReal\":\"RUR\",\"IdTipoCanalIngreso\":\"CLI\",\"IdTipoLibroOrden\":\"NORMAL\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoPrecio\":\"L\",\"IdTipoReajuste\":\"\",\"IdTipoVigencia\":\"SH\",\"MontoNeto\":0,\"MontoPlazo\":0,\"PagoCobroCuentaInversion\":\"S\",\"PlazoIF\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PrecioLimite\":6684,\"PrecioMercado\":0,\"TasaRFIF\":0,\"TasaSimultanea\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"TipoInteres\":\"\",\"ValorRescate\":0,\"IdPreferenciaPagoPortafolio\":246071448}]}";
			String RVhoy="{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9733,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_simularOperacion\": \"N\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_Ordenes\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"FechaRecepcion\": \"0001-01-01 00:00:00\",\r\n"
					+ "            \"FechaVigencia\": \"2026-07-29\",\r\n"
					+ "            \"FechaEmision\": \"0001-01-01\",\r\n"
					+ "            \"FechaOperacion\": \"0001-01-01\",\r\n"
					+ "            \"IdCondicionDeLiquidacion\": \"T2\",\r\n"
					+ "            \"IdTipoOperacion\": \"COMACN\",\r\n"
					+ "            \"IdTipoVigencia\": \"SH\",\r\n"
					+ "            \"Glosa\": \"\",\r\n"
					+ "            \"Cantidad\": 45,\r\n"
					+ "            \"MontoNeto\": 0,\r\n"
					+ "            \"ValorRescate\": 0,\r\n"
					+ "            \"IdBoveda\": \"DCV\",\r\n"
					+ "            \"IdFormaCustodia\": \"CUS\",\r\n"
					+ "            \"IdPorcentajeVisibilidad\": 1,\r\n"
					+ "            \"PrecioLimite\": 6308.9,\r\n"
					+ "            \"PrecioMercado\": 6308.9,\r\n"
					+ "            \"EsFacturaInmediata\": \"N\",\r\n"
					+ "            \"IdTipoPrecio\": \"M\",\r\n"
					+ "            \"IdPortafolio\": 12939,\r\n"
					+ "            \"IdTipoCanalIngreso\": \"AGE\",\r\n"
					+ "            \"IdTipoReajuste\": \"\",\r\n"
					+ "            \"IdEmisor\": \"COPEC\",\r\n"
					+ "            \"IdBolsa\": 0,\r\n"
					+ "            \"EspecificaBolsa\": \"N\",\r\n"
					+ "            \"IdMonedaLiquidacionOrden\": \"CLP\",\r\n"
					+ "            \"IdMonedaLiquidacionCobros\": \"CLP\",\r\n"
					+ "            \"TipoCambio\": null,\r\n"
					+ "            \"IdInstrumento\": 502,\r\n"
					+ "            \"IdInstrumentoSubyacente\": 0,\r\n"
					+ "            \"IdCondicionDeLiquidacionPlazo\": \"\",\r\n"
					+ "            \"TasaVentaCorta\": 0,\r\n"
					+ "            \"PlazoVentaCorta\": 0,\r\n"
					+ "            \"EsVentaCorta\": \"N\",\r\n"
					+ "            \"TasaSimultanea\": 0,\r\n"
					+ "            \"PlazoSimultanea\": 0,\r\n"
					+ "            \"PlazoIF\": 0,\r\n"
					+ "            \"TasaRFIF\": 0,\r\n"
					+ "            \"EsOD\": \"N\",\r\n"
					+ "            \"EsFungible\": \"N\",\r\n"
					+ "            \"MontoPlazo\": 0,\r\n"
					+ "            \"IdTipoLibroOrden\": \"NORMAL\",\r\n"
					+ "            \"IdLibroOrdenes\": null,\r\n"
					+ "            \"IdCuentaCustodioPortafolio\": 799,\r\n"
					+ "            \"EsPorMonto\": \"N\",\r\n"
					+ "            \"IdCorredorCorresponsal\": null,\r\n"
					+ "            \"IdPersonaAutorizadoDarOrdenes\": null,\r\n"
					+ "            \"IdTipoCanalDistribucion\": \"MAN\",\r\n"
					+ "            \"EsOrdenEspecial\": \"N\",\r\n"
					+ "            \"PagoCobroCuentaInversion\": \"S\",\r\n"
					+ "            \"BaseTasa\": 0,\r\n"
					+ "            \"TipoInteres\": \"\",\r\n"
					+ "            \"EsClienteFinal\": \"S\",\r\n"
					+ "            \"IdMecanismoCaptacion\": \"OM\",\r\n"
					+ "            \"FechaConfirmacionPlazo\": \"0001-01-01\",\r\n"
					+ "            \"TipoLiquidacionFactura\": \"\",\r\n"
					+ "            \"IngresoDesde\": \"COR\",\r\n"
					+ "            \"GeneraSpot\": \"N\",\r\n"
					+ "            \"NemoInstrumentoNegociacion\": \"\",\r\n"
					+ "            \"IdInstrumentoNegociacion\": 502,\r\n"
					+ "            \"IdMonedaOrden\": \"CLP\"\r\n"
					+ "        }\r\n"
					+ "    ],\r\n"
					+ "    \"p_CobrosModificados\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"IdCobro\": \"COVA\",\r\n"
					+ "            \"ValorCobroAplicado\": 0.4\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"IdCobro\": \"DEEX\",\r\n"
					+ "            \"ValorCobroAplicado\": 0\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"IdCobro\": \"GFOD\",\r\n"
					+ "            \"ValorCobroAplicado\": 3500\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"IdCobro\": \"IVA\",\r\n"
					+ "            \"ValorCobroAplicado\": 19\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			String pruebaRF = "";
	        
//			CrearOrdenesEntrada Objin = gson.fromJson(json, CrearOrdenesEntrada.class);
			CrearOrdenesEntrada Objincheck = gson.fromJson(RV, CrearOrdenesEntrada.class);
			CrearOrdenes demo=new CrearOrdenes();				
 			System.out.println("ENTRADA");
			System.out.println(Objincheck.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearOrdenesSalida ObjOut=demo.logica(con, Objincheck);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}

}
