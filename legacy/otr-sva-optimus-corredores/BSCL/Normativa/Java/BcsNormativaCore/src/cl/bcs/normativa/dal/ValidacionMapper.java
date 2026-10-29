package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Validacion;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Validacion}.
 *
 * @author fpino@email.com
 *
 */
public final class ValidacionMapper {

    /**
     * Obtiene una nueva instancia de {@link Validacion} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Validacion} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Validacion map(final ResultSet rs) throws SQLException {
        final Validacion validacion = new Validacion();
        validacion.setNombreEsquema(JdbcUtil.getString(rs, "Esquema"));
        validacion.setVersionEsquema(JdbcUtil.getString(rs, "Version"));
        validacion.setCodigo(JdbcUtil.getString(rs, "Codigo"));
        validacion.setTipo(JdbcUtil.getString(rs, "Tipo"));
        validacion.setActiva(JdbcUtil.getStringAsBoolean(rs, "Activa"));
        validacion.setDescripcion(JdbcUtil.getString(rs, "Descripcion"));
        validacion.setSeveridad(JdbcUtil.getString(rs, "Severidad"));
        validacion.setFormula(JdbcUtil.getString(rs, "Formula"));
        return validacion;
    }

    private ValidacionMapper() {
    }

}
