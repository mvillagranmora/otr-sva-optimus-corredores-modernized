package cl.bcs.socionegocio.soc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.stream.Collectors;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjERRORESDETALLE;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.pojo.DireccionElectronica;
import cl.bcs.socionegocio.soc.pojo.ResumenCorredora;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.socionegocio.soc.pojo.ResumenPersona;
import cl.bcs.socionegocio.soc.pojo.ResumenUsuario;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SocioNegocioUtils {
	static final Logger logger = LogManager.getLogger(SocioNegocioUtils.class);

	private static FileLoader loader;
	private static String SQL_SELECT_DATOS_PERSONA;
	private static String SQL_SELECT_DATOS_PORTAFOLIO;
	private static String SQL_SELECT_DATOS_PERSONA_POR_GRUPO_DIRECCION;
	private static String SQL_SELECT_RESUMENES_ANEXOS;
	private static String SQL_SELECT_RESUMEN_CORREDORA;
	private static String SQL_SELECT_DIRECCIONES_ELECTRONICAS_PERSONAS;
	private static String SQL_SELECT_RESUMEN_USUARIO;
	private static String SQL_SELECT_FIRMANTES_PERSONA;
	private static String SQL_SELECT_RESUMEN_AGENTE_CLIENTE;
	private static String SQL_SELECT_RESUMEN_PERSONA_POR_ID_PERSONA;
	private static String SQL_SELECT_RESUMEN_PERSONA_POR_DOCUMENTO_IDENTIDAD;
	private static String SQL_SELECT_TIPO_SEGMENTO;
	private static String SQL_SELECT_CONFIGURACION_PORTAFOLIO_DEFECTO;
	private static String SQL_SELECT_EMPLEADO_POR_USUARIO;

	static {
		loader = new FileLoader();
		SQL_SELECT_DATOS_PERSONA = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectDatosPersona.sql");
		SQL_SELECT_DATOS_PORTAFOLIO = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectDatosPortafolio.sql");
		SQL_SELECT_DATOS_PERSONA_POR_GRUPO_DIRECCION = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectDatosGrupoDireccionElectronica.sql");
		SQL_SELECT_RESUMENES_ANEXOS = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectResumenAnexo.sql");
		SQL_SELECT_RESUMEN_CORREDORA = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectResumenCorredora.sql");
		SQL_SELECT_DIRECCIONES_ELECTRONICAS_PERSONAS = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectDireccionesElectronicasPersonas.sql");
		SQL_SELECT_RESUMEN_USUARIO = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectResumenUsuario.sql");
		SQL_SELECT_FIRMANTES_PERSONA = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectFirmantesPersona.sql");
		SQL_SELECT_RESUMEN_AGENTE_CLIENTE = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectResumenAgenteCliente.sql");
		SQL_SELECT_RESUMEN_PERSONA_POR_ID_PERSONA = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectResumenPersonaPorIdPersona.sql");
		SQL_SELECT_RESUMEN_PERSONA_POR_DOCUMENTO_IDENTIDAD = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectResumenPersonaPorDocumentoIdentidad.sql");
		SQL_SELECT_TIPO_SEGMENTO = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectTipoSegmento.sql");
		SQL_SELECT_CONFIGURACION_PORTAFOLIO_DEFECTO = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectConfiguracionPortafolioDefecto.sql");
		SQL_SELECT_EMPLEADO_POR_USUARIO = loader.loadResourceToString("cl/bcs/socionegocio/soc/sql/SocioNegocioUtils_SelectEmpleadoPorUsuario.sql");
	}

	public static class Persona{
		public Long IdPersona;
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdPersona;
		public Long IdCliente;
		public String CodigoPortafolio;
		public String Proposito;
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	public static class GrupoDireccionElectronica {
		public Long IdGrupoDireccion;
		public Long IdPersona;
		public Long IdCliente;
		public String PersonaDocumentoIdentidad;
		public String PersonaNombreCompleto;
	}

	public static class ResultadoValidacionRequisitosEnrolamientoFull extends BcsPojo {
		public List<Validacion> validaciones;
		public List<RequisitoEnrolamientoFull> requisitos;

		public ResultadoValidacionRequisitosEnrolamientoFull() {
			validaciones = new ArrayList<>();
			requisitos = new ArrayList<>();
		}
	}

	public static class TipoSegmento extends BcsPojo {
		public Long IdSegmentoCliente;
		public String NombreSegmentoCliente;
		public String DescripcionSegmentoCliente;
	}

	public static class ConfiguracionPortafolioDefecto extends BcsPojo {
		public Long IdPortafolioConfiguracionDefecto;
		public String CodigoPortafolio;
		public String Proposito;
	}
	
	public static class Empleado extends BcsPojo {
		public Long IdUsuario;
		public Long IdEmpleado;
		public String CodigoEmpleado;
	}

	public static <T> T obtenerValorNoNulo(T valor, T valorPorDefecto) {
		T valorNoNulo = valor == null ? valorPorDefecto : valor;
		return valorNoNulo;
	}

	public static Boolean esOperacionConAutorizacion(Connection con, Long idEmpresa, String idConcepto, String usaReglasDeNegocio) {
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, idEmpresa, idConcepto);
		Boolean esOperacionConAutorizacion = "S".equals(requiereAutorizacion) && "S".equals(usaReglasDeNegocio);
		return esOperacionConAutorizacion;
	}

	public static String obtenerIdEstadoAutorizacion(Boolean esOperacionConAutorizacion) {
		String idEstadoAutorizacion = esOperacionConAutorizacion ? "PEN" : "ING";
		return idEstadoAutorizacion;
	}

	public static Map<Long, Persona> obtenerDatosPersonaPorIdPersona(Connection con, List<Long> idsPersona) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdPersona", idsPersona);
		List<Persona> personas = sql.query(con, SQL_SELECT_DATOS_PERSONA, parametros, Persona.class);
		Map<Long, Persona> personasPorIdPersona = personas
				.parallelStream()
				.collect(Collectors.toMap(persona->persona.IdPersona, persona->persona));
		return personasPorIdPersona;
	}

	public static Map<Long, Portafolio> obtenerDatosPortafolioPorIdPortafolio(Connection con, List<Long> idsPortafolio) throws SQLException {
		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>(4);
		parametros.put("IdPortafolio", idsPortafolio);

		List<Portafolio> personas = sql.query(con, SQL_SELECT_DATOS_PORTAFOLIO, parametros, Portafolio.class);

		return personas
				.parallelStream()
				.collect(Collectors.toMap(portafolio -> portafolio.IdPortafolio, portafolio -> portafolio));
	}

	public static Map<Long, GrupoDireccionElectronica> obtenerDatosPersonaPorIdGrupoDireccionElectronica(Connection con, List<Long> idsGrupoDireccionElectronica) throws SQLException {
		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>(4);
		parametros.put("IdGrupoDireccionElectronica", idsGrupoDireccionElectronica);

		List<GrupoDireccionElectronica> personas = sql.query(con, SQL_SELECT_DATOS_PERSONA_POR_GRUPO_DIRECCION, parametros, GrupoDireccionElectronica.class);

		return personas
				.parallelStream()
				.collect(Collectors.toMap(persona -> persona.IdGrupoDireccion, persona -> persona));
	}

	public static List<MensajeSalida> obtenerMensajesAdvertenciaAutorizacion(List<ObjADVERTENCIASDETALLE> advertencias){
		List<MensajeSalida> mensajesAdvertencias = advertencias
				.parallelStream()
				.map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion))
				.collect(Collectors.toList());
		return mensajesAdvertencias;
	}

	public static List<MensajeSalida> obtenerMensajesErroresAutorizacion(List<ObjERRORESDETALLE> errores){
		List<MensajeSalida> mensajesAdvertencias = errores
				.parallelStream()
				.map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion))
				.collect(Collectors.toList());
		return mensajesAdvertencias;
	}

	public static AgregarLog.InAgregarLog.DetalleLog obtenerDetalleLog(Long idEntidad, String campoRegistro, String valorAntiguo, String valorNuevo){
		AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
		detalle.IdCabeceraLog = idEntidad;
		detalle.SubRegistro = String.valueOf(idEntidad);
		detalle.CampoRegistro = campoRegistro;
		detalle.CampoRegistro = obtenerCampoRegistroTruncado(detalle.CampoRegistro);
		detalle.ValorAntiguo = valorAntiguo;
		detalle.ValorNuevo = valorNuevo;
		return detalle;
	}

	private static String obtenerCampoRegistroTruncado(String campoRegistro) {
		campoRegistro = campoRegistro.length() > 40 ? campoRegistro.substring(0, 40) : campoRegistro;
		return campoRegistro;
	}

	public static AgregarLog.InAgregarLog.Log obtenerLogPorDefecto(){
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		return log;
	}

	public static ResumenUsuario obtenerResumenUsuario(Connection conn, Long idEmpresa, String codigoUsuario) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerResumenUsuario");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(8);
		params.put("IdEmpresa", idEmpresa);
		params.put("CodigoUsuario", codigoUsuario);
		params.put("IdUsuario", -1); // Ignorar filtro por IdUsuario

		SQL sql = new SQL();
		ResumenUsuario resumenUsuario = sql.query(conn, SQL_SELECT_RESUMEN_USUARIO, params, ResumenUsuario.class).stream().findFirst().orElse(new ResumenUsuario());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerResumenUsuario", fin - inicio);

		return resumenUsuario;
	}
	public static ResumenUsuario obtenerResumenUsuario(Connection conn, Long idEmpresa, Long idUsuario) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerResumenUsuario");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(8);
		params.put("IdEmpresa", idEmpresa);
		params.put("CodigoUsuario", ""); // Ignorar filtro por IdUsuario
		params.put("IdUsuario", idUsuario);

		SQL sql = new SQL();
		ResumenUsuario resumenUsuario = sql.query(conn, SQL_SELECT_RESUMEN_USUARIO, params, ResumenUsuario.class).stream().findFirst().orElse(new ResumenUsuario());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerResumenUsuario", fin - inicio);

		return resumenUsuario;
	}

	public static ResumenCorredora obtenerResumenCorredora(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerResumenCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		SQL sql = new SQL();
		ResumenCorredora resumenCorredora = sql.query(conn, SQL_SELECT_RESUMEN_CORREDORA, params, ResumenCorredora.class).stream().findFirst().orElse(new ResumenCorredora());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerResumenCorredora", fin - inicio);

		return resumenCorredora;
	}

	public static Map<Long, ResumenDocumentoAnexo> obtenerResumenDocumentoAnexoPorIdAnexo(Connection conn, List<Long> idsAnexos) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerResumenDocumentoAnexoPorIdAnexo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdsAnexos", idsAnexos);

		SQL sql = new SQL();
		List<ResumenDocumentoAnexo> resumenes = sql.query(conn, SQL_SELECT_RESUMENES_ANEXOS, params, ResumenDocumentoAnexo.class);

		Map<Long, ResumenDocumentoAnexo> resumenPorIdAnexo = resumenes
				.stream()
				.collect(Collectors.toMap(resumen -> resumen.IdAnexo, resumen -> resumen));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerResumenDocumentoAnexoPorIdAnexo", fin - inicio);

		return resumenPorIdAnexo;
	}

	public static Map<Long, List<Long>> obtenerDireccionesElectronicasPorIdPersona(Connection conn, List<Long> idsPersonas) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenenerDireccionesElectronicasPorIdPersona");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("PersonasIds", idsPersonas);

		SQL sql = new SQL();
		List<DireccionElectronica> direccionesElectronicas = sql.query(conn, SQL_SELECT_DIRECCIONES_ELECTRONICAS_PERSONAS, params, DireccionElectronica.class);

		Map<Long, List<Long>> direccionesElectronicasPorIdPersona = direccionesElectronicas
				.stream()
				.collect(Collectors.groupingBy(dir -> dir.IdPersona, Collectors.mapping(dir -> dir.IdDireccionElectronica, Collectors.toList())));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenenerDireccionesElectronicasPorIdPersona", fin - inicio);

		return direccionesElectronicasPorIdPersona;
	}

	/**
	 * Obtiene a los relacionados del cliente habilitados para firmar y al cliente mismo.
	 * @param conn
	 * @param idPersona
	 * @return lista con el cliente + relacionados firmantes
	 * @throws SQLException
	 */
	public static List<ResumenPersona> obtenerFirmantesPersona(Connection conn, Long idPersona) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerFirmantesPersona");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdPersona", idPersona);

		SQL sql = new SQL();
		List<ResumenPersona> firmantes = sql.query(conn, SQL_SELECT_FIRMANTES_PERSONA, params, ResumenPersona.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerFirmantesPersona", fin - inicio);

		return firmantes;
	}

	public static ResumenPersona obtenerResumenAgenteCliente(Connection conn, Long idCliente) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerResumenAgenteCliente");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdCliente", idCliente);

		SQL sql = new SQL();
		ResumenPersona resumenAgente = sql.query(conn, SQL_SELECT_RESUMEN_AGENTE_CLIENTE, params, ResumenPersona.class).stream().findFirst().orElse(new ResumenPersona());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerResumenAgenteCliente", fin - inicio);

		return resumenAgente;
	}

	public static Map<Long, ResumenPersona> obtenerResumenPersonaPorIdPersona(Connection conn,  List<Long> idsPersona) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerResumenPersonaPorIdPersona");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdsPersona", idsPersona);

		SQL sql = new SQL();
		Map<Long, ResumenPersona> resumenPorDocumento = sql.query(conn, SQL_SELECT_RESUMEN_PERSONA_POR_ID_PERSONA, params, ResumenPersona.class)
				.stream()
				.collect(Collectors.toMap(rp -> rp.IdPersona, rp -> rp));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerResumenPersonaPorIdPersona", fin - inicio);

		return resumenPorDocumento;
	}

	public static Map<String, ResumenPersona> obtenerResumenPersonaPorDocumentoIdentidad(Connection conn,  List<String> documentosIdentidad) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerResumenPersonaPorDocumentoIdentidad");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("DocumentosIdentidad", documentosIdentidad);

		SQL sql = new SQL();
		Map<String, ResumenPersona> resumenPorDocumento = sql.query(conn, SQL_SELECT_RESUMEN_PERSONA_POR_DOCUMENTO_IDENTIDAD, params, ResumenPersona.class)
			.stream()
			.collect(Collectors.toMap(rp -> rp.DocumentoIdentidad, rp -> rp));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerResumenPersonaPorDocumentoIdentidad", fin - inicio);

		return resumenPorDocumento;
	}

	public static ResultadoValidacionRequisitosEnrolamientoFull validarEnrolamientoFull(Connection conn, Long idEmpresa, Long idUsuario, String idModulo, String usaReglasNegocio, String idTipoCanalIngreso, List<Long> idsClientes) throws Exception {
		logger.debug("INICIO::SocioNegocioUtils.validarEnrolamientoFull");
		long inicio = System.currentTimeMillis();

		ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio entradaConsultarFichaSocioNegocio = new ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio();
		entradaConsultarFichaSocioNegocio.p_IdEmpresa = idEmpresa;
		entradaConsultarFichaSocioNegocio.p_IdUsuario = idUsuario;
		entradaConsultarFichaSocioNegocio.entidades = idsClientes
				.stream()
				.map(idCliente -> {
					ConsultarFichaSocioNegocio.ClienteEntidad clienteEntidad = new ConsultarFichaSocioNegocio.ClienteEntidad();
					clienteEntidad.IdCliente = idCliente;

					return clienteEntidad;
				})
				.collect(Collectors.toList());

		ConsultarFichaSocioNegocio consultarFichaSocioNegocio = new ConsultarFichaSocioNegocio();
		ConsultarFichaSocioNegocio.SalidaConsultarFichaSocioNegocio salidaConsultarFichaSocioNegocio = consultarFichaSocioNegocio.logica(conn, entradaConsultarFichaSocioNegocio);

		ValidarRequisitosEnrolamientoFull.EntradaValidarRequisitosEnrolamientoFull entradaValidarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull.EntradaValidarRequisitosEnrolamientoFull();
		entradaValidarRequisitosEnrolamientoFull.p_IdTipoCanalIngreso = idTipoCanalIngreso;
		entradaValidarRequisitosEnrolamientoFull.p_IdEmpresa = idEmpresa;
		entradaValidarRequisitosEnrolamientoFull.p_IdUsuario = idUsuario;
		entradaValidarRequisitosEnrolamientoFull.p_IdModulo = idModulo;
		entradaValidarRequisitosEnrolamientoFull.p_UsaReglasNegocio = usaReglasNegocio;
		entradaValidarRequisitosEnrolamientoFull.clientes = salidaConsultarFichaSocioNegocio.P_OUT_RESULTADOPROCESO;

		ValidarRequisitosEnrolamientoFull validarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull();
		ValidarRequisitosEnrolamientoFull.SalidaValidarRequisitosEnrolamientoFull salidaValidarRequisitosEnrolamientoFull = validarRequisitosEnrolamientoFull.logica(conn, entradaValidarRequisitosEnrolamientoFull);

		ResultadoValidacionRequisitosEnrolamientoFull resultado = new ResultadoValidacionRequisitosEnrolamientoFull();
		resultado.requisitos.addAll(salidaValidarRequisitosEnrolamientoFull.P_OUT_REQUISITOS_ENR_FULL);
		resultado.validaciones.addAll(salidaValidarRequisitosEnrolamientoFull.P_OUT_VALIDACIONES);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.validarEnrolamientoFull", fin - inicio);

		return resultado;
	}

	public static List<TipoSegmento> obtenerTiposSegmentos(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerTiposSegmentos");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		List<TipoSegmento> segmentos = new SQL().query(conn, SQL_SELECT_TIPO_SEGMENTO, params, TipoSegmento.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerTiposSegmentos {}", fin - inicio);

		return segmentos;

	}

	public static List<ConfiguracionPortafolioDefecto> obtenerConfiguracionPortafolioDefecto(Connection conn) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerConfiguracionPortafolioDefecto");
		long inicio = System.currentTimeMillis();

		List<ConfiguracionPortafolioDefecto> configuracionesDefecto = new SQL().query(conn, SQL_SELECT_CONFIGURACION_PORTAFOLIO_DEFECTO, new HashMap<>(), ConfiguracionPortafolioDefecto.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerConfiguracionPortafolioDefecto {}", fin - inicio);

		return configuracionesDefecto;
	}
	
	public static Map<Long, Empleado> obtenerEmpleadoPorIdUsuario(Connection conn, Long idEmpresa, List<Long> usuarioIds) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerEmpleadoPorIdUsuario");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(4);
		params.put("IdEmpresa", idEmpresa);
		params.put("IdUsuario", usuarioIds);
		
		Map<Long, Empleado> empleadoPorIdUsuario = new SQL().query(conn, SQL_SELECT_EMPLEADO_POR_USUARIO, params, Empleado.class).stream().collect(Collectors.toMap(u -> u.IdUsuario, u -> u)); 
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerEmpleadoPorIdUsuario {}", fin - inicio);
		
		return empleadoPorIdUsuario;
	}
	
	public static Optional<Empleado> obtenerEmpleadoUsuario(Connection conn, Long idEmpresa, Long idUsuario) throws SQLException {
		logger.debug("INICIO::SocioNegocioUtils.obtenerEmpleadoUsuario");
		long inicio = System.currentTimeMillis();
		
		Map<Long, Empleado> empleadoPorIdUsuario = obtenerEmpleadoPorIdUsuario(conn, idEmpresa, new ArrayList<>(Collections.singletonList(idUsuario)));
		Optional<Empleado> maybeEmpleado = Optional.ofNullable(empleadoPorIdUsuario.get(idUsuario));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::SocioNegocioUtils.obtenerEmpleadoUsuario {}", fin - inicio);
		
		return maybeEmpleado;
	}
}
