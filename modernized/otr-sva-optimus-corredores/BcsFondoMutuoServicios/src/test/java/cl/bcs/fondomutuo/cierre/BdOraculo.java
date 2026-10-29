package cl.bcs.fondomutuo.cierre;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * H2 in memory with the SAME schema and base universe the legacy oracle used to record the goldens ({@code schema-types/fmm/keys/session.sql} and
 * {@code seed/base-fmm.sql}, copied under {@code fondomutuo-3a/} by {@code legacy-oracle/sync3a.py}), plus the {@code seed.sql} of the case. One private
 * database per call (MODE=LEGACY). No HANA, no credentials. The script reader is the oracle's: comment lines dropped, statements split on {@code ;} + newline.
 */
final class BdOraculo {

    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    private BdOraculo() {
    }

    static DataSource nueva(String seedCaso) {
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:h2:mem:orc" + UUID.randomUUID().toString().replace("-", "")
                + ";DB_CLOSE_DELAY=-1;MODE=LEGACY", "sa", "");
        ds.setDriverClassName("org.h2.Driver");
        try (Connection c = ds.getConnection()) {
            for (String f : ESQUEMA) {
                ejecutar(c, leer("fondomutuo-3a/" + f));
            }
            if (seedCaso != null) {
                ejecutar(c, seedCaso);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return ds;
    }

    static String leer(String ruta) {
        try (InputStream in = BdOraculo.class.getClassLoader().getResourceAsStream(ruta)) {
            if (in == null) {
                throw new IllegalStateException("oracle fixture missing on the classpath: " + ruta + " (run legacy-oracle/sync3a.py)");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void ejecutar(Connection c, String script) throws SQLException {
        List<String> limpio = new ArrayList<>();
        for (String linea : script.split("\n")) {
            if (!linea.strip().startsWith("--")) {
                limpio.add(linea.replaceAll(";\\s+--[^']*$", ";"));
            }
        }
        try (Statement st = c.createStatement()) {
            for (String sentencia : String.join("\n", limpio).split(";\\s*\n")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        }
    }
}
