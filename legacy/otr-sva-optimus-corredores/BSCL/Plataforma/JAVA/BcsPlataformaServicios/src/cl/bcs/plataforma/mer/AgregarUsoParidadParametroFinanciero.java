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

public class AgregarUsoParidadParametroFinanciero implements JavaCall{
	
	private final Log log = new Log("PLA.AgregarUsoParidadParametroFinanciero");
	
	private final String SQL_SELECT_USO_PARIDAD_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AgregarUsoParidadParametroFinanciero_SelectUsoParidadParametroFinanciero.sql");
	private final String SQL_INSERT_USO_PARIDAD_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AgregarUsoParidadParametroFinanciero_InsertUsoParidadParametroFinanciero.sql");
	
	public static class EntradaAgregarUsoParidadParametroFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InUsoParidadParametroFinanciero> p_UsosParidadParametroFinanciero;
		
		public EntradaAgregarUsoParidadParametroFinanciero(){
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
	
	public static class SalidaAgregarUsoParidadParametroFinanciero{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAgregarUsoParidadParametroFinanciero(){
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
		EntradaAgregarUsoParidadParametroFinanciero in = gson.fromJson(jin.toString(), EntradaAgregarUsoParidadParametroFinanciero.class);
		SalidaAgregarUsoParidadParametroFinanciero out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAgregarUsoParidadParametroFinanciero logica(Connection con, EntradaAgregarUsoParidadParametroFinanciero entrada) throws Exception {
		log.write("INICIO::AgregarUsoParidadParametroFinanciero");
		
		SalidaAgregarUsoParidadParametroFinanciero out = new SalidaAgregarUsoParidadParametroFinanciero();
		SQL sql = new SQL();
		
		List<String> usos = new ArrayList<String>();
		for(InUsoParidadParametroFinanciero uso : entrada.p_UsosParidadParametroFinanciero){
			usos.add(uso.IdUso);
		}
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdUso", usos);
		
		List<UsoParidadParametroFinanciero> usosExistentes = sql.query(con, SQL_SELECT_USO_PARIDAD_PARAMETRO_FINANCIERO, params, UsoParidadParametroFinanciero.class);
		HashMap<String,Object> hUsosExistentes = Utils.List2Hash(usosExistentes, "IdUsoIdParametroFinanciero");
		Long ultimaPrioridad = usosExistentes.size() > 0 ? usosExistentes.get(usosExistentes.size() -1).Prioridad : 0L;
		
		List<UsoParidadParametroFinanciero> insertUsos = new ArrayList<UsoParidadParametroFinanciero>();
		List<String> parametrosEntrada = new ArrayList<String>();
		
		for(InUsoParidadParametroFinanciero uso : entrada.p_UsosParidadParametroFinanciero){
			
			UsoParidadParametroFinanciero usoExistente = (UsoParidadParametroFinanciero) hUsosExistentes.get(uso.IdUso + "_" + uso.IdParametroFinanciero);
			
			if(parametrosEntrada.contains(uso.IdParametroFinanciero)){
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> datosMensaje = new HashMap<String,String>();
				datosMensaje.put("IdParametroFinanciero", uso.IdParametroFinanciero);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_PARAMETRO_FINANCIERO_ENTRADA_REPETIDO", datosMensaje,con, entrada.p_IdEmpresa));
			}else{
				parametrosEntrada.add(uso.IdParametroFinanciero);
				if(usoExistente == null){
					ultimaPrioridad++;
					UsoParidadParametroFinanciero nuevoUsoParametro = new UsoParidadParametroFinanciero();
					nuevoUsoParametro = Utils.copyPojo(uso, nuevoUsoParametro);
					nuevoUsoParametro.Prioridad = ultimaPrioridad;
					insertUsos.add(nuevoUsoParametro);
				}else{
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String,String> datosMensaje = new HashMap<String,String>();
					datosMensaje.put("IdParametroFinanciero", uso.IdParametroFinanciero);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_PARAMETRO_FINANCIERO_YA_EXISTE", datosMensaje,con, entrada.p_IdEmpresa));
				}
			}			
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)){
			sql.batch(con, SQL_INSERT_USO_PARIDAD_PARAMETRO_FINANCIERO, insertUsos, UsoParidadParametroFinanciero.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_PARAMETRO_FINANCIERO_AGREGADO_EXITOSAMENTE", new HashMap<String,String>(),con, entrada.p_IdEmpresa));
		}

		log.write("FIN::AgregarUsoParidadParametroFinanciero");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_UsosParidadParametroFinanciero\":[{\"IdUso\":\"CARTERA\",\"IdParametroFinanciero\":\"US$\"}]}";
				
				EntradaAgregarUsoParidadParametroFinanciero Objin = gson.fromJson(json, EntradaAgregarUsoParidadParametroFinanciero.class);
				
				AgregarUsoParidadParametroFinanciero  kk = new AgregarUsoParidadParametroFinanciero();
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
