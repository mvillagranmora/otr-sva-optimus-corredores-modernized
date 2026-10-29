package cl.bcs.cuentainversion.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EntregarPreciosTasasPort {
    Map<Long, BigDecimal> consultarValoresCuota(Long idEmpresa, List<Long> idsInstrumento, LocalDate fecha);
}
