package cl.bcs.tesoreria.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.tesoreria.jobs.support.Caso;
import cl.bcs.tesoreria.jobs.support.Equivalencia;
import cl.bcs.tesoreria.jobs.support.Observacion;
import cl.bcs.tesoreria.jobs.support.Puertos;
import cl.bcs.tesoreria.jobs.support.ServiciosDeCaso;
import cl.bcs.tesoreria.jobs.support.Traza;

/**
 * Legacy {@code GenerarArchivoCargos} (242 lines) with its {@code Archivo} and {@code FormatoArchivo} against the goldens CG-nn (structure,
 * parameters, outcome, FTP storage, failures) and CG-Fnn (the field formatter: CHAR / DATE / NUM / DEC boundaries), recorded by executing the
 * UNMODIFIED legacy class under the real legacy {@code BcsJob.run()}. The file name carries the wall clock of the legacy run; it is masked as
 * {@code <TS>} on both sides (legacy-oracle/README in this package).
 */
class GenerarArchivoCargosGoldenTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static List<String> casos() {
        return Caso.todos("CG-");
    }

    @Test
    void inventarioDeCasos() {
        assertThat(casos()).as("CG cases on the classpath").hasSizeGreaterThanOrEqualTo(80);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void golden(String id) throws Exception {
        Caso caso = Caso.cargar(id);
        Traza traza = new Traza(caso.colaboradores());
        Clock reloj = Clock.fixed(Instant.parse("2025-04-01T15:15:30Z"), ZoneOffset.UTC);
        GenerarArchivoCargos job = new GenerarArchivoCargos(ServiciosDeCaso.formatos(traza), ServiciosDeCaso.mandatos(traza),
                Puertos.procedimiento(traza), Puertos.secuencias(caso, traza), caso.bd().jdbc(), reloj);
        Equivalencia.comparar(caso, Observacion.ejecutar(caso, job, traza));
        EJECUTADOS.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (GenerarArchivoCargos): " + EJECUTADOS.get() + "/" + casos().size());
    }
}
