package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco} (entry point {@code call(conn, json)}) as
 * {@code ActualizarSaldosCuentaBancoService#execute(JsonObject)}: recomputes, from a date on, the running available and accounting balance stored in
 * every comprobante forma de pago of the given bank accounts (previous balance + movements ordered by time and id; pending = not yet affecting balance).
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-204..210} (output + TES.ComprobanteFormaDePago balances).
 */
@ExtendWith(ZonaUtc.class)
class ActualizarSaldosCuentaBancoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-204_una-cuenta-recalcula-saldo-disponible-y-contable-acumulados",
            "TES-B-205_dos-cuentas-una-sin-saldo-anterior",
            "TES-B-206_fecha-posterior-a-los-movimientos-no-actualiza-nada",
            "TES-B-207_sin-cuentas-no-actualiza-nada",
            "TES-B-208_fecha-nula-no-encuentra-movimientos",
            "TES-B-209_cuenta-sin-comprobantes-no-actualiza-nada",
            "TES-B-210_fecha-con-formato-invalido-lanza-SQLException-y-limpia-la-tabla-temporal");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> new ActualizarSaldosCuentaBancoService(caso.jdbc()).execute(caso.entrada().deepCopy()));
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ActualizarSaldosCuentaBanco");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ban/ActualizarSaldosCuentaBanco.java:168-239 + sql/ActualizarSaldosCuentaBanco_*.sql: one account: opening balance from the last balance of the previous day, ANU rows add 0, equal times ordered by id, accounting balance = pending + movements
    @Test
    void tesB204_unaCuentaRecalculaSaldoDisponibleYContableAcumulados() {
        verificar(CASOS.get(0));
    }

    // ban/ActualizarSaldosCuentaBanco.java:194-220: two accounts, the second one without a previous balance (starts at 0)
    @Test
    void tesB205_dosCuentasUnaSinSaldoAnterior() {
        verificar(CASOS.get(1));
    }

    // ban/ActualizarSaldosCuentaBanco.java:222-225: no movement on/after the date: no update
    @Test
    void tesB206_fechaPosteriorALosMovimientosNoActualizaNada() {
        verificar(CASOS.get(2));
    }

    // ban/ActualizarSaldosCuentaBanco.java:193,222: empty account list
    @Test
    void tesB207_sinCuentasNoActualizaNada() {
        verificar(CASOS.get(3));
    }

    // ban/ActualizarSaldosCuentaBanco.java:179: null date: nothing matches
    @Test
    void tesB208_fechaNulaNoEncuentraMovimientos() {
        verificar(CASOS.get(4));
    }

    // ban/ActualizarSaldosCuentaBanco.java:208: an account without comprobantes
    @Test
    void tesB209_cuentaSinComprobantesNoActualizaNada() {
        verificar(CASOS.get(5));
    }

    // ban/ActualizarSaldosCuentaBanco.java:228-235: an invalid date makes the query fail: error message added, exception rethrown, staging table emptied in finally
    @Test
    void tesB210_fechaInvalidaLanzaSqlExceptionYLimpiaLaTablaTemporal() {
        verificar(CASOS.get(6));
    }

}
