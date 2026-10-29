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

public class AdministrarIndices implements JavaCall{
	
	private final Log log = new Log("PLA.AdministrarIndices");
	
	private final String SQL_CREAR_INDICE_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarIndices_CrearIndiceFinanciero.sql");
	private final String SQL_UPDATE_INDICE_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarIndices_UpdateIndiceFinanciero.sql");
	private final String SQL_CONSULTAR_INDICE_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarIndices_ConsultarIndiceFinanciero.sql");
	private final String SQL_CONSULTAR_VALOR_INDICE_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/AdministrarIndices_ConsultarValorIndiceFinanciero.sql");
	
	
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
		public String IdIndiceFinanciero;
		public BigDecimal Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroFinanciero{
		public String IdIndiceFinanciero;
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
		public String IdIndiceFinanciero;
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
		log.write("INICIO::AdministrarIndices");
		SalidaAdministrarParametroFinanciero out = new SalidaAdministrarParametroFinanciero();
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		List<String> idsParametroFinanciero = new ArrayList<String>();
		List<String> fechas = new ArrayList<String>();
		for(EntradaParametroFinanciero entradaIndiceFinanciero : entrada.p_ParametrosFinancieros){
			if(!idsParametroFinanciero.contains(entradaIndiceFinanciero.IdIndiceFinanciero)) idsParametroFinanciero.add(entradaIndiceFinanciero.IdIndiceFinanciero);
			if(!fechas.contains(entradaIndiceFinanciero.Fecha)) fechas.add(entradaIndiceFinanciero.Fecha);
		}
		
		params.put("IdIndiceFinanciero", idsParametroFinanciero);
		List<ParametroFinanciero> parametrosFinancieros = sql.query(con, SQL_CONSULTAR_INDICE_FINANCIERO, params, ParametroFinanciero.class);
		HashMap<String,Object> hParametrosFinancieros = Utils.List2Hash(parametrosFinancieros, "IdIndiceFinanciero");
		
		params.clear();
		params.put("IdIndiceFinanciero", idsParametroFinanciero);
		params.put("Fecha", fechas);
		List<ValorParametroFinanciero> valoresParametrosFinancieros = sql.query(con, SQL_CONSULTAR_VALOR_INDICE_FINANCIERO, params, ValorParametroFinanciero.class);
		
		List<ValorParametroFinanciero> valoresActualizar = new ArrayList<ValorParametroFinanciero>();
		List<ValorParametroFinanciero> valoresInsertar = new ArrayList<ValorParametroFinanciero>();
		
		for(EntradaParametroFinanciero entradaIndiceFinanciero : entrada.p_ParametrosFinancieros){
			ParametroFinanciero parametroFinanciero = (ParametroFinanciero) hParametrosFinancieros.get(entradaIndiceFinanciero.IdIndiceFinanciero);
			if(parametroFinanciero == null){
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> valoresMensaje = new HashMap<String,String>();
				valoresMensaje.put("IdIndiceFinanciero", entradaIndiceFinanciero.IdIndiceFinanciero);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_INDICE", valoresMensaje,con, entrada.p_IdEmpresa));
			}
			else if (entradaIndiceFinanciero.Valor == null /*|| BigDecimal.ZERO.compareTo(entradaIndiceFinanciero.Valor) == 0*/){
				HashMap<String,String> valoresMensaje = new HashMap<String,String>();
				valoresMensaje.put("IdIndiceFinanciero", entradaIndiceFinanciero.IdIndiceFinanciero);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, "UI_INDICE_PRESENTA_VALOR_NULO", valoresMensaje,con, entrada.p_IdEmpresa));
			}
			else{
				ValorParametroFinanciero valorParametroFinanciero = null;
				for(ValorParametroFinanciero valorExistente : valoresParametrosFinancieros){
					if(valorExistente.IdIndiceFinanciero.equals(parametroFinanciero.IdIndiceFinanciero) && valorExistente.Fecha.equals(entradaIndiceFinanciero.Fecha)){
						valorParametroFinanciero = valorExistente;
						break;
					}
				}
				if(valorParametroFinanciero == null){
					valorParametroFinanciero = new ValorParametroFinanciero();
					valorParametroFinanciero.IdIndiceFinanciero = entradaIndiceFinanciero.IdIndiceFinanciero;
					valorParametroFinanciero.Fecha = entradaIndiceFinanciero.Fecha;
					valorParametroFinanciero.Valor = entradaIndiceFinanciero.Valor;
					if("ME".equals(parametroFinanciero.Periodo)){
						valorParametroFinanciero.AgnoMes = entradaIndiceFinanciero.Fecha.substring(0, 7);
					}
					if("AN".equals(parametroFinanciero.Periodo)){
						valorParametroFinanciero.Agno = entradaIndiceFinanciero.Fecha.substring(0, 4);
					}
					valoresInsertar.add(valorParametroFinanciero);
				}else{
					valorParametroFinanciero.Valor = entradaIndiceFinanciero.Valor;
					valoresActualizar.add(valorParametroFinanciero);
				}
			}
		}
		
		if("N".equals(entrada.p_SimularOperacion)){
			sql.batch(con, SQL_CREAR_INDICE_FINANCIERO, valoresInsertar, ValorParametroFinanciero.class);
			sql.batch(con, SQL_UPDATE_INDICE_FINANCIERO, valoresActualizar, ValorParametroFinanciero.class);
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INDICE_FUE_ACTUALIZADO_CORRECTAMENTE", new HashMap<String,String>() ,con, entrada.p_IdEmpresa));
		}
		
		log.write("FIN::AdministrarIndices");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ParametrosFinancieros\":[{\"Fecha\":\"2019-07-01\",\"IdIndiceFinanciero\":\"ICP\",\"Valor\":4444}]}";				                
				EntradaAdministrarParametroFinanciero Objin = gson.fromJson(json, EntradaAdministrarParametroFinanciero.class);
				
				AdministrarIndices  kk = new AdministrarIndices();
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

