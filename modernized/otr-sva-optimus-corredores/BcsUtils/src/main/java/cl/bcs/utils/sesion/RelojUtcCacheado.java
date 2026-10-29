package cl.bcs.utils.sesion;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.LongSupplier;

/**
 * RULE-071: the database UTC clock, read at most once per hour (legacy {@code SesionUtils.java:83-108}). The value is
 * the cached database time plus the local time elapsed since the read, at millisecond precision.
 *
 * <p>Legacy behavior kept as pinned: the cache is refreshed when its age is strictly over one hour
 * ({@link #VIGENCIA_MS}); a {@code null} read poisons the cache for that hour ({@code java.lang.Exception} on every
 * call); a failing read is not cached and is retried on the next call. The legacy cache was JVM-wide and
 * unsynchronized; here it is one thread-safe instance per database, and the first call always reads.
 */
public final class RelojUtcCacheado {

    public static final long VIGENCIA_MS = 3_600_000L;

    private final LongSupplier relojMs;
    private final Callable<Timestamp> consultaUtc;
    private Timestamp cache;
    private Long ultimaLecturaMs;

    /**
     * @param relojMs     local millisecond clock (for example {@code System::currentTimeMillis})
     * @param consultaUtc reads the database UTC time (for example {@code SELECT CURRENT_UTCTIMESTAMP FROM DUMMY})
     */
    public RelojUtcCacheado(LongSupplier relojMs, Callable<Timestamp> consultaUtc) {
        this.relojMs = Objects.requireNonNull(relojMs, "relojMs");
        this.consultaUtc = Objects.requireNonNull(consultaUtc, "consultaUtc");
    }

    public synchronized Timestamp ahoraUtc() throws Exception {
        if (ultimaLecturaMs == null || relojMs.getAsLong() - ultimaLecturaMs > VIGENCIA_MS) {
            cache = null;
            cache = consultaUtc.call();
            ultimaLecturaMs = relojMs.getAsLong();
        }
        if (cache == null) {
            throw new Exception("No se puede obtener cache UTC");
        }
        int transcurrido = (int) (relojMs.getAsLong() - ultimaLecturaMs);
        return new Timestamp(cache.getTime() + transcurrido);
    }
}
