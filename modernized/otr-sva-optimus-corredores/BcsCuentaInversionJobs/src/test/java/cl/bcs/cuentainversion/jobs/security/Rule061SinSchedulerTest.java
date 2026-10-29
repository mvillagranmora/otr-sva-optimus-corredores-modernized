package cl.bcs.cuentainversion.jobs.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RULE-061 / decision 2026-10-15: the jobs run only when the Job Manager calls {@code POST /internal/job-call}; the module has NO scheduler of
 * its own: no {@code @Scheduled}, no {@code @EnableScheduling}, no {@code Timer}, no {@code ScheduledExecutorService}, no thread sleep loop.
 */
class Rule061SinSchedulerTest {

    private static boolean contiene(byte[] datos, String patron) {
        return new String(datos, StandardCharsets.ISO_8859_1).contains(patron);
    }

    @Test
    @DisplayName("RULE-061: no scheduler annotation or executor in any compiled class of the module")
    void rule061_sinPlanificador() {
        List<String> marcas = List.of("org/springframework/scheduling/annotation/Scheduled", "org/springframework/scheduling/annotation/EnableScheduling",
                "java/util/Timer", "ScheduledExecutorService", "ScheduledThreadPoolExecutor");
        List<String> hallazgos = new ArrayList<>();
        List<Path> clases = ModuloFuente.clasesCompiladas();
        for (Path p : clases) {
            byte[] b = ModuloFuente.bytes(p);
            for (String m : marcas) {
                if (contiene(b, m)) {
                    hallazgos.add(ModuloFuente.nombreClase(p) + ": " + m);
                }
            }
        }
        assertThat(clases).hasSizeGreaterThanOrEqualTo(ModuloFuente.CLASES_OBLIGATORIAS.size());
        assertThat(hallazgos).isEmpty();
    }

    @Test
    @DisplayName("RULE-061: no scheduling source text: @Scheduled, @EnableScheduling, 'while (true)', Thread.sleep")
    void rule061_sinTextoDePlanificador() {
        List<String> hallazgos = new ArrayList<>();
        for (Path p : ModuloFuente.fuentesJava()) {
            String t = ModuloFuente.texto(p);
            for (String m : List.of("@Scheduled", "@EnableScheduling", "while (true)", "Thread.sleep")) {
                if (t.contains(m)) {
                    hallazgos.add(p.getFileName() + ": " + m);
                }
            }
        }
        assertThat(hallazgos).isEmpty();
    }
}
