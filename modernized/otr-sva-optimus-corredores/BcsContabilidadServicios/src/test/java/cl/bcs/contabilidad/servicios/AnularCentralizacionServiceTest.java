package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.AnularCentralizacionService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy cl.bcs.contabilidad.AnularCentralizacion
 * (legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/AnularCentralizacion.java).
 * Not pinned here: RevisarRequisitosBasicos (L96-102) is a platform call; these cases assume it passes.
 */
class AnularCentralizacionServiceTest {

    static final String SEL_CEN = "AnularCentralizacion_SelectCentralizacion.sql";
    static final String SEL_CMP = "AnularCentralizacion_SelectComprobantes.sql";
    static final String UPD_EVT = "AnularCentralizacion_UpdateEvento.sql";
    static final String UPD_MOVD = "AnularCentralizacion_UpdateMovimientoDato.sql";
    static final String UPD_MOV = "AnularCentralizacion_UpdateMovimiento.sql";
    static final String UPD_CEN = "AnularCentralizacion_UpdateCentralizacion.sql";

    final RecordingJdbc jdbc = new RecordingJdbc();

    JsonObject run() {
        AnularCentralizacionService svc = LegacySql.injectBlank(new AnularCentralizacionService(jdbc),
                "SQL_SELECT_CENTRALIZACION=" + SEL_CEN, "SQL_SELECT_COMPROBANTES=" + SEL_CMP,
                "SQL_UPDATE_EVENTO=" + UPD_EVT, "SQL_UPDATE_MOVIMIENTO=" + UPD_MOV,
                "SQL_UPDATE_MOVIMIENTO_DATO=" + UPD_MOVD, "SQL_UPDATE_CENTRALIZACION=" + UPD_CEN);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CON\",\"p_IdCentralizacion\":386}"));
    }

    void noUpdates() {
        assertThat(jdbc.executions(UPD_EVT) + jdbc.executions(UPD_MOVD) + jdbc.executions(UPD_MOV) + jdbc.executions(UPD_CEN)).isZero();
    }

    @Test
    @DisplayName("CONT-G-140 reads centralizacion and comprobantes with :IdCentralizacion = p_IdCentralizacion")
    void contG140_queriesByIdCentralizacion() {
        // legacy AnularCentralizacion.java:104-109
        jdbc.rows(SEL_CEN, row("IdCentralizacion", 386L, "Estado", "V", "EstadoNombre", "Vigente"));
        run();
        assertThat(jdbc.single(SEL_CEN).params()).containsEntry("IdCentralizacion", 386L);
        assertThat(jdbc.single(SEL_CMP).params()).containsEntry("IdCentralizacion", 386L);
    }

    @Test
    @DisplayName("CONT-G-141 unknown centralizacion -> 1 error CENTRALIZACION_NO_EXISTE id=p_IdCentralizacion, nothing updated")
    void contG141_noExiste() {
        // legacy AnularCentralizacion.java:111-116
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        List<JsonObject> m = mensajes(out);
        assertThat(m).hasSize(1);
        assertMensaje(m.get(0), 386L, "ERROR", "CENTRALIZACION_NO_EXISTE");
        noUpdates();
    }

    @ParameterizedTest(name = "estado {0}")
    @ValueSource(strings = {"A", "E", "M"})
    @DisplayName("CONT-G-142 estados A/E/M are not anulables -> CENTRALIZACION_NO_SE_PUEDE_ANULAR")
    void contG142_estadosNoAnulables(String estado) {
        // legacy AnularCentralizacion.java:34 (estadosNoAnulables) and 119-126
        jdbc.rows(SEL_CEN, row("IdCentralizacion", 386L, "Estado", estado, "EstadoNombre", "X"));
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 386L, "ERROR", "CENTRALIZACION_NO_SE_PUEDE_ANULAR");
        noUpdates();
    }

    @Test
    @DisplayName("CONT-G-143 estado check precedes contabilizada check (A + precomprobantes -> NO_SE_PUEDE_ANULAR)")
    void contG143_estadoBeforeContabilizada() {
        // legacy AnularCentralizacion.java:119-134 (order of the ifs)
        jdbc.rows(SEL_CEN, row("IdCentralizacion", 386L, "Estado", "A", "EstadoNombre", "Anulada"))
            .rows(SEL_CMP, row("IdPreComprobanteAsiento", 5L));
        assertThat(codigos(run())).containsExactly("CENTRALIZACION_NO_SE_PUEDE_ANULAR");
    }

    @Test
    @DisplayName("CONT-G-144 vigente with precomprobante asientos -> CENTRALIZACION_ESTA_CONTABILIZADA, nothing updated")
    void contG144_contabilizada() {
        // legacy AnularCentralizacion.java:128-134
        jdbc.rows(SEL_CEN, row("IdCentralizacion", 386L, "Estado", "V", "EstadoNombre", "Vigente"))
            .rows(SEL_CMP, row("IdPreComprobanteAsiento", 5L));
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        assertMensaje(mensajes(out).get(0), 386L, "ERROR", "CENTRALIZACION_ESTA_CONTABILIZADA");
        noUpdates();
    }

    @ParameterizedTest(name = "estado {0}")
    @ValueSource(strings = {"V", "C"})
    @DisplayName("CONT-G-145 anulable -> updates Evento, MovimientoDato, Movimiento, Centralizacion (in that order) with :IdCentralizacion")
    void contG145_anula(String estado) {
        // legacy AnularCentralizacion.java:136-139, 143
        jdbc.rows(SEL_CEN, row("IdCentralizacion", 386L, "Estado", estado, "EstadoNombre", "X"));
        JsonObject out = run();
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).isEmpty();
        assertThat(jdbc.executedFilesInOrder()).containsSubsequence(UPD_EVT, UPD_MOVD, UPD_MOV, UPD_CEN);
        for (String f : List.of(UPD_EVT, UPD_MOVD, UPD_MOV, UPD_CEN)) {
            assertThat(jdbc.executedParamSets(f)).as(f).hasSize(1);
            assertThat(jdbc.executedParamSets(f).get(0)).as(f).containsEntry("IdCentralizacion", 386L);
        }
    }
}
