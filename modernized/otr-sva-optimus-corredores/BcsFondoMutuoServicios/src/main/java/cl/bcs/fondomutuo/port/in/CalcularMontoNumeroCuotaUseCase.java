package cl.bcs.fondomutuo.port.in;

import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaEntrada;
import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaSalida;

/**
 * Use case: Calculate monto ↔ número de cuotas conversion.
 */
public interface CalcularMontoNumeroCuotaUseCase {
    CalcularMontoNumeroCuotaSalida calcular(CalcularMontoNumeroCuotaEntrada entrada);
}
