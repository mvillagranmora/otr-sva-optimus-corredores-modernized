package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.ConsultarConfiguracionesConceptoNormativoService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/** Characterization of legacy cl.bcs.contabilidad.ConsultarConfiguracionesConceptoNormativo (ConsultarConfiguracionesConceptoNormativo.java). */
class ConsultarConfiguracionesConceptoNormativoServiceTest {

    static final String SEL = "ConsultarConfiguracionesConceptoNormativo_SelectConceptosConfiguraciones.sql";
    final RecordingJdbc jdbc = new RecordingJdbc();

    JsonObject run() {
        return new ConsultarConfiguracionesConceptoNormativoService(jdbc)
                .execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdConceptoNormativo\":2,\"p_IdModulo\":\"CON\"}"));
    }

    @Test
    @DisplayName("CONT-G-380 executes the legacy statement with :IdConceptoNormativo = p_IdConceptoNormativo")
    void contG380_param() {
        // legacy ConsultarConfiguracionesConceptoNormativo.java:61-63
        run();
        assertThat(jdbc.single(SEL).params()).containsEntry("IdConceptoNormativo", 2L);
    }

    @Test
    @DisplayName("CONT-G-381 rows returned in 'configuracionConcepto' with the 7 legacy fields")
    void contG381_rows() {
        // legacy ConsultarConfiguracionesConceptoNormativo.java:20-33
        jdbc.rows(SEL, row("IdConceptoNormativo", 2L, "IdTipoDatoEvento", "TipoInstrumento", "ValorVarchar", "ACX",
                "OperadorFuncion", "+", "IdEstado", "ACT", "IdCondicionConceptoNormativo", 10L, "Descripcion", "Acciones"));
        JsonObject out = run();
        assertThat(errores(out)).isZero();
        assertThat(array(out, "configuracionConcepto")).singleElement().satisfies(o -> {
            assertThat(lng(o, "IdCondicionConceptoNormativo")).isEqualTo(10L);
            assertThat(lng(o, "IdConceptoNormativo")).isEqualTo(2L);
            assertThat(str(o, "IdTipoDatoEvento")).isEqualTo("TipoInstrumento");
            assertThat(str(o, "ValorVarchar")).isEqualTo("ACX");
            assertThat(str(o, "OperadorFuncion")).isEqualTo("+");
            assertThat(str(o, "IdEstado")).isEqualTo("ACT");
            assertThat(str(o, "Descripcion")).isEqualTo("Acciones");
        });
    }

    @Test
    @DisplayName("CONT-G-382 failure -> counter 1, P_OUT_MENSAJE = exception message, configuracionConcepto []")
    void contG382_failure() {
        // legacy ConsultarConfiguracionesConceptoNormativo.java:57, 64-67
        jdbc.failOn(SEL);
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isNotBlank();
        assertThat(array(out, "configuracionConcepto")).isEmpty();
    }
}
