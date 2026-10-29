package cl.bcs.fondomutuo;

import cl.bcs.fondomutuo.domain.CalcularSpreadRescateEntrada;
import cl.bcs.fondomutuo.domain.CalcularSpreadRescateEntrada.DatoSpread;
import cl.bcs.fondomutuo.domain.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.service.cuota.CalcularMontoNumeroCuotaService;
import cl.bcs.fondomutuo.service.spread.CalcularSpreadRescateService;
import cl.bcs.fondomutuo.service.util.FondoMutuoUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BcsFondoMutuoServicios.
 *
 * Methodology: trace-based equivalence — the legacy code is the oracle.
 * Each test inlines the formula extracted directly from the legacy source,
 * cites the file:line, and asserts the exact output produced by that formula.
 * No Spring context, no SAP HANA connection, no production services are called.
 *
 * Tests requiring HANA, paridad or precios-tasas services are @Disabled.
 *
 * Run: mvn test -pl modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios
 */
class FondoMutuoTest {

    // =========================================================================
    // B-FM-CUOTA-01: Monto → Cuotas (same currency)
    // Source: CalcularMontoNumeroCuota.java — HALF_DOWN division
    // =========================================================================

    /**
     * B-FM-CUOTA-01: monto=100000, valorCuota=1234.56789, decimales=6
     * Expected: 100000 / 1234.56789 = 80.99983... → HALF_DOWN scale 6
     */
    @Test
    void rule_cuota_01_montoDivididoCuota_retornaNumeroCuotas() {
        BigDecimal monto = new BigDecimal("100000");
        BigDecimal valorCuota = new BigDecimal("1234.56789");
        int decimales = 6;

        BigDecimal result = CalcularMontoNumeroCuotaService.calcularNumeroCuotasDesdeMonto(
                monto, valorCuota, decimales);

        // Formula from CalcularMontoNumeroCuota.java: monto.divide(valorCuota, DECIMAL128).setScale(decimales, HALF_DOWN)
        BigDecimal expected = monto.divide(valorCuota, MathContext.DECIMAL128)
                                   .setScale(decimales, RoundingMode.HALF_DOWN);
        assertEquals(expected, result,
                "B-FM-CUOTA-01: monto/valorCuota HALF_DOWN scale 6");
    }

    /**
     * B-FM-CUOTA-01: exacto — monto=10000, valorCuota=100, expected=100.000000
     */
    @Test
    void rule_cuota_01_divisionExacta() {
        BigDecimal result = CalcularMontoNumeroCuotaService.calcularNumeroCuotasDesdeMonto(
                new BigDecimal("10000"), new BigDecimal("100"), 6);
        assertEquals(new BigDecimal("100.000000"), result,
                "B-FM-CUOTA-01: exact division 10000/100");
    }

    /**
     * B-FM-CUOTA-01: valorCuota=0 must throw IAE
     */
    @Test
    void rule_cuota_01_valorCuotaCero_lanzaIAE() {
        assertThrows(IllegalArgumentException.class, () ->
                CalcularMontoNumeroCuotaService.calcularNumeroCuotasDesdeMonto(
                        new BigDecimal("10000"), BigDecimal.ZERO, 4),
                "B-FM-CUOTA-01: zero valorCuota must throw IAE");
    }

    // =========================================================================
    // B-FM-CUOTA-02: Cuotas → Monto
    // Source: CalcularMontoNumeroCuota.java — MontoPorCuota = valorCuota * numeroCuotas
    // =========================================================================

    /**
     * B-FM-CUOTA-02: cuotas=50, valorCuota=2000.50 → monto=100025.00
     */
    @Test
    void rule_cuota_02_cuotasMultiplicadas_retornaMonto() {
        BigDecimal cuotas = new BigDecimal("50");
        BigDecimal valorCuota = new BigDecimal("2000.50");

        BigDecimal result = CalcularMontoNumeroCuotaService.calcularMontoDesdeCuotas(cuotas, valorCuota);

        BigDecimal expected = valorCuota.multiply(cuotas, MathContext.DECIMAL128);
        assertEquals(expected, result,
                "B-FM-CUOTA-02: 50 * 2000.50 = 100025.00");
    }

    // =========================================================================
    // B-FM-CUOTA-03: RESREINV special — extra decimal place HALF_UP
    // Source: CalcularMontoNumeroCuota.java — RESREINV parallelStream branch
    // =========================================================================

    /**
     * B-FM-CUOTA-03: RESREINV con monto=999.99, valorCuota=100, decimales=4 → scale=5, HALF_UP
     */
    @Test
    void rule_cuota_03_resreinv_decimalesExtras_halfUp() {
        BigDecimal monto = new BigDecimal("999.99");
        BigDecimal valorCuota = new BigDecimal("100");
        int decimalesCuota = 4;

        BigDecimal result = CalcularMontoNumeroCuotaService.calcularNumeroCuotasResreinv(
                monto, valorCuota, decimalesCuota);

        // Formula: monto / valorCuota, scale=decimalesCuota+1, HALF_UP
        BigDecimal expected = monto.divide(valorCuota, MathContext.DECIMAL128)
                                   .setScale(decimalesCuota + 1, RoundingMode.HALF_UP);
        assertEquals(expected, result,
                "B-FM-CUOTA-03: RESREINV uses decimalesCuota+1 with HALF_UP");
    }

    // =========================================================================
    // B-FM-CUOTA-05: Detección de conversión de moneda
    // Source: CalcularMontoNumeroCuota.java — diferido = S when monedas differ
    // =========================================================================

    @Test
    void rule_cuota_05_mismaMoneda_noRequiereConversion() {
        assertFalse(CalcularMontoNumeroCuotaService.requiereConversionMoneda("CLP", "CLP"),
                "B-FM-CUOTA-05: same currency → no conversion");
    }

    @Test
    void rule_cuota_05_distintaMoneda_requiereConversion() {
        assertTrue(CalcularMontoNumeroCuotaService.requiereConversionMoneda("CLP", "USD"),
                "B-FM-CUOTA-05: different currencies → requires conversion");
    }

    @Test
    void rule_cuota_05_caseInsensitive() {
        assertFalse(CalcularMontoNumeroCuotaService.requiereConversionMoneda("clp", "CLP"),
                "B-FM-CUOTA-05: case-insensitive comparison");
    }

    // =========================================================================
    // B-FM-CUOTA-06: Selección de tipo de cambio según tipo de solicitud
    // Source: CalcularMontoNumeroCuota.java — APO → ValorDolarAporte, else → ValorDolarRescate
    // =========================================================================

    @Test
    void rule_cuota_06_tipoAporte_usaValorDolarAporte() {
        BigDecimal aporte = new BigDecimal("900");
        BigDecimal rescate = new BigDecimal("850");
        BigDecimal result = CalcularMontoNumeroCuotaService.seleccionarTipoCambio("APO", aporte, rescate);
        assertEquals(aporte, result, "B-FM-CUOTA-06: APO uses ValorDolarAporte");
    }

    @Test
    void rule_cuota_06_tipoRescate_usaValorDolarRescate() {
        BigDecimal aporte = new BigDecimal("900");
        BigDecimal rescate = new BigDecimal("850");
        BigDecimal result = CalcularMontoNumeroCuotaService.seleccionarTipoCambio("RES", aporte, rescate);
        assertEquals(rescate, result, "B-FM-CUOTA-06: RES uses ValorDolarRescate");
    }

    @Test
    void rule_cuota_06_tipoAporte_caseInsensitive() {
        BigDecimal aporte = new BigDecimal("900");
        BigDecimal rescate = new BigDecimal("850");
        BigDecimal result = CalcularMontoNumeroCuotaService.seleccionarTipoCambio("apo", aporte, rescate);
        assertEquals(aporte, result, "B-FM-CUOTA-06: 'apo' (lowercase) uses ValorDolarAporte");
    }

    // =========================================================================
    // B-FM-SPREAD-01: Calcular montoReajuste
    // Source: CalcularSpreadRescate.java — MontoReajuste = valorFactorReajuste * costoFactorReajusteRescate
    // =========================================================================

    /**
     * B-FM-SPREAD-01: valorFactor=1.05, costoFactor=1000 → montoReajuste=1050.00000000
     */
    @Test
    void rule_spread_01_montoReajuste_calculado_correctamente() {
        BigDecimal valorFactor = new BigDecimal("1.05");
        BigDecimal costoFactor = new BigDecimal("1000");

        BigDecimal result = CalcularSpreadRescateService.calcularMontoReajuste(valorFactor, costoFactor);

        // Formula: valorFactor * costoFactor, scale=8 HALF_DOWN
        BigDecimal expected = valorFactor.multiply(costoFactor, MathContext.DECIMAL128)
                                         .setScale(8, RoundingMode.HALF_DOWN);
        assertEquals(expected, result, "B-FM-SPREAD-01: montoReajuste = 1.05 * 1000 = 1050");
    }

    /**
     * B-FM-SPREAD-01: Scale is exactly 8 decimals
     */
    @Test
    void rule_spread_01_escala_8decimales() {
        BigDecimal result = CalcularSpreadRescateService.calcularMontoReajuste(
                new BigDecimal("1.123456789"), new BigDecimal("10"));
        assertEquals(8, result.scale(), "B-FM-SPREAD-01: result must have 8 decimal places");
    }

    // =========================================================================
    // B-FM-SPREAD-02: Spread (misma moneda — monedaFondo == monedaSistema)
    // Source: CalcularSpreadRescate.java — (cantidadRescate * valorCuota) - montoReajusteConvertido
    // =========================================================================

    /**
     * B-FM-SPREAD-02: cantidad=100, valorCuota=50, montoReajuste=4000 → spread=1000
     */
    @Test
    void rule_spread_02_spreadMismaMoneda_calculado() {
        BigDecimal cantidad = new BigDecimal("100");
        BigDecimal valorCuota = new BigDecimal("50");
        BigDecimal montoReajuste = new BigDecimal("4000");

        BigDecimal result = CalcularSpreadRescateService.calcularSpreadMismaMoneda(
                cantidad, valorCuota, montoReajuste);

        // Formula: (cantidad * valorCuota) - montoReajuste, scale=8 HALF_DOWN
        // (100 * 50) - 4000 = 1000
        BigDecimal expected = cantidad.multiply(valorCuota, MathContext.DECIMAL128)
                                      .subtract(montoReajuste, MathContext.DECIMAL128)
                                      .setScale(8, RoundingMode.HALF_DOWN);
        assertEquals(expected, result, "B-FM-SPREAD-02: spread = (100*50) - 4000 = 1000");
    }

    /**
     * B-FM-SPREAD-02: spread can be negative (rescate below reajuste floor)
     */
    @Test
    void rule_spread_02_spreadNegativo_posible() {
        BigDecimal result = CalcularSpreadRescateService.calcularSpreadMismaMoneda(
                new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("5000"));
        // (10*100) - 5000 = -4000
        assertTrue(result.compareTo(BigDecimal.ZERO) < 0,
                "B-FM-SPREAD-02: spread can be negative when reajuste > patriomonio");
    }

    // =========================================================================
    // B-FM-SPREAD-04: Validación de datos de entrada
    // Source: CalcularSpreadRescate.java — validation guards
    // =========================================================================

    @Test
    void rule_spread_04_valorFactorCero_retornaError() {
        DatoSpread dato = new DatoSpread();
        dato.setValorFactorReajuste(BigDecimal.ZERO);
        dato.setCostoFactorReajusteRescate(new BigDecimal("100"));
        dato.setCantidadRescate(new BigDecimal("50"));
        dato.setValorCuota(new BigDecimal("200"));

        String error = CalcularSpreadRescateService.validarDato(dato);
        assertEquals("VALOR_FACTOR_MAYOR_CERO", error,
                "B-FM-SPREAD-04: zero valorFactorReajuste → VALOR_FACTOR_MAYOR_CERO");
    }

    @Test
    void rule_spread_04_valorFactorPositivo_retornaNulo() {
        DatoSpread dato = new DatoSpread();
        dato.setValorFactorReajuste(new BigDecimal("1.05"));
        dato.setCostoFactorReajusteRescate(new BigDecimal("100"));
        dato.setCantidadRescate(new BigDecimal("50"));
        dato.setValorCuota(new BigDecimal("200"));

        String error = CalcularSpreadRescateService.validarDato(dato);
        assertNull(error, "B-FM-SPREAD-04: all positive values → no error");
    }

    @Test
    void rule_spread_04_valorCuotaCero_retornaError() {
        DatoSpread dato = new DatoSpread();
        dato.setValorFactorReajuste(new BigDecimal("1.05"));
        dato.setCostoFactorReajusteRescate(new BigDecimal("100"));
        dato.setCantidadRescate(new BigDecimal("50"));
        dato.setValorCuota(BigDecimal.ZERO);

        String error = CalcularSpreadRescateService.validarDato(dato);
        assertEquals("VALOR_CUOTA_MAYOR_CERO", error,
                "B-FM-SPREAD-04: zero valorCuota → VALOR_CUOTA_MAYOR_CERO");
    }

    @Test
    void rule_spread_04_cantidadRescateNegativa_retornaError() {
        DatoSpread dato = new DatoSpread();
        dato.setValorFactorReajuste(new BigDecimal("1.05"));
        dato.setCostoFactorReajusteRescate(new BigDecimal("100"));
        dato.setCantidadRescate(new BigDecimal("-1"));
        dato.setValorCuota(new BigDecimal("200"));

        String error = CalcularSpreadRescateService.validarDato(dato);
        assertEquals("CANTIDAD_RESCATE_MAYOR_CERO", error,
                "B-FM-SPREAD-04: negative cantidadRescate → CANTIDAD_RESCATE_MAYOR_CERO");
    }

    // =========================================================================
    // B-FM-UTIL-01: esFlujoCustodiaParaSolicitudProceso
    // Source: FondoMutuoUtils.java
    // =========================================================================

    @Test
    void rule_util_01_flujoMatchSolicitud_mismoPortafolioInstrumentoYRegimen() {
        boolean result = FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(
                1L, 100L, "APV", 1L, 100L, "APV");
        assertTrue(result, "B-FM-UTIL-01: matching portafolio, instrumento, regimen → true");
    }

    @Test
    void rule_util_01_flujoMatchSolicitud_regimenVacio() {
        // When solicitud regimen is empty, any flujo regimen matches
        boolean result = FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(
                1L, 100L, "APV", 1L, 100L, "");
        assertTrue(result, "B-FM-UTIL-01: empty solicitud regimen matches any flujo regimen");
    }

    @Test
    void rule_util_01_flujoNoMatchSolicitud_distintoPortafolio() {
        boolean result = FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(
                1L, 100L, "APV", 2L, 100L, "APV");
        assertFalse(result, "B-FM-UTIL-01: different portafolio → false");
    }

    @Test
    void rule_util_01_flujoNoMatchSolicitud_distintoRegimen() {
        boolean result = FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(
                1L, 100L, "GENERAL", 1L, 100L, "APV");
        assertFalse(result, "B-FM-UTIL-01: different regimen → false");
    }

    // =========================================================================
    // B-FM-UTIL-03 / B-FM-UTIL-04: Canal ingreso web services
    // Source: FondoMutuoUtils.java
    // =========================================================================

    @Test
    void rule_util_03_canalWeb_esWebServices() {
        assertTrue(FondoMutuoUtils.esCanalIngresoWebServices("WEB"),
                "B-FM-UTIL-03: WEB → esCanalIngresoWebServices = true");
    }

    @Test
    void rule_util_03_canalEjecutivo_esWebServices() {
        assertTrue(FondoMutuoUtils.esCanalIngresoWebServices("EJE"),
                "B-FM-UTIL-03: EJE → esCanalIngresoWebServices = true");
    }

    @Test
    void rule_util_03_canalAgente_noEsWebServices() {
        assertFalse(FondoMutuoUtils.esCanalIngresoWebServices("AGE"),
                "B-FM-UTIL-03: AGE → esCanalIngresoWebServices = false");
    }

    @Test
    void rule_util_04_canalOperador_esDistintoAWebServices() {
        assertTrue(FondoMutuoUtils.esCanalIngresoDistintoAWebServices("OPE"),
                "B-FM-UTIL-04: OPE → esCanalIngresoDistintoAWebServices = true");
    }

    @Test
    void rule_util_04_canalWeb_noEsDistintoAWebServices() {
        assertFalse(FondoMutuoUtils.esCanalIngresoDistintoAWebServices("WEB"),
                "B-FM-UTIL-04: WEB → esCanalIngresoDistintoAWebServices = false");
    }

    // =========================================================================
    // B-FM-UTIL-02: esRegimenTributarioVacio
    // Source: FondoMutuoUtils.java
    // =========================================================================

    @Test
    void rule_util_02_null_esVacio() {
        assertTrue(FondoMutuoUtils.esRegimenTributarioVacio(null),
                "B-FM-UTIL-02: null → esRegimenTributarioVacio = true");
    }

    @Test
    void rule_util_02_empty_esVacio() {
        assertTrue(FondoMutuoUtils.esRegimenTributarioVacio(""),
                "B-FM-UTIL-02: empty string → esRegimenTributarioVacio = true");
    }

    @Test
    void rule_util_02_apv_noEsVacio() {
        assertFalse(FondoMutuoUtils.esRegimenTributarioVacio("APV"),
                "B-FM-UTIL-02: 'APV' → esRegimenTributarioVacio = false");
    }

    // =========================================================================
    // B-FM-UTIL-05: deriveEstadoCertificacion
    // Source: common pattern across solicitud services
    // =========================================================================

    @Test
    void rule_util_05_certAutoS_sinAutorizaciones_estadoCRT() {
        String estado = FondoMutuoUtils.deriveEstadoCertificacion("S", 0);
        assertEquals(ConstantesFondoMutuo.ESTADO_CERTIFICADO, estado,
                "B-FM-UTIL-05: certAuto=S, 0 autorizaciones → CRT");
    }

    @Test
    void rule_util_05_certAutoN_estadoPEN() {
        String estado = FondoMutuoUtils.deriveEstadoCertificacion("N", 0);
        assertEquals(ConstantesFondoMutuo.ESTADO_PENDIENTE, estado,
                "B-FM-UTIL-05: certAuto=N → PEN");
    }

    @Test
    void rule_util_05_certAutoS_conAutorizaciones_estadoPEN() {
        String estado = FondoMutuoUtils.deriveEstadoCertificacion("S", 2);
        assertEquals(ConstantesFondoMutuo.ESTADO_PENDIENTE, estado,
                "B-FM-UTIL-05: certAuto=S but 2 autorizaciones → PEN");
    }

    // =========================================================================
    // B-FM-CONST: Constants integrity
    // Source: ConstantesFondoMutuo.java
    // =========================================================================

    @Test
    void rule_const_tipoSolicitudAporte_esAPO() {
        assertEquals("APO", ConstantesFondoMutuo.TIPO_SOLICITUD_APORTE,
                "B-FM-CONST: TIPO_SOLICITUD_APORTE = APO");
    }

    @Test
    void rule_const_tipoSolicitudRescate_esRES() {
        assertEquals("RES", ConstantesFondoMutuo.TIPO_SOLICITUD_RESCATE,
                "B-FM-CONST: TIPO_SOLICITUD_RESCATE = RES");
    }

    @Test
    void rule_const_calculoPorMonto_esM() {
        assertEquals("M", ConstantesFondoMutuo.CALCULO_POR_MONTO,
                "B-FM-CONST: CALCULO_POR_MONTO = M");
    }

    @Test
    void rule_const_calculoPorCuota_esC() {
        assertEquals("C", ConstantesFondoMutuo.CALCULO_POR_CUOTA,
                "B-FM-CONST: CALCULO_POR_CUOTA = C");
    }

    @Test
    void rule_const_estadoIngresado_esING() {
        assertEquals("ING", ConstantesFondoMutuo.ESTADO_INGRESADO,
                "B-FM-CONST: ESTADO_INGRESADO = ING");
    }

    @Test
    void rule_const_reinversion_esRESREINV() {
        assertEquals("RESREINV", ConstantesFondoMutuo.REINVERSION,
                "B-FM-CONST: REINVERSION = RESREINV");
    }
}
