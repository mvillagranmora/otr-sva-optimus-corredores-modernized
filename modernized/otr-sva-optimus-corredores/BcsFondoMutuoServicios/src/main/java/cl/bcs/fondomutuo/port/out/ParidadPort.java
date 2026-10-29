package cl.bcs.fondomutuo.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Outbound port: currency parity (tipo de cambio) calculation.
 * Delegates to the paridad microservice.
 */
public interface ParidadPort {
    BigDecimal calcularParidad(String idMonedaOrigen, String idMonedaDestino, LocalDate fecha, Long idEmpresa);
    BigDecimal convertirMonto(BigDecimal monto, String idMonedaOrigen, String idMonedaDestino, LocalDate fecha, Long idEmpresa);
}
