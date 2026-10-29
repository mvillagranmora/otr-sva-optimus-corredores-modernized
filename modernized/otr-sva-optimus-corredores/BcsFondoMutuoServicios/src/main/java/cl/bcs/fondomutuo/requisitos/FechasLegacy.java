package cl.bcs.fondomutuo.requisitos;

import java.sql.Date;
import java.time.format.DateTimeFormatter;

import cl.bcs.fondomutuo.cierre.FechaLegacy;

/**
 * The pieces of legacy {@code cl.bcs.utils.Fecha} used by step 3B. Step 4A merged them into the shared {@code cierre.FechaLegacy} (same formatters, same
 * results, nothing here keeps state: DEV-F2); this class delegates and keeps its names for the 3B callers.
 */
public final class FechasLegacy {

    public static final DateTimeFormatter TS = FechaLegacy.TS;

    private FechasLegacy() {
    }

    /** Legacy {@code Fecha.toDate(String)}: 19 characters are a date-time (its date part), anything else must be yyyy-MM-dd. */
    public static Date toDate(String date) {
        return FechaLegacy.aFechaSql(date);
    }
}
