package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.EliminarCobroComisionExclusionConceptoService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.EliminarCobroComisionExclusionConcepto} (legacy/.../cuentaInversion/EliminarCobroComisionExclusionConcepto.java) against the modern
 * {@code EliminarCobroComisionExclusionConceptoService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class EliminarCobroComisionExclusionConceptoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-22_exclusion-eliminar-existente",
            "CTA-B-23_exclusion-eliminar-no-existe-es-error",
            "CTA-B-24_exclusion-eliminar-sin-conceptos",
            "CTA-B-25_exclusion-eliminar-dos-existentes-y-repetido");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new EliminarCobroComisionExclusionConceptoService(caso.dataSource())::execute);
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
        System.out.println("equivalence cases executed (EliminarCobroComisionExclusionConcepto): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-22: existing exclusion is deleted and audited")
    void ctaB22_exclusionEliminarExistente() {
        // EliminarCobroComisionExclusionConcepto.java:102-108 (delete, UI_EXCLUSION_CONCEPTO_BORRADA, agregarLog)
        // Existing exclusion deleted; OK message; audit entry names the concept.
        Resultado r = ejecutar("CTA-B-22_exclusion-eliminar-existente");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_EXCLUSION_CONCEPTO_BORRADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto")).as("rows of CobroComisionExclusionConcepto").hasSize(1);
        verificar("CTA-B-22_exclusion-eliminar-existente", r);
    }

    @Test
    @DisplayName("CTA-B-23: one unknown id fails the whole request")
    void ctaB23_exclusionEliminarNoExisteEsError() {
        // EliminarCobroComisionExclusionConcepto.java:84-100 (UI_ERROR_EXCLUSION_CONCEPTO_NO_EXISTE, early return before deleting)
        // One unknown id makes the whole request fail with UI_ERROR_EXCLUSION_CONCEPTO_NO_EXISTE; the existing one (6001) is NOT
        // deleted.
        Resultado r = ejecutar("CTA-B-23_exclusion-eliminar-no-existe-es-error");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_EXCLUSION_CONCEPTO_NO_EXISTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-23_exclusion-eliminar-no-existe-es-error", r);
    }

    @Test
    @DisplayName("CTA-B-24: empty request is rejected")
    void ctaB24_exclusionEliminarSinConceptos() {
        // EliminarCobroComisionExclusionConcepto.java:67-71
        // Empty list -> UI_ERROR_ENTRADA_SIN_CONCEPTOS.
        Resultado r = ejecutar("CTA-B-24_exclusion-eliminar-sin-conceptos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_ENTRADA_SIN_CONCEPTOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-24_exclusion-eliminar-sin-conceptos", r);
    }

    @Test
    @DisplayName("CTA-B-25: duplicated id in the request is tolerated")
    void ctaB25_exclusionEliminarDosExistentesYRepetido() {
        // EliminarCobroComisionExclusionConcepto.java:78-91, :102-108
        // Duplicated id in the request is tolerated; both rows deleted; one audit entry per requested concept (3).
        Resultado r = ejecutar("CTA-B-25_exclusion-eliminar-dos-existentes-y-repetido");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_EXCLUSION_CONCEPTO_BORRADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto")).as("rows of CobroComisionExclusionConcepto").hasSize(0);
        verificar("CTA-B-25_exclusion-eliminar-dos-existentes-y-repetido", r);
    }
}
