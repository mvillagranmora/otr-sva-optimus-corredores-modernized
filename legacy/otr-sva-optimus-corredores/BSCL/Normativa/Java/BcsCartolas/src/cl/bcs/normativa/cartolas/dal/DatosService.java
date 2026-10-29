package cl.bcs.normativa.cartolas.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Datos;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.dal.RecordNotFoundException;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class DatosService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();
    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DatosService.class.getName());

    public static Datos lookup(
            final Connection connection,
            final String schema,
            final Datos datos)
                    throws SQLException {
        final SqlSelect select = new SqlSelect(SqlMetadata.newTable(schema, Nombres.TB_DATOS));
        select.getWhere().put("IdCartola", datos.getIdCartola());
        select.getWhere().put("Nombre", datos.getNombre());

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Datos.newInstance(rs);
                }
            }
        }

        final String msg = String.format(
                "'Datos' no encontrados para IdCartola=%d, Nombre=%s, Tipo=%s",
                datos.getIdCartola(), datos.getNombre(), datos.getFormato());
        throw new RecordNotFoundException(msg);
    }

    public static Datos lookup(
            final ContextoCartolas contexto,
            final Datos datos)
                    throws SQLException {
        return lookup(contexto.getConnection(), contexto.getSchema(), datos);
    }

    public static Map<String, Datos> lookupByIdCartola(
            final Connection connection,
            final String schema,
            final long idCartola)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.TB_DATOS);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdCartola", idCartola);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final Map<String, Datos> map = new ConcurrentHashMap<>();
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                rs.setFetchSize(100);
                while (rs.next()) {
                    final Datos datos = Datos.newInstance(rs);
                    map.put(datos.getNombre(), datos);
                }
            }
        }

        return map;
    }

    public static void save(
            final Connection connection,
            final String schema,
            final Datos datos)
                    throws SQLException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("INSERT INTO ").append(SqlUtil.toQuotedString(schema)).append('.')
                .append(SqlUtil.toQuotedString(Nombres.TB_DATOS)).append('(')
                .append(SqlUtil.toQuotedString("IdCartola")).append(',')
                .append(SqlUtil.toQuotedString("Nombre")).append(',')
                .append(SqlUtil.toQuotedString("Formato")).append(',')
                .append(SqlUtil.toQuotedString("Valor")).append(") VALUES(?, ?, ?, ?)");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, datos.getIdCartola());
            pstmt.setString(2, datos.getNombre());
            pstmt.setString(3, datos.getFormato());
            pstmt.setString(4, datos.getValor());
            pstmt.executeUpdate();
        }
    }

    private DatosService() {
    }

}
