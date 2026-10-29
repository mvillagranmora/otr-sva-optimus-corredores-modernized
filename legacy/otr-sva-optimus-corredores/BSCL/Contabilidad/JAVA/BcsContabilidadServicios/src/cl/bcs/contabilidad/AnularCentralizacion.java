package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularCentralizacion implements JavaCall{
	
	private final Log log = new Log("CON.AnularCentralizacion");
	
	private final String SQL_SELECT_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/AnularCentralizacion_SelectCentralizacion.sql");
	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/contabilidad/sql/AnularCentralizacion_SelectComprobantes.sql");
	private final String SQL_UPDATE_EVENTO = loadResourceToString("cl/bcs/contabilidad/sql/AnularCentralizacion_UpdateEvento.sql");
	private final String SQL_UPDATE_MOVIMIENTO = loadResourceToString("cl/bcs/contabilidad/sql/AnularCentralizacion_UpdateMovimiento.sql");
	private final String SQL_UPDATE_MOVIMIENTO_DATO = loadResourceToString("cl/bcs/contabilidad/sql/AnularCentralizacion_UpdateMovimientoDato.sql");
	private final String SQL_UPDATE_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/AnularCentralizacion_UpdateCentralizacion.sql");
	
	private final String[] estadosNoAnulables = { "A", "E", "M"};
	
	public static class EntradaAnularCentralizacion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdCentralizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAnularCentralizacion{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAnularCentralizacion(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Centralizacion{
		public Long IdCentralizacion;
		public String Estado;
		public String EstadoNombre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Evento{
		public Long IdEvento;
	}
	
	public static class PreComprobante{
		public Long IdPreComprobante;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaAnularCentralizacion in = gson.fromJson(jin.toString(), EntradaAnularCentralizacion.class);
		SalidaAnularCentralizacion out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAnularCentralizacion logica(Connection con, EntradaAnularCentralizacion entrada) throws Exception {
		log.write("INICIO::AnularCentralizacion");
		
		SalidaAnularCentralizacion out = new SalidaAnularCentralizacion();
		
		SQL sql = new SQL();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
				
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdCentralizacion", entrada.p_IdCentralizacion);
		
		List<Centralizacion> centralizaciones = sql.query(con, SQL_SELECT_CENTRALIZACION, params, Centralizacion.class);
		
		List<PreComprobante> precomprobantes = sql.query(con, SQL_SELECT_COMPROBANTES, params, PreComprobante.class);
		
		if(centralizaciones.size() == 0){
			out.P_OUT_ERRORESCONTADOR++;
			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("IdCentralizacion", String.valueOf(entrada.p_IdCentralizacion));
			out.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdCentralizacion,MensajeSalida.MENSAJE_ERROR,"CENTRALIZACION_NO_EXISTE",valores,con,entrada.p_IdEmpresa));
			return out;
		}
		
		if (Arrays.asList(estadosNoAnulables).contains(centralizaciones.get(0).Estado)) {
			out.P_OUT_ERRORESCONTADOR++;
			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("IdCentralizacion", String.valueOf(entrada.p_IdCentralizacion));
			valores.put("EstadoCentralizacion", String.valueOf(centralizaciones.get(0).EstadoNombre));
			out.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdCentralizacion,MensajeSalida.MENSAJE_ERROR,"CENTRALIZACION_NO_SE_PUEDE_ANULAR",valores,con,entrada.p_IdEmpresa));
			return out;
		}
		
		if(precomprobantes.size() > 0){
			out.P_OUT_ERRORESCONTADOR++;
			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("IdCentralizacion", String.valueOf(entrada.p_IdCentralizacion));
			out.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdCentralizacion,MensajeSalida.MENSAJE_ERROR,"CENTRALIZACION_ESTA_CONTABILIZADA",valores,con,entrada.p_IdEmpresa));
			return out;
		}
		
		sql.batch(con, SQL_UPDATE_EVENTO, centralizaciones, Centralizacion.class);
		sql.batch(con, SQL_UPDATE_MOVIMIENTO_DATO, centralizaciones, Centralizacion.class);
		sql.batch(con, SQL_UPDATE_MOVIMIENTO, centralizaciones, Centralizacion.class);
		sql.batch(con, SQL_UPDATE_CENTRALIZACION, centralizaciones, Centralizacion.class);
		
		log.write("FIN::AnularCentralizacion");
		
		return out;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CON\",\"p_IdCentralizacion\":386,\"p_Ejercicio\":\"2018\"}";
		EntradaAnularCentralizacion Objin = gson.fromJson(json, EntradaAnularCentralizacion.class);
		AnularCentralizacion demo= new AnularCentralizacion();				
		try {
			SalidaAnularCentralizacion ObjOut=demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
	
}
