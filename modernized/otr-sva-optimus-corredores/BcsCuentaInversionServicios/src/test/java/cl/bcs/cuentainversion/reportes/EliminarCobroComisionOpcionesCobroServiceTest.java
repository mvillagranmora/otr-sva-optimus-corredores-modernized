package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.EliminarCobroComisionOpcionesCobroService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.EliminarCobroComisionOpcionesCobro} (legacy/.../cuentaInversion/EliminarCobroComisionOpcionesCobro.java) against the modern
 * {@code EliminarCobroComisionOpcionesCobroService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class EliminarCobroComisionOpcionesCobroServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-13_eliminar-opcion-cobro-existente",
            "CTA-B-14_eliminar-opcion-cobro-inexistente-no-falla",
            "CTA-B-15_eliminar-opcion-cobro-sin-opciones");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new EliminarCobroComisionOpcionesCobroService(caso.dataSource())::execute);
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
        System.out.println("equivalence cases executed (EliminarCobroComisionOpcionesCobro): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-13: existing option is deleted, nothing audited (defect)")
    void ctaB13_eliminarOpcionCobroExistente() {
        // EliminarCobroComisionOpcionesCobro.java:144 (batch delete), :146 (agregarLog(deleteOpciones) with an empty list)
        // Row deleted. LEGACY DEFECT: the audit entry is built from an empty list (deleteOpciones), so nothing is audited.
        Resultado r = ejecutar("CTA-B-13_eliminar-opcion-cobro-existente");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro")).as("rows of CobroComisionOpcionesCobro").hasSize(3);
        verificar("CTA-B-13_eliminar-opcion-cobro-existente", r);
    }

    @Test
    @DisplayName("CTA-B-14: deleting an unknown id is silent")
    void ctaB14_eliminarOpcionCobroInexistenteNoFalla() {
        // EliminarCobroComisionOpcionesCobro.java:144 (DELETE ... WHERE IdOpcionCobro, no existence check)
        // Deleting an id that does not exist is silent: no message, no error, no change.
        Resultado r = ejecutar("CTA-B-14_eliminar-opcion-cobro-inexistente-no-falla");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-14_eliminar-opcion-cobro-inexistente-no-falla", r);
    }

    @Test
    @DisplayName("CTA-B-15: empty request is rejected")
    void ctaB15_eliminarOpcionCobroSinOpciones() {
        // EliminarCobroComisionOpcionesCobro.java:138-142
        // Empty list -> UI_ERROR_ENTRADA_SIN_CONCEPTOS.
        Resultado r = ejecutar("CTA-B-15_eliminar-opcion-cobro-sin-opciones");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_ENTRADA_SIN_CONCEPTOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-15_eliminar-opcion-cobro-sin-opciones", r);
    }
}
