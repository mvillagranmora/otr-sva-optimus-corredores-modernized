package cl.bcs.plataforma.adapter.out.jdbc;

import cl.bcs.plataforma.port.out.FeriadoRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JDBC adapter for PLA.Feriado table.
 * Requires SAP HANA datasource.
 */
@Repository
public class FeriadoJdbcAdapter implements FeriadoRepository {

    private static final String SQL_FIND_BY_PAIS =
        "SELECT \"IdFeriado\", \"Tipo\", \"Fecha\", \"Dia\", \"DescripcionDia\", \"IdPais\" " +
        "FROM \"BSCL.Plataforma.BD::PLA.Feriado\" " +
        "WHERE \"IdPais\" = :idPais";

    private final NamedParameterJdbcTemplate jdbc;

    public FeriadoJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Feriado> findByPais(Long idPais) {
        // TODO: wire when HANA datasource is available
        throw new UnsupportedOperationException("requires HANA datasource — use @Disabled(\"requires HANA\") in tests");
    }
}
