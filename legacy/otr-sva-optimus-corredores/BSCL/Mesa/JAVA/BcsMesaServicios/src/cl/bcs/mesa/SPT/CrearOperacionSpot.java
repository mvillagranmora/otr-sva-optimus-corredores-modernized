package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.SPT.ConfirmarLiquidacionSpot.ConfirmarLiquidacionSpotEntrada;
import cl.bcs.mesa.SPT.ConfirmarLiquidacionSpot.ConfirmarLiquidacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.CrearOperacionSpotFacturacionEntrada;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.FormaPagoSpotFacturacionObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.OperacionSpotFacturacionObjIn;
import cl.bcs.mesa.SPT.NotificarEstadoSpot.EntradaNotificarEstadoSpot;
import cl.bcs.mesa.pojo.ConstantesMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa.ConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarParametroMesa;
import cl.bcs.mesa.utils.ConsultarParametroMesa.ParametroMesa;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.lim.pojo.Validacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.DetalleLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.InObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Utils.DatosPais;

import org.apache.commons.lang3.tuple.Pair;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class CrearOperacionSpot implements JavaCall{
	private static final Logger logger = LogManager.getLogger(CrearOperacionSpot.class);
	
	private final String SQL_INSERT_OPERACIONES_SPOT_WRAPPER = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_InsertOperacionesSpotWrapper.sql");
	private final String SQL_INSERT_OPERACIONES_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_InsertOperacionesSpot.sql");
	private final String SQL_SELECT_PUNTAS_CALCULAR_MARGEN = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarPuntasCalcularMargen.sql");
	private final String SQL_SELECT_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarPuntas.sql");
	private final String SQL_SELECT_REQUISITOS_CREAR_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarRequisitosCrearOperacionSpot.sql");
	private final String SQL_SELECT_LIMITES_PARIDAD_Y_TIPO_CAMBIO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarLimitesParidadYTipoCambio.sql");
	private final String SQL_SELECT_PORTAFOLIOS_FLUJO_CAJA = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarPortafoliosFlujoCaja.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_MODALIDAD_DE_PAGO = "SELECT \"IdMedioDePagoYModalidadDePago\",\"IdMedioDePago\",\"IdModalidadDePago\",\"IdEstado\" FROM  \"BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago\" WHERE \"IdEstado\" = 'ACT'";
	private final String SQL_SELECT_OPERACIONES_SPOT_INTERBANCARIO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotINTERB.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_INTERBANCARIO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotARBINT.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DISTRIBUCION = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotDISTRI.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_DISTRIBUCION = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotARBDIS.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_SelectPortafolio.sql");
	private final String SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarMonedasFeriadoMonedaPrincipal.sql");
	private final String SQL_SELECT_PLATAFORMAS = loadResourceToString("cl/bcs/mesa/SPT/sql/WSingresarOperacionSpot_ConsultarPlataformas.sql");	
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	private final ConstantesMesa _cm = new ConstantesMesa();
	private final ConstantesLimites _cl = new ConstantesLimites();
	private final String SQL_BUSQUEDA_PREFERENCIAS =loadResourceToString("cl/bcs/mesa/SPT/sql/SelectPreferenciasConversionParaFacturacion.sql"); 
			
	public static class OperacionObjIn{
		public String NumeroOperacion;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public Long IdCliente; 
		public Long IdPortafolio;
		public java.sql.Timestamp FechaOperacion;
		public String HoraOperacion;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		
		public BigDecimal TipoCambioCierreAntiguo; //Para log
		public BigDecimal TipoCambioCostoAntiguo; //Para log
		
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCierreForward;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCierreForward;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal MontoEquivalente;
		public BigDecimal MontoRentabilidad;
		public BigDecimal MontoRentabilidadCanal;
		public BigDecimal Margen;
		public BigDecimal Utilidad;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String SolicitudTipoCambio;
		public Long IdMesa;
		public String IdModulo;
		public Long IdTipoDolar;
		public String TipoComprobanteFacturacion;
		public Long IdAgente;
		public String BenOur;
		public BigDecimal EquivalenteConGasto;
		public BigDecimal TipoCambioCierreConGasto;
		public String Observacion;
		public Long CodigoComercio;
		public Long FolioPlanilla;
		public String FolioInterfaz;
		public String OperacionFicticia;
		public String IdTipoCanalIngreso;
		public String IdDatatec;
		public String Estado;
		public Long IdSolicitudAporteRescate=0L;
		public String TipoSolicitud= "";
		public String TipoDocumento = "";
		public long	IdContacto = 0L;
		public String IdSerieFondo;
		public String ValidaFlujoCaja = "S";
		public String AfectoADerivado = "N";
		public String DolarObservado = "N";
		public String ProductoAgrupador ="";
		public String CodigoPortafolio ="";
		public String Confirmar = ""; //WS
		public String IdPlataformaNegociacion = "OTC";
		public String CodigoEjecutivoDigitador = "";
		public Long IdEmpleadoDigitador = 0L;
		public String FacturacionAutomatica;
		public String EstadoCaja;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		public OperacionObjIn() {
			this.ProductoAgrupador = "";
			this.CodigoPortafolio = "";
			this.Confirmar = "";
			this.FacturacionAutomatica = "N";
			this.EstadoCaja = "NRE";
		}
	}
	
	public static class FormaPagoObjIn{
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public String IdTipoMovimiento;
		public String IdFormaDePago;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String AfectarTesoreria;
		public Date FechaLiquidacion;
		public Long IdCuentaBancaria = 0L;		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearOperacionSpotEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CalculaMargen;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<OperacionObjIn> p_DatosOperacion;
		public List<FormaPagoObjIn> p_DatosFormaPago;
		public String p_PermiteOperarProspectos;
		public String p_EsCargaMasiva;
		public String p_EsIngresoWS;
		
		public CrearOperacionSpotEntrada() {
			this.p_DatosOperacion = new ArrayList<OperacionObjIn>();
			this.p_DatosFormaPago = new ArrayList<FormaPagoObjIn>();
			this.p_SimularOperacion = "N"; //Se deja N como por defecto dado que antes no tenia este parametro y para no modificar las otras piezas.
			this.p_PermiteOperarProspectos = "N";
			this.p_EsCargaMasiva = "N";
			this.p_EsIngresoWS = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionObjOut {
		public String IdInterno;
		public Long IdOperacionSpot;
		public String IdDatatec;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearOperacionSpotSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionObjOut>	P_OUT_IDOPERACIONSPOT;
		
		public CrearOperacionSpotSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpot{
		public Long IdInterno;
		public String NumeroOperacion;
		public Long IdOperacionSpot;
		public Long IdEmpresa;
		public Long IdCliente; 
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public java.sql.Timestamp FechaOperacion;
		public Date FechaLiquidacion;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public String IdMonedaLocal;
		public String IdMonedaEquivalente;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaPrincipalEquivalente;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCierreForward;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCierreForward;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal MontoEquivalente;
		public BigDecimal MontoRentabilidad;
		public BigDecimal MontoRentabilidadCanal;
		public BigDecimal ResultadoMonedaLocalPuntas;
		public BigDecimal ResultadoMonedaLocalBidAsk;
		public BigDecimal ResultadoMonedaLocalDO;
		public BigDecimal ResultadoMonedaLocalTCSpot;
		public BigDecimal ResultadoMonedaLocalTCCierreMesa = BigDecimal.ZERO; //IMPCREDOPT-2549 campo ResultadoMonedaLocalTCCierreMesa se debe actualizar una vez cerrada la mesa
		public BigDecimal ResultadoMonedaLocalConsolidado;
		public BigDecimal Margen;
		public BigDecimal MargenBidAsk;
		public BigDecimal Utilidad;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal PuntaBid;
		public BigDecimal PuntaAsk;
		public BigDecimal ParidadBid;
		public BigDecimal ParidadAsk;
		public BigDecimal ValorDolarObservado;
		public BigDecimal ValorTCSpot;
		public String SolicitudTipoCambio;
		public Long IdEvento;
		public Long IdMesa;
		public String IdModulo;
		public Long IdTipoDolar;
		public String TipoComprobanteFacturacion;
		public Long IdComprobanteFacturacion;
		public Long IdComprobanteTesoreriaIngreso;
		public Long IdComprobanteTesoreriaEgreso;
		public Long IdAgente;
		public String BenOur;
		public BigDecimal EquivalenteConGasto;
		public BigDecimal TipoCambioCierreConGasto;
		public String Observacion;
		public Long CodigoComercio;
		public Long FolioPlanilla;
		public String FolioInterfaz;
		public String OperacionFicticia;
		public String IdTipoCanalIngreso;
		public String IdDatatec;
		public String Estado;
		public Long IdSolicitudAporteRescate = 0L;
		public Long IdUsuario;
		public Long IdUsuarioModifica;
		public java.sql.Timestamp FechaModifica;
		public java.sql.Timestamp FechaReproceso;
		public java.sql.Timestamp FechaIngresoRegistro;
		public String EstadoConfirmacion;
		public Long IdOperacionSpotReferencia = 0L;
		public Long FolioOrigenFW = 0L;
		public Long IdOperacionSpotOrigen;
		public Long IdOperacionSpotVigente;
		
		public String MultiplicaODivide;
		public BigDecimal ParidadMercado;
		public BigDecimal CierreIngresadoPosMonPri;
		public BigDecimal CierreIngresadoPosMonEqui;
		public String Nemotecnico;
		public BigDecimal TipoCambioUSD;
		public BigDecimal TipoCambioMonedaSecundaria;
		
		public String NombreCompleto = "";
		public String DocumentoIdentidad = "";
		
		public BigDecimal MontoComision = BigDecimal.ZERO;
		public String IdTipoMovimientoFondoMutuo = "";
		public String IdEmisorOrigen = "";
		public String FechaCertificacion = "0001-01-01";
		public Long   IdInstrumentoFFMM = 0L;
		public String NemotecnicoFFMM = "";
		public String IdEmisorFFMM = "";
		
		public String TipoDocumento;
		public long	IdContacto;

		public String AfectoADerivado = "N";
		public String DolarObservado = "N";
		
		public String IdTipoSpot = "";
		public String CodigoOperador = "";
		public String Confirmar ="";
		public String IdPlataformaNegociacion = "OTC";
		public String IdTipoInstrumento ="";
		public Long IdEmpleadoDigitador = 0L;
		
		public String FacturacionAutomatica = "N";
		public String EstadoCaja = "NRE";
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpotFormaDePago{
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public Long IdAbono;
		public Long IdCargo;
		public Long IdIngresoEgresoAnticipo;
		public Long IdAbonoAnticipo;
		public Long IdCargoAnticipo;
		public String IdTipoProducto;
		public String IdTipoMovimiento;
		public String RecibePagaPortafolio;
		public String IdFormaDePago;
		public String IdModalidadPago;
		public String IdMedioDePago;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public String FechaIngresoComprobantePagoOperacion;
		public String FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public String AfectarTesoreria;
		public Long IdCuentaBancaria;
		public String IdTipoOperacion = "";
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntasCalcularMargen{
		public String NumeroOperacion;
		public Long IdEmpresa;
		public String IdTipoOperacion;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String MonedaLocal;
		public String CheckPrecioPorTramo;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCosto;
		public BigDecimal ParidadMercado;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal ValorCompra;
		public BigDecimal ValorVenta;
		public BigDecimal Margen;
		

		public BigDecimal TipoCambioCosto;
		
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	public static class Puntas{
		public String NumeroOperacion;
		public String IdTipoOperacion;
		public Long IdInstrumento;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal TipoCambioCierre;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String IdTipoCanalIngreso;
		public BigDecimal ValorCompra;
		public BigDecimal ValorVenta;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class RequisitosCrearOperacionSpot{
		public String NumeroOperacion;
		public Timestamp FechaOperacion;
		public String IdMonedaPrincipal;
		public Long IdMesa;
		public String IdEstadoMesa;
		public String IdEstadoModulo;
		public String TipoEstadoModulo;
		public String PautaEnEjecucion;
		public Timestamp UltimoProcesoCambioFecha;
		public String EstadoMesaSpot;
		public String EstadoFondo;
		public String EstadoProcesoCambioFecha;
		public String IdTipoComprobante;
		public String IdEstadoFacturacion;
		public BigDecimal TipoCambioUSD;
		public BigDecimal TipoCambioPrincipal;
		public BigDecimal ParidadPrincipal;
		public Long IdCliente;
		public String IdEstadoCliente;
		public String ClienteEsEnrolado;
		public Long IdPortafolio;
		public String IdEstadoPortafolio;
		public String IdEstadoAutorizacion;
		public Long IdSolicitudAporteRescate;
		public String CodigoPortafolio;
		public String DocumentoIdentidad;
		public String IdEstadoCuentaInversionMonedaPrincipal;
		public String IdEstadoCuentaInversionMonedaSecundaria;
		public String ResultadoPerfilProducto;
		public String MensajePerfilProducto;
		public String PlantillaEmpleadoProductoHabilitado;
		public String Nemotecnico;
		public String EsClienteNuevoProspecto = "N";
		public Long IdEmpleadoDigitador;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class LimitesParidadYTipoCambio{
		public String NumeroOperacion;
		public String FechaOperacion;//
		public String IdTipoOperacion;//
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal ParidadMercado;
		public BigDecimal ParidadBID;
		public BigDecimal ParidadASK;
		public BigDecimal LimiteSuperiorParidad;
		public BigDecimal LimiteInferiorParidad;
		public BigDecimal LimiteSuperiorPAR;
		public BigDecimal LimiteInferiorPAR;
		public BigDecimal TipoCambioMercado;
		public BigDecimal LimiteSuperiorTipoCambio;
		public BigDecimal LimiteInferiorTipoCambio;
		public BigDecimal LimiteSuperiorTC;
		public BigDecimal LimiteInferiorTC;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafoliosFlujoCaja{
		public String NumeroOperacion;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormaDePagoMesa {
		public String IdFormaDePago;
	    public String NombreFormaDePago;
	    public Long	Dias;
	    public String PorDefecto;
	    public String RecibePagaPortafolioDesc;
	    public String IdEstado;
	    public String NombreEstado;
	    public String IdPreferenciaPago;
	    public String IdTipoOperacion;
	    public String IdMedioDePago;
	    public String IdMoneda;
	    public String UsaPersonaRelacionada;
	    public Date FechaCreacion;
	    public Date FechaModificacion;
	    public Long IdUsuarioCreador;
	    public Long IdEmpresa;
	    public String NombreTipoOperacion;
	    public String IdTipoProducto;
	    public String NombreTipoProducto;
	    public String RecibePagaPortafolio;
	    public String NombreMedioDePago;
	    public String NombreMoneda;
	    public String UsaCuentaBancaria;
	    public String UsaTarjetaBancaria;
	    public String IdEstadoPreferenciaPago;
	    public String NombreEstadoPreferenciaPago;
	    public String IdCondicionDeLiquidacion;
	    public String CodigoCondicionDeLiquidacion;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ModalidadDePago {
		public Long IdMedioDePagoYModalidadDePago;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdEstado;
	}
	
	public static class OperacionSpotEventoAutorizacion{
		public String NumeroOperacion;
		public String IdTipoEvento;
		public String Glosa;
	}
	public static class PreferenciasPagoSpot{
		public String  IdPreferenciaPago;
		public String IdMedioDePago;
	}
	
	public static class PlataformaNegociacion extends BcsPojo{
		public String Codigo;
	}
	

	public static class Empleado extends BcsPojo{
		public Long IdEmpleadoDigitador;
		public String CodigoEmpleado;
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearOperacionSpotEntrada in = gson.fromJson(jin.toString(), CrearOperacionSpotEntrada.class);
		CrearOperacionSpotSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_MENSAJES.stream().filter(u -> "AUTORIZACION".equals(u.tipo) || MensajeSalida.MENSAJE_ADVERTENCIA.equals(u.tipo)).count();
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public boolean validarIngreso1(String estadoFondo,String estadoMesa,String estadoPauta) {
		return estadoFondo.equals("A") && estadoMesa.equals("ABT") && estadoPauta.equals("N");
	}
	
	//considerando feriados de USA
	public String calculaFechaLiquidacion(Connection conn, String fechaEntrada, Long numeroDias, Long pais , Long idPaisSistema) throws Exception {
		//se calcula el dia anterior porque se necesita consultar el mismo dia en EEUU y al pasar la misma fecha el procedimiento "logica" asume que el dia ya es habil  y lo retorna
		ObtenerDiaSiguienteMultiplesPaises obtenerDiaSiguienteMulti = new ObtenerDiaSiguienteMultiplesPaises();
		InObtenerDiaSiguienteMultiplesPaises entradaDiaSiguienteMulti = new InObtenerDiaSiguienteMultiplesPaises();
		OutObtenerDiaSiguienteMultiplesPaises salida = new OutObtenerDiaSiguienteMultiplesPaises();
		entradaDiaSiguienteMulti.p_diaHabil = "S";
		List<Long> paises=new ArrayList<Long>(Arrays.asList(pais,idPaisSistema));
		cl.bcs.plataforma.pla.pojo.FechasIn fechaIn = new cl.bcs.plataforma.pla.pojo.FechasIn(0L,fechaEntrada,numeroDias,paises);	
		
		if(numeroDias.equals(0L)) {
			InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
			ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
			InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
			fechasIn.fechaInicial = fechaEntrada;
			fechasIn.numeroDias = 1L;
			inObtenerDiaAnterior.fechas.add(fechasIn);
			inObtenerDiaAnterior.idPais = pais;
			inObtenerDiaAnterior.diaHabil = "S";
			
			OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(conn, inObtenerDiaAnterior);
			
			String fechaAnterior = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;

			fechaIn = new cl.bcs.plataforma.pla.pojo.FechasIn(0L,fechaAnterior,1L,paises);			
		
			entradaDiaSiguienteMulti.p_fechas.add(fechaIn);
			entradaDiaSiguienteMulti.p_soloCaeDiaHabil="S";		
		
			salida = obtenerDiaSiguienteMulti.logica(conn, entradaDiaSiguienteMulti);	
		}
		else if(numeroDias.equals(1L)) {
			cl.bcs.plataforma.pla.pojo.FechasIn fechaIn2 = new cl.bcs.plataforma.pla.pojo.FechasIn(0L,fechaEntrada,numeroDias,paises);
			InObtenerDiaSiguienteMultiplesPaises entradaDiaSiguienteMultipPaises = new InObtenerDiaSiguienteMultiplesPaises();
			entradaDiaSiguienteMultipPaises.p_diaHabil = "S";
			entradaDiaSiguienteMultipPaises.p_fechas.add(fechaIn2);
			entradaDiaSiguienteMultipPaises.p_soloCaeDiaHabil="S";
			
			salida = obtenerDiaSiguienteMulti.logica(conn, entradaDiaSiguienteMultipPaises);
		}
		else {
			cl.bcs.plataforma.pla.pojo.FechasIn fechaIn3 = new cl.bcs.plataforma.pla.pojo.FechasIn(0L,fechaEntrada,numeroDias,paises);
			InObtenerDiaSiguienteMultiplesPaises entradaDiaSiguienteMultipPaises = new InObtenerDiaSiguienteMultiplesPaises();
			entradaDiaSiguienteMultipPaises.p_diaHabil = "S";
			entradaDiaSiguienteMultipPaises.p_fechas.add(fechaIn3);
			entradaDiaSiguienteMultipPaises.p_soloCaeDiaHabil= "S";
			entradaDiaSiguienteMultipPaises.p_operacionSpot= "S";  			//opcion valida solo para Operaciones Spot
			entradaDiaSiguienteMultipPaises.p_paisSistema = idPaisSistema;	//Se usa para determinar si feriado es Nacional o Internacional
			
			salida = obtenerDiaSiguienteMulti.logica(conn, entradaDiaSiguienteMultipPaises);
		}
		
		if(salida.P_OUT_ERRORESCONTADOR>0) return fechaEntrada;
		
        return salida.p_out_fechas.get(0).fechaCalculada;
		
		
	}
	
	public static BigDecimal convertirCentavosAPesosChilenos(BigDecimal centavos) {
		logger.trace("INICIO::CrearOperacionSpot.convertirCentavosAPesosChilenos");
		long inicio = System.currentTimeMillis();
		
        BigDecimal pesosChilenos = centavos.divide(new BigDecimal(100));

        long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionSpot.convertirCentavosAPesosChilenos: " + (fin - inicio));
        return pesosChilenos;
    }


	public CrearOperacionSpotSalida logica(Connection conn, CrearOperacionSpotEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio CrearOperacionSpot");
		
		SQL _sql = new SQL();
		Date fechaDefecto = Fecha.toDate("0001-01-01");
		CrearOperacionSpotSalida out = new CrearOperacionSpotSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		Long advCont = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		try {
			if (in.p_DatosOperacion.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			/***** VARIABLES *****/
			int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(conn, in.p_IdEmpresa);
	        Fecha.setLocalOffSet(segundosZonaHoraria);
			Timestamp fechaHoraReal= Fecha.toLocalTimeStamp(FechaDB.ObtenerFechaHoraTs(conn, in.p_IdEmpresa));
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			java.sql.Date fechaReal = Fecha.toDate(fechaHoraReal.toString().split(" ")[0]);
			String horaSeparada = fechaHoraSistemaSecondate.split(" ")[1];
			Timestamp fechaRealAux = Fecha.toLocalTimeStamp(Fecha.toTimeStamp(fechaReal+" "+horaSeparada));
			java.sql.Time horaReal = new java.sql.Time(fechaRealAux.getHours(),fechaRealAux.getMinutes(),fechaRealAux.getSeconds());
			
			Long IdUsuarioSistema = 0L;
			try {
				IdUsuarioSistema = ConfiguracionSistema.getParametroConfiguracionLong(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.IdUsuarioBolsa");
	        } catch (Exception e) {           
	        	IdUsuarioSistema = 0L;           
	        }
			Timestamp fechaHoraSistemaTimestamp = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(conn,in.p_IdEmpresa));
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		    Long idPaisSistema = empresa.IdPais;
		    DatosPais paisUsa = Utils.getDatosPais(conn, "US");
		    Long idUsa = paisUsa.IdPais;
			
			ConfiguracionMesa configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "IDMONEDAEQUIVALENTE");
			String monedaEquivalente = configuracionMesa.Valor;
			
			configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "ENVIA_SPOT_A_CONFIRMAR_LIQUIDACION");
			String liquidacionPorConfirmar = configuracionMesa.Valor;
			String estadoConfirmacion = "PEN";
			Boolean ConfirmacionWS = false;
			
			// GCMBIDDLDP
			String ConfirmaLiquidacionNEG = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.GeneraComprobanteAutomaticoNEG");
			String ConfirmaLiquidacionCLI =  ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.GeneraComprobanteAutomaticoCLI");
			
			ParametroMesa parametroMesa = new ConsultarParametroMesa().logica(conn, in.p_IdEmpresa, "FACTURACIONINTERBANCARIOSPOT", "FACTURACIONINTERBANCARIOSPOT", "VIG");
			String parametroFacInterbSpot = parametroMesa.ValorParametro;
			
			String WSingresoMesaCerrada = "N";
			try {
				WSingresoMesaCerrada = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "MESA", "WS_INGRESO_MESA_CERRADA");
	        } catch (Exception e) {           
	        	WSingresoMesaCerrada = "N";           
	        }
			Boolean esIngresoWS = "S".equalsIgnoreCase(in.p_EsIngresoWS) && "S".equalsIgnoreCase(WSingresoMesaCerrada);			
			Long cantOpeIn = 0L;
			Long cantOpeVig = 0L;
			Long cantOpePen = 0L;
			Long cantOpeErr = 0L;
			
			HashMap<String,Object> params = new HashMap<>();
			List<OperacionObjIn> operacionesInErrValidaciones = new ArrayList<>();
			List<OperacionObjIn> operacionesInPenValidaciones = new ArrayList<>();
			List<OperacionObjIn> operacionesInErrSaldoFlujoCa = new ArrayList<>();
			
			List<OperacionObjIn> operacionesInUpdateEstado = new ArrayList<>();
			List<OperacionObjIn> operacionesInUpdateMargen = new ArrayList<>();
			List<OperacionSpot> operacionesSpot = new ArrayList<>();
			
			List<OperacionSpotEventoAutorizacion> evtList = new ArrayList<>();
			
			HashMap<String,OperacionObjIn> numeroOperacionHash = new HashMap<String,OperacionObjIn>();
			List<String> numeroOperacion = new ArrayList<String>();
			List<Long> cliente = new ArrayList<Long>();
			List<Long> portafolio = new ArrayList<Long>();
			
			for (OperacionObjIn ope: in.p_DatosOperacion){
				numeroOperacion.add(ope.NumeroOperacion);
				cantOpeIn++;
				
				numeroOperacionHash.put(ope.NumeroOperacion, ope);
				
				//Se formatea montos dependiendo de su idmoneda
				ope.MontoMonedaPrincipal = MonedaFormato.formatear(conn, ope.MontoMonedaPrincipal, "MONTO", ope.IdMonedaPrincipal,"SPT","").valor;
				ope.MontoMonedaSecundaria = MonedaFormato.formatear(conn, ope.MontoMonedaSecundaria, "MONTO", ope.IdMonedaSecundaria,"SPT","").valor;
				
				cliente.add(ope.IdCliente);
				portafolio.add(ope.IdPortafolio);
				
				logger.info("BuscandoElBug - ope.FechaOperacion (11): " + ope.FechaOperacion + " - Solicitud FFMM ID" + ope.IdSolicitudAporteRescate);
				
			}
			
			HashMap<String,Object> params2 = new HashMap<String,Object>();
			params2.put("IdCliente",cliente);
			params2.put("IdPortafolio", portafolio);
			logger.debug("INICIO::SQL_SELECT_PORTAFOLIO");
			List<ClientePortafolioSolicitudes> clientesSolicitudes = _sql.query(conn, SQL_SELECT_PORTAFOLIO, params2, ClientePortafolioSolicitudes.class);
			logger.debug("FiN::SQL_SELECT_PORTAFOLIO");
			HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
			
			//Formas de pago Mesa
			params.put("IdTipoProducto" , "MEME");
			logger.debug("INICIO::SQL_SELECT_FORMAS_DE_PAGO_MESA");
			List<FormaDePagoMesa> formaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, params, FormaDePagoMesa.class);
			logger.debug("FIN::SQL_SELECT_FORMAS_DE_PAGO_MESA");
			HashMap<String, Object> formaDePagoMesaHash = Utils.List2Hash(formaDePagoMesaList, "IdFormaDePago");
			
			//Modalidad de pago
			logger.debug("INICIO::SQL_SELECT_MODALIDAD_DE_PAGO");
			List<ModalidadDePago> modalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_DE_PAGO, new HashMap<>(), ModalidadDePago.class);
			logger.debug("FIN::SQL_SELECT_MODALIDAD_DE_PAGO");
			HashMap<String, Object> modalidadPagoHash = Utils.List2Hash(modalidadPago, "IdMedioDePago");
			
			SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(conn, in.p_IdEmpresa, in.p_IdUsuario).orElse(null);
			SocioNegocioUtils.Empleado empleadoUsuarioCliente = SocioNegocioUtils.obtenerEmpleadoUsuario(conn, in.p_IdEmpresa, IdUsuarioSistema).orElse(null);

			for (OperacionObjIn ope: in.p_DatosOperacion){
				if ("CLI".equals(ope.IdTipoCanalIngreso)) {
					ope.IdEmpleadoDigitador = empleadoUsuarioCliente.IdEmpleado;
				} else {
					ope.IdEmpleadoDigitador = ope.IdEmpleadoDigitador != 0 ? ope.IdEmpleadoDigitador: empleadoUsuario.IdEmpleado;
				}
			}
			
			//Insert tabla global para los select con varios join
			_sql.batch(conn, SQL_INSERT_OPERACIONES_SPOT_WRAPPER, in.p_DatosOperacion, OperacionObjIn.class);

			//Plataformas de negociacion validas
			logger.debug("INICIO::SQL_SELECT_PLATAFORMAS");
			List<PlataformaNegociacion> plataformasValidas = _sql.query(conn, SQL_SELECT_PLATAFORMAS, new HashMap<String, Object>(),PlataformaNegociacion.class);
			logger.debug("FIN::SQL_SELECT_PLATAFORMAS");

			String pautaEnEjecucion = new String();
			String estadoProcesoCambioFecha = new String();
			
			String fechaSistemaNueva = "0001-01-01";
			String fechaHoraSistemaSecondateNueva = "0001-01-01 00:00:00";
			String estadoFondo = new String();
			java.sql.Timestamp fechaUltimaEjecucionCambioFecha = Fecha.toTimeStamp("0001-01-01 00:00:00");
			Date fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate("0001-01-01");
			String estadoMesaSpot = new String();
			java.sql.Time horaUltimaEjecucionCambioFecha = Fecha.toTime("00:00:01");
			String tipoSolicitud = "";
						
			/***** VALIDACIONES *****/
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("NumeroOperacion", numeroOperacion);
			params.put("FechaSistema", fechaSistema);
			params.put("IdUsuario", in.p_IdUsuario);
			for(OperacionObjIn op: in.p_DatosOperacion)
				params.put("IdSerieFondo",op.IdSerieFondo);
				Map<Long, String> idEstadoMesaPorIdMesa = new HashMap<>();

			
			if(in.p_UsaReglasNegocio.equals("S")){
				//VALIDACIONES GENERALES
				logger.debug("Inicio SQL_SELECT_REQUISITOS_CREAR_OPERACION_SPOT");
				startTime = System.nanoTime();
				List<RequisitosCrearOperacionSpot> requisitosCrearOperacionSpot = _sql.query(conn, SQL_SELECT_REQUISITOS_CREAR_OPERACION_SPOT, params, RequisitosCrearOperacionSpot.class);
				String validaPlantillaEmpleado = "N";
				try{ validaPlantillaEmpleado = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "VALIDA_PLANTILLA_EMPLEADO").Valor;} catch(Exception er) {};
				logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_REQUISITOS_CREAR_OPERACION_SPOT");
				
				for(RequisitosCrearOperacionSpot req: requisitosCrearOperacionSpot){
					idEstadoMesaPorIdMesa.put(req.IdMesa, req.IdEstadoMesa);
					//Codigos que no se incorporaron : EXCEDE_HORARIO, DOLAR_MENOR_IGUAL_CERO. El codigo OPERACION_SPOT_FACTURADA solo aplica en la Modificacion Spot
					tipoSolicitud = numeroOperacionHash.get(req.NumeroOperacion).TipoSolicitud;					
					OperacionObjIn ope = new OperacionObjIn();
 					boolean entraIF = false;
					Long idNumOpe = Long.parseLong(req.NumeroOperacion);
					pautaEnEjecucion = req.PautaEnEjecucion;
					estadoProcesoCambioFecha = req.EstadoProcesoCambioFecha;
					String canalIngreso = in.p_DatosOperacion.stream().filter(op -> op.NumeroOperacion.equals(req.NumeroOperacion)).map(op -> op.IdTipoCanalIngreso).findFirst().orElse("");
					
					fechaUltimaEjecucionCambioFecha = Fecha.toLocalTimeStamp(req.UltimoProcesoCambioFecha);
					String[] fechaTs2 = fechaUltimaEjecucionCambioFecha.toString().split(" ")[1].split("\\.");
					horaUltimaEjecucionCambioFecha = Fecha.toTime(fechaTs2[0]);
					fechaUltimaEjecucionCambioFechaLocal = Fecha.toDate(fechaUltimaEjecucionCambioFecha.toString().split(" ")[0]);  
					estadoFondo = (_c.SI).equals(in.p_EsCargaMasiva) && ("FMM").equals(canalIngreso) ? _c.Abierto : req.EstadoFondo;
					estadoMesaSpot = req.EstadoMesaSpot;
					Boolean esAporteNormalFMM = ("FMM".equals(in.p_IdModulo) && "APONORM".equalsIgnoreCase(numeroOperacionHash.get(req.NumeroOperacion).TipoSolicitud)) ? true : false;
					
					if(!"A".equals(req.IdEstadoMesa) && "N".equals(req.PautaEnEjecucion) && !esAporteNormalFMM && !esIngresoWS){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"MESA_SPOT_CERRADA",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(req.IdMesa.longValue() == 0L){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_MESA_SPOT_POR_DEFECTO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(!("ACT".equals(req.IdEstadoModulo) && "A".equals(req.TipoEstadoModulo))){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_CERRADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(!("S".equals(in.p_EsCargaMasiva))){
						if(!(Fecha.toStringDate(req.FechaOperacion).equals(fechaSistema) || req.FechaOperacion.after(Fecha.toDate(fechaSistema)))){
							mapMsg.put("NumeroOperacion", req.NumeroOperacion);
							out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"FECHA_PROCESO_INVALIDA",mapMsg,conn,in.p_IdEmpresa));
							Id++;
							entraIF = true;
						}
					}
					if(req.IdTipoComprobante == null || (!(req.IdTipoComprobante.contains("FA") || "BOE".equals(req.IdTipoComprobante) || "BEE".equals(req.IdTipoComprobante)) && "".equals(req.IdEstadoFacturacion))){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"TIPO_COMPROBANTE_INVALIDO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(req.TipoCambioUSD == null || (req.TipoCambioUSD.compareTo(BigDecimal.ZERO) <= 0 && !"USD".equals(req.IdMonedaPrincipal))){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"VALOR_DOLAR_ACTUAL_IGUAL_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(!"ARBINT".equalsIgnoreCase(req.Nemotecnico) && (req.TipoCambioPrincipal == null || req.TipoCambioPrincipal.compareTo(BigDecimal.ZERO) <= 0)){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_PRINCIPAL_IGUAL_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(!"ARBINT".equalsIgnoreCase(req.Nemotecnico) && (req.ParidadPrincipal == null || req.ParidadPrincipal.compareTo(BigDecimal.ZERO) <= 0)){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"PARIDAD_PRINCIPAL_IGUAL_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(req.IdCliente == null || req.IdCliente <= 0L){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					req.IdEstadoCliente = "ACT".equals(req.IdEstadoCliente) ? "P".equals(req.ClienteEsEnrolado) || "N".equals(req.ClienteEsEnrolado) ? "PEN" : "ACT" : "BLO";
					if(req.IdCliente != null && !(req.IdCliente > 0L && "ACT".equals(req.IdEstadoCliente)) && !( _c.SI.equals(in.p_PermiteOperarProspectos) && _c.SI.equals(req.EsClienteNuevoProspecto))){
						String msjError = "";
						switch(req.IdEstadoCliente){
							case "BLO": msjError = "CLIENTE_BLOQUEADO"; break;
							case "PEN": msjError = "CLIENTE_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "CLIENTE_NO_HABILITADO"; break;
						}
						
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(req.IdPortafolio == null || req.IdPortafolio <= 0L){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					req.IdEstadoPortafolio = "ACT".equals(req.IdEstadoPortafolio) || "VIG".equals(req.IdEstadoPortafolio) ? "PEN".equals(req.IdEstadoAutorizacion) || "".equals(req.IdEstadoAutorizacion) ? "PEN" : req.IdEstadoPortafolio : "BLO";
					if(req.IdPortafolio != null && !(req.IdPortafolio > 0L && "ACT".equals(req.IdEstadoPortafolio))){
						String msjError = "";
						switch(req.IdEstadoPortafolio){
							case "BLO": msjError = "SPOT_PORTAFOLIO_BLOQUEADO"; break;
							case "PEN": msjError = "SPOT_PORTAFOLIO_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "SPOT_PORTAFOLIO_NO_HABILITADO"; break;
						}
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						mapMsg.put("DocumentoIdentidad", req.DocumentoIdentidad);
						mapMsg.put("CodigoPortafolio", req.CodigoPortafolio);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(!"ACT".equals(req.IdEstadoCuentaInversionMonedaPrincipal)){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"MONEDA_PRINCIPAL_NO_HABILITADA_CUENTA_INVERSION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(!"ACT".equals(req.IdEstadoCuentaInversionMonedaSecundaria)){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"MONEDA_SECUNDARIA_NO_HABILITADA_CUENTA_INVERSION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if("NOK".equals(req.ResultadoPerfilProducto)){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,req.MensajePerfilProducto,mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if("S".equals(validaPlantillaEmpleado) && "N".equals(req.PlantillaEmpleadoProductoHabilitado) && !"FMM".equals(in.p_IdModulo)){
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"PRODUCTO_NO_HABILITADO_EMPLEADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if (Long.valueOf(0L).equals(req.IdEmpleadoDigitador) && "S".equals(in.p_EsIngresoWS)) {
					    mapMsg.put("NumeroOperacion", req.NumeroOperacion);
					    out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe, MensajeSalida.MENSAJE_ERROR, "CODIGO_EJECUTIVO_DIGITADOR_NO_EXISTE", mapMsg, conn, in.p_IdEmpresa));
					    Id++;
					    entraIF = true;
					}
					if(entraIF){
						entraIF = false;
						
						ope.NumeroOperacion	= req.NumeroOperacion;
						ope.Estado			= "ERR";
						operacionesInErrValidaciones.add(ope);
					}
					//Si la pauta esta en ejecucion se cambia la fecha sistema al dia siguiente (antes que se ejecute el proceso de cambio de fecha)
					if("S".equals(pautaEnEjecucion) && !"TER".equals(estadoProcesoCambioFecha)) {
						ObtenerDiaSiguiente diaSiguiente = new ObtenerDiaSiguiente();
						InObtenerDiaSiguiente diaSiguienteIn = new InObtenerDiaSiguiente();
						diaSiguienteIn.p_idPais = idPaisSistema;
						diaSiguienteIn.p_diaHabil = "S";
						FechasIn fecha = new FechasIn();
						fecha.fechaInicial = fechaSistema;
						fecha.numeroDias = 1L;
						fecha.pais = idPaisSistema;
						diaSiguienteIn.p_fechas.add(fecha);
						OutObtenerDiaSiguiente salDiaSiguiente = diaSiguiente.logica(conn, diaSiguienteIn);
						
						String fechaSistemaLocal= FechaDB.ObtenerFechaHoraSistemaLocal(conn, in.p_IdEmpresa).substring(0,10);
						//Se actualiza la fecha sistema'
						logger.debug(fechaSistema);
						logger.debug(fechaSistemaLocal);
						if(fechaSistema.compareTo(fechaSistemaLocal) <=0 ){
							fechaSistemaNueva = salDiaSiguiente.p_out_fechas.get(0).fechaCalculada;
							fechaHoraSistemaSecondateNueva = Fecha.toStringDatetime(Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistemaNueva + " 00:00:01")));
						}
						else {
							fechaSistemaNueva = fechaSistema;	
							fechaHoraSistemaSecondateNueva = fechaHoraSistemaSecondate;
						}
					}
					logger.info("BuscandoElBug - req.FechaOperacion (12): " + req.FechaOperacion + " - Solicitud FFMM ID" + req.IdSolicitudAporteRescate);
					
				}
				
				if("S".equals(pautaEnEjecucion) && !"TER".equals(estadoProcesoCambioFecha)) {
					for(OperacionObjIn ope : in.p_DatosOperacion) {
						ope.FechaOperacion = Fecha.toDateTime(fechaHoraSistemaSecondateNueva);
						logger.info("BuscandoElBug - ope.FechaOperacion (13): " + ope.FechaOperacion + " - Solicitud FFMM ID" + ope.IdSolicitudAporteRescate);
					}
					for(FormaPagoObjIn fpag : in.p_DatosFormaPago)
						fpag.FechaLiquidacion = Fecha.toDate(fechaSistemaNueva);
				}
				
				//VAlidacion de Carga MAsiva
				if("S".equals(in.p_EsCargaMasiva)) {
					Boolean entraIF = false;
					for(OperacionObjIn ope : in.p_DatosOperacion){
						if(!(Fecha.toStringDate(ope.FechaOperacion).equals(fechaSistema) )){
							mapMsg.put("NumeroOperacion", ope.NumeroOperacion);
							out.P_OUT_MENSAJES.add(new MensajeSalida(Long.parseLong(ope.NumeroOperacion),MensajeSalida.MENSAJE_ERROR,"FECHA_PROCESO_INVALIDA",mapMsg,conn,in.p_IdEmpresa));
							Id++;
							entraIF = true;
						}
						if (ope.CodigoPortafolio.contains(String.valueOf("APV"))) {
							if("".equals(ope.ProductoAgrupador)|| ope.ProductoAgrupador == null)
							{
								out.P_OUT_MENSAJES.add(new MensajeSalida (Long.parseLong(ope.NumeroOperacion),MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_APV_REQUIERE_PRODUCTO_AGRUPADOR",mapMsg,conn, in.p_IdEmpresa));
								Id++;
								entraIF = true;
							}
						}
						if(entraIF){
							entraIF = false;
							ope.Estado			= "ERR";
							operacionesInErrValidaciones.add(ope);
						}
					}
				}

				//Normalizacion y validacion PlataformaNegociacion
				String plataforma = (in.p_DatosOperacion.get(0).IdPlataformaNegociacion == null) ? "OTC" : in.p_DatosOperacion.get(0).IdPlataformaNegociacion.trim().toUpperCase();
				in.p_DatosOperacion.get(0).IdPlataformaNegociacion = plataforma;
				if ("OTC".equals(plataforma)) {
					in.p_DatosOperacion.get(0).IdPlataformaNegociacion = "OTC";
				} else {		
					if (plataformasValidas == null || plataformasValidas.isEmpty()) {
						out.P_OUT_ERRORESCONTADOR++;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_VALIDACION_DICCIONARIOS", "No se pudo validar la plataforma de negociacion."));
						logger.error("No se pudo validar diccionario de plataformas de negociacion.");
					} else {
						boolean existe = plataformasValidas.stream().anyMatch(d -> d.Codigo != null && d.Codigo.equalsIgnoreCase(plataforma));
						if (!existe) {
							out.P_OUT_ERRORESCONTADOR++;
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_VALIDACION_PLATAFORMA_NEGOCIACION","La plataforma de negociacion ingresada no existe o no esta activa."));
							logger.error("Plataforma de negociacion no valida: " + plataforma);
						}
					}
				}
				
				//VALIDACION FORMAS DE PAGO
				if(in.p_DatosFormaPago.isEmpty() && "MSP".equalsIgnoreCase(in.p_IdModulo)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_SPOT_NO_TIENE_FOMA_PAGO",mapMsg,conn,in.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return out;
				}
				
				params.clear();
				params.put("NumeroOperacion", numeroOperacion);
				
				//VALIDACIONES PARIDAD Y TC
				logger.debug("INICIO::SQL_SELECT_LIMITES_PARIDAD_Y_TIPO_CAMBIO");
				List<LimitesParidadYTipoCambio> limitesParidadYTipoCambio = _sql.query(conn, SQL_SELECT_LIMITES_PARIDAD_Y_TIPO_CAMBIO, params, LimitesParidadYTipoCambio.class);
				logger.debug("FIN::SQL_SELECT_LIMITES_PARIDAD_Y_TIPO_CAMBIO");
				
				for(LimitesParidadYTipoCambio ptc: limitesParidadYTipoCambio){
					OperacionObjIn ope = new OperacionObjIn();
					OperacionSpotEventoAutorizacion evt = new OperacionSpotEventoAutorizacion();
					boolean entraIF = false;
					Long idNumOpe = Long.parseLong(ptc.NumeroOperacion);
					BigDecimal limiteDefault = BigDecimal.valueOf(10L);//Este dato va en duro ya que en el mantenedor de puntas esta igual

					if(ptc.IdMonedaSecundaria.equals("CLP") && (ptc.TipoCambioCierre.compareTo(ptc.LimiteSuperiorTC) > 0 || ptc.TipoCambioCierre.compareTo(ptc.LimiteInferiorTC) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						MensajeSalida mensaje= new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_AUTORIZACION,"TC_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa);
						mensaje.descripcion = mensaje.descripcion 
								+ " ( "+MonedaFormato.formatear(conn, ptc.LimiteInferiorTC, MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor
								+", "+ MonedaFormato.formatear(conn, ptc.LimiteSuperiorTC, MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor +" )";
						
						out.P_OUT_MENSAJES.add(mensaje);
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LITCSP";
						evt.Glosa = "TC_FUERA_LIMITES";
					}
					if(ptc.Nemotecnico == null){
						ptc.Nemotecnico = "";
					}
					if(ptc.IdMonedaSecundaria.equals("CLP") && ptc.Nemotecnico.equals("DISTRI") && (ptc.TipoCambioCosto.compareTo(ptc.LimiteSuperiorTC) > 0 || ptc.TipoCambioCosto.compareTo(ptc.LimiteInferiorTC) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						MensajeSalida mensaje= new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_AUTORIZACION,"TC_COSTO_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa);
						mensaje.descripcion = mensaje.descripcion 
								+ " ( "+MonedaFormato.formatear(conn, ptc.LimiteInferiorTC, MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor
								+", "+ MonedaFormato.formatear(conn, ptc.LimiteSuperiorTC, MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor +" )";
						
						out.P_OUT_MENSAJES.add(mensaje);
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LITCSP";
						evt.Glosa = "TC_FUERA_LIMITES";
					}
					if(!ptc.IdMonedaSecundaria.equals("CLP") && (ptc.ParidadCierre.compareTo(ptc.LimiteSuperiorPAR) > 0 || ptc.ParidadCierre.compareTo(ptc.LimiteInferiorPAR) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_AUTORIZACION,"PARIDAD_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LIPASP";
						evt.Glosa = "PARIDAD_FUERA_LIMITES";
					}
					if(!ptc.IdMonedaSecundaria.equals("CLP") && ptc.Nemotecnico.equals("ARBDIS") && (ptc.ParidadCosto.compareTo(ptc.LimiteSuperiorPAR) > 0 || ptc.ParidadCosto.compareTo(ptc.LimiteInferiorPAR) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_AUTORIZACION,"PARIDAD_COSTO_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LIPASP";
						evt.Glosa = "PARIDAD_FUERA_LIMITES";
					}
					
					if(entraIF){
						entraIF = false;
						
						ope.NumeroOperacion	= ptc.NumeroOperacion;
						ope.Estado			= "PEN";
						operacionesInUpdateEstado.add(ope);
						operacionesInPenValidaciones.add(ope);
						
						evt.NumeroOperacion = ptc.NumeroOperacion;
						evtList.add(evt);
					}
				}
				
				
				//VALIDACIONES LIMITES TIPO CAMBIO Y COSTO PARA AGENTE
				params.put("IdEmpresa", in.p_IdEmpresa);
				logger.debug("INICIO::SQL_SELECT_PUNTAS");
				List<Puntas> limitesTCAgente = _sql.query(conn, SQL_SELECT_PUNTAS, params, Puntas.class);
				logger.debug("FIN::SQL_SELECT_PUNTAS");
				
				ConfiguracionMesa limiteSuperiorConfiguracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "LIMITE_SUPERIOR_SPOT_MESA");
				BigDecimal limiteSuperior =convertirCentavosAPesosChilenos(new BigDecimal(limiteSuperiorConfiguracionMesa.Valor));
				
				ConfiguracionMesa limiteInferiorConfiguracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "LIMITE_INFERIOR_SPOT_MESA");
				BigDecimal limiteInferior =convertirCentavosAPesosChilenos(new BigDecimal(limiteInferiorConfiguracionMesa.Valor));
				
				for(Puntas ptc: limitesTCAgente){
					OperacionObjIn ope = new OperacionObjIn();
					OperacionSpotEventoAutorizacion evt = new OperacionSpotEventoAutorizacion();
					boolean entraIF = false;
					Long idNumOpe = Long.parseLong(ptc.NumeroOperacion);
					
					
					if(ptc.IdMonedaSecundaria.equals("CLP") 
							&& (ptc.IdTipoOperacion.equals("VENSPT")) 
							&& (ptc.TipoCambioCierre.compareTo(ptc.ValorVenta.add(limiteSuperior)) > 0 || ptc.TipoCambioCierre.compareTo(ptc.ValorVenta.subtract(limiteInferior)) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						MensajeSalida mensaje= new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_AUTORIZACION,"TC_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa);
						mensaje.descripcion = mensaje.descripcion 
								+ " ( "+MonedaFormato.formatear(conn, ptc.ValorVenta.subtract(limiteInferior), MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor
								+", "+ MonedaFormato.formatear(conn, ptc.ValorVenta.add(limiteSuperior), MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor +" )";
						
						out.P_OUT_MENSAJES.add(mensaje);
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LITCSP";
						evt.Glosa = "TC_FUERA_LIMITES";
					}
					if(ptc.IdMonedaSecundaria.equals("CLP") 
							&& (ptc.IdTipoOperacion.equals("COMSPT")) 
							&& (ptc.TipoCambioCierre.compareTo(ptc.ValorCompra.add(limiteSuperior)) > 0 || ptc.TipoCambioCierre.compareTo(ptc.ValorCompra.subtract(limiteInferior)) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						MensajeSalida mensaje= new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_AUTORIZACION,"TC_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa);
						mensaje.descripcion = mensaje.descripcion 
								+ " ( "+MonedaFormato.formatear(conn, ptc.ValorCompra.subtract(limiteInferior), MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor
								+", "+ MonedaFormato.formatear(conn, ptc.ValorCompra.add(limiteSuperior), MonedaFormato.PRECIO, ptc.IdMonedaPrincipal, "", "").valor +" )";
						
						out.P_OUT_MENSAJES.add(mensaje);
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LITCSP";
						evt.Glosa = "TC_FUERA_LIMITES";
					}
				}
				//VALIDACIONES SALDO FLUJO CAJA
				HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
				List<String> numeroOperacionFlujoCaja = in.p_DatosOperacion.parallelStream().filter(operacion->!_c.AporteReinversion.equals(operacion.TipoSolicitud))
																							.filter(operacion->("S").equals(operacion.ValidaFlujoCaja))
																							.map(operacion->operacion.NumeroOperacion).collect(Collectors.toList());
				params.clear();
				params.put("NumeroOperacion", numeroOperacionFlujoCaja);
				logger.debug("INICIO::SQL_SELECT_PORTAFOLIOS_FLUJO_CAJA");
				List<PortafoliosFlujoCaja> selectPortafoliosFlujoCaja = _sql.query(conn, SQL_SELECT_PORTAFOLIOS_FLUJO_CAJA, params, PortafoliosFlujoCaja.class);
				logger.debug("FIN::SQL_SELECT_PORTAFOLIOS_FLUJO_CAJA");
				
				//Obtencion fecha maxima flujo caja
				Date fechaMaximaLiquidacion = Fecha.toDate(fechaSistema);
				for(FormaPagoObjIn frmPago : in.p_DatosFormaPago){
					Date fechaLiqSol = frmPago.FechaLiquidacion == null ? fechaMaximaLiquidacion : frmPago.FechaLiquidacion;//liquidacionPorConfirmar.equals("S") ? "PEN" : "CNF";
					if(fechaLiqSol.compareTo(fechaMaximaLiquidacion) == 1){
						fechaMaximaLiquidacion = fechaLiqSol;
					}
				}
				for(PortafoliosFlujoCaja pfc: selectPortafoliosFlujoCaja){
					SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
					port.IdPortafolio = pfc.IdPortafolio;
					port.IdMoneda = pfc.IdMoneda;
					portafoliosFlujoCaja.add(port);
				}
				
				if(portafoliosFlujoCaja.size() > 0){
					SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
			    	entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
					entradaFlujoCaja.P_FECHADESDE = fechaSistema;
					entradaFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaMaximaLiquidacion);
					entradaFlujoCaja.P_USO = "FMM".equalsIgnoreCase(in.p_IdModulo) ? "INGRESO_OPERACION_SPOT_FFMM" : "INGRESO_OPERACION_SPOT";
					entradaFlujoCaja.P_DESPLEGARENUI = "N";
					entradaFlujoCaja.P_AGRUPARPORORIGEN = "N";
					entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
					
					SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
					SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(conn,entradaFlujoCaja);
					Map<String, List<FormaPagoObjIn>> hashFormaPago = in.p_DatosFormaPago.parallelStream().filter(c -> "P".equalsIgnoreCase(c.RecibePagaPortafolio)).collect(Collectors.groupingBy(ope->ope.IdOperacion.toString()));
					for(ObjFLUJOCAJA flj: salidaFlujoCaja.P_OUT_FLUJOCAJA){
						for(PortafoliosFlujoCaja pfc: selectPortafoliosFlujoCaja){
							OperacionObjIn ope = new OperacionObjIn();
							Long idNumOpe = Long.parseLong(pfc.NumeroOperacion);
							boolean entraIF = false;
							//Se obtiene fecha liquidacion spot
							String fechaLiqSpotIn = fechaSistema;
							List<FormaPagoObjIn> listNumOperacion = hashFormaPago.get(pfc.NumeroOperacion);
							if(listNumOperacion.size() > 0) {
								if( listNumOperacion.get(0).FechaLiquidacion != null)
								{
									if( listNumOperacion.get(0).FechaLiquidacion.after(Fecha.toDateTime(fechaLiqSpotIn)))
										fechaLiqSpotIn = listNumOperacion.get(0).FechaLiquidacion.toString(); 
								}
							}
				        	if(flj.IdPortafolio.equals(pfc.IdPortafolio) && flj.IdMoneda.equals(pfc.IdMoneda) && flj.FechaSaldo.equals(fechaLiqSpotIn) && pfc.Monto.compareTo(flj.Saldo) > 0 ){
				        		for(FormaPagoObjIn frmp : in.p_DatosFormaPago){
				        			if(frmp.IdOperacion.equals(idNumOpe)) {	
					        			if(frmp.RecibePagaPortafolio.equalsIgnoreCase("R") && frmp.IdFormaDePago.contains("_CIN_")){
					        				ClientePortafolioSolicitudes clientes = (ClientePortafolioSolicitudes) hclientes.get(pfc.IdPortafolio);
					        				
					        				mapMsg.put("NombreCompleto",clientes.NombreCompleto);
					        				mapMsg.put("DocumentoIdentidad",clientes.DocumentoIdentidad);
							        		mapMsg.put("NumeroOperacion", pfc.NumeroOperacion);
											out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO",mapMsg,conn,in.p_IdEmpresa));
											Id++;
											entraIF = true;
					        			}
				        			}
				        		}
				        	}
				        	
				        	
				        	if(entraIF){
								entraIF = false;
								ope.NumeroOperacion	= pfc.NumeroOperacion;
								ope.Estado			= "ERR";
								operacionesInErrSaldoFlujoCa.add(ope);
							}
				        }
					}
				}
				
			}
			
			if("N".equals(in.p_SimularOperacion)) {
				//Setea estado de las operaciones de entrada para continuar solo con las operaciones sin error
				for(OperacionObjIn ope: in.p_DatosOperacion) {
					for(OperacionObjIn opePen: operacionesInPenValidaciones) {
						if(ope.NumeroOperacion.equals(opePen.NumeroOperacion)) {
							ope.Estado = "PEN";
						}
					System.out.println(ope.IdContacto);
					}
					for(OperacionObjIn opeErr: operacionesInErrValidaciones) {
						if(ope.NumeroOperacion.equals(opeErr.NumeroOperacion)) {
							ope.Estado = "ERR";
						}
					}
					for(OperacionObjIn opeErr: operacionesInErrSaldoFlujoCa) {
						if(ope.NumeroOperacion.equals(opeErr.NumeroOperacion)) {
							ope.Estado = "ERR";
						}
					}
					logger.info("BuscandoElBug - ope.FechaOperacion (14): " + ope.FechaOperacion + " - Solicitud FFMM ID" + ope.IdSolicitudAporteRescate);
				}
				
				numeroOperacion = new ArrayList<String>();
				for(OperacionObjIn ope: in.p_DatosOperacion) {
					if("ERR".equals(ope.Estado)) {
						cantOpeErr++;
					}
					else if("VIG".equals(ope.Estado) || "PEN".equals(ope.Estado)) {
						//Agrega las operaciones sin Error
						numeroOperacion.add(ope.NumeroOperacion);
					}
					logger.info("BuscandoElBug - ope.FechaOperacion (15): " + ope.FechaOperacion + " - Solicitud FFMM ID" + ope.IdSolicitudAporteRescate);
				}
				
				//Retorna si existen errores en todas las operaciones de entrada
				logger.debug("Cantidad de errores en out.P_OUT_MENSAJES :: " + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES));
				if (cantOpeErr.longValue() != 0L && cantOpeErr.longValue() == cantOpeIn.longValue()){
					return out;
				}
				
				
				/***** CALCULOS NEGOCIO *****/
				
				//CALCULAR MARGEN
				if(in.p_CalculaMargen.equals("S")){
					String Canal = "Punta";
					for(OperacionObjIn ope : in.p_DatosOperacion)
						if("AGE".equalsIgnoreCase(ope.IdTipoCanalIngreso))
							Canal = "Agente";
						else if("CLI".equalsIgnoreCase(ope.IdTipoCanalIngreso))
							Canal = "Cliente";

					params.clear();
					params.put("IdEmpresa", in.p_IdEmpresa);
					params.put("MonedaLocal", monedaSistema);
					params.put("NumeroOperacion", numeroOperacion);
					params.put("Canal", Canal);
					logger.debug("INICIO::SQL_SELECT_PUNTAS_CALCULAR_MARGEN");
					List<PuntasCalcularMargen> puntasCalcularMargen = _sql.query(conn, SQL_SELECT_PUNTAS_CALCULAR_MARGEN, params, PuntasCalcularMargen.class);
					logger.debug("FIN::SQL_SELECT_PUNTAS_CALCULAR_MARGEN");
					for (PuntasCalcularMargen puntas: puntasCalcularMargen){
						OperacionObjIn ope = new OperacionObjIn();
						
						if(puntas.IdTipoOperacion.equals("COMSPT") && puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB") && !puntas.Nemotecnico.equals("ARBINT")){
							if(puntas.CheckPrecioPorTramo.equals("SI")) {
									puntas.Margen = (puntas.ValorCompra.subtract(puntas.TipoCambioCierre)).multiply(puntas.MontoMonedaPrincipal);
									ope.TipoCambioCosto = puntas.ValorCompra;
							} else {
									puntas.Margen = (puntas.PuntaCompra.subtract(puntas.TipoCambioCierre)).multiply(puntas.MontoMonedaPrincipal);
									ope.TipoCambioCosto = puntas.PuntaCompra;
							}
						}
						if(puntas.IdTipoOperacion.equals("COMSPT") && !puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB")){
							if(!(puntas.ParidadCosto.compareTo(BigDecimal.ZERO) == 0))
									puntas.Margen = (puntas.ParidadCosto.subtract(puntas.ParidadCierre)).multiply(puntas.MontoMonedaPrincipal);
							else	puntas.Margen = (puntas.ParidadMercado.subtract(puntas.ParidadCierre)).multiply(puntas.MontoMonedaPrincipal);
							
							ope.TipoCambioCosto = puntas.ValorCompra;
						}
						if(puntas.IdTipoOperacion.equals("VENSPT") && puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB") && !puntas.Nemotecnico.equals("ARBINT")){
							if(puntas.CheckPrecioPorTramo.equals("SI")) {
									puntas.Margen = (puntas.TipoCambioCierre.subtract(puntas.ValorVenta)).multiply(puntas.MontoMonedaPrincipal);
									ope.TipoCambioCosto = puntas.ValorVenta;
							} else {
									puntas.Margen = (puntas.TipoCambioCierre.subtract(puntas.TipoCambioCosto)).multiply(puntas.MontoMonedaPrincipal);
								    ope.TipoCambioCosto = puntas.PuntaVenta;
							}
						}
						if(puntas.IdTipoOperacion.equals("VENSPT") && !puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB")){
							if(!(puntas.ParidadCosto.compareTo(BigDecimal.ZERO) == 0))
									puntas.Margen = (puntas.ParidadCierre.subtract(puntas.ParidadCosto)).multiply(puntas.MontoMonedaPrincipal);
							else	puntas.Margen = (puntas.ParidadCierre.subtract(puntas.ParidadMercado)).multiply(puntas.MontoMonedaPrincipal);
							
							ope.TipoCambioCosto = puntas.ValorVenta;
						}
						ope.NumeroOperacion	= puntas.NumeroOperacion;
						ope.Margen			= puntas.Margen;
						ope.Utilidad		= puntas.Margen;
						ope.PuntaCompra		= puntas.PuntaCompra;
						ope.PuntaVenta		= puntas.PuntaVenta;
						operacionesInUpdateMargen.add(ope);
					}
				}
				
				
				//CALCULO DE CAMPOS POR INSTRUMENTO
				List<Long> secuenciaIdOperacionSpot = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::SPT.IdOperacionSpot",cantOpeIn);
				List<Long> secuenciaFolioPlanillaSpot = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::SPT.FolioPlanillaSpot",cantOpeIn);
				int i = 0;
				
				params.clear();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdUsuario", in.p_IdUsuario);
				params.put("MonedaLocal", monedaSistema);
				params.put("MonedaEquivalente", monedaEquivalente);
				params.put("FechaHoraSistema", fechaHoraSistemaSecondate);
				params.put("NumeroOperacion", numeroOperacion);
				
				//INTERBANCARIO
				logger.debug("INICIO::SQL_SELECT_OPERACIONES_SPOT_INTERBANCARIO");
				List<OperacionSpot> selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_INTERBANCARIO, params, OperacionSpot.class);
				if(!parametroFacInterbSpot.equalsIgnoreCase("S")) {
					selectOpeSpotInstrumento.forEach(element ->{
						if(element.Nemotecnico.equalsIgnoreCase("INTERB")) {
							element.EstadoConfirmacion="CNF";
						}
					});
				}
				logger.debug("FIN::SQL_SELECT_OPERACIONES_SPOT_INTERBANCARIO");
				
				for(OperacionSpot opSpIns: selectOpeSpotInstrumento){
					logger.info("BuscandoElBug - opSpIns.FechaOperacion (16): " + opSpIns.FechaOperacion + " - Solicitud FFMM ID" + opSpIns.IdSolicitudAporteRescate);
					OperacionSpot opeSpotProceso = copyPojoOperacionSpot(opSpIns);
					logger.info("BuscandoElBug - opeSpotProceso.FechaOperacion (17): " + opeSpotProceso.FechaOperacion + " - Solicitud FFMM ID" + opeSpotProceso.IdSolicitudAporteRescate);
					
					opeSpotProceso.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
					opeSpotProceso.Nemotecnico = opSpIns.Nemotecnico;
					
					OperacionObjIn operacion = in.p_DatosOperacion.stream().filter(ope->ope.NumeroOperacion.equals(opeSpotProceso.NumeroOperacion)).findFirst().orElse(new OperacionObjIn());
					opeSpotProceso.IdContacto = operacion.IdContacto;
					opeSpotProceso.TipoDocumento = operacion.TipoDocumento;
					
					if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal;
					}
					else if(opSpIns.IdMonedaSecundaria.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaSecundaria;
					}
					else if(!opSpIns.IdMonedaPrincipal.equals(monedaEquivalente) && opSpIns.IdMonedaSecundaria.equals(monedaSistema)){
						if(opSpIns.MultiplicaODivide.equals("D") && !(opSpIns.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP);
						}
						else if(opSpIns.MultiplicaODivide.equals("M")){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.ParidadMercado);
						}
					}
					
					if(opSpIns.IdTipoOperacion.equals("COMSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "VENSPT" : "COMSPT";
						
						if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
							opeSpotProceso.ResultadoMonedaLocalPuntas = (opSpIns.PuntaCompra.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal);
							opeSpotProceso.ResultadoMonedaLocalBidAsk = (opSpIns.PuntaBid.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal);
							opeSpotProceso.ResultadoMonedaLocalDO = (opSpIns.ValorDolarObservado.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal);
							opeSpotProceso.ResultadoMonedaLocalTCSpot = (opSpIns.TipoCambioUSD.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal);
						}
						else{
							opeSpotProceso.ResultadoMonedaLocalPuntas = (opSpIns.PuntaCompra.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							opeSpotProceso.ResultadoMonedaLocalBidAsk = (opSpIns.PuntaBid.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							opeSpotProceso.ResultadoMonedaLocalDO = (opSpIns.ValorDolarObservado.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							opeSpotProceso.ResultadoMonedaLocalTCSpot = (opSpIns.TipoCambioUSD.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
						}
						
						//IMPCREDOPT-2549 campo ResultadoMonedaLocalTCCierreMesa se debe actualizar una vez cerrada la mesa
						//Compra && Existe PosicionCierre
						if(!(opSpIns.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0)){
							opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = (opSpIns.CierreIngresadoPosMonPri.subtract(opSpIns.TipoCambioCierre)).multiply(opSpIns.MontoMonedaPrincipal);
						}
						//Compra && NO existe PosicionCierre para la monedaExtranjera
						else if(opSpIns.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0 && !(opSpIns.CierreIngresadoPosMonEqui.compareTo(BigDecimal.ZERO) == 0)){
							if(opSpIns.ParidadBid.compareTo(BigDecimal.ZERO) == 0){
								opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = BigDecimal.ZERO;
							}
							else if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
								opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = (opSpIns.CierreIngresadoPosMonEqui.subtract(opSpIns.TipoCambioCierre.divide(opSpIns.ParidadBid,8,RoundingMode.HALF_UP))).multiply(opSpIns.MontoMonedaPrincipal);
							}
							else{
								opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = (opSpIns.CierreIngresadoPosMonEqui.subtract(opSpIns.TipoCambioCierre.divide(opSpIns.ParidadBid,8,RoundingMode.HALF_UP))).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							}
						}
						
						if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
							opeSpotProceso.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
						}
						else{
							if(opSpIns.ParidadBid.compareTo(BigDecimal.ZERO) == 0 ){
								opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.TipoCambioCierre.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);								
							}else if(opSpIns.MultiplicaODivide.equals("M")){
								//si se utiliza la paridad BID se debe considerar si moneda fuerte o moneda debil
								//MultiplicaODivide = M significa que es moneda Fuerte y se divide, si MultiplicaODivide = D significa que es debil y se multiplica
								opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.TipoCambioCierre.divide(opSpIns.ParidadBid,8,RoundingMode.HALF_UP).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);

							}else {
								opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.TipoCambioCierre.multiply(opSpIns.ParidadBid).multiply(opSpIns.MontoMonedaPrincipal.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP));
								
							}
						}
					}
					else if(opSpIns.IdTipoOperacion.equals("VENSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "COMSPT" : "VENSPT";
						
						if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
							opeSpotProceso.ResultadoMonedaLocalPuntas = (opSpIns.TipoCambioCierre.subtract(opSpIns.PuntaVenta)).multiply(opSpIns.MontoMonedaPrincipal);
							opeSpotProceso.ResultadoMonedaLocalBidAsk = (opSpIns.TipoCambioCierre.subtract(opSpIns.PuntaAsk)).multiply(opSpIns.MontoMonedaPrincipal);
							opeSpotProceso.ResultadoMonedaLocalDO = (opSpIns.TipoCambioCierre.subtract(opSpIns.ValorDolarObservado)).multiply(opSpIns.MontoMonedaPrincipal);
							opeSpotProceso.ResultadoMonedaLocalTCSpot = (opSpIns.TipoCambioCierre.subtract(opSpIns.TipoCambioUSD)).multiply(opSpIns.MontoMonedaPrincipal);
							opeSpotProceso.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
						}
						else{
							opeSpotProceso.ResultadoMonedaLocalPuntas = (opSpIns.TipoCambioCierre.subtract(opSpIns.PuntaVenta)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							opeSpotProceso.ResultadoMonedaLocalBidAsk = (opSpIns.TipoCambioCierre.subtract(opSpIns.PuntaAsk)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							opeSpotProceso.ResultadoMonedaLocalDO = (opSpIns.TipoCambioCierre.subtract(opSpIns.ValorDolarObservado)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							opeSpotProceso.ResultadoMonedaLocalTCSpot = (opSpIns.TipoCambioCierre.subtract(opSpIns.TipoCambioUSD)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							
							
							if(opSpIns.ParidadAsk.compareTo(BigDecimal.ZERO) == 0 ){
								opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.TipoCambioCierre.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
								
							}else if(opSpIns.MultiplicaODivide.equals("M")){
								//si se utiliza la paridad ASK se debe considerar si moneda fuerte o moneda debil
								//MultiplicaODivide = M significa que es moneda Fuerte y se divide, si MultiplicaODivide = D significa que es debil y se multiplica
								opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.TipoCambioCierre.divide(opSpIns.ParidadAsk,8,RoundingMode.HALF_UP).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
								
							}else {
								opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.TipoCambioCierre.multiply(opSpIns.ParidadAsk).multiply(opSpIns.MontoMonedaPrincipal.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP));
								
							}
						}
						
						//IMPCREDOPT-2549 campo ResultadoMonedaLocalTCCierreMesa se debe actualizar una vez cerrada la mesa
						//Venta && Existe PosicionCierre
						if(!(opSpIns.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0)){
							opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = (opSpIns.TipoCambioCierre.subtract(opSpIns.CierreIngresadoPosMonPri)).multiply(opSpIns.MontoMonedaPrincipal);
						}
						//Venta && NO existe PosicionCierre para la monedaExtranjera
						else if(opSpIns.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0 && !(opSpIns.CierreIngresadoPosMonEqui.compareTo(BigDecimal.ZERO) == 0)){
							if(opSpIns.ParidadAsk.compareTo(BigDecimal.ZERO) == 0){
								opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = BigDecimal.ZERO;
							}
							else if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
								opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = ((opSpIns.TipoCambioCierre.divide(opSpIns.ParidadAsk,8,RoundingMode.HALF_UP)).subtract(opSpIns.CierreIngresadoPosMonEqui)).multiply(opSpIns.MontoMonedaPrincipal);
							}
							else{
								opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = ((opSpIns.TipoCambioCierre.divide(opSpIns.ParidadAsk,8,RoundingMode.HALF_UP)).subtract(opSpIns.CierreIngresadoPosMonEqui)).multiply(opSpIns.MontoMonedaPrincipal).multiply(opSpIns.ParidadMercado);
							}
						}
					}
					opeSpotProceso.FolioPlanilla = secuenciaFolioPlanillaSpot.get(i);
					opeSpotProceso.IdTipoInstrumento = opSpIns.IdTipoInstrumento;
					opeSpotProceso.IdEmpleadoDigitador = opSpIns.IdEmpleadoDigitador;
					i++;
					operacionesSpot.add(opeSpotProceso);
					logger.info("BuscandoElBug - opeSpotProceso.FechaOperacion (18): " + opeSpotProceso.FechaOperacion + " - Solicitud FFMM ID" + opeSpotProceso.IdSolicitudAporteRescate);
				}
				
				
				//ARBITRAJE INTERBANCARIO
				logger.debug("INICIO::SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_INTERBANCARIO");
				selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_INTERBANCARIO, params, OperacionSpot.class);
				logger.debug("FIN::SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_INTERBANCARIO");
				
				for(OperacionSpot opSpIns: selectOpeSpotInstrumento){
					logger.info("BuscandoElBug - opSpIns.FechaOperacion (19): " + opSpIns.FechaOperacion + " - Solicitud FFMM ID" + opSpIns.IdSolicitudAporteRescate);
					OperacionSpot opeSpotProceso = copyPojoOperacionSpot(opSpIns);
					logger.info("BuscandoElBug - opeSpotProceso.FechaOperacion (20): " + opeSpotProceso.FechaOperacion + " - Solicitud FFMM ID" + opeSpotProceso.IdSolicitudAporteRescate);
					
					opeSpotProceso.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
					opeSpotProceso.Nemotecnico = opSpIns.Nemotecnico;
					
					OperacionObjIn operacion = in.p_DatosOperacion.stream().filter(ope->ope.NumeroOperacion.equals(opeSpotProceso.NumeroOperacion)).findFirst().orElse(new OperacionObjIn());
					opeSpotProceso.IdContacto = operacion.IdContacto;
					opeSpotProceso.TipoDocumento = operacion.TipoDocumento;
					if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal;
					}
					else if(opSpIns.IdMonedaSecundaria.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaSecundaria;
					}
					else if(!opSpIns.IdMonedaPrincipal.equals(monedaEquivalente) && opSpIns.IdMonedaSecundaria.equals(monedaSistema)){
						if(opSpIns.MultiplicaODivide.equals("D") && !(opSpIns.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP);
						}
						else if(opSpIns.MultiplicaODivide.equals("M")){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.ParidadMercado);
						}
					}
					
					if(opSpIns.IdTipoOperacion.equals("COMSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "VENSPT" : "COMSPT";
						
						opeSpotProceso.ResultadoMonedaLocalPuntas = opSpIns.Margen.multiply(opSpIns.PuntaCompra);
						opeSpotProceso.ResultadoMonedaLocalBidAsk = opSpIns.Margen.multiply(opSpIns.PuntaBid);
						opeSpotProceso.MargenBidAsk = (opSpIns.ParidadBid.subtract(opSpIns.ParidadCierre)).multiply(opSpIns.MontoMonedaPrincipal);
					}
					else if(opSpIns.IdTipoOperacion.equals("VENSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "COMSPT" : "VENSPT";
						
						opeSpotProceso.ResultadoMonedaLocalPuntas = opSpIns.Margen.multiply(opSpIns.PuntaVenta);
						opeSpotProceso.ResultadoMonedaLocalBidAsk = opSpIns.Margen.multiply(opSpIns.PuntaAsk);
						opeSpotProceso.MargenBidAsk = (opSpIns.ParidadCierre.subtract(opSpIns.ParidadAsk)).multiply(opSpIns.MontoMonedaPrincipal);
					}
					opeSpotProceso.ResultadoMonedaLocalDO = opSpIns.Margen.multiply(opSpIns.ValorDolarObservado);
					opeSpotProceso.ResultadoMonedaLocalTCSpot = opSpIns.Margen.multiply(opSpIns.TipoCambioUSD);
					opeSpotProceso.FolioPlanilla = secuenciaFolioPlanillaSpot.get(i);
					opeSpotProceso.IdEmpleadoDigitador = opSpIns.IdEmpleadoDigitador;
					i++;
					operacionesSpot.add(opeSpotProceso);
					logger.info("BuscandoElBug - opeSpotProceso.FechaOperacion (21): " + opeSpotProceso.FechaOperacion + " - Solicitud FFMM ID" + opeSpotProceso.IdSolicitudAporteRescate);
				}
				
				
				//DISTRIBUCION
				logger.debug("INICIO::SQL_SELECT_OPERACIONES_SPOT_DISTRIBUCION");
				selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DISTRIBUCION, params, OperacionSpot.class);
				logger.debug("FIN::SQL_SELECT_OPERACIONES_SPOT_DISTRIBUCION");
				
				for(OperacionSpot opSpIns: selectOpeSpotInstrumento){
					logger.info("BuscandoElBug - opSpIns.FechaOperacion (22): " + opSpIns.FechaOperacion + " - Solicitud FFMM ID" + opSpIns.IdSolicitudAporteRescate);
					if("S".equals(pautaEnEjecucion) && !"TER".equals(estadoProcesoCambioFecha))
						opSpIns.FechaOperacion = Fecha.toDateTime(fechaHoraSistemaSecondateNueva);
					logger.info("BuscandoElBug - opSpIns.FechaOperacion (23): " + opSpIns.FechaOperacion + " - Solicitud FFMM ID" + opSpIns.IdSolicitudAporteRescate);
					OperacionSpot opeSpotProceso = copyPojoOperacionSpot(opSpIns);
					
					opeSpotProceso.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
					opeSpotProceso.Nemotecnico = opSpIns.Nemotecnico;
					
				
					OperacionObjIn operacion = in.p_DatosOperacion.stream().filter(ope->ope.NumeroOperacion.equals(opeSpotProceso.NumeroOperacion)).findFirst().orElse(new OperacionObjIn());
					opeSpotProceso.IdContacto = operacion.IdContacto;
					opeSpotProceso.TipoDocumento = operacion.TipoDocumento;
					
					if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal;
					}
					else if(opSpIns.IdMonedaSecundaria.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaSecundaria;
					}
					else if(!opSpIns.IdMonedaPrincipal.equals(monedaEquivalente) && opSpIns.IdMonedaSecundaria.equals(monedaSistema)){
						if(opSpIns.MultiplicaODivide.equals("D") && !(opSpIns.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP);
						}
						else if(opSpIns.MultiplicaODivide.equals("M")){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.ParidadMercado);
						}
					}
					
					if(opSpIns.IdTipoOperacion.equals("COMSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "VENSPT" : "COMSPT";
						
						if(opSpIns.ParidadBid.compareTo(BigDecimal.ZERO) == 0){
							opeSpotProceso.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
						}
						else if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
							opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.TipoCambioCosto);
						}
						else{
							opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.TipoCambioCosto).multiply(opSpIns.ParidadMercado).divide(opSpIns.ParidadBid,8,RoundingMode.HALF_UP);
						}
					}
					else if(opSpIns.IdTipoOperacion.equals("VENSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "COMSPT" : "VENSPT";
						
						if(opSpIns.ParidadAsk.compareTo(BigDecimal.ZERO) == 0){
							opeSpotProceso.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
						}
						else if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
							opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.TipoCambioCosto);
						}
						else{
							opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.TipoCambioCosto).multiply(opSpIns.ParidadMercado).divide(opSpIns.ParidadAsk,8,RoundingMode.HALF_UP);
						}
					}
					opeSpotProceso.FolioPlanilla = secuenciaFolioPlanillaSpot.get(i);
					opeSpotProceso.IdEmpleadoDigitador = opSpIns.IdEmpleadoDigitador;
					i++;
					operacionesSpot.add(opeSpotProceso);
					logger.info("BuscandoElBug - opeSpotProceso.FechaOperacion (24): " + opeSpotProceso.FechaOperacion + " - Solicitud FFMM ID" + opeSpotProceso.IdSolicitudAporteRescate);
				}
				
				
				//ARBITRAJE DISTRIBUCION
				logger.debug("INICIO::SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_DISTRIBUCION");
				selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_DISTRIBUCION, params, OperacionSpot.class);
				logger.debug("FIN::SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_DISTRIBUCION");
				
				for(OperacionSpot opSpIns: selectOpeSpotInstrumento){
					OperacionSpot opeSpotProceso = copyPojoOperacionSpot(opSpIns);
					logger.info("BuscandoElBug - opeSpotProceso.FechaOperacion (25): " + opeSpotProceso.FechaOperacion + " - Solicitud FFMM ID" + opeSpotProceso.IdSolicitudAporteRescate);
					
					opeSpotProceso.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
					opeSpotProceso.Nemotecnico = opSpIns.Nemotecnico;
					
					OperacionObjIn operacion = in.p_DatosOperacion.stream().filter(ope->ope.NumeroOperacion.equals(opeSpotProceso.NumeroOperacion)).findFirst().orElse(new OperacionObjIn());
					opeSpotProceso.IdContacto = operacion.IdContacto;
					opeSpotProceso.TipoDocumento = operacion.TipoDocumento;
					if(opSpIns.IdMonedaPrincipal.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal;
					}
					else if(opSpIns.IdMonedaSecundaria.equals(monedaEquivalente)){
						opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaSecundaria;
					}
					else if(!opSpIns.IdMonedaPrincipal.equals(monedaEquivalente) && opSpIns.IdMonedaSecundaria.equals(monedaSistema)){
						if(opSpIns.MultiplicaODivide.equals("D") && !(opSpIns.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.divide(opSpIns.ParidadMercado,8,RoundingMode.HALF_UP);
						}
						else if(opSpIns.MultiplicaODivide.equals("M")){
							opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipal.multiply(opSpIns.ParidadMercado);
						}
					}
					
					if(opSpIns.IdTipoOperacion.equals("COMSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "VENSPT" : "COMSPT";
						
						opeSpotProceso.ResultadoMonedaLocalPuntas = opSpIns.Margen.multiply(opSpIns.PuntaCompra);
						opeSpotProceso.ResultadoMonedaLocalBidAsk = opSpIns.Margen.multiply(opSpIns.PuntaBid);
						opeSpotProceso.MargenBidAsk = (opSpIns.ParidadBid.subtract(opSpIns.ParidadCierre)).multiply(opSpIns.MontoMonedaPrincipal);
					}
					else if(opSpIns.IdTipoOperacion.equals("VENSPT")){
						opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoCanalIngreso.equals("CRM") ? "COMSPT" : "VENSPT";
						
						opeSpotProceso.ResultadoMonedaLocalPuntas = opSpIns.Margen.multiply(opSpIns.PuntaVenta);
						opeSpotProceso.ResultadoMonedaLocalBidAsk = opSpIns.Margen.multiply(opSpIns.PuntaAsk);
						opeSpotProceso.MargenBidAsk = (opSpIns.ParidadCierre.subtract(opSpIns.ParidadAsk)).multiply(opSpIns.MontoMonedaPrincipal);
					}
					opeSpotProceso.ResultadoMonedaLocalDO = opSpIns.Margen.multiply(opSpIns.ValorDolarObservado);
					opeSpotProceso.ResultadoMonedaLocalTCSpot = opSpIns.Margen.multiply(opSpIns.TipoCambioUSD);
					opeSpotProceso.FolioPlanilla = secuenciaFolioPlanillaSpot.get(i);
					opeSpotProceso.IdEmpleadoDigitador = opSpIns.IdEmpleadoDigitador;
					i++;
					operacionesSpot.add(opeSpotProceso);
					logger.info("BuscandoElBug - opeSpotProceso.FechaOperacion (26): " + opeSpotProceso.FechaOperacion + " - Solicitud FFMM ID" + opeSpotProceso.IdSolicitudAporteRescate);
					
				}
				boolean ExisteOpFMM = false;
				for(OperacionSpot opSp: operacionesSpot){
					//Modifica el Estado cuando operacion queda PENDIENTE
					if(opSp.IdModulo.equals("FMM")) {
						opSp.Estado = "PEN";
						ExisteOpFMM = true;
					}
					else {
						for(OperacionObjIn opeUpd: operacionesInUpdateEstado){
							if(opSp.NumeroOperacion.equals(opeUpd.NumeroOperacion)){
								opSp.Estado = opeUpd.Estado;
							}
						}
					}
					//Asigna los valores calculados del Margen
					for(OperacionObjIn opeUpd: operacionesInUpdateMargen){
						if(opSp.NumeroOperacion.equals(opeUpd.NumeroOperacion)){
							opSp.Margen = opeUpd.Margen;
							if(!opSp.IdTipoCanalIngreso.equalsIgnoreCase("AGE") && !opSp.IdTipoCanalIngreso.equalsIgnoreCase("CLI") ) {
								opSp.Utilidad = opeUpd.Utilidad;	
							}
							opSp.PuntaCompra = opeUpd.PuntaCompra;
							opSp.PuntaVenta = opeUpd.PuntaVenta;
							opSp.TipoCambioCosto = opeUpd.TipoCambioCosto;
						}
					}
					logger.info("BuscandoElBug - opSp.FechaOperacion (27): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
				}
				
				
				/***** LIMITES *****/

				for (OperacionSpot operacionSpot : operacionesSpot) {
					operacionSpot.IdInterno 		= Long.valueOf(operacionSpot.NumeroOperacion);
					operacionSpot.CodigoOperador 	= "CLI".equals(operacionSpot.IdTipoCanalIngreso) ? null : (empleadoUsuario != null ? empleadoUsuario.CodigoEmpleado : null);
					operacionSpot.IdTipoSpot 		= _cm.ESTADO_MESA_ABIERTA.equals(idEstadoMesaPorIdMesa.get(operacionSpot.IdMesa)) ? _cm.TIPO_SPOT_INTRADAY : _cm.TIPO_SPOT_OVERNIGHT;
					logger.info("BuscandoElBug - operacionSpot.FechaOperacion (28): " + operacionSpot.FechaOperacion + " - Solicitud FFMM ID" + operacionSpot.IdSolicitudAporteRescate);
				}
				//LIMITES TODO
				ValidarLimitesCuentaPropiaSpot.SalidaValidarLimitesCuentaPropiaSpot salidaValidarLimitesCuentaPropiaSpot = validarLimitesCuentaPropia(conn, in, operacionesSpot);
				if (salidaValidarLimitesCuentaPropiaSpot.P_OUT_ERRORESCONTADOR > 0) {
					out.P_OUT_MENSAJES.addAll(salidaValidarLimitesCuentaPropiaSpot.P_OUT_MENSAJES);
				}
				
				if (salidaValidarLimitesCuentaPropiaSpot.P_OUT_VALIDACIONES.size() > 0) {
					AtomicLong totalAutLimCtaPropia = new AtomicLong(0L);
					Map<Long, Validacion> validacionLimCtaPropiaPorIdInterno = salidaValidarLimitesCuentaPropiaSpot.P_OUT_VALIDACIONES.stream().collect(Collectors.toMap(u -> u.IdInternoOperacion, u -> u));
					operacionesSpot = operacionesSpot.stream()
							.map(operacionSpot -> Pair.of(operacionSpot, validacionLimCtaPropiaPorIdInterno.get(operacionSpot.IdInterno)))
							.peek(parOperacionValidacion -> {
								Validacion validacion = parOperacionValidacion.getRight();
								if (Objects.nonNull(validacion)) {
									if (_cl.TIPO_VALIDACION_OMITIR.equals(validacion.TipoComportamiento) == false) {
										out.P_OUT_MENSAJES.add(validacion.Mensaje);
									}
									if (_cl.TIPO_VALIDACION_AUTORIZACION.equals(validacion.TipoComportamiento) || _cl.TIPO_VALIDACION_ADVERTENCIA.equals(validacion.TipoComportamiento)) {
										out.P_OUT_ADVERTENCIASCONTADOR++;
									}
									if (_cl.TIPO_VALIDACION_RECHAZAR.equals(validacion.TipoComportamiento)) {
										out.P_OUT_ERRORESCONTADOR++;
									}
									if (_cl.TIPO_VALIDACION_AUTORIZACION.equals(validacion.TipoComportamiento)) {
										totalAutLimCtaPropia.getAndIncrement();
										OperacionSpot operacionSpot = parOperacionValidacion.getLeft();
										operacionSpot.Estado = "PEN";
										
										OperacionSpotEventoAutorizacion eventoAutorizacion = new OperacionSpotEventoAutorizacion();
										eventoAutorizacion.IdTipoEvento = "LIMSPT";
										eventoAutorizacion.NumeroOperacion = operacionSpot.NumeroOperacion;
										eventoAutorizacion.Glosa = "FUERA_LIM_CTA_PROPIA";
										evtList.add(eventoAutorizacion);
									}
								}
							})
							.filter(parOperacionValidacion -> parOperacionValidacion.getRight() == null || _cl.TIPO_VALIDACION_RECHAZAR.equals(parOperacionValidacion.getRight().TipoComportamiento) == false)
							.map(Pair::getLeft)
							.collect(Collectors.toList());
					
					Id += totalAutLimCtaPropia.get();
					advCont += totalAutLimCtaPropia.get();
				}
				
				if (operacionesSpot.isEmpty()) {
					return out;
				}
				
				/***** AUTORIZACIONES *****/
				if(advCont > 0L && !ExisteOpFMM){
					CrearEventoAutorizacionMasivo eventoAutorizacion = new CrearEventoAutorizacionMasivo();
					CrearEventoAutorizacionMasivo.ServiceEntrada entradaEventoAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
					entradaEventoAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
					entradaEventoAutorizacion.P_IDMODULO = in.p_IdModulo;
					entradaEventoAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
					entradaEventoAutorizacion.P_SIMULAROPERACION = "N";
					entradaEventoAutorizacion.P_USAREGLASNEGOCIO = "N";
					
					for(OperacionSpotEventoAutorizacion evt: evtList){
						for(OperacionSpot opSp: operacionesSpot){
							if(opSp.NumeroOperacion.equals(evt.NumeroOperacion)){
								String idConcepto = "LIMSPT";
								if(opSp.Nemotecnico.equals("DISTRI") || opSp.Nemotecnico.equals("ARBDIS")){
									idConcepto = "LISDCP";
								}
								else if(opSp.Nemotecnico.equals("INTERB") || opSp.Nemotecnico.equals("ARBINT")){
									idConcepto = "LISICP";
								}
								
								CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
								evento.IdTipoEvento = evt.IdTipoEvento;
								evento.IdModuloSolicita = in.p_IdModulo;
								evento.IdUsuarioSolicita = in.p_IdUsuario;
								evento.Fecha = Fecha.toStringDate(opSp.FechaOperacion);
								evento.IdTipoOperacion = opSp.IdTipoOperacion;
								evento.IdConcepto = idConcepto;
								evento.NumeroOperacionConcepto = opSp.IdOperacionSpot.toString();
								evento.FechaOperacionConcepto = "";
								evento.IdCliente = opSp.IdCliente;
								evento.IdFuncionario = 0L;
								evento.IdProveedor = 0L;
								evento.IdTipoProducto = opSp.IdTipoProducto;
								evento.IdClasificacionTipoInstrumento = "SP";
								evento.IdTipoInstrumento = "SPT";
								evento.IdInstrumento = opSp.IdInstrumento;
								evento.Nemo = opSp.Nemotecnico;
								evento.Cantidad = 0L;
								evento.Monto = MonedaFormato.formatear(conn, opSp.MontoMonedaPrincipal, "MONTO", opSp.IdMonedaPrincipal,"SPT","").valor;
								evento.Precio = opSp.Nemotecnico.equals("DISTRI") || opSp.Nemotecnico.equals("INTERB") ? opSp.TipoCambioCierre : opSp.ParidadCierre;
								evento.Tasa = BigDecimal.ZERO;
								evento.IdMoneda = opSp.IdMonedaPrincipal;
								evento.Glosa = evt.Glosa;
								
								entradaEventoAutorizacion.P_TABLAEVENTOS.add(evento);
							}
							logger.info("BuscandoElBug - opSp.FechaOperacion (29): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
						}
					}
					CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = eventoAutorizacion.logica(conn, entradaEventoAutorizacion);
					for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error: salidaAut.P_OUT_ERRORESDETALLE) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,error.Codigo,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
				}
				
				
				/***** FORMAS DE PAGO OPERACION SPOT *****/
				HashMap<String,Object> parametros = new HashMap<String,Object>();
				for(OperacionObjIn ope: in.p_DatosOperacion) {
					parametros.put("IdInstrumento",ope.IdInstrumento);
					parametros.put("IdMonedaPrincipal", ope.IdMonedaPrincipal);
					parametros.put("IdMonedaSecundaria", ope.IdMonedaSecundaria);
					logger.info("BuscandoElBug - ope.FechaOperacion (30): " + ope.FechaOperacion + " - Solicitud FFMM ID" + ope.IdSolicitudAporteRescate);
				}
				Boolean aplicaFeriadoMonedaPrincipal = _sql.countQuery(conn, SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL, parametros)==0?false:true;
  				//dia inicial nacional
				ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente diaSgteIn = new InObtenerDiaSiguiente();
				diaSgteIn.p_idPais = idPaisSistema;
				diaSgteIn.p_diaHabil = "S";
				for(FormaPagoObjIn fpIn : in.p_DatosFormaPago){
					FechasIn fechas = new FechasIn();
					Long diasPago = Long.parseLong(fpIn.IdFormaDePago.substring(fpIn.IdFormaDePago.length() -1));
					fechas.fechaInicial = Fecha.toStringDate(((OperacionObjIn) numeroOperacionHash.get(fpIn.IdOperacion.toString())).FechaOperacion);
					fechas.numeroDias = formaDePagoMesaHash.get(fpIn.IdFormaDePago) != null ? ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias : 0L;
					diaSgteIn.p_fechas.add(fechas);
				}
				OutObtenerDiaSiguiente salDiaSgte = diaSgte.logica(conn, diaSgteIn);
				
				//Obtiene los datos generales para las formas de pago
				ArrayList<OperacionSpotFormaDePago> operacionesSpotForPag = new ArrayList<>();
				for(OperacionSpot opSp: operacionesSpot){
					int cantidadFormasPagoRecibimos = 0;
					int cantidadFormasPagoEntregamos = 0;
					for(FormaPagoObjIn fpIn: in.p_DatosFormaPago){
						if(opSp.NumeroOperacion.equals(fpIn.IdOperacion.toString())){
							String fechaCondicionLiquidacion = null;
							
							if (!formaDePagoMesaHash.containsKey(fpIn.IdFormaDePago)) {
								out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(opSp.NumeroOperacion) ,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_TIENE_PREFERENCIA_PAGO",mapMsg,conn,in.p_IdEmpresa));
								out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
								return out;
							}
							
							if(fpIn.FechaLiquidacion == null || fpIn.FechaLiquidacion.equals(fechaDefecto)) {
								for(FechasOut fec: salDiaSgte.p_out_fechas){
									if(fec.fechaInicial.equals(Fecha.toStringDate(opSp.FechaOperacion)) && fec.numeroDiasInicial.equals(((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias)){
										fechaCondicionLiquidacion = fec.fechaCalculada;
										break;
									}						
								}
							}else {
								fechaCondicionLiquidacion = Fecha.stringToSqlDate(fpIn.FechaLiquidacion);
							}
							
							if(fpIn.RecibePagaPortafolio.equalsIgnoreCase("R")) {
								cantidadFormasPagoRecibimos++;
							} else if(fpIn.RecibePagaPortafolio.equalsIgnoreCase("P")) {
								cantidadFormasPagoEntregamos++;
							};
							
							String formaDePagoMesa = "";
							String ModalidadPagoMesa = "";
							
							//CASO MESA NO TENGA CONFIGURADA LAS PREFERENCIAS DE PAGO DEJA COMO CTA. INV. POR DEFECTO
							if(formaDePagoMesaHash.get(fpIn.IdFormaDePago)== null) {
							    Long idNumOpe = Long.parseLong(opSp.NumeroOperacion);
							    out.P_OUT_MENSAJES.add(new MensajeSalida( idNumOpe,MensajeSalida.MENSAJE_ERROR,"MESA_SPOT_NO_TIENE_PREFERENCIA_PAGO",mapMsg,conn,in.p_IdEmpresa));
								out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
								return out;
							}else {
								formaDePagoMesa   = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).IdFormaDePago;
								ModalidadPagoMesa = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).IdMedioDePago;
							}
							
							OperacionSpotFormaDePago osfp = new OperacionSpotFormaDePago();
							osfp.IdInternoPagoOperacion = fpIn.IdInternoPagoOperacion;
							osfp.IdComprobantePagoOperacion = fpIn.IdComprobantePagoOperacion;
							osfp.IdEmpresa = in.p_IdEmpresa;
							osfp.IdOperacion = opSp.IdOperacionSpot;
							osfp.IdIngresoEgreso = 0L;
							osfp.IdAbono = 0L;
							osfp.IdCargo = 0L;
							osfp.IdIngresoEgresoAnticipo = 0L;
							osfp.IdAbonoAnticipo = 0L;
							osfp.IdCargoAnticipo = 0L;
							osfp.IdTipoProducto = opSp.IdTipoProducto;
							osfp.IdTipoMovimiento = fpIn.IdTipoMovimiento;
							osfp.RecibePagaPortafolio = fpIn.RecibePagaPortafolio;
							osfp.IdFormaDePago = formaDePagoMesa;
							osfp.IdModalidadPago = ((ModalidadDePago) modalidadPagoHash.get(ModalidadPagoMesa)).IdModalidadDePago;
							osfp.IdMedioDePago = formaDePagoMesa;
							osfp.IdMoneda = fpIn.IdMoneda;
							osfp.IdMonedaLiquidacion = fpIn.IdMonedaLiquidacion;
							osfp.Monto = MonedaFormato.formatear(conn, fpIn.MontoFormaDePago, "MONTO", fpIn.IdMoneda,"SPT","").valor;
							osfp.MontoLiquidacion = MonedaFormato.formatear(conn, fpIn.MontoFormaDePago, "MONTO", fpIn.IdMonedaLiquidacion,"SPT","").valor;
							osfp.FechaIngresoComprobantePagoOperacion = Fecha.toStringDate(opSp.FechaOperacion);
							if(aplicaFeriadoMonedaPrincipal && !"FMM".equalsIgnoreCase(opSp.IdModulo)) {
								Long numeroDias = formaDePagoMesaHash.get(fpIn.IdFormaDePago) != null ? ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias : 0L; 
								osfp.FechaLiquidacionComprobantePagoOperacion = calculaFechaLiquidacion(conn, Fecha.toStringDate(opSp.FechaOperacion), numeroDias, idUsa, idPaisSistema );
							}else {
								osfp.FechaLiquidacionComprobantePagoOperacion =fechaCondicionLiquidacion;
							}
							osfp.IdEstado = opSp.Estado;
							osfp.AfectarTesoreria = fpIn.AfectarTesoreria;
							osfp.IdCuentaBancaria = fpIn.IdCuentaBancaria;
							osfp.IdTipoOperacion = opSp.IdTipoOperacion;
							operacionesSpotForPag.add(osfp);
							logger.info("BuscandoElBug - opSp.FechaOperacion (31): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
						}
					}
					//Valida si almenos hay una forma de pago recibimos y una entregamos
					if(cantidadFormasPagoRecibimos  == 0 || cantidadFormasPagoEntregamos == 0 ) {
						Long idNumOpe = Long.parseLong(opSp.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida( idNumOpe,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_TIENE_PREFERENCIA_PAGO",mapMsg,conn,in.p_IdEmpresa));
						out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						return out;
					}
				}
				
				/***** INSERT OPERACION SPOT y PREPARA SALIDA DEL SERVICIO *****/
				List<OperacionObjOut> pOutIdOperacionSpot = new ArrayList<CrearOperacionSpot.OperacionObjOut>();
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				EntradaNotificarEstadoSpot entradaNotificarEstadoSpot = new EntradaNotificarEstadoSpot();
				
				Map<String, List<FormaPagoObjIn>> hashFormaPago = in.p_DatosFormaPago.parallelStream().filter(c -> "P".equalsIgnoreCase(c.RecibePagaPortafolio)).collect(Collectors.groupingBy(ope->ope.IdOperacion.toString()));
				for(OperacionSpot opSp: operacionesSpot){ 
					logger.info("BuscandoElBug - opSp.FechaOperacion (32): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
					Timestamp fechaOperacionSpot = "FMM".equals(opSp.IdTipoCanalIngreso) ? opSp.FechaOperacion : Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistema+ " "+horaReal));
					if("S".equalsIgnoreCase(pautaEnEjecucion) && "TER".equalsIgnoreCase(estadoProcesoCambioFecha))
						opSp.FechaOperacion = Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistema + " 00:00:01"));
					logger.info("BuscandoElBug - opSp.FechaOperacion (33): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
					
					if ("FMM".equalsIgnoreCase(opSp.IdTipoCanalIngreso) && opSp.IdSolicitudAporteRescate != null) {
						if (hashFormaPago.size() > 0) {
							List<FormaPagoObjIn> listaFormaPago = hashFormaPago.get(opSp.NumeroOperacion);
							if(listaFormaPago.size() > 0) {
								FormaPagoObjIn frmPago = listaFormaPago.get(0);
								if("S".equalsIgnoreCase(pautaEnEjecucion) && !"TER".equalsIgnoreCase(estadoProcesoCambioFecha))
									
									opSp.FechaLiquidacion = Fecha.toDate(fechaSistema);
								else
									opSp.FechaLiquidacion = frmPago.FechaLiquidacion == null ? Fecha.toDate(fechaSistema) : frmPago.FechaLiquidacion;
									
									/**SE CALCULA PROXIMO DIA HABIL**/
									ObtenerDiaSiguiente diaHabilSiguiente = new ObtenerDiaSiguiente();
									InObtenerDiaSiguiente diaHabilSiguienteIn = new InObtenerDiaSiguiente();
									diaHabilSiguienteIn.p_idPais = idPaisSistema;
									diaHabilSiguienteIn.p_diaHabil = "S";
									FechasIn fecha = new FechasIn();
									fecha.fechaInicial = fechaSistema;
									fecha.numeroDias = 1L;
									fecha.pais = idPaisSistema;
									diaHabilSiguienteIn.p_fechas.add(fecha);
									OutObtenerDiaSiguiente salDiaSiguiente = diaHabilSiguiente.logica(conn, diaHabilSiguienteIn);
									String siguienteDiaHabil = salDiaSiguiente.p_out_fechas.get(0).fechaCalculada.toString();
																		
									logger.info("BuscandoElBug - opSp.FechaOperacion (34): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
									boolean validarIngreso1 = validarIngreso1(estadoFondo,estadoMesaSpot,pautaEnEjecucion);
									java.sql.Time horaTope = Fecha.toTime("23:59:59");								
									if("S".equalsIgnoreCase(pautaEnEjecucion) && "TER".equalsIgnoreCase(estadoProcesoCambioFecha))
										opSp.FechaOperacion = Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistema + " 00:00:01"));
									else if("S".equalsIgnoreCase(pautaEnEjecucion) && "PEN".equalsIgnoreCase(estadoProcesoCambioFecha)) {
										opSp.FechaOperacion = Fecha.toUTCTimeStamp(Fecha.toTimeStamp(siguienteDiaHabil + " 00:00:01"));
										if(opSp.FechaLiquidacion.compareTo(opSp.FechaOperacion)<0) {
											opSp.FechaLiquidacion = Fecha.toDate(siguienteDiaHabil);
											}
										}	
									else if(validarIngreso1 && fechaReal.compareTo(fechaUltimaEjecucionCambioFechaLocal) == 0 && horaReal.getTime()>horaUltimaEjecucionCambioFecha.getTime() && horaReal.getTime()<horaTope.getTime())
										opSp.FechaOperacion = Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistema + " 00:00:01"));	
									else if(!validarIngreso1 && fechaReal.compareTo(fechaUltimaEjecucionCambioFechaLocal) == 0 && horaReal.getTime()>horaUltimaEjecucionCambioFecha.getTime() && horaReal.getTime()<horaTope.getTime())
										opSp.FechaOperacion = Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistema + " 00:00:01"));
									else if(!validarIngreso1)
										opSp.FechaOperacion = Fecha.toUTCTimeStamp(Fecha.toTimeStamp(siguienteDiaHabil + " 00:00:01"));	
								fechaOperacionSpot = opSp.FechaOperacion;
								logger.info("BuscandoElBug - opSp.FechaOperacion (35): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
							}
						}
					}

					opSp.FechaOperacion = fechaOperacionSpot;

					opSp.IdPlataformaNegociacion = in.p_DatosOperacion.get(0).IdPlataformaNegociacion;
					logger.info("BuscandoElBug - opSp.FechaOperacion (36): " + opSp.FechaOperacion + " - Solicitud FFMM ID" + opSp.IdSolicitudAporteRescate);
					
					if(!"0".equals(opSp.IdDatatec)) {
						opSp.Observacion = opSp.IdDatatec;
					}
					OperacionObjIn operacion = in.p_DatosOperacion.stream().filter(ope->ope.NumeroOperacion.equals(opSp.NumeroOperacion)).findFirst().orElse(new OperacionObjIn());
					if("S".equalsIgnoreCase(operacion.Confirmar)||"N".equalsIgnoreCase(operacion.Confirmar)) {
						opSp.EstadoConfirmacion = "PEN";
						opSp.Confirmar  = "S".equalsIgnoreCase(operacion.Confirmar) ? "S":"N";
						ConfirmacionWS = true;
						
					}else {
						opSp.EstadoConfirmacion = estadoConfirmacion;
					}
					
					opSp.IdOperacionSpotOrigen = opSp.IdOperacionSpot;
					opSp.IdOperacionSpotVigente = opSp.IdOperacionSpot;

					opSp.MontoMonedaPrincipal = MonedaFormato.formatear(conn, opSp.MontoMonedaPrincipal, "MONTO", opSp.IdMonedaPrincipal,"SPT","").valor;
					opSp.MontoMonedaSecundaria = MonedaFormato.formatear(conn, opSp.MontoMonedaSecundaria, "MONTO", opSp.IdMonedaSecundaria,"SPT","").valor;
					opSp.MontoEquivalente = MonedaFormato.formatear(conn, opSp.MontoEquivalente, "MONTO", opSp.IdMonedaEquivalente,"SPT","").valor;
					mapMsg.put("IdOperacionSpot", opSp.IdOperacionSpot.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"OPERACION_SPOT_INGRESADA",mapMsg,conn,in.p_IdEmpresa));
					Id++;

					//Salida
					OperacionObjOut opOut = new OperacionObjOut();
					opOut.IdInterno = opSp.NumeroOperacion;
					opOut.IdOperacionSpot = opSp.IdOperacionSpot;
					opOut.IdDatatec = opSp.IdDatatec;
					opOut.Estado = opSp.Estado;
					pOutIdOperacionSpot.add(opOut);
					
					if(opSp.Estado.equals("VIG"))cantOpeVig++;
					else if(opSp.Estado.equals("PEN"))cantOpePen++;
					
					//Log
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = opSp.IdOperacionSpot;
					log.IdTipoLog = "INGRESO";
					log.IdModulo = in.p_IdModulo;
					log.IdUsuario = in.p_IdUsuario;
					log.IdEmpresa = in.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = in.p_IdUsuario;
					log.IdCliente = opSp.IdCliente;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio = opSp.IdPortafolio;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = "INGRESOOPERACIONSPOT";
					log.EstadoRegistro = opSp.Estado;
					log.IdTipoOperacion = opSp.IdTipoOperacion;
					log.Folio = opSp.IdOperacionSpot;
					log.Cantidad = BigDecimal.ZERO;
					log.PrecioTasa = BigDecimal.ZERO;
					log.Monto = opSp.MontoMonedaSecundaria;
					log.IdMonedaOrigen = opSp.IdMonedaPrincipal;
					log.IdMonedaLiquidacion = "";
					log.TipoCambio = opSp.TipoCambioCierre != null ? opSp.TipoCambioCierre.toString() : "";
					log.Nemo = opSp.Nemotecnico;
					log.IdProceso = 0L;
					log.EstadoProceso = opSp.Estado;
					log.NombreProceso = "CrearOperacionSpot";						
					entradaAgregarLog.p_Log.add(log);
					entradaAgregarLog.p_LogDetalle.add(this.agregarDetalleLog(log, opSp, "Facturacion Automatica", opSp.FacturacionAutomatica));
					entradaAgregarLog.p_LogDetalle.add(this.agregarDetalleLog(log, opSp, "Canal Ingreso Operación", opSp.IdTipoCanalIngreso));
					entradaAgregarLog.p_LogDetalle.add(this.agregarDetalleLog(log, opSp, "Estado Caja", opSp.EstadoCaja));
					
					
					//Notificaciones
					NotificarEstadoSpot.OperacionSpot operacionSpot = new NotificarEstadoSpot.OperacionSpot();
					operacionSpot.IdOperacionSpot = opSp.IdOperacionSpot;
					entradaNotificarEstadoSpot.p_IdOperacionesSpot.add(operacionSpot);
				}
				
				//Comprobante Pago Operacion de Mesa (despues del ultimo repaso de la fecha de operacion del spot)
				CrearComprobantePagoOperacion crearComPag = new CrearComprobantePagoOperacion();
				CrearComprobantePagoOperacion_IN crearComPagIn = new CrearComprobantePagoOperacion_IN();
				CrearComprobantePagoOperacion_OUT crearComPagOut = new CrearComprobantePagoOperacion_OUT();
				HashMap<Long,Object> operacionesSpotHash = Utils.List2Hash(operacionesSpot, "IdOperacionSpot");
				crearComPagIn.p_IdEmpresa = in.p_IdEmpresa;
				crearComPagIn.p_IdUsuario = in.p_IdUsuario;
				crearComPagIn.p_IdModulo = in.p_IdModulo;
				crearComPagIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				
				for(OperacionSpotFormaDePago osfp: operacionesSpotForPag){
					ComprobantePagoOperacionObj_IN comPagIn = new ComprobantePagoOperacionObj_IN();
					OperacionSpot operacionSpot = (OperacionSpot) operacionesSpotHash.get(osfp.IdOperacion);
					
					if (operacionSpot != null) {
						osfp.FechaIngresoComprobantePagoOperacion = Fecha.toStringDate(operacionSpot.FechaOperacion);
					}
					
					comPagIn.IdInternoPagoOperacion = osfp.IdInternoPagoOperacion;
					comPagIn.IdComprobantePagoOperacion = osfp.IdComprobantePagoOperacion;
					comPagIn.IdEmpresa = in.p_IdEmpresa;
					comPagIn.IdOperacion = osfp.IdOperacion;
					comPagIn.IdIngresoEgreso = osfp.IdIngresoEgreso;
					comPagIn.IdAbono = osfp.IdAbono;
					comPagIn.IdCargo = osfp.IdCargo;
					comPagIn.IdIngresoEgresoAnticipo = osfp.IdIngresoEgresoAnticipo;
					comPagIn.IdAbonoAnticipo = osfp.IdAbonoAnticipo;
					comPagIn.IdCargoAnticipo = osfp.IdCargoAnticipo;
					comPagIn.IdTipoProducto = osfp.IdTipoProducto;
					comPagIn.RecibePagaPortafolio = osfp.RecibePagaPortafolio;
					comPagIn.IdFormaDePago = osfp.IdFormaDePago;
					comPagIn.IdModalidadPago = osfp.IdModalidadPago;
					comPagIn.IdMoneda = osfp.IdMoneda;
					comPagIn.Monto = osfp.Monto;
					comPagIn.FechaIngresoComprobantePagoOperacion = osfp.FechaIngresoComprobantePagoOperacion;
					comPagIn.FechaLiquidacionComprobantePagoOperacion = osfp.FechaLiquidacionComprobantePagoOperacion;
					comPagIn.IdEstado = osfp.IdEstado;
					comPagIn.AfectarTesoreria = osfp.AfectarTesoreria;
					comPagIn.IdCuentaBanco = osfp.IdCuentaBancaria;
					comPagIn.IdTipoOperacion = osfp.IdTipoOperacion;					
					crearComPagIn.p_ComprobantePagoOperacion.add(comPagIn);
				}
				
				crearComPagOut = crearComPag.logica(conn, crearComPagIn);
				if (MensajeSalida.getNumeroErrores(crearComPagOut.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(crearComPagOut.P_OUT_MENSAJES);
					return out;
				}
				logger.info("se insertan Operaciones y se estableceen valores para log");

				//En Carga Masiva, si hay un algun error no continua el flujo
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				if (out.P_OUT_ERRORESCONTADOR>0) {
					return out;
				}
				
				operacionesSpot = ModificarHora(operacionesSpot, in, fechaHoraSistemaTimestamp);
				_sql.batch(conn, SQL_INSERT_OPERACIONES_SPOT, operacionesSpot, OperacionSpot.class);
				
				
				/***** SALIDA *****/
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				out.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>(pOutIdOperacionSpot);
				
				logger.info("log a ingresar :" + entradaAgregarLog);
				/***** AGREGAR LOG *****/
				if (entradaAgregarLog.p_Log.size() > 0) {
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(conn, entradaAgregarLog);
				}
				logger.info("se insertaron los valores para log");
				
				/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS *****/
				AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ACTSPOT", "Refresco Movimientos Spot");
				
				
				/***** NOTIFICACIONES *****/
				if(entradaNotificarEstadoSpot.p_IdOperacionesSpot.size() > 0){
					entradaNotificarEstadoSpot.p_IdEmpresa = in.p_IdEmpresa;
					entradaNotificarEstadoSpot.p_IdUsuario = in.p_IdUsuario;
					entradaNotificarEstadoSpot.p_IdModulo = in.p_IdModulo;
					NotificarEstadoSpot notificarEstadoSpot = new NotificarEstadoSpot();
					notificarEstadoSpot.logica(conn, entradaNotificarEstadoSpot);
				}
				
				
				/***** FACTURACION *****/
				if(advCont.longValue() == 0){
					//Se calcula el maximo de fecha de las Formas de Pago
					HashMap<Long, Date> maxFechaLiquidacion = new HashMap<Long, Date>();
					HashMap<Long, String> maxIdCondicionLiquidacion = new HashMap<Long, String>();
					
					for(ComprobantePagoOperacionObj_IN comPagIn: crearComPagIn.p_ComprobantePagoOperacion){
						if (!maxFechaLiquidacion.containsKey(comPagIn.IdOperacion)) {
							maxFechaLiquidacion.put(comPagIn.IdOperacion, Fecha.toDate(comPagIn.FechaLiquidacionComprobantePagoOperacion));
							maxIdCondicionLiquidacion.put(comPagIn.IdOperacion, ((FormaDePagoMesa) formaDePagoMesaHash.get(comPagIn.IdFormaDePago)).IdCondicionDeLiquidacion);
						}
						else {
							Date iFecCondLiq = Fecha.toDate(comPagIn.FechaLiquidacionComprobantePagoOperacion);
							if(iFecCondLiq.compareTo(maxFechaLiquidacion.get(comPagIn.IdOperacion)) > 0){
								maxFechaLiquidacion.put(comPagIn.IdOperacion, iFecCondLiq);
								maxIdCondicionLiquidacion.put(comPagIn.IdOperacion, ((FormaDePagoMesa) formaDePagoMesaHash.get(comPagIn.IdFormaDePago)).IdCondicionDeLiquidacion);
							}
						}
					}
					
					
					CrearOperacionSpotFacturacionEntrada opSpFactIn = new CrearOperacionSpotFacturacionEntrada();
					opSpFactIn.p_IdEmpresa = in.p_IdEmpresa;
					opSpFactIn.p_IdUsuario = in.p_IdUsuario;
					opSpFactIn.p_IdModulo = in.p_IdModulo;
					opSpFactIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					
					for(OperacionSpot opSp: operacionesSpot){
						if(!"".equals(opSp.Confirmar)) {
							continue;
						}
						
						if(opSp.Nemotecnico.equals("INTERB") && parametroFacInterbSpot.equalsIgnoreCase("n")) {
							
							OperacionSpotFacturacionObjIn operacionSpotFactIn = new OperacionSpotFacturacionObjIn();
							operacionSpotFactIn.IdOperacionSpot = opSp.IdOperacionSpot;
							operacionSpotFactIn.IdTipoOperacion = opSp.IdTipoOperacion;
							operacionSpotFactIn.FechaOperacion = opSp.FechaOperacion;
							operacionSpotFactIn.IdCliente = opSp.IdCliente;
							operacionSpotFactIn.IdPortafolio = opSp.IdPortafolio;
							operacionSpotFactIn.IdInstrumento = opSp.IdInstrumento;
							operacionSpotFactIn.Nemotecnico = opSp.Nemotecnico;
							operacionSpotFactIn.IdTipoProducto = opSp.IdTipoProducto;
							operacionSpotFactIn.IdMonedaPrincipal= opSp.IdMonedaPrincipal;
							operacionSpotFactIn.IdMonedaSecundaria= opSp.IdMonedaSecundaria;
							operacionSpotFactIn.MontoMonedaPrincipal= opSp.MontoMonedaPrincipal;
							operacionSpotFactIn.MontoMonedaSecundaria= opSp.MontoMonedaSecundaria;
							operacionSpotFactIn.TipoCambioCierre = opSp.TipoCambioCierre;
							operacionSpotFactIn.ParidadCierre = opSp.ParidadCierre;
							operacionSpotFactIn.TipoComprobanteFacturacion = opSp.TipoComprobanteFacturacion;
							operacionSpotFactIn.IdBoveda = "COR";
							operacionSpotFactIn.IdCondicionLiquidacion = maxIdCondicionLiquidacion.get(opSp.IdOperacionSpot);
							operacionSpotFactIn.FechaLiquidacion = Fecha.toStringDate(maxFechaLiquidacion.get(opSp.IdOperacionSpot));
							operacionSpotFactIn.IdTipoConvenio = "FAC";
							operacionSpotFactIn.MultiplicaODivide = opSp.MultiplicaODivide;
							operacionSpotFactIn.TipoCambioUSD = opSp.TipoCambioUSD;
							operacionSpotFactIn.TipoCambioMonedaSecundaria = opSp.TipoCambioMonedaSecundaria;
							operacionSpotFactIn.Glosa = "SPOT";
							operacionSpotFactIn.IdMesa = opSp.IdMesa;
							operacionSpotFactIn.CrearComprobanteAutomatico = opSp.Nemotecnico.equals("INTERB") || opSp.Nemotecnico.equals("ARBINT") ? parametroFacInterbSpot : "N";
							operacionSpotFactIn.Estado = opSp.Estado;
							operacionSpotFactIn.IdTipoMovimientoFondoMutuo = opSp.IdTipoMovimientoFondoMutuo;
							
							opSpFactIn.p_DatosOperacion.add(operacionSpotFactIn);
						}
						if(opSp.Nemotecnico.equals("DISTRI") && liquidacionPorConfirmar.equalsIgnoreCase("n")) {
							OperacionSpotFacturacionObjIn operacionSpotFactIn = new OperacionSpotFacturacionObjIn();
							operacionSpotFactIn.IdOperacionSpot = opSp.IdOperacionSpot;
								operacionSpotFactIn.IdOperacionSpotOrigen = opSp.IdOperacionSpotOrigen;
							operacionSpotFactIn.IdTipoOperacion = opSp.IdTipoOperacion;
							operacionSpotFactIn.FechaOperacion = opSp.FechaOperacion;
							operacionSpotFactIn.IdCliente = opSp.IdCliente;
							operacionSpotFactIn.IdPortafolio = opSp.IdPortafolio;
							operacionSpotFactIn.IdInstrumento = opSp.IdInstrumento;
							operacionSpotFactIn.Nemotecnico = opSp.Nemotecnico;
							operacionSpotFactIn.IdTipoProducto = opSp.IdTipoProducto;
							operacionSpotFactIn.IdMonedaPrincipal= opSp.IdMonedaPrincipal;
							operacionSpotFactIn.IdMonedaSecundaria= opSp.IdMonedaSecundaria;
							operacionSpotFactIn.MontoMonedaPrincipal= opSp.MontoMonedaPrincipal;
							operacionSpotFactIn.MontoMonedaSecundaria= opSp.MontoMonedaSecundaria;
							operacionSpotFactIn.TipoCambioCierre = opSp.TipoCambioCierre;
							operacionSpotFactIn.ParidadCierre = opSp.ParidadCierre;
							operacionSpotFactIn.TipoComprobanteFacturacion = opSp.TipoComprobanteFacturacion;
							operacionSpotFactIn.IdBoveda = "COR";
							operacionSpotFactIn.IdCondicionLiquidacion = maxIdCondicionLiquidacion.get(opSp.IdOperacionSpot);
							operacionSpotFactIn.FechaLiquidacion = Fecha.toStringDate(maxFechaLiquidacion.get(opSp.IdOperacionSpot));
							operacionSpotFactIn.IdTipoConvenio = "FAC";
							operacionSpotFactIn.MultiplicaODivide = opSp.MultiplicaODivide;
							operacionSpotFactIn.TipoCambioUSD = opSp.TipoCambioUSD;
							operacionSpotFactIn.TipoCambioMonedaSecundaria = opSp.TipoCambioMonedaSecundaria;
							operacionSpotFactIn.Glosa = "SPOT";
							operacionSpotFactIn.IdMesa = opSp.IdMesa;
							operacionSpotFactIn.CrearComprobanteAutomatico = liquidacionPorConfirmar.equalsIgnoreCase( "S" ) ? "N" : "S";
							operacionSpotFactIn.Estado = opSp.Estado;
							operacionSpotFactIn.IdTipoCanalIngreso = opSp.IdTipoCanalIngreso;								
							operacionSpotFactIn.IdTipoMovimientoFondoMutuo = opSp.IdTipoMovimientoFondoMutuo;
														
							opSpFactIn.p_DatosOperacion.add(operacionSpotFactIn);
						}
						
					}
					HashMap<Long,Object> Operaciones = new HashMap<Long,Object>();
					opSpFactIn.p_DatosOperacion.forEach(element ->{
						Operaciones.put(element.IdOperacionSpot, element);
					});
					List<PreferenciasPagoSpot>ListaDePreferenciasSpot = _sql.query(conn,SQL_BUSQUEDA_PREFERENCIAS , new HashMap<>(), PreferenciasPagoSpot.class);
					HashMap<String,Object> Preferencias = new HashMap<String,Object>();
					ListaDePreferenciasSpot.forEach(element ->{
						Preferencias.put(element.IdPreferenciaPago, element);
					});
					
					for(OperacionSpotFormaDePago osfp: operacionesSpotForPag){
							
				        String[] arrOfStr = osfp.IdMedioDePago.split("_");
				        int tamano = arrOfStr.length;
				        String key="";
				        if(tamano>1) {
				        	 key =  arrOfStr[0]+"_"+ arrOfStr[1]+"_"+ arrOfStr[2];	
				        }else {
				        	 key = osfp.IdMedioDePago;
				        }
				        
						boolean existeOperacion =  Operaciones.get(osfp.IdOperacion) != null;
						PreferenciasPagoSpot MedioDePago = (PreferenciasPagoSpot) Preferencias.get(key);
						
						if(existeOperacion) {
							FormaPagoSpotFacturacionObjIn formaPagoSpotFactIn = new FormaPagoSpotFacturacionObjIn();
							formaPagoSpotFactIn.IdOperacion = osfp.IdOperacion;
							formaPagoSpotFactIn.IdTipoMovimiento = osfp.IdTipoMovimiento;
							formaPagoSpotFactIn.IdMedioDePago = MedioDePago.IdMedioDePago;
							formaPagoSpotFactIn.IdModalidadPago = osfp.IdModalidadPago;
							formaPagoSpotFactIn.IdMoneda = osfp.IdMoneda;
							formaPagoSpotFactIn.Monto = osfp.Monto;
							formaPagoSpotFactIn.FechaLiquidacionComprobantePagoOperacion = osfp.FechaLiquidacionComprobantePagoOperacion;
							formaPagoSpotFactIn.AfectarTesoreria = osfp.AfectarTesoreria;
							formaPagoSpotFactIn.IdCuentaBancaria = osfp.IdCuentaBancaria;							
							opSpFactIn.p_DatosFormaPago.add(formaPagoSpotFactIn);
						}
						
					}
					
					if(opSpFactIn.p_DatosOperacion.size()>0) {
						ArrayList<OperacionSpot> p_DatosOperacion = new ArrayList<>();
						opSpFactIn.p_DatosOperacion.forEach(element ->{
							OperacionSpot op =  new OperacionSpot();
							op.NumeroOperacion = element.IdOperacionSpot+"";
							p_DatosOperacion.add(op);
						});

						ConfirmarLiquidacionSpotEntrada aux =  new ConfirmarLiquidacionSpotEntrada();
						aux.p_IdEmpresa =  in.p_IdEmpresa;
						aux.p_UsaReglasNegocio = "S";
						aux.p_IdUsuario = in.p_IdUsuario;
						aux.p_IdModulo = "MSP";
						aux.p_DatosOperacion = p_DatosOperacion;
						
						ConfirmarLiquidacionSpot confirmacionIn = new ConfirmarLiquidacionSpot();
						
						ConfirmarLiquidacionSpotSalida confirmacionout = confirmacionIn.logica(conn, aux);

						if (MensajeSalida.getNumeroErrores(confirmacionout.P_OUT_MENSAJES) > 0){
							out.P_OUT_MENSAJES.addAll(confirmacionout.P_OUT_MENSAJES);
							out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(m -> m.tipo == MensajeSalida.MENSAJE_ERROR).collect(Collectors.toList());
							return out;
						}
					}

					
				}
				
				// GCMBIDDLDP
				if (in.p_DatosFormaPago.get(0).IdFormaDePago.contains("_CIN_") && (ConfirmaLiquidacionNEG.equalsIgnoreCase("S") || ConfirmaLiquidacionCLI.equalsIgnoreCase("S"))) {
					ArrayList<OperacionSpot> p_DatosOperacion = new ArrayList<>();
					ConfirmarLiquidacionSpotEntrada confLiqIn =  new ConfirmarLiquidacionSpotEntrada();
					p_DatosOperacion = operacionesSpot.stream()
						.filter(element -> {
							return (element.IdMonedaPrincipal.equalsIgnoreCase("USD") 
									&& ( element.IdTipoCanalIngreso.equalsIgnoreCase("AGE") && ConfirmaLiquidacionNEG.equalsIgnoreCase("S")) 
									|| (element.IdTipoCanalIngreso.equalsIgnoreCase("CLI") && ConfirmaLiquidacionCLI.equalsIgnoreCase("S"))
									&& element.Confirmar.equalsIgnoreCase(""));
						})
						.map(element -> {
							OperacionSpot op =  new OperacionSpot();
							op.NumeroOperacion = element.IdOperacionSpot+"";
							return op;
						}).collect(Collectors.toCollection(ArrayList::new));
					if (p_DatosOperacion.size() > 0) {
						confLiqIn.p_IdEmpresa =  in.p_IdEmpresa;
						confLiqIn.p_UsaReglasNegocio = "S";
						confLiqIn.p_IdUsuario = in.p_IdUsuario;
						confLiqIn.p_IdModulo = "MSP";
						confLiqIn.p_DatosOperacion = p_DatosOperacion;
						ConfirmarLiquidacionSpot confirmacionIn = new ConfirmarLiquidacionSpot();
						
						ConfirmarLiquidacionSpotSalida confirmacionout = confirmacionIn.logica(conn, confLiqIn);
					}
				}
				
				if(ConfirmacionWS) {
					logger.info("Inicio del servicio Confirmacion para WS:");
					ArrayList<OperacionSpot> p_DatosOperacion = new ArrayList<>();
					ConfirmarLiquidacionSpotEntrada confLiqIn =  new ConfirmarLiquidacionSpotEntrada();
					List<Long> requiereAutorizar = new ArrayList<>();
					
					for (MensajeSalida ms : out.P_OUT_MENSAJES) {
						if (MensajeSalida.MENSAJE_AUTORIZACION.equals(ms.tipo)) {
							requiereAutorizar.add(ms.id);
						}
					}
					
					p_DatosOperacion = operacionesSpot.stream()
						.filter(element -> {
							return (element.Confirmar.equalsIgnoreCase("S") && !requiereAutorizar.contains(element.IdInterno));
						})
						.map(element -> {
							OperacionSpot op =  new OperacionSpot();
							op.NumeroOperacion = element.IdOperacionSpot+"";
							return op;
						}).collect(Collectors.toCollection(ArrayList::new));
					
					if (p_DatosOperacion.size() > 0) {
						confLiqIn.p_IdEmpresa =  in.p_IdEmpresa;
						confLiqIn.p_UsaReglasNegocio = "S";
						confLiqIn.p_IdUsuario = in.p_IdUsuario;
						confLiqIn.p_IdModulo = "MSP";
						confLiqIn.p_ConfirmacionWS = "S";
						confLiqIn.p_DatosOperacion = p_DatosOperacion;
						ConfirmarLiquidacionSpot confirmacionIn = new ConfirmarLiquidacionSpot();
						
						ConfirmarLiquidacionSpotSalida confirmacionout = confirmacionIn.logica(conn, confLiqIn);
						
						logger.info("Termino del servicio Confirmacion para WS:");
					}
				}
			}

			
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		logger.info("Termino del servicio CrearOperacionSpot, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}

	private DetalleLog agregarDetalleLog(Log log, OperacionSpot opSp, String nombreCampo, String campo) {
		DetalleLog detalleLog = new DetalleLog();
		detalleLog.IdTrabajo = log.IdTrabajo;
		detalleLog.IdCabeceraLog = log.IdCabeceraLog;
		detalleLog.SubRegistro = opSp.IdTipoOperacion;
		detalleLog.CampoRegistro = nombreCampo;
		detalleLog.ValorAntiguo = "";
		detalleLog.ValorNuevo = campo;
		return detalleLog;
	}

	private ValidarLimitesCuentaPropiaSpot.SalidaValidarLimitesCuentaPropiaSpot validarLimitesCuentaPropia(Connection conn, CrearOperacionSpotEntrada entrada, List<OperacionSpot> operacionesSpot) throws Exception {
		logger.debug("INICIO::CrearOperacionSpot.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimitesCuentaPropiaSpot.EntradaValidarLimitesCuentaPropiaSpot entradaValidarLimitesCuentaPropiaSpot = new ValidarLimitesCuentaPropiaSpot.EntradaValidarLimitesCuentaPropiaSpot();
		entradaValidarLimitesCuentaPropiaSpot.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimitesCuentaPropiaSpot.p_IdTipoProceso = _cl.PROCESO_INGRESAR;
		entradaValidarLimitesCuentaPropiaSpot.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimitesCuentaPropiaSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarLimitesCuentaPropiaSpot.p_OperacionesSpot = operacionesSpot.stream()
				.map(u -> {
					ValidarLimitesCuentaPropiaSpot.OperacionSpot o = new ValidarLimitesCuentaPropiaSpot.OperacionSpot();
					o.IdInterno = u.IdInterno;
					o.IdOperacionSpot = null;
					o.IdCliente = u.IdCliente;
					o.IdPortafolio = u.IdPortafolio;
					o.IdMesa = u.IdMesa;
					o.IdAgenteOperacion = u.IdAgente;
					o.IdTipoSpot = u.IdTipoSpot;
					o.Estado = u.Estado;
					o.EstadoConfirmacion = u.EstadoConfirmacion;
					o.IdInstrumento = u.IdInstrumento;
					o.IdTipoInstrumento = _cl.TIPO_INSTRUMENTO_SPOT;
					o.NemotecnicoInstrumento = u.Nemotecnico;
					o.CodigoOperador = u.CodigoOperador;
					o.IdTipoCanalIngreso = u.IdTipoCanalIngreso;
					o.IdTipoOperacion = u.IdTipoOperacion;
					o.IdMonedaPrincipal = u.IdMonedaPrincipal;
					o.MontoPrincipal = u.MontoMonedaPrincipal;
					o.Origen = _cl.ORIGEN_ENTRADA;
					return o;
				})
				.collect(Collectors.toList());
		
		ValidarLimitesCuentaPropiaSpot.SalidaValidarLimitesCuentaPropiaSpot salidaValidarLimitesCuentaPropiaSpot = new ValidarLimitesCuentaPropiaSpot().logica(conn, entradaValidarLimitesCuentaPropiaSpot);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionSpot.validarLimitesCuentaPropia {}", fin - inicio);
		
		return salidaValidarLimitesCuentaPropiaSpot;
	}

	private OperacionSpot copyPojoOperacionSpot(OperacionSpot opSpIns) {
		OperacionSpot opeSpotProceso = new OperacionSpot();
		opeSpotProceso.NumeroOperacion = opSpIns.NumeroOperacion;
		opeSpotProceso.IdOperacionSpot = opSpIns.IdOperacionSpot;
		opeSpotProceso.IdOperacionSpotOrigen = opSpIns.IdOperacionSpotOrigen;
		opeSpotProceso.IdEmpresa = opSpIns.IdEmpresa;
		opeSpotProceso.IdCliente = opSpIns.IdCliente; 
		opeSpotProceso.IdPortafolio = opSpIns.IdPortafolio;
		opeSpotProceso.IdInstrumento = opSpIns.IdInstrumento;
		opeSpotProceso.IdTipoOperacion = opSpIns.IdTipoOperacion;
		opeSpotProceso.IdTipoProducto = opSpIns.IdTipoProducto;
		opeSpotProceso.FechaOperacion = opSpIns.FechaOperacion;
		opeSpotProceso.FechaLiquidacion = opSpIns.FechaLiquidacion;
		opeSpotProceso.IdMonedaPrincipal = opSpIns.IdMonedaPrincipal;	
		opeSpotProceso.IdMonedaSecundaria = opSpIns.IdMonedaSecundaria;
		opeSpotProceso.IdMonedaLocal = opSpIns.IdMonedaLocal;
		opeSpotProceso.IdMonedaEquivalente = opSpIns.IdMonedaEquivalente;
		opeSpotProceso.MontoMonedaPrincipal = opSpIns.MontoMonedaPrincipal;
		opeSpotProceso.MontoMonedaPrincipalEquivalente = opSpIns.MontoMonedaPrincipalEquivalente;
		opeSpotProceso.MontoMonedaSecundaria = opSpIns.MontoMonedaSecundaria;
		opeSpotProceso.TipoCambioCierre = opSpIns.TipoCambioCierre;
		opeSpotProceso.TipoCambioCierreForward = opSpIns.TipoCambioCierreForward;
		opeSpotProceso.ParidadCierre = opSpIns.ParidadCierre;
		opeSpotProceso.ParidadCierreForward = opSpIns.ParidadCierreForward;
		opeSpotProceso.TipoCambioCosto = opSpIns.TipoCambioCosto;
		opeSpotProceso.ParidadCosto = opSpIns.ParidadCosto;
		opeSpotProceso.MontoEquivalente = opSpIns.MontoEquivalente;
		opeSpotProceso.MontoRentabilidad = opSpIns.MontoRentabilidad;
		opeSpotProceso.MontoRentabilidadCanal = opSpIns.MontoRentabilidadCanal;
		opeSpotProceso.ResultadoMonedaLocalPuntas = opSpIns.ResultadoMonedaLocalPuntas;
		opeSpotProceso.ResultadoMonedaLocalBidAsk = opSpIns.ResultadoMonedaLocalBidAsk;
		opeSpotProceso.ResultadoMonedaLocalDO = opSpIns.ResultadoMonedaLocalDO;
		opeSpotProceso.ResultadoMonedaLocalTCSpot = opSpIns.ResultadoMonedaLocalTCSpot;
		opeSpotProceso.ResultadoMonedaLocalTCCierreMesa = opSpIns.ResultadoMonedaLocalTCCierreMesa;
		opeSpotProceso.ResultadoMonedaLocalConsolidado = opSpIns.ResultadoMonedaLocalConsolidado;
		opeSpotProceso.Margen = opSpIns.Margen;
		opeSpotProceso.MargenBidAsk = opSpIns.MargenBidAsk;
		opeSpotProceso.Utilidad = opSpIns.Utilidad;
		opeSpotProceso.PuntaCompra = opSpIns.PuntaCompra;
		opeSpotProceso.PuntaVenta = opSpIns.PuntaVenta;
		opeSpotProceso.PuntaBid = opSpIns.PuntaBid;
		opeSpotProceso.PuntaAsk = opSpIns.PuntaAsk;
		opeSpotProceso.ParidadBid = opSpIns.ParidadBid;
		opeSpotProceso.ParidadAsk = opSpIns.ParidadAsk;
		opeSpotProceso.ValorDolarObservado = opSpIns.ValorDolarObservado;
		opeSpotProceso.ValorTCSpot = opSpIns.ValorTCSpot;
		opeSpotProceso.SolicitudTipoCambio = opSpIns.SolicitudTipoCambio;
		opeSpotProceso.IdEvento = opSpIns.IdEvento;
		opeSpotProceso.IdMesa = opSpIns.IdMesa;
		opeSpotProceso.IdModulo = opSpIns.IdModulo;
		opeSpotProceso.IdTipoDolar = opSpIns.IdTipoDolar;
		opeSpotProceso.TipoComprobanteFacturacion = opSpIns.TipoComprobanteFacturacion;
		opeSpotProceso.IdComprobanteFacturacion = opSpIns.IdComprobanteFacturacion;
		opeSpotProceso.IdComprobanteTesoreriaIngreso = opSpIns.IdComprobanteTesoreriaIngreso;
		opeSpotProceso.IdComprobanteTesoreriaEgreso = opSpIns.IdComprobanteTesoreriaEgreso;
		opeSpotProceso.IdAgente = opSpIns.IdAgente;
		opeSpotProceso.BenOur = opSpIns.BenOur;
		opeSpotProceso.EquivalenteConGasto = opSpIns.EquivalenteConGasto;
		opeSpotProceso.TipoCambioCierreConGasto = opSpIns.TipoCambioCierreConGasto;
		opeSpotProceso.Observacion = opSpIns.Observacion;
		opeSpotProceso.CodigoComercio = opSpIns.CodigoComercio;
		opeSpotProceso.FolioPlanilla = opSpIns.FolioPlanilla;
		opeSpotProceso.FolioInterfaz = opSpIns.FolioInterfaz;
		opeSpotProceso.OperacionFicticia = opSpIns.OperacionFicticia;
		opeSpotProceso.IdTipoCanalIngreso = opSpIns.IdTipoCanalIngreso;
		opeSpotProceso.IdDatatec = opSpIns.IdDatatec;
		opeSpotProceso.Estado = opSpIns.Estado;
		opeSpotProceso.IdSolicitudAporteRescate = opSpIns.IdSolicitudAporteRescate;
		opeSpotProceso.IdUsuario = opSpIns.IdUsuario;
		opeSpotProceso.IdUsuarioModifica = opSpIns.IdUsuarioModifica;
		opeSpotProceso.FechaModifica = opSpIns.FechaModifica;
		opeSpotProceso.FechaReproceso = opSpIns.FechaReproceso;
		opeSpotProceso.FechaIngresoRegistro = opSpIns.FechaIngresoRegistro;
		opeSpotProceso.EstadoConfirmacion = opSpIns.EstadoConfirmacion;
		opeSpotProceso.IdOperacionSpotReferencia = opSpIns.IdOperacionSpotReferencia;
		opeSpotProceso.FolioOrigenFW = opSpIns.FolioOrigenFW;
		opeSpotProceso.IdOperacionSpotOrigen = opSpIns.IdOperacionSpotOrigen;
		opeSpotProceso.IdOperacionSpotVigente = opSpIns.IdOperacionSpotVigente;
		opeSpotProceso.MultiplicaODivide = opSpIns.MultiplicaODivide;
		opeSpotProceso.ParidadMercado = opSpIns.ParidadMercado;
		opeSpotProceso.CierreIngresadoPosMonPri = opSpIns.CierreIngresadoPosMonPri;
		opeSpotProceso.CierreIngresadoPosMonEqui = opSpIns.CierreIngresadoPosMonEqui;
		opeSpotProceso.Nemotecnico = opSpIns.Nemotecnico;
		opeSpotProceso.TipoCambioUSD = opSpIns.TipoCambioUSD;
		opeSpotProceso.TipoCambioMonedaSecundaria = opSpIns.TipoCambioMonedaSecundaria;
		opeSpotProceso.TipoDocumento = opSpIns.TipoDocumento;
		opeSpotProceso.IdContacto = opSpIns.IdContacto;
		opeSpotProceso.AfectoADerivado = opSpIns.AfectoADerivado;
		opeSpotProceso.DolarObservado = opSpIns.DolarObservado;
		opeSpotProceso.IdEmpleadoDigitador = opSpIns.IdEmpleadoDigitador;
		opeSpotProceso.FacturacionAutomatica = opSpIns.FacturacionAutomatica;
		opeSpotProceso.EstadoCaja = opSpIns.EstadoCaja;
		
		return opeSpotProceso;
	}
	
	public Timestamp horasConvertidasTimestamp(Fecha fecha,Timestamp time,String hour) {
		
		String fechaSistema = fecha.toStringDate(time);
		return fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema, hour);	
	}
	
	public boolean validarHora(String hora) {
		

		if (!hora.matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")){
			return false;
		}
		else {
			return true;
		}
	}
	 
	public String corregirHora(String hora) {
		
		if(hora.length()== 5) {
			hora = hora+":00";
		}
		String[] horaDividida = hora.split(":");
		for (int i = 0; i < horaDividida.length; i++) {
			if(horaDividida[i].length()==1) {
				horaDividida[i] = "0"+horaDividida[i];
			}
			
		}
		 return horaDividida[0]+":"+horaDividida[1]+":"+horaDividida[2];
		
	}
	
	public ArrayList<OperacionSpot> ModificarHora(List<OperacionSpot> operacionesSpot, CrearOperacionSpotEntrada in, Timestamp fechaHoraSistemaTimestamp) {
		
		Fecha fecha = new Fecha();
		ArrayList<OperacionSpot> modifHorasOpeSpot = new ArrayList<CrearOperacionSpot.OperacionSpot>();
		for(OperacionObjIn opeIn : in.p_DatosOperacion) {
			for (OperacionSpot opeSpot : operacionesSpot) {
				if(opeIn.NumeroOperacion.equals(opeSpot.NumeroOperacion)) {
					logger.info("BuscandoElBug - opeSpot.FechaOperacion (37): " + opeSpot.FechaOperacion + " - Solicitud FFMM ID" + opeSpot.IdSolicitudAporteRescate);
					String hs = fecha.toStringTime(opeSpot.FechaOperacion);
					if( opeIn.HoraOperacion  != null  && !opeIn.HoraOperacion.isEmpty()) {
						opeIn.HoraOperacion = corregirHora(opeIn.HoraOperacion);
						String ho = opeIn.HoraOperacion;
						boolean vHora = validarHora(ho);
						if( vHora == true && !hs.equals(ho) && opeIn.HoraOperacion != null && opeIn.HoraOperacion != "" ) {						
							opeSpot.FechaOperacion = horasConvertidasTimestamp(fecha,opeIn.FechaOperacion,ho);
							String fechaIngresoRegistro = fecha.toStringDate(opeIn.FechaOperacion) + " " + ho;
							opeSpot.FechaIngresoRegistro = fecha.toDateTime(fechaIngresoRegistro);

						}
					} else {
						opeSpot.FechaIngresoRegistro = fechaHoraSistemaTimestamp;
					}
					modifHorasOpeSpot.add(opeSpot);
					logger.info("BuscandoElBug - opeSpot.FechaOperacion (38): " + opeSpot.FechaOperacion + " - Solicitud FFMM ID" + opeSpot.IdSolicitudAporteRescate);
				}
			}
		}
			
		return modifHorasOpeSpot;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) { 
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9677,\"p_IdModulo\":\"MSP\",\"p_EsIngresoWS\":\"S\",\"p_CalculaMargen\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":1,\"IdInstrumento\":133258,\"IdTipoOperacion\":\"COMSPT\",\"IdTipoProducto\":\"MEME\",\"CodigoEjecutivoDigitador\":\"\",\"IdCliente\":2956,\"IdPortafolio\":6104,\"FechaOperacion\":\"2026-07-03\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoMonedaPrincipal\":20000,\"MontoMonedaSecundaria\":18249200,\"TipoCambioCierre\":912.46,\"TipoCambioCierreForward\":0,\"ParidadCierre\":0,\"ParidadCierreForward\":0,\"TipoCambioCosto\":914.15,\"ParidadCosto\":0,\"MontoEquivalente\":18249200,\"MontoRentabilidad\":0,\"MontoRentabilidadCanal\":0,\"Margen\":33799.99999999882,\"Utilidad\":33799.99999999882,\"PuntaCompra\":914.15,\"PuntaVenta\":914.39,\"SolicitudTipoCambio\":\"N\",\"IdMesa\":41,\"IdModulo\":\"SPT\",\"IdTipoDolar\":1,\"TipoComprobanteFacturacion\":\"FEE\",\"IdAgente\":1415,\"BenOur\":\"BEN\",\"EquivalenteConGasto\":0,\"TipoCambioCierreConGasto\":0,\"Observacion\":\"\",\"CodigoComercio\":\"100101\",\"FolioPlanilla\":0,\"FolioInterfaz\":\"\",\"OperacionFicticia\":\"S\",\"IdTipoCanalIngreso\":\"MES\",\"IdDatatec\":0,\"Estado\":\"VIG\",\"IdSolicitudAporteRescate\":0,\"IdContacto\":0,\"TipoDocumento\":\"\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENSPT_CIN_CLP_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"MontoFormaDePago\":18249200,\"AfectarTesoreria\":\"N\",\"IdCuentaBancaria\":0},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMSPT_TRA_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":20000,\"AfectarTesoreria\":\"S\",\"IdCuentaBancaria\":0}]}";
			
			CrearOperacionSpotEntrada Objin = gson.fromJson(json, CrearOperacionSpotEntrada.class);
			CrearOperacionSpot demo = new CrearOperacionSpot();			
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
    
 				CrearOperacionSpotSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}