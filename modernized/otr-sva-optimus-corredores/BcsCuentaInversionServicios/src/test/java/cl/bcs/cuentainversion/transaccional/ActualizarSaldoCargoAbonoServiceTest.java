package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono} as
 * {@link ActualizarSaldoCargoAbonoService#execute(com.google.gson.JsonObject)}: recalculates the running balances
 * ({@code Saldo} by effective date, {@code SaldoPorFechaMovimiento} by movement date) of the CERTIFIED movements of the selected
 * accounts through a step table, from the previous stored balance.
 *
 * <p>Goldens ({@code src/test/resources/transaccional/cases/CTA-A-01..11}) were recorded by executing the unmodified legacy class
 * (see README.md here). Each test runs the modern service against the same H2 data and compares the output JSON and the
 * table dumps.
 */
@ExtendWith(ZonaUtc.class)
class ActualizarSaldoCargoAbonoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-01_saldos-acumulados-con-saldo-anterior",
            "CTA-A-02_orden-fecha-sistema-vs-efectiva-y-desempate",
            "CTA-A-03_solo-movimientos-certificados",
            "CTA-A-04_varias-cuentas-y-duplicadas",
            "CTA-A-05_todos-S-lista-vacia-cuentas-activas",
            "CTA-A-06_N-lista-vacia-no-hace-nada",
            "CTA-A-07_todos-S-con-lista-no-hace-nada",
            "CTA-A-08_call-no-ejecuta-logica",
            "CTA-A-09_fecha-posterior-a-todos-los-movimientos",
            "CTA-A-10_cuentas-nulas-lanza-excepcion",
            "CTA-A-11_primer-movimiento-cargo-saldo-negativo");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ActualizarSaldoCargoAbonoService service = new ActualizarSaldoCargoAbonoService(caso.jdbc());
        // CTA-A-08 pins the legacy JavaCall entry point call(), which is a no-op; every other case runs the logic
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> "call".equals(caso.caso().get("metodo").getAsString())
                ? service.call(caso.entrada().deepCopy())
                : service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ActualizarSaldoCargoAbono");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ActualizarSaldoCargoAbono.java:184-248 + SQL Select_Saldo_Fecha_Movimiento/Efectiva: previous balance = stored balance of the
    // last row before p_FechaMovimiento; MOVCAR counts negative (Select_Cargo_Abono_Movimiento_Paso.sql: -1*Monto)
    @Test
    void ctaA01_saldosAcumuladosConSaldoAnterior() {
        verificar(CASOS.get(0));
    }

    // Select_Saldo_Fecha_Movimiento.sql ORDER BY FechaSistema,Id ; Select_Saldo_Fecha_Efectiva.sql ORDER BY FechaEfectiva,FechaCertificacion,Id
    @Test
    void ctaA02_ordenPorFechaSistemaYPorFechaEfectivaConDesempate() {
        verificar(CASOS.get(1));
    }

    // Select_Cargo_Abono_Movimiento_Paso.sql: WHERE B."IdEstado" = 'CRT' (PEN/ANU never enter the step table)
    @Test
    void ctaA03_soloMovimientosCertificadosParticipan() {
        verificar(CASOS.get(2));
    }

    // ActualizarSaldoCargoAbono.java:154-174: distinct account ids are loaded into the temp table; accounts not listed are untouched
    @Test
    void ctaA04_variasCuentasYCuentaDuplicadaEnLaLista() {
        verificar(CASOS.get(3));
    }

    // ActualizarSaldoCargoAbono.java:176-181 + Insert_Temp_Table_Cuenta_Inversion_Pauta.sql: 'S' + empty list = every ACT account
    @Test
    void ctaA05_todosSConListaVaciaProcesaCuentasActivas() {
        verificar(CASOS.get(4));
    }

    // ActualizarSaldoCargoAbono.java:154: 'N' with an empty list selects nothing
    @Test
    void ctaA06_nConListaVaciaNoHaceNada() {
        verificar(CASOS.get(5));
    }

    // ActualizarSaldoCargoAbono.java:154,176: 'S' with a non-empty list matches neither condition (legacy quirk): nothing is updated
    @Test
    void ctaA07_todosSConListaNoVaciaNoHaceNada() {
        verificar(CASOS.get(6));
    }

    // ActualizarSaldoCargoAbono.java:256-262: the JavaCall entry point call() parses the input and returns null WITHOUT running
    // logica(): the JSON entry point is a no-op (LEGACY DEFECT, see README "Legacy defects for SME review")
    @Test
    void ctaA08_callNoEjecutaLogica() {
        verificar(CASOS.get(7));
    }

    // Select_Saldo_Fecha_*.sql WHERE FechaSistema/FechaEfectivaMovimiento >= :p_FechaMovimiento: nothing at or after the date
    @Test
    void ctaA09_fechaPosteriorATodosLosMovimientosNoModificaSaldos() {
        verificar(CASOS.get(8));
    }

    // ActualizarSaldoCargoAbono.java:154: entrada.p_CuentaInversion.isEmpty() with a null list -> NullPointerException
    @Test
    void ctaA10_cuentasNulasLanzaExcepcion() {
        verificar(CASOS.get(9));
    }

    // Select_Saldo_Fecha_*.sql: COALESCE(previous,0) + running SUM; first movement a MOVCAR gives a negative balance
    @Test
    void ctaA11_primerMovimientoCargoDejaSaldoNegativo() {
        verificar(CASOS.get(10));
    }
}
