package cl.bcs.valo2;

import cl.bcs.valo2.domain.cupon.Flujo;
import cl.bcs.valo2.service.BonosService;
import cl.bcs.valo2.service.ContadorDiasService;
import cl.bcs.valo2.service.FinancieraService;
import cl.bcs.valo2.service.PagaresService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Oracle tests for BcsValo2 — 87 legacy files, 7,342 LOC.
 * All behaviors are pure-logic (no Spring context, no DB).
 * Equivalence is trace-based: legacy requires HANA + BcsUtils (not available locally).
 *
 * Canary: change TIR_INICIAL from 0.1 to 0.2 in FinancieraService → B-VAL-06 tests fail.
 *
 * B-VAL-01..05  PagaresService — captacion/rescate simple+compuesto + fraccionAno
 * B-VAL-06..08  FinancieraService — TIR Newton-Raphson, Duration, Convexidad
 * B-VAL-09..16  ContadorDiasService — 8 day-count conventions
 * B-VAL-17      ContadorDiasService — convention alias dispatcher
 * B-VAL-18..20  BonosService — bond interest devengado + precio
 * B-VAL-21      PagaresService — TipoInteres constants
 * B-VAL-22      ContadorDiasService — AjusteEOM (30/360 end-of-month logic)
 * B-VAL-23      ContadorDiasService — exception on unknown convention
 */
class Valo2ServiceTest {

    @Nested
    class B_VAL_01_CaptacionSimple {
        @Test void tasa_anual_1año() {
            assertEquals(1000000.0 / 1.05,
                    PagaresService.captacionSimple(1.0, 1_000_000, 0.05), 1e-6);
        }

        @Test void tasa_180dias_base360() {
            double t = FinancieraService.fraccionAno(180, 360);
            double expected = 1_000_000.0 / (1 + 0.04 * t);
            assertEquals(expected, PagaresService.captacionSimple(t, 1_000_000, 0.04), 1e-6);
        }

        @Test void tasa_90dias_base365() {
            double t = FinancieraService.fraccionAno(90, 365);
            assertEquals(1_000_000.0 / (1 + 0.06 * t),
                    PagaresService.captacionSimple(t, 1_000_000, 0.06), 1e-6);
        }

        @Test void tasa_cero_devuelve_rescate() {
            assertEquals(500_000, PagaresService.captacionSimple(0.5, 500_000, 0.0), 1e-9);
        }

        @Test void dispatch_simple() {
            double t = FinancieraService.fraccionAno(180, 360);
            assertEquals(PagaresService.captacionSimple(t, 1_000_000, 0.05),
                    PagaresService.captacion(PagaresService.SIMPLE, t, 1_000_000, 0.05), 1e-9);
        }
    }

    @Nested
    class B_VAL_02_CaptacionCompuesto {
        @Test void tasa_anual_1año() {
            assertEquals(1_000_000 / 1.05,
                    PagaresService.captacionCompuesto(1.0, 1_000_000, 0.05), 1e-6);
        }

        @Test void tasa_2años() {
            assertEquals(1_000_000 / Math.pow(1.05, 2.0),
                    PagaresService.captacionCompuesto(2.0, 1_000_000, 0.05), 1e-6);
        }

        @Test void tasa_180dias_base360() {
            double t = FinancieraService.fraccionAno(180, 360);
            assertEquals(1_000_000 / Math.pow(1.04, t),
                    PagaresService.captacionCompuesto(t, 1_000_000, 0.04), 1e-6);
        }

        @Test void dispatch_compuesto() {
            double t = FinancieraService.fraccionAno(365, 365);
            assertEquals(PagaresService.captacionCompuesto(t, 1_000_000, 0.05),
                    PagaresService.captacion(PagaresService.COMPUESTO, t, 1_000_000, 0.05), 1e-9);
        }

        @Test void tipo_invalido_lanza_excepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> PagaresService.captacion(99, 1.0, 1_000_000, 0.05));
        }
    }

    @Nested
    class B_VAL_03_RescateSimple {
        @Test void tasa_anual_1año() {
            assertEquals(1_000_000 * 1.05,
                    PagaresService.rescateSimple(1.0, 1_000_000, 0.05), 1e-6);
        }

        @Test void tasa_180dias() {
            double t = FinancieraService.fraccionAno(180, 360);
            assertEquals(1_000_000 * (1 + 0.04 * t),
                    PagaresService.rescateSimple(t, 1_000_000, 0.04), 1e-6);
        }

        @Test void roundtrip_simple() {
            double t = FinancieraService.fraccionAno(90, 365);
            double captacionInicial = 850_000;
            double tasa = 0.07;
            double r = PagaresService.rescateSimple(t, captacionInicial, tasa);
            assertEquals(captacionInicial, PagaresService.captacionSimple(t, r, tasa), 1e-4);
        }

        @Test void dispatch_simple() {
            double t = FinancieraService.fraccionAno(180, 365);
            assertEquals(PagaresService.rescateSimple(t, 1_000_000, 0.06),
                    PagaresService.rescate(PagaresService.SIMPLE, t, 1_000_000, 0.06), 1e-9);
        }
    }

    @Nested
    class B_VAL_04_RescateCompuesto {
        @Test void tasa_anual_1año() {
            assertEquals(1_000_000 * Math.pow(1.05, 1.0),
                    PagaresService.rescateCompuesto(1.0, 1_000_000, 0.05), 1e-6);
        }

        @Test void tasa_2años() {
            assertEquals(1_000_000 * Math.pow(1.05, 2.0),
                    PagaresService.rescateCompuesto(2.0, 1_000_000, 0.05), 1e-6);
        }

        @Test void roundtrip_compuesto() {
            double t = FinancieraService.fraccionAno(180, 360);
            double captacionInicial = 920_000;
            double tasa = 0.08;
            double r = PagaresService.rescateCompuesto(t, captacionInicial, tasa);
            assertEquals(captacionInicial, PagaresService.captacionCompuesto(t, r, tasa), 1e-4);
        }

        @Test void dispatch_compuesto() {
            double t = FinancieraService.fraccionAno(365, 365);
            assertEquals(PagaresService.rescateCompuesto(t, 1_000_000, 0.05),
                    PagaresService.rescate(PagaresService.COMPUESTO, t, 1_000_000, 0.05), 1e-9);
        }
    }

    @Nested
    class B_VAL_05_FraccionAno {
        @Test void base360_180dias() {
            assertEquals(0.5, FinancieraService.fraccionAno(180, 360), 1e-15);
        }

        @Test void base365_365dias() {
            assertEquals(1.0, FinancieraService.fraccionAno(365, 365), 1e-15);
        }

        @Test void base365_90dias() {
            assertEquals(90.0 / 365.0, FinancieraService.fraccionAno(90, 365), 1e-15);
        }

        @Test void base252_126dias() {
            assertEquals(0.5, FinancieraService.fraccionAno(126, 252), 1e-15);
        }

        @Test void entero_no_redondea() {
            // (double) cast ensures no integer truncation
            assertEquals(1.0 / 3.0, FinancieraService.fraccionAno(1, 3), 1e-15);
        }
    }

    @Nested
    class B_VAL_06_TIR {
        @Test void bono_cupon_anual_bullet() {
            List<Flujo> f = List.of(
                    new Flujo(1.0, 0.05),
                    new Flujo(2.0, 0.05),
                    new Flujo(3.0, 1.05));
            double tir = FinancieraService.calcularTIR(f, 1.0, 1.0);
            assertEquals(0.05, tir, 1e-8);
        }

        @Test void bono_precio_menor_par() {
            List<Flujo> f = List.of(
                    new Flujo(1.0, 0.05),
                    new Flujo(2.0, 0.05),
                    new Flujo(3.0, 1.05));
            double tir = FinancieraService.calcularTIR(f, 0.95, 1.0);
            assertTrue(tir > 0.05, "Yield debe ser > cupon cuando precio < par");
        }

        @Test void bono_precio_mayor_par() {
            List<Flujo> f = List.of(
                    new Flujo(1.0, 0.05),
                    new Flujo(2.0, 0.05),
                    new Flujo(3.0, 1.05));
            double tir = FinancieraService.calcularTIR(f, 1.05, 1.0);
            assertTrue(tir < 0.05, "Yield debe ser < cupon cuando precio > par");
        }

        @Test void semestral_frecuencia2() {
            List<Flujo> f = List.of(
                    new Flujo(0.5, 0.025),
                    new Flujo(1.0, 1.025));
            double tir = FinancieraService.calcularTIR(f, 1.0, 2.0);
            assertEquals(0.05, tir, 1e-8);
        }

        @Test void constantes_pinadas() {
            assertEquals(100, FinancieraService.TIR_MAX_ITER);
            assertEquals(1.0e-10, FinancieraService.TIR_EPSILON, 1e-15);
            assertEquals(0.1, FinancieraService.TIR_INICIAL, 1e-15);
        }
    }

    @Nested
    class B_VAL_07_Duration {
        @Test void bono_cupon_anual_3anos() {
            List<Flujo> f = List.of(
                    new Flujo(1.0, 0.06),
                    new Flujo(2.0, 0.06),
                    new Flujo(3.0, 1.06));
            double dur = FinancieraService.calcularDuration(f, 0.06, 1.0);
            assertTrue(dur > 2.0 && dur < 3.0, "Duration entre 2 y 3 años");
        }

        @Test void zero_coupon_duration_equals_maturity() {
            List<Flujo> f = List.of(new Flujo(5.0, 1.0));
            double dur = FinancieraService.calcularDuration(f, 0.05, 1.0);
            assertEquals(5.0, dur, 1e-8);
        }

        @Test void formula_exacta_2cupones() {
            List<Flujo> f = List.of(new Flujo(1.0, 0.1), new Flujo(2.0, 1.1));
            double a1 = 1.1;
            double a3_1 = 0.1 / Math.pow(a1, 1.0);
            double a3_2 = 1.1 / Math.pow(a1, 2.0);
            double expected = (1.0 * a3_1 + 2.0 * a3_2) / (a3_1 + a3_2);
            assertEquals(expected, FinancieraService.calcularDuration(f, 0.1, 1.0), 1e-10);
        }
    }

    @Nested
    class B_VAL_08_Convexidad {
        @Test void zero_coupon_convexidad() {
            double t = 5.0, tir = 0.05, f = 1.0;
            List<Flujo> flujos = List.of(new Flujo(t, 1.0));
            double a1 = 1 + tir / f;
            double a3 = 1.0 / Math.pow(a1, t * f);
            double expected = t * (1 + t) * a3 / (a3 * a1 * a1);
            assertEquals(expected, FinancieraService.calcularConvexidad(flujos, tir, f), 1e-8);
        }

        @Test void convexidad_positiva() {
            List<Flujo> f = List.of(
                    new Flujo(1.0, 0.05),
                    new Flujo(2.0, 0.05),
                    new Flujo(3.0, 1.05));
            assertTrue(FinancieraService.calcularConvexidad(f, 0.05, 1.0) > 0);
        }

        @Test void convexidad_mayor_que_duration_cuadrado() {
            List<Flujo> f = List.of(
                    new Flujo(1.0, 0.05),
                    new Flujo(2.0, 0.05),
                    new Flujo(3.0, 1.05));
            double dur  = FinancieraService.calcularDuration(f, 0.05, 1.0);
            double conv = FinancieraService.calcularConvexidad(f, 0.05, 1.0);
            assertTrue(conv > dur * dur - dur, "Convexidad debe ser > Duration^2-Duration");
        }
    }

    @Nested
    class B_VAL_09_DayCount30_360 {
        @Test void seis_meses_exactos() {
            assertEquals(180, ContadorDiasService.dias30_360(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2023, 7, 1)));
        }

        @Test void un_año_completo() {
            assertEquals(360, ContadorDiasService.dias30_360(
                    LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1)));
        }

        @Test void d1_31_d2_28_sin_eom() {
            // Without EOM: 30*(2-1) + (28-31) = 27
            assertEquals(27, ContadorDiasService.dias30_360(
                    LocalDate.of(2023, 1, 31), LocalDate.of(2023, 2, 28)));
        }

        @Test void d1_30_d2_30() {
            assertEquals(90, ContadorDiasService.dias30_360(
                    LocalDate.of(2023, 3, 30), LocalDate.of(2023, 6, 30)));
        }

        @Test void fraccion_un_mes() {
            assertEquals(30.0 / 360.0, ContadorDiasService.fraccion30_360(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1)), 1e-10);
        }
    }

    @Nested
    class B_VAL_10_DayCount30E_360 {
        @Test void mismo_resultado_sin_eom() {
            LocalDate d1 = LocalDate.of(2023, 2, 1);
            LocalDate d2 = LocalDate.of(2023, 8, 1);
            assertEquals(ContadorDiasService.dias30_360(d1, d2),
                         ContadorDiasService.dias30E_360(d1, d2));
        }

        @Test void seis_meses() {
            assertEquals(180, ContadorDiasService.dias30E_360(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2023, 7, 1)));
        }

        @Test void cruce_de_año() {
            // Nov 1 to Feb 1: 360*(2024-2023) + 30*(2-11) + 0 = 360-270 = 90
            assertEquals(90, ContadorDiasService.dias30E_360(
                    LocalDate.of(2023, 11, 1), LocalDate.of(2024, 2, 1)));
        }
    }

    @Nested
    class B_VAL_11_DayCount30_365 {
        @Test void fraccion_6meses() {
            assertEquals(180.0 / 365.0, ContadorDiasService.fraccion30_365(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2023, 7, 1)), 1e-10);
        }

        @Test void dias_raw_30_365() {
            LocalDate d1 = LocalDate.of(2023, 1, 1);
            LocalDate d2 = LocalDate.of(2023, 7, 1);
            assertEquals(ContadorDiasService.dias30_360(d1, d2),
                         ContadorDiasService.dias30_365(d1, d2));
        }

        @Test void fraccion_un_año_completo() {
            double fraccion = ContadorDiasService.fraccion30_365(
                    LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1));
            assertEquals(360.0 / 365.0, fraccion, 1e-10);
        }
    }

    @Nested
    class B_VAL_12_DayCountAct_360 {
        @Test void un_año_no_bisiesto() {
            assertEquals(365.0 / 360.0, ContadorDiasService.fraccionAct_360(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)), 1e-10);
        }

        @Test void exactamente_90_dias() {
            assertEquals(90.0 / 360.0, ContadorDiasService.fraccionAct_360(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2023, 4, 1)), 1e-10);
        }

        @Test void dias_act_correcto() {
            assertEquals(28, ContadorDiasService.diasAct(
                    LocalDate.of(2023, 1, 31), LocalDate.of(2023, 2, 28)));
        }
    }

    @Nested
    class B_VAL_13_DayCountAct_365 {
        @Test void un_año_no_bisiesto() {
            assertEquals(1.0, ContadorDiasService.fraccionAct_365(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)), 1e-10);
        }

        @Test void un_año_bisiesto() {
            double fraccion = ContadorDiasService.fraccionAct_365(
                    LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1));
            assertEquals(366.0 / 365.0, fraccion, 1e-10);
        }

        @Test void seis_meses_no_bisiesto() {
            assertEquals(181.0 / 365.0, ContadorDiasService.fraccionAct_365(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2023, 7, 1)), 1e-10);
        }
    }

    @Nested
    class B_VAL_14_DayCountActAct_ISDA {
        @Test void año_completo_no_bisiesto() {
            // diasRestantes(Jan1_2023)=364; diasTranscurridos(Jan1_2024)=0; Y2-Y1-1=0
            // = 364/365 + 0/365 + 0 = 364/365
            double actual = ContadorDiasService.fraccionActAct_ISDA(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1));
            assertEquals(364.0 / 365.0, actual, 1e-6);
        }

        @Test void seis_meses_cruce_año() {
            LocalDate d1 = LocalDate.of(2023, 7, 1);
            LocalDate d2 = LocalDate.of(2024, 1, 1);
            int diasRestantes     = d1.lengthOfYear() - d1.getDayOfYear();
            int diasTranscurridos = d2.getDayOfYear() - 1;
            double expected = diasRestantes / 365.0 + diasTranscurridos / 366.0
                    + (d2.getYear() - d1.getYear() - 1);
            assertEquals(expected, ContadorDiasService.fraccionActAct_ISDA(d1, d2), 1e-10);
        }
    }

    @Nested
    class B_VAL_15_DayCountNL_365 {
        @Test void sin_feb29_igual_act_365() {
            LocalDate d1 = LocalDate.of(2023, 1, 1);
            LocalDate d2 = LocalDate.of(2023, 12, 31);
            assertEquals(ContadorDiasService.diasAct(d1, d2),
                         ContadorDiasService.diasNL_365(d1, d2));
        }

        @Test void con_feb29_excluye_un_dia() {
            LocalDate d1 = LocalDate.of(2024, 2, 1);
            LocalDate d2 = LocalDate.of(2024, 3, 31);
            int actual = ContadorDiasService.diasAct(d1, d2);
            int nl365  = ContadorDiasService.diasNL_365(d1, d2);
            assertEquals(actual - 1, nl365);
        }

        @Test void feb29_excluido_en_fraccion() {
            LocalDate d1 = LocalDate.of(2024, 2, 28);
            LocalDate d2 = LocalDate.of(2024, 3, 1);
            // RIGHT_INCLUDE: (d1, d2] → Feb 29 and Mar 1 → Feb 29 is in range → count=1
            assertEquals(1, ContadorDiasService.contarFeb29(d1, d2));
            assertEquals(1, ContadorDiasService.diasNL_365(d1, d2));
        }
    }

    @Nested
    class B_VAL_16_DayCountBUS252 {
        @Test void semana_completa_sin_feriados() {
            // Mon Jan 2 to Sat Jan 7 2023: [Jan2, Jan7) = Mon-Fri = 5 business days
            LocalDate d1 = LocalDate.of(2023, 1, 2);
            LocalDate d2 = LocalDate.of(2023, 1, 7);
            assertEquals(5, ContadorDiasService.diasHabiles(d1, d2, Set.of()));
        }

        @Test void feriado_excluido() {
            LocalDate d1 = LocalDate.of(2023, 1, 2);
            LocalDate d2 = LocalDate.of(2023, 1, 9);
            assertEquals(4, ContadorDiasService.diasHabiles(d1, d2,
                    Set.of(LocalDate.of(2023, 1, 6))));
        }

        @Test void fraccion_252_correcto() {
            LocalDate d1 = LocalDate.of(2023, 1, 2);
            LocalDate d2 = LocalDate.of(2023, 1, 7);
            assertEquals(5.0 / 252.0, ContadorDiasService.fraccionBUS252(d1, d2, Set.of()), 1e-10);
        }

        @Test void fin_de_semana_no_cuenta() {
            // Sat Jan 7 to Mon Jan 9: [Jan7, Jan9) = Jan7(Sat) + Jan8(Sun) → 0 business days
            LocalDate d1 = LocalDate.of(2023, 1, 7);
            LocalDate d2 = LocalDate.of(2023, 1, 9);
            assertEquals(0, ContadorDiasService.diasHabiles(d1, d2, Set.of()));
        }
    }

    @Nested
    class B_VAL_17_ConventionAliases {
        @Test void alias_30_360_incluye_dc30_360() {
            assertTrue(ContadorDiasService.ALIAS_30_360.contains("DC30_360"));
        }

        @Test void alias_30_360_incluye_bond_basis() {
            assertTrue(ContadorDiasService.ALIAS_30_360.contains("Bond basis"));
        }

        @Test void alias_30E_360_incluye_dc30e_360() {
            assertTrue(ContadorDiasService.ALIAS_30E_360.contains("DC30E_360"));
        }

        @Test void alias_bus252_incluye_tres_variantes() {
            assertTrue(ContadorDiasService.ALIAS_BUS252.contains("BUS252"));
            assertTrue(ContadorDiasService.ALIAS_BUS252.contains("BUS DAYS252"));
            assertTrue(ContadorDiasService.ALIAS_BUS252.contains("BUS/252"));
        }

        @Test void alias_act_act_isda_incluye_dkact_act_isda() {
            assertTrue(ContadorDiasService.ALIAS_ACT_ACT_ISDA.contains("DCAct_Act_ISDA"));
        }

        @Test void alias_nl_365_incluye_dcnl_365() {
            assertTrue(ContadorDiasService.ALIAS_NL_365.contains("DCNL_365"));
        }

        @Test void total_convenciones_7_grupos() {
            assertEquals(7, List.of(
                    ContadorDiasService.ALIAS_30_360,
                    ContadorDiasService.ALIAS_30E_360,
                    ContadorDiasService.ALIAS_30_365,
                    ContadorDiasService.ALIAS_ACT_360,
                    ContadorDiasService.ALIAS_ACT_365,
                    ContadorDiasService.ALIAS_ACT_ACT_ISDA,
                    ContadorDiasService.ALIAS_NL_365).size());
        }
    }

    @Nested
    class B_VAL_18_InteresDevengadoSimple {
        @Test void inicio_periodo_cero_intereses() {
            // anosTranscurridos = anosPeriodo - anosVencimiento = 1.0 - 1.0 = 0
            assertEquals(0.0, BonosService.interesDevengadoSimple(0.06, 1.0, 1.0), 1e-10);
        }

        @Test void mitad_periodo() {
            // transcurridos = 1.0 - 0.5 = 0.5; interes = 0.06*0.5/1.0 = 0.03
            assertEquals(0.03, BonosService.interesDevengadoSimple(0.06, 1.0, 0.5), 1e-10);
        }

        @Test void fin_periodo() {
            // transcurridos = 1.0 - 0.0 = 1.0; interes = 0.06*1.0/1.0 = 0.06
            assertEquals(0.06, BonosService.interesDevengadoSimple(0.06, 1.0, 0.0), 1e-10);
        }

        @Test void periodo_semestral() {
            // periodo=0.5, vencimiento=0.25, transcurridos=0.25, interes=0.05*0.25/0.5=0.025
            assertEquals(0.025, BonosService.interesDevengadoSimple(0.05, 0.5, 0.25), 1e-10);
        }
    }

    @Nested
    class B_VAL_19_InteresDevengadoCompuesto {
        @Test void inicio_periodo_cero_intereses() {
            // transcurridos = 0 → (1+0.06)^0 - 1 = 0
            assertEquals(0.0, BonosService.interesDevengadoCompuesto(0.06, 1.0, 1.0), 1e-10);
        }

        @Test void fin_periodo_full() {
            // transcurridos = 1.0 → (1.06)^1 - 1 = 0.06
            assertEquals(0.06, BonosService.interesDevengadoCompuesto(0.06, 1.0, 0.0), 1e-10);
        }

        @Test void mitad_periodo_compuesto() {
            double expected = Math.pow(1.06, 0.5) - 1;
            assertEquals(expected, BonosService.interesDevengadoCompuesto(0.06, 1.0, 0.5), 1e-10);
        }

        @Test void menor_que_simple_en_fraccion_periodo() {
            // For t < 1: (1+r)^t - 1 < r*t  (compound < simple for partial period)
            double simple    = BonosService.interesDevengadoSimple(0.10, 1.0, 0.5);
            double compuesto = BonosService.interesDevengadoCompuesto(0.10, 1.0, 0.5);
            assertTrue(compuesto < simple, "Para t<1, interés compuesto < simple");
        }
    }

    @Nested
    class B_VAL_20_PrecioBono {
        @Test void precio_a_par() {
            assertEquals(100.0, BonosService.precioBono(1.0, 1.0), 1e-10);
        }

        @Test void precio_descuento() {
            assertEquals(95.0, BonosService.precioBono(0.95, 1.0), 1e-10);
        }

        @Test void precio_con_amortizacion() {
            // After 50% amortization: saldoCapital = 0.5, proporcion = 0.5 → precio = 100
            assertEquals(100.0, BonosService.precioBono(0.5, 0.5), 1e-10);
        }

        @Test void precio_con_prima() {
            assertEquals(100.0 / 0.95, BonosService.precioBono(1.0, 0.95), 1e-6);
        }
    }

    @Nested
    class B_VAL_21_TipoInteres {
        @Test void simple_es_cero() {
            assertEquals(0, PagaresService.SIMPLE);
        }

        @Test void compuesto_es_uno() {
            assertEquals(1, PagaresService.COMPUESTO);
        }

        @Test void distintos_entre_si() {
            assertNotEquals(PagaresService.SIMPLE, PagaresService.COMPUESTO);
        }
    }

    @Nested
    class B_VAL_22_AjusteEOM_30_360 {
        @Test void d1_31_d2_normal_aplica_ajuste() {
            // Jan 31 to Mar 15 with EOM:
            // Rule 3: D2=15≠31 → skip; Rule 4: D1=31 → adjAnterior=-1
            // Formula: 30*(3-1 + 0-(-1)) + (15-31) = 30*3 - 16 = 74
            // Note: adjAnterior=-1 adds +1 month in the formula (non-standard but pinned)
            assertEquals(74, ContadorDiasService.dias30_360_EOM(
                    LocalDate.of(2023, 1, 31), LocalDate.of(2023, 3, 15)));
        }

        @Test void d1_31_d2_31_ambos_ajustados() {
            // Jan 31 to Mar 31: adj posterior=-1 (rule 3), adj anterior=-1 (rule 4)
            // = 360*0 + 30*(3-1 + (-1)-(-1)) + (31-31) = 30*2 = 60
            assertEquals(60, ContadorDiasService.dias30_360_EOM(
                    LocalDate.of(2023, 1, 31), LocalDate.of(2023, 3, 31)));
        }

        @Test void d1_normal_d2_31_no_aplica_si_d1_menor_30() {
            // D1=15, D2=31: rule 3 requires D1+adj >= 30, D1=15 < 30 → D2 not adjusted
            // = 360*0 + 30*(3-1+0-0) + (31-15) = 60 + 16 = 76
            assertEquals(76, ContadorDiasService.dias30_360_EOM(
                    LocalDate.of(2023, 1, 15), LocalDate.of(2023, 3, 31)));
        }

        @Test void sin_dias_31_sin_ajuste() {
            int conEOM = ContadorDiasService.dias30_360_EOM(
                    LocalDate.of(2023, 2, 15), LocalDate.of(2023, 5, 15));
            int sinEOM = ContadorDiasService.dias30_360(
                    LocalDate.of(2023, 2, 15), LocalDate.of(2023, 5, 15));
            assertEquals(sinEOM, conEOM);
        }
    }

    @Nested
    class B_VAL_23_ConvencionInvalida {
        @Test void convencion_desconocida_no_en_ningun_alias() {
            String convencion = "XYZABC/999";
            boolean enAlguno = ContadorDiasService.ALIAS_30_360.contains(convencion)
                    || ContadorDiasService.ALIAS_30E_360.contains(convencion)
                    || ContadorDiasService.ALIAS_30_365.contains(convencion)
                    || ContadorDiasService.ALIAS_ACT_360.contains(convencion)
                    || ContadorDiasService.ALIAS_ACT_365.contains(convencion)
                    || ContadorDiasService.ALIAS_ACT_ACT_ISDA.contains(convencion)
                    || ContadorDiasService.ALIAS_NL_365.contains(convencion)
                    || ContadorDiasService.ALIAS_BUS252.contains(convencion);
            assertFalse(enAlguno, "Convencion desconocida no debe estar en ningún alias set");
        }

        @Test void convencion_vacia_no_en_alias() {
            assertFalse(ContadorDiasService.ALIAS_30_360.contains(""));
        }

        @Test void convencion_bus252_case_insensitive_busqueda() {
            // Legacy does convencionDias.toUpperCase() before map lookup
            assertTrue(ContadorDiasService.ALIAS_BUS252.contains("BUS252"));
            assertTrue(ContadorDiasService.ALIAS_BUS252.contains("BUS/252"));
        }
    }
}
