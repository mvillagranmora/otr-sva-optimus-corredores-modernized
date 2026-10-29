package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;

/**
 * Rule by rule, with LITERAL values: every card of FM-CER-21, 22, 24..28 (front F-C, aportes) and FM-CER-31..41 (front F-D, rescates: custody and costs) of RULES_FONDOMUTUO_F3_B.md 8.1
 * and the defects they pin (D-F3B-03/05/06/08/09/10/11/37, D-NOR-9/11/16, D-COM-1/5, D-FEC-6) against the units of F-C and F-D (see {@code API_ASSUMED_T2.md}). The expected values are
 * the ones the unmodified legacy produced when the goldens were recorded: they are written here by hand from the golden of the case (each test names the case), so a change of the golden that
 * is not a change of the rule shows up here. Each test runs the case through {@link CasoCerT2} (F-A and F-B of T1 REAL, then the five steps of F-C and F-D) and asserts on the
 * {@link CertificarAporteRescateContexto} that F-D leaves for the following fronts, and, where the legacy shows it only in a later call, on the golden itself.
 *
 * <p>What the legacy does not show in any collaborator call or in the UPDATE (for example {@code TieneMontoRetencion} of a rescate paid to the cuenta de inversion) is pinned here with the value
 * the rule gives and marked "(rule; the golden does not show it)": it is the only place where the expected value is not read in an executed run, and the question list of the
 * notes names it.
 *
 * <p>RED BEFORE CODE: until the units exist every test fails with "T2 not implemented: the production class ... does not exist yet".
 */
class CerT2ReglasTest {

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** The context after F-A, F-B, F-C and F-D of the case; F-C and F-D must have finished without an exception. */
    private static CertificarAporteRescateContexto ctx(String slug) throws Exception {
        CasoCerT2.Corrida c = CasoCerT2.ejecutar(CasoCerT2.idPorSlug(slug));
        assertNotNull(c.contexto, slug + " must reach the seam; it ended with " + c.excepcion);
        assertTrue(c.terminoF_C_F_D(), slug + " must finish F-C and F-D; it raised " + c.excepcion);
        return c.contexto;
    }

    /** What F-C / F-D raised in a case in which the legacy throws inside them, as {@code Class: message}. */
    private static String lanza(String slug) throws Exception {
        CasoCerT2.Corrida c = CasoCerT2.ejecutar(CasoCerT2.idPorSlug(slug));
        assertNotNull(c.contexto, slug + " must reach the seam; it ended with " + c.excepcion);
        assertFalse(c.terminoF_C_F_D(), slug + " must raise inside F-C / F-D");
        return CasoCer.texto(c.excepcion);
    }

    private static SolicitudProceso sol(CertificarAporteRescateContexto ctx, long id) {
        return ctx.solicitudes.stream().filter(s -> s.IdSolicitudAporteRescate == id).findFirst().orElseThrow(() -> new AssertionError("solicitud " + id + " not read"));
    }

    private static void igual(String esperado, BigDecimal real, String donde) {
        assertNotNull(real, donde + " is null, expected " + esperado);
        assertEquals(0, new BigDecimal(esperado).compareTo(real), donde + ": expected " + esperado + " was " + real.toPlainString());
    }

    /** state:code of the solicitud after F-C / F-D */
    private static String est(CertificarAporteRescateContexto ctx, long id) {
        SolicitudProceso s = sol(ctx, id);
        return s.EstadoProceso + ":" + s.Codigo;
    }

    private static JsonObject golden(String slug) {
        return CasoCerT2.golden(CasoCerT2.idPorSlug(slug));
    }

    /** the calls of the golden to a collaborator, in order */
    private static List<JsonObject> llamadas(String slug, String colaborador) {
        return CerCosturaT2.llamadas(golden(slug).getAsJsonArray("colaboradores"), colaborador);
    }

    private static JsonObject arg(JsonObject llamada) {
        return llamada.getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    /** "idPortafolio:moneda" of the pairs sent to SaldoFlujoCaja in the call number {@code k} */
    private static List<String> pares(String slug, int k) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : arg(llamadas(slug, "SaldoFlujoCaja").get(k)).getAsJsonArray("P_PORTAFOLIOACONSULTAR")) {
            out.add(e.getAsJsonObject().get("IdPortafolio").getAsString() + ":" + e.getAsJsonObject().get("IdMoneda").getAsString());
        }
        return out;
    }

    private static JsonObject mov0(String slug, int llamada) {
        return arg(llamadas(slug, "ProcesarCustodiaFondoMutuo").get(llamada)).getAsJsonArray("p_Movimientos").get(0).getAsJsonObject();
    }

    private static JsonObject entradaTesoreria(String slug, long idInterno) {
        for (JsonObject l : llamadas(slug, "ProcesarIngresoEgresoTesoreria")) {
            for (JsonElement e : arg(l).getAsJsonArray("p_Solicitudes")) {
                if (e.getAsJsonObject().get("IdInterno").getAsLong() == idInterno) return e.getAsJsonObject();
            }
        }
        return null;
    }

    private static void numero(String esperado, JsonObject o, String campo) {
        assertTrue(o != null && o.has(campo) && !o.get(campo).isJsonNull(), campo + " absent");
        assertEquals(0, new BigDecimal(esperado).compareTo(o.get(campo).getAsBigDecimal()), campo + ": expected " + esperado + " golden " + o.get(campo));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-21 saldo de la cuenta de inversion (canal distinto de PAC)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-21 an aporte whose total fits the saldo (500.000 against 300.000) is not rejected; SaldoFlujoCaja is asked for (500, CLP) on the effective date with the use CERTIFICACION_APORTE_FMM")
    void fmCer21_saldoSuficiente() throws Exception {
        CertificarAporteRescateContexto c = ctx("saldo-suficiente-no-pac");
        assertEquals("PEN:", est(c, 3001));
        assertEquals(List.of("500:CLP"), pares("saldo-suficiente-no-pac", 0));
        JsonObject a = arg(llamadas("saldo-suficiente-no-pac", "SaldoFlujoCaja").get(0));
        assertEquals("CERTIFICACION_APORTE_FMM", a.get("P_USO").getAsString());
        assertEquals("2026-03-02", a.get("P_FECHADESDE").getAsString());
        assertEquals("2026-03-02", a.get("P_FECHAHASTA").getAsString());
        assertEquals("N", a.get("P_DESPLEGARENUI").getAsString());
        assertEquals("N", a.get("P_AGRUPARPORORIGEN").getAsString());
        assertEquals(1, llamadas("saldo-suficiente-no-pac", "SaldoFlujoCaja").size());
    }

    @Test
    @DisplayName("FM-CER-21 total 700.000 against saldo 500.000: RZD UI_SALDO_INSUFICIENTE_FLUJO_CAJA; the other portfolio (100.000 against 500.000) passes; the counter is NOT raised (the rejection only lives in the answer)")
    void fmCer21_saldoInsuficienteRechaza() throws Exception {
        CertificarAporteRescateContexto c = ctx("saldo-insuficiente-rechaza");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3001));
        assertEquals("PEN:", est(c, 3002));
        assertEquals(0L, c.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals(List.of("500:CLP", "501:CLP"), pares("saldo-insuficiente-rechaza", 0));
    }

    @Test
    @DisplayName("FM-CER-21 the balance is NOT progressive: two aportes of 300.000 against 500.000 (total 600.000) are BOTH rejected; two of 200.000 and 300.000 (total = saldo) both pass (only total > saldo rejects)")
    void fmCer21_noEsProgresivo() throws Exception {
        CertificarAporteRescateContexto c = ctx("dos-aportes-del-mismo-par-rechazan-a-ambos");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3001));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3002));
        assertEquals(List.of("500:CLP"), pares("dos-aportes-del-mismo-par-rechazan-a-ambos", 0));
        CertificarAporteRescateContexto ok = ctx("dos-aportes-del-mismo-par-uno-basta-pasan-ambos");
        assertEquals("PEN:", est(ok, 3001));
        assertEquals("PEN:", est(ok, 3002));
        assertEquals("PEN:", est(ctx("saldo-igual-al-total-no-rechaza"), 3001));
    }

    @Test
    @DisplayName("FM-CER-20/21 OperaSaldoNegativo: S does not reject; absent is N and rejects; 'n' (equalsIgnoreCase) rejects; 's' is not N and does not reject")
    void fmCer21_operaSaldoNegativo() throws Exception {
        assertEquals("PEN:", est(ctx("opera-saldo-negativo-S-no-rechaza-aunque-no-alcance"), 3001));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(ctx("opera-saldo-negativo-ausente-es-N-y-rechaza"), 3001));
        CertificarAporteRescateContexto c = ctx("opera-saldo-negativo-n-minuscula-rechaza-s-minuscula-no");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3001));
        assertEquals("PEN:", est(c, 3002));
        assertEquals("s", sol(c, 3002).OperaSaldoNegativo);
    }

    @Test
    @DisplayName("FM-CER-21 only the aportes that move the cuenta (MueveCta S), of manual origin (M) and whose portfolio has no line of overdraft (sobregiro S) are sent to the flow: the others are never asked for")
    void fmCer21_quienEntraAlFlujo() throws Exception {
        CertificarAporteRescateContexto c = ctx("sobregiro-S-saca-el-portafolio-del-flujo");
        assertEquals("PEN:", est(c, 3001));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3002));
        assertEquals(List.of("501:CLP"), pares("sobregiro-S-saca-el-portafolio-del-flujo", 0));
        assertEquals(0, llamadas("sin-mueve-cuenta-no-consulta-flujo", "SaldoFlujoCaja").size());
        assertEquals(0, llamadas("origen-no-manual-no-consulta-flujo", "SaldoFlujoCaja").size());
        assertEquals("PEN:", est(ctx("sin-mueve-cuenta-no-consulta-flujo"), 3001));
        assertEquals("PEN:", est(ctx("origen-no-manual-no-consulta-flujo"), 3001));
        CertificarAporteRescateContexto r = ctx("aporte-rechazado-por-F-A-no-entra-al-flujo");
        assertEquals("RZD:UI_ESTADO_INCORRECTO_CERTIFICACION", est(r, 3002));
        assertEquals("PEN:", est(r, 3001), "the 900.000 of the aporte rejected by F-A must not be added to the total of the pair (400.000 against 500.000)");
    }

    @Test
    @DisplayName("FM-CER-21 / D-NOR-16 the pair (500, USD) is NOT sent when portfolio 500 and currency USD were already seen (two independent lists): its saldo is 0 and its aporte is rejected; with a row in the answer it passes")
    void fmCer21_paresOmitidosPorListasIndependientes() throws Exception {
        CertificarAporteRescateContexto c = ctx("pares-omitidos-por-listas-independientes-quedan-con-saldo-cero");
        assertEquals(List.of("500:CLP", "501:USD"), pares("pares-omitidos-por-listas-independientes-quedan-con-saldo-cero", 0));
        assertEquals("PEN:", est(c, 3001));
        assertEquals("PEN:", est(c, 3002));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3003));
        CertificarAporteRescateContexto d = ctx("par-omitido-con-fila-de-saldo-en-la-respuesta-pasa");
        assertEquals("PEN:", est(d, 3003));
        assertEquals("PEN:", est(ctx("monedas-distintas-del-mismo-portafolio-totales-separados"), 3001));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(ctx("monedas-distintas-del-mismo-portafolio-totales-separados"), 3002));
    }

    @Test
    @DisplayName("FM-CER-18/21 the saldo is read on the later of FechaCertificacion and PLA.FechaSistema (2026-02-27 and 2026-03-05 against the system date 2026-03-02); the LAST row of the pair on that date counts; rows of another date or no row = saldo 0")
    void fmCer21_fechaYFilaDelSaldo() throws Exception {
        assertEquals("PEN:", est(ctx("fecha-de-certificacion-anterior-al-sistema-usa-la-fecha-de-sistema"), 3001));
        assertEquals("2026-03-02", arg(llamadas("fecha-de-certificacion-anterior-al-sistema-usa-la-fecha-de-sistema", "SaldoFlujoCaja").get(0)).get("P_FECHADESDE").getAsString());
        assertEquals("PEN:", est(ctx("fecha-de-certificacion-posterior-al-sistema-usa-su-propia-fecha"), 3001));
        assertEquals("2026-03-05", arg(llamadas("fecha-de-certificacion-posterior-al-sistema-usa-su-propia-fecha", "SaldoFlujoCaja").get(0)).get("P_FECHAHASTA").getAsString());
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(ctx("varias-filas-del-mismo-par-gana-la-ultima"), 3001));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(ctx("fila-de-otra-fecha-no-cuenta"), 3001));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(ctx("respuesta-sin-filas-saldo-cero-rechaza"), 3001));
    }

    @Test
    @DisplayName("FM-CER-19/21 the aportes reach the flow in ascending id whatever the order of the request (3003, 3001, 3002 -> portfolios 502, 501, 500)")
    void fmCer21_ordenAscendente() throws Exception {
        ctx("ids-invertidos-el-flujo-ve-los-aportes-por-id-ascendente");
        assertEquals(List.of("502:CLP", "501:CLP", "500:CLP"), pares("ids-invertidos-el-flujo-ve-los-aportes-por-id-ascendente", 0));
    }

    @Test
    @DisplayName("FM-CER-21 an exception of SaldoFlujoCaja comes out untouched (plain Exception with its message)")
    void fmCer21_falloDelFlujoSePropaga() throws Exception {
        assertEquals("Exception: flujo de caja no disponible", lanza("el-flujo-de-caja-falla-y-propaga"));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-22 canal PAC (D-F3B-11: the validation is duplicated)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-22 PAC aportes are asked in their own call (P_USO CERTIFICACION_APORTE_FMM_PAC): 300.000 against 500.000 passes; two of 300.000 are both rejected")
    void fmCer22_pac() throws Exception {
        assertEquals("PEN:", est(ctx("pac-saldo-suficiente"), 3001));
        assertEquals("CERTIFICACION_APORTE_FMM_PAC", arg(llamadas("pac-saldo-suficiente", "SaldoFlujoCaja").get(0)).get("P_USO").getAsString());
        assertEquals(1, llamadas("pac-saldo-suficiente", "SaldoFlujoCaja").size());
        CertificarAporteRescateContexto c = ctx("pac-saldo-insuficiente-rechaza");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3001));
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3002));
        assertEquals("PEN:", est(ctx("pac-opera-saldo-negativo-S-no-rechaza"), 3001));
        assertEquals(0, llamadas("pac-sobregiro-S-no-consulta", "SaldoFlujoCaja").size());
    }

    @Test
    @DisplayName("FM-CER-22 / D-F3B-11 the PAC and the not PAC aportes of the same portfolio and currency are NOT added together (300.000 + 300.000 against 500.000 each: both pass) and make TWO calls, not PAC first; each call reads its own answer")
    void fmCer22_pacYNoPacNoSeSuman() throws Exception {
        CertificarAporteRescateContexto c = ctx("pac-y-no-pac-del-mismo-par-no-se-suman-entre-si");
        assertEquals("PEN:", est(c, 3001));
        assertEquals("PEN:", est(c, 3002));
        List<JsonObject> l = llamadas("pac-y-no-pac-del-mismo-par-no-se-suman-entre-si", "SaldoFlujoCaja");
        assertEquals(2, l.size());
        assertEquals("CERTIFICACION_APORTE_FMM", arg(l.get(0)).get("P_USO").getAsString());
        assertEquals("CERTIFICACION_APORTE_FMM_PAC", arg(l.get(1)).get("P_USO").getAsString());
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(ctx("pac-ignora-el-saldo-de-la-llamada-no-pac"), 3001));
        CertificarAporteRescateContexto p = ctx("pac-pares-omitidos-por-listas-independientes");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(p, 3003));
        assertEquals("Exception: flujo PAC no disponible", lanza("el-flujo-pac-falla-despues-del-no-pac"));
        assertEquals(2, llamadas("el-flujo-pac-falla-despues-del-no-pac", "SaldoFlujoCaja").size());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-23 format of the aportes, right after the saldo
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-23 the amounts of ALL the aportes are formatted after the saldo: fund in USD (2 decimals) 1356.67 / commission 12.3456 -> 12.35; local CLP (0 decimals) 11111.5 -> 11112 and 100000.6 -> 100001")
    void fmCer23_formatoDeLosAportes() throws Exception {
        CertificarAporteRescateContexto c = ctx("formato-de-los-aportes-fondo-usd-tras-el-saldo");
        SolicitudProceso a = sol(c, 3001);
        igual("1356.67", a.Monto, "Monto 3001 (USD)");
        igual("12.35", a.MontoComision, "MontoComision 3001 (USD)");
        igual("11112", a.MontoComisionLocal, "MontoComisionLocal 3001 (CLP)");
        igual("1234567", a.CostoMonedaLocal, "CostoMonedaLocal 3001");
        SolicitudProceso b = sol(c, 3002);
        igual("100001", b.Monto, "Monto 3002");
        igual("100001", b.CostoMonedaLocal, "CostoMonedaLocal 3002");
        CertificarAporteRescateContexto d = ctx("el-formato-alcanza-tambien-al-aporte-rechazado-por-saldo");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(d, 3001));
        igual("1001", sol(d, 3002).Monto, "Monto 3002 formatted although 3001 was rejected");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-24..26 costo UF of the reinversion aportes (D-F3B-10, D-NOR-11)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-26 the regimes APV-A, BF, DC-DL, DC-AL, DC-R (any case) inherit: CostoUF = MontoMonedaSistema / ValorUFRescate = 1.000.000 / (1.200.000 / 40) = 33.33333333333333 (DECIMAL64) and ValorUFFechaInversion = the one of the rescate (39000.5); no CalcularParidad")
    void fmCer26_herencia() throws Exception {
        for (String r : new String[] { "apv-a", "bf", "dc-dl", "dc-al", "dc-r", "minuscula-bf", "minuscula-dc-dl" }) {
            String slug = "reinversion-hereda-costo-uf-regimen-" + r;
            CertificarAporteRescateContexto c = ctx(slug);
            SolicitudProceso a = sol(c, 3002);
            igual("33.33333333333333", a.CostoUF, slug + " CostoUF");
            igual("39000.5", a.ValorUFFechaInversion, slug + " ValorUFFechaInversion");
            assertEquals(0, llamadas(slug, "CalcularParidad").stream().filter(l -> arg(l).get("p_IdTrabajo").getAsLong() == 1).count(), slug + " :: the UF parity is not asked for when the cost is inherited");
        }
    }

    @Test
    @DisplayName("FM-CER-26 IndicadorMLT S with the aporte and the rescate in regime 18Q (any case) inherits; MLT S with a rescate that is not 18Q, or MLT N, goes to the parity of the UF: CostoUF = 1.000.000 / 39500.25 = 25.31629546648439, ValorUFFechaInversion = 39500.25")
    void fmCer26_mltY18Q() throws Exception {
        igual("33.33333333333333", sol(ctx("reinversion-mlt-S-y-ambos-18Q-hereda"), 3002).CostoUF, "MLT S, 18Q both");
        igual("33.33333333333333", sol(ctx("reinversion-mlt-S-y-18q-minuscula-hereda"), 3002).CostoUF, "MLT s, 18q");
        for (String slug : new String[] { "reinversion-mlt-S-pero-el-rescate-no-es-18Q-usa-paridad", "reinversion-mlt-N-regimen-18Q-usa-paridad", "reinversion-otro-regimen-usa-la-paridad-de-la-uf" }) {
            SolicitudProceso a = sol(ctx(slug), 3002);
            igual("25.31629546648439", a.CostoUF, slug + " CostoUF");
            igual("39500.25", a.ValorUFFechaInversion, slug + " ValorUFFechaInversion");
        }
        JsonObject p = arg(llamadas("reinversion-otro-regimen-usa-la-paridad-de-la-uf", "CalcularParidad").stream().filter(l -> arg(l).get("p_IdTrabajo").getAsLong() == 1).findFirst().orElseThrow());
        JsonObject e = p.getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject();
        assertEquals("UF", e.get("IdMonedaOrigen").getAsString());
        assertEquals("CLP", e.get("IdMonedaDestino").getAsString());
        assertEquals("2026-03-02", e.get("Fecha").getAsString());
        assertEquals(1, e.get("IdInterno").getAsLong());
    }

    @Test
    @DisplayName("FM-CER-24 / D-F3B-10 / D-NOR-11 every error of the recalculation is swallowed and the aporte keeps its values: a parity without rows (NoSuchElementException), a failing parity, an origin without ValorUFFechaInversion or without CostoUF")
    void fmCer24_erroresSeTragan() throws Exception {
        for (String slug : new String[] { "reinversion-paridad-sin-salida-traga-el-error-y-no-cambia", "reinversion-paridad-error-de-colaborador-se-traga", "reinversion-origen-sin-valor-uf-no-cambia",
                "reinversion-origen-sin-costo-uf-no-cambia" }) {
            SolicitudProceso a = sol(ctx(slug), 3002);
            igual("0", a.CostoUF, slug + " CostoUF unchanged");
            igual("38000.5", a.ValorUFFechaInversion, slug + " ValorUFFechaInversion unchanged");
        }
    }

    @Test
    @DisplayName("FM-CER-26 / D-F3B-10 / D-F3B-40 a parity answer without MontoLocal: CostoUF = MontoMonedaSistema / the ValorUFFechaInversion of the aporte itself (1.000.000 / 38000.5 = 26.3154432178524) and ValorUFFechaInversion becomes NULL (HANA: NOT NULL, the UPDATE of the legacy fails later)")
    void fmCer26_paridadSinMontoLocal() throws Exception {
        SolicitudProceso a = sol(ctx("reinversion-paridad-sin-monto-local-usa-el-valor-uf-propio"), 3002);
        igual("26.3154432178524", a.CostoUF, "CostoUF with the own ValorUFFechaInversion");
        assertNull(a.ValorUFFechaInversion, "ValorUFFechaInversion = the (null) MontoLocal of the parity");
        assertTrue(golden("reinversion-paridad-sin-monto-local-usa-el-valor-uf-propio").get("excepcion").getAsString().startsWith("SQLException"), "the UPDATE of the legacy fails afterwards (a later front)");
    }

    @Test
    @DisplayName("FM-CER-24 / D-F3B-10 the origin is found with findFirst().get(): an aporte whose origin does not exist aborts the recalculation of ALL the following aportes (3004 keeps CostoUF 0 although its origin exists); with valid origins each aporte is computed in order (33.33333333333333 and 25)")
    void fmCer24_origenInexistenteAbortaElLote() throws Exception {
        CertificarAporteRescateContexto c = ctx("reinversion-origen-inexistente-aborta-el-lote-de-aportes");
        igual("0", sol(c, 3002).CostoUF, "3002 (origin 3999 does not exist)");
        igual("0", sol(c, 3004).CostoUF, "3004 (valid origin, never reached)");
        CertificarAporteRescateContexto d = ctx("reinversion-un-origen-valido-y-otro-despues-se-procesan-en-orden");
        igual("33.33333333333333", sol(d, 3002).CostoUF, "3002");
        igual("25", sol(d, 3004).CostoUF, "3004 = 1.000.000 / (1.000.000 / 25)");
        igual("38800.5", sol(d, 3004).ValorUFFechaInversion, "3004 inherits the UF value of its origin");
        igual("25.6407", sol(ctx("aporte-no-reinversion-se-salta-aunque-tenga-relacionado"), 3002).CostoUF, "an aporte that is not APOREINV keeps the CostoUF of F-B (FM-CER-17)");
    }

    @Test
    @DisplayName("FM-CER-24 / D-F3B-10 / D-F3B-11 one SINGLE entry object is added once per reinversion aporte: the second call to CalcularParidad carries the entry TWICE with the same IdInterno (2) and the same date")
    void fmCer24_laEntradaDeLaParidadSeReusa() throws Exception {
        String slug = "dos-reinversiones-otro-regimen-la-paridad-ve-la-entrada-repetida";
        CertificarAporteRescateContexto c = ctx(slug);
        igual("25.31629546648439", sol(c, 3002).CostoUF, "3002");
        igual("25.31629546648439", sol(c, 3004).CostoUF, "3004");
        List<JsonObject> uf = new ArrayList<>();
        for (JsonObject l : llamadas(slug, "CalcularParidad")) {
            if (arg(l).get("p_IdTrabajo").getAsLong() == 1) uf.add(l);
        }
        assertEquals(2, uf.size());
        assertEquals(1, arg(uf.get(0)).getAsJsonArray("p_CalculosEntrada").size());
        JsonArray segunda = arg(uf.get(1)).getAsJsonArray("p_CalculosEntrada");
        assertEquals(2, segunda.size());
        assertEquals(2, segunda.get(0).getAsJsonObject().get("IdInterno").getAsLong());
        assertEquals(2, segunda.get(1).getAsJsonObject().get("IdInterno").getAsLong());
        assertEquals(segunda.get(0), segunda.get(1));
    }

    @Test
    @DisplayName("FM-CER-24 the rescate of origin is READ FROM THE DATABASE, not taken from memory: with 3001 in the same request (CostoUF 40 and MontoMonedaSistema 1.200.000 in the database, recalculated by F-B in memory) the aporte inherits 33.33333333333333")
    void fmCer24_origenDeLaBase() throws Exception {
        CertificarAporteRescateContexto c = ctx("reinversion-lee-el-rescate-de-origen-de-la-base-no-el-de-memoria");
        igual("33.33333333333333", sol(c, 3002).CostoUF, "3002 (the in-memory rescate would give 25.6407)");
        igual("39000.5", sol(c, 3002).ValorUFFechaInversion, "3002");
    }

    @Test
    @DisplayName("FM-CER-25 foreign-currency reinversion (aporte and rescate in USD, system currency CLP): ValorUFRescate = SUM of the DISTINCT MontoMonedaSistema of the not rejected reinversion aportes / CostoUF: two aportes with the same amount count once (CostoUF 40), amounts 5.000 and 7.000 are summed (16.66666666666667 and 23.33333333333333)")
    void fmCer25_sumaDeMontosDistintos() throws Exception {
        CertificarAporteRescateContexto a = ctx("reinversion-moneda-extranjera-suma-los-montos-distintos");
        igual("40", sol(a, 3002).CostoUF, "3002, equal amounts counted once");
        igual("40", sol(a, 3004).CostoUF, "3004, equal amounts counted once");
        CertificarAporteRescateContexto b = ctx("reinversion-moneda-extranjera-montos-distintos-se-suman");
        igual("16.66666666666667", sol(b, 3002).CostoUF, "3002 = 5.000 / (12.000 / 40)");
        igual("23.33333333333333", sol(b, 3004).CostoUF, "3004 = 7.000 / (12.000 / 40)");
        CertificarAporteRescateContexto c = ctx("reinversion-moneda-extranjera-origen-en-otra-moneda-usa-su-monto");
        igual("0.1666666666666667", sol(c, 3002).CostoUF, "origin in CLP: 5.000 / (1.200.000 / 40)");
        igual("33.33333333333333", sol(ctx("reinversion-moneda-de-sistema-igual-usa-el-monto-del-rescate"), 3002).CostoUF, "same currency: MontoMonedaSistema of the rescate");
    }

    @Test
    @DisplayName("FM-CER-25 / FM-CER-21 an aporte rejected by the balance does not enter the sum of the foreign-currency reinversion (only the 7.000 one is left)")
    void fmCer25_elRechazadoNoEntraEnLaSuma() throws Exception {
        CertificarAporteRescateContexto c = ctx("reinversion-moneda-extranjera-sin-el-aporte-rechazado-en-la-suma");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3002));
        igual("40", sol(c, 3004).CostoUF, "3004 = 7.000 / (7.000 / 40): the rejected 5.000 is not in the sum");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-27 / 28 custody of the aportes, cartera and real profitability
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-27 the movement of an aporte: Cantidad = NumeroCuotas, PrecioCompra = ValorCuota, MontoCompra = MontoMonedaSistema, MontoCompraMonedaInstrumento = Monto, CostoEnFactorAjuste = CostoUF, TipoFactorAjuste UF, tipo APORFM, comprobante FFMM, bolsa BCS, boveda COR, canal '', p_simularOperacion N, p_UsaReglasNegocio S")
    void fmCer27_movimientoDelAporte() throws Exception {
        String slug = "custodia-de-ingreso-un-aporte";
        CertificarAporteRescateContexto c = ctx(slug);
        JsonObject a = arg(llamadas(slug, "ProcesarCustodiaFondoMutuo").get(0));
        assertEquals("N", a.get("p_simularOperacion").getAsString());
        assertEquals("S", a.get("p_UsaReglasNegocio").getAsString());
        JsonObject m = mov0(slug, 0);
        assertEquals("APORFM", m.get("IdTipoOperacion").getAsString());
        assertEquals("INFM", m.get("IdTipoProducto").getAsString());
        assertEquals("FFMM", m.get("IdTipoComprobante").getAsString());
        assertEquals("BCS", m.get("IdBolsa").getAsString());
        assertEquals("COR", m.get("IdBoveda").getAsString());
        assertEquals("UF", m.get("TipoFactorAjuste").getAsString());
        assertEquals("", m.get("IdTipoCanalIngreso").getAsString());
        assertEquals("2026-03-02 00:00:00.000", m.get("FechaOperacion").getAsString());
        numero("199.9835", m, "Cantidad");
        numero("1500.1234", m, "PrecioCompra");
        numero("300000", m, "MontoCompra");
        numero("300000", m, "MontoCompraMonedaInstrumento");
        numero("7.6922", m, "CostoEnFactorAjuste");
        numero("0", m, "ValorRescate");
        igual("199.9835", sol(c, 3001).NumeroCuotas, "NumeroCuotas");
    }

    @Test
    @DisplayName("FM-CER-27 a reinversion aporte takes FechaOriginalInversion as the origin of the purchase when it is not 0001-01-01 (2025-11-15); the sentinel 0001-01-01 is read by H2 as 0001-01-03 (calendar change: ENGINE LIMIT, HANA reads 0001-01-01) so the legacy does NOT see it as the sentinel; with the date that prints 0001-01-01 (0000-12-30 in H2) the FechaValorCuota is used")
    void fmCer27_fechaOrigenDeCompra() throws Exception {
        ctx("custodia-aporte-reinversion-con-fecha-original");
        assertEquals("2025-11-15 00:00:00.000", mov0("custodia-aporte-reinversion-con-fecha-original", 0).get("FechaOrigenCompra").getAsString());
        ctx("custodia-aporte-reinversion-fecha-original-0001-01-01-se-lee-como-0001-01-03-en-h2");
        assertEquals("0001-01-03 00:00:00.000", mov0("custodia-aporte-reinversion-fecha-original-0001-01-01-se-lee-como-0001-01-03-en-h2", 0).get("FechaOrigenCompra").getAsString());
        ctx("custodia-aporte-reinversion-fecha-original-centinela-usa-fecha-valor-cuota");
        assertEquals("2026-03-02 00:00:00.000", mov0("custodia-aporte-reinversion-fecha-original-centinela-usa-fecha-valor-cuota", 0).get("FechaOrigenCompra").getAsString());
        ctx("custodia-aporte-no-reinversion-ignora-la-fecha-original");
        assertEquals("2026-03-02 00:00:00.000", mov0("custodia-aporte-no-reinversion-ignora-la-fecha-original", 0).get("FechaOrigenCompra").getAsString());
        JsonObject m = mov0("custodia-aporte-con-regimen-tributario-y-ejecutivo", 0);
        assertEquals("APV-A", m.get("IdRegimenTributario").getAsString());
        assertEquals(33, m.get("IdAgente").getAsLong());
        assertEquals("TCN1", m.get("TipoCarteraNormativa").getAsString());
    }

    @Test
    @DisplayName("FM-CER-27 / FM-CER-19 ONE call to custody for all the not rejected aportes, in ascending id, whatever the channel (PAC included); none when every aporte was rejected by the balance")
    void fmCer27_unaLlamadaEnOrdenDeId() throws Exception {
        ctx("custodia-de-ingreso-varios-aportes-en-orden-de-id");
        List<JsonObject> l = llamadas("custodia-de-ingreso-varios-aportes-en-orden-de-id", "ProcesarCustodiaFondoMutuo");
        assertEquals(1, l.size());
        JsonArray movs = arg(l.get(0)).getAsJsonArray("p_Movimientos");
        List<Long> ids = new ArrayList<>();
        for (JsonElement e : movs) ids.add(e.getAsJsonObject().get("IdGenerarMovimientoCustodia").getAsLong());
        assertEquals(List.of(3001L, 3002L, 3003L), ids);
        ctx("custodia-aporte-pac-y-otros-canales-van-juntos-con-canal-vacio");
        assertEquals(3, arg(llamadas("custodia-aporte-pac-y-otros-canales-van-juntos-con-canal-vacio", "ProcesarCustodiaFondoMutuo").get(0)).getAsJsonArray("p_Movimientos").size());
        ctx("todos-los-aportes-rechazados-por-saldo-no-llaman-a-custodia");
        assertEquals(0, llamadas("todos-los-aportes-rechazados-por-saldo-no-llaman-a-custodia", "ProcesarCustodiaFondoMutuo").size());
    }

    @Test
    @DisplayName("FM-CER-27 / D-F3B-01 an ERROR of custody rejects the FIRST aporte whose id is |id| with the code and the description of the message, adds the message (id = |id|) and the counter; the id of a message that is not an aporte only leaves the message; a message that is not ERROR is ignored; the counter of the answer is added even without messages")
    void fmCer27_erroresDeCustodia() throws Exception {
        CertificarAporteRescateContexto a = ctx("custodia-error-rechaza-al-aporte-por-su-id");
        assertEquals("PEN:", est(a, 3001));
        assertEquals("RZD:UI_ERROR_CUSTODIA_APORTE", est(a, 3002));
        assertEquals("Error de custodia en el aporte", sol(a, 3002).DescripcionCodigo);
        assertEquals(1L, a.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals(1, a.salida.P_OUT_MENSAJES.size());
        assertEquals(3002L, a.salida.P_OUT_MENSAJES.get(0).id.longValue());
        assertEquals("ERROR", a.salida.P_OUT_MENSAJES.get(0).tipo);
        CertificarAporteRescateContexto n = ctx("custodia-error-con-id-negativo-usa-el-valor-absoluto");
        assertEquals("RZD:UI_ERROR_CUSTODIA_APORTE", est(n, 3001));
        assertEquals(3001L, n.salida.P_OUT_MENSAJES.get(0).id.longValue());
        CertificarAporteRescateContexto x = ctx("custodia-error-de-un-id-que-no-es-aporte-solo-queda-el-mensaje");
        assertEquals("PEN:", est(x, 3001));
        assertEquals(1L, x.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals(9999L, x.salida.P_OUT_MENSAJES.get(0).id.longValue());
        CertificarAporteRescateContexto adv = ctx("custodia-mensaje-que-no-es-error-no-rechaza");
        assertEquals("PEN:", est(adv, 3001));
        assertEquals(0L, adv.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertTrue(adv.salida.P_OUT_MENSAJES.isEmpty());
        CertificarAporteRescateContexto k = ctx("custodia-contador-sin-mensajes-sube-el-contador-final");
        assertEquals(2L, k.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertTrue(k.salida.P_OUT_MENSAJES.isEmpty());
        CertificarAporteRescateContexto d = ctx("custodia-dos-errores-del-mismo-id-rechaza-una-vez-y-agrega-ambos-mensajes");
        assertEquals(2L, d.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals(2, d.salida.P_OUT_MENSAJES.size());
        assertEquals("RZD:UI_ERROR_CUSTODIA_GENERAL", est(d, 3001), "the LAST message with the id writes the code");
    }

    @Test
    @DisplayName("FM-CER-27 a failing custody comes out untouched; a message without id raises NullPointerException when |id| is taken (D-NOR-9)")
    void fmCer27_excepciones() throws Exception {
        assertEquals("Exception: custodia no disponible", lanza("custodia-falla-y-propaga"));
        assertTrue(lanza("custodia-con-mensaje-sin-id-lanza-NPE-al-tomar-el-valor-absoluto").startsWith("NullPointerException:"));
    }

    @Test
    @DisplayName("FM-CER-28 IdCartera = IdCustodia and RentabilidadReal = MontoCompraFactorOriginal of the FIRST movement of the aporte (9201 and 3.5, not 9202 / 4.5); a rescate-sized id or a rejected aporte gets none")
    void fmCer28_carteraYRentabilidad() throws Exception {
        SolicitudProceso a = sol(ctx("cartera-del-aporte-se-asigna-con-el-primer-movimiento-de-su-id"), 3001);
        assertEquals(9201L, a.IdCartera.longValue());
        igual("3.5", a.RentabilidadReal, "RentabilidadReal");
        SolicitudProceso u = sol(ctx("custodia-de-ingreso-un-aporte"), 3001);
        assertEquals(9101L, u.IdCartera.longValue());
        igual("7.25", u.RentabilidadReal, "RentabilidadReal");
        CertificarAporteRescateContexto r = ctx("cartera-del-aporte-rechazado-por-custodia-no-se-asigna");
        assertEquals("RZD:UI_ERROR_CUSTODIA_GENERAL", est(r, 3002));
        assertEquals(0L, sol(r, 3002).IdCartera.longValue());
        assertNull(sol(r, 3002).RentabilidadReal);
        assertEquals(0L, sol(ctx("cartera-movimiento-con-id-ajeno-no-se-asigna"), 3001).IdCartera.longValue());
    }

    @Test
    @DisplayName("FM-CER-28 / D-F3B-37 SelectSolicitudes does not read RentabilidadReal: an aporte without movement in the answer keeps its IdCartera (77) but ends with RentabilidadReal NULL, and the UPDATE writes that NULL over the 1.25 of the database")
    void fmCer28_aporteSinCarteraDejaLaRentabilidadNula() throws Exception {
        CertificarAporteRescateContexto c = ctx("cartera-sin-movimiento-en-la-respuesta-no-se-asigna");
        assertEquals(9201L, sol(c, 3001).IdCartera.longValue());
        assertEquals(77L, sol(c, 3002).IdCartera.longValue());
        assertNull(sol(c, 3002).RentabilidadReal);
        JsonObject g = golden("cartera-sin-movimiento-en-la-respuesta-no-se-asigna");
        for (JsonElement e : g.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(CerCosturaT2.T_SOL)) {
            if (e.getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong() == 3002L) assertTrue(e.getAsJsonObject().get("RentabilidadReal").isJsonNull());
        }
    }

    @Test
    @DisplayName("FM-CER-27 / FM-CER-31 aportes and rescates in the same lot: custody of the aportes sees only the aportes (3001, 3003 in order)")
    void fmCer27_soloLosAportes() throws Exception {
        ctx("aportes-y-rescate-mezclados-la-custodia-solo-ve-los-aportes");
        List<JsonObject> l = llamadas("aportes-y-rescate-mezclados-la-custodia-solo-ve-los-aportes", "ProcesarCustodiaFondoMutuo");
        JsonArray movs = arg(l.get(0)).getAsJsonArray("p_Movimientos");
        assertEquals("APORFM", movs.get(0).getAsJsonObject().get("IdTipoOperacion").getAsString());
        assertEquals(2, movs.size());
        assertEquals(3001L, movs.get(0).getAsJsonObject().get("IdGenerarMovimientoCustodia").getAsLong());
        assertEquals(3003L, movs.get(1).getAsJsonObject().get("IdGenerarMovimientoCustodia").getAsLong());
        assertEquals("RESCFM", arg(l.get(1)).getAsJsonArray("p_Movimientos").get(0).getAsJsonObject().get("IdTipoOperacion").getAsString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-31 custodia disponible: rechazo (D-F3B-05, D-F3B-06)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-31 a result RZD rejects with UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION and the description 'Cliente <doc> portafolio <cod> sin custodia de <serie> (retiro <cuotas>, disponible <disponible>)'; the other rescate passes; the counter stays 0")
    void fmCer31_rechazoPorCustodia() throws Exception {
        CertificarAporteRescateContexto c = ctx("custodia-RZD-rechaza-con-su-descripcion");
        assertEquals("RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION", est(c, 3001));
        assertEquals("Cliente 11111111-1 portafolio PF500 sin custodia de FM001A (retiro 666.6118, disponible 0)", sol(c, 3001).DescripcionCodigo);
        assertEquals("PEN:", est(c, 3002));
        assertEquals(0L, c.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals(1, llamadas("custodia-RZD-rechaza-con-su-descripcion", "CalculaCustodiaRescate").size());
        CertificarAporteRescateContexto o = ctx("custodia-ADV-con-omnibus-procesado-rechaza");
        assertEquals("RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION", est(o, 3001));
        assertEquals("Cliente 11111111-1 portafolio PF500 sin custodia de FM001A (retiro 666.6118, disponible 100)", sol(o, 3001).DescripcionCodigo);
    }

    @Test
    @DisplayName("FM-CER-31 / D-F3B-05 / D-F3B-39 a rescate with no result (or a RZD result without CantidadDisponible) raises NullPointerException and aborts the whole lot; any state other than RZD / ADV (XYZ) is approved")
    void fmCer31_sinResultadoLanzaNpe() throws Exception {
        assertTrue(lanza("custodia-sin-resultado-lanza-NPE-y-aborta-el-lote").startsWith("NullPointerException:"));
        assertTrue(lanza("custodia-sin-ningun-resultado-lanza-NPE").startsWith("NullPointerException:"));
        assertTrue(lanza("custodia-RZD-sin-cantidad-disponible-lanza-NPE").startsWith("NullPointerException:"));
        assertEquals("PEN:", est(ctx("custodia-estado-desconocido-se-trata-como-aprobado"), 3001));
        assertEquals("Exception: custodia no disponible", lanza("custodia-el-calculo-falla-y-propaga"));
    }

    @Test
    @DisplayName("FM-CER-31 / FM-CER-19 CalculaCustodiaRescate receives the PEN / ADV rescates in ascending id (3001, 3003: the rejected 3002 is not sent) with the amounts of F-B; nothing is called when no rescate survives F-A")
    void fmCer31_loQueSeEnviaALaCustodia() throws Exception {
        String slug = "custodia-solo-ve-los-rescates-vigentes-en-orden-de-id";
        ctx(slug);
        JsonArray sols = arg(llamadas(slug, "CalculaCustodiaRescate").get(0)).getAsJsonArray("p_Solicitudes");
        assertEquals(2, sols.size());
        assertEquals(3001L, sols.get(0).getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong());
        assertEquals(3003L, sols.get(1).getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong());
        numero("666.6118", sols.get(0).getAsJsonObject(), "NumeroCuotas");
        numero("1000000", sols.get(0).getAsJsonObject(), "Monto");
        ctx("todos-los-rescates-rechazados-por-F-A-no-llaman-a-custodia");
        assertEquals(0, llamadas("todos-los-rescates-rechazados-por-F-A-no-llaman-a-custodia", "CalculaCustodiaRescate").size());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-32 advertencia y ajuste / FM-CER-33 rescate total
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-32 a result ADV (custody 100 against 666.6118 quotas) advises: ADV / UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION with the description 'cuotas a retirar 666.6118 pasan a 100 (disponible 100)'; the amounts are the ones of the custody (Monto 150.004, NumeroCuotas 100, ValorCuota 1500.0037)")
    void fmCer32_advertenciaYAjuste() throws Exception {
        CertificarAporteRescateContexto c = ctx("custodia-ADV-ajusta-los-montos-y-advierte");
        SolicitudProceso s = sol(c, 3001);
        assertEquals("ADV:UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION", est(c, 3001));
        assertEquals("Saldo de custodia insuficiente de FM001A: cuotas a retirar 666.6118 pasan a 100 (disponible 100)", s.DescripcionCodigo);
        igual("150004", s.Monto, "Monto");
        igual("100", s.NumeroCuotas, "NumeroCuotas");
        igual("1500.0037", s.ValorCuota, "ValorCuota");
        igual("150004", s.CostoMonedaLocal, "CostoMonedaLocal");
        igual("150004", s.MontoPago, "MontoPago");
        igual("150004", s.MontoPagoLocal, "MontoPagoLocal");
        igual("150004", s.MontoMonedaSistema, "MontoMonedaSistema");
        assertEquals(0L, c.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals("ADV:UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION", est(ctx("custodia-ADV-con-omnibus-en-cero-sigue-y-ajusta"), 3001));
        assertEquals("ADV:UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION", est(ctx("custodia-ADV-con-omnibus-nulo-sigue-y-ajusta"), 3001));
        CertificarAporteRescateContexto t = ctx("custodia-ADV-del-rescate-total-ajusta");
        assertEquals("Saldo de custodia insuficiente de FM001A: cuotas a retirar 120.0000 pasan a 100 (disponible 100)", sol(t, 3001).DescripcionCodigo);
    }

    @Test
    @DisplayName("FM-CER-31/32 lot with an approved, an advised and a rejected rescate and an aporte: states APD / ADV / RZD, counter 0 (the host commits), the aporte is processed first in the output order (APO before RES)")
    void fmCer32_loteMixto() throws Exception {
        CertificarAporteRescateContexto c = ctx("lote-mixto-aprobado-advertido-y-rechazado-por-custodia");
        assertEquals("PEN:", est(c, 3001));
        assertEquals("ADV:UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION", est(c, 3002));
        assertEquals("RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION", est(c, 3003));
        assertEquals("PEN:", est(c, 3004));
        assertEquals(0L, c.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals(List.of(3004L, 3001L, 3002L, 3003L), c.solicitudes.stream().map(s -> s.IdSolicitudAporteRescate).toList());
    }

    @Test
    @DisplayName("FM-CER-33 a total rescate (CalculoPorMontoCuota T) with IdOmnibus null or 0 takes the amounts of the custody even when it is approved (Monto 150.000, NumeroCuotas 99.5, ValorCuota 1507.5377); with IdOmnibus > 0 it keeps its own (150.387, 100.25); a not total rescate ignores them")
    void fmCer33_rescateTotal() throws Exception {
        for (String slug : new String[] { "total-con-omnibus-en-cero-toma-los-montos-de-custodia", "total-con-omnibus-nulo-toma-los-montos-de-custodia" }) {
            SolicitudProceso s = sol(ctx(slug), 3001);
            igual("150000", s.Monto, slug + " Monto");
            igual("99.5", s.NumeroCuotas, slug + " NumeroCuotas");
            igual("1507.5377", s.ValorCuota, slug + " ValorCuota");
            igual("150000", s.MontoPago, slug + " MontoPago");
            igual("150000", s.MontoMonedaSistema, slug + " MontoMonedaSistema");
            // the CostoUF of the custody (3.8461) is overwritten afterwards by the sum of the retired cost (FM-CER-41): 10.5
            igual("10.5", s.CostoUF, slug + " CostoUF");
        }
        SolicitudProceso p = sol(ctx("total-con-omnibus-procesado-conserva-los-montos"), 3001);
        igual("150387", p.Monto, "Monto kept");
        igual("100.25", p.NumeroCuotas, "NumeroCuotas kept");
        igual("1500.1234", p.ValorCuota, "ValorCuota kept");
        SolicitudProceso n = sol(ctx("no-total-ignora-los-montos-de-custodia-aprobada"), 3001);
        igual("1000000", n.Monto, "Monto of an approved not total rescate");
        igual("666.6118", n.NumeroCuotas, "NumeroCuotas of an approved not total rescate");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-34 GeneraSpot
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-34 GeneraSpot S only with RESCATE_GENERA_VENTA_SPOT_DOLAR S (equalsIgnoreCase) and (tipo RESCNN or IdMoneda USD) and IdMonedaPago CLP; the rescate is sent to Spot and NOT to Tesoreria; every other rescate gets N")
    void fmCer34_generaSpot() throws Exception {
        for (String slug : new String[] { "spot-S-rescate-en-usd-pagado-en-clp", "spot-S-rescate-nn-pagado-en-clp", "spot-parametro-minuscula-s-marca-por-equalsignorecase" }) {
            assertEquals("S", sol(ctx(slug), 3001).GeneraSpot, slug);
            assertEquals(1, llamadas(slug, "ProcesarIngresoOperacionSpot").size(), slug);
            assertEquals(0, llamadas(slug, "ProcesarIngresoEgresoTesoreria").size(), slug);
        }
        for (String slug : new String[] { "spot-parametro-N-no-marca", "spot-S-pagado-en-usd-no-marca", "spot-S-rescate-normal-en-clp-no-marca" }) {
            assertEquals("N", sol(ctx(slug), 3001).GeneraSpot, slug);
            assertEquals(0, llamadas(slug, "ProcesarIngresoOperacionSpot").size(), slug);
            assertEquals(1, llamadas(slug, "ProcesarIngresoEgresoTesoreria").size(), slug);
        }
        CertificarAporteRescateContexto c = ctx("spot-S-un-rescate-con-spot-y-otro-sin");
        assertEquals("S", sol(c, 3001).GeneraSpot);
        assertEquals("N", sol(c, 3002).GeneraSpot);
        JsonObject spot = arg(llamadas("spot-S-un-rescate-con-spot-y-otro-sin", "ProcesarIngresoOperacionSpot").get(0)).getAsJsonArray("p_DatosOperacion").get(0).getAsJsonObject();
        assertEquals(3001L, spot.get("IdInterno").getAsLong());
        assertEquals("S", spot.get("GeneraSpot").getAsString());
    }

    @Test
    @DisplayName("FM-CER-34 the rescate rejected by custody keeps GeneraSpot NULL (it is only set in the branch that does not reject); the other rescates get S / N")
    void fmCer34_elRechazadoNoSeMarca() throws Exception {
        CertificarAporteRescateContexto c = ctx("custodia-RZD-rechaza-con-su-descripcion");
        assertNull(sol(c, 3001).GeneraSpot);
        assertEquals("N", sol(c, 3002).GeneraSpot);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-35 traspaso
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-35 / D-F3B-38 RelacionTraspaso S (also for a rescate rejected by custody: the loop runs over all) when the solicitud, or its related one, has a traspaso (APV or not APV; the not APV one wins with the same id); Tesoreria then receives the traspaso entry (medio TRA, person of the traspaso 101 / 202); N otherwise")
    void fmCer35_traspaso() throws Exception {
        assertEquals("S", sol(ctx("traspaso-apv-de-la-solicitud-marca-la-relacion"), 3001).RelacionTraspaso);
        assertEquals(101L, entradaTesoreria("traspaso-apv-de-la-solicitud-marca-la-relacion", 3001).get("IdPersonaTraspaso").getAsLong());
        assertEquals("S", sol(ctx("traspaso-no-apv-de-la-solicitud-marca-la-relacion"), 3001).RelacionTraspaso);
        assertEquals(202L, entradaTesoreria("traspaso-no-apv-de-la-solicitud-marca-la-relacion", 3001).get("IdPersonaTraspaso").getAsLong());
        assertEquals("S", sol(ctx("traspaso-de-la-solicitud-relacionada-marca-la-relacion"), 3002).RelacionTraspaso);
        assertEquals(202L, entradaTesoreria("traspaso-de-la-solicitud-relacionada-marca-la-relacion", 3002).get("IdPersonaTraspaso").getAsLong());
        assertEquals("N", sol(ctx("sin-traspaso-no-marca-la-relacion"), 3001).RelacionTraspaso);
        CertificarAporteRescateContexto c = ctx("traspaso-solo-marca-a-la-solicitud-que-lo-tiene");
        assertEquals("S", sol(c, 3001).RelacionTraspaso);
        assertEquals("N", sol(c, 3002).RelacionTraspaso);
        // marcarTraspasos runs over ALL the rescates of the list, also the rejected ones (rule; the golden does not show it: a rejected rescate never reaches Tesoreria)
        assertEquals("S", sol(ctx("traspaso-del-rescate-rechazado-por-custodia-igual-se-marca"), 3001).RelacionTraspaso);
        assertEquals("RZD", sol(ctx("traspaso-del-rescate-rechazado-por-custodia-igual-se-marca"), 3001).EstadoProceso);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-36 / 38 retention of the APV rescates (D-F3B-08, D-F3B-09)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-36 APV-B and APV-AN rescate (RESNORM, medio TRA) with APV.PorcentajeRetencion 15: MontoRetencion = 1.000.000 x 15 / 100 = 150.000 over the gross amount; Tesoreria receives MontoPagoLocal 850.000 and a SECOND entry (IdInterno -3001) for the SII with 150.000")
    void fmCer36_retencion() throws Exception {
        for (String slug : new String[] { "retencion-apv-b-al-quince-por-ciento", "retencion-apv-an-al-quince-por-ciento" }) {
            SolicitudProceso s = sol(ctx(slug), 3001);
            assertEquals("S", s.TieneMontoRetencion, slug);
            igual("150000", s.MontoRetencion, slug + " MontoRetencion");
            igual("150000", s.MontoRetencionLocal, slug + " MontoRetencionLocal");
            numero("850000", entradaTesoreria(slug, 3001), "MontoPagoLocal");
            numero("150000", entradaTesoreria(slug, -3001), "MontoPagoLocal");
            assertEquals("S", entradaTesoreria(slug, -3001).get("RelacionTraspaso").getAsString());
            assertEquals("RETSII", entradaTesoreria(slug, -3001).get("IdConcepto").getAsString());
            assertEquals(3002L, entradaTesoreria(slug, -3001).get("IdPersonaTraspaso").getAsLong());
        }
    }

    @Test
    @DisplayName("FM-CER-36 NO retention: percentage 0, absent (D-F3B-09: 0 and an error log) or not numeric; medio CIN (no entry to Tesoreria at all); traspaso; Spot; a regime that is not APV-AN / APV-B (GEN, BF); a type other than RESNORM; the SII entry still exists for APV-B with 0")
    void fmCer36_sinRetencion() throws Exception {
        for (String slug : new String[] { "retencion-porcentaje-cero-no-aplica", "retencion-parametro-ausente-no-aplica", "retencion-parametro-no-numerico-no-aplica", "retencion-tipo-distinto-de-rescate-normal-no-aplica" }) {
            SolicitudProceso s = sol(ctx(slug), 3001);
            assertEquals("N", s.TieneMontoRetencion, slug);
            igual("0", s.MontoRetencion, slug + " MontoRetencion");
            numero("1000000", entradaTesoreria(slug, 3001), "MontoPagoLocal");
            numero("0", entradaTesoreria(slug, -3001), "MontoPagoLocal");
        }
        for (String slug : new String[] { "retencion-medio-cin-no-aplica", "retencion-con-traspaso-no-aplica", "retencion-regimen-que-no-es-apv-no-aplica", "retencion-regimen-bf-no-aplica" }) {
            SolicitudProceso s = sol(ctx(slug), 3001);
            assertEquals("N", s.TieneMontoRetencion, slug + " (rule; the golden does not show it when the rescate never reaches the normal Tesoreria path)");
            igual("0", s.MontoRetencion, slug + " MontoRetencion");
        }
        assertEquals(0, llamadas("retencion-medio-cin-no-aplica", "ProcesarIngresoEgresoTesoreria").size());
        SolicitudProceso spot = sol(ctx("retencion-con-spot-no-aplica"), 3001);
        assertEquals("N", spot.TieneMontoRetencion);
        assertEquals("S", spot.GeneraSpot);
    }

    @Test
    @DisplayName("FM-CER-36 IdRegimenTributarioAPV falls back to the portfolio code when the regime is empty ('' -> 'APV-B'): the retention applies; the second rescate paid to the cuenta de inversion has none")
    void fmCer36_regimenVacioYMezcla() throws Exception {
        SolicitudProceso s = sol(ctx("retencion-regimen-vacio-toma-el-codigo-de-portafolio"), 3001);
        assertEquals("S", s.TieneMontoRetencion);
        igual("150000", s.MontoRetencion, "MontoRetencion");
        CertificarAporteRescateContexto c = ctx("retencion-dos-rescates-uno-con-retencion-y-otro-cin");
        assertEquals("S", sol(c, 3001).TieneMontoRetencion);
        assertEquals("N", sol(c, 3002).TieneMontoRetencion);
        assertNull(entradaTesoreria("retencion-dos-rescates-uno-con-retencion-y-otro-cin", 3002));
    }

    @Test
    @DisplayName("FM-CER-36 / D-F3B-09 the retention is computed on the GROSS amount, BEFORE the commission: with a commission of 19.998 Tesoreria receives MontoPagoLocal 830.002 = 980.002 - 150.000 (not 15% of the net)")
    void fmCer36_retencionAntesDeLaComision() throws Exception {
        String slug = "retencion-se-calcula-sobre-el-monto-bruto-antes-de-la-comision";
        SolicitudProceso s = sol(ctx(slug), 3001);
        igual("150000", s.MontoRetencion, "MontoRetencion");
        igual("19998", s.MontoComision, "MontoComision");
        igual("980002", s.MontoPagoLocal, "MontoPagoLocal");
        numero("830002", entradaTesoreria(slug, 3001), "MontoPagoLocal");
        numero("150000", entradaTesoreria(slug, -3001), "MontoPagoLocal");
    }

    @Test
    @DisplayName("FM-CER-36 / D-F3B-09 fund in USD, investment in CLP: MontoRetencion (USD) = 5524.86 x 15 / 100 = 828.73 and MontoRetencionLocal = 750.000; Tesoreria subtracts the USD 828.73 from the CLP MontoPagoLocal: 5.000.000 - 828.73 = 4.999.171.27")
    void fmCer36_retencionEnFondoNoClp() throws Exception {
        String slug = "retencion-fondo-en-usd-resta-el-monto-del-fondo-del-pago-local";
        SolicitudProceso s = sol(ctx(slug), 3001);
        igual("828.73", s.MontoRetencion, "MontoRetencion (moneda del fondo)");
        igual("750000", s.MontoRetencionLocal, "MontoRetencionLocal (moneda de inversion)");
        numero("4999171.27", entradaTesoreria(slug, 3001), "MontoPagoLocal");
        numero("828.73", entradaTesoreria(slug, -3001), "MontoPagoLocal");
    }

    @Test
    @DisplayName("FM-CER-36 the plain divide(100) keeps every decimal before the format: 7% of 1000.37 gives 70 once formatted in CLP (Tesoreria: 930 and 70)")
    void fmCer36_divisionExacta() throws Exception {
        String slug = "retencion-con-decimales-exactos-sin-contexto-de-redondeo";
        SolicitudProceso s = sol(ctx(slug), 3001);
        igual("70", s.MontoRetencion, "MontoRetencion");
        numero("930", entradaTesoreria(slug, 3001), "MontoPagoLocal");
        numero("70", entradaTesoreria(slug, -3001), "MontoPagoLocal");
    }

    @Test
    @DisplayName("FM-CER-38 a retention that is 0 after the format (15% of 3 = 0.45 -> 0 in CLP) is no retention: TieneMontoRetencion N; the amount sent to Tesoreria is not reduced and the SII entry carries 0")
    void fmCer38_retencionCeroDejaDeSerRetencion() throws Exception {
        String slug = "retencion-cero-despues-del-formato-deja-de-ser-retencion";
        SolicitudProceso s = sol(ctx(slug), 3001);
        assertEquals("N", s.TieneMontoRetencion);
        igual("0", s.MontoRetencion, "MontoRetencion");
        numero("3", entradaTesoreria(slug, 3001), "MontoPagoLocal");
        numero("0", entradaTesoreria(slug, -3001), "MontoPagoLocal");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-37 commission of the certification
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-37 CALCULAR_COMISION_CERTIFICACION S: the REAL CalcularComisiones is called with the rescate (ConsultarFlujoCustodia of cliente 100, portafolio 500, instrumento 900); MontoComision 19.998 (fund) and MontoComisionLocal 19.998; MontoPagoLocal = 1.000.000 - 19.998 = 980.002 and MontoPago = Monto - MontoComision = 980.002")
    void fmCer37_comisionSeAplica() throws Exception {
        String slug = "comision-S-calcula-y-descuenta-del-pago";
        SolicitudProceso s = sol(ctx(slug), 3001);
        igual("19998", s.MontoComision, "MontoComision");
        igual("19998", s.MontoComisionLocal, "MontoComisionLocal");
        igual("980002", s.MontoPagoLocal, "MontoPagoLocal");
        igual("980002", s.MontoPago, "MontoPago");
        igual("1000000", s.Monto, "Monto is the gross amount");
        JsonObject f = arg(llamadas(slug, "ConsultarFlujoCustodia").get(0));
        assertEquals(100L, f.getAsJsonArray("p_Uso").get(0).getAsJsonObject().get("IdCliente").getAsLong());
        assertEquals(500L, f.getAsJsonArray("p_Uso").get(0).getAsJsonObject().get("IdPortafolio").getAsLong());
        assertEquals(900L, f.getAsJsonArray("p_FiltrosBusqueda").get(0).getAsJsonObject().get("IdInstrumento").getAsLong());
    }

    @Test
    @DisplayName("FM-CER-37 no commission when the parameter is N or is not exactly S ('s'): the custody flow is not even asked; with no lots the commission is 0 and the local commission is the 0 of the parity answer")
    void fmCer37_sinComision() throws Exception {
        for (String slug : new String[] { "comision-N-no-calcula", "comision-parametro-en-minuscula-no-calcula" }) {
            SolicitudProceso s = sol(ctx(slug), 3001);
            igual("0", s.MontoComision, slug);
            igual("1000000", s.MontoPagoLocal, slug);
            assertEquals(0, llamadas(slug, "ConsultarFlujoCustodia").size(), slug);
        }
        SolicitudProceso z = sol(ctx("comision-sin-lotes-de-custodia-es-cero"), 3001);
        igual("0", z.MontoComision, "no lots");
        igual("1000000", z.MontoPagoLocal, "no lots");
    }

    @Test
    @DisplayName("FM-CER-37 / FM-CER-17 a rescate with a related RESCNN (TieneComisionNNRelacionada S) is sent to CalcularComisiones but the result is NOT applied: the amounts of the database stay (commission 11.5 -> 12 and 9.5 -> 10; MontoPago 999.000)")
    void fmCer37_comisionNnNoSeAplica() throws Exception {
        String slug = "comision-con-comision-nn-relacionada-se-calcula-pero-no-se-aplica";
        SolicitudProceso s = sol(ctx(slug), 3001);
        assertEquals("S", s.TieneComisionNNRelacionada);
        igual("12", s.MontoComision, "MontoComision of the database, formatted");
        igual("10", s.MontoComisionLocal, "MontoComisionLocal of the database, formatted");
        igual("999000", s.MontoPago, "MontoPago kept");
        assertEquals(1, llamadas(slug, "ConsultarFlujoCustodia").size());
    }

    @Test
    @DisplayName("FM-CER-37 only the rescates not rejected by custody are sent to the commission (one portfolio in the flow filter, not two); none rejected-all: CalcularComisiones is not called")
    void fmCer37_soloLosNoRechazados() throws Exception {
        String slug = "comision-solo-de-los-rescates-no-rechazados-por-custodia";
        ctx(slug);
        assertEquals(1, arg(llamadas(slug, "ConsultarFlujoCustodia").get(0)).getAsJsonArray("p_Uso").size());
        assertEquals(0, llamadas("comision-todos-los-rescates-rechazados-no-llama-al-flujo-de-custodia", "ConsultarFlujoCustodia").size());
        assertEquals("RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION", est(ctx("comision-todos-los-rescates-rechazados-no-llama-al-flujo-de-custodia"), 3001));
        CertificarAporteRescateContexto c = ctx("comision-dos-rescates-de-distinto-portafolio");
        assertEquals(2, arg(llamadas("comision-dos-rescates-de-distinto-portafolio", "ConsultarFlujoCustodia").get(0)).getAsJsonArray("p_Uso").size());
        igual("19998", sol(c, 3001).MontoComision, "3001");
        igual("19998", sol(c, 3002).MontoComision, "3002");
    }

    @Test
    @DisplayName("FM-CER-37 / FM-CER-32 an advised rescate is sent with its ADJUSTED quotas (300) and the commission is 9.000 over them; FM-CER-35 / D-COM-1 a traspaso rescate gets the same commission as any other (EsTraspaso is not used by the calculation)")
    void fmCer37_cuotasAjustadasYTraspaso() throws Exception {
        SolicitudProceso a = sol(ctx("comision-con-rescate-advertido-usa-las-cuotas-ajustadas"), 3001);
        igual("300", a.NumeroCuotas, "NumeroCuotas");
        igual("9000", a.MontoComision, "MontoComision");
        igual("141004", a.MontoPago, "MontoPago = 150.004 - 9.000 (formatted)");
        CertificarAporteRescateContexto t = ctx("comision-rescate-con-traspaso-no-cambia-el-calculo");
        assertEquals("S", sol(t, 3001).RelacionTraspaso);
        igual("19998", sol(t, 3001).MontoComision, "MontoComision of a traspaso");
        assertEquals("Exception: custodia no disponible", lanza("comision-falla-el-flujo-de-custodia-y-propaga"));
    }

    @Test
    @DisplayName("FM-CER-37 / FM-CER-23 fund in USD: the commission is formatted in USD (109.15) and its local value in CLP (108.000); MontoPago = 5524.86 - 109.15 = 5415.71 and MontoPagoLocal = 5.000.000 - 108.000 = 4.892.000")
    void fmCer37_formatoDeLaComisionEnFondoUsd() throws Exception {
        SolicitudProceso s = sol(ctx("comision-fondo-usd-formatea-con-la-moneda-del-fondo"), 3001);
        igual("109.15", s.MontoComision, "MontoComision");
        igual("108000", s.MontoComisionLocal, "MontoComisionLocal");
        igual("5415.71", s.MontoPago, "MontoPago");
        igual("4892000", s.MontoPagoLocal, "MontoPagoLocal");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-39 / 40 retirement of the custody (two calls) and rescate without cartera
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-39 / D-F3B-01 the not rejected rescates are retired in TWO calls: first the ones with IdRegimenTributario (3001 GEN, 3003 APV-A), then the ones without (3002 ''); tipo RESCFM, concept RESCFM, p_simularOperacion N, p_UsaReglasNegocio S")
    void fmCer39_dosLlamadas() throws Exception {
        String slug = "retiro-con-regimen-y-sin-regimen-son-dos-llamadas";
        ctx(slug);
        List<JsonObject> l = llamadas(slug, "ProcesarCustodiaFondoMutuo");
        assertEquals(2, l.size());
        JsonArray con = arg(l.get(0)).getAsJsonArray("p_Movimientos");
        JsonArray sin = arg(l.get(1)).getAsJsonArray("p_Movimientos");
        assertEquals(List.of(3001L, 3003L), List.of(con.get(0).getAsJsonObject().get("IdGenerarMovimientoCustodia").getAsLong(), con.get(1).getAsJsonObject().get("IdGenerarMovimientoCustodia").getAsLong()));
        assertEquals(1, sin.size());
        assertEquals(3002L, sin.get(0).getAsJsonObject().get("IdGenerarMovimientoCustodia").getAsLong());
        assertEquals("", sin.get(0).getAsJsonObject().get("IdRegimenTributario").getAsString());
        assertEquals("RESCFM", con.get(0).getAsJsonObject().get("IdTipoOperacion").getAsString());
        assertEquals("N", arg(l.get(0)).get("p_simularOperacion").getAsString());
        assertEquals("S", arg(l.get(0)).get("p_UsaReglasNegocio").getAsString());
        assertEquals(1, llamadas("retiro-solo-con-regimen-una-llamada", "ProcesarCustodiaFondoMutuo").size());
        assertEquals(1, llamadas("retiro-solo-sin-regimen-una-llamada", "ProcesarCustodiaFondoMutuo").size());
        assertEquals(0, llamadas("retiro-todos-rechazados-por-custodia-no-llama", "ProcesarCustodiaFondoMutuo").size());
        ctx("retiro-del-rescate-rechazado-por-custodia-no-se-envia");
        assertEquals(1, llamadas("retiro-del-rescate-rechazado-por-custodia-no-se-envia", "ProcesarCustodiaFondoMutuo").size());
        JsonObject m = mov0("retiro-campos-del-movimiento-de-un-rescate", 0);
        assertEquals(33, m.get("IdAgente").getAsLong());
        assertEquals("2026-02-27 00:00:00.000", m.get("FechaCurse").getAsString());
        assertEquals("RESCFM", m.get("IdTipoOperacion").getAsString());
    }

    @Test
    @DisplayName("FM-CER-39 an ERROR of the retirement rejects the first rescate with id |id| (negative ids too), adds the message and the counters of BOTH calls (2 + 3 = 5); an id that is not a rescate only leaves the message; a message that is not ERROR does nothing")
    void fmCer39_errores() throws Exception {
        CertificarAporteRescateContexto a = ctx("retiro-error-rechaza-al-rescate-por-su-id");
        assertEquals("PEN:", est(a, 3001));
        assertEquals("RZD:UI_ERROR_CUSTODIA_RESCATE", est(a, 3002));
        assertEquals(1L, a.salida.P_OUT_ERRORESCONTADOR.longValue());
        CertificarAporteRescateContexto n = ctx("retiro-error-con-id-negativo-en-la-llamada-sin-regimen");
        assertEquals("RZD:UI_ERROR_CUSTODIA_RESCATE", est(n, 3002));
        assertEquals(3002L, n.salida.P_OUT_MENSAJES.get(0).id.longValue());
        CertificarAporteRescateContexto x = ctx("retiro-error-de-un-id-ajeno-solo-queda-el-mensaje");
        assertEquals("PEN:", est(x, 3001));
        assertEquals(1L, x.salida.P_OUT_ERRORESCONTADOR.longValue());
        CertificarAporteRescateContexto s = ctx("retiro-los-contadores-de-las-dos-llamadas-se-suman");
        assertEquals(5L, s.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals("RZD:UI_ERROR_CUSTODIA_RESCATE", est(s, 3001));
        assertEquals("RZD:UI_ERROR_CUSTODIA_GENERAL", est(s, 3002));
        CertificarAporteRescateContexto adv = ctx("retiro-mensaje-que-no-es-error-no-rechaza");
        assertEquals("PEN:", est(adv, 3001));
        assertEquals(0L, adv.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals("Exception: custodia no disponible", lanza("retiro-la-segunda-llamada-falla-despues-de-la-primera"));
        assertEquals(2, llamadas("retiro-la-segunda-llamada-falla-despues-de-la-primera", "ProcesarCustodiaFondoMutuo").size());
        assertTrue(lanza("retiro-mensaje-sin-id-lanza-NPE").startsWith("NullPointerException:"));
    }

    @Test
    @DisplayName("FM-CER-40 a rescate with no movement of its id in the retirement (none at all, or only another id) is rejected UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO even with counter 0 (D-F3B-03: the host commits what the other collaborators wrote); a rescate already rejected by a custody error keeps that code")
    void fmCer40_sinCartera() throws Exception {
        CertificarAporteRescateContexto a = ctx("sin-cartera-retirada-rechaza-al-rescate");
        assertEquals("PEN:", est(a, 3001));
        assertEquals("RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO", est(a, 3002));
        assertEquals(0L, a.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals("RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO", est(ctx("sin-cartera-con-cartera-de-id-ajeno-rechaza"), 3001));
        CertificarAporteRescateContexto t = ctx("sin-cartera-de-todos-los-rescates-contador-cero-y-commit");
        assertEquals("RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO", est(t, 3001));
        assertEquals("RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO", est(t, 3002));
        assertEquals(0L, t.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals("RZD:UI_ERROR_CUSTODIA_RESCATE", est(ctx("sin-cartera-rescate-con-error-de-custodia-queda-con-el-codigo-del-error"), 3001));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-41 cost UF, spread and real profitability (D-F3B-11)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-41 example of the card: movements with cost UF 10.5 and 4.5 and purchase amounts 6.0 and 3.5, previous CostoUF 16, MontoMonedaSistema 600.000 and UTM 60.000: CostoUF 15, Spread 1, RentabilidadReal 600.000 / 60.000 - 9.5 = 0.5")
    void fmCer41_ejemploDeLaTarjeta() throws Exception {
        String slug = "costo-uf-spread-y-rentabilidad-ejemplo-de-la-tarjeta";
        SolicitudProceso s = sol(ctx(slug), 3001);
        igual("15", s.CostoUF, "CostoUF");
        igual("1", s.Spread, "Spread");
        igual("0.5", s.RentabilidadReal, "RentabilidadReal");
        igual("600000", s.MontoMonedaSistema, "MontoMonedaSistema");
        JsonObject e = arg(llamadas(slug, "CalcularParidad").get(llamadas(slug, "CalcularParidad").size() - 1)).getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject();
        assertEquals("UTM", e.get("IdMonedaOrigen").getAsString());
        assertEquals("CLP", e.get("IdMonedaDestino").getAsString());
        assertEquals("2026-03-02", e.get("Fecha").getAsString());
        assertEquals(1L, e.get("IdInterno").getAsLong());
        assertEquals(1L, arg(llamadas(slug, "CalcularParidad").get(llamadas(slug, "CalcularParidad").size() - 1)).get("p_IdTrabajo").getAsLong());
        numero("15", entradaTesoreria(slug, 3001), "CostoUF");
        numero("1", entradaTesoreria(slug, 3001), "Spread");
    }

    @Test
    @DisplayName("FM-CER-41 without a related RESCNN F-B recalculates the previous cost: CostoUF = 10.5 (the retired one), Spread = 25.6407 - 10.5 = 15.1407 (exact in the pojo of Tesoreria; H2 writes the integer 15), RentabilidadReal = 1.000.000 / 60.000 - 6.0 = 10.66666666666666666666666666666667 (DECIMAL128)")
    void fmCer41_sinComisionNn() throws Exception {
        String slug = "costo-uf-spread-y-rentabilidad-sin-comision-nn-recalculados-por-F-B";
        SolicitudProceso s = sol(ctx(slug), 3001);
        igual("10.5", s.CostoUF, "CostoUF");
        igual("15.1407", s.Spread, "Spread");
        igual("10.66666666666666666666666666666667", s.RentabilidadReal, "RentabilidadReal (DECIMAL128)");
        numero("15.1407", entradaTesoreria(slug, 3001), "Spread");
    }

    @Test
    @DisplayName("FM-CER-41 / D-F3B-11 the UTM value is 1 when the parity answers with a Resultado other than OK or has no row for the date: RentabilidadReal = 600.000 / 1 - 6.0 = 599.994")
    void fmCer41_utmNoDisponibleVale1() throws Exception {
        for (String slug : new String[] { "utm-con-resultado-distinto-de-OK-usa-uno", "utm-sin-salida-para-la-fecha-usa-uno" }) {
            SolicitudProceso s = sol(ctx(slug), 3001);
            igual("599994", s.RentabilidadReal, slug + " RentabilidadReal");
            igual("5.5", s.Spread, slug + " Spread = 16 - 10.5");
        }
        assertEquals("Exception: paridad no disponible", lanza("utm-con-paridad-que-falla-propaga"));
    }

    @Test
    @DisplayName("FM-CER-41 / D-F3B-11 one parity call for all the rescates (all share FechaCertificacion after F-A, so there is ONE entry); each rescate sums ITS movements: 3001 -> 10.5 / 6.0, 3002 -> 7.25 / 2.125 (RentabilidadReal 14.54166666666666666666666666666667)")
    void fmCer41_variosRescates() throws Exception {
        String slug = "costo-uf-dos-rescates-comparten-la-fecha-y-la-utm";
        CertificarAporteRescateContexto c = ctx(slug);
        igual("10.5", sol(c, 3001).CostoUF, "3001 CostoUF");
        igual("7.25", sol(c, 3002).CostoUF, "3002 CostoUF");
        igual("5.5", sol(c, 3001).Spread, "3001 Spread = 16 - 10.5");
        igual("4", sol(c, 3001).RentabilidadReal, "3001 RentabilidadReal = 600.000 / 60.000 - 6.0");
        igual("18.3907", sol(c, 3002).Spread, "3002 Spread = 25.6407 - 7.25");
        igual("14.54166666666666666666666666666667", sol(c, 3002).RentabilidadReal, "3002 RentabilidadReal = 1.000.000 / 60.000 - 2.125 (DECIMAL128)");
        assertEquals(1, llamadas(slug, "CalcularParidad").stream().filter(l -> arg(l).getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject().get("IdMonedaOrigen").getAsString().equals("UTM")).count());
        assertTrue(lanza("costo-uf-movimiento-con-costo-nulo-lanza-NPE").startsWith("NullPointerException:"));
        CertificarAporteRescateContexto r = ctx("costo-uf-un-rescate-sin-cartera-queda-rechazado-y-el-otro-se-calcula");
        assertEquals("PEN:", est(r, 3001));
        assertEquals("RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO", est(r, 3002));
        igual("0", sol(r, 3002).CostoUF, "the rejected rescate is recalculated too: no movements -> CostoUF 0 (D-F3B-11; rule, the golden does not show it: a rejected rescate is not written nor sent)");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // D-F3B-01 simulation, D-F3B-08, D-COM-1, the order of the calls
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("D-F3B-01 p_SimularOperacion S does not simulate anything in F-C / F-D: every custody call still carries p_simularOperacion N (p_UsaReglasNegocio S), the lists are processed for real, and the counter of F-C / F-D is only what the collaborators added (0, or 3 here); the legacy forces the final counter to 1 later (front J: golden 1)")
    void dF3B01_simulacionNoCambiaLosColaboradores() throws Exception {
        CertificarAporteRescateContexto a = ctx("simulacion-S-la-custodia-y-la-comision-reciben-N-y-no-hay-contador");
        assertEquals("S", a.entrada.p_SimularOperacion);
        assertEquals(0L, a.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals("PEN:", est(a, 3001));
        assertEquals("PEN:", est(a, 3002));
        for (JsonObject l : llamadas("simulacion-S-la-custodia-y-la-comision-reciben-N-y-no-hay-contador", "ProcesarCustodiaFondoMutuo")) {
            assertEquals("N", arg(l).get("p_simularOperacion").getAsString());
            assertEquals("S", arg(l).get("p_UsaReglasNegocio").getAsString());
        }
        igual("19998", sol(a, 3002).MontoComision, "the commission is calculated for real");
        assertEquals(1L, golden("simulacion-S-la-custodia-y-la-comision-reciben-N-y-no-hay-contador").getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong());
        CertificarAporteRescateContexto e = ctx("simulacion-S-con-error-de-custodia-el-contador-final-sigue-siendo-uno");
        assertEquals(3L, e.salida.P_OUT_ERRORESCONTADOR.longValue());
        assertEquals("RZD:UI_ERROR_CUSTODIA_APORTE", est(e, 3001));
        assertEquals(1L, golden("simulacion-S-con-error-de-custodia-el-contador-final-sigue-siendo-uno").getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong());
    }

    @Test
    @DisplayName("FM-CER-35 / FM-CER-44 a traspaso rescate paid to the cuenta de inversion (medio CIN) still reaches Tesoreria through the traspaso entry (RelacionTraspaso S, medio TRA, person 101)")
    void fmCer35_traspasoConMedioCin() throws Exception {
        assertEquals("S", sol(ctx("traspaso-con-medio-cin-igual-va-a-tesoreria-por-traspaso"), 3001).RelacionTraspaso);
        JsonObject t = entradaTesoreria("traspaso-con-medio-cin-igual-va-a-tesoreria-por-traspaso", 3001);
        assertEquals("S", t.get("RelacionTraspaso").getAsString());
        assertEquals("TRA", t.get("IdMedioDePago").getAsString());
        assertEquals(101L, t.get("IdPersonaTraspaso").getAsLong());
    }

    @Test
    @DisplayName("D-F3B-08 APV-AN with no SII configured passes F-A and F-D (retention 150.000 computed) and the legacy raises NullPointerException LATER, in Tesoreria (a later front), AFTER the custody retirement was called: the effects of custody stay orphan until the host rolls back")
    void dF3B08_apvAnSinSiiFallaDespues() throws Exception {
        String slug = "apv-an-sin-sii-configurada-pasa-f-d-y-falla-despues-en-tesoreria";
        CertificarAporteRescateContexto c = ctx(slug);
        SolicitudProceso s = sol(c, 3001);
        assertEquals("S", s.TieneMontoRetencion);
        igual("150000", s.MontoRetencion, "MontoRetencion");
        assertNull(c.personaSII);
        assertTrue(golden(slug).get("excepcion").getAsString().startsWith("NullPointerException:"));
        assertEquals(1, llamadas(slug, "ProcesarCustodiaFondoMutuo").size());
        assertEquals(0, llamadas(slug, "ProcesarIngresoEgresoTesoreria").size());
    }

    @Test
    @DisplayName("FM-CER-37 / D-COM-1 a RESREINV rescate gets no commission (the reinversion is excluded by CalcularComisiones: 0 and 0) and is not sent to Tesoreria")
    void fmCer37_reinversionSinComision() throws Exception {
        String slug = "rescate-de-reinversion-no-paga-comision";
        SolicitudProceso s = sol(ctx(slug), 3001);
        igual("0", s.MontoComision, "MontoComision");
        igual("1000000", s.MontoPagoLocal, "MontoPagoLocal");
        assertEquals(1, llamadas(slug, "ConsultarFlujoCustodia").size());
        assertEquals(0, llamadas(slug, "ProcesarIngresoEgresoTesoreria").size());
    }

    @Test
    @DisplayName("FM-CER-21/24/27/31/37/39/41 the legacy interleaves the two fronts: the flow, the custody calculation, the commission (company, custody flow, quota value, parity), the UF parity, the custody entry of the aportes, the two retirements and the UTM parity; the cuenta de inversion of the aportes (front E) would sit before the retirements")
    void ordenDeLasLlamadas() throws Exception {
        String slug = "orden-de-las-llamadas-de-aportes-y-rescates-con-comision-y-reinversion";
        CertificarAporteRescateContexto c = ctx(slug);
        List<String> orden = new ArrayList<>();
        JsonArray cols = golden(slug).getAsJsonArray("colaboradores");
        int desde = 0;
        while (desde < cols.size() && CerCosturaT2.COLABORADORES_T2.contains(cols.get(desde).getAsJsonObject().get("colaborador").getAsString())
                && (cols.get(desde).getAsJsonObject().get("colaborador").getAsString().equals("EntregarPreciosTasas") || cols.get(desde).getAsJsonObject().get("colaborador").getAsString().equals("CalcularParidad"))) {
            desde++;
        }
        for (int i = desde; i < cols.size(); i++) {
            JsonObject l = cols.get(i).getAsJsonObject();
            String n = l.get("colaborador").getAsString();
            if (!CerCosturaT2.COLABORADORES_T2.contains(n)) continue;
            JsonElement a0 = l.getAsJsonArray("argumentos").get(0);
            JsonObject a = a0.isJsonObject() ? a0.getAsJsonObject() : null;
            if (n.equals("ProcesarCustodiaFondoMutuo")) n += ":" + a.getAsJsonArray("p_Movimientos").get(0).getAsJsonObject().get("IdTipoOperacion").getAsString();
            if (n.equals("CalcularParidad")) n += ":" + a.getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject().get("IdMonedaOrigen").getAsString();
            orden.add(n);
        }
        assertEquals(List.of("SaldoFlujoCaja", "CalculaCustodiaRescate", "ConsultarEmpresa", "ConsultarFlujoCustodia", "EntregarPreciosTasas", "CalcularParidad:CLP", "CalcularParidad:UF",
                "ProcesarCustodiaFondoMutuo:APORFM", "ProcesarCustodiaFondoMutuo:RESCFM", "ProcesarCustodiaFondoMutuo:RESCFM", "CalcularParidad:UTM"), orden);
        assertEquals("PEN:", est(c, 3001));
        assertEquals("PEN:", est(c, 3002));
        assertEquals("PEN:", est(c, 3003));
        assertEquals("PEN:", est(c, 3009));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // lots (parallel streams of the legacy)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-31/39/41 / D-F3B-06 forty rescates in two retirement calls (with and without regimen): every one approved, in ascending id order in each call; the result does not depend on the thread that built each description")
    void fmCer31_loteDeCuarentaRescates() throws Exception {
        String slug = "lote-de-cuarenta-rescates-aprobados";
        CertificarAporteRescateContexto c = ctx(slug);
        assertEquals(40, c.rescates.size());
        for (SolicitudProceso s : c.rescates) assertEquals("PEN", s.EstadoProceso);
        List<JsonObject> l = llamadas(slug, "ProcesarCustodiaFondoMutuo");
        assertEquals(2, l.size());
        long previo = 0;
        for (JsonElement e : arg(l.get(0)).getAsJsonArray("p_Movimientos")) {
            long id = e.getAsJsonObject().get("IdGenerarMovimientoCustodia").getAsLong();
            assertTrue(id > previo, "ascending ids");
            previo = id;
        }
        assertEquals(26, arg(l.get(0)).getAsJsonArray("p_Movimientos").size(), "26 rescates with regimen");
        assertEquals(14, arg(l.get(1)).getAsJsonArray("p_Movimientos").size(), "14 rescates without regimen (every third one)");
    }

    @Test
    @DisplayName("FM-CER-37 / D-COM-5 when the parity has no row for the commission the local commission is valued at 1: MontoComision 19.998, MontoComisionLocal 1, MontoPagoLocal 999.999 (the amount of the fund currency is subtracted from the local one without conversion check)")
    void fmCer37_sinFilaDeParidadLaComisionLocalVale1() throws Exception {
        SolicitudProceso s = sol(ctx("comision-sin-fila-de-paridad-la-comision-local-vale-uno"), 3001);
        igual("19998", s.MontoComision, "MontoComision");
        igual("1", s.MontoComisionLocal, "MontoComisionLocal");
        igual("980002", s.MontoPago, "MontoPago");
        igual("999999", s.MontoPagoLocal, "MontoPagoLocal");
    }

    @Test
    @DisplayName("FM-CER-21 / FM-CER-19 / D-NOR-16 forty aportes of five portfolios asked in ONE call in ascending id: a saldo equal to the total passes (502, 504), one peso less rejects ALL the aportes of the portfolio (500, 503), a large one passes (501)")
    void fmCer21_loteDeCuarentaAportes() throws Exception {
        String slug = "lote-de-cuarenta-aportes-de-cinco-portafolios-con-saldos-exactos";
        CertificarAporteRescateContexto c = ctx(slug);
        assertEquals(List.of("500:CLP", "501:CLP", "502:CLP", "503:CLP", "504:CLP"), pares(slug, 0));
        assertEquals(1, llamadas(slug, "SaldoFlujoCaja").size());
        int rechazados = 0;
        for (int i = 0; i < 40; i++) {
            SolicitudProceso s = sol(c, 6000L + i);
            boolean rechaza = i % 5 == 0 || i % 5 == 3;
            assertEquals(rechaza ? "RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA" : "PEN:", est(c, 6000L + i), "aporte " + (6000 + i) + " portfolio " + s.IdPortafolio);
            if (rechaza) rechazados++;
        }
        assertEquals(16, rechazados);
        assertEquals(24, arg(llamadas(slug, "ProcesarCustodiaFondoMutuo").get(0)).getAsJsonArray("p_Movimientos").size());
    }

    @Test
    @DisplayName("FM-CER-21/27/31/32/39/40 mixed lot of three aportes and six rescates: balance rejects 3001, custody rejects 3005, advises 3004, 3003 / 3006 / 3007 / 3009 pass; counter 0 (the host commits)")
    void loteMixto() throws Exception {
        CertificarAporteRescateContexto c = ctx("lote-mixto-de-aportes-y-rescates-con-rechazos-de-todo-tipo");
        assertEquals("RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA", est(c, 3001));
        assertEquals("PEN:", est(c, 3002));
        assertEquals("PEN:", est(c, 3008));
        assertEquals("PEN:", est(c, 3003));
        assertEquals("ADV:UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION", est(c, 3004));
        assertEquals("RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION", est(c, 3005));
        assertEquals("PEN:", est(c, 3006));
        assertEquals("PEN:", est(c, 3007));
        assertEquals("PEN:", est(c, 3009));
        assertEquals(0L, c.salida.P_OUT_ERRORESCONTADOR.longValue());
    }
}
