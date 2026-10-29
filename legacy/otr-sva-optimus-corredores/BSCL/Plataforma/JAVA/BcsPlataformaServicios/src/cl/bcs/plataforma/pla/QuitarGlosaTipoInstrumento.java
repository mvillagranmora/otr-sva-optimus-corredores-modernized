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

public class QuitarGlosaTipoInstrumento  implements JavaCall {
	
	private final Log log = new Log("PLA.QuitarGlosaTipoInstrumento");
	
	private final String SQL_SELECT_USO_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/pla/sql/QuitarGlosaTipoInstrumento_SelectUsoGlosaTipoInstrumento.sql");
	private final String SQL_UPDATE_USO_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/pla/sql/QuitarGlosaTipoInstrumento_UpdateUsoGlosaTipoInstrumento.sql");
	private final String SQL_DELETE_USO_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/pla/sql/QuitarGlosaTipoInstrumento_DeleteUsoGlosaTipoInstrumento.sql");

	
	public static class EntradaQuitarGlosaTipoInstrumento{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InGlosaTipoInstrumento> p_InGlosasTipoInstrumento;
		
		public EntradaQuitarGlosaTipoInstrumento(){
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
	
	public static class SalidaQuitarGlosaTipoInstrumento{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaQuitarGlosaTipoInstrumento(){
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
		EntradaQuitarGlosaTipoInstrumento in = gson.fromJson(jin.toString(), EntradaQuitarGlosaTipoInstrumento.class);
		SalidaQuitarGlosaTipoInstrumento out = logica(con,in);
		JsonParser parser =new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaQuitarGlosaTipoInstrumento logica (Connection con, EntradaQuitarGlosaTipoInstrumento entrada) throws Exception {
		
		log.write("INICIO::QuitarGlosaTipoInstrumento");
		
		SalidaQuitarGlosaTipoInstrumento out = new SalidaQuitarGlosaTipoInstrumento();
		SQL sql = new SQL();
		
		List<String> glosas = new ArrayList<String>();
		for(InGlosaTipoInstrumento glosa : entrada.p_InGlosasTipoInstrumento){
			glosas.add(glosa.IdTipoInstrumento);
		}
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTipoInstrumento", glosas);
		
		List<GlosaTipoInstrumento> glosasExistentes = sql.query(con, SQL_SELECT_USO_GLOSA_TIPO_INSTRUMENTO, params, GlosaTipoInstrumento.class);
		HashMap<String,Object> hglosasExistentes = Utils.List2Hash(glosasExistentes, "IdUsoGlosaIdTipoInstrumento");
		
		//VALIDACIONES
		for(InGlosaTipoInstrumento inGlosa : entrada.p_InGlosasTipoInstrumento){
			GlosaTipoInstrumento glosaExistente = (GlosaTipoInstrumento) hglosasExistentes.get(inGlosa.IdGlosa + "_" + inGlosa.IdTipoInstrumento);
			if(glosaExistente == null){
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> datosMensaje = new HashMap<String,String>();
				datosMensaje.put("IdGlosa", inGlosa.IdGlosa);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_GLOSA_NO_EXISTE", datosMensaje,con, entrada.p_IdEmpresa));
			}
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)){
			List<GlosaTipoInstrumento> glosasActualizar = new ArrayList<GlosaTipoInstrumento>();
			List<GlosaTipoInstrumento> glosasEliminar = new ArrayList<GlosaTipoInstrumento>();
			for(String idGlosa : glosas){
				Long prioridad = 0L;
				for(GlosaTipoInstrumento glosa : glosasExistentes){
					if(glosa.IdTipoInstrumento.equals(idGlosa)){
						GlosaTipoInstrumento glosaEliminar = null;
						for(InGlosaTipoInstrumento inGlosa : entrada.p_InGlosasTipoInstrumento){
							if(inGlosa.IdGlosa.equals(glosa.IdGlosa) && inGlosa.IdTipoInstrumento.equals(glosa.IdTipoInstrumento)){
								glosaEliminar = glosa;
								break;
							}
						}
						if(glosaEliminar == null){
							prioridad++;
							glosa.Prioridad = prioridad;
							glosasActualizar.add(glosa);
						}else{
							glosasEliminar.add(glosaEliminar);
						}
					}
				}
			}
			sql.batch(con, SQL_DELETE_USO_GLOSA_TIPO_INSTRUMENTO, glosasEliminar, GlosaTipoInstrumento.class);
			sql.batch(con, SQL_UPDATE_USO_GLOSA_TIPO_INSTRUMENTO, glosasActualizar, GlosaTipoInstrumento.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_GLOSA_TIPO_INSTRUMENTO_QUITADO_EXITOSAMENTE", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}
		
		
		log.write("FIN::QuitarGlosaTipoInstrumento");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("Desa02","C:\\Users\\felipe.malermo.ext\\Documents\\GIT-v2\\optimus-corredores\\connections.properties" )) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_InGlosasTipoInstrumento\":[{\"IdTipoInstrumento\":\"ACN\",\"IdGlosa\":\"TEST3\"}]}";
				
				EntradaQuitarGlosaTipoInstrumento Objin = gson.fromJson(json, EntradaQuitarGlosaTipoInstrumento.class);
				
				QuitarGlosaTipoInstrumento  kk = new QuitarGlosaTipoInstrumento();
				System.out.println(kk.logica(con, Objin));
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
