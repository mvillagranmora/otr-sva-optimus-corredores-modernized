package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Config;
import cl.bcs.cartolas.port.out.ConfigPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * Adaptador HANA para configuración de empresa (tabla CAR.Config).
 *
 * <p>Usa nombre de tabla simple {@code car_config} para compatibilidad con H2 en tests.
 * En producción HANA el esquema se configura vía {@code spring.datasource.url}.</p>
 */
@Repository
public class HanaConfigAdapter implements ConfigPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaConfigAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public List<Config> findByIdEmpresa(final long idEmpresa) {
        final String sql = """
                SELECT id_config, id_empresa, descripcion, tipo, nombre, valor
                FROM car_config
                WHERE id_empresa = :idEmpresa
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            final Config c = new Config();
            c.setId(rs.getLong("id_config"));
            c.setIdEmpresa(rs.getLong("id_empresa"));
            c.setDescripcion(rs.getString("descripcion"));
            c.setTipo(rs.getString("tipo"));
            c.setNombre(rs.getString("nombre"));
            c.setValor(rs.getString("valor"));
            return c;
        });
    }
}
