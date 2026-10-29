package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Empresa;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Empresa.
 *
 * @author fpino@email.com
 *
 */
public final class EmpresaDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(EmpresaDAO.class.getName());

    /**
     * Busca empresa asociada al documento.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @return Empresa asociada al documento.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     */
    public static Empresa buscarPorDocumento(
            final Connection conn,
            final String schema,
            final Documento documento)
                    throws SQLException,
                    RecordNotFoundException {
        return buscarPorId(conn, schema, documento.getIdEmpresa());
    }

    public static Empresa buscarPorId(
            final Connection conn,
            final String schema,
            final long idEmpresa)
                    throws SQLException,
                    RecordNotFoundException {

        final SqlSelect select = new SqlSelect(
                SqlMetadata.newTable(schema, Nombres.NOMBRE_VISTA_CONSULTAR_EMPRESA));
        select.getWhere().put("IdEmpresa", idEmpresa);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return EmpresaMapper.map(rs);
                }
            }
        }

        throw new RecordNotFoundException("Empresa no encontrada id: " + idEmpresa);
    }

    private EmpresaDAO() {
    }

}
