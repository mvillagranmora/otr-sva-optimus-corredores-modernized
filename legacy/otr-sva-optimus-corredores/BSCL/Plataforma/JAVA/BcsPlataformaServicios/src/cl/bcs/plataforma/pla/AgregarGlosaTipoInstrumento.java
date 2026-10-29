package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarGlosaTipoInstrumento implements JavaCall{
	
	private final Log log = new Log("PLA.AgregarGlosaTipoInstrumento");
	
	private final String SQL_SELECT_USO_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarGlosaTipoInstrumento_SelectUsoGlosaTipoInstrumento.sql");
	private final String SQL_INSERT_USO_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarGlosaTipoInstrumento_InsertUsoGlosaTipoInstrumento.sql");
	
	public static class EntradaAgregarGlosaTipoInstrumento{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InGlosaTipoInstrumento> p_InGlosasTipoInstrumento;
		
		public EntradaAgregarGlosaTipoInstrumento(){
			this.p_InGlosasTipoInstrumento = new ArrayList<InGlosaTipoInstrumento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InGlosaTipoInstrumento{
		public String IdTipoInstrumento;
		public String IdGlosa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GlosaTipoInstrumento{
		public String IdUsoGlosaIdTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdGlosa;
		public Long Prioridad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAgregarGlosaTipoInstrumento{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAgregarGlosaTipoInstrumento(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaAgregarGlosaTipoInstrumento in = gson.fromJson(jin.toString(), EntradaAgregarGlosaTipoInstrumento.class);
		SalidaAgregarGlosaTipoInstrumento out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAgregarGlosaTipoInstrumento logica(Connection con, EntradaAgregarGlosaTipoInstrumento entrada) throws Exception {
		log.write("INICIO::AgregarGlosaTipoInstrumento");
		
		SalidaAgregarGlosaTipoInstrumento out = new SalidaAgregarGlosaTipoInstrumento();
		SQL sql = new SQL();
		
		List<String> glosas = new ArrayList<String>();
		for(InGlosaTipoInstrumento glosa : entrada.p_InGlosasTipoInstrumento){
			glosas.add(glosa.IdTipoInstrumento);
		}
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTipoInstrumento", glosas);
		
		List<GlosaTipoInstrumento> glosasExistentes = sql.query(con, SQL_SELECT_USO_GLOSA_TIPO_INSTRUMENTO, params, GlosaTipoInstrumento.class);
		HashMap<String,Object> hglosasExistentes = Utils.List2Hash(glosasExistentes, "IdUsoGlosaIdTipoInstrumento");
		Long ultimaPrioridad = glosasExistentes.size() > 0 ? glosasExistentes.get(glosasExistentes.size() -1).Prioridad : 0L;
		
		List<GlosaTipoInstrumento> insertGlosas = new ArrayList<GlosaTipoInstrumento>();
		List<String> parametrosEntrada = new ArrayList<String>();
		
		for(InGlosaTipoInstrumento glosa : entrada.p_InGlosasTipoInstrumento){
			
			GlosaTipoInstrumento glosasExistente = (GlosaTipoInstrumento) hglosasExistentes.get(glosa.IdGlosa + "_" + glosa.IdTipoInstrumento);
			
			if(parametrosEntrada.contains(glosa.IdTipoInstrumento)){
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> datosMensaje = new HashMap<String,String>();
				datosMensaje.put("IdTipoInstrumento", glosa.IdTipoInstrumento);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_GLOSA_ENTRADA_REPETIDO", datosMensaje,con, entrada.p_IdEmpresa));
			}else{
				parametrosEntrada.add(glosa.IdTipoInstrumento);
				if(glosasExistente == null){
					ultimaPrioridad++;
					GlosaTipoInstrumento nuevoGlosaTipoInstrumento = new GlosaTipoInstrumento();
					nuevoGlosaTipoInstrumento = Utils.copyPojo(glosa, nuevoGlosaTipoInstrumento);
					nuevoGlosaTipoInstrumento.Prioridad = ultimaPrioridad;
					insertGlosas.add(nuevoGlosaTipoInstrumento);
				}else{
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String,String> datosMensaje = new HashMap<String,String>();
					datosMensaje.put("IdGlosa", glosa.IdGlosa);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_GLOSA_YA_EXISTE", datosMensaje,con, entrada.p_IdEmpresa));
				}
			}			
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)){
			sql.batch(con, SQL_INSERT_USO_GLOSA_TIPO_INSTRUMENTO, insertGlosas, GlosaTipoInstrumento.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_GLOSA_AGREGADO_EXITOSAMENTE", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}

		log.write("FIN::AgregarGlosaTipoInstrumento");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("Desa02","C:\\Users\\felipe.malermo.ext\\Documents\\GIT-v2\\optimus-corredores\\connections.properties" )) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_InGlosasTipoInstrumento\":[{\"IdTipoInstrumento\":\"ACN\",\"IdGlosa\":\"TEST4\"}]}";
				
				EntradaAgregarGlosaTipoInstrumento Objin = gson.fromJson(json, EntradaAgregarGlosaTipoInstrumento.class);
				
				AgregarGlosaTipoInstrumento  kk = new AgregarGlosaTipoInstrumento();
				System.out.println(kk.logica(con, Objin));
				//con.rollback();
				con.commit();
				con.close();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END MAIN ==");
	}
}