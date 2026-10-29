package cl.bcs.plataforma.login.h2;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

/**
 * A fresh in-memory H2 database with the login schema and the base configuration, for ONE test. {@link #con()} is the connection the code under test
 * receives (HANA dialect bridge, autocommit off, as the legacy pool); {@link #dataSource()} hands out more of them for the controllers and filters,
 * which take their own connection per request. {@link #consulta} reads through a separate raw connection, so it only sees what was COMMITTED.
 * The JVM must run in UTC (the tests set it).
 */
public final class LoginDb implements AutoCloseable {

    private static final AtomicInteger NUMERO = new AtomicInteger();

    private final String url;
    private final Connection mantiene;       // keeps the in-memory database alive and carries the schema
    private final Connection lector;         // raw, autocommit: sees only committed data
    private final Connection con;

    private LoginDb(String url) throws Exception {
        this.url = url;
        this.mantiene = DriverManager.getConnection(url, "sa", "");
        ejecutarScript(mantiene, "/login/schema-login.sql");
        ejecutarScript(mantiene, "/login/schema-bootstrap.sql");
        ejecutarScript(mantiene, "/login/base-login.sql");
        this.lector = DriverManager.getConnection(url, "sa", "");
        this.con = nuevaConexion();
    }

    public static LoginDb nueva() throws Exception {
        return new LoginDb("jdbc:h2:mem:login" + NUMERO.incrementAndGet() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1");
    }

    /** The connection of the code under test (bridge, autocommit off). */
    public Connection con() {
        return con;
    }

    /** Executes raw statements (seed data) and commits. */
    public LoginDb sql(String... sentencias) throws SQLException {
        try (Statement st = mantiene.createStatement()) {
            for (String s : sentencias) {
                st.execute(s);
            }
        }
        return this;
    }

    /** First column of the first row as text (committed data only), or null. */
    public String valor(String sql) throws SQLException {
        try (Statement st = lector.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    /** Rows of a query as text, columns separated by "|" (committed data only). */
    public List<String> filas(String sql) throws SQLException {
        List<String> filas = new ArrayList<>();
        try (Statement st = lector.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int n = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder f = new StringBuilder();
                for (int i = 1; i <= n; i++) {
                    if (i > 1) f.append('|');
                    f.append(rs.getString(i));
                }
                filas.add(f.toString());
            }
        }
        return filas;
    }

    /** A DataSource whose connections are bridged, with autocommit off, to this database. */
    public DataSource dataSource() {
        return (DataSource) Proxy.newProxyInstance(LoginDb.class.getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
            if (m.getName().equals("getConnection")) {
                return nuevaConexion();
            }
            if (m.getName().equals("toString")) {
                return "LoginDb.dataSource";
            }
            throw new UnsupportedOperationException(m.getName());
        });
    }

    private Connection nuevaConexion() throws SQLException {
        Connection raw = DriverManager.getConnection(url, "sa", "");
        raw.setAutoCommit(false);
        Connection puenteada = HanaH2.wrap(raw);
        return puenteada;
    }

    @Override
    public void close() throws Exception {
        try {
            con.close();
        } finally {
            lector.close();
            try (Statement st = mantiene.createStatement()) {
                st.execute("DROP ALL OBJECTS");
            } finally {
                mantiene.close();
            }
        }
    }

    private static void ejecutarScript(Connection c, String recurso) throws Exception {
        try (InputStream in = LoginDb.class.getResourceAsStream(recurso)) {
            if (in == null) throw new IllegalStateException("missing test resource " + recurso);
            String texto = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder limpio = new StringBuilder();
            for (String linea : texto.split("\n")) {
                if (!linea.strip().startsWith("--")) limpio.append(linea).append('\n');
            }
            try (Statement st = c.createStatement()) {
                for (String sentencia : limpio.toString().split(";\\s*\n")) {
                    if (!sentencia.isBlank()) st.execute(sentencia);
                }
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    @SuppressWarnings("unused")
    private static Object invocar(java.lang.reflect.Method m, Object o, Object[] a) throws Throwable {
        try {
            return m.invoke(o, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
