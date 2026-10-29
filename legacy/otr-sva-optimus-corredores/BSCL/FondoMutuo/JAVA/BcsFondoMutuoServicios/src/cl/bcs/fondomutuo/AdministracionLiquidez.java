package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.InSolicitudes;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SalidaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteOvernight.EntradaCrearSolicitudAporteOvernight;
import cl.bcs.fondomutuo.CrearSolicitudAporteOvernight.InSolicitudesAporte;
import cl.bcs.fondomutuo.CrearSolicitudAporteOvernight.SalidaCrearSolicitudAporteOvernight;
import cl.bcs.fondomutuo.GenerarDataReporteTRXIFolioContrato;
import cl.bcs.fondomutuo.GenerarDataReporteTRXIFolioContrato.EntradaGenerarDataReporteTRXIFolioContrato;
import cl.bcs.fondomutuo.GenerarDataReporteTRXIFolioContrato.SalidaGenerarDataReporteTRXIFolioContrato;
import cl.bcs.fondomutuo.pojo.ResultadoCertificacion;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AdministracionLiquidez implements JavaCall {
	static final Logger logger = LogManager.getLogger(AdministracionLiquidez.class);
	
	private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/fondomutuo/sql/AdministracionLiquidez_SelectPortafolios.sql");
	private final String SQL_INSERT_EJECUCION = loadResourceToString("cl/bcs/fondomutuo/sql/AdministracionLiquidez_InsertEjecucion.sql");
	private final String SQL_INSERT_LOG = loadResourceToString("cl/bcs/fondomutuo/sql/AdministracionLiquidez_InsertLog.sql");
	private final String OBTENER_FECHA_ACTUAL = loadResourceToString("cl/bcs/fondomutuo/sql/AdministracionLiquidez_ObtenerFechaActual.sql");
	
	public static class EntradaAdministracionLiquidez {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAdministracionLiquidez {
		public List<ResultadoAdministracionLiquidez> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaAdministracionLiquidez() {
			this.P_OUT_RESULTADO = new ArrayList<ResultadoAdministracionLiquidez>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoAdministracionLiquidez{
		public String Id;
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdSerieFondo;
		public String IdMoneda;
		public BigDecimal Saldo;
		public BigDecimal MontoMinimo;
		public String Codigo;
		
		public ResultadoAdministracionLiquidez() {
			this.IdSolicitudAporteRescate = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InMonedas{
		public Long IdEmpresa;
		public String IdMoneda;
		public String IdSerieFondo;
		public BigDecimal MontoMinimo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InPortafolios{
		public Long IdPortafolio;
		public Long IdCliente;
		public String IdSerieFondo;
		public BigDecimal MontoMinimo;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class FechaActual{
		public String Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoPortafolios{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public String IdSerieFondo;
		public BigDecimal Saldo;
		public BigDecimal MontoMinimo;
		public String Codigo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EjecucionAdministracionLiquidez{
		public String IdMoneda;
		public Date FechaEjecucion;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LogAdministracionLiquidez{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdSerieFondo;
		public String IdMoneda;
		public BigDecimal Saldo;
		public BigDecimal MontoMinimo;
		public Long IdSolicitudAporteRescate;
		public Date FechaIngreso;
		public String Codigo;
		public Long IdEmpresa;
		
		public LogAdministracionLiquidez() {
			this.MontoMinimo = BigDecimal.ZERO;
			this.IdSolicitudAporteRescate = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaAdministracionLiquidez jin = gson.fromJson(in.toString(), EntradaAdministracionLiquidez.class);
		SalidaAdministracionLiquidez out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministracionLiquidez logica (Connection conn, EntradaAdministracionLiquidez in) throws Exception {
		logger.info("INICIO::FM.AdministracionLiquidez");
		SQL sql = new SQL();
		SalidaAdministracionLiquidez out = new SalidaAdministracionLiquidez();
		String fechaProceso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");;
		String horaSistema = FechaDB.ObtenerHoraSistemaLocal(conn, in.p_IdEmpresa);
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<String> monedas = new ArrayList<String>();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		Boolean esDiaHabil = false;
		List<FechaActual> FechaActual = sql.query(conn, OBTENER_FECHA_ACTUAL, params, FechaActual.class);
		try {
		    esDiaHabil = Feriados.esDiaHabil(conn, FechaActual.get(0).Fecha, idPaisEmpresa);
		} catch (Exception e) {
			logger.info("Ocurrió un error al verificar si es día hábil: ");
		}
		if (!esDiaHabil) {
			logger.info("Proceso no se ejecuta por dia inhabil.");
		    return out;
		}
		//obtenemos portafolios

		params.put("HoraSistema", horaSistema);
		params.put("FechaProceso", fechaProceso);
		List<InPortafolios> inPortafolios = sql.query(conn, SQL_SELECT_PORTAFOLIOS, params, InPortafolios.class);
		
		for(InPortafolios port : inPortafolios) {
			ResultadoAdministracionLiquidez resultadoAdministracionLiquidez = new ResultadoAdministracionLiquidez();
			resultadoAdministracionLiquidez.Id = port.IdPortafolio + "_" + port.IdMoneda;
			resultadoAdministracionLiquidez.IdCliente = port.IdCliente;
			resultadoAdministracionLiquidez.IdPortafolio = port.IdPortafolio;
			resultadoAdministracionLiquidez.IdSerieFondo = port.IdSerieFondo;
			resultadoAdministracionLiquidez.IdMoneda = port.IdMoneda;
			resultadoAdministracionLiquidez.MontoMinimo = port.MontoMinimo;
			resultadoAdministracionLiquidez.Codigo = "UI_PENDIENTE";
			out.P_OUT_RESULTADO.add(resultadoAdministracionLiquidez);
			if(!monedas.contains(port.IdMoneda)) monedas.add(port.IdMoneda);
		}
		
		HashMap<String,Object> hashResultados = Utils.List2Hash(out.P_OUT_RESULTADO, "Id");
		
		//consultamos saldo flujo caja
		SaldoFlujoCaja.ServiceEntrada entradaSaldo = new SaldoFlujoCaja.ServiceEntrada();
		entradaSaldo.P_IDEMPRESA = in.p_IdEmpresa;
		entradaSaldo.P_FECHADESDE = fechaProceso;
		entradaSaldo.P_FECHAHASTA = fechaProceso;
		entradaSaldo.P_USO = "ADMINISTRACION_LIQUIDEZ";
		//creamos lista para la entrada del flujo de caja
		List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> objs = inPortafolios.stream()
			.map(p ->{
				SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR obj = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
				obj.IdMoneda = p.IdMoneda;
				obj.IdPortafolio = p.IdPortafolio;
				return obj;
			})
			.collect(Collectors.toList());
		entradaSaldo.P_PORTAFOLIOACONSULTAR = objs;
		SaldoFlujoCaja sfc = new SaldoFlujoCaja();
		SaldoFlujoCaja.ServiceSalida salidaSaldo = sfc.logica(conn, entradaSaldo);
		
		
		List<SaldoPortafolios> conSaldoCaja = new ArrayList<SaldoPortafolios>();
		for(ResultadoAdministracionLiquidez port : out.P_OUT_RESULTADO) {
			for(SaldoFlujoCaja.ObjFLUJOCAJA flujo : salidaSaldo.P_OUT_FLUJOCAJA) {
				if(port.IdPortafolio.equals(flujo.IdPortafolio) && flujo.FechaSaldo.equals(fechaProceso) && port.IdMoneda.equals(flujo.IdMoneda)) {
					port.Saldo = flujo.Saldo;
					if(port.MontoMinimo.compareTo(flujo.Saldo) == 1 || flujo.Saldo.compareTo(BigDecimal.ZERO) == 0) {
						port.Codigo = "SALDO_INSUFICIENTE";
						break;
					}
					else if(port.MontoMinimo.compareTo(flujo.Saldo) == -1 && flujo.Saldo.compareTo(BigDecimal.ZERO) == 1) {
						SaldoPortafolios portafolioConSaldoCaja = new SaldoPortafolios();
						portafolioConSaldoCaja.IdCliente = port.IdCliente;
						portafolioConSaldoCaja.IdPortafolio = port.IdPortafolio;
						portafolioConSaldoCaja.IdMoneda = port.IdMoneda;
						portafolioConSaldoCaja.IdSerieFondo = port.IdSerieFondo;
						portafolioConSaldoCaja.Saldo = flujo.Saldo.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : flujo.Saldo;
						portafolioConSaldoCaja.MontoMinimo = port.MontoMinimo;
						portafolioConSaldoCaja.Codigo = "";
						conSaldoCaja.add(portafolioConSaldoCaja);
						break;
					}
				}
			}
		}
		
		if(conSaldoCaja.size() > 0) {
			//creamos solicitudes overnight
			EntradaCrearSolicitudAporteOvernight entradaOvernight = new EntradaCrearSolicitudAporteOvernight();
			entradaOvernight.p_IdEmpresa = in.p_IdEmpresa;
			entradaOvernight.p_IdModulo = in.p_IdModulo;
			entradaOvernight.p_IdUsuario = in.p_IdUsuario;
			entradaOvernight.p_SimularOperacion = in.p_SimularOperacion;
			entradaOvernight.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			entradaOvernight.p_IdTipoCanalIngreso = "";
			Long id = 1L;
			HashMap<Long,String> hashIdSolicitudes = new HashMap<Long,String>();
			for(SaldoPortafolios saldo : conSaldoCaja) {
				InSolicitudesAporte aporte = new InSolicitudesAporte();
				aporte.IdInterno = id;
				aporte.IdCliente = saldo.IdCliente;
				aporte.IdPortafolio = saldo.IdPortafolio;
				aporte.IdSerieFondo = saldo.IdSerieFondo;
				aporte.Monto = saldo.Saldo;
				aporte.NumeroCuotas = BigDecimal.ZERO;
				aporte.IdMoneda = saldo.IdMoneda;
				aporte.IdMedioDePago = "CIN";
				aporte.IdMedioDePagoRescate = "CIN";
				aporte.IdPreferenciaPagoPortafolioRescate = 0L;
				aporte.Visado = "N";
				aporte.SolicitudBatch = "S";
				aporte.OrigenOperacion = "M";
				aporte.IdRegimenTributario = "NINGUNO";
				aporte.AdministracionLiquidez = "S";
				aporte.Glosa = "";
				aporte.IdAutorizadoOrigen = 0L;
				aporte.FolioContrato = 0L;
				aporte.IngresoAutomatico = "S";
				entradaOvernight.p_SolicitudesAporte.add(aporte);
				hashIdSolicitudes.put(id, saldo.IdPortafolio + "_" + saldo.IdMoneda);
				id++;
			}
			
			CrearSolicitudAporteOvernight crearOvernight = new CrearSolicitudAporteOvernight();
			SalidaCrearSolicitudAporteOvernight salidaOvernight = crearOvernight.logica(conn, entradaOvernight);
			out.P_OUT_MENSAJES.addAll(salidaOvernight.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR += salidaOvernight.P_OUT_ERRORESCONTADOR;
			
			for(MensajeSalida mje : out.P_OUT_MENSAJES) {
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
					ResultadoAdministracionLiquidez resultadoAdministracionLiquidez = (ResultadoAdministracionLiquidez) hashResultados.get(hashIdSolicitudes.get(mje.id));
					if(resultadoAdministracionLiquidez != null) {
						resultadoAdministracionLiquidez.Codigo = mje.codigo;
					}
				}
			}
			
			Map<String, ResultadoAdministracionLiquidez> resultadosPorId = out.P_OUT_RESULTADO.stream().collect(Collectors.toMap(resultado->resultado.Id, resultado->resultado));
			
			for(ResultadoSolicitud res : salidaOvernight.P_OUT_RESULTADO) {
				String idInterno = hashIdSolicitudes.get(res.IdInterno);
				ResultadoAdministracionLiquidez port = resultadosPorId.get(idInterno);
				if(port != null) {
					port.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
				}
			}
			
			//Reporte omnibus
			String utilizaOmnibus = "";
			try	{
				utilizaOmnibus = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "FONDOMUTUO", "VALIDAR_LIQUIDACION_OMNIBUS");
			}
			catch (Exception e)	{
				utilizaOmnibus = "N";
			}
			if ("S".equals(utilizaOmnibus)) {
				GenerarDataReporteTRXIFolioContrato omnibus = new GenerarDataReporteTRXIFolioContrato();
				SalidaGenerarDataReporteTRXIFolioContrato salidaOmnibus = new SalidaGenerarDataReporteTRXIFolioContrato();
				EntradaGenerarDataReporteTRXIFolioContrato entradaOmnibus = new EntradaGenerarDataReporteTRXIFolioContrato();
				entradaOmnibus.p_IdEmpresa = in.p_IdEmpresa;
				entradaOmnibus.p_IdUsuario = in.p_IdUsuario;
				entradaOmnibus.p_IdModulo = in.p_IdModulo;
				HashMap<Long,String> hashIdSolicitudesReportar = new HashMap<Long,String>();
				List<GenerarDataReporteTRXIFolioContrato.InSolicitudes> solicitudesReportar = new ArrayList<GenerarDataReporteTRXIFolioContrato.InSolicitudes>();
				for(ResultadoSolicitud res : salidaOvernight.P_OUT_RESULTADO) {
					ResultadoAdministracionLiquidez resultadoAdministracionLiquidez = (ResultadoAdministracionLiquidez) hashResultados.get(hashIdSolicitudes.get(res.IdInterno));
					if(resultadoAdministracionLiquidez != null && hashIdSolicitudesReportar.get(res.IdSolicitudAporteRescate) == null) {
						resultadoAdministracionLiquidez.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
						GenerarDataReporteTRXIFolioContrato.InSolicitudes sol = new GenerarDataReporteTRXIFolioContrato.InSolicitudes();
						sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
						solicitudesReportar.add(sol);
						hashIdSolicitudesReportar.put(res.IdSolicitudAporteRescate, resultadoAdministracionLiquidez.IdPortafolio + "_" + resultadoAdministracionLiquidez.IdMoneda);
					}
				}
				entradaOmnibus.p_Solicitudes = solicitudesReportar;
				salidaOmnibus = omnibus.logica(conn, entradaOmnibus);
				out.P_OUT_ERRORESCONTADOR += salidaOmnibus.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaOmnibus.P_OUT_MENSAJES);
			}
			
			//solicitudes exitosas
			EntradaCertificarSolicitudAporteRescate entradaCertificar = new EntradaCertificarSolicitudAporteRescate();
			entradaCertificar.p_IdEmpresa = in.p_IdEmpresa;
			entradaCertificar.p_IdModulo = in.p_IdModulo;
			entradaCertificar.p_IdUsuario = in.p_IdUsuario;
			entradaCertificar.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			entradaCertificar.p_SimularOperacion = in.p_SimularOperacion;
			entradaCertificar.p_FechaCertificacion = fechaProceso;
			entradaCertificar.p_IdTipoCanalIngreso = "";
			HashMap<Long,String> hashIdSolicitudesResultado = new HashMap<Long,String>();
			//agregamos para certificar
			List<InSolicitudes> solicitudesCertificar = new ArrayList<InSolicitudes>();
			for(ResultadoSolicitud res : salidaOvernight.P_OUT_RESULTADO) {
				ResultadoAdministracionLiquidez resultadoAdministracionLiquidez = (ResultadoAdministracionLiquidez) hashResultados.get(hashIdSolicitudes.get(res.IdInterno));
				if(resultadoAdministracionLiquidez != null && hashIdSolicitudesResultado.get(res.IdSolicitudAporteRescate) == null) {
					resultadoAdministracionLiquidez.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
					InSolicitudes sol = new InSolicitudes();
					sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
					solicitudesCertificar.add(sol);
					hashIdSolicitudesResultado.put(res.IdSolicitudAporteRescate, resultadoAdministracionLiquidez.IdPortafolio + "_" + resultadoAdministracionLiquidez.IdMoneda);
				}
			}
			
			entradaCertificar.p_Solicitudes = solicitudesCertificar;
			//certificamos
			CertificarSolicitudAporteRescate certificar = new CertificarSolicitudAporteRescate();
			SalidaCertificarSolicitudAporteRescate salidaCertificar = certificar.logica(conn, entradaCertificar);
			out.P_OUT_ERRORESCONTADOR += salidaCertificar.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaCertificar.P_OUT_MENSAJES);
			
			for(ResultadoCertificacion resultadoCertificar : salidaCertificar.P_OUT_RESULTADO) {
				ResultadoAdministracionLiquidez resultadoAdministracionLiquidez = (ResultadoAdministracionLiquidez) hashResultados.get(hashIdSolicitudesResultado.get(resultadoCertificar.IdSolicitudAporteRescate));
				if(resultadoAdministracionLiquidez != null) {
					resultadoAdministracionLiquidez.Codigo = resultadoCertificar.Codigo;
				}
			}
			
		}
		
		List<EjecucionAdministracionLiquidez> ejecucion = monedas.stream()
				.map(moneda ->{
					EjecucionAdministracionLiquidez ejec = new EjecucionAdministracionLiquidez();
					ejec.IdMoneda = moneda;
					ejec.FechaEjecucion = Fecha.toDate(fechaProceso);
					ejec.IdEmpresa = in.p_IdEmpresa;
					return ejec;
				})
				.collect(Collectors.toList());
			
		sql.batch(conn, SQL_INSERT_EJECUCION, ejecucion, EjecucionAdministracionLiquidez.class);
		
		List<LogAdministracionLiquidez> logAdministracionLiquidez = out.P_OUT_RESULTADO.stream().map(resultado ->{
					LogAdministracionLiquidez log = new LogAdministracionLiquidez();
					log.IdCliente = resultado.IdCliente;
					log.IdPortafolio = resultado.IdPortafolio;
					log.IdSerieFondo = resultado.IdSerieFondo;
					log.IdMoneda = resultado.IdMoneda;
					log.Saldo = resultado.Saldo;
					log.MontoMinimo = resultado.MontoMinimo;
					log.IdSolicitudAporteRescate = resultado.IdSolicitudAporteRescate;
					log.FechaIngreso = Fecha.toDate(fechaProceso);
					log.Codigo = resultado.Codigo;
					log.IdEmpresa = in.p_IdEmpresa;
					return log;
				})
				.collect(Collectors.toList());
		
		sql.batch(conn, SQL_INSERT_LOG, logAdministracionLiquidez, LogAdministracionLiquidez.class);
		
		if(logAdministracionLiquidez.size() > 0) {
			//agregamos log
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 1L;
			AgregarLog.InAgregarLog.Log logProceso = new AgregarLog.InAgregarLog.Log();
			logProceso.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
			logProceso.IdCabeceraLog	= 1L; 
			logProceso.IdTipoLog		= "INGRESO"; 
			logProceso.IdModulo		= in.p_IdModulo; 
			logProceso.IdUsuario		= in.p_IdUsuario; 
			logProceso.IdEmpresa		= in.p_IdEmpresa; 
			logProceso.FechaSistemaLog	= fechaProceso; 
			logProceso.IdUsuarioAtendido	= in.p_IdUsuario; 
			logProceso.IdCliente			= 0L; 
			logProceso.EstadoCliente		= ""; 
			logProceso.NombreCliente		= ""; 
			logProceso.IdTipoDocumentoIdentidad	= ""; 
			logProceso.IdDocumentoIdentidad	= ""; 
			logProceso.IdPortafolio			= 0L; 
			logProceso.EstadoPortafolio		= ""; 
			logProceso.IdTipoRegistro			= "ADMINISTRACION_LIQUIDEZ"; 
			logProceso.IdRegistro				= "";
			logProceso.EstadoRegistro			= "PRO"; 
			logProceso.IdTipoOperacion			= ""; 
			logProceso.Folio					= 0L; 
			logProceso.Cantidad				= BigDecimal.ZERO; 
			logProceso.PrecioTasa				= BigDecimal.ZERO; 
			logProceso.Monto					= BigDecimal.ZERO; 
			logProceso.IdMonedaOrigen			= ""; 
			logProceso.IdMonedaLiquidacion		= ""; 
			logProceso.TipoCambio				= ""; 
			logProceso.Nemo					=""; 
			logProceso.IdProceso				= 0L; 
			logProceso.EstadoProceso			= "PRO"; 
			logProceso.NombreProceso			= "ADMINISTRACION_LIQUIDEZ";
			entradaAgregarLog.p_Log.add(logProceso);
			
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
		
		logger.info("FIN::FM.AdministracionLiquidez");
		return out;
	};
	
	public static void main(String[] args) throws Exception,SQLException {
		Gson gson = new Gson();
		try (Connection conn = PlataformaUtils.getPropertiesConnection("qa02","connections.properties")) {
			System.out.println("== BEGIN ==");
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\"}";
			EntradaAdministracionLiquidez objIn = gson.fromJson(json,EntradaAdministracionLiquidez.class);
			AdministracionLiquidez demo = new AdministracionLiquidez();
			try {
				SalidaAdministracionLiquidez ObjOut=demo.logica(conn, objIn);
				//System.out.println(ObjOut.toString());
				conn.rollback();
				//conn.commit();
			}catch (Exception e){
				e.printStackTrace();
				conn.rollback();
			}finally {
				conn.close();
			}
		}
		System.out.println("== END ==");
	};

};
