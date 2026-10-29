package cl.bcs.contabilidad.envio.support;

import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One recorded case under {@code src/test/resources/envio/cases/<nombre>/}:
 * {@code case.json} (input), optional {@code seed.sql}, and {@code expected.json} recorded from the
 * unmodified legacy classes by {@code envio/legacy-oracle/runner/OracleRunner.java}.
 *
 * <p>A missing case directory, case.json or expected.json FAILS the test (AssertionError); it never skips.
 *
 * <p>Each instance owns a fresh H2 database ({@code MODE=LEGACY}: accepts HANA {@code "seq".NEXTVAL},
 * keeps '' distinct from NULL) built from schema-envio.sql + seed/base-envio.sql + the case seed, so the
 * production SQL resources run unmodified against the same data the legacy oracle saw.
 */
public final class CasoEnvio {

    public static final String RAIZ = "envio";

    private final String nombre;
    private final Path dir;
    private final JsonObject caso;
    private final JsonObject esperado;
    private final SimpleDriverDataSource ds;
    private final NamedParameterJdbcTemplate jdbc;

    private CasoEnvio(String nombre) {
        this.nombre = nombre;
        this.dir = recurso(RAIZ + "/cases/" + nombre);
        this.caso = leerObjeto(dir.resolve("case.json"));
        this.esperado = leerObjeto(dir.resolve("expected.json"));
        this.ds = new SimpleDriverDataSource(new org.h2.Driver(),
                "jdbc:h2:mem:envio_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        this.jdbc = new NamedParameterJdbcTemplate(ds);
        try (Connection c = ds.getConnection()) {
            ejecutar(c, recurso(RAIZ + "/schema-envio.sql"));
            ejecutar(c, recurso(RAIZ + "/seed/base-envio.sql"));
            if (Files.exists(dir.resolve("seed.sql"))) {
                ejecutar(c, dir.resolve("seed.sql"));
            }
        } catch (Exception e) {
            throw new AssertionError("cannot build H2 fixture for case " + nombre + ": " + e, e);
        }
    }

    public static CasoEnvio cargar(String nombre) {
        return new CasoEnvio(nombre);
    }

    public String nombre() {
        return nombre;
    }

    public JsonObject caso() {
        return caso;
    }

    public JsonObject entrada() {
        return caso.getAsJsonObject("entrada");
    }

    public JsonObject esperado() {
        return esperado;
    }

    public NamedParameterJdbcTemplate jdbc() {
        return jdbc;
    }

    public JdbcTemplate jdbcTemplate() {
        return jdbc.getJdbcTemplate();
    }

    public boolean sinArchivoConfiguracion() {
        return caso.has("sinArchivoConfiguracion") && caso.get("sinArchivoConfiguracion").getAsBoolean();
    }

    /** seed/config.json merged with the case "config" overrides (a JSON null removes the key). */
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

    /** OACT rows the fake SAP HANA returned to the legacy oracle. */
    public List<CuentaSap> planCuentas() {
        JsonArray plan = caso.has("planCuentas") ? caso.getAsJsonArray("planCuentas")
                : JsonParser.parseString(leer(recurso(RAIZ + "/seed/plan-cuentas.json"))).getAsJsonArray();
        List<CuentaSap> out = new ArrayList<>();
        Gson gson = new Gson();
        plan.forEach(e -> out.add(gson.fromJson(e, CuentaSap.class)));
        return out;
    }

    /** Dumps a table exactly like the oracle (numbers normalised, optional excluded columns). */
    public JsonArray tabla(String tabla, Set<String> excluir, String orderBy) {
        return jdbcTemplate().query("SELECT * FROM \"" + tabla + "\" ORDER BY " + orderBy, rs -> {
            JsonArray rows = new JsonArray();
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String col = md.getColumnLabel(i);
                    if (excluir.contains(col)) {
                        continue;
                    }
                    Object v = rs.getObject(i);
                    if (v == null) {
                        row.add(col, JsonNull.INSTANCE);
                    } else if (v instanceof Number n) {
                        row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(n.toString()).stripTrailingZeros().toPlainString())));
                    } else {
                        row.addProperty(col, v.toString());
                    }
                }
                rows.add(row);
            }
            return rows;
        });
    }

    /** The three staging tables EnvioContabilidad writes (legacy EnvioContabilidad.java:673-677). */
    public JsonObject tablasEnvio() {
        JsonObject t = new JsonObject();
        t.add("PreComprobanteEnvioContabilidad", tabla("BSCL.Contabilidad.BD::ITZ.PreComprobanteEnvioContabilidad", Set.of(), "\"JdtNum\""));
        t.add("PreComprobanteAsientoEnvioContabilidad", tabla("BSCL.Contabilidad.BD::ITZ.PreComprobanteAsientoEnvioContabilidad",
                Set.of("IdPreComprobanteAsientoEnvioContabilidad"), "\"IdPreComprobanteAsientoEnvioContabilidad\""));
        t.add("PreComprobanteCentralizacionTipo", tabla("BSCL.Contabilidad.BD::ITZ.PreComprobanteCentralizacionTipo", Set.of(), "\"IdPreComprobante\""));
        return t;
    }

    /** Lists every case directory present on the test classpath. */
    public static List<String> todos() {
        try (var s = Files.list(recurso(RAIZ + "/cases"))) {
            return s.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList();
        } catch (Exception e) {
            throw new AssertionError("cannot list " + RAIZ + "/cases", e);
        }
    }

    public static Path recurso(String ruta) {
        URL url = CasoEnvio.class.getClassLoader().getResource(ruta);
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
            throw new AssertionError("missing fixture " + p + " (record it with legacy-oracle/runner/OracleRunner.java)");
        }
        return JsonParser.parseString(leer(p)).getAsJsonObject();
    }

    private static String leer(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("cannot read " + p, e);
        }
    }

    private static void ejecutar(Connection c, Path script) {
        ScriptUtils.executeSqlScript(c, new EncodedResource(new FileSystemResource(script), StandardCharsets.UTF_8));
    }
}
