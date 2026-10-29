package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la vista ConsultarCodigoCorredora.
 *
 * @author fpino@email.com
 *
 */
public final class CodigoCorredoraDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CodigoCorredoraDAO.class.getName());

    /**
     * Nombre de la vista ConsultarCodigoCorredora.
     */
    private static final String NOMBRE_VISTA_CODIGO_CORREDORA = "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa";

    /**
     * Busca el c&oacute;digo de la corredora para la empresa utilizado en la
     * bolsa indicada.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param idEmpresa
     *            El id de la empresa.
     * @param idBolsa
     *            El id de la bolsa.
     * @return El c&oacute;digo de la corredora para la empresa utilizado en la
     *         bolsa indicada o, <code>null</code> si no existe el registro
     *         relacionado.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String buscarCodigoCorredora(final Connection conn, final String schema,
            final long idEmpresa, final String idBolsa) throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, NOMBRE_VISTA_CODIGO_CORREDORA);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdEmpresa", idEmpresa);
        select.getWhere().put("IdBolsa", idBolsa);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CodigoCorredora");
                }
            }
        }

        return null;

    }

    private CodigoCorredoraDAO() {
    }
}
