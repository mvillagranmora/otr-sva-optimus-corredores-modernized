package cl.bcs.contabilidad.infrastructure.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Draws values from a HANA sequence, legacy {@code cl.bcs.utils.Utils.getSecuencia(con, nombre, cantidad)}.
 *
 * <p>Same statement as legacy (HANA branch): {@code SELECT "<seq>".NEXTVAL AS "Id" FROM SERIES_GENERATE_BIGINT(1, 0,
 * :cantidad)}; a non-positive count returns no ids and runs nothing.
 */
public final class HanaSequences {

    private HanaSequences() {}

    public static List<Long> next(NamedParameterJdbcTemplate jdbc, String sequence, long cantidad) {
        if (cantidad <= 0) return List.of();
        if (sequence.contains("\"")) throw new IllegalArgumentException("invalid sequence name: " + sequence);
        String sql = "SELECT \"" + sequence + "\".NEXTVAL AS \"Id\" FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)";
        return jdbc.query(sql, new MapSqlParameterSource("cantidad", cantidad), SqlRow.mapper(r -> r.lng("Id")))
                .stream().filter(Objects::nonNull).toList();
    }
}
