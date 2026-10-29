package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Documento;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.statements.SqlUpdate;
import cl.bcs.sql.utils.SqlUtil;

/**
 * M&eacute;todos para acceder a la tabla Documento.
 *
 * @author fpino@email.com
 *
 */
public final class DocumentoDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DocumentoDAO.class.getName());

    /**
     * Obtiene el documento asociado al id indicado.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param id
     *            El id del documento.
     * @return El documento asociado al id indicado.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Documento buscarPorId(final Connection conn, final String schema, final long id)
            throws RecordNotFoundException, SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_DOCUMENTO);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdDocumento", id);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return DocumentoMapper.map(rs);
                }
            }
        }

        throw new RecordNotFoundException("Documento no encontrado id: " + id);
    }

    /**
     * Actualiza el estado de proceso del documento seg&uacute;n el indicador.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @param enProceso
     *            Indica si el documento est&aacute; en proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setEnProceso(final Connection conn, final String schema,
            final Documento documento, final boolean enProceso) throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_DOCUMENTO);
        final SqlUpdate update = new SqlUpdate(table);

        documento.setEnProceso(enProceso);
        if (enProceso) {
            update.getValues().put("EnProceso", "S");
            documento.setFechaInicioUltimoProceso(new Date());
            update.getValues().put("FechaInicioUltimoProceso",
                    documento.getFechaInicioUltimoProceso());
        } else {
            update.getValues().put("EnProceso", "N");
        }

        update.getWhere().put("IdDocumento", documento.getId());

        final String sql = GENERATOR.getSqlText(update);
        LOGGER.info(sql);
        SqlUtil.executeUpdate(conn, sql);
    }

    private DocumentoDAO() {
    }

}
