package cl.bcs.jobmanager.service;

import cl.bcs.jobmanager.domain.Agenda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Pure-logic service for diary (agenda) scheduling decisions.
 *
 * All methods are static to allow testing without a Spring context.
 */
public final class DiaryService {

    private static final Logger log = LoggerFactory.getLogger(DiaryService.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DiaryService() {}

    /**
     * Returns true if the agenda entry should trigger a new program.
     *
     * Schedule triggers when:
     *   - Both nextExecution and lastExecution are non-null
     *   - nextExecution &lt;= lastExecution (the scheduled time has passed)
     *
     * This mirrors the legacy compararFechasConDate semantics.
     *
     * @param agenda the agenda entry to check
     * @return true if the agenda should create a new program
     */
    public static boolean shouldTrigger(Agenda agenda) {
        Objects.requireNonNull(agenda, "agenda must not be null");

        String next = agenda.getSiguienteFechaEjecucion();
        String last = agenda.getUltimaFechaEjecucion();

        if (next == null || last == null) {
            return false;
        }

        return compareDates(next, last);
    }

    /**
     * Returns true if fecha1 (next execution) is before or equal to
     * fechaActual (last execution), meaning the schedule is due.
     *
     * @param fecha1       the "next execution" timestamp (yyyy-MM-dd HH:mm:ss)
     * @param fechaActual  the "last execution" timestamp (yyyy-MM-dd HH:mm:ss)
     * @return true if scheduled time has passed
     */
    public static boolean compareDates(String fecha1, String fechaActual) {
        Objects.requireNonNull(fecha1, "fecha1 must not be null");
        Objects.requireNonNull(fechaActual, "fechaActual must not be null");

        try {
            SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
            Date d1 = fmt.parse(fecha1);
            Date d2 = fmt.parse(fechaActual);
            return !d1.after(d2);   // true when d1 <= d2
        } catch (Exception e) {
            log.error("compareDates parse error — fecha1='{}' fechaActual='{}': {}",
                    fecha1, fechaActual, e.getMessage());
            return false;
        }
    }

    /**
     * Returns true if the agenda entry is a daemon (continuous job).
     *
     * @param agenda the agenda entry
     * @return true if EsDemonio = 'S'
     */
    public static boolean isDaemon(Agenda agenda) {
        Objects.requireNonNull(agenda, "agenda must not be null");
        return "S".equalsIgnoreCase(agenda.getEsDemonio());
    }

    /**
     * Returns true if the agenda entry is enabled.
     *
     * @param agenda the agenda entry
     * @return true if Habilitado = 'S'
     */
    public static boolean isEnabled(Agenda agenda) {
        Objects.requireNonNull(agenda, "agenda must not be null");
        return "S".equalsIgnoreCase(agenda.getHabilitado());
    }
}
