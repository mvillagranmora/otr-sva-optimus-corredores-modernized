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
import java.util.Collections;
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

import cl.bcs.oracle.LegacySubject;
import cl.bcs.oracle.Subject;

/**
 * Golden recorder / dumper for cl.bcs.plataforma.cre.ConsultarEstadoModulo (JavaCall 600017). Java 11 syntax on purpose (the legacy builds with JDK 11).
 *
 * Usage: java OracleRunner &lt;baseDir&gt; [caseNamePrefix]
 *   baseDir = equivalence/BcsPlataformaServicios (schema-plataforma-esm.sql, seed/base-esm.sql, cases/&lt;case&gt;/{case.json,seed.sql,expected.json})
 * System properties:
 *   oracle.subject  "legacy" (default) or the FQCN of a cl.bcs.oracle.Subject (the new implementation, or MutantSubject)
 *   oracle.out      directory for &lt;case&gt;.json dumps; when unset (and subject=legacy) the dump is written to cases/&lt;case&gt;/expected.json
 *   oracle.mutant   mutant name for MutantSubject
 * Per case: fresh in-memory H2 (MODE=LEGACY), schema + base seed + case seed, autocommit OFF (CallServlet works on a non autocommit
 * connection and commits only when P_OUT_ERRORESCONTADOR is present and 0, otherwise rolls back: CallServlet.java:441-456, 495).
 * Same dump format for every subject, so compare.py can compare legacy vs new. Nothing here touches HANA, a service or a credential.
 */
public class OracleRunner {

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static Path base;
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        String subjectName = System.getProperty("oracle.subject", "legacy");
        Subject subject = subjectName.equals("legacy") ? new LegacySubject()
                : (Subject) Class.forName(subjectName).getDeclaredConstructor().newInstance();
        String outDir = System.getProperty("oracle.out");
        if (outDir == null && !subjectName.equals("legacy")) {
            throw new IllegalArgumentException("a subject other than 'legacy' needs -Doracle.out=<dir>: goldens are only recorded from the legacy");
        }
        List<Path> dirs;
        try (Stream<Path> s = Files.list(base.resolve("cases"))) {
            dirs = s.sorted().collect(Collectors.toList());
        }
        int n = 0;
        for (Path dir : dirs) {
            if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix)) continue;
            JsonObject result = run(dir, subject, subjectName);
            String text = PRETTY.toJson(result) + "\n";
            if (outDir != null) {
                Files.createDirectories(Path.of(outDir));
                Files.write(Path.of(outDir).resolve(dir.getFileName() + ".json"), text.getBytes(StandardCharsets.UTF_8));
            } else {
                Files.write(dir.resolve("expected.json"), text.getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("== " + dir.getFileName());
            n++;
        }
        System.out.println("cases executed (" + subjectName + "): " + n);
        if (n == 0) System.exit(2);
    }

    static JsonObject run(Path dir, Subject subject, String subjectName) throws Exception {
        JsonObject caso = JsonParser.parseString(new String(Files.readAllBytes(dir.resolve("case.json")), StandardCharsets.UTF_8)).getAsJsonObject();
        Connection raw = DriverManager.getConnection("jdbc:h2:mem:esm" + (++dbCounter) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        runScript(raw, base.resolve("schema-plataforma-esm.sql"));
        runScript(raw, base.resolve("seed/base-esm.sql"));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, dir.resolve("seed.sql"));
        raw.commit();
        raw.setAutoCommit(false);

        JsonObject res = new JsonObject();
        res.addProperty("_provenance", subjectName.equals("legacy")
                ? "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy cl.bcs.plataforma.cre.ConsultarEstadoModulo.call"
                + " (real cl.bcs.utils.SQL/JavaCall/NamedParameterStatement) on H2 MODE=LEGACY, JVM zone UTC. Do not edit by hand; re-record."
                : "Dump of subject " + subjectName + " (not a golden)");
        String decision;
        try {
            JsonObject out = subject.call(raw, caso.getAsJsonObject("entrada").deepCopy());
            res.add("salida", out);
            res.addProperty("salidaCruda", out.toString());
            // CallServlet: error counter absent or > 0 -> rollback (and HTTP 200 with the output); 0 -> commit
            JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
            boolean commit = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
            decision = commit ? "COMMIT" : "ROLLBACK";
            if (commit) raw.commit(); else raw.rollback();
        } catch (Throwable t) {
            raw.rollback();
            decision = "ROLLBACK_HTTP500";
            String msg = String.valueOf(t.getMessage());
            // the driver part of an SQLException text is H2/HANA specific: keep the legacy-owned part (" Query: ... Parameters: ...")
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
        res.add("colaboradores", new JsonArray());
        res.add("secuenciasSolicitadas", new JsonArray());
        raw.close();
        return res;
    }

    /** Same row rendering as the earlier modules (Dump.java): numbers normalised, dates as ISO text, NULL as JSON null. */
    static JsonArray dump(Connection conn, String table, String orderBy) throws Exception {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM \"" + table + "\" ORDER BY " + orderBy)) {
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
            if (!line.strip().startsWith("--")) clean.add(line);
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }

    @SuppressWarnings("unused")
    static List<String> unused() {
        return Collections.emptyList();
    }
}
