package cl.bcs.cuentainversion.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CalcularParidadPort {
    BigDecimal calcularParidad(Long idEmpresa, String idMonedaOrigen, String idMonedaDestino,
            BigDecimal monto, LocalDate fecha);
}
