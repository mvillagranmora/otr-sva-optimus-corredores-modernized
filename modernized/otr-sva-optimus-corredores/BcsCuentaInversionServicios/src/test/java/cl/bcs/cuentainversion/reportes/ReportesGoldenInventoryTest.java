package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Inventory guard of the recorded legacy goldens (family B, ids CTA-B-NN): every case directory must have case.json and
 * expected.json (a missing fixture FAILS), and must be claimed by exactly one test class: a compiled *ServiceTest or a
 * pending *ServicePendingTest (src/test-pending/reportes). A recorded case that no test runs proves nothing.
 */
class ReportesGoldenInventoryTest {

    static final int CASOS_REGISTRADOS = 225;
    private static final Pattern CASO = Pattern.compile("\"(CTA-B-\\d+_[a-z0-9-]+)\"");

    private static Map<String, List<String>> reclamados(Path dir, String sufijo) throws IOException {
        Map<String, List<String>> m = new HashMap<>();
        assertThat(dir).as("directory of tests " + dir.toAbsolutePath()).isDirectory();
        try (Stream<Path> s = Files.list(dir)) {
            for (Path f : s.filter(p -> p.getFileName().toString().endsWith(sufijo)).toList()) {
                Matcher mt = CASO.matcher(Files.readString(f, StandardCharsets.UTF_8));
                List<String> vistos = new ArrayList<>();
                while (mt.find()) {
                    if (!vistos.contains(mt.group(1))) {
                        vistos.add(mt.group(1));
                    }
                }
                for (String c : vistos) {
                    m.computeIfAbsent(c, k -> new ArrayList<>()).add(f.getFileName().toString());
                }
            }
        }
        return m;
    }

    @Test
    void everyRecordedCaseHasCaseAndExpectedJson() {
        List<String> todos = CasoReporte.todos();
        assertThat(todos).as("recorded cases").hasSize(CASOS_REGISTRADOS);
        for (String c : todos) {
            assertThat(CasoReporte.recurso("reportes/cases/" + c + "/case.json")).as(c).exists();
            assertThat(CasoReporte.recurso("reportes/cases/" + c + "/expected.json")).as(c).exists();
        }
    }

    @Test
    void everyRecordedCaseIsClaimedByExactlyOneTestClass() throws IOException {
        Path base = Path.of("src");
        Map<String, List<String>> todos = new HashMap<>();
        reclamados(base.resolve("test/java/cl/bcs/cuentainversion/reportes"), "ServiceTest.java")
                .forEach((k, v) -> todos.computeIfAbsent(k, x -> new ArrayList<>()).addAll(v));
        reclamados(base.resolve("test-pending/reportes"), "ServicePendingTest.java")
                .forEach((k, v) -> todos.computeIfAbsent(k, x -> new ArrayList<>()).addAll(v));
        assertThat(todos.keySet()).as("cases claimed by tests").containsExactlyInAnyOrderElementsOf(CasoReporte.todos());
        todos.forEach((c, v) -> assertThat(v).as("test classes claiming " + c).hasSize(1));
    }

    @Test
    void caseIdsAreUniqueAndNumbered() {
        List<Integer> ids = CasoReporte.todos().stream().map(c -> Integer.parseInt(c.substring(6, c.indexOf('_')))).toList();
        assertThat(ids).doesNotHaveDuplicates();
        assertThat(ids).contains(1, CASOS_REGISTRADOS);
    }
}
