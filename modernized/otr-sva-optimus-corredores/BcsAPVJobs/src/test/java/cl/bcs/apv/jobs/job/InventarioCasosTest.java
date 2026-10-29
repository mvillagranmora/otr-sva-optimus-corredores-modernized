package cl.bcs.apv.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.apv.jobs.support.CasoJob;
import cl.bcs.apv.jobs.support.VerificaJob;

/** Every case directory is run by exactly one generated test method; each has an input, a golden and a provenance; nothing is skipped. */
class InventarioCasosTest {

    static final List<String> JOBS = List.of("BcsJobCargaMasivaPrevired", "IngresarOrdenBonificacionFiscal", "IngresarOrdenFMBonificacionFiscal",
            "IngresarOrdenRVBonificacionFiscal");

    @Test
    @DisplayName("inventory: each case directory is exercised by exactly one generated test; no test is disabled")
    void cadaCasoTieneUnTest() throws Exception {
        int total = 0;
        for (String job : JOBS) {
            Class<?> k = Class.forName("cl.bcs.apv.jobs.job." + job + "CasosTest");
            long tests = java.util.Arrays.stream(k.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(org.junit.jupiter.api.Test.class)).count();
            assertThat(tests).as(job).isEqualTo(VerificaJob.casosDe(job).size()).isGreaterThan(0);
            for (Method m : k.getDeclaredMethods()) {
                assertThat(m.isAnnotationPresent(org.junit.jupiter.api.Disabled.class)).as(m.getName()).isFalse();
            }
            total += tests;
        }
        assertThat(CasoJob.todos()).hasSize(total);
    }

    @Test
    @DisplayName("inventory: every golden says it was recorded from the unmodified legacy and names the outcome")
    void cadaGoldenTieneProcedencia() {
        for (String n : CasoJob.todos()) {
            JsonObject e = CasoJob.cargar(n).esperado();
            assertThat(e.get("_provenance").getAsString()).as(n).contains("unmodified legacy");
            assertThat(e.getAsJsonObject("resultado").get("desenlace").getAsString()).as(n).isIn("COMMIT", "ROLLBACK", "ERROR");
        }
    }
}
