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

public class ConsultarCondicionesConceptosNormativos implements JavaCall {

private final String SQL_SELECT_CONDICIONES = loadResourceToString("cl/bcs/contabilidad/sql/ConsultarCondicionesConceptosNormativos_SelectConceptosConfiguraciones.sql");

	public static class Valores {
		public String ValorVarchar;
		public String OperadorFuncion;
	}
	
	public static class CondicionesConceptoNormativo {
		public Long     IdCondicionConceptoNormativo;
		public Long 	IdConceptoNormativo;
		public String 	IdTipoDatoEvento;
		public List<Valores> valores;
	}
	
	public static class CondicionesConceptosNormativosSQL {
		public Long     IdCondicionConceptoNormativo;
		public Long 	IdConceptoNormativo;
		public String 	IdTipoDatoEvento;
		public String 	ValorVarchar;
		public String 	OperadorFuncion;
		public String   ConceptoNormativo;
	}
	
	public static class ConceptoNormativo {
		public Long     IdConceptoNormativo;
		public String 	ConceptoNormativo;
		public List<CondicionesConceptoNormativo> 	condiciones;
	}
	
	public static class ConfiguracionesConceptosNormativos_OUT {
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		List<ConceptoNormativo> conceptosNormativos;
	}
	
	public static class ConfiguracionesConceptosNormativos_IN {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		ConfiguracionesConceptosNormativos_IN in = gson.fromJson(jin.toString() ,ConfiguracionesConceptosNormativos_IN.class);
		ConfiguracionesConceptosNormativos_OUT out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public ConfiguracionesConceptosNormativos_OUT logica(Connection conn, ConfiguracionesConceptosNormativos_IN in) {
	    ConfiguracionesConceptosNormativos_OUT out = new ConfiguracionesConceptosNormativos_OUT();
	    out.P_OUT_ERRORESCONTADOR = 0L;
	    out.conceptosNormativos = new ArrayList<>();
	    SQL sql = new SQL();

	    try {
	        List<CondicionesConceptosNormativosSQL> condiciones = sql.query(conn, SQL_SELECT_CONDICIONES, new HashMap<>(0), CondicionesConceptosNormativosSQL.class);
	        HashMap<Long, ConceptoNormativo> mapaConceptos = new HashMap<>();

	        for (CondicionesConceptosNormativosSQL fila : condiciones) {
	            ConceptoNormativo concepto = mapaConceptos.get(fila.IdConceptoNormativo);
	            if (concepto == null) {
	                concepto = new ConceptoNormativo();
	                concepto.IdConceptoNormativo = fila.IdConceptoNormativo;
	                concepto.ConceptoNormativo = fila.ConceptoNormativo;
	                concepto.condiciones = new ArrayList<>();
	                mapaConceptos.put(fila.IdConceptoNormativo, concepto);
	            }

	            if (fila.IdCondicionConceptoNormativo == null) {
	                continue;
	            }

	            CondicionesConceptoNormativo grupo = concepto.condiciones.stream()
	                    .filter(c -> c.IdTipoDatoEvento.equalsIgnoreCase(fila.IdTipoDatoEvento))
	                    .findFirst()
	                    .orElse(null);

	            if (grupo == null) {
	                grupo = new CondicionesConceptoNormativo();
	                grupo.IdConceptoNormativo = fila.IdConceptoNormativo;
	                grupo.IdTipoDatoEvento = fila.IdTipoDatoEvento;
	                grupo.valores = new ArrayList<>();
	                concepto.condiciones.add(grupo);
	            }

	            Valores val = new Valores();
	            val.ValorVarchar = fila.ValorVarchar;
	            val.OperadorFuncion = fila.OperadorFuncion;
	            grupo.valores.add(val);
	        }

	        out.conceptosNormativos.addAll(mapaConceptos.values());

	    } catch (Exception ex) {
	        out.P_OUT_ERRORESCONTADOR++;
	        out.P_OUT_MENSAJE = ex.getMessage();
	    }

	    return out;
	}


	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")){
  			ConfiguracionesConceptosNormativos_IN entrada= new ConfiguracionesConceptosNormativos_IN();
			entrada.p_IdEmpresa					= 1L;
			entrada.p_IdUsuario					= 21L;
			entrada.p_IdModulo 					= "CON";
			JsonParser parser 		= new JsonParser();
			JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			try {
				ConsultarCondicionesConceptosNormativos demo= new ConsultarCondicionesConceptosNormativos();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
	}

}
