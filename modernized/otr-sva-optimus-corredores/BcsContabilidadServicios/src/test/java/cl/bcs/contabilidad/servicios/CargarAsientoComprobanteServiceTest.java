package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.CargarAsientoComprobanteService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Characterization of legacy cl.bcs.contabilidad.CargarAsientoComprobante (CargarAsientoComprobante.java). */
class CargarAsientoComprobanteServiceTest {

    static final String SEL_CMP = "CargarAsientoComprobante_SelectCombrobante.sql";
    static final String SEL_ID = "CargarAsientoComprobante_SelectIdCuentaContable.sql";
    static final String SEL_META = "CargarAsientoComprobante_SelectMetadata.sql";
    static final String INS_ASI = "CargarAsientoComprobante_InsertContabilidadDefinicionAsiento.sql";
    static final String INS_EVT = "CargarAsientoComprobante_InsertContabilidadDefinicionTipoEvento.sql";
    static final String INS_CON = "CargarAsientoComprobante_InsertContabilidadTipoAsientoCondicion.sql";
    static final String ACM_FILTROS = "AgregarCuentasPorMetadata_SelectCuentaFiltros.sql";
    static final String ACM_ORDEN = "AgregarCuentasPorMetadata_SelectUltimoOrden.sql";
    static final String ACM_INS_ASI = "AgregarCuentasPorMetadata_InsertContabilidadDefinicionAsiento.sql";
    static final String ACM_INS_EVT = "AgregarCuentasPorMetadata_InsertContabilidadDefinicionTipoEvento.sql";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .sequence("BSCL.Contabilidad.BD.Secuencias::IdDefinicionAsiento", 500, "Id")
            .rows(SEL_CMP, row("IdTipoContabilidad", 7001L, "Nemo", "CMB1", "Ejercicio", 2024L));

    static String asiento(String validacion, String nemo, String cuenta, String eventos, String condiciones) {
        return "{\"CACT_CONDICIONES\":" + q(condiciones) + ",\"CACT_CUENTAOMETADATA\":" + q(cuenta) + ",\"CACT_DEBEHABER\":\"D\","
                + "\"CACT_EVENTOS\":" + q(eventos) + ",\"CACT_FORMATO\":\"F1\",\"CACT_NEMOTECNICO\":" + q(nemo)
                + ",\"MENSAJEVALIDACION\":" + q(validacion) + "}";
    }

    static String q(String s) { return s == null ? "null" : "\"" + s + "\""; }

    JsonObject run(String... asientos) {
        CargarAsientoComprobanteService svc = LegacySql.injectBlank(new CargarAsientoComprobanteService(jdbc),
                "SQL_SELECT_COMPROBANTE=" + SEL_CMP, "SQL_SELECT_ID_CUENTA=" + SEL_ID, "SQL_SELECT_METADATA=" + SEL_META,
                "SQL_INSERT_ASIENTO=" + INS_ASI, "SQL_INSERT_TIPO_EVENTO=" + INS_EVT, "SQL_INSERT_ASIENTO_COND=" + INS_CON);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_Asientos\":[" + String.join(",", asientos) + "]}"));
    }

    @Test
    @DisplayName("CONT-G-220 comprobantes looked up by :nemos (OK lines) and :IdEmpresa")
    void contG220_queryComprobantes() {
        // legacy CargarAsientoComprobante.java:145-154
        run(asiento("OK", "CMB1", "110101001", "+EVT_A", null), asiento("NOK", "CMB9", "1", "+E", null));
        assertThat(jdbc.single(SEL_CMP).params()).containsEntry("nemos", List.of("CMB1")).containsEntry("IdEmpresa", 1L);
    }

    @Test
    @DisplayName("CONT-G-221 direct account -> DefinicionAsiento row (sequence id, Ejercicio from comprobante, TipoEvento C for 2 events)")
    void contG221_definicionAsiento() {
        // legacy CargarAsientoComprobante.java:222-253, 311
        run(asiento("OK", "CMB1", "110101001", "+EVT_A|-EVT_B", null));
        assertThat(jdbc.executedParamSets(INS_ASI)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdDefinicionAsiento", 500L).containsEntry("IdTipoContabilidad", 7001L)
                .containsEntry("IdCuentaContable", "110101001").containsEntry("Ejercicio", 2024L).containsEntry("Orden", 1L)
                .containsEntry("DebeHaber", "D").containsEntry("TipoEvento", "C").containsEntry("Condicionado", "S")
                .containsEntry("OmiteCondicionCmbte", "N").containsEntry("Estado", "V").containsEntry("Formato", "F1"));
    }

    @Test
    @DisplayName("CONT-G-222 CACT_EVENTOS '+EVT_A|-EVT_B': operator = first char, event = rest trimmed")
    void contG222_eventos() {
        // legacy CargarAsientoComprobante.java:254-266, 313
        run(asiento("OK", "CMB1", "110101001", "+EVT_A|- EVT_B", null));
        List<Map<String, Object>> e = jdbc.executedParamSets(INS_EVT);
        assertThat(e).extracting(p -> p.get("IdTipoEvento")).containsExactly("EVT_A", "EVT_B");
        assertThat(e).extracting(p -> p.get("Operador")).containsExactly("+", "-");
        assertThat(e).allSatisfy(p -> assertThat(p).containsEntry("IdDefinicionAsiento", 500L).containsEntry("Estado", "V"));
    }

    @Test
    @DisplayName("CONT-G-223 duplicated event in a line -> inserted once + ADVERTENCIA YA_EXISTE_EVENTO_ASIENTO")
    void contG223_eventoDuplicado() {
        // legacy CargarAsientoComprobante.java:263-271
        JsonObject out = run(asiento("OK", "CMB1", "110101001", "+EVT_A|-EVT_A", null));
        assertThat(jdbc.executedParamSets(INS_EVT)).hasSize(1);
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "YA_EXISTE_EVENTO_ASIENTO");
    }

    @Test
    @DisplayName("CONT-G-224 Orden increments per nemotecnico (1, 2) and each line takes a new IdDefinicionAsiento")
    void contG224_ordenPorNemo() {
        // legacy CargarAsientoComprobante.java:224-225, 241-246
        run(asiento("OK", "CMB1", "110101001", "+EVT_A", null), asiento("OK", "CMB1", "110101002", "+EVT_A", null));
        List<Map<String, Object>> a = jdbc.executedParamSets(INS_ASI);
        assertThat(a).extracting(p -> p.get("Orden")).containsExactly(1L, 2L);
        assertThat(a).extracting(p -> p.get("IdDefinicionAsiento")).containsExactly(500L, 501L);
        assertThat(a).extracting(p -> p.get("TipoEvento")).containsExactly("S", "S");
    }

    @Test
    @DisplayName("CONT-G-225 CACT_CONDICIONES split by '|' then '#'(3 parts); invalid operator warned; incomplete ignored")
    void contG225_condiciones() {
        // legacy CargarAsientoComprobante.java:275-306, 315-317
        JsonObject out = run(asiento("OK", "CMB1", "110101001", "+EVT_A", "Bolsa#=#BCS|Monto#>#100|Bad#~#1|incompleta"));
        assertThat(jdbc.executedParamSets(INS_CON)).hasSize(2);
        List<Map<String, Object>> c = jdbc.executedParamSets(INS_CON);
        assertThat(c.get(0)).containsEntry("IdDefinicionAsiento", 500L).containsEntry("IdTipoDatoEvento", "Bolsa").containsEntry("Condicion", "=")
                .containsEntry("ValorVarchar", "BCS").containsEntry("Conector", "AND").containsEntry("TipoCondicion", "A").containsEntry("Estado", "V");
        assertThat((BigDecimal) c.get(0).get("ValorDecimal")).isEqualByComparingTo("0");
        assertThat(c.get(1)).containsEntry("IdTipoDatoEvento", "Monto").containsEntry("Condicion", ">").containsEntry("ValorVarchar", "100");
        assertThat((BigDecimal) c.get(1).get("ValorDecimal")).isEqualByComparingTo("100");
        assertThat(codigos(out)).containsExactly("CONDICION_NO_VALIDA_DETERMINANTE_ASIENTO");
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "CONDICION_NO_VALIDA_DETERMINANTE_ASIENTO");
    }

    @Test
    @DisplayName("CONT-G-226 unknown nemotecnico -> ADVERTENCIA NO_EXISTE_CONTABILIDAD, nothing inserted for that line")
    void contG226_noExisteContabilidad() {
        // legacy CargarAsientoComprobante.java:226-239
        JsonObject out = run(asiento("OK", "OTRO", "110101001", "+EVT_A", null));
        assertThat(jdbc.executions(INS_ASI)).isZero();
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "NO_EXISTE_CONTABILIDAD");
    }

    @Test
    @DisplayName("CONT-G-227 non-OK lines are skipped silently (no ERROR_FORMATO_LINEA), output UI_EXITO / 0")
    void contG227_noOkSilencioso() {
        // legacy CargarAsientoComprobante.java:156-163 (no else branch for non-OK lines)
        JsonObject out = run(asiento("ERROR", "CMB1", "110101001", "+EVT_A", null));
        assertThat(mensajes(out)).isEmpty();
        assertThat(errores(out)).isZero();
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertThat(jdbc.executions(INS_ASI)).isZero();
    }

    @Test
    @DisplayName("CONT-G-228 '&META1&' -> metadata looked up by :CodigoMetadata 'META1'; missing -> ADVERTENCIA NO_EXISTE_METADATA")
    void contG228_metadataNoExiste() {
        // legacy CargarAsientoComprobante.java:164-177
        JsonObject out = run(asiento("OK", "CMB1", "&META1&", "+EVT_A", null));
        assertThat(jdbc.single(SEL_META).params()).containsEntry("CodigoMetadata", "META1");
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "NO_EXISTE_METADATA");
        assertThat(jdbc.executions(INS_ASI)).isZero();
    }

    @Test
    @DisplayName("CONT-G-229 existing metadata -> delegates to AgregarCuentasPorMetadata (filters of metadata, Orden = max+1)")
    void contG229_metadataDelegaAgregarCuentas() {
        // legacy CargarAsientoComprobante.java:178-219 -> AgregarCuentasPorMetadata.java:143-218
        jdbc.rows(SEL_META, row("Id", 33L))
            .rows(ACM_FILTROS, row("CuentaOptimus", "110201", "IdTipoDatoEvento", "Bolsa", "Condicion", "=", "Valor", "BCS"))
            .rows(ACM_ORDEN, row("Max", 4L));
        run(asiento("OK", "CMB1", "&META1&", "+EVT_A", null));
        assertThat(jdbc.single(ACM_FILTROS).params()).containsEntry("IdMetadataCuentaContable", 33L);
        assertThat(jdbc.executedParamSets(ACM_INS_ASI)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdCuentaContable", "110201").containsEntry("IdMetadataCuentaContable", 33L)
                .containsEntry("IdTipoContabilidad", 7001L).containsEntry("Ejercicio", "2024").containsEntry("Orden", 5L)
                .containsEntry("DebeHaber", "D").containsEntry("Formato", "F1").containsEntry("TipoEvento", "S"));
        assertThat(jdbc.executedParamSets(ACM_INS_EVT)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdTipoEvento", "EVT_A").containsEntry("Operador", "+"));
        assertThat(jdbc.executions(INS_ASI)).isZero();
    }

    @Test
    @DisplayName("CONT-G-230 database failure propagates")
    void contG230_failurePropagates() {
        // legacy CargarAsientoComprobante.java:320-323
        jdbc.failOn(INS_ASI);
        assertThatThrownBy(() -> run(asiento("OK", "CMB1", "110101001", "+EVT_A", null))).isInstanceOf(RuntimeException.class);
    }
}
