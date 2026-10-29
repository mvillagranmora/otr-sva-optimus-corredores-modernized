package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Cartola;
import cl.bcs.cartolas.domain.Opcion;
import cl.bcs.cartolas.port.out.CartolaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador HANA para operaciones de Cartola (tabla CAR.Cartola).
 */
@Repository
public class HanaCartolaAdapter implements CartolaPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaCartolaAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public Optional<Cartola> findById(final long idCartola) {
        final String sql = """
                SELECT id_cartola, id_nomina, id_cliente, id_portafolio,
                       reprocesar, notificar, estado_proceso, mensaje_proceso
                FROM car_cartola
                WHERE id_cartola = :idCartola
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCartola", idCartola);

        return jdbc.query(sql, params, this::mapRow).stream().findFirst();
    }

    @Override
    public List<Cartola> findByIdNomina(final long idNomina, final boolean soloReproceso) {
        final StringBuilder sql = new StringBuilder("""
                SELECT id_cartola, id_nomina, id_cliente, id_portafolio,
                       reprocesar, notificar, estado_proceso, mensaje_proceso
                FROM car_cartola
                WHERE id_nomina = :idNomina
                """);
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idNomina", idNomina);

        if (soloReproceso) {
            sql.append("  AND reprocesar = :reprocesar\n");
            params.addValue("reprocesar", Opcion.SI);
        }

        return jdbc.query(sql.toString(), params, this::mapRow);
    }

    @Override
    public long save(final Cartola cartola, final long idEmpresa, final long idUsuario) {
        Objects.requireNonNull(cartola, "cartola es requerido");
        // SP HANA: CAR.GrabarCartola — requiere HANA
        throw new UnsupportedOperationException(
                "save(Cartola) requiere SAP HANA — SP: CAR.GrabarCartola");
    }

    private Cartola mapRow(final java.sql.ResultSet rs, final int rowNum) throws java.sql.SQLException {
        final Cartola c = new Cartola();
        c.setId(rs.getLong("id_cartola"));
        c.setIdNomina(rs.getLong("id_nomina"));
        c.setIdCliente(rs.getLong("id_cliente"));
        c.setIdPortafolio(rs.getLong("id_portafolio"));
        c.setReprocesar(rs.getString("reprocesar"));
        c.setNotificar(rs.getString("notificar"));
        c.setEstadoProceso(rs.getString("estado_proceso"));
        c.setMensajeProceso(rs.getString("mensaje_proceso"));
        return c;
    }
}
