package cl.bcs.apv.application.recaudacion;

import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Batch with the update counts (legacy {@code SQL.batch} returns {@code int[]} and the mass-load classes test {@code res[0] == 1}); the parameters are
 * the public fields of each row, BigDecimal at scale 16 HALF_DOWN, as {@link SqlLegacy#lote}.
 */
final class CargasSql {

    private CargasSql() {
    }

    /** Like {@code SqlLegacy.lote} but returns the counts. An empty list with a parameterless statement runs it once. */
    static <T> int[] lote(SqlLegacy sql, String texto, List<T> filas) throws SQLException {
        String limpio = SqlLegacy.sinComentarios(texto);
        try {
            if (filas.isEmpty()) {
                sql.lote(texto, filas);
                return new int[0];
            }
            SqlParameterSource[] fuentes = filas.stream().map(CargasSql::fuente).toArray(SqlParameterSource[]::new);
            return sql.jdbc().batchUpdate(limpio, fuentes);
        } catch (DataAccessException e) {
            Throwable causa = e.getMostSpecificCause();
            throw causa instanceof SQLException s ? s : new SQLException(e.getMessage(), e);
        }
    }

    private static SqlParameterSource fuente(Object fila) {
        MapSqlParameterSource ps = new MapSqlParameterSource();
        for (java.lang.reflect.Field f : fila.getClass().getFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            try {
                Object v = f.get(fila);
                ps.addValue(f.getName(), v instanceof java.math.BigDecimal bd ? bd.setScale(16, java.math.RoundingMode.HALF_DOWN) : v);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return ps;
    }
}
