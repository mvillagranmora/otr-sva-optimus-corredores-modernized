package org.apache.commons.dbcp2;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ORACLE SHIM, compile-time only: the legacy class under test references dbcp2 only from its main() (which holds a HANA host and
 * credentials in clear text, RULE-076, and is never executed here). Every method fails, so that nothing can ever open a real connection.
 */
public class BasicDataSource {
    public void setDriverClassName(String s) { }
    public void setUsername(String s) { }
    public void setPassword(String s) { }
    public void setUrl(String s) { }
    public void setValidationQuery(String s) { }
    public void setConnectionProperties(String s) { }
    public void setDefaultAutoCommit(Boolean b) { }

    public Connection getConnection() throws SQLException {
        throw new SQLException("oracle shim: no real data source is available (main() of the legacy class must never run)");
    }
}
