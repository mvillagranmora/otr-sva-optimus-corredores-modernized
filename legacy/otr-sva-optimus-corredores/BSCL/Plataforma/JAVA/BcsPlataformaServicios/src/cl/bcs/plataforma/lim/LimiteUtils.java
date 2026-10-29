package cl.bcs.plataforma.lim;

import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.lim.pojo.CuentaPropia;
import cl.bcs.plataforma.lim.pojo.Limite;
import cl.bcs.plataforma.lim.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LimiteUtils {
	private static final Logger logger = LogManager.getLogger(LimiteUtils.class);
	
	private static final ConstantesLimites _cl = new ConstantesLimites();
	private static final Set<String> PROCESOS_ININTERRUMPIBLES = Stream.of(_cl.PROCESO_ANULAR, _cl.PROCESO_DESASIGNAR, _cl.PROCESO_MODIFICACION_MESA, _cl.PROCESO_PAUTA_VENCIMIENTO).collect(Collectors.toSet());
	
	private static final String SQL_SELECT_PORTAFOLIO_CUENTA_PROPIA;
	private static final String SQL_SELECT_INSTRUMENTO_POR_TIPO_INSTRUMENTO;
	
	static {
		FileLoader fileLoader = new FileLoader();
		SQL_SELECT_PORTAFOLIO_CUENTA_PROPIA = fileLoader.loadResourceToString("cl/bcs/plataforma/lim/sql/LimiteUtils_SelectPortafolioCuentaPropia.sql");
		SQL_SELECT_INSTRUMENTO_POR_TIPO_INSTRUMENTO = fileLoader.loadResourceToString("cl/bcs/plataforma/lim/sql/LimiteUtils_SelectInstrumentoPorTipoInstrumento.sql");
	}
	
	public static class Portafolio extends BcsPojo {
		public Long IdCliente;
		public Long IdPersona;
		public Long IdPortafolio;
		
		public Portafolio() {
			IdCliente = 0L;
			IdPersona = 0L;
			IdPortafolio = 0L;
		}
	}
	
	public static class EntradaObtenerValidacionLimite extends BcsPojo {
		public transient Connection Connection;
		public Long IdEmpresa;
		public Long IdInterno;
		public Long IdInternoGrupo;
		public String Folio;
		public String NemoInstrumento;
		public Limite Limite;
		public String Proceso;
	}
	
	public static class Instrumento extends BcsPojo {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String RazonSocial;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
	}
	
	public static List<Portafolio> obtenerPortafoliosCuentaPropia(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::LimiteUtils.obtenerPortafoliosCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		
		List<Portafolio> portafolios = new SQL().query(conn, SQL_SELECT_PORTAFOLIO_CUENTA_PROPIA, params, Portafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::LimiteUtils.obtenerPortafoliosCuentaPropia {}", fin - inicio);
		
		return portafolios;
	}
	
	public static CuentaPropia obtenerCuentaPropia(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::LimiteUtils.obtenerCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		List<Portafolio> portafolios = obtenerPortafoliosCuentaPropia(conn, idEmpresa);
		
		Portafolio pref = portafolios.stream().findFirst().orElse(new Portafolio());
		CuentaPropia cuentaPropia = new CuentaPropia();
		cuentaPropia.IdPersona = pref.IdPersona;
		cuentaPropia.IdCliente = pref.IdCliente;
		cuentaPropia.PortafolioIds = portafolios.stream().map(u -> u.IdPortafolio).collect(Collectors.toSet());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::LimiteUtils.obtenerCuentaPropia {}", fin - inicio);
		
		return cuentaPropia;
		
	}
	
	public static Optional<ChronoUnit> obtenerUnidadTemporal(String codigo) {
		logger.trace("INICIO::LimiteUtils.obtenerUnidadTemporal");
		long inicio = System.currentTimeMillis();
		
		ChronoUnit unidad;
		switch (codigo) {
			case "D":
				unidad = ChronoUnit.DAYS;
				break;
			case "M":
				unidad = ChronoUnit.MONTHS;
				break;
			case "Y":
				unidad = ChronoUnit.YEARS;
				break;
			case "W":
				unidad = ChronoUnit.WEEKS;
				break;
			default:
				unidad = null;
		}
		Optional<ChronoUnit> maybeUnidadTemporal = Optional.ofNullable(unidad);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::LimiteUtils.obtenerUnidadTemporal {}", fin - inicio);
		
		return maybeUnidadTemporal;
	}
	
	public static PlataformaUtils.MensajeSalida obtenerMensajeSalida(Connection conn, Long idEmpresa, Long id, String tipo, String codigo, HashMap<String, String> params) {
		logger.trace("FIN::LimiteUtils.obtenerMensajeSalida");
		long inicio = System.currentTimeMillis();
		
		PlataformaUtils.MensajeSalida mensajeSalida;
		try {
			mensajeSalida = new PlataformaUtils.MensajeSalida(id, tipo, codigo, params, conn, idEmpresa);
		} catch (Exception e) {
			mensajeSalida = new PlataformaUtils.MensajeSalida();
			mensajeSalida.codigo = codigo;
			mensajeSalida.id = id;
			mensajeSalida.tipo = tipo;
			mensajeSalida.descripcion = codigo + " sin traduccion";
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::LimiteUtils.obtenerMensajeSalida {}", fin - inicio);
		
		return mensajeSalida;
	}
	
	public static Optional<Validacion> obtenerValidacionLimite(EntradaObtenerValidacionLimite entrada) {
		logger.trace("INICIO::LimiteUtils.obtenerValidacionLimite");
		long inicio = System.currentTimeMillis();
		
		Optional<Validacion> maybeValidacion = Optional.empty();
		if (_cl.TIPO_VALIDACION_OMITIR.equals(entrada.Limite.IdTipoComportamiento) == false) {
			HashMap<String, String> params = new HashMap<>(8);
			String nombreLimite = obtenerMensajeSalida(entrada.Connection, entrada.IdEmpresa, entrada.IdInterno, PlataformaUtils.MensajeSalida.MENSAJE_INFORMACION, entrada.Limite.IdTipoLimite, params).descripcion;
			params.put("Nemo", entrada.NemoInstrumento);
			params.put("Limite", nombreLimite);
			if (Objects.nonNull(entrada.Folio)) {
				params.put("Folio", entrada.Folio);
			}
			
			Validacion validacion = new Validacion();
			validacion.IdInternoOperacion = entrada.IdInterno;
			validacion.IdInternoGrupoOperacion = entrada.IdInternoGrupo;
			validacion.TipoComportamiento = PROCESOS_ININTERRUMPIBLES.contains(entrada.Proceso) ? _cl.TIPO_VALIDACION_ADVERTENCIA : entrada.Limite.IdTipoComportamiento;
			
			if (_cl.TIPO_VALIDACION_ADVERTENCIA.equals(validacion.TipoComportamiento)) {
				String codigo = params.containsKey("Folio") ? "UI_ADV_LIMCTP_NEMOFOLIO" : "UI_ADV_LIMCTP_NEMO";
				validacion.Mensaje = obtenerMensajeSalida(entrada.Connection, entrada.IdEmpresa, entrada.IdInterno, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, codigo, params);
			} else if (_cl.TIPO_VALIDACION_AUTORIZACION.equals(validacion.TipoComportamiento)) {
				String codigo = params.containsKey("Folio") ? "UI_AUT_LIMCTP_NEMOFOLIO" : "UI_AUT_LIMCTP_NEMO";
				validacion.Mensaje = obtenerMensajeSalida(entrada.Connection, entrada.IdEmpresa, entrada.IdInterno, PlataformaUtils.MensajeSalida.MENSAJE_AUTORIZACION, codigo, params);
			} else if (_cl.TIPO_VALIDACION_RECHAZAR.equals(validacion.TipoComportamiento)) {
				String codigo = params.containsKey("Folio") ? "UI_RECH_LIMCTP_NEMOFOLIO" : "UI_RECH_LIMCTP_NEMO";
				validacion.Mensaje = obtenerMensajeSalida(entrada.Connection, entrada.IdEmpresa, entrada.IdInterno, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, codigo, params);
			}
			
			maybeValidacion = Optional.of(validacion);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::LimiteUtils.obtenerValidacionLimite {}", fin - inicio);
		
		return maybeValidacion;
	}
	
	public static List<Instrumento> obtenerInstrumentos(Connection conn, String idTipoInstrumento) throws SQLException {
		logger.debug("INICIO::LimiteUtils.obtenerInstrumentos");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdTipoInstrumento", idTipoInstrumento);
		
		List<Instrumento> instrumentos = new SQL().query(conn, SQL_SELECT_INSTRUMENTO_POR_TIPO_INSTRUMENTO, params, Instrumento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::LimiteUtils.obtenerInstrumentos {}", fin - inicio);
		
		return instrumentos;
	}
	
	public static Map<String, Instrumento> obtenerInstrumentoPorNemotecnico(Connection conn, String idTipoInstrumento) throws SQLException {
		logger.trace("INICIO::LimiteUtils.obtenerInstrumentoPorNemotecnico");
		long inicio = System.currentTimeMillis();
		
		Map<String, Instrumento> instrumentoPorNemotecnico = obtenerInstrumentos(conn, idTipoInstrumento).stream().collect(Collectors.toMap(u -> u.Nemotecnico, u -> u));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::LimiteUtils.obtenerInstrumentoPorNemotecnico {}", fin - inicio);
		
		return instrumentoPorNemotecnico;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				CuentaPropia cuentaPropia = obtenerCuentaPropia(conn, 1L);
				System.out.println(cuentaPropia);
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}
