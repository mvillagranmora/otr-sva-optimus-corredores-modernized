package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.CargarCabeceraComprobanteService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Characterization of legacy cl.bcs.contabilidad.CargarCabeceraComprobante (CargarCabeceraComprobante.java). */
class CargarCabeceraComprobanteServiceTest {

    static final String INS_CMP = "CargarCabeceraComprobante_InsertCombrobante.sql";
    static final String SEL_ID = "CargarCabeceraComprobante_SelectIdCombrobante.sql";
    static final String INS_CEN = "CargarCabeceraComprobante_InsertCentralizaciones.sql";
    static final String INS_GLO = "CargarCabeceraComprobante_InsertGlosa.sql";

    final RecordingJdbc jdbc = new RecordingJdbc().sequence("BSCL.Contabilidad.BD.Secuencias::IdTipoContabilidad", 7001, "Id");

    static String line(String validacion, String nemo, Object centralizacion, String glosa) {
        return "{\"CCCT_AGRUPA\":\"S\",\"CCCT_EJERCICIO\":2024,\"CCCT_FORMATO\":\"F1\",\"CCCT_NEMOTECNICO\":\"" + nemo + "\","
                + "\"CCCT_NOMBRETIPOCONTABILIDAD\":\"Comprobante " + nemo + "\",\"CCCT_REVERSA\":\"N\",\"CCCT_TIPOCOMPROBANTE\":\"T\","
                + "\"CCCT_TIPOGENERACION\":\"A\",\"CCCT_CENTRALIZACION\":" + centralizacion + ","
                + "\"CCCT_GLOSA\":" + (glosa == null ? "null" : "\"" + glosa + "\"") + ",\"MENSAJEVALIDACION\":\"" + validacion + "\"}";
    }

    JsonObject run(String... lines) {
        CargarCabeceraComprobanteService svc = LegacySql.injectBlank(new CargarCabeceraComprobanteService(jdbc),
                "SQL_INSERT_COMPROBANTE=" + INS_CMP, "SQL_SELECT_ID_COMPROBANTE=" + SEL_ID,
                "SQL_INSERT_CENTRALIZACIONES=" + INS_CEN, "SQL_INSERT_GLOSA=" + INS_GLO);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_Comprobantes\":[" + String.join(",", lines) + "]}"));
    }

    @Test
    @DisplayName("CONT-G-200 OK line -> ContabilidadTipo insert with sequence id, Condicionado S, Estado V, IdEmpresa from input")
    void contG200_insertComprobante() {
        // legacy CargarCabeceraComprobante.java:141-156, 193
        run(line("OK", "CMB1", 331, null));
        assertThat(jdbc.executedParamSets(INS_CMP)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdTipoContabilidad", 7001L).containsEntry("Nombre", "Comprobante CMB1").containsEntry("Nemo", "CMB1")
                .containsEntry("Tipo", "T").containsEntry("Reversa", "N").containsEntry("Agrupa", "S").containsEntry("Condicionado", "S")
                .containsEntry("TipoGeneracion", "A").containsEntry("IdEmpresa", 1L).containsEntry("Ejercicio", 2024L)
                .containsEntry("Formato", "F1").containsEntry("Estado", "V"));
    }

    @Test
    @DisplayName("CONT-G-201 CCCT_CENTRALIZACION > 0 -> ContabilidadTipoCentralizacionTipo row (Estado V)")
    void contG201_centralizacion() {
        // legacy CargarCabeceraComprobante.java:158-164, 194-196
        run(line("OK", "CMB1", 331, null));
        assertThat(jdbc.executedParamSets(INS_CEN)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdTipoCentralizacion", 331L).containsEntry("IdTipoContabilidad", 7001L).containsEntry("Estado", "V"));
    }

    @Test
    @DisplayName("CONT-G-202 CCCT_CENTRALIZACION 0 or null -> no centralizacion row")
    void contG202_sinCentralizacion() {
        // legacy CargarCabeceraComprobante.java:158
        run(line("OK", "CMB1", 0, null), line("OK", "CMB2", null, null));
        assertThat(jdbc.executions(INS_CMP)).isEqualTo(2);
        assertThat(jdbc.executions(INS_CEN)).isZero();
    }

    @Test
    @DisplayName("CONT-G-203 CCCT_GLOSA 'Folio|Rut|Nemo' -> 3 ContabilidadTipoGlosa rows, Orden 1..3, Origen DAT, IdDefinicionAsiento 0")
    void contG203_glosa() {
        // legacy CargarCabeceraComprobante.java:166-182, 197-199
        run(line("OK", "CMB1", 0, "Folio|Rut|Nemo"));
        List<Map<String, Object>> g = jdbc.executedParamSets(INS_GLO);
        assertThat(g).extracting(p -> p.get("IdTipoDatoEvento")).containsExactly("Folio", "Rut", "Nemo");
        assertThat(g).extracting(p -> p.get("Orden")).containsExactly(1L, 2L, 3L);
        assertThat(g).allSatisfy(p -> assertThat(p).containsEntry("IdTipoContabilidad", 7001L).containsEntry("IdDefinicionAsiento", 0L)
                .containsEntry("Origen", "DAT").containsEntry("GlosaCampo", "").containsEntry("NombreCampoMovimiento", "").containsEntry("Estado", "V"));
    }

    @Test
    @DisplayName("CONT-G-204 each OK line draws its own IdTipoContabilidad from the sequence (case-insensitive 'ok')")
    void contG204_secuenciaPorLinea() {
        // legacy CargarCabeceraComprobante.java:141 (equalsIgnoreCase), 143
        run(line("OK", "CMB1", 0, null), line("ok", "CMB2", 0, null));
        assertThat(jdbc.executedParamSets(INS_CMP)).extracting(p -> p.get("IdTipoContabilidad")).containsExactly(7001L, 7002L);
    }

    @Test
    @DisplayName("CONT-G-205 non-OK line -> ERROR ERROR_FORMATO_LINEA id 0, yet final counter 0 and P_OUT_MENSAJE UI_EXITO")
    void contG205_errorFormatoLineaReset() {
        // legacy CargarCabeceraComprobante.java:184-191 (counter/message overwritten after the loop)
        JsonObject out = run(line("ERROR EN COLUMNA", "CMB1", 0, null), line("OK", "CMB2", 0, null));
        assertThat(errores(out)).isZero();
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "ERROR_FORMATO_LINEA");
        assertThat(jdbc.executions(INS_CMP)).isEqualTo(1);
    }

    @Test
    @DisplayName("CONT-G-206 database failure propagates (no swallowed UI_ERROR response)")
    void contG206_failurePropagates() {
        // legacy CargarCabeceraComprobante.java:202-205 (catch, log, throw e)
        jdbc.failOn(INS_CMP);
        assertThatThrownBy(() -> run(line("OK", "CMB1", 0, null))).isInstanceOf(RuntimeException.class);
    }
}
