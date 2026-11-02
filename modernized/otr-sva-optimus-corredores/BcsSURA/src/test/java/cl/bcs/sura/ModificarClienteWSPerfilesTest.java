package cl.bcs.sura;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.support.SuraTestSupport.FakePort;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map;

import static cl.bcs.sura.support.SuraTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterizes legacy ModificarClienteModificarPerfilesPortafolioWS.logica
 * (legacy/.../ModificarClienteModificarPerfilesPortafolioWS.java:107-150, 161-190, 192-230).
 *
 * B-SURA-13 (order), B-SURA-14 (flag default "S" here), B-SURA-15 (DatosPortafolio loop quirks),
 * B-SURA-17 (output shape), B-SURA-18 (merge of IdFormaCobro, duplicate documents), B-SURA-09/10 (error entry as
 * raw MensajeSalida), B-SURA-19 (error gating: indicator writes still happen).
 */
class ModificarClienteWSPerfilesTest {

    static final String SVC = "ModificarClienteModificarPerfilesPortafolioWSService";
    static final String DOC = "11111111-1";
    static final String CC = "{\"CodigoTipoIndicador\":\"COBROCUST\",\"ValorIndicador\":\"OPCION 1\"}";

    static String wsSocio(String doc, String... indicadoresPorDatosPortafolio) {
        StringBuilder dp = new StringBuilder();
        for (int i = 0; i < indicadoresPorDatosPortafolio.length; i++) {
            if (i > 0) dp.append(',');
            dp.append("{\"Indicadores\":[").append(indicadoresPorDatosPortafolio[i]).append("]}");
        }
        return "{\"DocumentoIdentidad\":\"" + doc + "\",\"DatosPortafolio\":[" + dp + "]}";
    }

    static JsonObject params(int errores, String flag, String... socios) {
        String f = flag == null ? "" : ",\"p_ActualizarIndicadorFormaCobro\":\"" + flag + "\"";
        return json("{\"p_IdEmpresa\":5,\"p_IdUsuario\":4,\"P_OUT_ERRORESCONTADOR\":" + errores
                + ",\"P_OUT_ADVERTENCIASCONTADOR\":2,\"P_OUT_ENTIDADES_MANIPULADAS\":[{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":1,\"Mensajes\":[]}]"
                + f + ",\"p_SocioNegocio\":[" + String.join(",", socios) + "]}");
    }

    /** APV portfolio 101 without indicator, normal portfolio 102 with indicator 55. DB profile has empty IdFormaCobro. */
    static FakePort escenario() {
        return new FakePort()
                .ids(idRow(101, 1, DOC, 0, "PROPOSITOAPV"), idRow(102, 1, DOC, 55, ""))
                .perfiles(perfilRow(101, 1, DOC, 0, "", "PC", "PCO", "PP", "PROPOSITOAPV"),
                          perfilRow(102, 1, DOC, 55, "", "PC", "PCO", "PP", ""));
    }

    @Test
    void bSura13_flujoFeliz_ordenArgumentosYForma() throws Exception {
        FakePort f = escenario();
        JsonObject out = execute(SVC, f.port, params(0, null, wsSocio(DOC, CC)));

        assertThat(f.names().stream().filter(n -> !n.equals(MENSAJE)).toList())
                .containsExactly(IDS, INGRESAR, MODIF_IND, PERFILES, MODIF_PERF);
        assertThat(f.calls(IDS).get(0).strs(0)).containsExactly(DOC);
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("OPCION 1");
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).containsExactly(101L);
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L);

        // B-SURA-18: one document entry PER ID (not de-duplicated)
        assertThat(f.calls(PERFILES).get(0).strs(0)).containsExactly(DOC, DOC);
        // merge: DB IdFormaCobro "" is filled from the in-memory value set by the COBROCUST indicator
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0)).extracting(s -> s.idPortafolio(), s -> s.formaCobro())
                .containsExactly(org.assertj.core.api.Assertions.tuple(101L, "OPCION 1"),
                                 org.assertj.core.api.Assertions.tuple(102L, "OPCION 1"));

        // B-SURA-17: output is ONLY these three fields, counters passed through from jout
        assertThat(out.keySet()).containsExactlyInAnyOrder("P_OUT_ERRORESCONTADOR", "P_OUT_ADVERTENCIASCONTADOR", "P_OUT_ENTIDADES_MANIPULADAS");
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(out.get("P_OUT_ADVERTENCIASCONTADOR").getAsLong()).isEqualTo(2L);
        assertThat(arr(out, "P_OUT_ENTIDADES_MANIPULADAS")).hasSize(1);
    }

    @Test
    void bSura18_formaCobroDeLaBDNoVaciaSeMantiene() throws Exception {
        FakePort f = new FakePort()
                .ids(idRow(102, 1, DOC, 55, ""))
                .perfiles(perfilRow(102, 1, DOC, 55, "FC-BD", "PC", "PCO", "PP", ""));
        execute(SVC, f.port, params(0, null, wsSocio(DOC, CC)));
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0).get(0).formaCobro()).isEqualTo("FC-BD");
    }

    @Test
    void bSura18_filaDeBDSinPortafolioEnMemoria_lanzaNoSuchElement() {
        // legacy: ids.filter(...).findFirst().get() is evaluated for every returned row
        FakePort f = new FakePort().ids(idRow(102, 1, DOC, 55, ""))
                .perfiles(perfilRow(999, 1, DOC, 55, "X", "PC", "PCO", "PP", ""));
        assertThatThrownBy(() -> execute(SVC, f.port, params(0, null, wsSocio(DOC, CC))))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void bSura14_flagAusente_porDefectoEsS_apvActualizaIndicador() throws Exception {
        FakePort f = new FakePort().ids(idRow(101, 1, DOC, 77, "PROPOSITOAPV"), idRow(102, 1, DOC, 55, ""))
                .perfiles(perfilRow(101, 1, DOC, 77, "", "PC", "PCO", "PP", "PROPOSITOAPV"),
                          perfilRow(102, 1, DOC, 55, "", "PC", "PCO", "PP", ""));
        execute(SVC, f.port, params(0, null, wsSocio(DOC, CC)));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(77L, 55L);
    }

    @Test
    void bSura14_flagN_apvNoActualizaIndicador() throws Exception {
        FakePort f = new FakePort().ids(idRow(101, 1, DOC, 77, "PROPOSITOAPV"), idRow(102, 1, DOC, 55, ""))
                .perfiles(perfilRow(101, 1, DOC, 77, "", "PC", "PCO", "PP", "PROPOSITOAPV"),
                          perfilRow(102, 1, DOC, 55, "", "PC", "PCO", "PP", ""));
        execute(SVC, f.port, params(0, "N", wsSocio(DOC, CC)));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L);
        // the profiles are still written for the APV portfolio
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0)).extracting(s -> s.idPortafolio()).containsExactly(101L, 102L);
    }

    @Test
    void bSura15_socioSinDatosPortafolio_noSeProcesaNada() throws Exception {
        // quirk: listaPerfiles.addAll(perfiles) lives INSIDE the DatosPortafolio loop -> no entries -> ids empty
        FakePort f = escenario();
        JsonObject out = execute(SVC, f.port, params(0, null, wsSocio(DOC)));
        assertThat(f.calls(IDS)).hasSize(1);
        assertThat(f.calls(PERFILES)).isEmpty();
        assertThat(f.calls(MODIF_PERF)).isEmpty();
        assertThat(f.calls(INGRESAR)).isEmpty();
        assertThat(efectivosUpdate(f.calls(MODIF_IND).isEmpty() ? new Call("x", List.of(List.of())) : f.calls(MODIF_IND).get(0))).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }

    @Test
    void bSura15_dosDatosPortafolio_duplicanLosPerfiles() throws Exception {
        FakePort f = escenario();
        execute(SVC, f.port, params(0, null, wsSocio(DOC, CC, "{\"CodigoTipoIndicador\":\"COBROCUST\",\"ValorIndicador\":\"OPCION 2\"}")));
        // two inserts (one per DatosPortafolio) with their own value
        assertThat(f.calls(INGRESAR)).hasSize(2);
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("OPCION 1");
        assertThat(f.calls(INGRESAR).get(1).str(1)).isEqualTo("OPCION 2");
        // same objects added twice -> 4 entries -> 4 documents
        assertThat(f.calls(PERFILES).get(0).strs(0)).hasSize(4);
    }

    @Test
    void bSura15_datosPortafolioSinCobrocust_formaCobroQuedaVacia_noInserta_peroActualizaConVacio() throws Exception {
        FakePort f = escenario();
        execute(SVC, f.port, params(0, null,
                wsSocio(DOC, "{\"CodigoTipoIndicador\":\"OTRO\",\"ValorIndicador\":\"Z\"}")));
        assertThat(f.calls(INGRESAR)).isEmpty();
        assertThat(f.calls(MODIF_IND).get(0).snaps(0)).extracting(s -> s.idPortafolio(), s -> s.formaCobro())
                .containsExactly(org.assertj.core.api.Assertions.tuple(101L, ""), org.assertj.core.api.Assertions.tuple(102L, ""));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L); // updated with empty value
        assertThat(f.calls(PERFILES)).hasSize(1);
    }

    @Test
    void bSura15_primerCobrocustGana_yOtrosTiposSeIgnoran() throws Exception {
        FakePort f = escenario();
        execute(SVC, f.port, params(0, null, wsSocio(DOC,
                "{\"CodigoTipoIndicador\":\"OTRO\",\"ValorIndicador\":\"Z\"},{\"CodigoTipoIndicador\":\"COBROCUST\",\"ValorIndicador\":\"PRIMERO\"},"
                        + "{\"CodigoTipoIndicador\":\"COBROCUST\",\"ValorIndicador\":\"SEGUNDO\"}")));
        assertThat(f.calls(INGRESAR)).hasSize(1);
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("PRIMERO");
    }

    @Test
    void bSura15_socioSinPortafolios_noLlamaANada() throws Exception {
        FakePort f = new FakePort().ids();
        JsonObject out = execute(SVC, f.port, params(0, null, wsSocio("99999999-9", CC)));
        assertThat(f.calls(INGRESAR)).isEmpty();
        assertThat(f.calls(PERFILES)).isEmpty();
        assertThat(f.calls(MODIF_PERF)).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }

    @Test
    void bSura10_perfilVacio_errorComoMensajeSalidaCrudo_yContador() throws Exception {
        FakePort f = new FakePort().ids(idRow(102, 4, DOC, 55, ""))
                .perfiles(perfilRow(102, 4, DOC, 55, "", "PC", "", "PP", ""));
        JsonObject out = execute(SVC, f.port, params(0, null, wsSocio(DOC, CC)));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        JsonArray ent = arr(out, "P_OUT_ENTIDADES_MANIPULADAS");
        assertThat(ent).hasSize(2);
        JsonObject e = ent.get(1).getAsJsonObject();
        assertThat(e.keySet()).contains("TipoEntidad", "IdEntidad", "Mensajes");
        assertThat(e.get("TipoEntidad").getAsString()).isEqualTo("PORTAFOLIO");
        assertThat(e.get("IdEntidad").getAsLong()).isEqualTo(102L);
        // EntidadManipulada.Mensajes holds raw MensajeSalida objects (lower-case tipo/codigo/descripcion), NOT Codigo/Mensaje/TipoMensaje
        JsonObject m = e.getAsJsonArray("Mensajes").get(0).getAsJsonObject();
        assertThat(m.get("codigo").getAsString()).isEqualTo(CODIGO_ERROR);
        assertThat(m.get("descripcion").getAsString()).isEqualTo(DESC_FAKE);
        assertThat(m.get("tipo").getAsString()).isEqualTo("ERROR");
        assertThat(m.has("Codigo")).isFalse();
        assertThat(m.has("TipoMensaje")).isFalse();

        assertThat(f.calls(MENSAJE).get(0).args().get(3))
                .isEqualTo(Map.of("IdFormaCobro", "OPCION 1", "TipoEntidad", "PORTAFOLIO", "IdEntidad", "102"));
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(0)).longValue()).isEqualTo(4L);
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(1)).longValue()).isEqualTo(5L);
        // B-SURA-19: indicators were already written before the check; profiles are not
        assertThat(f.calls(MODIF_IND)).hasSize(1);
        assertThat(f.calls(MODIF_PERF)).isEmpty();
    }

    @Test
    void bSura19_erroresPrevios_indicadoresSiSeEscriben_perfilesNo() throws Exception {
        FakePort f = escenario();
        JsonObject out = execute(SVC, f.port, params(1, null, wsSocio(DOC, CC)));
        assertThat(f.calls(INGRESAR)).hasSize(1);
        assertThat(f.calls(MODIF_IND)).hasSize(1);
        assertThat(f.calls(PERFILES)).hasSize(1);
        assertThat(f.calls(MODIF_PERF)).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
    }
}
