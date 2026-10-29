package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Traza;
import cl.bcs.cartolas.port.out.TrazaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Adaptador HANA para trazas de proceso (tabla CAR.Traza).
 */
@Repository
public class HanaTrazaAdapter implements TrazaPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaTrazaAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public void save(final Traza traza) {
        Objects.requireNonNull(traza, "traza es requerido");
        final String sql = """
                INSERT INTO car_traza (id_nomina, mensaje)
                VALUES (:idNomina, :mensaje)
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idNomina", traza.getIdNomina())
                .addValue("mensaje",  traza.getMensaje());

        jdbc.update(sql, params);
    }
}
