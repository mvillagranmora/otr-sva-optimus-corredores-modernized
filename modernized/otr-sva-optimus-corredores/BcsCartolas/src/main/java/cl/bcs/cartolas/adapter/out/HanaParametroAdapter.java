package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Parametro;
import cl.bcs.cartolas.port.out.ParametroPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * Adaptador HANA para parámetros de nómina (tabla CAR.Parametro).
 */
@Repository
public class HanaParametroAdapter implements ParametroPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaParametroAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public List<Parametro> findByIdNomina(final long idNomina) {
        final String sql = """
                SELECT id_parametro, id_nomina, nombre, valor
                FROM car_parametro
                WHERE id_nomina = :idNomina
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idNomina", idNomina);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            final Parametro p = new Parametro();
            p.setId(rs.getLong("id_parametro"));
            p.setIdNomina(rs.getLong("id_nomina"));
            p.setNombre(rs.getString("nombre"));
            p.setValor(rs.getString("valor"));
            return p;
        });
    }

    @Override
    public long save(final Parametro parametro, final long idEmpresa, final long idUsuario) {
        Objects.requireNonNull(parametro, "parametro es requerido");
        // SP HANA: CAR.GrabarParametro — requiere HANA
        throw new UnsupportedOperationException(
                "save(Parametro) requiere SAP HANA — SP: CAR.GrabarParametro");
    }
}
