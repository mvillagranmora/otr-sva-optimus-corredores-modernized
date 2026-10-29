package cl.bcs.apv.infrastructure.persistence.contratos;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * The legacy {@code SQL.batch} for the persistence adapters of ModificarContratos / CambiarContratosAPlanActivo: it RETURNS the per-row counts
 * (the legacy tests {@code result[0] == 1} and {@code result.length}), which {@code SqlNombrado.lote} does not. One execution per row; every
 * {@link BigDecimal} is written with {@code setScale(16, HALF_DOWN)}; an empty list executes nothing and answers an empty array (the statements here all
 * have parameters or a non-empty list); failures surface as {@link SQLException}, as the legacy.
 */
final class LoteContratoModificacion {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private LoteContratoModificacion() {
    }

    static String sinComentarios(String sql) {
        return COMENTARIOS.matcher(sql).replaceAll("");
    }

    static int[] ejecutar(NamedParameterJdbcTemplate jdbc, String sql, List<? extends Record> filas) throws SQLException {
        if (filas == null) {
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        SqlParameterSource[] fuentes = new SqlParameterSource[filas.size()];
        for (int i = 0; i < fuentes.length; i++) {
            if (filas.get(i) == null) {
                throw new SQLException("Map Object failed");
            }
            fuentes[i] = new MapSqlParameterSource(campos(filas.get(i)));
        }
        try {
            return jdbc.batchUpdate(sinComentarios(sql), fuentes);
        } catch (DataAccessException e) {
            throw sqlException(e);
        }
    }

    /** The legacy {@code result != null && result.length > 0 && result[0] == 1}. */
    static boolean primeroEsUno(int[] resultado) {
        return resultado != null && resultado.length > 0 && resultado[0] == 1;
    }

    static Map<String, Object> campos(Record fila) {
        Map<String, Object> campos = new LinkedHashMap<>();
        try {
            for (RecordComponent c : fila.getClass().getRecordComponents()) {
                c.getAccessor().setAccessible(true);
                Object v = c.getAccessor().invoke(fila);
                campos.put(c.getName(), v instanceof BigDecimal bd ? bd.setScale(16, RoundingMode.HALF_DOWN) : v);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return campos;
    }

    static SQLException sqlException(DataAccessException e) {
        Throwable causa = e.getMostSpecificCause();
        if (causa instanceof SQLException sql) {
            return sql;
        }
        return new SQLException(e.getMessage(), e);
    }
}
