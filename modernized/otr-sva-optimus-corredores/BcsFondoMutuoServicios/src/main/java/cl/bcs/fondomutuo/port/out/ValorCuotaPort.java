package cl.bcs.fondomutuo.port.out;

import cl.bcs.fondomutuo.domain.ValorCuota;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port: access to valor cuota (unit price) data.
 */
public interface ValorCuotaPort {
    Optional<ValorCuota> findBySerieAndFecha(String idSerieFondo, LocalDate fecha, Long idEmpresa);
    List<ValorCuota> findBySeries(List<String> idsSerieFondo, Long idEmpresa);
}
