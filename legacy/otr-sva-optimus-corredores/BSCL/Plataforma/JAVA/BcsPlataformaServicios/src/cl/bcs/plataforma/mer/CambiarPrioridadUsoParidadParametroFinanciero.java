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

public class CambiarPrioridadUsoParidadParametroFinanciero implements JavaCall{
	
	private final Log log = new Log("PLA.CambiarPrioridadUsoParidadParametroFinanciero");
	
	private final String SQL_SELECT_USO_PARIDAD_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/CambiarPrioridadUsoParidadParametroFinanciero_SelectUsoParidadParametroFinanciero.sql");
	private final String SQL_UPDATE_USO_PARIDAD_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/CambiarPrioridadUsoParidadParametroFinanciero_UpdateUsoParidadParametroFinanciero.sql");
	
	public static class EntradaCambiarPrioridadUsoParidadParametroFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdUso;
		public String p_IdParametroFinanciero;
		public Long p_PrioridadNueva;
		public Long p_PrioridadAnterior;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class SalidaCambiarPrioridadUsoParidadParametroFinanciero{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCambiarPrioridadUsoParidadParametroFinanciero(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
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
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaCambiarPrioridadUsoParidadParametroFinanciero in = gson.fromJson(jin.toString(), EntradaCambiarPrioridadUsoParidadParametroFinanciero.class);
		SalidaCambiarPrioridadUsoParidadParametroFinanciero out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCambiarPrioridadUsoParidadParametroFinanciero logica(Connection con, EntradaCambiarPrioridadUsoParidadParametroFinanciero entrada) throws Exception {
		log.write("INICIO::QuitarUsoParidadParametroFinanciero");
		SalidaCambiarPrioridadUsoParidadParametroFinanciero out = new SalidaCambiarPrioridadUsoParidadParametroFinanciero();
		
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdUso", entrada.p_IdUso);
		params.put("IdParametroFinanciero", entrada.p_IdParametroFinanciero);
		params.put("Prioridad", entrada.p_PrioridadNueva);
		List<UsoParidadParametroFinanciero> usos = sql.query(con, SQL_SELECT_USO_PARIDAD_PARAMETRO_FINANCIERO, params, UsoParidadParametroFinanciero.class);
		
		UsoParidadParametroFinanciero usoNuevaPrioridad = null;
		UsoParidadParametroFinanciero usoAntiguaPrioridad = null;
		List<UsoParidadParametroFinanciero> usoActualizar = new ArrayList<UsoParidadParametroFinanciero>();
		for(UsoParidadParametroFinanciero uso : usos){
			if(uso.IdParametroFinanciero.equals(entrada.p_IdParametroFinanciero) && uso.Prioridad.equals(entrada.p_PrioridadAnterior)){
				usoNuevaPrioridad = uso;
			}else if (uso.Prioridad.equals(entrada.p_PrioridadNueva)){
				usoAntiguaPrioridad = uso;
			}
		}
		
		if(usoNuevaPrioridad == null || usoAntiguaPrioridad == null){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_PRIORIDAD_PARAMETRO_FINANCIERO_INVALIDA", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}else{
			usoNuevaPrioridad.Prioridad = entrada.p_PrioridadNueva;
			usoAntiguaPrioridad.Prioridad = entrada.p_PrioridadAnterior;
			usoActualizar.add(usoNuevaPrioridad);
			usoActualizar.add(usoAntiguaPrioridad);
			sql.batch(con, SQL_UPDATE_USO_PARIDAD_PARAMETRO_FINANCIERO, usoActualizar, UsoParidadParametroFinanciero.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_CAMBIO_PRIORIDAD_PARAMETRO_FINANCIERO_EXITOSO", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}
		
		log.write("FIN::QuitarUsoParidadParametroFinanciero");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_IdUso\":\"CARTERA\",\"p_IdParametroFinanciero\":\"USD_COM_MIL\",\"p_PrioridadNueva\":2,\"p_PrioridadAnterior\":3}";
				
				EntradaCambiarPrioridadUsoParidadParametroFinanciero Objin = gson.fromJson(json, EntradaCambiarPrioridadUsoParidadParametroFinanciero.class);
				
				CambiarPrioridadUsoParidadParametroFinanciero  kk = new CambiarPrioridadUsoParidadParametroFinanciero();
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
