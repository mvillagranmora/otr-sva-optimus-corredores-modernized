package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Usuario;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Usuario.
 *
 * @author fpino@email.com
 *
 */
public final class UsuarioDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UsuarioDAO.class.getName());

    /**
     * Nombre de la tabla Agrupador.
     */
    private static final String NOMBRE_TABLA_USUARIO = "BSCL.Plataforma.BD::ACC.Usuario";

    /**
     * Busca usuario por id.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param idUsuario
     *            El id del usuario.
     * @return Usuario asociado al id.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Usuario buscarPorId(final Connection conn, final String schema,
            final long idUsuario) throws SQLException, RecordNotFoundException {

        final SqlSelect select = new SqlSelect(SqlMetadata.newTable(schema, NOMBRE_TABLA_USUARIO));
        select.getWhere().put("IdUsuario", idUsuario);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return UsuarioMapper.map(rs);
                }
            }
        }

        throw new RecordNotFoundException("Usuario no encontrado id: " + idUsuario);
    }

    private UsuarioDAO() {
    }

}
