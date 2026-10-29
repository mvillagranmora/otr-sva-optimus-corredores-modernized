package cl.bcs.plataforma.jobs.adapter.out;

import cl.bcs.plataforma.jobs.port.out.ConfiguracionPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * HANA-backed adapter implementing ConfiguracionPort.
 * All methods require a live HANA connection.
 */
@Repository
public class ConfiguracionAdapter implements ConfiguracionPort {

    private final NamedParameterJdbcTemplate jdbc;

    public ConfiguracionAdapter(NamedParameterJdbcTemplate jdbc) {
        Objects.requireNonNull(jdbc, "jdbc must not be null");
        this.jdbc = jdbc;
    }

    @Override
    public String getParametroString(Long idEmpresa, String idModulo, String idParametro) {
        Objects.requireNonNull(idEmpresa,    "idEmpresa must not be null");
        Objects.requireNonNull(idModulo,     "idModulo must not be null");
        Objects.requireNonNull(idParametro,  "idParametro must not be null");
        throw new UnsupportedOperationException(
                "getParametroString requires a live HANA connection");
    }

    @Override
    public Long getParametroLong(Long idEmpresa, String idModulo, String idParametro) {
        Objects.requireNonNull(idEmpresa,    "idEmpresa must not be null");
        Objects.requireNonNull(idModulo,     "idModulo must not be null");
        Objects.requireNonNull(idParametro,  "idParametro must not be null");
        throw new UnsupportedOperationException(
                "getParametroLong requires a live HANA connection");
    }

    @Override
    public String getRutaBaseArchivos(Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        throw new UnsupportedOperationException(
                "getRutaBaseArchivos requires a live HANA connection");
    }
}
