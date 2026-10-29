package cl.bcs.plataforma.cre;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.adapter.in.javacall.ConsultarEstadoModuloHandler;

/**
 * Informative latency measurement (R-01: 1.77 M calls, the UI polls every 15 s per session): the handler on in-memory H2 with the 5 modules of
 * the base seed, no cache (the legacy has none). It prints p50/p99/max and the allocated bytes per call. NOT a performance proof: H2 in the
 * same JVM has no network and no HANA; the only hard assertion is a very loose ceiling so it cannot flake.
 */
class ConsultarEstadoModuloLatenciaTest {

    @Test
    void mideLatenciaYAsignacionesPorLlamada() throws Exception {
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdModulo\":\"\",\"p_IdUsuario\":97}").getAsJsonObject();
        ConsultarEstadoModuloHandler h = new ConsultarEstadoModuloHandler();
        try (Connection con = OraculoCasos.abrir(OraculoCasos.caso("PLA-ESM-01"))) {
            for (int i = 0; i < 3000; i++) h.call(con, in);                         // warm-up
            int n = 20000;
            long[] t = new long[n];
            var mx = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
            long a0 = mx.getCurrentThreadAllocatedBytes();
            for (int i = 0; i < n; i++) {
                long s = System.nanoTime();
                h.call(con, in);
                t[i] = System.nanoTime() - s;
            }
            long bytes = (mx.getCurrentThreadAllocatedBytes() - a0) / n;
            Arrays.sort(t);
            System.out.printf("LATENCIA ConsultarEstadoModulo (H2 en memoria, 5 modulos, %d llamadas): p50=%.1f us p99=%.1f us max=%.1f us, ~%d bytes asignados/llamada%n",
                    n, t[n / 2] / 1e3, t[(int) (n * 0.99)] / 1e3, t[n - 1] / 1e3, bytes);
            assertThat(t[n / 2]).isLessThan(50_000_000L);
        }
    }
}
