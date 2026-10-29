import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.oracle.Hooks;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.scheduler.BcsJobListener;
import cl.bcs.cuentainversion.jobs.support.Archivos;
import cl.bcs.cuentainversion.jobs.support.Canon;
import cl.bcs.cuentainversion.jobs.support.Dump;
import cl.bcs.cuentainversion.jobs.support.H2Hana;
import cl.bcs.utils.Utils;

/**
 * Records golden expectations by EXECUTING the unmodified legacy jobs of BcsCuentaInversionJobs under the real legacy BcsJob.run()
 * (non-daemon branch; the PAUTA steps are called directly) on H2 (MODE=LEGACY), with scripted collaborators and real temporary files.
 *
 * Usage: java OracleRunner &lt;cuenta-inversion-jobs resources dir&gt; [caseNamePrefix]   (see record.sh)
 * Reads  cases/&lt;case&gt;/{case.json, seed.sql}, schema-jobs.sql, seed/base.sql, seed/config.json
 * Writes cases/&lt;case&gt;/expected.json
 */
public class OracleRunner {

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    public static void main(String[] args) throws Exception {
        Path base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        int n = 0;
        try (var dirs = Files.list(base.resolve("cases"))) {
            for (Path dir : dirs.sorted().toList()) {
                if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix)) continue;
                System.out.println("== " + dir.getFileName());
                JsonObject expected = run(base, dir);
                Files.writeString(dir.resolve("expected.json"), Archivos.mascara(PRETTY.toJson(expected)) + "\n", StandardCharsets.UTF_8);
                n++;
            }
        }
        System.out.println("cases recorded: " + n);
    }

    static JsonObject run(Path recursos, Path dir) throws Exception {
        JsonObject caso0 = JsonParser.parseString(Files.readString(dir.resolve("case.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        List<String> subdirs = new ArrayList<>();
        if (caso0.has("dirs")) caso0.getAsJsonArray("dirs").forEach(e -> subdirs.add(e.getAsString()));
        Path rutaBase = Archivos.nuevaRaiz(subdirs);
        JsonObject caso = JsonParser.parseString(Archivos.sustituir(caso0.toString(), rutaBase)).getAsJsonObject();
        Path semillaF = dir.resolve("seed.sql");
        String semilla = Files.exists(semillaF) ? Archivos.sustituir(Files.readString(semillaF, StandardCharsets.UTF_8), rutaBase) : "";

        OracleDb db = new OracleDb(recursos, semilla);
        db.conexionJob.setAutoCommit(false);
        Utils.CONFIG.clear();
        JsonObject cfg = JsonParser.parseString(Files.readString(recursos.resolve("seed/config.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        if (caso.has("config")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) cfg.remove(e.getKey()); else cfg.add(e.getKey(), e.getValue());
            }
        }
        for (Map.Entry<String, JsonElement> e : cfg.entrySet()) Utils.CONFIG.put(e.getKey(), Archivos.sustituir(e.getValue().getAsString(), rutaBase));
        Utils.SECUENCIAS.clear();
        Utils.SECUENCIA_LLAMADAS.clear();
        if (caso.has("secuencias")) caso.getAsJsonObject("secuencias").entrySet().forEach(e -> Utils.SECUENCIAS.put(e.getKey(), e.getValue().getAsLong()));
        String reloj = caso.has("reloj") ? caso.get("reloj").getAsString() : null;
        Hooks.reset(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject(), reloj);
        H2Hana.reloj = Hooks.UTC;

        JsonObject expected = new JsonObject();
        expected.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy " + caso.get("job").getAsString()
                + " job on H2 MODE=LEGACY. Do not edit by hand; re-record.");
        String job = caso.get("job").getAsString();
        if (job.startsWith("Pauta")) ejecutarPauta(job, caso, db, rutaBase, expected);
        else ejecutarJob(job, caso, db, rutaBase, expected);

        JsonObject tablas = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), Dump.tabla(db.setup, t));
            }
        }
        expected.add("tablas", tablas);
        JsonObject f = Archivos.capturar(rutaBase);
        expected.add("archivos", f.get("archivos"));
        expected.add("directorios", f.get("directorios"));
        JsonArray colab = new JsonArray();
        for (JsonObject o : Hooks.LLAMADAS) colab.add(o);
        expected.add("colaboradores", colab);
        expected.add("secuencias", Secuencias.agregadas(Utils.SECUENCIA_LLAMADAS));
        db.setup.close();
        return expected;
    }

    static void ejecutarJob(String nombre, JsonObject caso, OracleDb db, Path rutaBase, JsonObject expected) throws Exception {
        BcsJob job = OracleJobs.crear(nombre);
        Map<String, String> params = new HashMap<>();
        if (caso.has("parametros")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("parametros").entrySet()) {
                if (!e.getValue().isJsonNull()) params.put(e.getKey(), e.getValue().getAsString());
            }
        }
        long id = caso.has("idPrograma") ? caso.get("idPrograma").getAsLong() : 900L;
        JsonArray estados = new JsonArray();
        job.setJobListener(new BcsJobListener() {
            public void status(long jobId, boolean isError, String msg) {
                JsonObject o = new JsonObject();
                o.addProperty("error", isError);
                o.addProperty("mensaje", Canon.mensaje(msg, rutaBase));
                estados.add(o);
            }

            public void updateDaemon(long jobId) {
            }
        });
        javax.sql.DataSource ds = (javax.sql.DataSource) java.lang.reflect.Proxy.newProxyInstance(OracleRunner.class.getClassLoader(),
                new Class<?>[]{javax.sql.DataSource.class}, (p, m, a) -> m.getName().equals("getConnection") ? db.conexionJob : null);
        job.setParameters(id, params, 0L, false, ds, "**");
        job.run();
        Throwable t = ((OracleJobs.Capturado) job).error();
        // BcsJob.run() rolls back only on `catch (Exception)`: a java.lang.Error (the legacy ClientesPorPagarYCobrar job throws one) skips it and
        // the `finally` just closes the connection, so the DRIVER rolls the open transaction back. The modern runner rolls back explicitly:
        // the implicit rollback is recorded as the same event (assumption CIJ-A6 of the README).
        if (t != null && !(t instanceof Exception) && (db.eventos.isEmpty() || !db.eventos.get(db.eventos.size() - 1).equals("rollback"))) db.eventos.add("rollback");
        List<String> tx = new ArrayList<>(db.eventos);
        expected.addProperty("desenlace", t != null ? "ERROR" : tx.isEmpty() || tx.get(tx.size() - 1).equals("rollback") ? "ROLLBACK" : "COMMIT");
        if (t != null) {
            JsonObject ex = new JsonObject();
            ex.addProperty("tipo", t.getClass().getSimpleName());
            ex.addProperty("mensaje", Canon.mensajeDe(t, rutaBase));
            expected.add("excepcion", ex);
        }
        expected.add("estados", estados);
        expected.add("transaccion", PRETTY.toJsonTree(tx));
    }

    /** A PAUTA step (BcsProcesoPauta): called directly with the job connection and the parameter map; nothing commits it (the PAUTA runner does). */
    static void ejecutarPauta(String nombre, JsonObject caso, OracleDb db, Path rutaBase, JsonObject expected) throws Exception {
        cl.bcs.plataforma.jobs.BcsProcesoPauta pauta = OracleJobs.crearPauta(nombre);
        Map<String, Object> params = new HashMap<>();
        if (caso.has("parametros")) {
            for (Map.Entry<String, JsonElement> e : caso.getAsJsonObject("parametros").entrySet()) {
                JsonElement v = e.getValue();
                if (v.isJsonNull()) continue;
                params.put(e.getKey(), v.getAsJsonPrimitive().isNumber() ? (Object) v.getAsLong() : v.getAsString());
            }
        }
        Throwable t = null;
        cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta salida = null;
        try {
            salida = pauta.transaction(db.conexionJob, params);
        } catch (Throwable x) {
            t = x;
        }
        if (t != null) {
            JsonObject ex = new JsonObject();
            ex.addProperty("tipo", t.getClass().getSimpleName());
            ex.addProperty("mensaje", Canon.mensajeDe(t, rutaBase));
            expected.add("excepcion", ex);
        } else {
            JsonObject so = new JsonObject();
            so.addProperty("P_OUT_MENSAJE", salida.P_OUT_MENSAJE);
            so.addProperty("P_OUT_ERRORESCONTADOR", salida.P_OUT_ERRORESCONTADOR);
            so.add("P_OUT_MENSAJES", PRETTY.toJsonTree(salida.P_OUT_MENSAJES));
            expected.add("salida", so);
        }
    }
}
