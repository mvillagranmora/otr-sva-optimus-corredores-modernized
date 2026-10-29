package cl.bcs.cuentainversion.port.out;

import cl.bcs.cuentainversion.domain.*;

import java.time.LocalDate;
import java.util.List;

public interface HanaCargoAbonoPort {
    CrearCargoAbonoSalida callCrearCargoAbonoHanaProc(Long idEmpresa, Long idUsuario,
            String idModulo, String simularOperacion, LocalDate fechaSistema,
            String esIngresoManual, List<CargoAbono> cargos);
    void insertarCargosAGenerar(List<CargoAbono> cargos);
    List<CargoAbonoMovimiento> consultarMovimientosParaActualizar(Long idTrabajo, List<Long> idsCuentaInversion);
    void actualizarSaldos(Long idTrabajo, List<CargoAbonoMovimiento> movimientos);
}
