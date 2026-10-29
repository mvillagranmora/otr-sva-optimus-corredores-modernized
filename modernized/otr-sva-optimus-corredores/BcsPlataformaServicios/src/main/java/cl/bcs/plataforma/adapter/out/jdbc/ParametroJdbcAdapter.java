package cl.bcs.plataforma.adapter.out.jdbc;

import cl.bcs.plataforma.port.out.ParametroRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JDBC adapter for PLA.Parametro table.
 * Requires SAP HANA datasource — disabled in test profile (H2 lacks HANA schema).
 */
@Repository
public class ParametroJdbcAdapter implements ParametroRepository {

    private static final String SQL_FIND_VALOR =
        "SELECT \"ValorParametro\" FROM \"BSCL.Plataforma.BD::PLA.Parametro\" " +
        "WHERE \"IdEmpresa\" = :idEmpresa " +
        "AND \"IdModuloParametro\" = :moduloParametro " +
        "AND \"NombreParametro\" = :nombreParametro";

    private final NamedParameterJdbcTemplate jdbc;

    public ParametroJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> findValor(Long idEmpresa, String moduloParametro, String nombreParametro) {
        // TODO: wire when HANA datasource is available
        throw new UnsupportedOperationException("requires HANA datasource — use @Disabled(\"requires HANA\") in tests");
    }
}
