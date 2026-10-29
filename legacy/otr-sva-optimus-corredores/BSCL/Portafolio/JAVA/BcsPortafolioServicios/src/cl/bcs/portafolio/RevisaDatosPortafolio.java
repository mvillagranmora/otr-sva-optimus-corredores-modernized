package cl.bcs.portafolio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RevisaDatosPortafolio implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(RevisaDatosPortafolio.class);
	
	private final String SQL_SELECT_PORTAFOLIO_NO_APV = loadResourceToString("cl/bcs/portafolio/sql/RevisaDatosPortafolio_SelectPortafolioNoAPV.sql");
	private final String SQL_SELECT_PERFIL_COMERCIAL_BASE = loadResourceToString("cl/bcs/portafolio/sql/RevisaDatosPortafolio_SelectPerfilComercialBase.sql");
	private final String SQL_SELECT_PORTAFOLIO_APV = loadResourceToString("cl/bcs/portafolio/sql/RevisaDatosPortafolio_SelectPortafolioAPV.sql");
	private final String SQL_SELECT_PORTAFOLIO_APV_CON_CONTRATO_EN_OTRO_PLAN = loadResourceToString("cl/bcs/portafolio/sql/RevisaDatosPortafolio_SelectPortafolioAPVConContratoVigenteEnOtroPlan.sql");
	
	public static class EntradaRevisaDatosPortafolio extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_CodigoPortafolio;
		public String p_PropositoPortafolio;
		public String p_CodigoRegimen;
		public String p_NombrePlan;
		public String p_CodigoAgrupador;
		public Long p_IdCliente;
		public String p_IdPerfilComercialBase;
		
		public EntradaRevisaDatosPortafolio() {
			this.p_CodigoPortafolio = "";
			this.p_PropositoPortafolio = "PROPGENERALES";
			this.p_CodigoRegimen = "";
			this.p_NombrePlan = "";
			this.p_CodigoAgrupador = "";
			this.p_IdCliente = 0L;
			this.p_IdPerfilComercialBase = "";
		}
	}
	
	public static class SalidaRevisaDatosPortafolio extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaRevisaDatosPortafolio() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	public static class Portafolio extends BcsPojo{
		public Long IdPortafolio;
	}
	
	public static class PerfilComercialBase extends BcsPojo{
		public String IdPerfilComercialBase;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaRevisaDatosPortafolio jin = gson.fromJson(in.toString(), EntradaRevisaDatosPortafolio.class);
		SalidaRevisaDatosPortafolio out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaRevisaDatosPortafolio logica(Connection con, EntradaRevisaDatosPortafolio entrada) throws Exception {
		logger.info("INICIO::RevisaDatosPortafolio");
		long inicioServicio = System.currentTimeMillis();
		SalidaRevisaDatosPortafolio out = new SalidaRevisaDatosPortafolio();
		
		if(esPropositoNoAPV(entrada.p_CodigoPortafolio)) {
			out = validaDatosPortafolioNoAPV(con, entrada);
		}
		
		if(esPropositoAPV(entrada.p_CodigoPortafolio)) {
			out = validaDatosPortafolioAPV(con, entrada);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::RevisaDatosPortafolio", finServicio - inicioServicio);
		return out;
	}
	
	private Boolean esPropositoNoAPV(String propositoPortafolio) {
		logger.trace("INICIO::RevisaDatosPortafolio.esPropositoNoAPV");
		long inicio = System.currentTimeMillis();
		Boolean esPropositoNoAPV = !"PROPOSITOAPV".equals(propositoPortafolio);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RevisaDatosPortafolio.esPropositoNoAPV", fin - inicio);
		return esPropositoNoAPV;
	}
	
	private SalidaRevisaDatosPortafolio validaDatosPortafolioNoAPV(Connection con, EntradaRevisaDatosPortafolio entrada) throws Exception {
		logger.trace("INICIO::RevisaDatosPortafolio.validaDatosPortafolioNoAPV");
		long inicio = System.currentTimeMillis();
		SalidaRevisaDatosPortafolio out = new SalidaRevisaDatosPortafolio();
		
		Validacion validaPortafolio = validaPortafolioNoAPV(con, entrada.p_IdCliente, entrada.p_CodigoPortafolio, entrada.p_IdEmpresa);
		Validacion validaPerfilComercial = validaPerfilComercialBase(con, entrada.p_IdPerfilComercialBase, entrada.p_IdEmpresa);
		
		out = agregarValidacionSalida(out,validaPortafolio);
		out = agregarValidacionSalida(out,validaPerfilComercial);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RevisaDatosPortafolio.validaDatosPortafolioNoAPV", fin - inicio);
		return out;
	}
	
	private Validacion validaPortafolioNoAPV(Connection con, Long idCliente, String codigoPortafolio, Long idEmpresa) throws Exception {
		logger.debug("INICIO::RevisaDatosPortafolio.validaPortafolioNoAPV");
		long inicio = System.currentTimeMillis();
		
		if(codigoPortafolio.isEmpty()) {
			codigoPortafolio = obtieneCodigoPortafolioPorDefecto(con, idEmpresa);
		}
		
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdCliente", idCliente);
		parametros.put("CodigoPortafolio", codigoPortafolio);
		
		SQL sql = new SQL();
		List<Portafolio> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIO_NO_APV, parametros, Portafolio.class);
		
		Validacion validacion = new Validacion();
		if(portafolios.size() > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_EXISTE",new HashMap<String,String>(),con,idEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RevisaDatosPortafolio.validaPortafolioNoAPV", fin - inicio);
		return validacion;
	}
	
	private String obtieneCodigoPortafolioPorDefecto(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::RevisaDatosPortafolio.validaPortafolioNoAPV");
		long inicio = System.currentTimeMillis();
		
		String codigoPortafolioPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "SOCIODENEGOCIO", "POR.CodigoPortafolioDefault");
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RevisaDatosPortafolio.validaPortafolioNoAPV", fin - inicio);
		return codigoPortafolioPorDefecto;
	}
	
	private Validacion validaPerfilComercialBase(Connection con, String idPerfilComercialBase, Long idEmpresa)  throws Exception {
		logger.debug("INICIO::RevisaDatosPortafolio.validaPerfilComercialBase");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdPerfilComercialBase", idPerfilComercialBase);
		
		SQL sql = new SQL();
		List<PerfilComercialBase> perfilComercialBase = sql.query(con, SQL_SELECT_PERFIL_COMERCIAL_BASE, parametros, PerfilComercialBase.class);
		
		Validacion validacion = new Validacion();
		if(perfilComercialBase.isEmpty()) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_COMERCIAL_NO_EXISTE",new HashMap<String,String>(),con,idEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RevisaDatosPortafolio.validaPerfilComercialBase", fin - inicio);
		return validacion;
	}
	
	private SalidaRevisaDatosPortafolio agregarValidacionSalida(SalidaRevisaDatosPortafolio salida, Validacion validacion) {
		logger.trace("INICIO::RevisaDatosPortafolio.agregarValidacionSalida");
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		logger.trace("FIN::RevisaDatosPortafolio.agregarValidacionSalida");
		return salida;
	}
	
	private Boolean esPropositoAPV(String propositoPortafolio) {
		logger.trace("INICIO::RevisaDatosPortafolio.esPropositoAPV");
		long inicio = System.currentTimeMillis();
		Boolean esPropositoAPV = "PROPOSITOAPV".equals(propositoPortafolio);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RevisaDatosPortafolio.esPropositoAPV", fin - inicio);
		return esPropositoAPV;
	}
	
	private SalidaRevisaDatosPortafolio validaDatosPortafolioAPV(Connection con, EntradaRevisaDatosPortafolio entrada) throws Exception {
		logger.trace("INICIO::RevisaDatosPortafolio.validaDatosPortafolioAPV");
		long inicio = System.currentTimeMillis();
		SalidaRevisaDatosPortafolio out = new SalidaRevisaDatosPortafolio();
		
		Validacion validaPortafolio = validaPortafolioAPV(con, entrada);
		Validacion validaPortafolioConContratoEnOtroPlan = validaPortafolioAPVConContratoEnOtroPlan(con, entrada);
		Validacion validaPerfilComercial = validaPerfilComercialBase(con, entrada.p_IdPerfilComercialBase, entrada.p_IdEmpresa);
		
		out = agregarValidacionSalida(out,validaPortafolio);
		out = agregarValidacionSalida(out,validaPortafolioConContratoEnOtroPlan);
		out = agregarValidacionSalida(out,validaPerfilComercial);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RevisaDatosPortafolio.validaDatosPortafolioAPV", fin - inicio);
		return out;
	}
	
	private Validacion validaPortafolioAPV(Connection con, EntradaRevisaDatosPortafolio entrada) throws Exception {
		logger.debug("INICIO::RevisaDatosPortafolio.validaCodigoPortafolioNoAPV");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdCliente", entrada.p_IdCliente);
		parametros.put("Proposito", entrada.p_PropositoPortafolio);
		parametros.put("CodigoRegimen", entrada.p_CodigoRegimen);
		parametros.put("NombrePlan", entrada.p_NombrePlan);
		parametros.put("CodigoAgrupador", entrada.p_CodigoAgrupador);
		
		SQL sql = new SQL();
		List<Portafolio> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIO_APV, parametros, Portafolio.class);
		
		Validacion validacion = new Validacion();
		if(portafolios.size() > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RevisaDatosPortafolio.validaCodigoPortafolioNoAPV", fin - inicio);
		return validacion;
	}
	
	private Validacion validaPortafolioAPVConContratoEnOtroPlan(Connection con, EntradaRevisaDatosPortafolio entrada) throws Exception {
		logger.debug("INICIO::RevisaDatosPortafolio.validaPortafolioAPVConContratoEnOtroPlan");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdCliente", entrada.p_IdCliente);
		parametros.put("Proposito", entrada.p_PropositoPortafolio);
		parametros.put("NombrePlan", entrada.p_NombrePlan);
		
		SQL sql = new SQL();
		List<Portafolio> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIO_APV_CON_CONTRATO_EN_OTRO_PLAN, parametros, Portafolio.class);
		
		Validacion validacion = new Validacion();
		if(portafolios.size() > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"REGLA_CLIENTE_PLAN_AHORRO",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RevisaDatosPortafolio.validaPortafolioAPVConContratoEnOtroPlan", fin - inicio);
		return validacion;
	}
}
