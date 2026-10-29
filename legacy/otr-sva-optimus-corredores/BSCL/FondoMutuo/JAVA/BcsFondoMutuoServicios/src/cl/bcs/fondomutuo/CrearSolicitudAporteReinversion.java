package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.SalidaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SolicitudesProcesoCertificacion;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAGF.EntradaCrearSolicitudAGF;
import cl.bcs.fondomutuo.CrearSolicitudAGF.SalidaCrearSolicitudAGF;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.SalidaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.EntradaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.SalidaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.EntradaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.SalidaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ValidaMontoInversion.EntradaValidaMontoInversion;
import cl.bcs.fondomutuo.ValidaMontoInversion.SalidaValidaMontoInversion;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.EntradaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.SalidaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.FormaPagoSpot;
import cl.bcs.fondomutuo.pojo.IndicadoresPortafolio;
import cl.bcs.fondomutuo.pojo.IndicadoresPortafolioFondoMutuo;
import cl.bcs.fondomutuo.pojo.Moneda;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ReinversionSerieFondoDestino;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudAporteReinversion implements JavaCall{

	private final Log log = new Log("FM.CrearSolicitudAporteReinversion");
	static 	final Logger logger = LogManager.getLogger(CrearSolicitudAporteReinversion.class);
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql");
	private final String SQL_SELECT_MONEDA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
	private final String SQL_SELECT_REINVERSION_DESTINO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteReinversion_SelectReinversionSerieFondoDestino.sql");
	private final String SQL_UPDATE_FECHA_FINAL_REINVERSION = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteReinversion_UpdateFechaFinalReinversion.sql");
	private final String SQL_SELECT_REGIMEN_REINVERSION = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteReinversion_SelectRegimenDestino.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudAporteReinversion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<InSolicitudesAporte> p_SolicitudesAporte;
		
		public EntradaCrearSolicitudAporteReinversion() {
			p_SolicitudesAporte = new ArrayList<InSolicitudesAporte>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesAporte{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondoOrigen;
    	public String IdEmisorOrigen;
    	public BigDecimal CostoUF;
    	public String FechaOrigenInversion;
    	public BigDecimal MontoOrigen;
    	public String IndicadorMLT;
    	public String IdSerieFondo;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public String Visado;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String IdRegimenTributario;
    	public String Glosa;
    	public Long IdAutorizadoOrigen;
    	public Long FolioContrato;
    	public Long IdSolicitudAporteRescateRelacionado;
    	public String FechaEmision;
    	public BigDecimal Porcentaje;
		public Long IdAgrupador;
		public String FechaCertificacionRescateOrigen;
		public String FechaLiquidacionRescateOrigen;
		public String FechaValorCuotaRescateOrigen;
		public String FechaCertificaSpotCompra;
		public String FechaLiquidaSpotCompra;
		public String Switch;
		public String GeneraSpot;
		public BigDecimal MontoPatrimonio;
		public String Canal;
		public String IngresoAutomatico;
		public BigDecimal MontoTotalInversion;
		public Date FechaValorCuota;

		public String TipoReinversion = "";        // SWITCH   -  INTERNACIONAL -  NACIONAL  
		
		public InSolicitudesAporte() {
			this.Switch= "N";
			this.IdAgrupador = 0L;
			this.Porcentaje = new BigDecimal("100");
			this.GeneraSpot = "N";
			this.MontoPatrimonio = BigDecimal.ZERO;
			this.IngresoAutomatico = "N";
			this.MontoTotalInversion = BigDecimal.ZERO;
			this.FechaCertificacionRescateOrigen = "0001-01-01";
			this.FechaLiquidacionRescateOrigen = "0001-01-01";
			this.FechaValorCuotaRescateOrigen = "0001-01-01";
			this.CostoUF = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaRegimenReinversion{
		public Long IdSolicitudAporteRescate;
		public String IdRegimenTributario;
	}
	
	public static class SalidaCrearSolicitudAporteReinversion{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudAporteReinversion(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearSolicitudAporteReinversion jin = gson.fromJson(in.toString(), EntradaCrearSolicitudAporteReinversion.class);
		SalidaCrearSolicitudAporteReinversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudAporteReinversion logica(Connection con, EntradaCrearSolicitudAporteReinversion entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudAporteReinversion");
		Long inicio = System.currentTimeMillis();
		
		SalidaCrearSolicitudAporteReinversion out = new SalidaCrearSolicitudAporteReinversion();
		SQL sql=new SQL();
		String aporteGeneraCompraSpotDolar = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "APORTE_GENERA_COMPRA_SPOT_DOLAR");
		String idMonedaLocal = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		Boolean generaSpot = false;
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesAporte.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitudesAporte mov: entrada.p_SolicitudesAporte) {
			if(mov.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdInterno);
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String respetaValutasCargaMasiva = _c.NO;
		try {
			respetaValutasCargaMasiva = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
		}catch(Exception e) {
			log.write("ADVERTENCIA::CrearSolicitudAporteRescateNormal::No se encuentra parametro RespetaValutasCargaMasiva");
		}
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		this.procesarEnLotes(con, sql, entrada, out, fechaSistema, idMonedaLocal, idPaisEmpresa, respetaValutasCargaMasiva, generaSpot, aporteGeneraCompraSpotDolar);
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::CrearSolicitudAporteReinversion, tiempo:" + (fin - inicio));
		return out;
	}
	
	public SolicitudAporteRescate copiarSolicitudAporteRescate(SolicitudAporteRescate Solicitud) {
		SolicitudAporteRescate copiaSol = new SolicitudAporteRescate();
		copiaSol.IdInterno= Solicitud.IdInterno;
		copiaSol.IdSolicitudAporteRescate= Solicitud.IdSolicitudAporteRescate;
		copiaSol.IdTipoCanalIngreso= Solicitud.IdTipoCanalIngreso;
		copiaSol.FolioContrato= Solicitud.FolioContrato;
		copiaSol.IdCliente= Solicitud.IdCliente;
		copiaSol.IdPortafolio= Solicitud.IdPortafolio;
		copiaSol.TipoSolicitud= Solicitud.TipoSolicitud;
		copiaSol.IdSerieFondo= Solicitud.IdSerieFondo;
		copiaSol.IdTipoMovimientoFondoMutuo= Solicitud.IdTipoMovimientoFondoMutuo;
		copiaSol.IdDividendo= Solicitud.IdDividendo;
		copiaSol.IdSerieFondoDestino= Solicitud.IdSerieFondoDestino;
		copiaSol.IdEmisorDestino= Solicitud.IdEmisorDestino;
		copiaSol.IdClienteDestino= Solicitud.IdClienteDestino;
		copiaSol.IdPortafolioDestino= Solicitud.IdPortafolioDestino;
		copiaSol.IdSerieFondoOrigen= Solicitud.IdSerieFondoOrigen;
		copiaSol.IdEmisorOrigen= Solicitud.IdEmisorOrigen;
		copiaSol.MontoOrigen= Solicitud.MontoOrigen;
		copiaSol.IndicadorMLT= Solicitud.IndicadorMLT;
		copiaSol.Overnight= Solicitud.Overnight;
		copiaSol.IdSolicitudAporteRescateRelacionado= Solicitud.IdSolicitudAporteRescateRelacionado;
		copiaSol.IdTipoCartera= Solicitud.IdTipoCartera;
		copiaSol.CalculoPorMontoCuota= Solicitud.CalculoPorMontoCuota;
		copiaSol.FechaIngresoRegistro= Solicitud.FechaIngresoRegistro;
		copiaSol.FechaEmision= Solicitud.FechaEmision;
		copiaSol.FechaAceptacion= Solicitud.FechaAceptacion;
		copiaSol.FechaValorCuota= Solicitud.FechaValorCuota;
		copiaSol.FechaCertificacion= Solicitud.FechaCertificacion;
		copiaSol.FechaLiquidacion= Solicitud.FechaLiquidacion;
		copiaSol.FechaAnulacion= Solicitud.FechaAnulacion;
		copiaSol.FechaModificacion= Solicitud.FechaModificacion;
		copiaSol.FechaOriginalInversion= Solicitud.FechaOriginalInversion;
		copiaSol.EsPorMontoLiquido= Solicitud.EsPorMontoLiquido;
		copiaSol.Monto= Solicitud.Monto;
		copiaSol.NumeroCuotas= Solicitud.NumeroCuotas;
		copiaSol.Porcentaje= Solicitud.Porcentaje;
		copiaSol.ValorCuota= Solicitud.ValorCuota;
		copiaSol.CostoUF= Solicitud.CostoUF;
		copiaSol.CostoMonedaLocal= Solicitud.CostoMonedaLocal;
		copiaSol.ParidadFechaCertificacion= Solicitud.ParidadFechaCertificacion;
		copiaSol.ValorUFFechaInversion= Solicitud.ValorUFFechaInversion;
		copiaSol.ParidadFechaInversion= Solicitud.ParidadFechaInversion;
		copiaSol.IdMoneda= Solicitud.IdMoneda;
		copiaSol.IdMonedaPago= Solicitud.IdMonedaPago;
		copiaSol.MontoMonedaPago= Solicitud.MontoMonedaPago;
		copiaSol.ValidaSpot= Solicitud.ValidaSpot;
		copiaSol.GeneraSpot= Solicitud.GeneraSpot;
		copiaSol.MontoComision= Solicitud.MontoComision;
		copiaSol.ComisionInformada= Solicitud.ComisionInformada;
		copiaSol.MontoComisionLocal= Solicitud.MontoComisionLocal;
		copiaSol.MontoLiquido= Solicitud.MontoLiquido;
		copiaSol.MontoLiquidoLocal= Solicitud.MontoLiquidoLocal;
		copiaSol.MontoPago= Solicitud.MontoPago;
		copiaSol.MontoPagoLocal= Solicitud.MontoPagoLocal;
		copiaSol.MontoInversion= Solicitud.MontoInversion;
		copiaSol.NumeroCuotasInversion= Solicitud.NumeroCuotasInversion;
		copiaSol.ValorCuotaInversion= Solicitud.ValorCuotaInversion;
		copiaSol.CostoMonedaLocalInversion= Solicitud.CostoMonedaLocalInversion;
		copiaSol.Spread= Solicitud.Spread;
		copiaSol.Estado= Solicitud.Estado;
		copiaSol.EstadoLiquidacion= Solicitud.EstadoLiquidacion;
		copiaSol.MueveCta= Solicitud.MueveCta;
		copiaSol.FolioCartaLiquidacion= Solicitud.FolioCartaLiquidacion;
		copiaSol.IdMedioDePago= Solicitud.IdMedioDePago;
		copiaSol.Glosa= Solicitud.Glosa;
		copiaSol.IdEmpresa= Solicitud.IdEmpresa;
		copiaSol.IdUsuario= Solicitud.IdUsuario;
		copiaSol.IdModulo= Solicitud.IdModulo;
		copiaSol.OrigenOperacion= Solicitud.OrigenOperacion;
		copiaSol.SolicitudBatch= Solicitud.SolicitudBatch;
		copiaSol.IdRegimenTributario= Solicitud.IdRegimenTributario;
		copiaSol.TipoCarteraNormativa= Solicitud.TipoCarteraNormativa;
		copiaSol.Tributacion= Solicitud.Tributacion;
		copiaSol.IdRegimenTributarioDestino= Solicitud.IdRegimenTributarioDestino;
		copiaSol.TipoCarteraNormativaDestino= Solicitud.TipoCarteraNormativaDestino;
		copiaSol.AdministracionLiquidez= Solicitud.AdministracionLiquidez;
		copiaSol.Visado= Solicitud.Visado;
		copiaSol.IdPreferenciaPagoPortafolio= Solicitud.IdPreferenciaPagoPortafolio;
		copiaSol.IdAutorizadoOrigen= Solicitud.IdAutorizadoOrigen;
		copiaSol.IdAutorizadoDestino= Solicitud.IdAutorizadoDestino;
		copiaSol.IdMonedaSistema= Solicitud.IdMonedaSistema;
		copiaSol.MontoMonedaSistema= Solicitud.MontoMonedaSistema;
		copiaSol.Switch= Solicitud.Switch;
		copiaSol.EstadoAnulacion= Solicitud.EstadoAnulacion;
		copiaSol.FolioOmnibus= Solicitud.FolioOmnibus;
		copiaSol.FolioAgfOmnibus= Solicitud.FolioAgfOmnibus;
		copiaSol.IdEmpleadoEjecutivo= Solicitud.IdEmpleadoEjecutivo;
		copiaSol.IdEmpleadoEjecutivoDestino= Solicitud.IdEmpleadoEjecutivoDestino;
		copiaSol.IdPersona= Solicitud.IdPersona;
		copiaSol.IdInstrumento= Solicitud.IdInstrumento;
		copiaSol.IdTipoInstrumento= Solicitud.IdTipoInstrumento;
		copiaSol.EstadoProceso= Solicitud.EstadoProceso;
		copiaSol.IdIngresoEgreso= Solicitud.IdIngresoEgreso;
		copiaSol.IdOperacionSpot= Solicitud.IdOperacionSpot;
		copiaSol.IdCartera= Solicitud.IdCartera;
		copiaSol.IdAgrupador= Solicitud.IdAgrupador;
		copiaSol.IdAgrupadorInterno= Solicitud.IdAgrupadorInterno;
		copiaSol.RelacionTraspaso= Solicitud.RelacionTraspaso;
		copiaSol.IdPersonaEmisorDestino= Solicitud.IdPersonaEmisorDestino;
		copiaSol.RentabilidadReal= Solicitud.RentabilidadReal;
		copiaSol.IdMatrizAgrupador= Solicitud.IdMatrizAgrupador;
		copiaSol.IdMotivoRetiro= Solicitud.IdMotivoRetiro;
		copiaSol.IngresoAutomatico= Solicitud.IngresoAutomatico;
		copiaSol.ForzarSpot= Solicitud.ForzarSpot;
		copiaSol.RazonRetiro= Solicitud.RazonRetiro;
		copiaSol.TipoContratoOperacion= Solicitud.TipoContratoOperacion;
		copiaSol.FolioContratoOperacion= Solicitud.FolioContratoOperacion;
		copiaSol.IdPersonaCobro= Solicitud.IdPersonaCobro;
		copiaSol.IdCuentaBancaria= Solicitud.IdCuentaBancaria;
		copiaSol.IdBanco= Solicitud.IdBanco;
		copiaSol.IdTipoCuentaBancaria= Solicitud.IdTipoCuentaBancaria;
		copiaSol.NumeroCuentaBancaria= Solicitud.NumeroCuentaBancaria;
		copiaSol.NombreTipoCuentaBancaria= Solicitud.NombreTipoCuentaBancaria;
		copiaSol.IdPersonaTraspaso= Solicitud.IdPersonaTraspaso;
		copiaSol.SecuenciaRelacionada= Solicitud.SecuenciaRelacionada;

		
		return copiaSol;
	}
	
	public boolean calcularHeredarCostoUF(Connection con,InSolicitudesAporte sol,String codigoPortafolio,List<String> regimenesDestino,List<ConsultaRegimenReinversion>regimenSolicitudReinversion) {
		//VERDADERO PARA LOS QUE DEBE CALCULAR
		//FALSO PARA LOS QUE DEBE HEREDAR
		boolean calcula = true;
		
		try {
			boolean todosSon18Q = regimenesDestino.stream()
	                .allMatch(regimen -> regimen.equals("18Q")) &&
	                regimenSolicitudReinversion.stream()
	                        .allMatch(regimen -> regimen.IdRegimenTributario.equals("18Q"));

			if("APV-A".equalsIgnoreCase(codigoPortafolio)||"BF".equalsIgnoreCase(codigoPortafolio)||"DC-DL".equalsIgnoreCase(codigoPortafolio)||"DC-AL".equalsIgnoreCase(codigoPortafolio)||"DC-R".equalsIgnoreCase(codigoPortafolio)) {
				//es APV y hereda
				calcula=false;
			}else if("S".equalsIgnoreCase(sol.IndicadorMLT)&&todosSon18Q){
				//HEREDA
				calcula=false;
			}

		}catch(Exception er) {
			er.printStackTrace();
		}
		return calcula;
	}
	
	private void actualizarFechaFinalReinversion(SQL sql, Connection con, List<SolicitudAporteRescate> solicitudesInsertar) throws Exception {
		logger.debug("INICIO::CrearSolicitudAporteReinversion.actualizarFechaFinalReinversion");
		Long inicio = System.currentTimeMillis();
		
		List<Long> idSolicitudesAporteRescate = solicitudesInsertar.stream()
	            .map(solicitud -> solicitud.IdSolicitudAporteRescateRelacionado != null ? solicitud.IdSolicitudAporteRescateRelacionado : 0L)
				.distinct()
	            .collect(Collectors.toList());

		HashMap<String,Object> params = new HashMap<String,Object>();
		
		int batchSize = _c.MAX_REGISTROS;
		
		List<ReinversionSerieFondoDestino> reinversionSerieFondoDestinoActualizarFinal = new ArrayList<>();
		
		for(int i=0; i < idSolicitudesAporteRescate.size(); i += batchSize) {
			int fin = Math.min(i+ batchSize, idSolicitudesAporteRescate.size());
			List<Long> idSolicitudesAporteRescateBatch = new ArrayList<>(idSolicitudesAporteRescate.subList(i, fin));
			params.put("IdSolicitudAporteRescate", idSolicitudesAporteRescateBatch);
			List<ReinversionSerieFondoDestino> reinversionSerieFondoDestinoActualizar = sql.query(con, SQL_SELECT_REINVERSION_DESTINO, params, ReinversionSerieFondoDestino.class);
			reinversionSerieFondoDestinoActualizarFinal.addAll(reinversionSerieFondoDestinoActualizar);
		}
		
		if(!reinversionSerieFondoDestinoActualizarFinal.isEmpty()) {
			sql.batch(con, SQL_UPDATE_FECHA_FINAL_REINVERSION, reinversionSerieFondoDestinoActualizarFinal, ReinversionSerieFondoDestino.class);
		}
			
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearSolicitudAporteReinversion.actualizarFechaFinalReinversion, tiempo:" + (fin - inicio));
		return;
	}
	
	private void procesarEnLotes(Connection con, SQL sql, EntradaCrearSolicitudAporteReinversion entrada, SalidaCrearSolicitudAporteReinversion salida, String fechaSistema, String idMonedaLocal, Long idPaisEmpresa, String respetaValutasCargaMasiva, Boolean generaSpot, String aporteGeneraCompraSpotDolar) throws Exception {
		logger.trace("INICIO::CrearSolicitudAporteReinversion.procesarEnLotes");
		Long inicio = System.currentTimeMillis();
		
    	int batchSize = _c.MAX_REGISTROS;
    	Boolean detieneEjecucion = true;
    	List<InSolicitudesAporte> respaldoSolicitudes = new ArrayList<>(entrada.p_SolicitudesAporte);
    	
    	int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));
        
        Date fechaMaximaCertificacion = Fecha.toDate("0001-01-01");

    	for (int i = 0; i < respaldoSolicitudes.size(); i += batchSize) {
        	int fin = Math.min(i + batchSize, respaldoSolicitudes.size());
        	entrada.p_SolicitudesAporte = new ArrayList<>(respaldoSolicitudes.subList(i, fin));
        	logger.info(String.format("Procesando lote de %s hasta %s, de %s", String.valueOf(i),String.valueOf(fin), String.valueOf(respaldoSolicitudes.size())));
        	detieneEjecucion = this.procesarSolicitudes(con, sql, entrada, salida, fechaSistema, idMonedaLocal, idPaisEmpresa, respetaValutasCargaMasiva, generaSpot, aporteGeneraCompraSpotDolar, fechaHoraUTC, fechaMaximaCertificacion);
        	if(detieneEjecucion) {
        		break;
        	}
    	}

    	Long fin = System.currentTimeMillis();
    	logger.trace("FIN::CrearSolicitudAporteReinversion.procesarEnLotes, tiempo:" + (fin - inicio));
	}
	
	private Boolean procesarSolicitudes(Connection con, SQL sql, EntradaCrearSolicitudAporteReinversion entrada, SalidaCrearSolicitudAporteReinversion salida, String fechaSistema, String idMonedaLocal, Long idPaisEmpresa, String respetaValutasCargaMasiva, Boolean generaSpot, String aporteGeneraCompraSpotDolar, Timestamp fechaHoraUTC, Date fechaMaximaCertificacion) throws Exception {
		logger.info("INICIO::CrearSolicitudAporteReinversion.procesarSolicitudes");
		Long inicio = System.currentTimeMillis();
		
		int countTransaccionesBatch = 0;
		List<String> series = new ArrayList<String>();
		List<String> seriesOrigenDestino = new ArrayList<String>();
		List<Long> clientes = new ArrayList<Long>();
		List<String> regimen = new ArrayList<String>();
		List<Long> portafolios = new ArrayList<Long>();
		List<String> monedas = new ArrayList<String>();
		List<String> idTipoMovimientos = new ArrayList<String>();
		idTipoMovimientos.add(_c.AporteReinversion);
		
		for(InSolicitudesAporte mov : entrada.p_SolicitudesAporte){
			if(_c.SI.equalsIgnoreCase(mov.SolicitudBatch)){
				countTransaccionesBatch++;
			}
			
			if(!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
			
			if(!seriesOrigenDestino.contains(mov.IdSerieFondoOrigen)) {				
				seriesOrigenDestino.add(mov.IdSerieFondoOrigen);
			}			
			if(!seriesOrigenDestino.contains(mov.IdSerieFondo)) {
				seriesOrigenDestino.add(mov.IdSerieFondo);				
			}
			
			if(!clientes.contains(mov.IdCliente)) clientes.add(mov.IdCliente);
			if(!portafolios.contains(mov.IdPortafolio)) portafolios.add(mov.IdPortafolio);
			if(!regimen.contains(mov.IdRegimenTributario)) regimen.add(mov.IdRegimenTributario);
		}
		
		ObtenerPerfilFondoMutuo obtenerPerfilFondoMutuo = new ObtenerPerfilFondoMutuo();
		EntradaObtenerPerfilFondoMutuo entradaObtenerPerfilFondoMutuo = new EntradaObtenerPerfilFondoMutuo();
		entradaObtenerPerfilFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
		SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondoMutuo.logica(con, entradaObtenerPerfilFondoMutuo);
		
		if(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo == null && countTransaccionesBatch != entrada.p_SolicitudesAporte.size() && entrada.p_SolicitudesAporte.get(0).FechaEmision != null){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return true;
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", seriesOrigenDestino);
		List<SerieFondoSolicitudes> seriesSolicitudesOrigenDestino = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		params.clear();
		params.put("IdPortafolio", portafolios);
		List<ClientePortafolioSolicitudes> clientesSolicitudes = sql.query(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolioSolicitudes.class);
		params.clear();
		params.put("IdTipoMovimientoFondoMutuo", idTipoMovimientos);
		List<TipoMovimientoFondoMutuo> tipoMovimientos = sql.query(con, SQL_SELECT_TIPO_MOVIMIENTO, params, TipoMovimientoFondoMutuo.class);
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		params.put("IdRegimenTributario", regimen);
		List<RegimenSerieDeFondo> regimenesSeries = sql.query(con, SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class);
		params.clear();
		params.put("IdPortafolio", portafolios);
		params.put("CodigoTipoIndicador", _c.UsaCuentaInversion);
		List<IndicadoresPortafolio> indicadoresPortafolio = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, params, IndicadoresPortafolio.class);
		params.clear();
		params.put("IdPortafolio", portafolios);
		List<IndicadoresPortafolioFondoMutuo> indicadoresPortafolioFFMM = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM, params, IndicadoresPortafolioFondoMutuo.class);
		params.clear();
		params.put("IdRegimenFondoMutuo", regimen);
		List<ConfiguracionPropositoPortafolio> configuracionesRegimenPortafolio = sql.query(con, SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO, params, ConfiguracionPropositoPortafolio.class);
		params.clear();
		
		for(SerieFondoSolicitudes serie : seriesSolicitudes){
			if(!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
		}
		params.put("IdMoneda", monedas);
		List<Moneda> monedasFondo = sql.query(con, SQL_SELECT_MONEDA, params, Moneda.class);
		params.clear();
		
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		
		HashMap<String,Object> hseriesOrigenDestino = Utils.List2Hash(seriesSolicitudesOrigenDestino, "IdSerieFondo");
		
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
		HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
		HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
		HashMap<Long,Object> hindicadoresPortafolio =  Utils.List2Hash(indicadoresPortafolio, "IdPortafolio");
		HashMap<Long,Object> hindicadoresPortafolioFFMM =  Utils.List2Hash(indicadoresPortafolioFFMM, "IdPortafolio");
		HashMap<String,Object> hmonedasFondo =  Utils.List2Hash(monedasFondo, "IdMoneda");
		HashMap<String,Object> hconfRegimenPortafolio =  Utils.List2Hash(configuracionesRegimenPortafolio, "Id");
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte) {
			
			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 1L;
			calculoEntrada.IdInterno = solicitud.IdInterno;
			calculoEntrada.Fecha = fechaSistema;
			calculoEntrada.IdMonedaOrigen = solicitud.IdMoneda;
			calculoEntrada.IdMonedaDestino = idMonedaLocal;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen = BigDecimal.ZERO.compareTo(solicitud.MontoTotalInversion) != 0 ? solicitud.MontoTotalInversion : solicitud.Monto;
			calculoEntrada.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
		}
		
		CalcularParidadSalida calculoMontoMonedaPago = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hashMontoTotalInversionMonedaSistema = Utils.List2Hash(calculoMontoMonedaPago.p_out_CalculosSalida, "IdInterno");
		
		CalcularFechaSolicitudAporteRescate calculoFechas = new CalcularFechaSolicitudAporteRescate();
		EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
		entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFechas.p_IdPais = idPaisEmpresa;
		
		// ------------------------------------------------------------------------------------------
		// DETERMINA EL TIPO DE REINVERSION (De acuerdo a Fondo Origen Destino)
		//  	- SWITCH		: Reinversiones entre fondos extranjeros de un mismo emisor (Ej: USD > USD  mismo emisor)
		//  	- INTERNACIONAL : Reinversiones entre fondos extranjeros de distintos emisores (Ej: USD > USD distinto emisor)
		//  	- NACIONAL      : Reinversion de un fondo extranjo a uno nacional (Ej: USD > CLP)
		//  	- NACIONAL      : Reinversion de un fondo nacional a cualquier otro (Ej:  CLP > *)
		// ------------------------------------------------------------------------------------------
		 
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){										
			SerieFondoSolicitudes serieOrigen   = (SerieFondoSolicitudes) hseriesOrigenDestino.get(solicitud.IdSerieFondoOrigen);
			SerieFondoSolicitudes serieDestino  = (SerieFondoSolicitudes) hseriesOrigenDestino.get(solicitud.IdSerieFondo);
							
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S")  && serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
				solicitud.TipoReinversion = "SWITCH";
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S")  && !serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
				solicitud.TipoReinversion = "INTERNACIONAL";
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("N"))
				solicitud.TipoReinversion = "NACIONAL";
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("N"))
				solicitud.TipoReinversion = "NACIONAL";
			if (serieOrigen.EmisorFondoNacionalDolar.equalsIgnoreCase("S") && serieDestino.EmisorFondoNacionalPesos.equalsIgnoreCase("S"))
				solicitud.TipoReinversion = "VALUTASNACUSD";
			if (serieOrigen.EmisorFondoNacionalPesos.equalsIgnoreCase("S") && serieDestino.EmisorFondoNacionalDolar.equalsIgnoreCase("S"))
				solicitud.TipoReinversion = "VALUTASNACUSD";
			if (serieOrigen.EmisorFondoInternacionalDolar.equalsIgnoreCase("S") && serieDestino.EmisorFondoNacionalDolar.equalsIgnoreCase("S"))
				solicitud.TipoReinversion = "VALUTASNACUSD";
		}
		
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			SerieFondoSolicitudes serieOrigen = (SerieFondoSolicitudes) hseriesOrigenDestino.get(solicitud.IdSerieFondoOrigen);
			CalcularFechaSolicitudAporteRescate.InFechas fecha = new CalcularFechaSolicitudAporteRescate.InFechas();
			if(serie != null){
				fecha.IdInterno = solicitud.IdInterno;
				fecha.IdSerieFondo = solicitud.IdSerieFondo;
				fecha.FechaEmision = solicitud.FechaEmision == null ? Fecha.stringToSqlDate(serie.FechaFondo) : solicitud.FechaEmision;
				if(_c.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && _c.NO.equals(respetaValutasCargaMasiva)){
					fecha.EstadoFondo = _c.Abierto;
					fecha.EstadoLiquidacion = _c.Abierto;
					fecha.NumeroDiasRetencion = 0L;
					fecha.NumeroValutaValorCuota = 0L;
					fecha.NumeroValutaCertificacion = 0L;
					fecha.NumeroValutaCurse = 0L;
					fecha.SolicitudBatch = _c.SI;
				}else{
					fecha.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
					
					fecha.NumeroDiasRetencion = solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") ? 2 : (solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL") ? 4 : serie.DiasRetencionesAporte);
					fecha.NumeroValutaValorCuota = (solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ? 1 : serie.DiasValutaValorCuotaAporte;
					fecha.NumeroValutaCertificacion = (solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ?  2 : serie.DiasValutaCertificacionAporte;
					fecha.NumeroValutaCurse = solicitud.TipoReinversion.equalsIgnoreCase("VALUTASNACUSD") ? serie.DiasValutaCurseAporte : ((solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ? 0 : serie.DiasValutaCurseAporte);
					fecha.EstadoLiquidacion = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
					fecha.Switch = solicitud.Switch;
					fecha.FechaCertificacionRescateOrigen = solicitud.FechaCertificacionRescateOrigen;
					fecha.FechaLiquidacionRescateOrigen = solicitud.FechaLiquidacionRescateOrigen;
					fecha.FechaValorCuotaRescateOrigen = solicitud.FechaValorCuotaRescateOrigen;
					
					//SIEMPRE EL FONDO ESTA ABIERTO PARA BATCH.
					if(_c.SI.equals(solicitud.SolicitudBatch)) {
						fecha.EstadoFondo = _c.Abierto;
						fecha.EstadoLiquidacion = _c.Abierto;
					}
					fecha.SolicitudBatch = _c.NO;
				}
				
				fecha.TipoSolicitud = _c.TipoSolicitudAporte;
				fecha.FondoExtranjero = serie.FondoExtranjero;
				fecha.IdPaisFondo = serie.IdPaisOrigen;
				fecha.IdPaisEmisor = serie.IdPaisEmisor;
				Moneda moneda = (Moneda) hmonedasFondo.get(serie.IdMoneda);
				fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
				fecha.FondoNacionalDolar = serie.FondoNacionalDolar;
				fecha.IdMercadoFondoMutuo = serie.IdMercadoFondoMutuo;
				entradaFechas.p_SolicitudAporteRescate.add(fecha);
			}
		}
		
		SalidaCalcularFechaSolicitudAporteRescate salidaFechas = calculoFechas.logica(con, entradaFechas);
		
		HashMap<Long,Object> hfechas =  Utils.List2Hash(salidaFechas.P_FECHASAPORTERESCATE, "IdInterno");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		
		HashMap<String,Object> paramsRegimen = new HashMap<String,Object>();
		List<Long> idsRescates = new ArrayList<Long>();
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			serie.IdInterno = solicitud.IdInterno;
			/*abria que pasarle la fecha de valor cuota, esta cayendo en el else */
			if (solicitud.FechaValorCuota != null) {
				serie.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			} else if (fecha != null) {
				serie.Fecha = Fecha.stringToSqlDate(fecha.FechaValorCuota);
			} else {
				serie.Fecha = fechaSistema;
			}
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = _c.CalculoPorMonto;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud =  _c.TipoSolicitudAporte;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		 	if(!idsRescates.contains(solicitud.IdSolicitudAporteRescateRelacionado))
		 		idsRescates.add(solicitud.IdSolicitudAporteRescateRelacionado);
		}
		
		paramsRegimen.put("IdSolicitudAporteRescate",idsRescates);
		List<ConsultaRegimenReinversion> regimenSolicitudReinversion = sql.query(con,SQL_SELECT_REGIMEN_REINVERSION, paramsRegimen, ConsultaRegimenReinversion.class);

		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesAporte.size()));
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		HashMap<Long,Object> listaEntrada = Utils.List2Hash(entrada.p_SolicitudesAporte, "IdInterno");
		
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		ValidaPatrimonioEmisor validaPatrimonioEmisor = new ValidaPatrimonioEmisor();
		EntradaValidaPatrimonioEmisor entradaValidaPatrimonioEmisor = new EntradaValidaPatrimonioEmisor();
		entradaValidaPatrimonioEmisor.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidaPatrimonioEmisor.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidaPatrimonioEmisor.p_Fecha = fechaSistema;
		int i = 0;
		List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		
		HashMap<String,BigDecimal> hashMontosPortafolioEmisor = new HashMap<String,BigDecimal>();
		
		List<Long> folioContratoSolicitudes = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", (long) entrada.p_SolicitudesAporte.size());

        for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			generaSpot = false;
			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			TipoMovimientoFondoMutuo tipoMovimiento = (TipoMovimientoFondoMutuo) htipoMovimiento.get(_c.AporteReinversion);
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			RegimenSerieDeFondo r = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
			if(cliente != null) confPropositoPortafolio = (ConfiguracionPropositoPortafolio) hconfRegimenPortafolio.get(solicitud.IdRegimenTributario + "_" + cliente.Proposito);
			
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			IndicadoresPortafolio indicadorPortafolio = (IndicadoresPortafolio) hindicadoresPortafolio.get(solicitud.IdPortafolio);
			
			SolicitudAporteRescate sol = new SolicitudAporteRescate();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			
			if((solicitud.IdMoneda.equals(_c.Pesos) && serie.IdMoneda.equals(_c.Dolar) && aporteGeneraCompraSpotDolar.equals(_c.SI)) 
			|| (solicitud.IdMoneda.equals(_c.Dolar) && serie.IdMoneda.equals(_c.Pesos) && aporteGeneraCompraSpotDolar.equals(_c.SI))) {
				generaSpot = true;
				sol.IdMonedaPago = solicitud.IdMoneda;
			}else {
				sol.IdMonedaPago = serie.IdMoneda;
			}
				
			//SE COMPLETA EL MOVIMIENTO
			sol.IdInterno = solicitud.IdInterno;
			sol.IdSolicitudAporteRescate = secuencias.get(i);
		 	sol.IdCliente = solicitud.IdCliente;
		 	sol.IdPortafolio = solicitud.IdPortafolio;
		 	sol.TipoSolicitud = _c.TipoSolicitudAporte;
		 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
		 	sol.IdTipoMovimientoFondoMutuo = _c.AporteReinversion;
		 	sol.IdDividendo = 0L;
			sol.IdSerieFondoDestino = "";
			sol.IdEmisorDestino = "";
			sol.IdClienteDestino = 0L;
			sol.IdPortafolioDestino = 0L;
			sol.IndicadorMLT = solicitud.IndicadorMLT;
			sol.Overnight = _c.NO;
			sol.IdSerieFondoOrigen = solicitud.IdSerieFondoOrigen;
			sol.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado == null ? 0L : solicitud.IdSolicitudAporteRescateRelacionado;
			sol.TipoCarteraNormativa = r != null ? r.TipoCarteraNormativa : "";
			sol.IdTipoCartera = "";
			sol.CalculoPorMontoCuota = _c.CalculoPorMonto;
			sol.FechaEmision = Fecha.toDate(fechaSistema);
			sol.FechaOriginalInversion = Fecha.toDate(solicitud.FechaOrigenInversion);
			sol.FechaIngresoRegistro = fechaHoraUTC;
			sol.IdMoneda = generaSpot ? serie.IdMoneda : solicitud.IdMoneda;
			sol.Estado =  _c.EstadoIngresado;
			sol.IdMedioDePago = solicitud.IdMedioDePago;
			sol.Glosa = solicitud.Glosa;
			sol.IdEmpresa = entrada.p_IdEmpresa;
			sol.IdUsuario = entrada.p_IdUsuario;
			sol.IdModulo = entrada.p_IdModulo;
			sol.IdEmisorOrigen = solicitud.IdEmisorOrigen;
			sol.MontoOrigen = solicitud.MontoOrigen;
			sol.SolicitudBatch = solicitud.SolicitudBatch;
			sol.OrigenOperacion = solicitud.OrigenOperacion;
			sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
			sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null  ? cliente.IdEmpleadoEjecutivo : 0L;
			sol.IdRegimenTributario = solicitud.IdRegimenTributario;
			sol.IngresoAutomatico = solicitud.IngresoAutomatico;
			sol.EsPorMontoLiquido = _c.NO;
			sol.IdTipoCanalIngreso = solicitud.Canal != null ? solicitud.Canal : entrada.p_IdTipoCanalIngreso;
			sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : folioContratoSolicitudes.get(entrada.p_SolicitudesAporte.indexOf(solicitud));
			sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
			sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
			sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
			sol.Visado = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) && _c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) ? _c.NO : solicitud.Visado; 
			sol.IdPreferenciaPagoPortafolio = 0L;
			sol.EstadoProceso = _c.EstadoPendiente;
			sol.Monto = BigDecimal.ZERO;
			sol.NumeroCuotas = BigDecimal.ZERO;
			sol.ValorCuota = BigDecimal.ZERO;
		    sol.CostoMonedaLocal = BigDecimal.ZERO; 
			sol.ValorUFFechaInversion = BigDecimal.ZERO; 
			sol.ParidadFechaInversion = BigDecimal.ZERO;
			sol.IdMonedaSistema = "";
			sol.MontoMonedaSistema = BigDecimal.ZERO;
			sol.MontoPagoLocal = BigDecimal.ZERO; 
			sol.MontoPago = BigDecimal.ZERO;
			sol.MontoComision = BigDecimal.ZERO;
			sol.MontoComisionLocal = BigDecimal.ZERO;
			sol.IdAgrupador = solicitud.IdAgrupador;
			sol.Switch = solicitud.Switch;
			if(fecha != null) {
				sol.FechaAceptacion = fecha.FechaAceptacion;
			    sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : fecha.FechaValorCuota;
			    sol.FechaCertificacion = fecha.FechaCertificacion;
			    sol.FechaLiquidacion = fecha.FechaLiquidacion;
			}
			if(vc != null){
				sol.Monto = vc.Monto;
				sol.NumeroCuotas = vc.NumeroCuotas;
				sol.ValorCuota = vc.ValorCuota;
			    sol.CostoMonedaLocal = generaSpot ? vc.Monto : vc.MontoDestino; 
				sol.ValorUFFechaInversion = vc.ValorFactorAjuste; 
				sol.ParidadFechaInversion = vc.ValorParidad;
				sol.IdMonedaSistema = vc.IdMonedaSistema;
				sol.MontoMonedaSistema = vc.MontoMonedaSistema;
				sol.MontoComision = BigDecimal.ZERO;
				sol.MontoComisionLocal = BigDecimal.ZERO;
				sol.MontoPagoLocal = generaSpot ? vc.Monto : vc.MontoDestino;
				sol.MontoPago = vc.Monto;
				sol.MontoInversion = vc.Monto;
				sol.CostoMonedaLocalInversion = generaSpot ? vc.Monto : vc.MontoDestino;
				sol.ValorCuotaInversion = vc.ValorCuota;
				sol.ParidadFechaCertificacion = vc.ValorParidad;
				sol.NumeroCuotasInversion = vc.NumeroCuotas;
				//SE DEBE DEFINIR PARA LOS APORTES POR REINVERSION, SI EL APORTE DEBE HEREDAR O CALCULAR EL COSTOUF A LA FECHA DE VALOR CUOTA DEL APORTE.
				ClientePortafolioSolicitudes codigoPortafolio = (ClientePortafolioSolicitudes)hclientes.get(sol.IdPortafolio);
				if(calcularHeredarCostoUF(con,solicitud,codigoPortafolio.CodigoPortafolio,regimen,regimenSolicitudReinversion )) {
					sol.CostoUF = vc.CostoEnFactorAjuste;
					sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
				}else {
					sol.CostoUF =  solicitud.CostoUF;
					sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
				}
			}
			//cambios si hay que hacer spot
			if(generaSpot) {
				sol.IdMoneda = serie.IdMoneda;
				sol.GeneraSpot = "S";
			}
			else
				sol.GeneraSpot = "N";
			
			if(r != null){
				switch(r.GrupoRegimen){
					case "APV":
						sol.Tributacion = _c.TributacionAPV;
						break;
					case "ART108":
						sol.Tributacion = _c.TributacionART108;
						break;
					case "ART107":
						sol.Tributacion = _c.TributacionART107;
						break;
					case "57BIS":
						sol.Tributacion = _c.Tributacion57BIS;
						break;
					default:
						sol.Tributacion = _c.TributacionDefecto;
						break;
				}
			}
			
			sol.EstadoLiquidacion = solicitud.Switch.equalsIgnoreCase("S") ? "LIQ" : "ING";
			
			solicitudesProcesar.add(sol);
			
			//VALIDACIONES
			solVal.IdInterno = sol.IdInterno;
			solVal.IdCliente = sol.IdCliente;
			solVal.IdPortafolio = sol.IdPortafolio;
			solVal.IdSerieFondo = sol.IdSerieFondo;
			solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
			solVal.Monto = sol.Monto;
			solVal.NumeroCuotas = sol.NumeroCuotas;
			solVal.IdEstadoSerie = "";
			solVal.EsDiferido = _c.SI;
			solVal.EsMontoMaximo = _c.SI;
			solVal.EsMontoMinimo = _c.SI;
			solVal.PermiteCambioGrupo = _c.SI;
			solVal.EsPortafolioValido = _c.SI;
			solVal.PermiteRescateMandato = _c.SI;
			solVal.EsCuentaInversionValido = _c.SI;
			solVal.EsFechaValidaAporte = _c.SI;
			solVal.EsFechaValidaRescate = _c.SI;
			solVal.ValorCuota = sol.ValorCuota;
			solVal.Overnight = _c.SI;
			solVal.EsMoneyMarket = _c.SI;
			solVal.EsFamiliaEquivalente = _c.SI;
			solVal.EsPortafolioRegimenValido = _c.SI;
			solVal.EsRegimenValido = _c.SI;
			solVal.EsCuentaInversionValido = _c.SI;
			solVal.PermiteAporte = _c.SI;
			solVal.EsRegimenDestinoValido = _c.SI;
			if(serie != null){
				solVal.IdEstadoSerie = serie.IdEstado;
				solVal.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
				solVal.EstadoBloqueo = serie.BloqueoAporte;
				solVal.EsMontoMaximo = serie.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && (serie.MontoMaximo.compareTo(sol.Monto) == -1 && serie.MontoMaximo.compareTo(solicitud.MontoTotalInversion) == -1) ? _c.NO : _c.SI;
				if(r != null && _c.SI.equals(r.ValidaMontoMinimoInstrumento)) {
					solVal.EsMontoMinimo = serie.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && (serie.MontoMinimo.compareTo(sol.Monto) == 1 && serie.MontoMinimo.compareTo(solicitud.MontoTotalInversion) == 1) ? _c.NO : _c.SI;
				}
				solVal.EstadoLiquidez = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
				solVal.EsFondoValido = _c.SI;
				if(!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && _c.NO.equalsIgnoreCase(serie.Diferido)){
					solVal.EsDiferido = _c.NO;
				}
				//PATRIMONIOS ALTOS
				BigDecimal montoPortafolioPorEmisor = hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + serie.IdEmisor);
				if(montoPortafolioPorEmisor == null) montoPortafolioPorEmisor = BigDecimal.ZERO;
				montoPortafolioPorEmisor = montoPortafolioPorEmisor.add(sol.MontoMonedaSistema, MathContext.DECIMAL128);
				hashMontosPortafolioEmisor.put(sol.IdCliente + "_" + serie.IdEmisor, montoPortafolioPorEmisor);
				if(_c.SI.equals(serie.PatrimonioAltos)) {
					ValidaPatrimonioEmisor.Solicitud valPatrimonio = new ValidaPatrimonioEmisor.Solicitud();
					valPatrimonio.IdInterno = sol.IdInterno;
					valPatrimonio.IdCliente = sol.IdCliente;
					valPatrimonio.IdSerieFondo = serie.IdSerieFondo;
					valPatrimonio.IdEmisor = serie.IdEmisor;
					valPatrimonio.MontoPatrimonio = solicitud.MontoPatrimonio;
					valPatrimonio.ValorCuota = sol.ValorCuota;
					entradaValidaPatrimonioEmisor.p_Solicitudes.add(valPatrimonio);
				}
			}
			
			if(r == null){
				solVal.EsRegimenValido = _c.NO;
			}else {
				solVal.PermiteAporte = r.PermiteAporte;
			}
			
			if(confPropositoPortafolio == null) solVal.EsPortafolioRegimenValido = _c.NO;
			
			if(_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) && _c.NO.equalsIgnoreCase(indicadorPortafolio.ValorIndicador)){
				solVal.EsCuentaInversionValido = _c.NO;
			}
			
			if(cliente != null){
				portVal.IdInterno = sol.IdInterno;
				portVal.IdPortafolio = sol.IdPortafolio;
				portVal.IdPortafolioReal = cliente.IdPortafolio;
				portVal.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
				portVal.IdCliente = sol.IdCliente;
				portVal.IdClienteReal = cliente.IdCliente;
				portVal.IdEstadoCliente = cliente.IdEstadoCliente;
				portVal.DocumentoIdentidad = cliente.DocumentoIdentidad;
				portVal.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
				portVal.IdClienteDestino = sol.IdClienteDestino;
				portVal.IdClienteRealDestino = 0L;
				portVal.IdPortafolioDestino = sol.IdPortafolio;
				portVal.IdPortafolioRealDestino = 0L;
				portVal.IdEstadoClienteDestino = "";
				portVal.IdEstadoPortafolioDestino = "";
			}
			
			solVal.IdTipoOperacion=_c.TipoOperacionAporteFondoMutuo;
			solVal.IdTipoProducto=_c.TipoOperacion;
			
			//Para portafolios con regimen BF (Bonificacion Fiscal) no se debe validar el monto Minimo para ingreso ni certificacion
			if (cliente.CodigoPortafolio.equalsIgnoreCase(_c.BonificacionFiscal))
			{
				solVal.EsMontoMinimo = _c.SI;
			}
			
			entradaCumpleRequisitos.p_Portafolios.add(portVal);
			entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
			
			//SE PREPARA SALIDA
			ResultadoSolicitud solicitudSalida = new ResultadoSolicitud();
			solicitudSalida = Utils.copyPojo(sol, solicitudSalida);
			if(serie != null){
				solicitudSalida.IdFondo = serie.IdFondo;
				solicitudSalida.Nemotecnico = serie.Nemotecnico;
				solicitudSalida.NombreFondo = serie.NombreFondo;
				solicitudSalida.NombreCorto = serie.NombreCortoTipoFondo;
				solicitudSalida.NombreSerieFondo = serie.NombreSerieFondo;
				solicitudSalida.IdEmisorDestino = serie.IdEmisor;
				solicitudSalida.CodigoSerie = serie.CodigoSerie;
			}
			if(cliente != null){
				solicitudSalida.DocumentoIdentidad = cliente.DocumentoIdentidad;
				solicitudSalida.CodigoPortafolio = cliente.CodigoPortafolio;
				solicitudSalida.NombreCliente = cliente.NombreCompleto;
			}
			solicitudSalida.IdSerieFondoOrigen = solicitud.IdSerieFondoOrigen;
			solicitudSalida.IdEmisorOrigen = solicitud.IdEmisorOrigen;
			solicitudSalida.MontoTransferencia = sol.Monto;
			solicitudSalida.NumeroCuotaTransferencia = sol.NumeroCuotas;
			solicitudSalida.FechaAceptacion = Fecha.stringToSqlDate(sol.FechaAceptacion);
			solicitudSalida.FechaOriginalInversion = Fecha.stringToSqlDate(sol.FechaOriginalInversion);
			solicitudSalida.FechaIngresoRegistro = Fecha.toStringTimeStamp(sol.FechaIngresoRegistro);
			solicitudSalida.FechaLiquidacion = Fecha.stringToSqlDate(sol.FechaLiquidacion);
			solicitudSalida.FechaCertificacion = Fecha.stringToSqlDate(sol.FechaCertificacion);
			solicitudSalida.FechaValorCuota = Fecha.stringToSqlDate(sol.FechaValorCuota);
			resultadoTemp.add(solicitudSalida);
			
			i++;
		}
        
        HashMap<Long,Object> hSolicitudSalida = Utils.List2Hash(resultadoTemp, "IdInterno");
		
		int registrosPorProcesar = 0;
		SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate salidaCumpleRequisitos = cumpleRequisitos.logica(con, entradaCumpleRequisitos);
		for(MensajeSalida mje : salidaCumpleRequisitos.P_OUT_MENSAJES){
			salida.P_OUT_MENSAJES.add(mje);
			if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
				salida.P_OUT_ERRORESCONTADOR++;
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if(sol.IdInterno.equals(Math.abs(mje.id))){
						sol.EstadoProceso = _c.EstadoRechazado;
						break;
					}
				}
			}
		}
		
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
				registrosPorProcesar++;
			}
		}
		
		if(registrosPorProcesar == 0){
			return false;
		}
		
		//VALIDACION INVERSIONES
		ValidaMontoInversion validaMontoInversion = new ValidaMontoInversion();
		EntradaValidaMontoInversion entradaValidaMontoInversion  = new EntradaValidaMontoInversion();
		entradaValidaMontoInversion.p_IdEmpresa=entrada.p_IdEmpresa;
		entradaValidaMontoInversion.p_IdUsuario=entrada.p_IdUsuario;
		entradaValidaMontoInversion.p_Solicitudes= new ArrayList<ValidaMontoInversion.Solicitud>();
		
		entradaValidaMontoInversion.p_Solicitudes = solicitudesProcesar.parallelStream().map(solicitud->{
			SerieFondoSolicitudes serieFondoSolicitud = (SerieFondoSolicitudes)hseries.get(solicitud.IdSerieFondo);
			RegimenSerieDeFondo regimenSerieFondo = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			CalculoSalida montoMonedaSistema = (CalculoSalida) hashMontoTotalInversionMonedaSistema.get(solicitud.IdInterno);
			ValidaMontoInversion.Solicitud solicitudAValidar = new ValidaMontoInversion.Solicitud();
			solicitudAValidar.IdInterno = solicitud.IdInterno;
			solicitudAValidar.FolioContrato = 0L;
			solicitudAValidar.AportesAltos = serieFondoSolicitud.AportesAltos;
			solicitudAValidar.Monto =  montoMonedaSistema != null ? montoMonedaSistema.MontoDestino : BigDecimal.ZERO;
			solicitudAValidar.ValidaMontoMinimo = regimenSerieFondo != null ? regimenSerieFondo.ValidaMontoMinimoInstrumento : _c.SI;
			return solicitudAValidar;
		}).collect(Collectors.toList());
		
		SalidaValidaMontoInversion salidaValidaMontoInversion = validaMontoInversion.logica(con, entradaValidaMontoInversion);
		
		for(MensajeSalida mje : salidaValidaMontoInversion.P_OUT_MENSAJES){
			salida.P_OUT_MENSAJES.add(mje);
			if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
				salida.P_OUT_ERRORESCONTADOR++;
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if(sol.IdInterno.equals(mje.id)){
						sol.EstadoProceso = _c.EstadoRechazado;
						break;
					}
				}
			}
		}
		
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && sol.FechaCertificacion.compareTo(fechaMaximaCertificacion) == 1){
				fechaMaximaCertificacion = sol.FechaCertificacion;
			}
		}
		
		//PATROMINO EMISOR
		if(entradaValidaPatrimonioEmisor.p_Solicitudes.size() > 0) {
			entradaValidaPatrimonioEmisor.p_Solicitudes.parallelStream().forEach(sol -> {
				BigDecimal montoPorEmisor = hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + sol.IdEmisor);
				if(montoPorEmisor != null) sol.MontoPatrimonio = sol.MontoPatrimonio.add(montoPorEmisor, MathContext.DECIMAL128);
			});
			SalidaValidaPatrimonioEmisor salidaValidaPatrimonioEmisor = validaPatrimonioEmisor.logica(con, entradaValidaPatrimonioEmisor);
			for(MensajeSalida mje : salidaValidaPatrimonioEmisor.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_MENSAJES.add(mje);
					for(SolicitudAporteRescate sol : solicitudesProcesar){
						if(sol.IdInterno.equals(mje.id)){
							sol.EstadoProceso = _c.EstadoRechazado;
							break;
						}
					}
				}
			}
		}
		
		//LLAMAMOS FLUJO CAJA
		List<Long> portFlujo = new ArrayList<Long>();
		List<String> monedaFlujo = new ArrayList<String>();
		HashMap<String,BigDecimal> portTotalesMontos = new HashMap<String,BigDecimal>();
		SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
		SaldoFlujoCaja.ServiceEntrada entradaSaldoFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
		entradaSaldoFlujoCaja.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaSaldoFlujoCaja.P_FECHADESDE = fechaSistema;
		entradaSaldoFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaMaximaCertificacion);
		entradaSaldoFlujoCaja.P_USO = _c.UsoFlujoCajaIngreso;
		entradaSaldoFlujoCaja.P_DESPLEGARENUI = _c.NO;
		entradaSaldoFlujoCaja.P_AGRUPARPORORIGEN = _c.NO;
		entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && _c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago)){
				String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
				BigDecimal montoAcum = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
				portTotalesMontos.put(id, montoAcum.add(sol.CostoMonedaLocal, MathContext.DECIMAL128));
				if(!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMonedaPago)){
					SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR saldo = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
					saldo.IdPortafolio = sol.IdPortafolio;
					saldo.IdMoneda = sol.IdMonedaPago;
					entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.add(saldo);
					if(!portFlujo.contains(sol.IdPortafolio)) portFlujo.add(sol.IdPortafolio);
					if(!monedaFlujo.contains(sol.IdMonedaPago)) monedaFlujo.add(sol.IdMonedaPago);
				}
			}
		}
		
		SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = new SaldoFlujoCaja.ServiceSalida();
		salidaFlujoCaja.P_OUT_FLUJOCAJA = new ArrayList<SaldoFlujoCaja.ObjFLUJOCAJA>();
		if(entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.size() > 0){
			salidaFlujoCaja = saldoFlujoCaja.logica(con, entradaSaldoFlujoCaja);
		}
		
		//LLAMAMOS FLUJO CUSTODIA
		
		ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
		entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujo.p_IdModulo = entrada.p_IdModulo;
		entradaFlujo.p_IdPais = idPaisEmpresa;
		entradaFlujo.p_FechaSaldo = fechaSistema;
		entradaFlujo.p_DiasProyeccion = 0;
		List<Long> fPortafolio = new ArrayList<Long>();
		List<Long> fInstrumento = new ArrayList<Long>();
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && serie.PatrimonioMinimo.compareTo(BigDecimal.ZERO) == 1){
				if(!fPortafolio.contains(sol.IdPortafolio)){
					fPortafolio.add(sol.IdPortafolio);
					ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
					cartera.IdCliente = sol.IdCliente;
					cartera.IdPortafolio = sol.IdPortafolio;
					entradaFlujo.p_Carteras.add(cartera);
				}
				if(!fInstrumento.contains(serie.IdInstrumento)){
					fInstrumento.add(serie.IdInstrumento);
					ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
					instrumento.IdInstrumento = serie.IdInstrumento;
					instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario;
					entradaFlujo.p_Instrumentos.add(instrumento);
				}
			}
		}
		
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
		
		if(entradaFlujo.p_Carteras.size() > 0){
			salidaFlujo = flujo.logica(con, entradaFlujo);
		}
		
		//TESORERIA
		ProcesarIngresoEgresoTesoreria procesarTesoreria = new ProcesarIngresoEgresoTesoreria();
		EntradaProcesarIngresoEgresoTesoreria entradaprocesarTesoreria = new EntradaProcesarIngresoEgresoTesoreria();
		//AGF
		CrearSolicitudAGF crearSolicitudAGF = new CrearSolicitudAGF();
		EntradaCrearSolicitudAGF entradaCrearAGF = new EntradaCrearSolicitudAGF();
		
		//SE VALIDA CUSTODIA Y FLUJO CAJA, SI HAY SE INSERTA
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)) continue;
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
			IndicadoresPortafolioFondoMutuo ind = (IndicadoresPortafolioFondoMutuo) hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
			String sobregiro = ind != null ? ind.OperaLineaSobregiro : _c.NO;
			BigDecimal monto =  BigDecimal.ZERO;
			for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
				if(FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(flujoCustodia, sol)){
					monto = flujoCustodia.CantidadDisponible.multiply(sol.ValorCuota, MathContext.DECIMAL128);
					break;
				}
			}

			if(serie.PatrimonioMinimo.compareTo(sol.Monto.add(monto, MathContext.DECIMAL128)) == 1){
				sol.EstadoProceso = _c.EstadoRechazado;
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_PATRIMONIO_MINIMO_INSUFICIENTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				continue;
			}
			
			//VALIDAMOS CUENTA INVERSION, SI NO HAY ERROR SALIMOS
			
			if(_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) && _c.NO.equalsIgnoreCase(sobregiro) && sol.IdSolicitudAporteRescateRelacionado.equals(0L)){
				String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
				BigDecimal saldo = BigDecimal.ZERO;
				BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
				String fecha = Fecha.stringToSqlDate(sol.FechaCertificacion);
				for(SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujoCaja.P_OUT_FLUJOCAJA){
					if(f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equalsIgnoreCase(sol.IdMonedaPago) && f.FechaSaldo.equalsIgnoreCase(fecha)){
						saldo = f.Saldo;
					}
				}
				if(total.compareTo(saldo) == 1){
					sol.EstadoProceso = _c.EstadoRechazado;
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_SALDO_INSUFICIENTE_FLUJO_CAJA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
			}
			
			//MUEVE TESORERIA
			if(!_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago)){
				entradaprocesarTesoreria.p_Solicitudes.add(sol);
			}
			
		}
		
		List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
		
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion)){
			if(entradaprocesarTesoreria.p_Solicitudes.size() > 0){
				entradaprocesarTesoreria.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaprocesarTesoreria.p_IdUsuario = entrada.p_IdUsuario;
				entradaprocesarTesoreria.p_IdModulo = entrada.p_IdModulo;
				entradaprocesarTesoreria.p_SimularOperacion = entrada.p_SimularOperacion;
				entradaprocesarTesoreria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;		
				entradaprocesarTesoreria.p_EsIngresoAutomatico = _c.NO;
				SalidaProcesarIngresoEgresoTesoreria salidaTesoreria = procesarTesoreria.logica(con, entradaprocesarTesoreria);
				for(ResultadoProceso_Salida rs : salidaTesoreria.P_OUT_RESULTADOPROCESO){
					if(_c.EstadoRechazadoTesoreria.equalsIgnoreCase(rs.Resultado)){
						for(SolicitudAporteRescate sol : solicitudesProcesar){
							if(rs.Id.longValue() == sol.IdInterno.longValue() && rs.Campo.equalsIgnoreCase(_c.NumeroReferenciaTesoreria)){
								sol.EstadoProceso = _c.EstadoRechazado;
								break;
							}
						}
					}
				}
			}
			//VALIDAMOS SOLICITUDES CORRECTAS A INSERTAR
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					if(_c.NO.equalsIgnoreCase(sol.SolicitudBatch)){
						//AGF
						CrearSolicitudAGF.Solicitudes solAGF = new CrearSolicitudAGF.Solicitudes();
						solAGF.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
						entradaCrearAGF.p_Solicitudes.add(solAGF);
					}
					solicitudesInsertar.add(sol);
					salida.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
			//SPOT
			if(aporteGeneraCompraSpotDolar.equals(_c.SI)) {
				//filtramos solo los movimientos que generan spot
				List<SolicitudAporteRescate> generanSpot = solicitudesInsertar.stream()
						.filter(p->p.GeneraSpot != null).filter(p->p.GeneraSpot.equals(_c.SI)).collect(Collectors.toList());
				Map<Long, InSolicitudesAporte> solicitudesEntradaAgrupadasPorIdInterno = entrada.p_SolicitudesAporte.parallelStream().collect(Collectors.toMap(solicitud->solicitud.IdInterno, solicitud->solicitud));
				if(generanSpot.size() > 0) {
					ProcesarIngresoOperacionSpot procesarSpot = new  ProcesarIngresoOperacionSpot();
					EntradaProcesarIngresoOperacionSpot entradaprocesarSpot= new EntradaProcesarIngresoOperacionSpot();
					Long c = 0L;
					for(SolicitudAporteRescate sol : generanSpot){
						sol.Estado = _c.EstadoPendiente;
						InSolicitudesAporte datosEntradaSolicitud = solicitudesEntradaAgrupadasPorIdInterno.get(sol.IdInterno);
						String fechaCertificaSpotCompra = null;
						String fechaLiquidaSpotCompra = null;
						if(datosEntradaSolicitud != null) {
							fechaCertificaSpotCompra = datosEntradaSolicitud.FechaCertificaSpotCompra;
							fechaLiquidaSpotCompra = datosEntradaSolicitud.FechaLiquidaSpotCompra;
						}
						//Se revisan los casos de compra spot que provengan de un rescate con reinversion para asignar la fecha certificacion del rescate
						if(_c.AporteReinversion.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && fechaCertificaSpotCompra != null) {
							SolicitudAporteRescate solSpot = copiarSolicitudAporteRescate(sol);
							solSpot.FechaCertificacion = Fecha.toDate(fechaCertificaSpotCompra);
							entradaprocesarSpot.p_DatosOperacion.add(solSpot);
						}
						else
							entradaprocesarSpot.p_DatosOperacion.add(sol);
						InSolicitudesAporte inSolicitud = (InSolicitudesAporte) listaEntrada.get(sol.IdInterno);
						entradaprocesarSpot.p_DatosOrigenInversion.add(inSolicitud);
						SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
						FormaPagoSpot venta = new FormaPagoSpot();
						FormaPagoSpot compra = new FormaPagoSpot();
						if(inSolicitud.IdMoneda.equals(_c.Pesos) && serie.IdMoneda.equals(_c.Dolar)) {
							venta.AfectarTesoreria="N";
							venta.IdComprobantePagoOperacion=0L;
							venta.IdFormaDePago="COMSPT_CIN_CLP_0";
							venta.IdInternoPagoOperacion=c++;
							venta.IdMoneda=_c.Pesos;
							venta.IdMonedaLiquidacion=_c.Pesos;
							venta.IdOperacion=sol.IdInterno;
							venta.IdTipoMovimiento="MOVING";
							venta.MontoFormaDePago=((CalcularMontoNumeroCuota.Resultado) valoresCuota.get(sol.IdInterno)).MontoDestino;
							venta.RecibePagaPortafolio="R";
							venta.FechaLiquidacion=sol.FechaAceptacion;
							entradaprocesarSpot.p_DatosFormaPago.add(venta);
							
							compra.AfectarTesoreria="N";
							compra.IdComprobantePagoOperacion=0L;
							compra.IdFormaDePago="VENSPT_CIN_USD_0";
							compra.IdInternoPagoOperacion=c++;
							compra.IdMoneda=_c.Dolar;
							compra.IdMonedaLiquidacion=_c.Dolar;
							compra.IdOperacion=sol.IdInterno;
							compra.IdTipoMovimiento="MOVEGR";
							compra.MontoFormaDePago=sol.Monto;
							compra.RecibePagaPortafolio="P";
							compra.FechaLiquidacion=sol.FechaAceptacion;
							entradaprocesarSpot.p_DatosFormaPago.add(compra);
						}
						else if(inSolicitud.IdMoneda.equals(_c.Dolar) && serie.IdMoneda.equals(_c.Pesos)) {
							venta.AfectarTesoreria="N";
							venta.IdComprobantePagoOperacion=0L;
							venta.IdFormaDePago="COMSPT_CIN_USD_0";
							venta.IdInternoPagoOperacion=c++;
							venta.IdMoneda=_c.Dolar;
							venta.IdMonedaLiquidacion=_c.Dolar;
							venta.IdOperacion=sol.IdInterno;
							venta.IdTipoMovimiento="MOVEGR";
							venta.MontoFormaDePago=((CalcularMontoNumeroCuota.Resultado) valoresCuota.get(sol.IdInterno)).MontoDestino;
							venta.RecibePagaPortafolio="R";
							if(_c.AporteReinversion.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && fechaLiquidaSpotCompra != null)
								venta.FechaLiquidacion = Fecha.toDate(fechaLiquidaSpotCompra);
							else
							venta.FechaLiquidacion=sol.FechaAceptacion;
							entradaprocesarSpot.p_DatosFormaPago.add(venta);
							
							compra.AfectarTesoreria="N";
							compra.IdComprobantePagoOperacion=0L;
							compra.IdFormaDePago="VENSPT_CIN_CLP_0";
							compra.IdInternoPagoOperacion=c++;
							compra.IdMoneda=_c.Pesos;
							compra.IdMonedaLiquidacion=_c.Pesos;
							compra.IdOperacion=sol.IdInterno;
							compra.IdTipoMovimiento="MOVING";
							compra.MontoFormaDePago=sol.Monto;
							compra.RecibePagaPortafolio="P";
							if(_c.AporteReinversion.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && fechaLiquidaSpotCompra != null)
								compra.FechaLiquidacion = Fecha.toDate(fechaLiquidaSpotCompra);
							else
							compra.FechaLiquidacion=sol.FechaAceptacion;
							entradaprocesarSpot.p_DatosFormaPago.add(compra);
						}
					}
					
					if(entradaprocesarSpot.p_DatosOperacion.size() > 0 && entradaprocesarSpot.p_DatosFormaPago.size() > 0){
						entradaprocesarSpot.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaprocesarSpot.p_IdUsuario = entrada.p_IdUsuario;
						entradaprocesarSpot.p_IdModulo = entrada.p_IdModulo;
						entradaprocesarSpot.p_CalculaMargen = _c.SpotCalculaMargen;
						entradaprocesarSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;		
						SalidaProcesarIngresoOperacionSpot salidaSpot = procesarSpot.logica(con, entradaprocesarSpot);
						for(OperacionObjOut rs : salidaSpot.P_OUT_IDOPERACIONSPOT){
							for(SolicitudAporteRescate sol : solicitudesInsertar){
								if((Long.parseLong(rs.IdInterno) == sol.IdInterno) ){
									if(_c.SpotEstadoRechazado.equalsIgnoreCase(rs.Estado)){			
										sol.EstadoProceso = _c.EstadoRechazado;
										break;
									}else{
										sol.IdOperacionSpot = rs.IdOperacionSpot;
										break;
									}
								}
							}
						}
						for(MensajeSalida rs : salidaSpot.P_OUT_MENSAJES){
							if(_c.SpotError.equalsIgnoreCase(rs.tipo)){
								salida.P_OUT_ERRORESCONTADOR += 1L;
								salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, rs.tipo, rs.codigo,  _c.ModuloSpot + " : " + rs.descripcion));
								//Si tiene errores, rechaza Aportes
								for(SolicitudAporteRescate sol : solicitudesInsertar){
										sol.EstadoProceso = _c.EstadoRechazado;
										break;
								}
							}
						}
						
						if(salida.P_OUT_ERRORESCONTADOR > 0) {
							return true;
						}
					}
				}
			}
			//END SPOT
			//INSERTAMOS LAS SIN ERRORES
			if(solicitudesInsertar.size() > 0){
				sql.batch(con, SQL_INSERT_SOLICITUD, solicitudesInsertar, SolicitudAporteRescate.class);
				actualizarFechaFinalReinversion(sql, con, solicitudesInsertar);
				//AGF
				if(entradaCrearAGF.p_Solicitudes.size() > 0){
					entradaCrearAGF.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCrearAGF.p_IdUsuario = entrada.p_IdUsuario;
					entradaCrearAGF.p_IdModulo = entrada.p_IdModulo;
					entradaCrearAGF.p_EsManual = _c.NO;
					SalidaCrearSolicitudAGF salidaAGF = crearSolicitudAGF.logica(con, entradaCrearAGF);
					salida.P_OUT_ERRORESCONTADOR += salidaAGF.P_OUT_ERRORESCONTADOR;
					salida.P_OUT_MENSAJES.addAll(salidaAGF.P_OUT_MENSAJES);
				}
				//AGREGAR LOG
				{
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					for (SolicitudAporteRescate sol : solicitudesInsertar) {
						ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(sol.IdPortafolio);
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = sol.IdSolicitudAporteRescate;
						log.IdTipoLog = "INGRESO";
						log.IdModulo = _c.ModuloFondoMutuo;
						log.IdUsuario = entrada.p_IdUsuario;
						log.IdEmpresa = entrada.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = entrada.p_IdUsuario;
						log.IdCliente = sol.IdCliente;
						log.EstadoCliente = cliente.IdEstadoCliente;
						log.NombreCliente = cliente.NombreCompleto;
						log.IdTipoDocumentoIdentidad = cliente.TipoDocumentoIdentidad;
						log.IdDocumentoIdentidad = cliente.DocumentoIdentidad;
						log.IdPortafolio = sol.IdPortafolio;
						log.EstadoPortafolio = cliente.IdEstadoPortafolio;
						log.IdTipoRegistro = "SOLICITUD_APORTE_REINVERSION";
						log.EstadoRegistro = sol.Estado;
						log.IdTipoOperacion = sol.IdTipoMovimientoFondoMutuo;
						log.Folio = sol.IdSolicitudAporteRescate;
						log.Cantidad = sol.NumeroCuotas;
						log.PrecioTasa = sol.ValorCuota;
						log.Monto = sol.Monto;
						log.IdMonedaOrigen = sol.IdMoneda;
						log.IdMonedaLiquidacion = sol.IdMoneda;
						log.TipoCambio = sol.ParidadFechaInversion.doubleValue()+"";
						log.Nemo = sol.IdSerieFondo;
						log.IdProceso = 0L;
						log.EstadoProceso = "";
						log.NombreProceso = "";
						entradaAgregarLog.p_Log.add(log);
					}
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(con, entradaAgregarLog);
				}
			}
		}else{
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					salida.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
		}
        
		Long fin = System.currentTimeMillis();
    	logger.info("FIN::CrearSolicitudAporteReinversion.procesarSolicitudes, tiempo:" + (fin - inicio));
    	return false;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		//String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"AGE\",\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdSerieFondoOrigen\":\"\",\"IdEmisorOrigen\":\"CREDICORPAGF\",\"CostoUF\":0.4639500414654660306667999835314461,\"FechaOrigenInversion\":\"2018-08-03\",\"MontoOrigen\":5048.40000000,\"IndicadorMLT\":\"N\",\"IdSerieFondo\":\"TEST6\",\"Monto\":5048.40000000,\"NumeroCuotas\":0,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"Visado\":\"S\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdSolicitudAporteRescateRelacionado\":19659,\"FechaEmision\":\"2018-08-03\",\"Porcentaje\":100,\"IdAgrupador\":50,\"FechaCertificacion\":\"2018-08-03\",\"Switch\":\"N\"},{\"IdInterno\":2,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdSerieFondoOrigen\":\"\",\"IdEmisorOrigen\":\"CREDICORPAGF\",\"CostoUF\":0.4639500414654660306667999835314461,\"FechaOrigenInversion\":\"2018-08-03\",\"MontoOrigen\":7572.60000000,\"IndicadorMLT\":\"N\",\"IdSerieFondo\":\"TEST_C\",\"Monto\":7572.60000000,\"NumeroCuotas\":0,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"Visado\":\"S\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdSolicitudAporteRescateRelacionado\":19659,\"FechaEmision\":\"2018-08-03\",\"Porcentaje\":100,\"IdAgrupador\":50,\"FechaCertificacion\":\"2018-08-03\",\"Switch\":\"N\"}]}";		
		//String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"AGE\",\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdSerieFondoOrigen\":\"\",\"IdEmisorOrigen\":\"CREDICORPAGF\",\"CostoUF\":0.3669399662394645367336976020609219,\"FechaOrigenInversion\":\"2018-08-03\",\"MontoOrigen\":3992.80000000,\"IndicadorMLT\":\"S\",\"IdSerieFondo\":\"FMLIQUIDEZ\",\"Monto\":3992.80000000,\"NumeroCuotas\":0,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"Visado\":\"S\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdSolicitudAporteRescateRelacionado\":19748,\"FechaEmision\":\"2018-08-03\",\"Porcentaje\":100,\"IdAgrupador\":61,\"FechaCertificacion\":\"2018-08-03\",\"Switch\":\"N\"},{\"IdInterno\":2,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdSerieFondoOrigen\":\"\",\"IdEmisorOrigen\":\"CREDICORPAGF\",\"CostoUF\":0.3669399662394645367336976020609219,\"FechaOrigenInversion\":\"2018-08-03\",\"MontoOrigen\":2994.60000000,\"IndicadorMLT\":\"S\",\"IdSerieFondo\":\"FM_APC1EXT_B\",\"Monto\":2994.60000000,\"NumeroCuotas\":0,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"Visado\":\"S\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdSolicitudAporteRescateRelacionado\":19748,\"FechaEmision\":\"2018-08-03\",\"Porcentaje\":100,\"IdAgrupador\":61,\"FechaCertificacion\":\"2018-08-03\",\"Switch\":\"N\"},{\"IdInterno\":3,\"IdCliente\":524891,\"IdPortafolio\":512341,\"IdSerieFondoOrigen\":\"\",\"IdEmisorOrigen\":\"CREDICORPAGF\",\"CostoUF\":0.3669399662394645367336976020609219,\"FechaOrigenInversion\":\"2018-08-03\",\"MontoOrigen\":2994.60000000,\"IndicadorMLT\":\"S\",\"IdSerieFondo\":\"FM_APC1EXT_A\",\"Monto\":2994.60000000,\"NumeroCuotas\":0,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"Visado\":\"S\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdSolicitudAporteRescateRelacionado\":19748,\"FechaEmision\":\"2018-08-03\",\"Porcentaje\":100,\"IdAgrupador\":61,\"FechaCertificacion\":\"2018-08-03\",\"Switch\":\"N\"}]}";
		//USD a CLP
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":1424051,\"IdPortafolio\":1451747,\"IdSerieFondoOrigen\":\"\",\"IdEmisorOrigen\":\"\",\"CostoUF\":0,\"FechaOrigenInversion\":\"0001-01-01\",\"MontoOrigen\":1000000,\"IndicadorMLT\":\"S\",\"IdSerieFondo\":\"FMLIQUIDEZ_B\",\"Monto\":1000000,\"NumeroCuotas\":359712.23,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"Visado\":\"N\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"APV-B\",\"Glosa\":\"\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0}]}";
		EntradaCrearSolicitudAporteReinversion Objin = gson.fromJson(jsonRF, EntradaCrearSolicitudAporteReinversion.class);
		CrearSolicitudAporteReinversion  demo=new CrearSolicitudAporteReinversion();				
		try {
			SalidaCrearSolicitudAporteReinversion ObjOut=demo.logica(con, Objin);
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
