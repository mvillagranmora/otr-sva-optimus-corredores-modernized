package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Esquema.
 *
 * @author fpino@email.com
 *
 */
public final class EsquemaDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(EsquemaDAO.class.getName());

    /**
     * Busca esquema por nombre/versi&oacute;n.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param nombreEsquema
     *            El nombre del esquema.
     * @param versionEsquema
     *            La versi&oacute;n del esquema.
     * @return El esquema.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Esquema buscarPorClave(final Connection conn, final String schema,
            final String nombreEsquema, final String versionEsquema)
                    throws RecordNotFoundException, SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_ESQUEMA);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Esquema", nombreEsquema);
        select.getWhere().put("Version", versionEsquema);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return EsquemaMapper.map(rs);
                }
            }
        }

        throw new RecordNotFoundException(
                "Esquema no encontrado para " + nombreEsquema + "-" + versionEsquema);
    }

    /**
     * Busca esquema asociado a un documento (incluye los campos del esquema).
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @return El esquema asociado a un documento (incluye los campos del
     *         esquema).
     * @throws SQLException
     *             En caso de error SQL.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     */
    public static Esquema buscarPorDocumento(final Connection conn, final String schema,
            final Documento documento) throws SQLException, RecordNotFoundException {
        final Esquema esquema = buscarPorClave(conn, schema, documento.getNombreEsquema(),
                documento.getVersionEsquema());
        final List<Campo> campos = CampoDAO.buscarPorDocumento(conn, schema, documento);
        esquema.setCampos(campos);
        return esquema;
    }

    private EsquemaDAO() {
    }
}
