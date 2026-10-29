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
 * Legacy {@code BcsJobCargaMasivaIngresoEgresoDAPFFMM} (447 lines) against the goldens CM-nn recorded by executing the UNMODIFIED legacy
 * class under the real legacy {@code BcsJob.run()} (see legacy-oracle/record.sh and the README of this package). Each case compares:
 * outcome (COMMIT / ROLLBACK / ERROR), exception type and message, status listener calls, commit / rollback order, the tables dumped,
 * the files written (bytes), the directories, and the trace of what the job handed to every collaborator.
 */
class CargaMasivaGoldenTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static List<String> casos() {
        return Caso.todos("CM-");
    }

    @Test
    void inventarioDeCasos() {
        assertThat(casos()).as("CM cases on the classpath").hasSizeGreaterThanOrEqualTo(60);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void golden(String id) throws Exception {
        Caso caso = Caso.cargar(id);
        Traza traza = new Traza(caso.colaboradores());
        BcsJobCargaMasivaIngresoEgresoDAPFFMM job = new BcsJobCargaMasivaIngresoEgresoDAPFFMM(ServiciosDeCaso.carga(traza), Puertos.cabeceras(traza),
                new ColaboradoresDeCaso(traza), Puertos.configuracion(caso), Puertos.diccionario(), Puertos.reloj(caso), caso.bd().jdbc());
        Equivalencia.comparar(caso, Observacion.ejecutar(caso, job, traza));
        EJECUTADOS.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (BcsJobCargaMasivaIngresoEgresoDAPFFMM): " + EJECUTADOS.get() + "/" + casos().size());
    }
}
