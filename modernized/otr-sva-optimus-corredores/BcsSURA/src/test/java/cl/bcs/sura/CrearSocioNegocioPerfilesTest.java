package cl.bcs.sura;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.support.SuraTestSupport.FakePort;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static cl.bcs.sura.support.SuraTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Characterizes legacy CrearSocioNegocioModificarPerfilesPortafolio.logica
 * (legacy/.../CrearSocioNegocioModificarPerfilesPortafolio.java:79-117, 119-146).
 *
 * Behaviors: B-SURA-07 (document selection), B-SURA-08 (profile writes only without errors),
 * B-SURA-09 (missing-profile error), B-SURA-10 (error entry shape), B-SURA-11 (temp-table seed + filter),
 * B-SURA-12 (COBROCUST first-wins, per-socio insert), B-SURA-13 (operation order), B-SURA-19 (error gating).
 * All tests are red today because the main service has no SuraDataPort seam (GAP-SEAM).
 */
class CrearSocioNegocioPerfilesTest {

    static final String SVC = "CrearSocioNegocioModificarPerfilesPortafolioService";
    static final String DOC = "11111111-1";
    static final String DOC2 = "22222222-2";

    static IdsEntidadYPerfiles apvNuevo()      { return idRow(101, 1, DOC, 0, "PROPOSITOAPV"); }
    static IdsEntidadYPerfiles normalExistente() { return idRow(102, 1, DOC, 55, ""); }

    static IdsEntidadYPerfiles completoApv()   { return perfilRow(101, 1, DOC, 0, "OPCION 1", "PC-A", "PCO-A", "PP-A", "PROPOSITOAPV"); }
    static IdsEntidadYPerfiles completoNormal() { return perfilRow(102, 1, DOC, 55, "OPCION 1", "PC-A", "PCO-A", "PP-A", ""); }

    static JsonObject params(int erroresPrevios, String... socios) {
        return json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"P_OUT_ERRORESCONTADOR\":" + erroresPrevios
                + ",\"p_Out_ContadorEntidadCreada\":1,\"p_Out_EntidadCreada\":[{\"TipoEntidad\":\"CLIENTE\",\"IdEntidad\":3,"
                + "\"Estado\":\"ACT\",\"Nuevo\":\"S\",\"Mensajes\":[]}],\"p_SocioNegocio\":[" + String.join(",", socios) + "]}");
    }

    @Test
    void bSura13_flujoFeliz_ordenYArgumentosDeLasOperaciones() throws Exception {
        FakePort f = new FakePort().ids(apvNuevo(), normalExistente()).filtrados(completoApv(), completoNormal());
        JsonObject out = execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));

        // order: ids -> filtered query -> insert -> update indicators -> update profiles
        assertThat(f.names().stream().filter(n -> !n.equals(MENSAJE)).toList())
                .containsExactly(IDS, FILTRADOS, INGRESAR, MODIF_IND, MODIF_PERF);
        assertThat(f.calls(IDS).get(0).strs(0)).containsExactly(DOC);

        // B-SURA-11: the seed sent to the temp table carries the COBROCUST value on every matching portfolio
        assertThat(f.calls(FILTRADOS).get(0).snaps(0)).extracting(s -> s.idPortafolio(), s -> s.formaCobro())
                .containsExactly(tuple(101L, "OPCION 1"), tuple(102L, "OPCION 1"));

        // B-SURA-12: insert gets the COBROCUST value; only IdIndicador==0 are effectively inserted
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("OPCION 1");
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).containsExactly(101L);
        // update indicators: only IdIndicador!=0 && "S" (Crear never applies the APV neutralisation)
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L);
        // profiles: written for every portfolio, no filter
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0)).extracting(s -> s.idPortafolio()).containsExactly(101L, 102L);
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0).get(0).perfilComercial()).isEqualTo("PC-A");

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(arr(out, "p_Out_EntidadCreada")).hasSize(1);
        assertThat(out.get("p_Out_ContadorEntidadCreada").getAsLong()).isEqualTo(1L); // other fields echoed (legacy returns `out`)
    }

    @Test
    void bSura07_soloSociosCLIENTEDanDocumentos() throws Exception {
        FakePort f = new FakePort().ids();
        execute(SVC, f.port, params(0, socio("CLIENTE", DOC), socio("PERSONA", DOC2), socio("CLIENTE", "33333333-3")));
        assertThat(f.calls(IDS)).hasSize(1);
        assertThat(f.calls(IDS).get(0).strs(0)).containsExactly(DOC, "33333333-3");
    }

    @Test
    void bSura07_sinSociosCliente_noEscribeNada() throws Exception {
        FakePort f = new FakePort().ids();
        JsonObject out = execute(SVC, f.port, params(0, socio("PERSONA", DOC2, "COBROCUST", "OPCION 1")));
        assertThat(f.writes()).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }

    @Test
    void bSura11_sinIndicadorCobrocust_filasSinFormaCobroSeDescartan() throws Exception {
        // DB returns formaCobro "" (no PERF match and TEMP empty) -> row dropped -> ids empty -> nothing written
        FakePort f = new FakePort().ids(apvNuevo()).filtrados(perfilRow(101, 1, DOC, 0, "", "", "", "", "PROPOSITOAPV"));
        JsonObject out = execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "OTRO", "X")));
        assertThat(f.calls(FILTRADOS).get(0).snaps(0).get(0).formaCobro()).isEmpty();
        assertThat(f.writes()).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(arr(out, "p_Out_EntidadCreada")).hasSize(1);
    }

    @Test
    void bSura11_formaCobroResueltaPorBD_sinIndicadorEnEntrada_seMantieneYSeActualiza() throws Exception {
        // COALESCE(PERF.IdFormaCobro, TEMP.IdFormaCobro, '') can yield a value even if the input has no COBROCUST
        FakePort f = new FakePort().ids(normalExistente())
                .filtrados(perfilRow(102, 1, DOC, 55, "FC-BD", "PC-A", "PCO-A", "PP-A", ""));
        execute(SVC, f.port, params(0, socio("CLIENTE", DOC)));
        assertThat(f.calls(INGRESAR)).isEmpty();            // no COBROCUST in input -> no insert call
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(55L);
        assertThat(f.calls(MODIF_IND).get(0).snaps(0).get(0).formaCobro()).isEqualTo("FC-BD");
        assertThat(f.calls(MODIF_PERF)).hasSize(1);
    }

    @Test
    void bSura12_varioSociosCadaUnoConSuValor_unaInsercionPorSocioEnOrden() throws Exception {
        FakePort f = new FakePort()
                .ids(idRow(101, 1, DOC, 0, ""), idRow(201, 2, DOC2, 0, ""))
                .filtrados(perfilRow(101, 1, DOC, 0, "OPCION 1", "PC", "PCO", "PP", ""),
                           perfilRow(201, 2, DOC2, 0, "OPCION 2", "PC", "PCO", "PP", ""));
        execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1"),
                socio("CLIENTE", DOC2, "COBROCUST", "OPCION 2"), socio("CLIENTE", "99999999-9", "COBROCUST", "OPCION 3")));
        List<?> ing = f.calls(INGRESAR);
        assertThat(ing).hasSize(2); // third socio has no matching portfolios -> no insert call
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("OPCION 1");
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).containsExactly(101L);
        assertThat(f.calls(INGRESAR).get(1).str(1)).isEqualTo("OPCION 2");
        assertThat(efectivosInsert(f.calls(INGRESAR).get(1))).containsExactly(201L);
    }

    @Test
    void bSura12_variosCobrocust_ganaElPrimeroAunqueEsteVacio() throws Exception {
        // legacy uses findFirst() on CodigoTipoIndicador == COBROCUST with no emptiness check
        FakePort f = new FakePort().ids(apvNuevo());
        execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "", "COBROCUST", "OPCION 9")));
        assertThat(f.calls(FILTRADOS).get(0).snaps(0).get(0).formaCobro()).isEmpty();

        FakePort g = new FakePort().ids(apvNuevo());
        execute(SVC, g.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "PRIMERO", "COBROCUST", "SEGUNDO")));
        assertThat(g.calls(FILTRADOS).get(0).snaps(0).get(0).formaCobro()).isEqualTo("PRIMERO");
    }

    @ParameterizedTest(name = "bSura09 campo vacio {0}")
    @CsvSource({"comercial", "cobros", "productos"})
    void bSura09_cualquierPerfilVacio_generaErrorYNoEscribe(String campo) throws Exception {
        IdsEntidadYPerfiles bad = completoApv();
        switch (campo) {
            case "comercial" -> bad.idPerfilComercial = "";
            case "cobros" -> bad.idPerfilCobros = "";
            default -> bad.idPerfilProductos = "";
        }
        FakePort f = new FakePort().ids(apvNuevo(), normalExistente()).filtrados(bad, completoNormal());
        JsonObject out = execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(f.writes()).isEmpty(); // B-SURA-19: nothing written when errors != 0
        JsonArray ent = arr(out, "p_Out_EntidadCreada");
        assertThat(ent).hasSize(2);
        JsonObject e = ent.get(1).getAsJsonObject();
        assertThat(e.get("TipoEntidad").getAsString()).isEqualTo("PORTAFOLIO");
        assertThat(e.get("IdEntidad").getAsLong()).isEqualTo(101L);
    }

    @Test
    void bSura10_formaDelErrorYParametrosDelCatalogoDeMensajes() throws Exception {
        IdsEntidadYPerfiles bad = completoApv();
        bad.idPerfilCobros = "";
        FakePort f = new FakePort().ids(apvNuevo()).filtrados(bad);
        JsonObject out = execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));

        JsonObject e = arr(out, "p_Out_EntidadCreada").get(1).getAsJsonObject();
        assertThat(e.keySet()).containsExactlyInAnyOrder("TipoEntidad", "IdEntidad", "Estado", "Nuevo", "Mensajes");
        assertThat(e.get("Estado").getAsString()).isEmpty();
        assertThat(e.get("Nuevo").getAsString()).isEmpty();
        JsonObject m = e.getAsJsonArray("Mensajes").get(0).getAsJsonObject();
        assertThat(e.getAsJsonArray("Mensajes")).hasSize(1);
        assertThat(m.get("IdInterno").getAsLong()).isZero();
        assertThat(m.get("Codigo").getAsString()).isEqualTo(CODIGO_ERROR);
        assertThat(m.get("Mensaje").getAsString()).isEqualTo(DESC_FAKE);   // text comes from the catalogue, not hard-coded
        assertThat(m.get("TipoMensaje").getAsString()).isEqualTo("ERROR");

        assertThat(f.calls(MENSAJE)).hasSize(1);
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(0)).longValue()).isEqualTo(1L); // IdCliente of the portfolio
        assertThat(((Number) f.calls(MENSAJE).get(0).args().get(1)).longValue()).isEqualTo(1L); // p_IdEmpresa
        assertThat(f.calls(MENSAJE).get(0).args().get(2)).isEqualTo(CODIGO_ERROR);
        assertThat(f.calls(MENSAJE).get(0).args().get(3)).isEqualTo(
                Map.of("IdFormaCobro", "OPCION 1", "TipoEntidad", "PORTAFOLIO", "IdEntidad", "101"));
    }

    @Test
    void bSura09_dosPortafoliosMalos_dosErroresEnOrdenYContadorDos() throws Exception {
        IdsEntidadYPerfiles a = completoApv();
        a.idPerfilComercial = "";
        IdsEntidadYPerfiles b = completoNormal();
        b.idPerfilProductos = "";
        FakePort f = new FakePort().ids(apvNuevo(), normalExistente()).filtrados(a, b);
        JsonObject out = execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(2L);
        JsonArray ent = arr(out, "p_Out_EntidadCreada");
        assertThat(ent).hasSize(3);
        assertThat(ent.get(1).getAsJsonObject().get("IdEntidad").getAsLong()).isEqualTo(101L);
        assertThat(ent.get(2).getAsJsonObject().get("IdEntidad").getAsLong()).isEqualTo(102L);
        assertThat(f.writes()).isEmpty();
    }

    @Test
    void bSura19_erroresPreviosEnJout_impidenTodaEscrituraAunqueLosPerfilesEstenCompletos() throws Exception {
        FakePort f = new FakePort().ids(apvNuevo(), normalExistente()).filtrados(completoApv(), completoNormal());
        JsonObject out = execute(SVC, f.port, params(2, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(f.writes()).isEmpty();
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(2L);
        assertThat(arr(out, "p_Out_EntidadCreada")).hasSize(1);
    }

    @Test
    void bSura19_erroresPreviosMasNuevoError_sumanContador() throws Exception {
        IdsEntidadYPerfiles bad = completoApv();
        bad.idPerfilCobros = "";
        FakePort f = new FakePort().ids(apvNuevo()).filtrados(bad);
        JsonObject out = execute(SVC, f.port, params(1, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(2L);
    }

    @Test
    void bSura09_perfilesCompletosPeroSinFilas_noHayErrorNiEscritura() throws Exception {
        FakePort f = new FakePort().ids();
        JsonObject out = execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(f.writes()).isEmpty();
        assertThat(f.calls(MENSAJE)).isEmpty();
    }

    @Test
    void bSura13_apvConIndicadorExistente_enCrearSeActualiza() throws Exception {
        // Crear does NOT call validarIndicadorFormaCobroPortafolioApv: APV rows keep ActualizarFormaCobro "S"
        FakePort f = new FakePort().ids(idRow(101, 1, DOC, 77, "PROPOSITOAPV"))
                .filtrados(perfilRow(101, 1, DOC, 77, "OPCION 1", "PC", "PCO", "PP", "PROPOSITOAPV"));
        execute(SVC, f.port, params(0, socio("CLIENTE", DOC, "COBROCUST", "OPCION 1")));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(77L);
    }
}
