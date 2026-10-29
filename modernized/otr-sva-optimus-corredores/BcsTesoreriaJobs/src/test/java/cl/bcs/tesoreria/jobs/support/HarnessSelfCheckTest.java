package cl.bcs.tesoreria.jobs.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Checks of the harness itself (no job code runs here): the goldens are complete and well formed, no secret is a fixture, and the canonical
 * comparison can both accept equivalent output and reject a different one (so a green comparison is not vacuous).
 */
class HarnessSelfCheckTest {

    @Test
    @DisplayName("every case has case.json, expected.json with its provenance and the facets the comparison reads")
    void casosCompletos() throws Exception {
        List<String> todos = Caso.todos("");
        assertThat(todos).hasSizeGreaterThanOrEqualTo(290);
        for (String id : todos) {
            Path dir = Caso.recurso(Caso.RAIZ + "/cases/" + id);
            assertThat(dir.resolve("case.json")).as(id).exists();
            JsonObject e = JsonParser.parseString(Files.readString(dir.resolve("expected.json"), StandardCharsets.UTF_8)).getAsJsonObject();
            assertThat(e.get("_provenance").getAsString()).as(id).contains("unmodified legacy");
            assertThat(e.has("desenlace") && e.has("tablas") && e.has("archivos") && e.has("colaboradores")).as(id + " facets").isTrue();
            assertThat(e.has("salida") || e.has("estados") || e.has("excepcion")).as(id + " has an outcome facet").isTrue();
        }
    }

    @Test
    @DisplayName("no credential-looking value in any case, seed or golden (only fake same-shape values are allowed)")
    void sinSecretos() throws Exception {
        Pattern clave = Pattern.compile("(?i)\"[^\"]*(password|passwd|pwd|secret|token|clave)[^\"]*\"\\s*:\\s*\"[^\"<]+\"");
        List<String> hallazgos = new ArrayList<>();
        for (String id : Caso.todos("")) {
            Path dir = Caso.recurso(Caso.RAIZ + "/cases/" + id);
            try (var s = Files.list(dir)) {
                for (Path f : s.toList()) {
                    if (clave.matcher(Files.readString(f, StandardCharsets.UTF_8)).find()) {
                        hallazgos.add(id + "/" + f.getFileName());
                    }
                }
            }
        }
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("canonical text ignores key order, nulls and number scale, masks the wall-clock part of file names, and still sees a real difference")
    void canonicalizacion() {
        var a = Canon.parse("{\"b\": 1.50, \"a\": null, \"f\": \"X_20250401_121530.TXT\"}");
        var b = Canon.parse("{\"f\": \"X_20991231_235959.TXT\", \"b\": 1.5}");
        assertThat(Canon.texto(a)).isEqualTo(Canon.texto(b));
        assertThat(Canon.texto(Canon.parse("{\"b\": 1.5}"))).isNotEqualTo(Canon.texto(Canon.parse("{\"b\": 1.6}")));
        assertThat(Canon.texto(Canon.parse("[1,2]"))).isNotEqualTo(Canon.texto(Canon.parse("[2,1]")));
    }

    @Test
    @DisplayName("exception messages are made reproducible: NullPointerException text, identity hashes, OS error text, base path")
    void mensajes() {
        Path base = Path.of("tmp-base").toAbsolutePath();
        assertThat(Canon.mensajeDe(new NullPointerException("Cannot invoke \"a.b()\" because \"x\" is null"), base)).isEqualTo("<npe>");
        assertThat(Canon.mensaje("java.lang.Exception: java.lang.NullPointerException: Cannot invoke \"a\"", base)).isEqualTo("java.lang.Exception: java.lang.NullPointerException: <npe>");
        assertThat(Canon.mensaje("[Ljava.lang.String;@616ac46a", base)).isEqualTo("[Ljava.lang.String;@<hash>");
        assertThat(Canon.mensaje(base + "/tes\\f.txt (The system cannot find the path specified)", base)).isEqualTo("${RUTA_BASE}/tes/f.txt (<os-error>)");
    }

    @Test
    @DisplayName("file capture is byte exact: a one-byte difference changes the digest, the BOM and the line ends are visible")
    void capturaDeArchivos() throws Exception {
        Path d = Archivos.nuevaRaiz(List.of("tes"));
        Files.write(d.resolve("tes/a.csv"), new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'x', '\r', '\n'});
        JsonObject c1 = Archivos.capturar(d);
        Path e = Archivos.nuevaRaiz(List.of("tes"));
        Files.write(e.resolve("tes/a.csv"), new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'x', '\n'});
        JsonObject c2 = Archivos.capturar(e);
        assertThat(c1.getAsJsonObject("archivos").getAsJsonObject("tes/a.csv").get("texto").getAsString()).isEqualTo("\\uFEFFx\\r\\n");
        assertThat(Canon.texto(c1)).isNotEqualTo(Canon.texto(c2));
        assertThat(c1.getAsJsonArray("directorios").get(0).getAsString()).isEqualTo("tes");
    }
}
