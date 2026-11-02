package cl.bcs.sura;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.support.SuraTestSupport.FakePort;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static cl.bcs.sura.support.SuraTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterizes legacy ModificarSocioNegocioModificarPerfilesPortafolio.logica
 * (legacy/.../ModificarSocioNegocioModificarPerfilesPortafolio.java:84-125).
 *
 * B-SURA-07 (all socios give documents, no TipoEntidad filter), B-SURA-09/10 (error entry, EntidadModificada shape),
 * B-SURA-13 (order), B-SURA-14 (p_ActualizarIndicadorFormaCobro defaults to "N" in THIS class and drives the APV
 * neutralisation), B-SURA-19 (error gating).
 */
class ModificarSocioNegocioPerfilesTest {

    static final String SVC = "ModificarSocioNegocioModificarPerfilesPortafolioService";
    static final String DOC = "11111111-1";

    static JsonObject params(Integer erroresPrevios, String flag, String... socios) {
        String f = flag == null ? "" : ",\"p_ActualizarIndicadorFormaCobro\":\"" + flag + "\"";
        return json("{\"p_IdEmpresa\":7,\"p_IdUsuario\":4,\"P_OUT_ERRORESCONTADOR\":" + erroresPrevios
                + ",\"p_Out_ContadorEntidadModificada\":1,\"p_Out_EntidadModificada\":[]" + f
                + ",\"p_SocioNegocio\":[" + String.join(",", socios) + "]}");
    }

    /** APV portfolio with existing indicator 77 and a normal one with existing indicator 55. */
    static FakePort escenarioDosPortafolios() {
        return new FakePort()
                .ids(idRow(101, 1, DOC, 77, "PROPOSITOAPV"), idRow(102, 1, DOC, 55, ""))
                .filtrados(perfilRow(101, 1, DOC, 77, "OPCION 1", "PC", "PCO", "PP", "PROPOSITOAPV"),
                           perfilRow(102, 1, DOC, 55, "OPCION 1", "PC", "PCO", "PP", ""));
    }

    @Test
    void bSura14_flagAusente_porDefectoEsN_apvNoActualizaIndicador() throws Exception {
        FakePort f = escenarioDosPortafolios();
        execute(SVC, f.port, params(0, null, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L); // 77 (APV) excluded
    }

    @Test
    void bSura14_flagN_apvNoActualizaIndicador() throws Exception {
        FakePort f = escenarioDosPortafolios();
        execute(SVC, f.port, params(0, "N", socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L);
    }

    @Test
    void bSura14_flagS_apvSiActualizaIndicador() throws Exception {
        FakePort f = escenarioDosPortafolios();
        execute(SVC, f.port, params(0, "S", socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(77L, 55L);
    }

    @Test
    void bSura14_flagDistintoDeN_ejemploX_noNeutraliza() throws Exception {
        // legacy only tests "N".equals(flag): any other value behaves like "S"
        FakePort f = escenarioDosPortafolios();
        execute(SVC, f.port, params(0, "X", socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(77L, 55L);
    }

    @Test
    void bSura13_ordenDeOperaciones_ingresarLuegoIndicadoresLuegoPerfiles() throws Exception {
        FakePort f = escenarioDosPortafolios();
        execute(SVC, f.port, params(0, "S", socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(f.names().stream().filter(n -> !n.equals(MENSAJE)).toList())
                .containsExactly(IDS, FILTRADOS, INGRESAR, MODIF_IND, MODIF_PERF);
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("OPCION 1");
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).isEmpty(); // both portfolios already have an indicator
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0)).extracting(s -> s.idPortafolio()).containsExactly(101L, 102L);
    }

    @Test
    void bSura07_todosLosSociosAportanDocumento_sinFiltroDeTipoEntidad() throws Exception {
        FakePort f = new FakePort().ids();
        execute(SVC, f.port, params(0, "S", socio("PERSONA", "A-1"), socio("CLIENTE", "B-2"), socio("OTRO", "C-3")));
        assertThat(f.calls(IDS).get(0).strs(0)).containsExactly("A-1", "B-2", "C-3");
    }

    @Test
    void bSura09_perfilVacio_errorEnEntidadModificadaYSinEscrituras() throws Exception {
        IdsEntidadYPerfiles bad = perfilRow(102, 9, DOC, 55, "OPCION 1", "PC", "", "PP", "");
        FakePort f = new FakePort().ids(idRow(102, 9, DOC, 55, "")).filtrados(bad);
        JsonObject out = execute(SVC, f.port, params(0, "S", socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(f.writes()).isEmpty();
        JsonArray ent = arr(out, "p_Out_EntidadModificada");
        assertThat(ent).hasSize(1);
        JsonObject e = ent.get(0).getAsJsonObject();
        assertThat(e.get("TipoEntidad").getAsString()).isEqualTo("PORTAFOLIO");
        assertThat(e.get("IdEntidad").getAsLong()).isEqualTo(102L);
        assertThat(e.has("Estado") && !e.get("Estado").getAsString().isEmpty()).isFalse();
        JsonObject m = e.getAsJsonArray("Mensajes").get(0).getAsJsonObject();
        assertThat(m.get("IdInterno").getAsLong()).isZero();
        assertThat(m.get("Codigo").getAsString()).isEqualTo(CODIGO_ERROR);
        assertThat(m.get("Mensaje").getAsString()).isEqualTo(DESC_FAKE);
        assertThat(m.get("TipoMensaje").getAsString()).isEqualTo("ERROR");
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(0)).longValue()).isEqualTo(9L);
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(1)).longValue()).isEqualTo(7L);
        assertThat(f.calls(MENSAJE).get(0).args().get(3))
                .isEqualTo(Map.of("IdFormaCobro", "OPCION 1", "TipoEntidad", "PORTAFOLIO", "IdEntidad", "102"));
    }

    @Test
    void bSura19_erroresPreviosEnJout_bloqueanEscrituras() throws Exception {
        FakePort f = escenarioDosPortafolios();
        JsonObject out = execute(SVC, f.port, params(3, "S", socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(f.writes()).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(3L);
    }

    @Test
    void bSura11_filasSinFormaCobroSeDescartan_noHayEscrituras() throws Exception {
        FakePort f = new FakePort().ids(idRow(102, 1, DOC, 55, ""))
                .filtrados(perfilRow(102, 1, DOC, 55, "", "", "", "", ""));
        JsonObject out = execute(SVC, f.port, params(0, "S", socio("CLIENTE", DOC)));
        assertThat(f.writes()).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }

    @Test
    void bSura12_insertaIndicadorSoloParaPortafoliosSinIndicador() throws Exception {
        FakePort f = new FakePort().ids(idRow(101, 1, DOC, 0, ""), idRow(102, 1, DOC, 55, ""))
                .filtrados(perfilRow(101, 1, DOC, 0, "OPCION 1", "PC", "PCO", "PP", ""),
                           perfilRow(102, 1, DOC, 55, "OPCION 1", "PC", "PCO", "PP", ""));
        execute(SVC, f.port, params(0, "S", socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).containsExactly(101L);
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L);
    }
}
