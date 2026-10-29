package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.EliminarMetadataService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/** Characterization of legacy cl.bcs.contabilidad.EliminarMetadata (EliminarMetadata.java). */
class EliminarMetadataServiceTest {

    static final String SEL = "EliminarMetadata_SelectMetadataCuentaContable.sql";
    static final String DEL_META = "EliminarMetadata_DeleteMetadataCuentaContable.sql";
    static final String DEL_FIL = "EliminarMetadata_DeleteMetadataFiltros.sql";

    final RecordingJdbc jdbc = new RecordingJdbc();

    JsonObject run() {
        EliminarMetadataService svc = LegacySql.injectBlank(new EliminarMetadataService(jdbc),
                "SQL_SELECT=" + SEL, "SQL_DELETE_METADATA=" + DEL_META, "SQL_DELETE_FILTROS=" + DEL_FIL);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdMetadataCuentaContable\":[13,14]}"));
    }

    @Test
    @DisplayName("CONT-G-320 existence checked with :IdMetadataCuentaContable = the input list")
    void contG320_query() {
        // legacy EliminarMetadata.java:59-61
        run();
        assertThat(jdbc.single(SEL).params()).containsEntry("IdMetadataCuentaContable", List.of(13L, 14L));
    }

    @Test
    @DisplayName("CONT-G-321 fewer rows than ids -> {1, UI_METADATA_NO_EXISTE}, nothing deleted")
    void contG321_noExiste() {
        // legacy EliminarMetadata.java:62-64
        jdbc.rows(SEL, row("IdMetadataCuentaContable", 13L));
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_METADATA_NO_EXISTE");
        assertThat(jdbc.executions(DEL_META) + jdbc.executions(DEL_FIL)).isZero();
    }

    @Test
    @DisplayName("CONT-G-322 all exist -> delete MetadataCuentaContable then MetadataFiltros per id; {0, UI_EXITO}, no P_OUT_MENSAJES")
    void contG322_elimina() {
        // legacy EliminarMetadata.java:32-35, 66-69
        jdbc.rows(SEL, row("IdMetadataCuentaContable", 13L), row("IdMetadataCuentaContable", 14L));
        JsonObject out = run();
        assertThat(errores(out)).isZero();
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertThat(out.has("P_OUT_MENSAJES")).isFalse();
        assertThat(jdbc.executedParamSets(DEL_META)).extracting(p -> p.get("IdMetadataCuentaContable")).containsExactly(13L, 14L);
        assertThat(jdbc.executedParamSets(DEL_FIL)).extracting(p -> p.get("IdMetadataCuentaContable")).containsExactly(13L, 14L);
        assertThat(jdbc.executedFilesInOrder()).containsSubsequence(DEL_META, DEL_FIL);
    }

    @Test
    @DisplayName("CONT-G-323 database failure is caught -> {1, UI_ERROR}")
    void contG323_failure() {
        // legacy EliminarMetadata.java:71-75
        jdbc.failOn(SEL);
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_ERROR");
    }
}
