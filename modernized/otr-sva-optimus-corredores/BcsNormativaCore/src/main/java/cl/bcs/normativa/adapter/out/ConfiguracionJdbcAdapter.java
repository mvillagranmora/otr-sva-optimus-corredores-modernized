package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.port.out.ConfiguracionPort;
import cl.bcs.normativa.port.out.ParametroPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para Configuracion y Parametro.
 * HANA tables:
 *   "BSCL.Normativa.BD::NOR.Configuracion" (alias CONFIGURACION en H2)
 *   "BSCL.Normativa.BD::DOC.Parametro"     (alias PARAMETRO en H2)
 */
@Repository
public class ConfiguracionJdbcAdapter implements ConfiguracionPort, ParametroPort {

    private final NamedParameterJdbcTemplate jdbc;

    public ConfiguracionJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** ConfiguracionPort — busca valor por empresa y codigo. */
    @Override
    public Optional<String> buscarValor(long idEmpresa, String codigoParametro) {
        var sql = """
                SELECT Valor FROM CONFIGURACION
                WHERE IdEmpresa = :idEmpresa AND CodigoParametro = :codigo
                """;
        List<String> result = jdbc.query(sql,
                new MapSqlParameterSource("idEmpresa", idEmpresa).addValue("codigo", codigoParametro),
                (rs, row) -> rs.getString("Valor"));
        return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.get(0));
    }

    /** ParametroPort — busca valor por esquema y codigo. */
    @Override
    public Optional<String> buscarValor(String nombreEsquema, String versionEsquema, String codigo) {
        var sql = """
                SELECT Valor FROM PARAMETRO
                WHERE Esquema = :esquema AND Version = :version AND Codigo = :codigo
                """;
        List<String> result = jdbc.query(sql,
                new MapSqlParameterSource("esquema", nombreEsquema)
                        .addValue("version", versionEsquema)
                        .addValue("codigo", codigo),
                (rs, row) -> rs.getString("Valor"));
        return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.get(0));
    }
}
