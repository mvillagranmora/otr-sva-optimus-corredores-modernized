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
import cl.bcs.tesoreria.jobs.support.Traza;

/**
 * Legacy {@code GenerarArchivoPago} (418 lines) with its {@code Archivo} and {@code FormatoArchivo} against the goldens PG-nn (format lookup,
 * parameters, TXT / CSV / DAT output, file creation, nomina update, failures), PG-Fnn (the field formatter), PG-Bnn (BANCO_ESTADO_CLP_TRADVP
 * through GenerarArchivoPagos) and PG-Cnn (BCP telecredito), recorded by executing the UNMODIFIED legacy class under the real legacy
 * {@code BcsJob.run()}. The format and the nomina data are rows of the case database (verbatim legacy SQL); the stored procedure is scripted.
 */
class GenerarArchivoPagoGoldenTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static List<String> casos() {
        return Caso.todos("PG-").stream().filter(id -> !id.startsWith("PG-E") && !id.startsWith("PG-D")).toList();
    }

    @Test
    void inventarioDeCasos() {
        assertThat(casos()).as("PG job cases on the classpath").hasSizeGreaterThanOrEqualTo(100);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void golden(String id) throws Exception {
        Caso caso = Caso.cargar(id);
        Traza traza = new Traza(caso.colaboradores());
        Clock reloj = Clock.fixed(Instant.parse("2025-04-01T15:15:30Z"), ZoneOffset.UTC);
        GenerarArchivoPago job = new GenerarArchivoPago(Puertos.procedimiento(traza), Puertos.configuracion(caso), caso.bd().jdbc(), reloj);
        Equivalencia.comparar(caso, Observacion.ejecutar(caso, job, traza));
        EJECUTADOS.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (GenerarArchivoPago): " + EJECUTADOS.get() + "/" + casos().size());
    }
}
