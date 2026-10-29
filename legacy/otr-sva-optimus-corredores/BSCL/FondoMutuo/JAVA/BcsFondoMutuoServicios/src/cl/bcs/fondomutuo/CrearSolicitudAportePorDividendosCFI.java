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
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.EntradaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.SalidaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.FormaPagoSpot;
import cl.bcs.fondomutuo.pojo.IndicadoresPortafolio;
import cl.bcs.fondomutuo.pojo.Moneda;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
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
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearSolicitudAportePorDividendosCFI implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearSolicitudAportePorDividendosCFI.class);
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_MONEDA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
		
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudAportePorDividendosCFI extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InSolicitudesAporte> p_SolicitudesAporte;
		
		public EntradaCrearSolicitudAportePorDividendosCFI() {
			p_SolicitudesAporte = new ArrayList<InSolicitudesAporte>();
		}

	}
	
	public static class InSolicitudesAporte extends BcsPojo{
    	public Long IdInterno;
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
    	public BigDecimal Monto;
    	public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdRegimenTributario;
    	public String Glosa;
    	public Date FechaValorCuota;
	}
	
	public static class SalidaCrearSolicitudAportePorDividendosCFI extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudAportePorDividendosCFI(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
	}
	
	public static class InformacionProceso extends BcsPojo{
		public Long IdPaisSistema;
		public String FechaSistema;
		public String AporteGeneraCompraSpot;
		public String RespetaValutasCargaMasiva;
		Map<String,SerieFondoSolicitudes> InformacionSerieFondo;
		Map<Long,ClientePortafolioSolicitudes> InformacionClientePortafolio;
		Map<String,TipoMovimientoFondoMutuo> InformacionTipoMovimientoFondoMutuo;
		Map<String,RegimenSerieDeFondo> InformacionRegimenSerieDeFondo;
		Map<Long,IndicadoresPortafolio> InformacionIndicadoresUsaCuentaInversionPortafolio;
		Map<String,ConfiguracionPropositoPortafolio> InformacionConfiguracionRegimenPropositoPortafolio;
		Map<String,Moneda> InformacionMonedaFondo;
		Map<Long,ResultadoFecha> FechaSolicitudes;
		Map<Long,CalcularMontoNumeroCuota.Resultado> MontoCuotaSolicitudes;
		Map<String,BigDecimal> MontosTotalesPorPortafolio;
		SaldoFlujoCaja.ServiceSalida FlujosCaja;
		List<Long> IdSolicitudesAporteRescate;
		List<Long> FolioContratos;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearSolicitudAportePorDividendosCFI jin = gson.fromJson(in.toString(), EntradaCrearSolicitudAportePorDividendosCFI.class);
		SalidaCrearSolicitudAportePorDividendosCFI out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public SalidaCrearSolicitudAportePorDividendosCFI logica(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudAportePorDividendosCFI");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearSolicitudAportePorDividendosCFI out = new SalidaCrearSolicitudAportePorDividendosCFI();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesAporte.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
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
		
		InformacionProceso informacionProceso = obtenerInformacionConfiguracionProceso(con, entrada);
		informacionProceso.FechaSolicitudes = obtenerFechaSolicitudes(con,entrada,informacionProceso);
		informacionProceso.MontoCuotaSolicitudes = obtenerMontoCuotaSolicitudes(con,entrada,informacionProceso);
		informacionProceso.IdSolicitudesAporteRescate = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesAporte.size()));
		informacionProceso.FolioContratos = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", Long.valueOf(entrada.p_SolicitudesAporte.size()));
		
		List<SolicitudAporteRescate> solicitudesProcesar = obtenerSolicitudesProcesar(con,entrada,informacionProceso);
		Map<Long,ResultadoSolicitud> resultadoSimulado = obtenerResultadoSimulado(solicitudesProcesar,informacionProceso);
		
		SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate salidaCumpleRequisitos = validarRequisitosFondoMutuo(con,entrada,solicitudesProcesar,informacionProceso);
		Map<Long,List<MensajeSalida>> mensajesSalidaAgrupadosPorSolicitud = salidaCumpleRequisitos.P_OUT_MENSAJES.parallelStream().collect(Collectors.groupingBy(mje->Math.abs(mje.id)));
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			List<MensajeSalida> mensajesSalidaSolicitud = mensajesSalidaAgrupadosPorSolicitud.get(sol.IdInterno);
			if(mensajesSalidaSolicitud != null){
				out.P_OUT_MENSAJES.addAll(mensajesSalidaSolicitud);
				Long errores = mensajesSalidaSolicitud.parallelStream().filter(mje->MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)).count();
				out.P_OUT_ERRORESCONTADOR += errores;
				if(!errores.equals(0L)){
					sol.EstadoProceso = _c.EstadoRechazado;
				}
			}
		}
		long registrosPorProcesar = solicitudesProcesar.parallelStream().filter(sol->_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)).count();
		if(registrosPorProcesar == 0){
			return out;
		}
		
		informacionProceso.MontosTotalesPorPortafolio = obtenerMontosTotalesPorPortafolio(solicitudesProcesar);
		informacionProceso.FlujosCaja = obtenerFlujoCaja(con,entrada,solicitudesProcesar,informacionProceso);
		
		Map<Long,SolicitudAporteRescate> solicitudesSinSaldoCaja = obtenerSolicitudesSinFlujoCaja(solicitudesProcesar,informacionProceso);
		
		for(SolicitudAporteRescate solicitud : solicitudesProcesar){
			if(_c.EstadoRechazado.equalsIgnoreCase(solicitud.EstadoProceso)) continue;
			
			if(solicitudesSinSaldoCaja.get(solicitud.IdInterno) != null){
				ClientePortafolioSolicitudes clientePortafolio = informacionProceso.InformacionClientePortafolio.get(solicitud.IdPortafolio);
				HashMap<String,String> mapMsg = new HashMap<>();
				mapMsg.put("CodigoPortafolio",clientePortafolio.CodigoPortafolio);
				mapMsg.put("DocumentoIdentidad",clientePortafolio.DocumentoIdentidad);
				
				solicitud.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_SALDO_INSUFICIENTE_FLUJO_CAJA",mapMsg,con,entrada.p_IdEmpresa));
				continue;
			}
		}
		
		if(_c.SI.equals(informacionProceso.AporteGeneraCompraSpot)) {
			SalidaProcesarIngresoOperacionSpot movimientosSpotGenerados = generarMovimientosSpot(con,entrada,solicitudesProcesar);

			for(OperacionObjOut resultadoSpot : movimientosSpotGenerados.P_OUT_IDOPERACIONSPOT){
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if((Long.parseLong(resultadoSpot.IdInterno) == sol.IdInterno) ){
						if(_c.SpotEstadoRechazado.equalsIgnoreCase(resultadoSpot.Estado)){			
							sol.EstadoProceso = _c.EstadoRechazado;
							out.P_OUT_ERRORESCONTADOR++;
							break;
						}else{
							sol.Estado = _c.EstadoPendiente;
							sol.IdOperacionSpot = resultadoSpot.IdOperacionSpot;
							break;
						}
					}
				}
			}
			
			for(MensajeSalida resultadoSpot : movimientosSpotGenerados.P_OUT_MENSAJES){
				if(_c.SpotError.equalsIgnoreCase(resultadoSpot.tipo)){
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(resultadoSpot.id, resultadoSpot.tipo, resultadoSpot.codigo,  _c.ModuloSpot + " : " + resultadoSpot.descripcion));
					for(SolicitudAporteRescate sol : solicitudesProcesar){
						sol.EstadoProceso = _c.EstadoRechazado;
						break;
					}
				}
			}
		}
		
		
		List<SolicitudAporteRescate> solicitudesCorrectas = obtenerSolicitudesCorrectas(solicitudesProcesar);
		
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion)){
			insertarSolicitudes(con, solicitudesCorrectas);
			insertarLog(con,entrada,informacionProceso,solicitudesCorrectas);
		}
		
		out.P_OUT_RESULTADO = obtenerResultado(solicitudesCorrectas,resultadoSimulado);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSolicitudAportePorDividendosCFI", finServicio - inicioServicio);
		return out;
		
	}
	
	
	
	private InformacionProceso obtenerInformacionConfiguracionProceso(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerInformacionConfiguracionProceso");
		long inicio = System.currentTimeMillis();
		InformacionProceso informacionConfiguracionFondoMutuo = new InformacionProceso();
		informacionConfiguracionFondoMutuo.FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		informacionConfiguracionFondoMutuo.AporteGeneraCompraSpot = obtenerParametroAporteGeneraCompraSpot(con, entrada.p_IdEmpresa);
		informacionConfiguracionFondoMutuo.IdPaisSistema = obtenerIdPaisSistema(con, entrada.p_IdEmpresa);
		informacionConfiguracionFondoMutuo.RespetaValutasCargaMasiva = obtenerParametroRespetaValutasCargaMasiva(con, entrada.p_IdEmpresa);
		List<SerieFondoSolicitudes> seriesSolicitudes = obtenerSerieFondos(con,entrada);
		Map<String, SerieFondoSolicitudes> seriesFondo = seriesSolicitudes.parallelStream().collect(Collectors.toMap(serieFondo->serieFondo.IdSerieFondo, serieFondo->serieFondo));
		informacionConfiguracionFondoMutuo.InformacionSerieFondo = seriesFondo;
		informacionConfiguracionFondoMutuo.InformacionClientePortafolio = obtenerClientesPortafolio(con,entrada);
		informacionConfiguracionFondoMutuo.InformacionTipoMovimientoFondoMutuo =  obtenerTipoMovimientoFondoMutuo(con,entrada);
		informacionConfiguracionFondoMutuo.InformacionRegimenSerieDeFondo =  obtenerRegimenesSerieFondo(con,entrada);
		informacionConfiguracionFondoMutuo.InformacionIndicadoresUsaCuentaInversionPortafolio =  obtenerIndicadorPortafolioUsaCuentaInversion(con,entrada);
		informacionConfiguracionFondoMutuo.InformacionMonedaFondo = obtenerInformacionMonedasFondo(con,seriesSolicitudes);
		informacionConfiguracionFondoMutuo.InformacionConfiguracionRegimenPropositoPortafolio =  obtenerConfiguracionRegimenPropositoPortafolio(con,entrada);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerInformacionConfiguracionProceso", fin - inicio);
		return informacionConfiguracionFondoMutuo;
	}
	
	private String obtenerParametroAporteGeneraCompraSpot(Connection con, Long idEmpresa) {
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerParametroAporteGeneraCompraSpot");
		long inicio = System.currentTimeMillis();
		String aporteGeneraCompraSpot = _c.NO;
		try {
			aporteGeneraCompraSpot = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "APORTE_GENERA_COMPRA_SPOT_DOLAR");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CrearSolicitudAportePorDividendosCFI.obtenerParametroAporteGeneraCompraSpot::Parametro APORTE_GENERA_COMPRA_SPOT_DOLAR No existe ");
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerParametroAporteGeneraCompraSpot", fin - inicio);
		return aporteGeneraCompraSpot;
	}
	
	private Long obtenerIdPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerIdPaisSistema");
		long inicio = System.currentTimeMillis();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerIdPaisSistema", fin - inicio);
		return idPaisEmpresa;
	}
	
	private String obtenerParametroRespetaValutasCargaMasiva(Connection con, Long idEmpresa) {
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerIdPaisSistema");
		long inicio = System.currentTimeMillis();
		String respetaValutasCargaMasiva = _c.NO;
		try {
			respetaValutasCargaMasiva = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::CrearSolicitudAportePorDividendosCFI::No se encuentra parametro RespetaValutasCargaMasiva");
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerIdPaisSistema", fin - inicio);
		return respetaValutasCargaMasiva;
	}
	
	private List<SerieFondoSolicitudes> obtenerSerieFondos(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerSerieFondos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<String> series = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->solicitud.IdSerieFondo).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdSerieFondo", series);
		List<SerieFondoSolicitudes> seriesFondo = sql.query(con, SQL_SELECT_SERIE_FONDO, parametros, SerieFondoSolicitudes.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerSerieFondos", fin - inicio);
		return seriesFondo;
	}
	
	private Map<Long,ClientePortafolioSolicitudes> obtenerClientesPortafolio(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerClientesPortafolio");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Long> portafolios = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->solicitud.IdPortafolio).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdPortafolio", portafolios);
		List<ClientePortafolioSolicitudes> clientesSolicitudes = sql.query(con, SQL_SELECT_PORTAFOLIO, parametros, ClientePortafolioSolicitudes.class);
		Map<Long,ClientePortafolioSolicitudes> clientesPortafolio = clientesSolicitudes.parallelStream().collect(Collectors.toMap(clientePortafolio->clientePortafolio.IdPortafolio, clientePortafolio->clientePortafolio));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerClientesPortafolio", fin - inicio);
		return clientesPortafolio;
	}
	
	private Map<String,TipoMovimientoFondoMutuo> obtenerTipoMovimientoFondoMutuo(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerTipoMovimientoFondoMutuo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<String> idTipoMovimientos = new ArrayList<String>();
		idTipoMovimientos.add(_c.AporteDividendoCFI);
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdTipoMovimientoFondoMutuo", idTipoMovimientos);
		List<TipoMovimientoFondoMutuo> tipoMovimientos = sql.query(con, SQL_SELECT_TIPO_MOVIMIENTO, parametros, TipoMovimientoFondoMutuo.class);
		Map<String,TipoMovimientoFondoMutuo> tipoMovimientosFondoMutuo = tipoMovimientos.parallelStream().collect(Collectors.toMap(tipoMovimiento->tipoMovimiento.IdTipoMovimientoFondoMutuo, tipoMovimiento->tipoMovimiento));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerTipoMovimientoFondoMutuo", fin - inicio);
		return tipoMovimientosFondoMutuo;
	}
	
	private Map<String,RegimenSerieDeFondo> obtenerRegimenesSerieFondo(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerRegimenesSerieFondo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<String> series = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->solicitud.IdSerieFondo).distinct().collect(Collectors.toList());
		List<String> regimenes = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->solicitud.IdRegimenTributario).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdSerieFondo", series);
		parametros.put("IdRegimenTributario", regimenes);
		List<RegimenSerieDeFondo> regimenesSeries = sql.query(con, SQL_SELECT_REGIMEN, parametros, RegimenSerieDeFondo.class);
		Map<String,RegimenSerieDeFondo> regimenesSeriesFondo = regimenesSeries.parallelStream().collect(Collectors.toMap(regimenSerie->regimenSerie.IdRegimenFondoMutuoSerieFondo, regimenSerie->regimenSerie));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerRegimenesSerieFondo", fin - inicio);
		return regimenesSeriesFondo;
	}
	
	private Map<Long,IndicadoresPortafolio> obtenerIndicadorPortafolioUsaCuentaInversion(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerIndicadorPortafolioUsaCuentaInversion");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Long> portafolios = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->solicitud.IdPortafolio).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdPortafolio", portafolios);
		parametros.put("CodigoTipoIndicador", _c.UsaCuentaInversion);
		List<IndicadoresPortafolio> indicadoresPortafolio = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, parametros, IndicadoresPortafolio.class);
		Map<Long,IndicadoresPortafolio> indicadoresPortafolioUsaCuentaInversion = indicadoresPortafolio.parallelStream().collect(Collectors.toMap(indicadorPortafolio->indicadorPortafolio.IdPortafolio, indicadorPortafolio->indicadorPortafolio));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerIndicadorPortafolioUsaCuentaInversion", fin - inicio);
		return indicadoresPortafolioUsaCuentaInversion;
	}
	
	private Map<String,ConfiguracionPropositoPortafolio> obtenerConfiguracionRegimenPropositoPortafolio(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerConfiguracionRegimenPropositoPortafolio");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<String> regimenes = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->solicitud.IdRegimenTributario).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdRegimenFondoMutuo", regimenes);
		List<ConfiguracionPropositoPortafolio> configuracionesRegimenPortafolio = sql.query(con, SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO, parametros, ConfiguracionPropositoPortafolio.class);
		Map<String,ConfiguracionPropositoPortafolio> configuracionesRegimenPropositoPortafolio = configuracionesRegimenPortafolio.parallelStream().collect(Collectors.toMap(configuracion->configuracion.Id, configuracion->configuracion));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerConfiguracionRegimenPropositoPortafolio", fin - inicio);
		return configuracionesRegimenPropositoPortafolio;
	}
	
	private Map<String,Moneda> obtenerInformacionMonedasFondo(Connection con, List<SerieFondoSolicitudes> seriesFondo) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerInformacionMonedasFondo");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<String> monedas = seriesFondo.parallelStream().map(series->series.IdMoneda).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdMoneda", monedas);
		List<Moneda> monedasFondo = sql.query(con, SQL_SELECT_MONEDA, parametros, Moneda.class);
		Map<String,Moneda> informacionMonedasFondo = monedasFondo.parallelStream().collect(Collectors.toMap(moneda->moneda.IdMoneda, moneda->moneda));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerInformacionMonedasFondo", fin - inicio);
		return informacionMonedasFondo;
	}
	
	private Map<Long,ResultadoFecha> obtenerFechaSolicitudes(Connection con,EntradaCrearSolicitudAportePorDividendosCFI entrada,InformacionProceso informacionProceso) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerFechaSolicitudes");
		long inicio = System.currentTimeMillis();
		CalcularFechaSolicitudAporteRescate calculoFechas = new CalcularFechaSolicitudAporteRescate();
		EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
		entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFechas.p_IdPais = informacionProceso.IdPaisSistema;
		entradaFechas.p_SolicitudAporteRescate = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->{
			SerieFondoSolicitudes serie = informacionProceso.InformacionSerieFondo.get(solicitud.IdSerieFondo);
			CalcularFechaSolicitudAporteRescate.InFechas fecha = new CalcularFechaSolicitudAporteRescate.InFechas();
			if(serie != null){
				fecha.IdInterno = solicitud.IdInterno;
				fecha.IdSerieFondo = solicitud.IdSerieFondo;
				fecha.FechaEmision = Fecha.stringToSqlDate(serie.FechaFondo);
				fecha.EstadoFondo = _c.Abierto;
				fecha.EstadoLiquidacion = _c.Abierto;
				fecha.TipoSolicitud = _c.TipoSolicitudAporte;
				fecha.FondoExtranjero = serie.FondoExtranjero;
				fecha.IdPaisFondo = serie.IdPaisOrigen;
				fecha.IdPaisEmisor = serie.IdPaisEmisor;
				Moneda moneda = informacionProceso.InformacionMonedaFondo.get(serie.IdMoneda);
				fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
				if(_c.NO.equals(informacionProceso.RespetaValutasCargaMasiva)){
					fecha.NumeroDiasRetencion = 0L;
					fecha.NumeroValutaValorCuota = 0L;
					fecha.NumeroValutaCertificacion = 0L;
					fecha.NumeroValutaCurse = 0L;
					fecha.SolicitudBatch = _c.SI;
				}else{
					fecha.NumeroDiasRetencion = serie.DiasRetencionesAporte;
					fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaAporte;
					fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionAporte;
					fecha.NumeroValutaCurse = serie.DiasValutaCurseAporte;					
					fecha.SolicitudBatch = _c.NO;
				}
			}
			return fecha;
		}).collect(Collectors.toList());
		SalidaCalcularFechaSolicitudAporteRescate salidaFechas = calculoFechas.logica(con, entradaFechas);
		Map<Long,ResultadoFecha> fechasSolicitudes =  salidaFechas.P_FECHASAPORTERESCATE.parallelStream().collect(Collectors.toMap(fecha->fecha.IdInterno,fecha->fecha));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerFechaSolicitudes", fin - inicio);
		return fechasSolicitudes;
	}
	
	private Map<Long,CalcularMontoNumeroCuota.Resultado> obtenerMontoCuotaSolicitudes(Connection con,EntradaCrearSolicitudAportePorDividendosCFI entrada,InformacionProceso informacionProceso) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerMontoCuotaSolicitudes");
		long inicio = System.currentTimeMillis();
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCalculo.p_Series = entrada.p_SolicitudesAporte.parallelStream().map(solicitud->{
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = informacionProceso.InformacionSerieFondo.get(solicitud.IdSerieFondo);
			ResultadoFecha fecha = informacionProceso.FechaSolicitudes.get(solicitud.IdInterno);
			serie.IdInterno = solicitud.IdInterno;			
			if (solicitud.FechaValorCuota != null) {
				serie.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			} else if (fecha != null) {
				serie.Fecha = Fecha.stringToSqlDate(fecha.FechaAceptacion);
			} else {
				serie.Fecha = informacionProceso.FechaSistema;
			}
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud = _c.TipoSolicitudAporte;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	return serie;
		}).collect(Collectors.toList());
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		Map<Long,CalcularMontoNumeroCuota.Resultado> montoCuotaSolicitudes = salidaCalculo.P_OUT_RESULTADO.parallelStream().collect(Collectors.toMap(resultado->resultado.IdInterno,resultado->resultado));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerMontoCuotaSolicitudes", fin - inicio);
		return montoCuotaSolicitudes;
	}
	
	private List<SolicitudAporteRescate> obtenerSolicitudesProcesar(Connection con,EntradaCrearSolicitudAportePorDividendosCFI entrada,InformacionProceso informacionProceso) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerMontoCuotaSolicitudes");
		long inicio = System.currentTimeMillis();
		int index = 0;
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(informacionProceso.FechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			ClientePortafolioSolicitudes cliente = informacionProceso.InformacionClientePortafolio.get(solicitud.IdPortafolio);
			SerieFondoSolicitudes serieFondo = informacionProceso.InformacionSerieFondo.get(solicitud.IdSerieFondo);
			CalcularMontoNumeroCuota.Resultado montoCuotaSolicitud = informacionProceso.MontoCuotaSolicitudes.get(solicitud.IdInterno);
			RegimenSerieDeFondo regimenSerieFondo = informacionProceso.InformacionRegimenSerieDeFondo.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			ResultadoFecha fecha = informacionProceso.FechaSolicitudes.get(solicitud.IdInterno);
			
			SolicitudAporteRescate solicitudProceso = new SolicitudAporteRescate();
			solicitudProceso.IdInterno = solicitud.IdInterno;
			solicitudProceso.IdSolicitudAporteRescate = informacionProceso.IdSolicitudesAporteRescate.get(index);
			solicitudProceso.IdCliente = solicitud.IdCliente;
		 	solicitudProceso.IdPortafolio = solicitud.IdPortafolio;
		 	solicitudProceso.TipoSolicitud = _c.TipoSolicitudAporte;
		 	solicitudProceso.IdTipoMovimientoFondoMutuo = _c.AporteDividendoCFI;
		 	solicitudProceso.IdDividendo = 0L;
			solicitudProceso.IdSerieFondoDestino = "";
			solicitudProceso.IdEmisorDestino = "";
			solicitudProceso.IdClienteDestino = 0L;
			solicitudProceso.IdPortafolioDestino = 0L;
			solicitudProceso.IndicadorMLT = _c.NO;
			solicitudProceso.Overnight = _c.NO;
			solicitudProceso.IdSolicitudAporteRescateRelacionado = 0L;
			solicitudProceso.TipoCarteraNormativa = _c.AporteFondoMutuo.equalsIgnoreCase(solicitudProceso.IdTipoMovimientoFondoMutuo) && regimenSerieFondo != null ? regimenSerieFondo.TipoCarteraNormativa : "";
			solicitudProceso.IdTipoCartera = "";
			solicitudProceso.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			solicitudProceso.FechaIngresoRegistro = fechaHoraUTC;
			solicitudProceso.FechaEmision = Fecha.toDate(informacionProceso.FechaSistema);
			solicitudProceso.FechaOriginalInversion = Fecha.toDate("0001-01-01");
			solicitudProceso.IdMoneda = solicitud.IdMoneda;
			solicitudProceso.Estado =  _c.EstadoIngresado;
			solicitudProceso.IdMedioDePago = _c.CuentaInversion;
			solicitudProceso.Glosa = solicitud.Glosa;
			solicitudProceso.IdEmpresa = entrada.p_IdEmpresa;
			solicitudProceso.IdUsuario = entrada.p_IdUsuario;
			solicitudProceso.IdModulo = entrada.p_IdModulo;
			solicitudProceso.SolicitudBatch = _c.SI;
			solicitudProceso.OrigenOperacion = _c.OrigenManual;
			solicitudProceso.IdAutorizadoOrigen = 0L;
			solicitudProceso.IdRegimenTributario = solicitud.IdRegimenTributario;
			solicitudProceso.IngresoAutomatico = _c.SI;
			solicitudProceso.EsPorMontoLiquido = _c.NO;
			solicitudProceso.Porcentaje = BigDecimal.ZERO;
			solicitudProceso.IdTipoCanalIngreso = _c.CanalOptimus;
			solicitudProceso.FolioContrato = informacionProceso.FolioContratos.get(index);
			solicitudProceso.Visado = _c.NO; 
			solicitudProceso.IdPreferenciaPagoPortafolio = 0L;
			solicitudProceso.EstadoProceso = _c.EstadoPendiente;
			solicitudProceso.Monto = BigDecimal.ZERO;
			solicitudProceso.NumeroCuotas = BigDecimal.ZERO;
			solicitudProceso.ValorCuota = BigDecimal.ZERO;
			solicitudProceso.CostoUF = BigDecimal.ZERO;
		    solicitudProceso.CostoMonedaLocal = BigDecimal.ZERO; 
			solicitudProceso.ValorUFFechaInversion = BigDecimal.ZERO; 
			solicitudProceso.ParidadFechaInversion = BigDecimal.ZERO;
			solicitudProceso.IdMonedaSistema = "";
			solicitudProceso.IdMonedaPago = solicitud.IdMoneda;
			solicitudProceso.MontoMonedaSistema = BigDecimal.ZERO;
			solicitudProceso.MontoPagoLocal = BigDecimal.ZERO; 
			solicitudProceso.MontoPago = BigDecimal.ZERO;
			solicitudProceso.MontoComision = BigDecimal.ZERO;
			solicitudProceso.MontoComisionLocal = BigDecimal.ZERO;
			solicitudProceso.GeneraSpot = _c.NO;
			solicitudProceso.ValidaSpot = _c.NO;
			solicitudProceso.ForzarSpot = _c.NO;
			solicitudProceso.TipoContratoOperacion = "";
			solicitudProceso.FolioContratoOperacion = "";
			solicitudProceso.IdSerieFondo = "";
			solicitudProceso.IdTipoInstrumento = "";
			solicitudProceso.IdInstrumento = 0L;
			solicitudProceso.IdEmisorOrigen = "";
			if(serieFondo != null) {
				solicitudProceso.IdSerieFondo = serieFondo.IdSerieFondo;
				solicitudProceso.IdTipoInstrumento = serieFondo.IdTipoInstrumento;
				solicitudProceso.IdInstrumento = serieFondo.IdInstrumento;
				solicitudProceso.IdEmisorOrigen = serieFondo.IdEmisor;
			}
			solicitudProceso.IdEmpleadoEjecutivo = 0L;
			solicitudProceso.IdPersona = 0L;
			if(cliente != null) {
				solicitudProceso.IdEmpleadoEjecutivo = cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
				solicitudProceso.IdPersona = cliente.IdPersona;
			}
			
			if(montoCuotaSolicitud != null){
				if (montoCuotaSolicitud.ValorCuota != null) {
					solicitudProceso.Monto = montoCuotaSolicitud.Monto;
					solicitudProceso.NumeroCuotas = montoCuotaSolicitud.NumeroCuotas;
					solicitudProceso.ValorCuota = montoCuotaSolicitud.ValorCuota;
					solicitudProceso.CostoUF = montoCuotaSolicitud.CostoEnFactorAjuste;
					solicitudProceso.CostoMonedaLocal = montoCuotaSolicitud.MontoDestino; 
					solicitudProceso.ValorUFFechaInversion = montoCuotaSolicitud.ValorFactorAjuste; 
					solicitudProceso.ParidadFechaInversion = montoCuotaSolicitud.ValorParidad;
					solicitudProceso.IdMonedaSistema = montoCuotaSolicitud.IdMonedaSistema;
					solicitudProceso.MontoMonedaPago = montoCuotaSolicitud.Monto;
					solicitudProceso.MontoMonedaSistema = montoCuotaSolicitud.MontoMonedaSistema;
					solicitudProceso.MontoComision = BigDecimal.ZERO;
					solicitudProceso.MontoComisionLocal = BigDecimal.ZERO;
					solicitudProceso.MontoPagoLocal = montoCuotaSolicitud.MontoDestino != null ? montoCuotaSolicitud.MontoDestino : BigDecimal.ZERO;
					solicitudProceso.MontoPago = montoCuotaSolicitud.Monto != null ? montoCuotaSolicitud.Monto : BigDecimal.ZERO;
					solicitudProceso.MontoInversion = montoCuotaSolicitud.Monto;
					solicitudProceso.CostoMonedaLocalInversion = montoCuotaSolicitud.MontoDestino;
					solicitudProceso.ValorCuotaInversion = montoCuotaSolicitud.ValorCuota;
					solicitudProceso.ParidadFechaCertificacion = montoCuotaSolicitud.ValorParidad;
					solicitudProceso.NumeroCuotasInversion = montoCuotaSolicitud.NumeroCuotas;
					if(_c.SI.equals(informacionProceso.AporteGeneraCompraSpot)) {
						solicitudProceso.IdMonedaPago = montoCuotaSolicitud.IdMonedaSistema;
						solicitudProceso.MontoMonedaPago = montoCuotaSolicitud.MontoMonedaSistema;
					}
				
				}
			}
			
			if(fecha != null){
				solicitudProceso.FechaAceptacion = fecha.FechaAceptacion;
			    solicitudProceso.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : fecha.FechaValorCuota;
			    solicitudProceso.FechaCertificacion = fecha.FechaCertificacion;
			    solicitudProceso.FechaLiquidacion = fecha.FechaLiquidacion;
			}
			
			if(regimenSerieFondo != null){
				switch(regimenSerieFondo.GrupoRegimen){
					case "APV":
						solicitudProceso.Tributacion = _c.TributacionAPV;
						break;
					case "ART108":
						solicitudProceso.Tributacion = _c.TributacionART108;
						break;
					case "ART107":
						solicitudProceso.Tributacion = _c.TributacionART107;
						break;
					case "57BIS":
						solicitudProceso.Tributacion = _c.Tributacion57BIS;
						break;
					default:
						solicitudProceso.Tributacion = _c.TributacionDefecto;
						break;
				}
			}
			solicitudesProcesar.add(solicitudProceso);
			index++;
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerMontoCuotaSolicitudes", fin - inicio);
		return solicitudesProcesar;
	}
	
	private Map<Long,ResultadoSolicitud> obtenerResultadoSimulado(List<SolicitudAporteRescate> solicitudesProcesar,InformacionProceso informacionProceso){
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerResultadoSimulado");
		long inicio = System.currentTimeMillis();
		
		Map<Long,ResultadoSolicitud> solicitudesSalida = solicitudesProcesar.parallelStream().map(solicitud->{
			ClientePortafolioSolicitudes cliente = informacionProceso.InformacionClientePortafolio.get(solicitud.IdPortafolio);
			SerieFondoSolicitudes serieFondo = informacionProceso.InformacionSerieFondo.get(solicitud.IdSerieFondo);
			ResultadoSolicitud solicitudSalida = new ResultadoSolicitud();
			solicitudSalida.IdInterno = solicitud.IdInterno;
			solicitudSalida.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
			solicitudSalida.TipoSolicitud = solicitud.TipoSolicitud;
			solicitudSalida.IdSerieFondo = solicitud.IdSerieFondo;
			solicitudSalida.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
			solicitudSalida.IdSerieFondoDestino = solicitud.IdSerieFondoDestino;
			solicitudSalida.IdEmisorDestino = solicitud.IdEmisorDestino;
			solicitudSalida.IndicadorMLT = solicitud.IndicadorMLT;
			solicitudSalida.Overnight = solicitud.Overnight;
			solicitudSalida.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			solicitudSalida.EsPorMontoLiquido = solicitud.EsPorMontoLiquido;
			solicitudSalida.Monto = solicitud.Monto;
			solicitudSalida.NumeroCuotas = solicitud.NumeroCuotas;
			solicitudSalida.ValorCuota = solicitud.ValorCuota;
			solicitudSalida.CostoUF = solicitud.CostoUF;
			solicitudSalida.CostoMonedaLocal = solicitud.CostoMonedaLocal;
			solicitudSalida.ValorUFFechaInversion = solicitud.ValorUFFechaInversion;
			solicitudSalida.ParidadFechaInversion = solicitud.ParidadFechaInversion;
			solicitudSalida.IdMoneda = solicitud.IdMoneda;
			solicitudSalida.MontoComision = solicitud.MontoComision;
			solicitudSalida.MontoPago = solicitud.MontoPago;
			solicitudSalida.MontoComisionLocal = solicitud.MontoComisionLocal;
			solicitudSalida.MontoPagoLocal = solicitud.MontoPagoLocal;
			solicitudSalida.IdEmisorOrigen = solicitud.IdEmisorOrigen;
			solicitudSalida.MontoLiquido = solicitud.MontoLiquido;
			solicitudSalida.MontoLiquidoLocal = solicitud.MontoLiquidoLocal;
			solicitudSalida.Porcentaje = solicitud.Porcentaje;
			solicitudSalida.FolioContrato = solicitud.FolioContrato;
			solicitudSalida.ParidadFechaCertificacion = solicitud.ParidadFechaCertificacion;
			solicitudSalida.TipoCarteraNormativa = solicitud.TipoCarteraNormativa;
			solicitudSalida.IdMonedaSistema = solicitud.IdMonedaSistema;
			solicitudSalida.MontoMonedaSistema = solicitud.MontoMonedaSistema;
			solicitudSalida.IdSerieFondoOrigen = solicitud.IdSerieFondoOrigen;
			solicitudSalida.IdAgrupador = solicitud.IdAgrupador;
			solicitudSalida.Switch = solicitud.Switch;
			solicitudSalida.IdTipoCanalIngreso = solicitud.IdTipoCanalIngreso;
			if(serieFondo != null){
				solicitudSalida.IdFondo = serieFondo.IdFondo;
				solicitudSalida.Nemotecnico = serieFondo.Nemotecnico;
				solicitudSalida.NombreFondo = serieFondo.NombreFondo;
				solicitudSalida.NombreCorto = serieFondo.NombreCortoTipoFondo;
				solicitudSalida.NombreSerieFondo = serieFondo.NombreSerieFondo;
				solicitudSalida.CodigoSerie = serieFondo.CodigoSerie;
				solicitudSalida.IdEmisorOrigen = serieFondo.IdEmisor;
			}
			if(cliente != null){
				solicitudSalida.DocumentoIdentidad = cliente.DocumentoIdentidad;
				solicitudSalida.CodigoPortafolio = cliente.CodigoPortafolio;
				solicitudSalida.NombreCliente = cliente.NombreCompleto;
			}
			solicitudSalida.MontoTransferencia = solicitud.Monto;
			solicitudSalida.NumeroCuotaTransferencia = solicitud.NumeroCuotas;
			solicitudSalida.FechaAceptacion = Fecha.stringToSqlDate(solicitud.FechaAceptacion);
			solicitudSalida.FechaOriginalInversion = Fecha.stringToSqlDate(solicitud.FechaOriginalInversion);
			solicitudSalida.FechaIngresoRegistro = Fecha.toStringTimeStamp(solicitud.FechaIngresoRegistro);
			solicitudSalida.FechaLiquidacion = Fecha.stringToSqlDate(solicitud.FechaLiquidacion);
			solicitudSalida.FechaCertificacion = Fecha.stringToSqlDate(solicitud.FechaCertificacion);
			solicitudSalida.FechaValorCuota = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			return solicitudSalida;
		}).collect(Collectors.toMap(solicitud->solicitud.IdInterno,solicitud->solicitud));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerResultadoSimulado", fin - inicio);
		return solicitudesSalida;
	}
	
	private SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate validarRequisitosFondoMutuo(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada, List<SolicitudAporteRescate> solicitudesProcesar,InformacionProceso informacionProceso) throws Exception {
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.validarRequisitosFondoMutuo");
		long inicio = System.currentTimeMillis();
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		for(SolicitudAporteRescate solicitudProcesar : solicitudesProcesar) {
			ClientePortafolioSolicitudes cliente = informacionProceso.InformacionClientePortafolio.get(solicitudProcesar.IdPortafolio);
			SerieFondoSolicitudes serieFondo = informacionProceso.InformacionSerieFondo.get(solicitudProcesar.IdSerieFondo);
			TipoMovimientoFondoMutuo tipoMovimiento = informacionProceso.InformacionTipoMovimientoFondoMutuo.get(_c.AporteDividendoCFI);
			RegimenSerieDeFondo regimenSerieFondo = informacionProceso.InformacionRegimenSerieDeFondo.get(solicitudProcesar.IdSerieFondo + "_" +solicitudProcesar.IdRegimenTributario);
			IndicadoresPortafolio indicadorPortafolio = informacionProceso.InformacionIndicadoresUsaCuentaInversionPortafolio.get(solicitudProcesar.IdPortafolio);
			ConfiguracionPropositoPortafolio configuracionRegimenPropositoPortafolio = null;
			if(cliente != null) configuracionRegimenPropositoPortafolio = informacionProceso.InformacionConfiguracionRegimenPropositoPortafolio.get(solicitudProcesar.IdRegimenTributario + "_" + cliente.Proposito);
			
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solicitudValidar = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			solicitudValidar.IdInterno = solicitudProcesar.IdInterno;
			solicitudValidar.IdCliente = solicitudProcesar.IdCliente;
			solicitudValidar.IdPortafolio = solicitudProcesar.IdPortafolio;
			solicitudValidar.IdSerieFondo = solicitudProcesar.IdSerieFondo;
			solicitudValidar.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
			solicitudValidar.Monto = solicitudProcesar.Monto;
			solicitudValidar.NumeroCuotas = solicitudProcesar.NumeroCuotas;
			solicitudValidar.IdEstadoSerie = "";
			solicitudValidar.EsDiferido = _c.SI;
			solicitudValidar.EsMontoMaximo = _c.SI;
			solicitudValidar.EsMontoMinimo = _c.SI;
			solicitudValidar.PermiteCambioGrupo = _c.SI;
			solicitudValidar.EsPortafolioValido = _c.SI;
			solicitudValidar.PermiteRescateMandato = _c.SI;
			solicitudValidar.EsCuentaInversionValido = _c.SI;
			solicitudValidar.EsFechaValidaAporte = _c.SI;
			solicitudValidar.EsFechaValidaRescate = _c.SI;
			solicitudValidar.ValorCuota = solicitudProcesar.ValorCuota;
			solicitudValidar.Overnight = _c.NO;
			solicitudValidar.EsMoneyMarket = _c.SI;
			solicitudValidar.EsFamiliaEquivalente = _c.SI;
			solicitudValidar.EsPortafolioRegimenValido = _c.SI;
			solicitudValidar.EsRegimenValido = _c.SI;
			solicitudValidar.EsCuentaInversionValido = _c.SI;
			solicitudValidar.PermiteAporte = _c.SI;
			solicitudValidar.EsRegimenDestinoValido = _c.SI;
			solicitudValidar.PermiteRescate = _c.SI;
			solicitudValidar.Glosa = solicitudProcesar.Glosa;
			if(serieFondo != null){
				solicitudValidar.IdEstadoSerie = serieFondo.IdEstado;
				solicitudValidar.RestringeAporte = serieFondo.RestringeAporte;
				solicitudValidar.RestringeRescate = serieFondo.RestringeRescate;
				solicitudValidar.EstadoFondo = _c.Abierto;
				solicitudValidar.EstadoBloqueo = serieFondo.BloqueoAporte;
				solicitudValidar.EsMontoMaximo = serieFondo.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && serieFondo.MontoMaximo.compareTo(solicitudProcesar.Monto) == -1 ? _c.NO : _c.SI;
				solicitudValidar.EstadoLiquidez = _c.Abierto;
				solicitudValidar.EsFondoValido = _c.SI;
				if(!solicitudProcesar.IdMoneda.equalsIgnoreCase(serieFondo.IdMoneda) && _c.NO.equalsIgnoreCase(serieFondo.Diferido)){
					solicitudValidar.EsDiferido = _c.NO;
				}
				
			}
			if(regimenSerieFondo == null){
				solicitudValidar.EsRegimenValido = _c.NO;
			}else {
				solicitudValidar.PermiteAporte = regimenSerieFondo.PermiteAporte;
			}
			
			if(configuracionRegimenPropositoPortafolio == null){
				solicitudValidar.EsPortafolioRegimenValido = _c.NO;
			}
			if(_c.NO.equalsIgnoreCase(indicadorPortafolio.ValorIndicador)){
				solicitudValidar.EsCuentaInversionValido = _c.NO;
			}
			
			if(cliente != null){
				portVal.IdInterno = solicitudProcesar.IdInterno;
				portVal.IdPortafolio = solicitudProcesar.IdPortafolio;
				portVal.IdPortafolioReal = cliente.IdPortafolio;
				portVal.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
				portVal.IdCliente = solicitudProcesar.IdCliente;
				portVal.IdClienteReal = cliente.IdCliente;
				portVal.IdEstadoCliente = cliente.IdEstadoCliente;
				portVal.DocumentoIdentidad = cliente.DocumentoIdentidad;
				portVal.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
				portVal.IdClienteDestino = solicitudProcesar.IdClienteDestino;
				portVal.IdClienteRealDestino = 0L;
				portVal.IdPortafolioDestino = solicitudProcesar.IdPortafolio;
				portVal.IdPortafolioRealDestino = 0L;
				portVal.IdEstadoClienteDestino = "";
				portVal.IdEstadoPortafolioDestino = "";
			}
			
			solicitudValidar.IdTipoOperacion= _c.TipoOperacionAporteFondoMutuo;
			solicitudValidar.IdTipoProducto=_c.TipoOperacion;
			if (cliente.CodigoPortafolio.equalsIgnoreCase(_c.BonificacionFiscal))
			{
				solicitudValidar.EsMontoMinimo = _c.SI;
			}
		
			entradaCumpleRequisitos.p_Portafolios.add(portVal);
			entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solicitudValidar);
		}
		
		SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate salidaCumpleRequisitos = cumpleRequisitos.logica(con, entradaCumpleRequisitos);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.validarRequisitosFondoMutuo", fin - inicio);
		return salidaCumpleRequisitos;
	}
	
	private Map<String,BigDecimal> obtenerMontosTotalesPorPortafolio(List<SolicitudAporteRescate> solicitudesProcesar){
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerMontosTotalesPorPortafolio");
		long inicio = System.currentTimeMillis();
		Map<String,BigDecimal> montosTotalesPorPortafolio = new HashMap<String,BigDecimal>();
		for(SolicitudAporteRescate sol : solicitudesProcesar) {
			if( _c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
				String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
				BigDecimal montoAcum = montosTotalesPorPortafolio.get(id) != null ? montosTotalesPorPortafolio.get(id) : BigDecimal.ZERO;
				montosTotalesPorPortafolio.put(id, montoAcum.add(sol.MontoMonedaPago, MathContext.DECIMAL128));
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerMontosTotalesPorPortafolio", fin - inicio);
		return montosTotalesPorPortafolio;
	}
	
	private SaldoFlujoCaja.ServiceSalida obtenerFlujoCaja(Connection con, EntradaCrearSolicitudAportePorDividendosCFI entrada, List<SolicitudAporteRescate> solicitudesProcesar,InformacionProceso informacionProceso) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerFlujoCaja");
		long inicio = System.currentTimeMillis();
		Date fechaMaximaCertificacion = Fecha.toDate("0001-01-01");
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && sol.FechaCertificacion.compareTo(fechaMaximaCertificacion) == 1){
				fechaMaximaCertificacion = sol.FechaCertificacion;
			}
		}

		List<Long> portFlujo = new ArrayList<Long>();
		List<String> monedaFlujo = new ArrayList<String>();
		
		SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
		SaldoFlujoCaja.ServiceEntrada entradaSaldoFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
		entradaSaldoFlujoCaja.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaSaldoFlujoCaja.P_FECHADESDE = informacionProceso.FechaSistema;
		entradaSaldoFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaMaximaCertificacion);
		entradaSaldoFlujoCaja.P_USO = _c.UsoFlujoCajaIngreso;
		entradaSaldoFlujoCaja.P_DESPLEGARENUI 	= _c.NO;
		entradaSaldoFlujoCaja.P_AGRUPARPORORIGEN = _c.NO;
		entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
		
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if( _c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
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
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerFlujoCaja", fin - inicio);
		return salidaFlujoCaja;
	}
	
	private Map<Long, SolicitudAporteRescate> obtenerSolicitudesSinFlujoCaja(List<SolicitudAporteRescate> solicitudesProcesar,InformacionProceso informacionProceso) {
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerSolicitudesSinFlujoCaja");
		long inicio = System.currentTimeMillis();
		List<SolicitudAporteRescate> solicitudesSinSaldoCaja = new ArrayList<SolicitudAporteRescate>();
		for(SolicitudAporteRescate solicitud : solicitudesProcesar){
			if(_c.EstadoRechazado.equalsIgnoreCase(solicitud.EstadoProceso)) continue;
			
			String id = solicitud.IdPortafolio.toString() + "_" + solicitud.IdMonedaPago;
			String fecha = Fecha.stringToSqlDate(solicitud.FechaCertificacion);   
			 
			BigDecimal saldo = BigDecimal.ZERO;
			BigDecimal total = informacionProceso.MontosTotalesPorPortafolio.get(id) != null ? informacionProceso.MontosTotalesPorPortafolio.get(id) : BigDecimal.ZERO;
			
			for(SaldoFlujoCaja.ObjFLUJOCAJA f : informacionProceso.FlujosCaja.P_OUT_FLUJOCAJA){		
				if(f.IdPortafolio.longValue() == solicitud.IdPortafolio && f.IdMoneda.equalsIgnoreCase(solicitud.IdMonedaPago) && f.FechaSaldo.equalsIgnoreCase(fecha)){
					saldo = f.Saldo;
				}
			}

			if(total.compareTo(saldo) == 1){
				solicitudesSinSaldoCaja.add(solicitud);
			}
		}
		Map<Long,SolicitudAporteRescate> solicitudesSinSaldoCajaPorIdInterno = solicitudesSinSaldoCaja.parallelStream().collect(Collectors.toMap(solicitud->solicitud.IdInterno, solicitud->solicitud));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerSolicitudesSinFlujoCaja", fin - inicio);
		return solicitudesSinSaldoCajaPorIdInterno;
	}
	
	private SalidaProcesarIngresoOperacionSpot generarMovimientosSpot(Connection con,EntradaCrearSolicitudAportePorDividendosCFI entrada,List<SolicitudAporteRescate> solicitudesProcesar) throws Exception {
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.generarMovimientosSpot");
		long inicio = System.currentTimeMillis();
		List<SolicitudAporteRescate> solicitudesSpot = solicitudesProcesar.parallelStream().filter(solicitud->!solicitud.IdMoneda.equals(solicitud.IdMonedaPago)).collect(Collectors.toList());
		SalidaProcesarIngresoOperacionSpot salidaSpot = new SalidaProcesarIngresoOperacionSpot();
		ProcesarIngresoOperacionSpot procesarSpot = new ProcesarIngresoOperacionSpot();
		EntradaProcesarIngresoOperacionSpot entradaprocesarSpot = new EntradaProcesarIngresoOperacionSpot();
		entradaprocesarSpot.p_DatosOperacion.addAll(solicitudesSpot);
		Long contadorFormaDePago = 1L;
		for(SolicitudAporteRescate solicitud : solicitudesSpot) {
			FormaPagoSpot recibimos = new FormaPagoSpot();
			recibimos.IdInternoPagoOperacion = contadorFormaDePago;
			recibimos.IdComprobantePagoOperacion = 0L;
			recibimos.IdOperacion = solicitud.IdInterno;
			recibimos.RecibePagaPortafolio = "R";
			recibimos.IdTipoMovimiento = _c.SpotTipoMovimientoIngreso;
			recibimos.IdFormaDePago = _c.SpotCompra+"_"+solicitud.IdMedioDePago+"_"+solicitud.IdMonedaSistema+"_"+_c.SpotValutaDefectoFormaDePago;
			recibimos.IdMoneda = solicitud.IdMonedaSistema;
			recibimos.IdMonedaLiquidacion = solicitud.IdMonedaSistema;
			recibimos.MontoFormaDePago = solicitud.MontoMonedaSistema;
			recibimos.AfectarTesoreria = _c.NO;
			recibimos.FechaLiquidacion = solicitud.FechaAceptacion;
			entradaprocesarSpot.p_DatosFormaPago.add(recibimos);
			contadorFormaDePago++;
			FormaPagoSpot entregamos = new FormaPagoSpot();
			entregamos.IdInternoPagoOperacion = contadorFormaDePago;
			entregamos.IdComprobantePagoOperacion = 0L;
			entregamos.IdOperacion = solicitud.IdInterno;
			entregamos.RecibePagaPortafolio = "P";
			entregamos.IdTipoMovimiento = _c.SpotTipoMovimientoIngreso;
			entregamos.IdFormaDePago = _c.SpotVenta+"_"+_c.CuentaInversion+"_"+solicitud.IdMoneda+"_"+_c.SpotValutaDefectoFormaDePago;
			entregamos.IdMoneda = solicitud.IdMoneda;
			entregamos.IdMonedaLiquidacion = solicitud.IdMoneda;
			entregamos.MontoFormaDePago = solicitud.Monto;
			entregamos.AfectarTesoreria = _c.NO;
			entregamos.FechaLiquidacion = solicitud.FechaAceptacion;
			entradaprocesarSpot.p_DatosFormaPago.add(entregamos);
			contadorFormaDePago++;
		}
		if(entradaprocesarSpot.p_DatosOperacion.size() > 0) {
			entradaprocesarSpot.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaprocesarSpot.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaprocesarSpot.p_IdUsuario = entrada.p_IdUsuario;
			entradaprocesarSpot.p_IdModulo = entrada.p_IdModulo;
			entradaprocesarSpot.p_CalculaMargen = _c.SpotCalculaMargen;
			entradaprocesarSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			salidaSpot = procesarSpot.logica(con, entradaprocesarSpot);
		}
		
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.generarMovimientosSpot", fin - inicio);
		return salidaSpot;
	}
	
	private List<SolicitudAporteRescate> obtenerSolicitudesCorrectas(List<SolicitudAporteRescate> solicitudesProcesar){
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerSolicitudesCorrectas");
		long inicio = System.currentTimeMillis();
		List<SolicitudAporteRescate> solicitudesCorrectas = solicitudesProcesar.parallelStream().filter(solicitud->_c.EstadoPendiente.equalsIgnoreCase(solicitud.EstadoProceso)).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerSolicitudesCorrectas", fin - inicio);
		return solicitudesCorrectas;
	}
	
	private List<ResultadoSolicitud> obtenerResultado(List<SolicitudAporteRescate> solicitudes,Map<Long,ResultadoSolicitud> resultadoSimulado){
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerResultado");
		long inicio = System.currentTimeMillis();
		List<ResultadoSolicitud> resultadosSolicitud = new ArrayList<ResultadoSolicitud>();
		for(SolicitudAporteRescate sol : solicitudes){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
				resultadosSolicitud.add((ResultadoSolicitud) resultadoSimulado.get(sol.IdInterno));
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.obtenerResultado", fin - inicio);
		return resultadosSolicitud;
	}
	
	private void insertarSolicitudes(Connection con,List<SolicitudAporteRescate> solicitudes) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.insertarSolicitudes");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_SOLICITUD, solicitudes, SolicitudAporteRescate.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.insertarSolicitudes", fin - inicio);
		
	}
	
	private void insertarLog(Connection con,EntradaCrearSolicitudAportePorDividendosCFI entrada,InformacionProceso informacionProceso,List<SolicitudAporteRescate> solicitudes) throws Exception{
		logger.trace("INICIO::CrearSolicitudAportePorDividendosCFI.obtenerResultado");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_Log = solicitudes.parallelStream().map(solicitud->{
			ClientePortafolioSolicitudes cliente = informacionProceso.InformacionClientePortafolio.get(solicitud.IdPortafolio);
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = solicitud.IdSolicitudAporteRescate;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = _c.ModuloFondoMutuo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = informacionProceso.FechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = solicitud.IdCliente;
			log.EstadoCliente = cliente.IdEstadoCliente;
			log.NombreCliente = cliente.NombreCompleto;
			log.IdTipoDocumentoIdentidad = cliente.TipoDocumentoIdentidad;
			log.IdDocumentoIdentidad = cliente.DocumentoIdentidad;
			log.IdPortafolio = solicitud.IdPortafolio;
			log.EstadoPortafolio = cliente.IdEstadoPortafolio;
			log.IdTipoRegistro = "SOLICITUD_APORTE_RESCATE";
			log.EstadoRegistro = solicitud.Estado;
			log.IdTipoOperacion = solicitud.IdTipoMovimientoFondoMutuo;
			log.Folio = solicitud.IdSolicitudAporteRescate;
			log.Cantidad = solicitud.NumeroCuotas;
			log.PrecioTasa = solicitud.ValorCuota;
			log.Monto = solicitud.Monto;
			log.IdMonedaOrigen = solicitud.IdMoneda;
			log.IdMonedaLiquidacion = solicitud.IdMoneda;
			log.TipoCambio = solicitud.ParidadFechaInversion.doubleValue()+"";
			log.Nemo = solicitud.IdSerieFondo;
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";		
			return log;
		}).collect(Collectors.toList());
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSolicitudAportePorDividendosCFI.insertarSolicitudes", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":524891,\"IdPortafolio\":1451809,\"IdSerieFondo\":\"FMOPTIMUS_A\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":12734.7978,\"NumeroCuotas\":10.5981,\"IdMoneda\":\"USD\",\"IdRegimenTributario\":\"18Q\",\"Glosa\":\"APORTE DOLAR\"}]}";		
		EntradaCrearSolicitudAportePorDividendosCFI Objin = gson.fromJson(json, EntradaCrearSolicitudAportePorDividendosCFI.class);
		CrearSolicitudAportePorDividendosCFI demo = new CrearSolicitudAportePorDividendosCFI();				
		try {
			SalidaCrearSolicitudAportePorDividendosCFI ObjOut = demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	}
		System.out.println("== END ==");
	}

}
