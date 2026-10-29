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
 * Legacy balance jobs and PAUTA steps: ActualizarSaldoCargoAbonoJob (AS), ActualizarSaldosReporteAntiguedadJob (AR), CargaInicialSaldosReporteAntiguedadJob (CI), PautaActualizarSaldoCargoAbono (PS), PautaGuardarSaldosReporteAntiguedad (PG), against the goldens recorded by executing the UNMODIFIED legacy classes under the real legacy
 * {@code BcsJob.run()} (the PAUTA steps are called directly): outcome, exception, status calls, the order of commits and rollbacks, the
 * tables, the files (bytes) and what the code handed to each collaborator. The cases are under
 * {@code src/test/resources/cuenta-inversion-jobs/cases}; the prefix of a case id selects its job.
 */
class SaldosGoldenTest {

    private static final AtomicInteger EJECUTADOS_AS = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_AR = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_CI = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_PS = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_PG = new AtomicInteger();

    static List<String> casosAS() {
        return Caso.todos("AS-");
    }

    @Test
    void inventarioAS() {
        assertThat(casosAS()).as("AS cases on the classpath (ActualizarSaldoCargoAbonoJob)").hasSizeGreaterThanOrEqualTo(18);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosAS")
    void actualizarSaldoCargoAbono(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_AS.incrementAndGet();
    }
    static List<String> casosAR() {
        return Caso.todos("AR-");
    }

    @Test
    void inventarioAR() {
        assertThat(casosAR()).as("AR cases on the classpath (ActualizarSaldosReporteAntiguedadJob)").hasSizeGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosAR")
    void actualizarSaldosReporteAntiguedad(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_AR.incrementAndGet();
    }
    static List<String> casosCI() {
        return Caso.todos("CI-");
    }

    @Test
    void inventarioCI() {
        assertThat(casosCI()).as("CI cases on the classpath (CargaInicialSaldosReporteAntiguedadJob)").hasSizeGreaterThanOrEqualTo(8);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosCI")
    void cargaInicialSaldosReporteAntiguedad(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_CI.incrementAndGet();
    }
    static List<String> casosPS() {
        return Caso.todos("PS-");
    }

    @Test
    void inventarioPS() {
        assertThat(casosPS()).as("PS cases on the classpath (PautaActualizarSaldoCargoAbono)").hasSizeGreaterThanOrEqualTo(7);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosPS")
    void pautaActualizarSaldoCargoAbono(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_PS.incrementAndGet();
    }
    static List<String> casosPG() {
        return Caso.todos("PG-");
    }

    @Test
    void inventarioPG() {
        assertThat(casosPG()).as("PG cases on the classpath (PautaGuardarSaldosReporteAntiguedad)").hasSizeGreaterThanOrEqualTo(7);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosPG")
    void pautaGuardarSaldosReporteAntiguedad(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_PG.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (ActualizarSaldoCargoAbonoJob): " + EJECUTADOS_AS.get() + "/" + casosAS().size());
        System.out.println("equivalence cases executed (ActualizarSaldosReporteAntiguedadJob): " + EJECUTADOS_AR.get() + "/" + casosAR().size());
        System.out.println("equivalence cases executed (CargaInicialSaldosReporteAntiguedadJob): " + EJECUTADOS_CI.get() + "/" + casosCI().size());
        System.out.println("equivalence cases executed (PautaActualizarSaldoCargoAbono): " + EJECUTADOS_PS.get() + "/" + casosPS().size());
        System.out.println("equivalence cases executed (PautaGuardarSaldosReporteAntiguedad): " + EJECUTADOS_PG.get() + "/" + casosPG().size());
    }
}
