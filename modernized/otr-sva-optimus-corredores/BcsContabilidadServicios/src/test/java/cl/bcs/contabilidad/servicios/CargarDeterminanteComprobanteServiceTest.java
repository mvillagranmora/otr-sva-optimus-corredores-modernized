package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.CargarDeterminanteComprobanteService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Characterization of legacy cl.bcs.contabilidad.CargarDeterminanteComprobante (CargarDeterminanteComprobante.java). */
class CargarDeterminanteComprobanteServiceTest {

    static final String INS = "CargarDeterminanteComprobante_InsertDeterminante.sql";
    static final String SEL = "CargarDeterminanteComprobante_SelectCombrobante.sql";

    final RecordingJdbc jdbc = new RecordingJdbc().rows(SEL, row("IdTipoContabilidad", 7001L, "Nemo", "CMB1"));

    static String det(String validacion, String nemo, String tipoDato, String condicion, String valor) {
        return "{\"CDCT_NEMOTECNICO\":\"" + nemo + "\",\"CDCT_TIPODATO\":\"" + tipoDato + "\",\"CDCT_CONDICION\":\"" + condicion
                + "\",\"CDCT_VALOR\":\"" + valor + "\",\"MENSAJEVALIDACION\":\"" + validacion + "\"}";
    }

    JsonObject run(String... dets) {
        CargarDeterminanteComprobanteService svc = LegacySql.injectBlank(new CargarDeterminanteComprobanteService(jdbc),
                "SQL_INSERT_DETERMINANTE=" + INS, "SQL_SELECT_COMPROBANTE=" + SEL);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_Determinantes\":[" + String.join(",", dets) + "]}"));
    }

    @Test
    @DisplayName("CONT-G-240 comprobantes looked up by :nemos of OK lines and :IdEmpresa")
    void contG240_query() {
        // legacy CargarDeterminanteComprobante.java:103-112
        run(det("OK", "CMB1", "Bolsa", "=", "BCS"), det("X", "CMB2", "Bolsa", "=", "BCS"));
        assertThat(jdbc.single(SEL).params()).containsEntry("nemos", List.of("CMB1")).containsEntry("IdEmpresa", 1L);
    }

    @Test
    @DisplayName("CONT-G-241 OK line -> condicion row: trimmed tipo/valor, IdDefinicionAsiento 0, TipoCondicion C, Conector AND")
    void contG241_insert() {
        // legacy CargarDeterminanteComprobante.java:129-143, 161
        run(det("OK", "CMB1", " Bolsa ", "=", " BCS "));
        assertThat(jdbc.executedParamSets(INS)).singleElement().satisfies(p -> {
            assertThat(p).containsEntry("IdDefinicionAsiento", 0L).containsEntry("IdTipoContabilidad", 7001L)
                    .containsEntry("IdTipoDatoEvento", "Bolsa").containsEntry("ValorVarchar", "BCS").containsEntry("Condicion", "=")
                    .containsEntry("Conector", "AND").containsEntry("TipoCondicion", "C").containsEntry("Estado", "V");
            assertThat((BigDecimal) p.get("ValorDecimal")).isEqualByComparingTo("0");
        });
    }

    @Test
    @DisplayName("CONT-G-242 ValorDecimal parsed from the UNtrimmed value: '100.50' -> 100.50, ' 100' -> 0")
    void contG242_valorDecimal() {
        // legacy CargarDeterminanteComprobante.java:131-137 (trim only for ValorVarchar)
        run(det("OK", "CMB1", "Monto", ">", "100.50"), det("OK", "CMB1", "Monto", "<", " 100"));
        assertThat((BigDecimal) jdbc.executedAt(INS, 0).get("ValorDecimal")).isEqualByComparingTo("100.50");
        assertThat(jdbc.executedAt(INS, 1)).containsEntry("ValorVarchar", "100");
        assertThat((BigDecimal) jdbc.executedAt(INS, 1).get("ValorDecimal")).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("CONT-G-243 condicion outside = > < <> -> ADVERTENCIA CONDICION_NO_VALIDA_DETERMINANTE, not inserted")
    void contG243_condicionInvalida() {
        // legacy CargarDeterminanteComprobante.java:142-150
        JsonObject out = run(det("OK", "CMB1", "Bolsa", "LIKE", "BCS"));
        assertThat(jdbc.executions(INS)).isZero();
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "CONDICION_NO_VALIDA_DETERMINANTE");
    }

    @Test
    @DisplayName("CONT-G-244 unknown nemo -> ADVERTENCIA NO_EXISTE_CONTABILIDAD; nemo match is case-insensitive")
    void contG244_nemo() {
        // legacy CargarDeterminanteComprobante.java:116-128
        JsonObject out = run(det("OK", "OTRO", "Bolsa", "=", "BCS"), det("OK", "cmb1", "Bolsa", "=", "BCS"));
        assertThat(codigos(out)).containsExactly("NO_EXISTE_CONTABILIDAD");
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "NO_EXISTE_CONTABILIDAD");
        assertThat(jdbc.executedParamSets(INS)).singleElement().satisfies(p -> assertThat(p).containsEntry("IdTipoContabilidad", 7001L));
    }

    @Test
    @DisplayName("CONT-G-245 non-OK line -> ERROR ERROR_FORMATO_LINEA but final counter 0 and UI_EXITO")
    void contG245_formatoLinea() {
        // legacy CargarDeterminanteComprobante.java:153-160
        JsonObject out = run(det("MAL", "CMB1", "Bolsa", "=", "BCS"));
        assertThat(errores(out)).isZero();
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "ERROR_FORMATO_LINEA");
    }

    @Test
    @DisplayName("CONT-G-246 database failure propagates")
    void contG246_failurePropagates() {
        // legacy CargarDeterminanteComprobante.java:162-165
        jdbc.failOn(INS);
        assertThatThrownBy(() -> run(det("OK", "CMB1", "Bolsa", "=", "BCS"))).isInstanceOf(RuntimeException.class);
    }
}
