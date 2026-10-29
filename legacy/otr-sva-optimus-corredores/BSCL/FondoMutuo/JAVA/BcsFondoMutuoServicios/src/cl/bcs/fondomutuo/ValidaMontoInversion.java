package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ValidaMontoInversion implements JavaCall	 {
	
	static final Logger logger = LogManager.getLogger(ValidaMontoInversion.class);

	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaValidaMontoInversion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<Solicitud> p_Solicitudes;
		
		public EntradaValidaMontoInversion() {
			this.p_Solicitudes= new ArrayList<Solicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class SalidaValidaMontoInversion {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidaMontoInversion(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValidacionMonto {
		public Long IdInterno;
		public Long FolioContrato;
		public BigDecimal MontoTotalInversion;
		public BigDecimal MontoTotalAportesAltos;
		public List<Solicitud> Solicitudes;
		
		public ValidacionMonto() {
			this.IdInterno = 0L;
			this.FolioContrato = 0L;
			this.MontoTotalInversion = BigDecimal.ZERO;
			this.MontoTotalAportesAltos = BigDecimal.ZERO;
			this.Solicitudes = new ArrayList<Solicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Solicitud {
		public Long IdInterno;
		public Long FolioContrato;
		public String AportesAltos;
		public BigDecimal Monto;
		public String ValidaMontoMinimo;
		
		public Solicitud() {
			this.ValidaMontoMinimo = "S";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MontoLimite {
		public BigDecimal Minimo;
		public BigDecimal Maximo;
		
		public MontoLimite() {
			this.Minimo = BigDecimal.ZERO;
			this.Maximo = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaValidaMontoInversion logica(Connection con, EntradaValidaMontoInversion entrada) throws Exception{
		logger.info("INICIO::ValidaMontoInversion");
		SalidaValidaMontoInversion out = new SalidaValidaMontoInversion();
		
		List<Solicitud> solicitudesConFolioContrato = obtenerSolicitudesConFolioContrato(entrada.p_Solicitudes);
		List<Solicitud> solicitudesSinFolioContrato = obtenerSolicitudesSinFolioContrato(entrada.p_Solicitudes);
		Map<Long,List<Solicitud>> solicitudesAgrupadasPorFolioContrato = obtenerSolicitudesAgrupadasPorFolioContrato(solicitudesConFolioContrato);
		
		List<ValidacionMonto> montosTotalesSolicitudesConFolioContrato = obtenerMontosSolicitudesConFolioContrato(solicitudesAgrupadasPorFolioContrato);
		List<ValidacionMonto> montosTotalesSolicitudesSinFolioContrato = obtenerMontosSolicitudesSinFolioContrato(solicitudesSinFolioContrato);
		
		List<ValidacionMonto> montosFinales = new ArrayList<ValidacionMonto>();
		montosFinales.addAll(montosTotalesSolicitudesConFolioContrato);
		montosFinales.addAll(montosTotalesSolicitudesSinFolioContrato);
		
		List<MensajeSalida> montosConErrores = obtenerMontosConErrores(con, entrada.p_IdEmpresa, montosFinales);
		
		out.P_OUT_MENSAJES = montosConErrores;
		out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.info("FIN::ValidaMontoInversion");
		return out;
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaValidaMontoInversion jin = gson.fromJson(in.toString(), EntradaValidaMontoInversion.class);
		SalidaValidaMontoInversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private BigDecimal obtenerMontoMinimoTotalInversion(Connection con, Long idEmpresa) {
		logger.trace("INICIO::ValidaMontoInversion.obtenerMontoMinimoTotalInversion");
		BigDecimal montoMinimoTotalInversion = BigDecimal.ZERO;
		try {
			montoMinimoTotalInversion = new BigDecimal( ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "MontoMinimoTotalInversion"));
		}catch(Exception e) {
			logger.error("ADVERTENCIA::ValidaMontoInversion.obtenerMontoMinimoTotalInversion::No existe parametro::MontoMinimoTotalInversion");
		}
		logger.trace("FIN::ValidaMontoInversion.obtenerMontoMinimoTotalInversion");
		return montoMinimoTotalInversion;
	}
	
	private BigDecimal obtenerMontoMaximoTotalInversion(Connection con, Long idEmpresa) {
		logger.trace("INICIO::ValidaMontoInversion.obtenerMontoMaximoTotalInversion");
		BigDecimal montoMaximoTotalInversion = BigDecimal.ZERO;
		try {
			montoMaximoTotalInversion = new BigDecimal( ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "MontoMaximoTotalInversion"));
		}catch(Exception e) {
			logger.error("ADVERTENCIA::ValidaMontoInversion.obtenerMontoMaximoTotalInversion::No existe parametro::MontoMaximoTotalInversion");
		}
		logger.trace("FIN::ValidaMontoInversion.obtenerMontoMaximoTotalInversion");
		return montoMaximoTotalInversion;
	}
	
	private BigDecimal obtenerMontoMinimoAportesAltos(Connection con, Long idEmpresa) {
		logger.trace("INICIO::ValidaMontoInversion.obtenerMontoMinimoAportesAltos");
		BigDecimal montoMinimoAportesAltos = BigDecimal.ZERO;
		try {
			montoMinimoAportesAltos = new BigDecimal( ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "MontoMinimoAportesAltos"));
		}catch(Exception e) {
			logger.error("ADVERTENCIA::ValidaMontoInversion.obtenerMontoMinimoAportesAltos::No existe parametro::MontoMinimoAportesAltos");
		}
		logger.trace("FIN::ValidaMontoInversion.obtenerMontoMinimoAportesAltos");
		return montoMinimoAportesAltos;
	}
	
	private BigDecimal obtenerMontoMaximoAportesAltos(Connection con, Long idEmpresa) {
		logger.trace("INICIO::ValidaMontoInversion.obtenerMontoMaximoAportesAltos");
		BigDecimal montoMaximoAportesAltos = BigDecimal.ZERO;
		try {
			montoMaximoAportesAltos = new BigDecimal( ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "MontoMaximoAportesAltos"));
		}catch(Exception e) {
			logger.error("ADVERTENCIA::ValidaMontoInversion.obtenerMontoMaximoAportesAltos::No existe parametro::MontoMaximoAportesAltos");
		}
		logger.trace("FIN::ValidaMontoInversion.obtenerMontoMaximoAportesAltos");
		return montoMaximoAportesAltos;
	}
	
	private List<Solicitud> obtenerSolicitudesConFolioContrato(List<Solicitud> solicitudes){
		logger.trace("INICIO::ValidaMontoInversion.obtenerSolicitudesConFolioContrato");
		List<Solicitud> solicitudesConFolioContrato = solicitudes.parallelStream().filter(solicitud->!solicitud.FolioContrato.equals(0L)).collect(Collectors.toList());
		logger.trace("FIN::ValidaMontoInversion.obtenerSolicitudesConFolioContrato");
		return solicitudesConFolioContrato;
	}
	
	private List<Solicitud> obtenerSolicitudesSinFolioContrato(List<Solicitud> solicitudes){
		logger.trace("INICIO::ValidaMontoInversion.obtenerSolicitudesSinFolioContrato");
		List<Solicitud> solicitudesSinFolioContrato = solicitudes.parallelStream().filter(solicitud->solicitud.FolioContrato.equals(0L)).collect(Collectors.toList());
		logger.trace("FIN::ValidaMontoInversion.obtenerSolicitudesSinFolioContrato");
		return solicitudesSinFolioContrato;
	}
	
	private Map<Long,List<Solicitud>> obtenerSolicitudesAgrupadasPorFolioContrato(List<Solicitud> solicitudes){
		logger.trace("INICIO::ValidaMontoInversion.obtenerSolicitudesAgrupadasPorFolio");
		Map<Long,List<Solicitud>> solicitudesAgrupadasPorFolioContrato = solicitudes.parallelStream().collect(Collectors.groupingBy(solicitud->solicitud.FolioContrato));
		logger.trace("FIN::ValidaMontoInversion.obtenerSolicitudesAgrupadasPorFolio");
		return solicitudesAgrupadasPorFolioContrato;
	}
	
	private List<ValidacionMonto> obtenerMontosSolicitudesConFolioContrato(Map<Long,List<Solicitud>> solicitudesAgrupadas){
		logger.trace("INICIO::ValidaMontoInversion.obtenerMontosSolicitudesConFolioContrato");
		List<ValidacionMonto> montosSolicitudesConFolioContrato = new ArrayList<ValidacionMonto>();
		for(Entry<Long, List<Solicitud>> solicitudAgrupada : solicitudesAgrupadas.entrySet()) {
			List<Solicitud> solicitudes = solicitudAgrupada.getValue();
			ValidacionMonto monto = new ValidacionMonto();
			monto.FolioContrato = solicitudAgrupada.getKey();
			for(Solicitud solicitud : solicitudes) {
				monto = obtenerMonto(monto, solicitud);
			}
			montosSolicitudesConFolioContrato.add(monto);
		}
		logger.trace("FIN::ValidaMontoInversion.obtenerMontosSolicitudesConFolioContrato");
		return montosSolicitudesConFolioContrato;
	}
	
	private List<ValidacionMonto> obtenerMontosSolicitudesSinFolioContrato(List<Solicitud> solicitudes){
		logger.trace("INICIO::ValidaMontoInversion.obtenerMontoSolicitudesSinFolio");
		List<ValidacionMonto> montosSolicitudesSinFolioContrato = solicitudes.parallelStream().map(solicitud->{
			ValidacionMonto monto = new ValidacionMonto();
			monto.IdInterno = solicitud.IdInterno;
			monto = obtenerMonto(monto, solicitud);
			return monto;
		}).collect(Collectors.toList());
		logger.trace("FIN::ValidaMontoInversion.obtenerMontoSolicitudesSinFolio");
		return montosSolicitudesSinFolioContrato;
		
	}
	
	private ValidacionMonto obtenerMonto(ValidacionMonto monto, Solicitud solicitud) {
		logger.trace("INICIO::ValidaMontoInversion.obtenerMonto");
		if(_c.SI.equals(solicitud.AportesAltos)) {
			monto.MontoTotalAportesAltos = monto.MontoTotalAportesAltos.add(solicitud.Monto, MathContext.DECIMAL128);
		}
		monto.MontoTotalInversion = monto.MontoTotalInversion.add(solicitud.Monto, MathContext.DECIMAL128);
		monto.Solicitudes.add(solicitud);
		logger.trace("FIN::ValidaMontoInversion.obtenerMonto");
		return monto;
	}
	
	private List<MensajeSalida> obtenerMontosConErrores(Connection con, Long idEmpresa, List<ValidacionMonto> montos) throws Exception{
		logger.trace("INICIO::ValidaMontoInversion.obtenerMontosConErrores");
		MontoLimite montoLimiteTotalInversion = new MontoLimite();
		montoLimiteTotalInversion.Minimo = obtenerMontoMinimoTotalInversion(con, idEmpresa);
		montoLimiteTotalInversion.Maximo = obtenerMontoMaximoTotalInversion(con, idEmpresa);
		
		MontoLimite montoLimiteAportesAltos = new MontoLimite();
		montoLimiteAportesAltos.Minimo = obtenerMontoMinimoAportesAltos(con, idEmpresa);
		montoLimiteAportesAltos.Maximo = obtenerMontoMaximoAportesAltos(con, idEmpresa);
		
		List<MensajeSalida> errores = new ArrayList<MensajeSalida>();
		for(ValidacionMonto monto : montos) {
			for(Solicitud solicitud : monto.Solicitudes) {
				if(_c.SI.equals(solicitud.AportesAltos)) {
					HashMap<String,String> parametros = new HashMap<String,String>();
					parametros.put("Monto", monto.MontoTotalAportesAltos.toPlainString());
					parametros.put("Minimo", montoLimiteAportesAltos.Minimo.toPlainString());
					parametros.put("Maximo", montoLimiteAportesAltos.Maximo.toPlainString());
					if(esErrorMontoAporteMinimo(monto.MontoTotalAportesAltos, montoLimiteAportesAltos.Minimo, solicitud.ValidaMontoMinimo) || esErrorMontoAporteMaximo(monto.MontoTotalAportesAltos, montoLimiteAportesAltos.Maximo)) {
						errores.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ALTOS_APORTE_FFMM",parametros,con,idEmpresa));
					}
				}
				HashMap<String,String> parametros = new HashMap<String,String>();
				parametros.put("Monto", monto.MontoTotalInversion.toPlainString());
				parametros.put("Minimo", montoLimiteTotalInversion.Minimo.toPlainString());
				parametros.put("Maximo", montoLimiteTotalInversion.Maximo.toPlainString());
				if(esErrorMontoMinimoTotalInversion(monto.MontoTotalInversion, montoLimiteTotalInversion.Minimo, solicitud.ValidaMontoMinimo) || esErrorMontoMaximoTotalInversion(monto.MontoTotalInversion, montoLimiteTotalInversion.Maximo)) {
					errores.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_MONTO_TOTAL_INVERSION_FFMM",parametros,con,idEmpresa));
				}
			}
		}
		logger.trace("FIN::ValidaMontoInversion.obtenerMontosConErrores");
		return errores;
	}
	
	private Boolean esErrorMontoAporteMinimo(BigDecimal montoAporteAltos, BigDecimal montoMinimoAportesAltos, String validaMontoMinimo){
		logger.trace("INICIO::ValidaMontoInversion.esErrorMontoAporteMinimo");
		Boolean esErrorMontoAporteMinimo = _c.SI.equals(validaMontoMinimo) && montoAporteAltos.compareTo(montoMinimoAportesAltos)==-1;
		logger.trace("FIN::ValidaMontoInversion.esErrorMontoAporteMinimo");
		return esErrorMontoAporteMinimo;
	}
	
	private Boolean esErrorMontoAporteMaximo(BigDecimal montoAporteAltos, BigDecimal montoMaximoAportesAltos){
		logger.trace("INICIO::ValidaMontoInversion.esErrorMontoAporteMaximo");
		Boolean esErrorMontoAporteMaximo = montoAporteAltos.compareTo(montoMaximoAportesAltos)==1;
		logger.trace("FIN::ValidaMontoInversion.esErrorMontoAporteMaximo");
		return esErrorMontoAporteMaximo;
	}
	
	private Boolean esErrorMontoMinimoTotalInversion(BigDecimal montoAporteAltos, BigDecimal montoMinimoTotalInversion, String validaMontoMinimo){
		logger.trace("INICIO::ValidaMontoInversion.esErrorMontoMinimoTotalInversion");
		Boolean esErrorMontoMinimoTotalInversion = _c.SI.equals(validaMontoMinimo) && montoAporteAltos.compareTo(montoMinimoTotalInversion)==-1;
		logger.trace("FIN::ValidaMontoInversion.esErrorMontoMinimoTotalInversion");
		return esErrorMontoMinimoTotalInversion;
	}
	
	private Boolean esErrorMontoMaximoTotalInversion(BigDecimal montoAporteAltos, BigDecimal montoMaximoTotalInversion){
		logger.trace("INICIO::ValidaMontoInversion.esErrorMontoMaximoTotalInversion");
		Boolean esErrorMontoMaximoTotalInversion = montoAporteAltos.compareTo(montoMaximoTotalInversion)==1;
		logger.trace("FIN::ValidaMontoInversion.esErrorMontoMaximoTotalInversion");
		return esErrorMontoMaximoTotalInversion;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		
		String Json = "";
		
		
		EntradaValidaMontoInversion Objin = gson.fromJson(Json, EntradaValidaMontoInversion.class);
		ValidaMontoInversion  demo=new ValidaMontoInversion();				
		try {
			SalidaValidaMontoInversion ObjOut=demo.logica(con, Objin);
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
