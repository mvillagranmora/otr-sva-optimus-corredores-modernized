package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
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

public class AdministrarParametroFinanciero implements JavaCall{
	
	private final Log log = new Log("PLA.AdministrarParametroFinanciero");
	
	private final String SQL_CREAR_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarParametroFinanciero_CrearParametroFinanciero.sql");
	private final String SQL_UPDATE_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarParametroFinanciero_UpdateParametroFinanciero.sql");
	private final String SQL_CONSULTAR_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarParametroFinanciero_ConsultarParametroFinanciero.sql");
	private final String SQL_CONSULTAR_VALOR_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarParametroFinanciero_ConsultarValorParametroFinanciero.sql");
	
	public static class EntradaAdministrarParametroFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<EntradaParametroFinanciero> p_ParametrosFinancieros;
		
		public EntradaAdministrarParametroFinanciero(){
			this.p_SimularOperacion = "N";
			this.p_ParametrosFinancieros = new ArrayList<EntradaParametroFinanciero>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EntradaParametroFinanciero{
		public String Fecha;
		public String IdParametroFinanciero;
		public BigDecimal Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroFinanciero{
		public String IdParametroFinanciero;
		public String Periodo;
		public String Nombre;
		public String Descripcion;
		public String IdMonedaParametro;
		public String IdMonedaValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ValorParametroFinanciero{
		public String IdParametroFinanciero;
		public String Fecha;
		public String AgnoMes;
		public String Agno;
		public BigDecimal Valor;
		
		public ValorParametroFinanciero(){
			this.AgnoMes = "";
			this.Agno = "";
			this.Valor = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class SalidaAdministrarParametroFinanciero{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAdministrarParametroFinanciero(){
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
		EntradaAdministrarParametroFinanciero in = gson.fromJson(jin.toString(), EntradaAdministrarParametroFinanciero.class);
		SalidaAdministrarParametroFinanciero out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarParametroFinanciero logica(Connection con, EntradaAdministrarParametroFinanciero entrada) throws Exception {
		log.write("INICIO::AdministrarParametroFinanciero");
		SalidaAdministrarParametroFinanciero out = new SalidaAdministrarParametroFinanciero();
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		List<String> idsParametroFinanciero = new ArrayList<String>();
		List<String> fechas = new ArrayList<String>();
		for(EntradaParametroFinanciero entradaParametroFinanciero : entrada.p_ParametrosFinancieros){
			if(!idsParametroFinanciero.contains(entradaParametroFinanciero.IdParametroFinanciero)) idsParametroFinanciero.add(entradaParametroFinanciero.IdParametroFinanciero);
			if(!fechas.contains(entradaParametroFinanciero.Fecha)) fechas.add(entradaParametroFinanciero.Fecha);
		}
		
		params.put("IdParametroFinanciero", idsParametroFinanciero);
		List<ParametroFinanciero> parametrosFinancieros = sql.query(con, SQL_CONSULTAR_PARAMETRO_FINANCIERO, params, ParametroFinanciero.class);
		HashMap<String,Object> hParametrosFinancieros = Utils.List2Hash(parametrosFinancieros, "IdParametroFinanciero");
		
		params.clear();
		params.put("IdParametroFinanciero", idsParametroFinanciero);
		params.put("Fecha", fechas);
		List<ValorParametroFinanciero> valoresParametrosFinancieros = sql.query(con, SQL_CONSULTAR_VALOR_PARAMETRO_FINANCIERO, params, ValorParametroFinanciero.class);
		
		List<ValorParametroFinanciero> valoresActualizar = new ArrayList<ValorParametroFinanciero>();
		List<ValorParametroFinanciero> valoresInsertar = new ArrayList<ValorParametroFinanciero>();
		
		for(EntradaParametroFinanciero entradaParametroFinanciero : entrada.p_ParametrosFinancieros){
			ParametroFinanciero parametroFinanciero = (ParametroFinanciero) hParametrosFinancieros.get(entradaParametroFinanciero.IdParametroFinanciero);
			if(parametroFinanciero == null){
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> valoresMensaje = new HashMap<String,String>();
				valoresMensaje.put("IdParametroFinanciero", entradaParametroFinanciero.IdParametroFinanciero);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_PARAMETRO_FINANCIERO", valoresMensaje,con, entrada.p_IdEmpresa));
			}
			else if (entradaParametroFinanciero.Valor == null || BigDecimal.ZERO.compareTo(entradaParametroFinanciero.Valor) == 0){
				HashMap<String,String> valoresMensaje = new HashMap<String,String>();
				valoresMensaje.put("IdParametroFinanciero", entradaParametroFinanciero.IdParametroFinanciero);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_PARAMETRO_FINANCIERO_VALOR_CERO", valoresMensaje,con, entrada.p_IdEmpresa));
			}
			else{
				ValorParametroFinanciero valorParametroFinanciero = null;
				for(ValorParametroFinanciero valorExistente : valoresParametrosFinancieros){
					if(valorExistente.IdParametroFinanciero.equals(parametroFinanciero.IdParametroFinanciero) && valorExistente.Fecha.equals(entradaParametroFinanciero.Fecha)){
						valorParametroFinanciero = valorExistente;
						break;
					}
				}
				if(valorParametroFinanciero == null){
					valorParametroFinanciero = new ValorParametroFinanciero();
					valorParametroFinanciero.IdParametroFinanciero = entradaParametroFinanciero.IdParametroFinanciero;
					valorParametroFinanciero.Fecha = entradaParametroFinanciero.Fecha;
					valorParametroFinanciero.Valor = entradaParametroFinanciero.Valor;
					if("ME".equals(parametroFinanciero.Periodo)){
						valorParametroFinanciero.AgnoMes = entradaParametroFinanciero.Fecha.substring(0, 7);
					}
					if("AN".equals(parametroFinanciero.Periodo)){
						valorParametroFinanciero.Agno = entradaParametroFinanciero.Fecha.substring(0, 4);
					}
					valoresInsertar.add(valorParametroFinanciero);
				}else{
					valorParametroFinanciero.Valor = entradaParametroFinanciero.Valor;
					valoresActualizar.add(valorParametroFinanciero);
				}
			}
		}
		
		if("N".equals(entrada.p_SimularOperacion)){
			sql.batch(con, SQL_CREAR_PARAMETRO_FINANCIERO, valoresInsertar, ValorParametroFinanciero.class);
			sql.batch(con, SQL_UPDATE_PARAMETRO_FINANCIERO, valoresActualizar, ValorParametroFinanciero.class);
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_PARAMETRO_FINANCIERO_ACTUALIZADO_CORRECTAMENTE", new HashMap<String,String>() ,con, entrada.p_IdEmpresa));
		}
		
		log.write("FIN::AdministrarParametroFinanciero");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ParametrosFinancieros\":[{\"Fecha\":\"2018-08-01\",\"IdParametroFinanciero\":\"TAB90\",\"Valor\":33},{\"Fecha\":\"2018-01-01\",\"IdParametroFinanciero\":\"TAB90\",\"Valor\":3}]}";
				
				EntradaAdministrarParametroFinanciero Objin = gson.fromJson(json, EntradaAdministrarParametroFinanciero.class);
				
				AdministrarParametroFinanciero  kk = new AdministrarParametroFinanciero();
				System.out.println(kk.logica(con, Objin));
				//con.commit();
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
