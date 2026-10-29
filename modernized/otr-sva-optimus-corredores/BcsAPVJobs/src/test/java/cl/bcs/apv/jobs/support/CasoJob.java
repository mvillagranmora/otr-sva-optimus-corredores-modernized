package cl.bcs.apv.jobs.support;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * One recorded case under {@code src/test/resources/apv-jobs/cases/<nombre>/}: {@code case.json} (input), optional {@code seed.sql} and
 * {@code expected.json} recorded from the UNMODIFIED legacy job by {@code legacy-oracle/runner/OracleRunner.java}.
 * A missing case directory, case.json or expected.json FAILS the test; it never skips.
 *
 * <p>Each instance owns a fresh H2 database (MODE=LEGACY) built from schema-apv-jobs.sql + seed/base-apv-jobs.sql + the case seed, reached through ONE
 * connection wrapped by {@link H2Hana} (HANA temporary tables live per connection, as in the legacy), and a temporary base folder for the job files.
 */
public final class CasoJob {

    public static final String RAIZ = "apv-jobs";

    private final String nombre;
    private final Path dir;
    private final JsonObject caso;
    private final JsonObject esperado;
    private final Connection conexion;
    private final SingleConnectionDataSource ds;
    private final NamedParameterJdbcTemplate jdbc;
    private final EventosTx tm;
    private final Path baseDir;

    private CasoJob(String nombre) {
        this.nombre = nombre;
        this.dir = recurso(RAIZ + "/cases/" + nombre);
        this.caso = leer(dir.resolve("case.json"));
        this.esperado = leer(dir.resolve("expected.json"));
        try {
            Connection raw = DriverManager.getConnection("jdbc:h2:mem:apvjobs_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
            ejecutar(raw, recurso(RAIZ + "/schema-apv-jobs.sql"));
            ejecutar(raw, recurso(RAIZ + "/seed/base-apv-jobs.sql"));
            if (Files.exists(dir.resolve("seed.sql"))) {
                ejecutar(raw, dir.resolve("seed.sql"));
            }
            raw.setAutoCommit(false);
            raw.commit();
            this.conexion = H2Hana.wrap(raw);
            this.ds = new SingleConnectionDataSource(conexion, true);
            this.jdbc = new NamedParameterJdbcTemplate(ds);
            this.tm = new EventosTx(ds);
            this.baseDir = Files.createTempDirectory("apvjobs-");
            if (caso.has("carpetas")) {
                for (JsonElement c : caso.getAsJsonArray("carpetas")) {
                    Files.createDirectories(baseDir.resolve(c.getAsString()));
                }
            }
        } catch (Exception e) {
            throw new AssertionError("cannot build the fixture of case " + nombre + ": " + e, e);
        }
    }

    public static CasoJob cargar(String nombre) {
        return new CasoJob(nombre);
    }

    public String nombre() { return nombre; }

    public JsonObject caso() { return caso; }

    public JsonObject esperado() { return esperado; }

    public Connection conexion() { return conexion; }

    public NamedParameterJdbcTemplate jdbc() { return jdbc; }

    public JdbcTemplate jdbcTemplate() { return jdbc.getJdbcTemplate(); }

    public EventosTx transactionManager() { return tm; }

    public Path baseDir() { return baseDir; }

    public String job() { return caso.get("job").getAsString(); }

    public long idPrograma() { return caso.get("idPrograma").getAsLong(); }

    public boolean pinMensaje() { return caso.has("pinMensaje") && caso.get("pinMensaje").getAsBoolean(); }

    public Map<String, String> parametros() {
        Map<String, String> m = new LinkedHashMap<>();
        if (caso.has("parametros")) {
            caso.getAsJsonObject("parametros").entrySet().forEach(e -> m.put(e.getKey(), e.getValue().getAsString()));
        }
        return m;
    }

    /** seed/config.json merged with the case "config" (JSON null removes a key): "MODULO/Parametro" -> value; "&lt;BASE&gt;" is this case's base folder. */
    public Map<String, String> config() {
        JsonObject cfg = leer(recurso(RAIZ + "/seed/config.json"));
        if (caso.has("config")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) cfg.remove(e.getKey()); else cfg.add(e.getKey(), e.getValue());
            }
        }
        Map<String, String> m = new LinkedHashMap<>();
        String base = baseDir.toAbsolutePath().toString().replace('\\', '/');
        cfg.entrySet().forEach(e -> m.put(e.getKey(), e.getValue().getAsString().replace("<BASE>", base)));
        return m;
    }

    public JsonObject colaboradores() {
        return caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject();
    }

    /** Dumps the tables the case declares, exactly as the oracle did. */
    public JsonObject tablas() {
        JsonObject t = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                try {
                    t.add(el.getAsJsonObject().get("nombre").getAsString(), Dump.tabla(conexion, el.getAsJsonObject()));
                } catch (SQLException e) {
                    throw new AssertionError("cannot dump " + el, e);
                }
            }
        }
        return t;
    }

    public static List<String> todos() {
        try (var s = Files.list(recurso(RAIZ + "/cases"))) {
            return s.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList();
        } catch (Exception e) {
            throw new AssertionError("cannot list " + RAIZ + "/cases", e);
        }
    }

    public static Path recurso(String ruta) {
        URL url = CasoJob.class.getClassLoader().getResource(ruta);
        if (url == null) {
            throw new AssertionError("missing test fixture on classpath: " + ruta + " (a missing fixture is a FAILURE, not a skip)");
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    private static JsonObject leer(Path p) {
        if (!Files.exists(p)) {
            throw new AssertionError("missing fixture " + p + " (record it with legacy-oracle/record.sh)");
        }
        try {
            return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("cannot read " + p, e);
        }
    }

    private static void ejecutar(Connection c, Path script) {
        ScriptUtils.executeSqlScript(c, new EncodedResource(new FileSystemResource(script), StandardCharsets.UTF_8));
    }

    /** Transaction manager that records the physical commits and rollbacks in order (the oracle recorded them on the legacy connection). */
    public static final class EventosTx extends DataSourceTransactionManager {
        private final List<String> eventos = new ArrayList<>();

        EventosTx(SingleConnectionDataSource ds) { super(ds); }

        @Override protected void doCommit(DefaultTransactionStatus s) { eventos.add("commit"); super.doCommit(s); }

        @Override protected void doRollback(DefaultTransactionStatus s) { eventos.add("rollback"); super.doRollback(s); }

        public List<String> eventos() { return eventos; }
    }

    /** Counts executed equivalence cases per test class so a green run that executed nothing is reported as not proven. */
    public static final class Contador {
        private final AtomicInteger n = new AtomicInteger();

        public void ejecutado(String caso) { n.incrementAndGet(); }

        public int total() { return n.get(); }

        public void imprimir(String job) { System.out.println("equivalence cases executed (" + job + "): " + n.get()); }
    }
}
