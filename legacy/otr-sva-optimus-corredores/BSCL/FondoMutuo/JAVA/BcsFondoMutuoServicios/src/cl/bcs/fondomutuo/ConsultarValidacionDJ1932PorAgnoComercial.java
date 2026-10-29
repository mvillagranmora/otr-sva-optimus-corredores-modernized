package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarValidacionDJ1932PorAgnoComercial implements JavaCall{
	static final Logger logger = LogManager.getLogger(ConsultarValidacionDJ1932PorAgnoComercial.class);
	private final String SQL_SELECT_VALIDACION_DJ1932 = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarValidacionDJ1932PorAgnoComercial_SelectValidacionDJ1932.sql");

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarValidacionDJ1932PorAgnoComercial in = gson.fromJson(jin.toString(), EntradaConsultarValidacionDJ1932PorAgnoComercial.class);
		SalidaConsultarValidacionDJ1932PorAgnoComercial out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static class EntradaConsultarValidacionDJ1932PorAgnoComercial extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario ;
		public String p_IdModulo;
		public String p_AgnoComercial;
		
		public EntradaConsultarValidacionDJ1932PorAgnoComercial() {
			
		}
	}
	
	public static class SalidaConsultarValidacionDJ1932PorAgnoComercial  extends BcsPojo {
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_ADVERTENCIAS;

		public SalidaConsultarValidacionDJ1932PorAgnoComercial () {
			P_OUT_ADVERTENCIASCONTADOR = 0L;
			P_OUT_ADVERTENCIAS = new ArrayList<>();
		}
	}
	
	public static class ValidacionDJ1932 extends BcsPojo{
		public String CodigoValidacion;
		
		public ValidacionDJ1932 () {
			this.CodigoValidacion = "N";
		}
	}
	
	public SalidaConsultarValidacionDJ1932PorAgnoComercial logica(Connection conn, EntradaConsultarValidacionDJ1932PorAgnoComercial entrada) throws Exception {
		logger.debug("INICIO::ConsultarValidacionDJ1932PorAgnoComercial");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarValidacionDJ1932PorAgnoComercial salida = new  SalidaConsultarValidacionDJ1932PorAgnoComercial();
		
		ValidacionDJ1932 validacion = new ValidacionDJ1932();
		
		if(!entrada.p_AgnoComercial.isEmpty()) {
			SQL sql = new SQL();
			HashMap<String, Object> parametros = new HashMap<>();
			parametros.put("AgnoComercial", entrada.p_AgnoComercial);
			
			validacion = sql.query(conn, SQL_SELECT_VALIDACION_DJ1932,parametros, ValidacionDJ1932.class).parallelStream().findFirst().orElse(new ValidacionDJ1932());
		}
		
		if(validacion.CodigoValidacion.equals("N")) {
			salida.P_OUT_ADVERTENCIAS.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_VALIDACION_DJ1932_ADVERTENCIA",new HashMap<>(),conn,entrada.p_IdEmpresa));
			salida.P_OUT_ADVERTENCIASCONTADOR = (long) salida.P_OUT_ADVERTENCIAS.size();
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarValidacionDJ1932PorAgnoComercial {}", fin - inicio);
		return salida;
		
	}
	
public static void main(String[]args) throws Exception{
		
		Gson gson = Utils.getGsonBuilder();

		String json = "{\"p_AgnoComercial\": \"2020\"\r\n"
				+ "\"p_IdEmpresa\": 1\r\n"
				+ "\"p_IdModulo\": \"FFM\"\r\n"
				+ "\"p_IdUsuario\": 9}";
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			EntradaConsultarValidacionDJ1932PorAgnoComercial Objin = gson.fromJson(json, EntradaConsultarValidacionDJ1932PorAgnoComercial.class);
			
			ConsultarValidacionDJ1932PorAgnoComercial demo=new ConsultarValidacionDJ1932PorAgnoComercial();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaConsultarValidacionDJ1932PorAgnoComercial ObjOut=demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.rollback();
 			
    	}
		 	
		System.out.println("== END ==");
	}
}
