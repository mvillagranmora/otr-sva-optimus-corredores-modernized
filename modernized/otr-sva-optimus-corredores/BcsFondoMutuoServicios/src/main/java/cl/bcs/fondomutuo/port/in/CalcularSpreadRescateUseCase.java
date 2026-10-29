package cl.bcs.fondomutuo.port.in;

import cl.bcs.fondomutuo.domain.CalcularSpreadRescateEntrada;
import cl.bcs.fondomutuo.domain.CalcularSpreadRescateSalida;

/**
 * Use case: Calculate spread on rescate (redemption) operations.
 */
public interface CalcularSpreadRescateUseCase {
    CalcularSpreadRescateSalida calcular(CalcularSpreadRescateEntrada entrada);
}
