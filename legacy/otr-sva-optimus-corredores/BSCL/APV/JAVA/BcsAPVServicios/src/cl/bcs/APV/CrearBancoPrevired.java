package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.ValidarBancoPrevired.ResultadoValidacion;
import cl.bcs.APV.pojo.BancoPreviredEntrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
public class CrearBancoPrevired implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBancoPrevired.class);
	
	public final String SQL_INSERT_BANCOS_PREVIRED = loadResourceToString("cl/bcs/APV/sql/CrearBancoPrevired_InsertBancosPrevired.sql");
	
	public static class EntradaCrearBancoPrevired extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<BancoPreviredEntrada> p_BancosPrevired;
		
		public EntradaCrearBancoPrevired() {
			this.p_BancosPrevired = new ArrayList<BancoPreviredEntrada>();
		}
	}
		
	public static class SalidaCrearBancoPrevired extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearBancoPrevired() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new  ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearBancoPrevired jin = gson.fromJson(in.toString(), EntradaCrearBancoPrevired.class);
		SalidaCrearBancoPrevired out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearBancoPrevired logica(Connection con, EntradaCrearBancoPrevired entrada) throws Exception {
		logger.info("INICIO::CrearBancoPrevired");
		long inicio = System.currentTimeMillis();
		SalidaCrearBancoPrevired out = new SalidaCrearBancoPrevired();
		
		ResultadoValidacion resultadoValidacion = validarBancosPreviredEntrada(con, entrada);
		List<BancoPreviredEntrada> bancosPreviredAInsertar = resultadoValidacion.BancosPreviredCorrectos;
		insertarBancosPrevired(con, bancosPreviredAInsertar);
		
		out.P_OUT_MENSAJES = resultadoValidacion.MensajesErrores;
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearBancoPrevired", fin - inicio);
		return out;
	}
	
	private ResultadoValidacion validarBancosPreviredEntrada(Connection con, EntradaCrearBancoPrevired entrada) throws Exception{
		logger.debug("INICIO::CrearBancoPrevired.validarBancosPreviredEntrada");
		long inicio = System.currentTimeMillis();
		ValidarBancoPrevired validarBancoPrevired = new ValidarBancoPrevired();
		ResultadoValidacion resultadoValidacion = validarBancoPrevired.validarCreacionBancoPrevired(con, entrada.p_BancosPrevired, entrada.p_IdEmpresa);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBancoPrevired.validarBancosPreviredEntrada", fin - inicio);
		return resultadoValidacion;
	}
	
	private void insertarBancosPrevired(Connection con, List<BancoPreviredEntrada> bancosPreviredAInsertar) throws Exception{
		logger.debug("INICIO::CrearBancoPrevired.insertarBancosPrevired");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_BANCOS_PREVIRED, bancosPreviredAInsertar, BancoPreviredEntrada.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBancoPrevired.insertarBancosPrevired", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"APV\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_BancosPrevired\":[{\"IdInterno\":0,\"IdBanco\":12,\"CodigoBancoPrevired\":\"BancoTest2\"}]}";
		EntradaCrearBancoPrevired Objin = gson.fromJson(json, EntradaCrearBancoPrevired.class);
		CrearBancoPrevired demo=new CrearBancoPrevired();				
		try {
			SalidaCrearBancoPrevired ObjOut=demo.logica(con, Objin);
			con.commit();
			//con.rollback();
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
