package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarInformeLimitesCorredora implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ConsultarInformeLimitesCorredora.class);
	
	private final String SQL_CONSULTA_OPERACION_LIMITES_CORREDORA = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarInformeLimitesCorredora_SelectOperaciones.sql");
	
	public static class EntradaConsultarInformacionLimitesCorredora{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdCliente = 0L;
		public String 	p_FechaOperacion = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class SalidaConsultarInformeLimitesCorredora{
		public List<SalidaInformeLimites> P_OUT_TABLA;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public String P_OUT_MENSAJE = "";
		
		public SalidaConsultarInformeLimitesCorredora(){
			this.P_OUT_TABLA = new ArrayList<SalidaInformeLimites>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaInformeLimites{
		public Long 			IdCliente;
		public String 			DocumentoIdentidadCliente; 
		public String 			NombreCliente; 
		public String 			FechaOperacion;
		public String 			IdMonedaPrincipal; 
		public BigDecimal		TotalMontoMonedaPrincipal;
		public String		 	IdMonedaSecundaria; 
		public BigDecimal 		TotalMontoMonedaSecundaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarInformacionLimitesCorredora jin = gson.fromJson(in.toString(), EntradaConsultarInformacionLimitesCorredora.class);
		SalidaConsultarInformeLimitesCorredora out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarInformeLimitesCorredora logica(Connection con, EntradaConsultarInformacionLimitesCorredora entrada) throws Exception {
		logger.info("INICIO::ConsultarInformacionCliente");
		SalidaConsultarInformeLimitesCorredora out = new SalidaConsultarInformeLimitesCorredora();
		
		SQL sql=new SQL();
		
		try{
			
			HashMap<String,Object> params =new HashMap<String,Object>();
			params.put("p_FechaOperacion", entrada.p_FechaOperacion);
			params.put("p_IdCliente", entrada.p_IdCliente);
			
			logger.info("Inicio de SQL_CONSULTA_OPERACION_LIMITES_CORREDORA");
			List<SalidaInformeLimites> InformacionLimitesCorredora = sql.query(con, SQL_CONSULTA_OPERACION_LIMITES_CORREDORA, params, SalidaInformeLimites.class);
			logger.info("Fin de SQL_CONSULTA_OPERACION_LIMITES_CORREDORA");
			out.P_OUT_TABLA = InformacionLimitesCorredora;
			
		}catch(Exception e){
			logger.info("ERROR", e);
			throw e;
		}finally{}
			logger.info("FIN::ConsultarInformacionCliente");
		return out;
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("== DESA ==");
		Connection con = PlataformaUtils.getDESAConnection();
		try {
			EntradaConsultarInformacionLimitesCorredora entrada = new EntradaConsultarInformacionLimitesCorredora();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "MES";
			entrada.p_IdUsuario = 97L;
			entrada.p_FechaOperacion = "2023-04-14";
			entrada.p_IdCliente = 84969L; // 84969L

			SalidaConsultarInformeLimitesCorredora Salida = new SalidaConsultarInformeLimitesCorredora();
			ConsultarInformeLimitesCorredora sp = new ConsultarInformeLimitesCorredora();
			Salida = sp.logica(con,entrada);
			System.out.println(Salida.P_OUT_MENSAJE);
			System.out.println(Salida.P_OUT_TABLA);
			con.commit();
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
		}finally{
			con.close();
		}
		System.out.println("== END ==");
	}
}
