package cl.bcs.cartolas.jobs.port.in;

import cl.bcs.cartolas.jobs.domain.CartolasJobRequest;

/**
 * Caso de uso de entrada: ejecutar el proceso de cartolas.
 *
 * B-CAR-01: Orquestación del proceso de cartolas por TipoProceso
 */
public interface CartolasJobUseCase {

    /**
     * Ejecuta el proceso de cartolas según el tipo de proceso indicado.
     *
     * @param request parámetros del job (jobId, idEmpresa, tipoProceso)
     */
    void ejecutar(CartolasJobRequest request);
}
