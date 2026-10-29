package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class InsertarLogServicio implements JavaCall{	
	
	private final Log log = new Log("InsertarLogServicio");
	static final Logger logger = LogManager.getLogger(InsertarLogServicio.class);
	
	private final String SQL_INSERT_LOG = loadResourceToString("cl/bcs/plataforma/pla/sql/InsertarLogServicio_InsertRegistroLog.sql");

	public static class EntradaLogServicio {
		public List<RegistroLog> RegistroLog;
	}
	
	public static class RegistroLog {
		public String IdModulo;
		public Long   IdUsuario;
		public String TipoMensaje;
		public String Glosa;
		public String Valor1;
		public String Valor2;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaLogServicio {
		public Long P_OUT_ERRORESCONTADOR;
	}
	
	

	public SalidaLogServicio logica(Connection con, List<RegistroLog> entrada) throws Exception {
		SalidaLogServicio salida = new SalidaLogServicio();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		SQL _sql = new SQL();
		logger.debug("INICIO:InsertarLogServicio");
		long inicioMetodo = System.currentTimeMillis();
		_sql.batch(con, SQL_INSERT_LOG, entrada, RegistroLog.class);
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN:InsertarLogServicio: ",finMetodo- inicioMetodo);
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		log.writeAsJson("SALIDA:",salida);
		return salida;
	}
	
	public RegistroLog armaRegistroLog(String IdModulo,Long IdUsuario, String TipoMensaje, String Glosa, String Valor1, String Valor2) {
		RegistroLog objLogServicio = new RegistroLog();
		objLogServicio.IdModulo = IdModulo;
		objLogServicio.IdUsuario = IdUsuario;
		objLogServicio.TipoMensaje = TipoMensaje;
		objLogServicio.Glosa = Glosa;
		objLogServicio.Valor1 = Valor1;
		objLogServicio.Valor2 = Valor2;
		
		return objLogServicio;
	}
	
	/***********************************************************************************************
	 *                                        CALL
	 ***********************************************************************************************/
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaLogServicio in = gson.fromJson(jin.toString(),EntradaLogServicio.class);
		SalidaLogServicio out = logica(con,in.RegistroLog);
		JsonParser parser 	  = new JsonParser();
        JsonObject dataout	  = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
}
