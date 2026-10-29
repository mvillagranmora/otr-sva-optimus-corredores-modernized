package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import cl.bcs.cuentainversion.transaccional.support.Caso;

/**
 * Keeps the characterization suite honest: every recorded case directory is exercised by exactly one test class (or is listed in
 * {@code pending-cases.txt} with the reason), every case has its golden with provenance, and nothing is silently skipped.
 */
class TransaccionalInventarioTest {

    private static List<String> casosEjecutados() {
        List<String> todos = new ArrayList<>();
        todos.addAll(ActualizarSaldoCargoAbonoServiceTest.CASOS);
        todos.addAll(CertificarCargoAbonoServiceTest.CASOS);
        todos.addAll(AnularCargoAbonoServiceTest.CASOS);
        todos.addAll(CrearCargoAbonoServiceTest.CASOS);
        todos.addAll(SaldoCuentaInversionServiceTest.CASOS);
        todos.addAll(ConsultarSaldoCuentaInversionServiceTest.CASOS);
        todos.addAll(OtrosCasos.todos());
        return todos;
    }

    @Test
    void cadaDirectorioDeCasoTieneExactamenteUnTest() throws Exception {
        List<String> ejecutados = casosEjecutados();
        Set<String> pendientes = new TreeSet<>();
        Path p = Caso.recurso(Caso.RAIZ + "/pending-cases.txt");
        for (String l : Files.readAllLines(p)) {
            if (!l.isBlank() && !l.startsWith("#")) {
                pendientes.add(l.split("\\s+")[0]);
            }
        }
        Set<String> sinDuplicados = new TreeSet<>(ejecutados);
        assertThat(sinDuplicados).as("a case is run by more than one test").hasSameSizeAs(ejecutados);
        Set<String> enDisco = new TreeSet<>(Caso.todos());
        Set<String> esperados = new TreeSet<>(sinDuplicados);
        esperados.addAll(pendientes);
        assertThat(enDisco).as("case directories vs cases run (+ pending-cases.txt)").isEqualTo(esperados);
    }

    @Test
    void cadaCasoTieneGoldenGrabadoPorElOraculo() throws Exception {
        for (String nombre : Caso.todos()) {
            Path dir = Caso.recurso(Caso.RAIZ + "/cases/" + nombre);
            assertThat(dir.resolve("case.json")).as(nombre + "/case.json").exists();
            Path exp = dir.resolve("expected.json");
            assertThat(exp).as(nombre + "/expected.json (record it with legacy-oracle/record.sh)").exists();
            assertThat(Files.readString(exp)).as(nombre + " provenance").contains("Recorded by legacy-oracle/runner/OracleRunner.java");
        }
    }

    @Test
    void elNumeroDeCasosEjecutadosNoEsCero() {
        assertThat(casosEjecutados().size()).as("equivalence cases wired to tests").isGreaterThan(0);
    }
}
