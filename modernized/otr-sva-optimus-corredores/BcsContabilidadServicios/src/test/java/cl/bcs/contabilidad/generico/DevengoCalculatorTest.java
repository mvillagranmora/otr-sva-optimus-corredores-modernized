package cl.bcs.contabilidad.generico;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.service.devengo.CarteraValorizada;
import cl.bcs.contabilidad.domain.service.devengo.DevengoCalculator;
import cl.bcs.contabilidad.domain.service.devengo.TipoDevengo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static cl.bcs.contabilidad.generico.DevengoFixtures.FECHA_CONTABLE;
import static cl.bcs.contabilidad.generico.DevengoFixtures.ID_EMPRESA;
import static cl.bcs.contabilidad.generico.DevengoFixtures.TIPO_CARTERA_PROPIA;
import static cl.bcs.contabilidad.generico.DevengoFixtures.TIPO_TASA_COMPRA_PACTADA;
import static cl.bcs.contabilidad.generico.DevengoFixtures.TIPO_TASA_PACTO;
import static cl.bcs.contabilidad.generico.DevengoFixtures.cartera;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Characterization tests for the Devengo* amount derivations.
 *
 * IMPORTANT: none of the three legacy Devengo* Java classes computes an accrual.
 * The accrued amounts (MontoInteres, MontoInteresCorrido, MontoReajuste, ...)
 * are read pre-computed from "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF".
 * There is no day count, no base 360/365 and no rate conversion in this scope.
 * What the legacy *does* compute lives in its SQL:
 *   - ROUND(x, 0) of every event amount (HANA default rounding = ROUND_HALF_UP,
 *     ties away from zero -> java.math.RoundingMode.HALF_UP, scale 0);
 *   - sign-based choice of event code, tested on the ROUNDED amount;
 *   - ABS(MontoActual) (NOT rounded) as movement amount;
 *   - YEAR(FechaOperacion) / TO_CHAR(FechaOperacion,'MM-YYYY');
 *   - CASE mappings of IdTipoOperacion and row filters.
 * The target moves those rules into {@link DevengoCalculator}; these tests pin them.
 */
class DevengoCalculatorTest {

    private final DevengoCalculator calc = new DevengoCalculator();

    private static TabEventoRef ev(Long tipo, Long idMov, String idEvento, String nombre, String monto) {
        return new TabEventoRef(tipo, idMov, idEvento, nombre,
                monto == null ? null : new BigDecimal(monto), "CLP");
    }

    // =====================================================================
    // Rounding
    // =====================================================================

    @ParameterizedTest(name = "CONT-C-01 ROUND({0},0) = {1}")
    @DisplayName("CONT-C-01: event amounts are ROUND(x,0) half-up (ties away from zero), scale 0")
    // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:7-13,
    //         CentralizaDevengoATasaPacto_SelectEventos.sql:7-13,
    //         CentralizaDevengoCarteraPropiaLibreYGarantia_SelectEventos.sql:6-12
    @CsvSource({
            "1234.5,        1235",
            "1234.49,       1234",
            "1234.4999999,  1234",
            "0.5,           1",
            "0.49,          0",
            "0,             0",
            "0.00,          0",
            "-0.4,          0",
            "-0.5,          -1",
            "-2.5,          -3",
            "-2.49,         -2",
            "123456789012.5, 123456789013",
            "1002666.67,    1002667"
    })
    void contC01_redondearMontoHalfUpScaleZero(String input, String expected) {
        BigDecimal r = calc.redondearMonto(new BigDecimal(input));
        assertThat(r).isEqualTo(new BigDecimal(expected));
        assertThat(r.scale()).isZero();
    }

    @ParameterizedTest(name = "CONT-C-01 ROUND(NULL,0) = NULL")
    @NullSource
    void contC01_redondearMontoNullStaysNull(BigDecimal input) {
        // SQL ROUND(NULL,0) is NULL.
        assertThat(calc.redondearMonto(input)).isNull();
    }

    // =====================================================================
    // DevengoATasaCompraCarteraPactada (IdTipoCentralizacion 440)
    // =====================================================================

    @Nested
    @DisplayName("TASA_COMPRA_CARTERA_PACTADA")
    class TasaCompraCarteraPactada {

        private final TipoDevengo T = TipoDevengo.TASA_COMPRA_CARTERA_PACTADA;
        private final Long TIPO = TIPO_TASA_COMPRA_PACTADA;

        @Test
        @DisplayName("CONT-C-02: COMPAC with non-negative amounts -> 7 events, *_TC_PACTO_COMPRA codes, rounded amounts")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:21-36
        void contC02_compacPositiveAmounts() {
            CarteraValorizada c = cartera().idTipoOperacion("COMPAC").build();

            List<TabEventoRef> eventos = calc.eventos(T, TIPO, c);

            assertThat(eventos).containsExactly(
                    ev(TIPO, 70001L, "MONTO_OPERACION", "Monto Operacion", "1000001"),
                    ev(TIPO, 70001L, "MONTO_PACTADO", "Monto Pactado", "1005000"),
                    ev(TIPO, 70001L, "MONTO_ANTERIOR_TC_PACTO_COMPRA", "Monto Anterior a Tasa Pacto", "1002501"),
                    ev(TIPO, 70001L, "MONTO_PRESENTE_TC_PACTO_COMPRA", "Monto Presente a Tasa Pacto", "1002667"),
                    ev(TIPO, 70001L, "INTERES_DIARIO_TC_PACTO_COMPRA", "Interes Diario", "167"),
                    ev(TIPO, 70001L, "INTERES_ACUMULADO_TC_PACTO_COMPRA", "Interes Acumulado", "2501"),
                    // MontoReajuste 0.00 -> 0 -> ">= 0" branch: zero is a POSITIVE event
                    ev(TIPO, 70001L, "REAJUSTE_TC_PACTO_COMPRA", "Reajuste", "0"));
        }

        @Test
        @DisplayName("CONT-C-03: VENPAC -> *_TC_PACTO_VENTA codes; MONTO_OPERACION/MONTO_PACTADO unchanged")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:25-35 (CASE 'VENPAC')
        void contC03_venpacUsesVentaSuffix() {
            CarteraValorizada c = cartera().idTipoOperacion("VENPAC").build();

            assertThat(calc.eventos(T, TIPO, c)).extracting(TabEventoRef::idEvento).containsExactly(
                    "MONTO_OPERACION",
                    "MONTO_PACTADO",
                    "MONTO_ANTERIOR_TC_PACTO_VENTA",
                    "MONTO_PRESENTE_TC_PACTO_VENTA",
                    "INTERES_DIARIO_TC_PACTO_VENTA",
                    "INTERES_ACUMULADO_TC_PACTO_VENTA",
                    "REAJUSTE_TC_PACTO_VENTA");
        }

        @Test
        @DisplayName("CONT-C-04: negative interes/reajuste -> NEGATIVO codes keep the negative rounded amount; 'Interes  Diario' has two spaces")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:29-45
        void contC04_negativeBranchesMixed() {
            CarteraValorizada c = cartera().idTipoOperacion("COMPAC")
                    .montoInteres("-166.50")       // -> -167 (half-up away from zero)
                    .montoInteresCorrido("2500.50") // -> 2501 positive
                    .montoReajuste("-12.49")       // -> -12
                    .build();

            assertThat(calc.eventos(T, TIPO, c)).containsExactly(
                    ev(TIPO, 70001L, "MONTO_OPERACION", "Monto Operacion", "1000001"),
                    ev(TIPO, 70001L, "MONTO_PACTADO", "Monto Pactado", "1005000"),
                    ev(TIPO, 70001L, "MONTO_ANTERIOR_TC_PACTO_COMPRA", "Monto Anterior a Tasa Pacto", "1002501"),
                    ev(TIPO, 70001L, "MONTO_PRESENTE_TC_PACTO_COMPRA", "Monto Presente a Tasa Pacto", "1002667"),
                    ev(TIPO, 70001L, "INTERES_ACUMULADO_TC_PACTO_COMPRA", "Interes Acumulado", "2501"),
                    ev(TIPO, 70001L, "INTERES_NEGATIVO_DIARIO_TC_PACTO_COMPRA", "Interes  Diario", "-167"),
                    ev(TIPO, 70001L, "REAJUSTE_NEGATIVO_TC_PACTO_COMPRA", "Reajuste", "-12"));
        }

        @Test
        @DisplayName("CONT-C-04: VENPAC all negative -> INTERES_NEGATIVO_DIARIO / INTERES_NEG_ACUMULADO / REAJUSTE_NEGATIVO *_VENTA")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:38-45
        void contC04_venpacAllNegative() {
            CarteraValorizada c = cartera().idTipoOperacion("VENPAC")
                    .montoInteres("-1").montoInteresCorrido("-2").montoReajuste("-3").build();

            assertThat(calc.eventos(T, TIPO, c))
                    .extracting(TabEventoRef::idEvento, TabEventoRef::nombre, TabEventoRef::montoEvento)
                    .endsWith(
                            tuple("INTERES_NEGATIVO_DIARIO_TC_PACTO_VENTA", "Interes  Diario", new BigDecimal("-1")),
                            tuple("INTERES_NEG_ACUMULADO_TC_PACTO_VENTA", "Interes Acumulado", new BigDecimal("-2")),
                            tuple("REAJUSTE_NEGATIVO_TC_PACTO_VENTA", "Reajuste", new BigDecimal("-3")));
            assertThat(calc.eventos(T, TIPO, c)).hasSize(7);
        }

        @Test
        @DisplayName("CONT-C-05: sign is tested AFTER rounding: -0.4 -> positive branch with 0; -0.5 -> negative branch with -1")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:11 (ROUND in CTE) + :30 / :39 (WHERE on rounded alias)
        void contC05_signTestedOnRoundedAmount() {
            CarteraValorizada casiCero = cartera().montoInteres("-0.4").build();
            CarteraValorizada medio = cartera().montoInteres("-0.5").build();

            assertThat(calc.eventos(T, TIPO, casiCero))
                    .filteredOn(e -> e.idEvento().contains("DIARIO"))
                    .singleElement()
                    .satisfies(e -> {
                        assertThat(e.idEvento()).isEqualTo("INTERES_DIARIO_TC_PACTO_COMPRA");
                        assertThat(e.montoEvento()).isEqualByComparingTo("0");
                    });
            assertThat(calc.eventos(T, TIPO, medio))
                    .filteredOn(e -> e.idEvento().contains("DIARIO"))
                    .singleElement()
                    .satisfies(e -> {
                        assertThat(e.idEvento()).isEqualTo("INTERES_NEGATIVO_DIARIO_TC_PACTO_COMPRA");
                        assertThat(e.montoEvento()).isEqualByComparingTo("-1");
                    });
        }

        @Test
        @DisplayName("CONT-C-06: NULL interes/corrido/reajuste -> no conditional event at all; NULL unconditional amount -> event with null monto")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:21-23 (no WHERE) vs :30,:33,:36,:39,:42,:45 (NULL >= 0 and NULL < 0 both false)
        void contC06_nullAmounts() {
            CarteraValorizada c = cartera().montoCompra(null)
                    .montoInteres(null).montoInteresCorrido(null).montoReajuste(null).build();

            assertThat(calc.eventos(T, TIPO, c)).containsExactly(
                    ev(TIPO, 70001L, "MONTO_OPERACION", "Monto Operacion", null),
                    ev(TIPO, 70001L, "MONTO_PACTADO", "Monto Pactado", "1005000"),
                    ev(TIPO, 70001L, "MONTO_ANTERIOR_TC_PACTO_COMPRA", "Monto Anterior a Tasa Pacto", "1002501"),
                    ev(TIPO, 70001L, "MONTO_PRESENTE_TC_PACTO_COMPRA", "Monto Presente a Tasa Pacto", "1002667"));
        }

        @Test
        @DisplayName("CONT-C-06: all-zero amounts -> 7 events, every conditional one on the non-negative branch with 0")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:30,33,36 (>= 0)
        void contC06_zeroAmounts() {
            CarteraValorizada c = cartera().montoCompra("0").montoPactado("0").montoActual("0")
                    .montoProximo("0").montoInteres("0").montoInteresCorrido("0").montoReajuste("0").build();

            assertThat(calc.eventos(T, TIPO, c))
                    .extracting(TabEventoRef::idEvento)
                    .containsExactly("MONTO_OPERACION", "MONTO_PACTADO",
                            "MONTO_ANTERIOR_TC_PACTO_COMPRA", "MONTO_PRESENTE_TC_PACTO_COMPRA",
                            "INTERES_DIARIO_TC_PACTO_COMPRA", "INTERES_ACUMULADO_TC_PACTO_COMPRA",
                            "REAJUSTE_TC_PACTO_COMPRA");
        }

        @ParameterizedTest(name = "CONT-C-07 IdTipoOperacion={0} -> no events")
        @ValueSource(strings = {"COMPRA", "VENTA", "compac", "SIM", ""})
        @DisplayName("CONT-C-07: events only for IdTipoOperacion IN ('COMPAC','VENPAC') (case-sensitive)")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:19
        void contC07_eventsOnlyForPactos(String op) {
            assertThat(calc.eventos(T, TIPO, cartera().idTipoOperacion(op).build())).isEmpty();
        }

        @Test
        @DisplayName("CONT-C-07: IdTipoOperacion NULL -> no events")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectEventos.sql:19 (NULL IN (...) is not true)
        void contC07_nullOperacionNoEvents() {
            assertThat(calc.eventos(T, TIPO, cartera().idTipoOperacion(null).build())).isEmpty();
        }

        @ParameterizedTest(name = "CONT-C-08 {0} -> {1}")
        @CsvSource(value = {
                "COMPAC, CARTPACCOM",
                "VENPAC, CARTPACVEN",
                "COMPRA, ??????????",
                "NULL,   ??????????"
        }, nullValues = "NULL")
        @DisplayName("CONT-C-08: IdTipoMovimiento mapping; movements are NOT filtered by IdTipoOperacion (non-pacto -> 10 '?')")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectMovimientos.sql:4-8, WHERE :24-25 has no IdTipoOperacion filter
        void contC08_idTipoMovimiento(String op, String expected) {
            Optional<TabMovimientoRef> m = calc.movimiento(T, TIPO, FECHA_CONTABLE,
                    cartera().idTipoOperacion(op).build());
            assertThat(m).isPresent();
            assertThat(m.get().idTipoMovimiento()).isEqualTo(expected);
        }

        @Test
        @DisplayName("CONT-C-09: movement fields: IdMovimiento=IdValorizacion, Numero=FolioOperacion, Linea=0, Fecha=fechaContable, Monto=ABS(MontoActual) unrounded, Estado=''")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectMovimientos.sql:1-19
        void contC09_movimientoFields() {
            CarteraValorizada c = cartera().montoActual("-1002500.55").build();

            TabMovimientoRef m = calc.movimiento(T, TIPO, FECHA_CONTABLE, c).orElseThrow();

            assertThat(m).isEqualTo(new TabMovimientoRef(
                    TIPO, 70001L, "CARTPACCOM", ID_EMPRESA, 88001L, 0L,
                    "2025-03-10", "11111111-1", 300L, 2025L, "02-2025",
                    new BigDecimal("1002500.55"), "CLP", ""));
        }

        @Test
        @DisplayName("CONT-C-10: NULL FechaOperacion -> Ejercicio/Periodo null; NULL MontoActual -> Monto null; NULL moneda sistema -> IdMoneda null")
        // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectMovimientos.sql:15-18, :23 (LEFT JOIN Configuracion)
        void contC10_movimientoNulls() {
            CarteraValorizada c = cartera().fechaOperacion(null).montoActual(null).idMonedaSistema(null).build();

            TabMovimientoRef m = calc.movimiento(T, TIPO, FECHA_CONTABLE, c).orElseThrow();

            assertThat(m.ejercicio()).isNull();
            assertThat(m.periodo()).isNull();
            assertThat(m.monto()).isNull();
            assertThat(m.idMoneda()).isNull();
        }
    }

    @ParameterizedTest(name = "CONT-C-10 FechaOperacion {0} -> Ejercicio {1}, Periodo {2}")
    @CsvSource({
            "2024-12-31, 2024, 12-2024",
            "2025-01-01, 2025, 01-2025",
            "2024-02-29, 2024, 02-2024",
            "2025-03-10, 2025, 03-2025"
    })
    @DisplayName("CONT-C-10: Ejercicio = YEAR(FechaOperacion), Periodo = TO_CHAR(FechaOperacion,'MM-YYYY') across month/year boundaries")
    // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectMovimientos.sql:15-16 (same in TasaPacto :15-16 and CarteraPropia :11-12)
    void contC10_ejercicioPeriodoBoundaries(String fecha, long ejercicio, String periodo) {
        CarteraValorizada c = cartera().fechaOperacion(LocalDate.parse(fecha)).build();
        for (TipoDevengo t : TipoDevengo.values()) {
            TabMovimientoRef m = calc.movimiento(t, 1L, FECHA_CONTABLE, c).orElseThrow();
            assertThat(m.ejercicio()).as(t.name()).isEqualTo(ejercicio);
            assertThat(m.periodo()).as(t.name()).isEqualTo(periodo);
        }
    }

    // =====================================================================
    // DevengoATasaPacto (IdTipoCentralizacion 430)
    // =====================================================================

    @Nested
    @DisplayName("TASA_PACTO")
    class TasaPacto {

        private final TipoDevengo T = TipoDevengo.TASA_PACTO;
        private final Long TIPO = TIPO_TASA_PACTO;

        @Test
        @DisplayName("CONT-C-11: COMPAC -> 7 unconditional *_TP_PACTO_COMPRA events; negative interes stays INTERES_DIARIO (no NEGATIVO code)")
        // Legacy: CentralizaDevengoATasaPacto_SelectEventos.sql:21-33 (no WHERE on any branch)
        void contC11_compacUnconditionalEvents() {
            CarteraValorizada c = cartera().idTipoOperacion("COMPAC")
                    .montoInteres("-166.50").montoReajuste("-0.5").build();

            assertThat(calc.eventos(T, TIPO, c)).containsExactly(
                    ev(TIPO, 70001L, "MONTO_OPERACION", "Monto Operacion", "1000001"),
                    ev(TIPO, 70001L, "MONTO_PACTADO", "Monto Pactado", "1005000"),
                    ev(TIPO, 70001L, "MONTO_ANTERIOR_TP_PACTO_COMPRA", "Monto Anterior a Tasa Pacto", "1002501"),
                    ev(TIPO, 70001L, "MONTO_PRESENTE_TP_PACTO_COMPRA", "Monto Presente a Tasa Pacto", "1002667"),
                    ev(TIPO, 70001L, "INTERES_DIARIO_TP_PACTO_COMPRA", "Interes Diario", "-167"),
                    ev(TIPO, 70001L, "INTERES_ACUMULADO_TP_PACTO_COMPRA", "Interes Acumulado", "2501"),
                    ev(TIPO, 70001L, "REAJUSTE_TP_PACTO_COMPRA", "Reajuste", "-1"));
        }

        @Test
        @DisplayName("CONT-C-11: NULL amounts still emit all 7 events (with null monto)")
        // Legacy: CentralizaDevengoATasaPacto_SelectEventos.sql:21-33
        void contC11_nullAmountsStillEmitted() {
            CarteraValorizada c = cartera().montoInteres(null).montoInteresCorrido(null).montoReajuste(null).build();

            List<TabEventoRef> eventos = calc.eventos(T, TIPO, c);

            assertThat(eventos).hasSize(7);
            assertThat(eventos).filteredOn(e -> e.montoEvento() == null)
                    .extracting(TabEventoRef::idEvento)
                    .containsExactly("INTERES_DIARIO_TP_PACTO_COMPRA",
                            "INTERES_ACUMULADO_TP_PACTO_COMPRA", "REAJUSTE_TP_PACTO_COMPRA");
        }

        @Test
        @DisplayName("CONT-C-12: VENPAC -> *_TP_PACTO_VENTA codes")
        // Legacy: CentralizaDevengoATasaPacto_SelectEventos.sql:25-33
        void contC12_venpacVentaCodes() {
            assertThat(calc.eventos(T, TIPO, cartera().idTipoOperacion("VENPAC").build()))
                    .extracting(TabEventoRef::idEvento).containsExactly(
                            "MONTO_OPERACION", "MONTO_PACTADO",
                            "MONTO_ANTERIOR_TP_PACTO_VENTA", "MONTO_PRESENTE_TP_PACTO_VENTA",
                            "INTERES_DIARIO_TP_PACTO_VENTA", "INTERES_ACUMULADO_TP_PACTO_VENTA",
                            "REAJUSTE_TP_PACTO_VENTA");
        }

        @Test
        @DisplayName("CONT-C-12: non-pacto row -> no events")
        // Legacy: CentralizaDevengoATasaPacto_SelectEventos.sql:19
        void contC12_nonPactoNoEvents() {
            assertThat(calc.eventos(T, TIPO, cartera().idTipoOperacion("COMPRA").build())).isEmpty();
        }

        @Test
        @DisplayName("CONT-C-13: COMPAC -> PACTOCOMP, VENPAC -> PACTOVTA; other fields as TC")
        // Legacy: CentralizaDevengoATasaPacto_SelectMovimientos.sql:1-19
        void contC13_movimientoMapping() {
            TabMovimientoRef compra = calc.movimiento(T, TIPO, FECHA_CONTABLE,
                    cartera().idTipoOperacion("COMPAC").build()).orElseThrow();
            TabMovimientoRef venta = calc.movimiento(T, TIPO, FECHA_CONTABLE,
                    cartera().idTipoOperacion("VENPAC").montoActual("-5.5").build()).orElseThrow();

            assertThat(compra).isEqualTo(new TabMovimientoRef(
                    TIPO, 70001L, "PACTOCOMP", ID_EMPRESA, 88001L, 0L,
                    "2025-03-10", "11111111-1", 300L, 2025L, "02-2025",
                    new BigDecimal("1002500.50"), "CLP", ""));
            assertThat(venta.idTipoMovimiento()).isEqualTo("PACTOVTA");
            assertThat(venta.monto()).isEqualTo(new BigDecimal("5.5"));
        }

        @Test
        @DisplayName("CONT-C-13: non-pacto row -> no movement (WHERE IdTipoOperacion IN ('COMPAC','VENPAC'))")
        // Legacy: CentralizaDevengoATasaPacto_SelectMovimientos.sql:26
        void contC13_nonPactoNoMovement() {
            assertThat(calc.movimiento(T, TIPO, FECHA_CONTABLE,
                    cartera().idTipoOperacion("COMPRA").build())).isEmpty();
            assertThat(calc.movimiento(T, TIPO, FECHA_CONTABLE,
                    cartera().idTipoOperacion(null).build())).isEmpty();
        }
    }

    // =====================================================================
    // DevengoCarteraPropiaLibreYGarantia (IdTipoCentralizacion 450)
    // =====================================================================

    @Nested
    @DisplayName("CARTERA_PROPIA_LIBRE_Y_GARANTIA")
    class CarteraPropia {

        private final TipoDevengo T = TipoDevengo.CARTERA_PROPIA_LIBRE_Y_GARANTIA;
        private final Long TIPO = TIPO_CARTERA_PROPIA;

        @Test
        @DisplayName("CONT-C-14: RF row, non-negative -> 5 events keyed by IdCartera; MontoPactado/MontoInteresCorrido unused")
        // Legacy: CentralizaDevengoCarteraPropiaLibreYGarantia_SelectEventos.sql:4,19-32
        void contC14_positiveEvents() {
            CarteraValorizada c = cartera().clasificacion("RF").idTipoOperacion(null).build();

            assertThat(calc.eventos(T, TIPO, c)).containsExactly(
                    ev(TIPO, 55501L, "MONTO_COMPRA", "Monto Compra", "1000001"),
                    ev(TIPO, 55501L, "MONTO_ACTUAL_TASA_COMPRA", "Monto Actual a Tasa Compra", "1002501"),
                    ev(TIPO, 55501L, "MONTO_PROXIMO_TASA_COMPRA", "Monto Proximo a Tasa Compra", "1002667"),
                    ev(TIPO, 55501L, "INTERES_DIARIO_TASA_COMPRA", "Interes Diario a Tasa Compra", "167"),
                    ev(TIPO, 55501L, "REAJUSTE_TASA_COMPRA", "Reajuste a Tasa Compra", "0"));
        }

        @Test
        @DisplayName("CONT-C-15: negative interes/reajuste -> *_NEGATIVO_* codes with the same nombre; order compra, actual, proximo, interes, reajuste")
        // Legacy: CentralizaDevengoCarteraPropiaLibreYGarantia_SelectEventos.sql:25-35
        void contC15_negativeEvents() {
            CarteraValorizada c = cartera().clasificacion("IF")
                    .montoInteres("-2.5").montoReajuste("-1000.5").build();

            assertThat(calc.eventos(T, TIPO, c)).containsExactly(
                    ev(TIPO, 55501L, "MONTO_COMPRA", "Monto Compra", "1000001"),
                    ev(TIPO, 55501L, "MONTO_ACTUAL_TASA_COMPRA", "Monto Actual a Tasa Compra", "1002501"),
                    ev(TIPO, 55501L, "MONTO_PROXIMO_TASA_COMPRA", "Monto Proximo a Tasa Compra", "1002667"),
                    ev(TIPO, 55501L, "INTERES_NEGATIVO_DIARIO_TASA_COMPRA", "Interes Diario a Tasa Compra", "-3"),
                    ev(TIPO, 55501L, "REAJUSTE_NEGATIVO_TASA_COMPRA", "Reajuste a Tasa Compra", "-1001"));
        }

        @Test
        @DisplayName("CONT-C-16: NULL interes/reajuste -> those events are dropped; the 3 unconditional ones remain")
        // Legacy: CentralizaDevengoCarteraPropiaLibreYGarantia_SelectEventos.sql:26,29,32,35
        void contC16_nullConditionalDropped() {
            CarteraValorizada c = cartera().montoInteres(null).montoReajuste(null).montoProximo(null).build();

            assertThat(calc.eventos(T, TIPO, c)).containsExactly(
                    ev(TIPO, 55501L, "MONTO_COMPRA", "Monto Compra", "1000001"),
                    ev(TIPO, 55501L, "MONTO_ACTUAL_TASA_COMPRA", "Monto Actual a Tasa Compra", "1002501"),
                    ev(TIPO, 55501L, "MONTO_PROXIMO_TASA_COMPRA", "Monto Proximo a Tasa Compra", null));
        }

        @ParameterizedTest(name = "CONT-C-17 clasificacion={0} included={1}")
        @CsvSource(value = {"RF, true", "IF, true", "RV, false", "rf, false", "NULL, false"}, nullValues = "NULL")
        @DisplayName("CONT-C-17: only IdClasificacionTipoInstrumento IN ('RF','IF') produce movement and events")
        // Legacy: CentralizaDevengoCarteraPropiaLibreYGarantia_SelectMovimientos.sql:22, _SelectEventos.sql:17
        void contC17_filterRfIf(String clasificacion, boolean included) {
            CarteraValorizada c = cartera().clasificacion(clasificacion).build();
            assertThat(calc.movimiento(T, TIPO, FECHA_CONTABLE, c).isPresent()).isEqualTo(included);
            assertThat(calc.eventos(T, TIPO, c).isEmpty()).isEqualTo(!included);
        }

        @Test
        @DisplayName("CONT-C-18: movement: IdMovimiento=IdCartera, CARTERARF, Numero=Linea=FolioCartera, Monto=ABS(MontoActual)")
        // Legacy: CentralizaDevengoCarteraPropiaLibreYGarantia_SelectMovimientos.sql:1-15
        void contC18_movimientoFields() {
            CarteraValorizada c = cartera().clasificacion("RF").montoActual("-0.01").build();

            assertThat(calc.movimiento(T, TIPO, FECHA_CONTABLE, c).orElseThrow())
                    .isEqualTo(new TabMovimientoRef(
                            TIPO, 55501L, "CARTERARF", ID_EMPRESA, 33001L, 33001L,
                            "2025-03-10", "11111111-1", 300L, 2025L, "02-2025",
                            new BigDecimal("0.01"), "CLP", ""));
        }
    }
}
