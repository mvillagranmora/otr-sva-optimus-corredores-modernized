package cl.bcs.plataforma.cre;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.adapter.in.javacall.ConsultarEstadoModuloHandler;

/**
 * Runs the 29 golden cases of the legacy oracle (analysis/.../equivalence/BcsPlataformaServicios) on the NEW code, the way
 * OracleRunner does: fresh in-memory H2 (MODE=LEGACY), the oracle schema + base seed + case seed, autocommit off. The goldens are only
 * read, never written. Fails (never skips) when the oracle directory cannot be found.
 */
final class OraculoCasos {

    static final AtomicInteger DB = new AtomicInteger();

    record Resultado(String salidaCruda, Throwable excepcion) { }

    private OraculoCasos() { }

    static Path base() {
        Path p = Path.of("").toAbsolutePath();
        while (p != null) {
            Path c = p.resolve("analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaServicios");
            if (Files.isDirectory(c.resolve("cases"))) return c;
            p = p.getParent();
        }
        throw new IllegalStateException("oracle directory analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaServicios not found above "
                + Path.of("").toAbsolutePath());
    }

    static List<Path> casos() throws Exception {
        try (Stream<Path> s = Files.list(base().resolve("cases"))) {
            return s.filter(Files::isDirectory).sorted().collect(Collectors.toList());
        }
    }

    static Path caso(String prefijo) throws Exception {
        return casos().stream().filter(c -> c.getFileName().toString().startsWith(prefijo)).findFirst().orElseThrow();
    }

    static JsonObject json(Path f) throws Exception {
        return JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    static Connection abrir(Path dir) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:h2:mem:plaesm" + DB.incrementAndGet() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        script(con, base().resolve("schema-plataforma-esm.sql"));
        script(con, base().resolve("seed/base-esm.sql"));
        if (Files.exists(dir.resolve("seed.sql"))) script(con, dir.resolve("seed.sql"));
        con.commit();
        con.setAutoCommit(false);
        return con;
    }

    /** Runs the NEW handler on a fresh database seeded for the case; the connection is closed (rolled back) afterwards. */
    static Resultado ejecutar(Path dir) throws Exception {
        JsonObject entrada = json(dir.resolve("case.json")).getAsJsonObject("entrada").deepCopy();
        try (Connection con = abrir(dir)) {
            try {
                String s = new ConsultarEstadoModuloHandler().call(con, entrada).toString();
                con.commit();
                return new Resultado(s, null);
            } catch (Throwable t) {
                con.rollback();
                return new Resultado(null, t);
            }
        }
    }

    static void script(Connection con, Path file) throws Exception {
        List<String> limpio = new ArrayList<>();
        for (String linea : Files.readString(file, StandardCharsets.UTF_8).split("\n")) {
            if (!linea.strip().startsWith("--")) limpio.add(linea);
        }
        try (Statement st = con.createStatement()) {
            for (String s : String.join("\n", limpio).split(";\\s*\n")) {
                if (!s.isBlank()) st.execute(s);
            }
        }
    }
}
