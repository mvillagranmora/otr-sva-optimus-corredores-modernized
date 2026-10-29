package cl.bcs.plataforma.demo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PingSession implements JavaCall {

	/*
	 * Nuestro gran amigo el log
	 */
	private final Logger logger = LogManager.getLogger(PingSession.class);

	/*
	 * 
	 * Queries que van a ser utilizadas por el servicio. Deben usar : para optimizar
	 * con los PreparedStatement
	 * 
	 */
	private final String SQL_SELECT_DUMMY = loadResourceToString("cl/bcs/plataforma/demo/sql/PingSession_select_dummy.sql");
	
	/*
	 * 
	 * Clases utilitarias que representan objetos IN/OUT Recordar que solo son
	 * permitidos tipos: - long como long - int como int - decimal como BigDecimal -
	 * char como string - varchar como string - date como string YYYY-MM-DD -
	 * datetime como string YYYY-MM-DD HH:MI:SS
	 */
	public class ServiceDemoEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_DelayJava;
		public String p_InsertaLog;
		
		public ServiceDemoEntrada() {
			this.p_DelayJava = 0L;
			this.p_InsertaLog = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public class ServiceDemoSalida{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		ServiceDemoSalida(){
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}

	}
	
	public static class ResultadoQuery extends BcsPojo{
		public Integer Contador;
	}

	/*
	 * 
	 * Objeto a ser utilizado para convertir y procesar las llamadas de la capa de
	 * transporte.
	 * 
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ServiceDemoEntrada in = gson.fromJson(jin.toString(), ServiceDemoEntrada.class);
		ServiceDemoSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) {

		// Example
		try (Connection con = PlataformaUtils.getDESAConnection()){
			
			// ---------------------------------
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\": 1, \"p_IdUsuario\": 3, \"p_IdModulo\": \"PLA\"}").getAsJsonObject();
			PingSession demo = new PingSession();
			System.out.println(demo.call(con, dataJsonIn).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

	/*
	 * Este servicio optimiza las llamadas internas para java dado que no hay
	 * necesidad de usar JSON
	 * 
	 * Estrategias a la hora de implementar:
	 * 
	 * - Obtener datos masivamente de la base datos - procesar logica de forma
	 * unitaria - Enviar datos masivamente a la base datos con batch
	 * 
	 * 
	 */
	public ServiceDemoSalida logica(Connection con, ServiceDemoEntrada entrada) throws Exception {
		long init = System.currentTimeMillis();
		
		if ("S".equalsIgnoreCase(entrada.p_InsertaLog))
			agregarLog(con, entrada, "INICIO");
		
		if (entrada.p_DelayJava > 0){
			logger.debug("Durmiendo por " + entrada.p_DelayJava + " milisegundos...");
			Thread.sleep(entrada.p_DelayJava);
			logger.debug("Desperto");
		}
		
		ServiceDemoSalida salida = new ServiceDemoSalida();
		SQL sql = new SQL();
		
		List<ResultadoQuery> resultado = sql.query(con, SQL_SELECT_DUMMY, new HashMap<String,Object>(), ResultadoQuery.class);
		resultado.forEach(res -> {
			logger.debug(res.Contador);
		});
		
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"OK","Served in: " + (System.currentTimeMillis()-init) ));
		
		if ("S".equalsIgnoreCase(entrada.p_InsertaLog))
			agregarLog(con, entrada, "FIN");
		
		return salida;
	}
	
	private void agregarLog(Connection con, ServiceDemoEntrada entrada, String posicion) throws Exception {
		logger.debug("INICIO::PingSession.agregarLog");
		Long inicio = System.currentTimeMillis();
		List<AgregarLog.InAgregarLog.Log> listaLog = new ArrayList<>();
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		
		log.IdTrabajo = 1L; 
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "INFORMATIVO";
		log.IdModulo = "PLA";
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = FechaSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdTipoRegistro = "PING";
		log.Nemo = posicion;
		log.NombreProceso = "cl.bcs.plataforma.demo.PingSession.java";
				
		listaLog.add(log);
	
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;		
		entradaAgregarLog.p_Log = listaLog;	
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::PingSession.agregarLog:" , fin - inicio);
	}
}
