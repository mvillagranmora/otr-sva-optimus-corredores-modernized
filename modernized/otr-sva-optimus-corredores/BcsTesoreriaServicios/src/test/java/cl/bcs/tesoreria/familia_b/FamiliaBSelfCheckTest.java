package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Inventory of the family B goldens (not an equivalence test, excluded from the dual-execution dump by its name): a missing, empty or
 * hand-edited golden FAILS here instead of silently shrinking the suite.
 */
class FamiliaBSelfCheckTest {

    private static final Path CASOS = Path.of("src/test/resources/tesoreria-b/cases");

    private static List<Path> directorios() throws IOException {
        try (Stream<Path> s = Files.list(CASOS)) {
            return s.filter(Files::isDirectory).sorted().toList();
        }
    }

    @Test
    void cadaCasoTieneEntradaYGoldenGrabadoPorElOraculo() throws IOException {
        List<String> problemas = new ArrayList<>();
        for (Path d : directorios()) {
            Path c = d.resolve("case.json");
            Path e = d.resolve("expected.json");
            if (!Files.exists(c)) problemas.add(d.getFileName() + ": missing case.json");
            if (!Files.exists(e)) { problemas.add(d.getFileName() + ": missing expected.json"); continue; }
            JsonObject g = JsonParser.parseString(Files.readString(e, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!g.has("_provenance") || !g.get("_provenance").getAsString().contains("Recorded by legacy-oracle")) {
                problemas.add(d.getFileName() + ": golden without oracle provenance (hand written?)");
            }
            if (!g.has("salida") && !g.has("excepcion")) problemas.add(d.getFileName() + ": neither salida nor excepcion");
        }
        assertThat(problemas).isEmpty();
        assertThat(directorios()).as("recorded cases").hasSizeGreaterThanOrEqualTo(330);
    }

    @Test
    void losIdentificadoresTESBSonConsecutivosYUnicos() throws IOException {
        Pattern p = Pattern.compile("^TES-B-(\\d{2,3})_.+");
        List<Integer> ids = new ArrayList<>();
        for (Path d : directorios()) {
            var m = p.matcher(d.getFileName().toString());
            assertThat(m.matches()).as(d.getFileName().toString()).isTrue();
            ids.add(Integer.parseInt(m.group(1)));
        }
        assertThat(ids).doesNotHaveDuplicates();
        assertThat(ids).contains(1, 330);
    }

    @Test
    void cadaCasoGrabadoLoReferenciaUnTestCompiladoOPendiente() throws IOException {
        StringBuilder fuentes = new StringBuilder();
        for (Path raiz : List.of(Path.of("src/test/java/cl/bcs/tesoreria/familia_b"), Path.of("src/test-pending/tesoreria-b"))) {
            try (Stream<Path> s = Files.walk(raiz)) {
                for (Path f : s.filter(x -> x.toString().endsWith(".java")).toList()) {
                    fuentes.append(Files.readString(f, StandardCharsets.UTF_8));
                }
            }
        }
        String texto = fuentes.toString();
        List<String> sinTest = new ArrayList<>();
        for (Path d : directorios()) {
            if (!texto.contains("\"" + d.getFileName() + "\"")) sinTest.add(d.getFileName().toString());
        }
        assertThat(sinTest).as("recorded cases no test refers to").isEmpty();
    }

    @Test
    void ningunaFixtureContieneCredencialesObvias() throws IOException {
        Pattern secreto = Pattern.compile("(?i)(password|passwd|secret|api[_-]?key|bearer\\s+[a-z0-9]{12,})\\s*[:=]\\s*\"?[^\"\\s,}]{6,}");
        List<String> hallazgos = new ArrayList<>();
        for (Path d : directorios()) {
            try (Stream<Path> fs = Files.list(d)) {
                for (Path f : fs.toList()) {
                    String t = Files.readString(f, StandardCharsets.UTF_8);
                    if (secreto.matcher(t).find()) hallazgos.add(f.toString());
                }
            }
        }
        assertThat(hallazgos).isEmpty();
    }
}
