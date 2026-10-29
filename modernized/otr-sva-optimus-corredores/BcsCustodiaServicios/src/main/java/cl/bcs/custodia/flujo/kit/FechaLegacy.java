package cl.bcs.custodia.flujo.kit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The date conversions of legacy {@code cl.bcs.utils.Fecha} that ConsultarFlujoCustodia uses: {@code toDate} (19 characters are
 * {@code yyyy-MM-dd HH:mm:ss}, anything else {@code yyyy-MM-dd}; same formatters, same exceptions), {@code toTimeStamp}, {@code toFormat} and
 * {@code stringToSqlDate} (SimpleDateFormat in the JVM zone). CUS-FLU-05, CUS-FLU-06, CUS-FLU-33.
 */
public final class FechaLegacy {

    private static final DateTimeFormatter DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private FechaLegacy() {
    }

    public static LocalDate aFecha(String fecha) {
        return fecha.length() == 19 ? LocalDate.parse(fecha, SD) : LocalDate.parse(fecha, DD);
    }

    /** Legacy {@code Fecha.toDate(String)}. */
    public static java.sql.Date aFechaSql(String fecha) {
        return java.sql.Date.valueOf(aFecha(fecha));
    }

    /** Legacy {@code Fecha.toTimeStamp(String)}: 19 characters are {@code yyyy-MM-dd HH:mm:ss}, otherwise {@code yyyy-MM-dd HH:mm:ss.SSS}. */
    public static java.sql.Timestamp aTimestamp(String fecha) {
        return java.sql.Timestamp.valueOf(LocalDateTime.parse(fecha, fecha.length() == 19 ? SD : TS));
    }

    public static String aTexto(LocalDate fecha) {
        return fecha != null ? fecha.format(DD) : "";
    }

    /** Legacy {@code Fecha.toFormat(date, pattern)} (SimpleDateFormat, JVM zone). */
    public static String formato(java.util.Date fecha, String patron) {
        return new java.text.SimpleDateFormat(patron).format(fecha);
    }

    /** Legacy {@code Fecha.stringToSqlDate(Date)}: {@code yyyy-MM-dd} of the instant in the JVM zone. */
    public static String texto(java.util.Date fecha) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(fecha);
    }

    /** The "optimus" day of the date helpers: Monday 0 ... Saturday 5, Sunday 6. */
    public static int diaOptimus(LocalDate fecha) {
        return fecha.getDayOfWeek() == DayOfWeek.SUNDAY ? 6 : fecha.getDayOfWeek().getValue() - 1;
    }
}
