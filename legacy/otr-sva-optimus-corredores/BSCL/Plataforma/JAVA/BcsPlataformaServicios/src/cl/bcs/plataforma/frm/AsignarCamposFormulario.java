package cl.bcs.plataforma.frm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AsignarCamposFormulario implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(AsignarCamposFormulario.class);
	private String SQL_CAMPOS_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/AsignarCamposFormulario_SelectCamposFormulario.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class CampoObligatorio extends BcsPojo {
		public String CodigoCampo;
		public String CodigoCampoBackend;
		public String TipoDato;
		public String ValorDefectoCampoFormulario;
		public String ValorDefecto;
	}
		
	public <T> List<T> asignarValoresDefecto(Connection con, FormularioCanal formularioCanal, List<T> objetos, Class<T> tipo) throws Exception {
		logger.debug("INICIO::AsignarCamposFormulario.asignarValoresDefecto");
		long inicio = System.currentTimeMillis();
		
		List<CampoObligatorio> camposValoresDefecto = obtenerCamposFormularioCanal(con, formularioCanal);
		
		for(T objeto : objetos) {
			for(CampoObligatorio campoValorDefecto : camposValoresDefecto) {
				Field campo = objeto.getClass().getField(campoValorDefecto.CodigoCampoBackend);

				if(CampoFormularioUtils.esCampoString(campoValorDefecto.TipoDato, campo)) {
					String valor = (String) campo.get(objeto);
					if(CampoFormularioUtils.noEsValorValido(valor, campoValorDefecto.ValorDefecto, campoValorDefecto.CodigoCampo)) {
						campo.set(objeto, campoValorDefecto.ValorDefectoCampoFormulario);
					}
					continue;
				}
				
				if(CampoFormularioUtils.esCampoLong(campoValorDefecto.TipoDato, campo)) {
					Long valor = (Long) campo.get(objeto);
					if(CampoFormularioUtils.noEsValorValido(valor, campoValorDefecto.ValorDefecto, campoValorDefecto.CodigoCampo)) {
						Long valorDefecto = Long.valueOf(campoValorDefecto.ValorDefectoCampoFormulario);
						campo.set(objeto, valorDefecto);
					}
					continue;
				}
				
				if(CampoFormularioUtils.esCampoDate(campoValorDefecto.TipoDato, campo)) {
					java.sql.Date valor = (java.sql.Date) campo.get(objeto);
					if(CampoFormularioUtils.noEsValorValido(valor, campoValorDefecto.ValorDefecto, campoValorDefecto.CodigoCampo)) {
						java.sql.Date valorDefecto = Fecha.toDate(campoValorDefecto.ValorDefectoCampoFormulario);
						campo.set(objeto, valorDefecto);
					}
					continue;
				}
				
				if(CampoFormularioUtils.esCampoTimestamp(campoValorDefecto.TipoDato, campo)) {
					java.sql.Timestamp valor = (java.sql.Timestamp) campo.get(objeto);
					if(CampoFormularioUtils.noEsValorValido(valor, campoValorDefecto.ValorDefecto, campoValorDefecto.CodigoCampo)) {
						java.sql.Timestamp valorDefecto = Fecha.toDateTime(campoValorDefecto.ValorDefectoCampoFormulario);
						campo.set(objeto, valorDefecto);
					}
					continue;
				}
				
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AsignarCamposFormulario.asignarValoresDefecto", fin - inicio);
		return objetos;
	}
	
	
	private List<CampoObligatorio> obtenerCamposFormularioCanal(Connection con, FormularioCanal formularioCanal) throws Exception {
		logger.debug("INICIO::AsignarCamposFormulario.obtenerCamposFormularioCanal");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("CodigoFormulario", formularioCanal.CodigoFormulario);
		parametros.put("IdTipoCanal", formularioCanal.IdTipoCanal);
		
		SQL sql = new SQL();
		List<CampoObligatorio> camposObligatorios = sql.query(con, SQL_CAMPOS_FORMULARIO, parametros, CampoObligatorio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AsignarCamposFormulario.obtenerCamposFormularioCanal", fin - inicio);
		return camposObligatorios;
	}
	
	

}
