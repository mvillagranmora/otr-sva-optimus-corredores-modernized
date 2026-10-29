package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.CrearCobroComisionOpcionesCobroService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.CrearCobroComisionOpcionesCobro} (legacy/.../cuentaInversion/CrearCobroComisionOpcionesCobro.java) against the modern
 * {@code CrearCobroComisionOpcionesCobroService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class CrearCobroComisionOpcionesCobroServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-01_crear-opcion-cobro-nueva",
            "CTA-B-02_crear-porcentaje-fuera-de-rango",
            "CTA-B-03_crear-opcion-duplicada",
            "CTA-B-04_crear-sin-opciones",
            "CTA-B-05_crear-fecha-vigencia-iso-con-zona-es-aceptada",
            "CTA-B-06_crear-porcentaje-limites-0-y-1-validos",
            "CTA-B-07_crear-un-porcentaje-negativo-bloquea-todo");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new CrearCobroComisionOpcionesCobroService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (CrearCobroComisionOpcionesCobro): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-01: new commission option is inserted (sequence id, FechaIngreso = system date), no audit entry")
    void ctaB01_crearOpcionCobroNueva() {
        // CrearCobroComisionOpcionesCobro.java:206-217 (insert + UI_OPCION_COBRO_INGRESADA), :228 (audit list is empty)
        // New (OpcionCobro, PortafolioAPV, Mercado) is inserted with the sequence value, FechaIngreso = system date; audit entry
        // is NOT sent (insertOpciones is never filled).
        Resultado r = ejecutar("CTA-B-01_crear-opcion-cobro-nueva");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_OPCION_COBRO_INGRESADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro")).as("rows of CobroComisionOpcionesCobro").hasSize(5);
        verificar("CTA-B-01_crear-opcion-cobro-nueva", r);
    }

    @Test
    @DisplayName("CTA-B-02: percentage above 1.0 is rejected")
    void ctaB02_crearPorcentajeFueraDeRango() {
        // CrearCobroComisionOpcionesCobro.java:164-181 (range check), :183-185
        // PorcentajeCobro > 1.0 -> UI_ERROR_PORCENTAJE_NO_VALIDO and nothing is inserted.
        Resultado r = ejecutar("CTA-B-02_crear-porcentaje-fuera-de-rango");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_PORCENTAJE_NO_VALIDO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-02_crear-porcentaje-fuera-de-rango", r);
    }

    @Test
    @DisplayName("CTA-B-03: duplicate (OpcionCobro, PortafolioAPV, Mercado) is rejected")
    void ctaB03_crearOpcionDuplicada() {
        // CrearCobroComisionOpcionesCobro.java:186-203 (existing options -> UI_ERROR_OPCION_DUPLICADA)
        // Same OpcionCobro/PortafolioAPV/Mercado already exists (7001) -> UI_ERROR_OPCION_DUPLICADA, nothing inserted.
        Resultado r = ejecutar("CTA-B-03_crear-opcion-duplicada");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_OPCION_DUPLICADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-03_crear-opcion-duplicada", r);
    }

    @Test
    @DisplayName("CTA-B-04: empty request is rejected")
    void ctaB04_crearSinOpciones() {
        // CrearCobroComisionOpcionesCobro.java:158-162
        // Empty list -> UI_ERROR_ENTRADA_SIN_CONCEPTOS.
        Resultado r = ejecutar("CTA-B-04_crear-sin-opciones");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_ENTRADA_SIN_CONCEPTOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-04_crear-sin-opciones", r);
    }

    @Test
    @DisplayName("CTA-B-05: ISO instant with T/Z is accepted as FechaVigencia (java.util.Date)")
    void ctaB05_crearFechaVigenciaIsoConZonaEsAceptada() {
        // CrearCobroComisionOpcionesCobro.java:6 (java.util.Date), :52-62 (OpcionesCobro.FechaVigencia)
        // FechaVigencia is a java.util.Date here: Gson falls back to ISO-8601 and an instant with T/Z is accepted (the stored
        // DATE is the calendar day in the JVM time zone; the oracle and the tests run in America/Santiago and noon UTC keeps the
        // day stable).
        Resultado r = ejecutar("CTA-B-05_crear-fecha-vigencia-iso-con-zona-es-aceptada");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_OPCION_COBRO_INGRESADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro")).as("rows of CobroComisionOpcionesCobro").hasSize(5);
        verificar("CTA-B-05_crear-fecha-vigencia-iso-con-zona-es-aceptada", r);
    }

    @Test
    @DisplayName("CTA-B-06: percentage boundaries 0.0 and 1.0 are valid")
    void ctaB06_crearPorcentajeLimites0Y1Validos() {
        // CrearCobroComisionOpcionesCobro.java:166 (< 0.0 or > 1.0)
        // Boundaries: 0.0 and 1.0 are accepted (check is < 0.0 or > 1.0).
        Resultado r = ejecutar("CTA-B-06_crear-porcentaje-limites-0-y-1-validos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_OPCION_COBRO_INGRESADA", "UI_OPCION_COBRO_INGRESADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro")).as("rows of CobroComisionOpcionesCobro").hasSize(6);
        verificar("CTA-B-06_crear-porcentaje-limites-0-y-1-validos", r);
    }

    @Test
    @DisplayName("CTA-B-07: one invalid percentage blocks the whole batch")
    void ctaB07_crearUnPorcentajeNegativoBloqueaTodo() {
        // CrearCobroComisionOpcionesCobro.java:164-181 (errors counted before any insert)
        // All-or-nothing: one invalid percentage rejects the whole batch (also the valid option 3).
        Resultado r = ejecutar("CTA-B-07_crear-un-porcentaje-negativo-bloquea-todo");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_PORCENTAJE_NO_VALIDO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-07_crear-un-porcentaje-negativo-bloquea-todo", r);
    }
}
