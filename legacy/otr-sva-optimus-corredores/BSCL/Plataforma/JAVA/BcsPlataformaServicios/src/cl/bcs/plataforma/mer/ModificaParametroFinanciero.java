package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificaParametroFinanciero implements JavaCall{
	private final String SQL_UPDATE_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/ModificaParametroFinanciero_UpdateParametroFinanciero.sql");
	
	public static class EntradaModificaParametroFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public List<Registro> p_Registros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public EntradaModificaParametroFinanciero() {
			this.p_Registros = new ArrayList<Registro>();
		}
	}
	
	public static class Registro{
		public String Fecha;
		public String IdParametroFinanciero;
		public BigDecimal Valor;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificaParametroFinanciero {
		
		public List<Registro> P_OUT_RESULTADO;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<Advertencia> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Errores> P_OUT_ERRORESDETALLE;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public SalidaModificaParametroFinanciero(){
			this.P_OUT_RESULTADO = new ArrayList<Registro>();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<Advertencia>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<Errores>();
		}
	}
	
	public static class Advertencia{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Errores{
		public String Codigo;
		public String Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaModificaParametroFinanciero in=gson.fromJson(jin.toString() ,EntradaModificaParametroFinanciero.class);
		SalidaModificaParametroFinanciero out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificaParametroFinanciero logica(Connection con, EntradaModificaParametroFinanciero entrada) throws Exception {
		SalidaModificaParametroFinanciero salida = new SalidaModificaParametroFinanciero();
		SQL sql = new SQL();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		sql.batch(con, SQL_UPDATE_PARAMETRO_FINANCIERO, entrada.p_Registros, Registro.class);
		salida.P_OUT_RESULTADO = entrada.p_Registros;
		return salida;
	}
	
	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getDESAConnection()){
  				Gson gson = new GsonBuilder().create();
  				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Registros\":[{\"Fecha\":\"2017-07-13\",\"IdParametroFinanciero\":\"UF\",\"Valor\":26679.2999}]}";
  				EntradaModificaParametroFinanciero entrada = gson.fromJson(json, EntradaModificaParametroFinanciero.class);
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				ModificaParametroFinanciero demo= new ModificaParametroFinanciero();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
  			}
	}
	
	
}
