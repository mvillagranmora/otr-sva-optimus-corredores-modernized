package cl.bcs.fondomutuo.cierre;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * The date conversions of legacy {@code cl.bcs.utils.Fecha} that the closure of step 3A uses, on {@link LocalDate} so that no JVM time zone takes part
 * (DEV-F2 / D-FEC-6: under the UTC of the container the legacy {@code java.sql.Date} + {@code Calendar} arithmetic gives the same days).
 * <ul>
 *   <li>{@link #aFecha}: {@code Fecha.toDate(String)} - 19 characters are {@code yyyy-MM-dd HH:mm:ss}, anything else {@code yyyy-MM-dd} (same formatters,
 *       same SMART resolution, same exceptions: {@code null} is a NullPointerException, bad text a DateTimeParseException);</li>
 *   <li>{@link #aTexto}: {@code Fecha.toStringDate(Date)} - {@code yyyy-MM-dd}, {@code null} is the empty text.</li>
 * </ul>
 */
public final class FechaLegacy {

    private static final DateTimeFormatter DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FechaLegacy() {
    }

    public static LocalDate aFecha(String fecha) {
        return fecha.length() == 19 ? LocalDate.parse(fecha, SD) : LocalDate.parse(fecha, DD);
    }

    /** Legacy {@code Fecha.toDate(String)} as a {@code java.sql.Date} (step 3B / 4A rows keep the legacy type); zone independent. */
    public static java.sql.Date aFechaSql(String fecha) {
        return java.sql.Date.valueOf(aFecha(fecha));
    }

    /** Timestamp text of legacy {@code Fecha.toStringTimeStamp}: {@code yyyy-MM-dd HH:mm:ss.SSS}. */
    public static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Legacy {@code Fecha.stringToSqlDate(java.util.Date)}: {@code yyyy-MM-dd} of the instant in the JVM zone (a {@code java.sql.Date} built by the
     * JDBC driver or by {@code Date.valueOf} is midnight of that zone, so the text is the date itself); a null date is a NullPointerException.
     */
    public static String texto(java.util.Date date) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * {@code java.sql.Date.valueOf(String)}: exactly {@code yyyy-[m]m-[d]d} (anything else, and {@code null}, is an IllegalArgumentException) and as lenient as
     * the JDBC class (an impossible day rolls over to the next month).
     */
    public static LocalDate fechaSql(String fecha) {
        return java.sql.Date.valueOf(fecha).toLocalDate();
    }

    public static String aTexto(LocalDate fecha) {
        return fecha != null ? fecha.format(DD) : "";
    }

    /** {@code Calendar.get(DAY_OF_WEEK)}: Sunday 1, Monday 2 ... Saturday 7. */
    public static Long diaDeSemanaCalendar(LocalDate fecha) {
        return (long) (fecha.getDayOfWeek().getValue() % 7) + 1;
    }

    /** The "optimus" day of the date helpers: Monday 0 ... Saturday 5, Sunday 6 (legacy: Calendar day 1 is 6, otherwise day - 2). */
    public static int diaOptimus(LocalDate fecha) {
        return fecha.getDayOfWeek() == DayOfWeek.SUNDAY ? 6 : fecha.getDayOfWeek().getValue() - 1;
    }
}
