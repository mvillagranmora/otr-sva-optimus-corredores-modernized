package cl.bcs.plataforma.mer;

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

public class QuitarUsoParidadParametroFinanciero implements JavaCall{
	
	private final Log log = new Log("PLA.QuitarUsoParidadParametroFinanciero");
	
	private final String SQL_SELECT_USO_PARIDAD_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/QuitarUsoParidadParametroFinanciero_SelectUsoParidadParametroFinanciero.sql");
	private final String SQL_UPDATE_USO_PARIDAD_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/QuitarUsoParidadParametroFinanciero_UpdateUsoParidadParametroFinanciero.sql");
	private final String SQL_DELETE_USO_PARIDAD_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/QuitarUsoParidadParametroFinanciero_DeleteUsoParidadParametroFinanciero.sql");
	
	public static class EntradaQuitarUsoParidadParametroFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InUsoParidadParametroFinanciero> p_UsosParidadParametroFinanciero;
		
		public EntradaQuitarUsoParidadParametroFinanciero(){
			this.p_UsosParidadParametroFinanciero = new ArrayList<InUsoParidadParametroFinanciero>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InUsoParidadParametroFinanciero{
		public String IdUso;
		public String IdParametroFinanciero;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UsoParidadParametroFinanciero{
		public String IdUsoIdParametroFinanciero;
		public String IdUso;
		public String IdParametroFinanciero;
		public Long Prioridad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaQuitarUsoParidadParametroFinanciero{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaQuitarUsoParidadParametroFinanciero(){
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
		EntradaQuitarUsoParidadParametroFinanciero in = gson.fromJson(jin.toString(), EntradaQuitarUsoParidadParametroFinanciero.class);
		SalidaQuitarUsoParidadParametroFinanciero out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaQuitarUsoParidadParametroFinanciero logica(Connection con, EntradaQuitarUsoParidadParametroFinanciero entrada) throws Exception {
		log.write("INICIO::QuitarUsoParidadParametroFinanciero");
		SalidaQuitarUsoParidadParametroFinanciero out = new SalidaQuitarUsoParidadParametroFinanciero();
		
		SQL sql = new SQL();
		
		List<String> idUsos = new ArrayList<String>();
		
		for(InUsoParidadParametroFinanciero uso : entrada.p_UsosParidadParametroFinanciero){
			idUsos.add(uso.IdUso);
		}
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdUso", idUsos);
		
		List<UsoParidadParametroFinanciero> usosExistentes = sql.query(con, SQL_SELECT_USO_PARIDAD_PARAMETRO_FINANCIERO, params, UsoParidadParametroFinanciero.class);
		HashMap<String,Object> hUsosExistentes = Utils.List2Hash(usosExistentes, "IdUsoIdParametroFinanciero");
		
		//VALIDACIONES
		for(InUsoParidadParametroFinanciero inUso : entrada.p_UsosParidadParametroFinanciero){
			UsoParidadParametroFinanciero usoExistente = (UsoParidadParametroFinanciero) hUsosExistentes.get(inUso.IdUso + "_" + inUso.IdParametroFinanciero);
			if(usoExistente == null){
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> datosMensaje = new HashMap<String,String>();
				datosMensaje.put("IdParametroFinanciero", inUso.IdParametroFinanciero);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_PARAMETRO_FINANCIERO_NO_EXISTE", datosMensaje,con, entrada.p_IdEmpresa));
			}
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)){
			List<UsoParidadParametroFinanciero> usosActualizar = new ArrayList<UsoParidadParametroFinanciero>();
			List<UsoParidadParametroFinanciero> usosEliminar = new ArrayList<UsoParidadParametroFinanciero>();
			for(String idUso : idUsos){
				Long prioridad = 0L;
				for(UsoParidadParametroFinanciero uso : usosExistentes){
					if(uso.IdUso.equals(idUso)){
						UsoParidadParametroFinanciero usoEliminar = null;
						for(InUsoParidadParametroFinanciero inUso : entrada.p_UsosParidadParametroFinanciero){
							if(inUso.IdUso.equals(uso.IdUso) && inUso.IdParametroFinanciero.equals(uso.IdParametroFinanciero)){
								usoEliminar = uso;
								break;
							}
						}
						if(usoEliminar == null){
							prioridad++;
							uso.Prioridad = prioridad;
							usosActualizar.add(uso);
						}else{
							usosEliminar.add(usoEliminar);
						}
					}
				}
			}
			sql.batch(con, SQL_DELETE_USO_PARIDAD_PARAMETRO_FINANCIERO, usosEliminar, UsoParidadParametroFinanciero.class);
			sql.batch(con, SQL_UPDATE_USO_PARIDAD_PARAMETRO_FINANCIERO, usosActualizar, UsoParidadParametroFinanciero.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_PARAMETRO_FINANCIERO_QUITADO_EXITOSAMENTE", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}
		
		
		log.write("FIN::QuitarUsoParidadParametroFinanciero");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_UsosParidadParametroFinanciero\":[{\"IdUso\":\"CARTERA\",\"IdParametroFinanciero\":\"US$\"}]}";
				
				EntradaQuitarUsoParidadParametroFinanciero Objin = gson.fromJson(json, EntradaQuitarUsoParidadParametroFinanciero.class);
				
				QuitarUsoParidadParametroFinanciero  kk = new QuitarUsoParidadParametroFinanciero();
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
