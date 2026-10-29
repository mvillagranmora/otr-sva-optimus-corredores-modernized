package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.GuardarPatrimonioCajaCalculoComisionService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GuardarPatrimonioCajaCalculoComision} (legacy/.../cuentaInversion/GuardarPatrimonioCajaCalculoComision.java) against the modern
 * {@code GuardarPatrimonioCajaCalculoComisionService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class GuardarPatrimonioCajaCalculoComisionServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-117_calculo-comision-parte-del-patrimonio-del-dia-habil-anterior",
            "CTA-B-118_calculo-comision-sin-cuentas-no-hace-nada",
            "CTA-B-119_calculo-comision-ignora-conceptos-excluidos",
            "CTA-B-120_calculo-comision-movimiento-cargo-resta",
            "CTA-B-121_calculo-comision-fecha-vigencia-futura-da-comision-cero",
            "CTA-B-122_calculo-comision-lunes-usa-el-viernes-habil-anterior",
            "CTA-B-123_calculo-comision-sin-patrimonio-del-dia-anterior-devuelve-vacio");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new GuardarPatrimonioCajaCalculoComisionService(caso.jdbc())::execute);
        EJECUTADOS.incrementAndGet();
        return r;
    }

    private static void verificar(String nombre, Resultado r) {
        CASOS_CARGADOS.get(nombre).assertEquivalente(r);
    }

    private static JsonObject salida(Resultado r) {
        assertThat(r.excepcion()).as("modern implementation threw").isNull();
        return r.salida();
    }

    private static long contador(JsonObject s, String campo) {
        return s.get(campo).getAsLong();
    }

    @AfterAll
    static void ningunCasoOmitido() {
        System.out.println("equivalence cases executed (GuardarPatrimonioCajaCalculoComision): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-117: commission starts from the previous business day patrimony of the requested portfolios")
    void ctaB117_calculoComisionParteDelPatrimonioDelDiaHabilAnterior() {
        // GuardarPatrimonioCajaCalculoComision.java:176-228 (obtenerDiaHabilAnterior, SQL_SELECT_PATRIMONIO_AFECTO_DIA_ANTERIOR, MontoComision)
        // Affected patrimony = previous business day (03-13) ADQ amount 1,000,000 + the day net movement of the account (+50,000
        // abono) = 1,050,000; commission = annual % / 365 x amount = 0.0365/365 x 1,050,000 = 105 (CLP truncated to 0 decimals),
        // state VIG. Result rows are the PREVIOUS day ADQ rows of the portfolios of the requested accounts, re-dated to the
        // query date (portfolio 2002 is not requested, so it is not reported).
        Resultado r = ejecutar("CTA-B-117_calculo-comision-parte-del-patrimonio-del-dia-habil-anterior");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-117_calculo-comision-parte-del-patrimonio-del-dia-habil-anterior", r);
    }

    @Test
    @DisplayName("CTA-B-118: no investment accounts: nothing is done")
    void ctaB118_calculoComisionSinCuentasNoHaceNada() {
        // GuardarPatrimonioCajaCalculoComision.java:157 (p_CuentaInversionAConsultar.isEmpty())
        // Empty account list -> empty result, no staging table is created.
        Resultado r = ejecutar("CTA-B-118_calculo-comision-sin-cuentas-no-hace-nada");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-118_calculo-comision-sin-cuentas-no-hace-nada", r);
    }

    @Test
    @DisplayName("CTA-B-119: excluded concepts are ignored in the commission base")
    void ctaB119_calculoComisionIgnoraConceptosExcluidos() {
        // GuardarPatrimonioCajaCalculoComision.java:209-228
        // A movement whose concept is in CobroComisionExclusionConcepto does not move the affected patrimony: it stays 1,000,000
        // -> commission 100.
        Resultado r = ejecutar("CTA-B-119_calculo-comision-ignora-conceptos-excluidos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-119_calculo-comision-ignora-conceptos-excluidos", r);
    }

    @Test
    @DisplayName("CTA-B-120: a charge movement subtracts from the commission base")
    void ctaB120_calculoComisionMovimientoCargoResta() {
        // GuardarPatrimonioCajaCalculoComision.java:209-228
        // A MOVCAR (cargo) of 400,000 certified on the query date subtracts: 600,000 -> commission 60; a non certified movement
        // (ANU) is ignored. Only ONE movement row per account is used (the first one of the group), see SQL grouping.
        Resultado r = ejecutar("CTA-B-120_calculo-comision-movimiento-cargo-resta");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-120_calculo-comision-movimiento-cargo-resta", r);
    }

    @Test
    @DisplayName("CTA-B-121: future validity date gives a zero commission (state INA)")
    void ctaB121_calculoComisionFechaVigenciaFuturaDaComisionCero() {
        // GuardarPatrimonioCajaCalculoComision.java:226-227 (montoComision > 0 ? ... : 0, INA/VIG)
        // The commission option is not yet in force on the query date (FechaVigencia > date) -> commission 0 and state INA (the
        // % is still reported).
        Resultado r = ejecutar("CTA-B-121_calculo-comision-fecha-vigencia-futura-da-comision-cero");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-121_calculo-comision-fecha-vigencia-futura-da-comision-cero", r);
    }

    @Test
    @DisplayName("CTA-B-122: Monday uses the previous Friday (business day before)")
    void ctaB122_calculoComisionLunesUsaElViernesHabilAnterior() {
        // GuardarPatrimonioCajaCalculoComision.java:176, :246 (obtenerDiaHabilAnterior)
        // Query date Monday 2025-03-17: the previous BUSINESS day is Friday 2025-03-14 (weekend skipped), whose patrimony is the
        // base.
        Resultado r = ejecutar("CTA-B-122_calculo-comision-lunes-usa-el-viernes-habil-anterior");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-122_calculo-comision-lunes-usa-el-viernes-habil-anterior", r);
    }

    @Test
    @DisplayName("CTA-B-123: no previous-day patrimony returns an empty result")
    void ctaB123_calculoComisionSinPatrimonioDelDiaAnteriorDevuelveVacio() {
        // GuardarPatrimonioCajaCalculoComision.java:181-209 (patrimoniosAfectoComision.isEmpty())
        // LEGACY DEFECT: with no previous-day patrimony nothing is reported even if the account moved: the result is built only
        // from the previous-day rows.
        Resultado r = ejecutar("CTA-B-123_calculo-comision-sin-patrimonio-del-dia-anterior-devuelve-vacio");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-123_calculo-comision-sin-patrimonio-del-dia-anterior-devuelve-vacio", r);
    }
}
