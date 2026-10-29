import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import cl.bcs.apv.familia_a.support.Dump;
import cl.bcs.apv.familia_a.support.H2Hana;
import cl.bcs.oracle.Hooks;
import cl.bcs.utils.Utils;

/**
 * Records golden expectations by executing the UNMODIFIED legacy classes of cl.bcs.APV (family A: contratos SAAPV) against H2 (MODE=LEGACY,
 * through support/H2Hana.java) seeded with each case. Collaborators of other modules are scripted fakes (legacy-oracle/shims, Hooks).
 *
 * Usage: java OracleRunner <apv-aResourcesDir> [caseNamePrefix]
 * Reads  cases/&lt;case&gt;/{case.json, seed.sql}, schema-apv-a.sql, seed/base-apv-a.sql, seed/config.json
 * Writes cases/&lt;case&gt;/expected.json (or $ORACLE_OUT/&lt;case&gt;.expected.json when the system property oracle.out is set, used by the mutation checks)
 *
 * case.json: { "servicio": "GrabarContratoSAAPV" (class of cl.bcs.APV),
 *              "metodo": "call" | "logica", "entrada": {...},
 *              "config": {"MODULO/Parametro": "valor" | null}, "colaboradores": {...scripted fakes...},
 *              "secuencias": {"<sequence>": firstValue},
 *              "tablas": [ {"nombre": "...", "orden": "\"Col\"", "columnas": ["Col"], "excluir": ["Col"], "expresiones": {"alias": "sql"}} ] }
 * "metodo" call (default) invokes JavaCall.call(conn, entrada) = the production entry point; "logica" invokes the typed
 * logica(Connection, Entrada) with the entrada decoded by Utils.getGsonBuilder() and the result encoded the same way.
 * No real HANA, service or credential is touched.
 */
public class OracleRunner {

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static Path base;
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        Path cases = base.resolve("cases");
        String outDir = System.getProperty("oracle.out");
        int n = 0;
        try (var dirs = Files.list(cases)) {
            for (Path dir : dirs.sorted().toList()) {
                if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix)) continue;
                System.out.println("== " + dir.getFileName());
                JsonObject expected = run(dir);
                String text = PRETTY.toJson(expected) + "\n";
                if (outDir != null) {
                    Files.createDirectories(Path.of(outDir));
                    Files.writeString(Path.of(outDir).resolve(dir.getFileName() + ".expected.json"), text, StandardCharsets.UTF_8);
                } else {
                    Files.writeString(dir.resolve("expected.json"), text, StandardCharsets.UTF_8);
                }
                n++;
            }
        }
        System.out.println("cases recorded: " + n);
    }

    static JsonObject run(Path dir) throws Exception {
        JsonObject caso = JsonParser.parseString(Files.readString(dir.resolve("case.json"))).getAsJsonObject();
        String servicio = caso.get("servicio").getAsString();
        String metodo = caso.has("metodo") ? caso.get("metodo").getAsString() : "call";
        int n = ++dbCounter;

        Connection raw = DriverManager.getConnection("jdbc:h2:mem:oracle" + n + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        runScript(raw, base.resolve("schema-apv-a.sql"));
        runScript(raw, base.resolve("seed/base-apv-a.sql"));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, dir.resolve("seed.sql"));
        // transaction model of the platform (JavaCall dispatcher): autocommit off, commit when the service returns, rollback when it throws
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
        if (caso.has("secuencias")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("secuencias").entrySet()) {
                Utils.SECUENCIAS.put(e.getKey(), e.getValue().getAsLong());
            }
        }
        Hooks.reset(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject());

        JsonObject expected = new JsonObject();
        expected.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy "
                + servicio + "." + metodo + " on H2 MODE=LEGACY (JVM zone UTC). Do not edit by hand; re-record.");

        try {
            JsonElement out = invoke(conn, servicio, metodo, caso.getAsJsonObject("entrada"));
            expected.add("salida", Dump.sinMensajeNpe(out));
            raw.commit();
        } catch (Throwable t) {
            raw.rollback();
            Throwable c = t instanceof InvocationTargetException ite ? ite.getCause() : t;
            expected.addProperty("excepcion", c.getClass().getSimpleName() + ": " + c.getMessage());
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
        java.util.Set<String> claves = new java.util.HashSet<>();
        if (caso.has("enmascararColaboradores")) for (JsonElement k : caso.getAsJsonArray("enmascararColaboradores")) claves.add(k.getAsString());
        for (JsonObject o : Hooks.LLAMADAS) colab.add(Dump.enmascararClaves(o, claves));
        expected.add("colaboradores", colab);
        expected.add("secuenciasSolicitadas", PRETTY.toJsonTree(Utils.SECUENCIA_LLAMADAS));
        raw.close();
        return expected;
    }

    static JsonElement invoke(Connection conn, String servicio, String metodo, JsonObject entrada) throws Throwable {
        if (metodo.equals("pojo")) {
            // data holders without behaviour (e.g. pojo.Traspaso): the characterised behaviour is the state of a freshly built instance
            JsonObject out = new JsonObject();
            Gson g = Utils.getGsonBuilder();
            for (JsonElement n : entrada.getAsJsonArray("clases")) {
                Class<?> pk = Class.forName(n.getAsString());
                var ctor = pk.getDeclaredConstructor();
                ctor.setAccessible(true);
                out.add(n.getAsString(), g.toJsonTree(ctor.newInstance()));
            }
            return out;
        }
        Class<?> k = Class.forName("cl.bcs.APV." + servicio);
        Object svc = k.getDeclaredConstructor().newInstance();
        if (metodo.equals("call")) {
            Object r = k.getMethod("call", Connection.class, JsonObject.class).invoke(svc, conn, entrada);
            return r == null ? com.google.gson.JsonNull.INSTANCE : (JsonElement) r;
        }
        // "logica", or any other typed entry point (e.g. the BcsAutorizaciones interface methods autorizarEvento / rechazarEvento)
        String nombreMetodo = metodo.equals("call") ? "call" : metodo;
        Method logica = null;
        for (Method m : k.getMethods()) {
            if (m.getName().equals(nombreMetodo) && m.getParameterCount() == 2 && m.getParameterTypes()[0] == Connection.class) logica = m;
        }
        if (logica == null) throw new IllegalStateException("no " + nombreMetodo + "(Connection, X) in " + servicio);
        Gson g = Utils.getGsonBuilder();
        Object in = g.fromJson(entrada, logica.getParameterTypes()[1]);
        Object r = logica.invoke(svc, conn, in);
        return r == null ? com.google.gson.JsonNull.INSTANCE : g.toJsonTree(r);
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

    @SuppressWarnings("unused")
    static String stack(Throwable t) {
        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }
}
