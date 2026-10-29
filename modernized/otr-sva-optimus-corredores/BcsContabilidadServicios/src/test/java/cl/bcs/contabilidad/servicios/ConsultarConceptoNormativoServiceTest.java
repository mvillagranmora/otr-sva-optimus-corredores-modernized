package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.ConsultarConceptoNormativoService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/** Characterization of legacy cl.bcs.contabilidad.ConsultarConceptoNormativo (ConsultarConceptoNormativo.java). */
class ConsultarConceptoNormativoServiceTest {

    static final String SEL = "ConsultarConceptoNormativo_SelectConceptosNormativos.sql";
    final RecordingJdbc jdbc = new RecordingJdbc();

    JsonObject run() {
        return new ConsultarConceptoNormativoService(jdbc).execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\"}"));
    }

    @Test
    @DisplayName("CONT-G-360 executes the legacy SELECT on ITZ.ConceptoNormativo once")
    void contG360_sql() {
        // legacy ConsultarConceptoNormativo.java:18, 56
        run();
        assertThat(jdbc.callsTo(SEL)).hasSize(1);
    }

    @Test
    @DisplayName("CONT-G-361 rows returned in 'conceptosNormativos' {IdConceptoNormativo, ConceptoNormativo, IdEstado}; counter 0, no P_OUT_MENSAJE")
    void contG361_rows() {
        // legacy ConsultarConceptoNormativo.java:20-30, 49-56 (new Gson(): nulls omitted)
        jdbc.rows(SEL, row("IdConceptoNormativo", 1L, "ConceptoNormativo", "Activo circulante", "IdEstado", "ACT"));
        JsonObject out = run();
        assertThat(errores(out)).isZero();
        assertThat(out.has("P_OUT_MENSAJE")).isFalse();
        assertThat(array(out, "conceptosNormativos")).singleElement().satisfies(o -> {
            assertThat(lng(o, "IdConceptoNormativo")).isEqualTo(1L);
            assertThat(str(o, "ConceptoNormativo")).isEqualTo("Activo circulante");
            assertThat(str(o, "IdEstado")).isEqualTo("ACT");
        });
    }

    @Test
    @DisplayName("CONT-G-362 failure -> counter 1, P_OUT_MENSAJE = exception message, conceptosNormativos []")
    void contG362_failure() {
        // legacy ConsultarConceptoNormativo.java:52, 57-60
        jdbc.failOn(SEL);
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isNotBlank();
        assertThat(array(out, "conceptosNormativos")).isEmpty();
    }
}
