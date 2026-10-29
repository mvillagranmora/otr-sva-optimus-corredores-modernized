package cl.bcs.contabilidad.jobs.support;

import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mockito.Mockito;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * One InterfazContable case RECORDED FROM THE UNMODIFIED LEGACY job by the BcsContabilidadServicios legacy oracle
 * ({@code BcsContabilidadServicios/src/test/resources/envio/legacy-oracle/runner/OracleRunner.java}, which runs
 * {@code cl.bcs.contabilidad.jobs.InterfazContable.transaction()} under a BcsJob shim against H2 and a fake SAP
 * Service Layer). The files are read IN PLACE from that module, not copied:
 * {@code <envio>/cases/<name>/{case.json,expected.json,seed.sql}}, {@code <envio>/schema-envio.sql},
 * {@code <envio>/seed/{base-envio.sql,config.json,plan-cuentas.json}}.
 *
 * <p>{@code <envio>} = system property {@code contabilidad.servicios.envio}, default
 * {@code <basedir>/../BcsContabilidadServicios/src/test/resources/envio}. A missing directory or file is an
 * {@link AssertionError}: a comparison that cannot run is a failure, never a skip.
 */
public final class CasoEnvioLegacy {

    private final String nombre;
    private final Path raiz;
    private final JsonObject caso;
    private final JsonObject esperado;

    private CasoEnvioLegacy(String nombre) {
        this.nombre = nombre;
        this.raiz = raiz();
        Path dir = existe(raiz.resolve("cases").resolve(nombre));
        this.caso = objeto(existe(dir.resolve("case.json")));
        this.esperado = objeto(existe(dir.resolve("expected.json")));
        if (!"InterfazContable".equals(caso.get("servicio").getAsString())) {
            throw new AssertionError(nombre + " is not an InterfazContable case");
        }
        if (!esperado.has("_provenance") || !esperado.get("_provenance").getAsString().contains("OracleRunner")) {
            throw new AssertionError(nombre + "/expected.json was not recorded by the legacy oracle");
        }
    }

    public static CasoEnvioLegacy cargar(String nombre) {
        return new CasoEnvioLegacy(nombre);
    }

    public static Path raiz() {
        String prop = System.getProperty("contabilidad.servicios.envio");
        Path p = prop != null ? Path.of(prop)
                : Path.of(System.getProperty("basedir", System.getProperty("user.dir")))
                .resolve("../BcsContabilidadServicios/src/test/resources/envio").normalize();
        return existe(p);
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

    /** Builds the case's database in {@code db}: schema-envio.sql + seed/base-envio.sql + the case seed.sql. */
    public void sembrar(H2Jobs db) {
        try (Connection c = db.dataSource().getConnection()) {
            ejecutar(c, existe(raiz.resolve("schema-envio.sql")));
            ejecutar(c, existe(raiz.resolve("seed/base-envio.sql")));
            Path seed = raiz.resolve("cases").resolve(nombre).resolve("seed.sql");
            if (Files.exists(seed)) ejecutar(c, seed);
        } catch (Exception e) {
            throw new AssertionError("cannot build the H2 fixture of " + nombre + ": " + e, e);
        }
    }

    /** seed/config.json + case "config" overrides (null removes), keyed "MODULO/Parametro", plus extra entries. */
    public Map<String, String> config(Map<String, String> extra) {
        JsonObject cfg = objeto(existe(raiz.resolve("seed/config.json")));
        if (caso.has("config")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) cfg.remove(e.getKey()); else cfg.add(e.getKey(), e.getValue());
            }
        }
        Map<String, String> m = new LinkedHashMap<>();
        cfg.entrySet().forEach(e -> m.put(e.getKey(), e.getValue().getAsString()));
        m.putAll(extra);
        return m;
    }

    public List<CuentaSap> planCuentas() {
        JsonArray plan = caso.has("planCuentas") ? caso.getAsJsonArray("planCuentas")
                : JsonParser.parseString(leer(existe(raiz.resolve("seed/plan-cuentas.json")))).getAsJsonArray();
        List<CuentaSap> out = new ArrayList<>();
        Gson gson = new Gson();
        plan.forEach(e -> out.add(gson.fromJson(e, CuentaSap.class)));
        return out;
    }

    /** Same deterministic dictionary as the oracle shim: "[CODIGO]{k=v}" (keys sorted). */
    public static DiccionarioPort diccionario() {
        return (idEmpresa, codigo, valores) -> "[" + codigo + "]" + new TreeMap<>(valores == null ? Map.of() : valores);
    }

    /** PlataformaPort over a "MODULO/Parametro" map; an absent key returns the caller's default. */
    public static PlataformaPort plataforma(Map<String, String> config) {
        return Mockito.mock(PlataformaPort.class, inv -> switch (inv.getMethod().getName()) {
            case "obtenerParametroConfiguracion" -> {
                String k = inv.getArgument(1) + "/" + inv.getArgument(2);
                yield config.containsKey(k) ? config.get(k) : inv.getArgument(3);
            }
            case "obtenerMonedaSistema" -> config.get("PLATAFORMA/PLA.MonedaSistema");
            case "obtenerLocalizacionSistema" -> config.get("PLATAFORMA/PLA.LocalizacionSistema");
            case "obtenerFechaSistema" -> "2025-04-01";
            case "toString" -> "FakePlataforma" + config;
            case "hashCode" -> System.identityHashCode(inv.getMock());
            case "equals" -> inv.getMock() == inv.getArgument(0);
            default -> throw new UnsupportedOperationException("PlataformaPort." + inv.getMethod().getName());
        });
    }

    private static Path existe(Path p) {
        if (!Files.exists(p)) {
            throw new AssertionError("missing legacy-oracle fixture " + p.toAbsolutePath()
                    + " (a missing fixture is a FAILURE, not a skip; set -Dcontabilidad.servicios.envio=<dir>)");
        }
        return p;
    }

    private static JsonObject objeto(Path p) {
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
