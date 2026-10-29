package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.BcsCentralizacionService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Declarative description of what one legacy centralization class does,
 * transcribed line by line from the legacy source (the oracle).
 */
final class Spec {

    /** How the legacy turns a POJO field into a TabDatosMovimiento row. */
    enum Formato {
        /** {@code mov.X} (String) — TabDatosMovimiento ctor turns null into "" (pojo/TabDatosMovimiento.java:23). */
        STR,
        /** {@code String.valueOf(mov.X)} with X a Long — null becomes the literal "null". */
        VALUEOF_LONG,
        /** {@code String.valueOf(mov.X)} with X a String — null becomes the literal "null". */
        VALUEOF_STR,
        /** {@code Fecha.stringToSqlDate(mov.X)} with X a java.sql.Date — yyyy-MM-dd; null throws (SimpleDateFormat.format(null)). */
        DATE,
        /** {@code mov.X.toString()} with X a java.sql.Date — yyyy-MM-dd; null throws NPE. */
        TOSTRING_DATE,
        /** {@code mov.X.toString()} with X a String — identity; null throws NPE. */
        TOSTRING_STR,
        /** Literal "" regardless of the row (column, if any, is ignored). */
        CONST,
        /** {@code new TabDatosMovimiento(.., "X", "", mov.X)} — ValorVarChar "", ValorDecimal = value (null -> ZERO). */
        DECIMAL,
        /** {@code mov.X != null ? Fecha.toStringTimeStamp(mov.X) : "0001-01-01 00:00:00"} — yyyy-MM-dd HH:mm:ss.SSS. */
        TS_O_DEFECTO,
        /** {@code Fecha.toStringDatetime(mov.X)} — yyyy-MM-dd HH:mm:ss; null throws NPE. */
        DATETIME;

        boolean lanzaConNulo() {
            return this == DATE || this == TOSTRING_DATE || this == TOSTRING_STR || this == DATETIME;
        }
    }

    enum Rol { MOV, EVE, DAT, OTRA }

    record Campo(String tipoDato, String columna, Formato formato) {}

    /** One named SQL the legacy runs: file name (without package path), role and exact named parameters. */
    record Consulta(String archivo, Rol rol, Map<String, Object> params) {}

    record Tipo(String nombre,
                BiFunction<PlataformaPort, ContabilidadRepository, BcsCentralizacionService> factory,
                List<Consulta> consultas,
                List<Campo> campos) {

        List<Consulta> de(Rol rol) {
            return consultas.stream().filter(c -> c.rol() == rol).toList();
        }

        boolean tieneCamposQueLanzan() {
            return campos.stream().anyMatch(c -> c.columna() != null && c.formato().lanzaConNulo());
        }
    }

    static Campo campo(String tipoDato, String columna, Formato formato) {
        return new Campo(tipoDato, columna, formato);
    }

    static Consulta consulta(String archivo, Rol rol, Map<String, Object> params) {
        return new Consulta(archivo, rol, params);
    }

    static Map<String, Object> params(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    private Spec() {}
}
