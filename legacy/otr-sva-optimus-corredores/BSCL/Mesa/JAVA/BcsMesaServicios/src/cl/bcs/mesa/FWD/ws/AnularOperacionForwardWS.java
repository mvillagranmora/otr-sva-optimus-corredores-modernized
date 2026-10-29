package cl.bcs.mesa.FWD.ws;

import java.sql.Connection;
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

import cl.bcs.mesa.FWD.AnularOperacionForward;
import cl.bcs.mesa.FWD.AnularOperacionForward.AnularOperacionForwardEntrada;
import cl.bcs.mesa.FWD.AnularOperacionForward.AnularOperacionForwardSalida;
import cl.bcs.mesa.FWD.ModificarOperacionForward;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularOperacionForwardWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(AnularOperacionForwardWS.class);
	
	public String SQL_SELECT_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/AnularOperacionForwardWS_SelectOperacionForward.sql");
	
	public static class EntradaAnularOperacionForwardWS extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<EntradaOperacionForward> p_Operaciones;
		
		public EntradaAnularOperacionForwardWS() {
			this.p_Operaciones = new ArrayList<EntradaOperacionForward>();
		}
	}
	
	public static class EntradaOperacionForward extends BcsPojo{
		public Long FolioOperacion;
	}
	
	public static class SalidaAnularOperacionForwardWS extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAnularOperacionForwardWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class OperacionForward extends BcsPojo{
		public Long FolioOperacion;
		public Long IdOperacion;
		public String IdEstado;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAnularOperacionForwardWS jin = gson.fromJson(in.toString(), EntradaAnularOperacionForwardWS.class);
		SalidaAnularOperacionForwardWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAnularOperacionForwardWS logica(Connection con, EntradaAnularOperacionForwardWS entrada) throws Exception {
		logger.info("INICIO::AnularOperacionForwardWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaAnularOperacionForwardWS out = new SalidaAnularOperacionForwardWS();
		
		Map<Long, OperacionForward> operacionesForwardPorFolioOperacion = obtenerOperacionForwardPorFolioOperacion(con, entrada.p_Operaciones);
		out = validar(con, entrada, operacionesForwardPorFolioOperacion);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		out = anularOperacionForward(con, entrada, operacionesForwardPorFolioOperacion);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AnularOperacionForwardWS", finServicio - inicioServicio);
		return out;
	}
	
	private Map<Long, OperacionForward> obtenerOperacionForwardPorFolioOperacion(Connection con, List<EntradaOperacionForward> entradaOperacionesForward) throws Exception {
		logger.debug("INICIO::AnularOperacionForwardWS.obtenerOperacionForwardPorFolioOperacion");
		long inicio = System.currentTimeMillis();
		List<Long> foliosOperacion = entradaOperacionesForward
				.stream()
				.map(entradaOperacionForward->entradaOperacionForward.FolioOperacion)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("FolioOperacion", foliosOperacion);
		
		SQL sql = new SQL();
		List<OperacionForward> operacionesForward = sql.query(con, SQL_SELECT_OPERACION_FORWARD, parametros, OperacionForward.class);
		
		Map<Long, OperacionForward> operacionesForwardPorFolioOperacion = operacionesForward
				.stream()
				.collect(Collectors.toMap(operacionForward->operacionForward.FolioOperacion, operacionForward->operacionForward));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionForwardWS.obtenerOperacionForwardPorFolioOperacion", fin - inicio);
		return operacionesForwardPorFolioOperacion;
	}
	
	private SalidaAnularOperacionForwardWS validar(Connection con, EntradaAnularOperacionForwardWS entrada, Map<Long, OperacionForward> operacionesForwardPorFolioOperacion) throws Exception {
		logger.trace("INICIO::AnularOperacionForwardWS.validar");
		long inicio = System.currentTimeMillis();
		SalidaAnularOperacionForwardWS out = new SalidaAnularOperacionForwardWS();
		
		List<MensajeSalida> validacionEntrada = validarEntrada(con, entrada);
		out.P_OUT_MENSAJES.addAll(validacionEntrada);
		
		List<MensajeSalida> validacionExistenciaOperacionForward = validarExistenciaOperacionForward(con, entrada, operacionesForwardPorFolioOperacion);
		out.P_OUT_MENSAJES.addAll(validacionExistenciaOperacionForward);
		
		List<MensajeSalida> validacionEstadoOperacionForward = validarEstadoOperacionForward(con, entrada, operacionesForwardPorFolioOperacion);
		out.P_OUT_MENSAJES.addAll(validacionEstadoOperacionForward);
				
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularOperacionForwardWS.validar", fin - inicio);
		return out;
	}
	
	private List<MensajeSalida> validarEntrada(Connection con, EntradaAnularOperacionForwardWS entrada) throws Exception {
		logger.trace("INICIO::AnularOperacionForwardWS.validarEntrada");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		if(entrada.p_Operaciones.isEmpty()) {
			MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ANULAROPERACIONFORWARDWS_NO_SE_AGREGARON_OPERACIONES", new HashMap<String,String>(1), con, entrada.p_IdEmpresa);
			mensajes.add(mensajeSalida);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularOperacionForwardWS.validarEntrada", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarExistenciaOperacionForward(Connection con, EntradaAnularOperacionForwardWS entrada, Map<Long, OperacionForward> operacionesForwardPorFolioOperacion) throws Exception {
		logger.trace("INICIO::AnularOperacionForwardWS.validarExistenciaOperacionForward");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntradaOperacionForward entradaOperacionForward : entrada.p_Operaciones) {
			OperacionForward operacionForward = operacionesForwardPorFolioOperacion.get(entradaOperacionForward.FolioOperacion);
			if(operacionForward == null) {
				HashMap<String,String> parametros = new HashMap<String,String>(2);
				parametros.put("FolioOperacion", String.valueOf(entradaOperacionForward.FolioOperacion));
				MensajeSalida mensajeSalida = new MensajeSalida(entradaOperacionForward.FolioOperacion, MensajeSalida.MENSAJE_ERROR, "ANULAROPERACIONFORWARDWS_NO_EXISTE_OPERACION", parametros, con, entrada.p_IdEmpresa);
				mensajes.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularOperacionForwardWS.validarExistenciaOperacionForward", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarEstadoOperacionForward(Connection con, EntradaAnularOperacionForwardWS entrada, Map<Long, OperacionForward> operacionesForwardPorFolioOperacion) throws Exception {
		logger.trace("INICIO::AnularOperacionForwardWS.validarEstadoOperacionForward");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntradaOperacionForward entradaOperacionForward : entrada.p_Operaciones) {
			OperacionForward operacionForward = operacionesForwardPorFolioOperacion.get(entradaOperacionForward.FolioOperacion);
			if(operacionForward != null && esIdEstadoInvalido(operacionForward.IdEstado)) {
				HashMap<String,String> parametros = new HashMap<String,String>(2);
				parametros.put("FolioOperacion", String.valueOf(entradaOperacionForward.FolioOperacion));
				MensajeSalida mensajeSalida = new MensajeSalida(entradaOperacionForward.FolioOperacion, MensajeSalida.MENSAJE_ERROR, "ANULAROPERACIONFORWARDWS_ESTADO_INCORRECTO", parametros, con, entrada.p_IdEmpresa);
				mensajes.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularOperacionForwardWS.validarEstadoOperacionForward", fin - inicio);
		return mensajes;
	}
	
	private Boolean esIdEstadoInvalido(String idEstado) {
		logger.trace("INICIO::AnularOperacionForwardWS.esIdEstadoInvalido");
		long inicio = System.currentTimeMillis();
		Boolean esEstadoInvalido = !"VIG".equals(idEstado) && !"PEN".equals(idEstado);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularOperacionForwardWS.esIdEstadoInvalido", fin - inicio);
		return esEstadoInvalido;
	}
	
	private SalidaAnularOperacionForwardWS anularOperacionForward(Connection con, EntradaAnularOperacionForwardWS entrada, Map<Long, OperacionForward> operacionesForwardPorFolioOperacion) throws Exception {
		logger.debug("INICIO::AnularOperacionForwardWS.validarExistenciaOperacionesForward");
		long inicio = System.currentTimeMillis();
		SalidaAnularOperacionForwardWS out = new SalidaAnularOperacionForwardWS();
		
		AnularOperacionForwardEntrada anularOperacionForwardEntrada = new AnularOperacionForwardEntrada();
		anularOperacionForwardEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		anularOperacionForwardEntrada.p_IdPais = obtenerIdPais(con, entrada.p_IdEmpresa);;
		anularOperacionForwardEntrada.p_IdUsuario = entrada.p_IdUsuario;
		anularOperacionForwardEntrada.p_IdModulo = entrada.p_IdModulo;
		anularOperacionForwardEntrada.p_IdBolsa = obtenerIdBolsa(con, entrada.p_IdEmpresa);
		anularOperacionForwardEntrada.p_IdSucursal = 0L;
		anularOperacionForwardEntrada.p_UsaReglasNegocio = "S";
		anularOperacionForwardEntrada.p_tablaAnulacion = entrada.p_Operaciones
				.stream()
				.map(entradaOperacionForward->{
					OperacionForward operacionForward = operacionesForwardPorFolioOperacion.get(entradaOperacionForward.FolioOperacion);
					ModificarOperacionForward.OperacionForwardObjIn operacionAnulacion = new ModificarOperacionForward.OperacionForwardObjIn();
					operacionAnulacion.FolioCartera = operacionForward.FolioOperacion;
					operacionAnulacion.IdOperacion = operacionForward.IdOperacion;
					return operacionAnulacion;
				})
				.collect(Collectors.toList());
		
		AnularOperacionForward anularOperacionForward = new AnularOperacionForward();
		AnularOperacionForwardSalida anularOperacionForwardSalida = anularOperacionForward.logica(con, anularOperacionForwardEntrada);
		out.P_OUT_ERRORESCONTADOR =  MensajeSalida.getNumeroErrores(anularOperacionForwardSalida.P_OUT_MENSAJES);
		out.P_OUT_MENSAJES = anularOperacionForwardSalida.P_OUT_MENSAJES;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularOperacionForwardWS.validarExistenciaOperacionesForward", fin - inicio);
		return out;
	}
	
	private String obtenerIdBolsa(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::AnularOperacionForwardWS.obtenerIdBolsa");
		long inicio = System.currentTimeMillis();
		String idBolsa = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularOperacionForwardWS.obtenerIdBolsa", fin - inicio);
		return idBolsa;
	}
	
	private Long obtenerIdPais(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::AnularOperacionForwardWS.obtenerIdPais");
		long inicio = System.currentTimeMillis();
		Long idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularOperacionForwardWS.obtenerIdPais", fin - inicio);
		return idPais;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"N\",\"p_Operaciones\":[{\"FolioOperacion\":20696400},{\"FolioOperacion\":20696399}]}";

		EntradaAnularOperacionForwardWS Objin = gson.fromJson(json, EntradaAnularOperacionForwardWS.class);
		AnularOperacionForwardWS demo = new AnularOperacionForwardWS();				
		try {
			SalidaAnularOperacionForwardWS ObjOut = demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.rollback();
			}else {
				con.rollback();
			}
			//con.commit();
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
			//con.commit();
    	}
		System.out.println("== END ==");
	}
}
