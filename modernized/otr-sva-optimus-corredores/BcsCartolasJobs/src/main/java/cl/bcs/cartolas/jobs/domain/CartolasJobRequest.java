package cl.bcs.cartolas.jobs.domain;

/**
 * Parámetros de entrada para el proceso de cartolas.
 *
 * Oracle: cl.bcs.normativa.cartolas.procesos.ContextoCartolas + Properties cartolas.properties
 *
 * B-CAR-02: Parámetros del proceso de cartolas
 */
public record CartolasJobRequest(
        /** Identificador del job (BAT.Programa.IdPrograma) */
        long jobId,

        /** Identificador de la empresa */
        long idEmpresa,

        /** Tipo de proceso: PROGRAMADO, NORMAL, RECURRENTE, REPROCESO, REEMPAQUETA */
        String tipoProceso
) {

    public CartolasJobRequest {
        java.util.Objects.requireNonNull(tipoProceso, "tipoProceso no puede ser null");
        if (tipoProceso.isBlank()) {
            throw new IllegalArgumentException("tipoProceso no puede ser vacío");
        }
        if (idEmpresa <= 0) {
            throw new IllegalArgumentException("idEmpresa debe ser positivo");
        }
    }
}
