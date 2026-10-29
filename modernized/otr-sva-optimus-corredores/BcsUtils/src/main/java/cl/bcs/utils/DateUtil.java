package cl.bcs.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * UTIL-30..32: {@link Date} helpers in the JVM default zone (legacy {@code DateUtil.java:34-239}).
 *
 * <p>The API is {@code java.util.Date}/{@link Calendar} by contract (BcsNormativaCore calls {@link #getDate(String)}
 * and catches {@link DateFormatException}), and the golden records compare exact epoch millis in America/Santiago
 * across DST edges and pre-Gregorian dates. That is why the arithmetic stays on {@link Calendar} (Julian/Gregorian
 * hybrid, lenient field resolution) instead of {@code java.time}, which can resolve those inputs differently.
 *
 * <ul>
 *   <li>UTIL-30 {@link #getDate(String)}: four shapes ({@code d-M-yyyy}, {@code yyyy-M-d}, {@code d/M/yyyy},
 *       {@code yyyy/M/d}), strict calendar; {@code null}/{@code ""} give {@code null}; anything else throws
 *       {@link DateFormatException}. {@link #getDate(int, int, int)} is lenient and its month is 0-based.</li>
 *   <li>UTIL-31 start/end of day and month, day/month/year getters.</li>
 *   <li>UTIL-32 {@link #addDays(Date, int)}: calendar days (keeps the wall-clock time across DST).</li>
 * </ul>
 */
public final class DateUtil {

    private record Formato(Pattern forma, String patron) {
    }

    private static final List<Formato> FORMATOS = List.of(
            new Formato(Pattern.compile("^\\d{1,2}-\\d{1,2}-\\d{4}$"), "dd-MM-yyyy"),
            new Formato(Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$"), "yyyy-MM-dd"),
            new Formato(Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}$"), "dd/MM/yyyy"),
            new Formato(Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2}$"), "yyyy/MM/dd"));

    private static final Locale ES = Locale.of("es");

    private DateUtil() {
    }

    /** UTIL-30: parses one of the four accepted shapes, strictly, in the default zone. */
    public static Date getDate(String val) {
        if (val == null || val.isEmpty()) {
            return null;
        }
        String patron = FORMATOS.stream()
                .filter(f -> f.forma().matcher(val).matches())
                .map(Formato::patron)
                .findFirst()
                .orElseThrow(() -> new DateFormatException(mensaje(val)));
        SimpleDateFormat df = new SimpleDateFormat(patron, ES);
        df.setLenient(false);
        try {
            return df.parse(val);
        } catch (ParseException ex) {
            throw new DateFormatException(mensaje(val), ex);
        }
    }

    /** UTIL-30: {@code getDate(val + "-01")}, so {@code "2024-03"} is 2024-03-01 and {@code null} throws. */
    public static Date getAnoMes(String val) {
        return getDate(val + "-01");
    }

    /** UTIL-30: lenient; {@code month} is 0-based ({@code 12} is January of the next year). */
    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return new Date(cal.getTimeInMillis());
    }

    /** A default-zone, default-locale {@link Calendar} set to {@code date}. */
    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /** UTIL-32: adds calendar days (DST-aware: the local time of day is kept). */
    public static Date addDays(Date date, int days) {
        return ajustar(date, cal -> cal.add(Calendar.DAY_OF_MONTH, days));
    }

    /** UTIL-31: the day of month. */
    public static int getDay(Date date) {
        return getCalendar(date).get(Calendar.DATE);
    }

    /** UTIL-31: the 0-based month. */
    public static int getMonth(Date date) {
        return getCalendar(date).get(Calendar.MONTH);
    }

    /** UTIL-31: the year. */
    public static int getYear(Date date) {
        return getCalendar(date).get(Calendar.YEAR);
    }

    /** UTIL-31: 00:00:00.000 of the same local day (01:00 on a day without midnight); {@code null} gives {@code null}. */
    public static Date getStartOfDay(Date date) {
        return date == null ? null : ajustar(date, cal -> horaDelDia(cal, 0, 0, 0, 0));
    }

    /** UTIL-31: 23:59:59.999 of the same local day; {@code null} gives {@code null}. */
    public static Date getEndOfDay(Date date) {
        return date == null ? null : ajustar(date, cal -> horaDelDia(cal, 23, 59, 59, 999));
    }

    /** UTIL-31: 00:00:00.000 of the first day of the month; {@code null} gives {@code null}. */
    public static Date getStartOfMonth(Date date) {
        return date == null ? null : ajustar(date, cal -> {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            horaDelDia(cal, 0, 0, 0, 0);
        });
    }

    private static Date ajustar(Date date, Consumer<Calendar> cambio) {
        Calendar cal = getCalendar(date);
        cambio.accept(cal);
        return cal.getTime();
    }

    private static void horaDelDia(Calendar cal, int hora, int minuto, int segundo, int milis) {
        cal.set(Calendar.HOUR_OF_DAY, hora);
        cal.set(Calendar.MINUTE, minuto);
        cal.set(Calendar.SECOND, segundo);
        cal.set(Calendar.MILLISECOND, milis);
    }

    private static String mensaje(String val) {
        return "Formato de fecha invalido: '" + val + "'";
    }
}
