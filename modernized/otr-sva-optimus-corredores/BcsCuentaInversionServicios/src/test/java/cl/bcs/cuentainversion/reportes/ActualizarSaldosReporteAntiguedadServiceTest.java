package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.ActualizarSaldosReporteAntiguedadService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ActualizarSaldosReporteAntiguedad} (legacy/.../cuentaInversion/ActualizarSaldosReporteAntiguedad.java) against the modern
 * {@code ActualizarSaldosReporteAntiguedadService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class ActualizarSaldosReporteAntiguedadServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-47_actualizar-sin-movimientos-copia-saldos-hacia-adelante",
            "CTA-B-48_actualizar-con-movimientos-acumula-por-dia-y-tipo",
            "CTA-B-49_actualizar-fecha-movimiento-igual-fecha-sistema",
            "CTA-B-50_actualizar-fecha-movimiento-posterior-fecha-sistema",
            "CTA-B-51_actualizar-fecha-movimiento-un-dia-antes-del-sistema",
            "CTA-B-52_actualizar-cuenta-no-encontrada",
            "CTA-B-53_actualizar-normaliza-codigo-portafolio-y-moneda-a-mayusculas",
            "CTA-B-54_actualizar-sin-saldos-del-dia-anterior",
            "CTA-B-55_actualizar-un-solo-tipo-de-saldo-con-movimientos-lanza-excepcion",
            "CTA-B-56_actualizar-dias-corridos-incluye-fin-de-semana",
            "CTA-B-57_actualizar-sobrescribe-saldo-existente-de-un-dia-futuro");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new ActualizarSaldosReporteAntiguedadService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (ActualizarSaldosReporteAntiguedad): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-47: no movements: balances copied forward to every missing calendar day")
    void ctaB47_actualizarSinMovimientosCopiaSaldosHaciaAdelante() {
        // ActualizarSaldosReporteAntiguedad.java:116-136 (dates), :141-145, :226-240 (copy + UPSERT)
        // No movements: the balances of the day before p_FechaMovimiento (2025-03-10) are copied to every calendar day up to the
        // day before the system date (03-11, 03-12, 03-13).
        Resultado r = ejecutar("CTA-B-47_actualizar-sin-movimientos-copia-saldos-hacia-adelante");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(10);
        verificar("CTA-B-47_actualizar-sin-movimientos-copia-saldos-hacia-adelante", r);
    }

    @Test
    @DisplayName("CTA-B-48: movements move the running balance by day and balance type (G/I)")
    void ctaB48_actualizarConMovimientosAcumulaPorDiaYTipo() {
        // ActualizarSaldosReporteAntiguedad.java:147-200 (MOVCAR negates, TipoSaldo I/G), SelectMovimientosCargoAbono.sql (FAC + RV = I)
        // Movements update the running balance: MOVCAR subtracts, MOVABN adds; module FAC + comprobante RV is intermediation
        // (I), anything else general (G). Only certified (CRT) movements of the client account inside [FechaMovimiento, day
        // before system date] count; the balance carries from one day to the next.
        Resultado r = ejecutar("CTA-B-48_actualizar-con-movimientos-acumula-por-dia-y-tipo");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(10);
        verificar("CTA-B-48_actualizar-con-movimientos-acumula-por-dia-y-tipo", r);
    }

    @Test
    @DisplayName("CTA-B-49: movement date equal to the system date is rejected")
    void ctaB49_actualizarFechaMovimientoIgualFechaSistema() {
        // ActualizarSaldosReporteAntiguedad.java:124-127, :253-257
        // p_FechaMovimiento = system date: both previous days coincide -> ERROR_FECHA_DE_MOVIMIENTO_MAYOR_O_IGUAL_FECHA_SISTEMA.
        Resultado r = ejecutar("CTA-B-49_actualizar-fecha-movimiento-igual-fecha-sistema");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_FECHA_DE_MOVIMIENTO_MAYOR_O_IGUAL_FECHA_SISTEMA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-49_actualizar-fecha-movimiento-igual-fecha-sistema", r);
    }

    @Test
    @DisplayName("CTA-B-50: movement date after the system date is rejected")
    void ctaB50_actualizarFechaMovimientoPosteriorFechaSistema() {
        // ActualizarSaldosReporteAntiguedad.java:124-127, :253-257
        // p_FechaMovimiento after the system date -> same error.
        Resultado r = ejecutar("CTA-B-50_actualizar-fecha-movimiento-posterior-fecha-sistema");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_FECHA_DE_MOVIMIENTO_MAYOR_O_IGUAL_FECHA_SISTEMA");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-50_actualizar-fecha-movimiento-posterior-fecha-sistema", r);
    }

    @Test
    @DisplayName("CTA-B-51: movement date one day before the system date loads exactly one day")
    void ctaB51_actualizarFechaMovimientoUnDiaAntesDelSistema() {
        // ActualizarSaldosReporteAntiguedad.java:124-136
        // Boundary: the movement date is the day before the system date: exactly one day (03-13) is loaded, starting from the
        // 03-12 balances.
        Resultado r = ejecutar("CTA-B-51_actualizar-fecha-movimiento-un-dia-antes-del-sistema");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(8);
        verificar("CTA-B-51_actualizar-fecha-movimiento-un-dia-antes-del-sistema", r);
    }

    @Test
    @DisplayName("CTA-B-52: unknown RUT -> account not found")
    void ctaB52_actualizarCuentaNoEncontrada() {
        // ActualizarSaldosReporteAntiguedad.java:137, :247-251, :310-331 (obtenerDatosCliente)
        // Unknown RUT -> ERROR_NO_SE_ENCONTRO_LA_CUENTA_DEL_CLIENTE.
        Resultado r = ejecutar("CTA-B-52_actualizar-cuenta-no-encontrada");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_NO_SE_ENCONTRO_LA_CUENTA_DEL_CLIENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-52_actualizar-cuenta-no-encontrada", r);
    }

    @Test
    @DisplayName("CTA-B-53: portfolio code and currency are upper-cased before the lookup")
    void ctaB53_actualizarNormalizaCodigoPortafolioYMonedaAMayusculas() {
        // ActualizarSaldosReporteAntiguedad.java:318-319
        // CodigoPortafolio and Moneda are upper-cased before the lookup (the RUT is not).
        Resultado r = ejecutar("CTA-B-53_actualizar-normaliza-codigo-portafolio-y-moneda-a-mayusculas");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(10);
        verificar("CTA-B-53_actualizar-normaliza-codigo-portafolio-y-moneda-a-mayusculas", r);
    }

    @Test
    @DisplayName("CTA-B-54: no balances on the starting day -> error")
    void ctaB54_actualizarSinSaldosDelDiaAnterior() {
        // ActualizarSaldosReporteAntiguedad.java:139-145
        // No balance rows for 2025-03-11 (day before the movement date) -> ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD.
        Resultado r = ejecutar("CTA-B-54_actualizar-sin-saldos-del-dia-anterior");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-54_actualizar-sin-saldos-del-dia-anterior", r);
    }

    @Test
    @DisplayName("CTA-B-55: a single balance row with movements throws IndexOutOfBounds (defect)")
    void ctaB55_actualizarUnSoloTipoDeSaldoConMovimientosLanzaExcepcion() {
        // ActualizarSaldosReporteAntiguedad.java:190-200 (saldoCliente.get(1))
        // LEGACY DEFECT: the code assumes exactly two balance rows (G and I) per account (saldoCliente.get(1)); with one row and
        // a movement it throws IndexOutOfBoundsException.
        Resultado r = ejecutar("CTA-B-55_actualizar-un-solo-tipo-de-saldo-con-movimientos-lanza-excepcion");
        assertThat(r.excepcion()).as("legacy threw java.lang.IndexOutOfBoundsException").isEqualTo("java.lang.IndexOutOfBoundsException");
        verificar("CTA-B-55_actualizar-un-solo-tipo-de-saldo-con-movimientos-lanza-excepcion", r);
    }

    @Test
    @DisplayName("CTA-B-56: calendar days: weekends are loaded too")
    void ctaB56_actualizarDiasCorridosIncluyeFinDeSemana() {
        // ActualizarSaldosReporteAntiguedad.java:116-136, :285-308 (diaHabil N, soloCaeDiaHabil N)
        // System date 2025-03-18 (Tue): days 03-14 (Fri) .. 03-17 (Mon) are loaded INCLUDING Saturday 03-15 and Sunday 03-16:
        // calendar days, no business-day skipping.
        Resultado r = ejecutar("CTA-B-56_actualizar-dias-corridos-incluye-fin-de-semana");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(14);
        verificar("CTA-B-56_actualizar-dias-corridos-incluye-fin-de-semana", r);
    }

    @Test
    @DisplayName("CTA-B-57: pre-existing balances of a day to load are replaced (UPSERT)")
    void ctaB57_actualizarSobrescribeSaldoExistenteDeUnDiaFuturo() {
        // ActualizarSaldosReporteAntiguedad.java:240, :370-390 + UpsertSaldoReporteAntiguedad.sql
        // Pre-existing balances of a day to be loaded are replaced (UPSERT keyed by date, client, portfolio, currency, type): no
        // duplicates.
        Resultado r = ejecutar("CTA-B-57_actualizar-sobrescribe-saldo-existente-de-un-dia-futuro");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(10);
        verificar("CTA-B-57_actualizar-sobrescribe-saldo-existente-de-un-dia-futuro", r);
    }
}
