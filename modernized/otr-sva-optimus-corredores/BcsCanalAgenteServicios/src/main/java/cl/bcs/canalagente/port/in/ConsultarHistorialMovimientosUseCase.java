package cl.bcs.canalagente.port.in;

import cl.bcs.canalagente.domain.HistorialMovimientosRequest;
import cl.bcs.canalagente.domain.HistorialMovimientosResponse;

/**
 * Inbound port: consultar historial de movimientos de cuenta inversión.
 */
public interface ConsultarHistorialMovimientosUseCase {

    HistorialMovimientosResponse consultar(HistorialMovimientosRequest request);
}
