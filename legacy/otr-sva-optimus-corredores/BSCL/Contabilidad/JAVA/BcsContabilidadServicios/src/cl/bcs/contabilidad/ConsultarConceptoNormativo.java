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

public class ConsultarConceptoNormativo implements JavaCall{

	private final String SQL_SELECT_CONCEPTOS_NORMATIVOS = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarConceptoNormativo_SelectConceptosNormativos.sql");
	
	public static class ConceptoNormativo {
		public Long 	IdConceptoNormativo;
		public String 	ConceptoNormativo;
		public String 	IdEstado;
	}
	
	public static class ConsultaConceptosNormativos_OUT {
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		List<ConceptoNormativo> conceptosNormativos;
	}
	
	public static class ConsultaConceptosNormativos_IN {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		ConsultaConceptosNormativos_IN in = gson.fromJson(jin.toString() ,ConsultaConceptosNormativos_IN.class);
		ConsultaConceptosNormativos_OUT out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public ConsultaConceptosNormativos_OUT logica(Connection conn, ConsultaConceptosNormativos_IN in) {
		ConsultaConceptosNormativos_OUT out = new ConsultaConceptosNormativos_OUT();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.conceptosNormativos = new ArrayList<>();
		SQL sql = new SQL();
		
		try {
			out.conceptosNormativos = sql.query(conn, SQL_SELECT_CONCEPTOS_NORMATIVOS, new HashMap<>(0), ConceptoNormativo.class);
		} catch(Exception ex) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = ex.getMessage();
		}
		
		return out;
	}

	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")){
  			ConsultaConceptosNormativos_IN entrada= new ConsultaConceptosNormativos_IN();
			entrada.p_IdEmpresa		= 1L;
			entrada.p_IdUsuario		= 21L;
			entrada.p_IdModulo 		= "CON";
			JsonParser parser 		= new JsonParser();
			JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			try {
				ConsultarConceptoNormativo demo= new ConsultarConceptoNormativo();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
	}
}
