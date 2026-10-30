package cl.bcs.canalagente.caracterizacion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test harness only (copy of cl.bcs.fondomutuo.BdEfimeraH2 of BcsFondoMutuoServicios). Opens the "keeper" connection of an in-memory H2 database created with {@code DB_CLOSE_DELAY=-1} and,
 * when it is closed, runs {@code SHUTDOWN} first: with that option the database otherwise lives until the JVM dies, and the hundreds of case databases of the whole suite (schema + seeds each)
 * exhaust the heap of the single test JVM. {@code SHUTDOWN} closes the database for every connection, so it must be done only after all the results and table dumps of the case were collected.
 */
final class BdEfimeraH2 {

    private BdEfimeraH2() {
    }

    static Connection abrir(String url) throws SQLException {
        Connection raw = DriverManager.getConnection(url, "sa", "");
        return (Connection) Proxy.newProxyInstance(BdEfimeraH2.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if (m.getName().equals("close") && m.getParameterCount() == 0) {
                cerrar(raw);
                return null;
            }
            try {
                return m.invoke(raw, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    /** Destroys the database behind {@code raw} and closes it; never throws (the case is already over). */
    static void cerrar(Connection raw) {
        try {
            if (!raw.isClosed()) {
                try (Statement st = raw.createStatement()) {
                    st.execute("SHUTDOWN");
                }
            }
        } catch (SQLException ignorada) {
            // the database may already be closed
        }
        try {
            raw.close();
        } catch (SQLException ignorada) {
            // already closed by the SHUTDOWN
        }
    }
}
