package cl.bcs.cuentainversion.reportes.support;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SAP HANA scalar functions used by the production SQL, exposed to H2 through {@code CREATE ALIAS} in
 * schema-reportes.sql. They are test infrastructure shared by the oracle and the modern tests; they model HANA, they
 * are not part of either implementation.
 */
public final class H2Functions {

    private H2Functions() {
    }

    /**
     * HANA {@code TO_DATE(x)}: DATE of a date, timestamp or 'yyyy-MM-dd[ ...]' value; NULL stays NULL. The parameter is declared
     * String because an H2 alias parameter typed Object becomes JAVA_OBJECT and rejects bound text; H2 renders dates and
     * timestamps as ISO text, from which the first 10 characters are the date.
     */
    public static Date toDate(String v) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return Date.valueOf(s.length() > 10 ? s.substring(0, 10) : s);
    }

    /** HANA {@code CURRENT_UTCTIMESTAMP}: now in UTC as a TIMESTAMP. */
    public static Timestamp currentUtcTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now(java.time.ZoneOffset.UTC));
    }

    /** HANA {@code CURRENT_UTCDATE}. */
    public static Date currentUtcDate() {
        return Date.valueOf(LocalDate.now(java.time.ZoneOffset.UTC));
    }

    /** HANA {@code TO_NVARCHAR(x)}: the text of the value (H2 renders dates as yyyy-MM-dd). String parameter, see {@link #toDate}. */
    public static String toNvarchar(String v) {
        return v;
    }

    /** HANA {@code TO_DATE(x, 'YYYY-MM-DD')}: the only format used by the SQL in this module; same as {@link #toDate(String)}. */
    public static Date toDate(String v, String formato) {
        return toDate(v);
    }

    /** HANA {@code TO_VARCHAR(x, 'YYYY-MM-DD')}: the date part as text (the only format used by the SQL in this module). */
    public static String toVarchar(String v, String formato) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return s.length() > 10 ? s.substring(0, 10) : s;
    }
}
