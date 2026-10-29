import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.jobs.BcsJobCargaMasivaPrevired;
import cl.bcs.APV.jobs.IngresarOrdenBonificacionFiscal;
import cl.bcs.APV.jobs.IngresarOrdenFMBonificacionFiscal;
import cl.bcs.APV.jobs.IngresarOrdenRVBonificacionFiscal;
import cl.bcs.apv.jobs.support.Dump;
import cl.bcs.apv.jobs.support.H2Hana;
import cl.bcs.oracle.Hooks;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.scheduler.BcsJobListener;
import cl.bcs.utils.Utils;

/**
 * Records golden expectations by executing the UNMODIFIED legacy job classes of cl.bcs.APV.jobs (BcsAPVJobs) through the UNMODIFIED legacy
 * framework class cl.bcs.scheduler.BcsJob (its run(), non-daemon branch) against H2 (MODE=LEGACY, through support/H2Hana.java) seeded with each case.
 * The collaborators of other modules are scripted fakes (legacy-oracle/shims, Hooks); the job files are real files under a temporary base folder.
 *
 * Usage: java OracleRunner <apv-jobsResourcesDir> [caseNamePrefix]
 * Reads  cases/&lt;case&gt;/{case.json, seed.sql}, schema-apv-jobs.sql, seed/base-apv-jobs.sql, seed/config.json
 * Writes cases/&lt;case&gt;/expected.json (or $ORACLE_OUT/&lt;case&gt;.expected.json when the system property oracle.out is set: mutation checks)
 *
 * case.json: { "job": "BcsJobCargaMasivaPrevired" | ..., "idPrograma": n, "parametros": {"name": "text"},
 *              "config": {"MODULO/Parametro": "valor" | null}  ("&lt;BASE&gt;" in a value is the temporary base folder),
 *              "carpetas": ["APV"] (folders created under the base folder), "colaboradores": {...scripted fakes...},
 *              "pinMensaje": true (the exception / status message is a contract), "tablas": [ ...Dump specs... ] }
 */
public class OracleRunner {

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static final Pattern NPE_MSG = Dump.NPE_MSG;
    static Path base;
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        String outDir = System.getProperty("oracle.out");
        int n = 0;
        try (var dirs = Files.list(base.resolve("cases"))) {
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

    static class Captura {
        Boolean retorno;
        Throwable error;
        final List<JsonObject> estados = new ArrayList<>();
    }

    /** The job class under test; the subclass only OBSERVES the return value of transaction() and the Throwable given to onError(). */
    static BcsJob crear(String nombre, Captura c) {
        return switch (nombre) {
            case "BcsJobCargaMasivaPrevired" -> new BcsJobCargaMasivaPrevired() {
                @Override public boolean transaction(Connection con) throws InterruptedException, Throwable { boolean r = super.transaction(con); c.retorno = r; return r; }
                @Override public void onError(Throwable t) { c.error = t; super.onError(t); }
            };
            case "IngresarOrdenBonificacionFiscal" -> new IngresarOrdenBonificacionFiscal() {
                @Override public boolean transaction(Connection con) throws InterruptedException, Throwable { boolean r = super.transaction(con); c.retorno = r; return r; }
                @Override public void onError(Throwable t) { c.error = t; super.onError(t); }
            };
            case "IngresarOrdenFMBonificacionFiscal" -> new IngresarOrdenFMBonificacionFiscal() {
                @Override public boolean transaction(Connection con) throws InterruptedException, Throwable { boolean r = super.transaction(con); c.retorno = r; return r; }
                @Override public void onError(Throwable t) { c.error = t; super.onError(t); }
            };
            case "IngresarOrdenRVBonificacionFiscal" -> new IngresarOrdenRVBonificacionFiscal() {
                @Override public boolean transaction(Connection con) throws InterruptedException, Throwable { boolean r = super.transaction(con); c.retorno = r; return r; }
                @Override public void onError(Throwable t) { c.error = t; super.onError(t); }
            };
            default -> throw new IllegalArgumentException("unknown job " + nombre);
        };
    }

    static JsonObject run(Path dir) throws Exception {
        JsonObject caso = JsonParser.parseString(Files.readString(dir.resolve("case.json"))).getAsJsonObject();
        String nombreJob = caso.get("job").getAsString();
        long idPrograma = caso.get("idPrograma").getAsLong();
        int n = ++dbCounter;

        Connection raw = DriverManager.getConnection("jdbc:h2:mem:oraclejobs" + n + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        runScript(raw, base.resolve("schema-apv-jobs.sql"));
        runScript(raw, base.resolve("seed/base-apv-jobs.sql"));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, dir.resolve("seed.sql"));
        raw.setAutoCommit(false);
        Connection h2 = H2Hana.wrap(raw);
        List<String> eventos = new ArrayList<>();
        boolean[] cerrada = {false};
        Connection grabada = (Connection) Proxy.newProxyInstance(OracleRunner.class.getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
            switch (m.getName()) {
                case "commit" -> eventos.add("commit");
                case "rollback" -> eventos.add("rollback");
                case "close" -> { cerrada[0] = true; return null; }
                default -> { }
            }
            try {
                return m.invoke(h2, a);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
        javax.sql.DataSource ds = (javax.sql.DataSource) Proxy.newProxyInstance(OracleRunner.class.getClassLoader(), new Class<?>[]{javax.sql.DataSource.class}, (p, m, a) -> {
            if (m.getName().equals("getConnection")) return grabada;
            throw new UnsupportedOperationException(m.getName());
        });

        // working folder of the case (the job files are REAL files)
        Path work = Path.of(System.getProperty("user.dir")).resolve("case" + n);
        Path baseDir = work.resolve("base");
        if (Files.exists(work)) {
            try (var viejos = Files.walk(work)) {
                for (Path v : viejos.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(v);
            }
        }
        Files.createDirectories(baseDir);
        if (caso.has("carpetas")) for (JsonElement c : caso.getAsJsonArray("carpetas")) Files.createDirectories(baseDir.resolve(c.getAsString()));
        String baseTexto = baseDir.toAbsolutePath().toString().replace('\\', '/');

        JsonObject cfg = JsonParser.parseString(Files.readString(base.resolve("seed/config.json"))).getAsJsonObject();
        if (caso.has("config")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) cfg.remove(e.getKey()); else cfg.add(e.getKey(), e.getValue());
            }
        }
        Utils.CONFIG.clear();
        for (Map.Entry<String, JsonElement> e : cfg.entrySet()) Utils.CONFIG.put(e.getKey(), e.getValue().getAsString().replace("<BASE>", baseTexto));
        Hooks.reset(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject());
        // MonedaFormato caches the format table in a static field forever (legacy defect): every case starts with a clean cache
        java.lang.reflect.Field cache = cl.bcs.plataforma.pla.utils.MonedaFormato.class.getDeclaredField("formatos");
        cache.setAccessible(true);
        cache.set(null, null);
        cl.bcs.utils.SQL.SESION.clear();

        Map<String, String> params = new HashMap<>();
        if (caso.has("parametros")) for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("parametros").entrySet()) params.put(e.getKey(), e.getValue().getAsString());

        Captura cap = new Captura();
        BcsJob job = crear(nombreJob, cap);
        job.setParameters(idPrograma, params, 0L, false, ds, "**");
        job.setJobListener(new BcsJobListener() {
            public void status(long jobId, boolean isError, String msg) {
                JsonObject o = new JsonObject();
                o.addProperty("error", isError);
                o.addProperty("mensaje", msg);
                cap.estados.add(o);
            }

            public void updateDaemon(long jobId) {
            }
        });

        PrintStream out = System.out, err = System.err;
        boolean debug = Boolean.getBoolean("oracle.debug");
        if (!debug) {
            PrintStream mudo = new PrintStream(new ByteArrayOutputStream());
            System.setOut(mudo);
            System.setErr(mudo);
        }
        long t0 = System.currentTimeMillis();
        try {
            job.run();
        } finally {
            System.setOut(out);
            System.setErr(err);
        }
        long t1 = System.currentTimeMillis();

        JsonObject expected = new JsonObject();
        expected.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy " + nombreJob
                + " through the unmodified legacy BcsJob.run() on H2 MODE=LEGACY (JVM zone UTC). Do not edit by hand; re-record.");

        JsonObject res = new JsonObject();
        boolean huboError = !cap.estados.isEmpty() && cap.estados.get(0).get("error").getAsBoolean();
        String ultimo = eventos.isEmpty() ? "" : eventos.get(eventos.size() - 1);
        res.addProperty("desenlace", huboError ? "ERROR" : ultimo.equals("commit") ? "COMMIT" : "ROLLBACK");
        JsonArray ev = new JsonArray();
        eventos.forEach(ev::add);
        res.add("eventos", ev);
        JsonArray es = new JsonArray();
        for (JsonObject s : cap.estados) {
            JsonObject o = new JsonObject();
            o.add("error", s.get("error"));
            if (caso.has("pinMensaje") && caso.get("pinMensaje").getAsBoolean()) o.addProperty("mensaje", limpiar(s.get("mensaje").isJsonNull() ? null : s.get("mensaje").getAsString(), baseTexto));
            es.add(o);
        }
        res.add("estados", es);
        if (cap.retorno == null) res.add("retorno", com.google.gson.JsonNull.INSTANCE); else res.addProperty("retorno", cap.retorno);
        if (cap.error != null) {
            res.addProperty("excepcion", cap.error.getClass().getSimpleName());
            res.addProperty("esException", cap.error instanceof Exception);
            if (caso.has("pinMensaje") && caso.get("pinMensaje").getAsBoolean()) res.addProperty("excepcionMensaje", limpiar(cap.error.getMessage(), baseTexto));
        }
        expected.add("resultado", res);

        JsonObject tablas = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), Dump.tabla(raw, t));
            }
        }
        expected.add("tablas", tablas);

        JsonArray colab = new JsonArray();
        for (JsonObject o : Hooks.LLAMADAS) colab.add(o);
        expected.add("colaboradores", colab);

        // the job files: relative name (the run-clock stamp masked) -> content
        JsonObject archivos = new JsonObject();
        boolean[] marcaOk = {true};
        boolean[] hubo = {false};
        try (var paths = Files.walk(baseDir)) {
            for (Path f : paths.filter(Files::isRegularFile).sorted().toList()) {
                String rel = baseDir.relativize(f).toString().replace('\\', '/');
                Matcher m = Dump.MARCA_14.matcher(rel);
                if (m.find()) {
                    hubo[0] = true;
                    marcaOk[0] &= dentroDeLaCorrida(m.group(), t0, t1);
                }
                archivos.addProperty(Dump.enmascararInstante(rel), limpiar(Files.readString(f, StandardCharsets.UTF_8), baseTexto));
            }
        }
        expected.add("archivos", archivos);
        if (hubo[0]) expected.addProperty("marcaDeTiempoEsLaHoraDeLaCorrida", marcaOk[0]);
        raw.close();
        return expected;
    }

    static boolean dentroDeLaCorrida(String marca, long t0, long t1) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
        for (long t = t0 - 1000; t <= t1 + 1000; t += 500) if (fm.format(new Date(t)).equals(marca)) return true;
        return false;
    }

    static String limpiar(String s, String baseTexto) {
        if (s == null) return null;
        String r = s.replace(baseTexto, "<BASE>").replace(baseTexto.replace('/', '\\'), "<BASE>");
        return NPE_MSG.matcher(r).replaceAll(Dump.NPE_TOKEN);
    }

    static void runScript(Connection conn, Path file) throws Exception {
        StringBuilder clean = new StringBuilder();
        for (String line : Files.readString(file, StandardCharsets.UTF_8).split("\n")) {
            if (line.strip().startsWith("--")) continue;
            clean.append(line).append('\n');
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : clean.toString().split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
        conn.commit();
    }
}
