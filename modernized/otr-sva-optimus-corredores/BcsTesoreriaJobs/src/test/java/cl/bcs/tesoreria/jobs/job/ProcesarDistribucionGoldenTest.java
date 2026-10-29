package cl.bcs.tesoreria.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.tesoreria.jobs.support.Caso;
import cl.bcs.tesoreria.jobs.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.jobs.support.Equivalencia;
import cl.bcs.tesoreria.jobs.support.Observacion;
import cl.bcs.tesoreria.jobs.support.Puertos;
import cl.bcs.tesoreria.jobs.support.ServiciosDeCaso;
import cl.bcs.tesoreria.jobs.support.Traza;

/**
 * Legacy {@code BcsJobProcesarDistribucion} (169 lines, PAC rendition job) against the goldens PD-nn recorded by executing the UNMODIFIED
 * legacy class under the real legacy {@code BcsJob.run()}: the CSV (bytes, BOM included), {@code BAT.Programa.RutaSalida}, the two commits
 * (job + flag reset), the rollbacks of a failure, the status calls and what the job handed to ProcesarDistribucion, ValidarMandatosPAC and
 * ValidaEjecucionProcesoRecPAC.
 */
class ProcesarDistribucionGoldenTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static List<String> casos() {
        return Caso.todos("PD-");
    }

    @Test
    void inventarioDeCasos() {
        assertThat(casos()).as("PD cases on the classpath").hasSizeGreaterThanOrEqualTo(30);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void golden(String id) throws Exception {
        Caso caso = Caso.cargar(id);
        Traza traza = new Traza(caso.colaboradores());
        BcsJobProcesarDistribucion job = new BcsJobProcesarDistribucion(ServiciosDeCaso.procesarDistribucion(traza), new ColaboradoresDeCaso(traza),
                Puertos.configuracion(caso), Puertos.reloj(caso));
        Equivalencia.comparar(caso, Observacion.ejecutar(caso, job, traza));
        EJECUTADOS.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (BcsJobProcesarDistribucion): " + EJECUTADOS.get() + "/" + casos().size());
    }
}
