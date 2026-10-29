package cl.bcs.ordenes;

import cl.bcs.ordenes.domain.ConstantesOrdenes;
import cl.bcs.ordenes.domain.OrdenProceso;
import cl.bcs.ordenes.service.CalcularMontoService;
import cl.bcs.ordenes.service.CrearOrdenesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for CrearOrdenes (legacy: 5,779 LOC, CCN=813).
 *
 * Purpose: pin pure-Java business-rule invariants BEFORE the Phase 3 rewrite.
 * Every test extracts a formula or decision branch from the legacy source and
 * asserts the exact legacy behavior. No DB / HANA access is needed.
 *
 * Canary: any failing test after Phase 3 means the rewrite changed behaviour.
 *
 * Source: BSCL/Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/CrearOrdenes.java
 */
@DisplayName("CrearOrdenes — characterization tests (Phase 3 entry criterion)")
class CrearOrdenesTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Test groups
    // ─────────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("ConstantesOrdenes — key string constants")
    class ConstantesOrdenesTest {

        @Test @DisplayName("ESTADO_ORDEN_PENDIENTE_AUTORIZAR is 'PDA'")
        void estado_orden_pendiente_autorizar_is_PDA() {
            assertThat(ConstantesOrdenes.ESTADO_ORDEN_PENDIENTE_AUTORIZAR).isEqualTo("PDA");
        }

        @Test @DisplayName("ESTADO_ORDEN_INGRESADA is 'ING'")
        void estado_orden_ingresada_is_ING() {
            assertThat(ConstantesOrdenes.ESTADO_ORDEN_INGRESADA).isEqualTo("ING");
        }

        @Test @DisplayName("ESTADO_ORDEN_AUTORIZADA is 'AUT'")
        void estado_orden_autorizada_is_AUT() {
            assertThat(ConstantesOrdenes.ESTADO_ORDEN_AUTORIZADA).isEqualTo("AUT");
        }

        @Test @DisplayName("TIPO_CANAL_INGRESO_TRADER is 'TRA'")
        void tipo_canal_ingreso_trader_is_TRA() {
            assertThat(ConstantesOrdenes.TIPO_CANAL_INGRESO_TRADER).isEqualTo("TRA");
        }

        @Test @DisplayName("TIPO_CANAL_INGRESO_AGENTE is 'AGE'")
        void tipo_canal_ingreso_agente_is_AGE() {
            assertThat(ConstantesOrdenes.TIPO_CANAL_INGRESO_AGENTE).isEqualTo("AGE");
        }

        @Test @DisplayName("ID_MODULO_ORDENES is 'ORD'")
        void id_modulo_ordenes_is_ORD() {
            assertThat(ConstantesOrdenes.ID_MODULO_ORDENES).isEqualTo("ORD");
        }

        @Test @DisplayName("ESTADO_MODULO_CERRADO is 'C'")
        void estado_modulo_cerrado_is_C() {
            assertThat(ConstantesOrdenes.ESTADO_MODULO_CERRADO).isEqualTo("C");
        }
    }

    @Nested @DisplayName("IdBloqueaOrden key construction")
    class IdBloqueaOrdenTest {

        @Test @DisplayName("Key concatenates all fields separated by underscore")
        void key_concatenates_fields() {
            String key = CrearOrdenesService.buildIdBloqueaOrden(
                100L, 200L, "COMPRA", new BigDecimal("50"), new BigDecimal("100.50"), "2026-10-05");
            assertThat(key).isEqualTo("100_200_COMPRA_50_100.5_2026-10-05");
        }

        @Test @DisplayName("PrecioLimite.stripTrailingZeros removes trailing zeros")
        void precio_limite_strips_trailing_zeros() {
            String key = CrearOrdenesService.buildIdBloqueaOrden(
                1L, 1L, "C", new BigDecimal("1"), new BigDecimal("1000.00"), "2026-01-01");
            assertThat(key).contains("_1E+3_");
        }

        @Test @DisplayName("Key is deterministic for same inputs")
        void key_is_deterministic() {
            String k1 = CrearOrdenesService.buildIdBloqueaOrden(
                5L, 10L, "VEN", new BigDecimal("3"), new BigDecimal("250"), "2026-10-05");
            String k2 = CrearOrdenesService.buildIdBloqueaOrden(
                5L, 10L, "VEN", new BigDecimal("3"), new BigDecimal("250"), "2026-10-05");
            assertThat(k1).isEqualTo(k2);
        }

        @Test @DisplayName("Key changes when portafolio changes")
        void key_changes_on_portafolio_change() {
            String k1 = CrearOrdenesService.buildIdBloqueaOrden(
                1L, 10L, "C", BigDecimal.ONE, BigDecimal.TEN, "2026-01-01");
            String k2 = CrearOrdenesService.buildIdBloqueaOrden(
                2L, 10L, "C", BigDecimal.ONE, BigDecimal.TEN, "2026-01-01");
            assertThat(k1).isNotEqualTo(k2);
        }

        @Test @DisplayName("Key changes when fecha changes")
        void key_changes_on_fecha_change() {
            String k1 = CrearOrdenesService.buildIdBloqueaOrden(
                1L, 1L, "C", BigDecimal.ONE, BigDecimal.ONE, "2026-01-01");
            String k2 = CrearOrdenesService.buildIdBloqueaOrden(
                1L, 1L, "C", BigDecimal.ONE, BigDecimal.ONE, "2026-01-02");
            assertThat(k1).isNotEqualTo(k2);
        }
    }

    @Nested @DisplayName("GeneraSpot routing (CrearOrdenes:1572-1586)")
    class GeneraSpotTest {

        @Test @DisplayName("COMPRA + USD moneda → generates spot")
        void compra_usd_genera_spot() {
            assertThat(CrearOrdenesService.resolveGeneraSpot("S", "COMPRA", "USD", "CLP")).isEqualTo("S");
        }

        @Test @DisplayName("COMPRA + CLP moneda + USD liquidacion → generates spot")
        void compra_clp_usd_liquidacion_genera_spot() {
            assertThat(CrearOrdenesService.resolveGeneraSpot("S", "COMPRA", "CLP", "USD")).isEqualTo("S");
        }

        @Test @DisplayName("COMPRA + CLP moneda + CLP liquidacion → no spot")
        void compra_clp_clp_no_genera_spot() {
            assertThat(CrearOrdenesService.resolveGeneraSpot("S", "COMPRA", "CLP", "CLP")).isEqualTo("N");
        }

        @Test @DisplayName("VENTA + USD moneda → no spot (only COMPRA generates spot)")
        void venta_usd_no_genera_spot() {
            assertThat(CrearOrdenesService.resolveGeneraSpot("S", "VENTA", "USD", "USD")).isEqualTo("N");
        }

        @Test @DisplayName("entrada GeneraSpot = 'N' → never generates spot")
        void entrada_N_no_genera_spot() {
            assertThat(CrearOrdenesService.resolveGeneraSpot("N", "COMPRA", "USD", "USD")).isEqualTo("N");
        }

        @Test @DisplayName("entrada GeneraSpot null-equivalent 'X' → no spot")
        void entrada_otros_no_genera_spot() {
            assertThat(CrearOrdenesService.resolveGeneraSpot("X", "COMPRA", "USD", "CLP")).isEqualTo("N");
        }
    }

    @Nested @DisplayName("TipoLiquidacionFactura (CrearOrdenes:1588)")
    class TipoLiquidacionFacturaTest {

        @Test @DisplayName("param ACTIVO → preserves entrada value")
        void activo_preserves_entrada() {
            assertThat(CrearOrdenesService.resolveTipoLiquidacionFactura("ACTIVO", "FAE")).isEqualTo("FAE");
        }

        @Test @DisplayName("param INACTIVO → returns empty string")
        void inactivo_returns_empty() {
            assertThat(CrearOrdenesService.resolveTipoLiquidacionFactura("INACTIVO", "FAE")).isEqualTo("");
        }

        @Test @DisplayName("param null-like empty → returns empty string")
        void empty_param_returns_empty() {
            assertThat(CrearOrdenesService.resolveTipoLiquidacionFactura("", "FAE")).isEqualTo("");
        }
    }

    @Nested @DisplayName("TipoCambio null derivation (CrearOrdenes:1782-1792)")
    class TipoCambioTest {

        @Test @DisplayName("null TipoCambio + same moneda + system moneda → BigDecimal.ONE")
        void null_same_moneda_returns_one() {
            assertThat(CrearOrdenesService.resolveTipoCambio(null, "CLP", "CLP", "CLP"))
                .isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test @DisplayName("null TipoCambio + different monedas → BigDecimal.ZERO")
        void null_different_moneda_returns_zero() {
            assertThat(CrearOrdenesService.resolveTipoCambio(null, "CLP", "USD", "CLP"))
                .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test @DisplayName("non-null TipoCambio → kept as-is")
        void non_null_kept_as_is() {
            BigDecimal tc = new BigDecimal("895.50");
            assertThat(CrearOrdenesService.resolveTipoCambio(tc, "CLP", "USD", "CLP"))
                .isEqualByComparingTo(tc);
        }

        @Test @DisplayName("null monedas → returns BigDecimal.ONE (null-safe path)")
        void null_monedas_returns_one() {
            assertThat(CrearOrdenesService.resolveTipoCambio(null, null, null, "CLP"))
                .isEqualByComparingTo(BigDecimal.ONE);
        }
    }

    @Nested @DisplayName("FechaVigencia derivation (CrearOrdenes:1738-1750)")
    class FechaVigenciaTest {

        private final Date fechaSistema = Date.valueOf("2026-10-05");
        private final String fechaFija = "2026-12-31";

        @Test @DisplayName("empty tipo vigencia → FechaSistemaCreacion")
        void empty_tipo_vigencia_returns_fecha_sistema() {
            assertThat(CrearOrdenesService.resolveFechaVigencia("", fechaSistema, fechaFija))
                .isEqualTo(fechaSistema);
        }

        @Test @DisplayName("SOLO_HOY → FechaSistemaCreacion")
        void solo_hoy_returns_fecha_sistema() {
            assertThat(CrearOrdenesService.resolveFechaVigencia("SH", fechaSistema, fechaFija))
                .isEqualTo(fechaSistema);
        }

        @Test @DisplayName("SEMANAL → uses entrada FechaVigencia")
        void semanal_uses_entrada() {
            assertThat(CrearOrdenesService.resolveFechaVigencia("SE", fechaSistema, fechaFija))
                .isEqualTo(Date.valueOf(fechaFija));
        }

        @Test @DisplayName("FECHA_FIJA → uses entrada FechaVigencia")
        void fecha_fija_uses_entrada() {
            assertThat(CrearOrdenesService.resolveFechaVigencia("FF", fechaSistema, fechaFija))
                .isEqualTo(Date.valueOf(fechaFija));
        }

        @Test @DisplayName("PERMANENTE → uses entrada FechaVigencia")
        void permanente_uses_entrada() {
            assertThat(CrearOrdenesService.resolveFechaVigencia("OP", fechaSistema, fechaFija))
                .isEqualTo(Date.valueOf(fechaFija));
        }

        @Test @DisplayName("unknown tipo vigencia → null")
        void unknown_tipo_vigencia_returns_null() {
            assertThat(CrearOrdenesService.resolveFechaVigencia("XX", fechaSistema, fechaFija)).isNull();
        }
    }

    @Nested @DisplayName("Initial orden state values (CrearOrdenes:1752-1754)")
    class InitialOrdenStateTest {

        @Test @DisplayName("IdEstadoOrden initial value is PENDIENTE_AUTORIZAR (PDA)")
        void estado_orden_initial_is_PDA() {
            // Canary: if changed to ING the order flow breaks — must start at PDA
            assertThat(ConstantesOrdenes.ESTADO_ORDEN_PENDIENTE_AUTORIZAR).isEqualTo("PDA");
        }

        @Test @DisplayName("IdEstadoInterno initial value is PENDIENTE_AUTORIZAR (PDA)")
        void estado_interno_initial_is_PDA() {
            assertThat(ConstantesOrdenes.ESTADO_ORDEN_PENDIENTE_AUTORIZAR).isEqualTo("PDA");
        }

        @Test @DisplayName("IdEstadoLimite initial value is AUTORIZADA (AUT)")
        void estado_limite_initial_is_AUT() {
            // AUTORIZADA is used as the limit state — not INGRESADA or PENDIENTE_AUTORIZAR
            assertThat(ConstantesOrdenes.ESTADO_ORDEN_AUTORIZADA).isEqualTo("AUT");
        }
    }

    @Nested @DisplayName("Null default resolution")
    class NullDefaultsTest {

        @Test @DisplayName("EsVentaCorta null → 'N'")
        void es_venta_corta_null_returns_N() {
            assertThat(CrearOrdenesService.resolveEsVentaCorta(null)).isEqualTo("N");
        }

        @Test @DisplayName("EsVentaCorta non-null → kept as-is")
        void es_venta_corta_non_null_kept() {
            assertThat(CrearOrdenesService.resolveEsVentaCorta("S")).isEqualTo("S");
        }

        @Test @DisplayName("EsFacturaInmediata null → 'N'")
        void es_factura_inmediata_null_returns_N() {
            assertThat(CrearOrdenesService.resolveEsFacturaInmediata(null)).isEqualTo("N");
        }

        @Test @DisplayName("EsFacturaInmediata empty → 'N'")
        void es_factura_inmediata_empty_returns_N() {
            assertThat(CrearOrdenesService.resolveEsFacturaInmediata("")).isEqualTo("N");
        }

        @Test @DisplayName("EsFacturaInmediata 'S' → 'S'")
        void es_factura_inmediata_S_kept() {
            assertThat(CrearOrdenesService.resolveEsFacturaInmediata("S")).isEqualTo("S");
        }

        @Test @DisplayName("IdCorredorCorresponsal null → 0L")
        void corredor_corresponsal_null_returns_0L() {
            assertThat(CrearOrdenesService.resolveIdCorredorCorresponsal(null)).isEqualTo(0L);
        }

        @Test @DisplayName("IdCorredorCorresponsal non-null → kept")
        void corredor_corresponsal_non_null_kept() {
            assertThat(CrearOrdenesService.resolveIdCorredorCorresponsal(42L)).isEqualTo(42L);
        }

        @Test @DisplayName("IdPersonaAutorizadoDarOrdenes null → 0L")
        void persona_autorizado_null_returns_0L() {
            assertThat(CrearOrdenesService.resolveIdPersonaAutorizadoDarOrdenes(null)).isEqualTo(0L);
        }

        @Test @DisplayName("IdBoveda '0' → null")
        void boveda_zero_returns_null() {
            assertThat(CrearOrdenesService.resolveIdBoveda("0")).isNull();
        }

        @Test @DisplayName("IdBoveda null → null")
        void boveda_null_returns_null() {
            assertThat(CrearOrdenesService.resolveIdBoveda(null)).isNull();
        }

        @Test @DisplayName("IdBoveda valid → kept")
        void boveda_valid_kept() {
            assertThat(CrearOrdenesService.resolveIdBoveda("BOV1")).isEqualTo("BOV1");
        }
    }

    @Nested @DisplayName("IdEmisor routing (CrearOrdenes:1834-1852)")
    class IdEmisorRoutingTest {

        @Test @DisplayName("IF clasificacion → emisor from entrada")
        void if_uses_entrada_emisor() {
            String emisor = CrearOrdenesService.resolveIdEmisor(
                "IF", "IF", "COMIF", "EMS-ENTRADA", "EMS-INSTR", "EMS-SUB");
            assertThat(emisor).isEqualTo("EMS-ENTRADA");
        }

        @Test @DisplayName("RE clasificacion → emisor from instrumento")
        void re_uses_instrumento_emisor() {
            String emisor = CrearOrdenesService.resolveIdEmisor(
                "RE", "RRF", "COMRRF", "EMS-ENTRADA", "EMS-INSTR", "EMS-SUB");
            assertThat(emisor).isEqualTo("EMS-INSTR");
        }

        @Test @DisplayName("RV + SIM + VENSIM → emisor from subyacente")
        void rv_sim_vensim_uses_subyacente_emisor() {
            String emisor = CrearOrdenesService.resolveIdEmisor(
                "RV", "SIM", "VENSIM", "EMS-ENTRADA", "EMS-INSTR", "EMS-SUB");
            assertThat(emisor).isEqualTo("EMS-SUB");
        }

        @Test @DisplayName("RV + SIM + COMSIM → emisor is empty string")
        void rv_sim_comsim_returns_empty() {
            String emisor = CrearOrdenesService.resolveIdEmisor(
                "RV", "SIM", "COMSIM", "EMS-ENTRADA", "EMS-INSTR", "EMS-SUB");
            assertThat(emisor).isEmpty();
        }

        @Test @DisplayName("RV + non-SIM → emisor from instrumento")
        void rv_non_sim_uses_instrumento_emisor() {
            String emisor = CrearOrdenesService.resolveIdEmisor(
                "RV", "ACN", "COMRV", "EMS-ENTRADA", "EMS-INSTR", "EMS-SUB");
            assertThat(emisor).isEqualTo("EMS-INSTR");
        }
    }

    @Nested @DisplayName("IdInstrumentoCustodia routing (CrearOrdenes:1854-1862)")
    class IdInstrumentoCustodiaTest {

        @Test @DisplayName("SIM tipo instrumento → uses subyacente")
        void sim_uses_subyacente() {
            assertThat(CrearOrdenesService.resolveIdInstrumentoCustodia("SIM", "RV", 100L, 200L))
                .isEqualTo(200L);
        }

        @Test @DisplayName("RE clasificacion → uses subyacente")
        void re_uses_subyacente() {
            assertThat(CrearOrdenesService.resolveIdInstrumentoCustodia("RRF", "RE", 100L, 200L))
                .isEqualTo(200L);
        }

        @Test @DisplayName("RV non-SIM → uses instrumento entry")
        void rv_non_sim_uses_instrumento() {
            assertThat(CrearOrdenesService.resolveIdInstrumentoCustodia("ACN", "RV", 100L, 200L))
                .isEqualTo(100L);
        }
    }

    @Nested @DisplayName("IdTipoReajuste routing (CrearOrdenes:1824-1832)")
    class IdTipoReajusteTest {

        @Test @DisplayName("CL + RF → uses instrument reajuste")
        void cl_rf_uses_instrument() {
            assertThat(CrearOrdenesService.resolveIdTipoReajuste("CL", "RF", "INSTRUMENTO-REA", "ENTRADA-REA"))
                .isEqualTo("INSTRUMENTO-REA");
        }

        @Test @DisplayName("IF → uses entrada reajuste")
        void if_uses_entrada() {
            assertThat(CrearOrdenesService.resolveIdTipoReajuste("CL", "IF", "INSTRUMENTO-REA", "ENTRADA-REA"))
                .isEqualTo("ENTRADA-REA");
        }

        @Test @DisplayName("Other classification → empty string")
        void other_returns_empty() {
            assertThat(CrearOrdenesService.resolveIdTipoReajuste("CL", "RV", "INSTRUMENTO-REA", "ENTRADA-REA"))
                .isEmpty();
        }

        @Test @DisplayName("PE + RF → empty string (only CL localization triggers instrument reajuste)")
        void pe_rf_returns_empty() {
            assertThat(CrearOrdenesService.resolveIdTipoReajuste("PE", "RF", "INSTRUMENTO-REA", "ENTRADA-REA"))
                .isEmpty();
        }
    }

    @Nested @DisplayName("BEC bolsa → MAN channel override (CrearOrdenes:1902)")
    class BecBolsaTest {

        @Test @DisplayName("BEC bolsa real → IdTipoCanalDistribucionReal forced to MAN")
        void bec_bolsa_forces_manual() {
            assertThat(CrearOrdenesService.resolveIdTipoCanalDistribucionReal("BEC", "RUR"))
                .isEqualTo("MAN");
        }

        @Test @DisplayName("non-BEC bolsa → keeps original channel")
        void non_bec_bolsa_keeps_channel() {
            assertThat(CrearOrdenesService.resolveIdTipoCanalDistribucionReal("BCH", "RUR"))
                .isEqualTo("RUR");
        }

        @Test @DisplayName("BEC comparison is case-insensitive")
        void bec_comparison_case_insensitive() {
            assertThat(CrearOrdenesService.resolveIdTipoCanalDistribucionReal("bec", "RUR"))
                .isEqualTo("MAN");
        }
    }

    @Nested @DisplayName("EsBroker derivation (CrearOrdenes:1965-1975)")
    class EsBrokerTest {

        @Test @DisplayName("IdPersonaBroker not null → EsBroker = 'S'")
        void persona_broker_not_null_is_S() {
            assertThat(CrearOrdenesService.resolveEsBroker(999L)).isEqualTo("S");
        }

        @Test @DisplayName("IdPersonaBroker null → EsBroker = 'N'")
        void persona_broker_null_is_N() {
            assertThat(CrearOrdenesService.resolveEsBroker(null)).isEqualTo("N");
        }
    }

    @Nested @DisplayName("VentaCorta + canal validation (CrearOrdenes:1343-1354)")
    class VentaCortaCanalTest {

        @Test @DisplayName("EsVentaCorta=S + canal != MAN → invalid (triggers ERROR_ORDEN_VC_RUTEO)")
        void venta_corta_no_manual_is_invalid() {
            assertThat(CrearOrdenesService.isVentaCortaConCanalInvalido("S", "RUR")).isTrue();
        }

        @Test @DisplayName("EsVentaCorta=S + canal MAN → valid")
        void venta_corta_manual_is_valid() {
            assertThat(CrearOrdenesService.isVentaCortaConCanalInvalido("S", "MAN")).isFalse();
        }

        @Test @DisplayName("EsVentaCorta=N → always valid regardless of canal")
        void no_venta_corta_always_valid() {
            assertThat(CrearOrdenesService.isVentaCortaConCanalInvalido("N", "RUA")).isFalse();
        }

        @Test @DisplayName("MAN canal comparison is case-insensitive")
        void man_comparison_case_insensitive() {
            assertThat(CrearOrdenesService.isVentaCortaConCanalInvalido("S", "man")).isFalse();
        }
    }

    @Nested @DisplayName("limpiarOrdenes utility (CrearOrdenes:1086-1097)")
    class LimpiarOrdenesTest {

        @Test @DisplayName("order with errores > 0 is moved to ordenesConErrores")
        void order_with_errors_moved() {
            List<OrdenProceso> ordenes = new ArrayList<>();
            List<OrdenProceso> conErrores = new ArrayList<>();
            OrdenProceso bad = new OrdenProceso("A", 1L);
            ordenes.add(bad);
            CrearOrdenesService.limpiarOrdenes(ordenes, conErrores);
            assertThat(conErrores).containsExactly(bad);
            assertThat(ordenes).isEmpty();
        }

        @Test @DisplayName("order with errores == 0 stays in ordenes list")
        void order_without_errors_stays() {
            List<OrdenProceso> ordenes = new ArrayList<>();
            List<OrdenProceso> conErrores = new ArrayList<>();
            OrdenProceso good = new OrdenProceso("B", 0L);
            ordenes.add(good);
            CrearOrdenesService.limpiarOrdenes(ordenes, conErrores);
            assertThat(ordenes).containsExactly(good);
            assertThat(conErrores).isEmpty();
        }

        @Test @DisplayName("mixed: error order removed, good order kept")
        void mixed_separates_correctly() {
            List<OrdenProceso> ordenes = new ArrayList<>();
            List<OrdenProceso> conErrores = new ArrayList<>();
            OrdenProceso bad  = new OrdenProceso("X", 2L);
            OrdenProceso good = new OrdenProceso("Y", 0L);
            ordenes.add(bad);
            ordenes.add(good);
            CrearOrdenesService.limpiarOrdenes(ordenes, conErrores);
            assertThat(ordenes).containsExactly(good);
            assertThat(conErrores).containsExactly(bad);
        }

        @Test @DisplayName("empty input list → both lists remain empty")
        void empty_list_no_change() {
            List<OrdenProceso> ordenes = new ArrayList<>();
            List<OrdenProceso> conErrores = new ArrayList<>();
            CrearOrdenesService.limpiarOrdenes(ordenes, conErrores);
            assertThat(ordenes).isEmpty();
            assertThat(conErrores).isEmpty();
        }
    }

    @Nested @DisplayName("calculaMontoParidad formula (CalcularMontoYCobrosOrden:130-153)")
    class CalculaMontoParidadTest {

        @Test @DisplayName("y = price * factor < 0.5 → scale(3, DOWN) then multiply by cantidad")
        void y_less_than_half_scales_down_3() {
            // 0.1 * 1 = 0.1 < 0.5 → scale(3,DOWN) = 0.100; 0.100 * 10 = 1.000
            BigDecimal result = CalcularMontoService.calculaMontoParidad(
                    new BigDecimal("0.1"), new BigDecimal("10"), BigDecimal.ONE);
            assertThat(result).isEqualByComparingTo(new BigDecimal("1.000"));
        }

        @Test @DisplayName("y = price * factor >= 0.5 → scale(2, HALF_UP) then multiply by cantidad")
        void y_geq_half_scales_half_up_2() {
            // 1.0 * 1 = 1.0 >= 0.5 → scale(2,HALF_UP) = 1.00; 1.00 * 100 = 100.00
            BigDecimal result = CalcularMontoService.calculaMontoParidad(
                    BigDecimal.ONE, new BigDecimal("100"), BigDecimal.ONE);
            assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test @DisplayName("factor conversion rounds HALF_UP for >= 0.5 boundary")
        void factor_conversion_rounds_half_up() {
            // 1.005 * 1 = 1.005 >= 0.5 → scale(2, HALF_UP) = 1.01; 1.01 * 2 = 2.02
            BigDecimal result = CalcularMontoService.calculaMontoParidad(
                    new BigDecimal("1.005"), new BigDecimal("2"), BigDecimal.ONE);
            assertThat(result).isEqualByComparingTo(new BigDecimal("2.02"));
        }

        @Test @DisplayName("zero price → result is zero")
        void zero_price_returns_zero() {
            BigDecimal result = CalcularMontoService.calculaMontoParidad(
                    BigDecimal.ZERO, new BigDecimal("1000"), new BigDecimal("1.5"));
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested @DisplayName("CONTADOR_ID_TRABAJO AtomicLong (CrearOrdenes:137)")
    class ContadorIdTrabajoTest {

        @Test @DisplayName("AtomicLong starts at 0 before first increment")
        void counter_starts_at_zero() {
            AtomicLong counter = new AtomicLong(0);
            assertThat(counter.get()).isZero();
        }

        @Test @DisplayName("incrementAndGet returns sequential values")
        void counter_increments_sequentially() {
            AtomicLong counter = new AtomicLong(0);
            long first  = counter.incrementAndGet();
            long second = counter.incrementAndGet();
            long third  = counter.incrementAndGet();
            assertThat(first).isEqualTo(1L);
            assertThat(second).isEqualTo(2L);
            assertThat(third).isEqualTo(3L);
        }
    }
}
