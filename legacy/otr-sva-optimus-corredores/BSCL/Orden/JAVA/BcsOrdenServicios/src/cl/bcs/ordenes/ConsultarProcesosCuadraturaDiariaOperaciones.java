package cl.bcs.ordenes;

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

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarProcesosCuadraturaDiariaOperaciones implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarProcesosCuadraturaDiariaOperaciones.class);
		
	private final String SQL_SELECT_CUADRATURAS = loadResourceToString("cl/bcs/ordenes/sql/ConsultarProcesosCuadraturaDiariaOperaciones_SelectCuadraturas.sql");
	
	public static class EntradaConsultarProcesosCuadraturaDiariaOperaciones{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaInicio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarProcesosCuadraturaDiariaOperaciones{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Cuadratura> P_OUT_RESUMEN;
		
		public SalidaConsultarProcesosCuadraturaDiariaOperaciones() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESUMEN = new ArrayList<Cuadratura>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cuadratura{
		public String IdCuadraturaDiariaOperaciones;
		public String OperacionCuadraturaDiariaOperaciones;
		public String FechaEjecucion;
		public Long TotalOperaciones;
		public Long OperacionesConProblemas;
		public String TipoOperacion;
		public String Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarProcesosCuadraturaDiariaOperaciones jin = gson.fromJson(in.toString(), EntradaConsultarProcesosCuadraturaDiariaOperaciones.class);
		SalidaConsultarProcesosCuadraturaDiariaOperaciones out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarProcesosCuadraturaDiariaOperaciones logica(Connection con, EntradaConsultarProcesosCuadraturaDiariaOperaciones entrada) throws Exception {
		logger.info("INICIO::ConsultarProcesosCuadraturaDiariaOperaciones");
		SalidaConsultarProcesosCuadraturaDiariaOperaciones out = new SalidaConsultarProcesosCuadraturaDiariaOperaciones();
		
		String pais = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		out.P_OUT_RESUMEN = obtenerCuadraturas(con,entrada,pais);
		
		logger.info("FIN::ConsultarProcesosCuadraturaDiariaOperaciones");
		return out;
	}
	
	private List<Cuadratura> obtenerCuadraturas(Connection con, EntradaConsultarProcesosCuadraturaDiariaOperaciones entrada, String pais) throws Exception{
		logger.info("INICIO::ConsultarProcesosCuadraturaDiariaOperaciones.obtenerCuadraturas");
		
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaInicio", entrada.p_FechaInicio);
		
		List<Cuadratura> cuadraturas = sql.query(con, SQL_SELECT_CUADRATURAS, params, Cuadratura.class);
		
		logger.info("FIN::ConsultarProcesosCuadraturaDiariaOperaciones.obtenerCuadraturas");
		return cuadraturas;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String jsonRF = "{\"p_IdUsuario\":21,\"p_IdEmpresa\":1,\"p_IdModulo\":\"ORD\",\"p_FechaInicio\":\"2019-08-23\"}";
		EntradaConsultarProcesosCuadraturaDiariaOperaciones Objin = gson.fromJson(jsonRF, EntradaConsultarProcesosCuadraturaDiariaOperaciones.class);
		ConsultarProcesosCuadraturaDiariaOperaciones demo=new ConsultarProcesosCuadraturaDiariaOperaciones();				
		try {
			SalidaConsultarProcesosCuadraturaDiariaOperaciones ObjOut=demo.logica(con, Objin);
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
