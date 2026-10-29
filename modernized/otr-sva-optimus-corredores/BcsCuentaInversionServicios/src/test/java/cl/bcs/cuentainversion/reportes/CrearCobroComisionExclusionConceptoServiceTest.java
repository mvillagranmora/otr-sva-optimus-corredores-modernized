package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.CrearCobroComisionExclusionConceptoService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.CrearCobroComisionExclusionConcepto} (legacy/.../cuentaInversion/CrearCobroComisionExclusionConcepto.java) against the modern
 * {@code CrearCobroComisionExclusionConceptoService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class CrearCobroComisionExclusionConceptoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-16_exclusion-crear-concepto-valido",
            "CTA-B-17_exclusion-crear-concepto-inexistente-advertencia",
            "CTA-B-18_exclusion-crear-concepto-ya-excluido-advertencia",
            "CTA-B-19_exclusion-crear-fecha-menor-fecha-sistema",
            "CTA-B-20_exclusion-crear-sin-conceptos",
            "CTA-B-21_exclusion-crear-mezcla-valido-inexistente-y-excluido",
            "CTA-B-46_exclusion-crear-fecha-iso-con-zona-lanza-excepcion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new CrearCobroComisionExclusionConceptoService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (CrearCobroComisionExclusionConcepto): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-16: valid concept is excluded with sequence id and audited")
    void ctaB16_exclusionCrearConceptoValido() {
        // CrearCobroComisionExclusionConcepto.java:147-160 (insert, UI_CONCEPTO_EXCLUIDO, agregarLog), :226-246
        // Existing, not yet excluded concept is inserted with sequence id and FechaVigenciaDesde = requested date; audit entry
        // sent with IdCabeceraLog = new id.
        Resultado r = ejecutar("CTA-B-16_exclusion-crear-concepto-valido");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_CONCEPTO_EXCLUIDO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto")).as("rows of CobroComisionExclusionConcepto").hasSize(2);
        verificar("CTA-B-16_exclusion-crear-concepto-valido", r);
    }

    @Test
    @DisplayName("CTA-B-17: unknown concept is a warning, not an error")
    void ctaB17_exclusionCrearConceptoInexistenteAdvertencia() {
        // CrearCobroComisionExclusionConcepto.java:131-137 (UI_ADVERTENCIA_CONCEPTO_NO_EXISTE)
        // Unknown concept -> ADVERTENCIA UI_ADVERTENCIA_CONCEPTO_NO_EXISTE (counted in ADVERTENCIASCONTADOR, ERRORESCONTADOR 0);
        // nothing inserted.
        Resultado r = ejecutar("CTA-B-17_exclusion-crear-concepto-inexistente-advertencia");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ADVERTENCIA_CONCEPTO_NO_EXISTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-17_exclusion-crear-concepto-inexistente-advertencia", r);
    }

    @Test
    @DisplayName("CTA-B-18: already excluded concept is a warning")
    void ctaB18_exclusionCrearConceptoYaExcluidoAdvertencia() {
        // CrearCobroComisionExclusionConcepto.java:138-144 (UI_ADVERTENCIA_CONCEPTO_YA_EXCLUIDO)
        // Concept already excluded -> ADVERTENCIA UI_ADVERTENCIA_CONCEPTO_YA_EXCLUIDO.
        Resultado r = ejecutar("CTA-B-18_exclusion-crear-concepto-ya-excluido-advertencia");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ADVERTENCIA_CONCEPTO_YA_EXCLUIDO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-18_exclusion-crear-concepto-ya-excluido-advertencia", r);
    }

    @Test
    @DisplayName("CTA-B-19: FechaVigenciaDesde before the system date is rejected")
    void ctaB19_exclusionCrearFechaMenorFechaSistema() {
        // CrearCobroComisionExclusionConcepto.java:119-125
        // Boundary: FechaVigenciaDesde one day before the system date (2025-03-14) ->
        // UI_ERROR_FECHA_VIGENCIA_DESDE_MENOR_FECHA_SISTEMA. The system date itself is accepted (CTA-B-16).
        Resultado r = ejecutar("CTA-B-19_exclusion-crear-fecha-menor-fecha-sistema");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_FECHA_VIGENCIA_DESDE_MENOR_FECHA_SISTEMA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-19_exclusion-crear-fecha-menor-fecha-sistema", r);
    }

    @Test
    @DisplayName("CTA-B-20: empty request is rejected before the date check")
    void ctaB20_exclusionCrearSinConceptos() {
        // CrearCobroComisionExclusionConcepto.java:113-117
        // Empty list -> UI_ERROR_ENTRADA_SIN_CONCEPTOS (checked before the date).
        Resultado r = ejecutar("CTA-B-20_exclusion-crear-sin-conceptos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_ENTRADA_SIN_CONCEPTOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-20_exclusion-crear-sin-conceptos", r);
    }

    @Test
    @DisplayName("CTA-B-21: mixed batch inserts only the valid concepts")
    void ctaB21_exclusionCrearMezclaValidoInexistenteYExcluido() {
        // CrearCobroComisionExclusionConcepto.java:127-160
        // Mixed batch: the valid concepts are inserted, the others produce one warning each; sequence asked once for the valid
        // count.
        Resultado r = ejecutar("CTA-B-21_exclusion-crear-mezcla-valido-inexistente-y-excluido");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ADVERTENCIA_CONCEPTO_NO_EXISTE", "UI_ADVERTENCIA_CONCEPTO_YA_EXCLUIDO", "UI_CONCEPTO_EXCLUIDO", "UI_CONCEPTO_EXCLUIDO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(2L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto")).as("rows of CobroComisionExclusionConcepto").hasSize(3);
        verificar("CTA-B-21_exclusion-crear-mezcla-valido-inexistente-y-excluido", r);
    }

    @Test
    @DisplayName("CTA-B-46: ISO instant in the java.sql.Date field crashes with NullPointerException (defect)")
    void ctaB46_exclusionCrearFechaIsoConZonaLanzaExcepcion() {
        // CrearCobroComisionExclusionConcepto.java:120 (p_FechaVigenciaDesde.before with a null produced by GsonDateDeserializer)
        // LEGACY DEFECT: here FechaVigenciaDesde is a java.sql.Date, handled by GsonDateDeserializer, which answers null for an
        // ISO timestamp (ParseException swallowed) and logica dereferences it -> NullPointerException. Only the exception class
        // is pinned.
        Resultado r = ejecutar("CTA-B-46_exclusion-crear-fecha-iso-con-zona-lanza-excepcion");
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        verificar("CTA-B-46_exclusion-crear-fecha-iso-con-zona-lanza-excepcion", r);
    }
}
