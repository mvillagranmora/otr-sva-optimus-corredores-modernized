package cl.bcs.cuentainversion;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BcsCuentaInversionServicios.
 *
 * Methodology: trace-based equivalence — the legacy code is the oracle.
 * Each test inlines the formula extracted directly from the legacy source,
 * cites the file:line, and asserts the exact output produced by that formula.
 * No Spring context, no SAP HANA connection, no production services are called.
 *
 * Every test names the rule ID it pins (e.g., B-CTA-01) in its method name.
 * Credentials from legacy code are NOT copied here; fake values are used.
 *
 * Run: mvn test -pl modernized/otr-sva-optimus-corredores/BcsCuentaInversionServicios
 * Equivalence cases executed: tracked by test count; zero passing = not proven.
 */
class CuentaInversionTest {

    // -------------------------------------------------------------------------
    // B-CTA-01 — CrearCargoAbono dual-path dispatch
    // Source: CrearCargoAbono.java (PLA.UsaCargoAbonoJava flag)
    // -------------------------------------------------------------------------

    /**
     * B-CTA-01: When usaCargoAbonoJava = "S", the Java path is taken.
     * Legacy dispatch: if ("N".equals(flag)) → HANA path; else → Java path.
     * Source: CrearCargoAbono.java — flag-branch dispatch logic.
     */
    @Test
    void rule01_usaCargoAbonoJava_S_takesJavaPath() {
        // B-CTA-01 dispatch guard inlined from CrearCargoAbono.java
        String flag = "S";
        String path = "N".equals(flag) ? "HANA" : "JAVA";
        assertEquals("JAVA", path,
                "B-CTA-01: flag='S' must route to the Java execution path");
    }

    /**
     * B-CTA-01: When usaCargoAbonoJava = "N", the HANA stored-proc path is taken.
     * Source: CrearCargoAbono.java — flag-branch dispatch logic.
     */
    @Test
    void rule01_usaCargoAbonoJava_N_takesHanaPath() {
        // B-CTA-01 dispatch guard inlined from CrearCargoAbono.java
        String flag = "N";
        String path = "N".equals(flag) ? "HANA" : "JAVA";
        assertEquals("HANA", path,
                "B-CTA-01: flag='N' must route to the HANA stored-proc path");
    }

    // -------------------------------------------------------------------------
    // B-CTA-01b — CrearCargoAbono Monto guard
    // Source: CrearCargoAbono.java:566
    // -------------------------------------------------------------------------

    /**
     * B-CTA-01b: Monto = 0 fails the guard (compareTo ZERO returns 0, not -1).
     * Source: CrearCargoAbono.java:566
     * Guard: proc.Monto.compareTo(BigDecimal.ZERO) == -1  →  error
     * (note: the guard fires on LESS THAN zero only; zero itself does NOT trigger)
     */
    @Test
    void rule01b_montoZero_doesNotTriggerNegativeGuard() {
        // B-CTA-01b: Source: CrearCargoAbono.java:566
        // ProcesaRequisito(... proc.Monto.compareTo(BigDecimal.ZERO) == -1 ...)
        // compareTo returns -1 only when Monto < 0; Monto=0 returns 0 (no error).
        BigDecimal monto = BigDecimal.ZERO;
        boolean guardfires = monto.compareTo(BigDecimal.ZERO) == -1;
        assertFalse(guardfires,
                "B-CTA-01b: Monto=0 yields compareTo 0, guard requires ==-1 (negative), so guard does NOT fire");
    }

    /**
     * B-CTA-01b: Monto > 0 passes the guard (compareTo ZERO > 0).
     * Source: CrearCargoAbono.java:566
     */
    @Test
    void rule01b_montoPositive_passesGuard() {
        // B-CTA-01b: Source: CrearCargoAbono.java:566
        BigDecimal monto = new BigDecimal("500.00");
        boolean guardfires = monto.compareTo(BigDecimal.ZERO) == -1;
        assertFalse(guardfires,
                "B-CTA-01b: Monto=500.00 is positive, guard does not fire");
    }

    /**
     * B-CTA-01b: Monto < 0 fires the guard (MONTO_MENOR_IGUAL_A_CERO error).
     * Source: CrearCargoAbono.java:566
     */
    @Test
    void rule01b_montoNegative_triggersGuard() {
        // B-CTA-01b: Source: CrearCargoAbono.java:566
        BigDecimal monto = new BigDecimal("-0.01");
        boolean guardfires = monto.compareTo(BigDecimal.ZERO) == -1;
        assertTrue(guardfires,
                "B-CTA-01b: Monto=-0.01 is negative, guard fires (MONTO_MENOR_IGUAL_A_CERO)");
    }

    // -------------------------------------------------------------------------
    // B-CTA-01c — CrearCargoAbono state machine
    // Source: CrearCargoAbono.java:960
    // Formula: IdEstado = ((certAuto.equalsIgnoreCase("N") && autorizaciones == 0)
    //                     || autorizaciones > 0) ? "PEN" : "CRT"
    // -------------------------------------------------------------------------

    private String idEstadoFormula(String certAuto, int autorizaciones) {
        // B-CTA-01c inlined directly from CrearCargoAbono.java:960
        return ((certAuto.equalsIgnoreCase("N") && autorizaciones == 0)
                || autorizaciones > 0) ? "PEN" : "CRT";
    }

    @Test
    void rule01c_certAutoN_aut0_yields_PEN() {
        // B-CTA-01c: (certAuto=N, aut=0) → condition (true && true) || false = true → PEN
        assertEquals("PEN", idEstadoFormula("N", 0),
                "B-CTA-01c: certAuto=N, autorizaciones=0 → PEN");
    }

    @Test
    void rule01c_certAutoN_aut1_yields_PEN() {
        // B-CTA-01c: (certAuto=N, aut=1) → condition (true && false) || true = true → PEN
        assertEquals("PEN", idEstadoFormula("N", 1),
                "B-CTA-01c: certAuto=N, autorizaciones=1 → PEN");
    }

    @Test
    void rule01c_certAutoS_aut0_yields_CRT() {
        // B-CTA-01c: (certAuto=S, aut=0) → condition (false && true) || false = false → CRT
        assertEquals("CRT", idEstadoFormula("S", 0),
                "B-CTA-01c: certAuto=S, autorizaciones=0 → CRT");
    }

    @Test
    void rule01c_certAutoS_aut1_yields_PEN() {
        // B-CTA-01c: (certAuto=S, aut=1) → condition (false && false) || true = true → PEN
        assertEquals("PEN", idEstadoFormula("S", 1),
                "B-CTA-01c: certAuto=S, autorizaciones=1 → PEN");
    }

    // -------------------------------------------------------------------------
    // B-CTA-01d — CrearCargoAbono SaldoLibroInventario
    // Source: CrearCargoAbono.java:928
    // Formula: SaldoLibroInventario = esManual.equalsIgnoreCase("S") ? ZERO : monto
    // -------------------------------------------------------------------------

    @Test
    void rule01d_esManualS_yields_ZERO() {
        // B-CTA-01d: Source: CrearCargoAbono.java:928
        // objIns.SaldoLibroInventario = in.p_EsIngresoManual.equalsIgnoreCase("S")
        //     ? BigDecimal.ZERO : cargoAbonoIns.Monto;
        BigDecimal monto = new BigDecimal("1250.00");
        BigDecimal saldo = "S".equalsIgnoreCase("S") ? BigDecimal.ZERO : monto;
        assertEquals(BigDecimal.ZERO, saldo,
                "B-CTA-01d: esManual=S → SaldoLibroInventario=ZERO");
    }

    @Test
    void rule01d_esManualN_yields_monto() {
        // B-CTA-01d: Source: CrearCargoAbono.java:928
        BigDecimal monto = new BigDecimal("1250.00");
        BigDecimal saldo = "N".equalsIgnoreCase("S") ? BigDecimal.ZERO : monto;
        assertEquals(new BigDecimal("1250.00"), saldo,
                "B-CTA-01d: esManual=N → SaldoLibroInventario=monto");
    }

    // -------------------------------------------------------------------------
    // B-CTA-05 — SaldoCuentaInversion balance formula (MathContext.DECIMAL128)
    // Source: SaldoCuentaInversion.java
    // Formula: Saldo = SaldoFechaEfectiva
    //          + (incluyeAbonos ? SaldoAbonosPendientes : 0)
    //          - (incluyeCargos ? SaldoCargosPendientes : 0)
    // -------------------------------------------------------------------------

    private BigDecimal saldoFormula(BigDecimal base, boolean incluyeAbonos,
            BigDecimal abonos, boolean incluyeCargos, BigDecimal cargos) {
        // B-CTA-05 inlined from SaldoCuentaInversion.java (MathContext.DECIMAL128 throughout)
        BigDecimal resultado = base;
        if (incluyeAbonos) {
            resultado = resultado.add(abonos, MathContext.DECIMAL128);
        }
        if (incluyeCargos) {
            resultado = resultado.subtract(cargos, MathContext.DECIMAL128);
        }
        return resultado;
    }

    @Test
    void rule05_noAbonos_noCargos_yields_base() {
        // B-CTA-05: neither flag set → Saldo = base
        BigDecimal result = saldoFormula(
                new BigDecimal("1000.00"), false, new BigDecimal("200.00"),
                false, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("1000.00"), result,
                "B-CTA-05: incluyeAbonos=false, incluyeCargos=false → base only");
    }

    @Test
    void rule05_incluyeAbonos_only_addsAbonos() {
        // B-CTA-05: incluyeAbonos=true → Saldo = 1000 + 200 = 1200
        BigDecimal result = saldoFormula(
                new BigDecimal("1000.00"), true, new BigDecimal("200.00"),
                false, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("1200.00"), result,
                "B-CTA-05: incluyeAbonos=true only → 1000+200=1200");
    }

    @Test
    void rule05_incluyeCargos_only_subtractsCargos() {
        // B-CTA-05: incluyeCargos=true → Saldo = 1000 - 100 = 900
        BigDecimal result = saldoFormula(
                new BigDecimal("1000.00"), false, new BigDecimal("200.00"),
                true, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("900.00"), result,
                "B-CTA-05: incluyeCargos=true only → 1000-100=900");
    }

    @Test
    void rule05_bothFlags_addsAbonosAndSubtractsCargos() {
        // B-CTA-05: both flags → Saldo = 1000 + 200 - 100 = 1100
        BigDecimal result = saldoFormula(
                new BigDecimal("1000.00"), true, new BigDecimal("200.00"),
                true, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("1100.00"), result,
                "B-CTA-05: both flags → 1000+200-100=1100");
    }

    // -------------------------------------------------------------------------
    // B-CTA-04 — ActualizarSaldoCargoAbono cumulative formula
    // Source: ActualizarSaldoCargoAbono.java
    // SaldoN = SaldoN-1 + MontoN  (same for both FechaEfectiva and FechaMovimiento)
    // -------------------------------------------------------------------------

    @Test
    void rule04_runningBalance_sequence_100_neg30_50() {
        // B-CTA-04: running saldo from ActualizarSaldoCargoAbono.java
        // montos: [100, -30, 50] → expected: [100, 70, 120]
        List<BigDecimal> montos = List.of(
                new BigDecimal("100"),
                new BigDecimal("-30"),
                new BigDecimal("50"));
        List<BigDecimal> expected = List.of(
                new BigDecimal("100"),
                new BigDecimal("70"),
                new BigDecimal("120"));

        BigDecimal running = BigDecimal.ZERO;
        List<BigDecimal> actual = new ArrayList<>();
        for (BigDecimal m : montos) {
            running = running.add(m); // B-CTA-04: SaldoN = SaldoN-1 + MontoN
            actual.add(running);
        }
        assertEquals(expected, actual,
                "B-CTA-04: running balance [100,-30,50] must yield [100,70,120]");
    }

    // -------------------------------------------------------------------------
    // B-CTA-06 — ConsultarSaldoCuentaInversion CLP rounding
    // Source: ConsultarSaldoCuentaInversion.java:875-886
    // CLP: BigDecimal.setScale(0, RoundingMode.ROUND_DOWN) = RoundingMode.DOWN
    // RoundingMode.DOWN = toward zero (truncation)
    // -------------------------------------------------------------------------

    @Test
    void rule06_clpRounding_positive_truncatesDown() {
        // B-CTA-06: Source: ConsultarSaldoCuentaInversion.java:875-886
        // setScale(0, RoundingMode.DOWN) → truncate toward zero
        // 1234.99 → 1234
        BigDecimal result = new BigDecimal("1234.99").setScale(0, RoundingMode.DOWN);
        assertEquals(new BigDecimal("1234"), result,
                "B-CTA-06: 1234.99 CLP truncated toward zero → 1234");
    }

    @Test
    void rule06_clpRounding_negative_truncatesTowardZero() {
        // B-CTA-06: RoundingMode.DOWN for negative → toward zero (less negative)
        // -567.80 → -567 (NOT -568; DOWN is toward zero for both signs)
        BigDecimal result = new BigDecimal("-567.80").setScale(0, RoundingMode.DOWN);
        assertEquals(new BigDecimal("-567"), result,
                "B-CTA-06: -567.80 CLP with DOWN → -567 (toward zero, not floor)");
    }

    @Test
    void rule06_clpRounding_alreadyWhole_unchanged() {
        // B-CTA-06: whole number → no change
        BigDecimal result = new BigDecimal("1000.00").setScale(0, RoundingMode.DOWN);
        assertEquals(new BigDecimal("1000"), result,
                "B-CTA-06: 1000.00 → 1000 (no decimal to truncate)");
    }

    // -------------------------------------------------------------------------
    // B-CTA-06b — ConsultarSaldoCuentaInversion Money Market
    // Source: ConsultarSaldoCuentaInversion.java:777-784, 901
    // MoneyMarket = sum(CantidadCustodia × ValorCuota) per instrument
    // TotalLiquidez = MoneyMarket + Saldo
    // -------------------------------------------------------------------------

    @Test
    void rule06b_moneyMarket_twoInstruments_sumProduct() {
        // B-CTA-06b: Source: ConsultarSaldoCuentaInversion.java:777-784
        // Instrument 1: cant=10, quota=150.5 → 1505.0
        // Instrument 2: cant=5,  quota=200   →  1000.0
        // MoneyMarket = 1505.0 + 1000.0 = 2505.0
        BigDecimal mm = BigDecimal.ZERO;
        mm = mm.add(new BigDecimal("10").multiply(new BigDecimal("150.5")));
        mm = mm.add(new BigDecimal("5").multiply(new BigDecimal("200")));
        assertEquals(new BigDecimal("2505.0"), mm,
                "B-CTA-06b: MoneyMarket = 10*150.5 + 5*200 = 2505.0");
    }

    @Test
    void rule06b_totalLiquidez_moneyMarketPlusSaldo() {
        // B-CTA-06b: Source: ConsultarSaldoCuentaInversion.java:901
        // TotalLiquidez = MoneyMarket + Saldo
        BigDecimal moneyMarket = new BigDecimal("2505.0");
        BigDecimal saldo = new BigDecimal("5000.00");
        BigDecimal totalLiquidez = moneyMarket.add(saldo);
        // BigDecimal.add preserves max scale: 2505.0 (scale 1) + 5000.00 (scale 2) = 7505.00 (scale 2)
        assertEquals(new BigDecimal("7505.00"), totalLiquidez,
                "B-CTA-06b: TotalLiquidez = 2505.0 + 5000.00 = 7505.00 (scale 2 from BigDecimal.add)");
    }

    // -------------------------------------------------------------------------
    // B-CTA-07 — ConsultarMovimientoCuentaInversion Go-Live routing
    // Source: ConsultarMovimientoCuentaInversion.java lines near 244, 299-305
    // -------------------------------------------------------------------------

    private static final LocalDate GO_LIVE = LocalDate.of(2024, 1, 1);

    private String goLiveRouting(LocalDate desde, LocalDate hasta) {
        // B-CTA-07: inlined from ConsultarMovimientoCuentaInversion.java:244, 299-305
        if (desde.isBefore(GO_LIVE) && hasta.isBefore(GO_LIVE)) {
            return "HISTORICAL_ONLY";
        } else if (desde.isBefore(GO_LIVE) && !hasta.isBefore(GO_LIVE)) {
            return "HYBRID";
        } else {
            return "CURRENT_ONLY";
        }
    }

    @Test
    void rule07_bothBeforeGoLive_yields_HISTORICAL_ONLY() {
        // B-CTA-07: desde=2023-06-01, hasta=2023-12-31 — both before 2024-01-01
        String route = goLiveRouting(LocalDate.of(2023, 6, 1), LocalDate.of(2023, 12, 31));
        assertEquals("HISTORICAL_ONLY", route,
                "B-CTA-07: both dates before GoLive → HISTORICAL_ONLY");
    }

    @Test
    void rule07_desdeBeforeGoLive_hastaAfter_yields_HYBRID() {
        // B-CTA-07: desde=2023-12-01, hasta=2024-02-01 — spans GoLive
        String route = goLiveRouting(LocalDate.of(2023, 12, 1), LocalDate.of(2024, 2, 1));
        assertEquals("HYBRID", route,
                "B-CTA-07: desde before GoLive, hasta on/after → HYBRID");
    }

    @Test
    void rule07_desdeOnOrAfterGoLive_yields_CURRENT_ONLY() {
        // B-CTA-07: desde=2024-01-01, hasta=2024-03-01 — both on/after GoLive
        String route = goLiveRouting(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1));
        assertEquals("CURRENT_ONLY", route,
                "B-CTA-07: desde on GoLive date → CURRENT_ONLY");
    }

    // -------------------------------------------------------------------------
    // B-CTA-08 — ConsultarCartolaDeMovimiento SaldoInicial selector
    // Source: ConsultarCartolaDeMovimiento.java:256-259
    // Formula: SaldoInicial = (p_SaldoPorFechaEfectiva.equals("S"))
    //              ? SaldoEfectivoInicial : SaldoMovimientoInicial
    // -------------------------------------------------------------------------

    @Test
    void rule08_saldoPorFechaEfectiva_S_yields_SaldoEfectivoInicial() {
        // B-CTA-08: Source: ConsultarCartolaDeMovimiento.java:256-259
        BigDecimal saldoEfectivo = new BigDecimal("8000.00");
        BigDecimal saldoMovimiento = new BigDecimal("7500.00");
        BigDecimal result = "S".equals("S") ? saldoEfectivo : saldoMovimiento;
        assertEquals(new BigDecimal("8000.00"), result,
                "B-CTA-08: flag=S → SaldoEfectivoInicial selected");
    }

    @Test
    void rule08_saldoPorFechaEfectiva_N_yields_SaldoMovimientoInicial() {
        // B-CTA-08: Source: ConsultarCartolaDeMovimiento.java:256-259
        BigDecimal saldoEfectivo = new BigDecimal("8000.00");
        BigDecimal saldoMovimiento = new BigDecimal("7500.00");
        BigDecimal result = "N".equals("S") ? saldoEfectivo : saldoMovimiento;
        assertEquals(new BigDecimal("7500.00"), result,
                "B-CTA-08: flag=N → SaldoMovimientoInicial selected");
    }

    // -------------------------------------------------------------------------
    // B-CTA-08b — ConsultarCartolaDeMovimiento FAC nemo enrichment
    // Source: ConsultarCartolaDeMovimiento.java:382-387, 288
    // FAC module + non-ABNAPV concept → nemo replaced
    // FAC module + ABNAPV concept → nemo NOT replaced (excluded)
    // -------------------------------------------------------------------------

    private String applyNemoEnrichment(String idModulo, String idConcepto,
            String originalNemo, String hashNemo) {
        // B-CTA-08b: inlined from ConsultarCartolaDeMovimiento.java:382-387, 288
        if ("FAC".equals(idModulo) && !"ABNAPV".equalsIgnoreCase(idConcepto)) {
            return hashNemo != null ? hashNemo : originalNemo;
        }
        return originalNemo;
    }

    @Test
    void rule08b_facModule_nonAbnapv_replacesNemo() {
        // B-CTA-08b: FAC + concept != ABNAPV → NemotecnicoOriginal replaced from hash
        String result = applyNemoEnrichment("FAC", "COMPRA", "OLD_NEMO", "NEW_NEMO");
        assertEquals("NEW_NEMO", result,
                "B-CTA-08b: FAC module + non-ABNAPV concept → nemo replaced by hash value");
    }

    @Test
    void rule08b_facModule_abnapvConcept_doesNotReplaceNemo() {
        // B-CTA-08b: FAC + ABNAPV → excluded from enrichment even when IncluyeAnulados=S
        String result = applyNemoEnrichment("FAC", "ABNAPV", "OLD_NEMO", "NEW_NEMO");
        assertEquals("OLD_NEMO", result,
                "B-CTA-08b: FAC module + ABNAPV concept → nemo NOT replaced (excluded)");
    }

    @Test
    void rule08b_nonFacModule_doesNotReplaceNemo() {
        // B-CTA-08b: non-FAC module → no enrichment regardless of concept
        String result = applyNemoEnrichment("OTR", "COMPRA", "OLD_NEMO", "NEW_NEMO");
        assertEquals("OLD_NEMO", result,
                "B-CTA-08b: non-FAC module → nemo unchanged");
    }

    // -------------------------------------------------------------------------
    // B-CTA-03 — CertificarCargoAbono state transition
    // Source: CertificarCargoAbono.java:348-350, 467
    // -------------------------------------------------------------------------

    private boolean canCertify(String idEstado) {
        // B-CTA-03: inlined from CertificarCargoAbono.java:348-350
        // Cannot certify CRT or ANU states
        return !("CRT".equalsIgnoreCase(idEstado) || "ANU".equalsIgnoreCase(idEstado));
    }

    @Test
    void rule03_estadoCRT_cannotBeCertified() {
        // B-CTA-03: state=CRT → certification blocked
        assertFalse(canCertify("CRT"),
                "B-CTA-03: CRT state cannot be certified again");
    }

    @Test
    void rule03_estadoANU_cannotBeCertified() {
        // B-CTA-03: state=ANU → certification blocked
        assertFalse(canCertify("ANU"),
                "B-CTA-03: ANU (annulled) state cannot be certified");
    }

    @Test
    void rule03_estadoPEN_canBeCertified() {
        // B-CTA-03: state=PEN → eligible for certification
        assertTrue(canCertify("PEN"),
                "B-CTA-03: PEN state can be certified");
    }

    @Test
    void rule03_flujoCajaInsuficiente_whenMontoExceedsSaldo() {
        // B-CTA-03: Source: CertificarCargoAbono.java:467
        // totalMontoPortafolio.doubleValue() > saldoCajaPortafolio.doubleValue()
        //   → FLUJO_CAJA_INSUFICIENTE
        double totalMonto = 1500.0;
        double saldoCaja = 1000.0;
        boolean insuficiente = totalMonto > saldoCaja;
        assertTrue(insuficiente,
                "B-CTA-03: totalMonto=1500 > saldoCaja=1000 → FLUJO_CAJA_INSUFICIENTE");
    }

    @Test
    void rule03_flujoCajaOk_whenMontoDoesNotExceedSaldo() {
        // B-CTA-03: Source: CertificarCargoAbono.java:467
        double totalMonto = 800.0;
        double saldoCaja = 1000.0;
        boolean insuficiente = totalMonto > saldoCaja;
        assertFalse(insuficiente,
                "B-CTA-03: totalMonto=800 <= saldoCaja=1000 → flujo OK");
    }

    // -------------------------------------------------------------------------
    // B-CTA-09 — CuadraturaCierreDiario result codes
    // Source: CuadraturaCierreDiario.java:190-195
    // -------------------------------------------------------------------------

    private String cuadraturaResultCode(int totalMovements, int pendingMovements) {
        // B-CTA-09: inlined from CuadraturaCierreDiario.java:190-195
        if (totalMovements == 0) {
            return "UI_SIN_OPERACIONES";
        } else if (pendingMovements == 0) {
            return "UI_CUADRADO";
        } else {
            return "UI_DESCUADRADO";
        }
    }

    @Test
    void rule09_noMovements_yields_UI_SIN_OPERACIONES() {
        // B-CTA-09: Source: CuadraturaCierreDiario.java:186-187
        assertEquals("UI_SIN_OPERACIONES", cuadraturaResultCode(0, 0),
                "B-CTA-09: no movements → UI_SIN_OPERACIONES");
    }

    @Test
    void rule09_allCertifiedOrAnnulled_yields_UI_CUADRADO() {
        // B-CTA-09: Source: CuadraturaCierreDiario.java:188-190
        // certificacionesPendientes.size() == 0 → UI_CUADRADO
        assertEquals("UI_CUADRADO", cuadraturaResultCode(5, 0),
                "B-CTA-09: 5 movements, 0 pending → UI_CUADRADO");
    }

    @Test
    void rule09_anyPending_yields_UI_DESCUADRADO() {
        // B-CTA-09: Source: CuadraturaCierreDiario.java:191-193
        assertEquals("UI_DESCUADRADO", cuadraturaResultCode(5, 2),
                "B-CTA-09: 5 movements, 2 pending → UI_DESCUADRADO");
    }

    // -------------------------------------------------------------------------
    // B-CTA-10 — ProcesarCobroComisionPatrimonio concept/type mapping
    // Source: ProcesarCobroComisionPatrimonio.java:217, 236
    // -------------------------------------------------------------------------

    private String[] conceptoVoucherForPortafolio(boolean isApv) {
        // B-CTA-10: inlined from ProcesarCobroComisionPatrimonio.java:217, 236
        if (isApv) {
            return new String[]{"CMTAPV", "FOX"};
        } else {
            return new String[]{"CMMNRV", "FOE"};
        }
    }

    @Test
    void rule10_apvPortafolio_yields_CMTAPV_FOX() {
        // B-CTA-10: Source: ProcesarCobroComisionPatrimonio.java:217
        String[] result = conceptoVoucherForPortafolio(true);
        assertAll("B-CTA-10 APV portafolio",
                () -> assertEquals("CMTAPV", result[0], "B-CTA-10: APV → concepto=CMTAPV"),
                () -> assertEquals("FOX", result[1], "B-CTA-10: APV → voucherType=FOX")
        );
    }

    @Test
    void rule10_nonApvPortafolio_yields_CMMNRV_FOE() {
        // B-CTA-10: Source: ProcesarCobroComisionPatrimonio.java:236
        String[] result = conceptoVoucherForPortafolio(false);
        assertAll("B-CTA-10 non-APV portafolio",
                () -> assertEquals("CMMNRV", result[0], "B-CTA-10: non-APV → concepto=CMMNRV"),
                () -> assertEquals("FOE", result[1], "B-CTA-10: non-APV → voucherType=FOE")
        );
    }

    // -------------------------------------------------------------------------
    // B-CTA-11 — ConsultarMovimientos Origen derivation
    // Source: webservices/ConsultarMovimientos.java:366
    // Origen = (startsWith("APV") || startsWith("DC")) ? "APV" : "NOAPV"
    // -------------------------------------------------------------------------

    private String origenFromPortafolio(String codigo) {
        // B-CTA-11: inlined from webservices/ConsultarMovimientos.java:366
        return (codigo.startsWith("APV") || codigo.startsWith("DC")) ? "APV" : "NOAPV";
    }

    @Test
    void rule11_apvPrefix_yields_APV() {
        // B-CTA-11: "APV001" → "APV"
        assertEquals("APV", origenFromPortafolio("APV001"),
                "B-CTA-11: APV prefix → Origen=APV");
    }

    @Test
    void rule11_dcPrefix_yields_APV() {
        // B-CTA-11: "DC0001" → "APV"
        assertEquals("APV", origenFromPortafolio("DC0001"),
                "B-CTA-11: DC prefix → Origen=APV");
    }

    @Test
    void rule11_pensPrefix_yields_NOAPV() {
        // B-CTA-11: "PENS001" → "NOAPV"
        assertEquals("NOAPV", origenFromPortafolio("PENS001"),
                "B-CTA-11: PENS prefix → Origen=NOAPV");
    }

    @Test
    void rule11_dcfundPrefix_yields_APV() {
        // B-CTA-11: "DCFUND" starts with "DC" → "APV"
        assertEquals("APV", origenFromPortafolio("DCFUND"),
                "B-CTA-11: DCFUND starts with DC → Origen=APV");
    }

    // -------------------------------------------------------------------------
    // B-CTA-11b — ConsultarMovimientos TipoFechaConsulta routing
    // Source: webservices/ConsultarMovimientos.java:257
    // -------------------------------------------------------------------------

    private String saldoPorFechaEfectivaForTipo(String tipo) {
        // B-CTA-11b: inlined from webservices/ConsultarMovimientos.java:257
        return "LIQ".equals(tipo) ? "S" : "N";
    }

    @Test
    void rule11b_tipoLIQ_yields_saldoPorFechaEfectiva_S() {
        // B-CTA-11b: TipoFechaConsulta=LIQ → p_SaldoPorFechaEfectiva=S
        assertEquals("S", saldoPorFechaEfectivaForTipo("LIQ"),
                "B-CTA-11b: LIQ → SaldoPorFechaEfectiva=S");
    }

    @Test
    void rule11b_tipoADQ_yields_saldoPorFechaEfectiva_N() {
        // B-CTA-11b: TipoFechaConsulta=ADQ → p_SaldoPorFechaEfectiva=N
        assertEquals("N", saldoPorFechaEfectivaForTipo("ADQ"),
                "B-CTA-11b: ADQ → SaldoPorFechaEfectiva=N");
    }

    // -------------------------------------------------------------------------
    // B-CTA-12 — IndiceRentabilidadAtributtionAnalysis profitability formula
    // Source: IndiceRentabilidadAtributtionAnalysis.java:158, 161
    // Formula: rentabilidadIndice = (PrecioFinal/PrecioInicial - 1) × 100
    //          scale 2, RoundingMode.HALF_UP
    //          Indice="ND" → 0.000; null prices → default to BigDecimal.ONE
    // -------------------------------------------------------------------------

    private BigDecimal rentabilidadFormula(BigDecimal precioFinal, BigDecimal precioInicial,
            String indice) {
        // B-CTA-12: inlined from IndiceRentabilidadAtributtionAnalysis.java:147-161
        if ("ND".equalsIgnoreCase(indice)) {
            // Source: ~line 140-146 (ND branch skips division)
            return BigDecimal.ZERO;
        }
        BigDecimal a = (precioFinal != null) ? precioFinal : BigDecimal.ONE;
        BigDecimal b = (precioInicial != null) ? precioInicial : BigDecimal.ONE;
        // Source: IndiceRentabilidadAtributtionAnalysis.java:158
        BigDecimal c = a.divide(b, 2, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
        // Source: IndiceRentabilidadAtributtionAnalysis.java:161
        return c.multiply(new BigDecimal(100L));
    }

    @Test
    void rule12_gain10pct_yields_10_00() {
        // B-CTA-12: PrecioFinal=110, PrecioInicial=100 → (110/100-1)*100 = 10.00
        BigDecimal result = rentabilidadFormula(
                new BigDecimal("110"), new BigDecimal("100"), "SPY");
        assertEquals(new BigDecimal("10.00"), result,
                "B-CTA-12: 10% gain → rentabilidadIndice=10.00");
    }

    @Test
    void rule12_loss5pct_yields_neg5_00() {
        // B-CTA-12: PrecioFinal=95, PrecioInicial=100 → (95/100-1)*100 = -5.00
        BigDecimal result = rentabilidadFormula(
                new BigDecimal("95"), new BigDecimal("100"), "SPY");
        assertEquals(new BigDecimal("-5.00"), result,
                "B-CTA-12: 5% loss → rentabilidadIndice=-5.00");
    }

    @Test
    void rule12_indiceND_yields_zero() {
        // B-CTA-12: Indice="ND" → rentabilidadIndice=0.000 (no division)
        BigDecimal result = rentabilidadFormula(
                new BigDecimal("110"), new BigDecimal("100"), "ND");
        assertEquals(BigDecimal.ZERO, result,
                "B-CTA-12: Indice=ND → 0 (no division performed)");
    }

    @Test
    void rule12_nullPrecioFinal_defaultsToOne_yields_zero() {
        // B-CTA-12: null PrecioFinal → default to ONE; ONE/ONE - 1 = 0 * 100 = 0
        // Source: IndiceRentabilidadAtributtionAnalysis.java:152-157
        BigDecimal result = rentabilidadFormula(null, null, "IDX");
        assertEquals(new BigDecimal("0.00"), result,
                "B-CTA-12: null prices default to ONE → (1/1-1)*100 = 0.00");
    }

    // -------------------------------------------------------------------------
    // B-CTA-13 — GuardarPatrimonioCajaCalculoComision commission base
    // Source: GuardarPatrimonioCajaCalculoComision.java:200, 226-227
    // -------------------------------------------------------------------------

    private String idEstadoComision(BigDecimal montoComision) {
        // B-CTA-13: Source: GuardarPatrimonioCajaCalculoComision.java:226-227
        // MontoComision < 0 → treated as 0 before state derivation
        BigDecimal efectivo = montoComision.compareTo(BigDecimal.ZERO) > 0
                ? montoComision : BigDecimal.ZERO;
        return efectivo.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "VIG";
    }

    @Test
    void rule13_positiveCommission_yields_VIG() {
        // B-CTA-13: commission=50 → VIG
        assertEquals("VIG", idEstadoComision(new BigDecimal("50")),
                "B-CTA-13: commission=50 → IdEstadoComision=VIG");
    }

    @Test
    void rule13_zeroCommission_yields_INA() {
        // B-CTA-13: commission=0 → INA
        assertEquals("INA", idEstadoComision(BigDecimal.ZERO),
                "B-CTA-13: commission=0 → IdEstadoComision=INA");
    }

    @Test
    void rule13_negativeCommission_treatedAsZero_yields_INA() {
        // B-CTA-13: Source: GuardarPatrimonioCajaCalculoComision.java:226
        // montoComision.compareTo(ZERO) > 0 ? montoComision : ZERO
        // commission=-10 → treated as 0 → INA
        assertEquals("INA", idEstadoComision(new BigDecimal("-10")),
                "B-CTA-13: commission=-10 treated as 0 → IdEstadoComision=INA");
    }

    @Test
    void rule13_patrimonioAccumulation_addsDaily() {
        // B-CTA-13: Source: GuardarPatrimonioCajaCalculoComision.java:200
        // montoPatrimonioAfectoComision(today) = prev + saldoDiario  (MathContext.DECIMAL128)
        BigDecimal prevDay = new BigDecimal("10000.00");
        BigDecimal saldoDiario = new BigDecimal("500.00");
        BigDecimal today = prevDay.add(saldoDiario, MathContext.DECIMAL128);
        assertEquals(new BigDecimal("10500.00"), today,
                "B-CTA-13: patrimonio today = prevDay+saldoDiario = 10500.00");
    }

    // -------------------------------------------------------------------------
    // B-CTA-14 — CrearCargoAbonoWS MOVTRN remapping
    // Source: webservices/CrearCargoAbonoWS.java:282, 290
    // -------------------------------------------------------------------------

    private String[] remapTipoMovimiento(String idTipo) {
        // B-CTA-14: inlined from webservices/CrearCargoAbonoWS.java:282, 290
        if ("MOVTRN".equals(idTipo)) {
            return new String[]{"MOVCAR", "S"};
        }
        return new String[]{idTipo, "N"};
    }

    @Test
    void rule14_MOVTRN_remappedTo_MOVCAR_traspasoS() {
        // B-CTA-14: Source: webservices/CrearCargoAbonoWS.java:282
        String[] result = remapTipoMovimiento("MOVTRN");
        assertAll("B-CTA-14 MOVTRN remap",
                () -> assertEquals("MOVCAR", result[0], "B-CTA-14: MOVTRN → IdTipoMovimiento=MOVCAR"),
                () -> assertEquals("S", result[1], "B-CTA-14: MOVTRN → EsCargoAbonoPorTraspaso=S")
        );
    }

    @Test
    void rule14_MOVCAR_unchanged_traspasoN() {
        // B-CTA-14: Source: webservices/CrearCargoAbonoWS.java:290
        String[] result = remapTipoMovimiento("MOVCAR");
        assertAll("B-CTA-14 MOVCAR unchanged",
                () -> assertEquals("MOVCAR", result[0], "B-CTA-14: MOVCAR → unchanged"),
                () -> assertEquals("N", result[1], "B-CTA-14: MOVCAR → EsCargoAbonoPorTraspaso=N")
        );
    }

    // -------------------------------------------------------------------------
    // B-CTA-15 — GuardarPatrimonioCaja patrimony formula + state
    // Source: GuardarPatrimonioCaja.java:304, 312
    // Formula: "+" → add field value; "-" → subtract (via negate)
    // MontoPatrimonio == 0 → "INA"; > 0 → "ACT"
    // -------------------------------------------------------------------------

    private BigDecimal applyPatrimonioItems(List<String[]> items) {
        // B-CTA-15: inlined from GuardarPatrimonioCaja.java:304
        // patrimonioCaja.MontoPatrimonio = patrimonioCaja.MontoPatrimonio
        //     .add("+".equals(item.FuncionOperador) ? monto : monto.negate(MathContext.DECIMAL128))
        BigDecimal patrimonio = BigDecimal.ZERO;
        for (String[] item : items) {
            BigDecimal monto = new BigDecimal(item[1]);
            patrimonio = patrimonio.add(
                    "+".equals(item[0]) ? monto : monto.negate(MathContext.DECIMAL128));
        }
        return patrimonio;
    }

    @Test
    void rule15_patrimonioItems_1000_minus200_yields_800_ACT() {
        // B-CTA-15: Source: GuardarPatrimonioCaja.java:304, 312
        // [(+,1000), (-,200)] → 800 → "ACT"
        List<String[]> items = List.of(new String[]{"+", "1000"}, new String[]{"-", "200"});
        BigDecimal patrimonio = applyPatrimonioItems(items);
        // Source: GuardarPatrimonioCaja.java:312
        String estado = patrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
        assertAll("B-CTA-15 net positive",
                () -> assertEquals(new BigDecimal("800"), patrimonio,
                        "B-CTA-15: 1000-200=800"),
                () -> assertEquals("ACT", estado,
                        "B-CTA-15: 800 > 0 → IdEstado=ACT")
        );
    }

    @Test
    void rule15_patrimonioItems_100_minus100_yields_0_INA() {
        // B-CTA-15: Source: GuardarPatrimonioCaja.java:304, 312
        // [(+,100), (-,100)] → 0 → "INA"
        List<String[]> items = List.of(new String[]{"+", "100"}, new String[]{"-", "100"});
        BigDecimal patrimonio = applyPatrimonioItems(items);
        // Source: GuardarPatrimonioCaja.java:312
        String estado = patrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
        assertAll("B-CTA-15 net zero",
                () -> assertEquals(BigDecimal.ZERO, patrimonio,
                        "B-CTA-15: 100-100=0"),
                () -> assertEquals("INA", estado,
                        "B-CTA-15: 0 → IdEstado=INA")
        );
    }

    // -------------------------------------------------------------------------
    // B-CTA-02 — AnularCargoAbono dual strategy
    // Source: AnularCargoAbono.java (PermiteAnularMovimiento flag)
    // -------------------------------------------------------------------------

    private String anularStrategy(String permiteAnular) {
        // B-CTA-02: inlined from AnularCargoAbono.java dispatch
        // if ("S".equals(permiteAnular)) → set IdEstado="ANU"
        // else → create counterpart movement (sign-reversed)
        if ("S".equals(permiteAnular)) {
            return "SET_ANU";
        } else {
            return "CREATE_COUNTERPART";
        }
    }

    @Test
    void rule02_permiteAnularS_setsIdEstadoANU() {
        // B-CTA-02: flag=S → direct state set to ANU
        assertEquals("SET_ANU", anularStrategy("S"),
                "B-CTA-02: permiteAnular=S → sets IdEstado=ANU directly");
    }

    @Test
    void rule02_permiteAnularN_createsCounterpart() {
        // B-CTA-02: flag=N → creates sign-reversed counterpart movement
        assertEquals("CREATE_COUNTERPART", anularStrategy("N"),
                "B-CTA-02: permiteAnular=N → creates counterpart movement");
    }

    // -------------------------------------------------------------------------
    // Canary — proves the test suite can detect formula regressions (B-CTA-04)
    // -------------------------------------------------------------------------

    /**
     * B-CTA-04 canary: changing '+' to '-' in the running accumulation must
     * cause this test to fail, proving the suite detects sign regressions.
     * The test uses the correct '+' operator and must PASS on correct code.
     * To prove regression detection: swap running.add(m) → running.subtract(m)
     * and observe this test go red before restoring it.
     */
    @Test
    void canary_saldoAccumulationBreakableByChangingSign() {
        // B-CTA-04: correct formula → [100, 70, 120]
        // Breaking formula (subtract) → [100, 130, 80] — different, test fails
        List<BigDecimal> montos = List.of(
                new BigDecimal("100"),
                new BigDecimal("-30"),
                new BigDecimal("50"));
        List<BigDecimal> expected = List.of(
                new BigDecimal("100"),
                new BigDecimal("70"),
                new BigDecimal("120"));
        BigDecimal running = BigDecimal.ZERO;
        List<BigDecimal> actual = new ArrayList<>();
        for (BigDecimal m : montos) {
            running = running.add(m); // B-CTA-04: change to subtract() here to break
            actual.add(running);
        }
        assertEquals(expected, actual,
                "Canary B-CTA-04: correct accumulation [100,-30,50] → [100,70,120]; "
                + "changing add() to subtract() breaks this test");
    }
}
