package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarConfiguracionesConceptoNormativo implements JavaCall{

	private final String SQL_SELECT_CONF_CONCEPTOS_NORMATIVOS = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarConfiguracionesConceptoNormativo_SelectConceptosConfiguraciones.sql");
	
	public static class ConfiguracionConceptoNormativo {
		public Long     IdCondicionConceptoNormativo;
		public Long 	IdConceptoNormativo;
		public String 	IdTipoDatoEvento;
		public String 	ValorVarchar;
		public String 	OperadorFuncion;
		public String   IdEstado;
		public String   Descripcion;
	}
	
	public static class ConsultaConfiguracionConceptosNormativos_OUT {
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		List<ConfiguracionConceptoNormativo> configuracionConcepto;
	}
	
	public static class ConsultaConfiguracionConceptosNormativos_IN {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public Long     p_IdConceptoNormativo;
		public String 	p_IdModulo;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		ConsultaConfiguracionConceptosNormativos_IN in = gson.fromJson(jin.toString() ,ConsultaConfiguracionConceptosNormativos_IN.class);
		ConsultaConfiguracionConceptosNormativos_OUT out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public ConsultaConfiguracionConceptosNormativos_OUT logica(Connection conn, ConsultaConfiguracionConceptosNormativos_IN in) {
		ConsultaConfiguracionConceptosNormativos_OUT out = new ConsultaConfiguracionConceptosNormativos_OUT();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.configuracionConcepto = new ArrayList<>();
		SQL sql = new SQL();
		
		try {
			HashMap<String, Object> params = new HashMap<>(1);
			params.put("IdConceptoNormativo", in.p_IdConceptoNormativo);
			out.configuracionConcepto = sql.query(conn, SQL_SELECT_CONF_CONCEPTOS_NORMATIVOS, params, ConfiguracionConceptoNormativo.class);
		} catch(Exception ex) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = ex.getMessage();
		}
		
		return out;
	}

	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")){
  			ConsultaConfiguracionConceptosNormativos_IN entrada= new ConsultaConfiguracionConceptosNormativos_IN();
			entrada.p_IdEmpresa					= 1L;
			entrada.p_IdUsuario					= 21L;
			entrada.p_IdConceptoNormativo		= 2L;
			entrada.p_IdModulo 					= "CON";
			JsonParser parser 		= new JsonParser();
			JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			try {
				ConsultarConfiguracionesConceptoNormativo demo= new ConsultarConfiguracionesConceptoNormativo();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
	}
}
