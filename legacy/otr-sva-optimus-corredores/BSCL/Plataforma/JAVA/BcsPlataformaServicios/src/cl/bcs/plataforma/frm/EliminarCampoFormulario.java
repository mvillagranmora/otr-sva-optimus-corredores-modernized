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

public class EliminarCampoFormulario implements JavaCall {
	

	static final Logger logger = LogManager.getLogger(EliminarCampoFormulario.class);
	
	private String SQL_SELECT_COUNT_CAMPO_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/ComunesCampoFormulario_SelectCountCampoFormulario.sql");
	private String SQL_DELETE_CAMPO_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/EliminarCampoFormulario_DeleteCampoFormulario.sql");
	
	public static class EntradaEliminarCampoFormulario extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_CodigoFormulario;
		public String p_CodigoCampo;
		public String p_IdTipoCanal;
	}
	
	public static class SalidaEliminarCampoFormulario extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaEliminarCampoFormulario() {
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
		EntradaEliminarCampoFormulario jin = gson.fromJson(in.toString(), EntradaEliminarCampoFormulario.class);
		SalidaEliminarCampoFormulario out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEliminarCampoFormulario logica(Connection con, EntradaEliminarCampoFormulario entrada) throws Exception {
		logger.info("INICIO::EliminarCampoFormulario");
		long inicioServicio = System.currentTimeMillis();
		SalidaEliminarCampoFormulario out = new SalidaEliminarCampoFormulario();
		
		Validacion validacionExistenciaCampoFormulario = validarExistenciaCampoFormulario(con, entrada);
		out = agregarValidacionSalida(out, validacionExistenciaCampoFormulario);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarCampoFormulario(con, entrada);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarCampoFormulario", finServicio - inicioServicio);
		return out;
	}
	
	private Validacion validarExistenciaCampoFormulario(Connection con, EntradaEliminarCampoFormulario entrada) throws Exception {
		logger.debug("INICIO::EliminarCampoFormulario.validarExistenciaCampoFormulario");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("CodigoFormulario", entrada.p_CodigoFormulario);
		parametros.put("CodigoCampo", entrada.p_CodigoCampo);
		parametros.put("IdTipoCanal", entrada.p_IdTipoCanal);
		
		SQL sql = new SQL();
		Long count = sql.countQuery(con, SQL_SELECT_COUNT_CAMPO_FORMULARIO, parametros);
		
		Validacion validacion = new Validacion();
		if(count == 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CAMPO_FORMULARIO_NO_EXISTE", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarCampoFormulario.validarExistenciaCampoFormulario", fin - inicio);
		return validacion;
	}
	
	private SalidaEliminarCampoFormulario agregarValidacionSalida(SalidaEliminarCampoFormulario salida, Validacion validacion) {
		logger.trace("INICIO::EliminarCampoFormulario.agregarValidacionSalida");
		long inicio = System.currentTimeMillis();
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarCampoFormulario.agregarValidacionSalida", fin - inicio);
		return salida;
	}
	
	private void eliminarCampoFormulario(Connection con, EntradaEliminarCampoFormulario entrada) throws Exception {
		logger.debug("INICIO::EliminarCampoFormulario.eliminarCampoFormulario");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		CampoFormulario campoFormulario = new CampoFormulario();
		campoFormulario.CodigoFormulario = entrada.p_CodigoFormulario;
		campoFormulario.CodigoCampo = entrada.p_CodigoCampo;
		campoFormulario.IdTipoCanal = entrada.p_IdTipoCanal;
		
		List<CampoFormulario> camposFormulario = Arrays.asList(campoFormulario);
		sql.batch(con, SQL_DELETE_CAMPO_FORMULARIO, camposFormulario, CampoFormulario.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarCampoFormulario.eliminarCampoFormulario", fin - inicio);
	}
}
