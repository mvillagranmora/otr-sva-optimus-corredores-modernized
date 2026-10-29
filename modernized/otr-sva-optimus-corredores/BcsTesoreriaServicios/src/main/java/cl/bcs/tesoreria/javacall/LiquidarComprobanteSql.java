package cl.bcs.tesoreria.javacall;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;

import cl.bcs.tesoreria.javacall.comun.Consultas;

/**
 * The legacy {@code SQL.batch} convention for the liquidation services: the parameters of each statement execution are read from the PUBLIC FIELDS of
 * the row objects, by the name of the {@code :parameter}; BigDecimal values are written with scale 16 (HALF_DOWN); a null value is a SQL NULL.
 * A {@code null} list is an error, and an empty list runs a statement without parameters once (and a statement with parameters never).
 */
final class LiquidarComprobanteSql {

    /** The legacy {@code SQL} removed the comments (block, double slash and double dash) of every statement before running it; the same text is run here. */
    private static final java.util.regex.Pattern COMENTARIOS = java.util.regex.Pattern
            .compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private LiquidarComprobanteSql() {
    }

    /** Loads a SQL resource without its comments. */
    static String cargar(String recurso) {
        return COMENTARIOS.matcher(TesoreriaJdbcHelper.loadSql(recurso)).replaceAll("");
    }

    static void lote(NamedParameterJdbcTemplate jdbc, String sql, List<?> filas) throws SQLException {
        if (filas == null) {
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        List<String> nombres = NamedParameterUtils.buildSqlParameterList(NamedParameterUtils.parseSqlStatement(sql), new MapSqlParameterSource()).stream()
                .map(SqlParameter::getName).distinct().toList();
        if (nombres.isEmpty() && filas.isEmpty()) {
            jdbc.update(sql, Map.of());
            return;
        }
        if (filas.isEmpty()) {
            return;
        }
        List<Map<String, Object>> parametros = new java.util.ArrayList<>(filas.size());
        for (Object fila : filas) {
            Map<String, Object> valores = new HashMap<>();
            for (String nombre : nombres) {
                valores.put(nombre, leer(fila, nombre));
            }
            parametros.add(valores);
        }
        Consultas.lote(jdbc, sql, parametros);
    }

    private static Object leer(Object fila, String nombre) throws SQLException {
        try {
            Field campo = fila.getClass().getField(nombre);
            return campo.get(fila);
        } catch (ReflectiveOperationException e) {
            throw new SQLException("Map Object failed", e);
        }
    }
}
