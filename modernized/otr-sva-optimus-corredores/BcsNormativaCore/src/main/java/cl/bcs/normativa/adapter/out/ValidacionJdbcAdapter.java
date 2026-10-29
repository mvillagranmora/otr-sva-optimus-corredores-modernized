package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.domain.Validacion;
import cl.bcs.normativa.port.out.ValidacionPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Adaptador JDBC para Validacion normativa.
 * HANA table: "BSCL.Normativa.BD::DOC.Validacion" (alias VALIDACION en H2)
 */
@Repository
public class ValidacionJdbcAdapter implements ValidacionPort {

    private final NamedParameterJdbcTemplate jdbc;

    public ValidacionJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Validacion> buscarActivasPorEsquema(String nombreEsquema, String versionEsquema) {
        var sql = """
                SELECT Esquema, Version, Codigo, Tipo, Activa, Descripcion, Severidad, Formula
                FROM VALIDACION
                WHERE Esquema = :esquema AND Version = :version AND Activa = 'S'
                ORDER BY Codigo ASC
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("esquema", nombreEsquema).addValue("version", versionEsquema),
                this::mapValidacion);
    }

    private Validacion mapValidacion(ResultSet rs, int row) throws SQLException {
        var v = new Validacion();
        v.setNombreEsquema(rs.getString("Esquema"));
        v.setVersionEsquema(rs.getString("Version"));
        v.setCodigo(rs.getString("Codigo"));
        v.setTipo(rs.getString("Tipo"));
        v.setActiva("S".equals(rs.getString("Activa")));
        v.setDescripcion(rs.getString("Descripcion"));
        v.setSeveridad(rs.getString("Severidad"));
        v.setFormula(rs.getString("Formula"));
        return v;
    }
}
