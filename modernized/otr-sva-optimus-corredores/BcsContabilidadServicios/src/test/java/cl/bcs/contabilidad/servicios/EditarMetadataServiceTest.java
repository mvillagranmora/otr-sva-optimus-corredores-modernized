package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.AgregarCuentasPorMetadataService;
import cl.bcs.contabilidad.service.EditarMetadataService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/** Characterization of legacy cl.bcs.contabilidad.EditarMetadata (EditarMetadata.java). */
class EditarMetadataServiceTest {

    static final String UPD = "EditarMetadata_UpdateMetadataCuentaContable.sql";
    static final String INS = "EditarMetadata_InsertMetadataFiltros.sql";
    static final String SEL_FIL = "EditarMetadata_SelectMetadataFiltros.sql";
    static final String DEL = "EditarMetadata_BorrarMetadataFiltros.sql";
    static final String SEL_CMP = "EditarMetadata_SelectComprobante.sql";
    static final String SEL_EVT = "EditarMetadata_SelectEventoComprobante.sql";
    static final String ACM_FILTROS = "AgregarCuentasPorMetadata_SelectCuentaFiltros.sql";
    static final String ACM_ORDEN = "AgregarCuentasPorMetadata_SelectUltimoOrden.sql";
    static final String ACM_INS_ASI = "AgregarCuentasPorMetadata_InsertContabilidadDefinicionAsiento.sql";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .sequence("BSCL.Contabilidad.BD.Secuencias::IdDefinicionAsiento", 500, "Id")
            .rows(SEL_FIL,
                    row("IdMetadataFiltros", 1L, "IdMetadataCuentaContable", 13L, "CuentaOptimus", "C1", "IdTipoDatoEvento", "Bolsa", "Condicion", "=", "Valor", "BCS"),
                    row("IdMetadataFiltros", 2L, "IdMetadataCuentaContable", 13L, "CuentaOptimus", "C1", "IdTipoDatoEvento", "Moneda", "Condicion", "=", "Valor", "CLP"),
                    row("IdMetadataFiltros", 3L, "IdMetadataCuentaContable", 13L, "CuentaOptimus", "C2", "IdTipoDatoEvento", "Bolsa", "Condicion", "=", "Valor", "BEC"))
            .rows(SEL_CMP, row("IdTipoContabilidad", 7001L, "DebeHaber", "D", "Ejercicio", 2024L, "Formato", "F1"))
            .rows(SEL_EVT, row("IdTipoEvento", "EVT_A", "Operador", "+"))
            .rows(ACM_ORDEN, row("Max", 9L));

    static final String FILTROS = "[{\"CuentaOptimus\":\"c1\",\"IdTipoDatoEvento\":\"BOLSA\",\"Condicion\":\"=\",\"Valor\":\"NUEVO\"},"
            + "{\"CuentaOptimus\":\"C1\",\"IdTipoDatoEvento\":\"Agente\",\"Condicion\":\"=\",\"Valor\":\"X\"},"
            + "{\"CuentaOptimus\":\"C3\",\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BCS\"}]";

    JsonObject run(String filtros) {
        AgregarCuentasPorMetadataService agregar = AgregarCuentasPorMetadataServiceTest.svc(jdbc);
        EditarMetadataService svc = LegacySql.injectBlank(new EditarMetadataService(jdbc, agregar),
                "SQL_UPDATE_METADATA=" + UPD, "SQL_INSERT_FILTROS=" + INS, "SQL_SELECT_FILTROS=" + SEL_FIL,
                "SQL_BORRAR_FILTROS=" + DEL, "SQL_SELECT_COMPROBANTE=" + SEL_CMP, "SQL_SELECT_EVENTO=" + SEL_EVT);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdMetadataCuentaContable\":13,"
                + "\"p_DescripcionMetadata\":\"TEST 68\",\"p_Filtros\":" + filtros + "}"));
    }

    @Test
    @DisplayName("CONT-G-300 statements in order: read filters, UPDATE description, INSERT new filters, DELETE removed filters")
    void contG300_orden() {
        // legacy EditarMetadata.java:115, 166-168
        run(FILTROS);
        assertThat(jdbc.single(SEL_FIL).params()).containsEntry("IdMetadataCuentaContable", 13L);
        assertThat(jdbc.executedParamSets(UPD)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdMetadataCuentaContable", 13L).containsEntry("DescripcionMetadata", "TEST 68"));
        assertThat(jdbc.executedInOrder(SEL_FIL, UPD, INS, DEL)).as("order %s", jdbc.executedFilesInOrder()).isTrue();
    }

    @Test
    @DisplayName("CONT-G-301 inserts only (cuenta, tipoDato) pairs not already present (case-insensitive); value changes are NOT updated")
    void contG301_inserta() {
        // legacy EditarMetadata.java:121-140 (existe compares CuentaOptimus+IdTipoDatoEvento only)
        run(FILTROS);
        assertThat(jdbc.executedParamSets(INS)).extracting(p -> p.get("CuentaOptimus") + "/" + p.get("IdTipoDatoEvento") + "/" + p.get("Valor"))
                .containsExactly("C1/Agente/X", "C3/Bolsa/BCS");
        assertThat(jdbc.executedParamSets(INS)).allSatisfy(p -> assertThat(p).containsEntry("IdMetadataCuentaContable", 13L));
    }

    @Test
    @DisplayName("CONT-G-302 existing filters absent from the input are deleted by :IdMetadataFiltros")
    void contG302_borra() {
        // legacy EditarMetadata.java:153-163, 168
        run(FILTROS);
        assertThat(jdbc.executedParamSets(DEL)).extracting(p -> p.get("IdMetadataFiltros")).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("CONT-G-303 new account -> for each comprobante using the metadata, AgregarCuentasPorMetadata with ONLY the new-account filters")
    void contG303_cuentaNueva() {
        // legacy EditarMetadata.java:170-201 -> AgregarCuentasPorMetadata.java:143-176
        run(FILTROS);
        assertThat(jdbc.single(SEL_CMP).params()).containsEntry("IdMetadataCuentaContable", 13L);
        assertThat(jdbc.single(SEL_EVT).params()).containsEntry("IdMetadataCuentaContable", 13L).containsEntry("IdTipoContabilidad", 7001L);
        assertThat(jdbc.callsTo(ACM_FILTROS)).isEmpty();
        assertThat(jdbc.executedParamSets(ACM_INS_ASI)).singleElement().satisfies(p -> assertThat(p)
                .containsEntry("IdCuentaContable", "C3").containsEntry("IdTipoContabilidad", 7001L).containsEntry("DebeHaber", "D")
                .containsEntry("Ejercicio", "2024").containsEntry("Formato", "F1").containsEntry("IdMetadataCuentaContable", 13L)
                .containsEntry("Orden", 10L).containsEntry("TipoEvento", "S"));
    }

    @Test
    @DisplayName("CONT-G-304 no new account -> comprobantes not queried; output UI_EXITO / 0 without P_OUT_MENSAJES")
    void contG304_sinCuentaNueva() {
        // legacy EditarMetadata.java:52-58, 170, 204-205
        JsonObject out = run("[{\"CuentaOptimus\":\"C1\",\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BCS\"}]");
        assertThat(jdbc.callsTo(SEL_CMP)).isEmpty();
        assertThat(errores(out)).isZero();
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertThat(out.has("P_OUT_MENSAJES")).isFalse();
    }

    @Test
    @DisplayName("CONT-G-305 database failure is caught -> 1 / UI_ERROR")
    void contG305_failure() {
        // legacy EditarMetadata.java:207-211
        jdbc.failOn(UPD);
        JsonObject out = run(FILTROS);
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_ERROR");
        assertThat(List.of(jdbc.executions(INS), jdbc.executions(DEL))).containsOnly(0);
    }
}
