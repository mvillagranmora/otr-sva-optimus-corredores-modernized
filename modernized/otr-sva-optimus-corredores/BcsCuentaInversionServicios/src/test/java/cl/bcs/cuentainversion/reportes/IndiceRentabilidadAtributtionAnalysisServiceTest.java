package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.IndiceRentabilidadAttributionAnalysisService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.IndiceRentabilidadAtributtionAnalysis} (legacy/.../cuentaInversion/IndiceRentabilidadAtributtionAnalysis.java) against the modern
 * {@code IndiceRentabilidadAttributionAnalysisService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 <p>Legacy call() returns null; the oracle invokes procesar(Connection, EntradaClasificacionAtributtion) directly (case.json invocacion).
 */
class IndiceRentabilidadAtributtionAnalysisServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-175_rentabilidad-indice-precio-final-sobre-inicial-menos-uno",
            "CTA-B-176_rentabilidad-redondea-el-cociente-antes-de-restar",
            "CTA-B-177_rentabilidad-un-solo-valor-en-el-rango-da-cero",
            "CTA-B-178_rentabilidad-clasificacion-sin-indice-es-nd-con-cero",
            "CTA-B-179_rentabilidad-indice-sin-valores-en-el-rango-da-cero",
            "CTA-B-180_rentabilidad-varias-clasificaciones-y-rango-sin-datos",
            "CTA-B-181_rentabilidad-sin-clasificaciones-devuelve-lista-vacia");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new IndiceRentabilidadAttributionAnalysisService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (IndiceRentabilidadAtributtionAnalysis): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-175: profitability = round(last/first, 2) - 1, times 100")
    void ctaB175_rentabilidadIndicePrecioFinalSobreInicialMenosUno() {
        // IndiceRentabilidadAtributtionAnalysis.java:152-161
        // Profitability = (last value / first value in the date range, rounded to 2 decimals HALF_UP) - 1, times 100: IPSA 110.5
        // / 100 = 1.105 -> 1.11 -> 11.
        Resultado r = ejecutar("CTA-B-175_rentabilidad-indice-precio-final-sobre-inicial-menos-uno");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-175_rentabilidad-indice-precio-final-sobre-inicial-menos-uno", r);
    }

    @Test
    @DisplayName("CTA-B-176: the quotient is rounded before subtracting")
    void ctaB176_rentabilidadRedondeaElCocienteAntesDeRestar() {
        // IndiceRentabilidadAtributtionAnalysis.java:158 (divide(b, 2, HALF_UP))
        // Range 03-01..03-12: 105 / 100 = 1.05 -> 0.05 -> 5. Rounding is applied to the quotient (2 decimals), not to the
        // percentage.
        Resultado r = ejecutar("CTA-B-176_rentabilidad-redondea-el-cociente-antes-de-restar");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-176_rentabilidad-redondea-el-cociente-antes-de-restar", r);
    }

    @Test
    @DisplayName("CTA-B-177: a single value in the range gives zero")
    void ctaB177_rentabilidadUnSoloValorEnElRangoDaCero() {
        // IndiceRentabilidadAtributtionAnalysis.java:152-161
        // IGPA has a single value in the range: first = last -> 0.
        Resultado r = ejecutar("CTA-B-177_rentabilidad-un-solo-valor-en-el-rango-da-cero");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-177_rentabilidad-un-solo-valor-en-el-rango-da-cero", r);
    }

    @Test
    @DisplayName("CTA-B-178: node without index is ND with zero")
    void ctaB178_rentabilidadClasificacionSinIndiceEsNdConCero() {
        // IndiceRentabilidadAtributtionAnalysis.java:139 (Indice.equals("ND"))
        // A tree node with an empty Indice is reported as 'ND' with profitability 0.
        Resultado r = ejecutar("CTA-B-178_rentabilidad-clasificacion-sin-indice-es-nd-con-cero");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-178_rentabilidad-clasificacion-sin-indice-es-nd-con-cero", r);
    }

    @Test
    @DisplayName("CTA-B-179: index without values in the range gives zero")
    void ctaB179_rentabilidadIndiceSinValoresEnElRangoDaCero() {
        // IndiceRentabilidadAtributtionAnalysis.java:152-157 (null prices replaced by 1)
        // Index with no values in the range: both prices are null, replaced by 1 -> profitability 0.
        Resultado r = ejecutar("CTA-B-179_rentabilidad-indice-sin-valores-en-el-rango-da-cero");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-179_rentabilidad-indice-sin-valores-en-el-rango-da-cero", r);
    }

    @Test
    @DisplayName("CTA-B-180: several nodes and a range without data")
    void ctaB180_rentabilidadVariasClasificacionesYRangoSinDatos() {
        // IndiceRentabilidadAtributtionAnalysis.java:127-171
        // Range without index values: every non ND index gives 0, ND stays ND; rows come from SELECT DISTINCT (order not
        // guaranteed).
        Resultado r = ejecutar("CTA-B-180_rentabilidad-varias-clasificaciones-y-rango-sin-datos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-180_rentabilidad-varias-clasificaciones-y-rango-sin-datos", r);
    }

    @Test
    @DisplayName("CTA-B-181: no classifications returns an empty list")
    void ctaB181_rentabilidadSinClasificacionesDevuelveListaVacia() {
        // IndiceRentabilidadAtributtionAnalysis.java:127-171
        // No classification ids: the temporary table is empty and the output list is empty.
        Resultado r = ejecutar("CTA-B-181_rentabilidad-sin-clasificaciones-devuelve-lista-vacia");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-181_rentabilidad-sin-clasificaciones-devuelve-lista-vacia", r);
    }
}
