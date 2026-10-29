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
 * Legacy equity and fee reports: BcsJobGenerarInformePatrimonioDiario (IP), BcsJobGenerarInformeResumenCobro (RC), GuardarPatrimonioCajaJob (GP), against the goldens recorded by executing the UNMODIFIED legacy classes under the real legacy
 * {@code BcsJob.run()} (the PAUTA steps are called directly): outcome, exception, status calls, the order of commits and rollbacks, the
 * tables, the files (bytes) and what the code handed to each collaborator. The cases are under
 * {@code src/test/resources/cuenta-inversion-jobs/cases}; the prefix of a case id selects its job.
 */
class InformesPatrimonioGoldenTest {

    private static final AtomicInteger EJECUTADOS_IP = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_RC = new AtomicInteger();
    private static final AtomicInteger EJECUTADOS_GP = new AtomicInteger();

    static List<String> casosIP() {
        return Caso.todos("IP-");
    }

    @Test
    void inventarioIP() {
        assertThat(casosIP()).as("IP cases on the classpath (BcsJobGenerarInformePatrimonioDiario)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosIP")
    void informePatrimonioDiario(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_IP.incrementAndGet();
    }
    static List<String> casosRC() {
        return Caso.todos("RC-");
    }

    @Test
    void inventarioRC() {
        assertThat(casosRC()).as("RC cases on the classpath (BcsJobGenerarInformeResumenCobro)").hasSizeGreaterThanOrEqualTo(14);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosRC")
    void informeResumenCobro(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_RC.incrementAndGet();
    }
    static List<String> casosGP() {
        return Caso.todos("GP-");
    }

    @Test
    void inventarioGP() {
        assertThat(casosGP()).as("GP cases on the classpath (GuardarPatrimonioCajaJob)").hasSizeGreaterThanOrEqualTo(12);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosGP")
    void guardarPatrimonioCaja(String id) {
        Caso caso = Caso.cargar(id);
        Equivalencia.comparar(caso, JobsDeCaso.observar(caso));
        EJECUTADOS_GP.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (BcsJobGenerarInformePatrimonioDiario): " + EJECUTADOS_IP.get() + "/" + casosIP().size());
        System.out.println("equivalence cases executed (BcsJobGenerarInformeResumenCobro): " + EJECUTADOS_RC.get() + "/" + casosRC().size());
        System.out.println("equivalence cases executed (GuardarPatrimonioCajaJob): " + EJECUTADOS_GP.get() + "/" + casosGP().size());
    }
}
