package cl.bcs.normativa.cartolas.caller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.sql.utils.SqlUtil;

public class CallTable {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CallTable.class.getName());

    public static CallTable newInstance(
            final Connection connection)
                    throws SQLException {
        return new CallTable(connection);
    }

    public static String newName() {
        final Date now = new Date();
        final long time = now.getTime();
        return "#temp" + time;
    }

    private final Connection connection;
    private final String name;

    public CallTable(
            final Connection connection) throws SQLException {
        this.connection = connection;
        this.name = newName();
        create();
    }

    private void create()
            throws SQLException {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("CREATE LOCAL TEMPORARY TABLE ").append(SqlUtil.toQuotedString(name)).append('(')
                .append(SqlUtil.toQuotedString("IdCliente")).append(" BIGINT, ")
                .append(SqlUtil.toQuotedString("IdPortafolio")).append(" BIGINT)");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void drop()
            throws SQLException {
        final String sql = String.format("DROP TABLE %s", SqlUtil.toQuotedString(name));

        try (final Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public String getName() {
        return name;
    }

    public void load(
            final CallParams params)
                    throws SQLException {
        final String sql = String.format("INSERT INTO %s VALUES(?, ?)",
                SqlUtil.toQuotedString(name));

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = params.getIndiceCartolaDesde(); i <= params.getIndiceCartolaHasta(); i++) {
                final Cartola cartola = params.getCartolas().get(i);
                pstmt.setLong(1, cartola.getIdCliente());
                pstmt.setLong(2, cartola.getIdPortafolio());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        }
    }

}
