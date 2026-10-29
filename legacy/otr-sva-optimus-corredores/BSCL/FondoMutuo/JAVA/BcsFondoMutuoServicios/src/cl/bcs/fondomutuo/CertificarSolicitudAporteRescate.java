package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.facturacion.CrearComprobante;
import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.fondomutuo.AgregarLogCertificacionAporteRescate.EntradaAgregarLogCertificacionAporteRescate;
import cl.bcs.fondomutuo.AgregarLogCertificacionAporteRescate.InLog;
import cl.bcs.fondomutuo.CalculaCustodiaRescate.EntradaCalculaCustodiaRescate;
import cl.bcs.fondomutuo.CalculaCustodiaRescate.ResultadoCalculaCustodiaRescate;
import cl.bcs.fondomutuo.CalculaCustodiaRescate.SalidaCalculaCustodiaRescate;
import cl.bcs.fondomutuo.CalcularComisiones.EntradaCalcularComisiones;
import cl.bcs.fondomutuo.CalcularComisiones.ResultadoCalcularComision;
import cl.bcs.fondomutuo.CalcularComisiones.SalidaCalcularComisiones;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.ConsultaRegimenReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.EntradaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.SalidaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.ParametroOperaCSN;
import cl.bcs.fondomutuo.NotificarEstadoOrdenFondoMutuo.EntradaNotificarEstadoOrdenFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.EntradaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaMovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.EntradaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.SalidaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.EntradaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.SalidaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.FormaPagoSpot;
import cl.bcs.fondomutuo.pojo.IndicadoresPortafolioFondoMutuo;
import cl.bcs.fondomutuo.pojo.MovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.ReinversionSerieFondoDestino;
import cl.bcs.fondomutuo.pojo.ResultadoCertificacion;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.normativa.ResumenPatrimonial.AporteRescate;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.NotificarEstadoOrdenes_Entrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CertificarSolicitudAporteRescate implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CertificarSolicitudAporteRescate.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectSolicitudes.sql");
	private final String SQL_SELECT_ESTADOS = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectEstados.sql");
	private final String SQL_SELECT_COMPROBANTES_NO_LIQUIDADOS = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectComprobantesNoLiquidados.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql");
	private final String SQL_UPDATE_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_UpdateSolicitudes.sql");
	private final String SQL_SELECT_REINVERSION_DESTINO	= loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectReinversionSerieFondoDestino.sql");
	private final String SQL_SELECT_TRASPASO_APV_ASOCIADO = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_ConsultaTraspasoAPVAsociado.sql");
	private final String SQL_SELECT_TRASPASO_NO_APV_ASOCIADO = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_ConsultaTraspasoNoAPVAsociado.sql");
	private final String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectPersonaPreferenciaPagoTGR.sql");
	private final String SQL_SELECT_COBRO_IVA = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectCobroIva.sql");
	private final String SQL_SELECT_MONTO_MONEDA_SISTEMA_SOLICITUDES_REINVERSION = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectMontoMonedaSistemaSolicitudesReinversion.sql");
	private final String SQL_UPDATE_MONTO_MONEDA_SISTEMA_SOLICITUDES_REINVERSION = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_UpdateMontoMonedaSistemaSolicitudesReinversion.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO_FONDO_MUTUO_GENERA_CARGO_ABONO = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectTipoMovimientoFondoMutuoGeneraCargoAbono.sql");
	private final String SQL_SELECT_REINVERSION_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectReinversionSerieFondosSolicitudRescateReinversion.sql");
	private final String SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_SelectParametroOperaConSaldoNegativo.sql");

	public static class EntradaCertificarSolicitudAporteRescate extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_FechaCertificacion;
		public String p_IdTipoCanalIngreso;
		public String p_NotificaOWP;
		public String p_CertificacionAporteSwitchSimulado;
		public List<InSolicitudes> p_Solicitudes;
		
		public EntradaCertificarSolicitudAporteRescate() {
			p_Solicitudes = new ArrayList<InSolicitudes>();
			p_NotificaOWP = "S";
			p_CertificacionAporteSwitchSimulado = "N";
		}
	}
	
	public static class InSolicitudes extends BcsPojo{
		public Long IdSolicitudAporteRescate;
		public String IdTipoCanalIngreso;
		
		public InSolicitudes(){
			this.IdTipoCanalIngreso = "";
		}
	}
	
	public static class SalidaCertificarSolicitudAporteRescate extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoCertificacion> P_OUT_RESULTADO;
		
		public SalidaCertificarSolicitudAporteRescate(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCertificacion>();
		}
	}
	
	public static class SolicitudesProcesoCertificacion extends BcsPojo{
		public Long IdSolicitudAporteRescate;
	    public Long IdSolicitudAporteRescateRelacionado;
	    public Date FechaCertificacion;
	    public String TipoSolicitud;
	    public String IdSerieFondo;
	    public Long IdInstrumento;
	    public String IdTipoInstrumento;
	    public String IdTipoMovimientoFondoMutuo;
	    public String IdEmisor;
	    public Long IdCliente;
	    public Long IdPortafolio;
	    public String IdMoneda;
	    public String IdMonedaPago;
	    public String IdMonedaFondo;
	    public BigDecimal CostoMonedaLocal;
	    public BigDecimal Monto;
	    public BigDecimal ValorCuota;
	    public BigDecimal NumeroCuotas;
	    public BigDecimal ParidadFechaCertificacion;
	    public Date FechaLiquidacion;
	    public Date FechaAceptacion;
	    public Long IdUsuario;
	    public Long IdPersona;
	    public String IdMedioDePago;
	    public String MueveCta;
	    public String CalculoPorMontoCuota;
	    public BigDecimal MontoComision;
	    public BigDecimal MontoPago;
	    public BigDecimal MontoComisionLocal;
	    public BigDecimal MontoPagoLocal;
	    public BigDecimal ValorUFFechaInversion;
	    public BigDecimal CostoUF;
	    public BigDecimal RentabilidadReal;
	    public String IndicadorMLT;
	    public String Overnight;
	    public String Glosa;
	    public String IdSerieFondoDestino;
	    public String IdEmisorDestino;
	    public String OrigenOperacion;
	    public String SolicitudBatch;
	    public String ComisionInformada;
	    public String PropositoPortafolio;
	    public String IdRegimenTributario;
	    public String IdRegimenTributarioAPV;
	    public String TipoCarteraNormativa;
	    public String IdRegimenTributarioDestino;
	    public String TipoCarteraNormativaDestino;
	    public BigDecimal Spread;
	    public String EstadoSolicitud;
	    public Long IdPreferenciaPagoPortafolio;
	    public String IdMonedaSistema;
	    public BigDecimal MontoMonedaSistema;
	    public String EsPorMontoLiquido;
	    public BigDecimal MontoLiquido;
	    public BigDecimal MontoLiquidoLocal;
	    public String IdEstadoComprobante;
	    public String EstadoLiquidacion;
	    public Long FolioCartaLiquidacion;
	    public String EstadoRelacionado;
	    public String EstadoLiquidacionRelacionado;
	    public String IdConcepto;
	    public Long IdEmpleadoEjecutivo;
	    public String EstadoProceso;
	    public String Codigo;
	    public String DescripcionCodigo;
	    public String GeneraSpot;
	    public Long IdIngresoEgreso;
	    public Long IdOperacionSpot;
	    public Long IdCartera;
	    public String RelacionTraspaso;
	    public Long IdAgrupador;
	    public Long IdPersonaEmisorDestino;
	    public String IdTipoCanalIngreso;
	    public String Switch;
	    public String IdFamiliaFondo;
	    public Date FechaOriginalInversion;
	    public Date FechaValorCuota;
	    public String IngresoAutomatico;
	    public String RazonRetiro ="";
	    public Long FolioContrato;
	    public Long IdOmnibus = 0L;
	    public String CertificaSinAsociado;
	    public Long IdComprobanteTesoreria;
	    public String TieneMontoRetencion;
	    public BigDecimal MontoRetencion;
	    public BigDecimal MontoRetencionLocal;
	    public String EsAPV;
	    public String DocumentoIdentidad;
	    public String CodigoPortafolio;
	    public String GeneraOmnibus;
	    public String TipoComision;
	    public String TieneComisionNNRelacionada;
	    public String OperaSaldoNegativo;
	    
	    public SolicitudesProcesoCertificacion(){
	    	this.EstadoProceso = "PEN";
	    	this.Codigo = "";
	    	this.DescripcionCodigo = "";
	    	this.RelacionTraspaso= "N";
	    	this.IdPersonaEmisorDestino= 0L;
	    	this.TieneMontoRetencion = "N";
	    	this.MontoRetencion = BigDecimal.ZERO;
	    	this.MontoRetencionLocal = BigDecimal.ZERO;
	    	this.TieneComisionNNRelacionada = "N";
	    	this.OperaSaldoNegativo = "N";
	    }
	    
	}
	
	public static class Estado extends BcsPojo{
		
		public String IdEstado;
		public String Nombre;
		
	}
	
	public static class ComprobanteNoLiquidado extends BcsPojo{
		public Long IdSolicitudAporteRescate;
		public Long IdComprobante;
		
	}
	
	public static class Traspaso extends BcsPojo{
		public Long IdSolicitudAporteRescate;
		public Long IdPersona;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		
	}
	
	public static class Persona extends BcsPojo{
		public Long IdPersona;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		
		public Persona(){
			this.IdPersona = 0L;
			this.IdCuentaBancaria = 0L;
			this.IdBanco = 0L;
			this.IdTipoCuentaBancaria = "";
			this.IdMoneda = "";
			this.NumeroCuentaBancaria = "";
			this.NombreTipoCuentaBancaria = "";
		}
	}
	
	public static class Cobro extends BcsPojo{
        public String      IdCobro;
        public BigDecimal  ValorCobro;
       
    }
	
	public static class SolicitudReinversion extends BcsPojo{
		public Long IdSolicitudAporteRescate;
		public BigDecimal MontoMonedaSistema;
		
	}

	public static class GeneraCargoAbono extends BcsPojo{
		public String GeneraCargoAbono;
		
		public GeneraCargoAbono() {
			this.GeneraCargoAbono = "N";
		}
	}

	public static class SolicitudOrigenReinversionSerieFondo extends BcsPojo {
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondoOrigen;
		public String FondoOrigenNacionalDolar;
		public String FondoDestinoNacionalDolar;
		public String IdSerieFondoDestino;
		public String FondoOrigenInternacionalDolar;
		public String FondoDestinoInternacionalDolar;

		public SolicitudOrigenReinversionSerieFondo() {
			this.IdSolicitudAporteRescate = 0L;
			this.IdSerieFondoOrigen = "";
			this.FondoOrigenNacionalDolar = "N";
			this.FondoDestinoNacionalDolar = "N";
			this.IdSerieFondoDestino = "";
			this.FondoOrigenInternacionalDolar = "N";
			this.FondoDestinoInternacionalDolar = "N";
		}
	}
	
	public static class ParametroOperaCSN {
		public String ValorIndicador = "N";
		public Long IdEntidad = 0L;
		public String CodigoTipoIndicador = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCertificarSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaCertificarSolicitudAporteRescate.class);
		SalidaCertificarSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCertificarSolicitudAporteRescate logica (Connection con, EntradaCertificarSolicitudAporteRescate entrada) throws Exception {
		logger.info("INICIO::CertificarSolicitudAporteRescate");
		SalidaCertificarSolicitudAporteRescate out = new SalidaCertificarSolicitudAporteRescate();
		

		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_Solicitudes.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> ids = new ArrayList<Long>();
		for (InSolicitudes mov: entrada.p_Solicitudes) {
			if (ids.contains(mov.IdSolicitudAporteRescate)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_DUPLICADA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				ids.add(mov.IdSolicitudAporteRescate);
			}
		}
		
		String fechaSistema = obtenerFechaSistema(con,entrada.p_IdEmpresa);
		String tipoComprobanteFacturaNoApv = obtenerTipoComprobanteFacturaNoAPV(con, entrada.p_IdEmpresa);
		String tipoComprobanteFacturaApv = obtenerTipoComprobanteFacturaAPV(con, entrada.p_IdEmpresa);
		String calcularComisionCertificacion = obtenerCalculaComision(con, entrada.p_IdEmpresa);
		String validarCertificacion = obtenerValidarCertificacion(con, entrada.p_IdEmpresa);
		String rescateGeneraVentaSpotDolar = obtenerRescateGeneraMovimientoSpot(con, entrada.p_IdEmpresa);
		String comisionComprobanteFacturacionAutomatico = obtenerRescateConComisionGeneraComprobanteAutomatico(con, entrada.p_IdEmpresa);
		Long porcentajeRetencion = obtenerPorcentajeRetencion(con, entrada.p_IdEmpresa);
		String notificaOWP = obtenerNotificarOWP(con, entrada.p_IdEmpresa);

		List<Long> idSolicitudes = obtenerIdSolicitudesEntrada(entrada.p_Solicitudes);
		
		List<InSolicitudes> aportesSwitch = new ArrayList<InSolicitudes>();
		List<SolicitudesProcesoCertificacion> solicitudes = obtenerSolicitudes(con,entrada.p_IdEmpresa,idSolicitudes);
		List<Long> idSolicitudesParaConsultaTraspasos = obtenerIdsSolicitudesConsultaTraspasos(solicitudes);
		List<Estado> estados = obtenerEstados(con);
		List<ComprobanteNoLiquidado> comprobantes = obtenerComprobantesNoLiquidados(con, idSolicitudes);
		Map<Long,Traspaso> hashTraspasosAPVAsociados = obtenerTraspasosAPV(con,idSolicitudesParaConsultaTraspasos);
		Map<Long,Traspaso> hashTraspasosNoAPVAsociados = obtenerTraspasosNoAPV(con,idSolicitudesParaConsultaTraspasos);
		Persona personaTGR = obtenerPersonaTGR(con, entrada.p_IdEmpresa);
		Persona personaSII = obtenerPersonaSII(con, entrada.p_IdEmpresa);
		
		Map<Long,Traspaso> hashTraspasos = hashTraspasosAPVAsociados;
		hashTraspasos.putAll(hashTraspasosNoAPVAsociados);
		HashMap<String,Object> hestado = Utils.List2Hash(estados, "IdEstado");
		
		if(solicitudes.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
	
		// Obtener valor cuota cargado usando servicio Precio y tasa (este servicio considerar valor cuota historico y valor cuota vigente)
		Map<Long,Object> hashValorCuotaCargado = obtenerValorCuotaCargado(con,solicitudes, entrada, paisSistema);	
		
		//VALIDACIONES
		int solicitudesPorProcesar = 0;
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			
			EntregarPreciosTasas_Salida_PreciosTasas ValorCuotaCargado = (EntregarPreciosTasas_Salida_PreciosTasas) hashValorCuotaCargado.get(sol.IdSolicitudAporteRescate);
			
			if(!_c.EstadoIngresado.equals(sol.EstadoSolicitud) && !_c.EstadoAceptado.equals(sol.EstadoSolicitud)){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_ESTADO_INCORRECTO_CERTIFICACION";
				continue;
			}
			
			if(!Fecha.stringToSqlDate(sol.FechaCertificacion).equals(entrada.p_FechaCertificacion)){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO";
				continue;
			}
			
			if (ValorCuotaCargado == null)
			{
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_NO_EXISTE_VALOR_CUOTA";
				continue;
			}
			
			if(ValorCuotaCargado.FechaFuente == null) {
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_NO_EXISTE_VALOR_CUOTA";
				continue;
			}
		
			String FechaValorCuotaSolicitud = Fecha.stringToSqlDate(sol.FechaValorCuota);
			String FechaValorCuotaCargado   = Fecha.toStringDate(ValorCuotaCargado.FechaFuente);	
			
			if (!FechaValorCuotaSolicitud.equalsIgnoreCase(FechaValorCuotaCargado))
			{
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_NO_EXISTE_VALOR_CUOTA";
				continue;
			}
			
			if(_c.SI.equals(sol.GeneraOmnibus) && sol.IdOmnibus == null && !(_c.SI.equals(sol.Switch) && _c.AporteReinversion.equals(sol.IdTipoMovimientoFondoMutuo))) {
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_ONMIBUS_NO_CREADO";
				continue;
			}
			
			boolean tieneComprobanteNoLiq = false;
			for(ComprobanteNoLiquidado comprobante : comprobantes){
				if(sol.IdSolicitudAporteRescate.longValue() == comprobante.IdSolicitudAporteRescate.longValue()){
					tieneComprobanteNoLiq = true;
					sol.IdComprobanteTesoreria = comprobante.IdComprobante;
					break;
				}
			}
			if(tieneComprobanteNoLiq){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO";
				continue;
			}
			if(!_c.SI.equals(sol.Switch) && _c.SI.equals(validarCertificacion) && _c.TipoSolicitudAporte.equals(sol.TipoSolicitud)
			   && !_c.EstadoLiquidado.equals(sol.EstadoLiquidacion) && sol.FolioCartaLiquidacion.longValue() == 0){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_VALIDACION_LIQUIDACION_FONDO_MUTUO";
				continue;
			}

			if(!(_c.SI.equals(sol.Switch)) && (_c.NO.equals(sol.CertificaSinAsociado))) {
				if(sol.IdSolicitudAporteRescateRelacionado.longValue() != 0 
				&& (sol.IdSolicitudAporteRescate.longValue() > sol.IdSolicitudAporteRescateRelacionado.longValue() 
				&& (!_c.EstadoCertificado.equals(sol.EstadoRelacionado) || !_c.EstadoLiquidado.equals(sol.EstadoLiquidacionRelacionado)))){
					sol.EstadoProceso = _c.EstadoRechazado;
					sol.Codigo = "UI_SOLICITUD_ASOCIADA_NO_CERTIFICADA";
					continue;
				}
			}
			
			if(personaTGR == null && _c.BonificacionFiscal.equals(sol.IdRegimenTributarioAPV)) {
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_NO_EXISTE_TGR_CONFIGURADA";
				continue;
			}
			
			if(personaSII == null && _c.APV_B.equals(sol.IdRegimenTributarioAPV)) {
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_NO_EXISTE_SII_CONFIGURADA";
				continue;
			}
			
			//SE SUMA VALIDOS
			solicitudesPorProcesar++;
		}

		if(solicitudesPorProcesar == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_RESULTADO.addAll(procesarSalida(con,entrada,solicitudes,hestado));
			insertarLog(con, solicitudes, entrada, fechaSistema);
			return out;
		}
		
		//INDICADORES DE LOS PORTAFOLIOS VALIADOS
		HashMap<Long,Object> hindicadoresPortafolioFFMM =  obtenerIndicadoresPortafolio(con, solicitudes);
		
		//ACTUALIZAMOS MONTOS
		HashMap<Long, Object> hcalculo = obtenerCalculoMontoCuotas(con, entrada.p_IdEmpresa, solicitudes);
		actualizarMontosSolicitudes(solicitudes, hcalculo);
		
		Date fechaCertificacion = obtenerFechaCertificacion(fechaSistema, entrada.p_FechaCertificacion);
		
		//Separamos Aportes con Rescates
		List<SolicitudesProcesoCertificacion> aportes = obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente(solicitudes, _c.TipoSolicitudAporte);
		List<SolicitudesProcesoCertificacion> rescates = obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente(solicitudes, _c.TipoSolicitudRescate);
		
		// CELLINCECT-763: Operar sin saldo en fondomutuo.
		// Consultamos parametro Opera con saldo negativo en el portafolio del cliente.
		ArrayList<Long> portafoliosABuscar = new ArrayList<>();
		SQL sql = new SQL();
				
		aportes.forEach(aporte -> {
			Long portafolio = aporte.IdPortafolio;
			portafoliosABuscar.add(portafolio);
		});
				
		HashMap<String,Object> hashListadoPortafolios = new HashMap<String,Object>(1);
		hashListadoPortafolios.put("IdPortafolio", portafoliosABuscar);
				
		List<ParametroOperaCSN> parametrosOPCSN = sql.query(con, SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO, hashListadoPortafolios, ParametroOperaCSN.class);
		
		aportes.forEach(aporte -> {
			ParametroOperaCSN paramOPCSN = new ParametroOperaCSN();
			paramOPCSN = parametrosOPCSN.stream().filter(parametro -> parametro.IdEntidad.equals(aporte.IdPortafolio)).findFirst().orElse(new ParametroOperaCSN());
			
			aporte.OperaSaldoNegativo = paramOPCSN.ValorIndicador;
		});
				
		//VALIDACIONES DE FLUJOS
		if(aportes.size() > 0){
			//APORTE
			validarSaldoCuentaInversion(con,aportes,fechaCertificacion,entrada.p_IdEmpresa,hindicadoresPortafolioFFMM,fechaSistema);
			validarSaldoCuentaInversionPAC(con,aportes,fechaCertificacion,entrada.p_IdEmpresa,hindicadoresPortafolioFFMM,fechaSistema);
			formatearMontos(con,aportes);
		}
		
		if(rescates.size() > 0){
			//RESCATE
			HashMap<Long,Object> hcalculaCustodiaRescate = calculaCustodiaRescate(con,entrada.p_IdEmpresa,rescates);
			cambiarMontosRescatesPorCantidadCustodia(con, entrada.p_IdEmpresa, rescates, hcalculaCustodiaRescate, rescateGeneraVentaSpotDolar);
			//SE DETERMINA SI EL RESCATE TIENE UN TRASPASO RELACIONADO
			marcarTraspasos(rescates,hashTraspasos);
			marcarRescatesConRetencion(rescates, porcentajeRetencion);
			
			//CALCULO COMISIONES SI EL PARAMETRO ESTA ENCENDIDO
			if(_c.SI.equals(calcularComisionCertificacion)){
				HashMap<Long,Object> hcomisiones = calcularComisiones(con, rescates, entrada);
				cambiarMontosPorComisiones(rescates, hcomisiones);
			}
			formatearMontos(con,rescates);
			esMontoRetencionValido(rescates);
		}

		//APORTES
		if(aportes.size() > 0){
			reCalcularCostoUFAportes(con,aportes,entrada.p_IdEmpresa);
			//CUSTODIA
			SalidaProcesarCustodiaFondoMutuo salidaCustodia = ingresoRetiroCustodia(con, aportes, entrada);
			out.P_OUT_ERRORESCONTADOR += salidaCustodia.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mje : salidaCustodia.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = Math.abs(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudesProcesoCertificacion sol : aportes){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							sol.DescripcionCodigo = mje.descripcion;
							break;
						}
					}
				}
			}
			
			calcularRentabilidadRealAportes(aportes, salidaCustodia);
			
			
			//CUENTA INVERSION
			CrearCargoAbonoSalida salidaCrearCargoAbono = mueveCuentaInversion(con, aportes, entrada, fechaSistema, porcentajeRetencion);
			out.P_OUT_ERRORESCONTADOR += salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = Math.abs(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudesProcesoCertificacion sol : aportes){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							sol.DescripcionCodigo = mje.descripcion;
							break;
						}
					}
				}
			}
		}
		
		
		//RESCATES
		if(rescates.size() > 0){
			//CUSTODIA
			List<SolicitudesProcesoCertificacion> rescatesConRegimen = obtenerRescatesConRegimen(rescates);
			List<SolicitudesProcesoCertificacion> rescatesSinRegimen = obtenerRescatesSinRegimen(rescates);
			SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateConRegimen = ingresoRetiroCustodia(con, rescatesConRegimen, entrada);
			out.P_OUT_ERRORESCONTADOR += salidaCustodiaRescateConRegimen.P_OUT_ERRORESCONTADOR;
			
			SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateSinRegimen = ingresoRetiroCustodia(con, rescatesSinRegimen, entrada);
			out.P_OUT_ERRORESCONTADOR += salidaCustodiaRescateSinRegimen.P_OUT_ERRORESCONTADOR;
			
			List<MensajeSalida> mensajeSalidaCustodia = obtenerMensajeSalidaRescateCustodia(salidaCustodiaRescateConRegimen, salidaCustodiaRescateSinRegimen);
			
			for(MensajeSalida mje : mensajeSalidaCustodia){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = Math.abs(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudesProcesoCertificacion sol : rescates){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							sol.DescripcionCodigo = mje.descripcion;
							break;
						}
					}
				}
			}
			
			List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas = obtenerCarterasRetiradas(salidaCustodiaRescateConRegimen, salidaCustodiaRescateSinRegimen);
			

			
			validarRetiroCarteras(rescates,carterasRetiradas);
			calcularCostoUFRentabilidadRescates(con,rescates,carterasRetiradas,entrada.p_IdEmpresa);
			
			//CUENTA INVERSION
			CrearCargoAbonoSalida salidaCrearCargoAbono = mueveCuentaInversion(con, rescates, entrada, fechaSistema, porcentajeRetencion);
			out.P_OUT_ERRORESCONTADOR += salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = Math.abs(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudesProcesoCertificacion sol : rescates){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							sol.DescripcionCodigo = mje.descripcion;
							break;
						}
					}
				}
			}

			
			//TESORERIA
			SalidaProcesarIngresoEgresoTesoreria salidaTesoreria = mueveTesoreria(con, rescates, entrada, fechaSistema, porcentajeRetencion, hashTraspasos, personaTGR, personaSII, entrada.p_IdEmpresa);
			for(ResultadoProceso_Salida rs : salidaTesoreria.P_OUT_RESULTADOPROCESO){
				if(_c.EstadoRechazadoTesoreria.equals(rs.Resultado)){
					Long id =  Math.abs(rs.Id);
					for(SolicitudesProcesoCertificacion sol : rescates){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue() && rs.Campo.equals(_c.NumeroReferenciaTesoreria)){
							sol.EstadoProceso = _c.EstadoRechazado;
							break;
						}
					}
				}
			}
			
			for(IngresoEgresoGenerado_Salida rs : salidaTesoreria.P_OUT_INGRESOEGRESOGENERADO){
				for(SolicitudesProcesoCertificacion sol : rescates){
					
					if(rs.NumeroReferencia.longValue() == sol.IdSolicitudAporteRescate.longValue()){
						sol.IdIngresoEgreso =rs.IdIngresoEgreso;
						break;
					}
					
				}
				
			}
			
			//MOVIMIENTO FACTURACION RESCATE POR COMISION
			CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = creaMovimientoFacturacion(con, rescates, entrada, tipoComprobanteFacturaNoApv, tipoComprobanteFacturaApv);
			out.P_OUT_ERRORESCONTADOR += salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR;
			for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES) {
				if(MensajeSalida.MENSAJE_ERROR.equals(msj.tipo)){
					Long id = Math.abs(msj.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, msj.tipo, msj.codigo, msj.descripcion));
					for(SolicitudesProcesoCertificacion sol : rescates){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = msj.codigo;
							sol.DescripcionCodigo = msj.descripcion;
							break;
						}
					}
				}
			}
			
			//COMPROBANTE FACTURACION RESCATE POR COMISION
			if ("S".equalsIgnoreCase(comisionComprobanteFacturacionAutomatico)) {
				CrearComprobante_Salida salidaCrearComprobante = creaComprobanteFacturacion(con, rescates, entrada, salidaCrearMovimientoComprobante, fechaSistema);
				out.P_OUT_ERRORESCONTADOR += salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR;
				for (MensajeSalida msj : salidaCrearComprobante.P_OUT_MENSAJES) {
					if(MensajeSalida.MENSAJE_ERROR.equals(msj.tipo)){
						Long id = Math.abs(msj.id);
						out.P_OUT_MENSAJES.add(new MensajeSalida(id, msj.tipo, msj.codigo, msj.descripcion));
						for(SolicitudesProcesoCertificacion sol : rescates){
							if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = msj.codigo;
								sol.DescripcionCodigo = msj.descripcion;
								break;
							}
						}
					}
				}
			}
		
			//CREACION APORTE REINVERSION
			SalidaCrearSolicitudAporteReinversion salidaCrearSolicitudAporteReinversion = crearAporteReinversion(con, rescates, entrada, carterasRetiradas);
			out.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudAporteReinversion.P_OUT_ERRORESCONTADOR;
			for (MensajeSalida msj : salidaCrearSolicitudAporteReinversion.P_OUT_MENSAJES) {
				if(MensajeSalida.MENSAJE_ERROR.equals(msj.tipo)){
					Long id = Math.abs(msj.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, msj.tipo, msj.codigo, msj.descripcion));
					for(SolicitudesProcesoCertificacion sol : rescates){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = msj.codigo;
							sol.DescripcionCodigo = msj.descripcion;
							break;
						}
					}
				}
			}
			
			//switch
			for(ResultadoSolicitud res : salidaCrearSolicitudAporteReinversion.P_OUT_RESULTADO){
				if(res.Switch.equals("S")){
					InSolicitudes aporte = new InSolicitudes();
					aporte.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
					aporte.IdTipoCanalIngreso = res.IdTipoCanalIngreso;
					aportesSwitch.add(aporte);
				}
			}
			
			//CREACION DE VENTA SPOT
			SalidaProcesarIngresoOperacionSpot salidaSpot = mueveSpot(con, rescates, entrada);
				
			if (salidaSpot.P_OUT_ERRORESCONTADOR != null){
				out.P_OUT_ERRORESCONTADOR += salidaSpot.P_OUT_ERRORESCONTADOR;	
			}

			if (salidaSpot != null)
			{
				for(OperacionObjOut rs : salidaSpot.P_OUT_IDOPERACIONSPOT){
					
					for(SolicitudesProcesoCertificacion sol : rescates){
						if((sol.IdSolicitudAporteRescate.equals(Long.parseLong(rs.IdInterno))) ){
							if(_c.SpotEstadoRechazado.equals(rs.Estado)){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = "ERROR_CREAR_SPOT";
								break;
							}else{
								sol.IdOperacionSpot = rs.IdOperacionSpot;
								break;
							}
							
						}
						
					}
					
				}	
				for(MensajeSalida rs : salidaSpot.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(rs.tipo)){
						for(SolicitudesProcesoCertificacion sol : rescates){
							if(sol.IdSolicitudAporteRescate.equals(Math.abs(rs.id)) || rs.id.equals(0L)) {
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = rs.codigo;
								sol.DescripcionCodigo = rs.descripcion;
								break;
							}
							
						}
					}
				
				}					
			}
			
			
		}//fin rescates
		
		List<SolicitudesProcesoCertificacion> solActualizar = actualizarSolicitudes(con, solicitudes);
		notificar(con, solActualizar, entrada);
		
		if(aportesSwitch.size() > 0) {
			SalidaCertificarSolicitudAporteRescate out_switch = certificarAportesSwitch(con, aportesSwitch, entrada);
			out.P_OUT_ERRORESCONTADOR += out_switch.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(out_switch.P_OUT_MENSAJES);
			out.P_OUT_RESULTADO.addAll(out_switch.P_OUT_RESULTADO);
		}
		
		cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot(con,solicitudes);
		
		//Notificacion OpenWorkPlace
		if(_c.SI.equalsIgnoreCase(notificaOWP) && _c.SI.equalsIgnoreCase(entrada.p_NotificaOWP)){
			notificarOWP(con,aportesSwitch,solActualizar,entrada.p_IdEmpresa,entrada.p_IdUsuario);
		}
		
		//AGREGAR LOG
		{
			agregarLogSistema(con,entrada,fechaSistema);
		}
		
		//CAMBIAMOS LOS PENDIENTES A EXITO
		marcarSolicitudesExitosas(solicitudes);
		out.P_OUT_RESULTADO.addAll(procesarSalida(con,entrada,solicitudes,hestado));
		insertarLog(con, solicitudes, entrada, fechaSistema);
		
		if(_c.SI.equals(entrada.p_SimularOperacion)){
			out.P_OUT_ERRORESCONTADOR=1L;
		}
		
		logger.info("FIN::CertificarSolicitudAporteRescate");
		return out;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private String obtenerTipoComprobanteFacturaNoAPV(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerTipoComprobanteFacturaNoAPV");
		String tipoComprobanteFacturaNoAPV = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "TipoComprobanteFacturaNoApv");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerTipoComprobanteFacturaNoAPV");
		return tipoComprobanteFacturaNoAPV;
	}
	
	private String obtenerTipoComprobanteFacturaAPV(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerTipoComprobanteFacturaAPV");
		String tipoComprobanteFacturaAPV = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "TipoComprobanteFacturaApv");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerTipoComprobanteFacturaAPV");
		return tipoComprobanteFacturaAPV;
	}
	
	private String obtenerCalculaComision(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerCalculaComision");
		String calculaComision = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "CALCULAR_COMISION_CERTIFICACION");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerCalculaComision");
		return calculaComision;
	}
	
	private String obtenerValidarCertificacion(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerValidarCertificacion");
		String validarCertificacion = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "VALIDAR_CERTIFICACION");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerValidarCertificacion");
		return validarCertificacion;
	}
	
	private String obtenerRescateGeneraMovimientoSpot(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerRescateGeneraMovimientoSpot");
		String rescateGeneraMovimientoSpot = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "RESCATE_GENERA_VENTA_SPOT_DOLAR");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerRescateGeneraMovimientoSpot");
		return rescateGeneraMovimientoSpot;
	}
	
	private String obtenerRescateConComisionGeneraComprobanteAutomatico(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerRescateConComisionGeneraComprobanteAutomatico");
		String rescateGeneraComprobante = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "RESCATE_COMISION_COMPROBANTE_AUTOMATICO");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerRescateConComisionGeneraComprobanteAutomatico");
		return rescateGeneraComprobante;
	}
	
	private List<Long> obtenerIdSolicitudesEntrada(List<InSolicitudes> solicitudes){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerRescateConComisionGeneraComprobanteAutomatico");
		List<Long> ids = solicitudes.parallelStream().map(solicitud->solicitud.IdSolicitudAporteRescate).distinct().collect(Collectors.toList());
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerRescateConComisionGeneraComprobanteAutomatico");
		return ids;
	}
	
	private List<SolicitudesProcesoCertificacion> obtenerSolicitudes(Connection con,Long idEmpresa, List<Long> idSolicitudes) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerSolicitudes");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<SolicitudesProcesoCertificacion> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, params, SolicitudesProcesoCertificacion.class);
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerSolicitudes");
		return solicitudes;
	}
	
	private List<Estado> obtenerEstados(Connection con) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerEstados");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Estado> estados = sql.query(con, SQL_SELECT_ESTADOS, params, Estado.class);
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerEstados");
		return estados;
	}
	
	private List<ComprobanteNoLiquidado> obtenerComprobantesNoLiquidados(Connection con, List<Long> idSolicitudes) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerComprobantesNoLiquidados");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<ComprobanteNoLiquidado> comprobantes = sql.query(con, SQL_SELECT_COMPROBANTES_NO_LIQUIDADOS, params, ComprobanteNoLiquidado.class);
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerComprobantesNoLiquidados");
		return comprobantes;
	}
	
	private Map<Long,Traspaso> obtenerTraspasosAPV(Connection con, List<Long> idSolicitudes) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerTraspasosAPV");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<Traspaso> traspasosAPVAsociados = sql.query(con, SQL_SELECT_TRASPASO_APV_ASOCIADO, params, Traspaso.class);
		Map<Long,Traspaso> hashTraspasosAPVAsociados = traspasosAPVAsociados.parallelStream().collect(Collectors.toMap(traspaso->traspaso.IdSolicitudAporteRescate, traspaso->traspaso));
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerTraspasosAPV");
		return hashTraspasosAPVAsociados;
	}
	
	private Map<Long,Traspaso> obtenerTraspasosNoAPV(Connection con, List<Long> idSolicitudes) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerTraspasosNoAPV");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<Traspaso> traspasosNoAPVAsociados = sql.query(con, SQL_SELECT_TRASPASO_NO_APV_ASOCIADO, params, Traspaso.class);
		Map<Long,Traspaso> hashTraspasosNoAPVAsociados = traspasosNoAPVAsociados.parallelStream().collect(Collectors.toMap(traspaso->traspaso.IdSolicitudAporteRescate, traspaso->traspaso));
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerTraspasosNoAPV");
		return hashTraspasosNoAPVAsociados;
	}
	
	private Persona obtenerPersonaTGR(Connection con, Long idEmpresa) throws SQLException {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerPersonaTGR");
		Persona personaTGR = null;
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		String documentoIdentidadTGR = obtenerDocumentoIdentidadTGR(con, idEmpresa);
		params.put("DocumentoIdentidad", documentoIdentidadTGR);
		List<Persona> personasTGR = sql.query(con, SQL_SELECT_PERSONA, params, Persona.class);
		if(personasTGR.size() > 0) {
			personaTGR = personasTGR.get(0);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerPersonaTGR");
		return personaTGR;
	}
	
	private String obtenerDocumentoIdentidadTGR(Connection con, Long idEmpresa) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerDocumentoIdentidadTGR");
		String documentoIdentidadTGR = "";
		try {
			documentoIdentidadTGR = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "APV.DocumentoIdentidadPersonaTGR");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::Parametro APV.DocumentoIdentidadPersonaTGR no existe.");
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerDocumentoIdentidadTGR");
		return documentoIdentidadTGR;
	}
	
	private Persona obtenerPersonaSII(Connection con, Long idEmpresa) throws SQLException {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerPersonaTGR");
		Persona personaSII = null;
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		String documentoIdentidadSII = obtenerDocumentoIdentidadSII(con, idEmpresa);
		params.put("DocumentoIdentidad", documentoIdentidadSII);
		List<Persona> personasSII = sql.query(con, SQL_SELECT_PERSONA, params, Persona.class);
		if(personasSII.size() > 0) {
			personaSII = personasSII.get(0);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerPersonaTGR");
		return personaSII;
	}
	
	private String obtenerDocumentoIdentidadSII(Connection con, Long idEmpresa) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerDocumentoIdentidadSII");
		String documentoIdentidadSII = "";
		try {
			documentoIdentidadSII = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "APV.DocumentoIdentidadPersonaSII");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::Parametro APV.DocumentoIdentidadPersonaSII no existe.");
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerDocumentoIdentidadSII");
		return documentoIdentidadSII;
	}
	
	private HashMap<Long,Object> obtenerIndicadoresPortafolio(Connection con, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerIndicadoresPortafolio");
		List<Long> portafolios = filtrarSolicitudesPendientesYAdvertencias(solicitudes).map(solicitud->solicitud.IdPortafolio).distinct().collect(Collectors.toList());
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPortafolio", portafolios);
		List<IndicadoresPortafolioFondoMutuo> indicadoresPortafolioFFMM = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM, params, IndicadoresPortafolioFondoMutuo.class);
		HashMap<Long,Object> hindicadoresPortafolioFFMM =  Utils.List2Hash(indicadoresPortafolioFFMM, "IdPortafolio");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerIndicadoresPortafolio");
		return hindicadoresPortafolioFFMM;
	}
	
	private Stream<SolicitudesProcesoCertificacion> filtrarSolicitudesPendientesYAdvertencias(List<SolicitudesProcesoCertificacion> solicitudes){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.filtarSolicitudes");
		Stream<SolicitudesProcesoCertificacion> solicitudesFiltradas = solicitudes.parallelStream().filter(solicitud->_c.EstadoPendiente.equals(solicitud.EstadoProceso) || _c.EstadoAdvertencia.equals(solicitud.EstadoProceso));
		logger.trace("FIN::CertificarSolicitudAporteRescate.filtarSolicitudes");
		return solicitudesFiltradas;
	}
	
	private Stream<SolicitudesProcesoCertificacion> filtrarSolicitudesPendientes(List<SolicitudesProcesoCertificacion> solicitudes){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.filtarSolicitudes");
		Stream<SolicitudesProcesoCertificacion> solicitudesFiltradas = solicitudes.parallelStream().filter(solicitud->_c.EstadoPendiente.equals(solicitud.EstadoProceso));
		logger.trace("FIN::CertificarSolicitudAporteRescate.filtarSolicitudes");
		return solicitudesFiltradas;
	}
	
	private HashMap<Long, Object> obtenerCalculoMontoCuotas(Connection con, Long idEmpresa, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerCalculoMontoCuotas");
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = idEmpresa;
		
		entradaCalculo.p_Series = filtrarSolicitudesPendientesYAdvertencias(solicitudes).map(solicitud->{
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			serie.IdInterno = solicitud.IdSolicitudAporteRescate;
		
			serie.Fecha =  Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		 	serie.Monto = solicitud.CostoMonedaLocal;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud =  solicitud.TipoSolicitud;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = idEmpresa;
		 	serie.IdTipoMovimientoFondoMutuo = (solicitud.IdTipoMovimientoFondoMutuo == null)? "":solicitud.IdTipoMovimientoFondoMutuo ;	
		 	return serie;
		}).collect(Collectors.toList());
		
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = new SalidaCalcularMontoNumeroCuota();
		if(entradaCalculo.p_Series.size() > 0){
			salidaCalculo = calculo.logica(con, entradaCalculo);
		}
		
		HashMap<Long, Object> hcalculo = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerCalculoMontoCuotas");
		return hcalculo;
	}
	
	private void actualizarMontosSolicitudes(List<SolicitudesProcesoCertificacion> solicitudes, HashMap<Long, Object> hcalculo) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.actualizarMontosSolicitudes");
		filtrarSolicitudesPendientesYAdvertencias(solicitudes).forEach(solicitud->{
			CalcularMontoNumeroCuota.Resultado r = (CalcularMontoNumeroCuota.Resultado) hcalculo.get(solicitud.IdSolicitudAporteRescate);
			if(r == null || !_c.OK.equals(r.Resultado)){
				solicitud.EstadoProceso = _c.EstadoRechazado;
				solicitud.Codigo = "ERROR_CALCULO_MONTO_CUOTA";
			}else if(_c.NO.equals(solicitud.TieneComisionNNRelacionada)) {
				solicitud.CostoMonedaLocal = r.MontoDestino;
				solicitud.Monto = r.Monto;
				solicitud.ValorCuota = r.ValorCuota;
				solicitud.NumeroCuotas = r.NumeroCuotas;
				solicitud.ParidadFechaCertificacion = r.ValorParidad;
				solicitud.CostoUF = (_c.AporteReinversion.equals(solicitud.IdTipoMovimientoFondoMutuo)) ? solicitud.CostoUF : r.CostoEnFactorAjuste;
				solicitud.ValorUFFechaInversion = (_c.AporteReinversion.equals(solicitud.IdTipoMovimientoFondoMutuo)) ? solicitud.ValorUFFechaInversion : r.ValorFactorAjuste;
				solicitud.IdMonedaSistema = r.IdMonedaSistema;
				solicitud.MontoMonedaSistema = _c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud) && solicitud.IdOperacionSpot != null && !solicitud.IdOperacionSpot.equals(0L) ? solicitud.MontoMonedaSistema : r.MontoMonedaSistema; //CUANDO ES APORTE Y TIENE SPOT SE DEBE RESPETAR EL MONTO DEL SPOT
				solicitud.MontoPago = r.Monto;
				solicitud.MontoPagoLocal = r.MontoDestino;
			}
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.actualizarMontosSolicitudes");
	}

	private Date obtenerFechaCertificacion(String fechaSistema, String fechaEntrada) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerFechaCertificacion");
		Date fechaCertificacion = Fecha.toDate(fechaEntrada).compareTo(Fecha.toDate(fechaSistema)) < 0 ? Fecha.toDate(fechaSistema) : Fecha.toDate(fechaEntrada);
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerFechaCertificacion");
		return fechaCertificacion;
	}
	
	private List<SolicitudesProcesoCertificacion> obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente(List<SolicitudesProcesoCertificacion> solicitudes, String tipoSolicitud){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente");
		List<SolicitudesProcesoCertificacion> solicitudesTipoOperacionDesordenada = filtrarSolicitudesPendientesYAdvertencias(solicitudes).filter(solicitud->tipoSolicitud.equals(solicitud.TipoSolicitud)).collect(Collectors.toList());
		Comparator<SolicitudesProcesoCertificacion> comparadorAscendiente = obtenerComparadorSolicitudesAscendiente();
		List<SolicitudesProcesoCertificacion> solicitudesTipoOperacionOrdenada = solicitudesTipoOperacionDesordenada.parallelStream().sorted(comparadorAscendiente).collect(Collectors.toList());
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente");
		return solicitudesTipoOperacionOrdenada;
	}
	
	private Comparator<SolicitudesProcesoCertificacion> obtenerComparadorSolicitudesAscendiente() {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerComparadorSolicitudesAscendiente");
		Comparator<SolicitudesProcesoCertificacion> comparadorSolicitudesAscendiente = new Comparator<SolicitudesProcesoCertificacion>() {
		    @Override
		    public int compare(SolicitudesProcesoCertificacion primeraSolicitud, SolicitudesProcesoCertificacion segundaSolicitud) {
		        if(primeraSolicitud.IdSolicitudAporteRescate.compareTo(segundaSolicitud.IdSolicitudAporteRescate) == 0){
		            return 0;
		        }
		        return primeraSolicitud.IdSolicitudAporteRescate.compareTo(segundaSolicitud.IdSolicitudAporteRescate) == 1 ? 1 : -1;
		     }
		};
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerComparadorSolicitudesAscendiente");
		return comparadorSolicitudesAscendiente;
	}
	
	private void validarSaldoCuentaInversion(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, Date fechaCertificacion, Long idEmpresa, HashMap<Long,Object> hindicadoresPortafolioFFMM, String fechaSistema) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerSaldoCuentaInversion");
		//FLUJO CAJA
		List<Long> portFlujo = new ArrayList<Long>();
		List<String> monedaFlujo = new ArrayList<String>();
		HashMap<String,BigDecimal> portTotalesMontos = new HashMap<String,BigDecimal>();
		SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
		SaldoFlujoCaja.ServiceEntrada entradaSaldoFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
		entradaSaldoFlujoCaja.P_IDEMPRESA = idEmpresa;
		entradaSaldoFlujoCaja.P_FECHADESDE = Fecha.stringToSqlDate(fechaCertificacion);
		entradaSaldoFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaCertificacion);
		entradaSaldoFlujoCaja.P_USO = _c.UsoFlujoCajaCertificacion;
		entradaSaldoFlujoCaja.P_DESPLEGARENUI = _c.NO;
		entradaSaldoFlujoCaja.P_AGRUPARPORORIGEN = _c.NO;
		entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
		
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			if(!"PAC".equals(sol.IdTipoCanalIngreso)) {
				IndicadoresPortafolioFondoMutuo ind = (IndicadoresPortafolioFondoMutuo) hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
				String sobregiro = ind != null ? ind.OperaLineaSobregiro : _c.NO;
				
				
				if(_c.SI.equals(sol.MueveCta) && _c.OrigenManual.equals(sol.OrigenOperacion) && _c.NO.equals(sobregiro)){
					String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
					BigDecimal montoAcum = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
					portTotalesMontos.put(id, montoAcum.add(sol.CostoMonedaLocal, MathContext.DECIMAL128));
					if(!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMoneda)){
						SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR saldo = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
						saldo.IdPortafolio = sol.IdPortafolio;
						saldo.IdMoneda = sol.IdMoneda;
						entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.add(saldo);
						if(!portFlujo.contains(sol.IdPortafolio)) portFlujo.add(sol.IdPortafolio);
						if(!monedaFlujo.contains(sol.IdMoneda)) monedaFlujo.add(sol.IdMoneda);
					}
				}
			}
		}
		
		SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = new SaldoFlujoCaja.ServiceSalida();
		salidaFlujoCaja.P_OUT_FLUJOCAJA = new ArrayList<SaldoFlujoCaja.ObjFLUJOCAJA>();
		if(entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.size() > 0){
			salidaFlujoCaja = saldoFlujoCaja.logica(con, entradaSaldoFlujoCaja);
		}
		
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			if(!"PAC".equals(sol.IdTipoCanalIngreso)) {
				IndicadoresPortafolioFondoMutuo ind = (IndicadoresPortafolioFondoMutuo) hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
				String sobregiro = ind != null ? ind.OperaLineaSobregiro : _c.NO;
				
				
				
				
				if(_c.SI.equals(sol.MueveCta) && _c.OrigenManual.equals(sol.OrigenOperacion) && _c.NO.equals(sobregiro)){
					String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
					BigDecimal saldo = BigDecimal.ZERO;
					BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
					String fecha = sol.FechaCertificacion.compareTo(Fecha.toDate(fechaSistema)) < 0 ? fechaSistema : Fecha.stringToSqlDate(sol.FechaCertificacion);
					for(SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujoCaja.P_OUT_FLUJOCAJA){
						if(f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equals(sol.IdMoneda) && f.FechaSaldo.equals(fecha)){
							saldo = f.Saldo;
						}
					}
					if(("N").equalsIgnoreCase(sol.OperaSaldoNegativo)) {
						if(total.compareTo(saldo) == 1){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = "UI_SALDO_INSUFICIENTE_FLUJO_CAJA";
							continue;
						}
					}
				}
			}
		}
		
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerSaldoCuentaInversion");
	}
	
	private void validarSaldoCuentaInversionPAC(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, Date fechaCertificacion, Long idEmpresa, HashMap<Long,Object> hindicadoresPortafolioFFMM, String fechaSistema) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerSaldoCuentaInversion");
		//FLUJO CAJA
		List<Long> portFlujo = new ArrayList<Long>();
		List<String> monedaFlujo = new ArrayList<String>();
		HashMap<String,BigDecimal> portTotalesMontos = new HashMap<String,BigDecimal>();
		SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
		SaldoFlujoCaja.ServiceEntrada entradaSaldoFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
		entradaSaldoFlujoCaja.P_IDEMPRESA = idEmpresa;
		entradaSaldoFlujoCaja.P_FECHADESDE = Fecha.stringToSqlDate(fechaCertificacion);
		entradaSaldoFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaCertificacion);
		entradaSaldoFlujoCaja.P_USO = _c.UsoFlujoCajaCertificacionPac;
		entradaSaldoFlujoCaja.P_DESPLEGARENUI = _c.NO;
		entradaSaldoFlujoCaja.P_AGRUPARPORORIGEN = _c.NO;
		entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
		
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			if("PAC".equals(sol.IdTipoCanalIngreso)) {
				IndicadoresPortafolioFondoMutuo ind = (IndicadoresPortafolioFondoMutuo) hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
				String sobregiro = ind != null ? ind.OperaLineaSobregiro : _c.NO;
				
				
				if(_c.SI.equals(sol.MueveCta) && _c.OrigenManual.equals(sol.OrigenOperacion) && _c.NO.equals(sobregiro)){
					String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
					BigDecimal montoAcum = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
					portTotalesMontos.put(id, montoAcum.add(sol.CostoMonedaLocal, MathContext.DECIMAL128));
					if(!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMoneda)){
						SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR saldo = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
						saldo.IdPortafolio = sol.IdPortafolio;
						saldo.IdMoneda = sol.IdMoneda;
						entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.add(saldo);
						if(!portFlujo.contains(sol.IdPortafolio)) portFlujo.add(sol.IdPortafolio);
						if(!monedaFlujo.contains(sol.IdMoneda)) monedaFlujo.add(sol.IdMoneda);
					}
				}
			}
		}
		
		SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = new SaldoFlujoCaja.ServiceSalida();
		salidaFlujoCaja.P_OUT_FLUJOCAJA = new ArrayList<SaldoFlujoCaja.ObjFLUJOCAJA>();
		if(entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.size() > 0){
			salidaFlujoCaja = saldoFlujoCaja.logica(con, entradaSaldoFlujoCaja);
		}
		
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			if("PAC".equals(sol.IdTipoCanalIngreso)) {
				IndicadoresPortafolioFondoMutuo ind = (IndicadoresPortafolioFondoMutuo) hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
				String sobregiro = ind != null ? ind.OperaLineaSobregiro : _c.NO;
				
				
				
				
				if(_c.SI.equals(sol.MueveCta) && _c.OrigenManual.equals(sol.OrigenOperacion) && _c.NO.equals(sobregiro)){
					String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
					BigDecimal saldo = BigDecimal.ZERO;
					BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
					String fecha = sol.FechaCertificacion.compareTo(Fecha.toDate(fechaSistema)) < 0 ? fechaSistema : Fecha.stringToSqlDate(sol.FechaCertificacion);
					for(SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujoCaja.P_OUT_FLUJOCAJA){
						if(f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equals(sol.IdMoneda) && f.FechaSaldo.equals(fecha)){
							saldo = f.Saldo;
						}
					}
					if(("N").equalsIgnoreCase(sol.OperaSaldoNegativo)) {
						if(total.compareTo(saldo) == 1){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = "UI_SALDO_INSUFICIENTE_FLUJO_CAJA";
							continue;
						}
					}
				}
			}
		}
		
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerSaldoCuentaInversion");
	}
	
	private HashMap<Long,Object> calculaCustodiaRescate(Connection con, Long idEmpresa, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.calculaCustodiaRescate");
		CalculaCustodiaRescate calculaCustodiaRescate = new CalculaCustodiaRescate();
		EntradaCalculaCustodiaRescate entradacalculaCustodiaRescate = new EntradaCalculaCustodiaRescate();
		entradacalculaCustodiaRescate.p_IdEmpresa = idEmpresa;
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			CalculaCustodiaRescate.InSolicitud solCus = new CalculaCustodiaRescate.InSolicitud();
			//solCus = Utils.copyPojo(sol, solCus);
			solCus = getPojoSolCus(sol,solCus);
			entradacalculaCustodiaRescate.p_Solicitudes.add(solCus);
		}
		
		SalidaCalculaCustodiaRescate salidaCalculaCustodiaRescate = calculaCustodiaRescate.logica(con, entradacalculaCustodiaRescate);
		HashMap<Long,Object> hcalculaCustodiaRescate = Utils.List2Hash(salidaCalculaCustodiaRescate.P_OUT_RESULTADO, "IdSolicitudAporteRescate");
		logger.trace("FIN::CertificarSolicitudAporteRescate.calculaCustodiaRescate");
		return hcalculaCustodiaRescate;
	}
	
	private void cambiarMontosRescatesPorCantidadCustodia(Connection con, Long idEmpresa, List<SolicitudesProcesoCertificacion> rescates, HashMap<Long,Object> hcalculaCustodiaRescate, String rescateGeneraVentaSpotDolar) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.cambiarMontosRescatesPorCantidadCustodia");
		rescates.parallelStream().forEach(sol->{
			ResultadoCalculaCustodiaRescate custodia = (ResultadoCalculaCustodiaRescate) hcalculaCustodiaRescate.get(sol.IdSolicitudAporteRescate);
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
			parametrosMensaje.put("DocumentoIdentidad", sol.DocumentoIdentidad);
			parametrosMensaje.put("CodigoPortafolio", sol.CodigoPortafolio);
			parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
			if((custodia == null || _c.EstadoRechazado.equals(custodia.Estado)) || (_c.EstadoAdvertencia.equals(custodia.Estado) && esOmnibusProcesado(sol.IdOmnibus))){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION";
				parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
				parametrosMensaje.put("CantidadCustodia",custodia.CantidadDisponible.toPlainString());
				sol.DescripcionCodigo = obtenerDescripcionMensajeSalida(con, idEmpresa, sol.Codigo, parametrosMensaje);
			}else {
				if(_c.EstadoAdvertencia.equals(custodia.Estado)){
					sol.EstadoProceso = _c.EstadoAdvertencia;
					sol.Codigo = "UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION";
					parametrosMensaje.put("CantidadRetiroInicial", sol.NumeroCuotas.toPlainString());
					parametrosMensaje.put("CantidadRetiroFinal", custodia.NumeroCuotas.toPlainString());
					parametrosMensaje.put("CantidadCustodia",custodia.CantidadDisponible.toPlainString());
					sol.DescripcionCodigo = obtenerDescripcionMensajeSalida(con, idEmpresa, sol.Codigo, parametrosMensaje);
				}
				if(_c.EstadoAdvertencia.equals(custodia.Estado) || (_c.CalculoPorTotal.equals(sol.CalculoPorMontoCuota) && esOmnibusPorProcesar(sol.IdOmnibus))){
					sol.CostoMonedaLocal = custodia.MontoDestino;
					sol.Monto = custodia.Monto;
					sol.ValorCuota = custodia.ValorCuota;
					sol.NumeroCuotas = custodia.NumeroCuotas;
					sol.ParidadFechaCertificacion = custodia.ValorParidad;
					sol.CostoUF = custodia.CostoEnFactorAjuste;
					sol.ValorUFFechaInversion = custodia.ValorFactorAjuste;
					sol.MontoMonedaSistema = custodia.MontoMonedaSistema;
					sol.MontoPago = custodia.Monto;
					sol.MontoPagoLocal = custodia.MontoDestino;
				}
				
				if(_c.SI.equalsIgnoreCase(rescateGeneraVentaSpotDolar) && (_c.RescateComisionNN.equals(sol.IdTipoMovimientoFondoMutuo) ||_c.Dolar.equalsIgnoreCase(sol.IdMoneda)) && _c.Pesos.equalsIgnoreCase(sol.IdMonedaPago) ){
					sol.GeneraSpot=_c.SI;
				}else{
					sol.GeneraSpot=_c.NO;
				}
			}
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.cambiarMontosRescatesPorCantidadCustodia");
	}
	
	private Boolean esOmnibusProcesado(Long idOmnibus) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.esIdOmnibusValido");
		Boolean esIdOmnibusValido = idOmnibus != null && idOmnibus > 0;
		logger.trace("FIN::CertificarSolicitudAporteRescate.esIdOmnibusValido");
		return esIdOmnibusValido;
	}
	
	private Boolean esOmnibusPorProcesar(Long idOmnibus) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.esIdOmnibusInValido");
		Boolean esIdOmnibusValido = idOmnibus == null || idOmnibus.equals(0L);
		logger.trace("FIN::CertificarSolicitudAporteRescate.esIdOmnibusInValido");
		return esIdOmnibusValido;
	}
	
	private String obtenerDescripcionMensajeSalida(Connection con, Long idEmpresa, String codigo, HashMap<String,String> parametrosMensaje) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerDescripcionMensajeSalida");
		String descripcion = "";
		try {
			descripcion = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,parametrosMensaje,con,idEmpresa).descripcion;
		}catch(Exception e) {
			logger.trace("ADVERTENCIA::CertificarSolicitudAporteRescate.obtenerDescripcionMensajeSalida:: Mensaje no encontrado para codigo: " + descripcion);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerDescripcionMensajeSalida");
		return descripcion;
	}
	
	private void marcarTraspasos(List<SolicitudesProcesoCertificacion> rescates, Map<Long,Traspaso> hashTraspasos) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.marcarTraspasos");
		rescates.parallelStream().forEach(sol->{
			if(hashTraspasos.get(sol.IdSolicitudAporteRescate) != null || hashTraspasos.get(sol.IdSolicitudAporteRescateRelacionado) != null) {
				sol.RelacionTraspaso=_c.SI;
			}
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.marcarTraspasos");
	}
	
	private void marcarRescatesConRetencion(List<SolicitudesProcesoCertificacion> rescates, Long porcentajeRetencion) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.marcarRescatesConRetencion");
		rescates.parallelStream().forEach(sol->{
			if(_c.RescateFondoMutuo.equals(sol.IdTipoMovimientoFondoMutuo) 
					&&(_c.APV_AN.equals(sol.IdRegimenTributarioAPV) || _c.APV_B.equals(sol.IdRegimenTributarioAPV)) 
					&& !_c.CuentaInversion.equals(sol.IdMedioDePago) 
					&& !_c.SI.equals(sol.RelacionTraspaso) 
					&& !_c.SI.equals(sol.GeneraSpot) 
					&& porcentajeRetencion.compareTo(0L) != 0) {
				sol.TieneMontoRetencion = _c.SI;
				sol.MontoRetencion = sol.Monto.multiply(BigDecimal.valueOf(porcentajeRetencion)).divide(BigDecimal.valueOf(100));
				sol.MontoRetencionLocal = sol.CostoMonedaLocal.multiply(BigDecimal.valueOf(porcentajeRetencion)).divide(BigDecimal.valueOf(100));
			}
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.marcarRescatesConRetencion");
	}
	
	private void cambiarMontosPorComisiones(List<SolicitudesProcesoCertificacion> rescates, HashMap<Long,Object> hcomisiones) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.cambiarMontosPorComisiones");
		rescates.parallelStream().forEach(solicitud->{
			ResultadoCalcularComision comision = (ResultadoCalcularComision) hcomisiones.get(solicitud.IdSolicitudAporteRescate);
			if(comision != null && _c.NO.equals(solicitud.TieneComisionNNRelacionada)){
				solicitud.MontoComision = comision.Comision;
				solicitud.MontoComisionLocal = comision.ComisionLocal;
				solicitud.MontoPagoLocal = solicitud.CostoMonedaLocal.subtract(solicitud.MontoComisionLocal, MathContext.DECIMAL128);
				solicitud.MontoPago = solicitud.Monto.subtract(solicitud.MontoComision, MathContext.DECIMAL128);
			}
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.cambiarMontosPorComisiones");
	}
	
	private void formatearMontos(Connection con, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.formatearMontos");
		for(SolicitudesProcesoCertificacion solicitud : solicitudes) {
			solicitud.Monto = MonedaFormato.formatear(con, solicitud.Monto, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoComision = MonedaFormato.formatear(con, solicitud.MontoComision, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoPago = MonedaFormato.formatear(con, solicitud.MontoPago, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoLiquido = MonedaFormato.formatear(con, solicitud.MontoLiquido, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoRetencion = MonedaFormato.formatear(con, solicitud.MontoRetencion, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    
		    solicitud.CostoMonedaLocal = MonedaFormato.formatear(con, solicitud.CostoMonedaLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoComisionLocal = MonedaFormato.formatear(con, solicitud.MontoComisionLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoPagoLocal = MonedaFormato.formatear(con, solicitud.MontoPagoLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoLiquidoLocal = MonedaFormato.formatear(con, solicitud.MontoLiquidoLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoRetencionLocal = MonedaFormato.formatear(con, solicitud.MontoRetencionLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    
		    solicitud.MontoMonedaSistema = MonedaFormato.formatear(con, solicitud.MontoMonedaSistema, MonedaFormato.MONTO, solicitud.IdMonedaSistema).valor;;

		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.formatearMontos");
	}
	
	private void esMontoRetencionValido(List<SolicitudesProcesoCertificacion> rescates) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.esMontoRetencionValido");
		rescates.parallelStream().filter(rescate->_c.SI.equals(rescate.TieneMontoRetencion)).forEach(rescate->{
			if(rescate.MontoRetencion.compareTo(BigDecimal.ZERO) == 0) {
				rescate.TieneMontoRetencion = _c.NO;
			}
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.esMontoRetencionValido");
	}
	
	private void calcularRentabilidadRealAportes(List<SolicitudesProcesoCertificacion> aportes, SalidaProcesarCustodiaFondoMutuo salidaCustodia) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.calcularRentabilidadRealAportes");
		for(SolicitudesProcesoCertificacion sol : aportes){
			if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso) || _c.TipoSolicitudRescate.equals(sol.TipoSolicitud)) continue;
			SalidaMovimientoCustodiaFondoMutuo carteraSolicitud = salidaCustodia.P_OUT_MOVIMIENTOS.parallelStream().filter(cartera->cartera.IdGenerarMovimientoCustodia.longValue() == sol.IdSolicitudAporteRescate.longValue()).findFirst().orElse(null);
			if(carteraSolicitud != null) {
				sol.IdCartera = carteraSolicitud.IdCustodia;
				sol.RentabilidadReal = carteraSolicitud.MontoCompraFactorOriginal;
			}
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.calcularRentabilidadRealAportes");
	}
	
	private List<SolicitudesProcesoCertificacion> obtenerRescatesConRegimen(List<SolicitudesProcesoCertificacion> rescates){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerRescatesParciales");
		List<SolicitudesProcesoCertificacion> rescateParciales = rescates.parallelStream().filter(rescate->!_c.EstadoRechazado.equals(rescate.EstadoProceso) && !rescate.IdRegimenTributario.isEmpty()).collect(Collectors.toList());
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerRescatesParciales");
		return rescateParciales;
	}
	
	private List<SolicitudesProcesoCertificacion> obtenerRescatesSinRegimen(List<SolicitudesProcesoCertificacion> rescates){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerRescatesTotales");
		List<SolicitudesProcesoCertificacion> rescateTotales = rescates.parallelStream().filter(rescate->!_c.EstadoRechazado.equals(rescate.EstadoProceso) && rescate.IdRegimenTributario.isEmpty()).collect(Collectors.toList());
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerRescatesTotales");
		return rescateTotales;
	}
	
	private List<MensajeSalida> obtenerMensajeSalidaRescateCustodia(SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateParciales, SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateTotales){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerMensajeSalidaRescateCustodia");
		List<MensajeSalida> mensajeSalidaCustodia = new ArrayList<MensajeSalida>();
		mensajeSalidaCustodia.addAll(salidaCustodiaRescateParciales.P_OUT_MENSAJES);
		mensajeSalidaCustodia.addAll(salidaCustodiaRescateTotales.P_OUT_MENSAJES);
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerMensajeSalidaRescateCustodia");
		return mensajeSalidaCustodia;
	}
	
	public List<SalidaMovimientoCustodiaFondoMutuo> obtenerCarterasRetiradas(SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateParciales, SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateTotales){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerCarterasRetiradas");
		List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas = new ArrayList<SalidaMovimientoCustodiaFondoMutuo>();
		carterasRetiradas.addAll(salidaCustodiaRescateParciales.P_OUT_MOVIMIENTOS);
		carterasRetiradas.addAll(salidaCustodiaRescateTotales.P_OUT_MOVIMIENTOS);
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerCarterasRetiradas");
		return carterasRetiradas;
	}
	
	private void validarRetiroCarteras(List<SolicitudesProcesoCertificacion> rescates, List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.validarRetiroCarteras");
		rescates.parallelStream().filter(rescate->!_c.EstadoRechazado.equals(rescate.EstadoProceso)).forEach(rescate->{
			int cantidadCarterasRetiradas = carterasRetiradas.parallelStream().filter(cartera->cartera.IdGenerarMovimientoCustodia.equals(rescate.IdSolicitudAporteRescate)).collect(Collectors.toList()).size();
			if(cantidadCarterasRetiradas == 0) {
				rescate.EstadoProceso = _c.EstadoRechazado;
				rescate.Codigo = "UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO";
			}
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.validarRetiroCarteras");
	}
	
	private void calcularCostoUFRentabilidadRescates(Connection con, List<SolicitudesProcesoCertificacion> rescates, List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.calcularCostoUFRentabilidadRescates");
		
		List<Date> fechaCertificaciones = rescates.parallelStream().map(solicitud->solicitud.FechaCertificacion).distinct().collect(Collectors.toList());
		Map<Long,List<SalidaMovimientoCustodiaFondoMutuo>> movimientosAgrupadosPorSolicitud = carterasRetiradas.parallelStream().collect(Collectors.groupingBy(solicitud->solicitud.IdGenerarMovimientoCustodia));
		HashMap<Date,Long> mapeoIdInternoFecha = new HashMap<Date,Long>();

		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
		
		CalculoEntrada calculoEntradaUTM = new CalculoEntrada();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Long idInterno = 1L;
		
		for(Date fechaCertificacion : fechaCertificaciones) {
			calculoEntradaUTM.IdTrabajo = 1L;
			calculoEntradaUTM.IdInterno = idInterno;
			calculoEntradaUTM.Fecha = dateFormat.format(fechaCertificacion);
			calculoEntradaUTM.IdMonedaOrigen = "UTM";
			calculoEntradaUTM.IdMonedaDestino = "CLP";
			calculoEntradaUTM.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaUTM.MontoOrigen = BigDecimal.ONE;
			calculoEntradaUTM.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUTM);
			mapeoIdInternoFecha.put(fechaCertificacion, idInterno);
			idInterno++;
		}
	
		CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hCalculo = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		
		for(SolicitudesProcesoCertificacion sol : rescates) {
			BigDecimal costoUF = BigDecimal.ZERO;
			BigDecimal rentabilidad = BigDecimal.ZERO;
			
			List<SalidaMovimientoCustodiaFondoMutuo> movimientosCustodia = movimientosAgrupadosPorSolicitud.get(sol.IdSolicitudAporteRescate);
			if(movimientosCustodia != null) {
				for(SalidaMovimientoCustodiaFondoMutuo movimientoCustodia : movimientosCustodia) {
					costoUF = costoUF.add(movimientoCustodia.CostoInversionEnFactorAjusteARetirar, MathContext.DECIMAL128);
					rentabilidad = rentabilidad.add(movimientoCustodia.MontoCompraFactorARetirar, MathContext.DECIMAL128);
				}
			}
	
			Long id = mapeoIdInternoFecha.get(sol.FechaCertificacion);
			CalculoSalida calculoValorUTM = (CalculoSalida) hCalculo.get(id);
			BigDecimal valorUTM = calculoValorUTM !=null && "OK".equals(calculoValorUTM.Resultado) ? calculoValorUTM.MontoDestino : BigDecimal.ONE;
			sol.RentabilidadReal = ((sol.MontoMonedaSistema.divide(valorUTM, MathContext.DECIMAL128)).subtract(rentabilidad, MathContext.DECIMAL128));
			sol.Spread = sol.CostoUF.subtract(costoUF, MathContext.DECIMAL128);
			sol.CostoUF = costoUF;
			
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.calcularCostoUFRentabilidadRescates");
	}
	
	private List<ResultadoCertificacion> procesarSalida(Connection con, EntradaCertificarSolicitudAporteRescate entrada,List<SolicitudesProcesoCertificacion> solicitudes, HashMap<String,Object> hestado) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.procesarSalida");
		List<ResultadoCertificacion> resultadosCertificacion = solicitudes.parallelStream().map(solicitud->{
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdComprobante", String.valueOf(solicitud.IdComprobanteTesoreria));
			params.put("IdSolicitudAporteRescateRelacionado", String.valueOf(solicitud.IdSolicitudAporteRescateRelacionado));
			params.put("IdSolicitudAporteRescate", String.valueOf(solicitud.IdSolicitudAporteRescate));
			params.put("DocumentoIdentidad", solicitud.DocumentoIdentidad);
			params.put("CodigoPortafolio", solicitud.CodigoPortafolio);
			
			Estado estado = (Estado) hestado.get(solicitud.EstadoProceso);
			ResultadoCertificacion resultadoCertificacion = new ResultadoCertificacion();
			resultadoCertificacion.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
			resultadoCertificacion.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado;
			resultadoCertificacion.IdSerieFondo = solicitud.IdSerieFondo;
			resultadoCertificacion.Codigo = solicitud.Codigo;
			resultadoCertificacion.Estado = solicitud.EstadoProceso;
			resultadoCertificacion.NombreEstado = estado.Nombre;
			resultadoCertificacion.DocumentoIdentidad = solicitud.DocumentoIdentidad;
			resultadoCertificacion.CodigoPortafolio = solicitud.CodigoPortafolio;
			resultadoCertificacion.DescripcionCodigo = solicitud.DescripcionCodigo.isEmpty() ? obtenerDescripcionMensajeSalida(con, entrada.p_IdEmpresa, solicitud.Codigo, params) : solicitud.DescripcionCodigo;;
			
			if(_c.SI.equals(entrada.p_CertificacionAporteSwitchSimulado)) {
				resultadoCertificacion.IdSolicitudAporteRescate = 0L;
			}
			
			return resultadoCertificacion;
		}).collect(Collectors.toList());
		
		logger.trace("FIN::CertificarSolicitudAporteRescate.procesarSalida");
		return resultadosCertificacion;
	}
	
	private void insertarLog(Connection con, List<SolicitudesProcesoCertificacion> sol, EntradaCertificarSolicitudAporteRescate entrada, String fechaSistema) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.insertarLog");
		AgregarLogCertificacionAporteRescate agregarLogCertificacionAporteRescate = new AgregarLogCertificacionAporteRescate();
		EntradaAgregarLogCertificacionAporteRescate entradaLog = new EntradaAgregarLogCertificacionAporteRescate();
		for(SolicitudesProcesoCertificacion s : sol){
			InLog l = new InLog();
			l.IdSolicitudAporteRescate = s.IdSolicitudAporteRescate;
			l.Codigo = s.Codigo;
			l.IdEstado = s.EstadoProceso;
			l.TipoSolicitud = s.TipoSolicitud;
			l.IdTipoMovimientoFondoMutuo = s.IdTipoMovimientoFondoMutuo;
			l.IdUsuario = entrada.p_IdUsuario;
			l.IdEmpresa = entrada.p_IdEmpresa;
			l.FechaSistema = fechaSistema;
			l.IdSerieFondo = s.IdSerieFondo;
			l.IdEmisor = s.IdEmisor;
			l.Monto = s.Monto;
			l.IdMoneda = s.IdMonedaFondo;
			l.MontoOriginal = s.CostoMonedaLocal;
			l.TipoCambio =s.ParidadFechaCertificacion ;
			l.IdMonedaOriginal = s.IdMoneda;
			l.ValorCuota = s.ValorCuota!=null?s.ValorCuota:BigDecimal.ZERO;
			l.NumeroCuotas = s.NumeroCuotas!=null?s.NumeroCuotas:BigDecimal.ZERO;
			l.IdCliente = s.IdCliente;
			l.IdPortafolio = s.IdPortafolio;
			l.Simulacion = entrada.p_SimularOperacion;
			entradaLog.p_Log.add(l);
		}
		agregarLogCertificacionAporteRescate.logica(con, entradaLog);
		logger.trace("FIN::CertificarSolicitudAporteRescate.insertarLog");
	}
	
	private HashMap<Long, Object> calcularComisiones(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.calcularComisiones");
		HashMap<Long,Object> hcomisiones = new HashMap<Long,Object>();
		CalcularComisiones calculoComisiones = new CalcularComisiones();
		EntradaCalcularComisiones entradaCalcularComisiones = new EntradaCalcularComisiones();
		entradaCalcularComisiones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCalcularComisiones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCalcularComisiones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCalcularComisiones.p_IdModulo = entrada.p_IdModulo;
		entradaCalcularComisiones.p_SimularOperacion = _c.NO;
		entradaCalcularComisiones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCalcularComisiones.p_DatosCustodia = solicitudes.parallelStream()
				.filter(solicitud->!_c.EstadoRechazado.equals(solicitud.EstadoProceso))
				.map(solicitud->{
					CalcularComisiones.InCustodia inCustodia =  new CalcularComisiones.InCustodia();
					inCustodia.IdInterno = solicitud.IdSolicitudAporteRescate;
					inCustodia.IdSerieFondo = solicitud.IdSerieFondo;
					inCustodia.IdInstrumento = solicitud.IdInstrumento;
					inCustodia.IdCliente = solicitud.IdCliente;
					inCustodia.IdPortafolio = solicitud.IdPortafolio;
					inCustodia.Cantidad = solicitud.NumeroCuotas;
					inCustodia.IdMonedaFondo = solicitud.IdMonedaFondo;
					inCustodia.IdMonedaOperacion = solicitud.IdMoneda;
					inCustodia.TipoSolicitud = solicitud.TipoSolicitud;
					inCustodia.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
					inCustodia.FechaValorCuota = Fecha.toStringDate(solicitud.FechaValorCuota);
					inCustodia.FechaCurse = Fecha.toStringDate(solicitud.FechaAceptacion);
					inCustodia.EsTraspaso = solicitud.RelacionTraspaso;
					return inCustodia;
				}).collect(Collectors.toList());
		
		if(entradaCalcularComisiones.p_DatosCustodia.size() > 0){
			SalidaCalcularComisiones salidaCalculoComisiones = calculoComisiones.logica(con, entradaCalcularComisiones);
			hcomisiones = Utils.List2Hash(salidaCalculoComisiones.P_OUT_RESULTADO, "IdInterno");
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.calcularComisiones");
		return hcomisiones;
	}
	
	private SalidaProcesarCustodiaFondoMutuo ingresoRetiroCustodia(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.ingresoRetiroCustodia");
		ProcesarCustodiaFondoMutuo procesarCustodia = new ProcesarCustodiaFondoMutuo();
		EntradaProcesarCustodiaFondoMutuo entradaCustodia = new EntradaProcesarCustodiaFondoMutuo();
		SalidaProcesarCustodiaFondoMutuo salidaCustodia = new SalidaProcesarCustodiaFondoMutuo();
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			MovimientoCustodiaFondoMutuo movCustodia = new MovimientoCustodiaFondoMutuo();
			movCustodia.IdGenerarMovimientoCustodia = sol.IdSolicitudAporteRescate;
			movCustodia.FechaOperacion = sol.FechaCertificacion;
			movCustodia.IdCliente = sol.IdCliente;
			movCustodia.IdPortafolio = sol.IdPortafolio;
			movCustodia.IdTipoProducto = _c.TipoOperacion;
			movCustodia.IdConcepto = sol.IdConcepto ;
			movCustodia.IdTipoOperacion = _c.TipoSolicitudAporte.equals(sol.TipoSolicitud) ? _c.TipoOperacionAporteFondoMutuo :  _c.TipoOperacionRescateFondoMutuo;
			movCustodia.IdOperacion = sol.IdSolicitudAporteRescate;
			movCustodia.IdTipoComprobante = _c.TipoComprobanteCustodia;
			movCustodia.FolioComprobante = sol.IdSolicitudAporteRescate;
			movCustodia.FechaComprobante = sol.FechaCertificacion;
			movCustodia.IdBolsa = _c.BCS;
			movCustodia.IdAgente = sol.IdEmpleadoEjecutivo;
			movCustodia.FechaLiquidacion = sol.FechaCertificacion;
			movCustodia.IdUsuario = entrada.p_IdUsuario;
			movCustodia.TipoCarteraNormativa = sol.TipoCarteraNormativa;
			movCustodia.FechaCurse = sol.FechaAceptacion;
			movCustodia.TipoFactorAjuste = _c.UF;
			movCustodia.ValorFactorAjusteaFechaCurse = sol.ValorUFFechaInversion;
			movCustodia.CostoEnFactorAjuste = sol.CostoUF;
			movCustodia.IdInstrumento = sol.IdInstrumento;
			movCustodia.IdEmisor = sol.IdEmisor;
			movCustodia.IdMonedaInstrumento = sol.IdMonedaFondo;
			movCustodia.IdBoveda = _c.COR;
			movCustodia.Cantidad = sol.NumeroCuotas;
			movCustodia.ValorRescate = BigDecimal.ZERO;
			movCustodia.PrecioCompra = sol.ValorCuota;
			movCustodia.MontoCompra = sol.MontoMonedaSistema;
			movCustodia.MontoCompraMonedaInstrumento = sol.Monto;
			movCustodia.IdTipoCanalIngreso = "";
			movCustodia.IdRegimenTributario = sol.IdRegimenTributario;
			movCustodia.FechaOrigenCompra = _c.AporteReinversion.equals(sol.IdTipoMovimientoFondoMutuo) && !"0001-01-01".equals(Fecha.stringToSqlDate(sol.FechaOriginalInversion)) ? sol.FechaOriginalInversion : sol.FechaValorCuota;
			entradaCustodia.p_Movimientos.add(movCustodia);
		}
		
		if(entradaCustodia.p_Movimientos.size() > 0){
			entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaCustodia.p_simularOperacion = _c.NO;
			entradaCustodia. p_UsaReglasNegocio = _c.SI;
			logger.debug("Se envia a custodia",entradaCustodia);
			salidaCustodia = procesarCustodia.logica(con, entradaCustodia);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.ingresoRetiroCustodia");
		return salidaCustodia;
		
	}

	private CrearCargoAbonoSalida mueveCuentaInversion(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada, String fechaSistema, Long porcentajeRetencion) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.mueveCuentaInversion");
		CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
		salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearCargoAbono.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
		Long i = 0L;
		HashMap<Long,Long> ids = new HashMap<Long,Long>();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date fechaSistemaDate = format.parse(fechaSistema);
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			//APORTES
			if(_c.TipoSolicitudAporte.equals(sol.TipoSolicitud)){
				if(_c.SI.equals(sol.MueveCta) && _c.OrigenManual.equals(sol.OrigenOperacion)
				   && ( _c.SI.equals( tipoMovimientoFondoMutuoGeneraCargoAbono(con, sol.IdTipoMovimientoFondoMutuo) ) ) ){
					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					//CARGO EN MONEDA DEL FONDO NORMALES Y DIRERIDOS CUENTA INVERSION('APONORM','APOREINV','APOOVER','APORESPRO','APODIV')
					obj.NumeroCargoAbono = i;
					obj.IdPortafolio = sol.IdPortafolio;
					obj.IdMoneda = sol.IdMonedaFondo;
					obj.IdTipoMovimiento = _c.CargoCuentaInversion;
					obj.Monto = sol.MontoPago;
					obj.IdMonedaOperacion = sol.IdMonedaFondo;
					obj.MontoOperacion = sol.MontoPago;
					obj.IdConcepto = sol.IdConcepto;
					obj.Descripcion = "APORTE_FONDO_MUTUO";
					obj.IdCondicionDeLiquidacion = "";
					
					obj.FechaCondicionDeLiquidacion = (fechaSistemaDate.compareTo(sol.FechaLiquidacion) > 0 || sol.Switch.equalsIgnoreCase("S")) ? fechaSistema : Fecha.stringToSqlDate(sol.FechaLiquidacion);
					obj.IdEjecutivo = sol.IdEmpleadoEjecutivo;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = _c.TipoProducto;
					obj.IdTipoOperacion = _c.TipoOperacionAporteFondoMutuo;
					obj.IdOperacion = sol.IdSolicitudAporteRescate;
					obj.FechaOperacion = Fecha.stringToSqlDate(sol.FechaCertificacion);
					obj.FechaLiquidacionOperacion = (fechaSistemaDate.compareTo(sol.FechaLiquidacion) > 0) ? fechaSistema : Fecha.stringToSqlDate(sol.FechaLiquidacion);
					obj.TipoCambioOperacion = sol.ParidadFechaCertificacion;
					obj.IdTipoComprobante = _c.TipoComprobanteCuentaInversion;
					obj.IdComprobante = sol.IdSolicitudAporteRescate;
					obj.FolioComprobante = sol.IdSolicitudAporteRescate.toString();
					obj.IdComprobanteDetalle = sol.IdSolicitudAporteRescate;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;
					obj.FechaComprobante = Fecha.stringToSqlDate(sol.FechaCertificacion);
					obj.IdInstrumento = sol.IdInstrumento;
					obj.IdTipoInstrumento = sol.IdTipoInstrumento;
					obj.Nemo = sol.IdSerieFondo;
					obj.EsCargoAbonoPorTraspaso = _c.NO;
					obj.GeneroContraparteEnTesoreria = _c.NO;
					obj.CertificadoAutomatico = _c.SI;
					ids.put(i, sol.IdSolicitudAporteRescate);
					i++;
					//CARGO EN MONEDA DE INVERSION PARA DIFERIDOS CUENTA INVERSION('APONORM', 'APOREINV','APOOVER','APORESPRO','APODIV')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = getPojoCardif(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMoneda;
						cardif.IdTipoMovimiento = _c.CargoCuentaInversion;
						cardif.Monto = sol.MontoPagoLocal;
						cardif.IdMonedaOperacion = sol.IdMoneda;
						cardif.MontoOperacion = sol.MontoPagoLocal;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					//ABONO EN MONEDA DE FONDO PARA DIFERIDOS CUENTA INVERSION('APONORM', 'APOREINV','APOOVER','APORESPRO','APODIV')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = getPojoCardif(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMonedaFondo;
						cardif.IdTipoMovimiento = _c.AbonoCuentaInversion;
						cardif.Monto = sol.MontoPago;
						cardif.IdMonedaOperacion = sol.IdMonedaFondo;
						cardif.MontoOperacion = sol.MontoPago;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);
				}
			}
			//RESCATES
			if(_c.TipoSolicitudRescate.equals(sol.TipoSolicitud)){
				if( _c.SI.equals( tipoMovimientoFondoMutuoGeneraCargoAbono(con, sol.IdTipoMovimientoFondoMutuo) ) ) {
					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					//ABONO EN MONEDA FONDO NORMALES Y DIFERIDO CUENTA INVERSION('RESNORM', 'RESREINV', 'RESCOM', RESCNN)
					obj.NumeroCargoAbono = i;
					obj.IdPortafolio = sol.IdPortafolio;
					obj.IdMoneda = sol.IdMonedaFondo;
					obj.IdTipoMovimiento = _c.AbonoCuentaInversion;
					obj.Monto = sol.Monto;
					obj.IdMonedaOperacion = sol.IdMonedaFondo;
					obj.MontoOperacion = sol.MontoPago;
					obj.IdConcepto = sol.IdConcepto;
					obj.Descripcion = "RESCATE FONDO MUTUO";
					obj.IdCondicionDeLiquidacion = "";
					
					obj.FechaCondicionDeLiquidacion = (fechaSistemaDate.compareTo(sol.FechaLiquidacion) > 0 || sol.Switch.equalsIgnoreCase("S")) ? fechaSistema : Fecha.stringToSqlDate(sol.FechaLiquidacion);
					obj.IdEjecutivo = sol.IdEmpleadoEjecutivo;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = _c.TipoProducto;
					obj.IdTipoOperacion = _c.TipoOperacionRescateFondoMutuo;
					obj.IdOperacion = sol.IdSolicitudAporteRescate;
					obj.FechaOperacion = Fecha.stringToSqlDate(sol.FechaAceptacion);
					obj.FechaLiquidacionOperacion = (fechaSistemaDate.compareTo(sol.FechaLiquidacion) > 0) ? fechaSistema : Fecha.stringToSqlDate(sol.FechaLiquidacion);
					obj.TipoCambioOperacion = sol.ParidadFechaCertificacion;
					obj.IdTipoComprobante = _c.TipoComprobanteCuentaInversion;
					obj.IdComprobante = sol.IdSolicitudAporteRescate;
					obj.FolioComprobante = sol.IdSolicitudAporteRescate.toString();
					obj.IdComprobanteDetalle = sol.IdSolicitudAporteRescate;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;
					obj.FechaComprobante = Fecha.stringToSqlDate(sol.FechaCertificacion);
					obj.IdInstrumento = sol.IdInstrumento;
					obj.IdTipoInstrumento = sol.IdTipoInstrumento;
					obj.Nemo = sol.IdSerieFondo;
					obj.EsCargoAbonoPorTraspaso = _c.NO;
					obj.GeneroContraparteEnTesoreria = _c.NO;
					obj.CertificadoAutomatico = _c.SI;
					ids.put(i, sol.IdSolicitudAporteRescate);
					i++;
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);
					//CARGO EN MONEDA DEL FONDO PARA DIFERIDOS CUENTA INVERSION('RESNORM','RESREINV', 'RESCOM')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = getPojoCardif(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMonedaFondo;
						cardif.IdTipoMovimiento = _c.CargoCuentaInversion;
						cardif.Monto = sol.MontoPago;
						cardif.IdMonedaOperacion = sol.IdMonedaFondo;
						cardif.MontoOperacion = sol.MontoPago;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					//ABONO EN MONEDA DE INVERSION PARA DIFERIDOS CUENTA INVERSION ('RESNORM','RESREINV','RESCOM')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = getPojoCardif(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMoneda;
						cardif.IdTipoMovimiento = _c.AbonoCuentaInversion;
						cardif.Monto = sol.MontoPagoLocal;
						cardif.IdMonedaOperacion = sol.IdMoneda;
						cardif.MontoOperacion = sol.MontoPagoLocal;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					//COMISION CON MONEDA DE OPERACION CUENTA INVERSION ('RESNORM','RESREINV')
					if((_c.Reinversion.equals(sol.IdTipoMovimientoFondoMutuo) || _c.RescateComision.equals(sol.IdTipoMovimientoFondoMutuo))
					   && sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = getPojoCardif(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMoneda;
						cardif.IdTipoMovimiento = _c.CargoCuentaInversion;
						cardif.Monto = sol.MontoComisionLocal;
						cardif.IdMonedaOperacion = sol.IdMoneda;
						cardif.MontoOperacion = sol.MontoComisionLocal;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
				}
			}
			
		}
		
		if(entradaCrearCargoAbono.p_CargoAbonoAGenerar.size() > 0){
			entradaCrearCargoAbono.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo = entrada.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion = _c.NO;
			entradaCrearCargoAbono.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual	= "N";
			salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);
			
			//Cambiamos el id interno por el id de la solicitud
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = ids.get(mje.id);
					mje.id = id;
				}
			}
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.mueveCuentaInversion");
		return salidaCrearCargoAbono;
	}
	

	public String tipoMovimientoFondoMutuoGeneraCargoAbono(Connection con, String idTipoMovimientoFondoMutuo) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.tipoMovimientoFondoMutuoGeneraCargoAbono");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(1);
		params.put("IdTipoMovimientoFondoMutuo", idTipoMovimientoFondoMutuo);
		
		String generaCargoAbono = sql.query(con, SQL_SELECT_TIPO_MOVIMIENTO_FONDO_MUTUO_GENERA_CARGO_ABONO, params, GeneraCargoAbono.class).stream().findFirst().orElse(new GeneraCargoAbono()).GeneraCargoAbono;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CertificarSolicitudAporteRescate.tipoMovimientoFondoMutuoGeneraCargoAbono", fin - inicio);
		return generaCargoAbono;
	}

	private SalidaProcesarIngresoEgresoTesoreria mueveTesoreria(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada, String fechaSistema, Long porcentajeRetencion, Map<Long,Traspaso> hashTraspasos, Persona personaTGR, Persona personaSII, Long idEmpresa) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.mueveTesoreria");
		SalidaProcesarIngresoEgresoTesoreria salidaTesoreria = new SalidaProcesarIngresoEgresoTesoreria();
		ProcesarIngresoEgresoTesoreria procesarTesoreria = new ProcesarIngresoEgresoTesoreria();
		EntradaProcesarIngresoEgresoTesoreria entradaprocesarTesoreria = new EntradaProcesarIngresoEgresoTesoreria();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date fechaSistemaDate = format.parse(fechaSistema);
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(!_c.EstadoRechazado.equals(sol.EstadoProceso) && !_c.SI.equals(sol.GeneraSpot)) {
				if(!_c.CuentaInversion.equals(sol.IdMedioDePago) && !_c.Reinversion.equals(sol.IdTipoMovimientoFondoMutuo)
						   && _c.NO.equals(sol.IndicadorMLT) && !_c.SI.equals(sol.RelacionTraspaso)){
					SolicitudAporteRescate s = new SolicitudAporteRescate();
					s = getPojoSolicitudAporteRescate(sol, s);
					s.IdInterno = sol.IdSolicitudAporteRescate;
					s.FechaEmision = (fechaSistemaDate.compareTo(sol.FechaLiquidacion) > 1) ? (Date) fechaSistemaDate : sol.FechaLiquidacion;
					s.ParidadFechaInversion = sol.ParidadFechaCertificacion;
					s.RelacionTraspaso=_c.NO;
									
					//SI ES UN RESCATE CON REGIMEN APV-B SE CALCULA RETENCION (NO APLICA PARA TRASPASOS)
					if(_c.SI.equals(sol.TieneMontoRetencion))
						s.MontoPagoLocal = s.MontoPagoLocal.subtract(sol.MontoRetencion);
					
					if(_c.BonificacionFiscal.equals(sol.IdRegimenTributarioAPV)) {
						s.ParidadFechaInversion = sol.ParidadFechaCertificacion;
						s.IdPersonaTraspaso = personaTGR.IdPersona;
						s.IdCuentaBancaria = personaTGR.IdCuentaBancaria;
						s.IdBanco = personaTGR.IdBanco;
						s.IdTipoCuentaBancaria = personaTGR.IdTipoCuentaBancaria;
						s.NumeroCuentaBancaria = personaTGR.NumeroCuentaBancaria;
						s.NombreTipoCuentaBancaria = personaTGR.NombreTipoCuentaBancaria;
						s.RelacionTraspaso =  _c.SI;
						s.IdMedioDePago = _c.MedioPagoTransferencia;
					}
					
					entradaprocesarTesoreria.p_Solicitudes.add(s);
					
					
					
					//SI ES UN RESCATE CON REGIMEN APV-B o APV-AN SE ENVIA A TESORERIA LA RETENCION COMO PAGO AL SII
					if (_c.APV_B.equals(sol.IdRegimenTributarioAPV) || _c.APV_AN.equals(sol.IdRegimenTributarioAPV))
					{				
						
					    
						String idConceptoRetencionPagoSII = obtenerConceptoRetencionPagoSII(con, idEmpresa);
						
						
						SolicitudAporteRescate so = new SolicitudAporteRescate();
						so = getPojoSolicitudAporteRescate(sol, so);
						so.IdInterno = sol.IdSolicitudAporteRescate * -1;
						so.FechaEmision = (fechaSistemaDate.compareTo(sol.FechaLiquidacion) > 1) ? (Date) fechaSistemaDate : sol.FechaLiquidacion;
						so.ParidadFechaInversion = sol.ParidadFechaCertificacion;
						so.MontoPagoLocal = sol.MontoRetencion;
						so.IdPersonaTraspaso = personaSII.IdPersona;
						so.IdCuentaBancaria = personaSII.IdCuentaBancaria;
						so.IdBanco = personaSII.IdBanco;
						so.IdTipoCuentaBancaria = personaSII.IdTipoCuentaBancaria;
						so.NumeroCuentaBancaria = personaSII.NumeroCuentaBancaria;
						so.NombreTipoCuentaBancaria = personaSII.NombreTipoCuentaBancaria;
						so.RelacionTraspaso =  _c.SI;
						so.IdMedioDePago = _c.MedioPagoTransferencia;
						
						so.IdConcepto = idConceptoRetencionPagoSII;
						   
						
						entradaprocesarTesoreria.p_Solicitudes.add(so);
					}
				
				}else if(_c.SI.equals(sol.RelacionTraspaso) ) {
					Traspaso traspaso = obtenerTraspasoSolicitud(sol,hashTraspasos);
					if(traspaso != null) {
						SolicitudAporteRescate s = new SolicitudAporteRescate();
						//s = Utils.copyPojo(sol, s);
						s = getPojoSolicitudAporteRescate(sol, s);
						s.IdInterno = sol.IdSolicitudAporteRescate;
						s.FechaEmision = (fechaSistemaDate.compareTo(sol.FechaLiquidacion) > 1) ? (Date) fechaSistemaDate : sol.FechaLiquidacion;
						s.ParidadFechaInversion = sol.ParidadFechaCertificacion;
						s.RelacionTraspaso=_c.SI;
						s.IdPersonaTraspaso = traspaso.IdPersona;
						s.IdCuentaBancaria = traspaso.IdCuentaBancaria;
						s.IdBanco = traspaso.IdBanco;
						s.IdTipoCuentaBancaria = traspaso.IdTipoCuentaBancaria;
						s.NumeroCuentaBancaria = traspaso.NumeroCuentaBancaria;
						s.NombreTipoCuentaBancaria = traspaso.NombreTipoCuentaBancaria;
						s.IdMedioDePago = _c.MedioPagoTransferencia;
						entradaprocesarTesoreria.p_Solicitudes.add(s);
					}
					
				}
			}
		}
		if(entradaprocesarTesoreria.p_Solicitudes.size() > 0){
			entradaprocesarTesoreria.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaprocesarTesoreria.p_IdUsuario = entrada.p_IdUsuario;
			entradaprocesarTesoreria.p_IdModulo = entrada.p_IdModulo;
			entradaprocesarTesoreria.p_SimularOperacion = _c.NO;
			entradaprocesarTesoreria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;		
			entradaprocesarTesoreria.p_EsIngresoAutomatico = _c.NO;
			salidaTesoreria = procesarTesoreria.logica(con, entradaprocesarTesoreria);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.mueveTesoreria");
		return salidaTesoreria; 
	}
	
	
	private SalidaProcesarIngresoOperacionSpot mueveSpot(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.mueveSpot");
		SalidaProcesarIngresoOperacionSpot salidaSpot = new SalidaProcesarIngresoOperacionSpot();
		ProcesarIngresoOperacionSpot procesarSpot = new ProcesarIngresoOperacionSpot();
		EntradaProcesarIngresoOperacionSpot entradaprocesarSpot = new EntradaProcesarIngresoOperacionSpot();
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(!_c.EstadoRechazado.equals(sol.EstadoProceso) && _c.SI.equals(sol.GeneraSpot)) {
				SolicitudAporteRescate s = new SolicitudAporteRescate();
				s = getPojoSolicitudAporteRescate(sol, s);
				s.IdInterno = sol.IdSolicitudAporteRescate;
				entradaprocesarSpot.p_DatosOperacion.add(s);
			}
		}
		if(entradaprocesarSpot.p_DatosOperacion.size() > 0){
			entradaprocesarSpot.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaprocesarSpot.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaprocesarSpot.p_IdUsuario = entrada.p_IdUsuario;
			entradaprocesarSpot.p_IdModulo = entrada.p_IdModulo;
			entradaprocesarSpot.p_CalculaMargen = _c.SpotCalculaMargen;
			entradaprocesarSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			//Formas de pago por defeto al rescate
			for(SolicitudesProcesoCertificacion sol : solicitudes){
				if(!_c.EstadoRechazado.equals(sol.EstadoProceso) && _c.SI.equals(sol.GeneraSpot)) {
					String idTipoOperacionSpot = "";
					if(_c.AporteReinversion.equals(sol.IdTipoMovimientoFondoMutuo)) {
						idTipoOperacionSpot = sol.IdMoneda.equals(_c.Dolar) ? _c.SpotVenta : _c.SpotCompra;
					} else {
						idTipoOperacionSpot = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? _c.SpotVenta : _c.SpotCompra;
					}
					
					//	Recibimos
					FormaPagoSpot pagRec = new FormaPagoSpot();
					pagRec.IdInternoPagoOperacion=1L;
					pagRec.IdComprobantePagoOperacion=0L;
					pagRec.IdOperacion=sol.IdSolicitudAporteRescate;
					pagRec.RecibePagaPortafolio="R";
					if (_c.SpotCompra.equals(idTipoOperacionSpot)) {
						pagRec.IdTipoMovimiento = _c.SpotTipoMovimientoEgreso;
						pagRec.IdMoneda=sol.IdMoneda;
						pagRec.IdMonedaLiquidacion=sol.IdMoneda;
						pagRec.MontoFormaDePago=sol.Monto;
						pagRec.AfectarTesoreria=  _c.RescateComisionNN.equals(sol.IdTipoMovimientoFondoMutuo) ? _c.SI : _c.NO;
						if(_c.TipoComisionTrimestral.equalsIgnoreCase(sol.TipoComision)) {
							pagRec.IdFormaDePago = _c.SpotCompra+"_"+_c.MedioPagoTransferencia+"_"+sol.IdMoneda+"_"+_c.SpotValutaDefectoFormaDePago;
						}
						else {
							pagRec.IdFormaDePago = _c.SpotCompra+"_"+_c.CuentaInversion+"_"+sol.IdMoneda+"_"+_c.SpotValutaDefectoFormaDePago;
						}
					}
					else {
						String idMedioDePago = "".equals(sol.IdMedioDePago) || sol.IdMedioDePago == null ? _c.CuentaInversion : sol.IdMedioDePago;
						pagRec.IdTipoMovimiento = _c.SpotTipoMovimientoIngreso;
						pagRec.IdFormaDePago = _c.SpotCompra+"_"+idMedioDePago+"_"+sol.IdMonedaSistema+"_"+_c.SpotValutaDefectoFormaDePago;
						pagRec.IdMoneda=sol.IdMonedaSistema;
						pagRec.IdMonedaLiquidacion=sol.IdMonedaSistema;
						pagRec.MontoFormaDePago=sol.MontoMonedaSistema;
						pagRec.AfectarTesoreria= _c.CuentaInversion.equals(idMedioDePago) ? _c.NO : _c.SI;
					}
					if(_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud))
						pagRec.FechaLiquidacion= sol.FechaAceptacion;
					else
						pagRec.FechaLiquidacion= sol.FechaLiquidacion;
					entradaprocesarSpot.p_DatosFormaPago.add(pagRec);
					
					// Entregamos
					FormaPagoSpot pagEnt = new FormaPagoSpot();
					pagEnt.IdInternoPagoOperacion=1L;
					pagEnt.IdComprobantePagoOperacion=0L;
					pagEnt.IdOperacion=sol.IdSolicitudAporteRescate;
					pagEnt.RecibePagaPortafolio="P";
					if (_c.SpotCompra.equals(idTipoOperacionSpot)) {
						String idMedioDePago = "".equals(sol.IdMedioDePago) || sol.IdMedioDePago == null ? _c.CuentaInversion : sol.IdMedioDePago;
						pagEnt.IdTipoMovimiento = _c.SpotTipoMovimientoIngreso;
						pagEnt.IdFormaDePago = _c.SpotVenta+"_"+idMedioDePago+"_"+sol.IdMonedaSistema+"_"+_c.SpotValutaDefectoFormaDePago;
						pagEnt.IdMoneda=sol.IdMonedaSistema;
						pagEnt.IdMonedaLiquidacion=sol.IdMonedaSistema;
						pagEnt.MontoFormaDePago=sol.MontoMonedaSistema;
						pagEnt.AfectarTesoreria= _c.CuentaInversion.equals(idMedioDePago) ? _c.NO : _c.SI;
					}
					else {
						pagEnt.IdTipoMovimiento = _c.SpotTipoMovimientoIngreso;
						pagEnt.IdFormaDePago = _c.SpotVenta+"_"+_c.CuentaInversion+"_"+sol.IdMoneda+"_"+_c.SpotValutaDefectoFormaDePago;
						pagEnt.IdMoneda=sol.IdMoneda;
						pagEnt.IdMonedaLiquidacion=sol.IdMoneda;
						pagEnt.MontoFormaDePago=sol.Monto;
						pagEnt.AfectarTesoreria= _c.NO;
					}

					if(_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud))
						pagEnt.FechaLiquidacion= sol.FechaAceptacion;
					else
						pagEnt.FechaLiquidacion= sol.FechaLiquidacion;
					entradaprocesarSpot.p_DatosFormaPago.add(pagEnt);
				}
			};
			
			salidaSpot = procesarSpot.logica(con, entradaprocesarSpot);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.mueveSpot");
		return salidaSpot; 
	}
	
	private CrearMovimientoComprobante_Salida creaMovimientoFacturacion(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada, String tipoComprobanteFacturaNoApv, String tipoComprobanteFacturaApv) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.creaMovimientoFacturacion");
		SQL sql=new SQL();
		
		CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = new CrearMovimientoComprobante_Salida();
		salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearMovimientoComprobante.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		
				
		//Obtiene valor Iva
		List<Cobro> cobros = sql.query(con, SQL_SELECT_COBRO_IVA, new HashMap<>(), Cobro.class);
		BigDecimal IVA = new BigDecimal(cobros.get(0).ValorCobro.toString()).divide(BigDecimal.valueOf(100));
		BigDecimal CalculaImporteIva = IVA.add(new BigDecimal(1));
		
		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if("N".equalsIgnoreCase(sol.GeneraSpot) && !_c.EstadoRechazado.equals(sol.EstadoProceso) && ((_c.RescateComision.equals(sol.IdTipoMovimientoFondoMutuo) || _c.RescateFondoMutuo.equals(sol.IdTipoMovimientoFondoMutuo) || _c.TraspasoPortafolio.equals(sol.IdTipoMovimientoFondoMutuo)) && sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1 && !_c.SI.equals(sol.RelacionTraspaso))){
				CrearMovimientoComprobante.MovimientoComprobante_Entrada mov = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
				mov.NumeroMvtoCmbte = sol.IdSolicitudAporteRescate;
				mov.IdEmpresa = entrada.p_IdEmpresa;
				mov.IdCliente = sol.IdCliente;
				mov.IdPortafolio = sol.IdPortafolio;
				mov.IdInstrumento = sol.IdInstrumento;
				mov.IdInstrumentoRelacionado = 0L;
				mov.IdMoneda = sol.IdMoneda;
				mov.IdMonedaLiquidacion = sol.IdMoneda;
				mov.IdBoveda = _c.COR;
				mov.IdBolsa = _c.BCS;
				mov.IdEmisor = sol.IdEmisor;
				mov.Cantidad = new BigDecimal(1);
				mov.Precio = new BigDecimal(0).equals(sol.MontoComisionLocal)? sol.MontoComisionLocal: (sol.MontoComisionLocal.divide(CalculaImporteIva, MathContext.DECIMAL128));
				
				mov.FechaLiquidacion = Fecha.stringToSqlDate(sol.FechaLiquidacion);
				mov.FechaOrden = Fecha.stringToSqlDate(sol.FechaCertificacion);
				mov.FechaTransaccion = Fecha.stringToSqlDate(sol.FechaCertificacion);
				mov.FechaMovimientoComprobante = Fecha.stringToSqlDate(sol.FechaCertificacion);
				mov.FechaEmision = Fecha.stringToSqlDate(sol.FechaCertificacion);
				mov.IdUsuario = entrada.p_IdUsuario;
				mov.IdTipoProducto = _c.TipoProducto;
				
				if (_c.RescateFondoMutuo.equals(sol.IdTipoMovimientoFondoMutuo) || _c.TraspasoPortafolio.equals(sol.IdTipoMovimientoFondoMutuo)) {
					mov.IdTipoComprobante = "FOE";
					mov.IdConcepto = _c.APV.equals(sol.PropositoPortafolio) ? _c.COSAPV : _c.COSAFM;
					mov.ActualizarCuentaInversion= _c.SI;
					//mov.Monto = sol.MontoComisionLocal;					
					mov.Monto = new BigDecimal(0).equals(sol.MontoComisionLocal)? sol.MontoComisionLocal:  (sol.MontoComisionLocal.divide(CalculaImporteIva, MathContext.DECIMAL128));
					mov.MontoMovimientoComprobante =  new BigDecimal(0).equals(sol.MontoComisionLocal)? sol.MontoComisionLocal:  (sol.MontoComisionLocal.divide(CalculaImporteIva, MathContext.DECIMAL128));
					mov.MontoLiquidacion =  new BigDecimal(0).equals(sol.MontoComisionLocal)? sol.MontoComisionLocal:  (sol.MontoComisionLocal.divide(CalculaImporteIva, MathContext.DECIMAL128));
				} else {
					mov.IdTipoComprobante = _c.APV.equals(sol.PropositoPortafolio) ? tipoComprobanteFacturaApv : tipoComprobanteFacturaNoApv;
					mov.IdConcepto = _c.APV.equals(sol.PropositoPortafolio) ? _c.COCAPV : _c.COAFFM;
					mov.ActualizarCuentaInversion= _c.NO;
					mov.Monto = sol.CostoMonedaLocal;
					mov.MontoMovimientoComprobante =  sol.CostoMonedaLocal;
					mov.MontoLiquidacion =  sol.CostoMonedaLocal;
				}
				mov.IdTipoOperacion = _c.TipoOperacionRescateFondoMutuo;
				mov.IdCondicionLiquidacion = _c.T0;
				mov.IdTipoReajuste = "";
				mov.IdTipoConvenio = _c.SCV;
				mov.IdFormaCustodia = _c.CUS;
				mov.IdTipoCanalIngreso = _c.CanalAgente;
				mov.EsComprobIndividual= _c.NO;
				mov.EsCuentaPropia= _c.NO;
				mov.EsVentaCorta= _c.NO;
				mov.EsPrecioPrima= _c.NO;
				mov.EsEnRueda= _c.NO;
				mov.EsRuteo= _c.NO;
				mov.EsAPlazo= _c.NO;
				mov.EsFungible= _c.NO;
				mov.ActualizarCustodia= _c.NO;
				mov.ActualizarTesoreria= _c.NO;
				mov.ActualizarCustodiaRelacionado = _c.NO;
				mov.CrearComprobanteAutomatico = _c.NO;
				mov.SecuenciaAgrupacion = "";
				mov.ModPrefFact = "MODPREFNORM";
				mov.IdEmpleadoDigitador = 0L;
				mov.IdInstrumentoSubyacente = 0L;
				mov.IdMonedaSubyacente = "";
				mov.IdCorredoraContraparte = 0L;
				mov.IdOrden = 0L;
				mov.IdAsignacion = 0L;
				mov.IdAsignacionRef = 0L;
				mov.IdTransaccion = 0L;
				mov.FolioTransaccion = "";
				mov.FechaVencimiento = "0001-01-01";
				mov.PrecioAPlazo = BigDecimal.ZERO;
				mov.MontoAPlazo = BigDecimal.ZERO;
				mov.FechaParidad = "0001-01-01";
				mov.ValorParidad = BigDecimal.ZERO;
				mov.FechaTipoCambio = "0001-01-01";
				mov.ValorTipoCambio = BigDecimal.ZERO;
				mov.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
				mov.Tasa = BigDecimal.ZERO;
				mov.MontoDescuento = BigDecimal.ZERO;
				mov.MontoInteres = BigDecimal.ZERO;
				mov.Glosa = "";
				mov.ValorRescate = BigDecimal.ZERO;
				mov.TasaAgente = BigDecimal.ZERO;
				mov.Spread = BigDecimal.ZERO;
				mov.DiasPlazo = BigDecimal.ZERO;
				mov.FolioCartera = 0L;
				mov.TasaEmision = BigDecimal.ZERO;
				entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
				
				//Cobros
				if (mov.Monto.compareTo(BigDecimal.ZERO) == 1) {
					CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada movcob = new CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada();
					movcob.NumeroMvtoCmbte = sol.IdSolicitudAporteRescate;
					movcob.NumeroMvtoCmbteCobro = sol.IdSolicitudAporteRescate;
					movcob.IdCobro = cobros.get(0).IdCobro;
					movcob.IdMonedaCobro = sol.IdMoneda;
					movcob.IdMonedaLiquidacionCobro = sol.IdMoneda;
					movcob.MontoCobro = new BigDecimal(0).equals(sol.MontoComisionLocal)? sol.MontoComisionLocal: (sol.MontoComisionLocal.divide(CalculaImporteIva, MathContext.DECIMAL128)).multiply(IVA);
					movcob.MontoMovimientoComprobanteCobro = new BigDecimal(0).equals(sol.MontoComisionLocal)? sol.MontoComisionLocal: (sol.MontoComisionLocal.divide(CalculaImporteIva, MathContext.DECIMAL128)).multiply(IVA);
					movcob.MontoLiquidacionCobro = new BigDecimal(0).equals(sol.MontoComisionLocal)? sol.MontoComisionLocal: (sol.MontoComisionLocal.divide(CalculaImporteIva, MathContext.DECIMAL128)).multiply(IVA);
					movcob.Porcentaje = new BigDecimal(cobros.get(0).ValorCobro.toString());
					entradaCrearMovimientoComprobante.p_MovimientosCobros.add(movcob);
				}
				
				//Forma de Pago
				if (_c.RescateFondoMutuo.equals(sol.IdTipoMovimientoFondoMutuo)) {
					CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada movfp = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
					movfp.AfectarCuentaInversion = _c.SI;
					movfp.AfectarTesoreria = _c.NO;
					movfp.IdMedioDePago = _c.CuentaInversion;
					movfp.IdMoneda = sol.IdMoneda;
					movfp.MontoLiquidacionFormaDePago = sol.MontoComisionLocal;
					movfp.NumeroMvtoCmbteFormaDePago = sol.IdSolicitudAporteRescate;
					movfp.NumeroMvtoCmbte = sol.IdSolicitudAporteRescate;
					movfp.FechaVctoTarjetaBancaria = "1900-01-01";
					movfp.FechaDocumentoPago = Fecha.stringToSqlDate(sol.FechaCertificacion);
					entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(movfp);
				}
				
				
			}
		}
		if(entradaCrearMovimientoComprobante.p_Movimientos.size() > 0){
			entradaCrearMovimientoComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearMovimientoComprobante.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearMovimientoComprobante.p_IdModulo = entrada.p_IdModulo;
			entradaCrearMovimientoComprobante.p_simularOperacion = _c.NO;
			entradaCrearMovimientoComprobante.p_UsaReglasNegocio = _c.SI;
			entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = _c.NO;
			CrearMovimientoComprobante serviceCrearMovimientoComprobante = new CrearMovimientoComprobante();
			salidaCrearMovimientoComprobante = serviceCrearMovimientoComprobante.logica(con,entradaCrearMovimientoComprobante);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.creaMovimientoFacturacion");
		return salidaCrearMovimientoComprobante;
	}
	
	private CrearComprobante_Salida creaComprobanteFacturacion(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada, CrearMovimientoComprobante_Salida movimientos, String fechaSistema) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.creaComprobanteFacturacion");
		CrearComprobante_Salida salidaCrearComprobante = new CrearComprobante_Salida();
		salidaCrearComprobante.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearComprobante.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		
		CrearComprobante.CrearComprobante_Entrada entradaCrearComprobante = new CrearComprobante.CrearComprobante_Entrada();
		
		HashMap<Long,Object> rescatesHash = Utils.List2Hash(solicitudes, "IdSolicitudAporteRescate");
		for(CrearMovimientoComprobante.MovimientoComprobante_Salida mov : movimientos.p_out_Movimientos){
			SolicitudesProcesoCertificacion res = (SolicitudesProcesoCertificacion) rescatesHash.get(mov.NumeroMvtoCmbte);
			if (!_c.EstadoRechazado.equals(res.EstadoProceso) && ((_c.RescateFondoMutuo.equals(res.IdTipoMovimientoFondoMutuo) || _c.Reinversion.equals(res.IdTipoMovimientoFondoMutuo)) && res.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1 && !_c.SI.equals(res.RelacionTraspaso))){
				CrearComprobante.MovimientoComprobante_Entrada movimientoAFacturar = new CrearComprobante.MovimientoComprobante_Entrada();
				movimientoAFacturar.IdMovimientoComprobante = mov.IdMovimientoComprobante;
				movimientoAFacturar.FechaComprobante = fechaSistema;
				entradaCrearComprobante.P_MOVIMIENTOFACTURAR.add(movimientoAFacturar);
			}
		}
		if(entradaCrearComprobante.P_MOVIMIENTOFACTURAR.size() > 0){
			entradaCrearComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearComprobante.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearComprobante.p_IdModulo = entrada.p_IdModulo;
			entradaCrearComprobante.p_simularOperacion = _c.NO;
			entradaCrearComprobante.p_UsaReglasNegocio = _c.SI;
			entradaCrearComprobante.p_EsIngresoAutomatico = _c.NO;
			CrearComprobante serviceCrearComprobante = new CrearComprobante();
			salidaCrearComprobante = serviceCrearComprobante.logica(con,entradaCrearComprobante);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.creaComprobanteFacturacion");
		return salidaCrearComprobante;
	}
	
	private SalidaCrearSolicitudAporteReinversion crearAporteReinversion(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada, List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.crearAporteReinversion");
		CrearSolicitudAporteReinversion crearSolicitudAporteReinversion = new CrearSolicitudAporteReinversion();
		EntradaCrearSolicitudAporteReinversion entradaCrearSolicitudAporteReinversion = new EntradaCrearSolicitudAporteReinversion();
		SalidaCrearSolicitudAporteReinversion salidaCrearSolicitudAporteReinversion = new SalidaCrearSolicitudAporteReinversion();
		
		List<Long> idSolicitudes = new ArrayList<Long>();
		
		for(SolicitudesProcesoCertificacion sol : solicitudes) {
			if(!idSolicitudes.contains(sol.IdSolicitudAporteRescate)) idSolicitudes.add(sol.IdSolicitudAporteRescate);
		}
		
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<ReinversionSerieFondoDestino> reinversionSerieFondoDestino = sql.query(con, SQL_SELECT_REINVERSION_DESTINO, params, ReinversionSerieFondoDestino.class);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<SolicitudOrigenReinversionSerieFondo> reinversionSerieFondo = sql.query(con, SQL_SELECT_REINVERSION_SERIE_FONDO, params, SolicitudOrigenReinversionSerieFondo.class);
		Map<Long,List<ReinversionSerieFondoDestino>> reinversionesAgrupadas = reinversionSerieFondoDestino.parallelStream().collect(Collectors.groupingBy(solicitud->solicitud.IdSolicitudAporteRescate));
		Map<Long,List<SalidaMovimientoCustodiaFondoMutuo>> movimientosCustodiaAgrupadas = carterasRetiradas.parallelStream().collect(Collectors.groupingBy(movimiento->movimiento.IdGenerarMovimientoCustodia));
		Long c = 1L;
		HashMap<Long,Object> reinversiones = Utils.List2Hash(solicitudes, "IdSolicitudAporteRescate");
		HashMap<Long,Object> reinversionSerie = Utils.List2Hash(reinversionSerieFondo, "IdSolicitudAporteRescate");
		HashMap<Long,Long> map = new HashMap<Long,Long>();
		for(Map.Entry<Long,List<ReinversionSerieFondoDestino>> lst : reinversionesAgrupadas.entrySet()) {
			
			Long tamano= Long.valueOf(lst.getValue().size());
			BigDecimal montoCuotasTotal =BigDecimal.ZERO;
			Long indexTotal = 1L;
			
			for(ReinversionSerieFondoDestino apor : lst.getValue()){
				SolicitudesProcesoCertificacion sol = (SolicitudesProcesoCertificacion) reinversiones.get(apor.IdSolicitudAporteRescate);
				SolicitudOrigenReinversionSerieFondo solReinversionSerie = (SolicitudOrigenReinversionSerieFondo) reinversionSerie.get(sol.IdSolicitudAporteRescate);
				if(!_c.EstadoRechazado.equals(sol.EstadoProceso) && _c.Reinversion.equals(sol.IdTipoMovimientoFondoMutuo)){
					
					if((!sol.IdFamiliaFondo.isEmpty() && sol.IdFamiliaFondo.equals(apor.IdFamiliaFondo)) && sol.IdMoneda.equals(apor.IdMoneda)) {
						List<SalidaMovimientoCustodiaFondoMutuo> movimientosCustodia = movimientosCustodiaAgrupadas.get(apor.IdSolicitudAporteRescate);
						BigDecimal montoEsperadoAporte = sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128);
						montoEsperadoAporte = MonedaFormato.formatear(con,montoEsperadoAporte,MonedaFormato.MONTO,sol.IdMoneda).valor;
						BigDecimal montoAporte = BigDecimal.ZERO;
						if(movimientosCustodia != null && movimientosCustodia.size() > 0) {
							for(SalidaMovimientoCustodiaFondoMutuo salidaMovimiento : movimientosCustodia) {
								CrearSolicitudAporteReinversion.InSolicitudesAporte aporte = new CrearSolicitudAporteReinversion.InSolicitudesAporte();
								BigDecimal porcentajeRelacionCantidad = salidaMovimiento.Cantidad.divide(sol.NumeroCuotas, MathContext.DECIMAL128);
								aporte.MontoOrigen = sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128).multiply(porcentajeRelacionCantidad, MathContext.DECIMAL128);
						    	aporte.Monto = sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128).multiply(porcentajeRelacionCantidad, MathContext.DECIMAL128);
						    	aporte.Monto = MonedaFormato.formatear(con,aporte.Monto,MonedaFormato.MONTO,sol.IdMoneda).valor;
						    	aporte.MontoOrigen = MonedaFormato.formatear(con,aporte.MontoOrigen,MonedaFormato.MONTO,sol.IdMoneda).valor;
						    	if(aporte.Monto.compareTo(BigDecimal.ZERO) == 1) {
						    		map.put(c, sol.IdSolicitudAporteRescate);
									aporte.IdInterno = c;
									aporte.IdCliente = sol.IdCliente;
							    	aporte.IdPortafolio = sol.IdPortafolio;
							    	aporte.IdSerieFondoOrigen = sol.IdSerieFondo;
							    	aporte.IdEmisorOrigen = sol.IdEmisor;
							    	aporte.CostoUF = salidaMovimiento.CostoInversionEnFactorAjusteARetirar.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128);
							    	aporte.FechaOrigenInversion = Fecha.stringToSqlDate(salidaMovimiento.FechaOrigenCompra);
							    	aporte.IndicadorMLT = sol.IndicadorMLT;
							    	aporte.IdSerieFondo = apor.IdSerieFondoDestino;
							    	aporte.NumeroCuotas = BigDecimal.ZERO;
							    	aporte.IdMoneda = sol.IdMoneda;
							    	aporte.IdMedioDePago = sol.IdMedioDePago;
							    	aporte.Visado = _c.SI;
							    	aporte.SolicitudBatch = sol.SolicitudBatch;
							    	aporte.OrigenOperacion = sol.OrigenOperacion;
							    	aporte.IdRegimenTributario = apor.IdRegimenTributario;
							    	aporte.Glosa = sol.Glosa;
							    	aporte.IdAutorizadoOrigen = 0L;
							    	aporte.FolioContrato = sol.FolioContrato;
							    	if(sol.IdMoneda.equalsIgnoreCase(apor.IdMoneda)) {
							    		if("S".equals(solReinversionSerie.FondoOrigenInternacionalDolar) && "S".equals(solReinversionSerie.FondoDestinoInternacionalDolar) && "N".equals(apor.Switch)) {
							    			aporte.FechaEmision = Fecha.stringToSqlDate(sol.FechaCertificacion);
							    		} else {
							    			aporte.FechaEmision = Fecha.stringToSqlDate(sol.FechaLiquidacion);
							    		}
							    	}else {
							    		aporte.FechaEmision = Fecha.stringToSqlDate(sol.FechaLiquidacion);
							    	}
							    	aporte.IdSolicitudAporteRescateRelacionado = sol.IdSolicitudAporteRescate;
							    	aporte.IdAgrupador = sol.IdAgrupador;
							    	aporte.Switch = apor.Switch;
							    	aporte.FechaCertificacionRescateOrigen = Fecha.stringToSqlDate(sol.FechaCertificacion);
							    	aporte.FechaLiquidacionRescateOrigen = Fecha.stringToSqlDate(sol.FechaLiquidacion);
							    	aporte.FechaValorCuotaRescateOrigen = Fecha.stringToSqlDate(sol.FechaValorCuota);
							    	aporte.Canal = sol.IdTipoCanalIngreso;
							    	aporte.IngresoAutomatico = sol.IngresoAutomatico;
							    	aporte.MontoTotalInversion =  sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128));
							    	if(_c.Dolar.equalsIgnoreCase(sol.IdMoneda)) {
										if(sol.FechaCertificacion != null && sol.FechaLiquidacion != null) {
											aporte.FechaCertificaSpotCompra = Fecha.toStringDate(sol.FechaCertificacion);
											aporte.FechaLiquidaSpotCompra = Fecha.toStringDate(sol.FechaLiquidacion);
										}
									}
							    	if(sol.IdMonedaSistema.equals(sol.IdMoneda)) {
							    		if(sol.FechaCertificacion != null && sol.FechaLiquidacion != null) {
											aporte.FechaCertificaSpotCompra = Fecha.toStringDate(sol.FechaAceptacion);
											aporte.FechaLiquidaSpotCompra = Fecha.toStringDate(sol.FechaAceptacion);
										}
							    	}
							    	entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.add(aporte);					    						    	
							    	c++;
							    	montoAporte = montoAporte.add(aporte.Monto, MathContext.DECIMAL128);
							    	montoCuotasTotal = montoCuotasTotal.add(aporte.Monto, MathContext.DECIMAL128);
						    	}
							}
						}
						//SE SUMA LA DIFERENCIA AL ULTIMO APORTE
						
						BigDecimal diferencia = montoEsperadoAporte.subtract(montoAporte);
						CrearSolicitudAporteReinversion.InSolicitudesAporte ultimoAporte = entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.parallelStream().filter(solicitud->solicitud.IdSolicitudAporteRescateRelacionado.equals(apor.IdSolicitudAporteRescate)).collect(Collectors.toList()).get(0);
						if(ultimoAporte != null && diferencia.compareTo(BigDecimal.ZERO) != 0) {
							BigDecimal montoAnterior = ultimoAporte.Monto;
							ultimoAporte.Monto = ultimoAporte.Monto.add(diferencia,MathContext.DECIMAL128);
							ultimoAporte.MontoOrigen = ultimoAporte.MontoOrigen.add(diferencia,MathContext.DECIMAL128);
							ultimoAporte.Monto = MonedaFormato.formatear(con,ultimoAporte.Monto,MonedaFormato.MONTO,sol.IdMoneda).valor;
							ultimoAporte.MontoOrigen = MonedaFormato.formatear(con,ultimoAporte.MontoOrigen,MonedaFormato.MONTO,sol.IdMoneda).valor;
							montoCuotasTotal = montoCuotasTotal.add(ultimoAporte.Monto.subtract(montoAnterior, MathContext.DECIMAL128),MathContext.DECIMAL128);
						}
						
						indexTotal++;
					}else {
						CrearSolicitudAporteReinversion.InSolicitudesAporte aporte = new CrearSolicitudAporteReinversion.InSolicitudesAporte();
						map.put(c, sol.IdSolicitudAporteRescate);
						aporte.IdInterno = c;
						aporte.IdCliente = sol.IdCliente;
				    	aporte.IdPortafolio = sol.IdPortafolio;
				    	aporte.IdSerieFondoOrigen = sol.IdSerieFondo;
				    	aporte.IdEmisorOrigen = sol.IdEmisor;
				    	aporte.CostoUF = sol.CostoUF.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128);
				    	aporte.FechaOrigenInversion = Fecha.stringToSqlDate(sol.FechaCertificacion);
				    	aporte.MontoOrigen = sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128);
				    	aporte.Monto = sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128);
				    	aporte.IndicadorMLT = sol.IndicadorMLT;
				    	aporte.IdSerieFondo = apor.IdSerieFondoDestino;
				    	aporte.Monto = MonedaFormato.formatear(con,aporte.Monto,MonedaFormato.MONTO,sol.IdMoneda).valor;
				    	aporte.MontoOrigen = MonedaFormato.formatear(con,aporte.MontoOrigen,MonedaFormato.MONTO,sol.IdMoneda).valor;
						aporte.NumeroCuotas = BigDecimal.ZERO;
				    	aporte.IdMoneda = sol.IdMoneda;
				    	aporte.IdMedioDePago = sol.IdMedioDePago;
				    	aporte.Visado = _c.SI;
				    	aporte.SolicitudBatch = sol.SolicitudBatch;
				    	aporte.OrigenOperacion = sol.OrigenOperacion;
				    	aporte.IdRegimenTributario = apor.IdRegimenTributario;
				    	aporte.Glosa = sol.Glosa;
				    	aporte.IdAutorizadoOrigen = 0L;
				    	aporte.FolioContrato = sol.FolioContrato;
				    	if(sol.IdMoneda.equalsIgnoreCase(apor.IdMoneda)) {
				    		if("S".equals(solReinversionSerie.FondoOrigenInternacionalDolar) && "S".equals(solReinversionSerie.FondoDestinoInternacionalDolar) && "N".equals(apor.Switch)) {
				    			aporte.FechaEmision = Fecha.stringToSqlDate(sol.FechaCertificacion);
				    		} else {
				    			aporte.FechaEmision = Fecha.stringToSqlDate(sol.FechaLiquidacion);
				    		}
				    	}else {
				    		aporte.FechaEmision = Fecha.stringToSqlDate(sol.FechaLiquidacion);
				    	}
				    	aporte.IdSolicitudAporteRescateRelacionado = sol.IdSolicitudAporteRescate;
				    	aporte.IdAgrupador = sol.IdAgrupador;
				    	aporte.Switch = apor.Switch;
				    	aporte.FechaCertificacionRescateOrigen = Fecha.stringToSqlDate(sol.FechaCertificacion);
				    	aporte.FechaLiquidacionRescateOrigen = Fecha.stringToSqlDate(sol.FechaLiquidacion);
				    	aporte.FechaValorCuotaRescateOrigen = Fecha.stringToSqlDate(sol.FechaValorCuota);
				    	aporte.Canal = sol.IdTipoCanalIngreso;
				    	aporte.IngresoAutomatico = sol.IngresoAutomatico;
				    	aporte.MontoTotalInversion =  sol.MontoPagoLocal.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128));
				    	if(_c.Dolar.equalsIgnoreCase(sol.IdMoneda)) {
							if(sol.FechaCertificacion != null && sol.FechaLiquidacion != null) {
								aporte.FechaCertificaSpotCompra = Fecha.toStringDate(sol.FechaCertificacion);
								aporte.FechaLiquidaSpotCompra = Fecha.toStringDate(sol.FechaLiquidacion);
							}
						}
				    	if(sol.IdMonedaSistema.equals(sol.IdMoneda)) {
				    		if(sol.FechaCertificacion != null && sol.FechaLiquidacion != null) {
								aporte.FechaCertificaSpotCompra = Fecha.toStringDate(sol.FechaAceptacion);
								aporte.FechaLiquidaSpotCompra = Fecha.toStringDate(sol.FechaAceptacion);
							}
				    	}
				    	montoCuotasTotal = montoCuotasTotal.add(aporte.Monto, MathContext.DECIMAL128);
				    	entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.add(aporte);
				    	c++;
				    	indexTotal++;
					}
					//SE SUMA LA DIFERENCIA AL ULTIMO APORTE
					if(indexTotal.compareTo(tamano) == 1) {
			    		BigDecimal diferencia = sol.MontoPagoLocal.subtract(montoCuotasTotal);
			    		CrearSolicitudAporteReinversion.InSolicitudesAporte ultimoAporte = entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.parallelStream().filter(solicitud->solicitud.IdSolicitudAporteRescateRelacionado.equals(apor.IdSolicitudAporteRescate)).collect(Collectors.toList()).get(0);
						if(ultimoAporte != null && diferencia.compareTo(BigDecimal.ZERO) != 0) {
							ultimoAporte.Monto = ultimoAporte.Monto.add(diferencia,MathContext.DECIMAL128);
							ultimoAporte.MontoOrigen = ultimoAporte.MontoOrigen.add(diferencia,MathContext.DECIMAL128);
							ultimoAporte.Monto = MonedaFormato.formatear(con,ultimoAporte.Monto,MonedaFormato.MONTO,sol.IdMoneda).valor;
							ultimoAporte.MontoOrigen = MonedaFormato.formatear(con,ultimoAporte.MontoOrigen,MonedaFormato.MONTO,sol.IdMoneda).valor;
						}
			    	}
				}
			}
		}
 
		if(entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.size() > 0){
			entradaCrearSolicitudAporteReinversion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearSolicitudAporteReinversion.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearSolicitudAporteReinversion.p_IdModulo = entrada.p_IdModulo;
			entradaCrearSolicitudAporteReinversion.p_SimularOperacion = _c.NO;
			entradaCrearSolicitudAporteReinversion.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaCrearSolicitudAporteReinversion.p_IdTipoCanalIngreso = _c.CanalAgente;	
			salidaCrearSolicitudAporteReinversion = crearSolicitudAporteReinversion.logica(con, entradaCrearSolicitudAporteReinversion);
			for (MensajeSalida msj : salidaCrearSolicitudAporteReinversion.P_OUT_MENSAJES) {
				if(map.get(msj.id) != null)
					msj.id = map.get(msj.id);
			}
			for (ResultadoSolicitud solicitud : salidaCrearSolicitudAporteReinversion.P_OUT_RESULTADO) {
				solicitud.IdInterno = map.get(solicitud.IdInterno);
			}
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.crearAporteReinversion");
		return salidaCrearSolicitudAporteReinversion;
	} 
	
	private CalculaCustodiaRescate.InSolicitud getPojoSolCus(SolicitudesProcesoCertificacion sol, CalculaCustodiaRescate.InSolicitud solCus){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.getPojoSolCus");
		solCus.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
		solCus.IdSerieFondo = sol.IdSerieFondo;
		solCus.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
		solCus.IdMoneda = sol.IdMoneda;
		solCus.IdMonedaFondo = sol.IdMonedaFondo;
		solCus.Monto = sol.Monto;
		solCus.NumeroCuotas = sol.NumeroCuotas;
		solCus.ValorCuota = sol.ValorCuota;
		solCus.TipoSolicitud = sol.TipoSolicitud;
		solCus.IdInstrumento = sol.IdInstrumento;
		solCus.IdCliente = sol.IdCliente;
		solCus.IdPortafolio = sol.IdPortafolio;
		solCus.FechaValorCuota = sol.FechaValorCuota;
		solCus.IdRegimenTributario = sol.IdRegimenTributario;
		solCus.IdOmnibus = sol.IdOmnibus;
		logger.trace("FIN::CertificarSolicitudAporteRescate.getPojoSolCus");
		return solCus;
	}
	
	private EntradaCertificarSolicitudAporteRescate getPojoEntradaSwitch(EntradaCertificarSolicitudAporteRescate entrada, EntradaCertificarSolicitudAporteRescate entradaSwitch){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.getPojoEntradaSwitch");
		entradaSwitch.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaSwitch.p_IdUsuario = entrada.p_IdUsuario;
		entradaSwitch.p_IdModulo = entrada.p_IdModulo;
		entradaSwitch.p_SimularOperacion = entrada.p_SimularOperacion;
		entradaSwitch.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaSwitch.p_FechaCertificacion = entrada.p_FechaCertificacion;
		entradaSwitch.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		logger.trace("FIN::CertificarSolicitudAporteRescate.getPojoEntradaSwitch");
		return entradaSwitch;
	}
	
	private ObjCargoAbonoAGenerar getPojoCardif(ObjCargoAbonoAGenerar obj, ObjCargoAbonoAGenerar cardif){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.getPojoCardif");
		cardif.NumeroCargoAbono = obj.NumeroCargoAbono;
		cardif.IdPortafolio = obj.IdPortafolio;
		cardif.IdMoneda = obj.IdMoneda;
		cardif.IdTipoMovimiento = obj.IdTipoMovimiento;
		cardif.IdConcepto = obj.IdConcepto;
		cardif.Descripcion = obj.Descripcion;
		cardif.IdCondicionDeLiquidacion = obj.IdCondicionDeLiquidacion;
		cardif.FechaCondicionDeLiquidacion = obj.FechaCondicionDeLiquidacion;
		cardif.Monto = obj.Monto;
		cardif.IdEjecutivo = obj.IdEjecutivo;
		cardif.IdSucursal = obj.IdSucursal;
		cardif.IdTipoProducto = obj.IdTipoProducto;
		cardif.IdTipoOperacion = obj.IdTipoOperacion;
		cardif.IdOperacion = obj.IdOperacion;
		cardif.FechaOperacion = obj.FechaOperacion;
		cardif.FechaLiquidacionOperacion = obj.FechaLiquidacionOperacion;
		cardif.FechaVencimientoOperacion = obj.FechaVencimientoOperacion;
		cardif.IdMonedaOperacion = obj.IdMonedaOperacion;
		cardif.MontoOperacion = obj.MontoOperacion;
		cardif.TipoCambioOperacion = obj.TipoCambioOperacion;
		cardif.IdContraparte = obj.IdContraparte;
		cardif.IdTipoComprobante = obj.IdTipoComprobante;
		cardif.IdComprobante = obj.IdComprobante;
		cardif.FolioComprobante = obj.FolioComprobante;
		cardif.IdComprobanteDetalle = obj.IdComprobanteDetalle;
		cardif.IdComprobanteFormaDePago = obj.IdComprobanteFormaDePago;
		cardif.IdTipoMovimientoAsociado = obj.IdTipoMovimientoAsociado;
		cardif.IdMovimientoAsociado = obj.IdMovimientoAsociado;
		cardif.FechaComprobante = obj.FechaComprobante;
		cardif.IdPersonaPagoCobro = obj.IdPersonaPagoCobro;
		cardif.IdInstrumento = obj.IdInstrumento;
		cardif.IdTipoInstrumento = obj.IdTipoInstrumento;
		cardif.Cantidad = obj.Cantidad;
		cardif.Nemo = obj.Nemo;
		cardif.EsIndicadorBilateral = obj.EsIndicadorBilateral;
		cardif.EsCargoAbonoPorTraspaso = obj.EsCargoAbonoPorTraspaso;
		cardif.IdPortafolioTraspaso = obj.IdPortafolioTraspaso;
		cardif.GeneroContraparteEnTesoreria = obj.GeneroContraparteEnTesoreria;
		cardif.CertificadoAutomatico = obj.CertificadoAutomatico;
		logger.trace("FIN::CertificarSolicitudAporteRescate.getPojoCardif");
		return cardif;
	}
	
	private SolicitudAporteRescate getPojoSolicitudAporteRescate(SolicitudesProcesoCertificacion sol, SolicitudAporteRescate s){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.getPojoSolicitudAporteRescate");
		s.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
		s.IdTipoCanalIngreso = sol.IdTipoCanalIngreso;
		s.IdCliente = sol.IdCliente;
		s.IdPortafolio = sol.IdPortafolio;
		s.TipoSolicitud = sol.TipoSolicitud;
		s.IdSerieFondo = sol.IdSerieFondo;
		s.IdTipoMovimientoFondoMutuo = sol.IdTipoMovimientoFondoMutuo;
		s.IdSerieFondoDestino = sol.IdSerieFondoDestino;
		s.IdEmisorDestino = sol.IdEmisorDestino;
		s.IndicadorMLT = sol.IndicadorMLT;
		s.Overnight = sol.Overnight;
		s.IdSolicitudAporteRescateRelacionado = sol.IdSolicitudAporteRescateRelacionado;
		s.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
		s.FechaAceptacion = sol.FechaAceptacion;
		s.FechaCertificacion = sol.FechaCertificacion;
		s.FechaLiquidacion = sol.FechaLiquidacion;
		s.EsPorMontoLiquido = sol.EsPorMontoLiquido;
		s.Monto = sol.Monto;
		s.NumeroCuotas = sol.NumeroCuotas;
		s.ValorCuota = sol.ValorCuota;
		s.CostoUF = sol.CostoUF;
		s.CostoMonedaLocal = sol.CostoMonedaLocal;
		s.ParidadFechaCertificacion = sol.ParidadFechaCertificacion;
		s.ValorUFFechaInversion = sol.ValorUFFechaInversion;
		s.IdMoneda = sol.IdMoneda;
		s.IdMonedaPago = sol.IdMonedaPago;
		s.GeneraSpot = sol.GeneraSpot;
		s.MontoComision = sol.MontoComision;
		s.ComisionInformada = sol.ComisionInformada;
		s.MontoComisionLocal = sol.MontoComisionLocal;
		s.MontoLiquido = sol.MontoLiquido;
		s.MontoLiquidoLocal = sol.MontoLiquidoLocal;
		s.MontoPago = sol.MontoPago;
		s.MontoPagoLocal = sol.MontoPagoLocal;
		s.Spread = sol.Spread;
		s.EstadoLiquidacion = sol.EstadoLiquidacion;
		s.MueveCta = sol.MueveCta;
		s.FolioCartaLiquidacion = sol.FolioCartaLiquidacion;
		s.IdMedioDePago = sol.IdMedioDePago;
		s.Glosa = sol.Glosa;
		s.IdUsuario = sol.IdUsuario;
		s.OrigenOperacion = sol.OrigenOperacion;
		s.SolicitudBatch = sol.SolicitudBatch;
		s.IdRegimenTributario = sol.IdRegimenTributario;
		s.TipoCarteraNormativa = sol.TipoCarteraNormativa;
		s.IdRegimenTributarioDestino = sol.IdRegimenTributarioDestino;
		s.TipoCarteraNormativaDestino = sol.TipoCarteraNormativaDestino;
		s.IdPreferenciaPagoPortafolio = sol.IdPreferenciaPagoPortafolio;
		s.IdAutorizadoOrigen = sol.IdPreferenciaPagoPortafolio;
		s.IdMonedaSistema = sol.IdMonedaSistema;
		s.MontoMonedaSistema = sol.MontoMonedaSistema;
		s.Switch = sol.Switch;
		s.IdEmpleadoEjecutivo = sol.IdEmpleadoEjecutivo;
		s.IdPersona = sol.IdPersona;
		s.IdInstrumento = sol.IdInstrumento;
		s.IdTipoInstrumento = sol.IdTipoInstrumento;
		s.EstadoProceso = sol.EstadoProceso;
		s.IdIngresoEgreso = sol.IdIngresoEgreso;
		s.IdOperacionSpot = sol.IdOperacionSpot;
		s.IdCartera = sol.IdCartera;
		s.IdAgrupador = sol.IdAgrupador;
		s.RelacionTraspaso = sol.RelacionTraspaso;
		s.IdPersonaEmisorDestino = sol.IdPersonaEmisorDestino;
		s.RazonRetiro = sol.RazonRetiro;
		logger.trace("FIN::CertificarSolicitudAporteRescate.getPojoSolicitudAporteRescate");
		return s;
	}
	
	private Long obtenerPorcentajeRetencion(Connection con, Long idEmpresa) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerPorcentajeRetencion");
		Long porcentajeRetencion = 0L;
		try {
			porcentajeRetencion= ConfiguracionSistema.getParametroConfiguracionLong(con,idEmpresa, "PLATAFORMA","APV.PorcentajeRetencion");
		}catch(Exception e) {
			logger.error("ERROR::CertificarSolicitudAporteRescate.obtenerPorcentajeRetencion:: Al encontrar parametro APV.PorcentajeRetencion");
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerPorcentajeRetencion");
		return porcentajeRetencion;
	}
	
	

	private String obtenerConceptoRetencionPagoSII(Connection con, Long idEmpresa) throws Exception {		
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerConceptoRetencionPagoSII");
		String conceptoRetencionPagoSII = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "CONCEPTO_RETENCION_PAGO_SII");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerConceptoRetencionPagoSII");
		return conceptoRetencionPagoSII;		
	}
	

	
	private String obtenerNotificarOWP(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerNotificarOWP");
		String notificaOWP = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerNotificarOWP");
		return notificaOWP;
	}
	
	private List<SolicitudesProcesoCertificacion> actualizarSolicitudes(Connection con, List<SolicitudesProcesoCertificacion> solicitudes) throws SQLException {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.actualizarSolicitudes");
		SQL sql = new SQL();
		List<SolicitudesProcesoCertificacion> solActualizar = filtrarSolicitudesPendientesYAdvertencias(solicitudes).collect(Collectors.toList());
		//ACTUALIZAMOS SOLICITUDES PROCESADAS OK
		{			
			if(solActualizar.size() > 0){
				sql.batch(con, SQL_UPDATE_SOLICITUDES, solActualizar, SolicitudesProcesoCertificacion.class);
			}
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.actualizarSolicitudes");
		return solActualizar;
	}
	
	private void notificar(Connection con, List<SolicitudesProcesoCertificacion> solActualizar, EntradaCertificarSolicitudAporteRescate entrada) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.notificar");
		NotificarEstadoOrdenFondoMutuo notificador = new NotificarEstadoOrdenFondoMutuo();
		EntradaNotificarEstadoOrdenFondoMutuo entradaNotificador = new EntradaNotificarEstadoOrdenFondoMutuo();
		entradaNotificador.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaNotificador.p_IdUsuario = entrada.p_IdUsuario;
		entradaNotificador.p_IdModulo = entrada.p_IdModulo;
		entradaNotificador.p_Solicitudes = solActualizar.parallelStream().map(solicitud->solicitud.IdSolicitudAporteRescate).collect(Collectors.toList());
		notificador.logicaAsync(con, entradaNotificador);
		logger.trace("FINs::CertificarSolicitudAporteRescate.notificar");
	}
	
	private SalidaCertificarSolicitudAporteRescate certificarAportesSwitch(Connection con, List<InSolicitudes> aportesSwitch, EntradaCertificarSolicitudAporteRescate entrada) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.certificarAportesSwitch");
		EntradaCertificarSolicitudAporteRescate entradaSwitch = new EntradaCertificarSolicitudAporteRescate();
		entradaSwitch = getPojoEntradaSwitch(entrada, entradaSwitch);
		entradaSwitch.p_NotificaOWP = "N";
		entradaSwitch.p_CertificacionAporteSwitchSimulado = entradaSwitch.p_SimularOperacion;
		entradaSwitch.p_Solicitudes = aportesSwitch;
		CertificarSolicitudAporteRescate certificarSwitch = new CertificarSolicitudAporteRescate();
		SalidaCertificarSolicitudAporteRescate salidaCertificacionSwitch = certificarSwitch.logica(con,entradaSwitch);
		logger.trace("INICIO::CertificarSolicitudAporteRescate.certificarAportesSwitch");
		return salidaCertificacionSwitch;
	}
	
	private void notificarOWP(Connection con, List<InSolicitudes> aportes_switch, List<SolicitudesProcesoCertificacion> solActualizar, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.notificarOWP");
		SQL sql = new SQL();
		List<Long> idSolicitudes = new ArrayList<Long>();
		List<Long> idSolicitudesAportesSwitch = aportes_switch.parallelStream().filter(solicitud->_c.OWP.equals(solicitud.IdTipoCanalIngreso)).map(solicitud->solicitud.IdSolicitudAporteRescate).distinct().collect(Collectors.toList());
		List<Long> idSolicitudesActualizar = solActualizar.parallelStream().filter(solicitud->_c.OWP.equals(solicitud.IdTipoCanalIngreso)).map(solicitud->solicitud.IdSolicitudAporteRescate).distinct().collect(Collectors.toList());
		idSolicitudes.addAll(idSolicitudesAportesSwitch);
		idSolicitudes.addAll(idSolicitudesActualizar);
		
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdEmpresa", idEmpresa);
		parametros.put("IdSolicitudAporteRescate", idSolicitudes);
		List<SolicitudesProcesoCertificacion> solicitudesModificadas = sql.query(con, SQL_SELECT_SOLICITUDES, parametros, SolicitudesProcesoCertificacion.class);
		
		NotificarEstadoOrdenesOWP servicioNotOwp = new NotificarEstadoOrdenesOWP();
		
		List<NotificarEstadoOrdenes_Entrada> listOrdenNotiOWP = new ArrayList<NotificarEstadoOrdenes_Entrada>();
		for (SolicitudesProcesoCertificacion solicitud : solicitudesModificadas) {
			NotificarEstadoOrdenes_Entrada entradaSolicitud = new NotificarEstadoOrdenes_Entrada();
			entradaSolicitud.IdEmpresa = idEmpresa;
			entradaSolicitud.IdUsuario = idUsuario;
			entradaSolicitud.Solsec = solicitud.IdSolicitudAporteRescate;
			entradaSolicitud.NTitulos = solicitud.NumeroCuotas;
			entradaSolicitud.Precio = solicitud.ValorCuota;
			entradaSolicitud.Fecha = solicitud.FechaAceptacion.toString();
			entradaSolicitud.Estado = solicitud.EstadoSolicitud; 
			entradaSolicitud.Comision = solicitud.MontoComision;
			entradaSolicitud.Gastos = new BigDecimal(0);
			entradaSolicitud.Mercado = "F"; 
			entradaSolicitud.Divisa = solicitud.IdMoneda;
			entradaSolicitud.TipoCambio = solicitud.CostoMonedaLocal;
			entradaSolicitud.SolsecAsoc = (solicitud.IdTipoMovimientoFondoMutuo.equals(_c.Reinversion)) ? "" : solicitud.IdSolicitudAporteRescateRelacionado.toString();
			entradaSolicitud.Nemo = solicitud.IdSerieFondo;
			entradaSolicitud.IdClasificacionTipoInstrumento = "FM";
		    listOrdenNotiOWP.add(entradaSolicitud);
		}
		
		if(listOrdenNotiOWP.size() > 0){
			servicioNotOwp.go(con, listOrdenNotiOWP);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.notificarOWP");
	}
	
	private void cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot(Connection con, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot");
		List<Long> idsRelacionadosSolicitudesAporteReinversion = solicitudes.parallelStream()
				.filter(solicitud->!_c.EstadoRechazado.equals(solicitud.EstadoProceso) && _c.AporteReinversion.equals(solicitud.IdTipoMovimientoFondoMutuo) && solicitud.IdSolicitudAporteRescateRelacionado > 0 && !solicitud.IdMonedaSistema.equals(solicitud.IdMoneda) && !_c.SI.equals(solicitud.GeneraSpot))
				.map(solicitud->solicitud.IdSolicitudAporteRescateRelacionado)
				.distinct()
				.collect(Collectors.toList());
		if(idsRelacionadosSolicitudesAporteReinversion.size() > 0) {
			SQL sql = new SQL();
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("IdSolicitudAporteRescateRelacionado", idsRelacionadosSolicitudesAporteReinversion);
			List<SolicitudReinversion> solicitudesReinversion = sql.query(con, SQL_SELECT_MONTO_MONEDA_SISTEMA_SOLICITUDES_REINVERSION, parametros, SolicitudReinversion.class);
			sql.batch(con, SQL_UPDATE_MONTO_MONEDA_SISTEMA_SOLICITUDES_REINVERSION, solicitudesReinversion, SolicitudReinversion.class);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot");
	}
	
	private void agregarLogSistema(Connection con, EntradaCertificarSolicitudAporteRescate entrada, String fechaSistema) throws Exception {
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 0L;
		log.IdTipoLog = "CERTIFICACION APORTE RESCATE";
		log.IdModulo = _c.ModuloFondoMutuo;
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "CERTIFICACION_APORTE_RESCATE";
		log.EstadoRegistro = _c.EstadoCertificado;
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "";
		log.NombreProceso = "";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
	}
	
	private void marcarSolicitudesExitosas(List<SolicitudesProcesoCertificacion> solicitudes) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.marcarSolicitudesExitosas");
		filtrarSolicitudesPendientes(solicitudes).forEach(solicitud->{
			solicitud.EstadoProceso = _c.EstadoAprobado;
			solicitud.Codigo = "UI_EXITO";
		});
		logger.trace("FIN::CertificarSolicitudAporteRescate.marcarSolicitudesExitosas");
	}
	
	private List<Long> obtenerIdsSolicitudesConsultaTraspasos(List<SolicitudesProcesoCertificacion> solicitudes) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerIdsSolicitudesConsultaTraspasos");
		List<Long> idsSolicitudes = new ArrayList<Long>();
		idsSolicitudes.addAll(solicitudes.parallelStream().map(solicitud->solicitud.IdSolicitudAporteRescate).collect(Collectors.toList()));
		idsSolicitudes.addAll(solicitudes.parallelStream().filter(solicitud->!solicitud.IdSolicitudAporteRescateRelacionado.equals(0L)).map(solicitud->solicitud.IdSolicitudAporteRescateRelacionado).collect(Collectors.toList()));
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerIdsSolicitudesConsultaTraspasos");
		return idsSolicitudes;
	}
	
	private Traspaso obtenerTraspasoSolicitud(SolicitudesProcesoCertificacion solicitud, Map<Long,Traspaso> hashTraspasos) {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerTraspasoSolicitud");
		Traspaso traspaso = hashTraspasos.get(solicitud.IdSolicitudAporteRescate);
		if(traspaso == null) {
			traspaso = hashTraspasos.get(solicitud.IdSolicitudAporteRescateRelacionado);
		}
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerTraspasoSolicitud");
		return traspaso;
	}


	// Obtener valor cuota cargado usando servicio Precio y tasa (este servicio considerar valor cuota historico y valor cuota vigente)
	private Map<Long,Object> obtenerValorCuotaCargado(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaCertificarSolicitudAporteRescate entrada, Long paisSistema) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerValorCuota");

		EntregarPreciosTasas servicioPreciosTasas = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  entradaPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		
		entradaPrecioTasa.p_IdEmpresa 	= entrada.p_IdEmpresa;
		entradaPrecioTasa.p_IdModulo 	= entrada.p_IdModulo;
		entradaPrecioTasa.p_IdPais 		= paisSistema;
		entradaPrecioTasa.p_IdUsuario 	= entrada.p_IdUsuario;
		entradaPrecioTasa.p_DatosInstrumentos = solicitudes.parallelStream()
				
		.map(sol->{
			EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
			paramsRegIn.FechaConsulta 		= Fecha.utilDateToSqlDate(sol.FechaValorCuota);
			paramsRegIn.IdClasificacionTipoInstrumento = "FM";
			paramsRegIn.IdInstrumento 		= sol.IdInstrumento;
			paramsRegIn.IdMoneda 			= sol.IdMonedaFondo;
			paramsRegIn.IdRegistro 			= sol.IdSolicitudAporteRescate;
			paramsRegIn.IdTipoCriterioTasa 	= "0";
			paramsRegIn.IdTipoInstrumento 	= sol.IdTipoInstrumento;
			paramsRegIn.IdUso 				= "CARTERA";
			paramsRegIn.Nemotecnico 		= sol.IdSerieFondo;
			paramsRegIn.PlazoResidual 		= 0L;
			paramsRegIn.TipoCartera 		= "0";			
			return paramsRegIn;
		})
		.collect(Collectors.toList());
		
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = servicioPreciosTasas.logica(con, entradaPrecioTasa);
		Map<Long,Object> precios = salidaPrecioTasa.P_OUT_PRECIOSTASAS.parallelStream().filter(precio -> precio.IdInstrumento != null).collect(Collectors.toMap(instrumento->instrumento.IdRegistro, precio-> precio));
		
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerValorCuota");
		return precios;
	}
	
	public void reCalcularCostoUFAportes(Connection con,List<SolicitudesProcesoCertificacion> aportes, Long idEmpresa) {
		try {
			HashMap<String,Object> params = new HashMap<String,Object>();
			List<Long> idSolicitudes = aportes.stream().map(aporte -> aporte.IdSolicitudAporteRescateRelacionado).distinct().collect(Collectors.toList());
			params.put("IdSolicitudAporteRescate", idSolicitudes);
			
			List<SolicitudesProcesoCertificacion> solicitudRescateOrigen = obtenerSolicitudes(con, idEmpresa, idSolicitudes);
			
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = idEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			
			CalculoEntrada calculoEntradaUF = new CalculoEntrada();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Long idInterno = 1L;

			for (SolicitudesProcesoCertificacion aporte : aportes) {
				if ("APOREINV".equalsIgnoreCase(aporte.IdTipoMovimientoFondoMutuo)) {
					
					calculoEntradaUF.IdTrabajo = 1L;
					calculoEntradaUF.IdInterno = idInterno;
					calculoEntradaUF.Fecha = dateFormat.format(aporte.FechaValorCuota);
					calculoEntradaUF.IdMonedaOrigen = "UF";
					calculoEntradaUF.IdMonedaDestino = "CLP";
					calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntradaUF.MontoOrigen = BigDecimal.ONE;
					calculoEntradaUF.IdUso = "";
					calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
					idInterno++;
					
					SolicitudesProcesoCertificacion rescateOrigen = solicitudRescateOrigen.stream().filter(sol->sol.IdSolicitudAporteRescate.equals(aporte.IdSolicitudAporteRescateRelacionado)).findFirst().get();

					if (rescateOrigen != null && rescateOrigen.ValorUFFechaInversion != null && rescateOrigen.CostoUF!=null && rescateOrigen.ValorUFFechaInversion.compareTo(BigDecimal.ZERO) > 0 && rescateOrigen.CostoUF.compareTo(BigDecimal.ZERO) > 0) {
						BigDecimal ValorUFRescate = BigDecimal.ZERO;
						
						//tanto el rescate como los aportes por reinversion deben ser regimen 18Q para heredar el costoUF.
						boolean rescateEs18Q = _c.REGIMEN_18Q.equalsIgnoreCase(rescateOrigen.IdRegimenTributario);
						boolean todosSon18Q = _c.REGIMEN_18Q.equalsIgnoreCase(aporte.IdRegimenTributario) && rescateEs18Q;		
						
						if(!aporte.IdMonedaSistema.equalsIgnoreCase(aporte.IdMoneda) && !rescateOrigen.IdMonedaSistema.equalsIgnoreCase(rescateOrigen.IdMoneda) && rescateOrigen.IdMoneda.equalsIgnoreCase(aporte.IdMoneda)) {
							
							List<BigDecimal> MontosRelacionadosSolicitudesAporteReinversion = aportes.stream()
									.filter(solicitud->!_c.EstadoRechazado.equals(solicitud.EstadoProceso) && _c.AporteReinversion.equals(solicitud.IdTipoMovimientoFondoMutuo) && solicitud.IdSolicitudAporteRescateRelacionado > 0 && !solicitud.IdMonedaSistema.equals(solicitud.IdMoneda) && !_c.SI.equals(solicitud.GeneraSpot))
									.map(solicitud->solicitud.MontoMonedaSistema)
									.distinct()
									.collect(Collectors.toList());
							Optional<BigDecimal> suma = MontosRelacionadosSolicitudesAporteReinversion.stream()
							        .reduce(BigDecimal::add);

							BigDecimal sumaTotalMontoMonedaSistema = suma.orElse(BigDecimal.ZERO);
							
							ValorUFRescate = sumaTotalMontoMonedaSistema.divide(rescateOrigen.CostoUF,MathContext.DECIMAL64);
						}else {
							ValorUFRescate = rescateOrigen.MontoMonedaSistema.divide(rescateOrigen.CostoUF,MathContext.DECIMAL64);
						}
						
						if ("APV-A".equalsIgnoreCase(aporte.IdRegimenTributario) || "BF".equalsIgnoreCase(aporte.IdRegimenTributario) || "DC-DL".equalsIgnoreCase(aporte.IdRegimenTributario) || "DC-AL".equalsIgnoreCase(aporte.IdRegimenTributario) || "DC-R".equalsIgnoreCase(aporte.IdRegimenTributario)) {
							aporte.CostoUF = (aporte.MontoMonedaSistema.divide(ValorUFRescate,MathContext.DECIMAL64));
							aporte.ValorUFFechaInversion = rescateOrigen.ValorUFFechaInversion;
							
						} else if ("S".equalsIgnoreCase(aporte.IndicadorMLT) && todosSon18Q) {
							aporte.CostoUF = (aporte.MontoMonedaSistema.divide(ValorUFRescate,MathContext.DECIMAL64));
							aporte.ValorUFFechaInversion = rescateOrigen.ValorUFFechaInversion;
						
						}else {
							CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
							BigDecimal UFValorCuotaAporte = calculoSalida.p_out_CalculosSalida.stream().findAny().get().MontoLocal;
							//preguntamos si existe la salida del calcularParidad 
							aporte.CostoUF = UFValorCuotaAporte!=null && UFValorCuotaAporte!=BigDecimal.ZERO?aporte.MontoMonedaSistema.divide(UFValorCuotaAporte,MathContext.DECIMAL64):aporte.MontoMonedaSistema.divide(aporte.ValorUFFechaInversion,MathContext.DECIMAL64);
							aporte.ValorUFFechaInversion = UFValorCuotaAporte;
							
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01", "connections.properties")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String jsonaporte = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCertificacion\":\"2024-11-18\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":17788687}],\"p_Filtros\":[]}";
		String qaJsonrescate = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCertificacion\":\"2024-07-25\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":17405614}],\"p_Filtros\":[]}";
		
		EntradaCertificarSolicitudAporteRescate Objin = gson.fromJson(jsonaporte, EntradaCertificarSolicitudAporteRescate.class);
		CertificarSolicitudAporteRescate  demo=new CertificarSolicitudAporteRescate();				
		try {
			SalidaCertificarSolicitudAporteRescate ObjOut=demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.commit();
				
			}else {
				con.rollback();
			}
			con.commit();
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			//con.rollback(); 			
			con.commit();
    	}
		System.out.println("== END ==");
	}
}