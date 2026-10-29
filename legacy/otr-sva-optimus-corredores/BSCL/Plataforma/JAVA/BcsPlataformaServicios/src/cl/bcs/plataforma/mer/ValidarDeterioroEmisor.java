package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.mer.pojo.DeterioroEmisor;
import cl.bcs.plataforma.mer.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public final class ValidarDeterioroEmisor implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CrearDeterioroEmisor.class);

	private static final FileLoader LOADER = new FileLoader();
	
	private static final String SQL_SELECT_EMISOR_VALIDO = LOADER.loadResourceToString("cl/bcs/plataforma/mer/sql/ValidarDeterioroEmisor_SelectEmisoresValidos.sql");
	private static final String SQL_SELECT_DETERIORO_EMISOR = LOADER.loadResourceToString("cl/bcs/plataforma/mer/sql/ValidarDeterioroEmisor_SelectDeterioroEmisor.sql");

	public static class ValidarDeterioroEmisor_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
	    public List<DeterioroEmisor> p_DeteriorosEmisor;
	    public DeterioroEmisor deterioroEmisor;
	    public ValidarDeterioroEmisor_Entrada() {
	        this.p_DeteriorosEmisor = new ArrayList<DeterioroEmisor>();
	    }
	}

	public static class ValidarDeterioroEmisor_Salida extends BcsPojo {
	    public List<Validacion> validaciones;
	
	    public ValidarDeterioroEmisor_Salida() {
	        this.validaciones = new ArrayList<>();
	    }
	}
	
	public static class EmisorValido {
		public String IdEmisor;
		public String Emisor;
		
		public EmisorValido() {
			this.IdEmisor = "";
			this.Emisor = "";
		}
		
		public String getIdEmisor() {
			return IdEmisor;
		}
		
		public String getEmisor() {
			return Emisor;
		}
		
		public void setIdEmisor(String IdEmisor) {
			this.IdEmisor = IdEmisor;
		}
		
		public void setEmisor(String Emisor) {
			this.Emisor = Emisor;
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static List<Validacion> validarRegistroDeterioroEmisor(Connection con, List<DeterioroEmisor> deterioros, Long idEmpresa) throws Exception{
		
		logger.info("INICIO::ValidarDeterioroEmisor.validarRegistroDeterioroEmisor");
		Long inicioMetodo = System.currentTimeMillis();
		
		List<Validacion> validaciones  = new ArrayList<Validacion>();
		
		if(deterioros.size() == 0) {
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			String msg = Diccionario.getMensaje(con, idEmpresa, "UI_DETERIORO_EMISOR_SIN_REGISTROS").msg();
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_DETERIORO_EMISOR_SIN_REGISTROS", msg);
			validaciones.add(validacion);
			return validaciones;
		}
		
		Map<String, DeterioroEmisor> mapEmisoresExistentes = getDeterioroEmisoresExistentes(con);
		Map<String,EmisorValido> mapEmisoresValidos = getEmisoresValidos(con);
		
		for(DeterioroEmisor deterioro : deterioros) {
			if(!esFlagDeterioroValido(deterioro.FlagDeterioro)) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				String msg = Diccionario.getMensaje(con, idEmpresa, "UI_VALIDACION_FLAG_DETERIORO").msg();
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_VALIDACION_FLAG_DETERIORO", msg);
				validaciones.add(validacion);
			}
			
			if(!esPorcentajeDeterioroValido(deterioro.PorcentajeDeterioro)) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				String msg = Diccionario.getMensaje(con, idEmpresa, "UI_VALIDACION_PORCENTAJE_DETERIORO").msg();
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_VALIDACION_PORCENTAJE_DETERIORO", msg);
				validaciones.add(validacion);
			}
			
			if(!esFaseDeterioroValido(deterioro.Fase)) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				String msg = Diccionario.getMensaje(con, idEmpresa, "UI_VALIDACION_FASE_DETERIORO").msg();
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_VALIDACION_FASE_DETERIORO", msg);
				validaciones.add(validacion);
			}

			if(mapEmisoresExistentes.containsKey(deterioro.Emisor) && "CREAR".equals(deterioro.Accion)) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(1);
		    	parametrosMensaje.put("Emisor", deterioro.Emisor);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_DETERIORO_EMISOR_EXISTE",parametrosMensaje, con, idEmpresa);
				validaciones.add(validacion);
			}
			
			if(!mapEmisoresExistentes.containsKey(deterioro.Emisor) && "MODIFICAR".equals(deterioro.Accion)) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(1);
		    	parametrosMensaje.put("Emisor", deterioro.Emisor);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_DETERIORO_EMISOR_NO_EXISTE",parametrosMensaje, con, idEmpresa);
				validaciones.add(validacion);
			}
			
			
			if(!mapEmisoresValidos.containsKey(deterioro.Emisor) || deterioro.Emisor == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(1);
		    	parametrosMensaje.put("Emisor", deterioro.Emisor);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CARGA_DETERIORO_EMISOR_NO_VALIDO",parametrosMensaje, con, idEmpresa);
				validaciones.add(validacion);
			}
				
			if(validaciones.size() > 0) break;
			
		}
		
		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::ValidarDeterioroEmisor.validarRegistroDeterioroEmisor :", finMetodo - inicioMetodo);
		
		return validaciones;
		
	}
		
	private static Map<String,DeterioroEmisor> getDeterioroEmisoresExistentes(Connection con) throws Exception {
		logger.debug("INICIO::ValidarDeterioroEmisor.getDeterioroEmisoresExistentes");
		Long inicioMetodo = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(1);
		SQL sql = new SQL();
		
		Map<String,DeterioroEmisor> emisores = sql.query(con, SQL_SELECT_DETERIORO_EMISOR, params, DeterioroEmisor.class).stream()
				.collect(Collectors.toMap(DeterioroEmisor::getEmisor, Function.identity(), (emisor1, emisor2) -> emisor1));;
		
		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::ValidarDeterioroEmisor.getDeterioroEmisoresExistentes :", finMetodo - inicioMetodo);
		return emisores;
	}
	
	private static Map<String,EmisorValido> getEmisoresValidos(Connection con) throws Exception {
		logger.debug("INICIO::ValidarDeterioroEmisor.getEmisoresValidos");
		Long inicioMetodo = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		SQL sql = new SQL();
		
		List<EmisorValido> emisores = sql.query(con, SQL_SELECT_EMISOR_VALIDO, params, EmisorValido.class);
		
		Map<String,EmisorValido> emisoresValidos = emisores.stream()
				.collect(Collectors.toMap(EmisorValido::getIdEmisor, Function.identity() ,(emisor1, emisor2) -> emisor1));

		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::ValidarDeterioroEmisor.getEmisoresValidos :", finMetodo - inicioMetodo);
		return emisoresValidos;
	}
	
	
	private static Boolean esFlagDeterioroValido(String flag) {
		
		if(flag == null) return false;
		if(!flag.matches("^S$|^N$")) return false;
		
		return true;
	}
	
	private static Boolean esPorcentajeDeterioroValido(BigDecimal porcentaje) {
		
		if(porcentaje == null) return false;
		if(porcentaje.compareTo(BigDecimal.ZERO) <= 0) return false;
		if(!porcentaje.toString().matches("^\\d{1,2}(?:\\.\\d{0,4})?$")) return false;
		
		return true;
	}
	
	private static Boolean esFaseDeterioroValido(Integer fase) {
		
		if(fase == null) return false;
		if(fase < 1 || fase > 3) return false;
		return true;
		
	}
	
	public static Boolean validarResultadoSQL(int [] resultado) throws Exception{
		
		if(resultado == null) return false;
		if(resultado.length < 1) return false;
		if(resultado[0] != 1) return false;
		
		return true;

	}

}
