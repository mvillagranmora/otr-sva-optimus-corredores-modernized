package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Legacy {@code SQL.batch} that answers the per-row update counts ({@code int[]}), which the services compare ({@code res[0] == 1}).
 * Same semantics as {@code SqlLegacy.lote} (parameters from the public fields of each row, BigDecimal at scale 16 HALF_DOWN, failures as SQLException).
 */
final class LoteConteo {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private LoteConteo() {
    }

    static <T> int[] ejecutar(NamedParameterJdbcTemplate jdbc, String sql, List<T> filas) throws SQLException {
        String limpio = COMENTARIOS.matcher(sql).replaceAll("");
        try {
            boolean parametros = !NamedParameterUtils.buildSqlParameterList(NamedParameterUtils.parseSqlStatement(limpio), new MapSqlParameterSource()).isEmpty();
            if (filas.isEmpty()) {
                if (!parametros) {
                    return new int[] { jdbc.update(limpio, new MapSqlParameterSource()) };
                }
                return new int[0];
            }
            SqlParameterSource[] fuentes = filas.stream().map(Campos::new).toArray(SqlParameterSource[]::new);
            return jdbc.batchUpdate(limpio, fuentes);
        } catch (DataAccessException e) {
            Throwable causa = e.getMostSpecificCause();
            if (causa instanceof SQLException s) {
                // legacy SQL.rethrow builds a plain SQLException (never the driver's batch subclass) chained to the cause
                SQLException plano = new SQLException(s.getMessage(), s.getSQLState(), s.getErrorCode());
                plano.setNextException(s);
                throw plano;
            }
            throw new SQLException(e.getMessage(), e);
        }
    }

    private static final class Campos extends AbstractSqlParameterSource {
        private final Object fila;

        Campos(Object fila) {
            this.fila = fila;
        }

        @Override
        public boolean hasValue(String nombre) {
            try {
                fila.getClass().getField(nombre);
                return true;
            } catch (NoSuchFieldException e) {
                return false;
            }
        }

        @Override
        public Object getValue(String nombre) {
            try {
                Object v = fila.getClass().getField(nombre).get(fila);
                return v instanceof BigDecimal bd ? bd.setScale(16, RoundingMode.HALF_DOWN) : v;
            } catch (ReflectiveOperationException e) {
                throw new InvalidDataAccessApiUsageException("Object field unknown: " + nombre, e);
            }
        }
    }
}
