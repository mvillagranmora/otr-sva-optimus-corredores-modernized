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


public class CambiarPrioridadGlosaTipoInstrumento implements JavaCall{
	
	private final Log log = new Log("PLA.CambiarPrioridadGlosaTipoInstrumento");
	
	private final String SQL_SELECT_USO_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/pla/sql/CambiarPrioridadGlosaTipoInstrumento_SelectUsoGlosaTipoInstrumento.sql");
	private final String SQL_UPDATE_USO_GLOSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/pla/sql/CambiarPrioridadGlosaTipoInstrumento_UpdateUsoGlosaTipoInstrumento.sql");
	
	
	public static class EntradaCambiarPrioridadGlosaTipoInstrumento{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdGlosa;
		public String p_IdTipoInstrumento;
		public Long p_PrioridadNueva;
		public Long p_PrioridadAnterior;
		
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
	
	public static class SalidaCambiarPrioridadGlosaTipoInstrumento{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCambiarPrioridadGlosaTipoInstrumento(){
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
		EntradaCambiarPrioridadGlosaTipoInstrumento in = gson.fromJson(jin.toString(), EntradaCambiarPrioridadGlosaTipoInstrumento.class);
		SalidaCambiarPrioridadGlosaTipoInstrumento out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public SalidaCambiarPrioridadGlosaTipoInstrumento logica(Connection con, EntradaCambiarPrioridadGlosaTipoInstrumento entrada) throws Exception {
		log.write("INICIO::CambiarPrioridadGlosaTipoInstrumento");
		
		SalidaCambiarPrioridadGlosaTipoInstrumento out = new SalidaCambiarPrioridadGlosaTipoInstrumento();
		
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTipoInstrumento", entrada.p_IdTipoInstrumento);
		
		List<GlosaTipoInstrumento> glosas = sql.query(con, SQL_SELECT_USO_GLOSA_TIPO_INSTRUMENTO, params, GlosaTipoInstrumento.class);
		
		GlosaTipoInstrumento glosaNuevaPrioridad = null;
		GlosaTipoInstrumento glosaAntiguaPrioridad = null;
		List<GlosaTipoInstrumento> glosaActualizar = new ArrayList<GlosaTipoInstrumento>();
		for(GlosaTipoInstrumento glosa : glosas) {
			if(glosa.IdGlosa.equals(entrada.p_IdGlosa) && glosa.IdTipoInstrumento.equals(entrada.p_IdTipoInstrumento)) {
				glosaNuevaPrioridad = glosa;
			}else if(glosa.Prioridad.equals(entrada.p_PrioridadNueva)) {
				glosaAntiguaPrioridad = glosa;	
			}
		}
		
		if(glosaNuevaPrioridad == null || glosaAntiguaPrioridad == null) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_PRIORIDAD_GLOSA_TIPO_INSTRUMENTO_INVALIDA", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}else {
			glosaNuevaPrioridad.Prioridad = entrada.p_PrioridadNueva;
			glosaAntiguaPrioridad.Prioridad = entrada.p_PrioridadAnterior;
			glosaActualizar.add(glosaNuevaPrioridad);
			glosaActualizar.add(glosaAntiguaPrioridad);
			sql.batch(con, SQL_UPDATE_USO_GLOSA_TIPO_INSTRUMENTO, glosaActualizar, GlosaTipoInstrumento.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_CAMBIO_PRIORIDAD_GLOSA_TIPO_INSTRUMENTO_EXITOSO", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}
		
		log.write("FIN::CambiarPrioridadGlosaTipoInstrumento");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_IdUso\":\"CARTERA\",\"p_IdParametroFinanciero\":\"USD_COM_MIL\",\"p_PrioridadNueva\":2,\"p_PrioridadAnterior\":3}";
				
				EntradaCambiarPrioridadGlosaTipoInstrumento Objin = gson.fromJson(json, EntradaCambiarPrioridadGlosaTipoInstrumento.class);
				
				CambiarPrioridadGlosaTipoInstrumento  kk = new CambiarPrioridadGlosaTipoInstrumento();
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
