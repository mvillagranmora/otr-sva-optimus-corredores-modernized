package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.CargaInicialSaldosReporteAntiguedadService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.CargaInicialSaldosReporteAntiguedad} (legacy/.../cuentaInversion/CargaInicialSaldosReporteAntiguedad.java) against the modern
 * {@code CargaInicialSaldosReporteAntiguedadService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class CargaInicialSaldosReporteAntiguedadServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-58_carga-inicial-sin-movimientos-replica-saldos-de-todos-los-clientes",
            "CTA-B-59_carga-inicial-con-movimientos-de-varios-clientes",
            "CTA-B-60_carga-inicial-ya-realizada",
            "CTA-B-61_carga-inicial-sin-ningun-saldo-lanza-excepcion",
            "CTA-B-62_carga-inicial-cuenta-sin-saldo-previo-crea-filas-g-e-i");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new CargaInicialSaldosReporteAntiguedadService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (CargaInicialSaldosReporteAntiguedad): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-58: no movements: all clients balances replicated to every day since the last stored one")
    void ctaB58_cargaInicialSinMovimientosReplicaSaldosDeTodosLosClientes() {
        // CargaInicialSaldosReporteAntiguedad.java:125-150, :270-285 (insert for each date)
        // From the last stored day (2025-03-10) to the day before p_FechaSistema (03-13): the balances of ALL clients are
        // inserted for 03-11, 03-12, 03-13.
        Resultado r = ejecutar("CTA-B-58_carga-inicial-sin-movimientos-replica-saldos-de-todos-los-clientes");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(16);
        verificar("CTA-B-58_carga-inicial-sin-movimientos-replica-saldos-de-todos-los-clientes", r);
    }

    @Test
    @DisplayName("CTA-B-59: movements of several clients move their running balances")
    void ctaB59_cargaInicialConMovimientosDeVariosClientes() {
        // CargaInicialSaldosReporteAntiguedad.java:157-262 (per date / per client update), :233-250
        // Movements of the two clients move their running balances from the day they occur; the movement of the last stored day
        // (03-10) and of the system date (03-14) are not applied.
        Resultado r = ejecutar("CTA-B-59_carga-inicial-con-movimientos-de-varios-clientes");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(16);
        verificar("CTA-B-59_carga-inicial-con-movimientos-de-varios-clientes", r);
    }

    @Test
    @DisplayName("CTA-B-60: initial load already done (last stored day = day before system date)")
    void ctaB60_cargaInicialYaRealizada() {
        // CargaInicialSaldosReporteAntiguedad.java:135-137, :292-296
        // The last stored day already is the day before the system date -> ERROR_CARGA_INICIAL_YA_REALIZADA (boundary: number of
        // days = 0).
        Resultado r = ejecutar("CTA-B-60_carga-inicial-ya-realizada");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_CARGA_INICIAL_YA_REALIZADA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-60_carga-inicial-ya-realizada", r);
    }

    @Test
    @DisplayName("CTA-B-61: empty balance table throws NullPointerException (defect)")
    void ctaB61_cargaInicialSinNingunSaldoLanzaExcepcion() {
        // CargaInicialSaldosReporteAntiguedad.java:132 (MAX(FechaSistema) is null)
        // LEGACY DEFECT: with an empty balance table MAX(FechaSistema) is NULL and the code dereferences it ->
        // NullPointerException.
        Resultado r = ejecutar("CTA-B-61_carga-inicial-sin-ningun-saldo-lanza-excepcion");
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        verificar("CTA-B-61_carga-inicial-sin-ningun-saldo-lanza-excepcion", r);
    }

    @Test
    @DisplayName("CTA-B-62: client without previous balance gets G and I rows")
    void ctaB62_cargaInicialCuentaSinSaldoPrevioCreaFilasGEI() {
        // CargaInicialSaldosReporteAntiguedad.java:252-262 (no previous record)
        // A client with movements but no previous balance gets a G and an I row created (dated with p_FechaSistema in the
        // working list, then re-dated per day).
        Resultado r = ejecutar("CTA-B-62_carga-inicial-cuenta-sin-saldo-previo-crea-filas-g-e-i");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(20);
        verificar("CTA-B-62_carga-inicial-cuenta-sin-saldo-previo-crea-filas-g-e-i", r);
    }
}
