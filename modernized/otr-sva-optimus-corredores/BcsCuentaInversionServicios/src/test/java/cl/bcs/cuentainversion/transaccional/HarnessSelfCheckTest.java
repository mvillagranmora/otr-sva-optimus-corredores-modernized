package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.transaccional.support.ActualizarSaldoReferencia;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Proves the characterization harness itself: (1) it can go GREEN against the legacy-recorded goldens when the behaviour is the legacy one
 * (executable reference = same SQL statements in the legacy order, support/ActualizarSaldoReferencia), and (2) it goes RED when that
 * behaviour is mutated in one small way that matters (a sign, an ordering, an inequality), i.e. the goldens pin the behaviour.
 * This is also the "break it and watch a test fail" evidence required for the suite.
 */
@ExtendWith(ZonaUtc.class)
class HarnessSelfCheckTest {

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    /** ActualizarSaldoCargoAbono goldens whose behaviour is a pure function of the SQL sequence (no exception, call() not a no-op). */
    static final List<String> REFERENCIA = List.of(
            "CTA-A-01_saldos-acumulados-con-saldo-anterior",
            "CTA-A-02_orden-fecha-sistema-vs-efectiva-y-desempate",
            "CTA-A-03_solo-movimientos-certificados",
            "CTA-A-04_varias-cuentas-y-duplicadas",
            "CTA-A-05_todos-S-lista-vacia-cuentas-activas",
            "CTA-A-06_N-lista-vacia-no-hace-nada",
            "CTA-A-07_todos-S-con-lista-no-hace-nada",
            "CTA-A-09_fecha-posterior-a-todos-los-movimientos",
            "CTA-A-11_primer-movimiento-cargo-saldo-negativo");

    private static void comparar(String nombre, java.util.function.BiFunction<String, String, String> mutador) {
        Caso caso = Caso.cargar(nombre);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> ActualizarSaldoReferencia.ejecutar(caso.jdbc(), caso.entrada().deepCopy(), mutador));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("HarnessSelfCheck");
        assertThat(EJECUTADOS.total()).isGreaterThan(0);
    }

    @Test
    void referenciaConSqlDeProduccionCoincideConLosGoldensDelLegacy() {
        for (String c : REFERENCIA) {
            comparar(c, null);
        }
    }

    // mutant 1: MOVCAR no longer counts negative (Select_Cargo_Abono_Movimiento_Paso.sql: -1*Monto)
    @Test
    void mutanteSignoDelCargoEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(0), (n, s) ->
                n.equals("Select_Cargo_Abono_Movimiento_Paso") ? s.replace("-1*B.\"Monto\"", "B.\"Monto\"") : s))
                .isInstanceOf(AssertionError.class);
    }

    // mutant 2: balance by MOVEMENT date accumulated in EFFECTIVE-date order (window ORDER BY swapped)
    @Test
    void mutanteOrdenDeLaVentanaEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(1), (n, s) ->
                n.equals("Select_Saldo_Fecha_Movimiento")
                        ? s.replace("ORDER BY CBM.\"FechaSistema\", CBM.\"IdCargoAbonoMovimiento\" asc", "ORDER BY CBM.\"FechaEfectivaMovimiento\", CBM.\"IdCargoAbonoMovimiento\" asc")
                        : s))
                .isInstanceOf(AssertionError.class);
    }

    // mutant 3: only CERTIFIED movements take part -> include every state (WHERE B."IdEstado" = 'CRT' removed)
    @Test
    void mutanteFiltroDeEstadoEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(2), (n, s) ->
                n.equals("Select_Cargo_Abono_Movimiento_Paso") ? s.replace("WHERE B.\"IdEstado\" = 'CRT'", "WHERE B.\"IdEstado\" IN ('CRT','PEN','ANU')") : s))
                .isInstanceOf(AssertionError.class);
    }

    // mutant 4: previous balance looked up with <= instead of < the movement date
    @Test
    void mutanteDesigualdadDeFechaAnteriorEsDetectado() {
        assertThatThrownBy(() -> comparar(REFERENCIA.get(0), (n, s) ->
                n.equals("Select_Saldo_Fecha_Movimiento") ? s.replace("\"FechaSistema\" < :p_FechaMovimiento", "\"FechaSistema\" <= :p_FechaMovimiento") : s))
                .isInstanceOf(AssertionError.class);
    }
}
