package cl.bcs.contabilidad.infrastructure.persistence;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

/**
 * Row count of a statement, legacy {@code cl.bcs.utils.SQL.countQuery}: the statement (without {@code ;}) is wrapped in
 * {@code SELECT COUNT(1) AS "Contador" FROM (...)}.
 */
public final class SqlCount {

    private SqlCount() {}

    public static long count(NamedParameterJdbcTemplate jdbc, String sql, SqlParameterSource params) {
        String wrapped = "SELECT COUNT(1) AS \"Contador\" FROM (" + sql.replace(";", "") + ")";
        List<Long> r = jdbc.query(wrapped, params, SqlRow.mapper(row -> row.lng("Contador")));
        return r.isEmpty() || r.get(0) == null ? 0L : r.get(0);
    }
}
