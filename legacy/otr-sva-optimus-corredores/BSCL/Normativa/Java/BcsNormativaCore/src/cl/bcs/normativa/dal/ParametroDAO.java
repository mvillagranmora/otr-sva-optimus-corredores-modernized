package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Parametro.
 *
 * @author fpino@email.com
 *
 */
public final class ParametroDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Busca valor de par&aacute;metro configurado para un esquema.
     * 
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param esquema
     *            El esquema.
     * @param codigo
     *            El c&oacute;digo del par&aacute;metro.
     * @return Valor del par&aacute;metro configurado para un esquema.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String buscarValor(final Connection conn, final String schema,
            final Esquema esquema, final String codigo) throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_PARAMETRO);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Esquema", esquema.getNombre());
        select.getWhere().put("Version", esquema.getVersion());
        select.getWhere().put("Codigo", codigo);

        final String sql = GENERATOR.getSqlText(select);
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Valor");
                }
            }
        }

        final String msg = String.format("Parametro no encontrado para %s-%s.%s",
                esquema.getNombre(), esquema.getVersion(), codigo);
        throw new RecordNotFoundException(msg);
    }

    ParametroDAO() {
    }

}
