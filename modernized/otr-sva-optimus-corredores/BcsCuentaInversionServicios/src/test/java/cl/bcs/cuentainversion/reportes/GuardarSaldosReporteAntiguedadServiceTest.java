package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.GuardarSaldosReporteAntiguedadService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GuardarSaldosReporteAntiguedad} (legacy/.../cuentaInversion/GuardarSaldosReporteAntiguedad.java) against the modern
 * {@code GuardarSaldosReporteAntiguedadService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class GuardarSaldosReporteAntiguedadServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-63_guardar-viernes-reprocesa-3-dias-y-cubre-el-fin-de-semana",
            "CTA-B-64_guardar-lunes-siguiente-habil-inmediato-no-agrega-dias",
            "CTA-B-65_guardar-feriado-del-lunes-extiende-las-fechas",
            "CTA-B-66_guardar-sin-saldos-del-dia-anterior",
            "CTA-B-67_guardar-sobrescribe-saldos-ya-guardados",
            "CTA-B-68_guardar-dias-por-reprocesar-configurable");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new GuardarSaldosReporteAntiguedadService(caso.jdbc())::execute);
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
        System.out.println("equivalence cases executed (GuardarSaldosReporteAntiguedad): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-63: Friday: reprocess 3 days and cover the weekend up to the next business day")
    void ctaB63_guardarViernesReprocesa3DiasYCubreElFinDeSemana() {
        // GuardarSaldosReporteAntiguedad.java:121-155 (dates), :300 (UPSERT)
        // System date Friday: reprocesses the previous DIASREPROCESOSALDOREPORTEANTIGUEDAD (3) calendar days and, because the
        // next business day is not the next day, also the days up to it (03-15, 03-16) and the system date: 03-11 .. 03-16 are
        // upserted from the 03-10 balances.
        Resultado r = ejecutar("CTA-B-63_guardar-viernes-reprocesa-3-dias-y-cubre-el-fin-de-semana");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(28);
        verificar("CTA-B-63_guardar-viernes-reprocesa-3-dias-y-cubre-el-fin-de-semana", r);
    }

    @Test
    @DisplayName("CTA-B-64: Monday: next business day is immediate, no extra days")
    void ctaB64_guardarLunesSiguienteHabilInmediatoNoAgregaDias() {
        // GuardarSaldosReporteAntiguedad.java:136-153
        // System date Monday: the next business day (Tuesday) is exactly one day ahead, so only the previous 3 days
        // (03-14..03-16) and the system date are upserted.
        Resultado r = ejecutar("CTA-B-64_guardar-lunes-siguiente-habil-inmediato-no-agrega-dias");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(14);
        verificar("CTA-B-64_guardar-lunes-siguiente-habil-inmediato-no-agrega-dias", r);
    }

    @Test
    @DisplayName("CTA-B-65: a Monday holiday extends the processed dates")
    void ctaB65_guardarFeriadoDelLunesExtiendeLasFechas() {
        // GuardarSaldosReporteAntiguedad.java:136-147 (ObtenerDiaSiguiente with FERIADO)
        // A holiday on Monday 2025-03-17 pushes the next business day to Tuesday: 03-15, 03-16 and 03-17 are also upserted.
        Resultado r = ejecutar("CTA-B-65_guardar-feriado-del-lunes-extiende-las-fechas");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(32);
        verificar("CTA-B-65_guardar-feriado-del-lunes-extiende-las-fechas", r);
    }

    @Test
    @DisplayName("CTA-B-66: no balances on the day before the first reprocessed day -> error")
    void ctaB66_guardarSinSaldosDelDiaAnterior() {
        // GuardarSaldosReporteAntiguedad.java:157-168
        // No balances on the day before the earliest reprocessed day -> ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD.
        Resultado r = ejecutar("CTA-B-66_guardar-sin-saldos-del-dia-anterior");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-66_guardar-sin-saldos-del-dia-anterior", r);
    }

    @Test
    @DisplayName("CTA-B-67: already stored balances of a day are replaced (UPSERT)")
    void ctaB67_guardarSobrescribeSaldosYaGuardados() {
        // GuardarSaldosReporteAntiguedad.java:300-306 + UpsertSaldoReporteAntiguedad.sql
        // Re-running a day replaces the stored balance (UPSERT) instead of duplicating it.
        Resultado r = ejecutar("CTA-B-67_guardar-sobrescribe-saldos-ya-guardados");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(28);
        verificar("CTA-B-67_guardar-sobrescribe-saldos-ya-guardados", r);
    }

    @Test
    @DisplayName("CTA-B-68: DIASREPROCESOSALDOREPORTEANTIGUEDAD is configurable")
    void ctaB68_guardarDiasPorReprocesarConfigurable() {
        // GuardarSaldosReporteAntiguedad.java:118-121
        // DIASREPROCESOSALDOREPORTEANTIGUEDAD = 1: only 03-13 plus the weekend (03-15, 03-16) and the system date are processed,
        // starting from the 03-12 balances.
        Resultado r = ejecutar("CTA-B-68_guardar-dias-por-reprocesar-configurable");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad")).as("rows of SaldosReporteAntiguedad").hasSize(14);
        verificar("CTA-B-68_guardar-dias-por-reprocesar-configurable", r);
    }
}
