package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;

/**
 * Rule by rule, with LITERAL values: every card of FM-CER-29, 30, 42, 43 (front F-E, cuenta de inversion), FM-CER-44..47 (front F-F, Tesoreria) and FM-CER-59, 60 (front F-H, Spot) of
 * RULES_FONDOMUTUO_F3_B.md 8.1 and the defects they pin (D-F3B-01/03/07/08/09/14/17/41/42/43/44, D-NOR-9) against the units of F-E, F-F and F-H (see {@code API_ASSUMED_T3.md}). The expected values are
 * the ones the unmodified legacy produced when the goldens were recorded: they are written here as literals read from the golden of the case (each assertion names the case), so a change of
 * the golden that is not a change of the rule shows up here. Each test runs the case through {@link CasoCerT3} (F-A and F-B of T1 REAL, then the nine steps of F-C..F-H) and asserts on what the
 * NEW code did: the lines it sent to the cuenta de inversion, the entries it sent to Tesoreria, the operation and forms it sent to Spot, the state of each solicitud, the columns the UPDATE
 * will write and the counter and messages of the output.
 *
 * <p>Projections (the same text for every case): {@code cuenta} = {@code NumeroCargoAbono|IdTipoMovimiento|IdMoneda|Monto|MontoOperacion|IdConcepto|FechaCondicionDeLiquidacion|FechaLiquidacionOperacion|
 * FechaOperacion|TipoCambioOperacion}; {@code tesoreria} = {@code IdInterno|MontoPagoLocal|RelacionTraspaso|IdMedioDePago|IdPersonaTraspaso|IdCuentaBancaria|IdBanco|NumeroCuentaBancaria|IdConcepto|
 * FechaEmision|ParidadFechaInversion|IdAutorizadoOrigen}; {@code spotOperaciones} = {@code IdInterno|Monto|IdMedioDePago|GeneraSpot}; {@code spotFormas} = {@code IdOperacion|RecibePaga|IdTipoMovimiento|
 * IdFormaDePago|IdMoneda|MontoFormaDePago|AfectarTesoreria|FechaLiquidacion}; numbers by value, dates as {@code yyyy-MM-dd}.
 *
 * <p>RED BEFORE CODE: until the units exist every test fails with "T3 not implemented: the production class ... does not exist yet".
 */
class CerT3ReglasTest {

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** The run of the case; F-C..F-H must have finished without an exception. */
    private static CasoCerT3.Corrida corre(String slug) throws Exception {
        CasoCerT3.Corrida c = CasoCerT3.ejecutar(CasoCerT3.idPorSlug(slug));
        assertNotNull(c.contexto, slug + " must reach the seam; it ended with " + c.excepcion);
        assertTrue(c.terminoF_C_F_D(), slug + " must finish F-C..F-H; it raised " + c.excepcion);
        return c;
    }

    /** The run of a case in which the legacy throws INSIDE F-C..F-H. */
    private static CasoCerT3.Corrida lanza(String slug) throws Exception {
        CasoCerT3.Corrida c = CasoCerT3.ejecutar(CasoCerT3.idPorSlug(slug));
        assertNotNull(c.contexto, slug + " must reach the seam; it ended with " + c.excepcion);
        assertFalse(c.terminoF_C_F_D(), slug + " must raise inside F-C..F-H");
        return c;
    }

    private static List<JsonObject> llamadas(CasoCerT3.Corrida c, String colaborador) {
        return CerCosturaT3.llamadas(c.volcado.getAsJsonArray("colaboradores"), colaborador);
    }

    private static JsonObject arg(JsonObject llamada) {
        return llamada.getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    private static String num(JsonElement e) {
        return new BigDecimal(e.getAsString()).stripTrailingZeros().toPlainString();
    }

    private static String txt(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        if (e == null || e.isJsonNull()) return "null";
        if (e.getAsJsonPrimitive().isNumber()) return new BigDecimal(e.getAsString()).signum() == 0 ? "0" : num(e);
        return e.getAsString();
    }

    private static String fecha(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        return e == null || e.isJsonNull() ? "null" : e.getAsString().substring(0, 10);
    }

    private static String une(String... partes) {
        return String.join("|", partes);
    }

    private static List<String> cuenta(CasoCerT3.Corrida c, int k) {
        List<String> out = new ArrayList<>();
        List<JsonObject> ll = llamadas(c, "CrearCargoAbono");
        assertTrue(ll.size() > k, "the call " + k + " to CrearCargoAbono was not made (" + ll.size() + " calls)");
        for (JsonElement e : arg(ll.get(k)).getAsJsonArray("p_CargoAbonoAGenerar")) {
            JsonObject l = e.getAsJsonObject();
            out.add(une(txt(l, "NumeroCargoAbono"), txt(l, "IdTipoMovimiento"), txt(l, "IdMoneda"), txt(l, "Monto"), txt(l, "MontoOperacion"), txt(l, "IdConcepto"), fecha(l, "FechaCondicionDeLiquidacion"),
                    fecha(l, "FechaLiquidacionOperacion"), fecha(l, "FechaOperacion"), txt(l, "TipoCambioOperacion")));
        }
        return out;
    }

    private static List<String> tesoreria(CasoCerT3.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonObject l : llamadas(c, "ProcesarIngresoEgresoTesoreria")) {
            for (JsonElement e : arg(l).getAsJsonArray("p_Solicitudes")) {
                JsonObject s = e.getAsJsonObject();
                out.add(une(txt(s, "IdInterno"), txt(s, "MontoPagoLocal"), txt(s, "RelacionTraspaso"), txt(s, "IdMedioDePago"), txt(s, "IdPersonaTraspaso"), txt(s, "IdCuentaBancaria"), txt(s, "IdBanco"),
                        txt(s, "NumeroCuentaBancaria"), txt(s, "IdConcepto"), fecha(s, "FechaEmision"), txt(s, "ParidadFechaInversion"), txt(s, "IdAutorizadoOrigen")));
            }
        }
        return out;
    }

    private static List<String> spotOperaciones(CasoCerT3.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonObject l : llamadas(c, "ProcesarIngresoOperacionSpot")) {
            for (JsonElement e : arg(l).getAsJsonArray("p_DatosOperacion")) {
                JsonObject s = e.getAsJsonObject();
                out.add(une(txt(s, "IdInterno"), txt(s, "Monto"), txt(s, "IdMedioDePago"), txt(s, "GeneraSpot")));
            }
        }
        return out;
    }

    private static List<String> spotFormas(CasoCerT3.Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonObject l : llamadas(c, "ProcesarIngresoOperacionSpot")) {
            for (JsonElement e : arg(l).getAsJsonArray("p_DatosFormaPago")) {
                JsonObject f = e.getAsJsonObject();
                out.add(une(txt(f, "IdOperacion"), txt(f, "RecibePagaPortafolio"), txt(f, "IdTipoMovimiento"), txt(f, "IdFormaDePago"), txt(f, "IdMoneda"), txt(f, "MontoFormaDePago"), txt(f, "AfectarTesoreria"),
                        fecha(f, "FechaLiquidacion")));
            }
        }
        return out;
    }

    /** The order of the calls to the collaborators that carry the legacy order of the flow (cash flow, custody, cuenta de inversion, Tesoreria, Spot). */
    private static List<String> orden(CasoCerT3.Corrida c) {
        java.util.Set<String> ordenados = java.util.Set.of("SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria",
                "ProcesarIngresoOperacionSpot");
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonArray("colaboradores")) {
            String n = e.getAsJsonObject().get("colaborador").getAsString();
            if (ordenados.contains(n)) out.add(n);
        }
        return out;
    }

    private static String cabecera(CasoCerT3.Corrida c, String colaborador, int k) {
        JsonObject a = arg(llamadas(c, colaborador).get(k));
        String[] claves = switch (colaborador) {
            case "CrearCargoAbono" -> new String[] { "p_simularOperacion", "p_UsaReglasNegocio", "p_EsIngresoManual", "p_FechaSistema", "p_IdEmpresa", "p_IdUsuario", "p_IdModulo" };
            case "ProcesarIngresoEgresoTesoreria" -> new String[] { "p_SimularOperacion", "p_UsaReglasNegocio", "p_EsIngresoAutomatico", "p_IdEmpresa", "p_IdUsuario", "p_IdModulo" };
            default -> new String[] { "p_CalculaMargen", "p_UsaReglasNegocio", "p_PermiteOperarProspectos", "p_IdEmpresa", "p_IdUsuario", "p_IdModulo" };
        };
        List<String> partes = new ArrayList<>();
        for (String k2 : claves) partes.add(txt(a, k2));
        return String.join("|", partes);
    }

    /** {@code id=OK} when the solicitud was still pending after F-C..F-H (the legacy certifies it), else {@code id=RZD:code} / {@code id=ADV:code}. */
    private static String estados(CasoCerT3.Corrida c) {
        List<String> out = new ArrayList<>();
        for (SolicitudProceso s : c.contexto.solicitudes) {
            boolean marcada = "RZD".equals(s.EstadoProceso) || "ADV".equals(s.EstadoProceso);
            out.add(s.IdSolicitudAporteRescate + "=" + (marcada ? s.EstadoProceso + ":" + s.Codigo : "OK"));
        }
        return String.join(";", out);
    }

    /** {@code id=IdIngresoEgreso/IdOperacionSpot} of every solicitud the UPDATE will write (not the rejected ones). */
    private static String filas(CasoCerT3.Corrida c) {
        List<String> out = new ArrayList<>();
        for (SolicitudProceso s : c.contexto.solicitudes) {
            if ("RZD".equals(s.EstadoProceso)) continue;
            out.add(s.IdSolicitudAporteRescate + "=" + s.IdIngresoEgreso + "/" + s.IdOperacionSpot);
        }
        return String.join(";", out);
    }

    private static String salida(CasoCerT3.Corrida c) {
        List<String> ms = new ArrayList<>();
        c.contexto.salida.P_OUT_MENSAJES.forEach(m -> ms.add(m.id + ":" + m.tipo + ":" + m.codigo + ":" + m.descripcion));
        return "contador=" + c.contexto.salida.P_OUT_ERRORESCONTADOR + ";" + String.join("|", ms);
    }


    @Test
    @DisplayName("FM-CER-29 an aporte that moves the cuenta (MueveCta S, origin M, GeneraCargoAbono S) sends ONE cargo MOVCAR in the fund currency for MontoPago (250.000) with the concept APORFM; the call carries simular N, the system date and the manual-income flag N")
    void fmCer29_aporteNormal() throws Exception {
        // FMM-CER-378
        CasoCerT3.Corrida c378 = corre("aporte-normal-un-cargo-en-la-moneda-del-fondo");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c378, 0), "aporte-normal-un-cargo-en-la-moneda-del-fondo :: lines of the call 0 to CrearCargoAbono");
        assertEquals("N|N|N|2026-03-02|1|7|FMM", cabecera(c378, "CrearCargoAbono", 0), "aporte-normal-un-cargo-en-la-moneda-del-fondo :: head of the call to CrearCargoAbono");
        assertEquals("3001=OK", estados(c378), "aporte-normal-un-cargo-en-la-moneda-del-fondo :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3001=0/0", filas(c378), "aporte-normal-un-cargo-en-la-moneda-del-fondo :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
    }

    @Test
    @DisplayName("FM-CER-29 an aporte without MueveCta, of a non manual origin, of a type whose GeneraCargoAbono is N or of a type without row does NOT reach the cuenta de inversion (no call)")
    void fmCer29_noMueveLaCuenta() throws Exception {
        // FMM-CER-379
        CasoCerT3.Corrida c379 = corre("aporte-sin-mueve-cuenta-no-llama-a-la-cuenta");
        assertEquals(0, llamadas(c379, "CrearCargoAbono").size(), "aporte-sin-mueve-cuenta-no-llama-a-la-cuenta :: calls to CrearCargoAbono");
        assertEquals("3001=OK", estados(c379), "aporte-sin-mueve-cuenta-no-llama-a-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-380
        CasoCerT3.Corrida c380 = corre("aporte-de-origen-no-manual-no-llama-a-la-cuenta");
        assertEquals(0, llamadas(c380, "CrearCargoAbono").size(), "aporte-de-origen-no-manual-no-llama-a-la-cuenta :: calls to CrearCargoAbono");
        assertEquals("3001=OK", estados(c380), "aporte-de-origen-no-manual-no-llama-a-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-381
        CasoCerT3.Corrida c381 = corre("aporte-con-tipo-que-no-genera-cargo-abono-no-llama");
        assertEquals(0, llamadas(c381, "CrearCargoAbono").size(), "aporte-con-tipo-que-no-genera-cargo-abono-no-llama :: calls to CrearCargoAbono");
        assertEquals("3001=OK", estados(c381), "aporte-con-tipo-que-no-genera-cargo-abono-no-llama :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-382
        CasoCerT3.Corrida c382 = corre("aporte-con-tipo-sin-fila-se-trata-como-N");
        assertEquals(0, llamadas(c382, "CrearCargoAbono").size(), "aporte-con-tipo-sin-fila-se-trata-como-N :: calls to CrearCargoAbono");
        assertEquals("3001=OK", estados(c382), "aporte-con-tipo-sin-fila-se-trata-como-N :: state of every solicitud (OK = pending, certified by the end)");
    }

    @Test
    @DisplayName("FM-CER-29 a deferred aporte (paid in USD, fund in CLP) sends THREE lines: cargo USD, abono CLP and the principal cargo CLP LAST, numbered 1, 2, 0")
    void fmCer29_diferidoUsd() throws Exception {
        // FMM-CER-383
        CasoCerT3.Corrida c383 = corre("aporte-diferido-usd-en-fondo-clp-tres-lineas-con-el-principal-al-final");
        assertEquals(List.of("1|MOVCAR|USD|2000|2000|APORFM|2026-03-03|2026-03-03|2026-03-02|900", "2|MOVABN|CLP|1800000|1800000|APORFM|2026-03-03|2026-03-03|2026-03-02|900", "0|MOVCAR|CLP|1800000|1800000|APORFM|2026-03-03|2026-03-03|2026-03-02|900"), cuenta(c383, 0), "aporte-diferido-usd-en-fondo-clp-tres-lineas-con-el-principal-al-final :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=OK", estados(c383), "aporte-diferido-usd-en-fondo-clp-tres-lineas-con-el-principal-al-final :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-398
        CasoCerT3.Corrida c398 = corre("aporte-de-reinversion-diferido-hereda-el-concepto-en-las-tres-lineas");
        assertEquals(List.of("1|MOVCAR|USD|2000|2000|APOREI|2026-03-03|2026-03-03|2026-03-02|900", "2|MOVABN|CLP|1800000|1800000|APOREI|2026-03-03|2026-03-03|2026-03-02|900", "0|MOVCAR|CLP|1800000|1800000|APOREI|2026-03-03|2026-03-03|2026-03-02|900"), cuenta(c398, 0), "aporte-de-reinversion-diferido-hereda-el-concepto-en-las-tres-lineas :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-29 a deferred aporte paid in CLP into a USD fund sends cargo CLP, abono USD and the principal cargo USD last; the fund amounts keep 2 decimals")
    void fmCer29_diferidoClpEnFondoUsd() throws Exception {
        // FMM-CER-384
        CasoCerT3.Corrida c384 = corre("aporte-diferido-clp-en-fondo-usd-tres-lineas");
        assertEquals(List.of("1|MOVCAR|CLP|5000000|5000000|APORFM|2026-03-03|2026-03-03|2026-03-02|910", "2|MOVABN|USD|5494.51|5494.51|APORFM|2026-03-03|2026-03-03|2026-03-02|910", "0|MOVCAR|USD|5494.51|5494.51|APORFM|2026-03-03|2026-03-03|2026-03-02|910"), cuenta(c384, 0), "aporte-diferido-clp-en-fondo-usd-tres-lineas :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=OK", estados(c384), "aporte-diferido-clp-en-fondo-usd-tres-lineas :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-406
        CasoCerT3.Corrida c406 = corre("aporte-con-decimales-exactos-en-el-pago-de-un-fondo-usd");
        assertEquals(List.of("1|MOVCAR|CLP|5000000|5000000|APORFM|2026-03-03|2026-03-03|2026-03-02|910", "2|MOVABN|USD|5494.51|5494.51|APORFM|2026-03-03|2026-03-03|2026-03-02|910", "0|MOVCAR|USD|5494.51|5494.51|APORFM|2026-03-03|2026-03-03|2026-03-02|910"), cuenta(c406, 0), "aporte-con-decimales-exactos-en-el-pago-de-un-fondo-usd :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-29 FechaCondicionDeLiquidacion = the system date when it is later than FechaLiquidacion OR the aporte is a switch (S / s), else FechaLiquidacion; FechaLiquidacionOperacion ignores the switch; FechaOperacion = FechaCertificacion")
    void fmCer29_fechas() throws Exception {
        // FMM-CER-385
        CasoCerT3.Corrida c385 = corre("aporte-switch-S-toma-la-fecha-de-sistema-aunque-la-liquidacion-sea-posterior");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-02|2026-03-05|2026-03-02|1"), cuenta(c385, 0), "aporte-switch-S-toma-la-fecha-de-sistema-aunque-la-liquidacion-sea-posterior :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-386
        CasoCerT3.Corrida c386 = corre("aporte-switch-minuscula-s-tambien-por-equalsignorecase");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-02|2026-03-05|2026-03-02|1"), cuenta(c386, 0), "aporte-switch-minuscula-s-tambien-por-equalsignorecase :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-387
        CasoCerT3.Corrida c387 = corre("aporte-con-liquidacion-posterior-al-sistema-usa-su-fecha");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-05|2026-03-05|2026-03-02|1"), cuenta(c387, 0), "aporte-con-liquidacion-posterior-al-sistema-usa-su-fecha :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-388
        CasoCerT3.Corrida c388 = corre("aporte-con-liquidacion-igual-al-sistema-usa-su-fecha");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-02|2026-03-02|2026-03-02|1"), cuenta(c388, 0), "aporte-con-liquidacion-igual-al-sistema-usa-su-fecha :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-389
        CasoCerT3.Corrida c389 = corre("aporte-con-liquidacion-anterior-al-sistema-usa-la-fecha-de-sistema");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-02|2026-03-02|2026-03-02|1"), cuenta(c389, 0), "aporte-con-liquidacion-anterior-al-sistema-usa-la-fecha-de-sistema :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-390
        CasoCerT3.Corrida c390 = corre("aporte-con-liquidacion-anterior-y-switch-S");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-02|2026-03-02|2026-03-02|1"), cuenta(c390, 0), "aporte-con-liquidacion-anterior-y-switch-S :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-391
        CasoCerT3.Corrida c391 = corre("aporte-fecha-de-certificacion-posterior-fija-fecha-de-operacion-y-comprobante");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-03|2026-03-03|2026-03-05|1"), cuenta(c391, 0), "aporte-fecha-de-certificacion-posterior-fija-fecha-de-operacion-y-comprobante :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-29 the concept of the lines is the one SelectSolicitudes computes from the movement type: APODIV, APOOVE, APOPRO, APOTRS, APOCFI, APOREI (reinversion), APORFM otherwise")
    void fmCer29_conceptos() throws Exception {
        // FMM-CER-392
        CasoCerT3.Corrida c392 = corre("aporte-apodiv-usa-el-concepto-apodiv");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APODIV|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c392, 0), "aporte-apodiv-usa-el-concepto-apodiv :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-393
        CasoCerT3.Corrida c393 = corre("aporte-apoover-usa-el-concepto-apoove");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APOOVE|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c393, 0), "aporte-apoover-usa-el-concepto-apoove :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-394
        CasoCerT3.Corrida c394 = corre("aporte-aporespro-usa-el-concepto-apopro");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APOPRO|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c394, 0), "aporte-aporespro-usa-el-concepto-apopro :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-395
        CasoCerT3.Corrida c395 = corre("aporte-apotrasp-usa-el-concepto-apotrs");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APOTRS|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c395, 0), "aporte-apotrasp-usa-el-concepto-apotrs :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-396
        CasoCerT3.Corrida c396 = corre("aporte-apodivcfi-usa-el-concepto-apocfi");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APOCFI|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c396, 0), "aporte-apodivcfi-usa-el-concepto-apocfi :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-397
        CasoCerT3.Corrida c397 = corre("aporte-de-reinversion-usa-el-concepto-aporei");
        assertEquals(List.of("0|MOVCAR|CLP|1000000|1000000|APOREI|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c397, 0), "aporte-de-reinversion-usa-el-concepto-aporei :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-29 the lines of a lot are numbered 0, 1, 2 ... in the order of the aportes (ascending id even when the request is reversed); an aporte that does not move the cuenta does not consume a number")
    void fmCer29_lote() throws Exception {
        // FMM-CER-399
        CasoCerT3.Corrida c399 = corre("lote-de-aportes-numera-las-lineas-correlativamente-normal-diferido-normal");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-03|2026-03-03|2026-03-02|1", "2|MOVCAR|USD|2000|2000|APORFM|2026-03-03|2026-03-03|2026-03-02|900", "3|MOVABN|CLP|1800000|1800000|APORFM|2026-03-03|2026-03-03|2026-03-02|900", "1|MOVCAR|CLP|1800000|1800000|APORFM|2026-03-03|2026-03-03|2026-03-02|900", "4|MOVCAR|CLP|100000|100000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c399, 0), "lote-de-aportes-numera-las-lineas-correlativamente-normal-diferido-normal :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=OK;3002=OK;3003=OK", estados(c399), "lote-de-aportes-numera-las-lineas-correlativamente-normal-diferido-normal :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-400
        CasoCerT3.Corrida c400 = corre("lote-de-aportes-uno-sin-mueve-cuenta-no-consume-numero-de-linea");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-03|2026-03-03|2026-03-02|1", "1|MOVCAR|CLP|100000|100000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c400, 0), "lote-de-aportes-uno-sin-mueve-cuenta-no-consume-numero-de-linea :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-407
        CasoCerT3.Corrida c407 = corre("aporte-con-ejecutivo-y-portafolio-propios-en-cada-linea");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c407, 0), "aporte-con-ejecutivo-y-portafolio-propios-en-cada-linea :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-29 an aporte already rejected (by saldo, by custody, by F-A) is NOT sent to the cuenta; the others are")
    void fmCer29_rechazadosNoSeEnvian() throws Exception {
        // FMM-CER-401
        CasoCerT3.Corrida c401 = corre("aporte-rechazado-por-saldo-no-se-envia-a-la-cuenta");
        assertEquals(List.of("0|MOVCAR|CLP|100000|100000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c401, 0), "aporte-rechazado-por-saldo-no-se-envia-a-la-cuenta :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=RZD:UI_SALDO_INSUFICIENTE_FLUJO_CAJA;3002=OK", estados(c401), "aporte-rechazado-por-saldo-no-se-envia-a-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-402
        CasoCerT3.Corrida c402 = corre("aporte-rechazado-por-custodia-no-se-envia-a-la-cuenta");
        assertEquals(List.of("0|MOVCAR|CLP|100000|100000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c402, 0), "aporte-rechazado-por-custodia-no-se-envia-a-la-cuenta :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=RZD:UI_ERROR_CUSTODIA_APORTE;3002=OK", estados(c402), "aporte-rechazado-por-custodia-no-se-envia-a-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;3001:ERROR:UI_ERROR_CUSTODIA_APORTE:Error de custodia en el aporte", salida(c402), "aporte-rechazado-por-custodia-no-se-envia-a-la-cuenta :: counter and messages of the output");
        // FMM-CER-403
        CasoCerT3.Corrida c403 = corre("todos-los-aportes-rechazados-por-F-A-no-llaman-a-la-cuenta");
        assertEquals(1, llamadas(c403, "CrearCargoAbono").size(), "todos-los-aportes-rechazados-por-F-A-no-llaman-a-la-cuenta :: calls to CrearCargoAbono");
        assertEquals("3001=RZD:UI_ESTADO_INCORRECTO_CERTIFICACION;3002=OK", estados(c403), "todos-los-aportes-rechazados-por-F-A-no-llaman-a-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
    }

    @Test
    @DisplayName("FM-CER-29 / D-F3B-01 in simulation S the cuenta de inversion is still called, with simular N and the UsaReglasNegocio of the request (S)")
    void fmCer29_simulacion() throws Exception {
        // FMM-CER-404
        CasoCerT3.Corrida c404 = corre("simulacion-S-la-cuenta-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada");
        assertEquals("N|S|N|2026-03-02|1|7|FMM", cabecera(c404, "CrearCargoAbono", 0), "simulacion-S-la-cuenta-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada :: head of the call to CrearCargoAbono");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c404, 0), "simulacion-S-la-cuenta-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-29 / FM-CER-42 / FM-CER-44 / FM-CER-59 the order of the legacy: cash flow, custody calculation, custody entry of the aportes, CUENTA of the aportes, custody retirement of the rescates, CUENTA of the rescates, Tesoreria, Spot")
    void fmCer29_ordenDentroDelFlujo() throws Exception {
        // FMM-CER-405
        CasoCerT3.Corrida c405 = corre("aportes-y-rescates-hacen-dos-llamadas-y-la-numeracion-reinicia");
        assertEquals(List.of("SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono"), orden(c405), "aportes-y-rescates-hacen-dos-llamadas-y-la-numeracion-reinicia :: order of the calls to the collaborators of the flow");
        // FMM-CER-465
        CasoCerT3.Corrida c465 = corre("los-aportes-no-pasan-por-tesoreria");
        assertEquals(List.of("SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria"), orden(c465), "los-aportes-no-pasan-por-tesoreria :: order of the calls to the collaborators of the flow");
        // FMM-CER-540
        CasoCerT3.Corrida c540 = corre("spot-junto-a-tesoreria-cada-rescate-va-a-su-colaborador");
        assertEquals(List.of("CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria", "ProcesarIngresoOperacionSpot"), orden(c540), "spot-junto-a-tesoreria-cada-rescate-va-a-su-colaborador :: order of the calls to the collaborators of the flow");
        // FMM-CER-443
        CasoCerT3.Corrida c443 = corre("rescate-con-retencion-apv-mueve-la-cuenta-con-el-pago-completo");
        assertEquals(List.of("CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria"), orden(c443), "rescate-con-retencion-apv-mueve-la-cuenta-con-el-pago-completo :: order of the calls to the collaborators of the flow");
    }

    @Test
    @DisplayName("FM-CER-29 / FM-CER-42 aportes and rescates make TWO calls (the aportes first), each numbering its lines from 0")
    void fmCer29_dosLlamadas() throws Exception {
        // FMM-CER-405
        CasoCerT3.Corrida c405 = corre("aportes-y-rescates-hacen-dos-llamadas-y-la-numeracion-reinicia");
        assertEquals(2, llamadas(c405, "CrearCargoAbono").size(), "aportes-y-rescates-hacen-dos-llamadas-y-la-numeracion-reinicia :: calls to CrearCargoAbono");
        assertEquals(List.of("0|MOVCAR|CLP|250000|250000|APORFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c405, 0), "aportes-y-rescates-hacen-dos-llamadas-y-la-numeracion-reinicia :: lines of the call 0 to CrearCargoAbono");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c405, 1), "aportes-y-rescates-hacen-dos-llamadas-y-la-numeracion-reinicia :: lines of the call 1 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-30 an ERROR of the cuenta rejects the aporte whose line failed (also a secondary line of a deferred one), adds the message with the id of the solicitud and sums the counter; two errors reject two aportes; a message that is not exactly ERROR is ignored; the counter of the collaborator is added even without messages")
    void fmCer30_errores() throws Exception {
        // FMM-CER-408
        CasoCerT3.Corrida c408 = corre("aporte-error-de-la-cuenta-rechaza-al-aporte-con-el-mensaje");
        assertEquals("3001=OK;3002=RZD:UI_ERROR_CUENTA_INVERSION", estados(c408), "aporte-error-de-la-cuenta-rechaza-al-aporte-con-el-mensaje :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;3002:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion", salida(c408), "aporte-error-de-la-cuenta-rechaza-al-aporte-con-el-mensaje :: counter and messages of the output");
        // FMM-CER-409
        CasoCerT3.Corrida c409 = corre("aporte-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-aporte");
        assertEquals("3001=RZD:UI_SALDO_CUENTA_INSUFICIENTE;3002=OK", estados(c409), "aporte-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-aporte :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;3001:ERROR:UI_SALDO_CUENTA_INSUFICIENTE:Saldo insuficiente en la cuenta de inversion", salida(c409), "aporte-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-aporte :: counter and messages of the output");
        // FMM-CER-410
        CasoCerT3.Corrida c410 = corre("aporte-errores-en-dos-aportes-rechazan-a-ambos-y-suman-el-contador");
        assertEquals("3001=RZD:UI_ERROR_CUENTA_INVERSION;3002=OK;3003=RZD:UI_SALDO_CUENTA_INSUFICIENTE", estados(c410), "aporte-errores-en-dos-aportes-rechazan-a-ambos-y-suman-el-contador :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=2;3001:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion|3003:ERROR:UI_SALDO_CUENTA_INSUFICIENTE:Saldo insuficiente en la cuenta de inversion", salida(c410), "aporte-errores-en-dos-aportes-rechazan-a-ambos-y-suman-el-contador :: counter and messages of the output");
        // FMM-CER-411
        CasoCerT3.Corrida c411 = corre("aporte-dos-errores-del-mismo-aporte-agregan-los-dos-mensajes");
        assertEquals("3001=RZD:UI_SALDO_CUENTA_INSUFICIENTE", estados(c411), "aporte-dos-errores-del-mismo-aporte-agregan-los-dos-mensajes :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=2;3001:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion|3001:ERROR:UI_SALDO_CUENTA_INSUFICIENTE:Saldo insuficiente en la cuenta de inversion", salida(c411), "aporte-dos-errores-del-mismo-aporte-agregan-los-dos-mensajes :: counter and messages of the output");
        // FMM-CER-412
        CasoCerT3.Corrida c412 = corre("aporte-mensaje-que-no-es-ERROR-no-rechaza");
        assertEquals("3001=OK", estados(c412), "aporte-mensaje-que-no-es-ERROR-no-rechaza :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=0;", salida(c412), "aporte-mensaje-que-no-es-ERROR-no-rechaza :: counter and messages of the output");
        // FMM-CER-413
        CasoCerT3.Corrida c413 = corre("aporte-mensaje-ERROR-en-minuscula-no-rechaza-por-equals");
        assertEquals("3001=OK", estados(c413), "aporte-mensaje-ERROR-en-minuscula-no-rechaza-por-equals :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=0;", salida(c413), "aporte-mensaje-ERROR-en-minuscula-no-rechaza-por-equals :: counter and messages of the output");
        // FMM-CER-414
        CasoCerT3.Corrida c414 = corre("aporte-el-contador-de-la-cuenta-se-suma-aunque-no-haya-mensajes");
        assertEquals("3001=OK", estados(c414), "aporte-el-contador-de-la-cuenta-se-suma-aunque-no-haya-mensajes :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=3;", salida(c414), "aporte-el-contador-de-la-cuenta-se-suma-aunque-no-haya-mensajes :: counter and messages of the output");
    }

    @Test
    @DisplayName("FM-CER-30 / D-NOR-9 an error whose line number is not in the map (or has no id) is a NullPointerException that aborts the lot after the call")
    void fmCer30_npe() throws Exception {
        // FMM-CER-415
        CasoCerT3.Corrida c415 = lanza("aporte-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote");
        assertEquals("NullPointerException", c415.excepcion.getClass().getSimpleName(), "aporte-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote");
        assertEquals(1, llamadas(c415, "CrearCargoAbono").size(), "aporte-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote :: calls to CrearCargoAbono");
        // FMM-CER-416
        CasoCerT3.Corrida c416 = lanza("aporte-error-sin-id-lanza-NPE");
        assertEquals("NullPointerException", c416.excepcion.getClass().getSimpleName(), "aporte-error-sin-id-lanza-NPE");
        assertEquals(1, llamadas(c416, "CrearCargoAbono").size(), "aporte-error-sin-id-lanza-NPE :: calls to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-30 an exception of CrearCargoAbono comes out untouched")
    void fmCer30_fallo() throws Exception {
        // FMM-CER-417
        CasoCerT3.Corrida c417 = lanza("aporte-la-cuenta-falla-y-propaga-la-excepcion");
        assertEquals("Exception: cuenta de inversion no disponible", CasoCer.texto(c417.excepcion), "aporte-la-cuenta-falla-y-propaga-la-excepcion");
        assertEquals(1, llamadas(c417, "CrearCargoAbono").size(), "aporte-la-cuenta-falla-y-propaga-la-excepcion :: calls to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-30 / D-F3B-03 an aporte rejected by the cuenta stays rejected but the custody was already entered (the host commits when the counter is 0 in the rest)")
    void fmCer30_efectosPrevios() throws Exception {
        // FMM-CER-418
        CasoCerT3.Corrida c418 = corre("aporte-rechazado-por-la-cuenta-deja-la-custodia-ya-ingresada");
        assertEquals("3001=OK;3002=RZD:UI_ERROR_CUENTA_INVERSION", estados(c418), "aporte-rechazado-por-la-cuenta-deja-la-custodia-ya-ingresada :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;3002:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion", salida(c418), "aporte-rechazado-por-la-cuenta-deja-la-custodia-ya-ingresada :: counter and messages of the output");
        assertEquals(1, llamadas(c418, "ProcesarCustodiaFondoMutuo").size(), "aporte-rechazado-por-la-cuenta-deja-la-custodia-ya-ingresada :: calls to ProcesarCustodiaFondoMutuo");
    }

    @Test
    @DisplayName("FM-CER-42 a rescate sends ONE abono MOVABN in the fund currency (Monto 1.000.000, MontoOperacion = MontoPago) with the concept RESCFM and the operation date = FechaAceptacion")
    void fmCer42_normal() throws Exception {
        // FMM-CER-419
        CasoCerT3.Corrida c419 = corre("rescate-normal-un-abono-en-la-moneda-del-fondo");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c419, 0), "rescate-normal-un-abono-en-la-moneda-del-fondo :: lines of the call 0 to CrearCargoAbono");
        assertEquals("N|N|N|2026-03-02|1|7|FMM", cabecera(c419, "CrearCargoAbono", 0), "rescate-normal-un-abono-en-la-moneda-del-fondo :: head of the call to CrearCargoAbono");
        assertEquals("3001=OK", estados(c419), "rescate-normal-un-abono-en-la-moneda-del-fondo :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3001=0/0", filas(c419), "rescate-normal-un-abono-en-la-moneda-del-fondo :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
    }

    @Test
    @DisplayName("FM-CER-42 / D-F3B-17 a rescate does NOT need MueveCta S nor a manual origin (an aporte does), only a movement type with GeneraCargoAbono S; without it, nothing is sent")
    void fmCer42_noExigeMueveCuenta() throws Exception {
        // FMM-CER-420
        CasoCerT3.Corrida c420 = corre("rescate-no-exige-mueve-cuenta-ni-origen-manual");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c420, 0), "rescate-no-exige-mueve-cuenta-ni-origen-manual :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-421
        CasoCerT3.Corrida c421 = corre("rescate-con-tipo-que-no-genera-cargo-abono-no-llama");
        assertEquals(0, llamadas(c421, "CrearCargoAbono").size(), "rescate-con-tipo-que-no-genera-cargo-abono-no-llama :: calls to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-42 a deferred rescate sends the principal abono FIRST, then the cargo in the fund currency and the abono in the investment currency (3 lines)")
    void fmCer42_diferido() throws Exception {
        // FMM-CER-422
        CasoCerT3.Corrida c422 = corre("rescate-diferido-usd-en-fondo-clp-abono-cargo-abono");
        assertEquals(List.of("0|MOVABN|CLP|1780000|1780000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890", "1|MOVCAR|CLP|1780000|1780000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890", "2|MOVABN|USD|2000|2000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890"), cuenta(c422, 0), "rescate-diferido-usd-en-fondo-clp-abono-cargo-abono :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-423
        CasoCerT3.Corrida c423 = corre("rescate-diferido-clp-en-fondo-usd");
        assertEquals(List.of("0|MOVABN|USD|5524.86|5524.86|RESCFM|2026-03-03|2026-03-03|2026-03-02|905", "1|MOVCAR|USD|5524.86|5524.86|RESCFM|2026-03-03|2026-03-03|2026-03-02|905", "2|MOVABN|CLP|5000000|5000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|905"), cuenta(c423, 0), "rescate-diferido-clp-en-fondo-usd :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-42 only RESREINV and RESCOM with MontoComisionLocal > 0 add the cargo of the commission in the investment currency (4 lines when also deferred); RESREINV without commission and RESNORM with commission do not")
    void fmCer42_comision() throws Exception {
        // FMM-CER-424
        CasoCerT3.Corrida c424 = corre("rescate-de-reinversion-diferido-con-comision-local-cuatro-lineas");
        assertEquals(List.of("0|MOVABN|CLP|1780000|1780000|RESREI|2026-03-03|2026-03-03|2026-03-02|890", "1|MOVCAR|CLP|1780000|1780000|RESREI|2026-03-03|2026-03-03|2026-03-02|890", "2|MOVABN|USD|2000|2000|RESREI|2026-03-03|2026-03-03|2026-03-02|890", "3|MOVCAR|USD|5000|5000|RESREI|2026-03-03|2026-03-03|2026-03-02|890"), cuenta(c424, 0), "rescate-de-reinversion-diferido-con-comision-local-cuatro-lineas :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-425
        CasoCerT3.Corrida c425 = corre("rescate-de-reinversion-con-comision-local-agrega-el-cargo-de-comision");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESREI|2026-03-03|2026-03-03|2026-03-02|1", "1|MOVCAR|CLP|5000|5000|RESREI|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c425, 0), "rescate-de-reinversion-con-comision-local-agrega-el-cargo-de-comision :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-426
        CasoCerT3.Corrida c426 = corre("rescate-de-reinversion-sin-comision-no-agrega-el-cargo");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESREI|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c426, 0), "rescate-de-reinversion-sin-comision-no-agrega-el-cargo :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-427
        CasoCerT3.Corrida c427 = corre("rescate-RESCOM-con-comision-local-agrega-el-cargo-y-usa-el-concepto-rescom");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCOM|2026-03-03|2026-03-03|2026-03-02|1", "1|MOVCAR|CLP|5000|5000|RESCOM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c427, 0), "rescate-RESCOM-con-comision-local-agrega-el-cargo-y-usa-el-concepto-rescom :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-428
        CasoCerT3.Corrida c428 = corre("rescate-normal-con-comision-local-no-agrega-cargo-de-comision");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c428, 0), "rescate-normal-con-comision-local-no-agrega-cargo-de-comision :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-42 the concept is RESREI for a reinversion, RESTRS for a traspaso and RESCFM for the others (a RESCNN too)")
    void fmCer42_conceptos() throws Exception {
        // FMM-CER-429
        CasoCerT3.Corrida c429 = corre("rescate-de-reinversion-usa-el-concepto-resrei");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESREI|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c429, 0), "rescate-de-reinversion-usa-el-concepto-resrei :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-430
        CasoCerT3.Corrida c430 = corre("rescate-de-traspaso-usa-el-concepto-restrs");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESTRS|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c430, 0), "rescate-de-traspaso-usa-el-concepto-restrs :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-431
        CasoCerT3.Corrida c431 = corre("rescate-nn-usa-el-concepto-rescfm");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c431, 0), "rescate-nn-usa-el-concepto-rescfm :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-42 same date rules as the aportes: switch or a system date later than the liquidation date moves FechaCondicionDeLiquidacion; FechaOperacion = FechaAceptacion")
    void fmCer42_fechas() throws Exception {
        // FMM-CER-432
        CasoCerT3.Corrida c432 = corre("rescate-switch-S-toma-la-fecha-de-sistema-aunque-la-liquidacion-sea-posterior");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-02|2026-03-05|2026-03-02|1"), cuenta(c432, 0), "rescate-switch-S-toma-la-fecha-de-sistema-aunque-la-liquidacion-sea-posterior :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-433
        CasoCerT3.Corrida c433 = corre("rescate-con-liquidacion-posterior-al-sistema-usa-su-fecha");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-05|2026-03-05|2026-03-02|1"), cuenta(c433, 0), "rescate-con-liquidacion-posterior-al-sistema-usa-su-fecha :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-434
        CasoCerT3.Corrida c434 = corre("rescate-con-liquidacion-igual-al-sistema-usa-su-fecha");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-02|2026-03-02|2026-03-02|1"), cuenta(c434, 0), "rescate-con-liquidacion-igual-al-sistema-usa-su-fecha :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-435
        CasoCerT3.Corrida c435 = corre("rescate-con-liquidacion-anterior-al-sistema-usa-la-fecha-de-sistema");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-02|2026-03-02|2026-03-02|1"), cuenta(c435, 0), "rescate-con-liquidacion-anterior-al-sistema-usa-la-fecha-de-sistema :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-436
        CasoCerT3.Corrida c436 = corre("rescate-la-fecha-de-operacion-es-la-de-aceptacion");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-02-20|1"), cuenta(c436, 0), "rescate-la-fecha-de-operacion-es-la-de-aceptacion :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-42 a rescate rejected by custody, without cartera, or advised (amounts of custody) is sent / not sent accordingly: RZD and the one without cartera are rejected by F-D, the ADV one goes with the adjusted amounts")
    void fmCer42_rechazadosNoSeEnvian() throws Exception {
        // FMM-CER-437
        CasoCerT3.Corrida c437 = corre("rescate-rechazado-por-custodia-no-se-envia-a-la-cuenta");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c437, 0), "rescate-rechazado-por-custodia-no-se-envia-a-la-cuenta :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION;3002=OK", estados(c437), "rescate-rechazado-por-custodia-no-se-envia-a-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-438
        CasoCerT3.Corrida c438 = corre("rescate-sin-cartera-retirada-no-se-envia-a-la-cuenta");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c438, 0), "rescate-sin-cartera-retirada-no-se-envia-a-la-cuenta :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO;3002=OK", estados(c438), "rescate-sin-cartera-retirada-no-se-envia-a-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-439
        CasoCerT3.Corrida c439 = corre("rescate-advertido-por-custodia-usa-los-montos-ajustados-en-la-cuenta");
        assertEquals(List.of("0|MOVABN|CLP|150004|150004|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c439, 0), "rescate-advertido-por-custodia-usa-los-montos-ajustados-en-la-cuenta :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=ADV:UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION", estados(c439), "rescate-advertido-por-custodia-usa-los-montos-ajustados-en-la-cuenta :: state of every solicitud (OK = pending, certified by the end)");
    }

    @Test
    @DisplayName("FM-CER-42 the lines of a lot of rescates are numbered in order, a rescate whose type does not generate cargo / abono does not consume a number")
    void fmCer42_lote() throws Exception {
        // FMM-CER-440
        CasoCerT3.Corrida c440 = corre("lote-de-rescates-numera-las-lineas-correlativamente-normal-diferido-comision");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1", "1|MOVABN|CLP|1780000|1780000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890", "2|MOVCAR|CLP|1780000|1780000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890", "3|MOVABN|USD|2000|2000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890", "4|MOVABN|CLP|1000000|1000000|RESREI|2026-03-03|2026-03-03|2026-03-02|1", "5|MOVCAR|CLP|7000|7000|RESREI|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c440, 0), "lote-de-rescates-numera-las-lineas-correlativamente-normal-diferido-comision :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-441
        CasoCerT3.Corrida c441 = corre("lote-de-rescates-uno-sin-genera-cargo-abono-no-consume-numero-de-linea");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1", "1|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c441, 0), "lote-de-rescates-uno-sin-genera-cargo-abono-no-consume-numero-de-linea :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-42 a rescate with Spot, with APV retention (the retention does not reduce the cuenta amounts) or with calculated commission still moves the cuenta, with the amounts that F-D left")
    void fmCer42_conOtrosFrentes() throws Exception {
        // FMM-CER-442
        CasoCerT3.Corrida c442 = corre("rescate-con-spot-tambien-mueve-la-cuenta-de-inversion");
        assertEquals(List.of("0|MOVABN|CLP|1780000|1780000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890", "1|MOVCAR|CLP|1780000|1780000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890", "2|MOVABN|USD|2000|2000|RESCFM|2026-03-03|2026-03-03|2026-03-02|890"), cuenta(c442, 0), "rescate-con-spot-tambien-mueve-la-cuenta-de-inversion :: lines of the call 0 to CrearCargoAbono");
        assertEquals("3001=OK", estados(c442), "rescate-con-spot-tambien-mueve-la-cuenta-de-inversion :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-443
        CasoCerT3.Corrida c443 = corre("rescate-con-retencion-apv-mueve-la-cuenta-con-el-pago-completo");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c443, 0), "rescate-con-retencion-apv-mueve-la-cuenta-con-el-pago-completo :: lines of the call 0 to CrearCargoAbono");
        // FMM-CER-444
        CasoCerT3.Corrida c444 = corre("rescate-con-comision-calculada-de-certificacion-reduce-el-pago-en-la-cuenta");
        assertEquals(List.of("0|MOVABN|CLP|1000000|980002|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c444, 0), "rescate-con-comision-calculada-de-certificacion-reduce-el-pago-en-la-cuenta :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-42 / D-F3B-01 simulation S: the cuenta of the rescates receives simular N and the UsaReglasNegocio of the request")
    void fmCer42_simulacion() throws Exception {
        // FMM-CER-445
        CasoCerT3.Corrida c445 = corre("simulacion-S-la-cuenta-de-los-rescates-recibe-simular-N");
        assertEquals("N|S|N|2026-03-02|1|7|FMM", cabecera(c445, "CrearCargoAbono", 0), "simulacion-S-la-cuenta-de-los-rescates-recibe-simular-N :: head of the call to CrearCargoAbono");
        assertEquals(List.of("0|MOVABN|CLP|1000000|1000000|RESCFM|2026-03-03|2026-03-03|2026-03-02|1"), cuenta(c445, 0), "simulacion-S-la-cuenta-de-los-rescates-recibe-simular-N :: lines of the call 0 to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-43 an ERROR of the cuenta rejects the rescate whose line failed (also a secondary line of a deferred one) and sums the counter; non ERROR messages are ignored; the counter is added without messages")
    void fmCer43_errores() throws Exception {
        // FMM-CER-446
        CasoCerT3.Corrida c446 = corre("rescate-error-de-la-cuenta-rechaza-al-rescate-con-el-mensaje");
        assertEquals("3001=RZD:UI_ERROR_CUENTA_INVERSION;3002=OK", estados(c446), "rescate-error-de-la-cuenta-rechaza-al-rescate-con-el-mensaje :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;3001:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion", salida(c446), "rescate-error-de-la-cuenta-rechaza-al-rescate-con-el-mensaje :: counter and messages of the output");
        // FMM-CER-447
        CasoCerT3.Corrida c447 = corre("rescate-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-rescate");
        assertEquals("3001=RZD:UI_SALDO_CUENTA_INSUFICIENTE;3002=OK", estados(c447), "rescate-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-rescate :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;3001:ERROR:UI_SALDO_CUENTA_INSUFICIENTE:Saldo insuficiente en la cuenta de inversion", salida(c447), "rescate-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-rescate :: counter and messages of the output");
        // FMM-CER-448
        CasoCerT3.Corrida c448 = corre("rescate-errores-en-dos-rescates-rechazan-a-ambos");
        assertEquals("3001=RZD:UI_ERROR_CUENTA_INVERSION;3002=OK;3003=RZD:UI_ERROR_CUENTA_INVERSION", estados(c448), "rescate-errores-en-dos-rescates-rechazan-a-ambos :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=2;3001:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion|3003:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion", salida(c448), "rescate-errores-en-dos-rescates-rechazan-a-ambos :: counter and messages of the output");
        // FMM-CER-450
        CasoCerT3.Corrida c450 = corre("rescate-mensaje-que-no-es-ERROR-no-rechaza");
        assertEquals("3001=OK", estados(c450), "rescate-mensaje-que-no-es-ERROR-no-rechaza :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=0;", salida(c450), "rescate-mensaje-que-no-es-ERROR-no-rechaza :: counter and messages of the output");
        // FMM-CER-451
        CasoCerT3.Corrida c451 = corre("rescate-el-contador-de-la-cuenta-se-suma");
        assertEquals("3001=OK", estados(c451), "rescate-el-contador-de-la-cuenta-se-suma :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=2;", salida(c451), "rescate-el-contador-de-la-cuenta-se-suma :: counter and messages of the output");
    }

    @Test
    @DisplayName("FM-CER-43 / D-NOR-9 an error with a line number out of the map is a NullPointerException; an exception of the collaborator comes out untouched")
    void fmCer43_npe() throws Exception {
        // FMM-CER-449
        CasoCerT3.Corrida c449 = lanza("rescate-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote");
        assertEquals("NullPointerException", c449.excepcion.getClass().getSimpleName(), "rescate-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote");
        assertEquals(1, llamadas(c449, "CrearCargoAbono").size(), "rescate-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote :: calls to CrearCargoAbono");
        // FMM-CER-452
        CasoCerT3.Corrida c452 = lanza("rescate-la-cuenta-falla-y-propaga-la-excepcion");
        assertEquals("Exception: cuenta de inversion no disponible", CasoCer.texto(c452.excepcion), "rescate-la-cuenta-falla-y-propaga-la-excepcion");
        assertEquals(1, llamadas(c452, "CrearCargoAbono").size(), "rescate-la-cuenta-falla-y-propaga-la-excepcion :: calls to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-30 / FM-CER-43 an error in the aportes call rejects the aporte and an error in the rescates call rejects the rescate: each call maps its own line numbers (both 0 here) to its own solicitud")
    void fmCer30_43_ambasLlamadas() throws Exception {
        // FMM-CER-453
        CasoCerT3.Corrida c453 = corre("aporte-y-rescate-con-error-cada-uno-rechazan-en-su-llamada");
        assertEquals("3001=RZD:UI_ERROR_CUENTA_INVERSION;3002=RZD:UI_SALDO_CUENTA_INSUFICIENTE", estados(c453), "aporte-y-rescate-con-error-cada-uno-rechazan-en-su-llamada :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=2;3001:ERROR:UI_ERROR_CUENTA_INVERSION:Error al crear el cargo / abono en la cuenta de inversion|3002:ERROR:UI_SALDO_CUENTA_INSUFICIENTE:Saldo insuficiente en la cuenta de inversion", salida(c453), "aporte-y-rescate-con-error-cada-uno-rechazan-en-su-llamada :: counter and messages of the output");
        assertEquals(2, llamadas(c453, "CrearCargoAbono").size(), "aporte-y-rescate-con-error-cada-uno-rechazan-en-su-llamada :: calls to CrearCargoAbono");
    }

    @Test
    @DisplayName("FM-CER-43 / FM-CER-44 / D-F3B-03 a rescate rejected by the cuenta is NOT sent to Tesoreria, and the custody that was already retired stays (the other rescate goes on)")
    void fmCer43_noLlegaATesoreria() throws Exception {
        // FMM-CER-454
        CasoCerT3.Corrida c454 = corre("rescate-rechazado-por-la-cuenta-no-llega-a-tesoreria");
        assertEquals("3001=RZD:UI_ERROR_CUENTA_INVERSION;3002=OK", estados(c454), "rescate-rechazado-por-la-cuenta-no-llega-a-tesoreria :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals(List.of("3002|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c454), "rescate-rechazado-por-la-cuenta-no-llega-a-tesoreria :: entries sent to Tesoreria");
        assertEquals(1, llamadas(c454, "ProcesarIngresoEgresoTesoreria").size(), "rescate-rechazado-por-la-cuenta-no-llega-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-455
        CasoCerT3.Corrida c455 = corre("rescate-rechazado-por-la-cuenta-deja-la-custodia-ya-retirada");
        assertEquals("3001=OK;3002=RZD:UI_ERROR_CUENTA_INVERSION", estados(c455), "rescate-rechazado-por-la-cuenta-deja-la-custodia-ya-retirada :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals(1, llamadas(c455, "ProcesarCustodiaFondoMutuo").size(), "rescate-rechazado-por-la-cuenta-deja-la-custodia-ya-retirada :: calls to ProcesarCustodiaFondoMutuo");
    }

    @Test
    @DisplayName("FM-CER-44 a rescate paid by a medium other than CIN (TRA, empty, null of the view, CHE) goes to Tesoreria by path (a): the whole pojo, RelacionTraspaso N, FechaEmision = FechaLiquidacion, ParidadFechaInversion = ParidadFechaCertificacion, the medium as it is")
    void fmCer44_ramaA() throws Exception {
        // FMM-CER-456
        CasoCerT3.Corrida c456 = corre("rescate-pagado-por-transferencia-va-a-tesoreria-por-la-rama-a");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c456), "rescate-pagado-por-transferencia-va-a-tesoreria-por-la-rama-a :: entries sent to Tesoreria");
        assertEquals("N|N|N|1|7|FMM", cabecera(c456, "ProcesarIngresoEgresoTesoreria", 0), "rescate-pagado-por-transferencia-va-a-tesoreria-por-la-rama-a :: head of the call to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-458
        CasoCerT3.Corrida c458 = corre("rescate-con-medio-vacio-va-a-tesoreria");
        assertEquals(List.of("3001|1000000|N||0|0|0|||2026-03-03|1|0"), tesoreria(c458), "rescate-con-medio-vacio-va-a-tesoreria :: entries sent to Tesoreria");
        // FMM-CER-459
        CasoCerT3.Corrida c459 = corre("rescate-con-medio-nulo-de-la-vista-va-a-tesoreria");
        assertEquals(List.of("3001|1000000|N|null|0|0|0|||2026-03-03|1|0"), tesoreria(c459), "rescate-con-medio-nulo-de-la-vista-va-a-tesoreria :: entries sent to Tesoreria");
        // FMM-CER-460
        CasoCerT3.Corrida c460 = corre("rescate-con-medio-cheque-va-a-tesoreria-con-ese-medio");
        assertEquals(List.of("3001|1000000|N|CHE|0|0|0|||2026-03-03|1|0"), tesoreria(c460), "rescate-con-medio-cheque-va-a-tesoreria-con-ese-medio :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-44 does NOT go to Tesoreria: medium CIN, RESREINV, IndicadorMLT other than N (S, s), GeneraSpot S (goes to Spot), an aporte, a rescate rejected by custody or without cartera")
    void fmCer44_quienNoVa() throws Exception {
        // FMM-CER-457
        CasoCerT3.Corrida c457 = corre("rescate-pagado-por-la-cuenta-de-inversion-no-va-a-tesoreria");
        assertEquals(0, llamadas(c457, "ProcesarIngresoEgresoTesoreria").size(), "rescate-pagado-por-la-cuenta-de-inversion-no-va-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-461
        CasoCerT3.Corrida c461 = corre("rescate-de-reinversion-no-va-a-tesoreria");
        assertEquals(0, llamadas(c461, "ProcesarIngresoEgresoTesoreria").size(), "rescate-de-reinversion-no-va-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-462
        CasoCerT3.Corrida c462 = corre("rescate-mlt-S-no-va-a-tesoreria");
        assertEquals(0, llamadas(c462, "ProcesarIngresoEgresoTesoreria").size(), "rescate-mlt-S-no-va-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-463
        CasoCerT3.Corrida c463 = corre("rescate-mlt-minuscula-s-no-es-N-y-no-va-a-tesoreria");
        assertEquals(0, llamadas(c463, "ProcesarIngresoEgresoTesoreria").size(), "rescate-mlt-minuscula-s-no-es-N-y-no-va-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-464
        CasoCerT3.Corrida c464 = corre("rescate-con-spot-no-va-a-tesoreria-va-a-spot");
        assertEquals(0, llamadas(c464, "ProcesarIngresoEgresoTesoreria").size(), "rescate-con-spot-no-va-a-tesoreria-va-a-spot :: calls to ProcesarIngresoEgresoTesoreria");
        assertEquals(1, llamadas(c464, "ProcesarIngresoOperacionSpot").size(), "rescate-con-spot-no-va-a-tesoreria-va-a-spot :: calls to ProcesarIngresoOperacionSpot");
        // FMM-CER-465
        CasoCerT3.Corrida c465 = corre("los-aportes-no-pasan-por-tesoreria");
        assertEquals(1, llamadas(c465, "ProcesarIngresoEgresoTesoreria").size(), "los-aportes-no-pasan-por-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        assertEquals(List.of("3002|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c465), "los-aportes-no-pasan-por-tesoreria :: entries sent to Tesoreria");
        // FMM-CER-466
        CasoCerT3.Corrida c466 = corre("rescate-rechazado-por-custodia-no-va-a-tesoreria");
        assertEquals(List.of("3002|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c466), "rescate-rechazado-por-custodia-no-va-a-tesoreria :: entries sent to Tesoreria");
        assertEquals("3001=RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION;3002=OK", estados(c466), "rescate-rechazado-por-custodia-no-va-a-tesoreria :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-467
        CasoCerT3.Corrida c467 = corre("rescate-sin-cartera-retirada-no-va-a-tesoreria");
        assertEquals(List.of("3002|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c467), "rescate-sin-cartera-retirada-no-va-a-tesoreria :: entries sent to Tesoreria");
        assertEquals("3001=RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO;3002=OK", estados(c467), "rescate-sin-cartera-retirada-no-va-a-tesoreria :: state of every solicitud (OK = pending, certified by the end)");
    }

    @Test
    @DisplayName("FM-CER-44 the entries go in the order of the rescates (ascending id, whatever the request) and only the ones that qualify; an ADV rescate carries the amounts of custody")
    void fmCer44_orden() throws Exception {
        // FMM-CER-468
        CasoCerT3.Corrida c468 = corre("lote-de-rescates-llega-en-orden-de-id-aunque-la-solicitud-este-invertida");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-03|1|0", "3003|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c468), "lote-de-rescates-llega-en-orden-de-id-aunque-la-solicitud-este-invertida :: entries sent to Tesoreria");
        // FMM-CER-469
        CasoCerT3.Corrida c469 = corre("rescate-ADV-envia-los-montos-ajustados-de-custodia");
        assertEquals(List.of("3001|150004|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c469), "rescate-ADV-envia-los-montos-ajustados-de-custodia :: entries sent to Tesoreria");
        assertEquals("3001=ADV:UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION", estados(c469), "rescate-ADV-envia-los-montos-ajustados-de-custodia :: state of every solicitud (OK = pending, certified by the end)");
    }

    @Test
    @DisplayName("FM-CER-44 / D-F3B-07 FechaEmision is FechaLiquidacion whether it is before or after the system date (the `> 1` of the legacy is never true)")
    void fmCer44_fechaEmision() throws Exception {
        // FMM-CER-470
        CasoCerT3.Corrida c470 = corre("fecha-de-emision-es-la-de-liquidacion-aunque-la-de-sistema-sea-posterior");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-01|1|0"), tesoreria(c470), "fecha-de-emision-es-la-de-liquidacion-aunque-la-de-sistema-sea-posterior :: entries sent to Tesoreria");
        // FMM-CER-471
        CasoCerT3.Corrida c471 = corre("fecha-de-emision-es-la-de-liquidacion-cuando-es-posterior-al-sistema");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-09|1|0"), tesoreria(c471), "fecha-de-emision-es-la-de-liquidacion-cuando-es-posterior-al-sistema :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-44 / D-F3B-41 the pojo copies IdPreferenciaPagoPortafolio into IdAutorizadoOrigen (77 in both)")
    void fmCer44_autorizadoOrigen() throws Exception {
        // FMM-CER-472
        CasoCerT3.Corrida c472 = corre("el-pojo-copia-la-preferencia-de-pago-en-el-autorizado-de-origen");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-03|1|77"), tesoreria(c472), "el-pojo-copia-la-preferencia-de-pago-en-el-autorizado-de-origen :: entries sent to Tesoreria");
        // FMM-CER-473
        CasoCerT3.Corrida c473 = corre("el-pojo-no-copia-el-valor-cuota-de-la-vista-ni-la-empresa");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c473), "el-pojo-no-copia-el-valor-cuota-de-la-vista-ni-la-empresa :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-47 / D-F3B-01 Tesoreria receives simular N, ingreso automatico N and the UsaReglasNegocio of the request, also in simulation S")
    void fmCer47_simulacion() throws Exception {
        // FMM-CER-474
        CasoCerT3.Corrida c474 = corre("simulacion-S-tesoreria-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada");
        assertEquals("N|S|N|1|7|FMM", cabecera(c474, "ProcesarIngresoEgresoTesoreria", 0), "simulacion-S-tesoreria-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada :: head of the call to ProcesarIngresoEgresoTesoreria");
        assertEquals(1, llamadas(c474, "ProcesarIngresoEgresoTesoreria").size(), "simulacion-S-tesoreria-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-475
        CasoCerT3.Corrida c475 = corre("ingreso-automatico-N-y-usuario-y-modulo-de-la-entrada");
        assertEquals("N|N|N|1|7|FMM", cabecera(c475, "ProcesarIngresoEgresoTesoreria", 0), "ingreso-automatico-N-y-usuario-y-modulo-de-la-entrada :: head of the call to ProcesarIngresoEgresoTesoreria");
    }

    @Test
    @DisplayName("FM-CER-44 path (b): a rescate with traspaso (APV, no APV, of the related solicitud, even with medium CIN or MLT S or RESREINV) goes with RelacionTraspaso S, medium TRA, and the person, bank, account type, number and name of the traspaso (zeros and empties when it has no account)")
    void fmCer44_traspaso() throws Exception {
        // FMM-CER-476
        CasoCerT3.Corrida c476 = corre("traspaso-apv-con-medio-cin-va-a-tesoreria-por-la-rama-b");
        assertEquals(List.of("3001|1000000|S|TRA|101|9101|13|0001230101||2026-03-03|1|0"), tesoreria(c476), "traspaso-apv-con-medio-cin-va-a-tesoreria-por-la-rama-b :: entries sent to Tesoreria");
        // FMM-CER-477
        CasoCerT3.Corrida c477 = corre("traspaso-apv-con-medio-tra-va-por-la-rama-b-y-no-por-la-a");
        assertEquals(List.of("3001|1000000|S|TRA|101|9101|13|0001230101||2026-03-03|1|0"), tesoreria(c477), "traspaso-apv-con-medio-tra-va-por-la-rama-b-y-no-por-la-a :: entries sent to Tesoreria");
        // FMM-CER-478
        CasoCerT3.Corrida c478 = corre("traspaso-no-apv-va-a-tesoreria-con-la-cuenta-de-la-entidad");
        assertEquals(List.of("3001|1000000|S|TRA|202|9202|14|0001230202||2026-03-03|1|0"), tesoreria(c478), "traspaso-no-apv-va-a-tesoreria-con-la-cuenta-de-la-entidad :: entries sent to Tesoreria");
        // FMM-CER-479
        CasoCerT3.Corrida c479 = corre("traspaso-de-la-solicitud-relacionada-va-a-tesoreria");
        assertEquals(List.of("3002|1000000|S|TRA|202|9202|14|0001230202||2026-03-03|1|0"), tesoreria(c479), "traspaso-de-la-solicitud-relacionada-va-a-tesoreria :: entries sent to Tesoreria");
        // FMM-CER-480
        CasoCerT3.Corrida c480 = corre("traspaso-sin-cuenta-bancaria-envia-ceros-y-vacios");
        assertEquals(List.of("3001|1000000|S|TRA|105|0|0|||2026-03-03|1|0"), tesoreria(c480), "traspaso-sin-cuenta-bancaria-envia-ceros-y-vacios :: entries sent to Tesoreria");
        // FMM-CER-482
        CasoCerT3.Corrida c482 = corre("traspaso-solo-envia-a-la-solicitud-que-lo-tiene");
        assertEquals(List.of("3001|1000000|S|TRA|101|9101|13|0001230101||2026-03-03|1|0", "3002|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c482), "traspaso-solo-envia-a-la-solicitud-que-lo-tiene :: entries sent to Tesoreria");
        // FMM-CER-483
        CasoCerT3.Corrida c483 = corre("traspaso-con-mlt-S-va-a-tesoreria-por-la-rama-b");
        assertEquals(List.of("3001|1000000|S|TRA|101|9101|13|0001230101||2026-03-03|1|0"), tesoreria(c483), "traspaso-con-mlt-S-va-a-tesoreria-por-la-rama-b :: entries sent to Tesoreria");
        // FMM-CER-484
        CasoCerT3.Corrida c484 = corre("traspaso-de-un-rescate-de-reinversion-va-por-la-rama-b");
        assertEquals(List.of("3001|1000000|S|TRA|101|9101|13|0001230101||2026-03-03|1|0"), tesoreria(c484), "traspaso-de-un-rescate-de-reinversion-va-por-la-rama-b :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-44 a rescate with traspaso and Spot, or rejected by custody, is not sent to Tesoreria; with APV-B the retention does not apply and there is no SII payment")
    void fmCer44_traspasoNoVa() throws Exception {
        // FMM-CER-481
        CasoCerT3.Corrida c481 = corre("traspaso-con-spot-no-va-a-tesoreria");
        assertEquals(0, llamadas(c481, "ProcesarIngresoEgresoTesoreria").size(), "traspaso-con-spot-no-va-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        assertEquals(1, llamadas(c481, "ProcesarIngresoOperacionSpot").size(), "traspaso-con-spot-no-va-a-tesoreria :: calls to ProcesarIngresoOperacionSpot");
        // FMM-CER-485
        CasoCerT3.Corrida c485 = corre("traspaso-del-rescate-rechazado-por-custodia-no-va");
        assertEquals(0, llamadas(c485, "ProcesarIngresoEgresoTesoreria").size(), "traspaso-del-rescate-rechazado-por-custodia-no-va :: calls to ProcesarIngresoEgresoTesoreria");
        assertEquals("3001=RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION", estados(c485), "traspaso-del-rescate-rechazado-por-custodia-no-va :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-486
        CasoCerT3.Corrida c486 = corre("traspaso-apv-b-no-aplica-retencion-ni-pago-al-sii");
        assertEquals(List.of("3001|1000000|S|TRA|101|9101|13|0001230101||2026-03-03|1|0"), tesoreria(c486), "traspaso-apv-b-no-aplica-retencion-ni-pago-al-sii :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-45 / D-F3B-09 APV-B and APV-AN with retention (15 % of 1.000.000): the entry carries MontoPagoLocal 850.000 and a SECOND entry (IdInterno -3001) carries 150.000 with the SII person and account, medium TRA, RelacionTraspaso S and the concept RETSII; with percentage 0 the SII entry is sent with MontoPagoLocal 0")
    void fmCer45_retencion() throws Exception {
        // FMM-CER-487
        CasoCerT3.Corrida c487 = corre("apv-b-resta-la-retencion-y-agrega-el-pago-al-sii");
        assertEquals(List.of("3001|850000|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|150000|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0"), tesoreria(c487), "apv-b-resta-la-retencion-y-agrega-el-pago-al-sii :: entries sent to Tesoreria");
        // FMM-CER-488
        CasoCerT3.Corrida c488 = corre("apv-an-resta-la-retencion-y-agrega-el-pago-al-sii");
        assertEquals(List.of("3001|850000|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|150000|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0"), tesoreria(c488), "apv-an-resta-la-retencion-y-agrega-el-pago-al-sii :: entries sent to Tesoreria");
        // FMM-CER-491
        CasoCerT3.Corrida c491 = corre("apv-b-sin-retencion-porcentaje-cero-igual-envia-la-entrada-del-sii");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|0|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0"), tesoreria(c491), "apv-b-sin-retencion-porcentaje-cero-igual-envia-la-entrada-del-sii :: entries sent to Tesoreria");
        // FMM-CER-492
        CasoCerT3.Corrida c492 = corre("apv-an-sin-retencion-porcentaje-cero-igual-envia-la-entrada-del-sii");
        assertEquals(List.of("3001|1000000|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|0|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0"), tesoreria(c492), "apv-an-sin-retencion-porcentaje-cero-igual-envia-la-entrada-del-sii :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-45 / D-F3B-08 APV-AN without SII configured raises NullPointerException AFTER custody and cuenta were called; with medium CIN it never reaches Tesoreria; D-F3B-44 a missing CONCEPTO_RETENCION_PAGO_SII raises before the call, also after custody and cuenta")
    void fmCer45_npe() throws Exception {
        // FMM-CER-489
        CasoCerT3.Corrida c489 = lanza("apv-an-sin-sii-configurada-lanza-NPE-despues-de-escribir-custodia-y-cuenta");
        assertEquals("NullPointerException", c489.excepcion.getClass().getSimpleName(), "apv-an-sin-sii-configurada-lanza-NPE-despues-de-escribir-custodia-y-cuenta");
        assertEquals(1, llamadas(c489, "ProcesarCustodiaFondoMutuo").size(), "apv-an-sin-sii-configurada-lanza-NPE-despues-de-escribir-custodia-y-cuenta :: calls to ProcesarCustodiaFondoMutuo");
        assertEquals(1, llamadas(c489, "CrearCargoAbono").size(), "apv-an-sin-sii-configurada-lanza-NPE-despues-de-escribir-custodia-y-cuenta :: calls to CrearCargoAbono");
        assertEquals(0, llamadas(c489, "ProcesarIngresoEgresoTesoreria").size(), "apv-an-sin-sii-configurada-lanza-NPE-despues-de-escribir-custodia-y-cuenta :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-490
        CasoCerT3.Corrida c490 = corre("apv-an-sin-sii-pero-con-medio-cin-no-llega-a-tesoreria");
        assertEquals(0, llamadas(c490, "ProcesarIngresoEgresoTesoreria").size(), "apv-an-sin-sii-pero-con-medio-cin-no-llega-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        assertEquals("3001=OK", estados(c490), "apv-an-sin-sii-pero-con-medio-cin-no-llega-a-tesoreria :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-497
        CasoCerT3.Corrida c497 = lanza("apv-b-sin-concepto-configurado-lanza-excepcion-antes-de-llamar-a-tesoreria");
        assertEquals("Exception: Parametro CONCEPTO_RETENCION_PAGO_SII en modulo FONDOMUTUO no encontrado", CasoCer.texto(c497.excepcion), "apv-b-sin-concepto-configurado-lanza-excepcion-antes-de-llamar-a-tesoreria");
        assertEquals(1, llamadas(c497, "ProcesarCustodiaFondoMutuo").size(), "apv-b-sin-concepto-configurado-lanza-excepcion-antes-de-llamar-a-tesoreria :: calls to ProcesarCustodiaFondoMutuo");
        assertEquals(1, llamadas(c497, "CrearCargoAbono").size(), "apv-b-sin-concepto-configurado-lanza-excepcion-antes-de-llamar-a-tesoreria :: calls to CrearCargoAbono");
        assertEquals(0, llamadas(c497, "ProcesarIngresoEgresoTesoreria").size(), "apv-b-sin-concepto-configurado-lanza-excepcion-antes-de-llamar-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
    }

    @Test
    @DisplayName("FM-CER-45 APV-B with medium CIN, or with Spot, does not go to Tesoreria nor to the SII")
    void fmCer45_noVa() throws Exception {
        // FMM-CER-493
        CasoCerT3.Corrida c493 = corre("apv-b-con-medio-cin-no-va-a-tesoreria-ni-al-sii");
        assertEquals(0, llamadas(c493, "ProcesarIngresoEgresoTesoreria").size(), "apv-b-con-medio-cin-no-va-a-tesoreria-ni-al-sii :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-494
        CasoCerT3.Corrida c494 = corre("apv-b-con-spot-no-va-a-tesoreria-ni-al-sii");
        assertEquals(0, llamadas(c494, "ProcesarIngresoEgresoTesoreria").size(), "apv-b-con-spot-no-va-a-tesoreria-ni-al-sii :: calls to ProcesarIngresoEgresoTesoreria");
        assertEquals(1, llamadas(c494, "ProcesarIngresoOperacionSpot").size(), "apv-b-con-spot-no-va-a-tesoreria-ni-al-sii :: calls to ProcesarIngresoOperacionSpot");
    }

    @Test
    @DisplayName("FM-CER-45 / D-F3B-09 in a USD fund the retention (828,73 USD-fund amount) is subtracted from the pago local in CLP: 5.000.000 - 828,73 = 4.999.171,27; decimals are kept exactly (7 % of 1.000,37 = 70,0259 -> the entries 930 / 70 after the format)")
    void fmCer45_fondoUsd() throws Exception {
        // FMM-CER-495
        CasoCerT3.Corrida c495 = corre("apv-b-fondo-en-usd-resta-el-monto-del-fondo-del-pago-local");
        assertEquals(List.of("3001|4999171.27|N|TRA|0|0|0|||2026-03-03|905|0", "-3001|828.73|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|905|0"), tesoreria(c495), "apv-b-fondo-en-usd-resta-el-monto-del-fondo-del-pago-local :: entries sent to Tesoreria");
        // FMM-CER-496
        CasoCerT3.Corrida c496 = corre("apv-b-retencion-con-decimales-exactos");
        assertEquals(List.of("3001|930|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|70|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0"), tesoreria(c496), "apv-b-retencion-con-decimales-exactos :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-45 each APV rescate is followed by its SII entry; the normal rescate has none")
    void fmCer45_orden() throws Exception {
        // FMM-CER-498
        CasoCerT3.Corrida c498 = corre("apv-b-y-rescate-normal-orden-de-las-entradas-del-lote");
        assertEquals(List.of("3001|850000|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|150000|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0", "3002|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c498), "apv-b-y-rescate-normal-orden-de-las-entradas-del-lote :: entries sent to Tesoreria");
        // FMM-CER-499
        CasoCerT3.Corrida c499 = corre("dos-apv-cada-uno-con-su-entrada-del-sii-despues-de-la-propia");
        assertEquals(List.of("3001|850000|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|150000|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0", "3002|850000|N|TRA|0|0|0|||2026-03-03|1|0", "-3002|150000|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0"), tesoreria(c499), "dos-apv-cada-uno-con-su-entrada-del-sii-despues-de-la-propia :: entries sent to Tesoreria");
        // FMM-CER-500
        CasoCerT3.Corrida c500 = corre("apv-b-con-comision-calculada-resta-la-retencion-bruta-del-pago-neto");
        assertEquals(List.of("3001|830002|N|TRA|0|0|0|||2026-03-03|1|0", "-3001|150000|S|TRA|3002|9302|12|0001230002|RETSII|2026-03-03|1|0"), tesoreria(c500), "apv-b-con-comision-calculada-resta-la-retencion-bruta-del-pago-neto :: entries sent to Tesoreria");
    }

    @Test
    @DisplayName("FM-CER-46 BF takes the TGR person and account (3001, bank 11), medium TRA and RelacionTraspaso S, without retention and without SII entry; with medium CIN it is not sent; with a traspaso path (b) wins; without TGR configured F-A rejects it")
    void fmCer46_bonificacionFiscal() throws Exception {
        // FMM-CER-501
        CasoCerT3.Corrida c501 = corre("bonificacion-fiscal-usa-la-persona-y-la-cuenta-de-la-tgr");
        assertEquals(List.of("3001|1000000|S|TRA|3001|9301|11|0001230001||2026-03-03|1|0"), tesoreria(c501), "bonificacion-fiscal-usa-la-persona-y-la-cuenta-de-la-tgr :: entries sent to Tesoreria");
        // FMM-CER-502
        CasoCerT3.Corrida c502 = corre("bonificacion-fiscal-con-medio-cin-no-va-a-tesoreria");
        assertEquals(0, llamadas(c502, "ProcesarIngresoEgresoTesoreria").size(), "bonificacion-fiscal-con-medio-cin-no-va-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-503
        CasoCerT3.Corrida c503 = corre("bonificacion-fiscal-con-traspaso-usa-la-rama-b-y-no-la-tgr");
        assertEquals(List.of("3001|1000000|S|TRA|101|9101|13|0001230101||2026-03-03|1|0"), tesoreria(c503), "bonificacion-fiscal-con-traspaso-usa-la-rama-b-y-no-la-tgr :: entries sent to Tesoreria");
        // FMM-CER-504
        CasoCerT3.Corrida c504 = corre("bonificacion-fiscal-sin-retencion-aunque-haya-porcentaje");
        assertEquals(List.of("3001|1000000|S|TRA|3001|9301|11|0001230001||2026-03-03|1|0"), tesoreria(c504), "bonificacion-fiscal-sin-retencion-aunque-haya-porcentaje :: entries sent to Tesoreria");
        // FMM-CER-505
        CasoCerT3.Corrida c505 = corre("bonificacion-fiscal-sin-tgr-configurada-se-rechaza-antes-de-tesoreria");
        assertEquals(List.of("3002|1000000|N|TRA|0|0|0|||2026-03-03|1|0"), tesoreria(c505), "bonificacion-fiscal-sin-tgr-configurada-se-rechaza-antes-de-tesoreria :: entries sent to Tesoreria");
        assertEquals("3001=RZD:UI_NO_EXISTE_TGR_CONFIGURADA;3002=OK", estados(c505), "bonificacion-fiscal-sin-tgr-configurada-se-rechaza-antes-de-tesoreria :: state of every solicitud (OK = pending, certified by the end)");
    }

    @Test
    @DisplayName("FM-CER-47 / D-F3B-03 / D-F3B-43 only Resultado RECH with Campo NumeroReferencia rejects (the rescate whose id is Math.abs(Id): also -3001 of the SII entry): state RZD WITHOUT code, the custody and the cuenta already written; other Campo, lowercase rech, other Resultado or an unknown id are ignored")
    void fmCer47_lecturaDelRechazo() throws Exception {
        // FMM-CER-507
        CasoCerT3.Corrida c507 = corre("tesoreria-RECH-de-NumeroReferencia-rechaza-al-rescate-y-deja-los-efectos-previos");
        assertEquals("3001=RZD:;3002=OK", estados(c507), "tesoreria-RECH-de-NumeroReferencia-rechaza-al-rescate-y-deja-los-efectos-previos :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3002=0/0", filas(c507), "tesoreria-RECH-de-NumeroReferencia-rechaza-al-rescate-y-deja-los-efectos-previos :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        assertEquals(1, llamadas(c507, "ProcesarCustodiaFondoMutuo").size(), "tesoreria-RECH-de-NumeroReferencia-rechaza-al-rescate-y-deja-los-efectos-previos :: calls to ProcesarCustodiaFondoMutuo");
        assertEquals(1, llamadas(c507, "CrearCargoAbono").size(), "tesoreria-RECH-de-NumeroReferencia-rechaza-al-rescate-y-deja-los-efectos-previos :: calls to CrearCargoAbono");
        // FMM-CER-508
        CasoCerT3.Corrida c508 = corre("tesoreria-RECH-con-id-negativo-de-la-entrada-del-sii-rechaza-al-rescate");
        assertEquals("3001=RZD:;3002=OK", estados(c508), "tesoreria-RECH-con-id-negativo-de-la-entrada-del-sii-rechaza-al-rescate :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-509
        CasoCerT3.Corrida c509 = corre("tesoreria-RECH-de-otro-campo-no-rechaza");
        assertEquals("3001=OK", estados(c509), "tesoreria-RECH-de-otro-campo-no-rechaza :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-510
        CasoCerT3.Corrida c510 = corre("tesoreria-resultado-en-minuscula-no-rechaza-por-equals");
        assertEquals("3001=OK", estados(c510), "tesoreria-resultado-en-minuscula-no-rechaza-por-equals :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-511
        CasoCerT3.Corrida c511 = corre("tesoreria-resultado-que-no-es-RECH-no-rechaza");
        assertEquals("3001=OK", estados(c511), "tesoreria-resultado-que-no-es-RECH-no-rechaza :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-512
        CasoCerT3.Corrida c512 = corre("tesoreria-RECH-de-un-id-sin-rescate-se-ignora");
        assertEquals("3001=OK", estados(c512), "tesoreria-RECH-de-un-id-sin-rescate-se-ignora :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-514
        CasoCerT3.Corrida c514 = corre("tesoreria-RECH-sin-campo-con-id-ajeno-no-evalua-el-campo");
        assertEquals("3001=OK", estados(c514), "tesoreria-RECH-sin-campo-con-id-ajeno-no-evalua-el-campo :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-515
        CasoCerT3.Corrida c515 = corre("tesoreria-RECH-de-todos-los-rescates-no-deja-ninguno-para-certificar");
        assertEquals("3001=RZD:;3002=RZD:", estados(c515), "tesoreria-RECH-de-todos-los-rescates-no-deja-ninguno-para-certificar :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("", filas(c515), "tesoreria-RECH-de-todos-los-rescates-no-deja-ninguno-para-certificar :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-525
        CasoCerT3.Corrida c525 = corre("tesoreria-RECH-del-primero-y-el-segundo-se-certifica-con-su-ingreso-egreso");
        assertEquals("3001=RZD:;3002=OK", estados(c525), "tesoreria-RECH-del-primero-y-el-segundo-se-certifica-con-su-ingreso-egreso :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3002=502/0", filas(c525), "tesoreria-RECH-del-primero-y-el-segundo-se-certifica-con-su-ingreso-egreso :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
    }

    @Test
    @DisplayName("FM-CER-47 / D-NOR-9 a RECH of the id of a rescate without Campo, or a generated income without NumeroReferencia, is a NullPointerException; an exception of Tesoreria comes out untouched")
    void fmCer47_npe() throws Exception {
        // FMM-CER-513
        CasoCerT3.Corrida c513 = lanza("tesoreria-RECH-sin-campo-con-id-del-rescate-lanza-NPE");
        assertEquals("NullPointerException", c513.excepcion.getClass().getSimpleName(), "tesoreria-RECH-sin-campo-con-id-del-rescate-lanza-NPE");
        assertEquals(1, llamadas(c513, "ProcesarIngresoEgresoTesoreria").size(), "tesoreria-RECH-sin-campo-con-id-del-rescate-lanza-NPE :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-520
        CasoCerT3.Corrida c520 = lanza("tesoreria-generado-sin-numero-de-referencia-lanza-NPE");
        assertEquals("NullPointerException", c520.excepcion.getClass().getSimpleName(), "tesoreria-generado-sin-numero-de-referencia-lanza-NPE");
        assertEquals(1, llamadas(c520, "ProcesarIngresoEgresoTesoreria").size(), "tesoreria-generado-sin-numero-de-referencia-lanza-NPE :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-523
        CasoCerT3.Corrida c523 = lanza("tesoreria-falla-y-propaga-la-excepcion");
        assertEquals("Exception: tesoreria no disponible", CasoCer.texto(c523.excepcion), "tesoreria-falla-y-propaga-la-excepcion");
        assertEquals(1, llamadas(c523, "ProcesarIngresoEgresoTesoreria").size(), "tesoreria-falla-y-propaga-la-excepcion :: calls to ProcesarIngresoEgresoTesoreria");
    }

    @Test
    @DisplayName("FM-CER-47 / D-F3B-03 the counter (3) and the ERROR messages of Tesoreria are NOT added to the output: counter 0, no messages, the rescate certifies")
    void fmCer47_contadorYMensajes() throws Exception {
        // FMM-CER-516
        CasoCerT3.Corrida c516 = corre("tesoreria-el-contador-y-los-mensajes-no-se-suman-a-la-salida");
        assertEquals("3001=OK", estados(c516), "tesoreria-el-contador-y-los-mensajes-no-se-suman-a-la-salida :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=0;", salida(c516), "tesoreria-el-contador-y-los-mensajes-no-se-suman-a-la-salida :: counter and messages of the output");
    }

    @Test
    @DisplayName("FM-CER-47 the generated income / expense goes to the first solicitud whose id is NumeroReferencia (the last one wins when repeated; an unknown reference is ignored; the SII entry does not match); a generated one of a rejected rescate is not written")
    void fmCer47_generados() throws Exception {
        // FMM-CER-517
        CasoCerT3.Corrida c517 = corre("tesoreria-generado-asigna-el-ingreso-egreso-a-la-solicitud");
        assertEquals("3001=501/0;3002=502/0", filas(c517), "tesoreria-generado-asigna-el-ingreso-egreso-a-la-solicitud :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-518
        CasoCerT3.Corrida c518 = corre("tesoreria-generado-de-una-referencia-sin-rescate-se-ignora");
        assertEquals("3001=0/0", filas(c518), "tesoreria-generado-de-una-referencia-sin-rescate-se-ignora :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-519
        CasoCerT3.Corrida c519 = corre("tesoreria-dos-generados-de-la-misma-referencia-gana-el-ultimo");
        assertEquals("3001=502/0", filas(c519), "tesoreria-dos-generados-de-la-misma-referencia-gana-el-ultimo :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-521
        CasoCerT3.Corrida c521 = corre("tesoreria-generado-y-RECH-del-mismo-rescate-deja-el-rescate-rechazado");
        assertEquals("3001=RZD:;3002=OK", estados(c521), "tesoreria-generado-y-RECH-del-mismo-rescate-deja-el-rescate-rechazado :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3002=502/0", filas(c521), "tesoreria-generado-y-RECH-del-mismo-rescate-deja-el-rescate-rechazado :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-522
        CasoCerT3.Corrida c522 = corre("tesoreria-con-pago-al-sii-el-generado-del-sii-no-coincide-con-la-solicitud");
        assertEquals("3001=501/0", filas(c522), "tesoreria-con-pago-al-sii-el-generado-del-sii-no-coincide-con-la-solicitud :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
    }

    @Test
    @DisplayName("FM-CER-47 without entries Tesoreria is not called; a RECH of the first rescate leaves the second certified with its own ingreso / egreso; without rejection or generated nothing changes")
    void fmCer47_sinEntradas() throws Exception {
        // FMM-CER-524
        CasoCerT3.Corrida c524 = corre("tesoreria-sin-entradas-no-se-llama");
        assertEquals(0, llamadas(c524, "ProcesarIngresoEgresoTesoreria").size(), "tesoreria-sin-entradas-no-se-llama :: calls to ProcesarIngresoEgresoTesoreria");
        // FMM-CER-506
        CasoCerT3.Corrida c506 = corre("tesoreria-sin-rechazos-ni-generados-no-cambia-nada");
        assertEquals("3001=OK", estados(c506), "tesoreria-sin-rechazos-ni-generados-no-cambia-nada :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3001=0/0", filas(c506), "tesoreria-sin-rechazos-ni-generados-no-cambia-nada :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
    }

    @Test
    @DisplayName("FM-CER-59 a rescate that generates Spot (USD paid in CLP with the parameter) is sent with the 126-key pojo and TWO forms: `recibe` R = MOVEGR, COMSPT_CIN_USD_0, the amount of the rescate (1.780.000), and `paga` P = MOVING, VENSPT_CIN_CLP_0, MontoMonedaSistema, AfectarTesoreria N with the CIN medium")
    void fmCer59_construccion() throws Exception {
        // FMM-CER-526
        CasoCerT3.Corrida c526 = corre("spot-rescate-usd-pagado-en-clp-con-medio-cin");
        assertEquals(List.of("3001|1780000|CIN|S"), spotOperaciones(c526), "spot-rescate-usd-pagado-en-clp-con-medio-cin :: operations sent to Spot");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-03"), spotFormas(c526), "spot-rescate-usd-pagado-en-clp-con-medio-cin :: payment forms sent to Spot");
        assertEquals("S|N|N|1|7|FMM", cabecera(c526, "ProcesarIngresoOperacionSpot", 0), "spot-rescate-usd-pagado-en-clp-con-medio-cin :: head of the call to ProcesarIngresoOperacionSpot");
        assertEquals("3001=OK", estados(c526), "spot-rescate-usd-pagado-en-clp-con-medio-cin :: state of every solicitud (OK = pending, certified by the end)");
    }

    @Test
    @DisplayName("FM-CER-59 the medium of the `paga` form: TRA -> VENSPT_TRA_CLP_0 with AfectarTesoreria S; empty or null of the view -> CIN; CHE -> VENSPT_CHE_CLP_0")
    void fmCer59_medioDePago() throws Exception {
        // FMM-CER-527
        CasoCerT3.Corrida c527 = corre("spot-medio-transferencia-afecta-tesoreria-en-la-forma-que-paga");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_TRA_CLP_0|CLP|1780000|S|2026-03-03"), spotFormas(c527), "spot-medio-transferencia-afecta-tesoreria-en-la-forma-que-paga :: payment forms sent to Spot");
        // FMM-CER-528
        CasoCerT3.Corrida c528 = corre("spot-medio-vacio-se-toma-como-cin");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-03"), spotFormas(c528), "spot-medio-vacio-se-toma-como-cin :: payment forms sent to Spot");
        // FMM-CER-529
        CasoCerT3.Corrida c529 = corre("spot-medio-nulo-de-la-vista-se-toma-como-cin");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-03"), spotFormas(c529), "spot-medio-nulo-de-la-vista-se-toma-como-cin :: payment forms sent to Spot");
        // FMM-CER-530
        CasoCerT3.Corrida c530 = corre("spot-medio-cheque-arma-la-forma-de-pago-con-ese-medio");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_CHE_CLP_0|CLP|1780000|S|2026-03-03"), spotFormas(c530), "spot-medio-cheque-arma-la-forma-de-pago-con-ese-medio :: payment forms sent to Spot");
    }

    @Test
    @DisplayName("FM-CER-59 a RESCNN `recibe` form affects Tesoreria (S); with a periodic commission (PER) the form is COMSPT_TRA, with a non periodic one COMSPT_CIN")
    void fmCer59_rescateNn() throws Exception {
        // FMM-CER-531
        CasoCerT3.Corrida c531 = corre("spot-rescate-nn-afecta-tesoreria-en-la-forma-que-recibe");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_CLP_0|CLP|1000000|S|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1000000|N|2026-03-03"), spotFormas(c531), "spot-rescate-nn-afecta-tesoreria-en-la-forma-que-recibe :: payment forms sent to Spot");
        // FMM-CER-532
        CasoCerT3.Corrida c532 = corre("spot-rescate-nn-con-comision-periodica-usa-la-forma-de-transferencia");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_TRA_CLP_0|CLP|1000000|S|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1000000|N|2026-03-03"), spotFormas(c532), "spot-rescate-nn-con-comision-periodica-usa-la-forma-de-transferencia :: payment forms sent to Spot");
        // FMM-CER-533
        CasoCerT3.Corrida c533 = corre("spot-con-comision-periodica-usa-la-forma-comspt-tra");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_TRA_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-03"), spotFormas(c533), "spot-con-comision-periodica-usa-la-forma-comspt-tra :: payment forms sent to Spot");
        // FMM-CER-534
        CasoCerT3.Corrida c534 = corre("spot-con-comision-no-periodica-usa-la-forma-comspt-cin");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-03"), spotFormas(c534), "spot-con-comision-no-periodica-usa-la-forma-comspt-cin :: payment forms sent to Spot");
    }

    @Test
    @DisplayName("FM-CER-59 both forms carry the FechaLiquidacion of the rescate (a later or an earlier one than the system date, unchanged); decimals of the amount are kept")
    void fmCer59_fechas() throws Exception {
        // FMM-CER-535
        CasoCerT3.Corrida c535 = corre("spot-la-fecha-de-liquidacion-de-las-dos-formas-es-la-del-rescate");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-09", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-09"), spotFormas(c535), "spot-la-fecha-de-liquidacion-de-las-dos-formas-es-la-del-rescate :: payment forms sent to Spot");
        // FMM-CER-536
        CasoCerT3.Corrida c536 = corre("spot-la-fecha-de-liquidacion-anterior-al-sistema-se-envia-tal-cual");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-01", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-01"), spotFormas(c536), "spot-la-fecha-de-liquidacion-anterior-al-sistema-se-envia-tal-cual :: payment forms sent to Spot");
        // FMM-CER-537
        CasoCerT3.Corrida c537 = corre("spot-la-forma-que-recibe-lleva-el-monto-del-rescate-con-decimales");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780329|N|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780329|N|2026-03-03"), spotFormas(c537), "spot-la-forma-que-recibe-lleva-el-monto-del-rescate-con-decimales :: payment forms sent to Spot");
        assertEquals(List.of("3001|1780329|CIN|S"), spotOperaciones(c537), "spot-la-forma-que-recibe-lleva-el-monto-del-rescate-con-decimales :: operations sent to Spot");
    }

    @Test
    @DisplayName("FM-CER-59 two rescates go in ascending id with two forms each; a rescate with Spot and another without call Spot only with the first; with Tesoreria each rescate goes to its own collaborator")
    void fmCer59_variosRescates() throws Exception {
        // FMM-CER-538
        CasoCerT3.Corrida c538 = corre("spot-dos-rescates-llegan-por-id-con-sus-dos-formas-cada-uno");
        assertEquals(List.of("3001|1780000|CIN|S", "3002|1780000|TRA|S"), spotOperaciones(c538), "spot-dos-rescates-llegan-por-id-con-sus-dos-formas-cada-uno :: operations sent to Spot");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|1780000|N|2026-03-03", "3002|R|MOVEGR|COMSPT_CIN_USD_0|USD|1780000|N|2026-03-03", "3002|P|MOVING|VENSPT_TRA_CLP_0|CLP|1780000|S|2026-03-03"), spotFormas(c538), "spot-dos-rescates-llegan-por-id-con-sus-dos-formas-cada-uno :: payment forms sent to Spot");
        // FMM-CER-539
        CasoCerT3.Corrida c539 = corre("spot-un-rescate-con-spot-y-otro-sin-llama-solo-con-el-primero");
        assertEquals(List.of("3001|1780000|CIN|S"), spotOperaciones(c539), "spot-un-rescate-con-spot-y-otro-sin-llama-solo-con-el-primero :: operations sent to Spot");
        // FMM-CER-540
        CasoCerT3.Corrida c540 = corre("spot-junto-a-tesoreria-cada-rescate-va-a-su-colaborador");
        assertEquals(List.of("3001|1780000|CIN|S"), spotOperaciones(c540), "spot-junto-a-tesoreria-cada-rescate-va-a-su-colaborador :: operations sent to Spot");
        assertEquals(1, llamadas(c540, "ProcesarIngresoEgresoTesoreria").size(), "spot-junto-a-tesoreria-cada-rescate-va-a-su-colaborador :: calls to ProcesarIngresoEgresoTesoreria");
    }

    @Test
    @DisplayName("FM-CER-59 in a USD fund the `recibe` form carries the amount of the rescate (5.524,86 USD) and the `paga` form the amount in system currency (5.000.000 CLP): they are NOT the same field")
    void fmCer59_montosEnFondoUsd() throws Exception {
        // FMM-CER-574
        CasoCerT3.Corrida c574 = corre("spot-fondo-usd-el-monto-del-rescate-y-el-de-moneda-de-sistema-difieren");
        assertEquals(List.of("3001|5524.86|CIN|S"), spotOperaciones(c574), "spot-fondo-usd-el-monto-del-rescate-y-el-de-moneda-de-sistema-difieren :: operations sent to Spot");
        assertEquals(List.of("3001|R|MOVEGR|COMSPT_CIN_CLP_0|CLP|5524.86|S|2026-03-03", "3001|P|MOVING|VENSPT_CIN_CLP_0|CLP|5000000|N|2026-03-03"), spotFormas(c574), "spot-fondo-usd-el-monto-del-rescate-y-el-de-moneda-de-sistema-difieren :: payment forms sent to Spot");
    }

    @Test
    @DisplayName("FM-CER-59 with the parameter N the rescate goes to Tesoreria, not to Spot")
    void fmCer59_sinSpot() throws Exception {
        // FMM-CER-541
        CasoCerT3.Corrida c541 = corre("spot-parametro-N-no-genera-spot-y-el-rescate-va-a-tesoreria");
        assertEquals(0, llamadas(c541, "ProcesarIngresoOperacionSpot").size(), "spot-parametro-N-no-genera-spot-y-el-rescate-va-a-tesoreria :: calls to ProcesarIngresoOperacionSpot");
        assertEquals(1, llamadas(c541, "ProcesarIngresoEgresoTesoreria").size(), "spot-parametro-N-no-genera-spot-y-el-rescate-va-a-tesoreria :: calls to ProcesarIngresoEgresoTesoreria");
    }

    @Test
    @DisplayName("FM-CER-59 / D-F3B-01 Spot receives CalculaMargen S, PermiteOperarProspectos N and the UsaReglasNegocio of the request in simulation S")
    void fmCer59_simulacion() throws Exception {
        // FMM-CER-542
        CasoCerT3.Corrida c542 = corre("spot-simulacion-S-recibe-calcula-margen-S-y-las-reglas-de-negocio-de-la-entrada");
        assertEquals("S|S|N|1|7|FMM", cabecera(c542, "ProcesarIngresoOperacionSpot", 0), "spot-simulacion-S-recibe-calcula-margen-S-y-las-reglas-de-negocio-de-la-entrada :: head of the call to ProcesarIngresoOperacionSpot");
        assertEquals(1, llamadas(c542, "ProcesarIngresoOperacionSpot").size(), "spot-simulacion-S-recibe-calcula-margen-S-y-las-reglas-de-negocio-de-la-entrada :: calls to ProcesarIngresoOperacionSpot");
    }

    @Test
    @DisplayName("FM-CER-59 a rescate rejected by custody, without cartera or by the cuenta is not sent to Spot; an ADV one goes with the adjusted amounts")
    void fmCer59_rechazados() throws Exception {
        // FMM-CER-543
        CasoCerT3.Corrida c543 = corre("spot-rescate-rechazado-por-custodia-no-se-envia");
        assertEquals(List.of("3002|1780000|CIN|S"), spotOperaciones(c543), "spot-rescate-rechazado-por-custodia-no-se-envia :: operations sent to Spot");
        assertEquals("3001=RZD:UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION;3002=OK", estados(c543), "spot-rescate-rechazado-por-custodia-no-se-envia :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-544
        CasoCerT3.Corrida c544 = corre("spot-rescate-sin-cartera-retirada-no-se-envia");
        assertEquals(List.of("3002|1780000|CIN|S"), spotOperaciones(c544), "spot-rescate-sin-cartera-retirada-no-se-envia :: operations sent to Spot");
        assertEquals("3001=RZD:UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO;3002=OK", estados(c544), "spot-rescate-sin-cartera-retirada-no-se-envia :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-545
        CasoCerT3.Corrida c545 = corre("spot-rescate-rechazado-por-la-cuenta-no-se-envia");
        assertEquals(List.of("3002|1780000|CIN|S"), spotOperaciones(c545), "spot-rescate-rechazado-por-la-cuenta-no-se-envia :: operations sent to Spot");
        assertEquals("3001=RZD:UI_ERROR_CUENTA_INVERSION;3002=OK", estados(c545), "spot-rescate-rechazado-por-la-cuenta-no-se-envia :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-546
        CasoCerT3.Corrida c546 = corre("spot-ADV-envia-los-montos-ajustados-de-custodia");
        assertEquals(List.of("3001|150004|CIN|S"), spotOperaciones(c546), "spot-ADV-envia-los-montos-ajustados-de-custodia :: operations sent to Spot");
    }

    @Test
    @DisplayName("FM-CER-59 / D-F3B-41 the Spot pojo copies IdPreferenciaPagoPortafolio into IdAutorizadoOrigen")
    void fmCer59_autorizadoOrigen() throws Exception {
        // FMM-CER-547
        CasoCerT3.Corrida c547 = corre("spot-el-pojo-copia-la-preferencia-de-pago-en-el-autorizado-de-origen");
        assertEquals(List.of("3001|1780000|CIN|S"), spotOperaciones(c547), "spot-el-pojo-copia-la-preferencia-de-pago-en-el-autorizado-de-origen :: operations sent to Spot");
    }

    @Test
    @DisplayName("FM-CER-59 without a rescate that generates Spot, or with an aporte in dollars, the collaborator is not called")
    void fmCer59_noLlamaSpot() throws Exception {
        // FMM-CER-548
        CasoCerT3.Corrida c548 = corre("spot-sin-rescates-con-spot-no-se-llama-al-colaborador");
        assertEquals(0, llamadas(c548, "ProcesarIngresoOperacionSpot").size(), "spot-sin-rescates-con-spot-no-se-llama-al-colaborador :: calls to ProcesarIngresoOperacionSpot");
        // FMM-CER-549
        CasoCerT3.Corrida c549 = corre("spot-un-aporte-no-genera-spot");
        assertEquals(0, llamadas(c549, "ProcesarIngresoOperacionSpot").size(), "spot-un-aporte-no-genera-spot :: calls to ProcesarIngresoOperacionSpot");
    }

    @Test
    @DisplayName("FM-CER-60 the operation ids go to the solicitud (UPDATE IdOperacionSpot); Estado RECH rejects with the code ERROR_CREAR_SPOT; an unknown IdInterno is ignored; `rech` in lowercase does not reject; nothing in the answer changes nothing")
    void fmCer60_operaciones() throws Exception {
        // FMM-CER-550
        CasoCerT3.Corrida c550 = corre("spot-resultado-ING-asigna-la-operacion-spot-a-la-solicitud");
        assertEquals("3001=0/8801;3002=0/8802", filas(c550), "spot-resultado-ING-asigna-la-operacion-spot-a-la-solicitud :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        assertEquals("3001=OK;3002=OK", estados(c550), "spot-resultado-ING-asigna-la-operacion-spot-a-la-solicitud :: state of every solicitud (OK = pending, certified by the end)");
        // FMM-CER-551
        CasoCerT3.Corrida c551 = corre("spot-resultado-RECH-rechaza-con-error-crear-spot-y-deja-los-efectos-previos");
        assertEquals("3001=RZD:ERROR_CREAR_SPOT;3002=OK", estados(c551), "spot-resultado-RECH-rechaza-con-error-crear-spot-y-deja-los-efectos-previos :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3002=0/8802", filas(c551), "spot-resultado-RECH-rechaza-con-error-crear-spot-y-deja-los-efectos-previos :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        assertEquals(1, llamadas(c551, "ProcesarCustodiaFondoMutuo").size(), "spot-resultado-RECH-rechaza-con-error-crear-spot-y-deja-los-efectos-previos :: calls to ProcesarCustodiaFondoMutuo");
        assertEquals(1, llamadas(c551, "CrearCargoAbono").size(), "spot-resultado-RECH-rechaza-con-error-crear-spot-y-deja-los-efectos-previos :: calls to CrearCargoAbono");
        // FMM-CER-552
        CasoCerT3.Corrida c552 = corre("spot-resultado-con-id-interno-sin-rescate-se-ignora");
        assertEquals("3001=OK", estados(c552), "spot-resultado-con-id-interno-sin-rescate-se-ignora :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3001=0/0", filas(c552), "spot-resultado-con-id-interno-sin-rescate-se-ignora :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-555
        CasoCerT3.Corrida c555 = corre("spot-resultado-estado-rech-en-minuscula-no-rechaza-por-equals");
        assertEquals("3001=OK", estados(c555), "spot-resultado-estado-rech-en-minuscula-no-rechaza-por-equals :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3001=0/8801", filas(c555), "spot-resultado-estado-rech-en-minuscula-no-rechaza-por-equals :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-558
        CasoCerT3.Corrida c558 = corre("spot-resultado-sin-operaciones-ni-mensajes-no-cambia-nada");
        assertEquals("3001=OK", estados(c558), "spot-resultado-sin-operaciones-ni-mensajes-no-cambia-nada :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3001=0/0", filas(c558), "spot-resultado-sin-operaciones-ni-mensajes-no-cambia-nada :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
    }

    @Test
    @DisplayName("FM-CER-60 / D-F3B-42 the reading does not skip the solicitud already rejected: RECH then ING leaves it RZD (the operation id would be written on the rejected one) and ING then RECH leaves it RZD")
    void fmCer60_noSeSaltaLaRechazada() throws Exception {
        // FMM-CER-556
        CasoCerT3.Corrida c556 = corre("spot-resultado-RECH-y-luego-ING-de-la-misma-solicitud-deja-la-operacion-en-la-rechazada");
        assertEquals("3001=RZD:ERROR_CREAR_SPOT", estados(c556), "spot-resultado-RECH-y-luego-ING-de-la-misma-solicitud-deja-la-operacion-en-la-rechazada :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("", filas(c556), "spot-resultado-RECH-y-luego-ING-de-la-misma-solicitud-deja-la-operacion-en-la-rechazada :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-557
        CasoCerT3.Corrida c557 = corre("spot-resultado-ING-y-luego-RECH-de-la-misma-solicitud-la-deja-rechazada");
        assertEquals("3001=RZD:ERROR_CREAR_SPOT", estados(c557), "spot-resultado-ING-y-luego-RECH-de-la-misma-solicitud-la-deja-rechazada :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("", filas(c557), "spot-resultado-ING-y-luego-RECH-de-la-misma-solicitud-la-deja-rechazada :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
    }

    @Test
    @DisplayName("FM-CER-60 / D-NOR-9 an IdInterno that is not a number (or is null) raises NumberFormatException after the call")
    void fmCer60_idNoNumerico() throws Exception {
        // FMM-CER-553
        CasoCerT3.Corrida c553 = lanza("spot-resultado-con-id-interno-no-numerico-lanza-NumberFormatException");
        assertEquals("NumberFormatException", c553.excepcion.getClass().getSimpleName(), "spot-resultado-con-id-interno-no-numerico-lanza-NumberFormatException");
        assertEquals(1, llamadas(c553, "ProcesarIngresoOperacionSpot").size(), "spot-resultado-con-id-interno-no-numerico-lanza-NumberFormatException :: calls to ProcesarIngresoOperacionSpot");
        // FMM-CER-554
        CasoCerT3.Corrida c554 = lanza("spot-resultado-con-id-interno-nulo-lanza-NumberFormatException");
        assertEquals("NumberFormatException", c554.excepcion.getClass().getSimpleName(), "spot-resultado-con-id-interno-nulo-lanza-NumberFormatException");
        assertEquals(1, llamadas(c554, "ProcesarIngresoOperacionSpot").size(), "spot-resultado-con-id-interno-nulo-lanza-NumberFormatException :: calls to ProcesarIngresoOperacionSpot");
    }

    @Test
    @DisplayName("FM-CER-60 an ERROR message rejects the rescate with that id (also its absolute value) with the code and the description of the message, and the counter is summed; the message itself is NOT copied to the output; lowercase `error` also rejects (equalsIgnoreCase); other types or an unknown id do not")
    void fmCer60_mensajes() throws Exception {
        // FMM-CER-559
        CasoCerT3.Corrida c559 = corre("spot-mensaje-ERROR-con-id-del-rescate-lo-rechaza-con-su-codigo");
        assertEquals("3001=OK;3002=RZD:UI_ERROR_SPOT_GENERAL", estados(c559), "spot-mensaje-ERROR-con-id-del-rescate-lo-rechaza-con-su-codigo :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;", salida(c559), "spot-mensaje-ERROR-con-id-del-rescate-lo-rechaza-con-su-codigo :: counter and messages of the output");
        // FMM-CER-560
        CasoCerT3.Corrida c560 = corre("spot-mensaje-ERROR-con-id-negativo-usa-el-valor-absoluto");
        assertEquals("3001=OK;3002=RZD:UI_ERROR_SPOT_GENERAL", estados(c560), "spot-mensaje-ERROR-con-id-negativo-usa-el-valor-absoluto :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;", salida(c560), "spot-mensaje-ERROR-con-id-negativo-usa-el-valor-absoluto :: counter and messages of the output");
        // FMM-CER-564
        CasoCerT3.Corrida c564 = corre("spot-mensaje-ERROR-en-minuscula-rechaza-por-equalsignorecase");
        assertEquals("3001=RZD:UI_ERROR_SPOT_GENERAL", estados(c564), "spot-mensaje-ERROR-en-minuscula-rechaza-por-equalsignorecase :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;", salida(c564), "spot-mensaje-ERROR-en-minuscula-rechaza-por-equalsignorecase :: counter and messages of the output");
        // FMM-CER-565
        CasoCerT3.Corrida c565 = corre("spot-mensaje-que-no-es-ERROR-no-rechaza");
        assertEquals("3001=OK", estados(c565), "spot-mensaje-que-no-es-ERROR-no-rechaza :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=0;", salida(c565), "spot-mensaje-que-no-es-ERROR-no-rechaza :: counter and messages of the output");
        // FMM-CER-566
        CasoCerT3.Corrida c566 = corre("spot-mensaje-ERROR-con-id-de-un-rescate-ajeno-no-rechaza-a-nadie");
        assertEquals("3001=OK", estados(c566), "spot-mensaje-ERROR-con-id-de-un-rescate-ajeno-no-rechaza-a-nadie :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;", salida(c566), "spot-mensaje-ERROR-con-id-de-un-rescate-ajeno-no-rechaza-a-nadie :: counter and messages of the output");
    }

    @Test
    @DisplayName("FM-CER-60 / D-F3B-14 an ERROR with id 0 rejects the FIRST rescate of the list, even if it is not the one that generated the Spot (3001 normal, 3002 Spot: 3001 is rejected)")
    void fmCer60_id0() throws Exception {
        // FMM-CER-561
        CasoCerT3.Corrida c561 = corre("spot-mensaje-ERROR-con-id-cero-rechaza-al-primero-de-la-lista-aunque-no-sea-el-culpable");
        assertEquals("3001=RZD:UI_ERROR_SPOT_GENERAL;3002=OK", estados(c561), "spot-mensaje-ERROR-con-id-cero-rechaza-al-primero-de-la-lista-aunque-no-sea-el-culpable :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;", salida(c561), "spot-mensaje-ERROR-con-id-cero-rechaza-al-primero-de-la-lista-aunque-no-sea-el-culpable :: counter and messages of the output");
        // FMM-CER-562
        CasoCerT3.Corrida c562 = corre("spot-mensaje-ERROR-con-id-cero-y-un-solo-rescate-lo-rechaza");
        assertEquals("3001=RZD:UI_ERROR_SPOT_GENERAL", estados(c562), "spot-mensaje-ERROR-con-id-cero-y-un-solo-rescate-lo-rechaza :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;", salida(c562), "spot-mensaje-ERROR-con-id-cero-y-un-solo-rescate-lo-rechaza :: counter and messages of the output");
        // FMM-CER-563
        CasoCerT3.Corrida c563 = corre("spot-mensaje-ERROR-con-id-cero-rechaza-al-primer-rescate-de-la-lista-que-no-genera-spot");
        assertEquals("3001=RZD:UI_ERROR_SPOT_GENERAL;3002=OK", estados(c563), "spot-mensaje-ERROR-con-id-cero-rechaza-al-primer-rescate-de-la-lista-que-no-genera-spot :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("contador=1;", salida(c563), "spot-mensaje-ERROR-con-id-cero-rechaza-al-primer-rescate-de-la-lista-que-no-genera-spot :: counter and messages of the output");
    }

    @Test
    @DisplayName("FM-CER-60 / D-NOR-9 an ERROR message without id raises NullPointerException")
    void fmCer60_mensajeSinId() throws Exception {
        // FMM-CER-567
        CasoCerT3.Corrida c567 = lanza("spot-mensaje-ERROR-sin-id-lanza-NPE");
        assertEquals("NullPointerException", c567.excepcion.getClass().getSimpleName(), "spot-mensaje-ERROR-sin-id-lanza-NPE");
        assertEquals(1, llamadas(c567, "ProcesarIngresoOperacionSpot").size(), "spot-mensaje-ERROR-sin-id-lanza-NPE :: calls to ProcesarIngresoOperacionSpot");
    }

    @Test
    @DisplayName("FM-CER-60 the counter of Spot is added to the output only when the answer carries one")
    void fmCer60_contador() throws Exception {
        // FMM-CER-568
        CasoCerT3.Corrida c568 = corre("spot-el-contador-del-colaborador-se-suma-a-la-salida");
        assertEquals("contador=2;", salida(c568), "spot-el-contador-del-colaborador-se-suma-a-la-salida :: counter and messages of the output");
        // FMM-CER-569
        CasoCerT3.Corrida c569 = corre("spot-sin-contador-no-suma-nada");
        assertEquals("contador=0;", salida(c569), "spot-sin-contador-no-suma-nada :: counter and messages of the output");
    }

    @Test
    @DisplayName("FM-CER-60 an exception of Spot comes out untouched")
    void fmCer60_fallo() throws Exception {
        // FMM-CER-570
        CasoCerT3.Corrida c570 = lanza("spot-falla-y-propaga-la-excepcion");
        assertEquals("Exception: spot no disponible", CasoCer.texto(c570.excepcion), "spot-falla-y-propaga-la-excepcion");
        assertEquals(1, llamadas(c570, "ProcesarIngresoOperacionSpot").size(), "spot-falla-y-propaga-la-excepcion :: calls to ProcesarIngresoOperacionSpot");
    }

    @Test
    @DisplayName("FM-CER-60 / FM-CER-69 / D-F3B-03 a RECH of one Spot rescate leaves the other certified with its operation; the Tesoreria ingreso of the other stays; the custody and the cuenta of the rejected one were already written")
    void fmCer60_otrosRescates() throws Exception {
        // FMM-CER-571
        CasoCerT3.Corrida c571 = corre("spot-RECH-de-uno-y-el-otro-se-certifica-con-su-operacion");
        assertEquals("3001=RZD:ERROR_CREAR_SPOT;3002=OK", estados(c571), "spot-RECH-de-uno-y-el-otro-se-certifica-con-su-operacion :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3002=0/8802", filas(c571), "spot-RECH-de-uno-y-el-otro-se-certifica-con-su-operacion :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-572
        CasoCerT3.Corrida c572 = corre("spot-RECH-con-tesoreria-ok-del-otro-rescate-mantiene-sus-ingresos-egresos");
        assertEquals("3001=RZD:ERROR_CREAR_SPOT;3002=OK", estados(c572), "spot-RECH-con-tesoreria-ok-del-otro-rescate-mantiene-sus-ingresos-egresos :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals("3002=502/0", filas(c572), "spot-RECH-con-tesoreria-ok-del-otro-rescate-mantiene-sus-ingresos-egresos :: IdIngresoEgreso / IdOperacionSpot that the UPDATE writes");
        // FMM-CER-573
        CasoCerT3.Corrida c573 = corre("spot-RECH-deja-la-custodia-y-la-cuenta-ya-escritas");
        assertEquals("3001=RZD:ERROR_CREAR_SPOT", estados(c573), "spot-RECH-deja-la-custodia-y-la-cuenta-ya-escritas :: state of every solicitud (OK = pending, certified by the end)");
        assertEquals(1, llamadas(c573, "ProcesarCustodiaFondoMutuo").size(), "spot-RECH-deja-la-custodia-y-la-cuenta-ya-escritas :: calls to ProcesarCustodiaFondoMutuo");
        assertEquals(1, llamadas(c573, "CrearCargoAbono").size(), "spot-RECH-deja-la-custodia-y-la-cuenta-ya-escritas :: calls to CrearCargoAbono");
    }
}
