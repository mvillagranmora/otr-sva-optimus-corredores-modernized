package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;

/**
 * M&eacute;todos para acceder a la tabla Configuracion.
 *
 * @author fpino@email.com
 *
 */
public final class ConfiguracionDAO {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Busca valor de par&aacute;metro configurado para una empresa por nombre.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param idEmpresa
     *            El id de la empresa.
     * @param codigoParametro
     *            El c&oacute;digo del par&aacute;metro.
     * @return Valor del par&aacute;metro configurado para una empresa por
     *         nombre.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String buscarPorEmpresaMasCodigo(final Connection conn, final String schema,
            final long idEmpresa, final String codigoParametro)
                    throws RecordNotFoundException, SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_CONFIGURACION);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdEmpresa", idEmpresa);
        select.getWhere().put("CodigoParametro", codigoParametro);

        final String sql = GENERATOR.getSqlText(select);
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Valor");
                }
            }
        }

        final String msg = String.format(
                "Configuracion no encontrada para IdEmpresa=%d, CodigoParametro=%s", idEmpresa,
                codigoParametro);
        throw new RecordNotFoundException(msg);
    }
    
    public static List<String> buscarDiccionarioPorTipo(final Connection conn, final String schema, final String codigoTipo, 
    		final String activo, final String objetivo, final String producto)
                    throws RecordNotFoundException, SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_DICCIONARIO);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Tipo", codigoTipo);
        select.getWhere().put("Activo", activo);
        select.getWhere().put("Objetivo", objetivo);
        select.getWhere().put("Producto", producto);
        final String sql = GENERATOR.getSqlText(select);
        List<String> valores = new ArrayList<>();
        
        try (final PreparedStatement pstmt = conn.prepareStatement(sql);
                final ResultSet rs = pstmt.executeQuery()) {

               while (rs.next()) {
                   valores.add(rs.getString("Codigo"));
               }
           }

           if (valores.isEmpty()) {
               String msg = String.format("Configuracion no encontrada para Codigo Tipo=%s" , codigoTipo);
               throw new RecordNotFoundException(msg);
           }

           return valores;
    }

    private ConfiguracionDAO() {
    }

}
