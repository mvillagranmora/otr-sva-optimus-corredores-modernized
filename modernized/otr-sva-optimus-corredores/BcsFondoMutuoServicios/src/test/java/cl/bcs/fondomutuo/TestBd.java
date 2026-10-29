package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/** H2 in memory for the kit tests: a private database per call, the minimal kit schema ({@code fondomutuo-kit/schema-kit.sql}). No HANA, no credentials. */
public final class TestBd {

    private TestBd() {
    }

    public static DataSource nueva() {
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:h2:mem:kit" + UUID.randomUUID().toString().replace("-", "")
                + ";DB_CLOSE_DELAY=-1;MODE=LEGACY", "sa", "");
        ds.setDriverClassName("org.h2.Driver");
        try (Connection c = ds.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("fondomutuo-kit/schema-kit.sql"));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return ds;
    }

    public static void ejecutar(DataSource ds, String... sentencias) {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            for (String s : sentencias) {
                st.execute(s);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static long contar(DataSource ds, String tablaCitada) {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM " + tablaCitada)) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
