package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.ConsultaAntiguedadDeudasCuentaInversionService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Activated pending test (see reportes/README.md "Pending tests").
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultaAntiguedadDeudasCuentaInversion} (legacy/.../cuentaInversion/ConsultaAntiguedadDeudasCuentaInversion.java) against the modern {@code ConsultaAntiguedadDeudasCuentaInversionService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores).
 */
class ConsultaAntiguedadDeudasCuentaInversionServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-93_antiguedad-sin-separar-deuda-creciente-por-tramos",
            "CTA-B-94_antiguedad-separa-intermediacion-y-fecha-de-deuda-por-tipo",
            "CTA-B-95_antiguedad-deuda-que-se-paga-reduce-los-tramos-antiguos",
            "CTA-B-96_antiguedad-saldo-no-negativo-borra-la-deuda-de-tramos-anteriores",
            "CTA-B-97_antiguedad-dia-con-un-solo-tipo-de-saldo-se-ignora",
            "CTA-B-98_antiguedad-todas-las-monedas-convierte-a-clp-con-paridad",
            "CTA-B-99_antiguedad-consolidado-reune-los-portafolios-del-cliente",
            "CTA-B-100_antiguedad-agrupa-portafolios",
            "CTA-B-101_antiguedad-sin-cuentas-con-saldo-negativo",
            "CTA-B-102_antiguedad-todas-las-monedas-con-consolidar-s-no-consolida-ni-convierte",
            "CTA-B-103_antiguedad-clp-trunca-decimales-y-muestra-valores-absolutos",
            "CTA-B-104_antiguedad-moneda-extranjera-dos-decimales-half-even",
            "CTA-B-105_antiguedad-empresa-inexistente-lanza-excepcion",
            "CTA-B-106_antiguedad-cliente-relacionado-sigue-como-no-relacionado",
            "CTA-B-107_antiguedad-cuenta-sin-historial-de-saldos-da-fila-en-cero",
            "CTA-B-108_antiguedad-historial-fuera-de-la-ventana-de-31-dias-no-cuenta");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new ConsultaAntiguedadDeudasCuentaInversionService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (ConsultaAntiguedadDeudasCuentaInversion): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-93: no intermediation split: debt grows by tranche")
    void ctaB93_antiguedadSinSepararDeudaCrecientePorTramos() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:349-404 (tramos), :1155 asignarSaldosPorTramo
        // Balances of the last days of each tramo (D -50, C -80, B -100, A -150 = general + intermediation) become the debt per
        // tramo by differences: D 50, C 30, B 20, A 50, total 150. FechaDeuda = oldest day with a negative balance, formatted
        // dd-MM-yyyy.
        Contexto x = ejecutar("CTA-B-93_antiguedad-sin-separar-deuda-creciente-por-tramos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-94: intermediation split with a debt date per balance type (G/I)")
    void ctaB94_antiguedadSeparaIntermediacionYFechaDeDeudaPorTipo() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:447-463, :1190
        // SeparaIntermediacion=S: general row (general debts D 30, C 20, B 20, A 40) followed by an intermediation row (D 20, C
        // 10, B 0, A 10) when its total is positive; one FechaDeuda per type.
        Contexto x = ejecutar("CTA-B-94_antiguedad-separa-intermediacion-y-fecha-de-deuda-por-tipo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-95: a paid debt reduces the oldest tranches")
    void ctaB95_antiguedadDeudaQueSePagaReduceLosTramosAntiguos() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:1238 borrarDeudaTramoAnterior
        // Debt paid down over time (-100, -60, -30, -10): the positive differences are netted against the older tramos
        // (repasarMontosPorTramos) leaving only 10 in tramo D.
        Contexto x = ejecutar("CTA-B-95_antiguedad-deuda-que-se-paga-reduce-los-tramos-antiguos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-96: a non negative balance erases the debt of earlier tranches")
    void ctaB96_antiguedadSaldoNoNegativoBorraLaDeudaDeTramosAnteriores() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:1155-1250
        // A day with a total balance >= 0 (03-05, tramo B) wipes the debt recorded in older tramos (C, D); only the later -40
        // (tramo A) remains.
        Contexto x = ejecutar("CTA-B-96_antiguedad-saldo-no-negativo-borra-la-deuda-de-tramos-anteriores");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-97: a day with only one balance type is ignored (defect)")
    void ctaB97_antiguedadDiaConUnSoloTipoDeSaldoSeIgnora() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:1190 (both lists must be non empty)
        // LEGACY DEFECT: a day that has only a general (or only an intermediation) balance row is not assigned to any tramo
        // (both types are required), so the debt of 2025-03-14 is lost.
        Contexto x = ejecutar("CTA-B-97_antiguedad-dia-con-un-solo-tipo-de-saldo-se-ignora");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-98: all currencies: converted to CLP through CalcularParidad")
    void ctaB98_antiguedadTodasLasMonedasConvierteAClpConParidad() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:349 (hashMoneda.size() > 1)
        // 'TODAS': one ConsultarSaldo call per currency (recorded in llamadasColaboradores); with more than one currency and
        // Consolidar=N the USD balances and aging rows are converted to CLP with CalcularParidad (USD>CLP = 950.5 in the fake)
        // and every amount is truncated to 0 decimals (-10 USD = 9505 CLP). Tramos without rows net the older debt into tramo A
        // (same quirk as CTA-B-103). Row order follows the unordered currency query, so it is not compared.
        Contexto x = ejecutar("CTA-B-98_antiguedad-todas-las-monedas-convierte-a-clp-con-paridad");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-99: consolidated: the client portfolios are merged")
    void ctaB99_antiguedadConsolidadoReuneLosPortafoliosDelCliente() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:305, :404
        // Consolidar=S, one currency: clients with a negative total (1001: -150) give ONE row (no portfolio id), tramos built
        // from the aging rows of all the client portfolios; the staging table gets the available balance only (not retained) as
        // SaldoFinal. Sums: D -60, A -120 and, with B/C empty, all 120 land in tramo A.
        Contexto x = ejecutar("CTA-B-99_antiguedad-consolidado-reune-los-portafolios-del-cliente");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-100: AgrupaPortafolios = S groups the portfolios")
    void ctaB100_antiguedadAgrupaPortafolios() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:611
        // AgrupaPortafolios=S: the per-account rows are added up per portfolio (one row per portfolio known to the client query)
        // with the oldest debt date of the portfolio.
        Contexto x = ejecutar("CTA-B-100_antiguedad-agrupa-portafolios");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-101: no accounts with a negative balance")
    void ctaB101_antiguedadSinCuentasConSaldoNegativo() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:313 (SIN_SALDO_CUENTAS)
        // No negative account -> ERROR SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO and nothing is written (the staging table is not
        // touched).
        Contexto x = ejecutar("CTA-B-101_antiguedad-sin-cuentas-con-saldo-negativo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-102: all currencies + Consolidar=S: neither consolidates nor converts (defect)")
    void ctaB102_antiguedadTodasLasMonedasConConsolidarSNoConsolidaNiConvierte() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:305, :349
        // LEGACY QUIRK: Consolidar=S only consolidates when there is exactly one currency; with several it takes the per-account
        // path and skips the currency conversion (the conversion only runs for Consolidar=N), mixing CLP and USD amounts.
        Contexto x = ejecutar("CTA-B-102_antiguedad-todas-las-monedas-con-consolidar-s-no-consolida-ni-convierte");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-103: CLP truncates decimals and shows absolute values")
    void ctaB103_antiguedadClpTruncaDecimalesYMuestraValoresAbsolutos() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:1061-1065 (darFormatoMontos)
        // CLP amounts are truncated (DOWN) to 0 decimals and made positive: -151.98 is reported as 151. QUIRK: tramos B and C
        // have no aging rows (balance 0), so the netting against them cancels the older debt of tramo D (-51.8) and the whole
        // 151 ends in tramo A.
        Contexto x = ejecutar("CTA-B-103_antiguedad-clp-trunca-decimales-y-muestra-valores-absolutos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-104: foreign currency: two decimals HALF_EVEN")
    void ctaB104_antiguedadMonedaExtranjeraDosDecimalesHalfEven() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:1061-1100
        // Single non-CLP currency: amounts rounded HALF_EVEN to 2 decimals; ConsultarSaldo is asked with that currency in the
        // account filter.
        Contexto x = ejecutar("CTA-B-104_antiguedad-moneda-extranjera-dos-decimales-half-even");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-105: unknown company throws")
    void ctaB105_antiguedadEmpresaInexistenteLanzaExcepcion() {
        // ConsultaAntiguedadDeudasCuentaInversion.java (ConsultarEmpresa: Empresa no existe)
        // ConsultarEmpresa fails with 'Empresa no existe'; the exception propagates.
        Contexto x = ejecutar("CTA-B-105_antiguedad-empresa-inexistente-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.Exception").isEqualTo("java.lang.Exception");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-106: related client is still classified as not related (defect)")
    void ctaB106_antiguedadClienteRelacionadoSigueComoNoRelacionado() {
        // ConsultaAntiguedadDeudasCuentaInversion.java (Relacionado compared with "S")
        // LEGACY DEFECT: Relacionado SI is compared with 'S'; the related legal entity (name from RazonSocial) is classified
        // like any other client.
        Contexto x = ejecutar("CTA-B-106_antiguedad-cliente-relacionado-sigue-como-no-relacionado");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-107: account without balance history gives a zero row")
    void ctaB107_antiguedadCuentaSinHistorialDeSaldosDaFilaEnCero() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:1130 obtenerSaldosReporteAntiguedad
        // A negative account with no rows in SaldosReporteAntiguedad still yields a row, with all tramos 0, no FechaDeuda.
        Contexto x = ejecutar("CTA-B-107_antiguedad-cuenta-sin-historial-de-saldos-da-fila-en-cero");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-108: history outside the 31 days window does not count")
    void ctaB108_antiguedadHistorialFueraDeLaVentanaDe31DiasNoCuenta() {
        // ConsultaAntiguedadDeudasCuentaInversion.java:1130-1155
        // Aging rows older than 2025-02-11 (31 days before the query date) are outside the window; but the FechaDeuda query has
        // no window and reports the oldest negative day (2025-02-10).
        Contexto x = ejecutar("CTA-B-108_antiguedad-historial-fuera-de-la-ventana-de-31-dias-no-cuenta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
