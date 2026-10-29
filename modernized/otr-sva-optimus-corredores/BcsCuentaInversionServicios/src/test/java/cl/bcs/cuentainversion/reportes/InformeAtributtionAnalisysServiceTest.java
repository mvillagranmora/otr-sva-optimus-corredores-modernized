package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.InformeAttributionAnalysisService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.InformeAtributtionAnalisys} (legacy/.../cuentaInversion/InformeAtributtionAnalisys.java) against the modern {@code InformeAttributionAnalysisService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores).
 */
class InformeAtributtionAnalisysServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-213_attribution-informe-completo-dos-mercados",
            "CTA-B-214_attribution-renta-variable-sin-precios-lanza-excepcion",
            "CTA-B-215_attribution-sin-clientes-devuelve-listas-vacias",
            "CTA-B-216_attribution-empresa-inexistente-lanza-excepcion",
            "CTA-B-217_attribution-fecha-desde-invalida-lanza-excepcion",
            "CTA-B-218_attribution-moneda-distinta-convierte-con-paridad",
            "CTA-B-219_attribution-moneda-distinta-sin-paridad-lanza-excepcion",
            "CTA-B-220_attribution-un-solo-dia-desde-igual-hasta",
            "CTA-B-221_attribution-operacion-liquidada-despues-de-la-fecha-hasta",
            "CTA-B-222_attribution-submercado-vacio-no-genera-cabecera-de-submercado",
            "CTA-B-223_attribution-sin-movimientos-de-titulos-ni-caja");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new InformeAttributionAnalysisService(caso.jdbc(), colab)::execute);
        EJECUTADOS.incrementAndGet();
        return new Contexto(caso, colab, r);
    }

    private static void verificar(Contexto x) {
        x.caso().assertEquivalente(x.resultado());
        PendienteSupport.assertLlamadas(x.colaboradores(), x.caso());
        PendienteSupport.assertArchivos(x.caso());
    }

    private static List<String> servicios(FakeColaboradores f) {
        return f.llamadas().stream().map(o -> o.get("servicio").getAsString()).toList();
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
        System.out.println("equivalence cases executed (InformeAtributtionAnalisys): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-213: full report with two markets")
    void ctaB213_attributionInformeCompletoDosMercados() {
        // InformeAtributtionAnalisys.java:237-1000 (logica), :955 (RentabilidadIndices)
        // Two instruments: ACME (RV, prices from EntregarPreciosTasas on FechaDesde and FechaHasta) and BONO1 (RF, values from
        // the portfolio). Output P_OUT_SALIDA: datosSocioNegocio (as answered by DatosCartolaSocioNegocio),
        // RentabilidadClasificacion (name + MonedaReajuste = sum of the profitability), Atributtion (market / submarket / detail
        // rows with value, profit, portfolio percentages, profitability and contribution) and RentabilidadIndices (index
        // profitability by classification, ND when the tree has no index). Calls: ConsultarEmpresa, DatosCartolaSocioNegocio,
        // MovimientosTitulos, MovimientosCaja, EntregarPreciosTasas. Sequence ids are requested for the RV price rows (2 per RV
        // row).
        Contexto x = ejecutar("CTA-B-213_attribution-informe-completo-dos-mercados");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-214: RV row without prices throws NullPointerException (defect)")
    void ctaB214_attributionRentaVariableSinPreciosLanzaExcepcion() {
        // InformeAtributtionAnalisys.java:480
        // LEGACY DEFECT: for an RV row the price of FechaDesde is read from the answer of EntregarPreciosTasas without a null
        // check: no price -> NullPointerException; the catch block adds UI_OPERACION_TIENE_ERRORES to a local output that is
        // thrown away and rethrows.
        Contexto x = ejecutar("CTA-B-214_attribution-renta-variable-sin-precios-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-215: no clients: empty lists")
    void ctaB215_attributionSinClientesDevuelveListasVacias() {
        // InformeAtributtionAnalisys.java:328-345
        // No client/portfolio: the tree query answers nothing and the report only holds the collaborator data and empty lists.
        Contexto x = ejecutar("CTA-B-215_attribution-sin-clientes-devuelve-listas-vacias");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-216: unknown company throws")
    void ctaB216_attributionEmpresaInexistenteLanzaExcepcion() {
        // InformeAtributtionAnalisys.java:263
        // ConsultarEmpresa answers 'Empresa no existe' before the try block: the exception goes straight out (no message list).
        Contexto x = ejecutar("CTA-B-216_attribution-empresa-inexistente-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.Exception").isEqualTo("java.lang.Exception");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-217: invalid FechaDesde throws DateTimeParseException")
    void ctaB217_attributionFechaDesdeInvalidaLanzaExcepcion() {
        // InformeAtributtionAnalisys.java:258
        // LocalDate.parse fails before anything is queried: DateTimeParseException.
        Contexto x = ejecutar("CTA-B-217_attribution-fecha-desde-invalida-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.time.format.DateTimeParseException").isEqualTo("java.time.format.DateTimeParseException");
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-218: p_Moneda different from the instrument currency uses CalcularParidad")
    void ctaB218_attributionMonedaDistintaConvierteConParidad() {
        // InformeAtributtionAnalisys.java:1161 (TransformadorMonedas)
        // p_Moneda USD: instrument values, coupons, dividends and the movements are converted from the reajustment currency with
        // CalcularParidad (CLP>USD 0.001), one call per amount (IdTrabajo/IdInterno 0, p_IdEmpresa fixed to 1).
        Contexto x = ejecutar("CTA-B-218_attribution-moneda-distinta-convierte-con-paridad");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-219: no rate throws NullPointerException (defect)")
    void ctaB219_attributionMonedaDistintaSinParidadLanzaExcepcion() {
        // InformeAtributtionAnalisys.java:1161-1200
        // LEGACY DEFECT: without a rate CalcularParidad answers MontoDestino null; TransformadorMonedas returns null and the
        // next BigDecimal operation fails with NullPointerException.
        Contexto x = ejecutar("CTA-B-219_attribution-moneda-distinta-sin-paridad-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-220: FechaDesde = FechaHasta")
    void ctaB220_attributionUnSoloDiaDesdeIgualHasta() {
        // InformeAtributtionAnalisys.java:700
        // FechaDesde = FechaHasta: the day difference is 0 and the legacy replaces it by 1 to avoid dividing by zero; only the
        // rows dated that day are reported.
        Contexto x = ejecutar("CTA-B-220_attribution-un-solo-dia-desde-igual-hasta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-221: non VIG operation liquidated after FechaHasta still counts")
    void ctaB221_attributionOperacionLiquidadaDespuesDeLaFechaHasta() {
        // InformeAtributtionAnalisys.java:531
        // A non VIG row whose EGRFechaLiquidacion is after FechaHasta still counts in the closing value; a row liquidated before
        // FechaHasta would not.
        Contexto x = ejecutar("CTA-B-221_attribution-operacion-liquidada-despues-de-la-fecha-hasta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-222: empty submarket (reference comparison, driver dependent)")
    void ctaB222_attributionSubmercadoVacioNoGeneraCabeceraDeSubmercado() {
        // InformeAtributtionAnalisys.java:452
        // LEGACY RISK: an empty submarket is detected with a reference comparison (subMercado == ''). With H2 the empty string
        // read from the database is the interned literal, so no submarket header is produced (Bonos disappears from the
        // classification table). With another JDBC driver that returns a new String the header would appear: SME to confirm
        // against HANA.
        Contexto x = ejecutar("CTA-B-222_attribution-submercado-vacio-no-genera-cabecera-de-submercado");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-223: no movements of titles or cash")
    void ctaB223_attributionSinMovimientosDeTitulosNiCaja() {
        // InformeAtributtionAnalisys.java:710-780
        // Empty movement lists: no weighting of abonos/cargos (ponderaciones 0) and the contribution divides the profit by the
        // initial amount only.
        Contexto x = ejecutar("CTA-B-223_attribution-sin-movimientos-de-titulos-ni-caja");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "DatosCartolaSocioNegocio", "MovimientosTitulos", "MovimientosCaja", "EntregarPreciosTasas");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
