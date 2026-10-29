package cl.bcs.jobmanager;

import cl.bcs.jobmanager.adapter.out.ProgramaJdbcAdapter;
import cl.bcs.jobmanager.port.out.ProgramaPort;
import cl.bcs.jobmanager.support.BatH2;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static cl.bcs.jobmanager.support.BatH2.AHORA;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * RULE-059 is DISABLED (decision of 2026-10-21): the legacy never retried (updateTryProgramm is commented out,
 * BcsJobManager.java:104-105), a failure stays ERR and is relaunched by hand (ReprocesarPrograma). These tests pin
 * that the dispatcher has no retry and cannot write programs at all.
 */
class RetryTest {

    @Test
    void elPuertoDeProgramasEsSoloLectura() {
        List<String> metodos = Arrays.stream(ProgramaPort.class.getDeclaredMethods()).map(Method::getName).toList();
        assertThat(metodos).containsExactly("readEligiblePrograms");
        assertThat(Arrays.stream(ProgramaJdbcAdapter.class.getDeclaredMethods()).map(Method::getName))
                .noneMatch(n -> n.toLowerCase().contains("retry") || n.startsWith("update") || n.startsWith("lock"));
    }

    @Test
    void unErrConIntentosRestantesNoVuelveAPen_niSeSeleccionaNiSeToca() {
        BatH2 bat = new BatH2();
        bat.tipo("T", "cl.bcs.x.Job", 3);
        long errParcial = bat.programa("T").estado("ERR").bloqueo("S").intento(1).insertar();
        long errAgotado = bat.programa("T").estado("ERR").bloqueo("S").intento(3).insertar();
        List<Object> a = bat.filaCompleta(errParcial);
        List<Object> b = bat.filaCompleta(errAgotado);
        for (int i = 0; i < 3; i++) {
            assertThat(bat.adapter.readEligiblePrograms("", List.of("T"), AHORA)).isEmpty();
        }
        assertThat(bat.filaCompleta(errParcial)).isEqualTo(a);
        assertThat(bat.filaCompleta(errAgotado)).isEqualTo(b);
    }
}
