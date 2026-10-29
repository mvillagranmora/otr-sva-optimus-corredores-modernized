package cl.bcs.facturacion;

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

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoAFP implements JavaCall {

	// Log
	static final Logger logger = LogManager.getLogger(GenerarArchivoAFP.class);

	// Queries
	private final String SQL_VALIDACION_AFP = loadResourceToString("cl/bcs/facturacion/sql/GenerarArchivoAFP_ValidarAFP.sql");
	private final String SQL_VALIDACION_MULTIFONDOS = loadResourceToString("cl/bcs/facturacion/sql/GenerarArchivoAFP_Multifondos.sql");

	// -------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------------------------------------------------

	public static class DatosEntrada {
		public int IdAFP;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------------------------------------------------

	public static class ValidacionSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
//		public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;

		public ValidacionSalida() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
//			P_OUT_RESULTADOPROCESO = new ArrayList<ResultadoProceso_Salida>();
		};

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE PROCESAMIENTO
	// -------------------------------------------------------------------------------------------

	public static class SalidaValidacion {
		public Long Validacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaMultifondos {
		public Long Multifondos;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------------------------------------------------

	public ValidacionSalida logica(Connection con, DatosEntrada in) throws Exception {

		logger.info("== INICIO VALIDAR GENERAR ARCHIVO AFP ==");
		logger.debug("ENTRADA > " + in.toString());

		// --------------------------------------------------------------------------------------
		// OBTIENE DATOS APRA AGREGAR O QUITAR CONFIGURACION AFP
		// --------------------------------------------------------------------------------------

		SQL sql = new SQL();
		ValidacionSalida out = new ValidacionSalida();
		HashMap<String, Object> validacionAFP = new HashMap<>();
		HashMap<String, Object> vacio = new HashMap<>();
		validacionAFP.put("IdAFP", in.IdAFP);
		List<SalidaValidacion> validacion = sql.query(con, SQL_VALIDACION_AFP, validacionAFP,SalidaValidacion.class);
		List<SalidaMultifondos> multifondos = sql.query(con, SQL_VALIDACION_MULTIFONDOS, vacio, SalidaMultifondos.class);
		
		if (validacion.get(0).Validacion == 0) {
			
			logger.debug("===:: ERROR - AFP SIN MULTIFONDOS CONFIGURADOS ::==");
			out.P_OUT_MENSAJES.add(new MensajeSalida(new Long(in.IdAFP),
			MensajeSalida.MENSAJE_ERROR, "Configuracion AFP",
			"La AFP no tiene Multifondos Configurados"));
		}else {
			logger.debug("==:: VALIDACION DE ARCHIVO CORRECTA ::==");
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.debug("SALIDA > " + out.toString());
		logger.info("== TERMINO VALIDAR GENERAR ARCHIVO AFP ==");
		return out;

	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	// -------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		DatosEntrada in = gson.fromJson(jin.toString(), DatosEntrada.class);
		ValidacionSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
//			String jsonDelete 	= "{\"tipo\":\"DELETE\",\"p_EnvioDelete\":[{\"IdConfiguracionAFP\":2}]}";
			String jsonAdd 		= "{\"tipo\":\"ADD\",\"p_EnvioAdd\":[{\"IdEmpresa\":1,\"IdPortafolio\":1451748,\"IdCliente\":130823,\"IdAFP\":3,\"IdMultifondo\":3,\"FechaIngreso\":\"2020-02-25\"}]}";
			String AFP = "{IdAFP: 3}";
			DatosEntrada Objin = gson.fromJson(AFP, DatosEntrada.class);
			GenerarArchivoAFP demo = new GenerarArchivoAFP();
			try {
				ValidacionSalida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
 				con.rollback();
 				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.commit();
			}
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
