package cl.bcs.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * UTIL-34 / UTIL-35: {@code java.sql} date-time formatting, parsing, offset conversion and arithmetic (legacy
 * {@code Fecha.java:12-335}; 658 legacy call sites, so the static API is kept). The debug {@code main()} is not ported
 * (RULE-076).
 *
 * <p>Legacy behavior kept as the golden records pin it:
 * <ul>
 *   <li>The local offset is ONE JVM-wide value in seconds ({@link #setLocalOffSet(int)}); until it is set, every offset
 *       conversion throws {@link NullPointerException}.</li>
 *   <li>Text formats: {@code yyyy-MM-dd[ HH:mm:ss[.SSS]]} with the {@code java.time} SMART resolver; a 19-character
 *       text is read as date-time. Caller-supplied patterns ({@code toFormat}, {@code toLocalFormat}) are
 *       {@link SimpleDateFormat} patterns in the default zone and locale.</li>
 *   <li>Second/hour/millisecond arithmetic is exact elapsed time; day arithmetic is calendar days in the default zone
 *       (wall-clock time kept across DST). Both return new values without sub-millisecond nanos.</li>
 * </ul>
 */
public final class Fecha {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int LARGO_FECHA_HORA = 19;

    public static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy");
    public static final String DEFAULT_DATE_TIME = "0001-01-01 00:00:00.000";
    public static final String DEFAULT_TIME = "00:00:00.000";

    /** JVM-wide local offset in seconds; {@code null} until {@link #setLocalOffSet(int)} (legacy Fecha.java:13). */
    private static volatile Integer offSetLocal;

    private Fecha() {
    }

    // ------------------------------------------------------------------ UTIL-34: local offset

    /** Sets the JVM-wide local offset, in seconds. */
    public static void setLocalOffSet(int offset) {
        offSetLocal = offset;
    }

    /** The JVM-wide local offset in seconds, or {@code null} if it was never set. */
    public static Integer getLocalOffSet() {
        return offSetLocal;
    }

    private static int offset() {
        return Objects.requireNonNull(offSetLocal, "Fecha.setLocalOffSet was never called");
    }

    public static Timestamp toLocalTimeStamp(Timestamp date) {
        return addSeconds(date, offset());
    }

    public static Timestamp toUTCTimeStamp(Timestamp date) {
        return substractSeconds(date, offset());
    }

    /** Adds the offset as {@link LocalTime} arithmetic, so it wraps around midnight. */
    public static Time toLocalTime(Time time) {
        return Time.valueOf(time.toLocalTime().plusSeconds(offset()));
    }

    /** {@code date} shifted by the offset, formatted with a {@link SimpleDateFormat} pattern. */
    public static String toLocalFormat(Timestamp date, String format) {
        return new SimpleDateFormat(format).format(addSeconds(date, offset()));
    }

    /** System date + local time text to a UTC timestamp. */
    public static Timestamp fechaSistemaHoraLocal_fechaHoraUTC(String fechaSistema, String horaLocal) {
        return toUTCTimeStamp(toDateTime(fechaSistema + " " + toStringTime(toTime(horaLocal))));
    }

    /** System date + UTC time to a UTC timestamp (the time goes through local and back, as in the legacy). */
    public static Timestamp fechaSistemaHoraUTC_fechaHoraUTC(String fechaSistema, Time horaUTC) {
        return toUTCTimeStamp(toDateTime(fechaSistema + " " + toStringTime(toLocalTime(horaUTC))));
    }

    // ------------------------------------------------------------------ UTIL-34: formatting

    public static String toFormat(Timestamp date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static String toFormat(java.sql.Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /** {@code yyyy-MM-dd} of any {@link Date} in the default zone (the legacy name is kept although it returns text). */
    public static String stringToSqlDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /** {@code yyyy-MM-dd HH:mm:ss.SSS} (nanos truncated to millis). */
    public static String toStringTimeStamp(Timestamp timestamp) {
        return timestamp.toLocalDateTime().format(TS);
    }

    /** {@code yyyy-MM-dd HH:mm:ss}. */
    public static String toStringDatetime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().format(SD);
    }

    /** {@code yyyy-MM-dd}, or {@code ""} for {@code null}. */
    public static String toStringDate(java.sql.Date date) {
        return toStringDate(date, "");
    }

    /** {@code yyyy-MM-dd}, or {@code defaultDate} for {@code null}. */
    public static String toStringDate(java.sql.Date date, String defaultDate) {
        return date != null ? date.toLocalDate().format(DD) : defaultDate;
    }

    /** {@code yyyy-MM-dd} of a timestamp ({@code null} throws). */
    public static String toStringDate(Timestamp date) {
        return date.toLocalDateTime().format(DD);
    }

    /** {@code HH:mm:ss}. */
    public static String toStringTime(Time time) {
        return time.toLocalTime().format(TT);
    }

    /** {@code HH:mm:ss} of a timestamp. */
    public static String toStringTime(Timestamp time) {
        return time.toLocalDateTime().format(TT);
    }

    // ------------------------------------------------------------------ UTIL-34: parsing

    /** {@code yyyy-MM-dd}, or the date part of a 19-character {@code yyyy-MM-dd HH:mm:ss}. */
    public static java.sql.Date toDate(String date) {
        return java.sql.Date.valueOf(LocalDate.parse(date, date.length() == LARGO_FECHA_HORA ? SD : DD));
    }

    /** {@code yyyy-MM-dd} of a {@link Date} in the default zone, as {@code java.sql.Date}. */
    public static java.sql.Date utilDateToSqlDate(Date date) {
        return java.sql.Date.valueOf(LocalDate.parse(stringToSqlDate(date), DD));
    }

    /** {@code yyyy-MM-dd HH:mm:ss} (19 characters) or any other text read as {@code <text> 00:00:00}. */
    public static Timestamp toDateTime(String date) {
        String texto = date.length() == LARGO_FECHA_HORA ? date : date + " 00:00:00";
        return Timestamp.valueOf(LocalDateTime.parse(texto, SD));
    }

    /** {@code yyyy-MM-dd HH:mm:ss} (19 characters) or {@code yyyy-MM-dd HH:mm:ss.SSS}. */
    public static Timestamp toTimeStamp(String date) {
        return Timestamp.valueOf(LocalDateTime.parse(date, date.length() == LARGO_FECHA_HORA ? SD : TS));
    }

    /** {@code HH:mm:ss}. */
    public static Time toTime(String time) {
        return Time.valueOf(LocalTime.parse(time, TT));
    }

    // ------------------------------------------------------------------ UTIL-35: arithmetic

    public static Timestamp addSeconds(Timestamp date, int secs) {
        return desplazar(date, secs * 1000L);
    }

    public static Timestamp substractSeconds(Timestamp date, int secs) {
        return addSeconds(date, -secs);
    }

    public static Timestamp addHours(Timestamp date, int hours) {
        return desplazar(date, hours * 3_600_000L);
    }

    public static Timestamp addMilliseconds(Timestamp date, int millisecs) {
        return desplazar(date, millisecs);
    }

    private static Timestamp desplazar(Timestamp date, long millis) {
        return new Timestamp(date.getTime() + millis);
    }

    public static java.sql.Date addDays(java.sql.Date date, int days) {
        return new java.sql.Date(masDias(date, days));
    }

    public static Timestamp addDays(Timestamp date, int days) {
        return new Timestamp(masDias(date, days));
    }

    public static java.sql.Date substractDays(java.sql.Date date, int days) {
        return addDays(date, -days);
    }

    public static Timestamp substractDays(Timestamp date, int days) {
        return addDays(date, -days);
    }

    /** Calendar days in the default zone (legacy Calendar.add(DAY_OF_WEEK, n), which adds days). */
    private static long masDias(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTimeInMillis();
    }
}
