package cl.bcs.contabilidad.infrastructure.persistence;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * {@link InterfazContadorSource} over the platform (HANA) database (CONT-A-03).
 *
 * <p>Legacy: {@code CreaInterfazDiaria.java:93-95} ran {@code ConsultaContadorInterfazSIGA.sql} with
 * {@code FechaEjecucion} as a {@code yyyy-MM-dd} string.
 */
public final class InterfazContadorJdbcSource implements InterfazContadorSource {

    static final String SQL_CONTADOR = "cl/bcs/contabilidad/sql/ConsultaContadorInterfazSIGA.sql";

    private final NamedParameterJdbcTemplate jdbc;

    public InterfazContadorJdbcSource(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public List<Long> contadoresInterfazSiga(LocalDate fechaEjecucion) {
        MapSqlParameterSource params = new MapSqlParameterSource("FechaEjecucion",
                fechaEjecucion.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return jdbc.query(SqlResources.load(SQL_CONTADOR), params, SqlRow.mapper(r -> r.lng("Cont")));
    }
}
