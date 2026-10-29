package cl.bcs.jobmanager;

import cl.bcs.jobmanager.domain.Programa;
import cl.bcs.jobmanager.support.BatH2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cl.bcs.jobmanager.support.BatH2.AHORA;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Queue order: oldest fechaCreacion first (harmless FIFO; the legacy had no ORDER BY). There is no row locking
 * (no FOR UPDATE, no SKIP LOCKED): the job module claims the program atomically, so there is nothing to test
 * against HANA here and no skipped test.
 */
class FifoOrderTest {

    @Test
    void pendientesSeDevuelvenDelMasAntiguoAlMasNuevo() {
        BatH2 bat = new BatH2();
        bat.tipo("TEST", "cl.bcs.TestJob", 3);
        long tercero = bat.programa("TEST").creacion(AHORA.minusSeconds(60)).insertar();
        long primero = bat.programa("TEST").creacion(AHORA.minusSeconds(3_000)).insertar();
        long segundo = bat.programa("TEST").creacion(AHORA.minusSeconds(1_000)).insertar();
        List<Long> orden = bat.adapter.readEligiblePrograms("", List.of("TEST"), AHORA).stream()
                .map(Programa::getIdPrograma).toList();
        assertThat(orden).containsExactly(primero, segundo, tercero);
    }

    @Test
    void conIgualFechaDeCreacionDesempataPorIdPrograma() {
        BatH2 bat = new BatH2();
        bat.tipo("TEST", "cl.bcs.TestJob", 3);
        long a = bat.programa("TEST").creacion(AHORA.minusSeconds(500)).insertar();
        long b = bat.programa("TEST").creacion(AHORA.minusSeconds(500)).insertar();
        assertThat(bat.adapter.readEligiblePrograms("", List.of("TEST"), AHORA).stream().map(Programa::getIdPrograma).toList())
                .containsExactly(a, b);
    }
}
