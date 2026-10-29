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
 * Legacy aging and creditor reports: GenerarReporteAcreedoresJob (RA), GenerarReporteAntiguedadDeudaBolsaCTAJob (AB), GenerarReporteAntiguedadDeudaCTAJob (AC), PautaGenerarReporteAcreedores (PR), PautaGenerarReporteAntiguedadDeudaBolsaCTA (PB), against the goldens recorded by executing the UNMODIFIED legacy classes under the real legacy
 * {@code BcsJob.run()} (the PAUTA steps are called directly): outcome, exception, status calls, the order of commits and rollbacks, the
 * tables, the files (bytes) and what the code handed to each collaborator. The cases are under
 * {@code src/test/resources/cuenta-inversion-jobs/cases}; the prefix of a case id selects its job.
 */
class ReportesAntiguedadGoldenTest {

    private static final AtomicInteger EJECUTADOS_RA = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_AB = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_AC = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_PR = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_PB = new AtomicInteger();

    static List<String> casosRA() {
        return Caso.todos("RA-");
    }

    @Test
    void inventarioRA() {
        assertThat(casosRA()).as("RA cases on the classpath (GenerarReporteAcreedoresJob)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosRA")
    void reporteAcreedores(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_RA.incrementAndGet();
    }
    static List<String> casosAB() {
        return Caso.todos("AB-");
    }

    @Test
    void inventarioAB() {
        assertThat(casosAB()).as("AB cases on the classpath (GenerarReporteAntiguedadDeudaBolsaCTAJob)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosAB")
    void reporteAntiguedadBolsa(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_AB.incrementAndGet();
    }
    static List<String> casosAC() {
        return Caso.todos("AC-");
    }

    @Test
    void inventarioAC() {
        assertThat(casosAC()).as("AC cases on the classpath (GenerarReporteAntiguedadDeudaCTAJob)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosAC")
    void reporteAntiguedad(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_AC.incrementAndGet();
    }
    static List<String> casosPR() {
        return Caso.todos("PR-");
    }

    @Test
    void inventarioPR() {
        assertThat(casosPR()).as("PR cases on the classpath (PautaGenerarReporteAcreedores)").hasSizeGreaterThanOrEqualTo(7);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosPR")
    void pautaReporteAcreedores(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_PR.incrementAndGet();
    }
    static List<String> casosPB() {
        return Caso.todos("PB-");
    }

    @Test
    void inventarioPB() {
        assertThat(casosPB()).as("PB cases on the classpath (PautaGenerarReporteAntiguedadDeudaBolsaCTA)").hasSizeGreaterThanOrEqualTo(7);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosPB")
    void pautaReporteAntiguedadBolsa(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_PB.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (GenerarReporteAcreedoresJob): " + EJECUTADOS_RA.get() + "/" + casosRA().size());
        System.out.println("equivalence cases executed (GenerarReporteAntiguedadDeudaBolsaCTAJob): " + EJECUTADOS_AB.get() + "/" + casosAB().size());
        System.out.println("equivalence cases executed (GenerarReporteAntiguedadDeudaCTAJob): " + EJECUTADOS_AC.get() + "/" + casosAC().size());
        System.out.println("equivalence cases executed (PautaGenerarReporteAcreedores): " + EJECUTADOS_PR.get() + "/" + casosPR().size());
        System.out.println("equivalence cases executed (PautaGenerarReporteAntiguedadDeudaBolsaCTA): " + EJECUTADOS_PB.get() + "/" + casosPB().size());
    }
}
