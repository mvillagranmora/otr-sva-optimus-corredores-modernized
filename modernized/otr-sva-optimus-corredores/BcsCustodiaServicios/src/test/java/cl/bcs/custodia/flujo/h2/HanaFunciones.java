package cl.bcs.custodia.flujo.h2;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * H2 implementations (CREATE ALIAS in schema-session.sql) of the HANA SQL functions used by the 29 FlujoCustodia_*.sql. SINGLE SOURCE, copied
 * byte for byte into the module tests by legacy-oracle/sync.py. Each one is an ASSUMED equivalence (QA HANA unreachable on 2026-10-25):
 * <ul>
 * <li>TO_BIGINT(x): NULL stays NULL, the value is read as a number and truncated to its integral part (same as FondoMutuo's H2Fn.toBigint).</li>
 * <li>HANA_TO_DATE(x) = HANA TO_DATE(x) with the default format: the first 10 characters (YYYY-MM-DD) of the text form; NULL stays NULL.</li>
 * <li>HANA_TO_DECIMAL(x) = HANA TO_DECIMAL(x) without precision: the same numeric value with the scale of its text form (0 -> 0).</li>
 * <li>HANA_TO_DECIMAL(x, p, s) = HANA TO_DECIMAL(x, p, s): the value at scale s (HALF_UP).</li>
 * </ul>
 */
public final class HanaFunciones {

    private HanaFunciones() {
    }

    public static Long toBigint(String v) {
        return v == null ? null : new BigDecimal(v.trim()).longValue();
    }

    public static java.sql.Date toDate(String v) {
        if (v == null) return null;
        String t = v.trim();
        return java.sql.Date.valueOf(t.length() > 10 ? t.substring(0, 10) : t);
    }

    public static BigDecimal toDecimal(String v) {
        return v == null ? null : new BigDecimal(v.trim());
    }

    public static BigDecimal toDecimal(String v, Integer precision, Integer scale) {
        return v == null ? null : new BigDecimal(v.trim()).setScale(scale, RoundingMode.HALF_UP);
    }
}
