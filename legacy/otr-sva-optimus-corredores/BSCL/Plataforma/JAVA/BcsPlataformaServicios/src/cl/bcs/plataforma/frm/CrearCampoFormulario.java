package cl.bcs.plataforma.frm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.pojo.CampoFormulario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCampoFormulario implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearCampoFormulario.class);
	
	private String SQL_SELECT_COUNT_CAMPO_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/ComunesCampoFormulario_SelectCountCampoFormulario.sql");
	private String SQL_INSERT_CAMPO_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/CrearCampoFormulario_InsertCampoFormulario.sql");
	
	public static class EntradaCrearCampoFormulario extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_CodigoFormulario;
		public String p_CodigoCampo;
		public String p_IdTipoCanal;
		public Long p_CantidadMinima;
		public String p_Requerido;
	}
	
	public static class SalidaCrearCampoFormulario extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearCampoFormulario() {
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
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearCampoFormulario jin = gson.fromJson(in.toString(), EntradaCrearCampoFormulario.class);
		SalidaCrearCampoFormulario out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearCampoFormulario logica(Connection con, EntradaCrearCampoFormulario entrada) throws Exception {
		logger.info("INICIO::CrearCampoFormulario");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearCampoFormulario out = new SalidaCrearCampoFormulario();
		
		Validacion validacionExistenciaCampoFormulario = validarExistenciaCampoFormulario(con, entrada);
		out = agregarValidacionSalida(out, validacionExistenciaCampoFormulario);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		insertarCampoFormulario(con, entrada);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearCampoFormulario", finServicio - inicioServicio);
		return out;
	}
	
	private Validacion validarExistenciaCampoFormulario(Connection con, EntradaCrearCampoFormulario entrada) throws Exception {
		logger.debug("INICIO::CrearCampoFormulario.validarExistenciaCampoFormulario");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("CodigoFormulario", entrada.p_CodigoFormulario);
		parametros.put("CodigoCampo", entrada.p_CodigoCampo);
		parametros.put("IdTipoCanal", entrada.p_IdTipoCanal);
		
		SQL sql = new SQL();
		Long count = sql.countQuery(con, SQL_SELECT_COUNT_CAMPO_FORMULARIO, parametros);
		
		Validacion validacion = new Validacion();
		if(count > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CAMPO_FORMULARIO_YA_EXISTE", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCampoFormulario.validarExistenciaCampoFormulario", fin - inicio);
		return validacion;
	}
	
	private SalidaCrearCampoFormulario agregarValidacionSalida(SalidaCrearCampoFormulario salida, Validacion validacion) {
		logger.trace("INICIO::CrearCampoFormulario.agregarValidacionSalida");
		long inicio = System.currentTimeMillis();
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCampoFormulario.agregarValidacionSalida", fin - inicio);
		return salida;
	}
	
	private void insertarCampoFormulario(Connection con, EntradaCrearCampoFormulario entrada) throws Exception {
		logger.debug("INICIO::CrearCampoFormulario.insertarCampoFormulario");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		CampoFormulario campoFormulario = new CampoFormulario();
		campoFormulario.CodigoFormulario = entrada.p_CodigoFormulario;
		campoFormulario.CodigoCampo = entrada.p_CodigoCampo;
		campoFormulario.IdTipoCanal = entrada.p_IdTipoCanal;
		campoFormulario.CantidadMinima = entrada.p_CantidadMinima;
		campoFormulario.Requerido = entrada.p_Requerido;
		
		List<CampoFormulario> camposFormulario = Arrays.asList(campoFormulario);
		sql.batch(con, SQL_INSERT_CAMPO_FORMULARIO, camposFormulario, CampoFormulario.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCampoFormulario.insertarCampoFormulario", fin - inicio);
	}
}
