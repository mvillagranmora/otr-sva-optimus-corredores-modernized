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
 * Same 21 cases x 5 formats and same legacy goldens/masks as EquivalenceTest, but EVERY case goes through the new HTTP
 * contract (POST /report with the legacy servlet's JSON body, X-Internal-Token), also the 16 that the old
 * /reports/generate contract could not carry. -Dequivalence.out=&lt;dir&gt; writes the facets for compare.py.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:equivrest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.internal-token=" + RestRenderer.TOKEN
})
@AutoConfigureMockMvc
class EquivalenceRestTest {

    private static final Path EQUIV = Paths.get(System.getProperty("equivalence.cases", "src/test/resources/crystal/equiv"));

    @Autowired MockMvc mvc;
    @Autowired DataSource ds;

    @BeforeAll
    static void environment() {
        CrystalTestEnvironment.ensure();
    }

    @TestFactory
    Stream<DynamicTest> everyCaseEveryFormatThroughRest() throws Exception {
        List<EquivCase> cases = EquivCase.all(EQUIV);
        assertFalse(cases.isEmpty(), "no equivalence cases under " + EQUIV);
        H2Shim.init(ds);
        RestRenderer renderer = new RestRenderer(mvc);
        List<DynamicTest> tests = new ArrayList<>();
        for (EquivCase c : cases) {
            H2Shim.putTemplate(ds, c.name, Paths.get(c.template));
            for (String f : c.formats) {
                tests.add(DynamicTest.dynamicTest(c.name + " [" + f + "] REST", () -> check(renderer, c, f)));
            }
        }
        return tests.stream();
    }

    private void check(RestRenderer renderer, EquivCase c, String format) throws Exception {
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
        byte[] rest = renderer.render(c, format);
        String fl = Facet.of(c.name, format, legacy, windows, days);
        String fn = Facet.of(c.name, format, rest, windows, days);
        System.out.println("EQUIV-REST " + c.name + " [" + format + "] mode=" + renderer.lastMode);
        dump("legacy", c, format, fl);
        dump("new", c, format, fn);
        assertFalse(renderer.lastStderr.contains("Exception"), "swallowed engine error: " + renderer.lastStderr);
        if ("excel".equals(format)) {
            for (long[] x : windows) {
                assertTrue(XlsWindows.temporal(XlsWindows.doubleAt(rest, x[0])), "REST window is not a date/time at " + x[0]);
            }
        }
        assertEquals(fl, fn, c.name + " " + format + " facet differs (REST)");
        byte[] a = Masks.apply(format, legacy, windows, days);
        byte[] b = Masks.apply(format, rest, windows, days);
        if (!Arrays.equals(a, b)) {
            int i = 0;
            while (i < Math.min(a.length, b.length) && a[i] == b[i]) {
                i++;
            }
            fail(c.name + " " + format + ": masked bytes differ, first difference at " + i + " (legacy " + a.length
                    + " B, REST " + b.length + " B)");
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
