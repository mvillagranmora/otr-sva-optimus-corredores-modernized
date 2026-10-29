package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.ConsultarCobroComisionRegistrosService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros} (legacy/.../cuentaInversion/ConsultarCobroComisionRegistros.java) against the modern
 * {@code ConsultarCobroComisionRegistrosService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class ConsultarCobroComisionRegistrosServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-26_consultar-registros-febrero-todos",
            "CTA-B-27_consultar-registros-filtro-portafolio-y-estado",
            "CTA-B-28_consultar-registros-monto-editado-reemplaza-suma",
            "CTA-B-29_consultar-registros-producto-agrupador-9999-solo-no-apv",
            "CTA-B-30_consultar-registros-producto-agrupador-apv",
            "CTA-B-31_consultar-registros-sin-resultados");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new ConsultarCobroComisionRegistrosService(caso.dataSource())::execute);
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
        System.out.println("equivalence cases executed (ConsultarCobroComisionRegistros): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-26: February commissions of both portfolios (custody + cash)")
    void ctaB26_consultarRegistrosFebreroTodos() {
        // ConsultarCobroComisionRegistros.java:145-175 + CobroComision_ConsultarRegistros.sql
        // Both portfolios, custody (RF) + cash (CI) commissions, summed per portfolio/state; INA rows and zero amounts excluded
        // when no state filter.
        Resultado r = ejecutar("CTA-B-26_consultar-registros-febrero-todos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-26_consultar-registros-febrero-todos", r);
    }

    @Test
    @DisplayName("CTA-B-27: filter by portfolio and by state INA")
    void ctaB27_consultarRegistrosFiltroPortafolioYEstado() {
        // CobroComision_ConsultarRegistros.sql (:p_Portafolio, :p_EstadoComision)
        // Filter by portfolio and by state INA (the only way to see INA rows).
        Resultado r = ejecutar("CTA-B-27_consultar-registros-filtro-portafolio-y-estado");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-27_consultar-registros-filtro-portafolio-y-estado", r);
    }

    @Test
    @DisplayName("CTA-B-28: an edited amount replaces the computed sum")
    void ctaB28_consultarRegistrosMontoEditadoReemplazaSuma() {
        // CobroComision_ConsultarRegistros.sql (COALESCE(EDIT.MontoComision, SUM(...)))
        // An edited amount (RegistrosEditadosComisiones, same period, state <> INA) replaces the computed SUM in each branch;
        // note the edited amount is added once per branch (custody + cash) by the outer SUM.
        Resultado r = ejecutar("CTA-B-28_consultar-registros-monto-editado-reemplaza-suma");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-28_consultar-registros-monto-editado-reemplaza-suma", r);
    }

    @Test
    @DisplayName("CTA-B-29: product 9999 = non APV portfolios only")
    void ctaB29_consultarRegistrosProductoAgrupador9999SoloNoApv() {
        // CobroComision_ConsultarRegistros.sql (:p_ProductoAgrupador = 9999 AND IdMatrizAgrupadorAPV = 0)
        // ProductoAgrupador 9999 means 'Otros (No APV)': only portfolios with IdMatrizAgrupadorAPV = 0.
        Resultado r = ejecutar("CTA-B-29_consultar-registros-producto-agrupador-9999-solo-no-apv");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-29_consultar-registros-producto-agrupador-9999-solo-no-apv", r);
    }

    @Test
    @DisplayName("CTA-B-30: product 9 = APV portfolio with product name")
    void ctaB30_consultarRegistrosProductoAgrupadorApv() {
        // CobroComision_ConsultarRegistros.sql (PROAGR.IdProductoAgrupador)
        // ProductoAgrupador 9 -> only the APV portfolio 2002 with its product name.
        Resultado r = ejecutar("CTA-B-30_consultar-registros-producto-agrupador-apv");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-30_consultar-registros-producto-agrupador-apv", r);
    }

    @Test
    @DisplayName("CTA-B-31: range without patrimony gives an empty table")
    void ctaB31_consultarRegistrosSinResultados() {
        // ConsultarCobroComisionRegistros.java:163-168
        // No patrimony in the range -> empty P_OUT_TABLA, no messages.
        Resultado r = ejecutar("CTA-B-31_consultar-registros-sin-resultados");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-31_consultar-registros-sin-resultados", r);
    }
}
