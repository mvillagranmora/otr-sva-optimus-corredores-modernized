package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ActualizarSaldosCuentaBancoCorredoraService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ActualizarSaldosCuentaBancoCorredora} (entry point {@code call(conn, json)}) as
 * {@code ActualizarSaldosCuentaBancoCorredoraService#execute(JsonObject)}: runs ActualizarSaldosCuentaBanco (real legacy code in the oracle) for EVERY
 * bank account of the table (no company filter) and reports SALDO_CUENTA_BANCO_CORREDORA_ACTUALIZADOS.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-211..213}.
 */
@ExtendWith(ZonaUtc.class)
class ActualizarSaldosCuentaBancoCorredoraServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-211_corredora-actualiza-todas-las-cuentas-y-informa",
            "TES-B-212_corredora-sin-cuentas-bancarias-no-hace-nada",
            "TES-B-213_corredora-con-fecha-invalida-propaga-la-excepcion");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> new ActualizarSaldosCuentaBancoCorredoraService(caso.jdbc(), new cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso(), new cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService(caso.jdbc())).execute(caso.entrada().deepCopy()));
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ActualizarSaldosCuentaBancoCorredora");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ActualizarSaldosCuentaBancoCorredora.java:62-90: every account of TES.CuentaBanco is updated, informative message at the end
    @Test
    void tesB211_corredoraActualizaTodasLasCuentasYInforma() {
        verificar(CASOS.get(0));
    }

    // ActualizarSaldosCuentaBancoCorredora.java:70: no bank account: nothing happens and no message
    @Test
    void tesB212_corredoraSinCuentasBancariasNoHaceNada() {
        verificar(CASOS.get(1));
    }

    // ActualizarSaldosCuentaBancoCorredora.java:77: the SQL failure of the inner service propagates
    @Test
    void tesB213_corredoraConFechaInvalidaPropagaLaExcepcion() {
        verificar(CASOS.get(2));
    }

}
