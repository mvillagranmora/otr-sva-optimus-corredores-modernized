package cl.bcs.contabilidad.infrastructure.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;

import java.time.Clock;
import java.util.List;

/**
 * The six interface steps in legacy order (CONT-A-06).
 *
 * <p>Legacy: {@code CreaInterfazDiaria.java:118-193}: Cuentas, Movimiento, Saldo, Descriptivas, CuentasBPC,
 * MovimientoBPC.
 */
public final class InterfazGenerators {

    private InterfazGenerators() {
    }

    public static List<InterfazGenerator> enOrdenLegacy(InterfazSapSource sap, Clock clock) {
        return List.of(
                new BroadCuentasGenerator(sap, clock),
                new BroadMovimientoGenerator(sap, clock),
                new BroadSaldoGenerator(sap, clock),
                new DescriptivasBroadGenerator(sap, clock),
                new CuentasBpcGenerator(sap, clock),
                new MovimientoBpcGenerator(sap, clock));
    }
}
