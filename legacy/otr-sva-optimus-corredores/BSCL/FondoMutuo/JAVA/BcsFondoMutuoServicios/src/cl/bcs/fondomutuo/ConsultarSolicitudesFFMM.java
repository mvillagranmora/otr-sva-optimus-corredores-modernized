package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSolicitudesFFMM implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarSolicitudesFFMM.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarSolicitudesFFMM_SelectSolicitudes.sql");
	
	public static class EntradaConsultarSolicitudesFFMM{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		//FILTROS
		public Long p_IdCliente;
		public String p_TipoSolicitud;
		public String p_Origen;
		public String p_Folio;
		public String p_TipoAhorro;
		public String p_TipoCuenta;
		public String p_IdSerieFondo;
		public String p_Estado;
		public String p_FechaDesde;
		public String p_FechaHasta;
		
		public EntradaConsultarSolicitudesFFMM() {
			this.p_IdCliente = 0L;
			this.p_TipoSolicitud = "";
			this.p_Origen = "";
			this.p_Folio = "";
			this.p_TipoAhorro = "";
			this.p_TipoCuenta = "";
			this.p_IdSerieFondo = "";
			this.p_Estado = "";
			this.p_FechaDesde = "";
			this.p_FechaHasta = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarSolicitudesFFMM{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<Solicitud> P_OUT_SOLICITUDES;
		
		public SalidaConsultarSolicitudesFFMM() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_SOLICITUDES = new ArrayList<Solicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Solicitud{
		public Long IdCliente;
		public String TipoSolicitud;
		public String FechaConsulta;
		public String Origen;
		public Long NumSolicitud;
		public String DetSolicitud;
		public String Estado;
		public String EstadoDetalle;
		public Long Rellamada;
		public String OrigenSolicitud;
		public String FechaSolicitud;
		public String HoraSolicitud;
		public String FechaCierre;
		public String TipoTraspaso;
		public String MotivoRechazo;
		public String Folio;
		public Long FolioRelacionado;
		public String RutCliente;
		public Long CodFondoOrigen;
		public String NombreFondoOrigen;
		public String SerieFondoOrigen;
		public String RegimenFondoOrigen;
		public BigDecimal MontoPesosOrigen;
		public BigDecimal MontoCuotasOrigen;
		public BigDecimal ValorCuotaOrigen;
		public Long CodFondoDestino;
		public String NombreFondoDestino;
		public String SerieFondoDestino;
		public String RegimenFondoDestino;
		public BigDecimal MontoPesosDestino;
		public BigDecimal MontoCuotasDestino;
		public BigDecimal ValorCuotaDestino;
		public BigDecimal MontoCuotasComision;
		public BigDecimal PorcentajeOrigen;
		public BigDecimal PorcentajeDestino;
		public String NombreCliente;
		public String ClasificacionMLT;
		public String DescripcionTipoAhorro;
		public String DescripcionTipoCuenta;
		public String FechaValorCuota;
		public String FechaIngreso;
		public BigDecimal MontoCuotas;
		public BigDecimal MontoCuotasComprometidas;
		public BigDecimal MontoCuotasDisponibles;
		public BigDecimal MontoPesos;
		public BigDecimal MontoPesosComprometidos;
		public BigDecimal MontoPesosDisponibles;
		public String NombreAdministradora;
		public BigDecimal OrigenValorCuota;
		public String RegimenTributario;
		public String SerieFondo;
		public String TipoAhorro;
		public String TipoCuenta;
		public String TipoOperacion;
		public BigDecimal ValorCuota;
		public String TipoMovimiento;
		public Long IdMatrizAgrupador;
		public String EsADC;
		public String IdSerieFondoOrigen;
		public String IdSerieFondoDestino;
		public String FechaCertificacion;
		public String FechaLiquidacion;
		public String FechaCurse;
		public String FechaValorCuotaOrigen;
		public String FechaValorCuotaDestino;
		public String IngresoAutomatico;
		public Long FolioContrato;
		public String Canal;
		public String DescripcionCanal;
		public String IdMedioDePago;
		public String DescripcionMedioPago;
		public String IdSerieFondo;
		public Long IdContrato;
		public String FechaFinalReinversion;
		public Solicitud() {
			this.MontoCuotasComprometidas = BigDecimal.ZERO;
			this.MontoCuotasDisponibles = BigDecimal.ZERO;
			this.MontoPesosComprometidos = BigDecimal.ZERO;
			this.MontoPesosDisponibles = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MontoProceso{
		public BigDecimal MontoCuotasComprometidas;
		public BigDecimal MontoCuotasDisponibles;
		public BigDecimal MontoPesosComprometidos;
		public BigDecimal MontoPesosDisponibles;
		
		public MontoProceso() {
			this.MontoCuotasComprometidas = BigDecimal.ZERO;
			this.MontoCuotasDisponibles = BigDecimal.ZERO;
			this.MontoPesosComprometidos = BigDecimal.ZERO;
			this.MontoPesosDisponibles = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarSolicitudesFFMM jin = gson.fromJson(in.toString(), EntradaConsultarSolicitudesFFMM.class);
		SalidaConsultarSolicitudesFFMM out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarSolicitudesFFMM logica(Connection con, EntradaConsultarSolicitudesFFMM entrada) throws Exception {
		logger.debug("INICIO::ConsultarSolicitudesFFMM");
		SalidaConsultarSolicitudesFFMM out = new SalidaConsultarSolicitudesFFMM();
		
		HashMap<String,Object> params = obtenerFiltros(con, entrada);
		
		List<Solicitud> solicitudes = obtenerSolicitudes(con,params, entrada);
		
		solicitudes = calcularEstadoGlobal(solicitudes);
		
		if(!entrada.p_Estado.isEmpty()) {
			solicitudes = filtrarEstadoGlobal(solicitudes,entrada.p_Estado);
		}else {
			solicitudes = filtrarEstadoGlobal(solicitudes);
		}
		
		out.P_OUT_SOLICITUDES = solicitudes;
		
		logger.debug("FIN::ConsultarSolicitudesFFMM");
		return out;
	}
	
	private String obtenerParametroFiltroADC(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerParametroFiltroADC");
		String filtraADC = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FiltroAdministracionCarteraWS");
		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerParametroFiltroADC");
		return filtraADC;
	}
	
	private String obtenerParametroFiltroSoloSolicitudesNoAutomaticas(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerParametroFiltroSoloSolicitudesNoAutomaticas");
		String filtraSoloSolicitudesNoAutomaticas = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS");
		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerParametroFiltroSoloSolicitudesNoAutomaticas");
		return filtraSoloSolicitudesNoAutomaticas;
	}
	
	private HashMap<String,Object> obtenerFiltros(Connection con, EntradaConsultarSolicitudesFFMM entrada) throws Exception{
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerFiltros");
		HashMap<String,Object> params = new HashMap<String,Object>();
		String filtraADC = obtenerParametroFiltroADC(con,entrada.p_IdEmpresa);
		String filtraSoloSolicitudesNoAutomaticas = obtenerParametroFiltroSoloSolicitudesNoAutomaticas(con, entrada.p_IdEmpresa);
		
		params.put("IdCliente", entrada.p_IdCliente);
		params.put("TipoSolicitud", entrada.p_TipoSolicitud);
		params.put("Origen", entrada.p_Origen);
		params.put("Folio", entrada.p_Folio);
		params.put("TipoAhorro", entrada.p_TipoAhorro);
		params.put("TipoCuenta", entrada.p_TipoCuenta);
		params.put("IdSerieFondo", entrada.p_IdSerieFondo);
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("FiltraTipoMovimiento", _c.SI);
		params.put("FiltraCliente", esFiltro(entrada.p_IdCliente));
		params.put("FiltraTipoSolicitud", esFiltro(entrada.p_TipoSolicitud));
		params.put("FiltraOrigen", esFiltro(entrada.p_Origen));
		params.put("FiltraFolio", esFiltro(entrada.p_Folio));
		params.put("FiltraTipoAhorro", esFiltro(entrada.p_TipoAhorro));
		params.put("FiltraTipoCuenta", esFiltro(entrada.p_TipoCuenta));
		params.put("FiltraIdSerieFondo", esFiltro(entrada.p_IdSerieFondo));
		params.put("FiltraFechaDesde", esFiltro(entrada.p_FechaDesde));
		params.put("FiltraFechaHasta", esFiltro(entrada.p_FechaHasta));
		
		if(_c.SI.equals(filtraADC)) {
			params.put("EsADC", _c.NO);
			params.put("FiltraADC", _c.SI);
		}else {
			params.put("EsADC", "");
			params.put("FiltraADC", _c.NO);
		}
		
		if(_c.SI.equals(filtraSoloSolicitudesNoAutomaticas)) {
			params.put("IngresoAutomatico", _c.NO);
			params.put("FiltraIngresoAutomatico", _c.SI);
		}else {
			params.put("IngresoAutomatico", "");
			params.put("FiltraIngresoAutomatico", _c.NO);
		}
		
		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerFiltros");
		return params;
	}
	
	private String esFiltro(String campo) {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.esFiltro");
		String filtra = campo.isEmpty() ? _c.NO : _c.SI;
		logger.trace("FIN::ConsultarSolicitudesFFMM.esFiltro");
		return filtra;
	}
	
	private String esFiltro(Long campo) {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.esFiltro");
		String filtra = campo.equals(0L) ? _c.NO : _c.SI;
		logger.trace("FIN::ConsultarSolicitudesFFMM.esFiltro");
		return filtra;
	}
	
	private List<Solicitud> obtenerSolicitudes(Connection con, HashMap<String,Object> params, EntradaConsultarSolicitudesFFMM entrada) throws Exception{
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerSolicitudes");
		SQL sql = new SQL();
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
		Fecha.setLocalOffSet(segundosZonaHoraria);
		List<Solicitud> solicitudes = sql.queryParallel(con, SQL_SELECT_SOLICITUDES, params, Solicitud.class);
		List<Solicitud> solicitudesLocal = solicitudes.parallelStream().map(sol->{
			sol.HoraSolicitud = Fecha.toStringTime(Fecha.toLocalTime(Fecha.toTime(sol.HoraSolicitud)));
			sol.FechaIngreso = sol.FechaSolicitud.concat(" "+ sol.HoraSolicitud);
			return sol;
		}).collect(Collectors.toList());

		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerSolicitudes");
		return solicitudesLocal;
	}
	
	private List<Solicitud> calcularEstadoGlobal(List<Solicitud> solicitudes){
		logger.trace("INICIO::ConsultarSolicitudesFFMM.calcularEstadoGlobal");
		List<Solicitud> reinversiones = obtenerReinversiones(solicitudes);
		List<Solicitud> aportesRescates = obtenerAportesRescatesAsociados(solicitudes);
		solicitudes.parallelStream().forEach(solicitud->{
			MontoProceso montos;
			List<Solicitud> solicitudesAsociadas = new ArrayList<Solicitud>(); 
			if(_c.TipoOperacionReinversionWS.equals(solicitud.TipoSolicitud)) {
				solicitudesAsociadas = buscarReinversionesAsociadas(reinversiones,solicitud);
				montos = obtenerMontosReinversion(solicitudesAsociadas);
			}else {
				solicitudesAsociadas = buscarAportesRescatesAsociados(aportesRescates,solicitud);
				montos = obtenerMontos(solicitudesAsociadas);
			}
			Long ingresadas = contarPorFiltro(_c.EstadoIngresadoWS,solicitudesAsociadas);
			Long enProceso = contarPorFiltro(_c.EstadoEnProcesoWS,solicitudesAsociadas);
			Long anuladas = contarPorFiltro(_c.EstadoAnuladoWS,solicitudesAsociadas);
			Long terminadas = contarPorFiltro(_c.EstadoTerminadoWS,solicitudesAsociadas);
			solicitud.Estado = calcularEstadoReinversion(ingresadas,enProceso,anuladas,terminadas,Long.valueOf(solicitudesAsociadas.size()));
			
			solicitud.MontoCuotasComprometidas = montos.MontoCuotasComprometidas;
			solicitud.MontoPesosComprometidos = montos.MontoPesosComprometidos;
			solicitud.MontoCuotasDisponibles = montos.MontoCuotasDisponibles;
			solicitud.MontoPesosDisponibles = montos.MontoPesosDisponibles;
		});
		
		logger.trace("FIN::ConsultarSolicitudesFFMM.calcularEstadoGlobal");
		
		return solicitudes;
	}
	
	private String calcularEstadoReinversion(Long ingresadas, Long enProceso, Long anuladas, Long terminadas, Long total) {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.calcularEstadoReinversion");
		String estado = "";
		if(anuladas.equals(total)) {
			estado = _c.EstadoAnuladoWS;
		}
		if(ingresadas.equals(total)) {
			estado = _c.EstadoIngresadoWS;
		}
		if(terminadas.equals(total)) {
			estado = _c.EstadoTerminadoWS;
		}
		if(enProceso.compareTo(0L)>0) {
			estado = _c.EstadoEnProcesoWS;
		}
		if (estado.isEmpty()) {
			estado = "";
		}
		logger.trace("FIN::ConsultarSolicitudesFFMM.calcularEstadoReinversion");
		
		return estado;
	}
	
	private List<Solicitud> buscarReinversionesAsociadas(List<Solicitud> reinversiones, Solicitud solicitud){
		logger.trace("INICIO::ConsultarSolicitudesFFMM.buscarReinversionesAsociadas");
		List<Solicitud> reinversionesAsociadas = reinversiones.parallelStream()
				.filter(reinversion->(!reinversion.FolioRelacionado.equals(0L) && reinversion.FolioRelacionado.equals(solicitud.FolioRelacionado)) || (reinversion.NumSolicitud.equals(solicitud.NumSolicitud)))
				.collect(Collectors.toList());
		logger.trace("FIN::ConsultarSolicitudesFFMM.buscarReinversionesAsociadas");
		return reinversionesAsociadas;
	}
	
	private List<Solicitud> buscarAportesRescatesAsociados(List<Solicitud> aportesRescates, Solicitud solicitud){
		logger.trace("INICIO::ConsultarSolicitudesFFMM.buscarAportesRescatesAsociadas");
		List<Solicitud> aportesRescatesAsociados = aportesRescates.parallelStream()
				.filter(aporteRescate->(!aporteRescate.FolioContrato.equals(0L) && aporteRescate.FolioContrato.equals(solicitud.FolioContrato)) || (aporteRescate.NumSolicitud.equals(solicitud.NumSolicitud)))
				.collect(Collectors.toList());
		logger.trace("FIN::ConsultarSolicitudesFFMM.buscarAportesRescatesAsociadas");
		return aportesRescatesAsociados;
	}
	
	private List<Solicitud> obtenerAportesRescatesAsociados(List<Solicitud> solicitudes){
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerAportesRescatesAsociados");
		List<Solicitud> aportesRescates = solicitudes.parallelStream().filter(solicitud->!_c.TipoOperacionReinversionWS.equals(solicitud.TipoSolicitud)).collect(Collectors.toList());
		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerAportesRescatesAsociados");
		return aportesRescates;
	}
	
	private Long contarPorFiltro(String estadoDetalle, List<Solicitud> reinversiones) {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.contarPorFiltro");
		Long contador = reinversiones.parallelStream().filter(reinversion->estadoDetalle.equals(reinversion.EstadoDetalle)).count();
		logger.trace("FIN::ConsultarSolicitudesFFMM.contarPorFiltro");
		return contador;
	}
	
	private List<Solicitud> obtenerReinversiones(List<Solicitud> solicitudes){
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerReinversiones");
		List<Solicitud> reinversiones = solicitudes.parallelStream().filter(solicitud->_c.TipoOperacionReinversionWS.equals(solicitud.TipoSolicitud)).collect(Collectors.toList());
		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerReinversiones");
		return reinversiones;
	}
	
	private List<Solicitud> filtrarEstadoGlobal(List<Solicitud> solicitudes, String estado){
		logger.trace("INICIO::ConsultarSolicitudesFFMM.filtrarEstadoGlobal");
		List<Solicitud> solicitudesFiltradas = solicitudes.parallelStream().filter(solicitud->estado.equals(solicitud.Estado)).collect(Collectors.toList());
		logger.trace("FIN::ConsultarSolicitudesFFMM.filtrarEstadoGlobal");
		return solicitudesFiltradas;
	}
	
	private List<Solicitud> filtrarEstadoGlobal(List<Solicitud> solicitudes){
		logger.trace("INICIO::ConsultarSolicitudesFFMM.filtrarEstadoGlobal");
		List<Solicitud> solicitudesFiltradas = solicitudes.parallelStream().filter(solicitud->!"".equals(solicitud.Estado)).collect(Collectors.toList());
		logger.trace("FIN::ConsultarSolicitudesFFMM.filtrarEstadoGlobal");
		return solicitudesFiltradas;
	}
	
	private MontoProceso obtenerMontosReinversion(List<Solicitud> solicitudes) {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerMontosReinversion");
		MontoProceso montoProceso = new MontoProceso();
		
		solicitudes.stream().forEach(solicitud->{
			if(!_c.EstadoTerminadoWS.equals(solicitud.EstadoDetalle) && !_c.EstadoAnuladoWS.equals(solicitud.EstadoDetalle)) {
				montoProceso.MontoCuotasComprometidas = montoProceso.MontoCuotasComprometidas.add(solicitud.MontoCuotasDestino, MathContext.DECIMAL128);
				montoProceso.MontoPesosComprometidos = montoProceso.MontoPesosComprometidos.add(solicitud.MontoPesosDestino, MathContext.DECIMAL128);
			}else if(_c.EstadoTerminadoWS.equals(solicitud.EstadoDetalle)) {
				montoProceso.MontoCuotasDisponibles = montoProceso.MontoCuotasDisponibles.add(solicitud.MontoCuotasDestino, MathContext.DECIMAL128);
				montoProceso.MontoPesosDisponibles = montoProceso.MontoPesosDisponibles.add(solicitud.MontoPesosDestino, MathContext.DECIMAL128);
			}
		});
		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerMontosReinversion");
		return montoProceso;
	}
	
	private MontoProceso obtenerMontos(List<Solicitud> solicitudes) {
		logger.trace("INICIO::ConsultarSolicitudesFFMM.obtenerMontos");
		MontoProceso montoProceso = new MontoProceso();
		
		solicitudes.stream().forEach(solicitud->{
			if(!_c.EstadoTerminadoWS.equals(solicitud.EstadoDetalle) && !_c.EstadoAnuladoWS.equals(solicitud.EstadoDetalle)) {
				montoProceso.MontoCuotasComprometidas = montoProceso.MontoCuotasComprometidas.add(solicitud.MontoCuotasOrigen, MathContext.DECIMAL128);
				montoProceso.MontoPesosComprometidos = montoProceso.MontoPesosComprometidos.add(solicitud.MontoPesosOrigen, MathContext.DECIMAL128);
			}else if(_c.EstadoTerminadoWS.equals(solicitud.EstadoDetalle)) {
				montoProceso.MontoCuotasDisponibles = montoProceso.MontoCuotasDisponibles.add(solicitud.MontoCuotasOrigen, MathContext.DECIMAL128);
				montoProceso.MontoPesosDisponibles = montoProceso.MontoPesosDisponibles.add(solicitud.MontoPesosOrigen, MathContext.DECIMAL128);
			}
		});
		logger.trace("FIN::ConsultarSolicitudesFFMM.obtenerMontos");
		return montoProceso;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
			ConsultarSolicitudesFFMM demo = new ConsultarSolicitudesFFMM();
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"27\",\"p_IdModulo\":\"FMM\",\"p_IdCliente\":\"79296\",\"p_TipoSolicitud\":\"\",\"p_Origen\":\"\",\"p_Folio\":\"\",\"p_TipoAhorro\":\"\",\"p_TipoCuenta\":\"\",\"p_IdSerieFondo\":\"\",\"p_Estado\":\"\",\"p_FechaDesde\":\"2022-09-01\",\"p_FechaHasta\":\"2022-09-30\"}";
			
			EntradaConsultarSolicitudesFFMM Objin = gson.fromJson(json, EntradaConsultarSolicitudesFFMM.class);
			
			try {
				SalidaConsultarSolicitudesFFMM salida = demo.logica(con, Objin);
				System.out.println(salida);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}