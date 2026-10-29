package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Validacion;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlOrderByCondition;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Validacion.
 *
 * @author fpino@email.com
 *
 */
public final class ValidacionDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ValidacionDAO.class.getName());

    /**
     * Nombre de la tabla Validacion.
     */
    private static final String NOMBRE_TABLA_VALIDACION = "BSCL.Normativa.BD::DOC.Validacion";

    /**
     * Busca lista de validaciones asociadas al documento.
     *
     * @param conn
     *            La conexi&oacute;n.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @return Lista de validaciones asociadas al documento.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static List<Validacion> buscarActivasPorDocumento(final Connection conn,
            final String schema, final Documento documento) throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, NOMBRE_TABLA_VALIDACION);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Esquema", documento.getNombreEsquema());
        select.getWhere().put("Version", documento.getVersionEsquema());
        select.getWhere().put("Activa", "S");
        select.getOrderBy().add(new SqlOrderByCondition("Codigo", "ASC"));

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final List<Validacion> validaciones = new ArrayList<>();
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    validaciones.add(ValidacionMapper.map(rs));
                }
            }
        }

        return validaciones;
    }

    private ValidacionDAO() {
    }

}
