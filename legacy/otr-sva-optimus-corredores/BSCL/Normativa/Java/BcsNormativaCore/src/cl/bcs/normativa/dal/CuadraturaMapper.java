package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Cuadratura;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Cuadratura}.
 *
 * @author fpino@email.com
 *
 */
final class CuadraturaMapper {

    /**
     * Obtiene una nueva instancia de {@link Cuadratura} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Cuadratura} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Cuadratura map(final ResultSet rs) throws SQLException {
        final Cuadratura cuadratura = new Cuadratura();
        cuadratura.setNombreEsquema(JdbcUtil.getString(rs, "Esquema"));
        cuadratura.setVersionEsquema(JdbcUtil.getString(rs, "Version"));
        cuadratura.setTipo(JdbcUtil.getString(rs, "Tipo"));
        cuadratura.setDescripcion(JdbcUtil.getString(rs, "Descripcion"));
        return cuadratura;
    }

    private CuadraturaMapper() {
    }
}
