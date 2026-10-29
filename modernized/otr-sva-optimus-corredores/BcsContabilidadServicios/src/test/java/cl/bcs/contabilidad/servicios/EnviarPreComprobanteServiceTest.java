package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import cl.bcs.contabilidad.service.EnviarPreComprobanteService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy cl.bcs.contabilidad.EnviarPreComprobante (EnviarPreComprobante.java).
 * Not observable at this layer: the InscribirProcesoBatch "ASIENTO_SAP" program registration (L137-157)
 * whose IdPrograma is written by the UPDATE; these cases only require that some IdPrograma is bound.
 * The platform registration is a port (ProcesoBatchPort); a fake returning a fixed IdPrograma stands in for it.
 */
class EnviarPreComprobanteServiceTest {

    static final String SEL = "EnviarPreComprobante_SelectPreComprobantes.sql";
    static final String UPD = "EnviarPreComprobante_UpdatePreComprobantes.sql";

    final RecordingJdbc jdbc = new RecordingJdbc();

    /** Fake platform registration: no errors, fixed IdPrograma (legacy draws it from a platform sequence). */
    static final ProcesoBatchPort PROCESO_BATCH = (emp, mod, usr, tipo, desfase, params) ->
            new ProcesoBatchPort.Inscripcion(0, List.of(), 9001L);

    JsonObject run(String secCmbte) {
        EnviarPreComprobanteService svc = LegacySql.injectBlank(new EnviarPreComprobanteService(jdbc, PROCESO_BATCH),
                "SQL_SELECT=" + SEL, "SQL_UPDATE=" + UPD);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"CON\",\"p_UsaReglasNegocio\":\"N\",\"p_SecCmbte\":\""
                + secCmbte + "\",\"p_Destino\":\"B\"}"));
    }

    @Test
    @DisplayName("CONT-G-160 p_SecCmbte '1132;1133;' -> :IdPreComprobantes = [1132, 1133]")
    void contG160_parsesSecCmbte() {
        // legacy EnviarPreComprobante.java:93, 98-100
        run("1132;1133;");
        assertThat(jdbc.single(SEL).params()).containsEntry("IdPreComprobantes", List.of(1132L, 1133L));
    }

    @Test
    @DisplayName("CONT-G-161 non-numeric and blank tokens are dropped")
    void contG161_dropsNonNumeric() {
        // legacy EnviarPreComprobante.java:93 (NumberUtils.isNumber filter)
        run("abc;12; ;;13");
        assertThat(jdbc.single(SEL).params()).containsEntry("IdPreComprobantes", List.of(12L, 13L));
    }

    @Test
    @DisplayName("CONT-G-162 a signed token '-5' is accepted by NumberUtils.isNumber and parsed")
    void contG162_signedTokenAccepted() {
        // legacy EnviarPreComprobante.java:93 (isNumber("-5") is true, Long.parseLong("-5") = -5)
        run("-5;7");
        assertThat(jdbc.single(SEL).params()).containsEntry("IdPreComprobantes", List.of(-5L, 7L));
    }

    @Test
    @DisplayName("CONT-G-163 no valid sequence -> ERROR_PRECOMPROBANTE_VACIO id 0, but P_OUT_ERRORESCONTADOR stays 0, no query")
    void contG163_vacio() {
        // legacy EnviarPreComprobante.java:56 (counter starts 0), 94-97 (returns before L130 recount)
        JsonObject out = run(";;");
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "ERROR_PRECOMPROBANTE_VACIO");
        assertThat(jdbc.callsTo(SEL)).isEmpty();
    }

    @Test
    @DisplayName("CONT-G-164 estados outside V/D/E -> one ERROR_PROCESO_PRECOMPROBANTE each, ids 0,1..; counter = #errors; nothing updated")
    void contG164_estadosInvalidos() {
        // legacy EnviarPreComprobante.java:102-125 (cont.getAndIncrement ids), 130-134
        jdbc.rows(SEL, row("IdPreComprobante", 1L, "Estado", "P", "NombreEstado", "Procesado"),
                row("IdPreComprobante", 2L, "Estado", "V", "NombreEstado", "Vigente"),
                row("IdPreComprobante", 3L, "Estado", "C", "NombreEstado", "Contabilizado"));
        JsonObject out = run("1;2;3");
        assertThat(errores(out)).isEqualTo(2L);
        List<JsonObject> m = mensajes(out);
        assertThat(m).hasSize(2);
        assertMensaje(m.get(0), 0L, "ERROR", "ERROR_PROCESO_PRECOMPROBANTE");
        assertMensaje(m.get(1), 1L, "ERROR", "ERROR_PROCESO_PRECOMPROBANTE");
        assertThat(jdbc.executions(UPD)).isZero();
    }

    @Test
    @DisplayName("CONT-G-165 all vigentes -> UPDATE per precomprobante with :IdPrograma and :IdPreComprobante, INFORMACION PRECOMPROBANTES_EN_ENVIO_SAP")
    void contG165_enviaVigentes() {
        // legacy EnviarPreComprobante.java:160-167
        jdbc.rows(SEL, row("IdPreComprobante", 1L, "Estado", "V", "NombreEstado", "Vigente"),
                row("IdPreComprobante", 2L, "Estado", "E", "NombreEstado", "Error"));
        JsonObject out = run("1;2");
        assertThat(errores(out)).isZero();
        List<Map<String, Object>> sets = jdbc.executedParamSets(UPD);
        assertThat(sets).extracting(s -> s.get("IdPreComprobante")).containsExactly(1L, 2L);
        assertThat(sets).allSatisfy(s -> assertThat(s.get("IdPrograma")).isNotNull().isNotSameAs(RecordingJdbc.MISSING));
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 0L, "INFORMACION", "PRECOMPROBANTES_EN_ENVIO_SAP");
    }

    @Test
    @DisplayName("CONT-G-166 any descuadrado (D) -> still updated, ADVERTENCIA PRECOMPROBANTES_EN_ENVIO_SAP_DESC")
    void contG166_descuadrado() {
        // legacy EnviarPreComprobante.java:127, 169-171
        jdbc.rows(SEL, row("IdPreComprobante", 1L, "Estado", "V", "NombreEstado", "Vigente"),
                row("IdPreComprobante", 2L, "Estado", "D", "NombreEstado", "Descuadrado"));
        JsonObject out = run("1;2");
        assertThat(errores(out)).isZero();
        assertThat(jdbc.executions(UPD)).isEqualTo(2);
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "PRECOMPROBANTES_EN_ENVIO_SAP_DESC");
    }
}
