package cl.bcs.oracle;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** H2 implementations (registered with CREATE ALIAS in schema-session.sql) of the HANA SQL functions used by the in-scope SQL. */
public final class H2Fn {
    private H2Fn() { }

    /** HANA ADD_SECONDS(timestamp, seconds) */
    public static Timestamp addSeconds(Timestamp t, Long seconds) {
        if (t == null || seconds == null) return null;
        return new Timestamp(t.getTime() + seconds * 1000L);
    }

    /** HANA TO_BIGINT(value): NULL stays NULL, text is parsed (HANA raises an error on non numeric text; H2 does too) */
    public static Long toBigint(String v) {
        return v == null ? null : new BigDecimal(v.trim()).longValue();
    }

    /** HANA TO_NVARCHAR(timestamp, 'YYYY-MM-DD HH24:MI:SS') limited to that numeric pattern family */
    public static String toNvarchar(Timestamp t, String fmt) {
        if (t == null) return null;
        java.time.LocalDateTime l = t.toLocalDateTime();
        String f = fmt.replace("YYYY", "yyyy").replace("DD", "dd").replace("HH24", "HH").replace("MI", "mm").replace("SS", "ss");
        return l.format(java.time.format.DateTimeFormatter.ofPattern(f));
    }
}
