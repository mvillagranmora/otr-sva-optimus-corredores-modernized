package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Cuadratura;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Cuadratura.
 *
 * @author fpino@email.com
 *
 */
public final class CuadraturaDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CuadraturaDAO.class.getName());

    /**
     * Busca la cuadratura de un documento dado el documento y el nombre de la
     * cuadratura.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @param tipoCuadratura
     *            El tipo de cuadratura.
     * @return La cuadratura de un documento o, <code>null</code> si no existe
     *         la cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Cuadratura buscarPorDocumentoMasTipo(final Connection conn, final String schema,
            final Documento documento, final String tipoCuadratura) throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_CUADRATURA);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Esquema", documento.getNombreEsquema());
        select.getWhere().put("Version", documento.getVersionEsquema());
        select.getWhere().put("Tipo", tipoCuadratura);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return CuadraturaMapper.map(rs);
                }
            }
        }

        return null;
    }

    private CuadraturaDAO() {
    }

}
