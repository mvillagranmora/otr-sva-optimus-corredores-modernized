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
 * Golden recorder / dumper for the FondoMutuo solicitud classes (ManejadorSolicitudAporteRescate, CrearSolicitudAporteRescateNormal).
 * Java 11 syntax on purpose (the legacy builds with JDK 11).
 *
 * Usage: java OracleRunner &lt;baseDir&gt; [caseNamePrefix]      baseDir = equivalence/BcsFondoMutuoServicios
 * System properties: oracle.subject ("legacy" default, or FQCN of a cl.bcs.oracle.Subject) | oracle.out (dir for &lt;case&gt;.json dumps; unset =
 * goldens cases/&lt;case&gt;/expected.json, legacy only) | oracle.mutant (MutantSubject).
 * Per case: fresh in-memory H2 (MODE=LEGACY), schema-types.sql + schema-fmm.sql + schema-session.sql + seed/base-fmm.sql + case seed.sql, autocommit OFF,
 * the subject gets the SessionConnection (HANA emulation at the JDBC boundary: AUTONOMOUS lock procedures, #local tables, dialect), registered
 * doubles answer the collaborators outside the closure and record every call. Decision of CallServlet.java:441-456,495: P_OUT_ERRORESCONTADOR
 * missing or > 0 => rollback; 0 => commit; an exception => rollback (HTTP 500). Nothing here touches HANA, a service or a credential.
 */
public class OracleRunner {
    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static Path base;
    static int dbCounter = 0;
    static final String CLOCK = "2026-03-02 14:30:00";

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        String subjectName = System.getProperty("oracle.subject", "legacy");
        Subject subject = subjectName.equals("legacy") ? new LegacySubject() : (Subject) Class.forName(subjectName).getDeclaredConstructor().newInstance();
        String outDir = System.getProperty("oracle.out");
        if (outDir == null && !subjectName.equals("legacy")) {
            throw new IllegalArgumentException("a subject other than 'legacy' needs -Doracle.out=<dir>: goldens are only recorded from the legacy");
        }
        List<Path> dirs;
        // step 5: -Doracle.cases=<dir> reads the case directories from elsewhere (fresh inputs); the schema/seed still come from base
        Path casesRoot = System.getProperty("oracle.cases") != null ? Path.of(System.getProperty("oracle.cases")) : base.resolve("cases");
        try (Stream<Path> s = Files.list(casesRoot)) { dirs = s.sorted().collect(Collectors.toList()); }
        int n = 0;
        int stableRuns = 0;
        int unstableHard = 0;
        List<String> unstable = new ArrayList<>();
        for (Path dir : dirs) {
            if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix) || !Files.exists(dir.resolve("case.json"))) continue;
            // F2 W1B: a layer-2 case ("tesoreriaReal": Tesoreria's CrearIngresoEgreso REAL) is only run by record_w1b.sh (-Doracle.tesoreria=true), and
            // record_w1b.sh only runs layer-2 cases: neither script can re-record the goldens of the other
            JsonObject cab = read(dir.resolve("case.json"));
            if ((cab.has("tesoreriaReal") && cab.get("tesoreriaReal").getAsBoolean()) != Boolean.getBoolean("oracle.tesoreria")) continue;
            // F2 W1 front 2: a case with "rescateReinversionReal" needs CrearSolicitudRescateReinversion REAL (record_rre.sh, -Doracle.rre=true); record.sh keeps
            // it as a registered double and never runs those cases, and record_rre.sh only runs them
            if ((cab.has("rescateReinversionReal") && cab.get("rescateReinversionReal").getAsBoolean()) != Boolean.getBoolean("oracle.rre")) continue;
            // F3 W1 front 1: the same for "consultarFechaReal" (ConsultarFechaSolicitudAporteRescate, record_fsa.sh, -Doracle.fsa=true): the other scripts never run those
            // cases and record_fsa.sh only runs them
            if ((cab.has("consultarFechaReal") && cab.get("consultarFechaReal").getAsBoolean()) != Boolean.getBoolean("oracle.fsa")) continue;
            // F3: -Doracle.repeat=N runs every case N times in fresh sessions (parallelStream / toMap in the path) and compares the dumps; an unstable case is
            // reported with the first differing section and NO golden is written for it
            int reps = Integer.getInteger("oracle.repeat", 1);
            JsonObject first = run(dir, subject, subjectName);
            String text = PRETTY.toJson(first) + "\n";
            if (reps > 1) {
                int differ = 0, messageOnly = 0;
                String why = "";
                for (int r = 1; r < reps; r++) {
                    JsonObject again = run(dir, subject, subjectName);
                    if (!PRETTY.toJson(again).equals(PRETTY.toJson(first))) {
                        // the equivalence tests compare an exception by CLASS (the message only for a plain java.lang.Exception): an instability limited to the
                        // message text of another class (e.g. Collectors.toMap lists the two merged values in a parallel-dependent order) is reported
                        // but does not make the case unusable
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
            rep.add("cases executed: " + n + ", repeats per case: " + Integer.getInteger("oracle.repeat", 1) + ", unstable (hard + message-only): " + unstable.size() + ", hard: " + unstableHard);
            rep.addAll(unstable);
            Files.write(Path.of(System.getProperty("oracle.report")), (String.join("\n", rep) + "\n").getBytes(StandardCharsets.UTF_8));
        }
        if (n == 0) System.exit(2);
        if (unstableHard > 0) System.exit(4);
    }

    /** First top-level section whose rendering differs between two dumps of the same case (the reason of an UNSTABLE verdict). */
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

    static JsonObject run(Path dir, Subject subject, String subjectName) throws Exception {
        resetStatics();
        JsonObject caso = read(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:fmm" + (++dbCounter) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        Connection raw = DriverManager.getConnection(url, "sa", "");
        for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) runScript(raw, base.resolve(f));
        // F2 W1B: extra schema/seed files named by the case (Tesoreria objects for layer 2), loaded after the base and before the case seed
        if (caso.has("esquemasExtra")) for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) runScript(raw, base.resolve(e.getAsString()));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, dir.resolve("seed.sql"));
        raw.commit();
        raw.setAutoCommit(false);
        String clock = caso.has("reloj") ? caso.get("reloj").getAsString() : CLOCK;
        SessionConnection session = SessionConnection.of(raw, url, clock);
        Connection con = session.connection();

        JsonObject doubles = read(base.resolve("seed/doubles-base.json"));
        if (caso.has("colaboradores")) for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
        Doubles.reset(doubles);

        JsonObject res = new JsonObject();
        res.addProperty("_provenance", subjectName.equals("legacy")
                ? "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy cl.bcs.fondomutuo." + caso.get("servicio").getAsString()
                + ".call (real cl.bcs.utils.SQL/JavaCall and the real closure) on H2 MODE=LEGACY, JVM zone UTC, fixed clock " + clock
                + ", collaborators outside the closure answered by registered doubles. Do not edit by hand; re-record."
                : "Dump of subject " + subjectName + " (not a golden)");
        String decision;
        try {
            JsonObject out = subject.call(caso.get("servicio").getAsString(), con, caso.getAsJsonObject("entrada").deepCopy());
            res.add("salida", out);
            res.addProperty("salidaCruda", out.toString());
            JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
            boolean commit = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
            decision = commit ? "COMMIT" : "ROLLBACK";
            if (commit) raw.commit(); else raw.rollback();
        } catch (Throwable t) {
            raw.rollback();
            decision = "ROLLBACK_HTTP500";
            String msg = String.valueOf(t.getMessage());
            if (t instanceof java.sql.SQLException && msg.contains(" Query: ")) msg = msg.substring(msg.indexOf(" Query: "));
            res.addProperty("excepcion", t.getClass().getSimpleName() + ": " + msg);
        }
        res.addProperty("decisionServlet", decision);
        JsonObject tablas = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), dump(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
            }
        }
        res.add("tablas", tablas);
        res.add("colaboradores", Doubles.calls());
        res.add("secuenciasSolicitadas", Doubles.secuencias());
        JsonObject ses = new JsonObject();
        JsonArray locales = new JsonArray();
        for (String s : session.localTablesCreated()) locales.add(s);
        ses.add("tablasLocalesSinDrop", locales);
        JsonArray au = new JsonArray();
        for (String s : session.autonomousLog()) au.add(s);
        ses.add("commitsAutonomos", au);
        res.add("sesion", ses);
        raw.close();
        return res;
    }

    /** The legacy keeps static caches (Diccionario, Fecha offset): every case starts from the state of a fresh JVM (one case = one request). */
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
        // step 4A: the parameter cache (loaded once per module for the life of the JVM) and the currency-format table must not leak between cases
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
            if (!line.strip().startsWith("--")) clean.add(line.replaceAll(";\\s+--[^']*$", ";"));   // "...; -- REAL (..)" tags of the generated schema
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
