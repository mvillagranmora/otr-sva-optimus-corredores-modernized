package cl.bcs.tesoreria.javacall.comun;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Small helpers that keep the SQL conventions of the legacy {@code cl.bcs.utils.SQL}.
 * <ul>
 *   <li>An empty list used in an {@code IN (:lista)} clause becomes a single NULL ({@code IN (NULL)}), never an empty list (which is not valid SQL).</li>
 *   <li>The parameters of a batch are written with {@code BigDecimal} scale 16, rounding HALF_DOWN.</li>
 * </ul>
 */
public final class Consultas {

    /** Scale the legacy batch applied to every BigDecimal parameter. */
    public static final int ESCALA_LOTE = 16;

    private Consultas() {
    }

    /** The list to bind to an {@code IN (:x)} parameter: the list itself, or one NULL when it is empty (or null). */
    public static <T> List<T> enLista(List<T> valores) {
        return valores == null || valores.isEmpty() ? Collections.singletonList(null) : valores;
    }

    /** One parameter source for a batch row, with the legacy rounding of BigDecimal values. */
    public static SqlParameterSource fila(Map<String, ?> valores) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        valores.forEach((k, v) -> p.addValue(k, v instanceof BigDecimal b ? b.setScale(ESCALA_LOTE, RoundingMode.HALF_DOWN) : v));
        return p;
    }

    /** Executes a batch of rows with the legacy rounding. */
    public static int[] lote(NamedParameterJdbcTemplate jdbc, String sql, List<? extends Map<String, ?>> filas) {
        return jdbc.batchUpdate(sql, filas.stream().map(Consultas::fila).toArray(SqlParameterSource[]::new));
    }
}
