package cl.bcs.sura;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.support.SuraTestSupport.FakePort;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static cl.bcs.sura.support.SuraTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Characterizes legacy CrearClienteFullWSModificarPerfilesPortafolio.logica
 * (legacy/.../CrearClienteFullWSModificarPerfilesPortafolio.java:84-145, 146-180, 182-216).
 *
 * main has NO service for this class (not even a controller route): every test here is red with
 * GAP-MISSING-SERVICE until cl.bcs.sura.service.CrearClienteFullWSModificarPerfilesPortafolioService exists
 * with a constructor (SuraDataPort | PerfilesPortafolioUtilsService) and execute(JsonObject).
 *
 * B-SURA-07 (first document per socio), B-SURA-12 (matching on ALL documents, COBROCUST first wins),
 * B-SURA-13 (order), B-SURA-16 (advertencias recount, output shape), B-SURA-09/10 (error entry), B-SURA-19.
 */
class CrearClienteFullWSPerfilesTest {

    static final String SVC = "CrearClienteFullWSModificarPerfilesPortafolioService";
    static final String DOC1 = "20046547-4";
    static final String DOC2 = "8710435-4";

    static String socio(String[] docs, String... cobrocust) {
        StringBuilder td = new StringBuilder();
        for (int i = 0; i < docs.length; i++) {
            if (i > 0) td.append(',');
            td.append("{\"NumeroDocumento\":\"").append(docs[i]).append("\"}");
        }
        StringBuilder pn = new StringBuilder();
        for (int i = 0; i < cobrocust.length; i += 2) {
            if (i > 0) pn.append(',');
            pn.append("{\"CodigoTipoIndicador\":\"").append(cobrocust[i]).append("\",\"ValorIndicador\":\"").append(cobrocust[i + 1]).append("\"}");
        }
        return "{\"TiposDocumento\":[" + td + "],\"DatosCliente\":{\"DatosPortafolio\":{\"PreferenciasNegocio\":[" + pn + "]}}}";
    }

    static final String ENTIDAD_CON_MENSAJES = "{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":1,\"IdInterno\":0,\"Mensajes\":["
            + "{\"IdInterno\":0,\"TipoMensaje\":\"OK\",\"Codigo\":\"UI_CLIENTE_CREADO\",\"Mensaje\":\"CLIENTE CREADO\"},"
            + "{\"IdInterno\":1,\"TipoMensaje\":\"ADVERTENCIA\",\"Codigo\":\"A1\",\"Mensaje\":\"a1\"},"
            + "{\"IdInterno\":2,\"TipoMensaje\":\"ADVERTENCIA\",\"Codigo\":\"A2\",\"Mensaje\":\"a2\"}]},"
            + "{\"TipoEntidad\":\"PERSONA\",\"IdEntidad\":2,\"IdInterno\":0,\"Mensajes\":["
            + "{\"IdInterno\":3,\"TipoMensaje\":\"ADVERTENCIA\",\"Codigo\":\"A3\",\"Mensaje\":\"a3\"}]}";

    static JsonObject params(int errores, String... socios) {
        return json("{\"p_IdEmpresa\":3,\"p_IdUsuario\":4,\"P_OUT_ERRORESCONTADOR\":" + errores
                + ",\"P_OUT_ENTIDADCREADA\":[" + ENTIDAD_CON_MENSAJES + "],\"p_SocioNegocio\":[" + String.join(",", socios) + "]}");
    }

    static FakePort escenario() {
        return new FakePort()
                .ids(idRow(101, 1, DOC1, 0, "PROPOSITOAPV"), idRow(102, 1, DOC1, 55, ""))
                .perfiles(perfilRow(101, 1, DOC1, 0, "", "PC", "PCO", "PP", "PROPOSITOAPV"),
                          perfilRow(102, 1, DOC1, 55, "", "PC", "PCO", "PP", ""));
    }

    @Test
    void bSura13_flujoFeliz_ordenArgumentosYSalida() throws Exception {
        FakePort f = escenario();
        JsonObject out = execute(SVC, f.port, params(0, socio(new String[]{DOC1, DOC2}, "COBROCUST", "OPCION 1")));

        assertThat(f.names().stream().filter(n -> !n.equals(MENSAJE)).toList())
                .containsExactly(IDS, INGRESAR, MODIF_IND, PERFILES, MODIF_PERF);
        // B-SURA-07: only the FIRST NumeroDocumento of each socio is queried
        assertThat(f.calls(IDS).get(0).strs(0)).containsExactly(DOC1);
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("OPCION 1");
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).containsExactly(101L);
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L);   // never APV-neutralised in Crear
        assertThat(f.calls(PERFILES).get(0).strs(0)).containsExactly(DOC1, DOC1);
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0)).extracting(s -> s.idPortafolio(), s -> s.formaCobro())
                .containsExactly(tuple(101L, "OPCION 1"), tuple(102L, "OPCION 1"));

        // B-SURA-16: three output fields only, warnings recounted from P_OUT_ENTIDADCREADA
        assertThat(out.keySet()).containsExactlyInAnyOrder("P_OUT_ERRORESCONTADOR", "P_OUT_ADVERTENCIASCONTADOR", "P_OUT_ENTIDADCREADA");
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(out.get("P_OUT_ADVERTENCIASCONTADOR").getAsLong()).isEqualTo(3L);   // 2 + 1 ADVERTENCIA, OK not counted
        assertThat(arr(out, "P_OUT_ENTIDADCREADA")).hasSize(2);
    }

    @Test
    void bSura12_elMatchUsaTodosLosDocumentosDelSocio() throws Exception {
        // portfolio row found under the SECOND document is still matched by ingresarIndicadoresCobroCustodia
        FakePort f = new FakePort().ids(idRow(301, 5, DOC2, 0, ""))
                .perfiles(perfilRow(301, 5, DOC2, 0, "", "PC", "PCO", "PP", ""));
        execute(SVC, f.port, params(0, socio(new String[]{DOC1, DOC2}, "COBROCUST", "OPCION 1")));
        assertThat(f.calls(INGRESAR)).hasSize(1);
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).containsExactly(301L);
    }

    @Test
    void bSura07_socioSinTiposDocumento_noAportaDocumento_yNoMatchea() throws Exception {
        FakePort f = new FakePort().ids();
        execute(SVC, f.port, params(0, socio(new String[]{}, "COBROCUST", "OPCION 1"), socio(new String[]{DOC1}, "COBROCUST", "OPCION 1")));
        assertThat(f.calls(IDS).get(0).strs(0)).containsExactly(DOC1);
    }

    @Test
    void bSura07_sinDocumentos_noEscribeNada() throws Exception {
        FakePort f = new FakePort().ids();
        JsonObject out = execute(SVC, f.port, params(0, socio(new String[]{}, "COBROCUST", "OPCION 1")));
        assertThat(f.calls(INGRESAR)).isEmpty();
        assertThat(f.calls(MODIF_PERF)).isEmpty();
        assertThat(f.calls(PERFILES)).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }

    @Test
    void bSura12_sinCobrocust_noInserta_peroSiActualizaConFormaVacia() throws Exception {
        FakePort f = escenario();
        execute(SVC, f.port, params(0, socio(new String[]{DOC1}, "OTRO", "X")));
        assertThat(f.calls(INGRESAR)).isEmpty();
        assertThat(f.calls(MODIF_IND).get(0).snaps(0)).extracting(s -> s.formaCobro()).containsExactly("", "");
        assertThat(f.calls(PERFILES)).hasSize(1);
    }

    @Test
    void bSura12_primerCobrocustGana() throws Exception {
        FakePort f = escenario();
        execute(SVC, f.port, params(0, socio(new String[]{DOC1}, "COBROCUST", "UNO", "COBROCUST", "DOS")));
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("UNO");
    }

    @Test
    void bSura16_advertenciasSeRecalculanIgnorandoElContadorEntrante() throws Exception {
        FakePort f = new FakePort().ids();
        JsonObject in = params(0, socio(new String[]{DOC1}));
        in.addProperty("P_OUT_ADVERTENCIASCONTADOR", 99L);
        JsonObject out = execute(SVC, f.port, in);
        assertThat(out.get("P_OUT_ADVERTENCIASCONTADOR").getAsLong()).isEqualTo(3L);
    }

    @Test
    void bSura10_perfilVacio_errorEntidadCreadaWsBase() throws Exception {
        FakePort f = new FakePort().ids(idRow(102, 6, DOC1, 55, ""))
                .perfiles(perfilRow(102, 6, DOC1, 55, "", "", "PCO", "PP", ""));
        JsonObject out = execute(SVC, f.port, params(0, socio(new String[]{DOC1}, "COBROCUST", "OPCION 1")));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        // warnings are counted BEFORE the error entity is appended (and the error is not a warning anyway)
        assertThat(out.get("P_OUT_ADVERTENCIASCONTADOR").getAsLong()).isEqualTo(3L);
        JsonArray ent = arr(out, "P_OUT_ENTIDADCREADA");
        assertThat(ent).hasSize(3);
        JsonObject e = ent.get(2).getAsJsonObject();
        assertThat(e.get("TipoEntidad").getAsString()).isEqualTo("PORTAFOLIO");
        assertThat(e.get("IdEntidad").getAsLong()).isEqualTo(102L);
        assertThat(e.get("IdInterno").getAsLong()).isZero();
        JsonObject m = e.getAsJsonArray("Mensajes").get(0).getAsJsonObject();
        assertThat(m.get("IdInterno").getAsLong()).isZero();
        assertThat(m.get("Codigo").getAsString()).isEqualTo(CODIGO_ERROR);
        assertThat(m.get("Mensaje").getAsString()).isEqualTo(DESC_FAKE);
        assertThat(m.get("TipoMensaje").getAsString()).isEqualTo("ERROR");
        assertThat(f.calls(MENSAJE).get(0).args().get(3))
                .isEqualTo(Map.of("IdFormaCobro", "OPCION 1", "TipoEntidad", "PORTAFOLIO", "IdEntidad", "102"));
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(0)).longValue()).isEqualTo(6L);
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(1)).longValue()).isEqualTo(3L);
        assertThat(f.calls(MODIF_PERF)).isEmpty();
        assertThat(f.calls(INGRESAR)).hasSize(1);   // B-SURA-19: indicator writes happened before the check
        assertThat(f.calls(MODIF_IND)).hasSize(1);
    }

    @Test
    void bSura19_erroresPrevios_perfilesNoSeEscriben_indicadoresSi() throws Exception {
        FakePort f = escenario();
        JsonObject out = execute(SVC, f.port, params(4, socio(new String[]{DOC1}, "COBROCUST", "OPCION 1")));
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(4L);
        assertThat(f.calls(INGRESAR)).hasSize(1);
        assertThat(f.calls(MODIF_IND)).hasSize(1);
        assertThat(f.calls(MODIF_PERF)).isEmpty();
    }

    @Test
    void bSura18_filaDeBDSinPortafolioEnMemoria_lanzaNoSuchElement() {
        FakePort f = new FakePort().ids(idRow(102, 1, DOC1, 55, ""))
                .perfiles(perfilRow(777, 1, DOC1, 55, "X", "PC", "PCO", "PP", ""));
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> execute(SVC, f.port, params(0, socio(new String[]{DOC1}, "COBROCUST", "OPCION 1"))))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
