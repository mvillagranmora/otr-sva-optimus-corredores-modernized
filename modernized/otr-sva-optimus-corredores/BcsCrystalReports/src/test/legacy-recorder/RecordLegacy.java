import cl.bcs.reports.equiv.EquivCase;
import cl.bcs.reports.equiv.H2Shim;
import cl.bcs.reports.equiv.Masks;
import cl.bcs.reports.equiv.XlsWindows;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Records the LEGACY goldens: args = equivDir moduleDir [caseNameFilter...]. Every case is rendered in two passes at
 * least 65 s apart (so minute-dependent output is exposed), the legacy engine must be self-deterministic modulo the
 * documented masks, and pass A is stored under &lt;case&gt;/legacy/&lt;format&gt;.&lt;ext&gt; with legacy/meta.json
 * (recording day, xls print date/time windows). Exit code 1 if anything failed (nothing is written for a failed case).
 */
public class RecordLegacy {

    /** Minimum ms between the two passes of a case (default 65 s so that minute-dependent output shows up). */
    static final long GAP = System.getenv("RECORD_GAP_MS") == null ? 65_000 : Long.parseLong(System.getenv("RECORD_GAP_MS"));

    public static void main(String[] a) throws Exception {
        Path equiv = Paths.get(a[0]).toAbsolutePath();
        Path module = Paths.get(a[1]).toAbsolutePath();
        List<String> filter = Arrays.asList(a).subList(2, a.length);
        FontHome.ensure(new File(module.toFile(), "target/legacy-recorder/java-home"));
        File work = new File(module.toFile(), "target/legacy-recorder/run");
        work.mkdirs();
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:legacyrec;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        H2Shim.init(ds);
        LegacyRenderer lr = new LegacyRenderer(ds);
        List<EquivCase> cases = new java.util.ArrayList<>();
        for (EquivCase c : EquivCase.all(equiv)) {
            if (filter.isEmpty() || filter.stream().anyMatch(c.name::contains)) {
                cases.add(c);
            }
        }
        LocalDate today = LocalDate.now();
        Map<String, Map<String, byte[]>> passA = new LinkedHashMap<>();
        Map<String, Long> startA = new LinkedHashMap<>();
        int failed = 0;
        for (EquivCase c : cases) {
            startA.put(c.name, System.currentTimeMillis());
            failed += pass(lr, c, module, passA);
        }
        for (EquivCase c : cases) {
            Map<String, byte[]> A = passA.get(c.name);
            if (A == null) {
                continue;
            }
            long wait = startA.get(c.name) + GAP - System.currentTimeMillis();
            if (wait > 0) {
                Thread.sleep(wait);
            }
            Map<String, Map<String, byte[]>> B = new LinkedHashMap<>();
            failed += pass(lr, c, module, B);
            if (B.get(c.name) == null) {
                continue;
            }
            failed += store(c, A, B.get(c.name), today);
        }
        System.out.println(failed == 0 ? "RECORD OK: " + cases.size() + " cases" : "RECORD FAILED: " + failed + " problems");
        System.exit(failed == 0 ? 0 : 1);
    }

    static int pass(LegacyRenderer lr, EquivCase c, Path module, Map<String, Map<String, byte[]>> into) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        try {
            lr.load(c, module);
            for (String f : c.formats) {
                out.put(f, lr.render(c, f));
                if (lr.lastStderr.contains("Exception")) {
                    System.out.println("WARN " + c.name + " " + f + " stderr: " + lr.lastStderr.split("\n")[0]);
                }
            }
            into.put(c.name, out);
            return 0;
        } catch (Throwable t) {
            System.out.println("ERROR " + c.name + ": " + t + "\n" + lr.lastStderr.split("\n")[0]);
            return 1;
        }
    }

    static int store(EquivCase c, Map<String, byte[]> a, Map<String, byte[]> b, LocalDate today) throws Exception {
        List<LocalDate> days = Collections.singletonList(today);
        JsonObject meta = new JsonObject();
        meta.addProperty("recordedOn", today.toString());
        meta.addProperty("javaVersion", System.getProperty("java.version"));
        JsonObject wins = new JsonObject();
        int bad = 0;
        for (String f : c.formats) {
            byte[] x = a.get(f), y = b.get(f);
            if ("excel".equals(f)) {
                List<long[]> w = XlsWindows.find(x, y, days);
                List<Integer> un = XlsWindows.unexplained(x, y, w);
                JsonArray arr = new JsonArray();
                w.forEach(r -> arr.add(r[0] + ":" + r[1]));
                wins.add(f, arr);
                if (!un.isEmpty()) {
                    System.out.println("NONDETERMINISTIC " + c.name + " excel at " + un);
                    bad++;
                }
            } else if (!Arrays.equals(Masks.apply(f, x, null, days), Masks.apply(f, y, null, days))) {
                System.out.println("NONDETERMINISTIC " + c.name + " " + f + " (lengths " + x.length + "/" + y.length + ")");
                Path nd = Paths.get("nondet");   // cwd = target/legacy-recorder/run: both passes kept for inspection
                Files.createDirectories(nd);
                Files.write(nd.resolve(c.name + "." + f + ".A"), x);
                Files.write(nd.resolve(c.name + "." + f + ".B"), y);
                bad++;
            }
        }
        if (bad > 0) {
            return bad;
        }
        meta.add("xlsWindows", wins);
        Path leg = c.dir.resolve("legacy");
        Files.createDirectories(leg);
        for (String f : c.formats) {
            Files.write(leg.resolve(f + "." + EquivCase.ext(f)), a.get(f));
        }
        Files.write(leg.resolve("meta.json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(meta).getBytes(StandardCharsets.UTF_8));
        System.out.println("recorded " + c.name + " " + c.formats);
        return 0;
    }
}
