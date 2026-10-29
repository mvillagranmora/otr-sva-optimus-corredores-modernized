package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.GuardarConfiguracionGrillaUsuario.Parametros_Entrada;
import cl.bcs.plataforma.pla.GuardarConfiguracionGrillaUsuario.Parametros_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class InsertarLogVentana implements JavaCall{	
	
	private final Log log = new Log("InsertarLogVentana");
	static final Logger logger = LogManager.getLogger(InsertarLogVentana.class);
	
	private final String SQL_INSERT_LOG_VENTANA = loadResourceToString("cl/bcs/plataforma/pla/sql/InsertarLogVentana_InsertRegistroLogVentana.sql");

	public static class EntradaLogServicio {
		public String p_IdVentana;
		public String p_TituloVentana;
		public Long   p_IdServicio;
		public Long   p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
//	public static class RegistroLog {
//		public String p_IdVentana;
//		public String p_TituloVentana;
//		public Long   p_IdServicio;
//		public Long   p_IdUsuario;
//		
//		public String toString() {
//			return Utils.printJson(this, false);
//		}
//	}

	public static class SalidaLogServicio {
		public Long P_OUT_ERRORESCONTADOR;
	}
	
	

	public SalidaLogServicio logica(Connection con, EntradaLogServicio entrada) throws Exception {
		SalidaLogServicio salida = new SalidaLogServicio();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		SQL _sql = new SQL();
		//logger.debug("INICIO:InsertarLogVentana");
		long inicioMetodo = System.currentTimeMillis();
		
		if ((entrada.p_TituloVentana).length() > 100) {
			entrada.p_TituloVentana = entrada.p_TituloVentana.substring(0, 100);  
		}
        // Crear una lista de objetos EntradaLogServicio
        List<EntradaLogServicio> listEntrada = new ArrayList<>();
        listEntrada.add(entrada);
		
		_sql.batch(con, SQL_INSERT_LOG_VENTANA, listEntrada, EntradaLogServicio.class);
		
	
		long finMetodo = System.currentTimeMillis();
		//logger.debug("FIN:InsertarLogVentana: ",finMetodo- inicioMetodo);
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		//log.writeAsJson("SALIDA:",salida);
		return salida;
	}
	

	/***********************************************************************************************
	 *                                        CALL
	 ***********************************************************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaLogServicio in = gson.fromJson(jin.toString(),EntradaLogServicio.class);
		SalidaLogServicio out = logica(con,in);
		JsonParser parser 	  = new JsonParser();
        JsonObject dataout	  = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

}
