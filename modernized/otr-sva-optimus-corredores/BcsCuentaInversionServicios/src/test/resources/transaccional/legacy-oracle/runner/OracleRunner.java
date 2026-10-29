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

import cl.bcs.cuentainversion.transaccional.support.Archivos;
import cl.bcs.cuentainversion.transaccional.support.Dump;
import cl.bcs.cuentainversion.transaccional.support.H2Hana;
import cl.bcs.oracle.Hooks;
import cl.bcs.utils.Utils;

/**
 * Records golden expectations by executing the UNMODIFIED legacy classes of cl.bcs.cuentaInversion (family "transaccional")
 * against H2 (MODE=LEGACY, through support/H2Hana.java) seeded with each case.
 *
 * Usage: java OracleRunner <transaccionalResourcesDir> [caseNamePrefix]
 * Reads  cases/&lt;case&gt;/{case.json, seed.sql}, schema-transaccional.sql, seed/base-transaccional.sql, seed/config.json
 * Writes cases/&lt;case&gt;/expected.json
 *
 * case.json: { "servicio": "CertificarCargoAbono", "metodo": "call" | "logica", "entrada": {...},
 *              "config": {"MODULO/Parametro": "valor" | null}, "colaboradores": {...scripted fakes...},
 *              "tablas": [ {"nombre": "...", "orden": "\"Col\"", "columnas": ["Col"], "excluir": ["Col"]} ] }
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
        int n = 0;
        try (var dirs = Files.list(cases)) {
            for (Path dir : dirs.sorted().toList()) {
                if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix)) continue;
                System.out.println("== " + dir.getFileName());
                JsonObject expected = run(dir);
                Files.writeString(dir.resolve("expected.json"), Archivos.mascara(PRETTY.toJson(expected)) + "\n", StandardCharsets.UTF_8);
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
        runScript(raw, base.resolve("schema-transaccional.sql"));
        runScript(raw, base.resolve("seed/base-transaccional.sql"));
        // ${RUTA_BASE} in the seed = the directory where the legacy generators write their csv (captured below, see support/Archivos)
        String semilla = Files.exists(dir.resolve("seed.sql")) ? Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8) : "";
        Path rutaBase = semilla.contains(Archivos.PLACEHOLDER) ? Archivos.nuevaRaiz() : null;
        if (!semilla.isEmpty()) runScript(raw, rutaBase == null ? semilla : Archivos.sustituir(semilla, rutaBase));
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
        expected.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy "
                + servicio + "." + metodo + " on H2 MODE=LEGACY. Do not edit by hand; re-record.");

        try {
            JsonElement out = invoke(conn, servicio, metodo, caso.getAsJsonObject("entrada"));
            expected.add("salida", out);
        } catch (Throwable t) {
            Throwable c = t instanceof InvocationTargetException ite ? ite.getCause() : t;
            String msg = String.valueOf(c.getMessage());
            if (rutaBase != null) {
                // machine specific temp path -> placeholder (also its native Windows form)
                msg = msg.replace(rutaBase.toAbsolutePath().toString(), Archivos.PLACEHOLDER).replace(Archivos.PLACEHOLDER + "\\", Archivos.PLACEHOLDER + "/");
            }
            expected.addProperty("excepcion", c.getClass().getSimpleName() + ": " + msg);
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
        if (rutaBase != null) expected.add("archivos", Archivos.capturar(rutaBase));

        JsonArray colab = new JsonArray();
        for (JsonObject o : Hooks.LLAMADAS) colab.add(o);
        expected.add("colaboradores", colab);
        expected.add("secuenciasSolicitadas", PRETTY.toJsonTree(Utils.SECUENCIA_LLAMADAS));
        raw.close();
        return expected;
    }

    static JsonElement invoke(Connection conn, String servicio, String metodo, JsonObject entrada) throws Throwable {
        Class<?> k = Class.forName("cl.bcs.cuentaInversion." + servicio);
        Object svc = k.getDeclaredConstructor().newInstance();
        if (metodo.equals("call")) {
            Object r = k.getMethod("call", Connection.class, JsonObject.class).invoke(svc, conn, entrada);
            return r == null ? com.google.gson.JsonNull.INSTANCE : (JsonElement) r;
        }
        Method logica = null;
        for (Method m : k.getMethods()) {
            if (m.getName().equals("logica") && m.getParameterCount() == 2 && m.getParameterTypes()[0] == Connection.class) logica = m;
        }
        if (logica == null) throw new IllegalStateException("no logica(Connection, X) in " + servicio);
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
