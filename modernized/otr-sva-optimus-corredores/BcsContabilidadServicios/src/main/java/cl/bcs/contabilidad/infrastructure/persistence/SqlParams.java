package cl.bcs.contabilidad.infrastructure.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Collection;
import java.util.function.Function;

/**
 * Explicit parameter binding for batches.
 *
 * <p>Legacy {@code SQL.batch(con, sql, List<Pojo>, Pojo.class)} bound every {@code :Name} of the statement from the
 * public field with that name. Modern code maps each record to a {@link MapSqlParameterSource} by hand (no
 * reflection over public fields, which Spring's {@code SqlParameterSourceUtils.createBatch} cannot read).
 */
public final class SqlParams {

    private SqlParams() {}

    public static <T> SqlParameterSource[] batch(Collection<T> rows, Function<T, MapSqlParameterSource> binder) {
        return rows.stream().map(binder).toArray(SqlParameterSource[]::new);
    }

    public static SqlParameterSource[] batch(Collection<? extends SqlParameterSource> rows) {
        return rows.toArray(SqlParameterSource[]::new);
    }
}
