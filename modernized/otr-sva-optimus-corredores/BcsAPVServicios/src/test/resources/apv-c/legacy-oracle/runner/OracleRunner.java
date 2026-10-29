import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.familia_c.support.Dump;
import cl.bcs.apv.familia_c.support.H2Hana;
import cl.bcs.oracle.Hooks;
import cl.bcs.utils.Utils;

/**
 * Records golden expectations by executing the UNMODIFIED legacy classes of cl.bcs.APV (family C: recaudacion) against H2 (MODE=LEGACY, through
 * support/H2Hana.java) seeded with each case. Collaborators outside APV are scripted fakes (shims/ + case.json "colaboradores"); the other APV classes
 * the legacy calls in process (ValidaContratoSAAPV, CambiarEstadoContratosSAAPV ...) run for real.
 *
 * Usage: java OracleRunner &lt;apv-cResourcesDir&gt; [caseNamePrefix]
 * Reads  cases/&lt;case&gt;/{case.json, seed.sql}, schema-apv-c.sql, seed/base.sql, seed/config.json
 * Writes cases/&lt;case&gt;/expected.json
 *
 * case.json: { "servicio": "GrabarTraspasos" (class below cl.bcs.APV), "entrada": {...legacy JSON...}, "config": {"MODULO/Parametro": "valor" | null},
 *              "colaboradores": {...scripts, see shims/cl/bcs/oracle/Hooks.java...}, "secuencias": {"seq": firstValue},
 *              "tablas": [ {"nombre": "...", "orden": "\"Col\"", "columnas": ["Col"], "excluir": ["Col"], "enmascarar": ["Col"]} ],
 *              "enmascarar": ["Key"] (keys whose date/timestamp text is written as &lt;TS&gt; in the output and in the collaborator trace) }
 * The entry point is JavaCall.call(conn, entrada) = what the production handler invokes (legacy JSON in, legacy JSON out).
 *
 * Transaction model (production JavaCall handler): autocommit OFF, commit when the call returns, rollback when it throws.
 * No real HANA, service, bank or credential is touched.
 */
public class OracleRunner {

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static Path base;
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        Path cases = base.resolve("cases");
        int n = 0;
        int malos = 0;
        try (var dirs = Files.list(cases)) {
            for (Path dir : dirs.sorted().toList()) {
                if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix)) continue;
                System.out.println("== " + dir.getFileName());
                JsonObject expected;
                try {
                    expected = run(dir);
                } catch (IllegalStateException incompleto) {
                    // a case whose collaborator script is incomplete is a defect of the CASE: report it, record nothing for it, keep going
                    System.err.println("INCOMPLETE CASE " + dir.getFileName() + ": " + incompleto.getMessage());
                    malos++;
                    continue;
                }
                Files.writeString(dir.resolve("expected.json"), PRETTY.toJson(expected) + "\n", StandardCharsets.UTF_8);
                n++;
            }
        }
        System.out.println("cases recorded: " + n);
        if (malos > 0) {
            System.out.println("INCOMPLETE CASES (not recorded): " + malos);
            System.exit(2);
        }
    }

    static JsonObject run(Path dir) throws Exception {
        JsonObject caso = JsonParser.parseString(Files.readString(dir.resolve("case.json"))).getAsJsonObject();
        String servicio = caso.get("servicio").getAsString();
        int n = ++dbCounter;

        Connection raw = DriverManager.getConnection("jdbc:h2:mem:oracleapvc" + n + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        runScript(raw, base.resolve("schema-apv-c.sql"));
        runScript(raw, base.resolve("seed/base.sql"));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
        raw.commit();
        raw.setAutoCommit(false);
        Connection conn = H2Hana.wrap(raw);

        // configuration: base + case overrides (JSON null removes a key)
        JsonObject cfg = JsonParser.parseString(Files.readString(base.resolve("seed/config.json"))).getAsJsonObject();
        if (caso.has("config")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) cfg.remove(e.getKey()); else cfg.add(e.getKey(), e.getValue());
            }
        }
        Utils.CONFIG.clear();
        for (Map.Entry<String, JsonElement> e : cfg.entrySet()) Utils.CONFIG.put(e.getKey(), e.getValue().getAsString());
        Utils.SECUENCIAS.clear();
        Utils.SECUENCIA_LLAMADAS.clear();
        // case.json "secuencias": {"<sequence name>": firstValue}: values returned by Utils.getSecuencia (legacy reads HANA sequences)
        if (caso.has("secuencias")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("secuencias").entrySet()) {
                Utils.SECUENCIAS.put(e.getKey(), e.getValue().getAsLong());
            }
        }
        Hooks.reset(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject());

        JsonObject expected = new JsonObject();
        expected.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy cl.bcs.APV."
                + servicio + (caso.has("metodo") ? "." + caso.get("metodo").getAsString() : ".call") + " on H2 MODE=LEGACY. Do not edit by hand; re-record.");

        java.util.Set<String> mascara = Dump.claves(caso, "enmascarar");
        JsonObject entrada = caso.getAsJsonObject("entrada");
        try {
            Class<?> k = Class.forName("cl.bcs.APV." + servicio);
            Object svc = k.getDeclaredConstructor().newInstance();
            Object r;
            if (caso.has("metodo") && "logica".equals(caso.get("metodo").getAsString())) {
                // TYPED entry point (in-process callers such as the Tesoreria job BcsJobProcesarDistribucion): ValidarMandatosPAC.logica(Connection,
                // SalidaRecaudacionTraspaso, Long IdEmpresa, Long IdUsuario). entrada = {"entrada": {typed fields}, "idEmpresa", "idUsuario"}; the typed
                // object is decoded with the legacy Gson (new Gson()), as the job's JSON round trip does. The method returns void: the golden "salida" is null.
                Object tipada = new Gson().fromJson(entrada.get("entrada").toString(), cl.bcs.tesoreria.ProcesarDistribucion.SalidaRecaudacionTraspaso.class);
                Long idEmpresa = entrada.get("idEmpresa").isJsonNull() ? null : entrada.get("idEmpresa").getAsLong();
                Long idUsuario = entrada.get("idUsuario").isJsonNull() ? null : entrada.get("idUsuario").getAsLong();
                k.getMethod("logica", Connection.class, cl.bcs.tesoreria.ProcesarDistribucion.SalidaRecaudacionTraspaso.class, Long.class, Long.class)
                        .invoke(svc, conn, tipada, idEmpresa, idUsuario);
                r = null;
            } else {
                r = k.getMethod("call", Connection.class, JsonObject.class).invoke(svc, conn, entrada.deepCopy());
            }
            conn.commit();
            expected.add("salida", r == null ? com.google.gson.JsonNull.INSTANCE : Dump.sinHash(Dump.enmascarar((JsonElement) r, mascara)));
        } catch (Throwable t) {
            try { conn.rollback(); } catch (Exception ignored) { }
            Throwable c = t instanceof InvocationTargetException ite ? ite.getCause() : t;
            // a missing collaborator script is a defect of the CASE, never legacy behaviour: refuse to record it
            if (c instanceof IllegalStateException && String.valueOf(c.getMessage()).contains("colaboradores.")) {
                throw new IllegalStateException("case " + dir.getFileName() + " is incomplete: " + c.getMessage(), c);
            }
            String msg = String.valueOf(c.getMessage());
            // JVM-dependent noise is normalised so a re-record is reproducible (identity hash codes)
            // the helpful-NPE normalisation looks for "NullPointerException: Cannot ...", so it must see the class name too
            expected.addProperty("excepcion", Dump.sinHash(c.getClass().getSimpleName() + ": " + msg));
            if (Boolean.getBoolean("oracle.debug")) c.printStackTrace();
        }

        JsonObject tablas = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), Dump.tabla(raw, t));
            }
        }
        expected.add("tablas", tablas);

        JsonArray colab = new JsonArray();
        for (JsonObject o : Hooks.LLAMADAS) colab.add(Dump.sinHash(Dump.enmascarar(o, mascara)));
        expected.add("colaboradores", colab);
        expected.add("secuenciasSolicitadas", PRETTY.toJsonTree(Utils.SECUENCIA_LLAMADAS));
        raw.close();
        return expected;
    }

    static void runScript(Connection conn, Path file) throws Exception {
        runScript(conn, Files.readString(file, StandardCharsets.UTF_8));
    }

    static void runScript(Connection conn, String sql) throws Exception {
        StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (line.strip().startsWith("--")) continue;
            clean.append(line).append('\n');
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : clean.toString().split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
