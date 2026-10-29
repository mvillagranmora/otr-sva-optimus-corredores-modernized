package cl.bcs.cuentainversion.reportes.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One recorded case under {@code src/test/resources/reportes/cases/<nombre>/}: {@code case.json} (input),
 * optional {@code seed.sql} and shared {@code seeds}, and {@code expected.json}, recorded from the UNMODIFIED legacy class by
 * {@code reportes/legacy-oracle/runner/OracleRunner.java} (see the README).
 *
 * <p>A missing case directory, case.json, expected.json or seed FAILS the test (AssertionError); it never skips.
 *
 * <p>Each instance owns a fresh H2 database (MODE=LEGACY) built from schema-reportes.sql + seed/base-reportes.sql + the case
 * seeds, exactly like the oracle, and exposes a DataSource whose SQL goes through {@link H2Dialect} (the same adapter the
 * oracle uses) so the HANA SQL resources run on both sides. The connection follows the oracle transaction model:
 * autocommit off, commit when the call returns, rollback when it throws.
 */
public final class CasoReporte {

    public static final String RAIZ = "reportes";
    public static final String ZONA = "America/Santiago";

    private final String nombre;
    private final Path dir;
    private final JsonObject caso;
    private final JsonObject esperado;
    private final Connection conn;
    private final DataSource ds;
    private final NamedParameterJdbcTemplate jdbc;
    private final Path salida;

    private CasoReporte(String nombre) {
        this.nombre = nombre;
        this.dir = recurso(RAIZ + "/cases/" + nombre);
        this.caso = leerObjeto(dir.resolve("case.json"));
        this.esperado = leerObjeto(dir.resolve("expected.json"));
        try {
            this.salida = Files.createTempDirectory("reportes-out-");
            if (caso.has("directorios")) {
                for (JsonElement d : caso.getAsJsonArray("directorios")) {
                    Files.createDirectories(salida.resolve(d.getAsString()));
                }
            }
            Connection raw = DriverManager.getConnection("jdbc:h2:mem:reportes_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
            ejecutar(raw, recurso(RAIZ + "/schema-reportes.sql"));
            raw.setAutoCommit(false);
            ejecutar(raw, recurso(RAIZ + "/seed/base-reportes.sql"));
            if (caso.has("seeds")) {
                for (JsonElement s : caso.getAsJsonArray("seeds")) {
                    ejecutar(raw, recurso(RAIZ + "/seed/" + s.getAsString()));
                }
            }
            if (Files.exists(dir.resolve("seed.sql"))) {
                ejecutar(raw, dir.resolve("seed.sql"));
            }
            raw.commit();
            this.conn = conDialecto(raw);
            this.ds = new SingleConnectionDataSource(conn, true);
            this.jdbc = new NamedParameterJdbcTemplate(ds);
        } catch (Exception e) {
            throw new AssertionError("cannot build H2 fixture for case " + nombre + ": " + e, e);
        }
    }

    public static CasoReporte cargar(String nombre) {
        return new CasoReporte(nombre);
    }

    public String nombre() {
        return nombre;
    }

    public JsonObject caso() {
        return caso;
    }

    public JsonObject entrada() {
        return JsonParser.parseString(caso.getAsJsonObject("entrada").toString().replace("${OUT}", salida.toString().replace('\\', '/'))).getAsJsonObject();
    }

    public JsonObject esperado() {
        return esperado;
    }

    public DataSource dataSource() {
        return ds;
    }

    public NamedParameterJdbcTemplate jdbc() {
        return jdbc;
    }

    public Path directorioSalida() {
        return salida;
    }

    /** A modern service entry point; like the legacy {@code JavaCall.call} it may throw checked exceptions (IOException, Exception). */
    @FunctionalInterface
    public interface Servicio {
        JsonObject ejecutar(JsonObject entrada) throws Exception;
    }

    /** Outcome of running the modern implementation on the case. */
    public record Resultado(JsonObject salida, String excepcion, JsonObject tablas) {
    }

    /**
     * Runs the modern implementation under the oracle transaction model and the oracle time zone, and snapshots the tables
     * that changed.
     */
    public Resultado ejecutar(Servicio servicio) {
        TimeZone previa = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(ZONA));
        try {
            JsonObject antes = Dump.snapshot(conn);
            JsonObject out = null;
            String exc = null;
            try {
                out = servicio.ejecutar(entrada());
                conn.commit();
            } catch (Throwable t) {
                conn.rollback();
                exc = t.getClass().getName();
            }
            return new Resultado(out, exc, Dump.cambios(antes, Dump.snapshot(conn)));
        } catch (Exception e) {
            throw new AssertionError("cannot snapshot database for " + nombre + ": " + e, e);
        } finally {
            TimeZone.setDefault(previa);
        }
    }

    /**
     * Asserts the modern outcome equals the legacy golden: JavaCall output (canonical JSON), exception class, and the set of changed
     * tables with their rows. The exception MESSAGE is not compared (it embeds JVM-specific text).
     */
    public void assertEquivalente(Resultado r) {
        if (esperado.has("excepcion")) {
            String exp = esperado.get("excepcion").getAsString();
            String expClase = exp.contains(":") ? exp.substring(0, exp.indexOf(':')) : exp;
            assertThat(r.excepcion()).as(nombre + ": legacy threw " + exp).isEqualTo(expClase);
        } else {
            assertThat(r.excepcion()).as(nombre + ": legacy returned normally, modern threw").isNull();
            JsonElement actual = r.salida() == null ? null : JsonParser.parseString(Mask.texto(r.salida().toString().replace(salida.toString().replace('\\', '/'), "${OUT}")));
            JsonCanon.assertEquivalente(nombre + " salida", esperado.get("salida"), actual, sinOrden());
        }
        JsonCanon.assertEquivalente(nombre + " tablas modificadas", esperado.get("tablas"), r.tablas(), sinOrden());
    }

    /** case.json "sinOrden": JSON keys whose arrays are compared as multisets (the legacy order comes from an unordered SQL). */
    public String[] sinOrden() {
        if (!caso.has("sinOrden")) {
            return new String[0];
        }
        List<String> l = new ArrayList<>();
        caso.getAsJsonArray("sinOrden").forEach(e -> l.add(e.getAsString()));
        return l.toArray(new String[0]);
    }

    /** Lists every case directory on the test classpath. */
    public static List<String> todos() {
        try (var s = Files.list(recurso(RAIZ + "/cases"))) {
            return s.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList();
        } catch (Exception e) {
            throw new AssertionError("cannot list " + RAIZ + "/cases", e);
        }
    }

    public static Path recurso(String ruta) {
        URL url = CasoReporte.class.getClassLoader().getResource(ruta);
        if (url == null) {
            throw new AssertionError("missing test fixture on classpath: " + ruta + " (a missing fixture is a FAILURE, not a skip)");
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public static JsonObject leerObjeto(Path p) {
        if (!Files.exists(p)) {
            throw new AssertionError("missing fixture " + p + " (record it with legacy-oracle/runner/OracleRunner.java)");
        }
        return JsonParser.parseString(leer(p)).getAsJsonObject();
    }

    static String leer(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("cannot read " + p, e);
        }
    }

    private void ejecutar(Connection c, Path script) throws Exception {
        String sql = leer(script).replace("${OUT}", salida.toString().replace('\\', '/'));
        StringBuilder limpio = new StringBuilder();
        for (String linea : sql.split("\n")) {
            if (!linea.strip().startsWith("--")) {
                limpio.append(linea).append('\n');
            }
        }
        try (Statement st = c.createStatement()) {
            for (String s : limpio.toString().split(";\\s*\n")) {
                if (!s.isBlank()) {
                    st.execute(s);
                }
            }
        }
    }

    // ------------------------------------------------------------------ HANA dialect on the modern side
    private static Connection conDialecto(Connection real) {
        InvocationHandler h = (proxy, m, args) -> {
            try {
                String n = m.getName();
                if ((n.equals("prepareStatement") || n.equals("prepareCall")) && args != null && args.length > 0 && args[0] instanceof String s) {
                    Object[] a = args.clone();
                    String traducida = H2Dialect.translate(s);
                    a[0] = traducida;
                    Object ps = m.invoke(real, a);
                    return n.equals("prepareStatement") && ps instanceof PreparedStatement p ? conNulosTipados(p, contarParametros(traducida)) : ps;
                }
                if (n.equals("createStatement")) {
                    return conDialecto((Statement) m.invoke(real, args));
                }
                if (n.equals("close")) {
                    return null;
                }
                return m.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(CasoReporte.class.getClassLoader(), new Class<?>[]{Connection.class}, h);
    }

    /**
     * H2 cannot type an untyped NULL parameter in {@code SELECT ? AS col} (HANA can). Like the oracle SQL shim, an untyped NULL
     * (setNull(i, Types.NULL) or setObject(i, null)) is bound as a VARCHAR null; comparisons with it stay unknown, so nothing else changes.
     */
    private static PreparedStatement conNulosTipados(PreparedStatement real, int parametros) {
        InvocationHandler h = (proxy, m, args) -> {
            try {
                String n = m.getName();
                // A HANA UPSERT ... WHERE k = ? is rewritten as MERGE ... KEY(k) VALUES(..), which no longer has the WHERE
                // parameters (they repeat the VALUES ones, trailing): the JDBC binds of the dropped placeholders are ignored.
                if (n.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer idx && idx > parametros) {
                    return null;
                }
                if (n.equals("setNull") && args != null && args.length >= 2 && args[1] instanceof Integer t && t == java.sql.Types.NULL) {
                    return m.invoke(real, args[0], java.sql.Types.VARCHAR);
                }
                if (n.equals("setObject") && args != null && args.length == 2 && args[1] == null) {
                    real.setNull((Integer) args[0], java.sql.Types.VARCHAR);
                    return null;
                }
                return m.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (PreparedStatement) Proxy.newProxyInstance(CasoReporte.class.getClassLoader(), new Class<?>[]{PreparedStatement.class}, h);
    }

    private static int contarParametros(String sql) {
        int n = 0;
        boolean comillas = false;
        for (char c : sql.toCharArray()) {
            if (c == '\'' || c == '"') {
                comillas = !comillas;
            } else if (c == '?' && !comillas) {
                n++;
            }
        }
        return n;
    }

    private static Statement conDialecto(Statement real) {
        Set<String> conSql = Set.of("execute", "executeQuery", "executeUpdate", "executeLargeUpdate", "addBatch");
        InvocationHandler h = (proxy, m, args) -> {
            try {
                if (conSql.contains(m.getName()) && args != null && args.length > 0 && args[0] instanceof String s) {
                    Object[] a = args.clone();
                    a[0] = H2Dialect.translate(s);
                    return m.invoke(real, a);
                }
                return m.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Statement) Proxy.newProxyInstance(CasoReporte.class.getClassLoader(), new Class<?>[]{Statement.class}, h);
    }

    /** Message codes of a JavaCall output, in order (P_OUT_MENSAJES[].codigo). */
    public static List<String> codigos(JsonObject salida) {
        List<String> out = new ArrayList<>();
        JsonArray a = salida == null ? null : salida.getAsJsonArray("P_OUT_MENSAJES");
        if (a != null) {
            a.forEach(e -> {
                JsonElement c = e.getAsJsonObject().get("codigo");
                out.add(c == null || c.isJsonNull() ? "<message without codigo>" : c.getAsString());
            });
        }
        return out;
    }
}
