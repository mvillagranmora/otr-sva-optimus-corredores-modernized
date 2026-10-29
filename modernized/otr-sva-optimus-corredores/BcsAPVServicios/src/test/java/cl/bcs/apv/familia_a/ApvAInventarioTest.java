package cl.bcs.apv.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.familia_a.support.Caso;

/**
 * Keeps the characterization suite honest: every recorded case directory is exercised by exactly one compiled test; every case has its golden with
 * provenance, the behaviour id of its name, the legacy citation and a plausible number of recorded facets; nothing is silently skipped.
 * The list of test classes ({@link ApvAClases}) is generated together with them.
 */
class ApvAInventarioTest {

    @Test
    void cadaDirectorioDeCasoTieneExactamenteUnTest() {
        List<String> ejecutados = new ArrayList<>();
        ApvAClases.CASOS.forEach(ejecutados::addAll);
        Set<String> sinDuplicados = new TreeSet<>(ejecutados);
        assertThat(sinDuplicados).as("a case is run by more than one test").hasSameSizeAs(ejecutados);
        Set<String> enDisco = new TreeSet<>(Caso.todos());
        assertThat(enDisco).as("case directories vs cases run by the generated tests (regenerate with mktests.py)").isEqualTo(sinDuplicados);
        assertThat(enDisco).as("equivalence cases defined (zero = not proven)").isNotEmpty();
    }

    @Test
    void cadaCasoTieneGoldenGrabadoPorElOraculoYSuCita() throws Exception {
        for (String nombre : Caso.todos()) {
            Path dir = Caso.recurso(Caso.RAIZ + "/cases/" + nombre);
            assertThat(dir.resolve("case.json")).as(nombre + "/case.json").exists();
            Path exp = dir.resolve("expected.json");
            assertThat(exp).as(nombre + "/expected.json (record it with legacy-oracle/record.sh)").exists();
            assertThat(Files.readString(exp)).as(nombre + " provenance").contains("Recorded by legacy-oracle/runner/OracleRunner.java");
            JsonObject caso = JsonParser.parseString(Files.readString(dir.resolve("case.json"))).getAsJsonObject();
            assertThat(nombre).as("directory name starts with the behaviour id").startsWith(caso.get("id").getAsString() + "_");
            assertThat(caso.get("pina").getAsString()).as(nombre + " cites the legacy file:line it pins").matches("[A-Za-z0-9_]+\\.java:\\d+(-\\d+)?");
            JsonObject esperado = JsonParser.parseString(Files.readString(exp)).getAsJsonObject();
            assertThat(esperado.has("salida") || esperado.has("excepcion")).as(nombre + ": the golden records an output or an exception").isTrue();
        }
    }
}
