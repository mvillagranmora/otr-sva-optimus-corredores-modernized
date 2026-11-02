package cl.bcs.fondomutuo.certificacionporfiltros;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.certificacion.CasoCerPf;

/**
 * FMM-CERPF rules, pinned with LITERALS read in the goldens RECORDED from the unmodified legacy (FM-CXF-01..05 of RULES_FONDOMUTUO_F3_B.md 8.2 and the matrix FM-CXF-C01..C05 of 11.2). Each test
 * names the card in its display name and the number of the case (FMM-CERPF-nnn) it pins. They run the PRODUCTION port through {@link CasoCerPf#ejecutar(String)}; with
 * {@code -Dcer.autocomprobacion=true} the harness hands back the golden itself (no production code runs): the proof that these assertions describe what the legacy did.
 *
 * <p>Seed of the FILTER cases (company 1, date 2026-03-02): 4001 APO FM001A CLP AGF1 nacional S money-market N ING; 4002 APO FM001A CLP AGF2 nacional S money-market S ACP; 4003 APO FM002A USD AGF1 nacional
 * N money-market N ING; 4004 APO FM002A USD AGF2 nacional N money-market S ING; 3001 RES (RESNORM) FM001A CLP AGF1 nacional S money-market N ING. Loads: 900 {4001 PEN, 4002 PEN}, 901 {4003 PEN, 4004
 * ERR}, 902 {4001 PEN twice}, 903 {4001 PEN, 4002 PRO}. RED BEFORE CODE: without the production port every test fails with "T6-A not implemented".
 */
class CerPfReglasTest {

    private static final List<Long> TODAS = List.of(3001L, 4001L, 4002L, 4003L, 4004L);
    private static final List<Long> NINGUNA = List.of();

    private static CasoCerPf.Corrida correr(int numero) throws Exception {
        return CasoCerPf.ejecutar(CasoCerPf.id(numero));
    }

    private static List<Long> l(Long... ids) {
        return List.of(ids);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-02: what each filter field selects (the ids that reach Certificar, in order)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    static Stream<Arguments> selecciones() {
        return Stream.of(
                Arguments.of(1, "all-pass filter (texts empty, Nacional N, MoneyMarket N, IdCarga 0) selects the five", TODAS),
                Arguments.of(2, "IdEmisor ZZZ selects nothing", NINGUNA),
                Arguments.of(3, "TipoSolicitud RES selects the one rescate", l(3001L)),
                Arguments.of(4, "IdSerieFondo FM002A selects the two USD aportes", l(4003L, 4004L)),
                Arguments.of(14, "a filter with no field at all: IdCarga null makes the OR false, nothing selected", NINGUNA),
                Arguments.of(15, "IdEmisor AGF2", l(4002L, 4004L)),
                Arguments.of(16, "IdMoneda USD", l(4003L, 4004L)),
                Arguments.of(17, "IdMoneda EUR selects nothing", NINGUNA),
                Arguments.of(18, "IdSerieFondo FM001A", l(3001L, 4001L, 4002L)),
                Arguments.of(19, "Nacional N is NO filter", TODAS),
                Arguments.of(20, "Nacional S selects the national funds only", l(3001L, 4001L, 4002L)),
                Arguments.of(21, "Nacional null is NO filter", TODAS),
                Arguments.of(22, "Nacional absent (null) is NO filter", TODAS),
                Arguments.of(23, "Nacional X (another value) equals EsFondoNacional of no row", NINGUNA),
                Arguments.of(24, "Nacional empty is NOT 'no filter': the equality with EsFondoNacional selects nothing", NINGUNA),
                Arguments.of(25, "Nacional lower case s does not match S", NINGUNA),
                Arguments.of(26, "TipoSolicitud APO", l(4001L, 4002L, 4003L, 4004L)),
                Arguments.of(27, "TipoSolicitud unknown", NINGUNA),
                Arguments.of(28, "IdTipoMovimientoFondoMutuo RESNORM", l(3001L)),
                Arguments.of(29, "IdTipoMovimientoFondoMutuo APONORM", l(4001L, 4002L, 4003L, 4004L)),
                Arguments.of(30, "IdTipoMovimientoFondoMutuo APOREINV selects nothing", NINGUNA),
                Arguments.of(31, "MoneyMarket S", l(4002L, 4004L)),
                Arguments.of(32, "MoneyMarket empty is NOT 'no filter'", NINGUNA),
                Arguments.of(33, "MoneyMarket null is NO filter", TODAS),
                Arguments.of(34, "every text field null with IdCarga 0 does not filter", TODAS),
                Arguments.of(35, "several fields at once are an AND", l(4004L)),
                Arguments.of(36, "IdEmisor one blank is not empty: it is compared and matches nothing", NINGUNA),
                Arguments.of(37, "unknown keys of the filter are ignored", l(3001L)),
                Arguments.of(38, "the p_Solicitudes of the request are ignored: only the filters count", l(4003L, 4004L)),
                Arguments.of(39, "IdCarga 0 ignores the detail rows and does not repeat an id for its two rows (DISTINCT)", TODAS),
                Arguments.of(40, "IdCarga 900 selects the pending rows of the load", l(4001L, 4002L)),
                Arguments.of(41, "IdCarga 901: the PEN row yes, the ERR row no", l(4003L)),
                Arguments.of(42, "IdCarga 902: two PEN rows of one solicitud give ONE id", l(4001L)),
                Arguments.of(43, "IdCarga 903: the PRO row does not count", l(4001L)),
                Arguments.of(44, "IdCarga 999 (no such load) selects nothing", NINGUNA),
                Arguments.of(45, "IdCarga absent is null: the OR is false, nothing selected", NINGUNA),
                Arguments.of(46, "IdCarga explicit null: nothing selected", NINGUNA),
                Arguments.of(47, "IdCarga 900 AND IdEmisor AGF2", l(4002L)),
                Arguments.of(48, "a PEN detail of a solicitud already certified (CRT) is not selected", l(4001L)),
                Arguments.of(49, "only ING and ACP are selected (CRT, ANU, REC, LIQ are not)", l(4001L, 4002L)),
                Arguments.of(50, "an ACP solicitud alone is selected", l(4002L)),
                Arguments.of(51, "nothing in ING or ACP", NINGUNA),
                Arguments.of(52, "the date of the request picks the solicitudes of that FechaCertificacion", l(4001L)),
                Arguments.of(53, "the request with another date picks the solicitud of that date", l(4002L)),
                Arguments.of(54, "a date with no solicitud selects nothing", NINGUNA),
                Arguments.of(55, "a solicitud of company 2 is not selected for company 1", l(4001L)));
    }

    @ParameterizedTest(name = "FM-CXF-02 case {0}: {1}")
    @MethodSource("selecciones")
    @DisplayName("FM-CXF-02 (matriz C01/C02/C05): what the filters select reaches Certificar, in this order")
    void laSeleccionDeLosFiltros(int numero, String que, List<Long> esperados) throws Exception {
        CasoCerPf.Corrida c = correr(numero);
        assertThat(CasoCerPf.llamoACertificar(c)).as("Certificar is always called once, even with an empty list").isTrue();
        assertThat(CasoCerPf.idsHaciaCertificar(c)).as(que).isEqualTo(esperados);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-03: several filters, concatenated, never de-duplicated
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-03 (matriz C03): two filters are concatenated in the ORDER OF THE FILTERS (RES then FM002A: 3001, 4003, 4004; the other way round: 4003, 4004, 3001)")
    void losFiltrosSeConcatenanEnSuOrden() throws Exception {
        assertThat(CasoCerPf.idsHaciaCertificar(correr(5))).isEqualTo(l(3001L, 4003L, 4004L));
        assertThat(CasoCerPf.idsHaciaCertificar(correr(6))).isEqualTo(l(4003L, 4004L, 3001L));
        assertThat(CasoCerPf.idsHaciaCertificar(correr(10))).as("three filters: IdEmisor ZZZ selects nothing, AGF1 + APO selects 4001 and 4003, IdMoneda EUR selects nothing").isEqualTo(l(4001L, 4003L));
    }

    @Test
    @DisplayName("FM-CXF-03 (matriz C03): the SAME filter twice repeats every id, an overlap repeats the shared id, and Certificar rejects the duplicates with counter 1 and no result")
    void losDuplicadosNoSeQuitan() throws Exception {
        assertThat(CasoCerPf.idsHaciaCertificar(correr(7))).isEqualTo(l(4003L, 4004L, 4003L, 4004L));
        assertThat(CasoCerPf.idsHaciaCertificar(correr(8))).as("emisor AGF2 then serie FM002A: 4004 is in both").isEqualTo(l(4002L, 4004L, 4003L, 4004L));
        assertThat(CasoCerPf.idsHaciaCertificar(correr(9))).isEqualTo(l(3001L, 4001L, 4002L, 4003L, 4004L, 3001L, 4001L, 4002L, 4003L, 4004L));
        for (int n : new int[] { 7, 8, 9, 75 }) {
            CasoCerPf.Corrida c = correr(n);
            assertThat(c.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).as("case " + n + " counter").isEqualTo(1L);
            assertThat(c.salida.getAsJsonArray("P_OUT_RESULTADO")).as("case " + n + " results").isEmpty();
            assertThat(c.volcado.get("decisionServlet").getAsString()).as("case " + n).isEqualTo("ROLLBACK");
        }
        assertThat(CasoCerPf.idsHaciaCertificar(correr(75))).as("derived from T5-902 with two filters").isEqualTo(l(4001L, 4001L));
    }

    @Test
    @DisplayName("FM-CXF-03: the id that T5-902 repeats in the LIST of Certificar comes out once here, by the DISTINCT of the select, and the run certifies it (counter 0, COMMIT)")
    void elDistinctColapsaElIdRepetidoDeLaVista() throws Exception {
        CasoCerPf.Corrida c = correr(74);
        assertThat(CasoCerPf.idsHaciaCertificar(c)).isEqualTo(l(4001L));
        assertThat(c.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(c.volcado.get("decisionServlet").getAsString()).isEqualTo("COMMIT");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-04: no filters or no rows -> Certificar with an EMPTY list (counter 1, NO_SE_INGRESARON_SOLICITUDES)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-04 (matriz C04): p_Filtros empty or absent: Certificar IS called with an empty list and answers counter 1 / NO_SE_INGRESARON_SOLICITUDES; the host rolls back")
    void sinFiltrosCertificarSeLlamaConListaVacia() throws Exception {
        for (int n : new int[] { 11, 12, 2, 68 }) {
            CasoCerPf.Corrida c = correr(n);
            assertThat(CasoCerPf.idsHaciaCertificar(c)).as("case " + n).isEmpty();
            assertThat(c.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).as("case " + n).isEqualTo(1L);
            JsonArray mensajes = c.salida.getAsJsonArray("P_OUT_MENSAJES");
            assertThat(mensajes).as("case " + n).hasSize(1);
            assertThat(mensajes.get(0).getAsJsonObject().get("codigo").getAsString()).as("case " + n).isEqualTo("NO_SE_INGRESARON_SOLICITUDES");
            assertThat(c.salida.getAsJsonArray("P_OUT_RESULTADO")).as("case " + n).isEmpty();
            assertThat(c.volcado.get("decisionServlet").getAsString()).as("case " + n).isEqualTo("ROLLBACK");
        }
    }

    @Test
    @DisplayName("FM-CXF-04 (matriz C04): p_Filtros null raises NullPointerException BEFORE Certificar is called (no record of the call, host: 500)")
    void filtrosNuloLanzaAntesDeCertificar() throws Exception {
        CasoCerPf.Corrida c = correr(13);
        assertThat(c.excepcionTexto).startsWith("NullPointerException");
        assertThat(CasoCerPf.llamoACertificar(c)).isFalse();
        assertThat(c.volcado.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_HTTP500");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-05: the header is copied, the two fields that are not copied keep the defaults of Certificar
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-05: the header of the request reaches Certificar field by field (usuario 21, modulo APV, reglas S, canal OWP), and p_NotificaOWP / p_CertificacionAporteSwitchSimulado keep S / N")
    void laCabeceraSeCopia() throws Exception {
        JsonObject e = CasoCerPf.entradaDeCertificar(correr(61));
        assertThat(e.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(e.get("p_IdUsuario").getAsLong()).isEqualTo(21L);
        assertThat(e.get("p_IdModulo").getAsString()).isEqualTo("APV");
        assertThat(e.get("p_SimularOperacion").getAsString()).isEqualTo("N");
        assertThat(e.get("p_UsaReglasNegocio").getAsString()).isEqualTo("S");
        assertThat(e.get("p_FechaCertificacion").getAsString()).isEqualTo("2026-03-02");
        assertThat(e.get("p_IdTipoCanalIngreso").getAsString()).isEqualTo("OWP");
        assertThat(e.get("p_NotificaOWP").getAsString()).isEqualTo("S");
        assertThat(e.get("p_CertificacionAporteSwitchSimulado").getAsString()).isEqualTo("N");
        assertThat(CasoCerPf.idsHaciaCertificar(correr(61))).isEqualTo(l(4001L, 4003L));
    }

    @Test
    @DisplayName("FM-CXF-05: p_SimularOperacion is copied as it comes: S, empty, absent (null), lower case s; a missing canal stays null, an empty one stays empty")
    void simularYCanalSeCopianTalCual() throws Exception {
        assertThat(CasoCerPf.entradaDeCertificar(correr(57)).get("p_SimularOperacion").getAsString()).isEqualTo("S");
        assertThat(CasoCerPf.entradaDeCertificar(correr(58)).get("p_SimularOperacion").getAsString()).isEmpty();
        assertThat(CasoCerPf.entradaDeCertificar(correr(59)).get("p_SimularOperacion").isJsonNull()).isTrue();
        assertThat(CasoCerPf.entradaDeCertificar(correr(60)).get("p_SimularOperacion").getAsString()).isEqualTo("s");
        assertThat(CasoCerPf.entradaDeCertificar(correr(65)).get("p_IdTipoCanalIngreso").isJsonNull()).as("the request carries no canal").isTrue();
        assertThat(CasoCerPf.entradaDeCertificar(correr(62)).get("p_IdTipoCanalIngreso").getAsString()).isEmpty();
    }

    @Test
    @DisplayName("FM-CXF-05 / FM-CER-68: p_SimularOperacion S: Certificar runs for real, answers counter 1 and the host rolls back; empty or lower case s is NOT a simulation (counter 0, COMMIT)")
    void laSimulacionLaDecideCertificar() throws Exception {
        CasoCerPf.Corrida s = correr(57);
        assertThat(s.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(s.salida.getAsJsonArray("P_OUT_RESULTADO")).hasSize(2);
        assertThat(s.volcado.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK");
        for (int n : new int[] { 58, 59, 60 }) {
            CasoCerPf.Corrida c = correr(n);
            assertThat(c.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).as("case " + n).isZero();
            assertThat(c.volcado.get("decisionServlet").getAsString()).as("case " + n).isEqualTo("COMMIT");
        }
    }

    @Test
    @DisplayName("FM-CXF-05: a header with null fields is copied as null: the legacy raises NullPointerException (company null reaches the parameters of Certificar) after the select ran")
    void cabeceraNulaSeCopiaYCertificarLanza() throws Exception {
        for (int n : new int[] { 63, 64 }) {
            CasoCerPf.Corrida c = correr(n);
            assertThat(c.excepcionTexto).as("case " + n).satisfiesAnyOf(t -> assertThat(t).startsWith("NullPointerException"), t -> assertThat(t).startsWith("Exception: Parametro ").endsWith(" no encontrado"));
            assertThat(CasoCerPf.idsHaciaCertificar(c)).as("case " + n + ": the select of an absent company / date finds nothing").isEmpty();
            assertThat(c.volcado.get("decisionServlet").getAsString()).as("case " + n).isEqualTo("ROLLBACK_HTTP500");
        }
        CasoCerPf.Corrida otra = correr(56);
        assertThat(otra.excepcionTexto).as("company 2 has no configuration: Certificar raises (the legacy NullPointerException; the kit's 'Parametro ... no encontrado' is the known DESVIO-KIT-1)")
                .satisfiesAnyOf(t -> assertThat(t).startsWith("NullPointerException"), t -> assertThat(t).startsWith("Exception: Parametro ").endsWith(" no encontrado"));
        assertThat(CasoCerPf.idsHaciaCertificar(otra)).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-01 / 05: what Certificar answers is returned as it is
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-01 / FM-CXF-05: counter, messages and results of Certificar are returned unchanged: clean aporte (4001 UI_EXITO APD), clean rescate, a lot of both in the order Certificar gives")
    void laSalidaDeCertificarSeDevuelveTalCual() throws Exception {
        CasoCerPf.Corrida a = correr(65);
        assertThat(a.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(a.salida.getAsJsonArray("P_OUT_MENSAJES")).isEmpty();
        JsonObject r = a.salida.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject();
        assertThat(r.get("IdSolicitudAporteRescate").getAsLong()).isEqualTo(4001L);
        assertThat(r.get("Codigo").getAsString()).isEqualTo("UI_EXITO");
        assertThat(r.get("Estado").getAsString()).isEqualTo("APD");
        assertThat(correr(66).salida.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong()).isEqualTo(3001L);
        JsonArray lote = correr(67).salida.getAsJsonArray("P_OUT_RESULTADO");
        assertThat(lote).hasSize(2);
        assertThat(lote.get(0).getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong()).as("the aporte first, as Certificar orders them").isEqualTo(4001L);
        assertThat(lote.get(1).getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong()).isEqualTo(3001L);
    }

    @Test
    @DisplayName("FM-CXF-05: the errors of Certificar are returned as they are: counter 1 with the custody error message and the rejected row RZD UI_ERROR_CUSTODIA_APORTE; the host rolls back")
    void losErroresDeCertificarSeDevuelven() throws Exception {
        CasoCerPf.Corrida c = correr(69);
        assertThat(CasoCerPf.idsHaciaCertificar(c)).isEqualTo(l(4001L, 4002L));
        assertThat(c.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(c.salida.getAsJsonArray("P_OUT_MENSAJES")).hasSize(1);
        assertThat(c.salida.getAsJsonArray("P_OUT_MENSAJES").get(0).toString()).contains("UI_ERROR_CUSTODIA_APORTE");
        JsonArray res = c.salida.getAsJsonArray("P_OUT_RESULTADO");
        assertThat(res.get(0).getAsJsonObject().get("Codigo").getAsString()).isEqualTo("UI_EXITO");
        assertThat(res.get(1).getAsJsonObject().get("Codigo").getAsString()).isEqualTo("UI_ERROR_CUSTODIA_APORTE");
        assertThat(res.get(1).getAsJsonObject().get("Estado").getAsString()).isEqualTo("RZD");
        assertThat(c.volcado.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK");
        assertThat(correr(70).salida.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject().get("Codigo").getAsString()).isEqualTo("UI_ONMIBUS_NO_CREADO");
    }

    @Test
    @DisplayName("FM-CXF-05: the counter of Certificar is returned as it is, also above 1 (2 with OWP; 2 accumulated by the collaborators; the child of a switch adds its own)")
    void elContadorSeDevuelveTalCual() throws Exception {
        assertThat(correr(71).salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(2L);
        assertThat(correr(72).salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(2L);
        CasoCerPf.Corrida hijo = correr(76);
        assertThat(hijo.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(2L);
        assertThat(hijo.salida.getAsJsonArray("P_OUT_RESULTADO")).as("the rows of the child come FIRST (both 4001 rows, then 3001)").hasSize(3);
        assertThat(hijo.salida.getAsJsonArray("P_OUT_MENSAJES")).hasSize(2);
        CasoCerPf.Corrida sim = correr(77);
        assertThat(sim.salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(sim.salida.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong()).as("simulated child rows come out with id 0").isZero();
        assertThat(correr(73).salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
    }

    @Test
    @DisplayName("FM-CXF-05: an exception of Certificar goes out UNCHANGED (class and message of the plain Exception), the host answers 500 and rolls back; the record of the call says error")
    void lasExcepcionesDeCertificarSalenSinCambios() throws Exception {
        String[][] esperadas = { { "78", "Exception: FALLA_NOTIFICACION_ASINCRONA" }, { "79", "Exception: Parametro PLA.CallBatchURL en modulo PLATAFORMA no encontrado" },
                { "80", "NullPointerException" }, { "81", "NullPointerException" }, { "82", "Exception: key field is not unique" },
                { "83", "Exception: Parametro TipoComprobanteFacturaNoApv en modulo FONDOMUTUO no encontrado" }, { "84", "Exception: FALLA_NOTIFICACION_DEL_HIJO" },
                { "85", "Exception: FALLA_SPOT" }, { "86", "Exception: FALLA_TESORERIA" } };
        for (String[] e : esperadas) {
            CasoCerPf.Corrida c = correr(Integer.parseInt(e[0]));
            assertThat(c.excepcionTexto).as("case " + e[0]).startsWith(e[1]);
            assertThat(c.volcado.get("decisionServlet").getAsString()).as("case " + e[0]).isEqualTo("ROLLBACK_HTTP500");
            assertThat(c.salida).as("case " + e[0] + ": no output").isNull();
        }
    }
}
