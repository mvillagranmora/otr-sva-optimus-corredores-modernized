package cl.bcs.utils;

import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * UTIL-36: measures how long blocks of code take (legacy {@code Timer.java:18-127}; log4j replaced by SLF4J).
 *
 * <pre>
 * Timer tm = Timer.createTimer();
 * tm.addMetric("INICIO PROCESO");
 * // ... block to measure ...
 * tm.endMetric();
 * </pre>
 *
 * <p>Each message is {@code "<flag>, tardo: <elapsed>(<unit>)"}: to the logger at trace ({@link #endMetric()}) or
 * debug ({@link #printMetrics()}) when one is set, otherwise to {@code System.out}. The clock is read once per
 * {@link #addMetric(String)} and once per {@link #endMetric()}; {@link #createTimer(LongSupplier)} injects it.
 *
 * <p>Legacy defects kept as pinned: only {@link TimeUnit#SECONDS} converts (integer division of millis); any other
 * unit prints raw milliseconds labelled with that unit; the division uses the unit current at calculation time while
 * the label is the unit current at {@code addMetric}; {@code printMetrics} always writes a blank line to stdout first.
 */
public final class Timer {

    private static final class Metrica {
        private final long inicio;
        private final String flag;
        private final String unidad;
        private Long fin;

        private Metrica(long inicio, String flag, String unidad) {
            this.inicio = inicio;
            this.flag = flag;
            this.unidad = unidad;
        }
    }

    private final Deque<Metrica> metricas = new ArrayDeque<>();
    private final LongSupplier relojMs;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private Logger logger;

    private Timer(LongSupplier relojMs) {
        this.relojMs = Objects.requireNonNull(relojMs, "relojMs");
    }

    /** A timer on the system clock ({@link System#currentTimeMillis()}). */
    public static Timer createTimer() {
        return new Timer(System::currentTimeMillis);
    }

    /** A timer on the given millisecond clock. */
    public static Timer createTimer(LongSupplier clockMillis) {
        return new Timer(clockMillis);
    }

    public Timer setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    /** Sends the messages to {@code logger} instead of stdout; {@code null} goes back to stdout. */
    public Timer setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /** Closes the open metric, if any, and starts a new one. */
    public Timer addMetric(String flag) {
        cerrarAbierta();
        metricas.addLast(new Metrica(relojMs.getAsLong(), flag, timeUnit.name()));
        return this;
    }

    /** Closes the last metric and reports it (trace); throws {@link java.util.NoSuchElementException} if there is none. */
    public Timer endMetric() {
        Metrica m = metricas.getLast();
        m.fin = relojMs.getAsLong();
        String msj = mensaje(m);
        if (logger != null) {
            logger.trace(msj);
        } else {
            System.out.println(msj);
        }
        return this;
    }

    public Timer clearMetrics() {
        metricas.clear();
        return this;
    }

    /** Closes the open metric, writes a blank line to stdout, reports every metric (debug) and clears them. */
    public void printMetrics() {
        cerrarAbierta();
        System.out.println();
        for (Metrica m : metricas) {
            String msj = mensaje(m);
            if (logger != null) {
                logger.debug(msj);
            } else {
                System.out.println(msj);
            }
        }
        clearMetrics();
    }

    private void cerrarAbierta() {
        if (!metricas.isEmpty() && metricas.getLast().fin == null) {
            endMetric();
        }
    }

    private String mensaje(Metrica m) {
        long transcurrido = m.fin - m.inicio;
        if (timeUnit == TimeUnit.SECONDS) {
            transcurrido /= 1000L;
        }
        return String.format("%s, tardo: %d(%s)", m.flag, transcurrido, m.unidad);
    }
}
