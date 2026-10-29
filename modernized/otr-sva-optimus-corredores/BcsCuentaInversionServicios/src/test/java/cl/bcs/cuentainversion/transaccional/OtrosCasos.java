package cl.bcs.cuentainversion.transaccional;

import java.util.ArrayList;
import java.util.List;

/** Case names run by the test classes of the remaining legacy classes (aggregated for TransaccionalInventarioTest). */
final class OtrosCasos {

    private OtrosCasos() {
    }

    static List<String> todos() {
        List<String> todos = new ArrayList<>(CargaMasivaServicesTest.CASOS);
        todos.addAll(GenerarArchivoCargoAbonoServiceTest.CASOS);
        todos.addAll(ConsultaComparacionSaldoServiceTest.CASOS);
        todos.addAll(CuadraturaCierreDiarioServiceTest.CASOS);
        todos.addAll(ConsultarMovimientoCuentaInversionServiceTest.CASOS);
        todos.addAll(ConsultarCartolaDeMovimientoServiceTest.CASOS);
        todos.addAll(CrearCargoAbonoWSServiceTest.CASOS);
        todos.addAll(ConsultarMovimientosWSServiceTest.CASOS);
        todos.addAll(CrearCargoAbonoProcedimientoAlmacenadoServiceTest.CASOS);
        todos.addAll(ConsultarSaldoMoneyMarketServiceTest.CASOS);
        return todos;
    }
}
