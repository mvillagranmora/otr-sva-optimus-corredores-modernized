import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.oracle.Doubles;
import cl.bcs.oracle.LegacySubject;
import cl.bcs.oracle.SessionConnection;
import cl.bcs.oracle.Subject;

/**
 * F3 W3 T1 (CertificarSolicitudAporteRescate: F-A header/validation and F-B amounts): OWN COPY of OracleRunnerCvc (neither OracleRunner, OracleRunnerLect nor OracleRunnerCvc is
 * touched). Same session emulation, doubles runtime, servlet decision (CallServlet.java:441-456,495: counter missing or > 0 => rollback; 0 => commit; an exception =>
 * rollback, HTTP 500) and N-runs stability check; what a WRITING class needs on top:
 * <ul>
 * <li>three dumps of the tables named by the case: {@code tablasIniciales} (after the seeds), {@code tablasTrasLaLlamada} (what the class left in the session BEFORE the host
 *     decides: the writes of the OK rows of a mixed batch are here), {@code tablas} (after the commit / rollback decision);</li>
 * <li>{@code entradasPrevias}: requests executed before the one under test in the SAME H2 session, each with its own decision (a repeated load); their outputs go to
 *     {@code previas};</li>
 * <li>{@link ConexionCer}: the inline NEXTVAL of the FMM.LogCertificacionAporteRescate insert is recorded as a sequence request ({@code secuenciasSolicitadas});</li>
 * <li>the H2 database of each run is shut down when the run ends (2000+ runs of 20 repetitions would otherwise stay in memory).</li>
 * </ul>
 * Usage: java OracleRunnerCer &lt;baseDir&gt; [caseNamePrefix]   baseDir = equivalence/BcsFondoMutuoServicios. System properties: oracle.subject ("legacy" default or the FQCN of a
 * cl.bcs.oracle.Subject), oracle.out (dir of &lt;case&gt;.json dumps; unset = goldens cases-cer/&lt;case&gt;/expected.json, legacy only), oracle.cases (case root, default
 * &lt;baseDir&gt;/cases-cer), oracle.repeat (N runs per case, default 1), oracle.report. Java 11 syntax on purpose. Nothing here touches HANA, a service or a credential.
 */
public class OracleRunnerCer {
    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static Path base;
    static int dbCounter = 0;
    static final String CLOCK = "2026-03-02 14:30:00";
    static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        String subjectName = System.getProperty("oracle.subject", "legacy");
        Subject subject = subjectName.equals("legacy") ? new LegacySubject() : (Subject) Class.forName(subjectName).getDeclaredConstructor().newInstance();
        String outDir = System.getProperty("oracle.out");
        if (outDir == null && !subjectName.equals("legacy")) {
            throw new IllegalArgumentException("a subject other than 'legacy' needs -Doracle.out=<dir>: goldens are only recorded from the legacy");
        }
        Path casesRoot = System.getProperty("oracle.cases") != null ? Path.of(System.getProperty("oracle.cases")) : base.resolve("cases-cer");
        List<Path> dirs;
        try (Stream<Path> s = Files.list(casesRoot)) { dirs = s.sorted().collect(Collectors.toList()); }
        int n = 0;
        int stableRuns = 0;
        int unstableHard = 0;
        List<String> unstable = new ArrayList<>();
        for (Path dir : dirs) {
            if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix) || !Files.exists(dir.resolve("case.json"))) continue;
            int reps = Integer.getInteger("oracle.repeat", 1);
            JsonObject first = run(dir, subject, subjectName);
            String text = PRETTY.toJson(first) + "\n";
            if (reps > 1) {
                int differ = 0, messageOnly = 0;
                String why = "";
                for (int r = 1; r < reps; r++) {
                    JsonObject again = run(dir, subject, subjectName);
                    if (!PRETTY.toJson(again).equals(PRETTY.toJson(first))) {
                        if (PRETTY.toJson(comparable(again)).equals(PRETTY.toJson(comparable(first)))) { messageOnly++; continue; }
                        differ++;
                        if (why.isEmpty()) why = firstDifference(first, again);
                    }
                }
                if (messageOnly > 0) {
                    String line = dir.getFileName() + " MESSAGE-ONLY-UNSTABLE: " + messageOnly + " of " + (reps - 1) + " re-runs differ only in the text of the exception message (class "
                            + first.get("excepcion").getAsString().split(":")[0] + " is stable); golden recorded, the tests compare the class";
                    System.out.println("~~ " + line);
                    unstable.add(line);
                }
                if (differ > 0) {
                    String line = dir.getFileName() + " UNSTABLE: " + differ + " of " + (reps - 1) + " re-runs differ from run 1 (" + why + ")";
                    System.out.println("!! " + line);
                    unstable.add(line);
                    unstableHard++;
                    if (outDir == null) Files.deleteIfExists(dir.resolve("expected.json"));   // never leave a stale golden of an unstable case
                    continue;
                }
                stableRuns += reps;
            }
            if (outDir != null) {
                Files.createDirectories(Path.of(outDir));
                Files.write(Path.of(outDir).resolve(dir.getFileName() + ".json"), text.getBytes(StandardCharsets.UTF_8));
            } else {
                Files.write(dir.resolve("expected.json"), text.getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("== " + dir.getFileName());
            n++;
        }
        System.out.println("cases executed (" + subjectName + "): " + n + (stableRuns > 0 ? " (stable runs executed: " + stableRuns + ")" : ""));
        if (System.getProperty("oracle.report") != null) {
            List<String> rep = new ArrayList<>();
            rep.add("cases executed: " + n + ", repeats per case: " + Integer.getInteger("oracle.repeat", 1) + ", stable runs executed: " + stableRuns + ", unstable (hard + message-only): "
                    + unstable.size() + ", hard: " + unstableHard);
            rep.addAll(unstable);
            Files.write(Path.of(System.getProperty("oracle.report")), (String.join("\n", rep) + "\n").getBytes(StandardCharsets.UTF_8));
        }
        if (n == 0) System.exit(2);
        if (unstableHard > 0) System.exit(4);
    }

    /** The dump as the equivalence tests compare it: an exception reduced to its class unless it is a plain java.lang.Exception (then the message counts). */
    static JsonObject comparable(JsonObject o) {
        JsonObject c = o.deepCopy();
        if (c.has("excepcion")) {
            String e = c.get("excepcion").getAsString();
            int i = e.indexOf(':');
            if (i > 0 && !e.substring(0, i).equals("Exception")) c.addProperty("excepcion", e.substring(0, i));
        }
        return c;
    }

    static String firstDifference(JsonObject a, JsonObject b) {
        for (String k : a.keySet()) {
            if (!PRETTY.toJson(a.get(k)).equals(PRETTY.toJson(b.get(k)))) return "section " + k + " differs";
        }
        return "keys differ";
    }

    static JsonObject read(Path p) throws Exception {
        return JsonParser.parseString(new String(Files.readAllBytes(p), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    static String clase(Throwable t) {
        return t.getClass().getSimpleName();
    }

    static String mensaje(Throwable t) {
        String msg = String.valueOf(t.getMessage());
        if (t instanceof java.sql.SQLException && msg.contains(" Query: ")) msg = msg.substring(msg.indexOf(" Query: "));
        return msg;
    }

    static JsonObject tablas(Connection raw, JsonObject caso) throws Exception {
        JsonObject tablas = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), dump(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
            }
        }
        return tablas;
    }

    static JsonObject run(Path dir, Subject subject, String subjectName) throws Exception {
        resetStatics();
        JsonObject caso = read(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:cer" + (++dbCounter) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        Connection raw = DriverManager.getConnection(url, "sa", "");
        for (String f : ESQUEMA) runScript(raw, base.resolve(f));
        if (caso.has("esquemasExtra")) for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) runScript(raw, base.resolve(e.getAsString()));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, dir.resolve("seed.sql"));
        raw.commit();
        raw.setAutoCommit(false);
        String clock = caso.has("reloj") ? caso.get("reloj").getAsString() : CLOCK;
        SessionConnection session = SessionConnection.of(raw, url, clock);
        Connection con = ConexionCer.envolver(session.connection());

        JsonObject doubles = read(base.resolve("seed/doubles-base.json"));
        JsonObject benignos = read(base.resolve("seed/doubles-cer.json"));
        for (String k : benignos.keySet()) doubles.add(k, benignos.get(k));
        if (caso.has("colaboradores")) for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
        Doubles.reset(doubles);

        JsonObject res = new JsonObject();
        res.addProperty("_provenance", subjectName.equals("legacy")
                ? "Recorded by legacy-oracle/runner-cer/OracleRunnerCer.java executing the unmodified legacy cl.bcs.fondomutuo." + caso.get("servicio").getAsString()
                + ".call (real cl.bcs.utils.SQL/JavaCall, RevisarRequisitosBasicos, CalcularMontoNumeroCuota, CalcularComisiones, AgregarLogCertificacionAporteRescate and the real closure) on H2 MODE=LEGACY, JVM zone UTC, fixed clock " + clock
                + ", every collaborator outside the module = registered double generated from its real source (seed/doubles-base.json + seed/doubles-cer.json + the case colaboradores; an \"error\" answer = the STOP of a parada case). Do not edit by hand; re-record."
                : "Dump of subject " + subjectName + " (not a golden)");
        JsonObject inicial = tablas(raw, caso);

        JsonArray previas = new JsonArray();
        if (caso.has("entradasPrevias")) {
            for (JsonElement ep : caso.getAsJsonArray("entradasPrevias")) {
                JsonObject p = new JsonObject();
                try {
                    JsonObject out = subject.call(caso.get("servicio").getAsString(), con, ep.getAsJsonObject().deepCopy());
                    p.addProperty("salidaCruda", out.toString());
                    boolean commit = decideCommit(out);
                    p.addProperty("decisionServlet", commit ? "COMMIT" : "ROLLBACK");
                    if (commit) raw.commit(); else raw.rollback();
                } catch (Throwable t) {
                    raw.rollback();
                    p.addProperty("decisionServlet", "ROLLBACK_HTTP500");
                    p.addProperty("excepcion", clase(t) + ": " + mensaje(t));
                }
                previas.add(p);
            }
        }

        String decision;
        JsonObject trasLlamada;
        try {
            JsonObject out = subject.call(caso.get("servicio").getAsString(), con, caso.getAsJsonObject("entrada").deepCopy());
            res.add("salida", out);
            res.addProperty("salidaCruda", out.toString());
            trasLlamada = tablas(raw, caso);
            boolean commit = decideCommit(out);
            decision = commit ? "COMMIT" : "ROLLBACK";
            if (commit) raw.commit(); else raw.rollback();
        } catch (Throwable t) {
            trasLlamada = tablas(raw, caso);
            raw.rollback();
            decision = "ROLLBACK_HTTP500";
            res.addProperty("excepcion", clase(t) + ": " + mensaje(t));
        }
        res.addProperty("decisionServlet", decision);
        res.add("tablasIniciales", inicial);
        res.add("tablasTrasLaLlamada", trasLlamada);
        res.add("tablas", tablas(raw, caso));
        res.add("colaboradores", Doubles.calls());
        res.add("secuenciasSolicitadas", Doubles.secuencias());
        res.add("previas", previas);
        JsonObject ses = new JsonObject();
        JsonArray locales = new JsonArray();
        for (String s : session.localTablesCreated()) locales.add(s);
        ses.add("tablasLocalesSinDrop", locales);
        JsonArray au = new JsonArray();
        for (String s : session.autonomousLog()) au.add(s);
        ses.add("commitsAutonomos", au);
        res.add("sesion", ses);
        try (Statement st = raw.createStatement()) {
            st.execute("SHUTDOWN");
        } catch (Exception e) {
            // the database is gone anyway
        }
        return res;
    }

    /** CallServlet.java:441-456,495: COMMIT only when the response carries the counter and it is 0. */
    static boolean decideCommit(JsonObject out) {
        JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
        return cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
    }

    /** The legacy keeps static caches (Diccionario, Fecha offset, parameter cache, currency formats): every case starts from the state of a fresh JVM (one case = one request). */
    static void resetStatics() throws Exception {
        Class<?> d = Class.forName("cl.bcs.plataforma.pla.utils.Diccionario");
        java.lang.reflect.Field f = d.getDeclaredField("diccionariosCargados");
        f.setAccessible(true);
        ((java.util.List<?>) f.get(null)).clear();
        java.lang.reflect.Field r = d.getDeclaredField("registros");
        r.setAccessible(true);
        r.set(null, null);
        Class<?> fd = Class.forName("cl.bcs.plataforma.pla.utils.FechaDB");
        for (String n : new String[] { "ultTimeMillis", "cacheCurrentUTC" }) {
            java.lang.reflect.Field x = fd.getDeclaredField(n);
            x.setAccessible(true);
            x.set(null, n.equals("ultTimeMillis") ? (Object) Long.valueOf(0L) : null);
        }
        cl.bcs.utils.Fecha.setLocalOffSet(0);
        cl.bcs.plataforma.pla.utils.ConfiguracionSistema.limpiarParametros();
        java.lang.reflect.Field mf = Class.forName("cl.bcs.plataforma.pla.utils.MonedaFormato").getDeclaredField("formatos");
        mf.setAccessible(true);
        mf.set(null, null);
    }

    /** Same row rendering as the earlier modules: numbers normalised, dates as ISO text, NULL as JSON null. */
    static JsonArray dump(Connection conn, String table, String orderBy) throws Exception {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM \"" + table + "\" ORDER BY " + orderBy)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String col = md.getColumnLabel(i);
                    if (v == null) row.add(col, JsonNull.INSTANCE);
                    else if (v instanceof Number) row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(v.toString()).stripTrailingZeros().toPlainString())));
                    else if (v instanceof java.util.Date) row.addProperty(col, rs.getString(i));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    static void runScript(Connection conn, Path file) throws Exception {
        List<String> clean = new ArrayList<>();
        for (String line : new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\n")) {
            if (!line.strip().startsWith("--")) clean.add(line.replaceAll(";\\s+--[^']*$", ";"));
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
