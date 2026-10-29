package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.enr.ConsultarFormularioSocioNegocio;
import cl.bcs.socionegocio.enr.ConsultarFormularioSocioNegocio.FormularioSocioNegocio;
import cl.bcs.socionegocio.enr.ConsultarFormularioSocioNegocio.SalidaConsultarFormularioSocioNegocio;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.wbs.pojo.DivisionPais;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public class WebServiceUtils{
	
	private static FileLoader loader;
	private static String SQL_SELECT_DIVISION_PAIS;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_DIVISION_PAIS = loader.loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarDireccionFisica_SelectDivisionPaisPorIdDivisionPais.sql");
	}
	
    public static class EntidadManipulada extends BcsPojo {

	public String TipoEntidad;
	public Long IdEntidad;
	public List<MensajeSalida> Mensajes;

	public EntidadManipulada() {
	    Mensajes = new ArrayList<MensajeSalida>();
	}

    }

    public static class EntidadModificada extends EntidadManipulada {

    }

    public static class EntidadCreada extends EntidadManipulada {

    }

    public static class SalidaWBS extends BcsPojo {
	public Long P_OUT_ERRORESCONTADOR;
	public Long P_OUT_ADVERTENCIASCONTADOR;
	public List<EntidadManipulada> P_OUT_ENTIDADES_MANIPULADAS;

	public SalidaWBS() {
		this.P_OUT_ERRORESCONTADOR = 0L;
		this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		this.P_OUT_ENTIDADES_MANIPULADAS = new ArrayList<WebServiceUtils.EntidadManipulada>();
	}
    }
	public static class Id extends BcsPojo {
	    public Long Id;
	}

    static final Logger logger = LogManager.getLogger(WebServiceUtils.class);

    public static EntidadManipulada noEsCliente(EntidadManipulada EntidadManipulada) {
		logger.trace("INICIO::WebServiceUtils.noEsCliente");
		long inicio = System.currentTimeMillis();
		MensajeSalida mensaje = new MensajeSalida();
		mensaje.id = 0L;
		mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		mensaje.descripcion = "ERROR_CLIENTE_NO_EXISTE";
		mensaje.codigo = "ERROR_CLIENTE_NO_EXISTE";
		EntidadManipulada.Mensajes.add(mensaje);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.noEsCliente", fin - inicio);
	
		return EntidadManipulada;
    }
    public static EntidadManipulada noSeRelaciona(EntidadManipulada EntidadManipulada) {
		logger.trace("INICIO::WebServiceUtils.noSeRelaciona");
		long inicio = System.currentTimeMillis();
		MensajeSalida mensaje = new MensajeSalida();
		mensaje.id = 0L;
		mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		mensaje.descripcion = "ERROR_NO_RELACION_PERSONA_ENTIDAD";
		mensaje.codigo = "ERROR_NO_RELACION_PERSONA_ENTIDAD";
		EntidadManipulada.Mensajes.add(mensaje);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.noSeRelaciona", fin - inicio);
	
		return EntidadManipulada;
    }
    public static EntidadManipulada noExisteIdEntidad(EntidadManipulada EntidadManipulada) {
		logger.trace("INICIO::WebServiceUtils.noExisteIdEntidad");
		long inicio = System.currentTimeMillis();
		MensajeSalida mensaje = new MensajeSalida();
		mensaje.id = 0L;
		mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		mensaje.descripcion = "ERROR_NOEXISTE_ID_ENTIDAD";
		mensaje.codigo = "ERROR_NOEXISTE_ID_ENTIDAD";
		EntidadManipulada.Mensajes.add(mensaje);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.noExisteIdEntidad", fin - inicio);
	
		return EntidadManipulada;
    }

    public static RegistroCambio crearRegistroCambio(String codigoCampo, String valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambio crearRegistroCambio(String codigoCampo, Long valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }
    
    public static RegistroCambio crearRegistroCambio(String codigoCampo, Integer valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambio crearRegistroCambio(String codigoCampo, Date valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDate(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambio crearRegistroCambio(String codigoCampo, Timestamp valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDatetime(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, String valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, Long valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }
    
    public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, Integer valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, Date valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDate(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, Timestamp valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDatetime(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static RegistroCambioMasivo crearRegistroCambio(Long idEntidad, String codigoCampo, BigDecimal valor) {
		logger.trace("INICIO::WebServiceUtils.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
		registroCambio.IdEntidad = idEntidad;
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor.toPlainString();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

    public static void merge(Object obj, Object newObject) {
		logger.trace("INICIO::WebServiceUtils.merge");
		long inicio = System.currentTimeMillis();
		assert obj.getClass().getName().equals(newObject.getClass().getName());
		for (Field field : obj.getClass().getDeclaredFields()) {
		    Field newField;
		    try {
			newField = newObject.getClass().getField(field.getName());
			if(newField != null) {
			    field.set(obj, newField.get(newObject) == null ? field.get(obj) : newField.get(newObject));
			}
		    } catch (Exception e) {
		    }
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.merge", fin - inicio);
    }

    public static String obtenerCodigoFormulario(Connection con, String tipoPersona, String tipoEnrolamiento, String formulario) {
		logger.trace("INICIO::WebServiceUtils.obtenerCodigoFormulario");
		long inicio = System.currentTimeMillis();
		ConsultarFormularioSocioNegocio consultarFormularioSocioNegocio = new ConsultarFormularioSocioNegocio();
		ConsultarFormularioSocioNegocio.EntradaConsultarFormularioSocioNegocio entradaConsultarFormularioSocioNegocio = new ConsultarFormularioSocioNegocio.EntradaConsultarFormularioSocioNegocio();
		entradaConsultarFormularioSocioNegocio.p_TipoEnrolamiento = tipoEnrolamiento;
		entradaConsultarFormularioSocioNegocio.p_TipoPersona = tipoPersona;
	
		SalidaConsultarFormularioSocioNegocio salidaConsultarFormularioSocioNegocio = consultarFormularioSocioNegocio.logica(con, entradaConsultarFormularioSocioNegocio);
		String out = salidaConsultarFormularioSocioNegocio.p_FormulariosSocioNegocio.parallelStream().filter(formServicio -> formulario.equals(formServicio.Formulario)).findFirst().orElse(new FormularioSocioNegocio()).CodigoFormulario;
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.obtenerCodigoFormulario", fin - inicio);
		return out;
    }

    public static EntidadManipulada agregarMensajeOk(EntidadManipulada EntidadManipulada, String accion) {
		logger.trace("INICIO::WebServiceUtils.agregarMensajeOk");
		long inicio = System.currentTimeMillis();
	
		MensajeSalida mensajeOk = new MensajeSalida();
		mensajeOk.id = 0L;
		mensajeOk.tipo = PlataformaUtils.MensajeSalida.MENSAJE_OK;
		mensajeOk.codigo = "ENTIDAD_" + accion.toUpperCase() + "_CORRECTAMENTE";
		mensajeOk.descripcion = "ENTIDAD_" + accion.toUpperCase() + "_CORRECTAMENTE";
		EntidadManipulada.Mensajes.add(mensajeOk);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::WebServiceUtils.agregarMensajeOk", fin - inicio);
		return EntidadManipulada;
    }

    public static EntidadManipulada agregarMensajesCamposRequeridos(EntidadManipulada entidadModificada, SalidaAdministrarWS out) {
		entidadModificada.Mensajes.addAll(out.P_OUT_MENSAJES);
		return entidadModificada;
    }
    

	public static Map<Long, DivisionPais> obtenerDivisionPaisPorIdDivisionPais(Connection conn, List<Long> divisionPaisIds) throws SQLException {
		logger.debug("INICIO::CrearDireccionFisica.obtenerDivisionPaisPorIdDivisionPais");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("DivisionPaisIds", divisionPaisIds);
		
		SQL sql = new SQL();
		Map<Long, DivisionPais> divisionPaisPorIdDivisionPais = sql.query(conn, SQL_SELECT_DIVISION_PAIS, params, DivisionPais.class)
				.stream()
				.collect(Collectors.toMap(u -> u.IdDivisionPais, u -> u));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionFisica.obtenerDivisionPaisPorIdDivisionPais {}", fin - inicio);

		return divisionPaisPorIdDivisionPais;
	}
	

}
