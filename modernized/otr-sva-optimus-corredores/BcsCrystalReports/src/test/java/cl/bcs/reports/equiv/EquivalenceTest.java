package cl.bcs.reports.equiv;

import cl.bcs.reports.support.CrystalTestEnvironment;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * LEGACY goldens (recorded by src/test/legacy-recorder/record.sh from the unmodified legacy engine) versus the SIDECAR
 * engine, same template bytes (H2 table "BSCL.Report::Template"), same fake rows, 5 formats, after the documented masks
 * (Masks, XlsWindows). Any difference, render error or missing SDK FAILS (never skips). -Dequivalence.out=&lt;dir&gt;
 * additionally writes the canonical facet of both engines per case and format for compare.py.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:equiv;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
class EquivalenceTest {

    /** -Dequivalence.cases=<dir> points the suite at another set of cases (used to prove the test fails on a difference). */
    private static final Path EQUIV = Paths.get(System.getProperty("equivalence.cases", "src/test/resources/crystal/equiv"));

    @Autowired MockMvc mvc;
    @Autowired DataSource ds;

    @BeforeAll
    static void environment() {
        CrystalTestEnvironment.ensure();
    }

    @TestFactory
    Stream<DynamicTest> everyCaseEveryFormat() throws Exception {
        List<EquivCase> cases = EquivCase.all(EQUIV);
        assertFalse(cases.isEmpty(), "no equivalence cases under " + EQUIV);
        H2Shim.init(ds);
        SidecarRenderer renderer = new SidecarRenderer(mvc, ds);
        List<DynamicTest> tests = new ArrayList<>();
        for (EquivCase c : cases) {
            H2Shim.putTemplate(ds, c.name, Paths.get(c.template));
            tests.add(DynamicTest.dynamicTest(c.name + " [data guard]", () -> dataGuard(c)));
            for (String f : c.formats) {
                tests.add(DynamicTest.dynamicTest(c.name + " [" + f + "]", () -> check(renderer, c, f)));
            }
        }
        return tests.stream();
    }

    private void check(SidecarRenderer renderer, EquivCase c, String format) throws Exception {
        JsonObject meta = JsonParser.parseString(new String(Files.readAllBytes(c.dir.resolve("legacy/meta.json")),
                StandardCharsets.UTF_8)).getAsJsonObject();
        List<LocalDate> days = Arrays.asList(LocalDate.parse(meta.get("recordedOn").getAsString()), LocalDate.now());
        List<long[]> windows = new ArrayList<>();
        JsonArray w = meta.getAsJsonObject("xlsWindows").has(format)
                ? meta.getAsJsonObject("xlsWindows").getAsJsonArray(format) : new JsonArray();
        for (JsonElement e : w) {
            String[] p = e.getAsString().split(":");
            windows.add(new long[] {Long.parseLong(p[0]), Long.parseLong(p[1])});
        }
        byte[] legacy = Files.readAllBytes(c.dir.resolve("legacy/" + format + "." + EquivCase.ext(format)));
        byte[] sidecar = renderer.render(c, format);
        String fl = Facet.of(c.name, format, legacy, windows, days);
        String fn = Facet.of(c.name, format, sidecar, windows, days);
        System.out.println("EQUIV " + c.name + " [" + format + "] mode=" + renderer.lastMode);
        dump("legacy", c, format, fl);
        dump("new", c, format, fn);
        dumpRaw(c, format, sidecar);
        assertFalse(renderer.lastStderr.contains("Exception"), "swallowed engine error: " + renderer.lastStderr);
        if ("excel".equals(format)) {
            for (long[] x : windows) {
                assertTrue(XlsWindows.temporal(XlsWindows.doubleAt(legacy, x[0])), "golden window is not a date/time");
                assertTrue(XlsWindows.temporal(XlsWindows.doubleAt(sidecar, x[0])), "sidecar window is not a date/time at " + x[0]);
            }
        }
        assertEquals(fl, fn, c.name + " " + format + " facet differs (" + renderer.lastMode + ")");
        byte[] a = Masks.apply(format, legacy, windows, days);
        byte[] b = Masks.apply(format, sidecar, windows, days);
        if (!Arrays.equals(a, b)) {
            int i = 0;
            while (i < Math.min(a.length, b.length) && a[i] == b[i]) {
                i++;
            }
            fail(c.name + " " + format + ": masked bytes differ, first difference at " + i + " (legacy " + a.length
                    + " B, sidecar " + b.length + " B)");
        }
    }

    /** The fake data must not collide with the masks (print date/time patterns, today and the recording day). */
    private void dataGuard(EquivCase c) {
        List<String> values = new ArrayList<>();
        collect(c.rows, values);
        c.subRows.values().forEach(r -> collect(r, values));
        List<LocalDate> days = Arrays.asList(LocalDate.now(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        for (String v : values) {
            assertEquals(v, Masks.maskText(v, days), "data value hit by a mask: " + v);
            assertFalse(v.matches(".*\\d{2}-\\d{2}-\\d{4}.*"), "data value looks like the PDF print date: " + v);
        }
    }

    private static void collect(JsonArray rows, List<String> out) {
        for (JsonElement r : rows) {
            for (JsonElement v : r.getAsJsonObject().asMap().values()) {
                if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                    out.add(v.getAsString());
                }
            }
        }
    }

    /** -Dequivalence.raw=&lt;dir&gt;: the raw (unmasked) sidecar bytes, to measure the raw difference to the goldens. */
    private static void dumpRaw(EquivCase c, String format, byte[] bytes) throws Exception {
        String out = System.getProperty("equivalence.raw");
        if (out != null && !out.isEmpty()) {
            Path f = Paths.get(out, c.name, format + "." + EquivCase.ext(format));
            Files.createDirectories(f.getParent());
            Files.write(f, bytes);
        }
    }

    private static void dump(String side, EquivCase c, String format, String facet) throws Exception {
        String out = System.getProperty("equivalence.out");
        if (out == null || out.isEmpty()) {
            return;
        }
        Path f = Paths.get(out, side, c.name, format + ".txt");
        Files.createDirectories(f.getParent());
        Files.write(f, facet.getBytes(StandardCharsets.UTF_8));
    }
}
