package cl.bcs.tesoreria.javacall;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;

import cl.bcs.tesoreria.javacall.comun.Consultas;

/**
 * Batch execution used by {@link GirarComprobanteIngresoService} and {@link GirarComprobantesEgresoService}: the legacy {@code SQL.batch(con, sql,
 * objects, Class)} read each named parameter from the public field of the same name of every object. Same contract: a null list is an error, a
 * missing field is an error, BigDecimal values are written with scale 16 (see {@link Consultas#lote}), and a statement without parameters runs once
 * per object (once when the list is empty).
 */
final class GirarComprobanteLote {

    private GirarComprobanteLote() {
    }

    static void ejecutar(NamedParameterJdbcTemplate jdbc, String sql, List<?> objetos) throws SQLException {
        if (objetos == null) {
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        List<String> nombres = NamedParameterUtils.buildSqlParameterList(NamedParameterUtils.parseSqlStatement(sql), new MapSqlParameterSource()).stream()
                .map(SqlParameter::getName).distinct().toList();
        if (nombres.isEmpty() && objetos.isEmpty()) {
            jdbc.update(sql, Map.of());
            return;
        }
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Object objeto : objetos) {
            filas.add(valores(nombres, objeto));
        }
        Consultas.lote(jdbc, sql, filas);
    }

    private static Map<String, Object> valores(List<String> nombres, Object objeto) throws SQLException {
        Map<String, Object> valores = new LinkedHashMap<>();
        try {
            for (String nombre : nombres) {
                Field campo = objeto.getClass().getField(nombre);
                valores.put(nombre, campo.get(objeto));
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new SQLException("Map Object failed", e);
        }
        return valores;
    }
}
