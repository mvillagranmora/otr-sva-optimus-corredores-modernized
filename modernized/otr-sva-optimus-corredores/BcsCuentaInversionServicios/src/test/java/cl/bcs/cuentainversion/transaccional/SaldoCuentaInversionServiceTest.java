package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.SaldoCuentaInversionService;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.SaldoCuentaInversion} (entry point {@code call(conn, json)}) as
 * {@link SaldoCuentaInversionService#execute(com.google.gson.JsonObject)}: per account of the requested portfolios and per day of the range,
 * the balance by EFFECTIVE date and by MOVEMENT date (accumulated certified movements + previous balance), pending cargos/abonos,
 * filters (negative/positive balance), and initial/final balances.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-77..90} (recorded from the unmodified legacy class).
 */
@ExtendWith(ZonaUtc.class)
class SaldoCuentaInversionServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-77_sin-movimientos-un-dia-devuelve-ceros",
            "CTA-A-78_rango-con-saldo-anterior-y-fechas-distintas",
            "CTA-A-79_incluye-cargos-pendientes",
            "CTA-A-80_incluye-abonos-pendientes",
            "CTA-A-81_incluye-cargos-y-abonos-pendientes",
            "CTA-A-82_solo-saldo-disponible-positivo",
            "CTA-A-83_solo-saldo-negativo",
            "CTA-A-84_solo-saldo-disponible-negativo-es-distinto-de-cero",
            "CTA-A-85_filtro-por-moneda-USD",
            "CTA-A-86_sin-portafolios-consulta-todas-las-cuentas",
            "CTA-A-87_fecha-desde-posterior-a-fecha-hasta",
            "CTA-A-88_fecha-desde-nula-lanza-excepcion",
            "CTA-A-89_portafolios-repetidos-no-duplican-cuentas",
            "CTA-A-90_anulados-y-fuera-de-rango-no-cuentan");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    /** The lists come from SQL without ORDER BY / HashMap iteration: compared as multisets. */
    private static final String[] SIN_ORDEN = {"P_OUT_SALDOINICIALFINAL", "P_OUT_SALDOPORFECHAEFECTIVA", "P_OUT_SALDOPORFECHAMOVIMIENTO"};

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        SaldoCuentaInversionService service = new SaldoCuentaInversionService(caso.jdbc());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("SaldoCuentaInversion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // SaldoCuentaInversion.java:437-447,589-629: one row per account and day, zero balances, one initial/final row per account
    @Test
    void ctaA77_sinMovimientosUnDiaDevuelveCeros() {
        verificar(CASOS.get(0));
    }

    // SaldoCuentaInversion.java:459-488 + SelectSaldoAnterior*.sql: previous balance = stored Saldo / SaldoPorFechaMovimiento of the last
    // certified movement before FechaDesde; movement date (FechaSistema) and effective date (FechaEfectivaMovimiento) accumulate separately
    @Test
    void ctaA78_rangoConSaldoAnteriorYFechasDistintas() {
        verificar(CASOS.get(1));
    }

    // SaldoCuentaInversion.java:514-526,603-614: Saldo = disponible - cargos pendientes
    @Test
    void ctaA79_incluyeCargosPendientes() {
        verificar(CASOS.get(2));
    }

    // SaldoCuentaInversion.java:520-526,616-621: Saldo = disponible + abonos pendientes
    @Test
    void ctaA80_incluyeAbonosPendientes() {
        verificar(CASOS.get(3));
    }

    // SaldoCuentaInversion.java:514-517,603-608: Saldo = disponible - cargos + abonos pendientes
    @Test
    void ctaA81_incluyeCargosYAbonosPendientes() {
        verificar(CASOS.get(4));
    }

    // SaldoCuentaInversion.java:499: p_SoloSaldoDisponible = S keeps only rows whose effective balance is > 0
    @Test
    void ctaA82_soloSaldoDisponiblePositivo() {
        verificar(CASOS.get(5));
    }

    // SaldoCuentaInversion.java:500: p_SoloSaldoNegativo = S keeps only effective balance < 0; the end balances of filtered accounts stay 0
    @Test
    void ctaA83_soloSaldoNegativo() {
        verificar(CASOS.get(6));
    }

    // SaldoCuentaInversion.java:501: p_SoloSaldoDisponibleNegativo = S keeps balance <> 0 (the name says "negative" but it is "different from zero")
    @Test
    void ctaA84_soloSaldoDisponibleNegativoEsDistintoDeCero() {
        verificar(CASOS.get(7));
    }

    // SaldoCuentaInversion.java:364-365,372-377 + SelectCuentaInversion.sql: filter by currency of p_CuentaInversionAConsultar
    @Test
    void ctaA85_filtroPorMonedaUsd() {
        verificar(CASOS.get(8));
    }

    // SaldoCuentaInversion.java:371-373 + SelectCuentaInversionFull.sql: no portfolio -> every account of the base
    @Test
    void ctaA86_sinPortafoliosConsultaTodasLasCuentas() {
        verificar(CASOS.get(9));
    }

    // SaldoCuentaInversion.java:440: loop while fDesde <= fHasta: with FechaDesde > FechaHasta there are no day rows
    @Test
    void ctaA87_fechaDesdePosteriorAFechaHasta() {
        verificar(CASOS.get(10));
    }

    // SaldoCuentaInversion.java:340: Fecha.toDate(null) -> NullPointerException
    @Test
    void ctaA88_fechaDesdeNulaLanzaExcepcion() {
        verificar(CASOS.get(11));
    }

    // SaldoCuentaInversion.java:343-349,389-398: portfolios are de-duplicated (Set) and accounts by IdCuentaInversion
    @Test
    void ctaA89_portafoliosRepetidosNoDuplicanCuentas() {
        verificar(CASOS.get(12));
    }

    // SelectMovimientos.sql: IdEstado IN (CRT, PEN) and date BETWEEN FechaDesde AND FechaHasta
    @Test
    void ctaA90_anuladosYFueraDeRangoNoCuentan() {
        verificar(CASOS.get(13));
    }
}
