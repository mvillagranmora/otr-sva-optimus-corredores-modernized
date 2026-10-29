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
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearParametroFinanciero implements JavaCall{
	private final String SQL_INSERT_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/CrearParametroFinanciero_InsertParametroFinanciero.sql");
	private final String SQL_SELECT_PARAMETRO_FINANCIERO = loadResourceToString("cl/bcs/plataforma/mer/sql/CrearParametroFinanciero_SelectParametroFinanciero.sql");
	
	public static class EntradaCrearParametroFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public List<Registro> p_Registros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public EntradaCrearParametroFinanciero() {
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
	
	public static class SalidaCrearParametroFinanciero {
		
		public List<Registro> P_OUT_RESULTADO;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<Advertencia> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Errores> P_OUT_ERRORESDETALLE;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public SalidaCrearParametroFinanciero(){
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
		EntradaCrearParametroFinanciero in=gson.fromJson(jin.toString() ,EntradaCrearParametroFinanciero.class);
		SalidaCrearParametroFinanciero out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearParametroFinanciero logica(Connection con, EntradaCrearParametroFinanciero entrada) throws Exception {
		SalidaCrearParametroFinanciero salida = new SalidaCrearParametroFinanciero();
		SQL sql = new SQL();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<String> fechas = new ArrayList<String>();
		List<String> idParametroFinanciero = new ArrayList<String>();
		for(Registro registro : entrada.p_Registros){
			fechas.add(registro.Fecha);
			idParametroFinanciero.add(registro.IdParametroFinanciero);
		}
		params.put("Fecha", fechas);
		params.put("IdParametroFinanciero", idParametroFinanciero);
		List<Registro> registros = sql.query(con, SQL_SELECT_PARAMETRO_FINANCIERO, params, Registro.class);
		List<Registro> registrosExistentes = sql.query(con, SQL_SELECT_PARAMETRO_FINANCIERO, params, Registro.class);
		for(Registro registro : entrada.p_Registros){
			for(Registro registroExistente : registros){
				if(registro.Fecha.equals(registroExistente.Fecha) && registro.IdParametroFinanciero.equalsIgnoreCase(registroExistente.IdParametroFinanciero)){
					registrosExistentes.add(registro);
				}
			}
		}
		if(registrosExistentes.isEmpty()){
			sql.batch(con, SQL_INSERT_PARAMETRO_FINANCIERO, entrada.p_Registros, Registro.class);
		}else{
			Errores error = new Errores();
			error.Codigo = "UI_REGISTRO_EXISTE";
			error.Descripcion = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_REGISTRO_EXISTE",null,con,entrada.p_IdEmpresa).descripcion;
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		salida.P_OUT_RESULTADO = entrada.p_Registros;
		return salida;
	}
	
	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getDESAConnection()){
  				Gson gson = new GsonBuilder().create();
  				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Registros\":[{\"Fecha\":\"2017-07-13\",\"IdParametroFinanciero\":\"UF\",\"Valor\":26679.2999}]}";
  				EntradaCrearParametroFinanciero entrada = gson.fromJson(json, EntradaCrearParametroFinanciero.class);
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				CrearParametroFinanciero demo= new CrearParametroFinanciero();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
  			}
	}
	
}
