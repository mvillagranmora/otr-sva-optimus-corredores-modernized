package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAGF.EntradaCrearSolicitudAGF;
import cl.bcs.fondomutuo.CrearSolicitudAGF.SalidaCrearSolicitudAGF;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.SalidaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.BloqueoReinversion;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.DesbloquearReinversion;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.InEntradaReinversion;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.InEntradaReinversionAPV;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.MensajesErrores;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.ResultadoOperacion;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.SalidaProcesarReinversionRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.EntradaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.SalidaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.pojo.ConfiguracionRegimenFondoMutuo;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.Moneda;
import cl.bcs.fondomutuo.pojo.PortafolioAPV;
import cl.bcs.fondomutuo.pojo.RegimenMandato;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ReinversionSerieFondoDestino;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InTipoPrograma;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchEntrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.Parametro;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudRescateReinversion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearSolicitudRescateReinversion.class);
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_SELECT_REGIMEN_MANDATO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondoMandato.sql");
	private final String SQL_SELECT_MONEDA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_INSERT_REINVERSION_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertReinversionSolicitud.sql");
	private final String SQL_CONFIGURACION_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionRegimenFondoMutuo.sql");
	private final String SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
	private final String SQL_SELECT_PORTAFOLIO_APV = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolioAPV.sql");
	private final String SQL_CALL_CREAR_BLOQUEO_REINVERSION 		= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_Call_CrearBloqueoReinversion.sql");
	private final String SQL_CALL_ELIMINAR_BLOQUEO_REINVERSION 		= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversion_Call_EliminarBloqueoReinversion.sql");
	private final String SQL_CREATE_TABLE_BLOQUEO_REINVERSION		= loadResourceToString("cl/bcs/fondomutuo/sql/CrearReinversionRescate_Tabla_Bloqueo.sql");
	private final String SQL_DROP_TABLE_BLOQUEO_REINVERSION			= loadResourceToString("cl/bcs/fondomutuo/sql/DropReinversionRescate_Tabla_Bloqueo.sql");
	private final String SQL_INSERT_TABLE_BLOQUEO_REINVERSION		= loadResourceToString("cl/bcs/fondomutuo/sql/InsertReinversionRescate_Tabla_Bloqueo.sql");
	private final String SQL_SELECT_TABLE_BLOQUEO_REINVERSION		= loadResourceToString("cl/bcs/fondomutuo/sql/SelectReinversionRescate_Tabla_Bloqueo.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudRescateReinversion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<InSolicitudesRescate> p_SolicitudesRescate;
		public List<InSolicitudesAporte> p_SolicitudesAporte;
		public String p_IdOrigenProceso;  //CAR: Carga Masiva Fondos Mutuos
		public EntradaCrearSolicitudRescateReinversion() {
			p_SolicitudesAporte = new ArrayList<InSolicitudesAporte>();
			p_SolicitudesRescate = new ArrayList<InSolicitudesRescate>();
			this.p_IdOrigenProceso = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudRescateReinversion{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudRescateReinversion(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesRescate{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String Glosa;
    	public Long IdAutorizadoOrigen;
    	public Long FolioContrato;
    	public Long IdAgrupador;
    	public String IdRegimenTributario;
		public BigDecimal MontoPatrimonio;
		public BigDecimal Porcentaje;
		public String IngresoAutomatico;
		public Date FechaValorCuota;
		public String IdBloqueoRescate;
		public String TipoReinversion = "";        // SWITCH   -  INTERNACIONAL -  NACIONAL     
		
		public InSolicitudesRescate() {
			this.MontoPatrimonio = BigDecimal.ZERO;
			this.IdAgrupador = 0L;
			this.Porcentaje = new BigDecimal("0");
			this.IngresoAutomatico = "N";
			this.IdBloqueoRescate="";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesAporte{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdSerieFondo;
		public String IdEmisor;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String IndicadorMLT;
		public String IdMoneda;
		public String IdMedioDePago;
		public String IdRegimenTributario;
		public String Glosa;
		public Long FolioContrato;
		public BigDecimal Porcentaje;
		public Long IdAgrupador;
		public String TipoReinversion;
		public String IdSerieFondoOrigen;
		public String SolicitudBatch;
		public BigDecimal PorcentajeReal = BigDecimal.ZERO;
		public InSolicitudesAporte() {
			this.IdAgrupador = 0L;
			this.Porcentaje = new BigDecimal("100");
			this.SolicitudBatch = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class KeyAporteMonto{
		public Long IdAgrupador;
		public String IdSerieFondo;
		@Override
		public int hashCode() {
			return 31 * this.IdAgrupador.hashCode() + this.IdSerieFondo.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			KeyAporteMonto llave = (KeyAporteMonto) obj;
			return this.IdAgrupador.equals(llave.IdAgrupador) && this.IdSerieFondo.equals(llave.IdSerieFondo);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SeriesMonedasDiferentes{
		public String SonMonedasDiferentes;
		
		public SeriesMonedasDiferentes() {
			this.SonMonedasDiferentes = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BloqueoReinversion extends BcsPojo{
		public String		IdBloqueaReinversion;
		public Long			IdPortafolio;
		public Long			IdCliente;
		public String		IdRegimenTributario;
		public String 		IdGrupoRegimen;
		public String		IdSerieFondo;	
		public BigDecimal	Monto;
		public BigDecimal	NumeroCuotas;
		public Date			FechaSistema;
		public String		Simula;
		
		public BloqueoReinversion() {
			this.IdPortafolio = 0L;
			this.IdCliente = 0L;
			this.IdRegimenTributario = "";
			this.IdGrupoRegimen = "";
			this.Simula = "";
		}
	}
	
	public static class DesbloquearReinversion extends BcsPojo{
		public String		IdBloqueaReinversion;
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearSolicitudRescateReinversion jin = gson.fromJson(in.toString(), EntradaCrearSolicitudRescateReinversion.class);
		SalidaCrearSolicitudRescateReinversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudRescateReinversion logica(Connection con, EntradaCrearSolicitudRescateReinversion entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudRescateReinversion");
		SalidaCrearSolicitudRescateReinversion out = new SalidaCrearSolicitudRescateReinversion();
		SQL sql=new SQL();
		boolean hasVC = true;
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesRescate.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_RESCATE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitudesRescate mov: entrada.p_SolicitudesRescate) {
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
				//agrego de acuerdo al idagrupador
				correlativos.add(mov.IdInterno);
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String rescateGeneraVentaSpotDolar = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "RESCATE_GENERA_VENTA_SPOT_DOLAR");
		String respetaValutasCargaMasiva = _c.NO;
		try {
			respetaValutasCargaMasiva = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CrearSolicitudAporteRescateNormal::No se encuentra parametro RespetaValutasCargaMasiva");
		}
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		int countTransaccionesBatch = 0;
		List<String> series = new ArrayList<String>();
		List<Long> clientes = new ArrayList<Long>();
		List<Long> portafolios = new ArrayList<Long>();
		List<String> regimen = new ArrayList<String>();
		List<String> monedas = new ArrayList<String>();
		List<String> idTipoMovimientos = new ArrayList<String>();
				
		idTipoMovimientos.add(_c.Reinversion);
		idTipoMovimientos.add(_c.AporteReinversion);
		
		ArrayList<BloqueoReinversion> bloquearReinversion = new ArrayList<>();
		
		int secuenciaReinversion = 0;
		
		for(InSolicitudesRescate rescates : entrada.p_SolicitudesRescate){
			if(_c.SI.equalsIgnoreCase(rescates.SolicitudBatch)){
				countTransaccionesBatch++;
			}
			if(!series.contains(rescates.IdSerieFondo)) series.add(rescates.IdSerieFondo);
			if(!clientes.contains(rescates.IdCliente)) clientes.add(rescates.IdCliente);
			if(!portafolios.contains(rescates.IdPortafolio)) portafolios.add(rescates.IdPortafolio);
			if(!regimen.contains(rescates.IdRegimenTributario)) regimen.add(rescates.IdRegimenTributario);
		}
		
		for(InSolicitudesAporte aportes : entrada.p_SolicitudesAporte){
			if(!series.contains(aportes.IdSerieFondo)) series.add(aportes.IdSerieFondo);
			if(!regimen.contains(aportes.IdRegimenTributario)) regimen.add(aportes.IdRegimenTributario);			
		}
		
		ObtenerPerfilFondoMutuo obtenerPerfilFondoMutuo = new ObtenerPerfilFondoMutuo();
		EntradaObtenerPerfilFondoMutuo entradaObtenerPerfilFondoMutuo = new EntradaObtenerPerfilFondoMutuo();
		entradaObtenerPerfilFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
		SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondoMutuo.logica(con, entradaObtenerPerfilFondoMutuo);
		
		if(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo == null && countTransaccionesBatch != entrada.p_SolicitudesRescate.size()){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		params.clear();
		params.put("IdPortafolio", portafolios);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<ClientePortafolioSolicitudes> clientesSolicitudes = sql.query(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolioSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		params.clear();
		params.put("IdTipoMovimientoFondoMutuo", idTipoMovimientos);
		logger.debug("INICIO::SQL_SELECT_TIPO_MOVIMIENTO");
		List<TipoMovimientoFondoMutuo> tipoMovimientos = sql.query(con, SQL_SELECT_TIPO_MOVIMIENTO, params, TipoMovimientoFondoMutuo.class);
		logger.debug("FIN::SQL_SELECT_TIPO_MOVIMIENTO");
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		params.put("IdRegimenTributario", regimen);
		logger.debug("INICIO::SQL_SELECT_REGIMEN");
		List<RegimenSerieDeFondo> regimenesSeries = sql.query(con, SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class);
		logger.debug("FIN::SQL_SELECT_REGIMEN");
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		logger.debug("INICIO::SQL_SELECT_REGIMEN_MANDATO");
		List<RegimenMandato> regimenMandato = sql.query(con, SQL_SELECT_REGIMEN_MANDATO, params, RegimenMandato.class);
		logger.debug("FIN::SQL_SELECT_REGIMEN_MANDATO");
		params.clear();
		params.put("IdRegimenFondoMutuoOrigen", regimen);
		logger.debug("INICIO::SQL_CONFIGURACION_REGIMEN");
		List<ConfiguracionRegimenFondoMutuo> configuracionesRegimenFFMM = sql.query(con, SQL_CONFIGURACION_REGIMEN, params, ConfiguracionRegimenFondoMutuo.class);
		logger.debug("FIN::SQL_CONFIGURACION_REGIMEN");
		params.clear();
		params.put("IdRegimenFondoMutuo", regimen);
		logger.debug("INICIO::SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO");
		List<ConfiguracionPropositoPortafolio> configuracionesRegimenPortafolio = sql.query(con, SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO, params, ConfiguracionPropositoPortafolio.class);
		logger.debug("FIN::SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO");
		params.clear();
		
		for(SerieFondoSolicitudes serie : seriesSolicitudes){
			if(!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
		}
		params.put("IdMoneda", monedas);
		logger.debug("INICIO::SQL_SELECT_MONEDA");
		List<Moneda> monedasFondo = sql.query(con, SQL_SELECT_MONEDA, params, Moneda.class);
		logger.debug("FIN::SQL_SELECT_MONEDA");
		params.clear();
		
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
		HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
		HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
		HashMap<Long,Object> hrescates = Utils.List2Hash(entrada.p_SolicitudesRescate, "IdInterno");
		HashMap<Long,Object> haportes = Utils.List2Hash(entrada.p_SolicitudesAporte, "IdInterno");
		HashMap<String,Object> hmonedasFondo =  Utils.List2Hash(monedasFondo, "IdMoneda");
		HashMap<String,Object> hregimenConfiguraciones =  Utils.List2Hash(configuracionesRegimenFFMM, "Id");
		HashMap<String,Object> hconfRegimenPortafolio =  Utils.List2Hash(configuracionesRegimenPortafolio, "Id");
		
		// ------------------------------------------------------------------------------------------
		// DETERMINA EL TIPO DE REINVERSION (De acuerdo a Fondo Origen Destino)
		//  	- SWITCH		: Reinversiones entre fondos extranjeros de un mismo emisor (Ej: USD > USD  mismo emisor)
		//  	- INTERNACIONAL : Reinversiones entre fondos extranjeros de distintos emisores (Ej: USD > USD distinto emisor)
		//  	- NACIONAL      : Reinversion de un fondo extranjo a uno nacional (Ej: USD > CLP)
		//  	- NACIONAL      : Reinversion de un fondo nacional a cualquier otro (Ej:  CLP > *)
		// ------------------------------------------------------------------------------------------
		HashMap<Long,SeriesMonedasDiferentes> hseriesMonedasDiferentes =new HashMap<Long,SeriesMonedasDiferentes>();
		for(InSolicitudesRescate rescate : entrada.p_SolicitudesRescate){
			hseriesMonedasDiferentes.put(rescate.IdInterno, new SeriesMonedasDiferentes());
			for(InSolicitudesAporte aporte : entrada.p_SolicitudesAporte){
				SerieFondoSolicitudes serieRescate = (SerieFondoSolicitudes) hseries.get(rescate.IdSerieFondo);										
				SerieFondoSolicitudes serieAporte  = (SerieFondoSolicitudes) hseries.get(aporte.IdSerieFondo);
				
				if (aporte.IdInterno.compareTo(rescate.IdInterno) == 0){
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("S")  && serieRescate.IdEmisor.equalsIgnoreCase(serieAporte.IdEmisor))
						rescate.TipoReinversion = "SWITCH";
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("S")  && !serieRescate.IdEmisor.equalsIgnoreCase(serieAporte.IdEmisor))
						rescate.TipoReinversion = "INTERNACIONAL";
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("N")) {
						rescate.TipoReinversion = "NACIONAL";
						hseriesMonedasDiferentes.get(rescate.IdInterno).SonMonedasDiferentes = !serieRescate.IdMoneda.equalsIgnoreCase(serieAporte.IdMoneda) ? _c.SI : _c.NO;
					}
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("N"))
						rescate.TipoReinversion = "NACIONAL";

					aporte.IdSerieFondoOrigen = rescate.IdSerieFondo;		
				}
			}
		}

		CalcularFechaSolicitudAporteRescate calculoFechas = new CalcularFechaSolicitudAporteRescate();
		EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
		entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFechas.p_IdPais = idPaisEmpresa;
		
		for(InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularFechaSolicitudAporteRescate.InFechas fecha = new CalcularFechaSolicitudAporteRescate.InFechas();
			if(serie != null){
				fecha.IdInterno = solicitud.IdInterno;
				fecha.IdSerieFondo = solicitud.IdSerieFondo;
				fecha.FechaEmision = Fecha.stringToSqlDate(serie.FechaFondo);
				if(_c.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && _c.NO.equals(respetaValutasCargaMasiva)){
					fecha.EstadoFondo = _c.Abierto;
					fecha.EstadoLiquidacion = _c.Abierto;
					fecha.NumeroDiasRetencion = 0L;
					fecha.NumeroValutaValorCuota = 0L;
					fecha.NumeroValutaCertificacion = 0L;
					fecha.NumeroValutaCurse = 0L;
					fecha.SolicitudBatch = _c.SI;
				}else{
					fecha.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
					fecha.NumeroDiasRetencion 		= solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") ? 2 : (solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL") ? 4 : serie.DiasRetencionesRescate);
					fecha.NumeroValutaValorCuota 	= (solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ? 1 : serie.DiasValutaValorCuotaRescate;
					fecha.NumeroValutaCertificacion = (solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ? 2 : serie.DiasValutaCertificacionRescate;
					fecha.NumeroValutaCurse = serie.DiasValutaCurseRescate;
					fecha.EstadoLiquidacion = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
					//SIEMPRE EL FONDO ESTA ABIERTO PARA BATCH.
					if(_c.SI.equals(solicitud.SolicitudBatch)) {
						fecha.EstadoFondo = _c.Abierto;
						fecha.EstadoLiquidacion = _c.Abierto;
					}
					fecha.SolicitudBatch = _c.NO;
				}
				fecha.TipoSolicitud = _c.TipoSolicitudRescate;
				fecha.FondoExtranjero = serie.FondoExtranjero;
				fecha.IdPaisFondo = serie.IdPaisOrigen;
				fecha.IdPaisEmisor = serie.IdPaisEmisor;
				Moneda moneda = (Moneda) hmonedasFondo.get(serie.IdMoneda);
				fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
				if(_c.SI.equalsIgnoreCase(rescateGeneraVentaSpotDolar) && _c.SI.equalsIgnoreCase(hseriesMonedasDiferentes.get(solicitud.IdInterno).SonMonedasDiferentes)) {
					fecha.RescateGeneraSpotCertificacion = _c.SI;
				}
				fecha.FondoNacionalDolar = serie.FondoNacionalDolar;
				fecha.IdMercadoFondoMutuo = serie.IdMercadoFondoMutuo;
				entradaFechas.p_SolicitudAporteRescate.add(fecha);
				
				String regimenTributario = entrada.p_SolicitudesRescate.get(secuenciaReinversion).IdRegimenTributario!=null?entrada.p_SolicitudesRescate.get(secuenciaReinversion).IdRegimenTributario:"";
				BigDecimal Monto_Cuotas = "C".equalsIgnoreCase(entrada.p_SolicitudesRescate.get(secuenciaReinversion).CalculoPorMontoCuota)?entrada.p_SolicitudesRescate.get(secuenciaReinversion).NumeroCuotas:entrada.p_SolicitudesRescate.get(secuenciaReinversion).Monto;
				
				BloqueoReinversion reinversionBloqueo = new BloqueoReinversion();
				String idBloqueaReInversion = entrada.p_SolicitudesRescate.get(secuenciaReinversion).IdPortafolio.toString() + '_' 
						+ regimenTributario + '_' + entrada.p_SolicitudesAporte.get(secuenciaReinversion).IdSerieFondo+'_'
						+ Monto_Cuotas + '_'
						+ fechaSistema.toString();
				
				solicitud.IdBloqueoRescate = idBloqueaReInversion;
				reinversionBloqueo.IdBloqueaReinversion= idBloqueaReInversion;
				reinversionBloqueo.IdPortafolio 		= entrada.p_SolicitudesRescate.get(secuenciaReinversion).IdPortafolio;	
				reinversionBloqueo.IdSerieFondo		= entrada.p_SolicitudesAporte.get(secuenciaReinversion).IdSerieFondo;
				reinversionBloqueo.IdRegimenTributario	= entrada.p_SolicitudesRescate.get(secuenciaReinversion).IdRegimenTributario!=null?entrada.p_SolicitudesRescate.get(secuenciaReinversion).IdRegimenTributario:"";
				reinversionBloqueo.Monto				= entrada.p_SolicitudesRescate.get(secuenciaReinversion).Monto;
				reinversionBloqueo.NumeroCuotas		= entrada.p_SolicitudesRescate.get(secuenciaReinversion).NumeroCuotas;
				reinversionBloqueo.FechaSistema		=Fecha.toDate(fechaSistema);
				reinversionBloqueo.Simula 				= entrada.p_SimularOperacion!=null?entrada.p_SimularOperacion:"N";
				
				bloquearReinversion.add(reinversionBloqueo);

				
				
			}
			secuenciaReinversion++;
		}
		insertarReinversionBloqueada(con,entrada.p_IdEmpresa, bloquearReinversion, out);

		if(out.P_OUT_ERRORESCONTADOR>0){
			return out;
		}

		SalidaCalcularFechaSolicitudAporteRescate salidaFechas = calculoFechas.logica(con, entradaFechas);
		
		Map<Long, String> solicitudesBatches = entrada.p_SolicitudesRescate.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r.SolicitudBatch));
		
		HashMap<Long,Object> hfechas =  Utils.List2Hash(salidaFechas.P_FECHASAPORTERESCATE, "IdInterno");
		HashMap<Long,Object> hfechasAporte = obtenerFechaAporte(con, entrada, idPaisEmpresa, hseries, salidaObtenerPerfilFondoMutuo, hmonedasFondo, hfechas, respetaValutasCargaMasiva, solicitudesBatches, hrescates);
		
		HashMap<Long,Object> rescates_unicos = new HashMap<Long,Object>();
		String p_IdSerieFondo = "";
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		for(InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			serie.IdInterno = solicitud.IdInterno;
			if (solicitud.FechaValorCuota != null) {
				serie.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			} else if (fecha != null) {
				serie.Fecha = Fecha.stringToSqlDate(fecha.FechaValorCuota);
			} else {
				serie.Fecha = fechaSistema;
			}
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud = _c.TipoSolicitudRescate;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	serie.IdTipoMovimientoFondoMutuo= "RESREINV";
		 	entradaCalculo.p_Series.add(serie);
		 	
		 	
		 	if(!rescates_unicos.containsKey(solicitud.IdAgrupador)){
		 		rescates_unicos.put(solicitud.IdAgrupador, solicitud);
		 		p_IdSerieFondo = solicitud.IdSerieFondo;
		 	}
		}
				
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);

		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		entradaCalculo.p_Series.clear();
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			CalcularMontoNumeroCuota.Resultado montoRescate = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			
			if (!"OK".equalsIgnoreCase(montoRescate.CodigoMensaje)) {
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> datosMensaje = new HashMap<String,String>();
				datosMensaje.put("IdSerieFondo", p_IdSerieFondo);
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"ERROR_VALOR_CUOTA",datosMensaje,con,entrada.p_IdEmpresa));			
				eliminarReinversionBloqueada(con,entrada.p_IdEmpresa, entrada.p_SolicitudesRescate, out.P_OUT_MENSAJES,entrada.p_SimularOperacion);
				return out;				
			}	
									
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			InSolicitudesRescate res = (InSolicitudesRescate) hrescates.get(solicitud.IdInterno);
			serie.IdInterno = solicitud.IdInterno * -1;
			serie.Fecha = serieFondo != null ? Fecha.stringToSqlDate(serieFondo.FechaFondo) : fechaSistema;
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = _c.CalculoPorMonto;
		 	serie.Monto = montoRescate.Monto.multiply(solicitud.Porcentaje, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128);			 		 	
		 	serie.NumeroCuotas = BigDecimal.ZERO;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = res.IdMoneda;
		 	serie.TipoSolicitud = _c.TipoSolicitudAporte;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	serie.Porcentaje = solicitud.Porcentaje;
		 	entradaCalculo.p_Series.add(serie);
		}
		salidaCalculo = calculo.logica(con, entradaCalculo);
		valoresCuota.putAll(Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno"));
		
		for (CalcularMontoNumeroCuota.Resultado sc : salidaCalculo.P_OUT_RESULTADO) {
			if (!salidaCalculo.P_OUT_RESULTADO.get(0).CodigoMensaje.equals("OK")) {
				out.P_OUT_ERRORESCONTADOR++;
				hasVC = false;
				MensajeSalida mensajesSalida = new MensajeSalida();
				
				mensajesSalida.id = sc.IdInterno;
				mensajesSalida.tipo = "ERROR";
				mensajesSalida.codigo = sc.CodigoMensaje;
				mensajesSalida.descripcion = sc.Resultado;
								
				out.P_OUT_MENSAJES.add(mensajesSalida);
			}
		}
		if (!hasVC) {
			eliminarReinversionBloqueada(con,entrada.p_IdEmpresa, entrada.p_SolicitudesRescate, out.P_OUT_MENSAJES,entrada.p_SimularOperacion);
			return out;
		}
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(rescates_unicos.size()));
		List<Long> secuenciasIdAgrupador = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::IdAgrupador", Long.valueOf(rescates_unicos.size()));
		
		
		
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		
		ValidaPatrimonioEmisor validaPatrimonioEmisor = new ValidaPatrimonioEmisor();
		EntradaValidaPatrimonioEmisor entradaValidaPatrimonioEmisor = new EntradaValidaPatrimonioEmisor();
		entradaValidaPatrimonioEmisor.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidaPatrimonioEmisor.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidaPatrimonioEmisor.p_Fecha = fechaSistema;
		HashMap<String,BigDecimal> hashMontosPortafolioEmisor = new HashMap<String,BigDecimal>();
		
		List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		HashMap<Long,Object> rescates_unicos_v2 = new HashMap<Long,Object>();
		
		HashMap<KeyAporteMonto, BigDecimal> aportesAgrupadosPorSerieOperacion = new HashMap<KeyAporteMonto, BigDecimal>(); 
		for(InSolicitudesAporte rev_ttl : entrada.p_SolicitudesAporte){
			CalcularMontoNumeroCuota.Resultado rev_apo = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(rev_ttl.IdInterno * -1);
			KeyAporteMonto keyAporteMonto = new KeyAporteMonto();
			keyAporteMonto.IdSerieFondo = rev_ttl.IdSerieFondo;
			if(FondoMutuoUtils.esCanalIngresoWebServices(entrada.p_IdTipoCanalIngreso)) {
				keyAporteMonto.IdAgrupador = 0L;
			}else {
				keyAporteMonto.IdAgrupador = rev_ttl.IdAgrupador;
			}
			
			BigDecimal montoAporteSerieOperacion = aportesAgrupadosPorSerieOperacion.get(keyAporteMonto) == null ? BigDecimal.ZERO : aportesAgrupadosPorSerieOperacion.get(keyAporteMonto);
			montoAporteSerieOperacion = montoAporteSerieOperacion.add(rev_apo.Monto, MathContext.DECIMAL128);
			aportesAgrupadosPorSerieOperacion.put(keyAporteMonto, montoAporteSerieOperacion);
		}
		
		List<Long> folioContratoSolicitudes = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", (long) entrada.p_SolicitudesRescate.size());
		
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));

		for(InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate){
			InSolicitudesAporte apor = (InSolicitudesAporte) haportes.get(solicitud.IdInterno);
			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			TipoMovimientoFondoMutuo tipoMovimientoRescate = (TipoMovimientoFondoMutuo) htipoMovimiento.get(_c.Reinversion);
			TipoMovimientoFondoMutuo tipoMovimientoAporte = (TipoMovimientoFondoMutuo) htipoMovimiento.get(_c.AporteReinversion);
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			SerieFondoSolicitudes serieDestino = (SerieFondoSolicitudes) hseries.get(apor.IdSerieFondo);
			
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			RegimenSerieDeFondo rRescate = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			RegimenSerieDeFondo rAporte = (RegimenSerieDeFondo) hseriesRegimen.get(apor.IdSerieFondo + "_" +apor.IdRegimenTributario);
			ConfiguracionRegimenFondoMutuo confRegimen = (ConfiguracionRegimenFondoMutuo) hregimenConfiguraciones.get(solicitud.IdRegimenTributario + "_" + apor.IdRegimenTributario);
			
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			CalcularMontoNumeroCuota.Resultado vcApor = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno * -1);

//			vcApor.NumeroCuotas = vc.MontoDestino.divide(vcApor.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
//			vcApor.Monto = vc.Monto;  
//			vcApor.MontoDestino = vc.MontoDestino;
//			vcApor.MontoMonedaSistema = vc.MontoMonedaSistema;
				
			ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
			if(cliente != null) confPropositoPortafolio = (ConfiguracionPropositoPortafolio) hconfRegimenPortafolio.get(apor.IdRegimenTributario + "_" + cliente.Proposito);
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solValApor = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portValApor = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			//SE BUSCA REGIMEN CON MANDATO
			RegimenMandato rMandato = new RegimenMandato();
			for(RegimenMandato rm : regimenMandato){
				if(solicitud.IdSerieFondo.equalsIgnoreCase(rm.IdSerieFondo)){
					rMandato = rm;
				}
			}
			
			if(!rescates_unicos_v2.containsKey(solicitud.IdAgrupador)) {
				rescates_unicos_v2.put(solicitud.IdAgrupador, solicitud);
			}
			
			SolicitudAporteRescate sol = new SolicitudAporteRescate();
			
			//SE COMPLETA EL MOVIMIENTO
			sol.IdInterno = solicitud.IdInterno;
			sol.IdSolicitudAporteRescate = secuencias.get(rescates_unicos_v2.size()-1);
		 	sol.IdCliente = solicitud.IdCliente;
		 	sol.IdPortafolio = solicitud.IdPortafolio;
		 	sol.TipoSolicitud = _c.TipoSolicitudRescate;
		 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
			sol.IdTipoMovimientoFondoMutuo = _c.Reinversion;
			sol.IdDividendo = 0L;
			sol.IdSerieFondoDestino = serieDestino.IdSerieFondo;
			sol.IdEmisorDestino = haportes.size() == 1 ? apor.IdEmisor : "";
			sol.IdClienteDestino = 0L;
			sol.IdPortafolioDestino = 0L;
			sol.IndicadorMLT = apor.IndicadorMLT;
			sol.Overnight = _c.NO;
			sol.IdSolicitudAporteRescateRelacionado = 0L;
			sol.IdTipoCartera = "";
			sol.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			sol.FechaEmision = Fecha.toDate(fechaSistema);
			sol.FechaOriginalInversion = Fecha.toDate("0001-01-01");
			//sol.FechaIngresoRegistro = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa));
			sol.FechaIngresoRegistro = fechaHoraUTC;
			sol.IdMoneda = solicitud.IdMoneda;
			sol.Estado =  _c.EstadoIngresado;
			sol.IdMedioDePago = solicitud.IdMedioDePago;
			sol.Glosa = solicitud.Glosa;
			sol.IdEmpresa = entrada.p_IdEmpresa;
			sol.IdUsuario = entrada.p_IdUsuario;
			sol.IdModulo = entrada.p_IdModulo;
			sol.IdEmisorOrigen = serie != null ? serie.IdEmisor : "";
			sol.SolicitudBatch = solicitud.SolicitudBatch;
			sol.OrigenOperacion = solicitud.OrigenOperacion;
			sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
			sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
			sol.IdRegimenTributario = solicitud.IdRegimenTributario;
			sol.IngresoAutomatico = solicitud.IngresoAutomatico;
			sol.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso != null ? entrada.p_IdTipoCanalIngreso : _c.CanalAgente;
			sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : folioContratoSolicitudes.get(entrada.p_SolicitudesRescate.indexOf(solicitud));
			sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
			sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
			sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
			sol.EstadoProceso = _c.EstadoPendiente;
			sol.Monto = BigDecimal.ZERO;
			sol.NumeroCuotas = BigDecimal.ZERO;
			sol.ValorCuota = BigDecimal.ZERO;
			sol.CostoUF = BigDecimal.ZERO;
		    sol.CostoMonedaLocal = BigDecimal.ZERO; 
			sol.ValorUFFechaInversion = BigDecimal.ZERO; 
			sol.ParidadFechaInversion = BigDecimal.ZERO;
			sol.IdMonedaSistema = "";
			sol.MontoMonedaSistema = BigDecimal.ZERO;
			sol.MontoPagoLocal = BigDecimal.ZERO; 
			sol.MontoPago = BigDecimal.ZERO;
			sol.MontoComision = BigDecimal.ZERO;
			sol.MontoComisionLocal = BigDecimal.ZERO;
			sol.IdAgrupador = secuenciasIdAgrupador.get(rescates_unicos_v2.size()-1);
			sol.IdAgrupadorInterno = solicitud.IdAgrupador;
			sol.Switch = _c.NO;
			sol.Porcentaje = solicitud.Porcentaje;
			if(vc != null){
				sol.Monto = vc.Monto;
				sol.NumeroCuotas = vc.NumeroCuotas;
				sol.ValorCuota = vc.ValorCuota;
				sol.CostoUF = vc.CostoEnFactorAjuste;
			    sol.CostoMonedaLocal = vc.MontoDestino; 
				sol.ValorUFFechaInversion = vc.ValorFactorAjuste; 
				sol.ParidadFechaInversion = vc.ValorParidad;
				sol.IdMonedaSistema = vc.IdMonedaSistema;
				sol.MontoMonedaSistema = vc.MontoMonedaSistema;
				sol.MontoPagoLocal = vc.MontoDestino; 
				sol.MontoPago = vc.Monto;
				sol.MontoInversion = vc.Monto;
				sol.CostoMonedaLocalInversion = vc.MontoDestino;
				sol.ValorCuotaInversion = vc.ValorCuota;
				sol.ParidadFechaCertificacion = vc.ValorParidad;
				sol.NumeroCuotasInversion = vc.NumeroCuotas;
			}
			if(fecha != null){
				sol.FechaAceptacion = fecha.FechaAceptacion;
				sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : fecha.FechaValorCuota;
			    sol.FechaCertificacion = fecha.FechaCertificacion;
			    sol.FechaLiquidacion = fecha.FechaLiquidacion;
			}
			if(rRescate != null){
				switch(rRescate.GrupoRegimen){
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
			//Omite Rescate de fondo BF cuando el numero de cuotas es igual a cero.
			if((BigDecimal.ZERO.compareTo(sol.NumeroCuotas) == 0 || BigDecimal.ZERO.compareTo(sol.Monto) == 0) && "BF".equalsIgnoreCase(sol.IdRegimenTributario)) {
				continue;
			}
			//Omite Aporte de fondo BF cuando el numero de cuotas es igual a cero.
			if((vcApor != null)&&((BigDecimal.ZERO.compareTo(vcApor.NumeroCuotas) == 0 || BigDecimal.ZERO.compareTo(vcApor.Monto) == 0) && "BF".equalsIgnoreCase(sol.IdRegimenTributario))) {
	               continue;
			  
			}
			solicitudesProcesar.add(sol);

			//VALIDACIONES RESCATE
		    solVal.IdInterno = sol.IdInterno;
			solVal.IdCliente = sol.IdCliente;
			solVal.IdPortafolio = sol.IdPortafolio;
			solVal.IdSerieFondo = sol.IdSerieFondo;
			solVal.IdTipoMovimientoFondoMutuo = tipoMovimientoRescate != null ? tipoMovimientoRescate.IdTipoMovimientoFondoMutuo : "";
			solVal.Monto = sol.Monto;
			solVal.NumeroCuotas = sol.NumeroCuotas;
			solVal.IdEstadoSerie = "";
			solVal.EsFechaValidaAporte = _c.SI;
			solVal.EsFechaValidaRescate = _c.SI;
			solVal.EsDiferido = _c.SI;
			solVal.EsMontoMaximo = _c.SI;
			solVal.EsMontoMinimo = _c.SI;
			solVal.ValorCuota = sol.ValorCuota;
			solVal.Overnight = _c.NO;
			solVal.EstadoLiquidez = "";
			solVal.EsMoneyMarket = _c.SI;
			solVal.EsFamiliaEquivalente = _c.SI;
			solVal.EsPortafolioRegimenValido = _c.SI;
			solVal.EsRegimenValido = _c.SI;
			solVal.PermiteRescateMandato = _c.SI;
			solVal.PermiteCambioGrupo = _c.SI;
			solVal.EsPortafolioValido = _c.SI;
			solVal.EsCuentaInversionValido = _c.SI;
			solVal.PermiteAporte = _c.SI;
			solVal.EsRegimenDestinoValido = _c.SI;
			if(serie != null){
				solVal.IdEstadoSerie = serie.IdEstado;
				solVal.RestringeAporte = serie.RestringeAporte;
				solVal.RestringeRescate = serie.RestringeRescate;
				solVal.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
				solVal.EstadoBloqueo = serie.BloqueoRescate;
				solVal.EstadoLiquidez = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
				solVal.EsFondoValido = serie.IdSerieFondo.equalsIgnoreCase(serieDestino.IdSerieFondo) ? _c.NO : _c.SI;
				solVal.PermiteCambioFondo = serie.CambioFondo;
				if(!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && _c.NO.equalsIgnoreCase(serie.Diferido) && _c.NO.equals(rescateGeneraVentaSpotDolar)){
					solVal.EsDiferido = _c.NO;
				}
			}
			
			if(rRescate == null) {
				solVal.EsRegimenValido = _c.NO;
			}
			
			if(solicitud.IdRegimenTributario != null) {
				if(!solicitud.IdRegimenTributario.equalsIgnoreCase(apor.IdRegimenTributario) && confRegimen == null) {
					solVal.EsRegimenDestinoValido = _c.NO;
				}				
			}
			
			if(_c.SI.equalsIgnoreCase(apor.IndicadorMLT) && "N".equalsIgnoreCase(rRescate.PermiteMLT)){	
				solVal.PermiteRescateMandato = _c.NO;
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
			
			solVal.IdTipoOperacion=_c.TipoOperacionRescateFondoMutuo;
			solVal.IdTipoProducto=_c.TipoOperacion;
			entradaCumpleRequisitos.p_Portafolios.add(portVal);
			entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
			
			//VALIDACIONES APORTE
			solValApor.IdInterno = sol.IdInterno * -1;
			solValApor.IdCliente = sol.IdCliente;
			solValApor.IdPortafolio = sol.IdPortafolio;
			solValApor.IdSerieFondo = serieDestino != null ? serieDestino.IdSerieFondo : "";
			solValApor.IdTipoMovimientoFondoMutuo = tipoMovimientoAporte != null ? tipoMovimientoAporte.IdTipoMovimientoFondoMutuo : "";
			solValApor.Monto = BigDecimal.ZERO;
			solValApor.NumeroCuotas = BigDecimal.ZERO;
			solValApor.ValorCuota = BigDecimal.ZERO;
			solValApor.IdEstadoSerie = "";
			solValApor.EsFechaValidaAporte = _c.SI;
			solValApor.EsFechaValidaRescate = _c.SI;
			solValApor.EsDiferido = _c.SI;
			solValApor.Overnight = _c.NO;
			solValApor.EstadoLiquidez = "";
			solValApor.EsMoneyMarket = _c.SI;
			solValApor.EsFamiliaEquivalente = _c.SI;
			solValApor.EsPortafolioRegimenValido = _c.SI;
			solValApor.EsRegimenValido = _c.SI;
			solValApor.PermiteRescateMandato = _c.SI;
			solValApor.PermiteCambioGrupo = _c.SI;
			solValApor.EsPortafolioValido = _c.SI;
			solValApor.EsCuentaInversionValido = _c.SI;
			solValApor.IdTipoOperacion= _c.AporteFondoMutuo;
			solValApor.IdTipoProducto=_c.TipoOperacion;
			solValApor.PermiteAporte = _c.SI;
			solValApor.EsRegimenDestinoValido = _c.SI;
			solValApor.EsMontoMinimo = _c.SI;
			if(vcApor != null){
				solValApor.Monto = vcApor.Monto;
				solValApor.NumeroCuotas = vcApor.NumeroCuotas;
				solValApor.ValorCuota = vcApor.ValorCuota;
			}
			if(serieDestino != null){
				solValApor.PermiteCambioFondo = serieDestino.CambioFondo;
				solValApor.IdEstadoSerie = serieDestino.IdEstado;
				solValApor.RestringeAporte = serieDestino.RestringeAporte;
				solValApor.RestringeRescate = serieDestino.RestringeRescate;
				solValApor.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serieDestino.EstadoAporteComercial : serieDestino.EstadoAporteBackoffice;
				solValApor.EstadoBloqueo = serieDestino.BloqueoAporte;
				solValApor.EsFondoValido = _c.SI;
				
				KeyAporteMonto keyAporteMonto = new KeyAporteMonto();
				keyAporteMonto.IdSerieFondo = serieDestino.IdSerieFondo;
				if(FondoMutuoUtils.esCanalIngresoWebServices(entrada.p_IdTipoCanalIngreso)) {
					keyAporteMonto.IdAgrupador = 0L;
				}else {
					keyAporteMonto.IdAgrupador = sol.IdAgrupador;
				}
				
				BigDecimal montoTotalAporte = aportesAgrupadosPorSerieOperacion.get(keyAporteMonto);
				if (montoTotalAporte == null) {
					solValApor.EsMontoMaximo = vcApor == null || (serieDestino.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && vcApor.Monto.compareTo(serieDestino.MontoMaximo) == 1) ? _c.NO : _c.SI;
					if(rAporte != null && _c.SI.equals(rAporte.ValidaMontoMinimoInstrumento)) {
						solValApor.EsMontoMinimo = vcApor == null || (serieDestino.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && vcApor.Monto.compareTo(serieDestino.MontoMinimo) == -1) ? _c.NO : _c.SI;
					}
					
				}
				else {
					solValApor.EsMontoMaximo = vcApor == null || (serieDestino.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && montoTotalAporte.compareTo(serieDestino.MontoMaximo) == 1) ? _c.NO : _c.SI;
					if(rAporte != null && _c.SI.equals(rAporte.ValidaMontoMinimoInstrumento)) {
						solValApor.EsMontoMinimo = vcApor == null || (serieDestino.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && montoTotalAporte.compareTo(serieDestino.MontoMinimo) == -1) ? _c.NO : _c.SI;
					}
				}
				
				solVal.EstadoLiquidez = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serieDestino.EstadoLiquidezComercial : serieDestino.EstadoLiquidezBackoffice;
				solVal.EsFondoValido = serie == null || serie.IdSerieFondo.equalsIgnoreCase(serieDestino.IdSerieFondo) ? _c.NO : _c.SI;
				if(!apor.IdMoneda.equalsIgnoreCase(serieDestino.IdMoneda) && _c.NO.equalsIgnoreCase(serieDestino.Diferido) && _c.NO.equals(rescateGeneraVentaSpotDolar)){
					solVal.EsDiferido = _c.NO;
				}
				//PATRIMONIOS ALTOS
				BigDecimal montoPortafolioPorEmisor = BigDecimal.ZERO;
				montoPortafolioPorEmisor = hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + serieDestino.IdEmisor);
				if(montoPortafolioPorEmisor == null) montoPortafolioPorEmisor = BigDecimal.ZERO;
				
				if(serie.IdMoneda != null && serieDestino.IdMoneda != null && "CLP".equalsIgnoreCase(serieDestino.IdMoneda)) {
					montoPortafolioPorEmisor = montoPortafolioPorEmisor.add(sol.MontoMonedaSistema.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128), MathContext.DECIMAL128), MathContext.DECIMAL128);
				} else if(serie.IdMoneda != null && serieDestino.IdMoneda != null && !"CLP".equalsIgnoreCase(serieDestino.IdMoneda)){
					montoPortafolioPorEmisor = solValApor.Monto;
				}
		
				hashMontosPortafolioEmisor.put(sol.IdCliente + "_" + serieDestino.IdEmisor, montoPortafolioPorEmisor);
				if(_c.SI.equals(serieDestino.PatrimonioAltos)) {
					ValidaPatrimonioEmisor.Solicitud valPatrimonio = new ValidaPatrimonioEmisor.Solicitud();
					valPatrimonio.IdInterno = sol.IdInterno;
					valPatrimonio.IdCliente = sol.IdCliente;
					valPatrimonio.IdSerieFondo = serieDestino.IdSerieFondo;
					valPatrimonio.IdEmisor = serieDestino.IdEmisor;
					//Se agrega el monto del aporte para validar patrimonio minimo emisor
					//Si serie de Emisor de Origen es igual a serie de Emisor de Destino, Monto Aporte no se suma a Monto pratrimonio 
					valPatrimonio.MontoPatrimonio = serie.IdEmisor.equals(serieDestino.IdEmisor) ? valPatrimonio.MontoPatrimonio.ZERO : solicitud.MontoPatrimonio.add(solValApor.Monto);
					valPatrimonio.ValorCuota = sol.ValorCuota;
					entradaValidaPatrimonioEmisor.p_Solicitudes.add(valPatrimonio);
				}
			}
			if(rAporte == null){
				solValApor.EsRegimenValido = _c.NO;
			}else {
				solValApor.PermiteAporte = rAporte.PermiteAporte;
			}
			
			if(confPropositoPortafolio == null) solValApor.EsPortafolioRegimenValido = _c.NO;
			
			if(_c.SI.equalsIgnoreCase(apor.IndicadorMLT) && ((rAporte == null || rRescate == null) || (!rAporte.GrupoRegimen.equalsIgnoreCase(rRescate.GrupoRegimen) || rMandato == null))){
				solVal.PermiteRescateMandato = _c.NO;
			}
			
			if(_c.SI.equalsIgnoreCase(apor.IndicadorMLT) && "N".equalsIgnoreCase(rAporte.PermiteMLT)){	
				solValApor.PermiteRescateMandato = _c.NO;
			}
			
			if(cliente != null){
				portValApor.IdInterno = sol.IdInterno * -1;
				portValApor.IdPortafolio = sol.IdPortafolio;
				portValApor.IdPortafolioReal = cliente.IdPortafolio;
				portValApor.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
				portValApor.IdCliente = sol.IdCliente;
				portValApor.IdClienteReal = cliente.IdCliente;
				portValApor.IdEstadoCliente = cliente.IdEstadoCliente;
				portValApor.DocumentoIdentidad = cliente.DocumentoIdentidad;
				portValApor.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
				portValApor.IdClienteDestino = sol.IdClienteDestino;
				portValApor.IdClienteRealDestino = 0L;
				portValApor.IdPortafolioDestino = sol.IdPortafolio;
				portValApor.IdPortafolioRealDestino = 0L;
				portValApor.IdEstadoClienteDestino = "";
				portValApor.IdEstadoPortafolioDestino = "";
			}
			
			solValApor.IdTipoOperacion=_c.TipoOperacionAporteFondoMutuo;
			solValApor.IdTipoProducto=_c.TipoOperacion;
			entradaCumpleRequisitos.p_Portafolios.add(portValApor);
			entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solValApor);
					
			//SE PREPARA SALIDA
			ResultadoSolicitud solicitudSalida = new ResultadoSolicitud();
			solicitudSalida = Utils.copyPojo(sol, solicitudSalida);
			if(serie !=  null){
				solicitudSalida.IdFondo = serie.IdFondo;
				solicitudSalida.Nemotecnico = serie.Nemotecnico;
				solicitudSalida.NombreFondo = serie.NombreFondo;
				solicitudSalida.NombreCorto = serie.NombreCortoTipoFondo;
				solicitudSalida.NombreSerieFondo = serie.NombreSerieFondo;
				solicitudSalida.CodigoSerie = serie.CodigoSerie;
				solicitudSalida.IdEmisorOrigen = serie.IdEmisor;
			}
			if(cliente != null){
				solicitudSalida.DocumentoIdentidad = cliente.DocumentoIdentidad;
				solicitudSalida.CodigoPortafolio = cliente.CodigoPortafolio;
				solicitudSalida.NombreCliente = cliente.NombreCompleto;
			}
			solicitudSalida.MontoTransferencia = sol.Monto;
			solicitudSalida.NumeroCuotaTransferencia = sol.NumeroCuotas;
			solicitudSalida.FechaAceptacion = Fecha.stringToSqlDate(sol.FechaAceptacion);
			solicitudSalida.FechaOriginalInversion = Fecha.stringToSqlDate(sol.FechaOriginalInversion);
			solicitudSalida.FechaIngresoRegistro = Fecha.toStringTimeStamp(sol.FechaIngresoRegistro);
			solicitudSalida.FechaLiquidacion = Fecha.stringToSqlDate(sol.FechaLiquidacion);
			solicitudSalida.FechaCertificacion = Fecha.stringToSqlDate(sol.FechaCertificacion);
			solicitudSalida.FechaValorCuota = Fecha.stringToSqlDate(sol.FechaValorCuota);
			if(vcApor != null){
				solicitudSalida.MontoAporte = vcApor.Monto;
				solicitudSalida.NumeroCuotasAporte = vcApor.NumeroCuotas;
			}
			solicitudSalida.IdEmisorAporte = apor.IdEmisor;
			resultadoTemp.add(solicitudSalida);
		}
		//for hasta aqui
		
		HashMap<Long,Object> hSolicitudSalida = Utils.List2Hash(resultadoTemp, "IdInterno");
		
		//repetidos
		Long registrosPorProcesar = 0L;
		SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate salidaCumpleRequisitos = cumpleRequisitos.logica(con, entradaCumpleRequisitos); 
		for(MensajeSalida mje : salidaCumpleRequisitos.P_OUT_MENSAJES){
			mje.id = Math.abs(mje.id);
			out.P_OUT_MENSAJES.add(mje);
			if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
				out.P_OUT_ERRORESCONTADOR++;
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if(sol.IdInterno.equals(mje.id)){
						sol.EstadoProceso = _c.EstadoRechazado;
						break;
					}
				}
			}
		}
		
		//no repetidos
		List<SolicitudAporteRescate> solicitudesProcesarFinal = new ArrayList<SolicitudAporteRescate>();
		List<Long> ids = new ArrayList<Long>();
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			
			if(!ids.contains(sol.IdSolicitudAporteRescate)) {
				solicitudesProcesarFinal.add(sol);
				ids.add(sol.IdSolicitudAporteRescate);
			}
			
			if(rescates_unicos.containsKey(sol.IdAgrupadorInterno)){
				rescates_unicos.remove(sol.IdAgrupadorInterno);
			}
		}
		
		//PATROMINO EMISOR
		if(entradaValidaPatrimonioEmisor.p_Solicitudes.size() > 0) {
			SalidaValidaPatrimonioEmisor salidaValidaPatrimonioEmisor = validaPatrimonioEmisor.logica(con, entradaValidaPatrimonioEmisor);
			for(MensajeSalida mje : salidaValidaPatrimonioEmisor.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(mje);
					for(SolicitudAporteRescate sol : solicitudesProcesar){
						if(sol.IdInterno.equals(mje.id)){
							sol.EstadoProceso = _c.EstadoRechazado;
							break;
						}
					}
				}
			}
		}
		
		registrosPorProcesar = solicitudesProcesar.parallelStream().filter(sol->_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)).count();
		
		if(registrosPorProcesar.equals(0L)){
			eliminarReinversionBloqueada(con,entrada.p_IdEmpresa, entrada.p_SolicitudesRescate, out.P_OUT_MENSAJES,entrada.p_SimularOperacion);
			return out;
		}
		//VALIDAMOS CUSTODIA
		ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
		entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujo.p_IdModulo = entrada.p_IdModulo;
		entradaFlujo.p_IdPais = idPaisEmpresa;
		entradaFlujo.p_FechaSaldo = fechaSistema;
		entradaFlujo.p_DiasProyeccion = 0;
		List<Long> fPortafolio = new ArrayList<Long>();
		Set<String> fCustodiaInstrumento = new HashSet<>();
		for(SolicitudAporteRescate sol : solicitudesProcesarFinal){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
				if(!fPortafolio.contains(sol.IdPortafolio)){
					fPortafolio.add(sol.IdPortafolio);
					ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
					cartera.IdCliente = sol.IdCliente;
					cartera.IdPortafolio = sol.IdPortafolio;
					entradaFlujo.p_Carteras.add(cartera);
				}
				String idInstrumento = serie.IdInstrumento != null ? serie.IdInstrumento.toString() : "";
				String idRegimenTributario = sol.IdRegimenTributario != null ? sol.IdRegimenTributario : "";
				String clave = idInstrumento + "_" + idRegimenTributario;
				if (fCustodiaInstrumento.add(clave)){
					ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
					instrumento.IdInstrumento = serie.IdInstrumento;
					instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario;
					entradaFlujo.p_Instrumentos.add(instrumento);
				}
			}
		}
				
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = flujo.logica(con, entradaFlujo);
		
		//AGF
		CrearSolicitudAGF crearSolicitudAGF = new CrearSolicitudAGF();
		EntradaCrearSolicitudAGF entradaCrearAGF = new EntradaCrearSolicitudAGF();
		
		//SE VALIDA CUSTODIA, SI HAY SE INSERTA Y MUEVE CUSTODIA
		for(SolicitudAporteRescate sol : solicitudesProcesarFinal){
			if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)) continue;
			
			BigDecimal custodia = BigDecimal.ZERO;
			BigDecimal monto =  BigDecimal.ZERO;
			
			for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
				if(FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(flujoCustodia, sol)){
					custodia = flujoCustodia.CantidadDisponible;
					monto = flujoCustodia.CantidadDisponible.multiply(sol.ValorCuota, MathContext.DECIMAL128);
					continue;
				}
			}
			//VALIDAMOS CUSTODIA, SI NO HAY ERROR Y SALIMOS. SI HAY INSERTAMOS Y MOVEMOS CUSTODIA 
			if(sol.NumeroCuotas.compareTo(custodia) == 1){
				sol.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
				parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
				parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
				parametrosMensaje.put("CantidadCustodia",custodia.toPlainString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FM_CLIENTE_SIN_CUSTODIA",parametrosMensaje,con,entrada.p_IdEmpresa));
				//La carga masiva valida la custodia de todos los registros
				if(_c.OrigenCargaMasivaFM.equalsIgnoreCase(entrada.p_IdOrigenProceso)) {
					continue;
				}else {
					break;
				}
			}
			
			for(InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate){
				InSolicitudesAporte apor = (InSolicitudesAporte) haportes.get(solicitud.IdInterno);
				SerieFondoSolicitudes serieDestino = (SerieFondoSolicitudes) hseries.get(apor.IdSerieFondo);
				HashMap<String,String> params2 = new HashMap<String,String>();
				params2.put("IdInstrumento",serieDestino.IdSerieFondo);
				
				if(serieDestino.PatrimonioMinimo.compareTo(sol.Monto.add(monto, MathContext.DECIMAL128)) == 1){
					sol.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_PATRIMONIO_MINIMO_INSUFICIENTE",params2,con,entrada.p_IdEmpresa));
					params2.clear();
					break;
				}
			}
			
		}
		
		List<ReinversionSerieFondoDestino> solicitudesReinversion = new ArrayList<ReinversionSerieFondoDestino>();
		List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion)){
			for(SolicitudAporteRescate sol : solicitudesProcesarFinal){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					if(_c.NO.equalsIgnoreCase(sol.SolicitudBatch)){
						//AGF
						CrearSolicitudAGF.Solicitudes solAGF = new CrearSolicitudAGF.Solicitudes();
						solAGF.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
						entradaCrearAGF.p_Solicitudes.add(solAGF);
					}
					solicitudesInsertar.add(sol);
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
			for(SolicitudAporteRescate sol : solicitudesInsertar) {
				for(Map.Entry<Long, Object> apo : haportes.entrySet()) {
					InSolicitudesAporte aporte = (InSolicitudesAporte) apo.getValue();
					if(aporte.IdAgrupador.equals(sol.IdAgrupadorInterno)) {
						ReinversionSerieFondoDestino reinversion = new ReinversionSerieFondoDestino();
						SerieFondoSolicitudes serieAporte = (SerieFondoSolicitudes) hseries.get(aporte.IdSerieFondo);
						SerieFondoSolicitudes serieRescate = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
						ResultadoFecha fecha = (ResultadoFecha) hfechasAporte.get(aporte.IdInterno);
						reinversion.IdSerieFondoDestino = aporte.IdSerieFondo;
						reinversion.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
						reinversion.Porcentaje = aporte.Porcentaje;
						
						
						reinversion.PorcentajeReal = aporte.PorcentajeReal;
						
						reinversion.IdRegimenTributario = aporte.IdRegimenTributario;
						if(serieAporte != null && serieRescate != null && _c.SI.equals(serieRescate.FondoExtranjero) && _c.SI.equals(serieAporte.FondoExtranjero) && serieRescate.IdEmisor.equals(serieAporte.IdEmisor)) {
							reinversion.Switch = _c.SI;
							sol.Switch= _c.SI;
						}else {
							reinversion.Switch = _c.NO;
							sol.Switch= _c.NO;
						}
						reinversion.FechaFinalReinversion = fecha.FechaCertificacion;

						solicitudesReinversion.add(reinversion);
					}
				}
			}
			if(solicitudesReinversion.size() > 0) {
				sql.batch(con, SQL_INSERT_REINVERSION_SOLICITUD, solicitudesReinversion, ReinversionSerieFondoDestino.class);
			}
			//INSERTAMOS LAS SIN ERRORES
			if(solicitudesInsertar.size() > 0){
				sql.batch(con, SQL_INSERT_SOLICITUD, solicitudesInsertar, SolicitudAporteRescate.class);
				//AGF
				if(entradaCrearAGF.p_Solicitudes.size() > 0){
					entradaCrearAGF.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCrearAGF.p_IdUsuario = entrada.p_IdUsuario;
					entradaCrearAGF.p_IdModulo = entrada.p_IdModulo;
					entradaCrearAGF.p_EsManual = _c.NO;
					SalidaCrearSolicitudAGF salidaAGF = crearSolicitudAGF.logica(con, entradaCrearAGF);
					out.P_OUT_ERRORESCONTADOR += salidaAGF.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES.addAll(salidaAGF.P_OUT_MENSAJES);
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
						log.IdTipoRegistro = "SOLICITUD_RESCATE_REINVERSION";
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
				//si es reinversion 1 a N agregamos mensaje
				if(solicitudesInsertar.get(0).IdAgrupador != null && FondoMutuoUtils.esCanalIngresoDistintoAWebServices(entrada.p_IdTipoCanalIngreso)) {
					
					params.put("IdPortafolio", portafolios);
					logger.debug("INICIO::SQL_SELECT_PORTAFOLIO_APV");
					List<PortafolioAPV> portafolioAPV = sql.query(con, SQL_SELECT_PORTAFOLIO_APV, params, PortafolioAPV.class);
					logger.debug("FIN::SQL_SELECT_PORTAFOLIO_APV");
					params.clear();
					HashMap<Long,Object> hportafolioAPV =  Utils.List2Hash(portafolioAPV, "IdPortafolio");
					InscribirProcesoBatch procesoBatch = new InscribirProcesoBatch();
					InscribirProcesoBatchEntrada entradaProcesoBatch = new InscribirProcesoBatchEntrada();
					List<Parametro> parametros = new ArrayList<Parametro>();
					//VALIDAMOS SOLICITUDES CORRECTAS A INSERTAR
					if(_c.EstadoPendiente.equalsIgnoreCase(solicitudesProcesar.get(0).EstadoProceso)){
						if(_c.NO.equalsIgnoreCase(solicitudesProcesar.get(0).SolicitudBatch)){
							//AGF
							CrearSolicitudAGF.Solicitudes solAGF = new CrearSolicitudAGF.Solicitudes();
							solAGF.IdSolicitudAporteRescate = solicitudesProcesar.get(0).IdSolicitudAporteRescate;
							entradaCrearAGF.p_Solicitudes.add(solAGF);
						}

						out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(solicitudesProcesar.get(0).IdInterno));
						PortafolioAPV papv = (PortafolioAPV) hportafolioAPV.get(solicitudesProcesar.get(0).IdPortafolio);
						if(papv != null){
							Long contadorParam = 0L;
							List<String> idsSolicitudAporteRescate = new ArrayList<String>();
							idsSolicitudAporteRescate = solicitudesProcesar.stream()
																           .collect(Collectors.groupingBy(
																               s -> s.FolioContrato,
																               Collectors.mapping(s -> s.IdSolicitudAporteRescate.toString(), Collectors.toSet()) 
																           ))
																           .values()
																           .stream()
																           .map(s -> String.join(";", s))
																           .collect(Collectors.toList());
							
							for(String idSolicitudes : idsSolicitudAporteRescate) {
								parametros.add(new Parametro(contadorParam,"IdSolicitudAporteRescate",idSolicitudes, null)); 
								contadorParam++;
							}
						}
					}
				
					for(Parametro param : parametros){

						entradaProcesoBatch.p_Parametro = new ArrayList<Parametro>();
						entradaProcesoBatch.p_TipoPrograma = new ArrayList<InTipoPrograma>();
						InTipoPrograma inTipoPrograma = new InTipoPrograma();
						inTipoPrograma.IdInterno = 0L;
						inTipoPrograma.IdTipoPrograma = "INGRESAR_BONIFICACION_FISCAL_REINVERSION";
						inTipoPrograma.SegundosDesfase = 0L;
						entradaProcesoBatch.p_TipoPrograma.add(inTipoPrograma);
					    entradaProcesoBatch.p_Parametro.add(new Parametro(0L,param.NombreParametro, param.Valor, null));
						entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdEmpresa",entrada.p_IdEmpresa.toString(), null));
						entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdUsuario",entrada.p_IdUsuario.toString(), null));
						entradaProcesoBatch.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaProcesoBatch.p_IdUsuario = entrada.p_IdUsuario;
						entradaProcesoBatch.p_IdModulo = entrada.p_IdModulo;
						procesoBatch.logica(con, entradaProcesoBatch);
					}
			
				}
				
				eliminarReinversionBloqueada(con,entrada.p_IdEmpresa, entrada.p_SolicitudesRescate, out.P_OUT_MENSAJES,entrada.p_SimularOperacion);
				
				//Mensajes de Salida para Reinversiones 1 a N
				for (SolicitudAporteRescate sol : solicitudesInsertar) {
					if(sol.IdAgrupador != null && !_c.CanalWEB.equals(entrada.p_IdTipoCanalIngreso) && !(out.P_OUT_ERRORESCONTADOR > 0)) {
						HashMap<String,String> hashMensaje = new HashMap<String,String>();
						hashMensaje.put("secuencia", sol.IdSolicitudAporteRescate.toString());
						hashMensaje.put("folioAgrupador", sol.IdAgrupador.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_REINVERSION_1N_CREADA",hashMensaje,con,entrada.p_IdEmpresa));
					}
				}
			}else {
				eliminarReinversionBloqueada(con,entrada.p_IdEmpresa, entrada.p_SolicitudesRescate, out.P_OUT_MENSAJES,entrada.p_SimularOperacion);
			}
		}else{
			for(SolicitudAporteRescate sol : solicitudesProcesarFinal){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
		}
		
		
		logger.info("FIN::CrearSolicitudRescateReinversion");
		return out;
	}
	
	private HashMap<Long, Object> obtenerFechaAporte(Connection con, EntradaCrearSolicitudRescateReinversion entrada, Long idPaisEmpresa, HashMap<String, Object> hseries, SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo, HashMap<String, Object> hmonedasFondo, HashMap<Long,Object> hfechas, String respetaValutasCargaMasiva, Map<Long, String> solicitudesBatches, HashMap<Long,Object> hrescates) throws Exception {
		logger.info("INICIO::CrearSolicitudRescateReinversion.obtenerFechaAporte");
		long inicioServicio = System.currentTimeMillis();
		
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String idMonedaSistemaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		
		CalcularFechaSolicitudAporteRescate calculoFechas = new CalcularFechaSolicitudAporteRescate();
		EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
		entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFechas.p_IdPais = idPaisEmpresa;
		
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){										
			SerieFondoSolicitudes serieOrigen   = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondoOrigen);
			SerieFondoSolicitudes serieDestino  = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
							
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S")  && serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
				solicitud.TipoReinversion = "SWITCH";
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S")  && !serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
				solicitud.TipoReinversion = "INTERNACIONAL";
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("N"))
				solicitud.TipoReinversion = "NACIONAL";
			if (serieOrigen.FondoExtranjero.equalsIgnoreCase("N"))
				solicitud.TipoReinversion = "NACIONAL";
		}
	
		
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularFechaSolicitudAporteRescate.InFechas fecha = new CalcularFechaSolicitudAporteRescate.InFechas();
			InSolicitudesRescate res = (InSolicitudesRescate) hrescates.get(solicitud.IdInterno);
			ResultadoFecha fechaRescate = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			solicitud.SolicitudBatch = solicitudesBatches.get(solicitud.IdInterno);
			
			if(serie != null){
				fecha.IdInterno = solicitud.IdInterno;
				fecha.IdSerieFondo = solicitud.IdSerieFondo;
		    	if(solicitud.IdMoneda.equalsIgnoreCase(res.IdMoneda)) {
		    		fecha.FechaEmision = idMonedaSistema.equalsIgnoreCase(serie.IdMoneda) || idMonedaSistemaAdicional.equalsIgnoreCase(serie.IdMoneda)  ? Fecha.stringToSqlDate(fechaRescate.FechaLiquidacion) : Fecha.stringToSqlDate(fechaRescate.FechaCertificacion);
		    	}else {
		    		fecha.FechaEmision = Fecha.stringToSqlDate(fechaRescate.FechaLiquidacion);
		    	}
				if(_c.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && _c.NO.equals(respetaValutasCargaMasiva)){
					fecha.EstadoFondo = _c.Abierto;
					fecha.EstadoLiquidacion = _c.Abierto;
					fecha.NumeroDiasRetencion = 0L;
					fecha.NumeroValutaValorCuota = 0L;
					fecha.NumeroValutaCertificacion = 0L;
					fecha.NumeroValutaCurse = 0L;
					fecha.SolicitudBatch = _c.SI;
				}else{
					fecha.EstadoFondo = _c.Abierto;
					fecha.NumeroDiasRetencion = solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") ? 2 : (solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL") ? 4 : serie.DiasRetencionesAporte);
					fecha.NumeroValutaValorCuota = (solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ? 1 : serie.DiasValutaValorCuotaAporte;
					fecha.NumeroValutaCertificacion = (solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ?  2 : serie.DiasValutaCertificacionAporte;
					fecha.NumeroValutaCurse = (solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL")) ? 0 : serie.DiasValutaCurseAporte;
					fecha.EstadoLiquidacion = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
					fecha.Switch = solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") ? _c.SI : _c.NO;
					fecha.FechaCertificacionRescateOrigen = Fecha.stringToSqlDate(fechaRescate.FechaCertificacion);
					fecha.FechaLiquidacionRescateOrigen = Fecha.stringToSqlDate(fechaRescate.FechaLiquidacion);
					fecha.FechaValorCuotaRescateOrigen = Fecha.stringToSqlDate(fechaRescate.FechaValorCuota);
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
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSolicitudRescateReinversion.obtenerFechaAporte {}", finServicio - inicioServicio);
		
		return Utils.List2Hash(salidaFechas.P_FECHASAPORTERESCATE, "IdInterno");
	}
	
	private void eliminarReinversionBloqueada(Connection con,Long idEmpresa,List<?> inEntradaReinversion,List<MensajeSalida> P_OUT_MENSAJES,String simula) throws Exception {
		logger.debug("INICIO::eliminarReinversionBloqueada");
		HashMap<String,Object> paramsSp = new HashMap<String,Object>();
		Long inicioMetodo = System.currentTimeMillis();
		
		String simular = simula!=null?simula:"N";
		boolean simulaOperacion = "S".equalsIgnoreCase(simular);
		//SimulaOperacion al tener S no deberia eliminar registro a la tabla de bloqueo
		if (inEntradaReinversion.size() > 0 ) {
			ArrayList <DesbloquearReinversion> Reinversiones = new ArrayList <DesbloquearReinversion>();
			SQL _sql = new SQL();
			
			for(Object reinversion: inEntradaReinversion) {
				DesbloquearReinversion desReinversion = new DesbloquearReinversion();
				InSolicitudesRescate reinve = (InSolicitudesRescate) reinversion;
				desReinversion.IdBloqueaReinversion = reinve.IdBloqueoRescate;
				Reinversiones.add(desReinversion);
				
			}
			
			if(!Reinversiones.isEmpty() && !simulaOperacion) {
				try {
					paramsSp.put("p_BloqueoProcesarReinversion", Reinversiones);
					logger.debug("INICIO::SQL_DELETE_BLOQUEO_REINVERSION");
					_sql.call(con, SQL_CALL_ELIMINAR_BLOQUEO_REINVERSION, paramsSp, new HashMap<String,Class>());
					logger.debug("FIN::SQL_DELETE_BLOQUEO_REINVERSION");
				}catch(SQLException sqle) {
					P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DELETE_BLOQUEO_REINVERSION",new HashMap<String,String>(),con,idEmpresa));
				}finally {
					SQL.exec(con, SQL_DROP_TABLE_BLOQUEO_REINVERSION);
				}
			}
			
		}
		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::eliminarReinversionBloqueada", finMetodo - inicioMetodo);
	}
	
	private void insertarReinversionBloqueada(Connection con,Long idEmpresa,ArrayList<BloqueoReinversion> bloquearReinversion,SalidaCrearSolicitudRescateReinversion out) throws Exception {
		logger.debug("INICIO::insertarReinversionBloqueada");
		Long inicioMetodo = System.currentTimeMillis();
		SQL sql = new SQL();
		
		try {
			
			List<String> idsBloqueoReinversion = bloquearReinversion.stream().map(sol -> sol.IdBloqueaReinversion).collect(Collectors.toList());
			//Campo Simula en S no deberia insertar registro a la tabla de bloqueo
			boolean simula = bloquearReinversion.stream().allMatch(item -> "S".equalsIgnoreCase(item.Simula));
			//variable simula contiene un match entre todos los registros del campo Simula y pregunta si es S, retornara true si encuentra S en todos los registros, false sino.
			if(!simula) {
				HashMap<String,Object> paramsQuery = new HashMap<String,Object>();
				paramsQuery.put("p_IdBloqueaReinversion", idsBloqueoReinversion);
				
				logger.debug("INICIO::SQL_SELECT_BLOQUEO_SOLICITUD");
				List<BloqueoReinversion> solicitudesBloqueadasBD = sql.query(con, SQL_SELECT_TABLE_BLOQUEO_REINVERSION, paramsQuery, BloqueoReinversion.class);
				logger.debug("FIN::SQL_SELECT_BLOQUEO_SOLICITUD");
				
				List<String> idsSolicitudesBloqueadasBD = solicitudesBloqueadasBD.stream().map(sol -> sol.IdBloqueaReinversion).collect(Collectors.toList());
				
				if(solicitudesBloqueadasBD.size() > 0 && idsBloqueoReinversion.equals(idsSolicitudesBloqueadasBD)) {
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BLOQUEO_SOLICITUD", new HashMap<String,String>(), con, idEmpresa));

				} else {
					HashMap<String,Object> paramsSp = new HashMap<String,Object>();
					paramsSp.put("p_BloqueoSolicitudAporteRescate", bloquearReinversion);
					SQL.exec(con, SQL_CREATE_TABLE_BLOQUEO_REINVERSION);
					sql.batch(con, SQL_INSERT_TABLE_BLOQUEO_REINVERSION, bloquearReinversion, BloqueoReinversion.class);
					logger.debug("INICIO::SQL_CALL_CREAR_BLOQUEO_SOLICITUD");
					sql.call(con, SQL_CALL_CREAR_BLOQUEO_REINVERSION, paramsSp, new HashMap<String,Class>());
					logger.debug("FIN::SQL_CALL_CREAR_BLOQUEO_SOLICITUD"); 
				}
				logger.debug("FIN::SQL_INSERT_BLOQUEO_REINVERSION");
			}
		}catch(SQLException sqle) {
			out.P_OUT_ERRORESCONTADOR++; 
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BLOQUEO_REINVERSION",new HashMap<String,String>(),con,idEmpresa));
		}
		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::insertarReinversionBloqueada", finMetodo - inicioMetodo);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master01", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesRescate\":[{\"IdInterno\":1,\"IdCliente\":43003,\"IdPortafolio\":388037,\"IdSerieFondo\":\"00240AUSD\",\"CalculoPorMontoCuota\":\"T\",\"IdRegimenTributario\":\"APV-A\",\"Monto\":4981.588,\"NumeroCuotas\":80.465,\"IdMoneda\":\"USD\",\"IdMedioDePago\":\"CIN\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0}],\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":43003,\"IdPortafolio\":388037,\"IdSerieFondo\":\"00103AUSD\",\"IdEmisor\":\"BGF\",\"CalculoPorMontoCuota\":\"T\",\"Monto\":4981.588,\"NumeroCuotas\":302.281,\"IndicadorMLT\":\"S\",\"IdMoneda\":\"USD\",\"IdMedioDePago\":\"CIN\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":\"\",\"FolioContrato\":0}]}";
		
		EntradaCrearSolicitudRescateReinversion Objin = gson.fromJson(jsonRF, EntradaCrearSolicitudRescateReinversion.class);
		CrearSolicitudRescateReinversion  demo=new CrearSolicitudRescateReinversion();				
		try {
			SalidaCrearSolicitudRescateReinversion ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
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