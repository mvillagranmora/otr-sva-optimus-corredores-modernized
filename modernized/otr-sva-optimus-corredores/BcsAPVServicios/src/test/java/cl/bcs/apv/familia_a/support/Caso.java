package cl.bcs.apv.familia_a.support;

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

import javax.sql.DataSource;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * One recorded case under {@code src/test/resources/apv-a/cases/<nombre>/}: {@code case.json} (input),
 * optional {@code seed.sql}, and {@code expected.json} recorded from the UNMODIFIED legacy classes by
 * {@code legacy-oracle/runner/OracleRunner.java}.
 *
 * <p>A missing case directory, case.json or expected.json FAILS the test (AssertionError); it never skips.
 *
 * <p>Each instance owns a fresh H2 database ({@code MODE=LEGACY}) built from schema-apv-a.sql + seed/base-apv-a.sql +
 * the case seed, accessed through ONE connection wrapped by {@link H2Hana} (HANA temporary tables live per connection, as in the
 * legacy where a single Connection is threaded through every call). The production SQL resources run unmodified.
 */
public final class Caso {

    public static final String RAIZ = "apv-a";

    private final String nombre;
    private final Path dir;
    private final JsonObject caso;
    private final JsonObject esperado;
    private final Connection conexion;
    private final DataSource ds;
    private final NamedParameterJdbcTemplate jdbc;

    private Caso(String nombre) {
        this.nombre = nombre;
        this.dir = recurso(RAIZ + "/cases/" + nombre);
        this.caso = leerObjeto(dir.resolve("case.json"));
        this.esperado = leerObjeto(dir.resolve("expected.json"));
        try {
            Connection raw = DriverManager.getConnection("jdbc:h2:mem:apva_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
            ejecutar(raw, recurso(RAIZ + "/schema-apv-a.sql"));
            ejecutar(raw, recurso(RAIZ + "/seed/base-apv-a.sql"));
            if (Files.exists(dir.resolve("seed.sql"))) {
                ejecutar(raw, dir.resolve("seed.sql"));
            }
            // transaction model of the platform (JavaCall dispatcher), same as the oracle: autocommit off, commit on return, rollback on throw
            raw.setAutoCommit(false);
            this.conexion = H2Hana.wrap(raw);
            this.ds = new SingleConnectionDataSource(conexion, true);
            this.jdbc = new NamedParameterJdbcTemplate(ds);
        } catch (SQLException e) {
            throw new AssertionError("cannot build H2 fixture for case " + nombre + ": " + e, e);
        }
    }

    /** Runs the code under test with the platform transaction model: commit when it returns, rollback when it throws (the exception is handed on). */
    public <T> T transaccion(java.util.concurrent.Callable<T> codigo) throws Exception {
        try {
            T r = codigo.call();
            conexion.commit();
            return r;
        } catch (Throwable t) {
            conexion.rollback();
            throw t instanceof Exception e ? e : new RuntimeException(t);
        }
    }

    public static Caso cargar(String nombre) {
        return new Caso(nombre);
    }

    public String nombre() {
        return nombre;
    }

    public JsonObject caso() {
        return caso;
    }

    /** Legacy entry point recorded for the case: call (default), logica, autorizarEvento or rechazarEvento. */
    public String metodo() {
        return caso().has("metodo") ? caso().get("metodo").getAsString() : "call";
    }

    public JsonObject entrada() {
        return caso.getAsJsonObject("entrada");
    }

    public JsonObject esperado() {
        return esperado;
    }

    public Connection conexion() {
        return conexion;
    }

    public DataSource dataSource() {
        return ds;
    }

    public NamedParameterJdbcTemplate jdbc() {
        return jdbc;
    }

    public JdbcTemplate jdbcTemplate() {
        return jdbc.getJdbcTemplate();
    }

    /** base seed/config.json merged with the case "config" overrides (JSON null removes the key): "MODULO/Parametro" -> valor. */
    public Map<String, String> config() {
        JsonObject cfg = leerObjeto(recurso(RAIZ + "/seed/config.json"));
        if (caso.has("config")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) {
                    cfg.remove(e.getKey());
                } else {
                    cfg.add(e.getKey(), e.getValue());
                }
            }
        }
        Map<String, String> m = new LinkedHashMap<>();
        cfg.entrySet().forEach(e -> m.put(e.getKey(), e.getValue().getAsString()));
        return m;
    }

    /** true when the legacy reads the REAL wall clock for the behaviour under test (System.currentTimeMillis in Java): the case runs with the real clock. */
    public boolean relojReal() {
        return caso.has("relojReal") && caso.get("relojReal").getAsBoolean();
    }

    /** Keys whose date/timestamp values are masked in the collaborator call log (case.json "enmascararColaboradores"). */
    public java.util.Set<String> enmascararColaboradores() {
        java.util.Set<String> s = new java.util.HashSet<>();
        if (caso.has("enmascararColaboradores")) {
            caso.getAsJsonArray("enmascararColaboradores").forEach(e -> s.add(e.getAsString()));
        }
        return s;
    }

    /** The scripted answers of the fake collaborators (case.json "colaboradores"). */
    public JsonObject colaboradores() {
        return caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject();
    }

    /** Dumps the tables the case declares, exactly as the oracle did. */
    public JsonObject tablas() {
        JsonObject t = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject spec = el.getAsJsonObject();
                try {
                    t.add(spec.get("nombre").getAsString(), Dump.tabla(conexion, spec));
                } catch (SQLException e) {
                    throw new AssertionError("cannot dump " + spec.get("nombre"), e);
                }
            }
        }
        return t;
    }

    /** Every case directory present on the test classpath, sorted. */
    public static List<String> todos() {
        try (var s = Files.list(recurso(RAIZ + "/cases"))) {
            return s.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList();
        } catch (Exception e) {
            throw new AssertionError("cannot list " + RAIZ + "/cases", e);
        }
    }

    public static Path recurso(String ruta) {
        URL url = Caso.class.getClassLoader().getResource(ruta);
        if (url == null) {
            throw new AssertionError("missing test fixture on classpath: " + ruta + " (a missing fixture is a FAILURE, not a skip)");
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    private static JsonObject leerObjeto(Path p) {
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

    /** Counts executed equivalence cases per test class so a green run that executed nothing is reported as not proven. */
    public static final class Contador {
        private final AtomicInteger n = new AtomicInteger();
        private final List<String> casos = new ArrayList<>();

        public void ejecutado(String caso) {
            n.incrementAndGet();
            synchronized (casos) {
                casos.add(caso);
            }
        }

        public int total() {
            return n.get();
        }

        public void imprimir(String servicio) {
            System.out.println("equivalence cases executed (" + servicio + "): " + n.get());
        }
    }
}
