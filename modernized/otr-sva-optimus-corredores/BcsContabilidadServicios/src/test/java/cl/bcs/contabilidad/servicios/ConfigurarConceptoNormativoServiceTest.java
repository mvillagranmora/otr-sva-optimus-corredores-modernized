package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.ConfigurarConceptoNormativoService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy cl.bcs.contabilidad.ConfigurarConceptoNormativo (ConfigurarConceptoNormativo.java).
 * NOTE the legacy input keys are NOT p_-prefixed: IdConceptoNormativo, IdEmpresa, condiciones (L54-58).
 */
@SuppressWarnings("unchecked")
class ConfigurarConceptoNormativoServiceTest {

    static final String SEL_EXIST = "ConfigurarConceptoNormativo_Select_CondicionConceptoExistentes.sql";
    static final String SEL_CONCEPTO = "ConfigurarConceptoNormativo_SelectConceptoNormativo.sql";
    static final String SEL_TIPOS = "ConfigurarConceptoNormativo_SelectTipoDatoEvento.sql";
    static final String SEL_VALORES = "ConfigurarConceptoNormativo_SelectTipoDatoValorValidos.sql";
    static final String INS = "ConfigurarConceptoNormativo_InsCondicionConcepto.sql";
    static final String UPD = "ConfigurarConceptoNormativo_UpdateCondicionConcepto.sql";
    static final String DEL = "ConfigurarConceptoNormativo_DeleteCondicionConcepto.sql";

    static final String CONDICIONES = "[{\"IdCondicion\":null,\"IdTipoDatoEvento\":\"TipoComprobantes\",\"ValorVarchar\":\"FAE\",\"OperadorFuncion\":\"+\"},"
            + "{\"IdCondicion\":10,\"IdTipoDatoEvento\":\"TipoInstrumento\",\"ValorVarchar\":\"ACX\",\"OperadorFuncion\":\"-\"}]";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .rows(SEL_CONCEPTO, row("1", 1L))
            .rows(SEL_EXIST, row("IdCondicion", 10L, "ValorVarchar", "TipoInstrumento", "OperadorFuncion", "-"),
                    row("IdCondicion", 11L, "ValorVarchar", "TipoComprobantes", "OperadorFuncion", "+"))
            .rows(SEL_TIPOS, row("IdTipoDatoEvento", "TipoComprobantes"), row("IdTipoDatoEvento", "TipoInstrumento"))
            .rows(SEL_VALORES, row("IdTipoDatoEvento", "TipoComprobantes", "ValorVarchar", "FAE"),
                    row("IdTipoDatoEvento", "TipoInstrumento", "ValorVarchar", "ACX"));

    JsonObject run(String condiciones) {
        ConfigurarConceptoNormativoService svc = LegacySql.injectBlank(new ConfigurarConceptoNormativoService(jdbc),
                "SQL_SELECT_CONCEPTO=" + SEL_CONCEPTO, "SQL_SELECT_CONDICIONES=" + SEL_EXIST, "SQL_SELECT_TIPO_EVENTO=" + SEL_TIPOS,
                "SQL_SELECT_TIPO_VALOR=" + SEL_VALORES, "SQL_INSERT_CONDICION=" + INS, "SQL_UPDATE_CONDICION=" + UPD, "SQL_DELETE_CONDICION=" + DEL);
        return svc.execute(json("{\"IdConceptoNormativo\":1,\"IdEmpresa\":1,\"condiciones\":" + condiciones + "}"));
    }

    void noWrites() {
        assertThat(jdbc.executions(INS) + jdbc.executions(UPD) + jdbc.executions(DEL)).isZero();
    }

    @Test
    @DisplayName("CONT-G-340 concept missing/inactive (count 0) -> ERROR CONCEPTO_NO_EXISTE (descripcion 'CONCEPTO_NO_EXISTE'), empty result lists")
    void contG340_conceptoNoExiste() {
        // legacy ConfigurarConceptoNormativo.java:81-95 (countQuery on SelectConceptoNormativo)
        jdbc.rows(SEL_CONCEPTO, List.of());
        JsonObject out = run(CONDICIONES);
        assertThat(errores(out)).isEqualTo(1L);
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "CONCEPTO_NO_EXISTE");
        assertThat(str(mensajes(out).get(0), "descripcion")).isEqualTo("CONCEPTO_NO_EXISTE");
        assertThat(array(out, "P_OUT_INSERTADOS")).isEmpty();
        assertThat(array(out, "P_OUT_ACTUALIZADOS")).isEmpty();
        assertThat(out.getAsJsonArray("P_OUT_ELIMINADOS")).isEmpty();
        noWrites();
    }

    @Test
    @DisplayName("CONT-G-341 validation queries bind :tipos and :valores built from the incoming conditions")
    void contG341_validacionParams() {
        // legacy ConfigurarConceptoNormativo.java:88-114
        run(CONDICIONES);
        assertThat(jdbc.executedAt(SEL_CONCEPTO, 0)).containsEntry("IdConceptoNormativo", 1L);
        assertThat((List<Object>) jdbc.single(SEL_TIPOS).params().get("tipos")).containsExactlyInAnyOrder("TipoComprobantes", "TipoInstrumento");
        assertThat((List<Object>) jdbc.single(SEL_VALORES).params().get("tipos")).containsExactlyInAnyOrder("TipoComprobantes", "TipoInstrumento");
        assertThat((List<Object>) jdbc.single(SEL_VALORES).params().get("valores")).containsExactlyInAnyOrder("FAE", "ACX");
    }

    @Test
    @DisplayName("CONT-G-342 unknown tipo -> ERROR TIPO_EVENTO_NO_EXISTE per condition, nothing written")
    void contG342_tipoNoExiste() {
        // legacy ConfigurarConceptoNormativo.java:127-133, 161-163
        jdbc.rows(SEL_TIPOS, row("IdTipoDatoEvento", "TipoComprobantes"));
        JsonObject out = run(CONDICIONES);
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(codigos(out)).containsExactly("TIPO_EVENTO_NO_EXISTE");
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "TIPO_EVENTO_NO_EXISTE");
        noWrites();
    }

    @Test
    @DisplayName("CONT-G-343 tipo valid but (tipo, valor) not valid -> ERROR TIPO_EVENTO_VALOR_INVALIDO, nothing written")
    void contG343_valorInvalido() {
        // legacy ConfigurarConceptoNormativo.java:134-141, 161-163
        jdbc.rows(SEL_VALORES, row("IdTipoDatoEvento", "TipoComprobantes", "ValorVarchar", "FAE"));
        JsonObject out = run(CONDICIONES);
        assertThat(errores(out)).isEqualTo(1L);
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "TIPO_EVENTO_VALOR_INVALIDO");
        noWrites();
    }

    @Test
    @DisplayName("CONT-G-344 IdCondicion null -> insert; known IdCondicion -> update; existing not sent -> delete; lists echoed")
    void contG344_sincroniza() {
        // legacy ConfigurarConceptoNormativo.java:143-176
        JsonObject out = run(CONDICIONES);
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).isEmpty();
        assertThat(jdbc.executedParamSets(INS)).singleElement().satisfies(p -> assertThat(p).containsEntry("IdConceptoNormativo", 1L)
                .containsEntry("IdTipoDatoEvento", "TipoComprobantes").containsEntry("ValorVarchar", "FAE").containsEntry("OperadorFuncion", "+"));
        assertThat(jdbc.executedParamSets(UPD)).singleElement().satisfies(p -> assertThat(p).containsEntry("IdCondicion", 10L)
                .containsEntry("IdConceptoNormativo", 1L).containsEntry("IdTipoDatoEvento", "TipoInstrumento")
                .containsEntry("ValorVarchar", "ACX").containsEntry("OperadorFuncion", "-"));
        assertThat(jdbc.executedParamSets(DEL)).singleElement().satisfies(p -> assertThat(p).containsEntry("IdCondicion", 11L));
        assertThat(array(out, "P_OUT_INSERTADOS")).singleElement().satisfies(o ->
                assertThat(str(o, "ValorVarchar")).isEqualTo("FAE"));
        assertThat(array(out, "P_OUT_ACTUALIZADOS")).singleElement().satisfies(o -> assertThat(lng(o, "IdCondicion")).isEqualTo(10L));
        assertThat(out.getAsJsonArray("P_OUT_ELIMINADOS").get(0).getAsLong()).isEqualTo(11L);
    }

    @Test
    @DisplayName("CONT-G-345 IdCondicion not among existing ones is inserted, not updated")
    void contG345_idDesconocido() {
        // legacy ConfigurarConceptoNormativo.java:145-146
        run("[{\"IdCondicion\":99,\"IdTipoDatoEvento\":\"TipoInstrumento\",\"ValorVarchar\":\"ACX\",\"OperadorFuncion\":\"-\"}]");
        assertThat(jdbc.executions(INS)).isEqualTo(1);
        assertThat(jdbc.executions(UPD)).isZero();
        assertThat(jdbc.executedParamSets(DEL)).extracting(p -> p.get("IdCondicion")).containsExactly(10L, 11L);
    }

    @Test
    @DisplayName("CONT-G-346 database failure is caught -> counter+1, ERROR with codigo \"\" and descripcion = exception text")
    void contG346_failure() {
        // legacy ConfigurarConceptoNormativo.java:178-181
        jdbc.failOn(SEL_EXIST);
        JsonObject out = run(CONDICIONES);
        assertThat(errores(out)).isEqualTo(1L);
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "");
        assertThat(str(mensajes(out).get(0), "descripcion")).isNotBlank();
    }
}
