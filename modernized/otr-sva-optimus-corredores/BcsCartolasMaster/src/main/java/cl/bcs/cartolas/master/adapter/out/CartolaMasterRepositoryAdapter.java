package cl.bcs.cartolas.master.adapter.out;

import cl.bcs.cartolas.master.domain.ConfiguracionCartola;
import cl.bcs.cartolas.master.domain.ParametroCartola;
import cl.bcs.cartolas.master.port.out.CartolaMasterDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * Adaptador de salida JDBC para consultas de datos de Cartolas Master.
 *
 * <p>Usa {@link NamedParameterJdbcTemplate} con {@link MapSqlParameterSource}
 * (H2-compatible y HANA-compatible).</p>
 *
 * <p>En producción las tablas son SAP HANA column-store tables con nombres
 * quoted (p.e. {@code "BSCL.Normativa.BD::CAR.Config"}). En el perfil test
 * se mapean a tablas H2 con nombres planos definidos en schema-test.sql.</p>
 */
@Repository
public class CartolaMasterRepositoryAdapter implements CartolaMasterDataPort {

    /**
     * SQL compatible H2: usa nombres de columna sin comillas dobles.
     * En producción HANA se deben usar comillas; aquí el DataSource de test
     * apunta a H2 con las tablas creadas según el schema H2-compatible.
     */
    private static final String SQL_CONFIG =
            "SELECT id_config, id_empresa, descripcion, tipo, nombre, valor " +
            "FROM car_config " +
            "WHERE id_empresa = :idEmpresa";

    private static final String SQL_PARAMETROS =
            "SELECT id_parametro, id_nomina, nombre, valor " +
            "FROM car_parametro " +
            "WHERE id_nomina = :idNomina";

    private final NamedParameterJdbcTemplate jdbc;

    public CartolaMasterRepositoryAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public List<ConfiguracionCartola> findConfiguracionByEmpresa(final long idEmpresa) {
        final var params = new MapSqlParameterSource("idEmpresa", idEmpresa);
        return jdbc.query(SQL_CONFIG, params, (rs, rowNum) ->
                new ConfiguracionCartola(
                        rs.getLong("id_config"),
                        rs.getLong("id_empresa"),
                        rs.getString("descripcion"),
                        rs.getString("tipo"),
                        rs.getString("nombre"),
                        rs.getString("valor")
                ));
    }

    @Override
    public List<ParametroCartola> findParametrosByNomina(final long idNomina) {
        final var params = new MapSqlParameterSource("idNomina", idNomina);
        return jdbc.query(SQL_PARAMETROS, params, (rs, rowNum) ->
                new ParametroCartola(
                        rs.getLong("id_parametro"),
                        rs.getLong("id_nomina"),
                        rs.getString("nombre"),
                        rs.getString("valor")
                ));
    }
}
