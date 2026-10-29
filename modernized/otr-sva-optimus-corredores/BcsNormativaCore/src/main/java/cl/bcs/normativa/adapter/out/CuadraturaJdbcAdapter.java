package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.domain.CampoCuadratura;
import cl.bcs.normativa.domain.Cuadratura;
import cl.bcs.normativa.port.out.CuadraturaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para Cuadratura normativa.
 * HANA tables:
 *   "BSCL.Normativa.BD::DOC.Cuadratura"     (alias CUADRATURA en H2)
 *   "BSCL.Normativa.BD::DOC.CampoCuadratura" (alias CAMPO_CUADRATURA en H2)
 */
@Repository
public class CuadraturaJdbcAdapter implements CuadraturaPort {

    private final NamedParameterJdbcTemplate jdbc;

    public CuadraturaJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Cuadratura> buscarPorClave(String nombreEsquema, String versionEsquema, String tipo) {
        var sql = """
                SELECT Esquema, Version, Tipo, Descripcion
                FROM CUADRATURA
                WHERE Esquema = :esquema AND Version = :version AND Tipo = :tipo
                """;
        List<Cuadratura> result = jdbc.query(sql,
                new MapSqlParameterSource("esquema", nombreEsquema)
                        .addValue("version", versionEsquema)
                        .addValue("tipo", tipo),
                this::mapCuadratura);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<CampoCuadratura> buscarCamposPorClave(
            String nombreEsquema, String versionEsquema, String tipoCuadratura) {
        var sql = """
                SELECT Esquema, Version, NombreCuadratura, NombreCampo, Orden
                FROM CAMPO_CUADRATURA
                WHERE Esquema = :esquema AND Version = :version AND NombreCuadratura = :tipo
                ORDER BY Orden ASC
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("esquema", nombreEsquema)
                        .addValue("version", versionEsquema)
                        .addValue("tipo", tipoCuadratura),
                this::mapCampoCuadratura);
    }

    private Cuadratura mapCuadratura(ResultSet rs, int row) throws SQLException {
        var c = new Cuadratura();
        c.setNombreEsquema(rs.getString("Esquema"));
        c.setVersionEsquema(rs.getString("Version"));
        c.setTipo(rs.getString("Tipo"));
        c.setDescripcion(rs.getString("Descripcion"));
        return c;
    }

    private CampoCuadratura mapCampoCuadratura(ResultSet rs, int row) throws SQLException {
        var cc = new CampoCuadratura();
        cc.setNombreEsquema(rs.getString("Esquema"));
        cc.setVersionEsquema(rs.getString("Version"));
        cc.setNombreCuadratura(rs.getString("NombreCuadratura"));
        cc.setNombreCampo(rs.getString("NombreCampo"));
        cc.setOrden(rs.getInt("Orden"));
        return cc;
    }
}
