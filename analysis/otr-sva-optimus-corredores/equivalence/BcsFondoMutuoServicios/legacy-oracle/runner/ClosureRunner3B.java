import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.oracle.Doubles;
import cl.bcs.oracle.SessionConnection;

/**
 * Step 3B golden recorder: calls ONE unmodified legacy closure class (cl.bcs.fondomutuo.&lt;servicio&gt;.call(Connection, JsonObject), as CallServlet
 * does) per case of cases3b/ and records the golden expected.json. Same H2 schema/seed/session emulation/doubles as OracleRunner (whose static
 * helpers are reused). The collaborators of step 3A (CalcularFechaSolicitudAporteRescate, CalcularMontoNumeroCuota) are registered DOUBLES here:
 * the case scripts their answers ("colaboradores") and the golden records the exact request the legacy class built for them.
 * Usage: java ClosureRunner3B &lt;baseDir&gt; [casePrefix]; -Doracle.out=&lt;dir&gt; dumps &lt;case&gt;.json instead of goldens (legacy only).
 */
public class ClosureRunner3B {
    static final String CLOCK = "2026-03-02 14:30:00";
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        Path base = Path.of(args[0]);
        OracleRunner.base = base;
        String prefix = args.length > 1 ? args[1] : "";
        String outDir = System.getProperty("oracle.out");
        List<Path> dirs;
        try (Stream<Path> s = Files.list(base.resolve("cases3b"))) { dirs = s.sorted().collect(Collectors.toList()); }
        int n = 0;
        for (Path dir : dirs) {
            if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix) || !Files.exists(dir.resolve("case.json"))) continue;
            String text = OracleRunner.PRETTY.toJson(run(dir)) + "\n";
            Path target = outDir != null ? Path.of(outDir).resolve(dir.getFileName() + ".json") : dir.resolve("expected.json");
            Files.createDirectories(target.getParent());
            Files.write(target, text.getBytes(StandardCharsets.UTF_8));
            System.out.println("== " + dir.getFileName());
            n++;
        }
        System.out.println("cases executed: " + n);
        if (n == 0) System.exit(2);
    }

    static JsonObject run(Path dir) throws Exception {
        OracleRunner.resetStatics();
        // more JVM-wide caches of the legacy: the parameter map of ConfiguracionSistema is only ever ADDED to (a parameter deleted by a case would
        // still be read from an earlier case) and MonedaFormato keeps the first format table it read: one case = one request in a fresh JVM
        cl.bcs.plataforma.pla.utils.ConfiguracionSistema.limpiarParametros();
        java.lang.reflect.Field fmt = Class.forName("cl.bcs.plataforma.pla.utils.MonedaFormato").getDeclaredField("formatos");
        fmt.setAccessible(true);
        fmt.set(null, null);
        JsonObject caso = OracleRunner.read(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:cl3b" + (++dbCounter) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        Connection raw = DriverManager.getConnection(url, "sa", "");
        for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) {
            OracleRunner.runScript(raw, OracleRunner.base.resolve(f));
        }
        if (Files.exists(dir.resolve("seed.sql"))) OracleRunner.runScript(raw, dir.resolve("seed.sql"));
        raw.commit();
        raw.setAutoCommit(false);
        String clock = caso.has("reloj") ? caso.get("reloj").getAsString() : CLOCK;
        Connection con = SessionConnection.of(raw, url, clock).connection();

        JsonObject doubles = OracleRunner.read(OracleRunner.base.resolve("seed/doubles-base.json"));
        if (caso.has("colaboradores")) {
            for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
        }
        Doubles.reset(doubles);

        String servicio = caso.get("servicio").getAsString();
        JsonObject res = new JsonObject();
        res.addProperty("_provenance", "Recorded by legacy-oracle/runner/ClosureRunner3B.java executing the unmodified legacy cl.bcs.fondomutuo." + servicio
                + ".call (real cl.bcs.utils.SQL/JavaCall) on H2 MODE=LEGACY, JVM zone UTC, fixed clock " + clock
                + ", collaborators outside the class answered by registered doubles. Do not edit by hand; re-record.");
        String decision;
        try {
            Object svc = Class.forName("cl.bcs.fondomutuo." + servicio).getDeclaredConstructor().newInstance();
            JsonObject out = ((cl.bcs.utils.JavaCall) svc).call(con, caso.getAsJsonObject("entrada").deepCopy());
            res.add("salida", out);
            res.addProperty("salidaCruda", out.toString());
            decision = "OK";
            raw.commit();
        } catch (Throwable t) {
            raw.rollback();
            decision = "EXCEPCION";
            String msg = String.valueOf(t.getMessage());
            if (t instanceof java.sql.SQLException && msg.contains(" Query: ")) msg = msg.substring(msg.indexOf(" Query: "));
            res.addProperty("excepcion", t.getClass().getSimpleName() + ": " + msg);
        }
        res.addProperty("resultado", decision);
        JsonObject tablas = new JsonObject();
        if (caso.has("tablas")) {
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), OracleRunner.dump(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
            }
        }
        res.add("tablas", tablas);
        res.add("colaboradores", Doubles.calls());
        res.add("secuenciasSolicitadas", Doubles.secuencias());
        raw.close();
        return res;
    }
}
