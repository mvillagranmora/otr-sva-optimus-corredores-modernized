package cl.bcs.normativa.cartolas.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Config;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class ConfigService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ParametroService.class.getName());

    public static Map<String, String> lookupByIdEmpresa(
            final Connection connection,
            final String schema,
            final long idEmpresa)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.TB_CONFIG);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdEmpresa", idEmpresa);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final Map<String, String> map = new ConcurrentHashMap<>();
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                rs.setFetchSize(1000);
                while (rs.next()) {
                    final Config config = Config.newInstance(rs);
                    map.put(config.getNombre(), config.getValor());
                }
            }
        }

        return map;
    }

    public static void save(
            final ContextoCartolas contexto,
            final Config config)
                    throws SQLException {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.SP_GRABAR_CONFIG))
                .append("(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setLong(4, config.getId());
            cstmt.setString(5, config.getDescripcion());
            cstmt.setString(6, config.getTipo());
            cstmt.setString(7, config.getNombre());
            cstmt.setString(8, config.getValor());
            cstmt.registerOutParameter(9, Types.INTEGER);
            cstmt.registerOutParameter(10, Types.INTEGER);
            cstmt.registerOutParameter(11, Types.INTEGER);
            cstmt.execute();
            config.setId(cstmt.getLong(9));
        }
    }

    private ConfigService() {
    }
}
