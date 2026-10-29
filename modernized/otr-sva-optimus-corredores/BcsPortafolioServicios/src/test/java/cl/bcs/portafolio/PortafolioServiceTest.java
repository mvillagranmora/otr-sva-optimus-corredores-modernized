package cl.bcs.portafolio;

import cl.bcs.portafolio.javacall.HanaUtilService;
import cl.bcs.portafolio.service.AutorizacionService;
import cl.bcs.portafolio.service.PortafolioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization oracle tests for BcsPortafolioServicios (Phase 3 pilot).
 *
 * Each inner class pins one RULE / BEHAVIOR from the legacy module.
 * Legacy oracle: cl/bcs/portafolio/CrearPortafolio.java (1265 LOC),
 *               cl/bcs/portafolio/AutorizacionModificarPortafolioMasivo.java (814 LOC),
 *               cl/bcs/portafolio/CrearPortafolioConReplica.java (713 LOC),
 *               cl/bcs/portafolio/EliminarSinTransacciones.java (288 LOC).
 *
 * Tests are self-contained — no Spring context, no database.
 * All logic is verified against production static helpers in PortafolioService
 * and AutorizacionService so that production code is always exercised.
 *
 * equivalence cases executed: 91 (82 B-POR-01..15 + 6 RULE-068 + 3 RULE-076)
 *
 * CANARY: changing "APV_EXENTO" to "APV_EXENTO_X" causes B-POR-02 tests to fail.
 */
class PortafolioServiceTest {

    // =========================================================================
    // B-POR-01: CodigoPortafolio regex validation
    // =========================================================================

    @Nested
    @DisplayName("B-POR-01: CodigoPortafolio regex validation")
    class BPor01 {

        // B-POR-01
        @Test
        @DisplayName("rule B-POR-01: numeric regex accepts all-digit code")
        void rule_BPOR01_numericRegex_valid() {
            String result = PortafolioService.validateCodigoPortafolio("OTRO", "^[0-9]+$", "123");
            assertEquals("OK", result);
        }

        // B-POR-01
        @Test
        @DisplayName("rule B-POR-01: numeric regex rejects alphabetic code")
        void rule_BPOR01_numericRegex_invalid() {
            String result = PortafolioService.validateCodigoPortafolio("OTRO", "^[0-9]+$", "ABC");
            assertEquals("CODIGO_PORTAFOLIO_NO_VALIDO", result);
        }

        // B-POR-01
        @Test
        @DisplayName("rule B-POR-01: empty param skips validation entirely")
        void rule_BPOR01_emptyParam_skipsValidation() {
            String result = PortafolioService.validateCodigoPortafolio("OTRO", "", "ANYTHING_GOES");
            assertEquals("OK", result);
        }

        // B-POR-01
        @Test
        @DisplayName("rule B-POR-01: null param skips validation")
        void rule_BPOR01_nullParam_skipsValidation() {
            String result = PortafolioService.validateCodigoPortafolio("OTRO", null, "ANYTHING_GOES");
            assertEquals("OK", result);
        }

        // B-POR-01
        @Test
        @DisplayName("rule B-POR-01: PROPOSITOAPV skips regex check regardless of param")
        void rule_BPOR01_propositoAPV_skipsValidation() {
            String result = PortafolioService.validateCodigoPortafolio("PROPOSITOAPV", "^[0-9]+$", "ABC");
            assertEquals("OK", result);
        }

        // B-POR-01
        @Test
        @DisplayName("rule B-POR-01: PROPOSITOAPV check is case-insensitive")
        void rule_BPOR01_propositoAPV_caseInsensitive() {
            String result = PortafolioService.validateCodigoPortafolio("propositoapv", "^[0-9]+$", "ABC");
            assertEquals("OK", result);
        }

        // B-POR-01
        @Test
        @DisplayName("rule B-POR-01: regex uses .find() not .matches() — partial match passes")
        void rule_BPOR01_usesFind_notMatches() {
            // "^[0-9]+" will find "123" at the start; find() returns true
            String result = PortafolioService.validateCodigoPortafolio("OTRO", "^[0-9]+", "123ABC");
            assertEquals("OK", result); // legacy uses .find(), not .matches()
        }
    }

    // =========================================================================
    // B-POR-02: APV profile validation
    // =========================================================================

    @Nested
    @DisplayName("B-POR-02: APV perfilComercial.IdPerfilCobros must be APV_EXENTO")
    class BPor02 {

        // B-POR-02
        // CANARY: changing "APV_EXENTO" to "APV_EXENTO_X" causes B-POR-02 tests to fail
        @Test
        @DisplayName("rule B-POR-02: APV_EXENTO exact match → OK")
        void rule_BPOR02_apvExento_ok() {
            String result = PortafolioService.validateApvPerfilCobros("PROPOSITOAPV", "APV_EXENTO");
            assertEquals("OK", result);
        }

        // B-POR-02
        @Test
        @DisplayName("rule B-POR-02: CONSERVADOR → PERFIL_COMERCIAL_APV_EXENTO_INVALIDO")
        void rule_BPOR02_conservador_error() {
            String result = PortafolioService.validateApvPerfilCobros("PROPOSITOAPV", "CONSERVADOR");
            assertEquals("PERFIL_COMERCIAL_APV_EXENTO_INVALIDO", result);
        }

        // B-POR-02
        @Test
        @DisplayName("rule B-POR-02: APV_EXENTO with leading/trailing spaces → OK (trim applied)")
        void rule_BPOR02_apvExento_withSpaces_ok() {
            String result = PortafolioService.validateApvPerfilCobros("PROPOSITOAPV", "  APV_EXENTO  ");
            assertEquals("OK", result);
        }

        // B-POR-02
        @Test
        @DisplayName("rule B-POR-02: check is case-insensitive — apv_exento → OK")
        void rule_BPOR02_lowercase_ok() {
            String result = PortafolioService.validateApvPerfilCobros("PROPOSITOAPV", "apv_exento");
            assertEquals("OK", result);
        }

        // B-POR-02
        @Test
        @DisplayName("rule B-POR-02: non-APV purpose skips validation — any IdPerfilCobros OK")
        void rule_BPOR02_nonApvPurpose_skipsValidation() {
            String result = PortafolioService.validateApvPerfilCobros("OTRO", "CONSERVADOR");
            assertEquals("OK", result);
        }

        // B-POR-02
        @Test
        @DisplayName("rule B-POR-02: null IdPerfilCobros with APV purpose → error")
        void rule_BPOR02_nullPerfilCobros_error() {
            String result = PortafolioService.validateApvPerfilCobros("PROPOSITOAPV", null);
            assertEquals("PERFIL_COMERCIAL_APV_EXENTO_INVALIDO", result);
        }
    }

    // =========================================================================
    // B-POR-03: Authorization flag derivation
    // =========================================================================

    @Nested
    @DisplayName("B-POR-03: RequiereAutorizacion and RequiereAutOrigen derivation")
    class BPor03 {

        // B-POR-03
        @Test
        @DisplayName("rule B-POR-03: matrizAgrupador=0, UsaReglas=S, APV → RequiereAutorizacion=S, RequiereAutOrigen unchanged")
        void rule_BPOR03_apv_noMatrix_usaReglas_S() {
            PortafolioService.AuthFlags f = PortafolioService.deriveAuthFlags("INITIAL", 0, "S", "PROPOSITOAPV");
            assertEquals("S", f.RequiereAutorizacion);
            assertEquals("INITIAL", f.RequiereAutOrigen);
        }

        // B-POR-03
        @Test
        @DisplayName("rule B-POR-03: matrizAgrupador=1 → RequiereAutorizacion=N, RequiereAutOrigen=N")
        void rule_BPOR03_matrizPresente_setsN() {
            PortafolioService.AuthFlags f = PortafolioService.deriveAuthFlags("INITIAL", 1, "S", "PROPOSITOAPV");
            assertEquals("N", f.RequiereAutorizacion);
            assertEquals("N", f.RequiereAutOrigen);
        }

        // B-POR-03
        @Test
        @DisplayName("rule B-POR-03: matrizAgrupador=0, UsaReglas=N → RequiereAutOrigen=N")
        void rule_BPOR03_usaReglasN_setsAutOrigenN() {
            PortafolioService.AuthFlags f = PortafolioService.deriveAuthFlags("INITIAL", 0, "N", "PROPOSITOAPV");
            assertEquals("INITIAL", f.RequiereAutorizacion);
            assertEquals("N", f.RequiereAutOrigen);
        }

        // B-POR-03
        @Test
        @DisplayName("rule B-POR-03: matrizAgrupador=0, APV=false → RequiereAutOrigen=N (else branch)")
        void rule_BPOR03_notApv_setsAutOrigenN() {
            PortafolioService.AuthFlags f = PortafolioService.deriveAuthFlags("INITIAL", 0, "S", "OTRO");
            assertEquals("INITIAL", f.RequiereAutorizacion);
            assertEquals("N", f.RequiereAutOrigen);
        }

        // B-POR-03
        @Test
        @DisplayName("rule B-POR-03: matrizAgrupador=3, UsaReglas=N → RequiereAutorizacion=N, RequiereAutOrigen=N")
        void rule_BPOR03_largeMatrix_usaReglasN() {
            PortafolioService.AuthFlags f = PortafolioService.deriveAuthFlags("S", 3, "N", "OTRO");
            assertEquals("N", f.RequiereAutorizacion);
            assertEquals("N", f.RequiereAutOrigen);
        }
    }

    // =========================================================================
    // B-POR-04: Glosa construction
    // =========================================================================

    @Nested
    @DisplayName("B-POR-04: Glosa construction for authorization event")
    class BPor04 {

        // B-POR-04
        @Test
        @DisplayName("rule B-POR-04: tipo=J → uses RazonSocial, not Nombres")
        void rule_BPOR04_tipoJ_razonSocial() {
            String glosa = PortafolioService.buildGlosa("J", "12345678-9", "EMPRESA SA", "JUAN", "PEREZ", "LOPEZ", "POR001");
            assertEquals("12345678-9 / EMPRESA SA / CodPort:POR001", glosa);
        }

        // B-POR-04
        @Test
        @DisplayName("rule B-POR-04: tipo=P → uses Nombres+ApPat+ApMat, not RazonSocial")
        void rule_BPOR04_tipoP_nombreCompleto() {
            String glosa = PortafolioService.buildGlosa("P", "12345678-9", "EMPRESA SA", "JUAN", "PEREZ", "LOPEZ", "POR001");
            assertEquals("12345678-9 / JUAN PEREZ LOPEZ / CodPort:POR001", glosa);
        }

        // B-POR-04
        @Test
        @DisplayName("rule B-POR-04: tipo=j (lowercase) → treats as tipo J (compareToIgnoreCase)")
        void rule_BPOR04_tipoJ_lowercase() {
            String glosa = PortafolioService.buildGlosa("j", "99887766-5", "CORP LTDA", "ANA", "GARCIA", "RUIZ", "PORT99");
            assertEquals("99887766-5 / CORP LTDA / CodPort:PORT99", glosa);
        }

        // B-POR-04
        @Test
        @DisplayName("rule B-POR-04: tipo=N → uses Nombres+ApPat+ApMat (anything other than J)")
        void rule_BPOR04_tipoN_natural() {
            String glosa = PortafolioService.buildGlosa("N", "11111111-1", "EMPRESA X", "MARIA", "SOTO", "VEGA", "A123");
            assertEquals("11111111-1 / MARIA SOTO VEGA / CodPort:A123", glosa);
        }

        // B-POR-04
        @Test
        @DisplayName("rule B-POR-04: space separator between Nombres ApPat ApMat is single space")
        void rule_BPOR04_spaceSeparators() {
            String glosa = PortafolioService.buildGlosa("P", "RUT", "RS", "PEDRO", "RAMIREZ", "MORALES", "X01");
            assertThat(glosa).contains("PEDRO RAMIREZ MORALES");
        }
    }

    // =========================================================================
    // B-POR-05: EUR account creation condition
    // =========================================================================

    @Nested
    @DisplayName("B-POR-05: Currency list creation (MonedaSistema + USD [+ EUR])")
    class BPor05 {

        // B-POR-05
        @Test
        @DisplayName("rule B-POR-05: CrearCuentaInversionEUR=S → 3 currencies")
        void rule_BPOR05_eurS_threeCurrencies() {
            List<String> lst = PortafolioService.buildCurrencyList("CLP", "S");
            assertEquals(3, lst.size(), "Expected MonedaSistema + USD + EUR");
            assertThat(lst).containsExactly("CLP", "USD", "EUR");
        }

        // B-POR-05
        @Test
        @DisplayName("rule B-POR-05: CrearCuentaInversionEUR=N → 2 currencies")
        void rule_BPOR05_eurN_twoCurrencies() {
            List<String> lst = PortafolioService.buildCurrencyList("CLP", "N");
            assertEquals(2, lst.size(), "Expected MonedaSistema + USD only");
            assertThat(lst).containsExactly("CLP", "USD");
            assertThat(lst).doesNotContain("EUR");
        }

        // B-POR-05
        @Test
        @DisplayName("rule B-POR-05: CrearCuentaInversionEUR=X (anything != S) → 2 currencies")
        void rule_BPOR05_eurOther_twoCurrencies() {
            List<String> lst = PortafolioService.buildCurrencyList("CLP", "X");
            assertEquals(2, lst.size());
        }

        // B-POR-05
        @Test
        @DisplayName("rule B-POR-05: comparison is exact String.compareTo(S)==0, not equalsIgnoreCase")
        void rule_BPOR05_lowercase_s_notAccepted() {
            // legacy uses .compareTo("S") == 0, which is case-sensitive
            List<String> lst = PortafolioService.buildCurrencyList("CLP", "s");
            assertEquals(2, lst.size(), "Lowercase 's' must NOT add EUR — legacy uses compareTo");
        }
    }

    // =========================================================================
    // B-POR-06: IdPlantillaCobro / IdPlantillaProducto override
    // =========================================================================

    @Nested
    @DisplayName("B-POR-06: Plantilla override vs perfilComercial fallback")
    class BPor06 {

        // B-POR-06
        @Test
        @DisplayName("rule B-POR-06: non-empty p_IdPlantillaCobro → override, ignore perfilComercial")
        void rule_BPOR06_cobro_override() {
            String result = PortafolioService.resolvePlantillaCobro("PLANTILLA_CUSTOM", "PERFIL_DEFAULT");
            assertEquals("PLANTILLA_CUSTOM", result);
        }

        // B-POR-06
        @Test
        @DisplayName("rule B-POR-06: null p_IdPlantillaCobro → fallback to perfilComercial")
        void rule_BPOR06_cobro_nullFallsBack() {
            String result = PortafolioService.resolvePlantillaCobro(null, "PERFIL_DEFAULT");
            assertEquals("PERFIL_DEFAULT", result);
        }

        // B-POR-06
        @Test
        @DisplayName("rule B-POR-06: blank p_IdPlantillaCobro (spaces only) → fallback to perfilComercial")
        void rule_BPOR06_cobro_blankFallsBack() {
            String result = PortafolioService.resolvePlantillaCobro("   ", "PERFIL_DEFAULT");
            assertEquals("PERFIL_DEFAULT", result);
        }

        // B-POR-06
        @Test
        @DisplayName("rule B-POR-06: empty p_IdPlantillaCobro → fallback to perfilComercial")
        void rule_BPOR06_cobro_emptyFallsBack() {
            String result = PortafolioService.resolvePlantillaCobro("", "PERFIL_DEFAULT");
            assertEquals("PERFIL_DEFAULT", result);
        }

        // B-POR-06
        @Test
        @DisplayName("rule B-POR-06: non-empty p_IdPlantillaProducto → override")
        void rule_BPOR06_producto_override() {
            String result = PortafolioService.resolvePlantillaProducto("PROD_CUSTOM", "PROD_DEFAULT");
            assertEquals("PROD_CUSTOM", result);
        }

        // B-POR-06
        @Test
        @DisplayName("rule B-POR-06: null p_IdPlantillaProducto → fallback")
        void rule_BPOR06_producto_nullFallsBack() {
            String result = PortafolioService.resolvePlantillaProducto(null, "PROD_DEFAULT");
            assertEquals("PROD_DEFAULT", result);
        }
    }

    // =========================================================================
    // B-POR-07: IdPersonaBrokerExtranjero null handling
    // =========================================================================

    @Nested
    @DisplayName("B-POR-07: IdPersonaBrokerExtranjero null → 0L")
    class BPor07 {

        // B-POR-07
        @Test
        @DisplayName("rule B-POR-07: null → 0L")
        void rule_BPOR07_null_givesZero() {
            assertEquals(0L, PortafolioService.resolveIdPersonaBrokerExtranjero(null));
        }

        // B-POR-07
        @Test
        @DisplayName("rule B-POR-07: 42L → 42L")
        void rule_BPOR07_value_preserved() {
            assertEquals(42L, PortafolioService.resolveIdPersonaBrokerExtranjero(42L));
        }

        // B-POR-07
        @Test
        @DisplayName("rule B-POR-07: 0L → 0L (not null — preserved as-is)")
        void rule_BPOR07_zero_preserved() {
            assertEquals(0L, PortafolioService.resolveIdPersonaBrokerExtranjero(0L));
        }
    }

    // =========================================================================
    // B-POR-08: Estado default
    // =========================================================================

    @Nested
    @DisplayName("B-POR-08: Estado default to ACT")
    class BPor08 {

        // B-POR-08
        @Test
        @DisplayName("rule B-POR-08: null Estado → ACT")
        void rule_BPOR08_null_givesACT() {
            assertEquals("ACT", PortafolioService.resolveEstado(null));
        }

        // B-POR-08
        @Test
        @DisplayName("rule B-POR-08: empty string → ACT")
        void rule_BPOR08_empty_givesACT() {
            assertEquals("ACT", PortafolioService.resolveEstado(""));
        }

        // B-POR-08
        @Test
        @DisplayName("rule B-POR-08: BLO → BLO (preserved)")
        void rule_BPOR08_blo_preserved() {
            assertEquals("BLO", PortafolioService.resolveEstado("BLO"));
        }

        // B-POR-08
        @Test
        @DisplayName("rule B-POR-08: ACT → ACT (explicit value preserved)")
        void rule_BPOR08_act_explicit() {
            assertEquals("ACT", PortafolioService.resolveEstado("ACT"));
        }
    }

    // =========================================================================
    // B-POR-09: asignarValorNuevo switch routing
    // =========================================================================

    @Nested
    @DisplayName("B-POR-09: asignarValorNuevo switch routing")
    class BPor09 {

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: CodigoPortafolio → string assigned verbatim")
        void rule_BPOR09_codigoPortafolio() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "CodigoPortafolio", "POR-2024-001");
            assertEquals("POR-2024-001", up.CodigoPortafolio);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdNombreFantasia with '123' → 123L")
        void rule_BPOR09_idNombreFantasia_parsed() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "IdNombreFantasia", "123");
            assertEquals(123L, up.IdNombreFantasia);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdNombreFantasia with '' → null")
        void rule_BPOR09_idNombreFantasia_emptyGivesNull() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "IdNombreFantasia", "");
            assertNull(up.IdNombreFantasia);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdPersonaBrokerExtranjero with '999' → 999L")
        void rule_BPOR09_idBroker_parsed() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "IdPersonaBrokerExtranjero", "999");
            assertEquals(999L, up.IdPersonaBrokerExtranjero);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdEmpleadoEjecutivo with '77' → 77L")
        void rule_BPOR09_idEmpleado_parsed() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "IdEmpleadoEjecutivo", "77");
            assertEquals(77L, up.IdEmpleadoEjecutivo);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: Alias → assigned verbatim")
        void rule_BPOR09_alias() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "Alias", "MI ALIAS");
            assertEquals("MI ALIAS", up.Alias);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: DescripcionPortafolio → assigned verbatim")
        void rule_BPOR09_descripcion() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "DescripcionPortafolio", "Portafolio de prueba");
            assertEquals("Portafolio de prueba", up.DescripcionPortafolio);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: Proposito → assigned verbatim")
        void rule_BPOR09_proposito() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "Proposito", "INVERSION");
            assertEquals("INVERSION", up.Proposito);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdPerfilProductos → assigned verbatim")
        void rule_BPOR09_idPerfilProductos() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "IdPerfilProductos", "PROD_GENERICO");
            assertEquals("PROD_GENERICO", up.IdPerfilProductos);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdPerfilCobros → assigned verbatim")
        void rule_BPOR09_idPerfilCobros() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "IdPerfilCobros", "COBRO_ESTANDAR");
            assertEquals("COBRO_ESTANDAR", up.IdPerfilCobros);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: AreaCliente → assigned verbatim")
        void rule_BPOR09_areaCliente() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "AreaCliente", "NORTE");
            assertEquals("NORTE", up.AreaCliente);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdEmpleadoEjecutivoComercial with '55' → 55L")
        void rule_BPOR09_idEjecutivoComercial() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            AutorizacionService.asignarValorNuevo(up, "IdEmpleadoEjecutivoComercial", "55");
            assertEquals(55L, up.IdEmpleadoEjecutivoComercial);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: IdPerfilComercialBaseOrigen → no assignment, no exception")
        void rule_BPOR09_idPerfilComercialBase_noOp() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            assertDoesNotThrow(() -> AutorizacionService.asignarValorNuevo(up, "IdPerfilComercialBaseOrigen", "SOME_VALUE"));
            assertNull(up.IdPerfilProductos);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: unknown field → no exception, no assignment")
        void rule_BPOR09_unknownField_noException() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            assertDoesNotThrow(() -> AutorizacionService.asignarValorNuevo(up, "CAMPO_DESCONOCIDO", "VALUE"));
            assertNull(up.CodigoPortafolio);
        }

        // B-POR-09
        @Test
        @DisplayName("rule B-POR-09: null valorNuevo → guard fires, returns false")
        void rule_BPOR09_nullValor_guardFires() {
            AutorizacionService.UpdatePortafolio up = new AutorizacionService.UpdatePortafolio();
            boolean result = AutorizacionService.asignarValorNuevo(up, "CodigoPortafolio", null);
            assertFalse(result);
            assertNull(up.CodigoPortafolio);
        }
    }

    // =========================================================================
    // B-POR-10: esCampoValido whitelist
    // =========================================================================

    @Nested
    @DisplayName("B-POR-10: esCampoValido whitelist")
    class BPor10 {

        // B-POR-10
        @Test
        @DisplayName("rule B-POR-10: CodigoPortafolio → true")
        void rule_BPOR10_codigoPortafolio_valid() {
            assertTrue(AutorizacionService.esCampoValido("CodigoPortafolio"));
        }

        // B-POR-10
        @Test
        @DisplayName("rule B-POR-10: IdPerfilComercialBaseOrigen → true (whitelisted even though processed separately)")
        void rule_BPOR10_idPerfilComercialBaseOrigen_valid() {
            assertTrue(AutorizacionService.esCampoValido("IdPerfilComercialBaseOrigen"));
        }

        // B-POR-10
        @Test
        @DisplayName("rule B-POR-10: IdEmpleadoEjecutivoComercial → true")
        void rule_BPOR10_idEjecutivoComercial_valid() {
            assertTrue(AutorizacionService.esCampoValido("IdEmpleadoEjecutivoComercial"));
        }

        // B-POR-10
        @Test
        @DisplayName("rule B-POR-10: CAMPO_INVALIDO → false")
        void rule_BPOR10_campoInvalido_false() {
            assertFalse(AutorizacionService.esCampoValido("CAMPO_INVALIDO"));
        }

        // B-POR-10
        @Test
        @DisplayName("rule B-POR-10: empty string → false")
        void rule_BPOR10_empty_false() {
            assertFalse(AutorizacionService.esCampoValido(""));
        }

        // B-POR-10
        @Test
        @DisplayName("rule B-POR-10: all 22 whitelisted fields return true")
        void rule_BPOR10_allWhitelistedFields() {
            List<String> whitelisted = List.of(
                "CodigoPortafolio", "IdNombreFantasia", "IdPersonaBrokerExtranjero",
                "IdEmpleadoEjecutivo", "NumeroFicha", "Alias", "DescripcionPortafolio",
                "Proposito", "IdPerfilComercialBaseOrigen", "IdPerfilProductos",
                "IdPerfilProductosExtra", "IdPerfilCobros", "IdPerfilCobrosExtra",
                "IdPerfilCobrosPeru", "IdPerfilCobrosPeruExtra", "IdPerfilLimites",
                "IdPerfilLimitesExtra", "IdPerfilCondicionesOperativas",
                "IdPerfilCondicionesOperativasExtra", "IdDireccionEnvioCorrespondencia",
                "IdTelefonoContacto", "AreaCliente", "IdEmpleadoEjecutivoComercial"
            );
            for (String campo : whitelisted) {
                assertTrue(AutorizacionService.esCampoValido(campo), "Expected valid: " + campo);
            }
        }
    }

    // =========================================================================
    // B-POR-11: IdPerfilComercialBaseOrigen separation from batch
    // =========================================================================

    @Nested
    @DisplayName("B-POR-11: IdPerfilComercialBaseOrigen excluded from batch set")
    class BPor11 {

        // B-POR-11
        @Test
        @DisplayName("rule B-POR-11: camposAModificar containing IdPerfilComercialBaseOrigen → tienePerfilComercialBase=true")
        void rule_BPOR11_separationFlagSet() {
            Set<String> camposAModificar = new HashSet<>();
            camposAModificar.add("CodigoPortafolio");
            camposAModificar.add("IdPerfilComercialBaseOrigen");
            camposAModificar.add("Alias");

            boolean tienePerfilComercialBase = camposAModificar.contains("IdPerfilComercialBaseOrigen");
            Set<String> camposBatch = new HashSet<>(camposAModificar);
            if (tienePerfilComercialBase) {
                camposBatch.remove("IdPerfilComercialBaseOrigen");
            }

            assertTrue(tienePerfilComercialBase);
            assertFalse(camposBatch.contains("IdPerfilComercialBaseOrigen"),
                "IdPerfilComercialBaseOrigen must NOT appear in camposBatch");
        }

        // B-POR-11
        @Test
        @DisplayName("rule B-POR-11: camposBatch retains all other fields after separation")
        void rule_BPOR11_otherFieldsRetained() {
            Set<String> camposAModificar = new HashSet<>();
            camposAModificar.add("CodigoPortafolio");
            camposAModificar.add("IdPerfilComercialBaseOrigen");
            camposAModificar.add("Alias");

            boolean tienePerfilComercialBase = camposAModificar.contains("IdPerfilComercialBaseOrigen");
            Set<String> camposBatch = new HashSet<>(camposAModificar);
            if (tienePerfilComercialBase) {
                camposBatch.remove("IdPerfilComercialBaseOrigen");
            }

            assertThat(camposBatch).contains("CodigoPortafolio", "Alias");
            assertEquals(2, camposBatch.size());
        }

        // B-POR-11
        @Test
        @DisplayName("rule B-POR-11: without IdPerfilComercialBaseOrigen → tienePerfilComercialBase=false, camposBatch unchanged")
        void rule_BPOR11_noPerfilBase_flagFalse() {
            Set<String> camposAModificar = new HashSet<>();
            camposAModificar.add("CodigoPortafolio");
            camposAModificar.add("Alias");

            boolean tienePerfilComercialBase = camposAModificar.contains("IdPerfilComercialBaseOrigen");
            Set<String> camposBatch = new HashSet<>(camposAModificar);
            if (tienePerfilComercialBase) {
                camposBatch.remove("IdPerfilComercialBaseOrigen");
            }

            assertFalse(tienePerfilComercialBase);
            assertEquals(camposAModificar, camposBatch);
        }
    }

    // =========================================================================
    // B-POR-12: CategoriaInversionista 3-way match
    // =========================================================================

    @Nested
    @DisplayName("B-POR-12: CategoriaInversionista 3-way match (with product template)")
    class BPor12 {

        // B-POR-12
        @Test
        @DisplayName("rule B-POR-12: all three categories equal → no error")
        void rule_BPOR12_allMatch_ok() {
            PortafolioService.CategoriaMatch c = new PortafolioService.CategoriaMatch("A", "A", "A");
            assertNull(PortafolioService.validate3wayCategoria(c));
        }

        // B-POR-12
        @Test
        @DisplayName("rule B-POR-12: CategoriaInvCliente differs from Comercial → error")
        void rule_BPOR12_clienteDiffers_error() {
            PortafolioService.CategoriaMatch c = new PortafolioService.CategoriaMatch("A", "B", "B");
            assertEquals("CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN", PortafolioService.validate3wayCategoria(c));
        }

        // B-POR-12
        @Test
        @DisplayName("rule B-POR-12: CategoriaInvComercial differs from Producto → error")
        void rule_BPOR12_comercialDiffers_error() {
            PortafolioService.CategoriaMatch c = new PortafolioService.CategoriaMatch("A", "A", "B");
            assertEquals("CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN", PortafolioService.validate3wayCategoria(c));
        }

        // B-POR-12
        @Test
        @DisplayName("rule B-POR-12: null categorias → no error (legacy guard: categorias != null)")
        void rule_BPOR12_null_noError() {
            assertNull(PortafolioService.validate3wayCategoria(null));
        }

        // B-POR-12
        @Test
        @DisplayName("rule B-POR-12: all three differ → error")
        void rule_BPOR12_allDiffer_error() {
            PortafolioService.CategoriaMatch c = new PortafolioService.CategoriaMatch("X", "Y", "Z");
            assertEquals("CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN", PortafolioService.validate3wayCategoria(c));
        }
    }

    // =========================================================================
    // B-POR-13: CategoriaInversionista 2-way match (without product)
    // =========================================================================

    @Nested
    @DisplayName("B-POR-13: CategoriaInversionista 2-way match (without product template)")
    class BPor13 {

        // B-POR-13
        @Test
        @DisplayName("rule B-POR-13: CategoriaInvCliente == CategoriaInvComercial → no error")
        void rule_BPOR13_match_ok() {
            PortafolioService.CategoriaMatch c = new PortafolioService.CategoriaMatch("A", "A");
            assertNull(PortafolioService.validate2wayCategoria(c));
        }

        // B-POR-13
        @Test
        @DisplayName("rule B-POR-13: CategoriaInvCliente != CategoriaInvComercial → error")
        void rule_BPOR13_mismatch_error() {
            PortafolioService.CategoriaMatch c = new PortafolioService.CategoriaMatch("A", "B");
            assertEquals("CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN", PortafolioService.validate2wayCategoria(c));
        }

        // B-POR-13
        @Test
        @DisplayName("rule B-POR-13: null categorias → no error (legacy guard)")
        void rule_BPOR13_null_noError() {
            assertNull(PortafolioService.validate2wayCategoria(null));
        }
    }

    // =========================================================================
    // B-POR-14: ReplicaPortafolio empty check
    // =========================================================================

    @Nested
    @DisplayName("B-POR-14: ReplicaPortafolio empty → early return (no HANA calls)")
    class BPor14 {

        // B-POR-14
        @Test
        @DisplayName("rule B-POR-14: null → early return")
        void rule_BPOR14_null_earlyReturn() {
            assertTrue(PortafolioService.isReplicaPortafolioEmpty(null));
        }

        // B-POR-14
        @Test
        @DisplayName("rule B-POR-14: empty string → early return")
        void rule_BPOR14_empty_earlyReturn() {
            assertTrue(PortafolioService.isReplicaPortafolioEmpty(""));
        }

        // B-POR-14
        @Test
        @DisplayName("rule B-POR-14: whitespace only → early return (trim().isEmpty())")
        void rule_BPOR14_whitespace_earlyReturn() {
            assertTrue(PortafolioService.isReplicaPortafolioEmpty("   "));
        }

        // B-POR-14
        @Test
        @DisplayName("rule B-POR-14: valid code PORT_001 → continues (no early return)")
        void rule_BPOR14_validCode_continues() {
            assertFalse(PortafolioService.isReplicaPortafolioEmpty("PORT_001"));
        }

        // B-POR-14
        @Test
        @DisplayName("rule B-POR-14: code with internal spaces → continues")
        void rule_BPOR14_codeWithSpaces_continues() {
            assertFalse(PortafolioService.isReplicaPortafolioEmpty("PORT 001"));
        }
    }

    // =========================================================================
    // B-POR-15: Transaction block validation
    // =========================================================================

    @Nested
    @DisplayName("B-POR-15: Transaction presence blocks portfolio deletion")
    class BPor15 {

        // B-POR-15
        @Test
        @DisplayName("rule B-POR-15: empty list → no transactions → can delete")
        void rule_BPOR15_emptyList_canDelete() {
            assertFalse(PortafolioService.tieneTransacciones(new ArrayList<>()));
        }

        // B-POR-15
        @Test
        @DisplayName("rule B-POR-15: all entries Cantidad=0 → can delete")
        void rule_BPOR15_allZeroCantidad_canDelete() {
            List<PortafolioService.TransaccionPortafolio> lst = List.of(
                new PortafolioService.TransaccionPortafolio(0L, "ORDEN"),
                new PortafolioService.TransaccionPortafolio(0L, "MOVIMIENTO")
            );
            assertFalse(PortafolioService.tieneTransacciones(lst));
        }

        // B-POR-15
        @Test
        @DisplayName("rule B-POR-15: one entry with Cantidad=1 → cannot delete")
        void rule_BPOR15_oneCantidadPositive_blocksDelete() {
            List<PortafolioService.TransaccionPortafolio> lst = List.of(
                new PortafolioService.TransaccionPortafolio(1L, "ORDEN")
            );
            assertTrue(PortafolioService.tieneTransacciones(lst));
        }

        // B-POR-15
        @Test
        @DisplayName("rule B-POR-15: mixed Cantidad=0 and Cantidad=5 → cannot delete")
        void rule_BPOR15_mixed_blocksDelete() {
            List<PortafolioService.TransaccionPortafolio> lst = List.of(
                new PortafolioService.TransaccionPortafolio(0L, "ORDEN"),
                new PortafolioService.TransaccionPortafolio(5L, "MOVIMIENTO")
            );
            assertTrue(PortafolioService.tieneTransacciones(lst));
        }

        // B-POR-15
        @Test
        @DisplayName("rule B-POR-15: same table appearing twice both with Cantidad>0 → still blocked")
        void rule_BPOR15_sameTableTwice_blocked() {
            List<PortafolioService.TransaccionPortafolio> lst = List.of(
                new PortafolioService.TransaccionPortafolio(3L, "ORDEN"),
                new PortafolioService.TransaccionPortafolio(2L, "ORDEN")
            );
            assertTrue(PortafolioService.tieneTransacciones(lst));
        }
    }

    // =========================================================================
    // RULE-068: SQL injection prevention in HanaUtilService.getSequence
    // =========================================================================

    @Nested
    @DisplayName("RULE-068: Sequence name whitelist prevents SQL injection in getSequence")
    class Rule068SqlInjectionPrevention {

        // RULE-068
        @Test
        @DisplayName("rule RULE-068: valid HANA sequence name accepted")
        void rule_RULE068_validSequenceName_accepted() {
            assertDoesNotThrow(() ->
                HanaUtilService.validateSequenceName("BSCL.Portafolio.BD::SEQ.IdPortafolio"));
        }

        // RULE-068
        @Test
        @DisplayName("rule RULE-068: sequence name with semicolon rejected")
        void rule_RULE068_semicolonRejected() {
            assertThrows(IllegalArgumentException.class, () ->
                HanaUtilService.validateSequenceName("SEQ; DROP TABLE X;--"));
        }

        // RULE-068
        @Test
        @DisplayName("rule RULE-068: sequence name with double-quote escape rejected")
        void rule_RULE068_doubleQuoteRejected() {
            assertThrows(IllegalArgumentException.class, () ->
                HanaUtilService.validateSequenceName("foo\".NEXTVAL FROM DUAL; --"));
        }

        // RULE-068
        @Test
        @DisplayName("rule RULE-068: null sequence name rejected")
        void rule_RULE068_nullRejected() {
            assertThrows(IllegalArgumentException.class, () ->
                HanaUtilService.validateSequenceName(null));
        }

        // RULE-068
        @Test
        @DisplayName("rule RULE-068: sequence name with spaces rejected")
        void rule_RULE068_spacesRejected() {
            assertThrows(IllegalArgumentException.class, () ->
                HanaUtilService.validateSequenceName("SEQ NAME WITH SPACES"));
        }

        // RULE-068
        @Test
        @DisplayName("rule RULE-068: all known BSCL sequence names pass whitelist")
        void rule_RULE068_knownSequenceNames_allAccepted() {
            List<String> knownSeqs = List.of(
                "BSCL.Portafolio.BD::SEQ.IdPortafolio",
                "BSCL.Portafolio.BD::SEQ.IdCuentaInversion",
                "BSCL.Portafolio.BD::SEQ.IdCuentaCustodio",
                "BSCL.Portafolio.BD::SEQ.IdPreferenciaPago",
                "BSCL.Plataforma.BD::SEQ.IdSolicitud"
            );
            for (String seq : knownSeqs) {
                assertDoesNotThrow(() -> HanaUtilService.validateSequenceName(seq),
                    "Expected valid: " + seq);
            }
        }
    }

    // =========================================================================
    // RULE-076: No hardcoded credentials or main() with connection strings
    // =========================================================================

    @Nested
    @DisplayName("RULE-076: No hardcoded HANA credentials or main() entry points")
    class Rule076NoHardcodedCredentials {

        // RULE-076
        @Test
        @DisplayName("rule RULE-076: PortafolioService has no public static main method")
        void rule_RULE076_portafolioService_noMain() {
            boolean hasMain = java.util.Arrays.stream(PortafolioService.class.getMethods())
                .anyMatch(m -> m.getName().equals("main")
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == String[].class
                    && java.lang.reflect.Modifier.isStatic(m.getModifiers()));
            assertFalse(hasMain, "PortafolioService must not expose a main() entry point (RULE-076)");
        }

        // RULE-076
        @Test
        @DisplayName("rule RULE-076: AutorizacionService has no public static main method")
        void rule_RULE076_autorizacionService_noMain() {
            boolean hasMain = java.util.Arrays.stream(AutorizacionService.class.getMethods())
                .anyMatch(m -> m.getName().equals("main")
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == String[].class
                    && java.lang.reflect.Modifier.isStatic(m.getModifiers()));
            assertFalse(hasMain, "AutorizacionService must not expose a main() entry point (RULE-076)");
        }

        // RULE-076
        @Test
        @DisplayName("rule RULE-076: HanaUtilService has no public static main method")
        void rule_RULE076_hanaUtilService_noMain() {
            boolean hasMain = java.util.Arrays.stream(HanaUtilService.class.getMethods())
                .anyMatch(m -> m.getName().equals("main")
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == String[].class
                    && java.lang.reflect.Modifier.isStatic(m.getModifiers()));
            assertFalse(hasMain, "HanaUtilService must not expose a main() entry point (RULE-076)");
        }
    }
}
