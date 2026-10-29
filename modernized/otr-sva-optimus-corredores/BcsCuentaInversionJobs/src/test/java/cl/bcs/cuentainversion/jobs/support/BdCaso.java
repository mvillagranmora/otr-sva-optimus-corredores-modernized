package cl.bcs.cuentainversion.jobs.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * The in-memory H2 database (MODE=LEGACY) of one case for the MODERN side: the same schema, base seed and case seed the legacy oracle
 * used, every SQL text translated by {@link H2Hana}. {@link #transacciones()} records the physical commits and rollbacks in order (what
 * the oracle records on the legacy connection). {@link #setup()} is a separate connection for the seed and the table dumps: a dump sees
 * only what the run committed.
 */
public final class BdCaso {

    private final String url = "jdbc:h2:mem:cijobsmod_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
    private final List<String> eventos = Collections.synchronizedList(new ArrayList<>());
    private final Connection setup;
    private final DataSource ds;
    private final Registro tm;

    BdCaso(String esquema, String base, String semilla) {
        try {
            setup = DriverManager.getConnection(url, "sa", "");
            script(esquema);
            script(base);
            if (!semilla.isBlank()) {
                script(semilla);
            }
        } catch (SQLException e) {
            throw new AssertionError("cannot build the H2 fixture: " + e, e);
        }
        ds = new AbstractDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return H2Hana.wrap(DriverManager.getConnection(url, "sa", ""));
            }

            @Override
            public Connection getConnection(String u, String p) throws SQLException {
                return getConnection();
            }
        };
        tm = new Registro(ds);
    }

    private void script(String sql) throws SQLException {
        StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.strip().startsWith("--")) {
                clean.append(line).append('\n');
            }
        }
        try (Statement st = setup.createStatement()) {
            for (String stmt : clean.toString().split(";\\s*\n")) {
                if (!stmt.isBlank()) {
                    st.execute(stmt);
                }
            }
        }
    }

    public DataSource dataSource() {
        return ds;
    }

    public JdbcTemplate jdbc() {
        return new JdbcTemplate(ds);
    }

    public Connection setup() {
        return setup;
    }

    public DataSourceTransactionManager transacciones() {
        return tm;
    }

    /** "commit" / "rollback" in the order they reached the database connection. */
    public List<String> eventos() {
        return List.copyOf(eventos);
    }

    static String pila(Throwable t) {
        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    private final class Registro extends DataSourceTransactionManager {
        Registro(DataSource d) {
            super(d);
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            eventos.add("commit");
            super.doCommit(status);
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            eventos.add("rollback");
            super.doRollback(status);
        }
    }
}
