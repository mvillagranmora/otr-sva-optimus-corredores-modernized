package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.AgregarCuentasPorMetadataService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/** Characterization of legacy cl.bcs.contabilidad.AgregarCuentasPorMetadata (AgregarCuentasPorMetadata.java). */
class AgregarCuentasPorMetadataServiceTest {

    static final String FILTROS = "AgregarCuentasPorMetadata_SelectCuentaFiltros.sql";
    static final String SEL_ID = "AgregarCuentasPorMetadata_SelectIdCuentaContable.sql";
    static final String ORDEN = "AgregarCuentasPorMetadata_SelectUltimoOrden.sql";
    static final String INS_ASI = "AgregarCuentasPorMetadata_InsertContabilidadDefinicionAsiento.sql";
    static final String INS_EVT = "AgregarCuentasPorMetadata_InsertContabilidadDefinicionTipoEvento.sql";
    static final String INS_CON = "AgregarCuentasPorMetadata_InsertContabilidadTipoAsientoCondicion.sql";

    static final String EVENTOS_2 = "[{\"IdTipoEvento\":\"EVT_A\",\"Operador\":\"+\"},{\"IdTipoEvento\":\"EVT_B\",\"Operador\":\"-\"}]";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .sequence("BSCL.Contabilidad.BD.Secuencias::IdDefinicionAsiento", 500, "Id")
            .rows(FILTROS,
                    row("CuentaOptimus", "110201", "IdTipoDatoEvento", "Bolsa", "Condicion", "=", "Valor", "BCS"),
                    row("CuentaOptimus", "110201", "IdTipoDatoEvento", "Moneda", "Condicion", "=", "Valor", "CLP"),
                    row("CuentaOptimus", "110202", "IdTipoDatoEvento", " Bolsa ", "Condicion", "<>", "Valor", " 12 "))
            .rows(ORDEN, row("Max", 4L));

    static AgregarCuentasPorMetadataService svc(RecordingJdbc jdbc) {
        return LegacySql.injectBlank(new AgregarCuentasPorMetadataService(jdbc),
                "SQL_CUENTAS_FILTROS=" + FILTROS, "SQL_SELECT_ID=" + SEL_ID, "SQL_SELECT_ORDEN=" + ORDEN,
                "SQL_INSERT_ASIENTO=" + INS_ASI, "SQL_INSERT_EVENTO=" + INS_EVT, "SQL_INSERT_CONDICION=" + INS_CON);
    }

    JsonObject run(String eventos, String extra) {
        return svc(jdbc).execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdMetadataCuentaContable\":33,"
                + "\"p_IdTipoContabilidad\":7001,\"p_DebeHaber\":\"H\",\"p_Ejercicio\":\"2024\",\"p_Formato\":\"F1\",\"p_Eventos\":" + eventos + extra + "}"));
    }

    @Test
    @DisplayName("CONT-G-260 without p_CuentasFiltros: filters read by :IdMetadataCuentaContable, max Orden by :IdTipoContabilidad")
    void contG260_queries() {
        // legacy AgregarCuentasPorMetadata.java:143-150
        run(EVENTOS_2, "");
        assertThat(jdbc.single(FILTROS).params()).containsEntry("IdMetadataCuentaContable", 33L);
        assertThat(jdbc.single(ORDEN).params()).containsEntry("IdTipoContabilidad", 7001L);
    }

    @Test
    @DisplayName("CONT-G-261 one DefinicionAsiento per DISTINCT CuentaOptimus, Orden = max + 1.., TipoEvento C for >1 events")
    void contG261_asientos() {
        // legacy AgregarCuentasPorMetadata.java:151-176, 216
        run(EVENTOS_2, "");
        List<Map<String, Object>> a = jdbc.executedParamSets(INS_ASI);
        assertThat(a).extracting(p -> p.get("IdCuentaContable")).containsExactly("110201", "110202");
        assertThat(a).extracting(p -> p.get("Orden")).containsExactly(5L, 6L);
        assertThat(a).extracting(p -> p.get("IdDefinicionAsiento")).containsExactly(500L, 501L);
        assertThat(a).allSatisfy(p -> assertThat(p).containsEntry("IdTipoContabilidad", 7001L).containsEntry("Ejercicio", "2024")
                .containsEntry("DebeHaber", "H").containsEntry("TipoEvento", "C").containsEntry("Condicionado", "S")
                .containsEntry("OmiteCondicionCmbte", "N").containsEntry("Estado", "V").containsEntry("Formato", "F1")
                .containsEntry("IdMetadataCuentaContable", 33L));
    }

    @Test
    @DisplayName("CONT-G-262 events = cross product, event-major order")
    void contG262_eventos() {
        // legacy AgregarCuentasPorMetadata.java:178-188, 217
        run(EVENTOS_2, "");
        List<Map<String, Object>> e = jdbc.executedParamSets(INS_EVT);
        assertThat(e).extracting(p -> p.get("IdTipoEvento") + "/" + p.get("Operador") + "/" + p.get("IdDefinicionAsiento"))
                .containsExactly("EVT_A/+/500", "EVT_A/+/501", "EVT_B/-/500", "EVT_B/-/501");
    }

    @Test
    @DisplayName("CONT-G-263 conditions per account from its filters: trimmed tipo/valor, ValorDecimal from untrimmed (0 if not numeric)")
    void contG263_condiciones() {
        // legacy AgregarCuentasPorMetadata.java:190-213, 218
        run(EVENTOS_2, "");
        List<Map<String, Object>> c = jdbc.executedParamSets(INS_CON);
        assertThat(c).extracting(p -> p.get("IdDefinicionAsiento") + "/" + p.get("IdTipoDatoEvento") + "/" + p.get("Condicion") + "/" + p.get("ValorVarchar"))
                .containsExactly("500/Bolsa/=/BCS", "500/Moneda/=/CLP", "501/Bolsa/<>/12");
        assertThat((BigDecimal) c.get(2).get("ValorDecimal")).isEqualByComparingTo("0");
        assertThat(c).allSatisfy(p -> assertThat(p).containsEntry("Conector", "AND").containsEntry("TipoCondicion", "A").containsEntry("Estado", "V"));
    }

    @Test
    @DisplayName("CONT-G-264 MAX(Orden) null -> Orden starts at 1; single event -> TipoEvento S")
    void contG264_maxNull() {
        // legacy AgregarCuentasPorMetadata.java:151, 166
        jdbc.rows(ORDEN, row("Max", null));
        run("[{\"IdTipoEvento\":\"EVT_A\",\"Operador\":\"+\"}]", "");
        List<Map<String, Object>> a = jdbc.executedParamSets(INS_ASI);
        assertThat(a).extracting(p -> p.get("Orden")).containsExactly(1L, 2L);
        assertThat(a).extracting(p -> p.get("TipoEvento")).containsOnly("S");
    }

    @Test
    @DisplayName("CONT-G-265 p_CuentasFiltros supplied -> metadata filters are NOT queried")
    void contG265_filtrosEntrada() {
        // legacy AgregarCuentasPorMetadata.java:143-147
        run(EVENTOS_2, ",\"p_CuentasFiltros\":[{\"CuentaOptimus\":\"C9\",\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BCS\"}]");
        assertThat(jdbc.callsTo(FILTROS)).isEmpty();
        assertThat(jdbc.executedParamSets(INS_ASI)).extracting(p -> p.get("IdCuentaContable")).containsExactly("C9");
    }

    @Test
    @DisplayName("CONT-G-266 output is {P_OUT_MENSAJE: UI_EXITO, P_OUT_ERRORESCONTADOR: 0} with no P_OUT_MENSAJES")
    void contG266_output() {
        // legacy AgregarCuentasPorMetadata.java:47-53, 139, 220
        JsonObject out = run(EVENTOS_2, "");
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertThat(errores(out)).isZero();
        assertThat(out.has("P_OUT_MENSAJES")).isFalse();
    }

    @Test
    @DisplayName("CONT-G-267 database failure is caught -> P_OUT_ERRORESCONTADOR 1, UI_ERROR (no exception)")
    void contG267_failure() {
        // legacy AgregarCuentasPorMetadata.java:221-224
        jdbc.failOn(INS_ASI);
        JsonObject out = run(EVENTOS_2, "");
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_ERROR");
    }
}
