package cl.bcs.sql.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class SqlConnectionBuilder {

    private static final String GET_CURRENT_SCHEMA_COMMAND = "SELECT CURRENT_SCHEMA FROM DUMMY;";

    public static Connection getConnection(final SqlConnectionSettings settings)
            throws SQLException {
        final Connection conn = DriverManager.getConnection(settings.getUrl(), settings.getUser(),
                settings.getPassword());
        return conn;
    }

    public static String getCurrentSchema(final Connection conn) throws SQLException {
        try (final PreparedStatement pstmt = conn.prepareStatement(GET_CURRENT_SCHEMA_COMMAND)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        return null;
    }

    private SqlConnectionBuilder() {
    }

}
