package cl.bcs.custodia;

/**
 * Objetivo: Servicio que recibe el usuario, el terminal y una lista de solicitudes de ejercicio, las cuales se van a validar y, en caso de estar correctas, 
 * actualizan las opciones de ejercicio del dia.   
 */
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresarEjercicio.Ejercicio;
import cl.bcs.custodia.IngresarEjercicio.Entrada_IngresarEjercicio;
import cl.bcs.custodia.IngresarEjercicio.Salida_IngresarEjercicio;
import cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo.EjercicioBloqueo;
import cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo.EjercicioBloqueoResponse;
import cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo.EjercicioBloqueoServiceService;
import cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo.PtdeConsultaejerciciosEjercicioBloqueoService;
import cl.bcs.custodia.utils.ClientWS.periodoEjercicioBloqueo.PtdeSorteoEstadoyhorasolicitudejercicioResponse;
import cl.bcs.custodia.utils.ClientWS.periodoEjercicioBloqueo.PtdeSorteoEstadoyhorasolicitudejercicioService;
import cl.bcs.custodia.utils.ClientWS.periodoEjercicioBloqueo.PtdeSorteoEstadoyhorasolicitudejercicioServiceService;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarEjercicioBloqueo implements JavaCall {

	private final Log log = new Log("CUS.EjercicioBloqueo");// cambiar por log4j (crearOrdenes)
	private final String SQL_SELECT_DATOS_CORREDORA = loadResourceToString(
			"cl/bcs/custodia/sql/ConsultarSorteoContratos_SelectDatosCorredora.sql");

	// --------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------
	// clases que son utilizadas unicamente en este java deben ser private y no
	// estaticas
	public static class EntradaConsultarEjercicioBloqueo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
//		public String 						p_Terminal;
		public List<cEjercicioBloqueo> p_ConsultarEjercicioBloqueo;

		public EntradaConsultarEjercicioBloqueo() {
			p_ConsultarEjercicioBloqueo = new ArrayList<cEjercicioBloqueo>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	private class cEjercicioBloqueo {
//Tabla "BSCL.Custodia.BD::DER.OpcionesEjercicioBloqueo"		
		private Long p_IdCartera;
		private Long p_FolioCartera;
		private Long p_ContratosSorteados;
		private Long p_IdCliente;
		private Long p_IdPortafolio;

// Envio		
		private String p_Bloqueo;
		private BigDecimal p_CantEjercicio;
		private String p_Comitente;
		private String p_FondoAgente;
		private String p_FondoComitente;
		private String p_Serie;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Horario {
		public String HoraInicio;
		public String HoraFin;
		public String Estado;

	}

	public static class Carteras {
		private Long IdCartera;
		private String Descripcion;

	}

	// --------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------

	public class SalidaConsultarEjercicioBloqueo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Horario P_OUT_HORARIO;
		public List<Carteras> P_OUT_CARTERAS;

		public SalidaConsultarEjercicioBloqueo() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_HORARIO = new Horario();
			this.P_OUT_CARTERAS = new ArrayList<Carteras>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// --------------------------------------------------------
	// CLASES DE PROCESO
	// --------------------------------------------------------

	private class ConexionWSEjercicioBloqueo {
		private String p_wsdl;
		private String p_nameSpace;
		private String p_localPart;
		private String p_usuario;
		private String p_pass;
		private String p_fechaSistema;

		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	public static class Empresa {
		public String CodigoCorredora;
		public Long IdEmpresa;
		public String Codigo;
		public String Nombre;
		public Long IdPais;
		public Long IdSocioDeNegocio;
		public Long IdCorredora;
		public Long IdPersona;
	}

	private static final Logger logger = LogManager.getLogger(ConsultarEjercicioBloqueo.class);

	// --------------------------------------------------------
	// LOGICA
	// --------------------------------------------------------
	// usar prefijo entrada y salida
	public SalidaConsultarEjercicioBloqueo logica(Connection con, EntradaConsultarEjercicioBloqueo in)
			throws Exception {
		logger.info("INICIO ENVIO OPERACIONES PENDIENTES");

		log.write("ENTRADA > " + in.toString());
		SalidaConsultarEjercicioBloqueo out = sendConsultarEjercicioBloqueo(con, in);
		logger.info("TERMINO CONSULTA BLOQUEO EJERICIO"); 
		return out;
	}

	// --------------------------------------------------------
	// METODOS
	// --------------------------------------------------------
	public SalidaConsultarEjercicioBloqueo sendConsultarEjercicioBloqueo(Connection con,
			EntradaConsultarEjercicioBloqueo entrada) throws Exception {

		HashMap<String, String> mapMensageSalida = new HashMap<>();

		SalidaConsultarEjercicioBloqueo salida = new SalidaConsultarEjercicioBloqueo();
		Entrada_IngresarEjercicio inEjercicio = new Entrada_IngresarEjercicio();

		// ----------------------------------------------------------
		// OBTIENE PARAMETROS DE CONEXION A WS HT
		ConexionWSEjercicioBloqueo conexion = new ConexionWSEjercicioBloqueo();
		conexion.p_usuario = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
				"PLATAFORMA", "DER.EnvioEjercicioDerivados.Usuario");
		conexion.p_pass = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA",
				"DER.EnvioEjercicioDerivados.Password");

		// CONEXION WS PARA CONSULTAR HORARIO
		conexion.p_wsdl = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA",
				"DER.PeriodoEjercicioDerivados.Wsdl");
		conexion.p_nameSpace = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
				"PLATAFORMA", "DER.PeriodoEjercicioDerivados.NameSpace");
		conexion.p_localPart = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
				"PLATAFORMA", "DER.PeriodoEjercicioDerivados.LocalPart");
		conexion.p_fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
				"PLATAFORMA", "PLA.FechaSistema");
		log.write("PARAMETROS CONEXION WS > " + conexion.toString());

		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdBolsa", "BCS");
		List<Empresa> empresa = sql.query(con, SQL_SELECT_DATOS_CORREDORA, params, Empresa.class);

		String codigoCorredora = "";

		if (!empresa.isEmpty()) {
			codigoCorredora = empresa.get(0).CodigoCorredora;
		}

		try {

			PtdeSorteoEstadoyhorasolicitudejercicioServiceService wsClientperiodoEjercicioBloqueo = new PtdeSorteoEstadoyhorasolicitudejercicioServiceService(
					new URL(conexion.p_wsdl), new QName(conexion.p_nameSpace, conexion.p_localPart));
			PtdeSorteoEstadoyhorasolicitudejercicioService clientperiodoEjercicioBloqueo = wsClientperiodoEjercicioBloqueo
					.getPtdeSorteoEstadoyhorasolicitudejercicioServicePort();

			((BindingProvider) clientperiodoEjercicioBloqueo).getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
					conexion.p_usuario);
			((BindingProvider) clientperiodoEjercicioBloqueo).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
					conexion.p_pass);

			PtdeSorteoEstadoyhorasolicitudejercicioResponse listDataWSEclientperiodoEjercicioBloqueo = clientperiodoEjercicioBloqueo
					.ptdeSorteoEstadoyhorasolicitudejercicioExecute();

			System.out.println("Hora Inicio : "
					+ listDataWSEclientperiodoEjercicioBloqueo.getXstatulogResult().get(0).getHORAINI());
			System.out.println(
					"Hora Fin : " + listDataWSEclientperiodoEjercicioBloqueo.getXstatulogResult().get(0).getHORAFIN());
			System.out.println("Estado : "
					+ listDataWSEclientperiodoEjercicioBloqueo.getXstatulogResult().get(0).getXStatusLogs());

			// RESPUESTA ESTADO Y HORARIO

			salida.P_OUT_HORARIO.HoraInicio = listDataWSEclientperiodoEjercicioBloqueo.getXstatulogResult().get(0)
					.getHORAINI();
			salida.P_OUT_HORARIO.HoraFin = listDataWSEclientperiodoEjercicioBloqueo.getXstatulogResult().get(0)
					.getHORAFIN();
			salida.P_OUT_HORARIO.Estado = listDataWSEclientperiodoEjercicioBloqueo.getXstatulogResult().get(0)
					.getXStatusLogs();

			if (listDataWSEclientperiodoEjercicioBloqueo.getXstatulogResult().get(0).getXStatusLogs().equals("SI")) {

				// ----------------------------------------------------------
				// CONEXION WS PARA ENVIO DE EJERCICIOS
				// ----------------------------------------------------------
				conexion.p_wsdl = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
						"PLATAFORMA", "DER.EnvioEjercicioDerivados.Wsdl");
				conexion.p_nameSpace = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
						"PLATAFORMA", "DER.EnvioEjercicioDerivados.NameSpace");
				conexion.p_localPart = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
						"PLATAFORMA", "DER.EnvioEjercicioDerivados.LocalPart");

				// ----------------------------------------------------------
				// ARMA DATA PARA ENVIO Y OBTIENE EJERCICIOS SELECCIONADOS
				// ----------------------------------------------------------

				List<EjercicioBloqueo> listEjercicioBloqueo = new ArrayList<EjercicioBloqueo>();

				for (cEjercicioBloqueo ejercicioBloqueo : entrada.p_ConsultarEjercicioBloqueo) {
					EjercicioBloqueo detalleEjercicioBloqueo = new EjercicioBloqueo();
					detalleEjercicioBloqueo.setAgente(codigoCorredora);
					detalleEjercicioBloqueo.setBloqueo(ejercicioBloqueo.p_Bloqueo);
					detalleEjercicioBloqueo.setCantEjercicio(ejercicioBloqueo.p_CantEjercicio);
					detalleEjercicioBloqueo.setComitente(ejercicioBloqueo.p_Comitente);
					detalleEjercicioBloqueo.setFondoComitente(ejercicioBloqueo.p_FondoComitente);
					detalleEjercicioBloqueo.setFondoAgente(ejercicioBloqueo.p_FondoAgente);
					detalleEjercicioBloqueo.setSerie(ejercicioBloqueo.p_Serie);
					listEjercicioBloqueo.add(detalleEjercicioBloqueo);
				}

				System.out.println(listEjercicioBloqueo);

				if (!listEjercicioBloqueo.isEmpty()) {

					EjercicioBloqueoServiceService wsClientEjercicioBloqueo = new EjercicioBloqueoServiceService(
							new URL(conexion.p_wsdl), new QName(conexion.p_nameSpace, conexion.p_localPart));
					PtdeConsultaejerciciosEjercicioBloqueoService clientEjercicioBloqueo = wsClientEjercicioBloqueo
							.getPtdeConsultaejerciciosEjercicioBloqueoServicePort();

					((BindingProvider) clientEjercicioBloqueo).getRequestContext()
							.put(BindingProvider.USERNAME_PROPERTY, conexion.p_usuario);
					((BindingProvider) clientEjercicioBloqueo).getRequestContext()
							.put(BindingProvider.PASSWORD_PROPERTY, conexion.p_pass);

					EjercicioBloqueoResponse listDataWSEjercicioBloqueo = clientEjercicioBloqueo
							.ptdeConsultaejerciciosEjercicioBloqueoExecute(entrada.p_IdUsuario.toString(),
									listEjercicioBloqueo);

					List<Carteras> lisCarteras = new ArrayList<Carteras>();

					for (int j = 0; j < listDataWSEjercicioBloqueo.getReporte().size(); j++) {

						Carteras typeCarteras = new Carteras();
						Ejercicio datoEjericio = new Ejercicio();
						
						mapMensageSalida.put("FolioCartera", entrada.p_ConsultarEjercicioBloqueo.get(j).p_FolioCartera.toString());
						mapMensageSalida.put("Serie", entrada.p_ConsultarEjercicioBloqueo.get(j).p_Serie);
						mapMensageSalida.put("Descripcion", listDataWSEjercicioBloqueo.getReporte().get(j).getEstadoIngreso());

						if (listDataWSEjercicioBloqueo.getReporte().get(j).getCodigoRespuesta().equals(0)) {

							salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_OK, "SRV_DER_OPERACION_ENVIADA_CORRECTAMENTE", mapMensageSalida, con, entrada.p_IdEmpresa));
							datoEjericio.IdEstado = "ENV";
							datoEjericio.IdEstadoCCLV = listDataWSEjercicioBloqueo.getReporte().get(j).getEstadoIngreso();
						}

						else

						{
							salida.P_OUT_ERRORESCONTADOR = 1L;
							salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR,"SRV_DER_OPERACION_NO_PUDO_SER_ENVIADA", mapMensageSalida, con,entrada.p_IdEmpresa));							
							salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
							datoEjericio.IdEstadoCCLV = listDataWSEjercicioBloqueo.getReporte().get(j).getEstadoIngreso();
							datoEjericio.IdEstado = "ERR";
						}
						;

						
						
						typeCarteras.IdCartera = entrada.p_ConsultarEjercicioBloqueo.get(j).p_IdCartera;
						typeCarteras.Descripcion = listDataWSEjercicioBloqueo.getReporte().get(j).getEstadoIngreso();
						lisCarteras.add(typeCarteras);
						salida.P_OUT_CARTERAS.add(typeCarteras);

						datoEjericio.DescripcionEstadoCCLV = listDataWSEjercicioBloqueo.getReporte().get(j)
								.getEstadoIngreso();
						datoEjericio.IdCartera = entrada.p_ConsultarEjercicioBloqueo.get(j).p_IdCartera;
						datoEjericio.Bloqueado = entrada.p_ConsultarEjercicioBloqueo.get(j).p_Bloqueo;
						datoEjericio.ContratosEjercidos = entrada.p_ConsultarEjercicioBloqueo.get(j).p_CantEjercicio
								.longValue();
						datoEjericio.ContratosSorteados = entrada.p_ConsultarEjercicioBloqueo
								.get(j).p_ContratosSorteados;
						datoEjericio.IdCliente = entrada.p_ConsultarEjercicioBloqueo.get(j).p_IdCliente;
						datoEjericio.IdPortafolio = entrada.p_ConsultarEjercicioBloqueo.get(j).p_IdPortafolio;
						datoEjericio.IdEmpresa = entrada.p_IdEmpresa;
						inEjercicio.p_Ejercicios.add(datoEjericio);

					}

					IngresarEjercicio ingEje = new IngresarEjercicio();
					inEjercicio.p_IdEmpresa = entrada.p_IdEmpresa;
					inEjercicio.p_IdPais = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa,
							"PLATAFORMA", "PLA.PaisSistema");
					inEjercicio.p_IdUsuario = entrada.p_IdUsuario;

					Salida_IngresarEjercicio salEjer = ingEje.logica(con, inEjercicio);
					System.out.println(salEjer);

				}
			}

			else

			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
						"SRV_DER_ENVIO_FUERA_DE_HORARIO", mapMensageSalida, con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"SRV_DER_EXISTEN_ERRORES_CON_EL_ENVIO", mapMensageSalida, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		}

		return salida;

	};

	// --------------------------------------------------------
	// CALL
	// --------------------------------------------------------

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder(); // NEW
		EntradaConsultarEjercicioBloqueo in = gson.fromJson(jin.toString(), EntradaConsultarEjercicioBloqueo.class);

		SalidaConsultarEjercicioBloqueo out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// --------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------------

	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try {
			Connection con = PlataformaUtils.getDESAConnection();

			Gson gson = Utils.getGsonBuilder();
// 2 Registros Envio			
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":232,\"p_IdModulo\":\"CUS\",\"p_Terminal\":\"\",\"p_ConsultarEjercicioBloqueo\":[{\"p_Bloqueo\":\"N\",\"p_CantEjercicio\":\"15\",\"p_Comitente\":\"10454333-2\",\"p_FondoAgente\":\"\",\"p_FondoComitente\":\"\",\"p_Serie\":\"DOL17X0650\",\"p_IdCartera\":61, \"p_ContratosSorteados\":\"15\", \"p_IdCliente\":123569, \"p_IdPortafolio\":1},{\"p_Bloqueo\":\"N\",\"p_CantEjercicio\":\"15\",\"p_Comitente\":\"10454333-2\",\"p_FondoAgente\":\"\",\"p_FondoComitente\":\"\",\"p_Serie\":\"DOL17X0650\",\"p_IdCartera\":62, \"p_ContratosSorteados\":\"15\", \"p_IdCliente\":123569, \"p_IdPortafolio\":1}]}";

// 1 Registro Envio			
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":232,\"p_IdModulo\":\"CUS\",\"p_Terminal\":\"\",\"p_ConsultarEjercicioBloqueo\":[{\"p_Bloqueo\":\"N\",\"p_CantEjercicio\":\"15\",\"p_Comitente\":\"10454333-2\",\"p_FondoAgente\":\"\",\"p_FondoComitente\":\"\",\"p_Serie\":\"DOL17X0650\",\"p_IdCartera\":61, \"p_ContratosSorteados\":\"15\", \"p_IdCliente\":123569, \"p_IdPortafolio\":1}]}";

// Consulta
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":232,\"p_IdModulo\":\"CUS\",\"p_Terminal\":\"\",\"p_ConsultarEjercicioBloqueo\":[]}";
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_Opcion\":\"A\",\"p_IdModulo\":\"CUS\",\"p_Terminal\":\"\",\"p_ConsultarEjercicioBloqueo\":[{\"p_IdCartera\":1530339,\"p_ContratosSorteados\":0,\"p_IdCliente\":123569,\"p_IdPortafolio\":27499,\"p_Bloqueo\":\"N\",\"p_CantEjercicio\":5,\"p_Comitente\":\"333-6\",\"p_FondoAgente\":\"\",\"p_FondoComitente\":\"\",\"p_Serie\":\"COP20B8210\"},{\"p_IdCartera\":1530607,\"p_ContratosSorteados\":0,\"p_IdCliente\":123569,\"p_IdPortafolio\":27499,\"p_Bloqueo\":\"S\",\"p_CantEjercicio\":0,\"p_Comitente\":\"333-6\",\"p_FondoAgente\":\"\",\"p_FondoComitente\":\"\",\"p_Serie\":\"AES20B0150\"},{\"p_IdCartera\":1530352,\"p_ContratosSorteados\":0,\"p_IdCliente\":123569,\"p_IdPortafolio\":27499,\"p_Bloqueo\":\"S\",\"p_CantEjercicio\":0,\"p_Comitente\":\"333-6\",\"p_FondoAgente\":\"\",\"p_FondoComitente\":\"\",\"p_Serie\":\"AES20B0150\"},{\"p_IdCartera\":1531343,\"p_ContratosSorteados\":0,\"p_IdCliente\":123569,\"p_IdPortafolio\":27499,\"p_Bloqueo\":\"S\",\"p_CantEjercicio\":0,\"p_Comitente\":\"333-6\",\"p_FondoAgente\":\"\",\"p_FondoComitente\":\"\",\"p_Serie\":\"AES20B0150\"}]}";

			EntradaConsultarEjercicioBloqueo Objin = gson.fromJson(json, EntradaConsultarEjercicioBloqueo.class);
			ConsultarEjercicioBloqueo demo = new ConsultarEjercicioBloqueo();

			try {
				SalidaConsultarEjercicioBloqueo ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
				 con.rollback();
				//con.commit();

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

};
