package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Clasificador;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Clasificador.
 *
 * @author fpino@email.com
 *
 */
public final class ClasificadorDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ClasificadorDAO.class.getName());

    /**
     * Busca clasificador asociado al documento y nombre.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @param tipoClasificador
     *            El tipo de clasificador.
     * @return Clasificador asociado al documento y nombre.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Clasificador buscarPorDocumentoMasTipo(final Connection conn,
            final String schema, final Documento documento, final String tipoClasificador)
                    throws RecordNotFoundException, SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_CLASIFICADOR);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Esquema", documento.getNombreEsquema());
        select.getWhere().put("Version", documento.getVersionEsquema());
        select.getWhere().put("Tipo", tipoClasificador);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return ClasificadorMapper.map(rs);
                }
            }
        }

        throw new RecordNotFoundException("Clasificador no encontrado: " + tipoClasificador);
    }

    private ClasificadorDAO() {
    }

}
