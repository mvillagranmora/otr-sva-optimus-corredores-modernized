package cl.bcs.contabilidad.domain.service.soporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * One {@code TabDatosMovimiento} entry produced from a data row: its {@code TipoDato} name and how the legacy class
 * rendered the value. The factories are the legacy formatting idioms (pojo/TabDatosMovimiento.java:19-25 turns a null
 * ValorVarChar into "" and a null ValorDecimal into ZERO; that coalescing is done by {@link DatosMovimiento}).
 */
public record Campo(String tipoDato, Function<Fila, String> texto, Function<Fila, BigDecimal> decimal) {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    /** Default of the legacy {@code x != null ? Fecha.toStringTimeStamp(x) : "0001-01-01 00:00:00"} idiom. */
    public static final String TIMESTAMP_POR_DEFECTO = "0001-01-01 00:00:00";

    private static Function<Fila, BigDecimal> sinDecimal() {
        return f -> BigDecimal.ZERO;
    }

    /** {@code mov.X} of a String field (null -> ""). */
    public static Campo texto(String tipoDato) {
        return texto(tipoDato, tipoDato);
    }

    public static Campo texto(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> f.texto(columna), sinDecimal());
    }

    /** {@code String.valueOf(mov.X)}: a null value becomes the literal "null". */
    public static Campo valorDe(String tipoDato) {
        return valorDe(tipoDato, tipoDato);
    }

    public static Campo valorDe(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> String.valueOf(f.texto(columna)), sinDecimal());
    }

    /** {@code Fecha.stringToSqlDate(mov.X)}: yyyy-MM-dd; null aborts. */
    public static Campo fecha(String tipoDato) {
        return fecha(tipoDato, tipoDato);
    }

    public static Campo fecha(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> requerido(f.fecha(columna), tipoDato, columna).format(FECHA), sinDecimal());
    }

    /** {@code Fecha.toStringDate(java.sql.Date)}: yyyy-MM-dd, null -> "". */
    public static Campo fechaOVacio(String tipoDato) {
        return fechaOVacio(tipoDato, tipoDato);
    }

    public static Campo fechaOVacio(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> {
            LocalDate d = f.fecha(columna);
            return d == null ? "" : d.format(FECHA);
        }, sinDecimal());
    }

    /** {@code mov.X.toString()}: the value as text; null aborts. */
    public static Campo obligatorio(String tipoDato) {
        return obligatorio(tipoDato, tipoDato);
    }

    public static Campo obligatorio(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> {
            Object v = f.valor(columna);
            requerido(v, tipoDato, columna);
            return v instanceof java.sql.Date || v instanceof LocalDate ? f.fecha(columna).toString() : f.texto(columna);
        }, sinDecimal());
    }

    /** {@code mov.X.toPlainString()} of a BigDecimal; null aborts. */
    public static Campo plano(String tipoDato) {
        return plano(tipoDato, tipoDato);
    }

    public static Campo plano(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> requerido(f.decimal(columna), tipoDato, columna).toPlainString(), sinDecimal());
    }

    /** {@code Fecha.toStringDatetime(mov.X)}: yyyy-MM-dd HH:mm:ss; null aborts. */
    public static Campo fechaHora(String tipoDato) {
        return fechaHora(tipoDato, tipoDato);
    }

    public static Campo fechaHora(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> requerido(f.instante(columna), tipoDato, columna).format(FECHA_HORA), sinDecimal());
    }

    /** {@code mov.X != null ? Fecha.toStringTimeStamp(mov.X) : "0001-01-01 00:00:00"}. */
    public static Campo timestampOPorDefecto(String tipoDato) {
        return timestampOPorDefecto(tipoDato, tipoDato);
    }

    public static Campo timestampOPorDefecto(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> {
            LocalDateTime t = f.instante(columna);
            return t == null ? TIMESTAMP_POR_DEFECTO : t.format(TIMESTAMP);
        }, sinDecimal());
    }

    /** {@code new TabDatosMovimiento(.., "X", "", mov.X)}: the amount goes to ValorDecimal (null -> ZERO). */
    public static Campo decimal(String tipoDato) {
        return decimal(tipoDato, tipoDato);
    }

    public static Campo decimal(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> "", f -> f.decimal(columna));
    }

    /** {@code new BigDecimal(mov.X)} of a Long field into ValorDecimal; null aborts (unboxing). */
    public static Campo decimalDeEntero(String tipoDato, String columna) {
        return new Campo(tipoDato, f -> "", f -> BigDecimal.valueOf(requerido(f.entero(columna), tipoDato, columna)));
    }

    /** A literal {@code ""}. */
    public static Campo vacio(String tipoDato) {
        return literal(tipoDato, "");
    }

    /** A literal value, the same for every row. */
    public static Campo literal(String tipoDato, String valor) {
        return new Campo(tipoDato, f -> valor, sinDecimal());
    }

    /** Anything else: a custom rendering of the row. */
    public static Campo de(String tipoDato, Function<Fila, String> texto) {
        return new Campo(tipoDato, texto, sinDecimal());
    }

    static <T> T requerido(T valor, String tipoDato, String columna) {
        if (valor == null) throw new DatoRequeridoException(tipoDato, columna);
        return valor;
    }
}
