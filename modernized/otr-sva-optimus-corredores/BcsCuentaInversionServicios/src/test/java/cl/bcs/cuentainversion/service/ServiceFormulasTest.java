package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.domain.GoLiveRouting;
import cl.bcs.cuentainversion.port.out.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises actual production service methods (BLK-1 fix).
 * No Spring context or HANA required — only tests package-private formula methods.
 */
class ServiceFormulasTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static ConfiguracionSistemaPort configWithGoLive(LocalDate goLive) {
        return new ConfiguracionSistemaPort() {
            public String getConfiguracion(Long id, String k) { throw new UnsupportedOperationException(); }
            public boolean usaCargoAbonoJava(Long id) { throw new UnsupportedOperationException(); }
            public boolean permiteAnularMovimiento(Long id) { throw new UnsupportedOperationException(); }
            public boolean validaFlujoCajaEnCertificacion(Long id) { throw new UnsupportedOperationException(); }
            public LocalDate getFechaGoLive(Long id) { return goLive; }
        };
    }

    private static HanaSaldoPort nullHanaSaldo() {
        return new HanaSaldoPort() {
            public cl.bcs.cuentainversion.domain.SaldoInicialFinal consultarSaldoInicialFinal(cl.bcs.cuentainversion.domain.SaldoCuentaInversionEntrada e) { throw new UnsupportedOperationException(); }
            public List<cl.bcs.cuentainversion.domain.SaldoPorFechaEfectiva> consultarSaldoPorFechaEfectiva(cl.bcs.cuentainversion.domain.SaldoCuentaInversionEntrada e) { throw new UnsupportedOperationException(); }
            public List<cl.bcs.cuentainversion.domain.Movimiento> consultarMovimientos(Long id, LocalDate d, LocalDate h, boolean a) { throw new UnsupportedOperationException(); }
            public List<cl.bcs.cuentainversion.domain.Movimiento> consultarMovimientosHistoricos(Long id, LocalDate d, LocalDate h, boolean a) { throw new UnsupportedOperationException(); }
            public BigDecimal consultarSaldoPorFechaAnterior(Long id, LocalDate f) { throw new UnsupportedOperationException(); }
        };
    }

    private ConsultarMovimientoCuentaInversionService movimientoService(LocalDate goLive) {
        return new ConsultarMovimientoCuentaInversionService(nullHanaSaldo(), configWithGoLive(goLive), null);
    }

    private CrearCargoAbonoService crearService() {
        // Only testing formula methods that don't use jdbc/dataSource
        return new CrearCargoAbonoService(null, null, null, null, null, null, null);
    }

    private ConsultarSaldoCuentaInversionService saldoService() {
        return new ConsultarSaldoCuentaInversionService(null, null, null, null, null);
    }

    private IndiceRentabilidadAttributionAnalysisService indiceService() {
        return new IndiceRentabilidadAttributionAnalysisService(null);
    }

    private ActualizarSaldoCargoAbonoService actualizarService() {
        return new ActualizarSaldoCargoAbonoService(null);
    }

    // ── B-CTA-07: resolverRouting (production method) ────────────────────────

    @Test void routing_bothBeforeGoLive_yieldsHistoricalOnly() {
        var svc = movimientoService(LocalDate.of(2024, 1, 1));
        assertEquals(GoLiveRouting.HISTORICAL_ONLY,
                svc.resolverRouting(1L, LocalDate.of(2023, 6, 1), LocalDate.of(2023, 12, 31)));
    }

    @Test void routing_bothAfterGoLive_yieldsCurrentOnly() {
        var svc = movimientoService(LocalDate.of(2024, 1, 1));
        assertEquals(GoLiveRouting.CURRENT_ONLY,
                svc.resolverRouting(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30)));
    }

    @Test void routing_straddlesGoLive_yieldsHybrid() {
        var svc = movimientoService(LocalDate.of(2024, 1, 1));
        assertEquals(GoLiveRouting.HYBRID,
                svc.resolverRouting(1L, LocalDate.of(2023, 12, 1), LocalDate.of(2024, 6, 30)));
    }

    @Test void routing_fechaDesdeEqualsGoLive_yieldsCurrentOnly() {
        var svc = movimientoService(LocalDate.of(2024, 1, 1));
        assertEquals(GoLiveRouting.CURRENT_ONLY,
                svc.resolverRouting(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)));
    }

    // ── B-CTA-11: derivarOrigen (production method — now in service) ─────────

    @Test void derivarOrigen_apvPrefix_yieldsAPV() {
        assertEquals("APV", movimientoService(LocalDate.now()).derivarOrigen("APV12345"));
    }

    @Test void derivarOrigen_dcPrefix_yieldsAPV() {
        assertEquals("APV", movimientoService(LocalDate.now()).derivarOrigen("DC00099"));
    }

    @Test void derivarOrigen_otherPrefix_yieldsNOAPV() {
        assertEquals("NOAPV", movimientoService(LocalDate.now()).derivarOrigen("PENS001"));
    }

    @Test void derivarOrigen_apvExactMatch_yieldsAPV() {
        assertEquals("APV", movimientoService(LocalDate.now()).derivarOrigen("APV"));
    }

    // ── B-CTA-01b: esMontoNegativo (production method) ───────────────────────

    @Test void esMontoNegativo_negativeAmount_isTrue() {
        assertTrue(crearService().esMontoNegativo(new BigDecimal("-0.01")));
    }

    @Test void esMontoNegativo_zero_isFalse() {
        // B-CTA-01b: monto=0 does NOT trigger the guard (compareTo == -1, not <= 0)
        assertFalse(crearService().esMontoNegativo(BigDecimal.ZERO));
    }

    @Test void esMontoNegativo_positive_isFalse() {
        assertFalse(crearService().esMontoNegativo(new BigDecimal("100.00")));
    }

    // ── B-CTA-01c: idEstadoFormula (production method) ───────────────────────

    @Test void idEstado_certN_autorizaciones0_isPEN() {
        assertEquals("PEN", crearService().idEstadoFormula("N", 0));
    }

    @Test void idEstado_certN_autorizaciones0_caseInsensitive_isPEN() {
        assertEquals("PEN", crearService().idEstadoFormula("n", 0));
    }

    @Test void idEstado_certY_autorizaciones0_isCRT() {
        assertEquals("CRT", crearService().idEstadoFormula("Y", 0));
    }

    @Test void idEstado_autorizacionesPositivo_isPEN() {
        assertEquals("PEN", crearService().idEstadoFormula("Y", 1));
    }

    @Test void idEstado_certY_autorizaciones0_isCRT_notPEN() {
        assertNotEquals("PEN", crearService().idEstadoFormula("Y", 0));
    }

    // ── B-CTA-06: truncarCLP (production method) ─────────────────────────────

    @Test void truncarCLP_positiveDecimal_truncatesDown() {
        assertEquals(new BigDecimal("1234"), saldoService().truncarCLP(new BigDecimal("1234.99")));
    }

    @Test void truncarCLP_negativeDecimal_truncatesDown() {
        // RoundingMode.DOWN (toward zero) for negatives: -1234.99 → -1234 (not -1235)
        assertEquals(new BigDecimal("-1234"), saldoService().truncarCLP(new BigDecimal("-1234.99")));
    }

    @Test void truncarCLP_zero_isZero() {
        assertEquals(new BigDecimal("0"), saldoService().truncarCLP(BigDecimal.ZERO));
    }

    // ── B-CTA-06b: sumarMoneyMarket (production method) ──────────────────────

    @Test void sumarMoneyMarket_preservesMaxScale() {
        // 7505.0 (scale 1) + 5000.00 (scale 2) → 12505.00 (scale 2, not scale 1)
        BigDecimal result = saldoService().sumarMoneyMarket(
                new BigDecimal("7505.0"), new BigDecimal("5000.00"));
        assertEquals(2, result.scale());
        assertEquals("12505.00", result.toPlainString());
    }

    // ── B-CTA-12: calcularIndice (production method) ─────────────────────────

    @Test void calcularIndice_basicReturn() {
        // pf=110, pi=100 → (110/100 - 1) * 100 = 10.00%
        BigDecimal result = indiceService().calcularIndice(
                new BigDecimal("110"), new BigDecimal("100"));
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test void calcularIndice_fractionalReturn() {
        // pf=1050.75, pi=1000 → 5.08% (rounded HALF_UP from 5.075)
        BigDecimal result = indiceService().calcularIndice(
                new BigDecimal("1050.75"), new BigDecimal("1000"));
        assertEquals(new BigDecimal("5.08"), result);
    }

    // ── B-CTA-04: acumular formulas (production methods) ─────────────────────

    @Test void acumularSaldoEfectivo_usesDECIMAL128() {
        BigDecimal result = actualizarService().acumularSaldoEfectivo(
                new BigDecimal("1000.00"), new BigDecimal("500.50"));
        assertEquals(0, new BigDecimal("1500.50").compareTo(result));
    }

    @Test void acumularSaldoMovimiento_usesDECIMAL128() {
        BigDecimal result = actualizarService().acumularSaldoMovimiento(
                new BigDecimal("1000.00"), new BigDecimal("500.50"));
        assertEquals(0, new BigDecimal("1500.50").compareTo(result));
    }
}
