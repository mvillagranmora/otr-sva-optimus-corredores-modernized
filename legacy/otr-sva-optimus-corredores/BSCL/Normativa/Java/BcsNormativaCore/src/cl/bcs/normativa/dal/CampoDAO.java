package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlOrderByCondition;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Campo.
 *
 * @author fpino@email.com
 *
 */
public final class CampoDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CampoDAO.class.getName());

    /**
     * Busca los campos asociados a un documento.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @return Los campos asociados a un documento.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static List<Campo> buscarPorDocumento(final Connection conn, final String schema,
            final Documento documento) throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_CAMPO);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Esquema", documento.getNombreEsquema());
        select.getWhere().put("Version", documento.getVersionEsquema());
        select.getOrderBy().add(new SqlOrderByCondition("Orden", "ASC"));

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final List<Campo> campos = new ArrayList<>();
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    campos.add(CampoMapper.map(rs));
                }
            }
        }

        return campos;
    }

    private CampoDAO() {
    }

}
