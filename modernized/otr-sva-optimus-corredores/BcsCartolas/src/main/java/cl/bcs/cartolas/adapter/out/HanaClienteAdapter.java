package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Cliente;
import cl.bcs.cartolas.port.out.ClientePort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador HANA para consulta de clientes (vista NOR.ConsultarCliente)
 * y código de portafolio (POR.Portafolio).
 */
@Repository
public class HanaClienteAdapter implements ClientePort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaClienteAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public Optional<Cliente> findById(final long idCliente) {
        final String sql = """
                SELECT id_cliente, razon_social, nombres, apellido_paterno, apellido_materno,
                       documento_identidad, email
                FROM vw_cliente
                WHERE id_cliente = :idCliente
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCliente", idCliente);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            final Cliente c = new Cliente();
            c.setId(rs.getLong("id_cliente"));
            c.setDocumentoIdentidad(rs.getString("documento_identidad"));
            c.setEmail(rs.getString("email"));
            c.setNombre(Cliente.buildNombre(
                    rs.getString("razon_social"),
                    rs.getString("nombres"),
                    rs.getString("apellido_paterno"),
                    rs.getString("apellido_materno")));
            return c;
        }).stream().findFirst();
    }

    @Override
    public Optional<String> findCodigoPortafolio(final long idPortafolio) {
        final String sql = """
                SELECT codigo_portafolio
                FROM por_portafolio
                WHERE id_portafolio = :idPortafolio
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPortafolio", idPortafolio);

        return jdbc.query(sql, params, (rs, rowNum) -> rs.getString("codigo_portafolio"))
                .stream().findFirst();
    }
}
