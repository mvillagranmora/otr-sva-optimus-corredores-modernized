package cl.bcs.oracle;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * H2 implementations (registered with CREATE ALIAS in schema-det.sql) of the two HANA SQL functions that only the SQL of the delegate of ConsultarDetalleSolicitud
 * (ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql) uses and that cl.bcs.oracle.H2Fn (shared, NOT modified) does not have. A COPY with the same class name and
 * body lives in the test source set of BcsFondoMutuoServicios (src/test/java/cl/bcs/oracle/H2FnDet.java): the new code runs on the same H2 functions.
 *
 * <ul>
 *   <li>{@code MAP(value, search, result, default)}: HANA decode. The SQL uses it only as {@code MAP(x, NULL, 0, x)} to turn a NULL into 0: a NULL value matches a NULL search
 *       (ASSUMED: it is the intent of the SQL; HANA's treatment of NULL in MAP was not verified).</li>
 *   <li>{@code TO_DECIMAL(value, precision, scale)}: HANA conversion; here only {@code TO_DECIMAL(0,26,8)} (the value cuota of a pending solicitud is shown as 0,00000000).</li>
 * </ul>
 */
public final class H2FnDet {
    private H2FnDet() { }

    public static Long map(Long value, Long search, Long result, Long otherwise) {
        boolean igual = value == null ? search == null : value.equals(search);
        return igual ? result : otherwise;
    }

    public static BigDecimal toDecimal(BigDecimal value, int precision, int scale) {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
    }
}
