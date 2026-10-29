package cl.bcs.normativa.cartolas.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Parametro;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class ParametroService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ParametroService.class.getName());

    public static Map<String, String> lookupByIdNomina(
            final Connection connection,
            final String schema,
            final long idNomina)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.TB_PARAMETRO);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdNomina", idNomina);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final Map<String, String> map = new ConcurrentHashMap<>();
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                rs.setFetchSize(1000);
                while (rs.next()) {
                    final Parametro parametro = Parametro.newInstance(rs);
                    map.put(parametro.getNombre(), parametro.getValor());
                }
            }
        }

        return map;
    }

    public static void save(
            final ContextoCartolas contexto,
            final Parametro parametro)
                    throws SQLException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.SP_GRABAR_PARAMETRO))
                .append("(?,?,?,?,?,?,?,?,?,?,?,?)}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setLong(4, parametro.getId());
            cstmt.setLong(5, parametro.getIdNomina());
            cstmt.setString(6, parametro.getNombre());
            cstmt.setString(7, parametro.getValor());
            cstmt.registerOutParameter(8, Types.INTEGER);
            cstmt.registerOutParameter(9, Types.INTEGER);
            cstmt.registerOutParameter(10, Types.INTEGER);
            cstmt.execute();
            parametro.setId(cstmt.getLong(8));
        }
    }

    public static void save(
            final ContextoCartolas contexto,
            final Properties properties)
                    throws SQLException {
        final Parametro parametro = new Parametro();
        for (final String nombre : properties.stringPropertyNames()) {
            parametro.setId(0);
            parametro.setIdNomina(contexto.getIdNomina());
            parametro.setNombre(nombre);
            parametro.setValor(properties.getProperty(nombre));
            save(contexto, parametro);
        }
    }

    private ParametroService() {
    }

}
