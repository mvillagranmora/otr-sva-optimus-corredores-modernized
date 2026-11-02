package cl.bcs.fondomutuo.liquidacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * FMM-LIQ rules, pinned with LITERALS read in the goldens RECORDED from the unmodified legacy (FM-LIQ-01..20 of RULES_FONDOMUTUO_F3_B.md 8.3 and the quirks of the class). Each test names the card in its display
 * name and the cases (by slug of {@code FMM-LIQ-nnn_slug}) it pins. They run the PRODUCTION chain through {@link CasoLiq#ejecutar(String)}; with {@code -Dliq.autocomprobacion=true} the harness hands back the golden
 * itself (no production code runs): the proof that these assertions describe what the legacy did.
 *
 * <p>Seed conventions (fake data): company 1, date of the system 2026-03-02; solicitud n of client 1000 / portfolio 500, AGF person 7001 / portfolio 8001, medium TRA, CLP, 1.000.000, certified 2026-03-02, to be
 * liquidated 2026-03-03; modalities CHE MODCAJ, TRA MODDEP, CIN MODCIN; parameters LiquidacionCuentaInversionCliente N, VALIDAR_LIQUIDACION_OMNIBUS N, APV.Habilitado N. The doubles answer: Tesoreria generated the
 * movements named by the case (counter 0), CuentaInversion generated ONE movement, AgregarLog ok. RED BEFORE CODE: without the production classes every test fails with "T6-B not implemented".
 */
class LiqReglasTest {

    private static CasoLiq.Corrida c(String slug) throws Exception {
        return CasoLiq.ejecutar(CasoLiq.idPorSlug(slug));
    }

    private static JsonObject linea(CasoLiq.Corrida c, int referencia) {
        for (JsonElement e : CasoLiq.lineasTesoreria(c)) if (e.getAsJsonObject().get("NumeroReferencia").getAsInt() == referencia) return e.getAsJsonObject();
        throw new AssertionError("no line of Tesoreria with reference " + referencia);
    }

    private static JsonObject forma(CasoLiq.Corrida c, int referencia) {
        for (JsonElement e : CasoLiq.formasDePago(c)) if (e.getAsJsonObject().get("NumeroReferencia").getAsInt() == referencia) return e.getAsJsonObject();
        throw new AssertionError("no payment form with reference " + referencia);
    }

    private static String s(JsonObject o, String campo) {
        return o.get(campo).isJsonNull() ? null : o.get(campo).getAsString();
    }

    private static BigDecimal n(JsonObject o, String campo) {
        return new BigDecimal(o.get(campo).getAsString());
    }

    private static List<Long> ids(JsonArray resultados) {
        List<Long> out = new ArrayList<>();
        for (JsonElement e : resultados) out.add(e.getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong());
        return out;
    }

    private static List<Long> l(Long... v) {
        return List.of(v);
    }

    private static List<String> tipos(CasoLiq.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : CasoLiq.lineasTesoreria(c)) out.add(e.getAsJsonObject().get("NumeroReferencia").getAsString() + ":" + s(e.getAsJsonObject(), "IdTipoMovimiento") + ":" + s(e.getAsJsonObject(), "IdPersona"));
        return out;
    }

    private static List<String> montosCuentaInversion(CasoLiq.Corrida c, int llamada) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : CasoLiq.cargosAbonos(c, llamada)) out.add(new BigDecimal(e.getAsJsonObject().get("Monto").getAsString()).stripTrailingZeros().toPlainString());
        return out;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-09: the line of Tesoreria of a group (AGF), field by field
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-09: an APORTE (APORFM) of 1.000.000 by transfer makes ONE egress line MOVEGR / CAAEFM of the AGF person 7001, with EVERY field of the legacy (case un-aporte-genera-un-egreso-de-la-AGF)")
    void laLineaDeUnAporte() throws Exception {
        CasoLiq.Corrida c = c("un-aporte-genera-un-egreso-de-la-AGF");
        assertThat(CasoLiq.lineasTesoreria(c)).hasSize(1);
        JsonObject l = linea(c, 0);
        assertThat(s(l, "IdTipoMovimiento")).isEqualTo("MOVEGR");
        assertThat(s(l, "IdTipoPersona")).isEqualTo("CNT");
        assertThat(l.get("IdPersona").getAsLong()).isEqualTo(7001L);
        assertThat(l.get("IdPortafolio").getAsLong()).isZero();
        assertThat(s(l, "IdMoneda")).isEqualTo("CLP");
        assertThat(n(l, "Monto")).isEqualByComparingTo("1000000");
        assertThat(s(l, "FechaLiquidacion")).isEqualTo("2026-03-03");
        assertThat(s(l, "IdConcepto")).isEqualTo("CAAEFM");
        assertThat(s(l, "IdTipoPersonaTitularPago")).isEqualTo("CNT");
        assertThat(l.get("IdPersonaTitularPago").getAsLong()).isEqualTo(7001L);
        assertThat(l.get("IdPortafolioTitularPago").getAsLong()).isZero();
        assertThat(l.get("IdCaja").getAsLong()).isZero();
        assertThat(s(l, "SecuenciaAgrupacion")).isEmpty();
        assertThat(l.get("SecuenciaRelacionada").getAsLong()).isZero();
        assertThat(s(l, "Glosa")).isEmpty();
        assertThat(l.get("IdEmpleadoDigitador").getAsLong()).isZero();
        assertThat(l.get("IdSucursalEmpleadoDigitador").getAsLong()).isZero();
        assertThat(s(l, "IdTipoProducto")).isEqualTo("INFM");
        assertThat(s(l, "IdTipoOperacion")).isEqualTo("APORFM");
        assertThat(s(l, "IdTipoComprobante")).isEqualTo("LFM");
        assertThat(l.get("IdComprobante").getAsLong()).as("the folio of the CARTA goes in IdComprobante").isEqualTo(1L);
        assertThat(l.get("IdOperacion").getAsLong()).as("the number of the comprobante of liquidation goes in IdOperacion").isEqualTo(1L);
        assertThat(s(l, "FechaComprobante")).isEqualTo("0001-01-01");
        assertThat(s(l, "FechaLiquidacionComprobante")).isEqualTo("0001-01-01");
        assertThat(s(l, "FechaOperacion")).isEqualTo("2026-03-03");
        assertThat(s(l, "IdCondicionLiquidacion")).isEqualTo("T0");
        assertThat(s(l, "IdBolsa")).isEqualTo("BCS");
        assertThat(s(l, "IdMonedaOperacion")).isEqualTo("CLP");
        assertThat(n(l, "MontoOperacion")).as("the legacy never sets MontoOperacion").isEqualByComparingTo("0");
        assertThat(s(l, "IdTipoReajuste")).isEmpty();
        assertThat(s(l, "FechaParidad")).isEqualTo("0001-01-01");
        assertThat(n(l, "ValorParidad")).isEqualByComparingTo("0");
        assertThat(l.get("FolioCartera").getAsLong()).isZero();
        assertThat(l.get("IdInstrumento").getAsLong()).isZero();
        assertThat(s(l, "IdTipoInstrumento")).isEmpty();
        assertThat(s(l, "IdClasificacionTipoInstrumento")).isEmpty();
        assertThat(s(l, "Bilateral")).isEqualTo("N");
        assertThat(s(l, "GeneraComprobante")).isEqualTo("N");
        assertThat(s(l, "AgruparEnComprobante")).isEqualTo("N");
        assertThat(s(l, "MueveCuentaInversion")).as("liquidation NOT by the client's account: S").isEqualTo("S");
        assertThat(s(l, "TitularPago")).isEqualTo("N");
        JsonObject f = forma(c, 0);
        assertThat(f.get("NumeroReferenciaFormaDePago").getAsInt()).isZero();
        assertThat(s(f, "IdMedioDePago")).isEqualTo("TRA");
        assertThat(s(f, "IdModalidadDePago")).isEqualTo("MODDEP");
        assertThat(s(f, "IdMoneda")).isEqualTo("CLP");
        assertThat(n(f, "Monto")).isEqualByComparingTo("1000000");
        for (String campo : List.of("IdCuentaBancariaCorredora", "IdTarjetaBancariaCorredora", "IdChequera", "NumeroDocumentoCorredora", "IdCuentaBancariaSocio", "IdBancoSocio", "IdTarjetaBancariaSocio", "NumeroDocumentoSocio")) {
            assertThat(f.get(campo).getAsLong()).as(campo).isZero();
        }
        for (String campo : List.of("NombreCuentaBancariaSocio", "NumeroCuentaBancariaSocio", "IdTipoCuentaBancariaSocio", "NombreTipoCuentaBancariaSocio", "IdTipoTarjetaBancariaSocio", "NombreTipoTarjetaBancariaSocio",
                "NumeroTarjetaBancariaSocio", "NumeroSerieSocio")) {
            assertThat(s(f, campo)).as(campo).isEmpty();
        }
        assertThat(s(f, "FechaDocumentoPagoCorredora")).isEqualTo("0001-01-01");
        assertThat(s(f, "FechaDocumentoPagoSocio")).isEqualTo("0001-01-01");
    }

    @Test
    @DisplayName("FM-LIQ-09: a RESCATE (RESCFM) makes the opposite line: MOVING / ABREFM")
    void laLineaDeUnRescate() throws Exception {
        CasoLiq.Corrida c = c("un-rescate-genera-un-ingreso-de-la-AGF");
        JsonObject l = linea(c, 0);
        assertThat(s(l, "IdTipoMovimiento")).isEqualTo("MOVING");
        assertThat(s(l, "IdConcepto")).isEqualTo("ABREFM");
        assertThat(s(l, "IdTipoOperacion")).isEqualTo("RESCFM");
    }

    @Test
    @DisplayName("FM-LIQ-07/09: the amount of a group is the BigDecimal SUM of its solicitudes: 100.000 + 250.000 = 350.000; 100000.5 + 250000.25 = 350000.75; -500 + 0 - 0.5 = -500.5; and ONE line for the two")
    void elMontoDelGrupoEsLaSuma() throws Exception {
        CasoLiq.Corrida a = c("dos-aportes-del-mismo-grupo-suman-su-monto");
        assertThat(CasoLiq.lineasTesoreria(a)).hasSize(1);
        assertThat(n(linea(a, 0), "Monto")).isEqualByComparingTo("350000");
        assertThat(n(forma(a, 0), "Monto")).isEqualByComparingTo("350000");
        assertThat(CasoLiq.folio(a, 1)).isEqualTo(CasoLiq.folio(a, 2)).isEqualTo(1L);
        assertThat(n(linea(c("el-monto-del-grupo-conserva-los-decimales"), 0), "Monto")).isEqualByComparingTo("350000.75");
        assertThat(n(linea(c("monto-negativo-y-cero-en-un-grupo"), 0), "Monto")).isEqualByComparingTo("-500.5");
        assertThat(n(linea(c("monto-grande-con-ocho-decimales"), 0), "Monto")).isEqualByComparingTo("123456789012345.12345679");
    }

    @Test
    @DisplayName("FM-LIQ-09: the line takes the currency of the FUND (USD), not the one of the client's solicitud")
    void laMonedaEsLaDelFondo() throws Exception {
        assertThat(s(linea(c("un-aporte-en-dolares"), 0), "IdMoneda")).isEqualTo("USD");
        assertThat(s(forma(c("un-aporte-en-dolares"), 0), "IdMoneda")).isEqualTo("USD");
        assertThat(s(linea(c("el-grupo-usa-la-moneda-del-fondo-no-la-del-cliente"), 0), "IdMoneda")).isEqualTo("USD");
        assertThat(s(linea(c("el-grupo-usa-la-moneda-del-fondo-no-la-del-cliente"), 0), "IdMonedaOperacion")).isEqualTo("USD");
    }

    @Test
    @DisplayName("FM-LIQ-02/09: LiquidacionCuentaInversionCliente = S makes the line CLI with the portfolio of the AGF (8001) and MueveCuentaInversion N; N makes it CNT with portfolio 0 and MueveCuentaInversion S")
    void laLiquidacionPorCuentaDelCliente() throws Exception {
        for (String slug : List.of("liquidacion-por-cuenta-del-cliente-aporte", "liquidacion-por-cuenta-del-cliente-rescate")) {
            JsonObject l = linea(c(slug), 0);
            assertThat(s(l, "IdTipoPersona")).as(slug).isEqualTo("CLI");
            assertThat(s(l, "IdTipoPersonaTitularPago")).as(slug).isEqualTo("CLI");
            assertThat(l.get("IdPortafolio").getAsLong()).as(slug).isEqualTo(8001L);
            assertThat(l.get("IdPortafolioTitularPago").getAsLong()).as(slug).isEqualTo(8001L);
            assertThat(s(l, "MueveCuentaInversion")).as(slug).isEqualTo("N");
        }
        JsonObject porDefecto = linea(c("un-aporte-genera-un-egreso-de-la-AGF"), 0);
        assertThat(s(porDefecto, "IdTipoPersona")).isEqualTo("CNT");
        assertThat(porDefecto.get("IdPortafolio").getAsLong()).isZero();
        assertThat(s(porDefecto, "MueveCuentaInversion")).isEqualTo("S");
    }

    @Test
    @DisplayName("FM-LIQ-09: the payment form takes the modality of the table by medium (CHE MODCAJ, TRA MODDEP, CIN MODCIN); a medium that is not in the table (EFE) or an empty table gives MODCAJ")
    void laModalidadDeLaFormaDePago() throws Exception {
        assertThat(s(forma(c("medio-sin-modalidad-usa-MODCAJ"), 0), "IdMedioDePago")).isEqualTo("EFE");
        assertThat(s(forma(c("medio-sin-modalidad-usa-MODCAJ"), 0), "IdModalidadDePago")).isEqualTo("MODCAJ");
        assertThat(s(forma(c("medio-CIN-del-grupo-usa-su-modalidad"), 0), "IdMedioDePago")).isEqualTo("CIN");
        assertThat(s(forma(c("medio-CIN-del-grupo-usa-su-modalidad"), 0), "IdModalidadDePago")).isEqualTo("MODCIN");
        CasoLiq.Corrida vacia = c("tabla-de-modalidades-vacia-todo-es-MODCAJ");
        assertThat(s(forma(vacia, 0), "IdMedioDePago")).isEqualTo("CHE");
        assertThat(s(forma(vacia, 0), "IdModalidadDePago")).isEqualTo("MODCAJ");
        assertThat(s(forma(vacia, 1), "IdMedioDePago")).isEqualTo("TRA");
        assertThat(s(forma(vacia, 1), "IdModalidadDePago")).as("TRA is MODDEP only when the table says so").isEqualTo("MODCAJ");
        CasoLiq.Corrida dos = c("modalidades-de-dos-medios-se-asignan-por-medio");
        assertThat(s(forma(dos, 0), "IdModalidadDePago")).isEqualTo("MODCAJ");
        assertThat(s(forma(dos, 1), "IdModalidadDePago")).isEqualTo("MODDEP");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-07: grouping and the order of the groups (HashMap keyed by Grupo)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    static Stream<Arguments> gruposDistintos() {
        return Stream.of(
                Arguments.of("aportes-de-distinta-persona-AGF-son-dos-grupos", "person AGF 7001 vs 7002", l(1L, 2L), List.of("0:MOVEGR:7001", "1:MOVEGR:7002")),
                Arguments.of("un-aporte-y-un-rescate-iguales-son-dos-grupos", "TipoSolicitud APO vs RES", l(1L, 2L), List.of("0:MOVEGR:7001", "1:MOVING:7001")),
                Arguments.of("distinta-moneda-del-fondo-son-dos-grupos", "currency of the fund CLP vs USD", l(1L, 2L), List.of("0:MOVEGR:7001", "1:MOVEGR:7001")),
                Arguments.of("distinto-portafolio-AGF-son-dos-grupos", "portfolio AGF 8001 vs 8002", l(1L, 2L), List.of("0:MOVEGR:7001", "1:MOVEGR:7001")),
                Arguments.of("distinto-medio-de-cargo-AGF-son-dos-grupos", "medium TRA vs CHE (the CHE group is visited FIRST)", l(2L, 1L), List.of("0:MOVEGR:7001", "1:MOVEGR:7001")),
                Arguments.of("distinta-fecha-de-liquidacion-son-dos-grupos", "FechaLiquidacion 03-03 vs 03-04 (03-04 FIRST)", l(2L, 1L), List.of("0:MOVEGR:7001", "1:MOVEGR:7001")),
                Arguments.of("distinta-fecha-de-certificacion-son-dos-grupos", "FechaCertificacion 03-02 vs 03-01", l(1L, 2L), List.of("0:MOVEGR:7001", "1:MOVEGR:7001")));
    }

    @ParameterizedTest(name = "FM-LIQ-07 {0}: {1}")
    @MethodSource("gruposDistintos")
    @DisplayName("FM-LIQ-07: each field of Grupo (person AGF, type, currency of the fund, portfolio AGF, medium, dates) separates the groups: two lines, two sequences of 2, the folios by the order of the hash map")
    void cadaCampoDelGrupoSeparaLosGrupos(String slug, String que, List<Long> ordenDeResultados, List<String> lineas) throws Exception {
        CasoLiq.Corrida c = c(slug);
        assertThat(CasoLiq.lineasTesoreria(c)).as(que).hasSize(2);
        assertThat(tipos(c)).as(que).isEqualTo(lineas);
        assertThat(ids(CasoLiq.resultados(c))).as(que + ": the results come in the order of the references").isEqualTo(ordenDeResultados);
        assertThat(CasoLiq.secuencias(c)).as(que).containsExactly("FolioCartaLiquidacionAporteRescate=2", "SecuenciaComprobanteLiquidacion=2");
        assertThat(CasoLiq.folio(c, ordenDeResultados.get(0))).as(que).isEqualTo(1L);
        assertThat(CasoLiq.folio(c, ordenDeResultados.get(1))).as(que).isEqualTo(2L);
    }

    @Test
    @DisplayName("FM-LIQ-07: the medium of the group of an APORTE is IdMedioDePagoCargoAGF (CHE => MODCAJ), the one of a RESCATE is IdMedioDePagoAbonoAGF (TRA => MODDEP); the other one does not matter even when null")
    void elMedioDelGrupoDependeDelTipo() throws Exception {
        assertThat(s(forma(c("el-medio-del-grupo-de-un-aporte-es-el-de-cargo"), 0), "IdMedioDePago")).isEqualTo("CHE");
        assertThat(s(forma(c("el-medio-del-grupo-de-un-rescate-es-el-de-abono"), 0), "IdMedioDePago")).isEqualTo("TRA");
        CasoLiq.Corrida nulo = c("medio-de-abono-nulo-de-un-aporte-no-importa");
        assertThat(nulo.termino()).isTrue();
        assertThat(s(forma(nulo, 0), "IdMedioDePago")).isEqualTo("TRA");
    }

    @Test
    @DisplayName("FM-LIQ-07 (orden-por-hash): the groups are visited in the order of a java.util.HashMap keyed by Grupo (SUM of the hashes), NOT in the order of the request: six persons come out 7001..7006; ten groups come out in the recorded order")
    void elOrdenDeLosGruposEsElDelHashMap() throws Exception {
        CasoLiq.Corrida seis = c("seis-grupos-se-recorren-en-el-orden-del-hash");
        assertThat(tipos(seis)).containsExactly("0:MOVEGR:7001", "1:MOVEGR:7002", "2:MOVEGR:7003", "3:MOVEGR:7004", "4:MOVEGR:7005", "5:MOVEGR:7006");
        assertThat(CasoLiq.secuencias(seis)).containsExactly("FolioCartaLiquidacionAporteRescate=6", "SecuenciaComprobanteLiquidacion=6");
        CasoLiq.Corrida diez = c("diez-grupos-de-aportes-y-rescates-en-el-orden-del-hash");
        assertThat(tipos(diez)).containsExactly("0:MOVEGR:7210", "1:MOVING:7107", "2:MOVEGR:7202", "3:MOVING:7109", "4:MOVEGR:7206", "5:MOVEGR:7204", "6:MOVEGR:7208", "7:MOVING:7101", "8:MOVING:7103",
                "9:MOVING:7105");
        assertThat(ids(CasoLiq.resultados(diez))).as("the results follow the references").isEqualTo(l(10L, 7L, 2L, 9L, 6L, 4L, 8L, 1L, 3L, 5L));
        assertThat(CasoLiq.folio(diez, 10)).isEqualTo(1L);
        assertThat(CasoLiq.folio(diez, 5)).isEqualTo(10L);
        CasoLiq.Corrida once = c("los-once-tipos-de-movimiento-se-seleccionan-y-el-doce-no");
        assertThat(ids(CasoLiq.resultados(once))).isEqualTo(l(11L, 2L, 1L, 4L, 3L, 5L, 6L, 8L, 7L, 9L, 10L));
    }

    @Test
    @DisplayName("FM-LIQ-07/20: a null in the key (person AGF, portfolio AGF, the medium that applies, the currency of the fund) raises NullPointerException from Grupo.hashCode and nothing is written; the medium that does NOT apply may be null")
    void unNuloEnLaClaveLanzaNPE() throws Exception {
        for (String slug : List.of("persona-AGF-nula-lanza-NPE", "portafolio-AGF-nulo-lanza-NPE", "medio-de-cargo-nulo-de-un-aporte-lanza-NPE", "medio-de-abono-nulo-de-un-rescate-lanza-NPE", "moneda-del-fondo-nula-lanza-NPE")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(c.excepcionTexto).as(slug).startsWith("NullPointerException:");
            assertThat(c.volcado.get("decisionServlet").getAsString()).as(slug).isEqualTo("ROLLBACK_HTTP500");
            assertThat(CasoLiq.llamadas(c, CasoLiq.TESORERIA)).as(slug + ": no collaborator was reached").isEmpty();
            assertThat(CasoLiq.secuencias(c)).as(slug + ": no sequence was asked").isEmpty();
            assertThat(CasoLiq.estadoLiquidacion(c, 1)).as(slug).isEqualTo("ING");
        }
    }

    @Test
    @DisplayName("FM-LIQ-09/20: a null Monto raises NullPointerException when the group is summed: AFTER the two sequences were asked (the folios are consumed) and BEFORE Tesoreria")
    void unMontoNuloLanzaNPE() throws Exception {
        CasoLiq.Corrida c = c("monto-nulo-lanza-NPE-al-sumar");
        assertThat(c.excepcionTexto).startsWith("NullPointerException:");
        assertThat(CasoLiq.secuencias(c)).containsExactly("FolioCartaLiquidacionAporteRescate=1", "SecuenciaComprobanteLiquidacion=1");
        assertThat(CasoLiq.llamadas(c, CasoLiq.TESORERIA)).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-02: parameters
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-02: a missing parameter is the plain Exception 'Parametro X en modulo Y no encontrado' (LiquidacionCuentaInversionCliente and VALIDAR_LIQUIDACION_OMNIBUS of FONDOMUTUO, PLA.FechaSistema and APV.Habilitado of PLATAFORMA), nothing is called or written")
    void unParametroFaltanteLanzaException() throws Exception {
        assertThat(c("falta-LiquidacionCuentaInversionCliente").excepcionTexto).isEqualTo("Exception: Parametro LiquidacionCuentaInversionCliente en modulo FONDOMUTUO no encontrado");
        assertThat(c("falta-VALIDAR_LIQUIDACION_OMNIBUS").excepcionTexto).isEqualTo("Exception: Parametro VALIDAR_LIQUIDACION_OMNIBUS en modulo FONDOMUTUO no encontrado");
        assertThat(c("falta-la-fecha-del-sistema").excepcionTexto).isEqualTo("Exception: Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado");
        CasoLiq.Corrida apv = c("falta-APV.Habilitado-de-la-empresa-1");
        assertThat(apv.excepcionTexto).isEqualTo("Exception: Parametro APV.Habilitado en modulo PLATAFORMA no encontrado");
        assertThat(CasoLiq.llamadas(apv, CasoLiq.TESORERIA)).isEmpty();
        assertThat(CasoLiq.secuencias(apv)).isEmpty();
        assertThat(CasoLiq.estadoLiquidacion(apv, 1)).isEqualTo("ING");
    }

    @Test
    @DisplayName("FM-LIQ-02 (apv-empresa-1) / D-F3B-18: APV.Habilitado is read with company 1 FIXED: a request of company 2 with S in company 1 and N in company 2 gets the APV lines; with N in company 1 and S in company 2 it does not")
    void apvSeLeeConLaEmpresaUno() throws Exception {
        CasoLiq.Corrida con = c("APV-se-lee-de-la-empresa-1-aunque-la-solicitud-sea-de-la-2");
        assertThat(CasoLiq.entradaDe(con, CasoLiq.TESORERIA, 0).get("p_IdEmpresa").getAsLong()).isEqualTo(2L);
        assertThat(tipos(con)).containsExactly("0:MOVEGR:7001", "1:MOVING:1000");
        assertThat(CasoLiq.secuencias(con)).containsExactly("FolioCartaLiquidacionAporteRescate=2", "SecuenciaComprobanteLiquidacion=2");
        CasoLiq.Corrida sin = c("APV-de-la-empresa-2-no-cuenta-si-la-1-dice-N");
        assertThat(tipos(sin)).containsExactly("0:MOVEGR:7001");
        assertThat(CasoLiq.secuencias(sin)).containsExactly("FolioCartaLiquidacionAporteRescate=1", "SecuenciaComprobanteLiquidacion=1");
    }

    @Test
    @DisplayName("FM-LIQ-02: the date of the system (PLA.FechaSistema) is what CrearCargoAbono gets as p_FechaSistema")
    void laFechaDelSistemaVaAlaCuentaDeInversion() throws Exception {
        CasoLiq.Corrida c = c("la-fecha-del-sistema-va-a-la-cuenta-de-inversion");
        assertThat(s(CasoLiq.entradaDe(c, CasoLiq.CUENTA_INVERSION, 0), "p_FechaSistema")).isEqualTo("2026-04-15");
    }

    @Test
    @DisplayName("FM-LIQ-02/20: a request with no company raises NullPointerException (the kit answers 'Parametro ... no encontrado': DESVIO-KIT-1) and nothing is called")
    void sinEmpresa() throws Exception {
        CasoLiq.Corrida c = c("sin-empresa-lanza-excepcion");
        assertThat(c.excepcionTexto).satisfiesAnyOf(t -> assertThat(t).startsWith("NullPointerException:"), t -> assertThat(t).startsWith("Exception: Parametro ").endsWith(" no encontrado"));
        assertThat(CasoLiq.llamadas(c, CasoLiq.TESORERIA)).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-01 / 03: the request, no filters, no movements
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-03 (sin-filtros-sin-mov) / D-F3B-22: no movements and no filters raise NoSuchElementException 'No value present' (findFirst().get()), nothing is called, nothing is written")
    void sinMovimientosNiFiltros() throws Exception {
        CasoLiq.Corrida c = c("sin-movimientos-ni-filtros-lanza-NoSuchElement");
        assertThat(c.excepcionTexto).isEqualTo("NoSuchElementException: No value present");
        assertThat(c.volcado.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_HTTP500");
        assertThat(c.volcado.getAsJsonArray("colaboradores")).isEmpty();
        assertThat(CasoLiq.secuencias(c)).isEmpty();
    }

    @Test
    @DisplayName("FM-LIQ-03 / FM-LIQ-01 (conteo-por-referencia): no filters WITH movements adds the error NO_SE_INGRESARON_FILTROS (id 0, built with the constant, so the recount counts it: counter 1) but DOES NOT stop: it liquidates, then the host rolls back")
    void sinFiltrosConMovimientosAvisaYLiquida() throws Exception {
        for (String slug : List.of("sin-filtros-pero-con-movimientos-avisa-y-liquida", "filtros-ausente-con-movimientos-avisa-y-liquida")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(CasoLiq.contador(c)).as(slug).isEqualTo(1L);
            assertThat(CasoLiq.mensajes(c)).as(slug).hasSize(1);
            JsonObject m = CasoLiq.mensajes(c).get(0).getAsJsonObject();
            assertThat(m.get("id").getAsLong()).isZero();
            assertThat(s(m, "tipo")).isEqualTo("ERROR");
            assertThat(s(m, "codigo")).isEqualTo("NO_SE_INGRESARON_FILTROS");
            assertThat(s(m, "descripcion")).isEqualTo("No se ingresaron filtros");
            assertThat(ids(CasoLiq.resultados(c))).as(slug + ": it liquidated").isEqualTo(l(1L));
            assertThat(CasoLiq.estadoLiquidacion(c, 1)).as(slug + ": the class left it LIQ").isEqualTo("LIQ");
            assertThat(CasoLiq.decision(c)).as(slug).isEqualTo("ROLLBACK");
            assertThat(c.volcado.getAsJsonObject("tablas").getAsJsonArray(CasoLiq.SOL).get(0).getAsJsonObject().get("EstadoLiquidacion").getAsString()).as(slug + ": the host reverted it").isEqualTo("ING");
        }
    }

    @Test
    @DisplayName("FM-LIQ-03 / FM-LIQ-13 (mensajes-perdidos): the error of Tesoreria REPLACES the messages gathered before (NO_SE_INGRESARON_FILTROS is lost): the output holds only ERROR_TESORERIA")
    void elErrorDeTesoreriaReemplazaElAviso() throws Exception {
        CasoLiq.Corrida c = c("sin-filtros-y-tesoreria-con-error-pierde-el-aviso");
        assertThat(CasoLiq.mensajes(c)).hasSize(1);
        assertThat(s(CasoLiq.mensajes(c).get(0).getAsJsonObject(), "codigo")).isEqualTo("ERROR_TESORERIA");
        assertThat(CasoLiq.resultados(c)).isEmpty();
    }

    @Test
    @DisplayName("FM-LIQ-01/20: p_Movimientos null and p_Filtros null raise NullPointerException; p_Movimientos absent is the empty list of the class (the filters select)")
    void listasNulasOAusentes() throws Exception {
        assertThat(c("movimientos-nulo-lanza-NPE").excepcionTexto).startsWith("NullPointerException:");
        CasoLiq.Corrida filtros = c("filtros-nulo-lanza-NPE-antes-de-todo");
        assertThat(filtros.excepcionTexto).startsWith("NullPointerException:");
        assertThat(filtros.volcado.getAsJsonArray("colaboradores")).as("it fails at the first validation: nothing is called").isEmpty();
        CasoLiq.Corrida ausente = c("movimientos-ausente-usa-la-lista-vacia-y-los-filtros");
        assertThat(ids(CasoLiq.resultados(ausente))).isEqualTo(l(1L));
        assertThat(CasoLiq.contador(ausente)).isZero();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-04: selection by movements
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    static Stream<Arguments> seleccionPorMovimientos() {
        return Stream.of(
                Arguments.of("los-movimientos-eligen-por-id", "ids 2 and 3 of four", l(2L, 3L)),
                Arguments.of("movimiento-ya-liquidado-no-se-selecciona", "EstadoLiquidacion LIQ is not ING", l(2L)),
                Arguments.of("movimiento-de-otro-tipo-no-se-selecciona", "APOAJU is not one of the eleven types", l(2L)),
                Arguments.of("movimiento-de-otra-empresa-no-se-selecciona", "IdEmpresa = the one of the request", l(2L)),
                Arguments.of("ids-de-movimiento-repetidos", "[1, 1] selects the solicitud once", l(1L)),
                Arguments.of("movimiento-sin-id", "[null] selects nothing", List.<Long>of()),
                Arguments.of("movimientos-que-no-existen-llaman-a-tesoreria-con-listas-vacias", "no such id selects nothing", List.<Long>of()),
                Arguments.of("movimientos-presentes-ignoran-los-filtros-para-seleccionar", "the filter that would select nothing is ignored", l(1L)));
    }

    @ParameterizedTest(name = "FM-LIQ-04 {0}: {1}")
    @MethodSource("seleccionPorMovimientos")
    @DisplayName("FM-LIQ-04: the movements select by id, company, EstadoLiquidacion ING and the eleven movement types; the results are the liquidated ones")
    void seleccionPorMovimientos(String slug, String que, List<Long> esperados) throws Exception {
        CasoLiq.Corrida c = c(slug);
        assertThat(c.termino()).as(que).isTrue();
        assertThat(ids(CasoLiq.resultados(c))).as(que).isEqualTo(esperados);
        assertThat(CasoLiq.contador(c)).as(que).isZero();
    }

    @Test
    @DisplayName("FM-LIQ-04: nothing selected still calls Tesoreria ONCE with EMPTY lists, asks for no sequence (0 groups), writes nothing and answers counter 0")
    void nadaSeleccionadoLlamaConListasVacias() throws Exception {
        for (String slug : List.of("movimientos-que-no-existen-llaman-a-tesoreria-con-listas-vacias", "omnibus-S-todos-sin-omnibus-no-queda-nada", "movimiento-sin-id")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(CasoLiq.llamadas(c, CasoLiq.TESORERIA)).as(slug).hasSize(1);
            assertThat(CasoLiq.lineasTesoreria(c)).as(slug).isEmpty();
            assertThat(CasoLiq.formasDePago(c)).as(slug).isEmpty();
            assertThat(CasoLiq.secuencias(c)).as(slug).isEmpty();
            assertThat(CasoLiq.resultados(c)).as(slug).isEmpty();
            assertThat(CasoLiq.decision(c)).as(slug).isEqualTo("COMMIT");
        }
    }

    @Test
    @DisplayName("FM-LIQ-04: the eleven movement types (APONORM RESNORM APOREINV RESREINV APORESPRO APOOVER RESTRAPOR RESTRASP APOTRASP APODIVCFI RESCNN) are selected, the twelfth (RESAJU) is not: 11 groups, 11 results, solicitud 12 stays ING")
    void losOnceTipos() throws Exception {
        CasoLiq.Corrida c = c("los-once-tipos-de-movimiento-se-seleccionan-y-el-doce-no");
        assertThat(CasoLiq.resultados(c)).hasSize(11);
        assertThat(CasoLiq.secuencias(c)).containsExactly("FolioCartaLiquidacionAporteRescate=11", "SecuenciaComprobanteLiquidacion=11");
        assertThat(CasoLiq.estadoLiquidacion(c, 12)).isEqualTo("ING");
        assertThat(CasoLiq.estadoLiquidacion(c, 11)).isEqualTo("LIQ");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-05: selection by the FIRST filter
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    static Stream<Arguments> seleccionPorFiltro() {
        return Stream.of(
                Arguments.of("el-filtro-de-aportes-por-fechas", "APO + certification 2026-03-02 + liquidation 2026-03-03 (not the RES, not the other date)", l(1L)),
                Arguments.of("fechas-vacias-del-filtro-se-vuelven-0001-01-01-y-no-seleccionan", "empty dates become 0001-01-01 and filter by it: nothing", List.<Long>of()),
                Arguments.of("una-solicitud-con-fechas-0001-01-01-si-sale-con-el-filtro-vacio", "a row dated 0001-01-01 IS selected by the empty dates", l(1L)),
                Arguments.of("el-filtro-por-emisor", "IdEmisor AGF2", l(2L)),
                Arguments.of("el-filtro-por-serie-del-fondo", "IdSerieFondo FM002A", l(2L)),
                Arguments.of("el-filtro-por-tipo-rescate-solo-trae-los-certificados", "RES with IdEstado ACP: a rescate is ALWAYS CRT", l(1L)),
                Arguments.of("el-filtro-por-estado-de-un-aporte", "APO with IdEstado ACP", l(2L)),
                Arguments.of("el-filtro-sin-tipo-trae-aportes-de-cualquier-estado-y-rescates-certificados", "no type, no state: every aporte and the CRT rescates", l(1L, 2L, 3L)),
                Arguments.of("solo-se-usa-el-primer-filtro", "two filters: only the first (FM001A) selects", l(1L)),
                Arguments.of("el-filtro-no-trae-lo-ya-liquidado", "EstadoLiquidacion LIQ is not selected", l(2L)),
                Arguments.of("el-filtro-con-emisor-nulo", "a null IdEmisor is not a filter", l(1L)));
    }

    @ParameterizedTest(name = "FM-LIQ-05 {0}: {1}")
    @MethodSource("seleccionPorFiltro")
    @DisplayName("FM-LIQ-05: with no movements the FIRST filter selects (type, state, issuer, series, dates; empty date = 0001-01-01); the results are the liquidated ones")
    void seleccionPorFiltro(String slug, String que, List<Long> esperados) throws Exception {
        CasoLiq.Corrida c = c(slug);
        assertThat(c.termino()).as(que).isTrue();
        assertThat(ids(CasoLiq.resultados(c))).as(que).isEqualTo(esperados);
    }

    @Test
    @DisplayName("FM-LIQ-05 / FM-LIQ-20: a null FechaCertificacion or FechaLiquidacion in the first filter raises NullPointerException before the select; a null filter in the list raises it too; nothing is called")
    void filtrosNulos() throws Exception {
        for (String slug : List.of("el-filtro-con-fecha-de-certificacion-nula-lanza-NPE", "el-filtro-con-fecha-de-liquidacion-nula-lanza-NPE", "un-filtro-nulo-en-la-lista-lanza-NPE-al-seleccionar")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(c.excepcionTexto).as(slug).startsWith("NullPointerException:");
            assertThat(c.volcado.getAsJsonArray("colaboradores")).as(slug).isEmpty();
            assertThat(CasoLiq.secuencias(c)).as(slug).isEmpty();
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-06: omnibus
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-06 (omnibus-silencioso) / D-F3B-23: with VALIDAR_LIQUIDACION_OMNIBUS = S the aportes (APO) with IdOmnibus 0 are REMOVED with NO message (the legacy message sits under an impossible condition): counter 0, no message, solicitud 1 stays ING")
    void omnibusQuitaEnSilencio() throws Exception {
        CasoLiq.Corrida c = c("omnibus-S-quita-en-silencio-los-aportes-sin-omnibus");
        assertThat(CasoLiq.mensajes(c)).isEmpty();
        assertThat(CasoLiq.contador(c)).isZero();
        assertThat(ids(CasoLiq.resultados(c))).isEqualTo(l(2L));
        assertThat(CasoLiq.estadoLiquidacion(c, 1)).isEqualTo("ING");
        assertThat(CasoLiq.secuencias(c)).containsExactly("FolioCartaLiquidacionAporteRescate=1", "SecuenciaComprobanteLiquidacion=1");
        CasoLiq.Corrida todos = c("omnibus-S-todos-sin-omnibus-no-queda-nada");
        assertThat(CasoLiq.mensajes(todos)).isEmpty();
        assertThat(CasoLiq.resultados(todos)).isEmpty();
    }

    @Test
    @DisplayName("FM-LIQ-06: only an APORTE with IdOmnibus exactly 0 and the flag S is removed: a RESCATE with 0, an aporte with NULL omnibus, the flag N, and any value other than S (s) all stay")
    void soloSeQuitaElAporteSinOmnibus() throws Exception {
        for (String slug : List.of("omnibus-S-un-rescate-sin-omnibus-se-queda", "omnibus-S-un-aporte-con-omnibus-nulo-se-queda", "omnibus-N-no-quita-nada", "omnibus-con-valor-que-no-es-S-no-quita-nada")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(ids(CasoLiq.resultados(c))).as(slug).isEqualTo(l(1L));
        }
    }

    @Test
    @DisplayName("FM-LIQ-06/08: with APV enabled the sequences count what is LEFT after the omnibus removal (1 group + 1 solicitud = 2)")
    void omnibusYApvCuentanLoQueQueda() throws Exception {
        CasoLiq.Corrida c = c("omnibus-S-con-APV-cuenta-solo-lo-que-queda");
        assertThat(CasoLiq.secuencias(c)).containsExactly("FolioCartaLiquidacionAporteRescate=2", "SecuenciaComprobanteLiquidacion=2");
        assertThat(CasoLiq.estadoLiquidacion(c, 1)).isEqualTo("ING");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-10 / 12: the lines of the client (APV)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-10/12 (carta-lista-completa): with APV an APORTE adds the client's line MOVING for Monto - MontoComision (1.000.000 - 10.000 = 990.000) with ITS reference 1, sequences G + S = 2, and it goes to Tesoreria AND to its payment forms")
    void laLineaDelClienteDeUnAporte() throws Exception {
        CasoLiq.Corrida c = c("APV-un-aporte-agrega-la-linea-del-cliente");
        assertThat(tipos(c)).containsExactly("0:MOVEGR:7001", "1:MOVING:1000");
        JsonObject cliente = linea(c, 1);
        assertThat(s(cliente, "IdConcepto")).isEqualTo("ABREFM");
        assertThat(s(cliente, "IdTipoOperacion")).isEqualTo("RESCFM");
        assertThat(n(cliente, "Monto")).isEqualByComparingTo("990000");
        assertThat(cliente.get("IdComprobante").getAsLong()).isEqualTo(2L);
        assertThat(cliente.get("IdOperacion").getAsLong()).isEqualTo(2L);
        assertThat(n(forma(c, 1), "Monto")).isEqualByComparingTo("990000");
        assertThat(s(forma(c, 1), "IdMedioDePago")).isEqualTo("TRA");
        assertThat(CasoLiq.secuencias(c)).containsExactly("FolioCartaLiquidacionAporteRescate=2", "SecuenciaComprobanteLiquidacion=2");
    }

    @Test
    @DisplayName("FM-LIQ-12: the client's line of a RESCATE (MOVEGR) and the one of a CIN solicitud never go to Tesoreria: only the group's line does; with a CIN client the sequences still count them")
    void lasLineasQueNoVanATesoreria() throws Exception {
        for (String slug : List.of("APV-un-rescate-agrega-una-linea-que-no-va-a-tesoreria", "APV-un-aporte-pagado-con-CIN-no-va-a-tesoreria", "APV-un-rescate-pagado-con-CIN-no-va-a-tesoreria")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(CasoLiq.lineasTesoreria(c)).as(slug).hasSize(1);
            assertThat(CasoLiq.formasDePago(c)).as(slug).hasSize(1);
            assertThat(CasoLiq.secuencias(c)).as(slug).containsExactly("FolioCartaLiquidacionAporteRescate=2", "SecuenciaComprobanteLiquidacion=2");
        }
    }

    @Test
    @DisplayName("FM-LIQ-10: the client's amount is Monto - MontoComision (975.000 for 1.000.000 - 25.000), with the person and portfolio of the SOLICITUD (client 1234 / portfolio 567 with the client's account), not the AGF's")
    void elMontoYLaPersonaDelCliente() throws Exception {
        assertThat(n(linea(c("APV-aporte-con-comision-y-monto-del-cliente"), 1), "Monto")).isEqualByComparingTo("975000");
        JsonObject l = linea(c("APV-persona-y-portafolio-de-la-solicitud-con-cuenta-del-cliente"), 1);
        assertThat(l.get("IdPersona").getAsLong()).isEqualTo(1234L);
        assertThat(l.get("IdPortafolio").getAsLong()).isEqualTo(567L);
        assertThat(s(l, "IdTipoPersona")).isEqualTo("CLI");
        assertThat(l.get("IdPersonaTitularPago").getAsLong()).isEqualTo(1234L);
        JsonObject agf = linea(c("APV-persona-y-portafolio-de-la-solicitud-con-cuenta-del-cliente"), 0);
        assertThat(agf.get("IdPersona").getAsLong()).isEqualTo(7009L);
        assertThat(agf.get("IdPortafolio").getAsLong()).isEqualTo(8009L);
    }

    @Test
    @DisplayName("FM-LIQ-10/20: with APV a null Monto or a null medium of the solicitud raises NullPointerException (nothing reaches Tesoreria)")
    void apvConNulos() throws Exception {
        for (String slug : List.of("APV-monto-nulo-lanza-NPE", "APV-medio-nulo-de-la-solicitud-lanza-NPE")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(c.excepcionTexto).as(slug).startsWith("NullPointerException:");
            assertThat(CasoLiq.llamadas(c, CasoLiq.TESORERIA)).as(slug).isEmpty();
        }
    }

    @Test
    @DisplayName("FM-LIQ-12/14 (carta-lista-completa, resultados-duplicados) / D-F3B-20/25: the folio stored under EVERY reference is the COMPLETE list of solicitudes: two aportes of ONE group with APV report 6 results ([1,2] three times) and the folio left in the table is the LAST one (3)")
    void laCartaGuardaLaListaCompleta() throws Exception {
        CasoLiq.Corrida c = c("APV-dos-aportes-en-un-grupo-repiten-los-resultados");
        assertThat(tipos(c)).containsExactly("0:MOVEGR:7001", "1:MOVING:1000", "2:MOVING:1000");
        assertThat(ids(CasoLiq.resultados(c))).isEqualTo(l(1L, 2L, 1L, 2L, 1L, 2L));
        assertThat(CasoLiq.folio(c, 1)).isEqualTo(3L);
        assertThat(CasoLiq.folio(c, 2)).isEqualTo(3L);
        CasoLiq.Corrida uno = c("APV-un-aporte-agrega-la-linea-del-cliente");
        assertThat(ids(CasoLiq.resultados(uno))).as("one solicitud, two references: reported twice").isEqualTo(l(1L, 1L));
        assertThat(CasoLiq.folio(uno, 1)).as("the folio of the LAST reference").isEqualTo(2L);
        CasoLiq.Corrida tres = c("con-APV-se-piden-grupos-mas-solicitudes");
        assertThat(CasoLiq.secuencias(tres)).containsExactly("FolioCartaLiquidacionAporteRescate=5", "SecuenciaComprobanteLiquidacion=5");
        assertThat(CasoLiq.resultados(tres)).hasSize(12);
        assertThat(CasoLiq.folio(tres, 1)).isEqualTo(5L);
        assertThat(CasoLiq.folio(tres, 3)).isEqualTo(5L);
    }

    @Test
    @DisplayName("FM-LIQ-14 (estado-ING): each result carries Codigo UI_EXITO, IdInterno 0, the series of the solicitud, Estado = the state READ (ING, not LIQ) and NombreEstado empty")
    void elResultadoDiceING() throws Exception {
        CasoLiq.Corrida c = c("un-aporte-genera-un-egreso-de-la-AGF");
        JsonObject r = CasoLiq.resultados(c).get(0).getAsJsonObject();
        assertThat(r.get("IdInterno").getAsLong()).isZero();
        assertThat(r.get("IdSolicitudAporteRescate").getAsLong()).isEqualTo(1L);
        assertThat(s(r, "IdSerieFondo")).isEqualTo("FM001A");
        assertThat(s(r, "Codigo")).isEqualTo("UI_EXITO");
        assertThat(s(r, "Estado")).isEqualTo("ING");
        assertThat(s(r, "NombreEstado")).isEmpty();
        assertThat(CasoLiq.estadoLiquidacion(c, 1)).as("the row of the table IS LIQ").isEqualTo("LIQ");
        assertThat(CasoLiq.fila(c, 1).get("FechaLiquidacionRegistro").getAsString()).isEqualTo("2026-03-02 14:30:00");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-11: withholding APV-B (visible in the lines of CuentaInversion, or in a MOVING line)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    static Stream<Arguments> retenciones() {
        // the rig: the client's line of the APV-B rescate (MontoPago 1.000.000, commission 10.000) is the THIRD line of the call; the first two are the groups of the AGF
        return Stream.of(
                Arguments.of("retencion-APV-B-en-un-RESNORM", "15% on a RESNORM: (1.000.000 - 150.000) - 10.000", "840000"),
                Arguments.of("retencion-APV-B-en-un-RESTRASP", "15% on a RESTRASP", "840000"),
                Arguments.of("sin-retencion-en-otro-tipo-de-movimiento", "RESREINV has no withholding: 1.000.000 - 10.000", "990000"),
                Arguments.of("sin-retencion-si-el-portafolio-no-es-APV-B", "APV-A has no withholding", "990000"),
                Arguments.of("sin-retencion-si-el-medio-es-CIN", "medium CIN has no withholding", "990000"),
                Arguments.of("la-primera-fila-con-porcentaje-cero-se-salta", "a 0% row is skipped, the 10% one applies: (1.000.000 - 100.000) - 10.000", "890000"),
                Arguments.of("las-filas-que-no-son-de-rescate-se-saltan", "rows APO and TRA are not RES: no withholding", "990000"),
                Arguments.of("una-fila-de-otro-regimen-se-salta-y-la-sin-regimen-aplica", "a row of regimen 99 is skipped, the one with no regimen (12%) applies", "870000"),
                Arguments.of("una-fila-del-mismo-regimen-aplica", "a row of regimen 1 (the solicitud's matrix) applies: 18%", "810000"),
                Arguments.of("gana-la-primera-fila-que-aplica", "two rows apply (15%, 20%): the FIRST", "840000"),
                Arguments.of("sin-filas-de-retencion-no-hay-retencion", "no configuration rows", "990000"),
                Arguments.of("retencion-con-porcentaje-fraccionario", "12.345% of 1.234.567,89: BigDecimal arithmetic, 7 decimals", "1072160.4839795"),
                Arguments.of("retencion-sobre-un-pago-menor-que-la-comision", "MontoPago 1.000, commission 50.000: negative amount", "-49150"));
    }

    @ParameterizedTest(name = "FM-LIQ-11 {0}: {1} => {2}")
    @MethodSource("retenciones")
    @DisplayName("FM-LIQ-11 (comparacion-de-tipos): the client's amount of an APV-B RESNORM / RESTRASP rescate paid with a medium other than CIN is (MontoPago - MontoPago x P) - MontoComision with the FIRST row RES, P <> 0, of regimen null or equal; otherwise Monto - MontoComision")
    void laRetencion(String slug, String que, String esperado) throws Exception {
        CasoLiq.Corrida c = c(slug);
        List<String> montos = montosCuentaInversion(c, 0);
        assertThat(montos).as(que).hasSize(4);
        assertThat(montos.get(0)).as("the groups of the AGF are never touched").isEqualTo("1000000");
        assertThat(montos.get(2)).as(que).isEqualTo(esperado);
    }

    @Test
    @DisplayName("FM-LIQ-11/20: a row of a regimen with a null IdMatrizAgrupadorAPV, or a null CodigoPortafolio, raises NullPointerException after the sequences and before Tesoreria")
    void retencionConNulos() throws Exception {
        for (String slug : List.of("regimen-de-la-fila-con-matriz-nula-lanza-NPE", "retencion-con-portafolio-nulo-lanza-NPE")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(c.excepcionTexto).as(slug).startsWith("NullPointerException:");
            assertThat(CasoLiq.llamadas(c, CasoLiq.TESORERIA)).as(slug).isEmpty();
            assertThat(CasoLiq.secuencias(c)).as(slug).containsExactly("FolioCartaLiquidacionAporteRescate=4", "SecuenciaComprobanteLiquidacion=4");
        }
    }

    @Test
    @DisplayName("FM-LIQ-11/12: a solicitud of type APO whose movement is RESNORM (inconsistent data) gets a MOVING line WITH the withholding: (1.000.000 - 150.000) - 10.000 = 840.000 in the line AND in its payment form")
    void retencionEnUnaLineaDeIngreso() throws Exception {
        CasoLiq.Corrida c = c("retencion-en-una-linea-de-ingreso-llega-a-tesoreria-y-a-la-forma-de-pago");
        assertThat(n(linea(c, 1), "Monto")).isEqualByComparingTo("840000");
        assertThat(n(forma(c, 1), "Monto")).isEqualByComparingTo("840000");
        assertThat(n(linea(c, 0), "Monto")).isEqualByComparingTo("1000000");
        assertThat(n(linea(c("APV-tipo-de-movimiento-RESNORM-con-solicitud-de-aporte"), 1), "Monto")).as("with no configuration rows: Monto - MontoComision").isEqualByComparingTo("990000");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-13: the call to Tesoreria
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-13 / FM-LIQ-19 (simulacion-escribe): the header is copied (p_IdEmpresa, p_IdUsuario, p_IdModulo, p_SimularOperacion, p_UsaReglasNegocio), p_EsIngresoAutomatico is N, and a null header is passed as null")
    void laCabeceraDeTesoreria() throws Exception {
        JsonObject normal = CasoLiq.entradaDe(c("un-aporte-genera-un-egreso-de-la-AGF"), CasoLiq.TESORERIA, 0);
        assertThat(normal.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(normal.get("p_IdUsuario").getAsLong()).isEqualTo(7L);
        assertThat(s(normal, "p_IdModulo")).isEqualTo("FMM");
        assertThat(s(normal, "p_SimularOperacion")).isEqualTo("N");
        assertThat(s(normal, "p_UsaReglasNegocio")).isEqualTo("S");
        assertThat(s(normal, "p_EsIngresoAutomatico")).isEqualTo("N");
        JsonObject otra = CasoLiq.entradaDe(c("la-cabecera-pasa-tal-cual-a-tesoreria"), CasoLiq.TESORERIA, 0);
        assertThat(otra.get("p_IdUsuario").getAsLong()).isEqualTo(15L);
        assertThat(s(otra, "p_IdModulo")).isEqualTo("XYZ");
        assertThat(s(otra, "p_UsaReglasNegocio")).isEqualTo("N");
        JsonObject nula = CasoLiq.entradaDe(c("cabecera-sin-modulo-ni-usuario-pasa-nula"), CasoLiq.TESORERIA, 0);
        assertThat(nula.get("p_IdUsuario").isJsonNull()).isTrue();
        assertThat(nula.get("p_IdModulo").isJsonNull()).isTrue();
        assertThat(nula.get("p_SimularOperacion").isJsonNull()).isTrue();
        assertThat(nula.get("p_UsaReglasNegocio").isJsonNull()).isTrue();
        assertThat(s(nula, "p_EsIngresoAutomatico")).isEqualTo("N");
    }

    @Test
    @DisplayName("FM-LIQ-19 (simulacion-escribe) / D-F3B-24: p_SimularOperacion = S goes ONLY to Tesoreria; the UPDATE of the solicitudes runs anyway (counter 0, COMMIT) and when Tesoreria generated nothing (what a simulation does) nothing is updated and the result is empty")
    void laSimulacionSoloVaATesoreria() throws Exception {
        CasoLiq.Corrida c = c("la-solicitud-simulada-pasa-a-tesoreria");
        assertThat(s(CasoLiq.entradaDe(c, CasoLiq.TESORERIA, 0), "p_SimularOperacion")).isEqualTo("S");
        assertThat(CasoLiq.estadoLiquidacion(c, 1)).isEqualTo("LIQ");
        assertThat(CasoLiq.decision(c)).isEqualTo("COMMIT");
        CasoLiq.Corrida vacia = c("simulada-y-tesoreria-no-genera-nada-no-actualiza");
        assertThat(CasoLiq.resultados(vacia)).isEmpty();
        assertThat(CasoLiq.estadoLiquidacion(vacia, 1)).isEqualTo("ING");
        assertThat(CasoLiq.secuencias(vacia)).as("the folios were consumed anyway").containsExactly("FolioCartaLiquidacionAporteRescate=1", "SecuenciaComprobanteLiquidacion=1");
        CasoLiq.Corrida cin = c("la-cuenta-de-inversion-siempre-va-con-modulo-FMM-y-sin-simular");
        assertThat(s(CasoLiq.entradaDe(cin, CasoLiq.TESORERIA, 0), "p_SimularOperacion")).isEqualTo("S");
        JsonObject ca = CasoLiq.entradaDe(cin, CasoLiq.CUENTA_INVERSION, 0);
        assertThat(s(ca, "p_simularOperacion")).as("CrearCargoAbono never simulates").isEqualTo("N");
        assertThat(s(ca, "p_IdModulo")).as("module FMM fixed, whatever the request says").isEqualTo("FMM");
        assertThat(s(ca, "p_UsaReglasNegocio")).as("business rules S fixed").isEqualTo("S");
        assertThat(s(ca, "p_EsIngresoManual")).isEqualTo("N");
        assertThat(ca.get("p_IdUsuario").getAsLong()).as("the user IS the one of the request").isEqualTo(15L);
        assertThat(CasoLiq.estadoLiquidacion(cin, 1)).isEqualTo("LIQ");
    }

    @Test
    @DisplayName("FM-LIQ-13/01 (conteo-por-referencia, mensajes-perdidos): a Tesoreria counter > 0 stops the run: no UPDATE, no CrearCargoAbono, no AgregarLog; the output carries ITS messages, and call() recounts them by REFERENCE: messages of a collaborator never count (counter 0, COMMIT)")
    void unErrorDeTesoreriaCorta() throws Exception {
        for (String slug : List.of("tesoreria-con-error-devuelve-su-contador-y-sus-mensajes", "tesoreria-con-contador-y-sin-mensajes", "el-log-no-se-llama-si-tesoreria-devuelve-error")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(CasoLiq.resultados(c)).as(slug).isEmpty();
            assertThat(CasoLiq.estadoLiquidacion(c, 1)).as(slug).isEqualTo("ING");
            assertThat(CasoLiq.llamadas(c, CasoLiq.LOG)).as(slug + ": the log is not reached").isEmpty();
            assertThat(CasoLiq.llamadas(c, CasoLiq.CUENTA_INVERSION)).as(slug).isEmpty();
            assertThat(CasoLiq.contador(c)).as(slug + ": the recount by reference").isZero();
            assertThat(CasoLiq.decision(c)).as(slug).isEqualTo("COMMIT");
        }
        CasoLiq.Corrida dos = c("tesoreria-con-error-devuelve-su-contador-y-sus-mensajes");
        assertThat(CasoLiq.mensajes(dos)).hasSize(2);
        assertThat(s(CasoLiq.mensajes(dos).get(0).getAsJsonObject(), "codigo")).isEqualTo("ERR_TES_1");
        assertThat(s(CasoLiq.mensajes(dos).get(1).getAsJsonObject(), "codigo")).isEqualTo("ERR_TES_2");
        assertThat(CasoLiq.mensajes(c("tesoreria-con-contador-y-sin-mensajes"))).isEmpty();
    }

    @Test
    @DisplayName("FM-LIQ-13: Tesoreria with a null counter, with counter 0 and ERROR messages, or with a warning, does NOT stop the run: it liquidates and the messages of Tesoreria are NOT copied")
    void tesoreriaQueNoCorta() throws Exception {
        for (String slug : List.of("tesoreria-con-contador-nulo-sigue", "tesoreria-con-contador-cero-y-mensajes-de-error-sigue", "tesoreria-con-advertencia-sigue-y-no-la-copia")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(ids(CasoLiq.resultados(c))).as(slug).isEqualTo(l(1L));
            assertThat(CasoLiq.mensajes(c)).as(slug).isEmpty();
            assertThat(CasoLiq.llamadas(c, CasoLiq.LOG)).as(slug).hasSize(1);
        }
    }

    @Test
    @DisplayName("FM-LIQ-13/20: an exception of Tesoreria, a null list of generated movements, a reference that is not in the map (99, null) or a movement with no group raise and write nothing: the folios of the sequences WERE consumed (2 asked)")
    void tesoreriaQueFalla() throws Exception {
        CasoLiq.Corrida excepcion = c("tesoreria-lanza-excepcion");
        assertThat(excepcion.excepcionTexto).isEqualTo("Exception: FALLA_SIMULADA_TESORERIA");
        assertThat(CasoLiq.secuencias(excepcion)).containsExactly("FolioCartaLiquidacionAporteRescate=1", "SecuenciaComprobanteLiquidacion=1");
        CasoLiq.Corrida folios = c("las-secuencias-se-consumen-aunque-despues-falle");
        assertThat(folios.excepcionTexto).isEqualTo("Exception: FALLA_DESPUES_DE_PEDIR_FOLIOS");
        assertThat(CasoLiq.secuencias(folios)).containsExactly("FolioCartaLiquidacionAporteRescate=2", "SecuenciaComprobanteLiquidacion=2");
        for (String slug : List.of("tesoreria-con-lista-de-generados-nula-lanza-NPE", "tesoreria-con-referencia-desconocida-lanza-NPE", "tesoreria-con-referencia-nula-lanza-NPE", "tesoreria-genera-un-movimiento-sin-grupos-lanza-NPE")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(c.excepcionTexto).as(slug).startsWith("NullPointerException:");
            assertThat(CasoLiq.estadoLiquidacion(c, 1)).as(slug).isEqualTo("ING");
            assertThat(CasoLiq.llamadas(c, CasoLiq.LOG)).as(slug).isEmpty();
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-14: the update
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-14 (resultados-duplicados): the SAME reference twice updates its solicitudes twice and reports them twice; a reference that Tesoreria did not generate leaves its group ING (the other group, folio 2, is LIQ)")
    void laActualizacionSigueALosGenerados() throws Exception {
        CasoLiq.Corrida doble = c("tesoreria-con-la-misma-referencia-dos-veces-actualiza-dos-veces");
        assertThat(ids(CasoLiq.resultados(doble))).isEqualTo(l(1L, 1L));
        assertThat(CasoLiq.folio(doble, 1)).isEqualTo(1L);
        CasoLiq.Corrida uno = c("tesoreria-solo-genera-un-grupo-el-otro-queda-ingresado");
        assertThat(ids(CasoLiq.resultados(uno))).isEqualTo(l(2L));
        assertThat(CasoLiq.estadoLiquidacion(uno, 1)).isEqualTo("ING");
        assertThat(CasoLiq.estadoLiquidacion(uno, 2)).isEqualTo("LIQ");
        assertThat(CasoLiq.folio(uno, 2)).isEqualTo(2L);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-15 / 16 / 17: cuenta de inversion
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-15 (cin-referencia-0, movcar-siempre) / D-F3B-21: with the client's account and a CIN solicitud CrearCargoAbono gets ONE line per line of the list; the type is MOVCAR / CAPEFM / RESCATE_FONDO_MUTUO ALWAYS (the comparison with APO never holds), IdTipoOperacion ABPEFM for an aporte, RESCFM for a rescate")
    void laLineaDeCuentaDeInversion() throws Exception {
        CasoLiq.Corrida aporte = c("cuenta-del-cliente-y-un-aporte-CIN-genera-un-cargo-abono");
        assertThat(CasoLiq.cargosAbonos(aporte, 0)).hasSize(1);
        JsonObject o = CasoLiq.cargosAbonos(aporte, 0).get(0).getAsJsonObject();
        assertThat(o.get("NumeroCargoAbono").getAsLong()).isEqualTo(1L);
        assertThat(o.get("IdPortafolio").getAsLong()).isEqualTo(8001L);
        assertThat(s(o, "IdMoneda")).isEqualTo("CLP");
        assertThat(s(o, "IdTipoMovimiento")).isEqualTo("MOVCAR");
        assertThat(s(o, "IdConcepto")).isEqualTo("CAPEFM");
        assertThat(s(o, "Descripcion")).isEqualTo("RESCATE_FONDO_MUTUO");
        assertThat(s(o, "IdCondicionDeLiquidacion")).isEqualTo("T0");
        assertThat(s(o, "FechaCondicionDeLiquidacion")).isEqualTo("2026-03-03");
        assertThat(n(o, "Monto")).isEqualByComparingTo("1000000");
        assertThat(o.get("IdEjecutivo").getAsLong()).isZero();
        assertThat(o.get("IdSucursal").getAsLong()).isZero();
        assertThat(s(o, "IdTipoProducto")).isEqualTo("INFM");
        assertThat(s(o, "IdTipoOperacion")).isEqualTo("ABPEFM");
        assertThat(o.get("IdOperacion").getAsLong()).isEqualTo(1L);
        assertThat(s(o, "FechaOperacion")).isEqualTo("2026-03-03");
        assertThat(s(o, "FechaLiquidacionOperacion")).isEqualTo("0001-01-01");
        assertThat(o.get("FechaVencimientoOperacion").isJsonNull()).isTrue();
        assertThat(s(o, "IdMonedaOperacion")).isEqualTo("CLP");
        assertThat(n(o, "MontoOperacion")).isEqualByComparingTo("1000000");
        assertThat(n(o, "TipoCambioOperacion")).isEqualByComparingTo("0");
        assertThat(o.get("IdContraparte").getAsLong()).isZero();
        assertThat(s(o, "IdTipoComprobante")).isEqualTo("FMM");
        assertThat(o.get("IdComprobante").isJsonNull()).as("IdComprobante is never set").isTrue();
        assertThat(o.get("IdComprobanteDetalle").getAsLong()).isEqualTo(1L);
        assertThat(s(o, "FolioComprobante")).isEqualTo("1");
        assertThat(o.get("IdComprobanteFormaDePago").getAsLong()).isEqualTo(1L);
        assertThat(s(o, "IdTipoMovimientoAsociado")).isEmpty();
        assertThat(o.get("IdMovimientoAsociado").getAsLong()).isZero();
        assertThat(s(o, "FechaComprobante")).isEqualTo("2026-03-03");
        assertThat(o.get("IdPersonaPagoCobro").isJsonNull()).isTrue();
        assertThat(o.get("IdInstrumento").getAsLong()).isZero();
        assertThat(s(o, "IdTipoInstrumento")).isEmpty();
        assertThat(s(o, "Nemo")).isEmpty();
        assertThat(o.get("EsIndicadorBilateral").isJsonNull()).isTrue();
        assertThat(s(o, "EsCargoAbonoPorTraspaso")).isEqualTo("N");
        assertThat(o.get("IdPortafolioTraspaso").isJsonNull()).isTrue();
        assertThat(s(o, "GeneroContraparteEnTesoreria")).isEqualTo("N");
        assertThat(s(o, "CertificadoAutomatico")).isEqualTo("S");
        JsonObject cabecera = CasoLiq.entradaDe(aporte, CasoLiq.CUENTA_INVERSION, 0);
        assertThat(cabecera.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(cabecera.get("p_IdUsuario").getAsLong()).isEqualTo(7L);
        assertThat(s(cabecera, "p_IdModulo")).isEqualTo("FMM");
        assertThat(s(cabecera, "p_simularOperacion")).isEqualTo("N");
        assertThat(s(cabecera, "p_UsaReglasNegocio")).isEqualTo("S");
        assertThat(s(cabecera, "p_FechaSistema")).isEqualTo("2026-03-02");
        assertThat(s(cabecera, "p_EsIngresoManual")).isEqualTo("N");
        JsonObject rescate = CasoLiq.cargosAbonos(c("cuenta-del-cliente-y-un-rescate-CIN"), 0).get(0).getAsJsonObject();
        assertThat(s(rescate, "IdTipoMovimiento")).as("a rescate is MOVCAR too").isEqualTo("MOVCAR");
        assertThat(s(rescate, "IdTipoOperacion")).isEqualTo("RESCFM");
    }

    @Test
    @DisplayName("FM-LIQ-15: no call to CuentaInversion unless the client's account is S AND a solicitud has medium CIN (N with a CIN solicitud: none; S with no CIN: none)")
    void cuandoSeLlamaALaCuentaDeInversion() throws Exception {
        assertThat(CasoLiq.llamadas(c("sin-cuenta-del-cliente-un-CIN-no-llama-a-cuenta-de-inversion"), CasoLiq.CUENTA_INVERSION)).isEmpty();
        assertThat(CasoLiq.llamadas(c("cuenta-del-cliente-sin-solicitudes-CIN-no-llama"), CasoLiq.CUENTA_INVERSION)).isEmpty();
        assertThat(CasoLiq.llamadas(c("cuenta-del-cliente-y-un-aporte-CIN-genera-un-cargo-abono"), CasoLiq.CUENTA_INVERSION)).hasSize(1);
    }

    @Test
    @DisplayName("FM-LIQ-15 (producto-n-por-m): one CrearCargoAbono PER CIN solicitud, each with ALL the lines of the list and the counter continuing across calls: two CIN solicitudes of two groups => 2 calls of 2 lines, numbers 1,2 and 3,4")
    void unaLlamadaPorCadaCin() throws Exception {
        CasoLiq.Corrida c = c("dos-CIN-de-grupos-distintos-llaman-dos-veces-con-todas-las-lineas");
        assertThat(CasoLiq.llamadas(c, CasoLiq.CUENTA_INVERSION)).hasSize(2);
        assertThat(CasoLiq.cargosAbonos(c, 0)).hasSize(2);
        assertThat(CasoLiq.cargosAbonos(c, 1)).hasSize(2);
        assertThat(CasoLiq.cargosAbonos(c, 0).get(0).getAsJsonObject().get("NumeroCargoAbono").getAsLong()).isEqualTo(1L);
        assertThat(CasoLiq.cargosAbonos(c, 0).get(1).getAsJsonObject().get("NumeroCargoAbono").getAsLong()).isEqualTo(2L);
        assertThat(CasoLiq.cargosAbonos(c, 1).get(0).getAsJsonObject().get("NumeroCargoAbono").getAsLong()).isEqualTo(3L);
        assertThat(CasoLiq.cargosAbonos(c, 1).get(1).getAsJsonObject().get("NumeroCargoAbono").getAsLong()).isEqualTo(4L);
        CasoLiq.Corrida apv = c("CIN-con-APV-manda-todas-las-lineas-incluida-la-del-cliente");
        assertThat(montosCuentaInversion(apv, 0)).containsExactly("1000000", "990000");
        assertThat(s(CasoLiq.cargosAbonos(apv, 0).get(1).getAsJsonObject(), "IdTipoOperacion")).as("the client's line of an aporte is RESCFM: the AGF's APORFM becomes ABPEFM, the rest RESCFM").isEqualTo("RESCFM");
        assertThat(CasoLiq.cargosAbonos(apv, 0).get(1).getAsJsonObject().get("IdPortafolio").getAsLong()).as("the client's portfolio (500)").isEqualTo(500L);
    }

    @Test
    @DisplayName("FM-LIQ-17 (cin-referencia-0, resultados-duplicados) / D-F3B-26: for each movement CuentaInversion generated the class re-updates and re-reports the solicitudes of carta_solicitud[0] (always the reference 0): one CIN aporte with 1 movement reports it twice, with 3 movements four times; with 0 movements once")
    void laReferenciaCeroSeRepite() throws Exception {
        assertThat(ids(CasoLiq.resultados(c("cuenta-del-cliente-y-un-aporte-CIN-genera-un-cargo-abono")))).isEqualTo(l(1L, 1L));
        assertThat(ids(CasoLiq.resultados(c("cada-cargo-abono-generado-repite-la-actualizacion")))).isEqualTo(l(1L, 1L, 1L, 1L));
        assertThat(ids(CasoLiq.resultados(c("cuenta-de-inversion-sin-movimientos-generados-no-actualiza")))).isEqualTo(l(1L));
        assertThat(ids(CasoLiq.resultados(c("dos-CIN-de-grupos-distintos-llaman-dos-veces-con-todas-las-lineas")))).as("results of the groups, then once per call: group 0 each time").isEqualTo(l(1L, 2L, 1L, 1L));
    }

    @Test
    @DisplayName("FM-LIQ-17 (cin-referencia-0) / D-F3B-26: with TWO solicitudes in group 0 the folio is read with K = the index of the solicitud: the second one asks secuencia_carta.get(1) of a list of 1 and the run raises IndexOutOfBoundsException (Index 1 out of bounds for length 1) AFTER the update (the host reverts it)")
    void dosCinEnElGrupoCeroLanzanIndexOutOfBounds() throws Exception {
        CasoLiq.Corrida c = c("dos-CIN-en-el-mismo-grupo-lanzan-IndexOutOfBounds");
        assertThat(c.excepcionTexto).startsWith("IndexOutOfBoundsException:");
        assertThat(CasoLiq.estadoLiquidacion(c, 1)).as("the class had already updated both").isEqualTo("LIQ");
        assertThat(c.volcado.getAsJsonObject("tablas").getAsJsonArray(CasoLiq.SOL).get(0).getAsJsonObject().get("EstadoLiquidacion").getAsString()).isEqualTo("ING");
        assertThat(montosCuentaInversion(c, 0)).as("the two solicitudes are ONE group: one line of 2.000.000").containsExactly("2000000");
    }

    @Test
    @DisplayName("FM-LIQ-16 (conteo-por-referencia, mensajes-perdidos) / D-REQ-11: an error of CuentaInversion (counter > 0, or an ERROR message even with counter 0, in any case) stops the run, REPLACES the messages and keeps the results already gathered; call() recounts by reference: 0, COMMIT")
    void unErrorDeCuentaInversion() throws Exception {
        for (String slug : List.of("cuenta-de-inversion-con-error-devuelve-su-contador-y-sus-mensajes", "cuenta-de-inversion-con-contador-cero-y-mensaje-ERROR-sale-igual", "cuenta-de-inversion-mensaje-error-en-minusculas-cuenta")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(CasoLiq.mensajes(c)).as(slug).hasSize(1);
            assertThat(ids(CasoLiq.resultados(c))).as(slug + ": the results of the Tesoreria pass stay").isEqualTo(l(1L));
            assertThat(CasoLiq.llamadas(c, CasoLiq.LOG)).as(slug + ": the log is not reached").isEmpty();
            assertThat(CasoLiq.contador(c)).as(slug).isZero();
            assertThat(CasoLiq.decision(c)).as(slug).isEqualTo("COMMIT");
        }
        assertThat(s(CasoLiq.mensajes(c("cuenta-de-inversion-mensaje-error-en-minusculas-cuenta")).get(0).getAsJsonObject(), "tipo")).isEqualTo("error");
        CasoLiq.Corrida sinFiltros = c("cuenta-de-inversion-con-error-pierde-el-aviso-de-filtros-y-conserva-los-resultados");
        assertThat(CasoLiq.mensajes(sinFiltros)).as("NO_SE_INGRESARON_FILTROS was lost").hasSize(1);
        assertThat(s(CasoLiq.mensajes(sinFiltros).get(0).getAsJsonObject(), "codigo")).isEqualTo("ERR_CIN");
    }

    @Test
    @DisplayName("FM-LIQ-16/20: a warning of CuentaInversion does not stop the run; a null counter, a null list of movements, or an exception of the collaborator raise (after the update of the first pass, reverted by the host)")
    void cuentaDeInversionQueNoCortaOQueFalla() throws Exception {
        CasoLiq.Corrida advertencia = c("cuenta-de-inversion-con-advertencia-sigue");
        assertThat(CasoLiq.mensajes(advertencia)).isEmpty();
        assertThat(CasoLiq.llamadas(advertencia, CasoLiq.LOG)).hasSize(1);
        assertThat(c("cuenta-de-inversion-con-contador-nulo-lanza-NPE").excepcionTexto).startsWith("NullPointerException:");
        assertThat(c("cuenta-de-inversion-con-lista-de-generados-nula-lanza-NPE").excepcionTexto).startsWith("NullPointerException:");
        CasoLiq.Corrida excepcion = c("cuenta-de-inversion-lanza-excepcion");
        assertThat(excepcion.excepcionTexto).isEqualTo("Exception: FALLA_SIMULADA_CIN");
        assertThat(CasoLiq.estadoLiquidacion(excepcion, 1)).isEqualTo("LIQ");
        assertThat(excepcion.volcado.getAsJsonObject("tablas").getAsJsonArray(CasoLiq.SOL).get(0).getAsJsonObject().get("EstadoLiquidacion").getAsString()).isEqualTo("ING");
    }

    @Test
    @DisplayName("FM-LIQ-15/20: with the client's account a solicitud with a NULL medium raises NullPointerException when the loop of CIN reads it, AFTER Tesoreria and the update")
    void medioNuloConCuentaDelCliente() throws Exception {
        CasoLiq.Corrida c = c("medio-nulo-con-cuenta-del-cliente-lanza-NPE-despues-de-actualizar");
        assertThat(c.excepcionTexto).startsWith("NullPointerException:");
        assertThat(CasoLiq.llamadas(c, CasoLiq.TESORERIA)).hasSize(1);
        assertThat(CasoLiq.estadoLiquidacion(c, 1)).isEqualTo("LIQ");
    }

    @Test
    @DisplayName("FM-LIQ-15: the date of the system and the user of the request travel to CuentaInversion; with the APV flag the client's line is in the list too (2 lines: 1.000.000 and 990.000)")
    void laCuentaDeInversionRecibeFechaYUsuario() throws Exception {
        CasoLiq.Corrida c = c("la-fecha-del-sistema-va-a-la-cuenta-de-inversion");
        assertThat(CasoLiq.cargosAbonos(c, 0)).hasSize(1);
        assertThat(CasoLiq.entradaDe(c, CasoLiq.CUENTA_INVERSION, 0).get("p_IdUsuario").getAsLong()).isEqualTo(7L);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-18: the log
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-18 (log-sin-asignar) / D-F3B-19: AgregarLog is called ONCE with the DEFAULT entry (p_IdTrabajo 1, p_Log and p_LogDetalle EMPTY, header null): the list of logs built per filter (three filters) is never assigned")
    void elLogNuncaSeAsigna() throws Exception {
        for (String slug : List.of("el-log-se-arma-por-filtro-y-nunca-se-asigna", "el-log-con-un-filtro-sin-id-interno", "un-aporte-genera-un-egreso-de-la-AGF")) {
            CasoLiq.Corrida c = c(slug);
            assertThat(CasoLiq.llamadas(c, CasoLiq.LOG)).as(slug).hasSize(1);
            JsonObject e = CasoLiq.entradaDe(c, CasoLiq.LOG, 0);
            assertThat(e.get("p_IdTrabajo").getAsLong()).as(slug).isEqualTo(1L);
            assertThat(e.getAsJsonArray("p_Log")).as(slug).isEmpty();
            assertThat(e.getAsJsonArray("p_LogDetalle")).as(slug).isEmpty();
            for (String campo : List.of("p_IdEmpresa", "p_IdUsuario", "p_IdModulo", "p_UsaReglasNegocio", "p_IdEstadoAutorizacion")) assertThat(e.get(campo).isJsonNull()).as(slug + " " + campo).isTrue();
        }
    }

    @Test
    @DisplayName("FM-LIQ-18/20: the log is the LAST step: after the update and the cuenta de inversion; an exception of AgregarLog, or a null filter in the list, raise AFTER the writes (the host reverts them); the answer of AgregarLog is ignored")
    void elLogEsElUltimoPaso() throws Exception {
        CasoLiq.Corrida excepcion = c("el-log-lanza-excepcion-despues-de-escribir");
        assertThat(excepcion.excepcionTexto).isEqualTo("Exception: FALLA_SIMULADA_LOG");
        assertThat(CasoLiq.estadoLiquidacion(excepcion, 1)).isEqualTo("LIQ");
        assertThat(excepcion.volcado.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_HTTP500");
        CasoLiq.Corrida nulo = c("un-filtro-nulo-en-la-lista-lanza-NPE-al-registrar-el-log");
        assertThat(nulo.excepcionTexto).startsWith("NullPointerException:");
        assertThat(CasoLiq.estadoLiquidacion(nulo, 1)).isEqualTo("LIQ");
        assertThat(CasoLiq.llamadas(nulo, CasoLiq.LOG)).as("the NPE is in the loop that BUILDS the logs, before the call").isEmpty();
        CasoLiq.Corrida ignora = c("el-log-ignora-lo-que-conteste-AgregarLog");
        assertThat(CasoLiq.mensajes(ignora)).isEmpty();
        assertThat(CasoLiq.contador(ignora)).isZero();
        List<String> orden = new ArrayList<>();
        for (JsonElement e : c("cuenta-del-cliente-y-un-aporte-CIN-genera-un-cargo-abono").volcado.getAsJsonArray("colaboradores")) orden.add(e.getAsJsonObject().get("colaborador").getAsString());
        assertThat(orden).containsExactly("CrearIngresoEgreso", "CrearCargoAbono", "AgregarLog");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-LIQ-08: sequences
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-08: ONE request per sequence, in this order, of G values (one per group) or G + S with APV; with zero groups NO request is recorded")
    void lasSecuencias() throws Exception {
        assertThat(CasoLiq.secuencias(c("un-aporte-genera-un-egreso-de-la-AGF"))).containsExactly("FolioCartaLiquidacionAporteRescate=1", "SecuenciaComprobanteLiquidacion=1");
        assertThat(CasoLiq.secuencias(c("dos-aportes-del-mismo-grupo-suman-su-monto"))).as("two aportes of one group: ONE value").containsExactly("FolioCartaLiquidacionAporteRescate=1", "SecuenciaComprobanteLiquidacion=1");
        assertThat(CasoLiq.secuencias(c("con-APV-se-piden-grupos-mas-solicitudes"))).as("2 groups + 3 solicitudes").containsExactly("FolioCartaLiquidacionAporteRescate=5", "SecuenciaComprobanteLiquidacion=5");
        assertThat(CasoLiq.secuencias(c("sin-solicitudes-no-se-piden-secuencias"))).isEmpty();
    }

    @Test
    @DisplayName("FM-LIQ-08/09: the folio of the carta and the number of the comprobante of each line are the values of the two sequences taken in the order of the references (1, 2)")
    void losFoliosVanEnLaLinea() throws Exception {
        CasoLiq.Corrida c = c("los-folios-de-carta-y-de-comprobante-van-en-la-linea-de-tesoreria");
        assertThat(linea(c, 0).get("IdComprobante").getAsLong()).isEqualTo(1L);
        assertThat(linea(c, 0).get("IdOperacion").getAsLong()).isEqualTo(1L);
        assertThat(linea(c, 1).get("IdComprobante").getAsLong()).isEqualTo(2L);
        assertThat(linea(c, 1).get("IdOperacion").getAsLong()).isEqualTo(2L);
        assertThat(CasoLiq.folio(c, 1)).isEqualTo(1L);
        assertThat(CasoLiq.folio(c, 2)).isEqualTo(2L);
    }

    @Test
    @DisplayName("FM-LIQ-08/09/14: the two sequences are DIFFERENT values: with the carta starting at 700 and the comprobante at 50 the line carries IdComprobante = 700 (the carta) and IdOperacion = 50 (the comprobante of liquidation), the table gets the carta (700, 701)")
    void laCartaYElComprobanteSonSecuenciasDistintas() throws Exception {
        CasoLiq.Corrida c = c("la-carta-y-el-comprobante-vienen-de-secuencias-distintas");
        assertThat(linea(c, 0).get("IdComprobante").getAsLong()).isEqualTo(700L);
        assertThat(linea(c, 0).get("IdOperacion").getAsLong()).isEqualTo(50L);
        assertThat(linea(c, 1).get("IdComprobante").getAsLong()).isEqualTo(701L);
        assertThat(linea(c, 1).get("IdOperacion").getAsLong()).isEqualTo(51L);
        assertThat(CasoLiq.folio(c, 1)).isEqualTo(700L);
        assertThat(CasoLiq.folio(c, 2)).isEqualTo(701L);
        CasoLiq.Corrida cin = c("la-cuenta-de-inversion-usa-el-folio-de-la-carta-como-operacion");
        JsonObject o = CasoLiq.cargosAbonos(cin, 0).get(0).getAsJsonObject();
        assertThat(o.get("IdOperacion").getAsLong()).as("IdOperacion of the cargo is the folio of the CARTA").isEqualTo(700L);
        assertThat(s(o, "FolioComprobante")).isEqualTo("700");
        assertThat(o.get("IdComprobanteDetalle").getAsLong()).isEqualTo(700L);
        assertThat(CasoLiq.folio(cin, 1)).isEqualTo(700L);
        assertThat(linea(cin, 0).get("IdOperacion").getAsLong()).isEqualTo(50L);
    }

    @Test
    @DisplayName("FM-LIQ-09/10: the payment form of the client's line (APV) also falls back to MODCAJ when its medium (EFE) is not in the table of modalities")
    void laModalidadDeLaFormaDelCliente() throws Exception {
        CasoLiq.Corrida c = c("APV-medio-del-cliente-sin-modalidad-usa-MODCAJ");
        assertThat(s(forma(c, 0), "IdModalidadDePago")).isEqualTo("MODDEP");
        assertThat(s(forma(c, 1), "IdMedioDePago")).isEqualTo("EFE");
        assertThat(s(forma(c, 1), "IdModalidadDePago")).isEqualTo("MODCAJ");
        assertThat(n(forma(c, 1), "Monto")).isEqualByComparingTo("990000");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the matrix is the matrix
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-01..20: every card and every quirk tag is named by at least one case (the matrix covers what these tests pin)")
    void laMatrizCubreLasTarjetasYLosQuirks() {
        for (int n = 1; n <= 20; n++) {
            String tarjeta = String.format("FM-LIQ-%02d", n);
            assertThat(CasoLiq.conTag(tarjeta)).as(tarjeta).isNotEmpty();
        }
        for (String quirk : List.of("log-sin-asignar", "carta-lista-completa", "cin-referencia-0", "apv-empresa-1", "omnibus-silencioso", "conteo-por-referencia", "primer-filtro", "fecha-vacia-0001", "estado-ING",
                "resultados-duplicados", "producto-n-por-m", "movcar-siempre", "simulacion-escribe", "mensajes-perdidos", "orden-por-hash", "comparacion-de-tipos", "secuencias-consumidas")) {
            assertThat(CasoLiq.conTag("LIQ-quirk:" + quirk)).as(quirk).isNotEmpty();
        }
        assertThat(CasoLiq.conTag("LIQ-quirk:sin-filtros-sin-mov")).isNotEmpty();
        assertThat(CasoLiq.casos().size()).isGreaterThanOrEqualTo(80);
    }
}
