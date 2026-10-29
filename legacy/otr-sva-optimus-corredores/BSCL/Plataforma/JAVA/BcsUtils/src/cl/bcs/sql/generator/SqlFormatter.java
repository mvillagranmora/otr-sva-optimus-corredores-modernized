package cl.bcs.sql.generator;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * M&eacute;todos utiles para formatear datos como texto SQL.
 *
 * @author fpino@email.com
 *
 */
public final class SqlFormatter {

    /**
     * Formatea {@link BigDecimal} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link BigDecimal} como texto SQL.
     */
    public static String format(final BigDecimal value) {
        return value.toString();
    }

    /**
     * Formatea {@link Character} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link Character} como texto SQL.
     */
    public static String format(final Character value) {
        return format(value.toString());
    }

    /**
     * Formatea {@link Date} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link Date} como texto SQL.
     */
    public static String format(final Date value) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", new Locale("es"));
        return "'" + df.format(value) + "'";
    }

    /**
     * Formatea {@link Double} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link Double} como texto SQL.
     */
    public static String format(final Double value) {
        return value.toString();
    }

    /**
     * Formatea {@link Integer} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link Integer} como texto SQL.
     */
    public static String format(final Integer value) {
        return value.toString();
    }

    /**
     * Formatea {@link Long} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link Long} como texto SQL.
     */
    public static String format(final Long value) {
        return value.toString();
    }

    /**
     * Formatea {@link Object} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link Object} como texto SQL.
     */
    public static String format(final Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof BigDecimal) {
            return format((BigDecimal) value);
        } else if (value instanceof Character) {
            return format((Character) value);
        } else if (value instanceof Date) {
            return format((Date) value);
        } else if (value instanceof Double) {
            return format((Double) value);
        } else if (value instanceof Integer) {
            return format((Integer) value);
        } else if (value instanceof Long) {
            return format((Long) value);
        } else if (value instanceof String) {
            return format((String) value);
        }

        throw new IllegalArgumentException(
                "Tipo de objeto no soportado: " + value.getClass().getName());
    }

    /**
     * Formatea {@link String} como texto SQL.
     *
     * @param value
     *            El valor.
     * @return {@link String} como texto SQL.
     */
    public static String format(final String value) {
        return "'" + value.replaceAll("'", "''") + "'";
    }

    private SqlFormatter() {
    }

}
