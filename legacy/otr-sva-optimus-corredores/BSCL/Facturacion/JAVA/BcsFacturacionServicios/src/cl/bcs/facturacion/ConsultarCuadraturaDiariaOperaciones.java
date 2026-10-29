package cl.bcs.facturacion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCuadraturaDiariaOperaciones implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarCuadraturaDiariaOperaciones.class);
	
	private final String SQL_SELECT_CUADRATURAS = loadResourceToString("cl/bcs/facturacion/sql/ConsultarCuadraturaDiariaOperaciones_SelectCuadraturas.sql");
	
	public static class EntradaConsultarCuadraturaDiariaOperaciones{
		public Long p_IdEmpresa;
		public String p_IdModuloCuadratura;
		public String p_FechaEjecucion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCuadraturaDiariaOperaciones{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Cuadratura> P_OUT_RESUMEN;
		
		public SalidaConsultarCuadraturaDiariaOperaciones() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESUMEN = new ArrayList<Cuadratura>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cuadratura{		
		public String IdModuloCuadratura;
		public String IdCuadraturaDiariaOperaciones;
		public Long   IdEmpresa;
		public String OperacionCuadraturaDiariaOperaciones;
		public String FechaEjecucion;
		public Long   TotalOperaciones;
		public Long   OperacionesConProblemas;
		public String TipoOperacion;
		public String Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarCuadraturaDiariaOperaciones jin = gson.fromJson(in.toString(), EntradaConsultarCuadraturaDiariaOperaciones.class);
		SalidaConsultarCuadraturaDiariaOperaciones out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarCuadraturaDiariaOperaciones logica(Connection con, EntradaConsultarCuadraturaDiariaOperaciones entrada) throws Exception {
		logger.info("INICIO::FAC.ConsultarCuadraturaDiariaOperaciones");
		SalidaConsultarCuadraturaDiariaOperaciones out = new SalidaConsultarCuadraturaDiariaOperaciones();
		
		out.P_OUT_RESUMEN = obtenerCuadraturas(con,entrada);
		
		logger.info("FIN::FAC.ConsultarCuadraturaDiariaOperaciones");
		return out;
	}
		
	private List<Cuadratura> obtenerCuadraturas(Connection con, EntradaConsultarCuadraturaDiariaOperaciones entrada) throws Exception{
		logger.info("INICIO::FAC.ConsultarCuadraturaDiariaOperaciones.obtenerCuadraturas");
		
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdModuloCuadratura", entrada.p_IdModuloCuadratura);
		params.put("FechaEjecucion", entrada.p_FechaEjecucion);
		
		List<Cuadratura> cuadraturas = sql.query(con, SQL_SELECT_CUADRATURAS, params, Cuadratura.class);
		
		logger.info("FIN::FAC.ConsultarCuadraturaDiariaOperaciones.obtenerCuadraturas");
		return cuadraturas;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()){ 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdModuloCuadratura\":\"FACTURACION\",\"p_FechaEjecucion\":\"2020-01-06\"}";
		
		EntradaConsultarCuadraturaDiariaOperaciones Objin = gson.fromJson(jsonRF, EntradaConsultarCuadraturaDiariaOperaciones.class);
		ConsultarCuadraturaDiariaOperaciones demo=new ConsultarCuadraturaDiariaOperaciones();				
		
		try {
			SalidaConsultarCuadraturaDiariaOperaciones ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
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