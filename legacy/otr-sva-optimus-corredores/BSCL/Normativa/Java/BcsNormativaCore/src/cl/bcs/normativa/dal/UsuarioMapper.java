package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Usuario;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Usuario}.
 *
 * @author fpino@email.com
 *
 */
public final class UsuarioMapper {

    /**
     * Obtiene una nueva instancia de {@link Usuario} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Usuario} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Usuario map(final ResultSet rs) throws SQLException {
        final Usuario usuario = new Usuario();
        usuario.setId(JdbcUtil.getLong(rs, "IdUsuario"));
        usuario.setCodigo(JdbcUtil.getString(rs, "CodigoUsuario"));
        usuario.setEstado(JdbcUtil.getString(rs, "IdEstado"));
        return usuario;
    }

    private UsuarioMapper() {
    }

}
