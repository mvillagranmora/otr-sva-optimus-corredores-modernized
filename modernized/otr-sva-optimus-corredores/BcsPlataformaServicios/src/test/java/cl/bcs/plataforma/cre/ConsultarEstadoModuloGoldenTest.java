package cl.bcs.plataforma.cre;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.adapter.in.javacall.JavaCallUnitOfWorkFilter;

/**
 * Characterization of the NEW ConsultarEstadoModulo against the 29 goldens recorded from the unmodified legacy (PLA-ESM-01..11,
 * D-ESM-1..7). One dynamic test per case. The only approved difference is case 29 (PLA-ESM-10, D-ESM-5): the golden pins the legacy
 * exception text "... Query: <SQL> Parameters: [{IdEmpresa=1}]", which the new service does not reproduce (DEV-A3, Q-ESM-6); there the test
 * requires an SQL failure carrying the SQLException cause and no "Query:"/"Parameters:" text.
 */
class ConsultarEstadoModuloGoldenTest {

    @TestFactory
    List<DynamicTest> plaEsm_cadaCasoDorado_coincideConElLegacy() throws Exception {
        List<Path> casos = OraculoCasos.casos();
        assertThat(casos).as("golden cases found").hasSize(29);
        System.out.println("equivalence cases executed (ConsultarEstadoModulo): " + casos.size());
        return casos.stream().map(dir -> DynamicTest.dynamicTest(dir.getFileName().toString(), () -> verificar(dir))).toList();
    }

    static void verificar(Path dir) throws Exception {
        JsonObject golden = OraculoCasos.json(dir.resolve("expected.json"));
        OraculoCasos.Resultado r = OraculoCasos.ejecutar(dir);
        if (golden.has("salidaCruda")) {
            assertThat(r.excepcion()).as("no exception expected").isNull();
            assertThat(r.salidaCruda()).as("byte-identical output text").isEqualTo(golden.get("salidaCruda").getAsString());
            // servlet decision of the golden = decision of the unit of work filter for a 200 with this body
            assertThat(JavaCallUnitOfWorkFilter.confirmable(200, r.salidaCruda().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .isEqualTo("COMMIT".equals(golden.get("decisionServlet").getAsString()));
            return;
        }
        String esperada = golden.get("excepcion").getAsString();
        assertThat(r.excepcion()).as("exception expected: " + esperada).isNotNull();
        assertThat(golden.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_HTTP500");
        if (esperada.contains(" Query: ")) {
            // APPROVED DIFFERENCE DEV-A3 / Q-ESM-6 (case 29)
            assertThat(r.excepcion().getCause()).isInstanceOf(SQLException.class);
            assertThat(String.valueOf(r.excepcion().getMessage())).doesNotContain("Query:").doesNotContain("Parameters:");
        } else {
            assertThat(r.excepcion().getClass().getSimpleName() + ": " + r.excepcion().getMessage()).isEqualTo(esperada);
        }
    }

    @Test
    void plaEsm10_soloElCaso29EsUnaDiferenciaAprobada() throws Exception {
        long conTextoDeConsulta = OraculoCasos.casos().stream().filter(d -> {
            try {
                JsonObject g = OraculoCasos.json(d.resolve("expected.json"));
                return g.has("excepcion") && g.get("excepcion").getAsString().contains(" Query: ");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).count();
        assertThat(conTextoDeConsulta).isEqualTo(1);
    }
}
