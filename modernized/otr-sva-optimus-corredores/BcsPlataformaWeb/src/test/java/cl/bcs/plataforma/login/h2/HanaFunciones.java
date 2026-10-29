package cl.bcs.plataforma.login.h2;

import java.sql.Timestamp;

/** H2 implementations (CREATE ALIAS in schema-login.sql) of the HANA functions the login SQL uses: {@code ADD_SECONDS(ts, n)} and {@code TO_INT(x)}. */
public final class HanaFunciones {

    private HanaFunciones() {
    }

    public static Timestamp addSeconds(Timestamp t, Long segundos) {
        return t == null || segundos == null ? null : new Timestamp(t.getTime() + segundos * 1000L);
    }

    public static Integer toInt(String v) {
        return v == null ? null : new java.math.BigDecimal(v.trim()).intValue();
    }

    /** HANA {@code TO_VARCHAR(date, 'YYYY-MM-DD')}: the only format the bootstrap uses. */
    public static String toVarchar(java.time.LocalDate fecha, String formato) {
        if (fecha == null) return null;
        if (!"YYYY-MM-DD".equals(formato)) throw new IllegalArgumentException("format not supported by the test double: " + formato);
        return fecha.toString();
    }
}
