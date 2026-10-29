package cl.bcs.jobmanager.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * In-memory cooldown per IdPrograma for programs whose delivery was rejected with 404, 422 or 503 (configuration or
 * data inconsistency): without it they would be re-posted and re-logged at every poll. Nothing is written to the
 * database. Entries leave when the cooldown expires, when the program is no longer in the eligible set, or (bounded
 * size) by evicting the oldest. A zero or negative duration disables the cooldown.
 */
public class EnfriamientoNoEntregable {

    /** Default bound on tracked programs. */
    public static final int MAX_POR_DEFECTO = 1_000;

    /** Answer of {@link #consultar}. */
    public enum Consulta { LIBRE, EN_ENFRIAMIENTO, EN_ENFRIAMIENTO_PRIMERA_OMISION }

    private static final class Entrada {
        final Instant hasta;
        boolean avisada;

        Entrada(Instant hasta) {
            this.hasta = hasta;
        }
    }

    private final Duration duracion;
    private final int maximo;
    private final Clock reloj;
    private final Map<Long, Entrada> entradas = new LinkedHashMap<>();

    public EnfriamientoNoEntregable(Duration duracion, int maximo, Clock reloj) {
        this.duracion = duracion == null ? Duration.ZERO : duracion;
        this.maximo = Math.max(1, maximo);
        this.reloj = reloj;
    }

    public boolean habilitado() {
        return !duracion.isZero() && !duracion.isNegative();
    }

    /** Starts (or restarts) the cooldown of the program. No-op when disabled. */
    public synchronized void registrar(long idPrograma) {
        if (!habilitado()) {
            return;
        }
        Instant ahora = reloj.instant();
        entradas.remove(idPrograma);
        if (entradas.size() >= maximo) {
            entradas.values().removeIf(e -> !e.hasta.isAfter(ahora));
        }
        while (entradas.size() >= maximo) {
            Iterator<Long> it = entradas.keySet().iterator();
            it.next();
            it.remove();
        }
        entradas.put(idPrograma, new Entrada(ahora.plus(duracion)));
    }

    /** Whether the program is cooling down; an expired entry is removed and answers LIBRE. */
    public synchronized Consulta consultar(long idPrograma) {
        Entrada e = entradas.get(idPrograma);
        if (e == null) {
            return Consulta.LIBRE;
        }
        if (!e.hasta.isAfter(reloj.instant())) {
            entradas.remove(idPrograma);
            return Consulta.LIBRE;
        }
        if (!e.avisada) {
            e.avisada = true;
            return Consulta.EN_ENFRIAMIENTO_PRIMERA_OMISION;
        }
        return Consulta.EN_ENFRIAMIENTO;
    }

    /** Drops the entries of programs that are not in the eligible set any more. */
    public synchronized void depurar(Set<Long> elegibles) {
        entradas.keySet().removeIf(id -> !elegibles.contains(id));
    }

    public synchronized int tamano() {
        return entradas.size();
    }
}
