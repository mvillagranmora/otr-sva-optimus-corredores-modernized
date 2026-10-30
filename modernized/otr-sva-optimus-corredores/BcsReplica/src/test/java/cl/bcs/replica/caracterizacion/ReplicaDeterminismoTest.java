package cl.bcs.replica.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Determinism: the same case run again in fresh H2 databases and fresh service instances produces the very same dump (no state kept between requests, no wall clock: the clock is injected), and the
 * oracle's own report says the goldens were stable over 20 runs per case.
 */
class ReplicaDeterminismoTest {

    @Test
    @DisplayName("BcsReplica: the oracle recorded every golden stable (20 runs per case, 0 unstable)")
    void elOraculoGraboGoldensEstables() {
        String informe = CasoReplica.texto("determinism.txt").split("\n")[0];
        assertTrue(informe.contains("repeats per case: 20") && informe.contains("unstable: 0") && informe.contains("hard: 0"), "determinism report: " + informe);
    }

    @Test
    @DisplayName("BcsReplica: running a case again gives the same dump (every case, two more runs)")
    void unaCorridaRepetidaEsIgual() throws Exception {
        AtomicInteger n = new AtomicInteger();
        List<String> ids = CasoReplica.casos();
        for (String id : ids) {
            JsonObject primera = CasoReplica.corrida(id);
            for (int i = 0; i < 2; i++) {
                JsonObject otra = CasoReplica.ejecutar(id);
                CasoReplica.comparar(id + " (repeat " + (i + 1) + ")", primera, otra);
            }
            n.incrementAndGet();
        }
        assertEquals(ids.size(), n.get());
    }
}
