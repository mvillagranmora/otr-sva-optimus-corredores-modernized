package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.EliminarCobroComisionRegistrosService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.EliminarCobroComisionRegistros} (legacy/.../cuentaInversion/EliminarCobroComisionRegistros.java) against the modern
 * {@code EliminarCobroComisionRegistrosService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class EliminarCobroComisionRegistrosServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-40_eliminar-registros-portafolios-marca-inactivos",
            "CTA-B-41_eliminar-registros-inactiva-editado-vigente",
            "CTA-B-42_eliminar-registros-sin-ids-numericos",
            "CTA-B-43_eliminar-registros-ids-mezclados-y-punto-y-coma-final",
            "CTA-B-44_eliminar-registros-ids-nulo-lanza-excepcion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new EliminarCobroComisionRegistrosService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (EliminarCobroComisionRegistros): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-40: VIG patrimony rows of the portfolios become INA, one audit per portfolio")
    void ctaB40_eliminarRegistrosPortafoliosMarcaInactivos() {
        // EliminarCobroComisionRegistros.java:176-194 (batches CUSTODIA/CAJA, UI_REGISTROS_ANULADOS_CORRECTAMENTE)
        // VIG patrimony rows of the portfolios in the date range become INA (custody and cash); one audit entry per portfolio.
        Resultado r = ejecutar("CTA-B-40_eliminar-registros-portafolios-marca-inactivos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTROS_ANULADOS_CORRECTAMENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja", "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones", "BSCL.Custodia.BD::CUS.PatrimonioCustodia");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.Custodia.BD::CUS.PatrimonioCustodia")).as("rows of PatrimonioCustodia").hasSize(5);
        verificar("CTA-B-40_eliminar-registros-portafolios-marca-inactivos", r);
    }

    @Test
    @DisplayName("CTA-B-41: the VIG edited row of the portfolio/period becomes INA")
    void ctaB41_eliminarRegistrosInactivaEditadoVigente() {
        // EliminarCobroComisionRegistros.java:160-175
        // The VIG edited row of the portfolio/period is set INA as well.
        Resultado r = ejecutar("CTA-B-41_eliminar-registros-inactiva-editado-vigente");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTROS_ANULADOS_CORRECTAMENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja", "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones", "BSCL.Custodia.BD::CUS.PatrimonioCustodia");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.Custodia.BD::CUS.PatrimonioCustodia")).as("rows of PatrimonioCustodia").hasSize(5);
        verificar("CTA-B-41_eliminar-registros-inactiva-editado-vigente", r);
    }

    @Test
    @DisplayName("CTA-B-42: no numeric portfolio id is rejected")
    void ctaB42_eliminarRegistrosSinIdsNumericos() {
        // EliminarCobroComisionRegistros.java:150-156 (UI_ERROR_ENTRADA_SIN_DATA)
        // Non numeric ids are dropped; nothing numeric -> UI_ERROR_ENTRADA_SIN_DATA.
        Resultado r = ejecutar("CTA-B-42_eliminar-registros-sin-ids-numericos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_ENTRADA_SIN_DATA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-42_eliminar-registros-sin-ids-numericos", r);
    }

    @Test
    @DisplayName("CTA-B-43: only the numeric ids are used")
    void ctaB43_eliminarRegistrosIdsMezcladosYPuntoYComaFinal() {
        // EliminarCobroComisionRegistros.java:150 (split(";") + NumberUtils.isNumber)
        // Only the numeric ids are used; trailing separator tolerated.
        Resultado r = ejecutar("CTA-B-43_eliminar-registros-ids-mezclados-y-punto-y-coma-final");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_REGISTROS_ANULADOS_CORRECTAMENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja", "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones", "BSCL.Custodia.BD::CUS.PatrimonioCustodia");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.Custodia.BD::CUS.PatrimonioCustodia")).as("rows of PatrimonioCustodia").hasSize(5);
        verificar("CTA-B-43_eliminar-registros-ids-mezclados-y-punto-y-coma-final", r);
    }

    @Test
    @DisplayName("CTA-B-44: absent p_IdPortafolios throws NullPointerException (defect)")
    void ctaB44_eliminarRegistrosIdsNuloLanzaExcepcion() {
        // EliminarCobroComisionRegistros.java:150
        // LEGACY DEFECT: p_IdPortafolios absent -> NullPointerException (no validation). Only the exception class is pinned.
        Resultado r = ejecutar("CTA-B-44_eliminar-registros-ids-nulo-lanza-excepcion");
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        verificar("CTA-B-44_eliminar-registros-ids-nulo-lanza-excepcion", r);
    }
}
