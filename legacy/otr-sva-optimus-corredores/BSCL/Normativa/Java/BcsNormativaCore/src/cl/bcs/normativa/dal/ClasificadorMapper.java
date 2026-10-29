package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Clasificador;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Clasificador}.
 *
 * @author fpino@email.com
 *
 */
final class ClasificadorMapper {

    /**
     * Obtiene una nueva instancia de {@link Clasificador} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Clasificador} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Clasificador map(final ResultSet rs) throws SQLException {
        final Clasificador clasificador = new Clasificador();
        clasificador.setNombreEsquema(JdbcUtil.getString(rs, "Esquema"));
        clasificador.setVersionEsquema(JdbcUtil.getString(rs, "Version"));
        clasificador.setTipo(JdbcUtil.getString(rs, "Tipo"));
        clasificador.setDescripcion(JdbcUtil.getString(rs, "Descripcion"));
        return clasificador;
    }

    private ClasificadorMapper() {
    }
}
