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
 * Legacy the file jobs that delegate to a service: GenerarArchivoCargoAbonoJob (GC), GenerarArchivoClientesPorPagarYCobrarJob (CP), GenerarArchivoCuentasPorCobrarAnexo16Job (A1), GenerarArchivoDiferenciaSaldoJob (DS), GenerarArchivoSaldoCuentasJob (SC), against the goldens recorded by executing the UNMODIFIED legacy classes under the real legacy
 * {@code BcsJob.run()} (the PAUTA steps are called directly): outcome, exception, status calls, the order of commits and rollbacks, the
 * tables, the files (bytes) and what the code handed to each collaborator. The cases are under
 * {@code src/test/resources/cuenta-inversion-jobs/cases}; the prefix of a case id selects its job.
 */
class ArchivosServiciosGoldenTest {

    private static final AtomicInteger EJECUTADOS_GC = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_CP = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_A1 = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_DS = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_SC = new AtomicInteger();

    static List<String> casosGC() {
        return Caso.todos("GC-");
    }

    @Test
    void inventarioGC() {
        assertThat(casosGC()).as("GC cases on the classpath (GenerarArchivoCargoAbonoJob)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosGC")
    void archivoCargoAbono(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_GC.incrementAndGet();
    }
    static List<String> casosCP() {
        return Caso.todos("CP-");
    }

    @Test
    void inventarioCP() {
        assertThat(casosCP()).as("CP cases on the classpath (GenerarArchivoClientesPorPagarYCobrarJob)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosCP")
    void archivoClientesPorPagarYCobrar(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_CP.incrementAndGet();
    }
    static List<String> casosA1() {
        return Caso.todos("A1-");
    }

    @Test
    void inventarioA1() {
        assertThat(casosA1()).as("A1 cases on the classpath (GenerarArchivoCuentasPorCobrarAnexo16Job)").hasSizeGreaterThanOrEqualTo(12);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosA1")
    void archivoAnexo16(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_A1.incrementAndGet();
    }
    static List<String> casosDS() {
        return Caso.todos("DS-");
    }

    @Test
    void inventarioDS() {
        assertThat(casosDS()).as("DS cases on the classpath (GenerarArchivoDiferenciaSaldoJob)").hasSizeGreaterThanOrEqualTo(12);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosDS")
    void archivoDiferenciaSaldo(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_DS.incrementAndGet();
    }
    static List<String> casosSC() {
        return Caso.todos("SC-");
    }

    @Test
    void inventarioSC() {
        assertThat(casosSC()).as("SC cases on the classpath (GenerarArchivoSaldoCuentasJob)").hasSizeGreaterThanOrEqualTo(18);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosSC")
    void archivoSaldoCuentas(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_SC.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (GenerarArchivoCargoAbonoJob): " + EJECUTADOS_GC.get() + "/" + casosGC().size());
        System.out.println("equivalence cases executed (GenerarArchivoClientesPorPagarYCobrarJob): " + EJECUTADOS_CP.get() + "/" + casosCP().size());
        System.out.println("equivalence cases executed (GenerarArchivoCuentasPorCobrarAnexo16Job): " + EJECUTADOS_A1.get() + "/" + casosA1().size());
        System.out.println("equivalence cases executed (GenerarArchivoDiferenciaSaldoJob): " + EJECUTADOS_DS.get() + "/" + casosDS().size());
        System.out.println("equivalence cases executed (GenerarArchivoSaldoCuentasJob): " + EJECUTADOS_SC.get() + "/" + casosSC().size());
    }
}
