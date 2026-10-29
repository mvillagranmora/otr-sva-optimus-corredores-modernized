package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.AgregarMetadataService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static cl.bcs.contabilidad.servicios.Out.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Characterization of legacy cl.bcs.contabilidad.AgregarMetadata (AgregarMetadata.java). */
class AgregarMetadataServiceTest {

    static final String SEL_ID = "AgregarMetadata_SelectMetadataCuentaContable.sql";
    static final String INS_META = "AgregarMetadata_InsertMetadataCuentaContable.sql";
    static final String INS_FIL = "AgregarMetadata_InsertMetadataFiltros.sql";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .sequence("BSCL.Contabilidad.BD.Secuencias::IdMetadataCuentaContable", 13, "IdMetadataCuentaContable");

    JsonObject run(String filtros) {
        AgregarMetadataService svc = LegacySql.injectBlank(new AgregarMetadataService(jdbc),
                "SQL_SELECT_ID_METADATA=" + SEL_ID, "SQL_INSERT_METADATA=" + INS_META, "SQL_INSERT_FILTROS=" + INS_FIL);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_CodigoMetadata\":\"PRUEBA\","
                + "\"p_DescripcionMetadata\":\"DESCRIPCION PRUEBA\",\"p_Filtros\":" + filtros + "}"));
    }

    static String filtro(String cuenta, String tipo, String cond, String valor) {
        return "{\"CuentaOptimus\":\"" + cuenta + "\",\"IdTipoDatoEvento\":\"" + tipo + "\",\"Condicion\":\"" + cond + "\",\"Valor\":\"" + valor + "\"}";
    }

    @Test
    @DisplayName("CONT-G-280 MetadataCuentaContable inserted with sequence id, p_CodigoMetadata and p_DescripcionMetadata")
    void contG280_insertMetadata() {
        // legacy AgregarMetadata.java:94-102, 122
        run("[]");
        assertThat(jdbc.executedParamSets(INS_META)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdMetadataCuentaContable", 13L).containsEntry("CodigoMetadata", "PRUEBA")
                .containsEntry("DescripcionMetadata", "DESCRIPCION PRUEBA"));
    }

    @ParameterizedTest(name = "condicion {0}")
    @ValueSource(strings = {"=", ">", "<", "<>"})
    @DisplayName("CONT-G-281 valid filter -> MetadataFiltros row with metadata id and filter fields")
    void contG281_filtroValido(String cond) {
        // legacy AgregarMetadata.java:104-112, 123
        run("[" + filtro("110101001001", "Agente", cond, "WEWE") + "]");
        assertThat(jdbc.executedParamSets(INS_FIL)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdMetadataCuentaContable", 13L).containsEntry("CuentaOptimus", "110101001001")
                .containsEntry("IdTipoDatoEvento", "Agente").containsEntry("Condicion", cond).containsEntry("Valor", "WEWE"));
    }

    @Test
    @DisplayName("CONT-G-282 invalid condicion -> ADVERTENCIA CONDICION_NO_VALIDA_ASIENTO id 0, filter skipped, still UI_EXITO / 0")
    void contG282_condicionInvalida() {
        // legacy AgregarMetadata.java:113-118, 124-125
        JsonObject out = run("[" + filtro("1", "Bolsa", "LIKE", "X") + "," + filtro("2", "Bolsa", "=", "BCS") + "]");
        assertThat(errores(out)).isZero();
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 0L, "ADVERTENCIA", "CONDICION_NO_VALIDA_ASIENTO");
        assertThat(jdbc.executedParamSets(INS_FIL)).extracting(p -> p.get("CuentaOptimus")).containsExactly("2");
    }

    @Test
    @DisplayName("CONT-G-283 database failure is caught -> 1 / UI_ERROR")
    void contG283_failure() {
        // legacy AgregarMetadata.java:127-131
        jdbc.failOn(INS_META);
        JsonObject out = run("[]");
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_ERROR");
    }
}
