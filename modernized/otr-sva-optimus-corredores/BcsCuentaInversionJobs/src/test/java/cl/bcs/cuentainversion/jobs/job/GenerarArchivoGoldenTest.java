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
 * Legacy the generic file generator GenerarArchivo (with Archivo, FormatoArchivo, FormatoArchivoDetalle): structure, TXT / CSV, directories, failures (GA-nn) and the field formatter (GF-nn), against the goldens recorded by executing the UNMODIFIED legacy classes under the real legacy
 * {@code BcsJob.run()} (the PAUTA steps are called directly): outcome, exception, status calls, the order of commits and rollbacks, the
 * tables, the files (bytes) and what the code handed to each collaborator. The cases are under
 * {@code src/test/resources/cuenta-inversion-jobs/cases}; the prefix of a case id selects its job.
 */
class GenerarArchivoGoldenTest {

    private static final AtomicInteger EJECUTADOS_GA = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_GF = new AtomicInteger();

    static List<String> casosGA() {
        return Caso.todos("GA-");
    }

    @Test
    void inventarioGA() {
        assertThat(casosGA()).as("GA cases on the classpath (GenerarArchivo)").hasSizeGreaterThanOrEqualTo(36);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosGA")
    void generarArchivo(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_GA.incrementAndGet();
    }
    static List<String> casosGF() {
        return Caso.todos("GF-");
    }

    @Test
    void inventarioGF() {
        assertThat(casosGF()).as("GF cases on the classpath (GenerarArchivo (formatter))").hasSizeGreaterThanOrEqualTo(36);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosGF")
    void formatoDeCampos(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_GF.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (GenerarArchivo): " + EJECUTADOS_GA.get() + "/" + casosGA().size());
        System.out.println("equivalence cases executed (GenerarArchivo (formatter)): " + EJECUTADOS_GF.get() + "/" + casosGF().size());
    }
}
