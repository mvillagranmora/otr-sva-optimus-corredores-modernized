package cl.bcs.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * M&eacute;todos utiles para el manejo de {@link Date}.
 *
 * @author fpino@email.com
 *
 */
public final class DateUtil {

    /**
     * {@link Map} de los patrones de formato soportados para la
     * interpretaci&oacute;n de {@link Date}. Relaciona el patr&oacute;n de una
     * expresi&oacute;n regular con el correspondiente formato.
     */
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new ConcurrentHashMap<>();

    static {
        DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        DATE_FORMAT_REGEXPS.put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "dd/MM/yyyy");
        DATE_FORMAT_REGEXPS.put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
    }

    public static Date addDays(
            final Date date,
            final int days) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * Obtiene fecha como primer d&iacute;a del mes.
     *
     * @param val
     *            El {@link String} en formato yyyy-MM.
     * @return Fecha como primer d&iacute;a del mes.
     */
    public static Date getAnoMes(
            final String val) {
        return getDate(val + "-01");
    }

    public static Calendar getCalendar(
            final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * Obtiene fecha a partir de los argumentos.
     *
     * @param year
     *            El a&ntilde;o.
     * @param month
     *            El mes
     * @param day
     *            El d&iacute;a.
     * @return La fecha a partir de los argumentos.
     */
    public static Date getDate(
            final int year,
            final int month,
            final int day) {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return new Date(cal.getTime().getTime());
    }

    /**
     * Obtiene instancia de {@link Date} interpretando el {@link String} dado.
     * <p>
     * Los patrones soportados son:
     * <ul>
     * <li><code>dd-MM-yyyy</code></li>
     * <li><code>yyyy-MM-dd</code></li>
     * <li><code>dd/MM/yyyy</code></li>
     * <li><code>yyyy/MM/dd</code></li>
     * </ul>
     *
     * @param val
     *            El {@link String} dado.
     * @return Instancia de {@link Date} interpretando el {@link String} dado.
     */
    public static Date getDate(
            final String val) {
        if (val == null || val.isEmpty()) {
            return null;
        }

        try {
            final String format = getDateFormat(val);
            final DateFormat df = new SimpleDateFormat(format, new Locale("es"));
            df.setLenient(false);
            return df.parse(val);
        } catch (final ParseException ex) {
            throw new DateFormatException("Formato de fecha invalido: '" + val + "'", ex);
        }
    }

    /**
     * Obtiene el formato seg&uacute;n el patr&oacute;n del valor de entrada.
     *
     * @param val
     *            Valor de entrada.
     * @return El formato seg&uacute;n el patr&oacute;n del valor de entrada.
     */
    private static String getDateFormat(
            final String val) {
        for (final String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (val.matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }

        throw new DateFormatException("Formato de fecha invalido: '" + val + "'");
    }

    /**
     * Returns the date corresponding to the input Date
     *
     * @param date
     *            Date Input
     *
     * @return Day
     */
    public static int getDay(
            final Date date) {
        return getCalendar(date).get(Calendar.DATE);
    }

    /**
     * Obtiene la fecha/hora de fin del d&iacute;a.
     *
     * @param date
     *            La fecha.
     * @return La fecha/hora de fin del d&iacute;a.
     */
    public static Date getEndOfDay(
            final Date date) {
        if (date == null) {
            return null;
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * Returns the month corresponding to the input Date. 0 => January, and 11
     * => December
     *
     * @param date
     *            Date Input
     *
     * @return Month
     */
    public static int getMonth(
            final Date date) {
        return getCalendar(date).get(Calendar.MONTH);
    }

    /**
     * Obtiene la fecha/hora de inicio del d&iacute;a.
     *
     * @param date
     *            La fecha.
     * @return La fecha/hora de inicio del d&iacute;a.
     */
    public static Date getStartOfDay(
            final Date date) {
        if (date == null) {
            return null;
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Obtiene la fecha/hora al comienzo del mes.
     *
     * @param date
     *            La fecha.
     * @return La fecha/hora al comienzo del mes.
     */
    public static Date getStartOfMonth(
            final Date date) {
        if (date == null) {
            return null;
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns the year corresponding to the input Date.
     *
     * @param date
     *            Date Input
     *
     * @return Year
     */
    public static int getYear(
            final Date date) {
        return getCalendar(date).get(Calendar.YEAR);
    }

    private DateUtil() {
    }

}
