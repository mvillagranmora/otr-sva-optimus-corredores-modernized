package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.domain.CampoClasificador;
import cl.bcs.normativa.domain.Clasificador;
import cl.bcs.normativa.port.out.ClasificadorPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para Clasificador normativo.
 * HANA tables:
 *   "BSCL.Normativa.BD::DOC.Clasificador"      (alias CLASIFICADOR en H2)
 *   "BSCL.Normativa.BD::DOC.CampoClasificador" (alias CAMPO_CLASIFICADOR en H2)
 */
@Repository
public class ClasificadorJdbcAdapter implements ClasificadorPort {

    private final NamedParameterJdbcTemplate jdbc;

    public ClasificadorJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Clasificador> buscarPorClave(String nombreEsquema, String versionEsquema, String tipo) {
        var sql = """
                SELECT Esquema, Version, Tipo, Descripcion
                FROM CLASIFICADOR
                WHERE Esquema = :esquema AND Version = :version AND Tipo = :tipo
                """;
        List<Clasificador> result = jdbc.query(sql,
                new MapSqlParameterSource("esquema", nombreEsquema)
                        .addValue("version", versionEsquema)
                        .addValue("tipo", tipo),
                this::mapClasificador);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<CampoClasificador> buscarCamposPorDocumentoMasTipo(
            String nombreEsquema, String versionEsquema, long idDocumento, String tipoClasificador) {
        var sql = """
                SELECT Esquema, Version, Tipo, NombreCampo, ValorClasificador
                FROM CAMPO_CLASIFICADOR
                WHERE Esquema = :esquema AND Version = :version
                  AND IdDocumento = :idDocumento AND Tipo = :tipo
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("esquema", nombreEsquema)
                        .addValue("version", versionEsquema)
                        .addValue("idDocumento", idDocumento)
                        .addValue("tipo", tipoClasificador),
                this::mapCampoClasificador);
    }

    private Clasificador mapClasificador(ResultSet rs, int row) throws SQLException {
        var c = new Clasificador();
        c.setNombreEsquema(rs.getString("Esquema"));
        c.setVersionEsquema(rs.getString("Version"));
        c.setTipo(rs.getString("Tipo"));
        c.setDescripcion(rs.getString("Descripcion"));
        return c;
    }

    private CampoClasificador mapCampoClasificador(ResultSet rs, int row) throws SQLException {
        var cc = new CampoClasificador();
        cc.setNombreEsquema(rs.getString("Esquema"));
        cc.setVersionEsquema(rs.getString("Version"));
        cc.setTipoClasificador(rs.getString("Tipo"));
        cc.setNombreCampo(rs.getString("NombreCampo"));
        cc.setValorClasificador(rs.getString("ValorClasificador"));
        return cc;
    }
}
