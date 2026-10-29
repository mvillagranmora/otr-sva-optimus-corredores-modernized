package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.socionegocio.soc.pojo.ResumenPersona;
import cl.bcs.socionegocio.soc.pojo.ResumenUsuario;
import cl.bcs.socionegocio.doc.pojo.FirmaDocumento;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.socionegocio.pojo.Validacion;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class ModificarFirmaDocumento implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarFirmaDocumento.class);
	private final String SQL_UPDATE_MODIFICAR_FECHA_FIRMA = loadResourceToString("cl/bcs/socionegocio/doc/sql/GestionarFirmasDocumento_UpdateFechaFirmaRepresentanteDocumentoAnexo.sql");
	private final String SQL_SELECT_CANALES_DOCUMENTO 	= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarFirmaCanalesDocumento.sql");

	public static class EntradaModificarFirmaDocumento extends BcsPojo { 
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public java.sql.Timestamp p_FechaFirma;
		public Long p_IdAnexo;

	}
	public static class SalidaModificarFirmaDocumento extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;

		public SalidaModificarFirmaDocumento() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<>();
		}

		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}

		public void agregarValidacionSalida(Validacion validacion) {
			if (validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
			}

			this.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarFirmaDocumento entrada = gson.fromJson(in, EntradaModificarFirmaDocumento.class);
		SalidaModificarFirmaDocumento salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	private SalidaModificarFirmaDocumento logica(Connection conn, EntradaModificarFirmaDocumento entrada) throws Exception {
		logger.info("INICIO::ModificarFirmaDocumento");
		long inicio = System.currentTimeMillis();

		SalidaModificarFirmaDocumento salida = new SalidaModificarFirmaDocumento();
		// DATA VALIDAR AGENTE
		ResumenUsuario resumenUsuario = SocioNegocioUtils.obtenerResumenUsuario(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		ResumenDocumentoAnexo resumenDocumentoAnexo = SocioNegocioUtils.obtenerResumenDocumentoAnexoPorIdAnexo(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdAnexo)))
				.getOrDefault(entrada.p_IdAnexo, new ResumenDocumentoAnexo());
		ResumenPersona resumenPersonaAgenteCliente = SocioNegocioUtils.obtenerResumenAgenteCliente(conn, resumenDocumentoAnexo.IdCliente);

		String firmaSiNoEsAgenteDelCliente = getParametroFirmaSiNoEsAgenteDelCliente(conn,entrada.p_IdEmpresa);
		// VALIDACIONES
		salida.agregarValidacionesSalida(validarCanalIngreso(conn, entrada.p_IdEmpresa, entrada.p_IdAnexo, entrada.p_IdTipoCanalIngreso));
		salida.agregarValidacionesSalida(validarEstado(conn, entrada.p_IdEmpresa, resumenDocumentoAnexo));
		salida.agregarValidacionesSalida(validarAgente(conn, entrada.p_IdEmpresa, resumenUsuario, resumenPersonaAgenteCliente, firmaSiNoEsAgenteDelCliente));
		salida.agregarValidacionesSalida(validarFechaFirma(conn, entrada.p_IdEmpresa, entrada.p_FechaFirma));
		
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}
		//MODIFICAR FECHA.
		fechaModificar(conn, entrada);

		if(salida.P_OUT_MENSAJES.isEmpty() && salida.P_OUT_ERRORESCONTADOR == 0) {
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_OK, "UI_FECHAFIRMA_MODIFICAR", new HashMap<>(), conn, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ModificarFirmaDocumento", fin - inicio);

		return salida;
	}
	private List<Validacion> validarCanalIngreso(Connection conn, Long IdEmpresa, Long IdAnexo,  String IdTipoCanalIngreso) throws Exception {

		logger.debug("INICIO::ModificarFirmaDocumento.validarCanalIngreso");
		long inicio = System.currentTimeMillis();
		List<Validacion> validaciones = new ArrayList<>();
		try {
    	SQL sql = new SQL();
    	HashMap<String,Object> paramsIN = new HashMap<>();
    	paramsIN.put("AnexoDocumento", IdAnexo ); 
    	paramsIN.put("Canal", IdTipoCanalIngreso); 
    	if (!IdTipoCanalIngreso.equals("OPT") && sql.countQuery(conn, SQL_SELECT_CANALES_DOCUMENTO, paramsIN)==0) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CANAL_NO_PERMITE_INGRESADO", new HashMap<>(), conn, IdEmpresa);
			validaciones.add(validacion);

    	}
    }catch (Exception e) {
    	System.out.println(e);
    }   
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFirmaDocumento.validarCanalIngreso", fin - inicio);

		return validaciones;
	}
	private List<Validacion> validarFechaFirma(Connection conn, Long idEmpresa, java.sql.Timestamp fecha) throws Exception {
		logger.debug("INICIO::ModificarFirmaDocumento.validarFechaFirma");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();
		
		long fechaMenor = FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa).getTime();
		long fechaMayor = fecha.getTime();
		fechaMayor = (long) (Math.floor(fechaMayor/(1000 * 60 * 60 * 24)));
		fechaMenor = (long) (Math.floor(fechaMenor/(1000 * 60 * 60 * 24)));
		long diferenciaEn_Dias = fechaMayor - fechaMenor;
		if (diferenciaEn_Dias > 0) {
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_FECHA_FIRMA_MAYOR_SISTEMA", new HashMap<>(), conn, idEmpresa);
			validaciones.add(validacion);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFirmaDocumento.validarFechaFirma", fin - inicio);

		return validaciones;
	}
	private List<Validacion> validarAgente(Connection conn, Long idEmpresa, ResumenUsuario resumenAgenteEntrante, ResumenPersona resumenAgente, String firmaSiNoEsAgenteDelCliente) throws Exception {
		logger.debug("INICIO::ModificarFirmaDocumento.validarAgente");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();

		boolean noEsAgenteCliente = resumenAgenteEntrante.IdPersona == null || !Objects.equals(resumenAgenteEntrante.IdPersona, resumenAgente.IdPersona);
		
		if (noEsAgenteCliente && firmaSiNoEsAgenteDelCliente.equalsIgnoreCase("N") ) {
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_USUARIO_NO_ES_AGENTE_DEL_CLIENTE", new HashMap<>(), conn, idEmpresa);
			validaciones.add(validacion);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFirmaDocumento.validarAgente", fin - inicio);

		return validaciones;
	}
	private List<Validacion> validarEstado(Connection conn, Long idEmpresa, ResumenDocumentoAnexo resumenAnexo) throws Exception {
		logger.debug("INICIO::ModificarFirmaDocumento.validarFirma");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();

		if (!resumenAnexo.IdEstadoAnexo.equals("ACT")) {
			Validacion validacion = new Validacion();
			validacion.EsError = true;
			validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NOFIRMA_DOCUMENTO", new HashMap<>(), conn, idEmpresa);
			validaciones.add(validacion);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFirmaDocumento.validarFirma", fin - inicio);

		return validaciones;
	}
	private String getParametroFirmaSiNoEsAgenteDelCliente(Connection conn, Long idEmpresa) {
		String firmaSiNoEsAgenteDelCliente = "N";	
			try {
				firmaSiNoEsAgenteDelCliente = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "SOCIODENEGOCIO", "DOC.FirmaTambienSiNoEsAgenteDelCliente");
			}		catch (Exception e) {
				e.printStackTrace();
				firmaSiNoEsAgenteDelCliente="N";
			};
			return firmaSiNoEsAgenteDelCliente;
		}
	private void fechaModificar(Connection conn, EntradaModificarFirmaDocumento entrada) throws SQLException {
		logger.debug("INICIO::ModificarFirmaDocumento.fechaModificar");
		long inicio = System.currentTimeMillis();

		FirmaDocumento fechaModificar = new FirmaDocumento();
		fechaModificar.IdAnexo = entrada.p_IdAnexo;
		fechaModificar.FechaFirma = entrada.p_FechaFirma;
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_MODIFICAR_FECHA_FIRMA, new ArrayList<>(Collections.singletonList(fechaModificar)), FirmaDocumento.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFirmaDocumento.fechaModificar", fin - inicio);
	}

	public static void main(String[] args) {
		System.out.println("Inicio prueba");

		EntradaModificarFirmaDocumento entrada = new EntradaModificarFirmaDocumento();
		try (Connection conn = PlataformaUtils.getDESAConnection()) {

			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 6,\r\n"
					+ "    \"p_IdModulo\": \"SOC\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "    \"p_IdTipoCanalIngreso\": \"OPT\",\r\n"
					+ "    \"p_FechaFirma\": \"2023-06-26 04:00:00\",\r\n"
					+ "    \"p_IdAnexo\": 5383\r\n"
					+ "}";

			Gson gson = Utils.getGsonBuilder();
			entrada = gson.fromJson(json, EntradaModificarFirmaDocumento.class);

			System.out.println(entrada);

			try {
				ModificarFirmaDocumento ModificarFirmaDocumento = new ModificarFirmaDocumento();
				SalidaModificarFirmaDocumento salida = ModificarFirmaDocumento.logica(conn, entrada);
				System.out.println(salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
				//conn.commit();
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("Fin prueba");
	}
}
