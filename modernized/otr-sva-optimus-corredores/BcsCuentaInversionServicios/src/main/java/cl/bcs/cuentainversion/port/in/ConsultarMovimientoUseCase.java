package cl.bcs.cuentainversion.port.in;

import cl.bcs.cuentainversion.domain.Movimiento;

import java.time.LocalDate;
import java.util.List;

public interface ConsultarMovimientoUseCase {
    List<Movimiento> consultar(Long idEmpresa, Long idCuentaInversion,
            LocalDate fechaDesde, LocalDate fechaHasta, boolean incluyeAnulados);
}
