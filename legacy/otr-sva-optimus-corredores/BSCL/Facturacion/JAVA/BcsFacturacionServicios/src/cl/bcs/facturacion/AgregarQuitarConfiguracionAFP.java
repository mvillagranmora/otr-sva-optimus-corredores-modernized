package cl.bcs.facturacion;

import java.sql.Connection;
import java.sql.Date;
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

public class AgregarQuitarConfiguracionAFP implements JavaCall {

	// Log
	static final Logger logger = LogManager.getLogger(AgregarQuitarConfiguracionAFP.class);

	// Queries
	private final String SQL_INSERT_COFIGURACION_AFP = loadResourceToString(
			"cl/bcs/facturacion/sql/AgregarQuitar_AgregarConfiguracion.sql");
	private final String SQL_DELETE_COFIGURACION_AFP = loadResourceToString(
			"cl/bcs/facturacion/sql/AgregarQuitar_QuitarConfiguracion.sql");
	private final String SQL_VALIDACION_AFP = loadResourceToString(
			"cl/bcs/facturacion/sql/AgregarQuitar_ValidarAFP.sql");
	private final String SQL_VALIDACION_CLIENTE = loadResourceToString(
			"cl/bcs/facturacion/sql/AgregarQuitar_ValidarCliente.sql");
	private final String SQL_VALIDACION_CLIENTE_AFP = loadResourceToString(
			"cl/bcs/facturacion/sql/AgregarQuitar_ValidarClienteAFP.sql");
	private final String SQL_VALIDACION_CLIENTE_AFP_MULTIFONDO = loadResourceToString(
			"cl/bcs/facturacion/sql/AgregarQuitar_ValidarClienteAFPMultifondo.sql");
	private final String SQL_VALIDACION_CLIENTE_AFP_MULTIFONDO_PORTAFOLIO = loadResourceToString(
			"cl/bcs/facturacion/sql/AgregarQuitar_ValidarClienteAFPMultifondoPortafolio.sql");

	// -------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------------------------------------------------

	public static class DatosEntrada {
		public String tipo;

		public List<AgregarConfiguracionAFPEntrada> p_EnvioAdd;

		public List<QuitarConfiguracionAFPEntrada> p_EnvioDelete;

		public DatosEntrada() {
			p_EnvioAdd = new ArrayList<AgregarConfiguracionAFPEntrada>();
			p_EnvioDelete = new ArrayList<QuitarConfiguracionAFPEntrada>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AgregarConfiguracionAFPEntrada {
		public int IdEmpresa;
		public int IdPortafolio;
		public int IdCliente;
		public int IdAFP;
		public int IdMultifondo;
		public Date FechaIngreso;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class QuitarConfiguracionAFPEntrada {
		public int IdConfiguracionAFP;

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

		public ValidacionSalida() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
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

	// -------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------------------------------------------------

	public ValidacionSalida logica(Connection con, DatosEntrada in) throws Exception {

		logger.info("== INICIO AGREGAR QUITAR CONFIGURACION AFP ==");
		logger.debug("ENTRADA > " + in.toString());

		// --------------------------------------------------------------------------------------
		// OBTIENE DATOS APRA AGREGAR O QUITAR CONFIGURACION AFP
		// --------------------------------------------------------------------------------------

		SQL sql = new SQL();
		ValidacionSalida out = new ValidacionSalida();
		HashMap<String, Object> validacionAFP = new HashMap<>();
		HashMap<String, Object> validacionCliente = new HashMap<>();
		HashMap<String, Object> validacionClienteAFP = new HashMap<>();
		HashMap<String, Object> validacionClienteAFPMultifondo = new HashMap<>();
		HashMap<String, Object> validacionClienteAFPMultifondoPortafolio = new HashMap<>();
		if (in.tipo.equalsIgnoreCase("ADD")) {
			logger.debug("==:: AGREGAR CONFIGURACION AFP ::==");
			validacionCliente.put("IdCliente", in.p_EnvioAdd.get(0).IdCliente);
			List<SalidaValidacion> validacion = sql.query(con, SQL_VALIDACION_CLIENTE, validacionCliente,
					SalidaValidacion.class);
			if (validacion.get(0).Validacion == 0) {
				validacionAFP.put("IdAFP", in.p_EnvioAdd.get(0).IdAFP);
				validacionAFP.put("IdMultiFondo", in.p_EnvioAdd.get(0).IdMultifondo);
				validacion = sql.query(con, SQL_VALIDACION_AFP, validacionAFP,
						SalidaValidacion.class);
				if(validacion.get(0).Validacion == 0) {
					sql.batch(con, SQL_INSERT_COFIGURACION_AFP, in.p_EnvioAdd, AgregarConfiguracionAFPEntrada.class);
					logger.debug("==:: CONFIGURACION AFP AGREGADA ::==");
				}else {
					logger.debug("===:: ERROR - AFP DUPLICADA ::==");
					out.P_OUT_MENSAJES.add(new MensajeSalida(new Long(in.p_EnvioAdd.get(0).IdCliente),
					MensajeSalida.MENSAJE_ERROR, "Configuracion AFP",
					"La AFP ya esta Configurada"));
				}
			} else {		
				validacionClienteAFP.put("IdCliente", in.p_EnvioAdd.get(0).IdCliente);
				validacionClienteAFP.put("IdAFP", in.p_EnvioAdd.get(0).IdAFP);
				validacion = sql.query(con, SQL_VALIDACION_CLIENTE_AFP, validacionClienteAFP,
						SalidaValidacion.class);
				if(validacion.get(0).Validacion != 0) {
					//inserta
					validacionClienteAFPMultifondo.put("IdCliente", in.p_EnvioAdd.get(0).IdCliente);
					validacionClienteAFPMultifondo.put("IdAFP", in.p_EnvioAdd.get(0).IdAFP);
					validacionClienteAFPMultifondo.put("IdMultifondo", in.p_EnvioAdd.get(0).IdMultifondo);
					validacion = sql.query(con, SQL_VALIDACION_CLIENTE_AFP_MULTIFONDO, validacionClienteAFPMultifondo,
							SalidaValidacion.class);
					if(validacion.get(0).Validacion == 0) {
						validacionClienteAFPMultifondoPortafolio.put("IdCliente", in.p_EnvioAdd.get(0).IdCliente);
						validacionClienteAFPMultifondoPortafolio.put("IdAFP", in.p_EnvioAdd.get(0).IdAFP);
						validacionClienteAFPMultifondoPortafolio.put("IdPortafolio", in.p_EnvioAdd.get(0).IdPortafolio);
						validacionClienteAFPMultifondoPortafolio.put("IdMultiFondo", in.p_EnvioAdd.get(0).IdMultifondo);
						validacion = sql.query(con, SQL_VALIDACION_CLIENTE_AFP_MULTIFONDO_PORTAFOLIO, validacionClienteAFPMultifondoPortafolio,
								SalidaValidacion.class);
						if(validacion.get(0).Validacion == 0) {
							sql.batch(con, SQL_INSERT_COFIGURACION_AFP, in.p_EnvioAdd, AgregarConfiguracionAFPEntrada.class);
							logger.debug("==:: CONFIGURACION MULTIFONDO AGREGADO ::==");
						}else {
							logger.debug("===:: ERROR - CLIENTE CON PORTAFOLIO DUPLICADO ::==");
							out.P_OUT_MENSAJES.add(new MensajeSalida(new Long(in.p_EnvioAdd.get(0).IdCliente),
							MensajeSalida.MENSAJE_ERROR, "Configuracion AFP",
							"Cliente Numero " + in.p_EnvioAdd.get(0).IdCliente + " ya tiene el Portafolio Configurado"));
						}
					}else {
						logger.debug("===:: ERROR - CLIENTE CON MULTIFONDO DUPLICADO ::==");
						out.P_OUT_MENSAJES.add(new MensajeSalida(new Long(in.p_EnvioAdd.get(0).IdCliente),
						MensajeSalida.MENSAJE_ERROR, "Configuracion AFP",
						"Cliente Numero " + in.p_EnvioAdd.get(0).IdCliente + " ya tiene el Multifondo Configurado"));
					}
				}else {
					logger.debug("===:: ERROR - CLIENTE CON AFP DUPLICADA ::==");
					out.P_OUT_MENSAJES.add(new MensajeSalida(new Long(in.p_EnvioAdd.get(0).IdCliente),
					MensajeSalida.MENSAJE_ERROR, "Configuracion AFP",
					"Cliente Numero " + in.p_EnvioAdd.get(0).IdCliente + " ya tiene AFP Configurada"));
					
				}
			}

		} else {
			logger.debug("===========:: QUITAR CONFIGURACION AFP ::===========");
			sql.batch(con, SQL_DELETE_COFIGURACION_AFP, in.p_EnvioDelete, QuitarConfiguracionAFPEntrada.class);
		}

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.debug("SALIDA > " + out.toString());
		logger.info("== TERMINO AGREGAR QUITAR CONFIGURACION AFP ==");
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
			DatosEntrada Objin = gson.fromJson(jsonAdd, DatosEntrada.class);
			AgregarQuitarConfiguracionAFP demo = new AgregarQuitarConfiguracionAFP();
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
