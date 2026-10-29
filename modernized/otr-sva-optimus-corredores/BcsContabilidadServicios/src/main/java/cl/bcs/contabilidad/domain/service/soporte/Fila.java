package cl.bcs.contabilidad.domain.service.soporte;

import org.springframework.util.LinkedCaseInsensitiveMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * One row of a named centralization query, read by column label.
 *
 * <p>Legacy {@code cl.bcs.utils.SQL.query} copied each column into the public POJO field with the same name and
 * ignored absent ones; here a missing column reads as {@code null} too. Labels are case-insensitive, as the
 * {@code queryForList} maps of the JDBC adapter.
 */
public final class Fila {

    private final Map<String, Object> valores;

    private Fila(Map<String, Object> valores) {
        this.valores = valores;
    }

    public static Fila de(Map<String, Object> columnas) {
        Map<String, Object> m = new LinkedCaseInsensitiveMap<>();
        if (columnas != null) m.putAll(columnas);
        return new Fila(m);
    }

    /** A copy of this row with one column replaced (legacy code mutated the POJO field in place). */
    public Fila con(String columna, Object valor) {
        Map<String, Object> m = new LinkedCaseInsensitiveMap<>();
        m.putAll(valores);
        m.put(columna, valor);
        return new Fila(m);
    }

    public Object valor(String columna) {
        return valores.get(columna);
    }

    /** Value of a String POJO field: any non-null value as text. */
    public String texto(String columna) {
        Object v = valores.get(columna);
        return v == null ? null : v instanceof String s ? s : escalar(v).toString();
    }

    /** Value of a Long POJO field. */
    public Long entero(String columna) {
        Object v = valores.get(columna);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        String s = v.toString().trim();
        return s.isEmpty() ? null : new BigDecimal(s).longValue();
    }

    /** Value of a BigDecimal POJO field. */
    public BigDecimal decimal(String columna) {
        Object v = valores.get(columna);
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Long || v instanceof Integer || v instanceof Short || v instanceof Byte)
            return BigDecimal.valueOf(((Number) v).longValue());
        if (v instanceof BigInteger bi) return new BigDecimal(bi);
        if (v instanceof Number n) return new BigDecimal(n.toString());
        String s = v.toString().trim();
        return s.isEmpty() ? null : new BigDecimal(s);
    }

    /** Value of a java.sql.Date POJO field, as a calendar date. */
    public LocalDate fecha(String columna) {
        Object v = valores.get(columna);
        return switch (v) {
            case null -> null;
            case java.sql.Date d -> d.toLocalDate();
            case Timestamp t -> t.toLocalDateTime().toLocalDate();
            case java.util.Date d -> new java.sql.Date(d.getTime()).toLocalDate();
            case LocalDate d -> d;
            case LocalDateTime t -> t.toLocalDate();
            default -> LocalDate.parse(v.toString().substring(0, 10));
        };
    }

    /** Value of a java.sql.Timestamp POJO field. */
    public LocalDateTime instante(String columna) {
        Object v = valores.get(columna);
        return switch (v) {
            case null -> null;
            case Timestamp t -> t.toLocalDateTime();
            case java.sql.Date d -> d.toLocalDate().atStartOfDay();
            case java.util.Date d -> new Timestamp(d.getTime()).toLocalDateTime();
            case LocalDateTime t -> t;
            case LocalDate d -> d.atStartOfDay();
            default -> Timestamp.valueOf(v.toString()).toLocalDateTime();
        };
    }

    /** Integral numbers render without scale, as the legacy Long fields did. */
    private static Object escalar(Object v) {
        if (v instanceof BigDecimal b && b.scale() <= 0) return b.toBigInteger();
        return v;
    }

    @Override
    public String toString() {
        return "Fila" + valores;
    }
}
