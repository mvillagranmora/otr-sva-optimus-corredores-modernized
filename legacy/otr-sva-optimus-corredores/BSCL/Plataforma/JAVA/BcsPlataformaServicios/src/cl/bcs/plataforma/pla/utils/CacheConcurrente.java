package cl.bcs.plataforma.pla.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Motor del cache: guarda textos (JSON) por un tiempo corto y evita que dos hilos
 * calculen lo mismo al mismo tiempo.
 *
 */
public class CacheConcurrente {

	private static final Logger logger = LogManager.getLogger(CacheConcurrente.class);

	/** Cada cuanto se barren las entradas vencidas aunque el cache este chico. */
	private static final long INTERVALO_LIMPIEZA_MS = 30000L;

	/** Calcula el valor cuando no esta en el cache. */
	@FunctionalInterface
	public interface Cargador {
		String cargar() throws Exception;
	}

	/** Una entrada del cache: el valor (o la promesa del valor) y cuando vence. */
	private static class Entrada {
		final CompletableFuture<String> valor = new CompletableFuture<>();
		final long iniciadaEn = System.currentTimeMillis();
		volatile long venceEn = Long.MAX_VALUE;

		boolean vencida(long ahora) {
			return valor.isDone() && ahora >= venceEn;
		}

		/** Lleva tanto tiempo calculando que hay que darla por perdida. */
		boolean abandonada(long ahora, long limiteMillis) {
			return !valor.isDone() && (ahora - iniciadaEn) > limiteMillis;
		}
	}

	private final ConcurrentHashMap<String, Entrada> entradas = new ConcurrentHashMap<>();

	private final long ttlMillis;
	private final long esperaMaximaMillis;
	private final int maxEntradas;

	private final AtomicLong aciertos = new AtomicLong();
	private final AtomicLong fallos = new AtomicLong();
	private final AtomicLong esperasVencidas = new AtomicLong();
	private final AtomicLong ultimaLimpieza = new AtomicLong();
	private final AtomicBoolean limpiando = new AtomicBoolean(false);

	/**
	 * Que esta calculando cada hilo en este momento, para detectar reentradas.
	 * La llave es el id del hilo.
	 *
	 * Con este mapa, los hilos que solo leen no dejan nada: solo escriben aqui los que
	 * calculan, y borran su entrada al terminar. En condiciones normales tiene 0 o unas
	 * pocas entradas.
	 */
	private final ConcurrentHashMap<Long, Set<String>> calculandoPorHilo = new ConcurrentHashMap<>();

	/**
	 * @param ttlMillis          cuanto vive el valor ya calculado. 0 desactiva el cache.
	 * @param esperaMaximaMillis tope de espera de quien se suma a un calculo en curso.
	 *                           Debe ser MENOR al timeout del pool de conexiones.
	 * @param maxEntradas        a partir de cuantas entradas se hace limpieza.
	 */
	public CacheConcurrente(long ttlMillis, long esperaMaximaMillis, int maxEntradas) {
		this.ttlMillis = ttlMillis;
		this.esperaMaximaMillis = esperaMaximaMillis;
		this.maxEntradas = maxEntradas;
	}

	public String obtener(String clave, Cargador cargador) throws Exception {
		return obtener(clave, cargador, null);
	}

	/**
	 * Devuelve el valor de la clave: del cache si esta vigente, o calculandolo.
	 *
	 * @param retener opcional. Si devuelve false, el valor se entrega a todos los que
	 *                estan esperando en esta misma rafaga, pero no se guarda para las
	 *                siguientes. Sirve para no dejar fija una respuesta con error.
	 */
	public String obtener(String clave, Cargador cargador, Predicate<String> retener) throws Exception {
		if (ttlMillis <= 0) {
			return cargador.cargar();
		}

		Set<String> calculandoAhora = calculandoPorHilo.get(Thread.currentThread().getId());
		if (calculandoAhora != null && calculandoAhora.contains(clave)) {
			logger.debug("Reentrada sobre la clave {}; se calcula sin cache", clave);
			return cargador.cargar();
		}

		final long ahora = System.currentTimeMillis();
		final Entrada nueva = new Entrada();

		Entrada entrada = entradas.compute(clave,
				(k, actual) -> (actual != null && !actual.vencida(ahora)) ? actual : nueva);

		if (entrada == nueva) {
			return calcular(clave, nueva, cargador, retener);
		}
		return esperar(clave, entrada);
	}

	private String calcular(String clave, Entrada entrada, Cargador cargador, Predicate<String> retener)
			throws Exception {

		fallos.incrementAndGet();
		final Long idHilo = Thread.currentThread().getId();
		Set<String> enCurso = calculandoPorHilo.get(idHilo);
		if (enCurso == null) {
			enCurso = new HashSet<>();
			calculandoPorHilo.put(idHilo, enCurso);
		}
		enCurso.add(clave);

		try {
			String valor = cargador.cargar();

			boolean guardar = (retener == null) || retener.test(valor);
			entrada.venceEn = guardar ? System.currentTimeMillis() + ttlMillis : 0L;
			entrada.valor.complete(valor);
			if (!guardar) {
				entradas.remove(clave, entrada);
			}
			limpiarSiCorresponde();
			return valor;

		} catch (Throwable error) {
			entrada.venceEn = 0L;
			entrada.valor.completeExceptionally(error);
			entradas.remove(clave, entrada);
			throw comoExcepcion(error);

		} finally {
			enCurso.remove(clave);
			if (enCurso.isEmpty()) {
				calculandoPorHilo.remove(idHilo);
			}
		}
	}

	private String esperar(String clave, Entrada entrada) throws Exception {
		aciertos.incrementAndGet();
		logger.debug("Cache: clave {}; peticion respondida desde el cache", clave);
		try {
			return entrada.valor.get(esperaMaximaMillis, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			throw comoExcepcion(e.getCause());
		} catch (TimeoutException e) {
			esperasVencidas.incrementAndGet();

			if (entrada.abandonada(System.currentTimeMillis(), esperaMaximaMillis * 2)) {
				entradas.remove(clave, entrada);
				logger.error("Calculo abandonado en la clave {}: lleva mas de {} ms. "
						+ "Se descarta para que la siguiente peticion reintente.",
						clave, esperaMaximaMillis * 2);
			} else {
				logger.warn("Espera vencida ({} ms) en la clave {}", esperaMaximaMillis, clave);
			}
			throw e;
		}
	}

	private static Exception comoExcepcion(Throwable t) throws Exception {
		if (t instanceof Error) {
			throw (Error) t;
		}
		if (t instanceof Exception) {
			return (Exception) t;
		}
		return new IllegalStateException(t);
	}

	/** @return true si la clave estaba guardada. */
	public boolean invalidar(String clave) {
		return entradas.remove(clave) != null;
	}

	/** @return cuantas claves se borraron. */
	public int invalidarTodo() {
		int cuantas = entradas.size();
		entradas.clear();
		return cuantas;
	}

	/**
	 * Una linea por clave guardada, con su estado. Es lo que sirve para revisar por
	 * consola si el cache se esta limpiando: si tras invalidar siguen apareciendo claves
	 * vigentes, la invalidacion no llego.
	 *
	 */
	public List<String> detalleClaves() {
		long ahora = System.currentTimeMillis();
		List<String> lineas = new ArrayList<>();
		for (Map.Entry<String, Entrada> e : entradas.entrySet()) {
			Entrada entrada = e.getValue();
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-45s ", e.getKey()));

			if (!entrada.valor.isDone()) {
				sb.append(String.format("%-12s calculando hace %.1fs", "CALCULANDOSE",
						(ahora - entrada.iniciadaEn) / 1000d));
			} else {
				long restan = entrada.venceEn - ahora;
				if (restan > 0) {
					sb.append(String.format("%-12s vence en %.1fs", "VIGENTE", restan / 1000d));
				} else {
					sb.append(String.format("%-12s vencida hace %.1fs", "VENCIDA", -restan / 1000d));
				}
				String v = entrada.valor.getNow(null);
				if (v != null) {
					sb.append(String.format("   %d KB", v.length() / 1024));
				}
			}
			lineas.add(sb.toString());
		}
		Collections.sort(lineas);
		return lineas;
	}

	public int tamano() {
		return entradas.size();
	}

	/**
	 * Libera memoria. Se ejecuta por dos motivos:
	 *
	 *   1. Cada 30 segundos. Necesario porque una clave que nunca se vuelve a consultar no se pisa sola y quedaria
	 *      ocupando memoria para siempre.
	 *   2. Cuando se pasa de maxEntradas, para poner un techo duro.
	 *
	 * Es perezosa: corre dentro de alguna peticion, no hay hilo aparte. Y esta limitada a
	 * una vez cada 30s (o una vez por segundo si se supero el tope), asi que no afecta el
	 * tiempo de respuesta.
	 */
	private void limpiarSiCorresponde() {
		final long ahora = System.currentTimeMillis();
		final boolean sobreTope = entradas.size() > maxEntradas;

		if (!sobreTope && ahora - ultimaLimpieza.get() < INTERVALO_LIMPIEZA_MS) {
			return;
		}
		if (!limpiando.compareAndSet(false, true)) {
			return;
		}

		try {
			ultimaLimpieza.set(ahora);
			int antes = entradas.size();

			for (Map.Entry<String, Entrada> e : entradas.entrySet()) {
				if (e.getValue().vencida(ahora) || e.getValue().abandonada(ahora, esperaMaximaMillis * 2)) {
					entradas.remove(e.getKey(), e.getValue());
				}
			}

			int objetivo = (int) (maxEntradas * 0.8);
			int excedente = entradas.size() - objetivo;
			if (entradas.size() > maxEntradas && excedente > 0) {
				List<Map.Entry<String, Entrada>> ordenadas = new ArrayList<>(entradas.entrySet());
				ordenadas.sort(Comparator.comparingLong(e -> e.getValue().venceEn));
				for (int i = 0; i < excedente && i < ordenadas.size(); i++) {
					entradas.remove(ordenadas.get(i).getKey(), ordenadas.get(i).getValue());
				}
				logger.warn("Cache sobre el tope de {} entradas. Revisar si la clave tiene "
						+ "demasiados valores posibles.", maxEntradas);
			}

			if (antes != entradas.size()) {
				logger.info("Limpieza de cache: {} -> {} entradas", antes, entradas.size());
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Claves en cache tras la limpieza ({}):", entradas.size());
				for (String linea : detalleClaves()) {
					logger.debug("   {}", linea);
				}
			}
		} finally {
			limpiando.set(false);
		}
	}
}