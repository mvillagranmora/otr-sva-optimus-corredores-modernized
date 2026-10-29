package cl.bcs.cuentainversion.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.cuentainversion.jobs.support.Caso;
import cl.bcs.cuentainversion.jobs.support.Equivalencia;
import cl.bcs.cuentainversion.jobs.support.JobsDeCaso;

/**
 * Legacy LibroInventarioJob: the stored procedure ActualizarSaldoLibroInventario, parameters and failures (LI), against the goldens recorded by executing the UNMODIFIED legacy classes under the real legacy
 * {@code BcsJob.run()} (the PAUTA steps are called directly): outcome, exception, status calls, the order of commits and rollbacks, the
 * tables, the files (bytes) and what the code handed to each collaborator. The cases are under
 * {@code src/test/resources/cuenta-inversion-jobs/cases}; the prefix of a case id selects its job.
 */
class LibroInventarioGoldenTest {

    private static final AtomicInteger EJECUTADOS_LI = new AtomicInteger();

    static List<String> casosLI() {
        return Caso.todos("LI-");
    }

    @Test
    void inventarioLI() {
        assertThat(casosLI()).as("LI cases on the classpath (LibroInventarioJob)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosLI")
    void libroInventario(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_LI.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (LibroInventarioJob): " + EJECUTADOS_LI.get() + "/" + casosLI().size());
    }
}
