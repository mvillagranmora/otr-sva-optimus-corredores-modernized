package cl.bcs.apv.application.recaudacion;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Legacy {@code SQL.batch} that ANSWERS the per-statement update counts (the kit's {@code SqlLegacy.lote} returns nothing): one batch element per row, the
 * parameters read from the row's public fields by name, every {@link BigDecimal} written with scale 16 HALF_DOWN; failures surface as {@link SQLException}.
 * An empty list gives an empty array (legacy: the statement has parameters, nothing is added to the batch).
 */
final class BonificacionLote {

    private BonificacionLote() {
    }

    static <T> int[] lote(SqlLegacy sql, String texto, List<T> filas) throws SQLException {
        NamedParameterJdbcTemplate jdbc = sql.jdbc();
        try {
            SqlParameterSource[] fuentes = filas.stream().map(Campos::new).toArray(SqlParameterSource[]::new);
            return jdbc.batchUpdate(SqlLegacy.sinComentarios(texto), fuentes);
        } catch (DataAccessException e) {
            Throwable causa = e.getMostSpecificCause();
            throw causa instanceof SQLException s ? s : new SQLException(e.getMessage(), e);
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
                Field f = fila.getClass().getField(nombre);
                Object v = f.get(fila);
                return v instanceof BigDecimal bd ? bd.setScale(16, RoundingMode.HALF_DOWN) : v;
            } catch (ReflectiveOperationException e) {
                throw new InvalidDataAccessApiUsageException("Map Object failed", e);
            }
        }
    }
}
