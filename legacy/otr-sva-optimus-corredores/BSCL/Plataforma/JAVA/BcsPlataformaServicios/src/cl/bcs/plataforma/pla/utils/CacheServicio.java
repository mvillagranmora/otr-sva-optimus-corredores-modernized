package cl.bcs.plataforma.pla.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.CacheConcurrente.Cargador;

/**
 * Cache de un servicio, identificado por un nombre.
 *
 * COMO SE USA (tres lineas en el servicio)
 *
 *   private static final CacheServicio CACHE =
 *           CacheServicio.registrar("MRF.ConsultarPosicionMRF", 7000L, "mrf:posicion");
 *
 *   String clave = CacheServicio.clave(jin.p_IdEmpresa, jin.p_IdMesa, jin.p_FechaPosicion);
 *   String json  = CACHE.obtener(clave, () -> gson.toJson(logica(conn, jin)));
 *   return new JsonParser().parse(json).getAsJsonObject();
 *
 */
public class CacheServicio {

	private static final Logger logger = LogManager.getLogger(CacheServicio.class);

	/** Espera maxima por defecto de quien se suma a un calculo en curso. */
	private static final long ESPERA_MAXIMA_MS = 30000L;
	/**
	 * Techo de entradas por cache.
	 *
	 */
	private static final int MAX_ENTRADAS = 500;

	/** Todos los caches registrados, por nombre. */
	private static final ConcurrentHashMap<String, CacheServicio> REGISTRO = new ConcurrentHashMap<>();

	private final String nombre;
	private final Set<String> tags;
	private final CacheConcurrente motor;

	private CacheServicio(String nombre, long ttlMillis, String... tags) {
		this.nombre = nombre;
		this.motor = new CacheConcurrente(ttlMillis, ESPERA_MAXIMA_MS, MAX_ENTRADAS);
		Set<String> conjunto = new LinkedHashSet<>();
		if (tags != null) {
			conjunto.addAll(Arrays.asList(tags));
		}
		this.tags = Collections.unmodifiableSet(conjunto);
	}

	/**
	 * Crea (o recupera) el cache de un servicio.
	 *
	 * Se llama una sola vez, desde un campo static final del servicio. Debe ser static:
	 * si el framework crea una instancia del servicio por peticion, un cache de instancia
	 * no compartiria nada y no serviria de nada.
	 *
	 * @param nombre     identificador unico. Convencion: "MODULO.NombreClase".
	 * @param ttlMillis  cuanto vive el resultado. Entre 5000 y 10000 para el caso de
	 *                   refresco por websocket. Se puede sobrescribir por propiedad.
	 * @param tags       eventos de negocio que dejan obsoleto este dato.
	 */
	public static CacheServicio registrar(String nombre, long ttlMillis, String... tags) {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del cache es obligatorio");
		}
		final long ttl = Long.getLong("cache." + nombre + ".ttlMs", ttlMillis);

		return REGISTRO.computeIfAbsent(nombre, n -> {
			logger.info("Cache registrado: {} ttlMs={} tags={}", n, ttl, Arrays.toString(tags));
			return new CacheServicio(n, ttl, tags);
		});
	}

	/** Devuelve el cache registrado con ese nombre, o null si no existe. */
	public static CacheServicio buscar(String nombre) {
		return REGISTRO.get(nombre);
	}

	public String obtener(String clave, Cargador cargador) throws Exception {
		return motor.obtener(clave, cargador, CacheServicio::sinErrores);
	}

	/**
	 * Version con regla propia para decidir si el resultado se guarda.
	 * Solo si la regla por defecto no sirve para este servicio.
	 */
	public String obtener(String clave, Cargador cargador, Predicate<String> retener) throws Exception {
		return motor.obtener(clave, cargador, retener);
	}

	/**
	 * Regla por defecto: no dejar guardada una respuesta con errores, para que una falla
	 * pasajera no quede fija durante todo el TTL.
	 *
	 */
	private static boolean sinErrores(String json) {
		if (!json.contains("\"P_OUT_ERRORESCONTADOR\":")) {
			return true;
		}
		return json.contains("\"P_OUT_ERRORESCONTADOR\":0");
	}

	/**
	 * Arma una clave juntando los parametros que cambian el resultado.
	 *
	 * Los null se tratan como texto vacio, para que null y "" no generen dos entradas
	 * distintas para la misma consulta.
	 *
	 * REGLA: van todos los parametros que cambian el resultado, y ninguno mas.
	 * Si falta uno, dos consultas distintas comparten respuesta (datos malos).
	 * Si sobra uno, se llena de entradas que nadie reutiliza (cache inutil).
	 * Ante la duda, incluirlo: que sobre cuesta rendimiento, que falte cuesta correccion.
	 */
	public static String clave(Object... partes) {
		StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < partes.length; i++) {
			if (i > 0) {
				sb.append('|');
			}
			sb.append(partes[i] == null ? "" : partes[i].toString().trim());
		}
		return sb.toString();
	}

	/**
	 * Borra todo el contenido de este cache.
	 * @return cuantas claves se borraron. Si sale 0 cuando esperabas mas, la invalidacion
	 *         se esta llamando antes de que existan entradas, o sobre el cache equivocado.
	 */
	public int invalidar() {
		int cuantas = motor.invalidarTodo();
		logger.debug("Cache {}: invalidadas {} claves", nombre, cuantas);
		return cuantas;
	}

	/** Borra una clave puntual de este cache. */
	public boolean invalidar(String clave) {
		boolean estaba = motor.invalidar(clave);
		logger.debug("Cache {}: invalidada la clave {} (estaba guardada: {})", nombre, clave, estaba);
		return estaba;
	}

	/**
	 * Borra todos los caches que tengan ese tag.
	 *
	 * Es lo que se llama desde un servicio que graba. Conviene invalidar por tag y no por
	 * nombre: cuando se agregue otro servicio cacheado con el mismo tag, queda cubierto
	 * sin tocar el codigo que invalida.
	 *
	 * @return cuantos caches se borraron.
	 */
	public static int invalidarPorTag(String tag) {
		int caches = 0;
		int claves = 0;
		for (CacheServicio cache : REGISTRO.values()) {
			if (cache.tags.contains(tag)) {
				claves += cache.invalidar();
				caches++;
			}
		}
		logger.info("Invalidacion por tag {}: {} caches, {} claves borradas", tag, caches, claves);
		return caches;
	}

	/**
	 * Borra el cache de un servicio por su nombre.
	 *
	 * @return false si ese nombre no esta registrado. Suele ser un error de tipeo, o que
	 *         la clase del servicio todavia no fue cargada por la JVM.
	 */
	public static boolean invalidarServicio(String nombre) {
		CacheServicio cache = REGISTRO.get(nombre);
		if (cache == null) {
			logger.warn("Se intento invalidar un cache no registrado: {}", nombre);
			return false;
		}
		cache.invalidar();
		return true;
	}

	public static int invalidarTodos() {
		for (CacheServicio cache : REGISTRO.values()) {
			cache.invalidar();
		}
		logger.info("Invalidados todos los caches ({})", REGISTRO.size());
		return REGISTRO.size();
	}

	public int tamano() {
		return motor.tamano();
	}

	public List<String> detalleClaves() {
		return motor.detalleClaves();
	}
}