package cl.bcs.socionegocio.enr;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.socionegocio.enr.pojo.Ciudad;
import cl.bcs.socionegocio.enr.pojo.TipoRelacion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronicaEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.PerfilComercialBase;
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.socionegocio.enr.pojo.TipoCliente;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;

public class EnrolamientoUtils {
	
	static final Logger logger = LogManager.getLogger(EnrolamientoUtils.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_PERFIL_COMERCIAL_POR_DEFECTO;
	private static String SQL_SELECT_PERFILES_COMERCIALES_POR_DEFECTO;
	private static String SQL_SELECT_TIPO_CLIENTE_POR_DEFECTO;
	private static String SQL_SELECT_TIPOS_CLIENTE_POR_DEFECTO;
	private static String SQL_SELECT_PAIS_CIUDAD;
	private static String SQL_SELECT_RELACION;

	private static String ID_CONCEPTO = "ENROLC";
	private static String ID_CONCEPTO_FULL = "ENCFUL";
	private static String ID_CONCEPTO_EXPRESS = "ENCEXP";
	
	
	static {
		loader = new FileLoader();
		SQL_SELECT_PERFIL_COMERCIAL_POR_DEFECTO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/EnrolamientoUtils_SelectPerfilComercialPorDefecto.sql");
		SQL_SELECT_PERFILES_COMERCIALES_POR_DEFECTO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/EnrolamientoUtils_SelectPerfilesComercialesPorDefecto.sql");
		SQL_SELECT_TIPO_CLIENTE_POR_DEFECTO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/EnrolamientoUtils_SelectTipoClientePorDefecto.sql");
		SQL_SELECT_TIPOS_CLIENTE_POR_DEFECTO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/EnrolamientoUtils_SelectTiposClientePorDefecto.sql");
		SQL_SELECT_PAIS_CIUDAD = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/EnrolamientoUtils_SelectPaisCiudad.sql");
		SQL_SELECT_RELACION = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/EnrolamientoUtils_SelectRelacion.sql");
	}
	
	public static PerfilComercialBase obtenerPerfilComercialPorDefecto(Connection con, Long idEmpresa, String idCategoriaInversionista, String tipoEnrolamiento) throws SQLException {
		logger.debug("INICIO::EnrolamientoUtils.obtenerPerfilComercialPorDefecto");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<>(5);
		parametros.put("IdEmpresa", idEmpresa);
		parametros.put("IdCategoriaInversionista", idCategoriaInversionista);
		parametros.put("TipoEnrolamiento", tipoEnrolamiento);

		SQL sql = new SQL();
		PerfilComercialBase perfilComercial = sql
				.query(con, SQL_SELECT_PERFIL_COMERCIAL_POR_DEFECTO, parametros, PerfilComercialBase.class)
				.stream()
				.findFirst()
				.orElse(null);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerPerfilComercialPorDefecto", fin - inicio);

		return perfilComercial;
	}
	
	public static List<PerfilComercialBase> obtenerPerfilesComercialesPorDefecto(Connection con, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::EnrolamientoUtils.obtenerPerfilesComercialesPorDefecto");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<>(2);
		parametros.put("IdEmpresa", idEmpresa);

		SQL sql = new SQL();
		List<PerfilComercialBase> perfilesComerciales = sql
				.query(con, SQL_SELECT_PERFILES_COMERCIALES_POR_DEFECTO, parametros, PerfilComercialBase.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerPerfilesComercialesPorDefecto", fin - inicio);

		return perfilesComerciales;
	}
	
	public static TipoCliente obtenerTipoClientePorDefecto(Connection con, Long idEmpresa, String tipoEnrolamiento) throws SQLException {
		logger.debug("INICIO::EnrolamientoUtils.obtenerTipoClientePorDefecto");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<>(5);
		parametros.put("IdEmpresa", idEmpresa);
		parametros.put("TipoEnrolamiento", tipoEnrolamiento);

		SQL sql = new SQL();
		TipoCliente tipoCliente = sql
				.query(con, SQL_SELECT_TIPO_CLIENTE_POR_DEFECTO, parametros, TipoCliente.class)
				.stream()
				.findFirst()
				.orElse(null);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerTipoClientePorDefecto", fin - inicio);

		return tipoCliente;
	}
	
	public static List<TipoCliente> obtenerTiposClientePorDefecto(Connection con, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::EnrolamientoUtils.obtenerTiposClientePorDefecto");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<>(5);
		parametros.put("IdEmpresa", idEmpresa);

		SQL sql = new SQL();
		List<TipoCliente> tiposCliente = sql
				.query(con, SQL_SELECT_TIPOS_CLIENTE_POR_DEFECTO, parametros, TipoCliente.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerTiposClientePorDefecto", fin - inicio);

		return tiposCliente;
	}
	
	public static DocumentoIdentidad obtenerDocumentoIdentidadPorId(List<DocumentoIdentidad> documentosIdentidad, Long idDocumentoIdentidad) {
		logger.trace("INICIO::EnrolamientoUtils.obtenerDocumentoIdentidadPorId");
		long inicio = System.currentTimeMillis();
		DocumentoIdentidad documentoIdentidadPorId = documentosIdentidad
				.parallelStream()
				.filter(documentoIdentidad->documentoIdentidad.IdDocumentoIdentidad.equals(idDocumentoIdentidad))
				.findFirst()
				.orElse(null);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerDocumentoIdentidadPorId", fin - inicio);
		return documentoIdentidadPorId;
	}
	
	public static DireccionFisica obtenerDireccionFisicaPorId(List<DireccionFisica> direccionesFisica, Long idDireccionFisica) {
		logger.trace("INICIO::EnrolamientoUtils.obtenerDireccionFisicaPorId");
		long inicio = System.currentTimeMillis();
		DireccionFisica direccionFisicaPorId = direccionesFisica
				.parallelStream()
				.filter(direccionFisica->direccionFisica.IdDireccionFisica.equals(idDireccionFisica))
				.findFirst()
				.orElse(null);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerDireccionFisicaPorId", fin - inicio);
		return direccionFisicaPorId;
	}
	
	public static Telefono obtenerTelefonoPorId(List<Telefono> telefonos, Long idTelefono) {
		logger.trace("INICIO::EnrolamientoUtils.obtenerTelefonoPorId");
		long inicio = System.currentTimeMillis();
		Telefono telefonoPorId = telefonos
				.parallelStream()
				.filter(telefono->telefono.IdTelefono.equals(idTelefono))
				.findFirst()
				.orElse(null);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerTelefonoPorId", fin - inicio);
		return telefonoPorId;
	}
	
	public static DireccionElectronicaEnrolamiento obtenerDireccionElectronicaPorId(List<DireccionElectronicaEnrolamiento> direccionesElectronicas, Long idDireccionElectronica) {
		logger.trace("INICIO::EnrolamientoUtils.obtenerDireccionElectronicaPorId");
		long inicio = System.currentTimeMillis();
		DireccionElectronicaEnrolamiento direccionElectronicaPorId = direccionesElectronicas
				.parallelStream()
				.filter(direccionElectronica->direccionElectronica.IdDireccionElectronica.equals(idDireccionElectronica))
				.findFirst()
				.orElse(null);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerDireccionElectronicaPorId", fin - inicio);
		return direccionElectronicaPorId;
	}
	
	public static List<MensajeSalida> obtenerMensajesSalidaAdvertencia(List<AdvertenciaDetalle> advertencias, Long idInterno){
		logger.trace("INICIO::EnrolamientoUtils.obtenerMensajeSalidaAdvertencia");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = advertencias
				.parallelStream()
				.map(advertencia->{
					MensajeSalida mensaje = new MensajeSalida();
					mensaje.id = idInterno;
					mensaje.tipo = "S".equals(advertencia.RequiereAutorizacion) ? MensajeSalida.MENSAJE_AUTORIZACION : MensajeSalida.MENSAJE_ADVERTENCIA;
					mensaje.codigo = advertencia.Codigo;
					mensaje.descripcion = advertencia.Descripcion;
					return mensaje;
				})
				.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerMensajeSalidaAdvertencia", fin - inicio);
		return mensajes;
	}
	
	public static List<MensajeSalida> obtenerMensajesSalidaError(List<ErrorDetalle> errores, Long idInterno){
		logger.trace("INICIO::EnrolamientoUtils.obtenerMensajesSalidaError");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = errores
				.parallelStream()
				.map(error->{
					MensajeSalida mensaje = new MensajeSalida();
					mensaje.id = idInterno;
					mensaje.tipo = MensajeSalida.MENSAJE_ERROR;
					mensaje.codigo = error.Codigo;
					mensaje.descripcion = error.Descripcion;
					return mensaje;
				})
				.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerMensajesSalidaError", fin - inicio);
		return mensajes;
	}
	
	public static RegistroCambio crearRegistroCambio(String codigoCampo, String valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambio crearRegistroCambio(String codigoCampo, Long valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambio crearRegistroCambio(String codigoCampo, Date valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDate(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambio crearRegistroCambio(String codigoCampo, Timestamp valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDatetime(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, String valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, Long valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, Date valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDate(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, Timestamp valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDatetime(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, BigDecimal valor){
		logger.trace("INICIO::EnrolamientoUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor.toPlainString();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EnrolamientoUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}

	public static String obtenerTipoEnrolamiento(String tipoEnrolamiento){
		if("FULL".equals(tipoEnrolamiento)){
			return ID_CONCEPTO_FULL;
		}

		if("EXPRESS".equals(tipoEnrolamiento)){
			return ID_CONCEPTO_EXPRESS;
		}
		return ID_CONCEPTO;
	}
	
	public static List<Long> obtenerIdsPersona(List<DatosClienteDatosGenerales> borradoresCliente){
		List<Long> idsPersona = borradoresCliente
				.parallelStream()
				.map(borradorCliente->borradorCliente.IdPersona)
				.collect(Collectors.toList());
		
		return idsPersona;
	}

	public static Map<Long, Ciudad> obtenerCiudadPorIdCiudad(Connection conn, List<Long> ciudadIds) throws SQLException {
		logger.debug("INICIO::EnrolamientoUtils.obtenerCiudadPorIdCiudad");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("CiudadIds", ciudadIds);

		Map<Long, Ciudad> ciudadPorIdCiudad = new SQL().query(conn, SQL_SELECT_PAIS_CIUDAD, params, Ciudad.class)
				.stream()
				.collect(Collectors.toMap(u -> u.IdCiudad, u -> u));


		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerCiudadPorIdCiudad {}", fin - inicio);

		return ciudadPorIdCiudad;
	}

	public static Map<String, TipoRelacion> obtenerTipoRelacionPorIdTipoRelacion(Connection conn, List<String> tipoRelacionIds) throws SQLException {
		logger.debug("INICIO::EnrolamientoUtils.obtenerTipoRelacionPorIdTipoRelacion");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("TipoRelacionIds", tipoRelacionIds);

		Map<String, TipoRelacion> tipoRelacionPorIdTipoRelacion = new SQL().query(conn, SQL_SELECT_RELACION, params, TipoRelacion.class)
				.stream()
				.collect(Collectors.toMap(u -> u.IdTipoRelacion, u -> u));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnrolamientoUtils.obtenerTipoRelacionPorIdTipoRelacion {}", fin - inicio);

		return tipoRelacionPorIdTipoRelacion;
	}
}
