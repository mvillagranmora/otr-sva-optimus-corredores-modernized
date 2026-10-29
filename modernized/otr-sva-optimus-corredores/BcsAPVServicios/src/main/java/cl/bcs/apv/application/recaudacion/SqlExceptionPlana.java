package cl.bcs.apv.application.recaudacion;

import java.sql.SQLException;

/**
 * The legacy {@code SQL} helper rethrew every driver failure as a plain {@code SQLException} (the goldens pin that simple class name); a JDBC driver
 * throws subclasses (H2 {@code JdbcSQLSyntaxErrorException}, HANA ...): this rebuilds the plain type keeping message, SQLState, vendor code and cause.
 */
final class SqlExceptionPlana {

    private SqlExceptionPlana() {
    }

    static SQLException de(SQLException e) {
        if (e.getClass() == SQLException.class) {
            return e;
        }
        return new SQLException(e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
    }
}
